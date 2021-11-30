package de.tgx03.uno.game;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

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
	 * The Unsafe used for deserialization of final fields.
	 */
	private static final Unsafe UNSAFE;
	/**
	 * The offset of the jump field. Used for deserialization with Unsafe.
	 */
	private static final long JUMP_OFFSET;
	/**
	 * The offset of the stack field. Used for deserialization with Unsafe.
	 */
	private static final long STACK_OFFSET;
	/**
	 * The offset of the force field. Used for deserialization with Unsafe.
	 */
	private static final long FORCE_OFFSET;

	static {
		Unsafe unsafe = null;
		long jump = -1L;
		long stack = -1L;
		long force = -1L;
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
			jump = unsafe.objectFieldOffset(Rules.class.getDeclaredField("jumping"));
			stack = unsafe.objectFieldOffset(Rules.class.getDeclaredField("stacking"));
			force = unsafe.objectFieldOffset(Rules.class.getDeclaredField("forceContinue"));

		} catch (NoSuchFieldException | IllegalAccessException exception) {
			exception.printStackTrace();
		}
		UNSAFE = unsafe;
		JUMP_OFFSET = jump;
		STACK_OFFSET = stack;
		FORCE_OFFSET = force;
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
		UNSAFE.putBoolean(this, JUMP_OFFSET, in.readBoolean());
		UNSAFE.putBoolean(this, STACK_OFFSET, in.readBoolean());
		UNSAFE.putBoolean(this, FORCE_OFFSET, in.readBoolean());
	}
}
