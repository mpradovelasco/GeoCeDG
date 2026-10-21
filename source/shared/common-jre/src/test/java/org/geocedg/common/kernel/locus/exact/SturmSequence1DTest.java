/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

/** Exact distinct-root counts of {@link SturmSequence1D} on half-open intervals. */
final class SturmSequence1DTest {
	private static final long SEED = 20261021L;

	@Test
	void countsIncludeTheUpperEndAndExcludeTheLowerEnd() {
		SturmSequence1D sturm = SturmSequence1D.of(rootsAt(1, 2, 3));

		assertEquals(3, sturm.countAllRealRoots());
		assertEquals(3, sturm.countDistinctRootsIn(null, null));
		assertEquals(1, sturm.countDistinctRootsIn(r(0), r(1)));
		assertEquals(1, sturm.countDistinctRootsIn(r(1), r(2)));
		assertEquals(2, sturm.countDistinctRootsIn(r(1), r(3)));
		assertEquals(0, sturm.countDistinctRootsIn(r(3), r(4)));
		assertEquals(1, sturm.countDistinctRootsIn(null, ExactRational2D.of(3, 2)));
		assertEquals(1, sturm.countDistinctRootsIn(r(2), null));
		assertEquals(3, sturm.countDistinctRootsIn(r(0), null));
		assertEquals(0, sturm.countDistinctRootsIn(r(2), r(2)));
		assertThrows(IllegalArgumentException.class,
				() -> sturm.countDistinctRootsIn(r(3), r(1)));
	}

	@Test
	void sequenceEndsWithANonzeroConstant() {
		SturmSequence1D sturm = SturmSequence1D.of(rootsAt(1, 2, 3).scale(r(-7)));
		List<ExactPolynomial1D> elements = sturm.polynomials();

		assertEquals(0, elements.get(elements.size() - 1).degree());
		assertEquals(3, elements.get(0).degree());
		assertEquals(-1, elements.get(0).signAtPositiveInfinity());
		assertEquals(sturm.polynomial(), elements.get(0));
		assertEquals(3, sturm.countAllRealRoots());
		assertThrows(UnsupportedOperationException.class,
				() -> elements.add(ExactPolynomial1D.ONE));
	}

	@Test
	void polynomialsWithoutRealRootsCountNothing() {
		SturmSequence1D sturm = SturmSequence1D.of(poly(1, 0, 1));

		assertEquals(0, sturm.countAllRealRoots());
		assertEquals(0, sturm.countDistinctRootsIn(r(-5), r(5)));
		assertEquals(0, SturmSequence1D.of(poly(4)).countAllRealRoots());
		assertThrows(IllegalArgumentException.class,
				() -> SturmSequence1D.of(ExactPolynomial1D.ZERO));
	}

	@Test
	void nonSquareFreeInputCountsDistinctRoots() {
		ExactPolynomial1D p = poly(-1, 1).pow(2).multiply(poly(1, 1).pow(3));
		SturmSequence1D sturm = SturmSequence1D.of(p);

		assertEquals(2, sturm.countAllRealRoots());
		assertEquals(1, sturm.countDistinctRootsIn(r(-1), r(1)));
		assertEquals(1, sturm.countDistinctRootsIn(r(-2), r(-1)));
		assertEquals(2, sturm.polynomial().degree());
	}

	@Test
	void countsMatchPlantedRationalRoots() {
		Random random = new Random(SEED);
		for (int trial = 0; trial < 40; trial++) {
			TreeSet<ExactRational2D> roots = new TreeSet<>();
			ExactPolynomial1D p = ExactPolynomial1D.ONE;
			int count = 1 + random.nextInt(6);
			while (roots.size() < count) {
				ExactRational2D root = ExactRational2D.of(random.nextInt(81) - 40,
						1 + random.nextInt(8));
				if (roots.add(root)) {
					p = p.multiply(ExactPolynomial1D.of(root.negate(), ExactRational2D.ONE)
							.pow(1 + random.nextInt(3)));
				}
			}
			p = p.multiply(poly(1 + random.nextInt(5), 0, 1));
			SturmSequence1D sturm = SturmSequence1D.of(p);
			assertEquals(count, sturm.countAllRealRoots());
			for (int query = 0; query < 20; query++) {
				ExactRational2D a = ExactRational2D.of(random.nextInt(97) - 48,
						1 + random.nextInt(8));
				ExactRational2D b = a.add(ExactRational2D.of(random.nextInt(40),
						1 + random.nextInt(4)));
				int expected = roots.subSet(a, false, b, true).size();
				assertEquals(expected, sturm.countDistinctRootsIn(a, b),
						p + " on " + a + ", " + b);
			}
		}
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
