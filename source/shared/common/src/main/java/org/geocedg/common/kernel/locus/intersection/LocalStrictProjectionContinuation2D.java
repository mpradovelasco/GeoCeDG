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

	/** Immutable accepted target/root correspondence, installed only after publication. */
	static final class State {
		final double x;
		final double y;
		final double parameter;
		final long lift;
		final SplineOutwardInterval2D root;
		final String selection;
		final double parameterError;

		private State(double x, double y, double parameter, long lift,
				SplineOutwardInterval2D root, String selection, double parameterError) {
			this.x = x;
			this.y = y;
			this.parameter = parameter;
			this.lift = lift;
			this.root = root;
			this.selection = selection;
			this.parameterError = parameterError;
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
			CertifiedConstructionProgram2D program, LocusSemanticAddress2D current,
			ContinuousComponent component, double x, double y,
			LocusPointInteractionPolicy2D policy, State previous, Work work) {
		if (!component.getNature().certifies() || previous != null
				&& (bits(previous.parameter) != bits(current.getCanonicalParameter())
						|| previous.lift != current.getPeriodicLift())) {
			return null;
		}
		try {
			ConstructionBoxModel2D model = new ConstructionBoxModel2D(program);
			LiftedDomain domain = new LiftedDomain(definition, program, component, current);
			SplineOutwardInterval2D prior = domain.align(previous == null
					? p(current.getCanonicalParameter()) : previous.root);
			work.charge();
			ContinuationJet anchor = model.continuationJet(prior, domain.periodic, true);
			if (!valid(anchor, true) || previous != null
					&& !previous.selection.equals(anchor.outcome.selection)) {
				return null;
			}
			// Initially q0 is the exact F(a0), enclosed here, not rounded forward output.
			SplineOutwardInterval2D[] q0 = previous == null ? anchor.value
					: new SplineOutwardInterval2D[] {p(previous.x), p(previous.y)};
			SplineOutwardInterval2D[] q1 = {p(x), p(y)};
			String selection = anchor.outcome.selection;
			Set<String> attempted = new HashSet<>();
			double length = domain.upper - domain.lower;
			for (int attempt = 0;
					attempt < policy.getWorkBudget().getMaximumRefinementIterations(); attempt++) {
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
				Proof proof = prove(model, chart, domain.periodic, q0, q1, selection, work, 0);
				if (proof == null) {
					continue;
				}
				boolean leftEnd = lower == domain.lower && domain.leftEnd;
				boolean rightEnd = upper == domain.upper && domain.rightEnd;
				work.charge();
				ContinuationJet left = model.continuationJet(p(lower), domain.periodic, false);
				work.charge();
				ContinuationJet right = model.continuationJet(p(upper), domain.periodic, false);
				if (!sameSelection(left, selection) || !sameSelection(right, selection)
						|| !leftEnd && !(g(left, q0).upper < 0 && g(left, q1).upper < 0)
						|| !rightEnd && !(g(right, q0).lower > 0 && g(right, q1).lower > 0)) {
					continue;
				}
				// The same equation, selector and prior root are certified in this chart.
				if (previous != null && bits(previous.x) == bits(x) && bits(previous.y) == bits(y)) {
					return new Established(previous, lower, upper, 0);
				}
				SplineOutwardInterval2D root = isolate(model, chart, domain.periodic,
						q1, proof.mu, selection, leftEnd, rightEnd,
						g(left, q1), g(right, q1), policy, work);
				if (root == null) {
					continue;
				}
				State state = canonicalState(definition, current, domain, root, x, y,
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

	/** The local equation is strictly increasing for the entire target segment. */
	private static Proof prove(ConstructionBoxModel2D model, SplineOutwardInterval2D box,
			boolean periodic, SplineOutwardInterval2D[] q0,
			SplineOutwardInterval2D[] q1, String selection, Work work, int depth) {
		work.charge();
		ContinuationJet jet = model.continuationJet(box, periodic, true);
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
	private static SplineOutwardInterval2D isolate(ConstructionBoxModel2D model,
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
			work.charge();
			ContinuationJet jet = model.continuationJet(p(middle), periodic, false);
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
			double x, double y, String selection, double tolerance) {
		double represented = root.midpoint();
		double canonical = definition.getProvider().canonicalize(represented);
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
		return new State(x, y, canonical, lift, canonicalRoot, selection, magnitude(error));
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
