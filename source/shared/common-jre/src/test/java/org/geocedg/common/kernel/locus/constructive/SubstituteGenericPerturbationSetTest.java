/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geocedg.common.kernel.locus.exact.ExactRational2D;
import org.junit.jupiter.api.Test;

/**
 * The deterministic perturbation set of {@code SUBSTITUTE_GENERIC} version 1
 * ({@code locus-v2-constructive-authority.md} section 7.1): its size, scale, determinism and
 * general position, and the foundation of the adversarial row V-AUT-RLX-2, a nonzero cubic
 * through all {@code N + 1} points of the author model's case {@code m = 2}.
 */
final class SubstituteGenericPerturbationSetTest {

	@Test
	void theNumberOfPerturbationsFollowsTheVersion() {
		assertEquals(8, SubstituteGenericPerturbationSet.perturbationCount(1));
		assertEquals(8, SubstituteGenericPerturbationSet.perturbationCount(2));
		assertEquals(10, SubstituteGenericPerturbationSet.perturbationCount(3));
		assertEquals(15, SubstituteGenericPerturbationSet.perturbationCount(4));
		assertEquals(6, SubstituteGenericPerturbationSet.monomialCount(2));
	}

	@Test
	void theScaleIsAPowerOfTwoRelativeToTheCoordinate() {
		assertEquals(power(-30), SubstituteGenericPerturbationSet.scale(
				ExactRational2D.ofBinary64(0.375)));
		assertEquals(power(-30), SubstituteGenericPerturbationSet.scale(ExactRational2D.ONE));
		assertEquals(power(-28), SubstituteGenericPerturbationSet.scale(ExactRational2D.of(5)));
		assertEquals(power(-20), SubstituteGenericPerturbationSet.scale(
				ExactRational2D.of(-1024)));
	}

	@Test
	void thePerturbationSetIsDeterministic() {
		List<ExactRational2D> base = modelBoundary();
		SubstituteGenericPerturbationSet first = SubstituteGenericPerturbationSet.of(base);
		SubstituteGenericPerturbationSet second = SubstituteGenericPerturbationSet.of(base);

		assertNotNull(first);
		assertEquals(first.getPoints(), second.getPoints());
		assertEquals(first.getGeneratorOutputs(), second.getGeneratorOutputs());
		for (int k = 0; k < first.getDirections().size(); k++) {
			assertArrayEquals(first.getDirections().get(k), second.getDirections().get(k));
		}
	}

	@Test
	void everyDirectionMovesEveryCoordinateWithinTheVersionRange() {
		SubstituteGenericPerturbationSet set = SubstituteGenericPerturbationSet.of(
				modelBoundary());

		assertEquals(8, set.getDirections().size());
		for (long[] direction : set.getDirections()) {
			for (long value : direction) {
				assertNotEquals(0L, value, "zero outputs are discarded");
				assertTrue(value >= -1024 && value <= 1023, "output range of version 1");
			}
		}
		for (int k = 0; k < set.getPoints().size(); k++) {
			for (int i = 0; i < 2; i++) {
				ExactRational2D expected = modelBoundary().get(i).add(
						SubstituteGenericPerturbationSet.scale(modelBoundary().get(i))
								.multiply(ExactRational2D.of(set.getDirections().get(k)[i])));
				assertEquals(expected, set.getPoints().get(k).get(i));
			}
		}
	}

	@Test
	void theBaseAndItsPerturbationsLieOnNoQuadric() {
		SubstituteGenericPerturbationSet set = SubstituteGenericPerturbationSet.of(
				modelBoundary());
		List<long[]> points = withBase(set);

		assertEquals(6, rank(monomialMatrix(points, 2)),
				"no nonzero polynomial of degree at most 2 vanishes at the nine points");
	}

	@Test
	void aNonzeroCubicVanishesAtTheNinePoints() {
		SubstituteGenericPerturbationSet set = SubstituteGenericPerturbationSet.of(
				modelBoundary());
		List<long[]> points = withBase(set);
		BigInteger[][] cubic = monomialMatrix(points, 3);
		BigInteger[] coefficients = nullVector(cubic);

		assertNotNull(coefficients, "ten cubic monomials, nine conditions");
		assertTrue(Arrays.stream(coefficients).anyMatch(c -> c.signum() != 0));
		for (BigInteger[] row : cubic) {
			BigInteger value = BigInteger.ZERO;
			for (int c = 0; c < row.length; c++) {
				value = value.add(row[c].multiply(coefficients[c]));
			}
			assertEquals(BigInteger.ZERO, value,
					"a finite agreement is not an identity (V-AUT-RLX-2)");
		}
	}

	@Test
	void theCapOfGeneratorOutputsIsCounted() {
		SubstituteGenericPerturbationSet set = SubstituteGenericPerturbationSet.of(
				modelBoundary());
		assertTrue(set.getGeneratorOutputs() >= 16);
		assertTrue(set.getGeneratorOutputs()
				<= SubstituteGenericPerturbationSet.OUTPUTS_PER_PERTURBATION_CAP * 8);
	}

	/** Two boundary coordinates of the size of those of the author model ({@code m = 2}). */
	private static List<ExactRational2D> modelBoundary() {
		return Arrays.asList(ExactRational2D.ofBinary64(4.827722208159745),
				ExactRational2D.ofBinary64(9.351467396034236));
	}

	private static List<long[]> withBase(SubstituteGenericPerturbationSet set) {
		List<long[]> points = new ArrayList<>();
		points.add(new long[2]);
		points.addAll(set.getDirections());
		return points;
	}

	/** Monomials of degree at most {@code degree} in two variables, in graded order. */
	private static BigInteger[][] monomialMatrix(List<long[]> points, int degree) {
		BigInteger[][] matrix = new BigInteger[points.size()][];
		for (int r = 0; r < points.size(); r++) {
			BigInteger u = BigInteger.valueOf(points.get(r)[0]);
			BigInteger v = BigInteger.valueOf(points.get(r)[1]);
			List<BigInteger> row = new ArrayList<>();
			for (int total = 0; total <= degree; total++) {
				for (int i = total; i >= 0; i--) {
					row.add(u.pow(i).multiply(v.pow(total - i)));
				}
			}
			matrix[r] = row.toArray(new BigInteger[0]);
		}
		return matrix;
	}

	private static int rank(BigInteger[][] matrix) {
		return eliminate(copy(matrix)).size();
	}

	private static BigInteger[] nullVector(BigInteger[][] matrix) {
		BigInteger[][] work = copy(matrix);
		List<Integer> pivots = eliminate(work);
		int width = work[0].length;
		int free = -1;
		for (int c = 0; c < width; c++) {
			if (!pivots.contains(c)) {
				free = c;
				break;
			}
		}
		if (free < 0) {
			return null;
		}
		// Back substitution in reduced row echelon form over the rationals.
		ExactRational2D[] solution = new ExactRational2D[width];
		Arrays.fill(solution, ExactRational2D.ZERO);
		solution[free] = ExactRational2D.ONE;
		for (int r = pivots.size() - 1; r >= 0; r--) {
			int pivot = pivots.get(r);
			ExactRational2D sum = ExactRational2D.ZERO;
			for (int c = pivot + 1; c < width; c++) {
				sum = sum.add(ExactRational2D.of(work[r][c], BigInteger.ONE)
						.multiply(solution[c]));
			}
			solution[pivot] = sum.negate().divide(ExactRational2D.of(work[r][pivot],
					BigInteger.ONE));
		}
		BigInteger common = BigInteger.ONE;
		for (ExactRational2D value : solution) {
			common = common.multiply(value.denominator()).divide(
					common.gcd(value.denominator()));
		}
		BigInteger[] integers = new BigInteger[width];
		for (int c = 0; c < width; c++) {
			integers[c] = solution[c].numerator().multiply(common)
					.divide(solution[c].denominator());
		}
		return integers;
	}

	/** Fraction-free row echelon form in place; returns the pivot columns in row order. */
	private static List<Integer> eliminate(BigInteger[][] m) {
		int rows = m.length;
		int width = m[0].length;
		List<Integer> pivots = new ArrayList<>();
		int r = 0;
		for (int c = 0; c < width && r < rows; c++) {
			int found = -1;
			for (int i = r; i < rows; i++) {
				if (m[i][c].signum() != 0) {
					found = i;
					break;
				}
			}
			if (found < 0) {
				continue;
			}
			BigInteger[] swap = m[r];
			m[r] = m[found];
			m[found] = swap;
			for (int i = 0; i < rows; i++) {
				if (i == r || m[i][c].signum() == 0) {
					continue;
				}
				BigInteger factor = m[i][c];
				BigInteger scale = m[r][c];
				for (int k = 0; k < width; k++) {
					m[i][k] = m[i][k].multiply(scale).subtract(m[r][k].multiply(factor));
				}
			}
			pivots.add(c);
			r++;
		}
		return pivots;
	}

	private static BigInteger[][] copy(BigInteger[][] matrix) {
		BigInteger[][] result = new BigInteger[matrix.length][];
		for (int r = 0; r < matrix.length; r++) {
			result[r] = matrix[r].clone();
		}
		return result;
	}

	private static ExactRational2D power(int exponent) {
		return exponent >= 0 ? ExactRational2D.of(BigInteger.ONE.shiftLeft(exponent),
				BigInteger.ONE)
				: ExactRational2D.of(BigInteger.ONE, BigInteger.ONE.shiftLeft(-exponent));
	}
}
