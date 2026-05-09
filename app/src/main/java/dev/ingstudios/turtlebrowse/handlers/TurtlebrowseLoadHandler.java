package dev.ingstudios.turtlebrowse.handlers;

import java.util.ArrayList;
import java.util.List;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;

public class TurtlebrowseLoadHandler extends CefLoadHandlerAdapter {
	private final List<JSQueueItem> queueStack = new java.util.concurrent.CopyOnWriteArrayList<>();
	private final List<Integer> readyBrowsers = new ArrayList<>();

	@Override
	public void onLoadEnd(CefBrowser browser, CefFrame frame, int statusCode) {
		readyBrowsers.add(browser.getIdentifier());
		final List<JSQueueItem> toRemove = new ArrayList<>();
		for (JSQueueItem item : queueStack) {
			System.out.printf("Item: %s\n", item.code);
			if (item.isSame(browser.getIdentifier())) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				browser.getMainFrame().executeJavaScript(item.code, item.url, 0);
				toRemove.add(item);
			}
		}
		queueStack.removeAll(toRemove);
		browser.onBeforeClose();
	}

	public void addToQueueStack(JSQueueItem item) {
		queueStack.add(item);
	}

	public boolean isBrowserReady(Integer id) {
		return readyBrowsers.contains(id);
	}

	public void removeBrowser(Integer id) {
		readyBrowsers.remove(id);
	}

	public record JSQueueItem(int identifier, String code, String url) {
		public boolean isSame(int id) {
			return id == identifier;
		}
	}
}
