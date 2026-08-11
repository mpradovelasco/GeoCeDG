/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/** Independent semantic axes carried by a Locus V2 snapshot or evaluation. */
public final class LocusSemanticMetadata2D {

	/** Status of the complete semantic definition. */
	public enum DefinitionStatus {
		VALID, EMPTY_DOMAIN, DRIVER_INVALID, UNSUPPORTED
	}

	/** Properties of a branch or its image. */
	public enum BranchProperty {
		FINITE, UNBOUNDED, PERIODIC, COLLAPSED_IMAGE
	}

	/** Status of one point evaluation. */
	public enum EvaluationStatus {
		VALID, OUT_OF_DOMAIN, DEPENDENCY_UNDEFINED, NON_FINITE,
		EVALUATION_FAILED, UNSUPPORTED_NONDETERMINISM
	}

	/** Optional differential regularity metadata. */
	public enum Regularity {
		REGULAR, SINGULAR, UNKNOWN
	}

	/** Typed branch lifecycle transition. */
	public enum LineageTransition {
		UNCHANGED, APPEARED, DISAPPEARED, SPLIT, MERGED
	}

	/** Fidelity to the approved construction. */
	public enum ConstructionFidelity {
		SEMANTICALLY_CONSTRUCTED, EXPLICIT_APPROXIMATION
	}

	/** Method used to evaluate the construction. */
	public enum EvaluationMethod {
		ANALYTIC_EVALUATION, DETERMINISTIC_NUMERIC_DEPENDENCY,
		CANONICAL_NUMERIC_CONTINUATION
	}

	/** Role of a representation; semantic results are not render samples. */
	public enum RepresentationRole {
		SEMANTIC_RESULT, VALIDATION_SAMPLE, RENDER_TESSELLATION,
		OPERATION_APPROXIMATION
	}

	/** Strength of the numerical guarantee. */
	public enum NumericGuarantee {
		EXACT_ARITHMETIC, CERTIFIED_ERROR_BOUND, ESTIMATED_ERROR,
		FLOATING_POINT_UNCERTIFIED
	}

	/** Reproducibility class of an evaluator. */
	public enum Determinism {
		POINTWISE_DETERMINISTIC, CANONICAL_CONTINUATION_DETERMINISTIC,
		UNSUPPORTED_NONDETERMINISM
	}

	/** Provider-owned orientation of increasing semantic parameter. */
	public enum Orientation {
		INCREASING, DECREASING
	}

	private LocusSemanticMetadata2D() {
		// Utility holder.
	}
}
