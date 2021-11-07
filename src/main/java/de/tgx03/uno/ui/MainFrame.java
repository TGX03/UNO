package de.tgx03.uno.ui;

import de.tgx03.uno.client.Client;
import de.tgx03.uno.client.ClientUpdate;
import de.tgx03.uno.game.Player;
import de.tgx03.uno.game.Rules;
import de.tgx03.uno.game.cards.Card;
import de.tgx03.uno.game.cards.Color;
import de.tgx03.uno.game.cards.ColorChooser;
import de.tgx03.uno.host.Host;
import de.tgx03.uno.host.HostExceptionHandler;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;

public class MainFrame extends Application implements ClientUpdate, HostExceptionHandler, ChangeListener<Number> {

	private static final ObservableList<String> NORMAL_COLORS = FXCollections.observableArrayList("Blue", "Green", "Red", "Yellow");
	private static final ObservableList<String> AVAILABLE_COLORS = FXCollections.observableArrayList("Blue", "Green", "Red", "Yellow", "Black");

	@FXML
	private ListView<ImageView> cardList;
	@FXML
	private MenuItem createHost;
	@FXML
	private MenuItem startGame;
	@FXML
	private MenuItem joinGame;
	@FXML
	private MenuItem end;
	@FXML
	private ImageView topCard;
	@FXML
	private ComboBox<String> colorPicker;
	@FXML
	private Button play;
	@FXML
	private Button accept;
	@FXML
	private Button take;
	@FXML
	private Button setColor;
	@FXML
	private ListView<String> counter;
	@FXML
	private Label colorText;

	private Host host;
	private Client client;

	/**
	 * Launches a new MainFrame
	 *
	 * @param args Gets sent to JavaFX
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(@NotNull Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/MainFrame.fxml"));
		stage.setTitle("UNO");
		stage.setScene(new Scene(root));
		stage.show();
	}

	/**
	 * Creates a new host
	 * Gets triggered by the "New Game" button
	 *
	 * @param event ignored
	 */
	public synchronized void createHost(@Nullable ActionEvent event) {

		// Show the window requesting the port from the user
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Create Host");
		dialog.setHeaderText("Choose Port");
		dialog.setContentText("Port:");
		Optional<String> result = dialog.showAndWait();

		// When the user entered a valid number, try to set up the host
		if (result.isPresent()) {
			try {
				int port = Integer.parseInt(result.get());
				Rules rules = createRules();
				try {
					host = new Host(port, rules);   // Create the host
					host.registerExceptionHandler(this);

					// Disable the buttons
					createHost.setDisable(true);
					startGame.setDisable(false);
					joinGame.setDisable(true);

					// Set up the client
					client = new Client("localhost", port);
					client.registerReceiver(this);
				} catch (IOException e) {
					ExceptionDialog.showException(e);
				}
			} catch (NumberFormatException e) { // When the user doesn't enter a valid number
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle("Invalid port");
				alert.setContentText("Invalid number entered");
				alert.showAndWait();
			}
		}
	}

	/**
	 * Starts the game
	 * Gets used by the "Start Game" button
	 *
	 * @param e ignored
	 */
	public synchronized void startHost(@Nullable ActionEvent e) {
		host.start();
		cardList.getSelectionModel().selectedIndexProperty().addListener(this);
		startGame.setDisable(true);
		end.setDisable(false);
	}

	/**
	 * Creates a new client that connects to a remote host
	 * Gets used by the "Join Game" button
	 *
	 * @param e ignored
	 */
	public synchronized void createClient(@Nullable ActionEvent e) {
		try {

			// Create the client and register with it
			ConnectionDialog dialog = new ConnectionDialog();
			client = dialog.createClient();
			client.registerReceiver(this);
			cardList.getSelectionModel().selectedIndexProperty().addListener(this);

			// Disable the buttons
			createHost.setDisable(true);
			joinGame.setDisable(true);
		} catch (Exception ex) {
			ExceptionDialog.showException(ex);
		}
	}

	/**
	 * Tires to play the currently selected card
	 * Gets used by the "Play" button
	 *
	 * @param e ignored
	 */
	public synchronized void playCard(@Nullable ActionEvent e) {
		int selected = cardList.getSelectionModel().getSelectedIndex();
		try {
			client.play(selected);
		} catch (IOException ex) {
			ExceptionDialog.showException(ex);
		}
	}

	/**
	 * Tries to jump in with the currently selected card
	 * Gets used by the "Jump" button
	 *
	 * @param e ignored
	 */
	public synchronized void jumpCard(@Nullable ActionEvent e) {
		int selected = cardList.getSelectionModel().getSelectedIndex();
		try {
			client.jump(selected);
		} catch (IOException ex) {
			ExceptionDialog.showException(ex);
		}
	}

	/**
	 * Accepts the penalty cards
	 * Gets used by the "Accept Cards" button
	 *
	 * @param e ignored
	 */
	public synchronized void acceptCards(@Nullable ActionEvent e) {
		try {
			client.acceptCards();
		} catch (IOException ex) {
			ExceptionDialog.showException(ex);
		}
	}

	/**
	 * Picks up a new card
	 * Gets used by the "Take card" button
	 *
	 * @param e ignored
	 */
	public synchronized void takeCard(@Nullable ActionEvent e) {
		try {
			client.takeCard();
		} catch (IOException ex) {
			ExceptionDialog.showException(ex);
		}
	}

	/**
	 * Selects the color of a black card
	 * Gets called when the "Set" button is used and takes what is currently selected in the Combo Box
	 *
	 * @param e ignored
	 */
	public synchronized void selectColor(@Nullable ActionEvent e) {
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

	/**
	 * Ends the game
	 * If this is the host, it tries to gracefully exit by only informing the host
	 * If this is a client, the client just gets killed
	 *
	 * @param e ignored
	 */
	public synchronized void endGame(@Nullable ActionEvent e) {
		if (host == null) {
			client.kill();
		} else {
			host.kill();
			host.removeExceptionHandler(this);
			host = null;
		}
		client.removeReceiver(this);
		client = null;
		end.setDisable(true);
		startGame.setDisable(false);
		joinGame.setDisable(false);
		clear();
	}

	/**
	 * Launch a Dialog requesting the rules for a new game from the player
	 *
	 * @return The created ruleset
	 */
	@Nullable
	private Rules createRules() {
		return new RuleDialog().showAndWait();
	}

	/**
	 * Enables or disables the buttons depending on whether it's currently the turn of this player
	 *
	 * @param turn Whether it's this clients turn
	 */
	private synchronized void enable(boolean turn) {
		play.setDisable(!turn);
		accept.setDisable(!turn);
		take.setDisable(!turn);
	}

	@Override
	public synchronized void update(@NotNull Update update) {

		// Get the images of the cards
		assert update.player != null;
		Card[] cards = update.player.getCards();
		ImageView[] images = new ImageView[cards.length];
		for (int i = 0; i < cards.length; i++) {
			images[i] = new ImageView(Cards.getCard(cards[i]));
		}

		assert update.topCard != null;
		topCard.setImage(Cards.getCard(update.topCard));    // Update the top card
		enable(update.turn);    // Enable or disable the buttons

		Platform.runLater(() -> {

			colorText.setText(update.topCard.color().toString());   // Update the displayed color

			// Clear the listview and add the new images to it
			ObservableList<ImageView> list = cardList.getItems();
			list.clear();
			list.addAll(images);
			cardList.refresh();

			// Update the list showing how many cards the other players have
			ObservableList<String> counts = counter.getItems();
			counts.clear();
			for (int i = 0; i < update.cardNumbers.length; i++) {
				counts.add((i + 1) + ": \t" + update.cardNumbers[i]);
			}
			counter.refresh();
		});
	}

	/**
	 * Clears everything currently displayed and thereby prepares for a new game
	 */
	private synchronized void clear() {
		topCard.setImage(null);
		enable(false);

		Platform.runLater(() -> {
			colorText.setText("");

			cardList.getItems().clear();
			cardList.refresh();

			counter.getItems().clear();
			counter.refresh();
		});
	}

	@Override
	public synchronized void handleException(@NotNull Exception exception) {
		if (ExceptionDialog.showExceptionAnswer(exception) == ExceptionDialog.Answer.END_CONNECTION) {
			endGame(null);
		}
	}

	@Override
	public synchronized void changed(@Nullable ObservableValue<? extends Number> observableValue, @Nullable Number number1, @NotNull Number number2) {
		// Updates the color shown in the combo box
		if (number2.intValue() > 0) {
			Player player = client.getPlayer();
			Card card = player.getCards()[number2.intValue()];
			if (card instanceof ColorChooser) {
				colorPicker.setDisable(false);
				setColor.setDisable(false);
				colorPicker.setItems(AVAILABLE_COLORS);
			} else {
				colorPicker.setDisable(true);
				setColor.setDisable(false);
				colorPicker.setItems(NORMAL_COLORS);
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
