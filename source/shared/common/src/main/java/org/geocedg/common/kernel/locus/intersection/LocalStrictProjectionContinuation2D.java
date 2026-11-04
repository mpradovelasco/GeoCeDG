/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.kernel.locus.intersection;

import java.util.HashSet;
import java.util.Set;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D.ContinuationJet;
import org.geocedg.common.kernel.locus.intersection.LocalContinuationEvidence2D.Work;

/** R3 optional local projection proof; never a global inverse-search result. */
final class LocalStrictProjectionContinuation2D {

	static final String METHOD = "LOCAL_STRICT_PROJECTION_CHART";
	private static final int MAXIMUM_PROOF_DEPTH = 6;
	private static final int MAXIMUM_REQUEST_DEPTH = 24;
	private static final int MAXIMUM_CHART_TRIALS = 12;

	/** Immutable accepted target/root correspondence, installed only after publication. */
	static final class State {
		final double x;
		final double y;
		final double parameter;
		final long lift;
		final SplineOutwardInterval2D root;
		final String selection;
		final double parameterError;
		final SplineOutwardInterval2D[] target;

		private State(double x, double y, double parameter, long lift,
				SplineOutwardInterval2D root, String selection, double parameterError,
				SplineOutwardInterval2D[] target) {
			this.x = x;
			this.y = y;
			this.parameter = parameter;
			this.lift = lift;
			this.root = root;
			this.selection = selection;
			this.parameterError = parameterError;
			this.target = target.clone();
		}
	}

	static final class Established {
		final State state;
		final double lower;
		final double upper;
		final double maximumParameterChange;

		private Established(State state, double lower, double upper, double maximumParameterChange) {
			this.state = state;
			this.lower = lower;
			this.upper = upper;
			this.maximumParameterChange = maximumParameterChange;
		}
	}

	private LocalStrictProjectionContinuation2D() {
	}

	static Established establish(LocusDefinition2D definition,
			LocalContinuationModel2D model, LocusSemanticAddress2D current,
			ContinuousComponent component, double x, double y,
			LocusPointInteractionPolicy2D policy, State previous, Work work) {
		if (!component.getNature().certifies() || previous != null
				&& (bits(previous.parameter) != bits(current.getCanonicalParameter())
						|| previous.lift != current.getPeriodicLift())) {
			return null;
		}
		try {
			LiftedDomain domain = new LiftedDomain(definition, model.program(), component, current);
			SplineOutwardInterval2D prior = domain.align(previous == null
					? p(current.getCanonicalParameter()) : previous.root);
			ContinuationJet anchor = model.jet(prior, domain.periodic, true, work);
			if (!valid(anchor, true) || previous != null
					&& !previous.selection.equals(anchor.outcome.selection)) {
				return null;
			}
			SplineOutwardInterval2D[] q0 = previous == null ? anchor.value
						: previous.target;
			return new Chain(definition, model, component, policy, work, q0,
					new SplineOutwardInterval2D[] {p(x), p(y)})
					.advance(current, previous, 0, 1, 0);
		} catch (ArithmeticException | IllegalArgumentException exception) {
			// Nonfinite enclosures and unrepresentable canonical/lift changes refuse proof.
		}
		return null;
	}

	/** All internal targets enclose the same original affine request path. No publication. */
	private static final class Chain {
		private final LocusDefinition2D definition;
		private final LocalContinuationModel2D model;
		private final ContinuousComponent component;
		private final LocusPointInteractionPolicy2D policy;
		private final Work work;
		private final SplineOutwardInterval2D[] from, to;

		private Chain(LocusDefinition2D definition, LocalContinuationModel2D model,
				ContinuousComponent component, LocusPointInteractionPolicy2D policy,
				Work work, SplineOutwardInterval2D[] from, SplineOutwardInterval2D[] to) {
			this.definition = definition; this.model = model; this.component = component;
			this.policy = policy; this.work = work; this.from = from; this.to = to;
		}

		private Established advance(LocusSemanticAddress2D current, State previous,
				double start, double end, int depth) {
			work.charge();
			work.chartAttempt();
			Established whole = step(definition, model, current, component, at(start), at(end),
					policy, previous, work);
			if (whole != null) { work.certifiedChart(); return whole; }
			if (depth >= Math.min(MAXIMUM_REQUEST_DEPTH,
					policy.getWorkBudget().getMaximumRefinementIterations())) { return null; }
			double middle = start / 2 + end / 2;
			if (middle == start || middle == end) { return null; }
			work.charge();
			work.requestSubdivision();
			Established left = advance(current, previous, start, middle, depth + 1);
			if (left == null) { return null; }
			State next = left.state;
			var seam = !definition.getProvider().isPeriodic()
					? LocusSemanticAddress2D.SeamSide.NOT_PERIODIC
					: next.parameter == definition.getProvider().getDeclaredDomain().getLower()
							? next.lift == 0 ? LocusSemanticAddress2D.SeamSide.LOWER_APPROACH
									: LocusSemanticAddress2D.SeamSide.UPPER_APPROACH
							: LocusSemanticAddress2D.SeamSide.INTERIOR;
			LocusSemanticAddress2D internal = new LocusSemanticAddress2D(current.getSourceLocusId(),
					current.getProviderVersion(), current.getBranchKey(), current.getComponentLineageKey(),
					next.parameter, next.lift, seam);
			Established right = advance(internal, next, middle, end, depth + 1);
			if (right == null) { return null; }
			double bound = Math.nextUp(left.maximumParameterChange + right.maximumParameterChange);
			return Double.isFinite(bound)
					? new Established(right.state, right.lower, right.upper, bound) : null;
		}

		private SplineOutwardInterval2D[] at(double fraction) {
			if (fraction == 0) { return from; }
			if (fraction == 1) { return to; }
			SplineOutwardInterval2D s = p(fraction), complement = p(1).subtract(s);
			return new SplineOutwardInterval2D[] {
					from[0].multiply(complement).add(to[0].multiply(s)),
					from[1].multiply(complement).add(to[1].multiply(s))};
		}
	}

	private static Established step(LocusDefinition2D definition, LocalContinuationModel2D model,
			LocusSemanticAddress2D current, ContinuousComponent component,
			SplineOutwardInterval2D[] q0, SplineOutwardInterval2D[] q1,
			LocusPointInteractionPolicy2D policy, State previous, Work work) {
		try {
			LiftedDomain domain = new LiftedDomain(definition, model.program(), component, current);
			SplineOutwardInterval2D prior = domain.align(previous == null
					? p(current.getCanonicalParameter()) : previous.root);
			ContinuationJet anchor = model.jet(prior, domain.periodic, true, work);
			work.correspondence();
			if (!valid(anchor, true) || previous != null
					&& !previous.selection.equals(anchor.outcome.selection)) { return null; }
			String selection = anchor.outcome.selection;
			SplineOutwardInterval2D anchorSpeed = square(anchor.first[0]).add(square(anchor.first[1]));
			// No chart containing this root can use the strict provider if this necessary
			// enclosure cannot establish gu>0. Subdivide the request, not a doomed chart.
			if (!(anchorSpeed.lower > 0) || !(gu(anchor, q0, anchorSpeed).lower > 0)
					|| !(gu(anchor, q1, anchorSpeed).lower > 0)) { return null; }
			Set<String> attempted = new HashSet<>();
			double length = domain.upper - domain.lower;
			for (int attempt = 0;
					attempt < Math.min(MAXIMUM_CHART_TRIALS,
							policy.getWorkBudget().getMaximumRefinementIterations()); attempt++) {
				int exponent = attempt == 0 ? 0
						: (attempt % 2 == 1 ? (attempt + 1) / 2 : -attempt / 2);
				double radius = Math.scalb(length / 16, exponent);
				double lower = Math.max(domain.lower, prior.lower - radius);
				double upper = Math.min(domain.upper, prior.upper + radius);
				if (!(lower < upper) || lower > prior.lower || upper < prior.upper
						|| !attempted.add(Double.toHexString(lower) + ":" + Double.toHexString(upper))) {
					continue;
				}
				SplineOutwardInterval2D chart = new SplineOutwardInterval2D(lower, upper);
				boolean leftEnd = lower == domain.lower && domain.leftEnd;
				boolean rightEnd = upper == domain.upper && domain.rightEnd;
				ContinuationJet left = model.jet(p(lower), domain.periodic, false, work);
				ContinuationJet right = model.jet(p(upper), domain.periodic, false, work);
				if (!sameSelection(left, selection) || !sameSelection(right, selection)
						|| !leftEnd && !(g(left, q0).upper < 0 && g(left, q1).upper < 0)
						|| !rightEnd && !(g(right, q0).lower > 0 && g(right, q1).lower > 0)) {
					continue;
				}
				Proof proof = prove(model, chart, domain.periodic, q0, q1, selection, work, 0);
				if (proof == null) { continue; }
				// The same equation, selector and prior root are certified in this chart.
				if (previous != null && sameTarget(previous.target, q1)) {
					return new Established(previous, lower, upper, 0);
				}
				SplineOutwardInterval2D root = isolate(model, chart, domain.periodic,
						q1, proof.mu, selection, leftEnd, rightEnd,
						g(left, q1), g(right, q1), policy, work);
				if (root == null) {
					continue;
				}
				State state = canonicalState(definition, current, domain, root, q1,
						selection, policy.getParameterTolerance());
				if (state != null) {
					SplineOutwardInterval2D priorError = domain.align(
							p(current.getCanonicalParameter())).subtract(prior);
					SplineOutwardInterval2D dx = q1[0].subtract(q0[0]);
					SplineOutwardInterval2D dy = q1[1].subtract(q0[1]);
					double displacement = Math.nextUp(Math.sqrt(square(dx).add(square(dy)).upper));
					double sensitivity = Math.nextUp(proof.maximumSpeed / proof.mu.lower);
					double error = Math.nextUp(magnitude(priorError) + state.parameterError);
					double bound = Math.nextUp(Math.nextUp(sensitivity * displacement) + error);
					if (Double.isFinite(bound)) {
						return new Established(state, lower, upper, bound);
					}
				}
			}
		} catch (ArithmeticException | IllegalArgumentException exception) {
			// Nonfinite enclosures and unrepresentable canonical/lift changes refuse proof.
		}
		return null;
	}

	private static boolean sameTarget(SplineOutwardInterval2D[] a, SplineOutwardInterval2D[] b) {
		return bits(a[0].lower) == bits(b[0].lower) && bits(a[0].upper) == bits(b[0].upper)
				&& bits(a[1].lower) == bits(b[1].lower) && bits(a[1].upper) == bits(b[1].upper);
	}

	/** The local equation is strictly increasing for the entire target segment. */
	private static Proof prove(LocalContinuationModel2D model, SplineOutwardInterval2D box,
			boolean periodic, SplineOutwardInterval2D[] q0,
			SplineOutwardInterval2D[] q1, String selection, Work work, int depth) {
		ContinuationJet jet = model.jet(box, periodic, true, work);
		if (valid(jet, true) && !selection.equals(jet.outcome.selection)) {
			return null;
		}
		if (valid(jet, true)) {
			SplineOutwardInterval2D speed = square(jet.first[0]).add(square(jet.first[1]));
			SplineOutwardInterval2D mu = gu(jet, q0, speed).hull(gu(jet, q1, speed));
			if (speed.lower > 0 && mu.lower > 0) {
				return new Proof(mu, Math.nextUp(Math.sqrt(speed.upper)));
			}
			if (speed.upper == 0 || mu.upper <= 0) {
				return null;
			}
		} else if (jet.outcome.kind == ConstructionBoxOutcome2D.Kind.UNIFORMLY_INVALID) {
			return null;
		}
		if (depth >= MAXIMUM_PROOF_DEPTH) {
			return null;
		}
		double middle = box.midpoint();
		if (middle == box.lower || middle == box.upper) {
			return null;
		}
		Proof left = prove(model, new SplineOutwardInterval2D(box.lower, middle),
				periodic, q0, q1, selection, work, depth + 1);
		if (left == null) {
			return null;
		}
		Proof right = prove(model, new SplineOutwardInterval2D(middle, box.upper),
				periodic, q0, q1, selection, work, depth + 1);
		return right == null ? null : new Proof(left.mu.hull(right.mu),
				Math.max(left.maximumSpeed, right.maximumSpeed));
	}

	/** Monotone root isolation, with clipping only at genuine included domain ends. */
	private static SplineOutwardInterval2D isolate(LocalContinuationModel2D model,
			SplineOutwardInterval2D chart, boolean periodic, SplineOutwardInterval2D[] target,
			SplineOutwardInterval2D mu, String selection, boolean leftEnd, boolean rightEnd,
			SplineOutwardInterval2D leftValue, SplineOutwardInterval2D rightValue,
			LocusPointInteractionPolicy2D policy, Work work) {
		if (leftEnd && leftValue.lower >= 0) {
			return p(chart.lower);
		}
		if (rightEnd && rightValue.upper <= 0) {
			return p(chart.upper);
		}
		double lower = chart.lower;
		double upper = chart.upper;
		for (int iteration = 0;
				iteration < policy.getWorkBudget().getMaximumRefinementIterations(); iteration++) {
			if (Math.nextUp(upper - lower) <= policy.getParameterTolerance() / 4) {
				return new SplineOutwardInterval2D(lower, upper);
			}
			double middle = lower / 2 + upper / 2;
			if (middle == lower || middle == upper) {
				return null;
			}
			ContinuationJet jet = model.jet(p(middle), periodic, false, work);
			if (!sameSelection(jet, selection)) {
				return null;
			}
			SplineOutwardInterval2D value = g(jet, target);
			if (value.upper < 0) {
				lower = middle;
			} else if (value.lower > 0) {
				upper = middle;
			} else {
				SplineOutwardInterval2D improved = p(middle).subtract(value.divide(mu));
				double low = leftEnd ? Math.max(chart.lower, improved.lower) : improved.lower;
				double high = rightEnd ? Math.min(chart.upper, improved.upper) : improved.upper;
				if (leftEnd && high < chart.lower) {
					low = high = chart.lower;
				}
				if (rightEnd && low > chart.upper) {
					low = high = chart.upper;
				}
				low = Math.max(lower, low);
				high = Math.min(upper, high);
				if (low > high || low == lower && high == upper) {
					return null;
				}
				lower = low;
				upper = high;
			}
		}
		return null;
	}

	private static State canonicalState(LocusDefinition2D definition,
			LocusSemanticAddress2D current, LiftedDomain domain, SplineOutwardInterval2D root,
			SplineOutwardInterval2D[] target, String selection, double tolerance) {
		double represented = root.midpoint();
		// The provider's modular arithmetic may need its normal stabilization pass.
		// The complete reduction (both passes) is bounded against canonicalRoot below.
		double firstCanonical = definition.getProvider().canonicalize(represented);
		double canonical = definition.getProvider().canonicalize(firstCanonical);
		if (bits(canonical) != bits(definition.getProvider().canonicalize(canonical))) {
			return null;
		}
		long turn = domain.periodic ? Math.round((represented - canonical) / domain.period) : 0;
		if (turn < -1 || turn > 1 || !domain.periodic && bits(canonical) != bits(represented)) {
			return null;
		}
		SplineOutwardInterval2D canonicalRoot = shift(root, -turn);
		SplineOutwardInterval2D error = p(canonical).subtract(canonicalRoot);
		if (magnitude(error) > tolerance
				|| !domain.component.contains(canonical, 0)) {
			return null;
		}
		long lift = Math.addExact(current.getPeriodicLift(), turn - domain.oldTurn);
		return new State(target[0].midpoint(), target[1].midpoint(), canonical, lift,
				canonicalRoot, selection, magnitude(error), target);
	}

	private static boolean valid(ContinuationJet jet, boolean second) {
		return jet.outcome.kind == ConstructionBoxOutcome2D.Kind.VALID
				&& jet.value != null && jet.first != null && (!second || jet.second != null);
	}

	private static boolean sameSelection(ContinuationJet jet, String selection) {
		return valid(jet, false) && selection.equals(jet.outcome.selection);
	}

	private static SplineOutwardInterval2D g(ContinuationJet jet,
			SplineOutwardInterval2D[] target) {
		return jet.value[0].subtract(target[0]).multiply(jet.first[0])
				.add(jet.value[1].subtract(target[1]).multiply(jet.first[1]));
	}

	private static SplineOutwardInterval2D gu(ContinuationJet jet,
			SplineOutwardInterval2D[] target, SplineOutwardInterval2D speed) {
		return speed.add(jet.value[0].subtract(target[0]).multiply(jet.second[0]))
				.add(jet.value[1].subtract(target[1]).multiply(jet.second[1]));
	}

	private static SplineOutwardInterval2D square(SplineOutwardInterval2D value) {
		double high = Math.max(Math.abs(value.lower), Math.abs(value.upper));
		double low = value.containsZero() ? 0
				: Math.min(Math.abs(value.lower), Math.abs(value.upper));
		return new SplineOutwardInterval2D(Math.max(0, Math.nextDown(low * low)),
				Math.nextUp(high * high));
	}

	/** Exact-real circle correspondence, including its binary64 seam discrepancy. */
	private static SplineOutwardInterval2D shift(SplineOutwardInterval2D value, long turns) {
		if (turns == 0) {
			return value;
		}
		SplineOutwardInterval2D period = new SplineOutwardInterval2D(
				2 * Math.PI, 2 * Math.nextUp(Math.PI));
		return value.add(period.multiply(p(turns)));
	}

	private static SplineOutwardInterval2D p(double value) {
		return SplineOutwardInterval2D.point(value);
	}

	private static long bits(double value) {
		return Double.doubleToLongBits(value);
	}

	private static double magnitude(SplineOutwardInterval2D value) {
		return Math.max(Math.abs(value.lower), Math.abs(value.upper));
	}

	private static final class Proof {
		private final SplineOutwardInterval2D mu;
		private final double maximumSpeed;

		private Proof(SplineOutwardInterval2D mu, double maximumSpeed) {
			this.mu = mu;
			this.maximumSpeed = maximumSpeed;
		}
	}

	/** One component, lifted across its certified circle seam rather than split into pieces. */
	private static final class LiftedDomain {
		private final ContinuousComponent component;
		private final boolean periodic;
		private final double period;
		private final long oldTurn;
		private final double lower;
		private final double upper;
		private final boolean leftEnd;
		private final boolean rightEnd;

		private LiftedDomain(LocusDefinition2D definition,
				CertifiedConstructionProgram2D program, ContinuousComponent component,
				LocusSemanticAddress2D current) {
			this.component = component;
			// The typed component is the existing certified correspondence (§10.8).
			// A periodic provider flag alone neither closes a component nor joins pieces.
			periodic = definition.getProvider().isPeriodic() && component.getNature().certifies()
					&& program.getDriver() == CertifiedConstructionProgram2D.Driver.CIRCLE
					&& (component.isClosedPeriodic() || component.crossesSeam());
			LocusInterval2D canonical = definition.getProvider().getDeclaredDomain();
			period = canonical.getUpper() - canonical.getLower();
			double old = current.getCanonicalParameter();
			oldTurn = periodic && component.crossesSeam() && old < component.getStart() ? 1 : 0;
			LocusInterval2D interval = component.getInterval();
			if (component.isClosedPeriodic()) {
				lower = old - period / 2;
				upper = old + period / 2;
				leftEnd = false;
				rightEnd = false;
			} else {
				lower = interval.isLowerClosed() ? interval.getLower() : Math.nextUp(interval.getLower());
				upper = interval.isUpperClosed() ? interval.getUpper() : Math.nextDown(interval.getUpper());
				leftEnd = realEnd(component.getLowerBoundary());
				rightEnd = realEnd(component.getUpperBoundary());
			}
		}

		private SplineOutwardInterval2D align(SplineOutwardInterval2D value) {
			return shift(value, oldTurn);
		}

		private static boolean realEnd(LocusBoundary2D boundary) {
			return boundary.getKind() == LocusBoundary2D.Kind.DOMAIN_END
					&& boundary.isBinaryEndIncluded() && !boundary.hasEnclosure();
		}
	}
}
