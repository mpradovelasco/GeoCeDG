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
	private final PersistentGeoId originGeoId;
	private final PersistentGeoId uGeoId;
	private final PersistentGeoId vGeoId;
	private final String family;
	private final String units;
	private final String handedness;
	private final String fidelity;
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
		if (semanticVersion != 1) {
			throw new IllegalArgumentException(
					"The inert projection-frame constructor requires semanticVersion 1");
		}
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = semanticVersion;
		this.definitionGeoIds = SpatialRecordSupport.immutableIds(definitionGeoIds);
		this.originGeoId = null;
		this.uGeoId = null;
		this.vGeoId = null;
		this.family = null;
		this.units = null;
		this.handedness = null;
		this.fidelity = null;
		this.revision = SpatialRecordSupport.requireRevision(revision, "revision");
		this.copySourceId = copySourceId;
	}

	/** Creates a version-two frame with explicit semantic input identities. */
	public ProjectionFrameRecord(ProjectionFrameId id, int semanticVersion,
			PersistentGeoId originGeoId, PersistentGeoId uGeoId,
			PersistentGeoId vGeoId, String family, String units, String handedness,
			String fidelity, long revision) {
		this(id, semanticVersion, originGeoId, uGeoId, vGeoId, family, units,
				handedness, fidelity, revision, null);
	}

	/** Creates a version-two frame with optional immediate copy lineage. */
	public ProjectionFrameRecord(ProjectionFrameId id, int semanticVersion,
			PersistentGeoId originGeoId, PersistentGeoId uGeoId,
			PersistentGeoId vGeoId, String family, String units, String handedness,
			String fidelity, long revision, ProjectionFrameId copySourceId) {
		if (semanticVersion != 2) {
			throw new IllegalArgumentException(
					"The semantic projection-frame constructor requires semanticVersion 2");
		}
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = semanticVersion;
		this.originGeoId = Objects.requireNonNull(originGeoId);
		this.uGeoId = Objects.requireNonNull(uGeoId);
		this.vGeoId = Objects.requireNonNull(vGeoId);
		this.definitionGeoIds = SpatialRecordSupport.immutableIds(
				java.util.Arrays.asList(originGeoId, uGeoId, vGeoId));
		this.family = SpatialRecordSupport.requireText(family, "family");
		this.units = SpatialRecordSupport.requireText(units, "units");
		this.handedness = SpatialRecordSupport.requireText(handedness, "handedness");
		this.fidelity = SpatialRecordSupport.requireText(fidelity, "fidelity");
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

	/** @return the explicit version-two origin input, or {@code null} for v1 */
	public PersistentGeoId getOriginGeoId() {
		return originGeoId;
	}

	/** @return the explicit version-two first basis input, or {@code null} for v1 */
	public PersistentGeoId getUGeoId() {
		return uGeoId;
	}

	/** @return the explicit version-two second basis input, or {@code null} for v1 */
	public PersistentGeoId getVGeoId() {
		return vGeoId;
	}

	/** @return the explicit version-two frame family, or {@code null} for v1 */
	public String getFamily() {
		return family;
	}

	/** @return the explicit version-two unit token, or {@code null} for v1 */
	public String getUnits() {
		return units;
	}

	/** @return the explicit version-two handedness token, or {@code null} for v1 */
	public String getHandedness() {
		return handedness;
	}

	/** @return the explicit version-two fidelity token, or {@code null} for v1 */
	public String getFidelity() {
		return fidelity;
	}

	public long getRevision() {
		return revision;
	}

	/** @return an immutable same-ID frame with the supplied revision */
	public ProjectionFrameRecord withRevision(long newRevision) {
		if (semanticVersion == 2) {
			return new ProjectionFrameRecord(id, semanticVersion, originGeoId, uGeoId,
					vGeoId, family, units, handedness, fidelity, newRevision,
					copySourceId);
		}
		return new ProjectionFrameRecord(id, semanticVersion, definitionGeoIds,
				newRevision, copySourceId);
	}

	@Override
	public ProjectionFrameRecord remap(Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource) {
		if (semanticVersion == 2) {
			return new ProjectionFrameRecord(SpatialRecordSupport.remap(id, remap),
					semanticVersion, SpatialRecordSupport.remap(originGeoId, remap),
					SpatialRecordSupport.remap(uGeoId, remap),
					SpatialRecordSupport.remap(vGeoId, remap), family, units, handedness,
					fidelity, revision,
					recordImmediateCopySource ? id : copySourceId);
		}
		return new ProjectionFrameRecord(SpatialRecordSupport.remap(id, remap),
				semanticVersion, SpatialRecordSupport.remap(definitionGeoIds, remap),
				revision, recordImmediateCopySource ? id : copySourceId);
	}
}
