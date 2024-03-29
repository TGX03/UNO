package eu.tgx03.uno.ui;

import eu.tgx03.uno.client.ClientUpdate;
import eu.tgx03.uno.client.SocketClient;
import eu.tgx03.uno.game.Player;
import eu.tgx03.uno.game.Rules;
import eu.tgx03.uno.game.cards.Card;
import eu.tgx03.uno.game.cards.ChooseColor;
import eu.tgx03.uno.game.cards.Color;
import eu.tgx03.uno.messaging.Update;
import eu.tgx03.uno.server.Server;
import eu.tgx03.uno.server.SocketServer;
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
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * The main UI hosting most of the game elements and dealing with client and host.
 */
public class MainFrame extends Application implements ClientUpdate, ChangeListener<Number> {

	/**
	 * The name of the normal colors available for cards.
	 */
	private static final ObservableList<String> NORMAL_COLORS = FXCollections.observableArrayList("Blue", "Green", "Red", "Yellow");
	/**
	 * All the available color including black.
	 */
	private static final ObservableList<String> AVAILABLE_COLORS = FXCollections.observableArrayList("Blue", "Green", "Red", "Yellow", "Black");

	/**
	 * The exception queue.
	 */
	private final ArrayBlockingQueue<Throwable> exceptionQueue = new ArrayBlockingQueue<>(5);

	/**
	 * A visual representation of all the cards this player currently has.
	 */
	@FXML
	private ListView<ImageView> cardList;
	/**
	 * The button to create a new host.
	 */
	@FXML
	private MenuItem createHost;
	/**
	 * The button to start the game.
	 */
	@FXML
	private MenuItem startGame;
	/**
	 * The button to join another game.
	 */
	@FXML
	private MenuItem joinGame;
	/**
	 * The button to end a game.
	 */
	@FXML
	private MenuItem end;
	/**
	 * The representation of the card currently on top.
	 */
	@FXML
	private ImageView topCard;
	/**
	 * The combo box allowing for the choosing of the color of a wild card.
	 */
	@FXML
	private ComboBox<String> colorPicker;
	/**
	 * The button to play the selected card the normal way.
	 */
	@FXML
	private Button play;
	/**
	 * The button to accept a penalty that has been stacked up.
	 */
	@FXML
	private Button accept;
	/**
	 * The button to pick up a card if you can't play any of your current cards.
	 */
	@FXML
	private Button take;
	/**
	 * The button to confirm a selected color for a wild card.
	 */
	@FXML
	private Button setColor;
	/**
	 * The list of how many cards the other players currently hold.
	 */
	@FXML
	private ListView<String> counter;
	/**
	 * The color of the current top Card. Useful if a wild card is on top.
	 */
	@FXML
	private Label colorText;

	/**
	 * The host object if it has been created.
	 */
	private Server server;
	/**
	 * The client object of the game.
	 */
	private SocketClient client;

	/**
	 * Launches a new MainFrame.
	 *
	 * @param args Gets sent to JavaFX.
	 */
	public static void main(@Nullable String[] args) {
		launch(args);
	}

	@Override
	public void start(@NotNull Stage stage) throws Exception {
		URL resource = getClass().getResource("/MainFrame.fxml");
		assert resource != null;
		Parent root = FXMLLoader.load(resource);
		stage.setTitle("UNO");
		stage.setScene(new Scene(root));
		stage.show();
		Thread exceptionHandler = new Thread(new ExceptionHandler());
		exceptionHandler.setDaemon(true);
		exceptionHandler.start();
	}

	/**
	 * Creates a new host.
	 * Gets triggered by the "New Game" button.
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
					server = new SocketServer(port, rules);   // Create the host
					server.registerExceptionHandler(this);

					// Disable the buttons
					createHost.setDisable(true);
					startGame.setDisable(false);
					joinGame.setDisable(true);

					// Set up the client
					client = new SocketClient("localhost", port);
					client.registerReceiver(this);
				} catch (IOException e) {
					handleInternalException(e);
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
	 * Starts the game.
	 * Gets used by the "Start Game" button.
	 *
	 * @param e ignored
	 */
	public synchronized void startHost(@Nullable ActionEvent e) {
		server.start();
		cardList.getSelectionModel().selectedIndexProperty().addListener(this);
		startGame.setDisable(true);
		end.setDisable(false);
	}

	/**
	 * Creates a new client that connects to a remote host.
	 * Gets used by the "Join Game" button.
	 *
	 * @param e ignored
	 */
	public synchronized void createClient(@Nullable ActionEvent e) {
		try {

			// Create the client and register with it
			ConnectionDialog dialog = new ConnectionDialog();
			client = dialog.createClient();
			if (client != null) {
				client.registerReceiver(this);
				cardList.getSelectionModel().selectedIndexProperty().addListener(this);

				// Disable the buttons
				createHost.setDisable(true);
				joinGame.setDisable(true);
			}
		} catch (Exception ex) {
			handleInternalException(ex);
		}
	}

	/**
	 * Tires to play the currently selected card.
	 * Gets used by the "Play" button.
	 *
	 * @param e ignored
	 */
	public synchronized void playCard(@Nullable ActionEvent e) {
		int selected = cardList.getSelectionModel().getSelectedIndex();
		try {
			client.play(selected);
		} catch (IOException ex) {
			handleInternalException(ex);
		}
	}

	/**
	 * Tries to jump in with the currently selected card.
	 * Gets used by the "Jump" button.
	 *
	 * @param e ignored
	 */
	public synchronized void jumpCard(@Nullable ActionEvent e) {
		int selected = cardList.getSelectionModel().getSelectedIndex();
		try {
			client.jump(selected);
		} catch (IOException ex) {
			handleInternalException(ex);
		}
	}

	/**
	 * Accepts the penalty cards.
	 * Gets used by the "Accept Cards" button.
	 *
	 * @param e ignored
	 */
	public synchronized void acceptCards(@Nullable ActionEvent e) {
		try {
			client.acceptCards();
		} catch (IOException ex) {
			handleInternalException(ex);
		}
	}

	/**
	 * Picks up a new card.
	 * Gets used by the "Take card" button.
	 *
	 * @param e ignored
	 */
	public synchronized void takeCard(@Nullable ActionEvent e) {
		try {
			client.takeCard();
		} catch (IOException ex) {
			handleInternalException(ex);
		}
	}

	/**
	 * Selects the color of a black card.
	 * Gets called when the "Set" button is used and takes what is currently selected in the Combo Box.
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
			handleInternalException(ex);
		}
	}

	/**
	 * Ends the game.
	 * If this is the host, it tries to gracefully exit by only informing the host.
	 * If this is a client, the client just gets killed.
	 *
	 * @param e ignored
	 */
	public synchronized void endGame(@Nullable ActionEvent e) {
		if (server == null && client == null) {
			return;
		} else if (server == null) {
			client.kill();
		} else {
			server.kill();
			server.removeExceptionHandler(this);
			server = null;
		}
		client.removeReceiver(this);
		client = null;
		end.setDisable(true);
		startGame.setDisable(false);
		joinGame.setDisable(false);
		clear();
	}

	/**
	 * Launch a Dialog requesting the rules for a new game from the player.
	 *
	 * @return The created ruleset.
	 */
	@NotNull
	private Rules createRules() {
		Optional<Rules> result = new RuleDialog().requestRules();
		return result.orElseGet(Rules::new);
	}

	/**
	 * Enables or disables the buttons depending on whether it's currently the turn of this player.
	 *
	 * @param turn Whether it's this clients turn.
	 */
	private synchronized void enable(boolean turn) {
		play.setDisable(!turn);
		accept.setDisable(!turn);
		take.setDisable(!turn);
	}

	@Override
	public synchronized void update(@NotNull Update update) {

		if (update.ended) {
			endGame(null);
		} else {
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
				counts.add("Stack:\t" + update.stack);
				for (int i = 0; i < update.cardNumbers.length; i++) {
					counts.add((i + 1) + ":\t" + update.cardNumbers[i]);
				}
				counter.refresh();
			});
		}
	}

	/**
	 * Clears everything currently displayed and thereby prepares for a new game.
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

	/**
	 * Handles exceptions that were directly caused by user interaction, or after they were removed from the queue.
	 *
	 * @param exception The exception to handle.
	 */
	private synchronized void handleInternalException(@NotNull Throwable exception) {
		if ((client != null || server != null) && ExceptionDialog.showExceptionAnswer(exception) == ExceptionDialog.Answer.END_CONNECTION) {
			endGame(null);
		} else {
			ExceptionDialog.showException(exception);
		}
	}

	@Override
	public void handleException(@NotNull Throwable exception) {
		try {
			this.exceptionQueue.put(exception);
		} catch (InterruptedException e) {
			handleException(e);
		}
	}

	@Override
	public synchronized void changed(@Nullable ObservableValue<? extends Number> observableValue, @Nullable Number number1, @NotNull Number number2) {
		// Updates the color shown in the combo box
		if (number2.intValue() >= 0) {
			Player player = client.getPlayer();
			Card card = player.getCards()[number2.intValue()];
			if (card instanceof ChooseColor) {
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

	/**
	 * A class providing the runnable for handling incoming exceptions.
	 */
	private class ExceptionHandler implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					handleInternalException(exceptionQueue.take());
				} catch (InterruptedException ex) {
					handleException(ex);
				}
			}
		}

	}
}
