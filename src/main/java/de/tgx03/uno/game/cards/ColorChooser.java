package de.tgx03.uno.game.cards;

/**
 * An interface allowing unified access to the colors of Wild and Take 4 cards
 */
public interface ColorChooser {

    /**
     * Sets the color of this card to the provided color
     *
     * @param color The chosen color
     */
    void setColor(Color color);
}
