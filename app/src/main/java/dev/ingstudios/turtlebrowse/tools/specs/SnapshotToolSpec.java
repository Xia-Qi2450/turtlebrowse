package dev.ingstudios.turtlebrowse.tools.specs;

import dev.ingstudios.turtlebrowse.components.MainWindow;
import dev.ingstudios.turtlebrowse.tools.SnapshotTool;
import io.github.ollama4j.tools.Tools;

public class SnapshotToolSpec {
	private final MainWindow parent;
	private final SnapshotTool snapshotTool;

	public SnapshotToolSpec(MainWindow parent) {
		this.parent = parent;
		snapshotTool = new SnapshotTool(parent);
	}

	public Tools.Tool getSpecification() {
		return Tools.Tool.builder().toolSpec(Tools.ToolSpec.builder()
				.name("get_dom_snapshot")
				.description(
						"Gets a snapshot of the DOM nodes on the current page the user is on. Use this tool if the user requests anything on the current page, or you need context of the current page to do actions such as navigation, summarization, and more. Use this when the user does not specify which page in their prompt as well.")
				.build()).toolFunction(args -> {
					try {
						final String url = parent.currentBrowser.getURL();
						System.out.println("Taking snapshot...");
						final String snapshot = snapshotTool.takeSnapshot().get();
						System.out.printf("Snapshot: %s\n", snapshot);
						return "Use this YAML snapshot of the DOM to understand the page (" + url + ") better: '"
								+ snapshot + "'\nThe user's original prompt was: '" + parent.ollamaSession.latestMessage
								+ "'. Use this new data to fufill the user's request.";
					} catch (Exception e) {
						return "Error: " + e.getMessage();
					}
				}).build();
	}
}
