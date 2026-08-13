/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/** Complete endpoint-free key for one component metric state. */
public final class LocusMetricIndexKey2D {
	private final String locusIdentity;
	private final long semanticRevision;
	private final String branchKey;
	private final String resolvedValidComponentKey;
	private final String providerEvaluatorCapabilityVersion;
	private final String metricAlgorithmVersion;
	private final String metricPolicyVersion;
	private final String tolerancePolicyVersion;
	private final double absoluteTolerance;
	private final double relativeTolerance;
	private final MetricMultiplicityPolicy multiplicityPolicy;
	private final ImproperLimitPolicy2D improperLimitPolicy;
	private final EvaluatorOnlyPolicy evaluatorOnlyPolicy;
	private final MetricWorkBudget2D workBudget;

	/** Creates a complete revision-scoped component key. */
	public LocusMetricIndexKey2D(String locusIdentity, long semanticRevision,
			String branchKey, String resolvedValidComponentKey,
			String providerEvaluatorCapabilityVersion,
			LocusMetricPolicy2D policy) {
		if (locusIdentity == null || locusIdentity.trim().isEmpty()
				|| semanticRevision < 1 || branchKey == null
				|| branchKey.trim().isEmpty()
				|| resolvedValidComponentKey == null
				|| resolvedValidComponentKey.trim().isEmpty()
				|| providerEvaluatorCapabilityVersion == null
				|| providerEvaluatorCapabilityVersion.trim().isEmpty()) {
			throw new IllegalArgumentException("Complete metric index key required");
		}
		this.locusIdentity = locusIdentity;
		this.semanticRevision = semanticRevision;
		this.branchKey = branchKey;
		this.resolvedValidComponentKey = resolvedValidComponentKey;
		this.providerEvaluatorCapabilityVersion =
				providerEvaluatorCapabilityVersion;
		this.metricAlgorithmVersion = policy.getMetricAlgorithmVersion();
		this.metricPolicyVersion = policy.getMetricPolicyVersion();
		this.tolerancePolicyVersion = policy.getTolerancePolicyVersion();
		this.absoluteTolerance = policy.getAbsoluteTolerance();
		this.relativeTolerance = policy.getRelativeTolerance();
		this.multiplicityPolicy = policy.getMultiplicityPolicy();
		this.improperLimitPolicy = policy.getImproperLimitPolicy();
		this.evaluatorOnlyPolicy = policy.getEvaluatorOnlyPolicy();
		this.workBudget = policy.getWorkBudget();
	}

	public String getLocusIdentity() {
		return locusIdentity;
	}

	public long getSemanticRevision() {
		return semanticRevision;
	}

	public String getBranchKey() {
		return branchKey;
	}

	public String getResolvedValidComponentKey() {
		return resolvedValidComponentKey;
	}

	public String getProviderEvaluatorCapabilityVersion() {
		return providerEvaluatorCapabilityVersion;
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

	public double getAbsoluteTolerance() {
		return absoluteTolerance;
	}

	public double getRelativeTolerance() {
		return relativeTolerance;
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

	public MetricWorkBudget2D getWorkBudget() {
		return workBudget;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusMetricIndexKey2D)) {
			return false;
		}
		LocusMetricIndexKey2D key = (LocusMetricIndexKey2D) other;
		return locusIdentity.equals(key.locusIdentity)
				&& semanticRevision == key.semanticRevision
				&& branchKey.equals(key.branchKey)
				&& resolvedValidComponentKey.equals(
						key.resolvedValidComponentKey)
				&& providerEvaluatorCapabilityVersion.equals(
						key.providerEvaluatorCapabilityVersion)
				&& metricAlgorithmVersion.equals(key.metricAlgorithmVersion)
				&& metricPolicyVersion.equals(key.metricPolicyVersion)
				&& tolerancePolicyVersion.equals(key.tolerancePolicyVersion)
				&& Double.doubleToLongBits(absoluteTolerance)
						== Double.doubleToLongBits(key.absoluteTolerance)
				&& Double.doubleToLongBits(relativeTolerance)
						== Double.doubleToLongBits(key.relativeTolerance)
				&& multiplicityPolicy == key.multiplicityPolicy
				&& improperLimitPolicy.equals(key.improperLimitPolicy)
				&& evaluatorOnlyPolicy == key.evaluatorOnlyPolicy
				&& workBudget.equals(key.workBudget);
	}

	@Override
	public int hashCode() {
		return Objects.hash(locusIdentity, semanticRevision, branchKey,
				resolvedValidComponentKey, providerEvaluatorCapabilityVersion,
				metricAlgorithmVersion, metricPolicyVersion,
				tolerancePolicyVersion,
				Double.doubleToLongBits(absoluteTolerance),
				Double.doubleToLongBits(relativeTolerance), multiplicityPolicy,
				improperLimitPolicy, evaluatorOnlyPolicy, workBudget);
	}

	@Override
	public String toString() {
		return locusIdentity + "/" + semanticRevision + "/" + branchKey + "/"
				+ resolvedValidComponentKey + "/"
				+ providerEvaluatorCapabilityVersion + "/"
				+ metricAlgorithmVersion + "/" + metricPolicyVersion + "/"
				+ tolerancePolicyVersion + "/"
				+ Double.toHexString(absoluteTolerance) + "/"
				+ Double.toHexString(relativeTolerance) + "/"
				+ multiplicityPolicy + "/" + improperLimitPolicy + "/"
				+ evaluatorOnlyPolicy + "/" + workBudget;
	}
}
