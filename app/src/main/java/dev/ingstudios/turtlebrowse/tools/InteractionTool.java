package dev.ingstudios.turtlebrowse.tools;

import java.util.HashMap;
import java.util.Map;

import org.cef.browser.CefDevToolsClient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.ingstudios.turtlebrowse.components.MainWindow;

public class InteractionTool {
	private final MainWindow parent;

	public InteractionTool(MainWindow parent) {
		this.parent = parent;
	}

	public String clickElement(String backendNodeId) {
		final CefDevToolsClient devToolsClient = parent.currentBrowser.getDevToolsClient();

		System.out.printf("Backend node ID: %s\n", backendNodeId);

		try {
			final JsonObject resolveResult = devToolsClient
					.executeDevToolsMethod("DOM.getBoxModel", "{\"backendNodeId\":" + backendNodeId + "}")
					.thenApply(response -> JsonParser.parseString(response).getAsJsonObject())
					.get();

			final JsonArray content = resolveResult.get("model").getAsJsonObject().get("content").getAsJsonArray();
			final double x = (content.get(0).getAsDouble() + content.get(2).getAsDouble()) / 2;
			final double y = (content.get(1).getAsDouble() + content.get(5).getAsDouble()) / 2;

			final String[] types = { "mousePressed", "mouseReleased" };
			for (String type : types) {
				JsonObject p = new JsonObject();
				p.addProperty("type", type);
				p.addProperty("x", x);
				p.addProperty("y", y);
				p.addProperty("button", "left");
				p.addProperty("clickCount", 1);
				devToolsClient.executeDevToolsMethod("Input.dispatchMouseEvent", p.toString());
			}

			try {
				return "Successfully clicked the element. You may need to call get_dom_snapshot again to get the latest changes if needed.";
			} catch (Exception e) {
				e.printStackTrace();
				return "An unexpected error occurred while taking a snapshot.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "An unexpected error occurred while clicking the element.";
		}
	}

	public String typeElement(String backendNodeId, String input) {
		final CefDevToolsClient devToolsClient = parent.currentBrowser.getDevToolsClient();

		try {
			devToolsClient.executeDevToolsMethod("DOM.focus", "{\"backendNodeId\":" + backendNodeId + "}");
			Thread.sleep(500);
			Map<String, String> params = new HashMap<>();
			params.put("text", input);
			String jsonPayload = new Gson().toJson(params);
			devToolsClient.executeDevToolsMethod("Input.insertText", jsonPayload);
		} catch (Exception e) {
			e.printStackTrace();
			return "An unexpected error occurred while inputting in the element.";
		}

		return "Successfully typed '" + input + "' in the element.";
	}
}
