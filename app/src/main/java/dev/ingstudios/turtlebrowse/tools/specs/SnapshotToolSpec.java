package dev.ingstudios.turtlebrowse.tools.specs;

import dev.ingstudios.turtlebrowse.components.MainWindow;
import dev.ingstudios.turtlebrowse.tools.SnapshotTool;
import io.github.ollama4j.tools.Tools;

public class SnapshotToolSpec {
	private final SnapshotTool snapshotTool;

	public SnapshotToolSpec(MainWindow parent) {
		snapshotTool = new SnapshotTool(parent);
	}

	public Tools.Tool getSpecification() {
		return Tools.Tool.builder().toolSpec(Tools.ToolSpec.builder()
				.name("get_dom_snapshot")
				.description("Gets a snapshot of the DOM nodes on the current page the user is on.")
				.build()).toolFunction(args -> {
					try {
						System.out.println("Taking snapshot...");
						final String snapshot = snapshotTool.takeSnapshot().get();
						System.out.printf("Snapshot: %s\n", snapshot);
						return "Use this YAML snapshot of the DOM to understand the page better: " + snapshot;
					} catch (Exception e) {
						return "Error: " + e.getMessage();
					}
				}).build();
	}
}
