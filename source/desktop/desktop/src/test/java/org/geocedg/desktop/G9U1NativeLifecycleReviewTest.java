/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionQuery2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResolver2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.io.DocumentArchivePreflight;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Producing lifecycle, not a migration or sanitization of the author's archive. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1NativeLifecycleReviewTest {

	@Test
	void lateFreeEndpointParticipationSurvivesUndoSaveAndTwoReopens(@TempDir Path directory)
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		for (String command : new String[] {"A=(-2,0)", "B=(-2/3,0)", "C=(2/3,0)",
				"D=(2,0)", "l1={A,B,C,D}", "b=SplineV2(l1,3)"}) {
			eval(app, command);
		}
		Path file = directory.resolve("review.cedg");
		assertTrue(gui(app).saveAsTo(file.toFile()));
		GeoLocusV2 source = (GeoLocusV2) lookup(app, "b");
		var query = new LocusPointInteractionQuery2D(source, -1, 0,
				LocusPointInteractionPolicy2D.initial(1E-6));
		var candidate = new LocusPointInteractionResolver2D().resolve(query).getUniqueCandidate();
		assertNotNull(candidate);
		LocusV2PublicOperations.createInteractiveSemanticPoint(app.getKernel().getConstruction(),
				"E", source, candidate);
		eval(app, "Q=Point(b,\"spline-v2/main\",0.75)");
		eval(app, "M=Length(b)");
		eval(app, "MP=Length(b,E,Q)");
		// A/C were free interpolation inputs. Their first identity participation
		// must refresh l1's derived dependency record, despite this metric being invalid.
		eval(app, "Invalid=Length(b,A,C)");
		assertFalse(lookup(app, "Invalid").isDefined());
		eval(app, "circle=Circle((0,0),1)");
		GeoCeDGIntersectionSession session = new GeoCeDGIntersectionSession(app);
		session.activate((GeoLocusIntersectionResult) eval(app, "R=Intersect(b,circle)"));
		List<GeoPoint> materialized = session.materializeAll(false);
		assertEquals(2, materialized.size());
		for (int i = 0; i < materialized.size(); i++) {
			materialized.get(i).setLabel("H" + i);
		}
		Map<String, State> expected = snapshot(app);
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
		try (var baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		eval(app, "extra=(5,5)");
		app.storeUndoInfo();
		await(undo::undoPossible);
		app.getKernel().undo();
		AppGeoCeDG first = app;
		await(() -> first.getKernel().lookupLabel("extra") == null);
		assertEquals(expected, snapshot(app));
		assertTrue(gui(app).saveAsTo(file.toFile()));
		app.getKernel().redo();
		await(() -> first.getKernel().lookupLabel("extra") != null);
		assertEquals(expected, snapshot(app));
		assertTrue(gui(app).saveAsTo(file.toFile()));
		app.getKernel().undo();
		await(() -> first.getKernel().lookupLabel("extra") == null);
		assertTrue(gui(app).saveAsTo(file.toFile()));
		for (int reopen = 0; reopen < 2; reopen++) {
			// Closing this disposable document, then loading through the actual native path.
			app.clearConstruction();
			AppGeoCeDG opened = G9U1TestApp.create();
			assertTrue(opened.loadFile(file.toFile(), false));
			assertEquals(expected, snapshot(opened));
			assertNull(opened.getKernel().lookupLabel("extra"));
			GeoPoint interactive = (GeoPoint) lookup(opened, "E");
			assertNotNull(LocusV2PublicOperations.moveInteractiveSemanticPoint(interactive,
					0, 0, LocusPointInteractionPolicy2D.initial(1E-6)).getUniqueCandidate());
			expected = snapshot(opened);
			assertTrue(gui(opened).saveAsTo(file.toFile()));
			app = opened;
		}
	}

	@Test
	void failedNativeSavePreflightPreservesExistingTargetAndLiveState(@TempDir Path directory)
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		eval(app, "A=(0,0)");
		Path file = directory.resolve("last-good.cedg");
		assertTrue(gui(app).saveAsTo(file.toFile()));
		byte[] good = Files.readAllBytes(file);
		eval(app, "B=(1,0)");
		app.setUnsaved();
		String live = app.getXML();
		try (var preflight = mockStatic(DocumentArchivePreflight.class)) {
			preflight.when(() -> DocumentArchivePreflight.validate(any(), any())).thenReturn(false);
			assertFalse(app.saveGeoGebraFile(file.toFile()));
			preflight.verify(() -> DocumentArchivePreflight.validate(any(), any()));
		}
		assertArrayEquals(good, Files.readAllBytes(file));
		assertEquals(live, app.getXML());
		assertFalse(app.isSaved());
		try (var children = Files.list(directory)) {
			assertEquals(List.of(file), children.toList());
		}
	}

	private record State(PersistentGeoId id, boolean defined, String parent,
			List<PersistentGeoId> inputs, LocusSemanticAddress2D address, String token) {
	}

	private static Map<String, State> snapshot(AppGeoCeDG app) {
		var registry = app.getKernel().getConstruction().getSpatialIdentityRegistry();
		Map<String, State> result = new LinkedHashMap<>();
		for (String label : new String[] {"A", "C", "l1", "b", "E", "Q", "M", "MP",
				"Invalid", "R", "H0", "H1"}) {
			GeoElement geo = lookup(app, label);
			var parent = geo.getParentAlgorithm();
			List<PersistentGeoId> inputs = new ArrayList<>();
			if (parent != null) {
				for (GeoElement input : parent.getInput()) {
					inputs.add(registry.getPersistentGeoId(input));
				}
			}
			PersistentGeoId id = registry.getPersistentGeoId(geo);
			assertNotNull(id, label);
			result.put(label, new State(id, geo.isDefined(),
					parent == null ? "free" : parent.getClass().getName(), inputs,
					parent instanceof AlgoSemanticLocusPoint2D
							? ((AlgoSemanticLocusPoint2D) parent).getCurrentSemanticAddress()
							: null,
					parent instanceof AlgoLocusIntersectionPointV2
							? ((AlgoLocusIntersectionPointV2) parent).getEffectiveRootToken()
							: null));
		}
		return result;
	}

	private static GuiManagerGeoCeDG gui(AppGeoCeDG app) {
		return (GuiManagerGeoCeDG) app.getGuiManager();
	}

	private static void await(BooleanSupplier condition) throws InterruptedException {
		long deadline = System.nanoTime() + 10_000_000_000L;
		while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
			Thread.sleep(10);
		}
		assertTrue(condition.getAsBoolean(), "Native undo/redo did not complete");
	}
}
