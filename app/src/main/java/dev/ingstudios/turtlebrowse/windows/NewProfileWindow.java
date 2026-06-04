package dev.ingstudios.turtlebrowse.windows;

import org.dizitart.no2.collection.NitriteId;

import com.jfoenix.controls.JFXButton;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.db.MainDatabase.ProfileStructureWithId;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class NewProfileWindow extends Stage {
	final NitriteId id = NitriteId.newId();
	String name;
	Color themeColor;

	public NewProfileWindow() {
		setTitle("Turtlebrowse");

		final BorderPane root = new BorderPane();
		root.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.mainMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(backgroundColor, null, null));
		}, Main.mainMaterialColorScheme.getSurface()));

		final Scene newProfileScene = new Scene(root, 800, 600);
		newProfileScene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

		final VBox profileCreationBox = new VBox();
		profileCreationBox.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
		profileCreationBox.setAlignment(Pos.CENTER);
		profileCreationBox.setFillWidth(false);

		final Label titleLabel = new Label("New Profile");

		final TextField nameTextField = new TextField();
		nameTextField.setText(name);
		nameTextField.setStyle("-fx-padding: 10px;");
		nameTextField.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.mainMaterialColorScheme.getSurfaceContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, Main.mainMaterialColorScheme.getSurfaceContainer()));
		nameTextField.setPromptText("Enter your preferred name");
		nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			name = newValue;
		});

		final Label seedColorLabel = new Label("Browser theme color");

		final ColorPicker seedColorPicker = new ColorPicker(themeColor);
		seedColorPicker.setBackground(new Background(
				new BackgroundFill(Main.mainMaterialColorScheme.getSurfaceContainer().get(), new CornerRadii(25),
						null)));
		seedColorPicker.setOnAction(event -> {
			themeColor = seedColorPicker.getValue();
		});

		final JFXButton createButton = new JFXButton("Create");
		createButton.setTextFill(Main.mainMaterialColorScheme.getOnPrimaryContainer().get());
		createButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.mainMaterialColorScheme.getPrimaryContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, Main.mainMaterialColorScheme.getPrimaryContainer()));
		createButton.setOnAction(event -> createProfile());

		profileCreationBox.getChildren().addAll(titleLabel, nameTextField, seedColorLabel, seedColorPicker,
				createButton);

		root.setCenter(profileCreationBox);

		setScene(newProfileScene);

		show();
	}

	private void createProfile() {
		final ProfileStructureWithId profile = new ProfileStructureWithId(name, themeColor, id);

		Main.getDb().createProfile(profile);

		Main.createMainWindow(profile);

		close();

		Platform.runLater(() -> Platform.exit());

		System.exit(0);
	}
}
