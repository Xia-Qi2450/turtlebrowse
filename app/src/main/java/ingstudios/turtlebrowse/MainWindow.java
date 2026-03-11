package ingstudios.turtlebrowse;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.CefApp.CefAppState;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefFocusHandlerAdapter;
import org.cef.handler.CefLifeSpanHandlerAdapter;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import me.friwi.jcefmaven.*;

public class MainWindow extends JFrame {
    private final String START_URL = "https://google.com";
    private final boolean USE_OSR = false;

    private CefApp cefApp;
    private CefClient cefClient;
    private CefMessageRouter cefMessageRouter;
    private CefSettings cefSettings;
    private CefBrowser currentBrowser;
    private ArrayList<CefBrowser> openedBrowserTabs = new ArrayList<>();
    private JPanel root;
    private JPanel browserContainer;
    private AddressBar addressBar;
    private TabBar tabBar;
    public final BooleanProperty isUiFocused = new SimpleBooleanProperty(false);

    public MainWindow() {
        super("Turtlebrowse");

        Platform.startup(() -> {});

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        final Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/logo_full_trans.png"));
        setIconImage(icon);

        root = new JPanel(new BorderLayout());
        setContentPane(root);

        setSize(1200, 800);
        setLocationRelativeTo(null);

        browserContainer = new JPanel(new BorderLayout());

        // Address bar
        addressBar = new AddressBar(cefClient, this, START_URL);

        // CEF browser setup
        try {
            cefApp = creatCefApp();
        } catch (RuntimeException error) {
            System.exit(1);
        }

        cefClient = cefApp.createClient();
        cefMessageRouter = CefMessageRouter.create();
        cefClient.addMessageRouter(cefMessageRouter);

        // Tab bar
        tabBar = new TabBar(cefClient, openedBrowserTabs, this);

        cefClient.addFocusHandler(new CefFocusHandlerAdapter() {
            @Override
            public void onGotFocus(CefBrowser browser) {
                if (isUiFocused.get()) {
                    browser.setFocus(false);
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    if (!isUiFocused.get()) {
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().clearFocusOwner();
                        browser.setFocus(true);
                    } else {
                        browser.setFocus(false);
                    }
                });
            }

            @Override
            public void onTakeFocus(CefBrowser browser, boolean next) {
                if (!isUiFocused.get()) {
                    browser.setFocus(false);
                    return;
                }
                
                isUiFocused.set(false);
            }
            
            @Override
            public boolean onSetFocus(CefBrowser browser, FocusSource source) {
                if (isUiFocused.get()) {
                    System.out.println("Blocked browser focus attempt while UI is active.");
                }
                return false;
            }
        });

        // Top panel (address + tab)
        final JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(tabBar, BorderLayout.NORTH);
        topPanel.add(addressBar, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(browserContainer, BorderLayout.CENTER);

        createTab(START_URL);

        cefClient.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onTitleChange(CefBrowser browser, String title) {
                Platform.runLater(() -> {
                    tabBar.setTabTitle(browser, title);
                });
                
                SwingUtilities.invokeLater(() -> {
                    updateWindowTitle(title);
                });
            }

            @Override
            public void onAddressChange(CefBrowser cefBrowser, CefFrame frame, String url) {
                System.out.print("Navigated to:");
                System.out.println(url);
                addressBar.updateUrl(url);
            }
        });

        cefClient.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String targetUrl, String targetFrameName) {
                SwingUtilities.invokeLater(() -> {
                    createTab(targetUrl);
                });

                return true;
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                CefApp.getInstance().dispose();
                dispose();
            }
        });
    }

    private CefApp creatCefApp() {
        CefAppBuilder builder = new CefAppBuilder();
        builder.addJcefArgs("--enable-media-stream", "--enable-gpu");

        cefSettings = builder.getCefSettings();

        cefSettings.windowless_rendering_enabled = USE_OSR;

        try {
            String cachePath = getCachePath();
            cefSettings.cache_path = cachePath;
        } catch (Exception error) {
            System.out.print("Error while getting cache path, defaulting: ");
            System.out.println(error);
        }

        builder.setAppHandler(new MavenCefAppHandlerAdapter() {
            @Override
            public void stateHasChanged(CefAppState state) {
                if (state == CefAppState.TERMINATED) System.exit(0);
            }
        });

        try {
            CefApp cefApp = builder.build();
            return cefApp;
        } catch (Exception error) {
            System.out.print("Error while building CEF app:");
            System.out.println(error);
            throw new RuntimeException("Error while building CEF app:", error);
        }
    }

    private String getCachePath() {
        Path cachePath;

        String appName = "Turtlebrowse";
        String cacheDir = "cef-cache";

        String userHome = System.getProperty("user.home");

        if (OS.isWindows()) {
            String localAppData = System.getenv("LOCALAPPDATA");
            cachePath = Paths.get(localAppData, "ingStudios", appName, cacheDir);
        } else if (OS.isLinux()) {
            String xdgDataHome = System.getenv("XDG_DATA_HOME");
            cachePath = Paths.get(xdgDataHome, "ingStudios", appName, cacheDir);
        } else if (OS.isMacintosh()) {
            cachePath = Paths.get(userHome, "Library", "Application Support", appName, cacheDir);
        } else {
            throw new RuntimeException("Unknown operating system");
        }

        return cachePath.toString();
    }

    public void updateWindowTitle(String pageTitle) {
        this.setTitle(pageTitle + " - Turtlebrowse");
    }

    private void createTab(String url) {
        CefBrowser browser = cefClient.createBrowser(url, USE_OSR, false);
        openedBrowserTabs.add(browser);

        Platform.runLater(() -> {
            tabBar.addTabToUI(browser);
        });

        SwingUtilities.invokeLater(() -> {
            showBrowser(browser);
        });
    }

    public void showBrowser(CefBrowser browser) {
        currentBrowser = browser;
        Component ui = browser.getUIComponent();

        ui.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent event) {
                isUiFocused.set(false);
                ui.requestFocus();
                browser.setFocus(true);
            }
        });
        
        browserContainer.removeAll();
        browserContainer.add(ui, BorderLayout.CENTER);
        
        browserContainer.revalidate();
        browserContainer.repaint();

        browser.setFocus(true);
    }

    public CefBrowser getBrowserInstance() {
        return currentBrowser;
    }

    @Override
    public void dispose() {
        System.out.println("Closing...");

        for (CefBrowser browser : openedBrowserTabs) {
            if (browser != null) browser.close(true);
        }

        if (cefApp != null) {
            cefApp.dispose();
        }

        super.dispose();

        System.out.println("Successfully closed browser.");
    }
}
