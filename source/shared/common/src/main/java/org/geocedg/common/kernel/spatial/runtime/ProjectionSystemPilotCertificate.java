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
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemCertificate;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemStatus;

/**
 * Construction-owned publication evidence for one projection-system revision.
 */
public final class ProjectionSystemPilotCertificate {
	private final ProjectionSystemId systemId;
	private final Map<SpatialIdentityId, String> revisionTuple;
	private final String valueSnapshotToken;
	private final ProjectionSystemCertificate semanticCertificate;
	private final boolean currentRevision;

	ProjectionSystemPilotCertificate(ProjectionSystemId systemId,
			Map<SpatialIdentityId, String> revisionTuple, String valueSnapshotToken,
			ProjectionSystemCertificate semanticCertificate, boolean currentRevision) {
		this.systemId = Objects.requireNonNull(systemId);
		this.revisionTuple = Collections.unmodifiableMap(
				new LinkedHashMap<>(Objects.requireNonNull(revisionTuple)));
		this.valueSnapshotToken = Objects.requireNonNull(valueSnapshotToken);
		this.semanticCertificate = Objects.requireNonNull(semanticCertificate);
		this.currentRevision = currentRevision;
		if (!currentRevision && semanticCertificate.getStatus()
				!= ProjectionSystemStatus.NOT_EVALUATED) {
			throw new IllegalArgumentException(
					"noncurrent system evidence must not retain an evaluated status");
		}
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

	public ProjectionSystemCertificate getSemanticCertificate() {
		return semanticCertificate;
	}

	/** @return whether the evidence still names the registry's current inputs */
	public boolean isCurrentRevision() {
		return currentRevision;
	}
}
