package dev.ingstudios.turtlebrowse.tools;

import java.util.HashMap;
import java.util.Map;

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

	public String clickAndReturnTree(String nodeId) {
		final CefDevToolsClient devToolsClient = parent.currentBrowser.getDevToolsClient();

		System.out.printf("Node ID: %s\n", nodeId);

		try {
			final JsonArray boxModelContent = devToolsClient
					.executeDevToolsMethod("DOM.getBoxModel", "{\"nodeId\":" + nodeId + "}").thenApply(response -> {
						System.out.printf("Box model response: %s\n", response);

						final JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
						final JsonObject model = responseJson.get("model").getAsJsonObject();
						final JsonArray content = model.get("content").getAsJsonArray();

						return content;
					}).get();
			final String x = boxModelContent.get(0).getAsString();
			final String y = boxModelContent.get(1).getAsString();

			System.out.printf("X: %s\nY: %s\n", x, y);

			final String command = "Input.dispatchMouseEvent";

			final Map<String, String> params = new HashMap<>();
			params.put("type", "mousePressed");
			params.put("x", x);
			params.put("y", y);
			params.put("button", "left");
			params.put("clickCount", "1");

			devToolsClient.executeDevToolsMethod(command, params.toString());

			params.put("type", "mouseReleased");

			devToolsClient.executeDevToolsMethod(command, params.toString());

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
