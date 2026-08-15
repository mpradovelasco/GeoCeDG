/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;

/**
 * Narrow semantic root continuation across at most two topology epochs.
 *
 * <p>Only an explicit continuation key inside the same constructive and branch
 * lineage may preserve a token. Parameters, intervals and coordinates never
 * select identity.</p>
 */
public final class LocusIntersectionContinuation2D {

	/**
	 * Applies the approved narrow continuation contract.
	 *
	 * @return current result with any uniquely established tokens continued
	 */
	public LocusIntersectionResult2D continueRoots(
			LocusIntersectionResult2D previous,
			LocusIntersectionResult2D current,
			LocusIntersectionPolicy2D policy) {
		java.util.Objects.requireNonNull(policy);
		return continueRoots(previous, current, policy.getWorkBudget()
				.getMaximumContinuationComparisons(), false);
	}

	/**
	 * Applies the same strict token contract to canonical two-locus roots.
	 *
	 * @return current result with any uniquely established pair tokens continued
	 */
	public LocusIntersectionResult2D continuePairRoots(
			LocusIntersectionResult2D previous,
			LocusIntersectionResult2D current,
			LocusPairIntersectionPolicy2D policy) {
		java.util.Objects.requireNonNull(policy);
		return continueRoots(previous, current, policy.getPairWorkBudget()
				.getMaximumPairContinuationComparisons(), true);
	}

	private LocusIntersectionResult2D continueRoots(
			LocusIntersectionResult2D previous,
			LocusIntersectionResult2D current, long maximumComparisons,
			boolean pair) {
		java.util.Objects.requireNonNull(current);
		if (previous == null || !continuableKind(current.getGeometryKind())
				|| current.getComputationStatus() != ComputationStatus.SUCCESS
				|| !continuableKind(previous.getGeometryKind())
				|| previous.getComputationStatus() != ComputationStatus.SUCCESS
				|| !sameConstructiveContext(previous, current)) {
			return current;
		}
		long comparisons = 0;
		ArrayList<LocusIntersectionSolution2D> continued = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : current.getFiniteSolutions()) {
			ContinuationOutcome outcome = continueOne(previous, current, solution,
					maximumComparisons, comparisons);
			comparisons += outcome.comparisons;
			if (comparisons > maximumComparisons) {
				return current;
			}
			continued.add(outcome.solution);
		}
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(current.getDiagnostics());
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.CONTINUATION_ESTABLISHED,
				"Semantic continuation compared " + comparisons
						+ " root contexts without Cartesian matching"));
		LocusIntersectionInstrumentationSnapshot2D work = pair
				? current.getWork().withPairContinuation(comparisons, 2)
				: current.getWork().withContinuation(comparisons, 2);
		return new LocusIntersectionResult2D(current.getSourceBinding(),
				current.getComputationStatus(), current.getCompletenessEvidence(),
				current.getGeometryKind(), current.getCurrentness(),
				current.getSupportLevel(), current.getNumericGuarantee(), continued,
				current.getOverlapEvidence(), work, diagnostics);
	}

	private static ContinuationOutcome continueOne(
			LocusIntersectionResult2D previous,
			LocusIntersectionResult2D current,
			LocusIntersectionSolution2D solution,
			long maximumComparisons, long alreadyCompared) {
		Optional<String> key = solution.getIdentity()
				.getExplicitContinuationKey();
		if (!key.isPresent()) {
			return new ContinuationOutcome(solution, 0);
		}
		List<LocusIntersectionSolution2D> sameCurrentKey = matching(current,
				solution.getIdentity().getEstablishedBranchLineage(), key.get());
		List<LocusIntersectionSolution2D> samePreviousKey = matching(previous,
				solution.getIdentity().getEstablishedBranchLineage(), key.get());
		long comparisons = current.getFiniteSolutions().size()
				+ previous.getFiniteSolutions().size();
		if (alreadyCompared + comparisons > maximumComparisons) {
			return new ContinuationOutcome(solution, comparisons);
		}
		LineageEventKind event = solution.getLineage().getEventKind();
		if (event == LineageEventKind.MERGE_CANDIDATE
				|| event == LineageEventKind.SPLIT_CANDIDATE) {
			List<String> parentTokens = parentTokens(previous,
					solution.getLineage().getCandidateParentContinuationKeys());
			return new ContinuationOutcome(reidentify(solution,
					solution.getIdentity().getRootToken(),
					parentTokens.isEmpty()
							? IdentityStatus.NEW_TOPOLOGICAL_SOLUTION
							: IdentityStatus.AMBIGUOUS_CONTINUATION,
					event, parentTokens, false), comparisons);
		}
		if (sameCurrentKey.size() == 1 && samePreviousKey.size() == 1) {
			if (!previous.getSourceBinding().getTopologyContext().equals(
					current.getSourceBinding().getTopologyContext())) {
				String priorToken = samePreviousKey.get(0).getIdentity()
						.getRootToken();
				return new ContinuationOutcome(reidentify(solution,
						solution.getIdentity().getRootToken(),
						IdentityStatus.IDENTITY_DISCONTINUITY,
						LineageEventKind.AMBIGUOUS_EVENT,
						List.of(priorToken), false), comparisons);
			}
			String priorToken = samePreviousKey.get(0).getIdentity().getRootToken();
			return new ContinuationOutcome(reidentify(solution, priorToken,
					IdentityStatus.CONTINUATION_ESTABLISHED,
					LineageEventKind.UNCHANGED, List.of(priorToken), true),
					comparisons);
		}
		if (sameCurrentKey.size() > 1 || samePreviousKey.size() > 1) {
			List<String> parents = new ArrayList<>();
			for (LocusIntersectionSolution2D prior : samePreviousKey) {
				parents.add(prior.getIdentity().getRootToken());
			}
			return new ContinuationOutcome(reidentify(solution,
					solution.getIdentity().getRootToken(),
					IdentityStatus.AMBIGUOUS_CONTINUATION,
					LineageEventKind.AMBIGUOUS_EVENT, parents, false),
					comparisons);
		}
		return new ContinuationOutcome(solution, comparisons);
	}

	private static LocusIntersectionSolution2D reidentify(
			LocusIntersectionSolution2D solution, String token,
			IdentityStatus status, LineageEventKind event,
			List<String> parentTokens, boolean established) {
		IntersectionRootIdentity2D old = solution.getIdentity();
		IntersectionRootIdentity2D identity = new IntersectionRootIdentity2D(
				token, old.getSourcePairIdentity(),
				old.getConstructiveIntersectionLineage(),
				old.getEstablishedBranchLineage(), old.getTopologyContext(),
				old.getExplicitContinuationKey(), status);
		IntersectionRootLineage2D lineage = new IntersectionRootLineage2D(event,
				parentTokens, List.of(token), Collections.emptyList(), established);
		return new LocusIntersectionSolution2D(identity,
				solution.getRevisionEvidence(), solution.getEvaluatedPoint(),
				solution.getClassification(), lineage, solution.getDiagnostics(),
				solution.getPairEvidence());
	}

	private static boolean continuableKind(GeometryKind kind) {
		return kind == GeometryKind.FINITE
				|| kind == GeometryKind.MIXED_FINITE_OVERLAP;
	}

	private static List<LocusIntersectionSolution2D> matching(
			LocusIntersectionResult2D result, String branchLineage, String key) {
		ArrayList<LocusIntersectionSolution2D> matches = new ArrayList<>();
		for (LocusIntersectionSolution2D candidate : result.getFiniteSolutions()) {
			if (hasContinuationIdentityEvidence(candidate)
					&& candidate.getIdentity().getEstablishedBranchLineage()
					.equals(branchLineage)
					&& candidate.getIdentity().getExplicitContinuationKey()
							.filter(key::equals).isPresent()) {
				matches.add(candidate);
			}
		}
		return matches;
	}

	private static List<String> parentTokens(
			LocusIntersectionResult2D previous,
			List<String> continuationKeys) {
		ArrayList<String> tokens = new ArrayList<>();
		for (String key : continuationKeys) {
			for (LocusIntersectionSolution2D solution
					: previous.getFiniteSolutions()) {
				if (solution.getIdentity().getExplicitContinuationKey()
						.filter(key::equals).isPresent()) {
					tokens.add(solution.getIdentity().getRootToken());
				}
			}
		}
		return tokens;
	}

	private static boolean hasContinuationIdentityEvidence(
			LocusIntersectionSolution2D solution) {
		IdentityStatus status = solution.getIdentity().getIdentityStatus();
		return solution.getRevisionEvidence().getLocalIsolationStatus()
					== LocalIsolationStatus.ESTABLISHED
				&& (status == IdentityStatus.NEW_TOPOLOGICAL_SOLUTION
						|| status == IdentityStatus.CONTINUATION_ESTABLISHED);
	}

	private static boolean sameConstructiveContext(
			LocusIntersectionResult2D previous,
			LocusIntersectionResult2D current) {
		IntersectionSourceBinding2D first = previous.getSourceBinding();
		IntersectionSourceBinding2D second = current.getSourceBinding();
		return first.getSourcePairIdentity().equals(second.getSourcePairIdentity())
				&& first.getConstructiveIntersectionLineage()
						.equals(second.getConstructiveIntersectionLineage())
				&& first.getTargetFamily() == second.getTargetFamily();
	}

	private static final class ContinuationOutcome {
		private final LocusIntersectionSolution2D solution;
		private final long comparisons;

		ContinuationOutcome(LocusIntersectionSolution2D solution,
				long comparisons) {
			this.solution = solution;
			this.comparisons = comparisons;
		}
	}
}
