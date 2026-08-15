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
				viewportReads, pixelToleranceReads, metricIndexReads);
	}
}
