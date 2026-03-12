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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class TabBar extends JPanel {
    private final Map<CefBrowser, HBox> tabMap = new HashMap<>();
    private final HBox root = new HBox();
    private MainWindow parent;

    public TabBar(CefClient client, ArrayList<CefBrowser> tabs, MainWindow parent) {
        this.parent = parent;

        this.setLayout(new java.awt.BorderLayout());

        final JFXPanel tabPanel = new JFXPanel();
        tabPanel.setPreferredSize(new java.awt.Dimension(1200, 50));

        root.setFillHeight(true);
        root.setStyle("-fx-spacing: 10px; -fx-padding: 10px; -fx-background-color: #FCFAED; -fx-fill: #1B1C14; -fx-text-fill: #1B1C14;");
        root.setAlignment(Pos.CENTER_LEFT);

        final Button createTabButton = new Button("+");
        createTabButton.setStyle("-fx-background-color: #F0EEE1; -fx-border-radius: 25px; -fx-background-radius: 25px; -fx-padding: 10px;");
        createTabButton.setMaxHeight(Double.MAX_VALUE);
        createTabButton.setOnAction(event -> {
            SwingUtilities.invokeLater(() -> {
                parent.createTab(parent.START_URL);
            });
        });
        root.getChildren().add(createTabButton);

        Platform.runLater(() -> {
            for (CefBrowser browser : tabs) {
                addTabToUI(browser);
            }
        });

        final Scene tabBarScene = new Scene(root);
        tabPanel.setScene(tabBarScene);
        Platform.runLater(() -> {
            root.prefWidthProperty().bind(tabBarScene.widthProperty());
            root.prefHeightProperty().bind(tabBarScene.heightProperty());
        });

        this.add(tabPanel);
    }

    public void addTabToUI(CefBrowser browser) {
        final Label tabTitle = new Label("Loading...");

        final Button closeButton = new Button("X");
        closeButton.setStyle("-fx-background-color: transparent;");

        final HBox tabBox = new HBox(10);
        tabBox.setStyle("-fx-background-color: #F0EEE1; -fx-border-radius: 25px; -fx-background-radius: 25px; -fx-padding: 10px; -fx-pref-width: 150px;");
        tabBox.setMaxHeight(Double.MAX_VALUE);
        final Region tabSpacer = new Region();
        tabBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(tabSpacer, Priority.ALWAYS);
        tabBox.setOnMouseClicked(event -> {
            SwingUtilities.invokeLater(() -> {
                parent.showBrowser(browser);
            });
        });

        closeButton.prefHeightProperty().bind(tabBox.heightProperty().multiply(0.8));
        closeButton.setOnAction(event -> {
            event.consume();

            Platform.runLater(() -> {
                root.getChildren().remove(tabBox);
            });

            SwingUtilities.invokeLater(() -> {
                tabMap.remove(browser);
                parent.closeTab(browser);
            });
        });

        tabBox.getChildren().addAll(tabTitle, tabSpacer, closeButton);

        tabMap.put(browser, tabBox);

        root.getChildren().add(tabBox);
    }

    public void setTabTitle(CefBrowser browser, String title) {
        final HBox box = tabMap.get(browser);
        final Label tabTitle = (Label) box.getChildren().get(0);
        tabTitle.setText(title);
    }
}
