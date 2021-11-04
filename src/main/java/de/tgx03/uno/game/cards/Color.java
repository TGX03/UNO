package de.tgx03.uno.game.cards;

/**
 * All the possible colors for a card
 */
public enum Color {
	RED, YELLOW, GREEN, BLUE,
	/**
	 * This color actually shouldn't be used in game, it's just a placeholder before the player
	 * chose the color of a wild or take 4 card
	 */
	BLACK;

	public static Color getByValue(byte value) {
		switch (value) {
			case 0 -> {
				return BLACK;
			}
			case 1 -> {
				return RED;
			}
			case 2 -> {
				return YELLOW;
			}
			case 3 -> {
				return GREEN;
			}
			case 4 -> {
				return BLUE;
			}
			default -> throw new IllegalArgumentException();
		}
	}

	public byte getValue() {
		switch (this) {
			case BLACK -> {
				return 0;
			}
			case RED -> {
				return 1;
			}
			case YELLOW -> {
				return 2;
			}
			case GREEN -> {
				return 3;
			}
			case BLUE -> {
				return 4;
			}
			default -> throw new IllegalArgumentException();
		}
	}
}
