package de.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serial;

/**
 * The card inverting the playing order
 */
public class Reverse extends Card {

	@Serial
	private static final long serialVersionUID = 467179758413513210L;
	private static final long COLOR_OFFSET;

	static {
		long offset = -1L;
		try {
			offset = UNSAFE.objectFieldOffset(Reverse.class.getField("color"));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		COLOR_OFFSET = offset;
	}

	/**
	 * The color of this card
	 */
	public final Color color;

	/**
	 * Default constructor for serialization.
	 * Initializes an invalid card, that will probably cause some kind of error
	 * unless the fields get assigned valid values.
	 *
	 * @deprecated Only to be used by deserialization
	 */
	@Deprecated
	public Reverse() {
		this.color = null;
	}

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
		assert color != null;
		return color;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o instanceof Reverse) {
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
	public void writeExternal(ObjectOutput out) throws IOException {
		assert color != null;
		out.writeByte(color.getValue());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException {
		UNSAFE.putObject(this, COLOR_OFFSET, Color.getByValue(in.readByte()));
	}
}
