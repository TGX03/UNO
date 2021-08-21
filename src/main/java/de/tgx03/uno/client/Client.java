package de.tgx03.uno.client;

import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.cards.Card;
import de.tgx03.uno.game.cards.Color;
import de.tgx03.uno.messaging.Command;
import de.tgx03.uno.messaging.Update;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client implements Runnable {

    private final ObjectInputStream input;
    private final ObjectOutputStream output;
    private final List<ClientUpdate> receivers = new ArrayList<>(1);

    private Player player;
    private Card topCard;

    public Client(String hostIP, int hostPort) throws IOException {
        Socket socket = new Socket(hostIP, hostPort);
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());
        new Thread(this).start();
    }

    public synchronized void play(int cardNumber) throws IOException {
        output.reset();
        Command command = new Command(Command.CommandType.NORMAL, cardNumber);
        output.writeObject(command);
    }

    public synchronized void jump(int cardNumber) throws IOException {
        output.reset();
        Command command = new Command(Command.CommandType.JUMP, cardNumber);
        output.writeObject(command);
    }

    public synchronized void acceptCards() throws IOException {
        output.reset();
        Command command = new Command(Command.CommandType.ACCEPT, -1);
        output.writeObject(command);
    }

    public synchronized void takeCard() throws IOException {
        output.reset();
        Command command = new Command();
        output.writeObject(command);
    }

    public synchronized void selectColor(int cardNumber, Color color) throws IOException {
        output.reset();
        Command command = new Command(color, cardNumber);
        output.writeObject(command);
    }

    public Player getPlayer() {
        return player;
    }

    public void registerReceiver(ClientUpdate receiver) {
        this.receivers.add(receiver);
    }

    public String toString() {
        Card[] cards = player.getCards();
        StringBuilder builder = new StringBuilder();
        for (Card card : cards) {
            builder.append(card).append("; ");
        }
        builder.append(System.lineSeparator()).append("Top Card:").append(topCard);
        return builder.toString();
    }

    @Override
    public void run() {
        while(true) {
            try {
                Update update = (Update) input.readObject();
                synchronized (Client.this) {
                    player = update.player;
                    topCard = update.topCard;
                }
                for (ClientUpdate receiver : receivers) {
                    receiver.update(update);
                }
            } catch (IOException | ClassCastException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
