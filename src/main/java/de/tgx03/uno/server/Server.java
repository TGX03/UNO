package de.tgx03.uno.server;

import de.tgx03.ExceptionHandler;
import de.tgx03.uno.game.Game;
import de.tgx03.uno.game.Rules;
import de.tgx03.uno.game.cards.ChooseColor;
import de.tgx03.uno.messaging.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class representing the server of a game of UNO.
 */
public abstract class Server implements Runnable {

	/**
	 * The lock used to wait for the start of the game.
	 * Required as virtual threads don't play nice with synchronized blocks.
	 */
	private final Lock startLock = new ReentrantLock(false);
	/**
	 * The Condition used to wait for the start of the game.
	 */
	private final Condition waiter = startLock.newCondition();
	/**
	 * The rules of the game to use once the game gets started.
	 */
	private final Rules rules;
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
	 * Creates a new server that listens on the provided port
	 * for clients.
	 *
	 * @param rules The rules of the game.
	 * @throws IOException When something goes wrong while starting the server.
	 */
	public Server(@Nullable Rules rules) throws IOException {
		this.rules = rules;
	}

	/**
	 * Starts the round.
	 */
	public synchronized void start() {
		start = true;
		notifyAll();
	}

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

	@Override
	public void run() {
		waitForClients();

		// Set up the game and inform the clients of it
		startLock.lock();
		game = new Game(getPlayerCount(), rules);
		waiter.signalAll();
		startLock.unlock();
		update();
	}

	/**
	 * How many players are currently registered to play the game.
	 *
	 * @return How many players are registered.
	 */
	public abstract int getPlayerCount();

	/**
	 * Accepts new clients and sets up the connections with them.
	 */
	protected abstract void waitForClients();

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
	 * Informs all the clients that the game has ended
	 * and shuts down the threads.
	 */
	protected abstract void end();

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

	protected void awaitStart() {
		while (!start || Server.this.game == null) {
			startLock.lock();
			waiter.awaitUninterruptibly();
			startLock.unlock();
		}
	}

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