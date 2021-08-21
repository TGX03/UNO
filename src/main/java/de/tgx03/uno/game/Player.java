package de.tgx03.uno.game;

import de.tgx03.uno.game.cards.Card;
import de.tgx03.uno.game.cards.TakeFour;
import de.tgx03.uno.game.cards.TakeTwo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {

    private final List<Card> cards = new ArrayList<>(7);
    private transient Card top;

    public void initialize() {
        cards.clear();
        for (int i = 0; i < 7; i++) {
            cards.add(Card.generateCard());
        }
    }

    public Card playCard(int cardNumber) {
        if (cardNumber < cards.size() && cards.get(cardNumber).place(top)) {
            return cards.remove(cardNumber);
        } else {
            return null;
        }
    }

    public Card jumpCard(int cardNumber) {
        if (cardNumber < cards.size() && cards.get(cardNumber).jump(top)) {
            return cards.remove(cardNumber);
        } else {
            return null;
        }
    }

    public void drawCard() {
        cards.add(Card.generateCard());
    }

    public void updateTop(Card card) {
        this.top = card;
    }

    public boolean canStack2() {
        for (Card card : cards) {
            if (card instanceof TakeTwo) return true;
        }
        return false;
    }

    public boolean canStack4() {
        for (Card card : cards) {
            if (card instanceof TakeFour) return true;
        }
        return false;
    }

    public int cardCount() {
        return cards.size();
    }

    public boolean won() {
        return cards.size() == 0;
    }

    public Card[] getCards() {
        return cards.toArray(new Card[0]);
    }

    protected void giveCard(Card card) {
        cards.add(card);
    }
}
