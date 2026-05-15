package dev.ingstudios.turtlebrowse.tools;

import org.cef.browser.CefDevToolsClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.ingstudios.turtlebrowse.components.MainWindow;

public class InteractionTool {
	private final MainWindow parent;
	private final SnapshotTool snapshotTool;

	public InteractionTool(MainWindow parent) {
		this.parent = parent;
		snapshotTool = new SnapshotTool(parent);
	}

	public String clickAndReturnTree(String backendNodeId) {
		final CefDevToolsClient devToolsClient = parent.currentBrowser.getDevToolsClient();

		System.out.printf("Backend node ID: %s\n", backendNodeId);

		try {
			final JsonObject boxModel = devToolsClient
					.executeDevToolsMethod("DOM.getBoxModel", "{\"backendNodeId\":" + backendNodeId + "}")
					.thenApply(response -> {
						System.out.printf("Box model response: %s\n", response);

						final JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
						final JsonObject model = responseJson.get("model").getAsJsonObject();

						return model;
					}).get();
			final JsonArray content = boxModel.get("content").getAsJsonArray();

			final float width = boxModel.get("width").getAsFloat();
			final float heignt = boxModel.get("height").getAsFloat();

			final float x = Float.parseFloat(content.get(0).getAsString()) + (width / 2);
			final float y = Float.parseFloat(content.get(1).getAsString()) + (heignt / 2);

			System.out.printf("X: %f\nY: %f\nWidth: %f\nHeight:%f\n", x, y, width, heignt);

			final String clickScript = """
					const element = document.elementFromPoint(%s, %s);
					element.click();
										""".formatted(String.valueOf(x), String.valueOf(y));

			parent.currentBrowser.executeJavaScript(clickScript, parent.currentBrowser.getURL(), 0);

			try {
				Thread.sleep(500);
				final String snapshot = snapshotTool.takeSnapshot().get();
				return snapshot;
			} catch (Exception e) {
				e.printStackTrace();
				return "An unexpected error occurred while taking a snapshot.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "An unexpected error occurred while clicking the element.";
		}
	}
}
