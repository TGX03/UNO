package de.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;

/**
 * A class representing the wild take four card
 */
public class TakeFour extends ColorChooser {

	@Serial
	private static final long serialVersionUID = 1773201801357025228L;

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
	public void setColor(@NotNull Color color) {
		this.color = color;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o instanceof TakeFour) {
			return ((TakeFour) o).color == this.color;
		} else return false;
	}

	@Override @NotNull
	public String toString() {
		return "Wild Take Four";
	}

	@Override @NotNull
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
