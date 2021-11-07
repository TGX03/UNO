package de.tgx03.uno.messaging;

import de.tgx03.uno.game.cards.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * A class representing a command the client sends to the host
 */
public class Command implements Externalizable {

	@Serial
	private static final long serialVersionUID = -569083254883826234L;
	private static final Unsafe UNSAFE;
	private static final long TYPE_OFFSET;
	private static final long NUMBER_OFFSET;
	private static final long COLOR_OFFSET;

	static {
		Unsafe unsafe = null;
		long type = -1L;
		long number = -1L;
		long color = -1L;
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
			type = unsafe.objectFieldOffset(Command.class.getDeclaredField("type"));
			number = unsafe.objectFieldOffset(Command.class.getDeclaredField("cardNumber"));
			color = unsafe.objectFieldOffset(Command.class.getDeclaredField("color"));
		} catch (NoSuchFieldException | IllegalAccessException exception) {
			exception.printStackTrace();
		}
		UNSAFE = unsafe;
		TYPE_OFFSET = type;
		NUMBER_OFFSET = number;
		COLOR_OFFSET = color;
	}

	/**
	 * The type of command being transmitted
	 */
	public final CommandType type;
	/**
	 * Which card of the player an operation shall be performed on
	 */
	public final int cardNumber;
	/**
	 * In case the color of a black card shall be changed,
	 * this variable contains the wished for color
	 */
	public final Color color;

	/**
	 * Creates a new command that requests a new card for the current player
	 */
	public Command() {
		this.type = CommandType.TAKE_CARD;
		this.cardNumber = -1;
		this.color = null;
	}

	/**
	 * Creates a new command that is used for performing play operations
	 * When wanting to choose the color of a card, another constructor needs to be used
	 *
	 * @param type       The type of command to be created
	 * @param cardNumber Which card shall be used for the operation. When taking a penalty or a new card, this gets ignored
	 */
	public Command(@NotNull CommandType type, int cardNumber) {
		this.type = type;
		this.cardNumber = cardNumber;
		color = null;
	}

	/**
	 * Creates a new command that requests to change the color
	 * of a black card
	 *
	 * @param color      The color to give to the black card
	 * @param cardNumber Which card the color shall be given to
	 */
	public Command(@NotNull Color color, int cardNumber) {
		this.type = CommandType.SELECT_COLOR;
		this.color = color;
		this.cardNumber = cardNumber;
	}

	@Override
	public int hashCode() {
		if (this.type == CommandType.NORMAL || this.type == CommandType.JUMP) {
			return Objects.hash(type, cardNumber);
		} else if (this.type == CommandType.SELECT_COLOR) {
			return Objects.hash(type, cardNumber, color);
		} else {
			return Objects.hash(type);
		}
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o instanceof Command c) {
			if (this.type == CommandType.JUMP || this.type == CommandType.NORMAL) {
				return this.type == c.type && this.cardNumber == c.cardNumber;
			} else if (this.type == CommandType.SELECT_COLOR) {
				return this.type == c.type && this.cardNumber == c.cardNumber && this.color == c.color;
			} else {
				return this.type == c.type;
			}
		} else return false;
	}

	@Override
	public String toString() {
		String result;
		switch (this.type) {
			case NORMAL -> result = "Lay down card number " + cardNumber;
			case JUMP -> result = "Jump with card number " + cardNumber;
			case TAKE_CARD -> result = "Player picks up a card";
			case ACCEPT -> result = "Player accepts the penalty";
			case SELECT_COLOR -> result = "Player changes color of card " + cardNumber + " to " + this.color;
			default -> result = "";
		}
		return result;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(this.type);
		switch (this.type) {
			case NORMAL, JUMP -> out.writeInt(cardNumber);
			case SELECT_COLOR -> {
				out.writeInt(cardNumber);
				assert color != null;
				out.writeByte(color.getValue());
			}
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		CommandType type = (CommandType) in.readObject();
		UNSAFE.putObject(this, TYPE_OFFSET, type);
		switch (type) {
			case NORMAL, JUMP -> UNSAFE.putInt(this, NUMBER_OFFSET, in.readInt());
			case SELECT_COLOR -> {
				UNSAFE.putInt(this, NUMBER_OFFSET, in.readInt());
				UNSAFE.putObject(this, COLOR_OFFSET, Color.getByValue(in.readByte()));
			}
		}
	}

	/**
	 * An enum representing which kind of command is being transmitted
	 */
	public enum CommandType {
		/**
		 * When a card shall be played the normal way
		 */
		NORMAL,
		/**
		 * When a card shall be thrown in
		 * because the exact same card is lying on the pile
		 */
		JUMP,
		/**
		 * When the current player decides to accept a penalty
		 */
		ACCEPT,
		/**
		 * When the color of a black card is to be selected
		 */
		SELECT_COLOR,
		/**
		 * When a player wants to pick up a card
		 */
		TAKE_CARD
	}
}
