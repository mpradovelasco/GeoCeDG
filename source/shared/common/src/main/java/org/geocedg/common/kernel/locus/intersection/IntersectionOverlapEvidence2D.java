/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.OverlapRelationKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.OverlapStatus;

/** Typed component-level evidence for overlap or infinitely many solutions. */
public final class IntersectionOverlapEvidence2D {
	private final String branchKey;
	private final String componentKey;
	private final String proofMethod;
	private final String diagnostic;
	private final String secondBranchKey;
	private final String secondComponentKey;
	private final OverlapStatus status;
	private final OverlapRelationKind relationKind;
	private final String parameterMapEvidence;
	private final NumericGuarantee numericGuarantee;

	/** Creates immutable overlap evidence without point sampling. */
	public IntersectionOverlapEvidence2D(String branchKey, String componentKey,
			String proofMethod, String diagnostic) {
		this.branchKey = requireText(branchKey, "Branch key");
		this.componentKey = requireText(componentKey, "Component key");
		this.proofMethod = requireText(proofMethod, "Overlap proof method");
		this.diagnostic = requireText(diagnostic, "Overlap diagnostic");
		this.secondBranchKey = null;
		this.secondComponentKey = null;
		this.status = OverlapStatus.OVERLAP_ESTABLISHED;
		this.relationKind = OverlapRelationKind.UNSPECIFIED;
		this.parameterMapEvidence = "single-target component proof";
		this.numericGuarantee = NumericGuarantee.CERTIFIED_ERROR_BOUND;
	}

	/** Creates typed two-locus component-pair overlap evidence. */
	public IntersectionOverlapEvidence2D(String firstBranchKey,
			String firstComponentKey, String secondBranchKey,
			String secondComponentKey, OverlapStatus status,
			OverlapRelationKind relationKind, String proofMethod,
			String parameterMapEvidence, NumericGuarantee numericGuarantee,
			String diagnostic) {
		this.branchKey = requireText(firstBranchKey, "First branch key");
		this.componentKey = requireText(firstComponentKey,
				"First component key");
		this.secondBranchKey = requireText(secondBranchKey,
				"Second branch key");
		this.secondComponentKey = requireText(secondComponentKey,
				"Second component key");
		this.status = java.util.Objects.requireNonNull(status);
		this.relationKind = java.util.Objects.requireNonNull(relationKind);
		this.proofMethod = requireText(proofMethod, "Overlap proof method");
		this.parameterMapEvidence = requireText(parameterMapEvidence,
				"Parameter-map evidence");
		this.numericGuarantee = java.util.Objects.requireNonNull(
				numericGuarantee);
		this.diagnostic = requireText(diagnostic, "Overlap diagnostic");
	}

	public String getBranchKey() {
		return branchKey;
	}

	public String getComponentKey() {
		return componentKey;
	}

	public String getProofMethod() {
		return proofMethod;
	}

	public String getDiagnostic() {
		return diagnostic;
	}

	/** @return whether this is two-locus component-pair evidence */
	public boolean isLocusPairEvidence() {
		return secondBranchKey != null;
	}

	public String getSecondBranchKey() {
		return secondBranchKey;
	}

	public String getSecondComponentKey() {
		return secondComponentKey;
	}

	public OverlapStatus getStatus() {
		return status;
	}

	public OverlapRelationKind getRelationKind() {
		return relationKind;
	}

	public String getParameterMapEvidence() {
		return parameterMapEvidence;
	}

	public NumericGuarantee getNumericGuarantee() {
		return numericGuarantee;
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
