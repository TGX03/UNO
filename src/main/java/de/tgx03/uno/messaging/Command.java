package de.tgx03.uno.messaging;

import de.tgx03.uno.game.cards.Color;

import java.io.Serializable;
import java.util.Objects;

/**
 * A class representing a command the client sends to the host
 */
public class Command implements Serializable {

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
	public Command(CommandType type, int cardNumber) {
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
	public Command(Color color, int cardNumber) {
		this.type = CommandType.SELECT_COLOR;
		this.color = color;
		this.cardNumber = cardNumber;
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
	public boolean equals(Object o) {
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
}
