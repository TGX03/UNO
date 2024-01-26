package eu.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.Serial;
import java.util.Random;

/**
 * The basic implementation of a game card.
 */
public abstract class Card implements Externalizable, Cloneable {
	@Serial
	private static final long serialVersionUID = 3828684409287282936L;
	/**
	 * The number generator used for generating new cards.
	 */
	private static final Random rand = new Random();

	/**
	 * Generates a new card in accordance with a normal uno deck.
	 * However, as they are newly generated, it's possible for cards
	 * to exist more often than in a real game.
	 *
	 * @return A new card.
	 */
	@NotNull
	public static Card generateCard() {
		int code = rand.nextInt(108);
		if (code < 19) {
			return new Default(Color.BLUE, (byte) (code % 10));
		} else if (code < 38) {
			return new Default(Color.GREEN, (byte) (code % 10));
		} else if (code < 57) {
			return new Default(Color.RED, (byte) (code % 10));
		} else if (code < 76) {
			return new Default(Color.YELLOW, (byte) (code % 10));
		} else if (code < 84) {
			return new TakeTwo(chooseSpecialColor(code % 8));
		} else if (code < 92) {
			return new Reverse(chooseSpecialColor(code % 8));
		} else if (code < 100) {
			return new Skip(chooseSpecialColor(code % 8));
		} else if (code < 104) {
			return new ChooseColor();
		} else {
			return new TakeFour();
		}
	}

	/**
	 * Transforms a number to a color.
	 *
	 * @param code The number to convert, must be lower than 8.
	 * @return The corresponding color.
	 */
	@NotNull
	private static Color chooseSpecialColor(int code) {
		code = code / 2;
		switch (code) {
			case 0 -> {
				return Color.BLUE;
			}
			case 1 -> {
				return Color.GREEN;
			}
			case 2 -> {
				return Color.RED;
			}
			case 3 -> {
				return Color.YELLOW;
			}
			default -> throw new IllegalArgumentException("Number must be between 0 and 3");
		}
	}

	/**
	 * Determines whether a card can be normally placed on top of another card.
	 *
	 * @param below The card this card shall be put on top of.
	 * @return Whether it's legal.
	 */
	public abstract boolean place(@NotNull Card below);

	/**
	 * Determines whether a card can be thrown in when it's not the player turn.
	 *
	 * @param below The card this card shall be thrown on top of.
	 * @return Whether it's legal.
	 */
	public boolean jump(@NotNull Card below) {
		return this.equals(below);
	}

	/**
	 * Whether this card reverses the current order of the game.
	 * In the base game gets only used by the Reverse card.
	 *
	 * @return Whether this card changed direction.
	 */
	public boolean changesDirection() {
		return false;
	}

	/**
	 * Whether this card results in the next player being skipped.
	 * In the base game gets only used by the Skip card.
	 *
	 * @return Whether to skip the next player.
	 */
	public boolean skipNextPlayer() {
		return false;
	}

	/**
	 * Whether this card requires any penalty cards to be picked up by the next player
	 * and if so how many.
	 *
	 * @return How many cards the next player needs to pick up.
	 */
	public int penalty() {
		return 0;
	}

	/**
	 * Returns the color of this card.
	 *
	 * @return The color of this card.
	 */
	@NotNull
	public abstract Color color();

	@Override
	@NotNull
	public abstract Card clone();
}
