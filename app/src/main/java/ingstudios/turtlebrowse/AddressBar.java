package ingstudios.turtlebrowse;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefClient;
import org.cef.browser.*;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

public class AddressBar extends JPanel {
    private TextField addressField;
    private MainWindow parent;

    public AddressBar(CefClient client, MainWindow parent, String startUrl) {
        this.parent = parent;

        this.setLayout(new java.awt.BorderLayout());

        JFXPanel addressBarPanel = new JFXPanel();
        addressBarPanel.setPreferredSize(new java.awt.Dimension(1200, 50));

        Platform.runLater(() -> {
            final HBox root = new HBox();
            root.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
            root.setStyle("-fx-spacing: 10px; -fx-padding: 10px; -fx-background-color: #FCFAED; -fx-fill: #1B1C14; -fx-text-fill: #1B1C14;");
            root.setAlignment(Pos.CENTER);

            final Button backButton = new Button("<");
            backButton.setGraphic(new FontIcon(Material2OutlinedAL.ARROW_BACK));
            backButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            backButton.setStyle("-fx-background-color: #F0EEE1; -fx-border-radius: 25px; -fx-background-radius: 25px; -fx-padding: 10px;");
            backButton.setOnMouseEntered(event -> {
                backButton.setCursor(Cursor.HAND);
            });
            backButton.setOnMouseDragExited(event -> {
                backButton.setCursor(Cursor.DEFAULT);
            });
            backButton.setOnAction(event -> {
                System.out.println("Back button clicked.");
                CefBrowser browser = this.parent.getBrowserInstance();
                if (browser.canGoBack()) browser.goBack();
            });

            final Button forwardButton = new Button(">");
            forwardButton.setGraphic(new FontIcon(Material2OutlinedAL.ARROW_FORWARD));
            forwardButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            forwardButton.setStyle("-fx-background-color: #F0EEE1; -fx-border-radius: 25px; -fx-background-radius: 25px; -fx-padding: 10px;");
            forwardButton.setOnMouseEntered(event -> {
                forwardButton.setCursor(Cursor.HAND);
            });
            forwardButton.setOnMouseDragExited(event -> {
                forwardButton.setCursor(Cursor.DEFAULT);
            });
            forwardButton.setOnAction(event -> {
                System.out.println("Forward button clicked.");
                CefBrowser browser = this.parent.getBrowserInstance();
                if (browser.canGoForward()) browser.goForward();
            });

            final Button reloadButton = new Button("↻");
            reloadButton.setGraphic(new FontIcon(Material2OutlinedMZ.REFRESH));
            reloadButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            reloadButton.setStyle("-fx-background-color: #F0EEE1; -fx-border-radius: 25px; -fx-background-radius: 25px; -fx-padding: 10px;");
            reloadButton.setOnMouseEntered(event -> {
                reloadButton.setCursor(Cursor.HAND);
            });
            reloadButton.setOnMouseDragExited(event -> {
                reloadButton.setCursor(Cursor.DEFAULT);
            });
            reloadButton.setOnAction(event -> {
                System.out.println("Reload button clicked.");
                CefBrowser browser = this.parent.getBrowserInstance();
                browser.reload();
            });

            addressField = new TextField(startUrl);
            addressField.setStyle("-fx-background-color: #F0EEE1; -fx-border-radius: 25px; -fx-background-radius: 25px; -fx-padding: 10px;");
            addressField.setOnAction(event -> {
                CefBrowser browser = parent.getBrowserInstance();

                String enteredUrl = formatURL(addressField.getText());

                System.out.print("Entered URL:");
                System.out.println(enteredUrl);

                if (browser != null) browser.loadURL(enteredUrl);
                else System.out.println("Browser is null.");
            });

            addressField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                parent.isUiFocused.set(newValue);
            });

            addressField.setOnMousePressed(event -> {
                parent.isUiFocused.set(true);
            });

            addressField.setOnMouseClicked(event -> {
                addressField.requestFocus();
                addressField.selectAll();

                SwingUtilities.invokeLater(() -> {
                    CefBrowser browser = this.parent.getBrowserInstance();
                    if (browser != null) browser.setFocus(false);
                });
            });

            root.getChildren().addAll(backButton, forwardButton, reloadButton, addressField);

            backButton.prefWidthProperty().bind(backButton.heightProperty());
            forwardButton.prefWidthProperty().bind(forwardButton.heightProperty());
            reloadButton.prefWidthProperty().bind(reloadButton.heightProperty());

            HBox.setHgrow(addressField, Priority.ALWAYS);
            addressField.setMaxWidth(Double.MAX_VALUE);

            root.setOnMouseClicked(event -> {
                addressField.requestFocus();
            });

            Scene addressBarScene = new Scene(root);
            addressBarPanel.setScene(addressBarScene);
            root.prefWidthProperty().bind(addressBarScene.widthProperty());
            root.prefHeightProperty().bind(addressBarScene.heightProperty());
        });

        this.add(addressBarPanel);
    }

    public void updateUrl(String newUrl) {
        addressField.setText(formatURL(newUrl));
    }

    public void focusAddressField() {
        addressField.requestFocus();
        addressField.selectAll();

        SwingUtilities.invokeLater(() -> {
            CefBrowser browser = parent.getBrowserInstance();
            if (browser != null) browser.setFocus(false);
            parent.isUiFocused.set(false);
        });
    }

    private String formatURL(String url) {
        if (url.contains(" ")) {
            String searchQuery = URLEncoder.encode(url, StandardCharsets.UTF_8);
            return parent.DEFAULT_SEARCH_PROVIDER + searchQuery;
        }

        return url;
    }
}
