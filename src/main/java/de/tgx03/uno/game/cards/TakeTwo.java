package de.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serial;

/**
 * A take two card.
 */
public class TakeTwo extends Card {

	@Serial
	private static final long serialVersionUID = 3572737636745065895L;
	/**
	 * The offset of the color field. Used during deserialization with Unsafe.
	 */
	private static final long COLOR_OFFSET;

	static {
		long color = -1L;
		try {
			color = UNSAFE.objectFieldOffset(TakeTwo.class.getField("color"));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		COLOR_OFFSET = color;
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
	 * @deprecated Only to be used during deserialization.
	 */
	@Deprecated
	public TakeTwo() {
		color = null;
	}

	/**
	 * Creates a new take two card with the provided color.
	 *
	 * @param color The color of the new card.
	 */
	public TakeTwo(@NotNull Color color) {
		if (color == Color.BLACK) throw new IllegalArgumentException();
		this.color = color;
	}

	@Override
	public boolean place(@NotNull Card below) {
		return below.color() == this.color || below instanceof TakeTwo;
	}

	@Override
	public int penalty() {
		return 2;
	}

	@Override
	@NotNull
	public Color color() {
		assert color != null;
		return color;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o != null && o.getClass() == TakeTwo.class) {
			return ((TakeTwo) o).color == this.color;
		}
		return false;
	}

	@Override
	@NotNull
	public String toString() {
		assert color != null;
		return color.name() + " TakeTwo";
	}

	@Override
	@NotNull
	public TakeTwo clone() {
		assert this.color != null;
		return new TakeTwo(this.color);
	}

	@Override
	public int hashCode() {
		int start = 48;
		assert this.color != null;
		start = start + this.color.ordinal();
		return start;
	}

	@Override
	public void writeExternal(@NotNull ObjectOutput out) throws IOException {
		assert this.color != null;
		out.writeByte(this.color.getValue());
	}

	@Override
	public void readExternal(@NotNull ObjectInput in) throws IOException {
		UNSAFE.putObject(this, COLOR_OFFSET, Color.getByValue(in.readByte()));
	}
}
