/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.intersection.IntersectionCandidate2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionCandidateSet2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionCapabilityContext2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionParameterInterval2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DomainLocation;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;
import org.geocedg.common.kernel.locus.intersection.IntersectionSourceBinding2D;
import org.geocedg.common.kernel.locus.intersection.LocusDifferentialEvaluation2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionPolicy2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionQuery2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolver2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTarget2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTargets2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionWorkBudget2D;
import org.geocedg.common.kernel.locus.intersection.TargetContactEvidence2D;
import org.geocedg.common.kernel.locus.intersection.TargetResidual2D;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.junit.jupiter.api.Test;

/** Productive G8B geometric, numerical and rich-result tests. */
class G8BIntersectionKernelTest extends BaseUnitTest {

	@Test
	void initialPolicyRetainsApprovedNormalizedValuesAndBudgets() {
		LocusIntersectionPolicy2D policy =
				LocusIntersectionPolicy2D.initial("provider/v1", "driver-angle");
		assertEquals("g8b-initial-normalized/v1", policy.getPolicyVersion());
		assertEquals(1E-12, policy.getRootParameterTolerance().getValue());
		assertEquals(2E-12,
				policy.getResidualTolerance().getAbsoluteTolerance());
		assertEquals(2E-12,
				policy.getResidualTolerance().getRelativeTolerance());
		assertEquals(1E-10, policy.getTangencyTolerance().getThreshold());
		assertEquals(4E-12, policy.getDeduplicationTolerance().getValue());
		assertEquals(1E-8, policy.getContinuationTolerance().getValue());
		assertEquals(4E-12, policy.getCoordinateTolerance().getValue());
		assertEquals(32_768,
				policy.getWorkBudget().getMaximumSemanticEvaluations());
		assertEquals(0,
				policy.getWorkBudget().getMaximumRetainedIndexEntries());
		assertEquals(2,
				policy.getWorkBudget().getMaximumRetainedTopologyEpochs());
	}

	@Test
	void lineResidualIsInvariantUnderEquationScaling() {
		GeoLine first = add("x + 2y = 3");
		GeoLine scaled = add("1000x + 2000y = 3000");
		LocusPoint2D point = new LocusPoint2D(4, -0.25);
		TargetResidual2D one = LocusIntersectionTargets2D.capture(first,
				"line-one", 1).evaluateResidual(point);
		TargetResidual2D two = LocusIntersectionTargets2D.capture(scaled,
				"line-two", 1).evaluateResidual(point);
		assertEquals(one.getNormalizedResidual(), two.getNormalizedResidual(),
				1E-15);
		assertEquals("model-coordinate", one.getContract().getUnits());
	}

	@Test
	void normalizedLineContactIsEquationAndSourceSpeedScaleInvariant() {
		GeoLine first = add("y=0");
		GeoLine scaled = add("1000y=0");
		LocusPoint2D point = new LocusPoint2D(0, 0);
		var derivative = differential(1, 2);
		double expected = 2 / Math.sqrt(5);
		assertEquals(expected, LocusIntersectionTargets2D.capture(first,
				"contact-line", 1).evaluateContact(point, derivative)
				.getNormalizedIndicator(), 1E-15);
		assertEquals(expected, LocusIntersectionTargets2D.capture(scaled,
				"contact-scaled", 1).evaluateContact(point, differential(10, 20))
				.getNormalizedIndicator(), 1E-15);
		assertEquals(-expected, LocusIntersectionTargets2D.capture(first,
				"contact-reversed", 1).evaluateContact(point, differential(-1, -2))
				.getNormalizedIndicator(), 1E-15);
	}

	@Test
	void normalizedCircleContactSeparatesTangencyAndUnknownRegularity() {
		LocusIntersectionTarget2D circle = LocusIntersectionTargets2D.capture(
				add("Circle((0,0),1)"), "contact-circle", 1);
		TargetContactEvidence2D tangent = circle.evaluateContact(
				new LocusPoint2D(1, 0), differential(0, 5));
		TargetContactEvidence2D transverse = circle.evaluateContact(
				new LocusPoint2D(1, 0), differential(7, 0));
		assertEquals(0, tangent.getNormalizedIndicator(), 0);
		assertEquals(1, transverse.getNormalizedIndicator(), 0);
		var singular = new LocusDifferentialEvaluation2D(new LocusPoint2D(1, 0),
				Regularity.SINGULAR, NumericGuarantee.ESTIMATED_ERROR,
				"test singular differential");
		assertFalse(circle.evaluateContact(new LocusPoint2D(1, 0), singular)
				.isEstablished());
	}

	@Test
	void segmentAndRayUseSeparateCapturedLimitedMembership() {
		GeoSegment segment = add("Segment((0,0),(2,0))");
		GeoRay ray = add("Ray((0,0),(1,0))");
		GeoRay diagonalRay = add("Ray((1,2),(3,5))");
		LocusIntersectionTarget2D segmentTarget =
				LocusIntersectionTargets2D.capture(segment, "segment", 1);
		LocusIntersectionTarget2D rayTarget =
				LocusIntersectionTargets2D.capture(ray, "ray", 1);
		LocusIntersectionTarget2D diagonalRayTarget =
				LocusIntersectionTargets2D.capture(diagonalRay, "diagonal-ray", 1);
		assertTrue(segmentTarget.evaluateMembership(new LocusPoint2D(1, 0),
				1E-12).getStatus().name().equals("MEMBER"));
		assertFalse(segmentTarget.evaluateMembership(new LocusPoint2D(3, 0),
				1E-12).getStatus().name().equals("MEMBER"));
		assertTrue(rayTarget.evaluateMembership(new LocusPoint2D(1, 0),
				1E-12).getStatus().name().equals("MEMBER"));
		assertFalse(rayTarget.evaluateMembership(new LocusPoint2D(-1, 0),
				1E-12).getStatus().name().equals("MEMBER"));
		assertTrue(diagonalRayTarget.evaluateMembership(new LocusPoint2D(5, 8),
				1E-12).getStatus().name().equals("MEMBER"));
		assertFalse(diagonalRayTarget.evaluateMembership(new LocusPoint2D(-1, -1),
				1E-12).getStatus().name().equals("MEMBER"));
	}

	@Test
	void circleKeepsSignedRadialDistanceAndExtendedConicUsesDistinctFamily() {
		GeoConic circle = add("Circle((3,-2),2)");
		LocusIntersectionTarget2D target =
				LocusIntersectionTargets2D.capture(circle, "circle", 1);
		assertEquals(0, target.evaluateResidual(new LocusPoint2D(5, -2))
				.getNormalizedResidual(), 1E-15);
		assertEquals(-2, target.evaluateResidual(new LocusPoint2D(3, -2))
				.getNormalizedResidual(), 1E-15);
		GeoConic ellipse = add("x^2 + 2y^2 = 1");
		assertEquals(TargetFamily.ELLIPSE,
				LocusIntersectionTargets2D.capture(ellipse, "ellipse", 1)
						.getFamily());
	}

	@Test
	void analyticLineCapabilityPublishesCompleteEmpty() {
		G8BIntersectionFixtures.Fixture fixture = parabola("line-empty");
		GeoLine line = add("y=-1");
		LocusIntersectionResult2D result = result(fixture, line,
				G8BIntersectionFixtures.capability("empty/v1",
						G8BIntersectionFixtures::completeEmpty));
		assertEquals(GeometryKind.EMPTY, result.getGeometryKind());
		assertEquals(Completeness.COMPLETE,
				result.getCompletenessEvidence().getCompleteness());
		assertEquals(0,
				result.getCompletenessEvidence().getVerifiedRootCount());
	}

	@Test
	void analyticParabolaLineFindsTwoTransverseRoots() {
		G8BIntersectionFixtures.Fixture fixture = parabola("line-two");
		GeoLine line = add("y=1");
		List<G8BIntersectionFixtures.Root> roots = List.of(
				G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
						fixture.componentKey(), -1, "left"),
				G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
						fixture.componentKey(), 1, "right"));
		LocusIntersectionResult2D result = complete(fixture, line, roots);
		assertEquals(GeometryKind.FINITE, result.getGeometryKind());
		assertEquals(2, result.getFiniteSolutions().size());
		assertTrue(result.findPointAdmissibleSolution(result.getFiniteSolutions()
				.get(0).getIdentity().getRootToken()).isPresent());
		assertTrue(result.getFiniteSolutions().stream().allMatch(solution ->
				solution.getClassification().getContactClass()
						== ContactClass.TRANSVERSE_ESTABLISHED));
	}

	@Test
	void evenTangencyIsEstablishedWithoutSignChange() {
		G8BIntersectionFixtures.Fixture fixture = parabola("line-tangent");
		GeoLine line = add("y=0");
		LocusIntersectionResult2D result = complete(fixture, line,
				List.of(G8BIntersectionFixtures.Root.tangent(fixture.branchKey(),
						fixture.componentKey(), 0, "tangent", 2)));
		assertEquals(1, result.getFiniteSolutions().size());
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				result.getFiniteSolutions().get(0).getClassification()
						.getContactClass());
		assertEquals(2, result.getFiniteSolutions().get(0).getClassification()
				.getEstablishedMultiplicity().orElseThrow());
		G8BIntersectionFixtures.Fixture quartic =
				G8BIntersectionFixtures.single(getConstruction(), "line-order-four",
						-1, 1, true, true, false,
						(source, branch, parameter) -> new LocusPoint2D(parameter,
								parameter * parameter * parameter * parameter));
		LocusIntersectionResult2D fourthOrder = complete(quartic, line,
				List.of(G8BIntersectionFixtures.Root.tangent(quartic.branchKey(),
						quartic.componentKey(), 0, "order-four", 4)));
		assertEquals(4, fourthOrder.getFiniteSolutions().get(0)
				.getClassification().getEstablishedMultiplicity().orElseThrow());
	}

	@Test
	void nearTangencyDistinguishesTwoOneAndZeroRoots() {
		G8BIntersectionFixtures.Fixture fixture = parabola("near-tangent");
		LocusIntersectionResult2D above = complete(fixture, add("y=1E-12"),
				List.of(G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
						fixture.componentKey(), -1E-6, "left"),
						G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
								fixture.componentKey(), 1E-6, "right")));
		LocusIntersectionResult2D tangent = complete(fixture, add("y=0"),
				List.of(G8BIntersectionFixtures.Root.tangent(fixture.branchKey(),
						fixture.componentKey(), 0, "tangent", 2)));
		LocusIntersectionResult2D below = result(fixture, add("y=-1E-12"),
				G8BIntersectionFixtures.capability("empty/v1",
						G8BIntersectionFixtures::completeEmpty));
		assertEquals(2, above.getFiniteSolutions().size());
		assertEquals(1, tangent.getFiniteSolutions().size());
		assertEquals(GeometryKind.EMPTY, below.getGeometryKind());
	}

	@Test
	void includedEndpointIsRetainedAndOpenEndpointIsExcluded() {
		G8BIntersectionFixtures.Fixture included = G8BIntersectionFixtures.single(
				getConstruction(), "endpoint-included", 0, 1, true, true, false,
				(source, branch, parameter) ->
						new LocusPoint2D(parameter, parameter));
		LocusIntersectionResult2D result = complete(included, add("y=0"),
				List.of(G8BIntersectionFixtures.Root.simple(included.branchKey(),
						included.componentKey(), 0, "start")));
		assertEquals(DomainLocation.INCLUDED_ENDPOINT,
				result.getFiniteSolutions().get(0).getClassification()
						.getDomainLocation());
		G8BIntersectionFixtures.Fixture open = G8BIntersectionFixtures.single(
				getConstruction(), "endpoint-open", 0, 1, false, true, false,
				(source, branch, parameter) ->
						new LocusPoint2D(parameter, parameter));
		LocusIntersectionResult2D excluded = result(open, add("y=0"),
				G8BIntersectionFixtures.capability("open-empty/v1",
						G8BIntersectionFixtures::completeEmpty));
		assertEquals(GeometryKind.EMPTY, excluded.getGeometryKind());
	}

	@Test
	void segmentFiltersSupportRootsAndMarksIncludedTargetBoundary() {
		G8BIntersectionFixtures.Fixture fixture = parabolaMinusOne("segment");
		GeoSegment segment = add("Segment((0,0),(1,0))");
		List<G8BIntersectionFixtures.Root> roots = List.of(
				G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
						fixture.componentKey(), -1, "left"),
				G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
						fixture.componentKey(), 1, "right"));
		LocusIntersectionResult2D result = complete(fixture, segment, roots);
		assertEquals(1, result.getFiniteSolutions().size());
		assertEquals(1, result.getFiniteSolutions().get(0)
				.getRevisionEvidence().getSemanticParameter());
		assertTrue(result.getFiniteSolutions().get(0).getClassification()
				.isTargetIncludedBoundary());
		assertEquals(Completeness.COMPLETE,
				result.getCompletenessEvidence().getCompleteness());
	}

	@Test
	void rayFiltersBehindStartAndRetainsItsStart() {
		G8BIntersectionFixtures.Fixture fixture = parabolaMinusOne("ray");
		GeoRay ray = add("Ray((0,0),(1,0))");
		List<G8BIntersectionFixtures.Root> roots = List.of(
				G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
						fixture.componentKey(), -1, "left"),
				G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
						fixture.componentKey(), 1, "right"));
		LocusIntersectionResult2D result = complete(fixture, ray, roots);
		assertEquals(1, result.getFiniteSolutions().size());
		assertEquals(1, result.getFiniteSolutions().get(0)
				.getEvaluatedPoint().getX());
		G8BIntersectionFixtures.Fixture atStart = G8BIntersectionFixtures.single(
				getConstruction(), "ray-start", -1, 1, true, true, false,
				(source, branch, parameter) -> new LocusPoint2D(parameter, parameter));
		LocusIntersectionResult2D start = complete(atStart, ray,
				List.of(G8BIntersectionFixtures.Root.simple(atStart.branchKey(),
						atStart.componentKey(), 0, "start")));
		assertTrue(start.getFiniteSolutions().get(0).getClassification()
				.isTargetIncludedBoundary());
	}

	@Test
	void circleSupportsSecantTangentAndCompleteEmpty() {
		GeoConic circle = add("Circle((0,0),1)");
		G8BIntersectionFixtures.Fixture secant = lineLocus("circle-secant", 0);
		LocusIntersectionResult2D two = complete(secant, circle, List.of(
				G8BIntersectionFixtures.Root.simple(secant.branchKey(),
						secant.componentKey(), -1, "left"),
				G8BIntersectionFixtures.Root.simple(secant.branchKey(),
						secant.componentKey(), 1, "right")));
		G8BIntersectionFixtures.Fixture tangent = lineLocus("circle-tangent", 1);
		LocusIntersectionResult2D one = complete(tangent, circle,
				List.of(G8BIntersectionFixtures.Root.tangent(tangent.branchKey(),
						tangent.componentKey(), 0, "tangent", 2)));
		G8BIntersectionFixtures.Fixture disjoint = lineLocus("circle-empty", 2);
		LocusIntersectionResult2D zero = result(disjoint, circle,
				G8BIntersectionFixtures.capability("circle-empty/v1",
						G8BIntersectionFixtures::completeEmpty));
		assertEquals(2, two.getFiniteSolutions().size());
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				one.getFiniteSolutions().get(0).getClassification()
						.getContactClass());
		assertEquals(GeometryKind.EMPTY, zero.getGeometryKind());
		G8BIntersectionFixtures.Fixture endpoint =
				G8BIntersectionFixtures.single(getConstruction(), "circle-endpoint",
						1, 2, true, true, false,
						(source, branch, parameter) ->
								new LocusPoint2D(parameter, 0));
		LocusIntersectionResult2D endpointResult = complete(endpoint, circle,
				List.of(G8BIntersectionFixtures.Root.simple(endpoint.branchKey(),
						endpoint.componentKey(), 1, "circle-endpoint")));
		assertEquals(DomainLocation.INCLUDED_ENDPOINT,
				endpointResult.getFiniteSolutions().get(0).getClassification()
						.getDomainLocation());
	}

	@Test
	void periodicSeamIsCanonicalizedAndPublishedOnce() {
		G8BIntersectionFixtures.Fixture fixture = G8BIntersectionFixtures.single(
				getConstruction(), "periodic", 0, 2 * Math.PI, true, true, true,
				(source, branch, parameter) -> new LocusPoint2D(
						Math.cos(parameter), Math.sin(parameter)));
		LocusIntersectionResult2D result = complete(fixture, add("x=1"),
				List.of(G8BIntersectionFixtures.Root.tangent(fixture.branchKey(),
						fixture.componentKey(), 2 * Math.PI, "seam", 2)));
		assertEquals(1, result.getFiniteSolutions().size());
		assertEquals(0, result.getFiniteSolutions().get(0)
				.getRevisionEvidence().getSemanticParameter(), 0);
		assertEquals(DomainLocation.PERIODIC_SEAM,
				result.getFiniteSolutions().get(0).getClassification()
						.getDomainLocation());
	}

	@Test
	void equalCoordinatesOnDistinctBranchesRemainDistinctPreimages() {
		String identity = "two-branches";
		var provider = G8BIntersectionFixtures.provider(identity, -1, 1, true,
				true, false, Orientation.INCREASING);
		LocusBranch2D upper = G8BIntersectionFixtures.branch(identity + "/upper",
				provider, List.of(provider.getDeclaredDomain()),
				EnumSet.of(BranchProperty.FINITE));
		LocusBranch2D lower = G8BIntersectionFixtures.branch(identity + "/lower",
				provider, List.of(provider.getDeclaredDomain()),
				EnumSet.of(BranchProperty.FINITE));
		G8BIntersectionFixtures.Fixture fixture = G8BIntersectionFixtures.create(
				getConstruction(), identity, provider, List.of(upper, lower),
				(source, branch, parameter) -> new LocusPoint2D(parameter,
						branch.getBranchKey().endsWith("upper")
								? parameter * parameter : -parameter * parameter));
		List<G8BIntersectionFixtures.Root> roots = List.of(
				G8BIntersectionFixtures.Root.tangent(upper.getBranchKey(),
						IntersectionCapabilityContext2D.componentKey(
								upper.getBranchKey(), 0), 0, "upper-zero", 2),
				G8BIntersectionFixtures.Root.tangent(lower.getBranchKey(),
						IntersectionCapabilityContext2D.componentKey(
								lower.getBranchKey(), 0), 0, "lower-zero", 2));
		LocusIntersectionResult2D result = complete(fixture, add("y=0"), roots);
		assertEquals(2, result.getFiniteSolutions().size());
		assertEquals(result.getFiniteSolutions().get(0).getEvaluatedPoint().getX(),
				result.getFiniteSolutions().get(1).getEvaluatedPoint().getX());
	}

	@Test
	void disconnectedComponentsAreCoveredSeparately() {
		String identity = "components";
		var provider = G8BIntersectionFixtures.provider(identity, -2, 2, true,
				true, false, Orientation.INCREASING);
		List<LocusInterval2D> components = List.of(
				new LocusInterval2D(-2, -0.5, true, true),
				new LocusInterval2D(0.5, 2, true, true));
		LocusBranch2D branch = G8BIntersectionFixtures.branch(
				identity + "/branch", provider, components,
				EnumSet.of(BranchProperty.FINITE));
		G8BIntersectionFixtures.Fixture fixture = G8BIntersectionFixtures.create(
				getConstruction(), identity, provider, List.of(branch),
				(source, ignored, parameter) ->
						new LocusPoint2D(parameter, parameter * parameter - 1));
		LocusIntersectionResult2D result = complete(fixture, add("y=0"),
				List.of(G8BIntersectionFixtures.Root.simple(branch.getBranchKey(),
						IntersectionCapabilityContext2D.componentKey(
								branch.getBranchKey(), 0), -1, "left"),
						G8BIntersectionFixtures.Root.simple(branch.getBranchKey(),
								IntersectionCapabilityContext2D.componentKey(
										branch.getBranchKey(), 1), 1, "right")));
		assertEquals(2, result.getFiniteSolutions().size());
		assertEquals(2,
				result.getCompletenessEvidence().getCoveredComponentKeys().size());
	}

	@Test
	void evaluatorOnlyFindsEvenCandidateButNeverClaimsCompleteness() {
		G8BIntersectionFixtures.Fixture fixture = parabola("evaluator-tangent");
		LocusIntersectionResult2D result = result(fixture, add("y=0"), null);
		assertEquals(GeometryKind.FINITE, result.getGeometryKind());
		assertEquals(Completeness.NOT_ESTABLISHED,
				result.getCompletenessEvidence().getCompleteness());
		assertEquals(ContactClass.CONTACT_UNDETERMINED,
				result.getFiniteSolutions().get(0).getClassification()
						.getContactClass());
		assertEquals(MultiplicityStatus.NOT_ESTABLISHED,
				result.getFiniteSolutions().get(0).getClassification()
						.getMultiplicityStatus());
		assertFalse(result.findPointAdmissibleSolution(result.getFiniteSolutions()
				.get(0).getIdentity().getRootToken()).isPresent());
	}

	@Test
	void overlapAndInfiniteSetsCarryTypedEvidenceWithoutPoints() {
		G8BIntersectionFixtures.Fixture fixture = lineLocus("overlap", 0);
		GeoLine line = add("y=0");
		LocusIntersectionResult2D overlap = result(fixture, line,
				G8BIntersectionFixtures.capability("overlap/v1",
						context -> G8BIntersectionFixtures.overlap(context,
								GeometryKind.OVERLAP, fixture.branchKey(),
								fixture.componentKey())));
		LocusIntersectionResult2D infinite = result(fixture, line,
				G8BIntersectionFixtures.capability("infinite/v1",
						context -> G8BIntersectionFixtures.overlap(context,
								GeometryKind.INFINITELY_MANY, fixture.branchKey(),
								fixture.componentKey())));
		assertEquals(0, overlap.getFiniteSolutions().size());
		assertEquals(1, overlap.getOverlapEvidence().size());
		assertEquals(GeometryKind.INFINITELY_MANY, infinite.getGeometryKind());
	}

	@Test
	void incompleteCoverageCannotPublishFalseEmpty() {
		G8BIntersectionFixtures.Fixture fixture = parabola("coverage");
		GeoLine line = add("y=1");
		var incomplete = G8BIntersectionFixtures.capability("incomplete/v1",
				context -> new IntersectionCandidateSet2D(Completeness.INCOMPLETE,
						CompletenessMethod.INCOMPLETE_CANDIDATE_COVERAGE,
						GeometryKind.FINITE, SupportLevel.CERTIFIED,
						NumericGuarantee.CERTIFIED_ERROR_BOUND,
						List.of(fixture.componentKey()),
						List.of(candidate(fixture, -1, "left")), List.of(),
						List.of()));
		LocusIntersectionResult2D result = result(fixture, line, incomplete);
		assertEquals(Completeness.INCOMPLETE,
				result.getCompletenessEvidence().getCompleteness());
		assertEquals(GeometryKind.FINITE, result.getGeometryKind());
		assertTrue(result.findPointAdmissibleSolution(result.getFiniteSolutions()
				.get(0).getIdentity().getRootToken()).isPresent());
	}

	@Test
	void nonfiniteEvaluationDowngradesCompleteCandidateToUnresolved() {
		G8BIntersectionFixtures.Fixture fixture = G8BIntersectionFixtures.single(
				getConstruction(), "nonfinite", -1, 1, true, true, false,
				(source, branch, parameter) ->
						new LocusPoint2D(Double.NaN, parameter));
		LocusIntersectionResult2D result = complete(fixture, add("x=0"),
				List.of(G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
						fixture.componentKey(), 0, "bad")));
		assertEquals(GeometryKind.UNRESOLVED, result.getGeometryKind());
		assertEquals(Completeness.INCOMPLETE,
				result.getCompletenessEvidence().getCompleteness());
		assertTrue(result.getFiniteSolutions().isEmpty());
	}

	@Test
	void semanticDedupKeepsCloseDistinctKeysAndCollapsesDuplicateCandidate() {
		double separation = 1E-7;
		G8BIntersectionFixtures.Fixture fixture = G8BIntersectionFixtures.single(
				getConstruction(), "dedup", -1, 1, true, true, false,
				(source, branch, parameter) -> new LocusPoint2D(parameter,
						(parameter - separation / 2)
								* (parameter + separation / 2)));
		GeoLine line = add("y=0");
		List<G8BIntersectionFixtures.Root> distinct = List.of(
				G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
						fixture.componentKey(), -separation / 2, "left"),
				G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
						fixture.componentKey(), separation / 2, "right"));
		assertEquals(2, complete(fixture, line, distinct)
				.getFiniteSolutions().size());
		List<G8BIntersectionFixtures.Root> duplicate = List.of(distinct.get(0),
				distinct.get(0));
		assertEquals(1, complete(fixture, line, duplicate)
				.getFiniteSolutions().size());
	}

	@Test
	void deterministicWorkLimitProducesTypedAtomicFailure() {
		G8BIntersectionFixtures.Fixture fixture = parabola("budget");
		GeoLine line = add("y=1");
		LocusIntersectionPolicy2D initial = LocusIntersectionPolicy2D.initial(
				fixture.provider().getProviderId(),
				fixture.provider().getParameterDescriptor());
		LocusIntersectionWorkBudget2D tiny = new LocusIntersectionWorkBudget2D(
				8, 8, 8, 8, 8, 4, 4, 4, 8, 8, 8, 0, 2);
		LocusIntersectionPolicy2D policy = new LocusIntersectionPolicy2D(
				initial.getPolicyVersion(), initial.getProvenanceVersion(),
				initial.getRootParameterTolerance(),
				initial.getResidualTolerance(), initial.getTangencyTolerance(),
				initial.getDeduplicationTolerance(),
				initial.getContinuationTolerance(),
				initial.getCoordinateTolerance(), tiny);
		LocusIntersectionQuery2D query = new LocusIntersectionQuery2D(
				"budget-pair", "budget-lineage", fixture.locus().getLocusIdentity(),
				fixture.locus().getSemanticRevision(), "budget-target", 1,
				"topology", policy);
		LocusIntersectionTarget2D target = LocusIntersectionTargets2D.capture(
				line, "budget-target", 1);
		AtomicInteger tokens = new AtomicInteger();
		LocusIntersectionResult2D result = new LocusIntersectionSolver2D()
				.intersect(query, fixture.locus().getSemanticDefinition(), target,
						new IntersectionSourceBinding2D(query, target.getFamily()),
						null, () -> "token-" + tokens.incrementAndGet());
		assertEquals(ComputationStatus.WORK_LIMIT_REACHED,
				result.getComputationStatus());
		assertEquals(GeometryKind.UNRESOLVED, result.getGeometryKind());
		assertEquals(0, result.getFiniteSolutions().size());
		assertEquals(1, result.getWork().getPublishedSnapshots());
	}

	@Test
	void unsupportedDegenerateConicProducesClosedRichState() {
		G8BIntersectionFixtures.Fixture fixture = parabola("unsupported");
		GeoConic pairOfLines = add("x^2-y^2=0");
		LocusIntersectionResult2D result = result(fixture, pairOfLines, null);
		assertEquals(ComputationStatus.UNSUPPORTED,
				result.getComputationStatus());
		assertEquals(GeometryKind.UNRESOLVED, result.getGeometryKind());
	}

	@Test
	void functionalCountersProveNoForbiddenOrSharedAuthority() {
		G8BIntersectionFixtures.Fixture fixture = parabola("authority");
		LocusIntersectionResult2D result = complete(fixture, add("y=1"),
				List.of(G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
						fixture.componentKey(), -1, "left"),
						G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
								fixture.componentKey(), 1, "right")));
		assertTrue(result.getWork().hasZeroForbiddenAuthorityReads());
		assertEquals(0, result.getWork().getRetainedIndexEntries());
		assertEquals(2, result.getWork().getSemanticEvaluations());
		assertEquals(2, result.getWork().getResidualVerifications());
		assertEquals(2, result.getWork().getVerifiedSolutions());
	}

	private G8BIntersectionFixtures.Fixture parabola(String identity) {
		return G8BIntersectionFixtures.single(getConstruction(), identity, -2, 2,
				true, true, false, (source, branch, parameter) ->
						new LocusPoint2D(parameter, parameter * parameter));
	}

	private G8BIntersectionFixtures.Fixture parabolaMinusOne(String identity) {
		return G8BIntersectionFixtures.single(getConstruction(), identity, -2, 2,
				true, true, false, (source, branch, parameter) ->
						new LocusPoint2D(parameter, parameter * parameter - 1));
	}

	private G8BIntersectionFixtures.Fixture lineLocus(String identity,
			double height) {
		return G8BIntersectionFixtures.single(getConstruction(), identity, -2, 2,
				true, true, false, (source, branch, parameter) ->
						new LocusPoint2D(parameter, height));
	}

	private LocusIntersectionResult2D complete(
			G8BIntersectionFixtures.Fixture fixture, GeoElement target,
			List<G8BIntersectionFixtures.Root> roots) {
		return result(fixture, target,
				G8BIntersectionFixtures.capability("analytic-roots/v1",
						context -> G8BIntersectionFixtures.completeRoots(context,
								roots)));
	}

	private LocusIntersectionResult2D result(
			G8BIntersectionFixtures.Fixture fixture, GeoElement target,
			org.geocedg.common.kernel.locus.intersection.LocusIntersectionCapability2D
					capability) {
		AlgoLocusIntersectionV2 algorithm = new AlgoLocusIntersectionV2(
				getConstruction(), fixture.locus(), target,
				fixture.locus().getLocusIdentity() + "/pair/"
						+ getConstruction().getAlgoList().size(),
				"g8b-test-intersection-lineage", "g8b-test-target",
				"g8b-test-topology", capability, new GeoElement[0]);
		return algorithm.getResult().getIntersectionResult();
	}

	private static IntersectionCandidate2D candidate(
			G8BIntersectionFixtures.Fixture fixture, double parameter,
			String continuationKey) {
		return new IntersectionCandidate2D(fixture.branchKey(),
				fixture.componentKey(), parameter, java.util.OptionalDouble.empty(),
				new IntersectionParameterInterval2D(parameter - 1E-12,
						parameter + 1E-12),
				LocalIsolationStatus.ESTABLISHED,
				java.util.Optional.of(continuationKey),
				ContactClass.TRANSVERSE_ESTABLISHED,
				MultiplicityStatus.ESTABLISHED,
				java.util.OptionalInt.of(1),
				SolverMethod.CERTIFIED_INTERVAL,
				NumericGuarantee.CERTIFIED_ERROR_BOUND,
				LineageEventKind.APPEARED, List.of(), List.of());
	}

	private static LocusDifferentialEvaluation2D differential(double x,
			double y) {
		return new LocusDifferentialEvaluation2D(new LocusPoint2D(x, y),
				Regularity.REGULAR, NumericGuarantee.CERTIFIED_ERROR_BOUND,
				"analytic test differential");
	}
}
