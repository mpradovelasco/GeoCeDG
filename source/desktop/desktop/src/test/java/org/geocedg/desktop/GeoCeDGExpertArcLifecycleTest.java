/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.save;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.geocedg.common.kernel.units.UnitState;
import org.geogebra.common.kernel.algos.AlgoConicPartCircle;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.App;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** The real worker, the accepted replay, and the native arc lifecycle. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class GeoCeDGExpertArcLifecycleTest {
	private static final double EPS = 1e-9;

	@TempDir
	Path directory;

	@Test
	void publishesReviewedArcAndKeepsItsNativeDynamicDomainAfterReopen() throws Exception {
		File source = onEdt(() -> {
			AppGeoCeDG model = app(UnitState.EMPTY);
			must(model, "A=(0,0)");
			must(model, "B=(2,0)");
			must(model, "C=(0,2)");
			GeoElement arc = must(model, "arc=CircularArc(A,B,C)");
			arc.removeView(App.VIEW_EUCLIDIAN);
			arc.addView(App.VIEW_EUCLIDIAN2);
			must(model, "u=0.5");
			return save(model, directory, "source");
		});
		byte[] originalSource = Files.readAllBytes(source.toPath());
		AppGeoCeDG receiver = onEdt(() -> {
			AppGeoCeDG model = app(UnitState.EMPTY);
			must(model, "A=(-2,0)").setLayer(4);
			baseline(model);
			must(model, "LiveOnly=(3,1)");
			model.storeUndoInfo();
			return model;
		});
		awaitQuiescent(receiver);
		GeoCeDGDocumentInsertion insertion = receiver.getDocumentInsertion();
		var initial = onEdt(() -> {
			assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
					insertion.insert(source).getOutcome());
			assertNull(named(receiver, "arc"));
			return named(receiver, "A");
		});
		String uuid = onEdt(receiver::getUniqueId);
		var review = onEdt(insertion::beginExpertReview);
		try {
			String classPath = workerClassPath();
			var worker = onEdt(() -> new GeoCeDGExpertWorker(review.snapshot(), classPath));
			try (var trial = worker.inspect(source, () -> false)) {
				assertTrue(trial.available(), () -> trial.report().toString());
				onEdt(() -> {
					var detail = GeoCeDGExpertReviewModel.project(receiver, trial).details();
					assertTrue(detail.contains("layer=0") && detail.contains("layer=4"), () -> detail);
					assertTrue(detail.contains("view1=false") && detail.contains("view1=true"), () -> detail);
					assertNotNull(named(receiver, "LiveOnly"));
					assertNull(named(receiver, "arc"));
					var offer = review.offer(source, trial.sourceBytes(), trial.fingerprint(),
							trial.reportSha256(), () -> !trial.source().changed());
					var result = insertion.publishExpert(offer, trial.reportSha256());
					assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED,
							result.getOutcome(), () -> result.getMessage() + " " + result.getFailure());
					receiver.recordExpertInsertion(source, result.getExpertStep());
					assertTrue(receiver.requiresExpertSaveAs());
					assertSame(initial, named(receiver, "A"));
					assertEquals(uuid, receiver.getUniqueId());
					assertTrue(assertArc(receiver, Math.PI / 2).isVisibleInView(App.VIEW_EUCLIDIAN));
					assertEquals(4, named(receiver, "u").getLayer());
					undo(receiver).undo();
					assertNull(named(receiver, "arc"));
					assertNotNull(named(receiver, "LiveOnly"));
					assertFalse(receiver.requiresExpertSaveAs());
					receiver.setSaved();
					undo(receiver).redo();
					assertArc(receiver, Math.PI / 2);
					assertTrue(receiver.requiresExpertSaveAs());
					assertFalse(receiver.isSaved());
					return null;
				});
			}
		} finally {
			onEdt(review::close);
		}

		File copy = onEdt(() -> {
			GeoConicPart arc = assertArc(receiver, Math.PI / 2);
			GeoPoint endpoint = (GeoPoint) named(receiver, "C");
			move(endpoint, 0, -3);
			assertSame(arc, assertArc(receiver, 3 * Math.PI / 2));
			GeoPoint start = (GeoPoint) named(receiver, "B");
			move(start, 0, 0);
			assertFalse(arc.isDefined());
			assertSame(arc, named(receiver, "arc"));
			move(start, 2, 0);
			assertSame(arc, assertArc(receiver, 3 * Math.PI / 2));
			File saved = directory.resolve("reviewed-copy.cedg").toFile();
			var notices = new ArrayList<String>();
			receiver.setInsertionMessageSink(notices::add);
			assertFalse(receiver.saveNativeAs(source));
			assertFalse(receiver.saveGeoGebraFile(saved));
			assertEquals(2, notices.size());
			assertTrue(receiver.saveNativeAs(saved));
			assertFalse(receiver.requiresExpertSaveAs());
			return saved;
		});
		onEdt(() -> {
			AppGeoCeDG reopened = app(UnitState.EMPTY);
			assertTrue(reopened.loadFile(copy, false));
			assertEquals(uuid, reopened.getUniqueId());
			assertNotNull(named(reopened, "LiveOnly"));
			assertEquals(4, named(reopened, "u").getLayer());
			assertArc(reopened, 3 * Math.PI / 2);
			move((GeoPoint) named(reopened, "C"), 0, 2);
			assertArc(reopened, Math.PI / 2);
		});
		assertArrayEquals(originalSource, Files.readAllBytes(source.toPath()));
	}

	private static String workerClassPath() throws Exception {
		var entries = new ArrayList<String>();
		for (ClassLoader loader = GeoCeDGExpertWorker.class.getClassLoader(); loader != null;
				loader = loader.getParent()) {
			if (loader instanceof URLClassLoader urls) {
				for (var url : urls.getURLs()) {
					if ("file".equals(url.getProtocol())) {
						entries.add(Path.of(url.toURI()).toString());
					}
				}
			}
		}
		entries.add(System.getProperty("java.class.path"));
		return String.join(File.pathSeparator, entries);
	}

	private static GeoConicPart assertArc(AppGeoCeDG app, double sweep) {
		GeoConicPart arc = (GeoConicPart) named(app, "arc");
		assertNotNull(arc);
		assertEquals(AlgoConicPartCircle.class, arc.getParentAlgorithm().getClass());
		var inputs = arc.getParentAlgorithm().getInput();
		assertSame(named(app, "A_1"), inputs[0]);
		assertSame(named(app, "B"), inputs[1]);
		assertSame(named(app, "C"), inputs[2]);
		assertTrue(arc.isDefined());
		assertTrue(arc.positiveOrientation());
		assertEquals(2, arc.getHalfAxis(0), EPS);
		assertEquals(sweep, arc.getParameterExtent(), EPS);
		assertEquals(2 * sweep, arc.getArcLength(), EPS);
		assertEquals(0, arc.getMinParameter(), EPS);
		assertEquals(1, arc.getMaxParameter(), EPS);
		return arc;
	}

	private static void move(GeoPoint point, double x, double y) {
		point.setCoords(x, y, 1);
		point.updateCascade();
	}

	private static GeoElement named(AppGeoCeDG app, String label) {
		return app.getKernel().lookupLabel(label);
	}
}
