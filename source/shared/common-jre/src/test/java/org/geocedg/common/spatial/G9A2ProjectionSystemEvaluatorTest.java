/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.spatial.semantic.DiagramMapDefinition;
import org.geocedg.common.kernel.spatial.semantic.DiagramMapEvidence;
import org.geocedg.common.kernel.spatial.semantic.DiagramMapFamily;
import org.geocedg.common.kernel.spatial.semantic.DiagramOrientation;
import org.geocedg.common.kernel.spatial.semantic.FoldSide;
import org.geocedg.common.kernel.spatial.semantic.FrameHandedness;
import org.geocedg.common.kernel.spatial.semantic.NumericPolicy;
import org.geocedg.common.kernel.spatial.semantic.ProjectionDefinedPointEvaluator;
import org.geocedg.common.kernel.spatial.semantic.ProjectionFrameDefinition;
import org.geocedg.common.kernel.spatial.semantic.ProjectionObservation;
import org.geocedg.common.kernel.spatial.semantic.ProjectionRelationDefinition;
import org.geocedg.common.kernel.spatial.semantic.ProjectionRelationEvidence;
import org.geocedg.common.kernel.spatial.semantic.ProjectionRelationKind;
import org.geocedg.common.kernel.spatial.semantic.ProjectionRelationOrientation;
import org.geocedg.common.kernel.spatial.semantic.ProjectionRelationProvenance;
import org.geocedg.common.kernel.spatial.semantic.ProjectionResidualEvidence;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemCertificate;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemEvaluator;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialCapabilityStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialCertificateStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialPointCertificate;
import org.geocedg.common.kernel.spatial.semantic.SpatialSemanticInstrumentation;
import org.geocedg.common.kernel.spatial.semantic.Vector2;
import org.geocedg.common.kernel.spatial.semantic.Vector3;
import org.junit.jupiter.api.Test;

class G9A2ProjectionSystemEvaluatorTest {
	private static final String UNIT = "model-unit";
	private static final NumericPolicy POLICY = new NumericPolicy(
			1e-9, 1e-9, 1e-12, 1e-9, 1e-9, 1e10);
	private final ProjectionSystemEvaluator evaluator = new ProjectionSystemEvaluator();

	@Test
	void mapRoundTripsIntrinsicAndDiagramCoordinates() {
		DiagramMapDefinition map = new DiagramMapDefinition(
				DiagramMapFamily.UNIT_SIMILARITY, 0, -2, 2, 0,
				new Vector2(7, -4), 2, DiagramOrientation.PRESERVING,
				UNIT, "diagram-unit", 3);
		Vector2 intrinsic = new Vector2(2, 5);
		Vector2 diagram = map.forward(intrinsic, POLICY);
		ProjectionSystemCertificate certificate = evaluator.evaluate(
				List.of(horizontalFrame()), List.of(map), List.of(), POLICY);
		DiagramMapEvidence evidence = certificate.getMapEvidence().get(0);

		assertEquals(ProjectionSystemStatus.CONSISTENT, map.validate(POLICY));
		assertVectorEquals(intrinsic, map.inverse(diagram, POLICY));
		assertVectorEquals(new Vector2(-3, 0), diagram);
		assertEquals(ProjectionSystemStatus.CONSISTENT, certificate.getStatus());
		assertEquals(1, certificate.getMapEvidence().size());
		assertEquals(DiagramMapFamily.UNIT_SIMILARITY, evidence.getFamily());
		assertEquals(DiagramOrientation.PRESERVING, evidence.getOrientation());
		assertEquals(UNIT, evidence.getSourceUnit());
		assertEquals("diagram-unit", evidence.getDiagramUnit());
		assertEquals(2, evidence.getDeclaredScale(), 0);
		assertEquals(3, evidence.getRevision());
		assertEquals(ProjectionSystemStatus.CONSISTENT, evidence.getStatus());
	}

	@Test
	void coherentCommonDiagramGaugeDoesNotChangeReconstructedPoint() {
		ProjectionFrameDefinition horizontal = horizontalFrame();
		ProjectionFrameDefinition vertical = verticalFrame();
		DiagramMapDefinition identity = identityMap();
		DiagramMapDefinition gauge = DiagramMapDefinition.orientedIsometry(
				0, -1, 1, 0, new Vector2(7, -4),
				DiagramOrientation.PRESERVING, UNIT, 1);
		ProjectionRelationDefinition baselineRelation =
				ProjectionRelationDefinition.hingeUnfold(horizontal, identity, vertical,
						identity, supportStart(), supportEnd(),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.SAME_DIAGRAM_SIDE, 1);
		ProjectionRelationDefinition transformedRelation =
				ProjectionRelationDefinition.hingeUnfold(horizontal, gauge, vertical,
						gauge, supportStart(), supportEnd(),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.SAME_DIAGRAM_SIDE, 1);
		ProjectionDefinedPointEvaluator pointEvaluator =
				new ProjectionDefinedPointEvaluator();
		SpatialPointCertificate baseline = pointEvaluator.evaluate(List.of(
				new ProjectionObservation(horizontal, identity, new Vector2(2, 3)),
				new ProjectionObservation(vertical, identity, new Vector2(2, 5))),
				List.of(baselineRelation), POLICY,
				new SpatialSemanticInstrumentation());
		SpatialPointCertificate transformed = pointEvaluator.evaluate(List.of(
				new ProjectionObservation(horizontal, gauge,
						gauge.forward(new Vector2(2, 3))),
				new ProjectionObservation(vertical, gauge,
						gauge.forward(new Vector2(2, 5)))),
				List.of(transformedRelation), POLICY,
				new SpatialSemanticInstrumentation());

		assertEquals(SpatialCertificateStatus.VALID, baseline.getStatus());
		assertEquals(SpatialCertificateStatus.VALID, transformed.getStatus());
		assertVectorEquals(baseline.getPoint().orElseThrow(),
				transformed.getPoint().orElseThrow());
		DiagramMapEvidence fixedIsometry = baseline.getProjectionSystemCertificate()
				.getMapEvidence().get(0);
		assertEquals(DiagramMapFamily.ORIENTED_ISOMETRY,
				fixedIsometry.getFamily());
		assertEquals(DiagramOrientation.PRESERVING,
				fixedIsometry.getOrientation());
		assertEquals(UNIT, fixedIsometry.getSourceUnit());
		assertEquals(UNIT, fixedIsometry.getDiagramUnit());
		assertEquals(1, fixedIsometry.getDeclaredScale(), 0);
		assertEquals(1, fixedIsometry.getRevision());
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				fixedIsometry.getStatus());
		assertEquivalentSystemEvidence(baseline.getProjectionSystemCertificate(),
				transformed.getProjectionSystemCertificate());
		assertEquivalentPointEvidence(baseline, transformed);
	}

	@Test
	void translatedNonOrthogonalPlanesValidateTheirActualIntersectionLine() {
		double rootHalf = Math.sqrt(0.5);
		ProjectionFrameDefinition first = ProjectionFrameDefinition.orthographic(
				new Vector3(0, 0, 1), new Vector3(1, 0, 0),
				new Vector3(0, 1, 0), FrameHandedness.RIGHT_HANDED, UNIT, 1);
		ProjectionFrameDefinition second = ProjectionFrameDefinition.orthographic(
				new Vector3(0, 1.5, 1.5), new Vector3(1, 0, 0),
				new Vector3(0, rootHalf, -rootHalf),
				FrameHandedness.RIGHT_HANDED, UNIT, 1);
		DiagramMapDefinition firstMap = identityMap();
		DiagramMapDefinition secondMap = DiagramMapDefinition.orientedIsometry(
				1, 0, 0, 1, new Vector2(0, 2 - rootHalf),
				DiagramOrientation.PRESERVING, UNIT, 1);
		ProjectionRelationDefinition relation =
				ProjectionRelationDefinition.hingeUnfold(first, firstMap, second,
						secondMap, new Vector3(0, 2, 1), new Vector3(-1, 2, 1),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.SAME_DIAGRAM_SIDE, 1);

		ProjectionSystemCertificate result = evaluator.evaluate(List.of(first, second),
				List.of(firstMap, secondMap), List.of(relation), POLICY);

		assertEquals(ProjectionSystemStatus.CONSISTENT, result.getStatus());
		assertEquals(0, result.getRelationEvidence().get(0)
				.getLineOffsetResidual(), 1e-9);
		assertEquals(0, result.getRelationEvidence().get(0)
				.getDirectionResidual(), 1e-9);
		assertTrue(result.getMaximumNormalizedResidual() <= 1);
	}

	@Test
	void relationEndpointsBecomePartOfTheExactEvaluatedSubcontext() {
		ProjectionFrameDefinition horizontal = horizontalFrame();
		ProjectionFrameDefinition vertical = verticalFrame();
		DiagramMapDefinition source = identityMap();
		DiagramMapDefinition destination = DiagramMapDefinition.orientedIsometry(
				1, 0, 0, 1, new Vector2(7, 0),
				DiagramOrientation.PRESERVING, UNIT, 1);
		ProjectionRelationDefinition relation =
				ProjectionRelationDefinition.hingeUnfold(horizontal, source,
						vertical, destination, supportStart(), supportEnd(),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.SAME_DIAGRAM_SIDE, 1);

		ProjectionSystemCertificate result = evaluator.evaluate(
				List.of(horizontal), List.of(source), List.of(relation), POLICY);

		assertEquals(ProjectionSystemStatus.CONSISTENT, result.getStatus());
		assertEquals(2, result.getEvaluatedFrameCount());
		assertEquals(2, result.getEvaluatedMapCount());
		assertEquals(2, result.getMapEvidence().size());
		assertEquals(1, result.getRelationEvidence().size());
		assertThrows(IllegalArgumentException.class,
				() -> new ProjectionSystemCertificate(
						SpatialCapabilityStatus.SUPPORTED,
						ProjectionSystemStatus.CONSISTENT,
						result.getMapEvidence().subList(0, 1),
						result.getRelationEvidence(), 2, 2, 0));
		assertThrows(IllegalArgumentException.class,
				() -> new ProjectionSystemCertificate(
						SpatialCapabilityStatus.SUPPORTED,
						ProjectionSystemStatus.CONSISTENT,
						result.getMapEvidence(), result.getRelationEvidence(),
						2, 2, 1.01));
		ProjectionRelationEvidence excessiveRelation =
				new ProjectionRelationEvidence("relation:excessive",
						ProjectionRelationKind.HINGE_UNFOLD,
						ProjectionSystemStatus.CONSISTENT, 0, 0, 0, 0, 1,
						1.01);
		assertThrows(IllegalArgumentException.class,
				() -> new ProjectionSystemCertificate(
						SpatialCapabilityStatus.SUPPORTED,
						ProjectionSystemStatus.CONSISTENT,
						result.getMapEvidence(), List.of(excessiveRelation),
						2, 2, 1));
	}

	@Test
	void foldSideAndChangeOfPlaneAreExplicitlyTyped() {
		ProjectionRelationDefinition validHinge =
				ProjectionRelationDefinition.hingeUnfold(horizontalFrame(), identityMap(),
						verticalFrame(), identityMap(), supportStart(), supportEnd(),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.SAME_DIAGRAM_SIDE, 1);
		ProjectionRelationDefinition invalidHinge = new ProjectionRelationDefinition(
				ProjectionRelationKind.HINGE_UNFOLD, horizontalFrame(), identityMap(),
				verticalFrame(), identityMap(), supportStart(), supportEnd(),
				ProjectionRelationOrientation.POSITIVE,
				ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
				FoldSide.NOT_APPLICABLE, 1);
		ProjectionRelationDefinition changeOfPlane =
				ProjectionRelationDefinition.changeOfPlane(horizontalFrame(), identityMap(),
						verticalFrame(), identityMap(), supportStart(), supportEnd(),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION, 1);
		ProjectionRelationDefinition invalidChange = new ProjectionRelationDefinition(
				ProjectionRelationKind.CHANGE_OF_PLANE, horizontalFrame(), identityMap(),
				verticalFrame(), identityMap(), supportStart(), supportEnd(),
				ProjectionRelationOrientation.POSITIVE,
				ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
				FoldSide.SAME_DIAGRAM_SIDE, 1);
		DiagramMapDefinition reversing = DiagramMapDefinition.orientedIsometry(
				1, 0, 0, -1, new Vector2(0, 0),
				DiagramOrientation.REVERSING, UNIT, 1);
		ProjectionRelationDefinition oppositeFold =
				ProjectionRelationDefinition.hingeUnfold(horizontalFrame(), identityMap(),
						verticalFrame(), reversing, supportStart(), supportEnd(),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.OPPOSITE_DIAGRAM_SIDE, 1);
		ProjectionSystemCertificate oppositeFoldResult = evaluate(oppositeFold);

		assertEquals(ProjectionSystemStatus.CONSISTENT,
				evaluate(validHinge).getStatus());
		assertEquals(ProjectionSystemStatus.INCONSISTENT,
				evaluate(invalidHinge).getStatus());
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				evaluate(changeOfPlane).getStatus());
		assertEquals(ProjectionSystemStatus.INCONSISTENT,
				evaluate(invalidChange).getStatus());
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				oppositeFoldResult.getStatus());
		assertEquals(DiagramOrientation.REVERSING,
				oppositeFoldResult.getMapEvidence().get(1).getOrientation());
		assertEquals(-1, oppositeFoldResult.getRelationEvidence().get(0)
				.getFoldSideDotProduct(), 1e-12);
		assertThrows(IllegalArgumentException.class,
				() -> ProjectionRelationDefinition.hingeUnfold(horizontalFrame(),
						identityMap(), verticalFrame(), identityMap(),
						supportStart(), supportEnd(),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.NOT_APPLICABLE, 1));
	}

	@Test
	void invalidMapsUnitsAndParallelHingesHaveIndependentSystemStates() {
		DiagramMapDefinition singular = DiagramMapDefinition.orientedIsometry(
				1, 0, 0, 0, new Vector2(0, 0),
				DiagramOrientation.PRESERVING, UNIT, 1);
		DiagramMapDefinition foreignUnit = DiagramMapDefinition.orientedIsometry(
				1, 0, 0, 1, new Vector2(0, 0),
				DiagramOrientation.PRESERVING, "foreign-unit", 1);
		ProjectionRelationDefinition parallel =
				ProjectionRelationDefinition.hingeUnfold(horizontalFrame(), identityMap(),
						horizontalFrame(), identityMap(), supportStart(), supportEnd(),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.SAME_DIAGRAM_SIDE, 1);
		ProjectionSystemCertificate singularResult = evaluator.evaluate(
				List.of(horizontalFrame()), List.of(singular), List.of(), POLICY);
		ProjectionSystemCertificate unitResult = evaluator.evaluateObservations(
				List.of(new ProjectionObservation(horizontalFrame(), foreignUnit,
						new Vector2(2, 3))), List.of(), POLICY,
				new SpatialSemanticInstrumentation());
		ProjectionSystemCertificate bareUnitResult = evaluator.evaluate(
				List.of(horizontalFrame()), List.of(foreignUnit), List.of(), POLICY);

		assertEquals(ProjectionSystemStatus.DEGENERATE, singularResult.getStatus());
		assertEquals(ProjectionSystemStatus.INCONSISTENT, unitResult.getStatus());
		assertEquals(ProjectionSystemStatus.INCONSISTENT, bareUnitResult.getStatus());
		assertEquals(ProjectionSystemStatus.DEGENERATE,
				evaluate(parallel).getStatus());
	}

	@Test
	void malformedSupportLinesAreRejectedWithoutInferredContinuity() {
		ProjectionRelationDefinition nonfinite =
				ProjectionRelationDefinition.hingeUnfold(horizontalFrame(), identityMap(),
						verticalFrame(), identityMap(), supportStart(),
						new Vector3(Double.POSITIVE_INFINITY, 0, 0),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.SAME_DIAGRAM_SIDE, 1);
		ProjectionRelationDefinition coincident =
				ProjectionRelationDefinition.hingeUnfold(horizontalFrame(), identityMap(),
						verticalFrame(), identityMap(), supportStart(), supportStart(),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.SAME_DIAGRAM_SIDE, 1);
		ProjectionRelationDefinition offPlane =
				ProjectionRelationDefinition.hingeUnfold(horizontalFrame(), identityMap(),
						verticalFrame(), identityMap(), new Vector3(0, 0, 1),
						new Vector3(1, 0, 1), ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.SAME_DIAGRAM_SIDE, 1);
		ProjectionRelationDefinition wrongOrientation =
				ProjectionRelationDefinition.hingeUnfold(horizontalFrame(), identityMap(),
						verticalFrame(), identityMap(), supportStart(),
						new Vector3(-1, 0, 0), ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.SAME_DIAGRAM_SIDE, 1);

		assertEquals(ProjectionSystemStatus.UNDEFINED,
				evaluate(nonfinite).getStatus());
		assertEquals(ProjectionSystemStatus.DEGENERATE,
				evaluate(coincident).getStatus());
		assertEquals(ProjectionSystemStatus.INCONSISTENT,
				evaluate(offPlane).getStatus());
		assertEquals(ProjectionSystemStatus.INCONSISTENT,
				evaluate(wrongOrientation).getStatus());
	}

	@Test
	void emptySystemIsNotEvaluatedAndMissingFrameOrMapIsUndefined() {
		ProjectionSystemCertificate empty = evaluator.evaluate(List.of(), List.of(),
				List.of(), POLICY);
		ProjectionSystemCertificate missingMap = evaluator.evaluate(
				List.of(horizontalFrame()), List.of(), List.of(), POLICY);
		ProjectionSystemCertificate missingFrame = evaluator.evaluate(List.of(),
				List.of(identityMap()), List.of(), POLICY);

		assertEquals(ProjectionSystemStatus.NOT_EVALUATED, empty.getStatus());
		assertEquals(ProjectionSystemStatus.UNDEFINED, missingMap.getStatus());
		assertEquals(ProjectionSystemStatus.UNDEFINED, missingFrame.getStatus());
	}

	@Test
	void verySmallDeclaredUnitSimilarityRemainsInvertibleAndValid() {
		double scale = 1e-12;
		DiagramMapDefinition tiny = DiagramMapDefinition.unitSimilarity(
				scale, 0, 0, scale, new Vector2(0, 0), scale,
				DiagramOrientation.PRESERVING, UNIT, "diagram-unit", 1);
		Vector2 intrinsic = new Vector2(2, 5);

		assertEquals(ProjectionSystemStatus.CONSISTENT, tiny.validate(POLICY));
		assertVectorEquals(intrinsic, tiny.inverse(tiny.forward(intrinsic), POLICY));
	}

	@Test
	void largeDiagramGaugeRejectsJustOverThresholdHingeOffset() {
		double scale = 1e9;
		Vector2 sourceTranslation = new Vector2(1e12, -1e12);
		DiagramMapDefinition source = DiagramMapDefinition.unitSimilarity(
				scale, 0, 0, scale, sourceTranslation, scale,
				DiagramOrientation.PRESERVING, UNIT, "diagram-unit", 1);
		DiagramMapDefinition destination = DiagramMapDefinition.unitSimilarity(
				scale, 0, 0, scale,
				new Vector2(sourceTranslation.getX(),
						sourceTranslation.getY() + 1.01),
				scale, DiagramOrientation.PRESERVING, UNIT, "diagram-unit", 1);
		ProjectionRelationDefinition relation =
				ProjectionRelationDefinition.hingeUnfold(horizontalFrame(), source,
						verticalFrame(), destination, supportStart(), supportEnd(),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.SAME_DIAGRAM_SIDE, 1);

		ProjectionSystemCertificate result = evaluator.evaluate(
				List.of(horizontalFrame(), verticalFrame()),
				List.of(source, destination), List.of(relation), POLICY);
		DiagramMapDefinition unitScale = DiagramMapDefinition.unitSimilarity(
				1, 0, 0, 1, new Vector2(0, 0), 1,
				DiagramOrientation.PRESERVING, UNIT, "diagram-unit", 1);
		DiagramMapDefinition doubleScale = DiagramMapDefinition.unitSimilarity(
				2, 0, 0, 2, new Vector2(0, 0), 2,
				DiagramOrientation.PRESERVING, UNIT, "diagram-unit", 1);
		ProjectionRelationDefinition unequalScaleRelation =
				ProjectionRelationDefinition.hingeUnfold(horizontalFrame(), unitScale,
						verticalFrame(), doubleScale, supportStart(), supportEnd(),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.SAME_DIAGRAM_SIDE, 1);
		ProjectionSystemCertificate unequalScaleResult = evaluator.evaluate(
				List.of(horizontalFrame(), verticalFrame()),
				List.of(unitScale, doubleScale), List.of(unequalScaleRelation), POLICY);
		DiagramMapDefinition expandedOffset = DiagramMapDefinition.unitSimilarity(
				1e9, 0, 0, 1e9, new Vector2(0, 0.01), 1e9,
				DiagramOrientation.PRESERVING, UNIT, "diagram-unit", 1);
		ProjectionRelationDefinition pullbackMismatch =
				ProjectionRelationDefinition.hingeUnfold(horizontalFrame(), unitScale,
						verticalFrame(), expandedOffset, supportStart(), supportEnd(),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.SAME_DIAGRAM_SIDE, 1);
		ProjectionSystemCertificate pullbackMismatchResult = evaluator.evaluate(
				List.of(horizontalFrame(), verticalFrame()),
				List.of(unitScale, expandedOffset), List.of(pullbackMismatch), POLICY);

		assertEquals(ProjectionSystemStatus.INCONSISTENT, result.getStatus());
		assertTrue(result.getMaximumNormalizedResidual() > 1);
		assertTrue(result.getMaximumNormalizedResidual() < 1.1);
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				unequalScaleResult.getStatus());
		assertEquals(0, unequalScaleResult.getMaximumNormalizedResidual(), 1e-12);
		assertEquals(ProjectionSystemStatus.INCONSISTENT,
				pullbackMismatchResult.getStatus());
		assertTrue(pullbackMismatchResult.getMaximumNormalizedResidual() > 1);
	}

	private ProjectionSystemCertificate evaluate(ProjectionRelationDefinition relation) {
		return evaluator.evaluate(List.of(relation.getSourceFrame(),
				relation.getDestinationFrame()), List.of(relation.getSourceMap(),
				relation.getDestinationMap()), List.of(relation), POLICY);
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

	private static DiagramMapDefinition identityMap() {
		return DiagramMapDefinition.orientedIsometry(1, 0, 0, 1,
				new Vector2(0, 0), DiagramOrientation.PRESERVING, UNIT, 1);
	}

	private static Vector3 supportStart() {
		return new Vector3(0, 0, 0);
	}

	private static Vector3 supportEnd() {
		return new Vector3(1, 0, 0);
	}

	private static void assertVectorEquals(Vector2 expected, Vector2 actual) {
		assertEquals(expected.getX(), actual.getX(), 1e-9);
		assertEquals(expected.getY(), actual.getY(), 1e-9);
	}

	private static void assertVectorEquals(Vector3 expected, Vector3 actual) {
		assertEquals(expected.getX(), actual.getX(), 1e-8);
		assertEquals(expected.getY(), actual.getY(), 1e-8);
		assertEquals(expected.getZ(), actual.getZ(), 1e-8);
	}

	private static void assertEquivalentSystemEvidence(
			ProjectionSystemCertificate expected,
			ProjectionSystemCertificate actual) {
		assertEquals(expected.getCapabilityStatus(), actual.getCapabilityStatus());
		assertEquals(expected.getStatus(), actual.getStatus());
		assertEquals(expected.getEvaluatedFrameCount(), actual.getEvaluatedFrameCount());
		assertEquals(expected.getEvaluatedMapCount(), actual.getEvaluatedMapCount());
		assertEquals(expected.getMaximumNormalizedResidual(),
				actual.getMaximumNormalizedResidual(), 1e-12);
		assertEquals(expected.getMapEvidence().size(), actual.getMapEvidence().size());
		for (int index = 0; index < expected.getMapEvidence().size(); index++) {
			DiagramMapEvidence expectedMap = expected.getMapEvidence().get(index);
			DiagramMapEvidence actualMap = actual.getMapEvidence().get(index);
			assertEquals(expectedMap.getFamily(), actualMap.getFamily());
			assertEquals(expectedMap.getOrientation(), actualMap.getOrientation());
			assertEquals(expectedMap.getSourceUnit(), actualMap.getSourceUnit());
			assertEquals(expectedMap.getDiagramUnit(), actualMap.getDiagramUnit());
			assertEquals(expectedMap.getDeclaredScale(), actualMap.getDeclaredScale(), 0);
			assertEquals(expectedMap.getRevision(), actualMap.getRevision());
			assertEquals(expectedMap.getStatus(), actualMap.getStatus());
		}
		assertEquals(expected.getRelationEvidence().size(),
				actual.getRelationEvidence().size());
		for (int index = 0; index < expected.getRelationEvidence().size(); index++) {
			ProjectionRelationEvidence expectedRelation =
					expected.getRelationEvidence().get(index);
			ProjectionRelationEvidence actualRelation =
					actual.getRelationEvidence().get(index);
			assertEquals(expectedRelation.getKind(), actualRelation.getKind());
			assertEquals(expectedRelation.getStatus(), actualRelation.getStatus());
			assertEquals(expectedRelation.getLineOffsetResidual(),
					actualRelation.getLineOffsetResidual(), 1e-12);
			assertEquals(expectedRelation.getDirectionResidual(),
					actualRelation.getDirectionResidual(), 1e-12);
			assertEquals(expectedRelation.getSupportPlaneResidual(),
					actualRelation.getSupportPlaneResidual(), 1e-12);
			assertEquals(expectedRelation.getSupportOrientationResidual(),
					actualRelation.getSupportOrientationResidual(), 1e-12);
			assertEquals(expectedRelation.getFoldSideDotProduct(),
					actualRelation.getFoldSideDotProduct(), 1e-12);
			assertEquals(expectedRelation.getMaximumNormalizedResidual(),
					actualRelation.getMaximumNormalizedResidual(), 1e-12);
		}
	}

	private static void assertEquivalentPointEvidence(SpatialPointCertificate expected,
			SpatialPointCertificate actual) {
		assertEquals(expected.getCapabilityStatus(), actual.getCapabilityStatus());
		assertEquals(expected.getDefinitionStatus(), actual.getDefinitionStatus());
		assertEquals(expected.getStatus(), actual.getStatus());
		assertEquals(expected.getCurrentnessStatus(), actual.getCurrentnessStatus());
		assertEquals(expected.getRepresentationFidelity(),
				actual.getRepresentationFidelity());
		assertEquals(expected.getNumericalEvidenceStatus(),
				actual.getNumericalEvidenceStatus());
		assertEquals(expected.getCorrespondenceStatus(),
				actual.getCorrespondenceStatus());
		assertEquals(expected.getSourceRevision(), actual.getSourceRevision());
		assertEquals(expected.getRank(), actual.getRank());
		assertArrayEquals(expected.getSingularValues(), actual.getSingularValues(), 1e-12);
		assertEquals(expected.getConditionNumber(), actual.getConditionNumber(), 1e-12);
		assertEquals(expected.getMaximumIntrinsicNormalizedResidual(),
				actual.getMaximumIntrinsicNormalizedResidual(), 1e-12);
		assertEquals(expected.getMaximumDiagramNormalizedResidual(),
				actual.getMaximumDiagramNormalizedResidual(), 1e-6);
		assertTrue(expected.getMaximumDiagramNormalizedResidual() <= 1e-6);
		assertTrue(actual.getMaximumDiagramNormalizedResidual() <= 1e-6);
		assertEquals(expected.getArithmeticMethod(), actual.getArithmeticMethod());
		assertEquals(expected.getResidualEvidence().size(),
				actual.getResidualEvidence().size());
		for (int index = 0; index < expected.getResidualEvidence().size(); index++) {
			ProjectionResidualEvidence expectedResidual =
					expected.getResidualEvidence().get(index);
			ProjectionResidualEvidence actualResidual =
					actual.getResidualEvidence().get(index);
			assertEquals(expectedResidual.getObservationIndex(),
					actualResidual.getObservationIndex());
			assertVectorEquals(expectedResidual.getIntrinsicResidual(),
					actualResidual.getIntrinsicResidual());
			assertEquals(expectedResidual.getIntrinsicResidualNorm(),
					actualResidual.getIntrinsicResidualNorm(), 1e-12);
			assertEquals(expectedResidual.getNormalizedIntrinsicResidual(),
					actualResidual.getNormalizedIntrinsicResidual(), 1e-12);
			assertVectorEquals(expectedResidual.getDiagramResidual(),
					actualResidual.getDiagramResidual());
			assertEquals(expectedResidual.getDiagramResidualNorm(),
					actualResidual.getDiagramResidualNorm(), 1e-12);
			assertEquals(expectedResidual.getNormalizedDiagramResidual(),
					actualResidual.getNormalizedDiagramResidual(), 1e-6);
		}
	}
}
