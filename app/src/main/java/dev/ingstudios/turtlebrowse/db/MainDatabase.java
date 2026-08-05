package dev.ingstudios.turtlebrowse.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.jspecify.annotations.Nullable;

import static org.dizitart.no2.filters.FluentFilter.where;

import dev.ingstudios.turtlebrowse.Main;
import javafx.scene.paint.Color;

public class MainDatabase {
	private static MainDatabase instance;
	private Nitrite db;
	private NitriteCollection profileCollection;
	private NitriteCollection featuresCollection;

	private MainDatabase() {
		initDb();
	}

	private void initDb() {
		System.out.println("Initializing database...");

		final Path dbPath = Main.getStoragePath("database", "main.db");
		final Path parentDir = dbPath.getParent();
		try {
			if (parentDir != null && Files.notExists(parentDir)) {
				Files.createDirectories(parentDir);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		final MVStoreModule storeModule = MVStoreModule.withConfig().filePath(dbPath.toString()).build();
		try {
			db = Nitrite.builder().loadModule(storeModule).openOrCreate();
		} catch (NitriteIOException e) {
			System.err.printf("Error while opening database: %s\n", e.getMessage());
		}

		profileCollection = db.getCollection("profile");

		featuresCollection = db.getCollection("features");
		if (!featuresCollection.hasIndex("feature")) {
			featuresCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "feature");
		}
	}

	public static synchronized MainDatabase getInstance() {
		if (instance == null) {
			instance = new MainDatabase();
		}
		return instance;
	}

	public ProfileStructureWithId createProfile(ProfileStructure profileStructure) {
		final Document profileDocument = Document
				.createDocument(profileStructure.toMap());
		profileDocument.put("uuid", UUID.randomUUID());
		profileCollection.insert(profileDocument);
		return parseProfile(profileDocument);
	}

	public List<ProfileStructureWithId> getAllProfiles() {

		final DocumentCursor cursor = profileCollection.find();
		final List<Document> profileList = cursor.toList();

		final List<ProfileStructureWithId> profileStructures = new ArrayList<>();

		for (final Document document : profileList) {
			profileStructures.add(parseProfile(document));
		}

		return profileStructures;
	}

	public ProfileStructureWithId getFirstProfile() {
		final Document profileDocument = profileCollection.find().firstOrNull();
		return parseProfile(profileDocument);
	}

	public ProfileStructureWithId getProfile(UUID id) {
		final Document profileDocument = profileCollection.find(where("uuid").eq(id)).firstOrNull();
		return parseProfile(profileDocument);
	}

	public void removeProfile(UUID id) {
		final Document doc = profileCollection.find(where("uuid").eq(id)).firstOrNull();
		if (doc != null) {
			profileCollection.remove(doc);
		}
	}

	public void editProfile(UUID id, ProfileStructureWithId profile) {
		final Document updateDoc = Document.createDocument(profile.toMap());
		profileCollection.update(where("uuid").eq(id), updateDoc);
	}

	private ProfileStructureWithId parseProfile(Document document) {
		final @Nullable String colorHex = document.get("seedColor", String.class);
		final ProfileStructureWithId profileStructure = new ProfileStructureWithId(document.get("name", String.class),
				colorHex != null ? Color.valueOf(colorHex) : null,
				document.get("uuid", UUID.class));
		return profileStructure;
	}

	public void setFeature(String featureName, boolean value) {
		final Map<String, Object> featureMap = new HashMap<>();
		featureMap.put("feature", featureName);
		featureMap.put("value", value);
		final Document featureDocument = Document.createDocument(featureMap);
		final UpdateOptions options = UpdateOptions.updateOptions(true);
		featuresCollection.update(where("feature").eq(featureName), featureDocument, options);
	}

	public boolean getFeature(String featureName) {
		final Document featureDocument = featuresCollection.find(where("feature").eq(featureName)).firstOrNull();
		if (featureDocument == null) {
			System.err.printf("Feature %s is null.", featureName);
			return false;
		} else {
			return featureDocument.get("value", boolean.class);
		}
	}

	public void closeDb() {
		if (db != null && !db.isClosed())
			db.close();
	}

	public record ProfileStructure(String name, Color seedColor) {
		public Map<String, Object> toMap() {
			Map<String, Object> map = new HashMap<>();
			map.put("name", name);
			map.put("seedColor", seedColor.toString());
			return map;
		}
	}

	public record ProfileStructureWithId(String name, Color seedColor, UUID id) {
		public String getIdAsString() {
			return id.toString();
		}

		public Map<String, Object> toMap() {
			Map<String, Object> map = new HashMap<>();
			map.put("name", name);
			map.put("seedColor", seedColor.toString());
			return map;
		}
	}
}
