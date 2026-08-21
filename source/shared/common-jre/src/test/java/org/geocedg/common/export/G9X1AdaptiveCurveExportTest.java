/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.CurveEvaluation2D;
import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.CurveEvaluator2D;
import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.Result;
import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.WorkLedger;
import org.geocedg.common.export.ApproximationEvidence.Guarantee;
import org.geocedg.common.export.ApproximationEvidence.Method;
import org.geocedg.common.export.GeometryExportModel.Point2D;
import org.geocedg.common.export.GeometryExportModel.PolylineGeometry;
import org.geocedg.common.export.GeometryExportRequest.SemanticDomain;
import org.geocedg.common.export.SourceExportOutcome.Reason;
import org.junit.jupiter.api.Test;

/** Focused contract tests for export-only oriented dyadic approximation. */
class G9X1AdaptiveCurveExportTest {

	private final AdaptiveCurveApproximationBuilder2D approximation =
			new AdaptiveCurveApproximationBuilder2D();

	// X1-A01
	@Test
	void a01DyadicApproximationAndRerunAreBitDeterministic() {
		GeometryExportRequest request = request(0.01);
		SemanticDomain domain = closedDomain("component", 0, 1);
		Result first = approximation.approximate(parabola(), domain, false,
				request);
		Result second = approximation.approximate(parabola(), domain, false,
				request);

		assertTrue(first.isSuccess());
		assertTrue(second.isSuccess());
		assertEquals(vertexBits(first), vertexBits(second));
		assertEquals(first.getEvaluations(), second.getEvaluations());
		assertEquals(first.getSubdivisions(), second.getSubdivisions());
		assertEquals(first.getSegments(), second.getSegments());
		assertEquals(first.getMaximumDepthReached(),
				second.getMaximumDepthReached());
		for (Point2D vertex : first.getVertices()) {
			double scaledParameter = Math.scalb(vertex.getX(),
					GeometryExportRequest.DEFAULT_MAXIMUM_DEPTH);
			assertEquals(Math.rint(scaledParameter), scaledParameter, 0);
		}
	}

	// X1-A02
	@Test
	void a02DescendingSemanticDomainPreservesSourceOrientation() {
		Result result = approximation.approximate(parabola(),
				closedDomain("descending", 2, -1), false, request(0.02));

		assertTrue(result.isSuccess());
		assertEquals(2, result.getVertices().get(0).getX(), 0);
		assertEquals(-1, last(result).getX(), 0);
		for (int index = 1; index < result.getVertices().size(); index++) {
			assertTrue(result.getVertices().get(index - 1).getX()
					> result.getVertices().get(index).getX());
		}
	}

	// X1-A03
	@Test
	void a03OnlyClosedFiniteEndpointsAreEvaluatedExactly() {
		GeometryExportRequest request = request(0.1);
		Result open = approximation.approximate(line(),
				new SemanticDomain("open", 0, 1, false, true), false, request);
		List<Double> parameters = new ArrayList<>();
		CurveEvaluator2D recording = parameter -> {
			parameters.add(parameter);
			return point(parameter, 2 * parameter);
		};
		Result closed = approximation.approximate(recording,
				closedDomain("closed", 3, -2), false, request);

		assertFalse(open.isSuccess());
		assertEquals(Reason.MISSING_DOMAIN, open.getReason());
		assertEquals(0, open.getEvaluations());
		assertTrue(open.getVertices().isEmpty());
		assertTrue(closed.isSuccess());
		assertEquals(3, parameters.get(0), 0);
		assertEquals(-2, parameters.get(1), 0);
		assertEquals(3, closed.getVertices().get(0).getX(), 0);
		assertEquals(-2, last(closed).getX(), 0);
	}

	// X1-A04
	@Test
	void a04QuarterPointsDetectBulgeHiddenFromMidpointTest() {
		CurveEvaluator2D midpointFlatBulge = parameter -> point(parameter,
				parameter * (parameter - 0.5) * (parameter - 1));
		Result result = approximation.approximate(midpointFlatBulge,
				closedDomain("bulge", 0, 1), false, request(0.01));

		assertTrue(result.isSuccess());
		assertTrue(result.getSubdivisions() > 0);
		assertTrue(result.getVertices().size() > 2);
		assertEquals(0, midpointFlatBulge.evaluate(0).getPoint().getY(), 0);
		assertEquals(0, midpointFlatBulge.evaluate(0.5).getPoint().getY(), 0);
		assertEquals(0, midpointFlatBulge.evaluate(1).getPoint().getY(), 0);
		assertTrue(Math.abs(midpointFlatBulge.evaluate(0.25).getPoint().getY())
				> result.getRequestedTolerance());
	}

	// X1-A05
	@Test
	void a05SampleEvidenceIsEstimatedAndNeverPromotedToCertified() {
		Result estimated = approximation.approximate(parabola(),
				closedDomain("estimated", 0, 1), false, request(0.02));
		GeometryExportRequest certifiedOnly = GeometryExportRequest.builder(0.02)
				.allowedGuarantees(Collections.singleton(
						Guarantee.CERTIFIED_ERROR_BOUND)).build();
		Result rejected = approximation.approximate(parabola(),
				closedDomain("certified", 0, 1), false, certifiedOnly);

		assertTrue(estimated.isSuccess());
		assertEquals(Guarantee.ESTIMATED_ERROR,
				estimated.toApproximationEvidence().getGuarantee());
		assertFalse(rejected.isSuccess());
		assertEquals(Reason.TOLERANCE_NOT_ESTABLISHED, rejected.getReason());
		assertEquals(0, rejected.getEvaluations());
	}

	// X1-A06
	@Test
	void a06SuccessfulComponentRecordsToleranceAndCompleteWorkEvidence() {
		double tolerance = 0.0125;
		Result result = approximation.approximate(parabola(),
				closedDomain("evidence", -1, 1), false, request(tolerance));
		ApproximationEvidence evidence = result.toApproximationEvidence();

		assertTrue(result.isSuccess());
		assertEquals(Method.ORIENTED_DYADIC_REFINEMENT, evidence.getMethod());
		assertEquals(tolerance, evidence.getRequestedTolerance(), 0);
		assertTrue(evidence.hasAchievedError());
		assertTrue(evidence.getAchievedError() <= tolerance);
		assertEquals(result.getEvaluations(), evidence.getEvaluations());
		assertEquals(result.getSubdivisions(), evidence.getSubdivisions());
		assertEquals(result.getSegments(), evidence.getSegments());
		assertEquals(result.getVertices().size(), evidence.getVertices());
		assertEquals(result.getMaximumDepthReached(), evidence.getMaximumDepth());
	}

	// X1-A07
	@Test
	void a07EvaluationBudgetFailsBeforeAnIncompleteComponentEscapes() {
		GeometryExportRequest request = GeometryExportRequest.builder(0.01)
				.maximumEvaluations(4).build();
		Result result = approximation.approximate(parabola(),
				closedDomain("evaluation-limit", 0, 1), false, request);

		assertFalse(result.isSuccess());
		assertEquals(Reason.WORK_LIMIT, result.getReason());
		assertEquals(4, result.getEvaluations());
		assertEquals(0, result.getSegments());
		assertTrue(result.getVertices().isEmpty());
	}

	// X1-A08
	@Test
	void a08DepthBudgetRejectsUnestablishedTolerance() {
		GeometryExportRequest request = GeometryExportRequest.builder(1E-12)
				.maximumDepth(0).build();
		Result result = approximation.approximate(parabola(),
				closedDomain("depth-limit", 0, 1), false, request);

		assertFalse(result.isSuccess());
		assertEquals(Reason.WORK_LIMIT, result.getReason());
		assertEquals(0, result.getMaximumDepthReached());
		assertEquals(0, result.getSubdivisions());
		assertEquals(5, result.getEvaluations());
		assertTrue(result.getVertices().isEmpty());
	}

	// X1-A09
	@Test
	void a09PerComponentVertexBudgetRejectsWholeComponent() {
		GeometryExportRequest request = GeometryExportRequest.builder(0.1)
				.maximumVerticesPerComponent(2).build();
		Result result = approximation.approximate(parabola(),
				closedDomain("component-limit", 0, 1), false, request);

		assertFalse(result.isSuccess());
		assertEquals(Reason.WORK_LIMIT, result.getReason());
		assertTrue(result.getSubdivisions() > 0);
		assertTrue(result.getSegments() > 0);
		assertTrue(result.getVertices().isEmpty());
	}

	// X1-A10
	@Test
	void a10TotalVertexBudgetIsSharedAndCommitsOnlyCompleteComponents() {
		GeometryExportRequest request = GeometryExportRequest.builder(0.1)
				.maximumTotalVertices(3).build();
		WorkLedger ledger = new WorkLedger(request);
		Result first = approximation.approximate(line(),
				closedDomain("first", 0, 1), false, request, ledger);
		Result second = approximation.approximate(line(),
				closedDomain("second", 1, 2), false, request, ledger);

		assertTrue(first.isSuccess());
		assertEquals(2, first.getVertices().size());
		assertFalse(second.isSuccess());
		assertEquals(Reason.WORK_LIMIT, second.getReason());
		assertTrue(second.getVertices().isEmpty());
		assertEquals(2, ledger.getCommittedVertices());
		assertEquals(first.getEvaluations() + second.getEvaluations(),
				ledger.getEvaluations());
	}

	// X1-A11
	@Test
	void a11TypedInvalidNonFiniteAndDiscontinuityReasonsStayDistinct() {
		SemanticDomain domain = closedDomain("invalid", 0, 1);
		GeometryExportRequest request = request(0.1);
		Result invalid = approximation.approximate(
				parameter -> CurveEvaluation2D.invalid(Reason.UNDEFINED_SOURCE),
				domain, false, request);
		Result nonFinite = approximation.approximate(
				parameter -> CurveEvaluation2D.invalid(Reason.NON_FINITE),
				domain, false, request);
		Result discontinuity = approximation.approximate(parameter -> {
			throw new IllegalStateException("semantic evaluator discontinuity");
		}, domain, false, request);

		assertEquals(Reason.UNDEFINED_SOURCE, invalid.getReason());
		assertEquals(Reason.NON_FINITE, nonFinite.getReason());
		assertEquals(Reason.DISCONTINUITY_UNRESOLVED,
				discontinuity.getReason());
		assertTrue(invalid.getVertices().isEmpty());
		assertTrue(nonFinite.getVertices().isEmpty());
		assertTrue(discontinuity.getVertices().isEmpty());
	}

	// X1-A12
	@Test
	void a12LateTypedFailureCannotExposeEarlierAcceptedSegments() {
		CurveEvaluator2D lateFailure = parameter -> {
			if (Double.doubleToLongBits(parameter)
					== Double.doubleToLongBits(0.875)) {
				return CurveEvaluation2D.invalid(
						Reason.DISCONTINUITY_UNRESOLVED);
			}
			return point(parameter, parameter * parameter);
		};
		GeometryExportRequest request = request(0.1);
		WorkLedger ledger = new WorkLedger(request);
		Result result = approximation.approximate(lateFailure,
				closedDomain("late-failure", 0, 1), false, request, ledger);

		assertFalse(result.isSuccess());
		assertEquals(Reason.DISCONTINUITY_UNRESOLVED, result.getReason());
		assertTrue(result.getEvaluations() > 5);
		assertTrue(result.getSegments() > 0);
		assertTrue(result.getEvidenceVertices() > 0);
		assertTrue(result.getVertices().isEmpty());
		assertEquals(0, ledger.getCommittedVertices());
	}

	// X1-A13
	@Test
	void a13CoincidentEndpointsDoNotInferPolylineClosure() {
		Result result = approximation.approximate(periodicEvaluator(),
				closedDomain("periodic-shape", 0, 1), false, request(0.05));
		PolylineGeometry polyline = result.toPolylineGeometry();

		assertTrue(result.isSuccess());
		assertFalse(result.isClosed());
		assertFalse(polyline.isClosed());
		assertTrue(samePoint(result.getVertices().get(0), last(result)));

		Result constant = approximation.approximate(
				parameter -> point(2, 3), closedDomain("constant", 0, 1),
				false, request(0.05));
		assertFalse(constant.isSuccess());
		assertEquals(Reason.DEGENERATE_SOURCE, constant.getReason());
		assertTrue(constant.getVertices().isEmpty());
		assertTrue(constant.getEvidenceVertices() > 0);
	}

	// X1-A14
	@Test
	void a14ExplicitTypedFullPeriodEvidenceClosesWithoutDuplicateVertex() {
		Result result = approximation.approximate(periodicEvaluator(),
				closedDomain("certified-period", 0, 1), true, request(0.05));
		PolylineGeometry polyline = result.toPolylineGeometry();

		assertTrue(result.isSuccess());
		assertTrue(result.isClosed());
		assertTrue(polyline.isClosed());
		assertTrue(result.getVertices().size() > 2);
		assertFalse(samePoint(result.getVertices().get(0), last(result)));
		assertEquals(result.getVertices().size(), result.getSegments());

		Result nearClosure = approximation.approximate(parameter -> {
			double angle = 2 * Math.PI * parameter;
			return point(Math.cos(angle), parameter == 1 ? 0.001
					: Math.sin(angle));
		}, closedDomain("near-period", 0, 1), true, request(0.01));
		assertTrue(nearClosure.isSuccess());
		assertTrue(nearClosure.toApproximationEvidence().getAchievedError()
				>= 0.001);

		Result unestablishedClosure = approximation.approximate(parameter -> {
			double angle = 2 * Math.PI * parameter;
			return point(Math.cos(angle), parameter == 1 ? 0.009
					: Math.sin(angle));
		}, closedDomain("unestablished-period", 0, 1), true, request(0.01));
		assertFalse(unestablishedClosure.isSuccess());
		assertEquals(Reason.TOLERANCE_NOT_ESTABLISHED,
				unestablishedClosure.getReason());

		Result undersampledClosure = approximation.approximate(periodicEvaluator(),
				closedDomain("undersampled-period", 0, 1), true,
				GeometryExportRequest.builder(10).maximumDepth(1).build());
		assertFalse(undersampledClosure.isSuccess());
		assertEquals(Reason.WORK_LIMIT, undersampledClosure.getReason());
	}

	private static GeometryExportRequest request(double tolerance) {
		return GeometryExportRequest.builder(tolerance).build();
	}

	private static SemanticDomain closedDomain(String key, double start,
			double end) {
		return new SemanticDomain(key, start, end, true, true);
	}

	private static CurveEvaluator2D parabola() {
		return parameter -> point(parameter, parameter * parameter);
	}

	private static CurveEvaluator2D line() {
		return parameter -> point(parameter, 2 * parameter - 1);
	}

	private static CurveEvaluator2D periodicEvaluator() {
		return parameter -> {
			if (parameter == 0 || parameter == 1) {
				return point(1, 0);
			}
			double angle = 2 * Math.PI * parameter;
			return point(Math.cos(angle), Math.sin(angle));
		};
	}

	private static CurveEvaluation2D point(double x, double y) {
		return CurveEvaluation2D.valid(new Point2D(x, y));
	}

	private static Point2D last(Result result) {
		return result.getVertices().get(result.getVertices().size() - 1);
	}

	private static boolean samePoint(Point2D first, Point2D second) {
		return Double.doubleToLongBits(first.getX())
				== Double.doubleToLongBits(second.getX())
				&& Double.doubleToLongBits(first.getY())
						== Double.doubleToLongBits(second.getY());
	}

	private static List<String> vertexBits(Result result) {
		List<String> bits = new ArrayList<>();
		for (Point2D vertex : result.getVertices()) {
			bits.add(Long.toHexString(Double.doubleToLongBits(vertex.getX()))
					+ ":" + Long.toHexString(
							Double.doubleToLongBits(vertex.getY())));
		}
		return bits;
	}
}
