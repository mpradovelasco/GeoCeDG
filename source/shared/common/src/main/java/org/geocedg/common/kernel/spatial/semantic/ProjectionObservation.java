/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

import java.util.Objects;

/** One defining point observation in a frame's common-diagram map. */
public final class ProjectionObservation {
	private final String evidenceKey;
	private final String evidenceContentKey;
	private final ProjectionFrameDefinition frame;
	private final DiagramMapDefinition diagramMap;
	private final Vector2 diagramPoint;
	private final SpatialDefinitionStatus definitionStatus;
	private final RepresentationFidelity fidelity;
	private final CorrespondenceStatus correspondenceStatus;
	private final long revision;

	/**
	 * Creates a defined numerical observation with no correspondence requirement.
	 *
	 * @param frame defining projection frame
	 * @param diagramMap frame-to-diagram map
	 * @param diagramPoint observed common-diagram point
	 */
	public ProjectionObservation(ProjectionFrameDefinition frame,
			DiagramMapDefinition diagramMap, Vector2 diagramPoint) {
		this(frame, diagramMap, diagramPoint, SpatialDefinitionStatus.DEFINED,
				RepresentationFidelity.NUMERICAL, CorrespondenceStatus.NOT_REQUIRED,
				Math.max(frame.getRevision(), diagramMap.getRevision()));
	}

	/**
	 * Creates a fully qualified defining observation.
	 *
	 * @param frame defining projection frame
	 * @param diagramMap frame-to-diagram map
	 * @param diagramPoint observed common-diagram point
	 * @param definitionStatus source definition status
	 * @param fidelity observation fidelity
	 * @param correspondenceStatus point correspondence status
	 * @param revision source revision
	 */
	public ProjectionObservation(ProjectionFrameDefinition frame,
			DiagramMapDefinition diagramMap, Vector2 diagramPoint,
			SpatialDefinitionStatus definitionStatus,
			RepresentationFidelity fidelity,
			CorrespondenceStatus correspondenceStatus, long revision) {
		this(SemanticEvidenceKey.observation(frame, diagramMap, diagramPoint,
				definitionStatus, fidelity, correspondenceStatus, revision), frame,
				diagramMap, diagramPoint, definitionStatus, fidelity,
				correspondenceStatus, revision);
	}

	/**
	 * Creates a fully qualified defining observation with a stable evidence key.
	 *
	 * @param evidenceKey stable semantic or durable binding identity
	 * @param frame defining projection frame
	 * @param diagramMap frame-to-diagram map
	 * @param diagramPoint observed common-diagram point
	 * @param definitionStatus source definition status
	 * @param fidelity observation fidelity
	 * @param correspondenceStatus point correspondence status
	 * @param revision source revision
	 */
	public ProjectionObservation(String evidenceKey, ProjectionFrameDefinition frame,
			DiagramMapDefinition diagramMap, Vector2 diagramPoint,
			SpatialDefinitionStatus definitionStatus,
			RepresentationFidelity fidelity,
			CorrespondenceStatus correspondenceStatus, long revision) {
		this.evidenceContentKey = SemanticEvidenceKey.observation(frame, diagramMap,
				diagramPoint, definitionStatus, fidelity, correspondenceStatus,
				revision);
		this.evidenceKey = SemanticEvidenceKey.require(evidenceKey);
		this.frame = Objects.requireNonNull(frame);
		this.diagramMap = Objects.requireNonNull(diagramMap);
		this.diagramPoint = Objects.requireNonNull(diagramPoint);
		this.definitionStatus = Objects.requireNonNull(definitionStatus);
		this.fidelity = Objects.requireNonNull(fidelity);
		this.correspondenceStatus = Objects.requireNonNull(correspondenceStatus);
		this.revision = SemanticValueChecks.requireRevision(revision);
	}

	public String getEvidenceKey() {
		return evidenceKey;
	}

	String getEvidenceContentKey() {
		return evidenceContentKey;
	}

	public ProjectionFrameDefinition getFrame() {
		return frame;
	}

	public DiagramMapDefinition getDiagramMap() {
		return diagramMap;
	}

	public Vector2 getDiagramPoint() {
		return diagramPoint;
	}

	public SpatialDefinitionStatus getDefinitionStatus() {
		return definitionStatus;
	}

	public RepresentationFidelity getFidelity() {
		return fidelity;
	}

	public CorrespondenceStatus getCorrespondenceStatus() {
		return correspondenceStatus;
	}

	public long getRevision() {
		return revision;
	}
}
