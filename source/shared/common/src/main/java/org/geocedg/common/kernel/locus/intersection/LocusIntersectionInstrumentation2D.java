/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/** Query-local functional counters with deterministic budget enforcement. */
public final class LocusIntersectionInstrumentation2D {
	private final LocusIntersectionWorkBudget2D budget;
	private long semanticEvaluations;
	private long derivativeEvaluations;
	private long targetEvaluations;
	private long targetDerivativeEvaluations;
	private long targetDomainEvaluations;
	private long invalidTargetEvaluations;
	private long candidateIntervals;
	private long isolationSubdivisions;
	private long maximumIsolationDepth;
	private long refinementCalls;
	private long refinementIterations;
	private long residualVerifications;
	private long membershipChecks;
	private long deduplicationComparisons;
	private long continuationComparisons;
	private long verifiedSolutions;
	private long rejectedCandidates;
	private long unresolvedCandidates;
	private long publishedSnapshots;
	private long failedPrivateComputations;
	private long retainedTopologyEpochs;

	/** Creates counters for one query and one immutable budget. */
	public LocusIntersectionInstrumentation2D(
			LocusIntersectionWorkBudget2D budget) {
		this.budget = java.util.Objects.requireNonNull(budget);
	}

	/** Records one semantic evaluator call. */
	public void recordSemanticEvaluation() {
		semanticEvaluations = increment(semanticEvaluations,
				budget.getMaximumSemanticEvaluations(), "semantic evaluations");
	}

	/** Records one semantic derivative evaluation. */
	public void recordDerivativeEvaluation() {
		derivativeEvaluations = increment(derivativeEvaluations,
				budget.getMaximumDerivativeEvaluations(),
				"semantic derivative evaluations");
	}

	/** Records one target residual evaluation. */
	public void recordTargetEvaluation() {
		targetEvaluations = increment(targetEvaluations,
				budget.getMaximumTargetEvaluations(), "target evaluations");
	}

	/** Records one target normal/derivative evaluation. */
	public void recordTargetDerivativeEvaluation() {
		targetDerivativeEvaluations = increment(targetDerivativeEvaluations,
				budget.getMaximumDerivativeEvaluations(),
				"target derivative evaluations");
	}

	/** Records one explicit target-domain or membership evaluation. */
	public void recordTargetDomainEvaluation() {
		targetDomainEvaluations = increment(targetDomainEvaluations,
				budget.getMaximumTargetEvaluations(),
				"target domain evaluations");
	}

	/** Records one typed invalid/undefined target-local evaluation. */
	public void recordInvalidTargetEvaluation() {
		invalidTargetEvaluations = increment(invalidTargetEvaluations,
				budget.getMaximumTargetEvaluations(),
				"invalid target evaluations");
	}

	/** Records one retained candidate interval. */
	public void recordCandidateInterval() {
		candidateIntervals = increment(candidateIntervals,
				budget.getMaximumCandidateIntervals(), "candidate intervals");
		if (candidateIntervals > budget.getMaximumCandidates()) {
			throw new LocusIntersectionWorkLimitException("candidate count");
		}
	}

	/** Records one isolation subdivision and its depth. */
	public void recordIsolationSubdivision(int depth) {
		isolationSubdivisions = increment(isolationSubdivisions,
				budget.getMaximumIsolationSubdivisions(),
				"isolation subdivisions");
		if (depth > budget.getMaximumIsolationDepth()) {
			throw new LocusIntersectionWorkLimitException("isolation depth");
		}
		maximumIsolationDepth = Math.max(maximumIsolationDepth, depth);
	}

	/** Records one refinement invocation. */
	public void recordRefinementStarted() {
		refinementCalls++;
	}

	/** Records one bounded refinement iteration. */
	public void recordRefinementIteration(long iterationForCandidate) {
		if (iterationForCandidate
				> budget.getMaximumRefinementIterations()) {
			throw new LocusIntersectionWorkLimitException(
					"refinement iterations per candidate");
		}
		refinementIterations++;
	}

	/** Records one independent residual verification. */
	public void recordResidualVerification() {
		residualVerifications = increment(residualVerifications,
				budget.getMaximumResidualVerifications(),
				"residual verifications");
	}

	/** Records one limited-target membership check. */
	public void recordMembershipCheck() {
		membershipChecks++;
	}

	/** Records one semantic-parameter deduplication comparison. */
	public void recordDeduplicationComparison() {
		deduplicationComparisons++;
	}

	/** Records one semantic continuation comparison. */
	public void recordContinuationComparison() {
		continuationComparisons = increment(continuationComparisons,
				budget.getMaximumContinuationComparisons(),
				"continuation comparisons");
	}

	/** Records one independently verified finite solution. */
	public void recordVerifiedSolution() {
		verifiedSolutions = increment(verifiedSolutions,
				budget.getMaximumPublishedSolutions(), "published solutions");
	}

	/** Records one rejected candidate. */
	public void recordRejectedCandidate() {
		rejectedCandidates++;
	}

	/** Records one candidate whose validity remains unresolved. */
	public void recordUnresolvedCandidate() {
		unresolvedCandidates++;
	}

	/** Records one atomic rich-result publication. */
	public void recordPublishedSnapshot() {
		publishedSnapshots++;
	}

	/** Records one failed private computation. */
	public void recordFailedPrivateComputation() {
		failedPrivateComputations++;
	}

	/** Records the bounded retained topology-epoch count. */
	public void setRetainedTopologyEpochs(long retainedTopologyEpochs) {
		if (retainedTopologyEpochs < 0 || retainedTopologyEpochs
				> budget.getMaximumRetainedTopologyEpochs()) {
			throw new LocusIntersectionWorkLimitException(
					"retained topology epochs");
		}
		this.retainedTopologyEpochs = retainedTopologyEpochs;
	}

	/** @return immutable counters; forbidden authority and index reads are zero */
	public LocusIntersectionInstrumentationSnapshot2D snapshot() {
		return new LocusIntersectionInstrumentationSnapshot2D(
				semanticEvaluations, derivativeEvaluations, targetEvaluations,
				targetDerivativeEvaluations, targetDomainEvaluations,
				invalidTargetEvaluations,
				candidateIntervals, isolationSubdivisions,
				maximumIsolationDepth, refinementCalls, refinementIterations,
				residualVerifications, membershipChecks,
				deduplicationComparisons, continuationComparisons,
				verifiedSolutions, rejectedCandidates, unresolvedCandidates,
				publishedSnapshots, failedPrivateComputations, 0,
				retainedTopologyEpochs, 0, 0, 0, 0, 0, 0, 0);
	}

	private static long increment(long current, long maximum, String name) {
		if (current >= maximum) {
			throw new LocusIntersectionWorkLimitException(name);
		}
		return current + 1;
	}
}
