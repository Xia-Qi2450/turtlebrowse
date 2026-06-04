package dev.ingstudios.turtlebrowse.wizard;

import org.dizitart.no2.collection.NitriteId;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.db.MainDatabase;
import dev.ingstudios.turtlebrowse.db.MainDatabase.ProfileStructureWithId;
import javafx.scene.paint.Color;

public class WizardData {
	private final MainDatabase db = MainDatabase.getInstance();
	public String name = "";
	public Color themeColor = Main.mainMaterialColorScheme.getPrimary().get();
	public boolean enableAI = false;

	public WizardData() {
	}

	public void saveData() {
		db.createProfile(new ProfileStructureWithId(name, themeColor, NitriteId.newId()));
		db.setFeature("ai", enableAI);
	}
}
