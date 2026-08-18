/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry.LoadPurpose;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry.LoadSession;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry.RedefinePublicationLease;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry.RedefineRebuildToken;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineCandidateOutput;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineContext;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineDecision;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineEffect;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineOutputGroup;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineProposal;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineSignature;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineTransaction;
import org.geocedg.common.kernel.spatial.runtime.ProjectionSystemPilotCertificate;
import org.geocedg.common.kernel.spatial.runtime.SpatialPointPilotCertificate;
import org.geocedg.common.kernel.spatial.runtime.SpatialSemanticRuntime;
import org.geocedg.common.kernel.spatial.semantic.Vector3;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.geogebra3D.kernel3D.geos.GeoPoint3D;
import org.geogebra.common.gui.view.algebra.EvalInfoFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

class G9A3SpatialRedefineTransactionTest
		extends G9A3SpatialRedefineTestSupport {

	@Test
	void redef01ExplicitSemanticNoOpPreservesIdentityAndBothRevisions() {
		G9A2SpatialSemanticRuntimeTest.Graph graph =
				G9A2SpatialSemanticRuntimeTest.Graph.createProductiveWithHinge(
						getConstruction(), this::add);
		GeoElement target = graph.supportEnd;
		PersistentGeoId id = registry().getPersistentGeoId(target);
		GeoIdentityRecord before = registry().getGeoRecord(id);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		final ProjectionSystemPilotCertificate unchangedSystem =
				runtime.getProjectionSystemCertificate(graph.systemId);
		final SpatialPointPilotCertificate unchangedPoint =
				runtime.getSpatialPointCertificate(graph.objectId);
		final long unchangedSystemPublications = runtime.getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.systemId, 0L);
		final long unchangedPointPublications = runtime.getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.objectId, 0L);
		SpatialRedefineContext context = registry().captureRedefineContext(target);

		SpatialRedefineTransaction transaction = registry().prepareRedefine(
				context, target, List.of(target), false);

		assertEquals(SpatialRedefineDecision.RETAIN, transaction.getDecision());
		assertEquals(SpatialRedefineEffect.NO_OP,
				transaction.getProposal().getEffect());
		transaction.commit(target);
		getConstruction().completeSpatialRedefineOperation(context);
		GeoIdentityRecord after = registry().getGeoRecord(id);
		assertSame(target, registry().getGeo(id));
		assertEquals(before.getDefinitionRevision(), after.getDefinitionRevision());
		assertEquals(before.getTopologyRevision(), after.getTopologyRevision());
		assertSame(unchangedSystem,
				runtime.getProjectionSystemCertificate(graph.systemId));
		assertSame(unchangedPoint,
				runtime.getSpatialPointCertificate(graph.objectId));
		assertEquals(unchangedSystemPublications, runtime.getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.systemId, 0L));
		assertEquals(unchangedPointPublications, runtime.getInstrumentation()
				.snapshotAuthoritativePublicationCounts()
				.getOrDefault(graph.objectId, 0L));

		SpatialRedefineContext recomputeContext = registry()
				.captureRedefineContext(target);
		SpatialRedefineTransaction recompute = registry().prepareRedefine(
				recomputeContext, target, List.of(target), false);
		assertEquals(SpatialRedefineEffect.NO_OP,
				recompute.getProposal().getEffect());
		String previousSystemToken = unchangedSystem.getValueSnapshotToken();
		String previousPointToken = unchangedPoint.getValueSnapshotToken();
		try (RedefinePublicationLease ignored = getConstruction()
				.beginSpatialRedefinePublicationLease(recompute)) {
			graph.setSupportEnd(1, 1, 0);
			recompute.commit(target);
		}
		getConstruction().completeSpatialRedefineOperation(recomputeContext);

		GeoIdentityRecord recomputed = registry().getGeoRecord(id);
		assertEquals(before.getDefinitionRevision(),
				recomputed.getDefinitionRevision());
		assertEquals(before.getTopologyRevision(), recomputed.getTopologyRevision());
		assertNotEquals(previousSystemToken,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getValueSnapshotToken());
		assertNotEquals(previousPointToken,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getValueSnapshotToken());
		assertEquals(unchangedSystemPublications + 1,
				runtime.getInstrumentation().snapshotAuthoritativePublicationCounts()
						.getOrDefault(graph.systemId, 0L));
		assertEquals(unchangedPointPublications + 1,
				runtime.getInstrumentation().snapshotAuthoritativePublicationCounts()
						.getOrDefault(graph.objectId, 0L));
	}

	@Test
	void redef02SameObjectAndNullHostNoOpCannotFabricateLifecycleChange()
			throws Exception {
		G9A2SpatialSemanticRuntimeTest.Graph graph =
				G9A2SpatialSemanticRuntimeTest.Graph.createProductiveWithHinge(
						getConstruction(), this::add);
		GeoElement target = graph.supportEnd;
		PersistentGeoId id = registry().getPersistentGeoId(target);
		GeoIdentityRecord before = registry().getGeoRecord(id);
		SpatialRedefineContext sameContext = registry().captureRedefineContext(target);

		getConstruction().replace(target, target,
				new EvalInfo(true).withSpatialRedefineContext(sameContext));

		assertEquals(before, registry().getGeoRecord(id));
		String sameXml = getApp().getXML();
		SpatialRedefineContext nullContext = registry().captureRedefineContext(target);
		assertThrows(RuntimeException.class,
				() -> getConstruction().replace(target, null,
						new EvalInfo(true).withSpatialRedefineContext(nullContext)));
		assertEquals(sameXml, getApp().getXML());
		assertEquals(before, registry().getGeoRecord(id));
	}

	@Test
	void redef18CompatibleHostRoutesHaveTheSameLifecycleResult() {
		registry().registerRedefineProvider(new CompatibleProvider(
				PROVIDER, signature(PROVIDER, "GENERIC", "VALUE", 1),
				SpatialRedefineDecision.RETAIN));
		GeoNumeric inPlace = add("R18A=1");
		GeoIdentityRecord inPlaceRecord = register(inPlace,
				signature(PROVIDER, "GENERIC", "VALUE", 1));
		edit(inPlace, "R18A=2");

		add("R18P=(0,0)");
		GeoElement soft = add("R18l=Line(R18P,(1,0))");
		GeoIdentityRecord softRecord = register(soft,
				signature(PROVIDER, "GENERIC", "VALUE", 1));
		edit(soft, "R18l=Line(R18P,(0,1))");

		add("R18Q=(1,0)");
		add("R18R=(0,1)");
		GeoElement replacement = add("R18m=Line(R18P,R18Q)");
		GeoIdentityRecord replacementRecord = register(replacement,
				signature(PROVIDER, "GENERIC", "VALUE", 1));
		edit(replacement, "R18m=PerpendicularLine(R18P,Line(R18Q,R18R))");

		for (GeoIdentityRecord original : List.of(inPlaceRecord, softRecord,
				replacementRecord)) {
			GeoIdentityRecord current = registry().getGeoRecord(original.getId());
			assertEquals(1, current.getDefinitionRevision());
			assertEquals(0, current.getTopologyRevision());
			assertNotNull(registry().getGeo(original.getId()));
		}
	}

	@Test
	void redef19EveryContinuitySignatureMismatchRejectsAtomically() {
		List<SpatialRedefineSignature> incompatible = List.of(
				signature("wrong-provider", "POINT", "VALUE", 1),
				new SpatialRedefineSignature("r19-family", "LINE", SCHEMA, 1,
						EditAuthorityMode.PROJECTION_DEFINED,
						ProjectionBindingRole.DEFINING, "VALUE", 1),
				new SpatialRedefineSignature("r19-schema", "POINT", "wrong.schema", 1,
						EditAuthorityMode.PROJECTION_DEFINED,
						ProjectionBindingRole.DEFINING, "VALUE", 1),
				new SpatialRedefineSignature("r19-version", "POINT", SCHEMA, 2,
						EditAuthorityMode.PROJECTION_DEFINED,
						ProjectionBindingRole.DEFINING, "VALUE", 1),
				new SpatialRedefineSignature("r19-authority", "POINT", SCHEMA, 1,
						EditAuthorityMode.SPATIAL_DEFINED,
						ProjectionBindingRole.DEFINING, "VALUE", 1),
				new SpatialRedefineSignature("r19-role", "POINT", SCHEMA, 1,
						EditAuthorityMode.PROJECTION_DEFINED,
						ProjectionBindingRole.DERIVED, "VALUE", 1));
		for (int index = 0; index < incompatible.size(); index++) {
			String provider = "r19-" + index;
			SpatialRedefineSignature oldSignature = signature(provider, "POINT",
					"VALUE", 1);
			SpatialRedefineSignature candidateSignature = index == 0
					? incompatible.get(index)
					: withProvider(incompatible.get(index), provider);
			GeoNumeric old = add("R19" + index + "=1");
			GeoIdentityRecord original = register(old, oldSignature);
			GeoNumeric candidate = new GeoNumeric(getConstruction(), 2);
			registry().registerRedefineProvider(new CompatibleProvider(provider,
					candidateSignature, SpatialRedefineDecision.RETAIN));
			String beforeXml = getApp().getXML();
			SpatialRedefineTransaction transaction = registry().prepareRedefine(
					registry().captureRedefineContext(old), candidate, 1, true);
			assertEquals(SpatialRedefineDecision.REJECT, transaction.getDecision());
			assertThrows(SpatialIdentityException.class,
					() -> transaction.commit(candidate));
			transaction.rollback();
			assertEquals(beforeXml, getApp().getXML());
			assertSame(old, registry().getGeo(original.getId()));
		}
	}

	@Test
	void redef20ExplicitTrueReplacementIsFreshAndUndoRestoresOldSnapshot() {
		activateUndo();
		GeoNumeric old = add("R20=1");
		GeoIdentityRecord original = register(old,
				signature(PROVIDER, "NUMERIC", "VALUE", 1));
		registry().registerRedefineProvider(new FreshOnThreeProvider());
		getApp().storeUndoInfo();
		String oldXml = getApp().getXML();
		EvalInfo replacement = EvalInfoFactory.getEvalInfoForRedefinition(
				getKernel(), old, true).withSpatialReplacementOperation();

		getAlgebraProcessor().changeGeoElementNoExceptionHandling(old, "R20=3",
				replacement, false, null, new CapturingErrorHandler());
		getApp().storeUndoInfo();

		PersistentGeoId fresh = registry().getPersistentGeoId(lookup("R20"));
		assertNotNull(fresh);
		assertNotEquals(original.getId(), fresh);
		getKernel().undo();
		assertEquals(oldXml, getApp().getXML());
		assertEquals(original.getId(),
				registry().getPersistentGeoId(lookup("R20")));

		String sameProvider = "g9a3.redef20.same-handle";
		GeoNumeric sameHandle = add("R20S=4");
		GeoIdentityRecord sameOriginal = register(sameHandle,
				signature(sameProvider, "NUMERIC", "VALUE", 1));
		registry().registerRedefineProvider(new CompatibleProvider(sameProvider,
				sameOriginal.toRedefineSignature(), SpatialRedefineDecision.FRESH));
		SpatialRedefineContext sameContext = registry()
				.captureRedefineContext(sameHandle);
		SpatialRedefineTransaction sameTransaction = registry().prepareRedefine(
				sameContext, sameHandle, List.of(sameHandle), true);

		sameTransaction.commit(sameHandle);
		getConstruction().completeSpatialRedefineOperation(sameContext);

		PersistentGeoId sameFresh = registry().getPersistentGeoId(sameHandle);
		assertNotNull(sameFresh);
		assertNotEquals(sameOriginal.getId(), sameFresh);
		assertNull(registry().getGeoRecord(sameOriginal.getId()));
		assertSame(sameHandle, registry().getGeo(sameFresh));
	}

	@Test
	void redef21MissingExplicitTargetContextRejectsBeforeProviderInspection() {
		GeoNumeric candidate = add("R21=1");
		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> registry().prepareRedefine(null,
						new SpatialRedefineProposal(candidate,
								signature(PROVIDER, "NUMERIC", "VALUE", 1),
								1, true)));
		assertEquals(SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
				failure.getDiagnostic().getCode());
	}

	@Test
	void redef22LabelTypeCoordinatesAndPositionNeverGrantContinuity() {
		GeoNumeric old = add("R22=5");
		GeoIdentityRecord original = register(old,
				signature(PROVIDER, "NUMERIC", "VALUE", 1));
		GeoNumeric lookalike = new GeoNumeric(getConstruction(), 5);
		lookalike.setLabelSimple("R22");
		String beforeXml = getApp().getXML();

		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().replaceWithoutSpatialRedefineAuthority(
						old, lookalike));

		assertEquals(beforeXml, getApp().getXML());
		assertSame(old, registry().getGeo(original.getId()));
		assertNull(registry().getPersistentGeoId(lookalike));
	}

	@Test
	void redef23StaleContextAndTransactionCannotRewindNewerPublication()
			throws Exception {
		GeoNumeric old = add("R23=1");
		GeoIdentityRecord original = register(old,
				signature(PROVIDER, "NUMERIC", "VALUE", 1));
		registry().registerRedefineProvider(new CompatibleProvider(PROVIDER,
				original.toRedefineSignature(), SpatialRedefineDecision.RETAIN));
		SpatialRedefineContext staleContext = registry().captureRedefineContext(old);
		GeoNumeric staleCandidate = new GeoNumeric(getConstruction(), 9);
		SpatialRedefineTransaction stale = registry().prepareRedefine(staleContext,
				staleCandidate, 1, true);
		edit(old, "R23=2");
		String newerXml = getApp().getXML();
		PersistentGeoId currentId = registry().getPersistentGeoId(lookup("R23"));

		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().rollbackSpatialRedefine(stale));

		assertEquals(newerXml, getApp().getXML());
		assertEquals(2, ((GeoNumeric) lookup("R23")).getDouble());
		assertEquals(currentId, registry().getPersistentGeoId(lookup("R23")));

		GeoElement current = lookup("R23");
		SpatialRedefineContext bareStale = registry().captureRedefineContext(current);
		edit(current, "R23=3");
		String latestXml = getApp().getXML();
		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().rollbackSpatialRedefinePreparation(bareStale));
		assertEquals(latestXml, getApp().getXML());
		assertEquals(3, ((GeoNumeric) lookup("R23")).getDouble());

		GeoNumeric graphPublished = add("R23G=4");
		SpatialRedefineContext graphStale = registry().captureRedefineContext(
				lookup("R23"));
		GeoIdentityRecord graphRecord = register(graphPublished,
				signature(PROVIDER, "NUMERIC", "VALUE", 1));
		String graphPublicationXml = getApp().getXML();
		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().rollbackSpatialRedefinePreparation(graphStale));
		assertEquals(graphPublicationXml, getApp().getXML());
		assertSame(graphPublished, registry().getGeo(graphRecord.getId()));

		G9A2SpatialSemanticRuntimeTest.Graph graph =
				G9A2SpatialSemanticRuntimeTest.Graph.createProductiveWithHinge(
						getConstruction(), this::add);
		SpatialRedefineContext runtimeStale = registry().captureRedefineContext(
				lookup("R23"));
		long runtimeEpoch = getConstruction().getSpatialSemanticRuntime()
				.getPublicationEpoch();
		graph.setSupportEnd(1, 1, 0);
		getKernel().updateConstruction(false);
		String runtimePublicationXml = getApp().getXML();
		String runtimePublicationToken = getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(graph.objectId).getValueSnapshotToken();
		assertTrue(getConstruction().getSpatialSemanticRuntime()
				.getPublicationEpoch() > runtimeEpoch);
		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().rollbackSpatialRedefinePreparation(runtimeStale));
		assertEquals(runtimePublicationXml, getApp().getXML());
		assertEquals(runtimePublicationToken,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate(graph.objectId)
						.getValueSnapshotToken());

		GeoNumeric ownLive = add("R23L=5");
		GeoIdentityRecord ownLiveRecord = register(ownLive,
				signature(PROVIDER, "NUMERIC", "VALUE", 1));
		SpatialRedefineContext ownLiveContext = registry().captureRedefineContext(
				ownLive);
		String ownLiveXml = ownLiveContext.getRollbackXml();
		SpatialRedefineTransaction ownLiveTransaction = registry().prepareRedefine(
				ownLiveContext, ownLive, List.of(ownLive), false);
		ownLiveTransaction.commit(ownLive);
		assertThrows(SpatialIdentityException.class,
				() -> registry().beginRedefinePublicationLease(
						List.of(ownLiveContext)));
		getConstruction().rollbackSpatialRedefine(ownLiveTransaction);
		assertEquals(ownLiveXml,
				getConstruction().getCurrentUndoXML(false).toString());
		assertEquals(ownLiveRecord.getId(),
				registry().getPersistentGeoId(lookup("R23L")));

		GeoNumeric ownRebuild = add("R23B=7");
		GeoIdentityRecord ownRebuildRecord = register(ownRebuild,
				signature(PROVIDER, "NUMERIC", "VALUE", 1));
		add("R23BChild=R23B+1");
		SpatialRedefineContext ownRebuildContext = registry()
				.captureRedefineContext(ownRebuild);
		String ownRebuildXml = ownRebuildContext.getRollbackXml();
		GeoNumeric ownRebuildCandidate = new GeoNumeric(getConstruction(), 8);
		SpatialRedefineTransaction ownRebuildTransaction = registry().prepareRedefine(
				ownRebuildContext, ownRebuildCandidate,
				List.of(ownRebuildCandidate), false);
		getConstruction().replace(ownRebuild, ownRebuildCandidate,
				new EvalInfo(true).withSpatialRedefineContext(ownRebuildContext)
						.withSpatialRedefineTransaction(ownRebuildTransaction));
		getConstruction().rollbackSpatialRedefine(ownRebuildTransaction);
		assertEquals(ownRebuildXml,
				getConstruction().getCurrentUndoXML(false).toString());
		assertEquals(ownRebuildRecord.getId(),
				registry().getPersistentGeoId(lookup("R23B")));
		assertEquals(8, lookup("R23BChild").evaluateDouble());

		GeoNumeric tokenTarget = add("R23Token=21");
		GeoIdentityRecord tokenRecord = register(tokenTarget,
				signature(PROVIDER, "NUMERIC", "VALUE", 1));
		SpatialRedefineContext tokenContext = registry().captureRedefineContext(
				tokenTarget);
		String tokenEntryXml = getApp().getXML();
		try (RedefinePublicationLease tokenLease = registry()
				.beginRedefinePublicationLease(List.of(tokenContext))) {
			assertNotNull(tokenLease);
			assertThrows(SpatialIdentityException.class,
					() -> registry().beginLoadSession(LoadPurpose.REDEFINE_REBUILD));
			RedefineRebuildToken token = registry().beginRedefineRebuild(
					List.of(tokenContext));
			assertThrows(SpatialIdentityException.class,
					() -> registry().beginRedefineRebuildLoad(token, 1));
			AppCommon foreignApp = AppCommonFactory.create3D();
			SpatialIdentityRegistry foreignRegistry = foreignApp.getKernel()
					.getConstruction().getSpatialIdentityRegistry();
			assertThrows(SpatialIdentityException.class,
					() -> foreignRegistry.clearForRedefineRebuild(token));
			registry().clearForRedefineRebuild(token);
			LoadSession failedLoad = registry().beginRedefineRebuildLoad(token, 1);
			failedLoad.stageRecord(tokenRecord);
			failedLoad.stageRecord(tokenRecord);
			assertThrows(SpatialIdentityException.class, failedLoad::commit);
			assertThrows(SpatialIdentityException.class,
					() -> registry().beginRedefineRebuildLoad(token, 1));
			assertThrows(SpatialIdentityException.class,
					() -> registry().beginRedefineRebuild(List.of(tokenContext)));
		}
		getConstruction().rollbackSpatialRedefinePreparation(tokenContext);
		assertEquals(tokenEntryXml, getApp().getXML());
		assertEquals(tokenRecord.getId(),
				registry().getPersistentGeoId(lookup("R23Token")));
		edit(lookup("R23Token"), "R23Token=22");
		assertEquals(22, lookup("R23Token").evaluateDouble());

		GeoNumeric rebuildHostile = add("R23Hostile=30");
		GeoIdentityRecord rebuildHostileRecord = register(rebuildHostile,
				signature(PROVIDER, "NUMERIC", "VALUE", 1));
		add("R23HostileChild=R23Hostile+1");
		GeoNumeric callbackGeo = add("R23Callback=41");
		GeoIdentityRecord callbackRecord = reserveRecord(
				signature(PROVIDER, "NUMERIC", "VALUE", 1));
		SpatialRedefineContext rebuildHostileContext = registry()
				.captureRedefineContext(rebuildHostile);
		GeoNumeric rebuildHostileCandidate = new GeoNumeric(getConstruction(), 31);
		SpatialRedefineTransaction rebuildHostileTransaction = registry()
				.prepareRedefine(rebuildHostileContext, rebuildHostileCandidate,
						List.of(rebuildHostileCandidate), false);
		String rebuildHostileXml = getApp().getXML();
		AtomicBoolean rebuildCallbackAttempted = new AtomicBoolean();
		View rebuildListener = mock(View.class);
		doAnswer(invocation -> {
			if (rebuildCallbackAttempted.compareAndSet(false, true)) {
				registry().registerParticipation(callbackGeo, callbackRecord);
			}
			return null;
		}).when(rebuildListener).reset();
		getKernel().attach(rebuildListener);
		try {
			assertThrows(RuntimeException.class,
					() -> getConstruction().replace(rebuildHostile,
							rebuildHostileCandidate,
							new EvalInfo(true)
									.withSpatialRedefineContext(
											rebuildHostileContext)
									.withSpatialRedefineTransaction(
											rebuildHostileTransaction)));
		} finally {
			getKernel().detach(rebuildListener);
		}
		assertTrue(rebuildCallbackAttempted.get());
		assertEquals(rebuildHostileXml, getApp().getXML());
		assertEquals(rebuildHostileRecord.getId(),
				registry().getPersistentGeoId(lookup("R23Hostile")));
		assertNull(registry().getGeo(callbackRecord.getId()));

		GeoElement liveSupportForGraphCallback = lookup("G9A2HE");
		final PersistentGeoId liveSupportId = registry().getPersistentGeoId(
				liveSupportForGraphCallback);
		GeoIdentityRecord updateCallbackRecord = reserveRecord(
				signature(PROVIDER, "NUMERIC", "VALUE", 1));
		String graphCallbackXml = getApp().getXML();
		AtomicBoolean graphCallbackAttempted = new AtomicBoolean();
		View graphCallback = mock(View.class);
		doAnswer(invocation -> {
			if (registry().isRedefineRuntimePublicationStaged()
					&& graphCallbackAttempted.compareAndSet(false, true)) {
				registry().registerParticipation(callbackGeo, updateCallbackRecord);
			}
			return null;
		}).when(graphCallback).update(any(GeoElement.class));
		getKernel().attach(graphCallback);
		CapturingErrorHandler graphCallbackErrors;
		try {
			graphCallbackErrors = attemptRedefine(liveSupportForGraphCallback,
					"G9A2HE=(4,0,0)");
		} finally {
			getKernel().detach(graphCallback);
		}
		assertTrue(graphCallbackAttempted.get(), graphCallbackErrors::describe);
		assertTrue(graphCallbackErrors.hasError());
		assertEquals(graphCallbackXml, getApp().getXML());
		assertNull(registry().getGeo(updateCallbackRecord.getId()));
		assertEquals(liveSupportId,
				registry().getPersistentGeoId(lookup("G9A2HE")));

		GeoElement caughtGraphSupport = lookup("G9A2HE");
		final PersistentGeoId caughtGraphSupportId = registry().getPersistentGeoId(
				caughtGraphSupport);
		GeoNumeric caughtGraphParticipant = add("R23CaughtGraph=43");
		final GeoIdentityRecord caughtGraphRecord = register(caughtGraphParticipant,
				signature(PROVIDER, "NUMERIC", "VALUE", 1));
		String caughtGraphXml = getApp().getXML();
		AtomicBoolean caughtGraphAttempted = new AtomicBoolean();
		AtomicBoolean caughtGraphRejected = new AtomicBoolean();
		View caughtGraphCallback = mock(View.class);
		doAnswer(invocation -> {
			if (registry().isRedefineRuntimePublicationStaged()
					&& caughtGraphAttempted.compareAndSet(false, true)) {
				try {
					registry().retireGeo(caughtGraphParticipant);
				} catch (SpatialIdentityException expected) {
					caughtGraphRejected.set(true);
				}
			}
			return null;
		}).when(caughtGraphCallback).update(any(GeoElement.class));
		getKernel().attach(caughtGraphCallback);
		CapturingErrorHandler caughtGraphErrors;
		try {
			caughtGraphErrors = attemptRedefine(caughtGraphSupport,
					"G9A2HE=(5,0,0)");
		} finally {
			getKernel().detach(caughtGraphCallback);
		}
		assertTrue(caughtGraphAttempted.get());
		assertTrue(caughtGraphRejected.get());
		assertTrue(caughtGraphErrors.hasError());
		assertEquals(caughtGraphXml, getApp().getXML());
		assertEquals(caughtGraphSupportId,
				registry().getPersistentGeoId(lookup("G9A2HE")));
		assertEquals(caughtGraphRecord.getId(),
				registry().getPersistentGeoId(lookup("R23CaughtGraph")));

		// The stale-runtime subcase above deliberately left this graph
		// inconsistent. Adapter rollback continuity requires a live payload.
		GeoPoint3D restoredSupport = (GeoPoint3D) lookup("G9A2HE");
		restoredSupport.setCoords(1, 0, 0, 1);
		restoredSupport.updateRepaint();
		getKernel().updateConstruction(false);
		assertTrue(getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(graph.objectId)
				.getSemanticCertificate().hasPayload());

		GeoElement addAllSupport = lookup("G9A2HE");
		PersistentGeoId addAllSupportId = registry().getPersistentGeoId(addAllSupport);
		add("R23AddParam=6");
		GeoElement addAllCandidate = add(
				"R23AddCandidate=(R23AddParam,0,0)");
		SpatialRedefineContext addAllContext = registry().captureRedefineContext(
				addAllSupport);
		SpatialRedefineTransaction addAllTransaction = registry().prepareRedefine(
				addAllContext, addAllCandidate, List.of(addAllCandidate), false);
		final String addAllXml = getApp().getXML();
		SpatialSemanticRuntime addAllRuntime =
				getConstruction().getSpatialSemanticRuntime();
		GeoElement oldRollbackAdapter = addAllRuntime.getDerivedPoint(graph.objectId);
		final String addAllCertificate = addAllRuntime
				.getSpatialPointCertificate(graph.objectId).getValueSnapshotToken();
		final String addAllSystemCertificate = addAllRuntime
				.getProjectionSystemCertificate(graph.systemId).getValueSnapshotToken();
		final Vector3 addAllPayload = addAllRuntime
				.getSpatialPointCertificate(graph.objectId)
				.getSemanticCertificate().getPoint().orElseThrow();
		final long addAllPublicationEpoch = addAllRuntime.getPublicationEpoch();
		final Map<SpatialIdentityId, Long> addAllPublicationCounts = addAllRuntime
				.getInstrumentation().snapshotAuthoritativePublicationCounts();
		final long addAllAdapterPublications = addAllRuntime.getInstrumentation()
				.getDerivedViewPublications();
		final long addAllAdapterWithdrawals = addAllRuntime.getInstrumentation()
				.getDerivedViewWithdrawals();
		AtomicBoolean addAllAttempted = new AtomicBoolean();
		AtomicBoolean nestedCaptureRejected = new AtomicBoolean();
		AtomicBoolean nestedCollectionRejected = new AtomicBoolean();
		AtomicBoolean directRuntimeRejected = new AtomicBoolean();
		AtomicBoolean caughtRuntimePublication = new AtomicBoolean();
		AtomicBoolean oldRollbackAdapterRemoved = new AtomicBoolean();
		AtomicBoolean undefinedRollbackAdapterObserved = new AtomicBoolean();
		AtomicBoolean multipleRollbackAdaptersObserved = new AtomicBoolean();
		AtomicReference<GeoElement> observedRollbackAdapter = new AtomicReference<>();
		View addAllListener = mock(View.class);
		doAnswer(invocation -> {
			GeoElement added = invocation.getArgument(0);
			GeoElement activeAdapter = addAllRuntime.getDerivedPoint(graph.objectId);
			if (added == activeAdapter) {
				if (!added.isDefined()) {
					undefinedRollbackAdapterObserved.set(true);
				}
				GeoElement previous = observedRollbackAdapter.getAndSet(added);
				if (previous != null && previous != added) {
					multipleRollbackAdaptersObserved.set(true);
				}
			}
			if (addAllAttempted.compareAndSet(false, true)) {
				try {
					registry().captureRedefineContext(
							registry().getGeo(addAllSupportId));
				} catch (SpatialIdentityException expected) {
					nestedCaptureRejected.set(true);
				}
				try {
					getConstruction().startCollectingRedefineCalls();
				} catch (SpatialIdentityException expected) {
					nestedCollectionRejected.set(true);
				}
				try {
					getConstruction().getSpatialSemanticRuntime().clear();
				} catch (SpatialIdentityException expected) {
					directRuntimeRejected.set(true);
				}
				GeoNumeric sameSystemInput = (GeoNumeric) lookup("G9A2Axis");
				sameSystemInput.setValue(1);
				try {
					sameSystemInput.updateRepaint();
				} catch (SpatialIdentityException expected) {
					caughtRuntimePublication.set(true);
				}
			}
			return null;
		}).when(addAllListener).add(any(GeoElement.class));
		doAnswer(invocation -> {
			if (invocation.getArgument(0) == oldRollbackAdapter) {
				oldRollbackAdapterRemoved.set(true);
			}
			return null;
		}).when(addAllListener).remove(any(GeoElement.class));
		getKernel().attach(addAllListener);
		try {
			assertThrows(RuntimeException.class,
					() -> getConstruction().replace(addAllSupport, addAllCandidate,
							new EvalInfo(true)
									.withSpatialRedefineContext(addAllContext)
									.withSpatialRedefineTransaction(
											addAllTransaction)));
		} finally {
			getKernel().detach(addAllListener);
		}
		assertTrue(addAllAttempted.get());
		assertTrue(nestedCaptureRejected.get());
		assertTrue(nestedCollectionRejected.get());
		assertTrue(directRuntimeRejected.get());
		assertTrue(caughtRuntimePublication.get());
		assertEquals(addAllXml, getApp().getXML());
		assertEquals(0, lookup("G9A2Axis").evaluateDouble());
		assertEquals(addAllSupportId,
				registry().getPersistentGeoId(lookup("G9A2HE")));
		assertEquals(addAllCertificate,
				addAllRuntime
						.getSpatialPointCertificate(graph.objectId)
						.getValueSnapshotToken());
		assertEquals(addAllSystemCertificate, addAllRuntime
				.getProjectionSystemCertificate(graph.systemId).getValueSnapshotToken());
		assertEquals(addAllPayload, addAllRuntime
				.getSpatialPointCertificate(graph.objectId).getSemanticCertificate()
				.getPoint().orElseThrow());
		assertEquals(addAllPublicationEpoch, addAllRuntime.getPublicationEpoch());
		assertEquals(addAllPublicationCounts, addAllRuntime.getInstrumentation()
				.snapshotAuthoritativePublicationCounts());
		assertEquals(addAllAdapterPublications, addAllRuntime.getInstrumentation()
				.getDerivedViewPublications());
		assertEquals(addAllAdapterWithdrawals, addAllRuntime.getInstrumentation()
				.getDerivedViewWithdrawals());
		GeoElement restoredRollbackAdapter = addAllRuntime.getDerivedPoint(graph.objectId);
		assertTrue(oldRollbackAdapterRemoved.get());
		assertNotSame(oldRollbackAdapter, restoredRollbackAdapter);
		assertSame(restoredRollbackAdapter, observedRollbackAdapter.get());
		assertTrue(restoredRollbackAdapter.isDefined());
		assertFalse(undefinedRollbackAdapterObserved.get());
		assertFalse(multipleRollbackAdaptersObserved.get());

		GeoElement liveSupportForRuntimeCallback = lookup("G9A2HE");
		String runtimeCallbackXml = getApp().getXML();
		final String runtimeCallbackToken = getConstruction()
				.getSpatialSemanticRuntime()
				.getSpatialPointCertificate(graph.objectId).getValueSnapshotToken();
		AtomicBoolean runtimeCallbackAttempted = new AtomicBoolean();
		View runtimeCallback = mock(View.class);
		doAnswer(invocation -> {
			if (registry().isRedefineRuntimePublicationStaged()
					&& runtimeCallbackAttempted.compareAndSet(false, true)) {
				GeoNumeric sameSystemInput = (GeoNumeric) lookup("G9A2Axis");
				sameSystemInput.setValue(1);
				sameSystemInput.updateRepaint();
			}
			return null;
		}).when(runtimeCallback).update(any(GeoElement.class));
		getKernel().attach(runtimeCallback);
		CapturingErrorHandler runtimeCallbackErrors;
		try {
			runtimeCallbackErrors = attemptRedefine(liveSupportForRuntimeCallback,
					"G9A2HE=(5,0,0)");
		} finally {
			getKernel().detach(runtimeCallback);
		}
		assertTrue(runtimeCallbackAttempted.get());
		assertTrue(runtimeCallbackErrors.hasError());
		assertEquals(runtimeCallbackXml, getApp().getXML());
		assertEquals(0, lookup("G9A2Axis").evaluateDouble());
		assertEquals(runtimeCallbackToken,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate(graph.objectId)
						.getValueSnapshotToken());

		add("R23Param=1");
		add("R23Listener=11");
		AtomicBoolean listenerPublished = new AtomicBoolean();
		AtomicReference<GeoIdentityRecord> listenerRecord = new AtomicReference<>();
		View listener = mock(View.class);
		doAnswer(invocation -> {
			GeoElement announced = invocation.getArgument(0);
			if (announced == getConstruction().getSpatialSemanticRuntime()
					.getDerivedPoint(graph.objectId)
					&& listenerPublished.compareAndSet(false, true)) {
				listenerRecord.set(register((GeoNumeric) lookup("R23Listener"),
						signature(PROVIDER, "NUMERIC", "VALUE", 1)));
			}
			return null;
		}).when(listener).add(any(GeoElement.class));
		getKernel().attach(listener);
		GeoElement currentSupport = lookup("G9A2HE");
		PersistentGeoId supportId = registry().getPersistentGeoId(currentSupport);
		CapturingErrorHandler listenerErrors = new CapturingErrorHandler();
		try {
			getAlgebraProcessor().changeGeoElementNoExceptionHandling(currentSupport,
					"G9A2HE=(R23Param,0,0)", EvalInfoFactory.getEvalInfoForRedefinition(
							getKernel(), currentSupport, true), false, null,
					listenerErrors);
		} finally {
			getKernel().detach(listener);
		}
		assertFalse(listenerErrors.hasError(), listenerErrors::describe);
		assertTrue(listenerPublished.get());
		assertNotNull(listenerRecord.get());
		assertSame(lookup("R23Listener"),
				registry().getGeo(listenerRecord.get().getId()));
		assertEquals(supportId,
				registry().getPersistentGeoId(lookup("G9A2HE")));
	}

	@Test
	void redef24StableRoleGroupIsIndependentOfCandidateEnumerationOrder()
			throws Exception {
		OutputPair old = intersectPair("R24c", "R24g");
		String provider = "g9a3.redef24";
		GeoIdentityRecord left = register(old.left,
				signature(provider, "POINT", "LEFT", 2));
		GeoIdentityRecord right = register(old.right,
				signature(provider, "POINT", "RIGHT", 2));
		OutputPair candidate = intersectPair("R24d", "R24h");
		final String leftValue = candidate.left.toValueString(
				StringTemplate.xmlTemplate);
		final String rightValue = candidate.right.toValueString(
				StringTemplate.xmlTemplate);
		GroupProvider groupProvider = new GroupProvider(provider, candidate.left,
				candidate.right, "LEFT", "RIGHT", SpatialRedefineDecision.RETAIN);
		registry().registerRedefineProvider(groupProvider);
		SpatialRedefineContext context = registry().captureRedefineContext(old.left);
		SpatialRedefineTransaction transaction = registry().prepareRedefine(
				context, candidate.left,
				List.of(candidate.right, candidate.left), false);

		assertEquals(SpatialRedefineDecision.RETAIN, transaction.getDecision());
		assertEquals(left.getId(), transaction.getDecidedId("LEFT"));
		assertEquals(right.getId(), transaction.getDecidedId("RIGHT"));
		assertTrue(groupProvider.inspected);
		EvalInfo info = new EvalInfo(true).withSpatialRedefineContext(context)
				.withSpatialRedefineTransaction(transaction);
		getConstruction().replace(old.left, candidate.left, info);
		getConstruction().completeSpatialRedefineOperation(context);
		GeoElement installedLeft = registry().getGeo(left.getId());
		GeoElement installedRight = registry().getGeo(right.getId());
		assertEquals(1, registry().getGeoRecord(left.getId())
				.getDefinitionRevision());
		assertEquals(1, registry().getGeoRecord(right.getId())
				.getDefinitionRevision());
		assertNotSame(old.left, installedLeft);
		assertNotSame(old.right, installedRight);
		assertEquals(leftValue,
				installedLeft.toValueString(StringTemplate.xmlTemplate));
		assertEquals(rightValue,
				installedRight.toValueString(StringTemplate.xmlTemplate));
		assertEquals(left.getId(), registry().getPersistentGeoId(installedLeft));
		assertEquals(right.getId(), registry().getPersistentGeoId(installedRight));

		OutputPair inPlaceOld = intersectPair("R24ic", "R24ig");
		String inPlaceProvider = "g9a3.redef24.in-place";
		GeoIdentityRecord inPlaceLeft = register(inPlaceOld.left,
				signature(inPlaceProvider, "POINT", "LEFT", 2));
		GeoIdentityRecord inPlaceRight = register(inPlaceOld.right,
				signature(inPlaceProvider, "POINT", "RIGHT", 2));
		OutputPair parsedSameParent = intersectExisting("R24ic", "R24ig");
		assertSame(inPlaceOld.left, parsedSameParent.left);
		assertSame(inPlaceOld.right, parsedSameParent.right);
		GroupProvider inPlaceGroup = new GroupProvider(inPlaceProvider,
				parsedSameParent.left, parsedSameParent.right, "LEFT", "RIGHT",
				SpatialRedefineDecision.RETAIN);
		registry().registerRedefineProvider(inPlaceGroup);
		SpatialRedefineContext inPlaceContext = registry()
				.captureRedefineContext(inPlaceOld.left);
		SpatialRedefineTransaction inPlaceTransaction = registry().prepareRedefine(
				inPlaceContext, parsedSameParent.left,
				List.of(parsedSameParent.right, parsedSameParent.left), false);
		EvalInfo inPlaceInfo = new EvalInfo(true)
				.withSpatialRedefineContext(inPlaceContext)
				.withSpatialRedefineTransaction(inPlaceTransaction);

		getConstruction().replace(inPlaceOld.left, parsedSameParent.left,
				inPlaceInfo);
		getConstruction().completeSpatialRedefineOperation(inPlaceContext);

		assertSame(inPlaceOld.left, registry().getGeo(inPlaceLeft.getId()));
		assertSame(inPlaceOld.right, registry().getGeo(inPlaceRight.getId()));
		assertEquals(1, registry().getGeoRecord(inPlaceLeft.getId())
				.getDefinitionRevision());
		assertEquals(1, registry().getGeoRecord(inPlaceRight.getId())
				.getDefinitionRevision());

		OutputPair rebuildOld = intersectPair("R24rc", "R24rg");
		rebuildOld.left.setLabel("R24RL");
		rebuildOld.right.setLabel("R24RR");
		add("R24Child=Line(R24RR,(0,1))");
		assertTrue(rebuildOld.right.hasChildren());
		String rebuildProvider = "g9a3.redef24.rebuild";
		final GeoIdentityRecord rebuildLeft = register(rebuildOld.left,
				signature(rebuildProvider, "POINT", "LEFT", 2));
		final GeoIdentityRecord rebuildRight = register(rebuildOld.right,
				signature(rebuildProvider, "POINT", "RIGHT", 2));
		OutputPair rebuildCandidate = intersectPair("R24rd", "R24rh");
		GroupProvider rebuildGroup = new GroupProvider(rebuildProvider,
				rebuildCandidate.left, rebuildCandidate.right, "LEFT", "RIGHT",
				SpatialRedefineDecision.RETAIN);
		registry().registerRedefineProvider(rebuildGroup);
		activateUndo();
		getApp().storeUndoInfo();
		SpatialRedefineContext rebuildContext = registry()
				.captureRedefineContext(rebuildOld.left);
		final String beforeRebuild = getApp().getXML();
		SpatialRedefineTransaction rebuildTransaction = registry().prepareRedefine(
				rebuildContext, rebuildCandidate.left,
				List.of(rebuildCandidate.right, rebuildCandidate.left), false);
		assertEquals(SpatialRedefineDecision.RETAIN,
				rebuildTransaction.getDecision());
		assertTrue(rebuildGroup.inspected);
		EvalInfo rebuildInfo = new EvalInfo(true)
				.withSpatialRedefineContext(rebuildContext)
				.withSpatialRedefineTransaction(rebuildTransaction);

		getConstruction().replace(rebuildOld.left, rebuildCandidate.left,
				rebuildInfo);
		getConstruction().completeSpatialRedefineOperation(rebuildContext);
		getApp().storeUndoInfo();

		GeoElement rebuiltLeft = registry().getGeo(rebuildLeft.getId());
		GeoElement rebuiltRight = registry().getGeo(rebuildRight.getId());
		GeoElement rebuiltChild = lookup("R24Child");
		assertNotSame(rebuildOld.left, rebuiltLeft);
		assertNotSame(rebuildOld.right, rebuiltRight);
		assertSame(rebuiltRight, rebuiltChild.getParentAlgorithm().getInput()[0]);
		assertTrue(rebuiltChild.isDefined());
		assertEquals(1, registry().getGeoRecord(rebuildLeft.getId())
				.getDefinitionRevision());
		assertEquals(1, registry().getGeoRecord(rebuildRight.getId())
				.getDefinitionRevision());
		assertEquals(0, registry().getGeoRecord(rebuildLeft.getId())
				.getTopologyRevision());
		assertEquals(0, registry().getGeoRecord(rebuildRight.getId())
				.getTopologyRevision());
		String rebuiltXml = getApp().getXML();

		getKernel().undo();
		assertEquals(beforeRebuild, getApp().getXML());
		assertEquals(rebuildLeft.getId(),
				registry().getPersistentGeoId(lookup("R24RL")));
		assertEquals(rebuildRight.getId(),
				registry().getPersistentGeoId(lookup("R24RR")));
		assertSame(registry().getGeo(rebuildRight.getId()),
				lookup("R24Child").getParentAlgorithm().getInput()[0]);

		getKernel().redo();
		assertEquals(rebuiltXml, getApp().getXML());
		assertSame(registry().getGeo(rebuildRight.getId()),
				lookup("R24Child").getParentAlgorithm().getInput()[0]);
		getApp().getXMLio().processXMLString(rebuiltXml, true, false, false);
		assertEquals(rebuiltXml, getApp().getXML());
		assertEquals(1, registry().getGeoRecord(rebuildLeft.getId())
				.getDefinitionRevision());
		assertEquals(1, registry().getGeoRecord(rebuildRight.getId())
				.getDefinitionRevision());
		assertSame(registry().getGeo(rebuildRight.getId()),
				lookup("R24Child").getParentAlgorithm().getInput()[0]);
		assertTrue(lookup("R24Child").isDefined());

		OutputPair collectedOld = intersectPair("R24bc", "R24bg");
		collectedOld.left.setLabel("R24BL");
		collectedOld.right.setLabel("R24BR");
		add("R24BatchChild=Line(R24BR,(0,2))");
		String collectedProvider = "g9a3.redef24.collected";
		GeoIdentityRecord collectedLeft = register(collectedOld.left,
				signature(collectedProvider, "POINT", "LEFT", 2));
		GeoIdentityRecord collectedRight = register(collectedOld.right,
				signature(collectedProvider, "POINT", "RIGHT", 2));
		OutputPair collectedCandidate = intersectPair("R24bd", "R24bh");
		GroupProvider collectedGroup = new GroupProvider(collectedProvider,
				collectedCandidate.left, collectedCandidate.right, "LEFT", "RIGHT",
				SpatialRedefineDecision.RETAIN);
		registry().registerRedefineProvider(collectedGroup);
		getConstruction().startCollectingRedefineCalls();
		SpatialRedefineContext collectedContext = registry()
				.captureRedefineContext(collectedOld.left);
		SpatialRedefineTransaction collectedTransaction = registry().prepareRedefine(
				collectedContext, collectedCandidate.left,
				List.of(collectedCandidate.right, collectedCandidate.left), false);
		getConstruction().replace(collectedOld.left, collectedCandidate.left,
				new EvalInfo(true).withSpatialRedefineContext(collectedContext)
						.withSpatialRedefineTransaction(collectedTransaction));
		getConstruction().processCollectedRedefineCalls();

		assertTrue(collectedGroup.inspected);
		assertEquals(collectedLeft.getId(),
				registry().getPersistentGeoId(registry().getGeo(collectedLeft.getId())));
		assertEquals(collectedRight.getId(),
				registry().getPersistentGeoId(registry().getGeo(collectedRight.getId())));
		assertSame(registry().getGeo(collectedRight.getId()),
				lookup("R24BatchChild").getParentAlgorithm().getInput()[0]);
		assertTrue(lookup("R24BatchChild").isDefined());
	}

	@Test
	void redef25AmbiguousPartialOrdinalAndCardinalityChangingGroupsReject() {
		String partialProvider = "g9a3.redef25.partial";
		OutputPair old = intersectPair("R25c", "R25g");
		register(old.left, signature(partialProvider, "POINT", "LEFT", 2));
		register(old.right, signature(partialProvider, "POINT", "RIGHT", 2));
		OutputPair candidate = intersectPair("R25d", "R25h");
		GroupProvider partial = new GroupProvider(partialProvider, candidate.left,
				candidate.right, "LEFT", "RIGHT", SpatialRedefineDecision.RETAIN);
		registry().registerRedefineProvider(partial);
		String beforePartial = getApp().getXML();
		assertThrows(SpatialIdentityException.class,
				() -> registry().prepareRedefine(
						registry().captureRedefineContext(old.left), candidate.left,
						List.of(candidate.left), false));
		assertFalse(partial.inspected);
		assertEquals(beforePartial, getApp().getXML());

		SpatialRedefineSignature duplicate = signature("duplicate", "POINT",
				"LEFT", 2);
		assertThrows(IllegalArgumentException.class,
				() -> SpatialRedefineOutputGroup.of(List.of(
						new SpatialRedefineCandidateOutput(candidate.left, duplicate),
						new SpatialRedefineCandidateOutput(candidate.right, duplicate))));

		String ordinalProvider = "g9a3.redef25.ordinal";
		OutputPair ordinalOld = intersectPair("R25i", "R25j");
		register(ordinalOld.left,
				signature(ordinalProvider, "POINT", "LEFT", 2));
		register(ordinalOld.right,
				signature(ordinalProvider, "POINT", "RIGHT", 2));
		OutputPair ordinalCandidate = intersectPair("R25k", "R25l");
		GroupProvider ordinal = new GroupProvider(ordinalProvider,
				ordinalCandidate.left, ordinalCandidate.right, "FIRST", "SECOND",
				SpatialRedefineDecision.RETAIN);
		registry().registerRedefineProvider(ordinal);
		SpatialRedefineTransaction ordinalTransaction = registry().prepareRedefine(
				registry().captureRedefineContext(ordinalOld.left),
				ordinalCandidate.left,
				List.of(ordinalCandidate.left, ordinalCandidate.right), false);
		assertEquals(SpatialRedefineDecision.REJECT,
				ordinalTransaction.getDecision());
		assertFalse(ordinal.inspected);
		ordinalTransaction.rollback();

		String cardinalityProvider = "g9a3.redef25.cardinality";
		GeoNumeric cardinalityOld = add("R25N=1");
		GeoIdentityRecord cardinalityRecord = register(cardinalityOld,
				signature(cardinalityProvider, "POINT", "VALUE", 1));
		OutputPair cardinalityCandidate = intersectPair("R25m", "R25n");
		GroupProvider hostile = new GroupProvider(cardinalityProvider,
				cardinalityCandidate.left, cardinalityCandidate.right, "LEFT", "RIGHT",
				SpatialRedefineDecision.FRESH);
		registry().registerRedefineProvider(hostile);
		long allocations = registry().getInstrumentation().getAllocations();
		SpatialRedefineTransaction cardinalityTransaction = registry().prepareRedefine(
				registry().captureRedefineContext(cardinalityOld),
				cardinalityCandidate.left,
				List.of(cardinalityCandidate.right, cardinalityCandidate.left), true);
		assertEquals(SpatialRedefineDecision.REJECT,
				cardinalityTransaction.getDecision());
		assertFalse(hostile.inspected);
		assertTrue(cardinalityTransaction.getDecidedIds().isEmpty());
		assertEquals(allocations,
				registry().getInstrumentation().getAllocations());
		cardinalityTransaction.rollback();
		assertSame(cardinalityOld, registry().getGeo(cardinalityRecord.getId()));
	}

	private static SpatialRedefineSignature withProvider(
			SpatialRedefineSignature source, String provider) {
		return new SpatialRedefineSignature(provider, source.getFamily(),
				source.getSchemaId(), source.getSchemaVersion(), source.getAuthority(),
				source.getBindingRole(), source.getStableOutputRole(),
				source.getOutputCardinality());
	}
}
