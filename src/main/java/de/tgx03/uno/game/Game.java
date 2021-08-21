package de.tgx03.uno.game;

import de.tgx03.uno.game.cards.*;

public class Game {

    private final Player[] players;
    private final Rules rules;
    private Card top = Card.generateCard();
    private int currentPlayer = 0;
    private boolean reversed = false;
    private int stack = 0;

    public Game(int playerCount, Rules rules) {
        this.rules = rules;
        players = new Player[playerCount];
        for (int i = 0; i < playerCount; i++) {
            players[i] = new Player();
            players[i].initialize();
            players[i].updateTop(top);
        }
        while (top instanceof ColorChooser) {
            top = Card.generateCard();
        }
        if (top instanceof Reverse) reversed = !reversed;
        else if (top instanceof Skip) nextPlayer();
        else if (top instanceof TakeTwo) stack = stack + 2;
    }

    public synchronized boolean playCard(int cardNumber) {
        if (stack > 0) return mustStack(cardNumber);
        else return normalPlay(cardNumber);
    }

    public synchronized boolean jump(int player, int cardNumber) {
        if (!rules.jumping) return false;
        Card played = players[player].jumpCard(cardNumber);
        if (played == null) return false;
        currentPlayer = player;
        top = played;
        if (played instanceof Reverse) {
            reversed = !reversed;
        } else if (played instanceof Skip) {
            nextPlayer();
        } else if (played instanceof TakeTwo) {
            stack = stack + 2;
        } else if (played instanceof TakeFour) {
            stack = stack + 4;
        }
        updateTop();
        nextPlayer();
        return true;
    }

    public synchronized boolean acceptCards() {
        if (stack == 0) return false;
        for (int i = 0; i < stack; i++) {
            players[currentPlayer].drawCard();
        }
        nextPlayer();
        stack = 0;
        return true;
    }

    public synchronized void takeCard() {
        players[currentPlayer].drawCard();
        if (!rules.forceContinue) {
            nextPlayer();
        }
    }

    public int getCurrentPlayer() {
        return this.currentPlayer;
    }

    public Player getPlayer(int id) {
        return players[id];
    }

    public synchronized short[] getCardCount() {
        short[] count = new short[players.length];
        for (int i = 0; i < players.length; i++) {
            count[i] = (short) players[i].cardCount();
        }
        return count;
    }

    public Card getTopCard() {
        return top;
    }

    private synchronized boolean normalPlay(int cardNumber) {
        while (players[currentPlayer].won()) {
            nextPlayer();
        }
        Card played = players[currentPlayer].playCard(cardNumber);
        if (played == null) return false;
        top = played;
        if (played instanceof ColorChooser) {
            if (played.color() == Color.BLACK) {
                return false;
            }
        }
        if (played instanceof Reverse) {
            reversed = !reversed;
        } else if (played instanceof Skip) {
            nextPlayer();
        } else if (played instanceof TakeTwo) {
            stack = stack + 2;
        } else if (played instanceof TakeFour) {
            stack = stack + 4;
        }
        updateTop();
        nextPlayer();
        return true;
    }

    private synchronized boolean mustStack(int cardNumber) {
        if (!rules.stacking) {
            return false;
        }
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
            players[currentPlayer].giveCard(played);
            return false;
        }
    }

    private synchronized void nextPlayer() {
        if (reversed) {
            currentPlayer--;
            if (currentPlayer < 0) currentPlayer = players.length - 1;
        } else {
            currentPlayer++;
            if (currentPlayer >= players.length) currentPlayer = 0;
        }
    }

    private synchronized void updateTop() {
        for (Player player : players) {
            player.updateTop(top);
        }
    }
}
