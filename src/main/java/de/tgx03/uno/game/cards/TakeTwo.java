package de.tgx03.uno.game.cards;

/**
 * A take two card
 */
public class TakeTwo extends Card {

	/**
	 * The color of this card
	 */
	public final Color color;

	/**
	 * Creates a new take two card with the provided color
	 *
	 * @param color The color of the new card
	 */
	public TakeTwo(Color color) {
		if (color == Color.BLACK) throw new IllegalArgumentException();
		this.color = color;
	}

	@Override
	public boolean place(Card below) {
		return below.color() == this.color || below instanceof TakeTwo;
	}

	@Override
	public boolean jump(Card below) {
		return below.color() == this.color && below instanceof TakeTwo;
	}

	@Override
	public Color color() {
		return color;
	}

	public boolean equals(Object o) {
		if (o instanceof TakeTwo) {
			return ((TakeTwo) o).color == this.color;
		}
		return false;
	}

	public String toString() {
		return color.name() + " TakeTwo";
	}
}
