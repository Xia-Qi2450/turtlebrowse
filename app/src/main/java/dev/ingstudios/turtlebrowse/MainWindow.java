package dev.ingstudios.turtlebrowse;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import org.cef.callback.CefSchemeRegistrar;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefFocusHandlerAdapter;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.glavo.monetfx.ColorScheme;
import org.glavo.monetfx.beans.property.ColorSchemeProperty;
import org.glavo.monetfx.beans.property.SimpleColorSchemeProperty;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import me.friwi.jcefmaven.*;

public class MainWindow extends JFrame {
    public final String START_URL = "https://google.com";
    public final String DEFAULT_SEARCH_PROVIDER = "https://google.com/search?q=";
    private final boolean USE_OSR = false;

    private CefApp cefApp;
    private CefClient cefClient;
    private CefMessageRouter cefMessageRouter;
    private CefSettings cefSettings;
    private CefBrowser currentBrowser;
    private ArrayList<CefBrowser> openedBrowserTabs = new ArrayList<>();
    private JPanel root;
    private JPanel browserContainer;
    public AddressBar addressBar;
    private TabBar tabBar;
    private final Map<CefBrowser, String> titleMap = new HashMap<>();
    public final BooleanProperty isUiFocused = new SimpleBooleanProperty(false);
    public ColorSchemeProperty materialColorScheme = new SimpleColorSchemeProperty(ColorScheme.fromSeed(Color.web("#BDCF47")));

    public MainWindow() {
        super("Turtlebrowse");

        Platform.startup(() -> {});
        Platform.runLater(() -> {
            Font.loadFont(getClass().getResourceAsStream("/fonts/google_sans_flex.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/material_icons_outlined.otf"), 10);
        });

        System.out.println("AWT Toolkit: " + java.awt.Toolkit.getDefaultToolkit().getClass().getName());
        System.out.println("DISPLAY: " + System.getenv("DISPLAY"));
        System.out.println("WAYLAND_DISPLAY: " + System.getenv("WAYLAND_DISPLAY"));

        setMaterialColorSchemeFromSystem();

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
            cefApp = createCefApp();
        } catch (RuntimeException error) {
            System.exit(1);
        }

        cefClient = cefApp.createClient();
        CefMessageRouter.CefMessageRouterConfig config = new CefMessageRouter.CefMessageRouterConfig("cefQuery", "cefQueryCancel");
        cefMessageRouter = CefMessageRouter.create(config);
        cefMessageRouter.addHandler(new TurtlebrowseMessageRouter(this), true);
        cefClient.addMessageRouter(cefMessageRouter);

        // Tab bar
        tabBar = new TabBar(cefClient, openedBrowserTabs, this);

        // Keyboard handler (JCEF)
        cefClient.addKeyboardHandler(new CefKeyboardHandler(this, START_URL));

        // Keyboard handler (Swing)
        new SwingKeyboardHandler(this, START_URL);

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

        SwingUtilities.invokeLater(() -> {
            createTab(START_URL);
            setVisible(true);
        });

        cefClient.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onTitleChange(CefBrowser browser, String title) {
                if (browser != currentBrowser) return;

                titleMap.put(browser, title);

                Platform.runLater(() -> {
                    tabBar.setTabTitle(browser, title);
                });
                
                SwingUtilities.invokeLater(() -> {
                    updateWindowTitle(title);
                });
            }

            @Override
            public void onAddressChange(CefBrowser cefBrowser, CefFrame frame, String url) {
                if (cefBrowser != currentBrowser) return;
                System.out.print("Navigated to:");
                System.out.println(url);
                Platform.runLater(() -> addressBar.updateUrl(url));
            }
        });

        cefClient.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String targetUrl, String targetFrameName) {
                createTab(targetUrl);
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

    private CefApp createCefApp() {
        CefAppBuilder builder = new CefAppBuilder();
        builder.addJcefArgs(
            "--enable-media-stream",
            "--ozone-platform=x11",
            "--disable-gpu",
            "--disable-gpu-compositing",
            "--disable-software-rasterizer"
        );

        cefSettings = builder.getCefSettings();
        builder.setInstallDir(getInstallDir());
        cefSettings.windowless_rendering_enabled = USE_OSR;
        cefSettings.remote_debugging_port = 6767;

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

            @Override
            public void onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
                registrar.addCustomScheme("turtlebrowse", true, false, false, false, false, false, false);
            }

            @Override
            public void onContextInitialized() {
                CefApp.getInstance().registerSchemeHandlerFactory("turtlebrowse", "", new TurtlebrowseSchemeHandlerFactory());
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

        final String appName = "Turtlebrowse";
        final String cacheDir = "cef-cache";

        final String userHome = System.getProperty("user.home");

        if (OS.isWindows()) {
            String localAppData = System.getenv("LOCALAPPDATA");
            cachePath = Paths.get(localAppData, "ingStudios", appName, cacheDir);
        } else if (OS.isLinux()) {
            String xdgDataHome = System.getenv("XDG_DATA_HOME");
            if (xdgDataHome == null || xdgDataHome.isEmpty()) {
                xdgDataHome = userHome + "/.local/share";
            }
            cachePath = Paths.get(xdgDataHome, "ingStudios", appName, cacheDir);
        } else if (OS.isMacintosh()) {
            cachePath = Paths.get(userHome, "Library", "Application Support", appName, cacheDir);
        } else {
            throw new RuntimeException("Unknown operating system");
        }

        return cachePath.toString();
    }

    private File getInstallDir() {
        Path installPath;

        final String appName = "Turtlebrowse";
        final String installDir = "cef-install";

        final String userHome = System.getProperty("user.home");

        if (OS.isWindows()) {
            String localAppData = System.getenv("LOCALAPPDATA");
            installPath = Paths.get(localAppData, "ingStudios", appName, installDir);
        } else if (OS.isLinux()) {
            String xdgDataHome = System.getenv("XDG_DATA_HOME");
            if (xdgDataHome == null || xdgDataHome.isEmpty()) {
                xdgDataHome = userHome + "/.local/share";
            }
            installPath = Paths.get(xdgDataHome, "ingStudios", appName, installDir);
        } else if (OS.isMacintosh()) {
            installPath = Paths.get(userHome, "Library", "Application Support", appName, installDir);
        } else {
            throw new RuntimeException("Unknown operating system");
        }

        final File installFile = installPath.toFile();

        if (!installFile.exists()) {
            installFile.mkdirs();
        }

        return installFile;
    }

    public void updateWindowTitle(String pageTitle) {
        this.setTitle(pageTitle + " - Turtlebrowse");
    }

    public void createTab(String url) {
        CefBrowser browser = cefClient.createBrowser(url, USE_OSR, false);
        openedBrowserTabs.add(browser);

        Platform.runLater(() -> {
            tabBar.addTabToUI(browser);
        });

        showTab(browser);
    }

    public void closeTab(CefBrowser browser) {
        int indexToClose = openedBrowserTabs.indexOf(browser);
        if (indexToClose == -1) return;
        
        openedBrowserTabs.remove(browser);
        titleMap.remove(browser);

        System.out.printf("Browser is current browser: %s", browser == currentBrowser);

        if (browser == currentBrowser) {
            if (openedBrowserTabs.isEmpty()) {
                System.out.println("No tabs.");
                currentBrowser = null;
                dispose();
            } else {
                System.out.println("Tabs is not empty, reverting to last tab.");

                int nextIndex = openedBrowserTabs.size() - 1;
                CefBrowser nextBrowser = openedBrowserTabs.get(nextIndex);
                System.out.println(openedBrowserTabs.get(nextIndex));

                showTab(nextBrowser);

                SwingUtilities.invokeLater(() -> {
                    browser.close(true);
                });
            }
        } else {
            browser.close(true);
        }
    }

    public void showTab(CefBrowser browser) {
        SwingUtilities.invokeLater(() -> {
            currentBrowser = browser;
            Component ui = browser.getUIComponent();

            if (ui.getMouseListeners().length == 0) {
                ui.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent event) {
                        SwingUtilities.invokeLater(() -> {
                            isUiFocused.set(false);
                            ui.requestFocusInWindow();
                            browser.setFocus(true);
                        });
                    }
                });
            }

            final String browserTitle = titleMap.get(browser);
            updateWindowTitle(browserTitle != null ? browserTitle : "Loading...");

            Platform.runLater(() -> {
                addressBar.updateUrl(browser.getURL());
                tabBar.setCurrentTab(browser);
            });
            
            browserContainer.removeAll();
            browserContainer.add(ui, BorderLayout.CENTER);
            
            browserContainer.revalidate();
            browserContainer.repaint();

            browser.setFocus(true);
        });
    }

    public CefBrowser getBrowserInstance() {
        return currentBrowser;
    }

    private void setMaterialColorSchemeFromSystem() {
        final Color accentColor = Platform.getPreferences().getAccentColor();
        if (accentColor == null) {
            materialColorScheme.set(ColorScheme.fromSeed(Color.web("#BDCF47")));
        } else {
            materialColorScheme.set(ColorScheme.fromSeed(accentColor));
        }
    }

    public void createDevTools() {
        currentBrowser.openDevTools();
    }

    @Override
    public void dispose() {
        System.out.println("Closing...");

        cefClient.removeMessageRouter(cefMessageRouter);
        cefMessageRouter.dispose();

        for (CefBrowser browser : openedBrowserTabs) {
            if (browser != null) browser.close(true);
        }

        if (cefApp != null) {
            cefApp.dispose();
        }

        super.dispose();

        System.out.println("Successfully closed browser.");
    }

    public String formatURL(String url, Boolean isSearching) {
        if (isSearching) {
            String searchQuery = URLEncoder.encode(url, StandardCharsets.UTF_8);
            return DEFAULT_SEARCH_PROVIDER + searchQuery;
        }

        if (url.contains(" ")) {
            String searchQuery = URLEncoder.encode(url, StandardCharsets.UTF_8);
            return DEFAULT_SEARCH_PROVIDER + searchQuery;
        }

        return url;
    }

    public void searchWeb(String query) {
        currentBrowser.loadURL(formatURL(query, true));
    }
}
