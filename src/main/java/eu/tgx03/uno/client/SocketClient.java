package eu.tgx03.uno.client;

import eu.tgx03.uno.game.cards.Card;
import eu.tgx03.uno.game.cards.Color;
import eu.tgx03.uno.messaging.Command;
import eu.tgx03.uno.messaging.Update;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.Objects;

/**
 * The client of a UNO-Game. It only holds information of its assigned player
 * and handles communication with the host.
 */
public class SocketClient extends Client {

	/**
	 * The input from the host were game updates are received.
	 */
	private final ObjectInputStream input;
	/**
	 * The output to the host where requests are sent through.
	 */
	private final ObjectOutputStream output;


	/**
	 * Creates a new client that is connected to the host and interfaces with it.
	 *
	 * @param host     The hostname of the server.
	 * @param hostPort The port to connect to.
	 * @throws IOException If an error occurred when trying to establish the connection.
	 */
	public SocketClient(@NotNull String host, int hostPort) throws IOException {
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
		super.kill();
		try {
			input.close();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
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
				this.update(update);
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
		if (o instanceof SocketClient c) {
			return output == c.output && input == c.input && player == c.player;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(input, output, player);
	}
}
