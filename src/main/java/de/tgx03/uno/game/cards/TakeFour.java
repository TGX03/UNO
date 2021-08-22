package de.tgx03.uno.game.cards;

/**
 * A class representing the wild take four card
 */
public class TakeFour extends Card implements ColorChooser {

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

	public void setColor(Color color) {
		this.color = color;
	}

	public boolean equals(Object o) {
		return o instanceof TakeFour;
	}

	public String toString() {
		return "Wild Take Four";
	}

	public TakeFour clone() {
		TakeFour result = new TakeFour();
		result.setColor(this.color);
		return result;
	}

	public int hashCode() {
		return 57 + this.color.ordinal();
	}
}
