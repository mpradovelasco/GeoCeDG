/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.interaction.IndividualForwardPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionQuery2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResolver2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Forward permission is independent of inverse metric uniqueness, never an R6 upgrade. */
class R5ForwardCreationRoutingTest extends G9U0PublicSurfaceTestBase {

    @Test void regularParabolaQueriesRemainAdmissibleWithoutReclassifyingHistoricalResults() {
        var source = certifiedParabola();
        var policy = LocusPointInteractionPolicy2D.initial(.15);
        for (double x : new double[] {0, .1, .2, .4, .42, .73, 1.13}) {
            double y = x * x + .01;
            var original = new LocusPointInteractionResolver2D().resolve(
                    new LocusPointInteractionQuery2D(source, x, y, policy));
            String before = getApp().getXML();
            try (var selection = LocusPointCreation2D.query(source, x, y, policy)) {
                assertNotEquals(LocusPointCreation2D.Route.REJECTED, selection.getRoute(),
                        () -> x + ": " + original.getReason() + " / " + selection.getFailure());
                assertFalse(selection.getChoices().isEmpty());
                assertEquals(before, getApp().getXML());
                if (original.getReason() == LocusPointInteractionResult2D.Reason.LOCAL_EVIDENCE_INSUFFICIENT) {
                    assertEquals(LocusPointCreation2D.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION,
                            selection.getRoute());
                    assertEquals(original.getStatus(), selection.getHistoricalResult().getStatus());
                    assertEquals(original.getReason(), selection.getHistoricalResult().getReason());
                    assertTrue(selection.getForwardSelection().getChoices().stream().allMatch(choice ->
                            choice.getProximity() == IndividualForwardPointCreation2D.Proximity.FLOATING_POINT_UNCERTIFIED
                            && choice.getComputedWorldResidual() <= .15));
                }
                System.out.println("R5_PARABOLA_ROUTING x=" + x + " y=" + y
                        + " originalR6Reason=" + original.getReason() + " selectedRoute=" + selection.getRoute());
            }
        }
    }

    @Test void certifiedSmallCircleCanOfferAForwardAddressWithoutAMetricMinimumProof() {
        add("c=Circle((0,0),1E-6)"); add("N=Point(c)");
        add("v=Vector((0,0),N)"); add("Q=N+(2*v)");
        GeoLocusV2 source = add("L=LocusV2(Q,N)");
        assertTrue(source.isDefined());
        var policy = LocusPointInteractionPolicy2D.initial(1E-7);
        var original = new LocusPointInteractionResolver2D().resolve(
                new LocusPointInteractionQuery2D(source, 3E-6, 0, policy));
        assertEquals(LocusPointInteractionResult2D.Reason.LOCAL_EVIDENCE_INSUFFICIENT,
                original.getReason());
        var evaluator = assertInstanceOf(ReconstructibleLocusEvaluator2D.class,
                source.getSemanticDefinition().getEvaluatorCapability());
        assertTrue(evaluator.captureCertifiedProgramV3().isPresent());
        try (var selection = LocusPointCreation2D.query(source, 3E-6, 0, policy)) {
            assertEquals(LocusPointCreation2D.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION,
                    selection.getRoute(), selection.getFailure());
            assertEquals(original.getReason(), selection.getHistoricalResult().getReason());
            var forward = selection.getForwardSelection().getChoices().get(0);
            assertEquals(IndividualForwardPointCreation2D.Proximity.FLOATING_POINT_UNCERTIFIED,
                    forward.getProximity());
            assertTrue(Double.isFinite(forward.getComputedWorldResidual()));
            assertTrue(forward.getComputedWorldResidual() <= 1E-7);
            System.out.println("R5_FORWARD_INDEPENDENT originalR6Status=" + original.getStatus()
                    + " originalR6Reason=" + original.getReason() + " route=" + selection.getRoute()
                    + " proximity=" + forward.getProximity() + " residual=" + forward.getComputedWorldResidual()
                    + " radius=" + policy.getMaximumWorldDistance());
            var point = selection.commit("P", selection.getChoices().get(0));
            assertEquals(3E-6, Math.hypot(point.inhomX, point.inhomY), 1E-18);
        }
    }

    @Test void invalidFineDerivativeGuardIsNotMisclassifiedAsOnlyMissingMinimumEvidence() throws Exception {
        var source = certifiedParabola();
        var definition = source.getSemanticDefinition();
        var original = assertInstanceOf(ReconstructibleLocusEvaluator2D.class,
                definition.getEvaluatorCapability());
        var faulted = spy(original);
        AtomicInteger observed = new AtomicInteger();
        doAnswer(invocation -> {
            if (Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(frame ->
                    frame.getClassName().equals(LocusPointInteractionResolver2D.class.getName())
                    && frame.getMethodName().equals("derivativeEstimate"))) {
                observed.incrementAndGet();
                return LocusEvaluation2D.invalid(EvaluationStatus.EVALUATION_FAILED,
                        definition.getBranches().get(0).getQuality(), "injected fine derivative guard failure");
            }
            return invocation.callRealMethod();
        }).when(faulted).evaluate(any(), any(), anyDouble(), any());
        var evaluatorField = LocusDefinition2D.class.getDeclaredField("evaluator");
        evaluatorField.setAccessible(true); evaluatorField.set(definition, faulted);
        try {
            String before = getApp().getXML();
            try (var selection = IndividualForwardPointCreation2D.query(source, .42, .18,
                    LocusPointInteractionPolicy2D.initial(.15))) {
                assertTrue(observed.get() > 0, "The guard injection must actually be evaluated");
                assertEquals(IndividualForwardPointCreation2D.Permission.NOT_OFFERABLE, selection.getPermission());
                assertEquals(IndividualForwardPointCreation2D.Failure.UNSAFE_DISCOVERY, selection.getFailure());
                assertTrue(selection.getChoices().isEmpty());
                assertEquals(0, selection.getPublicationAttempts());
            }
            assertEquals(before, getApp().getXML());
        } finally {
            evaluatorField.set(definition, original);
        }
    }

    @Test void parabolaWorldAndReferenceUseIdenticalIndependentAddressesAndNoQParent() {
        var source = certifiedParabola();
        var policy = LocusPointInteractionPolicy2D.initial(.15);
        GeoPoint reference = add("Reference=(.42,.18)");
        try (var click = LocusPointCreation2D.query(source, .42, .18, policy);
                var fromQ = LocusPointCreation2D.queryFromReference(source, reference, policy)) {
            assertNotEquals(LocusPointCreation2D.Route.REJECTED, click.getRoute(), click.getFailure());
            assertEquals(click.getRoute(), fromQ.getRoute());
            assertEquals(click.getChoices().stream().map(choice -> choice.getAddress()).toList(),
                    fromQ.getChoices().stream().map(choice -> choice.getAddress()).toList());
            var point = fromQ.commit("P", fromQ.getChoices().get(0));
            assertFalse(point.getAllPredecessors().contains(reference));
            assertEquals(point.inhomX * point.inhomX, point.inhomY, 1E-10);
            var parent = (AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
            var address = parent.getCurrentSemanticAddress();
            reference.setCoords(8, 9, 1); reference.updateCascade();
            assertEquals(address, parent.getCurrentSemanticAddress());
            var identity = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
            getApp().setXML(getApp().getXML(), true);
            var restored = (GeoPoint) lookup("P");
            assertEquals(identity, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(restored));
            assertEquals(address, ((AlgoSemanticLocusPoint2D) restored.getParentAlgorithm()).getCurrentSemanticAddress());
        }
    }

    @Test void exhaustedHistoricalSearchCannotOpenForwardRouteOrPublish() {
        var source = certifiedParabola();
        var policy = new LocusPointInteractionPolicy2D(.15, 1E-10, 1E-12,
                new LocusPointInteractionWorkBudget2D(1, 2, 1, 1));
        String before = getApp().getXML();
        try (var selection = LocusPointCreation2D.query(source, .42, .18, policy)) {
            assertEquals(LocusPointCreation2D.Route.REJECTED, selection.getRoute());
            assertEquals("BUDGET_EXHAUSTED", selection.getFailure());
            assertNull(selection.getForwardSelection());
            assertTrue(selection.getChoices().isEmpty());
        }
        assertEquals(before, getApp().getXML());
    }

    @Test void forwardParabolaOfferMustRevalidateRevisionBeforePublication() {
        var source = certifiedParabola();
        var policy = LocusPointInteractionPolicy2D.initial(.15);
        try (var selection = IndividualForwardPointCreation2D.query(source, .42, .18, policy)) {
            assertEquals(IndividualForwardPointCreation2D.Permission.EXPLICIT_FORWARD_SELECTION_REQUIRED,
                    selection.getPermission(), selection.getFailure().name());
            var choice = selection.getChoices().get(0);
            GeoPoint focus = (GeoPoint) lookup("ParabolaFocus");
            focus.setCoords(0, .3, 1); focus.updateCascade();
            String changed = getApp().getXML();
            assertEquals(M08SnapshotPlacement2D.Validity.STALE, selection.getValidity());
            assertEquals(IndividualForwardPointCreation2D.Failure.STALE_SELECTION,
                    assertThrows(IndividualForwardPointCreation2D.Rejected.class,
                            () -> selection.commit("StaleP", choice)).getFailure());
            assertNull(lookup("StaleP")); assertEquals(changed, getApp().getXML());
        }
    }
}
