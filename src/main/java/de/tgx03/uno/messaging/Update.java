package de.tgx03.uno.messaging;

import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.cards.Card;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

/**
 * A class providing clients with new data after the state of the game has changed
 */
public class Update implements Externalizable {

	@Serial
	private static final long serialVersionUID = 1959833843176392241L;
	private static final Unsafe UNSAFE;
	private static final long TURN_OFFSET;
	private static final long END_OFFSET;
	private static final long PLAYER_OFFSET;
	private static final long CARD_OFFSET;
	private static final long COUNT_OFFSET;

	static {
		Unsafe unsafe = null;
		long turn = -1L;
		long end = -1L;
		long player = -1L;
		long card = -1L;
		long count = -1L;
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
			turn = unsafe.objectFieldOffset(Update.class.getDeclaredField("turn"));
			end = unsafe.objectFieldOffset(Update.class.getDeclaredField("ended"));
			player = unsafe.objectFieldOffset(Update.class.getDeclaredField("player"));
			card = unsafe.objectFieldOffset(Update.class.getDeclaredField("topCard"));
			count = unsafe.objectFieldOffset(Update.class.getDeclaredField("cardNumbers"));
		} catch (NoSuchFieldException | IllegalAccessException exception) {
			exception.printStackTrace();
		}
		UNSAFE = unsafe;
		TURN_OFFSET = turn;
		END_OFFSET = end;
		PLAYER_OFFSET = player;
		CARD_OFFSET = card;
		COUNT_OFFSET = count;
	}

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
	 * How many cards all the other players have.
	 * Done as short to save space, and I don't think any player will have more than 60000 cards
	 */
	public final short[] cardNumbers;

	/**
	 * Default constructor for serialization.
	 * Initializes an invalid update, that will probably cause some kind of error
	 * unless the fields get assigned valid values.
	 * @deprecated Only to be used during deserialization
	 */
	@Deprecated
	public Update() {
		turn = false;
		ended = false;
		player = null;
		topCard = null;
		cardNumbers = new short[0];
	}

	/**
	 * Creates a new update
	 *
	 * @param turn    Whether it's this clients turn
	 * @param player  The player object representing this player
	 * @param topCard The card on top of the pile
	 * @param count   How many cards the other players have
	 */
	public Update(boolean turn, @NotNull Player player, @NotNull Card topCard, @NotNull short[] count) {
		this.turn = turn;
		this.ended = false;
		this.player = player;
		this.topCard = topCard;
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
	public Update(boolean turn, boolean ended, @NotNull Player player, @NotNull Card card, @NotNull short[] count) {
		this.turn = turn;
		this.ended = ended;
		this.player = player;
		this.topCard = card;
		this.cardNumbers = count;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Update update = (Update) o;
		if (turn != update.turn || ended != update.ended) return false;
		assert player != null;
		if (!player.equals(update.player)) return false;
		assert topCard != null;
		return topCard.equals(update.topCard) && Arrays.equals(cardNumbers, update.cardNumbers);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(turn, ended, player, topCard);
		result = 31 * result + Arrays.hashCode(cardNumbers);
		return result;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(turn);
		out.writeBoolean(ended);
		out.writeObject(player);
		out.writeObject(topCard);
		out.writeObject(cardNumbers);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		UNSAFE.putBoolean(this, TURN_OFFSET, in.readBoolean());
		UNSAFE.putBoolean(this, END_OFFSET, in.readBoolean());
		UNSAFE.putObject(this, PLAYER_OFFSET, in.readObject());
		UNSAFE.putObject(this, CARD_OFFSET, in.readObject());
		UNSAFE.putObject(this, COUNT_OFFSET, in.readObject());
	}
}
