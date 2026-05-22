package dev.ingstudios.turtlebrowse.windows;

import org.controlsfx.dialog.Wizard;

import dev.ingstudios.turtlebrowse.components.wizard_panes.AIWizardPane;
import dev.ingstudios.turtlebrowse.components.wizard_panes.PersonalizationWizardPane;
import dev.ingstudios.turtlebrowse.components.wizard_panes.StartWizardPane;
import dev.ingstudios.turtlebrowse.components.wizard_panes.ThemeWizardPane;
import dev.ingstudios.turtlebrowse.wizard.WizardData;

public class SetupWindow extends Wizard {
	public final WizardData wizardData = new WizardData();

	public SetupWindow() {
		final StartWizardPane startWizardPane = new StartWizardPane();
		final PersonalizationWizardPane personalizationWizardPane = new PersonalizationWizardPane(wizardData);
		final ThemeWizardPane themeWizardPane = new ThemeWizardPane(wizardData);
		final AIWizardPane aiWizardPane = new AIWizardPane(wizardData);

		final LinearFlow flow = new LinearFlow(startWizardPane, personalizationWizardPane, themeWizardPane,
				aiWizardPane);

		setFlow(flow);
	}
}
