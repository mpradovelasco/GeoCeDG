/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.kernel.locus.interaction;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusCertifiedFold2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusSingularParameter2D;

/**
 * Local parametric continuation of an existing semantic address inside its certified
 * continuous component, by forward semantic evaluations only.
 *
 * <p>The argument moves continuously from the current parameter: a bounded Gauss-Newton
 * descent of the squared distance to the request, in the lifted parameter of the component.
 * Every accepted iterate is a valid semantic evaluation of the same branch and component,
 * the distance to the request never increases, and the result is proposed only when the
 * descent has converged to a well-conditioned projection within its explicit work bounds.
 * Component ends, certified folds, singular parameters, invalid or singular evaluations, a
 * reversed or collapsing tangent and an ill-conditioned projection are barriers: the
 * request is refused and nothing is proposed.</p>
 *
 * <p>This is a numerical correspondence between the request and the argument. It is not a
 * certified chart, an inverse-uniqueness proof or an incidence proof: the published point
 * is the semantic evaluation at the proposed argument, and only the choice of that argument
 * is {@code FLOATING_POINT_UNCERTIFIED}. Cartesian proximity never selects another branch,
 * component or sheet, because the argument is never chosen by a search.</p>
 */
final class LocalForwardParametricContinuation2D {

	static final String METHOD = "LOCAL_FORWARD_PARAMETRIC_CONTINUATION";

	/** Largest single correction, as a fraction of the component length. */
	private static final double STEP_FRACTION = 1.0 / 16;
	/** Initial difference step, as a fraction of the component length. */
	private static final double PROBE_FRACTION = 1.0 / 16384;
	/** Convergence and difference lengths, as fractions of the declared request radius. */
	private static final double CONVERGENCE_FRACTION = 1.0 / 1024;
	private static final double PROBE_RADIUS_FRACTION = 1.0 / 16;
	private static final int MAXIMUM_CORRECTIONS = 16;
	private static final int MAXIMUM_BACKTRACKS = 6;
	/** A squared speed below this fraction of the initial one is a singular approach. */
	private static final double SPEED_COLLAPSE = 1.0 / 64;
	/** Least second-order growth of the squared distance, relative to the squared speed. */
	private static final double CONDITIONING = 1.0 / 4;

	/** Why a request produced no proposal. */
	enum Refusal { BARRIER, BUDGET }

	/** One proposed argument; its forward binding and publication belong to the caller. */
	static final class Proposal {
		private final double parameter;
		private final long lift;
		private final int evaluations;
		private final Refusal refusal;

		private Proposal(double parameter, long lift, int evaluations, Refusal refusal) {
			this.parameter = parameter;
			this.lift = lift;
			this.evaluations = evaluations;
			this.refusal = refusal;
		}

		boolean isEstablished() { return refusal == null; }
		Refusal getRefusal() { return refusal; }
		double getParameter() { return parameter; }
		long getLift() { return lift; }
		/** @return root semantic evaluations requested by this continuation */
		int getEvaluations() { return evaluations; }
	}

	private static final class Budget extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	private final LocusDefinition2D definition;
	private final LocusBranch2D branch;
	private final ContinuousComponent component;
	private final LocusEvaluationSession2D session;
	private final LocusDriverDomainProvider2D provider;
	private final int maximumEvaluations;
	private final double x;
	private final double y;
	private int evaluations;

	private LocalForwardParametricContinuation2D(LocusDefinition2D definition, LocusBranch2D branch,
			ContinuousComponent component, double x, double y, int maximumEvaluations,
			LocusEvaluationSession2D session) {
		this.definition = definition;
		this.branch = branch;
		this.component = component;
		this.provider = definition.getProvider();
		this.x = x;
		this.y = y;
		this.maximumEvaluations = maximumEvaluations;
		this.session = session;
	}

	/**
	 * Proposes the continued argument of {@code current} for the request {@code (x, y)}.
	 *
	 * @param component the resolved certified component of the current address
	 * @param session the caller-owned bounded session; nested work is charged to it
	 * @return a proposal, or a typed refusal; never {@code null}
	 */
	static Proposal advance(LocusDefinition2D definition, LocusSemanticAddress2D current,
			ContinuousComponent component, double x, double y,
			LocusPointInteractionPolicy2D policy, LocusEvaluationSession2D session) {
		LocusBranch2D branch = definition.getBranch(current.getBranchKey());
		if (branch == null || !component.getNature().certifies()) {
			return new Proposal(Double.NaN, 0, 0, Refusal.BARRIER);
		}
		var continuation = new LocalForwardParametricContinuation2D(definition, branch, component,
				x, y, policy.getWorkBudget().getMaximumRefinementIterations(), session);
		try {
			return continuation.advance(current, policy);
		} catch (Budget | LocusEvaluationSession2D.EvaluationWorkLimitException exhausted) {
			return new Proposal(Double.NaN, 0, continuation.evaluations, Refusal.BUDGET);
		} catch (ArithmeticException | IllegalArgumentException unrepresentable) {
			return continuation.barrier();
		}
	}

	private Proposal barrier() {
		return new Proposal(Double.NaN, 0, evaluations, Refusal.BARRIER);
	}

	private Proposal advance(LocusSemanticAddress2D current, LocusPointInteractionPolicy2D policy) {
		double old = current.getCanonicalParameter();
		LocusInterval2D domain = provider.getDeclaredDomain();
		double period = provider.isPeriodic() ? domain.getUpper() - domain.getLower() : 0;
		boolean closed = period > 0 && component.isClosedPeriodic();
		double length = component.getLength();
		double radius = policy.getMaximumWorldDistance();
		if (!(length > 0) || !Double.isFinite(length) || !(radius > 0) || !Double.isFinite(radius)) {
			return barrier();
		}
		// Lifted coordinate of the component: contiguous, monotone and continuous on it.
		double start;
		double lower;
		double upper;
		if (closed) {
			start = old;
			lower = old - period / 2;
			upper = old + period / 2;
		} else {
			LocusInterval2D interval = component.getInterval();
			double coordinate = component.getArcCoordinate(old);
			if (Double.isNaN(coordinate)) {
				return barrier();
			}
			start = component.isArc() ? component.getStart() + coordinate : old;
			lower = interval.isLowerClosed() ? interval.getLower() : Math.nextUp(interval.getLower());
			upper = interval.isUpperClosed() ? interval.getUpper() : Math.nextDown(interval.getUpper());
		}
		// Certified folds and singular parameters are never crossed or entered.
		LocusExistenceStructure2D structure = branch.getExistenceStructure();
		double[] admissible = {lower, upper};
		for (LocusCertifiedFold2D fold : structure.getCertifiedFolds()) {
			admissible = exclude(admissible, start, fold.getParameterLower(),
					fold.getParameterUpper(), period);
		}
		for (LocusSingularParameter2D singular : structure.getSingularParameters()) {
			admissible = exclude(admissible, start, singular.getParameterLower(),
					singular.getParameterUpper(), period);
		}
		if (admissible == null || !(admissible[0] <= start && start <= admissible[1])) {
			return barrier();
		}
		double hardLower = admissible[0];
		double hardUpper = admissible[1];
		lower = hardLower;
		upper = hardUpper;
		double tolerance = radius * CONVERGENCE_FRACTION;
		double differenceLength = radius * PROBE_RADIUS_FRACTION;
		double maximumStep = length * STEP_FRACTION;
		double maximumProbe = length * PROBE_FRACTION * 64;
		double minimumProbe = Math.max(length * 0x1p-30,
				16 * Math.ulp(Math.max(1, Math.max(Math.abs(hardLower), Math.abs(hardUpper)))));

		double v = start;
		double[] point = evaluate(v);
		if (point == null) {
			return barrier();
		}
		double distance = squared(point);
		double probe = length * PROBE_FRACTION;
		double initialSpeed = Double.NaN;
		double previousTangentX = 0;
		double previousTangentY = 0;
		double h;
		double aheadDistance;
		double speed;
		for (int correction = 0;; correction++) {
			// One-sided difference toward the interior; its bias only slows convergence.
			h = Math.max(minimumProbe, Math.min(maximumProbe, probe));
			if (v + h > hardUpper) {
				h = -h;
			}
			double[] ahead = v + h < hardLower || v + h > hardUpper ? null : evaluate(v + h);
			if (ahead == null) {
				return barrier();
			}
			double tangentX = (ahead[0] - point[0]) / h;
			double tangentY = (ahead[1] - point[1]) / h;
			speed = tangentX * tangentX + tangentY * tangentY;
			if (!(speed > 0) || !Double.isFinite(speed)) {
				return barrier();
			}
			if (correction == 0) {
				initialSpeed = speed;
			} else if (tangentX * previousTangentX + tangentY * previousTangentY <= 0) {
				// The tangent reversed: a cusp or fold lies between two accepted arguments.
				return barrier();
			}
			if (speed < initialSpeed * SPEED_COLLAPSE) {
				return barrier();
			}
			previousTangentX = tangentX;
			previousTangentY = tangentY;
			aheadDistance = squared(ahead);
			probe = differenceLength / Math.sqrt(speed);
			double step = (tangentX * (x - point[0]) + tangentY * (y - point[1])) / speed;
			if (!Double.isFinite(step)) {
				return barrier();
			}
			if (Math.abs(step) * Math.sqrt(speed) <= tolerance) {
				break;
			}
			if (correction >= MAXIMUM_CORRECTIONS) {
				// Not a projection yet: a partial descent is never proposed as one.
				return barrier();
			}
			double target = Math.max(lower, Math.min(upper,
					v + Math.max(-maximumStep, Math.min(maximumStep, step))));
			double[] accepted = null;
			double acceptedDistance = Double.NaN;
			double candidate = target;
			for (int attempt = 0; attempt <= MAXIMUM_BACKTRACKS && candidate != v; attempt++) {
				double[] next = evaluate(candidate);
				if (next != null) {
					double nextDistance = squared(next);
					if (nextDistance < distance) {
						accepted = next;
						acceptedDistance = nextDistance;
						break;
					}
				}
				candidate = v + (candidate - v) / 2;
			}
			if (accepted == null) {
				// No admissible decrease, or an end reached: this argument is the minimizer.
				break;
			}
			v = candidate;
			point = accepted;
			distance = acceptedDistance;
		}
		// The differences above were read at the final argument. An included end of the
		// admissible interval is a constrained minimizer and needs no interior condition;
		// the half-period window of a closed component is not an end and decides nothing.
		boolean atEnd = v == hardLower || v == hardUpper;
		if (atEnd && closed && (v == start - period / 2 || v == start + period / 2)) {
			return barrier();
		}
		if (!atEnd && !conditioned(v, hardLower, hardUpper, h, aheadDistance, distance, speed,
				Math.max(minimumProbe, Math.min(maximumProbe, differenceLength / Math.sqrt(speed))))) {
			return barrier();
		}
		if (v == start) {
			return new Proposal(old, current.getPeriodicLift(), evaluations, null);
		}
		double canonical = canonical(v);
		if (!component.contains(canonical, 0)) {
			return barrier();
		}
		long lift = current.getPeriodicLift();
		if (period > 0 && (closed || component.crossesSeam())) {
			lift = Math.addExact(lift, Math.round((v - canonical) / period)
					- Math.round((start - old) / period));
		}
		return new Proposal(canonical, lift, evaluations, null);
	}

	/**
	 * Second-order regularity of the squared distance at {@code v}: the projection is a
	 * well-conditioned strict local minimum only while the request stays clear of the
	 * centre of curvature. The difference already read at {@code v} is reused when its
	 * step is large enough to dominate evaluation noise.
	 */
	private boolean conditioned(double v, double lower, double upper, double h,
			double aheadDistance, double distance, double speed, double preferred) {
		double step = Math.abs(h);
		double ahead = aheadDistance;
		if (step < preferred / 4) {
			step = preferred;
			double[] forward = v + step > upper ? null : evaluate(v + step);
			if (forward == null) {
				// Too close to an end for a two-sided reading: decided by the end itself.
				return v + step > upper;
			}
			ahead = squared(forward);
			h = step;
		}
		double behind = v - h;
		if (behind < lower || behind > upper) {
			return true;
		}
		double[] other = evaluate(behind);
		if (other == null) {
			return false;
		}
		return ahead + squared(other) - 2 * distance >= 2 * CONDITIONING * speed * step * step;
	}

	/** Removes the enclosure of one barrier parameter from the admissible interval. */
	private static double[] exclude(double[] admissible, double start,
			double barrierLower, double barrierUpper, double period) {
		if (admissible == null) {
			return null;
		}
		double lower = admissible[0];
		double upper = admissible[1];
		for (int turn = -1; turn <= 1; turn++) {
			if (turn != 0 && !(period > 0)) {
				continue;
			}
			double low = barrierLower + turn * period;
			double high = barrierUpper + turn * period;
			if (low <= start && start <= high) {
				return null;
			}
			if (high < start) {
				lower = Math.max(lower, Math.nextUp(high));
			} else {
				upper = Math.min(upper, Math.nextDown(low));
			}
		}
		return new double[] {lower, upper};
	}

	private double canonical(double lifted) {
		// The provider's modular arithmetic may need its normal stabilization pass.
		return provider.canonicalize(provider.canonicalize(lifted));
	}

	/** @return the valid regular semantic image of a lifted argument, or {@code null} */
	private double[] evaluate(double lifted) {
		double canonical = canonical(lifted);
		if (!Double.isFinite(canonical) || !component.contains(canonical, 0)) {
			return null;
		}
		if (evaluations >= maximumEvaluations) {
			throw new Budget();
		}
		evaluations++;
		LocusEvaluation2D evaluation = definition.evaluate(branch.getBranchKey(), canonical, session);
		if (session.isWorkLimitExhausted()) {
			throw new Budget();
		}
		LocusPoint2D point = evaluation.isValid() ? evaluation.getPoint() : null;
		if (point == null || evaluation.getRegularity() == Regularity.SINGULAR
				|| !Double.isFinite(point.getX()) || !Double.isFinite(point.getY())) {
			return null;
		}
		return new double[] {point.getX(), point.getY()};
	}

	private double squared(double[] point) {
		double dx = point[0] - x;
		double dy = point[1] - y;
		return dx * dx + dy * dy;
	}
}
