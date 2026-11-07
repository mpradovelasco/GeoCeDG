/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Documents.M;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.kernel.algos.AlgoConicPartCircle;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoCurveCartesian;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;
import org.geogebra.desktop.headless.GFileHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Diagnostic on trusted, synthetic files. Direct native copy runs only in a
 * disposable receiver, after separately testing ordinary admission.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostCu4P4Probe {
	private static final double EPS = 1e-9;

	record Case(String name, String[] commands, String driver, double[] moved, double[] singular,
			boolean admitted) {
		@Override
		public String toString() {
			return name;
		}
	}

	static Stream<Case> cases() {
		return Stream.of(
				new Case("arc", new String[] {"A=(0,0)", "B=(2,0)", "C=(0,5)",
						"obj=CircularArc(A,B,C)", "W=Point(obj,0.5)"}, "C",
						new double[] {-2, 0}, new double[] {0, 0}, false),
				new Case("sector", new String[] {"A=(0,0)", "B=(2,0)", "C=(0,2)",
						"obj=CircularSector(A,B,C)"}, "C",
						new double[] {-2, 0}, new double[] {0, 0}, false),
				new Case("hyperbola", new String[] {"F=(-2,0)", "G=(2,0)", "a=1",
						"obj=Hyperbola(F,G,a)"}, "a",
						new double[] {1.25}, new double[] {2}, false),
				new Case("parabola", new String[] {"F=(0,1)", "d: y=-1",
						"obj=Parabola(F,d)"}, "F",
						new double[] {1, 2}, new double[] {0, -1}, false),
				new Case("curve", new String[] {"a=2", "obj=Curve(t,a*t^2,t,-1,1)"}, "a",
						new double[] {3}, null, false),
				new Case("sequence", new String[] {"a=2", "obj=Sequence(k*a,k,1,4)"},
						"a", new double[] {3}, null, true),
				new Case("dynamic-text", new String[] {"a=2", "obj=Text(a)"},
						"a", new double[] {3}, null, true));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("cases")
	void characterize(Case sample) throws Exception {
		Path output = Path.of(System.getProperty("geocedg.p4.dir"), sample.name());
		Files.createDirectory(output);
		var stage = onEdt(() -> prepare(sample, output));
		awaitQuiescent(stage.target());
		onEdt(() -> checkHistoryAndSave(sample, stage, output));
	}

	@Test
	void explainBaselineFixtureLayerResults() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.p4.dir"), "f3-controls");
		Files.createDirectory(output);
		onEdt(() -> {
			File elementary = PreG9BR6PlusF3Documents.elementary(output, "elementary");
			File locusSpline = PreG9BR6PlusF3Documents.locus(output, "locus-spline");
			for (int layer : new int[] {4, 0}) {
				reportBaselineFixture(output, elementary, "a", layer);
				reportBaselineFixture(output, locusSpline, "s", layer);
			}
			return null;
		});
	}

	private void reportBaselineFixture(Path output, File file, String driver, int workingLayer)
			throws Exception {
		var target = PreG9BR6PlusF3Documents.target(output,
				"receiver-layer-" + workingLayer + "-" + file.getName());
		target.getLayerWorkspace().setWorkingLayer(workingLayer);
		var before = new PreG9BR6PlusF3Documents.Observation(target);
		byte[] original = Files.readAllBytes(file.toPath());
		var receiver = new ByteArrayOutputStream();
		target.getXMLio().writeGeoGebraFile(receiver, false);
		var result = target.getDocumentInsertion().insert(file);
		System.out.printf("P4PROBE f3-control %s working=%d %s %s%n",
				file.getName(), workingLayer, result.getOutcome(), result.getMessage());
		if (result.getOutcome() != GeoCeDGDocumentInsertion.Outcome.INSERTED) {
			assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
					result.getOutcome());
			var after = new PreG9BR6PlusF3Documents.Observation(target);
			assertEquals(List.of(), before.serializableDifferences(after));
			assertTrue(before.sameInstances(after));
		}
		assertArrayEquals(original, Files.readAllBytes(file.toPath()));
		replayScratchCopy(file, driver, target, receiver.toByteArray(), result.getOutcome());
	}

	private static void replayScratchCopy(File file, String driver, AppGeoCeDG target,
			byte[] receiver, GeoCeDGDocumentInsertion.Outcome outcome) throws Exception {
		var source = target.newInsertionHelper();
		var scratch = target.newInsertionHelper();
		try {
			assertTrue(source.loadFile(file, false));
			assertTrue(GFileHandler.loadXML(scratch, new ByteArrayInputStream(receiver), false));
			baseline(source);
			baseline(scratch);
			var admission = GeoCeDGInsertionAdmission.admit(scratch, source);
			assertTrue(admission.isAdmitted());
			int maxLayer = scratch.getMaxLayerUsed();
			int working = scratch.getLayerWorkspace().getWorkingLayer();
			var initial = GeoCeDGDocumentInsertion.identitySet(scratch);
			var ids = GeoCeDGDocumentInsertion.identities(scratch);
			var buffer = new GeoCeDGInsertionCopyPaste(admission.getTransferredMacros());
			buffer.insertFrom(source, scratch, admission.getDuplicateLabels(), false);
			var created = GeoCeDGDocumentInsertion.createdSince(scratch, initial);
			var from = named(source, driver);
			// Trace the exact pair read by the inherited F3 verifier. Its ordered
			// comparison is not evidence of durable identity or expert provenance.
			assertEquals(admission.getSource().size(), created.size());
			var to = created.get(admission.getSource().indexOf(from));
			assertEquals(from.getClass(), to.getClass());
			String mismatch = GeoCeDGDocumentInsertion.verify(admission.getSource(), created,
					scratch, source, ids);
			System.out.printf("P4PROBE layer-witness %s driver=%s source=%d copy=%d "
					+ "serializedLayer=%s scratchMax=%d scratchWorking=%d hidden=%s "
					+ "mapped=%s mismatch=%s%n",
					file.getName(), driver, from.getLayer(), to.getLayer(),
					from.getXML().contains("<layer "), maxLayer, working,
					scratch.getLayerWorkspace().getHiddenLayers(), buffer.getCopies().get(from) == to,
					mismatch);
			assertEquals(List.of(3), scratch.getLayerWorkspace().getHiddenLayers());
			assertEquals(3, named(scratch, "B").getLayer());
			var sourcePoint = named(source, "B");
			assertEquals(sourcePoint.getLayer(), created.get(admission.getSource()
					.indexOf(sourcePoint)).getLayer());
			if (outcome == GeoCeDGDocumentInsertion.Outcome.INSERTED) {
				assertNull(mismatch);
			} else {
				assertEquals("layer " + driver, mismatch);
				assertFalse(from.getXML().contains("<layer "));
				assertNotEquals(from.getLayer(), to.getLayer());
			}
		} finally {
			target.releaseInsertionHelper(scratch);
			target.releaseInsertionHelper(source);
		}
	}

	private Stage prepare(Case sample, Path output) throws Exception {
		AppGeoCeDG source = app(M);
		for (String command : sample.commands()) {
			must(source, command);
		}
		if (sample.name().equals("arc")) {
			var arc = named(source, "obj");
			arc.setLayer(2);
			arc.setObjColor(GColor.RED);
			assertTrue(source.getLayerWorkspace().setLayerHidden(2, true));
		}
		var input = save(source, output, "source");
		byte[] bytes = Files.readAllBytes(input.toPath());
		AppGeoCeDG loaded = app(M);
		assertTrue(loaded.loadFile(input, false));
		baseline(loaded);
		GeoElement original = named(loaded, "obj");
		assertGeometry(named(source, "obj"), original);
		if (sample.name().equals("arc")) {
			assertEquals(List.of(2), loaded.getLayerWorkspace().getHiddenLayers());
			assertArc(original, named(loaded, "W"), Math.PI / 2);
		}
		trace(sample, "load", original.getClass().getSimpleName() + " producer="
				+ original.getParentAlgorithm().getClass().getSimpleName());

		AppGeoCeDG publicTarget = app(M);
		must(publicTarget, "obj=41");
		baseline(publicTarget);
		var before = new PreG9BR6PlusF3Documents.Observation(publicTarget);
		var result = publicTarget.getDocumentInsertion().insert(input);
		trace(sample, "ordinary", result.getOutcome() + " " + result.getMessage());
		assertArrayEquals(bytes, Files.readAllBytes(input.toPath()));
		if (sample.admitted()) {
			assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED, result.getOutcome(),
					result::getMessage);
		} else {
			assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
					result.getOutcome(), result::getMessage);
			assertTrue(result.getMessage().contains("cannot be inserted"), result::getMessage);
			var after = new PreG9BR6PlusF3Documents.Observation(publicTarget);
			assertEquals(List.of(), before.serializableDifferences(after));
			assertTrue(before.sameInstances(after));
		}

		AppGeoCeDG target = app(M);
		must(target, "obj=41");
		must(target, sample.driver() + "=21");
		baseline(target);
		String receiverXml = normalizedXml(target);
		String uuid = target.getUniqueId();
		GeoElement resident = named(target, "obj");
		GeoElement residentDriver = named(target, sample.driver());
		var receiverInstances = GeoCeDGDocumentInsertion.identitySet(target);
		int commandCount = undo(target).getCommands().size();
		var clipboard = new GeoCeDGInsertionCopyPaste(Set.of());
		clipboard.insertFrom(loaded, target, Set.of("obj", sample.driver()), false);
		assertSame(resident, named(target, "obj"));
		assertSame(residentDriver, named(target, sample.driver()));
		String nativeUuid = target.getUniqueId();
		trace(sample, "uuid", "preserved by direct native copy=" + uuid.equals(nativeUuid));
		var copies = clipboard.getCopies();
		var added = GeoCeDGDocumentInsertion.createdSince(target, receiverInstances);
		GeoElement driver = named(loaded, sample.driver());
		GeoElement copied;
		GeoElement copiedDriver;
		if (copies.isEmpty()) {
			trace(sample, "mapping-gap", "no native correspondence. Created "
					+ added.stream().map(geo -> geo.getLabelSimple() + ":"
							+ geo.getClass().getSimpleName()).toList());
			// Uniquely identifiable in these minimal fixtures for value sampling.
			// This supplies neither a provenance proof nor an identity association.
			copied = uniqueAdded(original, added);
			copiedDriver = uniqueAdded(driver, added);
		} else {
			copied = copies.get(original);
			copiedDriver = copies.get(driver);
			assertNotNull(copied);
			assertNotNull(copiedDriver);
			assertEquals(added.size(), copies.size());
			assertMappedEdges(copies);
		}
		assertNotEquals("obj", copied.getLabelSimple());
		assertGeometry(original, copied);
		if (sample.name().equals("arc")) {
			assertArc(copied, copies.get(named(loaded, "W")), Math.PI / 2);
			assertEquals(List.of(), target.getLayerWorkspace().getHiddenLayers());
		}
		assertNotEquals(driver.getLabelSimple(), copiedDriver.getLabelSimple());
		trace(sample, "copy", "label=" + copied.getLabelSimple() + " driver="
				+ copiedDriver.getLabelSimple() + " mapped=" + copies.size()
				+ " identity=" + loaded.getKernel().getConstruction().getSpatialIdentityRegistry()
						.isParticipating(original));
		assertEquals(commandCount, undo(target).getCommands().size());
		trace(sample, "native-history", "no checkpoint by the copy primitive");
		target.getKernel().getConstruction().getUndoManager().storeUndoInfo();
		String witnessLabel = sample.name().equals("arc")
				? copies.get(named(loaded, "W")).getLabelSimple() : null;
		return new Stage(loaded, target, copied.getLabelSimple(), copiedDriver.getLabelSimple(),
				witnessLabel, receiverXml, uuid, nativeUuid, commandCount);
	}

	private Void checkHistoryAndSave(Case sample, Stage stage, Path output) {
		AppGeoCeDG target = stage.target();
		GeoElement original = named(stage.source(), "obj");
		assertEquals(stage.commandCount() + 1, undo(target).getCommands().size());
		undo(target).undo();
		assertNull(target.getKernel().lookupLabel(stage.label()));
		assertEquals(stage.uuid(), target.getUniqueId());
		assertEquals(stage.before(), normalizedXml(target));
		undo(target).redo();
		GeoElement copied = named(target, stage.label());
		assertEquals(stage.nativeUuid(), target.getUniqueId());
		assertGeometry(original, copied);
		if (stage.witnessLabel() != null) {
			assertArc(copied, named(target, stage.witnessLabel()), Math.PI / 2);
		}
		trace(sample, "history", "explicit checkpoint; Undo restored receiver XML; Redo restored witness");

		var archive = save(target, output, "merged");
		AppGeoCeDG reopened = app(M);
		assertTrue(reopened.loadFile(archive, false));
		GeoElement persisted = named(reopened, stage.label());
		assertEquals(stage.nativeUuid(), reopened.getUniqueId());
		assertGeometry(original, persisted);
		assertEquals(copied.getParentAlgorithm().getClass(),
				persisted.getParentAlgorithm().getClass());
		assertInputs(copied, persisted);
		if (stage.witnessLabel() != null) {
			assertArc(persisted, named(reopened, stage.witnessLabel()), Math.PI / 2);
		}
		assertEquals(GeoNumeric.class, named(reopened, "obj").getClass());
		assertEquals(41, named(reopened, "obj").evaluateDouble(), EPS);
		trace(sample, "reopen", "witness, typed producer, labeled inputs and stored UUID");

		GeoElement driver = named(stage.source(), sample.driver());
		GeoElement mergedDriver = named(reopened, stage.driverLabel());
		double[] initial = witness(original);
		String initialText = original instanceof GeoText text ? text.getTextString() : null;
		move(driver, sample.moved());
		move(mergedDriver, sample.moved());
		assertGeometry(original, persisted);
		if (original instanceof GeoText text) {
			assertNotEquals(initialText, text.getTextString());
		} else {
			assertTrue(!Arrays.equals(initial, witness(original)));
		}
		if (sample.singular() != null) {
			move(driver, sample.singular());
			move(mergedDriver, sample.singular());
			assertGeometry(original, persisted);
			trace(sample, "singular", "defined=" + original.isDefined()
					+ " type=" + ((GeoConic) original).getType());
			move(driver, sample.moved());
			move(mergedDriver, sample.moved());
			assertGeometry(original, persisted);
		}
		trace(sample, "dynamic", "paired input change after reopen");
		if (stage.witnessLabel() != null) {
			GeoElement pathPoint = named(reopened, stage.witnessLabel());
			assertArc(persisted, pathPoint, Math.PI);
			move(mergedDriver, new double[] {0, -2});
			assertArc(persisted, pathPoint, 3 * Math.PI / 2);
			move(mergedDriver, new double[] {4, 0});
			assertArc(persisted, pathPoint, 0);
			mergedDriver.setUndefined();
			mergedDriver.updateCascade();
			assertFalse(persisted.isDefined());
			move(mergedDriver, new double[] {0, 5});
			assertArc(persisted, pathPoint, Math.PI / 2);
			trace(sample, "analytic", "end ray off circle, seam, zero extent, "
					+ "undefined input, recovery, path identity and presentation");
		}
		return null;
	}

	private static void assertArc(GeoElement geo, GeoElement point, double extent) {
		assertEquals(GeoConicPart.class, geo.getClass());
		var arc = (GeoConicPart) geo;
		assertEquals(AlgoConicPartCircle.class, arc.getParentAlgorithm().getClass());
		assertEquals(GeoConicNDConstants.CONIC_PART_ARC, arc.getConicPartType());
		assertEquals(GeoConicNDConstants.CONIC_CIRCLE, arc.getType());
		assertTrue(arc.isDefined());
		assertTrue(arc.positiveOrientation());
		assertEquals(2, arc.getHalfAxis(0), EPS);
		assertEquals(extent, arc.getParameterExtent(), EPS);
		assertEquals(2 * extent, arc.getArcLength(), EPS);
		assertEquals(0, arc.getMinParameter(), EPS);
		assertEquals(1, arc.getMaxParameter(), EPS);
		assertOnCircle(arc.getPointParam(0), 0);
		assertOnCircle(arc.getPointParam(1), extent);
		var pathPoint = (GeoPoint) point;
		assertSame(arc, pathPoint.getPath());
		assertEquals(0.5, pathPoint.getPathParameter().getT(), EPS);
		assertOnCircle(pathPoint, extent / 2);
		assertEquals(2, arc.getLayer());
		assertEquals(GColor.RED, arc.getObjectColor());
		assertTrue(arc.isSetEuclidianVisible());
	}

	private static void assertOnCircle(GeoPoint point, double angle) {
		assertEquals(2 * Math.cos(angle), point.getInhomX(), EPS);
		assertEquals(2 * Math.sin(angle), point.getInhomY(), EPS);
	}

	private static GeoElement uniqueAdded(GeoElement original, List<GeoElement> added) {
		var matches = added.stream().filter(geo -> geo.getClass() == original.getClass()).toList();
		assertEquals(1, matches.size(), "Ambiguous or absent output in synthetic fixture");
		return matches.get(0);
	}

	private static void assertMappedEdges(Map<GeoElement, GeoElement> copies) {
		for (var pair : copies.entrySet()) {
			var from = pair.getKey().getParentAlgorithm();
			var to = pair.getValue().getParentAlgorithm();
			if (from == null) {
				assertNull(to);
				continue;
			}
			assertNotNull(to);
			assertEquals(from.getClass(), to.getClass());
			assertEquals(from.getInput().length, to.getInput().length);
			for (int i = 0; i < from.getInput().length; i++) {
				GeoElement input = from.getInput()[i];
				if (input.isLabelSet()) {
					assertNotNull(copies.get(input), input.getLabelSimple());
					assertSame(copies.get(input), to.getInput(i));
				}
			}
		}
	}

	private static void assertInputs(GeoElement first, GeoElement second) {
		var before = first.getParentAlgorithm().getInput();
		var after = second.getParentAlgorithm().getInput();
		assertEquals(before.length, after.length);
		for (int i = 0; i < before.length; i++) {
			if (before[i].isLabelSet()) {
				assertEquals(before[i].getLabelSimple(), after[i].getLabelSimple());
			}
		}
	}

	private static void assertGeometry(GeoElement source, GeoElement copy) {
		assertEquals(source.getClass(), copy.getClass());
		assertEquals(source.isDefined(), copy.isDefined());
		if (!source.isDefined()) {
			return;
		}
		assertArrayEquals(witness(source), witness(copy), EPS);
		if (source instanceof GeoText text) {
			assertEquals(text.getTextString(), ((GeoText) copy).getTextString());
		}
	}

	private static double[] witness(GeoElement geo) {
		if (geo instanceof GeoConicPart part) {
			var values = new ArrayList<Double>();
			for (double coefficient : part.getMatrix()) {
				values.add(coefficient);
			}
			values.addAll(List.of(part.getParameterStart(), part.getParameterEnd(),
					part.getParameterExtent(), part.getArcLength(), part.getDouble(),
					part.positiveOrientation() ? 1.0 : 0.0));
			return values.stream().mapToDouble(Double::doubleValue).toArray();
		}
		if (geo instanceof GeoConic conic) {
			double[] matrix = conic.getMatrix();
			return new double[] {conic.getType(), matrix[0], matrix[1], matrix[2],
					matrix[3], matrix[4], matrix[5]};
		}
		if (geo instanceof GeoCurveCartesian curve) {
			double lo = curve.getMinParameter();
			double hi = curve.getMaxParameter();
			return new double[] {lo, hi, curve.evaluateCurve(lo).getY(),
					curve.evaluateCurve((lo + hi) / 2).getY(), curve.evaluateCurve(hi).getY(),
					curve.evaluateCurve(lo).getX(), curve.evaluateCurve(hi).getX()};
		}
		if (geo instanceof GeoList list) {
			double[] values = new double[list.size()];
			for (int i = 0; i < list.size(); i++) {
				values[i] = list.get(i).evaluateDouble();
			}
			return values;
		}
		return new double[0];
	}

	private static void move(GeoElement geo, double[] value) {
		if (geo instanceof GeoPoint point) {
			point.setCoords(value[0], value[1], 1);
		} else {
			((GeoNumeric) geo).setValue(value[0]);
		}
		geo.updateCascade();
	}

	private static GeoElement named(AppGeoCeDG app, String label) {
		GeoElement geo = app.getKernel().lookupLabel(label);
		assertNotNull(geo, label);
		return geo;
	}

	private static String normalizedXml(AppGeoCeDG app) {
		return GeoCeDGDocumentInsertion.Certificate.normalize(app.getXML());
	}

	private static void trace(Case sample, String step, String observation) {
		System.out.printf("P4PROBE %s %s %s%n", sample.name(), step, observation);
	}

	private record Stage(AppGeoCeDG source, AppGeoCeDG target, String label, String driverLabel,
			String witnessLabel, String before, String uuid, String nativeUuid, int commandCount) {
	}
}
