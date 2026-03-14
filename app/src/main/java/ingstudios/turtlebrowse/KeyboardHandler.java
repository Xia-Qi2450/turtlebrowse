package ingstudios.turtlebrowse;

import java.awt.event.KeyEvent;

import org.cef.browser.CefBrowser;
import org.cef.handler.CefKeyboardHandlerAdapter;
import org.cef.misc.BoolRef;
import org.cef.misc.EventFlags;

public class KeyboardHandler extends CefKeyboardHandlerAdapter {
    private MainWindow parent;
    private String startUrl;

    public KeyboardHandler(MainWindow parent, String startUrl) {
        System.out.println("New keyboard handler created.");
        this.parent = parent;
        this.startUrl = startUrl;
    }

    @Override
    public boolean onPreKeyEvent(CefBrowser browser, CefKeyEvent event, BoolRef isKeyboardShortcut) {
        System.out.println("Key pressed.");

        boolean ctrlPressed = (event.modifiers & EventFlags.EVENTFLAG_CONTROL_DOWN) != 0;
        System.out.printf("Ctrl pressed: %s", ctrlPressed);

        if (ctrlPressed && event.windows_key_code == KeyEvent.VK_T) {
            System.out.println("Ctrl + T pressed.");
            parent.createTab(startUrl);
            return true;
        }

        return false;
    }
}
