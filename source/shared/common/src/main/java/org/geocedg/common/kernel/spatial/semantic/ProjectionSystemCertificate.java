/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable validation result for one required projection-system subcontext. */
public final class ProjectionSystemCertificate {
	private final SpatialCapabilityStatus capabilityStatus;
	private final ProjectionSystemStatus status;
	private final List<DiagramMapEvidence> mapEvidence;
	private final List<ProjectionRelationEvidence> relationEvidence;
	private final int evaluatedFrameCount;
	private final int evaluatedMapCount;
	private final double maximumNormalizedResidual;

	/**
	 * Creates an immutable projection-system validation result.
	 *
	 * @param capabilityStatus support status of the projection-system vocabulary
	 * @param status evaluated projection-system status
	 * @param relationEvidence immutable-source relation evidence
	 * @param evaluatedFrameCount number of evaluated frames
	 * @param evaluatedMapCount number of evaluated diagram maps
	 * @param maximumNormalizedResidual maximum relation residual
	 */
	public ProjectionSystemCertificate(SpatialCapabilityStatus capabilityStatus,
			ProjectionSystemStatus status,
			List<ProjectionRelationEvidence> relationEvidence,
			int evaluatedFrameCount, int evaluatedMapCount,
			double maximumNormalizedResidual) {
		this(capabilityStatus, status, Collections.emptyList(), relationEvidence,
				evaluatedFrameCount, evaluatedMapCount, maximumNormalizedResidual);
	}

	/**
	 * Creates an immutable projection-system validation result with map evidence.
	 *
	 * @param capabilityStatus support status of the projection-system vocabulary
	 * @param status evaluated projection-system status
	 * @param mapEvidence immutable-source declared-map evidence
	 * @param relationEvidence immutable-source relation evidence
	 * @param evaluatedFrameCount number of evaluated frames
	 * @param evaluatedMapCount number of evaluated diagram maps
	 * @param maximumNormalizedResidual maximum relation residual
	 */
	public ProjectionSystemCertificate(SpatialCapabilityStatus capabilityStatus,
			ProjectionSystemStatus status, List<DiagramMapEvidence> mapEvidence,
			List<ProjectionRelationEvidence> relationEvidence,
			int evaluatedFrameCount, int evaluatedMapCount,
			double maximumNormalizedResidual) {
		this.capabilityStatus = Objects.requireNonNull(capabilityStatus);
		this.status = Objects.requireNonNull(status);
		ArrayList<DiagramMapEvidence> canonicalMaps = new ArrayList<>(
				Objects.requireNonNull(mapEvidence));
		ArrayList<ProjectionRelationEvidence> canonicalRelations = new ArrayList<>(
				Objects.requireNonNull(relationEvidence));
		canonicalMaps.sort(Comparator.comparing(DiagramMapEvidence::getEvidenceKey));
		canonicalRelations.sort(Comparator.comparing(
				ProjectionRelationEvidence::getEvidenceKey));
		requireDistinctMapKeys(canonicalMaps);
		requireDistinctRelationKeys(canonicalRelations);
		this.mapEvidence = Collections.unmodifiableList(canonicalMaps);
		this.relationEvidence = Collections.unmodifiableList(canonicalRelations);
		this.evaluatedFrameCount = evaluatedFrameCount;
		this.evaluatedMapCount = evaluatedMapCount;
		this.maximumNormalizedResidual = maximumNormalizedResidual;
		if (evaluatedFrameCount < 0 || evaluatedMapCount < 0) {
			throw new IllegalArgumentException("evaluated counts must be non-negative");
		}
		if (capabilityStatus == SpatialCapabilityStatus.UNSUPPORTED
				&& status != ProjectionSystemStatus.NOT_EVALUATED) {
			throw new IllegalArgumentException(
					"an unsupported system capability must remain not evaluated");
		}
		if (status == ProjectionSystemStatus.CONSISTENT
				&& !hasFiniteConsistentEvidence()) {
			throw new IllegalArgumentException(
					"a consistent system certificate requires finite evidence");
		}
	}

	private boolean hasFiniteConsistentEvidence() {
		if (!Double.isFinite(maximumNormalizedResidual)
				|| maximumNormalizedResidual < 0
				|| maximumNormalizedResidual > 1 || evaluatedFrameCount == 0
				|| evaluatedMapCount == 0
				|| mapEvidence.size() != evaluatedMapCount) {
			return false;
		}
		for (DiagramMapEvidence evidence : mapEvidence) {
			if (evidence.getStatus() != ProjectionSystemStatus.CONSISTENT
					|| !Double.isFinite(evidence.getDeclaredScale())
					|| !(evidence.getDeclaredScale() > 0)) {
				return false;
			}
		}
		for (ProjectionRelationEvidence evidence : relationEvidence) {
			if (evidence.getStatus() != ProjectionSystemStatus.CONSISTENT
					|| !Double.isFinite(evidence.getLineOffsetResidual())
					|| !Double.isFinite(evidence.getDirectionResidual())
					|| !Double.isFinite(evidence.getSupportPlaneResidual())
					|| !Double.isFinite(evidence.getSupportOrientationResidual())
					|| !Double.isFinite(evidence.getFoldSideDotProduct())
					|| !Double.isFinite(evidence.getMaximumNormalizedResidual())
					|| evidence.getMaximumNormalizedResidual() < 0
					|| evidence.getMaximumNormalizedResidual() > 1
					|| maximumNormalizedResidual
							< evidence.getMaximumNormalizedResidual()) {
				return false;
			}
		}
		return true;
	}

	private static void requireDistinctMapKeys(List<DiagramMapEvidence> evidence) {
		Map<String, String> contentByKey = new LinkedHashMap<>();
		for (DiagramMapEvidence item : evidence) {
			DiagramMapEvidence current = Objects.requireNonNull(item);
			String previous = contentByKey.putIfAbsent(current.getEvidenceKey(),
					current.getEvidenceContentKey());
			if (previous != null && !previous.equals(
					current.getEvidenceContentKey())) {
				throw new IllegalArgumentException(
						"conflicting diagram-map evidence key");
			}
		}
	}

	private static void requireDistinctRelationKeys(
			List<ProjectionRelationEvidence> evidence) {
		Map<String, String> contentByKey = new LinkedHashMap<>();
		for (ProjectionRelationEvidence item : evidence) {
			ProjectionRelationEvidence current = Objects.requireNonNull(item);
			String previous = contentByKey.putIfAbsent(current.getEvidenceKey(),
					current.getEvidenceContentKey());
			if (previous != null && !previous.equals(
					current.getEvidenceContentKey())) {
				throw new IllegalArgumentException(
						"conflicting relation evidence key");
			}
		}
	}

	public SpatialCapabilityStatus getCapabilityStatus() {
		return capabilityStatus;
	}

	public ProjectionSystemStatus getStatus() {
		return status;
	}

	public List<DiagramMapEvidence> getMapEvidence() {
		return mapEvidence;
	}

	public List<ProjectionRelationEvidence> getRelationEvidence() {
		return relationEvidence;
	}

	public int getEvaluatedFrameCount() {
		return evaluatedFrameCount;
	}

	public int getEvaluatedMapCount() {
		return evaluatedMapCount;
	}

	public double getMaximumNormalizedResidual() {
		return maximumNormalizedResidual;
	}
}
