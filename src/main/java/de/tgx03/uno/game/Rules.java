package de.tgx03.uno.game;

import java.io.Serializable;
import java.util.Objects;

/**
 * All the rules that can be changed for a game of UNO
 */
public class Rules implements Serializable, Cloneable {

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

	public Rules clone() {
		return new Rules(this.jumping, this.stacking, this.forceContinue);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Rules rules = (Rules) o;
		return jumping == rules.jumping && stacking == rules.stacking && forceContinue == rules.forceContinue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(jumping, stacking, forceContinue);
	}
}
