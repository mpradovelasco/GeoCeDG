/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Documents.M;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.classic;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.insert;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.save;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.store;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.geogebra.common.kernel.ConstructionDefaults.LABEL_VISIBLE_ALWAYS_OFF;
import static org.geogebra.common.kernel.ConstructionDefaults.LABEL_VISIBLE_ALWAYS_ON;
import static org.geogebra.common.kernel.kernelND.GeoElementND.LABEL_DEFAULT;
import static org.geogebra.common.kernel.kernelND.GeoElementND.LABEL_NAME_VALUE;
import static org.geogebra.common.kernel.kernelND.GeoElementND.LABEL_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** CU4 A4. Explicit slider label choices through native serialization routes. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class Cu4SliderLabelPersistenceTest {

	private static final String LEGACY_NAME_AND_VALUE_TAG = "<labelMode val=\"1\"/>";
	private static final String EXPLICIT_NAME_AND_VALUE_TAG = "<labelMode val=\"6\"/>";

	@TempDir
	Path directory;

	@Test
	void explicitModeAndIndependentVisibilitySurviveUndoRedoAndReopen() throws Exception {
		onEdt(() -> {
			AppGeoCeDG source = app(M);
			source.setLabelingStyle(LABEL_VISIBLE_ALWAYS_OFF);
			GeoNumeric slider = typedSlider(source, "a=2");
			slider.setLabelMode(LABEL_NAME_VALUE);
			slider.setLabelVisible(false);
			assertTrue(slider.getXML().contains(EXPLICIT_NAME_AND_VALUE_TAG));
			GeoNumeric angle = typedSlider(source, "theta=47°");
			angle.setLabelMode(LABEL_NAME_VALUE);
			angle.setEuclidianVisible(false);
			assertTrue(angle.getXML().contains(EXPLICIT_NAME_AND_VALUE_TAG));
			baseline(source);

			slider.setLabelModeFromStylebar(1 + LABEL_VALUE);
			store(source);
			undo(source).undo();
			awaitQuiescent(source);
			assertLabel(source, "a", LABEL_NAME_VALUE, false);
			undo(source).redo();
			awaitQuiescent(source);
			assertLabel(source, "a", LABEL_VALUE, true);
			undo(source).undo();
			awaitQuiescent(source);

			File file = save(source, directory, "explicit-slider-mode");
			AppGeoCeDG receiver = app(M);
			receiver.setLabelingStyle(LABEL_VISIBLE_ALWAYS_ON);
			assertTrue(receiver.loadExistingFile(file, false));
			assertLabel(receiver, "a", LABEL_NAME_VALUE, false);
			assertTrue(receiver.getKernel().lookupLabel("a").getXML()
					.contains(EXPLICIT_NAME_AND_VALUE_TAG));
			GeoElement restoredAngle = G9U1TestApp.lookup(receiver, "theta");
			assertEquals(LABEL_NAME_VALUE, restoredAngle.getLabelMode());
			assertFalse(restoredAngle.isSetEuclidianVisible());
			return null;
		});
	}

	@Test
	void styleBarChoiceSurvivesSameWindowAndCrossWindowPasteAndFileInsertion() throws Exception {
		onEdt(() -> {
			AppGeoCeDG source = app(M);
			source.setLabelingStyle(LABEL_VISIBLE_ALWAYS_OFF);
			GeoNumeric slider = typedSlider(source, "a=2");
			slider.setLabelModeFromStylebar(1 + LABEL_NAME_VALUE);
			slider.setLabelModeFromStylebar(0);
			assertTrue(slider.getXML().contains(EXPLICIT_NAME_AND_VALUE_TAG));
			GeoElement commandSlider = must(source, "b=Slider(0,10)");
			File file = save(source, directory, "clipboard-and-insertion-source");
			baseline(source);

			var buffer = source.getCopyPaste();
			buffer.copyToXML(source, List.of(slider), false);
			buffer.pasteFromXML(source, false);
			assertLabel(source, "a_1", LABEL_NAME_VALUE, false);

			AppGeoCeDG crossWindow = app(M);
			crossWindow.setLabelingStyle(LABEL_VISIBLE_ALWAYS_OFF);
			baseline(crossWindow);
			buffer.copyToXML(source, List.of(slider, commandSlider), false);
			buffer.pasteFromXML(crossWindow, false);
			assertLabel(crossWindow, "a", LABEL_NAME_VALUE, false);
			assertEquals(LABEL_NAME_VALUE, crossWindow.getKernel().lookupLabel("b").getLabelMode());

			AppGeoCeDG inserted = app(M);
			inserted.setLabelingStyle(LABEL_VISIBLE_ALWAYS_OFF);
			baseline(inserted);
			assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED,
					insert(inserted, file).getOutcome());
			assertLabel(inserted, "a", LABEL_NAME_VALUE, false);
			undo(inserted).undo();
			awaitQuiescent(inserted);
			undo(inserted).redo();
			awaitQuiescent(inserted);
			assertLabel(inserted, "a", LABEL_NAME_VALUE, false);
			return null;
		});
	}

	@Test
	void oldOmissionRemainsImplicitAndClassicKeepsItsWriter() throws Exception {
		onEdt(() -> {
			AppGeoCeDG source = app(M);
			source.setLabelingStyle(LABEL_VISIBLE_ALWAYS_ON);
			GeoNumeric implicit = typedSlider(source, "a=2");
			implicit.setLabelMode(LABEL_DEFAULT);
			assertEquals(LABEL_NAME_VALUE, implicit.getLabelMode());
			assertFalse(implicit.getXML().contains(EXPLICIT_NAME_AND_VALUE_TAG));
			assertFalse(implicit.getXML().contains(LEGACY_NAME_AND_VALUE_TAG));

			File legacyFile = save(source, directory, "implicit-slider-mode");
			AppGeoCeDG receiver = app(M);
			assertTrue(receiver.loadExistingFile(legacyFile, false));
			GeoElement restored = receiver.getKernel().lookupLabel("a");
			assertEquals(LABEL_NAME_VALUE, restored.getLabelMode());
			assertFalse(restored.getXML().contains(EXPLICIT_NAME_AND_VALUE_TAG));
			assertFalse(restored.getXML().contains(LEGACY_NAME_AND_VALUE_TAG));

			source.setLabelingStyle(LABEL_VISIBLE_ALWAYS_OFF);
			implicit.setLabelMode(LABEL_NAME_VALUE);
			assertTrue(source.getXML().contains(EXPLICIT_NAME_AND_VALUE_TAG));
			receiver.setXML(source.getXML().replace(EXPLICIT_NAME_AND_VALUE_TAG,
					LEGACY_NAME_AND_VALUE_TAG), true);
			GeoElement legacyExplicit = G9U1TestApp.lookup(receiver, "a");
			assertEquals(LABEL_NAME_VALUE, legacyExplicit.getLabelMode());
			assertFalse(legacyExplicit.getXML().contains(EXPLICIT_NAME_AND_VALUE_TAG));
			assertFalse(legacyExplicit.getXML().contains(LEGACY_NAME_AND_VALUE_TAG));

			AppD classicApp = classic();
			GeoNumeric classicSlider = typedSlider(classicApp, "a=2");
			classicSlider.setLabelMode(LABEL_NAME_VALUE);
			assertFalse(classicSlider.getXML().contains(EXPLICIT_NAME_AND_VALUE_TAG));
			assertFalse(classicSlider.getXML().contains(LEGACY_NAME_AND_VALUE_TAG));
			return null;
		});
	}

	private static GeoNumeric typedSlider(AppD app, String input) {
		GeoNumeric slider = (GeoNumeric) must(app, input);
		slider.setEuclidianVisible(true);
		assertNotNull(slider.getDefinition());
		assertTrue(slider.isSlider());
		return slider;
	}

	private static void assertLabel(AppGeoCeDG app, String label, int mode, boolean visible) {
		GeoElement geo = G9U1TestApp.lookup(app, label);
		assertEquals(mode, geo.getLabelMode(), label);
		assertEquals(visible, geo.getLabelVisible(), label);
	}
}
