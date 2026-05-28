package dev.ingstudios.turtlebrowse.windows;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.dizitart.no2.collection.NitriteId;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.db.NitriteDatabase;
import dev.ingstudios.turtlebrowse.db.NitriteDatabase.ProfileStructureWithId;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ProfilePickerWindow extends Stage {
	private final NitriteDatabase db = NitriteDatabase.getInstance();
	private final List<ProfileStructureWithId> profiles;

	public ProfilePickerWindow() {
		setTitle("Turtlebrowse");

		// profiles = db.getAllProfiles();

		// Test profiles
		final List<ProfileStructureWithId> testProfiles = new ArrayList<>();

		for (int i = 0; i < 3; i++) {
			testProfiles.add(
					new ProfileStructureWithId("Profile %s".formatted(i), Color.web("#11bedd"), NitriteId.newId()));
		}

		profiles = testProfiles;

		System.out.printf("Profiles (test): %s\n", profiles.toString());

		final BorderPane root = new BorderPane();
		root.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.mainMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(backgroundColor, null, null));
		}, Main.mainMaterialColorScheme.getSurface()));

		final Scene profilePickerScene = new Scene(root, 800, 600);
		profilePickerScene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

		final HBox profilesBox = new HBox();
		profilesBox.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
		profilesBox.setAlignment(Pos.CENTER);
		profilesBox.setFillHeight(false);

		for (final ProfileStructureWithId profile : profiles) {
			final VBox profileBox = new VBox();
			profileBox.setStyle("-fx-padding: 10px;");
			profileBox.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = Main.mainMaterialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, Main.mainMaterialColorScheme.getSurfaceContainer()));
			profileBox.setAlignment(Pos.CENTER);
			profileBox.setOnMouseEntered(event -> {
				profileBox.setCursor(Cursor.HAND);
			});
			profileBox.setOnMouseDragExited(event -> {
				profileBox.setCursor(Cursor.DEFAULT);
			});
			profileBox.setOnMouseClicked(event -> {
				event.consume();
				createMainWindow(profile);
			});

			final Label profileName = new Label(profile.name());
			profileName.setFont(Font.font("Google Sans Flex", FontWeight.BOLD, 25));

			profileBox.getChildren().add(profileName);

			profilesBox.getChildren().add(profileBox);
		}

		root.setCenter(profilesBox);

		setScene(profilePickerScene);

		setOnCloseRequest(event -> {
			Platform.exit();
			System.exit(0);
		});
	}

	public void showProfilePickerWindow() {
		if (profiles.size() <= 1) {
			final ProfileStructureWithId profile = db.getFirstProfile();
			createMainWindow(profile);
			return;
		}

		show();
	}

	private void createMainWindow(ProfileStructureWithId profile) {
		SwingUtilities.invokeLater(() -> {
			final MainWindow mainWindow = new MainWindow(profile);
			mainWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
			mainWindow.setUndecorated(false);
			mainWindow.setVisible(true);
		});

		hide();
	}
}
