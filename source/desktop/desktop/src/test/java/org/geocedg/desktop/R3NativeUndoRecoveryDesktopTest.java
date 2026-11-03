/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.PathParameter;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoPoint.NativeRollbackPointState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Exact native host preservation across one real controller Undo/Redo gesture. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class R3NativeUndoRecoveryDesktopTest {

	@BeforeAll static void initialize() { G9U1SemanticPointInteractionTest.initializeDesktop(); }

	private static GeoElement eval(AppGeoCeDG app, String command) {
		return G9U1SemanticPointInteractionTest.eval(app, command);
	}
	private static GeoPoint point(AppGeoCeDG app, String label) {
		return (GeoPoint) app.getKernel().lookupLabel(label);
	}
	private static LocusSemanticAddress2D address(AppGeoCeDG app) {
		return AlgoSemanticLocusPoint2D.requireSemanticParent(point(app, "SemanticP"))
				.getCurrentSemanticAddress();
	}
	private static long bits(double value) { return Double.doubleToLongBits(value); }
	private record NativeInput(long t, int type, long x, long y, long z, long inhomX,
			long inhomY, long animation, boolean fallbackPresent, long fallbackT, int fallbackType) {
		static NativeInput capture(GeoPoint point) throws Exception {
			Field field = GeoPoint.class.getDeclaredField("tempPathParameter");
			field.setAccessible(true);
			PathParameter fallback = (PathParameter) field.get(point);
			return new NativeInput(bits(point.getPathParameter().t),
					point.getPathParameter().getPathType(), bits(point.x), bits(point.y), bits(point.z),
					bits(point.inhomX), bits(point.inhomY), bits(point.getAnimationValue()),
					fallback != null, fallback == null ? 0 : bits(fallback.t),
					fallback == null ? 0 : fallback.getPathType());
		}
	}
	private record Restored(Map<String, NativeInput> inputs, Map<String, Boolean> typedMatches,
			LocusSemanticAddress2D address, PersistentGeoId pointId, boolean dependentFollows) { }
	private static Restored restored(AppGeoCeDG app, Map<String, NativeRollbackPointState> expected)
			throws Exception {
		Map<String, NativeInput> inputs = new LinkedHashMap<>();
		Map<String, Boolean> typed = new LinkedHashMap<>();
		for (var entry : expected.entrySet()) {
			GeoPoint point = point(app, entry.getKey());
			inputs.put(entry.getKey(), NativeInput.capture(point));
			typed.put(entry.getKey(), entry.getValue().matches(point));
		}
		GeoPoint semantic = point(app, "SemanticP"), dependent = point(app, "SemanticDependent");
		return new Restored(inputs, typed, address(app), app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(semantic),
				bits(dependent.inhomX) == bits(semantic.inhomX + 1)
						&& bits(dependent.inhomY) == bits(semantic.inhomY + 2));
	}
	private static void assertNativeUnchanged(String stage, AppGeoCeDG app,
			Map<String, NativeInput> expectedInputs, Map<String, NativeRollbackPointState> expectedTyped)
			throws Exception {
		Restored current = restored(app, expectedTyped);
		System.out.println("R3_NATIVE_UNDO_STAGE " + stage + " nativeInputs=" + current.inputs());
		assertEquals(expectedInputs, current.inputs(), stage + " native input bits");
		assertTrue(current.typedMatches().values().stream().allMatch(Boolean::booleanValue),
				stage + " native source/domain and typed input");
		assertFalse(point(app, "NativeH").getXML().contains("<conicPointState"), stage);
		assertFalse(point(app, "NativeP").getXML().contains("<conicPointState"), stage);
	}

	@Test void semanticDragUndoRedoPreserveUnrelatedProjectedNativeAncestorInputsExactly()
			throws Exception {
		var host = new AtomicReference<AppGeoCeDG>();
		var expectedInputs = new AtomicReference<Map<String, NativeInput>>();
		var expectedTyped = new AtomicReference<Map<String, NativeRollbackPointState>>();
		var initialAddress = new AtomicReference<LocusSemanticAddress2D>();
		var movedAddress = new AtomicReference<LocusSemanticAddress2D>();
		var identity = new AtomicReference<PersistentGeoId>();
		var undoState = new AtomicReference<Restored>();
		var redoState = new AtomicReference<Restored>();
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); host.set(app);
			eval(app, "nativeHyperbola=Hyperbola((-.7,.3),(1.2,1.1),.6)");
			GeoPoint ancestor = (GeoPoint) eval(app, "NativeH=Point(nativeHyperbola)");
			ancestor.setCoords(2.2, 2.4, 1); ancestor.updateCascade();
			eval(app, "nativeCircle=Circle(NativeH,1)");
			GeoPoint nativePoint = (GeoPoint) eval(app, "NativeP=Point(nativeCircle)");
			nativePoint.updatePathParameter(.73123); nativePoint.updateCascade();
			assertFalse(ancestor.getXML().contains("<conicPointState"));
			assertFalse(nativePoint.getXML().contains("<conicPointState"));
			for (String command : List.of("c=Circle((0,0),1)", "N=Point(c)",
					"v=Vector((0,0),N)", "Q=N+(2*v)", "L=LocusV2(Q,N)")) { eval(app, command); }
			GeoLocusV2 locus = (GeoLocusV2) app.getKernel().lookupLabel("L");
			assertFalse(LocusV2PublicOperations.hasUnsupportedInteractiveHostState(
					app.getKernel().getConstruction()));
			try (var offer = new GeoCeDGReferencePlacement(locus, 3, .02,
					LocusPointInteractionPolicy2D.initial(.1))) {
				assertEquals(GeoCeDGReferencePlacement.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION,
						offer.getRoute());
				GeoPoint semantic = offer.commit(offer.getChoices().get(0));
				semantic.setLabel("SemanticP");
				identity.set(app.getKernel().getConstruction().getSpatialIdentityRegistry()
						.getPersistentGeoId(semantic)); assertNotNull(identity.get());
			}
			eval(app, "SemanticDependent=SemanticP+(1,2)");
			Map<String, NativeInput> inputs = new LinkedHashMap<>();
			Map<String, NativeRollbackPointState> typed = new LinkedHashMap<>();
			for (String label : List.of("NativeH", "NativeP")) {
				inputs.put(label, NativeInput.capture(point(app, label)));
				var state = point(app, label).captureNativeRollbackState(); assertNotNull(state);
				typed.put(label, state);
			}
			expectedInputs.set(inputs); expectedTyped.set(typed); initialAddress.set(address(app));
			for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
				geo.setEuclidianVisible(geo == locus || geo == point(app, "SemanticP"));
			}
			app.getEuclidianView1().setCoordSystem(220, 420, 100, 100);
			app.getKernel().notifyRepaint();
			assertNativeUnchanged("after visibility/view setup before baseline", app,
					expectedInputs.get(), expectedTyped.get());
			app.getKernel().initUndoInfo();
		});
		var app = host.get();
		var undo = (GeoCeDGUndoManager) app.getKernel().getConstruction().getUndoManager();
		Cu7M08SDesktopEventsTest.await(() -> undo.isQuiescent() && undo.getHistorySize() == 0);
		Cu7M08SDesktopEventsTest.edt(() -> {
			GeoPoint semantic = point(app, "SemanticP");
			assertNativeUnchanged("immediately before gesture", app, expectedInputs.get(), expectedTyped.get());
			app.setMode(EuclidianConstants.MODE_MOVE);
			assertNativeUnchanged("after setMode", app, expectedInputs.get(), expectedTyped.get());
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_PRESSED, semantic.inhomX, semantic.inhomY);
			assertNativeUnchanged("after press", app, expectedInputs.get(), expectedTyped.get());
			double x = 3 * Math.cos(.04), y = 3 * Math.sin(.04);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, x, y);
			assertNativeUnchanged("after drag before release", app, expectedInputs.get(), expectedTyped.get());
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, x, y);
			assertNativeUnchanged("after release", app, expectedInputs.get(), expectedTyped.get());
			assertSame(semantic, point(app, "SemanticP"));
			movedAddress.set(address(app)); assertNotEquals(initialAddress.get(), movedAddress.get());
			assertEquals(expectedInputs.get(), restored(app, expectedTyped.get()).inputs(),
					"The accepted semantic move itself preserves both unrelated native inputs");
		});
		for (int cycle = 0; cycle < 3; cycle++) {
		System.out.println("R3_NATIVE_UNDO_REPEAT cycle=" + cycle);
		Cu7M08SDesktopEventsTest.await(() -> undo.isQuiescent() && undo.undoPossible());
		Cu7M08SDesktopEventsTest.edt(() -> {
			assertEquals(1, undo.getHistorySize(), "One controller gesture creates one Undo state");
			app.getKernel().undo(); undoState.set(restored(app, expectedTyped.get()));
		});
		Cu7M08SDesktopEventsTest.await(undo::redoPossible);
		Cu7M08SDesktopEventsTest.edt(() -> {
			app.getKernel().redo(); redoState.set(restored(app, expectedTyped.get()));
		});
		// Capture both stages before asserting, retaining evidence of Undo and Redo
		// even when the inherited XML-only route changes an unrelated native input.
		assertAll("Normal Undo/Redo must preserve the native host as well as semantic identity",
				() -> assertEquals(expectedInputs.get(), undoState.get().inputs(), "Undo native input bits"),
				() -> assertTrue(undoState.get().typedMatches().values().stream().allMatch(Boolean::booleanValue),
						"Undo native source/domain and typed input"),
				() -> assertEquals(initialAddress.get(), undoState.get().address(), "Undo semantic address"),
				() -> assertEquals(identity.get(), undoState.get().pointId(), "Undo semantic identity"),
				() -> assertTrue(undoState.get().dependentFollows(), "Undo normal DAG dependent"),
				() -> assertEquals(expectedInputs.get(), redoState.get().inputs(), "Redo native input bits"),
				() -> assertTrue(redoState.get().typedMatches().values().stream().allMatch(Boolean::booleanValue),
						"Redo native source/domain and typed input"),
				() -> assertEquals(movedAddress.get(), redoState.get().address(), "Redo semantic address"),
				() -> assertEquals(identity.get(), redoState.get().pointId(), "Redo semantic identity"),
				() -> assertTrue(redoState.get().dependentFollows(), "Redo normal DAG dependent"));
		}
	}
}
