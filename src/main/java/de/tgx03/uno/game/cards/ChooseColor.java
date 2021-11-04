package de.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serial;

/**
 * A class representing the wild card
 */
public class ChooseColor extends ColorChooser {

	@Serial
	private static final long serialVersionUID = -2223262444434498162L;

	private Color color = Color.BLACK;

	@Override
	public boolean place(@NotNull Card below) {
		return true;
	}

	@Override
	public boolean jump(@NotNull Card below) {
		return false;
	}

	@Override
	public @NotNull Color color() {
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
	public @NotNull ChooseColor clone() {
		ChooseColor result = new ChooseColor();
		result.setColor(this.color);
		return result;
	}

	@Override
	public int hashCode() {
		return 52 + color.ordinal();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(color.getValue());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.color = Color.getByValue(in.readByte());
	}
}
