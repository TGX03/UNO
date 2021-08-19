package de.tgx03.uno.messaging;

import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.cards.Card;

import java.io.Serializable;

public class Update implements Serializable {

    public final Player player;
    public final Card topCard;
    public final int[] cardNumbers;

    public Update(Player player, Card card, int[] count) {
        this.player = player;
        this.topCard = card;
        this.cardNumbers = count;
    }
}
