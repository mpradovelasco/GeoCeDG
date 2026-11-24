/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDeclaredPieceIdentity;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * B4 of the PRE-G9B-MAINT-P0 limitations plan (decision D-B-4, test only): pieces of a driver
 * domain governed by sliders have no declarative identity
 * ({@code locus-v2-existence-components.md} section 20.2: a slider has a label and no durable
 * identity, and a label is no identity). Each is a place of its list, with the positional
 * meaning that D2 approves, and section 20.6 declares that redefining the list can change
 * which piece stands at a place. This test fixes what a redefinition that moves a slider
 * piece to another place does to the root tokens; nothing in the product changes.
 *
 * <p>The source {@code LA} is the line {@code y = 1} traced by {@code m}; the spline
 * {@code SP} crosses it going up at about 0.174 and again at about 2.826. The piece
 * {@code [a, a + wa] = [0.1, 0.9]} contains the first root, {@code [b, b + wb] =
 * [2.3, 2.95]} the second, and {@code [c, c + wc] = [-2, -1.6]} none.</p>
 */
final class PreG9BMaintB4SliderPieceReorderingTest extends PreG9bR3RedefineTestBase {
	private static final double LOW = 0.174;
	private static final double HIGH = 2.826;
	private static final String A_THEN_B = "{false,{a,a+wa,true,true},{b,b+wb,true,true}}";
	private static final String C_THEN_A = "{false,{c,c+wc,true,true},{a,a+wa,true,true}}";

	private GeoLocusIntersectionResult rich() {
		return (GeoLocusIntersectionResult) requireLookup("RAB");
	}

	private String tokenAt(double x) {
		for (LocusIntersectionSolution2D root : rich().getIntersectionResult()
				.getFiniteSolutions()) {
			if (Math.abs(root.getEvaluatedPoint().getX() - x) < 1e-3) {
				return root.getIdentity().getRootToken();
			}
		}
		return null;
	}

	private static String selectedToken(GeoPoint point) {
		return ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm())
				.getSelectedRootToken();
	}

	private void redefineList(String list) {
		Outcome outcome = redefine("DLA", list, PreG9bR3RedefineTestBase::retainWhenAvailable);
		assertTrue(outcome.succeeded(), outcome.errors());
	}

	private GeoPoint point(String label) {
		return (GeoPoint) requireLookup(label);
	}

	@Test
	void aSliderPieceThatMovesToAnotherPlaceGivesThatPlaceItsRoots() {
		add("a=0.1");
		add("wa=0.8");
		add("b=2.3");
		add("wb=0.65");
		add("c=-2");
		add("wc=0.4");
		add("m=0");
		add("QA=(m,1)");
		add("DLA=" + A_THEN_B);
		GeoLocusV2 locus = add("LA=LocusV2(QA,m,DLA)");
		assertNotNull(locus);
		add("SA=(0,0)");
		add("SB=(1,4)");
		add("SC=(2,-2)");
		add("SD=(3,2)");
		add("SP=SplineV2({SA,SB,SC,SD},3)");
		add("RAB=Intersect(LA,SP)");
		var registry = getConstruction().getSpatialIdentityRegistry();
		for (String identity : LocusDeclaredPieceIdentity.of((GeoList) requireLookup("DLA"),
				registry)) {
			assertNull(identity, "a piece declared by sliders is a place of its list");
		}
		String first = tokenAt(LOW);
		String second = tokenAt(HIGH);
		assertNotNull(first);
		assertNotNull(second);
		add("X=Intersect(RAB,\"" + first + "\")");
		add("Y=Intersect(RAB,\"" + second + "\")");
		assertEquals(LOW, point("X").getInhomX(), 1e-3);
		assertEquals(HIGH, point("Y").getInhomX(), 1e-3);
		assertEquals(4, rich().getTokenLedgerState().split("\\|", -1).length,
				"no frame is kept for a place");

		// The piece [a, a + wa] moves from the first place to the second; the piece of c takes
		// the first place and [b, b + wb] leaves the list. The tokens follow their places.
		redefineList(C_THEN_A);
		assertFalse(point("X").isDefined(), "the first place now holds a piece without a root");
		assertEquals(first, selectedToken(point("X")), "X keeps its token");
		assertTrue(point("Y").isDefined(), "the second place holds a root again");
		assertEquals(LOW, point("Y").getInhomX(), 1e-3,
				"the token of the second place names the root of the piece now there");
		assertEquals(second, selectedToken(point("Y")));
		assertEquals(second, tokenAt(LOW));
		assertNull(tokenAt(HIGH));
		assertEquals(4, rich().getTokenLedgerState().split("\\|", -1).length,
				"no frame is kept for a place");

		// The first list again: each place holds its first piece and each token its root.
		redefineList(A_THEN_B);
		assertTrue(point("X").isDefined());
		assertEquals(LOW, point("X").getInhomX(), 1e-3);
		assertEquals(first, selectedToken(point("X")));
		assertTrue(point("Y").isDefined());
		assertEquals(HIGH, point("Y").getInhomX(), 1e-3);
		assertEquals(second, selectedToken(point("Y")));
	}
}
