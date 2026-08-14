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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.export.GeometryExportModel;
import org.geocedg.common.export.GeometryExportModel.DiagnosticCode;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusBranchSnapshot2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDualRunDiagnostic2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.LineageTransition;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusSourceSnapshot2D;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.kernel.locus.LocusValidationTolerance2D;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLocusNDInterface;
import org.geogebra.common.kernel.geos.GeoLocusable;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.plugin.GeoClass;
import org.junit.jupiter.api.Test;

class LocusV2KernelIntegrationTest extends BaseUnitTest {
	private static final String BRANCH_KEY = "fixture.sheet.main";
	private static final double EPS_DOMAIN = 1E-14;

	@Test
	void levelAAnalyticFixturesSatisfyConstructionRelations() {
		assertAnalytic("line", -1.25, 8,
				point -> point.getY() - 2 * point.getX() - 1,
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, 2 * parameter + 1));
		assertAnalytic("circle", 0.75, 2,
				point -> point.getX() * point.getX()
						+ point.getY() * point.getY() - 4,
				(source, branch, parameter, session) -> new LocusPoint2D(
						2 * Math.cos(parameter), 2 * Math.sin(parameter)));
		assertAnalytic("ellipse", -0.8, 3,
				point -> point.getX() * point.getX() / 9
						+ point.getY() * point.getY() / 4 - 1,
				(source, branch, parameter, session) -> new LocusPoint2D(
						3 * Math.cos(parameter), 2 * Math.sin(parameter)));
		assertAnalytic("parabola", 1.5, 4,
				point -> point.getY() - point.getX() * point.getX(),
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, parameter * parameter));
		assertAnalytic("transcendental", 0.4, 2 * Math.PI,
				point -> point.getY() - Math.sin(point.getX()),
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, Math.sin(parameter)));
	}

	@Test
	void parameterMultiplicityAndPeriodicSeamAreSemantic() {
		ExplicitNumericDomainProvider2D provider = numeric(-Math.PI, Math.PI,
				false, true);
		GeoNumeric source = new GeoNumeric(getConstruction(), 1);
		GeoLocusV2 selfIntersection = LocusV2Factory.createAnalytic(
				LocusV2Mode.V2, getConstruction(), "self-intersection", source,
				provider, branches(provider), (value, branch, parameter, session) ->
						new LocusPoint2D(Math.sin(parameter),
								Math.sin(2 * parameter)), "lemniscate-parameter/v1");
		LocusPoint2D atZero = evaluate(selfIntersection, 0).getPoint();
		LocusPoint2D atPi = evaluate(selfIntersection, Math.PI).getPoint();
		assertEquals(atZero.getX(), atPi.getX(), 5E-16);
		assertEquals(atZero.getY(), atPi.getY(), 5E-16);
		assertNotEquals(0, Math.PI);

		ExplicitNumericDomainProvider2D periodic = numeric(-Math.PI, Math.PI,
				true, false);
		GeoLocusV2 circle = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "periodic-circle", source, periodic,
				branches(periodic), (value, branch, parameter, session) ->
						new LocusPoint2D(Math.cos(parameter), Math.sin(parameter)),
				"periodic-circle/v1");
		assertEquals(evaluate(circle, -Math.PI), evaluate(circle, Math.PI));
	}

	@Test
	void providerOwnsDynamicBranchTopologyAndLineage() {
		ExplicitNumericDomainProvider2D provider = numeric(-1, 1, false, true);
		GeoNumeric branchControl = new GeoNumeric(getConstruction(), 0);
		GeoNumeric componentControl = new GeoNumeric(getConstruction(), -0.25);
		GeoLocusV2 locus = LocusV2Factory.createDynamicAnalytic(LocusV2Mode.V2,
				getConstruction(), "topology-fixture", Arrays.asList(branchControl,
						componentControl), provider,
				(sources, previous) -> topologySnapshot(sources, previous, provider),
				(sources, branch, parameter, session) -> new LocusPoint2D(parameter,
						branch.getBranchKey().endsWith("/+")
								? Math.sqrt(Math.max(0, sources.get(0)))
								: branch.getBranchKey().endsWith("/-")
										? -Math.sqrt(Math.max(0, sources.get(0))) : 0),
				"g6a-topology-fixture/v1");

		assertTopology(locus, DefinitionStatus.VALID, "root", 1,
				LineageTransition.UNCHANGED);
		componentControl.setValue(0.25);
		componentControl.updateCascade();
		assertTopology(locus, DefinitionStatus.VALID, "root", 2,
				LineageTransition.UNCHANGED);
		assertEquals(EvaluationStatus.OUT_OF_DOMAIN,
				locus.evaluate("root", 0, LocusEvaluationSession2D.reference())
						.getStatus());

		componentControl.setValue(1);
		componentControl.updateCascade();
		assertTopology(locus, DefinitionStatus.VALID, "root", 2,
				LineageTransition.UNCHANGED);
		assertTrue(locus.getSemanticDefinition().getBranch("root")
				.getValidDomainComponents().stream()
				.allMatch(interval -> interval.getLower() == interval.getUpper()));

		componentControl.setValue(1.25);
		componentControl.updateCascade();
		assertTopology(locus, DefinitionStatus.EMPTY_DOMAIN, "root", 0,
				LineageTransition.DISAPPEARED);
		componentControl.setValue(-0.25);
		componentControl.updateCascade();
		assertTopology(locus, DefinitionStatus.VALID, "root", 1,
				LineageTransition.APPEARED);

		branchControl.setValue(1);
		branchControl.updateCascade();
		assertEquals(2, locus.getSemanticDefinition().getBranches().size());
		assertEquals(LineageTransition.SPLIT, locus.getSemanticDefinition()
				.getBranch("root/+").getLineage().getTransition());
		assertEquals(1, locus.evaluate("root/+", 0.25,
				LocusEvaluationSession2D.reference()).getPoint().getY(), 0);
		assertEquals(-1, locus.evaluate("root/-", 0.25,
				LocusEvaluationSession2D.reference()).getPoint().getY(), 0);

		branchControl.setValue(0);
		branchControl.updateCascade();
		assertTopology(locus, DefinitionStatus.VALID, "root", 1,
				LineageTransition.MERGED);
		branchControl.setValue(-1);
		branchControl.updateCascade();
		assertTopology(locus, DefinitionStatus.EMPTY_DOMAIN, "root", 0,
				LineageTransition.DISAPPEARED);
		branchControl.setValue(0);
		branchControl.updateCascade();
		assertTopology(locus, DefinitionStatus.VALID, "root", 1,
				LineageTransition.APPEARED);
	}

	@Test
	void liveSegmentPathProviderUsesNormalDagButNotPathParameterIdentity() {
		GeoPoint start = add("A=(0,0)");
		GeoPoint end = add("B=(2,4)");
		GeoSegment segment = add("s=Segment(A,B)");
		GeoPoint driver = add("D=Point(s)");
		GeoLocusV2 locus = LocusV2Factory.createSegmentPathDriven(
				LocusV2Mode.V2, getConstruction(), "live-segment-pilot", segment,
				driver, EPS_DOMAIN,
				(driverPoint, branch, parameter, session) -> driverPoint,
				"segment-identity/v1");

		assertEquals(new LocusPoint2D(1, 2), locus.evaluate("segment.sheet.main",
				0.5, LocusEvaluationSession2D.reference()).getPoint());
		assertEquals("stable-path-domain/v1", locus.getSemanticDefinition()
				.getProvider().getProviderId());
		assertFalse(locus.getSemanticDefinition().getProvider()
				.getSemanticSignature().contains("PathParameter"));
		long initialRevision = locus.getSemanticRevision();
		driver.setCoords(1.5, 3, 1);
		driver.updateCascade();
		assertEquals(initialRevision, locus.getSemanticRevision());

		end.setCoords(4, 2, 1);
		end.updateCascade();
		assertEquals(initialRevision + 1, locus.getSemanticRevision());
		assertEquals(new LocusPoint2D(2, 1), locus.evaluate("segment.sheet.main",
				0.5, LocusEvaluationSession2D.reference()).getPoint());
		assertEquals(0, locus.getInstrumentation().getDependencySliceBuilds());
		assertEquals(0, locus.getInstrumentation().getRenderEvaluations());
	}

	@Test
	void cuspAndCollapsedImageRetainTypedSemanticMeaning() {
		ExplicitNumericDomainProvider2D provider = numeric(-1, 1, false, true);
		GeoNumeric source = new GeoNumeric(getConstruction(), 0);
		GeoLocusV2 cusp = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "semicubical-cusp", source, provider,
				branches(provider), (value, branch, parameter, session) ->
						new LocusPoint2D(parameter * parameter,
								parameter * parameter * parameter),
				"semicubical-cusp/v1");
		LocusEvaluation2D cuspAtOrigin = evaluate(cusp, 0);
		assertEquals(EvaluationStatus.VALID, cuspAtOrigin.getStatus());
		assertEquals(Regularity.UNKNOWN, cuspAtOrigin.getRegularity());

		LocusBranch2D collapsedBranch = LocusV2Factory.fullDomainBranch(
				BRANCH_KEY, provider, "collapsed-image/v1",
				EnumSet.of(BranchProperty.COLLAPSED_IMAGE));
		GeoLocusV2 collapsed = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "collapsed-image", source, provider,
				Collections.singletonList(collapsedBranch),
				(value, branch, parameter, session) -> new LocusPoint2D(2, 3),
				"collapsed-image/v1");
		assertTrue(collapsed.getSemanticDefinition().getBranch(BRANCH_KEY)
				.getProperties().contains(BranchProperty.COLLAPSED_IMAGE));
		assertEquals(evaluate(collapsed, -0.75), evaluate(collapsed, 0.75));
	}

	@Test
	void undefinedDependencyPublishesTypedRevisionAndRecovers() {
		ExplicitNumericDomainProvider2D provider = numeric(-1, 1, false, true);
		GeoNumeric source = new GeoNumeric(getConstruction(), 2);
		GeoLocusV2 locus = LocusV2Factory.createDynamicAnalytic(LocusV2Mode.V2,
				getConstruction(), "undefined-recovery", Collections.singletonList(source),
				provider, (sources, previous) -> new LocusBranchSnapshot2D(
						DefinitionStatus.VALID, branches(provider)),
				(sources, branch, parameter, session) ->
						new LocusPoint2D(parameter, sources.get(0) * parameter),
				"undefined-recovery/v1");
		long validRevision = locus.getSemanticRevision();
		source.setUndefined();
		source.updateCascade();
		assertEquals(validRevision + 1, locus.getSemanticRevision());
		assertEquals(DefinitionStatus.DRIVER_INVALID,
				locus.getSemanticDefinition().getDefinitionStatus());
		assertEquals(EvaluationStatus.DEPENDENCY_UNDEFINED,
				locus.evaluate(BRANCH_KEY, 0.25,
						LocusEvaluationSession2D.reference()).getStatus());

		source.setValue(3);
		source.updateCascade();
		assertEquals(validRevision + 2, locus.getSemanticRevision());
		assertEquals(new LocusPoint2D(0.25, 0.75), evaluate(locus, 0.25)
				.getPoint());
	}

	@Test
	void discreteTopologyInputUsesStableProviderKeysAndNormalInvalidation() {
		ExplicitNumericDomainProvider2D provider = numeric(-1, 1, false, true);
		GeoNumeric count = new GeoNumeric(getConstruction(), 1);
		GeoLocusV2 locus = LocusV2Factory.createDynamicAnalytic(LocusV2Mode.V2,
				getConstruction(), "discrete-topology", Collections.singletonList(count),
				provider, (sources, previous) -> discreteBranches(sources, previous,
						provider),
				(sources, branch, parameter, session) -> new LocusPoint2D(parameter,
						Integer.parseInt(branch.getBranchKey().substring(
								branch.getBranchKey().lastIndexOf('.') + 1))),
				"discrete-topology/v1");
		assertEquals(1, locus.getSemanticDefinition().getBranches().size());
		assertEquals("discrete.sheet.0", locus.getSemanticDefinition().getBranches()
				.get(0).getBranchKey());
		long firstRevision = locus.getSemanticRevision();
		count.setValue(2);
		count.updateCascade();
		assertEquals(firstRevision + 1, locus.getSemanticRevision());
		assertEquals(2, locus.getSemanticDefinition().getBranches().size());
		assertEquals(LineageTransition.UNCHANGED, locus.getSemanticDefinition()
				.getBranch("discrete.sheet.0").getLineage().getTransition());
		assertEquals(LineageTransition.APPEARED, locus.getSemanticDefinition()
				.getBranch("discrete.sheet.1").getLineage().getTransition());
	}

	@Test
	void nestedCompositionHasQueriesTimesDepthFunctionalCost() {
		List<Double> queries = Arrays.asList(-1.5, -0.75, 0.0, 0.75, 1.5);
		for (int depth : Arrays.asList(1, 2, 3, 5)) {
			Chain memoizedChain = chain(depth);
			reset(memoizedChain);
			LocusEvaluationSession2D memoized =
					LocusEvaluationSession2D.memoizing(256);
			List<LocusEvaluation2D> memoizedResults = evaluateAll(
					memoizedChain.outer(), queries, memoized);
			assertEquals(queries.size() * depth,
					totalEvaluatorCalls(memoizedChain));
			assertEquals(queries.size() * depth, memoized.getMisses());
			assertEquals(0, memoized.getHits());
			assertNoLegacyRegeneration(memoizedChain);

			Chain referenceChain = chain(depth);
			reset(referenceChain);
			LocusEvaluationSession2D reference = LocusEvaluationSession2D.reference();
			List<LocusEvaluation2D> referenceResults = evaluateAll(
					referenceChain.outer(), queries, reference);
			assertEquals(memoizedResults, referenceResults);
			assertEquals(queries.size() * depth,
					totalEvaluatorCalls(referenceChain));
			assertNoLegacyRegeneration(referenceChain);
		}
	}

	@Test
	void exactSemanticKeyIsEvaluatedOncePerEligibleSession() {
		Chain chain = chain(3);
		reset(chain);
		LocusEvaluationSession2D session =
				LocusEvaluationSession2D.memoizing(32);
		LocusEvaluation2D first = chain.outer().evaluate(BRANCH_KEY, 0.75,
				session);
		LocusEvaluation2D second = chain.outer().evaluate(BRANCH_KEY, 0.75,
				session);
		assertEquals(first, second);
		assertEquals(3, totalEvaluatorCalls(chain));
		assertEquals(1, session.getHits());
		assertEquals(3, session.getMisses());
		assertEquals(3, session.getCachedEntryCount());
		assertNoLegacyRegeneration(chain);
	}

	@Test
	void innermostChangeInvalidatesNormalDagOncePerSemanticSnapshot() {
		Chain chain = chain(3);
		long[] before = revisions(chain);
		LocusPoint2D pointBefore = evaluate(chain.outer(), 0.5).getPoint();

		chain.source().setValue(4);
		chain.source().updateCascade();

		long[] after = revisions(chain);
		for (int index = 0; index < before.length; index++) {
			assertEquals(before[index] + 1, after[index]);
		}
		LocusPoint2D pointAfter = evaluate(chain.outer(), 0.5).getPoint();
		assertNotEquals(pointBefore, pointAfter);
		for (GeoLocusV2 locus : chain.loci()) {
			assertEquals(0, locus.getInstrumentation().getRenderEvaluations());
			long revision = locus.getSemanticRevision();
			evaluate(locus, 0.25);
			assertEquals(revision, locus.getSemanticRevision());
			locus.getParentAlgorithm().update();
			assertEquals(revision, locus.getSemanticRevision());
		}
	}

	@Test
	void activeKeyCycleIsDiagnosedWithoutHiddenCallbackGraph() {
		ExplicitNumericDomainProvider2D provider = numeric(-1, 1, false, true);
		LocusInstrumentation2D instrumentation = new LocusInstrumentation2D();
		LocusDefinition2D[] holder = new LocusDefinition2D[1];
		holder[0] = new LocusDefinition2D("cycle", 1, DefinitionStatus.VALID,
				provider, branches(provider),
				(definition, branch, parameter, session) -> holder[0].evaluate(
						branch.getBranchKey(), parameter, session),
				Determinism.POINTWISE_DETERMINISTIC, "cycle-fixture/v1",
				instrumentation);
		LocusEvaluationSession2D session =
				LocusEvaluationSession2D.memoizing(8);
		LocusEvaluation2D result = holder[0].evaluate(BRANCH_KEY, 0.25, session);
		assertEquals(EvaluationStatus.EVALUATION_FAILED, result.getStatus());
		assertTrue(result.getDiagnostic().contains("re-entry cycle"));
		assertEquals(1, session.getCycles());
	}

	@Test
	void typedInvalidEvaluationsNeverCarryStaleCoordinates() {
		ExplicitNumericDomainProvider2D provider = numeric(-1, 1, false, true);
		LocusInstrumentation2D instrumentation = new LocusInstrumentation2D();
		LocusDefinition2D nonFinite = new LocusDefinition2D("non-finite", 1,
				DefinitionStatus.VALID, provider, branches(provider),
				(definition, branch, parameter, session) -> LocusEvaluation2D.invalid(
						EvaluationStatus.NON_FINITE, branch.getQuality(),
						"Controlled non-finite fixture"),
				Determinism.POINTWISE_DETERMINISTIC, "non-finite/v1",
				instrumentation);
		LocusEvaluation2D nonFiniteResult = nonFinite.evaluate(BRANCH_KEY, 0,
				LocusEvaluationSession2D.reference());
		assertEquals(EvaluationStatus.NON_FINITE, nonFiniteResult.getStatus());
		assertEquals(null, nonFiniteResult.getPoint());

		LocusDefinition2D historyDependent = new LocusDefinition2D("history", 1,
				DefinitionStatus.VALID, provider, branches(provider),
				(definition, branch, parameter, session) -> LocusEvaluation2D.valid(
						new LocusPoint2D(parameter, parameter), Regularity.UNKNOWN,
						branch.getQuality()),
				Determinism.UNSUPPORTED_NONDETERMINISM, "history/v1",
				new LocusInstrumentation2D());
		assertEquals(EvaluationStatus.UNSUPPORTED_NONDETERMINISM,
				historyDependent.evaluate(BRANCH_KEY, 0,
						LocusEvaluationSession2D.reference()).getStatus());
	}

	@Test
	void sessionIsBoundedAndFullSemanticKeyPreventsConflation() {
		ExplicitNumericDomainProvider2D provider = numeric(-1, 1, false, true);
		LocusBranch2D upper = branch("sheet.upper", provider,
				Collections.singletonList(provider.getDeclaredDomain()),
				LocusLineage2D.unchanged());
		LocusBranch2D lower = branch("sheet.lower", provider,
				Collections.singletonList(provider.getDeclaredDomain()),
				LocusLineage2D.unchanged());
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "full-key", new GeoNumeric(getConstruction(), 0),
				provider, Arrays.asList(upper, lower),
				(source, branch, parameter, session) -> new LocusPoint2D(parameter,
						branch.getBranchKey().endsWith("upper") ? 1 : -1),
				"full-key/v1");
		LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(2);
		assertEquals(1, locus.evaluate("sheet.upper", 0, session).getPoint().getY(),
				0);
		assertEquals(-1, locus.evaluate("sheet.lower", 0, session).getPoint().getY(),
				0);
		locus.evaluate("sheet.upper", 0.5, session);
		assertEquals(2, session.getCachedEntryCount());
		assertEquals(1, session.getEvictions());
	}

	@Test
	void pointwiseResultsAreIndependentOfQueryOrderAndDualModeIsExplicit() {
		ExplicitNumericDomainProvider2D provider = numeric(-1, 1, false, true);
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.DUAL,
				getConstruction(), "deterministic-order",
				new GeoNumeric(getConstruction(), 0), provider, branches(provider),
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, parameter * parameter),
				"deterministic-order/v1");
		double[] forward = {-1, -0.5, 0, 0.5, 1};
		double[] reverse = {1, 0.5, 0, -0.5, -1};
		for (double parameter : forward) {
			LocusEvaluation2D reference = locus.evaluate(BRANCH_KEY, parameter,
					LocusEvaluationSession2D.reference());
			LocusEvaluation2D memoized = locus.evaluate(BRANCH_KEY, parameter,
					LocusEvaluationSession2D.memoizing(8));
			assertEquals(reference, memoized);
		}
		for (double parameter : reverse) {
			assertEquals(new LocusPoint2D(parameter, parameter * parameter),
					evaluate(locus, parameter).getPoint());
		}

		List<LocusDualRunDiagnostic2D.Comparison> comparisons =
				LocusDualRunDiagnostic2D.compare(locus::evaluate,
						Arrays.asList(new LocusDualRunDiagnostic2D.SampleEvidence(
								BRANCH_KEY, 0.5, new LocusPoint2D(0.5, 0.25)),
								new LocusDualRunDiagnostic2D.SampleEvidence(BRANCH_KEY,
										0.5, new LocusPoint2D(0.5, 0.5))), 1);
		assertTrue(comparisons.get(0).isWithinEnvelope());
		assertFalse(comparisons.get(1).isWithinEnvelope());
	}

	@Test
	void classificationAndPublicCompatibilityStayStrictlySeparate() {
		Chain chain = chain(1);
		GeoLocusV2 locus = chain.outer();
		assertEquals(GeoClass.LOCUS_V2, locus.getGeoClassType());
		assertEquals(GeoClass.SHAPE_STADIUM.ordinal() + 1,
				GeoClass.LOCUS_V2.ordinal());
		assertEquals(GeoClass.LOCUS_V2.ordinal() + 1,
				GeoClass.LOCUS_METRIC_RESULT.ordinal());
		assertEquals(GeoClass.LOCUS_METRIC_RESULT.ordinal() + 1,
				GeoClass.LOCUS_INTERSECTION_RESULT.ordinal());
		assertEquals(GeoClass.values().length - 1,
				GeoClass.LOCUS_INTERSECTION_RESULT.ordinal());
		assertFalse(locus.isGeoLocus());
		assertFalse(locus.isGeoLocusable());
		assertFalse(Path.class.isAssignableFrom(locus.getClass()));
		assertFalse(GeoLocusNDInterface.class.isAssignableFrom(locus.getClass()));
		assertFalse(GeoLocusable.class.isAssignableFrom(locus.getClass()));
		assertFalse(locus.isGeoElement3D());
		assertFalse(locus.hasDrawable3D());
		assertEquals("", locus.getXML());
		assertFalse(getApp().getXML().contains("locusv2"));
		assertFalse(getKernel().createGeoElement(getConstruction(), "locusv2")
				instanceof GeoLocusV2);

		GeometryExportModel export = new GeometryExportService().createModel(
				Collections.singletonList(locus), SelectionMode.CURRENT_SELECTION);
		assertTrue(export.getEntities().isEmpty());
		assertEquals(DiagnosticCode.UNSUPPORTED,
				export.getDiagnostics().get(0).getCode());

		GeoElement legacy = add("a=Slider(-1,1)");
		assertTrue(legacy instanceof GeoNumeric);
		add("P=(a,a^2)");
		GeoElement publicLocus = add("legacyLocus=Locus(P,a)");
		assertEquals(GeoClass.LOCUS, publicLocus.getGeoClassType());
		assertTrue(publicLocus.isGeoLocus());
	}

	@Test
	void runtimeModesDoNotRedirectPublicCreation() {
		assertEquals(LocusV2Mode.LEGACY, LocusV2Mode.parse(null));
		assertEquals(LocusV2Mode.LEGACY, LocusV2Mode.parse("invalid"));
		assertEquals(LocusV2Mode.V2, LocusV2Mode.parse("v2"));
		assertEquals(LocusV2Mode.DUAL, LocusV2Mode.parse("DUAL"));
		ExplicitNumericDomainProvider2D provider = numeric(-1, 1, false, true);
		GeoNumeric source = new GeoNumeric(getConstruction(), 1);
		assertThrows(IllegalStateException.class,
				() -> LocusV2Factory.createAnalytic(LocusV2Mode.LEGACY,
						getConstruction(), "forbidden", source, provider,
						branches(provider), (value, branch, parameter, session) ->
								new LocusPoint2D(parameter, parameter),
						"identity/v1"));
	}

	private void assertAnalytic(String identity, double parameter,
			double characteristicScale,
			PointResidual residual,
			org.geocedg.common.kernel.locus.LocusPointFunction2D function) {
		ExplicitNumericDomainProvider2D provider = numeric(-2, 2, false, true);
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), identity, new GeoNumeric(getConstruction(), 1),
				provider, branches(provider), function, identity + "/v1");
		LocusPoint2D point = evaluate(locus, parameter).getPoint();
		assertEquals(0, residual.residual(point),
				LocusValidationTolerance2D.evaluationEnvelope(characteristicScale));
	}

	private Chain chain(int depth) {
		ExplicitNumericDomainProvider2D provider = numeric(-2, 2, false, true);
		List<LocusBranch2D> branches = branches(provider);
		GeoNumeric source = new GeoNumeric(getConstruction(), 1);
		List<GeoLocusV2> loci = new ArrayList<>();
		GeoLocusV2 current = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "nested-L1-" + depth + "-" + source.hashCode(),
				source, provider, branches,
				(value, branch, parameter, session) ->
						new LocusPoint2D(value + parameter, 2 * parameter),
				"nested-leaf/v1");
		loci.add(current);
		for (int level = 2; level <= depth; level++) {
			final int capturedLevel = level;
			current = LocusV2Factory.createNested(LocusV2Mode.V2,
					getConstruction(), "nested-L" + level + "-" + depth + "-"
							+ source.hashCode(), current, BRANCH_KEY, provider, branches,
					parameter -> parameter / 2,
					(parameter, upstream) -> new LocusPoint2D(
							upstream.getX() + capturedLevel * parameter,
							upstream.getY() - parameter),
					"nested-transform-level-" + level + "/v1");
			loci.add(current);
		}
		return new Chain(source, loci);
	}

	private static List<LocusBranch2D> branches(
			ExplicitNumericDomainProvider2D provider) {
		return Collections.singletonList(LocusV2Factory.fullDomainBranch(
				BRANCH_KEY, provider, "g6b-controlled-fixture/v1",
				EnumSet.noneOf(BranchProperty.class)));
	}

	private static LocusBranchSnapshot2D topologySnapshot(
			LocusSourceSnapshot2D sources, LocusDefinition2D previous,
			ExplicitNumericDomainProvider2D provider) {
		double branchControl = sources.get(0);
		double componentControl = sources.get(1);
		if (branchControl < 0 || componentControl > 1) {
			LocusBranch2D disappeared = branch("root", provider,
					Collections.emptyList(), new LocusLineage2D(
							LineageTransition.DISAPPEARED,
							Collections.singletonList("root"),
							Collections.emptyList()));
			return new LocusBranchSnapshot2D(DefinitionStatus.EMPTY_DOMAIN,
					Collections.singletonList(disappeared));
		}
		List<LocusInterval2D> components = validComponents(componentControl);
		if (branchControl > 0) {
			LocusLineage2D lineage = previous != null
					&& previous.getBranch("root") != null
							? new LocusLineage2D(LineageTransition.SPLIT,
									Collections.singletonList("root"),
									Arrays.asList("root/+", "root/-"))
							: new LocusLineage2D(LineageTransition.APPEARED,
									Collections.emptyList(),
									Arrays.asList("root/+", "root/-"));
			return new LocusBranchSnapshot2D(DefinitionStatus.VALID,
					Arrays.asList(branch("root/+", provider, components, lineage),
							branch("root/-", provider, components, lineage)));
		}
		LocusLineage2D lineage = lineageForRoot(previous);
		return new LocusBranchSnapshot2D(DefinitionStatus.VALID,
				Collections.singletonList(branch("root", provider, components,
						lineage)));
	}

	private static LocusLineage2D lineageForRoot(LocusDefinition2D previous) {
		if (previous == null) {
			return LocusLineage2D.unchanged();
		}
		if (previous.getBranch("root/+") != null) {
			return new LocusLineage2D(LineageTransition.MERGED,
					Arrays.asList("root/+", "root/-"),
					Collections.singletonList("root"));
		}
		if (previous.getDefinitionStatus() == DefinitionStatus.EMPTY_DOMAIN) {
			return new LocusLineage2D(LineageTransition.APPEARED,
					Collections.emptyList(), Collections.singletonList("root"));
		}
		return LocusLineage2D.unchanged();
	}

	private static LocusBranchSnapshot2D discreteBranches(
			LocusSourceSnapshot2D sources, LocusDefinition2D previous,
			ExplicitNumericDomainProvider2D provider) {
		int count = (int) sources.get(0);
		if (count < 1 || count != sources.get(0)) {
			return new LocusBranchSnapshot2D(DefinitionStatus.UNSUPPORTED,
					Collections.emptyList());
		}
		List<LocusBranch2D> branches = new ArrayList<>();
		for (int index = 0; index < count; index++) {
			String key = "discrete.sheet." + index;
			LocusLineage2D lineage = previous != null
					&& previous.getBranch(key) != null ? LocusLineage2D.unchanged()
							: new LocusLineage2D(LineageTransition.APPEARED,
									Collections.emptyList(),
									Collections.singletonList(key));
			branches.add(branch(key, provider,
					Collections.singletonList(provider.getDeclaredDomain()), lineage));
		}
		return new LocusBranchSnapshot2D(DefinitionStatus.VALID, branches);
	}

	private static List<LocusInterval2D> validComponents(double control) {
		if (control <= 0) {
			return Collections.singletonList(
					new LocusInterval2D(-1, 1, true, true));
		}
		double root = Math.sqrt(control);
		return Arrays.asList(new LocusInterval2D(-1, -root, true, true),
				new LocusInterval2D(root, 1, true, true));
	}

	private static LocusBranch2D branch(String key,
			ExplicitNumericDomainProvider2D provider,
			List<LocusInterval2D> components, LocusLineage2D lineage) {
		return new LocusBranch2D(key, provider.getDeclaredDomain(), components,
				Orientation.INCREASING, "g6a-topology-fixture/v1", lineage,
				EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());
	}

	private static void assertTopology(GeoLocusV2 locus,
			DefinitionStatus status, String branchKey, int componentCount,
			LineageTransition transition) {
		assertEquals(status, locus.getSemanticDefinition().getDefinitionStatus());
		LocusBranch2D branch = locus.getSemanticDefinition().getBranch(branchKey);
		assertEquals(componentCount, branch.getValidDomainComponents().size());
		assertEquals(transition, branch.getLineage().getTransition());
	}

	private static ExplicitNumericDomainProvider2D numeric(double lower,
			double upper, boolean periodic, boolean upperClosed) {
		return new ExplicitNumericDomainProvider2D("fixture-parameter/v1",
				new LocusInterval2D(lower, upper, true, upperClosed),
				Orientation.INCREASING, periodic, EPS_DOMAIN);
	}

	private static LocusEvaluation2D evaluate(GeoLocusV2 locus,
			double parameter) {
		return locus.evaluate(BRANCH_KEY, parameter,
				LocusEvaluationSession2D.reference());
	}

	private static List<LocusEvaluation2D> evaluateAll(GeoLocusV2 locus,
			List<Double> queries, LocusEvaluationSession2D session) {
		List<LocusEvaluation2D> results = new ArrayList<>();
		for (double query : queries) {
			results.add(locus.evaluate(BRANCH_KEY, query, session));
		}
		return results;
	}

	private static void reset(Chain chain) {
		for (GeoLocusV2 locus : chain.loci()) {
			locus.getInstrumentation().reset();
		}
	}

	private static long totalEvaluatorCalls(Chain chain) {
		long calls = 0;
		for (GeoLocusV2 locus : chain.loci()) {
			calls += locus.getInstrumentation().getEvaluatorCalls();
		}
		return calls;
	}

	private static void assertNoLegacyRegeneration(Chain chain) {
		for (GeoLocusV2 locus : chain.loci()) {
			LocusInstrumentation2D counters = locus.getInstrumentation();
			assertEquals(0, counters.getDependencySliceBuilds());
			assertEquals(0, counters.getDependencySliceSynchronizations());
			assertEquals(0, counters.getWholeLocusRegenerations());
			assertEquals(0, counters.getRenderEvaluations());
		}
	}

	private static long[] revisions(Chain chain) {
		long[] revisions = new long[chain.loci().size()];
		for (int index = 0; index < revisions.length; index++) {
			revisions[index] = chain.loci().get(index).getSemanticRevision();
		}
		return revisions;
	}

	private interface PointResidual {
		double residual(LocusPoint2D point);
	}

	private static final class Chain {
		private final GeoNumeric source;
		private final List<GeoLocusV2> loci;

		Chain(GeoNumeric source, List<GeoLocusV2> loci) {
			this.source = source;
			this.loci = loci;
		}

		GeoNumeric source() {
			return source;
		}

		List<GeoLocusV2> loci() {
			return loci;
		}

		GeoLocusV2 outer() {
			return loci.get(loci.size() - 1);
		}
	}
}
