package de.tgx03.uno.game.cards;

public class ChooseColor extends Card implements ColorChooser{

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
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ChooseColor;
    }

    public String toString() {
        return "Wild";
    }
}
