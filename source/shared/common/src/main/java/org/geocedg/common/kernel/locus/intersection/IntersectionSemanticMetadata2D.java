/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/** Closed semantic axes for internal Locus V2 intersections. */
public final class IntersectionSemanticMetadata2D {

	/** Overall computation outcome. */
	public enum ComputationStatus {
		SUCCESS, INVALID_INPUT, UNSUPPORTED, NUMERICAL_FAILURE,
		WORK_LIMIT_REACHED
	}

	/** Exhaustiveness of the returned solution set. */
	public enum Completeness {
		COMPLETE, INCOMPLETE, NOT_ESTABLISHED
	}

	/** Geometric kind of the intersection set. */
	public enum GeometryKind {
		EMPTY, FINITE, OVERLAP, INFINITELY_MANY, UNSUPPORTED_OVERLAP,
		MIXED_FINITE_OVERLAP, UNRESOLVED
	}

	/** Whether a result is bound to the currently captured sources. */
	public enum Currentness {
		CURRENT, NON_CURRENT
	}

	/** Strongest support level established for the result. */
	public enum SupportLevel {
		EXACT_CAPABILITY, CERTIFIED, VERIFIED_UNCERTIFIED, UNSUPPORTED
	}

	/** Method by which set completeness was assessed. */
	public enum CompletenessMethod {
		ANALYTIC_ROOT_ENUMERATION, CERTIFIED_DOMAIN_ISOLATION,
		CERTIFIED_DOMAIN_EXCLUSION, CONSERVATIVE_COVERAGE_PROOF,
		INCOMPLETE_CANDIDATE_COVERAGE, NOT_ESTABLISHED
	}

	/** Closed target families authorized through G8C1. */
	public enum TargetFamily {
		LINE, SEGMENT, RAY, CIRCLE, ELLIPSE, PARABOLA, HYPERBOLA,
		BOUNDED_FUNCTION_GRAPH, REGULAR_POLYNOMIAL_IMPLICIT, LOCUS_V2,
		UNSUPPORTED
	}

	/** Physical meaning of a target residual. */
	public enum ResidualQuantityKind {
		MODEL_COORDINATE_DISTANCE, FIRST_ORDER_NORMAL_LENGTH,
		VERTICAL_MODEL_LENGTH, TARGET_FAMILY_SPECIFIC
	}

	/** Result of binding one target GeoElement to a closed adapter. */
	public enum TargetSupportStatus {
		SUPPORTED, TARGET_UNDEFINED, UNSUPPORTED_TARGET_SUBTYPE,
		DOMAIN_NOT_EXPLICIT, NONPOLYNOMIAL_IMPLICIT,
		RESIDUAL_NORMALIZATION_UNAVAILABLE
	}

	/** One local target evaluation without NaN or magic-state semantics. */
	public enum TargetEvaluationStatus {
		ESTABLISHED, OUTSIDE_EXPLICIT_DOMAIN, TARGET_UNDEFINED,
		RESIDUAL_NORMALIZATION_UNAVAILABLE,
		UNSUPPORTED_LOCAL_GEOMETRY
	}

	/** Limited-target membership evidence. */
	public enum MembershipStatus {
		MEMBER, NOT_MEMBER, NOT_ESTABLISHED
	}

	/** Established first-order contact classification. */
	public enum ContactClass {
		TRANSVERSE_ESTABLISHED, TANGENT_ESTABLISHED, CONTACT_UNDETERMINED
	}

	/** Whether an integer multiplicity has been established. */
	public enum MultiplicityStatus {
		ESTABLISHED, NOT_ESTABLISHED
	}

	/** Semantic-domain location of one solution preimage. */
	public enum DomainLocation {
		INTERIOR, INCLUDED_ENDPOINT, PERIODIC_SEAM, ISOLATED_COMPONENT
	}

	/** Current continuation status of one opaque solution token. */
	public enum IdentityStatus {
		CONTINUATION_ESTABLISHED, DETERMINISTIC_SELECTION_ESTABLISHED,
		NEW_TOPOLOGICAL_SOLUTION,
		AMBIGUOUS_CONTINUATION, IDENTITY_DISCONTINUITY, NOT_ESTABLISHED
	}

	/** Explicit topology/identity event evidence. */
	public enum LineageEventKind {
		UNCHANGED, APPEARED, DISAPPEARED, MERGE_CANDIDATE,
		SPLIT_CANDIDATE, AMBIGUOUS_EVENT
	}

	/** Numerical or analytic method that localized a solution. */
	public enum SolverMethod {
		ANALYTIC_ROOT_ENUMERATION, CERTIFIED_INTERVAL,
		SAFEGUARDED_DERIVATIVE, EVALUATOR_ADAPTIVE,
		CONSERVATIVE_BROAD_PHASE, CERTIFIED_PARAMETER_RECTANGLE,
		SAFEGUARDED_DUAL_PARAMETER, EVALUATOR_PARAMETER_BOXES
	}

	/** Whether one verified root is locally isolated as a semantic preimage. */
	public enum LocalIsolationStatus {
		ESTABLISHED, NOT_ESTABLISHED
	}

	/** Coverage evidence for one local parameter-pair rectangle. */
	public enum PairCoverageStatus {
		EXHAUSTIVE_RECTANGLE, NOT_ESTABLISHED
	}

	/** Uniqueness evidence inside one local parameter-pair rectangle. */
	public enum PairUniquenessStatus {
		CERTIFIED_UNIQUE, REGULAR_JACOBIAN_UNIQUE, NOT_ESTABLISHED
	}

	/** Method establishing or declining local pair isolation. */
	public enum PairIsolationMethod {
		ANALYTIC_UNIQUE_PAIR, CERTIFIED_RECTANGLE,
		SAFEGUARDED_REGULAR_JACOBIAN, NOT_ESTABLISHED
	}

	/** Strength of one component-pair overlap contribution. */
	public enum OverlapStatus {
		OVERLAP_ESTABLISHED, OVERLAP_SUSPECTED_NOT_ESTABLISHED,
		UNSUPPORTED_OVERLAP
	}

	/** Geometric form of one typed overlap contribution. */
	public enum OverlapRelationKind {
		FULL_COMPONENT, PARTIAL_COMPONENT, REVERSE_PARAMETERIZATION,
		REPEATED_TRAVERSAL, UNSPECIFIED
	}

	/** Typed broad diagnostic categories. */
	public enum DiagnosticCode {
		INVALID_SOURCE, INVALID_TARGET, UNSUPPORTED_TARGET,
		UNSUPPORTED_TARGET_SUBTYPE, TARGET_DOMAIN_NOT_EXPLICIT,
		TARGET_UNDEFINED, RESIDUAL_NORMALIZATION_UNAVAILABLE,
		LOCAL_ISOLATION_ESTABLISHED,
		CAPABILITY_NOT_AVAILABLE, DOMAIN_EXCLUSION_ESTABLISHED,
		COVERAGE_NOT_ESTABLISHED,
		OPEN_BOUNDARY_EXCLUDED, CANDIDATE_REJECTED,
		NONFINITE_EVALUATION, RESIDUAL_REJECTED, MEMBERSHIP_REJECTED,
		WORK_LIMIT_REACHED, NUMERICAL_FAILURE, OVERLAP_ESTABLISHED,
		CONTINUATION_ESTABLISHED, DETERMINISTIC_SELECTION_ESTABLISHED,
		CONTINUATION_AMBIGUOUS,
		IDENTITY_DISCONTINUITY, REVISION_MISMATCH, INTERNAL_FAILURE,
		PAIR_DOMAIN_UNSUPPORTED, PAIR_COVERAGE_NOT_ESTABLISHED,
		PAIR_LOCAL_ISOLATION_NOT_ESTABLISHED, PAIR_RESIDUAL_REJECTED,
		PAIR_SOURCE_ORDER_CANONICALIZED, PAIR_SELECTOR_CLASS_EVIDENCE,
		PAIR_CERTIFICATION_WORK, OVERLAP_SUSPECTED,
		UNSUPPORTED_OVERLAP
	}

	private IntersectionSemanticMetadata2D() {
		// Utility holder.
	}
}
