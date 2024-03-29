package eu.tgx03.uno.server;

import eu.tgx03.ExceptionHandler;
import eu.tgx03.uno.game.Game;
import eu.tgx03.uno.game.cards.ChooseColor;
import eu.tgx03.uno.messaging.Command;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing the server of a game of UNO.
 */
public abstract class Server {

	/**
	 * The handlers for exceptions that may occur during operation.
	 */
	private final List<ExceptionHandler> exceptionHandlers = new ArrayList<>(1);

	/**
	 * Whether the game shall be started.
	 */
	protected volatile boolean start = false;
	/**
	 * Whether the game shall be stopped.
	 */
	protected volatile boolean kill = false;
	/**
	 * The game instance this host deals with.
	 */
	protected Game game;

	/**
	 * Registers a new object that wishes to handle exceptions that may occur during this hosts execution.
	 *
	 * @param handler The object to be registered as exception handler.
	 */
	public void registerExceptionHandler(@NotNull ExceptionHandler handler) {
		synchronized (exceptionHandlers) {
			exceptionHandlers.add(handler);
		}
	}

	/**
	 * Removes an exception handler that no longer wishes to be informed of exceptions.
	 *
	 * @param handler The handler to remove.
	 */
	public void removeExceptionHandler(@NotNull ExceptionHandler handler) {
		synchronized (exceptionHandlers) {  // TODO: When removing an exception handler while handling an exception a concurrent modification exception occurs.
			exceptionHandlers.remove(handler);
		}
	}

	/**
	 * Starts the round.
	 */
	public abstract void start();

	/**
	 * How many players are currently registered to play the game.
	 *
	 * @return How many players are registered.
	 */
	public abstract int getPlayerCount();

	/**
	 * Informs all the clients of an update to the game.
	 */
	protected abstract void update();

	/**
	 * Ends this host, if required by force.
	 */
	public void kill() {
		kill = true;
	}

	/**
	 * Gives an exception that occurred to all the registered handlers.
	 *
	 * @param e The exception to forward.
	 */
	protected synchronized void handleException(@NotNull Exception e) {
		synchronized (this.exceptionHandlers) {
			this.exceptionHandlers.parallelStream().forEach(x -> x.handleException(e));
		}
	}

	/**
	 * Execute a received command.
	 *
	 * @param player  The player this belongs to.
	 * @param command The received command to execute.
	 */
	protected final void executeCommand(int player, Command command) {
		boolean result = switch (command.type) {
			case NORMAL -> game.playCard(player, command.cardNumber);
			case JUMP -> game.jump(player, command.cardNumber);
			case ACCEPT -> game.acceptCards(player);
			case TAKE_CARD -> game.takeCard(player);
			case SELECT_COLOR -> {
				if (game.getPlayer(player).getCards()[command.cardNumber] instanceof ChooseColor cc) {
					assert command.color != null;
					cc.setColor(command.color);
					yield true;
				} else yield false;
			}
		};
		if (result) this.update();
	}
}