package de.tgx03.uno.server;

import de.tgx03.uno.game.Game;
import de.tgx03.uno.game.Rules;
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

public class SocketServer extends Server implements Runnable {

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
	 * The rules of the game.
	 */
	private final Rules rules;
	/**
	 * The lock used to make receivers wait for the start of the round.
	 */
	private final Lock startLock = new ReentrantLock();
	/**
	 * The condition derived from startLock to make threads wait for the start of the round.
	 */
	private final Condition startWaiter = startLock.newCondition();

	/**
	 * Creates a new server that listens on the provided port
	 * for clients.
	 *
	 * @param port  The port this server should listen on.
	 * @param rules The rules of the game.
	 * @throws IOException When something goes wrong while starting the server.
	 */
	public SocketServer(int port, @Nullable Rules rules) throws IOException {
		this.rules = rules;
		serverSocket = new ServerSocket(port);
		Thread accepter = new Thread(this, "Host-Main");
		accepter.setDaemon(true);
		accepter.start();
	}

	@Override
	public void start() {
		start = true;
		super.game = new Game(this.getPlayerCount(), rules);
		try {
			serverSocket.close();
		} catch (IOException e) {
			handleException(e);
		}
	}

	@Override
	public int getPlayerCount() {
		return receivers.size();
	}

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
		game.gameLock.lock();
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
		game.gameLock.unlock();
	}

	@Override
	public void kill() {
		boolean lock = false;
		try {
			lock = game.gameLock.tryLock(5, TimeUnit.SECONDS);
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
		if (lock) game.gameLock.unlock();
	}

	@Override
	protected void end() {
		game.gameLock.lock();
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
		game.gameLock.unlock();
	}

	@Override
	public void run() {
		waitForClients();

		startLock.lock();
		startWaiter.signalAll();
		startLock.unlock();
	}

	private void awaitStart() {
		startLock.lock();
		startWaiter.awaitUninterruptibly();
		startLock.unlock();
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
					executeCommand(this.id, order);
				} catch (Exception e) {
					handleException(e);
				}
			}
			if (!kill) {
				SocketServer.this.end();
			}
			System.out.println("Shutting down host thread");
		}
	}
}
