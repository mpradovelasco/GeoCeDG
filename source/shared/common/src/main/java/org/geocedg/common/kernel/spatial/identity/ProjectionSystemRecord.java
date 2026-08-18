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
	private final String units;
	private final double absoluteTolerance;
	private final double relativeTolerance;
	private final double rankTolerance;
	private final double mapTolerance;
	private final double hingeTolerance;
	private final double conditionLimit;
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
		if (semanticVersion != 1) {
			throw new IllegalArgumentException(
					"The inert projection-system constructor requires semanticVersion 1");
		}
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = semanticVersion;
		this.mapIds = SpatialRecordSupport.immutableIds(mapIds);
		this.relationIds = SpatialRecordSupport.immutableIds(relationIds);
		this.definitionGeoIds = SpatialRecordSupport.immutableIds(definitionGeoIds);
		this.units = null;
		this.absoluteTolerance = Double.NaN;
		this.relativeTolerance = Double.NaN;
		this.rankTolerance = Double.NaN;
		this.mapTolerance = Double.NaN;
		this.hingeTolerance = Double.NaN;
		this.conditionLimit = Double.NaN;
		this.revision = SpatialRecordSupport.requireRevision(revision, "revision");
		this.copySourceId = copySourceId;
	}

	/** Creates a version-two system with an explicit numeric policy. */
	public ProjectionSystemRecord(ProjectionSystemId id, int semanticVersion,
			List<ProjectionDiagramMapId> mapIds,
			List<ProjectionFrameRelationId> relationIds, String units,
			double absoluteTolerance, double relativeTolerance, double rankTolerance,
			double mapTolerance, double hingeTolerance, double conditionLimit,
			long revision) {
		this(id, semanticVersion, mapIds, relationIds, units, absoluteTolerance,
				relativeTolerance, rankTolerance, mapTolerance, hingeTolerance,
				conditionLimit, revision, null);
	}

	/** Creates a version-two system with optional immediate copy lineage. */
	public ProjectionSystemRecord(ProjectionSystemId id, int semanticVersion,
			List<ProjectionDiagramMapId> mapIds,
			List<ProjectionFrameRelationId> relationIds, String units,
			double absoluteTolerance, double relativeTolerance, double rankTolerance,
			double mapTolerance, double hingeTolerance, double conditionLimit,
			long revision, ProjectionSystemId copySourceId) {
		if (semanticVersion != 2) {
			throw new IllegalArgumentException(
					"The semantic projection-system constructor requires semanticVersion 2");
		}
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = semanticVersion;
		this.mapIds = SpatialRecordSupport.immutableIds(mapIds);
		this.relationIds = SpatialRecordSupport.immutableIds(relationIds);
		this.definitionGeoIds = java.util.Collections.emptyList();
		this.units = SpatialRecordSupport.requireText(units, "units");
		this.absoluteTolerance = requireFinitePositive(absoluteTolerance,
				"absoluteTolerance");
		this.relativeTolerance = requireFinitePositive(relativeTolerance,
				"relativeTolerance");
		this.rankTolerance = requireFinitePositive(rankTolerance, "rankTolerance");
		this.mapTolerance = requireFinitePositive(mapTolerance, "mapTolerance");
		this.hingeTolerance = requireFinitePositive(hingeTolerance,
				"hingeTolerance");
		this.conditionLimit = requireFinitePositive(conditionLimit, "conditionLimit");
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

	/** @return the explicit version-two unit token, or {@code null} for v1 */
	public String getUnits() {
		return units;
	}

	public double getAbsoluteTolerance() {
		return absoluteTolerance;
	}

	public double getRelativeTolerance() {
		return relativeTolerance;
	}

	public double getRankTolerance() {
		return rankTolerance;
	}

	public double getMapTolerance() {
		return mapTolerance;
	}

	public double getHingeTolerance() {
		return hingeTolerance;
	}

	public double getConditionLimit() {
		return conditionLimit;
	}

	public long getRevision() {
		return revision;
	}

	@Override
	public ProjectionSystemRecord remap(Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource) {
		if (semanticVersion == 2) {
			return new ProjectionSystemRecord(SpatialRecordSupport.remap(id, remap),
					semanticVersion, SpatialRecordSupport.remap(mapIds, remap),
					SpatialRecordSupport.remap(relationIds, remap), units,
					absoluteTolerance, relativeTolerance, rankTolerance, mapTolerance,
					hingeTolerance, conditionLimit, revision,
					recordImmediateCopySource ? id : copySourceId);
		}
		return new ProjectionSystemRecord(SpatialRecordSupport.remap(id, remap),
				semanticVersion, SpatialRecordSupport.remap(mapIds, remap),
				SpatialRecordSupport.remap(relationIds, remap),
				SpatialRecordSupport.remap(definitionGeoIds, remap), revision,
				recordImmediateCopySource ? id : copySourceId);
	}

	private static double requireFinitePositive(double value, String name) {
		if (!Double.isFinite(value) || value <= 0) {
			throw new IllegalArgumentException(name + " must be finite and positive");
		}
		return value;
	}
}
