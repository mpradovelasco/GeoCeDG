/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTokenLedger2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.GeoGebraConstants;
import org.geogebra.common.jre.io.MyXMLioJre;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.main.App;
import org.geogebra.common.main.AppConfig;
import org.geogebra.common.main.settings.config.AppConfigDefault;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.headless.AppDNoGui;
import org.geogebra.desktop.headless.GFileHandler;
import org.geogebra.desktop.io.AtomicDocumentFileWriter;
import org.geogebra.desktop.main.LocalizationD;
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
		// U1-Q02 declares a second native reopen while the periodic offset
		// evidence is still insufficient: quarantine must survive unchanged
		// rather than resolve, so the bound test performs it before release.
		Path unresolved = directory.resolve("r4-unresolved-second.cedg");
		assertTrue(((GuiManagerGeoCeDG) reopened.getGuiManager())
				.saveAsTo(unresolved.toFile()));
		AppGeoCeDG twice = G9U1TestApp.create();
		assertTrue(twice.loadFile(unresolved.toFile(), false));
		GeoLocusIntersectionResult second =
				(GeoLocusIntersectionResult) lookup(twice, "R");
		assertEquals(restored.getTokenLedgerState(), second.getTokenLedgerState());
		assertEquals(4, claimedQuarantineCount(second));
		GeoCeDGIntersectionSession secondSession = new GeoCeDGIntersectionSession(twice);
		secondSession.activate(second);
		assertTrue(secondSession.markerSolutions().isEmpty());
		assertTrue(secondSession.eligibleTokens().isEmpty());
		assertTrue(secondSession.materializeAll(true).isEmpty());
		assertEquals(count, points(twice));
		for (String label : ownership.keySet()) {
			GeoPoint dormant = (GeoPoint) lookup(twice, label);
			assertFalse(dormant.isDefined());
			assertEquals(identities.get(label), twice.getKernel().getConstruction()
					.getSpatialIdentityRegistry().getPersistentGeoId(dormant));
			assertEquals(ownership.get(label), ((AlgoLocusIntersectionPointV2)
					dormant.getParentAlgorithm()).getSelectedRootToken());
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

	@Test
	void provedNonzeroOffsetRetiresOnlyItsGroupAcrossTheNativeArchive(
			@TempDir Path directory) throws Exception {
		Quarantined seed = quarantinedSeed(directory, "r4-nonzero-seed.cedg");
		AppGeoCeDG retiring = G9U1TestApp.create();
		assertTrue(retiring.loadFile(seed.file.toFile(), false));
		GeoLocusIntersectionResult rich =
				(GeoLocusIntersectionResult) lookup(retiring, "R");
		assertEquals(4, claimedQuarantineCount(rich));
		assertEquals(List.of(), defined(retiring, seed));

		// A proved unique nonzero cyclic offset is typed monodromy: only the
		// affected allocations retire, and they never retarget another root.
		move(retiring, 0.2 - Math.PI / 4);
		List<String> survivors = defined(retiring, seed);
		assertFalse(survivors.isEmpty(), rich.getTokenLedgerState());
		assertNotEquals(List.copyOf(seed.ownership.keySet()), survivors);
		assertSameTokensAndIdentities(retiring, seed);
		assertEquals(seed.points, points(retiring));

		Path retired = directory.resolve("r4-retired.cedg");
		assertTrue(((GuiManagerGeoCeDG) retiring.getGuiManager())
				.saveAsTo(retired.toFile()));
		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(retired.toFile(), false));
		GeoLocusIntersectionResult restored =
				(GeoLocusIntersectionResult) lookup(reopened, "R");
		// Reopening legitimately re-derives fresh unclaimed allocations for the
		// current roots the retired consumers no longer own, so the serialized
		// ledger is not byte-equal here. Quarantine is nevertheless fully
		// resolved, and no retired token is reused by any surviving consumer.
		assertEquals(0, claimedQuarantineCount(restored));
		assertEquals(survivors, defined(reopened, seed));
		assertSameTokensAndIdentities(reopened, seed);
		assertEquals(seed.points, points(reopened));

		// The same parameter that released the whole group from the unique-zero
		// path cannot revive a retired allocation: retirement fails closed.
		move(reopened, 0.2);
		assertEquals(survivors, defined(reopened, seed));
		assertSameTokensAndIdentities(reopened, seed);
		assertEquals(seed.points, points(reopened));

		Path durable = directory.resolve("r4-retired-origin.cedg");
		assertTrue(((GuiManagerGeoCeDG) reopened.getGuiManager())
				.saveAsTo(durable.toFile()));
		AppGeoCeDG again = G9U1TestApp.create();
		assertTrue(again.loadFile(durable.toFile(), false));
		assertEquals(survivors, defined(again, seed));
		assertSameTokensAndIdentities(again, seed);
		assertEquals(seed.points, points(again));
	}

	@Test
	void featureOffAndClassicPreserveNativeQuarantineWithoutCreation(
			@TempDir Path directory) throws Exception {
		Quarantined seed = quarantinedSeed(directory, "r4-preservation-seed.cedg");
		assertPreservesQuarantine(seed, directory, "feature-off",
				() -> new AppConfigGeoCeDG(false));
		assertPreservesQuarantine(seed, directory, "classic", AppConfigDefault::new);
	}

	/** One byte-identical quarantined native seed, forked by the closure fixtures. */
	private static final class Quarantined {
		private final Path file;
		private final Map<String, String> ownership;
		private final Map<String, PersistentGeoId> identities;
		private final String ledger;
		private final long points;

		private Quarantined(Path file, Map<String, String> ownership,
				Map<String, PersistentGeoId> identities, String ledger, long points) {
			this.file = file;
			this.ownership = ownership;
			this.identities = identities;
			this.ledger = ledger;
			this.points = points;
		}
	}

	private static Quarantined quarantinedSeed(Path directory, String name)
			throws Exception {
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
			String label = "X" + index;
			GeoPoint point = LocusV2PublicOperations.selectIntersectionPoint(
					app.getKernel().getConstruction(), label, rich,
					new GeoText(app.getKernel().getConstruction(), tokens.get(index)));
			assertTrue(point.isDefined());
			ownership.put(label, tokens.get(index));
			identities.put(label, app.getKernel().getConstruction()
					.getSpatialIdentityRegistry().getPersistentGeoId(point));
		}
		long count = points(app);
		move(app, 0.25);
		move(app, 0.2);
		move(app, 0.2 + Math.PI);
		assertEquals(4, claimedQuarantineCount(rich), rich.getTokenLedgerState());
		Path file = directory.resolve(name);
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		return new Quarantined(file, ownership, identities,
				rich.getTokenLedgerState(), count);
	}

	private static void assertPreservesQuarantine(Quarantined seed, Path directory,
			String route, Supplier<AppConfig> configFactory) throws Exception {
		AppDNoGui app = reopenHeadless(seed.file, configFactory.get());
		assertEquals(seed.ledger, ledgerOf(app), route);
		assertFalse(RuntimeFeatureService.mayCreateLocusV2(
				app.getKernel().getConstruction()), route);
		assertEquals(GeoGebraConstants.CLASSIC_APPCODE,
				app.getConfig().getAppCode(), route);
		String preservedXml = app.getXML();
		int geoCount = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size();
		int identityCount = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getRecords().size();
		assertQuarantinedDormant(app, seed, route);

		Path resaved = directory.resolve(route + "-quarantine-resaved.cedg");
		writeHeadless(app, resaved);
		AppDNoGui reopened = reopenHeadless(resaved, configFactory.get());
		assertEquals(preservedXml, reopened.getXML(), route);
		assertEquals(seed.ledger, ledgerOf(reopened), route);
		assertEquals(geoCount, reopened.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size(), route);
		assertEquals(identityCount, reopened.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getRecords().size(), route);
		assertQuarantinedDormant(reopened, seed, route);
	}

	private static void assertQuarantinedDormant(App app, Quarantined seed,
			String route) {
		for (Map.Entry<String, String> owned : seed.ownership.entrySet()) {
			GeoPoint point = (GeoPoint) find(app, owned.getKey());
			assertFalse(point.isDefined(), route);
			assertEquals(owned.getValue(), ((AlgoLocusIntersectionPointV2)
					point.getParentAlgorithm()).getSelectedRootToken(), route);
			assertEquals(seed.identities.get(owned.getKey()),
					app.getKernel().getConstruction().getSpatialIdentityRegistry()
							.getPersistentGeoId(point), route);
		}
	}

	private static String ledgerOf(App app) {
		return ((GeoLocusIntersectionResult) find(app, "R")).getTokenLedgerState();
	}

	private static List<String> defined(AppGeoCeDG app, Quarantined seed) {
		List<String> labels = new ArrayList<>();
		for (String label : seed.ownership.keySet()) {
			if (lookup(app, label).isDefined()) {
				labels.add(label);
			}
		}
		return labels;
	}

	private static void assertSameTokensAndIdentities(AppGeoCeDG app,
			Quarantined seed) {
		for (Map.Entry<String, String> owned : seed.ownership.entrySet()) {
			GeoPoint point = (GeoPoint) lookup(app, owned.getKey());
			assertEquals(owned.getValue(), ((AlgoLocusIntersectionPointV2)
					point.getParentAlgorithm()).getSelectedRootToken());
			assertEquals(seed.identities.get(owned.getKey()),
					app.getKernel().getConstruction().getSpatialIdentityRegistry()
							.getPersistentGeoId(point));
		}
	}

	private static GeoElement find(App app, String label) {
		GeoElement geo = app.getKernel().lookupLabel(label);
		assertNotNull(geo, label);
		return geo;
	}

	private static AppDNoGui reopenHeadless(Path source, AppConfig config)
			throws Exception {
		Log previousLogger = Log.getLogger();
		AppDNoGui reopened;
		try {
			reopened = new AppDNoGui(new LocalizationD(3), true, config);
		} finally {
			Log.setLogger(previousLogger);
		}
		assertTrue(GFileHandler.loadXML(reopened, Files.newInputStream(source),
				false));
		return reopened;
	}

	private static void writeHeadless(AppDNoGui app, Path target) throws Exception {
		AtomicDocumentFileWriter.write(target, temporary ->
				((MyXMLioJre) app.getXMLio()).writeGeoGebraFile(temporary.toFile()));
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
