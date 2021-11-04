package de.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serial;

/**
 * A take two card
 */
public class TakeTwo extends Card {

	@Serial
	private static final long serialVersionUID = 3572737636745065895L;
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
	 * The color of this card
	 */
	public final Color color;

	/**
	 * Default constructor for serialization.
	 * Initializes an invalid card, that will probably cause some kind of error
	 * unless the fields get assigned valid values.
	 */
	public TakeTwo() {
		color = null;
	}

	/**
	 * Creates a new take two card with the provided color
	 *
	 * @param color The color of the new card
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
	public boolean jump(@NotNull Card below) {
		return below.color() == this.color && below instanceof TakeTwo;
	}

	@Override
	public @NotNull Color color() {
		return color;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o instanceof TakeTwo) {
			return ((TakeTwo) o).color == this.color;
		}
		return false;
	}

	@Override
	@NotNull
	public String toString() {
		return color.name() + " TakeTwo";
	}

	@Override
	@NotNull
	public TakeTwo clone() {
		return new TakeTwo(this.color);
	}

	@Override
	public int hashCode() {
		int start = 48;
		start = start + this.color.ordinal();
		return start;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(this.color.getValue());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		UNSAFE.putObject(this, COLOR_OFFSET, Color.getByValue(in.readByte()));
	}
}
