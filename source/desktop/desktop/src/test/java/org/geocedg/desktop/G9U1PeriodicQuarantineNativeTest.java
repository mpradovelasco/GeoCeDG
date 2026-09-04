/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTokenLedger2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Native R4 single-locus periodic quarantine, not the distinct R1 pair lifecycle. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1PeriodicQuarantineNativeTest {

	@Test
	void actualPeriodicQuarantineRoundTripsAndReactivatesOriginalPoints(
			@TempDir Path directory) throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		for (String command : new String[] {"a=0.2", "u=0",
				"Q=(cos(2*u+a),sin(2*u+a))", "D={true,{0,2*pi,true,false}}",
				"L=LocusV2(Q,u,D)", "axis:y=0", "R=Intersect(L,axis)"}) {
			eval(app, command);
		}
		GeoLocusIntersectionResult rich = (GeoLocusIntersectionResult) lookup(app, "R");
		List<String> tokens = eligible(rich);
		assertEquals(4, tokens.size(), rich.getIntersectionResult().toString());
		Map<String, String> ownership = new LinkedHashMap<>();
		Map<String, PersistentGeoId> identities = new LinkedHashMap<>();
		for (int index = 0; index < tokens.size(); index++) {
			// Labels are test presentation only; each parent consumes its exact token.
			String label = "X" + index;
			GeoPoint point = LocusV2PublicOperations.selectIntersectionPoint(
					app.getKernel().getConstruction(), label, rich,
					new GeoText(app.getKernel().getConstruction(), tokens.get(index)));
			assertTrue(point.isDefined());
			ownership.put(label, tokens.get(index));
			identities.put(label, app.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(point));
		}
		final long count = points(app);
		move(app, 0.25);
		ownership.keySet().forEach(label -> assertTrue(lookup(app, label).isDefined()));
		move(app, 0.2);
		move(app, 0.2 + Math.PI);
		assertEquals(4, rich.getIntersectionResult().getFiniteSolutions().size());
		assertEquals(4, claimedQuarantineCount(rich), rich.getTokenLedgerState());
		ownership.keySet().forEach(label -> assertFalse(lookup(app, label).isDefined()));
		assertEquals(count, points(app));
		Path file = directory.resolve("r4-periodic-quarantine.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(file.toFile(), false));
		GeoLocusIntersectionResult restored = (GeoLocusIntersectionResult) lookup(reopened, "R");
		assertEquals(4, claimedQuarantineCount(restored));
		GeoCeDGIntersectionSession session = new GeoCeDGIntersectionSession(reopened);
		session.activate(restored);
		assertTrue(session.markerSolutions().isEmpty());
		assertTrue(session.eligibleTokens().isEmpty());
		assertTrue(session.materializeAll(true).isEmpty());
		assertEquals(count, points(reopened));
		for (String label : ownership.keySet()) {
			GeoPoint point = (GeoPoint) lookup(reopened, label);
			assertFalse(point.isDefined());
			assertEquals(identities.get(label), reopened.getKernel().getConstruction()
					.getSpatialIdentityRegistry().getPersistentGeoId(point));
			assertEquals(ownership.get(label), ((AlgoLocusIntersectionPointV2)
					point.getParentAlgorithm()).getSelectedRootToken());
		}
		move(reopened, 0.2);
		assertEquals(0, claimedQuarantineCount(restored));
		for (String label : ownership.keySet()) {
			assertTrue(lookup(reopened, label).isDefined());
			assertEquals(identities.get(label), reopened.getKernel().getConstruction()
					.getSpatialIdentityRegistry().getPersistentGeoId(lookup(reopened, label)));
		}
		assertEquals(count, points(reopened));
		Path active = directory.resolve("r4-reactivated.cedg");
		assertTrue(((GuiManagerGeoCeDG) reopened.getGuiManager()).saveAsTo(active.toFile()));
		AppGeoCeDG reactivated = G9U1TestApp.create();
		assertTrue(reactivated.loadFile(active.toFile(), false));
		for (String label : ownership.keySet()) {
			assertTrue(lookup(reactivated, label).isDefined());
			assertEquals(ownership.get(label), ((AlgoLocusIntersectionPointV2)
					lookup(reactivated, label).getParentAlgorithm()).getSelectedRootToken());
		}
		assertEquals(count, points(reactivated));
	}

	private static long claimedQuarantineCount(GeoLocusIntersectionResult rich) {
		String state = rich.getTokenLedgerState();
		new LocusIntersectionTokenLedger2D().importState(state);
		String[] fields = state.split("\\|", -1);
		int statusField = "5".equals(fields[0]) ? 1 : 0;
		return Arrays.stream(fields[2].split("~", -1)).skip(5)
				.filter(entry -> "r".equals(entry.split(",", -1)[statusField])).count();
	}

	private static List<String> eligible(GeoLocusIntersectionResult rich) {
		return rich.getIntersectionResult().getFiniteSolutions().stream()
				.map(root -> root.getIdentity().getRootToken())
				.filter(rich::isPointAdmissible).toList();
	}

	private static long points(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
				.filter(GeoPoint.class::isInstance).count();
	}

	private static void move(AppGeoCeDG app, double value) {
		GeoNumeric parameter = (GeoNumeric) lookup(app, "a");
		parameter.setValue(value);
		parameter.updateCascade();
	}
}
