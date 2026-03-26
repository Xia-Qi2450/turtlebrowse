package dev.ingstudios.turtlebrowse;

import java.nio.charset.StandardCharsets;

import org.cef.callback.CefCallback;
import org.cef.callback.CefResourceReadCallback;
import org.cef.callback.CefResourceSkipCallback;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.BoolRef;
import org.cef.misc.IntRef;
import org.cef.misc.LongRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

public class TurtlebrowseSchemeResourceHandler implements CefResourceHandler {
    private String content;
    private int offset;
    
    @Override
    public boolean processRequest(CefRequest request, CefCallback callback) {
        String url = request.getURL();
        System.out.println("Intercepted: " + url);

        switch (url) {
            case "turtlebrowse://newtab":
                content = "<html><body><h1>TurtleBrowse New Tab</h1></body></html>";
                break;
        }

        callback.Continue();
        return true;
    }

    @Override
    public void getResponseHeaders(CefResponse response, IntRef responseLength, StringRef redirectUrl) {
        response.setMimeType("text/html");
        response.setStatus(200);
        responseLength.set(content.length());
    }

    @Override
    public boolean readResponse(byte[] dataOut, int bytesToRead, 
        IntRef bytesRead, CefCallback callback) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        int maxRead = Math.min(bytes.length - offset, bytesToRead);
        
        System.arraycopy(bytes, offset, dataOut, 0, maxRead);
        offset += maxRead;
        
        bytesRead.set(maxRead);
        return maxRead > 0;
    }

    @Override
    public void cancel() {}

    @Override
    public boolean skip(long bytesToSkip, LongRef bytesSkipped, CefResourceSkipCallback callback) {
        return true;
    }

    @Override
    public boolean open(CefRequest request, BoolRef handleRequest, CefCallback callback) {
        return true;
    }

    @Override
    public boolean read(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefResourceReadCallback callback) {
        return true;
    }
}
