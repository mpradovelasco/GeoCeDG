/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * Closing check of GeoCeDG 1.2.2 (author instruction of 2026-11-22): the life cycle of a
 * {@code LocusLength} number on the author model, deleted through the ordinary deletion of
 * the application ({@code deleteSelectedObjects}, the Delete key and the context menu). The
 * private rich authority that the command created for the number goes with it, the work of
 * the real changes returns to what it was, and Undo, Redo, save and reopen keep the
 * document coherent. A rich result that the document names itself stays where it is.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostR1LengthDeletionDesktopTest {
	@TempDir
	Path work;

	@Test
	void deletingALengthReleasesItsPrivateAuthorityAndItsWork() throws Exception {
		Map<String, Object> row = new LinkedHashMap<>();
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG opened = PreG9BCu0Corpus.open(PostR1Corpus.workingCopy(
					PostR1Corpus.original(), work));
			opened.setUndoActive(true);
			row.put("baseline", work(opened, 4));
			row.put("metric results before", richResults(opened).size());
			LocusLengths.create(opened, "cuT=LocusLength(w_2)");
			GeoNumeric total = (GeoNumeric) opened.getKernel().lookupLabel("cuT");
			GeoLocusMetricResult rich = AlgoLocusMetricScalarAdapter.richResultOf(total);
			row.put("total defined", total.isDefined());
			row.put("authority label hidden", rich != null && !rich.isAlgebraLabelVisible());
			row.put("with the total", work(opened, 4));
			return opened;
		});
		// The input bar stores an Undo step after a new object.
		storeUndo(app);
		delete(app, "cuT");
		PreG9BR6PlusF3Fixtures.onEdt(() -> {
			row.put("total gone", app.getKernel().lookupLabel("cuT") == null);
			row.put("metric results after", richResults(app).size());
			row.put("orphans", orphans(app));
			row.put("after the deletion", work(app, 4));
			return null;
		});
		// Undo restores the number with its authority; Redo deletes both again.
		PreG9BR6PlusF3Fixtures.onEdt(() -> {
			app.getKernel().undo();
			GeoElement restored = app.getKernel().lookupLabel("cuT");
			row.put("undo restores", restored != null && AlgoLocusMetricScalarAdapter
					.richResultOf(restored) != null);
			row.put("undo results", richResults(app).size());
			app.getKernel().redo();
			row.put("redo deletes", app.getKernel().lookupLabel("cuT") == null);
			row.put("redo results", richResults(app).size());
			row.put("redo orphans", orphans(app));
			return null;
		});
		PreG9BR6PlusF3Fixtures.onEdt(() -> {
			File saved = work.resolve("length-deleted.cedg").toFile();
			row.put("saved", app.saveGeoGebraFile(saved));
			AppGeoCeDG reopened = PreG9BCu0Corpus.open(saved);
			row.put("reopened results", richResults(reopened).size());
			row.put("reopened orphans", orphans(reopened));
			row.put("reopened work", work(reopened, 4));
			return null;
		});
		int before = (Integer) row.get("metric results before");
		// The total over the conditioned regions is a subtotal, not a number (metrics, 28).
		assertEquals(Boolean.FALSE, row.get("total defined"), row.toString());
		assertEquals(Boolean.TRUE, row.get("authority label hidden"), row.toString());
		assertTrue((Long) row.get("with the total") > (Long) row.get("baseline"),
				row.toString());
		assertEquals(Boolean.TRUE, row.get("total gone"), row.toString());
		assertEquals(before, row.get("metric results after"), row.toString());
		assertEquals(List.of(), row.get("orphans"), row.toString());
		assertEquals(row.get("baseline"), row.get("after the deletion"), row.toString());
		assertEquals(Boolean.TRUE, row.get("undo restores"), row.toString());
		assertEquals(before + 1, row.get("undo results"), row.toString());
		assertEquals(Boolean.TRUE, row.get("redo deletes"), row.toString());
		assertEquals(before, row.get("redo results"), row.toString());
		assertEquals(List.of(), row.get("redo orphans"), row.toString());
		assertEquals(Boolean.TRUE, row.get("saved"), row.toString());
		assertEquals(before, row.get("reopened results"), row.toString());
		assertEquals(List.of(), row.get("reopened orphans"), row.toString());
		assertEquals(row.get("baseline"), row.get("reopened work"), row.toString());
	}

	@Test
	void aLengthBetweenTwoPointsAndAReopenedLengthAreReleasedToo() throws Exception {
		Map<String, Object> row = new LinkedHashMap<>();
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG opened = PreG9BCu0Corpus.open(PostR1Corpus.workingCopy(
					PostR1Corpus.original(), work));
			opened.setUndoActive(true);
			row.put("before", richResults(opened).size());
			LocusLengths.create(opened, "CUP=Point(w_2,\"" + PostR1Corpus.MAIN + "\","
					+ LocusLengths.at(opened, 0.3) + ")");
			LocusLengths.create(opened, "CUQ=Point(w_2,\"" + PostR1Corpus.MAIN + "\","
					+ LocusLengths.at(opened, 0.7) + ")");
			LocusLengths.create(opened, "cuL=LocusLength(w_2,CUP,CUQ)");
			LocusLengths.create(opened, "cuT=LocusLength(w_2)");
			File saved = work.resolve("two-lengths.cedg").toFile();
			row.put("saved", opened.saveGeoGebraFile(saved));
			AppGeoCeDG reopened = PreG9BCu0Corpus.open(saved);
			reopened.setUndoActive(true);
			row.put("reopened", richResults(reopened).size());
			return reopened;
		});
		// The number of a reopened document, then the one between two points.
		delete(app, "cuT");
		delete(app, "cuL");
		PreG9BR6PlusF3Fixtures.onEdt(() -> {
			row.put("after", richResults(app).size());
			row.put("orphans", orphans(app));
			row.put("points kept", app.getKernel().lookupLabel("CUP") != null
					&& app.getKernel().lookupLabel("CUQ") != null);
			return null;
		});
		int before = (Integer) row.get("before");
		assertEquals(Boolean.TRUE, row.get("saved"), row.toString());
		assertEquals(before + 2, row.get("reopened"), row.toString());
		assertEquals(before, row.get("after"), row.toString());
		assertEquals(List.of(), row.get("orphans"), row.toString());
		assertEquals(Boolean.TRUE, row.get("points kept"), row.toString());
	}

	@Test
	void aRichResultThatTheDocumentNamesStaysWhenItsNumberIsDeleted() throws Exception {
		Map<String, Object> row = new LinkedHashMap<>();
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG opened = PreG9BCu0Corpus.open(PostR1Corpus.workingCopy(
					PostR1Corpus.original(), work));
			opened.setUndoActive(true);
			row.put("before", richResults(opened).size());
			// n_2 = LocusLength(e_2, POrigen, R_1) is a rich result of the document, with an
			// ordinary name, and o_2 = Length(n_2) is its number.
			row.put("named", opened.getKernel().lookupLabel("n_2") != null
					&& opened.getKernel().lookupLabel("o_2") != null);
			return opened;
		});
		delete(app, "o_2");
		PreG9BR6PlusF3Fixtures.onEdt(() -> {
			row.put("number gone", app.getKernel().lookupLabel("o_2") == null);
			row.put("rich kept", app.getKernel().lookupLabel("n_2") != null);
			row.put("after", richResults(app).size());
			return null;
		});
		assertEquals(Boolean.TRUE, row.get("named"), row.toString());
		assertEquals(Boolean.TRUE, row.get("number gone"), row.toString());
		assertEquals(Boolean.TRUE, row.get("rich kept"), row.toString());
		assertEquals(row.get("before"), row.get("after"), row.toString());
	}

	private static void storeUndo(AppGeoCeDG app) throws Exception {
		CountDownLatch stored = new CountDownLatch(1);
		app.getKernel().getConstruction().getUndoManager()
				.addUndoInfoStoredListener(stored::countDown);
		PreG9BR6PlusF3Fixtures.onEdt(() -> {
			app.storeUndoInfo();
			return null;
		});
		assertTrue(stored.await(30, TimeUnit.SECONDS), "the Undo step was stored");
		PreG9BR6PlusF3Fixtures.onEdt(() -> null);
	}

	/** Deletes one object as the application does for the Delete key, and waits for Undo. */
	private static void delete(AppGeoCeDG app, String label) throws Exception {
		CountDownLatch stored = new CountDownLatch(1);
		app.getKernel().getConstruction().getUndoManager()
				.addUndoInfoStoredListener(stored::countDown);
		PreG9BR6PlusF3Fixtures.onEdt(() -> {
			GeoElement geo = app.getKernel().lookupLabel(label);
			app.getSelectionManager().clearSelectedGeos(false);
			app.getSelectionManager().addSelectedGeo(geo);
			app.deleteSelectedObjects(false);
			return null;
		});
		assertTrue(stored.await(30, TimeUnit.SECONDS), "the deletion stored its Undo step");
		PreG9BR6PlusF3Fixtures.onEdt(() -> null);
	}

	/** Work of real changes of excCyl: evaluator calls of w_2 over the changes. */
	private static long work(AppGeoCeDG app, int changes) {
		GeoLocusV2 hole = PreG9BCu0Corpus.locus(app, "w_2");
		GeoNumeric eccentricity = (GeoNumeric) app.getKernel().lookupLabel("excCyl");
		double value = eccentricity.getValue();
		long start = hole.getInstrumentation().getEvaluatorCalls();
		for (int index = 0; index < changes; index++) {
			PreG9BCu0Corpus.setNumber(app, "excCyl", value + (index % 2 == 0 ? 0.05 : 0));
		}
		return hole.getInstrumentation().getEvaluatorCalls() - start;
	}

	private static List<GeoLocusMetricResult> richResults(AppGeoCeDG app) {
		List<GeoLocusMetricResult> results = new ArrayList<>();
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (geo instanceof GeoLocusMetricResult) {
				results.add((GeoLocusMetricResult) geo);
			}
		}
		return results;
	}

	/** @return labels of the rich results that no algorithm reads and no name exposes */
	private static List<String> orphans(AppGeoCeDG app) {
		List<String> orphans = new ArrayList<>();
		for (GeoLocusMetricResult rich : richResults(app)) {
			if (rich.getAlgorithmList().isEmpty() && !rich.isAlgebraLabelVisible()) {
				orphans.add(rich.getLabelSimple());
			}
		}
		return orphans;
	}

	/** Commands sent as the input bar sends them. */
	private static final class LocusLengths {
		private LocusLengths() {
		}

		static void create(AppGeoCeDG app, String command) {
			PostR1Corpus.command(app, command);
		}

		static double at(AppGeoCeDG app, double fraction) {
			var region = PreG9BCu0Corpus.locus(app, "w_2")
					.getConditionedEvidence(PostR1Corpus.MAIN).getRegions().get(0);
			return region.getLower() + fraction * (region.getUpper() - region.getLower());
		}
	}
}
