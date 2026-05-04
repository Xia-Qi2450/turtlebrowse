package dev.ingstudios.turtlebrowse;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;

import javafx.beans.property.BooleanProperty;

public class AISidebar extends JPanel {
    private CefBrowser browser;
    private Component ui;

    public AISidebar(CefClient client, MainWindow parent, boolean useOsr, BooleanProperty isUiFocused) {
        this.setLayout(new java.awt.BorderLayout());
        this.setPreferredSize(new java.awt.Dimension(300, 800));

        final CefBrowser aiBrowser = client.createBrowser("turtlebrowse://chat", useOsr, false);
        browser = aiBrowser;
        final Component browserComponent = aiBrowser.getUIComponent();
        ui = browserComponent;

        if (browserComponent.getMouseListeners().length == 0) {
            browserComponent.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent event) {
                    SwingUtilities.invokeLater(() -> {
                        isUiFocused.set(false);
                        browserComponent.requestFocusInWindow();
                        aiBrowser.setFocus(true);
                    });
                }
            });
        }

        this.add(browserComponent);
    }

    public void closeSidebar() {
        if (browser == null || ui == null) return;
        this.remove(ui);
        browser.close(true);
    }
}
