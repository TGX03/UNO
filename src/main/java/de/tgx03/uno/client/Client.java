package de.tgx03.uno.client;

import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.cards.Card;
import de.tgx03.uno.game.cards.Color;
import de.tgx03.uno.messaging.Command;
import de.tgx03.uno.messaging.Update;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The client of a UNO-Game. It only holds information of its assigned player
 * and handles communication with the host.
 */
public class Client implements Runnable {

	/**
	 * The input from the host were game updates are received.
	 */
	private final ObjectInputStream input;
	/**
	 * The output to the host where requests are sent through.
	 */
	private final ObjectOutputStream output;
	/**
	 * All the receivers that wish to be updated once the host sends an update.
	 */
	private final List<ClientUpdate> receivers = new ArrayList<>(1);

	/**
	 * The player object of this client.
	 * Gets updated with every update from the host.
	 */
	private Player player;
	/**
	 * The card on top of the pile.
	 * Gets updated with every update from the host.
	 */
	private Card topCard;
	/**
	 * Whether the round has ended.
	 * Once set to true, the client thread shuts down.
	 */
	private boolean ended = false;

	/**
	 * Creates a new client that is connected to the host and interfaces with it.
	 *
	 * @param host     The hostname of the server.
	 * @param hostPort The port to connect to.
	 * @throws IOException If an error occurred when trying to establish the connection.
	 */
	public Client(@NotNull String host, int hostPort) throws IOException {
		Socket socket = new Socket(host, hostPort);
		output = new ObjectOutputStream(socket.getOutputStream());
		input = new ObjectInputStream(socket.getInputStream());
		Thread thread = new Thread(this, "Client-Receiver");
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Play the selected card normally.
	 * There is no response, whether the operation has actually succeeded
	 * must be determined with the update.
	 *
	 * @param cardNumber The card to place.
	 * @throws IOException When an error occurs during transmission.
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
	 * @param cardNumber The card to throw.
	 * @throws IOException When an error occurs during transmission.
	 */
	public synchronized void jump(int cardNumber) throws IOException {
		output.reset();
		Command command = new Command(Command.CommandType.JUMP, cardNumber);
		output.writeObject(command);
	}

	/**
	 * Informs the host that the penalty cards get accepted by this client.
	 *
	 * @throws IOException When an error occurs during transmission.
	 */
	public synchronized void acceptCards() throws IOException {
		output.reset();
		Command command = new Command(Command.CommandType.ACCEPT, -1);
		output.writeObject(command);
	}

	/**
	 * Requests the host to create a new card and add it to this player.
	 *
	 * @throws IOException When an error occurs during transmission.
	 */
	public synchronized void takeCard() throws IOException {
		output.reset();
		Command command = new Command();
		output.writeObject(command);
	}

	/**
	 * Informs the server which color a +4 or Wild Card should have.
	 *
	 * @param cardNumber The number of the card to set.
	 * @param color      The desired color.
	 * @throws IOException When an error occurs during transmission.
	 */
	public synchronized void selectColor(int cardNumber, @NotNull Color color) throws IOException {
		output.reset();
		Command command = new Command(color, cardNumber);
		output.writeObject(command);
	}

	/**
	 * Informs this client that the game is to be ended.
	 * The client actually only shuts down when either a new update is received
	 * or an error occurs during last transmission.
	 */
	public synchronized void kill() {
		ended = true;
		try {
			input.close();
		} catch (IOException e) {
			handleException(e);
		}
	}

	/**
	 * Returns the player object of this client
	 *
	 * @return The player of this client
	 */
	@Nullable
	public Player getPlayer() {
		return player;
	}

	/**
	 * Allows another class to receive updates when this client receives
	 * an update from the host.
	 *
	 * @param receiver The class requesting to get updated.
	 */
	public void registerReceiver(@NotNull ClientUpdate receiver) {
		synchronized (this.receivers) {
			this.receivers.add(receiver);
		}
	}

	/**
	 * Removes a receiver to not receive any further updates from this client.
	 *
	 * @param receiver The client to remove.
	 */
	public void removeReceiver(@Nullable ClientUpdate receiver) {
		synchronized (this.receivers) { // TODO: After not doing this in a separate thread, make sure it doesn't cause an isse
			this.receivers.remove(receiver);
		}
	}

	/**
	 * Informs the receivers of this client that an exception occurred.
	 *
	 * @param exception The exception that occurred.
	 */
	private void handleException(@NotNull Exception exception) {
		synchronized (this.receivers) {
			for (ClientUpdate receiver : receivers) {
				receiver.handleException(exception);
			}
		}
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
	 * Basically the daemon waiting for updates from the server.
	 */
	@Override
	public void run() {
		do {
			try {
				Update update = (Update) input.readObject();
				synchronized (Client.this) {
					player = update.player;
					topCard = update.topCard;
				}
				synchronized (this.receivers) {
					for (ClientUpdate receiver : receivers) {
						receiver.update(update);
					}
				}
				if (update.ended) {
					ended = true;
				}
			} catch (IOException | ClassCastException | ClassNotFoundException e) {
				if (!ended) handleException(e);
			}
		} while (!ended);
		System.out.println("Shutting down client thread");
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o instanceof Client c) {
			return output == c.output && input == c.input && player == c.player;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(input, output, player);
	}
}
