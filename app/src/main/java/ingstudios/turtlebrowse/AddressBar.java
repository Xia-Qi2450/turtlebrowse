package ingstudios.turtlebrowse;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefClient;
import org.cef.browser.*;

public class AddressBar extends JPanel {
    private TextField addressField;
    private MainWindow parent;

    public AddressBar(CefClient client, MainWindow parent, String startUrl) {
        this.parent = parent;

        this.setLayout(new java.awt.BorderLayout());

        JFXPanel addressBarPanel = new JFXPanel();
        addressBarPanel.setPreferredSize(new java.awt.Dimension(1200, 50));

        Platform.runLater(() -> {
            HBox root = new HBox();
            root.setAlignment(Pos.CENTER);
            root.setSpacing(10);
            root.setPadding(new Insets(10));

            Button backButton = new Button("<");
            backButton.setOnAction(event -> {
                System.out.println("Back button clicked.");
                CefBrowser browser = this.parent.getBrowserInstance();
                if (browser.canGoBack()) browser.goBack();
            });

            Button forwardButton = new Button(">");
            forwardButton.setOnAction(event -> {
                System.out.println("Forward button clicked.");
                CefBrowser browser = this.parent.getBrowserInstance();
                if (browser.canGoForward()) browser.goForward();
            });

            Button reloadButton = new Button("↻");
            reloadButton.setOnAction(event -> {
                System.out.println("Reload button clicked.");
                CefBrowser browser = this.parent.getBrowserInstance();
                browser.reload();
            });

            addressField = new TextField(startUrl);
            addressField.setOnAction(event -> {
                CefBrowser browser = parent.getBrowserInstance();

                String enteredUrl = addressField.getText();

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
        addressField.setText(newUrl);
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
}
