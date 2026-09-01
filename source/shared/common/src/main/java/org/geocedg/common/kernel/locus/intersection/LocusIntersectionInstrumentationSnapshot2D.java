/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/** Immutable functional-counter snapshot for one query-local computation. */
public final class LocusIntersectionInstrumentationSnapshot2D {
	private final long semanticEvaluations;
	private final long derivativeEvaluations;
	private final long targetEvaluations;
	private final long targetDerivativeEvaluations;
	private final long targetDomainEvaluations;
	private final long invalidTargetEvaluations;
	private final long candidateIntervals;
	private final long isolationSubdivisions;
	private final long maximumIsolationDepth;
	private final long refinementCalls;
	private final long refinementIterations;
	private final long residualVerifications;
	private final long membershipChecks;
	private final long deduplicationComparisons;
	private final long continuationComparisons;
	private final long verifiedSolutions;
	private final long rejectedCandidates;
	private final long unresolvedCandidates;
	private final long publishedSnapshots;
	private final long failedPrivateComputations;
	private final long retainedIndexEntries;
	private final long retainedTopologyEpochs;
	private final long wholeLocusRegenerations;
	private final long renderCacheReads;
	private final long renderVertexReads;
	private final long legacySampleReads;
	private final long viewportReads;
	private final long pixelToleranceReads;
	private final long metricIndexReads;
	private final long branchPairs;
	private final long componentPairs;
	private final long parameterBoxesVisited;
	private final long parameterBoxesRejected;
	private final long pairCandidateBoxes;
	private final long pairRefinementCalls;
	private final long pairRefinementIterations;
	private final long jacobianEvaluations;
	private final long overlapChecks;
	private final long pairContinuationComparisons;
	private final long retainedPairEntries;
	private final long polynomialSpansExamined;
	private final long polynomialSpansRejected;
	private final long polynomialRootCandidates;

	/** Creates a complete immutable snapshot. */
	public LocusIntersectionInstrumentationSnapshot2D(long semanticEvaluations,
			long derivativeEvaluations, long targetEvaluations,
			long targetDerivativeEvaluations, long targetDomainEvaluations,
			long invalidTargetEvaluations,
			long candidateIntervals, long isolationSubdivisions,
			long maximumIsolationDepth, long refinementCalls,
			long refinementIterations, long residualVerifications,
			long membershipChecks, long deduplicationComparisons,
			long continuationComparisons, long verifiedSolutions,
			long rejectedCandidates, long unresolvedCandidates,
			long publishedSnapshots, long failedPrivateComputations,
			long retainedIndexEntries, long retainedTopologyEpochs,
			long wholeLocusRegenerations, long renderCacheReads,
			long renderVertexReads, long legacySampleReads, long viewportReads,
			long pixelToleranceReads, long metricIndexReads) {
		this(semanticEvaluations, derivativeEvaluations, targetEvaluations,
				targetDerivativeEvaluations, targetDomainEvaluations,
				invalidTargetEvaluations, candidateIntervals,
				isolationSubdivisions, maximumIsolationDepth, refinementCalls,
				refinementIterations, residualVerifications, membershipChecks,
				deduplicationComparisons, continuationComparisons,
				verifiedSolutions, rejectedCandidates, unresolvedCandidates,
				publishedSnapshots, failedPrivateComputations,
				retainedIndexEntries, retainedTopologyEpochs,
				wholeLocusRegenerations, renderCacheReads, renderVertexReads,
				legacySampleReads, viewportReads, pixelToleranceReads,
				metricIndexReads, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0);
	}

	private LocusIntersectionInstrumentationSnapshot2D(
			long semanticEvaluations, long derivativeEvaluations,
			long targetEvaluations, long targetDerivativeEvaluations,
			long targetDomainEvaluations, long invalidTargetEvaluations,
			long candidateIntervals, long isolationSubdivisions,
			long maximumIsolationDepth, long refinementCalls,
			long refinementIterations, long residualVerifications,
			long membershipChecks, long deduplicationComparisons,
			long continuationComparisons, long verifiedSolutions,
			long rejectedCandidates, long unresolvedCandidates,
			long publishedSnapshots, long failedPrivateComputations,
			long retainedIndexEntries, long retainedTopologyEpochs,
			long wholeLocusRegenerations, long renderCacheReads,
			long renderVertexReads, long legacySampleReads, long viewportReads,
			long pixelToleranceReads, long metricIndexReads, long branchPairs,
			long componentPairs, long parameterBoxesVisited,
			long parameterBoxesRejected, long pairCandidateBoxes,
			long pairRefinementCalls, long pairRefinementIterations,
			long jacobianEvaluations, long overlapChecks,
			long pairContinuationComparisons, long retainedPairEntries,
			long polynomialSpansExamined, long polynomialSpansRejected,
			long polynomialRootCandidates) {
		this.semanticEvaluations = semanticEvaluations;
		this.derivativeEvaluations = derivativeEvaluations;
		this.targetEvaluations = targetEvaluations;
		this.targetDerivativeEvaluations = targetDerivativeEvaluations;
		this.targetDomainEvaluations = targetDomainEvaluations;
		this.invalidTargetEvaluations = invalidTargetEvaluations;
		this.candidateIntervals = candidateIntervals;
		this.isolationSubdivisions = isolationSubdivisions;
		this.maximumIsolationDepth = maximumIsolationDepth;
		this.refinementCalls = refinementCalls;
		this.refinementIterations = refinementIterations;
		this.residualVerifications = residualVerifications;
		this.membershipChecks = membershipChecks;
		this.deduplicationComparisons = deduplicationComparisons;
		this.continuationComparisons = continuationComparisons;
		this.verifiedSolutions = verifiedSolutions;
		this.rejectedCandidates = rejectedCandidates;
		this.unresolvedCandidates = unresolvedCandidates;
		this.publishedSnapshots = publishedSnapshots;
		this.failedPrivateComputations = failedPrivateComputations;
		this.retainedIndexEntries = retainedIndexEntries;
		this.retainedTopologyEpochs = retainedTopologyEpochs;
		this.wholeLocusRegenerations = wholeLocusRegenerations;
		this.renderCacheReads = renderCacheReads;
		this.renderVertexReads = renderVertexReads;
		this.legacySampleReads = legacySampleReads;
		this.viewportReads = viewportReads;
		this.pixelToleranceReads = pixelToleranceReads;
		this.metricIndexReads = metricIndexReads;
		this.branchPairs = branchPairs;
		this.componentPairs = componentPairs;
		this.parameterBoxesVisited = parameterBoxesVisited;
		this.parameterBoxesRejected = parameterBoxesRejected;
		this.pairCandidateBoxes = pairCandidateBoxes;
		this.pairRefinementCalls = pairRefinementCalls;
		this.pairRefinementIterations = pairRefinementIterations;
		this.jacobianEvaluations = jacobianEvaluations;
		this.overlapChecks = overlapChecks;
		this.pairContinuationComparisons = pairContinuationComparisons;
		this.retainedPairEntries = retainedPairEntries;
		this.polynomialSpansExamined = polynomialSpansExamined;
		this.polynomialSpansRejected = polynomialSpansRejected;
		this.polynomialRootCandidates = polynomialRootCandidates;
	}

	public long getSemanticEvaluations() {
		return semanticEvaluations;
	}

	public long getDerivativeEvaluations() {
		return derivativeEvaluations;
	}

	public long getTargetEvaluations() {
		return targetEvaluations;
	}

	public long getTargetDerivativeEvaluations() {
		return targetDerivativeEvaluations;
	}

	public long getTargetDomainEvaluations() {
		return targetDomainEvaluations;
	}

	public long getInvalidTargetEvaluations() {
		return invalidTargetEvaluations;
	}

	public long getCandidateIntervals() {
		return candidateIntervals;
	}

	public long getIsolationSubdivisions() {
		return isolationSubdivisions;
	}

	public long getMaximumIsolationDepth() {
		return maximumIsolationDepth;
	}

	public long getRefinementCalls() {
		return refinementCalls;
	}

	public long getRefinementIterations() {
		return refinementIterations;
	}

	public long getResidualVerifications() {
		return residualVerifications;
	}

	public long getMembershipChecks() {
		return membershipChecks;
	}

	public long getDeduplicationComparisons() {
		return deduplicationComparisons;
	}

	public long getContinuationComparisons() {
		return continuationComparisons;
	}

	public long getVerifiedSolutions() {
		return verifiedSolutions;
	}

	public long getRejectedCandidates() {
		return rejectedCandidates;
	}

	public long getUnresolvedCandidates() {
		return unresolvedCandidates;
	}

	public long getPublishedSnapshots() {
		return publishedSnapshots;
	}

	public long getFailedPrivateComputations() {
		return failedPrivateComputations;
	}

	public long getRetainedIndexEntries() {
		return retainedIndexEntries;
	}

	public long getRetainedTopologyEpochs() {
		return retainedTopologyEpochs;
	}

	public long getWholeLocusRegenerations() {
		return wholeLocusRegenerations;
	}

	public long getRenderCacheReads() {
		return renderCacheReads;
	}

	public long getRenderVertexReads() {
		return renderVertexReads;
	}

	public long getLegacySampleReads() {
		return legacySampleReads;
	}

	public long getViewportReads() {
		return viewportReads;
	}

	public long getPixelToleranceReads() {
		return pixelToleranceReads;
	}

	public long getMetricIndexReads() {
		return metricIndexReads;
	}

	public long getBranchPairs() {
		return branchPairs;
	}

	public long getComponentPairs() {
		return componentPairs;
	}

	public long getParameterBoxesVisited() {
		return parameterBoxesVisited;
	}

	public long getParameterBoxesRejected() {
		return parameterBoxesRejected;
	}

	public long getPairCandidateBoxes() {
		return pairCandidateBoxes;
	}

	public long getPairRefinementCalls() {
		return pairRefinementCalls;
	}

	public long getPairRefinementIterations() {
		return pairRefinementIterations;
	}

	public long getJacobianEvaluations() {
		return jacobianEvaluations;
	}

	public long getOverlapChecks() {
		return overlapChecks;
	}

	public long getPairContinuationComparisons() {
		return pairContinuationComparisons;
	}

	public long getRetainedPairEntries() {
		return retainedPairEntries;
	}

	/** @return explicit semantic polynomial spans examined by G9S1 */
	public long getPolynomialSpansExamined() {
		return polynomialSpansExamined;
	}

	/** @return examined semantic polynomial spans with no retained root */
	public long getPolynomialSpansRejected() {
		return polynomialSpansRejected;
	}

	/** @return raw polynomial roots before semantic-knot deduplication */
	public long getPolynomialRootCandidates() {
		return polynomialRootCandidates;
	}

	/** @return whether every forbidden-authority counter is zero */
	public boolean hasZeroForbiddenAuthorityReads() {
		return wholeLocusRegenerations == 0 && renderCacheReads == 0
				&& renderVertexReads == 0 && legacySampleReads == 0
				&& viewportReads == 0 && pixelToleranceReads == 0
				&& metricIndexReads == 0;
	}

	/** @return copy including bounded continuation work and retained epochs */
	public LocusIntersectionInstrumentationSnapshot2D withContinuation(
			long additionalComparisons, long topologyEpochs) {
		return new LocusIntersectionInstrumentationSnapshot2D(
				semanticEvaluations, derivativeEvaluations, targetEvaluations,
				targetDerivativeEvaluations, targetDomainEvaluations,
				invalidTargetEvaluations,
				candidateIntervals, isolationSubdivisions,
				maximumIsolationDepth, refinementCalls, refinementIterations,
				residualVerifications, membershipChecks,
				deduplicationComparisons,
				continuationComparisons + additionalComparisons,
				verifiedSolutions, rejectedCandidates, unresolvedCandidates,
				publishedSnapshots, failedPrivateComputations,
				retainedIndexEntries, topologyEpochs, wholeLocusRegenerations,
				renderCacheReads, renderVertexReads, legacySampleReads,
				viewportReads, pixelToleranceReads, metricIndexReads,
				branchPairs, componentPairs, parameterBoxesVisited,
				parameterBoxesRejected, pairCandidateBoxes, pairRefinementCalls,
				pairRefinementIterations, jacobianEvaluations, overlapChecks,
				pairContinuationComparisons, retainedPairEntries,
				polynomialSpansExamined, polynomialSpansRejected,
				polynomialRootCandidates);
	}

	/** @return copy including query-local G8C2 pair counters */
	public LocusIntersectionInstrumentationSnapshot2D withPairCounters(
			long newBranchPairs, long newComponentPairs,
			long newParameterBoxesVisited, long newParameterBoxesRejected,
			long newPairCandidateBoxes, long newPairRefinementCalls,
			long newPairRefinementIterations, long newJacobianEvaluations,
			long newOverlapChecks, long newPairContinuationComparisons,
			long newRetainedPairEntries) {
		return new LocusIntersectionInstrumentationSnapshot2D(
				semanticEvaluations, derivativeEvaluations, targetEvaluations,
				targetDerivativeEvaluations, targetDomainEvaluations,
				invalidTargetEvaluations, candidateIntervals,
				isolationSubdivisions, maximumIsolationDepth, refinementCalls,
				refinementIterations, residualVerifications, membershipChecks,
				deduplicationComparisons, continuationComparisons,
				verifiedSolutions, rejectedCandidates, unresolvedCandidates,
				publishedSnapshots, failedPrivateComputations,
				retainedIndexEntries, retainedTopologyEpochs,
				wholeLocusRegenerations, renderCacheReads, renderVertexReads,
				legacySampleReads, viewportReads, pixelToleranceReads,
				metricIndexReads, newBranchPairs, newComponentPairs,
				newParameterBoxesVisited, newParameterBoxesRejected,
				newPairCandidateBoxes, newPairRefinementCalls,
				newPairRefinementIterations, newJacobianEvaluations,
				newOverlapChecks, newPairContinuationComparisons,
				newRetainedPairEntries, polynomialSpansExamined,
				polynomialSpansRejected, polynomialRootCandidates);
	}

	/** @return copy including query-local G9S1 polynomial-span counters */
	LocusIntersectionInstrumentationSnapshot2D withPolynomialCounters(
			long newPolynomialSpansExamined,
			long newPolynomialSpansRejected,
			long newPolynomialRootCandidates) {
		if (newPolynomialSpansExamined < 0 || newPolynomialSpansRejected < 0
				|| newPolynomialRootCandidates < 0
				|| newPolynomialSpansRejected > newPolynomialSpansExamined) {
			throw new IllegalArgumentException(
					"Polynomial work counters must be coherent");
		}
		return new LocusIntersectionInstrumentationSnapshot2D(
				semanticEvaluations, derivativeEvaluations, targetEvaluations,
				targetDerivativeEvaluations, targetDomainEvaluations,
				invalidTargetEvaluations, candidateIntervals,
				isolationSubdivisions, maximumIsolationDepth, refinementCalls,
				refinementIterations, residualVerifications, membershipChecks,
				deduplicationComparisons, continuationComparisons,
				verifiedSolutions, rejectedCandidates, unresolvedCandidates,
				publishedSnapshots, failedPrivateComputations,
				retainedIndexEntries, retainedTopologyEpochs,
				wholeLocusRegenerations, renderCacheReads, renderVertexReads,
				legacySampleReads, viewportReads, pixelToleranceReads,
				metricIndexReads, branchPairs, componentPairs,
				parameterBoxesVisited, parameterBoxesRejected,
				pairCandidateBoxes, pairRefinementCalls,
				pairRefinementIterations, jacobianEvaluations, overlapChecks,
				pairContinuationComparisons, retainedPairEntries,
				newPolynomialSpansExamined, newPolynomialSpansRejected,
				newPolynomialRootCandidates);
	}

	/** @return copy after bounded two-source continuation work */
	public LocusIntersectionInstrumentationSnapshot2D withPairContinuation(
			long additionalComparisons, long topologyEpochs) {
		return withContinuation(additionalComparisons, topologyEpochs)
				.withPairCounters(branchPairs, componentPairs,
						parameterBoxesVisited, parameterBoxesRejected,
						pairCandidateBoxes, pairRefinementCalls,
						pairRefinementIterations, jacobianEvaluations,
						overlapChecks,
						pairContinuationComparisons + additionalComparisons,
						retainedPairEntries);
	}
}
