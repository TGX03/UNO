package de.tgx03.uno.game.cards;

import java.io.Serializable;
import java.util.Random;

public abstract class Card implements Serializable {

    private static final Random rand = new Random();

    public static Card generateCard() {
        int code = rand.nextInt(108);
        if (code < 19) {
            return new Default(Color.BLUE, (byte) code);
        } else if (code < 38) {
            return new Default(Color.GREEN, (byte) (code % 10));
        } else if (code < 57) {
            return new Default(Color.RED, (byte) (code % 10));
        } else if (code < 76) {
            return new Default(Color.YELLOW, (byte) (code % 10));
        } else if (code < 84) {
            return new TakeTwo(chooseSpecialColor(code % 8));
        } else if (code < 92) {
            return new Reverse(chooseSpecialColor(code % 8));
        } else if (code < 100) {
            return new Skip(chooseSpecialColor(code % 8));
        } else if (code < 104) {
            return new ChooseColor();
        } else {
            return new TakeFour();
        }
    }

    private static Color chooseSpecialColor(int code) {
        code = code / 2;
        switch (code) {
            case 0 -> {
                return Color.BLUE;
            }
            case 1 -> {
                return Color.GREEN;
            }
            case 2 -> {
                return Color.RED;
            }
            case 3 -> {
                return Color.YELLOW;
            }
            default -> {throw new IllegalArgumentException("Number must be between 0 and 3");}
        }
    }

    public abstract boolean place(Card below);

    public abstract boolean jump(Card below);

    public abstract Color color();
}
