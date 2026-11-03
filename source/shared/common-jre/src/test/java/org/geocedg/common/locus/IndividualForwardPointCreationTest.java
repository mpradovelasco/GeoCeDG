/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.interaction.IndividualForwardPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.IndividualForwardPointCreation2D.Failure;
import org.geocedg.common.kernel.locus.interaction.IndividualForwardPointCreation2D.Rejected;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D;
import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleRuntime;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Independent permission, lifecycle and persistence oracles for forward-only creation. */
class IndividualForwardPointCreationTest extends G9U0PublicSurfaceTestBase {

	private GeoLocusV2 vectorCircle() {
		add("Center=(0,0)");
		add("c=Circle(Center,1)");
		add("N=Point(c)");
		add("v=Vector((0,0),N)");
		add("Q=N+(2*v)");
		return add("L=LocusV2(Q,N)");
	}
	private LocusPointInteractionPolicy2D policy() {
		return LocusPointInteractionPolicy2D.initial(.1);
	}

	@Test
	void v3CircleWithoutH1CreatesOnlyByIndependentExplicitForwardPermission() {
		var source = vectorCircle();
		try (var h1 = M08SnapshotPlacement2D.query(source, 3, 0, policy())) {
			assertEquals(M08SnapshotPlacement2D.Permission.NOT_OFFERABLE,
					h1.getResult().getPermission());
		}
		String before = getApp().getXML();
		try (var selection = IndividualForwardPointCreation2D.query(source, 3, .01, policy())) {
			assertEquals(IndividualForwardPointCreation2D.Permission.EXPLICIT_FORWARD_SELECTION_REQUIRED,
					selection.getPermission(), selection.getFailure().name());
			assertEquals(before, getApp().getXML());
			var choice = selection.getChoices().get(0);
			assertEquals(IndividualForwardPointCreation2D.Proximity.FLOATING_POINT_UNCERTIFIED,
					choice.getProximity());
			assertTrue(choice.getComputedWorldResidual() <= .1);
			long discovery = selection.getSemanticMisses();
			GeoPoint point = selection.commit("P", choice);
			assertTrue(point.isDefined());
			assertEquals(3, Math.hypot(point.getInhomX(), point.getInhomY()), 1E-10);
			assertTrue(selection.getSemanticMisses() > discovery);
			assertEquals(1, selection.getPublicationAttempts());
			assertThrows(Rejected.class, () -> selection.commit("Again", choice));
		}
	}

	@Test
	void facadeClickAndReferenceChooseIdenticalAddressesWithoutKeepingReferenceParent() {
		var source = vectorCircle();
		GeoPoint reference = add("Reference=(3,.01)");
		try (var click = LocusPointCreation2D.query(source, 3, .01, policy());
				var fromQ = LocusPointCreation2D.queryFromReference(source, reference, policy())) {
			assertEquals(LocusPointCreation2D.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION,
					click.getRoute(), click.getFailure());
			assertEquals(click.getRoute(), fromQ.getRoute());
			var chosen = fromQ.getChoices().get(0);
			assertEquals(click.getChoices().get(0).getAddress(), chosen.getAddress());
			GeoPoint p = fromQ.commit("P", chosen);
			assertFalse(p.getAllPredecessors().contains(reference));
			var address = ((AlgoSemanticLocusPoint2D) p.getParentAlgorithm()).getCurrentSemanticAddress();
			GeoPoint dependent = add("Dependent=P+(1,0)");
			reference.setCoords(50, 50, 1);
			reference.updateCascade();
			assertEquals(address,
					((AlgoSemanticLocusPoint2D) p.getParentAlgorithm()).getCurrentSemanticAddress());
			assertEquals(p.getInhomX() + 1, dependent.getInhomX(), 1E-12);
			var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(p);
			getApp().setXML(getApp().getXML(), true);
			GeoPoint loaded = (GeoPoint) lookup("P");
			assertEquals(id, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(loaded));
			assertEquals(address,
					((AlgoSemanticLocusPoint2D) loaded.getParentAlgorithm()).getCurrentSemanticAddress());
		}
	}

	@Test
	void staleReferenceAndForeignCapabilityCannotPublishAndFailuresConsumeOnce() {
		var source = vectorCircle();
		GeoPoint reference = add("Reference=(3,0)");
		try (var first = IndividualForwardPointCreation2D.queryFromReference(source, reference, policy());
				var other = IndividualForwardPointCreation2D.query(source, 3, 0, policy())) {
			var own = first.getChoices().get(0);
			String before = getApp().getXML();
			assertEquals(Failure.FOREIGN_CHOICE, assertThrows(Rejected.class,
					() -> other.commit("Foreign", own)).getFailure());
			assertEquals(before, getApp().getXML());
			reference.setCoords(20, 30, 1);
			reference.updateCascade();
			String moved = getApp().getXML();
			assertEquals(Failure.STALE_SELECTION, assertThrows(Rejected.class,
					() -> first.commit("Stale", own)).getFailure());
			assertEquals(moved, getApp().getXML());
			assertEquals(Failure.CONSUMED_SELECTION, assertThrows(Rejected.class,
					() -> first.commit("Replay", own)).getFailure());
		}
	}

	@Test
	void cancelledAndWrongThreadHandlesLeaveConstructionExactlyUnchanged() throws Exception {
		var source = vectorCircle();
		try (var selection = IndividualForwardPointCreation2D.query(source, 3, 0, policy())) {
			var choice = selection.getChoices().get(0);
			String before = getApp().getXML();
			var failure = new java.util.concurrent.atomic.AtomicReference<Throwable>();
			var thread = new Thread(() -> {
				try { selection.commit("WrongThread", choice); }
				catch (Throwable rejected) { failure.set(rejected); }
			});
			thread.start();
			thread.join();
			assertEquals(Failure.WRONG_THREAD, ((Rejected) failure.get()).getFailure());
			assertNull(selection.commit("Cancelled", null));
			assertEquals(before, getApp().getXML());
		}
	}

	@Test
	void arbitraryFormulaWithoutCertifiedComponentsAndInsufficientWorkStayClosed() {
		var circle = vectorCircle();
		var tiny = new LocusPointInteractionPolicy2D(.1, 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D(1, 2, 1, 1));
		String before = getApp().getXML();
		try (var selection = IndividualForwardPointCreation2D.query(circle, 3, 0, tiny)) {
			assertEquals(Failure.BUDGET_EXHAUSTED, selection.getFailure());
			assertTrue(selection.getChoices().isEmpty());
			assertTrue(selection.getSemanticMisses() <= 1);
		}
		assertEquals(before, getApp().getXML());
		var arbitrary = createScalarLocus("Unsupported", "s", "R", "(s,exp(s))",
				"{false,{-1,1,true,true}}");
		try (var selection = IndividualForwardPointCreation2D.query(arbitrary, 0, 1, policy())) {
			assertEquals(IndividualForwardPointCreation2D.Permission.NOT_OFFERABLE,
					selection.getPermission());
			assertTrue(selection.getChoices().isEmpty());
		}
	}

	@Test
	void sourceRevisionABAInvalidatesOfferAndOutOfRadiusHasNoOffer() {
		var source = vectorCircle();
		try (var selection = IndividualForwardPointCreation2D.query(source, 3, 0, policy())) {
			var choice = selection.getChoices().get(0);
			GeoPoint centre = (GeoPoint) lookup("Center");
			centre.setCoords(0, 1, 1);
			centre.updateCascade();
			String before = getApp().getXML();
			assertEquals(Failure.STALE_SELECTION, assertThrows(Rejected.class,
					() -> selection.commit("Stale", choice)).getFailure());
			assertEquals(before, getApp().getXML());
		}
		try (var selection = IndividualForwardPointCreation2D.query(source, 30, 0, policy())) {
			assertEquals(IndividualForwardPointCreation2D.Permission.NOT_OFFERABLE,
					selection.getPermission());
			assertTrue(selection.getChoices().isEmpty());
		}
	}

	@Test
	void confirmationCannotRestartAnExhaustedQueryBudget() {
		var source = vectorCircle();
		long queryWork;
		try (var baseline = IndividualForwardPointCreation2D.query(source, 3, 0, policy())) {
			assertFalse(baseline.getChoices().isEmpty());
			queryWork = baseline.getSemanticMisses();
		}
		var exactQueryBudget = new LocusPointInteractionPolicy2D(.1, 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D((int) queryWork, 512, 80, 1024));
		try (var selection = IndividualForwardPointCreation2D.query(source, 3, 0, exactQueryBudget)) {
			assertFalse(selection.getChoices().isEmpty(), selection.getFailure().name());
			var choice = selection.getChoices().get(0);
			String before = getApp().getXML();
			assertEquals(Failure.BUDGET_EXHAUSTED,
					assertThrows(Rejected.class, () -> selection.commit("P", choice)).getFailure());
			assertEquals(queryWork, selection.getSemanticMisses());
			assertEquals(0, selection.getPublicationAttempts());
			assertEquals(before, getApp().getXML());
		}
	}

	@Test
	void initialConstructiveAuthorityCannotBorrowACapturedForwardCertificate() {
		var source = vectorCircle();
		getConstruction().getSpatialIdentityRegistry()
				.adoptDocumentAuthority(DocumentAuthorityRecord.CONSTRUCTIVE_V1);
		String before = getApp().getXML();
		try (var selection = IndividualForwardPointCreation2D.query(source, 3, 0, policy())) {
			assertEquals(Failure.AUTHORITY_NOT_ESTABLISHED, selection.getFailure());
			assertEquals(IndividualForwardPointCreation2D.Permission.NOT_OFFERABLE,
					selection.getPermission());
			assertTrue(selection.getChoices().isEmpty());
			assertEquals(0, selection.getSemanticMisses());
		}
		assertEquals(before, getApp().getXML());
	}

	@Test
	void authorityAdoptedAfterAnOfferInvalidatesItsPublication() {
		var source = vectorCircle();
		try (var selection = IndividualForwardPointCreation2D.query(source, 3, 0, policy())) {
			var choice = selection.getChoices().get(0);
			getConstruction().getSpatialIdentityRegistry()
					.adoptDocumentAuthority(DocumentAuthorityRecord.CONSTRUCTIVE_V1);
			String before = getApp().getXML();
			assertEquals(M08SnapshotPlacement2D.Validity.STALE, selection.getValidity());
			assertEquals(Failure.STALE_SELECTION,
					assertThrows(Rejected.class, () -> selection.commit("P", choice)).getFailure());
			assertEquals(0, selection.getPublicationAttempts());
			assertEquals(before, getApp().getXML());
		}
	}

	@Test
	void realCollapsedV3ImageCannotOfferAnyOfItsCoincidentAddresses() {
		vectorCircle();
		add("Collapsed=N+(-1*v)");
		GeoLocusV2 collapsed = add("CollapsedL=LocusV2(Collapsed,N)");
		var evaluator = assertInstanceOf(ReconstructibleLocusEvaluator2D.class,
				collapsed.getSemanticDefinition().getEvaluatorCapability());
		assertTrue(evaluator.captureCertifiedProgramV3().isPresent());
		String before = getApp().getXML();
		try (var selection = IndividualForwardPointCreation2D.query(collapsed, 0, 0, policy())) {
			assertEquals(IndividualForwardPointCreation2D.Permission.NOT_OFFERABLE,
					selection.getPermission());
			assertTrue(selection.getChoices().isEmpty());
			assertEquals(0, selection.getPublicationAttempts());
		}
		assertEquals(before, getApp().getXML());
	}

	@Test
	void undefinedSourceRejectsOldAndNewOffersThenRecoversWithoutReusingTheHandle() {
		var source = vectorCircle();
		var sourceId = source.getPersistentLocusId();
		try (var old = IndividualForwardPointCreation2D.query(source, 3, 0, policy())) {
			var choice = old.getChoices().get(0);
			GeoPoint centre = (GeoPoint) lookup("Center");
			centre.setUndefined();
			centre.updateCascade();
			assertFalse(source.isDefined());
			String undefined = getApp().getXML();
			try (var rejected = IndividualForwardPointCreation2D.query(source, 3, 0, policy())) {
				assertEquals(Failure.INVALID_SOURCE, rejected.getFailure());
				assertTrue(rejected.getChoices().isEmpty());
			}
			assertEquals(Failure.STALE_SELECTION,
					assertThrows(Rejected.class, () -> old.commit("Stale", choice)).getFailure());
			assertEquals(undefined, getApp().getXML());
			centre.setCoords(0, 0, 1);
			centre.updateCascade();
			assertTrue(source.isDefined());
			assertEquals(sourceId, source.getPersistentLocusId());
			try (var recovered = IndividualForwardPointCreation2D.query(source, 3, 0, policy())) {
				assertTrue(recovered.commit("Recovered", recovered.getChoices().get(0)).isDefined());
			}
			assertEquals(Failure.CONSUMED_SELECTION,
					assertThrows(Rejected.class, () -> old.commit("Replay", choice)).getFailure());
		}
	}

	@Test
	void registrationRuntimeFailureRollsBackIdentitiesReservationsAndHiddenInputs() throws Exception {
		var source = vectorCircle();
		SpatialIdentityRegistry registry = getConstruction().getSpatialIdentityRegistry();
		var records = registry.getRecords();
		int reserved = registry.getReservedIdentityCount();
		int size = getConstruction().getGeoSetConstructionOrder().size();
		String before = getApp().getXML();
		AtomicBoolean commitAttempted = new AtomicBoolean();
		AtomicBoolean rollbackAttempted = new AtomicBoolean();
		// Inject inside the real registration transaction, preserving its reservation cleanup.
		Field runtimeField = SpatialIdentityRegistry.class.getDeclaredField("lifecycleRuntime");
		runtimeField.setAccessible(true);
		Object originalRuntime = runtimeField.get(registry);
		try (var selection = IndividualForwardPointCreation2D.query(source, 3, 0, policy())) {
			var choice = selection.getChoices().get(0);
			runtimeField.set(registry, (SpatialLifecycleRuntime) graph ->
					new SpatialLifecycleRuntime.PreparedSwitch() {
						@Override public void commit() {
							commitAttempted.set(true);
							throw new IllegalStateException("injected forward registration failure");
						}
						@Override public void rollback() { rollbackAttempted.set(true); }
					});
			try {
				var failure = assertThrows(SpatialIdentityException.class,
						() -> selection.commit("FailedPublication", choice));
				assertEquals(SpatialIdentityDiagnostic.Code.LIFECYCLE_RUNTIME_FAILURE,
						failure.getDiagnostic().getCode());
				assertEquals("injected forward registration failure", failure.getCause().getMessage());
			} finally {
				runtimeField.set(registry, originalRuntime);
			}
			assertTrue(commitAttempted.get());
			assertTrue(rollbackAttempted.get());
			assertEquals(1, selection.getPublicationAttempts());
			assertEquals(Failure.CONSUMED_SELECTION,
					assertThrows(Rejected.class, () -> selection.commit("Replay", choice)).getFailure());
		}
		assertEquals(before, getApp().getXML());
		assertEquals(records, registry.getRecords());
		assertEquals(reserved, registry.getReservedIdentityCount());
		assertEquals(size, getConstruction().getGeoSetConstructionOrder().size());
		assertNull(lookup("FailedPublication"));
		try (var retry = IndividualForwardPointCreation2D.query(source, 3, 0, policy())) {
			assertTrue(retry.commit("Recovered", retry.getChoices().get(0)).isDefined());
		}
	}

	@Test
	void facadeConfirmationCannotRestartWorkSpentByHistoricalAndH1Attempts() {
		var source = vectorCircle();
		long queryWork;
		try (var baseline = LocusPointCreation2D.query(source, 3, 0, policy())) {
			assertEquals(LocusPointCreation2D.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION,
					baseline.getRoute(), baseline.getFailure());
			queryWork = baseline.getSemanticMisses();
			assertTrue(queryWork > baseline.getForwardSelection().getSemanticMisses());
		}
		var queryOnlyPolicy = new LocusPointInteractionPolicy2D(.1, 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D((int) queryWork, 512, 80, 1024));
		try (var selection = LocusPointCreation2D.query(source, 3, 0, queryOnlyPolicy)) {
			assertEquals(LocusPointCreation2D.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION,
					selection.getRoute(), selection.getFailure());
			assertEquals(queryWork, selection.getSemanticMisses());
			var choice = selection.getChoices().get(0);
			String before = getApp().getXML();
			assertEquals(Failure.BUDGET_EXHAUSTED,
					assertThrows(Rejected.class, () -> selection.commit("P", choice)).getFailure());
			assertTrue(selection.getSemanticMisses() <= queryWork);
			assertEquals(0, selection.getForwardSelection().getPublicationAttempts());
			assertEquals(before, getApp().getXML());
		}
	}
}
