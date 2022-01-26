package de.tgx03.uno.host;

import de.tgx03.ExceptionHandler;
import de.tgx03.uno.game.Game;
import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.Rules;
import de.tgx03.uno.game.cards.Card;
import de.tgx03.uno.game.cards.ColorChooser;
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

/**
 * A class representing the server of a game of UNO.
 */
public class Host implements Runnable {

	/**
	 * The server socket that accepts new connections.
	 */
	private final ServerSocket serverSocket;
	/**
	 * The rules of the game to use once the game gets started.
	 */
	private final Rules rules;
	/**
	 * A list of all the handlers connecting to the clients.
	 */
	private final List<Handler> handler = new ArrayList<>();
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
		synchronized (exceptionHandlers) {  // TODO: Check the issue that occurs when not doing this in a separate thread.
			exceptionHandlers.remove(handler);
		}
	}

	@Override
	public void run() {
		waitForClients();

		// Set up the game and inform the clients of it
		synchronized (this) {
			game = new Game(handler.size(), rules);
			notifyAll();
		}
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
					Handler handler = new Handler(socket, currentID);
					new Thread(handler, "Host-Receiver " + currentID).start();
					currentID++;
					this.handler.add(handler);
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
	 * @throws IOException When something went wrong while sendig the update.
	 */
	private void update() throws IOException {
		synchronized (game) {
			short[] cardCount = game.getCardCount();
			for (Handler handler : this.handler) {
				handler.update(cardCount);
			}
		}
	}

	/**
	 * Tries to terminate this host and end the round.
	 */
	public void kill() {
		kill = true;
		try {
			this.end();
		} catch (IOException ignored) {
		}
	}

	/**
	 * Informs all the clients that the game has ended
	 * and shuts down the threads.
	 *
	 * @throws IOException When something goes wrong during sending.
	 */
	private void end() throws IOException {
		for (Handler handler : this.handler) {
			handler.end();
		}
	}

	/**
	 * Gives an exception that occurred to all the registered handlers.
	 *
	 * @param e The exception to forward.
	 */
	private synchronized void handleException(@NotNull Exception e) {
		synchronized (this.exceptionHandlers) {
			for (ExceptionHandler exceptionHandler : exceptionHandlers) {
				exceptionHandler.handleException(e);
			}
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
	private class Handler implements Runnable {

		/**
		 * The ID of the player this handler represents.
		 */
		private final int id;
		/**
		 * The input stream for receiving commands from the client.
		 */
		private final ObjectInputStream input;
		/**
		 * The output stream for sending updates to the client.
		 */
		private final ObjectOutputStream output;

		/**
		 * Creates a new handler.
		 *
		 * @param socket The socket of this handler.
		 * @param id     The ID of the player this handler is responsible for.
		 * @throws IOException When a stream couldn't be opened.
		 */
		public Handler(@NotNull Socket socket, int id) throws IOException {
			this.id = id;
			this.input = new ObjectInputStream(socket.getInputStream());
			this.output = new ObjectOutputStream(socket.getOutputStream());
		}

		@Override
		public void run() {

			// Wait until the game starts
			synchronized (Host.this) {
				while (!start || Host.this.game == null) {
					try {
						Host.this.wait();
					} catch (InterruptedException exception) {
						handleException(exception);
					}
				}
			}

			while (!game.hasEnded() && !kill) {
				// Read orders and process them
				try {
					Command order = (Command) input.readObject();
					System.out.println("Received command from player " + this.id + " \"" + order.toString() + "\"");
					boolean success = false;
					synchronized (game) {
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
					}

					// Update the clients when something was changed after execution
					if (success) {
						Host.this.update();
					}
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
		 * Sends an update to the client of this handler.
		 *
		 * @param cardCount How many cards all the players have.
		 * @throws IOException When something goes wrong during send operation.
		 */
		public void update(@NotNull short[] cardCount) throws IOException {
			Update update;
			synchronized (game) {
				boolean turn = game.getCurrentPlayer() == this.id;
				update = new Update(turn, game.getPlayer(this.id), game.getTopCard(), cardCount, (short) game.getStackSize());
			}
			synchronized (output) {
				output.reset();
				output.writeObject(update);
			}
		}

		/**
		 * Sends a last update informing all clients that the round has ended.
		 *
		 * @throws IOException When something goes wrong during send operation.
		 */
		public void end() throws IOException {
			Update update;
			synchronized (game) {
				update = new Update(false, true, game.getPlayer(this.id), game.getTopCard(), new short[game.playerCount()], (short) game.getStackSize());
			}
			synchronized (output) {
				output.reset();
				output.writeObject(update);
			}
			input.close();
		}

		/**
		 * Sets the color of a wild card this player holds.
		 *
		 * @param order The order informing this host of the operation.
		 * @return Whether the operation succeeded.
		 */
		private boolean selectColor(@NotNull Command order) {
			synchronized (game) {
				Player player = game.getPlayer(this.id);
				Card card = player.getCards()[order.cardNumber];
				if (card instanceof ColorChooser) {
					((ColorChooser) card).setColor(order.color);
					return true;
				} else {
					return false;
				}
			}
		}
	}
}
