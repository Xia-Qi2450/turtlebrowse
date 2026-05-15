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

		System.out.println("Before JSON element iteration...");

		for (final JsonElement el : nodes) {
			final JsonObject node = el.getAsJsonObject();
			if (!node.has("backendDOMNodeId"))
				continue;
			nodeMap.put(node.get("backendDOMNodeId").getAsString(), node);
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

	private static final java.util.Set<String> IGNORED_ROLES = java.util.Set.of(
			"InlineTextBox", "StaticText", "LineBreak", "none", "generic");

	private void writeNode(String backendNodeId, int depth, java.util.Map<String, JsonObject> nodeMap,
			StringBuilder sb) {
		final JsonObject node = nodeMap.get(backendNodeId);
		if (node == null)
			return;

		final boolean ignored = node.has("ignored") && node.get("ignored").getAsBoolean();

		String role = "";
		if (node.has("role")) {
			role = node.getAsJsonObject("role").get("value").getAsString();
		}

		final boolean skip = ignored || IGNORED_ROLES.contains(role);

		if (!skip) {
			final String indent = "    ".repeat(depth);
			sb.append(indent).append("- backendNodeId:\"").append(backendNodeId).append("\"\n");
			sb.append(indent).append("    role: ").append(role).append("\n");

			if (node.has("name")) {
				final String name = node.getAsJsonObject("name").get("value").getAsString();
				if (!name.isBlank()) {
					sb.append(indent).append("    name: ").append(name).append("\n");
				}
			}

			if (node.has("value")) {
				final String val = node.getAsJsonObject("value").get("value").getAsString();
				if (!val.isBlank()) {
					sb.append(indent).append("    value: ").append(val).append("\n");
				}
			}

			if (node.has("properties")) {
				for (final JsonElement propEl : node.getAsJsonArray("properties")) {
					final JsonObject prop = propEl.getAsJsonObject();
					final String key = prop.get("name").getAsString();
					if (!java.util.Set
							.of("focusable", "focused", "disabled", "checked", "expanded", "required", "invalid")
							.contains(key))
						continue;
					final JsonObject valObj = prop.get("value").getAsJsonObject();
					final JsonElement val = valObj.get("value");
					sb.append(indent).append("    ").append(key).append(": ").append(val).append("\n");
				}
			}

			if (node.has("description")) {
				final String desc = node.getAsJsonObject("description").get("value").getAsString();
				if (!desc.isBlank()) {
					sb.append(indent).append("    description: ").append(desc).append("\n");
				}
			}
		}

		if (node.has("childIds")) {
			for (final JsonElement childId : node.getAsJsonArray("childIds")) {
				writeNode(childId.getAsString(), skip ? depth : depth + 1, nodeMap, sb);
			}
		}
	}
}
