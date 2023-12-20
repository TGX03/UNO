package de.tgx03.uno.client;

import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.cards.Card;
import de.tgx03.uno.game.cards.Color;
import de.tgx03.uno.messaging.Update;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

	protected void update(Update update) {
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

	public abstract void play(int cardNumber) throws IOException;

	public abstract void jump(int cardNumber) throws IOException;

	public abstract void acceptCards() throws IOException;

	public abstract void takeCard() throws IOException;

	public abstract void selectColor(int cardNumber, @NotNull Color color) throws IOException;
}
