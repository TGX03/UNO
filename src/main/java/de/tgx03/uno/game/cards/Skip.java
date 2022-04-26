package de.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serial;
import java.lang.reflect.Field;

/**
 * A class representing the skip card.
 */
public class Skip extends Card {

	@Serial
	private static final long serialVersionUID = 8521656320247047647L;

	/**
	 * The reflective field of the Color of this card.
	 * Used for deserialization.
	 */
	private static final Field COLOR_FIELD;

	static {
		try {
			COLOR_FIELD = Skip.class.getDeclaredField("color");
		} catch (NoSuchFieldException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	/**
	 * The color of this skip card.
	 */
	public final Color color;

	/**
	 * Default constructor for serialization.
	 * Initializes an invalid card, that will probably cause some kind of error
	 * unless the fields get assigned valid values.
	 *
	 * @deprecated Only to be used during deserialization.
	 */
	@Deprecated
	public Skip() {
		color = null;
	}

	/**
	 * Creates a new skip card with the provided color.
	 *
	 * @param color The color of the new card.
	 */
	public Skip(@NotNull Color color) {
		if (color == Color.BLACK) throw new IllegalArgumentException();
		this.color = color;
	}

	@Override
	public boolean place(@NotNull Card below) {
		return below.color() == this.color || below instanceof Skip;
	}

	@Override
	public boolean skipNextPlayer() {
		return true;
	}

	@Override
	@NotNull
	public Color color() {
		assert color != null;
		return color;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o != null && o.getClass() == Skip.class) {
			return ((Skip) o).color == this.color;
		}
		return false;
	}

	@Override
	@NotNull
	public String toString() {
		assert color != null;
		return color.name() + " Skip";
	}

	@Override
	@NotNull
	public Skip clone() {
		assert this.color != null;
		return new Skip(this.color);
	}

	@Override
	public int hashCode() {
		assert this.color != null;
		return 44 + this.color.ordinal();
	}

	@Override
	public void writeExternal(@NotNull ObjectOutput out) throws IOException {
		assert this.color != null;
		out.writeByte(this.color.getValue());
	}

	@Override
	public void readExternal(@NotNull ObjectInput in) throws IOException {
		try {
			COLOR_FIELD.set(this, Color.getByValue(in.readByte()));
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
