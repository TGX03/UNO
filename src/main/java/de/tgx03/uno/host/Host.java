package de.tgx03.uno.host;

import de.tgx03.uno.game.Game;
import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.Rules;
import de.tgx03.uno.game.cards.Card;
import de.tgx03.uno.game.cards.ColorChooser;
import de.tgx03.uno.messaging.Command;
import de.tgx03.uno.messaging.Update;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing the server of a game of UNO
 */
public class Host implements Runnable {

	private final ServerSocket serverSocket;
	private final Rules rules;
	private final List<Handler> handler = new ArrayList<>();

	private boolean start = false;
	private Game game;

	/**
	 * Creates a new server that listens on the provided port
	 * for clients
	 *
	 * @param port  The port this server should listen on
	 * @param rules The rules of the game
	 * @throws IOException When something goes wrong while starting the server
	 */
	public Host(int port, Rules rules) throws IOException {
		serverSocket = new ServerSocket(port);
		this.rules = rules;
		Thread accepter = new Thread(this, "Host-Main");
		accepter.setDaemon(true);
		accepter.start();
	}

	/**
	 * Starts the round
	 */
	public synchronized void start() {
		start = true;
		notifyAll();
	}

	@Override
	public void run() {

		// Take in new players
		new Thread(this::waitForClients, "Host-Accepter").start();

		// Wait for the round to start
		synchronized (this) {
			while (!start) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		// Set up the game and inform the clients of it
		game = new Game(handler.size(), rules);
		try {
			update();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Accepts new clients and sets up the connections with them
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		} while (!start);
	}

	/**
	 * Informs all the clients of an update to the game
	 *
	 * @throws IOException When something went wrong while sendig the update
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
	 * Informs all the clients that the game has ended
	 * and shuts down the threads
	 *
	 * @throws IOException When something goes wrong during sending
	 */
	private void end() throws IOException {
		for (Handler handler : this.handler) {
			handler.end();
		}
	}

	/**
	 * A class handling the connection with a client
	 */
	private class Handler implements Runnable {

		private final int id;
		private final ObjectInputStream input;
		private final ObjectOutputStream output;

		/**
		 * Creates a new handler
		 *
		 * @param socket The socket of this handler
		 * @param id     The ID of the player this handler is responsible for
		 * @throws IOException When a stream couldn't be opened
		 */
		public Handler(Socket socket, int id) throws IOException {
			this.id = id;
			this.input = new ObjectInputStream(socket.getInputStream());
			this.output = new ObjectOutputStream(socket.getOutputStream());
		}

		@Override
		public void run() {

			// Wait until the game starts
			synchronized (Host.this) {
				while (!start) {
					try {
						Host.this.wait();
					} catch (InterruptedException ignored) {
					}
				}
			}

			do {
				// Read orders and process them
				try {
					Command order = (Command) input.readObject();
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
					e.printStackTrace();
				}
			} while (!game.hasEnded());
			try {
				Host.this.end();
			} catch (IOException ignored) {
			}
			System.out.println("Shutting down host thread");
		}

		/**
		 * Sends an update to the client of this handler
		 *
		 * @param cardCount How many cards all the players have
		 * @throws IOException When something goes wrong during send operation
		 */
		public void update(short[] cardCount) throws IOException {
			Update update;
			synchronized (game) {
				boolean turn = game.getCurrentPlayer() == this.id;
				update = new Update(turn, game.getPlayer(this.id), game.getTopCard(), cardCount);
			}
			synchronized (output) {
				output.reset();
				output.writeObject(update);
			}
		}

		/**
		 * Sends a last update informing all clients that the round has ended
		 *
		 * @throws IOException When something goes wrong during send operation
		 */
		public void end() throws IOException {
			Update update;
			synchronized (game) {
				update = new Update(false, true, game.getPlayer(this.id), game.getTopCard(), new short[game.playerCount()]);
			}
			synchronized (output) {
				output.reset();
				output.writeObject(update);
			}
		}

		/**
		 * Sets the color of a wild card this player holds
		 *
		 * @param order The order informing this host of the operation
		 * @return Whether the operation succeeded
		 */
		private boolean selectColor(Command order) {
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
