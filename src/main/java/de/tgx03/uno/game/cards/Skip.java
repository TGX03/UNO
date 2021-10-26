package de.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	public Skip(@NotNull Color color) {
		this.color = color;
	}

	@Override
	public boolean place(@NotNull Card below) {
		return below.color() == this.color || below instanceof Skip;
	}

	@Override
	public boolean jump(@NotNull Card below) {
		return below.color() == this.color && below instanceof Skip;
	}

	@Override
	public @NotNull Color color() {
		return color;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o instanceof Skip) {
			return ((Skip) o).color == this.color;
		}
		return false;
	}

	@Override @NotNull
	public String toString() {
		return color.name() + " Skip";
	}

	@Override @NotNull
	public Skip clone() {
		return new Skip(this.color);
	}

	@Override
	public int hashCode() {
		return 44 + this.color.ordinal();
	}
}
