package dev.ingstudios.turtlebrowse.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseLoadHandler.JSQueueItem;
import dev.kreuzberg.htmltomarkdown.HtmlToMarkdown;

public class AISidebar extends JPanel {
	private final Component ui;
	private final java.awt.Dimension preferredDim = new java.awt.Dimension(0, 800);
	public boolean isOpen = false;
	private final CefBrowser aiBrowser;
	private final ScheduledExecutorService summarizeScheduler = Executors.newSingleThreadScheduledExecutor();
	private final ScheduledExecutorService rewriteScheduler = Executors.newSingleThreadScheduledExecutor();
	private final ScheduledExecutorService summarizePageScheduler = Executors.newSingleThreadScheduledExecutor();

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

		aiBrowser = client.createBrowser("turtlebrowse://chat", useOsr, false);
		parent.requestHandler.setAiBrowser(aiBrowser);
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
		System.out.println("Opening sidebar...");
		ui.setVisible(true);
		preferredDim.width = 500;
		isOpen = true;
		this.revalidate();
	}

	public void closeSidebar() {
		preferredDim.width = 0;
		ui.setVisible(false);
		isOpen = false;
		this.revalidate();
	}

	public void summarize(String text) {
		if (!isOpen)
			SwingUtilities.invokeLater(this::openSidebar);

		summarizeScheduler.schedule(() -> {
			try {
				final String jsonText = new ObjectMapper().writeValueAsString("Summarize this: " + text);
				final JSQueueItem item = new JSQueueItem(aiBrowser.getIdentifier(),
						"window.addPrompt(" + jsonText + ");", "turtlebrowse://chat");

				aiBrowser.getMainFrame().executeJavaScript(item.code(), item.url(), 0);
			} catch (JsonProcessingException e) {
				System.err.println(e.getMessage());
			}
		}, 500, TimeUnit.MILLISECONDS);
	}

	public void rewrite(String text) {
		if (!isOpen)
			SwingUtilities.invokeLater(this::openSidebar);

		rewriteScheduler.schedule(() -> {
			try {
				final String jsonText = new ObjectMapper().writeValueAsString("Rewrite \'" + text + "\' to be");
				final JSQueueItem item = new JSQueueItem(aiBrowser.getIdentifier(),
						"window.addPromptRewrite(" + jsonText + ");", "turtlebrowse://chat");

				aiBrowser.getMainFrame().executeJavaScript(item.code(), item.url(), 0);
			} catch (JsonProcessingException e) {
				System.err.println(e.getMessage());
			}
		}, 1000, TimeUnit.MILLISECONDS);
	}

	public void summarizePage(String html) {
		System.out.printf("Summarizing page...\n");

		if (!isOpen)
			SwingUtilities.invokeLater(this::openSidebar);

		summarizePageScheduler.schedule(() -> {
			try {
				if (html == null) {
					return;
				}
				final Document doc = Jsoup.parse(html);
				doc.select("script, style, svg, canvas, iframe, noscript, img, video, audio").remove();
				doc.select("*").forEach(Element::clearAttributes);
				final String cleanHtml = doc.body().html();
				System.out.printf("Clean HTML: %s\n", cleanHtml);
				System.out.println("Attempting to convert HTML to Markdown...");
				final String markdown = HtmlToMarkdown.convert(cleanHtml);
				System.out.printf("Converted Markdown: %s\n", markdown);
				final String jsonText = new ObjectMapper().writeValueAsString("Summarize this page: " + markdown);
				final JSQueueItem item = new JSQueueItem(aiBrowser.getIdentifier(),
						"window.addPrompt(" + jsonText + ");", "turtlebrowse://chat");

				aiBrowser.getMainFrame().executeJavaScript(item.code(), item.url(), 0);
			} catch (JsonProcessingException e) {
				System.err.println(e.getMessage());
			}
		}, 500, TimeUnit.MILLISECONDS);
	}
}
