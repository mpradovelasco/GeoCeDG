/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.geocedg.common.euclidian.draw.DrawLocusV2;
import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentationSnapshot2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU0 reproducible harness, run only through
 * {@code tools/benchmark/pre-g9b-cu/run-harness.ps1}. It is not part of the Desktop test
 * source set: the runner's init script adds it for one explicit run and removes it again.
 *
 * <ul>
 * <li>{@code references}: writes the CU0 geometric reference baseline (deterministic).</li>
 * <li>{@code composition}: the nested Locus V2 / metric composition bench of {@code e_2},
 * with deterministic work counters and informational timings, with and without {@code e_2}.
 * </li>
 * <li>{@code certification} (PRE-G9B-CU2, step T1): certification and revision work of the
 * model loci for gates GC-1 to GC-3 and GC-6 under driver-only moves, non-driver changes and
 * consumer queries.</li>
 * <li>{@code tessellation} (PRE-G9B-CU2, step T1): drawing vertices of the model loci per
 * view and zoom for gate GC-5.</li>
 * </ul>
 *
 * <p>The {@code windowed} scenario runs in {@link PreG9BCu0WindowedHarness}, outside the
 * embedded-host lifecycle, because it starts the real product.
 */
@EnabledIfSystemProperty(named = "geocedg.cu0.harness",
		matches = "references|composition|certification|tessellation")
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu0Harness {

	@TempDir
	Path work;

	@Test
	void run() throws Exception {
		String scenario = System.getProperty("geocedg.cu0.harness");
		Path output = Path.of(System.getProperty("geocedg.cu0.output"));
		Files.createDirectories(output);
		switch (scenario) {
		case "references" -> references(output);
		case "composition" -> composition(output,
				Boolean.getBoolean("geocedg.cu0.dense"));
		case "certification" -> certification(output);
		case "tessellation" -> tessellation(output);
		default -> throw new IllegalArgumentException("Unknown scenario " + scenario);
		}
	}

	// ---------------------------------------------------------------- references

	private void references(Path output) throws Exception {
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("schema_version", 1);
		root.put("id", "pre-g9b-cu0-geometric-reference-baseline");
		root.put("status", "CU0 CHARACTERIZATION BASELINE - CANDIDATE");
		root.put("rule", "preserve: geometric facts of the unchanged product that corrections"
				+ " must reproduce within the tolerances unless an approved contract changes"
				+ " them explicitly; pins: signatures of characterized defects with their owning"
				+ " PRE-G9B-CU package, reference evidence and not normative expectations");
		Map<String, Object> generator = new LinkedHashMap<>();
		generator.put("harness", "tools/benchmark/pre-g9b-cu/run-harness.ps1 -Scenario"
				+ " references");
		generator.put("support", List.of(
				"source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu0Corpus.java",
				"source/desktop/desktop/src/test/java/org/geocedg/desktop/"
						+ "PreG9BCu0References.java"));
		generator.put("source_commit", System.getProperty("geocedg.cu0.commit"));
		generator.put("source_tree", System.getProperty("geocedg.cu0.tree"));
		generator.put("product_sources_modified_relative_to_commit",
				Boolean.getBoolean("geocedg.cu0.productDirty"));
		root.put("generator", generator);
		Map<String, Object> corpus = new LinkedHashMap<>();
		corpus.put(PreG9BCu0Corpus.CEDG, PreG9BCu0Corpus.CEDG_SHA256);
		corpus.put(PreG9BCu0Corpus.GGB, PreG9BCu0Corpus.GGB_SHA256);
		root.put("corpus", corpus);
		Map<String, Object> tolerances = new LinkedHashMap<>();
		tolerances.put("point_model_units", PreG9BCu0References.POINT_TOLERANCE);
		tolerances.put("length_model_units", PreG9BCu0References.LENGTH_TOLERANCE);
		tolerances.put("branch_end_parameter", PreG9BCu0References.BOUNDARY_TOLERANCE);
		root.put("tolerances", tolerances);

		Map<String, Map<String, Object>> variant = onEdt(() -> PreG9BCu0References
				.variantSections(PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work))));
		Map<String, Object> check = new LinkedHashMap<>();
		Map<String, Map<String, Object>> complete = onEdt(() -> {
			AppGeoCeDG full = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			Map<String, Map<String, Object>> sections = PreG9BCu0References.e2Sections(full);
			PreG9BCu0Corpus.locus(full, "e_2").remove();
			check.put("state", PreG9BCu0References.state(full));
			check.put("loci", PreG9BCu0References.locusSamples(full));
			return sections;
		});
		Map<String, Object> synthetic = onEdt(PreG9BCu0References::syntheticPins);
		Map<String, Object> equivalence = new LinkedHashMap<>();
		equivalence.put("method", "state and locus samples of the derived variant versus the"
				+ " complete model after removing e_2 through the kernel");
		equivalence.put("max_abs_difference", Math.max(
				maxDifference(variant.get("preserve").get("state"), check.get("state")),
				maxDifference(variant.get("preserve").get("loci"), check.get("loci"))));
		generator.put("variant_equivalence", equivalence);

		Map<String, Object> preserve = new LinkedHashMap<>(variant.get("preserve"));
		preserve.putAll(complete.get("preserve"));
		Map<String, Object> pins = new LinkedHashMap<>(variant.get("pins"));
		pins.putAll(complete.get("pins"));
		pins.putAll(synthetic);
		root.put("preserve", preserve);
		root.put("pins", pins);
		Files.writeString(output.resolve("cu0-geometric-reference-baseline.json"),
				PreG9BCu0References.json(root), StandardCharsets.UTF_8);
	}

	/** Largest absolute numeric difference between two structurally equal JSON trees. */
	private static double maxDifference(Object a, Object b) {
		if (a instanceof Map<?, ?> left && b instanceof Map<?, ?> right) {
			if (!left.keySet().equals(right.keySet())) {
				return Double.POSITIVE_INFINITY;
			}
			double max = 0;
			for (Object key : left.keySet()) {
				max = Math.max(max, maxDifference(left.get(key), right.get(key)));
			}
			return max;
		}
		if (a instanceof List<?> left && b instanceof List<?> right) {
			if (left.size() != right.size()) {
				return Double.POSITIVE_INFINITY;
			}
			double max = 0;
			for (int i = 0; i < left.size(); i++) {
				max = Math.max(max, maxDifference(left.get(i), right.get(i)));
			}
			return max;
		}
		if (a instanceof Number left && b instanceof Number right) {
			return Math.abs(left.doubleValue() - right.doubleValue());
		}
		return java.util.Objects.equals(a, b) ? 0 : Double.POSITIVE_INFINITY;
	}

	// ---------------------------------------------------------------- composition bench

	private void composition(Path output, boolean dense) throws Exception {
		Map<String, Object> bench = new LinkedHashMap<>();
		bench.put("scenario", "composition");
		bench.put("source_commit", System.getProperty("geocedg.cu0.commit"));
		bench.put("java_runtime", System.getProperty("java.runtime.version"));
		bench.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));
		bench.put("processors", Runtime.getRuntime().availableProcessors());
		bench.put("timing_status", "informational; work counters are the reproducible measure");
		File file = PreG9BCu0Corpus.workingCopy(PreG9BCu0Corpus.CEDG, work);
		onEdt(() -> {
			long start = System.nanoTime();
			AppGeoCeDG app = PreG9BCu0Corpus.open(file);
			bench.put("load_with_e_2_ms", millis(start));
			EuclidianView view = authorView(app);
			GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_2");
			bench.put("nested_copy_after_load", nested(e2));
			List<Object> evaluations = new ArrayList<>();
			for (double t : new double[] {-3, -1, -1e-3, 1e-3, 1, 3}) {
				Map<String, Object> before = nested(e2);
				long begin = System.nanoTime();
				LocusPoint2D point;
				try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
					point = PreG9BCu0Corpus.point(e2, t, session);
				}
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("t", t);
				row.put("ms", millis(begin));
				row.put("valid", point != null);
				row.put("nested_delta", delta(before, nested(e2)));
				evaluations.add(row);
			}
			bench.put("single_point_evaluations_of_e_2", evaluations);
			double saved = PreG9BCu0Corpus.pathParameter(app, "E_1");
			List<Object> withE2 = new ArrayList<>();
			withE2.add(interaction(app, view, "drag E_1 +0.05",
					() -> PreG9BCu0Corpus.movePathPoint(app, "E_1", saved + 0.05)));
			withE2.add(interaction(app, view, "drag E_1 back",
					() -> PreG9BCu0Corpus.movePathPoint(app, "E_1", saved)));
			withE2.add(interaction(app, view, "excCyl 2.9 -> 3.0",
					() -> PreG9BCu0Corpus.setNumber(app, "excCyl", 3.0)));
			withE2.add(interaction(app, view, "excCyl 3.0 -> 2.9",
					() -> PreG9BCu0Corpus.setNumber(app, "excCyl", 2.9)));
			withE2.add(interaction(app, view, "rbCone 5 -> 5.1",
					() -> PreG9BCu0Corpus.setNumber(app, "rbCone", 5.1)));
			withE2.add(interaction(app, view, "rbCone 5.1 -> 5",
					() -> PreG9BCu0Corpus.setNumber(app, "rbCone", 5)));
			bench.put("interactions_with_e_2", withE2);
			if (dense) {
				bench.put("dense_sweep_of_e_2", denseSweep(e2, 720));
			}
			long remove = System.nanoTime();
			e2.remove();
			bench.put("remove_e_2_ms", millis(remove));
			List<Object> withoutE2 = new ArrayList<>();
			withoutE2.add(interaction(app, view, "drag E_1 +0.05",
					() -> PreG9BCu0Corpus.movePathPoint(app, "E_1", saved + 0.05)));
			withoutE2.add(interaction(app, view, "drag E_1 back",
					() -> PreG9BCu0Corpus.movePathPoint(app, "E_1", saved)));
			withoutE2.add(interaction(app, view, "excCyl 2.9 -> 3.0",
					() -> PreG9BCu0Corpus.setNumber(app, "excCyl", 3.0)));
			withoutE2.add(interaction(app, view, "excCyl 3.0 -> 2.9",
					() -> PreG9BCu0Corpus.setNumber(app, "excCyl", 2.9)));
			withoutE2.add(interaction(app, view, "rbCone 5 -> 5.1",
					() -> PreG9BCu0Corpus.setNumber(app, "rbCone", 5.1)));
			withoutE2.add(interaction(app, view, "rbCone 5.1 -> 5",
					() -> PreG9BCu0Corpus.setNumber(app, "rbCone", 5)));
			bench.put("interactions_without_e_2", withoutE2);
			long create = System.nanoTime();
			PreG9BR6PlusF3Fixtures.must(app, "e_2=LocusV2(R_1,E_1)");
			Map<String, Object> recreate = new LinkedHashMap<>();
			recreate.put("create_ms", millis(create));
			long paint = System.nanoTime();
			paint(view);
			recreate.put("paint_ms", millis(paint));
			recreate.put("nested_copy", nested(PreG9BCu0Corpus.locus(app, "e_2")));
			bench.put("recreate_e_2", recreate);
			return null;
		});
		Files.writeString(output.resolve("composition-bench.json"),
				PreG9BCu0References.json(bench), StandardCharsets.UTF_8);
	}

	private static Map<String, Object> interaction(AppGeoCeDG app, EuclidianView view,
			String action, Runnable change) {
		Map<String, Map<String, Object>> before = counters(app);
		long start = System.nanoTime();
		change.run();
		double update = millis(start);
		long paint = System.nanoTime();
		paint(view);
		double painted = millis(paint);
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("action", action);
		row.put("update_ms", update);
		row.put("paint_ms", painted);
		Map<String, Object> deltas = new LinkedHashMap<>();
		Map<String, Map<String, Object>> after = counters(app);
		for (Map.Entry<String, Map<String, Object>> entry : after.entrySet()) {
			Map<String, Object> previous = before.get(entry.getKey());
			deltas.put(entry.getKey(), previous == null ? entry.getValue()
					: delta(previous, entry.getValue()));
		}
		row.put("counter_deltas", deltas);
		return row;
	}

	private static Map<String, Map<String, Object>> counters(AppGeoCeDG app) {
		Map<String, Map<String, Object>> all = new LinkedHashMap<>();
		for (String label : List.of("o", "v", "w", "a_1", "e_1", "o_1", "e_2")) {
			if (!(app.getKernel().lookupLabel(label) instanceof GeoLocusV2 locus)) {
				continue;
			}
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("semantic_revision", locus.getSemanticRevision());
			row.put("evaluator_calls", locus.getInstrumentation().snapshot().getEvaluatorCalls());
			row.put("revision_publications", locus.getInstrumentation().snapshot()
					.getRevisionPublications());
			row.put("render_evaluations", locus.getInstrumentation().snapshot()
					.getRenderEvaluations());
			row.put("metric_index_builds", locus.getMetricInstrumentation().snapshot()
					.getComponentStateBuilds());
			row.put("metric_evaluator_calls", locus.getMetricInstrumentation().snapshot()
					.getEvaluatorCalls());
			row.put("metric_integrator_calls", locus.getMetricInstrumentation().snapshot()
					.getIntegratorCalls());
			if ("e_2".equals(label)) {
				row.putAll(prefixed("nested_o_1_", nested(locus)));
			}
			all.put(label, row);
		}
		return all;
	}

	private static Map<String, Object> nested(GeoLocusV2 outer) {
		Map<String, Object> row = new LinkedHashMap<>();
		try {
			GeoLocusV2 copy = PreG9BCu0References.nestedCopy(outer, "o_1");
			row.put("semantic_revision", copy.getSemanticRevision());
			row.put("evaluator_calls", copy.getInstrumentation().snapshot().getEvaluatorCalls());
			row.put("metric_index_builds", copy.getMetricInstrumentation().snapshot()
					.getComponentStateBuilds());
			row.put("metric_evaluator_calls", copy.getMetricInstrumentation().snapshot()
					.getEvaluatorCalls());
		} catch (ReflectiveOperationException | RuntimeException unavailable) {
			row.put("unavailable", unavailable.toString());
		}
		return row;
	}

	private static Map<String, Object> prefixed(String prefix, Map<String, Object> values) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			result.put(prefix + entry.getKey(), entry.getValue());
		}
		return result;
	}

	private static Map<String, Object> delta(Map<String, Object> before,
			Map<String, Object> after) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : after.entrySet()) {
			Object previous = before.get(entry.getKey());
			if (entry.getValue() instanceof Number now && previous instanceof Number then) {
				result.put(entry.getKey(), now.longValue() - then.longValue());
			} else {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	private static Map<String, Object> denseSweep(GeoLocusV2 e2, int points) {
		long start = System.nanoTime();
		int invalid = 0;
		double maxStep = 0;
		double atT = Double.NaN;
		LocusPoint2D previous = null;
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			for (int i = 0; i < points; i++) {
				double t = -Math.PI + 2 * Math.PI * i / points;
				LocusPoint2D point = PreG9BCu0Corpus.point(e2, t, session);
				if (point == null) {
					invalid++;
					previous = null;
					continue;
				}
				if (previous != null) {
					double step = PreG9BCu0References.distance(previous, point);
					if (step > maxStep) {
						maxStep = step;
						atT = t;
					}
				}
				previous = point;
			}
		}
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("points", points);
		row.put("ms", millis(start));
		row.put("invalid", invalid);
		row.put("max_consecutive_distance", maxStep);
		row.put("max_at_t", atT);
		return row;
	}

	// ---------------------------------------------------------------- certification bench

	/** Loci of the author model, in construction order. */
	private static final List<String> MODEL_LOCI = List.of("o", "v", "w", "a_1", "e_1", "o_1",
			"e_2");
	/** Critical driver parameter of the first family (analysis section 3.8). */
	private static final double T_CRITICAL = 1.2767651295307840;
	/** Penetration and bite sides of the critical value of excCyl, one ulp apart. */
	private static final double PENETRATION = Double.longBitsToDouble(0x400d416bb67a38bbL);
	private static final double BITE = Double.longBitsToDouble(0x400d416bb67a38bcL);

	private void certification(Path output) throws Exception {
		Map<String, Object> bench = benchHeader("certification");
		File file = PreG9BCu0Corpus.workingCopy(PreG9BCu0Corpus.CEDG, work);
		onEdt(() -> {
			long start = System.nanoTime();
			AppGeoCeDG app = PreG9BCu0Corpus.open(file);
			bench.put("load_ms", millis(start));
			EuclidianView view = authorView(app);
			paint(view);
			bench.put("after_load", certificationCounters(app));
			double n = PreG9BCu0Corpus.pathParameter(app, "N");
			double e1 = PreG9BCu0Corpus.pathParameter(app, "E_1");
			List<Object> steps = new ArrayList<>();
			steps.add(certificationStep(app, "GC-1 drag N to -1",
					() -> PreG9BCu0Corpus.movePathPoint(app, "N", -1.0)));
			steps.add(certificationStep(app, "GC-1 drag N to t_c",
					() -> PreG9BCu0Corpus.movePathPoint(app, "N", T_CRITICAL)));
			steps.add(certificationStep(app, "GC-1 drag N back",
					() -> PreG9BCu0Corpus.movePathPoint(app, "N", n)));
			steps.add(certificationStep(app, "GC-1 drag E_1 +0.05",
					() -> PreG9BCu0Corpus.movePathPoint(app, "E_1", e1 + 0.05)));
			steps.add(certificationStep(app, "GC-1 drag E_1 back",
					() -> PreG9BCu0Corpus.movePathPoint(app, "E_1", e1)));
			steps.add(certificationStep(app, "GC-2 excCyl 2.9 -> 3.0",
					() -> PreG9BCu0Corpus.setNumber(app, "excCyl", 3.0)));
			steps.add(certificationStep(app, "GC-2 excCyl 3.0 -> 2.9",
					() -> PreG9BCu0Corpus.setNumber(app, "excCyl", 2.9)));
			steps.add(certificationStep(app, "GC-2 rbCone 5 -> 5.1",
					() -> PreG9BCu0Corpus.setNumber(app, "rbCone", 5.1)));
			steps.add(certificationStep(app, "GC-2 rbCone 5.1 -> 5",
					() -> PreG9BCu0Corpus.setNumber(app, "rbCone", 5)));
			steps.add(certificationStep(app, "GC-2 excCyl to the penetration side",
					() -> PreG9BCu0Corpus.setNumber(app, "excCyl", PENETRATION)));
			steps.add(certificationStep(app, "GC-2 excCyl one ulp to the bite side",
					() -> PreG9BCu0Corpus.setNumber(app, "excCyl", BITE)));
			steps.add(certificationStep(app, "GC-2 excCyl back to 2.9",
					() -> PreG9BCu0Corpus.setNumber(app, "excCyl", 2.9)));
			steps.add(certificationStep(app, "GC-3 paint the author view",
					() -> paint(view)));
			steps.add(certificationStep(app, "GC-3 total length of o_1",
					() -> PreG9BCu0References.totalLength(app, "o_1")));
			steps.add(certificationStep(app, "GC-3 point queries of e_2", () -> {
				try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
					for (double t : new double[] {-3, -1, -1e-3, 1e-3, 1, 3}) {
						PreG9BCu0Corpus.point(PreG9BCu0Corpus.locus(app, "e_2"), t, session);
					}
				}
			}));
			bench.put("steps", steps);
			return null;
		});
		Files.writeString(output.resolve("certification-bench.json"),
				PreG9BCu0References.json(bench), StandardCharsets.UTF_8);
	}

	private static Map<String, Object> certificationStep(AppGeoCeDG app, String action,
			Runnable change) {
		Map<String, Map<String, Object>> before = certificationCounters(app);
		long start = System.nanoTime();
		change.run();
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("action", action);
		row.put("ms", millis(start));
		Map<String, Object> deltas = new LinkedHashMap<>();
		for (Map.Entry<String, Map<String, Object>> entry
				: certificationCounters(app).entrySet()) {
			Map<String, Object> previous = before.get(entry.getKey());
			deltas.put(entry.getKey(), previous == null ? entry.getValue()
					: delta(previous, entry.getValue()));
		}
		row.put("counter_deltas", deltas);
		return row;
	}

	private static Map<String, Map<String, Object>> certificationCounters(AppGeoCeDG app) {
		Map<String, Map<String, Object>> all = new LinkedHashMap<>();
		for (String label : MODEL_LOCI) {
			if (!(app.getKernel().lookupLabel(label) instanceof GeoLocusV2 locus)) {
				continue;
			}
			LocusInstrumentationSnapshot2D snapshot = locus.getInstrumentation().snapshot();
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("semantic_revision", locus.getSemanticRevision());
			row.put("revision_publications", snapshot.getRevisionPublications());
			row.put("slice_builds", snapshot.getDependencySliceBuilds());
			row.put("equivalent_recomputes", snapshot.getEquivalentRecomputes());
			row.put("nested_slice_builds", snapshot.getNestedSliceBuilds());
			for (LocusCertificationWork2D kind : LocusCertificationWork2D.values()) {
				String name = kind.name().toLowerCase(java.util.Locale.ROOT);
				row.put(name, snapshot.getCertificationWork(kind));
				row.put("nested_" + name, snapshot.getNestedCertificationWork(kind));
			}
			all.put(label, row);
		}
		return all;
	}

	// ---------------------------------------------------------------- tessellation bench

	private void tessellation(Path output) throws Exception {
		Map<String, Object> bench = benchHeader("tessellation");
		File file = PreG9BCu0Corpus.workingCopy(PreG9BCu0Corpus.CEDG, work);
		onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(file);
			EuclidianView view = authorView(app);
			List<Object> zooms = new ArrayList<>();
			for (double zoom : new double[] {1, 2, 4}) {
				double cx = PreG9BCu0Corpus.AUTHOR_WIDTH / 2.0;
				double cy = PreG9BCu0Corpus.AUTHOR_HEIGHT / 2.0;
				view.setCoordSystem(cx - (cx - PreG9BCu0Corpus.AUTHOR_X_ZERO) * zoom,
						cy - (cy - PreG9BCu0Corpus.AUTHOR_Y_ZERO) * zoom,
						PreG9BCu0Corpus.AUTHOR_SCALE * zoom,
						PreG9BCu0Corpus.AUTHOR_SCALE * zoom);
				long start = System.nanoTime();
				paint(view);
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("zoom", zoom);
				row.put("paint_ms", millis(start));
				Map<String, Object> loci = new LinkedHashMap<>();
				for (String label : MODEL_LOCI) {
					GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
					if (!(view.getDrawableFor(locus) instanceof DrawLocusV2 drawable)) {
						loci.put(label, "no drawable");
						continue;
					}
					LocusRenderCache2D cache = drawable.getRenderCache();
					Map<String, Object> counts = new LinkedHashMap<>();
					counts.put("last_build_vertices", cache.getLastBuildVertices());
					counts.put("maximum_build_vertices", cache.getMaximumBuildVertices());
					counts.put("built_vertices", cache.getBuiltVertices());
					counts.put("builds", cache.getBuilds());
					loci.put(label, counts);
				}
				row.put("loci", loci);
				zooms.add(row);
			}
			bench.put("views", zooms);
			return null;
		});
		Files.writeString(output.resolve("tessellation-bench.json"),
				PreG9BCu0References.json(bench), StandardCharsets.UTF_8);
	}

	private static Map<String, Object> benchHeader(String scenario) {
		Map<String, Object> bench = new LinkedHashMap<>();
		bench.put("scenario", scenario);
		bench.put("source_commit", System.getProperty("geocedg.cu0.commit"));
		bench.put("java_runtime", System.getProperty("java.runtime.version"));
		bench.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));
		bench.put("processors", Runtime.getRuntime().availableProcessors());
		bench.put("timing_status", "informational; work counters are the reproducible measure");
		return bench;
	}

	private static EuclidianView authorView(AppGeoCeDG app) {
		EuclidianView view = app.getEuclidianView1();
		((EuclidianViewD) view).getJPanel().setSize(PreG9BCu0Corpus.AUTHOR_WIDTH,
				PreG9BCu0Corpus.AUTHOR_HEIGHT);
		view.updateSize();
		view.setCoordSystem(PreG9BCu0Corpus.AUTHOR_X_ZERO, PreG9BCu0Corpus.AUTHOR_Y_ZERO,
				PreG9BCu0Corpus.AUTHOR_SCALE, PreG9BCu0Corpus.AUTHOR_SCALE);
		return view;
	}

	private static void paint(EuclidianView view) {
		BufferedImage image = new BufferedImage(PreG9BCu0Corpus.AUTHOR_WIDTH,
				PreG9BCu0Corpus.AUTHOR_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		view.updateAllDrawables(true);
		view.paint(new GGraphics2DD(g));
		g.dispose();
	}

	private static double millis(long start) {
		return Math.round((System.nanoTime() - start) / 1e5) / 10.0;
	}

	private static <T> T onEdt(Callable<T> action) throws Exception {
		return PreG9BR6PlusF3Fixtures.onEdt(action);
	}
}
