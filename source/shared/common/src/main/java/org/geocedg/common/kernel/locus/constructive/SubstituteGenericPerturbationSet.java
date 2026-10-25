/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.exact.ExactRational2D;

/**
 * The deterministic perturbation set of {@code SUBSTITUTE_GENERIC} version 1
 * ({@code locus-v2-constructive-authority.md} section 7.1). For the exact boundary coordinates
 * {@code z} it gives the {@code N} dyadic points {@code z + rho o d_k}: the directions come in
 * order from SplitMix64 with the fixed seed of the version, each output mapped to
 * {@code (x >>> 53) - 1024} with zeros discarded, and a direction is accepted only if it raises
 * the rank of the matrix of the monomials of degree at most 2 while that rank is not full, so
 * that the {@code N + 1} points never lie on one quadric. Since every {@code rho_i} is a
 * positive power of two, that rank is computed on the integer directions themselves, with the
 * base point at the origin: an invertible affine map sends quadrics to quadrics.
 */
public final class SubstituteGenericPerturbationSet {

	/** Policy version of this rule. */
	public static final String VERSION = "substitute-generic/v1";
	/** SplitMix64 seed of version 1. */
	public static final long SEED = 0x9E3779B97F4A7C15L;
	/** Exponent offset of the scale {@code rho_i = 2^(e_i - 30)}. */
	public static final int SCALE_EXPONENT_OFFSET = 30;
	/** Minimum number of perturbations. */
	public static final int MINIMUM_PERTURBATIONS = 8;
	/** Cap of generator outputs per perturbation. */
	public static final int OUTPUTS_PER_PERTURBATION_CAP = 64;

	private final List<ExactRational2D> base;
	private final List<long[]> directions;
	private final List<List<ExactRational2D>> points;
	private final int generatorOutputs;

	private SubstituteGenericPerturbationSet(List<ExactRational2D> base,
			List<long[]> directions, List<List<ExactRational2D>> points,
			int generatorOutputs) {
		this.base = base;
		this.directions = directions;
		this.points = points;
		this.generatorOutputs = generatorOutputs;
	}

	/**
	 * Builds the perturbation set of version 1.
	 *
	 * @param boundaryCoordinates exact values of the {@code m >= 1} boundary coordinates
	 * @return the set, or {@code null} when the cap of {@code 64 N} generator outputs is
	 *         exhausted before {@code N} directions are accepted
	 */
	public static SubstituteGenericPerturbationSet of(
			List<ExactRational2D> boundaryCoordinates) {
		Objects.requireNonNull(boundaryCoordinates);
		int m = boundaryCoordinates.size();
		if (m == 0) {
			throw new IllegalArgumentException("SUBSTITUTE_GENERIC needs a boundary coordinate");
		}
		int n = perturbationCount(m);
		int monomials = monomialCount(m);
		int cap = OUTPUTS_PER_PERTURBATION_CAP * n;
		ExactRational2D[] scales = new ExactRational2D[m];
		for (int i = 0; i < m; i++) {
			scales[i] = scale(boundaryCoordinates.get(i));
		}
		SplitMix64 generator = new SplitMix64(SEED);
		RowSpace rank = new RowSpace(monomials);
		rank.add(monomialRow(new long[m]));
		List<long[]> accepted = new ArrayList<>();
		int outputs = 0;
		while (accepted.size() < n) {
			long[] direction = new long[m];
			for (int i = 0; i < m; i++) {
				long value = 0;
				while (value == 0) {
					if (outputs >= cap) {
						return null;
					}
					value = (generator.next() >>> 53) - 1024;
					outputs++;
				}
				direction[i] = value;
			}
			if (contains(accepted, direction)) {
				continue;
			}
			if (rank.rank() < monomials && !rank.add(monomialRow(direction))) {
				continue;
			}
			accepted.add(direction);
		}
		List<List<ExactRational2D>> perturbed = new ArrayList<>(n);
		for (long[] direction : accepted) {
			List<ExactRational2D> point = new ArrayList<>(m);
			for (int i = 0; i < m; i++) {
				point.add(boundaryCoordinates.get(i).add(
						scales[i].multiply(ExactRational2D.of(direction[i]))));
			}
			perturbed.add(Collections.unmodifiableList(point));
		}
		return new SubstituteGenericPerturbationSet(
				Collections.unmodifiableList(new ArrayList<>(boundaryCoordinates)),
				Collections.unmodifiableList(accepted),
				Collections.unmodifiableList(perturbed), outputs);
	}

	/** @return {@code N = max(8, (m + 1)(m + 2) / 2)} */
	public static int perturbationCount(int m) {
		return Math.max(MINIMUM_PERTURBATIONS, monomialCount(m));
	}

	/** @return the number of monomials of degree at most 2 in {@code m} variables */
	public static int monomialCount(int m) {
		return (m + 1) * (m + 2) / 2;
	}

	/**
	 * @param coordinate exact coordinate {@code z_i}
	 * @return {@code rho_i = 2^(e_i - 30)} with {@code e_i = floor(log2(max(1, |z_i|)))}
	 */
	public static ExactRational2D scale(ExactRational2D coordinate) {
		BigInteger integerPart = coordinate.abs().numerator()
				.divide(coordinate.abs().denominator());
		int exponent = integerPart.signum() == 0 ? 0 : integerPart.bitLength() - 1;
		int shift = exponent - SCALE_EXPONENT_OFFSET;
		return shift >= 0 ? ExactRational2D.of(BigInteger.ONE.shiftLeft(shift), BigInteger.ONE)
				: ExactRational2D.of(BigInteger.ONE, BigInteger.ONE.shiftLeft(-shift));
	}

	/** @return the base point {@code z} */
	public List<ExactRational2D> getBase() {
		return base;
	}

	/** @return the accepted integer directions, in order */
	public List<long[]> getDirections() {
		List<long[]> copy = new ArrayList<>(directions.size());
		for (long[] direction : directions) {
			copy.add(direction.clone());
		}
		return copy;
	}

	/** @return the perturbed points {@code z + rho o d_k}, in order */
	public List<List<ExactRational2D>> getPoints() {
		return points;
	}

	/** @return generator outputs consumed, counted against the cap */
	public int getGeneratorOutputs() {
		return generatorOutputs;
	}

	private static boolean contains(List<long[]> accepted, long[] direction) {
		for (long[] other : accepted) {
			if (java.util.Arrays.equals(other, direction)) {
				return true;
			}
		}
		return false;
	}

	/** Monomials of degree at most 2, in the fixed order 1, u_i, u_i u_j (i <= j). */
	private static BigInteger[] monomialRow(long[] u) {
		int m = u.length;
		BigInteger[] row = new BigInteger[monomialCount(m)];
		int index = 0;
		row[index++] = BigInteger.ONE;
		for (int i = 0; i < m; i++) {
			row[index++] = BigInteger.valueOf(u[i]);
		}
		for (int i = 0; i < m; i++) {
			for (int j = i; j < m; j++) {
				row[index++] = BigInteger.valueOf(u[i]).multiply(BigInteger.valueOf(u[j]));
			}
		}
		return row;
	}

	/** SplitMix64 (Steele, Lea and Flood), with its fixed constants. */
	static final class SplitMix64 {
		private long state;

		SplitMix64(long seed) {
			this.state = seed;
		}

		long next() {
			state += 0x9E3779B97F4A7C15L;
			long z = state;
			z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
			z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
			return z ^ (z >>> 31);
		}
	}

	/** Exact incremental row space over the integers, by fraction-free elimination. */
	static final class RowSpace {
		private final int width;
		private final List<BigInteger[]> rows = new ArrayList<>();
		private final List<Integer> pivots = new ArrayList<>();

		RowSpace(int width) {
			this.width = width;
		}

		int rank() {
			return rows.size();
		}

		/** @return whether the row raised the rank (it is kept only then) */
		boolean add(BigInteger[] candidate) {
			BigInteger[] row = candidate.clone();
			for (int k = 0; k < rows.size(); k++) {
				int pivot = pivots.get(k);
				if (row[pivot].signum() == 0) {
					continue;
				}
				BigInteger[] basis = rows.get(k);
				BigInteger factor = row[pivot];
				BigInteger scale = basis[pivot];
				for (int c = 0; c < width; c++) {
					row[c] = row[c].multiply(scale).subtract(basis[c].multiply(factor));
				}
				BigInteger gcd = BigInteger.ZERO;
				for (BigInteger value : row) {
					gcd = gcd.gcd(value);
				}
				if (gcd.signum() > 0 && !gcd.equals(BigInteger.ONE)) {
					for (int c = 0; c < width; c++) {
						row[c] = row[c].divide(gcd);
					}
				}
			}
			for (int c = 0; c < width; c++) {
				if (row[c].signum() != 0) {
					rows.add(row);
					pivots.add(c);
					return true;
				}
			}
			return false;
		}
	}
}
