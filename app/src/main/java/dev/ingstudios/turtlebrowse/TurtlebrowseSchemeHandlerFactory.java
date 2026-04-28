package dev.ingstudios.turtlebrowse;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;

public class TurtlebrowseSchemeHandlerFactory implements CefSchemeHandlerFactory {
    @Override
    public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {
        System.out.println("Factory create() called for: " + request.getURL());
        return new TurtlebrowseSchemeResourceHandler();
    }
}