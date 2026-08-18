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
	private final PersistentGeoId projectedPointGeoId;
	private final String fidelity;
	private final String correspondence;
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
		if (semanticVersion != 1) {
			throw new IllegalArgumentException(
					"The inert projection-binding constructor requires semanticVersion 1");
		}
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = semanticVersion;
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
		this.projectedPointGeoId = null;
		this.fidelity = null;
		this.correspondence = null;
		this.revision = SpatialRecordSupport.requireRevision(revision, "revision");
		this.copySourceId = copySourceId;
	}

	/** Creates a version-two point binding with one explicit projected point. */
	public ProjectionBindingRecord(ProjectionBindingId id, int semanticVersion,
			SpatialObjectId objectId, ProjectionSystemId systemId,
			ProjectionDiagramMapId diagramMapId, ProjectionFrameId frameId,
			ProjectionBindingRole role, String representationType,
			String expectedSpatialType, String schemaId, int schemaVersion,
			PersistentGeoId projectedPointGeoId, String fidelity,
			String correspondence, long revision) {
		this(id, semanticVersion, objectId, systemId, diagramMapId, frameId, role,
				representationType, expectedSpatialType, schemaId, schemaVersion,
				projectedPointGeoId, fidelity, correspondence, revision, null);
	}

	/** Creates a version-two point binding with optional immediate copy lineage. */
	public ProjectionBindingRecord(ProjectionBindingId id, int semanticVersion,
			SpatialObjectId objectId, ProjectionSystemId systemId,
			ProjectionDiagramMapId diagramMapId, ProjectionFrameId frameId,
			ProjectionBindingRole role, String representationType,
			String expectedSpatialType, String schemaId, int schemaVersion,
			PersistentGeoId projectedPointGeoId, String fidelity,
			String correspondence, long revision, ProjectionBindingId copySourceId) {
		if (semanticVersion != 2) {
			throw new IllegalArgumentException(
					"The semantic projection-binding constructor requires semanticVersion 2");
		}
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = semanticVersion;
		this.objectId = Objects.requireNonNull(objectId);
		this.systemId = Objects.requireNonNull(systemId);
		this.diagramMapId = Objects.requireNonNull(diagramMapId);
		this.frameId = Objects.requireNonNull(frameId);
		this.role = Objects.requireNonNull(role);
		this.representationType = SpatialRecordSupport.requireText(representationType,
				"representationType");
		if (!SpatialObjectRecord.POINT_TYPE.equals(this.representationType)) {
			throw new IllegalArgumentException(
					"Version-two projection bindings support POINT only");
		}
		this.expectedSpatialType = SpatialRecordSupport.requireText(expectedSpatialType,
				"expectedSpatialType");
		if (!SpatialObjectRecord.POINT_TYPE.equals(this.expectedSpatialType)) {
			throw new IllegalArgumentException(
					"Version-two projection bindings require expected POINT type");
		}
		this.schemaId = SpatialRecordSupport.requireText(schemaId, "schemaId");
		this.schemaVersion = SpatialRecordSupport.requirePositive(schemaVersion,
				"schemaVersion");
		if (!SpatialObjectRecord.POINT_SCHEMA_ID.equals(this.schemaId)
				|| this.schemaVersion != SpatialObjectRecord.POINT_SCHEMA_VERSION) {
			throw new IllegalArgumentException(
					"Version-two projection bindings require the G9A2 point schema");
		}
		this.projectedPointGeoId = Objects.requireNonNull(projectedPointGeoId);
		this.projectedGeoIds = SpatialRecordSupport.immutableIds(
				java.util.Collections.singletonList(projectedPointGeoId));
		this.fidelity = SpatialRecordSupport.requireText(fidelity, "fidelity");
		this.correspondence = SpatialRecordSupport.requireText(correspondence,
				"correspondence");
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

	/** @return the explicit version-two projected point, or {@code null} for v1 */
	public PersistentGeoId getProjectedPointGeoId() {
		return projectedPointGeoId;
	}

	/** @return the explicit version-two fidelity token, or {@code null} for v1 */
	public String getFidelity() {
		return fidelity;
	}

	/** @return the explicit version-two correspondence token, or {@code null} for v1 */
	public String getCorrespondence() {
		return correspondence;
	}

	public long getRevision() {
		return revision;
	}

	@Override
	public ProjectionBindingRecord remap(Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource) {
		if (semanticVersion == 2) {
			return new ProjectionBindingRecord(SpatialRecordSupport.remap(id, remap),
					semanticVersion, SpatialRecordSupport.remap(objectId, remap),
					SpatialRecordSupport.remap(systemId, remap),
					SpatialRecordSupport.remap(diagramMapId, remap),
					SpatialRecordSupport.remap(frameId, remap), role, representationType,
					expectedSpatialType, schemaId, schemaVersion,
					SpatialRecordSupport.remap(projectedPointGeoId, remap), fidelity,
					correspondence, revision,
					recordImmediateCopySource ? id : copySourceId);
		}
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
