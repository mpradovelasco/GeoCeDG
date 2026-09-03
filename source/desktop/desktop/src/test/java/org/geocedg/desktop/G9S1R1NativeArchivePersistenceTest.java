/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTokenLedger2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTokenLedger2D.PairBindingState;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.util.LoggerD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Actual native archives, not an XML-only proxy for pair token persistence. */
class G9S1R1NativeArchivePersistenceTest {

	@BeforeAll
	static void initializeDesktop() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
	}

	@Test
	void nativeCedgActiveDormantAndReactivatedPairPreservesExactOwnership(
			@TempDir Path directory) throws Exception {
		AppGeoCeDG app = enabledApp();
		for (String command : new String[] {"h=0", "A=(-2,h)", "B=(-2/3,h)",
				"C=(2/3,h)", "D=(2,h)", "S=SplineV2({A,B,C,D},3)",
				"E=(0,-2)", "F=(0,-2/3)", "G=(0,2/3)", "H=(0,2)",
				"T=SplineV2({E,F,G,H},3)", "R=Intersect(S,T)"}) {
			eval(app, command);
		}
		GeoLocusIntersectionResult rich = (GeoLocusIntersectionResult) lookup(app, "R");
		String token = rich.getIntersectionResult().getFiniteSolutions().stream()
				.map(root -> root.getIdentity().getRootToken())
				.filter(rich::isPointAdmissible).findFirst().orElseThrow();
		GeoText text = new GeoText(app.getKernel().getConstruction(), token);
		text.setAuxiliaryObject(true);
		text.setEuclidianVisible(false);
		GeoPoint point = LocusV2PublicOperations.selectIntersectionPoint(
				app.getKernel().getConstruction(), "X", rich, text);
		assertTrue(point.isDefined());
		Map<String, PersistentGeoId> ids = identities(app);
		for (double height : new double[] {0, 9, 0, 0.25}) {
			GeoNumeric h = (GeoNumeric) lookup(app, "h");
			h.setValue(height);
			h.updateCascade();
			assertEquals(height != 9, lookup(app, "X").isDefined());
			String ledger = ((GeoLocusIntersectionResult) lookup(app, "R")).getTokenLedgerState();
			Path file = directory.resolve("pair-" + height + ".cedg");
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
			AppGeoCeDG reopened = enabledApp();
			assertTrue(reopened.loadFile(file.toFile(), false));
			assertEquals(ids, identities(reopened));
			GeoPoint restored = (GeoPoint) lookup(reopened, "X");
			assertEquals(height != 9, restored.isDefined());
			assertEquals(token, ((AlgoLocusIntersectionPointV2) restored.getParentAlgorithm())
					.getSelectedRootToken());
			assertEquals(ledger,
					((GeoLocusIntersectionResult) lookup(reopened, "R")).getTokenLedgerState());
			assertFalse(reopened.getXML().contains("KrawczykBox"));
			app = reopened;
		}
	}

	@Test
	void nativeCedgPairQuarantineRestoresCurrentMultiplicityAndReactivatesSamePoint(
			@TempDir Path directory) throws Exception {
		AppGeoCeDG app = enabledApp();
		app.getKernel().setContinuous(false);
		for (String command : new String[] {"h=0", "w(x,y)=1", "A=(-3,-1)",
				"B=(-1,-1/3+4*h/3)", "C=(1,1/3-4*h/3)", "D=(3,1)",
				"S=SplineV2({A,B,C,D},3,w)", "E=(-4,0)", "F=(-4/3,0)",
				"G=(4/3,0)", "H=(4,0)", "T=SplineV2({E,F,G,H},3,w)",
				"R=Intersect(S,T)"}) {
			eval(app, command);
		}
		GeoLocusIntersectionResult rich = (GeoLocusIntersectionResult) lookup(app, "R");
		String token = rich.getIntersectionResult().getFiniteSolutions().stream()
				.map(root -> root.getIdentity().getRootToken())
				.filter(rich::isPointAdmissible).findFirst().orElseThrow();
		GeoPoint point = LocusV2PublicOperations.selectIntersectionPoint(
				app.getKernel().getConstruction(), "X", rich,
				new GeoText(app.getKernel().getConstruction(), token));
		assertTrue(point.isDefined());
		final Map<String, PersistentGeoId> ids = identities(app);
		long points = pointCount(app);
		move(app, 1);
		assertEquals(3, rich.getIntersectionResult().getFiniteSolutions().size());
		assertFalse(point.isDefined());
		assertEquals(PairBindingState.QUARANTINED, pairState(rich, token));
		assertEquals(points, pointCount(app));
		String quarantinedLedger = rich.getTokenLedgerState();
		Path quarantine = directory.resolve("pair-quarantined.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(quarantine.toFile()));

		AppGeoCeDG reopened = enabledApp();
		assertTrue(reopened.loadFile(quarantine.toFile(), false));
		assertEquals(ids, identities(reopened));
		GeoPoint restored = (GeoPoint) lookup(reopened, "X");
		GeoLocusIntersectionResult restoredRich =
				(GeoLocusIntersectionResult) lookup(reopened, "R");
		assertFalse(restored.isDefined());
		assertEquals(PairBindingState.QUARANTINED, pairState(restoredRich, token));
		assertEquals(quarantinedLedger, restoredRich.getTokenLedgerState());
		assertEquals(points, pointCount(reopened));
		move(reopened, 0);
		assertTrue(restored.isDefined());
		assertEquals(PairBindingState.ACTIVE, pairState(restoredRich, token));
		assertEquals(token, ((AlgoLocusIntersectionPointV2) restored.getParentAlgorithm())
				.getSelectedRootToken());
		assertEquals(ids, identities(reopened));
		assertEquals(points, pointCount(reopened));

		Path active = directory.resolve("pair-reactivated.cedg");
		assertTrue(((GuiManagerGeoCeDG) reopened.getGuiManager()).saveAsTo(active.toFile()));
		AppGeoCeDG reactivated = enabledApp();
		assertTrue(reactivated.loadFile(active.toFile(), false));
		assertTrue(lookup(reactivated, "X").isDefined());
		assertEquals(ids, identities(reactivated));
		assertEquals(points, pointCount(reactivated));
		assertEquals(PairBindingState.ACTIVE,
				pairState((GeoLocusIntersectionResult) lookup(reactivated, "R"), token));
		assertFalse(reactivated.getXML().contains("KrawczykBox"));
	}

	@Test
	void nativeCedgPeriodicActiveSeamPreservesBothExactTokenBindings(
			@TempDir Path directory) throws Exception {
		AppGeoCeDG app = enabledApp();
		app.getKernel().setContinuous(false);
		for (String command : new String[] {"h=0.05", "A=(1,0)", "B=(0,1)",
				"C=(-1,0)", "D=(0,-1)", "S=SplineV2({A,B,C,D,A},3)",
				"E=(-2,h)", "F=(0,h)", "G=(2,h)", "T=SplineV2({E,F,G},3)",
				"R=Intersect(S,T)"}) {
			eval(app, command);
		}
		GeoLocusIntersectionResult rich = (GeoLocusIntersectionResult) lookup(app, "R");
		List<String> initialTokens = eligibleTokens(rich).stream().sorted().toList();
		assertEquals(2, initialTokens.size());
		Map<String, String> ownedTokens = new LinkedHashMap<>();
		Map<String, PersistentGeoId> pointIds = new LinkedHashMap<>();
		Map<String, Double> initialX = new LinkedHashMap<>();
		for (int index = 0; index < 2; index++) {
			// Enumeration names this test's output geos; the exact selected token,
			// never that presentation index, remains the point's semantic authority.
			String label = index == 0 ? "X" : "Y";
			String token = initialTokens.get(index);
			GeoPoint point = LocusV2PublicOperations.selectIntersectionPoint(
					app.getKernel().getConstruction(), label, rich,
					new GeoText(app.getKernel().getConstruction(), token));
			assertTrue(point.isDefined());
			ownedTokens.put(label, token);
			pointIds.put(label, app.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(point));
			initialX.put(label, point.getInhomX());
		}
		Map<String, PersistentGeoId> ids = identities(app);
		long count = pointCount(app);
		for (double height : new double[] {0, -0.05, 0.05}) {
			move(app, height);
			rich = (GeoLocusIntersectionResult) lookup(app, "R");
			assertEquals(Set.copyOf(initialTokens), eligibleTokens(rich));
			GeoLocusV2 source = (GeoLocusV2) lookup(app, "S");
			if (height == 0) {
				assertEquals(1, rich.getIntersectionResult().getFiniteSolutions().stream()
						.filter(root -> parameter(root, source) == 0).count());
			}
			assertTrue(rich.getIntersectionResult().getFiniteSolutions().stream()
					.allMatch(root -> parameter(root, source) >= 0 && parameter(root, source) < 1));
			String ledger = rich.getTokenLedgerState();
			Path file = directory.resolve("periodic-pair-" + height + ".cedg");
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
			AppGeoCeDG reopened = enabledApp();
			assertTrue(reopened.loadFile(file.toFile(), false));
			assertEquals(ids, identities(reopened));
			assertEquals(count, pointCount(reopened));
			GeoLocusIntersectionResult restoredRich =
					(GeoLocusIntersectionResult) lookup(reopened, "R");
			assertEquals(ledger, restoredRich.getTokenLedgerState());
			assertEquals(Set.copyOf(initialTokens), eligibleTokens(restoredRich));
			for (String label : ownedTokens.keySet()) {
				GeoPoint restored = (GeoPoint) lookup(reopened, label);
				assertTrue(restored.isDefined());
				assertEquals(pointIds.get(label), reopened.getKernel().getConstruction()
						.getSpatialIdentityRegistry().getPersistentGeoId(restored));
				assertEquals(ownedTokens.get(label),
						((AlgoLocusIntersectionPointV2) restored.getParentAlgorithm())
								.getSelectedRootToken());
				assertEquals(PairBindingState.ACTIVE,
						pairState(restoredRich, ownedTokens.get(label)));
				assertEquals(height, restored.getInhomY(), 1E-9);
				if (height == 0.05) {
					assertEquals(initialX.get(label), restored.getInhomX(), 1E-9);
				}
			}
			app = reopened;
		}
	}

	private static Set<String> eligibleTokens(GeoLocusIntersectionResult rich) {
		return rich.getIntersectionResult().getFiniteSolutions().stream()
				.map(root -> root.getIdentity().getRootToken()).filter(rich::isPointAdmissible)
				.collect(Collectors.toSet());
	}

	private static double parameter(LocusIntersectionSolution2D root, GeoLocusV2 source) {
		var pair = root.getPairEvidence().orElseThrow();
		String identity = source.getPersistentLocusId().toExternalForm();
		assertTrue(pair.getFirst().getLocusIdentity().equals(identity)
				|| pair.getSecond().getLocusIdentity().equals(identity));
		return pair.getFirst().getLocusIdentity().equals(identity)
				? pair.getFirst().getSemanticParameter() : pair.getSecond().getSemanticParameter();
	}

	private static PairBindingState pairState(GeoLocusIntersectionResult rich, String token) {
		// Inspect the actual persisted discriminated state without a reflected
		// product field or an invented numerical certificate.
		LocusIntersectionTokenLedger2D persisted = new LocusIntersectionTokenLedger2D();
		persisted.importState(rich.getTokenLedgerState());
		return persisted.getPairBindingState(token).orElseThrow();
	}

	private static long pointCount(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
				.filter(GeoPoint.class::isInstance).count();
	}

	private static void move(AppGeoCeDG app, double value) {
		GeoNumeric input = (GeoNumeric) lookup(app, "h");
		input.setValue(value);
		input.updateCascade();
	}

	private static Map<String, PersistentGeoId> identities(AppGeoCeDG app) {
		Map<String, PersistentGeoId> ids = new LinkedHashMap<>();
		// The spline registers its direct list/degree dependencies. The ordinary
		// transitive numeric h need not itself participate in the identity graph.
		for (String label : new String[] {"S", "T", "R", "X"}) {
			PersistentGeoId id = app.getKernel().getConstruction()
					.getSpatialIdentityRegistry().getPersistentGeoId(lookup(app, label));
			assertNotNull(id, label);
			ids.put(label, id);
		}
		return ids;
	}

	private static GeoElement eval(AppGeoCeDG app, String command) {
		GeoElementND[] result = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(result, command);
		assertTrue(result.length > 0, command);
		return result[0].toGeoElement();
	}

	private static GeoElement lookup(AppGeoCeDG app, String label) {
		GeoElement geo = app.getKernel().lookupLabel(label);
		assertNotNull(geo, label);
		return geo;
	}

	private static AppGeoCeDG enabledApp() {
		AppGeoCeDG app = new AppGeoCeDG(new CommandLineArguments(
				new String[] {"--silent", "--enableLocusV2=true"}), new JPanel());
		app.setErrorDialogsActive(false);
		return app;
	}
}
