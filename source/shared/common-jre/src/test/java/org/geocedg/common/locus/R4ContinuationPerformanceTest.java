/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointContinuation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/** Portable R3/R4 diagnostic benchmark; all raws precede the new R4 assertions. */
class R4ContinuationPerformanceTest extends G9U0PublicSurfaceTestBase {
	private static final double START = .11, END = 1.91;
	private static final LocusPointInteractionPolicy2D POLICY = LocusPointInteractionPolicy2D.initial(.5);
	private static final List<String> OPTIONAL_COUNTERS = List.of(
			"getContinuationChartAttempts", "getContinuationCharts", "getContinuationSubdivisions",
			"getContinuationIntervalEvaluations", "getContinuationCorrespondenceChecks",
			"getContinuationSimilarityApplications");
	private enum Mode { SMALL_ONE_EVENT, LARGE_ONE_EVENT, CHORD_24_EVENTS }
	private record Sample(Mode mode, int iteration, Map<String, Object> raw, boolean accepted,
			double finalParameter, int pointUpdates, int dependentUpdates, int recordsChanged) { }

	@Test void collectAllModesBeforeAssertingTheNewLargeEventCapability() {
		List<Sample> measured = new ArrayList<>();
		for (Mode mode : Mode.values()) {
			for (int iteration = -2; iteration < 9; iteration++) {
				Sample sample = sample(mode, iteration);
				System.out.println("R4_CONTINUATION_RAW " + json(sample.raw));
				if (iteration >= 0) { measured.add(sample); }
			}
			var selected = measured.stream().filter(value -> value.mode == mode).toList();
			var summary = new LinkedHashMap<String, Object>();
			summary.put("mode", mode.name()); summary.put("warmups", 2); summary.put("samples", 9);
			for (String field : List.of("publicApiNanos", "resolutionNanos", "nativeCaptureNanos",
					"xmlCaptureNanos", "dagPropagationNanos")) {
				long[] values = selected.stream().mapToLong(value -> ((Number) value.raw.get(field)).longValue()).toArray();
				long[] sorted = values.clone(); Arrays.sort(sorted);
				summary.put(field + "MedianMs", sorted[sorted.length / 2] / 1e6);
				summary.put(field + "MaximumMs", Arrays.stream(values).max().orElseThrow() / 1e6);
			}
			summary.put("gui", "NOT_MEASURED_HEADLESS");
			summary.put("timingGate", false); summary.put("stageMediansAdditive", false);
			System.out.println("R4_CONTINUATION_SUMMARY " + json(summary));
		}
		// R3 deliberately fails the large-event expectation only AFTER all baseline modes exist.
		List<Executable> checks = new ArrayList<>();
		for (Sample sample : measured) {
			checks.add(() -> {
				String label = sample.mode + " sample=" + sample.iteration + " " + json(sample.raw);
				assertTrue(sample.accepted, label);
				assertEquals(sample.mode == Mode.SMALL_ONE_EVENT ? .12 : END, sample.finalParameter, 3e-11, label);
				int expected = sample.mode == Mode.CHORD_24_EVENTS ? 24 : 1;
				assertEquals(expected, sample.pointUpdates, "One final point update per external event: " + label);
				assertEquals(expected, sample.dependentUpdates, "Normal dependent DAG updates: " + label);
				assertEquals(expected, sample.recordsChanged, "Observed transaction records per event: " + label);
				for (String invariant : List.of("identityRetained", "componentRetained", "countRetained", "dependentValid")) {
					assertEquals(Boolean.TRUE, sample.raw.get(invariant), invariant + ": " + label);
				}
				if (sample.mode == Mode.LARGE_ONE_EVENT && sample.raw.get("getContinuationCharts") != null) {
					assertTrue(((Number) sample.raw.get("getContinuationCharts")).longValue() > 1, label);
					assertTrue(((Number) sample.raw.get("getContinuationSubdivisions")).longValue() > 0, label);
				}
			});
		}
		assertAll("R4 MEET continuation functionality; timings are informational", checks);
	}

	private Sample sample(Mode mode, int iteration) {
		AppCommon app = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		for (String command : List.of("c=Circle((0,0),1)", "N=Point(c)", "horizontal=Line(N,xAxis)",
				"vertical=Line(N,yAxis)", "Q=Intersect(horizontal,vertical)", "L=LocusV2(Q,N)")) { eval(app, command); }
		GeoLocusV2 source = (GeoLocusV2) app.getKernel().lookupLabel("L");
		LocusPoint2D initial = at(source, .1); GeoPoint point;
		try (var offer = LocusPointCreation2D.query(source, initial.getX(), initial.getY(), POLICY)) {
			assertFalse(offer.getChoices().isEmpty(), offer.getFailure());
			point = offer.commit("P", offer.getChoices().stream().min(Comparator.comparingDouble(
					choice -> Math.abs(choice.getAddress().getCanonicalParameter() - .1))).orElseThrow());
		}
		GeoPoint dependent = (GeoPoint) eval(app, "Dependent=P+(2,3)");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		var primed = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				Math.cos(START), Math.sin(START), POLICY, context);
		assertTrue(primed.isEstablished(), primed.getDiagnostic());
		var construction = app.getKernel().getConstruction();
		var identity = construction.getSpatialIdentityRegistry().getPersistentGeoId(point);
		var oldAddress = AlgoSemanticLocusPoint2D.requireSemanticParent(point).getCurrentSemanticAddress();
		int oldCount = construction.getGeoSetConstructionOrder().size();
		AtomicInteger pointUpdates = new AtomicInteger(), dependentUpdates = new AtomicInteger();
		View observer = mock(View.class);
		doAnswer(invocation -> {
			Object updated = invocation.getArgument(0);
			if (updated == point) { pointUpdates.incrementAndGet(); }
			if (updated == dependent) { dependentUpdates.incrementAndGet(); }
			return null;
		}).when(observer).update(any(GeoElement.class));
		var raw = new LinkedHashMap<String, Object>();
		raw.put("mode", mode.name()); raw.put("iteration", iteration); raw.put("warmup", iteration < 0);
		raw.put("radiusWorld", .5); raw.put("startParameter", START);
		raw.put("targetParameter", mode == Mode.SMALL_ONE_EVENT ? .12 : END);
		raw.put("requests", mode == Mode.CHORD_24_EVENTS ? 24 : 1);
		for (String counter : OPTIONAL_COUNTERS) { raw.put(counter, null); }
		long total = 0, resolution = 0, semantic = 0, proofs = 0, nativeCapture = 0, xml = 0, dag = 0;
		int recordsChanged = 0, completed = 0; boolean accepted = true;
		List<String> statuses = new ArrayList<>(), methods = new ArrayList<>();
		app.getKernel().attach(observer);
		try {
			int requests = mode == Mode.CHORD_24_EVENTS ? 24 : 1;
			for (int request = 1; request <= requests; request++) {
				double fraction = request / 24.0;
				double x = mode == Mode.SMALL_ONE_EVENT ? Math.cos(.12) : mode == Mode.LARGE_ONE_EVENT
						? Math.cos(END) : (1 - fraction) * Math.cos(START) + fraction * Math.cos(END);
				double y = mode == Mode.SMALL_ONE_EVENT ? Math.sin(.12) : mode == Mode.LARGE_ONE_EVENT
						? Math.sin(END) : (1 - fraction) * Math.sin(START) + fraction * Math.sin(END);
				var previousMetrics = construction.getLastNativeTransactionMetrics();
				long started = System.nanoTime();
				LocusPointContinuation2D.Result result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(
						point, x, y, POLICY, context);
				total += System.nanoTime() - started;
				resolution += result.getResolutionNanos(); dag += result.getDagPropagationNanos();
				semantic += result.getSemanticEvaluations(); proofs += result.getIntervalPasses();
				statuses.add(result.getStatus().name()); methods.add(result.getMethod()); completed++;
				for (String counter : OPTIONAL_COUNTERS) {
					Long value = optionalCounter(result, counter);
					Object previous = raw.get(counter);
					raw.put(counter, value == null ? null : (previous == null ? 0L : ((Number) previous).longValue()) + value);
				}
				var metrics = construction.getLastNativeTransactionMetrics();
				if (metrics != previousMetrics) {
					recordsChanged++; assertNotNull(metrics); assertNull(metrics.unsupported);
					nativeCapture += metrics.nativeCaptureNanos; xml += metrics.xmlCaptureNanos;
					assertEquals(0, metrics.dagCalls, "Accepted moves do not run the rollback DAG");
				}
				accepted &= result.isEstablished();
				if (!result.isEstablished()) { break; }
			}
		} finally { app.getKernel().detach(observer); }
		var address = AlgoSemanticLocusPoint2D.requireSemanticParent(point).getCurrentSemanticAddress();
		raw.put("completedRequests", completed); raw.put("accepted", accepted);
		raw.put("statuses", statuses); raw.put("methods", methods);
		raw.put("publicApiNanos", total); raw.put("resolutionNanos", resolution);
		raw.put("semanticEvaluations", semantic); raw.put("intervalPassesBudgetUnits", proofs);
		raw.put("nativeCaptureNanos", nativeCapture); raw.put("xmlCaptureNanos", xml);
		raw.put("dagPropagationNanos", dag); raw.put("pointUpdateNotifications", pointUpdates.get());
		raw.put("dependentUpdateNotifications", dependentUpdates.get());
		raw.put("transactionMetricRecordChanges", recordsChanged);
		raw.put("snapshotCounterScope", "LAST_RECORD_IDENTITY_PER_PUBLIC_CALL_NOT_INTERNAL_COUNT");
		raw.put("optionalCounterState", raw.get("getContinuationCharts") == null ? "NOT_AVAILABLE" : "AVAILABLE");
		raw.put("finalParameter", address.getCanonicalParameter()); raw.put("lift", address.getPeriodicLift());
		raw.put("identityRetained", identity.equals(construction.getSpatialIdentityRegistry().getPersistentGeoId(point)));
		raw.put("componentRetained", oldAddress.getComponentLineageKey().equals(address.getComponentLineageKey()));
		raw.put("countRetained", oldCount == construction.getGeoSetConstructionOrder().size());
		raw.put("dependentValid", dependent.inhomX == point.inhomX + 2 && dependent.inhomY == point.inhomY + 3);
		return new Sample(mode, iteration, raw, accepted, address.getCanonicalParameter(), pointUpdates.get(),
				dependentUpdates.get(), recordsChanged);
	}

	private static GeoElement eval(AppCommon app, String command) {
		var result = app.getKernel().getAlgebraProcessor().processAlgebraCommand(command, false);
		assertNotNull(result, command); assertTrue(result.length > 0, command); return (GeoElement) result[0];
	}
	private static LocusPoint2D at(GeoLocusV2 source, double parameter) {
		try (var session = LocusEvaluationSession2D.memoizing(100)) {
			var result = source.getSemanticDefinition().evaluate(BRANCH, parameter, session);
			assertTrue(result.isValid()); return result.getPoint();
		}
	}
	private static Long optionalCounter(Object result, String getter) {
		try { return ((Number) result.getClass().getMethod(getter).invoke(result)).longValue(); }
		catch (NoSuchMethodException absentInR3) { return null; }
		catch (IllegalAccessException | InvocationTargetException failure) { throw new AssertionError(getter, failure); }
	}
	private static String json(Object value) {
		if (value == null) { return "null"; }
		if (value instanceof Number || value instanceof Boolean) { return value.toString(); }
		if (value instanceof Map<?, ?> map) {
			return map.entrySet().stream().map(entry -> json(entry.getKey().toString()) + ":" + json(entry.getValue()))
					.collect(java.util.stream.Collectors.joining(",", "{", "}"));
		}
		if (value instanceof List<?> list) {
			return list.stream().map(R4ContinuationPerformanceTest::json)
					.collect(java.util.stream.Collectors.joining(",", "[", "]"));
		}
		return "\"" + value.toString().replace("\\", "\\\\").replace("\"", "\\\"")
				.replace("\n", "\\n").replace("\r", "\\r") + "\"";
	}
}
