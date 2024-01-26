package eu.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serial;
import java.lang.reflect.Field;

/**
 * The card inverting the playing order.
 */
public class Reverse extends Card {

	@Serial
	private static final long serialVersionUID = 467179758413513210L;

	/**
	 * The reflective field of the Color of this card.
	 * Used for deserialization.
	 */
	private static final Field COLOR_FIELD;

	static {
		try {
			COLOR_FIELD = Reverse.class.getDeclaredField("color");
			COLOR_FIELD.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	/**
	 * The color of this card.
	 */
	public final Color color;

	/**
	 * Default constructor for serialization.
	 * Initializes an invalid card, that will probably cause some kind of error
	 * unless the fields get assigned valid values.
	 *
	 * @deprecated Only to be used by deserialization.
	 */
	@Deprecated
	public Reverse() {
		this.color = null;
	}

	/**
	 * Creates a new reverse card with the provided color.
	 *
	 * @param color The color of the new card.
	 */
	public Reverse(@NotNull Color color) {
		if (color == Color.BLACK) throw new IllegalArgumentException();
		this.color = color;
	}

	@Override
	public boolean place(@NotNull Card below) {
		return below.color() == this.color || below instanceof Reverse;
	}

	@Override
	public boolean changesDirection() {
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
		if (o != null && o.getClass() == Reverse.class) {
			return ((Reverse) o).color == this.color;
		}
		return false;
	}

	@Override
	@NotNull
	public String toString() {
		assert color != null;
		return color.name() + " Reverse";
	}

	@Override
	@NotNull
	public Reverse clone() {
		assert this.color != null;
		return new Reverse(this.color);
	}

	@Override
	public int hashCode() {
		assert this.color != null;
		return 40 + this.color.ordinal();
	}

	@Override
	public void writeExternal(@NotNull ObjectOutput out) throws IOException {
		assert color != null;
		out.writeByte(color.getValue());
	}

	@Override
	public void readExternal(@NotNull ObjectInput in) throws IOException {
		//colorField.setAccessible(true);
		try {
			COLOR_FIELD.set(this, Color.getByValue(in.readByte()));
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
