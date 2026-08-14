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

/** Immutable capability output before independent semantic verification. */
public final class IntersectionCandidateSet2D {
	private final Completeness completeness;
	private final CompletenessMethod completenessMethod;
	private final GeometryKind geometryKind;
	private final SupportLevel supportLevel;
	private final NumericGuarantee numericGuarantee;
	private final List<String> coveredComponentKeys;
	private final List<IntersectionCandidate2D> candidates;
	private final List<IntersectionOverlapEvidence2D> overlapEvidence;
	private final List<IntersectionDiagnostic2D> diagnostics;

	/** Creates a capability result with orthogonal geometry and coverage axes. */
	public IntersectionCandidateSet2D(Completeness completeness,
			CompletenessMethod completenessMethod, GeometryKind geometryKind,
			SupportLevel supportLevel, NumericGuarantee numericGuarantee,
			List<String> coveredComponentKeys,
			List<IntersectionCandidate2D> candidates,
			List<IntersectionOverlapEvidence2D> overlapEvidence,
			List<IntersectionDiagnostic2D> diagnostics) {
		this.completeness = java.util.Objects.requireNonNull(completeness);
		this.completenessMethod =
				java.util.Objects.requireNonNull(completenessMethod);
		this.geometryKind = java.util.Objects.requireNonNull(geometryKind);
		this.supportLevel = java.util.Objects.requireNonNull(supportLevel);
		this.numericGuarantee =
				java.util.Objects.requireNonNull(numericGuarantee);
		this.coveredComponentKeys = immutableStrings(coveredComponentKeys);
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

	public List<String> getCoveredComponentKeys() {
		return coveredComponentKeys;
	}

	public List<IntersectionCandidate2D> getCandidates() {
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
					"Only complete coverage may claim an empty set");
		}
		if (geometryKind == GeometryKind.FINITE && candidates.isEmpty()) {
			throw new IllegalArgumentException(
					"Finite candidate set requires at least one candidate");
		}
		boolean overlapKind = geometryKind == GeometryKind.OVERLAP
				|| geometryKind == GeometryKind.INFINITELY_MANY
				|| geometryKind == GeometryKind.UNSUPPORTED_OVERLAP;
		if (geometryKind != GeometryKind.FINITE && !overlapKind
				&& !candidates.isEmpty()) {
			throw new IllegalArgumentException(
					"Empty or unresolved geometry cannot carry root candidates");
		}
		if (overlapKind != !overlapEvidence.isEmpty()) {
			throw new IllegalArgumentException(
					"Typed overlap geometry and evidence must agree");
		}
	}

	private static List<String> immutableStrings(List<String> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<String> copy = new ArrayList<>();
		for (String value : input) {
			if (value == null || value.trim().isEmpty() || copy.contains(value)) {
				throw new IllegalArgumentException(
						"Covered component keys must be unique and nonblank");
			}
			copy.add(value);
		}
		return Collections.unmodifiableList(copy);
	}

	private static List<IntersectionCandidate2D> immutableCandidates(
			List<IntersectionCandidate2D> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<IntersectionCandidate2D> copy = new ArrayList<>();
		for (IntersectionCandidate2D candidate : input) {
			copy.add(java.util.Objects.requireNonNull(candidate));
		}
		return Collections.unmodifiableList(copy);
	}

	private static List<IntersectionOverlapEvidence2D> immutableOverlap(
			List<IntersectionOverlapEvidence2D> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<IntersectionOverlapEvidence2D> copy = new ArrayList<>();
		for (IntersectionOverlapEvidence2D evidence : input) {
			copy.add(java.util.Objects.requireNonNull(evidence));
		}
		return Collections.unmodifiableList(copy);
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
