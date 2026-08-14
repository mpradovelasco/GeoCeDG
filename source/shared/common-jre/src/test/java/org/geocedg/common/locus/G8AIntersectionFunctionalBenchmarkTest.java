/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.locus.G8AIntersectionNumerics.FactorizationProof;
import org.geocedg.common.locus.G8AIntersectionNumerics.Problem;
import org.geocedg.common.locus.G8AIntersectionNumerics.RootProof;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Completeness;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.GeometryKind;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Result;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.WorkSnapshot;
import org.geocedg.common.locus.G8ATargetAdapters.ConicTarget;
import org.geocedg.common.locus.G8ATargetAdapters.LineTarget;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/** Deterministic G8A work, scientific-pilot, and state characterization. */
class G8AIntersectionFunctionalBenchmarkTest extends BaseUnitTest {
	private static final String NESTED_BRANCH = "g8a.nested.main";

	@Test
	void queryLocalAnalyticWorkScalesExactlyForOneThreeTenAndHundredConsumers() {
		Problem problem = lineProblem("consumer-scale");
		for (int consumers : new int[] {1, 3, 10, 100}) {
			long semanticCalls = 0;
			long verifications = 0;
			for (int consumer = 0; consumer < consumers; consumer++) {
				Result result = G8AIntersectionNumerics
						.analyticFactorization(problem);
				assertEquals(Completeness.COMPLETE, result.completeness());
				semanticCalls += result.work().semanticEvaluatorCalls();
				verifications += result.work().residualVerificationCalls();
				assertHardZero(result.work());
			}
			assertEquals(consumers, semanticCalls);
			assertEquals(consumers, verifications);
			System.out.println("G8A_METRIC case=QUERY-LOCAL-" + consumers
					+ " consumers=" + consumers
					+ " semantic_evaluations=" + semanticCalls
					+ " residual_verifications=" + verifications
					+ " retained_entries=0");
		}
	}

	@Test
	void evaluatorOnlyBuildsQueryLocalStateAndRetainsNothing() {
		Problem problem = lineProblem("evaluator-query-local");
		for (int consumers : new int[] {1, 3, 10, 100}) {
			long builds = 0;
			long retained = 0;
			long sharedBuilds = 0;
			for (int consumer = 0; consumer < consumers; consumer++) {
				Result result = G8AIntersectionNumerics.evaluatorOnly(problem, 64);
				assertEquals(Completeness.NOT_ESTABLISHED,
						result.completeness());
				builds += result.work().queryLocalIndexBuilds();
				retained += result.work().retainedIndexEntries();
				sharedBuilds += result.work().sharedIndexBuilds();
				assertHardZero(result.work());
			}
			assertEquals(consumers, builds);
			assertEquals(0, retained);
			assertEquals(0, sharedBuilds);
		}
	}

	@Test
	void repeatedQueryIsDeterministicWithoutCrossQueryOwner() {
		Problem problem = lineProblem("repeat-equality");
		Result first = G8AIntersectionNumerics.analyticFactorization(problem);
		Result second = G8AIntersectionNumerics.analyticFactorization(problem);
		assertEquals(first, second);
		assertEquals(0, first.work().retainedIndexEntries());
		assertEquals(0, second.work().indexHits());
		assertHardZero(first.work());
	}

	@Test
	void nestedDepthOneTwoThreeHasQueriesTimesDepthSemanticCost() {
		for (int depth : new int[] {1, 2, 3}) {
			NestedChain chain = nestedChain(depth);
			Problem problem = nestedProblem(chain, depth);
			int consumers = 10;
			reset(chain);
			for (int consumer = 0; consumer < consumers; consumer++) {
				Result result = G8AIntersectionNumerics
						.analyticFactorization(problem);
				assertEquals(GeometryKind.FINITE, result.geometryKind());
				assertHardZero(result.work());
			}
			assertEquals((long) consumers * depth, evaluatorCalls(chain));
			assertEquals(0, forbiddenLocusWork(chain));
			System.out.println("G8A_METRIC case=NESTED-" + depth
					+ " consumers=" + consumers + " evaluator_calls="
					+ evaluatorCalls(chain) + " expected="
					+ consumers * depth + " whole_locus_regenerations=0");
		}
	}

	@Test
	void innermostNestedRevisionInvalidatesAndRecoversDeterministically() {
		NestedChain chain = nestedChain(3);
		long[] before = revisions(chain);
		Result first = G8AIntersectionNumerics.analyticFactorization(
				nestedProblem(chain, 3));
		chain.source().setValue(7);
		chain.source().updateCascade();
		long[] after = revisions(chain);
		for (int index = 0; index < before.length; index++) {
			assertEquals(before[index] + 1, after[index]);
		}
		Result second = G8AIntersectionNumerics.analyticFactorization(
				nestedProblem(chain, 3));
		assertNotEquals(first.sourceBinding().locusRevision(),
				second.sourceBinding().locusRevision());
		assertEquals(first.solutions().get(0).point(),
				second.solutions().get(0).point());
		assertEquals(0, forbiddenLocusWork(chain));
		assertHardZero(second.work());
	}

	@Test
	void reducedFocalSphereConePilotCoversSecantTangentAndEmpty() {
		GeoConic circle = add("Circle((0,0),1)");
		ConicTarget target = new ConicTarget("focal-separatrix", circle, true);
		Result secant = focalResult("focal-secant", 0.6, target);
		Result tangent = focalResult("focal-tangent", 1, target);
		Result empty = focalResult("focal-empty", 1.2, target);
		assertEquals(2, secant.solutions().size());
		assertEquals(GeometryKind.FINITE, tangent.geometryKind());
		assertEquals(2, tangent.solutions().get(0).classification()
				.establishedMultiplicity());
		assertEquals(GeometryKind.EMPTY, empty.geometryKind());
		assertEquals(Completeness.COMPLETE, empty.completeness());
		assertHardZero(secant.work());
		assertHardZero(tangent.work());
		assertHardZero(empty.work());
	}

	@Test
	void reducedConeCylinderPilotPreservesFourLeavesAndRepeatedPreimages() {
		List<Result> penetration = reducedLsimResults(0.25);
		List<Result> tangent = reducedLsimResults(0);
		List<Result> separated = reducedLsimResults(-0.25);
		assertEquals(4, verifiedRoots(penetration));
		assertEquals(2, verifiedRoots(tangent));
		assertEquals(0, verifiedRoots(separated));
		assertEquals(tangent.get(0).solutions().get(0).point(),
				tangent.get(1).solutions().get(0).point());
		assertNotEquals(tangent.get(0).solutions().get(0).branchKey(),
				tangent.get(1).solutions().get(0).branchKey());
		assertNotEquals(tangent.get(0).solutions().get(0).durableIdentity()
				.rootToken(), tangent.get(1).solutions().get(0).durableIdentity()
				.rootToken());
		for (Result result : concat(penetration, tangent, separated)) {
			assertEquals(Completeness.COMPLETE, result.completeness());
			assertHardZero(result.work());
		}
	}

	@Test
	void workExhaustionIsTypedAndDoesNotPublishPartialFiniteSet() {
		Problem problem = lineProblem("bounded-failure");
		Result exhausted = G8AIntersectionNumerics.evaluatorOnly(problem,
				problem.budget().maximumIsolationSubdivisions() + 1);
		assertEquals(Completeness.NOT_ESTABLISHED, exhausted.completeness());
		assertEquals(GeometryKind.UNRESOLVED, exhausted.geometryKind());
		assertTrue(exhausted.solutions().isEmpty());
		assertEquals(1, exhausted.work().failedPrivateComputations());
		assertEquals(0, exhausted.work().partialSnapshotsPublished());
		assertHardZero(exhausted.work());
	}

	private Problem lineProblem(String identity) {
		G8AIntersectionFixtures.Fixture fixture = G8AIntersectionFixtures.create(
				getConstruction(), identity, -1, 1, true, true, false,
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, parameter),
				parameter -> new LocusPoint2D(1, 1));
		LineTarget target = new LineTarget(identity + "/line", 0, 1, 0);
		FactorizationProof proof = new FactorizationProof(1,
				List.of(new RootProof("root-zero", 0, 1)), false, "h(t)=t");
		return fixture.problem(target, proof, "topology-1");
	}

	private Result focalResult(String identity, double height,
			ConicTarget target) {
		G8AIntersectionFixtures.Fixture fixture = G8AIntersectionFixtures.create(
				getConstruction(), identity, -2, 2, true, true, false,
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, source),
				parameter -> new LocusPoint2D(1, 0));
		fixture.source().setValue(height);
		fixture.source().updateCascade();
		double discriminant = 1 - height * height;
		List<RootProof> roots;
		if (discriminant > 0) {
			double root = Math.sqrt(discriminant);
			roots = List.of(new RootProof("left", -root, 1),
					new RootProof("right", root, 1));
		} else if (discriminant == 0) {
			roots = List.of(new RootProof("tangent", 0, 2));
		} else {
			roots = List.of();
		}
		FactorizationProof proof = new FactorizationProof(1, roots, false,
				"h(t)=t^2+height^2-1; analytic discriminant");
		return G8AIntersectionNumerics.analyticFactorization(
				fixture.problem(target, proof, "focal-topology"));
	}

	private List<Result> reducedLsimResults(double topologyParameter) {
		List<Result> results = new ArrayList<>();
		LineTarget target = new LineTarget("lsim-section-line", 0, 1, 0);
		for (int sign : new int[] {-1, 1}) {
			String identity = "lsim-branch-" + sign + "-" + topologyParameter;
			G8AIntersectionFixtures.Fixture fixture =
					G8AIntersectionFixtures.create(getConstruction(), identity,
							-1, 1, true, true, false,
							(source, branch, parameter, session) ->
									new LocusPoint2D(parameter,
											sign * (parameter * parameter - source)),
							parameter -> new LocusPoint2D(1,
									2 * sign * parameter));
			fixture.source().setValue(topologyParameter);
			fixture.source().updateCascade();
			List<RootProof> roots;
			if (topologyParameter > 0) {
				double root = Math.sqrt(topologyParameter);
				roots = List.of(new RootProof("negative-leaf", -root, 1),
						new RootProof("positive-leaf", root, 1));
			} else if (topologyParameter == 0) {
				roots = List.of(new RootProof("tangent-leaf", 0, 2));
			} else {
				roots = List.of();
			}
			FactorizationProof proof = new FactorizationProof(sign, roots, false,
					"h_branch(t)=sign*(t^2-topologyParameter)");
			results.add(G8AIntersectionNumerics.analyticFactorization(
					fixture.problem(target, proof, "lsim-topology")));
		}
		return results;
	}

	private NestedChain nestedChain(int depth) {
		ExplicitNumericDomainProvider2D provider =
				new ExplicitNumericDomainProvider2D("g8a-nested-parameter/v1",
						new LocusInterval2D(-1, 1, true, true),
						Orientation.INCREASING, false, 1E-14);
		LocusBranch2D branch = LocusV2Factory.fullDomainBranch(NESTED_BRANCH,
				provider, "g8a-nested-lineage/v1",
				EnumSet.noneOf(BranchProperty.class));
		List<LocusBranch2D> branches = Collections.singletonList(branch);
		GeoNumeric source = new GeoNumeric(getConstruction(), 1);
		List<GeoLocusV2> loci = new ArrayList<>();
		GeoLocusV2 current = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "g8a-nested-L1-" + depth + "-"
						+ source.hashCode(), source, provider, branches,
				(value, semanticBranch, parameter, session) ->
						new LocusPoint2D(parameter, parameter),
				"g8a-nested-leaf/v1");
		loci.add(current);
		for (int level = 2; level <= depth; level++) {
			current = LocusV2Factory.createNested(LocusV2Mode.V2,
					getConstruction(), "g8a-nested-L" + level + "-" + depth
							+ "-" + source.hashCode(), current, NESTED_BRANCH,
					provider, branches, parameter -> parameter,
					(parameter, upstream) -> new LocusPoint2D(upstream.getX(),
							upstream.getY()), "g8a-nested-transform/v1");
			loci.add(current);
		}
		return new NestedChain(source, loci);
	}

	private Problem nestedProblem(NestedChain chain, int depth) {
		GeoLocusV2 outer = chain.outer();
		LineTarget target = new LineTarget("nested-section-" + depth, 0, 1, 0);
		FactorizationProof proof = new FactorizationProof(1,
				List.of(new RootProof("nested-root", 0, 1)), false, "h(t)=t");
		return new Problem(outer.getLocusIdentity() + "+" + target.identity(),
				outer.getLocusIdentity(), outer.getSemanticRevision(),
				target.identity(), 1, NESTED_BRANCH,
				NESTED_BRANCH + "/lineage-v1", NESTED_BRANCH + "/component-0",
				"nested-topology", -1, 1, true, true, false, parameter -> {
					try (LocusEvaluationSession2D session =
							LocusEvaluationSession2D.reference()) {
						LocusEvaluation2D evaluation = outer.evaluate(NESTED_BRANCH,
								parameter, session);
						if (!evaluation.isValid()) {
							throw new IllegalStateException(evaluation.getDiagnostic());
						}
						return evaluation.getPoint();
					}
				}, target, proof,
				G8AIntersectionSemanticModel.Policy.measuredCandidate(),
				G8AIntersectionSemanticModel.WorkBudget.measuredCandidate());
	}

	private static void assertHardZero(WorkSnapshot work) {
		assertTrue(work.hardZeroAuthorityReads());
		assertEquals(0, work.sharedIndexBuilds());
		assertEquals(0, work.retainedIndexEntries());
		assertEquals(0, work.retainedRootHistoryEntries());
	}

	private static int verifiedRoots(List<Result> results) {
		return results.stream().mapToInt(result -> result.solutions().size()).sum();
	}

	private static List<Result> concat(List<Result> first, List<Result> second,
			List<Result> third) {
		List<Result> all = new ArrayList<>(first);
		all.addAll(second);
		all.addAll(third);
		return all;
	}

	private static void reset(NestedChain chain) {
		for (GeoLocusV2 locus : chain.loci()) {
			locus.getInstrumentation().reset();
		}
	}

	private static long evaluatorCalls(NestedChain chain) {
		return chain.loci().stream().mapToLong(locus -> locus.getInstrumentation()
				.getEvaluatorCalls()).sum();
	}

	private static long forbiddenLocusWork(NestedChain chain) {
		return chain.loci().stream().mapToLong(locus -> locus.getInstrumentation()
				.getWholeLocusRegenerations() + locus.getInstrumentation()
				.getRenderEvaluations()).sum();
	}

	private static long[] revisions(NestedChain chain) {
		return chain.loci().stream().mapToLong(GeoLocusV2::getSemanticRevision)
				.toArray();
	}

	private record NestedChain(GeoNumeric source, List<GeoLocusV2> loci) {
		NestedChain {
			loci = List.copyOf(loci);
		}

		GeoLocusV2 outer() {
			return loci.get(loci.size() - 1);
		}
	}
}
