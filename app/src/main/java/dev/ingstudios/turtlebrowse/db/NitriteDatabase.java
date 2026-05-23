package dev.ingstudios.turtlebrowse.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.jspecify.annotations.Nullable;

import static org.dizitart.no2.filters.FluentFilter.where;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.type_adapters.ColorTypeAdapter;
import javafx.scene.paint.Color;

public class NitriteDatabase {
	private static NitriteDatabase instance;
	private final Gson gson = new GsonBuilder().registerTypeAdapter(Color.class, new ColorTypeAdapter()).create();
	private Nitrite db;
	private NitriteCollection profileCollection;
	private NitriteCollection featuresCollection;

	private NitriteDatabase() {
		initDb();
	}

	private void initDb() {
		final Path dbPath = Main.getStoragePath("database", "turtlebrowse.db");
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

		profileCollection = db.getCollection("profile");

		featuresCollection = db.getCollection("features");
		if (!featuresCollection.hasIndex("feature")) {
			featuresCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "feature");
		}
	}

	public static synchronized NitriteDatabase getInstance() {
		if (instance == null) {
			instance = new NitriteDatabase();
		}
		return instance;
	}

	public void createProfile(ProfileStructure profileStructure) {
		final Document profileDocument = Document
				.createDocument(gson.fromJson(gson.toJson(profileStructure), new TypeToken<Map<String, Object>>() {
				}.getType()));
		profileCollection.insert(profileDocument);
	}

	public List<ProfileStructure> getAllProfiles() {
		final DocumentCursor cursor = profileCollection.find();
		final List<Document> profileList = cursor.toList();

		final List<ProfileStructure> profileStructures = new ArrayList<>();

		for (final Document document : profileList) {
			profileStructures.add(parseProfile(document));
		}

		return profileStructures;
	}

	public ProfileStructure getFirstProfile() {
		final Document profileDocument = profileCollection.find().firstOrNull();
		return parseProfile(profileDocument);
	}

	public ProfileStructure getProfile(NitriteId id) {
		final Document profileDocument = profileCollection.getById(id);
		return parseProfile(profileDocument);
	}

	private ProfileStructure parseProfile(Document document) {
		final @Nullable String colorHex = document.get("seedColor", String.class);
		final ProfileStructure profileStructure = new ProfileStructure(document.get("name", String.class),
				colorHex != null ? Color.valueOf(colorHex) : null);
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

	}
}
