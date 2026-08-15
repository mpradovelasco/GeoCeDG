/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;

/** Pair capability output before independent two-sided verification. */
public final class LocusPairIntersectionCandidateSet2D {
	private final Completeness completeness;
	private final CompletenessMethod completenessMethod;
	private final GeometryKind geometryKind;
	private final SupportLevel supportLevel;
	private final NumericGuarantee numericGuarantee;
	private final List<String> coveredComponentPairKeys;
	private final List<LocusPairIntersectionCandidate2D> candidates;
	private final List<IntersectionOverlapEvidence2D> overlapEvidence;
	private final List<IntersectionDiagnostic2D> diagnostics;

	/** Creates orthogonal pair geometry and coverage evidence. */
	public LocusPairIntersectionCandidateSet2D(Completeness completeness,
			CompletenessMethod completenessMethod, GeometryKind geometryKind,
			SupportLevel supportLevel, NumericGuarantee numericGuarantee,
			List<String> coveredComponentPairKeys,
			List<LocusPairIntersectionCandidate2D> candidates,
			List<IntersectionOverlapEvidence2D> overlapEvidence,
			List<IntersectionDiagnostic2D> diagnostics) {
		this.completeness = java.util.Objects.requireNonNull(completeness);
		this.completenessMethod = java.util.Objects.requireNonNull(
				completenessMethod);
		this.geometryKind = java.util.Objects.requireNonNull(geometryKind);
		this.supportLevel = java.util.Objects.requireNonNull(supportLevel);
		this.numericGuarantee = java.util.Objects.requireNonNull(
				numericGuarantee);
		this.coveredComponentPairKeys = immutableUniqueStrings(
				coveredComponentPairKeys);
		this.candidates = immutableCandidates(candidates);
		this.overlapEvidence = immutableOverlap(overlapEvidence);
		this.diagnostics = immutableDiagnostics(diagnostics);
		validateShape();
	}

	public Completeness getCompleteness() {
		return completeness;
	}

	public CompletenessMethod getCompletenessMethod() {
		return completenessMethod;
	}

	public GeometryKind getGeometryKind() {
		return geometryKind;
	}

	public SupportLevel getSupportLevel() {
		return supportLevel;
	}

	public NumericGuarantee getNumericGuarantee() {
		return numericGuarantee;
	}

	public List<String> getCoveredComponentPairKeys() {
		return coveredComponentPairKeys;
	}

	public List<LocusPairIntersectionCandidate2D> getCandidates() {
		return candidates;
	}

	public List<IntersectionOverlapEvidence2D> getOverlapEvidence() {
		return overlapEvidence;
	}

	public List<IntersectionDiagnostic2D> getDiagnostics() {
		return diagnostics;
	}

	private void validateShape() {
		if (geometryKind == GeometryKind.EMPTY
				&& completeness != Completeness.COMPLETE) {
			throw new IllegalArgumentException(
					"Only exhaustive pair coverage may claim empty");
		}
		boolean mixed = geometryKind == GeometryKind.MIXED_FINITE_OVERLAP;
		if ((geometryKind == GeometryKind.FINITE || mixed)
				&& candidates.isEmpty()) {
			throw new IllegalArgumentException(
					"Finite pair geometry requires a candidate");
		}
		boolean overlapKind = geometryKind == GeometryKind.OVERLAP
				|| geometryKind == GeometryKind.INFINITELY_MANY
				|| geometryKind == GeometryKind.UNSUPPORTED_OVERLAP || mixed;
		if (overlapKind != !overlapEvidence.isEmpty()) {
			throw new IllegalArgumentException(
					"Typed pair overlap geometry and evidence must agree");
		}
		if (geometryKind != GeometryKind.FINITE && !mixed && !overlapKind
				&& !candidates.isEmpty()) {
			throw new IllegalArgumentException(
					"Unresolved pair geometry cannot carry candidates");
		}
	}

	private static List<String> immutableUniqueStrings(List<String> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<String> copy = new ArrayList<>();
		for (String value : input) {
			if (value == null || value.trim().isEmpty() || copy.contains(value)) {
				throw new IllegalArgumentException(
						"Covered pair keys must be unique and nonblank");
			}
			copy.add(value);
		}
		return Collections.unmodifiableList(copy);
	}

	private static List<LocusPairIntersectionCandidate2D> immutableCandidates(
			List<LocusPairIntersectionCandidate2D> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<LocusPairIntersectionCandidate2D> copy = new ArrayList<>();
		for (LocusPairIntersectionCandidate2D value : input) {
			copy.add(java.util.Objects.requireNonNull(value));
		}
		return Collections.unmodifiableList(copy);
	}

	private static List<IntersectionOverlapEvidence2D> immutableOverlap(
			List<IntersectionOverlapEvidence2D> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<IntersectionOverlapEvidence2D> copy = new ArrayList<>();
		for (IntersectionOverlapEvidence2D value : input) {
			copy.add(java.util.Objects.requireNonNull(value));
		}
		return Collections.unmodifiableList(copy);
	}

	private static List<IntersectionDiagnostic2D> immutableDiagnostics(
			List<IntersectionDiagnostic2D> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<IntersectionDiagnostic2D> copy = new ArrayList<>();
		for (IntersectionDiagnostic2D value : input) {
			copy.add(java.util.Objects.requireNonNull(value));
		}
		return Collections.unmodifiableList(copy);
	}
}
