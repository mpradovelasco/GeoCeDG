/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Inert identity and typed-reference record for one diagram map. */
public final class ProjectionDiagramMapRecord implements SpatialIdentityRecord {
	private final ProjectionDiagramMapId id;
	private final int semanticVersion;
	private final ProjectionSystemId systemId;
	private final ProjectionFrameId frameId;
	private final ProjectionFrameUseRole frameUseRole;
	private final String family;
	private final List<ProjectionFrameRelationId> relationIds;
	private final List<PersistentGeoId> definitionGeoIds;
	private final long revision;
	private final ProjectionDiagramMapId copySourceId;

	/** Creates an original inert diagram-map record. */
	public ProjectionDiagramMapRecord(ProjectionDiagramMapId id, int semanticVersion,
			ProjectionSystemId systemId, ProjectionFrameId frameId,
			ProjectionFrameUseRole frameUseRole, String family,
			List<ProjectionFrameRelationId> relationIds,
			List<PersistentGeoId> definitionGeoIds, long revision) {
		this(id, semanticVersion, systemId, frameId, frameUseRole, family, relationIds,
				definitionGeoIds, revision, null);
	}

	/** Creates an inert diagram-map record with optional immediate copy lineage. */
	public ProjectionDiagramMapRecord(ProjectionDiagramMapId id, int semanticVersion,
			ProjectionSystemId systemId, ProjectionFrameId frameId,
			ProjectionFrameUseRole frameUseRole, String family,
			List<ProjectionFrameRelationId> relationIds,
			List<PersistentGeoId> definitionGeoIds, long revision,
			ProjectionDiagramMapId copySourceId) {
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = SpatialRecordSupport.requirePositive(semanticVersion,
				"semanticVersion");
		this.systemId = Objects.requireNonNull(systemId);
		this.frameId = Objects.requireNonNull(frameId);
		this.frameUseRole = Objects.requireNonNull(frameUseRole);
		this.family = SpatialRecordSupport.requireText(family, "family");
		this.relationIds = SpatialRecordSupport.immutableIds(relationIds);
		this.definitionGeoIds = SpatialRecordSupport.immutableIds(definitionGeoIds);
		this.revision = SpatialRecordSupport.requireRevision(revision, "revision");
		this.copySourceId = copySourceId;
	}

	@Override
	public ProjectionDiagramMapId getId() {
		return id;
	}

	@Override
	public int getSemanticVersion() {
		return semanticVersion;
	}

	@Override
	public String getXmlElementName() {
		return "diagramMap";
	}

	@Override
	public List<SpatialIdentityId> getReferences() {
		return SpatialRecordSupport.references(relationIds, definitionGeoIds,
				systemId, frameId);
	}

	@Override
	public ProjectionDiagramMapId getCopySourceId() {
		return copySourceId;
	}

	public ProjectionSystemId getSystemId() {
		return systemId;
	}

	public ProjectionFrameId getFrameId() {
		return frameId;
	}

	public ProjectionFrameUseRole getFrameUseRole() {
		return frameUseRole;
	}

	public String getFamily() {
		return family;
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
	public ProjectionDiagramMapRecord remap(
			Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource) {
		return new ProjectionDiagramMapRecord(SpatialRecordSupport.remap(id, remap),
				semanticVersion, SpatialRecordSupport.remap(systemId, remap),
				SpatialRecordSupport.remap(frameId, remap), frameUseRole, family,
				SpatialRecordSupport.remap(relationIds, remap),
				SpatialRecordSupport.remap(definitionGeoIds, remap), revision,
				recordImmediateCopySource ? id : copySourceId);
	}
}
