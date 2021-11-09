package de.tgx03.uno.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Shows a dialog presenting the user with the stacktrace of an exception
 */
public final class ExceptionDialog {

	private ExceptionDialog() throws IllegalAccessException {
		throw new IllegalAccessException();
	}

	/**
	 * Quickly show an exception that doesn't allow any kind of interaction from the user
	 *
	 * @param e The exception to show
	 */
	public static void showException(@NotNull Exception e) {

		String exceptionText = parseStacktrace(e);

		Platform.runLater(() -> createAlert(e, exceptionText).showAndWait());
	}

	/**
	 * Shows an exception and asks whether the user wants to continue with the execution or to abort
	 *
	 * @param exception The exception to show
	 * @return The option the user selected
	 */
	public static Answer showExceptionAnswer(@NotNull Exception exception) {

		String exceptionText = parseStacktrace(exception);
		final Container container = new Container();

		Platform.runLater(() -> {

			Alert alert = createAlert(exception, exceptionText);

			ButtonType ignore = new ButtonType("Continue");
			ButtonType abort = new ButtonType("End Game");
			alert.getButtonTypes().setAll(ignore, abort);

			synchronized (container) {
				Optional<ButtonType> result = alert.showAndWait();
				if (result.isEmpty() || result.get() == ignore) {
					container.answer = Answer.IGNORE;
				} else {
					container.answer = Answer.END_CONNECTION;
				}
				container.notifyAll();
			}
		});
		synchronized (container) {
			while (container.answer == null) {
				try {
					container.wait();
				} catch (InterruptedException ex) {
					exception.printStackTrace();
				}
			}
			return container.answer;
		}
	}

	/**
	 * Creates a string holding the stacktrace of an exception
	 *
	 * @param exception The requested exception
	 * @return The stacktrace of that exception
	 */
	private static String parseStacktrace(@NotNull Exception exception) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		return sw.toString();
	}

	/**
	 * Creates the basic alert from an exception and its stacktrace
	 *
	 * @param exception  The exception to show
	 * @param stacktrace The stacktrace of the excption
	 * @return The created Alert
	 */
	private static Alert createAlert(@NotNull Exception exception, @Nullable String stacktrace) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText("An exception occurred");
		alert.setContentText(exception.getMessage());

		Label label = new Label("The exception stacktrace was:");

		TextArea textArea = new TextArea(stacktrace);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		alert.getDialogPane().setExpandableContent(expContent);
		return alert;
	}

	/**
	 * The choices the user has when he sees a dialog
	 */
	public enum Answer {
		/**
		 * The host should carry on
		 */
		IGNORE,
		/**
		 * The User selected to kill the game
		 */
		END_CONNECTION
	}

	private static class Container {

		private Answer answer;

	}
}
