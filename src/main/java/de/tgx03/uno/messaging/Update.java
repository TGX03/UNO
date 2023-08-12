package de.tgx03.uno.messaging;

import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.cards.Card;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

/**
 * A class providing clients with new data after the state of the game has changed.
 */
public class Update implements Externalizable {

	@Serial
	private static final long serialVersionUID = 1959833843176392241L;

	/**
	 * The reflective field of the boolean stating whether it's this players turn
	 * Used for deserialization.
	 */
	private static final Field TURN_FIELD;
	/**
	 * The reflective field of the boolean stating whether the game has ended.
	 * Used for deserialization.
	 */
	private static final Field END_FIELD;
	/**
	 * The reflective field of the corresponding player object for the client.
	 * Used for deserialization.
	 */
	private static final Field PLAYER_FIELD;
	/**
	 * The reflective field of the card currently lying on top.
	 * Used for deserialization.
	 */
	private static final Field CARD_FIELD;
	/**
	 * The reflective field of how many cards the other players have.
	 * Used for deserialization.
	 */
	private static final Field NUMBERS_FIELD;
	/**
	 * The reflective field of how many cards are on the stack.
	 * Used for deserialization.
	 */
	private static final Field STACK_FIELD;

	static {
		try {
			TURN_FIELD = Update.class.getDeclaredField("turn");
			END_FIELD = Update.class.getDeclaredField("ended");
			PLAYER_FIELD = Update.class.getDeclaredField("player");
			CARD_FIELD = Update.class.getDeclaredField("topCard");
			NUMBERS_FIELD = Update.class.getDeclaredField("cardNumbers");
			STACK_FIELD = Update.class.getDeclaredField("stack");
			TURN_FIELD.setAccessible(true);
			END_FIELD.setAccessible(true);
			PLAYER_FIELD.setAccessible(true);
			CARD_FIELD.setAccessible(true);
			NUMBERS_FIELD.setAccessible(true);
			STACK_FIELD.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	/**
	 * Whether it's this player turn.
	 */
	public final boolean turn;
	/**
	 * Whether the game has ended because no players hold any more cards.
	 */
	public final boolean ended;
	/**
	 * The updated player object corresponding to this player.
	 */
	public final Player player;
	/**
	 * The card on top of the pile.
	 */
	public final Card topCard;
	/**
	 * How many cards all the other players have.
	 * Done as short to save space, and I don't think any player will have more than 30000 cards.
	 */
	public final short[] cardNumbers;
	/**
	 * How many cards are currently on the stack. Short as I hope no game ever results in more than 30000 cards on the stack. Otherwise poor fella.
	 */
	public final short stack;

	/**
	 * Default constructor for serialization.
	 * Initializes an invalid update, that will probably cause some kind of error
	 * unless the fields get assigned valid values.
	 *
	 * @deprecated Only to be used during deserialization.
	 */
	@Deprecated
	public Update() {
		turn = false;
		ended = false;
		player = null;
		topCard = null;
		cardNumbers = new short[0];
		stack = -1;
	}

	/**
	 * Creates a new update.
	 *
	 * @param turn    Whether it's this clients turn.
	 * @param player  The player object representing this player.
	 * @param topCard The card on top of the pile.
	 * @param count   How many cards the other players have.
	 */
	public Update(boolean turn, @NotNull Player player, @NotNull Card topCard, short @NotNull [] count, short stack) {
		this.turn = turn;
		this.ended = false;
		this.player = player;
		this.topCard = topCard;
		this.cardNumbers = count;
		this.stack = stack;
	}

	/**
	 * Creates a new update.
	 *
	 * @param turn   Whether it's this clients turn.
	 * @param ended  Whether the game has ended.
	 * @param player The player object representing this player.
	 * @param card   The card on top of the pile.
	 * @param count  How many cards the other players have.
	 */
	public Update(boolean turn, boolean ended, @NotNull Player player, @NotNull Card card, short @NotNull [] count, short stack) {
		this.turn = turn;
		this.ended = ended;
		this.player = player;
		this.topCard = card;
		this.cardNumbers = count;
		this.stack = stack;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o instanceof Update u) {
			assert this.player != null && u.player != null && this.topCard != null && u.topCard != null : "Guess somebody used the deprecated constructor";
			return this.turn == u.turn && this.ended == u.ended && this.player.equals(u.player) && this.topCard.equals(u.topCard) && Arrays.equals(this.cardNumbers, u.cardNumbers) && this.stack == u.stack;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(turn, ended, player, topCard);
		result = 31 * result + Arrays.hashCode(cardNumbers);
		return result;
	}

	@Override
	public void writeExternal(@NotNull ObjectOutput out) throws IOException {
		out.writeBoolean(turn);
		out.writeBoolean(ended);
		out.writeShort(stack);
		out.writeObject(player);
		out.writeObject(topCard);
		out.writeObject(cardNumbers);
	}

	@Override
	public void readExternal(@NotNull ObjectInput in) throws IOException, ClassNotFoundException {
		try {
			TURN_FIELD.setBoolean(this, in.readBoolean());
			END_FIELD.setBoolean(this, in.readBoolean());
			STACK_FIELD.setShort(this, in.readShort());
			PLAYER_FIELD.set(this, in.readObject());
			CARD_FIELD.set(this, in.readObject());
			NUMBERS_FIELD.set(this, in.readObject());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
