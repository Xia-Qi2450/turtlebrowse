package dev.ingstudios.turtlebrowse.windows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.jfoenix.controls.JFXButton;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.db.MainDatabase.ProfileStructure;
import dev.ingstudios.turtlebrowse.db.MainDatabase.ProfileStructureWithId;
import dev.ingstudios.turtlebrowse.managers.DiscordPresenceManager;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class NewProfileWindow extends Stage {
	String name;
	Color themeColor = Main.mainMaterialColorScheme.getPrimary().get();
	String profileAvatarPath = "/images/avatars/default_avatar.png";
	File uploadedAvatarFile = null;

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
		profileCreationBox.setStyle("-fx-spacing: 20px; -fx-padding: 10px;");
		profileCreationBox.setAlignment(Pos.CENTER);
		profileCreationBox.setFillWidth(false);

		final Label titleLabel = new Label("New Profile");
		titleLabel.setFont(Font.font("Google Sans Flex", FontWeight.BOLD, 25));

		final ImageView profileImageView = new ImageView(profileAvatarPath);
		profileImageView.setFitWidth(200);
		profileImageView.setFitHeight(200);
		profileImageView.setPreserveRatio(false);
		profileImageView.setSmooth(true);

		final Circle imageClip = new Circle(100, 100, 100);
		profileImageView.setClip(imageClip);
		profileImageView.setPickOnBounds(true);

		profileImageView.setOnMouseEntered(event -> {
			profileImageView.setCursor(Cursor.HAND);
		});
		profileImageView.setOnMouseDragExited(event -> {
			profileImageView.setCursor(Cursor.DEFAULT);
		});
		profileImageView.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY) {
				final FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Upload Profile Image");
				fileChooser.getExtensionFilters().addAll(
						new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));

				final File profileImageFile = fileChooser.showOpenDialog(this);
				if (profileImageFile != null) {
					uploadedAvatarFile = profileImageFile;
					final Image profileImage = new Image(profileAvatarPath);
					profileImageView.setImage(profileImage);
				}
			}
		});

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
		seedColorLabel.setFont(Font.font("Google Sans Flex", FontWeight.NORMAL, 25));

		final ColorPicker seedColorPicker = new ColorPicker(themeColor);
		seedColorPicker.setBackground(new Background(
				new BackgroundFill(Main.mainMaterialColorScheme.getSurfaceContainer().get(), new CornerRadii(25),
						null)));
		seedColorPicker.setOnAction(event -> {
			themeColor = seedColorPicker.getValue();
		});

		final JFXButton createButton = new JFXButton("Create");
		createButton.setFont(Font.font("Google Sans Flex", FontWeight.NORMAL, 25));
		createButton.setTextFill(Main.mainMaterialColorScheme.getOnPrimaryContainer().get());
		createButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.mainMaterialColorScheme.getPrimaryContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, Main.mainMaterialColorScheme.getPrimaryContainer()));
		createButton.setOnAction(event -> createProfile());

		profileCreationBox.getChildren().addAll(titleLabel, profileImageView, nameTextField, seedColorLabel,
				seedColorPicker,
				createButton);

		root.setCenter(profileCreationBox);

		setScene(newProfileScene);

		show();

		DiscordPresenceManager.getInstance().updateDiscordPresence("Creating a new profile");
	}

	private void createProfile() {
		final ProfileStructure profile = new ProfileStructure(name, themeColor);

		final ProfileStructureWithId newProfile = Main.getDb().createProfile(profile);

		saveProfileImage(newProfile.getIdAsString());

		Main.createMainWindow(newProfile);
	}

	private void saveProfileImage(String profileId) {
		if (profileAvatarPath == null)
			return;

		final Path profilePath = Main.getStoragePath("profiles", profileId, "avatar");

		final File profileParentDir = profilePath.getParent().toFile();
		if (!profileParentDir.exists()) {
			profileParentDir.mkdirs();
		}

		try {
			Files.copy(uploadedAvatarFile != null ? uploadedAvatarFile.toPath() : Path.of(profileAvatarPath),
					profilePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
