package de.tgx03.uno.game.cards;

public class Skip extends Card{

    public final Color color;

    public Skip(Color color) {
        this.color = color;
    }

    @Override
    public boolean place(Card below) {
        return below.color() == this.color || below instanceof Skip;
    }

    @Override
    public boolean jump(Card below) {
        return below.color() == this.color && below instanceof Skip;
    }

    @Override
    public Color color() {
        return color;
    }

    public boolean equals(Object o) {
        if (o instanceof Skip) {
            return ((Skip) o).color == this.color;
        }
        return false;
    }

    public String toString() {
        return color.name() + " Skip";
    }
}
