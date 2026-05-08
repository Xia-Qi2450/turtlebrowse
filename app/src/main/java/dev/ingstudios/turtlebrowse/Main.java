package dev.ingstudios.turtlebrowse;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import dev.ingstudios.turtlebrowse.components.MainWindow;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();

            mainWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
            mainWindow.setUndecorated(false);

            mainWindow.setVisible(true);
        });
    }
}