/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/** Three independent deterministic limits for one metric integration call. */
public final class MetricWorkBudget2D {
	private final long maximumMetricEvaluations;
	private final long maximumMetricSubdivisions;
	private final int maximumAdaptiveDepth;

	/** Creates a positive work budget. */
	public MetricWorkBudget2D(long maximumMetricEvaluations,
			long maximumMetricSubdivisions, int maximumAdaptiveDepth) {
		if (maximumMetricEvaluations < 3 || maximumMetricSubdivisions < 1
				|| maximumAdaptiveDepth < 1) {
			throw new IllegalArgumentException("Metric work limits must be positive");
		}
		this.maximumMetricEvaluations = maximumMetricEvaluations;
		this.maximumMetricSubdivisions = maximumMetricSubdivisions;
		this.maximumAdaptiveDepth = maximumAdaptiveDepth;
	}

	public long getMaximumMetricEvaluations() {
		return maximumMetricEvaluations;
	}

	public long getMaximumMetricSubdivisions() {
		return maximumMetricSubdivisions;
	}

	public int getMaximumAdaptiveDepth() {
		return maximumAdaptiveDepth;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MetricWorkBudget2D)) {
			return false;
		}
		MetricWorkBudget2D budget = (MetricWorkBudget2D) other;
		return maximumMetricEvaluations == budget.maximumMetricEvaluations
				&& maximumMetricSubdivisions == budget.maximumMetricSubdivisions
				&& maximumAdaptiveDepth == budget.maximumAdaptiveDepth;
	}

	@Override
	public int hashCode() {
		return Objects.hash(maximumMetricEvaluations, maximumMetricSubdivisions,
				maximumAdaptiveDepth);
	}

	@Override
	public String toString() {
		return maximumMetricEvaluations + "/" + maximumMetricSubdivisions + "/"
				+ maximumAdaptiveDepth;
	}
}
