package de.tgx03.uno.game;

import de.tgx03.uno.game.cards.*;

/**
 * A class representing a game of Uno
 * It stores the players, the current card on top
 * And performs all the checks for legal moves
 * and executes them
 */
public class Game {

	private final Player[] players;
	private final Rules rules;
	private Card top = Card.generateCard();
	private int currentPlayer = 0;
	private boolean reversed = false;
	private int stack = 0;

	/**
	 * Creates a new game of UNO
	 *
	 * @param playerCount The number of players in this game
	 * @param rules       The selected rules this game should be played with
	 */
	public Game(int playerCount, Rules rules) {

		// Store the rules
		this.rules = rules;

		// Initialize the players with their cards
		players = new Player[playerCount];
		for (int i = 0; i < playerCount; i++) {
			players[i] = new Player();
			players[i].initialize();
			players[i].updateTop(top);
		}

		// If the card on top is a card that chooses colors, get a new one cause fuck it, I'm not implementing that
		while (top instanceof ColorChooser) {
			top = Card.generateCard();
		}

		// Test for the various penalties that will apply to the first player
		if (top instanceof Reverse) reversed = !reversed;
		else if (top instanceof Skip) nextPlayer();
		else if (top instanceof TakeTwo) stack = stack + 2;
	}

	/**
	 * Tries to play a card and return whether it was successful
	 * It automatically does that for the current player
	 *
	 * @param cardNumber The number of the number in the current players stack
	 * @return Whether the current player did play this card
	 */
	public synchronized boolean playCard(int cardNumber) {
		if (stack > 0) return mustStack(cardNumber);
		else return normalPlay(cardNumber);
	}

	/**
	 * Tries to throw in a card of the selected player even though it's not his turn
	 * Returns whether the move was legal and executed
	 *
	 * @param player     The player wishing to jump
	 * @param cardNumber Which player wishes to jump
	 * @return Whether the move was executed
	 */
	public synchronized boolean jump(int player, int cardNumber) {

		// Directly return if jumping is forbidden
		if (!rules.jumping) return false;

		// Try to play the card
		Card played = players[player].jumpCard(cardNumber);
		if (played == null) return false;
		currentPlayer = player;
		top = played;

		// Apply the various penalties
		if (played instanceof Reverse) {
			reversed = !reversed;
		} else if (played instanceof Skip) {
			nextPlayer();
		} else if (played instanceof TakeTwo) {
			stack = stack + 2;
		} else if (played instanceof TakeFour) {
			stack = stack + 4;
		}

		// Update
		updateTop();
		nextPlayer();
		return true;
	}

	/**
	 * The current player accepts his card penalty and picks up the cards
	 *
	 * @return Whether any card were picked up
	 */
	public synchronized boolean acceptCards() {

		// Directly return if there currently is no penalty
		if (stack == 0) return false;

		// Pick up the cards
		for (int i = 0; i < stack; i++) {
			players[currentPlayer].drawCard();
		}

		// Update
		nextPlayer();
		stack = 0;
		return true;
	}

	/**
	 * The current player picks up a card
	 */
	public synchronized void takeCard() {
		players[currentPlayer].drawCard();
		if (!rules.forceContinue) {
			nextPlayer();
		}
	}

	/**
	 * Get the number of the player whose turn its right now
	 *
	 * @return The number of the current player
	 */
	public int getCurrentPlayer() {
		return this.currentPlayer;
	}

	/**
	 * Get the player with the given number
	 *
	 * @param id The number of the requested player
	 * @return The corresponding player
	 */
	public Player getPlayer(int id) {
		return players[id];
	}

	/**
	 * Get how many cards each player has
	 *
	 * @return The card count of each player
	 */
	public synchronized short[] getCardCount() {
		short[] count = new short[players.length];
		for (int i = 0; i < players.length; i++) {
			count[i] = (short) players[i].cardCount();
		}
		return count;
	}

	/**
	 * @return The card currently on top
	 */
	public Card getTopCard() {
		return top;
	}

	/**
	 * Plays a card the normal way without any penalties in the game
	 *
	 * @param cardNumber The number of the card to play
	 * @return Whether the card was played
	 */
	private synchronized boolean normalPlay(int cardNumber) {

		// Play the cards
		Card played = players[currentPlayer].playCard(cardNumber);
		if (played == null) return false;
		// Check whether a black card got played by accident and return it if so
		if (played instanceof ColorChooser) {
			if (played.color() == Color.BLACK) {
				players[currentPlayer].giveCard(cardNumber, played);
				return false;
			}
		}
		top = played;

		// Apply penalties
		if (played instanceof Reverse) {
			reversed = !reversed;
		} else if (played instanceof Skip) {
			nextPlayer();
		} else if (played instanceof TakeTwo) {
			stack = stack + 2;
		} else if (played instanceof TakeFour) {
			stack = stack + 4;
		}

		// Update
		updateTop();
		nextPlayer();
		return true;
	}

	/**
	 * Plays a card when take cards have been played
	 *
	 * @param cardNumber The card of the current player to be played
	 * @return Whether it was successful
	 */
	private synchronized boolean mustStack(int cardNumber) {

		// Already return false if stacking is forbidden
		if (!rules.stacking) {
			return false;
		}

		// Try to play card and check whether it's the correct type of card to stack
		Card played = players[currentPlayer].playCard(cardNumber);
		if (played == null) return false;
		if ((top instanceof TakeTwo && played instanceof TakeTwo)) {
			stack = stack + 2;
			top = played;
			nextPlayer();
			updateTop();
			return true;
		} else if (top instanceof TakeFour && played instanceof TakeFour) {
			stack = stack + 4;
			top = played;
			nextPlayer();
			updateTop();
			return true;
		} else {
			players[currentPlayer].giveCard(cardNumber, played);
			return false;
		}
	}

	/**
	 * Go to the next player
	 */
	private synchronized void nextPlayer() {
		if (reversed) {
			currentPlayer--;
			if (currentPlayer < 0) currentPlayer = players.length - 1;
		} else {
			currentPlayer++;
			if (currentPlayer >= players.length) currentPlayer = 0;
		}

		// In case the current player has won, go to the next one
		// Will probably get a stackoverflow if everyone has finished
		if (getPlayer(getCurrentPlayer()).won()) {
			nextPlayer();
		}
	}

	/**
	 * Give the new top card to all the players
	 */
	private synchronized void updateTop() {
		for (Player player : players) {
			player.updateTop(top);
		}
	}
}
