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

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionStatus2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D.Failure;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D.Permission;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D.Rejected;
import org.geocedg.common.kernel.locus.intersection.M08LocalAddressEvidence2D;
import org.geocedg.common.kernel.locus.intersection.M08LocalAddressEvidence2D.Status;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Real shared-kernel fixtures; the separate native-file probe is also mandatory. */
class M08SnapshotPlacementKernelTest extends G9U0PublicSurfaceTestBase {

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
		return add("ReferenceR1=(0.8976484384297626,1.7854818601338847)");
	}

	private M08SnapshotPlacement2D.Selection select(GeoLocusV2 source) {
		return M08SnapshotPlacement2D.query(source, 0.8976484384297626,
				1.7854818601338847, LocusPointInteractionPolicy2D.initial(0.1));
	}

	private static M08SnapshotPlacement2D.Choice offered(M08SnapshotPlacement2D.Selection selection) {
		assertEquals(Permission.EXPLICIT_LOCAL_SELECTION_REQUIRED,
				selection.getResult().getPermission(), selection.getResult().getFailure().name());
		return selection.getResult().getChoices().get(0);
	}

	@Test
	void generalConstructionGetsIndividualProofWithoutPromotingR6() {
		var source = nativeGraph();
		String before = getApp().getXML();
		var selection = select(source);
		var choice = offered(selection);
		assertEquals(before, getApp().getXML());
		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				selection.getResult().getHistoricalResult().getStatus());
		assertNull(selection.getResult().getHistoricalResult().getUniqueCandidate());
		assertFalse(selection.getResult().getHistoricalResult().getSearchCoverage()
				.establishesCompleteRequestedScope());
		assertEquals(NumericGuarantee.CERTIFIED_ERROR_BOUND, choice.getEvidence().getNumericGuarantee());
		assertTrue(choice.getEvidence().getDerivativeLower(1) > 1);
		assertTrue(choice.getEvidence().getDistanceSquaredUpper() < 0.01);
		assertEquals(selection.getResult().getHistoricalResult().getCandidates().get(0).getAddress(),
				choice.getAddress());
		assertEquals(0, selection.getResult().getHistoricalResult().getInstrumentation().getRenderReads());
	}

	@Test
	void referenceIsNotAssignedIdentityOrAddedAsParentAndItsMotionIsIgnored() {
		var source = nativeGraph();
		GeoPoint q = reference();
		var registry = getConstruction().getSpatialIdentityRegistry();
		assertNull(registry.getPersistentGeoId(q));
		var selection = M08SnapshotPlacement2D.queryFromReference(source, q,
				LocusPointInteractionPolicy2D.initial(0.1));
		var choice = offered(selection);
		GeoPoint point = selection.commit("P", choice);
		assertNotNull(point);
		assertEquals(choice.getAddress(), ((AlgoSemanticLocusPoint2D) point.getParentAlgorithm())
				.getCurrentSemanticAddress());
		assertFalse(point.getAllPredecessors().contains(q));
		assertNull(registry.getPersistentGeoId(q));
		double x = point.getInhomX();
		GeoPoint dependent = add("Dependent=P+(1,0)");
		q.setCoords(20, 30, 1);
		q.updateCascade();
		assertEquals(x, point.getInhomX(), 0);
		assertEquals(x + 1, dependent.getInhomX(), 1E-12);
		GeoPoint i = (GeoPoint) lookup("I");
		i.setCoords(1, 2, 1);
		i.updateCascade();
		assertTrue(point.isDefined());
		assertNotEquals(x, point.getInhomX());
	}

	@Test
	void sourceABAInvalidatesOldChoiceWithoutMutationAtRejection() {
		var source = nativeGraph();
		var selection = select(source);
		var choice = offered(selection);
		GeoPoint i = (GeoPoint) lookup("I");
		i.setCoords(1, 2, 1);
		i.updateCascade();
		i.setCoords(0.4965866445356883, 1.7854818601338847, 1);
		i.updateCascade();
		String before = getApp().getXML();
		assertEquals(Failure.STALE_SELECTION,
				assertThrows(Rejected.class, () -> selection.commit("P", choice)).getFailure());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void changingReferenceBeforeConfirmationRejectsWithoutMutation() {
		var source = nativeGraph();
		GeoPoint q = reference();
		var selection = M08SnapshotPlacement2D.queryFromReference(source, q,
				LocusPointInteractionPolicy2D.initial(0.1));
		var choice = offered(selection);
		q.setCoords(2, 3, 1);
		q.updateCascade();
		String before = getApp().getXML();
		assertEquals(Failure.STALE_SELECTION,
				assertThrows(Rejected.class, () -> selection.commit("P", choice)).getFailure());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void foreignChoiceAndPublicationBypassAreRejected() {
		var source = nativeGraph();
		var first = select(source);
		var second = select(source);
		var choice = offered(first);
		String before = getApp().getXML();
		assertEquals(Failure.PUBLICATION_NOT_AUTHORIZED, assertThrows(Rejected.class,
				() -> LocusV2PublicOperations.createSnapshotSemanticPoint(
						getConstruction(), "P", source, choice)).getFailure());
		assertEquals(Failure.FOREIGN_CHOICE,
				assertThrows(Rejected.class, () -> second.commit("P", choice)).getFailure());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void cancellationAndReplayCannotPublish() {
		var source = nativeGraph();
		var selection = select(source);
		var choice = offered(selection);
		String before = getApp().getXML();
		assertNull(selection.commit("P", null));
		assertEquals(Failure.CONSUMED_SELECTION,
				assertThrows(Rejected.class, () -> selection.commit("P", choice)).getFailure());
		var closed = select(source);
		var closedChoice = offered(closed);
		closed.close();
		assertThrows(Rejected.class, () -> closed.commit("P", closedChoice));
		assertEquals(before, getApp().getXML());
	}

	@Test
	void reloadInvalidatesSourceHandleAndPointRoundTripRetainsForwardAddress() {
		var source = nativeGraph();
		var old = select(source);
		var oldChoice = offered(old);
		var selection = select(source);
		var choice = offered(selection);
		var point = selection.commit("P", choice);
		var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		getApp().setXML(getApp().getXML(), true);
		String before = getApp().getXML();
		assertThrows(Rejected.class, () -> old.commit("Other", oldChoice));
		assertEquals(before, getApp().getXML());
		GeoPoint restored = (GeoPoint) lookup("P");
		assertEquals(id, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(restored));
		assertEquals(choice.getAddress(), ((AlgoSemanticLocusPoint2D) restored.getParentAlgorithm())
				.getCurrentSemanticAddress());
	}

	@Test
	void nativeStyleCreationHasOneNormalUndoRedoBoundary() {
		var source = nativeGraph();
		activateUndo();
		getApp().storeUndoInfo();
		var selection = select(source);
		var choice = offered(selection);
		GeoPoint point = selection.commit("P", choice);
		var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		getApp().storeUndoInfo();
		getKernel().undo();
		assertNull(lookup("P"));
		getKernel().redo();
		assertEquals(id, getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId((GeoPoint) lookup("P")));
	}

	@Test
	void exhaustedDiscoveryBudgetCannotOfferPartialEvidence() {
		var source = nativeGraph();
		String before = getApp().getXML();
		var policy = new LocusPointInteractionPolicy2D(0.1, 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D(1, 2, 1, 1));
		var selection = M08SnapshotPlacement2D.query(source, 0.9, 1.8, policy);
		assertEquals(Failure.BUDGET_EXHAUSTED, selection.getResult().getFailure());
		assertEquals(Permission.NOT_OFFERABLE, selection.getResult().getPermission());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void constructiveAuthorityIsNotSilentlyCertifiedInCapturedConfiguration() {
		var source = nativeGraph();
		getConstruction().getSpatialIdentityRegistry().adoptDocumentAuthority(DocumentAuthorityRecord.CONSTRUCTIVE_V1);
		String before = getApp().getXML();
		assertEquals(Failure.AUTHORITY_NOT_ESTABLISHED, select(source).getResult().getFailure());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void endpointAndPeriodicSeamAreRejectedByInteriorEvidenceContract() {
		var source = nativeGraph();
		var definition = source.getSemanticDefinition();
		var domain = definition.getProvider().getDeclaredDomain();
		assertEquals(Status.DOMAIN_BOUNDARY, M08LocalAddressEvidence2D.establish(definition, BRANCH,
				domain.getLower(), domain, 0, 0, 100).getStatus());
		assertEquals(Status.DOMAIN_BOUNDARY, M08LocalAddressEvidence2D.establish(definition, BRANCH,
				domain.getUpper(), domain, 0, 0, 100).getStatus());
	}

	@Test
	void distanceMustBeProvenRatherThanMerelyFinite() {
		var source = nativeGraph();
		var definition = source.getSemanticDefinition();
		var domain = definition.getProvider().getDeclaredDomain();
		assertEquals(Status.OUTSIDE_RADIUS, M08LocalAddressEvidence2D.establish(definition, BRANCH,
				0, domain, 4, 4, 0.1).getStatus());
		assertFalse(M08LocalAddressEvidence2D.establish(definition, BRANCH,
				0, domain, 0.8976484384297626, 1.7854818601338847, 0.01).isEstablished());
	}

	@Test
	void ordinarySplinePathAndDragKeepTheirHistoricalContract() {
		GeoLocusV2 source = add("S=SplineV2({(-2,0),(-1,0),(1,0),(2,0)},3)");
		var selection = M08SnapshotPlacement2D.query(source, 0.25, 0.01,
				LocusPointInteractionPolicy2D.initial(0.1));
		GeoPoint p = selection.commit("P", offered(selection));
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				LocusV2PublicOperations.moveInteractiveSemanticPoint(p, 0.5, 0,
						LocusPointInteractionPolicy2D.initial(0.1)).getStatus());
	}

	@Test
	void certificateCanEstablishNearAddressThatIsNotADistanceMinimum() {
		var source = nativeGraph();
		var definition = source.getSemanticDefinition();
		var proof = M08LocalAddressEvidence2D.establish(definition, BRANCH, 0,
				definition.getProvider().getDeclaredDomain(), 0.89, 1.8, 0.1);
		assertTrue(proof.isEstablished());
		// F'_x(0)=0, F'_y(0)>1 and q_y differs from F_y(0): d'(0) != 0.
		assertTrue(proof.getDerivativeLower(1) > 1);
		assertTrue(proof.getValueUpper(1) < 1.8);
	}

	@Test
	void zeroDerivativeAtFoldCannotUseFloatingRegularityAsProof() {
		var source = projectedCircle();
		var definition = source.getSemanticDefinition();
		var proof = M08LocalAddressEvidence2D.establish(definition, BRANCH, 0,
				definition.getProvider().getDeclaredDomain(), 1, 0, 0.1);
		assertEquals(Status.REGULARITY_NOT_ESTABLISHED, proof.getStatus());
	}

	@Test
	void disconnectedDomainDoesNotBridgeTheGap() {
		var source = createDisconnectedLine();
		String before = getApp().getXML();
		var selection = M08SnapshotPlacement2D.query(source, 0, 0,
				LocusPointInteractionPolicy2D.initial(0.1));
		assertEquals(Permission.NOT_OFFERABLE, selection.getResult().getPermission());
		assertTrue(selection.getResult().getChoices().isEmpty());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void unknownNonAffineProgramCannotGainCertificateFromNumericalSeed() {
		var source = createScalarLocus("U", "s", "R", "(s,exp(s))",
				"{false,{-1,1,true,true}}");
		var definition = source.getSemanticDefinition();
		assertFalse(M08LocalAddressEvidence2D.establish(definition, BRANCH, 0,
				definition.getProvider().getDeclaredDomain(), 0, 1, 0.1).isEstablished());
	}

	@Test
	void dependentReferenceUsesOnlyItsCopiedValue() {
		var source = nativeGraph();
		reference();
		GeoPoint q = add("ReferenceDependent=ReferenceR1+(0,0)");
		var selection = M08SnapshotPlacement2D.queryFromReference(source, q,
				LocusPointInteractionPolicy2D.initial(0.1));
		GeoPoint point = selection.commit("P", offered(selection));
		assertFalse(point.getAllPredecessors().contains(q));
		assertFalse(point.getAllPredecessors().contains(lookup("ReferenceR1")));
	}

	@Test
	void referenceAncestorKeepsOnlyItsExistingRouteThroughTheLocus() {
		var source = nativeGraph();
		GeoPoint q = (GeoPoint) lookup("I");
		var selection = M08SnapshotPlacement2D.queryFromReference(source, q,
				LocusPointInteractionPolicy2D.initial(1));
		GeoPoint p = selection.commit("P", offered(selection));
		assertFalse(java.util.Arrays.asList(p.getParentAlgorithm().getInput()).contains(q));
		assertTrue(p.getAllPredecessors().contains(q));
	}

	@Test
	void undefinedAndDetachedReferenceAreRejectedBeforeQuery() {
		var source = nativeGraph();
		GeoPoint q = reference();
		q.setUndefined();
		String before = getApp().getXML();
		assertEquals(Failure.INVALID_REFERENCE, assertThrows(Rejected.class,
				() -> M08SnapshotPlacement2D.queryFromReference(source, q,
						LocusPointInteractionPolicy2D.initial(0.1))).getFailure());
		assertEquals(Failure.INVALID_REFERENCE, assertThrows(Rejected.class,
				() -> M08SnapshotPlacement2D.queryFromReference(source, null,
						LocusPointInteractionPolicy2D.initial(0.1))).getFailure());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void multipleProvenAddressesRequireAnExplicitMember() {
		var source = nativeGraph();
		var selection = M08SnapshotPlacement2D.query(source, 0.24829332226784415,
				1.8854818601338847, LocusPointInteractionPolicy2D.initial(1));
		offered(selection);
		assertEquals(2, selection.getResult().getChoices().size());
		var chosen = selection.getResult().getChoices().get(1);
		var point = selection.commit("P", chosen);
		assertEquals(chosen.getAddress(), ((AlgoSemanticLocusPoint2D) point.getParentAlgorithm())
				.getCurrentSemanticAddress());
	}

	@Test
	void publicationFromAnotherThreadIsRejectedBeforeAnyMutation() throws Exception {
		var source = nativeGraph();
		var selection = select(source);
		var choice = offered(selection);
		var failure = new java.util.concurrent.atomic.AtomicReference<Rejected>();
		String before = getApp().getXML();
		Thread other = new Thread(() -> failure.set(assertThrows(Rejected.class,
				() -> selection.commit("P", choice))));
		other.start();
		other.join();
		assertEquals(Failure.WRONG_THREAD, failure.get().getFailure());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void validityIsSeparateFromImmutableQueryEvidence() {
		var source = nativeGraph();
		var selection = select(source);
		assertEquals(M08SnapshotPlacement2D.Validity.CURRENT, selection.getValidity());
		source.setUndefined();
		assertEquals(M08SnapshotPlacement2D.Validity.STALE, selection.getValidity());
		selection.close();
		assertEquals(M08SnapshotPlacement2D.Validity.CONSUMED, selection.getValidity());
		assertNotNull(selection.getPolicy());
	}
}
