/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.OverlapRelationKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.OverlapStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionCandidateSet2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionEvidence2D;
import org.geocedg.common.locus.G8C2IntersectionTestSupport.PairRoot;
import org.geogebra.common.BaseUnitTest;
import org.junit.jupiter.api.Test;

/** Productive G8C2 pair geometry, evidence and closed-taxonomy tests. */
class G8C2LocusPairKernelTest extends BaseUnitTest {

	@Test
	void evaluatorBoxesFindLineLikeCrossingWithoutFalseCompleteness() {
		var horizontal = G8C2IntersectionTestSupport.line(getConstruction(),
				"line-A", -1, 1, 0, true);
		var vertical = G8C2IntersectionTestSupport.line(getConstruction(),
				"line-B", -1, 1, 0, false);
		LocusIntersectionResult2D result = G8C2IntersectionTestSupport
				.algorithm(getConstruction(), horizontal, vertical, "line-boxes")
				.getResult().getIntersectionResult();
		assertEquals(ComputationStatus.SUCCESS, result.getComputationStatus());
		assertEquals(GeometryKind.FINITE, result.getGeometryKind());
		assertEquals(Completeness.NOT_ESTABLISHED,
				result.getCompletenessEvidence().getCompleteness());
		assertEquals(1, result.getFiniteSolutions().size());
		assertEquals(0, result.getFiniteSolutions().get(0).getEvaluatedPoint()
				.getX(), 2E-12);
		assertEquals(LocalIsolationStatus.NOT_ESTABLISHED,
				result.getFiniteSolutions().get(0).getPairEvidence().orElseThrow()
						.getLocalIsolation().getStatus());
	}

	@Test
	void analyticTransversePairPublishesCompleteTwoSidedEvidence() {
		var first = G8C2IntersectionTestSupport.line(getConstruction(),
				"analytic-A", -1, 1, 0, true);
		var second = G8C2IntersectionTestSupport.line(getConstruction(),
				"analytic-B", -1, 1, 0, false);
		LocusIntersectionResult2D result = result(first, second,
				"analytic-cross", List.of(
						G8C2IntersectionTestSupport.simple(0, 0, "cross")),
				Completeness.COMPLETE);
		assertEquals(Completeness.COMPLETE,
				result.getCompletenessEvidence().getCompleteness());
		var solution = result.getFiniteSolutions().get(0);
		LocusPairIntersectionEvidence2D evidence = solution.getPairEvidence()
				.orElseThrow();
		assertEquals("analytic-A", evidence.getFirst().getLocusIdentity());
		assertEquals("analytic-B", evidence.getSecond().getLocusIdentity());
		assertEquals(ContactClass.TRANSVERSE_ESTABLISHED,
				solution.getClassification().getContactClass());
		assertEquals(LocalIsolationStatus.ESTABLISHED,
				evidence.getLocalIsolation().getStatus());
		assertTrue(result.findPointAdmissibleSolution(
				solution.getIdentity().getRootToken()).isPresent());
	}

	@Test
	void argumentReversalKeepsTokenAndReversesOrderedEvidence() {
		var first = G8C2IntersectionTestSupport.line(getConstruction(),
				"order-A", -1, 1, 0, true);
		var second = G8C2IntersectionTestSupport.line(getConstruction(),
				"order-B", -1, 1, 0, false);
		var capability = G8C2IntersectionTestSupport.capability("order/v1",
				context -> G8C2IntersectionTestSupport.roots(context,
						List.of(G8C2IntersectionTestSupport.simple(0, 0,
								"order-root")), Completeness.COMPLETE));
		LocusIntersectionResult2D forward = G8C2IntersectionTestSupport.result(
				getConstruction(), first, second, "order", capability);
		LocusIntersectionResult2D reverse = G8C2IntersectionTestSupport.result(
				getConstruction(), second, first, "order", capability);
		var forwardSolution = forward.getFiniteSolutions().get(0);
		var reverseSolution = reverse.getFiniteSolutions().get(0);
		assertEquals(forwardSolution.getIdentity().getRootToken(),
				reverseSolution.getIdentity().getRootToken());
		assertEquals(forward.getSourceBinding().getSourcePairIdentity(),
				reverse.getSourceBinding().getSourcePairIdentity());
		LocusPairIntersectionEvidence2D reversed = forwardSolution
				.getPairEvidence().orElseThrow().reversed();
		assertEquals(forwardSolution.getPairEvidence().orElseThrow().getFirst()
				.getLocusIdentity(), reversed.getSecond().getLocusIdentity());
		assertEquals(-forwardSolution.getPairEvidence().orElseThrow()
				.getNormalizedTangentDeterminant().getAsDouble(),
				reversed.getNormalizedTangentDeterminant().getAsDouble(), 0);
	}

	@Test
	void periodicCirclePairKeepsBothConstructiveSolutions() {
		var first = circle("circle-A", 0);
		var second = circle("circle-B", 1);
		var roots = List.of(
				G8C2IntersectionTestSupport.simple(Math.PI / 3,
						2 * Math.PI / 3, "upper"),
				G8C2IntersectionTestSupport.simple(-Math.PI / 3,
						-2 * Math.PI / 3, "lower"));
		LocusIntersectionResult2D result = result(first, second, "circles",
				roots, Completeness.COMPLETE);
		assertEquals(2, result.getFiniteSolutions().size());
		assertNotEquals(result.getFiniteSolutions().get(0).getIdentity()
				.getRootToken(), result.getFiniteSolutions().get(1).getIdentity()
						.getRootToken());
		assertTrue(result.getFiniteSolutions().stream().allMatch(solution ->
				Math.abs(solution.getEvaluatedPoint().getX() - 0.5) < 2E-12));
	}

	@Test
	void certifiedTangencyIsFoundWithoutSignChange() {
		var parabola = G8C2IntersectionTestSupport.curve(getConstruction(),
				"tangent-A", -1, 1, false,
				(source, branch, parameter) ->
						new LocusPoint2D(parameter, parameter * parameter));
		var axis = G8C2IntersectionTestSupport.line(getConstruction(),
				"tangent-B", -1, 1, 0, true);
		LocusIntersectionResult2D result = result(parabola, axis, "tangent",
				List.of(G8C2IntersectionTestSupport.tangent(0, 0, "tangent", 2,
						false)), Completeness.NOT_ESTABLISHED);
		var solution = result.getFiniteSolutions().get(0);
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				solution.getClassification().getContactClass());
		assertEquals(LocalIsolationStatus.NOT_ESTABLISHED,
				solution.getPairEvidence().orElseThrow().getLocalIsolation()
						.getStatus());
		assertFalse(result.findPointAdmissibleSolution(
				solution.getIdentity().getRootToken()).isPresent());
	}

	@Test
	void certifiedHigherContactRetainsEstablishedMultiplicity() {
		var quartic = G8C2IntersectionTestSupport.curve(getConstruction(),
				"quartic-A", -1, 1, false,
				(source, branch, parameter) ->
						new LocusPoint2D(parameter, Math.pow(parameter, 4)));
		var axis = G8C2IntersectionTestSupport.line(getConstruction(),
				"quartic-B", -1, 1, 0, true);
		LocusIntersectionResult2D result = result(quartic, axis, "quartic",
				List.of(G8C2IntersectionTestSupport.tangent(0, 0, "quartic", 4,
						false)), Completeness.NOT_ESTABLISHED);
		assertEquals(4, result.getFiniteSolutions().get(0).getClassification()
				.getEstablishedMultiplicity().orElseThrow());
	}

	@Test
	void closeRootsAreDeduplicatedOnlyInSemanticPairSpace() {
		double epsilon = 1E-4;
		var curve = G8C2IntersectionTestSupport.curve(getConstruction(),
				"close-A", -1, 1, false,
				(source, branch, parameter) -> new LocusPoint2D(parameter,
						parameter * parameter - epsilon * epsilon));
		var axis = G8C2IntersectionTestSupport.line(getConstruction(),
				"close-B", -1, 1, 0, true);
		LocusIntersectionResult2D result = result(curve, axis, "close",
				List.of(G8C2IntersectionTestSupport.simple(-epsilon, -epsilon,
						"close-left"),
						G8C2IntersectionTestSupport.simple(epsilon, epsilon,
								"close-right")), Completeness.COMPLETE);
		assertEquals(2, result.getFiniteSolutions().size());
	}

	@Test
	void identicalCoordinatesWithDistinctPairPreimagesKeepDistinctTokens() {
		var folded = G8C2IntersectionTestSupport.curve(getConstruction(),
				"fold-A", -2, 2, false,
				(source, branch, parameter) ->
						new LocusPoint2D(parameter * parameter, 0));
		var vertical = G8C2IntersectionTestSupport.line(getConstruction(),
				"fold-B", -1, 1, 1, false);
		LocusIntersectionResult2D result = result(folded, vertical, "fold",
				List.of(G8C2IntersectionTestSupport.simple(-1, 0, "negative"),
						G8C2IntersectionTestSupport.simple(1, 0, "positive")),
				Completeness.COMPLETE);
		assertEquals(result.getFiniteSolutions().get(0).getEvaluatedPoint(),
				result.getFiniteSolutions().get(1).getEvaluatedPoint());
		assertNotEquals(result.getFiniteSolutions().get(0).getIdentity()
				.getRootToken(), result.getFiniteSolutions().get(1).getIdentity()
						.getRootToken());
	}

	@Test
	void completenessStatesRemainOrthogonalToOptionBAdmissibility() {
		for (Completeness completeness : Completeness.values()) {
			var first = G8C2IntersectionTestSupport.line(getConstruction(),
					"option-A-" + completeness, -1, 1, 0, true);
			var second = G8C2IntersectionTestSupport.line(getConstruction(),
					"option-B-" + completeness, -1, 1, 0, false);
			LocusIntersectionResult2D result = result(first, second,
					"option-" + completeness,
					List.of(G8C2IntersectionTestSupport.simple(0, 0,
							"option-root")), completeness);
			String token = result.getFiniteSolutions().get(0).getIdentity()
					.getRootToken();
			assertTrue(result.findPointAdmissibleSolution(token).isPresent());
			assertEquals(completeness,
					result.getCompletenessEvidence().getCompleteness());
		}
	}

	@Test
	void completeEmptyIsSuccessWhileUnknownCoverageIsUnresolved() {
		var first = G8C2IntersectionTestSupport.line(getConstruction(),
				"empty-A", -1, 1, 0, true);
		var second = G8C2IntersectionTestSupport.line(getConstruction(),
				"empty-B", -1, 1, 2, true);
		LocusIntersectionResult2D empty = G8C2IntersectionTestSupport.result(
				getConstruction(), first, second, "empty",
				G8C2IntersectionTestSupport.capability("empty/v1",
						G8C2IntersectionTestSupport::completeEmpty));
		assertEquals(GeometryKind.EMPTY, empty.getGeometryKind());
		assertEquals(Completeness.COMPLETE,
				empty.getCompletenessEvidence().getCompleteness());
		var unknownCapability = G8C2IntersectionTestSupport.capability(
				"unknown/v1", context -> new LocusPairIntersectionCandidateSet2D(
						Completeness.NOT_ESTABLISHED,
						CompletenessMethod.NOT_ESTABLISHED,
						GeometryKind.UNRESOLVED,
						SupportLevel.VERIFIED_UNCERTIFIED,
						org.geocedg.common.kernel.locus.LocusSemanticMetadata2D
								.NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
						context.getAllComponentPairKeys(), Collections.emptyList(),
						Collections.emptyList(), Collections.emptyList()));
		LocusIntersectionResult2D unknown = G8C2IntersectionTestSupport.result(
				getConstruction(), first, second, "unknown", unknownCapability);
		assertEquals(GeometryKind.UNRESOLVED, unknown.getGeometryKind());
		assertEquals(Completeness.NOT_ESTABLISHED,
				unknown.getCompletenessEvidence().getCompleteness());
	}

	@Test
	void overlapTaxonomyNeverManufacturesFiniteSamples() {
		for (OverlapStatus status : OverlapStatus.values()) {
			var first = G8C2IntersectionTestSupport.line(getConstruction(),
					"overlap-A-" + status, -1, 1, 0, true);
			var second = G8C2IntersectionTestSupport.line(getConstruction(),
					"overlap-B-" + status, -1, 1, 0, true);
			GeometryKind requested = status == OverlapStatus.OVERLAP_ESTABLISHED
					? GeometryKind.OVERLAP : GeometryKind.UNSUPPORTED_OVERLAP;
			var capability = G8C2IntersectionTestSupport.capability(
					"overlap/" + status, context ->
							G8C2IntersectionTestSupport.overlap(context, status,
									OverlapRelationKind.FULL_COMPONENT,
									requested));
			LocusIntersectionResult2D result = G8C2IntersectionTestSupport
					.result(getConstruction(), first, second,
							"overlap-" + status, capability);
			assertEquals(0, result.getFiniteSolutions().size());
			assertFalse(result.getOverlapEvidence().isEmpty());
		}
	}

	@Test
	void reverseAndPartialOverlapKeepTypedComponentRelations() {
		for (OverlapRelationKind relation : List.of(
				OverlapRelationKind.REVERSE_PARAMETERIZATION,
				OverlapRelationKind.PARTIAL_COMPONENT,
				OverlapRelationKind.REPEATED_TRAVERSAL)) {
			var first = G8C2IntersectionTestSupport.line(getConstruction(),
					"relation-A-" + relation, -1, 1, 0, true);
			var second = G8C2IntersectionTestSupport.line(getConstruction(),
					"relation-B-" + relation, -1, 1, 0, true);
			var capability = G8C2IntersectionTestSupport.capability(
					"relation/" + relation, context ->
							G8C2IntersectionTestSupport.overlap(context,
									OverlapStatus.OVERLAP_ESTABLISHED, relation,
									GeometryKind.OVERLAP));
			var evidence = G8C2IntersectionTestSupport.result(getConstruction(),
					first, second, "relation-" + relation, capability)
					.getOverlapEvidence().get(0);
			assertEquals(relation, evidence.getRelationKind());
		}
	}

	@Test
	void mixedFiniteAndOverlapRetainsBothContributions() {
		var first = G8C2IntersectionTestSupport.line(getConstruction(),
				"mixed-A", -1, 1, 0, true);
		var second = G8C2IntersectionTestSupport.line(getConstruction(),
				"mixed-B", -1, 1, 0, false);
		var capability = G8C2IntersectionTestSupport.capability("mixed/v1",
				context -> G8C2IntersectionTestSupport.mixed(context,
						G8C2IntersectionTestSupport.simple(0, 0, "isolated")));
		LocusIntersectionResult2D result = G8C2IntersectionTestSupport.result(
				getConstruction(), first, second, "mixed", capability);
		assertEquals(GeometryKind.MIXED_FINITE_OVERLAP,
				result.getGeometryKind());
		assertEquals(1, result.getFiniteSolutions().size());
		assertEquals(1, result.getOverlapEvidence().size());
		String token = result.getFiniteSolutions().get(0).getIdentity()
				.getRootToken();
		assertTrue(result.findPointAdmissibleSolution(token).isPresent());
	}

	@Test
	void evaluatorAgreementOnlyProducesSuspectedOverlap() {
		var first = G8C2IntersectionTestSupport.line(getConstruction(),
				"suspect-A", -1, 1, 0, true);
		var second = G8C2IntersectionTestSupport.line(getConstruction(),
				"suspect-B", -1, 1, 0, true);
		LocusIntersectionResult2D result = G8C2IntersectionTestSupport
				.algorithm(getConstruction(), first, second, "suspect")
				.getResult().getIntersectionResult();
		assertEquals(GeometryKind.UNSUPPORTED_OVERLAP,
				result.getGeometryKind());
		assertEquals(OverlapStatus.OVERLAP_SUSPECTED_NOT_ESTABLISHED,
				result.getOverlapEvidence().get(0).getStatus());
		assertEquals(0, result.getFiniteSolutions().size());
	}

	@Test
	void componentProductCoverageIsExplicit() {
		var firstProvider = G8BIntersectionFixtures.provider("components-A", -2,
				2, true, true, false, Orientation.INCREASING);
		var firstBranch = G8BIntersectionFixtures.branch("components-A/branch",
				firstProvider,
				List.of(new LocusInterval2D(-2, -1, true, true),
						new LocusInterval2D(1, 2, true, true)),
				EnumSet.of(BranchProperty.FINITE));
		var first = G8BIntersectionFixtures.create(getConstruction(),
				"components-A", firstProvider, List.of(firstBranch),
				(source, branch, parameter) -> new LocusPoint2D(parameter, 0));
		var secondProvider = G8BIntersectionFixtures.provider("components-B",
				-2, 2, true, true, false, Orientation.INCREASING);
		var secondBranch = G8BIntersectionFixtures.branch("components-B/branch",
				secondProvider,
				List.of(new LocusInterval2D(-2, 0, true, true),
						new LocusInterval2D(0, 2, true, true)),
				EnumSet.of(BranchProperty.FINITE));
		var second = G8BIntersectionFixtures.create(getConstruction(),
				"components-B", secondProvider, List.of(secondBranch),
				(source, branch, parameter) -> new LocusPoint2D(0, parameter));
		var capability = G8C2IntersectionTestSupport.capability(
				"component-coverage/v1", context -> {
					assertEquals(4, context.getAllComponentPairKeys().size());
					return G8C2IntersectionTestSupport.completeEmpty(context);
				});
		LocusIntersectionResult2D result = G8C2IntersectionTestSupport.result(
				getConstruction(), first, second, "components", capability);
		assertEquals(4, result.getCompletenessEvidence()
				.getCoveredComponentKeys().size());
	}

	@Test
	void upstreamFiniteDomainContractRejectsArbitraryUnboundedWindows() {
		assertThrows(IllegalArgumentException.class, () ->
				new LocusInterval2D(Double.NEGATIVE_INFINITY, 1, false, true));
		assertThrows(IllegalArgumentException.class, () ->
				new LocusInterval2D(-1, Double.POSITIVE_INFINITY, true, false));
	}

	private G8BIntersectionFixtures.Fixture circle(String identity,
			double centerX) {
		return G8C2IntersectionTestSupport.curve(getConstruction(), identity,
				-Math.PI, Math.PI, true,
				(source, branch, parameter) -> new LocusPoint2D(
						centerX + Math.cos(parameter), Math.sin(parameter)));
	}

	private LocusIntersectionResult2D result(
			G8BIntersectionFixtures.Fixture first,
			G8BIntersectionFixtures.Fixture second, String identity,
			List<PairRoot> roots, Completeness completeness) {
		var capability = G8C2IntersectionTestSupport.capability(identity + "/v1",
				context -> G8C2IntersectionTestSupport.roots(context, roots,
						completeness));
		return G8C2IntersectionTestSupport.result(getConstruction(), first,
				second, identity, capability);
	}
}
