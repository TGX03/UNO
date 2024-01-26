package eu.tgx03.uno.ui;

import eu.tgx03.uno.client.SocketClient;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * A class responsible for getting the hostname and port of a game server.
 */
public class ConnectionDialog {

	/**
	 * The (hopefully) created client.
	 */
	private SocketClient client;
	/**
	 * Whether a valid port was entered.
	 */
	private boolean validPort = true;

	/**
	 * Requests the data for a client from the user
	 * and then sets up the client and returns it.
	 *
	 * @return The created client.
	 */
	@Nullable
	public SocketClient createClient() {
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);

		GridPane layout = createConnectionGrid(stage);

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

	/**
	 * Creates the Grid to get the connection details from the user
	 * for the given stage.
	 * @param stage The stage the grid shall be placed on.
	 * @return The created Grid Pane with fields and labels.
	 */
	@NotNull
	private GridPane createConnectionGrid(Stage stage) {
		TextField host = new TextField("Host");
		TextField port = new TextField("Port");
		Label hostLabel = new Label("Host:");
		Label portLabel = new Label("Port");

		Button confirm = new Button("Confirm");
		confirm.setOnAction(e -> {
			try {
				client = new SocketClient(host.getText(), Integer.parseInt(port.getText()));
				stage.close();
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
		return layout;
	}
}
