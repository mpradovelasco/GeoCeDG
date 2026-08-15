/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Objects;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ResidualQuantityKind;

/** Immutable versioned and dimension-aware G8B numerical policy. */
public final class LocusIntersectionPolicy2D {
	public static final String POLICY_VERSION = "g8b-initial-normalized/v1";
	public static final String EXTENDED_TARGET_POLICY_VERSION =
			"g8c1-extended-target-normalized/v1";
	public static final String PROVENANCE_VERSION = "g8a-measured-candidate/v1";
	public static final double DEFAULT_ROOT_PARAMETER_TOLERANCE = 1E-12;
	public static final double DEFAULT_ABSOLUTE_RESIDUAL_TOLERANCE = 2E-12;
	public static final double DEFAULT_RELATIVE_RESIDUAL_TOLERANCE = 2E-12;
	public static final double DEFAULT_TANGENCY_THRESHOLD = 1E-10;
	public static final double DEFAULT_DEDUPLICATION_TOLERANCE = 4E-12;
	public static final double DEFAULT_CONTINUATION_TOLERANCE = 1E-8;
	public static final double DEFAULT_COORDINATE_TOLERANCE = 4E-12;

	private final String policyVersion;
	private final String provenanceVersion;
	private final ParameterTolerance rootParameterTolerance;
	private final ResidualTolerance residualTolerance;
	private final TangencyTolerance tangencyTolerance;
	private final ParameterTolerance deduplicationTolerance;
	private final ParameterTolerance continuationTolerance;
	private final CoordinateTolerance coordinateTolerance;
	private final LocusIntersectionWorkBudget2D workBudget;

	/** Creates one complete intersection policy. */
	public LocusIntersectionPolicy2D(String policyVersion,
			String provenanceVersion,
			ParameterTolerance rootParameterTolerance,
			ResidualTolerance residualTolerance,
			TangencyTolerance tangencyTolerance,
			ParameterTolerance deduplicationTolerance,
			ParameterTolerance continuationTolerance,
			CoordinateTolerance coordinateTolerance,
			LocusIntersectionWorkBudget2D workBudget) {
		this.policyVersion = requireText(policyVersion, "Policy version");
		this.provenanceVersion = requireText(provenanceVersion,
				"Policy provenance");
		this.rootParameterTolerance =
				Objects.requireNonNull(rootParameterTolerance);
		this.residualTolerance = Objects.requireNonNull(residualTolerance);
		this.tangencyTolerance = Objects.requireNonNull(tangencyTolerance);
		this.deduplicationTolerance =
				Objects.requireNonNull(deduplicationTolerance);
		this.continuationTolerance =
				Objects.requireNonNull(continuationTolerance);
		this.coordinateTolerance = Objects.requireNonNull(coordinateTolerance);
		this.workBudget = Objects.requireNonNull(workBudget);
		if (!rootParameterTolerance.getProviderId()
				.equals(deduplicationTolerance.getProviderId())
				|| !rootParameterTolerance.getProviderId()
						.equals(continuationTolerance.getProviderId())) {
			throw new IllegalArgumentException(
					"Parameter tolerances require one semantic provider");
		}
	}

	/**
	 * Creates the approved initial policy for one declared semantic provider.
	 *
	 * @param providerId versioned provider identifier
	 * @param parameterDescriptor provider-owned parameter units/normalization
	 * @return initial G8B policy
	 */
	public static LocusIntersectionPolicy2D initial(String providerId,
			String parameterDescriptor) {
		return initial(providerId, parameterDescriptor,
				ResidualQuantityKind.MODEL_COORDINATE_DISTANCE,
				"model-coordinate",
				"max(1,target-characteristic-length)", POLICY_VERSION);
	}

	/**
	 * Creates the approved numeric values for one typed target residual.
	 *
	 * <p>The values retain their G8A provenance, while quantity, units and
	 * characteristic scale come from the captured adapter contract.</p>
	 *
	 * @return dimension-compatible G8C1 query policy
	 */
	public static LocusIntersectionPolicy2D initial(String providerId,
			String parameterDescriptor,
			IntersectionResidualContract2D targetContract) {
		Objects.requireNonNull(targetContract);
		return initial(providerId, parameterDescriptor,
				targetContract.getQuantityKind(), targetContract.getUnits(),
				targetContract.getCharacteristicScalePolicy(),
				targetContract.getQuantityKind()
						== ResidualQuantityKind.MODEL_COORDINATE_DISTANCE
								? POLICY_VERSION
								: EXTENDED_TARGET_POLICY_VERSION);
	}

	private static LocusIntersectionPolicy2D initial(String providerId,
			String parameterDescriptor, ResidualQuantityKind residualQuantity,
			String residualUnits, String characteristicScalePolicy,
			String policyVersion) {
		ParameterTolerance root = new ParameterTolerance(
				DEFAULT_ROOT_PARAMETER_TOLERANCE, providerId,
				parameterDescriptor);
		ParameterTolerance dedup = new ParameterTolerance(
				DEFAULT_DEDUPLICATION_TOLERANCE, providerId,
				parameterDescriptor);
		ParameterTolerance continuation = new ParameterTolerance(
				DEFAULT_CONTINUATION_TOLERANCE, providerId,
				parameterDescriptor);
		return new LocusIntersectionPolicy2D(policyVersion,
				PROVENANCE_VERSION, root,
				new ResidualTolerance(
						residualQuantity, residualUnits,
						DEFAULT_ABSOLUTE_RESIDUAL_TOLERANCE,
						DEFAULT_RELATIVE_RESIDUAL_TOLERANCE,
						characteristicScalePolicy),
				new TangencyTolerance(
						"normalized-target-contact/source-arc-length",
						"dimensionless", DEFAULT_TANGENCY_THRESHOLD),
				dedup, continuation,
				new CoordinateTolerance(DEFAULT_COORDINATE_TOLERANCE,
						"model-coordinate"),
				LocusIntersectionWorkBudget2D.initial());
	}

	public String getPolicyVersion() {
		return policyVersion;
	}

	public String getProvenanceVersion() {
		return provenanceVersion;
	}

	public ParameterTolerance getRootParameterTolerance() {
		return rootParameterTolerance;
	}

	public ResidualTolerance getResidualTolerance() {
		return residualTolerance;
	}

	public TangencyTolerance getTangencyTolerance() {
		return tangencyTolerance;
	}

	public ParameterTolerance getDeduplicationTolerance() {
		return deduplicationTolerance;
	}

	public ParameterTolerance getContinuationTolerance() {
		return continuationTolerance;
	}

	public CoordinateTolerance getCoordinateTolerance() {
		return coordinateTolerance;
	}

	public LocusIntersectionWorkBudget2D getWorkBudget() {
		return workBudget;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusIntersectionPolicy2D)) {
			return false;
		}
		LocusIntersectionPolicy2D policy = (LocusIntersectionPolicy2D) other;
		return policyVersion.equals(policy.policyVersion)
				&& provenanceVersion.equals(policy.provenanceVersion)
				&& rootParameterTolerance.equals(policy.rootParameterTolerance)
				&& residualTolerance.equals(policy.residualTolerance)
				&& tangencyTolerance.equals(policy.tangencyTolerance)
				&& deduplicationTolerance.equals(policy.deduplicationTolerance)
				&& continuationTolerance.equals(policy.continuationTolerance)
				&& coordinateTolerance.equals(policy.coordinateTolerance)
				&& workBudget.equals(policy.workBudget);
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyVersion, provenanceVersion,
				rootParameterTolerance, residualTolerance, tangencyTolerance,
				deduplicationTolerance, continuationTolerance,
				coordinateTolerance, workBudget);
	}

	/** Provider-scoped semantic-parameter tolerance. */
	public static final class ParameterTolerance {
		private final double value;
		private final String providerId;
		private final String parameterDescriptor;

		/** Creates a typed parameter tolerance. */
		public ParameterTolerance(double value, String providerId,
				String parameterDescriptor) {
			this.value = positive(value, "Parameter tolerance");
			this.providerId = requireText(providerId, "Provider id");
			this.parameterDescriptor = requireText(parameterDescriptor,
					"Parameter descriptor");
		}

		public double getValue() {
			return value;
		}

		public String getProviderId() {
			return providerId;
		}

		public String getParameterDescriptor() {
			return parameterDescriptor;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ParameterTolerance)) {
				return false;
			}
			ParameterTolerance tolerance = (ParameterTolerance) other;
			return Double.doubleToLongBits(value)
						== Double.doubleToLongBits(tolerance.value)
					&& providerId.equals(tolerance.providerId)
					&& parameterDescriptor.equals(tolerance.parameterDescriptor);
		}

		@Override
		public int hashCode() {
			return Objects.hash(Double.doubleToLongBits(value), providerId,
					parameterDescriptor);
		}
	}

	/** Dimension-aware normalized target residual tolerance. */
	public static final class ResidualTolerance {
		private final ResidualQuantityKind quantityKind;
		private final String units;
		private final double absoluteTolerance;
		private final double relativeTolerance;
		private final String characteristicScalePolicy;

		/** Creates a typed residual tolerance. */
		public ResidualTolerance(ResidualQuantityKind quantityKind,
				String units, double absoluteTolerance, double relativeTolerance,
				String characteristicScalePolicy) {
			this.quantityKind = Objects.requireNonNull(quantityKind);
			this.units = requireText(units, "Residual units");
			this.absoluteTolerance = positive(absoluteTolerance,
					"Absolute residual tolerance");
			if (!Double.isFinite(relativeTolerance) || relativeTolerance < 0) {
				throw new IllegalArgumentException(
						"Relative residual tolerance must be finite and nonnegative");
			}
			this.relativeTolerance = relativeTolerance;
			this.characteristicScalePolicy = requireText(
					characteristicScalePolicy, "Characteristic-scale policy");
		}

		public ResidualQuantityKind getQuantityKind() {
			return quantityKind;
		}

		public String getUnits() {
			return units;
		}

		public double getAbsoluteTolerance() {
			return absoluteTolerance;
		}

		public double getRelativeTolerance() {
			return relativeTolerance;
		}

		public String getCharacteristicScalePolicy() {
			return characteristicScalePolicy;
		}

		/** @return accepted magnitude for a compatible characteristic scale */
		public double threshold(double characteristicScale) {
			if (!Double.isFinite(characteristicScale)
					|| characteristicScale <= 0) {
				throw new IllegalArgumentException(
						"Residual characteristic scale must be positive");
			}
			return absoluteTolerance
					+ relativeTolerance * Math.max(1, characteristicScale);
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ResidualTolerance)) {
				return false;
			}
			ResidualTolerance tolerance = (ResidualTolerance) other;
			return quantityKind == tolerance.quantityKind
					&& units.equals(tolerance.units)
					&& Double.doubleToLongBits(absoluteTolerance)
							== Double.doubleToLongBits(tolerance.absoluteTolerance)
					&& Double.doubleToLongBits(relativeTolerance)
							== Double.doubleToLongBits(tolerance.relativeTolerance)
					&& characteristicScalePolicy
							.equals(tolerance.characteristicScalePolicy);
		}

		@Override
		public int hashCode() {
			return Objects.hash(quantityKind, units,
					Double.doubleToLongBits(absoluteTolerance),
					Double.doubleToLongBits(relativeTolerance),
					characteristicScalePolicy);
		}
	}

	/** Normalized contact-indicator threshold. */
	public static final class TangencyTolerance {
		private final String indicatorId;
		private final String units;
		private final double threshold;

		/** Creates a typed contact threshold. */
		public TangencyTolerance(String indicatorId, String units,
				double threshold) {
			this.indicatorId = requireText(indicatorId, "Contact indicator");
			this.units = requireText(units, "Contact-indicator units");
			this.threshold = positive(threshold, "Tangency threshold");
		}

		public String getIndicatorId() {
			return indicatorId;
		}

		public String getUnits() {
			return units;
		}

		public double getThreshold() {
			return threshold;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof TangencyTolerance)) {
				return false;
			}
			TangencyTolerance tolerance = (TangencyTolerance) other;
			return indicatorId.equals(tolerance.indicatorId)
					&& units.equals(tolerance.units)
					&& Double.doubleToLongBits(threshold)
							== Double.doubleToLongBits(tolerance.threshold);
		}

		@Override
		public int hashCode() {
			return Objects.hash(indicatorId, units,
					Double.doubleToLongBits(threshold));
		}
	}

	/** Secondary model-coordinate verification tolerance. */
	public static final class CoordinateTolerance {
		private final double value;
		private final String units;

		/** Creates a typed coordinate tolerance. */
		public CoordinateTolerance(double value, String units) {
			this.value = positive(value, "Coordinate tolerance");
			this.units = requireText(units, "Coordinate units");
		}

		public double getValue() {
			return value;
		}

		public String getUnits() {
			return units;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof CoordinateTolerance)) {
				return false;
			}
			CoordinateTolerance tolerance = (CoordinateTolerance) other;
			return Double.doubleToLongBits(value)
						== Double.doubleToLongBits(tolerance.value)
					&& units.equals(tolerance.units);
		}

		@Override
		public int hashCode() {
			return Objects.hash(Double.doubleToLongBits(value), units);
		}
	}

	private static double positive(double value, String name) {
		if (!Double.isFinite(value) || value <= 0) {
			throw new IllegalArgumentException(name + " must be positive and finite");
		}
		return value;
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
