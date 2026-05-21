package dev.ingstudios.turtlebrowse.tools;

import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class NavigateSiteTool {
	final MainWindow parent;

	public NavigateSiteTool(MainWindow parent) {
		this.parent = parent;
	}

	public void navigateTo(String url) {
		parent.currentBrowser.loadURL(url);
	}
}
