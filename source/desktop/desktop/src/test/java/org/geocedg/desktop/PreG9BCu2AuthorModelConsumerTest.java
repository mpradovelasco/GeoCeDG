/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Result;
import org.geocedg.common.kernel.locus.intersection.IntersectionEndpointProvenanceResolver2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU2 step T9: consumers of the author model read the existence structure, with the
 * shadow certificates injected into definitions that keep the live evaluators while the
 * compatibility certificate is still published (consumption design sections 3 to 5.3): the
 * chain {@code o_1 -> w_1 -> d_2 -> e_2} stays defined (V-CON-10), the excluded end of
 * {@code e_2} is its one-sided limit through the explicit query of the slice, never the image
 * of {@code t_N} (P2, precursor of V-E2-8), and a point of the evaluator inside a certified
 * gap is not published as valid (V-AVAIL-1). Step T11 reruns them on published
 * certificates.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu2AuthorModelConsumerTest {
	/** First binary64 of the bite: two simple roots 2.4e-8 apart (analysis section 3.8.3). */
	private static final double FIRST_BITE = Double.longBitsToDouble(0x400d416bb67a38bcL);

	@TempDir
	Path work;

	/**
	 * V-CON-10: the generating occurrence of the driver on {@code o_1} is addressed through
	 * the certified components of its existence structure, and {@code w_1}, {@code d_2} and
	 * {@code e_2} stay defined.
	 */
	@Test
	void theGeneratingOccurrenceChainStaysDefined() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			Map<String, Object> values = new LinkedHashMap<>();
			GeoLocusV2 o1 = PreG9BCu0Corpus.locus(app, "o_1");
			for (double parameter : new double[] {-Math.PI, -1, 0, 1, 3}) {
				values.put("address@" + parameter, IntersectionEndpointProvenanceResolver2D
						.addressFromRawParameter(o1.getSemanticDefinition(),
								o1.getPersistentLocusId(), PreG9BCu0Corpus.MAIN, parameter)
						!= null);
			}
			AlgoLocusBetweenMetricV2 w1 = (AlgoLocusBetweenMetricV2) app.getKernel()
					.lookupLabel("w_1").getParentAlgorithm();
			LocusMetricResult2D length = w1.getResult().getMetricResult();
			values.put("w_1", length.getComputationStatus() + "/" + length.isScalarAdmissible());
			GeoElement d2 = app.getKernel().lookupLabel("d_2");
			values.put("d_2", d2.isDefined());
			GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_2");
			values.put("e_2", e2.isDefined() && e2.getSemanticDefinition() != null);
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8)) {
				values.put("e_2@1", e2.evaluate(PreG9BCu0Corpus.MAIN, 1, session).getStatus()
						.name());
			}
			return values;
		});
		for (double parameter : new double[] {-Math.PI, -1, 0, 1, 3}) {
			assertEquals(true, row.get("address@" + parameter), row.toString());
		}
		assertEquals("SUCCESS/true", row.get("w_1"), row.toString());
		assertEquals(true, row.get("d_2"), row.toString());
		assertEquals(true, row.get("e_2"), row.toString());
		assertEquals("VALID", row.get("e_2@1"), row.toString());
	}

	/**
	 * P2 and precursor of V-E2-8: on the certified open arc of {@code e_2}, the start
	 * {@code s = 0} is the image of {@code t_N = 0}; the excluded end {@code s = 2 pi} is the
	 * image of no canonical parameter and the explicit query evaluates the copy of
	 * {@code w_1} at the origin with the opposite same-position policy, {@code w_1 = L}. The
	 * limit agrees with the driver just below {@code t_N} and lies the total length
	 * {@code L} of {@code o_1} away from the start.
	 */
	@Test
	void theExcludedEndOfE2IsItsOneSidedLimit() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_2");
			LocusDefinition2D certified = injected(e2, certificate(e2).getStructure());
			Map<String, Object> values = new LinkedHashMap<>();
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(16)) {
				LocusEvaluation2D start = certified.evaluateComponentEnd(PreG9BCu0Corpus.MAIN,
						0, false, session);
				LocusEvaluation2D end = certified.evaluateComponentEnd(PreG9BCu0Corpus.MAIN,
						0, true, session);
				LocusEvaluation2D atOrigin = certified.evaluate(PreG9BCu0Corpus.MAIN, 0,
						session);
				LocusEvaluation2D below = certified.evaluate(PreG9BCu0Corpus.MAIN, -1e-9,
						session);
				LocusEvaluation2D again = certified.evaluate(PreG9BCu0Corpus.MAIN, 0, session);
				values.put("statuses", start.getStatus() + "/" + end.getStatus() + "/"
						+ atOrigin.getStatus() + "/" + below.getStatus());
				values.put("startIsOrigin", distance(start.getPoint(), atOrigin.getPoint()));
				values.put("endToBelow", distance(end.getPoint(), below.getPoint()));
				values.put("endToStart", distance(end.getPoint(), start.getPoint()));
				values.put("restored", distance(again.getPoint(), atOrigin.getPoint()));
			}
			AlgoLocusBetweenMetricV2 w1 = (AlgoLocusBetweenMetricV2) app.getKernel()
					.lookupLabel("w_1").getParentAlgorithm();
			values.put("total", w1.resolveJumpValue().getMetricValue().getFiniteValue()
					.getAsDouble());
			return values;
		});
		assertEquals("VALID/VALID/VALID/VALID", row.get("statuses"), row.toString());
		assertEquals(0.0, (Double) row.get("startIsOrigin"), 0, row.toString());
		assertTrue((Double) row.get("endToBelow") < 1e-6, row.toString());
		// e_2 moves by w_1 along a fixed direction: its two ends are L apart (V-E2-3).
		assertEquals((Double) row.get("total"), (Double) row.get("endToStart"), 1e-9,
				row.toString());
		assertEquals(0.0, (Double) row.get("restored"), 0,
				"the persisted policy applies again after the query " + row);
	}

	/**
	 * V-AVAIL-1: at the first binary64 of the bite, the evaluator of {@code o} returns a point
	 * inside the certified gap around {@code t_c} (F-CU2-1); with the certificate the point is
	 * not published as valid: {@code DEPENDENCY_UNDEFINED}, certified nonexistence and the
	 * disagreement of the evaluator.
	 */
	@Test
	void anEvaluatorPointInACertifiedGapIsNotPublished() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			PreG9BCu0Corpus.setNumber(app, "excCyl", FIRST_BITE);
			GeoLocusV2 o = PreG9BCu0Corpus.locus(app, "o");
			LocusExistenceStructure2D structure = certificate(o).getStructure();
			Coverage gap = null;
			for (Coverage item : structure.getCoverage()) {
				if (item.getState() == CoverageState.CERTIFIED_DOES_NOT_EXIST
						&& item.getNature().certifies()) {
					gap = item;
				}
			}
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("gaps", gap == null ? "none" : gap.getInterval().toString());
			if (gap != null) {
				double inside = gap.getInterval().getLower() / 2
						+ gap.getInterval().getUpper() / 2;
				LocusDefinition2D certified = injected(o, structure);
				try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8)) {
					values.put("live", o.evaluate(PreG9BCu0Corpus.MAIN, inside, session)
							.getStatus().name());
					LocusEvaluation2D evaluation = certified.evaluate(PreG9BCu0Corpus.MAIN,
							inside, session);
					values.put("certified", evaluation.getStatus().name());
					values.put("diagnostic", evaluation.getDiagnostic());
				}
			}
			return values;
		});
		assertTrue(!"none".equals(row.get("gaps")), row.toString());
		assertEquals("VALID", row.get("live"), "F-CU2-1: the evaluator gives a point " + row);
		assertEquals("DEPENDENCY_UNDEFINED", row.get("certified"), row.toString());
		String diagnostic = (String) row.get("diagnostic");
		assertTrue(diagnostic.startsWith(LocusDefinition2D.CERTIFIED_NONEXISTENCE), diagnostic);
		assertTrue(diagnostic.contains(LocusDefinition2D.EVALUATOR_DISAGREEMENT), diagnostic);
	}

	/**
	 * A definition with the live evaluator and the shadow certificate, as step T11 will
	 * publish it.
	 */
	private static LocusDefinition2D injected(GeoLocusV2 locus,
			LocusExistenceStructure2D structure) {
		LocusDefinition2D published = locus.getSemanticDefinition();
		LocusBranch2D branch = published.getBranch(PreG9BCu0Corpus.MAIN);
		LocusBranch2D certified = new LocusBranch2D(branch.getBranchKey(),
				branch.getDeclaredDriverDomain(), structure, branch.getOrientation(),
				branch.getProvenance(), branch.getLineage(), branch.getProperties(),
				branch.getQuality());
		return new LocusDefinition2D(published.getLocusIdentity(),
				published.getSemanticRevision(), published.getDefinitionStatus(),
				published.getProvider(), List.of(certified),
				published.getEvaluatorCapability(), published.getDeterminism(),
				"t9-injected|" + structure.getCertifierContract(),
				new LocusInstrumentation2D());
	}

	private static Result certificate(GeoLocusV2 locus) {
		Result result = ((AlgoDependentPointLocusV2) locus.getParentAlgorithm())
				.getHybridExistenceCertificate();
		if (result == null) {
			throw new IllegalStateException("No shadow certificate");
		}
		return result;
	}

	private static double distance(LocusPoint2D first, LocusPoint2D second) {
		return Math.hypot(first.getX() - second.getX(), first.getY() - second.getY());
	}
}
