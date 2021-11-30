package de.tgx03.uno.game;

import de.tgx03.uno.game.cards.Card;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a single Player
 * holding cards
 */
public class Player implements Externalizable {

	@Serial
	private static final long serialVersionUID = -1301404883505022064L;

	private final List<Card> cards = new ArrayList<>(7);
	private transient Card top; // Transient as clients already gets this other ways

	/**
	 * Creates a player and gives it 7 cards to start the game
	 */
	public Player() {
		for (int i = 0; i < 7; i++) {
			cards.add(Card.generateCard());
		}
	}

	/**
	 * Lets this player play a card
	 * The card is returned as a result and removed from this player
	 * if successful
	 *
	 * @param cardNumber The number of the card to play
	 * @return The card at the given position if operation succeeded
	 */
	@Nullable
	public Card playCard(int cardNumber) {
		if (cardNumber < cards.size() && cards.get(cardNumber).place(top)) {
			return cards.remove(cardNumber);
		} else {
			return null;
		}
	}

	/**
	 * Tries to throw in a card
	 * The card is returned as a result and removed from this player
	 * if successful
	 *
	 * @param cardNumber The number of the card to jump in
	 * @return The card at the given position if operation succeeded
	 */
	@Nullable
	public Card jumpCard(int cardNumber) {
		if (cardNumber < cards.size() && cards.get(cardNumber).jump(top)) {
			return cards.remove(cardNumber);
		} else {
			return null;
		}
	}

	/**
	 * Lets this player take another card
	 */
	public void drawCard() {
		cards.add(Card.generateCard());
	}

	/**
	 * Informs this player that a new card is on top of the pile
	 *
	 * @param card The new card
	 */
	protected void updateTop(@NotNull Card card) {
		this.top = card;
	}

	/**
	 * Returns how many cards this player is currently holding
	 *
	 * @return How many cards this player has
	 */
	public int cardCount() {
		return cards.size();
	}

	/**
	 * Whether this player has finished the game
	 *
	 * @return Whether the player has played all his cards
	 */
	public boolean won() {
		return cards.size() == 0;
	}

	/**
	 * Returns all the cards this player is currently holding
	 *
	 * @return All the cards of this player
	 */
	@NotNull
	public Card[] getCards() {
		return cards.toArray(new Card[0]);
	}

	/**
	 * Gives a specific card to this player
	 * Gets used to give back a black card
	 *
	 * @param cardNumber Where to place the card
	 * @param card       The card to give back
	 */
	protected void giveCard(int cardNumber, @NotNull Card card) {
		cards.add(cardNumber, card);
	}

	@Override
	public boolean equals(@Nullable Object o) {
		return o == this;
	}

	@Override
	public int hashCode() {
		return cards.hashCode();
	}

	@Override
	@NotNull
	public String toString() {
		return cards.toString();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(this.cards.size());
		for (Card card : cards) {
			out.writeObject(card);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int count = in.readInt();
		this.cards.clear();
		for (int i = 0; i < count; i++) {
			this.cards.add((Card) in.readObject());
		}
	}
}
