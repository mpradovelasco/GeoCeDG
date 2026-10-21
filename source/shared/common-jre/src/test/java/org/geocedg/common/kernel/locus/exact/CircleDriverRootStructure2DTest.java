/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.geocedg.common.kernel.locus.exact.CircleDriverRootStructure2D.Root;
import org.geocedg.common.kernel.locus.exact.CircleDriverRootStructure2D.Status;
import org.geocedg.common.kernel.locus.exact.RealRootStructure1D.RootKind;
import org.junit.jupiter.api.Test;

/**
 * Exact classification of first-level circle-driver predicates on the synthetic CeDG
 * configurations of the PRE-G9B-CU2 validation matrix (section 1, rows V-CRIT and V-POINT).
 *
 * <p>The circle {@code p} has centre (0, 0) and radius 25 and the fixed point is
 * {@code P = (-25, 50)}. The driver is {@code N(t) = C + r (cos t, sin t)}. The line
 * {@code a x + b y + c = 0} through {@code P} and {@code N} (the AlgoJoinPoints formula
 * {@code a = P_y - N_y}, {@code b = N_x - P_x}, {@code c = P_x N_y - P_y N_x}) meets
 * {@code p} exactly when {@code D = r_p^2 (a^2 + b^2) - (a x_p + b y_p + c)^2 >= 0}.</p>
 */
final class CircleDriverRootStructure2DTest {
	private static final ExactRational2D CIRCLE_X = ExactRational2D.ZERO;
	private static final ExactRational2D CIRCLE_Y = ExactRational2D.ZERO;
	private static final ExactRational2D CIRCLE_RADIUS = ExactRational2D.of(25);
	private static final ExactRational2D POINT_X = ExactRational2D.of(-25);
	private static final ExactRational2D POINT_Y = ExactRational2D.of(50);
	private static final ExactRational2D U_WIDTH = powerOfTwo(-40);
	private static final int[] NEAR_CRITICAL_EXPONENTS = {10, 30, 47, 80};

	@Test
	void criticalConfigurationHasOneSingularInteriorDoubleRoot() {
		// V-CRIT-1 and V-CRIT-4: g_1 with centre (35, -45) and radius 40
		CircleDriverRootStructure2D structure = classify(discriminant(r(35), r(-45), r(40)));

		assertEquals(Status.CLASSIFIED, structure.status());
		assertFalse(structure.isIdenticallyZero());
		assertEquals(2, structure.totalDegree());
		assertEquals(poly(-1, 2).pow(2).multiply(poly(5, 0, 1)).scale(r(1000000)),
				structure.tangentHalfAnglePolynomial());
		assertEquals(1, structure.finiteRootStructure().roots().size());
		assertEquals(1, structure.roots().size());
		Root root = structure.roots().get(0);
		assertFalse(root.isAtInfinity());
		assertFalse(root.hasCanonicalParameterMinusPi());
		assertTrue(root.isExactInU());
		assertEquals(ExactRational2D.of(1, 2), root.uLower());
		assertEquals(2, root.multiplicity());
		assertEquals(1, root.signLeft());
		assertEquals(1, root.signRight());
		assertEquals(RootKind.SINGULAR_INTERIOR, root.kind());
		assertEnclosesAngle(root, Math.atan2(4, 3));
		assertEquals(Arrays.asList(1, 1), structure.gapSigns());
		assertEquals(r(4000000), structure.valueAtPi());
		assertEquals(0, structure.multiplicityAtPi());
	}

	@Test
	void exteriorTangencyIsAnIsolatedPoint() {
		// V-CRIT-5 and V-POINT-1: g_1 with centre (83, 19) and radius 40
		CircleDriverRootStructure2D structure = classify(discriminant(r(83), r(19), r(40)));

		assertEquals(poly(2, 1).pow(2).multiply(poly(37, 0, 17)).scale(r(-200000)),
				structure.tangentHalfAnglePolynomial());
		assertEquals(1, structure.roots().size());
		Root root = structure.roots().get(0);
		assertTrue(root.isExactInU());
		assertEquals(r(-2), root.uLower());
		assertEquals(2, root.multiplicity());
		assertEquals(-1, root.signLeft());
		assertEquals(-1, root.signRight());
		assertEquals(RootKind.ISOLATED_POINT, root.kind());
		assertEnclosesAngle(root, Math.atan2(-4, -3));
		assertEquals(Arrays.asList(-1, -1), structure.gapSigns());
		assertEquals(r(-3400000), structure.valueAtPi());
		assertEquals(0, structure.multiplicityAtPi());
	}

	@Test
	void seamAndInteriorPointComponentsAreBothIsolatedPoints() {
		// V-POINT-2: Line(P, N) with N on the circle of centre (15, 70) and radius 40
		CircleDriverRootStructure2D structure = classify(discriminant(r(15), r(70), r(40)));

		assertEquals(poly(2, 1).pow(2).scale(r(-4000000)),
				structure.tangentHalfAnglePolynomial());
		assertTrue(structure.valueAtPi().isZero());
		assertEquals(2, structure.multiplicityAtPi());
		assertEquals(2, structure.roots().size());
		Root seam = structure.roots().get(0);
		assertTrue(seam.isAtInfinity());
		assertTrue(seam.hasCanonicalParameterMinusPi());
		assertNull(seam.uLower());
		assertNull(seam.uUpper());
		assertFalse(seam.isExactInU());
		assertEquals(Math.PI, seam.tLower());
		assertEquals(Math.nextUp(Math.PI), seam.tUpper());
		assertEquals(-Math.nextUp(Math.PI), seam.canonicalTLower());
		assertEquals(-Math.PI, seam.canonicalTUpper());
		assertEquals(2, seam.multiplicity());
		assertEquals(-1, seam.signLeft());
		assertEquals(-1, seam.signRight());
		assertEquals(RootKind.ISOLATED_POINT, seam.kind());
		assertEquals(0, seam.bisections());
		Root interior = structure.roots().get(1);
		assertEquals(r(-2), interior.uLower());
		assertEquals(2, interior.multiplicity());
		assertEquals(RootKind.ISOLATED_POINT, interior.kind());
		assertEnclosesAngle(interior, Math.atan2(-4, -3));
		assertEquals(Arrays.asList(0, -1, -1), structure.gapSigns());
	}

	@Test
	void nearCriticalRadiiAreDecidedExactly() {
		// V-CRIT-2 (k = 10, 30, 47) and V-CRIT-3 (k = 80)
		for (int k : NEAR_CRITICAL_EXPONENTS) {
			ExactRational2D smaller = r(40).subtract(powerOfTwo(-k));
			ExactRational2D larger = r(40).add(powerOfTwo(-k));
			if (k <= 47) {
				assertEquals(smaller, ExactRational2D.ofBinary64(40.0 - Math.scalb(1.0, -k)));
				assertEquals(larger, ExactRational2D.ofBinary64(40.0 + Math.scalb(1.0, -k)));
			}

			CircleDriverRootStructure2D penetration = classify(
					discriminant(r(35), r(-45), smaller));
			CircleDriverRootStructure2D bite = classify(discriminant(r(35), r(-45), larger));

			assertTrue(penetration.roots().isEmpty(), "k=" + k);
			assertEquals(Arrays.asList(1), penetration.gapSigns());
			assertTrue(penetration.valueAtPi().signum() > 0);
			assertTwoSimpleRootsAroundTheCriticalParameter(bite, k);
			Root first = bite.roots().get(0);
			Root second = bite.roots().get(1);
			assertTrue(first.tUpper() < second.tLower(), "k=" + k);
			assertTrue(first.tLower() < Math.atan2(4, 3) && Math.atan2(4, 3) < second.tUpper());
		}
		assertEquals(ExactRational2D.ofBinary64(Math.nextUp(40.0)), r(40).add(powerOfTwo(-47)));
	}

	@Test
	void separationOfTwoToTheMinus100IsDecidedWithinTheDefaultBudget() {
		// V-CRIT-3 with k = 200; recorded outcome: the default budget suffices, so no
		// ExactBudgetExhaustedException. The roots are about 2^-101.8 apart in u.
		ExactRational2D larger = r(40).add(powerOfTwo(-200));
		ExactRational2D smaller = r(40).subtract(powerOfTwo(-200));

		CircleDriverRootStructure2D bite = classify(discriminant(r(35), r(-45), larger));
		CircleDriverRootStructure2D penetration = classify(
				discriminant(r(35), r(-45), smaller));

		assertTwoSimpleRootsAroundTheCriticalParameter(bite, 200);
		assertTrue(bite.maximumRootBisections() > 100);
		assertTrue(bite.maximumRootBisections()
				<= ExactBudget1D.DEFAULT_MAXIMUM_BISECTIONS_PER_ROOT);
		assertTrue(bite.maximumCoefficientBitLength()
				<= ExactBudget1D.DEFAULT_MAXIMUM_COEFFICIENT_BITS);
		assertTrue(penetration.roots().isEmpty());
		assertEquals(Arrays.asList(1), penetration.gapSigns());
	}

	@Test
	void evenSeamRootsTakeTheirMultiplicityFromTheDegreeDrop() {
		TrigonometricPolynomial2D onePlusCosine = constant(1)
				.add(TrigonometricPolynomial2D.cosine());

		CircleDriverRootStructure2D touching = classify(onePlusCosine);
		CircleDriverRootStructure2D squared = classify(onePlusCosine.multiply(onePlusCosine));

		assertEquals(poly(2), touching.tangentHalfAnglePolynomial());
		assertEquals(2, touching.multiplicityAtPi());
		assertEquals(1, touching.roots().size());
		assertEquals(RootKind.SINGULAR_INTERIOR, touching.roots().get(0).kind());
		assertEquals(Arrays.asList(0, 1), touching.gapSigns());
		assertEquals(4, squared.multiplicityAtPi());
		assertEquals(RootKind.SINGULAR_INTERIOR, squared.roots().get(0).kind());
	}

	@Test
	void sineCrossesZeroAtTheSeamAndAtTheOrigin() {
		CircleDriverRootStructure2D crossing = classify(TrigonometricPolynomial2D.sine());

		assertEquals(1, crossing.multiplicityAtPi());
		assertEquals(2, crossing.roots().size());
		Root seam = crossing.roots().get(0);
		Root zero = crossing.roots().get(1);
		assertTrue(seam.isAtInfinity());
		assertEquals(1, seam.multiplicity());
		assertEquals(1, seam.signLeft());
		assertEquals(-1, seam.signRight());
		assertEquals(RootKind.TRANSITION, seam.kind());
		assertTrue(zero.isExactInU());
		assertEquals(ExactRational2D.ZERO, zero.uLower());
		assertEquals(RootKind.TRANSITION, zero.kind());
		assertTrue(zero.tLower() <= 0.0 && 0.0 <= zero.tUpper());
		assertEquals(Arrays.asList(0, -1, 1), crossing.gapSigns());
	}

	@Test
	void identicallyZeroAndConstantPredicates() {
		TrigonometricPolynomial2D cosine = TrigonometricPolynomial2D.cosine();
		TrigonometricPolynomial2D sine = TrigonometricPolynomial2D.sine();
		TrigonometricPolynomial2D unit = cosine.multiply(cosine).add(sine.multiply(sine))
				.subtract(constant(1));

		CircleDriverRootStructure2D vanishing = classify(unit);

		assertEquals(Status.IDENTICALLY_ZERO, vanishing.status());
		assertTrue(vanishing.isIdenticallyZero());
		assertTrue(vanishing.roots().isEmpty());
		assertEquals(Arrays.asList(0), vanishing.gapSigns());
		assertNull(vanishing.finiteRootStructure());
		assertEquals(0, vanishing.bisections());
		assertTrue(vanishing.valueAtPi().isZero());
		assertEquals(Status.IDENTICALLY_ZERO,
				classify(TrigonometricPolynomial2D.ZERO).status());
		CircleDriverRootStructure2D negative = classify(constant(-2));
		assertEquals(Status.CLASSIFIED, negative.status());
		assertTrue(negative.roots().isEmpty());
		assertEquals(Arrays.asList(-1), negative.gapSigns());
		assertEquals(r(-2), negative.valueAtPi());
	}

	@Test
	void classificationsAreDeterministic() {
		// V-CRIT-10: every field, including work counters and binary64 enclosures
		List<TrigonometricPolynomial2D> predicates = Arrays.asList(
				discriminant(r(35), r(-45), r(40)), discriminant(r(83), r(19), r(40)),
				discriminant(r(15), r(70), r(40)),
				discriminant(r(35), r(-45), r(40).add(powerOfTwo(-80))),
				discriminant(r(35), r(-45), r(40).subtract(powerOfTwo(-80))));
		for (TrigonometricPolynomial2D predicate : predicates) {
			CircleDriverRootStructure2D first = classify(predicate);
			CircleDriverRootStructure2D second = classify(predicate);
			assertEquals(first, second);
			assertEquals(first.hashCode(), second.hashCode());
			assertEquals(first.toString(), second.toString());
			assertEquals(first.bisections(), second.bisections());
			assertEquals(first.maximumCoefficientBitLength(),
					second.maximumCoefficientBitLength());
		}
	}

	@Test
	void budgetsAndArgumentsAreEnforced() {
		TrigonometricPolynomial2D bite = discriminant(r(35), r(-45),
				r(40).add(powerOfTwo(-80)));

		ExactBudgetExhaustedException bits = assertThrows(ExactBudgetExhaustedException.class,
				() -> CircleDriverRootStructure2D.classify(bite, U_WIDTH,
						ExactBudget1D.of(64, 512)));
		ExactBudgetExhaustedException bisections = assertThrows(
				ExactBudgetExhaustedException.class,
				() -> CircleDriverRootStructure2D.classify(bite, U_WIDTH,
						ExactBudget1D.of(4096, 10)));

		assertEquals(ExactBudgetExhaustedException.Reason.COEFFICIENT_BITS, bits.reason());
		assertEquals(ExactBudgetExhaustedException.Reason.BISECTIONS, bisections.reason());
		assertThrows(IllegalArgumentException.class, () -> CircleDriverRootStructure2D
				.classify(bite, ExactRational2D.ZERO, ExactBudget1D.DEFAULT));
	}

	private static void assertTwoSimpleRootsAroundTheCriticalParameter(
			CircleDriverRootStructure2D structure, int k) {
		String label = "k=" + k;
		assertEquals(Status.CLASSIFIED, structure.status(), label);
		assertEquals(2, structure.roots().size(), label);
		Root first = structure.roots().get(0);
		Root second = structure.roots().get(1);
		ExactRational2D half = ExactRational2D.of(1, 2);
		ExactRational2D margin = powerOfTwo(1 - k / 2);
		for (Root root : structure.roots()) {
			assertFalse(root.isAtInfinity(), label);
			assertEquals(1, root.multiplicity(), label);
			assertEquals(RootKind.TRANSITION, root.kind(), label);
			assertTrue(root.uUpper().subtract(root.uLower()).compareTo(U_WIDTH) <= 0, label);
			assertTrue(root.uLower().subtract(half).abs().compareTo(margin) <= 0, label);
			assertTrue(root.uUpper().subtract(half).abs().compareTo(margin) <= 0, label);
		}
		assertTrue(first.uUpper().compareTo(second.uLower()) < 0, label);
		assertTrue(first.uLower().compareTo(half) < 0, label);
		assertTrue(second.uUpper().compareTo(half) > 0, label);
		assertEquals(1, first.signLeft(), label);
		assertEquals(-1, first.signRight(), label);
		assertEquals(-1, second.signLeft(), label);
		assertEquals(1, second.signRight(), label);
		assertEquals(Arrays.asList(1, -1, 1), structure.gapSigns(), label);
		assertEquals(0, structure.multiplicityAtPi(), label);
	}

	private static void assertEnclosesAngle(Root root, double angle) {
		assertTrue(root.tLower() <= angle && angle <= root.tUpper(), root.toString());
		assertTrue(root.tUpper() - root.tLower() < 1e-14, root.toString());
		assertEquals(root.tLower(), root.canonicalTLower());
		assertEquals(root.tUpper(), root.canonicalTUpper());
	}

	/** Discriminant of Line(P, N(t)) with the circle p for N(t) = C + radius (cos t, sin t). */
	private static TrigonometricPolynomial2D discriminant(ExactRational2D centreX,
			ExactRational2D centreY, ExactRational2D radius) {
		TrigonometricPolynomial2D nx = TrigonometricPolynomial2D.constant(centreX)
				.add(TrigonometricPolynomial2D.cosine().scale(radius));
		TrigonometricPolynomial2D ny = TrigonometricPolynomial2D.constant(centreY)
				.add(TrigonometricPolynomial2D.sine().scale(radius));
		TrigonometricPolynomial2D px = TrigonometricPolynomial2D.constant(POINT_X);
		TrigonometricPolynomial2D py = TrigonometricPolynomial2D.constant(POINT_Y);
		TrigonometricPolynomial2D a = py.subtract(ny);
		TrigonometricPolynomial2D b = nx.subtract(px);
		TrigonometricPolynomial2D c = px.multiply(ny).subtract(py.multiply(nx));
		TrigonometricPolynomial2D offset = a.scale(CIRCLE_X).add(b.scale(CIRCLE_Y)).add(c);
		return a.multiply(a).add(b.multiply(b)).scale(CIRCLE_RADIUS.multiply(CIRCLE_RADIUS))
				.subtract(offset.multiply(offset));
	}

	private static CircleDriverRootStructure2D classify(TrigonometricPolynomial2D predicate) {
		return CircleDriverRootStructure2D.classify(predicate, U_WIDTH, ExactBudget1D.DEFAULT);
	}

	private static TrigonometricPolynomial2D constant(long value) {
		return TrigonometricPolynomial2D.constant(r(value));
	}

	private static ExactRational2D powerOfTwo(int exponent) {
		BigInteger power = BigInteger.ONE.shiftLeft(Math.abs(exponent));
		return exponent >= 0 ? ExactRational2D.of(power, BigInteger.ONE)
				: ExactRational2D.of(BigInteger.ONE, power);
	}

	private static ExactPolynomial1D poly(long... coefficients) {
		ExactRational2D[] values = new ExactRational2D[coefficients.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = r(coefficients[i]);
		}
		return ExactPolynomial1D.of(values);
	}

	private static ExactRational2D r(long value) {
		return ExactRational2D.of(value);
	}
}
