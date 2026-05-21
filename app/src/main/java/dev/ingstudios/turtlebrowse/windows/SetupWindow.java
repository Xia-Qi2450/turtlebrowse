package dev.ingstudios.turtlebrowse.windows;

import dev.ingstudios.turtlebrowse.Main;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class SetupWindow {
	private final Stage primaryStage = new Stage();

	public SetupWindow() {
		Font.loadFont(getClass().getResourceAsStream("/fonts/google_sans_flex.ttf"), 10);
		Font.loadFont(getClass().getResourceAsStream("/fonts/material_icons_outlined.otf"), 10);

		final BorderPane root = new BorderPane();
		root.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
		root.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.materialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(backgroundColor, null, null));
		}, Main.materialColorScheme.getSurface()));

		final Label welcomeText = new Label("Welcome to Turtlebrowse");
		welcomeText.setFont(Font.font("Google Sans Flex", FontWeight.BOLD, 50));

		root.setCenter(welcomeText);

		final Scene scene = new Scene(root, 800, 600);

		primaryStage.setTitle("Turtlebrowse Setup");
		primaryStage.setScene(scene);
	}

	public void show() {
		primaryStage.show();
	}

	public void hide() {
		primaryStage.hide();
	}
}
