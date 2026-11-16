/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.common.kernel.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicy;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/** A broader program capture must preserve the already admitted particular proof. */
class GeoCeDG120RotationCancellationRegressionTest extends BaseUnitTest {

	private static final String CANCELLATION =
			"circle-length-over-own-radius-periodicity/v1";

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	private void circularMetric() {
		getKernel().setContinuous(false);
		assertFalse(AuthorityPolicy.of(getConstruction()).isConstructive());
		add("radius=2");
		add("O=(0,0)");
		add("A=(1,0)");
		add("C=Circle(O,radius)");
		add("E=Point(C)");
		add("u=Vector((0,0),(0,1))");
		add("Q=Translate(E,u)");
		add("L=LocusV2(Q,E)");
		add("P=Point(L,\"generator.main\",0)");
		add("m=LocusLength(L,P,Q)");
	}

	private GeoLocusV2 rotation(String trace, String angle, String divisor, String locus) {
		add(angle + "=m/" + divisor);
		add(trace + "=Rotate(A," + angle + ",O)");
		return assertInstanceOf(GeoLocusV2.class, add(locus + "=LocusV2(" + trace + ",E)"));
	}

	private static LocusExistenceStructure2D structure(GeoLocusV2 locus) {
		return locus.getSemanticDefinition().getBranches().get(0).getExistenceStructure();
	}

	private static void assertGenericRotationIsAvailable(GeoLocusV2 locus) {
		var evaluator = assertInstanceOf(ReconstructibleLocusEvaluator2D.class,
				locus.getSemanticDefinition().getEvaluatorCapability());
		var program = evaluator.captureCertifiedProgramV3().orElseThrow(
				() -> new AssertionError(evaluator.getProgramV3Refusal()));
		assertTrue(program.getSteps().stream().anyMatch(step -> step.getOperation()
						== CertifiedConstructionProgram2D.Operation.ROTATE_BY_ANGLE),
				"The particular proof must remain reachable when generic capture succeeds");
	}

	private void assertOwnRadiusCancellation(GeoLocusV2 locus, String trace) {
		assertGenericRotationIsAvailable(locus);
		var parent = assertInstanceOf(AlgoDependentPointLocusV2.class,
				locus.getParentAlgorithm());
		var particular = ClosedMetricRotationCertificate2D.capture(lookup(trace),
				lookup("E"), lookup("C"), parent.getGeneratorDescriptor());
		assertTrue(particular.isPresent(), "The admitted own-radius premises still hold");
		assertTrue(particular.orElseThrow().isClosedPeriodic());
		var published = structure(locus);
		assertEquals(LocusExistenceStructure2D.Completeness.COMPLETE,
				published.getCompleteness(), published.getSemanticSignature());
		assertTrue(published.isClosedPeriodic(), published.getSemanticSignature());
		assertTrue(published.getSemanticSignature().contains(CANCELLATION),
				"The public certificate must retain the exact constructive reason");
	}

	@Test
	void ownRadiusCancellationSurvivesGenericCaptureMotionAndReopen() {
		circularMetric();
		GeoLocusV2 image = rotation("T", "lengthAngle", "radius", "W");
		assertOwnRadiusCancellation(image, "T");
		GeoNumeric radius = (GeoNumeric) lookup("radius");
		radius.setValue(3);
		radius.updateCascade();
		assertOwnRadiusCancellation(image, "T");
		getApp().setXML(getApp().getXML(), true);
		assertOwnRadiusCancellation(assertInstanceOf(GeoLocusV2.class, lookup("W")), "T");
	}

	@Test
	void independentCoincidentDivisorDoesNotAcquireTheOwnRadiusPremise() {
		circularMetric();
		GeoNumeric divisor = add("independentRadius=2");
		GeoLocusV2 image = rotation("Z", "otherAngle", "independentRadius", "V");
		assertEquals(((GeoNumeric) lookup("radius")).getDouble(), divisor.getDouble());
		assertNoOwnRadiusProof(image);
		divisor.setValue(3);
		divisor.updateCascade();
		assertNoOwnRadiusProof(image);
	}

	private void assertNoOwnRadiusProof(GeoLocusV2 locus) {
		assertGenericRotationIsAvailable(locus);
		var parent = assertInstanceOf(AlgoDependentPointLocusV2.class,
				locus.getParentAlgorithm());
		assertTrue(ClosedMetricRotationCertificate2D.capture(lookup("Z"), lookup("E"),
				lookup("C"), parent.getGeneratorDescriptor()).isEmpty());
		assertFalse(structure(locus).isClosedPeriodic(),
				"Numerical coincidence is not the defining radius dependency");
		assertFalse(structure(locus).getSemanticSignature().contains(CANCELLATION));
	}
}
