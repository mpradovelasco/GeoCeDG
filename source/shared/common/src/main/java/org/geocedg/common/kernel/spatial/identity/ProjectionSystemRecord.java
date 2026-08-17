/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Inert identity and closure record for one projection system. */
public final class ProjectionSystemRecord implements SpatialIdentityRecord {
	private final ProjectionSystemId id;
	private final int semanticVersion;
	private final List<ProjectionDiagramMapId> mapIds;
	private final List<ProjectionFrameRelationId> relationIds;
	private final List<PersistentGeoId> definitionGeoIds;
	private final long revision;
	private final ProjectionSystemId copySourceId;

	/** Creates an original inert projection-system record. */
	public ProjectionSystemRecord(ProjectionSystemId id, int semanticVersion,
			List<ProjectionDiagramMapId> mapIds,
			List<ProjectionFrameRelationId> relationIds,
			List<PersistentGeoId> definitionGeoIds, long revision) {
		this(id, semanticVersion, mapIds, relationIds, definitionGeoIds, revision, null);
	}

	/** Creates an inert system record with optional immediate copy lineage. */
	public ProjectionSystemRecord(ProjectionSystemId id, int semanticVersion,
			List<ProjectionDiagramMapId> mapIds,
			List<ProjectionFrameRelationId> relationIds,
			List<PersistentGeoId> definitionGeoIds, long revision,
			ProjectionSystemId copySourceId) {
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = SpatialRecordSupport.requirePositive(semanticVersion,
				"semanticVersion");
		this.mapIds = SpatialRecordSupport.immutableIds(mapIds);
		this.relationIds = SpatialRecordSupport.immutableIds(relationIds);
		this.definitionGeoIds = SpatialRecordSupport.immutableIds(definitionGeoIds);
		this.revision = SpatialRecordSupport.requireRevision(revision, "revision");
		this.copySourceId = copySourceId;
	}

	@Override
	public ProjectionSystemId getId() {
		return id;
	}

	@Override
	public int getSemanticVersion() {
		return semanticVersion;
	}

	@Override
	public String getXmlElementName() {
		return "system";
	}

	@Override
	public List<SpatialIdentityId> getReferences() {
		return SpatialRecordSupport.references(mapIds, relationIds,
				definitionGeoIds.toArray(new SpatialIdentityId[0]));
	}

	@Override
	public ProjectionSystemId getCopySourceId() {
		return copySourceId;
	}

	public List<ProjectionDiagramMapId> getMapIds() {
		return mapIds;
	}

	public List<ProjectionFrameRelationId> getRelationIds() {
		return relationIds;
	}

	public List<PersistentGeoId> getDefinitionGeoIds() {
		return definitionGeoIds;
	}

	public long getRevision() {
		return revision;
	}

	@Override
	public ProjectionSystemRecord remap(Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource) {
		return new ProjectionSystemRecord(SpatialRecordSupport.remap(id, remap),
				semanticVersion, SpatialRecordSupport.remap(mapIds, remap),
				SpatialRecordSupport.remap(relationIds, remap),
				SpatialRecordSupport.remap(definitionGeoIds, remap), revision,
				recordImmediateCopySource ? id : copySourceId);
	}
}
