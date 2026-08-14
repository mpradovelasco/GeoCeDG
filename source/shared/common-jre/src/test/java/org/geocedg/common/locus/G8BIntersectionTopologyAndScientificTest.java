/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.intersection.IntersectionCandidateSet2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionCapabilityContext2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionOverlapEvidence2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DomainLocation;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionCapability2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.junit.jupiter.api.Test;

/** Topological, scientific-pilot and functional-budget validation for G8B. */
class G8BIntersectionTopologyAndScientificTest extends BaseUnitTest {

	@Test
	void viewportAndRenderScaleCannotChangeIntersectionAuthority() {
		G8BIntersectionFixtures.Fixture semantic = parabola("viewport");
		GeoLine target = add("y=1");
		AlgoLocusIntersectionV2 algorithm = complete(semantic, target, "viewport",
				List.of(simple(semantic, -1, "left"),
						simple(semantic, 1, "right")));
		LocusIntersectionResult2D before =
				algorithm.getResult().getIntersectionResult();
		EuclidianView view = getApp().getEuclidianView1();
		view.setCoordSystem(400, 300, 20, 20);
		view.setCoordSystem(400, 300, 250, 250);
		LocusIntersectionResult2D after =
				algorithm.getResult().getIntersectionResult();
		assertSame(before, after);
		assertEquals(List.of(-1.0, 1.0), parameters(after));
		assertTrue(after.getWork().hasZeroForbiddenAuthorityReads());
		assertEquals(0, after.getWork().getViewportReads());
		assertEquals(0, after.getWork().getPixelToleranceReads());
	}

	@Test
	void selfIntersectionRetainsTwoPreimagesAtOneCoordinate() {
		G8BIntersectionFixtures.Fixture semantic =
				G8BIntersectionFixtures.single(getConstruction(), "self-crossing",
						-2, 2, true, true, false,
						(source, branch, parameter) -> new LocusPoint2D(
								parameter * parameter - 1,
								parameter * (parameter * parameter - 1)));
		AlgoLocusIntersectionV2 algorithm = complete(semantic, add("x=0"),
				"self-crossing", List.of(simple(semantic, -1, "preimage-left"),
						simple(semantic, 1, "preimage-right")));
		LocusIntersectionResult2D result =
				algorithm.getResult().getIntersectionResult();
		assertEquals(2, result.getFiniteSolutions().size());
		assertEquals(result.getFiniteSolutions().get(0).getEvaluatedPoint(),
				result.getFiniteSolutions().get(1).getEvaluatedPoint());
		assertEquals(2, result.getFiniteSolutions().stream()
				.map(solution -> solution.getIdentity().getRootToken()).distinct()
				.count());
	}

	@Test
	void cuspKeepsSingularRegularityUnclaimedDespiteExactMultiplicity() {
		G8BIntersectionFixtures.Fixture semantic =
				G8BIntersectionFixtures.single(getConstruction(), "cusp", -1, 1,
						true, true, false,
						(source, branch, parameter) -> new LocusPoint2D(
								parameter * parameter,
								parameter * parameter * parameter));
		AlgoLocusIntersectionV2 algorithm = complete(semantic, add("y=0"),
				"cusp", List.of(G8BIntersectionFixtures.Root.tangent(
						semantic.branchKey(), semantic.componentKey(), 0,
						"cusp-root", 3)));
		var classification = algorithm.getResult().getIntersectionResult()
				.getFiniteSolutions().get(0).getClassification();
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				classification.getContactClass());
		assertEquals(3, classification.getEstablishedMultiplicity().orElseThrow());
		assertEquals(Regularity.UNKNOWN, classification.getSourceRegularity());
	}

	@Test
	void isolatedEmptyAndCollapsedComponentsUseDistinctSetSemantics() {
		String identity = "component-degenerations";
		var provider = G8BIntersectionFixtures.provider(identity, -1, 1, true,
				true, false, Orientation.INCREASING);
		LocusBranch2D isolated = G8BIntersectionFixtures.branch(
				identity + "/isolated", provider,
				List.of(new LocusInterval2D(0, 0, true, true)),
				EnumSet.of(BranchProperty.COLLAPSED_IMAGE));
		G8BIntersectionFixtures.Fixture isolatedFixture =
				G8BIntersectionFixtures.create(getConstruction(), identity + "/one",
						provider, List.of(isolated),
						(source, branch, parameter) -> new LocusPoint2D(0, 0));
		String isolatedComponent = IntersectionCapabilityContext2D.componentKey(
				isolated.getBranchKey(), 0);
		AlgoLocusIntersectionV2 point = complete(isolatedFixture, add("x=0"),
				"isolated", List.of(G8BIntersectionFixtures.Root.simple(
						isolated.getBranchKey(), isolatedComponent, 0, "isolated")));
		assertEquals(DomainLocation.ISOLATED_COMPONENT,
				point.getResult().getIntersectionResult().getFiniteSolutions().get(0)
						.getClassification().getDomainLocation());

		LocusBranch2D empty = G8BIntersectionFixtures.branch(identity + "/empty",
				provider, List.of(), EnumSet.noneOf(BranchProperty.class));
		G8BIntersectionFixtures.Fixture emptyFixture =
				G8BIntersectionFixtures.create(getConstruction(), identity + "/empty",
						provider, List.of(empty),
						(source, branch, parameter) -> new LocusPoint2D(0, 0));
		AlgoLocusIntersectionV2 emptyResult = algorithm(emptyFixture, add("x=0"),
				"empty-component", null);
		assertEquals(GeometryKind.EMPTY,
				emptyResult.getResult().getIntersectionResult().getGeometryKind());
		assertEquals(Completeness.COMPLETE, emptyResult.getResult()
				.getIntersectionResult().getCompletenessEvidence().getCompleteness());

		G8BIntersectionFixtures.Fixture collapsed =
				G8BIntersectionFixtures.single(getConstruction(), "collapsed", -1, 1,
						true, true, false,
						(source, branch, parameter) -> new LocusPoint2D(0, 0));
		LocusIntersectionCapability2D overlap =
				G8BIntersectionFixtures.capability("collapsed-overlap/v1", context ->
						G8BIntersectionFixtures.overlap(context,
								GeometryKind.OVERLAP, collapsed.branchKey(),
								collapsed.componentKey()));
		AlgoLocusIntersectionV2 overlapResult = algorithm(collapsed, add("y=0"),
				"collapsed", overlap);
		assertEquals(GeometryKind.OVERLAP,
				overlapResult.getResult().getIntersectionResult().getGeometryKind());
		assertEquals(0, overlapResult.getResult().getIntersectionResult()
				.getFiniteSolutions().size());
	}

	@Test
	void overlapComponentCanCoexistWithVerifiedIsolatedRootEvidence() {
		String identity = "partial-overlap";
		var provider = G8BIntersectionFixtures.provider(identity, -1, 1, true,
				true, false, Orientation.INCREASING);
		LocusBranch2D overlapBranch = G8BIntersectionFixtures.branch(
				identity + "/overlap", provider,
				List.of(provider.getDeclaredDomain()),
				EnumSet.of(BranchProperty.FINITE));
		LocusBranch2D crossingBranch = G8BIntersectionFixtures.branch(
				identity + "/crossing", provider,
				List.of(provider.getDeclaredDomain()),
				EnumSet.of(BranchProperty.FINITE));
		G8BIntersectionFixtures.Fixture semantic =
				G8BIntersectionFixtures.create(getConstruction(), identity, provider,
						List.of(overlapBranch, crossingBranch),
						(source, branch, parameter) -> new LocusPoint2D(parameter,
								branch == overlapBranch ? 0 : parameter));
		String crossingComponent = IntersectionCapabilityContext2D.componentKey(
				crossingBranch.getBranchKey(), 0);
		LocusIntersectionCapability2D capability =
				G8BIntersectionFixtures.capability("partial-overlap/v1", context -> {
					var roots = G8BIntersectionFixtures.completeRoots(context,
							List.of(G8BIntersectionFixtures.Root.simple(
									crossingBranch.getBranchKey(), crossingComponent, 0,
									"isolated-crossing")));
					var overlap = new IntersectionOverlapEvidence2D(
							overlapBranch.getBranchKey(),
							IntersectionCapabilityContext2D.componentKey(
									overlapBranch.getBranchKey(), 0),
							"analytic component identity",
							"line residual is identically zero");
					return new IntersectionCandidateSet2D(Completeness.COMPLETE,
							CompletenessMethod.ANALYTIC_ROOT_ENUMERATION,
							GeometryKind.OVERLAP, SupportLevel.EXACT_CAPABILITY,
							NumericGuarantee.CERTIFIED_ERROR_BOUND,
							context.getAllComponentKeys(), roots.getCandidates(),
							List.of(overlap), List.of());
				});
		AlgoLocusIntersectionV2 algorithm = algorithm(semantic, add("y=0"),
				identity, capability);
		LocusIntersectionResult2D result =
				algorithm.getResult().getIntersectionResult();
		assertEquals(GeometryKind.OVERLAP, result.getGeometryKind());
		assertEquals(1, result.getOverlapEvidence().size());
		assertEquals(1, result.getFiniteSolutions().size());
		assertFalse(result.findPointAdmissibleSolution(result.getFiniteSolutions()
				.get(0).getIdentity().getRootToken()).isPresent());
	}

	@Test
	void scaleAndTranslationPreserveReferenceParametersAndResiduals() {
		G8BIntersectionFixtures.Fixture base = parabola("transform-base");
		AlgoLocusIntersectionV2 first = complete(base, add("y=1"),
				"transform-base", List.of(simple(base, -1, "left"),
						simple(base, 1, "right")));
		G8BIntersectionFixtures.Fixture transformed =
				G8BIntersectionFixtures.single(getConstruction(), "transform-image",
						-2, 2, true, true, false,
						(source, branch, parameter) -> new LocusPoint2D(
								10 + 3 * parameter,
								-7 + 3 * parameter * parameter));
		AlgoLocusIntersectionV2 second = complete(transformed, add("y=-4"),
				"transform-image", List.of(simple(transformed, -1, "left"),
						simple(transformed, 1, "right")));
		assertEquals(parameters(first.getResult().getIntersectionResult()),
				parameters(second.getResult().getIntersectionResult()));
		assertTrue(first.getResult().getIntersectionResult().getFiniteSolutions()
				.stream().allMatch(solution -> Math.abs(solution.getRevisionEvidence()
						.getResidualEvidence().getNormalizedResidual()) <= 2E-12));
		assertTrue(second.getResult().getIntersectionResult().getFiniteSolutions()
				.stream().allMatch(solution -> Math.abs(solution.getRevisionEvidence()
						.getResidualEvidence().getNormalizedResidual()) <= 2E-12));
	}

	@Test
	void lsimMultileafPilotKeepsFourConstructivePreimages() {
		String identity = "scientific-lsim-multileaf";
		var provider = G8BIntersectionFixtures.provider(identity, -2, 2, true,
				true, false, Orientation.INCREASING);
		LocusBranch2D leafA = G8BIntersectionFixtures.branch(identity + "/leaf-a",
				provider, List.of(provider.getDeclaredDomain()),
				EnumSet.of(BranchProperty.FINITE));
		LocusBranch2D leafB = G8BIntersectionFixtures.branch(identity + "/leaf-b",
				provider, List.of(provider.getDeclaredDomain()),
				EnumSet.of(BranchProperty.FINITE));
		G8BIntersectionFixtures.Fixture semantic =
				G8BIntersectionFixtures.create(getConstruction(), identity, provider,
						List.of(leafA, leafB), (source, branch, parameter) ->
								new LocusPoint2D(parameter,
										branch == leafA ? parameter * parameter - 1
												: 1 - parameter * parameter));
		List<G8BIntersectionFixtures.Root> roots = List.of(
				root(leafA, -1, "a-left"), root(leafA, 1, "a-right"),
				root(leafB, -1, "b-left"), root(leafB, 1, "b-right"));
		AlgoLocusIntersectionV2 algorithm = complete(semantic, add("y=0"),
				identity, roots);
		LocusIntersectionResult2D result =
				algorithm.getResult().getIntersectionResult();
		assertEquals(4, result.getFiniteSolutions().size());
		assertEquals(2, result.getFiniteSolutions().stream()
				.map(solution -> solution.getEvaluatedPoint().toString()).distinct()
				.count());
		assertEquals(4, result.getFiniteSolutions().stream()
				.map(solution -> solution.getIdentity().getRootToken()).distinct()
				.count());
	}

	@Test
	void focalIlluminationCirclePilotTransitionsTwoOneZeroExplicitly() {
		G8BIntersectionFixtures.Fixture semantic =
				G8BIntersectionFixtures.single(getConstruction(),
						"scientific-focal-circle", -2, 2, true, true, false,
						(source, branch, parameter) ->
								new LocusPoint2D(parameter, source));
		GeoConic circle = add("Circle((0,0),1)");
		LocusIntersectionCapability2D capability =
				G8BIntersectionFixtures.capability("focal-circle-pilot/v1", context -> {
					double height = semantic.source().getDouble();
					if (Math.abs(height) > 1) {
						return G8BIntersectionFixtures.completeEmpty(context);
					}
					if (Math.abs(height) == 1) {
						return G8BIntersectionFixtures.completeRoots(context,
								List.of(G8BIntersectionFixtures.Root.tangent(
										semantic.branchKey(), semantic.componentKey(), 0,
										"tangent", 2)));
					}
					double root = Math.sqrt(1 - height * height);
					return G8BIntersectionFixtures.completeRoots(context, List.of(
							simple(semantic, -root, "left"),
							simple(semantic, root, "right")));
				});
		AlgoLocusIntersectionV2 algorithm = algorithm(semantic, circle,
				"focal-circle", capability);
		assertEquals(2, algorithm.getResult().getIntersectionResult()
				.getFiniteSolutions().size());
		semantic.source().setValue(1);
		semantic.source().updateCascade();
		assertEquals(1, algorithm.getResult().getIntersectionResult()
				.getFiniteSolutions().size());
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				algorithm.getResult().getIntersectionResult().getFiniteSolutions()
						.get(0).getClassification().getContactClass());
		semantic.source().setValue(1.5);
		semantic.source().updateCascade();
		assertEquals(GeometryKind.EMPTY,
				algorithm.getResult().getIntersectionResult().getGeometryKind());
	}

	@Test
	void repeatedQueriesRemainDeterministicQueryLocalAndBounded() {
		G8BIntersectionFixtures.Fixture semantic =
				G8BIntersectionFixtures.single(getConstruction(), "repeated-query",
						-1, 1, true, true, false,
						(source, branch, parameter) ->
								new LocusPoint2D(parameter, parameter));
		AlgoLocusIntersectionV2 algorithm = complete(semantic, add("y=0"),
				"repeated-query", List.of(simple(semantic, 0, "root")));
		String token = algorithm.getResult().getIntersectionResult()
				.getFiniteSolutions().get(0).getIdentity().getRootToken();
		for (int iteration = 0; iteration < 100; iteration++) {
			algorithm.compute();
			LocusIntersectionResult2D result =
					algorithm.getResult().getIntersectionResult();
			assertEquals(token, result.getFiniteSolutions().get(0).getIdentity()
					.getRootToken());
			assertEquals(1, result.getWork().getSemanticEvaluations());
			assertEquals(1, result.getWork().getResidualVerifications());
			assertEquals(0, result.getWork().getRetainedIndexEntries());
			assertTrue(result.getWork().getRetainedTopologyEpochs() <= 2);
			assertTrue(result.getWork().hasZeroForbiddenAuthorityReads());
		}
		assertEquals(0,
				semantic.locus().getInstrumentation().getWholeLocusRegenerations());
	}

	private G8BIntersectionFixtures.Fixture parabola(String identity) {
		return G8BIntersectionFixtures.single(getConstruction(), identity, -2, 2,
				true, true, false,
				(source, branch, parameter) ->
						new LocusPoint2D(parameter, parameter * parameter));
	}

	private AlgoLocusIntersectionV2 complete(
			G8BIntersectionFixtures.Fixture semantic, GeoElement target,
			String identity, List<G8BIntersectionFixtures.Root> roots) {
		return algorithm(semantic, target, identity,
				G8BIntersectionFixtures.capability(identity + "/analytic/v1",
						context -> G8BIntersectionFixtures.completeRoots(context,
								roots)));
	}

	private AlgoLocusIntersectionV2 algorithm(
			G8BIntersectionFixtures.Fixture semantic, GeoElement target,
			String identity, LocusIntersectionCapability2D capability) {
		return new AlgoLocusIntersectionV2(getConstruction(), semantic.locus(),
				target, identity + "/pair", identity + "/lineage",
				identity + "/target", identity + "/topology-v1", capability,
				new GeoElement[0]);
	}

	private static G8BIntersectionFixtures.Root simple(
			G8BIntersectionFixtures.Fixture semantic, double parameter,
			String key) {
		return G8BIntersectionFixtures.Root.simple(semantic.branchKey(),
				semantic.componentKey(), parameter, key);
	}

	private static G8BIntersectionFixtures.Root root(LocusBranch2D branch,
			double parameter, String key) {
		return G8BIntersectionFixtures.Root.simple(branch.getBranchKey(),
				IntersectionCapabilityContext2D.componentKey(branch.getBranchKey(), 0),
				parameter, key);
	}

	private static List<Double> parameters(LocusIntersectionResult2D result) {
		return result.getFiniteSolutions().stream().map(solution ->
				solution.getRevisionEvidence().getSemanticParameter()).toList();
	}
}
