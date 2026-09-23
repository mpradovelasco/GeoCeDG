/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.intersection.PairSemanticSlotSelector2D.DomainKind;
import org.geocedg.common.kernel.locus.intersection.PairSemanticSlotSelector2D.SourceDescriptor;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R2-E0/E1 copy/remap of pair selectors whose parameterization names a
 * structural participant (pair materialization spec section 10). Identities are
 * synthetic canonical durable IDs; no geometry is involved.
 */
final class PreG9bR2E1PairCopyParticipantTest {
	private static final String RING = geo('1');
	private static final String SPLINE = geo('2');
	private static final String DRIVER = geo('3');
	private static final String RING_COPY = geo('a');
	private static final String SPLINE_COPY = geo('b');
	private static final String DRIVER_COPY = geo('c');
	private static final Map<String, String> SOURCES = Map.of(RING, RING_COPY, SPLINE,
			SPLINE_COPY);
	private static final Map<String, String> PARTICIPANTS = Map.of(DRIVER, DRIVER_COPY);

	@Test
	void onlyTheGeneratorGrammarDeclaresAParticipant() {
		assertEquals(Optional.of(DRIVER), generator(RING, DRIVER).getDeclaredParticipant());
		assertEquals(Optional.empty(), spline(SPLINE).getDeclaredParticipant());
		for (String family : new String[] {"scalar-state/v1", "segment-point/v1",
				"circular-arc-point/v1", "locus-branch-point/v1"}) {
			assertEquals(Optional.of(DRIVER), descriptor(RING, family
					+ "/true-coordinate/" + DRIVER).getDeclaredParticipant());
		}
		// A generator contract naming a malformed identity fails closed.
		assertThrows(IllegalArgumentException.class,
				() -> descriptor(RING, "circle-point/v1/true-coordinate/geo:C"));
		assertThrows(IllegalArgumentException.class, () -> descriptor(RING,
				"circle-point/v1/true-coordinate/" + DRIVER.toUpperCase()));
		// The selector's external form is the unchanged v1 structural tuple.
		PairSemanticSlotSelector2D selector = selector(generator(RING, DRIVER));
		assertEquals(selector, PairSemanticSlotSelector2D.parse(selector.toExternalForm()));
		assertTrue(selector.toExternalForm().contains("circle-point/v1/true-coordinate/"
				+ DRIVER));
	}

	@Test
	void aCopyRemapsTheParticipantWithItsSourceAndFailsClosedOtherwise() {
		PairSemanticSlotSelector2D original = selector(generator(RING, DRIVER));
		assertEquals(Set.of(DRIVER), original.getDeclaredParticipants());
		PairSemanticSlotSelector2D copied = original.remap(SOURCES, PARTICIPANTS);
		assertEquals(Set.of(DRIVER_COPY), copied.getDeclaredParticipants());
		// Originals and copies both sort ring before spline, so the germ is kept.
		assertEquals(original.getGerm(), copied.getGerm());
		for (String identity : new String[] {RING, SPLINE, DRIVER}) {
			assertFalse(copied.toExternalForm().contains(identity), identity);
		}
		// Remapping only the sources would keep a link to the original driver.
		assertThrows(IllegalArgumentException.class, () -> original.remapSources(SOURCES));
		assertThrows(IllegalArgumentException.class,
				() -> original.remap(SOURCES, Map.of(geo('9'), DRIVER_COPY)));
		// A spline-only selector needs no participant map, as before.
		PairSemanticSlotSelector2D splines = PairSemanticSlotSelector2D.of(spline(RING),
				spline(SPLINE), 1);
		assertEquals(splines.remapSources(SOURCES), splines.remap(SOURCES, Map.of()));
	}

	@Test
	void participantCorrespondenceIsDerivedOnlyThroughSourceAssociation() {
		PairSemanticSlotSelector2D original = selector(generator(RING, DRIVER));
		PairSemanticSlotSelector2D copied = original.remap(SOURCES, PARTICIPANTS);
		assertEquals(PARTICIPANTS, original.participantCorrespondence(SOURCES, copied));
		// A copied binding that still names the original driver is a cross-link.
		PairSemanticSlotSelector2D crossLinked = PairSemanticSlotSelector2D.of(
				generator(RING_COPY, DRIVER), spline(SPLINE_COPY), copied.getGerm());
		assertThrows(IllegalArgumentException.class,
				() -> original.participantCorrespondence(SOURCES, crossLinked));
		// A participant that appears or disappears is not the same parameterization.
		PairSemanticSlotSelector2D reshaped = PairSemanticSlotSelector2D.of(
				spline(RING_COPY), spline(SPLINE_COPY), copied.getGerm());
		assertThrows(IllegalArgumentException.class,
				() -> original.participantCorrespondence(SOURCES, reshaped));
		// The association never follows sources outside the proved map.
		assertThrows(IllegalArgumentException.class,
				() -> original.participantCorrespondence(Map.of(RING, SPLINE_COPY,
						SPLINE, RING_COPY), copied));
		// Two images of one generator locus share one participant consistently.
		PairSemanticSlotSelector2D shared = PairSemanticSlotSelector2D.of(
				generator(RING, DRIVER), generator(SPLINE, DRIVER), 1);
		PairSemanticSlotSelector2D sharedCopy = shared.remap(SOURCES, PARTICIPANTS);
		assertEquals(PARTICIPANTS, shared.participantCorrespondence(SOURCES, sharedCopy));
		PairSemanticSlotSelector2D split = PairSemanticSlotSelector2D.of(
				generator(RING_COPY, DRIVER_COPY), generator(SPLINE_COPY, geo('d')),
				sharedCopy.getGerm());
		assertThrows(IllegalArgumentException.class,
				() -> shared.participantCorrespondence(SOURCES, split));
	}

	@Test
	void theLedgerRejectsParticipantMapsThatCouldLinkToTheOriginal() {
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		ledger.preparePairSourceCopy(SOURCES, PARTICIPANTS);
		assertThrows(IllegalArgumentException.class,
				() -> ledger.preparePairSourceCopy(SOURCES, Map.of(DRIVER, DRIVER)));
		assertThrows(IllegalArgumentException.class,
				() -> ledger.preparePairSourceCopy(SOURCES, Map.of(DRIVER, RING_COPY)));
		assertThrows(IllegalArgumentException.class,
				() -> ledger.preparePairSourceCopy(SOURCES, Map.of(RING, DRIVER_COPY)));
		assertThrows(IllegalArgumentException.class, () -> ledger.preparePairSourceCopy(
				SOURCES, Map.of(DRIVER, DRIVER_COPY, geo('4'), DRIVER_COPY)));
	}

	private static PairSemanticSlotSelector2D selector(SourceDescriptor generator) {
		return PairSemanticSlotSelector2D.of(generator, spline(SPLINE), 1);
	}

	private static SourceDescriptor generator(String source, String driver) {
		return descriptor(source, "circle-point/v1/true-coordinate/" + driver);
	}

	private static SourceDescriptor spline(String source) {
		return descriptor(source,
				"explicit-numeric-domain/v1/SplineV2 normalized oriented parameter t");
	}

	private static SourceDescriptor descriptor(String source, String contract) {
		return new SourceDescriptor(source, "branch", "branch/component-0",
				Orientation.INCREASING, DomainKind.NON_PERIODIC, contract);
	}

	private static String geo(char digit) {
		return "geo:" + String.valueOf(digit).repeat(32);
	}
}
