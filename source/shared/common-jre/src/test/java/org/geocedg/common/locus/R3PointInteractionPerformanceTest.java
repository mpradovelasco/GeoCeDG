/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Diagnostic timings with functional assertions and no duration acceptance threshold. */
class R3PointInteractionPerformanceTest extends G9U0PublicSurfaceTestBase {

	@ParameterizedTest(name = "stages {0}")
	@ValueSource(strings = {"circle", "segment", "line-circle"})
	void changedNoopAndRollbackStagesAcrossDocumentSizes(String family) {
		if ("segment".equals(family)) {
			add("driverPath=Segment((0,0),(1,0))");
		} else {
			add("driverPath=Circle((0,0)," + ("line-circle".equals(family) ? "2)" : "1)"));
		}
		GeoPoint driver = add("N=Point(driverPath)");
		double parameter = "segment".equals(family) ? .3 : Math.PI / 2;
		driver.updatePathParameter(parameter);
		driver.updateCascade();
		if ("line-circle".equals(family)) {
			add("g=Line(N,yAxis)");
			add("f=Circle((0,0),1)");
			add("Q=Intersect(g,f,1)");
		} else {
			add("v=Vector((0,0),N)");
			add("Q=N+(2*v)");
		}
		GeoLocusV2 source = add("L=LocusV2(Q,N)");
		add("unrelatedPath=Segment((4,0),(6,1))");
		GeoPoint nativePoint = add("NativeU=Point(unrelatedPath)");
		nativePoint.updatePathParameter(.37123456789);
		nativePoint.updateCascade();
		var registry = getConstruction().getSpatialIdentityRegistry();
		assertNull(registry.getPersistentGeoId(nativePoint));
		var policy = LocusPointInteractionPolicy2D.initial(.1);
		LocusPoint2D initial = at(source, parameter);
		GeoPoint point;
		try (var selection = LocusPointCreation2D.query(source, initial.getX(), initial.getY(), policy)) {
			var choice = selection.getChoices().stream().min(java.util.Comparator.comparingDouble(
					value -> Math.abs(value.getAddress().getCanonicalParameter() - parameter)))
					.orElseThrow(() -> new AssertionError(selection.getFailure()));
			point = selection.commit("P", choice);
		}
		for (int index = 0; index < 32; index++) {
			add("Dep" + index + "=P+(" + (index + 1) + ",2)");
		}
		for (int index = 0; index < 8; index++) {
			add("depCircle" + index + "=Circle(Dep" + index + ",1)");
		}
		var pointId = registry.getPersistentGeoId(point);
		assertNotNull(pointId);
		int previousCount = 0;
		double requestedParameter = parameter;
		for (int count : new int[] {100, 1000, 10000}) {
			for (int index = previousCount; index < count; index++) {
				new GeoNumeric(getConstruction(), index + .25).setLabel("perfN" + index);
			}
			previousCount = count;
			int xmlChars = getApp().getXML().length();
			var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
			long[] total = new long[9], resolution = new long[9], nativeCapture = new long[9];
			long[] xml = new long[9], dag = new long[9], noop = new long[9];
			long[] evaluations = new long[9], proofs = new long[9];
			for (int iteration = 0; iteration < 11; iteration++) {
				requestedParameter += .005;
				LocusPoint2D target = at(source, requestedParameter);
				long started = System.nanoTime();
				var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
						target.getX(), target.getY(), policy, context);
				long elapsed = System.nanoTime() - started;
				assertTrue(result.isEstablished(), result.getDiagnostic());
				assertTrue(result.hasChanged());
				var metrics = getConstruction().getLastNativeTransactionMetrics();
				assertNotNull(metrics);
				assertNull(metrics.unsupported);
				assertTrue(metrics.capturedPoints >= 2);
				assertTrue(metrics.privateBindings >= 1);
				assertEquals(0, metrics.dagCalls, "Accepted move does not restore the XML DAG");
				started = System.nanoTime();
				var repeated = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
						target.getX(), target.getY(), policy, context);
				long noopElapsed = System.nanoTime() - started;
				assertTrue(repeated.isEstablished(), repeated.getDiagnostic());
				assertFalse(repeated.hasChanged());
				assertEquals(0, repeated.getDagPropagationNanos());
				assertSame(metrics, getConstruction().getLastNativeTransactionMetrics(),
						"No-op must not capture another native/XML snapshot");
				assertEquals(pointId, registry.getPersistentGeoId(point));
				if (iteration >= 2) {
					int sample = iteration - 2;
					total[sample] = elapsed; resolution[sample] = result.getResolutionNanos();
					nativeCapture[sample] = metrics.nativeCaptureNanos;
					xml[sample] = metrics.xmlCaptureNanos;
					dag[sample] = result.getDagPropagationNanos(); noop[sample] = noopElapsed;
					evaluations[sample] = result.getSemanticEvaluations();
					proofs[sample] = result.getIntervalPasses();
				}
			}
			System.out.println("R3_PERFORMANCE family=" + family + " unrelated=" + count
					+ " xmlChars=" + xmlChars + " samples=9 totalMedianMs=" + median(total)
					+ " totalMaxMs=" + maximum(total) + " resolutionMedianMs=" + median(resolution)
					+ " nativeCaptureMedianMs=" + median(nativeCapture) + " xmlMedianMs=" + median(xml)
					+ " dagMedianMs=" + median(dag) + " noopMedianMs=" + median(noop)
					+ " semanticEvaluations=" + Arrays.toString(evaluations)
					+ " proofPasses=" + Arrays.toString(proofs)
					+ " gui=NOT_MEASURED_HEADLESS stageSamplesNotAdditive=true");
		}
		String before = getApp().getXML();
		var expectedAddress = AlgoSemanticLocusPoint2D.requireSemanticParent(point)
				.getCurrentSemanticAddress();
		var nativeBefore = nativePoint.captureNativeRollbackState();
		assertNotNull(nativeBefore);
		IllegalStateException rejection = assertThrows(IllegalStateException.class,
				() -> getConstruction().runAtomicConstructionMutation(() -> {
					nativePoint.updatePathParameter(.6123456789);
					nativePoint.updateCascade();
					throw new IllegalStateException("PERF_EXPECTED_ROLLBACK");
				}));
		assertEquals("PERF_EXPECTED_ROLLBACK", rejection.getMessage());
		assertEquals(before, getApp().getXML());
		assertTrue(nativeBefore.matches((GeoPoint) lookup("NativeU")));
		GeoPoint restored = (GeoPoint) getConstruction().getSpatialIdentityRegistry().getGeo(pointId);
		assertNotNull(restored);
		assertEquals(expectedAddress,
				AlgoSemanticLocusPoint2D.requireSemanticParent(restored).getCurrentSemanticAddress());
		var metrics = getConstruction().getLastNativeTransactionMetrics();
		assertTrue(metrics.dagCalls >= 1);
		System.out.println("R3_RESTORE_PERFORMANCE family=" + family + " captured="
				+ metrics.capturedPoints + " privateBindings=" + metrics.privateBindings
				+ " xmlRestoreInclusiveMs=" + metrics.xmlRestoreInclusiveNanos / 1e6
				+ " dagRestoreSubsetMs=" + metrics.dagRestoreSubsetNanos / 1e6
				+ " verifySubsetMs=" + metrics.verifySubsetNanos / 1e6
				+ " dagCalls=" + metrics.dagCalls + " gui=NOT_MEASURED_HEADLESS");
	}

	private LocusPoint2D at(GeoLocusV2 source, double parameter) {
		try (var session = LocusEvaluationSession2D.memoizing(32)) {
			var value = source.getSemanticDefinition().evaluate(BRANCH, parameter, session);
			assertTrue(value.isValid());
			return value.getPoint();
		}
	}

	private static double median(long[] samples) {
		long[] ordered = samples.clone();
		Arrays.sort(ordered);
		return ordered[ordered.length / 2] / 1e6;
	}

	private static double maximum(long[] samples) {
		return Arrays.stream(samples).max().orElseThrow() / 1e6;
	}
}
