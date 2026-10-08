/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.export.pstricks.GeoGebraExport;
import org.geogebra.common.kernel.geos.GProperty;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-E2 author-smoke follow-up B1 and B2 on the Desktop product: the
 * initial line thickness preference of the two dimension tools, and the dimension unit
 * suffix policy of new documents, of the Document units dialog and of saved documents
 * (unit-system v1.1, section 14.2).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusE2PresentationFollowUpDesktopTest {

	@TempDir
	Path temporary;

	private static GeoCeDGDimensionPreferences dimensionStore(
			PreG9BR6PlusD1DocumentUnitsTest.MemoryStore store) {
		return new GeoCeDGDimensionPreferences(store);
	}

	private static AlgoNativeDimension toolDimension(AppGeoCeDG app, int mode, String a,
			String b, String direction) {
		GeoPoint pointA = (GeoPoint) G9U1TestApp.eval(app, a);
		GeoPoint pointB = (GeoPoint) G9U1TestApp.eval(app, b);
		GeoElement dir = direction == null ? null : G9U1TestApp.eval(app, direction);
		EuclidianView view = app.getActiveEuclidianView();
		GeoElement[] out = new GeoCeDGDimensionTools(app).create(mode, pointA, pointB, dir,
				pointA.getInhomX(), pointA.getInhomY() + 1, view);
		assertNotNull(out);
		return AlgoNativeDimension.ownerOf(out[0]);
	}

	private static List<GeoSegment> segments(AlgoNativeDimension algo) {
		return List.of(algo.getDimensionLine(), algo.getExtensionA(), algo.getExtensionB());
	}

	private static String text(AlgoNativeDimension algo) {
		return ((GeoText) algo.getPresentationText()).getTextString();
	}

	// ------------------------------------------------------------------ B1

	@Test
	void bothToolsUseTheInitialThicknessPreferenceAndNeverChangeExistingDimensions()
			throws Exception {
		PreG9BR6PlusD1DocumentUnitsTest.MemoryStore store =
				new PreG9BR6PlusD1DocumentUnitsTest.MemoryStore();
		try (AutoCloseable override = GeoCeDGDimensionPreferences.useStoreForTesting(store)) {
			AppGeoCeDG app = G9U1TestApp.create();
			AlgoNativeDimension aligned = toolDimension(app,
					EuclidianConstants.MODE_ALIGNED_DIMENSION, "A=(0,0)", "B=(4,3)", null);
			AlgoNativeDimension linear = toolDimension(app,
					EuclidianConstants.MODE_LINEAR_DIMENSION, "C=(0,5)", "D=(4,8)", "g:y=0");
			for (AlgoNativeDimension algo : List.of(aligned, linear)) {
				for (GeoSegment segment : segments(algo)) {
					assertEquals(2, segment.getLineThickness(), "default 2");
				}
			}
			dimensionStore(store).setInitialLineThickness(5);
			AlgoNativeDimension later = toolDimension(app,
					EuclidianConstants.MODE_ALIGNED_DIMENSION, "E=(0,10)", "F=(4,10)", null);
			for (GeoSegment segment : segments(later)) {
				assertEquals(5, segment.getLineThickness(), "custom initial thickness");
			}
			for (GeoSegment segment : segments(aligned)) {
				assertEquals(2, segment.getLineThickness(), "existing dimensions unchanged");
			}
			double value = aligned.getValue().getDouble();
			GeoSegment line = aligned.getDimensionLine();
			double x = line.getStartPoint().getInhomX();
			line.setLineThickness(9);
			line.updateVisualStyleRepaint(GProperty.LINE_STYLE);
			assertEquals(9, line.getLineThickness());
			assertEquals(2, aligned.getExtensionA().getLineThickness(), "independent styles");
			assertEquals(value, aligned.getValue().getDouble(), 0, "no geometric recompute");
			assertEquals(x, line.getStartPoint().getInhomX(), 0);
		}
	}

	@Test
	void thePreferencePersistsInTheStoreAndRejectsInvalidValues() {
		PreG9BR6PlusD1DocumentUnitsTest.MemoryStore store =
				new PreG9BR6PlusD1DocumentUnitsTest.MemoryStore();
		assertEquals(2, dimensionStore(store).initialLineThickness());
		for (String invalid : new String[] {"0", "-1", "99", "two", "", " 3"}) {
			store.values.put(GeoCeDGDimensionPreferences.LINE_THICKNESS_KEY, invalid);
			assertEquals(2, dimensionStore(store).initialLineThickness(), invalid);
		}
		assertEquals(0, store.saves, "reading never writes");
		dimensionStore(store).setInitialLineThickness(4);
		assertEquals("4", store.values.get("geocedg.dimensions.initial-line-thickness.v1"));
		assertEquals(4, dimensionStore(store).initialLineThickness(), "a restart reads it");
	}

	@Test
	void savedSegmentsKeepTheirOwnStyleWhateverThePreference() throws Exception {
		PreG9BR6PlusD1DocumentUnitsTest.MemoryStore store =
				new PreG9BR6PlusD1DocumentUnitsTest.MemoryStore();
		File saved = temporary.resolve("thickness.cedg").toFile();
		try (AutoCloseable override = GeoCeDGDimensionPreferences.useStoreForTesting(store)) {
			dimensionStore(store).setInitialLineThickness(5);
			AppGeoCeDG app = G9U1TestApp.create();
			AlgoNativeDimension algo = toolDimension(app,
					EuclidianConstants.MODE_ALIGNED_DIMENSION, "A=(0,0)", "B=(4,3)", null);
			algo.getValue().setLabel("d");
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(saved));
			dimensionStore(store).setInitialLineThickness(9);
			AppGeoCeDG reopened = G9U1TestApp.create();
			assertTrue(reopened.loadFile(saved, false));
			AlgoNativeDimension again = AlgoNativeDimension.ownerOf(
					reopened.getKernel().lookupLabel("d"));
			for (GeoSegment segment : segments(again)) {
				assertEquals(5, segment.getLineThickness(), "the document style wins");
			}
		}
	}

	// ------------------------------------------------------------------ B2

	@Test
	void newDocumentsHideTheSuffixAndTheDialogTogglesItInOneUndoStep() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		assertFalse(app.getDocumentUnits().getState().isDimensionUnitSuffixShown(),
				"author decision: new documents hide the suffix");
		assertTrue(app.isSaved(), "an untouched new document stays saved");
		app.setUnsaved();
		assertTrue(app.isSaved(), "the policy alone is never save-relevant");
		app.getDocumentUnits().replace(app.getDocumentUnits().getState()
				.withConstructionUnit(UnitToken.CM).withPresentationUnit(UnitToken.MM));
		AlgoNativeDimension algo = AlgoNativeDimension.ownerOf(G9U1TestApp.eval(app,
				"d=AlignedDimension((0,0),(12,0),1)"));
		assertEquals("120", text(algo));
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
		try (var baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		int history = undo.getHistorySize();
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);
		app.setDocumentUnitsPrompt((owner, initial) -> {
			assertEquals(Boolean.FALSE, initial.dimensionUnitSuffixShown);
			return new GeoCeDGDocumentUnits.Request(initial.construction,
					initial.presentation, initial.usmDefined, initial.factor, initial.name,
					initial.symbol, true);
		});
		assertTrue(app.editDocumentUnits());
		assertTrue(stored.await(5, TimeUnit.SECONDS), "one undo point");
		assertEquals(history + 1, undo.getHistorySize());
		assertEquals("120 mm", text(algo));
		assertEquals(12, algo.getValue().getDouble(), 0, "model-unit value");
		app.getKernel().undo();
		AlgoNativeDimension undone = AlgoNativeDimension.ownerOf(
				app.getKernel().lookupLabel("d"));
		assertEquals("120", text(undone));
		assertFalse(app.getDocumentUnits().getState().isDimensionUnitSuffixShown());
	}

	@Test
	void theDocumentPolicyPersistsAndWinsOverThePreference() throws Exception {
		File saved = temporary.resolve("hidden.cedg").toFile();
		AppGeoCeDG app = G9U1TestApp.create();
		app.getDocumentUnits().replace(app.getDocumentUnits().getState()
				.withConstructionUnit(UnitToken.CM).withPresentationUnit(UnitToken.MM));
		G9U1TestApp.eval(app, "d=AlignedDimension((0,0),(12,0),1)");
		assertTrue(app.getXML().contains("<geocedgUnits version=\"2\" construction=\"cm\""
				+ " presentation=\"mm\" dimensionUnitSuffix=\"hidden\"/>"));
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(saved));
		PreG9BR6PlusD1DocumentUnitsTest.MemoryStore shown =
				new PreG9BR6PlusD1DocumentUnitsTest.MemoryStore();
		shown.values.put(GeoCeDGUnitPreferences.DIMENSION_SUFFIX_KEY, "shown");
		try (AutoCloseable override = GeoCeDGUnitPreferences.useStoreForTesting(shown)) {
			AppGeoCeDG reopened = G9U1TestApp.create();
			assertTrue(reopened.getDocumentUnits().getState().isDimensionUnitSuffixShown(),
					"the preference sets new documents");
			assertTrue(reopened.loadFile(saved, false));
			assertFalse(reopened.getDocumentUnits().getState().isDimensionUnitSuffixShown(),
					"an opened document keeps its own policy");
			assertEquals("120", text(AlgoNativeDimension.ownerOf(
					reopened.getKernel().lookupLabel("d"))));
		}
	}

	@Test
	void historicalE2DocumentsWithoutPolicyKeepTheVisibleSuffix() throws Exception {
		File saved = temporary.resolve("historical.cedg").toFile();
		PreG9BR6PlusD1DocumentUnitsTest.MemoryStore shown =
				new PreG9BR6PlusD1DocumentUnitsTest.MemoryStore();
		shown.values.put(GeoCeDGUnitPreferences.DIMENSION_SUFFIX_KEY, "shown");
		try (AutoCloseable override = GeoCeDGUnitPreferences.useStoreForTesting(shown)) {
			AppGeoCeDG app = G9U1TestApp.create();
			app.getDocumentUnits().replace(UnitState.of(UnitToken.CM, UnitToken.MM, null));
			G9U1TestApp.eval(app, "d=AlignedDimension((0,0),(12,0),1)");
			assertTrue(app.getXML().contains(
					"<geocedgUnits version=\"1\" construction=\"cm\" presentation=\"mm\"/>"),
					"exactly the original E2 serialization");
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(saved));
		}
		AppGeoCeDG reopened = G9U1TestApp.create();
		assertFalse(reopened.getDocumentUnits().getState().isDimensionUnitSuffixShown());
		assertTrue(reopened.loadFile(saved, false));
		assertTrue(reopened.getDocumentUnits().getState().isDimensionUnitSuffixShown());
		assertEquals("120 mm", text(AlgoNativeDimension.ownerOf(
				reopened.getKernel().lookupLabel("d"))));
	}

	@Test
	void exportedTextFollowsTheDocumentPolicy() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getDocumentUnits().replace(UnitState.of(UnitToken.CM, UnitToken.MM, null, false));
		G9U1TestApp.eval(app, "d=AlignedDimension((0,0),(4,0),1)");
		EuclidianView view = app.getEuclidianView1();
		GeoGebraExport hidden = PreG9BR6PlusCLatexExportTest.exporter(app, "pgf");
		String hiddenCode = PreG9BR6PlusCLatexExportTest.generate(hidden, view);
		assertTrue(hiddenCode.contains("] {40};"), hiddenCode);
		app.getDocumentUnits().replace(UnitState.of(UnitToken.CM, UnitToken.MM, null, true));
		GeoGebraExport shown = PreG9BR6PlusCLatexExportTest.exporter(app, "pgf");
		String shownCode = PreG9BR6PlusCLatexExportTest.generate(shown, view);
		assertTrue(shownCode.contains("] {40 mm};"), shownCode);
	}

	@Test
	void theDimensionPanelStoresCreationPreferencesOnly() {
		PreG9BR6PlusD1DocumentUnitsTest.MemoryStore dimensions =
				new PreG9BR6PlusD1DocumentUnitsTest.MemoryStore();
		PreG9BR6PlusD1DocumentUnitsTest.MemoryStore units =
				new PreG9BR6PlusD1DocumentUnitsTest.MemoryStore();
		AppGeoCeDG app = G9U1TestApp.create();
		G9U1TestApp.eval(app, "d=AlignedDimension((0,0),(12,0),1)");
		app.setSaved();
		final String xml = app.getXML();
		UnitState state = app.getDocumentUnits().getState();
		GeoCeDGDimensionPresentationPanel panel = new GeoCeDGDimensionPresentationPanel(app,
				new GeoCeDGDimensionPreferences(dimensions), new GeoCeDGUnitPreferences(units));
		assertEquals(2, panel.thicknessControl().getValue());
		assertFalse(panel.suffixControl().isSelected());
		panel.thicknessControl().setValue(6);
		panel.suffixControl().doClick();
		assertEquals("6", dimensions.values.get(GeoCeDGDimensionPreferences.LINE_THICKNESS_KEY));
		assertEquals("shown", units.values.get(GeoCeDGUnitPreferences.DIMENSION_SUFFIX_KEY));
		assertEquals(state, app.getDocumentUnits().getState(), "the document is untouched");
		assertEquals(xml, app.getXML());
		assertTrue(app.isSaved());
		assertTrue(app.newProductDimensionPresentationPanel()
				instanceof GeoCeDGDimensionPresentationPanel);
		assertEquals(12, ((GeoNumeric) app.getKernel().lookupLabel("d")).getDouble(), 0);
	}
}
