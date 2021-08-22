package de.tgx03.uno.messaging;

import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.cards.Card;

import java.io.Serializable;

/**
 * A class providing clients with new data after the state of the game has changed
 */
public class Update implements Serializable {

	/**
	 * Whether it's this player turn
	 */
	public final boolean turn;
	/**
	 * The updated player object corresponding to this player
	 */
	public final Player player;
	/**
	 * The card on top of the pile
	 */
	public final Card topCard;
	/**
	 * How many card all the other players have
	 * Done as short to save space and I don't think any player will have more than 60000 cards
	 */
	public final short[] cardNumbers;

	/**
	 * Creates a new update
	 *
	 * @param turn   Whether it's this players turn
	 * @param player The player object representing this palyer
	 * @param card   The card on top of the pile
	 * @param count  How many cards the other players have
	 */
	public Update(boolean turn, Player player, Card card, short[] count) {
		this.turn = turn;
		this.player = player;
		this.topCard = card;
		this.cardNumbers = count;
	}
}
