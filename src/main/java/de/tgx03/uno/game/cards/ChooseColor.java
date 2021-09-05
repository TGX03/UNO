package de.tgx03.uno.game.cards;

import java.io.Serial;

/**
 * A class representing the wild card
 */
public class ChooseColor extends Card implements ColorChooser {

	@Serial
	private static final long serialVersionUID = -2223262444434498162L;

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
		if (o instanceof ChooseColor) {
			return this.color == ((ChooseColor) o).color;
		} else return false;
	}

	@Override
	public String toString() {
		return "Wild";
	}

	@Override
	public ChooseColor clone() {
		ChooseColor result = new ChooseColor();
		result.setColor(this.color);
		return result;
	}

	@Override
	public int hashCode() {
		return 52 + color.ordinal();
	}
}
