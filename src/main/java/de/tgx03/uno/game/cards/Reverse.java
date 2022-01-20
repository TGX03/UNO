package de.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serial;

/**
 * The card inverting the playing order.
 */
public class Reverse extends Card {

	@Serial
	private static final long serialVersionUID = 467179758413513210L;
	/**
	 * The offset of the color field. Used for deserialization with Unsafe.
	 */
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
	public boolean jump(@Nullable Card below) {
		return this.equals(below);
	}

	@Override
	public boolean changesDirection() {
		return true;
	}

	@Override
	public boolean skipNextPlayer() {
		return false;
	}

	@Override
	public int penalty() {
		return 0;
	}

	@Override
	public @NotNull Color color() {
		assert color != null;
		return color;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o instanceof Reverse r) {
			return r.color == this.color;
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
		UNSAFE.putObject(this, COLOR_OFFSET, Color.getByValue(in.readByte()));
	}
}
