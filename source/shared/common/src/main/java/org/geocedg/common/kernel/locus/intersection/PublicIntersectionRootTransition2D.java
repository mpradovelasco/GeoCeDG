/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;

/**
 * Stateful public root transition, analogous to ordinary conic root continuity.
 *
 * <p>Classic conic algorithms relate old output roots to fresh candidates and
 * retain an output only through a one-to-one assignment. This semantic variant
 * deliberately replaces Cartesian distance and output permutation with stable
 * constructive context, branch/component lineage, a typed transverse root
 * germ, and a bounded displacement in the provider's stable semantic-parameter
 * chart. The chart map is the identity while the provider contract is
 * unchanged, with the canonical modular lift at an exact complete periodic
 * seam. The germ and mapped displacement only filter possible edges; neither
 * establishes identity by itself or proves a homotopy. Tangency and merge/split
 * handling remain barriers in the enclosing resolver.</p>
 */
public final class PublicIntersectionRootTransition2D {
	private static final int MAXIMUM_ROOT_TUBE_PARTITIONS = 256;

	/**
	 * Builds the bounded previous-to-current relation for one revision step.
	 *
	 * @return immutable one-to-one transition evidence
	 */
	public Transition relate(LocusIntersectionResult2D previous,
			LocusIntersectionResult2D current, LocusDefinition2D definition,
			LocusIntersectionPolicy2D policy) {
		java.util.Objects.requireNonNull(current);
		java.util.Objects.requireNonNull(definition);
		java.util.Objects.requireNonNull(policy);
		if (previous == null || !sameConstructiveContext(previous, current)
				|| current.getWork().getUnresolvedCandidates() != 0
				|| previous.getWork().getUnresolvedCandidates() != 0
				|| !current.getOverlapEvidence().isEmpty()
				|| !previous.getOverlapEvidence().isEmpty()) {
			return Transition.empty();
		}

		List<LocusIntersectionSolution2D> priorRoots = previous
				.getFiniteSolutions().stream().filter(this::eligiblePrior).toList();
		List<LocusIntersectionSolution2D> currentRoots = current
				.getFiniteSolutions().stream().filter(this::eligibleCurrent).toList();
		Map<LocusIntersectionSolution2D, List<LocusIntersectionSolution2D>>
				priorsByCurrent = new IdentityHashMap<>();
		Map<LocusIntersectionSolution2D, List<LocusIntersectionSolution2D>>
				currentsByPrior = new IdentityHashMap<>();
		long comparisons = 0;
		long maximumComparisons = policy.getWorkBudget()
				.getMaximumContinuationComparisons();
		for (LocusIntersectionSolution2D currentRoot : currentRoots) {
			for (LocusIntersectionSolution2D priorRoot : priorRoots) {
				if (comparisons == maximumComparisons) {
					Map<LocusIntersectionSolution2D, Boolean> exhausted =
							new IdentityHashMap<>();
					for (LocusIntersectionSolution2D root : currentRoots) {
						exhausted.put(root, Boolean.TRUE);
					}
					return new Transition(Map.of(), exhausted, comparisons, true);
				}
				comparisons++;
				if (sameTransitionEdge(priorRoot, currentRoot, definition,
						policy)) {
					priorsByCurrent.computeIfAbsent(currentRoot,
							unused -> new ArrayList<>()).add(priorRoot);
					currentsByPrior.computeIfAbsent(priorRoot,
							unused -> new ArrayList<>()).add(currentRoot);
				}
			}
		}

		Map<LocusIntersectionSolution2D, LocusIntersectionSolution2D> unique =
				new IdentityHashMap<>();
		Map<LocusIntersectionSolution2D, Boolean> ambiguous =
				new IdentityHashMap<>();
		for (LocusIntersectionSolution2D currentRoot : currentRoots) {
			List<LocusIntersectionSolution2D> priors = priorsByCurrent.getOrDefault(
					currentRoot, List.of());
			if (priors.size() > 1) {
				ambiguous.put(currentRoot, Boolean.TRUE);
				continue;
			}
			if (priors.size() == 1) {
				LocusIntersectionSolution2D prior = priors.get(0);
				List<LocusIntersectionSolution2D> descendants =
						currentsByPrior.getOrDefault(prior, List.of());
				if (descendants.size() == 1) {
					unique.put(currentRoot, prior);
				} else {
					ambiguous.put(currentRoot, Boolean.TRUE);
				}
			}
		}
		return new Transition(unique, ambiguous, comparisons, false);
	}

	private boolean sameTransitionEdge(LocusIntersectionSolution2D prior,
			LocusIntersectionSolution2D current, LocusDefinition2D definition,
			LocusIntersectionPolicy2D policy) {
		IntersectionRootRevisionEvidence2D first = prior.getRevisionEvidence();
		IntersectionRootRevisionEvidence2D second = current.getRevisionEvidence();
		return first.getBranchSnapshotKey().equals(second.getBranchSnapshotKey())
				&& first.getResolvedValidComponentKey().equals(
						second.getResolvedValidComponentKey())
				&& first.getCurrentRootGerm().equals(second.getCurrentRootGerm())
				&& first.getCurrentRootGerm()
						.filter(PublicTargetIntersectionCapability2D
								::isCurrentPublicRootGerm)
						.isPresent()
				&& boundedMappedSemanticStep(first, second, definition, policy);
	}

	private static boolean boundedMappedSemanticStep(
			IntersectionRootRevisionEvidence2D previous,
			IntersectionRootRevisionEvidence2D current,
			LocusDefinition2D definition,
			LocusIntersectionPolicy2D policy) {
		LocusBranch2D branch = definition.getBranch(
				current.getBranchSnapshotKey());
		if (branch == null) {
			return false;
		}
		for (int index = 0; index < branch.getValidDomainComponents().size();
				index++) {
			LocusInterval2D component =
					branch.getValidDomainComponents().get(index);
			if (!IntersectionCapabilityContext2D.componentKey(
					branch.getBranchKey(), index).equals(
							current.getResolvedValidComponentKey())) {
				continue;
			}
			double span = component.getUpper() - component.getLower();
			int subdivisions = Math.min(MAXIMUM_ROOT_TUBE_PARTITIONS,
					policy.getWorkBudget()
					.getMaximumIsolationSubdivisions());
			double radius = Math.max(
					policy.getContinuationTolerance().getValue(),
					span / subdivisions);
			double delta = current.getSemanticParameter()
					- previous.getSemanticParameter();
			if (hasCompletePeriodicSeam(definition, branch, component)
					&& span > 0) {
				delta -= Math.rint(delta / span) * span;
				if (Math.abs(Math.abs(delta) - span / 2) <= policy
						.getContinuationTolerance().getValue()) {
					return false;
				}
			}
			return Math.abs(delta) <= radius;
		}
		return false;
	}

	private static boolean hasCompletePeriodicSeam(
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusInterval2D component) {
		return definition.getProvider().isPeriodic()
				&& branch.getProperties().contains(BranchProperty.PERIODIC)
				&& branch.getValidDomainComponents().size() == 1
				&& component.equals(branch.getDeclaredDriverDomain())
				&& component.equals(definition.getProvider().getDeclaredDomain());
	}

	private boolean eligiblePrior(LocusIntersectionSolution2D solution) {
		IdentityStatus status = solution.getIdentity().getIdentityStatus();
		IntersectionRootLineage2D lineage = solution.getLineage();
		String token = solution.getIdentity().getRootToken();
		if (!eligibleTransverseRoot(solution)
				|| !lineage.getCandidateParentContinuationKeys().isEmpty()
				|| !lineage.getCandidateChildTokens().equals(List.of(token))) {
			return false;
		}
		if (status == IdentityStatus.NEW_TOPOLOGICAL_SOLUTION) {
			return lineage.getEventKind() == LineageEventKind.APPEARED
					&& lineage.getCandidateParentTokens().isEmpty()
					&& !lineage.isContinuationEstablished();
		}
		if (status == IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED) {
			return lineage.getEventKind() == LineageEventKind.UNCHANGED;
		}
		return status == IdentityStatus.CONTINUATION_ESTABLISHED
				&& lineage.getEventKind() == LineageEventKind.UNCHANGED
				&& lineage.getCandidateParentTokens().equals(List.of(token))
				&& lineage.isContinuationEstablished();
	}

	private boolean eligibleCurrent(LocusIntersectionSolution2D solution) {
		LineageEventKind event = solution.getLineage().getEventKind();
		return eligibleTransverseRoot(solution)
				&& (event == LineageEventKind.APPEARED
						|| event == LineageEventKind.UNCHANGED)
				&& solution.getLineage().getCandidateParentTokens().isEmpty()
				&& solution.getLineage().getCandidateParentContinuationKeys()
						.isEmpty();
	}

	private static boolean eligibleTransverseRoot(
			LocusIntersectionSolution2D solution) {
		return solution.getRevisionEvidence().getLocalIsolationStatus()
					== LocalIsolationStatus.ESTABLISHED
				&& solution.getClassification().getContactClass()
						== ContactClass.TRANSVERSE_ESTABLISHED;
	}

	private static boolean sameConstructiveContext(
			LocusIntersectionResult2D previous,
			LocusIntersectionResult2D current) {
		IntersectionSourceBinding2D first = previous.getSourceBinding();
		IntersectionSourceBinding2D second = current.getSourceBinding();
		return first.getSourcePairIdentity().equals(second.getSourcePairIdentity())
				&& first.getConstructiveIntersectionLineage().equals(
						second.getConstructiveIntersectionLineage())
				&& first.getTopologyContext().equals(second.getTopologyContext())
				&& first.getTargetFamily() == second.getTargetFamily();
	}

	/** Immutable one-step relation and comparison evidence. */
	public static final class Transition {
		private final Map<LocusIntersectionSolution2D,
				LocusIntersectionSolution2D> unique;
		private final Map<LocusIntersectionSolution2D, Boolean> ambiguous;
		private final long comparisons;
		private final boolean budgetExhausted;

		private Transition(Map<LocusIntersectionSolution2D,
				LocusIntersectionSolution2D> unique,
				Map<LocusIntersectionSolution2D, Boolean> ambiguous,
				long comparisons, boolean budgetExhausted) {
			this.unique = new IdentityHashMap<>(unique);
			this.ambiguous = new IdentityHashMap<>(ambiguous);
			this.comparisons = comparisons;
			this.budgetExhausted = budgetExhausted;
		}

		private static Transition empty() {
			return new Transition(Map.of(), Map.of(), 0, false);
		}

		/** @return unique prior root selected by the semantic relation */
		public Optional<LocusIntersectionSolution2D> priorFor(
				LocusIntersectionSolution2D current) {
			return Optional.ofNullable(unique.get(current));
		}

		/** @return whether more than one semantic transition edge remained */
		public boolean isAmbiguous(LocusIntersectionSolution2D current) {
			return ambiguous.containsKey(current);
		}

		/** @return deterministic number of previous/current comparisons */
		public long getComparisons() {
			return comparisons;
		}

		/** @return whether the comparison ceiling ended the relation early */
		public boolean isBudgetExhausted() {
			return budgetExhausted;
		}
	}
}
