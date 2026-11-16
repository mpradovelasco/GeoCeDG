/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.NativeSourceFidelity2D;
import org.geocedg.common.kernel.locus.NativeTransactionalState2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoPoint.NativeRollbackPointState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** The author's exact cone must exercise publication, not merely offer discovery. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class GeoCeDG120ConePlacementLifecycleTest {
	private static final String SHA = "4cde946eba57bfd36d531c677dc9f622b9036d6e95cd92941311b51d5f786a01";
	private static final List<String> INPUTS = List.of("B", "C", "H", "A_2", "N", "E_1");
	@TempDir Path directory;

	private Path copy() throws Exception {
		Path source = Path.of(System.getProperty("geocedg.120.cone.fixture",
				"C:/Temp/InterCilConoOblique_2.cedg"));
		byte[] bytes = Files.readAllBytes(source);
		assertEquals(SHA, sha(bytes), "The original scientific fixture must be immutable");
		Path copy = directory.resolve("cone-working-copy.cedg");
		Files.write(copy, bytes);
		assertEquals(SHA, sha(Files.readAllBytes(copy)));
		System.out.println("R9_CONE_FIXTURE source=" + source + " sha256=" + SHA);
		return copy;
	}

	private static String sha(byte[] bytes) throws Exception {
		return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
	}
	private static GeoPoint point(AppGeoCeDG app, String label) {
		return (GeoPoint) app.getKernel().lookupLabel(label);
	}
	private static AlgoIntersectLineConic selector(AppGeoCeDG app) {
		return (AlgoIntersectLineConic) point(app, "J").getParentAlgorithm();
	}
	private static String nativeDescription(GeoElement value) {
		return "label=" + value.getLabelSimple() + " type=" + value.getClass().getSimpleName()
				+ " parent=" + (value.getParentAlgorithm() == null ? "independent"
						: value.getParentAlgorithm().getClass().getSimpleName());
	}
	private static void unsupportedFrontier(GeoElement value, Set<GeoElement> seen) {
		if (!seen.add(value) || NativeSourceFidelity2D.isRecoverableSource(value)) { return; }
		boolean unsupportedInput = false;
		if (value.getParentAlgorithm() != null) {
			for (GeoElement input : value.getParentAlgorithm().getInput()) {
				if (!NativeSourceFidelity2D.isRecoverableSource(input)) {
					unsupportedInput = true;
					unsupportedFrontier(input, seen);
				}
			}
		}
		if (!unsupportedInput) {
			System.out.println("R9_NATIVE_UNSUPPORTED_FRONTIER " + nativeDescription(value));
			if (value.getClass() == GeoAngle.class) {
				GeoAngle angle = (GeoAngle) value;
				System.out.println("R9_NATIVE_ANGLE raw=" + Double.toHexString(angle.getRawAngle())
						+ " value=" + Double.toHexString(angle.getValue())
						+ " style=" + angle.getAngleStyle() + " definition="
						+ (angle.getDefinition() == null ? "null"
								: Double.toHexString(angle.getDefinition().evaluateDouble())));
				describeExpression(angle.getDefinition(), "root",
						Collections.newSetFromMap(new IdentityHashMap<>()), 0);
			}
		}
	}
	private static void describeExpression(ExpressionValue value, String path,
			Set<ExpressionValue> seen, int depth) {
		if (value == null || depth > 12 || seen.size() > 128 || !seen.add(value)) { return; }
		String details = value instanceof GeoElement geo ? " " + nativeDescription(geo)
				: value instanceof MyDouble literal
						? " literal=" + Double.toHexString(literal.getDouble())
								+ " angle=" + literal.isAngle()
						: value instanceof ExpressionNode node ? " operation=" + node.getOperation() : "";
		System.out.println("R9_NATIVE_EXPRESSION path=" + path
				+ " type=" + value.getClass().getSimpleName() + details);
		if (value instanceof GeoElement) { return; }
		for (int index = 0; index < value.getChildCount(); index++) {
			describeExpression(value.getChild(index), path + "." + index, seen, depth + 1);
		}
	}
	private static Object selectorGuard(AlgoIntersectLineConic selection, String name) {
		try {
			var field = AlgoIntersectLineConic.class.getDeclaredField(name);
			field.setAccessible(true);
			Object value = field.get(selection);
			return value instanceof Boolean ? value : value != null;
		} catch (ReflectiveOperationException failure) {
			return failure.getClass().getSimpleName();
		}
	}
	private static void diagnoseNativeSources(AppGeoCeDG app) {
		for (String label : INPUTS) {
			GeoPoint input = point(app, label);
			GeoElement path = input.getPath().toGeoElement();
			boolean recoverable = NativeSourceFidelity2D.isRecoverableSource(path);
			System.out.println("R9_NATIVE_INPUT label=" + label + " capture="
					+ (input.captureNativeRollbackState() != null) + " recoverable=" + recoverable
					+ " path={" + nativeDescription(path) + "}");
			if (!recoverable) {
				unsupportedFrontier(path, Collections.newSetFromMap(new IdentityHashMap<>()));
			}
		}
		var selection = selector(app);
		System.out.println("R9_NATIVE_SELECTOR capture="
				+ (selection.captureNativeRollbackSelectionState() != null)
				+ " first=" + selection.isSelectionFirstIntersectionPending()
				+ " pending=" + selection.isSelectionPermutationPending()
				+ " permutation=" + java.util.Arrays.toString(selection.getSelectionPermutation())
				+ " tangent=" + selectorGuard(selection, "isDefinedAsTangent")
				+ " limited=" + selectorGuard(selection, "isLimitedPathSituation")
				+ " special=" + selectorGuard(selection, "possibleSpecialCase")
				+ " existing=" + (selection.getSelectionExistingIntersection() != null)
				+ " construction=" + selection.isInConstructionList());
	}
	private static LocusSemanticAddress2D address(AppGeoCeDG app) {
		return AlgoSemanticLocusPoint2D.requireSemanticParent(point(app, "PlacedEndpoint120"))
				.getCurrentSemanticAddress();
	}
	private record NativeSnapshot(Map<String, NativeRollbackPointState> inputs,
			AlgoIntersectLineConic.NativeRollbackSelectionState selection) {
		static NativeSnapshot capture(AppGeoCeDG app) {
			diagnoseNativeSources(app);
			Map<String, NativeRollbackPointState> inputs = new LinkedHashMap<>();
			for (String label : INPUTS) {
				var state = point(app, label).captureNativeRollbackState();
				assertNotNull(state, label);
				inputs.put(label, state);
			}
			var selection = selector(app).captureNativeRollbackSelectionState();
			assertNotNull(selection, "The actual saved cone must qualify without test normalization");
			return new NativeSnapshot(Map.copyOf(inputs), selection);
		}
		void assertMatches(AppGeoCeDG app) {
			inputs.forEach((label, state) -> assertTrue(state.matches(point(app, label)), label));
			assertTrue(selection.matches(selector(app)), "Actual J/K selector state");
		}
	}
	private record PointImage(double x, double y) {
		static PointImage capture(GeoPoint point) {
			return new PointImage(point.inhomX, point.inhomY);
		}
		void assertMatches(GeoPoint point, String role) {
			assertEquals(x, point.inhomX, 1e-9, role + " x");
			assertEquals(y, point.inhomY, 1e-9, role + " y");
		}
	}
	private record PublicImage(PointImage endpoint, PointImage firstRoot, PointImage secondRoot,
			double length) {
		static PublicImage capture(AppGeoCeDG app) {
			return new PublicImage(PointImage.capture(point(app, "PlacedEndpoint120")),
					PointImage.capture(point(app, "J")), PointImage.capture(point(app, "K")),
					((GeoNumeric) app.getKernel().lookupLabel("PlacedLength120")).getDouble());
		}
		void assertMatches(AppGeoCeDG app) {
			endpoint.assertMatches(point(app, "PlacedEndpoint120"), "placed endpoint");
			firstRoot.assertMatches(point(app, "J"), "ordered J root");
			secondRoot.assertMatches(point(app, "K"), "ordered K root");
			double actual = ((GeoNumeric) app.getKernel().lookupLabel("PlacedLength120")).getDouble();
			assertEquals(length, actual, 1e-8 * Math.max(1, Math.abs(length)), "public numeric value");
		}
	}

	private static GeoCeDGReferencePlacement offer(AppGeoCeDG app) {
		var locus = (GeoLocusV2) app.getKernel().lookupLabel("e_2");
		return new GeoCeDGReferencePlacement(locus, point(app, "P_1"),
				LocusPointInteractionPolicy2D.initial(.1));
	}

	@Test void actualConePlacementCommitsThenSurvivesUndoRedoAndReopen() throws Exception {
		Path copy = copy();
		AppGeoCeDG app = onEdt(() -> PreG9BCu0Corpus.open(copy.toFile()));
		awaitQuiescent(app);
		NativeSnapshot before = onEdt(() -> {
			diagnoseNativeSources(app);
			assertNull(NativeTransactionalState2D.unsupportedReason(app.getKernel().getConstruction()));
			var state = NativeSnapshot.capture(app);
			baseline(app);
			return state;
		});
		var identity = onEdt(() -> {
			try (var selection = offer(app)) {
				assertFalse(selection.getChoices().isEmpty(), selection.getFailure());
				assertEquals(GeoCeDGReferencePlacement.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION,
						selection.getRoute());
				GeoPoint result = selection.commit(selection.getChoices().get(0));
				result.setLabel("PlacedEndpoint120");
				assertTrue(result.isDefined());
				assertNotNull(AlgoSemanticLocusPoint2D.requireSemanticParent(result));
				assertFalse(java.util.Arrays.asList(result.getParentAlgorithm().getInput())
						.contains(point(app, "P_1")), "Placement never installs reference-Q as a parent");
			}
			GeoNumeric length = assertInstanceOf(GeoNumeric.class,
					must(app, "PlacedLength120=LocusLength(e_2,PlacedEndpoint120,R_1)"));
			assertTrue(Double.isFinite(length.getDouble()));
			before.assertMatches(app);
			app.storeUndoInfo();
			return app.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(point(app, "PlacedEndpoint120"));
		});
		assertNotNull(identity);
		awaitQuiescent(app);
		var savedAddress = onEdt(() -> address(app));
		var savedImage = onEdt(() -> PublicImage.capture(app));
		for (int cycle = 0; cycle < 3; cycle++) {
			onEdt(() -> { assertTrue(undo(app).undoPossible()); app.getKernel().undo(); });
			awaitQuiescent(app);
			onEdt(() -> {
				assertNull(app.getKernel().lookupLabel("PlacedEndpoint120"));
				before.assertMatches(app);
				assertTrue(undo(app).redoPossible()); app.getKernel().redo();
			});
			awaitQuiescent(app);
			onEdt(() -> {
				assertEquals(savedAddress, address(app));
				assertEquals(identity, app.getKernel().getConstruction().getSpatialIdentityRegistry()
						.getPersistentGeoId(point(app, "PlacedEndpoint120")));
				before.assertMatches(app);
			});
		}
		var file = onEdt(() -> save(app, directory, "cone-placed"));
		AppGeoCeDG reopened = onEdt(() -> PreG9BCu0Corpus.open(file));
		awaitQuiescent(reopened);
		onEdt(() -> {
			assertEquals(savedAddress, address(reopened));
			// FILE keeps historical native codecs: test geometric values and ordered roots,
			// without promoting their native parameter bits to a new FILE guarantee.
			savedImage.assertMatches(reopened);
			assertEquals(identity, reopened.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(point(reopened, "PlacedEndpoint120")));
			assertTrue(Double.isFinite(((GeoNumeric) reopened.getKernel()
					.lookupLabel("PlacedLength120")).getDouble()));
			assertFalse(reopened.getXML().contains(NativeTransactionalState2D.BINDING_TAG));
		});
		assertEquals(SHA, sha(Files.readAllBytes(copy)), "Tests only save new files");
	}

	@Test void actualConeFailedPublicationRestoresAllNativeInputsAndSelection() throws Exception {
		Path copy = copy();
		AppGeoCeDG app = onEdt(() -> PreG9BCu0Corpus.open(copy.toFile()));
		awaitQuiescent(app);
		onEdt(() -> {
			var before = NativeSnapshot.capture(app);
			String xml = app.getXML();
			var registry = app.getKernel().getConstruction().getSpatialIdentityRegistry();
			var ids = registry.getRecords().stream().map(record -> record.getId()).toList();
			int reserved = registry.getReservedIdentityCount();
			var injected = new IllegalStateException("R9 injected cone publication failure");
			AtomicBoolean entered = new AtomicBoolean();
			View observer = mock(View.class);
			doAnswer(invocation -> {
				var added = invocation.getArgument(0);
				if (added instanceof GeoPoint output
						&& output.getParentAlgorithm() instanceof AlgoSemanticLocusPoint2D
						&& entered.compareAndSet(false, true)) {
					GeoPoint input = point(app, "B");
					input.setCoords(input.inhomX, input.inhomY + .2, 1);
					input.updateCascade();
					throw injected;
				}
				return null;
			}).when(observer).add(any());
			app.getKernel().attach(observer);
			try (var selection = offer(app)) {
				assertFalse(selection.getChoices().isEmpty(), selection.getFailure());
				assertSame(injected, assertThrows(IllegalStateException.class,
						() -> selection.commit(selection.getChoices().get(0))));
			} finally { app.getKernel().detach(observer); }
			assertTrue(entered.get(), "The test must reach actual publication");
			assertEquals(xml, app.getXML());
			before.assertMatches(app);
			assertEquals(ids, registry.getRecords().stream().map(record -> record.getId()).toList());
			assertEquals(reserved, registry.getReservedIdentityCount());
			assertEquals(1, app.getKernel().getConstruction().getLastNativeTransactionMetrics().dagCalls);
		});
	}
}
