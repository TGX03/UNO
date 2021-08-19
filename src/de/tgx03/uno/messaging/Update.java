package de.tgx03.uno.messaging;

import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.cards.Card;

import java.io.Serializable;

public class Update implements Serializable {

    public final boolean turn;
    public final Player player;
    public final Card topCard;
    public final short[] cardNumbers;

    public Update(boolean turn, Player player, Card card, short[] count) {
        this.turn = turn;
        this.player = player;
        this.topCard = card;
        this.cardNumbers = count;
    }
}
