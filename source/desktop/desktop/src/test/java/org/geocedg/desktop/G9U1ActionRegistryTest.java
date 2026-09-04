/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.JPanel;

import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.main.settings.AlgebraStyle;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
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
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
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
}
