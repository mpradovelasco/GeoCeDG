/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geocedg.desktop.GeoCeDGLineReferenceMemory.Slot;
import org.geocedg.desktop.GeoCeDGLineReferenceMemory.State;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianController;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.event.AbstractEvent;
import org.geogebra.common.geogebra3D.euclidianFor3D.EuclidianControllerFor3DCompanion;
import org.geogebra.common.kernel.algos.AlgoAnglePoints;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoLinePointVector;
import org.geogebra.common.kernel.algos.AlgoOrthoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoOrthoLinePointVector;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.algos.AlgoRotatePoint;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.kernel.kernelND.GeoSegmentND;
import org.geogebra.common.main.App;
import org.geogebra.common.main.DialogManager;
import org.geogebra.common.main.error.ErrorHelper;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.euclidian.event.MouseEventD;
import org.geogebra.desktop.geogebra3D.euclidianFor3D.EuclidianControllerFor3DD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.dialog.DialogManagerD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R6-plus-F2: Parallel/Perpendicular session memory and the hidden Angle with
 * Given Size angle (geocedg/specs/ui/authoring-memory.md). Gestures drive the real
 * Graphics View 1 controller with press/release events.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusF2AuthoringMemoryTest {

	private static final double[] ON_L = {0, -4};
	private static final double[] ON_M = {0, 4.5};
	private static final double[] P = {-2, 1};
	private static final double[] Q = {2, 0};
	private static final double[] R = {4, 3};
	private static final double[] S = {-1, 3};
	private static final double[] EMPTY = {-3, -1};

	// ------------------------------------------------------------------ harness

	private static GeoElementND[] eval(AppGeoCeDG app, String input) {
		GeoElementND[] result = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommandNoExceptionHandling(input, false, ErrorHelper.silent(),
						false, null);
		assertNotNull(result, input);
		assertTrue(result.length > 0, input);
		return result;
	}

	private static GeoElement lookup(AppGeoCeDG app, String label) {
		return app.getKernel().lookupLabel(label);
	}

	private static GeoCeDGEuclidianController controller(AppGeoCeDG app) {
		return (GeoCeDGEuclidianController) app.getEuclidianView1().getEuclidianController();
	}

	private static GeoCeDGLineReferenceMemory memory(AppGeoCeDG app) {
		return controller(app).getReferenceMemory();
	}

	private static AppGeoCeDG scene() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = app.getEuclidianView1();
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(800, 600);
			view.updateSize();
			view.setCoordSystem(400, 300, 40, 40);
		});
		view.setShowAxes(false, false);
		view.showGrid(false);
		eval(app, "A=(-6,-4)");
		eval(app, "B=(6,-4)");
		eval(app, "l=Line(A,B)");
		eval(app, "C=(-6,4)");
		eval(app, "D=(6,5)");
		eval(app, "m=Line(C,D)");
		eval(app, "P=(-2,1)");
		eval(app, "Q=(2,0)");
		eval(app, "R=(4,3)");
		eval(app, "S=(-1,3)");
		eval(app, "v=Vector((1,2))");
		eval(app, "s=Segment((-8,-6),(-4,-6))");
		eval(app, "ry=Ray((-9,6.5),(-5,6.5))");
		eval(app, "g(x)=x+9");
		eval(app, "h(x)=0.5x^2-7");
		eval(app, "k(x)=-6.8");
		eval(app, "tri=Polygon((3,-2),(7,-2),(7,2))");
		app.storeUndoInfo();
		return app;
	}

	private static AbstractEvent event(AppGeoCeDG app, double[] at, int id, int button) {
		EuclidianView v = app.getEuclidianView1();
		return MouseEventD.wrapEvent(new MouseEvent(((EuclidianViewD) v).getJPanel(), id, 1, 0,
				v.toScreenCoordX(at[0]), v.toScreenCoordY(at[1]), 1, false, button));
	}

	private static void click(AppGeoCeDG app, double[] at) {
		AbstractEvent e = event(app, at, MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON1);
		controller(app).wrapMousePressed(e);
		controller(app).wrapMouseReleased(e);
	}

	/** Two clicks at one place, separated beyond the inherited double-click delay. */
	private static void clickAgainLater(AppGeoCeDG app, double[] at) throws Exception {
		Thread.sleep(EuclidianConstants.DOUBLE_CLICK_DELAY + 300L);
		click(app, at);
	}

	private static GeoElement created(AppGeoCeDG app) {
		ArrayList<GeoElement> created = controller(app).getJustCreatedGeos();
		assertEquals(1, created.size(), "one created object");
		return created.get(0);
	}

	private static void assertConstruction(GeoElement line, Class<?> algorithm,
			GeoElement point, GeoElement reference) {
		AlgoElement algo = line.getParentAlgorithm();
		assertNotNull(algo);
		assertSame(algorithm, algo.getClass());
		assertSame(point, algo.getInput(0));
		assertSame(reference, algo.getInput(1));
		assertTrue(line.isDefined());
		assertTrue(line.isLabelSet());
	}

	private static int size(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getGeoSetConstructionOrder().size();
	}

	private static List<String> selection(AppGeoCeDG app) {
		List<String> labels = new ArrayList<>();
		for (GeoElement geo : app.getSelectionManager().getSelectedGeos()) {
			labels.add(geo.getLabelSimple());
		}
		return labels;
	}

	private static void redefine(AppGeoCeDG app, GeoElement geo, String definition) {
		app.getKernel().getAlgebraProcessor().changeGeoElement(geo, definition, true, false,
				ErrorHelper.silent(), null);
	}

	private static UndoManagerD undo(AppGeoCeDG app) {
		return PreG9BR6PlusA1LayerWorkspaceTest.undo(app);
	}

	private static DialogManagerD installDialogSpy(AppGeoCeDG app) throws Exception {
		Field field = GuiManagerD.class.getDeclaredField("dialogManager");
		field.setAccessible(true);
		DialogManagerD spied = spy((DialogManagerD) field.get(app.getGuiManager()));
		field.set(app.getGuiManager(), spied);
		return spied;
	}

	/** The spy answers the dialog request with the dialog's own confirm call. */
	private static void confirmAngleDialogWith(AppGeoCeDG app, DialogManagerD dialogs,
			String size, boolean clockwise, AtomicInteger requests) {
		doAnswer(call -> {
			requests.incrementAndGet();
			DialogManager.createAngleFixed(app.getKernel(), size, clockwise,
					app.getErrorHandler(), call.getArgument(1), call.getArgument(2), null,
					call.getArgument(4));
			return null;
		}).when(dialogs).showNumberInputDialogAngleFixed(anyString(),
				any(GeoSegmentND[].class), any(GeoPointND[].class), any(GeoElement[].class),
				any(EuclidianController.class));
	}

	// ------------------------------------------------------------------ target A

	@Test
	void parallelPointFirstReusesEveryAdmittedReference() throws Exception {
		Object[][] references = {{"l", ON_L, AlgoLinePointLine.class},
				{"s", new double[] {-6, -6}, AlgoLinePointLine.class},
				{"ry", new double[] {-7, 6.5}, AlgoLinePointLine.class},
				{"i", new double[] {7, 0}, AlgoLinePointLine.class},
				{"v", new double[] {0.5, 1}, AlgoLinePointVector.class},
				{"g", new double[] {-8, 1}, AlgoLinePointLine.class}};
		for (Object[] reference : references) {
			AppGeoCeDG app = scene();
			app.setMode(EuclidianConstants.MODE_PARALLEL);
			GeoElement ref = lookup(app, (String) reference[0]);
			assertEquals(State.EMPTY, memory(app).getState(Slot.PARALLEL));
			click(app, (double[]) reference[1]);
			click(app, P);
			assertConstruction(created(app), (Class<?>) reference[2], lookup(app, "P"), ref);
			assertSame(ref, memory(app).peek(Slot.PARALLEL), (String) reference[0]);
			click(app, Q);
			assertConstruction(created(app), (Class<?>) reference[2], lookup(app, "Q"), ref);
			click(app, R);
			assertConstruction(created(app), (Class<?>) reference[2], lookup(app, "R"), ref);
			assertEquals(List.of(), selection(app), "an injected reference never stays selected");
		}
	}

	@Test
	void perpendicularPointFirstReusesEveryAdmittedReference() throws Exception {
		Object[][] references = {{"l", ON_L, AlgoOrthoLinePointLine.class},
				{"s", new double[] {-6, -6}, AlgoOrthoLinePointLine.class},
				{"ry", new double[] {-7, 6.5}, AlgoOrthoLinePointLine.class},
				{"i", new double[] {7, 0}, AlgoOrthoLinePointLine.class},
				{"v", new double[] {0.5, 1}, AlgoOrthoLinePointVector.class},
				{"g", new double[] {-8, 1}, AlgoOrthoLinePointLine.class}};
		for (Object[] reference : references) {
			AppGeoCeDG app = scene();
			app.setMode(EuclidianConstants.MODE_ORTHOGONAL);
			GeoElement ref = lookup(app, (String) reference[0]);
			click(app, (double[]) reference[1]);
			click(app, P);
			assertConstruction(created(app), (Class<?>) reference[2], lookup(app, "P"), ref);
			assertSame(ref, memory(app).peek(Slot.PERPENDICULAR));
			click(app, Q);
			assertConstruction(created(app), (Class<?>) reference[2], lookup(app, "Q"), ref);
		}
	}

	@Test
	void pointFirstBeforeAnyReferenceWaitsForAnExplicitReference() throws Exception {
		AppGeoCeDG app = scene();
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		int before = size(app);
		click(app, P);
		assertEquals(before, size(app), "nothing is invented");
		assertEquals(List.of("P"), selection(app));
		click(app, ON_L);
		assertConstruction(created(app), AlgoLinePointLine.class, lookup(app, "P"),
				lookup(app, "l"));
		assertSame(lookup(app, "l"), memory(app).peek(Slot.PARALLEL),
				"point-then-reference also commits the reference actually used");
	}

	@Test
	void referenceFirstReplacesTheSlotAfterSuccess() throws Exception {
		AppGeoCeDG app = scene();
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		click(app, ON_L);
		click(app, P);
		click(app, ON_M);
		assertSame(lookup(app, "l"), memory(app).peek(Slot.PARALLEL),
				"a selected reference does not replace the slot before success");
		click(app, Q);
		assertConstruction(created(app), AlgoLinePointLine.class, lookup(app, "Q"),
				lookup(app, "m"));
		assertSame(lookup(app, "m"), memory(app).peek(Slot.PARALLEL));
		click(app, R);
		assertConstruction(created(app), AlgoLinePointLine.class, lookup(app, "R"),
				lookup(app, "m"));
	}

	@Test
	void slotsAreIndependentAndSurviveToolChangesAndEscape() throws Exception {
		AppGeoCeDG app = scene();
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		click(app, ON_L);
		click(app, P);
		app.setMode(EuclidianConstants.MODE_ORTHOGONAL);
		int before = size(app);
		click(app, Q);
		assertEquals(before, size(app), "the Parallel slot never feeds Perpendicular");
		assertEquals(State.EMPTY, memory(app).getState(Slot.PERPENDICULAR));
		click(app, ON_M);
		assertConstruction(created(app), AlgoOrthoLinePointLine.class, lookup(app, "Q"),
				lookup(app, "m"));
		app.setMoveMode();
		app.setMode(EuclidianConstants.MODE_POINT);
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		click(app, R);
		assertConstruction(created(app), AlgoLinePointLine.class, lookup(app, "R"),
				lookup(app, "l"));
		app.setMode(EuclidianConstants.MODE_ORTHOGONAL);
		click(app, S);
		assertConstruction(created(app), AlgoOrthoLinePointLine.class, lookup(app, "S"),
				lookup(app, "m"));
		assertSame(lookup(app, "l"), memory(app).peek(Slot.PARALLEL));
		assertSame(lookup(app, "m"), memory(app).peek(Slot.PERPENDICULAR));
	}

	@Test
	void emptySpaceWithAndWithoutMemory() throws Exception {
		AppGeoCeDG app = scene();
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		int before = size(app);
		click(app, EMPTY);
		assertEquals(before + 1, size(app), "without memory only the free point");
		assertEquals(1, selection(app).size());
		app.setMoveMode();
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		click(app, ON_L);
		click(app, P);
		UndoManagerD undo = undo(app);
		PreG9BR6PlusA1LayerWorkspaceTest.baseline(undo);
		AtomicInteger stores = new AtomicInteger();
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(() -> {
			stores.incrementAndGet();
			stored.countDown();
		});
		int withMemory = size(app);
		click(app, new double[] {3, -1});
		assertTrue(stored.await(5, TimeUnit.SECONDS));
		Thread.sleep(300);
		assertEquals(1, stores.get(), "one undo point for the point and its line");
		assertEquals(withMemory + 2, size(app));
		GeoElement line = created(app);
		GeoElement point = line.getParentAlgorithm().getInput(0).toGeoElement();
		assertTrue(point.isIndependent());
		assertConstruction(line, AlgoLinePointLine.class, point, lookup(app, "l"));
		String pointLabel = point.getLabelSimple();
		String lineLabel = line.getLabelSimple();
		app.getKernel().undo();
		assertNull(lookup(app, pointLabel));
		assertNull(lookup(app, lineLabel));
	}

	@Test
	void inheritedPointOnPathGestureIsUnchanged() throws Exception {
		AppGeoCeDG app = scene();
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		click(app, ON_L);
		click(app, P);
		click(app, ON_M);
		int before = size(app);
		click(app, new double[] {-4, -4});
		assertEquals(before + 2, size(app), "point on l and the line through it");
		GeoElement line = created(app);
		GeoElement point = line.getParentAlgorithm().getInput(0).toGeoElement();
		assertSame(AlgoPointOnPath.class, point.getParentAlgorithm().getClass());
		assertSame(lookup(app, "l"), point.getParentAlgorithm().getInput(0));
		assertConstruction(line, AlgoLinePointLine.class, point, lookup(app, "m"));
		assertSame(lookup(app, "m"), memory(app).peek(Slot.PARALLEL));

		click(app, ON_M);
		int beforeSame = size(app);
		clickAgainLater(app, ON_M);
		assertEquals(beforeSame + 2, size(app), "same line after the delay: point on it");
		assertSame(AlgoPointOnPath.class, created(app).getParentAlgorithm().getInput(0)
				.toGeoElement().getParentAlgorithm().getClass());
	}

	/** Outcome of a quick double click on {@code m}, with or without a remembered line. */
	private static String doubleClickOutcome(boolean withMemory) throws Exception {
		AppGeoCeDG app = scene();
		DialogManagerD dialogs = installDialogSpy(app);
		List<String> redefined = new ArrayList<>();
		doAnswer(call -> {
			GeoElement target = call.getArgument(0);
			redefined.add(target.getTypeString() + "@" + target.getParentAlgorithm()
					.getClass().getSimpleName() + "(" + target.getParentAlgorithm().getInput(0)
					.getLabelSimple() + ")");
			return null;
		}).when(dialogs).showRedefineDialog(any(GeoElement.class), anyBoolean());
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		if (withMemory) {
			click(app, ON_L);
			click(app, new double[] {-9, -2});
			assertSame(lookup(app, "l"), memory(app).peek(Slot.PARALLEL));
		}
		int before = size(app);
		double[] onM = {3, 4.75};
		click(app, onM);
		click(app, onM);
		if (withMemory) {
			assertSame(lookup(app, "l"), memory(app).peek(Slot.PARALLEL),
					"the double click never writes the memory");
		}
		return "redefine=" + redefined + " mode=" + app.getMode() + " delta="
				+ (size(app) - before);
	}

	@Test
	void inheritedDoubleClickRedefineIsUnchanged() throws Exception {
		String inherited = doubleClickOutcome(false);
		assertTrue(inherited.contains("redefine=[") && !inherited.contains("redefine=[]"),
				inherited);
		assertTrue(inherited.contains("mode=" + EuclidianConstants.MODE_MOVE), inherited);
		assertEquals(inherited, doubleClickOutcome(true));
	}

	@Test
	void previewAndCancellationNeverCreateOrWrite() throws Exception {
		AppGeoCeDG app = scene();
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		click(app, ON_L);
		click(app, P);
		int before = size(app);
		for (double[] at : new double[][] {Q, R, EMPTY, ON_M}) {
			controller(app).wrapMouseMoved(event(app, at, MouseEvent.MOUSE_MOVED,
					MouseEvent.NOBUTTON));
		}
		assertEquals(before, size(app), "preview creates nothing");
		assertSame(lookup(app, "l"), memory(app).peek(Slot.PARALLEL));
		click(app, ON_M);
		app.setMoveMode();
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		assertEquals(List.of(), selection(app), "Escape discards the pending selection");
		assertSame(lookup(app, "l"), memory(app).peek(Slot.PARALLEL),
				"an abandoned reference-first gesture keeps the slot");
		click(app, Q);
		assertConstruction(created(app), AlgoLinePointLine.class, lookup(app, "Q"),
				lookup(app, "l"));
	}

	@Test
	void functionReferencesFollowTheKernelLinearityContract() throws Exception {
		AppGeoCeDG app = scene();
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		int before = size(app);
		click(app, new double[] {5, -6.8});
		click(app, P);
		assertEquals(before, size(app), "a constant function yields no line (kernel route)");
		assertEquals(State.EMPTY, memory(app).getState(Slot.PARALLEL));
		click(app, new double[] {3, -2.5});
		click(app, Q);
		assertEquals(before, size(app), "a non-linear function yields no line");
		assertEquals(State.EMPTY, memory(app).getState(Slot.PARALLEL));

		eval(app, "a=0");
		GeoElement f = eval(app, "f(x)=a x^2+x+8")[0].toGeoElement();
		assertTrue(GeoCeDGLineReferenceMemory.isAdmissible(f));
		assertFalse(GeoCeDGLineReferenceMemory.isAdmissible(lookup(app, "k")));
		assertFalse(GeoCeDGLineReferenceMemory.isAdmissible(lookup(app, "h")));
		app.setMoveMode();
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		click(app, new double[] {-9, -1});
		click(app, P);
		assertConstruction(created(app), AlgoLinePointLine.class, lookup(app, "P"), f);
		assertSame(f, memory(app).peek(Slot.PARALLEL));
		GeoNumeric a = (GeoNumeric) lookup(app, "a");
		a.setValue(1);
		a.updateCascade();
		assertEquals(State.SUSPENDED, memory(app).getState(Slot.PARALLEL));
		int suspended = size(app);
		click(app, R);
		assertEquals(suspended, size(app), "a suspended function is not reused");
		app.setMoveMode();
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		a.setValue(0);
		a.updateCascade();
		assertEquals(State.HELD, memory(app).getState(Slot.PARALLEL));
		click(app, S);
		assertConstruction(created(app), AlgoLinePointLine.class, lookup(app, "S"), f);
	}

	@Test
	void hiddenUndefinedAndHiddenLayerReferencesAreSuspended() throws Exception {
		AppGeoCeDG app = scene();
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		click(app, ON_L);
		click(app, P);
		GeoElement l = lookup(app, "l");
		l.setEuclidianVisible(false);
		l.updateRepaint();
		assertEquals(State.SUSPENDED, memory(app).getState(Slot.PARALLEL));
		int before = size(app);
		click(app, Q);
		assertEquals(before, size(app), "a hidden reference is not reused");
		app.setMoveMode();
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		l.setEuclidianVisible(true);
		l.updateRepaint();
		assertEquals(State.HELD, memory(app).getState(Slot.PARALLEL));

		l.setLayer(5);
		assertTrue(app.getLayerWorkspace().setLayerHidden(5, true));
		assertEquals(State.SUSPENDED, memory(app).getState(Slot.PARALLEL));
		app.getLayerWorkspace().setLayerHidden(5, false);
		assertEquals(State.HELD, memory(app).getState(Slot.PARALLEL));

		l.removeView(App.VIEW_EUCLIDIAN);
		assertEquals(State.SUSPENDED, memory(app).getState(Slot.PARALLEL),
				"not shown in Graphics View 1");
		l.addView(App.VIEW_EUCLIDIAN);
		assertEquals(State.HELD, memory(app).getState(Slot.PARALLEL));

		GeoPoint b = (GeoPoint) lookup(app, "B");
		b.setCoords(-6, -4, 1);
		b.updateCascade();
		assertEquals(State.SUSPENDED, memory(app).getState(Slot.PARALLEL));
		assertSame(l, memory(app).peek(Slot.PARALLEL), "suspension never rebinds");
		b.setCoords(6, -4, 1);
		b.updateCascade();
		assertEquals(State.HELD, memory(app).getState(Slot.PARALLEL));
		click(app, R);
		assertConstruction(created(app), AlgoLinePointLine.class, lookup(app, "R"), l);
	}

	@Test
	void currentnessAcrossRenameRecomputeRedefineAndDelete() throws Exception {
		AppGeoCeDG app = scene();
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		click(app, ON_L);
		click(app, P);
		GeoElement l = lookup(app, "l");
		l.rename("base");
		assertEquals(State.HELD, memory(app).getState(Slot.PARALLEL), "rename keeps the instance");
		GeoPoint b = (GeoPoint) lookup(app, "B");
		b.setCoords(6, -3, 1);
		b.updateCascade();
		assertEquals(State.HELD, memory(app).getState(Slot.PARALLEL), "recompute keeps it");
		redefine(app, l, "Line(A,Q)");
		assertNotSame(l, lookup(app, "base"));
		assertEquals(State.EMPTY, memory(app).getState(Slot.PARALLEL),
				"a replaced object clears the slot");
		assertNull(memory(app).peek(Slot.PARALLEL));

		click(app, ON_M);
		click(app, Q);
		GeoElement m = lookup(app, "m");
		assertSame(m, memory(app).peek(Slot.PARALLEL));
		m.remove();
		assertEquals(State.EMPTY, memory(app).getState(Slot.PARALLEL), "removal clears the slot");
		assertNull(memory(app).peek(Slot.PARALLEL));
		GeoElement recreated = eval(app, "m=Line(C,D)")[0].toGeoElement();
		assertEquals(State.EMPTY, memory(app).getState(Slot.PARALLEL),
				"a recreated label is never adopted");
		int before = size(app);
		click(app, R);
		assertEquals(before, size(app));
		assertNotNull(recreated);
	}

	@Test
	void rebuildsAndDocumentBoundariesClearBothSlots() throws Exception {
		String[] actions = {"undo", "redo", "rebuild", "setXML", "loadXML", "new"};
		for (String action : actions) {
			AppGeoCeDG app = scene();
			UndoManagerD undo = undo(app);
			PreG9BR6PlusA1LayerWorkspaceTest.baseline(undo);
			eval(app, "C2=Point(m)");
			PreG9BR6PlusA1LayerWorkspaceTest.awaitStore(app, undo);
			CountDownLatch toolStores = new CountDownLatch(2);
			undo.addUndoInfoStoredListener(toolStores::countDown);
			app.setMode(EuclidianConstants.MODE_PARALLEL);
			click(app, ON_L);
			click(app, P);
			app.setMode(EuclidianConstants.MODE_ORTHOGONAL);
			click(app, ON_L);
			click(app, Q);
			assertTrue(toolStores.await(5, TimeUnit.SECONDS));
			assertNotNull(memory(app).peek(Slot.PARALLEL));
			assertNotNull(memory(app).peek(Slot.PERPENDICULAR));
			switch (action) {
			case "undo":
				app.getKernel().undo();
				break;
			case "redo":
				app.getKernel().undo();
				memory(app).remember(Slot.PARALLEL, lookup(app, "l"));
				app.getKernel().redo();
				break;
			case "rebuild":
				redefine(app, lookup(app, "m"), "Line(C,R)");
				break;
			case "setXML":
				app.setXML(app.getXML(), true);
				break;
			case "loadXML":
				assertTrue(app.loadXML(app.getXML()));
				break;
			default:
				app.setSaved();
				app.fileNew();
				break;
			}
			SwingUtilities.invokeAndWait(() -> { });
			assertNull(memory(app).peek(Slot.PARALLEL), action);
			assertNull(memory(app).peek(Slot.PERPENDICULAR), action);
			assertEquals(State.EMPTY, memory(app).getState(Slot.PARALLEL), action);
		}
	}

	@Test
	void memoryIsNotAnUndoPointAndNotSerialized() throws Exception {
		AppGeoCeDG app = scene();
		UndoManagerD undo = undo(app);
		PreG9BR6PlusA1LayerWorkspaceTest.baseline(undo);
		app.setMode(EuclidianConstants.MODE_PARALLEL);
		click(app, ON_L);
		click(app, P);
		Thread.sleep(500);
		String held = app.getXML();
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);
		memory(app).clear();
		memory(app).remember(Slot.PARALLEL, lookup(app, "m"));
		memory(app).getState(Slot.PARALLEL);
		assertFalse(stored.await(2, TimeUnit.SECONDS), "memory operations store no undo point");
		memory(app).clear();
		assertEquals(held, app.getXML(), "the document never contains the memory");
		assertFalse(held.contains("referenceMemory"));
	}

	@Test
	void commandsAreUnchangedAndCreateTheSameConstructions() throws Exception {
		AppGeoCeDG app = scene();
		GeoElement byCommand = eval(app, "Line(P,l)")[0].toGeoElement();
		GeoElement perp = eval(app, "PerpendicularLine(P,l)")[0].toGeoElement();
		GeoElement byVector = eval(app, "Line(Q,v)")[0].toGeoElement();
		assertConstruction(byCommand, AlgoLinePointLine.class, lookup(app, "P"), lookup(app, "l"));
		assertSame(AlgoOrthoLinePointLine.class, perp.getParentAlgorithm().getClass());
		assertSame(AlgoLinePointVector.class, byVector.getParentAlgorithm().getClass());
		assertEquals(State.EMPTY, memory(app).getState(Slot.PARALLEL));
		assertEquals(State.EMPTY, memory(app).getState(Slot.PERPENDICULAR));
	}

	// ------------------------------------------------------------------ target B

	private static List<GeoElement> newObjects(AppGeoCeDG app, List<GeoElement> before) {
		List<GeoElement> created = new ArrayList<>();
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (!before.contains(geo)) {
				created.add(geo);
			}
		}
		return created;
	}

	private static List<GeoElement> objects(AppGeoCeDG app) {
		return new ArrayList<>(app.getKernel().getConstruction().getGeoSetConstructionOrder());
	}

	private static GeoAngle single(List<GeoElement> created, Class<?> algorithm) {
		GeoElement found = null;
		for (GeoElement geo : created) {
			if (geo.getParentAlgorithm() != null
					&& geo.getParentAlgorithm().getClass() == algorithm) {
				assertNull(found);
				found = geo;
			}
		}
		assertNotNull(found);
		return found instanceof GeoAngle ? (GeoAngle) found : null;
	}

	private static GeoElement rotatedPoint(List<GeoElement> created) {
		for (GeoElement geo : created) {
			if (geo.getParentAlgorithm() instanceof AlgoRotatePoint) {
				return geo;
			}
		}
		return null;
	}

	@Test
	void angleWithGivenSizeHidesOnlyTheAngleOnBothRoutesAndOrientations() throws Exception {
		for (boolean clockwise : new boolean[] {false, true}) {
			for (boolean segmentRoute : new boolean[] {false, true}) {
				AppGeoCeDG app = scene();
				DialogManagerD dialogs = installDialogSpy(app);
				AtomicInteger requests = new AtomicInteger();
				confirmAngleDialogWith(app, dialogs, "40\u00b0", clockwise, requests);
				UndoManagerD undo = undo(app);
				PreG9BR6PlusA1LayerWorkspaceTest.baseline(undo);
				AtomicInteger stores = new AtomicInteger();
				CountDownLatch stored = new CountDownLatch(1);
				undo.addUndoInfoStoredListener(() -> {
					stores.incrementAndGet();
					stored.countDown();
				});
				app.setMode(EuclidianConstants.MODE_ANGLE_FIXED);
				List<GeoElement> before = objects(app);
				if (segmentRoute) {
					click(app, new double[] {-6, -6});
				} else {
					click(app, new double[] {6, -4});
					click(app, new double[] {-6, -4});
				}
				assertEquals(1, requests.get(), "the tool asked for the size once");
				assertTrue(stored.await(5, TimeUnit.SECONDS));
				Thread.sleep(300);
				assertEquals(1, stores.get(), "one undo point");
				List<GeoElement> created = newObjects(app, before);
				assertEquals(2, created.size(), created.toString());
				GeoAngle angle = single(created, AlgoAnglePoints.class);
				GeoElement point = rotatedPoint(created);
				assertNotNull(point);
				assertFalse(angle.isEuclidianVisible(), "angle hidden");
				assertTrue(angle.isDefined());
				assertTrue(angle.isLabelSet());
				assertTrue(angle.isAlgebraVisible(), "the angle stays in the Algebra View");
				assertTrue(point.isEuclidianVisible(), "rotated point visible");
				assertTrue(point.isLabelSet());
				assertTrue(angle.getParentAlgorithm().getInput(0) == point
						|| angle.getParentAlgorithm().getInput(2) == point,
						"the angle depends on the rotated point");
				String angleLabel = angle.getLabelSimple();
				String pointLabel = point.getLabelSimple();
				app.getKernel().undo();
				assertNull(lookup(app, angleLabel));
				assertNull(lookup(app, pointLabel));
				app.getKernel().redo();
				assertFalse(lookup(app, angleLabel).isEuclidianVisible(),
						"redo restores the hidden angle");
				assertTrue(lookup(app, pointLabel).isEuclidianVisible());
			}
		}
	}

	@Test
	void hiddenAngleIsOrdinaryPersistedVisibilityAndRecoverable() throws Exception {
		AppGeoCeDG app = scene();
		DialogManagerD dialogs = installDialogSpy(app);
		confirmAngleDialogWith(app, dialogs, "40\u00b0", false, new AtomicInteger());
		app.setMode(EuclidianConstants.MODE_ANGLE_FIXED);
		List<GeoElement> before = objects(app);
		click(app, new double[] {6, -4});
		click(app, new double[] {-6, -4});
		List<GeoElement> created = newObjects(app, before);
		String label = single(created, AlgoAnglePoints.class).getLabelSimple();
		String point = rotatedPoint(created).getLabelSimple();
		String xml = app.getXML();
		assertTrue(showTag(xml, label).contains("object=\"false\""), showTag(xml, label));
		app.setXML(xml, true);
		assertFalse(lookup(app, label).isEuclidianVisible(), "reopened hidden");
		assertTrue(lookup(app, point).isEuclidianVisible());
		assertTrue(app.loadXML(xml));
		assertFalse(lookup(app, label).isEuclidianVisible());
		GeoElement angle = lookup(app, label);
		angle.setEuclidianVisible(true);
		angle.updateRepaint();
		assertTrue(showTag(app.getXML(), label).contains("object=\"true\""));
	}

	private static String showTag(String xml, String label) {
		int element = xml.indexOf("label=\"" + label + "\"");
		assertTrue(element >= 0, label);
		int show = xml.indexOf("<show", element);
		return xml.substring(show, xml.indexOf("/>", show) + 2);
	}

	@Test
	void angleCommandScriptingApiAndOtherModesAreUnchanged() throws Exception {
		AppGeoCeDG app = scene();
		app.setMode(EuclidianConstants.MODE_ANGLE_FIXED);
		List<GeoElement> before = objects(app);
		eval(app, "Angle(Q,P,30\u00b0)");
		assertTrue(single(newObjects(app, before), AlgoAnglePoints.class).isEuclidianVisible(),
				"Angle(...) command");
		before = objects(app);
		app.getGgbApi().evalCommand("Execute({\"Angle(R,P,20\u00b0)\"})");
		assertTrue(single(newObjects(app, before), AlgoAnglePoints.class).isEuclidianVisible(),
				"GGBScript");
		before = objects(app);
		app.getGgbApi().evalCommand("Angle(S,P,25\u00b0)");
		assertTrue(single(newObjects(app, before), AlgoAnglePoints.class).isEuclidianVisible(),
				"API");
		app.setMode(EuclidianConstants.MODE_MOVE);
		GeoAngle direct = controller(app).getCompanion().createAngle(
				(GeoPoint) lookup(app, "Q"), (GeoPoint) lookup(app, "R"),
				(GeoNumeric) eval(app, "num=15\u00b0")[0], false);
		assertTrue(direct.isEuclidianVisible(), "only the Angle with Given Size mode hides");
		assertTrue(controller(app).getCompanion() instanceof GeoCeDGControllerCompanion);
	}

	@Test
	void graphicsView2AndClassicKeepTheInheritedBehavior() throws Exception {
		AppGeoCeDG app = scene();
		EuclidianController gv2 = app.getEuclidianView2(1).getEuclidianController();
		assertSame(EuclidianControllerFor3DD.class, gv2.getClass());
		assertSame(EuclidianControllerFor3DCompanion.class, gv2.getCompanion().getClass());
		app.setMode(EuclidianConstants.MODE_ANGLE_FIXED);
		List<GeoElement> before = objects(app);
		DialogManager.createAngleFixed(app.getKernel(), "40\u00b0", false, app.getErrorHandler(),
				new GeoSegmentND[0], new GeoPointND[] {(GeoPoint) lookup(app, "Q"),
						(GeoPoint) lookup(app, "P")}, null, gv2);
		assertTrue(single(newObjects(app, before), AlgoAnglePoints.class).isEuclidianVisible(),
				"the upstream Graphics View 2 tool keeps a visible angle");

		before = objects(app);
		DialogManager.createAngleFixed(app.getKernel(), "40\u00b0", false, app.getErrorHandler(),
				new GeoSegmentND[0], new GeoPointND[] {(GeoPoint) lookup(app, "R"),
						(GeoPoint) lookup(app, "P")}, null, controller(app));
		GeoAngle fromGv1 = single(newObjects(app, before), AlgoAnglePoints.class);
		assertFalse(fromGv1.isEuclidianVisible(), "one object property for every view");

		AppD classic = new AppD(new CommandLineArguments(new String[] {"--silent"}),
				new JPanel(), true);
		G9U1TestApp.withoutWindowDispatcher(classic);
		EuclidianController classicController = classic.getEuclidianView1()
				.getEuclidianController();
		assertFalse(classicController instanceof GeoCeDGEuclidianController);
		assertFalse(classicController.getCompanion() instanceof GeoCeDGControllerCompanion);
		classic.setMode(EuclidianConstants.MODE_ANGLE_FIXED);
		classic.getKernel().getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(
				"A=(0,0)", false, ErrorHelper.silent(), false, null);
		classic.getKernel().getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(
				"B=(3,1)", false, ErrorHelper.silent(), false, null);
		List<GeoElement> classicBefore = new ArrayList<>(
				classic.getKernel().getConstruction().getGeoSetConstructionOrder());
		DialogManager.createAngleFixed(classic.getKernel(), "40\u00b0", false,
				classic.getErrorHandler(), new GeoSegmentND[0],
				new GeoPointND[] {(GeoPoint) classic.getKernel().lookupLabel("B"),
						(GeoPoint) classic.getKernel().lookupLabel("A")}, null,
				classicController);
		for (GeoElement geo : classic.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (!classicBefore.contains(geo)) {
				assertTrue(geo.isEuclidianVisible(), "Classic unchanged: " + geo);
			}
		}
	}
}
