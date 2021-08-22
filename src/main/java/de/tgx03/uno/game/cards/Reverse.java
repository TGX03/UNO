package de.tgx03.uno.game.cards;

/**
 * The card inverting the playing order
 */
public class Reverse extends Card {

    /**
     * The color of this card
     */
    public final Color color;

    /**
     * Creates a new reverse card with the provided color
     *
     * @param color The color of the new card
     */
    public Reverse(Color color) {
        this.color = color;
    }

    @Override
    public boolean place(Card below) {
        return below.color() == this.color || below instanceof Reverse;
    }

    @Override
    public boolean jump(Card below) {
        return below.color() == this.color && below instanceof Reverse;
    }

    @Override
    public Color color() {
        return color;
    }

    public boolean equals(Object o) {
        if (o instanceof Reverse) {
            return ((Reverse) o).color == this.color;
        }
        return false;
    }

    public String toString() {
        return color.name() + " Reverse";
    }
}
