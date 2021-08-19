package de.tgx03.uno.game;

public class Rules {

    public final boolean jumping;
    public final boolean stacking;
    public final boolean forceContinue;

    public Rules(boolean jumping, boolean stacking, boolean forceContinue) {
        this.jumping = jumping;
        this.stacking = stacking;
        this.forceContinue = forceContinue;
    }
}
