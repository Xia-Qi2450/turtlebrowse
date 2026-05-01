package dev.ingstudios.turtlebrowse;

import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.AWTEvent;

public class SwingKeyboardHandler {
    public SwingKeyboardHandler(MainWindow parent, String startUrl) {
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof KeyEvent) {
                    KeyEvent keyEvent = (KeyEvent) event;
                    if (keyEvent.getID() == KeyEvent.KEY_PRESSED) {
                        int keyCode = keyEvent.getKeyCode();
                        System.out.println("Global key pressed: " + KeyEvent.getKeyText(keyCode));

                        if (keyCode == KeyEvent.VK_I && keyEvent.isControlDown() && keyEvent.isShiftDown()) { // DevTools (Ctrl + Shift + I)
                            parent.createDevTools();
                            keyEvent.consume();
                        } else if (keyCode == KeyEvent.VK_T && keyEvent.isControlDown()) { // New tab (Ctrl + T)
                            System.out.println("Ctrl + T detected, creating a new tab.");
                            parent.createTab(startUrl);
                            keyEvent.consume();
                        } else if (keyCode == KeyEvent.VK_L && keyEvent.isControlDown()) { // Focus address field (Ctrl + L)
                            System.out.println("Ctrl + L pressed.");
                            parent.addressBar.focusAddressField();
                            keyEvent.consume();
                        } else if (keyCode == KeyEvent.VK_LEFT && keyEvent.isAltDown()) { // Navigates back (Alt + <)
                            if (parent.currentBrowser.canGoBack()) parent.currentBrowser.goBack();
                            keyEvent.consume();
                        } else if (keyCode == KeyEvent.VK_RIGHT && keyEvent.isAltDown()) { // Navigates forward (Alt + >)
                            if (parent.currentBrowser.canGoForward()) parent.currentBrowser.goForward();
                            keyEvent.consume();
                        }
                    }
                }
            }
        }, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
    }
}