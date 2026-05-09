package dev.ingstudios.turtlebrowse.handlers;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLifeSpanHandlerAdapter;

import dev.ingstudios.turtlebrowse.components.MainWindow;

public class TurtlebrowseLifeSpanHandler extends CefLifeSpanHandlerAdapter {
	private final MainWindow parent;

	public TurtlebrowseLifeSpanHandler(MainWindow parent) {
		this.parent = parent;
	}

	@Override
	public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String targetUrl, String targetFrameName) {
		parent.createTab(targetUrl);
		return true;
	}

	@Override
	public void onBeforeClose(CefBrowser browser) {
		parent.loadHandler.removeBrowser(browser.getIdentifier());
	}
}
