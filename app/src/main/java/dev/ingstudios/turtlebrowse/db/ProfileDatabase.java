package dev.ingstudios.turtlebrowse.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mvstore.MVStoreModule;

import dev.ingstudios.turtlebrowse.Main;

public class ProfileDatabase {
	private static ProfileDatabase instance;
	private Nitrite db;

	private ProfileDatabase(String profileId) {
		initDb(profileId);
	}

	private void initDb(String profileId) {
		final Path dbPath = Main.getStoragePath("profiles", profileId, "main.db");
		final Path parentDir = dbPath.getParent();
		try {
			if (parentDir != null && Files.notExists(parentDir)) {
				Files.createDirectories(parentDir);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		final MVStoreModule storeModule = MVStoreModule.withConfig().filePath(dbPath.toString()).build();

		db = Nitrite.builder().loadModule(storeModule).openOrCreate();
	}

	public static synchronized ProfileDatabase getInstance(String profileId) {
		if (instance == null) {
			instance = new ProfileDatabase(profileId);
		}
		return instance;
	}
}
