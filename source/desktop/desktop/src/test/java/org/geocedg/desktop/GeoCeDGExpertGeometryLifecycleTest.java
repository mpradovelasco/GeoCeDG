/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.expertWorkerClassPath;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.save;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.geocedg.common.kernel.units.UnitState;
import org.geogebra.common.kernel.PathRegionHandling;
import org.geogebra.common.kernel.algos.AlgoJoinPointsSegment;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Exercises an exact coordinate change caused by a native evaluation setting. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class GeoCeDGExpertGeometryLifecycleTest {
	@TempDir
	Path directory;

	@Test
	void publishesDisclosedPointChangeAndPreservesItsNativeDependencies() throws Exception {
		File source = source();
		byte[] sourceBytes = Files.readAllBytes(source.toPath());
		assertUnchangedWithSamePathMode(source);
		AppGeoCeDG receiver = receiver(PathRegionHandling.ON);
		String uuid = onEdt(receiver::getUniqueId);
		GeoElement resident = onEdt(() -> named(receiver, "A"));
		var insertion = receiver.getDocumentInsertion();
		var review = onEdt(insertion::beginExpertReview);
		try (var trial = inspect(review, source)) {
			assertTrue(trial.available(), () -> trial.report().toString());
			assertPointEvidence(trial.report());
			onEdt(() -> {
				var details = GeoCeDGExpertReviewModel.project(receiver, trial).details();
				assertTrue(details.contains("Current value · (0.0, 0.0) → (3.0, 0.0)"), () -> details);
				assertTrue(details.contains("Path and region handling · Use closest point → Keep parameter"),
						() -> details);
				assertNull(named(receiver, "P"));
				var offer = review.offer(source, trial.sourceBytes(), trial.fingerprint(),
						trial.reportSha256(), () -> !trial.source().changed());
				var result = insertion.publishExpert(offer, trial.reportSha256());
				assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED,
						result.getOutcome(), () -> String.valueOf(result.getFailure()));
				receiver.recordExpertInsertion(source, result.getExpertStep());
				assertSame(resident, named(receiver, "A"));
				assertPoint(receiver, "A_1", 3);
				undo(receiver).undo();
				assertNull(named(receiver, "P"));
				assertNull(named(receiver, "A_1"));
				assertEquals(uuid, receiver.getUniqueId());
				undo(receiver).redo();
				assertPoint(receiver, "A_1", 3);
				assertEquals(uuid, receiver.getUniqueId());
				return null;
			});
		} finally {
			onEdt(review::close);
		}
		File copy = directory.resolve("accepted.cedg").toFile();
		onEdt(() -> {
			assertTrue(receiver.saveNativeAs(copy));
			var reopened = app(UnitState.EMPTY);
			assertTrue(reopened.loadFile(copy, false));
			assertEquals(uuid, reopened.getUniqueId());
			assertEquals(PathRegionHandling.ON, reopened.getKernel().usePathAndRegionParameters);
			GeoPoint point = assertPoint(reopened, "A_1", 3);
			setParameter(reopened, 0.25);
			assertSame(point, assertPoint(reopened, "A_1", 1));
			var end = (GeoPoint) named(reopened, "B");
			end.setCoords(8, 0, 1);
			end.updateCascade();
			assertSame(point, assertPoint(reopened, "A_1", 2));
			return null;
		});
		assertArrayEquals(sourceBytes, Files.readAllBytes(source.toPath()));
	}

	@Test
	void recoversWhenPointChangesAgainAfterConsent() throws Exception {
		File source = source();
		AppGeoCeDG receiver = receiver(PathRegionHandling.ON);
		var insertion = receiver.getDocumentInsertion();
		var review = onEdt(insertion::beginExpertReview);
		try (var trial = inspect(review, source)) {
			assertTrue(trial.available(), () -> trial.report().toString());
			onEdt(() -> {
				var before = GeoCeDGDocumentInsertion.Certificate.of(receiver);
				var history = GeoCeDGDocumentInsertion.HistoryRecord.of(undo(receiver));
				insertion.setFaults(new GeoCeDGDocumentInsertion.Faults() {
					@Override
					public void afterCommit() {
						setParameter(receiver, 0.25);
						assertPoint(receiver, "A_1", 1);
					}
				});
				var offer = review.offer(source, trial.sourceBytes(), trial.fingerprint(),
						trial.reportSha256(), () -> !trial.source().changed());
				var result = insertion.publishExpert(offer, trial.reportSha256());
				assertEquals(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED,
						result.getOutcome(), () -> String.valueOf(result.getFailure()));
				assertTrue(result.getFailure().getMessage().contains("differs from the consented trial"));
				var restored = GeoCeDGDocumentInsertion.Certificate.of(receiver);
				assertTrue(before.sameInstances(restored));
				assertEquals(List.of(), before.serializableDifferences(restored));
				assertNull(history.mismatch(undo(receiver), null));
				assertNull(named(receiver, "P"));
				assertNull(result.getExpertStep());
				return null;
			});
		} finally {
			onEdt(review::close);
		}
	}

	private File source() throws Exception {
		return onEdt(() -> {
			var original = app(UnitState.EMPTY);
			original.getKernel().setUsePathAndRegionParameters(PathRegionHandling.OFF);
			must(original, "A=(0,0)");
			must(original, "B=(4,0)");
			must(original, "s=Segment(A,B)");
			must(original, "t=0.75");
			must(original, "P=Point(s,t)");
			assertPoint(original, "A", 0);
			var file = save(original, directory, "source");
			var reopened = app(UnitState.EMPTY);
			assertTrue(reopened.loadFile(file, false));
			assertEquals(PathRegionHandling.OFF, reopened.getKernel().usePathAndRegionParameters);
			assertPoint(reopened, "A", 0);
			return file;
		});
	}

	private void assertUnchangedWithSamePathMode(File source) throws Exception {
		AppGeoCeDG control = receiver(PathRegionHandling.OFF);
		var review = onEdt(control.getDocumentInsertion()::beginExpertReview);
		try (var trial = inspect(review, source)) {
			assertTrue(trial.available(), () -> trial.report().toString());
			assertEquals("0x0.0p0,0x0.0p0", observedPoint(stage(trial.report(), "TRIAL"))
					.getString("currentValue"));
		} finally {
			onEdt(review::close);
		}
	}

	private GeoCeDGExpertWorker.Result inspect(GeoCeDGDocumentInsertion.ExpertReview review,
			File source) throws Exception {
		String classPath = expertWorkerClassPath();
		var worker = onEdt(() -> new GeoCeDGExpertWorker(review.snapshot(), classPath));
		return worker.inspect(source, () -> false);
	}

	private static AppGeoCeDG receiver(PathRegionHandling mode) throws Exception {
		return onEdt(() -> {
			var receiver = app(UnitState.EMPTY);
			receiver.getKernel().setUsePathAndRegionParameters(mode);
			must(receiver, "A=(-5,-3)");
			baseline(receiver);
			return receiver;
		});
	}

	private static void assertPointEvidence(JSONObject report) throws Exception {
		var source = stage(report, "SOURCE");
		var trial = stage(report, "TRIAL");
		var reopened = stage(report, "REOPENED");
		assertEquals("OFF", source.getJSONObject("documentContext").getString("pathRegionHandling"));
		assertEquals("ON", trial.getJSONObject("documentContext").getString("pathRegionHandling"));
		assertEquals("ON", reopened.getJSONObject("documentContext").getString("pathRegionHandling"));
		var before = observedPoint(source);
		var after = observedPoint(trial);
		for (var point : List.of(before, after, observedPoint(reopened))) {
			assertTrue(point.getBoolean("defined"));
			assertTrue(point.isNull("identity"));
			assertEquals(AlgoPointOnPath.class.getName(), point.getString("producer"));
			assertEquals(2, point.getJSONArray("inputs").length());
		}
		assertEquals(before.getString("definition"), after.getString("definition"));
		assertEquals(before.getString("presentation"), after.getString("presentation"));
		assertEquals("0x0.0p0,0x0.0p0", before.getString("currentValue"));
		assertEquals("0x1.8p1,0x0.0p0", after.getString("currentValue"));
		assertEquals(after.getString("currentValue"), observedPoint(reopened).getString("currentValue"));
		JSONArray comparisons = report.getJSONArray("comparisons");
		for (int i = 0; i < comparisons.length(); i++) {
			var comparison = comparisons.getJSONObject(i);
			if ("SOURCE".equals(comparison.getString("before"))
					&& "TRIAL".equals(comparison.getString("after"))) {
				assertEquals("NATIVE_COPY_TRACE", comparison.getString("basis"));
				var delta = row(comparison.getJSONArray("objects"), "beforeRef", before.getString("ref"));
				assertEquals(after.getString("ref"), delta.getString("afterRef"));
				var value = row(delta.getJSONArray("observations"), "property", "CURRENT_VALUE");
				assertEquals("VERIFIED_CHANGED", value.getString("status"));
				assertEquals(before.getString("currentValue"), value.getString("before"));
				assertEquals(after.getString("currentValue"), value.getString("after"));
				return;
			}
		}
		throw new AssertionError("Missing source-to-trial comparison");
	}

	private static JSONObject stage(JSONObject report, String phase) throws Exception {
		return row(report.getJSONArray("stages"), "phase", phase);
	}

	private static JSONObject observedPoint(JSONObject stage) throws Exception {
		return row(stage.getJSONArray("objects"), "label", "P");
	}

	private static JSONObject row(JSONArray rows, String key, String expected) throws Exception {
		for (int i = 0; i < rows.length(); i++) {
			var row = rows.getJSONObject(i);
			if (expected.equals(row.optString(key))) {
				return row;
			}
		}
		throw new AssertionError("Missing report row " + key + "=" + expected);
	}

	private static GeoPoint assertPoint(AppGeoCeDG model, String startLabel, double x) {
		var point = (GeoPoint) named(model, "P");
		var segment = (GeoSegment) named(model, "s");
		assertNotNull(point);
		assertNotNull(segment);
		assertTrue(point.isDefined() && point.isFinite());
		assertEquals(x, point.getInhomX());
		assertEquals(0.0, point.getInhomY());
		assertTrue(segment.isOnPath(point, 0));
		assertSame(segment, point.getPath());
		assertEquals(AlgoPointOnPath.class, point.getParentAlgorithm().getClass());
		assertArrayEquals(new GeoElement[] {segment, named(model, "t")}, point.getParentAlgorithm().getInput());
		assertEquals(AlgoJoinPointsSegment.class, segment.getParentAlgorithm().getClass());
		assertArrayEquals(new GeoElement[] {named(model, startLabel), named(model, "B")},
				segment.getParentAlgorithm().getInput());
		return point;
	}

	private static void setParameter(AppGeoCeDG model, double value) {
		var parameter = (GeoNumeric) named(model, "t");
		parameter.setValue(value);
		parameter.updateCascade();
	}

	private static GeoElement named(AppGeoCeDG model, String label) {
		return model.getKernel().lookupLabel(label);
	}
}
