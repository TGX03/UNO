package de.tgx03.uno.game.cards;

import java.io.Serial;

/**
 * A standard UNO card
 */
public class Default extends Card {

	@Serial
	private static final long serialVersionUID = -807259155534165108L;

	/**
	 * The color of this card
	 */
	public final Color color;
	/**
	 * The number on this card
	 */
	public final byte value;

	/**
	 * Creates a new card
	 *
	 * @param color The color of the new card
	 * @param value The number of the new card
	 */
	public Default(Color color, byte value) {
		if (color == Color.BLACK || value < 0 || value >= 10) {
			throw new IllegalArgumentException();
		}
		this.color = color;
		this.value = value;
	}

	@Override
	public boolean place(Card below) {
		if (below.color() == this.color) {
			return true;
		} else return below instanceof Default && ((Default) below).value == this.value;
	}

	@Override
	public boolean jump(Card below) {
		return this.equals(below);
	}

	@Override
	public Color color() {
		return this.color;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Default) {
			return ((Default) o).color == this.color && ((Default) o).value == this.value;
		}
		return false;
	}

	@Override
	public String toString() {
		return color.name() + " " + value;
	}

	@Override
	public Default clone() {
		return new Default(this.color, this.value);
	}

	@Override
	public int hashCode() {
		int start = 0;
		switch (color) {
			case GREEN -> start = 10;
			case RED -> start = 20;
			case YELLOW -> start = 30;
		}
		start = start + this.value;
		return start;
	}
}
