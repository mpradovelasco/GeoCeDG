/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

import java.util.Objects;

/** Immutable declared-map evidence included in a projection-system certificate. */
public final class DiagramMapEvidence {
	private final String evidenceKey;
	private final String evidenceContentKey;
	private final DiagramMapFamily family;
	private final DiagramOrientation orientation;
	private final String sourceUnit;
	private final String diagramUnit;
	private final double declaredScale;
	private final long revision;
	private final ProjectionSystemStatus status;

	/**
	 * Creates immutable evidence for one evaluated diagram map.
	 *
	 * @param family admitted map family
	 * @param orientation declared determinant orientation
	 * @param sourceUnit referenced frame/source unit
	 * @param diagramUnit target common-diagram unit
	 * @param declaredScale declared unit-similarity scale
	 * @param revision map semantic revision
	 * @param status evaluated map status
	 */
	public DiagramMapEvidence(DiagramMapFamily family,
			DiagramOrientation orientation, String sourceUnit, String diagramUnit,
			double declaredScale, long revision, ProjectionSystemStatus status) {
		this(SemanticEvidenceKey.mapEvidence(family, orientation, sourceUnit,
				diagramUnit, declaredScale, revision, status), family, orientation,
				sourceUnit, diagramUnit, declaredScale, revision, status);
	}

	/** Creates immutable keyed evidence for one evaluated diagram map. */
	public DiagramMapEvidence(String evidenceKey, DiagramMapFamily family,
			DiagramOrientation orientation, String sourceUnit, String diagramUnit,
			double declaredScale, long revision, ProjectionSystemStatus status) {
		this.evidenceContentKey = SemanticEvidenceKey.mapEvidence(family,
				orientation, sourceUnit, diagramUnit, declaredScale, revision, status);
		this.evidenceKey = SemanticEvidenceKey.require(evidenceKey);
		this.family = Objects.requireNonNull(family);
		this.orientation = Objects.requireNonNull(orientation);
		this.sourceUnit = Objects.requireNonNull(sourceUnit);
		this.diagramUnit = Objects.requireNonNull(diagramUnit);
		this.declaredScale = declaredScale;
		this.revision = revision;
		this.status = Objects.requireNonNull(status);
	}

	public String getEvidenceKey() {
		return evidenceKey;
	}

	String getEvidenceContentKey() {
		return evidenceContentKey;
	}

	public DiagramMapFamily getFamily() {
		return family;
	}

	public DiagramOrientation getOrientation() {
		return orientation;
	}

	public String getSourceUnit() {
		return sourceUnit;
	}

	public String getDiagramUnit() {
		return diagramUnit;
	}

	public double getDeclaredScale() {
		return declaredScale;
	}

	public long getRevision() {
		return revision;
	}

	public ProjectionSystemStatus getStatus() {
		return status;
	}
}
