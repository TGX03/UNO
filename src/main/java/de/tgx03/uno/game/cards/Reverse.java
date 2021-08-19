package de.tgx03.uno.game.cards;

public class Reverse extends Card{

    public final Color color;

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
