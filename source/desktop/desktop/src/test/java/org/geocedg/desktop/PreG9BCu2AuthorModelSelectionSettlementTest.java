/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU2 step T11, normative precision D-CU2-T11-R1, on the author model: the slice of a
 * revision rebuilt with the driver where the copy of {@code Q/R} cannot finish its first
 * initialization (inside the gap, or at the double root {@code t_c} on the bite side of the
 * critical value) is settled to the live selection state, which the accessors of both
 * algorithms show equal; the certificate is the one of the same configuration rebuilt with the
 * driver at {@code t = -1}; the live construction and the semantic identities are untouched.
 * Matrix rows V-REV-EXACT-1 (driver at {@code t = -1}, at {@code t_c} and inside the gap),
 * V-SEL-1 and V-PUB-1 (state part).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu2AuthorModelSelectionSettlementTest {
	private static final List<String> FIRST_FAMILY = List.of("o", "v", "w", "a_1");
	/** Penetration and bite sides of the critical value of {@code excCyl}, one ulp apart. */
	private static final double PENETRATION = Double.longBitsToDouble(0x400d416bb67a38bbL);
	private static final double BITE = Double.longBitsToDouble(0x400d416bb67a38bcL);
	/** {@code t_c} of the matrix (section 1). */
	private static final double T_C = 1.2767651295;
	/** Saved parameter of {@code N}, inside the gap at excCyl 4.0 ({@code pins.H01}). */
	private static final double SAVED = 0.6456498832185171;

	@TempDir
	Path work;

	/**
	 * V-REV-EXACT-1 with the driver at {@code t = -1}, at {@code t_c} and inside the gap:
	 * every one-ulp change publishes one revision whose copy holds the exact bits of
	 * {@code excCyl}, reproduces the live selection state of {@code Q/R} on the accessors
	 * (settling it where the driver leaves the copy in its first initialization) and has the
	 * certificate of the same state reached with the driver at {@code t = -1}.
	 */
	@Test
	void oneUlpChangesAtEveryDriverPositionReproduceTheSelection() throws Exception {
		List<String> failures = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			List<String> problems = new ArrayList<>();
			Map<Double, String> reference = new LinkedHashMap<>();
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			for (double value : new double[] {PENETRATION, BITE, 4.0, Math.nextUp(4.0)}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", value);
				reference.put(value, signatures(app));
				check(app, problems, "t=-1 " + value, value, "not-needed");
			}
			PreG9BCu0Corpus.movePathPoint(app, "N", T_C);
			for (double value : new double[] {BITE, PENETRATION, BITE}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", value);
				check(app, problems, "t_c " + value, value, null);
				compare(problems, "t_c " + value, reference.get(value), signatures(app));
			}
			PreG9BCu0Corpus.setNumber(app, "excCyl", 4.0);
			PreG9BCu0Corpus.movePathPoint(app, "N", SAVED);
			if (app.getKernel().lookupLabel("Q").isDefined()) {
				problems.add("the saved driver is not in the gap");
			}
			for (double value : new double[] {Math.nextUp(4.0), 4.0, Math.nextUp(4.0)}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", value);
				check(app, problems, "gap " + value, value, "settled");
				compare(problems, "gap " + value, reference.get(value), signatures(app));
			}
			return problems;
		});
		assertEquals(List.of(), failures);
	}

	/**
	 * Contract steps 5 and 6 on the author model: the settlement of a revision rebuilt in the
	 * gap computes one auxiliary parameter per locus, reads the live construction only (the
	 * live selection state, the driver and every value are unchanged) and leaves the
	 * persistent locus identities and the component signatures equal to those of the same
	 * revision rebuilt outside the gap.
	 */
	@Test
	void theSettlementTouchesNeitherTheLiveConstructionNorTheIdentities() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			Map<String, Object> values = new LinkedHashMap<>();
			Map<String, String> identities = new LinkedHashMap<>();
			Map<String, Map<LocusCertificationWork2D, Long>> before = new LinkedHashMap<>();
			for (String label : FIRST_FAMILY) {
				identities.put(label, PreG9BCu0Corpus.locus(app, label).getPersistentLocusId()
						.toExternalForm());
				before.put(label, counters(PreG9BCu0Corpus.locus(app, label)));
			}
			String liveBefore = state(live(app));
			// excCyl moves the path of N: its coordinates follow, its parameter does not.
			String driverBefore = parameter((GeoPoint) app.getKernel().lookupLabel("N"));
			PreG9BCu0Corpus.setNumber(app, "excCyl", 4.0);
			values.put("live", liveBefore.equals(state(live(app))));
			values.put("driver", driverBefore.equals(parameter((GeoPoint) app.getKernel()
					.lookupLabel("N"))));
			StringBuilder settlement = new StringBuilder();
			for (String label : FIRST_FAMILY) {
				GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
				ReconstructibleLocusEvaluator2D evaluator = evaluator(locus);
				settlement.append(label).append('=')
						.append(evaluator.getSettlementOutcome().getToken())
						.append('/').append(delta(before.get(label), locus,
								LocusCertificationWork2D.SELECTION_SETTLEMENT))
						.append('/').append(delta(before.get(label), locus,
								LocusCertificationWork2D.SELECTION_SETTLEMENT_PARAMETER))
						.append('/').append(delta(before.get(label), locus,
								LocusCertificationWork2D.SELECTION_SETTLEMENT_FAILURE))
						.append(';');
				values.put("identity." + label, identities.get(label).equals(
						locus.getPersistentLocusId().toExternalForm()));
			}
			values.put("work", settlement.toString());
			String settled = signatures(app);
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			PreG9BCu0Corpus.setNumber(app, "excCyl", 4.5);
			PreG9BCu0Corpus.setNumber(app, "excCyl", 4.0);
			values.put("signatures", settled.equals(signatures(app)));
			return values;
		});
		assertEquals(true, row.get("live"), row.toString());
		assertEquals(true, row.get("driver"), row.toString());
		assertEquals("o=settled/1/1/0;v=settled/1/1/0;w=settled/1/1/0;a_1=settled/1/1/0;",
				row.get("work"), row.toString());
		for (String label : FIRST_FAMILY) {
			assertEquals(true, row.get("identity." + label), row.toString());
		}
		assertEquals(true, row.get("signatures"), row.toString());
	}

	private static void check(AppGeoCeDG app, List<String> problems, String step,
			double value, String outcome) throws Exception {
		GeoNumeric excCyl = (GeoNumeric) app.getKernel().lookupLabel("excCyl");
		if (Double.doubleToRawLongBits(excCyl.getValue()) != Double.doubleToRawLongBits(value)) {
			problems.add(step + " excCyl is not exact");
		}
		AlgoIntersectLineConic live = live(app);
		for (String label : FIRST_FAMILY) {
			ReconstructibleLocusEvaluator2D evaluator = evaluator(PreG9BCu0Corpus.locus(app,
					label));
			GeoElement copyValue = copy(evaluator).lookupLabel("excCyl");
			if (!evaluator.isReconstructionFaithful()) {
				problems.add(step + " " + label + " unfaithful " + evaluator.getFidelityReport());
				continue;
			}
			if (Double.doubleToRawLongBits(((GeoNumeric) copyValue).getValue())
					!= Double.doubleToRawLongBits(value)) {
				problems.add(step + " " + label + " copy of excCyl is not exact");
			}
			AlgoIntersectLineConic copy = (AlgoIntersectLineConic) copy(evaluator)
					.lookupLabel("Q").getParentAlgorithm();
			if (!state(live).equals(state(copy))) {
				problems.add(step + " " + label + " live " + state(live) + " copy "
						+ state(copy));
			}
			if (outcome != null && !outcome.equals(evaluator.getSettlementOutcome().getToken())) {
				problems.add(step + " " + label + " settlement "
						+ evaluator.getSettlementOutcome());
			}
			LocusExistenceStructure2D structure = PreG9BCu0Corpus.locus(app, label)
					.getSemanticDefinition().getBranch(PreG9BCu0Corpus.MAIN)
					.getExistenceStructure();
			if (structure.getCompleteness() != LocusExistenceStructure2D.Completeness.COMPLETE) {
				problems.add(step + " " + label + " " + structure.getCompleteness());
			}
		}
	}

	private static void compare(List<String> problems, String step, String expected,
			String actual) {
		if (!expected.equals(actual)) {
			problems.add(step + " certificate differs from the driver at t = -1");
		}
	}

	/** @return semantic signatures of the published certificates of the first family */
	private static String signatures(AppGeoCeDG app) {
		StringBuilder text = new StringBuilder();
		for (String label : FIRST_FAMILY) {
			text.append(label).append('=').append(PreG9BCu0Corpus.locus(app, label)
					.getSemanticDefinition().getBranch(PreG9BCu0Corpus.MAIN)
					.getExistenceStructure().getSemanticSignature()).append('\n');
		}
		return text.toString();
	}

	private static Map<LocusCertificationWork2D, Long> counters(GeoLocusV2 locus) {
		Map<LocusCertificationWork2D, Long> counters = new LinkedHashMap<>();
		for (LocusCertificationWork2D kind : LocusCertificationWork2D.values()) {
			counters.put(kind, locus.getInstrumentation().getCertificationWork(kind));
		}
		return counters;
	}

	private static long delta(Map<LocusCertificationWork2D, Long> before, GeoLocusV2 locus,
			LocusCertificationWork2D kind) {
		return locus.getInstrumentation().getCertificationWork(kind) - before.get(kind);
	}

	private static AlgoIntersectLineConic live(AppGeoCeDG app) {
		return (AlgoIntersectLineConic) app.getKernel().lookupLabel("Q").getParentAlgorithm();
	}

	private static String state(AlgoIntersectLineConic intersection) {
		return Arrays.toString(intersection.getSelectionPermutation()) + "/"
				+ intersection.isSelectionFirstIntersectionPending() + "/"
				+ intersection.isSelectionPermutationPending();
	}

	private static String parameter(GeoPoint point) {
		return Double.toHexString(point.getPathParameter().getT());
	}

	private static Construction copy(ReconstructibleLocusEvaluator2D evaluator)
			throws ReflectiveOperationException {
		Field field = ReconstructibleLocusEvaluator2D.class.getDeclaredField(
				"isolatedConstruction");
		field.setAccessible(true);
		return (Construction) field.get(evaluator);
	}

	private static ReconstructibleLocusEvaluator2D evaluator(GeoLocusV2 locus)
			throws ReflectiveOperationException {
		Field field = AlgoDependentPointLocusV2.class.getDeclaredField("evaluator");
		field.setAccessible(true);
		return (ReconstructibleLocusEvaluator2D) field.get(locus.getParentAlgorithm());
	}
}
