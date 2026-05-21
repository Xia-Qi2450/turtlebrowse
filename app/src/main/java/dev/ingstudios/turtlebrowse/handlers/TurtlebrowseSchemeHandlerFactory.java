package dev.ingstudios.turtlebrowse.handlers;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;

import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class TurtlebrowseSchemeHandlerFactory implements CefSchemeHandlerFactory {
    private MainWindow parent;

    public TurtlebrowseSchemeHandlerFactory(MainWindow parent) {
        this.parent = parent;
    }

    @Override
    public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {
        final String url = request.getURL();

        if (url.startsWith("turtlebrowse://api/prompt-stream")) {
            String prompt = "";
            String query = url.contains("?") ? url.substring(url.indexOf('?') + 1) : "";
            for (final String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length == 2 && pair[0].equals("prompt")) {
                    prompt = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                }
            }
            return new StreamingSchemeResourceHandler(prompt, parent);
        }

        return new TurtlebrowseSchemeResourceHandler(parent);
    }
}
