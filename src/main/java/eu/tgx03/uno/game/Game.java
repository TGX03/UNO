package eu.tgx03.uno.game;

import eu.tgx03.uno.game.cards.Card;
import eu.tgx03.uno.game.cards.ChooseColor;
import eu.tgx03.uno.game.cards.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class representing a game of Uno.
 * It stores the players, the current card on top
 * and performs all the checks for legal moves
 * and executes them.
 */
public class Game {

	/**
	 * How many cards each player gets at the start of a round.
	 */
	private static final int INITIAL_CARDS = 7;

	/**
	 * This lock can be used to synchronize on a game externally, as synchronized blocks are avoided for virtual threads.
	 */
	public final Lock gameLock = new ReentrantLock(true);

	/**
	 * ALl the players in this game.
	 */
	private final Player[] players;
	/**
	 * The rules that apply to this game
	 */
	private final Rules rules;
	/**
	 * The card currently on top of the pile.
	 */
	private Card top;
	/**
	 * The number of the player who's currently playing.
	 */
	private int currentPlayer = 0;
	/**
	 * Which direction the game is currently going in.
	 */
	private boolean reversed = false;
	/**
	 * How many cards are currently to be picked up once a player accepts a penalty.
	 */
	private int stack = 0;

	/**
	 * Creates a new game of UNO.
	 * If null rules are supplied, everything is disabled.
	 *
	 * @param playerCount The number of players in this game.
	 * @param rules       The selected rules this game should be played with.
	 */
	public Game(int playerCount, @Nullable Rules rules) {

		// Store the rules
		if (rules != null) this.rules = rules;
		else this.rules = new Rules();

		top = Card.generateCard();
		if (top.color() == Color.BLACK) {   // If the top card is a color chooser, randomly select a color.
			byte colorValue = (byte) (new Random().nextInt(4) + 1);
			((ChooseColor) this.top).setColor(Color.getByValue(colorValue));
		}

		// Initialize the players with their cards
		players = new Player[playerCount];
		for (int i = 0; i < playerCount; i++) {
			players[i] = new Player(INITIAL_CARDS);
			players[i].updateTop(top);
		}

		// Test for the various penalties that will apply to the first player
		applyPenalties();
	}

	/**
	 * Tries to play a card and return whether it was successful.
	 * It automatically does that for the current player.
	 *
	 * @param cardNumber The number of the number in the current players stack.
	 * @return Whether the current player did play this card.
	 */
	public boolean playCard(int playerNumber, int cardNumber) {
		gameLock.lock();
		try {
			if (currentPlayer != playerNumber) return false;
			if (stack > 0) return mustStack(cardNumber);
			else return normalPlay(cardNumber);
		} finally {
			gameLock.unlock();
		}
	}

	/**
	 * Tries to throw in a card of the selected player even though it's not his turn.
	 * Returns whether the move was legal and executed.
	 *
	 * @param player     The player wishing to jump.
	 * @param cardNumber Which card the player wants to jump with.
	 * @return Whether the move was executed.
	 */
	public boolean jump(int player, int cardNumber) {
		gameLock.lock();
		try {
			// Directly return if jumping is forbidden
			if (!rules.jumping) return false;

			// Try to play the card
			Card played = players[player].jumpCard(cardNumber);
			if (played == null) return false;
			currentPlayer = player;
			top = played;

			// Update
			updateTop();
			applyPenalties();
			nextPlayer();
			return true;
		} finally {
			gameLock.unlock();
		}
	}

	/**
	 * The current player accepts his card penalty and picks up the cards.
	 *
	 * @return Whether a card was picked up.
	 */
	public boolean acceptCards(int playerNumber) {

		gameLock.lock();

		try {
			// Directly return if there currently is no penalty
			if (stack == 0 || currentPlayer != playerNumber) return false;

			// Pick up the cards
			for (int i = 0; i < stack; i++) {
				players[currentPlayer].drawCard();
			}

			// Update
			nextPlayer();
			stack = 0;
			return true;
		} finally {
			gameLock.unlock();
		}
	}

	/**
	 * The current player picks up a card.
	 */
	public boolean takeCard(int playerNumber) {
		gameLock.lock();
		try {
			if (playerNumber != currentPlayer) return false;
			players[currentPlayer].drawCard();
			if (!rules.forceContinue) {
				nextPlayer();
			}
			return true;
		} finally {
			gameLock.unlock();
		}
	}

	/**
	 * How many cards are currently on the penalty stack.
	 * @return How many cards are currently on the penalty stack.
	 */
	public int getStackSize() {
		return this.stack;
	}

	/**
	 * Get the number of the player whose turn its right now.
	 *
	 * @return The number of the current player.
	 */
	public int getCurrentPlayer() {
		return this.currentPlayer;
	}

	/**
	 * Get the player with the given number.
	 *
	 * @param id The number of the requested player.
	 * @return The corresponding player object.
	 */
	@NotNull
	public Player getPlayer(int id) {
		return players[id];
	}

	/**
	 * Get how many cards each player has.
	 *
	 * @return The card count of each player.
	 */
	public short @NotNull [] getCardCount() {
		short[] count = new short[players.length];
		gameLock.lock();
		for (int i = 0; i < players.length; i++) {
			count[i] = (short) players[i].cardCount();
		}
		gameLock.unlock();
		return count;
	}

	/**
	 * @return The card currently on top.
	 */
	@NotNull
	public Card getTopCard() {
		return top;
	}

	/**
	 * Determines whether this game has finished by checking if any player still has some cards left.
	 *
	 * @return If the game has finished.
	 */
	public boolean hasEnded() {
		for (Player player : players) {
			if (!player.finished()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the amount of players in this game.
	 *
	 * @return How many players are in this game.
	 */
	public int playerCount() {
		return players.length;
	}

	/**
	 * Plays a card the normal way without any penalties in the game.
	 *
	 * @param cardNumber The number of the card to play.
	 * @return Whether the card was played.
	 */
	private boolean normalPlay(int cardNumber) {
		gameLock.lock();
		try {
			// Play the cards
			Card played = players[currentPlayer].playCard(cardNumber);
			if (played == null) return false;
			// Check whether a black card got played by accident and return it if so
			if (played.color() == Color.BLACK) {
				players[currentPlayer].giveCard(cardNumber, played);
				return false;
			}
			top = played;

			// Update
			updateTop();
			applyPenalties();
			nextPlayer();
			return true;
		} finally {
			gameLock.unlock();
		}
	}

	/**
	 * Plays a card when take cards have been played.
	 *
	 * @param cardNumber The card of the current player to be played.
	 * @return Whether it was successful.
	 */
	private boolean mustStack(int cardNumber) {
		gameLock.lock();
		try {
			// Already return false if stacking is forbidden
			if (!rules.stacking) {
				return false;
			}

			// Try to play card and check whether it's the correct type of card to stack
			Card played = players[currentPlayer].playCard(cardNumber);
			if (played == null) return false;
			if (played.penalty() != 0 && top.penalty() == played.penalty()) {
				top = played;
				updateTop();
				applyPenalties();
				nextPlayer();
				return true;
			} else {
				players[currentPlayer].giveCard(cardNumber, played);
				return false;
			}
		} finally {
			gameLock.unlock();
		}
	}

	/**
	 * Go to the next player.
	 */
	private void nextPlayer() {
		gameLock.lock();
		try {
			if (!this.hasEnded()) {
				if (reversed) {
					do {
						currentPlayer--;
						if (currentPlayer < 0) currentPlayer = players.length - 1;
					} while (players[currentPlayer].finished());
				} else {
					do {
						currentPlayer++;
						if (currentPlayer >= players.length) currentPlayer = 0;
					} while (players[currentPlayer].finished());
				}
			}
		} finally {
			gameLock.unlock();
		}
	}

	/**
	 * Apply the penalties in accordance with the card currently lying on top.
	 */
	private synchronized void applyPenalties() {
		gameLock.lock();
		stack = stack + top.penalty();
		reversed = top.changesDirection() != reversed;  // This was a simplification provided by IntelliJ, hope it works
		if (top.skipNextPlayer()) nextPlayer();
		gameLock.unlock();
	}

	/**
	 * Give the new top card to all the players.
	 */
	private void updateTop() {
		gameLock.lock();
		for (Player player : players) {
			player.updateTop(top);
		}
		gameLock.unlock();
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o instanceof Game g && g.players.length == this.players.length) {
			for (int i = 0; i < this.players.length; i++) {
				if (this.players[i] != g.players[i]) return false;
			}
			return this.rules.equals(g.rules);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(rules);
		result = 31 * result + Arrays.hashCode(players);
		return result;
	}
}
