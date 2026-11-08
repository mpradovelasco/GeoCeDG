/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.GeoCeDGEuclidianView.indexedIntersectionSlot;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** B04 uses the kernel's live ordered outputs without materializing extra points. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BCu6IndexedIntersectionTest {
	@Test
	void selectedNativeSlotsFollowIndexedCommandAcrossDegeneration() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			G9U1TestApp.eval(app, "c=Circle((0,0),3)");
			G9U1TestApp.eval(app, "k=1");
			G9U1TestApp.eval(app, "n=Segment((-5,k),(5,k))");
			var first = G9U1TestApp.eval(app, "Intersect(c,n)");
			var producer = assertInstanceOf(AlgoIntersectLineConic.class,
					first.getParentAlgorithm());
			GeoPoint[] slots = producer.getIntersectionPoints();
			assertEquals(2, slots.length);
			for (double height : new double[] {1, 2.999, 3, 4, -2, -3, -4, 0}) {
				PreG9BCu0Corpus.setNumber(app, "k", height);
				assertMatchesIndexed(app, slots, height);
			}

			G9U1TestApp.eval(app, "m=Segment((0,k),(5,k))");
			var limited = G9U1TestApp.eval(app, "Intersect(m,c)");
			var limitedProducer = assertInstanceOf(AlgoIntersectLineConic.class,
					limited.getParentAlgorithm());
			GeoPoint[] limitedSlots = limitedProducer.getIntersectionPoints();
			int visible = 0;
			for (int slot = 0; slot < limitedSlots.length; slot++) {
				GeoPoint point = limitedSlots[slot];
				if (point.isDefined()) {
					visible++;
					assertEquals(slot + 1, indexedIntersectionSlot(point));
				} else {
					assertEquals(0, indexedIntersectionSlot(point));
				}
			}
			assertEquals(1, visible);
			return null;
		});
	}

	private static void assertMatchesIndexed(AppGeoCeDG app, GeoPoint[] slots, double height) {
		int visible = 0;
		for (int slot = 0; slot < slots.length; slot++) {
			GeoPoint output = slots[slot];
			int index = slot + 1;
			boolean defined = output.isDefined();
			double x = output.getInhomX();
			double y = output.getInhomY();
			assertEquals(defined ? index : 0, indexedIntersectionSlot(output));
			GeoPoint indexed = assertInstanceOf(GeoPoint.class,
					G9U1TestApp.eval(app, "Intersect(n,c," + index + ")"));
			assertEquals(defined, indexed.isDefined(), "definedness at height " + height);
			if (defined) {
				visible++;
				assertEquals(x, indexed.getInhomX(), 1e-10);
				assertEquals(y, indexed.getInhomY(), 1e-10);
			}
			indexed.remove();
		}
		if (Math.abs(height) > 3) {
			assertEquals(0, visible);
		} else if (Math.abs(height) < 3) {
			assertEquals(2, visible);
		} else {
			assertTrue(visible >= 1, "tangent point remains available");
		}
	}

	@Test
	void rejectsSingleAndRichOutputs() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			for (String command : List.of("c=Circle((0,0),3)", "n=Line((-5,0),(5,0))",
					"P=Point(c)", "Q=P+(0,1)", "L=LocusV2(Q,P)")) {
				G9U1TestApp.eval(app, command);
			}
			assertEquals(0, indexedIntersectionSlot(G9U1TestApp.eval(app, "Intersect(c,n,1)")));
			var rich = assertInstanceOf(GeoLocusIntersectionResult.class,
					G9U1TestApp.eval(app, "Intersect(L,n)"));
			assertEquals(0, indexedIntersectionSlot(rich));
			return null;
		});
	}
}
