package de.tgx03.uno.game;

import de.tgx03.uno.game.cards.Card;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a single Player
 * holding cards
 */
public class Player implements Serializable {

	private final List<Card> cards = new ArrayList<>(7);
	private transient Card top; // Transient as clients already gets this other ways

	/**
	 * Gives this player 7 cards to start a game
	 */
	protected void initialize() {
		cards.clear();
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
	protected void updateTop(Card card) {
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
	protected void giveCard(int cardNumber, Card card) {
		cards.add(cardNumber, card);
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}

	@Override
	public int hashCode() {
		return cards.hashCode();
	}

	@Override
	public String toString() {
		return cards.toString();
	}
}
