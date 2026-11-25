/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

/**
 * Admissible degree and size of validated data, supplied by the consumer.
 *
 * <p>The consumer owns its own limits: the {@code bspline-2d/v1} family admits
 * {@code 1 <= p <= 7} and at most 128 control points; the DXF reference import admits
 * {@code 1 <= p <= P_MAX_IMPORT} and at most {@code N_MAX_IMPORT} control points. The library
 * additionally enforces absolute safety caps so that no consumer can configure unbounded
 * work: {@link #ABSOLUTE_MAX_DEGREE} keeps every binomial coefficient of the power transform
 * an exact binary64 integer, and {@link #ABSOLUTE_MAX_CONTROL_POINTS} bounds every linear
 * operation and the canonical encoding (about 1.6 MB at the cap).</p>
 */
public final class BSplineLimits {

	/** Absolute cap of the degree; {@code 3^31 < 2^53}, so all binomials are exact. */
	public static final int ABSOLUTE_MAX_DEGREE = 31;
	/** Absolute cap of the number of control points of any data the library builds. */
	public static final int ABSOLUTE_MAX_CONTROL_POINTS = 65536;

	private final int maxDegree;
	private final int maxControlPoints;

	private BSplineLimits(int maxDegree, int maxControlPoints) {
		this.maxDegree = maxDegree;
		this.maxControlPoints = maxControlPoints;
	}

	/**
	 * Creates consumer limits.
	 *
	 * @param maxDegree largest admitted degree, {@code 1..ABSOLUTE_MAX_DEGREE}
	 * @param maxControlPoints largest admitted number of control points,
	 *            {@code 2..ABSOLUTE_MAX_CONTROL_POINTS}
	 * @return the limits
	 * @throws IllegalArgumentException when a limit is outside the absolute caps (a
	 *             configuration error of the caller, never a data outcome)
	 */
	public static BSplineLimits of(int maxDegree, int maxControlPoints) {
		if (maxDegree < 1 || maxDegree > ABSOLUTE_MAX_DEGREE) {
			throw new IllegalArgumentException("maxDegree outside 1.." + ABSOLUTE_MAX_DEGREE);
		}
		if (maxControlPoints < 2 || maxControlPoints > ABSOLUTE_MAX_CONTROL_POINTS) {
			throw new IllegalArgumentException(
					"maxControlPoints outside 2.." + ABSOLUTE_MAX_CONTROL_POINTS);
		}
		return new BSplineLimits(maxDegree, maxControlPoints);
	}

	/**
	 * @return largest admitted degree
	 */
	public int maxDegree() {
		return maxDegree;
	}

	/**
	 * @return largest admitted number of control points
	 */
	public int maxControlPoints() {
		return maxControlPoints;
	}
}
