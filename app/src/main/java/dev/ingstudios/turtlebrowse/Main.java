package dev.ingstudios.turtlebrowse;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.glavo.monetfx.ColorScheme;
import org.glavo.monetfx.beans.property.ColorSchemeProperty;
import org.glavo.monetfx.beans.property.SimpleColorSchemeProperty;

import dev.ingstudios.turtlebrowse.windows.MainWindow;
import dev.ingstudios.turtlebrowse.windows.SetupWindow;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.paint.Color;

public class Main {
	public static ColorSchemeProperty materialColorScheme = new SimpleColorSchemeProperty(
			ColorScheme.fromSeed(Color.web("#BDCF47")));

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new JFXPanel();

			Platform.runLater(() -> {
				final SetupWindow setupWindow = new SetupWindow();
				setupWindow.show();
			});

			final MainWindow mainWindow = new MainWindow();

			mainWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
			mainWindow.setUndecorated(false);

			mainWindow.setVisible(true);
		});
	}
}
