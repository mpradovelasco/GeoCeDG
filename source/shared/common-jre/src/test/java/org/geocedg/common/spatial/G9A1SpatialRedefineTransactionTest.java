/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry.LoadPurpose;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineContext;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineDecision;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineProposal;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineSignature;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineTransaction;
import org.geocedg.common.kernel.spatial.identity.SpatialTokenSource;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

class G9A1SpatialRedefineTransactionTest extends BaseUnitTest {
	private static final String PROVIDER = "g9a1.test";
	private static final String FAMILY = "POINT";
	private static final String SCHEMA = "cedg.point.orthographic";

	@Test
	void exactSingleOutputTopologyPreservingRedefineRetainsIdentity() {
		SpatialIdentityRegistry registry = registry();
		GeoElement oldTarget = add("A=1");
		GeoIdentityRecord record = register(registry, oldTarget);
		GeoElement actualCandidate = add("B=2");
		registry.registerRedefineProvider(new CandidateProvider(actualCandidate,
				SpatialRedefineDecision.RETAIN));
		SpatialRedefineTransaction transaction = registry.prepareRedefine(
				registry.captureRedefineContext(oldTarget),
				new SpatialRedefineProposal(actualCandidate,
						record.toRedefineSignature(), 1, true));

		transaction.commit(actualCandidate);

		assertEquals(SpatialRedefineDecision.RETAIN, transaction.getDecision());
		assertEquals(SpatialRedefineTransaction.State.COMMITTED, transaction.getState());
		assertEquals(record.getId(), registry.getPersistentGeoId(actualCandidate));
		assertNull(registry.getPersistentGeoId(oldTarget));
		assertEquals(1, registry.getGeoRecord(record.getId()).getDefinitionRevision());
		assertEquals(0, registry.getGeoRecord(record.getId()).getTopologyRevision());
		assertEquals(1, registry.getInstrumentation().getRedefineCommits());

		SpatialIdentityRegistry overflowRegistry = registry();
		GeoElement overflowOldTarget = add("C=3");
		PersistentGeoId overflowId = overflowRegistry.allocatePersistentGeoId();
		GeoIdentityRecord overflowRecord = new GeoIdentityRecord(overflowId,
				PROVIDER, FAMILY, SCHEMA, 1,
				EditAuthorityMode.PROJECTION_DEFINED,
				ProjectionBindingRole.DEFINING, "POINT", 1, Long.MAX_VALUE, 7);
		overflowRegistry.registerParticipation(overflowOldTarget, overflowRecord);
		GeoElement overflowCandidate = add("D=4");
		overflowRegistry.registerRedefineProvider(new CandidateProvider(
				overflowCandidate, SpatialRedefineDecision.RETAIN));
		SpatialRedefineTransaction overflowTransaction =
				overflowRegistry.prepareRedefine(
						overflowRegistry.captureRedefineContext(overflowOldTarget),
						new SpatialRedefineProposal(overflowCandidate,
								overflowRecord.toRedefineSignature(), 1, true));

		SpatialIdentityException overflow = assertThrows(
				SpatialIdentityException.class,
				() -> overflowTransaction.commit(overflowCandidate));

		assertEquals(SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
				overflow.getDiagnostic().getCode());
		assertSame(overflowOldTarget, overflowRegistry.getGeo(overflowId));
		assertNull(overflowRegistry.getPersistentGeoId(overflowCandidate));
		assertEquals(overflowRecord, overflowRegistry.getGeoRecord(overflowId));
		assertEquals(SpatialRedefineTransaction.State.PREPARED,
				overflowTransaction.getState());
		overflowTransaction.rollback();
	}

	@Test
	void freshDecisionRetiresOldIdentityInsteadOfTransferringIt() {
		SpatialIdentityRegistry registry = new SpatialIdentityRegistry(
				new ScriptedTokenSource(1, 2, 3, 1, 2, 4), 4);
		GeoElement oldTarget = add("A=1");
		GeoIdentityRecord record = register(registry, oldTarget);
		SpatialObjectId dependentId = registry.allocateSpatialObjectId();
		registry.registerRecords(Collections.singletonList(new SpatialObjectRecord(
				dependentId, 1, "POINT", EditAuthorityMode.PROJECTION_DEFINED,
				SCHEMA, 1, Collections.singletonList(record.getId()), 0, 0)));
		GeoElement actualCandidate = add("B=2");
		registry.registerRedefineProvider(new CandidateProvider(actualCandidate,
				SpatialRedefineDecision.FRESH));
		SpatialRedefineTransaction transaction = registry.prepareRedefine(
				registry.captureRedefineContext(oldTarget),
				new SpatialRedefineProposal(actualCandidate,
						record.toRedefineSignature(), 1, true, true));
		PersistentGeoId decided = transaction.getDecidedId();

		// Simulate the host XML-rebuild branch: the transaction-specific section
		// has already restored only the fresh result before the decision commits.
		String rebuildView = registry.writeSpatialSectionForRedefine(transaction);
		assertTrue(rebuildView.contains(decided.toExternalForm()));
		assertFalse(rebuildView.contains(record.getId().toExternalForm()));
		assertFalse(rebuildView.contains(dependentId.toExternalForm()));
		registry.clearPreservingRetiredTokens();
		GeoIdentityRecord rebuiltRecord = new GeoIdentityRecord(decided, PROVIDER,
				FAMILY, SCHEMA, 1, EditAuthorityMode.PROJECTION_DEFINED,
				ProjectionBindingRole.DEFINING, "POINT", 1, 0, 0);
		SpatialIdentityRegistry.LoadSession load = registry.beginLoadSession(
				LoadPurpose.REDEFINE_REBUILD);
		load.stageRecord(rebuiltRecord);
		load.stageGeoAttachment(actualCandidate, decided);
		load.commit();
		transaction.commit(actualCandidate);

		assertNotEquals(record.getId(), decided);
		assertNull(registry.getRecord(record.getId()));
		assertNull(registry.getRecord(dependentId));
		assertEquals(2, transaction.getRetiredIds().size());
		assertSame(actualCandidate, registry.getGeo(decided));
		assertEquals(decided, registry.getPersistentGeoId(actualCandidate));
		assertEquals(token(4), registry.allocatePersistentGeoId().getRawToken());

		SpatialIdentityRegistry changedClosureRegistry = registry();
		GeoElement closureOldTarget = add("C=3");
		GeoIdentityRecord closureRecord = register(changedClosureRegistry,
				closureOldTarget);
		GeoElement closureCandidate = add("D=4");
		changedClosureRegistry.registerRedefineProvider(new CandidateProvider(
				closureCandidate, SpatialRedefineDecision.FRESH));
		SpatialRedefineTransaction changedClosure =
				changedClosureRegistry.prepareRedefine(
						changedClosureRegistry.captureRedefineContext(
								closureOldTarget),
						new SpatialRedefineProposal(closureCandidate,
								closureRecord.toRedefineSignature(), 1, true, true));
		SpatialObjectId lateDependentId =
				changedClosureRegistry.allocateSpatialObjectId();
		SpatialObjectRecord lateDependent = new SpatialObjectRecord(
				lateDependentId, 1, "POINT",
				EditAuthorityMode.PROJECTION_DEFINED, SCHEMA, 1,
				Collections.singletonList(closureRecord.getId()), 0, 0);
		changedClosureRegistry.registerRecords(
				Collections.singletonList(lateDependent));

		SpatialIdentityException viewFailure = assertThrows(
				SpatialIdentityException.class,
				() -> changedClosureRegistry.writeSpatialSectionForRedefine(
						changedClosure));
		SpatialIdentityException commitFailure = assertThrows(
				SpatialIdentityException.class,
				() -> changedClosure.commit(closureCandidate));

		assertEquals(SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
				viewFailure.getDiagnostic().getCode());
		assertEquals(SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
				commitFailure.getDiagnostic().getCode());
		assertSame(closureOldTarget,
				changedClosureRegistry.getGeo(closureRecord.getId()));
		assertEquals(lateDependent,
				changedClosureRegistry.getRecord(lateDependentId));
		assertNull(changedClosureRegistry.getPersistentGeoId(closureCandidate));
		changedClosure.rollback();
	}

	@Test
	void providerCannotRetainAnIncompatibleSignature() {
		SpatialIdentityRegistry registry = registry();
		GeoElement oldTarget = add("A=1");
		GeoIdentityRecord record = register(registry, oldTarget);
		GeoElement candidate = add("B=2");
		registry.registerRedefineProvider(new CandidateProvider(candidate,
				SpatialRedefineDecision.RETAIN));
		SpatialRedefineSignature incompatible = new SpatialRedefineSignature(
				PROVIDER, "LINE", SCHEMA, 1, EditAuthorityMode.PROJECTION_DEFINED,
				ProjectionBindingRole.DEFINING, "POINT", 1);

		SpatialRedefineTransaction transaction = registry.prepareRedefine(
				registry.captureRedefineContext(oldTarget),
				new SpatialRedefineProposal(candidate, incompatible, 1, true));
		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> transaction.commit(candidate));

		assertEquals(SpatialRedefineDecision.REJECT, transaction.getDecision());
		assertEquals(SpatialIdentityDiagnostic.Code.REDEFINE_REJECTED,
				failure.getDiagnostic().getCode());
		assertSame(oldTarget, registry.getGeo(record.getId()));
		assertNull(registry.getPersistentGeoId(candidate));
	}

	@Test
	void cardinalityAndTopologyArePartOfTheRetainPredicate() {
		SpatialIdentityRegistry registry = registry();
		GeoElement oldTarget = add("A=1");
		GeoIdentityRecord record = register(registry, oldTarget);
		GeoElement candidate = add("B=2");
		registry.registerRedefineProvider(new CandidateProvider(candidate,
				SpatialRedefineDecision.RETAIN));
		SpatialRedefineContext context = registry.captureRedefineContext(oldTarget);

		SpatialRedefineTransaction multiple = registry.prepareRedefine(context,
				new SpatialRedefineProposal(candidate,
						record.toRedefineSignature(), 2, true));
		SpatialRedefineTransaction topologyChange = registry.prepareRedefine(context,
				new SpatialRedefineProposal(candidate,
						record.toRedefineSignature(), 1, false));

		assertEquals(SpatialRedefineDecision.REJECT, multiple.getDecision());
		assertEquals(SpatialRedefineDecision.REJECT, topologyChange.getDecision());
		assertSame(oldTarget, registry.getGeo(record.getId()));
	}

	@Test
	void missingExplicitContextCannotReachAProviderDecision() {
		SpatialIdentityRegistry registry = registry();
		GeoElement candidate = add("A=1");
		SpatialRedefineProposal proposal = new SpatialRedefineProposal(candidate,
				signature(), 1, true);

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> registry.prepareRedefine(null, proposal));

		assertEquals(SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
				failure.getDiagnostic().getCode());
	}

	@Test
	void missingProviderRejectsBeforeMutation() {
		SpatialIdentityRegistry registry = registry();
		GeoElement oldTarget = add("A=1");
		GeoIdentityRecord record = register(registry, oldTarget);
		GeoElement candidate = add("B=2");

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> registry.prepareRedefine(
						registry.captureRedefineContext(oldTarget),
						new SpatialRedefineProposal(candidate,
								record.toRedefineSignature(), 1, true)));

		assertEquals(SpatialIdentityDiagnostic.Code.REDEFINE_PROVIDER_MISSING,
				failure.getDiagnostic().getCode());
		assertSame(oldTarget, registry.getGeo(record.getId()));
	}

	@Test
	void serializationOverlayIsScopedAndDoesNotPublishCandidateIdentity() {
		SpatialIdentityRegistry registry = registry();
		GeoElement oldTarget = add("A=1");
		GeoIdentityRecord record = register(registry, oldTarget);
		GeoElement candidate = add("B=2");
		registry.registerRedefineProvider(new CandidateProvider(candidate,
				SpatialRedefineDecision.RETAIN));
		SpatialRedefineTransaction transaction = registry.prepareRedefine(
				registry.captureRedefineContext(oldTarget),
				new SpatialRedefineProposal(candidate,
						record.toRedefineSignature(), 1, true, true));

		try (SpatialIdentityRegistry.SerializationOverlay ignored =
				registry.beginRedefineSerializationOverlay(transaction)) {
			assertEquals(record.getId(),
					registry.getPersistentGeoIdForSerialization(candidate));
			assertNull(registry.getPersistentGeoId(candidate));
		}

		assertNull(registry.getPersistentGeoIdForSerialization(candidate));
		assertSame(oldTarget, registry.getGeo(record.getId()));
	}

	@Test
	void rollbackLeavesOldIdentityCurrentAndCannotBeRepeated() {
		SpatialIdentityRegistry registry = new SpatialIdentityRegistry(
				new ScriptedTokenSource(1, 2, 2, 3), 2);
		GeoElement oldTarget = add("A=1");
		GeoIdentityRecord record = register(registry, oldTarget);
		GeoElement candidate = add("B=2");
		registry.registerRedefineProvider(new CandidateProvider(candidate,
				SpatialRedefineDecision.FRESH));
		SpatialRedefineTransaction transaction = registry.prepareRedefine(
				registry.captureRedefineContext(oldTarget),
				new SpatialRedefineProposal(candidate,
						record.toRedefineSignature(), 1, true, true));

		transaction.rollback();

		assertEquals(SpatialRedefineTransaction.State.ROLLED_BACK,
				transaction.getState());
		assertSame(oldTarget, registry.getGeo(record.getId()));
		assertNull(registry.getPersistentGeoId(candidate));
		assertThrows(SpatialIdentityException.class, transaction::rollback);
		assertEquals(1, registry.getInstrumentation().getRedefineRollbacks());
		PersistentGeoId nextId = registry.allocatePersistentGeoId();
		assertNotEquals(transaction.getDecidedId(), nextId);
		assertEquals(token(3), nextId.getRawToken());
	}

	@Test
	void freshDecisionCanCommitAgainstAnInPlaceHostResult() {
		SpatialIdentityRegistry registry = registry();
		GeoElement oldTarget = add("A=1");
		GeoIdentityRecord record = register(registry, oldTarget);
		registry.registerRedefineProvider(new CandidateProvider(oldTarget,
				SpatialRedefineDecision.FRESH));
		SpatialRedefineTransaction transaction = registry.prepareRedefine(
				registry.captureRedefineContext(oldTarget),
				new SpatialRedefineProposal(oldTarget,
						record.toRedefineSignature(), 1, true, true));
		PersistentGeoId freshId = transaction.getDecidedId();

		transaction.commit(oldTarget);

		assertNotEquals(record.getId(), freshId);
		assertNull(registry.getRecord(record.getId()));
		assertSame(oldTarget, registry.getGeo(freshId));
		assertEquals(freshId, registry.getPersistentGeoId(oldTarget));

		SpatialIdentityRegistry rejectingRegistry = registry();
		GeoElement protectedTarget = add("C=1");
		GeoIdentityRecord protectedRecord = register(rejectingRegistry,
				protectedTarget);
		GeoElement unlabeledCandidate = new GeoNumeric(getConstruction(), 2);
		rejectingRegistry.registerRedefineProvider(new CandidateProvider(
				unlabeledCandidate, SpatialRedefineDecision.FRESH));
		SpatialRedefineTransaction rejected = rejectingRegistry.prepareRedefine(
				rejectingRegistry.captureRedefineContext(protectedTarget),
				new SpatialRedefineProposal(unlabeledCandidate,
						protectedRecord.toRedefineSignature(), 1, true, true));

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> rejected.commit(unlabeledCandidate));
		assertEquals(SpatialIdentityDiagnostic.Code.GEO_NOT_SERIALIZABLE,
				failure.getDiagnostic().getCode());
		assertSame(protectedTarget,
				rejectingRegistry.getGeo(protectedRecord.getId()));
		rejected.rollback();
	}

	@Test
	void commitCannotAttachTheDecisionToAnotherParticipant() {
		SpatialIdentityRegistry registry = registry();
		GeoElement oldTarget = add("A=1");
		GeoIdentityRecord oldRecord = register(registry, oldTarget);
		GeoElement candidate = add("B=2");
		GeoElement unrelated = add("C=3");
		GeoIdentityRecord unrelatedRecord = register(registry, unrelated);
		registry.registerRedefineProvider(new CandidateProvider(candidate,
				SpatialRedefineDecision.RETAIN));
		SpatialRedefineTransaction transaction = registry.prepareRedefine(
				registry.captureRedefineContext(oldTarget),
				new SpatialRedefineProposal(candidate,
						oldRecord.toRedefineSignature(), 1, true));

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> transaction.commit(unrelated));

		assertEquals(SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
				failure.getDiagnostic().getCode());
		assertSame(oldTarget, registry.getGeo(oldRecord.getId()));
		assertSame(unrelated, registry.getGeo(unrelatedRecord.getId()));
		assertNull(registry.getPersistentGeoId(candidate));
		transaction.rollback();

		SpatialIdentityRegistry unlabeledRegistry = new SpatialIdentityRegistry(
				getConstruction(), new SequentialTokenSource(), 4);
		GeoElement unlabeledOldTarget = add("D=4");
		GeoIdentityRecord unlabeledRecord = register(unlabeledRegistry,
				unlabeledOldTarget);
		GeoElement unlabeledCandidate = new GeoNumeric(getConstruction(), 5);
		unlabeledRegistry.registerRedefineProvider(new CandidateProvider(
				unlabeledCandidate, SpatialRedefineDecision.RETAIN));
		SpatialRedefineTransaction unlabeledTransaction =
				unlabeledRegistry.prepareRedefine(
						unlabeledRegistry.captureRedefineContext(unlabeledOldTarget),
						new SpatialRedefineProposal(unlabeledCandidate,
								unlabeledRecord.toRedefineSignature(), 1, true));

		SpatialIdentityException unlabeledFailure = assertThrows(
				SpatialIdentityException.class,
				() -> unlabeledTransaction.commit(unlabeledCandidate));

		assertEquals(SpatialIdentityDiagnostic.Code.GEO_NOT_SERIALIZABLE,
				unlabeledFailure.getDiagnostic().getCode());
		assertSame(unlabeledOldTarget,
				unlabeledRegistry.getGeo(unlabeledRecord.getId()));
		assertNull(unlabeledRegistry.getPersistentGeoId(unlabeledCandidate));
		assertEquals(unlabeledRecord,
				unlabeledRegistry.getGeoRecord(unlabeledRecord.getId()));
		unlabeledTransaction.rollback();

		SpatialIdentityRegistry foreignRegistry = new SpatialIdentityRegistry(
				getConstruction(), new SequentialTokenSource(), 4);
		GeoElement foreignOldTarget = add("E=5");
		GeoIdentityRecord foreignRecord = register(foreignRegistry,
				foreignOldTarget);
		GeoNumeric foreignCandidate = new GeoNumeric(
				AppCommonFactory.create().getKernel().getConstruction(), 6);
		foreignCandidate.setLabel("ForeignCandidate");
		foreignRegistry.registerRedefineProvider(new CandidateProvider(
				foreignCandidate, SpatialRedefineDecision.RETAIN));
		SpatialRedefineTransaction foreignTransaction =
				foreignRegistry.prepareRedefine(
						foreignRegistry.captureRedefineContext(foreignOldTarget),
						new SpatialRedefineProposal(foreignCandidate,
								foreignRecord.toRedefineSignature(), 1, true));

		SpatialIdentityException foreignFailure = assertThrows(
				SpatialIdentityException.class,
				() -> foreignTransaction.commit(foreignCandidate));

		assertEquals(SpatialIdentityDiagnostic.Code.GEO_ATTACHMENT_MISSING,
				foreignFailure.getDiagnostic().getCode());
		assertSame(foreignOldTarget,
				foreignRegistry.getGeo(foreignRecord.getId()));
		assertNull(foreignRegistry.getPersistentGeoId(foreignCandidate));
		assertEquals(foreignRecord,
				foreignRegistry.getGeoRecord(foreignRecord.getId()));
		foreignTransaction.rollback();
	}

	@Test
	void serializationOverlayRejectsForeignAndFinishedTransactions() {
		SpatialIdentityRegistry registry = registry();
		SpatialIdentityRegistry foreignRegistry = registry();
		GeoElement oldTarget = add("A=1");
		GeoIdentityRecord record = register(registry, oldTarget);
		GeoElement candidate = add("B=2");
		registry.registerRedefineProvider(new CandidateProvider(candidate,
				SpatialRedefineDecision.RETAIN));
		SpatialRedefineTransaction transaction = registry.prepareRedefine(
				registry.captureRedefineContext(oldTarget),
				new SpatialRedefineProposal(candidate,
						record.toRedefineSignature(), 1, true));

		SpatialIdentityException foreignFailure = assertThrows(
				SpatialIdentityException.class,
				() -> foreignRegistry.beginRedefineSerializationOverlay(transaction));
		assertEquals(SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
				foreignFailure.getDiagnostic().getCode());
		transaction.rollback();
		SpatialIdentityException finishedFailure = assertThrows(
				SpatialIdentityException.class,
				() -> registry.beginRedefineSerializationOverlay(transaction));
		assertEquals(SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
				finishedFailure.getDiagnostic().getCode());
	}

	private static SpatialIdentityRegistry registry() {
		return new SpatialIdentityRegistry(new SequentialTokenSource(), 4);
	}

	private static GeoIdentityRecord register(SpatialIdentityRegistry registry,
			GeoElement geo) {
		GeoIdentityRecord record = new GeoIdentityRecord(
				registry.allocatePersistentGeoId(), PROVIDER, FAMILY, SCHEMA, 1,
				EditAuthorityMode.PROJECTION_DEFINED, ProjectionBindingRole.DEFINING,
				"POINT", 1, 0, 0);
		registry.registerParticipation(geo, record);
		return record;
	}

	private static SpatialRedefineSignature signature() {
		return new SpatialRedefineSignature(PROVIDER, FAMILY, SCHEMA, 1,
				EditAuthorityMode.PROJECTION_DEFINED, ProjectionBindingRole.DEFINING,
				"POINT", 1);
	}

	private static String token(int value) {
		return String.format("%032x", value);
	}

	private static final class SequentialTokenSource implements SpatialTokenSource {
		private int next = 1;

		@Override
		public String nextToken() {
			return token(next++);
		}
	}

	private static final class ScriptedTokenSource implements SpatialTokenSource {
		private final int[] tokens;
		private int next;

		private ScriptedTokenSource(int... tokens) {
			this.tokens = tokens;
		}

		@Override
		public String nextToken() {
			return token(tokens[next++]);
		}
	}

	private static final class CandidateProvider implements SpatialRedefineProvider {
		private final GeoElement acceptedCandidate;
		private final SpatialRedefineDecision decision;

		private CandidateProvider(GeoElement acceptedCandidate,
				SpatialRedefineDecision decision) {
			this.acceptedCandidate = acceptedCandidate;
			this.decision = decision;
		}

		@Override
		public String getProviderId() {
			return PROVIDER;
		}

		@Override
		public SpatialRedefineSignature describeCandidate(SpatialRedefineContext context,
				GeoElement candidate) {
			return signature();
		}

		@Override
		public boolean isTopologyPreserving(SpatialRedefineContext context,
				GeoElement candidate) {
			return candidate == acceptedCandidate;
		}

		@Override
		public SpatialRedefineDecision inspect(SpatialRedefineContext context,
				SpatialRedefineProposal proposal) {
			return proposal.getCandidate() == acceptedCandidate
					? decision : SpatialRedefineDecision.REJECT;
		}
	}
}
