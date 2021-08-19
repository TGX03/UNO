package de.tgx03.uno.messaging;

import java.io.Serializable;

public class Command implements Serializable {

    public final CommandType type;
    public final int cardNumber;

    public Command(CommandType type, int cardNumber) {
        this.type = type;
        this.cardNumber = cardNumber;
    }

    public enum CommandType {
        NORMAL, JUMP, ACCEPT
    }
}
