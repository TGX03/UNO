package eu.tgx03.uno.client;

import eu.tgx03.uno.game.Player;
import eu.tgx03.uno.game.cards.Card;
import eu.tgx03.uno.game.cards.Color;
import eu.tgx03.uno.messaging.Command;
import eu.tgx03.uno.messaging.Update;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a generic client for UNO, which provides the necessary functions to communicate with the server.
 */
public abstract class Client implements Runnable {

	/**
	 * All the receivers that wish to be updated once the host sends an update.
	 */
	private final List<ClientUpdate> receivers = new ArrayList<>(1);

	/**
	 * The player object of this client.
	 * Gets updated with every update from the host.
	 */
	protected Player player;
	/**
	 * The card on top of the pile.
	 * Gets updated with every update from the host.
	 */
	protected Card topCard;
	/**
	 * Whether the round has ended.
	 * Once set to true, the client thread shuts down.
	 */
	protected boolean ended = false;

	/**
	 * Returns the player object of this client
	 *
	 * @return The player of this client
	 */
	@NotNull
	public Player getPlayer() {
		return player == null ? new Player() : this.player;
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
		synchronized (this.receivers) {
			this.receivers.remove(receiver);
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
	 * Informs the receivers of this client that an exception occurred.
	 *
	 * @param exception The exception that occurred.
	 */
	protected void handleException(@NotNull Exception exception) {
		synchronized (this.receivers) {
			this.receivers.parallelStream().forEach(x -> x.handleException(exception));
		}
	}

	/**
	 * Examines an update and updates the presentation to the Use accordingly.
	 *
	 * @param update The new data.
	 */
	protected final void update(Update update) {
		synchronized (this) {
			player = update.player;
			topCard = update.topCard;
		}
		synchronized (this.receivers) {
			for (ClientUpdate receiver : receivers) {
				receiver.update(update);
			}
		}
	}

	public void kill() {
		ended = true;
	}

	/**
	 * Play the selected card normally.
	 * There is no response, whether the operation has actually succeeded
	 * must be determined with the update.
	 *
	 * @param cardNumber The card to place.
	 * @throws IOException When an error occurs during transmission.
	 */
	public final void play(int cardNumber) throws IOException {
		sendCommand(new Command(Command.CommandType.NORMAL, cardNumber));
	}

	/**
	 * Throws in the selected card no matter whose turn it is
	 * There is no response, whether the operation has actually succeeded
	 * must be determined with the update.
	 *
	 * @param cardNumber The card to throw.
	 * @throws IOException When an error occurs during transmission.
	 */
	public final void jump(int cardNumber) throws IOException {
		sendCommand(new Command(Command.CommandType.JUMP, cardNumber));
	}

	/**
	 * Informs the host that the penalty cards get accepted by this client.
	 *
	 * @throws IOException When an error occurs during transmission.
	 */
	public final void acceptCards() throws IOException {
		sendCommand(new Command(Command.CommandType.ACCEPT, -1));
	}

	/**
	 * Requests the host to create a new card and add it to this player.
	 *
	 * @throws IOException When an error occurs during transmission.
	 */
	public final void takeCard() throws IOException {
		sendCommand(new Command());
	}

	/**
	 * Informs the server which color a +4 or Wild Card should have.
	 *
	 * @param cardNumber The number of the card to set.
	 * @param color      The desired color.
	 * @throws IOException When an error occurs during transmission.
	 */
	public final void selectColor(int cardNumber, @NotNull Color color) throws IOException {
		sendCommand(new Command(color, cardNumber));
	}

	/**
	 * Send a given command to the host through this client.
	 *
	 * @param command The command to send.
	 * @throws IOException When an error occurs during transmission
	 */
	protected abstract void sendCommand(Command command) throws IOException;
}
