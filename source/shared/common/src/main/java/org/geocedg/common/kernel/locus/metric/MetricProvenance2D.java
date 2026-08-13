/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/** Immutable source, revision and policy provenance for metric evidence. */
public final class MetricProvenance2D {
	private final String locusIdentity;
	private final long semanticRevision;
	private final String capabilityVersion;
	private final String metricAlgorithmVersion;
	private final String metricPolicyVersion;

	/** Creates complete provenance for one coherent semantic revision. */
	public MetricProvenance2D(String locusIdentity, long semanticRevision,
			String capabilityVersion, String metricAlgorithmVersion,
			String metricPolicyVersion) {
		if (locusIdentity == null || locusIdentity.trim().isEmpty()
				|| semanticRevision < 1 || capabilityVersion == null
				|| capabilityVersion.trim().isEmpty()
				|| metricAlgorithmVersion == null
				|| metricAlgorithmVersion.trim().isEmpty()
				|| metricPolicyVersion == null
				|| metricPolicyVersion.trim().isEmpty()) {
			throw new IllegalArgumentException("Complete metric provenance is required");
		}
		this.locusIdentity = locusIdentity;
		this.semanticRevision = semanticRevision;
		this.capabilityVersion = capabilityVersion;
		this.metricAlgorithmVersion = metricAlgorithmVersion;
		this.metricPolicyVersion = metricPolicyVersion;
	}

	public String getLocusIdentity() {
		return locusIdentity;
	}

	public long getSemanticRevision() {
		return semanticRevision;
	}

	public String getCapabilityVersion() {
		return capabilityVersion;
	}

	public String getMetricAlgorithmVersion() {
		return metricAlgorithmVersion;
	}

	public String getMetricPolicyVersion() {
		return metricPolicyVersion;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MetricProvenance2D)) {
			return false;
		}
		MetricProvenance2D provenance = (MetricProvenance2D) other;
		return locusIdentity.equals(provenance.locusIdentity)
				&& semanticRevision == provenance.semanticRevision
				&& capabilityVersion.equals(provenance.capabilityVersion)
				&& metricAlgorithmVersion.equals(provenance.metricAlgorithmVersion)
				&& metricPolicyVersion.equals(provenance.metricPolicyVersion);
	}

	@Override
	public int hashCode() {
		return Objects.hash(locusIdentity, semanticRevision, capabilityVersion,
				metricAlgorithmVersion, metricPolicyVersion);
	}
}
