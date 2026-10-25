/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/**
 * Outward enclosures of a target residual {@code h} of a locus source and of its first two
 * derivatives with respect to the driver parameter, on one parameter box (PRE-G9B-CU3 piece
 * I-1; Stage B design section 4.3; {@code locus-v2-intersections.md} CU3.1 (a)). The residual
 * is any function with the zero set, the signs and the root multiplicities of the residual of
 * the target authority of the decision configuration; it is revision evidence and never enters
 * a token, a selector or the construction graph.
 */
public final class ResidualJetEnclosure2D {
	/** Sign that an enclosure containing zero leaves undecided. */
	public static final int UNDECIDED = 0;

	private final double valueLower;
	private final double valueUpper;
	private final double firstLower;
	private final double firstUpper;
	private final double secondLower;
	private final double secondUpper;

	/**
	 * @param value enclosure {@code {lower, upper}} of {@code h} on the box
	 * @param first enclosure of {@code h'} on the box
	 * @param second enclosure of {@code h''} on the box
	 */
	public ResidualJetEnclosure2D(double[] value, double[] first, double[] second) {
		valueLower = lower(value);
		valueUpper = upper(value);
		firstLower = lower(first);
		firstUpper = upper(first);
		secondLower = lower(second);
		secondUpper = upper(second);
	}

	/** @return {@code {lower, upper}} of {@code h} */
	public double[] getValue() {
		return new double[] {valueLower, valueUpper};
	}

	/** @return {@code {lower, upper}} of {@code h'} */
	public double[] getFirst() {
		return new double[] {firstLower, firstUpper};
	}

	/** @return {@code {lower, upper}} of {@code h''} */
	public double[] getSecond() {
		return new double[] {secondLower, secondUpper};
	}

	/**
	 * @param order {@code 0} for {@code h}, {@code 1} for {@code h'}, {@code 2} for {@code h''}
	 * @return {@code 1} or {@code -1} when the enclosure excludes zero, otherwise
	 *         {@link #UNDECIDED}
	 */
	public int sign(int order) {
		double lower;
		double upper;
		switch (order) {
		case 0:
			lower = valueLower;
			upper = valueUpper;
			break;
		case 1:
			lower = firstLower;
			upper = firstUpper;
			break;
		case 2:
			lower = secondLower;
			upper = secondUpper;
			break;
		default:
			throw new IllegalArgumentException("Residual jets have orders 0 to 2");
		}
		return lower > 0 ? 1 : upper < 0 ? -1 : UNDECIDED;
	}

	@Override
	public String toString() {
		return "h=[" + valueLower + "," + valueUpper + "] h'=[" + firstLower + "," + firstUpper
				+ "] h''=[" + secondLower + "," + secondUpper + "]";
	}

	private static double lower(double[] bounds) {
		check(bounds);
		return bounds[0];
	}

	private static double upper(double[] bounds) {
		check(bounds);
		return bounds[1];
	}

	private static void check(double[] bounds) {
		if (bounds == null || bounds.length != 2 || Double.isNaN(bounds[0])
				|| Double.isNaN(bounds[1]) || bounds[0] > bounds[1]) {
			throw new IllegalArgumentException("A residual enclosure needs ordered bounds");
		}
	}
}
