package dev.ingstudios.turtlebrowse;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import com.google.gson.Gson;

public class TurtlebrowseMessageRouter extends CefMessageRouterHandlerAdapter {
    private final Gson gson = new Gson();
    private MainWindow parent;

    public TurtlebrowseMessageRouter(MainWindow parent) {
        this.parent = parent;
        System.out.println("Successfully registered message router.");
    }

    @Override
    public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
        @SuppressWarnings("null")
        BridgeRequest data = gson.fromJson(request, BridgeRequest.class);

        if ("GET_NAME".equals(data.request)) {
            System.out.println("Client called GET_NAME");
            callback.success("Ethan Lee");
            return true;
        } else if ("SEARCH_WEB".equals(data.request)) {
            parent.searchWeb((String) data.params.get("query"));
            callback.success("Successfully searched web.");
            return true;
        }

        return false;
    }
}
