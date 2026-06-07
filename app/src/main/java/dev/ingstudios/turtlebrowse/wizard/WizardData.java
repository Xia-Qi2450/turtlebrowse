package dev.ingstudios.turtlebrowse.wizard;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.db.MainDatabase;
import dev.ingstudios.turtlebrowse.db.MainDatabase.ProfileStructure;
import javafx.scene.paint.Color;

public class WizardData {
	private final MainDatabase db = MainDatabase.getInstance();
	public String name = "";
	public Color themeColor = Main.mainMaterialColorScheme.getPrimary().get();
	public boolean enableAI = false;

	public WizardData() {
	}

	public void saveData() {
		db.createProfile(new ProfileStructure(name, themeColor));
		db.setFeature("ai", enableAI);
	}
}
