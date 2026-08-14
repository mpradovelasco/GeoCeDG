/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;

/** Test-private candidate value model for G8A characterization only. */
final class G8AIntersectionSemanticModel {
	private G8AIntersectionSemanticModel() {
	}

	enum ComputationStatus {
		SUCCESS,
		INVALID_INPUT,
		UNSUPPORTED,
		NUMERICAL_FAILURE,
		WORK_LIMIT_REACHED
	}

	enum Completeness {
		COMPLETE,
		INCOMPLETE,
		NOT_ESTABLISHED
	}

	enum GeometryKind {
		EMPTY,
		FINITE,
		OVERLAP,
		INFINITELY_MANY,
		UNSUPPORTED_OVERLAP,
		UNRESOLVED
	}

	enum Currentness {
		CURRENT,
		NON_CURRENT
	}

	enum SupportLevel {
		EXACT,
		CERTIFIED,
		VERIFIED_UNCERTIFIED,
		UNSUPPORTED
	}

	enum ContactClass {
		TRANSVERSE_ESTABLISHED,
		TANGENT_ESTABLISHED,
		CONTACT_UNDETERMINED
	}

	enum MultiplicityStatus {
		ESTABLISHED,
		NOT_ESTABLISHED
	}

	enum DomainLocation {
		INTERIOR,
		INCLUDED_ENDPOINT,
		PERIODIC_SEAM,
		ISOLATED_COMPONENT
	}

	enum IdentityStatus {
		CONTINUATION_ESTABLISHED,
		NEW_TOPOLOGICAL_SOLUTION,
		AMBIGUOUS_CONTINUATION,
		IDENTITY_DISCONTINUITY,
		NOT_ESTABLISHED
	}

	enum LineageTransition {
		UNCHANGED,
		APPEARED,
		DISAPPEARED,
		MERGE_CANDIDATE,
		SPLIT_CANDIDATE,
		AMBIGUOUS_EVENT
	}

	enum Method {
		ANALYTIC_FACTORIZATION,
		CERTIFIED_BRACKET,
		DERIVATIVE_AWARE,
		EVALUATOR_ONLY,
		CONSERVATIVE_BROAD_PHASE
	}

	record Policy(String policyId, double rootParameterTolerance,
			double absoluteResidualTolerance,
			double relativeResidualTolerance, double tangencyThreshold,
			double deduplicationParameterTolerance,
			double continuationParameterTolerance,
			double coordinateVerificationTolerance) {
		Policy {
			Objects.requireNonNull(policyId);
			positive(rootParameterTolerance, "root parameter tolerance");
			positive(absoluteResidualTolerance, "absolute residual tolerance");
			positive(relativeResidualTolerance, "relative residual tolerance");
			positive(tangencyThreshold, "tangency threshold");
			positive(deduplicationParameterTolerance,
					"deduplication parameter tolerance");
			positive(continuationParameterTolerance,
					"continuation parameter tolerance");
			positive(coordinateVerificationTolerance,
					"coordinate verification tolerance");
		}

		static Policy measuredCandidate() {
			return new Policy("g8a-measured-candidate/v1", 1E-12, 2E-12,
					2E-12, 1E-10, 4E-12, 1E-8, 4E-12);
		}
	}

	record WorkBudget(int maximumSemanticEvaluations,
			int maximumSemanticDerivativeEvaluations,
			int maximumTargetEvaluations, int maximumCandidateIntervals,
			int maximumIsolationSubdivisions, int maximumIsolationDepth,
			int maximumRefinementIterationsPerCandidate,
			int maximumResidualVerifications, int maximumCandidateCount,
			int maximumContinuationComparisons,
			int maximumPublishedFiniteSolutions,
			int maximumRetainedIndexEntries,
			int maximumRetainedTopologyEpochs) {
		WorkBudget {
			if (maximumSemanticEvaluations < 1
					|| maximumSemanticDerivativeEvaluations < 1
					|| maximumTargetEvaluations < 1
					|| maximumCandidateIntervals < 1
					|| maximumIsolationSubdivisions < 1
					|| maximumIsolationDepth < 1
					|| maximumRefinementIterationsPerCandidate < 1
					|| maximumResidualVerifications < 1
					|| maximumCandidateCount < 1
					|| maximumContinuationComparisons < 1
					|| maximumPublishedFiniteSolutions < 1
					|| maximumRetainedIndexEntries < 0
					|| maximumRetainedTopologyEpochs < 1) {
				throw new IllegalArgumentException("All work limits must be bounded");
			}
		}

		static WorkBudget measuredCandidate() {
			return new WorkBudget(32768, 16384, 32768, 8192, 8192, 40,
					80, 1024, 512, 4096, 256, 0, 2);
		}
	}

	record SourceBinding(String sourcePairIdentity, String locusIdentity,
			long locusRevision, String targetIdentity, long targetUpdateStamp,
			String policyId) {
		SourceBinding {
			Objects.requireNonNull(sourcePairIdentity);
			Objects.requireNonNull(locusIdentity);
			Objects.requireNonNull(targetIdentity);
			Objects.requireNonNull(policyId);
			if (locusRevision < 1 || targetUpdateStamp < 0) {
				throw new IllegalArgumentException("Invalid source revision");
			}
		}
	}

	record DurableIdentity(String rootToken, String sourcePairIdentity,
			String constructiveIntersectionLineage, String branchLineage,
			String topologyContext, IdentityStatus status) {
		DurableIdentity {
			Objects.requireNonNull(rootToken);
			Objects.requireNonNull(sourcePairIdentity);
			Objects.requireNonNull(constructiveIntersectionLineage);
			Objects.requireNonNull(branchLineage);
			Objects.requireNonNull(topologyContext);
			Objects.requireNonNull(status);
		}
	}

	record RevisionEvidence(long locusRevision, long targetUpdateStamp,
			double semanticParameter, double isolatingLower,
			double isolatingUpper, double normalizedResidual, Method method,
			NumericGuarantee numericGuarantee) {
		RevisionEvidence {
			Objects.requireNonNull(method);
			Objects.requireNonNull(numericGuarantee);
			if (locusRevision < 1 || targetUpdateStamp < 0
					|| !Double.isFinite(semanticParameter)
					|| !Double.isFinite(isolatingLower)
					|| !Double.isFinite(isolatingUpper)
					|| isolatingLower > semanticParameter
					|| semanticParameter > isolatingUpper
					|| !Double.isFinite(normalizedResidual)
					|| normalizedResidual < 0) {
				throw new IllegalArgumentException("Invalid revision evidence");
			}
		}
	}

	record Classification(ContactClass contactClass,
			MultiplicityStatus multiplicityStatus, int establishedMultiplicity,
			DomainLocation domainLocation) {
		Classification {
			Objects.requireNonNull(contactClass);
			Objects.requireNonNull(multiplicityStatus);
			Objects.requireNonNull(domainLocation);
			if (multiplicityStatus == MultiplicityStatus.ESTABLISHED
					&& establishedMultiplicity < 1) {
				throw new IllegalArgumentException("Multiplicity must be positive");
			}
			if (multiplicityStatus == MultiplicityStatus.NOT_ESTABLISHED
					&& establishedMultiplicity != 0) {
				throw new IllegalArgumentException(
						"Unknown multiplicity cannot carry a magic integer");
			}
		}
	}

	record RootLineage(LineageTransition transition, List<String> parentTokens,
			List<String> childTokens, boolean deterministic) {
		RootLineage {
			Objects.requireNonNull(transition);
			parentTokens = List.copyOf(parentTokens);
			childTokens = List.copyOf(childTokens);
		}
	}

	record Solution(DurableIdentity durableIdentity,
			RevisionEvidence revisionEvidence, String branchKey,
			String componentKey, LocusPoint2D point,
			Classification classification, RootLineage lineage,
			List<String> diagnostics) {
		Solution {
			Objects.requireNonNull(durableIdentity);
			Objects.requireNonNull(revisionEvidence);
			Objects.requireNonNull(branchKey);
			Objects.requireNonNull(componentKey);
			Objects.requireNonNull(point);
			Objects.requireNonNull(classification);
			Objects.requireNonNull(lineage);
			diagnostics = List.copyOf(diagnostics);
		}
	}

	record WorkSnapshot(long semanticEvaluatorCalls,
			long semanticDerivativeCalls, long targetEquationEvaluations,
			long targetDerivativeEvaluations, long candidateIntervalsCreated,
			long candidateBoxesCreated, long broadPhaseCandidatesAccepted,
			long broadPhaseCandidatesRejected, long rootIsolationSubdivisions,
			long rootIsolationMaximumDepth, long rootRefinementCalls,
			long rootRefinementIterations, long residualVerificationCalls,
			long targetMembershipChecks, long deduplicationComparisons,
			long verifiedFiniteSolutions, long verifiedRootCount,
			long rejectedCandidates,
			long unresolvedCandidates, long completenessEstablishmentChecks,
			long completenessDomainsExcluded,
			long completenessDomainsUnresolved,
			long completenessCompleteResults,
			long completenessIncompleteResults,
			long completenessNotEstablishedResults,
			long overlapComponentsDetected,
			long identityContinuationPredictions,
			long identityContinuationComparisons,
			long identityContinuationsAccepted,
			long identityContinuationsAmbiguous,
			long identityContinuationsNotEstablished,
			long reparameterizationMappingsChecked,
			long reparameterizationContinuationsAccepted,
			long topologyMergeEvents, long topologySplitEvents,
			long topologyTerminationEvents, long queryLocalIndexBuilds,
			long sharedIndexBuilds, long indexHits, long indexMisses,
			long indexInvalidations, long indexEvictions,
			long retainedIndexEntries, long retainedSemanticRevisions,
			long retainedRootHistoryEntries, long approximateRetainedBytes,
			long publishedSnapshots,
			long partialSnapshotsPublished, long failedPrivateComputations,
			long staleRevisionEntriesAfterInvalidation,
			long wholeLocusRegenerations, long renderCacheReads,
			long renderVertexReads, long legacySampleReads, long viewportReads,
			long pixelToleranceReads, long metricIndexReads) {
		boolean hardZeroAuthorityReads() {
			return partialSnapshotsPublished == 0
					&& staleRevisionEntriesAfterInvalidation == 0
					&& wholeLocusRegenerations == 0 && renderCacheReads == 0
					&& renderVertexReads == 0 && legacySampleReads == 0
					&& viewportReads == 0 && pixelToleranceReads == 0
					&& metricIndexReads == 0;
		}
	}

	record Result(SourceBinding sourceBinding, ComputationStatus status,
			Completeness completeness, GeometryKind geometryKind,
			Currentness currentness, SupportLevel supportLevel,
			NumericGuarantee numericGuarantee, List<Solution> solutions,
			WorkSnapshot work, List<String> diagnostics) {
		Result {
			Objects.requireNonNull(sourceBinding);
			Objects.requireNonNull(status);
			Objects.requireNonNull(completeness);
			Objects.requireNonNull(geometryKind);
			Objects.requireNonNull(currentness);
			Objects.requireNonNull(supportLevel);
			Objects.requireNonNull(numericGuarantee);
			solutions = List.copyOf(solutions);
			diagnostics = List.copyOf(diagnostics);
			if (geometryKind == GeometryKind.EMPTY
					&& completeness != Completeness.COMPLETE) {
				throw new IllegalArgumentException(
						"Only a complete search may claim EMPTY");
			}
			if (geometryKind == GeometryKind.FINITE && solutions.isEmpty()) {
				throw new IllegalArgumentException(
						"FINITE requires at least one verified solution");
			}
			if (geometryKind != GeometryKind.FINITE && !solutions.isEmpty()
					&& geometryKind != GeometryKind.OVERLAP) {
				throw new IllegalArgumentException(
						"Non-finite result cannot carry anonymous point samples");
			}
		}

		boolean pointProjectionAdmissible() {
			return status == ComputationStatus.SUCCESS
					&& completeness == Completeness.COMPLETE
					&& geometryKind == GeometryKind.FINITE
					&& currentness == Currentness.CURRENT;
		}
	}

	static final class WorkCounters {
		long semanticEvaluatorCalls;
		long semanticDerivativeCalls;
		long targetEquationEvaluations;
		long targetDerivativeEvaluations;
		long candidateIntervalsCreated;
		long candidateBoxesCreated;
		long broadPhaseCandidatesAccepted;
		long broadPhaseCandidatesRejected;
		long rootIsolationSubdivisions;
		long rootIsolationMaximumDepth;
		long rootRefinementCalls;
		long rootRefinementIterations;
		long residualVerificationCalls;
		long targetMembershipChecks;
		long deduplicationComparisons;
		long verifiedFiniteSolutions;
		long verifiedRootCount;
		long rejectedCandidates;
		long unresolvedCandidates;
		long completenessEstablishmentChecks;
		long completenessDomainsExcluded;
		long completenessDomainsUnresolved;
		long completenessCompleteResults;
		long completenessIncompleteResults;
		long completenessNotEstablishedResults;
		long overlapComponentsDetected;
		long identityContinuationPredictions;
		long identityContinuationComparisons;
		long identityContinuationsAccepted;
		long identityContinuationsAmbiguous;
		long identityContinuationsNotEstablished;
		long reparameterizationMappingsChecked;
		long reparameterizationContinuationsAccepted;
		long topologyMergeEvents;
		long topologySplitEvents;
		long topologyTerminationEvents;
		long queryLocalIndexBuilds;
		long sharedIndexBuilds;
		long indexHits;
		long indexMisses;
		long indexInvalidations;
		long indexEvictions;
		long retainedIndexEntries;
		long retainedSemanticRevisions;
		long retainedRootHistoryEntries;
		long approximateRetainedBytes;
		long publishedSnapshots;
		long partialSnapshotsPublished;
		long failedPrivateComputations;
		long staleRevisionEntriesAfterInvalidation;
		long wholeLocusRegenerations;
		long renderCacheReads;
		long renderVertexReads;
		long legacySampleReads;
		long viewportReads;
		long pixelToleranceReads;
		long metricIndexReads;

		WorkSnapshot snapshot() {
			return new WorkSnapshot(semanticEvaluatorCalls,
					semanticDerivativeCalls, targetEquationEvaluations,
					targetDerivativeEvaluations, candidateIntervalsCreated,
					candidateBoxesCreated, broadPhaseCandidatesAccepted,
					broadPhaseCandidatesRejected, rootIsolationSubdivisions,
					rootIsolationMaximumDepth, rootRefinementCalls,
					rootRefinementIterations, residualVerificationCalls,
					targetMembershipChecks, deduplicationComparisons,
					verifiedFiniteSolutions, verifiedRootCount,
					rejectedCandidates,
					unresolvedCandidates, completenessEstablishmentChecks,
					completenessDomainsExcluded, completenessDomainsUnresolved,
					completenessCompleteResults,
					completenessIncompleteResults,
					completenessNotEstablishedResults,
					overlapComponentsDetected,
					identityContinuationPredictions,
					identityContinuationComparisons,
					identityContinuationsAccepted,
					identityContinuationsAmbiguous,
					identityContinuationsNotEstablished,
					reparameterizationMappingsChecked,
					reparameterizationContinuationsAccepted, topologyMergeEvents,
					topologySplitEvents, topologyTerminationEvents,
					queryLocalIndexBuilds, sharedIndexBuilds, indexHits,
					indexMisses, indexInvalidations, indexEvictions,
					retainedIndexEntries,
					retainedSemanticRevisions, retainedRootHistoryEntries,
					approximateRetainedBytes, publishedSnapshots,
					partialSnapshotsPublished,
					failedPrivateComputations,
					staleRevisionEntriesAfterInvalidation,
					wholeLocusRegenerations, renderCacheReads, renderVertexReads,
					legacySampleReads, viewportReads, pixelToleranceReads,
					metricIndexReads);
		}
	}

	private static void positive(double value, String name) {
		if (!Double.isFinite(value) || value <= 0) {
			throw new IllegalArgumentException(name + " must be finite and positive");
		}
	}
}
