/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxOutcome2D;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU2 step T3 on the author model: every locus slice has a class-v3 program within
 * the versioned step limit, with the selection rule read from its algorithms
 * ({@code locus-v2-certified-construction-model.md} section 10). Matrix rows V-V3-4 (model
 * part), V-SEL-1 to V-SEL-5 and the model variants of V-COINC-1.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu2AuthorModelClassV3Test {

	private static final List<String> FIRST_FAMILY = List.of("o", "v", "w", "a_1");
	/** Critical value of excCyl, one ulp on each side (analysis section 3.8). */
	private static final double PENETRATION = Double.longBitsToDouble(0x400d416bb67a38bbL);
	private static final double BITE = Double.longBitsToDouble(0x400d416bb67a38bcL);
	private static final double T_CRITICAL = 1.2767651295307840;
	/** Analytic ends of the gap at excCyl 3.7 (analysis section 3.3). */
	private static final double GAP_LOWER = 1.0418225300;
	private static final double GAP_UPPER = 1.5117077290;

	@TempDir
	Path work;

	/**
	 * V-V3-4, model part, and V-SEL-1: every slice of the complete model, e_2 included with
	 * both macros expanded and its structural metric input, fits the class-v3 step limit; the
	 * rule of each intersection is the algorithm's own.
	 */
	@Test
	void everyModelSliceHasAClassV3ProgramWithItsAlgorithmRules() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			Map<String, Object> values = new LinkedHashMap<>();
			for (String label : List.of("o", "v", "w", "a_1", "e_1", "o_1", "e_2")) {
				values.put(label, describe(program(app, label)));
			}
			return values;
		});
		for (Object value : row.values()) {
			Map<?, ?> program = (Map<?, ?>) value;
			assertEquals(CertifiedConstructionProgram2D.VERSION_V3, program.get("version"),
					row.toString());
			assertTrue((Integer) program.get("steps")
					<= CertifiedConstructionProgram2D.MAXIMUM_STEPS_V3, row.toString());
		}
		for (String label : FIRST_FAMILY) {
			Map<?, ?> program = (Map<?, ?>) row.get(label);
			// {Q, R}: settled permutation [0, 1], both outputs labelled.
			assertEquals(List.of("0,0,1,1,1"), program.get("selections"), row.toString());
			int output = label.equals("v") || label.equals("w") ? 0 : 1;
			assertEquals(List.of(output), program.get("outputs"), row.toString());
		}
		for (String label : List.of("e_1", "o_1", "e_2")) {
			Map<?, ?> program = (Map<?, ?>) row.get(label);
			// H_1 on its hidden source: settled [0, 1]; G_1: compaction of a first
			// initialization, the ray s_1 leaving the centre of c_1.
			assertEquals(List.of("0,0,1,0,0", "1,0,1,0,0"), program.get("selections"),
					row.toString());
			assertEquals(List.of(0, 0), program.get("outputs"), row.toString());
		}
		Map<?, ?> e2 = (Map<?, ?>) row.get("e_2");
		Map<?, ?> operations = (Map<?, ?>) e2.get("operations");
		assertEquals(1, operations.get("METRIC_INPUT"), row.toString());
		assertEquals(1, operations.get("DOT"), row.toString());
		assertEquals(1, operations.get("VECTOR_POINTS"), row.toString());
		assertEquals(2, operations.get("POINT_PLUS_VECTOR"), row.toString());
		assertEquals(2, operations.get("SCALE"), row.toString());
		assertEquals(1, operations.get("UNIT_VECTOR_LINE"), row.toString());
	}

	/**
	 * V-SEL-1: the program of a revision does not depend on the driver position, and moving
	 * only the driver keeps the revision.
	 */
	@Test
	void theProgramDoesNotDependOnTheDriverPosition() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			Map<String, Object> values = new LinkedHashMap<>();
			Map<String, String> signatures = signatures(app);
			Map<String, Long> revisions = revisions(app);
			List<String> changes = new ArrayList<>();
			for (double parameter : new double[] {-1, 2, T_CRITICAL, -3}) {
				PreG9BCu0Corpus.movePathPoint(app, "N", parameter);
				PreG9BCu0Corpus.movePathPoint(app, "E_1", parameter / 2);
				if (!signatures.equals(signatures(app))) {
					changes.add("signature at " + parameter);
				}
				if (!revisions.equals(revisions(app))) {
					changes.add("revision at " + parameter);
				}
			}
			values.put("changes", changes);
			values.put("signatures", signatures.size());
			return values;
		});
		assertEquals(List.of(), row.get("changes"), row.toString());
		assertEquals(6, row.get("signatures"), row.toString());
	}

	/**
	 * V-SEL-2, step T3: with excCyl 4.0 the program needs no valid sample; with the driver
	 * moved into the gap, where no output is defined, the program of the revision is present
	 * and identical, and a document saved there and reopened keeps it after the next valid
	 * position. A document reopened with the driver in the gap builds no slice at the base
	 * (CU0 pin {@code H01}); the publication rule of step T11 completes the row.
	 */
	@Test
	void theRuleIsCapturedWithoutAnyValidSample() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			PreG9BCu0Corpus.setNumber(app, "excCyl", 4.0);
			Map<String, Object> values = new LinkedHashMap<>();
			Map<String, String> valid = new LinkedHashMap<>();
			for (String label : FIRST_FAMILY) {
				valid.put(label, program(app, label).getSignature());
			}
			PreG9BCu0Corpus.movePathPoint(app, "N", 0.645649883219);
			values.put("defined_in_gap", app.getKernel().lookupLabel("Q").isDefined());
			Map<String, String> inGap = new LinkedHashMap<>();
			for (String label : FIRST_FAMILY) {
				inGap.put(label, program(app, label).getSignature());
			}
			values.put("same_in_gap", valid.equals(inGap));
			File gap = PreG9BR6PlusF3Fixtures.save(app, work, "gap");
			AppGeoCeDG reopened = PreG9BCu0Corpus.open(gap);
			values.put("evaluator_in_gap", evaluator(reopened, "o") != null);
			// The first valid position ends the first initialization; the next computation
			// settles the permutation, a real transition that publishes one revision.
			PreG9BCu0Corpus.movePathPoint(reopened, "N", -1);
			long first = PreG9BCu0Corpus.locus(reopened, "o").getSemanticRevision();
			PreG9BCu0Corpus.movePathPoint(reopened, "N", -1.01);
			values.put("settling_revisions",
					PreG9BCu0Corpus.locus(reopened, "o").getSemanticRevision() - first);
			PreG9BCu0Corpus.movePathPoint(reopened, "N", -1.02);
			values.put("later_revisions",
					PreG9BCu0Corpus.locus(reopened, "o").getSemanticRevision() - first);
			Map<String, String> after = new LinkedHashMap<>();
			for (String label : FIRST_FAMILY) {
				after.put(label, String.join(";", selections(program(reopened, label))));
			}
			values.put("rules_after_reopen", after);
			return values;
		});
		assertEquals(false, row.get("defined_in_gap"), row.toString());
		assertEquals(true, row.get("same_in_gap"), row.toString());
		assertEquals(false, row.get("evaluator_in_gap"), row.toString());
		assertEquals(1L, row.get("settling_revisions"), row.toString());
		assertEquals(1L, row.get("later_revisions"), row.toString());
		for (Object rule : ((Map<?, ?>) row.get("rules_after_reopen")).values()) {
			assertEquals("0,0,1,1,1", rule, row.toString());
		}
	}

	/**
	 * V-SEL-4: penetration and bite publish new revisions and keep the selection rule of
	 * every intersection.
	 */
	@Test
	void penetrationAndBiteKeepTheSelectionRule() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			Map<String, Object> values = new LinkedHashMap<>();
			List<Object> rules = new ArrayList<>();
			List<Long> revisions = new ArrayList<>();
			for (double value : new double[] {2.9, 3.7, 2.9, PENETRATION, BITE}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", value);
				Map<String, Object> rule = new TreeMap<>();
				for (String label : FIRST_FAMILY) {
					rule.put(label, selections(program(app, label)));
				}
				rules.add(rule);
				revisions.add(PreG9BCu0Corpus.locus(app, "o").getSemanticRevision());
			}
			values.put("rules", rules);
			values.put("revisions", revisions);
			return values;
		});
		List<?> rules = (List<?>) row.get("rules");
		for (Object rule : rules) {
			assertEquals(rules.get(0), rule, row.toString());
		}
		List<?> revisions = (List<?>) row.get("revisions");
		for (int index = 1; index < revisions.size(); index++) {
			assertNotEquals(revisions.get(index - 1), revisions.get(index), row.toString());
		}
	}

	/**
	 * V-SEL-5: at excCyl 3.7 the gap is uniformly invalid by the negative discriminant, with
	 * the same raw root selected on both sides of it.
	 */
	@Test
	void theGapIsUniformlyInvalidWithTheSameRuleOnBothSides() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			PreG9BCu0Corpus.setNumber(app, "excCyl", 3.7);
			Map<String, Object> values = new LinkedHashMap<>();
			for (String label : FIRST_FAMILY) {
				ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(
						PreG9BCu0Corpus.locus(app, label).getSemanticDefinition());
				List<String> inside = new ArrayList<>();
				for (int index = 1; index < 10; index++) {
					double t = GAP_LOWER + (GAP_UPPER - GAP_LOWER) * index / 10;
					ConstructionBoxOutcome2D outcome = model.evaluate(t, t);
					inside.add(outcome.getKind() + ":" + outcome.getInvalidReason());
				}
				values.put(label + ".inside", inside);
				ConstructionBoxOutcome2D before = model.evaluate(GAP_LOWER - 0.05,
						GAP_LOWER - 0.05);
				ConstructionBoxOutcome2D after = model.evaluate(GAP_UPPER + 0.05,
						GAP_UPPER + 0.05);
				values.put(label + ".sides", before.getKind() + "/" + after.getKind());
				values.put(label + ".selection", before.getSelection() + "/"
						+ after.getSelection());
			}
			return values;
		});
		for (String label : FIRST_FAMILY) {
			for (Object outcome : (List<?>) row.get(label + ".inside")) {
				assertEquals("UNIFORMLY_INVALID:DISCRIMINANT_NEGATIVE", outcome, row.toString());
			}
			assertEquals("VALID/VALID", row.get(label + ".sides"), row.toString());
			String[] selection = row.get(label + ".selection").toString().split("/");
			assertEquals(selection[0], selection[1], row.toString());
		}
	}

	/**
	 * V-SEL-3 and V-COINC-1, step T3: at the analytic ends of the gap at excCyl 3.7 and at
	 * the double root one ulp below the critical value, both labelled outputs are defined and
	 * coincide; crossing the end keeps the permutation, and the capture reads both labels.
	 */
	@Test
	void atTheTangencyBothLabelledOutputsCoincide() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			Map<String, Object> values = new LinkedHashMap<>();
			GeoPoint q = (GeoPoint) app.getKernel().lookupLabel("Q");
			GeoPoint r = (GeoPoint) app.getKernel().lookupLabel("R");
			AlgoIntersectLineConic algorithm = (AlgoIntersectLineConic) q.getParentAlgorithm();
			List<String> tangencies = new ArrayList<>();
			PreG9BCu0Corpus.setNumber(app, "excCyl", 3.7);
			for (double parameter : new double[] {GAP_LOWER, GAP_UPPER}) {
				PreG9BCu0Corpus.movePathPoint(app, "N", parameter - 0.01);
				PreG9BCu0Corpus.movePathPoint(app, "N", parameter);
				tangencies.add(coincidence(q, r));
				PreG9BCu0Corpus.movePathPoint(app, "N", parameter + 0.01);
			}
			PreG9BCu0Corpus.setNumber(app, "excCyl", PENETRATION);
			PreG9BCu0Corpus.movePathPoint(app, "N", T_CRITICAL);
			tangencies.add(coincidence(q, r));
			values.put("tangencies", tangencies);
			values.put("permutation", java.util.Arrays.toString(
					algorithm.getSelectionPermutation()));
			values.put("labels", selections(program(app, "o")));
			return values;
		});
		for (Object tangency : (List<?>) row.get("tangencies")) {
			assertEquals("coincident", tangency, row.toString());
		}
		assertEquals("[0, 1]", row.get("permutation"), row.toString());
		assertEquals(List.of("0,0,1,1,1"), row.get("labels"), row.toString());
	}

	private static String coincidence(GeoPoint q, GeoPoint r) {
		if (!q.isDefined() || !r.isDefined()) {
			return "undefined";
		}
		double distance = Math.hypot(q.getInhomX() - r.getInhomX(),
				q.getInhomY() - r.getInhomY());
		return distance < 1e-3 ? "coincident" : "apart:" + distance;
	}

	private static Map<String, Object> describe(CertifiedConstructionProgram2D program) {
		Map<String, Object> description = new LinkedHashMap<>();
		description.put("version", program.getVersion());
		description.put("steps", program.getSteps().size());
		Map<String, Integer> operations = new TreeMap<>();
		List<Integer> outputs = new ArrayList<>();
		for (Step step : program.getSteps()) {
			operations.merge(step.getOperation().name(), 1, Integer::sum);
			if (step.getOperation() == Operation.INTERSECTION_OUTPUT) {
				outputs.add((int) step.getParameter(0));
			}
		}
		description.put("operations", operations);
		description.put("selections", selections(program));
		description.put("outputs", outputs);
		return description;
	}

	private static List<String> selections(CertifiedConstructionProgram2D program) {
		List<String> selections = new ArrayList<>();
		for (Step step : program.getSteps()) {
			if (step.getOperation() == Operation.LINE_CIRCLE) {
				StringBuilder selection = new StringBuilder();
				for (int index = 0; index < 5; index++) {
					selection.append(index == 0 ? "" : ",")
							.append((int) step.getParameter(index));
				}
				selections.add(selection.toString());
			}
		}
		return selections;
	}

	private static Map<String, String> signatures(AppGeoCeDG app) throws Exception {
		Map<String, String> signatures = new LinkedHashMap<>();
		for (String label : List.of("o", "v", "w", "a_1", "e_1", "o_1")) {
			signatures.put(label, program(app, label).getSignature());
		}
		return signatures;
	}

	private static Map<String, Long> revisions(AppGeoCeDG app) {
		Map<String, Long> revisions = new LinkedHashMap<>();
		for (String label : List.of("o", "v", "w", "a_1", "e_1", "o_1")) {
			revisions.put(label, PreG9BCu0Corpus.locus(app, label).getSemanticRevision());
		}
		return revisions;
	}

	private static CertifiedConstructionProgram2D program(AppGeoCeDG app, String label)
			throws Exception {
		ReconstructibleLocusEvaluator2D evaluator = evaluator(app, label);
		if (evaluator == null) {
			throw new IllegalStateException("No slice evaluator for " + label);
		}
		return evaluator.captureCertifiedProgramV3().orElseThrow(() ->
				new IllegalStateException("No class-v3 program for " + label + ": "
						+ evaluator.getFidelityReport()));
	}

	private static ReconstructibleLocusEvaluator2D evaluator(AppGeoCeDG app, String label)
			throws Exception {
		GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
		Field field = AlgoDependentPointLocusV2.class.getDeclaredField("evaluator");
		field.setAccessible(true);
		return (ReconstructibleLocusEvaluator2D) field.get(locus.getParentAlgorithm());
	}
}
