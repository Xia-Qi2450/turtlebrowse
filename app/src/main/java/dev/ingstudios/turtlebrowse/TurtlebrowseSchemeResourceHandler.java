package dev.ingstudios.turtlebrowse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

import org.cef.callback.CefCallback;
import org.cef.callback.CefResourceReadCallback;
import org.cef.callback.CefResourceSkipCallback;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.BoolRef;
import org.cef.misc.IntRef;
import org.cef.misc.LongRef;
import org.cef.misc.StringRef;
import org.cef.network.CefPostData;
import org.cef.network.CefPostDataElement;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

public class TurtlebrowseSchemeResourceHandler implements CefResourceHandler {
    private byte[] data;
    private int offset = 0;
    private String mimeType = "text/html";
    private MainWindow parent;

    public TurtlebrowseSchemeResourceHandler(MainWindow parent) {
        this.parent = parent;
    }

    private void loadResource(String resourcePath) {
        try (var inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                this.data = inputStream.readAllBytes();
                
                if (resourcePath.endsWith(".js")) mimeType = "application/javascript";
                else if (resourcePath.endsWith(".css")) mimeType = "text/css";
                else if (resourcePath.endsWith(".svg")) mimeType = "image/svg+xml";
                else if (resourcePath.endsWith(".png")) mimeType = "image/png";
                else if (resourcePath.endsWith(".jpeg") || resourcePath.endsWith(".jpg")) mimeType = "image/jpeg";
                else mimeType = "text/html";
            } else {
                this.data = "<html><body>404 Resource Not Found</body></html>".getBytes(StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean open(CefRequest request, BoolRef handleRequest, CefCallback callback) {
        final String url = request.getURL();
        System.out.println("Scheme handler open() called with URL: " + url);

        if (url.startsWith("turtlebrowse://newtab")) {
            final String path = url.substring("turtlebrowse://newtab".length());
            System.out.println("Parsed path: '" + path + "'");

            if (path.isEmpty() || path.equals("/")) {
                loadResource("/web/newtab.html");
            } else {
                loadResource("/web" + path);
            }

            System.out.println("Data loaded, length: " + (data != null ? data.length : "NULL"));
            System.out.println("MIME type: " + mimeType);

            handleRequest.set(true);
            callback.Continue();
            return true;
        } else if (url.startsWith("turtlebrowse://api")) {
            final String action = url.replace("turtlebrowse://api/", "");

            System.out.printf("Action: %s URL: %s", action, url);

            final CefPostData postData = request.getPostData();
            String body = "{}";

            if (postData != null) {
                final Vector<CefPostDataElement> elements = new Vector<>();
                postData.getElements(elements);
                if (elements != null && elements.size() > 0) {
                    final CefPostDataElement element = elements.firstElement();
                    final int elementSize = (int) element.getBytesCount();
                    final byte[] buffer = new byte[elementSize];
                    element.getBytes(elementSize, buffer);
                    body = new String(buffer, StandardCharsets.UTF_8);
                }
            }

            String result = parent.handleApiFromClient(action, body);
            this.data = result.getBytes(StandardCharsets.UTF_8);
            this.mimeType = "application/json";
            handleRequest.set(true);
            callback.Continue();
            return true;
        }

        return false;
    }

    @Override
    public void getResponseHeaders(CefResponse response, IntRef responseLength, StringRef redirectUrl) {
        if (data == null) {
            data = "<html><body>500 Internal Error</body></html>".getBytes(StandardCharsets.UTF_8);
            mimeType = "text/html";
            response.setStatus(500);
        } else {
            response.setStatus(200);
        }
        response.setMimeType(mimeType);
        response.setHeaderByName("Access-Control-Allow-Origin", "*", true);
        response.setHeaderByName("Access-Control-Allow-Methods", "GET, OPTIONS", true);
        response.setHeaderByName("Access-Control-Allow-Headers", "*", true);
        responseLength.set(data.length);
    }

    @Override
    public boolean read(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefResourceReadCallback callback) {
        if (offset >= data.length) {
            bytesRead.set(0);
            return false;
        }

        int available = data.length - offset;
        int toCopy = Math.min(available, bytesToRead);

        System.arraycopy(data, offset, dataOut, 0, toCopy);
        offset += toCopy;

        bytesRead.set(toCopy);
        return true;
    }

    @Override
    public void cancel() {
        offset = 0;
    }

    @Override
    public boolean processRequest(CefRequest request, CefCallback callback) { return false; }
    @Override
    public boolean readResponse(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefCallback callback) { return false; }
    @Override
    public boolean skip(long bytesToSkip, LongRef bytesSkipped, CefResourceSkipCallback callback) { return false; }
}
