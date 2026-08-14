/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Objects;

/** Immutable deterministic work ceilings for one query-local computation. */
public final class LocusIntersectionWorkBudget2D {
	public static final long DEFAULT_MAXIMUM_SEMANTIC_EVALUATIONS = 32_768;
	public static final long DEFAULT_MAXIMUM_DERIVATIVE_EVALUATIONS = 16_384;
	public static final long DEFAULT_MAXIMUM_TARGET_EVALUATIONS = 32_768;
	public static final int DEFAULT_MAXIMUM_CANDIDATE_INTERVALS = 8_192;
	public static final int DEFAULT_MAXIMUM_ISOLATION_SUBDIVISIONS = 8_192;
	public static final int DEFAULT_MAXIMUM_ISOLATION_DEPTH = 40;
	public static final int DEFAULT_MAXIMUM_REFINEMENT_ITERATIONS = 80;
	public static final int DEFAULT_MAXIMUM_RESIDUAL_VERIFICATIONS = 1_024;
	public static final int DEFAULT_MAXIMUM_CANDIDATES = 512;
	public static final int DEFAULT_MAXIMUM_CONTINUATION_COMPARISONS = 4_096;
	public static final int DEFAULT_MAXIMUM_PUBLISHED_SOLUTIONS = 256;
	public static final int DEFAULT_MAXIMUM_RETAINED_INDEX_ENTRIES = 0;
	public static final int DEFAULT_MAXIMUM_RETAINED_TOPOLOGY_EPOCHS = 2;

	private final long maximumSemanticEvaluations;
	private final long maximumDerivativeEvaluations;
	private final long maximumTargetEvaluations;
	private final int maximumCandidateIntervals;
	private final int maximumIsolationSubdivisions;
	private final int maximumIsolationDepth;
	private final int maximumRefinementIterations;
	private final int maximumResidualVerifications;
	private final int maximumCandidates;
	private final int maximumContinuationComparisons;
	private final int maximumPublishedSolutions;
	private final int maximumRetainedIndexEntries;
	private final int maximumRetainedTopologyEpochs;

	/** Creates a fully bounded work policy. */
	public LocusIntersectionWorkBudget2D(long maximumSemanticEvaluations,
			long maximumDerivativeEvaluations, long maximumTargetEvaluations,
			int maximumCandidateIntervals, int maximumIsolationSubdivisions,
			int maximumIsolationDepth, int maximumRefinementIterations,
			int maximumResidualVerifications, int maximumCandidates,
			int maximumContinuationComparisons, int maximumPublishedSolutions,
			int maximumRetainedIndexEntries,
			int maximumRetainedTopologyEpochs) {
		if (maximumSemanticEvaluations < 1 || maximumDerivativeEvaluations < 1
				|| maximumTargetEvaluations < 1 || maximumCandidateIntervals < 1
				|| maximumIsolationSubdivisions < 1
				|| maximumIsolationDepth < 1 || maximumRefinementIterations < 1
				|| maximumResidualVerifications < 1 || maximumCandidates < 1
				|| maximumContinuationComparisons < 1
				|| maximumPublishedSolutions < 1
				|| maximumRetainedIndexEntries < 0
				|| maximumRetainedTopologyEpochs < 1) {
			throw new IllegalArgumentException("Intersection work must be bounded");
		}
		this.maximumSemanticEvaluations = maximumSemanticEvaluations;
		this.maximumDerivativeEvaluations = maximumDerivativeEvaluations;
		this.maximumTargetEvaluations = maximumTargetEvaluations;
		this.maximumCandidateIntervals = maximumCandidateIntervals;
		this.maximumIsolationSubdivisions = maximumIsolationSubdivisions;
		this.maximumIsolationDepth = maximumIsolationDepth;
		this.maximumRefinementIterations = maximumRefinementIterations;
		this.maximumResidualVerifications = maximumResidualVerifications;
		this.maximumCandidates = maximumCandidates;
		this.maximumContinuationComparisons = maximumContinuationComparisons;
		this.maximumPublishedSolutions = maximumPublishedSolutions;
		this.maximumRetainedIndexEntries = maximumRetainedIndexEntries;
		this.maximumRetainedTopologyEpochs = maximumRetainedTopologyEpochs;
	}

	/** @return author-approved initial G8B deterministic ceilings */
	public static LocusIntersectionWorkBudget2D initial() {
		return new LocusIntersectionWorkBudget2D(
				DEFAULT_MAXIMUM_SEMANTIC_EVALUATIONS,
				DEFAULT_MAXIMUM_DERIVATIVE_EVALUATIONS,
				DEFAULT_MAXIMUM_TARGET_EVALUATIONS,
				DEFAULT_MAXIMUM_CANDIDATE_INTERVALS,
				DEFAULT_MAXIMUM_ISOLATION_SUBDIVISIONS,
				DEFAULT_MAXIMUM_ISOLATION_DEPTH,
				DEFAULT_MAXIMUM_REFINEMENT_ITERATIONS,
				DEFAULT_MAXIMUM_RESIDUAL_VERIFICATIONS,
				DEFAULT_MAXIMUM_CANDIDATES,
				DEFAULT_MAXIMUM_CONTINUATION_COMPARISONS,
				DEFAULT_MAXIMUM_PUBLISHED_SOLUTIONS,
				DEFAULT_MAXIMUM_RETAINED_INDEX_ENTRIES,
				DEFAULT_MAXIMUM_RETAINED_TOPOLOGY_EPOCHS);
	}

	public long getMaximumSemanticEvaluations() {
		return maximumSemanticEvaluations;
	}

	public long getMaximumDerivativeEvaluations() {
		return maximumDerivativeEvaluations;
	}

	public long getMaximumTargetEvaluations() {
		return maximumTargetEvaluations;
	}

	public int getMaximumCandidateIntervals() {
		return maximumCandidateIntervals;
	}

	public int getMaximumIsolationSubdivisions() {
		return maximumIsolationSubdivisions;
	}

	public int getMaximumIsolationDepth() {
		return maximumIsolationDepth;
	}

	public int getMaximumRefinementIterations() {
		return maximumRefinementIterations;
	}

	public int getMaximumResidualVerifications() {
		return maximumResidualVerifications;
	}

	public int getMaximumCandidates() {
		return maximumCandidates;
	}

	public int getMaximumContinuationComparisons() {
		return maximumContinuationComparisons;
	}

	public int getMaximumPublishedSolutions() {
		return maximumPublishedSolutions;
	}

	public int getMaximumRetainedIndexEntries() {
		return maximumRetainedIndexEntries;
	}

	public int getMaximumRetainedTopologyEpochs() {
		return maximumRetainedTopologyEpochs;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusIntersectionWorkBudget2D)) {
			return false;
		}
		LocusIntersectionWorkBudget2D budget =
				(LocusIntersectionWorkBudget2D) other;
		return maximumSemanticEvaluations == budget.maximumSemanticEvaluations
				&& maximumDerivativeEvaluations
						== budget.maximumDerivativeEvaluations
				&& maximumTargetEvaluations == budget.maximumTargetEvaluations
				&& maximumCandidateIntervals == budget.maximumCandidateIntervals
				&& maximumIsolationSubdivisions
						== budget.maximumIsolationSubdivisions
				&& maximumIsolationDepth == budget.maximumIsolationDepth
				&& maximumRefinementIterations
						== budget.maximumRefinementIterations
				&& maximumResidualVerifications
						== budget.maximumResidualVerifications
				&& maximumCandidates == budget.maximumCandidates
				&& maximumContinuationComparisons
						== budget.maximumContinuationComparisons
				&& maximumPublishedSolutions == budget.maximumPublishedSolutions
				&& maximumRetainedIndexEntries
						== budget.maximumRetainedIndexEntries
				&& maximumRetainedTopologyEpochs
						== budget.maximumRetainedTopologyEpochs;
	}

	@Override
	public int hashCode() {
		return Objects.hash(maximumSemanticEvaluations,
				maximumDerivativeEvaluations, maximumTargetEvaluations,
				maximumCandidateIntervals, maximumIsolationSubdivisions,
				maximumIsolationDepth, maximumRefinementIterations,
				maximumResidualVerifications, maximumCandidates,
				maximumContinuationComparisons, maximumPublishedSolutions,
				maximumRetainedIndexEntries, maximumRetainedTopologyEpochs);
	}
}
