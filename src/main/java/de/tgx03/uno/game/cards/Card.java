package de.tgx03.uno.game.cards;

import java.io.Serializable;
import java.util.Random;

/**
 * The basic implementation of a game card
 */
public abstract class Card implements Serializable, Cloneable {

	private static final Random rand = new Random();

	/**
	 * Generates a new card in accordance with a normal uno deck
	 * However as they are newly generated it's possible for cards
	 * to exist more often than in a real game
	 *
	 * @return A new card
	 */
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
	 * Transforms a number to a color
	 *
	 * @param code The number to convert, must be lower than 8
	 * @return The corresponding color
	 */
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
	 * Determines whether a card can be normally placed on top of another card
	 *
	 * @param below The card this card shall be put on top of
	 * @return Whether it's legal
	 */
	public abstract boolean place(Card below);

	/**
	 * Determines whether a card can be thrown in when it's not the player turn
	 *
	 * @param below The card this card shall be thrown on top of
	 * @return Whether it's legal
	 */
	public abstract boolean jump(Card below);

	/**
	 * Returns the color of this card
	 *
	 * @return The color of this card
	 */
	public abstract Color color();

	@Override
	public abstract Card clone();
}
