/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.locus.CertifiedAffineLocus2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.OperandKind;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;

/**
 * Structural metric identities of class 1 ({@code locus-v2-metrics.md} section 25.1):
 * an affine image {@code |a| * delta_u}, and a similarity image, with driver-independent
 * parameters, of a circle, circular-arc or segment driver.
 *
 * <p>The identity is read from provider-owned certificate material only: the
 * certified affine coefficients or the captured construction program of the
 * certified construction class, in their exact-real semantics. The speed of the
 * semantic curve is then enclosed, for every parameter, by outward bounds; the
 * length of {@code [s, e]} lies between {@code speed_lower * (e - s)} and
 * {@code speed_upper * (e - s)}. No sample is used as authority.</p>
 */
final class StructuralLocusMetric2D {
	private final MetricOutwardInterval2D speed;
	private final String method;

	private StructuralLocusMetric2D(MetricOutwardInterval2D speed, String method) {
		this.speed = speed;
		this.method = method;
	}

	/**
	 * @return the structural identity of one branch component, or {@code null}
	 *         when the provider exposes none
	 */
	static StructuralLocusMetric2D capture(LocusDefinition2D root, String branchKey,
			LocusInterval2D component) {
		Object capability = root.getEvaluatorCapability();
		try {
			if (capability instanceof CertifiedAffineLocus2D
					&& ((CertifiedAffineLocus2D) capability)
							.supportsCertifiedAffine(root)) {
				CertifiedAffineLocus2D affine = (CertifiedAffineLocus2D) capability;
				double[] x = affine.getCertifiedAffineCoefficients(branchKey, 0);
				double[] y = affine.getCertifiedAffineCoefficients(branchKey, 1);
				if (x != null && y != null && x.length == 2 && y.length == 2) {
					return new StructuralLocusMetric2D(MetricLinearPart2D.norm(
							MetricOutwardInterval2D.point(x[0]),
							MetricOutwardInterval2D.point(y[0])),
							"structural affine image |a|*du of the certified affine "
									+ "branch");
				}
			}
			if (capability instanceof ReconstructibleLocusEvaluator2D
					&& SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY
							.equals(branchKey)) {
				Optional<CertifiedConstructionProgram2D> program =
						((ReconstructibleLocusEvaluator2D) capability)
								.captureCertifiedProgram();
				if (program.isPresent()) {
					return fromProgram(program.get(), component);
				}
			}
		} catch (IllegalArgumentException | ArithmeticException exception) {
			// Certificate material that cannot be enclosed rigorously gives no
			// structural identity; the next capability applies (fail closed).
			return null;
		}
		return null;
	}

	/** @return outward enclosure of the semantic length of {@code [from, to]} */
	MetricEnclosure2D enclose(double from, double to) {
		double low = Math.min(from, to);
		double high = Math.max(from, to);
		if (low == high) {
			return new MetricEnclosure2D(0, 0, method);
		}
		double widthLower = MetricOutwardArithmetic2D.nonNegativeDown(
				MetricOutwardArithmetic2D.subDown(high, low));
		double widthUpper = MetricOutwardArithmetic2D.subUp(high, low);
		double lower = speed.lower <= 0 || widthLower == 0 ? 0
				: MetricOutwardArithmetic2D.nonNegativeDown(
						MetricOutwardArithmetic2D.mulDown(speed.lower, widthLower));
		double upper = speed.upper <= 0 ? 0
				: MetricOutwardArithmetic2D.mulUp(speed.upper, widthUpper);
		return new MetricEnclosure2D(lower, Math.max(lower, upper), method);
	}

	String getMethod() {
		return method;
	}

	private static StructuralLocusMetric2D fromProgram(
			CertifiedConstructionProgram2D program, LocusInterval2D component) {
		LocusInterval2D domain = program.getDomain();
		if (component.getLower() < domain.getLower()
				|| component.getUpper() > domain.getUpper()) {
			return null;
		}
		int[] similaritySteps = new int[1];
		MetricLinearPart2D linear = pathLinearPart(program, similaritySteps);
		if (linear == null) {
			return null;
		}
		MetricOutwardInterval2D speed;
		switch (program.getDriver()) {
		case SCALAR_AFFINE:
			speed = norm(linear.apply(constant(program, 0), constant(program, 2)));
			break;
		case SEGMENT:
			speed = norm(linear.apply(constant(program, 3),
					constant(program, 2).negate()));
			break;
		case CIRCLE:
			speed = linear.multiply(circleDerivativeMatrix(program))
					.singularValueBounds();
			break;
		case CIRCULAR_ARC:
			speed = linear.multiply(circleDerivativeMatrix(program))
					.singularValueBounds().multiply(MetricOutwardInterval2D
							.point(Math.abs(program.getDriverConstant(9))));
			break;
		default:
			return null;
		}
		return new StructuralLocusMetric2D(speed, "structural "
				+ program.getDriver() + " driver under " + similaritySteps[0]
				+ " similarity step(s) of " + program.getVersion());
	}

	/**
	 * Follows the dependency path from the program output back to the driver.
	 * Every step on it must be a point similarity of exactly one earlier point
	 * node with driver-independent captured parameters.
	 *
	 * @return linear part of the composed map, or {@code null}
	 */
	private static MetricLinearPart2D pathLinearPart(
			CertifiedConstructionProgram2D program, int[] count) {
		List<Step> steps = program.getSteps();
		MetricLinearPart2D product = MetricLinearPart2D.IDENTITY;
		int node = steps.size();
		while (node > 0) {
			Step step = steps.get(node - 1);
			int source = -1;
			boolean constantPoint = false;
			for (Operand operand : step.getOperands()) {
				if (operand.getKind() == OperandKind.NODE) {
					if (source >= 0) {
						return null;
					}
					source = operand.getNode();
				} else if (operand.getKind() == OperandKind.POINT) {
					constantPoint = true;
				} else {
					return null;
				}
			}
			if (source < 0 || !program.isPointNode(source)) {
				return null;
			}
			MetricLinearPart2D part = linearPart(step, constantPoint);
			if (part == null) {
				return null;
			}
			// Walking backwards meets the outermost map first.
			product = product.multiply(part);
			node = source;
			count[0]++;
		}
		return product;
	}

	/**
	 * Exact-real linear part of one point similarity, in the formulas of the
	 * certified construction model (the evaluator's captured cosine and sine).
	 */
	private static MetricLinearPart2D linearPart(Step step, boolean constantPoint) {
		switch (step.getOperation()) {
		case TRANSLATE:
		case EXPRESSION_TRANSLATE:
			return MetricLinearPart2D.IDENTITY;
		case ROTATE:
		case ROTATE_ABOUT:
			return MetricLinearPart2D.of(step.getParameter(0), -step.getParameter(1),
					step.getParameter(1), step.getParameter(0));
		case MIRROR_POINT:
			return MetricLinearPart2D.scalar(-1);
		case MIRROR_LINE:
			return MetricLinearPart2D.of(step.getParameter(4), step.getParameter(5),
					step.getParameter(5), -step.getParameter(4));
		case DILATE:
			return MetricLinearPart2D.scalar(step.getParameter(0));
		case MIDPOINT:
			// The midpoint of the moving point and a captured constant point.
			return constantPoint ? MetricLinearPart2D.scalar(0.5) : null;
		default:
			return null;
		}
	}

	/**
	 * Derivative matrix of the circle driver
	 * {@code (h0 cos t) e0 + (h1 sin t) e1 + m}: its columns are {@code h0 e0} and
	 * {@code h1 e1}, applied to the unit vector {@code (-sin t, cos t)}.
	 */
	private static MetricLinearPart2D circleDerivativeMatrix(
			CertifiedConstructionProgram2D program) {
		MetricOutwardInterval2D h0 = constant(program, 0);
		MetricOutwardInterval2D h1 = constant(program, 1);
		return new MetricLinearPart2D(h0.multiply(constant(program, 2)),
				h1.multiply(constant(program, 4)), h0.multiply(constant(program, 3)),
				h1.multiply(constant(program, 5)));
	}

	private static MetricOutwardInterval2D constant(
			CertifiedConstructionProgram2D program, int index) {
		return MetricOutwardInterval2D.point(program.getDriverConstant(index));
	}

	private static MetricOutwardInterval2D norm(MetricOutwardInterval2D[] vector) {
		return MetricLinearPart2D.norm(vector[0], vector[1]);
	}
}
