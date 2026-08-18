/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Inert typed identity and definition record for one map-to-map relation. */
public final class ProjectionFrameRelationRecord implements SpatialIdentityRecord {
	/** Version-two typed hinge-unfold relation token. */
	public static final String HINGE_UNFOLD = "HINGE_UNFOLD";

	/** Version-two typed auxiliary change-of-plane relation token. */
	public static final String CHANGE_OF_PLANE = "CHANGE_OF_PLANE";

	/** Support-line direction agrees with the declared frame-intersection direction. */
	public static final String POSITIVE_ORIENTATION = "POSITIVE";

	/** Support-line direction opposes the declared frame-intersection direction. */
	public static final String NEGATIVE_ORIENTATION = "NEGATIVE";

	/** Relation geometry comes from explicit ordinary construction inputs. */
	public static final String EXPLICIT_CONSTRUCTION = "EXPLICIT_CONSTRUCTION";

	private final ProjectionFrameRelationId id;
	private final int semanticVersion;
	private final ProjectionSystemId systemId;
	private final ProjectionDiagramMapId sourceMapId;
	private final ProjectionDiagramMapId destinationMapId;
	private final String relationKind;
	private final List<PersistentGeoId> definitionGeoIds;
	private final PersistentGeoId supportStartGeoId;
	private final PersistentGeoId supportEndGeoId;
	private final String orientation;
	private final String provenance;
	private final PersistentGeoId foldSignGeoId;
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
		if (semanticVersion != 1) {
			throw new IllegalArgumentException(
					"The inert frame-relation constructor requires semanticVersion 1");
		}
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = semanticVersion;
		this.systemId = Objects.requireNonNull(systemId);
		this.sourceMapId = Objects.requireNonNull(sourceMapId);
		this.destinationMapId = Objects.requireNonNull(destinationMapId);
		this.relationKind = SpatialRecordSupport.requireText(relationKind,
				"relationKind");
		this.definitionGeoIds = SpatialRecordSupport.immutableIds(definitionGeoIds);
		this.supportStartGeoId = null;
		this.supportEndGeoId = null;
		this.orientation = null;
		this.provenance = null;
		this.foldSignGeoId = null;
		this.revision = SpatialRecordSupport.requireRevision(revision, "revision");
		this.copySourceId = copySourceId;
	}

	/** Creates a version-two relation from an explicit oriented support line. */
	public ProjectionFrameRelationRecord(ProjectionFrameRelationId id,
			int semanticVersion, ProjectionSystemId systemId,
			ProjectionDiagramMapId sourceMapId,
			ProjectionDiagramMapId destinationMapId, String relationKind,
			PersistentGeoId supportStartGeoId, PersistentGeoId supportEndGeoId,
			String orientation, String provenance, PersistentGeoId foldSignGeoId,
			long revision) {
		this(id, semanticVersion, systemId, sourceMapId, destinationMapId,
				relationKind, supportStartGeoId, supportEndGeoId, orientation,
				provenance, foldSignGeoId, revision, null);
	}

	/** Creates a version-two relation with optional immediate copy lineage. */
	public ProjectionFrameRelationRecord(ProjectionFrameRelationId id,
			int semanticVersion, ProjectionSystemId systemId,
			ProjectionDiagramMapId sourceMapId,
			ProjectionDiagramMapId destinationMapId, String relationKind,
			PersistentGeoId supportStartGeoId, PersistentGeoId supportEndGeoId,
			String orientation, String provenance, PersistentGeoId foldSignGeoId,
			long revision,
			ProjectionFrameRelationId copySourceId) {
		if (semanticVersion != 2) {
			throw new IllegalArgumentException(
					"The semantic frame-relation constructor requires semanticVersion 2");
		}
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = semanticVersion;
		this.systemId = Objects.requireNonNull(systemId);
		this.sourceMapId = Objects.requireNonNull(sourceMapId);
		this.destinationMapId = Objects.requireNonNull(destinationMapId);
		this.relationKind = SpatialRecordSupport.requireText(relationKind,
				"relationKind");
		this.supportStartGeoId = Objects.requireNonNull(supportStartGeoId,
				"supportStartGeoId");
		this.supportEndGeoId = Objects.requireNonNull(supportEndGeoId,
				"supportEndGeoId");
		if (supportStartGeoId.equals(supportEndGeoId)) {
			throw new IllegalArgumentException(
					"A relation support line requires two distinct geo identities");
		}
		this.orientation = SpatialRecordSupport.requireText(orientation,
				"orientation");
		if (!POSITIVE_ORIENTATION.equals(orientation)
				&& !NEGATIVE_ORIENTATION.equals(orientation)) {
			throw new IllegalArgumentException(
					"Unsupported relation support orientation: " + orientation);
		}
		this.provenance = SpatialRecordSupport.requireText(provenance,
				"provenance");
		if (!EXPLICIT_CONSTRUCTION.equals(provenance)) {
			throw new IllegalArgumentException(
					"Version-two relation provenance must be explicit construction");
		}
		ArrayList<PersistentGeoId> definitions = new ArrayList<>();
		definitions.add(supportStartGeoId);
		definitions.add(supportEndGeoId);
		if (HINGE_UNFOLD.equals(relationKind)) {
			this.foldSignGeoId = Objects.requireNonNull(foldSignGeoId,
					"foldSignGeoId");
			definitions.add(foldSignGeoId);
		} else if (CHANGE_OF_PLANE.equals(relationKind)) {
			if (foldSignGeoId != null) {
				throw new IllegalArgumentException(
						"CHANGE_OF_PLANE must not persist an unused fold sign");
			}
			this.foldSignGeoId = null;
		} else {
			throw new IllegalArgumentException(
					"Unsupported version-two frame relation kind: " + relationKind);
		}
		this.definitionGeoIds = SpatialRecordSupport.immutableIds(definitions);
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

	/** @return first explicitly constructed support-line point identity */
	public PersistentGeoId getSupportStartGeoId() {
		return supportStartGeoId;
	}

	/** @return second explicitly constructed support-line point identity */
	public PersistentGeoId getSupportEndGeoId() {
		return supportEndGeoId;
	}

	/** @return declared support-line orientation relative to the frame pair */
	public String getOrientation() {
		return orientation;
	}

	/** @return explicit construction provenance token */
	public String getProvenance() {
		return provenance;
	}

	/** @return the oriented-hinge fold input, or {@code null} when not applicable */
	public PersistentGeoId getFoldSignGeoId() {
		return foldSignGeoId;
	}

	public long getRevision() {
		return revision;
	}

	@Override
	public ProjectionFrameRelationRecord remap(
			Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource) {
		if (semanticVersion == 2) {
			return new ProjectionFrameRelationRecord(
					SpatialRecordSupport.remap(id, remap), semanticVersion,
					SpatialRecordSupport.remap(systemId, remap),
					SpatialRecordSupport.remap(sourceMapId, remap),
					SpatialRecordSupport.remap(destinationMapId, remap), relationKind,
					SpatialRecordSupport.remap(supportStartGeoId, remap),
					SpatialRecordSupport.remap(supportEndGeoId, remap), orientation,
					provenance,
					foldSignGeoId == null ? null
							: SpatialRecordSupport.remap(foldSignGeoId, remap),
					revision, recordImmediateCopySource ? id : copySourceId);
		}
		return new ProjectionFrameRelationRecord(SpatialRecordSupport.remap(id, remap),
				semanticVersion, SpatialRecordSupport.remap(systemId, remap),
				SpatialRecordSupport.remap(sourceMapId, remap),
				SpatialRecordSupport.remap(destinationMapId, remap), relationKind,
				SpatialRecordSupport.remap(definitionGeoIds, remap), revision,
				recordImmediateCopySource ? id : copySourceId);
	}
}
