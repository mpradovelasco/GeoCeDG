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
 * Every accepted iterate is a valid semantic evaluation of the same branch and component
 * on its own side of the component ends, the distance to the request never increases, and
 * the result is proposed only with evidence, within explicit work bounds: a stationary or
 * discretely minimizing argument with second-order evidence, or an end that the component
 * includes with the correction pointing out of it. An excluded end, certified folds,
 * singular parameters, invalid or singular evaluations, a reversed or collapsing tangent,
 * an ill-conditioned projection and a reading that cannot be taken are barriers: the
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
		private final boolean excludedEnd;

		private Proposal(double parameter, long lift, int evaluations, Refusal refusal) {
			this(parameter, lift, evaluations, refusal, false);
		}

		private Proposal(double parameter, long lift, int evaluations, Refusal refusal,
				boolean excludedEnd) {
			this.parameter = parameter;
			this.lift = lift;
			this.evaluations = evaluations;
			this.refusal = refusal;
			this.excludedEnd = excludedEnd;
		}

		/**
		 * @return whether the refusal is the descent leaving its admissible interval through
		 *         an end that the interval does not include
		 */
		boolean isAtExcludedEnd() { return excludedEnd; }

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
	/** Certified component of the argument, or {@code null} on a conditioned region. */
	private final ContinuousComponent component;
	/** Conditioned region of the argument, or {@code null} on a certified component. */
	private final LocusInterval2D region;
	private final LocusEvaluationSession2D session;
	private final LocusDriverDomainProvider2D provider;
	private final int maximumEvaluations;
	private final double x;
	private final double y;
	/** Period of the provider, or zero. */
	private final double period;
	/** Whether the component is a certified closed periodic one; never the provider's word. */
	private final boolean closed;
	private int evaluations;

	private LocalForwardParametricContinuation2D(LocusDefinition2D definition, LocusBranch2D branch,
			ContinuousComponent component, LocusInterval2D region, boolean closedRegion,
			double x, double y, int maximumEvaluations, LocusEvaluationSession2D session) {
		this.definition = definition;
		this.branch = branch;
		this.component = component;
		this.region = region;
		this.provider = definition.getProvider();
		this.x = x;
		this.y = y;
		this.maximumEvaluations = maximumEvaluations;
		this.session = session;
		LocusInterval2D domain = provider.getDeclaredDomain();
		this.period = provider.isPeriodic() ? domain.getUpper() - domain.getLower() : 0;
		this.closed = period > 0 && (component != null ? component.isClosedPeriodic()
				: closedRegion);
	}

	private double arcLength() {
		return component != null ? component.getLength()
				: region.getUpper() - region.getLower();
	}

	private LocusInterval2D arcInterval() {
		return component != null ? component.getInterval() : region;
	}

	private boolean arcContains(double canonical) {
		return component != null ? component.contains(canonical, 0)
				: region.contains(canonical, 0);
	}

	private double arcCoordinate(double canonical) {
		return component != null ? component.getArcCoordinate(canonical)
				: arcContains(canonical) ? canonical - region.getLower() : Double.NaN;
	}

	/**
	 * Proposes the continued argument of a parameter address inside its conditioned region
	 * (ADR 0042; contract {@code post-r1-conditioned-use.md}, section 8.2): the same local
	 * descent, with the region as the admissible interval. An end of the region next to a
	 * band is never included and never crossed. The continuation is declared conditioned:
	 * it assumes the hypotheses of the spans it moves along and certifies nothing.
	 *
	 * @param region admissible parameter interval; an end is closed only when it is an
	 *        included end of the declared domain
	 * @param closedRegion whether the region is the whole period of a periodic provider
	 *        with agreeing seam nodes
	 * @return a proposal, or a typed refusal; never {@code null}
	 */
	static Proposal advanceOnRegion(LocusDefinition2D definition,
			LocusSemanticAddress2D current, LocusInterval2D region, boolean closedRegion,
			double x, double y, LocusPointInteractionPolicy2D policy,
			LocusEvaluationSession2D session) {
		LocusBranch2D branch = definition.getBranch(current.getBranchKey());
		if (branch == null || region == null) {
			return new Proposal(Double.NaN, 0, 0, Refusal.BARRIER);
		}
		var continuation = new LocalForwardParametricContinuation2D(definition, branch, null,
				region, closedRegion, x, y,
				policy.getWorkBudget().getMaximumRefinementIterations(), session);
		try {
			return continuation.advance(current, policy);
		} catch (Budget | LocusEvaluationSession2D.EvaluationWorkLimitException exhausted) {
			return new Proposal(Double.NaN, 0, continuation.evaluations, Refusal.BUDGET);
		} catch (ArithmeticException | IllegalArgumentException unrepresentable) {
			return continuation.barrier();
		}
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
				null, false, x, y, policy.getWorkBudget().getMaximumRefinementIterations(),
				session);
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
		double length = arcLength();
		double radius = policy.getMaximumWorldDistance();
		if (!(length > 0) || !Double.isFinite(length) || !(radius > 0) || !Double.isFinite(radius)) {
			return barrier();
		}
		// Lifted coordinate of the component: contiguous, monotone and continuous on it.
		double start;
		double lower;
		double upper;
		// Only an end that the component includes can hold a constrained minimizer.
		boolean includedLower = false;
		boolean includedUpper = false;
		if (closed) {
			start = old;
			lower = old - period / 2;
			upper = old + period / 2;
		} else {
			LocusInterval2D interval = arcInterval();
			double coordinate = arcCoordinate(old);
			if (Double.isNaN(coordinate)) {
				return barrier();
			}
			start = component != null && component.isArc()
					? component.getStart() + coordinate : old;
			lower = interval.isLowerClosed() ? interval.getLower() : Math.nextUp(interval.getLower());
			upper = interval.isUpperClosed() ? interval.getUpper() : Math.nextDown(interval.getUpper());
			includedLower = interval.isLowerClosed();
			includedUpper = interval.isUpperClosed();
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
		// A limit set by an excluded fold or singular parameter is not an end of the component.
		includedLower &= admissible[0] == lower;
		includedUpper &= admissible[1] == upper;
		lower = admissible[0];
		upper = admissible[1];
		double tolerance = radius * CONVERGENCE_FRACTION;
		double differenceLength = radius * PROBE_RADIUS_FRACTION;
		double maximumStep = length * STEP_FRACTION;
		double maximumProbe = length * PROBE_FRACTION * 64;
		double minimumProbe = Math.max(length * 0x1p-30,
				16 * Math.ulp(Math.max(1, Math.max(Math.abs(lower), Math.abs(upper)))));

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
		// Whether the descent stopped because its correction leaves the admissible interval,
		// and whether it leaves it through an end that the component includes.
		boolean outward = false;
		boolean throughIncludedEnd = false;
		for (int correction = 0;; correction++) {
			// One-sided difference toward the interior; its bias only slows convergence.
			h = Math.max(minimumProbe, Math.min(maximumProbe, probe));
			if (v + h > upper) {
				h = -h;
			}
			double[] ahead = v + h < lower || v + h > upper ? null : evaluate(v + h);
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
			if (step > 0 ? v >= upper : v <= lower) {
				outward = true;
				throughIncludedEnd = step > 0 ? includedUpper : includedLower;
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
			// Whether the smallest trial of this correction had no valid evaluation.
			boolean unread = true;
			for (int attempt = 0; attempt <= MAXIMUM_BACKTRACKS && candidate != v; attempt++) {
				double[] next = evaluate(candidate);
				unread = next == null;
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
				if (unread) {
					// Nothing was read next to this argument: it is not known to minimize.
					return barrier();
				}
				// No strict decrease down to 1/64 of the correction: a discrete minimizer
				// along the descent direction, subject to the second-order evidence below.
				break;
			}
			v = candidate;
			point = accepted;
			distance = acceptedDistance;
		}
		// The differences above were read at the final argument.
		if (outward) {
			// A constrained minimizer, by its strict first-order condition, only at an end
			// that the component includes. The neighbour of an excluded end, the limit of an
			// excluded fold or singular parameter and the half-period window of a closed
			// component are not ends: the distance has no established minimum there.
			if (!throughIncludedEnd) {
				return new Proposal(Double.NaN, 0, evaluations, Refusal.BARRIER, true);
			}
		} else if (closed && (v <= lower || v >= upper) || !conditioned(v, lower, upper, h,
				aheadDistance, distance, speed,
				Math.max(minimumProbe, Math.min(maximumProbe, differenceLength / Math.sqrt(speed))))) {
			return barrier();
		}
		if (v == start) {
			return new Proposal(old, current.getPeriodicLift(), evaluations, null);
		}
		double canonical = coherent(v);
		if (Double.isNaN(canonical)) {
			return barrier();
		}
		long lift = current.getPeriodicLift();
		if (period > 0 && (closed || component != null && component.crossesSeam())) {
			lift = Math.addExact(lift, Math.round((v - canonical) / period)
					- Math.round((start - old) / period));
		}
		return new Proposal(canonical, lift, evaluations, null);
	}

	/**
	 * Second-order evidence at a stationary or discretely minimizing argument {@code v}: the
	 * projection is a well-conditioned strict local minimum only while the request stays
	 * clear of the centre of curvature. The second difference of the squared distance over
	 * a window of width {@code 2 s} must reach {@code 2 * CONDITIONING * |F'|^2 * s^2}; it
	 * equals {@code s^2} times the second derivative at some argument of its window. The
	 * window is centred at {@code v}; where a centred window leaves the admissible interval
	 * it starts at {@code v} and lies on its interior side. Without a window of that width
	 * in the admissible interval, or without a valid evaluation at an argument of it, there
	 * is no evidence and the answer is {@code false}: an impossible reading never admits.
	 * The difference already read at {@code v} is reused when its step is large enough to
	 * dominate evaluation noise.
	 */
	private boolean conditioned(double v, double lower, double upper, double h,
			double aheadDistance, double distance, double speed, double preferred) {
		double step = Math.abs(h);
		double direction = h < 0 ? -1 : 1;
		double read = aheadDistance;
		if (step < preferred / 4) {
			step = preferred;
			read = Double.NaN;
		}
		double second;
		if (v - step >= lower && v + step <= upper) {
			second = (Double.isNaN(read) ? distanceAt(v + direction * step) : read)
					+ distanceAt(v - direction * step) - 2 * distance;
		} else {
			double side = admits(v + 2 * direction * step, lower, upper) ? direction
					: admits(v - 2 * direction * step, lower, upper) ? -direction : 0;
			if (side == 0) {
				return false;
			}
			double first = side == direction && !Double.isNaN(read) ? read
					: distanceAt(v + side * step);
			second = distanceAt(v + 2 * side * step) - 2 * first + distance;
		}
		// A missing evaluation makes the difference NaN, which reaches no bound.
		return second >= 2 * CONDITIONING * speed * step * step;
	}

	private static boolean admits(double lifted, double lower, double upper) {
		return lifted >= lower && lifted <= upper;
	}

	/** @return the squared distance at a lifted argument, or {@code NaN} without evaluation */
	private double distanceAt(double lifted) {
		double[] point = evaluate(lifted);
		return point == null ? Double.NaN : squared(point);
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

	/**
	 * The canonical parameter that a lifted argument addresses on its own side of the
	 * component.
	 *
	 * <p>Membership of the canonical parameter is not enough on a component that is not a
	 * certified closed periodic one: the two ends of a full-length open arc share a canonical
	 * parameter, and the periodic canonicalization of the provider can round the lifted
	 * neighbour of the excluded end onto it. That parameter belongs to the component as its
	 * included end, on the other side ({@code locus-v2-existence-components.md} section
	 * 15.3). The arc coordinate of the canonical parameter must therefore be the lifted
	 * coordinate of the argument: the two differ by canonicalization rounding on the same
	 * side and by the whole length across the ends. On a certified closed periodic component
	 * the wrap is the authorized seam crossing and membership decides.</p>
	 *
	 * @return the canonical parameter, or {@code NaN} when the argument does not address a
	 *         parameter of the component on its own side
	 */
	private double coherent(double lifted) {
		double canonical = canonical(lifted);
		if (!Double.isFinite(canonical) || !arcContains(canonical)) {
			return Double.NaN;
		}
		if (closed) {
			return canonical;
		}
		double coordinate = arcCoordinate(canonical);
		return Math.abs(coordinate - (lifted - arcInterval().getLower()))
				<= arcLength() / 2 ? canonical : Double.NaN;
	}

	/**
	 * @return the valid regular semantic image of a lifted argument on its own side of the
	 *         component, or {@code null}
	 */
	private double[] evaluate(double lifted) {
		double canonical = coherent(lifted);
		if (Double.isNaN(canonical)) {
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
