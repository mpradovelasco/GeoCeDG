/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;

import org.geocedg.common.kernel.locus.constructive.ExactQuadraticTower.Element;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;
import org.junit.jupiter.api.Test;

/**
 * Exact arithmetic of the tower of quadratic extensions and its budget
 * {@code cu3-exact-evaluation-budget/v1} (validation matrix V-M3-8 for the injected budget).
 */
final class ExactQuadraticTowerTest {

	private static final MathContext DIGITS = new MathContext(60);

	@Test
	void squareRootsAreExactRealNumbers() {
		ExactQuadraticTower tower = new ExactQuadraticTower(ExactEvaluationBudget.V1);
		Element two = tower.of(2);
		Element root = tower.sqrt(two);

		assertEquals(1, root.getLevel());
		assertTrue(root.square().subtract(two).isZero(), "sqrt(2)^2 = 2 exactly");
		Element conjugateProduct = tower.one().add(root).multiply(tower.one().subtract(root));
		assertTrue(conjugateProduct.add(tower.one()).isZero(), "(1 + sqrt 2)(1 - sqrt 2) = -1");
		assertEquals(1, root.subtract(tower.rational(ExactRational2D.of(141421356237L,
				100000000000L))).signum());
		assertEquals(-1, root.subtract(tower.rational(ExactRational2D.of(141421356238L,
				100000000000L))).signum());
	}

	@Test
	void rationalSquaresHaveRationalRootsAndOpenNoLevel() {
		ExactQuadraticTower tower = new ExactQuadraticTower(ExactEvaluationBudget.V1);
		Element root = tower.sqrt(tower.rational(ExactRational2D.of(9, 4)));

		assertTrue(root.isRational());
		assertEquals(ExactRational2D.of(3, 2), root.getRational());
		assertEquals(0, tower.depth());
		assertEquals(1, tower.counters().getRationalSquareRoots());
	}

	@Test
	void nestedRootsAreSignedExactlyAgainstAHighPrecisionReference() {
		ExactQuadraticTower tower = new ExactQuadraticTower(ExactEvaluationBudget.V1);
		Element root3 = tower.sqrt(tower.of(3));
		Element nested = tower.sqrt(tower.of(2).add(root3));
		BigDecimal reference = new BigDecimal(2).add(new BigDecimal(3).sqrt(DIGITS))
				.sqrt(DIGITS);
		BigDecimal below = reference.subtract(new BigDecimal("1e-40"));
		BigDecimal above = reference.add(new BigDecimal("1e-40"));

		assertEquals(2, tower.depth());
		assertEquals(1, nested.subtract(rational(tower, below)).signum());
		assertEquals(-1, nested.subtract(rational(tower, above)).signum());
		// The square of the nested root equals its radicand exactly.
		Element identity = nested.square().subtract(tower.of(2).add(root3));
		assertTrue(identity.isZero());
	}

	@Test
	void aSquareRadicandAboveLevelZeroStillDecidesSignsAndInverses() {
		ExactQuadraticTower tower = new ExactQuadraticTower(ExactEvaluationBudget.V1);
		Element root2 = tower.sqrt(tower.of(2));
		Element onePlusRoot2 = tower.one().add(root2);
		// 3 + 2 sqrt 2 is the square of 1 + sqrt 2; the tower opens a level anyway.
		Element y = tower.sqrt(onePlusRoot2.square());
		Element vanishing = y.subtract(onePlusRoot2);
		Element doubled = y.add(onePlusRoot2);

		assertEquals(2, tower.depth());
		assertTrue(vanishing.isZero(), "sqrt((1 + sqrt 2)^2) - (1 + sqrt 2) = 0");
		assertThrows(ArithmeticException.class, vanishing::inverse);
		Element product = doubled.multiply(doubled.inverse());
		assertTrue(product.subtract(tower.one()).isZero(),
				"the inverse of an element with zero norm uses a + b sqrt(d) = 2a");
	}

	@Test
	void negativeRadicandsAreRefused() {
		ExactQuadraticTower tower = new ExactQuadraticTower(ExactEvaluationBudget.V1);
		assertThrows(ArithmeticException.class, () -> tower.sqrt(tower.of(-1)));
		assertTrue(tower.sqrt(tower.zero()).isZero());
	}

	@Test
	void elementsOfAnotherEvaluationAreRefused() {
		ExactQuadraticTower first = new ExactQuadraticTower(ExactEvaluationBudget.V1);
		ExactQuadraticTower second = new ExactQuadraticTower(ExactEvaluationBudget.V1);
		Element a = first.of(1);
		Element b = second.of(1);

		assertThrows(IllegalArgumentException.class, () -> a.add(b));
	}

	@Test
	void anInjectedOperationBudgetFailsClosedWithItsCounters() {
		ExactQuadraticTower tower = new ExactQuadraticTower(
				ExactEvaluationBudget.restrictedTo(8, ExactEvaluationBudget.MAXIMUM_DEPTH,
						ExactEvaluationBudget.MAXIMUM_BITS));
		Element value = tower.of(1);
		ExactEvaluationBudgetExhaustedException exhausted = assertThrows(
				ExactEvaluationBudgetExhaustedException.class, () -> {
					Element x = value;
					for (int i = 0; i < 9; i++) {
						x = x.add(value);
					}
				});

		assertEquals(ExactEvaluationBudgetExhaustedException.Limit.OPERATIONS,
				exhausted.getLimit());
		assertEquals(9, exhausted.getCounters().getOperations(), "9 > 8, as in V-M3-8");
		assertTrue(exhausted.getMessage().startsWith("EXACT_EVALUATION_BUDGET_EXHAUSTED"));
	}

	@Test
	void anInjectedDepthBudgetFailsClosed() {
		ExactQuadraticTower tower = new ExactQuadraticTower(
				ExactEvaluationBudget.restrictedTo(ExactEvaluationBudget.MAXIMUM_OPERATIONS, 1,
						ExactEvaluationBudget.MAXIMUM_BITS));
		tower.sqrt(tower.of(2));
		ExactEvaluationBudgetExhaustedException exhausted = assertThrows(
				ExactEvaluationBudgetExhaustedException.class, () -> tower.sqrt(tower.of(3)));

		assertEquals(ExactEvaluationBudgetExhaustedException.Limit.DEPTH, exhausted.getLimit());
		assertEquals(1, exhausted.getCounters().getDepth());
	}

	@Test
	void anInjectedBitBudgetFailsClosed() {
		ExactQuadraticTower tower = new ExactQuadraticTower(
				ExactEvaluationBudget.restrictedTo(ExactEvaluationBudget.MAXIMUM_OPERATIONS,
						ExactEvaluationBudget.MAXIMUM_DEPTH, 64));
		ExactEvaluationBudgetExhaustedException exhausted = assertThrows(
				ExactEvaluationBudgetExhaustedException.class,
				() -> tower.ofBinary64(Math.pow(2, 80)));

		assertEquals(ExactEvaluationBudgetExhaustedException.Limit.BITS, exhausted.getLimit());
	}

	@Test
	void theApprovedBudgetCanOnlyBeRestricted() {
		assertTrue(ExactEvaluationBudget.V1.isApproved());
		assertEquals(100_000L, ExactEvaluationBudget.V1.getMaximumOperations());
		assertEquals(8, ExactEvaluationBudget.V1.getMaximumDepth());
		assertEquals(4_096, ExactEvaluationBudget.V1.getMaximumBits());
		assertThrows(IllegalArgumentException.class,
				() -> ExactEvaluationBudget.restrictedTo(100_001L, 8, 4_096));
		assertThrows(IllegalArgumentException.class,
				() -> ExactEvaluationBudget.restrictedTo(100_000L, 9, 4_096));
		assertThrows(IllegalArgumentException.class,
				() -> ExactEvaluationBudget.restrictedTo(100_000L, 8, 4_097));
		assertFalse(ExactEvaluationBudget.restrictedTo(10, 8, 4_096).isApproved());
	}

	@Test
	void countersAreDeterministic() {
		assertEquals(sampleCounters().toString(), sampleCounters().toString());
		assertEquals(sampleCounters().getOperations(), sampleCounters().getOperations());
	}

	private static ExactEvaluationCounters sampleCounters() {
		ExactQuadraticTower tower = new ExactQuadraticTower(ExactEvaluationBudget.V1);
		Element root = tower.sqrt(tower.of(5));
		Element x = tower.of(3).add(root).divide(tower.of(2).subtract(root));
		x.signum();
		return tower.counters();
	}

	private static Element rational(ExactQuadraticTower tower, BigDecimal value) {
		BigDecimal stripped = value.stripTrailingZeros();
		java.math.BigInteger unscaled = stripped.unscaledValue();
		int scale = stripped.scale();
		return scale >= 0
				? tower.rational(ExactRational2D.of(unscaled, java.math.BigInteger.TEN.pow(scale)))
				: tower.rational(ExactRational2D.of(unscaled.multiply(
						java.math.BigInteger.TEN.pow(-scale)), java.math.BigInteger.ONE));
	}
}
