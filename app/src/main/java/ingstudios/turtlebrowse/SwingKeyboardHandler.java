package ingstudios.turtlebrowse;

import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;

import javafx.application.Platform;

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

                        if (keyCode == KeyEvent.VK_T && keyEvent.isControlDown()) {
                            System.out.println("Ctrl + T detected, creating a new tab.");
                            Platform.runLater(() -> parent.createTab(startUrl));
                            keyEvent.consume();
                        } else if (keyCode == KeyEvent.VK_I && keyEvent.isControlDown() && keyEvent.isShiftDown()) {
                            parent.createDevTools();
                            keyEvent.consume();
                        }
                    }
                }
            }
        }, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
    }
}