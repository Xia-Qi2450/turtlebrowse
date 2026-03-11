package ingstudios.turtlebrowse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class TabBar extends JPanel {
    private final Map<CefBrowser, Button> tabMap = new HashMap<>();
    private final HBox root = new HBox();
    private MainWindow parent;

    public TabBar(CefClient client, ArrayList<CefBrowser> tabs, MainWindow parent) {
        this.parent = parent;

        this.setLayout(new java.awt.BorderLayout());

        JFXPanel tabPanel = new JFXPanel();
        tabPanel.setPreferredSize(new java.awt.Dimension(1200, 50));

        root.setAlignment(Pos.CENTER_LEFT);
        root.setSpacing(10);
        root.setPadding(new Insets(10));

        Platform.runLater(() -> {
            for (CefBrowser browser : tabs) {
                addTabToUI(browser);
            }
        });

        Scene tabBarScene = new Scene(root);
        tabPanel.setScene(tabBarScene);
        Platform.runLater(() -> {
            root.prefWidthProperty().bind(tabBarScene.widthProperty());
            root.prefHeightProperty().bind(tabBarScene.heightProperty());
        });

        this.add(tabPanel);
    }

    public void addTabToUI(CefBrowser browser) {
        Button tabButton = new Button("Loading...");
        tabButton.setPrefWidth(150);
        tabButton.setOnAction(event -> {
            SwingUtilities.invokeLater(() -> {
                parent.showBrowser(browser);
            });
        });

        tabMap.put(browser, tabButton);

        root.getChildren().add(tabButton);
    }

    public void setTabTitle(CefBrowser browser, String title) {
        Button button = tabMap.get(browser);
        if (button != null) button.setText(title);
    }
}
