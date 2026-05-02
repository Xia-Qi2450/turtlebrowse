package dev.ingstudios.turtlebrowse;

import java.io.File;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefBeforeDownloadCallback;
import org.cef.callback.CefDownloadItem;
import org.cef.handler.CefDownloadHandlerAdapter;

import javafx.application.Platform;
import javafx.stage.FileChooser;

public class TurtlebrowseDownloadHandler extends CefDownloadHandlerAdapter {
    @Override
    public boolean onBeforeDownload(CefBrowser browser, CefDownloadItem downloadItem, String suggestedName, CefBeforeDownloadCallback callback) {
        Platform.runLater(() -> {
            final FileChooser chooser = new FileChooser();
            chooser.setTitle("Save File As...");
            chooser.setInitialFileName(suggestedName);
            
            final File selectedFile = chooser.showSaveDialog(null);

            if (selectedFile != null) {
                callback.Continue(selectedFile.getAbsolutePath(), false);
            }
        });

        return true;
    }
}
