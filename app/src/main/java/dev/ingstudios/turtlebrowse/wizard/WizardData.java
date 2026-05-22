package dev.ingstudios.turtlebrowse.wizard;

import dev.ingstudios.turtlebrowse.Main;
import javafx.scene.paint.Color;

public class WizardData {
	public String name = "";
	public Color themeColor = Main.materialColorScheme.getPrimary().get();
	public boolean enableAI = false;

	public WizardData() {

	}
}
