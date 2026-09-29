/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersectLines;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoMidpoint;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R3-C1-U1 through the real Desktop entry points: Ctrl+C and Ctrl+V of an
 * ordinary construction whose predecessor command has an axis input. The Desktop
 * same-window fast paste and the full-payload paste both keep the pasted geo
 * dependent, and no geo becomes an identity participant.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9bR3C1U1DesktopClipboardTest {

	@Test
	void ctrlCAndCtrlVUseTheSameWindowFastPasteAndKeepTheDependency() throws Exception {
		AppGeoCeDG app = ordinaryAxisConstruction();
		UndoManagerD undo = baseline(app);
		select(app, "M");
		Object stateAtCopy = undo.getCurrentUndoInfo();
		app.getGlobalKeyDispatcher().dispatchKeyEvent(ctrl(app, KeyEvent.VK_C));

		assertSame(stateAtCopy, undo.getCurrentUndoInfo(),
				"an unchanged undo state selects the same-window fast paste");
		paste(app);

		assertPastedDependency(app);
	}

	@Test
	void ctrlVAfterAnotherUndoPointUsesTheFullPayloadAndKeepsTheDependency()
			throws Exception {
		AppGeoCeDG app = ordinaryAxisConstruction();
		UndoManagerD undo = baseline(app);
		select(app, "M");
		Object stateAtCopy = undo.getCurrentUndoInfo();
		app.getGlobalKeyDispatcher().dispatchKeyEvent(ctrl(app, KeyEvent.VK_C));
		CountDownLatch stored = storeLatch(undo);
		G9U1TestApp.eval(app, "Z=(7,7)");
		app.storeUndoInfo();
		assertTrue(stored.await(5, TimeUnit.SECONDS));
		assertNotSame(stateAtCopy, undo.getCurrentUndoInfo(),
				"another undo point forces the full-payload route");

		paste(app);

		assertPastedDependency(app);
	}

	private static void paste(AppGeoCeDG app) {
		app.getGlobalKeyDispatcher().dispatchKeyEvent(ctrl(app, KeyEvent.VK_V));
	}

	private static void assertPastedDependency(AppGeoCeDG app) {
		Construction cons = app.getKernel().getConstruction();
		AlgoElement line = G9U1TestApp.lookup(app, "f_{1}").getParentAlgorithm();
		assertInstanceOf(AlgoLinePointLine.class, line);
		assertSame(G9U1TestApp.lookup(app, "P_{1}"), line.getInput(0));
		assertSame(cons.getXAxis(), line.getInput(1));
		AlgoElement intersection = G9U1TestApp.lookup(app, "D_{1}").getParentAlgorithm();
		assertInstanceOf(AlgoIntersectLines.class, intersection);
		assertSame(G9U1TestApp.lookup(app, "f_{1}"), intersection.getInput(0));
		assertSame(cons.getYAxis(), intersection.getInput(1));
		AlgoElement midpoint = G9U1TestApp.lookup(app, "M_{1}").getParentAlgorithm();
		assertInstanceOf(AlgoMidpoint.class, midpoint,
				"the pasted geo must not become free geometry");
		assertSame(G9U1TestApp.lookup(app, "D_{1}"), midpoint.getInput(0));
		assertSame(G9U1TestApp.lookup(app, "P_{1}"), midpoint.getInput(1));
		SpatialIdentityRegistry registry = cons.getSpatialIdentityRegistry();
		assertTrue(registry.isEmpty(), "no durable identity may be synthesized");
		assertFalse(app.isBlockUpdateScripts());
		GeoPoint pasted = (GeoPoint) G9U1TestApp.lookup(app, "M_{1}");
		assertEquals(0.5, pasted.getInhomX(), 1e-12);
		assertEquals(1.0, pasted.getInhomY(), 1e-12);
	}

	private static AppGeoCeDG ordinaryAxisConstruction() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getKernel().setContinuous(false);
		for (String command : new String[] {"P=(1,1)", "f=Line(P,xAxis)",
				"D=Intersect(f,yAxis)", "M=Midpoint(D,P)"}) {
			G9U1TestApp.eval(app, command);
		}
		return app;
	}

	private static void select(AppGeoCeDG app, String label) {
		app.getSelectionManager().setSelectedGeos(
				new ArrayList<>(List.of(G9U1TestApp.lookup(app, label))));
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

	private static KeyEvent ctrl(AppGeoCeDG app, int keyCode) {
		return new KeyEvent(app.getEuclidianView1().getJPanel(), KeyEvent.KEY_PRESSED,
				System.currentTimeMillis(), InputEvent.CTRL_DOWN_MASK, keyCode,
				KeyEvent.CHAR_UNDEFINED);
	}
}
