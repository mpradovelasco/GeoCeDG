/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D;
import org.junit.jupiter.api.Test;

/**
 * Design counterexamples, not production root certificates or materialization
 * acceptance. Analytic branches and diagnostic bisection do not assign tokens.
 */
final class G9S1R1PairSelectorCharacterizationTest {

	@Test
	void threeRootExampleHasDistinctRegularSemanticPreimagesAtZero() {
		// F_t(u) = (u, t), G(v) = (v^2 - 1, v^3 - v).
		double[] parameters = {-1, 0, 1};
		for (double v : parameters) {
			double u = v * v - 1;
			assertTrue(u >= -2 && u <= 1);
			assertEquals(0, cubic(v), 0);
			assertNotEquals(0, cubicDerivative(v));
		}
		assertEquals(2, cubicDerivative(-1), 0);
		assertEquals(2, cubicDerivative(1), 0);
		assertEquals(-1, cubicDerivative(0), 0);
		// Equal u projections do not identify the two distinct outer preimages.
		assertEquals(parameters[0] * parameters[0] - 1,
				parameters[2] * parameters[2] - 1, 0);
		assertNotEquals(parameters[0], parameters[2]);
	}

	@Test
	void ordinaryMotionReversesFirstProjectionWithoutPairSingularity() {
		double[][] roots = new double[2][3];
		double[][] intervals = {{-1.1, -0.9}, {-0.1, 0.1}, {0.9, 1.1}};
		double[] heights = {-0.01, 0.01};
		for (int snapshot = 0; snapshot < heights.length; snapshot++) {
			for (int branch = 0; branch < intervals.length; branch++) {
				// Each fixed interval is monotone for this diagnostic polynomial.
				// It is not a proposed general-purpose semantic selector.
				double v = bisectCubic(heights[snapshot], intervals[branch][0],
						intervals[branch][1]);
				roots[snapshot][branch] = v;
				assertEquals(heights[snapshot], cubic(v), 1E-14);
				assertTrue(Math.abs(cubicDerivative(v)) > 0.9);
			}
			assertTrue(roots[snapshot][0] < roots[snapshot][1]);
			assertTrue(roots[snapshot][1] < roots[snapshot][2]);
			assertTrue(cubicDerivative(roots[snapshot][0]) > 0);
			assertTrue(cubicDerivative(roots[snapshot][2]) > 0);
		}
		double before = roots[0][0] * roots[0][0] - roots[0][2] * roots[0][2];
		double after = roots[1][0] * roots[1][0] - roots[1][2] * roots[1][2];
		assertTrue(before > 0);
		assertTrue(after < 0);
	}

	@Test
	void quarticPairHasNineExactNonsingularPreimagesAtZero() {
		// F(u) = (p(u), u p(u)/2), G_t(v) = (v p(v)/2 + t, p(v)).
		// On [-1.1, 1.1]^2, 1 - uv/4 > 0. The equations at t=0
		// therefore imply p(u)=p(v)=0, not merely nine discovered candidates.
		assertTrue(1 - 1.1 * 1.1 / 4 > 0);
		int pairs = 0;
		for (double u : new double[] {-1, 0, 1}) {
			for (double v : new double[] {-1, 0, 1}) {
				assertEquals(cubic(u), v * cubic(v) / 2, 0);
				assertEquals(u * cubic(u) / 2, cubic(v), 0);
				double determinant = cubicDerivative(u) * cubicDerivative(v)
						* (1 - u * v / 4);
				assertNotEquals(0, determinant);
				if (u != 0 && v != 0) {
					assertTrue(determinant > 0);
				}
				pairs++;
			}
		}
		assertEquals(9, pairs);
	}

	@Test
	void quarticPairImplicitVelocitiesExchangeBothProjectionRanks() {
		// Differentiate F(u(t)) = G_t(v(t)) at an outer root (u,v).
		// Nonzero pair Jacobians establish local analytic branches. Unequal
		// derivatives at an equal projection imply a projection-order crossing,
		// without a collision of the distinct parameter pairs.
		for (double u : new double[] {-1, 1}) {
			for (double v : new double[] {-1, 1}) {
				double[] velocity = outerPairVelocity(u, v);
				assertEquals(1, 2 * velocity[0] - v * velocity[1], 1E-15);
				assertEquals(0, u * velocity[0] - 2 * velocity[1], 1E-15);
			}
			assertNotEquals(outerPairVelocity(u, -1)[0],
					outerPairVelocity(u, 1)[0]);
		}
		for (double v : new double[] {-1, 1}) {
			assertNotEquals(outerPairVelocity(-1, v)[1],
					outerPairVelocity(1, v)[1]);
		}
		assertEquals(2.0 / 3, outerPairVelocity(1, 1)[0], 0);
		assertEquals(2.0 / 5, outerPairVelocity(1, -1)[0], 0);
		assertEquals(1.0 / 3, outerPairVelocity(1, 1)[1], 0);
		assertEquals(-1.0 / 5, outerPairVelocity(-1, 1)[1], 0);
	}

	@Test
	void equalDiscoveryCountsDoNotEstablishSemanticRootRank() {
		List<Double> completeRoots = List.of(1.0, 2.0, 3.0, 4.0);
		List<Double> firstDiscovery = List.of(1.0, 3.0);
		List<Double> secondDiscovery = List.of(2.0, 4.0);
		for (double root : completeRoots) {
			assertEquals(0, (root - 1) * (root - 2) * (root - 3) * (root - 4), 0);
			double derivative = 1;
			for (double other : completeRoots) {
				if (root != other) {
					derivative *= root - other;
				}
			}
			assertNotEquals(0, derivative);
		}
		// Equal found cardinality and regular individual roots do not certify
		// that rank in the found subset is rank in the semantic root set.
		assertEquals(firstDiscovery.size(), secondDiscovery.size());
		for (int foundRank = 0; foundRank < firstDiscovery.size(); foundRank++) {
			assertNotEquals(firstDiscovery.get(foundRank), secondDiscovery.get(foundRank));
			assertNotEquals(completeRoots.indexOf(firstDiscovery.get(foundRank)),
					completeRoots.indexOf(secondDiscovery.get(foundRank)));
		}
	}

	@Test
	void roundedTransformedCoefficientsAreNotSemanticEvaluationAuthority() {
		double offset = Math.scalb(1, 54);
		LocusSimilarityTransform2D transform = LocusSimilarityTransform2D.translation(
				offset, 0);
		double[][] coefficients = transform.transformPolynomialCoefficients(
				new double[] {1, 0}, new double[] {0, 0});
		double expandedValue = coefficients[0][0] * 4 + coefficients[0][1];
		double semanticValue = transform.transform(new LocusPoint2D(4, 0)).getX();
		// The existing coefficient expansion subtracts translated basis values.
		// At this magnitude offset+1 rounds to offset, losing the unit slope.
		// This characterizes a certificate prerequisite, not an accepted fix.
		assertEquals(4, Math.ulp(offset), 0);
		assertEquals(0, coefficients[0][0], 0);
		assertEquals(offset, expandedValue, 0);
		assertEquals(offset + 4, semanticValue, 0);
		assertNotEquals(Double.doubleToRawLongBits(expandedValue),
				Double.doubleToRawLongBits(semanticValue));
	}

	private static double cubic(double parameter) {
		return parameter * parameter * parameter - parameter;
	}

	private static double cubicDerivative(double parameter) {
		return 3 * parameter * parameter - 1;
	}

	private static double bisectCubic(double value, double lower, double upper) {
		double lowerResidual = cubic(lower) - value;
		assertTrue(lowerResidual * (cubic(upper) - value) < 0);
		for (int iteration = 0; iteration < 64; iteration++) {
			double middle = (lower + upper) / 2;
			double residual = cubic(middle) - value;
			if (residual == 0) {
				return middle;
			}
			if (Math.signum(residual) == Math.signum(lowerResidual)) {
				lower = middle;
				lowerResidual = residual;
			} else {
				upper = middle;
			}
		}
		return (lower + upper) / 2;
	}

	private static double[] outerPairVelocity(double u, double v) {
		double factor = 1 - u * v / 4;
		return new double[] {1 / (2 * factor), u / (4 * factor)};
	}
}
