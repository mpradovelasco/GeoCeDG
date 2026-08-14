/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.locus.G8AIntersectionIdentityExperiment.Observation;
import org.geocedg.common.locus.G8AIntersectionIdentityExperiment.Step;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.IdentityStatus;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.LineageTransition;
import org.junit.jupiter.api.Test;

/** Dynamic identity, reparameterization, seam and genealogy traces. */
class G8AIntersectionIdentityCharacterizationTest {
	private static final double EPS_CONTINUATION = 1E-8;

	@Test
	void ordinaryContinuousMotionPreservesTokenThroughSemanticPrediction() {
		G8AIntersectionIdentityExperiment engine =
				new G8AIntersectionIdentityExperiment();
		Step first = engine.initial(List.of(observation("root", "branch", 0.2,
				0.19, 0.21, 1, 1)));
		Step second = engine.continueRegular(first,
				List.of(observation("root", "branch", 0.3, 0.29, 0.31, 2, 1)),
				value -> value + 0.1, EPS_CONTINUATION);
		assertEquals(first.roots().get(0).token(), second.roots().get(0).token());
		assertEquals(IdentityStatus.CONTINUATION_ESTABLISHED,
				second.roots().get(0).identityStatus());
		assertEquals(1, second.counters().identityContinuationsAccepted);
	}

	@Test
	void equivalentMonotoneReparameterizationPreservesDurableToken() {
		G8AIntersectionIdentityExperiment engine =
				new G8AIntersectionIdentityExperiment();
		Step original = engine.initial(List.of(observation("root", "branch", 0.6,
				0.59, 0.61, 1, 1)));
		Step reparameterized = engine.continueRegular(original,
				List.of(observation("root", "branch", 0.3, 0.29, 0.31, 2, 1)),
				value -> value / 2, EPS_CONTINUATION);
		assertEquals(original.roots().get(0).token(),
				reparameterized.roots().get(0).token());
		assertNotEquals(original.roots().get(0).observation().isolatingLower(),
				reparameterized.roots().get(0).observation().isolatingLower());
		assertEquals(1,
				reparameterized.counters().reparameterizationMappingsChecked);
	}

	@Test
	void derivativeDegenerateMonotoneMapNeedsExplicitRelationButNotCoordinates() {
		G8AIntersectionIdentityExperiment engine =
				new G8AIntersectionIdentityExperiment();
		Step original = engine.initial(List.of(observation("origin", "branch", 0,
				-1E-4, 1E-4, 1, 1)));
		Step cubic = engine.continueRegular(original,
				List.of(observation("origin", "branch", 0, -1E-2, 1E-2, 2, 1)),
				value -> Math.cbrt(value), EPS_CONTINUATION);
		assertEquals(original.roots().get(0).token(), cubic.roots().get(0).token());
		assertEquals(IdentityStatus.CONTINUATION_ESTABLISHED,
				cubic.roots().get(0).identityStatus());
	}

	@Test
	void allowedOrientationReversalUsesKnownParameterMap() {
		G8AIntersectionIdentityExperiment engine =
				new G8AIntersectionIdentityExperiment();
		Step forward = engine.initial(List.of(observation("root", "branch", 0.4,
				0.39, 0.41, 1, 1)));
		Step reversed = engine.continueRegular(forward,
				List.of(observation("root", "branch", -0.4, -0.41, -0.39, 2, 1)),
				value -> -value, EPS_CONTINUATION);
		assertEquals(forward.roots().get(0).token(), reversed.roots().get(0).token());
	}

	@Test
	void isolatingIntervalIsRevisionEvidenceAndNeverFundamentalIdentity() {
		G8AIntersectionIdentityExperiment engine =
				new G8AIntersectionIdentityExperiment();
		Step coarse = engine.initial(List.of(observation("root", "branch", 0.25,
				0.2, 0.3, 1, 1)));
		Step fine = engine.continueRegular(coarse,
				List.of(observation("root", "branch", 0.25, 0.249, 0.251, 2, 1)),
				value -> value, EPS_CONTINUATION);
		assertEquals(coarse.roots().get(0).token(), fine.roots().get(0).token());
		assertNotEquals(coarse.roots().get(0).observation().isolatingLower(),
				fine.roots().get(0).observation().isolatingLower());
	}

	@Test
	void periodicSeamUsesLiftedContinuationNotCanonicalNearestCoordinate() {
		G8AIntersectionIdentityExperiment engine =
				new G8AIntersectionIdentityExperiment();
		double before = Math.PI - 0.01;
		double afterCanonical = -Math.PI + 0.01;
		double afterLifted = Math.PI + 0.01;
		Step first = engine.initial(List.of(observationWithLift("seam-root",
				"periodic-branch", before, before, before - 0.001,
				before + 0.001, 1, 1)));
		Step second = engine.continueRegular(first,
				List.of(observationWithLift("seam-root", "periodic-branch",
						afterCanonical, afterLifted, afterCanonical - 0.001,
						afterCanonical + 0.001, 2, 1)),
				value -> value + 0.02, EPS_CONTINUATION);
		assertEquals(first.roots().get(0).token(), second.roots().get(0).token());
		assertTrue(Math.abs(first.roots().get(0).observation().semanticParameter()
				- second.roots().get(0).observation().semanticParameter()) > 6);
	}

	@Test
	void forwardTwoToOneToTwoTraceCreatesCandidateGenealogyNotUniversalIdentity() {
		G8AIntersectionIdentityExperiment engine =
				new G8AIntersectionIdentityExperiment();
		Step two = engine.initial(List.of(
				observation("left", "branch", -0.1, -0.11, -0.09, 1, 1),
				observation("right", "branch", 0.1, 0.09, 0.11, 1, 1)));
		Step one = engine.mergeCandidate(two,
				observation("tangent", "branch", 0, -1E-5, 1E-5, 2, 2));
		Step split = engine.splitCandidate(one,
				List.of(observation("left", "branch", -0.1, -0.11, -0.09, 3, 3),
						observation("right", "branch", 0.1, 0.09, 0.11, 3, 3)),
				true);
		assertEquals(LineageTransition.MERGE_CANDIDATE,
				one.roots().get(0).transition());
		assertEquals(2, one.roots().get(0).parentTokens().size());
		assertTrue(split.roots().stream().allMatch(root ->
				root.identityStatus() == IdentityStatus.AMBIGUOUS_CONTINUATION));
		assertTrue(split.ambiguityEvent());
	}

	@Test
	void reverseTraversalDoesNotFabricateRestorationOfOldTokens() {
		G8AIntersectionIdentityExperiment engine =
				new G8AIntersectionIdentityExperiment();
		Step original = engine.initial(List.of(
				observation("left", "branch", -0.2, -0.21, -0.19, 1, 1),
				observation("right", "branch", 0.2, 0.19, 0.21, 1, 1)));
		Step merged = engine.mergeCandidate(original,
				observation("tangent", "branch", 0, -1E-5, 1E-5, 2, 2));
		Step split = engine.splitCandidate(merged, List.of(
				observation("left", "branch", -0.2, -0.21, -0.19, 3, 3),
				observation("right", "branch", 0.2, 0.19, 0.21, 3, 3)), true);
		Step reverseMerge = engine.mergeCandidate(split,
				observation("tangent", "branch", 0, -1E-5, 1E-5, 4, 4));
		Step returned = engine.splitCandidate(reverseMerge, List.of(
				observation("left", "branch", -0.2, -0.21, -0.19, 5, 5),
				observation("right", "branch", 0.2, 0.19, 0.21, 5, 5)), true);
		for (int index = 0; index < 2; index++) {
			assertNotEquals(original.roots().get(index).token(),
					split.roots().get(index).token());
			assertNotEquals(original.roots().get(index).token(),
					returned.roots().get(index).token());
			assertEquals(IdentityStatus.AMBIGUOUS_CONTINUATION,
					returned.roots().get(index).identityStatus());
		}
		assertEquals(LineageTransition.MERGE_CANDIDATE,
				reverseMerge.roots().get(0).transition());
	}

	@Test
	void symmetricDescendantsAreExplicitlyAmbiguous() {
		G8AIntersectionIdentityExperiment engine =
				new G8AIntersectionIdentityExperiment();
		Step tangent = engine.initial(List.of(observation("tangent", "branch", 0,
				-1E-5, 1E-5, 1, 1)));
		Step children = engine.splitCandidate(tangent, List.of(
				observation("child-a", "branch", -0.1, -0.11, -0.09, 2, 2),
				observation("child-b", "branch", 0.1, 0.09, 0.11, 2, 2)), true);
		assertEquals(2, children.counters().identityContinuationsAmbiguous);
		assertFalse(children.roots().get(0).continuationDeterministic());
		assertFalse(children.roots().get(1).continuationDeterministic());
	}

	@Test
	void mergeAtPeriodicSeamStillAllocatesEventTokenAndNoDuplicateParent() {
		G8AIntersectionIdentityExperiment engine =
				new G8AIntersectionIdentityExperiment();
		Step parents = engine.initial(List.of(
				observationWithLift("left", "periodic", Math.PI - 0.02,
						Math.PI - 0.02, Math.PI - 0.03, Math.PI - 0.01, 1, 1),
				observationWithLift("right", "periodic", -Math.PI + 0.02,
						Math.PI + 0.02, -Math.PI + 0.01, -Math.PI + 0.03, 1, 1)));
		Step merged = engine.mergeCandidate(parents,
				observationWithLift("seam-tangent", "periodic", -Math.PI,
						Math.PI, -Math.PI, -Math.PI, 2, 2));
		assertEquals(2, merged.roots().get(0).parentTokens().stream().distinct()
				.count());
		assertNotEquals(parents.roots().get(0).token(),
				merged.roots().get(0).token());
	}

	@Test
	void branchReplacementNearMergeTerminatesRatherThanGuessing() {
		G8AIntersectionIdentityExperiment engine =
				new G8AIntersectionIdentityExperiment();
		Step before = engine.initial(List.of(
				observation("left", "old-branch", -0.01, -0.02, 0, 1, 1),
				observation("right", "old-branch", 0.01, 0, 0.02, 1, 1)));
		Step terminated = engine.terminate(before, 2,
				"branch lineage replacement has no unique continuation");
		Step after = engine.initial(List.of(observation("tangent", "new-branch", 0,
				-1E-5, 1E-5, 2, 2)));
		assertTrue(terminated.roots().isEmpty());
		assertEquals(2, terminated.counters().topologyTerminationEvents);
		assertTrue(after.roots().stream().allMatch(root ->
				root.identityStatus() == IdentityStatus.NEW_TOPOLOGICAL_SOLUTION));
	}

	@Test
	void ambiguousSameConstructiveKeyNeverFallsBackToCoordinatesOrSlot() {
		G8AIntersectionIdentityExperiment engine =
				new G8AIntersectionIdentityExperiment();
		Step previous = engine.initial(List.of(
				observation("same-key", "branch", -0.1, -0.2, 0, 1, 1),
				observation("same-key", "branch", 0.1, 0, 0.2, 1, 1)));
		Step current = engine.continueRegular(previous,
				List.of(observation("same-key", "branch", 0, -0.01, 0.01, 2, 1)),
				value -> 0, EPS_CONTINUATION);
		assertEquals(IdentityStatus.AMBIGUOUS_CONTINUATION,
				current.roots().get(0).identityStatus());
		assertEquals(2, current.roots().get(0).parentTokens().size());
		assertTrue(current.ambiguityEvent());
	}

	private static Observation observation(String key, String branch,
			double parameter, double lower, double upper, long revision,
			long epoch) {
		return observationWithLift(key, branch, parameter, parameter, lower, upper,
				revision, epoch);
	}

	private static Observation observationWithLift(String key, String branch,
			double parameter, double lifted, double lower, double upper,
			long revision, long epoch) {
		return new Observation(key, branch, parameter, lifted, lower, upper,
				revision, epoch);
	}
}
