package de.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serial;

/**
 * A standard UNO card.
 */
public class Default extends Card {

	@Serial
	private static final long serialVersionUID = -807259155534165108L;
	/**
	 * The offset of the color field, used during deserialization with Unsafe
	 */
	private static final long COLOR_OFFSET;
	/**
	 * The offset of the value field, used during deserialization with Unsafe
	 */
	private static final long VALUE_OFFSET;

	static {
		long color = -1L;
		long value = -1L;
		try {
			color = UNSAFE.objectFieldOffset(Default.class.getField("color"));
			value = UNSAFE.objectFieldOffset(Default.class.getField("value"));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		COLOR_OFFSET = color;
		VALUE_OFFSET = value;
	}

	/**
	 * The color of this card.
	 */
	public final Color color;
	/**
	 * The number on this card.
	 */
	public final byte value;

	/**
	 * Default constructor for serialization.
	 * Initializes an invalid card, that will probably cause some kind of error
	 * unless the fields get assigned valid values.
	 *
	 * @deprecated Not to be used, only for deserialization.
	 */
	@Deprecated
	public Default() {
		this.color = null;
		this.value = -1;
	}

	/**
	 * Creates a new card.
	 *
	 * @param color The color of the new card.
	 * @param value The number of the new card.
	 */
	public Default(@NotNull Color color, byte value) {
		if (color == Color.BLACK || value < 0 || value >= 10) {
			throw new IllegalArgumentException();
		}
		this.color = color;
		this.value = value;
	}

	@Override
	public boolean place(@NotNull Card below) {
		if (below.color() == this.color) {
			return true;
		} else return below instanceof Default d && d.value == this.value;
	}

	@Override
	@NotNull
	public Color color() {
		assert this.color != null;
		return this.color;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o != null && o.getClass() == Default.class) {
			return ((Default) o).color == this.color && ((Default) o).value == this.value;
		}
		return false;
	}

	@Override
	@NotNull
	public String toString() {
		assert color != null;
		return color.name() + " " + value;
	}

	@Override
	@NotNull
	public Default clone() {
		assert this.color != null;
		return new Default(this.color, this.value);
	}

	@Override
	public int hashCode() {
		int start = 0;
		assert color != null;
		switch (color) {
			case GREEN -> start = 10;
			case RED -> start = 20;
			case YELLOW -> start = 30;
		}
		start = start + this.value;
		return start;
	}

	@Override
	public void writeExternal(@NotNull ObjectOutput out) throws IOException {
		out.writeByte(this.value);
		assert this.color != null;
		out.writeByte(this.color.getValue());
	}

	@Override
	public void readExternal(@NotNull ObjectInput in) throws IOException {
		UNSAFE.putByte(this, VALUE_OFFSET, in.readByte());
		UNSAFE.putObject(this, COLOR_OFFSET, Color.getByValue(in.readByte()));
	}
}
