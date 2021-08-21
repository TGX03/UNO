package de.tgx03.uno.host;

import de.tgx03.uno.game.Game;
import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.Rules;
import de.tgx03.uno.game.cards.Card;
import de.tgx03.uno.game.cards.ColorChooser;
import de.tgx03.uno.messaging.Command;
import de.tgx03.uno.messaging.Update;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Host implements Runnable {

    private final ServerSocket serverSocket;
    private final Rules rules;
    private final List<Handler> handler = new ArrayList<>();

    private boolean start = false;
    private Game game;

    public Host(int port, Rules rules) throws IOException {
        serverSocket = new ServerSocket(port);
        this.rules = rules;
        new Thread(this).start();
    }

    public synchronized void start() {
        start = true;
        notifyAll();
    }

    @Override
    public void run() {
        new Thread(this::waitForClients).start();
        synchronized (this) {
            while (!start) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        game = new Game(handler.size(), rules);
        try {
            update();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void waitForClients() {
        int currentID = 0;
        while (!start) {
            try {
                Socket socket = serverSocket.accept();
                if (!start) {
                    Handler handler = new Handler(socket, currentID);
                    currentID++;
                    new Thread(handler).start();
                    this.handler.add(handler);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() throws IOException {
        synchronized (game) {
            short[] cardCount = game.getCardCount();
            for (Handler handler : this.handler) {
                handler.update(cardCount);
            }
        }
    }

    private class Handler implements Runnable {

        private final int id;
        private final ObjectInputStream input;
        private final ObjectOutputStream output;

        public Handler(Socket socket, int id) throws IOException {
            this.id = id;
            this.input = new ObjectInputStream(socket.getInputStream());
            this.output = new ObjectOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {

            // Wait until the game starts
            synchronized (Host.this) {
                while (!start) {
                    try {
                        Host.this.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
            }

            while (true) {
                // Read orders and process them
                try {
                    Command order = (Command) input.readObject();
                    boolean success = false;
                    synchronized (game) {
                        switch (order.type) {
                            case NORMAL -> {
                                if (game.getCurrentPlayer() == this.id) {
                                    success = game.playCard(order.cardNumber);
                                }
                            }
                            case JUMP -> success = game.jump(this.id, order.cardNumber);
                            case ACCEPT -> {
                                if (game.getCurrentPlayer() == this.id) {
                                    success = game.acceptCards();
                                }
                            }
                            case SELECT_COLOR -> success = selectColor(order);
                            case TAKE_CARD -> {
                                if (game.getCurrentPlayer() == this.id) {
                                    game.takeCard();
                                    success = true;
                                }
                            }
                        }
                    }
                    if (success) {
                        Host.this.update();
                    }
                } catch (ClassCastException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        public void update(short[] cardCount) throws IOException {
            boolean turn = game.getCurrentPlayer() == this.id;
            Update update = new Update(turn, game.getPlayer(this.id), game.getTopCard(), cardCount);
            output.reset();
            output.writeObject(update);
        }

        private boolean selectColor(Command order) {
            Player player = game.getPlayer(this.id);
            Card card = player.getCards()[order.cardNumber];
            if (card instanceof ColorChooser) {
                ((ColorChooser) card).setColor(order.color);
                return true;
            } else {
                return false;
            }
        }
    }
}
