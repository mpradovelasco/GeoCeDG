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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairCoverageStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairIsolationMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairUniquenessStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ResidualQuantityKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTokenLedger2D.PairBindingState;
import org.geocedg.common.kernel.locus.intersection.PairSemanticSlotSelector2D.DomainKind;
import org.geocedg.common.kernel.locus.intersection.PairSemanticSlotSelector2D.SourceDescriptor;
import org.junit.jupiter.api.Test;

/**
 * D2 structural ledger unit tests. Synthetic rich publications below are assumed
 * current certificate fixtures, NOT a numerical solver or certification proof.
 * The separate numerical/host suite establishes actual pair-root eligibility.
 */
final class G9S1R1PairTokenLedgerTest {
	private static final String OWNER = "pair-owner";
	private static final String CONSTRUCTIVE = "pair-query/v1";
	private static final String TOPOLOGY = "pair-topology/v1";
	private static final String CONTRACT = "singleton-germ-proof/v1";
	private static final SourceDescriptor FIRST = descriptor("a", "a");
	private static final SourceDescriptor SECOND = descriptor("b", "b");
	private static final PairSemanticSlotSelector2D POSITIVE =
			PairSemanticSlotSelector2D.of(FIRST, SECOND, 1);
	private static final PairSemanticSlotSelector2D NEGATIVE =
			PairSemanticSlotSelector2D.of(FIRST, SECOND, -1);
	private static final String BRANCH = LocusPairIdentity2D.componentPair(
			FIRST.getBranchLineage(), FIRST.getComponentLineage(),
			SECOND.getBranchLineage(), SECOND.getComponentLineage());

	@Test
	void sourceSwapNormalizesGermAndKeepsDescriptorsAssociated() {
		assertEquals(POSITIVE, PairSemanticSlotSelector2D.of(SECOND, FIRST, -1));
		assertNotEquals(POSITIVE, PairSemanticSlotSelector2D.of(SECOND, FIRST, 1));
		assertEquals("branch-a", POSITIVE.getFirst().getBranchLineage());
		assertNotEquals(POSITIVE, PairSemanticSlotSelector2D.of(
				descriptor("a", "b"), descriptor("b", "a"), 1));
		assertThrows(IllegalArgumentException.class,
				() -> PairSemanticSlotSelector2D.of(FIRST, FIRST, 1));
	}

	@Test
	void selectorRoundTripIsStrictAndContainsNoAddressOrChart() {
		String encoded = POSITIVE.toExternalForm();
		assertEquals(POSITIVE, PairSemanticSlotSelector2D.parse(encoded));
		assertFalse(encoded.contains("0.25"));
		assertFalse(encoded.contains("rank"));
		assertFalse(encoded.contains("chart"));
		assertThrows(IllegalArgumentException.class,
				() -> PairSemanticSlotSelector2D.parse(encoded + "0:"));
		assertThrows(IllegalArgumentException.class,
				() -> PairSemanticSlotSelector2D.parse(encoded.replace(
						"POSITIVE", "UNKNOWN!")));
	}

	@Test
	void pairProofRetainsBothExactAddressBitsAndCanonicalAxes() {
		PairRootAddressProof2D first = proof(POSITIVE, -0.0, Math.nextUp(0.5));
		PairRootAddressProof2D reversed = PairRootAddressProof2D.of("b", "b/r1",
				Math.nextUp(0.5), "a", "a/r1", -0.0);
		assertEquals(first, reversed);
		assertEquals(first, PairRootAddressProof2D.parse(first.toExternalForm()));
		assertEquals(Double.doubleToLongBits(-0.0), first.getFirstParameterBits());
		assertNotEquals(first, proof(POSITIVE, 0.0, Math.nextUp(0.5)));
		assertThrows(IllegalArgumentException.class,
				() -> proof(POSITIVE, Double.NaN, 0));
	}

	@Test
	void oppositeGermsReceiveDistinctOpaqueTokensWithV5PairBindings() {
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		var evaluation = begin(ledger, OWNER, POSITIVE);
		Published positive = allocate(evaluation, POSITIVE, 0.25, 0.75);
		Published negative = allocate(evaluation, NEGATIVE, 0.75, 0.25);
		ledger.commit(evaluation, published(POSITIVE, positive, negative));
		assertNotEquals(positive.token(), negative.token());
		assertTrue(positive.token().startsWith("locus-root/v3/"));
		assertTrue(ledger.exportState().startsWith("5|"));
		assertEquals(Optional.of(PairBindingState.ACTIVE),
				ledger.getPairBindingState(positive.token()));
		assertTrue(ledger.validatesCurrentToken(negative.token()));
	}

	@Test
	void ordinaryParameterAndProviderDriftNeverReplaceTheSelectorToken() {
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		Published original = commitOne(ledger, POSITIVE, 0.25, 0.75);
		var next = begin(ledger, OWNER, POSITIVE);
		PairRootAddressProof2D moved = PairRootAddressProof2D.of("a", "a/r2",
				0.45, "b", "b/r2", 0.55);
		var allocation = next.resolveCurrentPairRoot(BRANCH, CONTRACT, POSITIVE,
				moved);
		assertTrue(allocation.isReused());
		assertEquals(original.token(), allocation.getRootToken());
		ledger.commit(next, published(POSITIVE,
				new Published(allocation, POSITIVE, moved)));
		assertTrue(ledger.validatesCurrentToken(original.token()));
	}

	@Test
	void missingCurrentProofMakesClaimDormantAndUniqueRecurrenceReusesIt() {
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		Published original = commitOne(ledger, POSITIVE, 0.25, 0.75);
		assertTrue(ledger.retainMaterializedToken(original.token()));
		ledger.commit(begin(ledger, OWNER, POSITIVE), published(POSITIVE));
		assertEquals(Optional.of(PairBindingState.DORMANT),
				ledger.getPairBindingState(original.token()));
		String dormant = ledger.exportState();
		LocusIntersectionTokenLedger2D reopened = new LocusIntersectionTokenLedger2D();
		reopened.importState(dormant);
		assertEquals(dormant, reopened.exportState());
		Published resumed = commitOne(reopened, POSITIVE, 0.4, 0.6);
		assertEquals(original.token(), resumed.token());
		assertEquals(Optional.of(PairBindingState.ACTIVE),
				reopened.getPairBindingState(original.token()));
	}

	@Test
	void pairQuarantineRoundTripIsNotStickyAndNeverBecomesR4PeriodicState() {
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		Published original = commitOne(ledger, POSITIVE, 0.25, 0.75);
		ledger.retainMaterializedToken(original.token());
		var ambiguous = begin(ledger, OWNER, POSITIVE);
		ambiguous.quarantineCurrentPairSelector(POSITIVE);
		ledger.commit(ambiguous, published(POSITIVE));
		String quarantined = ledger.exportState();
		assertTrue(quarantined.contains("~P,p,"));
		assertFalse(quarantined.contains("~P,q,"));
		LocusIntersectionTokenLedger2D restored = new LocusIntersectionTokenLedger2D();
		restored.importState(quarantined);
		assertEquals(Optional.of(PairBindingState.QUARANTINED),
				restored.getPairBindingState(original.token()));
		Published active = commitOne(restored, POSITIVE, 0.3, 0.7);
		assertEquals(original.token(), active.token());
		assertTrue(restored.validatesCurrentToken(original.token()));
	}

	@Test
	void unavailableSnapshotPreservesOnlyClaimsAndDowngradesPairQuarantine() {
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		Published original = commitOne(ledger, POSITIVE, 0.25, 0.75);
		ledger.retainMaterializedToken(original.token());
		var ambiguous = begin(ledger, OWNER, POSITIVE);
		ambiguous.quarantineCurrentPairSelector(POSITIVE);
		ledger.commit(ambiguous, published(POSITIVE));
		ledger.observeUnavailable();
		assertEquals(Optional.of(PairBindingState.DORMANT),
				ledger.getPairBindingState(original.token()));
		ledger.releaseMaterializedToken(original.token());
		assertFalse(ledger.validatesRetainedToken(original.token()));
		assertTrue(ledger.exportState().startsWith("4|"));
	}

	@Test
	void unclaimedAmbiguityAllocatesAndRetainsNoPairToken() {
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		Published unclaimed = commitOne(ledger, POSITIVE, 0.25, 0.75);
		var ambiguous = begin(ledger, OWNER, POSITIVE);
		ambiguous.quarantineCurrentPairSelector(POSITIVE);
		ledger.commit(ambiguous, published(POSITIVE));
		assertFalse(ledger.validatesRetainedToken(unclaimed.token()));
		assertTrue(ledger.exportState().startsWith("4|2|"));
	}

	@Test
	void duplicateStagingFailsClosedAndQuarantineCannotContradictStaging() {
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		var evaluation = begin(ledger, OWNER, POSITIVE);
		Published first = allocate(evaluation, POSITIVE, 0.25, 0.75);
		Published duplicate = allocate(evaluation, POSITIVE, 0.75, 0.25);
		assertEquals(first.token(), duplicate.token());
		assertThrows(IllegalArgumentException.class,
				() -> evaluation.quarantineCurrentPairSelector(POSITIVE));
		ledger.commit(evaluation, published(POSITIVE, first));
		assertFalse(ledger.validatesCurrentToken(first.token()));
	}

	@Test
	void wrongSourceOrBranchContextCannotRetargetRetainedPairBinding() {
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		commitOne(ledger, POSITIVE, 0.25, 0.75);
		var evaluation = begin(ledger, OWNER, POSITIVE);
		assertThrows(IllegalArgumentException.class,
				() -> evaluation.resolveCurrentPairRoot("wrong-branch", CONTRACT,
						POSITIVE, proof(POSITIVE, 0.25, 0.75)));
		assertThrows(IllegalArgumentException.class,
				() -> evaluation.resolveCurrentPairRoot(BRANCH, CONTRACT, POSITIVE,
						PairRootAddressProof2D.of("a", "a/r1", 0.25,
								"c", "c/r1", 0.75)));
		assertThrows(IllegalStateException.class,
				() -> ledger.begin(OWNER, "foreign-pair", CONSTRUCTIVE, TOPOLOGY));
		ledger.abort(evaluation);
	}

	@Test
	void pairSchemaRejectsDownversionedPayloadsMalformedVariantsAndProofs() {
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		commitOne(ledger, POSITIVE, 0.25, 0.75);
		String state = ledger.exportState();
		for (String version : List.of("1", "2", "3", "4")) {
			assertThrows(IllegalArgumentException.class,
					() -> new LocusIntersectionTokenLedger2D()
							.importState(version + state.substring(1)));
		}
		assertThrows(IllegalArgumentException.class,
				() -> new LocusIntersectionTokenLedger2D()
						.importState(state.replace("~P,a,", "~S,a,")));
		assertThrows(IllegalArgumentException.class,
				() -> new LocusIntersectionTokenLedger2D()
						.importState(state.replace("~P,a,", "~P,q,")));
		String wrongProof = mutatePairEntry(state, entry -> {
			entry[8] = hex(PairRootAddressProof2D.of("a", "a/r1", 0.25,
					"c", "c/r1", 0.75).toExternalForm());
			return entry;
		});
		assertThrows(IllegalArgumentException.class,
				() -> new LocusIntersectionTokenLedger2D().importState(wrongProof));
	}

	@Test
	void exactCopyReversesCanonicalAxesAndGermWithoutLosingTokenOwnership() {
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		Published original = commitOne(ledger, POSITIVE, 0.25, 0.75);
		ledger.retainMaterializedToken(original.token());
		Map<String, String> mapping = Map.of("a", "z", "b", "y");
		PairSemanticSlotSelector2D copied = POSITIVE.remapSources(mapping);
		assertEquals(-1, copied.getGerm());
		assertEquals("branch-b", copied.getFirst().getBranchLineage());
		ledger.authorizeImmediateCopy(OWNER);
		ledger.preparePairSourceCopy(mapping);
		var copy = begin(ledger, "copy-owner", copied);
		PairRootAddressProof2D copyProof = original.proof().remapSources(mapping);
		var allocated = copy.resolveCurrentPairRoot(BRANCH, CONTRACT, copied,
				copyProof);
		ledger.commit(copy, published(copied,
				new Published(allocated, copied, copyProof)));
		assertNotEquals(original.token(), allocated.getRootToken());
		assertEquals(allocated.getRootToken(), ledger.rebaseCopiedRetainedToken(
				original.token(), "copy-owner", OWNER).orElseThrow());
		ledger.validatePairSourceAttachments("z", "y", mapping);
		ledger.validatePreattachmentContext("copy-owner", copied.getSourcePairIdentity(),
				CONSTRUCTIVE, TOPOLOGY, OWNER, POSITIVE.getSourcePairIdentity(), false);
		LocusIntersectionTokenLedger2D reopened = new LocusIntersectionTokenLedger2D();
		reopened.importState(ledger.exportState());
		reopened.validatePairSourceAttachments("y", "z", mapping);
		assertEquals(allocated.getRootToken(), reopened.rebaseCopiedRetainedToken(
				original.token(), "copy-owner", OWNER).orElseThrow());
	}

	@Test
	void dormantClosureCopyRetainsExactAssociationAndCanReactivateLater() {
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		Published original = commitOne(ledger, POSITIVE, 0.25, 0.75);
		ledger.retainMaterializedToken(original.token());
		ledger.observeUnavailable();
		Map<String, String> mapping = Map.of("a", "z", "b", "y");
		PairSemanticSlotSelector2D copied = POSITIVE.remapSources(mapping);
		ledger.authorizeImmediateCopy(OWNER);
		ledger.preparePairSourceCopy(mapping);
		ledger.commit(begin(ledger, "copy-owner", copied), published(copied));
		String copiedToken = ledger.rebaseCopiedRetainedToken(original.token(),
				"copy-owner", OWNER).orElseThrow();
		assertEquals(Optional.of(PairBindingState.DORMANT),
				ledger.getPairBindingState(copiedToken));
		var next = begin(ledger, "copy-owner", copied);
		Published active = allocate(next, copied, 0.6, 0.4);
		ledger.commit(next, published(copied, active));
		assertEquals(copiedToken, active.token());
		assertThrows(IllegalArgumentException.class,
				() -> ledger.validatePairSourceAttachments("y", "z",
						Map.of("a", "y", "b", "z")));
	}

	@Test
	void copyWithoutExactTwoSourceAssociationFailsBeforeAllocation() {
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		commitOne(ledger, POSITIVE, 0.25, 0.75);
		ledger.authorizeImmediateCopy(OWNER);
		assertThrows(IllegalArgumentException.class,
				() -> ledger.preparePairSourceCopy(Map.of("a", "y")));
		assertThrows(IllegalArgumentException.class,
				() -> ledger.preparePairSourceCopy(Map.of("a", "z", "b", "z")));
		assertThrows(IllegalArgumentException.class,
				() -> ledger.begin("copy-owner", LocusPairIdentity2D.sourcePair("y", "z"),
						CONSTRUCTIVE, TOPOLOGY));
	}

	@Test
	void directIncrementalAndDormantHistoriesHaveIdenticalFinalSlotBindings() {
		LocusIntersectionTokenLedger2D source = new LocusIntersectionTokenLedger2D();
		Published original = commitOne(source, POSITIVE, 0.25, 0.75);
		source.retainMaterializedToken(original.token());
		String initial = source.exportState();
		String expected = null;
		for (int path = 0; path < 3; path++) {
			LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
			ledger.importState(initial);
			if (path == 1) {
				for (int step = 1; step < 5; step++) {
					commitOne(ledger, POSITIVE, 0.25 + step * 0.01,
							0.75 - step * 0.01);
				}
			} else if (path == 2) {
				ledger.observeUnavailable();
				ledger.importState(ledger.exportState());
			}
			Published finalRoot = commitOne(ledger, POSITIVE, 0.4, 0.6);
			assertEquals(original.token(), finalRoot.token());
			if (expected == null) {
				expected = ledger.exportState();
			} else {
				assertEquals(expected, ledger.exportState());
			}
		}
	}

	private static SourceDescriptor descriptor(String id, String structure) {
		return new SourceDescriptor(id, "branch-" + structure,
				"component-" + structure, Orientation.INCREASING,
				DomainKind.NON_PERIODIC, "spline-domain/v1");
	}

	private static PairRootAddressProof2D proof(PairSemanticSlotSelector2D selector,
			double first, double second) {
		String a = selector.getFirst().getSourceId();
		String b = selector.getSecond().getSourceId();
		return PairRootAddressProof2D.of(a, a + "/r1", first, b, b + "/r1", second);
	}

	private static LocusIntersectionTokenLedger2D.Evaluation begin(
			LocusIntersectionTokenLedger2D ledger, String owner,
			PairSemanticSlotSelector2D selector) {
		return ledger.begin(owner, selector.getSourcePairIdentity(), CONSTRUCTIVE,
				TOPOLOGY);
	}

	private static Published allocate(LocusIntersectionTokenLedger2D.Evaluation attempt,
			PairSemanticSlotSelector2D selector, double first, double second) {
		PairRootAddressProof2D proof = proof(selector, first, second);
		return new Published(attempt.resolveCurrentPairRoot(BRANCH, CONTRACT,
				selector, proof), selector, proof);
	}

	private static Published commitOne(LocusIntersectionTokenLedger2D ledger,
			PairSemanticSlotSelector2D selector, double first, double second) {
		var evaluation = begin(ledger, OWNER, selector);
		Published root = allocate(evaluation, selector, first, second);
		ledger.commit(evaluation, published(selector, root));
		return root;
	}

	private static LocusIntersectionResult2D published(
			PairSemanticSlotSelector2D selector, Published... roots) {
		List<LocusIntersectionSolution2D> solutions = Arrays.stream(roots)
				.map(G9S1R1PairTokenLedgerTest::solution).toList();
		return new LocusIntersectionResult2D(
				IntersectionSourceBinding2D.unavailableLocusPair(
						selector.getFirst().getSourceId(), selector.getSecond().getSourceId(),
						CONSTRUCTIVE, TOPOLOGY), ComputationStatus.SUCCESS,
				new IntersectionCompletenessEvidence2D(Completeness.NOT_ESTABLISHED,
						CompletenessMethod.NOT_ESTABLISHED, roots.length,
						List.of(BRANCH), List.of()),
				roots.length == 0 ? GeometryKind.UNRESOLVED : GeometryKind.FINITE,
				Currentness.CURRENT, SupportLevel.EXACT_CAPABILITY,
				NumericGuarantee.CERTIFIED_ERROR_BOUND, solutions, List.of(),
				new LocusIntersectionInstrumentation2D(
						LocusIntersectionWorkBudget2D.initial()).snapshot(), List.of());
	}

	private static LocusIntersectionSolution2D solution(Published root) {
		double first = Double.longBitsToDouble(root.proof().getFirstParameterBits());
		double second = Double.longBitsToDouble(root.proof().getSecondParameterBits());
		LocusPoint2D point = new LocusPoint2D(first, second);
		IntersectionRootIdentity2D identity = new IntersectionRootIdentity2D(
				root.token(), root.selector().getSourcePairIdentity(), CONSTRUCTIVE,
				BRANCH, TOPOLOGY, Optional.of(root.allocation().getContinuationKey()),
				IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED);
		IntersectionResidualContract2D residual = new IntersectionResidualContract2D(
				"pair-distance/v1", ResidualQuantityKind.MODEL_COORDINATE_DISTANCE,
				"model-coordinate", "test-normalization", "1");
		IntersectionRootRevisionEvidence2D evidence = new IntersectionRootRevisionEvidence2D(
				1, 1, BRANCH, BRANCH, first, OptionalDouble.empty(), interval(first),
				LocalIsolationStatus.ESTABLISHED, new TargetResidual2D(0, 1, 0, 1, residual),
				SolverMethod.CERTIFIED_INTERVAL, NumericGuarantee.CERTIFIED_ERROR_BOUND,
				Optional.empty());
		LocalPairIsolationEvidence2D isolation = new LocalPairIsolationEvidence2D(
				LocalIsolationStatus.ESTABLISHED, PairIsolationMethod.CERTIFIED_RECTANGLE,
				PairCoverageStatus.EXHAUSTIVE_RECTANGLE, PairUniquenessStatus.CERTIFIED_UNIQUE,
				NumericGuarantee.CERTIFIED_ERROR_BOUND, "Assumed ledger fixture certificate");
		LocusPairIntersectionEvidence2D pair = new LocusPairIntersectionEvidence2D(
				side(root.selector().getFirst(), first), side(root.selector().getSecond(), second),
				isolation, new LocusPairResidualEvidence2D(point, point, 0, 1E-9,
						NumericGuarantee.CERTIFIED_ERROR_BOUND),
				OptionalDouble.of(root.selector().getGerm()), SolverMethod.CERTIFIED_INTERVAL,
				NumericGuarantee.CERTIFIED_ERROR_BOUND);
		return new LocusIntersectionSolution2D(identity, evidence, point,
				new IntersectionClassification2D(ContactClass.TRANSVERSE_ESTABLISHED,
						MultiplicityStatus.ESTABLISHED, OptionalInt.of(1),
						DomainLocation.INTERIOR, false, Regularity.REGULAR),
				new IntersectionRootLineage2D(LineageEventKind.UNCHANGED, List.of(),
						List.of(root.token()), List.of(), false), List.of(), Optional.of(pair));
	}

	private static LocusPairSourceRevisionEvidence2D side(SourceDescriptor descriptor,
			double parameter) {
		return new LocusPairSourceRevisionEvidence2D(descriptor.getSourceId(), 1,
				descriptor.getBranchLineage(), descriptor.getComponentLineage(), parameter,
				OptionalDouble.empty(), interval(parameter));
	}

	private static IntersectionParameterInterval2D interval(double parameter) {
		return new IntersectionParameterInterval2D(parameter - 1E-6, parameter + 1E-6);
	}

	private static String mutatePairEntry(String state,
			java.util.function.UnaryOperator<String[]> mutation) {
		String[] fields = state.split("\\|", -1);
		String[] snapshot = fields[2].split("~", -1);
		snapshot[5] = String.join(",", mutation.apply(snapshot[5].split(",", -1)));
		fields[2] = String.join("~", snapshot);
		return String.join("|", fields);
	}

	private static String hex(String value) {
		return java.util.HexFormat.of().formatHex(
				value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	private record Published(IntersectionRootAllocation2D allocation,
			PairSemanticSlotSelector2D selector, PairRootAddressProof2D proof) {
		String token() {
			return allocation.getRootToken();
		}
	}
}
