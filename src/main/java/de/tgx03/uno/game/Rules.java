package de.tgx03.uno.game;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * All the rules that can be changed for a game of UNO.
 */
public class Rules implements Externalizable, Cloneable {

	@Serial
	private static final long serialVersionUID = 5037643636101348351L;

	/**
	 * The reflective field of the Jumping boolean.
	 * Used for deserialization.
	 */
	private static final Field JUMP_FIELD;
	/**
	 * The reflective field of the Stacking boolean.
	 * Used for deserialization.
	 */
	private static final Field STACK_FIELD;
	/**
	 * The reflective field of the ForceContinue boolean.
	 * Used for deserialization.
	 */
	private static final Field FORCE_FIELD;

	static {
		try {
			JUMP_FIELD = Rules.class.getDeclaredField("jumping");
			STACK_FIELD = Rules.class.getDeclaredField("stacking");
			FORCE_FIELD = Rules.class.getDeclaredField("forceContinue");
			JUMP_FIELD.setAccessible(true);
			STACK_FIELD.setAccessible(true);
			FORCE_FIELD.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new ExceptionInInitializerError("Couldn't get fields for deserialization.");
		}
	}

	/**
	 * Whether throwing in when a player has exactly the same card as is lying on the pile
	 * is allowed.
	 */
	public final boolean jumping;
	/**
	 * Whether stacking penalty cards is allowed.
	 */
	public final boolean stacking;
	/**
	 * Whether a player has to pick up cards until he is able to play.
	 */
	public final boolean forceContinue;

	/**
	 * Creates new Rules with all special rules disabled.
	 */
	public Rules() {
		jumping = false;
		stacking = false;
		forceContinue = false;
	}

	/**
	 * @param jumping       Whether throwing in when a player has exactly the same card as is lying on the pile is allowed.
	 * @param stacking      Whether stacking penalty cards is allowed.
	 * @param forceContinue Whether a player has to pick up cards until he is able to play.
	 */
	public Rules(boolean jumping, boolean stacking, boolean forceContinue) {
		this.jumping = jumping;
		this.stacking = stacking;
		this.forceContinue = forceContinue;
	}

	@Override
	@NotNull
	public Rules clone() {
		return new Rules(this.jumping, this.stacking, this.forceContinue);
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Rules rules = (Rules) o;
		return jumping == rules.jumping && stacking == rules.stacking && forceContinue == rules.forceContinue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(jumping, stacking, forceContinue);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(jumping);
		out.writeBoolean(stacking);
		out.writeBoolean(forceContinue);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException {
		try {
			JUMP_FIELD.setBoolean(this, in.readBoolean());
			STACK_FIELD.setBoolean(this, in.readBoolean());
			FORCE_FIELD.setBoolean(this, in.readBoolean());
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Can't access fields.");
		}
	}
}
