package de.tgx03.uno.ui;

/**
 * This class only exists because directly launching from the MainFrame class
 * doesn't work because JavaFX.
 */
public final class Launcher {

	/**
	 * Starts the MainFrame.
	 *
	 * @param args Gets sent to JavaFX.
	 */
	public static void main(String[] args) {
		MainFrame.main(args);
	}
}
