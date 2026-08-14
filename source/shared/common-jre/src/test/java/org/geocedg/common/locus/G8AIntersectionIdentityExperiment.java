/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleUnaryOperator;

import org.geocedg.common.locus.G8AIntersectionSemanticModel.IdentityStatus;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.LineageTransition;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.WorkCounters;

/** Test-private continuation and topology-event hypothesis. */
final class G8AIntersectionIdentityExperiment {
	private final AtomicInteger tokenSequence = new AtomicInteger();

	record Observation(String constructiveRootKey, String branchLineage,
			double semanticParameter, double liftedParameter,
			double isolatingLower, double isolatingUpper, long sourceRevision,
			long topologyEpoch) {
		Observation {
			Objects.requireNonNull(constructiveRootKey);
			Objects.requireNonNull(branchLineage);
			if (!Double.isFinite(semanticParameter)
					|| !Double.isFinite(liftedParameter)
					|| !Double.isFinite(isolatingLower)
					|| !Double.isFinite(isolatingUpper)
					|| isolatingLower > semanticParameter
					|| semanticParameter > isolatingUpper
					|| sourceRevision < 1 || topologyEpoch < 1) {
				throw new IllegalArgumentException("Invalid root observation");
			}
		}
	}

	record TrackedRoot(String token, Observation observation,
			IdentityStatus identityStatus, LineageTransition transition,
			List<String> parentTokens, boolean continuationDeterministic,
			String diagnostic) {
		TrackedRoot {
			Objects.requireNonNull(token);
			Objects.requireNonNull(observation);
			Objects.requireNonNull(identityStatus);
			Objects.requireNonNull(transition);
			parentTokens = List.copyOf(parentTokens);
			Objects.requireNonNull(diagnostic);
		}
	}

	record Step(List<TrackedRoot> roots, long topologyEpoch,
			boolean ambiguityEvent, WorkCounters counters) {
		Step {
			roots = List.copyOf(roots);
		}
	}

	Step initial(List<Observation> observations) {
		WorkCounters counters = new WorkCounters();
		List<TrackedRoot> roots = observations.stream().map(observation ->
				new TrackedRoot(newToken(), observation,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION,
						LineageTransition.APPEARED, List.of(), true,
						"initial constructive solution")).toList();
		return new Step(roots, observations.isEmpty() ? 1
				: observations.get(0).topologyEpoch(), false, counters);
	}

	Step continueRegular(Step previous, List<Observation> current,
			DoubleUnaryOperator oldLiftedToNewLifted,
			double continuationTolerance) {
		WorkCounters counters = new WorkCounters();
		List<TrackedRoot> result = new ArrayList<>();
		boolean ambiguity = false;
		for (Observation observation : current) {
			counters.identityContinuationPredictions++;
			counters.reparameterizationMappingsChecked++;
			List<TrackedRoot> candidates = previous.roots().stream()
					.filter(root -> root.observation().constructiveRootKey()
							.equals(observation.constructiveRootKey()))
					.filter(root -> root.observation().branchLineage()
							.equals(observation.branchLineage()))
					.toList();
			counters.identityContinuationComparisons += candidates.size();
			List<TrackedRoot> admissible = candidates.stream().filter(root ->
					Math.abs(oldLiftedToNewLifted.applyAsDouble(
							root.observation().liftedParameter())
							- observation.liftedParameter())
							<= continuationTolerance).toList();
			if (admissible.size() == 1) {
				TrackedRoot parent = admissible.get(0);
				counters.identityContinuationsAccepted++;
				counters.reparameterizationContinuationsAccepted++;
				result.add(new TrackedRoot(parent.token(), observation,
						IdentityStatus.CONTINUATION_ESTABLISHED,
						LineageTransition.UNCHANGED, List.of(parent.token()), true,
						"explicit semantic continuation map"));
			} else if (admissible.size() > 1) {
				ambiguity = true;
				counters.identityContinuationsAmbiguous++;
				result.add(new TrackedRoot(newToken(), observation,
						IdentityStatus.AMBIGUOUS_CONTINUATION,
						LineageTransition.AMBIGUOUS_EVENT,
						admissible.stream().map(TrackedRoot::token).toList(), false,
						"several semantic continuations are equally admissible"));
			} else {
				counters.identityContinuationsNotEstablished++;
				result.add(new TrackedRoot(newToken(), observation,
						IdentityStatus.NOT_ESTABLISHED,
						LineageTransition.APPEARED, List.of(), false,
						"no semantic continuation relation established"));
			}
		}
		long epoch = current.isEmpty() ? previous.topologyEpoch()
				: current.get(0).topologyEpoch();
		return new Step(result, epoch, ambiguity, counters);
	}

	Step mergeCandidate(Step previous, Observation merged) {
		WorkCounters counters = new WorkCounters();
		counters.topologyMergeEvents++;
		counters.identityContinuationComparisons += previous.roots().size();
		List<String> parents = previous.roots().stream().map(TrackedRoot::token)
				.toList();
		TrackedRoot root = new TrackedRoot(newToken(), merged,
				IdentityStatus.IDENTITY_DISCONTINUITY,
				LineageTransition.MERGE_CANDIDATE, parents, false,
				"merge genealogy is recorded as a G8A candidate, not identity proof");
		return new Step(List.of(root), merged.topologyEpoch(), false, counters);
	}

	Step splitCandidate(Step previous, List<Observation> children,
			boolean symmetricAmbiguity) {
		WorkCounters counters = new WorkCounters();
		counters.topologySplitEvents++;
		List<String> parents = previous.roots().stream().map(TrackedRoot::token)
				.toList();
		List<TrackedRoot> result = new ArrayList<>();
		for (Observation child : children) {
			if (symmetricAmbiguity) {
				counters.identityContinuationsAmbiguous++;
			}
			result.add(new TrackedRoot(newToken(), child,
					symmetricAmbiguity ? IdentityStatus.AMBIGUOUS_CONTINUATION
							: IdentityStatus.IDENTITY_DISCONTINUITY,
					LineageTransition.SPLIT_CANDIDATE, parents, false,
					symmetricAmbiguity
							? "symmetric child correspondence is intrinsically ambiguous"
							: "split candidate creates new tokens at topology epoch"));
		}
		long epoch = children.isEmpty() ? previous.topologyEpoch()
				: children.get(0).topologyEpoch();
		return new Step(result, epoch, symmetricAmbiguity, counters);
	}

	Step terminate(Step previous, long nextTopologyEpoch, String diagnostic) {
		WorkCounters counters = new WorkCounters();
		counters.topologyTerminationEvents += previous.roots().size();
		return new Step(List.of(), nextTopologyEpoch, false, counters);
	}

	private String newToken() {
		return "g8a-root-token-" + tokenSequence.incrementAndGet();
	}
}
