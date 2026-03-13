package ingstudios.turtlebrowse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class TabBar extends JPanel {
    private final Map<CefBrowser, HBox> tabMap = new HashMap<>();
    private final HBox root = new HBox();
    private MainWindow parent;

    public TabBar(CefClient client, ArrayList<CefBrowser> tabs, MainWindow parent) {
        this.parent = parent;

        this.setLayout(new java.awt.BorderLayout());

        final JFXPanel tabPanel = new JFXPanel();
        tabPanel.setPreferredSize(new java.awt.Dimension(1200, 50));

        root.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        root.setFillHeight(true);
        root.setStyle("-fx-spacing: 10px; -fx-padding: 10px; -fx-background-color: #FCFAED; -fx-fill: #1B1C14; -fx-text-fill: #1B1C14;");
        root.setAlignment(Pos.CENTER_LEFT);

        final Button createTabButton = new Button("+");
        createTabButton.setGraphic(new FontIcon(Material2OutlinedAL.ADD));
        createTabButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        createTabButton.setStyle("-fx-background-color: #FCFAED; -fx-border-radius: 25px; -fx-background-radius: 25px; -fx-padding: 10px;");
        createTabButton.setMaxHeight(Double.MAX_VALUE);
        createTabButton.setOnMouseEntered(event -> {
            createTabButton.setCursor(Cursor.HAND);
        });
        createTabButton.setOnMouseDragExited(event -> {
            createTabButton.setCursor(Cursor.DEFAULT);
        });
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
        closeButton.setGraphic(new FontIcon(Material2OutlinedAL.CLOSE));
        closeButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        closeButton.setStyle("-fx-background-color: transparent;");

        final HBox tabBox = new HBox(10);
        tabBox.setStyle("-fx-background-color: #F0EEE1; -fx-border-radius: 25px; -fx-background-radius: 25px; -fx-padding: 10px; -fx-pref-width: 150px;");
        tabBox.setMaxHeight(Double.MAX_VALUE);
        final Region tabSpacer = new Region();
        tabBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(tabSpacer, Priority.ALWAYS);
        tabBox.setOnMouseEntered(event -> {
            tabBox.setCursor(Cursor.HAND);
        });
        tabBox.setOnMouseDragExited(event -> {
            tabBox.setCursor(Cursor.DEFAULT);
        });
        tabBox.setOnMouseClicked(event -> {
            SwingUtilities.invokeLater(() -> {
                parent.showTab(browser);
            });
        });

        closeButton.prefHeightProperty().bind(tabBox.heightProperty().multiply(0.8));
        closeButton.setOnMouseEntered(event -> {
            closeButton.setCursor(Cursor.HAND);
        });
        closeButton.setOnMouseDragExited(event -> {
            closeButton.setCursor(Cursor.DEFAULT);
        });
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

        root.getChildren().add(Math.max(0, root.getChildren().size() - 1), tabBox);
    }

    public void setTabTitle(CefBrowser browser, String title) {
        final HBox box = tabMap.get(browser);
        final Label tabTitle = (Label) box.getChildren().get(0);
        tabTitle.setText(title);
    }

    public void setCurrentTab(CefBrowser currentBrowser) {
        final HBox currentBrowserBox = tabMap.get(currentBrowser);
        currentBrowserBox.setBackground(Background.fill(new BackgroundFill(Color.web("#F0EEE1"), "25px", "10px")));
    }
}
