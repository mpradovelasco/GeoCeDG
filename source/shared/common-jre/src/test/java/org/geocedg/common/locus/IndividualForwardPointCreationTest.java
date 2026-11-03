/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.interaction.IndividualForwardPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.IndividualForwardPointCreation2D.Failure;
import org.geocedg.common.kernel.locus.interaction.IndividualForwardPointCreation2D.Rejected;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.locus.interaction.LocusSnapshotPlacement2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D;
import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleRuntime;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.algos.AlgoDependentNumber;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.View;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

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
		var sourceId = source.getPersistentLocusId();
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
		SpatialLifecycleRuntime originalRuntime =
				assertInstanceOf(SpatialLifecycleRuntime.class, runtimeField.get(registry));
		try (var selection = IndividualForwardPointCreation2D.query(source, 3, 0, policy())) {
			var choice = selection.getChoices().get(0);
			runtimeField.set(registry, (SpatialLifecycleRuntime) graph -> {
				// Inject one publication failure; mandatory host restoration uses the real runtime.
				if (commitAttempted.get()) { return originalRuntime.prepare(graph); }
				return new SpatialLifecycleRuntime.PreparedSwitch() {
						@Override public void commit() {
							commitAttempted.set(true);
							throw new IllegalStateException("injected forward registration failure");
						}
						@Override public void rollback() { rollbackAttempted.set(true); }
					};
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
		var restoredSource = assertInstanceOf(GeoLocusV2.class, getConstruction()
				.getSpatialIdentityRegistry().getGeo(sourceId));
		try (var retry = IndividualForwardPointCreation2D.query(restoredSource, 3, 0, policy())) {
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

	private GeoLocusV2 publicationSpline() {
		add("A=(-2,0)"); add("B=(-2/3,0)"); add("C=(2/3,0)"); add("D=(2,0)");
		return add("S=SplineV2({A,B,C,D},3)");
	}

	private void assertUnsupportedHostRefusal(GeoLocusV2 source) {
		assertTrue(org.geocedg.common.kernel.locus.LocusV2PublicOperations
				.hasUnsupportedInteractiveHostState(getConstruction()));
		try (var selection = IndividualForwardPointCreation2D.query(source, 3, 0, policy())) {
			assertFalse(selection.getChoices().isEmpty(), selection.getFailure().name());
			PublicationState before = publicationState();
			assertEquals(Failure.UNSUPPORTED_HOST_STATE, assertThrows(Rejected.class,
					() -> selection.commit("Refused", selection.getChoices().get(0))).getFailure());
			assertEquals(0, selection.getPublicationAttempts());
			assertPublicationRestored(before);
			assertNull(lookup("Refused"));
		}
	}

	@Test
	void newForwardPermissionRefusesAnUnrelatedFreeLinePlaceBeforeMutation() {
		GeoLocusV2 source = vectorCircle();
		GeoPoint other = add("Other=Point(x+y=1)");
		other.setCoords(.3, .2, 1); other.updateCascade();
		long argument = Double.doubleToLongBits(other.getPathParameter().t);
		assertUnsupportedHostRefusal(source);
		assertSame(other, lookup("Other"));
		assertEquals(argument, Double.doubleToLongBits(other.getPathParameter().t));
		other.remove();
		assertFalse(org.geocedg.common.kernel.locus.LocusV2PublicOperations
				.hasUnsupportedInteractiveHostState(getConstruction()));
		try (var selection = IndividualForwardPointCreation2D.query(source, 3, 0, policy())) {
			assertTrue(selection.commit("Accepted", selection.getChoices().get(0)).isDefined());
		}
	}

	@Test
	void newForwardPermissionRefusesRegionPlacesAndUndefinedConicCaches() {
		GeoLocusV2 source = vectorCircle();
		GeoPoint region = add("RegionPoint=PointIn(Circle((5,5),1))");
		assertUnsupportedHostRefusal(source);
		region.remove();
		assertFalse(org.geocedg.common.kernel.locus.LocusV2PublicOperations
				.hasUnsupportedInteractiveHostState(getConstruction()));
		GeoPoint badCenter = add("BadCenter=(4,5)");
		add("badCircle=Circle(BadCenter,1)");
		badCenter.setUndefined(); badCenter.updateCascade();
		assertFalse(lookup("badCircle").isDefined());
		assertUnsupportedHostRefusal(source);
	}

	@Test
	void newForwardPermissionRefusesLossyIndependentInputsAndRawAngleLiterals() {
		GeoLocusV2 source = vectorCircle();
		GeoNumeric negativeZero = add("z=1");
		negativeZero.setValue(-0.0); negativeZero.updateCascade();
		assertUnsupportedHostRefusal(source);
		negativeZero.remove();
		GeoPoint signedPoint = add("Signed=(1,2)");
		signedPoint.setCoords(-0.0, 2, 1);
		assertUnsupportedHostRefusal(source);
		signedPoint.remove();
		MyDouble literal = new MyDouble(getKernel(), .4097);
		literal.setAngle();
		new AlgoDependentNumber(getConstruction(), new ExpressionNode(getKernel(), literal), true)
				.getNumber().toGeoElement().setLabel("RawAngle");
		assertUnsupportedHostRefusal(source);
	}

	private GeoLocusV2 publicationH1() {
		add("I=(0.4965866445356883,1.7854818601338847)");
		add("J=(-0.5082406249102692,2.5763181370126467)");
		add("f=Circle(I,J)"); add("D=Point(f)"); add("g=Line(D,xAxis)");
		add("K=Intersect(g,yAxis)"); add("M=Midpoint(K,D)");
		return add("h=LocusV2(M,D)");
	}

	private record PublicationState(String xml, Set<SpatialIdentityId> ids, int reservations,
			int geos, long hiddenInputs, int undoHistory, Set<PersistentGeoId> locusIds) { }

	private PublicationState publicationState() {
		getKernel().setUndoActive(true);
		getKernel().initUndoInfo();
		var registry = getConstruction().getSpatialIdentityRegistry();
		return new PublicationState(getApp().getXML(), registry.getRecords().stream()
				.map(record -> record.getId()).collect(Collectors.toSet()),
				registry.getReservedIdentityCount(), getConstruction().getGeoSetConstructionOrder().size(),
				hiddenInputCount(), getConstruction().getUndoManager().getHistorySize(),
				getConstruction().getGeoSetConstructionOrder().stream().filter(GeoLocusV2.class::isInstance)
						.map(GeoLocusV2.class::cast).map(GeoLocusV2::getPersistentLocusId)
						.collect(Collectors.toSet()));
	}

	private long hiddenInputCount() {
		return getConstruction().getGeoSetConstructionOrder().stream()
				.filter(geo -> geo.isAuxiliaryObject() && !geo.isEuclidianVisible()).count();
	}

	private void assertPublicationRestored(PublicationState before) {
		var registry = getConstruction().getSpatialIdentityRegistry();
		assertEquals(before.xml(), getApp().getXML());
		assertEquals(before.ids(), registry.getRecords().stream()
				.map(record -> record.getId()).collect(Collectors.toSet()));
		assertEquals(before.reservations(), registry.getReservedIdentityCount());
		assertEquals(before.geos(), getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(before.hiddenInputs(), hiddenInputCount());
		assertEquals(before.undoHistory(), getConstruction().getUndoManager().getHistorySize());
		for (PersistentGeoId id : before.locusIds()) {
			assertTrue(assertInstanceOf(GeoLocusV2.class, registry.getGeo(id)).isDefined(),
					"The same durable source must regain its valid runtime state after rollback");
		}
		assertNull(lookup("P"));
	}

	/** A real synchronous kernel notification, after inputs exist and before publication closes. */
	private <T extends RuntimeException> T rejectDuringAdd(Predicate<GeoElement> target,
			Consumer<GeoElement> mutation, Class<T> failureType, Executable publication) {
		AtomicBoolean entered = new AtomicBoolean();
		View view = mock(View.class);
		doAnswer(invocation -> {
			GeoElement added = invocation.getArgument(0);
			if (target.test(added) && entered.compareAndSet(false, true)) { mutation.accept(added); }
			return null;
		}).when(view).add(any(GeoElement.class));
		getKernel().attach(view);
		try { return assertThrows(failureType, publication); }
		finally {
			getKernel().detach(view);
			assertTrue(entered.get(), "The adversarial synchronous publication callback must execute");
		}
	}

	private static boolean isPublishedPoint(GeoElement geo) { return "P".equals(geo.getLabelSimple()); }

	private void assertR1SourceMutationRejected(boolean changeRevision) {
		var source = publicationSpline();
		var definition = source.getSemanticDefinition();
		AtomicBoolean invalidated = new AtomicBoolean();
		try (var selection = LocusSnapshotPlacement2D.query(source, .4, .01, policy())) {
			var choice = selection.getResult().getUniqueCandidate(); assertNotNull(choice);
			var before = publicationState();
			var rejected = rejectDuringAdd(IndividualForwardPointCreationTest::isPublishedPoint, geo -> {
				if (changeRevision) {
					GeoPoint control = (GeoPoint) lookup("A");
					control.setCoords(-2, 1, 1); control.updateCascade();
					invalidated.set(source.getSemanticDefinition() != definition);
				} else { source.setUndefined(); invalidated.set(!source.isDefined()); }
			}, IllegalArgumentException.class, () -> selection.commit("P", choice));
			assertEquals("CU7_STALE_SELECTION", rejected.getMessage());
			assertTrue(invalidated.get()); assertPublicationRestored(before);
			assertEquals("CU7_SELECTION_CONSUMED", assertThrows(IllegalStateException.class,
					() -> selection.commit("P", choice)).getMessage());
		}
	}

	@Test void directR1RejectsSourceUndefinedDuringOutputAddWithExactRollback() {
		assertR1SourceMutationRejected(false);
	}

	@Test void directR1RejectsSourceRevisionChangeDuringOutputAddWithExactRollback() {
		assertR1SourceMutationRejected(true);
	}

	private void assertH1SourceMutationRejected(boolean changeRevision) {
		var source = publicationH1();
		var definition = source.getSemanticDefinition();
		AtomicBoolean invalidated = new AtomicBoolean();
		try (var selection = M08SnapshotPlacement2D.query(source, .8976484384297626,
				1.7854818601338847, policy())) {
			assertFalse(selection.getResult().getChoices().isEmpty());
			var choice = selection.getResult().getChoices().get(0);
			var before = publicationState();
			var rejected = rejectDuringAdd(IndividualForwardPointCreationTest::isPublishedPoint, geo -> {
				if (changeRevision) {
					GeoPoint center = (GeoPoint) lookup("I");
					center.setCoords(center.getInhomX() + .1, center.getInhomY(), 1); center.updateCascade();
					invalidated.set(source.getSemanticDefinition() != definition);
				} else { source.setUndefined(); invalidated.set(!source.isDefined()); }
			}, M08SnapshotPlacement2D.Rejected.class, () -> selection.commit("P", choice));
			assertEquals(M08SnapshotPlacement2D.Failure.STALE_SELECTION, rejected.getFailure());
			assertTrue(invalidated.get()); assertPublicationRestored(before);
			assertEquals(M08SnapshotPlacement2D.Failure.CONSUMED_SELECTION,
					assertThrows(M08SnapshotPlacement2D.Rejected.class,
							() -> selection.commit("P", choice)).getFailure());
		}
	}

	@Test void directH1RejectsSourceUndefinedDuringOutputAddWithExactRollback() {
		assertH1SourceMutationRejected(false);
	}

	@Test void directH1RejectsSourceRevisionChangeDuringOutputAddWithExactRollback() {
		assertH1SourceMutationRejected(true);
	}

	@Test void facadeR1RechecksReferenceInsideFactoryAndConsumesRejectedCapability() {
		var source = publicationSpline();
		GeoPoint reference = add("Reference=(.4,.01)");
		try (var selection = LocusPointCreation2D.queryFromReference(source, reference, policy())) {
			assertEquals(LocusPointCreation2D.Route.R1_R6, selection.getRoute());
			var choice = selection.getChoices().get(0);
			var before = publicationState();
			var rejected = rejectDuringAdd(IndividualForwardPointCreationTest::isPublishedPoint, geo -> {
				reference.setCoords(20, 30, 1); reference.updateCascade();
			}, LocusPointCreation2D.Rejected.class, () -> selection.commit("P", choice));
			assertEquals(LocusPointCreation2D.Failure.STALE_SELECTION, rejected.getFailure());
			assertPublicationRestored(before);
			assertEquals(LocusPointCreation2D.Failure.CONSUMED_SELECTION,
					assertThrows(LocusPointCreation2D.Rejected.class,
							() -> selection.commit("P", choice)).getFailure());
		}
	}

	private void assertIndependentMutationRejected(GeoLocusV2 source, GeoPoint reference,
			Consumer<GeoElement> mutation, Failure expected) {
		try (var selection = reference == null
				? IndividualForwardPointCreation2D.query(source, 3, 0, policy())
				: IndividualForwardPointCreation2D.queryFromReference(source, reference, policy())) {
			assertFalse(selection.getChoices().isEmpty(), selection.getFailure().name());
			var choice = selection.getChoices().get(0);
			var before = publicationState();
			var rejected = rejectDuringAdd(IndividualForwardPointCreationTest::isPublishedPoint,
					mutation, Rejected.class, () -> selection.commit("P", choice));
			assertEquals(expected, rejected.getFailure()); assertPublicationRestored(before);
			assertEquals(Failure.CONSUMED_SELECTION,
					assertThrows(Rejected.class, () -> selection.commit("P", choice)).getFailure());
		}
	}

	@Test void independentCreationRejectsSourceUndefinedInsideOutputPublication() {
		var source = vectorCircle();
		assertIndependentMutationRejected(source, null, geo -> source.setUndefined(), Failure.STALE_SELECTION);
	}

	@Test void independentCreationRejectsSourceRevisionChangeInsideOutputPublication() {
		var source = vectorCircle();
		assertIndependentMutationRejected(source, null, geo -> {
			GeoPoint center = (GeoPoint) lookup("Center");
			center.setCoords(1, 0, 1); center.updateCascade();
		}, Failure.STALE_SELECTION);
	}

	@Test void independentCreationRechecksReferenceInsideOutputPublication() {
		var source = vectorCircle();
		GeoPoint reference = add("Reference=(3,0)");
		assertIndependentMutationRejected(source, reference, geo -> {
			reference.setCoords(20, 30, 1); reference.updateCascade();
		}, Failure.STALE_SELECTION);
	}

	@Test void independentCreationRejectsOutputCoordinatesOutsideItsSelectedForwardBinding() {
		// Still inside the discovery radius: proximity cannot replace the selected forward value.
		assertIndependentMutationRejected(vectorCircle(), null,
				geo -> ((GeoPoint) geo).setCoords(3, .01, 1), Failure.INVALID_FORWARD);
	}

	@Test void independentCreationRejectsHiddenParameterTamperingWithoutACascade() {
		assertIndependentMutationRejected(vectorCircle(), null, geo -> {
			var parent = (AlgoSemanticLocusPoint2D) geo.getParentAlgorithm();
			((GeoNumeric) parent.getParameterInput().toGeoElement()).setValue(.5);
		}, Failure.INVALID_FORWARD);
	}

	private void assertCallbackExceptionRollsBack(Predicate<GeoElement> phase) {
		var source = vectorCircle();
		GeoPoint reference = add("Reference=(3,0)");
		try (var selection = IndividualForwardPointCreation2D.queryFromReference(source, reference, policy())) {
			var choice = selection.getChoices().get(0);
			var before = publicationState();
			RuntimeException injected = new IllegalStateException("synchronous creation publication callback");
			var rejected = rejectDuringAdd(phase, geo -> {
				reference.setCoords(20, 30, 1); reference.updateCascade();
				throw injected;
			}, RuntimeException.class, () -> selection.commit("P", choice));
			assertSame(injected, rejected); assertPublicationRestored(before);
			assertEquals(Failure.CONSUMED_SELECTION,
					assertThrows(Rejected.class, () -> selection.commit("P", choice)).getFailure());
		}
	}

	@Test void outputAddRuntimeExceptionRestoresReferenceRegistryInputsAndUndo() {
		assertCallbackExceptionRollsBack(IndividualForwardPointCreationTest::isPublishedPoint);
	}

	@Test void auxiliaryAddRuntimeExceptionRestoresReferenceBeforeThePointFactoryCompletes() {
		assertCallbackExceptionRollsBack(geo -> geo instanceof GeoText);
	}
}
