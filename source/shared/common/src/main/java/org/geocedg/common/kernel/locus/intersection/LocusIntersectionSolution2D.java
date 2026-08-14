/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusPoint2D;

/** Deeply immutable verified finite semantic intersection solution. */
public final class LocusIntersectionSolution2D {
	private final IntersectionRootIdentity2D identity;
	private final IntersectionRootRevisionEvidence2D revisionEvidence;
	private final LocusPoint2D evaluatedPoint;
	private final IntersectionClassification2D classification;
	private final IntersectionRootLineage2D lineage;
	private final List<IntersectionDiagnostic2D> diagnostics;

	/** Creates a verified solution; candidates cannot bypass this value. */
	public LocusIntersectionSolution2D(IntersectionRootIdentity2D identity,
			IntersectionRootRevisionEvidence2D revisionEvidence,
			LocusPoint2D evaluatedPoint,
			IntersectionClassification2D classification,
			IntersectionRootLineage2D lineage,
			List<IntersectionDiagnostic2D> diagnostics) {
		this.identity = java.util.Objects.requireNonNull(identity);
		this.revisionEvidence = java.util.Objects.requireNonNull(revisionEvidence);
		this.evaluatedPoint = java.util.Objects.requireNonNull(evaluatedPoint);
		if (!Double.isFinite(evaluatedPoint.getX())
				|| !Double.isFinite(evaluatedPoint.getY())) {
			throw new IllegalArgumentException("Published point must be finite");
		}
		this.classification = java.util.Objects.requireNonNull(classification);
		this.lineage = java.util.Objects.requireNonNull(lineage);
		this.diagnostics = immutableDiagnostics(diagnostics);
	}

	public IntersectionRootIdentity2D getIdentity() {
		return identity;
	}

	public IntersectionRootRevisionEvidence2D getRevisionEvidence() {
		return revisionEvidence;
	}

	public LocusPoint2D getEvaluatedPoint() {
		return evaluatedPoint;
	}

	public IntersectionClassification2D getClassification() {
		return classification;
	}

	public IntersectionRootLineage2D getLineage() {
		return lineage;
	}

	public List<IntersectionDiagnostic2D> getDiagnostics() {
		return diagnostics;
	}

	private static List<IntersectionDiagnostic2D> immutableDiagnostics(
			List<IntersectionDiagnostic2D> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<IntersectionDiagnostic2D> copy = new ArrayList<>();
		for (IntersectionDiagnostic2D diagnostic : input) {
			copy.add(java.util.Objects.requireNonNull(diagnostic));
		}
		return Collections.unmodifiableList(copy);
	}
}
