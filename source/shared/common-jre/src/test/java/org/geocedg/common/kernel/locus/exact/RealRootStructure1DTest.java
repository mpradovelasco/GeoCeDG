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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import org.geocedg.common.kernel.locus.exact.RealRootStructure1D.Root;
import org.geocedg.common.kernel.locus.exact.RealRootStructure1D.RootKind;
import org.junit.jupiter.api.Test;

/** Exact isolation, multiplicities, side signs and budgets of {@link RealRootStructure1D}. */
final class RealRootStructure1DTest {
	private static final long SEED = 20261021L;
	private static final ExactRational2D WIDTH = powerOfTwo(-40);

	@Test
	void simpleRootsAreIsolatedWithAlternatingGaps() {
		RealRootStructure1D structure = RealRootStructure1D.isolate(
				rootsAt(-3, 1, 2), WIDTH, ExactBudget1D.DEFAULT);

		assertTrue(structure.isWholeLine());
		assertNull(structure.lowerBound());
		assertNull(structure.upperBound());
		assertEquals(3, structure.roots().size());
		assertContains(structure.roots().get(0), r(-3));
		assertContains(structure.roots().get(1), r(1));
		assertContains(structure.roots().get(2), r(2));
		for (Root root : structure.roots()) {
			assertEquals(1, root.multiplicity());
			assertEquals(RootKind.TRANSITION, root.kind());
			assertWidth(root, WIDTH);
		}
		assertEquals(Arrays.asList(-1, 1, -1, 1), structure.gapSigns());
		assertTrue(structure.bisections() > 0);
		assertTrue(structure.maximumCoefficientBitLength() > 0);
		assertThrows(UnsupportedOperationException.class,
				() -> structure.roots().remove(0));
	}

	@Test
	void irrationalRootsRespectTheWidthBound() {
		RealRootStructure1D structure = RealRootStructure1D.isolate(poly(-2, 0, 1), WIDTH,
				ExactBudget1D.DEFAULT);

		assertEquals(2, structure.roots().size());
		Root positive = structure.roots().get(1);
		assertFalse(positive.isExact());
		assertTrue(positive.lower().multiply(positive.lower()).compareTo(r(2)) < 0);
		assertTrue(positive.upper().multiply(positive.upper()).compareTo(r(2)) > 0);
		assertTrue(positive.lower().signum() > 0);
		assertWidth(positive, WIDTH);
		assertWidth(structure.roots().get(0), WIDTH);
		assertEquals(Arrays.asList(1, -1, 1), structure.gapSigns());
		assertEquals(-1, positive.signLeft());
		assertEquals(1, positive.signRight());
	}

	@Test
	void doubleRootWithPositiveSidesIsSingularInterior() {
		ExactPolynomial1D p = linear(1, 3).pow(2).multiply(poly(1, 1));

		RealRootStructure1D structure = RealRootStructure1D.isolate(p, WIDTH,
				ExactBudget1D.DEFAULT);

		assertEquals(2, structure.roots().size());
		Root simple = structure.roots().get(0);
		Root twice = structure.roots().get(1);
		assertContains(simple, r(-1));
		assertEquals(1, simple.multiplicity());
		assertEquals(RootKind.TRANSITION, simple.kind());
		assertContains(twice, ExactRational2D.of(1, 3));
		assertFalse(twice.isExact());
		assertEquals(2, twice.multiplicity());
		assertEquals(1, twice.signLeft());
		assertEquals(1, twice.signRight());
		assertEquals(RootKind.SINGULAR_INTERIOR, twice.kind());
		assertEquals(Arrays.asList(-1, 1, 1), structure.gapSigns());
	}

	@Test
	void doubleRootWithNegativeSidesIsAnIsolatedPoint() {
		ExactPolynomial1D p = linear(1, 3).pow(2).multiply(poly(1, 0, 1)).negate();

		RealRootStructure1D structure = RealRootStructure1D.isolate(p, WIDTH,
				ExactBudget1D.DEFAULT);

		assertEquals(1, structure.roots().size());
		Root root = structure.roots().get(0);
		assertContains(root, ExactRational2D.of(1, 3));
		assertEquals(2, root.multiplicity());
		assertEquals(-1, root.signLeft());
		assertEquals(-1, root.signRight());
		assertEquals(RootKind.ISOLATED_POINT, root.kind());
		assertEquals(Arrays.asList(-1, -1), structure.gapSigns());
	}

	@Test
	void tripleRootChangesSign() {
		RealRootStructure1D rising = RealRootStructure1D.isolate(linear(2, 3).pow(3), WIDTH,
				ExactBudget1D.DEFAULT);
		RealRootStructure1D falling = RealRootStructure1D.isolate(
				linear(2, 3).pow(3).negate(), WIDTH, ExactBudget1D.DEFAULT);

		Root up = rising.roots().get(0);
		Root down = falling.roots().get(0);
		assertEquals(3, up.multiplicity());
		assertContains(up, ExactRational2D.of(2, 3));
		assertEquals(-1, up.signLeft());
		assertEquals(1, up.signRight());
		assertEquals(RootKind.TRANSITION, up.kind());
		assertEquals(1, down.signLeft());
		assertEquals(-1, down.signRight());
		assertEquals(RootKind.TRANSITION, down.kind());
	}

	@Test
	void clusteredRootsGetDisjointIntervalsWithTheDefaultBudget() {
		ExactRational2D half = ExactRational2D.of(1, 2);
		ExactRational2D next = half.add(powerOfTwo(-100));
		ExactPolynomial1D p = linear(half).multiply(linear(next));

		RealRootStructure1D structure = RealRootStructure1D.isolate(p, WIDTH,
				ExactBudget1D.DEFAULT);

		assertEquals(2, structure.roots().size());
		Root first = structure.roots().get(0);
		Root second = structure.roots().get(1);
		assertContains(first, half);
		assertContains(second, next);
		assertTrue(first.upper().compareTo(second.lower()) < 0);
		assertEquals(1, first.multiplicity());
		assertEquals(1, second.multiplicity());
		assertEquals(Arrays.asList(1, -1, 1), structure.gapSigns());
		assertTrue(structure.maximumRootBisections() > 100);
		assertTrue(structure.maximumRootBisections()
				<= ExactBudget1D.DEFAULT_MAXIMUM_BISECTIONS_PER_ROOT);
	}

	@Test
	void rootsStraddlingASplitPointGetStrictlyDisjointIntervals() {
		// the split point 1/2 separates the roots early; it is not a root and must not
		// remain a common end of both closed intervals
		ExactRational2D half = ExactRational2D.of(1, 2);
		ExactRational2D below = half.subtract(powerOfTwo(-60));
		ExactRational2D above = half.add(powerOfTwo(-60).multiply(ExactRational2D.of(1, 3)));
		ExactPolynomial1D p = linear(below).multiply(linear(above));

		RealRootStructure1D structure = RealRootStructure1D.isolate(p, WIDTH,
				ExactBudget1D.DEFAULT);

		assertEquals(2, structure.roots().size());
		Root first = structure.roots().get(0);
		Root second = structure.roots().get(1);
		assertContains(first, below);
		assertContains(second, above);
		assertTrue(first.upper().compareTo(second.lower()) < 0);
		assertWidth(first, WIDTH);
		assertWidth(second, WIDTH);
		assertEquals(Arrays.asList(1, -1, 1), structure.gapSigns());
	}

	@Test
	void exhaustedBudgetsThrowTypedReasons() {
		ExactRational2D half = ExactRational2D.of(1, 2);
		ExactPolynomial1D clustered = linear(half).multiply(linear(half.add(powerOfTwo(-100))));

		ExactBudgetExhaustedException bits = assertThrows(ExactBudgetExhaustedException.class,
				() -> RealRootStructure1D.isolate(clustered, WIDTH, ExactBudget1D.of(16, 512)));
		ExactBudgetExhaustedException bisections = assertThrows(
				ExactBudgetExhaustedException.class,
				() -> RealRootStructure1D.isolate(clustered, WIDTH, ExactBudget1D.of(4096, 8)));
		ExactBudgetExhaustedException width = assertThrows(ExactBudgetExhaustedException.class,
				() -> RealRootStructure1D.isolate(poly(-2, 0, 1), powerOfTwo(-600),
						ExactBudget1D.DEFAULT));

		assertEquals(ExactBudgetExhaustedException.Reason.COEFFICIENT_BITS, bits.reason());
		assertEquals(16, bits.limit());
		assertTrue(bits.required() > 16);
		assertEquals(ExactBudgetExhaustedException.Reason.BISECTIONS, bisections.reason());
		assertEquals(8, bisections.limit());
		assertEquals(9, bisections.required());
		assertEquals(ExactBudgetExhaustedException.Reason.BISECTIONS, width.reason());
		assertEquals(ExactBudget1D.DEFAULT_MAXIMUM_BISECTIONS_PER_ROOT, width.limit());
		assertThrows(IllegalArgumentException.class, () -> ExactBudget1D.of(0, 1));
		assertThrows(IllegalArgumentException.class, () -> ExactBudget1D.of(1, -1));
		assertEquals(ExactBudget1D.DEFAULT, ExactBudget1D.of(4096, 512));
	}

	@Test
	void rootsAtTheEndsOfAClosedIntervalAreExact() {
		ExactPolynomial1D p = poly(0, 1).multiply(poly(-1, 1)).multiply(linear(1, 2));

		RealRootStructure1D structure = RealRootStructure1D.isolateIn(p, r(0), r(1), WIDTH,
				ExactBudget1D.DEFAULT);

		assertFalse(structure.isWholeLine());
		assertEquals(r(0), structure.lowerBound());
		assertEquals(r(1), structure.upperBound());
		assertEquals(3, structure.roots().size());
		Root lowerEnd = structure.roots().get(0);
		Root middle = structure.roots().get(1);
		Root upperEnd = structure.roots().get(2);
		assertTrue(lowerEnd.isExact());
		assertEquals(r(0), lowerEnd.lower());
		assertEquals(0, lowerEnd.bisections());
		assertTrue(middle.isExact());
		assertEquals(ExactRational2D.of(1, 2), middle.lower());
		assertTrue(upperEnd.isExact());
		assertEquals(r(1), upperEnd.upper());
		assertEquals(0, upperEnd.bisections());
		assertEquals(Arrays.asList(0, 1, -1, 0), structure.gapSigns());
		assertEquals(0, lowerEnd.signLeft());
		assertEquals(1, lowerEnd.signRight());
		assertEquals(RootKind.TRANSITION, lowerEnd.kind());
		assertEquals(0, upperEnd.signRight());
		assertEquals(RootKind.TRANSITION, upperEnd.kind());
	}

	@Test
	void nonDyadicEndsAndARootNextToAnEndAreHandledExactly() {
		ExactRational2D third = ExactRational2D.of(1, 3);
		ExactRational2D twoThirds = ExactRational2D.of(2, 3);
		ExactPolynomial1D p = linear(third).multiply(linear(twoThirds))
				.multiply(linear(1, 2));
		ExactRational2D close = third.add(ExactRational2D.of(BigInteger.ONE,
				BigInteger.ONE.shiftLeft(60).multiply(BigInteger.valueOf(3))));
		ExactPolynomial1D q = linear(third).multiply(linear(close));

		RealRootStructure1D ends = RealRootStructure1D.isolateIn(p, third, twoThirds, WIDTH,
				ExactBudget1D.DEFAULT);
		RealRootStructure1D neighbours = RealRootStructure1D.isolateIn(q, third, r(1), WIDTH,
				ExactBudget1D.DEFAULT);

		assertEquals(3, ends.roots().size());
		assertEquals(third, ends.roots().get(0).lower());
		assertEquals(ExactRational2D.of(1, 2), ends.roots().get(1).lower());
		assertEquals(twoThirds, ends.roots().get(2).upper());
		assertEquals(2, neighbours.roots().size());
		assertTrue(neighbours.roots().get(0).isExact());
		Root second = neighbours.roots().get(1);
		assertContains(second, close);
		assertTrue(second.lower().compareTo(third) > 0);
		assertEquals(Arrays.asList(0, -1, 1), neighbours.gapSigns());
	}

	@Test
	void evenRootAtAnEndUsesTheInnerSide() {
		ExactPolynomial1D p = poly(0, 0, 1).multiply(poly(-2, 1));

		RealRootStructure1D structure = RealRootStructure1D.isolateIn(p, r(0), r(1), WIDTH,
				ExactBudget1D.DEFAULT);

		assertEquals(1, structure.roots().size());
		Root root = structure.roots().get(0);
		assertEquals(2, root.multiplicity());
		assertEquals(0, root.signLeft());
		assertEquals(-1, root.signRight());
		assertEquals(RootKind.ISOLATED_POINT, root.kind());
		assertEquals(Arrays.asList(0, -1), structure.gapSigns());
	}

	@Test
	void domainsWithoutRootsHaveASingleGap() {
		assertEquals(Arrays.asList(1), RealRootStructure1D.isolateIn(poly(1, 0, 1), r(-1),
				r(1), WIDTH, ExactBudget1D.DEFAULT).gapSigns());
		RealRootStructure1D constant = RealRootStructure1D.isolate(poly(-5), WIDTH,
				ExactBudget1D.DEFAULT);
		assertTrue(constant.roots().isEmpty());
		assertEquals(Arrays.asList(-1), constant.gapSigns());
		assertEquals(0, constant.bisections());
		assertEquals(Arrays.asList(-1), RealRootStructure1D.isolateIn(poly(-5), r(0), r(1),
				WIDTH, ExactBudget1D.DEFAULT).gapSigns());
		assertEquals(Arrays.asList(1), RealRootStructure1D.isolate(poly(3, 0, 1), WIDTH,
				ExactBudget1D.DEFAULT).gapSigns());
	}

	@Test
	void invalidArgumentsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> RealRootStructure1D.isolate(
				ExactPolynomial1D.ZERO, WIDTH, ExactBudget1D.DEFAULT));
		assertThrows(IllegalArgumentException.class, () -> RealRootStructure1D.isolate(
				poly(1, 1), ExactRational2D.ZERO, ExactBudget1D.DEFAULT));
		assertThrows(IllegalArgumentException.class, () -> RealRootStructure1D.isolate(
				poly(1, 1), r(-1), ExactBudget1D.DEFAULT));
		assertThrows(IllegalArgumentException.class, () -> RealRootStructure1D.isolateIn(
				poly(1, 1), r(1), r(1), WIDTH, ExactBudget1D.DEFAULT));
		assertThrows(IllegalArgumentException.class, () -> RealRootStructure1D.isolateIn(
				poly(1, 1), r(2), r(1), WIDTH, ExactBudget1D.DEFAULT));
	}

	@Test
	void resultsAreDeterministic() {
		List<ExactPolynomial1D> polynomials = Arrays.asList(rootsAt(-3, 1, 2),
				poly(-2, 0, 1), linear(1, 3).pow(2).multiply(poly(1, 1)),
				linear(ExactRational2D.of(1, 2)).multiply(linear(ExactRational2D.of(1, 2)
						.add(powerOfTwo(-100)))));
		for (ExactPolynomial1D p : polynomials) {
			RealRootStructure1D first = RealRootStructure1D.isolate(p, WIDTH,
					ExactBudget1D.DEFAULT);
			RealRootStructure1D second = RealRootStructure1D.isolate(p, WIDTH,
					ExactBudget1D.DEFAULT);
			assertEquals(first, second);
			assertEquals(first.hashCode(), second.hashCode());
			assertEquals(first.toString(), second.toString());
			assertEquals(first.roots(), second.roots());
		}
	}

	@Test
	void plantedRootsAreFoundWithTheirMultiplicities() {
		Random random = new Random(SEED);
		ExactRational2D width = powerOfTwo(-20);
		for (int trial = 0; trial < 40; trial++) {
			TreeMap<ExactRational2D, Integer> planted = new TreeMap<>();
			ExactPolynomial1D p = poly(1 + random.nextInt(4), 0, 1)
					.scale(ExactRational2D.of(random.nextBoolean() ? 3 : -2, 7));
			int count = 1 + random.nextInt(5);
			while (planted.size() < count) {
				ExactRational2D root = ExactRational2D.of(random.nextInt(201) - 100,
						1 + random.nextInt(9));
				if (!planted.containsKey(root)) {
					int multiplicity = 1 + random.nextInt(3);
					planted.put(root, multiplicity);
					p = p.multiply(linear(root).pow(multiplicity));
				}
			}

			RealRootStructure1D structure = RealRootStructure1D.isolate(p, width,
					ExactBudget1D.DEFAULT);

			List<ExactRational2D> expected = new ArrayList<>(planted.keySet());
			assertEquals(expected.size(), structure.roots().size(), p.toString());
			for (int i = 0; i < expected.size(); i++) {
				Root root = structure.roots().get(i);
				assertContains(root, expected.get(i));
				assertWidth(root, width);
				assertEquals((int) planted.get(expected.get(i)), root.multiplicity());
				if (i > 0) {
					assertTrue(structure.roots().get(i - 1).upper().compareTo(root.lower()) < 0);
				}
				if (i + 1 < expected.size()) {
					ExactRational2D between = ExactRational2D.midpoint(expected.get(i),
							expected.get(i + 1));
					assertEquals(p.signAt(between), (int) structure.gapSigns().get(i + 1));
				}
			}
			assertEquals(p.signAtNegativeInfinity(), (int) structure.gapSigns().get(0));
			assertEquals(p.signAtPositiveInfinity(),
					(int) structure.gapSigns().get(expected.size()));
		}
	}

	private static void assertContains(Root root, ExactRational2D value) {
		assertTrue(root.lower().compareTo(value) <= 0, root + " vs " + value);
		assertTrue(root.upper().compareTo(value) >= 0, root + " vs " + value);
	}

	private static void assertWidth(Root root, ExactRational2D bound) {
		assertTrue(root.upper().subtract(root.lower()).compareTo(bound) <= 0, root.toString());
	}

	private static ExactRational2D powerOfTwo(int exponent) {
		BigInteger power = BigInteger.ONE.shiftLeft(Math.abs(exponent));
		return exponent >= 0 ? ExactRational2D.of(power, BigInteger.ONE)
				: ExactRational2D.of(BigInteger.ONE, power);
	}

	private static ExactPolynomial1D linear(ExactRational2D root) {
		return ExactPolynomial1D.of(root.negate(), ExactRational2D.ONE);
	}

	private static ExactPolynomial1D linear(long numerator, long denominator) {
		return linear(ExactRational2D.of(numerator, denominator));
	}

	private static ExactPolynomial1D rootsAt(long... roots) {
		ExactPolynomial1D p = ExactPolynomial1D.ONE;
		for (long root : roots) {
			p = p.multiply(poly(-root, 1));
		}
		return p;
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
