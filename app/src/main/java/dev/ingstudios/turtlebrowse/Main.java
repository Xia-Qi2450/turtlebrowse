package dev.ingstudios.turtlebrowse;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cef.OS;
import org.glavo.monetfx.ColorScheme;
import org.glavo.monetfx.beans.property.ColorSchemeProperty;
import org.glavo.monetfx.beans.property.SimpleColorSchemeProperty;

import dev.ingstudios.turtlebrowse.db.MainDatabase;
import dev.ingstudios.turtlebrowse.db.MainDatabase.ProfileStructureWithId;
import dev.ingstudios.turtlebrowse.windows.MainWindow;
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
	private static final MainDatabase db = MainDatabase.getInstance();
	public static ProfilePickerWindow profilePickerWindow;
	public static ProfileStructureWithId currentProfile;

	public static void main(String[] args) {
		Platform.startup(() -> {
			Platform.setImplicitExit(false);
		});

		setMaterialColorSchemeFromSystem();

		final List<ProfileStructureWithId> profiles = db.getAllProfiles();
		System.out.println("Profiles: " + profiles);

		final String profileId = getProfileId(args);
		System.out.println("Profile ID (after getProfileId): " + profileId);

		try {
			currentProfile = profiles.stream().filter(p -> {
				System.out.println("Profile (p): %s Name: %s".formatted(p.getIdAsString(), p.name()));
				System.out.println("Target profile: " + profileId);
				return p.getIdAsString().equals(profileId);
			}).findFirst()
					.orElse(null);
			System.out.println("Current profile (after filtering profiles): " + currentProfile);
		} catch (Throwable t) {
			System.err.println("An exception occurred while filtering for the current profile: " + t.getMessage());
			t.printStackTrace();
		}

		if (profileId != null) {
			System.out.println("Profile ID is not null.");
			db.closeDb();
			SwingUtilities.invokeLater(() -> {
				System.out.println("Creating main window...");
				final MainWindow mainWindow = new MainWindow(currentProfile);
				mainWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
				mainWindow.setUndecorated(false);
				mainWindow.setVisible(true);
			});
		} else {
			System.out.println("Profile ID is null.");

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

					createProfilePicker();
				});
			});

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				MainDatabase.getInstance().closeDb();
			}));
		}
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

	public static String getUserAgent() {
		String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.1.0 Safari/537.36";

		if (OS.isLinux()) {
			userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.1.0 Safari/537.36";
		} else if (OS.isWindows()) {
			userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.1.0 Safari/537.36";
		} else if (OS.isMacintosh()) {
			userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.1.0 Safari/537.36";
		}

		return userAgent;
	}

	private static String getProfileId(String[] args) {
		String targetProfileId = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--profile-id") && i + 1 < args.length) {
				targetProfileId = args[i + 1];
				break;
			}
		}
		return targetProfileId;
	}

	public static MainDatabase getDb() {
		return db;
	}

	public static void createMainWindow(ProfileStructureWithId profile) {
		db.closeDb();

		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final String classpath = System.getProperty("java.class.path");

		final String profileId = profile.getIdAsString();

		List<String> command = new ArrayList<>();
		command.add(javaBin);

		final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		List<String> vmArguments = runtimeMxBean.getInputArguments();
		for (String arg : vmArguments) {
			if (!arg.contains("-agentlib") && !arg.contains("-javaagent")) {
				command.add(arg);
			}
		}

		command.add("-cp");
		command.add(classpath);
		command.add("dev.ingstudios.turtlebrowse.Main");
		command.add("--profile-id");
		command.add(profileId);

		System.out.println("Spawning Command: " + String.join(" ", command));

		final ProcessBuilder builder = new ProcessBuilder(command);

		try {
			builder.start();
			System.out.printf("Successfully spawned process for profile: %s\n", profileId);
			Platform.runLater(() -> profilePickerWindow.close());
		} catch (IOException e) {
			System.err.printf("Failed while running process for profile: %s\n", profileId);
			e.printStackTrace();
		}

		System.exit(0);
	}
}
