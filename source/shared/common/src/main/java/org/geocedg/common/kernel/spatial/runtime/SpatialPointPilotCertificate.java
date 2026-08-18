/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.runtime;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.geocedg.common.kernel.spatial.identity.ProjectionSystemId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.semantic.SpatialCurrentnessStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialPointCertificate;

/**
 * Atomic construction-owned publication for one projection-defined point.
 */
public final class SpatialPointPilotCertificate {
	private final SpatialObjectId objectId;
	private final ProjectionSystemId systemId;
	private final Map<SpatialIdentityId, String> revisionTuple;
	private final String valueSnapshotToken;
	private final SpatialPointCertificate semanticCertificate;
	private final boolean currentRevision;

	SpatialPointPilotCertificate(SpatialObjectId objectId,
			ProjectionSystemId systemId,
			Map<SpatialIdentityId, String> revisionTuple, String valueSnapshotToken,
			SpatialPointCertificate semanticCertificate, boolean currentRevision) {
		this.objectId = Objects.requireNonNull(objectId);
		this.systemId = Objects.requireNonNull(systemId);
		this.revisionTuple = Collections.unmodifiableMap(
				new LinkedHashMap<>(Objects.requireNonNull(revisionTuple)));
		this.valueSnapshotToken = Objects.requireNonNull(valueSnapshotToken);
		this.semanticCertificate = Objects.requireNonNull(semanticCertificate);
		this.currentRevision = currentRevision;
		SpatialCurrentnessStatus semanticCurrentness =
				semanticCertificate.getCurrentnessStatus();
		if (currentRevision == (semanticCurrentness
				== SpatialCurrentnessStatus.INVALIDATED)) {
			throw new IllegalArgumentException(
					"wrapper and semantic point currentness must agree");
		}
		if (!currentRevision && semanticCertificate.getPoint().isPresent()) {
			throw new IllegalArgumentException(
					"noncurrent point evidence must not retain a payload");
		}
	}

	public SpatialObjectId getObjectId() {
		return objectId;
	}

	public ProjectionSystemId getSystemId() {
		return systemId;
	}

	/** @return the exact typed record revision evidence used by evaluation */
	public Map<SpatialIdentityId, String> getRevisionTuple() {
		return revisionTuple;
	}

	/** @return deterministic, label-independent source-value evidence */
	public String getValueSnapshotToken() {
		return valueSnapshotToken;
	}

	public SpatialPointCertificate getSemanticCertificate() {
		return semanticCertificate;
	}

	/** @return whether the evidence still names the registry's current inputs */
	public boolean isCurrentRevision() {
		return currentRevision;
	}
}
