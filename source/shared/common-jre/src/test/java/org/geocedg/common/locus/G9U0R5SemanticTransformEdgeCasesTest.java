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

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Semantic edge cases intentionally separate from the principal R5 matrix. */
class G9U0R5SemanticTransformEdgeCasesTest
		extends G9U0PublicSurfaceTestBase {

	private static final String UPPER_BRANCH = "r5.fixture.upper";
	private static final String LOWER_BRANCH = "r5.fixture.lower";

	@Test
	void periodicNegativeDilationPreservesFundamentalDomainAndSeam() {
		GeoLocusV2 source = createPeriodicCircle();
		GeoLocusV2 image = add("T=Dilate(L,-2,(1,-1))");

		assertSame(source.getSemanticDefinition().getProvider(),
				image.getSemanticDefinition().getProvider());
		assertTrue(image.getSemanticDefinition().getProvider().isPeriodic());
		assertBranchStructure(source, image, BRANCH);
		assertEquals(Orientation.INCREASING,
				image.getSemanticDefinition().getBranch(BRANCH).getOrientation());
		assertTrue(image.getSemanticDefinition().getBranch(BRANCH).getProperties()
				.contains(BranchProperty.PERIODIC));

		assertPoint(image, BRANCH, 0, 1, -3);
		assertPoint(image, BRANCH, 2 * Math.PI, 1, -3);
		assertPoint(image, BRANCH, Math.PI / 2, 3, -5);
	}

	@Test
	void disconnectedGapSurvivesCollapsedImageAndLaterTransformClosure() {
		GeoLocusV2 source = createDisconnectedLine();
		GeoLocusV2 collapsed = add("C=Dilate(L,0,(4,5))");
		GeoLocusV2 rotated = add("R=Rotate(C,pi/2)");
		GeoLocusV2 image = add("T=Translate(R,(3,4))");

		assertBranchStructure(source, collapsed, BRANCH);
		assertBranchStructure(collapsed, rotated, BRANCH);
		assertBranchStructure(rotated, image, BRANCH);
		assertEquals(EvaluationStatus.OUT_OF_DOMAIN,
				evaluate(image, BRANCH, 0).getStatus());
		assertPoint(image, BRANCH, -1.5, -2, 8);
		assertPoint(image, BRANCH, 1.5, -2, 8);
		assertTrue(image.getSemanticDefinition().getBranch(BRANCH).getProperties()
				.contains(BranchProperty.COLLAPSED_IMAGE));

		GeoLocusMetricResult metric = totalMetric(image);
		assertEquals(MetricComputationStatus.SUCCESS,
				metric.getMetricResult().getComputationStatus());
		assertEquals(0, metricValue(metric), 0);
		assertTrue(metric.getMetricResult().getDiagnostics().stream()
				.anyMatch(diagnostic -> diagnostic.getCode()
						== MetricDiagnosticCode2D.COLLAPSED_IMAGE));
	}

	@Test
	void decreasingOrientationAndNegativeScalePreserveSemanticAddress() {
		GeoLocusV2 source = createScalarLocus("L", "s", "Q", "(s,s^2)",
				"{false,{2,-2,true,true}}");
		GeoLocusV2 image = add("T=Dilate(L,-3,(1,2))");

		assertEquals(Orientation.DECREASING,
				source.getSemanticDefinition().getProvider().getOrientation());
		assertBranchStructure(source, image, BRANCH);
		assertEquals(Orientation.DECREASING,
				image.getSemanticDefinition().getBranch(BRANCH).getOrientation());
		assertPoint(image, BRANCH, 0.5, 2.5, 7.25);

		GeoPoint sourcePoint = add("P=Point(L,\"" + BRANCH + "\",0.5)");
		GeoPoint imagePoint = add("TP=Point(T,\"" + BRANCH + "\",0.5)");
		assertEquals(sourcePoint.getInhomX(), 0.5, 1E-10);
		assertEquals(sourcePoint.getInhomY(), 0.25, 1E-10);
		assertEquals(imagePoint.getInhomX(), 2.5, 1E-10);
		assertEquals(imagePoint.getInhomY(), 7.25, 1E-10);
		assertEquals(3 * metricValue(totalMetric(source)),
				metricValue(add("MT=LocusLength(T)")), 1E-6);
	}

	@Test
	void finiteRotationOverflowAndUndefinedInputsRecoverWithoutStalePoint() {
		createLine();
		GeoNumeric centerX = add("c=1E308");
		add("A=(c,0)");
		GeoLocusV2 image = add("T=Rotate(L,pi,A)");

		assertTrue(image.isDefined());
		assertEquals(EvaluationStatus.NON_FINITE,
				evaluate(image, BRANCH, 0.5).getStatus());

		centerX.setValue(1);
		centerX.updateCascade();
		assertTrue(image.isDefined());
		assertPoint(image, BRANCH, 0.5, 1.5, 0);

		centerX.setUndefined();
		centerX.updateCascade();
		assertFalse(image.isDefined());
		centerX.setValue(1);
		centerX.updateCascade();
		assertTrue(image.isDefined());
		assertPoint(image, BRANCH, 0.5, 1.5, 0);
	}

	@Test
	void multipleBranchesAndEmptyDefinitionRemainStructurallyExact() {
		GeoLocusV2 source = createLine();
		publishTwoBranchFixture(source);
		GeoLocusV2 image = add("T=Translate(L,(3,4))");

		assertEquals(List.of(UPPER_BRANCH, LOWER_BRANCH),
				image.getSemanticDefinition().getBranches().stream()
						.map(LocusBranch2D::getBranchKey).toList());
		assertBranchStructure(source, image, UPPER_BRANCH);
		assertBranchStructure(source, image, LOWER_BRANCH);
		assertPoint(image, UPPER_BRANCH, -1, 2, 5);
		assertPoint(image, LOWER_BRANCH, 1, 4, 3);
		assertEquals(EvaluationStatus.OUT_OF_DOMAIN,
				evaluate(image, UPPER_BRANCH, 1).getStatus());
		assertEquals(EvaluationStatus.OUT_OF_DOMAIN,
				evaluate(image, LOWER_BRANCH, -1).getStatus());
	}

	@Test
	void emptyDefinitionTransformsToAnEmptyDefinedSemanticImage() {
		GeoLocusV2 source = createLine();
		publishEmptyFixture(source);
		GeoLocusV2 image = add("T=Translate(L,(3,4))");

		assertTrue(source.isDefined());
		assertTrue(image.isDefined());
		assertEquals(DefinitionStatus.EMPTY_DOMAIN,
				image.getSemanticDefinition().getDefinitionStatus());
		assertTrue(image.getSemanticDefinition().getBranches().isEmpty());
		assertEquals(EvaluationStatus.OUT_OF_DOMAIN,
				evaluate(image, BRANCH, 0).getStatus());
	}

	@Test
	void unboundedCollapsedImageHasRichExactZeroWithoutEndpointEvaluation() {
		LocusSimilarityTransform2D extremeCollapsed =
				LocusSimilarityTransform2D.dilation(0, 1E308, -1E308);
		LocusPoint2D extremeImage = extremeCollapsed.transform(
				new LocusPoint2D(-1E308, 1E308));
		assertEquals(1E308, extremeImage.getX(), 0);
		assertEquals(-1E308, extremeImage.getY(), 0);

		GeoLocusV2 source = createLine();
		publishUnboundedFixture(source);
		GeoLocusV2 image = add("T=Dilate(L,0,(4,5))");

		LocusBranch2D branch = image.getSemanticDefinition().getBranch(BRANCH);
		assertTrue(branch.getProperties().contains(BranchProperty.COLLAPSED_IMAGE));
		assertFalse(branch.getProperties().contains(BranchProperty.FINITE));
		assertTrue(branch.getProperties().contains(BranchProperty.UNBOUNDED));
		assertPoint(image, BRANCH, 0, 4, 5);
		assertEquals(EvaluationStatus.OUT_OF_DOMAIN,
				evaluate(image, BRANCH, -3).getStatus());

		long sourceCalls = source.getInstrumentation().getEvaluatorCalls();
		long imageCalls = image.getInstrumentation().getEvaluatorCalls();
		GeoLocusMetricResult metric = totalMetric(image);
		assertEquals(MetricComputationStatus.SUCCESS,
				metric.getMetricResult().getComputationStatus());
		assertEquals(0, metricValue(metric), 0);
		assertTrue(metric.getMetricResult().getDiagnostics().stream()
				.anyMatch(diagnostic -> diagnostic.getCode()
						== MetricDiagnosticCode2D.COLLAPSED_IMAGE));
		assertEquals(sourceCalls, source.getInstrumentation().getEvaluatorCalls());
		assertEquals(imageCalls, image.getInstrumentation().getEvaluatorCalls());
	}

	@Test
	void finiteLargeReflectionCoefficientsNormalizeWithoutOverflow() {
		LocusSimilarityTransform2D reflection =
				LocusSimilarityTransform2D.lineReflection(1E308, 1E308, 1E308);
		LocusPoint2D image = reflection.transform(new LocusPoint2D(0, 0));

		assertEquals(-1, image.getX(), 1E-15);
		assertEquals(-1, image.getY(), 1E-15);
	}

	private static void publishTwoBranchFixture(GeoLocusV2 source) {
		ExplicitNumericDomainProvider2D provider = fixtureProvider(
				Orientation.INCREASING, false, true, true);
		LocusBranch2D upper = fixtureBranch(UPPER_BRANCH, provider,
				List.of(new LocusInterval2D(-2, 0, true, true)),
				EnumSet.of(BranchProperty.FINITE));
		LocusBranch2D lower = fixtureBranch(LOWER_BRANCH, provider,
				List.of(new LocusInterval2D(0, 2, true, true)),
				EnumSet.of(BranchProperty.FINITE));
		source.publishSemanticDefinition(new LocusDefinition2D(
				source.getLocusIdentity(), source.getSemanticRevision() + 1,
				DefinitionStatus.VALID, provider, List.of(upper, lower),
				(definition, branch, parameter, session) ->
						LocusEvaluation2D.valid(new LocusPoint2D(parameter,
								UPPER_BRANCH.equals(branch.getBranchKey()) ? 1 : -1),
								Regularity.REGULAR, branch.getQuality()),
				Determinism.POINTWISE_DETERMINISTIC,
				"r5-edge-two-branch-evaluator/v1", source.getInstrumentation()));
	}

	private static void publishEmptyFixture(GeoLocusV2 source) {
		ExplicitNumericDomainProvider2D provider = fixtureProvider(
				Orientation.INCREASING, false, true, true);
		source.publishSemanticDefinition(new LocusDefinition2D(
				source.getLocusIdentity(), source.getSemanticRevision() + 1,
				DefinitionStatus.EMPTY_DOMAIN, provider, List.of(),
				(definition, branch, parameter, session) ->
						LocusEvaluation2D.valid(new LocusPoint2D(0, 0),
								Regularity.UNKNOWN,
								LocusQuality2D.analyticDoubleSemantic()),
				Determinism.POINTWISE_DETERMINISTIC,
				"r5-edge-empty-evaluator/v1", source.getInstrumentation()));
	}

	private static void publishUnboundedFixture(GeoLocusV2 source) {
		ExplicitNumericDomainProvider2D provider = fixtureProvider(
				Orientation.INCREASING, false, false, false);
		LocusBranch2D branch = fixtureBranch(BRANCH, provider,
				List.of(provider.getDeclaredDomain()),
				EnumSet.of(BranchProperty.UNBOUNDED));
		source.publishSemanticDefinition(new LocusDefinition2D(
				source.getLocusIdentity(), source.getSemanticRevision() + 1,
				DefinitionStatus.VALID, provider, List.of(branch),
				(definition, semanticBranch, parameter, session) ->
						LocusEvaluation2D.valid(new LocusPoint2D(
								parameter / (1 - parameter * parameter), 0),
								Regularity.REGULAR, semanticBranch.getQuality()),
				Determinism.POINTWISE_DETERMINISTIC,
				"r5-edge-unbounded-evaluator/v1", source.getInstrumentation()));
	}

	private static ExplicitNumericDomainProvider2D fixtureProvider(
			Orientation orientation, boolean periodic, boolean lowerClosed,
			boolean upperClosed) {
		return new ExplicitNumericDomainProvider2D("r5-edge-parameter/v1",
				new LocusInterval2D(-2, 2, lowerClosed, upperClosed), orientation,
				periodic, 1E-14);
	}

	private static LocusBranch2D fixtureBranch(String key,
			ExplicitNumericDomainProvider2D provider,
			List<LocusInterval2D> components, EnumSet<BranchProperty> properties) {
		return new LocusBranch2D(key, provider.getDeclaredDomain(), components,
				provider.getOrientation(), "r5-edge-fixture/v1",
				LocusLineage2D.unchanged(), properties,
				LocusQuality2D.analyticDoubleSemantic());
	}

	private static void assertBranchStructure(GeoLocusV2 source,
			GeoLocusV2 image, String branchKey) {
		LocusBranch2D sourceBranch = source.getSemanticDefinition()
				.getBranch(branchKey);
		LocusBranch2D imageBranch = image.getSemanticDefinition()
				.getBranch(branchKey);
		assertEquals(sourceBranch.getDeclaredDriverDomain(),
				imageBranch.getDeclaredDriverDomain());
		assertEquals(sourceBranch.getValidDomainComponents(),
				imageBranch.getValidDomainComponents());
		assertEquals(sourceBranch.getOrientation(), imageBranch.getOrientation());
		assertEquals(sourceBranch.getLineage(), imageBranch.getLineage());
	}

	private static void assertPoint(GeoLocusV2 locus, String branchKey,
			double parameter, double expectedX, double expectedY) {
		LocusEvaluation2D evaluation = evaluate(locus, branchKey, parameter);
		assertTrue(evaluation.isValid(), evaluation.getDiagnostic());
		assertEquals(expectedX, evaluation.getPoint().getX(), 1E-9);
		assertEquals(expectedY, evaluation.getPoint().getY(), 1E-9);
	}

	private static LocusEvaluation2D evaluate(GeoLocusV2 locus,
			String branchKey, double parameter) {
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.reference()) {
			return locus.evaluate(branchKey, parameter, session);
		}
	}

	private static double metricValue(GeoLocusMetricResult metric) {
		return metric.getMetricResult().getMetricValue().getFiniteValue()
				.orElseThrow();
	}
}
