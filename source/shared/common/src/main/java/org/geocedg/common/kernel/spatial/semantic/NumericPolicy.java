/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

/** World-coordinate tolerances for deterministic spatial evaluation. */
public final class NumericPolicy {
	private final double absoluteTolerance;
	private final double relativeTolerance;
	private final double rankRelativeTolerance;
	private final double mapTolerance;
	private final double hingeTolerance;
	private final double maximumConditionNumber;

	/**
	 * Creates a policy whose map and hinge tolerances use the residual tolerance.
	 *
	 * @param absoluteTolerance absolute world-coordinate tolerance
	 * @param relativeTolerance relative world-coordinate tolerance
	 * @param rankRelativeTolerance relative singular-value threshold factor
	 * @param maximumConditionNumber admitted condition-number limit
	 */
	public NumericPolicy(double absoluteTolerance, double relativeTolerance,
			double rankRelativeTolerance, double maximumConditionNumber) {
		this(absoluteTolerance, relativeTolerance, rankRelativeTolerance,
				Math.max(absoluteTolerance, relativeTolerance),
				Math.max(absoluteTolerance, relativeTolerance), maximumConditionNumber);
	}

	/** Creates the complete numeric policy persisted by a version-two system. */
	public NumericPolicy(double absoluteTolerance, double relativeTolerance,
			double rankRelativeTolerance, double mapTolerance,
			double hingeTolerance, double maximumConditionNumber) {
		this.absoluteTolerance = requireFiniteNonNegative(absoluteTolerance,
				"absoluteTolerance");
		this.relativeTolerance = requireFiniteNonNegative(relativeTolerance,
				"relativeTolerance");
		if (absoluteTolerance == 0 && relativeTolerance == 0) {
			throw new IllegalArgumentException("at least one residual tolerance must be positive");
		}
		this.rankRelativeTolerance = requireFinitePositive(rankRelativeTolerance,
				"rankRelativeTolerance");
		this.mapTolerance = requireFinitePositive(mapTolerance, "mapTolerance");
		this.hingeTolerance = requireFinitePositive(hingeTolerance,
				"hingeTolerance");
		this.maximumConditionNumber = requireFinitePositive(maximumConditionNumber,
				"maximumConditionNumber");
	}

	/**
	 * Conservative default policy in document world coordinates.
	 *
	 * @return standard persisted numeric policy
	 */
	public static NumericPolicy standard() {
		return new NumericPolicy(1e-10, 1e-10, 1e-12, 1e-10, 1e-10, 1e12);
	}

	public double getAbsoluteTolerance() {
		return absoluteTolerance;
	}

	public double getRelativeTolerance() {
		return relativeTolerance;
	}

	public double getRankRelativeTolerance() {
		return rankRelativeTolerance;
	}

	/**
	 * Alias matching the persisted projection-system record spelling.
	 *
	 * @return relative singular-value threshold factor
	 */
	public double getRankTolerance() {
		return rankRelativeTolerance;
	}

	public double getMapTolerance() {
		return mapTolerance;
	}

	public double getHingeTolerance() {
		return hingeTolerance;
	}

	public double getMaximumConditionNumber() {
		return maximumConditionNumber;
	}

	/**
	 * Alias matching the persisted projection-system record spelling.
	 *
	 * @return admitted condition-number limit
	 */
	public double getConditionLimit() {
		return maximumConditionNumber;
	}

	/**
	 * Returns an acceptance tolerance at the supplied world-coordinate scale.
	 *
	 * @return scale-aware residual tolerance
	 */
	public double tolerance(double scale) {
		return absoluteTolerance + relativeTolerance * Math.max(1, Math.abs(scale));
	}

	/**
	 * Returns the admitted residual for a map invariant at the supplied scale.
	 *
	 * @return scale-aware map tolerance
	 */
	public double mapTolerance(double scale) {
		return mapTolerance * Math.max(1, Math.abs(scale));
	}

	/**
	 * Returns the admitted hinge residual at the supplied world-coordinate scale.
	 *
	 * @return scale-aware hinge tolerance
	 */
	public double hingeTolerance(double scale) {
		return hingeTolerance * Math.max(1, Math.abs(scale));
	}

	/**
	 * Returns the deterministic singular-value threshold for a matrix.
	 *
	 * @return declared singular-value rank threshold
	 */
	public double rankThreshold(double maximumSingularValue, int rows, int columns) {
		return rankRelativeTolerance * Math.max(rows, columns)
				* Math.max(1, Math.abs(maximumSingularValue));
	}

	private static double requireFiniteNonNegative(double value, String name) {
		if (!Double.isFinite(value) || value < 0) {
			throw new IllegalArgumentException(name + " must be finite and non-negative");
		}
		return value;
	}

	private static double requireFinitePositive(double value, String name) {
		if (!Double.isFinite(value) || !(value > 0)) {
			throw new IllegalArgumentException(name + " must be finite and positive");
		}
		return value;
	}
}
