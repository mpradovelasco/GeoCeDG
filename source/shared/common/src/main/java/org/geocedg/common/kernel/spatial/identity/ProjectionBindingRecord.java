/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Inert typed relation from one spatial object to projected ordinary geos. */
public final class ProjectionBindingRecord implements SpatialIdentityRecord {
	private final ProjectionBindingId id;
	private final int semanticVersion;
	private final SpatialObjectId objectId;
	private final ProjectionSystemId systemId;
	private final ProjectionDiagramMapId diagramMapId;
	private final ProjectionFrameId frameId;
	private final ProjectionBindingRole role;
	private final String representationType;
	private final String expectedSpatialType;
	private final String schemaId;
	private final int schemaVersion;
	private final List<PersistentGeoId> projectedGeoIds;
	private final long revision;
	private final ProjectionBindingId copySourceId;

	/** Creates an original inert projection-binding record. */
	public ProjectionBindingRecord(ProjectionBindingId id, int semanticVersion,
			SpatialObjectId objectId, ProjectionSystemId systemId,
			ProjectionDiagramMapId diagramMapId, ProjectionFrameId frameId,
			ProjectionBindingRole role, String representationType,
			String expectedSpatialType, String schemaId, int schemaVersion,
			List<PersistentGeoId> projectedGeoIds, long revision) {
		this(id, semanticVersion, objectId, systemId, diagramMapId, frameId, role,
				representationType, expectedSpatialType, schemaId, schemaVersion,
				projectedGeoIds, revision, null);
	}

	/** Creates an inert binding record with optional immediate copy lineage. */
	public ProjectionBindingRecord(ProjectionBindingId id, int semanticVersion,
			SpatialObjectId objectId, ProjectionSystemId systemId,
			ProjectionDiagramMapId diagramMapId, ProjectionFrameId frameId,
			ProjectionBindingRole role, String representationType,
			String expectedSpatialType, String schemaId, int schemaVersion,
			List<PersistentGeoId> projectedGeoIds, long revision,
			ProjectionBindingId copySourceId) {
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = SpatialRecordSupport.requirePositive(semanticVersion,
				"semanticVersion");
		this.objectId = Objects.requireNonNull(objectId);
		this.systemId = Objects.requireNonNull(systemId);
		this.diagramMapId = Objects.requireNonNull(diagramMapId);
		this.frameId = Objects.requireNonNull(frameId);
		this.role = Objects.requireNonNull(role);
		this.representationType = SpatialRecordSupport.requireText(representationType,
				"representationType");
		this.expectedSpatialType = SpatialRecordSupport.requireText(expectedSpatialType,
				"expectedSpatialType");
		this.schemaId = SpatialRecordSupport.requireText(schemaId, "schemaId");
		this.schemaVersion = SpatialRecordSupport.requirePositive(schemaVersion,
				"schemaVersion");
		this.projectedGeoIds = SpatialRecordSupport.immutableIds(projectedGeoIds);
		this.revision = SpatialRecordSupport.requireRevision(revision, "revision");
		this.copySourceId = copySourceId;
	}

	@Override
	public ProjectionBindingId getId() {
		return id;
	}

	@Override
	public int getSemanticVersion() {
		return semanticVersion;
	}

	@Override
	public String getXmlElementName() {
		return "binding";
	}

	@Override
	public List<SpatialIdentityId> getReferences() {
		return SpatialRecordSupport.references(projectedGeoIds,
				java.util.Collections.<SpatialIdentityId>emptyList(), objectId, systemId,
				diagramMapId, frameId);
	}

	@Override
	public ProjectionBindingId getCopySourceId() {
		return copySourceId;
	}

	public SpatialObjectId getObjectId() {
		return objectId;
	}

	public ProjectionSystemId getSystemId() {
		return systemId;
	}

	public ProjectionDiagramMapId getDiagramMapId() {
		return diagramMapId;
	}

	public ProjectionFrameId getFrameId() {
		return frameId;
	}

	public ProjectionBindingRole getRole() {
		return role;
	}

	public String getRepresentationType() {
		return representationType;
	}

	public String getExpectedSpatialType() {
		return expectedSpatialType;
	}

	public String getSchemaId() {
		return schemaId;
	}

	public int getSchemaVersion() {
		return schemaVersion;
	}

	public List<PersistentGeoId> getProjectedGeoIds() {
		return projectedGeoIds;
	}

	public long getRevision() {
		return revision;
	}

	@Override
	public ProjectionBindingRecord remap(Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource) {
		return new ProjectionBindingRecord(SpatialRecordSupport.remap(id, remap),
				semanticVersion, SpatialRecordSupport.remap(objectId, remap),
				SpatialRecordSupport.remap(systemId, remap),
				SpatialRecordSupport.remap(diagramMapId, remap),
				SpatialRecordSupport.remap(frameId, remap), role, representationType,
				expectedSpatialType, schemaId, schemaVersion,
				SpatialRecordSupport.remap(projectedGeoIds, remap), revision,
				recordImmediateCopySource ? id : copySourceId);
	}
}
