/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter.richResultOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.metric.LocusConditionedRouteJumpGuard2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricConditionedRoute2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricDiagnostic2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricGuaranteeClass2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-B5a ({@code locus-v2-metrics.md} section 28.3.1, admission
 * {@code conditioned-length-scalar/v2}, policy {@code conditioned-route-jump-guard/v1}) through
 * the public surface: the probe P-1 of the P0 characterization (a line and an arc that jump
 * along their chord at {@code 0.75}, which the traversal does not see) gives no number; the
 * stretch before the jump stays a number; a fold end keeps its own cause; an admitted route
 * records the check; the estimate itself is unchanged.
 */
final class PreG9BMaintB5aChordScalingCheckTest extends G9U0PublicSurfaceTestBase {
	/** The scalar that 1.2.2 admitted for the line of P-1 (characterization report, P-1). */
	private static final double P1_LINE_ESTIMATE = 2.459674775249769;
	/** The scalar that 1.2.2 admitted for the arc of P-1. */
	private static final double P1_ARC_ESTIMATE = 0.35128942567868376;

	private GeoLocusV2 segmentLocus(String abscissa, String ordinate) {
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(" + abscissa + "," + ordinate + ")");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		assertNotNull(locus);
		return locus;
	}

	private LocusMetricResult2D between(String label, double first, double second) {
		add(label + "A=Point(L,\"" + BRANCH + "\"," + first + ")");
		add(label + "B=Point(L,\"" + BRANCH + "\"," + second + ")");
		return richResultOf((GeoElement) add(label + "=LocusLength(L," + label + "A,"
				+ label + "B)")).getMetricResult();
	}

	private static boolean has(LocusMetricResult2D result, MetricDiagnosticCode2D code) {
		return result.getDiagnostics().stream().anyMatch(d -> d.getCode() == code);
	}

	private static String message(LocusMetricResult2D result, MetricDiagnosticCode2D code) {
		return result.getDiagnostics().stream().filter(d -> d.getCode() == code)
				.map(MetricDiagnostic2D::getMessage).findFirst().orElse("");
	}

	private static boolean anyCheckDiagnostic(LocusMetricResult2D result) {
		return has(result, MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE)
				|| has(result, MetricDiagnosticCode2D.CHORD_SCALING_INCONCLUSIVE)
				|| has(result, MetricDiagnosticCode2D.CHORD_SCALING_BUDGET_EXHAUSTED);
	}

	private static double simpson(DoubleUnaryOperator function, double lower, double upper) {
		int cells = 1 << 14;
		double width = (upper - lower) / cells;
		double sum = function.applyAsDouble(lower) + function.applyAsDouble(upper);
		for (int index = 1; index < cells; index++) {
			sum += function.applyAsDouble(lower + index * width) * (index % 2 == 0 ? 2 : 4);
		}
		return sum * width / 3;
	}

	/** The refusal of a route by condition 8, with its estimate kept in the rich result. */
	private void assertRefusedBySignature(LocusMetricResult2D result, String label,
			double estimate) {
		String text = result.getDiagnostics().toString();
		assertEquals(MetricComputationStatus.SUCCESS, result.getComputationStatus(), text);
		assertEquals(estimate, result.getMetricValue().getFiniteValue().orElseThrow(), 0,
				"the estimate is unchanged and kept in the rich result");
		assertEquals(MetricGuaranteeClass2D.ESTIMATED, result.getGuaranteeClass());
		assertTrue(result.isConditionedEstimate(), text);
		assertFalse(result.isScalarAdmissible(), text);
		assertFalse(result.getConditionedRoute().isPresent(), text);
		assertFalse(has(result, MetricDiagnosticCode2D.CONDITIONED_ROUTE_ADMITTED), text);
		assertTrue(has(result, MetricDiagnosticCode2D.CONDITIONED_ROUTE_NOT_ADMISSIBLE), text);
		assertTrue(has(result, MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE), text);
		assertFalse(has(result, MetricDiagnosticCode2D.CHORD_SCALING_INCONCLUSIVE), text);
		assertFalse(has(result, MetricDiagnosticCode2D.CHORD_SCALING_BUDGET_EXHAUSTED), text);
		String signature = message(result, MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE);
		// The diagnostic records the leaf, the interval of the decision, the ratios, the
		// policy version and the evaluations; it names the signature, never a proven jump.
		assertTrue(signature.startsWith(LocusConditionedRouteJumpGuard2D.VERSION), signature);
		assertTrue(signature.contains("leaf ") && signature.contains("decision interval [")
				&& signature.contains("ratios [") && signature.contains("evaluation(s)"),
				signature);
		assertTrue(signature.contains("compatible with a discontinuity or with a singular "
				+ "variation of speed"), signature);
		assertTrue(signature.contains("demonstrates neither a jump nor that H3 is false"),
				signature);
		assertTrue(message(result, MetricDiagnosticCode2D.CONDITIONED_ROUTE_NOT_ADMISSIBLE)
				.contains(LocusMetricConditionedRoute2D.VERSION));
		assertFalse(lookup(label).isDefined(), "no number");
	}

	/** An admitted route of version 2 with the record of its check. */
	private static LocusConditionedRouteJumpGuard2D.Record assertAdmitted(
			LocusMetricResult2D result) {
		String text = result.getDiagnostics().toString();
		assertTrue(result.isScalarAdmissible(), text);
		assertFalse(anyCheckDiagnostic(result), text);
		assertFalse(has(result, MetricDiagnosticCode2D.CONDITIONED_ROUTE_NOT_ADMISSIBLE), text);
		LocusMetricConditionedRoute2D route = result.getConditionedRoute().orElseThrow();
		assertEquals("conditioned-length-scalar/v2", route.getPolicyVersion());
		LocusConditionedRouteJumpGuard2D.Record record = route.getJumpGuard().orElseThrow();
		assertEquals("conditioned-route-jump-guard/v1", record.getPolicyVersion());
		assertEquals("no chord-scaling signature observed; this is not a proof of continuity",
				record.getStatement());
		assertTrue(record.getLeavesTested() > 0);
		assertTrue(record.getEvaluations() <= (long) LocusConditionedRouteJumpGuard2D
				.LEVELS_PER_LEAF * record.getLeavesTested());
		assertTrue(record.getEvaluations() <= LocusConditionedRouteJumpGuard2D.EVALUATION_CAP);
		String admitted = result.getDiagnostics().stream().filter(d -> d.getCode()
				== MetricDiagnosticCode2D.CONDITIONED_ROUTE_ADMITTED).findFirst().orElseThrow()
				.getMessage();
		assertTrue(admitted.contains(record.toString()), admitted);
		assertTrue(admitted.contains("H1, H2, H3, H4"), admitted);
		return record;
	}

	@Test
	void theLineOfProbeP1IsNoNumberAndItsStretchesBesideTheJumpAre() {
		GeoLocusV2 locus = segmentLocus("x(P)+floor(x(P)+0.25)", "2*(x(P)+floor(x(P)+0.25))");
		// The traversal does not see the jump: one region, as in 1.2.2.
		assertEquals(1, locus.getConditionedEvidence(BRANCH).getRegions().size());
		assertRefusedBySignature(between("M", 0.7, 0.8), "M", P1_LINE_ESTIMATE);
		// The stretch before the jump, and the one after it, stay numbers. On a straight
		// stretch every leaf is the minimum partition (64 leaves) and every leaf ends without
		// signature after L - m + 1 = 4 evaluations.
		LocusMetricResult2D before = between("N", 0.7, 0.74);
		LocusConditionedRouteJumpGuard2D.Record record = assertAdmitted(before);
		assertEquals(0.04 * Math.sqrt(5), before.getMetricValue().getFiniteValue()
				.orElseThrow(), 1e-12);
		assertEquals(64, record.getLeavesTested());
		assertEquals(0, record.getLeavesBelowResolution());
		assertEquals(4L * 64, record.getEvaluations());
		assertTrue(lookup("N").isDefined());
		assertAdmitted(between("K", 0.76, 0.8));
		assertTrue(lookup("K").isDefined());
	}

	@Test
	void theArcOfProbeP1IsNoNumberAndItsStretchBeforeTheJumpIs() {
		GeoLocusV2 locus = segmentLocus("x(P)+0.05*floor(x(P)+0.25)",
				"exp(x(P))+0.05*floor(x(P)+0.25)*exp(0.75)");
		assertEquals(1, locus.getConditionedEvidence(BRANCH).getRegions().size());
		assertRefusedBySignature(between("M", 0.7, 0.8), "M", P1_ARC_ESTIMATE);
		LocusMetricResult2D before = between("N", 0.7, 0.74);
		assertAdmitted(before);
		assertEquals(simpson(x -> Math.hypot(1, Math.exp(x)), 0.7, 0.74),
				before.getMetricValue().getFiniteValue().orElseThrow(), 1e-9);
	}

	@Test
	void aFoldEndKeepsItsOwnCauseAndTheCheckDoesNotRun() {
		GeoLocusV2 locus = segmentLocus("x(P)", "sqrt(x(P)-0.3)+exp(x(P))");
		List<LocusInterval2D> regions = locus.getConditionedEvidence(BRANCH).getRegions();
		assertEquals(1, regions.size());
		LocusMetricResult2D fold = between("M", regions.get(0).getLower(), 0.9);
		// The estimate does not converge: its status and cause are those of 1.2.2.
		assertEquals(MetricComputationStatus.LIMIT_NOT_ESTABLISHED, fold.getComputationStatus());
		assertTrue(has(fold, MetricDiagnosticCode2D.WORK_BUDGET_EXHAUSTED),
				fold.getDiagnostics().toString());
		assertFalse(anyCheckDiagnostic(fold), fold.getDiagnostics().toString());
		assertFalse(has(fold, MetricDiagnosticCode2D.CONDITIONED_ROUTE_NOT_ADMISSIBLE));
		assertFalse(fold.isScalarAdmissible());
		assertFalse(lookup("M").isDefined());
		// Away from the fold the same region is measured and checked.
		assertAdmitted(between("N", 0.5, 0.9));
	}

	@Test
	void anAdmittedRouteRecordsTheCheckAndKeepsItsEstimate() {
		segmentLocus("x(P)", "exp(x(P))");
		LocusMetricResult2D result = between("M", 0.2, 0.7);
		LocusConditionedRouteJumpGuard2D.Record record = assertAdmitted(result);
		assertEquals(64, record.getLeavesTested());
		assertEquals(4L * 64, record.getEvaluations());
		// The value and the error are those of the estimate, which the check never changes.
		assertEquals(0.9388979755898462, result.getMetricValue().getFiniteValue()
				.orElseThrow(), 0);
		assertEquals(simpson(x -> Math.hypot(1, Math.exp(x)), 0.2, 0.7),
				result.getMetricValue().getFiniteValue().orElseThrow(), 1e-6);
		assertTrue(result.getErrorEvidence().getEstablishedAbsoluteAmount()
				<= result.getErrorEvidence().getRequestedTolerance().orElseThrow());
		GeoNumeric scalar = (GeoNumeric) lookup("M");
		assertTrue(scalar.isDefined());
		assertEquals(result.getMetricValue().getFiniteValue().orElseThrow(), scalar.getValue(),
				0);
		// Determinism: the same revision and policy give the same outcome and record.
		getKernel().updateConstruction(false);
		LocusMetricResult2D again = richResultOf(scalar).getMetricResult();
		assertEquals(record.toString(), again.getConditionedRoute().orElseThrow()
				.getJumpGuard().orElseThrow().toString());
		assertEquals(result.getMetricValue().getFiniteValue().orElseThrow(),
				again.getMetricValue().getFiniteValue().orElseThrow(), 0);
	}

	@Test
	void aRefusedNumberMakesItsFormulaUndefinedAndIsRecomputedAfterReloading() {
		segmentLocus("x(P)+floor(x(P)+0.25)", "2*(x(P)+floor(x(P)+0.25))");
		between("M", 0.7, 0.8);
		add("F=2*M+1");
		assertFalse(lookup("M").isDefined());
		assertFalse(lookup("F").isDefined(), "an undefined number propagates through the DAG");
		reload();
		LocusMetricResult2D reopened = richResultOf(lookup("M")).getMetricResult();
		assertTrue(has(reopened, MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE),
				reopened.getDiagnostics().toString());
		assertEquals(P1_LINE_ESTIMATE, reopened.getMetricValue().getFiniteValue()
				.orElseThrow(), 0);
		assertFalse(lookup("M").isDefined());
		assertFalse(lookup("F").isDefined());
	}
}
