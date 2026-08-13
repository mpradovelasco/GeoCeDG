/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/** Immutable output of one per-call deterministic integration context. */
public final class MetricIntegrationResult2D {
	private final MetricValue2D metricValue;
	private final MetricErrorEvidence2D errorEvidence;
	private final MetricComputationStatus computationStatus;
	private final MetricComponentPartition2D partition;
	private final MetricArcCoordinateEvidence2D arcCoordinateEvidence;
	private final long evaluations;
	private final long subdivisions;
	private final MetricWorkLimit2D exhaustedWorkLimit;

	/** Creates a complete integration result. */
	public MetricIntegrationResult2D(MetricValue2D metricValue,
			MetricErrorEvidence2D errorEvidence,
			MetricComputationStatus computationStatus,
			MetricComponentPartition2D partition,
			MetricArcCoordinateEvidence2D arcCoordinateEvidence,
			long evaluations, long subdivisions,
			MetricWorkLimit2D exhaustedWorkLimit) {
		this.metricValue = Objects.requireNonNull(metricValue);
		this.errorEvidence = Objects.requireNonNull(errorEvidence);
		this.computationStatus = Objects.requireNonNull(computationStatus);
		this.partition = Objects.requireNonNull(partition);
		this.arcCoordinateEvidence =
				Objects.requireNonNull(arcCoordinateEvidence);
		this.evaluations = evaluations;
		this.subdivisions = subdivisions;
		this.exhaustedWorkLimit = Objects.requireNonNull(exhaustedWorkLimit);
	}

	public MetricValue2D getMetricValue() {
		return metricValue;
	}

	public MetricErrorEvidence2D getErrorEvidence() {
		return errorEvidence;
	}

	public MetricComputationStatus getComputationStatus() {
		return computationStatus;
	}

	public MetricComponentPartition2D getPartition() {
		return partition;
	}

	public MetricArcCoordinateEvidence2D getArcCoordinateEvidence() {
		return arcCoordinateEvidence;
	}

	public long getEvaluations() {
		return evaluations;
	}

	public long getSubdivisions() {
		return subdivisions;
	}

	public MetricWorkLimit2D getExhaustedWorkLimit() {
		return exhaustedWorkLimit;
	}
}
