package de.tgx03.uno.messaging;

import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.cards.Card;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * A class providing clients with new data after the state of the game has changed
 */
public class Update implements Serializable {

	@Serial
	private static final long serialVersionUID = 1959833843176392241L;

	/**
	 * Whether it's this player turn
	 */
	public final boolean turn;
	/**
	 * Whether the game has ended because no players hold any more cards
	 */
	public final boolean ended;
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
	 * @param turn   Whether it's this clients turn
	 * @param player The player object representing this player
	 * @param card   The card on top of the pile
	 * @param count  How many cards the other players have
	 */
	public Update(boolean turn, Player player, Card card, short[] count) {
		this.turn = turn;
		this.ended = false;
		this.player = player;
		this.topCard = card;
		this.cardNumbers = count;
	}

	/**
	 * Creates a new update
	 *
	 * @param turn   Whether it's this clients turn
	 * @param ended  Whether the game has ended
	 * @param player The player object representing this player
	 * @param card   The card on top of the pile
	 * @param count  How many cards the other players have
	 */
	public Update(boolean turn, boolean ended, Player player, Card card, short[] count) {
		this.turn = turn;
		this.ended = ended;
		this.player = player;
		this.topCard = card;
		this.cardNumbers = count;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Update update = (Update) o;
		return turn == update.turn && ended == update.ended && player.equals(update.player) && topCard.equals(update.topCard) && Arrays.equals(cardNumbers, update.cardNumbers);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(turn, ended, player, topCard);
		result = 31 * result + Arrays.hashCode(cardNumbers);
		return result;
	}
}
