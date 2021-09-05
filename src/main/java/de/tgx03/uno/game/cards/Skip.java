package de.tgx03.uno.game.cards;

import java.io.Serial;

/**
 * A class representing the skip card
 */
public class Skip extends Card {

	@Serial
	private static final long serialVersionUID = 8521656320247047647L;

	/**
	 * The color of this skip card
	 */
	public final Color color;

	/**
	 * Creates a new skip card with the provided color
	 *
	 * @param color The color of the new card
	 */
	public Skip(Color color) {
		this.color = color;
	}

	@Override
	public boolean place(Card below) {
		return below.color() == this.color || below instanceof Skip;
	}

	@Override
	public boolean jump(Card below) {
		return below.color() == this.color && below instanceof Skip;
	}

	@Override
	public Color color() {
		return color;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Skip) {
			return ((Skip) o).color == this.color;
		}
		return false;
	}

	@Override
	public String toString() {
		return color.name() + " Skip";
	}

	@Override
	public Skip clone() {
		return new Skip(this.color);
	}

	@Override
	public int hashCode() {
		return 44 + this.color.ordinal();
	}
}
