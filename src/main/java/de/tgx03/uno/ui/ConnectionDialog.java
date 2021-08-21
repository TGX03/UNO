package de.tgx03.uno.ui;

import de.tgx03.uno.client.Client;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ConnectionDialog {

    private Client client;
    private boolean validPort = true;

    public Client createClient() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        TextField host = new TextField("Host");
        TextField port = new TextField("Port");
        Label hostLabel = new Label("Host:");
        Label portLabel = new Label("Port");

        Button confirm = new Button("Confirm");
        confirm.setOnAction(e -> {
            try {
                client = new Client(host.getText(), Integer.parseInt(port.getText()));
            } catch (IOException ignored) {
            } catch (NumberFormatException ex) {
                validPort = false;
            }
        });

        GridPane layout = new GridPane();
        layout.add(hostLabel, 0, 0);
        layout.add(host, 1, 0);
        layout.add(portLabel, 0, 1);
        layout.add(port, 1, 1);
        layout.add(confirm, 1, 2);

        Scene scene = new Scene(layout, 300, 100);
        stage.setTitle("Create connection");
        stage.setScene(scene);
        stage.showAndWait();

        if (!validPort) {
            throw new IllegalArgumentException("Invalid port provided");
        } else if (client == null) {
            throw new RuntimeException("Couldn't create client");
        } else {
            return client;
        }
    }
}
