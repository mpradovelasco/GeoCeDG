/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.locus.constructive.RelationIntegrityRecord.Evidence;
import org.junit.jupiter.api.Test;

/**
 * Integrity of relations and its propagation by premises ({@code relation-integrity/v1},
 * {@code locus-v2-constructive-authority.md} section 6): validation matrix rows V-AUT-INT-1 to
 * V-AUT-INT-3 on a synthetic graph of premises.
 */
final class RelationIntegrityTest {

	private static final List<RelationIntegrityRecord> NONE = Collections.emptyList();
	private static final List<String> ESTABLISHED = Collections.emptyList();

	@Test
	void integrityIsTheMethodBoundedByTheWorstPremise() {
		RelationIntegrityRecord strict = strict("A on g");
		RelationIntegrityRecord relaxed = RelationIntegrityRecord.derive("B on c",
				RelationIntegrity.STRICT, Evidence.SUBSTITUTE_GENERIC, NONE,
				Arrays.asList("H"), null);

		assertEquals(RelationIntegrity.RELAXED, relaxed.getIntegrity(),
				"a relaxed kind never gives STRICT, even when the method says so");
		assertEquals(RelationIntegrity.STRICT, derived("P on r", strict).getIntegrity());
		assertEquals(RelationIntegrity.RELAXED, derived("P on r", strict, relaxed)
				.getIntegrity(), "a relaxed premise bounds the derivation");
	}

	@Test
	void aContradictedPremiseVoidsTheDerivation() {
		RelationIntegrityRecord refuted = RelationIntegrityRecord.derive("Q on n",
				RelationIntegrity.CONTRADICTED, Evidence.EXACT_EVALUATION, NONE, ESTABLISHED,
				"INCIDENCE_REFUTED");

		assertEquals(RelationIntegrity.CONTRADICTED, refuted.getIntegrity());
		RelationIntegrityRecord dependent = derived("P on r", strict("A on g"), refuted);
		assertEquals(RelationIntegrity.NOT_ESTABLISHED, dependent.getIntegrity(),
				"what depends on a contradicted premise is not established, never contradicted");
	}

	@Test
	void aRefutationIsRigorousOnlyWithStrictPremises() {
		RelationIntegrityRecord relaxed = RelationIntegrityRecord.derive("B on c",
				RelationIntegrity.RELAXED, Evidence.SUBSTITUTE_GENERIC, NONE,
				Arrays.asList("H"), null);
		RelationIntegrityRecord refutationOnRelaxed = RelationIntegrityRecord.derive("P on r",
				RelationIntegrity.CONTRADICTED, Evidence.EXACT_EVALUATION,
				Collections.singletonList(relaxed), ESTABLISHED, "INCIDENCE_REFUTED");
		RelationIntegrityRecord refutationOnSubstitute = RelationIntegrityRecord.derive("P on r",
				RelationIntegrity.CONTRADICTED, Evidence.EXACT_EVALUATION, NONE,
				Arrays.asList("H"), "INCIDENCE_REFUTED");

		assertEquals(RelationIntegrity.NOT_ESTABLISHED, refutationOnRelaxed.getIntegrity());
		assertEquals(RelationIntegrity.NOT_ESTABLISHED, refutationOnSubstitute.getIntegrity(),
				"an exact refutation with a non-constructible substitute is not rigorous");
	}

	@Test
	void anUnestablishedPremiseNeverGivesStrictToAStrictMethod() {
		RelationIntegrityRecord exactWithSubstitute = RelationIntegrityRecord.derive("P on r",
				RelationIntegrity.STRICT, Evidence.EXACT_EVALUATION, NONE,
				Arrays.asList("H", "decision of If(b, P1, P2)"), null);

		assertEquals(RelationIntegrity.NOT_ESTABLISHED, exactWithSubstitute.getIntegrity(),
				"P1: no inherited decision or substitute is a strict premise, and one exact"
						+ " equality at a substitute is not relaxed evidence either");
		assertEquals(Arrays.asList("H", "decision of If(b, P1, P2)"),
				exactWithSubstitute.getUnestablishedPremises());
	}

	@Test
	void aDistantRelaxedRelationDoesNotContaminateProvenance() {
		RelationIntegrityRecord distant = RelationIntegrityRecord.derive("X on y",
				RelationIntegrity.RELAXED, Evidence.SUBSTITUTE_GENERIC, NONE,
				Arrays.asList("H"), null);
		RelationIntegrityRecord provenance = strict("M_1 on n");

		assertEquals(RelationIntegrity.RELAXED, distant.getIntegrity());
		assertEquals(RelationIntegrity.STRICT, derived("h_2 through M_1", provenance)
				.getIntegrity(), "a relation inherits only what its proof uses");
	}

	@Test
	void consumersFailExplicitlyWithATypedCause() {
		RelationIntegrityRecord relaxed = RelationIntegrityRecord.derive("P on r",
				RelationIntegrity.RELAXED, Evidence.SUBSTITUTE_GENERIC, NONE,
				Arrays.asList("H"), null);

		assertNull(relaxed.unmetRequirementCause(RelationIntegrity.RELAXED));
		String cause = relaxed.unmetRequirementCause(RelationIntegrity.STRICT);
		assertNotNull(cause, "V-AUT-INT-2: a strict consumer fails explicitly");
		assertTrue(cause.startsWith("INTEGRITY_REQUIREMENT_NOT_MET"), cause);
		assertEquals(Arrays.asList("H"), relaxed.getUnestablishedPremises(),
				"the query of integrity lists the premises that are not established");
		RelationIntegrityRecord contradicted = RelationIntegrityRecord.derive("Q on n",
				RelationIntegrity.CONTRADICTED, Evidence.EXACT_EVALUATION, NONE, ESTABLISHED,
				"INCIDENCE_REFUTED");
		assertNotNull(contradicted.unmetRequirementCause(RelationIntegrity.NOT_ESTABLISHED),
				"a contradicted relation satisfies no requirement");
	}

	@Test
	void aContradictionIsNeverLaunderedIntoRelaxed() {
		RelationIntegrityRecord refuted = RelationIntegrityRecord.derive("P on r",
				RelationIntegrity.CONTRADICTED, Evidence.EXACT_EVALUATION, NONE, ESTABLISHED,
				"INCIDENCE_REFUTED");
		RelationIntegrityRecord relaxedClaim = RelationIntegrityRecord.derive("P on r",
				RelationIntegrity.RELAXED, Evidence.ENCLOSURE_COMPATIBLE,
				Collections.singletonList(refuted), ESTABLISHED, null);

		assertFalse(relaxedClaim.getIntegrity().satisfies(RelationIntegrity.RELAXED),
				"a relaxed admission resting on a contradicted fact never admits it");
	}

	@Test
	void notEstablishedRecordsCarryTheirCause() {
		RelationIntegrityRecord record = RelationIntegrityRecord.notEstablished("P on L",
				"EVIDENCE_INSUFFICIENT: OPEN_MARGIN_INSUFFICIENT");

		assertEquals(RelationIntegrity.NOT_ESTABLISHED, record.getIntegrity());
		assertEquals(Evidence.NONE, record.getEvidence());
		assertEquals("EVIDENCE_INSUFFICIENT: OPEN_MARGIN_INSUFFICIENT", record.getCause());
		assertThrows(NullPointerException.class,
				() -> RelationIntegrityRecord.notEstablished("P on L", null));
	}

	private static RelationIntegrityRecord strict(String relation) {
		return RelationIntegrityRecord.derive(relation, RelationIntegrity.STRICT,
				Evidence.PROVENANCE, NONE, ESTABLISHED, null);
	}

	private static RelationIntegrityRecord derived(String relation,
			RelationIntegrityRecord... premises) {
		return RelationIntegrityRecord.derive(relation, RelationIntegrity.STRICT,
				Evidence.EXACT_EVALUATION, Arrays.asList(premises), ESTABLISHED, null);
	}
}
