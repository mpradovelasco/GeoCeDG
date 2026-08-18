/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

import java.util.Objects;

/** Resolved, typed geometric relation between two frame/map uses. */
public final class ProjectionRelationDefinition {
	private final String evidenceKey;
	private final String evidenceContentKey;
	private final ProjectionRelationKind kind;
	private final ProjectionFrameDefinition sourceFrame;
	private final DiagramMapDefinition sourceMap;
	private final ProjectionFrameDefinition destinationFrame;
	private final DiagramMapDefinition destinationMap;
	private final Vector3 supportStart;
	private final Vector3 supportEnd;
	private final ProjectionRelationOrientation orientation;
	private final ProjectionRelationProvenance provenance;
	private final FoldSide foldSide;
	private final long revision;

	/**
	 * Creates a resolved geometric relation between two frame/map uses.
	 *
	 * @param kind admitted relation kind
	 * @param sourceFrame source frame
	 * @param sourceMap source frame-to-diagram map
	 * @param destinationFrame destination frame
	 * @param destinationMap destination frame-to-diagram map
	 * @param supportStart oriented support-line start
	 * @param supportEnd oriented support-line end
	 * @param orientation declared support orientation
	 * @param provenance relation provenance
	 * @param foldSide declared fold side or not-applicable marker
	 * @param revision source revision
	 */
	public ProjectionRelationDefinition(ProjectionRelationKind kind,
			ProjectionFrameDefinition sourceFrame, DiagramMapDefinition sourceMap,
			ProjectionFrameDefinition destinationFrame,
			DiagramMapDefinition destinationMap, Vector3 supportStart,
			Vector3 supportEnd, ProjectionRelationOrientation orientation,
			ProjectionRelationProvenance provenance, FoldSide foldSide,
			long revision) {
		this(SemanticEvidenceKey.relation(kind, sourceFrame, sourceMap,
				destinationFrame, destinationMap, supportStart, supportEnd,
				orientation, provenance, foldSide, revision), kind, sourceFrame,
				sourceMap, destinationFrame, destinationMap, supportStart, supportEnd,
				orientation, provenance, foldSide, revision);
	}

	/** Creates a resolved relation with a stable semantic evidence key. */
	public ProjectionRelationDefinition(String evidenceKey,
			ProjectionRelationKind kind, ProjectionFrameDefinition sourceFrame,
			DiagramMapDefinition sourceMap,
			ProjectionFrameDefinition destinationFrame,
			DiagramMapDefinition destinationMap, Vector3 supportStart,
			Vector3 supportEnd, ProjectionRelationOrientation orientation,
			ProjectionRelationProvenance provenance, FoldSide foldSide,
			long revision) {
		this.evidenceContentKey = SemanticEvidenceKey.relation(kind, sourceFrame,
				sourceMap, destinationFrame, destinationMap, supportStart, supportEnd,
				orientation, provenance, foldSide, revision);
		this.evidenceKey = SemanticEvidenceKey.require(evidenceKey);
		this.kind = Objects.requireNonNull(kind);
		this.sourceFrame = Objects.requireNonNull(sourceFrame);
		this.sourceMap = Objects.requireNonNull(sourceMap);
		this.destinationFrame = Objects.requireNonNull(destinationFrame);
		this.destinationMap = Objects.requireNonNull(destinationMap);
		this.supportStart = Objects.requireNonNull(supportStart);
		this.supportEnd = Objects.requireNonNull(supportEnd);
		this.orientation = Objects.requireNonNull(orientation);
		this.provenance = Objects.requireNonNull(provenance);
		this.foldSide = Objects.requireNonNull(foldSide);
		this.revision = SemanticValueChecks.requireRevision(revision);
	}

	/**
	 * Creates an explicit hinge-unfold relation.
	 *
	 * @return hinge-unfold relation definition
	 */
	public static ProjectionRelationDefinition hingeUnfold(
			ProjectionFrameDefinition sourceFrame, DiagramMapDefinition sourceMap,
			ProjectionFrameDefinition destinationFrame,
			DiagramMapDefinition destinationMap, Vector3 supportStart,
			Vector3 supportEnd, ProjectionRelationOrientation orientation,
			ProjectionRelationProvenance provenance, FoldSide foldSide,
			long revision) {
		if (foldSide == FoldSide.NOT_APPLICABLE) {
			throw new IllegalArgumentException("hinge-unfold requires an explicit fold side");
		}
		return new ProjectionRelationDefinition(ProjectionRelationKind.HINGE_UNFOLD,
				sourceFrame, sourceMap, destinationFrame, destinationMap, supportStart,
				supportEnd, orientation, provenance, foldSide, revision);
	}

	/**
	 * Creates an explicit hinge-unfold relation with a stable evidence key.
	 *
	 * @return hinge-unfold relation definition
	 */
	public static ProjectionRelationDefinition hingeUnfold(String evidenceKey,
			ProjectionFrameDefinition sourceFrame, DiagramMapDefinition sourceMap,
			ProjectionFrameDefinition destinationFrame,
			DiagramMapDefinition destinationMap, Vector3 supportStart,
			Vector3 supportEnd, ProjectionRelationOrientation orientation,
			ProjectionRelationProvenance provenance, FoldSide foldSide,
			long revision) {
		if (foldSide == FoldSide.NOT_APPLICABLE) {
			throw new IllegalArgumentException("hinge-unfold requires an explicit fold side");
		}
		return new ProjectionRelationDefinition(evidenceKey,
				ProjectionRelationKind.HINGE_UNFOLD, sourceFrame, sourceMap,
				destinationFrame, destinationMap, supportStart, supportEnd,
				orientation, provenance, foldSide, revision);
	}

	/**
	 * Creates an explicit change-of-plane relation.
	 *
	 * @return change-of-plane relation definition
	 */
	public static ProjectionRelationDefinition changeOfPlane(
			ProjectionFrameDefinition sourceFrame, DiagramMapDefinition sourceMap,
			ProjectionFrameDefinition destinationFrame,
			DiagramMapDefinition destinationMap, Vector3 supportStart,
			Vector3 supportEnd, ProjectionRelationOrientation orientation,
			ProjectionRelationProvenance provenance, long revision) {
		return new ProjectionRelationDefinition(ProjectionRelationKind.CHANGE_OF_PLANE,
				sourceFrame, sourceMap, destinationFrame, destinationMap, supportStart,
				supportEnd, orientation, provenance, FoldSide.NOT_APPLICABLE, revision);
	}

	/**
	 * Creates an explicit change-of-plane relation with a stable evidence key.
	 *
	 * @return change-of-plane relation definition
	 */
	public static ProjectionRelationDefinition changeOfPlane(String evidenceKey,
			ProjectionFrameDefinition sourceFrame, DiagramMapDefinition sourceMap,
			ProjectionFrameDefinition destinationFrame,
			DiagramMapDefinition destinationMap, Vector3 supportStart,
			Vector3 supportEnd, ProjectionRelationOrientation orientation,
			ProjectionRelationProvenance provenance, long revision) {
		return new ProjectionRelationDefinition(evidenceKey,
				ProjectionRelationKind.CHANGE_OF_PLANE, sourceFrame, sourceMap,
				destinationFrame, destinationMap, supportStart, supportEnd,
				orientation, provenance, FoldSide.NOT_APPLICABLE, revision);
	}

	public String getEvidenceKey() {
		return evidenceKey;
	}

	String getEvidenceContentKey() {
		return evidenceContentKey;
	}

	public ProjectionRelationKind getKind() {
		return kind;
	}

	public ProjectionFrameDefinition getSourceFrame() {
		return sourceFrame;
	}

	public DiagramMapDefinition getSourceMap() {
		return sourceMap;
	}

	public ProjectionFrameDefinition getDestinationFrame() {
		return destinationFrame;
	}

	public DiagramMapDefinition getDestinationMap() {
		return destinationMap;
	}

	public Vector3 getSupportStart() {
		return supportStart;
	}

	public Vector3 getSupportEnd() {
		return supportEnd;
	}

	public ProjectionRelationOrientation getOrientation() {
		return orientation;
	}

	public ProjectionRelationProvenance getProvenance() {
		return provenance;
	}

	public FoldSide getFoldSide() {
		return foldSide;
	}

	public long getRevision() {
		return revision;
	}
}
