package dev.ingstudios.turtlebrowse.wizard;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.db.NitriteDatabase;
import dev.ingstudios.turtlebrowse.db.NitriteDatabase.ProfileStructure;
import javafx.scene.paint.Color;

public class WizardData {
	private final NitriteDatabase db = NitriteDatabase.getInstance();
	public String name = "";
	public Color themeColor = Main.materialColorScheme.getPrimary().get();
	public boolean enableAI = false;

	public WizardData() {
	}

	public void saveData() {
		db.createProfile(new ProfileStructure(name, themeColor));
		db.setFeature("ai", enableAI);
	}
}
