/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Typed validation in the order of bspline-2d section 7 followed by the weight causes. */
final class BSplineValidationTest {

	private static final BSplineLimits FAMILY = BSplineLimits.of(7, 128);
	private static final double[] CUBIC_KNOTS = {0, 0, 0, 0, 1, 2, 2, 2, 2};
	private static final double[] XS = {0, 1, 2, 3, 4};
	private static final double[] YS = {0, 1, 0, 1, 0};

	private static BSplineDataCause cause(int p, double[] knots, double[] xs, double[] ys,
			double[] ws) {
		BSplineValidation validation = BSplineData.validate(p, knots, xs, ys, ws, FAMILY);
		assertFalse(validation.isValid());
		assertThrows(IllegalStateException.class, validation::data);
		return validation.cause();
	}

	@Test
	void validCubicIsCapturedByCopy() {
		double[] knots = CUBIC_KNOTS.clone();
		double[] xs = XS.clone();
		BSplineValidation validation = BSplineData.validate(3, knots, xs, YS, null, FAMILY);
		assertTrue(validation.isValid());
		assertNull(validation.cause());
		knots[4] = 1.5;
		xs[0] = 99;
		BSplineData data = validation.data();
		assertEquals(1.0, data.knot(4));
		assertEquals(0.0, data.x(0));
		double[] copy = data.knots();
		copy[4] = 7;
		assertEquals(1.0, data.knot(4));
		assertEquals(BSplineWeightMode.IMPLICIT_UNIT, data.weightMode());
		assertNull(data.explicitWeights());
	}

	@Test
	void eachCauseIsReported() {
		assertEquals(BSplineDataCause.NONFINITE_DATA, cause(3,
				new double[] {0, 0, 0, 0, Double.NaN, 2, 2, 2, 2}, XS, YS, null));
		assertEquals(BSplineDataCause.NONFINITE_DATA, cause(3, CUBIC_KNOTS,
				new double[] {0, 1, Double.POSITIVE_INFINITY, 3, 4}, YS, null));
		assertEquals(BSplineDataCause.NONFINITE_DATA, cause(3, CUBIC_KNOTS, XS, YS,
				new double[] {1, 1, Double.POSITIVE_INFINITY, 1, 1}));
		assertEquals(BSplineDataCause.DEGREE_INVALID, cause(0, CUBIC_KNOTS, XS, YS, null));
		assertEquals(BSplineDataCause.DEGREE_INVALID, cause(8, CUBIC_KNOTS, XS, YS, null));
		assertEquals(BSplineDataCause.TOO_FEW_CONTROL_POINTS,
				cause(5, CUBIC_KNOTS, XS, YS, null));
		assertEquals(BSplineDataCause.TOO_MANY_CONTROL_POINTS, rejectWithLimit());
		assertEquals(BSplineDataCause.KNOT_COUNT_MISMATCH,
				cause(3, new double[] {0, 0, 0, 0, 2, 2, 2, 2}, XS, YS, null));
		assertEquals(BSplineDataCause.KNOTS_DECREASING,
				cause(3, new double[] {0, 0, 0, 0, 3, 2, 2, 2, 2}, XS, YS, null));
		assertEquals(BSplineDataCause.NOT_CLAMPED,
				cause(3, new double[] {0, 0, 0, 0.5, 1, 2, 2, 2, 2}, XS, YS, null));
		assertEquals(BSplineDataCause.NOT_CLAMPED,
				cause(3, new double[] {0, 0, 0, 0, 1, 2, 2, 2, 3}, XS, YS, null));
		// End multiplicity p + 2 with a non-empty domain.
		assertEquals(BSplineDataCause.NOT_CLAMPED,
				cause(3, new double[] {0, 0, 0, 0, 0, 2, 2, 2, 2}, XS, YS, null));
		assertEquals(BSplineDataCause.NOT_CLAMPED,
				cause(3, new double[] {0, 0, 0, 0, 2, 2, 2, 2, 2}, XS, YS, null));
		assertEquals(BSplineDataCause.EMPTY_DOMAIN,
				cause(3, new double[] {1, 1, 1, 1, 1, 1, 1, 1, 1}, XS, YS, null));
		assertEquals(BSplineDataCause.INTERIOR_MULTIPLICITY_EXCEEDED, cause(2,
				new double[] {0, 0, 0, 1, 1, 1, 2, 2, 2}, new double[] {0, 1, 2, 3, 4, 5},
				new double[] {0, 1, 0, 1, 0, 1}, null));
		assertEquals(BSplineDataCause.WEIGHT_COUNT_MISMATCH,
				cause(3, CUBIC_KNOTS, XS, YS, new double[] {1, 1, 1, 1}));
		assertEquals(BSplineDataCause.NONPOSITIVE_WEIGHT,
				cause(3, CUBIC_KNOTS, XS, YS, new double[] {1, 1, 0.0, 1, 1}));
		assertEquals(BSplineDataCause.NONPOSITIVE_WEIGHT,
				cause(3, CUBIC_KNOTS, XS, YS, new double[] {1, 1, -0.0, 1, 1}));
		assertEquals(BSplineDataCause.NONPOSITIVE_WEIGHT,
				cause(3, CUBIC_KNOTS, XS, YS, new double[] {1, -2, 1, 1, 1}));
	}

	private static BSplineDataCause rejectWithLimit() {
		BSplineValidation validation = BSplineData.validate(1, new double[] {0, 0, 1, 2, 3, 3},
				new double[] {0, 1, 2, 3}, new double[] {0, 0, 0, 0}, null,
				BSplineLimits.of(7, 3));
		return validation.cause();
	}

	@Test
	void onlyTheFirstFailingCauseIsReported() {
		// nonfinite before degree
		assertEquals(BSplineDataCause.NONFINITE_DATA, cause(9,
				new double[] {0, 0, 0, 0, 1, 2, 2, 2, Double.NaN}, XS, YS, null));
		// too few before knot count
		assertEquals(BSplineDataCause.TOO_FEW_CONTROL_POINTS,
				cause(5, new double[] {0, 1}, XS, YS, null));
		// decreasing before not clamped
		assertEquals(BSplineDataCause.KNOTS_DECREASING,
				cause(3, new double[] {0, 0, 0, 1, 0.5, 2, 2, 2, 3}, XS, YS, null));
		// interior multiplicity before weights
		assertEquals(BSplineDataCause.INTERIOR_MULTIPLICITY_EXCEEDED, cause(2,
				new double[] {0, 0, 0, 1, 1, 1, 2, 2, 2}, new double[] {0, 1, 2, 3, 4, 5},
				new double[] {0, 1, 0, 1, 0, 1}, new double[] {1, 2}));
		// weight count before positivity
		assertEquals(BSplineDataCause.WEIGHT_COUNT_MISMATCH,
				cause(3, CUBIC_KNOTS, XS, YS, new double[] {-1, 1}));
	}

	@Test
	void signedZerosAreTheSameKnotValue() {
		BSplineValidation validation = BSplineData.validate(2,
				new double[] {-0.0, 0.0, 0.0, 1, 1, 1}, new double[] {0, 1, 2},
				new double[] {0, 1, 0}, null, FAMILY);
		assertTrue(validation.isValid());
		assertEquals(3, validation.data().multiplicity(0.0));
	}

	@Test
	void programmingErrorsAreNotDataOutcomes() {
		assertThrows(IllegalArgumentException.class, () -> BSplineLimits.of(0, 10));
		assertThrows(IllegalArgumentException.class,
				() -> BSplineLimits.of(BSplineLimits.ABSOLUTE_MAX_DEGREE + 1, 10));
		assertThrows(IllegalArgumentException.class,
				() -> BSplineLimits.of(3, BSplineLimits.ABSOLUTE_MAX_CONTROL_POINTS + 1));
		assertThrows(IllegalArgumentException.class, () -> BSplineData.validate(3, CUBIC_KNOTS,
				XS, new double[] {0, 1}, null, FAMILY));
		assertThrows(NullPointerException.class, () -> BSplineData.validate(3, null, XS, YS,
				null, FAMILY));
	}
}
