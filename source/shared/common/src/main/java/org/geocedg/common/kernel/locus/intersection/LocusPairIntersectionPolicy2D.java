/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ResidualQuantityKind;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionPolicy2D.CoordinateTolerance;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionPolicy2D.ParameterTolerance;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionPolicy2D.ResidualTolerance;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionPolicy2D.TangencyTolerance;

/** Two-provider, dimension-aware G8C2 numerical and work policy. */
public final class LocusPairIntersectionPolicy2D {
	public static final String POLICY_VERSION = "g8c2-locus-pair/v1";
	private final String policyVersion;
	private final ParameterTolerance firstRootTolerance;
	private final ParameterTolerance secondRootTolerance;
	private final ParameterTolerance firstDeduplicationTolerance;
	private final ParameterTolerance secondDeduplicationTolerance;
	private final ResidualTolerance residualTolerance;
	private final TangencyTolerance tangencyTolerance;
	private final CoordinateTolerance coordinateTolerance;
	private final LocusIntersectionWorkBudget2D commonWorkBudget;
	private final LocusPairIntersectionWorkBudget2D pairWorkBudget;

	/** Creates one complete pair policy. */
	public LocusPairIntersectionPolicy2D(String policyVersion,
			ParameterTolerance firstRootTolerance,
			ParameterTolerance secondRootTolerance,
			ParameterTolerance firstDeduplicationTolerance,
			ParameterTolerance secondDeduplicationTolerance,
			ResidualTolerance residualTolerance,
			TangencyTolerance tangencyTolerance,
			CoordinateTolerance coordinateTolerance,
			LocusIntersectionWorkBudget2D commonWorkBudget,
			LocusPairIntersectionWorkBudget2D pairWorkBudget) {
		this.policyVersion = requireText(policyVersion);
		this.firstRootTolerance = java.util.Objects.requireNonNull(
				firstRootTolerance);
		this.secondRootTolerance = java.util.Objects.requireNonNull(
				secondRootTolerance);
		this.firstDeduplicationTolerance = java.util.Objects.requireNonNull(
				firstDeduplicationTolerance);
		this.secondDeduplicationTolerance = java.util.Objects.requireNonNull(
				secondDeduplicationTolerance);
		this.residualTolerance = java.util.Objects.requireNonNull(
				residualTolerance);
		this.tangencyTolerance = java.util.Objects.requireNonNull(
				tangencyTolerance);
		this.coordinateTolerance = java.util.Objects.requireNonNull(
				coordinateTolerance);
		this.commonWorkBudget = java.util.Objects.requireNonNull(commonWorkBudget);
		this.pairWorkBudget = java.util.Objects.requireNonNull(pairWorkBudget);
		if (!firstRootTolerance.getProviderId().equals(
				firstDeduplicationTolerance.getProviderId())
				|| !secondRootTolerance.getProviderId().equals(
						secondDeduplicationTolerance.getProviderId())) {
			throw new IllegalArgumentException(
					"Each parameter tolerance belongs to its own provider");
		}
	}

	/** @return initial approved-value policy for two captured providers */
	public static LocusPairIntersectionPolicy2D initial(
			LocusDefinition2D first, LocusDefinition2D second) {
		java.util.Objects.requireNonNull(first);
		java.util.Objects.requireNonNull(second);
		return new LocusPairIntersectionPolicy2D(POLICY_VERSION,
				parameter(first,
						LocusIntersectionPolicy2D.DEFAULT_ROOT_PARAMETER_TOLERANCE),
				parameter(second,
						LocusIntersectionPolicy2D.DEFAULT_ROOT_PARAMETER_TOLERANCE),
				parameter(first,
						LocusIntersectionPolicy2D.DEFAULT_DEDUPLICATION_TOLERANCE),
				parameter(second,
						LocusIntersectionPolicy2D.DEFAULT_DEDUPLICATION_TOLERANCE),
				new ResidualTolerance(
						ResidualQuantityKind.MODEL_COORDINATE_DISTANCE,
						"model-coordinate",
						LocusIntersectionPolicy2D
								.DEFAULT_ABSOLUTE_RESIDUAL_TOLERANCE,
						LocusIntersectionPolicy2D
								.DEFAULT_RELATIVE_RESIDUAL_TOLERANCE,
						"max(1,coordinate-magnitude-of-both-sources)"),
				new TangencyTolerance(
						"normalized-two-source-tangent-determinant",
						"dimensionless",
						LocusIntersectionPolicy2D.DEFAULT_TANGENCY_THRESHOLD),
				new CoordinateTolerance(
						LocusIntersectionPolicy2D.DEFAULT_COORDINATE_TOLERANCE,
						"model-coordinate"),
				LocusIntersectionWorkBudget2D.initial(),
				LocusPairIntersectionWorkBudget2D.initial());
	}

	/** @return ordered view with provider-specific axes exchanged */
	public LocusPairIntersectionPolicy2D reversed() {
		return new LocusPairIntersectionPolicy2D(policyVersion,
				secondRootTolerance, firstRootTolerance,
				secondDeduplicationTolerance, firstDeduplicationTolerance,
				residualTolerance, tangencyTolerance, coordinateTolerance,
				commonWorkBudget, pairWorkBudget);
	}

	public String getPolicyVersion() {
		return policyVersion;
	}

	public ParameterTolerance getFirstRootTolerance() {
		return firstRootTolerance;
	}

	public ParameterTolerance getSecondRootTolerance() {
		return secondRootTolerance;
	}

	public ParameterTolerance getFirstDeduplicationTolerance() {
		return firstDeduplicationTolerance;
	}

	public ParameterTolerance getSecondDeduplicationTolerance() {
		return secondDeduplicationTolerance;
	}

	public ResidualTolerance getResidualTolerance() {
		return residualTolerance;
	}

	public TangencyTolerance getTangencyTolerance() {
		return tangencyTolerance;
	}

	public CoordinateTolerance getCoordinateTolerance() {
		return coordinateTolerance;
	}

	public LocusIntersectionWorkBudget2D getCommonWorkBudget() {
		return commonWorkBudget;
	}

	public LocusPairIntersectionWorkBudget2D getPairWorkBudget() {
		return pairWorkBudget;
	}

	private static ParameterTolerance parameter(LocusDefinition2D definition,
			double value) {
		return new ParameterTolerance(value,
				definition.getProvider().getProviderId(),
				definition.getProvider().getParameterDescriptor());
	}

	private static String requireText(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Policy version is required");
		}
		return value;
	}
}
