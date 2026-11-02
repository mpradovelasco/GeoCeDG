/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D.Failure;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D.Rejected;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D.Route;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionStatus2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Shared creation uses the existing admissions, including terminal general refusals. */
class M08PointCreationKernelTest extends G9U0PublicSurfaceTestBase {

	private static final double REQUEST_X = 0.8976484384297626;
	private static final double REQUEST_Y = 1.7854818601338847;

	private GeoLocusV2 nativeGraph() {
		add("I=(0.4965866445356883,1.7854818601338847)");
		add("J=(-0.5082406249102692,2.5763181370126467)");
		add("f=Circle(I,J)");
		add("D=Point(f)");
		add("g=Line(D,xAxis)");
		add("K=Intersect(g,yAxis)");
		add("M=Midpoint(K,D)");
		return add("h=LocusV2(M,D)");
	}

	private GeoPoint reference() {
		return add("Reference=(" + REQUEST_X + "," + REQUEST_Y + ")");
	}

	private static LocusPointInteractionPolicy2D policy() {
		return LocusPointInteractionPolicy2D.initial(0.1);
	}

	private static LocusPointCreation2D.Choice first(
			LocusPointCreation2D.Selection selection) {
		assertFalse(selection.getChoices().isEmpty(), selection.getFailure());
		return selection.getChoices().get(0);
	}

	private static LocusSemanticAddress2D address(GeoPoint point) {
		return ((AlgoSemanticLocusPoint2D) point.getParentAlgorithm()).getCurrentSemanticAddress();
	}

	@Test
	void clickAndReferenceHaveTheSameH1ChoicesAndForwardConstruction() {
		GeoLocusV2 source = nativeGraph();
		GeoPoint reference = reference();
		String before = getApp().getXML();
		try (var click = LocusPointCreation2D.query(source, REQUEST_X, REQUEST_Y, policy());
				var fromReference = LocusPointCreation2D.queryFromReference(source, reference, policy())) {
			assertEquals(before, getApp().getXML());
			assertEquals(Route.R2_H1, click.getRoute(), click.getFailure());
			assertEquals(click.getRoute(), fromReference.getRoute());
			assertEquals(click.getChoices().stream().map(choice -> choice.getAddress()).toList(),
					fromReference.getChoices().stream().map(choice -> choice.getAddress()).toList());
			assertEquals(LocusPointInteractionResult2D.Reason.GLOBAL_UNIQUENESS_NOT_ESTABLISHED,
					click.getHistoricalResult().getReason());
			GeoPoint clicked = click.commit("Clicked", first(click));
			GeoPoint referred = fromReference.commit("Referred", first(fromReference));
			assertTrue(clicked.isDefined());
			assertEquals(address(clicked), address(referred));
			assertEquals(clicked.getInhomX(), referred.getInhomX(), 0);
			assertEquals(clicked.getInhomY(), referred.getInhomY(), 0);
			assertFalse(referred.getAllPredecessors().contains(reference));
			assertNull(getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(reference));
		}
	}

	@Test
	void existingSplineAdmissionHasPriorityAndDoesNotOpenAnH1Query() {
		GeoLocusV2 source = add("S=SplineV2({(-2,0),(-1,0),(1,0),(2,0)},3)");
		try (var selection = LocusPointCreation2D.query(source, 0.25, 0.01, policy())) {
			assertEquals(Route.R1_R6, selection.getRoute());
			assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
					selection.getHistoricalResult().getStatus());
			assertNull(selection.getLocalSelection());
			GeoPoint point = selection.commit("P", first(selection));
			assertTrue(point.isDefined());
			assertEquals(0.25, point.getInhomX(), 1E-8);
		}
	}

	@Test
	void explicitChoiceFromMultipleHistoricalAddressesKeepsTheChosenPreimage() {
		GeoLocusV2 source = add("S=SplineV2({(-1,0),(0,1),(1,0),(0,1),(-1,0)},3)");
		try (var selection = LocusPointCreation2D.query(source, 0, 1, policy())) {
			assertEquals(Route.R1_R6, selection.getRoute());
			assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,
					selection.getHistoricalResult().getStatus());
			assertTrue(selection.getChoices().size() > 1);
			var choice = selection.getChoices().get(selection.getChoices().size() - 1);
			assertNotEquals(first(selection).getAddress(), choice.getAddress());
			assertEquals(choice.getAddress(), address(selection.commit("P", choice)));
			assertNull(selection.getLocalSelection());
		}
	}

	@Test
	void referenceMoveBeforeConfirmationIsStaleAndDoesNotPublish() {
		GeoLocusV2 source = nativeGraph();
		GeoPoint reference = reference();
		try (var selection = LocusPointCreation2D.queryFromReference(source, reference, policy())) {
			var choice = first(selection);
			reference.setCoords(20, 30, 1);
			reference.updateCascade();
			String before = getApp().getXML();
			assertEquals(M08SnapshotPlacement2D.Validity.STALE, selection.getValidity());
			assertEquals(Failure.STALE_SELECTION,
					assertThrows(Rejected.class, () -> selection.commit("P", choice)).getFailure());
			assertEquals(before, getApp().getXML());
		}
	}

	@Test
	void sourceABAConsumesOldClickSelectionWithoutPublicationOrFallback() {
		GeoLocusV2 source = nativeGraph();
		try (var selection = LocusPointCreation2D.query(source, REQUEST_X, REQUEST_Y, policy())) {
			var choice = first(selection);
			GeoPoint center = (GeoPoint) lookup("I");
			center.setCoords(1, 2, 1);
			center.updateCascade();
			center.setCoords(0.4965866445356883, REQUEST_Y, 1);
			center.updateCascade();
			String before = getApp().getXML();
			assertEquals(Failure.STALE_SELECTION,
					assertThrows(Rejected.class, () -> selection.commit("P", choice)).getFailure());
			assertEquals(M08SnapshotPlacement2D.Validity.CONSUMED, selection.getValidity());
			assertEquals(before, getApp().getXML());
		}
	}

	@Test
	void cancelledAndForeignChoicesCannotPublishOrBeReplayed() {
		GeoLocusV2 source = nativeGraph();
		try (var cancelled = LocusPointCreation2D.query(source, REQUEST_X, REQUEST_Y, policy());
				var first = LocusPointCreation2D.query(source, REQUEST_X, REQUEST_Y, policy());
				var foreign = LocusPointCreation2D.query(source, REQUEST_X, REQUEST_Y, policy())) {
			String before = getApp().getXML();
			var cancelledChoice = first(cancelled);
			assertNull(cancelled.commit("P", null));
			assertEquals(Failure.CONSUMED_SELECTION,
					assertThrows(Rejected.class, () -> cancelled.commit("P", cancelledChoice)).getFailure());
			assertEquals(Failure.FOREIGN_CHOICE,
					assertThrows(Rejected.class, () -> first.commit("P", first(foreign))).getFailure());
			assertEquals(before, getApp().getXML());
		}
	}

	@Test
	void wrongThreadCannotConsumeOrPublishTheOwnersSelection() throws Exception {
		GeoLocusV2 source = nativeGraph();
		try (var selection = LocusPointCreation2D.query(source, REQUEST_X, REQUEST_Y, policy())) {
			var choice = first(selection);
			AtomicReference<Rejected> failure = new AtomicReference<>();
			String before = getApp().getXML();
			Thread other = new Thread(() -> failure.set(assertThrows(Rejected.class,
					() -> selection.commit("P", choice))));
			other.start();
			other.join();
			assertEquals(Failure.WRONG_THREAD, failure.get().getFailure());
			assertEquals(M08SnapshotPlacement2D.Validity.CURRENT, selection.getValidity());
			assertEquals(before, getApp().getXML());
			assertTrue(selection.commit("P", choice).isDefined());
		}
	}

	@Test
	void exhaustedHistoricalSearchIsTerminalAndDoesNotStartH1() {
		GeoLocusV2 source = nativeGraph();
		var limited = new LocusPointInteractionPolicy2D(0.1, 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D(1, 2, 1, 1));
		String before = getApp().getXML();
		try (var selection = LocusPointCreation2D.query(source, REQUEST_X, REQUEST_Y, limited)) {
			assertEquals(Route.REJECTED, selection.getRoute());
			assertEquals(LocusPointInteractionResult2D.Reason.BUDGET_EXHAUSTED,
					selection.getHistoricalResult().getReason());
			assertTrue(selection.getChoices().isEmpty());
			assertNull(selection.getLocalSelection());
			assertEquals(before, getApp().getXML());
		}
	}

	@Test
	void pointCreatedFromReferenceIgnoresQButPropagatesLThroughItsDependents() {
		GeoLocusV2 source = nativeGraph();
		GeoPoint reference = reference();
		GeoPoint point;
		try (var selection = LocusPointCreation2D.queryFromReference(source, reference, policy())) {
			point = selection.commit("P", first(selection));
		}
		GeoPoint dependent = add("Dependent=P+(1,0)");
		double originalX = point.getInhomX();
		reference.setCoords(20, 30, 1);
		reference.updateCascade();
		assertEquals(originalX, point.getInhomX(), 0);
		assertEquals(originalX + 1, dependent.getInhomX(), 1E-12);
		GeoPoint center = (GeoPoint) lookup("I");
		center.setCoords(1, 2, 1);
		center.updateCascade();
		assertTrue(point.isDefined());
		assertNotEquals(originalX, point.getInhomX());
		assertEquals(point.getInhomX() + 1, dependent.getInhomX(), 1E-12);
	}

	@Test
	void clickCreationReopensAsTheSameForwardAddressWithoutARequestParent() {
		GeoLocusV2 source = nativeGraph();
		GeoPoint point;
		try (var selection = LocusPointCreation2D.query(source, REQUEST_X, REQUEST_Y, policy())) {
			point = selection.commit("P", first(selection));
		}
		var pointId = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		var before = address(point);
		String xml = getApp().getXML();
		getApp().setXML(xml, true);
		GeoPoint reopened = (GeoPoint) lookup("P");
		assertTrue(reopened.isDefined());
		assertEquals(pointId, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(reopened));
		assertEquals(before, address(reopened));
		assertEquals(3, reopened.getParentAlgorithm().getInput().length);
	}

	@Test
	void v3VectorSourceDoesNotGainH1PermissionFromItsCertifiedComponent() {
		add("c=Circle((0,0),1)");
		add("N=Point(c)");
		add("v=Vector((0,0),N)");
		// Class v3 captures the inline vector product in a point-plus-vector AST.
		// A separate dependent arithmetic output W=2*v is outside that capture table.
		add("Q=N+(2*v)");
		GeoLocusV2 source = add("L=LocusV2(Q,N)");
		var evaluator = (ReconstructibleLocusEvaluator2D) source.getSemanticDefinition()
				.getEvaluatorCapability();
		assertTrue(evaluator.captureCertifiedProgramV3().isPresent(), evaluator.getProgramV3Refusal());
		assertTrue(evaluator.captureCertifiedProgram().isEmpty());
		var structure = source.getSemanticDefinition().getBranches().get(0).getExistenceStructure();
		assertEquals(LocusExistenceStructure2D.Completeness.COMPLETE, structure.getCompleteness());
		assertFalse(structure.getContinuousValidComponents().isEmpty());
		GeoPoint explicit = add("Explicit=Point(L,\"generator.main\",0)");
		assertTrue(explicit.isDefined());
		assertEquals(3, explicit.getInhomX(), 1E-12);
		assertEquals(0, explicit.getInhomY(), 1E-12);
		String before = getApp().getXML();
		try (var selection = LocusPointCreation2D.query(source, 3, 0, policy())) {
			assertEquals(Route.REJECTED, selection.getRoute());
			assertEquals(LocusPointInteractionResult2D.Reason.GLOBAL_UNIQUENESS_NOT_ESTABLISHED,
					selection.getHistoricalResult().getReason());
			assertNotNull(selection.getLocalSelection());
			assertEquals(M08SnapshotPlacement2D.Failure.LOCAL_EVIDENCE_NOT_ESTABLISHED,
					selection.getLocalSelection().getResult().getFailure());
			assertTrue(selection.getChoices().isEmpty());
			assertEquals(before, getApp().getXML());
		}
	}

	@Test
	void arbitraryScalarFormulaStillNeedsAComponentAndHasNoFallbackPermission() {
		GeoLocusV2 source = createScalarLocus("L", "s", "R", "(s,exp(s))",
				"{false,{-1,1,true,true}}");
		assertTrue(source.getSemanticDefinition().getBranches().get(0).getExistenceStructure()
				.getContinuousValidComponents().isEmpty());
		String before = getApp().getXML();
		try (var selection = LocusPointCreation2D.query(source, 0, 1, policy())) {
			assertEquals(Route.REJECTED, selection.getRoute());
			assertTrue(selection.getChoices().isEmpty());
			assertNull(selection.getLocalSelection());
			assertEquals(before, getApp().getXML());
		}
	}
}
