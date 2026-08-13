/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusInterval2D;

/**
 * Immutable reusable metric state for one complete revision-scoped component.
 *
 * <p>This is not a query result, route, contribution or aggregate.</p>
 */
public final class LocusMetricComponentState2D {
	private final LocusMetricIndexKey2D indexKey;
	private final LocusInterval2D componentExtent;
	private final MetricComponentPartition2D adaptivePartition;
	private final MetricArcCoordinateEvidence2D arcCoordinateEvidence;
	private final MetricCapabilityMetadata2D capabilityMetadata;
	private final MetricValue2D completeComponentValue;
	private final MetricComputationStatus computationStatus;
	private final MetricRectifiability rectifiability;
	private final MetricErrorEvidence2D componentErrorEvidence;
	private final List<MetricDiagnostic2D> diagnostics;

	/** Creates a deeply immutable component-level state. */
	public LocusMetricComponentState2D(LocusMetricIndexKey2D indexKey,
			LocusInterval2D componentExtent,
			MetricComponentPartition2D adaptivePartition,
			MetricArcCoordinateEvidence2D arcCoordinateEvidence,
			MetricCapabilityMetadata2D capabilityMetadata,
			MetricValue2D completeComponentValue,
			MetricComputationStatus computationStatus,
			MetricRectifiability rectifiability,
			MetricErrorEvidence2D componentErrorEvidence,
			List<MetricDiagnostic2D> diagnostics) {
		this.indexKey = Objects.requireNonNull(indexKey);
		this.componentExtent = Objects.requireNonNull(componentExtent);
		this.adaptivePartition = Objects.requireNonNull(adaptivePartition);
		this.arcCoordinateEvidence = Objects.requireNonNull(arcCoordinateEvidence);
		this.capabilityMetadata = Objects.requireNonNull(capabilityMetadata);
		this.completeComponentValue =
				Objects.requireNonNull(completeComponentValue);
		this.computationStatus = Objects.requireNonNull(computationStatus);
		this.rectifiability = Objects.requireNonNull(rectifiability);
		this.componentErrorEvidence =
				Objects.requireNonNull(componentErrorEvidence);
		this.diagnostics = immutableDiagnostics(diagnostics);
	}

	public LocusMetricIndexKey2D getIndexKey() {
		return indexKey;
	}

	public String getLocusIdentity() {
		return indexKey.getLocusIdentity();
	}

	public long getSemanticRevision() {
		return indexKey.getSemanticRevision();
	}

	public String getBranchKey() {
		return indexKey.getBranchKey();
	}

	public String getResolvedValidComponentKey() {
		return indexKey.getResolvedValidComponentKey();
	}

	public LocusInterval2D getComponentExtent() {
		return componentExtent;
	}

	public MetricComponentPartition2D getAdaptivePartition() {
		return adaptivePartition;
	}

	public MetricArcCoordinateEvidence2D getArcCoordinateEvidence() {
		return arcCoordinateEvidence;
	}

	public MetricCapabilityMetadata2D getCapabilityMetadata() {
		return capabilityMetadata;
	}

	public MetricValue2D getCompleteComponentValue() {
		return completeComponentValue;
	}

	public MetricComputationStatus getComputationStatus() {
		return computationStatus;
	}

	public MetricRectifiability getRectifiability() {
		return rectifiability;
	}

	public MetricErrorEvidence2D getComponentErrorEvidence() {
		return componentErrorEvidence;
	}

	public List<MetricDiagnostic2D> getDiagnostics() {
		return diagnostics;
	}

	public long getApproximateRetainedBytes() {
		return 192L + adaptivePartition.getApproximateRetainedBytes()
				+ arcCoordinateEvidence.getApproximateRetainedBytes();
	}

	private static List<MetricDiagnostic2D> immutableDiagnostics(
			List<MetricDiagnostic2D> input) {
		Objects.requireNonNull(input);
		ArrayList<MetricDiagnostic2D> copy = new ArrayList<>();
		for (MetricDiagnostic2D diagnostic : input) {
			copy.add(Objects.requireNonNull(diagnostic));
		}
		return Collections.unmodifiableList(copy);
	}
}
