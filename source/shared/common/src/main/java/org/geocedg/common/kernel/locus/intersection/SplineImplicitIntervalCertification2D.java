/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.math.BigDecimal;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSimilarityEvaluator2D;
import org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D;
import org.geocedg.common.kernel.spline.SplineSemanticEvaluator2D;

/**
 * Query-local scalar certificates for the captured structural spline and target.
 * Floating composed coefficients supply proposals only. Every admitted simple
 * root has a strict scalar Krawczyk inclusion or an exact boundary-zero witness,
 * together with a nonzero derivative enclosure for Q(S(u)), evaluated through
 * the actual structural coefficient intervals.
 * This neither enumerates all roots nor proves multiplicity of rich contacts.
 */
final class SplineImplicitIntervalCertification2D {

	static final String WORK_PREFIX = "STRUCTURAL_IMPLICIT_CERTIFICATION_WORK";
	private final SplineIntervalModel2D source;
	private final double[][] target;
	private final LocusIntersectionWorkBudget2D budget;
	private int candidates;
	private int exclusions;
	private int attempts;
	private int successes;
	private int derivativeFailures;
	private int subdivisions;
	private int unresolved;
	private int workLimitHits;
	private int valueEvaluations;
	private int derivativeEvaluations;
	private int boundaryWitnessChecks;
	private int exactBoundaryZeros;

	enum Status { EXCLUDED, SIMPLE, UNRESOLVED }

	/** Current evidence only: no field participates in durable root identity. */
	static final class Proof {
		final Status status;
		final SplineOutwardInterval2D neighborhood;
		final SplineOutwardInterval2D root;
		final SplineOutwardInterval2D derivative;
		final double parameter;
		final boolean compatibleContact;
		final SplineOutwardInterval2D canonicalRoot;
		private final double period;

		Proof(Status status, SplineOutwardInterval2D neighborhood,
				SplineOutwardInterval2D root, SplineOutwardInterval2D derivative,
				double parameter, boolean compatibleContact) {
			this(status, neighborhood, root, derivative, parameter, compatibleContact,
					root, 0);
		}

		private Proof(Status status, SplineOutwardInterval2D neighborhood,
				SplineOutwardInterval2D root, SplineOutwardInterval2D derivative,
				double parameter, boolean compatibleContact,
				SplineOutwardInterval2D canonicalRoot, double period) {
			this.status = status;
			this.neighborhood = neighborhood;
			this.root = root;
			this.derivative = derivative;
			this.parameter = parameter;
			this.compatibleContact = compatibleContact;
			this.canonicalRoot = canonicalRoot;
			this.period = period;
		}

		boolean sameCertifiedRoot(Proof other) {
			if (status != Status.SIMPLE || other.status != Status.SIMPLE) {
				return false;
			}
			if (neighborhood.contains(other.root) || other.neighborhood.contains(root)
					|| root.lower == root.upper && other.root.lower == other.root.upper
							&& parameter == other.parameter) {
				return true;
			}
			if (period > 0 && period == other.period) {
				for (int cycle : new int[] {-1, 1}) {
					SplineOutwardInterval2D shift = SplineOutwardInterval2D.point(cycle * period);
					if (neighborhood.contains(other.root.subtract(shift))
							|| other.neighborhood.contains(root.subtract(shift))) {
						return true;
					}
				}
			}
			return false;
		}
	}

	private SplineImplicitIntervalCertification2D(SplineIntervalModel2D source,
			double[][] target, LocusIntersectionWorkBudget2D budget) {
		this.source = source;
		this.target = new double[target.length][];
		for (int row = 0; row < target.length; row++) {
			this.target[row] = target[row].clone();
		}
		this.budget = budget;
	}

	static SplineImplicitIntervalCertification2D capture(LocusDefinition2D definition,
			String branch, double[][] target, LocusIntersectionWorkBudget2D budget) {
		SplineIntervalModel2D source = SplineIntervalModel2D.capture(definition, branch);
		return source == null || !source.isStructural() ? null
				: new SplineImplicitIntervalCertification2D(source, target, budget);
	}

	static boolean isSplineSource(LocusDefinition2D definition) {
		LocusDefinition2D source = definition;
		int depth = 0;
		while (source.getEvaluatorCapability() instanceof LocusSimilarityEvaluator2D) {
			if (++depth > PiecewisePolynomialLocus2D.MAXIMUM_SAFE_COMPOSITION_DEPTH) {
				throw new ArithmeticException("Spline capture exceeded composition policy");
			}
			source = ((LocusSimilarityEvaluator2D) source.getEvaluatorCapability())
					.getCapturedSourceDefinition();
		}
		return source.getEvaluatorCapability() instanceof SplineSemanticEvaluator2D;
	}

	Proof verify(LocusInterval2D component, double parameter,
			double proposedLower, double proposedUpper) {
		candidates++;
		double period = source.period(component);
		double lower = component.getLower() - (period > 0 ? period : 0);
		double upper = component.getUpper() + (period > 0 ? period : 0);
		double center = Math.max(lower, Math.min(upper, parameter));
		double radius = Math.max((component.getUpper() - component.getLower()) / 32,
				Math.max(Math.abs(center - proposedLower), Math.abs(proposedUpper - center)));
		Proof last = null;
		Proof contact = null;
		for (int depth = 0; depth <= budget.getMaximumIsolationDepth(); depth++) {
			if (attempts >= budget.getMaximumIsolationSubdivisions()) {
				workLimitHits++;
				break;
			}
			double a = Math.max(lower, Math.nextDown(center - radius));
			double b = Math.min(upper, Math.nextUp(center + radius));
			if (!(a < b)) {
				workLimitHits++;
				break;
			}
			SplineOutwardInterval2D interval = new SplineOutwardInterval2D(a, b);
			try {
				last = attempt(interval, center, component);
				if (last.compatibleContact && contact == null) {
					contact = last;
				}
				if (last.status == Status.SIMPLE) {
					successes++;
					return last;
				}
				if (last.status == Status.EXCLUDED) {
					if (contact != null) {
						// A smaller proposal window may exclude an approximate stationary
						// representative without excluding its earlier rich-contact region.
						unresolved++;
						return contact;
					}
					exclusions++;
					return last;
				}
			} catch (ArithmeticException exception) {
				// Overflow or failed enclosure is missing proof, never exclusion.
				last = null;
			}
			radius /= 2;
			subdivisions++;
			if (depth == budget.getMaximumIsolationDepth()) {
				workLimitHits++;
			}
		}
		unresolved++;
		if (contact != null) {
			return contact;
		}
		if (last != null) {
			return last;
		}
		return new Proof(Status.UNRESOLVED, null, null, null, center, false);
	}

	private Proof attempt(SplineOutwardInterval2D interval, double center,
			LocusInterval2D component) {
		attempts++;
		if (!source.isSmooth(interval)) {
			return new Proof(Status.UNRESOLVED, interval, null, null, center, false);
		}
		SplineOutwardInterval2D value = value(interval);
		SplineOutwardInterval2D derivative = derivative(interval);
		if (!value.containsZero()) {
			return new Proof(Status.EXCLUDED, interval, null, derivative, center, false);
		}
		if (derivative.containsZero()) {
			derivativeFailures++;
			return new Proof(Status.UNRESOLVED, interval, null, derivative, center, true);
		}
		// A proven exact boundary zero also gives one-sided existence. This is
		// not numerical snapping of a nearby root onto a knot or periodic seam.
		for (double boundary : source.getKnots()) {
			if (interval.lower <= boundary && boundary <= interval.upper) {
				SplineOutwardInterval2D atBoundary = value(SplineOutwardInterval2D.point(boundary));
				if (atBoundary.lower == 0 && atBoundary.upper == 0
						|| hasExactBoundaryZero(boundary)) {
					return simple(interval, SplineOutwardInterval2D.point(boundary),
							derivative, boundary, component);
				}
			}
		}
		SplineOutwardInterval2D image = krawczyk(interval, center, derivative);
		if (interval.strictlyContains(image)) {
			SplineOutwardInterval2D root = image;
			for (int step = 0; step < Math.min(8,
					budget.getMaximumRefinementIterations()); step++) {
				SplineOutwardInterval2D next = krawczyk(root, root.midpoint(), derivative(root));
				SplineOutwardInterval2D intersection = root.intersection(next);
				if (intersection == null) {
					throw new ArithmeticException("Inconsistent scalar root enclosure");
				}
				if (intersection.lower == root.lower && intersection.upper == root.upper) {
					break;
				}
				root = intersection;
			}
			return simple(interval, root, derivative, root.midpoint(), component);
		}
		if (interval.disjoint(image)) {
			return new Proof(Status.EXCLUDED, interval, null, derivative, center, false);
		}
		return new Proof(Status.UNRESOLVED, interval, null, derivative, center, false);
	}

	private Proof simple(SplineOutwardInterval2D interval, SplineOutwardInterval2D root,
			SplineOutwardInterval2D derivative, double parameter, LocusInterval2D component) {
		double canonical = source.canonical(parameter);
		if (!component.contains(canonical, 0)) {
			return new Proof(Status.UNRESOLVED, interval, root, derivative, parameter, false);
		}
		double period = source.period(component);
		SplineOutwardInterval2D canonicalRoot = root;
		if (canonical < root.lower || canonical > root.upper) {
			canonicalRoot = null;
			for (int cycle : new int[] {-1, 1}) {
				SplineOutwardInterval2D shifted = root.subtract(
						SplineOutwardInterval2D.point(cycle * period));
				if (shifted.lower <= canonical && canonical <= shifted.upper) {
					canonicalRoot = shifted;
					break;
				}
			}
			if (canonicalRoot == null || period == 0) {
				return new Proof(Status.UNRESOLVED, interval, root, derivative, parameter, false);
			}
		}
		return new Proof(Status.SIMPLE, interval, root, derivative, canonical, false,
				canonicalRoot, period);
	}

	private SplineOutwardInterval2D krawczyk(SplineOutwardInterval2D interval,
			double center, SplineOutwardInterval2D derivative) {
		SplineOutwardInterval2D c = SplineOutwardInterval2D.point(1 / derivative.midpoint());
		SplineOutwardInterval2D point = SplineOutwardInterval2D.point(center);
		return point.subtract(c.multiply(value(point))).add(
				SplineOutwardInterval2D.point(1).subtract(c.multiply(derivative))
						.multiply(interval.subtract(point)));
	}

	SplineOutwardInterval2D value(SplineOutwardInterval2D parameter) {
		valueEvaluations++;
		return polynomial(target, evaluate(parameter, false), 0);
	}

	/** Exact native knot incidence, not isolation, transversality or multiplicity. */
	boolean hasExactBoundaryZero(double parameter) {
		boundaryWitnessChecks++;
		BigDecimal[] point = source.exactUntransformedBoundary(parameter);
		if (point == null) {
			return false;
		}
		BigDecimal result = BigDecimal.ZERO;
		for (int x = 0; x < target.length; x++) {
			for (int y = 0; y < target[x].length; y++) {
				if (target[x][y] != 0) {
					result = result.add(new BigDecimal(target[x][y])
							.multiply(point[0].pow(x)).multiply(point[1].pow(y)));
				}
			}
		}
		boolean zero = result.signum() == 0;
		if (zero) {
			exactBoundaryZeros++;
		}
		return zero;
	}

	SplineOutwardInterval2D derivative(SplineOutwardInterval2D parameter) {
		derivativeEvaluations++;
		SplineOutwardInterval2D[] point = evaluate(parameter, false);
		SplineOutwardInterval2D[] tangent = evaluate(parameter, true);
		return polynomial(target, point, 1).multiply(tangent[0])
				.add(polynomial(target, point, 2).multiply(tangent[1]));
	}

	private SplineOutwardInterval2D[] evaluate(SplineOutwardInterval2D parameter,
			boolean derivative) {
		return parameter.lower == parameter.upper
				? source.evaluatePoint(parameter.lower, derivative)
				: source.evaluate(parameter, derivative);
	}

	private static SplineOutwardInterval2D polynomial(double[][] coefficients,
			SplineOutwardInterval2D[] point, int derivativeCoordinate) {
		SplineOutwardInterval2D result = SplineOutwardInterval2D.point(0);
		for (int x = 0; x < coefficients.length; x++) {
			for (int y = 0; y < coefficients[x].length; y++) {
				double coefficient = coefficients[x][y];
				int dx = derivativeCoordinate == 1 ? 1 : 0;
				int dy = derivativeCoordinate == 2 ? 1 : 0;
				if (coefficient == 0 || x < dx || y < dy) {
					continue;
				}
				SplineOutwardInterval2D term = SplineOutwardInterval2D.point(coefficient);
				if (dx + dy != 0) {
					term = term.multiply(SplineOutwardInterval2D.point(dx == 1 ? x : y));
				}
				for (int power = 0; power < x - dx; power++) {
					term = term.multiply(point[0]);
				}
				for (int power = 0; power < y - dy; power++) {
					term = term.multiply(point[1]);
				}
				result = result.add(term);
			}
		}
		return result;
	}

	String workSummary() {
		return WORK_PREFIX + " floatingCandidates=" + candidates
				+ "; certifiedExclusions=" + exclusions + "; simpleAttempts=" + attempts
				+ "; simpleSuccesses=" + successes + "; derivativeFailures=" + derivativeFailures
				+ "; intervalSubdivisions=" + subdivisions + "; precisionEscalations=0"
				+ "; unresolvedCandidates=" + unresolved + "; workLimitHits=" + workLimitHits
				+ "; valueEvaluations=" + valueEvaluations
				+ "; derivativeEvaluations=" + derivativeEvaluations
				+ "; boundaryWitnessChecks=" + boundaryWitnessChecks
				+ "; exactBoundaryZeros=" + exactBoundaryZeros;
	}
}
