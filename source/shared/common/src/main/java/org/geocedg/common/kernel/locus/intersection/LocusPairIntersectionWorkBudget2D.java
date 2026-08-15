/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/** Versioned deterministic ceilings for one query-local pair computation. */
public final class LocusPairIntersectionWorkBudget2D {
	public static final String BUDGET_VERSION = "g8c2-pair-initial/v1";
	public static final int DEFAULT_MAXIMUM_BRANCH_PAIRS = 256;
	public static final int DEFAULT_MAXIMUM_COMPONENT_PAIRS = 1_024;
	public static final int DEFAULT_MAXIMUM_PARAMETER_BOXES = 32_768;
	public static final int DEFAULT_MAXIMUM_BOX_DEPTH = 16;
	public static final int DEFAULT_MAXIMUM_CANDIDATE_BOXES = 4_096;
	public static final int DEFAULT_MAXIMUM_PAIR_REFINEMENTS = 1_024;
	public static final int DEFAULT_MAXIMUM_PAIR_REFINEMENT_ITERATIONS = 80;
	public static final int DEFAULT_MAXIMUM_JACOBIAN_EVALUATIONS = 16_384;
	public static final int DEFAULT_MAXIMUM_OVERLAP_CHECKS = 4_096;
	public static final int DEFAULT_MAXIMUM_PAIR_CONTINUATION_COMPARISONS = 4_096;
	public static final int DEFAULT_MAXIMUM_PUBLISHED_SOLUTIONS = 256;
	public static final int DEFAULT_MAXIMUM_RETAINED_ENTRIES = 0;

	private final int maximumBranchPairs;
	private final int maximumComponentPairs;
	private final int maximumParameterBoxes;
	private final int maximumBoxDepth;
	private final int maximumCandidateBoxes;
	private final int maximumPairRefinements;
	private final int maximumPairRefinementIterations;
	private final int maximumJacobianEvaluations;
	private final int maximumOverlapChecks;
	private final int maximumPairContinuationComparisons;
	private final int maximumPublishedSolutions;
	private final int maximumRetainedEntries;

	/** Creates a fully bounded pair policy. */
	public LocusPairIntersectionWorkBudget2D(int maximumBranchPairs,
			int maximumComponentPairs, int maximumParameterBoxes,
			int maximumBoxDepth, int maximumCandidateBoxes,
			int maximumPairRefinements,
			int maximumPairRefinementIterations,
			int maximumJacobianEvaluations, int maximumOverlapChecks,
			int maximumPairContinuationComparisons,
			int maximumPublishedSolutions, int maximumRetainedEntries) {
		if (maximumBranchPairs < 1 || maximumComponentPairs < 1
				|| maximumParameterBoxes < 1 || maximumBoxDepth < 1
				|| maximumCandidateBoxes < 1 || maximumPairRefinements < 1
				|| maximumPairRefinementIterations < 1
				|| maximumJacobianEvaluations < 1 || maximumOverlapChecks < 1
				|| maximumPairContinuationComparisons < 1
				|| maximumPublishedSolutions < 1 || maximumRetainedEntries < 0) {
			throw new IllegalArgumentException("Pair work must be bounded");
		}
		this.maximumBranchPairs = maximumBranchPairs;
		this.maximumComponentPairs = maximumComponentPairs;
		this.maximumParameterBoxes = maximumParameterBoxes;
		this.maximumBoxDepth = maximumBoxDepth;
		this.maximumCandidateBoxes = maximumCandidateBoxes;
		this.maximumPairRefinements = maximumPairRefinements;
		this.maximumPairRefinementIterations = maximumPairRefinementIterations;
		this.maximumJacobianEvaluations = maximumJacobianEvaluations;
		this.maximumOverlapChecks = maximumOverlapChecks;
		this.maximumPairContinuationComparisons =
				maximumPairContinuationComparisons;
		this.maximumPublishedSolutions = maximumPublishedSolutions;
		this.maximumRetainedEntries = maximumRetainedEntries;
	}

	/** @return initial versioned G8C2 candidate ceilings */
	public static LocusPairIntersectionWorkBudget2D initial() {
		return new LocusPairIntersectionWorkBudget2D(
				DEFAULT_MAXIMUM_BRANCH_PAIRS,
				DEFAULT_MAXIMUM_COMPONENT_PAIRS,
				DEFAULT_MAXIMUM_PARAMETER_BOXES, DEFAULT_MAXIMUM_BOX_DEPTH,
				DEFAULT_MAXIMUM_CANDIDATE_BOXES,
				DEFAULT_MAXIMUM_PAIR_REFINEMENTS,
				DEFAULT_MAXIMUM_PAIR_REFINEMENT_ITERATIONS,
				DEFAULT_MAXIMUM_JACOBIAN_EVALUATIONS,
				DEFAULT_MAXIMUM_OVERLAP_CHECKS,
				DEFAULT_MAXIMUM_PAIR_CONTINUATION_COMPARISONS,
				DEFAULT_MAXIMUM_PUBLISHED_SOLUTIONS,
				DEFAULT_MAXIMUM_RETAINED_ENTRIES);
	}

	public int getMaximumBranchPairs() {
		return maximumBranchPairs;
	}

	public int getMaximumComponentPairs() {
		return maximumComponentPairs;
	}

	public int getMaximumParameterBoxes() {
		return maximumParameterBoxes;
	}

	public int getMaximumBoxDepth() {
		return maximumBoxDepth;
	}

	public int getMaximumCandidateBoxes() {
		return maximumCandidateBoxes;
	}

	public int getMaximumPairRefinements() {
		return maximumPairRefinements;
	}

	public int getMaximumPairRefinementIterations() {
		return maximumPairRefinementIterations;
	}

	public int getMaximumJacobianEvaluations() {
		return maximumJacobianEvaluations;
	}

	public int getMaximumOverlapChecks() {
		return maximumOverlapChecks;
	}

	public int getMaximumPairContinuationComparisons() {
		return maximumPairContinuationComparisons;
	}

	public int getMaximumPublishedSolutions() {
		return maximumPublishedSolutions;
	}

	public int getMaximumRetainedEntries() {
		return maximumRetainedEntries;
	}
}
