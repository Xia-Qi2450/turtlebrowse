package dev.ingstudios.turtlebrowse.components;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import com.jfoenix.controls.JFXButton;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;

public class AISidebar extends JPanel {
	private final Component ui;
	private final java.awt.Dimension preferredDim = new java.awt.Dimension(0, 800);
	public boolean isOpen = false;

	public AISidebar(CefClient client, MainWindow parent, boolean useOsr, BooleanProperty isUiFocused) {
		this.setLayout(new java.awt.BorderLayout());
		this.setPreferredSize(preferredDim);

		final JFXPanel actionsBarJfxPanel = new JFXPanel();
		actionsBarJfxPanel.setFocusable(true);
		actionsBarJfxPanel.setPreferredSize(new java.awt.Dimension(preferredDim.width, 50));

		Platform.runLater(() -> {
			final HBox actionsBar = new HBox();
			actionsBar.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
			actionsBar.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.materialColorScheme.getSurface().get();
				return new Background(new BackgroundFill(backgroundColor, null, null));
			}, parent.materialColorScheme.getSurface()));
			actionsBar.setAlignment(Pos.CENTER_RIGHT);

			final Button closeButton = new JFXButton("X");
			closeButton.setGraphic(new FontIcon(Material2OutlinedAL.CLOSE));
			closeButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			closeButton.setStyle("-fx-padding: 10px;");
			closeButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.materialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.materialColorScheme.getSurfaceContainer()));
			closeButton.setOnMouseEntered(event -> {
				closeButton.setCursor(Cursor.HAND);
			});
			closeButton.setOnMouseDragExited(event -> {
				closeButton.setCursor(Cursor.DEFAULT);
			});
			closeButton.setOnAction(event -> {
				closeSidebar();
			});

			actionsBar.getChildren().addAll(closeButton);

			final Scene actionsBarScene = new Scene(actionsBar);
			actionsBarJfxPanel.setScene(actionsBarScene);
			actionsBar.prefWidthProperty().bind(actionsBarScene.widthProperty());
			actionsBar.prefHeightProperty().bind(actionsBarScene.heightProperty());
		});

		final CefBrowser aiBrowser = client.createBrowser("turtlebrowse://chat", useOsr, false);
		final Component browserComponent = aiBrowser.getUIComponent();
		ui = browserComponent;

		if (browserComponent.getMouseListeners().length == 0) {
			browserComponent.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent event) {
					SwingUtilities.invokeLater(() -> {
						isUiFocused.set(false);
						browserComponent.requestFocusInWindow();
						aiBrowser.setFocus(true);
					});
				}
			});
		}

		this.add(actionsBarJfxPanel, BorderLayout.NORTH);
		this.add(browserComponent, BorderLayout.CENTER);
	}

	public void toggleSidebar() {
		if (isOpen) {
			closeSidebar();
		} else {
			openSidebar();
		}
	}

	public void openSidebar() {
		ui.setVisible(true);
		preferredDim.width = 500;
		isOpen = true;
	}

	public void closeSidebar() {
		preferredDim.width = 0;
		ui.setVisible(false);
		isOpen = false;
	}
}
