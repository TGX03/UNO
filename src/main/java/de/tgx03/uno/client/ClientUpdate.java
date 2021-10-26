package de.tgx03.uno.client;

import de.tgx03.uno.messaging.Update;
import org.jetbrains.annotations.NotNull;

/**
 * An interface getting used to update data when the client receives an update from the host
 */
public interface ClientUpdate {

	/**
	 * Provides the implementing class with updates of the game
	 *
	 * @param update The update sent by the server
	 */
	void update(@NotNull Update update);
}
