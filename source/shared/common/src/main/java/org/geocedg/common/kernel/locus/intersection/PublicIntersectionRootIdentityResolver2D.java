/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Currentness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;

/**
 * Public G9U0-R4 authority for deterministic current-root allocation.
 *
 * <p>The numeric solver remains responsible for geometry and local proof. This
 * resolver allocates an opaque identity only after the root is current, finite,
 * transverse, locally isolated and free of candidate-lineage ambiguity. It
 * resolves an allocation from a selector intrinsic to the current snapshot and
 * reuses it only when that selector is unique under the stable ledger contract.
 * The previous/current relation contributes continuity and topology diagnostics
 * only; it never chooses the current token. Cartesian coordinates, semantic
 * parameter equality, list order, screen proximity and rendering are never
 * identity authority.</p>
 */
public final class PublicIntersectionRootIdentityResolver2D {
	private final PublicIntersectionRootTransition2D transitionAuthority =
			new PublicIntersectionRootTransition2D();

	/**
	 * Resolves public identity before the enclosing algorithm atomically commits
	 * the token ledger and publishes the rich result.
	 *
	 * @return immutable current result with only justified identities promoted
	 */
	public LocusIntersectionResult2D resolve(
			LocusIntersectionResult2D previous,
			LocusIntersectionResult2D candidate,
			LocusDefinition2D definition,
			LocusIntersectionTarget2D target,
			LocusIntersectionPolicy2D policy,
			LocusIntersectionTokenLedger2D.Evaluation evaluation) {
		java.util.Objects.requireNonNull(candidate);
		java.util.Objects.requireNonNull(definition);
		java.util.Objects.requireNonNull(target);
		java.util.Objects.requireNonNull(policy);
		java.util.Objects.requireNonNull(evaluation);
		LocusIntersectionResult2D current = candidate;
		current = failClosedObservedTopologyTransition(previous, current,
				definition, evaluation);
		if (!supportsAllocation(current)) {
			return current;
		}

		String targetContract =
				IntersectionRootAddressProof2D.targetContractSignature(target);
		String continuationContract = continuationContract(definition,
				targetContract);
		PublicIntersectionRootTransition2D.Transition transition =
				transitionAuthority.relate(previous, current, definition, policy);
		ArrayList<LocusIntersectionSolution2D> resolved =
				new ArrayList<>(current.getFiniteSolutions());
		Map<IntersectionRootDeterministicSelector2D, List<CurrentRoot>> bySelector =
				new LinkedHashMap<>();
		Map<String, Integer> finiteRootsByComponent =
				finiteRootCounts(current, definition);
		for (CurrentRoot root : currentRoots(current, definition)) {
			bySelector.computeIfAbsent(root.selector,
					unused -> new ArrayList<>()).add(root);
		}
		Map<LocusIntersectionSolution2D,
				IntersectionRootDeterministicSelector2D> priorPhaseSelectors =
				phaseSelectorsBySolution(previous);
		int allocated = 0;
		int resumed = 0;
		int ambiguous = 0;
		int discontinuous = 0;
		ArrayList<CurrentRoot> uniqueRoots = new ArrayList<>();
		for (Map.Entry<IntersectionRootDeterministicSelector2D,
				List<CurrentRoot>> entry : bySelector.entrySet()) {
			if (entry.getValue().size() == 1) {
				uniqueRoots.add(entry.getValue().get(0));
			} else {
				Optional<List<CurrentRoot>> ranked = intrinsicPhaseRanked(
						entry.getValue(), definition);
				if (!ranked.isPresent()) {
					List<String> priorPhaseParents = priorPhaseParentsForBase(
							entry.getKey(), priorPhaseSelectors);
					for (CurrentRoot root : entry.getValue()) {
						if (priorPhaseParents.isEmpty()) {
							resolved.set(root.solutionIndex,
									failClosedSelectorCollision(root, evaluation));
							ambiguous++;
						} else {
							resolved.set(root.solutionIndex,
									failClosedPhaseTransition(root, evaluation,
											priorPhaseParents));
							discontinuous++;
						}
					}
					continue;
				}
				List<String> phaseParents = observedPhaseTransitionParents(
						ranked.get(), priorPhaseSelectors, transition);
				if (!phaseParents.isEmpty()) {
					for (CurrentRoot root : ranked.get()) {
						resolved.set(root.solutionIndex,
								failClosedPhaseTransition(root, evaluation,
										phaseParents));
						discontinuous++;
					}
					continue;
				}
				for (CurrentRoot root : ranked.get()) {
					uniqueRoots.add(root);
				}
			}
		}
		uniqueRoots.sort(Comparator.comparing(root -> root.selector));
		for (CurrentRoot currentRoot : uniqueRoots) {
			IntersectionRootAddressProof2D addressProof = addressProof(definition,
					targetContract,
					currentRoot.solution.getRevisionEvidence()
							.getSemanticParameter());
			IntersectionRootAllocation2D allocation = evaluation.resolveCurrentRoot(
					currentRoot.componentLineage, continuationContract,
					currentRoot.selector, addressProof,
					finiteRootsByComponent.getOrDefault(
							currentRoot.componentLineage, 0) == 1);
			boolean reused = allocation.isReused();
			boolean continuityEstablished = transition
					.priorFor(currentRoot.solution)
					.filter(prior -> prior.getIdentity().getRootToken()
							.equals(allocation.getRootToken()))
					.isPresent();
			resolved.set(currentRoot.solutionIndex,
					reidentify(currentRoot.solution, allocation,
							reused
									? IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED
									: IdentityStatus.NEW_TOPOLOGICAL_SOLUTION,
							reused ? LineageEventKind.UNCHANGED
									: LineageEventKind.APPEARED,
							continuityEstablished
									? List.of(allocation.getRootToken())
									: Collections.emptyList(),
							continuityEstablished,
							reused
									? "Unique current semantic selector restored the "
											+ "exact durable root allocation"
									: "Unique current semantic selector received a "
											+ "fresh opaque ledger identity"));
			if (reused) {
				resumed++;
			} else {
				allocated++;
			}
		}
		if (allocated == 0 && resumed == 0 && ambiguous == 0
				&& discontinuous == 0) {
			return current;
		}
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(current.getDiagnostics());
		if (resumed != 0) {
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.DETERMINISTIC_SELECTION_ESTABLISHED,
					"G9U0-R4 resolved " + resumed
							+ " exact ledger root allocations from unique "
							+ "current semantic selectors"));
		}
		if (ambiguous != 0) {
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.CONTINUATION_AMBIGUOUS,
					"G9U0-R4 kept " + ambiguous
							+ " roots fail-closed because their current "
							+ "semantic selectors were not unique"));
		}
		if (discontinuous != 0) {
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.IDENTITY_DISCONTINUITY,
					"G9U0-R4 kept " + discontinuous
							+ " roots fail-closed because their intrinsic "
							+ "semantic phase changed across an observed "
							+ "topology/seam transition"));
		}
		if (transition.isBudgetExhausted()) {
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.WORK_LIMIT_REACHED,
					"G9U0-R4 exhausted the optional previous/current "
							+ "continuity comparison budget; deterministic "
							+ "current selection remained authoritative"));
		}
		long retainedEpochs = previous == null ? 1 : 2;
		LocusIntersectionInstrumentationSnapshot2D work = current.getWork()
				.withContinuation(transition.getComparisons(), retainedEpochs);
		return new LocusIntersectionResult2D(current.getSourceBinding(),
				current.getComputationStatus(), current.getCompletenessEvidence(),
				current.getGeometryKind(), current.getCurrentness(),
				current.getSupportLevel(), current.getNumericGuarantee(), resolved,
				current.getOverlapEvidence(),
				current.getUnresolvedCandidateComponentKeys(), work, diagnostics);
	}

	private static LocusIntersectionSolution2D failClosedSelectorCollision(
			CurrentRoot root,
			LocusIntersectionTokenLedger2D.Evaluation evaluation) {
		LocusIntersectionSolution2D solution = root.solution;
		IntersectionTokenLineage2D tokenLineage =
				IntersectionTokenLineage2D.forSingleComponent(
						root.component.branchKey, root.component.interval,
						Optional.empty());
		String handle = evaluation.revisionLocalHandle(tokenLineage,
				solution.getRevisionEvidence());
		IntersectionRootIdentity2D old = solution.getIdentity();
		IntersectionRootIdentity2D identity = new IntersectionRootIdentity2D(
				handle, old.getSourcePairIdentity(),
				old.getConstructiveIntersectionLineage(),
				old.getEstablishedBranchLineage(), old.getTopologyContext(),
				Optional.empty(), IdentityStatus.AMBIGUOUS_CONTINUATION);
		IntersectionRootLineage2D lineage = new IntersectionRootLineage2D(
				LineageEventKind.AMBIGUOUS_EVENT, Collections.emptyList(),
				List.of(handle), Collections.emptyList(), false);
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(solution.getDiagnostics());
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.CONTINUATION_AMBIGUOUS,
				"Current semantic root selector was not unique; no order, "
						+ "coordinate or historical fallback was used"));
		return new LocusIntersectionSolution2D(identity,
				solution.getRevisionEvidence(), solution.getEvaluatedPoint(),
				solution.getClassification(), lineage, diagnostics,
				solution.getPairEvidence());
	}

	private static LocusIntersectionSolution2D failClosedPhaseTransition(
			CurrentRoot root,
			LocusIntersectionTokenLedger2D.Evaluation evaluation,
			List<String> parentTokens) {
		LocusIntersectionSolution2D solution = root.solution;
		IntersectionTokenLineage2D tokenLineage =
				IntersectionTokenLineage2D.forSingleComponent(
						root.component.branchKey, root.component.interval,
						Optional.empty());
		String handle = evaluation.revisionLocalHandle(tokenLineage,
				solution.getRevisionEvidence());
		IntersectionRootIdentity2D old = solution.getIdentity();
		IntersectionRootIdentity2D identity = new IntersectionRootIdentity2D(
				handle, old.getSourcePairIdentity(),
				old.getConstructiveIntersectionLineage(),
				old.getEstablishedBranchLineage(), old.getTopologyContext(),
				Optional.empty(), IdentityStatus.IDENTITY_DISCONTINUITY);
		IntersectionRootLineage2D lineage = new IntersectionRootLineage2D(
				LineageEventKind.AMBIGUOUS_EVENT, parentTokens, List.of(handle),
				Collections.emptyList(), false);
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(solution.getDiagnostics());
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.IDENTITY_DISCONTINUITY,
				"Observed intrinsic semantic phase/rank transition; prior exact "
						+ "tokens were invalidated instead of being shifted"));
		return new LocusIntersectionSolution2D(identity,
				solution.getRevisionEvidence(), solution.getEvaluatedPoint(),
				solution.getClassification(), lineage, diagnostics,
				solution.getPairEvidence());
	}

	private static LocusIntersectionResult2D failClosedObservedTopologyTransition(
			LocusIntersectionResult2D previous,
			LocusIntersectionResult2D current, LocusDefinition2D definition,
			LocusIntersectionTokenLedger2D.Evaluation evaluation) {
		if (previous == null || current.getFiniteSolutions().isEmpty()
				|| previous.getFiniteSolutions().isEmpty()) {
			return current;
		}
		ArrayList<LocusIntersectionSolution2D> guarded =
				new ArrayList<>(current.getFiniteSolutions());
		Map<ComponentKey, List<LocusIntersectionSolution2D>> priorComponents =
				byComponent(previous);
		Map<ComponentKey, List<LocusIntersectionSolution2D>> currentComponents =
				byComponent(current);
		Set<ComponentKey> priorNonTransverseComponents =
				nonTransverseComponents(previous);
		int guardedCount = 0;
		for (int index = 0; index < guarded.size(); index++) {
			LocusIntersectionSolution2D solution = guarded.get(index);
			ComponentKey key = ComponentKey.of(solution);
			List<LocusIntersectionSolution2D> priorComponent =
					priorComponents.getOrDefault(key, List.of());
			List<LocusIntersectionSolution2D> currentComponent =
					currentComponents.getOrDefault(key, List.of());
			boolean split = currentComponent.size() > priorComponent.size()
					&& priorNonTransverseComponents.contains(key);
			boolean merge = currentComponent.size() < priorComponent.size()
					&& solution.getClassification().getContactClass()
							!= ContactClass.TRANSVERSE_ESTABLISHED;
			if (!split && !merge) {
				continue;
			}
			ComponentAddress address = component(definition,
					solution.getRevisionEvidence());
			if (address == null) {
				continue;
			}
			IntersectionTokenLineage2D tokenLineage =
					IntersectionTokenLineage2D.forSingleComponent(
							address.branchKey, address.interval, Optional.empty());
			String handle = evaluation.revisionLocalHandle(tokenLineage,
					solution.getRevisionEvidence());
			guarded.set(index, failClosedTopology(solution, handle,
					split ? LineageEventKind.SPLIT_CANDIDATE
						: LineageEventKind.MERGE_CANDIDATE,
					priorComponent));
			guardedCount++;
		}
		if (guardedCount == 0) {
			return current;
		}
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(current.getDiagnostics());
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.CONTINUATION_AMBIGUOUS,
				"G9U0-R4 kept " + guardedCount
						+ " observed merge/split roots fail-closed"));
		return new LocusIntersectionResult2D(current.getSourceBinding(),
				current.getComputationStatus(), current.getCompletenessEvidence(),
				current.getGeometryKind(), current.getCurrentness(),
				current.getSupportLevel(), current.getNumericGuarantee(), guarded,
				current.getOverlapEvidence(),
				current.getUnresolvedCandidateComponentKeys(), current.getWork(),
				diagnostics);
	}

	private static Map<ComponentKey, List<LocusIntersectionSolution2D>>
			byComponent(LocusIntersectionResult2D result) {
		Map<ComponentKey, List<LocusIntersectionSolution2D>> grouped =
				new LinkedHashMap<>();
		for (LocusIntersectionSolution2D solution
				: result.getFiniteSolutions()) {
			grouped.computeIfAbsent(ComponentKey.of(solution),
					unused -> new ArrayList<>()).add(solution);
		}
		return grouped;
	}

	private static Set<ComponentKey> nonTransverseComponents(
			LocusIntersectionResult2D result) {
		Set<ComponentKey> components = new HashSet<>();
		for (LocusIntersectionSolution2D solution
			: result.getFiniteSolutions()) {
			if (solution.getClassification().getContactClass()
					!= ContactClass.TRANSVERSE_ESTABLISHED) {
				components.add(ComponentKey.of(solution));
			}
		}
		return components;
	}

	private static LocusIntersectionSolution2D failClosedTopology(
			LocusIntersectionSolution2D solution, String handle,
			LineageEventKind event,
			List<LocusIntersectionSolution2D> priorComponent) {
		IntersectionRootIdentity2D old = solution.getIdentity();
		IntersectionRootIdentity2D identity = new IntersectionRootIdentity2D(
				handle, old.getSourcePairIdentity(),
				old.getConstructiveIntersectionLineage(),
				old.getEstablishedBranchLineage(), old.getTopologyContext(),
				Optional.empty(), IdentityStatus.AMBIGUOUS_CONTINUATION);
		List<LocusIntersectionSolution2D> canonicalParents = priorComponent.stream()
				.sorted(Comparator.comparing(root -> root.getIdentity().getRootToken()))
				.toList();
		List<String> parentTokens = canonicalParents.stream()
				.map(root -> root.getIdentity().getRootToken()).toList();
		List<String> parentKeys = canonicalParents.stream()
				.map(root -> root.getIdentity().getExplicitContinuationKey())
				.filter(Optional::isPresent).map(Optional::get).toList();
		IntersectionRootLineage2D lineage = new IntersectionRootLineage2D(event,
				parentTokens, List.of(handle), parentKeys, false);
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(solution.getDiagnostics());
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.CONTINUATION_AMBIGUOUS,
				"Observed " + event
						+ " has no authorized one-to-one continuation"));
		return new LocusIntersectionSolution2D(identity,
				solution.getRevisionEvidence(), solution.getEvaluatedPoint(),
				solution.getClassification(), lineage, diagnostics,
				solution.getPairEvidence());
	}

	private static boolean supportsAllocation(LocusIntersectionResult2D result) {
		return result.getComputationStatus() == ComputationStatus.SUCCESS
				&& (result.getGeometryKind() == GeometryKind.FINITE
						|| result.getGeometryKind()
								== GeometryKind.MIXED_FINITE_OVERLAP)
				&& result.getCurrentness() == Currentness.CURRENT
				&& result.getSupportLevel() != SupportLevel.UNSUPPORTED;
	}

	private static List<CurrentRoot> currentRoots(
			LocusIntersectionResult2D current, LocusDefinition2D definition) {
		ArrayList<CurrentRoot> roots = new ArrayList<>();
		for (int index = 0; index < current.getFiniteSolutions().size(); index++) {
			LocusIntersectionSolution2D solution =
					current.getFiniteSolutions().get(index);
			ComponentAddress component = component(definition,
					solution.getRevisionEvidence());
			Optional<String> germ = solution.getRevisionEvidence()
					.getCurrentRootGerm();
			if (eligibleCurrent(current, solution) && component != null
					&& germ.filter(value -> PublicTargetIntersectionCapability2D
							.isCurrentPublicRootGermForComponent(value,
									component.stableLineage)).isPresent()) {
				IntersectionRootDeterministicSelector2D selector =
						IntersectionRootDeterministicSelector2D.of(
								component.stableLineage, germ.get());
				roots.add(new CurrentRoot(index, solution, component, selector));
			}
		}
		return roots;
	}

	private static Optional<List<CurrentRoot>> intrinsicPhaseRanked(
			List<CurrentRoot> collisionGroup,
			LocusDefinition2D definition) {
		if (collisionGroup.size() < 2) {
			return Optional.of(List.copyOf(collisionGroup));
		}
		LocusDriverDomainProvider2D provider = definition.getProvider();
		CurrentRoot first = collisionGroup.get(0);
		ArrayList<CurrentRoot> ordered = new ArrayList<>();
		for (CurrentRoot root : collisionGroup) {
			if (!root.componentLineage.equals(first.componentLineage)
					|| !root.selector.equals(first.selector)
					|| root.component.orientation
							!= first.component.orientation
					|| root.component.completePeriodicCycle
							!= first.component.completePeriodicCycle) {
				return Optional.empty();
			}
			IntersectionRootRevisionEvidence2D evidence =
					root.solution.getRevisionEvidence();
			double parameter = provider.canonicalize(
					evidence.getSemanticParameter());
			IntersectionParameterInterval2D interval =
					evidence.getIsolatingInterval();
			if (!Double.isFinite(parameter)
					|| !root.component.interval.contains(parameter,
							provider.getDomainEpsilon())
					|| !interval.contains(parameter,
							provider.getDomainEpsilon())) {
				return Optional.empty();
			}
			Optional<PhaseInterval> phase = phaseInterval(root.component,
					interval, parameter, provider.getDomainEpsilon());
			if (!phase.isPresent()) {
				return Optional.empty();
			}
			ordered.add(root.withPhaseInterval(phase.get()));
		}
		/*
		 * Phase zero is the declared start of the oriented component. For a
		 * periodic component this is the explicit fundamental-interval seam,
		 * not a root-dependent moving anchor. Consequently ordinary motion
		 * preserves rank until roots collide or one reaches the seam. A seam
		 * interval itself is rejected above; the observed transition guard then
		 * invalidates old ranked tokens rather than rotating them.
		 */
		ordered.sort(Comparator.comparingDouble(root -> root.phase.lower));
		for (int index = 1; index < ordered.size(); index++) {
			if (ordered.get(index - 1).phase.upper
					>= ordered.get(index).phase.lower) {
				return Optional.empty();
			}
		}
		ArrayList<CurrentRoot> ranked = new ArrayList<>();
		for (int rank = 0; rank < ordered.size(); rank++) {
			CurrentRoot root = ordered.get(rank);
			IntersectionRootDeterministicSelector2D selector =
					IntersectionRootDeterministicSelector2D.ofIntrinsicPhase(
							root.componentLineage,
							root.selector.getCurrentRootGerm(),
							root.component.orientation,
							root.component.completePeriodicCycle,
							ordered.size(), rank);
			ranked.add(root.withSelector(selector));
		}
		return Optional.of(List.copyOf(ranked));
	}

	private static Optional<PhaseInterval> phaseInterval(
			ComponentAddress component, IntersectionParameterInterval2D interval,
			double canonicalParameter, double epsilon) {
		double start = component.interval.getLower();
		double end = component.interval.getUpper();
		double span = end - start;
		if (!(span > 0)) {
			return Optional.empty();
		}
		if (component.completePeriodicCycle
				&& (interval.getLower() <= start + epsilon
						|| interval.getUpper() >= end - epsilon)) {
			return Optional.empty();
		}
		double lower;
		double upper;
		double point;
		if (component.orientation == Orientation.INCREASING) {
			lower = interval.getLower() - start;
			upper = interval.getUpper() - start;
			point = canonicalParameter - start;
		} else {
			lower = end - interval.getUpper();
			upper = end - interval.getLower();
			point = end - canonicalParameter;
			if (component.completePeriodicCycle && point >= span - epsilon) {
				point = 0;
			}
		}
		if (!Double.isFinite(lower) || !Double.isFinite(upper)
				|| !Double.isFinite(point) || lower < -epsilon
				|| upper > span + epsilon || point < lower - epsilon
				|| point > upper + epsilon) {
			return Optional.empty();
		}
		return Optional.of(new PhaseInterval(Math.max(0, lower),
				Math.min(span, upper)));
	}

	private static Map<LocusIntersectionSolution2D,
			IntersectionRootDeterministicSelector2D> phaseSelectorsBySolution(
					LocusIntersectionResult2D result) {
		if (result == null) {
			return Map.of();
		}
		Map<LocusIntersectionSolution2D,
				IntersectionRootDeterministicSelector2D> selectors =
				new IdentityHashMap<>();
		for (LocusIntersectionSolution2D solution
				: result.getFiniteSolutions()) {
			solution.getIdentity().getExplicitContinuationKey()
					.flatMap(LocusIntersectionTokenLedger2D
							::selectorFromContinuationKey)
					.filter(IntersectionRootDeterministicSelector2D
							::hasIntrinsicPhase)
					.ifPresent(selector -> selectors.put(solution, selector));
		}
		return selectors;
	}

	private static List<String> priorPhaseParentsForBase(
			IntersectionRootDeterministicSelector2D base,
			Map<LocusIntersectionSolution2D,
					IntersectionRootDeterministicSelector2D> priorSelectors) {
		ArrayList<String> tokens = new ArrayList<>();
		for (Map.Entry<LocusIntersectionSolution2D,
				IntersectionRootDeterministicSelector2D> prior
				: priorSelectors.entrySet()) {
			IntersectionRootDeterministicSelector2D selector = prior.getValue();
			if (selector.getComponentLineage().equals(base.getComponentLineage())
					&& selector.getCurrentRootGerm().equals(
							base.getCurrentRootGerm())) {
				tokens.add(prior.getKey().getIdentity().getRootToken());
			}
		}
		Collections.sort(tokens);
		return List.copyOf(tokens);
	}

	private static List<String> observedPhaseTransitionParents(
			List<CurrentRoot> currentRoots,
			Map<LocusIntersectionSolution2D,
					IntersectionRootDeterministicSelector2D> priorSelectors,
			PublicIntersectionRootTransition2D.Transition transition) {
		if (currentRoots.isEmpty()) {
			return List.of();
		}
		List<String> priorGroup = priorPhaseParentsForBase(
				currentRoots.get(0).selector, priorSelectors);
		if (priorGroup.isEmpty()) {
			return List.of();
		}
		/*
		 * Rank is current-snapshot authority on an oriented nonperiodic component;
		 * a missing bounded continuation edge must not replace that authority. On a
		 * complete periodic component, however, the same current ranks can be a
		 * cyclic permutation across the fundamental seam. Without a semantic lift,
		 * every prior member therefore has to map one-to-one to the same selector.
		 * This relation only guards reuse of old tokens; it never selects the current
		 * root. Cardinality or an actually observed selector change is a topology
		 * barrier for either domain kind.
		 */
		boolean periodic = currentRoots.get(0).component.completePeriodicCycle;
		boolean phaseChanged = priorGroup.size() != currentRoots.size()
				|| periodic && transition.isBudgetExhausted();
		Set<String> mappedParents = new HashSet<>();
		for (CurrentRoot current : currentRoots) {
			if (periodic && transition.isAmbiguous(current.solution)) {
				phaseChanged = true;
			}
			Optional<LocusIntersectionSolution2D> prior = transition
					.priorFor(current.solution);
			if (!prior.isPresent()) {
				if (periodic) {
					phaseChanged = true;
				}
				continue;
			}
			IntersectionRootDeterministicSelector2D priorSelector =
					priorSelectors.get(prior.get());
			if (priorSelector == null) {
				if (periodic) {
					phaseChanged = true;
				}
				continue;
			}
			mappedParents.add(prior.get().getIdentity().getRootToken());
			if (!priorSelector.equals(current.selector)) {
				phaseChanged = true;
			}
		}
		if (periodic && (!mappedParents.equals(new HashSet<>(priorGroup))
				|| mappedParents.size() != currentRoots.size())) {
			phaseChanged = true;
		}
		if (!phaseChanged) {
			return List.of();
		}
		return priorGroup;
	}

	private static Map<String, Integer> finiteRootCounts(
			LocusIntersectionResult2D current, LocusDefinition2D definition) {
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (LocusIntersectionSolution2D solution
				: current.getFiniteSolutions()) {
			ComponentAddress address = component(definition,
					solution.getRevisionEvidence());
			if (address != null) {
				counts.merge(address.stableLineage, 1, Integer::sum);
			}
		}
		return counts;
	}

	private static boolean eligibleCurrent(LocusIntersectionResult2D result,
			LocusIntersectionSolution2D solution) {
		IntersectionRootIdentity2D identity = solution.getIdentity();
		IntersectionRootRevisionEvidence2D evidence =
				solution.getRevisionEvidence();
		LineageEventKind event = solution.getLineage().getEventKind();
		return !result.getSourceBinding().isLocusPair()
				&& evidence.getLocalIsolationStatus()
						== LocalIsolationStatus.ESTABLISHED
				&& solution.getClassification().getContactClass()
						== ContactClass.TRANSVERSE_ESTABLISHED
				&& !solution.getPairEvidence().isPresent()
				&& (event == LineageEventKind.APPEARED
						|| event == LineageEventKind.UNCHANGED)
				&& solution.getLineage().getCandidateParentTokens().isEmpty()
				&& solution.getLineage().getCandidateParentContinuationKeys()
						.isEmpty()
				&& identity.getSourcePairIdentity().equals(
						result.getSourceBinding().getSourcePairIdentity())
				&& identity.getConstructiveIntersectionLineage().equals(
						result.getSourceBinding()
								.getConstructiveIntersectionLineage())
				&& identity.getTopologyContext().equals(
						result.getSourceBinding().getTopologyContext())
				&& identity.getEstablishedBranchLineage().equals(
						evidence.getBranchSnapshotKey())
				&& evidence.getLocusSemanticRevision()
						== result.getSourceBinding().getLocusSemanticRevision()
				&& evidence.getTargetUpdateStamp()
						== result.getSourceBinding().getTargetUpdateStamp()
				&& !result.getUnresolvedCandidateComponentKeys().contains(
						evidence.getResolvedValidComponentKey())
				&& !hasOverlapOnComponent(result, evidence);
	}

	private static boolean hasOverlapOnComponent(
			LocusIntersectionResult2D result,
			IntersectionRootRevisionEvidence2D evidence) {
		for (IntersectionOverlapEvidence2D overlap : result.getOverlapEvidence()) {
			if (overlap.getBranchKey().equals(evidence.getBranchSnapshotKey())
					&& overlap.getComponentKey().equals(
							evidence.getResolvedValidComponentKey())) {
				return true;
			}
		}
		return false;
	}

	private static LocusIntersectionSolution2D reidentify(
			LocusIntersectionSolution2D solution,
			IntersectionRootAllocation2D allocation, IdentityStatus status,
			LineageEventKind event, List<String> parentTokens,
			boolean continuationEstablished, String diagnostic) {
		IntersectionRootIdentity2D old = solution.getIdentity();
		IntersectionRootIdentity2D identity = new IntersectionRootIdentity2D(
				allocation.getRootToken(), old.getSourcePairIdentity(),
				old.getConstructiveIntersectionLineage(),
				old.getEstablishedBranchLineage(), old.getTopologyContext(),
				Optional.of(allocation.getContinuationKey()), status);
		IntersectionRootLineage2D lineage = new IntersectionRootLineage2D(event,
				parentTokens, List.of(allocation.getRootToken()),
				Collections.emptyList(), continuationEstablished);
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				withoutPreResolutionContinuation(solution.getDiagnostics());
		DiagnosticCode code = status == IdentityStatus.CONTINUATION_ESTABLISHED
				? DiagnosticCode.CONTINUATION_ESTABLISHED
				: status == IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED
						? DiagnosticCode.DETERMINISTIC_SELECTION_ESTABLISHED
						: DiagnosticCode.LOCAL_ISOLATION_ESTABLISHED;
		diagnostics.add(new IntersectionDiagnostic2D(
				code, diagnostic));
		return new LocusIntersectionSolution2D(identity,
				solution.getRevisionEvidence(), solution.getEvaluatedPoint(),
				solution.getClassification(), lineage, diagnostics,
				solution.getPairEvidence());
	}

	private static ArrayList<IntersectionDiagnostic2D>
			withoutPreResolutionContinuation(
					List<IntersectionDiagnostic2D> input) {
		ArrayList<IntersectionDiagnostic2D> diagnostics = new ArrayList<>();
		for (IntersectionDiagnostic2D diagnostic : input) {
			if (diagnostic.getCode() != DiagnosticCode.CONTINUATION_AMBIGUOUS
					&& diagnostic.getCode()
							!= DiagnosticCode.CONTINUATION_ESTABLISHED
					&& diagnostic.getCode()
							!= DiagnosticCode.DETERMINISTIC_SELECTION_ESTABLISHED) {
				diagnostics.add(diagnostic);
			}
		}
		return diagnostics;
	}

	private static ComponentAddress component(LocusDefinition2D definition,
			IntersectionRootRevisionEvidence2D evidence) {
		LocusBranch2D branch = definition.getBranch(
				evidence.getBranchSnapshotKey());
		if (branch == null) {
			return null;
		}
		for (int index = 0; index < branch.getValidDomainComponents().size();
				index++) {
			if (IntersectionCapabilityContext2D.componentKey(
					branch.getBranchKey(), index).equals(
							evidence.getResolvedValidComponentKey())) {
				LocusInterval2D interval =
						branch.getValidDomainComponents().get(index);
				return new ComponentAddress(
						IntersectionTokenLineage2D.stableComponentLineage(
								branch.getBranchKey(), interval),
						branch.getBranchKey(), interval, branch.getOrientation(),
						hasCompletePeriodicCycle(definition, branch, interval));
			}
		}
		return null;
	}

	private static boolean hasCompletePeriodicCycle(
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusInterval2D component) {
		return definition.getProvider().isPeriodic()
				&& branch.getProperties().contains(BranchProperty.PERIODIC)
				&& branch.getValidDomainComponents().size() == 1
				&& component.equals(branch.getDeclaredDriverDomain())
				&& component.equals(
						definition.getProvider().getDeclaredDomain());
	}

	private static IntersectionRootAddressProof2D addressProof(
			LocusDefinition2D definition, String targetContract,
			double semanticParameter) {
		return new IntersectionRootAddressProof2D(
				definition.getProvider().getSemanticSignature(), targetContract,
				semanticParameter);
	}

	private static String continuationContract(LocusDefinition2D definition,
			String targetContract) {
		LocusDriverDomainProvider2D provider = definition.getProvider();
		return framed(provider.getProviderId())
				+ framed(provider.getParameterDescriptor())
				+ framed(targetContract);
	}

	private static String framed(String value) {
		return value.length() + ":" + value;
	}

	private static final class ComponentAddress {
		private final String stableLineage;
		private final String branchKey;
		private final LocusInterval2D interval;
		private final Orientation orientation;
		private final boolean completePeriodicCycle;

		ComponentAddress(String stableLineage, String branchKey,
				LocusInterval2D interval, Orientation orientation,
				boolean completePeriodicCycle) {
			this.stableLineage = stableLineage;
			this.branchKey = branchKey;
			this.interval = interval;
			this.orientation = orientation;
			this.completePeriodicCycle = completePeriodicCycle;
		}
	}

	private static final class CurrentRoot {
		private final int solutionIndex;
		private final LocusIntersectionSolution2D solution;
		private final ComponentAddress component;
		private final String componentLineage;
		private final IntersectionRootDeterministicSelector2D selector;
		private final PhaseInterval phase;

		CurrentRoot(int solutionIndex, LocusIntersectionSolution2D solution,
				ComponentAddress component,
				IntersectionRootDeterministicSelector2D selector) {
			this(solutionIndex, solution, component, selector, null);
		}

		private CurrentRoot(int solutionIndex,
				LocusIntersectionSolution2D solution, ComponentAddress component,
				IntersectionRootDeterministicSelector2D selector,
				PhaseInterval phase) {
			this.solutionIndex = solutionIndex;
			this.solution = solution;
			this.component = component;
			this.componentLineage = component.stableLineage;
			this.selector = selector;
			this.phase = phase;
		}

		private CurrentRoot withPhaseInterval(PhaseInterval interval) {
			return new CurrentRoot(solutionIndex, solution, component, selector,
					interval);
		}

		private CurrentRoot withSelector(
				IntersectionRootDeterministicSelector2D selected) {
			return new CurrentRoot(solutionIndex, solution, component, selected,
					phase);
		}
	}

	private static final class PhaseInterval {
		private final double lower;
		private final double upper;

		private PhaseInterval(double lower, double upper) {
			this.lower = lower;
			this.upper = upper;
		}
	}

	private static final class ComponentKey {
		private final String branch;
		private final String component;

		ComponentKey(String branch, String component) {
			this.branch = branch;
			this.component = component;
		}

		static ComponentKey of(LocusIntersectionSolution2D solution) {
			IntersectionRootRevisionEvidence2D evidence =
					solution.getRevisionEvidence();
			return new ComponentKey(evidence.getBranchSnapshotKey(),
					evidence.getResolvedValidComponentKey());
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ComponentKey)) {
				return false;
			}
			ComponentKey key = (ComponentKey) other;
			return branch.equals(key.branch) && component.equals(key.component);
		}

		@Override
		public int hashCode() {
			return 31 * branch.hashCode() + component.hashCode();
		}
	}
}
