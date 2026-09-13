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

import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Maximality;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricCapabilityHierarchy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricEngine2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexMode;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.TotalLocusMetricQuery;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/** Focused value/lifecycle contract for the PRE-G9B-S1-R2 certificate. */
class PreG9BS1R2ExistenceStructureTest extends BaseUnitTest {

	private static final String BRANCH = "fixture.main";
	private static final LocusInterval2D CANONICAL =
			new LocusInterval2D(-2, 2, true, false);

	@Test
	void canonicalDomainAndCompleteExistenceDomainRemainDistinct() {
		LocusInterval2D left = new LocusInterval2D(-2, -1, true, true);
		LocusInterval2D right = new LocusInterval2D(1, 2, true, false);
		LocusBranch2D branch = compatibilityBranch(List.of(left, right));

		assertEquals(CANONICAL, branch.getDeclaredDriverDomain());
		assertEquals(Completeness.COMPLETE,
				branch.getExistenceStructure().getCompleteness());
		assertEquals(List.of(left, right), branch.getValidDomainComponents());
		assertEquals(List.of(left, right),
				branch.getCertifiedContinuousValidComponents());
		assertEquals(List.of(CoverageState.CERTIFIED_EXISTS,
				CoverageState.CERTIFIED_DOES_NOT_EXIST,
				CoverageState.CERTIFIED_EXISTS), branch.getExistenceStructure()
						.getCoverage().stream().map(Coverage::getState).toList());
	}

	@Test
	void unknownDecompositionIsNeverPresentedAsCompleteEmptyDomain() {
		LocusExistenceStructure2D unknown =
				LocusExistenceStructure2D.notEstablished(BRANCH, CANONICAL,
						"fixture-proof-unavailable");
		LocusBranch2D branch = explicitBranch(unknown);

		assertEquals(Completeness.NOT_ESTABLISHED,
				unknown.getCompleteness());
		assertEquals(CoverageState.UNRESOLVED,
				unknown.getCoverage().get(0).getState());
		assertTrue(branch.getValidDomainComponents().isEmpty());
		assertTrue(branch.getCertifiedContinuousValidComponents().isEmpty());
	}

	@Test
	void localComponentsRemainUsableWithoutBridgingUnresolvedCoverage() {
		LocusInterval2D left = new LocusInterval2D(-2, -1, true, true);
		LocusInterval2D gap = new LocusInterval2D(-1, 1, false, false);
		LocusInterval2D right = new LocusInterval2D(1, 2, true, false);
		LocusExistenceStructure2D local = new LocusExistenceStructure2D(
				BRANCH, CANONICAL, 0,
				List.of(new Coverage(left, CoverageState.CERTIFIED_EXISTS,
						"interval-proof-left"),
						new Coverage(gap, CoverageState.UNRESOLVED,
								"boundary-not-established"),
						new Coverage(right, CoverageState.CERTIFIED_EXISTS,
								"interval-proof-right")),
				List.of(component(left, "left"), component(right, "right")),
				Completeness.NOT_ESTABLISHED, "fixture-certifier/v1");
		LocusBranch2D branch = explicitBranch(local);
		ExplicitNumericDomainProvider2D provider = provider();

		assertTrue(branch.getValidDomainComponents().isEmpty());
		assertEquals(List.of(left, right),
				branch.getCertifiedContinuousValidComponents());
		assertTrue(branch.containsValidParameter(-1, provider));
		assertTrue(branch.containsValidParameter(1, provider));
		assertFalse(branch.containsValidParameter(0, provider));
		assertEquals(7, local.bindToRevision(7).getSemanticRevision());
		assertEquals(local.getSemanticSignature(),
				local.bindToRevision(7).getSemanticSignature());
	}

	@Test
	void aComponentWithoutIntervalWideExistenceEvidenceIsRejected() {
		LocusInterval2D local = new LocusInterval2D(-1, 1, true, true);
		assertThrows(IllegalArgumentException.class,
				() -> new LocusExistenceStructure2D(BRANCH, CANONICAL, 0,
						List.of(new Coverage(CANONICAL, CoverageState.UNRESOLVED,
								"samples-only-not-authority")),
						List.of(component(local, "unsupported-sample-claim")),
						Completeness.NOT_ESTABLISHED, "fixture-certifier/v1"));
	}

	@Test
	void renderingConsumesLocalCertificatesWithoutBridgingUnknownGap() {
		LocusExistenceStructure2D local = localStructure();
		ExplicitNumericDomainProvider2D provider = provider();
		LocusBranch2D branch = explicitBranch(local);
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "r2-render-local", new GeoNumeric(getConstruction(), 0),
				provider, List.of(branch), (source, semanticBranch, parameter,
						session) -> new LocusPoint2D(parameter, parameter * parameter),
				"r2-render-local/v1");

		LocusRenderData2D render = new LocusRenderCache2D().getOrBuild(locus,
				new LocusRenderPolicy2D(1, 800, 600, 100, 100, 16));

		assertEquals(2, render.getVertices().stream()
				.filter(LocusRenderData2D.Vertex::startsSubpath).count());
		assertFalse(render.getVertices().stream()
				.anyMatch(vertex -> Math.abs(vertex.getSemanticParameter()) < 1));
		assertEquals(Completeness.NOT_ESTABLISHED, locus.getSemanticDefinition()
				.getBranches().get(0).getExistenceStructure().getCompleteness());
	}

	@Test
	void totalMetricRejectsIncompleteGlobalCoverageBeforeLocalEvaluation() {
		ExplicitNumericDomainProvider2D provider = provider();
		LocusDefinition2D definition = new LocusDefinition2D("r2-metric-local", 1,
				DefinitionStatus.VALID, provider,
				List.of(explicitBranch(localStructure())),
				(semantic, branch, parameter, session) ->
						LocusEvaluation2D.valid(
								new LocusPoint2D(parameter, parameter * parameter),
								Regularity.REGULAR,
								LocusQuality2D.analyticDoubleSemantic()),
				Determinism.POINTWISE_DETERMINISTIC, "r2-metric-local/v1",
				new org.geocedg.common.kernel.locus.LocusInstrumentation2D());
		LocusMetricInstrumentation2D instrumentation =
				new LocusMetricInstrumentation2D();
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D(
				definition.getLocusIdentity(), instrumentation);

		LocusMetricResult2D result = new LocusMetricEngine2D().compute(
				new TotalLocusMetricQuery(definition.getLocusIdentity(), 1,
						LocusMetricPolicy2D.initial()), definition,
				new LocusMetricCapabilityHierarchy2D(List.of()), owner,
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION, instrumentation);

		assertEquals(MetricComputationStatus.INVALID_QUERY,
				result.getComputationStatus());
		assertTrue(result.getDiagnostics().stream().anyMatch(diagnostic ->
				diagnostic.getCode() == MetricDiagnosticCode2D.INCOMPLETE_AGGREGATE));
	}

	@Test
	void localIntersectionSurvivesWithoutClaimingGlobalCompleteness() {
		ExplicitNumericDomainProvider2D provider = provider();
		LocusBranch2D branch = explicitBranch(localStructure());
		G8BIntersectionFixtures.Fixture fixture = G8BIntersectionFixtures.create(
				getConstruction(), "r2-intersection-local", provider, List.of(branch),
				(source, semanticBranch, parameter) ->
						new LocusPoint2D(parameter, parameter + 1.5));
		GeoElement target = add("y=0");
		AlgoLocusIntersectionV2 algorithm = new AlgoLocusIntersectionV2(
				getConstruction(), fixture.locus(), target,
				"r2-intersection-local/pair", "r2-intersection-local/lineage",
				"r2-intersection-local/target", "r2-intersection-local/topology",
				G8BIntersectionFixtures.capability("r2-local-root/v1", context ->
						G8BIntersectionFixtures.completeRoots(context,
								List.of(G8BIntersectionFixtures.Root.simple(
										fixture.branchKey(), fixture.componentKey(),
										-1.5, "r2-local-root")))),
				new GeoElement[0]);

		LocusIntersectionResult2D result = algorithm.getResult()
				.getIntersectionResult();
		assertEquals(GeometryKind.FINITE, result.getGeometryKind());
		assertEquals(1, result.getFiniteSolutions().size());
		assertEquals(
				IntersectionSemanticMetadata2D.Completeness.INCOMPLETE,
				result.getCompletenessEvidence().getCompleteness());
		assertTrue(result.getDiagnostics().stream().anyMatch(diagnostic ->
				diagnostic.getCode() == DiagnosticCode.COVERAGE_NOT_ESTABLISHED));
	}

	private static ContinuousComponent component(LocusInterval2D interval,
			String key) {
		return new ContinuousComponent(interval, key,
				"certified-interval-boundaries", Maximality.LOCAL_CERTIFIED_SUBINTERVAL,
				"outward-interval-proof/v1");
	}

	private static LocusExistenceStructure2D localStructure() {
		LocusInterval2D left = new LocusInterval2D(-2, -1, true, true);
		LocusInterval2D gap = new LocusInterval2D(-1, 1, false, false);
		LocusInterval2D right = new LocusInterval2D(1, 2, true, false);
		return new LocusExistenceStructure2D(BRANCH, CANONICAL, 0,
				List.of(new Coverage(left, CoverageState.CERTIFIED_EXISTS,
						"interval-proof-left"),
						new Coverage(gap, CoverageState.UNRESOLVED,
								"boundary-not-established"),
						new Coverage(right, CoverageState.CERTIFIED_EXISTS,
								"interval-proof-right")),
				List.of(component(left, "left"), component(right, "right")),
				Completeness.NOT_ESTABLISHED, "fixture-certifier/v1");
	}

	private static LocusBranch2D compatibilityBranch(
			List<LocusInterval2D> intervals) {
		return new LocusBranch2D(BRANCH, CANONICAL, intervals,
				Orientation.INCREASING, "fixture-provider/v1",
				LocusLineage2D.unchanged(), EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());
	}

	private static LocusBranch2D explicitBranch(
			LocusExistenceStructure2D structure) {
		return new LocusBranch2D(BRANCH, CANONICAL, structure,
				Orientation.INCREASING, "fixture-certifier/v1",
				LocusLineage2D.unchanged(), EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());
	}

	private static ExplicitNumericDomainProvider2D provider() {
		return new ExplicitNumericDomainProvider2D("fixture-provider/v1", CANONICAL,
				Orientation.INCREASING, false, 0);
	}
}
