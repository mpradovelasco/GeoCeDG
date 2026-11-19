/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JList;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DomainLocation;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * Post-1.2.0 maintenance on the author's oblique cylinder and cone model: creating and
 * dragging points on its chained Locus V2, the last of which reads two nested lengths.
 * The model is an external immutable input read from a working copy; the test is skipped
 * where the file is not installed.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class Maintenance120ConeInteractionDesktopTest {
	private static final String SHA = "de9d494e2c4883521494dfb4ce8827e2e629e2a2b77972fd5721b41c92c097e8";
	/** World size of two pixels of the test viewport: canvas events are integer pixels. */
	private static final double PIXEL = 2.0 / 24;
	/** The same resolution as a parameter change, for the slowest of the dragged curves. */
	private static final double PIXEL_PARAMETER = .06;
	@TempDir Path directory;

	private AppGeoCeDG open() throws Exception {
		Path source = Path.of(System.getProperty("geocedg.maintenance120.cone.fixture",
				"C:/Temp/InterCilConoObliqueFinal.cedg"));
		assumeTrue(Files.isRegularFile(source), "The author's model is not installed: " + source);
		byte[] bytes = Files.readAllBytes(source);
		assertEquals(SHA, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)),
				"The author's model must be the exact reproduction input");
		Path copy = directory.resolve("cone-working-copy.cedg");
		Files.write(copy, bytes);
		AppGeoCeDG app = onEdt(() -> PreG9BCu0Corpus.open(copy.toFile()));
		awaitQuiescent(app);
		onEdt(() -> {
			// The embedded host has no window manager: give the canvas a real viewport that
			// shows the projections and the development at the author's scale.
			var view = app.getEuclidianView1();
			view.setSize(new Dimension(1200, 800));
			view.updateSize();
			view.setCoordSystem(60, 320, 24, 24);
			baseline(app);
			return null;
		});
		return app;
	}

	private static GeoPoint point(AppGeoCeDG app, String label) {
		return (GeoPoint) app.getKernel().lookupLabel(label);
	}

	private static LocusSemanticAddress2D address(GeoPoint point) {
		return AlgoSemanticLocusPoint2D.requireSemanticParent(point).getCurrentSemanticAddress();
	}

	private static double[] at(GeoLocusV2 locus, double parameter) {
		try (var session = LocusEvaluationSession2D.memoizing(64)) {
			var value = PreG9BCu0Corpus.point(locus, parameter, session);
			assertNotNull(value, "parameter " + parameter);
			return new double[] {value.getX(), value.getY()};
		}
	}

	private static String notice(AppGeoCeDG app) {
		JLabel label = app.getStatusBar().getSegment(GeoCeDGStatusBar.POINT_INTERACTION_NOTICE_SEGMENT);
		return label != null && label.isVisible() ? label.getText() : "";
	}

	/** Drags through real canvas events along the source, alternately off the trace. */
	private static void drag(AppGeoCeDG app, GeoPoint point, double step, int events, String name) {
		var parent = AlgoSemanticLocusPoint2D.requireSemanticParent(point);
		GeoLocusV2 source = parent.getSource();
		double start = address(point).getCanonicalParameter();
		app.setMode(EuclidianConstants.MODE_MOVE);
		Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_PRESSED, point.inhomX, point.inhomY);
		long total = 0;
		long worst = 0;
		double[] target = null;
		for (int event = 1; event <= events; event++) {
			double[] on = at(source, start + event * step);
			double[] ahead = at(source, start + event * step + 1e-4);
			double length = Math.hypot(ahead[0] - on[0], ahead[1] - on[1]);
			double offset = (event % 3 - 1) * .08;
			target = new double[] {on[0] - (ahead[1] - on[1]) / length * offset,
					on[1] + (ahead[0] - on[0]) / length * offset};
			long started = System.nanoTime();
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, target[0], target[1]);
			long elapsed = System.nanoTime() - started;
			total += elapsed;
			worst = Math.max(worst, elapsed);
			assertEquals("", notice(app), name + " event " + event);
			// The point follows the request up to the pixel grid of the pointer events.
			assertTrue(Math.hypot(point.inhomX - on[0], point.inhomY - on[1]) <= PIXEL,
					name + " event " + event + " parameter " + address(point).getCanonicalParameter());
		}
		Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, target[0], target[1]);
		System.out.printf(Locale.ROOT, "MAINTENANCE120_DRAG point=%s source=%s events=%d averageMs=%.2f worstMs=%.2f%n",
				name, source.getLabelSimple(), events, total / 1e6 / events, worst / 1e6);
	}

	@Test void savedPointsDragFluidlyAndSurviveUndoRedoAndReopen() throws Exception {
		AppGeoCeDG app = open();
		// W_1 lies on a plain locus; D_2 on a locus whose generator reads a nested length.
		for (String label : List.of("W_1", "D_2")) {
			var registry = app.getKernel().getConstruction().getSpatialIdentityRegistry();
			var before = onEdt(() -> address(point(app, label)));
			var identity = onEdt(() -> registry.getPersistentGeoId(point(app, label)));
			int size = onEdt(() -> app.getKernel().getConstruction().getGeoSetConstructionOrder().size());
			onEdt(() -> drag(app, point(app, label), .03, 24, label));
			awaitQuiescent(app);
			var after = onEdt(() -> address(point(app, label)));
			onEdt(() -> {
				assertEquals(before.getCanonicalParameter() + 24 * .03, after.getCanonicalParameter(), PIXEL_PARAMETER);
				assertEquals(before.getComponentLineageKey(), after.getComponentLineageKey());
				assertEquals(identity, registry.getPersistentGeoId(point(app, label)));
				assertEquals(size, app.getKernel().getConstruction().getGeoSetConstructionOrder().size());
				// One gesture is one history step.
				assertTrue(undo(app).undoPossible());
				app.getKernel().undo();
			});
			awaitQuiescent(app);
			onEdt(() -> {
				assertEquals(before, address(point(app, label)));
				assertEquals(identity, app.getKernel().getConstruction().getSpatialIdentityRegistry()
						.getPersistentGeoId(point(app, label)));
				assertTrue(undo(app).redoPossible());
				app.getKernel().redo();
			});
			awaitQuiescent(app);
			onEdt(() -> assertEquals(after, address(point(app, label))));
		}
		var saved = onEdt(() -> List.of(address(point(app, "W_1")), address(point(app, "D_2"))));
		var file = onEdt(() -> save(app, directory, "cone-dragged"));
		AppGeoCeDG reopened = onEdt(() -> PreG9BCu0Corpus.open(file));
		awaitQuiescent(reopened);
		onEdt(() -> {
			assertEquals(saved.get(0), address(point(reopened, "W_1")));
			assertEquals(saved.get(1), address(point(reopened, "D_2")));
		});
	}

	@Test void clickOnTheDevelopmentOffersOnePositionAndThePointDrags() throws Exception {
		AppGeoCeDG app = open();
		onEdt(() -> {
			GeoLocusV2 development = PreG9BCu0Corpus.locus(app, "s_2");
			double[] click = at(development, 1);
			app.setMode(EuclidianConstants.MODE_POINT);
			long started = System.nanoTime();
			Cu7M08SDesktopEventsTest.pointer(app, click[0], click[1]);
			long elapsed = System.nanoTime() - started;
			var tool = G9U1SemanticPointInteractionTest.controller(app).referencePointTool();
			assertNull(tool.lastFailure(), "The click must not exhaust the work budget");
			var selection = tool.pendingSelection();
			assertNotNull(selection);
			assertEquals(GeoCeDGReferencePlacement.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION,
					selection.getRoute());
			assertEquals(1, selection.getChoices().size());
			System.out.printf(Locale.ROOT, "MAINTENANCE120_CREATE source=s_2 clickMs=%.1f semanticMisses=%d%n",
					elapsed / 1e6, selection.getSemanticMisses());
			// The usual label names branch, section and parameter; no internal identifier.
			JList<?> choices = Cu7M08SDesktopEventsTest.find(tool.contextMenu(), JList.class, list -> true);
			assertNotNull(choices);
			String label = String.valueOf(choices.getModel().getElementAt(0));
			assertTrue(label.matches("Branch 1 \\S Section 1 \\S u = (0\\.99|1\\.00)\\d\\d"), label);
			assertFalse(label.contains("generator") || label.contains("geo:") || label.contains("METRIC"));
			Cu7M08SDesktopEventsTest.confirm(app);
			GeoPoint created = app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
					.filter(GeoPoint.class::isInstance).map(GeoPoint.class::cast)
					.filter(GeoCeDGPointInteraction::owns)
					.filter(candidate -> AlgoSemanticLocusPoint2D.requireSemanticParent(candidate)
							.getSource() == development).findFirst().orElseThrow();
			created.setLabel("Maintenance120Created");
			assertEquals(1, address(created).getCanonicalParameter(), PIXEL_PARAMETER);
			assertEquals(click[0], created.inhomX, PIXEL);
			assertEquals(click[1], created.inhomY, PIXEL);
			assertFalse(created.isLocked());
		});
		awaitQuiescent(app);
		onEdt(() -> drag(app, point(app, "Maintenance120Created"), .02, 16, "created-on-s_2"));
		awaitQuiescent(app);
		onEdt(() -> assertEquals(1 + 16 * .02,
				address(point(app, "Maintenance120Created")).getCanonicalParameter(), PIXEL_PARAMETER));
	}

	@Test void endOfTheVisibleCurveRefusesTheRequestWithAReadableNoticeAndTheGestureContinues()
			throws Exception {
		AppGeoCeDG app = open();
		onEdt(() -> {
			// e_1 is traced twice and folds at u = 0: W_1 can reach the end of the visible
			// curve from its own side, never the other preimage beyond the fold.
			GeoPoint point = point(app, "W_1");
			GeoLocusV2 source = AlgoSemanticLocusPoint2D.requireSemanticParent(point).getSource();
			var start = address(point);
			assertTrue(start.getCanonicalParameter() < 0);
			double[] end = at(source, 0);
			double[] inside = at(source, -.2);
			app.setMode(EuclidianConstants.MODE_MOVE);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_PRESSED, point.inhomX, point.inhomY);
			// A request far beyond the fold, along the direction in which the curve ends.
			double[] beyond = {end[0] + 3 * (end[0] - inside[0]), end[1] + 3 * (end[1] - inside[1])};
			for (int event = 0; event < 3; event++) {
				Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, beyond[0], beyond[1]);
				assertTrue(address(point).getCanonicalParameter() < 0, "The fold is never crossed");
			}
			String refusal = notice(app);
			assertFalse(refusal.isEmpty());
			assertFalse(refusal.contains("null") || refusal.contains("_") || refusal.contains("Drag stopped"),
					refusal);
			// The same gesture continues with the next admissible request.
			double[] back = at(source, start.getCanonicalParameter() + .1);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, back[0], back[1]);
			assertEquals("", notice(app));
			assertEquals(start.getCanonicalParameter() + .1, address(point).getCanonicalParameter(), PIXEL_PARAMETER);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, back[0], back[1]);
		});
		awaitQuiescent(app);
	}

	@Test void doublyTracedCurveOffersTwoReadableBranchChoices() throws Exception {
		AppGeoCeDG app = open();
		onEdt(() -> {
			GeoLocusV2 traced = PreG9BCu0Corpus.locus(app, "e_1");
			double[] click = at(traced, 1);
			try (var offer = new GeoCeDGReferencePlacement(traced, click[0], click[1],
					LocusPointInteractionPolicy2D.initial(.05))) {
				assertEquals(GeoCeDGReferencePlacement.Route.R1_R6, offer.getRoute(), offer.getFailure());
				assertEquals(2, offer.getChoices().size(), "Two preimages share that image");
				var addresses = offer.getChoices().stream()
						.map(GeoCeDGReferencePlacement.Choice::getAddress).toList();
				var english = GeoCeDGAddressPresentation.labels(Locale.ENGLISH, traced, addresses);
				var spanish = GeoCeDGAddressPresentation.labels(Locale.forLanguageTag("es"), traced, addresses);
				assertEquals(2, english.stream().distinct().count());
				for (int index = 0; index < 2; index++) {
					assertTrue(english.get(index).matches("Branch 1 \\S Section 1 \\S u = -?1\\.0000"),
							english.get(index));
					assertTrue(spanish.get(index).matches("Rama 1 \\S Tramo 1 \\S u = -?1,0000"),
							spanish.get(index));
					// The complete identifier stays available for inspection.
					assertTrue(GeoCeDGAddressPresentation.technical(addresses.get(index))
							.contains(addresses.get(index).getComponentLineageKey()));
				}
			}
		});
	}

	@Test void developmentMeetsItsStartingGeneratrixOnce() throws Exception {
		AppGeoCeDG app = open();
		onEdt(() -> {
			GeoLocusIntersectionResult result = (GeoLocusIntersectionResult) must(app,
					"Maintenance120Seam=Intersect(s_2,l_2)");
			var solutions = result.getIntersectionResult().getFiniteSolutions();
			// The development starts on l_2 and ends away from it: one root, at the included
			// start of the component, and no repetition at the excluded end.
			assertEquals(1, solutions.size());
			var root = solutions.get(0);
			assertEquals(0, root.getRevisionEvidence().getSemanticParameter(), 1e-9);
			assertEquals(DomainLocation.INCLUDED_ENDPOINT, root.getClassification().getDomainLocation());
			assertTrue(result.isPointAdmissible(root.getIdentity().getRootToken()));
			GeoPoint apex = point(app, "S_1");
			assertEquals(apex.inhomX, root.getEvaluatedPoint().getX(), 1e-9);
		});
	}
}
