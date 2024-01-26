package eu.tgx03.uno.ui;

import org.jetbrains.annotations.Nullable;

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
	public static void main(@Nullable String[] args) {
		MainFrame.main(args);
	}
}
