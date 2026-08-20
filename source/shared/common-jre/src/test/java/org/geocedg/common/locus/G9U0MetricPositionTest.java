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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.metric.AbsentMetricValue2D;
import org.geocedg.common.kernel.locus.metric.BetweenPositionsMetricQuery;
import org.geocedg.common.kernel.locus.metric.FiniteMetricValue2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIntegrator2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricRoute2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricRouteResolver2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricCoverage;
import org.geocedg.common.kernel.locus.metric.MetricErrorEvidence2D;
import org.geocedg.common.kernel.locus.metric.MetricEvaluatorMethod2D;
import org.geocedg.common.kernel.locus.metric.MetricIntegrationResult2D;
import org.geocedg.common.kernel.locus.metric.MetricMethod2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.locus.metric.MetricProvenance2D;
import org.geocedg.common.kernel.locus.metric.MetricRectifiability;
import org.geocedg.common.kernel.locus.metric.MetricRepresentationRole2D;
import org.geocedg.common.kernel.locus.metric.MetricRouteStatus;
import org.geocedg.common.kernel.locus.metric.MetricUnit2D;
import org.geocedg.common.kernel.locus.metric.MetricValueKind;
import org.geocedg.common.kernel.locus.metric.MetricWorkBudget2D;
import org.geocedg.common.kernel.locus.metric.MetricWorkLimit2D;
import org.geocedg.common.kernel.locus.metric.OpenBoundaryPolicy;
import org.geocedg.common.kernel.locus.metric.PositiveInfinityMetricValue2D;
import org.geocedg.common.kernel.locus.metric.SamePositionPolicy;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;
import org.geocedg.common.kernel.locus.metric.TraversalOutcome;
import org.geogebra.common.kernel.algos.AlgoLengthLocus;
import org.geogebra.common.kernel.geos.GeoLocus;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Exact M11 semantic-position and rich/scalar metric scenarios. */
class G9U0MetricPositionTest extends G9U0PublicSurfaceTestBase {

	@Test
	void m01ExplicitSemanticPointCarriesDurableAddress() {
		GeoLocusV2 locus = createParabola();
		GeoPoint point = semanticPoint(locus, 0.5);
		AlgoSemanticLocusPoint2D parent =
				(AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
		assertNotNull(parent.getSemanticAddress());
		assertEquals(locus.getPersistentLocusId(),
				parent.getSemanticAddress().getSourceLocusId());
	}

	@Test
	void m02SelfIntersectionPositionsRemainDifferentPreimages() {
		GeoLocusV2 locus = createScalarLocus("L", "s", "Q",
				"(sin(s),sin(2*s))", "{false,{0,pi,true,true}}");
		GeoPoint first = semanticPoint(locus, 0);
		GeoPoint second = add("P2=Point(L,\"" + BRANCH + "\",pi)");
		AlgoSemanticLocusPoint2D a =
				(AlgoSemanticLocusPoint2D) first.getParentAlgorithm();
		AlgoSemanticLocusPoint2D b =
				(AlgoSemanticLocusPoint2D) second.getParentAlgorithm();
		assertEquals(first.getInhomX(), second.getInhomX(), 1E-14);
		assertNotEquals(a.getSemanticPosition().getProviderCanonicalParameter(),
				b.getSemanticPosition().getProviderCanonicalParameter());
	}

	@Test
	void m03StaleSemanticPositionPublishesNoCoordinate() {
		GeoLocusV2 locus = createDisconnectedLine();
		GeoPoint point = semanticPoint(locus, -1.5);
		GeoNumeric parameter = (GeoNumeric) point.getParentAlgorithm().getInput(2);
		parameter.setValue(0);
		parameter.updateCascade();
		assertFalse(point.isDefined());
	}

	@Test
	void m04RichTotalLengthIsNonnumericAuthority() {
		GeoLocusMetricResult segment = totalMetric(createLine());
		add("c=0");
		add("Qc=(cos(c),sin(c))");
		add("Dc={true,{0,2*pi,true,false}}");
		GeoLocusV2 circle = add("Lc=LocusV2(Qc,c,Dc)");
		GeoLocusMetricResult circleMetric = add("Mc=LocusLength(Lc)");
		add("e=0");
		add("Qe=(3*cos(e),2*sin(e))");
		add("De={true,{0,2*pi,true,false}}");
		GeoLocusV2 ellipse = add("Le=LocusV2(Qe,e,De)");
		GeoLocusMetricResult ellipseMetric = add("Me=LocusLength(Le)");

		assertRichTotal(segment, 4, 1E-10);
		assertRichTotal(circleMetric, 2 * Math.PI, 2E-7);
		assertRichTotal(ellipseMetric, 15.86543958929059, 2E-6);
		assertEquals(circle.getLocusIdentity(), circleMetric.getMetricResult()
				.getProvenance().getLocusIdentity());
		assertEquals(ellipse.getLocusIdentity(), ellipseMetric.getMetricResult()
				.getProvenance().getLocusIdentity());
		assertFalse(GeoNumeric.class.isInstance(segment));

		LocusMetricResult2D analytic = analyticTotal("m04-analytic", 0, 4,
				1, "m04/segment-closed-form/v1");
		assertEquals(4, analytic.getMetricValue().getFiniteValue().orElseThrow(),
				0);
		assertEquals(MetricEvaluatorMethod2D.ANALYTIC,
				analytic.getEvaluatorMethod());
		assertEquals(MetricMethod2D.CLOSED_FORM, analytic.getMetricMethod());
		assertEquals(NumericGuarantee.EXACT_ARITHMETIC,
				analytic.getErrorEvidence().getNumericGuarantee().orElseThrow());
	}

	@Test
	void m05RichPartialLengthDependsOnExactSemanticEndpoints() {
		GeoLocusV2 locus = createLine();
		GeoPoint first = semanticPoint(locus, -1);
		GeoPoint second = add("P2=Point(L,\"" + BRANCH + "\",1)");
		GeoLocusMetricResult metric = add("M=LocusLength(L,P,P2)");
		assertTrue(metric.isDefined());
		assertSame(first, metric.getParentAlgorithm().getInput(1));
		assertSame(second, metric.getParentAlgorithm().getInput(2));
		assertEquals(2, finite(metric), 1E-10);

		GeoLocusMetricResult reversed = add("Mreverse=LocusLength(L,P2,P)");
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				reversed.getMetricResult().getComputationStatus());
		assertEquals(TraversalOutcome.TARGET_NOT_REACHABLE,
				reversed.getMetricResult().getTraversalOutcome().orElseThrow());

		add("d=0");
		add("Qd=(d,0)");
		add("Dd={false,{-2,-1,true,true},{1,2,true,true}}");
		add("Ld=LocusV2(Qd,d,Dd)");
		add("Pd1=Point(Ld,\"" + BRANCH + "\",-1.5)");
		add("Pd2=Point(Ld,\"" + BRANCH + "\",1.5)");
		GeoLocusMetricResult acrossGap = add(
				"Mgap=LocusLength(Ld,Pd1,Pd2)");
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				acrossGap.getMetricResult().getComputationStatus());
		assertEquals(TraversalOutcome.DISCONTINUITY_ENCOUNTERED,
				acrossGap.getMetricResult().getTraversalOutcome().orElseThrow());

		add("w=0");
		add("Qw=(cos(w),sin(w))");
		add("Dw={true,{0,2*pi,true,false}}");
		add("Lw=LocusV2(Qw,w,Dw)");
		add("Pw1=Point(Lw,\"" + BRANCH + "\",3*pi/2)");
		add("Pw2=Point(Lw,\"" + BRANCH + "\",pi/2)");
		GeoLocusMetricResult wrapped = add(
				"Mwrap=LocusLength(Lw,Pw1,Pw2)");
		assertEquals(TraversalOutcome.TARGET_REACHED,
				wrapped.getMetricResult().getTraversalOutcome().orElseThrow());
		assertEquals(2, wrapped.getMetricResult().getContributions().size());
		assertEquals(Math.PI, finite(wrapped), 2E-7);

		LocusDefinition2D branches = G7BMetricFixtures.definitionWithBranches(
				"m05-branches", 1, false,
				List.of(G7BMetricFixtures.branch("m05-a", -2, 2,
						G7BMetricFixtures.components(-2, 2)),
						G7BMetricFixtures.branch("m05-b", -2, 2,
								G7BMetricFixtures.components(-2, 2))),
				parameter -> new LocusPoint2D(parameter, 0));
		MetricPositionBinding2D onA = G7BMetricFixtures.bind(branches, "m05-a",
				0);
		MetricPositionBinding2D onB = G7BMetricFixtures.bind(branches, "m05-b",
				0);
		BetweenPositionsMetricQuery crossBranch = new BetweenPositionsMetricQuery(
				onA, onB, TraversalDirection.FORWARD,
				OpenBoundaryPolicy.STRICT, SamePositionPolicy.ZERO_LENGTH,
				org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D.initial());
		LocusMetricRoute2D crossRoute = new LocusMetricRouteResolver2D()
				.resolve(crossBranch, branches);
		assertEquals(MetricRouteStatus.DIFFERENT_BRANCH,
				crossRoute.getRouteStatus());
	}

	@Test
	void m06GuardedStandardLengthDependsOnRichParent() {
		GeoNumeric scalar = scalarLength(createLine());
		GeoLocusMetricResult rich = (GeoLocusMetricResult)
				scalar.getParentAlgorithm().getInput(0);
		assertTrue(scalar.getParentAlgorithm() instanceof AlgoLocusMetricScalarAdapter);
		assertTrue(rich.isAuxiliaryObject());
		assertTrue(getConstruction().isInConstructionList(rich));
		var registry = getConstruction().getSpatialIdentityRegistry();
		var richId = registry.getPersistentGeoId(rich);
		var scalarId = registry.getPersistentGeoId(scalar);
		assertNotNull(richId);
		assertNotNull(scalarId);
		assertEquals(List.of(richId),
				registry.getGeoRecord(scalarId).getDependencies());
		assertEquals(4, scalar.getDouble(), 1E-10);

		assertTrue(richGate(new FiniteMetricValue2D(1), MetricCoverage.COMPLETE,
				MetricComputationStatus.SUCCESS, MetricRectifiability.RECTIFIABLE,
				Optional.empty(), MetricErrorEvidence2D.exact("m06/exact"))
				.isScalarAdmissible());
		assertFalse(richGate(new AbsentMetricValue2D(), MetricCoverage.COMPLETE,
				MetricComputationStatus.SUCCESS, MetricRectifiability.RECTIFIABLE,
				Optional.empty(), MetricErrorEvidence2D.notApplicable("m06/absent"))
				.isScalarAdmissible());
		assertFalse(richGate(new FiniteMetricValue2D(1), MetricCoverage.INCOMPLETE,
				MetricComputationStatus.SUCCESS, MetricRectifiability.RECTIFIABLE,
				Optional.empty(), MetricErrorEvidence2D.exact("m06/incomplete"))
				.isScalarAdmissible());
		assertFalse(richGate(new FiniteMetricValue2D(1), MetricCoverage.COMPLETE,
				MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				MetricRectifiability.RECTIFIABLE, Optional.empty(),
				MetricErrorEvidence2D.exact("m06/limit"))
				.isScalarAdmissible());
		assertFalse(richGate(new FiniteMetricValue2D(1), MetricCoverage.COMPLETE,
				MetricComputationStatus.SUCCESS,
				MetricRectifiability.NON_RECTIFIABLE, Optional.empty(),
				MetricErrorEvidence2D.exact("m06/nonrectifiable"))
				.isScalarAdmissible());
		assertFalse(richGate(new FiniteMetricValue2D(1), MetricCoverage.COMPLETE,
				MetricComputationStatus.SUCCESS, MetricRectifiability.RECTIFIABLE,
				Optional.of(TraversalOutcome.STOPPED_AT_BOUNDARY),
				MetricErrorEvidence2D.exact("m06/partial"))
				.isScalarAdmissible());
		assertFalse(richGate(new FiniteMetricValue2D(1), MetricCoverage.COMPLETE,
				MetricComputationStatus.SUCCESS, MetricRectifiability.RECTIFIABLE,
				Optional.empty(), MetricErrorEvidence2D.uncertified(
						"m06/uncertified"))
				.isScalarAdmissible());
	}

	@Test
	void m07NonrectifiableOrInfiniteMetricNeverFabricatesFiniteScalar() {
		GeoLocusV2 locus = createScalarLocus("L", "s", "Q", "(s,1/s)",
				"{false,{-1,1,true,true}}");
		GeoLocusMetricResult result = totalMetric(locus);
		assertFalse(result.isScalarAdmissible());
		GeoNumeric scalar = add("badScalar=Length(L)");
		assertFalse(scalar.isDefined());
		assertTrue(result.getMetricResult().getMetricValue().getKind()
				!= MetricValueKind.FINITE
				|| result.getMetricResult().getCoverage() != MetricCoverage.COMPLETE
				|| result.getMetricResult().getComputationStatus()
						!= MetricComputationStatus.SUCCESS);

		MetricIntegrationResult2D cusp = new LocusMetricIntegrator2D().integrate(
				parameter -> Math.hypot(2 * parameter,
						3 * parameter * parameter), -1, 1,
				org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D.initial(),
				3, new org.geocedg.common.kernel.locus.metric
						.LocusMetricInstrumentation2D());
		assertEquals(MetricComputationStatus.SUCCESS,
				cusp.getComputationStatus());
		assertEquals(2.8794197467431008,
				cusp.getMetricValue().getFiniteValue().orElseThrow(), 2E-9);

		LocusMetricResult2D unbounded = richGate(
				new PositiveInfinityMetricValue2D(), MetricCoverage.COMPLETE,
				MetricComputationStatus.SUCCESS,
				MetricRectifiability.NON_RECTIFIABLE, Optional.empty(),
				MetricErrorEvidence2D.notApplicable("m07/unbounded"));
		LocusMetricResult2D improper = richGate(new AbsentMetricValue2D(),
				MetricCoverage.INCOMPLETE,
				MetricComputationStatus.NUMERICAL_FAILURE,
				MetricRectifiability.UNDETERMINED, Optional.empty(),
				MetricErrorEvidence2D.notApplicable("m07/improper"));
		assertEquals(MetricValueKind.POSITIVE_INFINITY,
				unbounded.getMetricValue().getKind());
		assertFalse(unbounded.isScalarAdmissible());
		assertEquals(MetricValueKind.ABSENT,
				improper.getMetricValue().getKind());
		assertFalse(improper.isScalarAdmissible());
	}

	@Test
	void m08ErrorMetricRetainsRichStatusAndCoverage() {
		GeoLocusV2 locus = createScalarLocus("L", "s", "Q", "(s,1/s)",
				"{false,{-1,1,true,true}}");
		GeoLocusMetricResult result = totalMetric(locus);
		assertNotEquals(MetricComputationStatus.SUCCESS,
				result.getMetricResult().getComputationStatus());
		assertNotEquals(MetricCoverage.COMPLETE,
				result.getMetricResult().getCoverage());
		assertFalse(result.getMetricResult().getDiagnostics().isEmpty());

		java.util.function.DoubleUnaryOperator difficult =
				parameter -> 1 + Math.abs(Math.sin(10000 * parameter));
		org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D limited =
				metricPolicy("m08-limit/v1", new MetricWorkBudget2D(3, 100, 22));
		MetricIntegrationResult2D first = new LocusMetricIntegrator2D().integrate(
				difficult, 0, 1, limited, 1,
				new org.geocedg.common.kernel.locus.metric
						.LocusMetricInstrumentation2D());
		MetricIntegrationResult2D second = new LocusMetricIntegrator2D().integrate(
				difficult, 0, 1, limited, 1,
				new org.geocedg.common.kernel.locus.metric
						.LocusMetricInstrumentation2D());
		assertEquals(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				first.getComputationStatus());
		assertEquals(MetricWorkLimit2D.MAXIMUM_EVALUATIONS,
				first.getExhaustedWorkLimit());
		assertEquals(first.getComputationStatus(), second.getComputationStatus());
		assertEquals(first.getExhaustedWorkLimit(), second.getExhaustedWorkLimit());
		assertEquals(first.getMetricValue().getKind(),
				second.getMetricValue().getKind());
	}

	@Test
	void m09ZoomAndDpiCannotChangeMetricValue() {
		GeoLocusV2 locus = createLine();
		GeoLocusMetricResult metric = totalMetric(locus);
		double before = finite(metric);
		getApp().getActiveEuclidianView().setCoordSystem(400, 300, 20, 20);
		metric.getParentAlgorithm().update();
		getApp().getActiveEuclidianView().setCoordSystem(400, 300, 250, 250);
		metric.getParentAlgorithm().update();
		assertEquals(before, finite(metric), 0);
		assertEquals(0, locus.getMetricInstrumentation().snapshot().getRenderReads());
	}

	@Test
	void m10LegacyLengthDispatchRemainsUnchanged() {
		add("legacyPath=Circle((0,0),2)");
		add("LegacyDriver=Point(legacyPath)");
		add("LegacyDependent=LegacyDriver+(1,0)");
		GeoLocus legacy = add(
				"legacy=Locus(LegacyDependent,LegacyDriver)");
		GeoNumeric length = add("legacyLength=Length(legacy)");
		assertTrue(legacy.getPointLength() > 0);
		assertEquals(legacy.getPointLength(), length.getDouble(), 0);
		assertTrue(length.getParentAlgorithm() instanceof AlgoLengthLocus);
		assertFalse(length.getParentAlgorithm() instanceof AlgoLocusMetricScalarAdapter);
	}

	@Test
	void m11DevelopedAndDirectLineMetricAgree() {
		add("sd=0");
		add("Qdirect=(cos(sd),sin(sd))");
		add("Ddirect={true,{0,2*pi,true,false}}");
		GeoLocusV2 direct = add("Ldirect=LocusV2(Qdirect,sd,Ddirect)");
		GeoLocusMetricResult directMetric = add(
				"Mdirect=LocusLength(Ldirect)");

		add("sf=0");
		add("Qdeveloped=(sf,0)");
		add("Ddeveloped={false,{0,2*pi,true,true}}");
		GeoLocusV2 developed = add(
				"Ldeveloped=LocusV2(Qdeveloped,sf,Ddeveloped)");
		GeoLocusMetricResult developedMetric = add(
				"Mdeveloped=LocusLength(Ldeveloped)");

		assertEquals(2 * Math.PI, finite(directMetric), 2E-7);
		assertEquals(finite(directMetric), finite(developedMetric), 2E-7);
		MetricProvenance2D directProvenance =
				directMetric.getMetricResult().getProvenance();
		MetricProvenance2D developedProvenance =
				developedMetric.getMetricResult().getProvenance();
		assertEquals(direct.getLocusIdentity(),
				directProvenance.getLocusIdentity());
		assertEquals(developed.getLocusIdentity(),
				developedProvenance.getLocusIdentity());
		assertNotEquals(directProvenance.getLocusIdentity(),
				developedProvenance.getLocusIdentity());
		assertNotEquals(direct.getPersistentLocusId(),
				developed.getPersistentLocusId());
	}

	private LocusMetricResult2D analyticTotal(String identity, double lower,
			double upper, double scale, String capability) {
		LocusDefinition2D definition = G7BMetricFixtures.definition(identity, 1,
				false, G7BMetricFixtures.components(lower, upper),
				parameter -> new LocusPoint2D(parameter, 0));
		org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentation2D
				instrumentation = new org.geocedg.common.kernel.locus.metric
						.LocusMetricInstrumentation2D();
		org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D owner =
				new org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D(
						identity, instrumentation);
		try (org.geocedg.common.kernel.locus.metric.LocusMetricOwnerLease2D lease =
					owner.acquireLease()) {
			return new org.geocedg.common.kernel.locus.metric.LocusMetricEngine2D()
					.compute(new org.geocedg.common.kernel.locus.metric
							.TotalLocusMetricQuery(identity, 1,
									org.geocedg.common.kernel.locus.metric
											.LocusMetricPolicy2D.initial()),
							definition, G7BMetricFixtures.analytic(scale, capability),
							owner, org.geocedg.common.kernel.locus.metric
									.LocusMetricIndexMode.LAZY_COMPONENT_REVISION,
							instrumentation, "m04-analytic-consumer");
		}
	}

	private static void assertRichTotal(GeoLocusMetricResult result,
			double expected, double tolerance) {
		assertTrue(result.isDefined());
		LocusMetricResult2D rich = result.getMetricResult();
		assertEquals(MetricValueKind.FINITE, rich.getMetricValue().getKind());
		assertEquals(expected, rich.getMetricValue().getFiniteValue().orElseThrow(),
				tolerance);
		assertEquals(MetricCoverage.COMPLETE, rich.getCoverage());
		assertEquals(MetricComputationStatus.SUCCESS,
				rich.getComputationStatus());
		assertEquals(MetricRectifiability.RECTIFIABLE,
				rich.getRectifiability());
		assertEquals(MetricRepresentationRole2D.SEMANTIC_METRIC,
				rich.getRepresentationRole());
		assertEquals(MetricUnit2D.CONSTRUCTION_LENGTH_UNIT, rich.getUnit());
		assertNotNull(rich.getEvaluatorMethod());
		assertNotNull(rich.getMetricMethod());
		assertNotNull(rich.getErrorEvidence());
		assertFalse(rich.getErrorEvidence().getAssumptions().isEmpty());
		assertNotNull(rich.getProvenance());
		assertFalse(rich.getContributions().isEmpty());
	}

	private static LocusMetricResult2D richGate(
			org.geocedg.common.kernel.locus.metric.MetricValue2D value,
			MetricCoverage coverage, MetricComputationStatus status,
			MetricRectifiability rectifiability,
			Optional<TraversalOutcome> traversal,
			MetricErrorEvidence2D evidence) {
		return new LocusMetricResult2D(value, coverage, status, rectifiability,
				traversal, ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
				MetricEvaluatorMethod2D.ANALYTIC, MetricMethod2D.CLOSED_FORM,
				MetricRepresentationRole2D.SEMANTIC_METRIC, evidence,
				MetricUnit2D.CONSTRUCTION_LENGTH_UNIT,
				new MetricProvenance2D("m06-gate", 1, "m06/capability/v1",
						"m06/algorithm/v1", "m06/policy/v1"),
				List.of(), List.of());
	}

	private static org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D
			metricPolicy(String version, MetricWorkBudget2D budget) {
		org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D base =
				org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D.initial();
		return new org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D(
				base.getAbsoluteTolerance(), base.getRelativeTolerance(), budget,
				base.getMetricAlgorithmVersion(), version,
				base.getTolerancePolicyVersion(), base.getMultiplicityPolicy(),
				base.getImproperLimitPolicy(), base.getEvaluatorOnlyPolicy());
	}

	private static double finite(GeoLocusMetricResult result) {
		return result.getMetricResult().getMetricValue().getFiniteValue()
				.orElseThrow();
	}
}
