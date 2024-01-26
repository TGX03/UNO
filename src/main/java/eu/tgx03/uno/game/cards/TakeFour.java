package eu.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;

/**
 * A class representing the wild take four card.
 */
public class TakeFour extends ChooseColor {

	@Serial
	private static final long serialVersionUID = 1773201801357025228L;

	@Override
	public int penalty() {
		return 4;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o != null && o.getClass() == TakeFour.class) {
			return ((TakeFour) o).color() == this.color();
		} else return false;
	}

	@Override
	@NotNull
	public String toString() {
		return "Wild Take Four";
	}

	@Override
	@NotNull
	public TakeFour clone() {
		return (TakeFour) super.clone();
	}

	@Override
	public int hashCode() {
		return 57 + this.color().ordinal();
	}
}
