package dev.ingstudios.turtlebrowse.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
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
import org.cef.callback.CefSchemeRegistrar;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.components.AISidebar;
import dev.ingstudios.turtlebrowse.components.AddressBar;
import dev.ingstudios.turtlebrowse.components.TabBar;
import dev.ingstudios.turtlebrowse.handlers.CefKeyboardHandler;
import dev.ingstudios.turtlebrowse.handlers.SwingKeyboardHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseContextMenuHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseDialogHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseDisplayHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseDownloadHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseFocusHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseLifeSpanHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseLoadHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseRequestHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseSchemeHandlerFactory;
import dev.ingstudios.turtlebrowse.ollama.OllamaChat;
import io.github.ollama4j.exceptions.OllamaException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import me.friwi.jcefmaven.*;

public class MainWindow extends JFrame {
	public final String START_URL = "turtlebrowse://newtab";
	public final String DEFAULT_SEARCH_PROVIDER = "https://google.com/search?q=";
	private final boolean USE_OSR = false;
	private final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.1.0 Safari/537.36";

	private CefApp cefApp;
	private CefClient cefClient;
	private CefSettings cefSettings;
	public CefBrowser currentBrowser;
	private ArrayList<CefBrowser> openedBrowserTabs = new ArrayList<>();
	private JPanel root;
	private JPanel browserContainer;
	public AddressBar addressBar;
	public TabBar tabBar;
	public final Map<CefBrowser, String> titleMap = new HashMap<>();
	public final BooleanProperty isUiFocused = new SimpleBooleanProperty(false);
	public OllamaChat ollamaSession;
	public AISidebar aiSidebar;
	private final Gson gson = new Gson();
	public final TurtlebrowseLoadHandler loadHandler = new TurtlebrowseLoadHandler();
	public final TurtlebrowseRequestHandler requestHandler = new TurtlebrowseRequestHandler(this);

	public MainWindow() {
		super("Turtlebrowse");

		Platform.runLater(() -> {
			Font.loadFont(getClass().getResourceAsStream("/fonts/google_sans_flex.ttf"), 10);
		});

		System.out.println("AWT Toolkit: " + java.awt.Toolkit.getDefaultToolkit().getClass().getName());
		System.out.println("DISPLAY: " + System.getenv("DISPLAY"));
		System.out.println("WAYLAND_DISPLAY: " + System.getenv("WAYLAND_DISPLAY"));

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

		// Tab bar
		tabBar = new TabBar(cefClient, openedBrowserTabs, this);

		// AI Sidebar
		aiSidebar = new AISidebar(cefClient, this, USE_OSR, isUiFocused);

		// Keyboard handler (JCEF)
		cefClient.addKeyboardHandler(new CefKeyboardHandler(this, START_URL));

		// Keyboard handler (Swing)
		new SwingKeyboardHandler(this, START_URL);

		cefClient.addFocusHandler(new TurtlebrowseFocusHandler(this));

		// Top panel (address + tab)
		final JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(tabBar, BorderLayout.NORTH);
		topPanel.add(addressBar, BorderLayout.SOUTH);

		// Bottom panel (main browser + AI sidebar)
		final JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(browserContainer, BorderLayout.CENTER);
		bottomPanel.add(aiSidebar, BorderLayout.EAST);

		root.add(topPanel, BorderLayout.NORTH);
		root.add(bottomPanel, BorderLayout.CENTER);

		cefClient.addDisplayHandler(new TurtlebrowseDisplayHandler(this));
		cefClient.addLifeSpanHandler(new TurtlebrowseLifeSpanHandler(this));
		cefClient.addDialogHandler(new TurtlebrowseDialogHandler());
		cefClient.addDownloadHandler(new TurtlebrowseDownloadHandler());
		cefClient.addContextMenuHandler(new TurtlebrowseContextMenuHandler(this));
		cefClient.addLoadHandler(loadHandler);
		cefClient.addRequestHandler(requestHandler);

		try {
			ollamaSession = new OllamaChat(USER_AGENT, this);
		} catch (OllamaException e) {
			e.printStackTrace();
		}

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				CefApp.getInstance().dispose();
				dispose();
			}
		});

		SwingUtilities.invokeLater(() -> {
			createTab(START_URL);
			setVisible(true);
		});
	}

	private CefApp createCefApp() {
		CefAppBuilder builder = new CefAppBuilder();
		builder.addJcefArgs(
				"--enable-media-stream",
				"--ozone-platform=x11",
				"--disable-gpu",
				"--disable-gpu-compositing",
				"--disable-software-rasterizer",
				"--no-sandbox",
				"--single-process");

		cefSettings = builder.getCefSettings();
		builder.setInstallDir(getInstallDir());
		cefSettings.windowless_rendering_enabled = USE_OSR;
		cefSettings.remote_debugging_port = 6767;

		cefSettings.user_agent = USER_AGENT;

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
				if (state == CefAppState.TERMINATED)
					System.exit(0);
			}

			@Override
			public void onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
				registrar.addCustomScheme("turtlebrowse", true, true, false, true, true, true, true);
			}

			@Override
			public void onContextInitialized() {
				CefApp.getInstance().registerSchemeHandlerFactory("turtlebrowse", "",
						new TurtlebrowseSchemeHandlerFactory(MainWindow.this));
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

	public String getCachePath() {
		return Main.getStoragePath("cef-cache").toString();
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
		if (indexToClose == -1)
			return;

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
			final Component ui = browser.getUIComponent();

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

	public void createDevTools() {
		currentBrowser.openDevTools();
	}

	@Override
	public void dispose() {
		System.out.println("Closing...");

		for (final CefBrowser browser : openedBrowserTabs) {
			if (browser != null)
				browser.close(true);
		}

		if (cefApp != null) {
			cefApp.dispose();
		}

		super.dispose();

		System.out.println("Successfully closed browser.");

		System.exit(0);
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

	public String handleApiFromClient(String action, String body) {
		@SuppressWarnings("null")
		JsonObject params = gson.fromJson(body, JsonObject.class);

		switch (action) {
			case "GET_NAME": {
				System.out.println("GET_NAME called.");
				return "Ethan Lee";
			}

			case "SEARCH_WEB": {
				final String query = params.get("query").getAsString();
				SwingUtilities.invokeLater(() -> searchWeb(query));
				return "\"ok\"";
			}

			case "GET_THEME": {
				final Color accentColor = Platform.getPreferences().getAccentColor();
				String hex = String.format("#%02x%02x%02x",
						(int) (accentColor.getRed() * 255),
						(int) (accentColor.getGreen() * 255),
						(int) (accentColor.getBlue() * 255));
				return hex;
			}

			default:
				return "\"Unknown action\"";
		}
	}

	public OllamaChat getOllamaSession() {
		return ollamaSession;
	}

	public void refeshSwingLayout() {
		SwingUtilities.invokeLater(() -> {
			if (root != null) {
				root.revalidate();
				root.repaint();
			}
		});
	}
}
