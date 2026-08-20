/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/** Immutable, complete and versioned numerical metric policy. */
public final class LocusMetricPolicy2D {
	public static final double DEFAULT_ABSOLUTE_TOLERANCE = 1E-10;
	public static final double DEFAULT_RELATIVE_TOLERANCE = 1E-9;
	public static final long DEFAULT_MAXIMUM_EVALUATIONS = 32_768;
	public static final long DEFAULT_MAXIMUM_SUBDIVISIONS = 16_384;
	public static final int DEFAULT_MAXIMUM_DEPTH = 22;

	private final double absoluteTolerance;
	private final double relativeTolerance;
	private final MetricWorkBudget2D workBudget;
	private final String metricAlgorithmVersion;
	private final String metricPolicyVersion;
	private final String tolerancePolicyVersion;
	private final MetricMultiplicityPolicy multiplicityPolicy;
	private final ImproperLimitPolicy2D improperLimitPolicy;
	private final EvaluatorOnlyPolicy evaluatorOnlyPolicy;

	/** Creates one immutable policy; every field participates in key equality. */
	public LocusMetricPolicy2D(double absoluteTolerance, double relativeTolerance,
			MetricWorkBudget2D workBudget, String metricAlgorithmVersion,
			String metricPolicyVersion, String tolerancePolicyVersion,
			MetricMultiplicityPolicy multiplicityPolicy,
			ImproperLimitPolicy2D improperLimitPolicy,
			EvaluatorOnlyPolicy evaluatorOnlyPolicy) {
		if (!Double.isFinite(absoluteTolerance) || absoluteTolerance <= 0
				|| !Double.isFinite(relativeTolerance) || relativeTolerance < 0
				|| metricAlgorithmVersion == null
				|| metricAlgorithmVersion.trim().isEmpty()
				|| metricPolicyVersion == null
				|| metricPolicyVersion.trim().isEmpty()
				|| tolerancePolicyVersion == null
				|| tolerancePolicyVersion.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid metric tolerance or version");
		}
		this.absoluteTolerance = absoluteTolerance;
		this.relativeTolerance = relativeTolerance;
		this.workBudget = Objects.requireNonNull(workBudget);
		this.metricAlgorithmVersion = metricAlgorithmVersion;
		this.metricPolicyVersion = metricPolicyVersion;
		this.tolerancePolicyVersion = tolerancePolicyVersion;
		this.multiplicityPolicy = Objects.requireNonNull(multiplicityPolicy);
		this.improperLimitPolicy = Objects.requireNonNull(improperLimitPolicy);
		this.evaluatorOnlyPolicy = Objects.requireNonNull(evaluatorOnlyPolicy);
	}

	/**
	 * Initial author-approved G7B implementation policy.
	 *
	 * @return versioned initial policy
	 */
	public static LocusMetricPolicy2D initial() {
		return new LocusMetricPolicy2D(DEFAULT_ABSOLUTE_TOLERANCE,
				DEFAULT_RELATIVE_TOLERANCE,
				new MetricWorkBudget2D(DEFAULT_MAXIMUM_EVALUATIONS,
						DEFAULT_MAXIMUM_SUBDIVISIONS, DEFAULT_MAXIMUM_DEPTH),
				"g7b-total-variation/v1", "g7b-metric-policy/v1",
				"g7b-abs-rel/v1", MetricMultiplicityPolicy
						.CONSTRUCTIVE_TRAVERSAL_LENGTH,
				new ImproperLimitPolicy2D("g7b-improper-limit/v1", 12, 0.25),
				EvaluatorOnlyPolicy.UNCERTIFIED);
	}

	/**
	 * G9U0 public adaptive policy with an explicit, non-certified estimate.
	 *
	 * <p>This enables the guarded scalar adapter only when every other rich
	 * admissibility predicate also succeeds. The value is never described as
	 * exact or certified.</p>
	 *
	 * @return versioned experimental public metric policy
	 */
	public static LocusMetricPolicy2D publicExperimental() {
		return new LocusMetricPolicy2D(DEFAULT_ABSOLUTE_TOLERANCE,
				DEFAULT_RELATIVE_TOLERANCE,
				new MetricWorkBudget2D(DEFAULT_MAXIMUM_EVALUATIONS,
						DEFAULT_MAXIMUM_SUBDIVISIONS, DEFAULT_MAXIMUM_DEPTH),
				"g9u0-public-total-variation/v1",
				"g9u0-public-metric-policy/v1", "g9u0-abs-rel/v1",
				MetricMultiplicityPolicy.CONSTRUCTIVE_TRAVERSAL_LENGTH,
				new ImproperLimitPolicy2D("g9u0-improper-limit/v1", 12, 0.25),
				EvaluatorOnlyPolicy.ESTIMATED_WITH_EXPLICIT_ASSUMPTIONS);
	}

	public double getAbsoluteTolerance() {
		return absoluteTolerance;
	}

	public double getRelativeTolerance() {
		return relativeTolerance;
	}

	public MetricWorkBudget2D getWorkBudget() {
		return workBudget;
	}

	public String getMetricAlgorithmVersion() {
		return metricAlgorithmVersion;
	}

	public String getMetricPolicyVersion() {
		return metricPolicyVersion;
	}

	public String getTolerancePolicyVersion() {
		return tolerancePolicyVersion;
	}

	public MetricMultiplicityPolicy getMultiplicityPolicy() {
		return multiplicityPolicy;
	}

	public ImproperLimitPolicy2D getImproperLimitPolicy() {
		return improperLimitPolicy;
	}

	public EvaluatorOnlyPolicy getEvaluatorOnlyPolicy() {
		return evaluatorOnlyPolicy;
	}

	/**
	 * Translation-invariant absolute/relative stopping threshold.
	 *
	 * @return effective world-coordinate threshold
	 */
	public double threshold(double geometricScale) {
		if (!Double.isFinite(geometricScale) || geometricScale < 0) {
			throw new IllegalArgumentException("Geometric scale must be finite");
		}
		return Math.max(absoluteTolerance, relativeTolerance * geometricScale);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusMetricPolicy2D)) {
			return false;
		}
		LocusMetricPolicy2D policy = (LocusMetricPolicy2D) other;
		return Double.doubleToLongBits(absoluteTolerance)
						== Double.doubleToLongBits(policy.absoluteTolerance)
				&& Double.doubleToLongBits(relativeTolerance)
						== Double.doubleToLongBits(policy.relativeTolerance)
				&& workBudget.equals(policy.workBudget)
				&& metricAlgorithmVersion.equals(policy.metricAlgorithmVersion)
				&& metricPolicyVersion.equals(policy.metricPolicyVersion)
				&& tolerancePolicyVersion.equals(policy.tolerancePolicyVersion)
				&& multiplicityPolicy == policy.multiplicityPolicy
				&& improperLimitPolicy.equals(policy.improperLimitPolicy)
				&& evaluatorOnlyPolicy == policy.evaluatorOnlyPolicy;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Double.doubleToLongBits(absoluteTolerance),
				Double.doubleToLongBits(relativeTolerance), workBudget,
				metricAlgorithmVersion, metricPolicyVersion,
				tolerancePolicyVersion, multiplicityPolicy, improperLimitPolicy,
				evaluatorOnlyPolicy);
	}
}
