/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.interaction;

import java.util.Objects;

/** World-coordinate selection policy; none of its values become identity. */
public final class LocusPointInteractionPolicy2D {
	public static final String VERSION = "g9u0-r6-semantic-point/v1";
	private final double maximumWorldDistance;
	private final double equalDistanceTolerance;
	private final double parameterTolerance;
	private final LocusPointInteractionWorkBudget2D workBudget;

	/** Creates one finite, deterministic query policy. */
	public LocusPointInteractionPolicy2D(double maximumWorldDistance,
			double equalDistanceTolerance, double parameterTolerance,
			LocusPointInteractionWorkBudget2D workBudget) {
		if (!Double.isFinite(maximumWorldDistance) || maximumWorldDistance <= 0
				|| !Double.isFinite(equalDistanceTolerance)
				|| equalDistanceTolerance <= 0 || !Double.isFinite(parameterTolerance)
				|| parameterTolerance <= 0) {
			throw new IllegalArgumentException(
					"Point-interaction tolerances must be finite and positive");
		}
		this.maximumWorldDistance = maximumWorldDistance;
		this.equalDistanceTolerance = equalDistanceTolerance;
		this.parameterTolerance = parameterTolerance;
		this.workBudget = Objects.requireNonNull(workBudget);
	}

	/** @return initial model-coordinate policy for a caller-supplied hit radius */
	public static LocusPointInteractionPolicy2D initial(double hitRadius) {
		return new LocusPointInteractionPolicy2D(hitRadius, 1E-10, 1E-12,
				LocusPointInteractionWorkBudget2D.initial());
	}

	public double getMaximumWorldDistance() {
		return maximumWorldDistance;
	}

	public double getEqualDistanceTolerance() {
		return equalDistanceTolerance;
	}

	public double getParameterTolerance() {
		return parameterTolerance;
	}

	public LocusPointInteractionWorkBudget2D getWorkBudget() {
		return workBudget;
	}
}
