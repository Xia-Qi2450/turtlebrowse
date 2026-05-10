package dev.ingstudios.turtlebrowse.handlers;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.callback.CefStringVisitor;
import org.cef.handler.CefContextMenuHandlerAdapter;

import dev.ingstudios.turtlebrowse.components.MainWindow;

public class TurtlebrowseContextMenuHandler extends CefContextMenuHandlerAdapter {
	private static final int ID_SUMMARIZE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 1;
	private static final int ID_REWRITE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 2;
	private static final int ID_SUMMARIZE_PAGE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 3;
	private static final int ID_DEVTOOLS = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 4;
	private final MainWindow parent;

	public TurtlebrowseContextMenuHandler(MainWindow parent) {
		this.parent = parent;
	}

	@Override
	public void onBeforeContextMenu(CefBrowser browser, CefFrame frame, CefContextMenuParams params,
			CefMenuModel model) {
		final String selectedText = params.getSelectionText();
		final boolean hasText = selectedText != null && !selectedText.isEmpty();

		if (hasText) {
			model.addItem(ID_SUMMARIZE, "Summarize with AI");
		}

		if (hasText && params.isEditable()) {
			model.addItem(ID_REWRITE, "Rewrite with AI");
		}

		model.addItem(ID_SUMMARIZE_PAGE, "Summarize page with AI");
		model.addItem(ID_DEVTOOLS, "Open DevTools");
	}

	@Override
	public boolean onContextMenuCommand(CefBrowser browser, CefFrame frame, CefContextMenuParams params, int commandId,
			int eventFlags) {
		final String selectedText = params.getSelectionText();

		if (commandId == ID_SUMMARIZE) {
			System.out.printf("Selected: %s\n", selectedText);
			summarizeSelection(selectedText);
			return true;
		} else if (commandId == ID_REWRITE) {
			rewriteSelection(selectedText);
		} else if (commandId == ID_SUMMARIZE_PAGE) {
			summarizePage(browser);
		} else if (commandId == ID_DEVTOOLS) {
			browser.openDevTools();
		}

		return false;
	}

	private void summarizeSelection(String selection) {
		parent.aiSidebar.summarize(selection);
	}

	private void rewriteSelection(String selection) {
		parent.aiSidebar.rewrite(selection);
	}

	private void summarizePage(CefBrowser browser) {
		browser.getSource(new CefStringVisitor() {
			@Override
			public void visit(String html) {
				TurtlebrowseContextMenuHandler.this.parent.aiSidebar.summarizePage(html);
			}
		});
	}
}
