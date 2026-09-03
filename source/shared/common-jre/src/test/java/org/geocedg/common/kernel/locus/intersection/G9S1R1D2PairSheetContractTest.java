/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * D2 design-contract model only: no production certifier, selector, ledger,
 * GeoPoint, copy transaction or native persistence is implemented or tested.
 * Every ProofFixture explicitly ASSUMES its whole-component-product coverage
 * and regularity facts. These Boolean fixtures are not numerical certificates.
 *
 * <p>The analytic control F_t(u)=(t,u), G(v)=(v^2-1,v^3-v) has exactly two
 * roots v=+-sqrt(1+t), u=t*v, for |t|<=0.1 on the stated finite domains.
 * det D(F-G)=2v gives a unique root of each sign even when their u ranks swap.
 * Sampled arithmetic below checks this derivation, not an interval proof.
 * The admitted structural slot is the unique CURRENT root of its canonical
 * germ. Recurrence is not a claim of historical trajectory continuation.
 */
final class G9S1R1D2PairSheetContractTest {

	private static final String FIRST = "branch-a/component-a/increasing";
	private static final String SECOND = "branch-b/component-b/increasing";
	private static final double EPSILON = 1E-12;

	@Test
	void uniqueGermsSurviveProjectedRankExchange() {
		for (double t : new double[] {-0.1, 0, 0.1}) {
			for (int sign : new int[] {-1, 1}) {
				double v = sign * Math.sqrt(1 + t);
				double u = t * v;
				assertTrue(u >= -1 && u <= 1 && v >= -1.25 && v <= 1.25);
				assertEquals(t, v * v - 1, EPSILON);
				assertEquals(u, v * v * v - v, EPSILON);
				assertEquals(sign, (int) Math.signum(2 * v));
				ProofFixture proof = unique(key(sign), u, v);
				assertTrue(proof.eligible());
				assertEquals(key(sign), proof.key);
			}
		}
		assertTrue(-0.1 * Math.sqrt(0.9) < 0);
		assertTrue(0.1 * Math.sqrt(1.1) > 0);
		assertNotEquals(key(-1), key(1));
	}

	@Test
	void operandReversalPreservesSelectorsAcrossOtherProjectedRankExchange() {
		for (double t : new double[] {-0.1, 0, 0.1}) {
			for (int sign : new int[] {-1, 1}) {
				double v = sign * Math.sqrt(1 + t);
				double u = t * v;
				Key reversed = canonical("query", "source-b", SECOND,
						"source-a", FIRST, -sign);
				assertEquals(key(sign), reversed);
				// Reversed coordinates are (v,u); its second projection changes order.
				assertEquals(sign, (int) -Math.signum(-2 * v));
				assertEquals(u, t * v, 0);
			}
		}
	}

	@Test
	void chartsKnotsParametersAndEnumerationDoNotEnterTheKey() {
		Key selector = key(1);
		ProofFixture before = unique(selector, 0.2, 0.8);
		ProofFixture after = unique(selector, 0.8, 0.2);
		assertEquals(before.key, after.key);
		assertNotEquals(before.u, after.u);
		for (String forbidden : List.of("chart", "box", "rank", "parameter", "history")) {
			assertFalse(selector.encode().contains(forbidden));
		}
		// These distinct current witnesses may have different knot owners/boxes.
		// Neither witness data nor a chart label is a field of the structural key.
		List<Key> reversed = new ArrayList<>(List.of(key(-1), key(1)));
		Collections.reverse(reversed);
		assertEquals(selector, reversed.get(0));
	}

	@Test
	void collisionAndMissingCoverageNeverAllocateModelPoints() {
		ProofFixture collision = fixture(key(1), true, 2, true, 0.2, 0.3);
		ProofFixture missingCoverage = fixture(key(1), false, 1, true, 0.2, 0.3);
		assertNull(materialize(collision));
		assertNull(materialize(missingCoverage));
		ModelPoint existing = materialize(unique(key(1), 0.1, 0.2));
		assertNotNull(existing);
		ModelPoint same = existing;
		existing.apply(collision);
		assertFalse(existing.defined);
		existing.apply(missingCoverage);
		assertFalse(existing.defined);
		assertSame(same, existing);
	}

	@Test
	void dormantSlotReactivatesIdenticallyAcrossDifferentHistories() {
		ProofFixture initial = unique(key(1), 0.1, 0.2);
		ProofFixture finish = unique(key(1), 0.4, 0.6);
		List<List<ProofFixture>> histories = List.of(
				List.of(finish),
				List.of(fixture(key(1), true, 2, true, 0, 0), finish),
				List.of(fixture(key(1), false, 1, true, 0, 0),
						fixture(key(1), true, 0, true, 0, 0), finish));
		for (List<ProofFixture> history : histories) {
			ModelPoint point = materialize(initial);
			assertNotNull(point);
			ModelPoint original = point;
			for (ProofFixture proof : history) {
				point.apply(proof);
			}
			assertSame(original, point);
			assertTrue(point.defined);
			assertEquals("existing-point", point.id);
			assertEquals("opaque-design-token", point.token);
			assertEquals(finish.u, point.u, 0);
			assertEquals(finish.v, point.v, 0);
		}
	}

	@Test
	void wrongSourcesOrLineageCannotRetargetExistingPoint() {
		ModelPoint point = materialize(unique(key(1), 0.1, 0.2));
		assertNotNull(point);
		Key wrongSource = canonical("query", "other-source", FIRST,
				"source-b", SECOND, 1);
		Key wrongLineage = canonical("query", "source-a", FIRST + "/redefined",
				"source-b", SECOND, 1);
		for (Key wrong : List.of(wrongSource, wrongLineage, key(-1))) {
			point.apply(unique(wrong, 0.3, 0.4));
			assertFalse(point.defined);
			assertEquals(key(1), point.key);
			assertEquals("opaque-design-token", point.token);
		}
	}

	@Test
	void explicitCopyMapRenormalizesGermWhenCanonicalIdsReverse() {
		// Assume an exact provenance map a->z-copy and b->a-copy, not label matching.
		Key copied = canonical("copied-query", "z-copy", FIRST,
				"a-copy", SECOND, 1);
		Key reversedView = canonical("copied-query", "a-copy", SECOND,
				"z-copy", FIRST, -1);
		assertEquals(copied, reversedView);
		assertEquals(-1, copied.germ);
		assertEquals(SECOND, copied.lowLineage);
		assertEquals(FIRST, copied.highLineage);
		assertNotEquals(key(1), copied);
	}

	@Test
	void singleTraversalSeamNeedsNoRetirementWithUniqueCurrentGerm() {
		Key periodic = canonical("query", "source-a", FIRST + "/periodic",
				"source-b", SECOND, 1);
		ModelPoint point = materialize(unique(periodic, 0.98, 0.5));
		assertNotNull(point);
		ModelPoint original = point;
		for (double canonicalU : new double[] {0.02, 0.07, 0.02, 0.98}) {
			// This fixture ASSUMES proved regular seam glue and singleton coverage.
			// It does not run a spline inverse solver or infer identity from proximity.
			point.apply(unique(periodic, canonicalU, 0.5));
			assertTrue(point.defined);
			assertSame(original, point);
			assertEquals(canonicalU, point.u, 0);
			assertEquals(periodic, point.key);
		}
	}

	@Test
	void repeatedMonodromyOrbitIsOutsideScopeRegardlessOfHistory() {
		for (String history : List.of("no-op", "forward", "inverse", "two-loops")) {
			// The proved double-traversal witness has two current roots of this germ.
			// History is a test annotation, never an input of eligibility or the key.
			assertFalse(history.isEmpty());
			ProofFixture repeated = fixture(key(1), true, 2, true, 0.1, 0.5);
			assertFalse(repeated.eligible());
			assertNull(materialize(repeated));
		}
	}

	@Test
	void diagnosticReloadRechecksCoverageInsteadOfTrustingActiveStatus() {
		ModelPoint original = materialize(unique(key(1), 0.1, 0.2));
		assertNotNull(original);
		String snapshot = original.id + "\n" + original.token + "\n"
				+ original.key.encode() + "\nclaimed-active=true";
		String[] fields = snapshot.split("\n");
		ModelPoint restored = new ModelPoint(fields[0], fields[1], Key.decode(fields[2]));
		restored.apply(fixture(key(1), false, 1, true, 0.1, 0.2));
		assertFalse(restored.defined);
		restored.apply(unique(key(1), 0.3, 0.4));
		assertTrue(restored.defined);
		assertEquals(original.id, restored.id);
		assertEquals(original.token, restored.token);
		assertEquals(original.key, restored.key);
		// Test-local text only: neither a ledger format nor native .cedg evidence.
	}

	@Test
	void oppositeGermRootsDoNotCompeteWithTheCertifiedSingleton() {
		ProofFixture selected = new ProofFixture(key(1), true, 1, 2, true, 0, 0);
		assertEquals(2, selected.opposite);
		assertTrue(selected.eligible());
		assertNotNull(materialize(selected));
		assertFalse(new ProofFixture(key(-1), true, 2, 1, true, 0, 0).eligible());
		assertFalse(fixture(key(1), true, 1, false, 0, 0).eligible());
		// Actual local/global numerical evidence is unchanged by this slot policy.
	}

	@Test
	void diagnosticContractTraceIsEnumerationIndependent()
			throws NoSuchAlgorithmException {
		List<ProofFixture> fixtures = new ArrayList<>(List.of(
				unique(key(1), 0.2, 0.8), unique(key(-1), 0.8, 0.2),
				fixture(key(1), false, 1, true, 0.2, 0.8),
				fixture(key(1), true, 2, true, 0.2, 0.8),
				fixture(key(1), true, 0, true, 0, 0),
				fixture(key(1), true, 1, false, 0.2, 0.8)));
		String forward = trace(fixtures);
		Collections.reverse(fixtures);
		assertEquals(forward, trace(fixtures));
		String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
				.digest(forward.getBytes(StandardCharsets.UTF_8)));
		System.out.println("G9S1_R1_D2_CONTRACT_TRACE_SHA256=" + hash);
	}

	private static Key key(int sign) {
		return canonical("query", "source-a", FIRST, "source-b", SECOND, sign);
	}

	private static Key canonical(String query, String firstId, String firstLineage,
			String secondId, String secondLineage, int germ) {
		if (Math.abs(germ) != 1 || firstId.equals(secondId)) {
			throw new IllegalArgumentException("Distinct sources and transverse sign required");
		}
		return firstId.compareTo(secondId) < 0
				? new Key(query, firstId, firstLineage, secondId, secondLineage, germ)
				: new Key(query, secondId, secondLineage, firstId, firstLineage, -germ);
	}

	private static ProofFixture unique(Key key, double u, double v) {
		return fixture(key, true, 1, true, u, v);
	}

	private static ProofFixture fixture(Key key, boolean coverage, int matching,
			boolean regular, double u, double v) {
		return new ProofFixture(key, coverage, matching, 0, regular, u, v);
	}

	private static ModelPoint materialize(ProofFixture proof) {
		if (!proof.eligible()) {
			return null;
		}
		ModelPoint point = new ModelPoint("existing-point", "opaque-design-token", proof.key);
		point.apply(proof);
		return point;
	}

	private static String trace(List<ProofFixture> fixtures) {
		List<String> records = new ArrayList<>();
		for (ProofFixture proof : fixtures) {
			records.add(proof.key.encode() + "|coverage=" + proof.coverage
					+ "|matching=" + proof.matching + "|opposite=" + proof.opposite
					+ "|regular=" + proof.regular
					+ "|eligible=" + proof.eligible() + "|u="
					+ Long.toHexString(Double.doubleToRawLongBits(proof.u)) + "|v="
					+ Long.toHexString(Double.doubleToRawLongBits(proof.v)));
		}
		// Sorting is deterministic evidence encoding ONLY, never root identity.
		Collections.sort(records);
		return "d2-assumed-contract-fixtures/v1\n" + String.join("\n", records) + "\n";
	}

	private record Key(String query, String lowId, String lowLineage,
			String highId, String highLineage, int germ) {
		private String encode() {
			return "pair-singleton-germ/v1|" + query + "|" + lowId + "|" + lowLineage
					+ "|" + highId + "|" + highLineage + "|" + germ;
		}

		private static Key decode(String encoded) {
			String[] fields = encoded.split("\\|");
			if (fields.length != 7 || !"pair-singleton-germ/v1".equals(fields[0])) {
				throw new IllegalArgumentException("Unexpected diagnostic key format");
			}
			return new Key(fields[1], fields[2], fields[3], fields[4], fields[5],
					Integer.parseInt(fields[6]));
		}
	}

	private record ProofFixture(Key key, boolean coverage, int matching, int opposite,
			boolean regular, double u, double v) {
		private boolean eligible() {
			return coverage && matching == 1 && regular;
		}
	}

	private static final class ModelPoint {
		private final String id;
		private final String token;
		private final Key key;
		private boolean defined;
		private double u = Double.NaN;
		private double v = Double.NaN;

		private ModelPoint(String id, String token, Key key) {
			this.id = id;
			this.token = token;
			this.key = key;
		}

		private void apply(ProofFixture proof) {
			defined = key.equals(proof.key) && proof.eligible();
			u = defined ? proof.u : Double.NaN;
			v = defined ? proof.v : Double.NaN;
		}
	}
}
