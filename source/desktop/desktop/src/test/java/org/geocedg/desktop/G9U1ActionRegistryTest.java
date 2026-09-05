/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.JPanel;

import org.geogebra.common.GeoGebraConstants;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.main.App;
import org.geogebra.common.main.OptionType;
import org.geogebra.common.main.settings.AlgebraStyle;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.main.DialogManagerMinimal;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.desktop.util.LoggerD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1ActionRegistryTest {

	@BeforeAll
	static void initializeDesktop() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		Log.setLogger(new LoggerD());
	}

	static AppGeoCeDG app(boolean feature) {
		return G9U1TestApp.withoutWindowDispatcher(new AppGeoCeDG(
				new CommandLineArguments(new String[] {
						"--silent", "--enableLocusV2=" + feature}), new JPanel()));
	}

	@Test
	void everyActionHasExactlyOneBoundTargetAndReadableName() {
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app(true));
		assertEquals(110, registry.ids().size());
		for (String id : registry.ids()) {
			assertEquals(id, registry.get(id).getValue(GeoCeDGActionRegistry.ACTION_ID));
			assertFalse(((String) registry.get(id).getValue(Action.NAME)).isBlank(), id);
			assertNotNull(registry.get(id).getValue(Action.SHORT_DESCRIPTION), id);
		}
	}

	@Test
	void contextualHelpIsSpecificLocalizedAndUsesDistinctLongDescriptions() {
		AppGeoCeDG product = app(true);
		product.setLocale(Locale.ENGLISH);
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(product);
		for (GeoCeDGProfile.ActionDefinition definition : GeoCeDGProfile.getActions()) {
			if (definition.mode() == null) {
				String shortHelp = (String) registry.get(definition.id())
						.getValue(Action.SHORT_DESCRIPTION);
				String longHelp = (String) registry.get(definition.id())
						.getValue(Action.LONG_DESCRIPTION);
				assertNotEquals(shortHelp, longHelp, definition.id());
				assertEquals(GeoCeDGProfile.getText(
						definition.textKey() + ".long_help", "en"), longHelp,
						definition.id());
				assertFalse(shortHelp.contains(
						"Use the current construction and its explicit selection."),
						definition.id());
				assertFalse(longHelp.toLowerCase(Locale.ROOT).matches(
						".*\\b(todo|tbd|placeholder|lorem ipsum)\\b.*"), definition.id());
			}
		}
	}

	@Test
	void aboutUsesCentralProductVersionAndRetainsOriginCredits() {
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app(true));
		String about = registry.aboutText();
		assertTrue(about.contains(GeoCeDGProductInfo.applicationTitle()));
		assertTrue(about.contains(GeoGebraConstants.VERSION_STRING));
		assertTrue(about.contains("Manuel Prado-Velasco, Universidad de Sevilla"));
		assertTrue(about.contains("LICENSE"));
	}

	@Test
	void featureOffRetainsTruthfulDisabledActions() {
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app(false));
		assertFalse(registry.get("semantic.spline-v2.create").isEnabled());
		assertFalse(registry.get("semantic.locus-v2.create").isEnabled());
		assertTrue(registry.get("construction.point").isEnabled());
		assertNotNull(registry.unavailableReason("semantic.spline-v2.create"));
	}

	@Test
	void unavailableDihedralActionNeverPretendsToBeProductive() {
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app(true));
		assertFalse(registry.get("workspace.cedg-dihedral-procedures").isEnabled());
		assertTrue(registry.unavailableReason("workspace.cedg-dihedral-procedures")
				.contains("G9U2"));
	}

	@Test
	void modeInvocationSelectsExistingModeWithoutCreatingGeometry() {
		AppGeoCeDG app = app(true);
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
		int before = app.getKernel().getConstruction().steps();
		registry.invoke("construction.point", new ActionEvent(this, 1, "test"));
		assertEquals(GeoCeDGProfile.getAction("construction.point").mode().intValue(),
				app.getMode());
		assertEquals(before, app.getKernel().getConstruction().steps());
	}

	@Test
	void algebraDescriptionCheckedStateTracksActualHostSetting() {
		AppGeoCeDG app = app(true);
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
		registry.invoke("algebra.description.definition", new ActionEvent(this, 1, "test"));
		assertEquals(AlgebraStyle.DEFINITION, app.getSettings().getAlgebra().getStyle());
		assertEquals(true, registry.get("algebra.description.definition")
				.getValue(Action.SELECTED_KEY));
		assertEquals(false, registry.get("algebra.description.value")
				.getValue(Action.SELECTED_KEY));
	}

	@Test
	void globalPropertiesActionDispatchesToHostPreferencesWithoutSelection() {
		AppGeoCeDG product = spy(app(true));
		DialogManagerMinimal dialogs = mock(DialogManagerMinimal.class);
		doReturn(dialogs).when(product).getDialogManager();
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(product);
		registry.invoke("view.properties", new ActionEvent(this, 1, "test"));
		verify(dialogs).showPropertiesDialog(OptionType.GLOBAL, null);
	}

	@Test
	void constructionNavigationUsesTheGraphicsHostBarWithoutDagOrUndoMutation()
			throws IOException {
		AppGeoCeDG app = app(true);
		app.setShowConstructionProtocolNavigation(false, App.VIEW_EUCLIDIAN);
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction()
				.getUndoManager();
		try (UndoManagerD.PreparedUndoBaseline baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		int historySize = undo.getHistorySize();
		String before = constructionXml(app);
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
		registry.invoke("view.construction-navigation", new ActionEvent(this, 1, "test"));
		assertTrue(app.showConsProtNavigation(App.VIEW_EUCLIDIAN));
		assertEquals(Boolean.TRUE, registry.get("view.construction-navigation")
				.getValue(Action.SELECTED_KEY));
		assertEquals(before, constructionXml(app));
		assertEquals(historySize, undo.getHistorySize());
	}

	@Test
	void languageRefreshDoesNotReplaceActionIdentity() {
		AppGeoCeDG app = app(true);
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
		Action action = registry.get("document.open");
		app.setLocale(new Locale("es"));
		registry.refresh();
		assertEquals(action, registry.get("document.open"));
		assertEquals("Abrir\u2026", action.getValue(Action.NAME));
	}

	@Test
	void materializationRequiresCurrentResultContext() {
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app(true));
		assertFalse(registry.get("result.materialize-selected").isEnabled());
		assertFalse(registry.get("result.materialize-multiple").isEnabled());
		assertFalse(registry.get("result.materialize-all-eligible").isEnabled());
	}

	@Test
	void diagnosticLaunchUsesSeparateHostAndExplicitFileWithoutChangingWorkspace(
			@TempDir Path directory) throws Exception {
		Path resource = Files.createFile(directory.resolve("explicit diagnostic.ggb"));
		Path preferences = directory.resolve("laboratory.properties");
		List<String> arguments = GeoCeDGActionRegistry.diagnosticCommand(
				directory.resolve("javaw.exe"), preferences, resource.toFile());
		assertTrue(arguments.contains("org.geogebra.desktop.GeoGebra3D"));
		assertTrue(arguments.contains("--settingsfile=" + preferences));
		assertEquals(resource.toString(), arguments.get(arguments.size() - 1));
		assertFalse(arguments.contains("org.geocedg.desktop.GeoCeDG"));
	}

	@Test
	void diagnosticLaunchRejectsUnsupportedResource(@TempDir Path directory) throws Exception {
		Path resource = Files.createFile(directory.resolve("not-a-model.txt"));
		assertThrows(IOException.class, () -> GeoCeDGActionRegistry.diagnosticCommand(
				directory.resolve("java"), directory.resolve("prefs"), resource.toFile()));
	}

	private static String constructionXml(AppGeoCeDG app) {
		XMLStringBuilder xml = new XMLStringBuilder();
		app.getKernel().getConstruction().getConstructionXML(xml, true);
		return xml.toString();
	}
}
