package de.tgx03.uno;

import de.tgx03.ExceptionHandler;
import de.tgx03.uno.game.Game;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * A class representing the server of a game of UNO.
 */
public class Host implements Runnable {

	/**
	 * The lock used to wait for the start of the game.
	 * Required as virtual threads don't play nice with synchronized blocks.
	 */
	private final Lock startLock = new ReentrantLock(false);
	/**
	 * The Condition used to wait for the start of the game.
	 */
	private final Condition waiter = startLock.newCondition();
	/**
	 * This lock gets used for synchronizing on the game state.
	 * Not exactly required for virtual threads, but may slightly advantageous.
	 */
	private final Lock gameLock = new ReentrantLock(true);
	/**
	 * The server socket that accepts new connections.
	 */
	private final ServerSocket serverSocket;
	/**
	 * The rules of the game to use once the game gets started.
	 */
	private final Rules rules;
	/**
	 * A list of all the receivers receiving commands from the clients
	 */
	private final List<Receiver> receivers = new ArrayList<>();
	/**
	 * A list of all the output streams updates get sent through.
	 */
	private final List<ObjectOutputStream> outputs = new ArrayList<>();
	/**
	 * The handlers for exceptions that may occur during operation.
	 */
	private final List<ExceptionHandler> exceptionHandlers = new ArrayList<>(1);

	/**
	 * Whether the game shall be started.
	 */
	private volatile boolean start = false;
	/**
	 * Whether the game shall be stopped.
	 */
	private volatile boolean kill = false;
	/**
	 * The game instance this host deals with.
	 */
	private Game game;

	/**
	 * Creates a new server that listens on the provided port
	 * for clients.
	 *
	 * @param port  The port this server should listen on.
	 * @param rules The rules of the game.
	 * @throws IOException When something goes wrong while starting the server.
	 */
	public Host(int port, @Nullable Rules rules) throws IOException {
		serverSocket = new ServerSocket(port);
		this.rules = rules;
		Thread accepter = new Thread(this, "Host-Main");
		accepter.setDaemon(true);
		accepter.start();
	}

	/**
	 * Starts the round.
	 */
	public synchronized void start() {
		start = true;
		try {
			serverSocket.close();
		} catch (IOException e) {
			handleException(e);
		}
		notifyAll();
	}

	/**
	 * Registers a new object that wishes to handle exceptions that may occur during this hosts execution.
	 *
	 * @param handler The object to be registered as exception handler.
	 */
	public void registerExceptionHandler(@NotNull ExceptionHandler handler) {
		synchronized (exceptionHandlers) {
			exceptionHandlers.add(handler);
		}
	}

	/**
	 * Removes an exception handler that no longer wishes to be informed of exceptions.
	 *
	 * @param handler The handler to remove.
	 */
	public void removeExceptionHandler(@NotNull ExceptionHandler handler) {
		synchronized (exceptionHandlers) {  // TODO: When removing an exception handler while handling an exception a concurrent modification exception occurs.
			exceptionHandlers.remove(handler);
		}
	}

	@Override
	public void run() {
		waitForClients();

		// Set up the game and inform the clients of it
		startLock.lock();
		game = new Game(receivers.size(), rules);
		waiter.signalAll();
		startLock.unlock();

		try {
			update();
		} catch (IOException e) {
			handleException(e);
		}
	}

	/**
	 * Accepts new clients and sets up the connections with them.
	 */
	private void waitForClients() {
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

	/**
	 * Informs all the clients of an update to the game.
	 *
	 * @throws IOException When something went wrong while sending the update.
	 */
	private void update() throws IOException {
		Container<IOException> container = new Container<>();
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
				container.element = e;
			}
		});
		gameLock.unlock();
		if (container.element != null) throw container.element;
	}

	/**
	 * Ends this host, if required by force.
	 */
	public void kill() {
		kill = true;
		boolean lock = false;
		try {
			lock = gameLock.tryLock(5, TimeUnit.SECONDS);
		} catch (InterruptedException ignored) {
		}
		if (lock) {
			try {
				this.end();
			} catch (IOException e) {
				outputs.parallelStream().forEach(stream -> {
					try {
						stream.close();
					} catch (IOException ignored) {
					}
				});
				receivers.parallelStream().forEach(receiver -> {
					try {
						receiver.input.close();
					} catch (IOException ignored) {
					}
				});
			}
		} else {    // Just forcefully close every stream if the lock couldn't be acquired.
			outputs.parallelStream().forEach(stream -> {
				try {
					stream.close();
				} catch (IOException ignored) {
				}
			});
			receivers.parallelStream().forEach(receiver -> {
				try {
					receiver.input.close();
				} catch (IOException ignored) {
				}
			});
		}
		gameLock.unlock();
	}

	/**
	 * Informs all the clients that the game has ended
	 * and shuts down the threads.
	 *
	 * @throws IOException When something goes wrong during sending.
	 */
	private void end() throws IOException {
		Container<IOException> container = new Container<>();
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
				container.element = e;
			}
		});
		gameLock.unlock();
		if (container.element != null) throw container.element;
	}

	/**
	 * Gives an exception that occurred to all the registered handlers.
	 *
	 * @param e The exception to forward.
	 */
	private synchronized void handleException(@NotNull Exception e) {
		synchronized (this.exceptionHandlers) {
			this.exceptionHandlers.parallelStream().forEach(x -> x.handleException(e));
		}
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o instanceof Host h) {
			return this.serverSocket.equals(h.serverSocket) && this.rules.equals(h.rules) && (this.game != null && this.game.equals(h.game));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(serverSocket, rules, game);
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
			while (!start || Host.this.game == null) {
				startLock.lock();
				waiter.awaitUninterruptibly();
				startLock.unlock();
			}

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
						Host.this.update();
					}
				} catch (InterruptedException ignored) {
				} catch (Exception e) {
					handleException(e);
				}
			}
			if (!kill) {
				try {
					Host.this.end();
				} catch (IOException e) {
					handleException(e);
				}
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

	/**
	 * Just a small holder that gets used to hold an exception that occurs in another thread.
	 *
	 * @param <E> Whatever this is supposed to hold.
	 */
	private static class Container<E> {
		/**
		 * The element this holds.
		 */
		private volatile E element;
	}
}
