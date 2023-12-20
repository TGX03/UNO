package de.tgx03.uno.server;

import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.Rules;
import de.tgx03.uno.game.cards.Card;
import de.tgx03.uno.game.cards.ChooseColor;
import de.tgx03.uno.messaging.Command;
import de.tgx03.uno.messaging.Update;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class SocketServer extends Server {

	/**
	 * The server socket that accepts new connections.
	 */
	private final ServerSocket serverSocket;
	/**
	 * A list of all the receivers receiving commands from the clients
	 */
	private final List<Receiver> receivers = new ArrayList<>();
	/**
	 * A list of all the output streams updates get sent through.
	 */
	private final List<ObjectOutputStream> outputs = new ArrayList<>();

	/**
	 * Creates a new server that listens on the provided port
	 * for clients.
	 *
	 * @param port  The port this server should listen on.
	 * @param rules The rules of the game.
	 * @throws IOException When something goes wrong while starting the server.
	 */
	public SocketServer(int port, @Nullable Rules rules) throws IOException {
		super(rules);
		serverSocket = new ServerSocket(port);
		Thread accepter = new Thread(this, "Host-Main");
		accepter.setDaemon(true);
		accepter.start();
	}

	@Override
	public synchronized void start() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			handleException(e);
		}
		super.start();
	}

	@Override
	public int getPlayerCount() {
		return receivers.size();
	}

	@Override
	protected void waitForClients() {
		int currentID = 0;  // Used to get the ID for each new connection
		do {
			try {
				Socket socket = serverSocket.accept();
				if (!start) {
					Receiver receiver = new Receiver(new ObjectInputStream(socket.getInputStream()), currentID);
					Thread.ofVirtual().name("Host-Receiver " + currentID).start(receiver);
					currentID++;
					outputs.add(new ObjectOutputStream(socket.getOutputStream()));
					this.receivers.add(receiver);
				}
			} catch (SocketException e) {
				if (!start) {
					handleException(e);
				}
			} catch (IOException e) {
				handleException(e);
			}
		} while (!start && !kill);
	}

	@Override
	protected void update() {
		gameLock.lock();
		short[] cardCount = game.getCardCount();
		IntStream.range(0, receivers.size()).parallel().forEach(id -> {
			boolean turn = game.getCurrentPlayer() == id;
			Update update = new Update(turn, game.getPlayer(id), game.getTopCard(), cardCount, (short) game.getStackSize());
			try {
				synchronized (outputs.get(id)) {
					outputs.get(id).reset();
					outputs.get(id).writeObject(update);
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		gameLock.unlock();
	}

	@Override
	public void kill() {
		boolean lock = false;
		try {
			lock = gameLock.tryLock(5, TimeUnit.SECONDS);
		} catch (InterruptedException ignored) {
		}
		if (lock) this.end();
		outputs.parallelStream().forEach(stream -> {
			try {
				stream.close();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		receivers.parallelStream().forEach(receiver -> {
			try {
				receiver.input.close();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		if (lock) gameLock.unlock();
	}

	@Override
	protected void end() {
		gameLock.lock();
		short[] cardCount = game.getCardCount();
		IntStream.range(0, outputs.size()).parallel().forEach(id -> {
			Update update;
			update = new Update(false, true, game.getPlayer(id), game.getTopCard(), cardCount, (short) game.getStackSize());
			try {
				synchronized (outputs.get(id)) {
					outputs.get(id).reset();
					outputs.get(id).writeObject(update);
				}
				receivers.get(id).input.close();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		gameLock.unlock();
	}

	/**
	 * A class handling the connection with a client.
	 */
	private class Receiver implements Runnable {

		/**
		 * The ID of the player this receiver represents.
		 */
		private final int id;
		/**
		 * The input stream for receiving commands from the client.
		 */
		private final ObjectInputStream input;

		/**
		 * Creates a new receiver.
		 *
		 * @param input The input stream this receiver receives updates on.
		 * @param id    The ID of the player this receiver is responsible for.
		 * @throws IOException When a stream couldn't be opened.
		 */
		public Receiver(@NotNull ObjectInputStream input, int id) throws IOException {
			this.id = id;
			this.input = input;
		}

		@Override
		public void run() {

			// Wait until the game starts
			awaitStart();

			while (!game.hasEnded() && !kill) {
				// Read orders and process them
				try {
					Command order = (Command) input.readObject();
					System.out.println("Received command from player " + this.id + " \"" + order.toString() + "\"");
					boolean success = false;
					gameLock.lockInterruptibly();
					switch (order.type) {
						case NORMAL -> {
							if (game.getCurrentPlayer() == this.id) {
								success = game.playCard(order.cardNumber);
							}
						}
						case JUMP -> success = game.jump(this.id, order.cardNumber);
						case ACCEPT -> {
							if (game.getCurrentPlayer() == this.id) {
								success = game.acceptCards();
							}
						}
						case SELECT_COLOR -> success = selectColor(order);
						case TAKE_CARD -> {
							if (game.getCurrentPlayer() == this.id) {
								game.takeCard();
								success = true;
							}
						}
					}
					gameLock.unlock();

					// Update the clients when something was changed after execution
					if (success) {
						SocketServer.this.update();
					}
				} catch (InterruptedException ignored) {
				} catch (Exception e) {
					handleException(e);
				}
			}
			if (!kill) {
				SocketServer.this.end();
			}
			System.out.println("Shutting down host thread");
		}

		/**
		 * Sets the color of a wild card this player holds.
		 *
		 * @param order The order informing this host of the operation.
		 * @return Whether the operation succeeded.
		 */
		private boolean selectColor(@NotNull Command order) {
			assert order.color != null;
			gameLock.lock();
			Player player = game.getPlayer(this.id);
			Card card = player.getCards()[order.cardNumber];
			if (card instanceof ChooseColor cc) {
				cc.setColor(order.color);
				gameLock.unlock();
				return true;
			} else {
				gameLock.unlock();
				return false;
			}
		}
	}
}
