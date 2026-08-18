/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.spatial.semantic.CorrespondenceStatus;
import org.geocedg.common.kernel.spatial.semantic.DiagramMapDefinition;
import org.geocedg.common.kernel.spatial.semantic.DiagramOrientation;
import org.geocedg.common.kernel.spatial.semantic.FrameHandedness;
import org.geocedg.common.kernel.spatial.semantic.NumericPolicy;
import org.geocedg.common.kernel.spatial.semantic.NumericalEvidenceStatus;
import org.geocedg.common.kernel.spatial.semantic.ProjectionDefinedPointEvaluator;
import org.geocedg.common.kernel.spatial.semantic.ProjectionFrameDefinition;
import org.geocedg.common.kernel.spatial.semantic.ProjectionObservation;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemCertificate;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemStatus;
import org.geocedg.common.kernel.spatial.semantic.RepresentationFidelity;
import org.geocedg.common.kernel.spatial.semantic.SpatialCapabilityStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialCertificateStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialCurrentnessStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialDefinitionStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialPointCertificate;
import org.geocedg.common.kernel.spatial.semantic.Vector2;
import org.geocedg.common.kernel.spatial.semantic.Vector3;
import org.junit.jupiter.api.Test;

class G9A2ProjectionDefinedPointEvaluatorTest {
	private static final String UNIT = "model-unit";
	private static final NumericPolicy POLICY = new NumericPolicy(
			1e-9, 1e-9, 1e-12, 1e-9, 1e-9, 1e10);
	private final ProjectionDefinedPointEvaluator evaluator =
			new ProjectionDefinedPointEvaluator();

	@Test
	void genericPointReconstructsAndReprojectsInBothCoordinateSpaces() {
		SpatialPointCertificate result = evaluator.evaluate(observations(
				new Vector2(2, 3), new Vector2(2, 5)), POLICY);

		assertValidPoint(result, new Vector3(2, 3, 5));
		assertEquals(3, result.getRank());
		assertEquals(2, result.getResidualEvidence().size());
		assertTrue(result.getMaximumIntrinsicNormalizedResidual() <= 1);
		assertTrue(result.getMaximumDiagramNormalizedResidual() <= 1);
		assertEquals(SpatialDefinitionStatus.DEFINED,
				result.getDefinitionStatus());
		assertEquals(RepresentationFidelity.NUMERICAL,
				result.getRepresentationFidelity());
		assertEquals(CorrespondenceStatus.NOT_REQUIRED,
				result.getCorrespondenceStatus());
	}

	@Test
	void threeConsistentViewsReportResidualEvidenceForEveryObservation() {
		ProjectionFrameDefinition profile = ProjectionFrameDefinition.orthographic(
				new Vector3(0, 0, 0), new Vector3(0, 1, 0),
				new Vector3(0, 0, 1), FrameHandedness.RIGHT_HANDED, UNIT, 1);
		SpatialPointCertificate result = evaluator.evaluate(List.of(
				new ProjectionObservation(horizontalFrame(), identityMap(),
						new Vector2(2, 3)),
				new ProjectionObservation(verticalFrame(), identityMap(),
						new Vector2(2, 5)),
				new ProjectionObservation(profile, identityMap(),
						new Vector2(3, 5))), POLICY);

		assertValidPoint(result, new Vector3(2, 3, 5));
		assertEquals(3, result.getRank());
		assertEquals(3, result.getResidualEvidence().size());
		for (int index = 0; index < 3; index++) {
			assertEquals(index, result.getResidualEvidence().get(index)
					.getObservationIndex());
			assertTrue(result.getResidualEvidence().get(index)
					.getNormalizedIntrinsicResidual() <= 1);
			assertTrue(result.getResidualEvidence().get(index)
					.getNormalizedDiagramResidual() <= 1);
		}
	}

	@Test
	void pointOnProjectionPlaneIsValidRatherThanDegenerate() {
		SpatialPointCertificate result = evaluator.evaluate(observations(
				new Vector2(-4, 7), new Vector2(-4, 0)), POLICY);

		assertValidPoint(result, new Vector3(-4, 7, 0));
	}

	@Test
	void oneFrameIsExplicitlyUnderdeterminedWithNoPayload() {
		SpatialPointCertificate result = evaluator.evaluate(List.of(
				new ProjectionObservation(horizontalFrame(), identityMap(),
						new Vector2(2, 3))), POLICY);

		assertEquals(SpatialCertificateStatus.UNDERDETERMINED,
				result.getStatus());
		assertEquals(2, result.getRank());
		assertEquals(SpatialCurrentnessStatus.FAILED_CURRENT_REVISION,
				result.getCurrentness());
		assertFalse(result.getPoint().isPresent());
	}

	@Test
	void repeatedParallelFramesRemainExplicitlyUnderdetermined() {
		ProjectionObservation first = new ProjectionObservation(horizontalFrame(),
				identityMap(), new Vector2(2, 3));
		ProjectionObservation repeated = new ProjectionObservation(horizontalFrame(),
				identityMap(), new Vector2(2, 3));

		SpatialPointCertificate result = evaluator.evaluate(
				List.of(first, repeated), POLICY);

		assertEquals(SpatialCertificateStatus.UNDERDETERMINED,
				result.getStatus());
		assertEquals(2, result.getRank());
		assertEquals(SpatialCurrentnessStatus.FAILED_CURRENT_REVISION,
				result.getCurrentness());
		assertFalse(result.hasPayload());
	}

	@Test
	void inconsistentProjectionRaysPublishNoPoint() {
		SpatialPointCertificate result = evaluator.evaluate(observations(
				new Vector2(2, 3), new Vector2(4, 5)), POLICY);

		assertEquals(SpatialCertificateStatus.INCONSISTENT_PROJECTIONS,
				result.getStatus());
		assertFalse(result.hasPayload());
		assertTrue(result.getMaximumIntrinsicNormalizedResidual() > 1);
	}

	@Test
	void invalidFramesAndUndefinedObservationRemainDistinct() {
		ProjectionFrameDefinition invalidFrame = ProjectionFrameDefinition.orthographic(
				new Vector3(0, 0, 0), new Vector3(1, 0, 0),
				new Vector3(1, 0, 0), FrameHandedness.RIGHT_HANDED, UNIT, 1);
		ProjectionFrameDefinition arithmeticOverflowFrame =
				ProjectionFrameDefinition.orthographic(new Vector3(0, 0, 0),
						new Vector3(Double.MAX_VALUE, Double.MAX_VALUE, 0),
						new Vector3(Double.MAX_VALUE, 0, Double.MAX_VALUE),
						FrameHandedness.RIGHT_HANDED, UNIT, 1);
		SpatialPointCertificate degenerate = evaluator.evaluate(List.of(
				new ProjectionObservation(horizontalFrame(), identityMap(),
						new Vector2(2, 3)),
				new ProjectionObservation(invalidFrame, identityMap(),
						new Vector2(2, 5))), POLICY);
		SpatialPointCertificate undefined = evaluator.evaluate(List.of(
				new ProjectionObservation(horizontalFrame(), identityMap(),
						new Vector2(2, 3)),
				new ProjectionObservation(verticalFrame(), identityMap(),
						new Vector2(Double.NaN, 5))), POLICY);
		final SpatialPointCertificate arithmeticUndefined = evaluator.evaluate(List.of(
				new ProjectionObservation(horizontalFrame(), identityMap(),
						new Vector2(2, 3)),
				new ProjectionObservation(arithmeticOverflowFrame, identityMap(),
						new Vector2(2, 5))), POLICY);

		assertEquals(SpatialCertificateStatus.DEGENERATE, degenerate.getStatus());
		assertEquals(SpatialDefinitionStatus.DEGENERATE,
				degenerate.getDefinitionStatus());
		assertFalse(degenerate.hasPayload());
		assertEquals(SpatialCertificateStatus.UNDEFINED, undefined.getStatus());
		assertEquals(SpatialDefinitionStatus.UNDEFINED,
				undefined.getDefinitionStatus());
		assertFalse(undefined.hasPayload());
		assertEquals(SpatialDefinitionStatus.UNDEFINED,
				arithmeticOverflowFrame.validate(POLICY));
		assertEquals(SpatialCertificateStatus.UNDEFINED,
				arithmeticUndefined.getStatus());
		assertEquals(SpatialDefinitionStatus.UNDEFINED,
				arithmeticUndefined.getDefinitionStatus());
		assertFalse(arithmeticUndefined.hasPayload());
	}

	@Test
	void finiteCandidateWithOverflowingCandidateOriginArithmeticIsUndefined() {
		double extreme = 1e308;
		ProjectionFrameDefinition offsetProfile =
				ProjectionFrameDefinition.orthographic(new Vector3(extreme, 0, 0),
						new Vector3(0, 1, 0), new Vector3(0, 0, 1),
						FrameHandedness.RIGHT_HANDED, UNIT, 1);
		SpatialPointCertificate result = evaluator.evaluate(List.of(
				new ProjectionObservation(offsetProfile, identityMap(),
						new Vector2(3, 5)),
				new ProjectionObservation(horizontalFrame(), identityMap(),
						new Vector2(-extreme, 3)),
				new ProjectionObservation(verticalFrame(), identityMap(),
						new Vector2(-extreme, 5))), POLICY);

		assertEquals(3, result.getRank());
		assertEquals(SpatialCertificateStatus.UNDEFINED, result.getStatus());
		assertEquals(SpatialDefinitionStatus.UNDEFINED,
				result.getDefinitionStatus());
		assertFalse(result.hasPayload());
		assertTrue(result.getResidualEvidence().isEmpty());
	}

	@Test
	void nearParallelFramesFailConditioningWithoutPublishingCandidate() {
		double angle = 1e-7;
		ProjectionFrameDefinition nearParallel =
				ProjectionFrameDefinition.orthographic(new Vector3(0, 0, 0),
						new Vector3(1, 0, 0),
						new Vector3(0, Math.cos(angle), Math.sin(angle)),
						FrameHandedness.RIGHT_HANDED, UNIT, 1);
		NumericPolicy strictCondition = new NumericPolicy(
				1e-9, 1e-9, 1e-12, 1e-9, 1e-9, 1e5);
		SpatialPointCertificate result = evaluator.evaluate(List.of(
				new ProjectionObservation(horizontalFrame(), identityMap(),
						new Vector2(2, 3)),
				new ProjectionObservation(nearParallel, identityMap(),
						nearParallel.project(new Vector3(2, 3, 5)))), strictCondition);

		assertEquals(3, result.getRank());
		assertTrue(result.getConditionNumber() > strictCondition.getConditionLimit());
		assertEquals(SpatialCertificateStatus.DEGENERATE, result.getStatus());
		assertFalse(result.hasPayload());
	}

	@Test
	void decimalOracleNearRankCasesFallOnTheSpecifiedSidesOfTheThreshold() {
		NumericPolicy referencePolicy = new NumericPolicy(
				1e-10, 1e-10, 1e-12, 1e-10, 1e-10, 1e12);
		Vector3 expected = new Vector3(2, 3, 5);
		ProjectionFrameDefinition belowFrame = referenceNearRankFrame(1e-12);
		ProjectionFrameDefinition aboveFrame = referenceNearRankFrame(8e-12);
		SpatialPointCertificate below = evaluator.evaluate(List.of(
				new ProjectionObservation(horizontalFrame(), identityMap(),
						horizontalFrame().project(expected)),
				new ProjectionObservation(belowFrame, identityMap(),
						belowFrame.project(expected))), referencePolicy);
		SpatialPointCertificate above = evaluator.evaluate(List.of(
				new ProjectionObservation(horizontalFrame(), identityMap(),
						horizontalFrame().project(expected)),
				new ProjectionObservation(aboveFrame, identityMap(),
						aboveFrame.project(expected))), referencePolicy);

		assertEquals(2, below.getRank());
		assertEquals(SpatialCertificateStatus.UNDERDETERMINED, below.getStatus());
		assertFalse(below.hasPayload());
		assertEquals(3, above.getRank());
		assertEquals(SpatialCertificateStatus.VALID, above.getStatus());
		assertEquals(SpatialCurrentnessStatus.CURRENT, above.getCurrentness());
		assertTrue(above.hasPayload());
		assertTrue(above.getPoint().orElseThrow().isFinite());
		assertTrue(above.getMaximumIntrinsicNormalizedResidual() <= 1);
		assertTrue(above.getConditionNumber()
				< referencePolicy.getMaximumConditionNumber());
	}

	@Test
	void dynamicValidFailureRecoveryNeverLeaksStalePayload() {
		SpatialPointCertificate first = evaluator.evaluate(observations(
				new Vector2(2, 3), new Vector2(2, 5)), POLICY);
		SpatialPointCertificate failed = evaluator.evaluate(observations(
				new Vector2(2, 3), new Vector2(9, 5)), POLICY);
		SpatialPointCertificate recovered = evaluator.evaluate(observations(
				new Vector2(2, 3), new Vector2(2, 5)), POLICY);

		assertValidPoint(first, new Vector3(2, 3, 5));
		assertEquals(SpatialCertificateStatus.INCONSISTENT_PROJECTIONS,
				failed.getStatus());
		assertFalse(failed.hasPayload());
		assertValidPoint(recovered, new Vector3(2, 3, 5));
		assertTrue(first.getPoint().isPresent());
	}

	@Test
	void presentationInputsAndEqualDependencyCreationOrderAreSemanticallyAbsent() {
		List<ProjectionObservation> ordered = keyedObservations();
		SpatialPointCertificate baseline = evaluator.evaluate(ordered, POLICY);
		SpatialPointCertificate repeated = evaluator.evaluate(keyedObservations(), POLICY);
		SpatialPointCertificate permuted = evaluator.evaluate(List.of(
				ordered.get(1), ordered.get(0)), POLICY);

		assertEquals(baseline.getStatus(), repeated.getStatus());
		assertVectorEquals(baseline.getPoint().orElseThrow(),
				repeated.getPoint().orElseThrow());
		assertEquals(baseline.getMaximumIntrinsicNormalizedResidual(),
				repeated.getMaximumIntrinsicNormalizedResidual());
		assertEquals(baseline.getStatus(), permuted.getStatus());
		assertEquals(baseline.getProjectionSystemStatus(),
				permuted.getProjectionSystemStatus());
		assertVectorEquals(baseline.getPoint().orElseThrow(),
				permuted.getPoint().orElseThrow());
		assertEquals(baseline.getRank(), permuted.getRank());
		assertArrayEquals(baseline.getSingularValues(), permuted.getSingularValues(),
				1e-12);
		assertEquals(baseline.getConditionNumber(), permuted.getConditionNumber(),
				1e-12);
		assertEquals(baseline.getMaximumIntrinsicNormalizedResidual(),
				permuted.getMaximumIntrinsicNormalizedResidual(), 1e-12);
		assertEquals(baseline.getMaximumDiagramNormalizedResidual(),
				permuted.getMaximumDiagramNormalizedResidual(), 1e-12);
		assertEquals(baseline.getResidualEvidence().size(),
				permuted.getResidualEvidence().size());
		for (int index = 0; index < baseline.getResidualEvidence().size(); index++) {
			assertEquals(baseline.getResidualEvidence().get(index).getEvidenceKey(),
					permuted.getResidualEvidence().get(index).getEvidenceKey());
			assertEquals(baseline.getResidualEvidence().get(index)
					.getNormalizedIntrinsicResidual(), permuted.getResidualEvidence()
							.get(index).getNormalizedIntrinsicResidual(), 1e-12);
			assertEquals(baseline.getResidualEvidence().get(index)
					.getNormalizedDiagramResidual(), permuted.getResidualEvidence()
							.get(index).getNormalizedDiagramResidual(), 1e-12);
		}
		assertEquals("binding:h",
				baseline.getResidualEvidence().get(0).getEvidenceKey());
		assertEquals("binding:v",
				baseline.getResidualEvidence().get(1).getEvidenceKey());
		assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(List.of(
				keyedObservation("binding:conflict", horizontalFrame(),
						new Vector2(2, 3)),
				keyedObservation("binding:conflict", verticalFrame(),
						new Vector2(2, 5))), POLICY));
	}

	@Test
	void unsupportedAndEveryNonValidCertificateStateCarryNoPayload() {
		ProjectionSystemCertificate unsupportedSystem =
				new ProjectionSystemCertificate(SpatialCapabilityStatus.UNSUPPORTED,
						ProjectionSystemStatus.NOT_EVALUATED, Collections.emptyList(),
						0, 0, 0);
		SpatialPointCertificate unsupportedPoint = new SpatialPointCertificate(
				SpatialCapabilityStatus.UNSUPPORTED, unsupportedSystem,
				SpatialDefinitionStatus.UNDEFINED,
				SpatialCertificateStatus.NOT_EVALUATED,
				SpatialCurrentnessStatus.FAILED_CURRENT_REVISION,
				RepresentationFidelity.NUMERICAL,
				NumericalEvidenceStatus.NOT_APPLICABLE,
				CorrespondenceStatus.NOT_REQUIRED, 0, Optional.empty(), 0,
				new double[0], Double.NaN, Collections.emptyList(),
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, "not evaluated");

		assertFalse(unsupportedPoint.hasPayload());
		assertEquals(SpatialCertificateStatus.NOT_EVALUATED,
				unsupportedPoint.getStatus());
		assertThrows(IllegalArgumentException.class,
				() -> new ProjectionSystemCertificate(
						SpatialCapabilityStatus.UNSUPPORTED,
						ProjectionSystemStatus.INCONSISTENT, Collections.emptyList(),
						0, 0, 0));
		ProjectionSystemCertificate consistentSystem =
				evaluator.evaluate(observations(new Vector2(2, 3),
						new Vector2(2, 5)), POLICY).getProjectionSystemCertificate();
		assertThrows(IllegalArgumentException.class,
				() -> new SpatialPointCertificate(SpatialCapabilityStatus.SUPPORTED,
						consistentSystem, SpatialDefinitionStatus.DEFINED,
						SpatialCertificateStatus.VALID,
						SpatialCurrentnessStatus.CURRENT,
						RepresentationFidelity.NUMERICAL,
						NumericalEvidenceStatus.ESTIMATED_ERROR,
						CorrespondenceStatus.NOT_REQUIRED, 0,
						Optional.of(new Vector3(Double.NaN, 2, 3)), 3,
						new double[] {1, 1, 1}, 1, Collections.emptyList(),
						0, 0, "test"));
		for (SpatialCertificateStatus status : SpatialCertificateStatus.values()) {
			if (status == SpatialCertificateStatus.VALID) {
				continue;
			}
			assertThrows(IllegalArgumentException.class,
					() -> new SpatialPointCertificate(SpatialCapabilityStatus.SUPPORTED,
							consistentSystem,
							SpatialDefinitionStatus.DEFINED, status,
							SpatialCurrentnessStatus.CURRENT,
							RepresentationFidelity.NUMERICAL,
							NumericalEvidenceStatus.ESTIMATED_ERROR,
							CorrespondenceStatus.NOT_REQUIRED, 0,
							Optional.of(new Vector3(1, 2, 3)), 3,
							new double[] {1, 1, 1}, 1, Collections.emptyList(),
							0, 0, "test"));
		}
		SpatialPointCertificate validPoint = evaluator.evaluate(observations(
				new Vector2(2, 3), new Vector2(2, 5)), POLICY);
		assertThrows(IllegalArgumentException.class,
				() -> new SpatialPointCertificate(SpatialCapabilityStatus.SUPPORTED,
						validPoint.getProjectionSystemCertificate(),
						SpatialDefinitionStatus.DEFINED,
						SpatialCertificateStatus.VALID,
						SpatialCurrentnessStatus.CURRENT,
						validPoint.getRepresentationFidelity(),
						validPoint.getNumericalEvidenceStatus(),
						validPoint.getCorrespondenceStatus(),
						validPoint.getSourceRevision(), validPoint.getPoint(),
						validPoint.getRank(), validPoint.getSingularValues(),
						validPoint.getConditionNumber(),
						validPoint.getResidualEvidence(), 1.01,
						validPoint.getMaximumDiagramNormalizedResidual(),
						validPoint.getArithmeticMethod()));
	}

	private static List<ProjectionObservation> observations(Vector2 horizontal,
			Vector2 vertical) {
		return List.of(new ProjectionObservation(horizontalFrame(), identityMap(),
				horizontal), new ProjectionObservation(verticalFrame(), identityMap(),
				vertical));
	}

	private static List<ProjectionObservation> keyedObservations() {
		return List.of(keyedObservation("binding:h", horizontalFrame(),
				new Vector2(2, 3)), keyedObservation("binding:v", verticalFrame(),
				new Vector2(2, 5)));
	}

	private static ProjectionObservation keyedObservation(String evidenceKey,
			ProjectionFrameDefinition frame, Vector2 point) {
		return new ProjectionObservation(evidenceKey, frame, identityMap(), point,
				SpatialDefinitionStatus.DEFINED, RepresentationFidelity.NUMERICAL,
				CorrespondenceStatus.NOT_REQUIRED, 1);
	}

	private static ProjectionFrameDefinition horizontalFrame() {
		return ProjectionFrameDefinition.orthographic(new Vector3(0, 0, 0),
				new Vector3(1, 0, 0), new Vector3(0, 1, 0),
				FrameHandedness.RIGHT_HANDED, UNIT, 1);
	}

	private static ProjectionFrameDefinition verticalFrame() {
		return ProjectionFrameDefinition.orthographic(new Vector3(0, 0, 0),
				new Vector3(1, 0, 0), new Vector3(0, 0, 1),
				FrameHandedness.RIGHT_HANDED, UNIT, 1);
	}

	private static ProjectionFrameDefinition referenceNearRankFrame(double t) {
		double denominator = 1 + t * t;
		double cosine = (1 - t * t) / denominator;
		double sine = 2 * t / denominator;
		return ProjectionFrameDefinition.orthographic(new Vector3(0, 0, 0),
				new Vector3(cosine, 0, -sine), new Vector3(0, 1, 0),
				FrameHandedness.RIGHT_HANDED, UNIT, 1);
	}

	private static DiagramMapDefinition identityMap() {
		return DiagramMapDefinition.orientedIsometry(1, 0, 0, 1,
				new Vector2(0, 0), DiagramOrientation.PRESERVING, UNIT, 1);
	}

	private static void assertValidPoint(SpatialPointCertificate result,
			Vector3 expected) {
		assertEquals(SpatialCertificateStatus.VALID, result.getStatus());
		assertEquals(SpatialCurrentnessStatus.CURRENT, result.getCurrentness());
		assertTrue(result.getPoint().isPresent());
		assertVectorEquals(expected, result.getPoint().orElseThrow());
	}

	private static void assertVectorEquals(Vector3 expected, Vector3 actual) {
		assertEquals(expected.getX(), actual.getX(), 1e-8);
		assertEquals(expected.getY(), actual.getY(), 1e-8);
		assertEquals(expected.getZ(), actual.getZ(), 1e-8);
	}
}
