package dev.ingstudios.turtlebrowse.handlers;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefContextMenuHandlerAdapter;

public class TurtlebrowseContextMenuHandler extends CefContextMenuHandlerAdapter {
	private static final int ID_SUMMARIZE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 1;
	private static final int ID_REWRITE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 2;
	private static final int ID_SUMMARIZE_PAGE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 3;

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
	}

	@Override
	public boolean onContextMenuCommand(CefBrowser browser, CefFrame frame, CefContextMenuParams params, int commandId,
			int eventFlags) {
		final String selectedText = params.getSelectionText();

		if (commandId == ID_SUMMARIZE) {
			System.out.printf("Selected: %s\n", selectedText);
			return true;
		} else if (commandId == ID_REWRITE) {
			// TODO(developer): Add rewrite
		} else if (commandId == ID_SUMMARIZE_PAGE) {
			// TODO(developer): Add summarize page
		}

		return false;
	}
}
