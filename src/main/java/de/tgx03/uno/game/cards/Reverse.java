package de.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;

/**
 * The card inverting the playing order
 */
public class Reverse extends Card {

	@Serial
	private static final long serialVersionUID = 467179758413513210L;

	/**
	 * The color of this card
	 */
	public final Color color;

	/**
	 * Creates a new reverse card with the provided color
	 *
	 * @param color The color of the new card
	 */
	public Reverse(@NotNull Color color) {
		this.color = color;
	}

	@Override
	public boolean place(@NotNull Card below) {
		return below.color() == this.color || below instanceof Reverse;
	}

	@Override
	public boolean jump(@NotNull Card below) {
		return below.color() == this.color && below instanceof Reverse;
	}

	@Override
	public @NotNull Color color() {
		return color;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o instanceof Reverse) {
			return ((Reverse) o).color == this.color;
		}
		return false;
	}

	@Override @NotNull
	public String toString() {
		return color.name() + " Reverse";
	}

	@Override @NotNull
	public Reverse clone() {
		return new Reverse(this.color);
	}

	@Override
	public int hashCode() {
		return 40 + this.color.ordinal();
	}
}
