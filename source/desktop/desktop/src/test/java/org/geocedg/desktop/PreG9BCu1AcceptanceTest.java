/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInstrumentationSnapshot2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricCapabilityHierarchy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricEngine2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexMode;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricOwnerLease2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D;
import org.geocedg.common.kernel.locus.metric.MetricGuaranteeClass2D;
import org.geocedg.common.kernel.locus.metric.PolicyV2LocusMetricCapability2D;
import org.geocedg.common.kernel.locus.metric.TotalLocusMetricQuery;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU1 acceptance on the complete author model ({@code locus-v2-semantics.md} v1.2
 * and {@code locus-v2-metrics.md} §25; validation matrix V-REV-1, V-SLICE-1, V-NEST-1,
 * V-NEST-2, V-CACHE-1, V-DAG-1 and V-MET-EST-3; gates G2 to G4 and G7). The public counters
 * of {@link LocusInstrumentation2D} are the product evidence; the reflective access to the
 * isolated slice is only an independent cross-check of their per-level sums.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu1AcceptanceTest {

	/** G4 target approved with D-CU1-12: nested evaluations per point query of e_2. */
	static final long NESTED_EVALUATIONS_PER_POINT_QUERY = 256;

	/** Saved D_1 is (8.871653198789899, 7.8269897780685485); the move adds 0.1 to x. */
	private static final String MOVED_D1_X = "8.971653198789899";
	private static final Pattern D1_COORDS = Pattern.compile(
			"(<element type=\"point\" label=\"D_1\">[\\s\\S]*?<coords x=\")([^\"]+)(\")");

	@TempDir
	Path work;

	/** V-REV-1 (G2): driver moves keep the revision, the slice and the metric states. */
	@Test
	void driverMovesKeepTheRevisionSliceAndMetricStatesOfO1() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			GeoLocusV2 o1 = PreG9BCu0Corpus.locus(app, "o_1");
			Double before = PreG9BCu0References.totalLength(app, "o_1");
			long revision = o1.getSemanticRevision();
			LocusInstrumentationSnapshot2D start = o1.getInstrumentation().snapshot();
			long builds = o1.getMetricInstrumentation().snapshot().getComponentStateBuilds();
			double saved = PreG9BCu0Corpus.pathParameter(app, "E_1");
			for (double delta : new double[] {0.05, -0.3, 1.1}) {
				PreG9BCu0Corpus.movePathPoint(app, "E_1", saved + delta);
			}
			PreG9BCu0Corpus.movePathPoint(app, "E_1", saved);
			LocusInstrumentationSnapshot2D end = o1.getInstrumentation().snapshot();
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("revision_delta", o1.getSemanticRevision() - revision);
			values.put("slice_builds_delta", end.getDependencySliceBuilds()
					- start.getDependencySliceBuilds());
			values.put("equivalent_recomputes_delta", end.getEquivalentRecomputes()
					- start.getEquivalentRecomputes());
			values.put("metric_builds_delta", o1.getMetricInstrumentation().snapshot()
					.getComponentStateBuilds() - builds);
			values.put("length_before", before);
			values.put("length_after", PreG9BCu0References.number(app, "cu0Totalo1"));
			return values;
		});
		assertEquals(0L, row.get("revision_delta"), row.toString());
		assertEquals(0L, row.get("slice_builds_delta"), row.toString());
		assertEquals(0L, row.get("metric_builds_delta"), row.toString());
		assertTrue((Long) row.get("equivalent_recomputes_delta") >= 4, row.toString());
		assertNotNull(row.get("length_before"));
		assertEquals(Double.doubleToLongBits((Double) row.get("length_before")),
				Double.doubleToLongBits((Double) row.get("length_after")), row.toString());
	}

	/**
	 * V-SLICE-1, V-NEST-1 and V-NEST-2 (G3, G4): point queries of e_2 reuse its slice
	 * and the nested copy of o_1 with its metric states; their nested work is local and
	 * the aggregated public counters equal the per-level sums.
	 */
	@Test
	void pointQueriesOfE2ReuseTheNestedSliceAndStayLocal() throws Exception {
		Map<String, Object> result = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_2");
			List<GeoLocusV2> children = nestedChildren(e2);
			long childrenStart = ownAndNestedCalls(children);
			LocusInstrumentationSnapshot2D first = e2.getInstrumentation().snapshot();
			List<Object> rows = new ArrayList<>();
			for (double t : PreG9BCu0References.e2Parameters()) {
				LocusInstrumentationSnapshot2D before = e2.getInstrumentation().snapshot();
				LocusPoint2D point;
				try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
					point = PreG9BCu0Corpus.point(e2, t, session);
				}
				LocusInstrumentationSnapshot2D after = e2.getInstrumentation().snapshot();
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("t", t);
				row.put("valid", point != null);
				row.put("nested_evaluator_calls", after.getNestedEvaluatorCalls()
						- before.getNestedEvaluatorCalls());
				row.put("nested_slice_builds", after.getNestedSliceBuilds()
						- before.getNestedSliceBuilds());
				row.put("nested_revision_publications", after.getNestedRevisionPublications()
						- before.getNestedRevisionPublications());
				row.put("nested_metric_builds", after.getNestedMetricComponentBuilds()
						- before.getNestedMetricComponentBuilds());
				rows.add(row);
			}
			LocusInstrumentationSnapshot2D last = e2.getInstrumentation().snapshot();
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("rows", rows);
			values.put("slice_builds_total", last.getDependencySliceBuilds());
			values.put("slice_builds_delta", last.getDependencySliceBuilds()
					- first.getDependencySliceBuilds());
			values.put("aggregated_nested_calls", last.getNestedEvaluatorCalls()
					- first.getNestedEvaluatorCalls());
			values.put("per_level_nested_calls", ownAndNestedCalls(children)
					- childrenStart);
			values.put("children", children.size());
			return values;
		});
		String summary = result.toString();
		assertEquals(1L, result.get("slice_builds_total"), summary);
		assertEquals(0L, result.get("slice_builds_delta"), summary);
		assertTrue((Integer) result.get("children") >= 1, summary);
		assertEquals(result.get("per_level_nested_calls"),
				result.get("aggregated_nested_calls"), summary);
		for (Object item : (List<?>) result.get("rows")) {
			Map<?, ?> row = (Map<?, ?>) item;
			assertEquals(Boolean.TRUE, row.get("valid"), row.toString());
			assertEquals(0L, row.get("nested_slice_builds"), row.toString());
			assertEquals(0L, row.get("nested_revision_publications"), row.toString());
			assertEquals(0L, row.get("nested_metric_builds"), row.toString());
			assertTrue((Long) row.get("nested_evaluator_calls")
					<= NESTED_EVALUATIONS_PER_POINT_QUERY, row.toString());
		}
	}

	/**
	 * V-CACHE-1 (G1, G2): a change of a non-driver input of the chain o_1 -> d_2 -> e_2
	 * (the free point D_1) discards every metric state of the previous revision, and the
	 * results equal those of a fresh document saved with the same state; a change of an
	 * input outside the chain (excCyl, which only drives the cylinder family) neither
	 * republishes o_1 nor discards its metric states.
	 */
	@Test
	void nonDriverChangesDiscardMetricStatesAndMatchAFreshDocument() throws Exception {
		File changedCopy = PreG9BCu0Corpus.withoutE2(
				Files.createDirectories(work.resolve("changed")));
		File freshCopy = withD1X(PreG9BCu0Corpus.withoutE2(
				Files.createDirectories(work.resolve("fresh"))), MOVED_D1_X);
		Map<String, Object> result = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG changed = PreG9BCu0Corpus.open(changedCopy);
			GeoLocusV2 o1 = PreG9BCu0Corpus.locus(changed, "o_1");
			PreG9BCu0References.totalLength(changed, "o_1");
			long revision = o1.getSemanticRevision();
			long invalidations = o1.getMetricInstrumentation().snapshot().getInvalidations();
			long builds = o1.getMetricInstrumentation().snapshot().getComponentStateBuilds();
			PreG9BCu0Corpus.setNumber(changed, "excCyl", 3.3);
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("outside_revision_delta", o1.getSemanticRevision() - revision);
			values.put("outside_builds_delta", o1.getMetricInstrumentation().snapshot()
					.getComponentStateBuilds() - builds);
			GeoPoint d1 = (GeoPoint) changed.getKernel().lookupLabel("D_1");
			d1.setCoords(Double.parseDouble(MOVED_D1_X), d1.getInhomY(), 1);
			d1.updateRepaint();
			values.put("revision_delta", o1.getSemanticRevision() - revision);
			values.put("invalidations_delta", o1.getMetricInstrumentation().snapshot()
					.getInvalidations() - invalidations);
			values.put("changed", observables(changed,
					PreG9BCu0References.number(changed, "cu0Totalo1")));
			AppGeoCeDG fresh = PreG9BCu0Corpus.open(freshCopy);
			PreG9BCu0Corpus.setNumber(fresh, "excCyl", 3.3);
			values.put("fresh", observables(fresh,
					PreG9BCu0References.totalLength(fresh, "o_1")));
			return values;
		});
		String summary = result.toString();
		assertEquals(0L, result.get("outside_revision_delta"), summary);
		assertEquals(0L, result.get("outside_builds_delta"), summary);
		assertTrue((Long) result.get("revision_delta") >= 1, summary);
		assertTrue((Long) result.get("invalidations_delta") >= 1, summary);
		assertEquals(result.get("fresh"), result.get("changed"), summary);
	}

	/**
	 * V-DAG-1 (estimate contract, §25.5): the metric input of d_2 is an estimate and the
	 * length of e_2, which depends on it, is at most an estimate.
	 */
	@Test
	void estimatedMetricInputsKeepDownstreamLengthsEstimated() throws Exception {
		Map<String, Object> result = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			Map<String, Object> values = new LinkedHashMap<>();
			GeoElement w1 = app.getKernel().lookupLabel("w_1");
			values.put("w_1", ((GeoLocusMetricResult) w1).getMetricResult()
					.getGuaranteeClass());
			values.put("d_2_defined", app.getKernel().lookupLabel("d_2").isDefined());
			values.put("e_2_defined", PreG9BCu0Corpus.locus(app, "e_2").isDefined());
			GeoElement length = PreG9BR6PlusF3Fixtures.q(app, "cu1E2=LocusLength(e_2)");
			LocusMetricResult2D rich = ((GeoLocusMetricResult) length).getMetricResult();
			values.put("e_2_length", rich.getGuaranteeClass());
			values.put("e_2_diagnostics", rich.getDiagnostics().toString());
			return values;
		});
		String summary = result.toString();
		assertEquals(MetricGuaranteeClass2D.ESTIMATED, result.get("w_1"), summary);
		assertEquals(Boolean.TRUE, result.get("d_2_defined"), summary);
		assertEquals(Boolean.TRUE, result.get("e_2_defined"), summary);
		assertNotEquals(MetricGuaranteeClass2D.STRUCTURAL, result.get("e_2_length"), summary);
		assertNotEquals(MetricGuaranteeClass2D.CERTIFIED_ENCLOSURE, result.get("e_2_length"),
				summary);
	}

	/**
	 * V-MET-EST-3 and G6 (H04): the length of o_1 agrees with the independent CU0 reference
	 * within 1E-8 and with the preserved CU0 value within 1E-6, is an admissible estimate,
	 * and its complete component state costs at most a quarter of the evaluation budget.
	 */
	@Test
	void totalLengthOfO1MeetsItsIndependentReference() throws Exception {
		JSONObject baseline = PreG9BCu0Baseline.load();
		Object[] measured = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			LocusMetricResult2D result = ((GeoLocusMetricResult) PreG9BR6PlusF3Fixtures.q(
					app, "cu1O1=LocusLength(o_1)")).getMetricResult();
			return new Object[] {result, freshComponentBuildCalls(
					PreG9BCu0Corpus.locus(app, "o_1"))};
		});
		LocusMetricResult2D rich = (LocusMetricResult2D) measured[0];
		long buildCalls = (Long) measured[1];
		JSONObject preserve = baseline.getJSONObject("preserve");
		double value = rich.getMetricValue().getFiniteValue().orElseThrow();
		String summary = rich.getGuaranteeClass() + " " + value + " "
				+ rich.getErrorEvidence().getEstablishedAbsoluteAmount() + " "
				+ rich.getDiagnostics() + " build calls " + buildCalls;
		assertEquals(MetricGuaranteeClass2D.ESTIMATED, rich.getGuaranteeClass(), summary);
		assertEquals(preserve.getDouble("o_1_total_length_independent"), value, 1E-8,
				summary);
		assertEquals(preserve.getDouble("o_1_total_length"), value, 1E-6, summary);
		assertTrue(rich.isScalarAdmissible(), summary);
		assertTrue(buildCalls <= LocusMetricPolicy2D.DEFAULT_MAXIMUM_EVALUATIONS / 4,
				summary);
	}

	/** Evaluator calls of one complete component-state build of o_1 on a fresh owner. */
	private static long freshComponentBuildCalls(GeoLocusV2 locus) {
		LocusDefinition2D definition = locus.getSemanticDefinition();
		LocusMetricInstrumentation2D instrumentation = new LocusMetricInstrumentation2D();
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D(
				definition.getLocusIdentity(), instrumentation);
		try (LocusMetricOwnerLease2D lease = owner.acquireLease()) {
			new LocusMetricEngine2D().compute(new TotalLocusMetricQuery(
					definition.getLocusIdentity(), definition.getSemanticRevision(),
					LocusMetricPolicy2D.cu1PublicV2()), definition,
					new LocusMetricCapabilityHierarchy2D(
							List.of(new PolicyV2LocusMetricCapability2D())),
					owner, LocusMetricIndexMode.LAZY_COMPONENT_REVISION, instrumentation,
					"cu1-g6-budget-probe");
		}
		return instrumentation.snapshot().getEvaluatorCalls();
	}

	private static Map<String, Object> observables(AppGeoCeDG app, Double length) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("o_1_length_bits", length == null ? null
				: Double.doubleToLongBits(length));
		values.put("d_2_bits", Double.doubleToLongBits(
				PreG9BCu0References.number(app, "d_2")));
		List<Object> samples = new ArrayList<>();
		for (Object row : PreG9BCu0References.samples(PreG9BCu0Corpus.locus(app, "o_1"),
				PreG9BCu0References.grid(24))) {
			List<?> values2 = (List<?>) row;
			samples.add(List.of(values2.get(0),
					values2.get(1) == null ? "invalid"
							: Double.doubleToLongBits((Double) values2.get(1)),
					values2.get(2) == null ? "invalid"
							: Double.doubleToLongBits((Double) values2.get(2))));
		}
		values.put("o_1_samples_bits", samples);
		return values;
	}

	/** Direct nested locus copies of e_2 (independent reflective cross-check only). */
	private static List<GeoLocusV2> nestedChildren(GeoLocusV2 outer)
			throws ReflectiveOperationException {
		List<GeoLocusV2> children = new ArrayList<>();
		for (GeoElement geo : PreG9BCu0References.isolatedConstruction(outer)
				.getGeoSetConstructionOrder()) {
			if (geo instanceof GeoLocusV2 copy && copy.getInstrumentation()
					.getNestedParent() == outer.getInstrumentation()) {
				children.add(copy);
			}
		}
		return children;
	}

	private static long ownAndNestedCalls(List<GeoLocusV2> children) {
		long total = 0;
		for (GeoLocusV2 child : children) {
			LocusInstrumentationSnapshot2D snapshot = child.getInstrumentation().snapshot();
			total += snapshot.getEvaluatorCalls() + snapshot.getNestedEvaluatorCalls();
		}
		return total;
	}

	/** Copy of a native document whose free point D_1 is saved with a new abscissa. */
	private static File withD1X(File source, String value) throws IOException {
		Path target = source.toPath().resolveSibling("D_1-x-" + value + ".cedg");
		boolean edited = false;
		try (ZipInputStream in = new ZipInputStream(Files.newInputStream(source.toPath()));
				ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(target))) {
			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				byte[] bytes = readAll(in);
				if ("geogebra.xml".equals(entry.getName())) {
					Matcher matcher = D1_COORDS.matcher(new String(bytes,
							StandardCharsets.UTF_8));
					if (!matcher.find()) {
						throw new IllegalStateException("D_1 coordinates not found");
					}
					bytes = matcher.replaceFirst("$1" + value + "$3")
							.getBytes(StandardCharsets.UTF_8);
					edited = true;
				}
				out.putNextEntry(new ZipEntry(entry.getName()));
				out.write(bytes);
				out.closeEntry();
			}
		}
		if (!edited) {
			throw new IllegalStateException("geogebra.xml not found");
		}
		return target.toFile();
	}

	private static byte[] readAll(InputStream in) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int read;
		while ((read = in.read(buffer)) >= 0) {
			bytes.write(buffer, 0, read);
		}
		return bytes.toByteArray();
	}
}
