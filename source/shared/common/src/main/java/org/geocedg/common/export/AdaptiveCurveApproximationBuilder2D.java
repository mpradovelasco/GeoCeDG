/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.export.ApproximationEvidence.Guarantee;
import org.geocedg.common.export.ApproximationEvidence.Method;
import org.geocedg.common.export.GeometryExportModel.Point2D;
import org.geocedg.common.export.GeometryExportModel.PolylineGeometry;
import org.geocedg.common.export.GeometryExportRequest.SemanticDomain;
import org.geocedg.common.export.SourceExportOutcome.Reason;

/**
 * Export-only deterministic adaptive approximation in model coordinates.
 * The builder owns no construction object, dependency, renderer, viewport, or
 * persistence state.
 */
public final class AdaptiveCurveApproximationBuilder2D {

	/** Read-only semantic evaluator supplied by an approved source adapter. */
	@FunctionalInterface
	public interface CurveEvaluator2D {
		/** @return a typed finite point or explicit non-emittable outcome */
		CurveEvaluation2D evaluate(double semanticParameter);
	}

	/** One typed semantic evaluation without render-derived fallback. */
	public static final class CurveEvaluation2D {
		private final Point2D point;
		private final Reason reason;

		private CurveEvaluation2D(Point2D point, Reason reason) {
			this.point = point;
			this.reason = reason;
		}

		/** @return a valid finite model-coordinate evaluation */
		public static CurveEvaluation2D valid(Point2D point) {
			if (point == null) {
				throw new IllegalArgumentException("Evaluated point is required");
			}
			return new CurveEvaluation2D(point, Reason.NONE);
		}

		/** @return an explicit evaluation failure with no stale coordinate */
		public static CurveEvaluation2D invalid(Reason reason) {
			if (reason == null || reason == Reason.NONE) {
				throw new IllegalArgumentException(
						"An invalid evaluation requires a failure reason");
			}
			return new CurveEvaluation2D(null, reason);
		}

		public boolean isValid() {
			return reason == Reason.NONE;
		}

		public Point2D getPoint() {
			return point;
		}

		public Reason getReason() {
			return reason;
		}
	}

	/**
	 * Mutable count ledger shared only by component builds in one request.
	 * Limits are count-based and therefore independent of machine speed.
	 */
	public static final class WorkLedger {
		private final long maximumEvaluations;
		private final int maximumTotalVertices;
		private long evaluations;
		private int committedVertices;

		/** Creates an empty ledger carrying the request-wide limits. */
		public WorkLedger(GeometryExportRequest request) {
			if (request == null) {
				throw new IllegalArgumentException("Geometry export request is required");
			}
			maximumEvaluations = request.getMaximumEvaluations();
			maximumTotalVertices = request.getMaximumTotalVertices();
		}

		public long getEvaluations() {
			return evaluations;
		}

		public int getCommittedVertices() {
			return committedVertices;
		}

		private boolean consumeEvaluation() {
			if (evaluations >= maximumEvaluations) {
				return false;
			}
			evaluations++;
			return true;
		}

		private boolean canFitComponent(int componentVertices) {
			return componentVertices >= 0
					&& componentVertices <= maximumTotalVertices - committedVertices;
		}

		private void commitComponent(int componentVertices) {
			if (!canFitComponent(componentVertices)) {
				throw new IllegalStateException("Total vertex budget exceeded");
			}
			committedVertices += componentVertices;
		}

		private boolean carriesLimits(GeometryExportRequest request) {
			return maximumEvaluations == request.getMaximumEvaluations()
					&& maximumTotalVertices == request.getMaximumTotalVertices();
		}
	}

	/** Immutable all-or-nothing result for one semantic component. */
	public static final class Result {
		private final List<Point2D> vertices;
		private final boolean closed;
		private final Reason reason;
		private final double requestedTolerance;
		private final Double achievedEstimate;
		private final long evaluations;
		private final long subdivisions;
		private final long segments;
		private final long evidenceVertices;
		private final int maximumDepthReached;

		private Result(List<Point2D> vertices, boolean closed, Reason reason,
				double requestedTolerance, Double achievedEstimate, long evaluations,
				long subdivisions, long segments, long evidenceVertices,
				int maximumDepthReached) {
			this.vertices = Collections.unmodifiableList(new ArrayList<>(vertices));
			this.closed = closed;
			this.reason = reason;
			this.requestedTolerance = requestedTolerance;
			this.achievedEstimate = achievedEstimate;
			this.evaluations = evaluations;
			this.subdivisions = subdivisions;
			this.segments = segments;
			this.evidenceVertices = evidenceVertices;
			this.maximumDepthReached = maximumDepthReached;
		}

		public boolean isSuccess() {
			return reason == Reason.NONE;
		}

		/** @return vertices, or an empty list for every failure */
		public List<Point2D> getVertices() {
			return vertices;
		}

		public boolean isClosed() {
			return closed;
		}

		public Reason getReason() {
			return reason;
		}

		public double getRequestedTolerance() {
			return requestedTolerance;
		}

		public Double getAchievedEstimate() {
			return achievedEstimate;
		}

		public long getEvaluations() {
			return evaluations;
		}

		public long getSubdivisions() {
			return subdivisions;
		}

		public long getSegments() {
			return segments;
		}

		/** @return vertices examined or emitted for deterministic work evidence */
		public long getEvidenceVertices() {
			return evidenceVertices;
		}

		public int getMaximumDepthReached() {
			return maximumDepthReached;
		}

		/** @return neutral geometry for a complete successful component */
		public PolylineGeometry toPolylineGeometry() {
			if (!isSuccess()) {
				throw new IllegalStateException(
						"A failed approximation has no polyline geometry");
			}
			return new PolylineGeometry(vertices, closed);
		}

		/** @return complete deterministic approximation/work evidence */
		public ApproximationEvidence toApproximationEvidence() {
			return new ApproximationEvidence(Method.ORIENTED_DYADIC_REFINEMENT,
					requestedTolerance, achievedEstimate, Guarantee.ESTIMATED_ERROR,
					evaluations, subdivisions, segments, evidenceVertices,
					maximumDepthReached);
		}
	}

	/**
	 * Approximate one component with a fresh request-wide ledger.
	 *
	 * @param evaluator semantic evaluator for the captured source revision
	 * @param domain explicit oriented closed subdomain
	 * @param semanticClosure true only with external typed full-period evidence
	 * @param request immutable export policy
	 * @return all-or-nothing component result
	 */
	public Result approximate(CurveEvaluator2D evaluator, SemanticDomain domain,
			boolean semanticClosure, GeometryExportRequest request) {
		return approximate(evaluator, domain, semanticClosure, request,
				new WorkLedger(request));
	}

	/**
	 * Approximate one component while sharing request-wide count budgets.
	 * Closure is consumed as caller-supplied semantic evidence and is never
	 * inferred from endpoint proximity.
	 * @return all-or-nothing component result
	 */
	public Result approximate(CurveEvaluator2D evaluator, SemanticDomain domain,
			boolean semanticClosure, GeometryExportRequest request,
			WorkLedger ledger) {
		if (evaluator == null || request == null || ledger == null) {
			throw new IllegalArgumentException(
					"Evaluator, request, and work ledger are required");
		}
		if (!ledger.carriesLimits(request)) {
			throw new IllegalArgumentException(
					"Work ledger does not carry this request's limits");
		}
		if (!request.isApproximationAllowed()) {
			return failure(request, Reason.UNSUPPORTED_FAMILY, 0, 0, 0);
		}
		if (!request.allowsGuarantee(Guarantee.ESTIMATED_ERROR)) {
			return failure(request, Reason.TOLERANCE_NOT_ESTABLISHED, 0, 0, 0);
		}
		if (domain == null || !domain.isStartClosed() || !domain.isEndClosed()) {
			return failure(request, Reason.MISSING_DOMAIN, 0, 0, 0);
		}

		State state = new State(evaluator, request, ledger,
				semanticClosure ? 2 : 0);
		double start = domain.getStartParameter();
		double end = domain.getEndParameter();
		Point2D startPoint = state.evaluate(start);
		Point2D endPoint = state.evaluate(end);
		double midpointParameter = midpoint(start, end);
		Point2D midpoint = state.evaluate(midpointParameter);
		if (state.hasFailed()) {
			return state.failureResult();
		}
		state.addFirst(startPoint);
		state.refine(start, end, startPoint, endPoint, midpoint, 0);
		if (state.hasFailed()) {
			return state.failureResult();
		}
		if (!state.hasDistinctCoordinates()) {
			state.fail(Reason.DEGENERATE_SOURCE);
			return state.failureResult();
		}
		if (semanticClosure) {
			double closureResidual = distance(startPoint, endPoint);
			if (closureResidual > request.getRequestedTolerance()) {
				state.fail(Reason.DISCONTINUITY_UNRESOLVED);
				return state.failureResult();
			}
			if (!state.includeClosureResidual(closureResidual)) {
				state.fail(Reason.TOLERANCE_NOT_ESTABLISHED);
				return state.failureResult();
			}
			if (!state.hasMinimumClosedCoordinates()) {
				state.fail(Reason.DEGENERATE_SOURCE);
				return state.failureResult();
			}
		}
		return state.successResult(semanticClosure);
	}

	private static Result failure(GeometryExportRequest request, Reason reason,
			long evaluations, long subdivisions, int maximumDepth) {
		return new Result(Collections.emptyList(), false, reason,
				request.getRequestedTolerance(), null, evaluations, subdivisions, 0, 0,
				maximumDepth);
	}

	private static double midpoint(double first, double second) {
		return first * 0.5 + second * 0.5;
	}

	private static double quarter(double first, double second) {
		return first * 0.75 + second * 0.25;
	}

	private static double threeQuarter(double first, double second) {
		return first * 0.25 + second * 0.75;
	}

	private static double distance(Point2D first, Point2D second) {
		return Math.hypot(first.getX() - second.getX(),
				first.getY() - second.getY());
	}

	private static double chordDeviation(Point2D point, Point2D start,
			Point2D end) {
		double dx = end.getX() - start.getX();
		double dy = end.getY() - start.getY();
		double denominator = dx * dx + dy * dy;
		if (denominator == 0) {
			return Math.hypot(point.getX() - start.getX(),
					point.getY() - start.getY());
		}
		if (!Double.isFinite(denominator)) {
			return Double.NaN;
		}
		double projection = ((point.getX() - start.getX()) * dx
				+ (point.getY() - start.getY()) * dy) / denominator;
		double clamped = Math.max(0, Math.min(1, projection));
		double nearestX = start.getX() + clamped * dx;
		double nearestY = start.getY() + clamped * dy;
		return Math.hypot(point.getX() - nearestX, point.getY() - nearestY);
	}

	private static final class State {
		private final CurveEvaluator2D evaluator;
		private final GeometryExportRequest request;
		private final WorkLedger ledger;
		private final int minimumDepth;
		private final List<Point2D> vertices = new ArrayList<>();
		private Reason reason = Reason.NONE;
		private long evaluations;
		private long subdivisions;
		private long segments;
		private int maximumDepthReached;
		private double maximumEstimatedDeviation;

		private State(CurveEvaluator2D evaluator, GeometryExportRequest request,
				WorkLedger ledger, int minimumDepth) {
			this.evaluator = evaluator;
			this.request = request;
			this.ledger = ledger;
			this.minimumDepth = minimumDepth;
		}

		private Point2D evaluate(double parameter) {
			if (hasFailed()) {
				return null;
			}
			if (!Double.isFinite(parameter)) {
				fail(Reason.NON_FINITE);
				return null;
			}
			if (!ledger.consumeEvaluation()) {
				fail(Reason.WORK_LIMIT);
				return null;
			}
			evaluations++;
			CurveEvaluation2D evaluation;
			try {
				evaluation = evaluator.evaluate(parameter);
			} catch (RuntimeException exception) {
				fail(Reason.DISCONTINUITY_UNRESOLVED);
				return null;
			}
			if (evaluation == null) {
				fail(Reason.DISCONTINUITY_UNRESOLVED);
				return null;
			}
			if (!evaluation.isValid()) {
				fail(evaluation.getReason());
				return null;
			}
			return evaluation.getPoint();
		}

		private void addFirst(Point2D point) {
			if (!canAddVertex()) {
				fail(Reason.WORK_LIMIT);
				return;
			}
			vertices.add(point);
		}

		private void refine(double startParameter, double endParameter,
				Point2D start, Point2D end, Point2D midpoint, int depth) {
			if (hasFailed()) {
				return;
			}
			maximumDepthReached = Math.max(maximumDepthReached, depth);
			double firstQuarterParameter = quarter(startParameter, endParameter);
			double thirdQuarterParameter = threeQuarter(startParameter, endParameter);
			Point2D firstQuarter = evaluate(firstQuarterParameter);
			Point2D thirdQuarter = evaluate(thirdQuarterParameter);
			if (hasFailed()) {
				return;
			}

			double deviation = Math.max(chordDeviation(firstQuarter, start, end),
					Math.max(chordDeviation(midpoint, start, end),
							chordDeviation(thirdQuarter, start, end)));
			if (!Double.isFinite(deviation)) {
				fail(Reason.NON_FINITE);
				return;
			}
			if (deviation <= request.getRequestedTolerance()
					&& depth >= minimumDepth) {
				if (!canAddVertex()) {
					fail(Reason.WORK_LIMIT);
					return;
				}
				vertices.add(end);
				segments++;
				maximumEstimatedDeviation = Math.max(maximumEstimatedDeviation,
						deviation);
				return;
			}
			if (depth >= request.getMaximumDepth()) {
				fail(Reason.WORK_LIMIT);
				return;
			}

			subdivisions++;
			double midpointParameter = midpoint(startParameter, endParameter);
			refine(startParameter, midpointParameter, start, midpoint,
					firstQuarter, depth + 1);
			refine(midpointParameter, endParameter, midpoint, end,
					thirdQuarter, depth + 1);
		}

		private boolean canAddVertex() {
			int candidate = vertices.size() + 1;
			return candidate <= request.getMaximumVerticesPerComponent()
					&& ledger.canFitComponent(candidate);
		}

		private boolean hasFailed() {
			return reason != Reason.NONE;
		}

		private boolean hasDistinctCoordinates() {
			if (vertices.size() < 2) {
				return false;
			}
			Point2D first = vertices.get(0);
			long firstX = Double.doubleToLongBits(first.getX());
			long firstY = Double.doubleToLongBits(first.getY());
			for (int index = 1; index < vertices.size(); index++) {
				Point2D candidate = vertices.get(index);
				if (Double.doubleToLongBits(candidate.getX()) != firstX
						|| Double.doubleToLongBits(candidate.getY()) != firstY) {
					return true;
				}
			}
			return false;
		}

		private boolean includeClosureResidual(double residual) {
			double combined = maximumEstimatedDeviation + residual;
			if (!Double.isFinite(combined)
					|| combined > request.getRequestedTolerance()) {
				return false;
			}
			maximumEstimatedDeviation = combined;
			return true;
		}

		private boolean hasMinimumClosedCoordinates() {
			if (vertices.size() < 4) {
				return false;
			}
			List<String> coordinates = new ArrayList<>();
			for (int index = 0; index < vertices.size() - 1; index++) {
				Point2D vertex = vertices.get(index);
				String bits = Long.toHexString(Double.doubleToLongBits(vertex.getX()))
						+ ":" + Long.toHexString(
								Double.doubleToLongBits(vertex.getY()));
				if (!coordinates.contains(bits)) {
					coordinates.add(bits);
					if (coordinates.size() == 3) {
						return true;
					}
				}
			}
			return false;
		}

		private void fail(Reason failureReason) {
			if (!hasFailed()) {
				reason = failureReason == null || failureReason == Reason.NONE
						? Reason.DISCONTINUITY_UNRESOLVED : failureReason;
			}
		}

		private Result failureResult() {
			return new Result(Collections.emptyList(), false, reason,
					request.getRequestedTolerance(), null, evaluations, subdivisions,
					segments, vertices.size(), maximumDepthReached);
		}

		private Result successResult(boolean semanticClosure) {
			List<Point2D> emitted = new ArrayList<>(vertices);
			if (semanticClosure && emitted.size() > 2
					&& distance(emitted.get(0), emitted.get(emitted.size() - 1))
							<= request.getRequestedTolerance()) {
				emitted.remove(emitted.size() - 1);
			}
			ledger.commitComponent(emitted.size());
			return new Result(emitted, semanticClosure, Reason.NONE,
					request.getRequestedTolerance(), maximumEstimatedDeviation,
					evaluations, subdivisions, segments, emitted.size(),
					maximumDepthReached);
		}
	}
}
