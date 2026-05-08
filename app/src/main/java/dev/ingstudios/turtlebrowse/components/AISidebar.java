package dev.ingstudios.turtlebrowse.components;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

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
	private final int MAX_WIDTH = 500;
	private final float EASING_FACTOR = 0.15f;

	private final CefBrowser browser;
	private final Component ui;
	private Timer animationTimer;
	private int targetWidth = 0;
	private float currentWidth = 0;
	private final java.awt.Dimension preferredDim = new java.awt.Dimension(0, 800);

	public AISidebar(CefClient client, MainWindow parent, boolean useOsr, BooleanProperty isUiFocused) {
		this.setLayout(new java.awt.BorderLayout());
		this.setPreferredSize(new java.awt.Dimension(MAX_WIDTH, 800));

		final JFXPanel actionsBarJfxPanel = new JFXPanel();
		actionsBarJfxPanel.setFocusable(true);
		actionsBarJfxPanel.setPreferredSize(new java.awt.Dimension(MAX_WIDTH, 50));

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
				toggleSidebar(false);
			});

			actionsBar.getChildren().addAll(closeButton);

			final Scene actionsBarScene = new Scene(actionsBar);
			actionsBarJfxPanel.setScene(actionsBarScene);
			actionsBar.prefWidthProperty().bind(actionsBarScene.widthProperty());
			actionsBar.prefHeightProperty().bind(actionsBarScene.heightProperty());
		});

		final CefBrowser aiBrowser = client.createBrowser("turtlebrowse://chat", useOsr, false);
		browser = aiBrowser;
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

		animationTimer = new Timer(10, e -> animate());
	}

	private void animate() {
		final float diff = targetWidth - currentWidth;

		if (Math.abs(diff) < 1.0f) {
			currentWidth = targetWidth;
			animationTimer.stop();
			if (currentWidth == 0) {
				ui.setVisible(false);
				browser.setFocus(false);
				this.setVisible(false);
			}
		} else {
			currentWidth += diff * EASING_FACTOR;
		}

		preferredDim.width = (int) currentWidth;
		this.setPreferredSize(preferredDim);
		this.revalidate();
	}

	public void toggleSidebar(boolean open) {
		targetWidth = open ? 500 : 0;

		if (open) {
			ui.setVisible(true);
			browser.setFocus(true);
			this.setVisible(true);
		}

		if (!animationTimer.isRunning()) {
			animationTimer.start();
		}
	}
}
