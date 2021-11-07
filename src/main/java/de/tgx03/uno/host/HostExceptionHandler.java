package de.tgx03.uno.host;

import org.jetbrains.annotations.NotNull;

public interface HostExceptionHandler {

	void handleException(@NotNull Exception exception);
}
