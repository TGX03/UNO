package de.tgx03.uno.game.cards;

/**
 * A class representing the skip card
 */
public class Skip extends Card {

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

	public boolean equals(Object o) {
		if (o instanceof Skip) {
			return ((Skip) o).color == this.color;
		}
		return false;
	}

	public String toString() {
		return color.name() + " Skip";
	}
}
