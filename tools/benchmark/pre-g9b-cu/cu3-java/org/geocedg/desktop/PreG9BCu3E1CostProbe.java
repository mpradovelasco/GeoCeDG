/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.geocedg.common.kernel.algos.AlgoLocusDriverCorrespondencePoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.locus.constructive.ExactEvaluationBudget;
import org.geocedg.common.kernel.locus.constructive.ExactEvaluationCounters;
import org.geocedg.common.kernel.locus.constructive.StructuralPointResolution;
import org.geocedg.common.kernel.locus.constructive.StructuralPointResolver;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU3 gate G-AUT-8, increment E1, run only through
 * {@code tools/benchmark/pre-g9b-cu/run-cu3-e1-cost.ps1}: the deterministic counters of the
 * exact evaluation against {@code cu3-exact-evaluation-budget/v1} and of
 * {@code SUBSTITUTE_GENERIC} for the structural points and the five mandatory contacts of the
 * author model under the constructive authority, for the synthetic controls of the matrix and
 * for a construction with independent square roots; the injection of a budget; the
 * repetition of every evaluation; and a drag of the driver without a new revision. The kernel
 * enforces the budgets, this bench only measures them; timings are informative and never part
 * of a decision. It reads copies of the author model and synthetic constructions only, assigns
 * no verdict and is not part of any source set.
 */
@EnabledIfSystemProperty(named = "geocedg.cu0.harness", matches = "cu3-e1-cost")
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3E1CostProbe {

	private static final int TIMING_RUNS = 7;
	private static final String[][] MODEL = {{"SPe=Point(e_1,K)", "e_1", "K", null},
			{"SPo=Point(o_1,K)", "o_1", "K", null}, {"SPj=Point(e_1,J)", "e_1", "J", null},
			{"SX1=Intersect(e_1,n,K)", "e_1", "K", "n"},
			{"SX2=Intersect(e_1,c_1,K)", "e_1", "K", "c_1"},
			{"SX3=Intersect(o_1,h_2,K)", "o_1", "K", "h_2"},
			{"SX4=Intersect(e_1,c_1,J)", "e_1", "J", "c_1"},
			{"SX5=Intersect(e_1,r,J)", "e_1", "J", "r"}};

	@TempDir
	Path work;

	@Test
	void run() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.cu0.output"));
		Files.createDirectories(output);
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("schema_version", 1);
		root.put("id", "pre-g9b-cu3-e1-cost");
		root.put("status", "CU3 E1 GATE G-AUT-8 BENCH EVIDENCE - NO VERDICT OF ITS OWN");
		Map<String, Object> generator = new LinkedHashMap<>();
		generator.put("runner", "tools/benchmark/pre-g9b-cu/run-cu3-e1-cost.ps1");
		generator.put("source_commit", System.getProperty("geocedg.cu0.commit"));
		generator.put("source_tree", System.getProperty("geocedg.cu0.tree"));
		generator.put("product_sources_modified_relative_to_commit",
				Boolean.getBoolean("geocedg.cu0.productDirty"));
		root.put("generator", generator);
		Map<String, Object> budget = new LinkedHashMap<>();
		budget.put("version", ExactEvaluationBudget.VERSION);
		budget.put("operations", ExactEvaluationBudget.MAXIMUM_OPERATIONS);
		budget.put("depth", ExactEvaluationBudget.MAXIMUM_DEPTH);
		budget.put("bits", ExactEvaluationBudget.MAXIMUM_BITS);
		root.put("budget", budget);
		root.put("timing_runs", TIMING_RUNS);
		root.put("model", onEdt(() -> model(PreG9BCu0Corpus.open(
				PreG9BCu0Corpus.withoutE2(work)))));
		root.put("synthetic", onEdt(PreG9BCu3E1CostProbe::synthetic));
		Files.writeString(output.resolve("cu3-e1-cost.json"), PreG9BCu0References.json(root),
				StandardCharsets.UTF_8);
	}

	// ---------------------------------------------------------------- author model

	private static Map<String, Object> model(AppGeoCeDG app) {
		Construction construction = app.getKernel().getConstruction();
		AuthorityPolicyAdoption.adopt(construction);
		Map<String, Object> rows = new LinkedHashMap<>();
		for (String[] row : MODEL) {
			GeoElement point = PreG9BR6PlusF3Fixtures.must(app, row[0]);
			rows.put(row[0], measure(resolution(point), () -> StructuralPointResolver.resolve(
					(GeoLocusV2) construction.lookupLabel(row[1]),
					(GeoPoint) construction.lookupLabel(row[2]),
					row[3] == null ? null : construction.lookupLabel(row[3]),
					ExactEvaluationBudget.V1)));
		}
		rows.put("budget injection on SX1 (8 operations)", describe(
				StructuralPointResolver.resolve((GeoLocusV2) construction.lookupLabel("e_1"),
						(GeoPoint) construction.lookupLabel("K"), construction.lookupLabel("n"),
						ExactEvaluationBudget.restrictedTo(8, ExactEvaluationBudget.MAXIMUM_DEPTH,
								ExactEvaluationBudget.MAXIMUM_BITS))));
		GeoElement contact = construction.lookupLabel("SX1");
		StructuralPointResolution before = resolution(contact);
		double t = PreG9BCu0Corpus.pathParameter(app, "E_1");
		PreG9BCu0Corpus.movePathPoint(app, "E_1", t + 0.3);
		Map<String, Object> drag = new LinkedHashMap<>();
		drag.put("same resolution after moving E_1", before == resolution(contact));
		drag.put("same exact counters", before.getExactCounters()
				== resolution(contact).getExactCounters());
		PreG9BCu0Corpus.movePathPoint(app, "E_1", t);
		rows.put("drag of the driver without a new revision", drag);
		return rows;
	}

	// ---------------------------------------------------------------- synthetic

	private static Map<String, Object> synthetic() {
		Map<String, Object> rows = new LinkedHashMap<>();
		String[] circle = {"O=(0,0)", "c=Circle(O,3)", "P=Point(c)", "Q=Midpoint(O,P)",
				"L=LocusV2(Q,P)", "D=Intersect(c,yAxis,2)"};
		rows.put("V-M3-5 radius 1.5 against y = 1.5", synthetic(circle,
				"g=Line((0,1.5),(1,1.5))", "X=Intersect(L,g,D)"));
		String above = Double.toString(Math.nextUp(1.5));
		rows.put("V-M3-4 one ulp away", synthetic(circle,
				"g=Line((0," + above + "),(1," + above + "))", "X=Intersect(L,g,D)"));
		rows.put("V-M3-6 0.3 against 0.1 + 0.2", synthetic(new String[] {"O=(0,0)",
				"c=Circle(O,0.6)", "P=Point(c)", "Q=Midpoint(O,P)", "L=LocusV2(Q,P)",
				"D=Intersect(c,yAxis,2)", "F=(0,0.2)", "G=(0,0.4)", "M=Midpoint(F,G)"},
				"g=OrthogonalLine(M,yAxis)", "X=Intersect(L,g,D)"));
		rows.put("V-CFG-1 joint recomputation", synthetic(new String[] {"O=(0,0)", "U=(1,1)",
				"W=(1,-1)", "c=Circle(O,U)", "d=Circle(O,3)", "P=Point(d)", "q=Ray(O,P)",
				"Q=Intersect(c,q,1)", "L=LocusV2(Q,P)", "D=Intersect(d,xAxis,1)"},
				"r=Circle(O,W)", "X=Intersect(L,r,D)"));
		String[] roots = {"O=(0,0)", "c=Circle(O,2)", "P=Point(c)", "k1=Circle(O,1)",
				"k2=Circle(O,1.5)", "v=PerpendicularLine(P,xAxis)", "Q1=Intersect(k1,v,1)",
				"Q2=Intersect(k2,v,1)", "M=Midpoint(Q1,Q2)", "L=LocusV2(M,P)",
				"D=Intersect(c,Line((0.5,0),(0.5,1)),1)"};
		// Point(L, D) evaluates only D* exactly; the intersection evaluates the traced point.
		String vertical = "g=Line((0.5,0),(0.5,1))";
		rows.put("independent square roots", synthetic(roots, vertical, "X=Intersect(L,g,D)"));
		rows.put("independent square roots, depth budget 1", syntheticWithBudget(roots,
				vertical, ExactEvaluationBudget.restrictedTo(
						ExactEvaluationBudget.MAXIMUM_OPERATIONS, 1,
						ExactEvaluationBudget.MAXIMUM_BITS)));
		return rows;
	}

	private static Map<String, Object> synthetic(String[] commands, String target,
			String point) {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
		for (String command : commands) {
			PreG9BR6PlusF3Fixtures.must(app, command);
		}
		if (target != null) {
			PreG9BR6PlusF3Fixtures.must(app, target);
		}
		Construction construction = app.getKernel().getConstruction();
		AuthorityPolicyAdoption.adopt(construction);
		GeoElement created = PreG9BR6PlusF3Fixtures.must(app, point);
		return measure(resolution(created), () -> StructuralPointResolver.resolve(
				(GeoLocusV2) construction.lookupLabel("L"),
				(GeoPoint) construction.lookupLabel("D"),
				target == null ? null : construction.lookupLabel(target.substring(0, 1)),
				ExactEvaluationBudget.V1));
	}

	private static Map<String, Object> syntheticWithBudget(String[] commands, String target,
			ExactEvaluationBudget budget) {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
		for (String command : commands) {
			PreG9BR6PlusF3Fixtures.must(app, command);
		}
		PreG9BR6PlusF3Fixtures.must(app, target);
		Construction construction = app.getKernel().getConstruction();
		AuthorityPolicyAdoption.adopt(construction);
		Map<String, Object> row = describe(StructuralPointResolver.resolve(
				(GeoLocusV2) construction.lookupLabel("L"),
				(GeoPoint) construction.lookupLabel("D"),
				construction.lookupLabel(target.substring(0, 1)), budget));
		row.put("budget", budget.toString());
		return row;
	}

	// ---------------------------------------------------------------- measurement

	/**
	 * The resolution of the product algorithm, then fresh resolutions of the same revision:
	 * identical results and counters, and the median wall time of {@value #TIMING_RUNS} runs.
	 */
	private static Map<String, Object> measure(StructuralPointResolution resolution,
			Callable<StructuralPointResolution> fresh) {
		Map<String, Object> row = describe(resolution);
		try {
			List<Long> times = new ArrayList<>();
			boolean deterministic = true;
			for (int run = 0; run < TIMING_RUNS; run++) {
				long start = System.nanoTime();
				StructuralPointResolution again = fresh.call();
				times.add(System.nanoTime() - start);
				deterministic &= describe(again).equals(row);
			}
			Collections.sort(times);
			row.put("deterministic_over_runs", deterministic);
			row.put("median_wall_time_ms", times.get(times.size() / 2) / 1E6);
		} catch (Exception failure) {
			row.put("repetition_failure", failure.toString());
		}
		return row;
	}

	private static Map<String, Object> describe(StructuralPointResolution resolution) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("defined", resolution.isDefined());
		row.put("cause", String.valueOf(resolution.getCause()));
		row.put("integrity", resolution.getIntegrity().name());
		row.put("membership", resolution.getMembership() == null ? null
				: resolution.getMembership().getIntegrity().name());
		row.put("incidence", resolution.getIncidence() == null ? null
				: resolution.getIncidence().getIntegrity().name());
		row.put("transfer", resolution.getTransfer() == null ? null
				: resolution.getTransfer().toString());
		row.put("exact", counters(resolution.getExactCounters()));
		List<ExactEvaluationCounters> relaxed = resolution.getRelaxedCounters();
		Map<String, Object> generic = new LinkedHashMap<>();
		generic.put("evaluations", relaxed.size());
		long total = 0;
		long largest = 0;
		int depth = 0;
		int bits = 0;
		for (ExactEvaluationCounters counters : relaxed) {
			total += counters.getOperations();
			largest = Math.max(largest, counters.getOperations());
			depth = Math.max(depth, counters.getDepth());
			bits = Math.max(bits, counters.getMaximumBits());
		}
		generic.put("operations_total", total);
		generic.put("operations_largest", largest);
		generic.put("depth_largest", depth);
		generic.put("bits_largest", bits);
		row.put("substitute_generic", generic);
		return row;
	}

	private static Map<String, Object> counters(ExactEvaluationCounters counters) {
		if (counters == null) {
			return null;
		}
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("operations", counters.getOperations());
		row.put("additions", counters.getAdditions());
		row.put("multiplications", counters.getMultiplications());
		row.put("inversions", counters.getInversions());
		row.put("sign_decisions", counters.getSignDecisions());
		row.put("square_roots", counters.getSquareRoots());
		row.put("rational_square_roots", counters.getRationalSquareRoots());
		row.put("depth", counters.getDepth());
		row.put("maximum_bits", counters.getMaximumBits());
		return row;
	}

	private static StructuralPointResolution resolution(GeoElement point) {
		return ((AlgoLocusDriverCorrespondencePoint2D) point.getParentAlgorithm())
				.getResolution();
	}

	private static <T> T onEdt(Callable<T> action) throws Exception {
		return PreG9BR6PlusF3Fixtures.onEdt(action);
	}
}
