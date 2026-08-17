/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Inert identity and definition-input record for a future projection frame. */
public final class ProjectionFrameRecord implements SpatialIdentityRecord {
	private final ProjectionFrameId id;
	private final int semanticVersion;
	private final List<PersistentGeoId> definitionGeoIds;
	private final long revision;
	private final ProjectionFrameId copySourceId;

	/** Creates an original inert projection-frame record. */
	public ProjectionFrameRecord(ProjectionFrameId id, int semanticVersion,
			List<PersistentGeoId> definitionGeoIds, long revision) {
		this(id, semanticVersion, definitionGeoIds, revision, null);
	}

	/** Creates an inert frame record with optional immediate copy lineage. */
	public ProjectionFrameRecord(ProjectionFrameId id, int semanticVersion,
			List<PersistentGeoId> definitionGeoIds, long revision,
			ProjectionFrameId copySourceId) {
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = SpatialRecordSupport.requirePositive(semanticVersion,
				"semanticVersion");
		this.definitionGeoIds = SpatialRecordSupport.immutableIds(definitionGeoIds);
		this.revision = SpatialRecordSupport.requireRevision(revision, "revision");
		this.copySourceId = copySourceId;
	}

	@Override
	public ProjectionFrameId getId() {
		return id;
	}

	@Override
	public int getSemanticVersion() {
		return semanticVersion;
	}

	@Override
	public String getXmlElementName() {
		return "frame";
	}

	@Override
	public List<SpatialIdentityId> getReferences() {
		return SpatialRecordSupport.references(definitionGeoIds,
				java.util.Collections.<SpatialIdentityId>emptyList());
	}

	@Override
	public ProjectionFrameId getCopySourceId() {
		return copySourceId;
	}

	public List<PersistentGeoId> getDefinitionGeoIds() {
		return definitionGeoIds;
	}

	public long getRevision() {
		return revision;
	}

	@Override
	public ProjectionFrameRecord remap(Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource) {
		return new ProjectionFrameRecord(SpatialRecordSupport.remap(id, remap),
				semanticVersion, SpatialRecordSupport.remap(definitionGeoIds, remap),
				revision, recordImmediateCopySource ? id : copySourceId);
	}
}
