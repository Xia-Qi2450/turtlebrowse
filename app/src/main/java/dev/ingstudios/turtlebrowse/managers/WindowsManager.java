package dev.ingstudios.turtlebrowse.managers;

import java.util.ArrayList;
import java.util.List;

public class WindowsManager {
	private static WindowsManager instance;
	private final List<WindowItem> windows;

	public WindowsManager() {
		windows = new ArrayList<>();
	}

	public static synchronized WindowsManager getInstance() {
		if (instance == null) {
			instance = new WindowsManager();
		}
		return instance;
	}

	public List<WindowItem> getWindows() {
		return windows;
	}

	public void addWindow(WindowItem item) {
		windows.add(item);
	}

	public void removeWindow(String id) {
		System.out.printf("Removing window: %s\n", id);
		windows.removeIf(window -> {
			System.out.printf("Window ID: %s\n", window.id());
			return window.id() == id;
		});
	}

	public boolean hasWindows() {
		return windows.isEmpty();
	}

	public record WindowItem(String id, Object classType) {
	}
}
