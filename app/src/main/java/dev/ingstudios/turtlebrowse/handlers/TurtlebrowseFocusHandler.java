package dev.ingstudios.turtlebrowse.handlers;

import java.awt.KeyboardFocusManager;

import javax.swing.SwingUtilities;

import org.cef.browser.CefBrowser;
import org.cef.handler.CefFocusHandlerAdapter;

import dev.ingstudios.turtlebrowse.components.MainWindow;

public class TurtlebrowseFocusHandler extends CefFocusHandlerAdapter {
	private final MainWindow parent;

	public TurtlebrowseFocusHandler(MainWindow parent) {
		this.parent = parent;
	}

	@Override
	public void onGotFocus(CefBrowser browser) {
		if (parent.isUiFocused.get()) {
			browser.setFocus(false);
			return;
		}

		SwingUtilities.invokeLater(() -> {
			if (!parent.isUiFocused.get()) {
				KeyboardFocusManager.getCurrentKeyboardFocusManager().clearFocusOwner();
				browser.setFocus(true);
			} else {
				browser.setFocus(false);
			}
		});
	}

	@Override
	public void onTakeFocus(CefBrowser browser, boolean next) {
		if (!parent.isUiFocused.get()) {
			browser.setFocus(false);
			return;
		}

		parent.isUiFocused.set(false);
	}

	@Override
	public boolean onSetFocus(CefBrowser browser, FocusSource source) {
		if (parent.isUiFocused.get()) {
			System.out.println("Blocked browser focus attempt while UI is active.");
		}
		return false;
	}
}
