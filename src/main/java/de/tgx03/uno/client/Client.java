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

    private final Socket socket;
    private final ObjectInputStream input;
    private final ObjectOutputStream output;
    private final List<ClientUpdate> receivers = new ArrayList<>(1);

    private Player player;
    private Card topCard;
    private boolean exit = false;

    public Client(String hostIP, int hostPort) throws IOException {
        socket = new Socket(hostIP, hostPort);
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());
        new Thread(this).start();
    }

    public synchronized boolean play(int cardNumber) throws IOException {
        Command command = new Command(Command.CommandType.NORMAL, cardNumber);
        output.writeObject(command);
        return input.readBoolean();
    }

    public synchronized boolean jump(int cardNumber) throws IOException {
        Command command = new Command(Command.CommandType.JUMP, cardNumber);
        output.writeObject(command);
        return input.readBoolean();
    }

    public synchronized boolean acceptCards() throws IOException {
        Command command = new Command(Command.CommandType.ACCEPT, -1);
        output.writeObject(command);
        return input.readBoolean();
    }

    public synchronized boolean takeCard() throws IOException {
        Command command = new Command();
        output.writeObject(command);
        return input.readBoolean();
    }

    public synchronized boolean selectColor(int cardNumber, Color color) throws IOException {
        Command command = new Command(color, cardNumber);
        output.writeObject(command);
        return input.readBoolean();
    }

    public Player getPlayer() {
        return player;
    }

    public synchronized boolean started() {
        return this.player != null;
    }

    public void exit() {
        exit = true;
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
        while(!exit) {
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
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
