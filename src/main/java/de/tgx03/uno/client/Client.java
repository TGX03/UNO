package de.tgx03.uno.client;

import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.cards.Card;
import de.tgx03.uno.game.cards.Color;
import de.tgx03.uno.messaging.Command;
import de.tgx03.uno.messaging.Update;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * The client of a UNO-Game. It only holds information of its assigned player
 * and handles communication with the host
 */
public class Client implements Runnable {

	private final ObjectInputStream input;
	private final ObjectOutputStream output;
	private final List<ClientUpdate> receivers = new ArrayList<>(1);

	private Player player;
	private Card topCard;

	/**
	 * Creates a new client that is connected to the host and interfaces with it
	 *
	 * @param host     The hostname of the server
	 * @param hostPort The port to connect to
	 * @throws IOException If an error occurred when trying to establish the connection
	 */
	public Client(String host, int hostPort) throws IOException {
		Socket socket = new Socket(host, hostPort);
		output = new ObjectOutputStream(socket.getOutputStream());
		input = new ObjectInputStream(socket.getInputStream());
		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Play the selected card normally
	 * There is no response, whether the operation has actually succeeded
	 * must be determined with the update
	 *
	 * @param cardNumber The card to place
	 * @throws IOException When an error occurs during transmission
	 */
	public synchronized void play(int cardNumber) throws IOException {
		output.reset();
		Command command = new Command(Command.CommandType.NORMAL, cardNumber);
		output.writeObject(command);
	}

	/**
	 * Throws in the selected card no matter whose turn it is
	 * There is no response, whether the operation has actually succeeded
	 * must be determined with the update.
	 *
	 * @param cardNumber The card to throw
	 * @throws IOException When an error occurs during transmission
	 */
	public synchronized void jump(int cardNumber) throws IOException {
		output.reset();
		Command command = new Command(Command.CommandType.JUMP, cardNumber);
		output.writeObject(command);
	}

	/**
	 * Informs the host that the penalty cards get accepted by this client
	 *
	 * @throws IOException When an error occurs during transmission
	 */
	public synchronized void acceptCards() throws IOException {
		output.reset();
		Command command = new Command(Command.CommandType.ACCEPT, -1);
		output.writeObject(command);
	}

	/**
	 * Requests the host to create a new card and add it to this player
	 *
	 * @throws IOException When an error occurs during transmission
	 */
	public synchronized void takeCard() throws IOException {
		output.reset();
		Command command = new Command();
		output.writeObject(command);
	}

	/**
	 * Informs the server which color a +4 or Wild Card should have
	 *
	 * @param cardNumber The number of the card to set
	 * @param color      The desired color
	 * @throws IOException When an error occurs during transmission
	 */
	public synchronized void selectColor(int cardNumber, Color color) throws IOException {
		output.reset();
		Command command = new Command(color, cardNumber);
		output.writeObject(command);
	}

	/**
	 * Returns the player object of this client
	 *
	 * @return The player of this client
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Allows another class to receive updates when this client receives
	 * an update from the host
	 *
	 * @param receiver The class requesting to get updated
	 */
	public void registerReceiver(ClientUpdate receiver) {
		this.receivers.add(receiver);
	}

	@Override
	public String toString() {
		Card[] cards = player.getCards();
		StringBuilder builder = new StringBuilder();
		for (Card card : cards) {
			builder.append(card).append("; ");
		}
		builder.append(System.lineSeparator()).append("Top Card:").append(topCard);
		return builder.toString();
	}

	/**
	 * Basically the daemon waiting for updates from the server
	 */
	@Override
	public void run() {
		while (true) {
			try {
				Update update = (Update) input.readObject();
				synchronized (Client.this) {
					player = update.player;
					topCard = update.topCard;
				}
				for (ClientUpdate receiver : receivers) {
					receiver.update(update);
				}
			} catch (IOException | ClassCastException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
