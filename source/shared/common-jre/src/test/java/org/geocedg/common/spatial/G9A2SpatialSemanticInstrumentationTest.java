/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.kernel.spatial.semantic.DiagramMapDefinition;
import org.geocedg.common.kernel.spatial.semantic.DiagramOrientation;
import org.geocedg.common.kernel.spatial.semantic.FoldSide;
import org.geocedg.common.kernel.spatial.semantic.FrameHandedness;
import org.geocedg.common.kernel.spatial.semantic.NumericPolicy;
import org.geocedg.common.kernel.spatial.semantic.ProjectionDefinedPointEvaluator;
import org.geocedg.common.kernel.spatial.semantic.ProjectionFrameDefinition;
import org.geocedg.common.kernel.spatial.semantic.ProjectionObservation;
import org.geocedg.common.kernel.spatial.semantic.ProjectionRelationDefinition;
import org.geocedg.common.kernel.spatial.semantic.ProjectionRelationOrientation;
import org.geocedg.common.kernel.spatial.semantic.ProjectionRelationProvenance;
import org.geocedg.common.kernel.spatial.semantic.SpatialSemanticInstrumentation;
import org.geocedg.common.kernel.spatial.semantic.Vector2;
import org.geocedg.common.kernel.spatial.semantic.Vector3;
import org.junit.jupiter.api.Test;

class G9A2SpatialSemanticInstrumentationTest {
	private static final String UNIT = "model-unit";
	private static final NumericPolicy POLICY = new NumericPolicy(
			1e-9, 1e-9, 1e-12, 1e-9, 1e-9, 1e10);

	@Test
	void validPointTraceHasDeterministicFunctionalBudget() {
		ProjectionFrameDefinition horizontal = horizontalFrame();
		ProjectionFrameDefinition vertical = verticalFrame();
		DiagramMapDefinition map = identityMap();
		ProjectionRelationDefinition relation =
				ProjectionRelationDefinition.hingeUnfold(horizontal, map, vertical, map,
						new Vector3(0, 0, 0), new Vector3(1, 0, 0),
						ProjectionRelationOrientation.POSITIVE,
						ProjectionRelationProvenance.EXPLICIT_CONSTRUCTION,
						FoldSide.SAME_DIAGRAM_SIDE, 1);
		SpatialSemanticInstrumentation evidence =
				new SpatialSemanticInstrumentation();

		new ProjectionDefinedPointEvaluator().evaluate(List.of(
				new ProjectionObservation(horizontal, map, new Vector2(2, 3)),
				new ProjectionObservation(vertical, map, new Vector2(2, 5))),
				List.of(relation), POLICY, evidence);

		assertEquals(1, evidence.getProjectionSystemEvaluations());
		assertEquals(4, evidence.getFrameEvaluations());
		assertEquals(2, evidence.getDiagramMapInverseEvaluations());
		assertEquals(6, evidence.getDiagramMapForwardEvaluations());
		assertEquals(1, evidence.getHingeConsistencyEvaluations());
		assertEquals(0, evidence.getChangeOfPlaneConsistencyEvaluations());
		assertEquals(1, evidence.getProjectionSystemCertificatePublications());
		assertEquals(0, evidence.getProjectionSystemCertificateRejections());
		assertEquals(1, evidence.getReconstructionAttempts());
		assertEquals(1, evidence.getRankEvaluations());
		assertEquals(1, evidence.getCandidateObjectsBuilt());
		assertEquals(2, evidence.getReprojectionEvaluations());
		assertEquals(1, evidence.getCertificatePublications());
		assertEquals(0, evidence.getFailurePublications());
	}

	@Test
	void forbiddenAuthorityAndStalePublicationCountersAreHardZero() {
		SpatialSemanticInstrumentation evidence =
				new SpatialSemanticInstrumentation();
		evidence.recordDependencyUpdate();

		assertEquals(1, evidence.getDependencyUpdates());
		assertEquals(0, evidence.getLabelFallbackLookups());
		assertEquals(0, evidence.getCoordinateAssociationAttempts());
		assertEquals(0, evidence.getCreationOrderAssociationAttempts());
		assertEquals(0, evidence.getXmlPositionAssociationAttempts());
		assertEquals(0, evidence.getOutputIndexAssociationAttempts());
		assertEquals(0, evidence.getJavaReferenceIdentityAssumptions());
		assertEquals(0, evidence.getVisibleDiagramAssociationAttempts());
		assertEquals(0, evidence.getStalePayloadPublications());
		assertEquals(0, evidence.getMixedAuthorityRevisionPublications());
		assertEquals(0, evidence.getHiddenGraphRecomputations());
		assertEquals(0, evidence.getRenderCacheReads());
		assertEquals(0, evidence.getRendererReads());
		assertEquals(0, evidence.getViewportReads());
		assertEquals(0, evidence.getScreenCoordinateReads());
		assertEquals(0, evidence.getDpiReads());
		assertEquals(0, evidence.getCameraTransformReads());
		assertEquals(0, evidence.getLayerOrVisibilityReads());
		assertEquals(0, evidence.getStaleSpatialPayloadPublications());
	}

	@Test
	void inconsistentSystemRejectsBeforeReconstructionOrMapInverse() {
		DiagramMapDefinition singular = DiagramMapDefinition.orientedIsometry(
				1, 0, 0, 0, new Vector2(0, 0),
				DiagramOrientation.PRESERVING, UNIT, 1);
		SpatialSemanticInstrumentation evidence =
				new SpatialSemanticInstrumentation();

		new ProjectionDefinedPointEvaluator().evaluate(List.of(
				new ProjectionObservation(horizontalFrame(), singular,
						new Vector2(2, 3)),
				new ProjectionObservation(verticalFrame(), identityMap(),
						new Vector2(2, 5))), POLICY, evidence);

		assertEquals(1, evidence.getProjectionSystemEvaluations());
		assertEquals(0, evidence.getProjectionSystemCertificatePublications());
		assertEquals(1, evidence.getProjectionSystemCertificateRejections());
		assertEquals(0, evidence.getReconstructionAttempts());
		assertEquals(0, evidence.getRankEvaluations());
		assertEquals(0, evidence.getCandidateObjectsBuilt());
		assertEquals(0, evidence.getDiagramMapInverseEvaluations());
		assertEquals(1, evidence.getFailurePublications());
	}

	@Test
	void instrumentationRejectsMutationFromAnotherThread() throws Exception {
		SpatialSemanticInstrumentation evidence =
				new SpatialSemanticInstrumentation();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread foreign = new Thread(() -> {
			try {
				evidence.recordDependencyUpdate();
			} catch (Throwable throwable) {
				failure.set(throwable);
			}
		});

		foreign.start();
		foreign.join();

		assertInstanceOf(IllegalStateException.class, failure.get());
		assertTrue(failure.get().getMessage().contains("thread-confined"));
		assertEquals(0, evidence.getDependencyUpdates());
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
}
