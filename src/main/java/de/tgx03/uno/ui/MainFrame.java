package de.tgx03.uno.ui;

import de.tgx03.uno.client.Client;
import de.tgx03.uno.client.ClientUpdate;
import de.tgx03.uno.game.Rules;
import de.tgx03.uno.game.cards.Card;
import de.tgx03.uno.host.Host;
import de.tgx03.uno.messaging.Update;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class MainFrame extends Application implements ClientUpdate {

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

    private final ObservableList<ImageView> list = FXCollections.observableArrayList();

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
        stage.show();
    }

    public void createHost(ActionEvent event) {
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

    public void startHost(ActionEvent e) {
        host.start();
    }

    public void createClient(ActionEvent e) {
        try {
            ConnectionDialog dialog = new ConnectionDialog();
            client = dialog.createClient();
            client.registerReceiver(this);
        } catch (Exception ex) {
            ExceptionDialog.showException(ex);
        }
    }

    private Rules createRules() {
        return new RuleDialog().showAndWait();
    }

    @Override
    public void update(Update update) {
        Card[] cards = update.player.getCards();
        ImageView[] images = new ImageView[cards.length];
        for (int i = 0; i < cards.length; i++)  {
            images[i] = new ImageView(Cards.getCard(cards[i]));
        }
        list.clear();
        list.addAll(images);
        cardList.setItems(list);
        topCard.setImage(Cards.getCard(update.topCard));
    }
}
