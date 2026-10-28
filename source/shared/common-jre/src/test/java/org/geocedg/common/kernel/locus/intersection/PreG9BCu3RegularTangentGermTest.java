/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU3 increment {@code CU3-B2}: the regular tangent contact germ and its selector
 * (CU3-B2 design sections 3 to 5; validation matrix rows B2-5 to B2-11 at the level of the
 * selector). These are properties of the germ and the selector, proved here on canonical keys;
 * the dynamic transitions of a regular tangency are not reachable by moving a target in the
 * product (G-B2-1 record, K1), and the absence of a current anchor leaves a claimed token latent
 * by the generic rules of ADR 0017, without a rule of its own.
 */
final class PreG9BCu3RegularTangentGermTest {

	private static final String LINEAGE = "generator.main/component/0";

	@Test
	void theGermIsCanonicalAndCitesOnlyLineageTargetAndSide() {
		String germ = RegularTangentAnchor2D.of(1).germ(LINEAGE, "LINE");
		assertEquals(RegularTangentAnchor2D.GERM_PREFIX + "26:" + LINEAGE
				+ "14:LINE/canonical8:positive", germ);
		assertTrue(RegularTangentAnchor2D.isGerm(germ));
		assertEquals(Optional.of(LINEAGE), RegularTangentAnchor2D.germLineage(germ));
		for (String forged : new String[] {germ + "x", germ.replace("positive", "zero"),
				germ.replace("LINE/canonical", "LINE"), germ.replace("26:", "026:"),
				RegularTangentAnchor2D.GERM_PREFIX}) {
			assertFalse(RegularTangentAnchor2D.isGerm(forged), forged);
		}
		assertNull(RegularTangentAnchor2D.of(0), "an undecided side anchors nothing (P1)");
	}

	@Test
	void b2x10OppositeSidesAreDistinctSelectorsAndNeverShareAToken() {
		String positive = RegularTangentAnchor2D.of(1).germ(LINEAGE, "CIRCLE");
		String negative = RegularTangentAnchor2D.of(-1).germ(LINEAGE, "CIRCLE");
		assertNotEquals(positive, negative);
		assertNotEquals(IntersectionRootDeterministicSelector2D.of(LINEAGE, positive),
				IntersectionRootDeterministicSelector2D.of(LINEAGE, negative),
				"B2-10: a change of side is a change of selector, a barrier");
		assertNotEquals(RegularTangentAnchor2D.of(1).germ(LINEAGE, "LINE"), positive,
				"B2-3: another target family is another selector");
	}

	@Test
	void b2x5to7TheRegularGermIsDisjointFromFoldAndTransverseGerms() {
		String regular = RegularTangentAnchor2D.of(-1).germ(LINEAGE, "LINE");
		assertFalse(FoldContactAnchor2D.isGerm(regular), "never a fold anchor of CU3-B1");
		IntersectionRootDeterministicSelector2D selector =
				IntersectionRootDeterministicSelector2D.of(LINEAGE, regular);
		assertTrue(selector.isRegularTangentContact());
		assertFalse(selector.isFoldContact());
		assertFalse(PublicTargetIntersectionCapability2D.currentPublicRootOrientation(regular)
				.isPresent(), "B2-5: no transverse orientation, so no transverse root can"
						+ " resolve the selector of the contact or inherit its token");
	}

	@Test
	void b2x11ARepeatedRegularGermTakesPhaseAndRankUnlikeAFoldLabel() {
		String regular = RegularTangentAnchor2D.of(1).germ(LINEAGE, "LINE");
		IntersectionRootDeterministicSelector2D first =
				IntersectionRootDeterministicSelector2D.ofIntrinsicPhase(LINEAGE, regular,
						Orientation.INCREASING, true, 2, 0);
		IntersectionRootDeterministicSelector2D second =
				IntersectionRootDeterministicSelector2D.ofIntrinsicPhase(LINEAGE, regular,
						Orientation.INCREASING, true, 2, 1);
		assertNotEquals(first, second, "B2-11: two contacts with one germ, two ranks");
		assertTrue(first.hasIntrinsicPhase());

		String fold = FoldContactAnchor2D.GERM_PREFIX + "26:" + LINEAGE + "10:5:t->-t1:0";
		assertTrue(FoldContactAnchor2D.isGerm(fold), fold);
		assertThrows(IllegalArgumentException.class,
				() -> IntersectionRootDeterministicSelector2D.ofIntrinsicPhase(LINEAGE, fold,
						Orientation.INCREASING, true, 2, 0), "a fold label carries no rank");
	}

	@Test
	void theRevisionLocalMarkerIsNeverAGerm() {
		RegularTangentAnchor2D anchor = RegularTangentAnchor2D.of(-1);
		assertTrue(RegularTangentAnchor2D.fromMarker(Optional.of(anchor.marker())).isPresent());
		assertFalse(RegularTangentAnchor2D.isGerm(anchor.marker()));
		assertFalse(RegularTangentAnchor2D.fromMarker(Optional.of(anchor.marker() + "x"))
				.isPresent());
		assertFalse(RegularTangentAnchor2D.fromMarker(Optional.empty()).isPresent());
	}

	@Test
	void b2x12TheExactEvidenceCarriesASideOnlyForOrderTwo() {
		ExactContactEvidence2D two = new ExactContactEvidence2D("0", 0, 0,
				IntersectionSemanticMetadata2D.ContactEvidenceClass.CONTACT_EXISTENCE_EXACT,
				IntersectionSemanticMetadata2D.ContactOrder.TWO, 1, "declared");
		assertEquals(-1, two.withCanonicalSide(-1).getCanonicalSide());
		assertEquals(0, two.getCanonicalSide());
		ExactContactEvidence2D fold = new ExactContactEvidence2D("0", 0, 0,
				IntersectionSemanticMetadata2D.ContactEvidenceClass.CONTACT_EXISTENCE_EXACT,
				IntersectionSemanticMetadata2D.ContactOrder.FOLD_CONTACT, 1, "declared");
		assertEquals(0, fold.withCanonicalSide(1).getCanonicalSide(),
				"a fold contact is never a regular tangent contact");
		assertThrows(IllegalArgumentException.class, () -> two.withCanonicalSide(2));
	}
}
