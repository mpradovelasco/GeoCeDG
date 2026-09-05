/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.algos.AlgoLocusSimilarityTransform2D;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.view.algebra.AlgebraViewD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Ordinary numeric editing continues to use the real inherited Algebra gestures. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1AlgebraGestureEditingTest {

	@Test
	void doubleClickEditsDilateFactorWithIdentityUndoRedoAndReopen(
			@TempDir Path directory) throws Exception {
		assertGestureLifecycle(EditGesture.DOUBLE_CLICK, directory);
	}

	@Test
	void f2EditsDilateFactorWithIdentityUndoRedoAndReopen(
			@TempDir Path directory) throws Exception {
		assertGestureLifecycle(EditGesture.F2, directory);
	}

	private static void assertGestureLifecycle(EditGesture gesture, Path directory)
			throws Exception {
		AtomicReference<Scenario> reference = new AtomicReference<>();
		onEventThread(() -> reference.set(createScenario()));
		Scenario scenario = reference.get();
		flushEventQueue();
		await(() -> scenario.undo.getHistorySize() == 0);
		onEventThread(() -> assertEquals(0, scenario.undo.getHistorySize()));

		onEventThread(() -> editThroughGesture(scenario.app, scenario.algebra,
				scenario.factor, gesture, "2"));
		flushEventQueue();
		await(() -> scenario.undo.getHistorySize() == 1);
		onEventThread(() -> assertEditedState(scenario));

		onEventThread(() -> scenario.app.getKernel().undo());
		flushEventQueue();
		onEventThread(() -> {
			assertTrue(scenario.undo.redoPossible());
			assertState(scenario.app, scenario.factorId, 1, 4);
		});

		onEventThread(() -> scenario.app.getKernel().redo());
		flushEventQueue();
		onEventThread(() -> assertState(scenario.app, scenario.factorId, 2, 8));

		Path file = directory.resolve("ordinary-numeric-" + gesture + ".cedg");
		onEventThread(() -> {
			assertTrue(((GuiManagerGeoCeDG) scenario.app.getGuiManager())
					.saveAsTo(file.toFile()));
			AppGeoCeDG reopened = G9U1TestApp.create();
			assertTrue(reopened.loadFile(file.toFile(), false));
			assertState(reopened, scenario.factorId, 2, 8);
		});
	}

	private static Scenario createScenario() {
		AppGeoCeDG app = G9U1TestApp.create();
		for (String command : new String[] {"kesc=1",
				"b=SplineV2({(-2,0),(-2/3,0),(2/3,0),(2,0)},3)",
				"d=Dilate(b,kesc)", "m=Length(d)"}) {
			eval(app, command);
		}
		assertEquals(4, value(app, "m"), 1E-8);
		GeoNumeric factor = (GeoNumeric) lookup(app, "kesc");
		PersistentGeoId factorId = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(factor);
		assertNotNull(factorId);
		factor.setValue(2);
		factor.updateRepaint();
		assertEquals(8, value(app, "m"), 1E-8);
		factor.setValue(1);
		factor.updateRepaint();
		assertEquals(4, value(app, "m"), 1E-8);
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction()
				.getUndoManager();
		app.getKernel().initUndoInfo();
		return new Scenario(app, factor, factorId, attachedAlgebra(app), undo);
	}

	private static void assertEditedState(Scenario scenario) {
		assertSame(scenario.factor, lookup(scenario.app, "kesc"));
		GeoLocusV2 image = (GeoLocusV2) lookup(scenario.app, "d");
		AlgoLocusSimilarityTransform2D transform =
				(AlgoLocusSimilarityTransform2D) image.getParentAlgorithm();
		assertEquals(2, transform.getTransformSnapshot().getLengthScale(), 0);
		GeoNumeric metric = (GeoNumeric) lookup(scenario.app, "m");
		AlgoLocusMetricScalarAdapter scalarAdapter =
				(AlgoLocusMetricScalarAdapter) metric.getParentAlgorithm();
		GeoLocusMetricResult richMetric = scalarAdapter.getRichInput();
		assertEquals(8, richMetric.getMetricResult().getMetricValue()
				.getFiniteValue().orElse(Double.NaN), 1E-8);
		assertTrue(scenario.factor.algoUpdateSetContains(scalarAdapter));
		assertEquals(1, scenario.undo.getHistorySize());
		assertState(scenario.app, scenario.factorId, 2, 8);
	}

	private static void editThroughGesture(AppGeoCeDG app, AlgebraViewD algebra,
			GeoNumeric factor, EditGesture gesture, String value) throws Exception {
		Runnable edit = () -> {
			TreePath path = pathFor(algebra, factor);
			algebra.expandPath(path.getParentPath());
			algebra.scrollPathToVisible(path);
			algebra.setSelectionPath(path);
			if (gesture == EditGesture.DOUBLE_CLICK) {
				Rectangle bounds = algebra.getPathBounds(path);
				assertNotNull(bounds);
				assertTrue(bounds.width > algebra.getIconShownHeight() + 2);
				int x = bounds.x + bounds.width - 2;
				int y = bounds.y + bounds.height / 2;
				long when = System.currentTimeMillis();
				algebra.dispatchEvent(new MouseEvent(algebra, MouseEvent.MOUSE_PRESSED,
						when, 0, x, y, 2, false, MouseEvent.BUTTON1));
				algebra.dispatchEvent(new MouseEvent(algebra, MouseEvent.MOUSE_RELEASED,
						when, 0, x, y, 2, false, MouseEvent.BUTTON1));
			} else {
				app.getSelectionManager().clearSelectedGeos();
				app.getSelectionManager().addSelectedGeo(factor);
				KeyEvent event = new KeyEvent(algebra, KeyEvent.KEY_PRESSED,
						System.currentTimeMillis(), 0, KeyEvent.VK_F2,
						KeyEvent.CHAR_UNDEFINED);
				assertTrue(app.getGlobalKeyDispatcher().dispatchKeyEvent(event));
			}
			assertTrue(algebra.isEditItem(), gesture.name());
			JTextField editor = findComponent(algebra, JTextField.class);
			assertNotNull(editor);
			assertTrue(editor.getText().contains(factor.getLabelSimple()));
			editor.setText(factor.getLabelSimple() + " = " + value);
			assertTrue(algebra.stopEditing());
		};
		edit.run();
	}

	private static AlgebraViewD attachedAlgebra(AppGeoCeDG app) {
		GuiManagerD gui = (GuiManagerD) app.getGuiManager();
		gui.attachAlgebraView();
		AlgebraViewD algebra = gui.getAlgebraView();
		JScrollPane host = new JScrollPane(algebra);
		host.setSize(new Dimension(700, 500));
		host.doLayout();
		algebra.setSize(new Dimension(680, 480));
		algebra.doLayout();
		return algebra;
	}

	private static TreePath pathFor(AlgebraViewD algebra, GeoElement target) {
		Object root = algebra.getModel().getRoot();
		assertTrue(root instanceof DefaultMutableTreeNode);
		Enumeration<?> nodes = ((DefaultMutableTreeNode) root).depthFirstEnumeration();
		while (nodes.hasMoreElements()) {
			Object node = nodes.nextElement();
			if (node instanceof DefaultMutableTreeNode
					&& ((DefaultMutableTreeNode) node).getUserObject() == target) {
				return new TreePath(((DefaultMutableTreeNode) node).getPath());
			}
		}
		throw new AssertionError("Numeric is missing from the Algebra tree");
	}

	private static <T extends Component> T findComponent(Container root, Class<T> type) {
		for (Component child : root.getComponents()) {
			if (type.isInstance(child)) {
				return type.cast(child);
			}
			if (child instanceof Container) {
				T nested = findComponent((Container) child, type);
				if (nested != null) {
					return nested;
				}
			}
		}
		return null;
	}

	private static void assertState(AppGeoCeDG app, PersistentGeoId expectedId,
			double factor, double length) {
		GeoElement currentFactor = lookup(app, "kesc");
		assertEquals(expectedId, app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(currentFactor));
		assertEquals(factor, ((GeoNumeric) currentFactor).getDouble(), 0);
		assertEquals(length, value(app, "m"), 1E-8);
		assertTrue(lookup(app, "d").isDefined());
	}

	private static double value(AppGeoCeDG app, String label) {
		GeoElement geo = app.getKernel().lookupLabel(label);
		return geo instanceof GeoNumeric ? ((GeoNumeric) geo).getDouble() : Double.NaN;
	}

	private static void flushEventQueue() throws Exception {
		onEventThread(() -> {
			// A no-op event runs after callbacks already queued by the prior gesture.
		});
	}

	private static void await(BooleanSupplier condition) throws InterruptedException {
		long deadline = System.nanoTime() + 5_000_000_000L;
		while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
			Thread.sleep(10);
		}
		assertTrue(condition.getAsBoolean(), "Desktop background work did not complete");
	}

	private static void onEventThread(ThrowingRunnable action) throws Exception {
		AtomicReference<Throwable> failure = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
			try {
				action.run();
			} catch (Throwable throwable) {
				failure.set(throwable);
			}
		});
		Throwable throwable = failure.get();
		if (throwable instanceof Exception) {
			throw (Exception) throwable;
		}
		if (throwable instanceof Error) {
			throw (Error) throwable;
		}
		if (throwable != null) {
			throw new IllegalStateException(throwable);
		}
	}

	@FunctionalInterface
	private interface ThrowingRunnable {
		void run() throws Exception;
	}

	private static final class Scenario {
		private final AppGeoCeDG app;
		private final GeoNumeric factor;
		private final PersistentGeoId factorId;
		private final AlgebraViewD algebra;
		private final UndoManagerD undo;

		private Scenario(AppGeoCeDG app, GeoNumeric factor,
				PersistentGeoId factorId, AlgebraViewD algebra, UndoManagerD undo) {
			this.app = app;
			this.factor = factor;
			this.factorId = factorId;
			this.algebra = algebra;
			this.undo = undo;
		}
	}

	private enum EditGesture {
		DOUBLE_CLICK,
		F2
	}
}
