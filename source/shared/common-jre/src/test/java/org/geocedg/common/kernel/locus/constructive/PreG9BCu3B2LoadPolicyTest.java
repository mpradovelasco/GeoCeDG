/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry.LoadPurpose;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry.LoadSession;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU3 increment {@code CU3-B2}, gate K6 (K6 focal review; G-B2-1 record section 5): a
 * load that restores a document governs the evaluation of its elements by the persisted authority
 * policy from the moment the policy record is validated, so reopening and saving an unedited
 * document leaves its token ledger unchanged; a failed load restores the policy the registry had
 * before (validation matrix rows B2-13 and B2-22).
 */
final class PreG9BCu3B2LoadPolicyTest extends ConstructiveModelReplicaTest {

	@Test
	void k6x1ALegacyDocumentKeepsItsPolicyAndItsLedgerOnReopen() {
		GeoLocusIntersectionResult result = segment(1.99, false);
		claim(result);
		String state = result.getTokenLedgerState();

		reopen();
		reopen();

		assertFalse(AuthorityPolicyAdoption.isConstructive(getConstruction()));
		assertEquals(state, result().getTokenLedgerState(), "B2-22: legacy ledger unchanged");
		assertTrue(state.startsWith("4|"), "the legacy ledger keeps version 4");
	}

	@Test
	void k6x2ReopeningAConstructiveDocumentIsIdempotent() {
		for (boolean claimed : new boolean[] {true, false}) {
			getApp().getKernel().clearConstruction(true);
			GeoLocusIntersectionResult result = segment(1.9999, true);
			if (claimed) {
				claim(result);
			}
			String state = result.getTokenLedgerState();
			List<String> tokens = tokens(result);

			reopen();
			assertEquals(state, result().getTokenLedgerState(),
					"B2-13: the ledger state is unchanged, claimed=" + claimed);
			reopen();
			assertEquals(state, result().getTokenLedgerState());
			assertEquals(tokens, tokens(result()),
					"B2-22: even an unclaimed anchor keeps its token, claimed=" + claimed);
			assertTrue(AuthorityPolicyAdoption.isConstructive(getConstruction()));
		}
	}

	@Test
	void k6x3TheReplicaWithTwoAnchorsReopensWithTheSameLedger() {
		buildAuthorReplica();
		add("R=Intersect(e_1,c_1)");
		AuthorityPolicyAdoption.adopt(getConstruction());
		String state = result().getTokenLedgerState();
		List<String> tokens = tokens(result());

		reopen();

		assertEquals(state, result().getTokenLedgerState(), "B2-22: unclaimed anchors");
		assertEquals(tokens, tokens(result()));
	}

	@Test
	void k6x4TheStagedPolicyGovernsTheLoadAndAbortRestoresThePreviousOne() {
		SpatialIdentityRegistry registry = getConstruction().getSpatialIdentityRegistry();
		assertNull(registry.getDocumentAuthorityRecord());

		LoadSession session = registry.beginLoadSession(LoadPurpose.NATIVE_OR_UNDO_RESTORE);
		session.stageRecord(DocumentAuthorityRecord.ELEMENT, constructiveAttributes());
		assertEquals(DocumentAuthorityRecord.CONSTRUCTIVE_V1,
				registry.getDocumentAuthorityRecord(),
				"the validated record governs the rest of the load");
		session.abort();
		assertNull(registry.getDocumentAuthorityRecord(), "abort restores the previous policy");

		LoadSession duplicate = registry.beginLoadSession(LoadPurpose.NATIVE_OR_UNDO_RESTORE);
		duplicate.stageRecord(DocumentAuthorityRecord.ELEMENT, constructiveAttributes());
		assertThrows(RuntimeException.class, () -> duplicate.stageRecord(
				DocumentAuthorityRecord.ELEMENT, constructiveAttributes()),
				"a second record fails closed");
		duplicate.abort();
		assertNull(registry.getDocumentAuthorityRecord());

		Map<String, String> unknown = constructiveAttributes();
		unknown.put("authority", "constructive-authority/v9");
		LoadSession unsupported = registry.beginLoadSession(LoadPurpose.NATIVE_OR_UNDO_RESTORE);
		assertThrows(RuntimeException.class, () -> unsupported.stageRecord(
				DocumentAuthorityRecord.ELEMENT, unknown), "an unknown version fails closed");
		assertNull(registry.getDocumentAuthorityRecord(), "an invalid record is never published");
		unsupported.abort();
		assertNull(registry.getDocumentAuthorityRecord());
	}

	@Test
	void k6x5AClipboardImportNeverChangesThePolicyOfTheDestination() {
		SpatialIdentityRegistry registry = getConstruction().getSpatialIdentityRegistry();
		LoadSession session = registry.beginLoadSession(LoadPurpose.CLIPBOARD_IMPORT);
		session.stageRecord(DocumentAuthorityRecord.ELEMENT, constructiveAttributes());
		assertNull(registry.getDocumentAuthorityRecord(),
				"the destination policy governs a clipboard import");
		assertThrows(RuntimeException.class, session::commit,
				"a clipboard section with a policy record fails closed");
		session.abort();
		assertNull(registry.getDocumentAuthorityRecord());
	}

	@Test
	void k6x6AnAbortedLoadOfAnAdoptedDocumentRestoresItsPolicy() {
		AuthorityPolicyAdoption.adopt(getConstruction());
		SpatialIdentityRegistry registry = getConstruction().getSpatialIdentityRegistry();
		LoadSession session = registry.beginLoadSession(LoadPurpose.NATIVE_OR_UNDO_RESTORE);
		session.stageRecord(DocumentAuthorityRecord.ELEMENT, constructiveAttributes());
		session.abort();
		assertEquals(DocumentAuthorityRecord.CONSTRUCTIVE_V1,
				registry.getDocumentAuthorityRecord(), "the policy before the load is restored");
	}

	@Test
	void k6x7UndoAndRedoAcrossAMoveRestoreTheLedgerWithoutReservations() {
		GeoLocusIntersectionResult result = segment(1.9999, true);
		claim(result);
		getApp().storeUndoInfo();
		add("A=(1.9995,0)");
		getApp().storeUndoInfo();

		getApp().getUndoManager().undo();
		String undone = result().getTokenLedgerState();
		getApp().getUndoManager().redo();
		String redone = result().getTokenLedgerState();
		getApp().getUndoManager().undo();
		assertEquals(undone, result().getTokenLedgerState(),
				"undo restores the same ledger state each time, without reservations");
		getApp().getUndoManager().redo();
		assertEquals(redone, result().getTokenLedgerState(),
				"redo restores the same ledger state each time, without reservations");
		assertTrue(((GeoPoint) lookup("X")).isDefined());
	}

	// ---------------------------------------------------------------- helpers

	private void reopen() {
		getApp().setXML(getApp().getXML(), true);
	}

	private static Map<String, String> constructiveAttributes() {
		DocumentAuthorityRecord record = DocumentAuthorityRecord.CONSTRUCTIVE_V1;
		Map<String, String> attributes = new LinkedHashMap<>();
		attributes.put("authority", record.getAuthority());
		attributes.put("classTable", record.getClassTable());
		attributes.put("relaxedAdmission", record.getRelaxedAdmission());
		attributes.put("certifier", record.getCertifier());
		attributes.put("transfer", record.getTransfer());
		return attributes;
	}

	private GeoLocusIntersectionResult segment(double a, boolean adopt) {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("X0=Intersect(Line(N,Line((0,0),(0,1))),Line((0,3),(1,3)))");
		add("lx=LocusV2(X0,N)");
		add("A=(" + a + ",0)");
		add("g=Line(A,Line((0,0),(0,1)))");
		if (adopt) {
			AuthorityPolicyAdoption.adopt(getConstruction());
		}
		return add("R=Intersect(lx,g)");
	}

	private GeoLocusIntersectionResult result() {
		return (GeoLocusIntersectionResult) lookup("R");
	}

	private GeoPoint claim(GeoLocusIntersectionResult result) {
		String token = result.getIntersectionResult().getFiniteSolutions().stream()
				.map(solution -> solution.getIdentity().getRootToken())
				.filter(result::isPointAdmissible).findFirst().orElseThrow();
		return LocusV2PublicOperations.selectIntersectionPoint(getConstruction(), "X", result,
				new GeoText(getConstruction(), token));
	}

	private static List<String> tokens(GeoLocusIntersectionResult result) {
		List<String> tokens = new ArrayList<>();
		for (LocusIntersectionSolution2D solution
				: result.getIntersectionResult().getFiniteSolutions()) {
			tokens.add(solution.getIdentity().getRootToken());
		}
		return tokens;
	}
}
