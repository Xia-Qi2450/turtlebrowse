package dev.ingstudios.turtlebrowse;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Paint;

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
    private boolean addressFieldFocused = false;

    public AddressBar(CefClient client, MainWindow parent, String startUrl) {
        this.parent = parent;

        this.setLayout(new java.awt.BorderLayout());

        JFXPanel addressBarPanel = new JFXPanel();
        addressBarPanel.setFocusable(true);
        addressBarPanel.setPreferredSize(new java.awt.Dimension(1200, 50));

        Platform.runLater(() -> {
            final HBox root = new HBox();
            root.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
            root.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
            root.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
                final Paint backgroundColor = this.parent.materialColorScheme.getSurface().get();
                return new Background(new BackgroundFill(backgroundColor, null, null));
            }, this.parent.materialColorScheme.getSurface()));
            root.setAlignment(Pos.CENTER);

            final Button backButton = new Button("<");
            backButton.setGraphic(new FontIcon(Material2OutlinedAL.ARROW_BACK));
            backButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            backButton.setStyle("-fx-padding: 10px;");
            backButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
                final Paint backgroundColor = this.parent.materialColorScheme.getSurfaceContainer().get();
                return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
            }, this.parent.materialColorScheme.getSurfaceContainer()));
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
            forwardButton.setStyle("-fx-padding: 10px;");
            forwardButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
                final Paint backgroundColor = this.parent.materialColorScheme.getSurfaceContainer().get();
                return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
            }, this.parent.materialColorScheme.getSurfaceContainer()));
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
            reloadButton.setStyle("-fx-padding: 10px;");
            reloadButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
                final Paint backgroundColor = this.parent.materialColorScheme.getSurfaceContainer().get();
                return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
            }, this.parent.materialColorScheme.getSurfaceContainer()));
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
            addressField.setStyle("-fx-padding: 10px;");
            addressField.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
                final Paint backgroundColor = this.parent.materialColorScheme.getSurfaceContainer().get();
                return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
            }, this.parent.materialColorScheme.getSurfaceContainer()));
            addressField.setOnAction(event -> {
                CefBrowser browser = this.parent.getBrowserInstance();

                String enteredUrl = this.parent.formatURL(addressField.getText(), false);

                System.out.print("Entered URL:");
                System.out.println(enteredUrl);

                if (browser != null) browser.loadURL(enteredUrl);
                else System.out.println("Browser is null.");
            });

            addressField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    System.out.println("Address field has lost focus.");
                    addressFieldFocused = false;
                    return;
                }
            });

            addressField.setOnMouseClicked(event -> {
                this.parent.isUiFocused.set(true);

                if (addressFieldFocused) {
                    System.out.println("Address field already focused, not selecting all.");
                    return;
                }

                addressField.requestFocus();
                addressField.selectAll();

                SwingUtilities.invokeLater(() -> {
                    CefBrowser browser = this.parent.getBrowserInstance();
                    if (browser != null) browser.setFocus(false);
                });

                addressFieldFocused = true;
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
        addressField.setText(this.parent.formatURL(newUrl, false));
    }

    public void focusAddressField() {
        System.out.println("Focus address field called.");

        SwingUtilities.invokeLater(() -> {
            this.parent.isUiFocused.set(true);

            if (addressFieldFocused) {
                System.out.println("Address field already focused, not selecting all.");
                return;
            }

            addressField.requestFocus();
            addressField.selectAll();

            SwingUtilities.invokeLater(() -> {
                CefBrowser browser = this.parent.getBrowserInstance();
                if (browser != null) browser.setFocus(false);
            });

            addressFieldFocused = true;
        });
    }
}