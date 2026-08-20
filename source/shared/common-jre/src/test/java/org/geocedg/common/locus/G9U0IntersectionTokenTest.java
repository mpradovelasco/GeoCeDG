/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.intersection.IntersectionCandidateSet2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionCapabilityContext2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionOverlapEvidence2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.IntersectionTokenLineage2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionCapability2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusSemanticIntersectionToken2D;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Exact I18 public rich-intersection and semantic-token scenarios. */
class G9U0IntersectionTokenTest extends G9U0PublicSurfaceTestBase {

	@Test
	void i01GeneralIntersectPublishesRichAuthority() {
		GeoLocusV2 locus = createParabola();
		List<GeoLocusIntersectionResult> results =
				allPublicIntersectionFamilies(locus);
		assertEquals(10, results.size());
		for (GeoLocusIntersectionResult result : results) {
			assertEquals(Commands.Intersect,
					result.getParentAlgorithm().getClassName());
			assertSame(locus, result.getParentAlgorithm().getInput(0));
			assertNotNull(result.getIntersectionResult());
		}
		assertTrue(results.stream().allMatch(result ->
				result.getParentAlgorithm().getOutput(0) == result));

		add("sOption=0");
		add("QOption=(sOption,0)");
		add("DOption={false,{0,2,true,true}}");
		add("LOption=LocusV2(QOption,sOption,DOption)");
		add("optionLine:x=1");
		add("optionSegment=Segment((1,-1),(1,1))");
		add("optionRay=Ray((1,-1),(1,1))");
		add("optionCircle=Circle((0,0),1)");
		for (String target : List.of("optionLine", "optionSegment", "optionRay",
				"optionCircle")) {
			GeoLocusIntersectionResult optionB = add("R" + target
					+ "=Intersect(LOption," + target + ")");
			assertEquals(Completeness.NOT_ESTABLISHED,
					value(optionB).getCompletenessEvidence().getCompleteness(), target);
			assertEquals(1, value(optionB).getFiniteSolutions().size(), target);
			String token = firstToken(optionB);
			assertTrue(value(optionB).findPointAdmissibleSolution(token)
					.isPresent(), target);
		}
	}

	@Test
	void i02BaselineNonV2DispatchRemainsARegularPoint() {
		add("g:x=0");
		add("h:y=0");
		GeoPoint point = add("A=Intersect(g,h)");
		assertEquals(0, point.getInhomX(), 0);
		assertEquals(0, point.getInhomY(), 0);
		assertFalse(point.getParentAlgorithm().getInput(0)
				instanceof GeoLocusV2);
	}

	@Test
	void i03CompleteEmptyRichResultIsNotAFalseFiniteSet() {
		var fixture = analyticLine("u0-i03-empty");
		GeoLocusIntersectionResult result = analyticSingleTarget(fixture,
				add("y=3"), "u0-i03-empty",
				G8BIntersectionFixtures.capability("u0-i03/analytic/v1",
						G8BIntersectionFixtures::completeEmpty)).getResult();
		assertEquals(GeometryKind.EMPTY, value(result).getGeometryKind());
		assertEquals(Completeness.COMPLETE,
				value(result).getCompletenessEvidence().getCompleteness());
		assertTrue(value(result).getFiniteSolutions().isEmpty());
	}

	@Test
	void i04CompleteFiniteRichResultRetainsVerifiedRoots() {
		var fixture = analyticLine("u0-i04-finite");
		GeoLocusIntersectionResult result = analyticSingleTarget(fixture,
				add("x=0"), "u0-i04-finite",
				G8BIntersectionFixtures.capability("u0-i04/analytic/v1",
						context -> G8BIntersectionFixtures.completeRoots(context,
								List.of(analyticRoot(fixture, 0,
										"only-root"))))).getResult();
		assertEquals(GeometryKind.FINITE, value(result).getGeometryKind());
		assertEquals(Completeness.COMPLETE,
				value(result).getCompletenessEvidence().getCompleteness());
		assertEquals(1, value(result).getFiniteSolutions().size());
	}

	@Test
	void i05IncompleteFiniteResultKeepsOptionBSeparation() {
		GeoLocusIntersectionResult result = intersect(createLine(), "x=0");
		assertEquals(GeometryKind.FINITE, value(result).getGeometryKind());
		assertNotEquals(Completeness.COMPLETE,
				value(result).getCompletenessEvidence().getCompleteness());
		assertTrue(value(result).getFiniteSolutions().stream().anyMatch(solution ->
				value(result).findPointAdmissibleSolution(
						solution.getIdentity().getRootToken()).isPresent()));
	}

	@Test
	void i06LocallyUnisolatedRootCannotBecomeAPoint() {
		GeoLocusV2 locus = createScalarLocus("L", "s", "Q",
				"(s,abs(s))", "{false,{-1,1,true,true}}");
		GeoLocusIntersectionResult result = intersect(locus, "y=0");
		assertTrue(value(result).getFiniteSolutions().stream().allMatch(solution ->
				solution.getRevisionEvidence().getLocalIsolationStatus()
						== LocalIsolationStatus.NOT_ESTABLISHED));
	}

	@Test
	void i07TangentAndNearTangentRootsAreTopologicallyDistinct() {
		add("a=0");
		GeoLocusIntersectionResult result = intersect(createParabola(), "y=a");
		assertEquals(1, value(result).getFiniteSolutions().size());
		assertTrue(value(result).getFiniteSolutions().stream().allMatch(solution ->
				value(result).findPointAdmissibleSolution(
						solution.getIdentity().getRootToken()).isEmpty()));
		((org.geogebra.common.kernel.geos.GeoNumeric) requireLookup("a"))
				.setValue(1E-6);
		requireLookup("a").updateCascade();
		List<String> handles = value(result).getFiniteSolutions().stream()
				.map(solution -> solution.getIdentity().getRootToken())
				.collect(Collectors.toList());
		assertEquals(2, handles.size());
		assertEquals(2, Set.copyOf(handles).size());
		assertTrue(handles.stream().allMatch(
				LocusSemanticIntersectionToken2D::isRevisionLocalHandle));
		assertTrue(handles.stream().allMatch(handle ->
				LocusSemanticIntersectionToken2D.decode(handle).isEmpty()));
		assertTrue(handles.stream().allMatch(handle ->
				result.findExactPointAdmissibleSolution(handle).isEmpty()));
		GeoPoint firstUnselectable = add("Xi07a=Intersect("
				+ result.getLabelSimple() + ",\"" + handles.get(0) + "\")");
		GeoPoint secondUnselectable = add("Xi07b=Intersect("
				+ result.getLabelSimple() + ",\"" + handles.get(1) + "\")");
		assertFalse(firstUnselectable.isDefined());
		assertFalse(secondUnselectable.isDefined());
	}

	@Test
	void i08TypedOverlapPublishesNoSyntheticPointList() {
		var fixture = analyticLine("u0-i08-overlap");
		GeoLocusIntersectionResult result = analyticSingleTarget(fixture,
				add("y=0"), "u0-i08-overlap",
				G8BIntersectionFixtures.capability("u0-i08/analytic/v1",
						context -> G8BIntersectionFixtures.overlap(context,
								GeometryKind.OVERLAP, fixture.branchKey(),
								fixture.componentKey()))).getResult();
		assertEquals(GeometryKind.OVERLAP, value(result).getGeometryKind());
		assertTrue(value(result).getFiniteSolutions().isEmpty());
		assertFalse(value(result).getOverlapEvidence().isEmpty());
	}

	@Test
	void i09MixedFiniteAndOverlapRemainsARichMixedResult() {
		String identity = "u0-i09-mixed";
		var provider = G8BIntersectionFixtures.provider(identity, -2, 2, true,
				true, false, Orientation.INCREASING);
		LocusBranch2D overlapBranch = G8BIntersectionFixtures.branch(
				identity + "/overlap", provider,
				List.of(new LocusInterval2D(-2, -1, true, true)),
				EnumSet.of(BranchProperty.FINITE));
		LocusBranch2D crossingBranch = G8BIntersectionFixtures.branch(
				identity + "/crossing", provider,
				List.of(new LocusInterval2D(1, 2, true, true)),
				EnumSet.of(BranchProperty.FINITE));
		var fixture = G8BIntersectionFixtures.create(getConstruction(), identity,
				provider, List.of(overlapBranch, crossingBranch),
				(source, branch, parameter) -> new LocusPoint2D(parameter,
						branch == overlapBranch ? 0 : parameter - 1.5));
		String crossingComponent = IntersectionCapabilityContext2D.componentKey(
				crossingBranch.getBranchKey(), 0);
		var capability = G8BIntersectionFixtures.capability(
				"u0-i09/analytic/v1", context -> {
					var roots = G8BIntersectionFixtures.completeRoots(context,
							List.of(G8BIntersectionFixtures.Root.simple(
									crossingBranch.getBranchKey(), crossingComponent,
									1.5, "isolated-crossing")));
					var overlap = new IntersectionOverlapEvidence2D(
							overlapBranch.getBranchKey(),
							IntersectionCapabilityContext2D.componentKey(
									overlapBranch.getBranchKey(), 0),
							"analytic component identity",
							"target residual is identically zero");
					return new IntersectionCandidateSet2D(Completeness.COMPLETE,
							CompletenessMethod.ANALYTIC_ROOT_ENUMERATION,
							GeometryKind.MIXED_FINITE_OVERLAP,
							SupportLevel.EXACT_CAPABILITY,
							NumericGuarantee.CERTIFIED_ERROR_BOUND,
							context.getAllComponentKeys(), roots.getCandidates(),
							List.of(overlap), List.of());
				});
		GeoLocusIntersectionResult result = analyticSingleTarget(fixture,
				add("y=0"), identity, capability).getResult();
		assertEquals(GeometryKind.MIXED_FINITE_OVERLAP,
				value(result).getGeometryKind());
		assertFalse(value(result).getFiniteSolutions().isEmpty());
		assertFalse(value(result).getOverlapEvidence().isEmpty());
	}

	@Test
	void i10DeterministicWorkLimitDoesNotPublishPartialAsComplete() {
		GeoLocusV2 locus = createScalarLocus("L", "s", "Q",
				"(s,sin(1000000000*s))", "{false,{-1,1,true,true}}");
		GeoLocusIntersectionResult result = intersect(locus, "y=0");
		ComputationStatus first = value(result).getComputationStatus();
		long work = value(result).getWork().getSemanticEvaluations();
		result.getParentAlgorithm().update();
		assertEquals(first, value(result).getComputationStatus());
		assertEquals(work, value(result).getWork().getSemanticEvaluations());
		assertNotEquals(Completeness.COMPLETE,
				value(result).getCompletenessEvidence().getCompleteness());
	}

	@Test
	void i11ExactTokenPointOverloadCreatesANormalDerivedPoint() {
		GeoLocusIntersectionResult result = intersect(createLine(), "x=0");
		String token = firstToken(result);
		GeoPoint point = tokenPoint(result, token);
		assertTrue(point.isDefined());
		assertSame(result, point.getParentAlgorithm().getInput(0));
		assertEquals(0, point.getInhomX(), 1E-12);
	}

	@Test
	void i12UnknownOrTamperedTokenNeverFallsBackToProximity() {
		GeoLocusIntersectionResult result = intersect(createLine(), "x=0");
		String token = firstToken(result);
		String forged = token.substring(0, token.length() - 1)
				+ (token.endsWith("0") ? "1" : "0");
		assertFalse(tokenPoint(result, forged).isDefined());
		assertFalse(result.findExactPointAdmissibleSolution(forged).isPresent());
		var decoded = LocusSemanticIntersectionToken2D.decode(token).orElseThrow();
		String forgedMaterial = LocusSemanticIntersectionToken2D.create(
				decoded.getResultOwnerIdentity(), "forged-source-pair",
				"forged-constructive-lineage", "forged-topology",
				IntersectionTokenLineage2D.forSingleComponent(BRANCH,
						new LocusInterval2D(-2, 2, true, true),
						Optional.of("forged-continuation")),
				decoded.getIncarnation());
		assertTrue(LocusSemanticIntersectionToken2D.decode(forgedMaterial)
				.isPresent());
		assertFalse(result.findExactPointAdmissibleSolution(forgedMaterial)
				.isPresent());
	}

	@Test
	void i13OpaqueTokenContainsOnlyExplicitSemanticRoutingMaterial() {
		String token = firstToken(intersect(createLine(), "x=0"));
		var decoded = LocusSemanticIntersectionToken2D.decode(token).orElseThrow();
		String lineage = decoded.getEstablishedBranchLineage().toLowerCase();
		assertTrue(token.startsWith("locus-root/v3/"));
		assertFalse(lineage.contains("sample"));
		assertFalse(lineage.contains("ordinal"));
		assertFalse(lineage.contains("candidate"));
	}

	@Test
	void i14StableSemanticPreimageRetainsExactTokenContinuation() {
		add("b=0");
		GeoLocusV2 locus = createScalarLocus("L", "s", "Q", "(s,b)",
				"{false,{-2,2,true,true}}");
		GeoLocusIntersectionResult result = intersect(locus, "x=0");
		Set<String> before = tokens(value(result));
		String oldToken = before.iterator().next();
		GeoPoint oldPoint = tokenPoint(result, oldToken);
		assertTrue(oldPoint.isDefined());
		assertEquals(0, oldPoint.getInhomY(), 0);
		((org.geogebra.common.kernel.geos.GeoNumeric) requireLookup("b"))
				.setValue(1);
		requireLookup("b").updateCascade();
		Set<String> after = tokens(value(result));
		assertEquals(1, before.size());
		assertEquals(1, after.size());
		assertEquals(before, after);
		assertTrue(oldPoint.isDefined());
		assertEquals(0, oldPoint.getInhomX(), 0);
		assertEquals(1, oldPoint.getInhomY(), 1E-12);

		// A stable token is possible only when an explicit semantic continuation
		// certificate supplies the same non-geometric lineage material.
		LocusInterval2D exactComponent =
				new LocusInterval2D(-2, 2, true, true);
		IntersectionTokenLineage2D beforeComponentReorder =
				IntersectionTokenLineage2D.forSingleComponent(BRANCH,
						exactComponent, Optional.of("same-local-root"));
		IntersectionTokenLineage2D afterComponentReorder =
				IntersectionTokenLineage2D.forSingleComponent(BRANCH,
						exactComponent, Optional.of("same-local-root"));
		assertEquals(beforeComponentReorder.getSolutionLineageKey(),
				afterComponentReorder.getSolutionLineageKey());
	}

	@Test
	void i15MergeAndSplitBurnAmbiguousContinuationTokens() {
		String duplicateIdentity = "u0-i15-duplicate-semantic";
		var duplicateFixture = G8BIntersectionFixtures.single(getConstruction(),
				duplicateIdentity, -1, 1, true, true, false,
				(source, branch, parameter) -> new LocusPoint2D(parameter,
						parameter * parameter - 0.25));
		GeoLocusIntersectionResult duplicate = analyticSingleTarget(
				duplicateFixture, add("y=0"), duplicateIdentity,
				G8BIntersectionFixtures.capability(
						"u0-i15/duplicate-semantic/v1",
						context -> G8BIntersectionFixtures.completeRoots(context,
								List.of(analyticRoot(duplicateFixture, -0.5,
										"colliding-continuation"),
										analyticRoot(duplicateFixture, 0.5,
												"colliding-continuation")))))
				.getResult();
		assertEquals(2, value(duplicate).getFiniteSolutions().size());
		assertNotEquals(value(duplicate).getFiniteSolutions().get(0).getIdentity()
				.getRootToken(), value(duplicate).getFiniteSolutions().get(1)
						.getIdentity().getRootToken());
		var firstDuplicate = value(duplicate).getFiniteSolutions().get(0);
		assertTrue(value(duplicate).findPointAdmissibleSolutionByLineage(
				firstDuplicate.getIdentity().getEstablishedBranchLineage(),
				firstDuplicate.getIdentity().getExplicitContinuationKey()
						.orElseThrow()).isEmpty());

		add("a=1");
		GeoLocusIntersectionResult result = intersect(createParabola(), "y=a");
		Set<String> before = tokens(value(result));
		((org.geogebra.common.kernel.geos.GeoNumeric) requireLookup("a"))
				.setValue(0);
		requireLookup("a").updateCascade();
		((org.geogebra.common.kernel.geos.GeoNumeric) requireLookup("a"))
				.setValue(1);
		requireLookup("a").updateCascade();
		Set<String> after = tokens(value(result));
		assertTrue(before.stream().noneMatch(after::contains));

		add("c=0");
		add("Tu:x=c");
		GeoLocusIntersectionResult unique = add("Ru=Intersect(L,Tu)");
		String uniqueBefore = firstToken(unique);
		GeoPoint oldUniquePoint = add(
				"Xu=Intersect(Ru,\"" + uniqueBefore + "\")");
		assertTrue(oldUniquePoint.isDefined());
		((org.geogebra.common.kernel.geos.GeoNumeric) requireLookup("c"))
				.setValue(0.5);
		requireLookup("c").updateCascade();
		String uniqueAfter = firstToken(unique);
		assertNotEquals(uniqueBefore, uniqueAfter);
		assertFalse(oldUniquePoint.isDefined());
		assertTrue(value(unique).findPointAdmissibleSolution(uniqueAfter)
				.isPresent());
		GeoPoint currentUniquePoint = add(
				"Yu=Intersect(Ru,\"" + uniqueAfter + "\")");
		assertTrue(currentUniquePoint.isDefined());
		assertEquals(0.5, currentUniquePoint.getInhomX(), 1E-12);
		assertEquals(0.25, currentUniquePoint.getInhomY(), 1E-12);

		add("k=-1");
		add("jump=If(k<0,-1,1)");
		add("Tjump:x=jump");
		GeoLocusIntersectionResult discontinuous =
				add("Rjump=Intersect(L,Tjump)");
		String discontinuousBefore = firstToken(discontinuous);
		GeoPoint oldDiscontinuousPoint = add(
				"Xjump=Intersect(Rjump,\"" + discontinuousBefore + "\")");
		assertTrue(oldDiscontinuousPoint.isDefined());
		((org.geogebra.common.kernel.geos.GeoNumeric) requireLookup("k"))
				.setValue(1);
		requireLookup("k").updateCascade();
		String discontinuousAfter = firstToken(discontinuous);
		assertNotEquals(discontinuousBefore, discontinuousAfter);
		assertFalse(oldDiscontinuousPoint.isDefined());
		GeoPoint currentDiscontinuousPoint = add(
				"Yjump=Intersect(Rjump,\"" + discontinuousAfter + "\")");
		assertTrue(currentDiscontinuousPoint.isDefined());
		assertEquals(1, currentDiscontinuousPoint.getInhomX(), 1E-12);
		assertEquals(1, currentDiscontinuousPoint.getInhomY(), 1E-12);
	}

	@Test
	void i16TokenFromAnotherResultFailsRevisionOwnerValidation() {
		GeoLocusIntersectionResult first = intersect(createLine(), "x=0");
		String token = firstToken(first);
		add("s2=0");
		add("Q2=(s2,1)");
		add("D2={false,{-2,2,true,true}}");
		GeoLocusV2 secondLocus = add("L2=LocusV2(Q2,s2,D2)");
		add("target2:x=0");
		GeoLocusIntersectionResult second = add("R2=Intersect(L2,target2)");
		GeoPoint point = add("Y=Intersect(R2,\"" + token + "\")");
		assertFalse(point.isDefined());
	}

	@Test
	void i17CanonicalLocusPairLineageIsSourceOrderInvariant() {
		GeoLocusV2 firstLocus = createLine();
		add("t=0");
		add("Q2=(t,t)");
		add("D2={false,{-2,2,true,true}}");
		GeoLocusV2 secondLocus = add("L2=LocusV2(Q2,t,D2)");
		GeoLocusIntersectionResult forwardResult =
				add("PairForward=Intersect(L,L2)");
		GeoLocusIntersectionResult reverseResult =
				add("PairReverse=Intersect(L2,L)");
		assertSame(firstLocus, forwardResult.getParentAlgorithm().getInput(0));
		assertSame(secondLocus, reverseResult.getParentAlgorithm().getInput(0));
		assertEquals(forwardResult.getSourcePairIdentity(),
				reverseResult.getSourcePairIdentity());
		assertEquals(value(forwardResult).getGeometryKind(),
				value(reverseResult).getGeometryKind());
		assertEquals(value(forwardResult).getFiniteSolutions().size(),
				value(reverseResult).getFiniteSolutions().size());
		assertFalse(value(forwardResult).getFiniteSolutions().isEmpty());
		assertEquals(value(forwardResult).getFiniteSolutions().get(0)
				.getPairEvidence().orElseThrow()
				.getEstablishedBranchPairLineage(),
				value(reverseResult).getFiniteSolutions().get(0)
						.getPairEvidence().orElseThrow()
						.getEstablishedBranchPairLineage());
		assertTrue(value(forwardResult).getFiniteSolutions().stream()
				.allMatch(solution -> value(forwardResult)
						.findPointAdmissibleSolution(
								solution.getIdentity().getRootToken()).isEmpty()));
		assertTrue(value(reverseResult).getFiniteSolutions().stream()
				.allMatch(solution -> value(reverseResult)
						.findPointAdmissibleSolution(
								solution.getIdentity().getRootToken()).isEmpty()));

		LocusInterval2D first = new LocusInterval2D(-2, -1, true, false);
		LocusInterval2D second = new LocusInterval2D(1, 2, false, true);
		IntersectionTokenLineage2D forward =
				IntersectionTokenLineage2D.forCanonicalComponentPair(
						"branch-a", first, "branch-b", second,
						Optional.of("continuation"));
		IntersectionTokenLineage2D reverse =
				IntersectionTokenLineage2D.forCanonicalComponentPair(
						"branch-b", second, "branch-a", first,
						Optional.of("continuation"));
		assertEquals(forward.getEstablishedBranchLineage(),
				reverse.getEstablishedBranchLineage());
		assertEquals(forward.getSolutionLineageKey(),
				reverse.getSolutionLineageKey());
	}

	@Test
	void i18UnsupportedTargetAndUnboundedDomainFailExplicitly() {
		GeoLocusV2 locus = createLine();
		GeoLocusIntersectionResult unsupported = intersect(locus,
				"Polygon((0,0),(1,0),(0,1))");
		assertEquals(ComputationStatus.UNSUPPORTED,
				value(unsupported).getComputationStatus());
		AssertionError unbounded = assertThrows(AssertionError.class, () -> {
			add("u=0");
			add("U=(u,u)");
			add("Du={false,{-infinity,infinity,true,true}}");
			add("LU=LocusV2(U,u,Du)");
		});
		assertTrue(unbounded.getMessage()
				.contains("domain endpoints must be finite"));
	}

	private static LocusIntersectionResult2D value(
			GeoLocusIntersectionResult result) {
		assertNotNull(result.getIntersectionResult());
		return result.getIntersectionResult();
	}

	private G8BIntersectionFixtures.Fixture analyticLine(String identity) {
		return G8BIntersectionFixtures.single(getConstruction(), identity, -2, 2,
				true, true, false, (source, branch, parameter) ->
						new LocusPoint2D(parameter, 0));
	}

	private AlgoLocusIntersectionV2 analyticSingleTarget(
			G8BIntersectionFixtures.Fixture fixture, GeoElement target,
			String identity, LocusIntersectionCapability2D capability) {
		return new AlgoLocusIntersectionV2(getConstruction(), fixture.locus(),
				target, identity + "/pair", identity + "/lineage",
				identity + "/target", identity + "/topology-v1", capability,
				new GeoElement[0]);
	}

	private static G8BIntersectionFixtures.Root analyticRoot(
			G8BIntersectionFixtures.Fixture fixture, double parameter,
			String continuation) {
		return G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
				fixture.componentKey(), parameter, continuation);
	}

	private static Set<String> tokens(LocusIntersectionResult2D result) {
		return result.getFiniteSolutions().stream()
				.map(solution -> solution.getIdentity().getRootToken())
				.collect(Collectors.toSet());
	}
}
