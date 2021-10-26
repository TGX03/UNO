package de.tgx03.uno.game.cards;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;

/**
 * An interface allowing unified access to the colors of Wild and Take 4 cards
 */
public abstract class ColorChooser extends Card {

	@Serial
	private static final long serialVersionUID = -9052963457767015206L;

	/**
	 * Sets the color of this card to the provided color
	 *
	 * @param color The chosen color
	 */
	public abstract void setColor(@NotNull Color color);
}
