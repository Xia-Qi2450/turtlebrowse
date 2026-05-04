package dev.ingstudios.turtlebrowse;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefContextMenuHandlerAdapter;

public class TurtlebrowseContextMenuHandler extends CefContextMenuHandlerAdapter {
    private static final int ID_SUMMARIZE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 1;
    private static final int ID_REWRITE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 2;

    @Override
    public void onBeforeContextMenu(CefBrowser browser, CefFrame frame, CefContextMenuParams params, CefMenuModel model) {
        final String selectedText = params.getSelectionText();
        final boolean hasText = selectedText != null && !selectedText.isEmpty();

        if (hasText) {
            model.addItem(ID_SUMMARIZE, "Summarize with AI");
        }
        
        if (hasText && params.isEditable()) {
            model.addItem(ID_REWRITE, "Rewrite with AI");
        }
    }
}
