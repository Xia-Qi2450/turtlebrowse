package dev.ingstudios.turtlebrowse.tools;

import java.util.concurrent.CompletableFuture;

import dev.ingstudios.turtlebrowse.components.MainWindow;

public class SnapshotTool {
	private final MainWindow parent;

	public SnapshotTool(MainWindow parent) {
		this.parent = parent;
	}

	public CompletableFuture<String> takeSnapshot() {
		return parent.currentBrowser.getDevToolsClient().executeDevToolsMethod("Accessibility.getFullAXTree", null)
				.thenApply(response -> {
					System.out.printf("Snapshot response: %s\n", response);
					return response;
				});
	}

	private String formatSnapshot() {

	}
}
