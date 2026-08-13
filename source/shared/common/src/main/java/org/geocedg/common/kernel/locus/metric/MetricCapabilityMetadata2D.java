/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;

/** Immutable capability and method metadata for one component state. */
public final class MetricCapabilityMetadata2D {
	private final String capabilityVersion;
	private final MetricEvaluatorMethod2D evaluatorMethod;
	private final MetricMethod2D metricMethod;
	private final ConstructionFidelity constructionFidelity;

	/** Creates complete component capability metadata. */
	public MetricCapabilityMetadata2D(String capabilityVersion,
			MetricEvaluatorMethod2D evaluatorMethod, MetricMethod2D metricMethod,
			ConstructionFidelity constructionFidelity) {
		if (capabilityVersion == null || capabilityVersion.trim().isEmpty()) {
			throw new IllegalArgumentException("Capability version is required");
		}
		this.capabilityVersion = capabilityVersion;
		this.evaluatorMethod = Objects.requireNonNull(evaluatorMethod);
		this.metricMethod = Objects.requireNonNull(metricMethod);
		this.constructionFidelity =
				Objects.requireNonNull(constructionFidelity);
	}

	public String getCapabilityVersion() {
		return capabilityVersion;
	}

	public MetricEvaluatorMethod2D getEvaluatorMethod() {
		return evaluatorMethod;
	}

	public MetricMethod2D getMetricMethod() {
		return metricMethod;
	}

	public ConstructionFidelity getConstructionFidelity() {
		return constructionFidelity;
	}
}
