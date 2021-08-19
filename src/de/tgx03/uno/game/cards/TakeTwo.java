package de.tgx03.uno.game.cards;

public class TakeTwo extends Card {

    public final Color color;

    public TakeTwo(Color color) {
        if (color == Color.BLACK) throw new IllegalArgumentException();
        this.color = color;
    }

    @Override
    public boolean place(Card below) {
        return below.color() == this.color || below instanceof TakeTwo;
    }

    @Override
    public boolean jump(Card below) {
        return below.color() == this.color && below instanceof TakeTwo;
    }

    @Override
    public Color color() {
        return color;
    }

    public boolean equals(Object o) {
        if (o instanceof TakeTwo) {
            return ((TakeTwo) o).color == this.color;
        }
        return false;
    }

    public String toString() {
        return color.name() + " TakeTwo";
    }
}
