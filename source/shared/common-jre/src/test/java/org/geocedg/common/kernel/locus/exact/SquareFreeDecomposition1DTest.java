/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

/** Yun's square-free decomposition and its reconstruction identity. */
final class SquareFreeDecomposition1DTest {
	private static final long SEED = 20261021L;

	@Test
	void doubleAndSimpleRootsAreSeparated() {
		ExactPolynomial1D xMinusOne = poly(-1, 1);
		ExactPolynomial1D xPlusTwo = poly(2, 1);
		ExactPolynomial1D p = xMinusOne.pow(2).multiply(xPlusTwo);

		SquareFreeDecomposition1D decomposition = SquareFreeDecomposition1D.of(p);

		assertEquals(Arrays.asList(xPlusTwo, xMinusOne), decomposition.factors());
		assertEquals(ExactRational2D.ONE, decomposition.content());
		assertEquals(xMinusOne.multiply(xPlusTwo), decomposition.squareFreePart());
		assertEquals(2, decomposition.maximumMultiplicity());
		assertEquals(p, decomposition.polynomial());
		assertReconstruction(decomposition);
	}

	@Test
	void missingMultiplicitiesAreConstantFactors() {
		ExactPolynomial1D sumOfSquares = poly(1, 0, 1);
		ExactPolynomial1D p = sumOfSquares.pow(3).multiply(ExactPolynomial1D.X);

		SquareFreeDecomposition1D decomposition = SquareFreeDecomposition1D.of(p);

		assertEquals(Arrays.asList(ExactPolynomial1D.X, ExactPolynomial1D.ONE, sumOfSquares),
				decomposition.factors());
		assertReconstruction(decomposition);
	}

	@Test
	void contentIsTheLeadingCoefficient() {
		ExactPolynomial1D root = ExactPolynomial1D.of(ExactRational2D.of(-1, 2),
				ExactRational2D.ONE);
		ExactPolynomial1D p = root.pow(3).scale(ExactRational2D.of(5));

		SquareFreeDecomposition1D decomposition = SquareFreeDecomposition1D.of(p);

		assertEquals(ExactRational2D.of(5), decomposition.content());
		assertEquals(Arrays.asList(ExactPolynomial1D.ONE, ExactPolynomial1D.ONE, root),
				decomposition.factors());
		assertEquals(root, decomposition.squareFreePart());
		assertReconstruction(decomposition);
	}

	@Test
	void constantsHaveNoFactorsAndZeroIsRejected() {
		SquareFreeDecomposition1D constant = SquareFreeDecomposition1D.of(poly(-3));

		assertTrue(constant.factors().isEmpty());
		assertEquals(ExactRational2D.of(-3), constant.content());
		assertEquals(ExactPolynomial1D.ONE, constant.squareFreePart());
		assertEquals(0, constant.maximumMultiplicity());
		assertReconstruction(constant);
		assertThrows(IllegalArgumentException.class,
				() -> SquareFreeDecomposition1D.of(ExactPolynomial1D.ZERO));
		assertThrows(UnsupportedOperationException.class,
				() -> constant.factors().add(ExactPolynomial1D.ONE));
	}

	@Test
	void randomProductsAreReconstructedExactly() {
		Random random = new Random(SEED);
		for (int trial = 0; trial < 60; trial++) {
			int maximumMultiplicity = 1 + random.nextInt(4);
			List<ExactPolynomial1D> expected = new ArrayList<>();
			for (int i = 0; i < maximumMultiplicity; i++) {
				expected.add(ExactPolynomial1D.ONE);
			}
			ExactRational2D content = ExactRational2D.of(1 + random.nextInt(9),
					1 + random.nextInt(5));
			if (random.nextBoolean()) {
				content = content.negate();
			}
			ExactPolynomial1D p = ExactPolynomial1D.ONE.scale(content);
			TreeSet<ExactRational2D> usedRoots = new TreeSet<>();
			TreeSet<Integer> usedSquares = new TreeSet<>();
			int factorCount = 1 + random.nextInt(4);
			for (int k = 0; k < factorCount; k++) {
				int multiplicity = 1 + random.nextInt(maximumMultiplicity);
				ExactPolynomial1D factor = random.nextInt(3) == 0
						? quadratic(random, usedSquares) : linear(random, usedRoots);
				p = p.multiply(factor.pow(multiplicity));
				expected.set(multiplicity - 1, expected.get(multiplicity - 1).multiply(factor));
			}
			while (expected.get(expected.size() - 1).degree() == 0) {
				expected.remove(expected.size() - 1);
			}

			SquareFreeDecomposition1D decomposition = SquareFreeDecomposition1D.of(p);

			assertEquals(expected, decomposition.factors(), p.toString());
			assertEquals(content, decomposition.content());
			assertReconstruction(decomposition);
			assertEquals(decomposition, SquareFreeDecomposition1D.of(p));
		}
	}

	/** Monic x - root with a root not used before. */
	private static ExactPolynomial1D linear(Random random, TreeSet<ExactRational2D> used) {
		ExactRational2D root;
		do {
			root = ExactRational2D.of(random.nextInt(41) - 20, 1 + random.nextInt(7));
		} while (!used.add(root));
		return ExactPolynomial1D.of(root.negate(), ExactRational2D.ONE);
	}

	/** Monic x^2 + c with c > 0, irreducible over the rationals and not used before. */
	private static ExactPolynomial1D quadratic(Random random, TreeSet<Integer> used) {
		int constant;
		do {
			constant = 1 + random.nextInt(30);
		} while (!used.add(constant));
		return poly(constant, 0, 1);
	}

	private static void assertReconstruction(SquareFreeDecomposition1D decomposition) {
		List<ExactPolynomial1D> factors = decomposition.factors();
		ExactPolynomial1D product = ExactPolynomial1D.ONE.scale(decomposition.content());
		ExactPolynomial1D squareFree = ExactPolynomial1D.ONE;
		for (int i = 0; i < factors.size(); i++) {
			ExactPolynomial1D factor = factors.get(i);
			assertEquals(ExactRational2D.ONE, factor.leadingCoefficient());
			if (factor.degree() > 0) {
				assertEquals(ExactPolynomial1D.ONE, factor.gcd(factor.derivative()));
			}
			for (int j = 0; j < i; j++) {
				assertEquals(ExactPolynomial1D.ONE, factor.gcd(factors.get(j)));
			}
			product = product.multiply(factor.pow(i + 1));
			squareFree = squareFree.multiply(factor);
		}
		assertEquals(decomposition.polynomial(), product);
		assertEquals(squareFree, decomposition.squareFreePart());
		if (!factors.isEmpty()) {
			assertTrue(factors.get(factors.size() - 1).degree() > 0);
		}
	}

	private static ExactPolynomial1D poly(long... coefficients) {
		ExactRational2D[] values = new ExactRational2D[coefficients.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = ExactRational2D.of(coefficients[i]);
		}
		return ExactPolynomial1D.of(values);
	}
}
