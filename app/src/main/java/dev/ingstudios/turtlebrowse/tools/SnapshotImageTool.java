package dev.ingstudios.turtlebrowse.tools;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.ingstudios.turtlebrowse.components.MainWindow;

public class SnapshotImageTool {
	private final MainWindow parent;

	public SnapshotImageTool(MainWindow parent) {
		this.parent = parent;
	}

	public CompletableFuture<byte[]> takeSnapshotImage() {
		return parent.currentBrowser.getDevToolsClient().executeDevToolsMethod("Page.captureScreenshot", null)
				.thenApply(response -> {
					final JsonObject obj = JsonParser.parseString(response).getAsJsonObject();
					final String base64 = obj.get("data").getAsString();
					return Base64.getDecoder().decode(base64);
				});
	}
}
