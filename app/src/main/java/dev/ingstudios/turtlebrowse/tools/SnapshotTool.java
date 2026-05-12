package dev.ingstudios.turtlebrowse.tools;

import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.ingstudios.turtlebrowse.components.MainWindow;

public class SnapshotTool {
	private final MainWindow parent;

	public SnapshotTool(MainWindow parent) {
		this.parent = parent;
	}

	public CompletableFuture<String> takeSnapshot() {
		return parent.currentBrowser.getDevToolsClient().executeDevToolsMethod("Accessibility.getFullAXTree", null)
				.thenApply(response -> {
					final String yaml = formatSnapshot(response);
					System.out.printf("Snapshot response: %s\n", yaml);
					return yaml;
				});
	}

	private String formatSnapshot(String json) {
		final JsonObject root = JsonParser.parseString(json).getAsJsonObject();
		final JsonArray nodes = root.get("nodes").getAsJsonArray();

		final java.util.Map<String, JsonObject> nodeMap = new java.util.LinkedHashMap<>();

		for (final JsonElement el : nodes) {
			final JsonObject node = el.getAsJsonObject();
			nodeMap.put(node.get("nodeId").getAsString(), node);
		}

		String rootId = null;
		for (java.util.Map.Entry<String, JsonObject> entry : nodeMap.entrySet()) {
			JsonObject node = entry.getValue();
			if (node.has("role") && node.getAsJsonObject("role").get("value").getAsString().equals("RootWebArea")) {
				rootId = entry.getKey();
				break;
			}
		}
		if (rootId == null)
			rootId = nodeMap.keySet().iterator().next();

		final StringBuilder yamlOutput = new StringBuilder();
		writeNode(rootId, 0, nodeMap, yamlOutput);

		System.out.printf("YAML: %s\n", yamlOutput.toString());
		return yamlOutput.toString();
	}

	private void writeNode(String nodeId, int depth, java.util.Map<String, JsonObject> nodeMap, StringBuilder sb) {
		final JsonObject node = nodeMap.get(nodeId);
		if (node == null)
			return;

		final boolean ignored = node.has("ignored") && node.get("ignored").getAsBoolean();

		if (!ignored) {
			final String indent = "    ".repeat(depth);
			sb.append(indent).append("- nodeId:\"").append(nodeId).append("\"\n");

			if (node.has("role")) {
				sb.append(indent).append("    role: ")
						.append(node.getAsJsonObject("role").get("value").getAsString()).append("\n");
			}

			if (node.has("name")) {
				sb.append(indent).append("    name: ")
						.append(node.getAsJsonObject("name").get("value").getAsString()).append("\n");
			}

			if (node.has("properties")) {
				for (final JsonElement propEl : node.getAsJsonArray("properties")) {
					final JsonObject prop = propEl.getAsJsonObject();
					final String key = prop.get("name").getAsString();
					final JsonObject valObj = prop.get("value").getAsJsonObject();
					final JsonElement val = valObj.get("value");
					sb.append(indent).append("    ").append(key).append(": ").append(val).append("\n");
				}
			}

			if (node.has("childIds") && node.getAsJsonArray("childIds").size() > 0) {
				sb.append("\n");
			}
		}

		if (node.has("childIds")) {
			for (final JsonElement childId : node.getAsJsonArray("childIds")) {
				writeNode(childId.getAsString(), ignored ? depth : depth + 1, nodeMap, sb);
			}
		}
	}
}
