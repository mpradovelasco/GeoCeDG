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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HexFormat;
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
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.view.algebra.AlgebraViewD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Ordinary numeric editing continues to use the real inherited Algebra gestures. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1AlgebraGestureEditingTest {
	static final String REVISION3_PATH =
			"artifacts/smoke-test-g9u1/Revision3.cedg";
	static final String REVISION3_SHA256 =
			"351955499d47d0407ab11c906da6e9b6d2ab636b0beef4e67c3edfddecccd939";

	@Test
	void doubleClickEditsDilateFactorWithIdentityUndoRedoAndReopen(
			@TempDir Path directory) throws Exception {
		assertGestureLifecycle(EditRoute.DOUBLE_CLICK, directory);
	}

	@Test
	void f2EditsDilateFactorWithIdentityUndoRedoAndReopen(
			@TempDir Path directory) throws Exception {
		assertGestureLifecycle(EditRoute.F2, directory);
	}

	@Test
	void rowEditorEditsDilateFactorWithProductSwingOwnership(
			@TempDir Path directory) throws Exception {
		assertGestureLifecycle(EditRoute.ROW, directory);
	}

	@Test
	void freeInputEditsExistingDilateFactorWithoutReplacingIt(
			@TempDir Path directory) throws Exception {
		assertGestureLifecycle(EditRoute.FREE_INPUT, directory);
	}

	@Test
	void deterministicFixtureCoversAllAlgebraEditRoutes(
			@TempDir Path directory) throws Exception {
		for (EditRoute route : EditRoute.values()) {
			assertGestureLifecycle(route, directory);
		}
	}

	static void assertRevision3Archive(Path archive, Path directory) throws Exception {
		assertTrue(Files.isRegularFile(archive), archive.toString());
		byte[] original = Files.readAllBytes(archive);
		assertEquals(14_110, original.length);
		assertEquals(REVISION3_SHA256, sha256(original));

		// GeoCeDG product startup owns the construction on the same Swing EDT
		// that later handles Algebra gestures.
		AtomicReference<Revision3Scenario> scenarioReference = new AtomicReference<>();
		onEventThread(() -> scenarioReference.set(createRevision3Scenario(archive)));
		Revision3Scenario scenario = scenarioReference.get();
		AppGeoCeDG app = scenario.app;
		GeoNumeric factor = scenario.factor;
		GeoLocusV2 image = scenario.image;
		AlgoLocusSimilarityTransform2D transform = scenario.transform;
		PersistentGeoId factorId = scenario.factorId;
		PersistentGeoId imageId = scenario.imageId;
		int constructionSize = scenario.constructionSize;
		AlgebraViewD algebra = scenario.algebra;
		UndoManagerD undo = scenario.undo;
		flushEventQueue();
		await(() -> undo.getHistorySize() == 0);

		EditRoute[] routes = {EditRoute.DOUBLE_CLICK, EditRoute.F2,
				EditRoute.ROW, EditRoute.FREE_INPUT};
		String[] inputs = {"0", ".25", "-1", "1"};
		double[] values = {0, 0.25, -1, 1};
		for (int index = 0; index < routes.length; index++) {
			int expectedHistorySize = index + 1;
			EditRoute route = routes[index];
			String input = inputs[index];
			double value = values[index];
			onEventThread(() -> editThroughRoute(app, algebra, factor, route, input));
			flushEventQueue();
			await(() -> undo.getHistorySize() == expectedHistorySize);
			onEventThread(() -> assertRevision3State(app, factor, factorId, image,
					imageId, transform, constructionSize, value));
		}

		onEventThread(() -> app.getKernel().undo());
		flushEventQueue();
		onEventThread(() -> assertRevision3Value(app, factorId, imageId,
				constructionSize, -1));
		onEventThread(() -> app.getKernel().redo());
		flushEventQueue();
		onEventThread(() -> assertRevision3Value(app, factorId, imageId,
				constructionSize, 1));

		Path saved = directory.resolve("Revision3-algebra-edited.cedg");
		onEventThread(() -> assertTrue(((GuiManagerGeoCeDG) app.getGuiManager())
				.saveAsTo(saved.toFile())));
		onEventThread(() -> {
			AppGeoCeDG reopened = G9U1TestApp.create();
			assertTrue(reopened.loadFile(saved.toFile(), false));
			assertRevision3Value(reopened, factorId, imageId, constructionSize, 1);
		});
		assertEquals(REVISION3_SHA256, sha256(Files.readAllBytes(archive)));
	}

	private static Revision3Scenario createRevision3Scenario(Path archive) {
		AppGeoCeDG app = G9U1TestApp.create();
		assertTrue(app.loadFile(archive.toFile(), false));
		GeoNumeric factor = (GeoNumeric) lookup(app, "kesc");
		GeoLocusV2 image = (GeoLocusV2) lookup(app, "T");
		AlgoLocusSimilarityTransform2D transform =
				(AlgoLocusSimilarityTransform2D) image.getParentAlgorithm();
		PersistentGeoId factorId = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(factor);
		PersistentGeoId imageId = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(image);
		assertNotNull(factorId);
		assertNotNull(imageId);
		int constructionSize = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size();
		assertEquals(26, constructionSize);
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction()
				.getUndoManager();
		app.getKernel().initUndoInfo();
		return new Revision3Scenario(app, factor, image, transform, factorId,
				imageId, constructionSize, attachedAlgebra(app), undo);
	}

	private static void assertGestureLifecycle(EditRoute gesture, Path directory)
			throws Exception {
		AtomicReference<Scenario> scenarioReference = new AtomicReference<>();
		onEventThread(() -> scenarioReference.set(createScenario()));
		Scenario scenario = scenarioReference.get();
		flushEventQueue();
		await(() -> scenario.undo.getHistorySize() == 0);
		onEventThread(() -> assertEquals(0, scenario.undo.getHistorySize()));

		String[] inputs = {"0", ".25", "-1", "1"};
		double[] values = {0, 0.25, -1, 1};
		double[] lengths = {0, 1, 4, 4};
		for (int index = 0; index < inputs.length; index++) {
			int expectedHistorySize = index + 1;
			double value = values[index];
			double length = lengths[index];
			onEventThread(() -> editThroughRoute(scenario.app, scenario.algebra,
					scenario.factor, gesture, inputs[expectedHistorySize - 1]));
			flushEventQueue();
			await(() -> scenario.undo.getHistorySize() == expectedHistorySize);
			onEventThread(() -> assertEditedState(scenario, value, length,
					expectedHistorySize));
		}

		onEventThread(() -> scenario.app.getKernel().undo());
		flushEventQueue();
		onEventThread(() -> {
			assertTrue(scenario.undo.redoPossible());
			assertState(scenario.app, scenario.factorId, -1, 4);
		});

		onEventThread(() -> scenario.app.getKernel().redo());
		flushEventQueue();
		onEventThread(() -> assertState(scenario.app, scenario.factorId, 1, 4));

		Path file = directory.resolve("ordinary-numeric-" + gesture + ".cedg");
		onEventThread(() -> {
			assertTrue(((GuiManagerGeoCeDG) scenario.app.getGuiManager())
					.saveAsTo(file.toFile()));
			AppGeoCeDG reopened = G9U1TestApp.create();
			assertTrue(reopened.loadFile(file.toFile(), false));
			assertState(reopened, scenario.factorId, 1, 4);
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

	private static void assertEditedState(Scenario scenario, double value,
			double expectedLength, int expectedHistorySize) {
		assertSame(scenario.factor, lookup(scenario.app, "kesc"));
		GeoLocusV2 image = (GeoLocusV2) lookup(scenario.app, "d");
		AlgoLocusSimilarityTransform2D transform =
				(AlgoLocusSimilarityTransform2D) image.getParentAlgorithm();
		assertEquals(Math.abs(value),
				transform.getTransformSnapshot().getLengthScale(), 0);
		GeoNumeric metric = (GeoNumeric) lookup(scenario.app, "m");
		AlgoLocusMetricScalarAdapter scalarAdapter =
				(AlgoLocusMetricScalarAdapter) metric.getParentAlgorithm();
		GeoLocusMetricResult richMetric = scalarAdapter.getRichInput();
		assertEquals(expectedLength, richMetric.getMetricResult().getMetricValue()
				.getFiniteValue().orElse(Double.NaN), 1E-8);
		assertTrue(scenario.factor.algoUpdateSetContains(scalarAdapter));
		assertEquals(expectedHistorySize, scenario.undo.getHistorySize());
		assertState(scenario.app, scenario.factorId, value, expectedLength);
	}

	private static void editThroughRoute(AppGeoCeDG app, AlgebraViewD algebra,
			GeoNumeric factor, EditRoute route, String value) throws Exception {
		Runnable edit = () -> {
			if (route == EditRoute.FREE_INPUT) {
				submitFreeInput(app, factor.getLabelSimple() + " = " + value);
				return;
			}
			TreePath path = pathFor(algebra, factor);
			algebra.expandPath(path.getParentPath());
			algebra.scrollPathToVisible(path);
			algebra.setSelectionPath(path);
			if (route == EditRoute.DOUBLE_CLICK) {
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
			} else if (route == EditRoute.F2) {
				app.getSelectionManager().clearSelectedGeos();
				app.getSelectionManager().addSelectedGeo(factor);
				KeyEvent event = new KeyEvent(algebra, KeyEvent.KEY_PRESSED,
						System.currentTimeMillis(), 0, KeyEvent.VK_F2,
						KeyEvent.CHAR_UNDEFINED);
				assertTrue(app.getGlobalKeyDispatcher().dispatchKeyEvent(event));
			} else {
				algebra.startEditItem(factor);
			}
			assertTrue(algebra.isEditItem(), route.name());
			JTextField editor = findComponent(algebra, JTextField.class);
			assertNotNull(editor);
			assertTrue(editor.getText().contains(factor.getLabelSimple()));
			editor.setText(factor.getLabelSimple() + " = " + value);
			assertTrue(algebra.stopEditing());
		};
		edit.run();
	}

	private static void submitFreeInput(AppGeoCeDG app, String command) {
		AtomicReference<GeoElementND[]> output = new AtomicReference<>();
		ErrorAccumulator errors = new ErrorAccumulator();
		try {
			GeoCeDGAlgebraInputSubmission.submit(app, command,
					new EvalInfo(true, true).withSliders(true).withSymbolic(true),
					errors, output::set);
		} catch (Exception exception) {
			throw new AssertionError(command, exception);
		}
		assertEquals("", errors.getErrors(), command);
		assertNotNull(output.get(), command);
		assertEquals(1, output.get().length, command);
	}

	private static void assertRevision3State(AppGeoCeDG app, GeoNumeric factor,
			PersistentGeoId factorId, GeoLocusV2 image, PersistentGeoId imageId,
			AlgoLocusSimilarityTransform2D transform, int constructionSize,
			double value) {
		assertSame(factor, lookup(app, "kesc"));
		assertSame(image, lookup(app, "T"));
		assertSame(transform, image.getParentAlgorithm());
		assertTrue(factor.algoUpdateSetContains(transform));
		assertRevision3Value(app, factorId, imageId, constructionSize, value);
	}

	private static void assertRevision3Value(AppGeoCeDG app,
			PersistentGeoId factorId, PersistentGeoId imageId, int constructionSize,
			double value) {
		GeoNumeric factor = (GeoNumeric) lookup(app, "kesc");
		GeoLocusV2 image = (GeoLocusV2) lookup(app, "T");
		assertEquals(factorId, app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(factor));
		assertEquals(imageId, app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(image));
		assertEquals(constructionSize, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
		assertEquals(value, factor.getDouble(), 0);
		assertTrue(image.isDefined());
		assertEquals(Math.abs(value), ((AlgoLocusSimilarityTransform2D)
				image.getParentAlgorithm()).getTransformSnapshot().getLengthScale(), 0);
	}

	private static String sha256(byte[] value) throws Exception {
		return HexFormat.of().formatHex(
				MessageDigest.getInstance("SHA-256").digest(value));
	}

	static Path findRepositoryRoot() {
		Path candidate = Path.of("").toAbsolutePath().normalize();
		while (candidate != null) {
			if (Files.isRegularFile(candidate.resolve("AGENTS.md"))
					&& Files.isDirectory(candidate.resolve("geocedg"))) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		throw new IllegalStateException("GeoCeDG repository root not found");
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

	private static final class Revision3Scenario {
		private final AppGeoCeDG app;
		private final GeoNumeric factor;
		private final GeoLocusV2 image;
		private final AlgoLocusSimilarityTransform2D transform;
		private final PersistentGeoId factorId;
		private final PersistentGeoId imageId;
		private final int constructionSize;
		private final AlgebraViewD algebra;
		private final UndoManagerD undo;

		private Revision3Scenario(AppGeoCeDG app, GeoNumeric factor,
				GeoLocusV2 image, AlgoLocusSimilarityTransform2D transform,
				PersistentGeoId factorId, PersistentGeoId imageId,
				int constructionSize, AlgebraViewD algebra, UndoManagerD undo) {
			this.app = app;
			this.factor = factor;
			this.image = image;
			this.transform = transform;
			this.factorId = factorId;
			this.imageId = imageId;
			this.constructionSize = constructionSize;
			this.algebra = algebra;
			this.undo = undo;
		}
	}

	private enum EditRoute {
		DOUBLE_CLICK,
		F2,
		ROW,
		FREE_INPUT
	}
}
