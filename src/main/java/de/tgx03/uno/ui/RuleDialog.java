package de.tgx03.uno.ui;

import de.tgx03.uno.game.Rules;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

/**
 * A window requesting the rules for a game of UNO.
 */
public class RuleDialog {

	private Rules rules;

	/**
	 * Shows the user a dialog and creates the ruleset
	 * and then returns it.
	 *
	 * @return The created rules.
	 */
	@NotNull
	public Rules showAndWait() {
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);

		CheckBox jump = new CheckBox("Jumping");
		CheckBox stack = new CheckBox("Stacking");
		CheckBox force = new CheckBox("Force Continue");

		Button confirm = new Button("Confirm");
		confirm.setOnAction(e -> {
			rules = new Rules(jump.isSelected(), stack.isSelected(), force.isSelected());
			stage.close();
		});

		GridPane layout = new GridPane();
		layout.add(jump, 0, 0);
		layout.add(stack, 0, 1);
		layout.add(force, 0, 2);
		layout.add(confirm, 0, 3);

		Scene scene = new Scene(layout, 300, 100);
		stage.setTitle("Select rules");
		stage.setScene(scene);
		stage.showAndWait();

		return rules;
	}
}
