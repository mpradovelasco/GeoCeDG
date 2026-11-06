/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED;
import static org.geocedg.desktop.GeoCeDGDocumentInsertion.Outcome.INSERTED;
import static org.geocedg.desktop.GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.desktop.GeoCeDGDocumentInsertion.AngularPreview;
import org.geocedg.desktop.GeoCeDGDocumentInsertion.Result;
import org.geocedg.desktop.PreG9BR6PlusF3Documents.Observation;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.io.MyXMLio;
import org.geogebra.common.kernel.CircularDefinitionException;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoDependentNumber;
import org.geogebra.common.kernel.algos.AlgoDependentPoint;
import org.geogebra.common.kernel.arithmetic.MyVecNode;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoAngle.AngleStyle;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.plugin.ActionType;
import org.geogebra.common.plugin.Operation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** CU4-R2 mixed angle modes through the real Desktop insertion transaction. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class Cu4B2InsertionTest {
	private static final int[] MODES = {Kernel.ANGLE_DEGREE, Kernel.ANGLE_RADIANT,
			Kernel.ANGLE_DEGREES_MINUTES_SECONDS};
	private static final String LITERAL = "theta is a literal · π";
	private static final String ANGLE_TAG = "<angleUnit\\b[^>]*/>";

	@TempDir
	Path dir;

	@Test
	void confirmsEveryDirectedPairAndRetainsTheTypedConstruction() throws Exception {
		onEdt(() -> {
			for (int from : MODES) {
				File source = archive(source(from, false), "source-" + from,
						from == Kernel.ANGLE_RADIANT);
				byte[] bytes = Files.readAllBytes(source.toPath());
				for (int to : MODES) {
					if (from == to) {
						continue;
					}
					AppGeoCeDG receiver = receiver(to);
					if (from == Kernel.ANGLE_DEGREE && to == Kernel.ANGLE_RADIANT) {
						receiver.getKernel().setPrintDecimals(5);
					}
					AppGeoCeDG original = receiver.newInsertionHelper();
					try {
						assertTrue(original.loadFile(source, false));
						assertEquals(from, original.getKernel().getAngleUnit());
						confirmAndCheck(source, original, receiver, from, to);
						assertArrayEquals(bytes, Files.readAllBytes(source.toPath()));
					} finally {
						receiver.releaseInsertionHelper(original);
					}
				}
			}
			return null;
		});
	}

	@Test
	void cancellationAndStaleOffersDoNotReachTheTransaction() throws Exception {
		onEdt(() -> {
			File source = archive(source(Kernel.ANGLE_DEGREE, false), "candidate", false);
			byte[] originalBytes = Files.readAllBytes(source.toPath());
			AppGeoCeDG cancelled = receiver(Kernel.ANGLE_RADIANT);
			Observation beforeCancel = new Observation(cancelled);
			AtomicInteger confirmations = new AtomicInteger();
			AtomicInteger barriers = countBarriers(cancelled);
			Result declined = cancelled.getDocumentInsertion().insert(source, preview -> {
				confirmations.incrementAndGet();
				return false;
			});
			assertRejected(declined);
			assertNull(declined.getMessage());
			assertEquals(1, confirmations.get());
			assertEquals(0, barriers.get());
			assertUnchanged(cancelled, beforeCancel);
			assertArrayEquals(originalBytes, Files.readAllBytes(source.toPath()));

			File replacement = archive(source(Kernel.ANGLE_DEGREE, true), "replacement", false);
			byte[] replacementBytes = Files.readAllBytes(replacement.toPath());
			for (Invalidation invalidation : Invalidation.values()) {
				File offered = Files.copy(source.toPath(),
						dir.resolve("stale-" + invalidation.name() + ".cedg")).toFile();
				AppGeoCeDG receiver = receiver(Kernel.ANGLE_RADIANT);
				if (invalidation == Invalidation.HISTORY) {
					undo(receiver).storeAction(ActionType.UPDATE, new String[0],
							ActionType.UPDATE);
					receiver.setSaved();
				}
				awaitQuiescent(receiver);
				Observation before = new Observation(receiver);
				AtomicReference<Observation> changed = new AtomicReference<>();
				AtomicInteger entered = countBarriers(receiver);
				assertRejected(receiver.getDocumentInsertion().insert(offered, preview -> {
					assertUnchanged(receiver, before);
					switch (invalidation) {
					case GEOMETRY:
						must(receiver, "External=(3,4)");
						break;
					case MODE:
						receiver.getKernel().setAngleUnit(Kernel.ANGLE_DEGREES_MINUTES_SECONDS);
						break;
					case RECEIVER_FILE:
						receiver.setCurrentFile(replacement);
						break;
					case SOURCE_FILE:
						replaceBytes(offered, replacementBytes);
						break;
					case HISTORY:
						undo(receiver).undo();
						awaitQuiescent(receiver);
						receiver.setSaved();
						assertEquals(before.cursor - 1, undo(receiver).getCursor());
						assertEquals(List.of(), before.certificate.serializableDifferences(
								new Observation(receiver).certificate));
						break;
					}
					assertNull(changed.getAndSet(new Observation(receiver)));
					return true;
				}));
				assertNotNull(changed.get(), invalidation.name());
				assertEquals(0, entered.get(), invalidation.name());
				assertUnchanged(receiver, changed.get());
				assertArrayEquals(invalidation == Invalidation.SOURCE_FILE
						? replacementBytes : originalBytes, Files.readAllBytes(offered.toPath()));
			}
			return null;
		});
	}

	@Test
	void keepsSameModeRouteAndRejectsUncertifiedConsumers() throws Exception {
		onEdt(() -> {
			for (int mode : MODES) {
				File source = archive(source(mode, false), "same-" + mode, false);
				AppGeoCeDG receiver = receiver(mode);
				assertInserted(receiver.getDocumentInsertion().insert(source,
						preview -> fail("Same-mode insertion offered mixed adaptation")));
				assertEquals(mode, receiver.getKernel().getAngleUnit());
			}

			AppGeoCeDG text = source(Kernel.ANGLE_DEGREE, false);
			must(text, "formatted=Text(theta)");
			File computedText = archive(text, "computed-text", true);
			AppGeoCeDG ordinary = receiver(Kernel.ANGLE_DEGREE);
			assertInserted(ordinary.getDocumentInsertion().insert(computedText,
					preview -> fail("Same-mode computed text offered mixed adaptation")));
			assertNotNull(lookup(ordinary, "formatted"));
			must(text, "chars=Length(formatted)");
			File textConsumer = archive(text, "text-consumer", false);
			AppGeoCeDG floor = source(Kernel.ANGLE_DEGREE, true);
			GeoElement expression = must(floor, "number=sin(theta)+floor(theta)");
			assertEquals(0, expression.evaluateDouble());
			File modeSensitiveNumber = archive(floor, "floor", false);

			File valid = archive(source(Kernel.ANGLE_DEGREE, false), "explicit-mode", false);
			File absent = rewriteEntry(valid, "absent", MyXMLio.XML_FILE, ANGLE_TAG, "");
			File malformed = rewriteEntry(valid, "malformed", MyXMLio.XML_FILE, ANGLE_TAG,
					"<angleUnit val=\"unsupported\"/>");
			File duplicate = rewriteEntry(valid, "duplicate", MyXMLio.XML_FILE, ANGLE_TAG,
					"<angleUnit val=\"degree\"/><angleUnit val=\"radiant\"/>");
			File library = rewriteEntry(valid, "custom-library", MyXMLio.JAVASCRIPT_FILE,
					"(?s)^.*$", Kernel.defaultLibraryJavaScript
							+ "\nggbApplet.registerClientListener(function(){});");
			File bound = rewriteEntry(valid, "default-bound-command", MyXMLio.XML_FILE_DEFAULTS_2D,
					"<slider min=\"[^\"]*\"", "<slider min=\"Distance((0,0),(1,0))\"");
			File anchor = rewriteEntry(valid, "legacy-anchor-command", MyXMLio.XML_FILE,
					"<startPoint exp=\"A\"\\s*/>",
					"<startPoint label=\"Point((1,2))\" x=\"1\" y=\"2\" z=\"1\"/>");
			AppGeoCeDG receiver = receiver(Kernel.ANGLE_RADIANT);
			AtomicInteger barriers = countBarriers(receiver);
			Observation before = new Observation(receiver);
			assertRejected(receiver.getDocumentInsertion().insert(valid));
			assertUnchanged(receiver, before);
			for (File file : List.of(computedText, textConsumer, modeSensitiveNumber,
					absent, malformed, duplicate, library, bound, anchor)) {
				assertRejected(receiver.getDocumentInsertion().insert(file,
						preview -> fail("Uncertified source offered: " + file.getName())));
				assertUnchanged(receiver, before);
			}
			assertEquals(0, barriers.get());
			return null;
		});
	}

	@Test
	void restoresMixedModeHistoryOnFailureAndPersistsRemappedInsertion() throws Exception {
		onEdt(() -> {
			File source = archive(source(Kernel.ANGLE_DEGREE, false), "lifecycle", true);
			byte[] sourceBytes = Files.readAllBytes(source.toPath());
			File originalFile = PreG9BR6PlusF3Documents.locus(dir, "native-receiver");
			AppGeoCeDG receiver = PreG9BCu0Corpus.open(originalFile);
			receiver.getKernel().setAngleUnit(Kernel.ANGLE_RADIANT);
			GeoElement oldAngleLabel = must(receiver, "theta=10");
			GeoElement oldAnchor = lookup(receiver, "A");
			GeoLocusV2 oldLocus = (GeoLocusV2) lookup(receiver, "L");
			var locusId = oldLocus.getPersistentLocusId();
			assertNotNull(locusId);
			baseline(receiver);
			Observation before = new Observation(receiver);
			AtomicInteger confirmations = new AtomicInteger();
			var insertion = receiver.getDocumentInsertion();
			AtomicInteger corruptions = new AtomicInteger();
			insertion.setFaults(new GeoCeDGDocumentInsertion.Faults() {
				@Override
				public void afterCommit() {
					GeoPoint copied = assertInstanceOf(GeoPoint.class, lookup(receiver, "P"));
					assertEquals(8, copied.getPointSize());
					copied.setPointSize(4);
					corruptions.incrementAndGet();
				}
			});
			Result failed = insertion.insert(source, preview -> {
				confirmations.incrementAndGet();
				return true;
			});
			assertEquals(FAILED_RECOVERY_CERTIFIED, failed.getOutcome(), failed.getMessage());
			var failure = assertInstanceOf(IllegalStateException.class, failed.getFailure());
			assertTrue(failure.getMessage().startsWith("verification failed:"),
					failure::getMessage);
			assertEquals(1, corruptions.get());
			assertEquals(1, confirmations.get());
			assertUnchanged(receiver, before);
			assertArrayEquals(sourceBytes, Files.readAllBytes(source.toPath()));
			insertion.setFaults(null);

			AppGeoCeDG original = receiver.newInsertionHelper();
			try {
				assertTrue(original.loadFile(source, false));
				assertInserted(insertion.insert(source, preview -> true));
				awaitQuiescent(receiver);
				assertEquals(before.cursor + 1, undo(receiver).getCursor());
				assertSame(oldAngleLabel, lookup(receiver, "theta"));
				assertSame(oldAnchor, lookup(receiver, "A"));
				assertSame(oldLocus, lookup(receiver, "L"));
				assertContent(original, receiver);
				undo(receiver).undo();
				awaitQuiescent(receiver);
				assertEquals(before.cursor, undo(receiver).getCursor());
				assertNull(lookup(receiver, "theta_{1}"));
				assertNull(lookup(receiver, "P"));
				assertEquals(Kernel.ANGLE_RADIANT, receiver.getKernel().getAngleUnit());
				undo(receiver).redo();
				awaitQuiescent(receiver);
				assertEquals(before.cursor + 1, undo(receiver).getCursor());
				assertEquals(locusId,
						((GeoLocusV2) lookup(receiver, "L")).getPersistentLocusId());
				assertContent(original, receiver);

				for (boolean compatibility : new boolean[]{false, true}) {
					File merged = archive(receiver, "merged", compatibility);
					AppGeoCeDG reopened = PreG9BCu0Corpus.open(merged);
					assertEquals(Kernel.ANGLE_RADIANT, reopened.getKernel().getAngleUnit());
					assertEquals(locusId,
							((GeoLocusV2) lookup(reopened, "L")).getPersistentLocusId());
					assertContent(original, reopened);
				}
				assertArrayEquals(sourceBytes, Files.readAllBytes(source.toPath()));
			} finally {
				receiver.releaseInsertionHelper(original);
			}
			return null;
		});
	}

	private static void confirmAndCheck(File source, AppGeoCeDG original, AppGeoCeDG receiver,
			int from, int to) {
		List<String> order = new ArrayList<>();
		Observation before = new Observation(receiver);
		AtomicReference<AngularPreview> confirmed = new AtomicReference<>();
		AtomicInteger coldHelpers = new AtomicInteger();
		boolean coldTrial = from == Kernel.ANGLE_DEGREE;
		boolean coldSource = coldTrial && to == Kernel.ANGLE_DEGREES_MINUTES_SECONDS;
		var insertion = coldTrial
				? withColdHelpers(receiver, () -> coldSource || confirmed.get() != null,
						coldHelpers) : receiver.getDocumentInsertion();
		insertion.setFaults(new GeoCeDGDocumentInsertion.Faults() {
			@Override
			public void beforeSnapshot() {
				order.add("transaction");
			}
		});
		assertInserted(insertion.insert(source, preview -> {
			assertTrue(SwingUtilities.isEventDispatchThread());
			assertUnchanged(receiver, before);
			assertEquals(source.getName(), preview.sourceName());
			assertEquals(from, preview.sourceMode());
			assertEquals(to, preview.targetMode());
			assertEquals(1, preview.samples().size());
			assertEquals("theta", preview.samples().get(0).label());
			assertEquals(presentation(lookup(original, "theta")), preview.samples().get(0).original());
			order.add("confirm");
			assertNull(confirmed.getAndSet(preview));
			if (coldTrial && !coldSource) {
				assertRejected(insertion.insert(source,
						ignored -> fail("Reentrant insertion offered a second confirmation")));
				assertUnchanged(receiver, before);
			}
			return true;
		}));
		assertEquals(coldSource ? 3 : coldTrial ? 1 : 0, coldHelpers.get());
		assertEquals(List.of("confirm", "transaction"), order);
		assertEquals(to, receiver.getKernel().getAngleUnit());
		assertEquals(presentation(lookup(receiver, "theta_{1}")),
				confirmed.get().samples().get(0).adapted());
		assertNotEquals(confirmed.get().samples().get(0).original(),
				confirmed.get().samples().get(0).adapted());
		awaitQuiescent(receiver);
		assertEquals(before.cursor + 1, undo(receiver).getCursor());
		assertContent(original, receiver);
		assertRecomputesAcrossTheDomain(receiver);
	}

	private static AppGeoCeDG source(int mode, boolean zero) throws CircularDefinitionException {
		AppGeoCeDG source = app(M);
		source.getKernel().setAngleUnit(mode);
		double raw = mode == Kernel.ANGLE_RADIANT ? -1.25 : Math.toRadians(40.5);
		AngleStyle style = mode == Kernel.ANGLE_RADIANT ? AngleStyle.UNBOUNDED
				: AngleStyle.ANTICLOCKWISE;
		GeoAngle angle = mode == Kernel.ANGLE_DEGREE
				? (GeoAngle) must(source, zero ? "theta=0°" : "theta=47°")
				: new GeoAngle(source.getKernel().getConstruction(), raw, style, false);
		if (!angle.isLabelSet()) {
			angle.setLabel("theta");
		}
		angle.setAngleStyle(style);
		angle.setEuclidianVisible(true);
		angle.setIntervalMin(mode == Kernel.ANGLE_RADIANT ? -2 : 0);
		angle.setIntervalMax(2);
		angle.setAutoStep(false);
		angle.setAnimationStep(0.015625);
		angle.setAnimationSpeed(1.75);
		angle.setAnimationType(GeoElement.ANIMATION_DECREASING);
		angle.setLabelMode(GeoElement.LABEL_NAME_VALUE);
		angle.setLabelVisible(true);
		angle.setLineThickness(6);
		angle.setLineOpacity(160);
		must(source, "s=sin(theta)");
		must(source, "c=cos(theta)").setObjColor(GColor.BLUE);
		GeoPoint point = (GeoPoint) must(source, "P=(c,s)");
		point.setPointSize(8);
		point.setPointStyle(2);
		GeoPoint anchor = (GeoPoint) must(source, "A=(1,2)");
		((GeoText) must(source, "literal=\"" + LITERAL + "\"")).setStartPoint(anchor);
		return source;
	}

	private static AppGeoCeDG receiver(int mode) throws IOException {
		AppGeoCeDG receiver = app(M);
		receiver.getKernel().setAngleUnit(mode);
		must(receiver, "theta=10");
		must(receiver, "s=5");
		must(receiver, "A=(20,30)");
		baseline(receiver);
		return receiver;
	}

	private static void assertContent(AppGeoCeDG original, AppGeoCeDG receiver) {
		GeoAngle expected = (GeoAngle) lookup(original, "theta");
		GeoAngle angle = assertInstanceOf(GeoAngle.class, lookup(receiver, "theta_{1}"));
		assertTrue(angle.isIndependent());
		assertEquals(angleSlots(expected), angleSlots(angle));
		assertEquals(6, angle.getLineThickness());
		assertEquals(160, angle.getLineOpacity());
		if (angle.getDefinition() != null) {
			assertNotNull(expected.getDefinition());
			assertEquals(expected.getDefinition().toString(StringTemplate.xmlTemplate),
					angle.getDefinition().toString(StringTemplate.xmlTemplate));
		}
		GeoElement s = lookup(receiver, "s_{1}");
		GeoElement c = lookup(receiver, "c");
		assertScalar(original, angle, s, "s", Operation.SIN);
		assertScalar(original, angle, c, "c", Operation.COS);
		assertEquals(GColor.BLUE, c.getStaticObjectColor());
		GeoPoint point = (GeoPoint) lookup(receiver, "P");
		GeoPoint originalPoint = (GeoPoint) lookup(original, "P");
		var algo = assertInstanceOf(AlgoDependentPoint.class, point.getParentAlgorithm());
		assertEquals(Set.of(c, s), Set.of(algo.getInput()));
		MyVecNode vector = assertInstanceOf(MyVecNode.class, algo.getExpression().unwrap());
		assertEquals(Kernel.COORD_CARTESIAN, vector.getToStringMode());
		assertSame(c, vector.getX().unwrap());
		assertSame(s, vector.getY().unwrap());
		assertEquals(originalPoint.getInhomX(), point.getInhomX());
		assertEquals(originalPoint.getInhomY(), point.getInhomY());
		assertEquals(8, point.getPointSize());
		assertEquals(2, point.getPointStyle());

		GeoText text = (GeoText) lookup(receiver, "literal");
		assertEquals(LITERAL, text.getTextString());
		assertSame(lookup(receiver, "A_{1}"), text.getStartPoint());
	}

	private static List<Object> angleSlots(GeoAngle angle) {
		return List.of(angle.getRawAngle(), angle.getValue(), angle.getAngleStyle(),
				angle.isKeepDegrees(), angle.isAutoStep(), angle.isAnimating(), angle.isRandom(),
				angle.getIntervalMin(), angle.getIntervalMinObject().getAngleDim(),
				angle.getIntervalMax(), angle.getIntervalMaxObject().getAngleDim(),
				angle.getAnimationStep(), angle.getAnimationStepObject().getAngleDim(),
				angle.getAnimationSpeed(), angle.getAnimationType(), angle.isSliderHorizontal(),
				angle.getSliderWidth(), angle.getSliderBlobSize(), angle.isLockedPosition(),
				angle.isAbsoluteScreenLocActive(), angle.getSliderX(), angle.getSliderY(),
				angle.isEuclidianVisible(), angle.getLabelMode(), angle.isLabelVisible());
	}

	private static void assertScalar(AppGeoCeDG original, GeoAngle angle, GeoElement scalar,
			String name, Operation operation) {
		var algo = assertInstanceOf(AlgoDependentNumber.class, scalar.getParentAlgorithm());
		assertArrayEquals(new GeoElement[]{angle}, algo.getInput());
		assertEquals(operation, algo.getExpression().getOperation());
		assertSame(angle, algo.getExpression().getLeft().unwrap());
		assertEquals(lookup(original, name).evaluateDouble(), scalar.evaluateDouble());
	}

	private static void assertRecomputesAcrossTheDomain(AppGeoCeDG receiver) {
		GeoAngle angle = (GeoAngle) lookup(receiver, "theta_{1}");
		GeoElement sine = lookup(receiver, "s_{1}");
		GeoElement cosine = lookup(receiver, "c");
		GeoPoint point = (GeoPoint) lookup(receiver, "P");
		double raw = angle.getRawAngle();
		try {
			double min = angle.getIntervalMin();
			double max = angle.getIntervalMax();
			for (double sample : new double[]{min, (min + max) / 2, max}) {
				angle.setValue(sample);
				angle.updateCascade();
				assertEquals(Math.sin(angle.getValue()), sine.evaluateDouble());
				assertEquals(Math.cos(angle.getValue()), cosine.evaluateDouble());
				assertEquals(cosine.evaluateDouble(), point.getInhomX());
				assertEquals(sine.evaluateDouble(), point.getInhomY());
			}
		} finally {
			angle.setValue(raw);
			angle.updateCascade();
		}
	}

	private File archive(AppGeoCeDG source, String name, boolean compatibility) throws Exception {
		if (!compatibility) {
			return save(source, dir, name);
		}
		Path file = dir.resolve(name + ".ggb");
		try (var output = Files.newOutputStream(file)) {
			source.getXMLio().writeGeoGebraFile(output, false);
		}
		return file.toFile();
	}

	private File rewriteEntry(File source, String name, String targetEntry, String pattern,
			String replacement) throws IOException {
		Path output = dir.resolve(name + ".cedg");
		boolean rewritten = false;
		try (var input = new ZipInputStream(Files.newInputStream(source.toPath()));
				var zip = new ZipOutputStream(Files.newOutputStream(output))) {
			for (ZipEntry entry; (entry = input.getNextEntry()) != null;) {
				zip.putNextEntry(new ZipEntry(entry.getName()));
				if (targetEntry.equals(entry.getName())) {
					String xml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
					String changed = xml.replaceFirst(pattern, replacement);
					assertNotEquals(xml, changed, name);
					zip.write(changed.getBytes(StandardCharsets.UTF_8));
					rewritten = true;
				} else {
					input.transferTo(zip);
				}
				zip.closeEntry();
			}
		}
		assertTrue(rewritten, targetEntry);
		return output.toFile();
	}

	private static AtomicInteger countBarriers(AppGeoCeDG receiver) {
		AtomicInteger entries = new AtomicInteger();
		receiver.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
			@Override
			public void beforeSnapshot() {
				entries.incrementAndGet();
			}
		});
		return entries;
	}

	private static void replaceBytes(File file, byte[] bytes) {
		try {
			Files.write(file.toPath(), bytes);
		} catch (IOException failed) {
			fail(failed);
		}
	}

	private static GeoElement lookup(AppGeoCeDG app, String label) {
		return app.getKernel().lookupLabel(label);
	}

	private static String presentation(GeoElement angle) {
		return angle.toValueString(StringTemplate.defaultTemplate);
	}

	private static void assertUnchanged(AppGeoCeDG receiver, Observation before) {
		Observation after = new Observation(receiver);
		assertEquals(List.of(), before.serializableDifferences(after));
		assertTrue(before.sameInstances(after));
	}

	private static void assertInserted(Result result) {
		assertEquals(INSERTED, result.getOutcome(),
				() -> result.getMessage() + " " + result.getFailure());
	}

	private static void assertRejected(Result result) {
		assertEquals(REJECTED_BEFORE_MUTATION, result.getOutcome(),
				() -> result.getMessage() + " " + result.getFailure());
		assertNull(result.getFailure(), result::getMessage);
	}

	private static GeoCeDGDocumentInsertion withColdHelpers(AppGeoCeDG receiver,
			BooleanSupplier shouldClear, AtomicInteger coldHelpers) {
		AppGeoCeDG receiverWithColdHelpers = mock(AppGeoCeDG.class, delegatesTo(receiver));
		doAnswer(invocation -> {
			AppGeoCeDG helper = receiver.newInsertionHelper();
			if (shouldClear.getAsBoolean()) {
				awaitQuiescent(helper);
				// Reproduce the empty history seen before the file loader's worker stores.
				helper.getKernel().setUndoActive(false);
				undo(helper).clearUndoInfo();
				assertTrue(undo(helper).getCommands().isEmpty());
				coldHelpers.incrementAndGet();
			}
			return helper;
		}).when(receiverWithColdHelpers).newInsertionHelper();
		return new GeoCeDGDocumentInsertion(receiverWithColdHelpers);
	}

	private enum Invalidation {
		GEOMETRY, MODE, RECEIVER_FILE, SOURCE_FILE, HISTORY
	}
}
