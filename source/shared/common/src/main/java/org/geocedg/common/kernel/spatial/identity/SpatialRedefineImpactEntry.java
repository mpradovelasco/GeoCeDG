/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** One typed durable participant in a legacy-replacement impact report. */
public final class SpatialRedefineImpactEntry {
	/** Predicted semantic state after explicit legacy replacement. */
	public enum PredictedStatus {
		AUTO_REVALIDATES,
		BECOMES_NONCURRENT,
		BECOMES_UNDEFINED,
		RETIRED
	}

	/** Explicit recovery needed after replacement; it never selects a new source. */
	public enum RecoveryClass {
		NONE,
		REQUIRES_EXPLICIT_REBIND,
		REQUIRES_EXPLICIT_REDEFINE
	}

	/** Stable machine-readable reason for inclusion. */
	public enum ReasonCode {
		REPLACED_SOURCE_IDENTITY_RETIRED,
		DIRECT_SEMANTIC_SOURCE_LOSS,
		TRANSITIVE_SEMANTIC_SOURCE_LOSS,
		PROJECTION_RELATION_SOURCE_LOSS
	}

	private final SpatialIdentityId participantId;
	private final String relationType;
	private final List<PersistentGeoId> affectedSourceIds;
	private final PredictedStatus predictedStatus;
	private final ReasonCode reasonCode;
	private final RecoveryClass recoveryClass;

	/** Creates one immutable impact entry. */
	public SpatialRedefineImpactEntry(SpatialIdentityId participantId,
			String relationType, Collection<PersistentGeoId> affectedSourceIds,
			PredictedStatus predictedStatus, ReasonCode reasonCode,
			RecoveryClass recoveryClass) {
		this.participantId = Objects.requireNonNull(participantId);
		this.relationType = SpatialRecordSupport.requireText(relationType,
				"relationType");
		ArrayList<PersistentGeoId> sources = new ArrayList<>(
				Objects.requireNonNull(affectedSourceIds));
		if (sources.isEmpty() || sources.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException(
					"affectedSourceIds must contain durable identities");
		}
		Collections.sort(sources);
		this.affectedSourceIds = Collections.unmodifiableList(sources);
		this.predictedStatus = Objects.requireNonNull(predictedStatus);
		this.reasonCode = Objects.requireNonNull(reasonCode);
		this.recoveryClass = Objects.requireNonNull(recoveryClass);
	}

	public SpatialIdentityId getParticipantId() {
		return participantId;
	}

	public String getRelationType() {
		return relationType;
	}

	/** @return every retired source identity affecting this participant */
	public List<PersistentGeoId> getAffectedSourceIds() {
		return affectedSourceIds;
	}

	public PredictedStatus getPredictedStatus() {
		return predictedStatus;
	}

	public ReasonCode getReasonCode() {
		return reasonCode;
	}

	public RecoveryClass getRecoveryClass() {
		return recoveryClass;
	}
}
