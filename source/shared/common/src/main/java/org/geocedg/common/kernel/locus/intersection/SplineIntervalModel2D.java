/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSimilarityEvaluator2D;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D;
import org.geocedg.common.kernel.locus.LocusSourceSnapshot2D;
import org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D;
import org.geocedg.common.kernel.spline.SplinePolynomialModel2D;
import org.geocedg.common.kernel.spline.SplineSemanticEvaluator2D;

/** Original captured spline polynomial followed by its captured similarity maps. */
final class SplineIntervalModel2D {
	private final LocusDefinition2D definition;
	private final SplinePolynomialModel2D model;
	private final double[] knots;
	private final double[][][] coefficients;
	private final SplineOutwardInterval2D[][][] coefficientEnclosures;
	private final boolean[] smoothKnots;
	private final boolean smoothPeriodicSeam;
	private final boolean structural;
	private final List<LocusSimilarityTransform2D> transforms;

	private SplineIntervalModel2D(LocusDefinition2D definition,
			SplinePolynomialModel2D model,
			List<LocusSimilarityTransform2D> transforms) {
		this.definition = definition;
		this.model = model;
		this.transforms = transforms;
		knots = model.getKnots();
		coefficients = new double[model.getSpanCount()][2][];
		coefficientEnclosures = new SplineOutwardInterval2D[model.getSpanCount()][2][];
		structural = model.getStructuralContinuityOrder() >= 1;
		for (int span = 0; span < coefficients.length; span++) {
			for (int coordinate = 0; coordinate < 2; coordinate++) {
				coefficients[span][coordinate] = model.getCoefficients(span,
						coordinate);
				coefficientEnclosures[span][coordinate] = structural
						? enclose(model.getExactCoefficientNumerators(span, coordinate),
								model.getCoefficientDenominator())
						: points(coefficients[span][coordinate]);
			}
		}
		smoothKnots = new boolean[knots.length];
		for (int knot = 1; knot + 1 < knots.length; knot++) {
			smoothKnots[knot] = structural || equalJet(knot - 1, knots[knot], knot,
					knots[knot]);
		}
		smoothPeriodicSeam = definition.getProvider().isPeriodic()
				&& model.isClosed() && (structural || equalJet(coefficients.length - 1,
						knots[knots.length - 1], 0, knots[0]));
	}

	static SplineIntervalModel2D capture(LocusDefinition2D definition,
			String branch) {
		LocusDefinition2D current = definition;
		ArrayList<LocusSimilarityTransform2D> transforms = new ArrayList<>();
		while (current.getEvaluatorCapability()
				instanceof LocusSimilarityEvaluator2D) {
			if (transforms.size()
					>= PiecewisePolynomialLocus2D.MAXIMUM_SAFE_COMPOSITION_DEPTH) {
				return null;
			}
			LocusSimilarityEvaluator2D similarity =
					(LocusSimilarityEvaluator2D) current.getEvaluatorCapability();
			if (similarity.getCapturedTransform().isCollapsed()) {
				return null;
			}
			transforms.add(similarity.getCapturedTransform());
			current = similarity.getCapturedSourceDefinition();
		}
		if (!(current.getEvaluatorCapability() instanceof SplineSemanticEvaluator2D)) {
			return null;
		}
		SplineSemanticEvaluator2D evaluator =
				(SplineSemanticEvaluator2D) current.getEvaluatorCapability();
		evaluator.getPolynomialSpanCount(branch);
		return new SplineIntervalModel2D(definition, evaluator.getModel(), transforms);
	}

	double[] getKnots() {
		return knots.clone();
	}

	boolean isStructural() {
		return structural;
	}

	/** Exact native knot value only; no rounded span or transformed surrogate. */
	BigDecimal[] exactUntransformedBoundary(double parameter) {
		int knot = java.util.Arrays.binarySearch(knots, parameter);
		if (!structural || !transforms.isEmpty() || knot < 0) {
			return null;
		}
		int span = Math.min(knot, coefficients.length - 1);
		BigDecimal argument = new BigDecimal(parameter);
		BigDecimal denominator = BigDecimal.valueOf(model.getCoefficientDenominator());
		BigDecimal[] result = new BigDecimal[2];
		for (int coordinate = 0; coordinate < 2; coordinate++) {
			BigDecimal value = BigDecimal.ZERO;
			for (BigDecimal coefficient : model.getExactCoefficientNumerators(span, coordinate)) {
				value = value.multiply(argument).add(coefficient);
			}
			try {
				result[coordinate] = value.divide(denominator);
			} catch (ArithmeticException exception) {
				// A nonterminating rational is not replaced by a rounded exact witness.
				return null;
			}
		}
		return result;
	}

	SplineOutwardInterval2D[] evaluatePoint(double parameter, boolean derivative) {
		if (parameter < knots[0] || parameter > knots[knots.length - 1]) {
			// A rounded periodic reduction is not an enclosure of the real lifted
			// parameter. Retain outward interval subtraction for lifted points.
			return evaluate(SplineOutwardInterval2D.point(parameter), derivative);
		}
		double value = smoothPeriodicSeam && parameter == knots[knots.length - 1]
				? knots[0] : parameter;
		int span = java.util.Arrays.binarySearch(knots, value);
		span = span >= 0 ? Math.min(span, coefficients.length - 1) : -span - 2;
		SplineOutwardInterval2D point = SplineOutwardInterval2D.point(value);
		SplineOutwardInterval2D[] result = pair(
				polynomial(coefficientEnclosures[span][0], point, derivative),
				polynomial(coefficientEnclosures[span][1], point, derivative));
		for (int map = transforms.size() - 1; map >= 0; map--) {
			result = transform(transforms.get(map), result, derivative);
		}
		return result;
	}

	double period(LocusInterval2D component) {
		return smoothPeriodicSeam && component.getLower() == knots[0]
				&& component.getUpper() == knots[knots.length - 1]
				? component.getUpper() - component.getLower() : 0;
	}

	double canonical(double parameter) {
		return definition.getProvider().canonicalize(parameter);
	}

	boolean isSmooth(SplineOutwardInterval2D parameter) {
		if (parameter.lower < knots[0]
				|| parameter.upper > knots[knots.length - 1]) {
			if (!smoothPeriodicSeam) {
				return false;
			}
		}
		double period = knots[knots.length - 1] - knots[0];
		int minimumCycle = smoothPeriodicSeam ? -1 : 0;
		int maximumCycle = smoothPeriodicSeam ? 1 : 0;
		for (int cycle = minimumCycle; cycle <= maximumCycle; cycle++) {
			SplineOutwardInterval2D local = parameter.subtract(
					SplineOutwardInterval2D.point(cycle * period));
			for (int knot = 0; knot < knots.length; knot++) {
				double value = knots[knot];
				if (local.lower <= value && value <= local.upper) {
					boolean seam = knot == 0 || knot == knots.length - 1;
					if (seam) {
						if (local.lower < value && local.upper > value
								&& !smoothPeriodicSeam) {
							return false;
						}
					} else if (!smoothKnots[knot]) {
						return false;
					}
				}
			}
		}
		return true;
	}

	SplineOutwardInterval2D[] evaluate(SplineOutwardInterval2D parameter,
			boolean derivative) {
		SplineOutwardInterval2D[] result = null;
		double period = knots[knots.length - 1] - knots[0];
		int minimumCycle = smoothPeriodicSeam ? -1 : 0;
		int maximumCycle = smoothPeriodicSeam ? 1 : 0;
		for (int cycle = minimumCycle; cycle <= maximumCycle; cycle++) {
			SplineOutwardInterval2D local = parameter.subtract(
					SplineOutwardInterval2D.point(cycle * period));
			for (int span = 0; span < coefficients.length; span++) {
				SplineOutwardInterval2D intersection = local.intersection(
						new SplineOutwardInterval2D(knots[span], knots[span + 1]));
				if (intersection == null) {
					continue;
				}
				SplineOutwardInterval2D[] value = new SplineOutwardInterval2D[] {
						polynomial(coefficientEnclosures[span][0], intersection, derivative),
						polynomial(coefficientEnclosures[span][1], intersection, derivative)};
				result = result == null ? value : new SplineOutwardInterval2D[] {
						result[0].hull(value[0]), result[1].hull(value[1])};
			}
		}
		if (result == null || !covered(parameter)) {
			throw new ArithmeticException("Parameter proof is outside spline charts");
		}
		for (int map = transforms.size() - 1; map >= 0; map--) {
			result = transform(transforms.get(map), result, derivative);
		}
		return result;
	}

	private boolean covered(SplineOutwardInterval2D parameter) {
		double period = knots[knots.length - 1] - knots[0];
		double lower = knots[0] - (smoothPeriodicSeam ? period : 0);
		double upper = knots[knots.length - 1] + (smoothPeriodicSeam ? period : 0);
		return parameter.lower >= lower && parameter.upper <= upper;
	}

	static SplineOutwardInterval2D polynomial(double[] coefficients,
			SplineOutwardInterval2D parameter, boolean derivative) {
		return polynomial(points(coefficients), parameter, derivative);
	}

	private static SplineOutwardInterval2D polynomial(
			SplineOutwardInterval2D[] coefficients,
			SplineOutwardInterval2D parameter, boolean derivative) {
		SplineOutwardInterval2D value = SplineOutwardInterval2D.point(0);
		int degree = coefficients.length - 1;
		int count = derivative ? degree : coefficients.length;
		for (int index = 0; index < count; index++) {
			SplineOutwardInterval2D coefficient = coefficients[index];
			if (derivative) {
				coefficient = coefficient.multiply(
						SplineOutwardInterval2D.point(degree - index));
			}
			value = value.multiply(parameter).add(coefficient);
		}
		return value;
	}

	private static SplineOutwardInterval2D[] points(double[] coefficients) {
		SplineOutwardInterval2D[] result = new SplineOutwardInterval2D[coefficients.length];
		for (int index = 0; index < result.length; index++) {
			result[index] = SplineOutwardInterval2D.point(coefficients[index]);
		}
		return result;
	}

	private static SplineOutwardInterval2D[] enclose(BigDecimal[] numerators,
			int denominator) {
		SplineOutwardInterval2D[] result = new SplineOutwardInterval2D[numerators.length];
		for (int index = 0; index < result.length; index++) {
			result[index] = encloseCoefficient(numerators[index], denominator);
		}
		return result;
	}

	/**
	 * The decimal quotient is only a seed. Exact cross-multiplication proves both
	 * binary64 bounds against the structural numerator/denominator; neither the
	 * quotient precision nor a floating span approximation is proof authority.
	 */
	static SplineOutwardInterval2D encloseCoefficient(BigDecimal numerator,
			int denominator) {
		if (denominator <= 0) {
			throw new ArithmeticException("Nonpositive structural denominator");
		}
		BigDecimal divisor = BigDecimal.valueOf(denominator);
		double seed = numerator.divide(divisor, MathContext.DECIMAL128).doubleValue();
		if (!Double.isFinite(seed)) {
			throw new ArithmeticException("Nonfinite structural coefficient");
		}
		int comparison = new BigDecimal(seed).multiply(divisor).compareTo(numerator);
		double lower = comparison > 0 ? Math.nextDown(seed) : seed;
		double upper = comparison < 0 ? Math.nextUp(seed) : seed;
		SplineOutwardInterval2D result = new SplineOutwardInterval2D(lower, upper);
		if (new BigDecimal(lower).multiply(divisor).compareTo(numerator) > 0
				|| new BigDecimal(upper).multiply(divisor).compareTo(numerator) < 0) {
			throw new ArithmeticException("Structural coefficient enclosure not established");
		}
		return result;
	}

	private boolean equalJet(int first, double firstParameter, int second,
			double secondParameter) {
		for (int coordinate = 0; coordinate < 2; coordinate++) {
			for (int order = 0; order <= 1; order++) {
				if (exactEvaluation(coefficients[first][coordinate], firstParameter,
						order).compareTo(exactEvaluation(coefficients[second][coordinate],
								secondParameter, order)) != 0) {
					return false;
				}
			}
		}
		return true;
	}

	private static BigDecimal exactEvaluation(double[] coefficients,
			double parameter, int order) {
		BigDecimal value = BigDecimal.ZERO;
		BigDecimal x = new BigDecimal(parameter);
		int degree = coefficients.length - 1;
		for (int index = 0; index < coefficients.length - order; index++) {
			BigDecimal coefficient = new BigDecimal(coefficients[index]);
			if (order == 1) {
				coefficient = coefficient.multiply(BigDecimal.valueOf(degree - index));
			}
			value = value.multiply(x).add(coefficient);
		}
		return value;
	}

	private static SplineOutwardInterval2D[] transform(
			LocusSimilarityTransform2D map, SplineOutwardInterval2D[] source,
			boolean derivative) {
		SplineOutwardInterval2D x = source[0];
		SplineOutwardInterval2D y = source[1];
		LocusSourceSnapshot2D values = map.getCapturedValues();
		SplineOutwardInterval2D a = SplineOutwardInterval2D.point(values.get(0));
		SplineOutwardInterval2D b = SplineOutwardInterval2D.point(values.get(1));
		SplineOutwardInterval2D two = SplineOutwardInterval2D.point(2);
		switch (map.getKind()) {
		case TRANSLATION:
			return derivative ? source : pair(x.add(a), y.add(b));
		case POINT_REFLECTION:
			return derivative ? pair(x.negate(), y.negate())
					: pair(two.multiply(a).subtract(x), two.multiply(b).subtract(y));
		case ROTATION:
			SplineOutwardInterval2D c = SplineOutwardInterval2D.point(values.get(2));
			if (!derivative && values.get(0) == 0) {
				return source;
			}
			SplineOutwardInterval2D cosine =
					SplineOutwardInterval2D.point(Math.cos(values.get(0)));
			SplineOutwardInterval2D sine =
					SplineOutwardInterval2D.point(Math.sin(values.get(0)));
			SplineOutwardInterval2D centeredX = derivative ? x : x.subtract(b);
			SplineOutwardInterval2D centeredY = derivative ? y : y.subtract(c);
			SplineOutwardInterval2D imageX = cosine.multiply(centeredX)
					.subtract(sine.multiply(centeredY));
			SplineOutwardInterval2D imageY = sine.multiply(centeredX)
					.add(cosine.multiply(centeredY));
			return derivative ? pair(imageX, imageY)
					: pair(b.add(imageX), c.add(imageY));
		case LINE_REFLECTION:
			SplineOutwardInterval2D normal = a.multiply(x).add(b.multiply(y));
			if (!derivative) {
				normal = normal.add(SplineOutwardInterval2D.point(values.get(2)));
			}
			return pair(x.subtract(two.multiply(a).multiply(normal)),
					y.subtract(two.multiply(b).multiply(normal)));
		case DILATION:
			if (derivative) {
				return pair(a.multiply(x), a.multiply(y));
			}
			if (values.get(0) == 1) {
				return source;
			}
			SplineOutwardInterval2D centerY =
					SplineOutwardInterval2D.point(values.get(2));
			return pair(b.add(a.multiply(x.subtract(b))),
					centerY.add(a.multiply(y.subtract(centerY))));
		default:
			throw new ArithmeticException("No captured similarity proof map");
		}
	}

	private static SplineOutwardInterval2D[] pair(SplineOutwardInterval2D x,
			SplineOutwardInterval2D y) {
		return new SplineOutwardInterval2D[] {x, y};
	}
}
