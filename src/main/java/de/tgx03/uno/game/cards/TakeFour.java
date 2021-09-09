package de.tgx03.uno.game.cards;

import java.io.Serial;

/**
 * A class representing the wild take four card
 */
public class TakeFour extends ColorChooser {

	@Serial
	private static final long serialVersionUID = 1773201801357025228L;

	private Color color = Color.BLACK;

	@Override
	public boolean place(Card below) {
		return true;
	}

	@Override
	public boolean jump(Card below) {
		return false;
	}

	@Override
	public Color color() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TakeFour) {
			return ((TakeFour) o).color == this.color;
		} else return false;
	}

	@Override
	public String toString() {
		return "Wild Take Four";
	}

	@Override
	public TakeFour clone() {
		TakeFour result = new TakeFour();
		result.setColor(this.color);
		return result;
	}

	@Override
	public int hashCode() {
		return 57 + this.color.ordinal();
	}
}
