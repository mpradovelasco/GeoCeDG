/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.PreG9BR4SplineV2AuthoringTest.click;
import static org.geocedg.desktop.PreG9BR4SplineV2AuthoringTest.count;
import static org.geocedg.desktop.PreG9BR4SplineV2AuthoringTest.hit;
import static org.geocedg.desktop.PreG9BR4SplineV2AuthoringTest.size;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Action;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.algos.AlgoDependentList;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R4 focal contract for Create List from Selection: an ordinary host
 * GeoList in exact click order, host toggle deduplication, mixed members, one
 * undo point, cancel, and inherited downstream, deletion and redefinition.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR4OrderedListToolTest {

	@Test
	void theToolIsAGeoCeDGModeWithItsOwnLocalizedText() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		GeoCeDGProfile.ActionDefinition definition =
				GeoCeDGProfile.getAction("construction.list-from-selection");
		assertEquals("upstream-mode", definition.kind());
		assertEquals(EuclidianConstants.MODE_ORDERED_LIST, definition.mode().intValue());
		assertEquals(140, EuclidianConstants.MODE_ORDERED_LIST);
		assertTrue(definition.features().isEmpty(), "no Locus V2 feature requirement");
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
		assertEquals("Create List from Selection",
				registry.get("construction.list-from-selection").getValue(Action.NAME));
		assertTrue(app.getToolHelp(EuclidianConstants.MODE_ORDERED_LIST)
				.contains("in that order"));
		app.setLocale(new Locale("es"));
		assertEquals("Crear lista desde la selección",
				app.getToolName(EuclidianConstants.MODE_ORDERED_LIST));
		registry.invoke("construction.list-from-selection", new ActionEvent(this, 1, "t"));
		assertEquals(EuclidianConstants.MODE_ORDERED_LIST, app.getMode());
	}

	@Test
	void exactClickOrderBecomesTheOrderOfAnOrdinaryDependentGeoList() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement a = eval(app, "A=(0,0)");
		GeoElement b = eval(app, "B=(1,0)");
		GeoElement c = eval(app, "C=(2,0)");
		GeoCeDGEuclidianController controller = listMode(app);
		AtomicInteger commits = new AtomicInteger();
		int before = size(app);
		// Click order is neither construction nor label order.
		for (GeoElement geo : new GeoElement[] {c, a, b}) {
			assertFalse(click(controller, geo, commits));
		}
		assertEquals(before, size(app));
		assertTrue(click(controller, c, commits));

		assertEquals(1, commits.get(), "one undo point");
		GeoList list = onlyList(app);
		assertSame(GeoList.class, list.getClass(), "no CeDG-specific list type");
		assertInstanceOf(AlgoDependentList.class, list.getParentAlgorithm());
		assertTrue(list.isLabelSet());
		assertEquals(List.of(c, a, b), members(list));
	}

	@Test
	void repeatedSelectionIsTheHostToggleAndNeverARepeatedMember() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement a = eval(app, "A=(0,0)");
		GeoElement b = eval(app, "B=(1,0)");
		GeoElement c = eval(app, "C=(2,0)");
		GeoCeDGEuclidianController controller = listMode(app);
		AtomicInteger commits = new AtomicInteger();
		click(controller, a, commits);
		assertFalse(click(controller, a, commits), "one member cannot finish");
		assertTrue(app.getSelectionManager().getSelectedGeoList().isEmpty(),
				"it was deselected");
		click(controller, a, commits);
		click(controller, b, commits);
		click(controller, c, commits);
		click(controller, b, commits);
		assertTrue(click(controller, a, commits));
		assertEquals(List.of(a, c), members(onlyList(app)));
		assertEquals(1, commits.get());
	}

	@Test
	void mixedMemberTypesAreAllowed() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement point = eval(app, "A=(0,0)");
		GeoElement segment = eval(app, "s=Segment((1,0),(2,1))");
		GeoElement number = eval(app, "n=5");
		GeoElement circle = eval(app, "c=Circle((0,0),1)");
		GeoCeDGEuclidianController controller = listMode(app);
		AtomicInteger commits = new AtomicInteger();
		for (GeoElement geo : new GeoElement[] {number, circle, point, segment}) {
			click(controller, geo, commits);
		}
		assertTrue(click(controller, number, commits));
		assertEquals(List.of(number, circle, point, segment), members(onlyList(app)));
	}

	@Test
	void escapeAndToolChangeCancelWithoutConstructionOrUndo() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement a = eval(app, "A=(0,0)");
		GeoElement b = eval(app, "B=(1,0)");
		UndoManagerD undo = baseline(app);
		final int history = undo.getHistorySize();
		final int before = size(app);
		GeoCeDGEuclidianController controller = listMode(app);
		click(controller, a, new AtomicInteger());
		click(controller, b, new AtomicInteger());
		app.setMoveMode();
		assertTrue(app.getSelectionManager().getSelectedGeoList().isEmpty());
		app.setMode(EuclidianConstants.MODE_ORDERED_LIST);
		click(controller, a, new AtomicInteger());
		click(controller, b, new AtomicInteger());
		app.setMode(EuclidianConstants.MODE_SPLINE_V2);
		assertTrue(app.getSelectionManager().getSelectedGeoList().isEmpty());
		assertEquals(before, size(app));
		assertEquals(0, count(app, GeoList.class));
		assertEquals(history, undo.getHistorySize());
	}

	@Test
	void undoAndRedoRemoveAndRestoreTheCommittedList() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement a = eval(app, "A=(0,0)");
		GeoElement b = eval(app, "B=(1,0)");
		UndoManagerD undo = baseline(app);
		int history = undo.getHistorySize();
		GeoCeDGEuclidianController controller = listMode(app);
		controller.processMode(hit(b), false, false);
		controller.processMode(hit(a), false, false);
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);
		assertTrue(controller.processMode(hit(b), false, false));
		assertTrue(stored.await(5, TimeUnit.SECONDS));
		assertEquals(history + 1, undo.getHistorySize());
		String label = onlyList(app).getLabelSimple();

		app.getKernel().undo();
		assertNull(app.getKernel().lookupLabel(label));
		app.getKernel().redo();
		GeoList restored = (GeoList) G9U1TestApp.lookup(app, label);
		assertEquals(List.of(G9U1TestApp.lookup(app, "B"), G9U1TestApp.lookup(app, "A")),
				members(restored));
	}

	@Test
	void theListFeedsSplineV2AsAnOrdinaryDagInput() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoPoint[] points = PreG9BR4SplineV2AuthoringTest.fourPoints(app);
		GeoCeDGEuclidianController controller = listMode(app);
		for (GeoPoint point : points) {
			click(controller, point, new AtomicInteger());
		}
		click(controller, points[0], new AtomicInteger());
		GeoList list = onlyList(app);
		GeoLocusV2 spline = (GeoLocusV2) eval(app, "S=SplineV2(" + list.getLabelSimple()
				+ ",4)");
		assertTrue(spline.isDefined());
		assertSame(list, spline.getParentAlgorithm().getInput(0));
		GeoPoint end = (GeoPoint) eval(app, "E=Point(S,\"spline-v2/main\",1)");
		assertEquals(1, end.getInhomY(), 1e-9);
		eval(app, "D=(3,2)");
		assertTrue(spline.isDefined());
		assertEquals(2, end.getInhomY(), 1e-9, "the DAG recomputed the spline");
	}

	@Test
	void memberRedefinitionAndDeletionFollowTheHostDependentList() {
		AppGeoCeDG tool = G9U1TestApp.create();
		GeoElement a = eval(tool, "A=(0,0)");
		GeoElement b = eval(tool, "B=(1,0)");
		GeoElement c = eval(tool, "C=(2,0)");
		GeoCeDGEuclidianController controller = listMode(tool);
		for (GeoElement geo : new GeoElement[] {a, b, c}) {
			click(controller, geo, new AtomicInteger());
		}
		click(controller, a, new AtomicInteger());
		String label = onlyList(tool).getLabelSimple();

		AppGeoCeDG typed = G9U1TestApp.create();
		eval(typed, "A=(0,0)");
		eval(typed, "B=(1,0)");
		eval(typed, "C=(2,0)");
		eval(typed, label + "={A,B,C}");

		for (AppGeoCeDG app : new AppGeoCeDG[] {tool, typed}) {
			// Redefining a member keeps it in place in the ordinary dependent list.
			eval(app, "B=Midpoint(A,C)");
			GeoList list = (GeoList) G9U1TestApp.lookup(app, label);
			assertEquals(3, list.size());
			assertSame(G9U1TestApp.lookup(app, "B"), list.get(1));
			assertEquals(1, ((GeoPoint) list.get(1)).getInhomX(), 1e-12);
			G9U1TestApp.lookup(app, "C").remove();
		}
		assertEquals(typed.getKernel().lookupLabel(label) == null,
				tool.getKernel().lookupLabel(label) == null,
				"deletion follows the host dependent list");
	}

	// ------------------------------------------------------------------------ helpers

	private static GeoCeDGEuclidianController listMode(AppGeoCeDG app) {
		return PreG9BR4SplineV2AuthoringTest.splineMode(app,
				EuclidianConstants.MODE_ORDERED_LIST);
	}

	private static GeoList onlyList(AppGeoCeDG app) {
		List<GeoList> lists = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().stream().filter(GeoList.class::isInstance)
				.map(GeoList.class::cast).toList();
		assertEquals(1, lists.size());
		return lists.get(0);
	}

	private static List<GeoElement> members(GeoList list) {
		GeoElement[] result = new GeoElement[list.size()];
		for (int i = 0; i < list.size(); i++) {
			result[i] = list.get(i);
		}
		return List.of(result);
	}

	private static UndoManagerD baseline(AppGeoCeDG app) throws Exception {
		app.setUndoActive(true);
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction()
				.getUndoManager();
		try (UndoManagerD.PreparedUndoBaseline baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		return undo;
	}
}
