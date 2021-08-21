package de.tgx03.uno.ui;

import de.tgx03.uno.client.Client;
import de.tgx03.uno.client.ClientUpdate;
import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.Rules;
import de.tgx03.uno.game.cards.Card;
import de.tgx03.uno.game.cards.Color;
import de.tgx03.uno.game.cards.ColorChooser;
import de.tgx03.uno.host.Host;
import de.tgx03.uno.messaging.Update;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class MainFrame extends Application implements ClientUpdate, ChangeListener<Number> {

    private static final ObservableList<String> normalColors = FXCollections.observableArrayList("Blue", "Green", "Red", "Yellow");
    private static final ObservableList<String> choosableColors = FXCollections.observableArrayList("Blue", "Green", "Red", "Yellow", "Black");

    @FXML
    private ListView<ImageView> cardList;
    @FXML
    private MenuItem createHost;
    @FXML
    private MenuItem startGame;
    @FXML
    private MenuItem joinGame;
    @FXML
    private ImageView topCard;
    @FXML
    private ComboBox<String> colorPicker;
    @FXML
    private ListView<String> playerList;
    @FXML
    private Button play;
    @FXML
    private Button accept;
    @FXML
    private Button take;
    @FXML
    private Button setColor;

    private Host host;
    private Client client;

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/MainFrame.fxml"));
        stage.setTitle("UNO");
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.show();
    }

    public synchronized void createHost(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Host");
        dialog.setHeaderText("Choose Port");
        dialog.setContentText("Port:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int port = Integer.parseInt(result.get());
                Rules rules = createRules();
                try {
                    host = new Host(port, rules);
                    createHost.setDisable(true);
                    startGame.setDisable(false);
                    joinGame.setDisable(true);
                    client = new Client("localhost", port);
                    client.registerReceiver(this);
                } catch (IOException e) {
                    ExceptionDialog.showException(e);
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid port");
                alert.setContentText("Invalid number entered");
                alert.showAndWait();
            }
        }
    }

    public synchronized void startHost(ActionEvent e) {
        host.start();
        cardList.getSelectionModel().selectedIndexProperty().addListener(this);
    }

    public synchronized void createClient(ActionEvent e) {
        try {
            ConnectionDialog dialog = new ConnectionDialog();
            client = dialog.createClient();
            client.registerReceiver(this);
            cardList.getSelectionModel().selectedIndexProperty().addListener(this);
        } catch (Exception ex) {
            ExceptionDialog.showException(ex);
        }
    }

    public synchronized void playCard(ActionEvent e) {
        int selected = cardList.getSelectionModel().getSelectedIndex();
        try {
            client.play(selected);
        } catch (IOException ex) {
            ExceptionDialog.showException(ex);
        }
    }

    public synchronized void jumpCard(ActionEvent e) {
        int selected = cardList.getSelectionModel().getSelectedIndex();
        try {
            client.jump(selected);
        } catch (IOException ex) {
            ExceptionDialog.showException(ex);
        }
    }

    public synchronized void acceptCards(ActionEvent e) {
        try {
            client.acceptCards();
        } catch (IOException ex) {
            ExceptionDialog.showException(ex);
        }
    }

    public synchronized void takeCard(ActionEvent e) {
        try {
            client.takeCard();
        } catch (IOException ex) {
            ExceptionDialog.showException(ex);
        }
    }

    public synchronized void selectColor(ActionEvent e) {
        int selectedColor = colorPicker.getSelectionModel().getSelectedIndex();
        int selectedCard = cardList.getSelectionModel().getSelectedIndex();
        try {
            switch (selectedColor) {
                case 0 -> client.selectColor(selectedCard, Color.BLUE);
                case 1 -> client.selectColor(selectedCard, Color.GREEN);
                case 2 -> client.selectColor(selectedCard, Color.RED);
                case 3 -> client.selectColor(selectedCard, Color.YELLOW);
                case 4 -> client.selectColor(selectedCard, Color.BLACK);
            }
        } catch (IOException ex) {
            ExceptionDialog.showException(ex);
        }
    }

    private Rules createRules() {
        return new RuleDialog().showAndWait();
    }

    private void playError() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText("Can't play this card");
        alert.showAndWait();
    }

    private synchronized void enable(boolean turn) {
        play.setDisable(!turn);
        accept.setDisable(!turn);
        take.setDisable(!turn);
    }

    @Override
    public synchronized void update(Update update) {
        Platform.runLater(() -> {
            Card[] cards = update.player.getCards();
            ImageView[] images = new ImageView[cards.length];
            for (int i = 0; i < cards.length; i++) {
                images[i] = new ImageView(Cards.getCard(cards[i]));
            }
            ObservableList<ImageView> list = cardList.getItems();
            list.clear();
            list.addAll(images);
            cardList.setItems(list);
            cardList.refresh();
            topCard.setImage(Cards.getCard(update.topCard));
            enable(update.turn);
        });

    }

    @Override
    public synchronized void changed(ObservableValue<? extends Number> observableValue, Number number1, Number number2) {
        if (number2.intValue() > 0) {
            Player player = client.getPlayer();
            Card card = player.getCards()[number2.intValue()];
            if (card instanceof ColorChooser) {
                colorPicker.setDisable(false);
                setColor.setDisable(false);
                colorPicker.setItems(choosableColors);
            } else {
                colorPicker.setDisable(true);
                setColor.setDisable(false);
                colorPicker.setItems(normalColors);
            }
            SelectionModel<String> selector = colorPicker.getSelectionModel();
            switch (card.color()) {
                case BLUE -> selector.select(0);
                case GREEN -> selector.select(1);
                case RED -> selector.select(2);
                case YELLOW -> selector.select(3);
                case BLACK -> selector.select(4);
            }
        }
    }
}
