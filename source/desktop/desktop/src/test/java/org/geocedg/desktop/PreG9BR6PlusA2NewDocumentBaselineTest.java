/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geogebra.common.main.GeoGebraPreferences;
import org.geogebra.desktop.main.GeoGebraPreferencesD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * OBS-R6PLUS-NEW-DOCUMENT-PREFERENCE-NUMERIC-SAVE-PROMPT (pre-existing, reproduced on
 * P_R6PLUS_D1; author-authorized focal correction during the PRE-G9B-R6-plus-A-2
 * review): the completed File &gt; New is the saved baseline, whatever the reloaded
 * user preferences register, and the first real edit still makes it unsaved.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusA2NewDocumentBaselineTest {
	private static final String TABLEVIEW = "<tableview min=\"0\" max=\"0\" step=\"0\"/>";
	private static final String NO_STEP = "consStep=\"-1\"";
	private static final String SAVED_STEP = "consStep=\"2\"";

	@TempDir
	Path temporary;

	// ------------------------------------------------------- N1, N4, N7 (factory)

	@Test
	void anUntouchedNewIsSavedWithFactoryPreferences() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.fileNew();
		assertBlankAndSaved(app);

		AppGeoCeDG dirty = G9U1TestApp.create();
		dirty.getLayerWorkspace().setWorkingLayer(3);
		eval(dirty, "A=(1,1)");
		assertTrue(dirty.getLayerWorkspace().setLayerHidden(5, true));
		assertFalse(dirty.isSaved());
		// the prompt of New is accepted by saving the dirty document first
		assertTrue(((GuiManagerGeoCeDG) dirty.getGuiManager()).saveAsTo(
				temporary.resolve("dirty.cedg").toFile()));
		dirty.fileNew();
		assertBlankAndSaved(dirty);
	}

	// ----------------------------------------------- N2, N3, N4 (saved <tableview>)

	@Test
	void anUntouchedNewIsSavedWithReplayedUserPreferences() throws Exception {
		try (AutoCloseable preferences = replayedUserPreferences()) {
			AppGeoCeDG app = G9U1TestApp.create();
			app.fileNew();
			assertTrue(app.getKernel().getConstruction().isStarted(),
					"the replayed preferences really register a used type");
			assertBlankAndSaved(app);
		}
	}

	@Test
	void newDocumentUnitDefaultsStayAppliedAndSaved() throws Exception {
		PreG9BR6PlusD1DocumentUnitsTest.MemoryStore store =
				new PreG9BR6PlusD1DocumentUnitsTest.MemoryStore();
		store.values.put(GeoCeDGUnitPreferences.CONSTRUCTION_KEY, "cm");
		try (AutoCloseable preferences = replayedUserPreferences();
				AutoCloseable units = GeoCeDGUnitPreferences.useStoreForTesting(store)) {
			AppGeoCeDG app = G9U1TestApp.create();
			app.fileNew();
			assertEquals(UnitState.of(UnitToken.CM, null, null, false),
					app.getDocumentUnits().getState(), "D1 defaults are applied");
			assertTrue(app.isSaved(), "and the initialized document is saved");
		}
	}

	// ------------------------------------------------------------------ N5, N6

	@Test
	void theFirstRealEditAfterNewMakesTheDocumentUnsaved() throws Exception {
		try (AutoCloseable preferences = replayedUserPreferences()) {
			AppGeoCeDG geometry = newDocument();
			eval(geometry, "P=(1,1)");
			// a user creation ends with its undo point, which marks the document
			PreG9BR6PlusA1LayerWorkspaceTest.awaitStore(geometry,
					PreG9BR6PlusA1LayerWorkspaceTest.undo(geometry));
			assertFalse(geometry.isSaved(), "a new object");

			AppGeoCeDG units = newDocument();
			units.setDocumentUnitsPrompt((owner, initial) -> new GeoCeDGDocumentUnits.Request(
					UnitToken.MM, null, false, null, null, null));
			assertTrue(units.editDocumentUnits());
			assertFalse(units.isSaved(), "a unit change");

			AppGeoCeDG hidden = newDocument();
			assertTrue(hidden.getLayerWorkspace().setLayerHidden(4, true));
			assertFalse(hidden.isSaved(), "a hidden-layer change");
		}
	}

	@Test
	void aHiddenLayerChangeAfterNewIsUnsavedNeverAnUndoPointAndSaveClearsIt()
			throws Exception {
		try (AutoCloseable preferences = replayedUserPreferences()) {
			AppGeoCeDG app = newDocument();
			UndoManagerD undo = PreG9BR6PlusA1LayerWorkspaceTest.undo(app);
			int history = undo.getHistorySize();
			CountDownLatch stored = new CountDownLatch(1);
			undo.addUndoInfoStoredListener(stored::countDown);
			assertTrue(app.getLayerWorkspace().setLayerHidden(4, true));
			assertFalse(app.isSaved());
			assertFalse(stored.await(2, TimeUnit.SECONDS), "no undo point");
			assertEquals(history, undo.getHistorySize());
			Path file = temporary.resolve("hidden.cedg");
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
			assertTrue(app.isSaved(), "Save clears the change");
			AppGeoCeDG reopened = G9U1TestApp.create();
			assertTrue(reopened.loadFile(file.toFile(), false));
			assertEquals(List.of(4), reopened.getLayerWorkspace().getHiddenLayers());
			assertTrue(reopened.isSaved());
		}
	}

	// --------------------------------------------------------------------- helpers

	private static AppGeoCeDG newDocument() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.fileNew();
		assertTrue(app.isSaved());
		return app;
	}

	private static void assertBlankAndSaved(AppGeoCeDG app) {
		assertTrue(app.isSaved(), "the completed New is the saved baseline");
		assertTrue(app.getKernel().getConstruction().getGeoSetConstructionOrder().isEmpty());
		assertTrue(app.getLayerWorkspace().getHiddenLayers().isEmpty());
		assertEquals(0, app.getLayerWorkspace().getWorkingLayer());
		assertFalse(app.getXML().contains("geocedgHiddenLayers"),
				"initialization emits no hidden-layer metadata");
	}

	/**
	 * Points the portable preference store, which the product always uses, at a
	 * scratch settings file whose user preferences replay the two automatic effects
	 * found in the author's installed settings: a saved table view, which registers a
	 * used type, and a saved construction-protocol step, which clears the saved flag
	 * when restored. Every static of the store is restored afterwards.
	 */
	private AutoCloseable replayedUserPreferences() throws Exception {
		String xml = G9U1TestApp.create().getPreferencesXML();
		assertTrue(xml.contains("</geogebra>") && xml.contains(NO_STEP));
		Properties settings = new Properties();
		settings.setProperty(GeoGebraPreferences.XML_USER_PREFERENCES, xml
				.replace(NO_STEP, SAVED_STEP)
				.replace("</geogebra>", TABLEVIEW + "\n</geogebra>"));
		Path file = temporary.resolve("preferences-" + System.nanoTime() + ".properties");
		try (OutputStream out = Files.newOutputStream(file)) {
			settings.store(out, null);
		}
		Field path = GeoGebraPreferencesD.class.getDeclaredField("PROPERTY_FILEPATH");
		path.setAccessible(true);
		Class<?> portable = Class.forName(
				"org.geogebra.desktop.main.GeoGebraPortablePreferences");
		Field singleton = portable.getDeclaredField("singleton");
		singleton.setAccessible(true);
		Field properties = portable.getDeclaredField("properties");
		properties.setAccessible(true);
		final Object previousPath = path.get(null);
		final Object previousSingleton = singleton.get(null);
		final Properties loaded = (Properties) properties.get(null);
		final Properties previousLoaded = (Properties) loaded.clone();
		path.set(null, file.toString());
		singleton.set(null, null);
		loaded.clear();
		return () -> {
			path.set(null, previousPath);
			singleton.set(null, previousSingleton);
			loaded.clear();
			loaded.putAll(previousLoaded);
		};
	}
}
