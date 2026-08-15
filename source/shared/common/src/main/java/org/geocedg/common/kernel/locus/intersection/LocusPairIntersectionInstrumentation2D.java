/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/** Query-local pair counters layered on the existing intersection counters. */
public final class LocusPairIntersectionInstrumentation2D {
	private final LocusIntersectionInstrumentation2D common;
	private final LocusPairIntersectionWorkBudget2D budget;
	private long branchPairs;
	private long componentPairs;
	private long parameterBoxesVisited;
	private long parameterBoxesRejected;
	private long pairCandidateBoxes;
	private long pairRefinementCalls;
	private long pairRefinementIterations;
	private long jacobianEvaluations;
	private long overlapChecks;
	private long pairContinuationComparisons;

	/** Creates one bounded pair counter set. */
	public LocusPairIntersectionInstrumentation2D(
			LocusPairIntersectionPolicy2D policy) {
		java.util.Objects.requireNonNull(policy);
		this.common = new LocusIntersectionInstrumentation2D(
				policy.getCommonWorkBudget());
		this.budget = policy.getPairWorkBudget();
	}

	/** Records one semantic evaluation on either source. */
	public void recordSemanticEvaluation() {
		common.recordSemanticEvaluation();
	}

	/** Records one semantic derivative evaluation. */
	public void recordDerivativeEvaluation() {
		common.recordDerivativeEvaluation();
	}

	/** Records independent two-sided residual verification. */
	public void recordResidualVerification() {
		common.recordResidualVerification();
	}

	/** Records one semantic parameter-pair deduplication comparison. */
	public void recordDeduplicationComparison() {
		common.recordDeduplicationComparison();
	}

	/** Records one independently verified finite pair solution. */
	public void recordVerifiedSolution() {
		common.recordVerifiedSolution();
		if (common.snapshot().getVerifiedSolutions()
				> budget.getMaximumPublishedSolutions()) {
			throw new LocusIntersectionWorkLimitException(
					"pair published solutions");
		}
	}

	/** Records one rejected candidate. */
	public void recordRejectedCandidate() {
		common.recordRejectedCandidate();
	}

	/** Records one candidate that could not be established. */
	public void recordUnresolvedCandidate() {
		common.recordUnresolvedCandidate();
	}

	/** Records one atomic public snapshot. */
	public void recordPublishedSnapshot() {
		common.recordPublishedSnapshot();
	}

	/** Records one private computation failure. */
	public void recordFailedPrivateComputation() {
		common.recordFailedPrivateComputation();
	}

	/** Records one declared branch product. */
	public void recordBranchPair() {
		branchPairs = increment(branchPairs, budget.getMaximumBranchPairs(),
				"branch pairs");
	}

	/** Records one declared valid-component product. */
	public void recordComponentPair() {
		componentPairs = increment(componentPairs,
				budget.getMaximumComponentPairs(), "component pairs");
	}

	/** Records one visited pair-parameter box and validates its depth. */
	public void recordParameterBox(int depth) {
		parameterBoxesVisited = increment(parameterBoxesVisited,
				budget.getMaximumParameterBoxes(), "parameter boxes");
		if (depth > budget.getMaximumBoxDepth()) {
			throw new LocusIntersectionWorkLimitException("pair box depth");
		}
	}

	/** Records one semantically rejected pair-parameter box. */
	public void recordRejectedBox() {
		parameterBoxesRejected++;
	}

	/** Records one retained broad-phase candidate box. */
	public void recordCandidateBox() {
		pairCandidateBoxes = increment(pairCandidateBoxes,
				budget.getMaximumCandidateBoxes(), "pair candidate boxes");
	}

	/** Records one two-parameter refinement invocation. */
	public void recordPairRefinementStarted() {
		pairRefinementCalls = increment(pairRefinementCalls,
				budget.getMaximumPairRefinements(), "pair refinements");
	}

	/** Records and bounds one iteration inside a pair refinement. */
	public void recordPairRefinementIteration(long iterationForCandidate) {
		if (iterationForCandidate
				> budget.getMaximumPairRefinementIterations()) {
			throw new LocusIntersectionWorkLimitException(
					"pair refinement iterations per candidate");
		}
		pairRefinementIterations++;
	}

	/** Records one normalized Jacobian/tangent evaluation. */
	public void recordJacobianEvaluation() {
		jacobianEvaluations = increment(jacobianEvaluations,
				budget.getMaximumJacobianEvaluations(), "Jacobian evaluations");
	}

	/** Records one overlap-classification operation. */
	public void recordOverlapCheck() {
		overlapChecks = increment(overlapChecks,
				budget.getMaximumOverlapChecks(), "overlap checks");
	}

	/** Records bounded semantic continuation comparisons. */
	public void recordPairContinuationComparisons(long count) {
		if (count < 0 || pairContinuationComparisons + count
				> budget.getMaximumPairContinuationComparisons()) {
			throw new LocusIntersectionWorkLimitException(
					"pair continuation comparisons");
		}
		pairContinuationComparisons += count;
	}

	/** @return immutable snapshot with retained pair state fixed at zero */
	public LocusIntersectionInstrumentationSnapshot2D snapshot() {
		return common.snapshot().withPairCounters(branchPairs, componentPairs,
				parameterBoxesVisited, parameterBoxesRejected,
				pairCandidateBoxes, pairRefinementCalls,
				pairRefinementIterations, jacobianEvaluations, overlapChecks,
				pairContinuationComparisons, 0);
	}

	private static long increment(long current, long maximum, String name) {
		if (current >= maximum) {
			throw new LocusIntersectionWorkLimitException(name);
		}
		return current + 1;
	}
}
