/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.store;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.locus.NativeTransactionalState2D;
import org.geogebra.common.jre.io.MyXMLioJre;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoPoint.NativeRollbackPointState;
import org.geogebra.common.main.undo.AppState;
import org.geogebra.common.main.undo.UndoCommand;
import org.geogebra.common.plugin.EventType;
import org.geogebra.desktop.main.undo.FileAppState;
import org.geogebra.desktop.main.undo.UndoManagerD.UndoStateLoadFailure;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Owned native history lifecycle; no public file or private key becomes an identity. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class R3NativeUndoLifecycleDesktopTest {

	@BeforeAll static void initialize() { G9U1SemanticPointInteractionTest.initializeDesktop(); }

	private static GeoElement eval(AppGeoCeDG app, String command) {
		return G9U1SemanticPointInteractionTest.eval(app, command);
	}
	private static GeoPoint point(AppGeoCeDG app, String label) {
		var point = app.getKernel().lookupLabel(label); assertInstanceOf(GeoPoint.class, point);
		return (GeoPoint) point;
	}
	private static AppGeoCeDG nativeApp() {
		var app = G9U1SemanticPointInteractionTest.app();
		eval(app, "nativeHyperbola=Hyperbola((-.7,.3),(1.2,1.1),.6)");
		var ancestor = (GeoPoint) eval(app, "NativeH=Point(nativeHyperbola)");
		ancestor.setCoords(2.2, 2.4, 1); ancestor.updateCascade();
		eval(app, "nativeCircle=Circle(NativeH,1)");
		var current = (GeoPoint) eval(app, "NativeP=Point(nativeCircle)");
		current.updatePathParameter(.73123); current.updateCascade();
		eval(app, "AxisP=Point(xAxis)");
		assertFalse(ancestor.getXML().contains("<conicPointState"));
		assertFalse(current.getXML().contains("<conicPointState"));
		assertNull(NativeTransactionalState2D.unsupportedReason(app.getKernel().getConstruction()));
		return app;
	}
	private static NativeInputs changePoint(AppGeoCeDG app, double parameter) {
		var point = point(app, "NativeP");
		point.updatePathParameter(parameter); point.updateCascade();
		return NativeInputs.capture(app);
	}
	private record NativeInputs(Map<String, NativeRollbackPointState> states) {
		static NativeInputs capture(AppGeoCeDG app) {
			Map<String, NativeRollbackPointState> states = new LinkedHashMap<>();
			for (String label : List.of("NativeH", "NativeP", "AxisP")) {
				var state = point(app, label).captureNativeRollbackState(); assertNotNull(state, label);
				states.put(label, state);
			}
			return new NativeInputs(Map.copyOf(states));
		}
		void assertExact(AppGeoCeDG app) {
			states.forEach((label, state) -> assertTrue(state.matches(point(app, label)),
					label + " raw input, argument, fallback, animation and native source/domain"));
			assertFalse(app.getXML().contains("<" + NativeTransactionalState2D.BINDING_TAG));
		}
	}
	private static Set<?> ids(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getSpatialIdentityRegistry().getRecords().stream()
				.map(record -> record.getId()).collect(Collectors.toSet());
	}
	private record LiveState(String xml, NativeInputs inputs, Set<?> ids, int reservations,
			List<UndoCommand> commands, int cursor) {
		static LiveState capture(AppGeoCeDG app, GeoCeDGUndoManager manager) {
			return new LiveState(app.getXML(), NativeInputs.capture(app), R3NativeUndoLifecycleDesktopTest.ids(app),
					app.getKernel().getConstruction().getSpatialIdentityRegistry().getReservedIdentityCount(),
					manager.getCommands(), manager.getCursor());
		}
		void assertUnchanged(AppGeoCeDG app, GeoCeDGUndoManager manager) {
			assertEquals(xml, app.getXML()); inputs.assertExact(app); assertEquals(ids, R3NativeUndoLifecycleDesktopTest.ids(app));
			assertEquals(reservations, app.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getReservedIdentityCount());
			assertEquals(cursor, manager.getCursor()); assertEquals(commands.size(), manager.getCommands().size());
			for (int i = 0; i < commands.size(); i++) {
				assertSame(commands.get(i), manager.getCommands().get(i), "Original history command " + i);
			}
		}
	}
	private static final class RecordingManager extends GeoCeDGUndoManager {
		final List<FileAppState> created = new CopyOnWriteArrayList<>();
		RecordingManager(Construction construction) { super(construction); }
		@Override protected FileAppState createUndoState(StringBuilder xml) throws IOException {
			var state = super.createUndoState(xml); created.add(state); return state;
		}
		void load(AppState state) { loadUndoInfo(state, null); }
		StringBuilder capture() { return captureUndoXML(); }
		FileAppState build(StringBuilder xml) throws IOException { return createUndoState(xml); }
	}
	private static boolean hasTemplate(FileAppState state) throws Exception {
		assertInstanceOf(GeoCeDGUndoManager.NativeFileAppState.class, state);
		Field field = GeoCeDGUndoManager.NativeFileAppState.class.getDeclaredField("template");
		field.setAccessible(true);
		return field.get(state) != null;
	}
	private static int pendingHandoffs(GeoCeDGUndoManager manager) throws Exception {
		Field field = GeoCeDGUndoManager.class.getDeclaredField("pendingNativeStates");
		field.setAccessible(true);
		var pending = (Map<?, ?>) field.get(manager); return pending == null ? 0 : pending.size();
	}
	private static String privateXml(FileAppState state) throws Exception {
		try (var zip = new ZipFile(state.getFile())) {
			var entry = zip.getEntry("geogebra.xml"); assertNotNull(entry);
			try (var input = zip.getInputStream(entry)) {
				return new String(input.readAllBytes(), StandardCharsets.UTF_8);
			}
		}
	}
	private static void assertEventsEnabled(AppGeoCeDG app) {
		AtomicInteger received = new AtomicInteger();
		org.geogebra.common.plugin.EventListener listener = event -> {
			if (event.type == EventType.SELECT) { received.incrementAndGet(); }
		};
		app.getEventDispatcher().addEventListener(listener);
		try {
			app.getEventDispatcher().dispatchEvent(EventType.SELECT, point(app, "NativeP"));
			assertEquals(1, received.get(), "Undo must re-enable the ordinary event dispatcher");
		} finally { app.getEventDispatcher().removeEventListener(listener); }
	}
	private static void assertObjectUpdateListener(AppGeoCeDG app) {
		var listeners = app.getScriptManager().getUpdateListenerMap(); assertNotNull(listeners);
		assertNotNull(listeners.get(point(app, "NativeP")));
		assertEquals("r3NativeUpdate", listeners.get(point(app, "NativeP")).getText());
	}

	@Test void preparedBaselineFreezesNativeInputsAndObjectUpdateListeners() throws Exception {
		var app = onEdt(R3NativeUndoLifecycleDesktopTest::nativeApp);
		var manager = undo(app);
		var initial = onEdt(() -> NativeInputs.capture(app));
		var changed = onEdt(() -> {
			app.getScriptManager().registerObjectUpdateListener("NativeP", "r3NativeUpdate");
			try (var prepared = manager.prepareUndoBaseline()) {
				var next = changePoint(app, .81234);
				manager.commitUndoBaseline(prepared); next.assertExact(app);
				return next;
			}
		});
		store(app);
		onEdt(() -> {
			manager.undo(); initial.assertExact(app); assertObjectUpdateListener(app); assertEventsEnabled(app);
			manager.redo(); changed.assertExact(app); assertObjectUpdateListener(app); assertEventsEnabled(app);
		});
		assertEquals(0, pendingHandoffs(manager));
	}

	@Test void workerStoresCallerCaptureRatherThanTheMovingConstruction() throws Exception {
		var app = onEdt(R3NativeUndoLifecycleDesktopTest::nativeApp); var manager = undo(app);
		onEdt(() -> { baseline(app); return null; });
		var frozen = onEdt(() -> {
			synchronized (manager) {
				var result = changePoint(app, .81234); manager.storeUndoInfo();
				changePoint(app, .92345); return result;
			}
		});
		awaitQuiescent(app);
		onEdt(() -> { manager.restoreCurrentUndoInfo(); frozen.assertExact(app); assertEventsEnabled(app); });
		assertEquals(2, manager.getCommands().size()); assertEquals(0, pendingHandoffs(manager));
	}

	@Test void heldHistoryKeepsOriginalCompanionsAndRedo() throws Exception {
		var app = onEdt(R3NativeUndoLifecycleDesktopTest::nativeApp); var manager = undo(app);
		var initial = onEdt(() -> NativeInputs.capture(app));
		onEdt(() -> { baseline(app); return null; });
		var changed = onEdt(() -> { return changePoint(app, .81234); }); store(app);
		onEdt(manager::undo);
		var original = manager.getCommands(); int cursor = manager.getCursor();
		original.get(1).labels = List.of("NativeP");
		var held = onEdt(manager::detach);
		assertTrue(manager.getCommands().isEmpty());
		for (int i = 0; i < original.size(); i++) {
			assertSame(original.get(i), held.getCommands().get(i));
			var state = (FileAppState) original.get(i).getAppState();
			assertTrue(state.getFile().exists()); assertTrue(hasTemplate(state));
		}
		assertEquals(cursor, held.getCursor()); assertNull(onEdt(() -> manager.attach(held.getCommands(), cursor)));
		assertEquals(List.of("NativeP"), manager.getCommands().get(1).labels);
		onEdt(() -> {
			for (int cycle = 0; cycle < 3; cycle++) {
				assertTrue(manager.redoPossible()); manager.redo(); changed.assertExact(app);
				manager.undo(); initial.assertExact(app);
			}
		});
		for (int i = 0; i < original.size(); i++) { assertSame(original.get(i), manager.getCommands().get(i)); }
	}

	@Test void abortedPreparedBaselineDeletesTheCompanionWithItsFile() throws Exception {
		var app = onEdt(R3NativeUndoLifecycleDesktopTest::nativeApp);
		var manager = new RecordingManager(app.getKernel().getConstruction());
		var before = onEdt(() -> LiveState.capture(app, manager));
		onEdt(() -> {
			try (var prepared = manager.prepareUndoBaseline()) {
				assertEquals(1, manager.created.size());
				assertTrue(manager.created.get(0).getFile().exists()); assertTrue(hasTemplate(manager.created.get(0)));
			}
			return null;
		});
		var state = manager.created.get(0); assertFalse(state.getFile().exists()); assertFalse(hasTemplate(state));
		onEdt(() -> before.assertUnchanged(app, manager)); assertEquals(0, pendingHandoffs(manager));
	}

	@Test void prunedRedoDeletesTheCompanionWithItsFile() throws Exception {
		var app = onEdt(R3NativeUndoLifecycleDesktopTest::nativeApp); var manager = undo(app);
		onEdt(() -> { baseline(app); return null; });
		onEdt(() -> { changePoint(app, .81234); }); store(app);
		onEdt(() -> { changePoint(app, .92345); }); store(app);
		var discarded = (FileAppState) manager.getCommands().get(2).getAppState();
		assertTrue(hasTemplate(discarded)); onEdt(manager::undo);
		var retained = onEdt(() -> changePoint(app, 1.03456)); store(app);
		assertFalse(discarded.getFile().exists()); assertFalse(hasTemplate(discarded));
		assertFalse(manager.redoPossible()); assertEquals(3, manager.getCommands().size());
		onEdt(() -> { manager.undo(); manager.redo(); retained.assertExact(app); });
	}

	@Test void staleGenerationDiscardsItsCompanionAndPendingHandoff() throws Exception {
		var app = onEdt(R3NativeUndoLifecycleDesktopTest::nativeApp);
		var manager = new RecordingManager(app.getKernel().getConstruction());
		onEdt(() -> {
			try (var prepared = manager.prepareUndoBaseline()) { manager.commitUndoBaseline(prepared); }
			synchronized (manager) {
				manager.storeUndoInfo();
				try (var prepared = manager.prepareUndoBaseline()) { manager.commitUndoBaseline(prepared); }
				assertEquals(0, manager.getPendingStores());
			}
			return null;
		});
		Cu7M08SDesktopEventsTest.await(() -> manager.created.size() == 3
				&& !manager.created.get(2).getFile().exists());
		assertFalse(hasTemplate(manager.created.get(2))); assertTrue(manager.isQuiescent());
		assertEquals(1, manager.getCommands().size());
		assertSame(manager.created.get(1), manager.getCommands().get(0).getAppState());
		assertEquals(0, pendingHandoffs(manager));
	}

	@Test void foreignManagerCannotLoadAnOwnedPrivateState() throws Exception {
		var source = onEdt(R3NativeUndoLifecycleDesktopTest::nativeApp);
		onEdt(() -> { baseline(source); return null; });
		var state = undo(source).getCommands().get(0).getAppState();
		var target = onEdt(R3NativeUndoLifecycleDesktopTest::nativeApp);
		for (AppGeoCeDG host : List.of(source, target)) {
			var foreign = new RecordingManager(host.getKernel().getConstruction());
			var before = onEdt(() -> LiveState.capture(host, foreign));
			onEdt(() -> { assertThrows(UndoStateLoadFailure.class, () -> foreign.load(state));
				before.assertUnchanged(host, foreign); assertEventsEnabled(host); });
		}
		assertTrue(hasTemplate((FileAppState) state));
	}

	@Test void corruptOwnedSnapshotFailsAtomicallyAndThenRecovers() throws Exception {
		var app = onEdt(R3NativeUndoLifecycleDesktopTest::nativeApp); var manager = undo(app);
		var initial = onEdt(() -> NativeInputs.capture(app));
		onEdt(() -> { baseline(app); return null; });
		var changed = onEdt(() -> { return changePoint(app, .81234); }); store(app);
		var baseline = (FileAppState) manager.getCommands().get(0).getAppState();
		byte[] original = Files.readAllBytes(baseline.getFile().toPath());
		String tampered = privateXml(baseline).replace("NativeP", "TamperedP");
		try (var output = Files.newOutputStream(baseline.getFile().toPath())) {
			MyXMLioJre.writeZipped(output, new StringBuilder(tampered));
		}
		var before = onEdt(() -> LiveState.capture(app, manager));
		try {
			onEdt(() -> { assertThrows(UndoStateLoadFailure.class, manager::undo);
				before.assertUnchanged(app, manager); assertEventsEnabled(app); });
			assertTrue(hasTemplate(baseline));
		} finally { Files.write(baseline.getFile().toPath(), original); }
		onEdt(() -> { manager.undo(); initial.assertExact(app); manager.redo(); changed.assertExact(app); });
	}

	@Test void changedCallerBuilderIsRejectedAndThePendingHandoffReleased() throws Exception {
		var app = onEdt(R3NativeUndoLifecycleDesktopTest::nativeApp);
		var manager = new RecordingManager(app.getKernel().getConstruction());
		var before = onEdt(() -> LiveState.capture(app, manager));
		onEdt(() -> {
			var captured = manager.capture(); assertEquals(1, pendingHandoffs(manager));
			captured.append("\n");
			assertThrows(IOException.class, () -> manager.build(captured));
			assertEquals(0, pendingHandoffs(manager)); before.assertUnchanged(app, manager);
			return null;
		});
	}

	@Test void publicDocumentsNeverPublishPrivateNativeBindings(@TempDir Path directory) throws Exception {
		var app = onEdt(R3NativeUndoLifecycleDesktopTest::nativeApp);
		var manager = new RecordingManager(app.getKernel().getConstruction());
		onEdt(() -> {
			try (var prepared = manager.prepareUndoBaseline()) {
				String privateState = privateXml(manager.created.get(0));
				assertTrue(privateState.contains("<" + NativeTransactionalState2D.BINDING_TAG));
				assertFalse(app.getXML().contains("<" + NativeTransactionalState2D.BINDING_TAG));
				Path file = directory.resolve("native-public.cedg");
				assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
				try (var zip = new ZipFile(file.toFile()); var input = zip.getInputStream(zip.getEntry("geogebra.xml"))) {
					assertFalse(new String(input.readAllBytes(), StandardCharsets.UTF_8)
							.contains("<" + NativeTransactionalState2D.BINDING_TAG));
				}
			}
			return null;
		});
		assertFalse(hasTemplate(manager.created.get(0)));
	}

	@Test void ordinaryUnsupportedNativeHistoryStillUndoesAndRedoesTheCompatibleSemanticGraph() throws Exception {
		var app = onEdt(() -> {
			var host = G9U1SemanticPointInteractionTest.app();
			eval(host, "nativeCircle=Circle((0,0),1)");
			var nativePoint = (GeoPoint) eval(host, "NativeP=Point(nativeCircle)");
			nativePoint.updatePathParameter(.73123); nativePoint.updateCascade();
			var source = G9U1SemanticPointInteractionTest.spline(host);
			var semantic = new GeoCeDGPointInteraction(host).create(source, -1, .01, .1, result -> null);
			assertNotNull(semantic); semantic.setLabel("SemanticP");
			eval(host, "SemanticDependent=SemanticP+(1,2)");
			assertNull(NativeTransactionalState2D.unsupportedReason(host.getKernel().getConstruction()));
			baseline(host); return host;
		});
		var manager = undo(app);
		assertInstanceOf(GeoCeDGUndoManager.NativeFileAppState.class,
				manager.getCommands().get(0).getAppState());
		var semantic = point(app, "SemanticP");
		var identity = app.getKernel().getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(semantic);
		assertNotNull(identity);
		var initialAddress = AlgoSemanticLocusPoint2D.requireSemanticParent(semantic).getCurrentSemanticAddress();
		onEdt(() -> {
			eval(app, "ordinaryCurve=Curve(t,t^2,t,-2,2)");
			var curvePoint = (GeoPoint) eval(app, "CurveP=Point(ordinaryCurve)");
			curvePoint.setCoords(.8, .64, 1); curvePoint.updateCascade();
			eval(app, "CurveDependent=CurveP+(1,2)");
			assertEquals(NativeTransactionalState2D.UnsupportedReason.UNSUPPORTED_NATIVE_PLACE,
					NativeTransactionalState2D.unsupportedReason(app.getKernel().getConstruction()));
			String before = app.getXML();
			assertFalse(new GeoCeDGPointInteraction(app).move(semantic, 0, 0, .1),
					"The legacy Undo fallback cannot grant a new CU7 editing permission");
			assertEquals(before, app.getXML());
		});
		store(app);
		assertFalse(manager.getCommands().get(1).getAppState() instanceof GeoCeDGUndoManager.NativeFileAppState,
				"The unsupported B state must keep the inherited native route");
		var typedBaseline = (FileAppState) manager.getCommands().get(0).getAppState();
		byte[] originalArchive = Files.readAllBytes(typedBaseline.getFile().toPath());
		String tamperedBaseline = privateXml(typedBaseline).replace("NativeP", "TamperedP");
		try (var output = Files.newOutputStream(typedBaseline.getFile().toPath())) {
			MyXMLioJre.writeZipped(output, new StringBuilder(tamperedBaseline));
		}
		try {
			onEdt(() -> {
				var construction = app.getKernel().getConstruction();
				String beforeXml = app.getXML(); var beforeIds = ids(app);
				int beforeCount = construction.getGeoSetConstructionOrder().size();
				int beforeReservations = construction.getSpatialIdentityRegistry().getReservedIdentityCount();
				var beforeCommands = manager.getCommands(); int beforeCursor = manager.getCursor();
				var ordinaryState = point(app, "NativeP").captureNativeRollbackState(); assertNotNull(ordinaryState);
				var curve = point(app, "CurveP");
				long[] curveRaw = {Double.doubleToLongBits(curve.getPathParameter().t),
						curve.getPathParameter().getPathType(), Double.doubleToLongBits(curve.x),
						Double.doubleToLongBits(curve.y), Double.doubleToLongBits(curve.z),
						Double.doubleToLongBits(curve.inhomX), Double.doubleToLongBits(curve.inhomY),
						Double.doubleToLongBits(curve.getAnimationValue())};
				Field fallbackField = GeoPoint.class.getDeclaredField("tempPathParameter");
				fallbackField.setAccessible(true);
				var fallback = (org.geogebra.common.kernel.PathParameter) fallbackField.get(curve);
				long fallbackT = fallback == null ? 0 : Double.doubleToLongBits(fallback.t);
				int fallbackType = fallback == null ? 0 : fallback.getPathType();
				assertFalse(construction.isFileLoading());
				// B has no exact restorer. A's byte preflight must reject before touching B at all.
				assertThrows(UndoStateLoadFailure.class, manager::undo);
				assertEquals(beforeXml, app.getXML()); assertEquals(beforeIds, ids(app));
				assertEquals(beforeCount, construction.getGeoSetConstructionOrder().size());
				assertEquals(beforeReservations, construction.getSpatialIdentityRegistry().getReservedIdentityCount());
				assertEquals(beforeCursor, manager.getCursor());
				assertEquals(beforeCommands.size(), manager.getCommands().size());
				for (int i = 0; i < beforeCommands.size(); i++) {
					assertSame(beforeCommands.get(i), manager.getCommands().get(i));
				}
				assertTrue(ordinaryState.matches(point(app, "NativeP")));
				assertSame(curve, point(app, "CurveP"));
				assertArrayEquals(curveRaw, new long[] {Double.doubleToLongBits(curve.getPathParameter().t),
						curve.getPathParameter().getPathType(), Double.doubleToLongBits(curve.x),
						Double.doubleToLongBits(curve.y), Double.doubleToLongBits(curve.z),
						Double.doubleToLongBits(curve.inhomX), Double.doubleToLongBits(curve.inhomY),
						Double.doubleToLongBits(curve.getAnimationValue())});
				assertSame(fallback, fallbackField.get(curve));
				if (fallback != null) {
					assertEquals(fallbackT, Double.doubleToLongBits(fallback.t));
					assertEquals(fallbackType, fallback.getPathType());
				}
				assertFalse(construction.isFileLoading()); assertEventsEnabled(app);
				return null;
			});
		} finally { Files.write(typedBaseline.getFile().toPath(), originalArchive); }
		onEdt(() -> {
			for (int cycle = 0; cycle < 2; cycle++) {
				manager.undo();
				assertNull(app.getKernel().lookupLabel("ordinaryCurve"));
				assertNull(app.getKernel().lookupLabel("CurveP"));
				assertNull(app.getKernel().lookupLabel("CurveDependent"));
				assertNull(NativeTransactionalState2D.unsupportedReason(app.getKernel().getConstruction()));
				var restored = point(app, "SemanticP");
				assertEquals(identity, app.getKernel().getConstruction().getSpatialIdentityRegistry()
						.getPersistentGeoId(restored));
				assertEquals(initialAddress, AlgoSemanticLocusPoint2D.requireSemanticParent(restored)
						.getCurrentSemanticAddress());
				var ordinary = point(app, "NativeP");
				assertEquals(1, ordinary.inhomX * ordinary.inhomX + ordinary.inhomY * ordinary.inhomY, 1e-10);
				var dependent = point(app, "SemanticDependent");
				assertEquals(restored.inhomX + 1, dependent.inhomX, 1e-10);
				assertEquals(restored.inhomY + 2, dependent.inhomY, 1e-10);
				manager.redo();
				assertNotNull(app.getKernel().lookupLabel("ordinaryCurve"));
				var curvePoint = point(app, "CurveP"); var curveDependent = point(app, "CurveDependent");
				assertSame(app.getKernel().lookupLabel("ordinaryCurve"), curvePoint.getPath());
				assertEquals(curvePoint.inhomX * curvePoint.inhomX, curvePoint.inhomY, 1e-10);
				assertEquals(curvePoint.inhomX + 1, curveDependent.inhomX, 1e-10);
				assertEquals(curvePoint.inhomY + 2, curveDependent.inhomY, 1e-10);
				restored = point(app, "SemanticP");
				assertEquals(identity, app.getKernel().getConstruction().getSpatialIdentityRegistry()
						.getPersistentGeoId(restored));
				assertEquals(initialAddress, AlgoSemanticLocusPoint2D.requireSemanticParent(restored)
						.getCurrentSemanticAddress());
				assertEventsEnabled(app);
				assertFalse(app.getXML().contains("<" + NativeTransactionalState2D.BINDING_TAG));
			}
		});
	}
}
