package dev.ingstudios.turtlebrowse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import javax.swing.SwingUtilities;

import org.cef.OS;
import org.glavo.monetfx.ColorScheme;
import org.glavo.monetfx.beans.property.ColorSchemeProperty;
import org.glavo.monetfx.beans.property.SimpleColorSchemeProperty;

import dev.ingstudios.turtlebrowse.db.NitriteDatabase;
import dev.ingstudios.turtlebrowse.db.NitriteDatabase.ProfileStructureWithId;
import dev.ingstudios.turtlebrowse.windows.ProfilePickerWindow;
import dev.ingstudios.turtlebrowse.windows.SetupWindow;
import dev.ingstudios.turtlebrowse.wizard.WizardData;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;

public class Main {
	public static ColorSchemeProperty mainMaterialColorScheme = new SimpleColorSchemeProperty(
			ColorScheme.fromSeed(Color.web("#BDCF47")));
	public final static NitriteDatabase db = NitriteDatabase.getInstance();
	public static ProfilePickerWindow profilePickerWindow;

	public static void main(String[] args) {
		Platform.startup(() -> {
			Platform.setImplicitExit(false);
		});

		setMaterialColorSchemeFromSystem();

		final List<ProfileStructureWithId> profiles = db.getAllProfiles();
		final boolean noProfile = profiles.isEmpty();

		SwingUtilities.invokeLater(() -> {
			new JFXPanel();

			Platform.runLater(() -> {
				if (noProfile) {
					final SetupWindow setupWindow = new SetupWindow();
					Optional<ButtonType> result = setupWindow.showAndWait();

					final WizardData wizardData = setupWindow.wizardData;

					if (result.get() == ButtonType.FINISH) {
						System.out.printf("""
								Finished wizard:
								Name: %s
								Theme: %s
								AI enabled: %s
								""", wizardData.name, wizardData.themeColor.toString(),
								String.valueOf(wizardData.enableAI));

						wizardData.saveData();
					}
				}
			});

			createProfilePicker();
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			NitriteDatabase.getInstance().closeDb();
		}));
	}

	private static void createProfilePicker() {
		Platform.runLater(() -> {
			if (profilePickerWindow == null)
				profilePickerWindow = new ProfilePickerWindow();
			profilePickerWindow.showProfilePickerWindow();
		});
	}

	private static void setMaterialColorSchemeFromSystem() {
		final Color accentColor = Platform.getPreferences().getAccentColor();
		if (accentColor == null) {
			Main.mainMaterialColorScheme.set(ColorScheme.fromSeed(Color.web("#BDCF47")));
		} else {
			Main.mainMaterialColorScheme.set(ColorScheme.fromSeed(accentColor));
		}
	}

	public static Path getStoragePath(String... names) {
		Path dataPath;

		final String appName = "Turtlebrowse";

		final String userHome = System.getProperty("user.home");

		if (OS.isWindows()) {
			String localAppData = System.getenv("LOCALAPPDATA");
			dataPath = Paths.get(localAppData, "ingStudios", appName);
		} else if (OS.isLinux()) {
			String xdgDataHome = System.getenv("XDG_DATA_HOME");
			if (xdgDataHome == null || xdgDataHome.isEmpty()) {
				xdgDataHome = userHome + "/.local/share";
			}
			dataPath = Paths.get(xdgDataHome, "ingStudios", appName);
		} else if (OS.isMacintosh()) {
			dataPath = Paths.get(userHome, "Library", "Application Support", appName);
		} else {
			throw new RuntimeException("Unknown operating system");
		}

		if (names != null) {
			for (final String name : names) {
				dataPath = dataPath.resolve(name);
			}
		}

		return dataPath;
	}
}
