/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersectLines;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.desktop.util.CopyPasteD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R3-C1 through the real Desktop entry points: Ctrl+C and Ctrl+V reach
 * the key dispatcher, and Insert File with overwrite shares the atomic import.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9bR3C1DesktopClipboardTest {

	@Test
	void ctrlCAndCtrlVCopyTheAxisWitnessWithItsCompleteClosure() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		axisWitness(app);
		UndoManagerD undo = baseline(app);
		int history = undo.getHistorySize();
		Construction cons = app.getKernel().getConstruction();
		final SpatialIdentityRegistry registry = cons.getSpatialIdentityRegistry();
		app.getSelectionManager().setSelectedGeos(
				new ArrayList<>(List.of(G9U1TestApp.lookup(app, "a"))));

		app.getGlobalKeyDispatcher().dispatchKeyEvent(ctrl(app, KeyEvent.VK_C));
		CountDownLatch stored = storeLatch(undo);
		app.getGlobalKeyDispatcher().dispatchKeyEvent(ctrl(app, KeyEvent.VK_V));

		assertTrue(stored.await(5, TimeUnit.SECONDS), "the paste stores one undo point");
		assertEquals(history + 1, undo.getHistorySize());
		assertFalse(app.isBlockUpdateScripts());
		AlgoElement line = G9U1TestApp.lookup(app, "f_{1}").getParentAlgorithm();
		assertInstanceOf(AlgoLinePointLine.class, line);
		assertSame(G9U1TestApp.lookup(app, "C_{1}"), line.getInput(0));
		assertSame(cons.getXAxis(), line.getInput(1));
		AlgoElement intersection = G9U1TestApp.lookup(app, "D_{1}").getParentAlgorithm();
		assertInstanceOf(AlgoIntersectLines.class, intersection);
		assertSame(G9U1TestApp.lookup(app, "f_{1}"), intersection.getInput(0));
		assertSame(cons.getYAxis(), intersection.getInput(1));
		AlgoElement locus = G9U1TestApp.lookup(app, "a_{1}").getParentAlgorithm();
		assertSame(G9U1TestApp.lookup(app, "D_{1}"), locus.getInput(0));
		for (String label : List.of("c", "C", "D", "a")) {
			PersistentGeoId source = registry.getPersistentGeoId(
					G9U1TestApp.lookup(app, label));
			PersistentGeoId copy = registry.getPersistentGeoId(
					G9U1TestApp.lookup(app, label + "_{1}"));
			assertNotNull(source, label);
			assertNotNull(copy, label);
			assertNotEquals(source, copy, label);
			assertEquals(source, registry.getGeoRecord(copy).getCopySourceId(), label);
		}
		assertNull(registry.getPersistentGeoId(cons.getXAxis()));
	}

	@Test
	void aFailedInsertAfterTheOverwriteRestoresTheOverwrittenObject() throws Exception {
		AppGeoCeDG from = G9U1TestApp.create();
		axisWitness(from);
		AppGeoCeDG to = G9U1TestApp.create();
		to.getKernel().setContinuous(false);
		final GeoElement overwritten = G9U1TestApp.eval(to, "A=(9,9)");
		G9U1TestApp.eval(to, "Q=(5,5)");
		UndoManagerD undo = baseline(to);
		Kernel kernel = to.getKernel();
		final Construction cons = kernel.getConstruction();
		String xml = to.getXML();
		int history = undo.getHistorySize();
		LinkedHashSet<String> duplicates = new LinkedHashSet<>(List.of("A", "Missing"));
		CountDownLatch stored = storeLatch(undo);

		// "Missing" makes the overwrite mutation fail after it has removed A.
		new CopyPasteD().insertFrom(from, to, duplicates, true);

		assertEquals(xml, to.getXML());
		assertFalse(stored.await(2, TimeUnit.SECONDS), "a failed insert stores no undo");
		assertEquals(history, undo.getHistorySize());
		assertFalse(to.isBlockUpdateScripts());
		assertFalse(kernel.getLoadingMode());
		assertTrue(kernel.isNotifyViewsActive());
		assertFalse(cons.isFileLoading());
		GeoElement restored = G9U1TestApp.lookup(to, "A");
		assertNotSame(overwritten, restored, "the rollback rebuilds the snapshot");
		assertEquals("(9, 9)", restored.toValueString(StringTemplate.defaultTemplate));
		assertNull(to.getKernel().lookupLabel("a"));
		assertEquals(0, cons.getSpatialIdentityRegistry().getRecords().size());
	}

	/** @return the undo manager after a synchronous baseline of the current state */
	private static UndoManagerD baseline(AppGeoCeDG app) throws Exception {
		app.setUndoActive(true);
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction()
				.getUndoManager();
		try (UndoManagerD.PreparedUndoBaseline baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		return undo;
	}

	private static CountDownLatch storeLatch(UndoManagerD undo) {
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);
		return stored;
	}

	private static void axisWitness(AppGeoCeDG app) {
		app.getKernel().setContinuous(false);
		for (String command : new String[] {"A=(0,0)", "c=Circle(A,1)", "C=Point(c)",
				"f=Line(C,xAxis)", "D=Intersect(f,yAxis)", "a=LocusV2(D,C)"}) {
			G9U1TestApp.eval(app, command);
		}
	}

	private static KeyEvent ctrl(AppGeoCeDG app, int keyCode) {
		return new KeyEvent(app.getEuclidianView1().getJPanel(), KeyEvent.KEY_PRESSED,
				System.currentTimeMillis(), InputEvent.CTRL_DOWN_MASK, keyCode,
				KeyEvent.CHAR_UNDEFINED);
	}
}
