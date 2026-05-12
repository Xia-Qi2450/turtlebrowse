package dev.ingstudios.turtlebrowse.tools.specs;

import dev.ingstudios.turtlebrowse.components.MainWindow;
import dev.ingstudios.turtlebrowse.tools.SnapshotImageTool;
import io.github.ollama4j.tools.Tools;

public class SnapshotImageToolSpec {
	private final MainWindow parent;
	private final SnapshotImageTool snapshotImageTool;

	public SnapshotImageToolSpec(MainWindow parent) {
		this.parent = parent;
		snapshotImageTool = new SnapshotImageTool(parent);
	}

	public Tools.Tool getSpecification() {
		return Tools.Tool.builder().toolSpec(Tools.ToolSpec.builder()
				.name("get_page_screenshot")
				.description("Gets a screenshot of the current page the user is on.")
				.build()).toolFunction(args -> {
					try {
						parent.ollamaSession.pageScreenshot = snapshotImageTool.takeSnapshotImage().get();
						return "Successfully took a screenshot of the current page, attaching on next prompt.";
					} catch (Exception e) {
						return "Error: " + e.getMessage();
					}
				}).build();
	}
}
