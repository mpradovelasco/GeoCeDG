/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter.richResultOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidenceOwner2D.ReportOutcome;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricConditionedRoute2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricCoverage;
import org.geocedg.common.kernel.locus.metric.MetricDiagnostic2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricGuaranteeClass2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * Increment I3 of the conditioned use of Locus V2 (ADR 0042; contract {@code
 * post-r1-conditioned-use.md}, sections 5.3, 8.3 and 8.4): the length between two positions
 * of one conditioned region and the total over conditioned regions, at most estimated,
 * with their hypotheses recorded; a band is never dropped from a total; a position inside
 * a band is a valid position with no route. Criterion 4a, route part.
 *
 * <p>Step C4 of the corrections after block 3B (decision DN-2 of the author, 2026-11-22;
 * {@code locus-v2-metrics.md} section 28.3): the length of a route that lies in one
 * conditioned region is admissible as a number, still conditioned and never a proof.</p>
 */
final class PostR1ConditionedMetricTest extends G9U0PublicSurfaceTestBase {

	private GeoLocusV2 outsideClass(String abscissa, String ordinate) {
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(" + abscissa + "," + ordinate + ")");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		assertNotNull(locus);
		return locus;
	}

	private LocusMetricResult2D between(String label, String first, String second) {
		return richResultOf((GeoElement) add(label + "=LocusLength(L," + first + "," + second + ")"))
				.getMetricResult();
	}

	private static boolean has(LocusMetricResult2D result, MetricDiagnosticCode2D code) {
		for (MetricDiagnostic2D diagnostic : result.getDiagnostics()) {
			if (diagnostic.getCode() == code) {
				return true;
			}
		}
		return result.getRouteEvidence().isPresent() && result.getRouteEvidence().get()
				.getDiagnostics().stream().anyMatch(d -> d.getCode() == code);
	}

	private static String text(LocusMetricResult2D result) {
		return result.getComputationStatus() + " " + result.getGuaranteeClass() + " "
				+ result.getDiagnostics() + " route " + result.getRouteEvidence().map(route
						-> route.getRouteStatus() + " " + route.getDiagnostics()).orElse("none");
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

	@Test
	void theLengthBetweenTwoPositionsOfOneRegionIsEstimatedUnderItsHypotheses() {
		GeoLocusV2 locus = outsideClass("x(P)", "exp(x(P))");
		add("A=Point(L,\"" + BRANCH + "\",0.2)");
		add("B=Point(L,\"" + BRANCH + "\",0.7)");
		LocusMetricResult2D result = between("M", "A", "B");
		assertEquals(MetricComputationStatus.SUCCESS, result.getComputationStatus(),
				text(result));
		double reference = simpson(x -> Math.hypot(1, Math.exp(x)), 0.2, 0.7);
		assertEquals(reference, result.getMetricValue().getFiniteValue().orElseThrow(), 1e-6,
				text(result));
		// At most estimated, never a rigorous enclosure, and declared conditioned.
		assertEquals(MetricGuaranteeClass2D.ESTIMATED, result.getGuaranteeClass());
		assertTrue(result.isConditionedEstimate(), text(result));
		assertTrue(has(result, MetricDiagnosticCode2D.EXISTENCE_NOT_CERTIFIED), text(result));
		// Geometric coverage is not complete; the numeric coverage of the route is, and the
		// length is a number (section 28.3) with its hypotheses and what makes it current.
		assertEquals(MetricCoverage.INCOMPLETE, result.getCoverage());
		assertTrue(result.isScalarAdmissible(), text(result));
		assertTrue(has(result, MetricDiagnosticCode2D.CONDITIONED_ROUTE_ADMITTED),
				text(result));
		LocusMetricConditionedRoute2D admitted = result.getConditionedRoute().orElseThrow();
		assertEquals(LocusMetricConditionedRoute2D.VERSION, admitted.getPolicyVersion());
		assertEquals(BRANCH, admitted.getBranchKey());
		assertEquals(0.2, admitted.getRouteLower(), 0);
		assertEquals(0.7, admitted.getRouteUpper(), 0);
		assertTrue(admitted.getRegion().getLower() <= 0.2
				&& admitted.getRegion().getUpper() >= 0.7);
		assertEquals("H1, H2, H3, H4", admitted.getHypotheses());
		assertFalse(admitted.getSpanClasses().isEmpty());
		assertEquals(locus.getConditionedEvidence(BRANCH).getContentIdentity(),
				admitted.getEvidenceContentIdentity());
		assertEquals(locus.getConditionedEvidence(BRANCH).getPolicyVersion(),
				admitted.getTraversalPolicyVersion());
		assertEquals(locus.getSemanticRevision(), admitted.getSemanticRevision());
		assertTrue(result.getErrorEvidence().getEstablishedAbsoluteAmount()
				<= result.getErrorEvidence().getRequestedTolerance().orElseThrow());
		assertTrue(result.getErrorEvidence().getRigorousEnclosure().isEmpty());
		GeoNumeric scalar = (GeoNumeric) lookup("M");
		assertTrue(scalar.isDefined());
		assertEquals(result.getMetricValue().getFiniteValue().orElseThrow(),
				scalar.getValue(), 0);
		// The number feeds an ordinary formula.
		GeoNumeric formula = add("F=2*M+1");
		assertTrue(formula.isDefined());
		assertEquals(2 * scalar.getValue() + 1, formula.getValue(), 0);
		// Nothing is certified by measuring.
		assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED, locus
				.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getCompleteness());
		// The same positions the other way round.
		LocusMetricResult2D reversed = between("N", "B", "A");
		assertNotNull(reversed);
		if (reversed.getComputationStatus() == MetricComputationStatus.SUCCESS
				&& reversed.getMetricValue().getFiniteValue().isPresent()) {
			assertEquals(reference, Math.abs(reversed.getMetricValue().getFiniteValue()
					.orElseThrow()), 1e-6, text(reversed));
		} else {
			assertTrue(has(reversed, MetricDiagnosticCode2D.ROUTE_CROSSES_BAND)
					|| has(reversed, MetricDiagnosticCode2D.TARGET_NOT_REACHABLE),
					text(reversed));
		}
	}

	@Test
	void aRouteNeverCrossesABandAndAPositionInABandHasNoRoute() {
		// An absence up to 0.3 and a jump at 0.75: regions (0.3, 0.75) and (0.75, 1].
		outsideClass("x(P)+floor(x(P)+0.25)", "sqrt(x(P)-0.3)+exp(x(P))");
		add("A=Point(L,\"" + BRANCH + "\",0.5)");
		add("B=Point(L,\"" + BRANCH + "\",0.9)");
		// Strictly inside the band of minimum step that isolates the jump.
		add("C=Point(L,\"" + BRANCH + "\",0.7499995)");
		add("D=Point(L,\"" + BRANCH + "\",0.6)");
		assertTrue(((GeoPoint) lookup("C")).isDefined(), "a valid point inside a band");

		LocusMetricResult2D across = between("M", "A", "B");
		assertNotEquals(MetricComputationStatus.SUCCESS, across.getComputationStatus());
		assertTrue(has(across, MetricDiagnosticCode2D.POSITIONS_IN_DIFFERENT_REGIONS),
				text(across));
		assertTrue(text(across).contains("DISCONTINUITY_BAND"), text(across));
		assertFalse(across.getMetricValue().getFiniteValue().isPresent());

		assertFalse(across.isScalarAdmissible());
		assertFalse(lookup("M").isDefined());

		LocusMetricResult2D inBand = between("N", "A", "C");
		assertNotEquals(MetricComputationStatus.SUCCESS, inBand.getComputationStatus());
		assertTrue(has(inBand, MetricDiagnosticCode2D.POSITION_IN_BAND), text(inBand));
		assertFalse(inBand.getMetricValue().getFiniteValue().isPresent());
		assertFalse(inBand.isScalarAdmissible());
		assertFalse(lookup("N").isDefined());

		LocusMetricResult2D inside = between("K", "A", "D");
		assertEquals(MetricComputationStatus.SUCCESS, inside.getComputationStatus(),
				text(inside));
		double reference = simpson(x -> Math.hypot(1, 0.5 / Math.sqrt(x - 0.3)
				+ Math.exp(x)), 0.5, 0.6);
		assertEquals(reference, inside.getMetricValue().getFiniteValue().orElseThrow(), 1e-6);
		assertTrue(inside.isScalarAdmissible(), text(inside));
		assertTrue(lookup("K").isDefined());
	}

	/**
	 * Section 28.1: a position at an end of a region is in the region, also where a band
	 * shares that end, so a whole region is measured between its two ends; and the same
	 * node is no position of the neighbouring region.
	 */
	@Test
	void aWholeRegionIsMeasuredBetweenItsEnds() {
		// A jump at 0.75: regions [0, 0.75) and [0.75, 1].
		GeoLocusV2 locus = outsideClass("x(P)+floor(x(P)+0.25)", "exp(x(P))");
		List<org.geocedg.common.kernel.locus.LocusInterval2D> regions =
				locus.getConditionedEvidence(BRANCH).getRegions();
		assertEquals(2, regions.size());
		double lower = regions.get(1).getLower();
		double upper = regions.get(1).getUpper();
		assertEquals(0.75, lower, 0);
		assertEquals(1, upper, 0);
		// The lower end is shared with the band that isolates the jump.
		assertNotNull(locus.getConditionedEvidence(BRANCH).bandAt(lower));
		add("A=Point(L,\"" + BRANCH + "\"," + lower + ")");
		add("B=Point(L,\"" + BRANCH + "\"," + upper + ")");
		LocusMetricResult2D whole = between("M", "A", "B");
		assertEquals(MetricComputationStatus.SUCCESS, whole.getComputationStatus(),
				text(whole));
		assertTrue(whole.isScalarAdmissible(), text(whole));
		assertEquals(lower, whole.getConditionedRoute().orElseThrow().getRouteLower(), 0);
		assertEquals(upper, whole.getConditionedRoute().orElseThrow().getRouteUpper(), 0);
		assertEquals(simpson(x -> Math.hypot(1, Math.exp(x)), lower, upper),
				whole.getMetricValue().getFiniteValue().orElseThrow(), 1e-6);
		assertTrue(lookup("M").isDefined());
		// The last node of the first region is in that region, not in the second.
		add("C=Point(L,\"" + BRANCH + "\"," + regions.get(0).getUpper() + ")");
		LocusMetricResult2D other = between("N", "C", "B");
		assertTrue(has(other, MetricDiagnosticCode2D.POSITIONS_IN_DIFFERENT_REGIONS),
				text(other));
		assertFalse(lookup("N").isDefined());
		add("O=Point(L,\"" + BRANCH + "\",0)");
		assertTrue(between("K", "O", "C").isScalarAdmissible());
		assertTrue(lookup("K").isDefined());
	}

	/**
	 * A jump of 9e-4 across the curve is below the chord criterion of the traversal policy
	 * (a declared limit: the cell is a span under H3) and the route across it lies in one
	 * region. The estimator does not converge there, so the route is no number; the
	 * stretch that does not reach the jump is.
	 */
	@Test
	void aRouteAcrossAJumpThatTheTraversalDoesNotSeeIsNoNumber() {
		GeoLocusV2 locus = outsideClass("x(P)", "exp(x(P))+0.0009*floor(x(P)+0.5)");
		assertEquals(1, locus.getConditionedEvidence(BRANCH).getRegions().size());
		add("A=Point(L,\"" + BRANCH + "\",0.2)");
		add("B=Point(L,\"" + BRANCH + "\",0.7)");
		add("C=Point(L,\"" + BRANCH + "\",0.45)");
		LocusMetricResult2D across = between("M", "A", "B");
		assertEquals(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				across.getComputationStatus(), text(across));
		assertTrue(has(across, MetricDiagnosticCode2D.WORK_BUDGET_EXHAUSTED), text(across));
		assertFalse(across.getMetricValue().getFiniteValue().isPresent());
		assertFalse(across.isScalarAdmissible());
		assertFalse(lookup("M").isDefined());
		LocusMetricResult2D before = between("N", "A", "C");
		assertTrue(before.isScalarAdmissible(), text(before));
		assertEquals(simpson(x -> Math.hypot(1, Math.exp(x)), 0.2, 0.45),
				before.getMetricValue().getFiniteValue().orElseThrow(), 1e-6);
	}

	/**
	 * An estimate that does not converge is no number and keeps its own cause: a region
	 * that ends where the derivative of the traced point is unbounded.
	 */
	@Test
	void aRouteWhoseEstimateDoesNotConvergeIsNoNumber() {
		GeoLocusV2 locus = outsideClass("x(P)", "sqrt(x(P)-0.3)+exp(x(P))");
		List<org.geocedg.common.kernel.locus.LocusInterval2D> regions =
				locus.getConditionedEvidence(BRANCH).getRegions();
		assertEquals(1, regions.size());
		add("A=Point(L,\"" + BRANCH + "\"," + regions.get(0).getLower() + ")");
		add("B=Point(L,\"" + BRANCH + "\",0.9)");
		LocusMetricResult2D singular = between("M", "A", "B");
		assertEquals(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				singular.getComputationStatus(), text(singular));
		assertTrue(has(singular, MetricDiagnosticCode2D.WORK_BUDGET_EXHAUSTED),
				text(singular));
		assertFalse(singular.isScalarAdmissible());
		assertFalse(singular.getConditionedRoute().isPresent());
		assertFalse(lookup("M").isDefined());
		// Away from that end the same region is measured.
		add("C=Point(L,\"" + BRANCH + "\",0.5)");
		assertTrue(between("N", "C", "B").isScalarAdmissible());
	}

	/**
	 * The admitted number is recomputed, never kept: it follows a change of the source,
	 * survives a reload and never makes what depends on it certified.
	 */
	@Test
	void theAdmittedNumberFollowsTheSourceAndCertifiesNothingDownstream() {
		add("k=1");
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(x(P),k*exp(x(P)))");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		add("A=Point(L,\"" + BRANCH + "\",0.2)");
		add("B=Point(L,\"" + BRANCH + "\",0.7)");
		GeoNumeric scalar = add("M=LocusLength(L,A,B)");
		assertTrue(scalar.isDefined());
		assertEquals(simpson(x -> Math.hypot(1, Math.exp(x)), 0.2, 0.7), scalar.getValue(),
				1e-6);
		long revision = locus.getSemanticRevision();
		// A real change of the source: another revision, another length.
		GeoNumeric factor = (GeoNumeric) lookup("k");
		factor.setValue(2);
		factor.updateCascade();
		assertNotEquals(revision, locus.getSemanticRevision());
		LocusMetricResult2D changed = richResultOf(scalar).getMetricResult();
		assertEquals(locus.getSemanticRevision(), changed.getConditionedRoute().orElseThrow()
				.getSemanticRevision());
		assertTrue(scalar.isDefined());
		assertEquals(simpson(x -> Math.hypot(1, 2 * Math.exp(x)), 0.2, 0.7),
				scalar.getValue(), 1e-6);
		// What reads the number is outside the certified class: the number is an estimate,
		// never a rigorous input (section 26.3), and the locus that uses it is not certified.
		add("s2=Segment((0,0),(1,0))");
		add("P2=Point(s2)");
		add("Q2=P2+M*(0,1)");
		GeoLocusV2 reading = add("L2=LocusV2(Q2,P2)");
		assertNotEquals(LocusExistenceStructure2D.Completeness.COMPLETE, reading
				.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getCompleteness());
		assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED, locus
				.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getCompleteness());
		// Reloaded: the same number, recomputed.
		double before = scalar.getValue();
		reload();
		GeoNumeric reopened = (GeoNumeric) lookup("M");
		assertTrue(reopened.isDefined());
		assertEquals(before, reopened.getValue(), 1e-12);
		assertTrue(richResultOf(reopened).getMetricResult().getConditionedRoute()
				.isPresent());
	}

	@Test
	void aWithdrawnCellRecomputesTheLengthThatUsedIt() {
		// A hole of width 2e-12 at 0.3: neither the traversal nor the estimator meets it.
		GeoLocusV2 locus = outsideClass("x(P)",
				"x(P)+0*sqrt((x(P)-0.3)^2-1E-24)+0*exp(x(P))");
		add("A=Point(L,\"" + BRANCH + "\",0.21)");
		add("B=Point(L,\"" + BRANCH + "\",0.6)");
		add("C=Point(L,\"" + BRANCH + "\",0.9)");
		GeoElement spanning = add("M=LocusLength(L,A,B)");
		GeoElement apart = add("N=LocusLength(L,B,C)");
		assertEquals(MetricComputationStatus.SUCCESS, richResultOf(spanning)
				.getMetricResult().getComputationStatus());
		assertTrue(spanning.isDefined());
		double kept = richResultOf(apart).getMetricResult().getMetricValue()
				.getFiniteValue().orElseThrow();
		assertEquals(0.3 * Math.sqrt(2), kept, 1e-6);
		// A contradiction confirmed between A and B withdraws cells of that route only.
		assertEquals(ReportOutcome.CONFIRMED,
				locus.reportConditionedContradiction(BRANCH, 0.3));
		LocusMetricResult2D after = richResultOf(spanning).getMetricResult();
		assertNotEquals(MetricComputationStatus.SUCCESS, after.getComputationStatus(),
				text(after));
		assertTrue(has(after, MetricDiagnosticCode2D.POSITIONS_IN_DIFFERENT_REGIONS),
				text(after));
		// The number that was admitted on that route is gone with its region; the other
		// number, on a route that the contradiction does not touch, stays.
		assertFalse(spanning.isDefined());
		assertTrue(apart.isDefined());
		LocusMetricResult2D still = richResultOf(apart).getMetricResult();
		assertEquals(MetricComputationStatus.SUCCESS, still.getComputationStatus());
		assertEquals(kept, still.getMetricValue().getFiniteValue().orElseThrow(), 1e-9);
	}

	@Test
	void aWholePeriodRegionIsMeasuredAcrossItsSeam() {
		add("c=Circle((0,0),1)");
		add("E=Point(c)");
		add("Q=(x(E)*exp(0*y(E)),y(E))");
		GeoLocusV2 locus = add("L=LocusV2(Q,E)");
		assertNotNull(locus);
		assertTrue(locus.getConditionedEvidence(BRANCH).seamNodesAgree());
		add("A=Point(L,\"" + BRANCH + "\",3)");
		add("B=Point(L,\"" + BRANCH + "\",-3)");
		LocusMetricResult2D forward = between("M", "A", "B");
		LocusMetricResult2D backward = between("N", "B", "A");
		double shortArc = 2 * Math.PI - 6;
		double longArc = 6;
		// One of the two orders crosses the seam in the direction of the branch.
		assertEquals(MetricComputationStatus.SUCCESS, forward.getComputationStatus(),
				text(forward));
		assertEquals(MetricComputationStatus.SUCCESS, backward.getComputationStatus(),
				text(backward));
		double first = forward.getMetricValue().getFiniteValue().orElseThrow();
		double second = backward.getMetricValue().getFiniteValue().orElseThrow();
		assertEquals(shortArc + longArc, first + second, 1e-5, first + " + " + second);
		assertEquals(shortArc, Math.min(first, second), 1e-5);
		assertTrue(forward.isConditionedEstimate() && backward.isConditionedEstimate());
	}

	@Test
	void aTotalWithBandsIsASubtotalThatListsThemAndAScalarRefusesIt() {
		// A consumer whose traced point reads a root of an intersection: three regions
		// between four folds, and absences where the ray misses the source.
		add("c=Circle((0,0),2)");
		add("E=Point(c)");
		add("QF=(1,0)");
		add("M=Midpoint(E,QF)");
		add("S=LocusV2(M,E)");
		add("k=Circle((0.5,0),3)");
		add("T=Point(k)");
		add("H=(4,0)");
		add("r=Ray(H,T)");
		GeoPoint driver = (GeoPoint) lookup("T");
		driver.setCoords(0.5 + 3 * Math.cos(3.0), 3 * Math.sin(3.0), 1.0);
		driver.updateCascade();
		GeoLocusIntersectionResult rich = add("v=Intersect(S,r)");
		List<LocusIntersectionSolution2D> roots =
				rich.getIntersectionResult().getFiniteSolutions();
		add("RA=Intersect(v,\"" + roots.get(0).getIdentity().getRootToken() + "\")");
		add("ZP=Midpoint(RA,T)");
		GeoLocusV2 consumer = add("L=LocusV2(ZP,T)");
		getKernel().updateConstruction(false);
		assertEquals(3, consumer.getConditionedEvidence(BRANCH).getRegions().size());

		LocusMetricResult2D total = richResultOf((GeoElement) add("W=LocusLength(L)")).getMetricResult();
		assertEquals(MetricComputationStatus.SUCCESS, total.getComputationStatus(),
				text(total));
		assertTrue(total.isConditionedSubtotal(), text(total));
		assertEquals(MetricCoverage.INCOMPLETE, total.getCoverage());
		assertTrue(total.getMetricValue().getFiniteValue().orElseThrow() > 0);
		assertEquals(3, total.getContributions().size(), "one estimate per region");
		String listed = total.getDiagnostics().toString();
		assertEquals(5, listed.split("TRANSITION_BAND").length, listed);
		assertFalse(listed.contains("ABSENT_BAND"), "an absence contributes nothing");
		assertTrue(total.isConditionedEstimate());
		// A consumer without a subtotal refuses it.
		assertFalse(total.isScalarAdmissible());
		assertFalse(add("b=Length(L)").isDefined());
	}

	@Test
	void theConditionedEstimateOfADeclaredDomainIsUnchanged() {
		// Section 26.5 of the metric contract (H10): a construction outside the class is
		// still measured on its declared domain, with the same value and admissibility.
		outsideClass("x(P)", "exp(x(P))");
		LocusMetricResult2D total = richResultOf((GeoElement) add("W=LocusLength(L)")).getMetricResult();
		assertEquals(MetricComputationStatus.SUCCESS, total.getComputationStatus(),
				text(total));
		assertFalse(total.isConditionedSubtotal());
		assertTrue(total.isScalarAdmissible());
		assertTrue(total.getProvenance().getCapabilityVersion()
				.contains("#conditioned-estimate-"), total.getProvenance().toString());
		assertEquals(simpson(x -> Math.hypot(1, Math.exp(x)), 0, 1), total.getMetricValue()
				.getFiniteValue().orElseThrow(), 1e-6);
		assertTrue(add("b=Length(L)").isDefined());
	}
}
