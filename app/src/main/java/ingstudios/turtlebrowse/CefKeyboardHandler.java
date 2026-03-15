package ingstudios.turtlebrowse;

import java.awt.event.KeyEvent;

import org.cef.browser.CefBrowser;
import org.cef.handler.CefKeyboardHandlerAdapter;
import org.cef.misc.EventFlags;

import javafx.application.Platform;

public class CefKeyboardHandler extends CefKeyboardHandlerAdapter {
    private MainWindow parent;
    private String startUrl;

    public CefKeyboardHandler(MainWindow parent, String startUrl) {
        System.out.println("New keyboard handler created.");
        this.parent = parent;
        this.startUrl = startUrl;
    }

    @Override
    public boolean onKeyEvent(CefBrowser browser, CefKeyEvent event) {
        System.out.println("Key pressed.");

        if (event.type == CefKeyEvent.EventType.KEYEVENT_RAWKEYDOWN) {
            boolean ctrlPressed = (event.modifiers & EventFlags.EVENTFLAG_CONTROL_DOWN) != 0;
            boolean shiftPressed = (event.modifiers & EventFlags.EVENTFLAG_SHIFT_DOWN) != 0;
            System.out.printf("Ctrl pressed: %s", ctrlPressed);

            if (ctrlPressed && event.windows_key_code == KeyEvent.VK_T) {
                System.out.println("Ctrl + T pressed.");
                Platform.runLater(() -> parent.createTab(startUrl));
                return true;
            } else if (ctrlPressed && shiftPressed && event.windows_key_code == KeyEvent.VK_I) {
                parent.createDevTools();
                return true;
            }
        }

        return false;
    }
}
