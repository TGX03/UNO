package de.tgx03;

import org.jetbrains.annotations.NotNull;

/**
 * An interface that declares any object as being able
 * to process exceptions that may occur during another objects operations.
 */
public interface ExceptionHandler {

	/**
	 * Provides an object with an exception that occurred during host execution
	 * and allows it to perform fitting actions.
	 *
	 * @param exception The exception that occurred.
	 */
	void handleException(@NotNull Exception exception);
}
