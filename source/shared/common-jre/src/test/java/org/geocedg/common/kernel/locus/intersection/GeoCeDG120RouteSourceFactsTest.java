/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundarySource2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Maximality;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.intersection.RouteSourceFacts2D.PrecisionStatus;
import org.geocedg.common.kernel.locus.metric.LocusMetricBoxEvidence2D;
import org.junit.jupiter.api.Test;

/** Analytic oracles of the proof integral, independent of ordinary metric estimates. */
class GeoCeDG120RouteSourceFactsTest {
	private static final LocusInterval2D CIRCLE =
			new LocusInterval2D(-Math.PI, Math.PI, true, false);
	private static final BigDecimal REAL_CIRCUMFERENCE =
			new BigDecimal("6.2831853071795864769252867665590057683943387987502");

	private static LocusBoundary2D boundary(LocusBoundary2D.Kind kind, boolean included) {
		return new LocusBoundary2D(kind,
				new LocusBoundarySource2D("analytic-source", kind.name(), "proof"),
				included, LocusEvidenceNature2D.EXACT);
	}

	private static ContinuousComponent interval(double lower, double upper,
			LocusEvidenceNature2D nature) {
		return ContinuousComponent.interval(new LocusInterval2D(lower, upper, true, true),
				boundary(LocusBoundary2D.Kind.DOMAIN_END, true),
				boundary(LocusBoundary2D.Kind.DOMAIN_END, true), Maximality.ESTABLISHED,
				nature, "analytic-affine-component");
	}

	private static ContinuousComponent circle(boolean closed) {
		LocusBoundary2D.Kind kind = closed ? LocusBoundary2D.Kind.PERIODIC_CLOSURE
				: LocusBoundary2D.Kind.DECLARED_CONSTRUCTIVE_CUT;
		return ContinuousComponent.arc(CIRCLE, 0, 0, true, boundary(kind, true),
				boundary(kind, closed), Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT,
				closed ? "analytic-closed-circle" : "analytic-declared-open-turn");
	}

	private static ConstructionBoxModel2D circleModel() {
		return new ConstructionBoxModel2D(new CertifiedConstructionProgram2D(Driver.CIRCLE,
				new double[] {1, 1, 1, 0, 0, 1, 0, 0}, CIRCLE, List.of()));
	}

	private static ConstructionBoxModel2D affineModel(LocusInterval2D domain,
			double xSlope, double ySlope) {
		return new ConstructionBoxModel2D(new CertifiedConstructionProgram2D(Driver.SCALAR_AFFINE,
				new double[] {xSlope, 0, ySlope, 0}, domain, List.of()));
	}

	private static void encloses(double[] bounds, BigDecimal exact) {
		assertNotNull(bounds);
		assertTrue(new BigDecimal(bounds[0]).compareTo(exact) <= 0,
				() -> "Lower bound " + bounds[0] + " exceeds " + exact);
		assertTrue(new BigDecimal(bounds[1]).compareTo(exact) >= 0,
				() -> "Upper bound " + bounds[1] + " misses " + exact);
	}

	private static ConstructionBoxModel2D rationalModel(LocusInterval2D domain) {
		return new ConstructionBoxModel2D(new CertifiedConstructionProgram2D(Driver.SEGMENT,
				new double[] {0, 0, 0, 1}, domain, List.of(
						new Step(Operation.POINT_X, List.of(Operand.node(0))),
						new Step(Operation.SCALAR_ADD, List.of(Operand.node(1), Operand.scalar(1.0 / 64))),
						new Step(Operation.SCALAR_DIVIDE, List.of(Operand.scalar(1), Operand.node(2))),
						new Step(Operation.POINT_COORDINATES, List.of(Operand.node(3), Operand.scalar(0))))));
	}

	private static void enclosesRatio(double[] bounds, long numerator, long denominator) {
		assertNotNull(bounds);
		BigDecimal exactNumerator = BigDecimal.valueOf(numerator);
		BigDecimal exactDenominator = BigDecimal.valueOf(denominator);
		assertTrue(new BigDecimal(bounds[0]).multiply(exactDenominator)
				.compareTo(exactNumerator) <= 0);
		assertTrue(new BigDecimal(bounds[1]).multiply(exactDenominator)
				.compareTo(exactNumerator) >= 0);
	}

	@Test void rationalCurveRefinesAValidCoarseCoverWithoutReadingAnyMetricEstimate() {
		LocusInterval2D domain = new LocusInterval2D(0, 1, true, true);
		var component = interval(0, 1, LocusEvidenceNature2D.EXACT);
		var coarse = RouteSourceFacts2D.buildIndex(rationalModel(domain), domain, component, 64);
		assertTrue(coarse.isCertified());
		assertNull(coarse.getFailureCause());
		assertEquals(PrecisionStatus.EFFORT_BUDGET, coarse.getPrecisionStatus());
		assertEquals(64, coarse.getEvaluatedBoxes());
		enclosesRatio(coarse.totalBounds(), 4096, 65);
		assertTrue(coarse.totalBounds()[1] - coarse.totalBounds()[0] > .25 * coarse.totalBounds()[1]);
		var refined = RouteSourceFacts2D.buildIndex(rationalModel(domain), domain, component);
		assertTrue(refined.isCertified());
		assertNull(refined.getFailureCause());
		assertEquals(PrecisionStatus.TARGET_REACHED, refined.getPrecisionStatus());
		enclosesRatio(refined.totalBounds(), 4096, 65);
		assertTrue(refined.totalBounds()[0] > coarse.totalBounds()[0]);
		assertTrue(refined.totalBounds()[1] < coarse.totalBounds()[1]);
		assertTrue(refined.totalBounds()[1] - refined.totalBounds()[0] <= .25 * refined.totalBounds()[1]);
		assertTrue(refined.getEvaluatedBoxes() > 64
				&& refined.getEvaluatedBoxes() <= RouteSourceFacts2D.COVER_BUDGET);
	}

	@Test void refinementIsDeterministicAndPreservesAnalyticSubintervalAndSpeedBounds() {
		LocusInterval2D domain = new LocusInterval2D(0, 1, true, true);
		var component = interval(0, 1, LocusEvidenceNature2D.EXACT);
		var first = RouteSourceFacts2D.buildIndex(rationalModel(domain), domain, component);
		var second = RouteSourceFacts2D.buildIndex(rationalModel(domain), domain, component);
		assertTrue(first.isCertified());
		assertTrue(second.isCertified());
		assertArrayEquals(first.totalBounds(), second.totalBounds(), 0);
		assertEquals(first.getEvaluatedBoxes(), second.getEvaluatedBoxes());
		assertEquals(first.getPrecisionStatus(), second.getPrecisionStatus());
		enclosesRatio(first.integral(.25, .5, false), 1024, 561);
		enclosesRatio(first.speedBounds(.25, .5), 4096, 289);
		enclosesRatio(first.speedBounds(.25, .5), 4096, 1089);
		int evaluated = first.getEvaluatedBoxes();
		for (int repeat = 0; repeat < 4; repeat++) {
			assertArrayEquals(first.integral(.25, .5, false), second.integral(.25, .5, false), 0);
		}
		assertEquals(evaluated, first.getEvaluatedBoxes(), "Queries reuse the proof partition");
	}

	@Test void anUnavailableOptionalChildRetainsTheIndependentlyCertifiedParentCover() {
		LocusInterval2D domain = new LocusInterval2D(0, 1, true, true);
		var program = new CertifiedConstructionProgram2D(Driver.SEGMENT,
				new double[] {0, 0, 0, 1}, domain, List.of(
						new Step(Operation.METRIC_INPUT, List.of(Operand.node(0)), "analytic-affine"),
						new Step(Operation.POINT_COORDINATES, List.of(Operand.node(1), Operand.scalar(0)))));
		// Sound but incomplete proof service for F(t)=t: its derivative is inside [0,4].
		// It deliberately has no proof on the narrower child vocabulary.
		ConstructionBoxModel2D.MetricEnclosureProvider provider = (node, lower, upper) ->
				upper - lower < 1.0 / 64 ? null
						: LocusMetricBoxEvidence2D.enclosed(lower, upper, 0, 4, "analytic-affine/v1");
		var component = interval(0, 1, LocusEvidenceNature2D.EXACT);
		var coarse = RouteSourceFacts2D.buildIndex(new ConstructionBoxModel2D(program, provider),
				domain, component, 64);
		var retained = RouteSourceFacts2D.buildIndex(new ConstructionBoxModel2D(program, provider),
				domain, component);
		assertTrue(retained.isCertified());
		assertNull(retained.getFailureCause());
		assertEquals(PrecisionStatus.NO_IMPROVEMENT, retained.getPrecisionStatus());
		assertArrayEquals(coarse.totalBounds(), retained.totalBounds(), 0);
		encloses(retained.totalBounds(), BigDecimal.ONE);
		assertTrue(retained.getEvaluatedBoxes() > coarse.getEvaluatedBoxes());
	}

	@Test void theDownwardBudgetSeamCannotIncreaseEffortOrPublishAnIncompleteCover() {
		LocusInterval2D domain = new LocusInterval2D(0, 1, true, true);
		var model = rationalModel(domain);
		var component = interval(0, 1, LocusEvidenceNature2D.EXACT);
		var incomplete = RouteSourceFacts2D.buildIndex(model, domain, component, 63);
		assertFalse(incomplete.isCertified());
		assertEquals(LocusCoverageCause2D.BUDGET_EXHAUSTED, incomplete.getFailureCause());
		assertEquals(PrecisionStatus.NOT_AVAILABLE, incomplete.getPrecisionStatus());
		assertNull(incomplete.totalBounds());
		assertEquals(63, incomplete.getEvaluatedBoxes());
		var withoutRealSeam = RouteSourceFacts2D.buildIndex(circleModel(), CIRCLE, circle(true), 64);
		assertFalse(withoutRealSeam.isCertified(), "Canonical cells cannot replace the real seam proof");
		assertEquals(LocusCoverageCause2D.BUDGET_EXHAUSTED, withoutRealSeam.getFailureCause());
		assertThrows(IllegalArgumentException.class,
				() -> RouteSourceFacts2D.buildIndex(model, domain, component, 0));
		assertThrows(IllegalArgumentException.class,
				() -> RouteSourceFacts2D.buildIndex(model, domain, component, RouteSourceFacts2D.COVER_BUDGET + 1));
	}

	@Test void affineProofEnclosesExactIntegralAndEuclideanVelocity() {
		LocusInterval2D domain = new LocusInterval2D(0, 1, true, true);
		var index = RouteSourceFacts2D.buildIndex(affineModel(domain, 3, 4), domain,
				interval(0, 1, LocusEvidenceNature2D.EXACT));
		assertTrue(index.isCertified());
		encloses(index.totalBounds(), new BigDecimal("5"));
		encloses(index.integral(.25, .5, false), new BigDecimal("1.25"));
		encloses(index.speedBounds(.3, .31), new BigDecimal("5"));
		assertTrue(index.totalBounds()[1] - index.totalBounds()[0] < 1e-10);
		assertTrue(index.getEvaluatedBoxes() <= RouteSourceFacts2D.COVER_BUDGET);
	}

	@Test void anExactlyCollapsedCurveRetainsZeroSpeedLengthAndCircleGap() {
		var model = new ConstructionBoxModel2D(new CertifiedConstructionProgram2D(Driver.CIRCLE,
				new double[] {1, 1, 1, 0, 0, 1, 0, 0}, CIRCLE,
				List.of(new Step(Operation.DILATE, List.of(Operand.node(0)), 0, 1, 1))));
		var index = RouteSourceFacts2D.buildIndex(model, CIRCLE, circle(true));
		assertTrue(index.isCertified());
		assertEquals(PrecisionStatus.TARGET_REACHED, index.getPrecisionStatus());
		assertArrayEquals(new double[] {0, 0}, index.speedBounds(-1, 1), 0);
		assertArrayEquals(new double[] {0, 0}, index.totalBounds(), 0,
				"A proven zero derivative must not acquire a positive subnormal length bound");
		assertArrayEquals(new double[] {0, 0}, index.integral(1, -1, true), 0);
		assertEquals(0, index.getSeamUpperBound(), 0);
	}

	@Test void circleCoverIncludesTheRealPeriodGapWithoutMakingBinaryPiExact() {
		var index = RouteSourceFacts2D.buildIndex(circleModel(), CIRCLE, circle(true));
		assertTrue(index.isCertified());
		encloses(index.totalBounds(), REAL_CIRCUMFERENCE);
		encloses(index.integral(1, -1, true), REAL_CIRCUMFERENCE.subtract(new BigDecimal("2")));
		assertTrue(index.getSeamUpperBound() > 0);
		assertTrue(index.getSeamUpperBound() < 1e-12);
		double[] local = index.integral(-.01, .01, false);
		encloses(local, new BigDecimal(.01).subtract(new BigDecimal(-.01)));
		assertTrue(local[1] < .03, "A small proof box must not get the whole-period range");
	}

	@Test void declaredOpenFullTurnUsesTheSameRigorousSpeedProof() {
		ContinuousComponent component = circle(false);
		var model = circleModel();
		var index = RouteSourceFacts2D.buildIndex(model, CIRCLE, component);
		assertTrue(index.isCertified());
		assertFalse(component.isClosedPeriodic());
		encloses(index.totalBounds(), REAL_CIRCUMFERENCE);
		encloses(index.integral(0, 0, true), REAL_CIRCUMFERENCE);
		assertTrue(RouteSourceFacts2D.chordLowerBound(model, CIRCLE, component, 0) > 1.9);
	}

	@Test void partialProofNeverIntegratesOutsideItsCertifiedComponent() {
		LocusInterval2D domain = new LocusInterval2D(0, 4, true, true);
		var index = RouteSourceFacts2D.buildIndex(affineModel(domain, 1, 0), domain,
				interval(1, 2, LocusEvidenceNature2D.EXACT));
		assertTrue(index.isCertified());
		encloses(index.integral(1.2, 1.8, false),
				new BigDecimal(1.8).subtract(new BigDecimal(1.2)));
		assertNull(index.integral(.5, 1.5, false));
		assertNull(index.speedBounds(.5, 1.5));
		assertNull(index.integral(1.8, 1.2, true));
	}

	@Test void noncertifyingComponentCannotAuthorizeAnyBound() {
		LocusInterval2D domain = new LocusInterval2D(0, 1, true, true);
		var index = RouteSourceFacts2D.buildIndex(affineModel(domain, 1, 0), domain,
				interval(0, 1, LocusEvidenceNature2D.EXPLORATORY));
		assertFalse(index.isCertified());
		assertEquals(LocusCoverageCause2D.NOT_CERTIFIED, index.getFailureCause());
		assertNull(index.totalBounds());
		assertNull(index.integral(0, 1, false));
	}

	@Test void anUnprovableCoverProducesTypedUnknownInsteadOfFiniteBounds() {
		// A purported full component cannot override the actual zero direction at t=0.
		var program = new CertifiedConstructionProgram2D(Driver.CIRCLE,
				new double[] {1, 1, 1, 0, 0, 1, 0, 0}, CIRCLE, List.of(
						new Step(Operation.VECTOR_POINTS,
								List.of(Operand.point(1, 0, 1, 1, 0), Operand.node(0))),
						new Step(Operation.UNIT_VECTOR, List.of(Operand.node(1)), 1),
						new Step(Operation.POINT_PLUS_VECTOR,
								List.of(Operand.point(0, 0, 1, 0, 0), Operand.node(2)), 1)));
		var index = RouteSourceFacts2D.buildIndex(new ConstructionBoxModel2D(program),
				CIRCLE, circle(true));
		assertFalse(index.isCertified());
		assertEquals(LocusCoverageCause2D.BUDGET_EXHAUSTED, index.getFailureCause());
		assertTrue(index.getEvaluatedBoxes() <= RouteSourceFacts2D.COVER_BUDGET);
		assertNull(index.totalBounds());
		assertNull(index.integral(0, 1, false));
	}
}
