package de.tgx03.uno.game;

/**
 * All the rules that can be changed for a game of UNO
 */
public class Rules {

	/**
	 * Whether throwing in when a player has exactly the same card as is lying on the pile
	 * is allowed
	 */
	public final boolean jumping;
	/**
	 * Whether stacking penalty cards is allowed
	 */
	public final boolean stacking;
	/**
	 * Whether a player has to pick up cards until he is able to play
	 */
	public final boolean forceContinue;

	/**
	 * @param jumping       Whether throwing in when a player has exactly the same card as is lying on the pile is allowed
	 * @param stacking      Whether stacking penalty cards is allowed
	 * @param forceContinue Whether a player has to pick up cards until he is able to play
	 */
	public Rules(boolean jumping, boolean stacking, boolean forceContinue) {
		this.jumping = jumping;
		this.stacking = stacking;
		this.forceContinue = forceContinue;
	}
}
