/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Inert typed identity and definition record for one map-to-map relation. */
public final class ProjectionFrameRelationRecord implements SpatialIdentityRecord {
	private final ProjectionFrameRelationId id;
	private final int semanticVersion;
	private final ProjectionSystemId systemId;
	private final ProjectionDiagramMapId sourceMapId;
	private final ProjectionDiagramMapId destinationMapId;
	private final String relationKind;
	private final List<PersistentGeoId> definitionGeoIds;
	private final long revision;
	private final ProjectionFrameRelationId copySourceId;

	/** Creates an original inert frame-relation record. */
	public ProjectionFrameRelationRecord(ProjectionFrameRelationId id,
			int semanticVersion, ProjectionSystemId systemId,
			ProjectionDiagramMapId sourceMapId,
			ProjectionDiagramMapId destinationMapId, String relationKind,
			List<PersistentGeoId> definitionGeoIds, long revision) {
		this(id, semanticVersion, systemId, sourceMapId, destinationMapId,
				relationKind, definitionGeoIds, revision, null);
	}

	/** Creates an inert frame-relation record with optional immediate copy lineage. */
	public ProjectionFrameRelationRecord(ProjectionFrameRelationId id,
			int semanticVersion, ProjectionSystemId systemId,
			ProjectionDiagramMapId sourceMapId,
			ProjectionDiagramMapId destinationMapId, String relationKind,
			List<PersistentGeoId> definitionGeoIds, long revision,
			ProjectionFrameRelationId copySourceId) {
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = SpatialRecordSupport.requirePositive(semanticVersion,
				"semanticVersion");
		this.systemId = Objects.requireNonNull(systemId);
		this.sourceMapId = Objects.requireNonNull(sourceMapId);
		this.destinationMapId = Objects.requireNonNull(destinationMapId);
		this.relationKind = SpatialRecordSupport.requireText(relationKind,
				"relationKind");
		this.definitionGeoIds = SpatialRecordSupport.immutableIds(definitionGeoIds);
		this.revision = SpatialRecordSupport.requireRevision(revision, "revision");
		this.copySourceId = copySourceId;
	}

	@Override
	public ProjectionFrameRelationId getId() {
		return id;
	}

	@Override
	public int getSemanticVersion() {
		return semanticVersion;
	}

	@Override
	public String getXmlElementName() {
		return "frameRelation";
	}

	@Override
	public List<SpatialIdentityId> getReferences() {
		return SpatialRecordSupport.references(definitionGeoIds,
				java.util.Collections.<SpatialIdentityId>emptyList(), systemId,
				sourceMapId, destinationMapId);
	}

	@Override
	public ProjectionFrameRelationId getCopySourceId() {
		return copySourceId;
	}

	public ProjectionSystemId getSystemId() {
		return systemId;
	}

	public ProjectionDiagramMapId getSourceMapId() {
		return sourceMapId;
	}

	public ProjectionDiagramMapId getDestinationMapId() {
		return destinationMapId;
	}

	public String getRelationKind() {
		return relationKind;
	}

	public List<PersistentGeoId> getDefinitionGeoIds() {
		return definitionGeoIds;
	}

	public long getRevision() {
		return revision;
	}

	@Override
	public ProjectionFrameRelationRecord remap(
			Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource) {
		return new ProjectionFrameRelationRecord(SpatialRecordSupport.remap(id, remap),
				semanticVersion, SpatialRecordSupport.remap(systemId, remap),
				SpatialRecordSupport.remap(sourceMapId, remap),
				SpatialRecordSupport.remap(destinationMapId, remap), relationKind,
				SpatialRecordSupport.remap(definitionGeoIds, remap), revision,
				recordImmediateCopySource ? id : copySourceId);
	}
}
