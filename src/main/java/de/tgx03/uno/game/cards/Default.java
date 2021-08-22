package de.tgx03.uno.game.cards;

/**
 * A standard UNO card
 */
public class Default extends Card {

    public final Color color;
    public final byte value;

    /**
     * Creates a new card
     *
     * @param color The color of the new card
     * @param value The number of the new card
     */
    public Default(Color color, byte value) {
        if (color == Color.BLACK || value < 0 || value >= 10) {
            throw new IllegalArgumentException();
        }
        this.color = color;
        this.value = value;
    }

    @Override
    public boolean place(Card below) {
        if (below.color() == this.color) {
            return true;
        } else return below instanceof Default && ((Default) below).value == this.value;
    }

    @Override
    public boolean jump(Card below) {
        return this.equals(below);
    }

    @Override
    public Color color() {
        return this.color;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Default) {
            return ((Default) o).color == this.color && ((Default) o).value == this.value;
        }
        return false;
    }

    public String toString() {
        return color.name() + " " + value;
    }
}
