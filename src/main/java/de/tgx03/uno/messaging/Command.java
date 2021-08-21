package de.tgx03.uno.messaging;

import de.tgx03.uno.game.cards.Color;

import java.io.Serializable;

public class Command implements Serializable {

    public final CommandType type;
    public final int cardNumber;
    public final Color color;

    public Command(CommandType type, int cardNumber) {
        this.type = type;
        this.cardNumber = cardNumber;
        color = null;
    }

    public Command(Color color, int cardNumber) {
        this.type = CommandType.SELECT_COLOR;
        this.color = color;
        this.cardNumber = cardNumber;
    }

    public enum CommandType {
        NORMAL, JUMP, ACCEPT, SELECT_COLOR
    }
}
