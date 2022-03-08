package de.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serial;

/**
 * A class representing the wild card.
 */
public class ChooseColor extends Card {

	@Serial
	private static final long serialVersionUID = -2223262444434498162L;

	/**
	 * The color this card currently represents.
	 * Needs to be changed before the card is actually played.
	 */
	protected Color color = Color.BLACK;

	@Override
	public boolean place(@Nullable Card below) {
		return true;
	}

	@Override
	public boolean jump(@Nullable Card below) {
		return false;
	}

	@Override
	@NotNull
	public Color color() {
		return color;
	}

	public void setColor(@NotNull Color color) {
		this.color = color;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o != null && o.getClass() == ChooseColor.class) {
			return this.color == ((ChooseColor) o).color;
		} else return false;
	}

	@Override
	@NotNull
	public String toString() {
		return "Wild";
	}

	@Override
	@NotNull
	public ChooseColor clone() {
		ChooseColor result = new ChooseColor();
		result.setColor(this.color);
		return result;
	}

	@Override
	public int hashCode() {
		return 52 + color.ordinal();
	}

	@Override
	public void writeExternal(@NotNull ObjectOutput out) throws IOException {
		out.writeByte(color.getValue());
	}

	@Override
	public void readExternal(@NotNull ObjectInput in) throws IOException {
		this.color = Color.getByValue(in.readByte());
	}
}
