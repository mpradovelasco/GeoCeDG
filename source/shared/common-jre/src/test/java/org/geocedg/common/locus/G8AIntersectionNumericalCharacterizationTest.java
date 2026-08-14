/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.locus.G8AIntersectionNumerics.FactorizationProof;
import org.geocedg.common.locus.G8AIntersectionNumerics.ParameterInterval;
import org.geocedg.common.locus.G8AIntersectionNumerics.Problem;
import org.geocedg.common.locus.G8AIntersectionNumerics.RootProof;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Completeness;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.ComputationStatus;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.ContactClass;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.DomainLocation;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.GeometryKind;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.MultiplicityStatus;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Result;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.WorkBudget;
import org.geocedg.common.locus.G8ATargetAdapters.LineTarget;
import org.geogebra.common.BaseUnitTest;
import org.junit.jupiter.api.Test;

/** Root, tangency, completeness and tolerance characterization. */
class G8AIntersectionNumericalCharacterizationTest extends BaseUnitTest {

	@Test
	void factorizationProvesCompleteEmptyWithoutConflatingFailure() {
		Problem problem = parabolaHorizontal(-1, List.of(), "no-real-roots");
		Result result = G8AIntersectionNumerics.analyticFactorization(problem);
		assertEquals(ComputationStatus.SUCCESS, result.status());
		assertEquals(GeometryKind.EMPTY, result.geometryKind());
		assertEquals(Completeness.COMPLETE, result.completeness());
		assertEquals(1, result.work().completenessDomainsExcluded());
	}

	@Test
	void analyticFactorizationFindsAndVerifiesEverySimpleRoot() {
		Problem problem = parabolaHorizontal(1,
				List.of(new RootProof("left", -1, 1),
						new RootProof("right", 1, 1)), "t^2-1");
		Result result = G8AIntersectionNumerics.analyticFactorization(problem);
		assertEquals(GeometryKind.FINITE, result.geometryKind());
		assertEquals(Completeness.COMPLETE, result.completeness());
		assertEquals(2, result.solutions().size());
		assertEquals(2, result.work().residualVerificationCalls());
		assertTrue(result.pointProjectionAdmissible());
	}

	@Test
	void signChangingBracketsAreCertifiedForSimpleRootsOnly() {
		Problem problem = parabolaHorizontal(1,
				List.of(new RootProof("left", -1, 1),
						new RootProof("right", 1, 1)), "t^2-1");
		Result result = G8AIntersectionNumerics.certifiedBrackets(problem);
		assertEquals(Completeness.COMPLETE, result.completeness());
		assertEquals(NumericGuarantee.CERTIFIED_ERROR_BOUND,
				result.numericGuarantee());
		assertEquals(2, result.solutions().size());
		assertTrue(result.work().rootRefinementIterations() > 0);
	}

	@Test
	void evenTangencyIsFoundWithoutSignChangeAndBracketStrategyStaysHonest() {
		Problem tangent = parabolaHorizontal(0,
				List.of(new RootProof("tangent", 0, 2)), "t^2");
		Result bracketed = G8AIntersectionNumerics.certifiedBrackets(tangent);
		assertEquals(GeometryKind.UNRESOLVED, bracketed.geometryKind());
		assertEquals(Completeness.NOT_ESTABLISHED, bracketed.completeness());

		Result derivative = G8AIntersectionNumerics.derivativeAware(tangent);
		assertEquals(1, derivative.solutions().size());
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				derivative.solutions().get(0).classification().contactClass());
		assertEquals(2, derivative.solutions().get(0).classification()
				.establishedMultiplicity());
	}

	@Test
	void evaluatorOnlyMayVerifyTangencyButCannotInferCompleteness() {
		Problem tangent = parabolaHorizontal(0,
				List.of(new RootProof("tangent", 0, 2)), "t^2");
		Result result = G8AIntersectionNumerics.evaluatorOnly(tangent, 128);
		assertEquals(GeometryKind.FINITE, result.geometryKind());
		assertEquals(Completeness.NOT_ESTABLISHED, result.completeness());
		assertEquals(1, result.solutions().size());
		assertEquals(ContactClass.CONTACT_UNDETERMINED,
				result.solutions().get(0).classification().contactClass());
		assertEquals(MultiplicityStatus.NOT_ESTABLISHED,
				result.solutions().get(0).classification().multiplicityStatus());
	}

	@Test
	void fourthOrderRootRetainsEstablishedHigherMultiplicity() {
		G8AIntersectionFixtures.Fixture fixture = G8AIntersectionFixtures.create(
				getConstruction(), "quartic", -1, 1, true, true, false,
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, Math.pow(parameter, 4)),
				parameter -> new LocusPoint2D(1, 4 * Math.pow(parameter, 3)));
		Problem problem = fixture.problem(new LineTarget("axis", 0, 1, 0),
				new FactorizationProof(1,
						List.of(new RootProof("quartic-contact", 0, 4)), false,
						"t^4"), "topology-1");
		Result result = G8AIntersectionNumerics.derivativeAware(problem);
		assertEquals(4, result.solutions().get(0).classification()
				.establishedMultiplicity());
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				result.solutions().get(0).classification().contactClass());
	}

	@Test
	void nearTangencySweepDistinguishesTwoOneAndZeroRoots() {
		Result above = G8AIntersectionNumerics.analyticFactorization(
				parabolaHorizontal(1E-12,
						List.of(new RootProof("left", -1E-6, 1),
								new RootProof("right", 1E-6, 1)),
						"t^2-1e-12"));
		Result tangent = G8AIntersectionNumerics.analyticFactorization(
				parabolaHorizontal(0, List.of(new RootProof("tangent", 0, 2)),
						"t^2"));
		Result below = G8AIntersectionNumerics.analyticFactorization(
				parabolaHorizontal(-1E-12, List.of(), "t^2+1e-12"));
		assertEquals(2, above.solutions().size());
		assertEquals(1, tangent.solutions().size());
		assertEquals(GeometryKind.EMPTY, below.geometryKind());
	}

	@Test
	void equationScalingLeavesNormalizedAcceptanceAndRootSetInvariant() {
		Problem base = parabolaHorizontal(1,
				List.of(new RootProof("left", -1, 1),
						new RootProof("right", 1, 1)), "t^2-1");
		G8AIntersectionFixtures.Fixture fixture = G8AIntersectionFixtures.create(
				getConstruction(), "scaled-parabola", -2, 2, true, true, false,
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, parameter * parameter),
				parameter -> new LocusPoint2D(1, 2 * parameter));
		FactorizationProof scaledProof = new FactorizationProof(1E12,
				List.of(new RootProof("left", -1, 1),
						new RootProof("right", 1, 1)), false,
				"1e12*(t^2-1)");
		Problem scaled = fixture.problem(new LineTarget("scaled-line", 0, 1E12,
				-1E12), scaledProof, "topology-1");
		Result first = G8AIntersectionNumerics.analyticFactorization(base);
		Result second = G8AIntersectionNumerics.analyticFactorization(scaled);
		assertEquals(first.solutions().stream().map(solution -> solution
				.revisionEvidence().semanticParameter()).toList(),
				second.solutions().stream().map(solution -> solution
						.revisionEvidence().semanticParameter()).toList());
		assertTrue(second.solutions().stream().allMatch(solution ->
				solution.revisionEvidence().normalizedResidual() == 0));
	}

	@Test
	void semanticDedupDoesNotMergeCloseDistinctRoots() {
		double separation = 1E-7;
		G8AIntersectionFixtures.Fixture fixture = G8AIntersectionFixtures.create(
				getConstruction(), "close-roots", -1, 1, true, true, false,
				(source, branch, parameter, session) -> new LocusPoint2D(parameter,
						(parameter + separation / 2)
								* (parameter - separation / 2)),
				parameter -> new LocusPoint2D(1, 2 * parameter));
		Problem problem = fixture.problem(new LineTarget("axis", 0, 1, 0),
				new FactorizationProof(1,
						List.of(new RootProof("left", -separation / 2, 1),
								new RootProof("right", separation / 2, 1)), false,
						"clustered pair"), "topology-1");
		Result result = G8AIntersectionNumerics.analyticFactorization(problem);
		assertEquals(2, result.solutions().size());
		assertTrue(separation > problem.policy()
				.deduplicationParameterTolerance());
	}

	@Test
	void includedAndExcludedSemanticEndpointsHaveDifferentSetSemantics() {
		G8AIntersectionFixtures.Fixture included = G8AIntersectionFixtures.create(
				getConstruction(), "included-endpoint", 0, 1, true, true, false,
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, parameter),
				parameter -> new LocusPoint2D(1, 1));
		FactorizationProof proof = new FactorizationProof(1,
				List.of(new RootProof("start", 0, 1)), false, "t");
		Result atIncluded = G8AIntersectionNumerics.analyticFactorization(
				included.problem(new LineTarget("axis", 0, 1, 0), proof,
						"topology-1"));
		assertEquals(DomainLocation.INCLUDED_ENDPOINT,
				atIncluded.solutions().get(0).classification().domainLocation());

		G8AIntersectionFixtures.Fixture excluded = G8AIntersectionFixtures.create(
				getConstruction(), "excluded-endpoint", 0, 1, false, true, false,
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, parameter),
				parameter -> new LocusPoint2D(1, 1));
		Result atExcluded = G8AIntersectionNumerics.analyticFactorization(
				excluded.problem(new LineTarget("axis-2", 0, 1, 0), proof,
						"topology-1"));
		assertEquals(GeometryKind.EMPTY, atExcluded.geometryKind());
	}

	@Test
	void deliberatelyIncompleteBroadPhaseKeepsVerifiedSubsetExplicit() {
		Problem problem = parabolaHorizontal(1,
				List.of(new RootProof("left", -1, 1),
						new RootProof("right", 1, 1)), "t^2-1");
		Result result = G8AIntersectionNumerics.conservativeBroadPhase(problem,
				List.of(new ParameterInterval(-1.5, -0.5)));
		assertEquals(Completeness.INCOMPLETE, result.completeness());
		assertEquals(1, result.solutions().size());
		assertEquals(1, result.work().unresolvedCandidates());
		assertFalse(result.pointProjectionAdmissible());
	}

	@Test
	void workExhaustionIsTypedAndPublishesNoPartialCompleteSet() {
		Problem original = parabolaHorizontal(1,
				List.of(new RootProof("left", -1, 1),
						new RootProof("right", 1, 1)), "t^2-1");
		WorkBudget tiny = new WorkBudget(8, 8, 8, 8, 4, 4, 4, 4, 4, 4,
				4, 0, 1);
		Problem limited = new Problem(original.sourcePairIdentity(),
				original.locusIdentity(), original.locusRevision(),
				original.targetIdentity(), original.targetUpdateStamp(),
				original.branchKey(), original.branchLineage(),
				original.componentKey(), original.topologyContext(), original.lower(),
				original.upper(), original.lowerIncluded(), original.upperIncluded(),
				original.periodic(), original.curve(), original.target(),
				original.proof(), original.policy(), tiny);
		Result result = G8AIntersectionNumerics.evaluatorOnly(limited, 128);
		assertEquals(ComputationStatus.WORK_LIMIT_REACHED, result.status());
		assertEquals(Completeness.NOT_ESTABLISHED, result.completeness());
		assertEquals(GeometryKind.UNRESOLVED, result.geometryKind());
		assertEquals(0, result.work().partialSnapshotsPublished());
	}

	@Test
	void evaluatorOnlyUnboundedDomainCannotEstablishCompleteness() {
		Problem bounded = parabolaHorizontal(1,
				List.of(new RootProof("left", -1, 1),
						new RootProof("right", 1, 1)), "t^2-1");
		Problem unbounded = new Problem(bounded.sourcePairIdentity(),
				bounded.locusIdentity(), bounded.locusRevision(),
				bounded.targetIdentity(), bounded.targetUpdateStamp(),
				bounded.branchKey(), bounded.branchLineage(), bounded.componentKey(),
				bounded.topologyContext(), Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, false, false, false, bounded.curve(),
				bounded.target(), bounded.proof(), bounded.policy(), bounded.budget());
		Result result = G8AIntersectionNumerics.evaluatorOnly(unbounded, 128);
		assertEquals(Completeness.NOT_ESTABLISHED, result.completeness());
		assertEquals(GeometryKind.UNRESOLVED, result.geometryKind());
		assertTrue(result.solutions().isEmpty());
	}

	@Test
	void nonfiniteSemanticEvaluationCannotBecomeFalseEmptyOrStaleSuccess() {
		G8AIntersectionFixtures.Fixture fixture = G8AIntersectionFixtures.create(
				getConstruction(), "nonfinite", -1, 1, true, true, false,
				(source, branch, parameter, session) ->
						new LocusPoint2D(Double.NaN, parameter), null);
		Problem problem = fixture.problem(new LineTarget("axis", 1, 0, 0),
				new FactorizationProof(1, List.of(new RootProof("bad", 0, 1)),
						false, "declared root"), "topology-1");
		Result result = G8AIntersectionNumerics.analyticFactorization(problem);
		assertEquals(ComputationStatus.NUMERICAL_FAILURE, result.status());
		assertEquals(Completeness.INCOMPLETE, result.completeness());
		assertEquals(GeometryKind.UNRESOLVED, result.geometryKind());
	}

	@Test
	void candidateToleranceQuantitiesRemainDistinctAndVersioned() {
		Problem problem = parabolaHorizontal(1,
				List.of(new RootProof("left", -1, 1),
						new RootProof("right", 1, 1)), "t^2-1");
		assertEquals("g8a-measured-candidate/v1", problem.policy().policyId());
		assertTrue(problem.policy().rootParameterTolerance()
				!= problem.policy().tangencyThreshold());
		assertTrue(problem.policy().deduplicationParameterTolerance()
				!= problem.policy().continuationParameterTolerance());
		assertTrue(problem.policy().absoluteResidualTolerance()
				<= problem.policy().relativeResidualTolerance());
	}

	private Problem parabolaHorizontal(double height, List<RootProof> roots,
			String proofMethod) {
		G8AIntersectionFixtures.Fixture fixture = G8AIntersectionFixtures.create(
				getConstruction(), "numeric-parabola-" + height + "-" + proofMethod,
				-2, 2, true, true, false,
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, parameter * parameter),
				parameter -> new LocusPoint2D(1, 2 * parameter));
		return fixture.problem(new LineTarget("horizontal-" + height, 0, 1,
				-height), new FactorizationProof(1, roots, false, proofMethod),
				"topology-1");
	}
}
