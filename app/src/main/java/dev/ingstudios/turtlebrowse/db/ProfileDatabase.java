package dev.ingstudios.turtlebrowse.db;

import static org.dizitart.no2.filters.FluentFilter.where;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.mvstore.MVStoreModule;

import dev.ingstudios.turtlebrowse.Main;

public class ProfileDatabase {
	private static ProfileDatabase instance;
	private Nitrite db;

	private NitriteCollection settingsCollection;

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

		settingsCollection = db.getCollection("settings");
	}

	public static synchronized ProfileDatabase getInstance(String profileId) {
		if (instance == null) {
			instance = new ProfileDatabase(profileId);
		}
		return instance;
	}

	public String getDefaultSearchEngine() {
		final Document searchEngineDocument = settingsCollection.find(where("setting").eq("searchEngine"))
				.firstOrNull();

		if (searchEngineDocument == null) {
			final Document newSearchEngineDocument = Document.createDocument().put("setting", "searchEngine")
					.put("engine", "google");
			settingsCollection.insert(newSearchEngineDocument);
			return "google";
		}

		return searchEngineDocument.get("engine").toString();
	}

	public void setDefaultSearchEngine(String searchEngine) {
		System.out.printf("Setting search engine: %s\n", searchEngine);

		final Document searchEngineDocument = settingsCollection.find(where("setting").eq("searchEngine"))
				.firstOrNull();

		if (searchEngineDocument == null) {
			System.err.println("Search engine document is null.");
			final Document newSearchEngineDocument = Document.createDocument().put("setting", "searchEngine")
					.put("engine", searchEngine);
			settingsCollection.insert(newSearchEngineDocument);
			return;
		}

		searchEngineDocument.put("engine", searchEngine);
		settingsCollection.update(searchEngineDocument);
	}
}
