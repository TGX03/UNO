package de.tgx03.uno.game.cards;

public class TakeFour extends Card{

    private Color color = Color.BLACK;

    @Override
    public boolean place(Card below) {
        return true;
    }

    @Override
    public boolean jump(Card below) {
        return false;
    }

    @Override
    public Color color() {
        return color;
    }

    public void setColor(Color color) {
        if (color == Color.BLACK) {
            throw new IllegalArgumentException("Needs to be set to a valid color after playing");
        } else {
            this.color = color;
        }
    }

    public boolean equals(Object o) {
        return o instanceof TakeFour;
    }

    public String toString() {
        return "Wild Take Four";
    }
}
