/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;

import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R6-plus-D1 T-CLIPBOARD (unit-system v1.0, section 11; DQ-D1-7, DQ-D1-9):
 * transient provenance beside each window's buffer, a non-blocking notice only for
 * different physical meanings, numbers and target units never changed.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusD1UnitClipboardTest {

	private static KeyEvent ctrl(AppGeoCeDG app, int keyCode) {
		return new KeyEvent(app.getEuclidianView1().getJPanel(), KeyEvent.KEY_PRESSED,
				System.currentTimeMillis(), InputEvent.CTRL_DOWN_MASK, keyCode,
				KeyEvent.CHAR_UNDEFINED);
	}

	private static AppGeoCeDG app(UnitState state) {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		app.getKernel().setContinuous(false);
		app.getDocumentUnits().replace(state);
		app.getStatusBar().setNoticeTimer((delay, expiry) -> () -> { });
		return app;
	}

	private static void copy(AppGeoCeDG app, String... labels) {
		List<GeoElement> selection = new ArrayList<>();
		for (String label : labels) {
			selection.add(G9U1TestApp.lookup(app, label));
		}
		app.getSelectionManager().setSelectedGeos(new ArrayList<>(selection));
		app.getGlobalKeyDispatcher().dispatchKeyEvent(ctrl(app, KeyEvent.VK_C));
	}

	private static void paste(AppGeoCeDG app) {
		app.getGlobalKeyDispatcher().dispatchKeyEvent(ctrl(app, KeyEvent.VK_V));
	}

	private static String notice(AppGeoCeDG app) {
		JLabel label = app.getStatusBar().getSegment(GeoCeDGStatusBar.PASTE_NOTICE_SEGMENT);
		return label.isVisible() ? label.getText() : null;
	}

	/** File &gt; New in the same window, then the document units of the new document. */
	private static void newDocument(AppGeoCeDG app, UnitState state) {
		app.setSaved();
		app.fileNew();
		assertTrue(app.getKernel().getConstruction().getGeoSetConstructionOrder().isEmpty());
		assertNull(notice(app), "a document transition clears the notice");
		app.getDocumentUnits().replace(state);
	}

	@Test
	void provenanceIsRecordedOnlyWhenTheBufferIsReplaced() {
		AppGeoCeDG app = app(UnitState.of(UnitToken.MM, null, null));
		G9U1TestApp.eval(app, "P=(1,2)");
		GeoCeDGCopyPaste copyPaste = (GeoCeDGCopyPaste) app.getCopyPaste();
		assertNull(copyPaste.getProvenance());
		copy(app, "P");
		assertEquals(UnitState.of(UnitToken.MM, null, null), copyPaste.getProvenance());
		app.getDocumentUnits().replace(UnitState.of(UnitToken.CM, null, null));
		app.getSelectionManager().clearSelectedGeos();
		app.getGlobalKeyDispatcher().dispatchKeyEvent(ctrl(app, KeyEvent.VK_C));
		assertEquals(UnitState.of(UnitToken.MM, null, null), copyPaste.getProvenance(),
				"an empty selection keeps the old buffer and its provenance");
		copyPaste.clearClipboard();
		assertNull(copyPaste.getProvenance());
		assertFalse(app.getXML().contains("provenance"));
	}

	@Test
	void crossDocumentPasteNoticesOnlyDifferentPhysicalMeanings() throws Exception {
		AppGeoCeDG app = app(UnitState.of(UnitToken.MM, null, null));
		G9U1TestApp.eval(app, "P=(1,2)");
		copy(app, "P");
		paste(app);
		assertNull(notice(app), "same document, same meaning");

		newDocument(app, UnitState.of(UnitToken.CM, null, null));
		String unitsBefore = app.getDocumentUnits().getState().toString();
		paste(app);
		assertEquals(app.layerText("Units.PasteNotice", "mm", "cm"), notice(app));
		GeoElement pasted = app.getKernel().getConstruction().getGeoSetConstructionOrder()
				.first();
		assertEquals("(1, 2)", pasted.toValueString(StringTemplate.defaultTemplate),
				"numbers are copied unchanged, never scaled");
		assertEquals(unitsBefore, app.getDocumentUnits().getState().toString(),
				"the target unit never changes");
		assertFalse(app.getXML().contains("Pasted from"), "the notice is not document state");

		newDocument(app, UnitState.of(UnitToken.USM, null,
				UsmDefinition.of(0.001, "milli", "mmm")));
		paste(app);
		assertNull(notice(app), "usm with k = fb(mm) has the same physical meaning");

		newDocument(app, UnitState.EMPTY);
		paste(app);
		assertNull(notice(app), "an unspecified target never gets a notice");

		newDocument(app, UnitState.of(UnitToken.M, UnitToken.MM, null));
		paste(app);
		assertEquals(app.layerText("Units.PasteNotice", "mm", "m"), notice(app),
				"physical meaning follows the construction unit, not the presentation");
	}

	@Test
	void theNoticeIsNeitherAnUndoPointNorAnEdit() throws Exception {
		AppGeoCeDG app = app(UnitState.of(UnitToken.CM, null, null));
		app.setUndoActive(true);
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
		try (UndoManagerD.PreparedUndoBaseline baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		app.setSaved();
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);
		String xml = app.getXML();
		app.unitPasteCompleted(UnitState.of(UnitToken.MM, null, null), app);
		assertEquals(app.layerText("Units.PasteNotice", "mm", "cm"), notice(app));
		assertFalse(stored.await(2, TimeUnit.SECONDS), "the notice is no undo point");
		assertTrue(app.isSaved(), "the notice does not make the document unsaved");
		assertEquals(xml, app.getXML());
	}

	@Test
	void unspecifiedSourcesAndUsmFactorsAreComparedByMetreFactor() {
		AppGeoCeDG app = app(UnitState.EMPTY);
		G9U1TestApp.eval(app, "P=(1,2)");
		copy(app, "P");
		newDocument(app, UnitState.of(UnitToken.MM, null, null));
		paste(app);
		assertNull(notice(app), "an unspecified source never gets a notice");

		app.getDocumentUnits().replace(UnitState.of(UnitToken.USM, null,
				UsmDefinition.of(0.0254, "inch", "in")));
		copy(app, "P");
		newDocument(app, UnitState.of(UnitToken.USM, null,
				UsmDefinition.of(0.3048, "foot", "ft")));
		paste(app);
		assertEquals(app.layerText("Units.PasteNotice", "in (1 usm = 0.0254 m)",
				"ft (1 usm = 0.3048 m)"), notice(app));
		paste(app);
		assertEquals(app.layerText("Units.PasteNotice", "in (1 usm = 0.0254 m)",
				"ft (1 usm = 0.3048 m)"), notice(app), "a later paste keeps the provenance");
	}

	@Test
	void insertFileCarriesTheInsertedDocumentsProvenance() {
		AppGeoCeDG source = app(UnitState.of(UnitToken.M, null, null));
		G9U1TestApp.eval(source, "Q=(5,5)");
		AppGeoCeDG target = app(UnitState.of(UnitToken.MM, null, null));
		G9U1TestApp.eval(target, "R=(1,1)");
		((GeoCeDGCopyPaste) target.getCopyPaste()).insertFrom(source, target,
				new LinkedHashSet<>(), false);
		assertEquals(target.layerText("Units.PasteNotice", "m", "mm"), notice(target));
		assertEquals(UnitState.of(UnitToken.MM, null, null),
				target.getDocumentUnits().getState());
		assertEquals("(5, 5)", G9U1TestApp.lookup(target, "Q")
				.toValueString(StringTemplate.defaultTemplate));
		paste(target);
		assertEquals(target.layerText("Units.PasteNotice", "m", "mm"), notice(target),
				"Ctrl+V of the same buffer keeps the Insert File provenance");
	}

	@Test
	void twoWindowsNeverShareABufferOrItsProvenance() {
		AppGeoCeDG first = app(UnitState.of(UnitToken.MM, null, null));
		AppGeoCeDG second = app(UnitState.of(UnitToken.CM, null, null));
		G9U1TestApp.eval(first, "P=(1,2)");
		G9U1TestApp.eval(second, "Q=(3,3)");
		assertNotSame(first.getCopyPaste(), second.getCopyPaste());
		copy(first, "P");
		int before = second.getKernel().getConstruction().getGeoSetConstructionOrder().size();
		paste(second);
		assertEquals(before, second.getKernel().getConstruction().getGeoSetConstructionOrder()
				.size(), "no cross-window paste route exists");
		assertNull(notice(second));
		assertNull(((GeoCeDGCopyPaste) second.getCopyPaste()).getProvenance());
	}

	@Test
	void apiAndScriptInsertionCarryNoProvenance() {
		AppGeoCeDG app = app(UnitState.of(UnitToken.MM, null, null));
		app.getGgbApi().evalXML("<element type=\"point\" label=\"S\"><coords x=\"1\""
				+ " y=\"1\" z=\"1\"/></element>");
		G9U1TestApp.eval(app, "T=(2,2)");
		assertNull(notice(app));
		assertNull(((GeoCeDGCopyPaste) app.getCopyPaste()).getProvenance());
	}
}
