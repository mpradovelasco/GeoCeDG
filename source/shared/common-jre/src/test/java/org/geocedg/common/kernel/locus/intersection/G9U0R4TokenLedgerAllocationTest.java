/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Currentness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DomainLocation;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ResidualQuantityKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;
import org.junit.jupiter.api.Test;

/** Focused G9U0-R4 value tests for current-root ledger allocation. */
final class G9U0R4TokenLedgerAllocationTest {
	private static final String OWNER = "r4-owner";
	private static final String SOURCE_PAIR = "r4-source-pair";
	private static final String CONSTRUCTIVE = "r4-constructive-lineage";
	private static final String TOPOLOGY = "r4-topology";
	private static final String BRANCH = "r4-branch";
	private static final String COMPONENT = "r4-component";
	private static final String CONTINUATION_CONTRACT =
			"r4-provider-target-contract/v1";
	private static final String TARGET_CONTRACT = "r4-target-contract/v1";
	/** Fixed canonical state in the pre-phase-rank ledger-v2 schema. */
	private static final String HISTORICAL_LEDGER_V2 =
			"2|2|72342d6f776e6572~72342d736f757263652d70616972~72342d636f6e7374727563"
			+ "746976652d6c696e65616765~72342d746f706f6c6f6779~1~a,1,72342d6272616e6368"
			+ "2f736f6c7574696f6e2f673975302d72342f6c65646765722d63757272656e742d726f6f"
			+ "742f76322f33303a72342d70726f76696465722d7461726765742d636f6e74726163742f"
			+ "76313134343a673975302d72342f64657465726d696e69737469632d63757272656e742d"
			+ "726f6f742f76312f393a72342d6272616e636839323a673975302d72342f63757272656e"
			+ "742d7472616e7376657273652d726f6f742d6765726d2f76312f393a72342d6272616e63"
			+ "6833383a32373a72342d7461726765742d636f6e746163742d696e64696361746f726e65"
			+ "67617469766531,72342d6272616e6368,673975302d72342f6c65646765722d63757272"
			+ "656e742d726f6f742f76322f33303a72342d70726f76696465722d7461726765742d636f"
			+ "6e74726163742f76313134343a673975302d72342f64657465726d696e69737469632d63"
			+ "757272656e742d726f6f742f76312f393a72342d6272616e636839323a673975302d7234"
			+ "2f63757272656e742d7472616e7376657273652d726f6f742d6765726d2f76312f393a72"
			+ "342d6272616e636833383a32373a72342d7461726765742d636f6e746163742d696e6469"
			+ "6361746f726e6567617469766531,70726f76696465722f7231,72342d7461726765742d"
			+ "636f6e74726163742f7631,3fd0000000000000,72342d70726f76696465722d74617267"
			+ "65742d636f6e74726163742f7631,673975302d72342f64657465726d696e69737469632"
			+ "d63757272656e742d726f6f742f76312f393a72342d6272616e636839323a673975302d7"
			+ "2342f63757272656e742d7472616e7376657273652d726f6f742d6765726d2f76312f393"
			+ "a72342d6272616e636833383a32373a72342d7461726765742d636f6e746163742d696e6"
			+ "4696361746f726e65676174697665|-";
	private static final String HISTORICAL_LEDGER_V2_TOKEN =
			"locus-root/v3/72342d6f776e6572/72342d6272616e6368/673975302d72342f6c6564"
			+ "6765722d63757272656e742d726f6f742f76322f33303a72342d70726f76696465722d74"
			+ "61726765742d636f6e74726163742f76313134343a673975302d72342f64657465726d69"
			+ "6e69737469632d63757272656e742d726f6f742f76312f393a72342d6272616e63683932"
			+ "3a673975302d72342f63757272656e742d7472616e7376657273652d726f6f742d676572"
			+ "6d2f76312f393a72342d6272616e636833383a32373a72342d7461726765742d636f6e74"
			+ "6163742d696e64696361746f726e6567617469766531/1/9b0f35b1bdc6e43a50c3d36bc"
			+ "3d8482be11d17b93021791e355144418d8beb81";
	private static final IntersectionRootDeterministicSelector2D NEGATIVE =
			selector("negative");
	private static final IntersectionRootDeterministicSelector2D POSITIVE =
			selector("positive");

	@Test
	void twoDistinctFirstAllocationsReceiveDistinctOpaqueIdentities() {
		LocusIntersectionTokenLedger2D ledger =
				new LocusIntersectionTokenLedger2D();
		LocusIntersectionTokenLedger2D.Evaluation evaluation = begin(ledger);
		IntersectionRootAddressProof2D firstProof = proof("provider/r1", 0.25);
		IntersectionRootAddressProof2D secondProof = proof("provider/r1", 0.75);
		IntersectionRootAllocation2D first = evaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE, firstProof);
		IntersectionRootAllocation2D second = evaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, POSITIVE, secondProof);

		assertFalse(first.isReused());
		assertFalse(second.isReused());
		assertNotEquals(first.getRootToken(), second.getRootToken());
		assertNotEquals(first.getContinuationKey(), second.getContinuationKey());

		ledger.commit(evaluation, published(1, 1,
				new PublishedRoot(first, firstProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION),
				new PublishedRoot(second, secondProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION)));

		LocusIntersectionTokenLedger2D.Evaluation next = begin(ledger);
		IntersectionRootAllocation2D continuedFirst = next.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE,
				proof("provider/r2", 0.35));
		IntersectionRootAllocation2D continuedSecond = next.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, POSITIVE,
				proof("provider/r2", 0.65));
		assertTrue(continuedFirst.isReused());
		assertTrue(continuedSecond.isReused());
		assertEquals(first.getRootToken(), continuedFirst.getRootToken());
		assertEquals(second.getRootToken(), continuedSecond.getRootToken());
		ledger.abort(next);
	}

	@Test
	void duplicateCurrentSelectorReturnsTheSameAllocationAndFailsClosed() {
		LocusIntersectionTokenLedger2D ledger =
				new LocusIntersectionTokenLedger2D();
		LocusIntersectionTokenLedger2D.Evaluation evaluation = begin(ledger);
		IntersectionRootAddressProof2D address = proof("provider/r1", 0.25);
		IntersectionRootAllocation2D first = evaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE, address);
		IntersectionRootAllocation2D duplicate = evaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE, address);

		assertFalse(first.isReused());
		assertTrue(duplicate.isReused());
		assertEquals(first.getRootToken(), duplicate.getRootToken());
		assertEquals(first.getContinuationKey(), duplicate.getContinuationKey());

		ledger.commit(evaluation, published(1, 1,
				new PublishedRoot(first, address,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION)));
		assertEquals("4|2|" + materialPrefix() + "0|-", ledger.exportState());
	}

	@Test
	void changedRevisionAddressKeepsUniqueDeterministicIdentity() {
		LocusIntersectionTokenLedger2D ledger = committedSingleAllocation();
		String priorToken = currentToken(ledger, "provider/r1", 0.25);
		LocusIntersectionTokenLedger2D.Evaluation evaluation = begin(ledger);
		IntersectionRootAddressProof2D newProof = proof("provider/r2", 0.25);
		IntersectionRootAllocation2D replacement = evaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE, newProof);

		assertTrue(replacement.isReused());
		assertEquals(priorToken, replacement.getRootToken());
		ledger.abort(evaluation);
	}

	@Test
	void differentDeterministicSelectorNeverReusesACommittedAddress() {
		LocusIntersectionTokenLedger2D ledger = committedSingleAllocation();
		String priorToken = currentToken(ledger, "provider/r1", 0.25);
		LocusIntersectionTokenLedger2D.Evaluation evaluation = begin(ledger);
		IntersectionRootAllocation2D replacement =
				evaluation.resolveCurrentRoot(BRANCH, CONTINUATION_CONTRACT,
						POSITIVE, proof("provider/r1", 0.25));

		assertFalse(replacement.isReused());
		assertNotEquals(priorToken, replacement.getRootToken());
		ledger.abort(evaluation);
	}

	@Test
	void selectorRejectsContextMismatchButNotParameterMotion() {
		LocusIntersectionTokenLedger2D ledger = committedSingleAllocation();
		String priorToken = currentToken(ledger, "provider/r1", 0.25);
		LocusIntersectionTokenLedger2D.Evaluation evaluation = begin(ledger);

		IntersectionRootAllocation2D branch = evaluation.resolveCurrentRoot(
				"other-branch", CONTINUATION_CONTRACT,
				NEGATIVE, proof("provider/r1", 0.25));
		IntersectionRootAllocation2D contract = evaluation.resolveCurrentRoot(
				BRANCH, "other-contract", NEGATIVE,
				proof("provider/r1", 0.25));
		IntersectionRootAllocation2D target = evaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE,
				new IntersectionRootAddressProof2D("provider/r1",
						"other-target-contract", 0.25));
		IntersectionRootAllocation2D parameter = evaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE,
				proof("provider/r2", 0.5));
		for (IntersectionRootAllocation2D replacement
				: List.of(branch, contract, target)) {
			assertFalse(replacement.isReused());
			assertNotEquals(priorToken, replacement.getRootToken());
		}
		assertTrue(parameter.isReused());
		assertEquals(priorToken, parameter.getRootToken());
		ledger.abort(evaluation);
	}

	@Test
	void unavailablePublicationBurnsPriorAllocation() {
		LocusIntersectionTokenLedger2D ledger = committedSingleAllocation();
		String priorToken = currentToken(ledger, "provider/r1", 0.25);
		ledger.observeUnavailable();
		LocusIntersectionTokenLedger2D.Evaluation stale = begin(ledger);
		IntersectionRootAllocation2D replacement = stale.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE,
				proof("provider/r1", 0.25));
		assertFalse(replacement.isReused());
		assertNotEquals(priorToken, replacement.getRootToken());
		ledger.abort(stale);
	}

	@Test
	void claimedAllocationBecomesDormantAndReactivatesByExactSelector() {
		LocusIntersectionTokenLedger2D ledger = committedSingleAllocation();
		String token = currentToken(ledger, "provider/r1", 0.25);
		assertTrue(ledger.retainMaterializedToken(token));

		LocusIntersectionTokenLedger2D.Evaluation unavailable = begin(ledger);
		ledger.commit(unavailable, publishedEmpty(2, 2));
		assertFalse(ledger.validatesCurrentToken(token));
		assertTrue(ledger.validatesRetainedToken(token));
		assertTrue(ledger.exportState().contains("~1~d,"));

		IntersectionRootAddressProof2D movedProof = proof("provider/r3", 0.75);
		LocusIntersectionTokenLedger2D.Evaluation returned = begin(ledger);
		IntersectionRootAllocation2D reactivated = returned.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE, movedProof);
		assertTrue(reactivated.isReused());
		assertEquals(token, reactivated.getRootToken());
		ledger.commit(returned, published(3, 3,
				new PublishedRoot(reactivated, movedProof,
						IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED)));
		assertTrue(ledger.validatesCurrentToken(token));
		assertTrue(ledger.exportState().contains("~1~c,"));
	}

	@Test
	void materializedClaimReferenceCountPrunesOnlyAfterLastRelease() {
		LocusIntersectionTokenLedger2D ledger = committedSingleAllocation();
		String token = currentToken(ledger, "provider/r1", 0.25);
		assertTrue(ledger.retainMaterializedToken(token));
		assertTrue(ledger.retainMaterializedToken(token));
		LocusIntersectionTokenLedger2D.Evaluation unavailable = begin(ledger);
		ledger.commit(unavailable, publishedEmpty(2, 2));

		ledger.releaseMaterializedToken(token);
		assertTrue(ledger.validatesRetainedToken(token));
		ledger.releaseMaterializedToken(token);
		assertFalse(ledger.validatesRetainedToken(token));

		LocusIntersectionTokenLedger2D.Evaluation returned = begin(ledger);
		IntersectionRootAllocation2D replacement = returned.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE,
				proof("provider/r3", 0.25));
		assertFalse(replacement.isReused());
		assertNotEquals(token, replacement.getRootToken());
		ledger.abort(returned);
	}

	@Test
	void explicitPermanentRetirementPreventsDormantReactivation() {
		LocusIntersectionTokenLedger2D ledger = committedSingleAllocation();
		String token = currentToken(ledger, "provider/r1", 0.25);
		assertTrue(ledger.retainMaterializedToken(token));
		LocusIntersectionTokenLedger2D.Evaluation seam = begin(ledger);
		seam.retirePersistedToken(token);
		ledger.commit(seam, publishedEmpty(2, 2));
		assertFalse(ledger.validatesRetainedToken(token));

		LocusIntersectionTokenLedger2D.Evaluation returned = begin(ledger);
		IntersectionRootAllocation2D replacement = returned.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE,
				proof("provider/r3", 0.25));
		assertFalse(replacement.isReused());
		assertNotEquals(token, replacement.getRootToken());
		ledger.abort(returned);
	}

	@Test
	void authorizedCopyRebasesContractAndRetainsIncarnation() {
		LocusIntersectionTokenLedger2D ledger = committedSingleAllocation();
		String sourceToken = currentToken(ledger, "provider/r1", 0.25);
		assertTrue(ledger.retainMaterializedToken(sourceToken));
		long sourceIncarnation = LocusSemanticIntersectionToken2D
				.decode(sourceToken).orElseThrow().getIncarnation();
		ledger.authorizeImmediateCopy(OWNER);
		LocusIntersectionTokenLedger2D.Evaluation copy = ledger.begin(
				"r4-copy-owner", "r4-copy-source-pair", CONSTRUCTIVE, TOPOLOGY);
		IntersectionRootAllocation2D rebased = copy.resolveCurrentRoot(BRANCH,
				"r4-copied-provider-target-contract/v1",
				NEGATIVE, proof("provider/copied", 0.25));

		assertTrue(rebased.isReused());
		assertNotEquals(sourceToken, rebased.getRootToken());
		assertEquals(sourceIncarnation, LocusSemanticIntersectionToken2D
				.decode(rebased.getRootToken()).orElseThrow().getIncarnation());
		LocusIntersectionResult2D copiedPublication = published(1, 1,
				new PublishedRoot(rebased, proof("provider/copied", 0.25),
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION));
		ledger.commit(copy, copiedPublication);
		assertEquals(rebased.getRootToken(), ledger.rebaseCopiedToken(sourceToken,
				"r4-copy-owner", OWNER, copiedPublication).orElseThrow());
		assertEquals(rebased.getRootToken(), ledger.rebaseCopiedRetainedToken(
				sourceToken, "r4-copy-owner", OWNER).orElseThrow());
		assertTrue(ledger.validatesCurrentToken(rebased.getRootToken()));
		assertFalse(ledger.exportState().contains("~d,"));
	}

	@Test
	void authorizedCopyRebasesDormantClaimBeforeRootReappears() {
		LocusIntersectionTokenLedger2D ledger = committedSingleAllocation();
		String sourceToken = currentToken(ledger, "provider/r1", 0.25);
		assertTrue(ledger.retainMaterializedToken(sourceToken));
		LocusIntersectionTokenLedger2D.Evaluation unavailable = begin(ledger);
		ledger.commit(unavailable, publishedEmpty(2, 2));

		String copyOwner = "r4-dormant-copy-owner";
		String copyPair = "r4-dormant-copy-pair";
		ledger.authorizeImmediateCopy(OWNER);
		LocusIntersectionTokenLedger2D.Evaluation copied = ledger.begin(copyOwner,
				copyPair, CONSTRUCTIVE, TOPOLOGY);
		ledger.commit(copied, publishedEmptyForPair(copyPair, 3, 3));
		String copiedToken = ledger.rebaseCopiedRetainedToken(sourceToken,
				copyOwner, OWNER).orElseThrow();
		assertNotEquals(sourceToken, copiedToken);
		assertTrue(ledger.validatesRetainedToken(copiedToken));
		assertFalse(ledger.validatesCurrentToken(copiedToken));

		IntersectionRootAddressProof2D copiedProof = proof("copied/provider", 0.75);
		LocusIntersectionTokenLedger2D.Evaluation returned = ledger.begin(copyOwner,
				copyPair, CONSTRUCTIVE, TOPOLOGY);
		IntersectionRootAllocation2D reactivated = returned.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE, copiedProof);
		assertTrue(reactivated.isReused());
		assertEquals(copiedToken, reactivated.getRootToken());
		ledger.commit(returned, publishedForPair(copyPair, 4, 4,
				new PublishedRoot(reactivated, copiedProof,
						IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED)));
		assertTrue(ledger.validatesCurrentToken(copiedToken));
	}

	@Test
	void multiRootCurrentAllocationsCopyThroughOneToOneProvenance() {
		LocusIntersectionTokenLedger2D ledger =
				new LocusIntersectionTokenLedger2D();
		IntersectionRootAddressProof2D firstProof =
				proof("provider/source", 0.25);
		IntersectionRootAddressProof2D secondProof =
				proof("provider/source", 0.75);
		LocusIntersectionTokenLedger2D.Evaluation source = begin(ledger);
		IntersectionRootAllocation2D first = source.resolveCurrentRoot(BRANCH,
				CONTINUATION_CONTRACT, NEGATIVE, firstProof);
		IntersectionRootAllocation2D second = source.resolveCurrentRoot(BRANCH,
				CONTINUATION_CONTRACT, POSITIVE, secondProof);
		ledger.commit(source, published(1, 1,
				new PublishedRoot(first, firstProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION),
				new PublishedRoot(second, secondProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION)));

		ledger.authorizeImmediateCopy(OWNER);
		String copyOwner = "r4-copy-owner";
		String copyPair = "r4-copy-source-pair";
		LocusIntersectionTokenLedger2D.Evaluation copy = ledger.begin(copyOwner,
				copyPair, CONSTRUCTIVE, TOPOLOGY);
		IntersectionRootAddressProof2D copiedFirstProof =
				proof("provider/copied", 0.25);
		IntersectionRootAddressProof2D copiedSecondProof =
				proof("provider/copied", 0.75);
		IntersectionRootAllocation2D copiedFirst = copy.resolveCurrentRoot(BRANCH,
				"r4-copied-contract/v1", NEGATIVE, copiedFirstProof);
		IntersectionRootAllocation2D copiedSecond = copy.resolveCurrentRoot(BRANCH,
				"r4-copied-contract/v1", POSITIVE, copiedSecondProof);
		assertTrue(copiedFirst.isReused());
		assertTrue(copiedSecond.isReused());
		assertNotEquals(copiedFirst.getRootToken(), copiedSecond.getRootToken());
		assertEquals(LocusSemanticIntersectionToken2D.decode(first.getRootToken())
				.orElseThrow().getIncarnation(),
				LocusSemanticIntersectionToken2D.decode(copiedFirst.getRootToken())
						.orElseThrow().getIncarnation());
		assertEquals(LocusSemanticIntersectionToken2D.decode(second.getRootToken())
				.orElseThrow().getIncarnation(),
				LocusSemanticIntersectionToken2D.decode(copiedSecond.getRootToken())
						.orElseThrow().getIncarnation());
		LocusIntersectionResult2D copiedPublication = publishedForPair(copyPair,
				2, 2,
				new PublishedRoot(copiedFirst, copiedFirstProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION),
				new PublishedRoot(copiedSecond, copiedSecondProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION));
		ledger.commit(copy, copiedPublication);
		ledger.validatePreattachmentContext(copyOwner, copyPair, CONSTRUCTIVE,
				TOPOLOGY, OWNER, SOURCE_PAIR, false);
		assertEquals(copiedFirst.getRootToken(), ledger.rebaseCopiedToken(
				first.getRootToken(), copyOwner, OWNER, copiedPublication)
				.orElseThrow());
		assertEquals(copiedSecond.getRootToken(), ledger.rebaseCopiedToken(
				second.getRootToken(), copyOwner, OWNER, copiedPublication)
				.orElseThrow());
	}

	@Test
	void allocationSnapshotRoundTripRetainsExactOpaqueIdentity() {
		LocusIntersectionTokenLedger2D source = committedSingleAllocation();
		String token = currentToken(source, "provider/r1", 0.25);
		String state = source.exportState();

		LocusIntersectionTokenLedger2D restored =
				new LocusIntersectionTokenLedger2D();
		restored.importState(state);
		assertEquals(state, restored.exportState());
		LocusIntersectionTokenLedger2D.Evaluation evaluation = begin(restored);
		IntersectionRootAllocation2D allocation = evaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE,
				proof("provider/r2", 0.5));
		assertTrue(allocation.isReused());
		assertEquals(token, allocation.getRootToken());
		restored.abort(evaluation);
	}

	@Test
	void legacyMintRetainsItsExactReuseAndPersistenceContract() {
		LocusIntersectionTokenLedger2D ledger =
				new LocusIntersectionTokenLedger2D();
		IntersectionRootAddressProof2D address = proof("provider/legacy", 0.5);
		IntersectionTokenLineage2D lineage = new IntersectionTokenLineage2D(
				"legacy-solution", BRANCH, Optional.of("legacy-key"));
		LocusIntersectionTokenLedger2D.Evaluation firstEvaluation = begin(ledger);
		String first = firstEvaluation.mint(lineage, address);
		ledger.commit(firstEvaluation, published(1, 1,
				new PublishedRoot(first, "legacy-key", address,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION)));

		LocusIntersectionTokenLedger2D.Evaluation secondEvaluation = begin(ledger);
		String second = secondEvaluation.mint(lineage, address);
		assertEquals(first, second);
		ledger.commit(secondEvaluation, published(2, 2,
				new PublishedRoot(second, "legacy-key", address,
						IdentityStatus.CONTINUATION_ESTABLISHED)));

		String state = ledger.exportState();
		LocusIntersectionTokenLedger2D restored =
				new LocusIntersectionTokenLedger2D();
		restored.importState(state);
		LocusIntersectionTokenLedger2D.Evaluation restoredEvaluation =
				begin(restored);
		assertEquals(first, restoredEvaluation.mint(lineage, address));
		restored.abort(restoredEvaluation);
	}

	@Test
	void r3SingletonTokenMigratesWithoutChangingItsExactOpaqueMaterial() {
		LocusIntersectionTokenLedger2D source =
				new LocusIntersectionTokenLedger2D();
		String legacyKey = "g9u0/g8c1-explicit-unique-local-root/v1/"
				+ BRANCH.length() + ":" + BRANCH;
		IntersectionTokenLineage2D lineage = new IntersectionTokenLineage2D(
				BRANCH + "/solution/" + legacyKey, BRANCH,
				Optional.of(legacyKey));
		IntersectionRootAddressProof2D initialProof =
				proof("provider/r3", 0.25);
		LocusIntersectionTokenLedger2D.Evaluation r3 = begin(source);
		String exactR3Token = r3.mint(lineage, initialProof);
		source.commit(r3, published(1, 1,
				new PublishedRoot(exactR3Token, legacyKey, initialProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION)));

		LocusIntersectionTokenLedger2D migrated =
				new LocusIntersectionTokenLedger2D();
		migrated.importState(toLegacyV1State(source.exportState()));
		LocusIntersectionTokenLedger2D.Evaluation firstR4 = begin(migrated);
		IntersectionRootAllocation2D bound = firstR4.resolveCurrentRoot(BRANCH,
				CONTINUATION_CONTRACT, NEGATIVE, initialProof, true);
		assertTrue(bound.isReused());
		assertEquals(exactR3Token, bound.getRootToken());
		assertEquals(legacyKey, bound.getContinuationKey());
		migrated.commit(firstR4, published(1, 1,
				new PublishedRoot(bound, initialProof,
						IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED)));

		String migratedState = migrated.exportState();
		assertTrue(migratedState.startsWith("4|"));
		LocusIntersectionTokenLedger2D reopened =
				new LocusIntersectionTokenLedger2D();
		reopened.importState(migratedState);
		LocusIntersectionTokenLedger2D.Evaluation moved = begin(reopened);
		IntersectionRootAllocation2D resolved = moved.resolveCurrentRoot(BRANCH,
				CONTINUATION_CONTRACT, NEGATIVE,
				proof("provider/r4", 0.75));
		assertTrue(resolved.isReused());
		assertEquals(exactR3Token, resolved.getRootToken());
		reopened.abort(moved);
	}

	@Test
	void r3SingletonMigrationRequiresTheExactInitialAddress() {
		LocusIntersectionTokenLedger2D source =
				new LocusIntersectionTokenLedger2D();
		String legacyKey = "g9u0/g8c1-explicit-unique-local-root/v1/"
				+ BRANCH.length() + ":" + BRANCH;
		IntersectionTokenLineage2D lineage = new IntersectionTokenLineage2D(
				BRANCH + "/solution/" + legacyKey, BRANCH,
				Optional.of(legacyKey));
		IntersectionRootAddressProof2D proof = proof("provider/r3", 0.25);
		LocusIntersectionTokenLedger2D.Evaluation r3 = begin(source);
		String exactR3Token = r3.mint(lineage, proof);
		source.commit(r3, published(1, 1,
				new PublishedRoot(exactR3Token, legacyKey, proof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION)));

		LocusIntersectionTokenLedger2D migrated =
				new LocusIntersectionTokenLedger2D();
		migrated.importState(toLegacyV1State(source.exportState()));
		LocusIntersectionTokenLedger2D.Evaluation evaluation = begin(migrated);
		IntersectionRootAllocation2D replacement = evaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE,
				proof("provider/r3", 0.5), true);
		assertFalse(replacement.isReused());
		assertNotEquals(exactR3Token, replacement.getRootToken());
		migrated.abort(evaluation);
	}

	@Test
	void deterministicSelectorRejectsAGermFromAnotherComponent() {
		String indicator = framed("r4-target-contact-indicator") + "negative";
		String foreignGerm = "g9u0-r4/current-transverse-root-germ/v1/"
				+ framed("foreign-component") + framed(indicator);
		assertThrows(IllegalArgumentException.class,
				() -> IntersectionRootDeterministicSelector2D.of(BRANCH,
						foreignGerm));
		String overflowingGerm =
				"g9u0-r4/current-transverse-root-germ/v1/2147483647:x";
		assertThrows(IllegalArgumentException.class,
				() -> IntersectionRootDeterministicSelector2D.of(BRANCH,
						overflowingGerm));
	}

	@Test
	void intrinsicPhaseSelectorRoundTripPreservesSemanticFrame() {
		IntersectionRootDeterministicSelector2D selector = phaseSelector(
				POSITIVE, Orientation.DECREASING, true, 4, 2);
		IntersectionRootDeterministicSelector2D restored =
				IntersectionRootDeterministicSelector2D.parse(
						selector.toExternalForm());

		assertEquals(selector, restored);
		assertTrue(restored.hasIntrinsicPhase());
		assertEquals(OptionalInt.of(2), restored.getIntrinsicPhaseRank());
		assertEquals(OptionalInt.of(4), restored.getCollisionCardinality());
		assertEquals(Optional.of(Orientation.DECREASING),
				restored.getPhaseOrientation());
		assertTrue(restored.isPeriodicPhase());
		assertEquals(POSITIVE.getComponentLineage(),
				restored.getComponentLineage());
		assertEquals(POSITIVE.getCurrentRootGerm(),
				restored.getCurrentRootGerm());
		assertThrows(IllegalArgumentException.class,
				() -> phaseSelector(POSITIVE, Orientation.INCREASING,
						false, 2, 2));
	}

	@Test
	void phaseSelectorLedgerRoundTripRetainsExactOpaqueTokens() {
		IntersectionRootDeterministicSelector2D firstSelector = phaseSelector(
				POSITIVE, Orientation.INCREASING, false, 2, 0);
		IntersectionRootDeterministicSelector2D secondSelector = phaseSelector(
				POSITIVE, Orientation.INCREASING, false, 2, 1);
		LocusIntersectionTokenLedger2D ledger =
				new LocusIntersectionTokenLedger2D();
		LocusIntersectionTokenLedger2D.Evaluation firstEvaluation = begin(ledger);
		IntersectionRootAddressProof2D firstProof = proof("provider/r1", -1);
		IntersectionRootAddressProof2D secondProof = proof("provider/r1", 1);
		IntersectionRootAllocation2D first = firstEvaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, firstSelector, firstProof);
		IntersectionRootAllocation2D second = firstEvaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, secondSelector, secondProof);
		ledger.commit(firstEvaluation, published(1, 1,
				new PublishedRoot(first, firstProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION),
				new PublishedRoot(second, secondProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION)));

		String encoded = ledger.exportState();
		LocusIntersectionTokenLedger2D restored =
				new LocusIntersectionTokenLedger2D();
		restored.importState(encoded);
		assertEquals(encoded, restored.exportState());
		LocusIntersectionTokenLedger2D.Evaluation next = begin(restored);
		IntersectionRootAllocation2D resumedFirst = next.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, firstSelector,
				proof("provider/r2", -0.9));
		IntersectionRootAllocation2D resumedSecond = next.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, secondSelector,
				proof("provider/r2", 0.9));
		assertTrue(resumedFirst.isReused());
		assertTrue(resumedSecond.isReused());
		assertEquals(first.getRootToken(), resumedFirst.getRootToken());
		assertEquals(second.getRootToken(), resumedSecond.getRootToken());
		restored.abort(next);

		LocusIntersectionTokenLedger2D migrated =
				new LocusIntersectionTokenLedger2D();
		migrated.importState(HISTORICAL_LEDGER_V2);
		assertEquals("4" + HISTORICAL_LEDGER_V2.substring(1),
				migrated.exportState());
		LocusIntersectionTokenLedger2D.Evaluation migratedEvaluation =
				begin(migrated);
		IntersectionRootAllocation2D migratedAllocation =
				migratedEvaluation.resolveCurrentRoot(BRANCH,
						CONTINUATION_CONTRACT, NEGATIVE,
						proof("provider/r2", 0.5));
		assertTrue(migratedAllocation.isReused());
		assertEquals(HISTORICAL_LEDGER_V2_TOKEN,
				migratedAllocation.getRootToken());
		migrated.abort(migratedEvaluation);

		String activeV4 = ledger.exportState();
		String historicalV3 = "3" + activeV4.substring(1);
		LocusIntersectionTokenLedger2D migratedV3 =
				new LocusIntersectionTokenLedger2D();
		migratedV3.importState(historicalV3);
		assertEquals(activeV4, migratedV3.exportState());
		assertTrue(migratedV3.validatesCurrentToken(first.getRootToken()));
		assertTrue(migratedV3.retainMaterializedToken(first.getRootToken()));
		String claimedV4 = migratedV3.exportState();
		String falselyRelabeledClaimedV3 = "3" + claimedV4.substring(1);
		assertThrows(IllegalArgumentException.class,
				() -> new LocusIntersectionTokenLedger2D()
						.importState(falselyRelabeledClaimedV3));

		String falselyRelabeledPhaseState = "2|" + encoded.substring(2);
		assertThrows(IllegalArgumentException.class,
				() -> new LocusIntersectionTokenLedger2D()
						.importState(falselyRelabeledPhaseState));

		String legacyKey = "g9u0/g8c1-explicit-unique-local-root/v1/"
				+ BRANCH.length() + ":" + BRANCH;
		IntersectionTokenLineage2D legacyLineage = new IntersectionTokenLineage2D(
				BRANCH + "/solution/" + legacyKey, BRANCH,
				Optional.of(legacyKey));
		IntersectionRootAddressProof2D legacyProof = proof("provider/r3", 0.25);
		IntersectionRootDeterministicSelector2D legacyPhaseSelector = phaseSelector(
				NEGATIVE, Orientation.INCREASING, false, 2, 0);
		LocusIntersectionTokenLedger2D legacySource =
				new LocusIntersectionTokenLedger2D();
		LocusIntersectionTokenLedger2D.Evaluation legacyEvaluation =
				begin(legacySource);
		String exactLegacyToken = legacyEvaluation.mint(legacyLineage, legacyProof);
		legacySource.commit(legacyEvaluation, published(1, 1,
				new PublishedRoot(exactLegacyToken, legacyKey, legacyProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION)));
		String legacyState = toLegacyV1State(legacySource.exportState());

		LocusIntersectionTokenLedger2D phaseAttempt =
				new LocusIntersectionTokenLedger2D();
		phaseAttempt.importState(legacyState);
		LocusIntersectionTokenLedger2D.Evaluation phaseEvaluation =
				begin(phaseAttempt);
		IntersectionRootAllocation2D phaseAllocation =
				phaseEvaluation.resolveCurrentRoot(BRANCH, CONTINUATION_CONTRACT,
						legacyPhaseSelector, legacyProof, true);
		assertFalse(phaseAllocation.isReused());
		assertNotEquals(exactLegacyToken, phaseAllocation.getRootToken());
		phaseAttempt.abort(phaseEvaluation);

		LocusIntersectionTokenLedger2D baseMigration =
				new LocusIntersectionTokenLedger2D();
		baseMigration.importState(legacyState);
		LocusIntersectionTokenLedger2D.Evaluation baseEvaluation =
				begin(baseMigration);
		IntersectionRootAllocation2D baseAllocation =
				baseEvaluation.resolveCurrentRoot(BRANCH, CONTINUATION_CONTRACT,
						NEGATIVE, legacyProof, true);
		assertTrue(baseAllocation.isReused());
		assertEquals(exactLegacyToken, baseAllocation.getRootToken());
		baseMigration.commit(baseEvaluation, published(1, 1,
				new PublishedRoot(baseAllocation, legacyProof,
						IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED)));
		String falselyBoundLegacyPhaseState = mutateOnlyCurrentEntry(
				baseMigration.exportState(), entry -> {
					entry[9] = hex(legacyPhaseSelector.toExternalForm());
					return entry;
				});
		assertThrows(IllegalArgumentException.class,
				() -> new LocusIntersectionTokenLedger2D()
						.importState(falselyBoundLegacyPhaseState));
	}

	@Test
	void periodicQuarantineSurvivesRecomputeReopenAndCopyUntilProvedRelease() {
		IntersectionRootDeterministicSelector2D firstSelector = phaseSelector(
				POSITIVE, Orientation.INCREASING, true, 2, 0);
		IntersectionRootDeterministicSelector2D secondSelector = phaseSelector(
				POSITIVE, Orientation.INCREASING, true, 2, 1);
		IntersectionRootDeterministicSelector2D foreignFirstSelector = phaseSelector(
				NEGATIVE, Orientation.INCREASING, true, 2, 0);
		IntersectionRootDeterministicSelector2D foreignSecondSelector = phaseSelector(
				NEGATIVE, Orientation.INCREASING, true, 2, 1);
		IntersectionRootAddressProof2D firstProof = proof("provider/r1", 1);
		IntersectionRootAddressProof2D secondProof = proof("provider/r1", 6);
		IntersectionRootAddressProof2D foreignFirstProof = proof("provider/r1", 2);
		IntersectionRootAddressProof2D foreignSecondProof = proof("provider/r1", 8);
		LocusIntersectionTokenLedger2D ledger =
				new LocusIntersectionTokenLedger2D();
		LocusIntersectionTokenLedger2D.Evaluation initial = begin(ledger);
		IntersectionRootAllocation2D first = initial.resolveCurrentRoot(BRANCH,
				CONTINUATION_CONTRACT, firstSelector, firstProof);
		IntersectionRootAllocation2D second = initial.resolveCurrentRoot(BRANCH,
				CONTINUATION_CONTRACT, secondSelector, secondProof);
		IntersectionRootAllocation2D foreignFirst = initial.resolveCurrentRoot(BRANCH,
				CONTINUATION_CONTRACT, foreignFirstSelector, foreignFirstProof);
		IntersectionRootAllocation2D foreignSecond = initial.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, foreignSecondSelector,
				foreignSecondProof);
		ledger.commit(initial, published(1, 1,
				new PublishedRoot(first, firstProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION),
				new PublishedRoot(second, secondProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION),
				new PublishedRoot(foreignFirst, foreignFirstProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION),
				new PublishedRoot(foreignSecond, foreignSecondProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION)));
		assertTrue(ledger.retainMaterializedToken(first.getRootToken()));

		LocusIntersectionTokenLedger2D.Evaluation barrier = begin(ledger);
		List<LocusIntersectionTokenLedger2D.PersistedPhaseAllocation> evidence =
				barrier.persistedPhaseAllocations(CONTINUATION_CONTRACT, POSITIVE);
		assertEquals(2, evidence.size());
		List<String> groupTokens = evidence.stream()
				.map(LocusIntersectionTokenLedger2D.PersistedPhaseAllocation::getToken)
				.sorted().toList();
		List<String> foreignGroupTokens = barrier.persistedPhaseAllocations(
				CONTINUATION_CONTRACT, NEGATIVE).stream()
				.map(LocusIntersectionTokenLedger2D.PersistedPhaseAllocation::getToken)
				.sorted().toList();
		assertEquals(2, foreignGroupTokens.size());
		assertThrows(IllegalArgumentException.class,
				() -> barrier.quarantinePersistedPeriodicTokens(
						List.of(groupTokens.get(0))));
		assertThrows(IllegalArgumentException.class,
				() -> barrier.quarantinePersistedPeriodicTokens(
						List.of(groupTokens.get(0), groupTokens.get(0))));
		assertThrows(IllegalArgumentException.class,
				() -> barrier.quarantinePersistedPeriodicTokens(
						List.of(groupTokens.get(0), foreignGroupTokens.get(0))));
		barrier.quarantinePersistedPeriodicTokens(groupTokens);
		ledger.commit(barrier, publishedEmpty(2, 2));
		String quarantinedState = ledger.exportState();
		assertEquals(1, statusCount(quarantinedState, "r"));
		assertEquals(1, statusCount(quarantinedState, "q"));
		assertFalse(ledger.validatesCurrentToken(first.getRootToken()));
		assertTrue(ledger.validatesRetainedToken(first.getRootToken()));
		assertTrue(ledger.validatesRetainedToken(second.getRootToken()));

		LocusIntersectionTokenLedger2D restored =
				new LocusIntersectionTokenLedger2D();
		restored.importState(quarantinedState);
		assertEquals(quarantinedState, restored.exportState());
		LocusIntersectionTokenLedger2D.Evaluation stillBlocked = begin(restored);
		List<LocusIntersectionTokenLedger2D.PersistedPhaseAllocation>
				restoredEvidence = stillBlocked.persistedPhaseAllocations(
						CONTINUATION_CONTRACT, POSITIVE);
		assertEquals(List.of(1D, 6D), restoredEvidence.stream()
				.map(LocusIntersectionTokenLedger2D.PersistedPhaseAllocation
						::getCanonicalParameter).sorted().toList());
		assertTrue(restoredEvidence.stream().allMatch(
				LocusIntersectionTokenLedger2D.PersistedPhaseAllocation
						::isPeriodicallyQuarantined));
		assertThrows(IllegalStateException.class,
				() -> stillBlocked.resolveCurrentRoot(BRANCH,
						CONTINUATION_CONTRACT, firstSelector,
						proof("provider/r2", 2)));
		restored.abort(stillBlocked);

		LocusIntersectionTokenLedger2D.Evaluation provedZero = begin(restored);
		assertThrows(IllegalArgumentException.class,
				() -> provedZero.releasePersistedPeriodicQuarantine(
						List.of(groupTokens.get(0))));
		assertThrows(IllegalArgumentException.class,
				() -> provedZero.releasePersistedPeriodicQuarantine(
						List.of(groupTokens.get(0), groupTokens.get(0))));
		provedZero.releasePersistedPeriodicQuarantine(groupTokens);
		IntersectionRootAddressProof2D movedFirst = proof("provider/r2", 2);
		IntersectionRootAddressProof2D movedSecond = proof("provider/r2", 7);
		IntersectionRootAllocation2D resumedFirst =
				provedZero.resolveCurrentRoot(BRANCH, CONTINUATION_CONTRACT,
						firstSelector, movedFirst);
		IntersectionRootAllocation2D resumedSecond =
				provedZero.resolveCurrentRoot(BRANCH, CONTINUATION_CONTRACT,
						secondSelector, movedSecond);
		assertEquals(first.getRootToken(), resumedFirst.getRootToken());
		assertEquals(second.getRootToken(), resumedSecond.getRootToken());
		restored.commit(provedZero, published(3, 3,
				new PublishedRoot(resumedFirst, movedFirst,
						IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED),
				new PublishedRoot(resumedSecond, movedSecond,
						IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED)));
		assertEquals(1, statusCount(restored.exportState(), "c"));
		assertEquals(1, statusCount(restored.exportState(), "a"));
		assertEquals(0, statusCount(restored.exportState(), "q"));
		assertEquals(0, statusCount(restored.exportState(), "r"));

		LocusIntersectionTokenLedger2D copied =
				new LocusIntersectionTokenLedger2D();
		copied.importState(quarantinedState);
		copied.authorizeImmediateCopy(OWNER);
		String copyOwner = "r4-periodic-quarantine-copy-owner";
		String copyPair = "r4-periodic-quarantine-copy-pair";
		String copyContract = "r4-periodic-quarantine-copy-contract";
		LocusIntersectionTokenLedger2D.Evaluation copy = copied.begin(copyOwner,
				copyPair, CONSTRUCTIVE, TOPOLOGY);
		List<LocusIntersectionTokenLedger2D.PersistedPhaseAllocation>
				copyEvidence = copy.persistedPhaseAllocations(copyContract, POSITIVE);
		List<String> copySourceTokens = copyEvidence.stream()
				.map(LocusIntersectionTokenLedger2D.PersistedPhaseAllocation::getToken)
				.sorted().toList();
		assertThrows(IllegalArgumentException.class,
				() -> copy.rebasePersistedPeriodicQuarantineForCopy(
						List.of(copySourceTokens.get(0)), copyContract));
		assertThrows(IllegalArgumentException.class,
				() -> copy.rebasePersistedPeriodicQuarantineForCopy(
						List.of(copySourceTokens.get(0), copySourceTokens.get(0)),
						copyContract));
		copy.rebasePersistedPeriodicQuarantineForCopy(copySourceTokens,
				copyContract);
		copy.quarantinePersistedPeriodicTokens(copySourceTokens);
		assertThrows(IllegalStateException.class,
				() -> copy.releasePersistedPeriodicQuarantine(copySourceTokens));
		assertThrows(IllegalStateException.class,
				() -> copy.resolveCurrentRoot(BRANCH, copyContract, firstSelector,
						proof("provider/copied", 1)));
		copied.commit(copy, publishedEmptyForPair(copyPair, 4, 4));
		String copiedToken = copied.rebaseCopiedRetainedToken(first.getRootToken(),
				copyOwner, OWNER).orElseThrow();
		assertNotEquals(first.getRootToken(), copiedToken);
		assertTrue(copied.validatesRetainedToken(copiedToken));
		assertFalse(copied.validatesCurrentToken(copiedToken));
		assertEquals(1, statusCount(copied.exportState(), "r"));
		assertEquals(1, statusCount(copied.exportState(), "q"));
		LocusIntersectionTokenLedger2D.Evaluation copiedRecompute = copied.begin(
				copyOwner, copyPair, CONSTRUCTIVE, TOPOLOGY);
		List<LocusIntersectionTokenLedger2D.PersistedPhaseAllocation>
				copiedEvidence = copiedRecompute.persistedPhaseAllocations(
						copyContract, POSITIVE);
		assertEquals(2, copiedEvidence.size());
		assertTrue(copiedEvidence.stream().allMatch(
				LocusIntersectionTokenLedger2D.PersistedPhaseAllocation
						::isPeriodicallyQuarantined));
		assertThrows(IllegalStateException.class,
				() -> copiedRecompute.resolveCurrentRoot(BRANCH, copyContract,
						firstSelector, proof("provider/copied", 1)));
		copied.abort(copiedRecompute);
		String copiedQuarantineState = copied.exportState();
		LocusIntersectionTokenLedger2D reopenedCopy =
				new LocusIntersectionTokenLedger2D();
		reopenedCopy.importState(copiedQuarantineState);
		LocusIntersectionTokenLedger2D.Evaluation reopenedRecompute =
				reopenedCopy.begin(copyOwner, copyPair, CONSTRUCTIVE, TOPOLOGY);
		assertEquals(2, reopenedRecompute.persistedPhaseAllocations(copyContract,
				POSITIVE).size());
		assertThrows(IllegalStateException.class,
				() -> reopenedRecompute.resolveCurrentRoot(BRANCH, copyContract,
						firstSelector, proof("provider/copied", 1)));
		reopenedCopy.abort(reopenedRecompute);

		LocusIntersectionTokenLedger2D prunable =
				new LocusIntersectionTokenLedger2D();
		prunable.importState(quarantinedState);
		assertTrue(prunable.retainMaterializedToken(first.getRootToken()));
		prunable.releaseMaterializedToken(first.getRootToken());
		assertFalse(prunable.validatesRetainedToken(first.getRootToken()));
		assertFalse(prunable.validatesRetainedToken(second.getRootToken()));

		assertEquals(OptionalInt.of(0), PublicIntersectionRootIdentityResolver2D
				.uniquePeriodicPhaseOffset(List.of(1D, 6D), List.of(2D, 7D),
						10, 1E-9));
		assertEquals(OptionalInt.of(1), PublicIntersectionRootIdentityResolver2D
				.uniquePeriodicPhaseOffset(List.of(1D, 6D), List.of(4D, 9D),
						10, 1E-9));
		assertTrue(PublicIntersectionRootIdentityResolver2D
				.uniquePeriodicPhaseOffset(List.of(1D, 6D),
						List.of(3.5D, 8.5D), 10, 1E-9).isEmpty());
	}

	@Test
	void changedVerifiedCollisionCardinalityBurnsAllRankedBindings() {
		IntersectionRootDeterministicSelector2D oldFirst = phaseSelector(POSITIVE,
				Orientation.INCREASING, false, 2, 0);
		IntersectionRootDeterministicSelector2D oldSecond = phaseSelector(POSITIVE,
				Orientation.INCREASING, false, 2, 1);
		LocusIntersectionTokenLedger2D ledger =
				new LocusIntersectionTokenLedger2D();
		LocusIntersectionTokenLedger2D.Evaluation initial = begin(ledger);
		IntersectionRootAddressProof2D firstProof = proof("provider/r1", -1);
		IntersectionRootAddressProof2D secondProof = proof("provider/r1", 1);
		IntersectionRootAllocation2D first = initial.resolveCurrentRoot(BRANCH,
				CONTINUATION_CONTRACT, oldFirst, firstProof);
		IntersectionRootAllocation2D second = initial.resolveCurrentRoot(BRANCH,
				CONTINUATION_CONTRACT, oldSecond, secondProof);
		ledger.commit(initial, published(1, 1,
				new PublishedRoot(first, firstProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION),
				new PublishedRoot(second, secondProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION)));

		LocusIntersectionTokenLedger2D.Evaluation changed = begin(ledger);
		PublishedRoot[] replacements = new PublishedRoot[3];
		for (int rank = 0; rank < 3; rank++) {
			IntersectionRootAddressProof2D replacementProof =
					proof("provider/r2", rank - 1);
			IntersectionRootAllocation2D replacement = changed.resolveCurrentRoot(
					BRANCH, CONTINUATION_CONTRACT,
					phaseSelector(POSITIVE, Orientation.INCREASING,
							false, 3, rank),
					replacementProof);
			assertFalse(replacement.isReused());
			assertNotEquals(first.getRootToken(), replacement.getRootToken());
			assertNotEquals(second.getRootToken(), replacement.getRootToken());
			replacements[rank] = new PublishedRoot(replacement,
					replacementProof, IdentityStatus.NEW_TOPOLOGICAL_SOLUTION);
		}
		ledger.commit(changed, published(2, 2, replacements));
		assertFalse(ledger.validatesCurrentToken(first.getRootToken()));
		assertFalse(ledger.validatesCurrentToken(second.getRootToken()));

		LocusIntersectionTokenLedger2D.Evaluation restoredCardinality =
				begin(ledger);
		IntersectionRootAddressProof2D restoredFirstProof =
				proof("provider/r3", -0.8);
		IntersectionRootAddressProof2D restoredSecondProof =
				proof("provider/r3", 0.8);
		IntersectionRootAllocation2D restoredFirst =
				restoredCardinality.resolveCurrentRoot(BRANCH,
						CONTINUATION_CONTRACT, oldFirst, restoredFirstProof);
		IntersectionRootAllocation2D restoredSecond =
				restoredCardinality.resolveCurrentRoot(BRANCH,
						CONTINUATION_CONTRACT, oldSecond, restoredSecondProof);
		assertFalse(restoredFirst.isReused());
		assertFalse(restoredSecond.isReused());
		assertNotEquals(first.getRootToken(), restoredFirst.getRootToken());
		assertNotEquals(second.getRootToken(), restoredSecond.getRootToken());
		ledger.commit(restoredCardinality, published(3, 3,
				new PublishedRoot(restoredFirst, restoredFirstProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION),
				new PublishedRoot(restoredSecond, restoredSecondProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION)));
		assertFalse(ledger.validatesCurrentToken(first.getRootToken()));
		assertFalse(ledger.validatesCurrentToken(second.getRootToken()));
	}

	@Test
	void orientationReversalCannotReuseRankedAllocationsWithoutDeclaredMap() {
		IntersectionRootDeterministicSelector2D increasing = phaseSelector(
				POSITIVE, Orientation.INCREASING, false, 2, 0);
		IntersectionRootDeterministicSelector2D decreasing = phaseSelector(
				POSITIVE, Orientation.DECREASING, false, 2, 1);
		LocusIntersectionTokenLedger2D ledger =
				new LocusIntersectionTokenLedger2D();
		LocusIntersectionTokenLedger2D.Evaluation initial = begin(ledger);
		IntersectionRootAddressProof2D initialProof =
				proof("provider/increasing", -1);
		IntersectionRootAllocation2D first = initial.resolveCurrentRoot(BRANCH,
				CONTINUATION_CONTRACT, increasing, initialProof);
		ledger.commit(initial, published(1, 1,
				new PublishedRoot(first, initialProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION)));

		LocusIntersectionTokenLedger2D.Evaluation reversed = begin(ledger);
		IntersectionRootAllocation2D replacement = reversed.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, decreasing,
				proof("provider/decreasing", -1));
		assertFalse(replacement.isReused());
		assertNotEquals(first.getRootToken(), replacement.getRootToken());
		ledger.abort(reversed);
	}

	@Test
	void publishedContinuationKeyExposesItsExactVersionedSelector() {
		IntersectionRootDeterministicSelector2D selector = phaseSelector(POSITIVE,
				Orientation.INCREASING, true, 4, 3);
		LocusIntersectionTokenLedger2D ledger =
				new LocusIntersectionTokenLedger2D();
		LocusIntersectionTokenLedger2D.Evaluation evaluation = begin(ledger);
		IntersectionRootAllocation2D allocation = evaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, selector,
				proof("provider/r1", 2));

		assertEquals(Optional.of(selector), LocusIntersectionTokenLedger2D
				.selectorFromContinuationKey(allocation.getContinuationKey()));
		assertTrue(LocusIntersectionTokenLedger2D
				.selectorFromContinuationKey("legacy-key").isEmpty());
		ledger.abort(evaluation);
	}

	@Test
	void persistedAllocationRejectsNoncanonicalIncarnationSuffix() {
		String state = committedSingleAllocation().exportState();
		String tampered = mutateOnlyCurrentEntry(state, entry -> {
			String continuation = unhex(entry[4]);
			entry[4] = hex(continuation.substring(0,
					continuation.length() - 1) + "01");
			return entry;
		});
		assertThrows(IllegalArgumentException.class,
				() -> new LocusIntersectionTokenLedger2D().importState(tampered));
	}

	@Test
	void persistedBindingRejectsForgedLegacyTokenMaterial() {
		String state = committedSingleAllocation().exportState();
		String tampered = mutateOnlyCurrentEntry(state, entry -> {
			String forged = "arbitrary-legacy-key";
			entry[2] = hex(BRANCH + "/solution/" + forged);
			entry[4] = hex(forged);
			return entry;
		});
		assertThrows(IllegalArgumentException.class,
				() -> new LocusIntersectionTokenLedger2D().importState(tampered));
	}

	@Test
	void persistedSnapshotRejectsDuplicateDeterministicBinding() {
		String state = committedSingleAllocation().exportState();
		String[] fields = state.split("\\|", -1);
		String[] snapshot = fields[2].split("~", -1);
		String[] duplicate = snapshot[5].split(",", -1);
		String oldKey = unhex(duplicate[4]);
		String newKey = oldKey.substring(0, oldKey.length() - 1) + "2";
		duplicate[1] = "2";
		duplicate[2] = hex(BRANCH + "/solution/" + newKey);
		duplicate[4] = hex(newKey);
		fields[1] = "3";
		snapshot[4] = "2";
		fields[2] = String.join("~", snapshot) + "~"
				+ String.join(",", duplicate);
		String tampered = String.join("|", fields);
		assertThrows(IllegalArgumentException.class,
				() -> new LocusIntersectionTokenLedger2D().importState(tampered));
	}

	@Test
	void deterministicSelectorResolvesMovedAddressWithoutHistory() {
		LocusIntersectionTokenLedger2D ledger = committedSingleAllocation();
		String prior = currentToken(ledger, "provider/r1", 0.25);
		LocusIntersectionTokenLedger2D.Evaluation evaluation = begin(ledger);
		IntersectionRootAddressProof2D moved = proof("provider/r2", 1.75);
		IntersectionRootAllocation2D continued = evaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE, moved);

		assertTrue(continued.isReused());
		assertEquals(prior, continued.getRootToken());
		ledger.commit(evaluation, published(2, 2,
				new PublishedRoot(continued, moved,
						IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED)));
		assertTrue(ledger.validatesCurrentToken(prior));
	}

	@Test
	void multiRootCopyUsesOneSemanticMappingPerSharedLegacyIncarnation() {
		LocusIntersectionTokenLedger2D ledger =
				new LocusIntersectionTokenLedger2D();
		IntersectionTokenLineage2D firstLineage = new IntersectionTokenLineage2D(
				"legacy-first", BRANCH, Optional.of("legacy-first-key"));
		IntersectionTokenLineage2D secondLineage = new IntersectionTokenLineage2D(
				"legacy-second", BRANCH, Optional.of("legacy-second-key"));
		IntersectionRootAddressProof2D firstProof = proof("provider/source", 0.25);
		IntersectionRootAddressProof2D secondProof = proof("provider/source", 0.75);
		LocusIntersectionTokenLedger2D.Evaluation source = begin(ledger);
		String first = source.mint(firstLineage, firstProof);
		String second = source.mint(secondLineage, secondProof);
		assertEquals(LocusSemanticIntersectionToken2D.decode(first).orElseThrow()
				.getIncarnation(), LocusSemanticIntersectionToken2D.decode(second)
						.orElseThrow().getIncarnation());
		ledger.commit(source, published(1, 1,
				new PublishedRoot(first, "legacy-first-key", firstProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION),
				new PublishedRoot(second, "legacy-second-key", secondProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION)));

		ledger.authorizeImmediateCopy(OWNER);
		String copyOwner = "r4-copy-owner";
		String copyPair = "r4-copy-source-pair";
		LocusIntersectionTokenLedger2D.Evaluation copy = ledger.begin(copyOwner,
				copyPair, CONSTRUCTIVE, TOPOLOGY);
		IntersectionRootAddressProof2D copiedFirstProof =
				proof("provider/copied", 0.25);
		IntersectionRootAddressProof2D copiedSecondProof =
				proof("provider/copied", 0.75);
		String copiedFirst = copy.mint(firstLineage, copiedFirstProof);
		String copiedSecond = copy.mint(secondLineage, copiedSecondProof);
		LocusIntersectionResult2D copiedPublication = publishedForPair(copyPair,
				2, 2,
				new PublishedRoot(copiedFirst, "legacy-first-key",
						copiedFirstProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION),
				new PublishedRoot(copiedSecond, "legacy-second-key",
						copiedSecondProof,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION));
		ledger.commit(copy, copiedPublication);
		ledger.validatePreattachmentContext(copyOwner, copyPair, CONSTRUCTIVE,
				TOPOLOGY, OWNER, SOURCE_PAIR, false);
		assertEquals(copiedFirst, ledger.rebaseCopiedToken(first, copyOwner,
				OWNER, copiedPublication).orElseThrow());
		assertEquals(copiedSecond, ledger.rebaseCopiedToken(second, copyOwner,
				OWNER, copiedPublication).orElseThrow());
	}

	private static LocusIntersectionTokenLedger2D committedSingleAllocation() {
		LocusIntersectionTokenLedger2D ledger =
				new LocusIntersectionTokenLedger2D();
		LocusIntersectionTokenLedger2D.Evaluation evaluation = begin(ledger);
		IntersectionRootAddressProof2D address = proof("provider/r1", 0.25);
		IntersectionRootAllocation2D allocation = evaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE, address);
		ledger.commit(evaluation, published(1, 1,
				new PublishedRoot(allocation, address,
						IdentityStatus.NEW_TOPOLOGICAL_SOLUTION)));
		return ledger;
	}

	private static String currentToken(LocusIntersectionTokenLedger2D ledger,
			String provider, double parameter) {
		LocusIntersectionTokenLedger2D.Evaluation evaluation = begin(ledger);
		IntersectionRootAllocation2D allocation = evaluation.resolveCurrentRoot(
				BRANCH, CONTINUATION_CONTRACT, NEGATIVE,
				proof(provider, parameter));
		ledger.abort(evaluation);
		return allocation.getRootToken();
	}

	private static LocusIntersectionTokenLedger2D.Evaluation begin(
			LocusIntersectionTokenLedger2D ledger) {
		return ledger.begin(OWNER, SOURCE_PAIR, CONSTRUCTIVE, TOPOLOGY);
	}

	private static IntersectionRootAddressProof2D proof(String provider,
			double parameter) {
		return new IntersectionRootAddressProof2D(provider, TARGET_CONTRACT,
				parameter);
	}

	private static IntersectionRootDeterministicSelector2D selector(
			String orientation) {
		String indicator = framed("r4-target-contact-indicator") + orientation;
		return IntersectionRootDeterministicSelector2D.of(BRANCH,
				"g9u0-r4/current-transverse-root-germ/v1/"
						+ framed(BRANCH) + framed(indicator));
	}

	private static IntersectionRootDeterministicSelector2D phaseSelector(
			IntersectionRootDeterministicSelector2D base,
			Orientation orientation, boolean periodic, int cardinality, int rank) {
		return IntersectionRootDeterministicSelector2D.ofIntrinsicPhase(
				base.getComponentLineage(), base.getCurrentRootGerm(), orientation,
				periodic, cardinality, rank);
	}

	private static String framed(String value) {
		return value.length() + ":" + value;
	}

	private static LocusIntersectionResult2D published(long locusRevision,
			long targetUpdateStamp, PublishedRoot... roots) {
		return publishedForPair(SOURCE_PAIR, locusRevision, targetUpdateStamp,
				roots);
	}

	private static LocusIntersectionResult2D publishedEmpty(long locusRevision,
			long targetUpdateStamp) {
		return publishedEmptyForPair(SOURCE_PAIR, locusRevision,
				targetUpdateStamp);
	}

	private static LocusIntersectionResult2D publishedEmptyForPair(
			String sourcePair, long locusRevision, long targetUpdateStamp) {
		LocusIntersectionPolicy2D policy = LocusIntersectionPolicy2D.initial(
				"r4-provider", "u");
		LocusIntersectionQuery2D query = new LocusIntersectionQuery2D(
				sourcePair, CONSTRUCTIVE, "r4-locus", locusRevision,
				"r4-target", targetUpdateStamp, TOPOLOGY, policy);
		return new LocusIntersectionResult2D(
				new IntersectionSourceBinding2D(query, TargetFamily.LINE),
				ComputationStatus.SUCCESS,
				new IntersectionCompletenessEvidence2D(Completeness.COMPLETE,
						CompletenessMethod.CERTIFIED_DOMAIN_EXCLUSION, 0,
						List.of(COMPONENT), List.of()),
				GeometryKind.EMPTY, Currentness.CURRENT,
				SupportLevel.EXACT_CAPABILITY,
				NumericGuarantee.CERTIFIED_ERROR_BOUND, List.of(), List.of(),
				new LocusIntersectionInstrumentation2D(
						policy.getWorkBudget()).snapshot(), List.of());
	}

	private static LocusIntersectionResult2D publishedForPair(String sourcePair,
			long locusRevision, long targetUpdateStamp, PublishedRoot... roots) {
		LocusIntersectionPolicy2D policy = LocusIntersectionPolicy2D.initial(
				"r4-provider", "u");
		LocusIntersectionQuery2D query = new LocusIntersectionQuery2D(
				sourcePair, CONSTRUCTIVE, "r4-locus", locusRevision,
				"r4-target", targetUpdateStamp, TOPOLOGY, policy);
		List<LocusIntersectionSolution2D> solutions = java.util.Arrays.stream(roots)
				.map(root -> solution(root, sourcePair, locusRevision,
						targetUpdateStamp))
				.toList();
		return new LocusIntersectionResult2D(
				new IntersectionSourceBinding2D(query, TargetFamily.LINE),
				ComputationStatus.SUCCESS,
				new IntersectionCompletenessEvidence2D(
						Completeness.NOT_ESTABLISHED,
						CompletenessMethod.NOT_ESTABLISHED, solutions.size(),
						List.of(COMPONENT), List.of()),
				GeometryKind.FINITE, Currentness.CURRENT,
				SupportLevel.EXACT_CAPABILITY,
				NumericGuarantee.CERTIFIED_ERROR_BOUND, solutions, List.of(),
				new LocusIntersectionInstrumentation2D(
						policy.getWorkBudget()).snapshot(), List.of());
	}

	private static LocusIntersectionSolution2D solution(PublishedRoot root,
			String sourcePair, long locusRevision, long targetUpdateStamp) {
		double parameter = Double.longBitsToDouble(
				root.addressProof().getCanonicalParameterBits());
		IntersectionResidualContract2D residualContract =
				new IntersectionResidualContract2D(TARGET_CONTRACT,
						ResidualQuantityKind.MODEL_COORDINATE_DISTANCE,
						"model-coordinate", "test-normalization",
						"max(1,target-characteristic-length)");
		IntersectionRootIdentity2D identity = new IntersectionRootIdentity2D(
				root.token(), sourcePair, CONSTRUCTIVE, BRANCH, TOPOLOGY,
				Optional.of(root.continuationKey()), root.status());
		IntersectionRootRevisionEvidence2D evidence =
				new IntersectionRootRevisionEvidence2D(locusRevision,
						targetUpdateStamp, BRANCH, COMPONENT, parameter,
						OptionalDouble.empty(),
						new IntersectionParameterInterval2D(parameter - 1E-6,
								parameter + 1E-6),
						LocalIsolationStatus.ESTABLISHED,
						new TargetResidual2D(0, 1, 0, 1, residualContract),
						SolverMethod.CERTIFIED_INTERVAL,
						NumericGuarantee.CERTIFIED_ERROR_BOUND,
						Optional.empty());
		IntersectionClassification2D classification =
				new IntersectionClassification2D(
						ContactClass.TRANSVERSE_ESTABLISHED,
						MultiplicityStatus.ESTABLISHED, OptionalInt.of(1),
						DomainLocation.INTERIOR, false, Regularity.REGULAR);
		boolean continued = root.status()
				== IdentityStatus.CONTINUATION_ESTABLISHED;
		boolean existing = continued || root.status()
				== IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED;
		IntersectionRootLineage2D lineage = new IntersectionRootLineage2D(
				existing ? LineageEventKind.UNCHANGED : LineageEventKind.APPEARED,
				List.of(), List.of(root.token()), List.of(), continued);
		return new LocusIntersectionSolution2D(identity, evidence,
				new LocusPoint2D(parameter, 0), classification, lineage,
				List.of());
	}

	private static String materialPrefix() {
		return hex(OWNER) + "~" + hex(SOURCE_PAIR) + "~" + hex(CONSTRUCTIVE)
				+ "~" + hex(TOPOLOGY) + "~";
	}

	private static String toLegacyV1State(String currentState) {
		String[] fields = currentState.split("\\|", -1);
		assertEquals("4", fields[0]);
		return "1|" + fields[1] + "|" + legacySnapshot(fields[2]) + "|"
				+ legacySnapshot(fields[3]);
	}

	private static String mutateOnlyCurrentEntry(String state,
			java.util.function.UnaryOperator<String[]> mutation) {
		String[] fields = state.split("\\|", -1);
		String[] snapshot = fields[2].split("~", -1);
		assertEquals("1", snapshot[4]);
		String[] entry = snapshot[5].split(",", -1);
		assertEquals(10, entry.length);
		snapshot[5] = String.join(",", mutation.apply(entry));
		fields[2] = String.join("~", snapshot);
		return String.join("|", fields);
	}

	private static long statusCount(String state, String status) {
		String[] fields = state.split("\\|", -1);
		String[] entries = fields[2].split("~", -1);
		return java.util.Arrays.stream(entries).skip(5)
				.filter(entry -> entry.startsWith(status + ",")).count();
	}

	private static String legacySnapshot(String snapshot) {
		if ("-".equals(snapshot)) {
			return snapshot;
		}
		String[] parts = snapshot.split("~", -1);
		for (int index = 5; index < parts.length; index++) {
			String[] entry = parts[index].split(",", -1);
			assertEquals(10, entry.length);
			parts[index] = String.join(",",
					java.util.Arrays.copyOf(entry, 8));
		}
		return String.join("~", parts);
	}

	private static String hex(String value) {
		return java.util.HexFormat.of().formatHex(
				value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	private static String unhex(String value) {
		return new String(java.util.HexFormat.of().parseHex(value),
				java.nio.charset.StandardCharsets.UTF_8);
	}

	private record PublishedRoot(String token, String continuationKey,
			IntersectionRootAddressProof2D addressProof, IdentityStatus status) {
		PublishedRoot(IntersectionRootAllocation2D allocation,
				IntersectionRootAddressProof2D addressProof,
				IdentityStatus status) {
			this(allocation.getRootToken(), allocation.getContinuationKey(),
					addressProof, status);
		}
	}
}
