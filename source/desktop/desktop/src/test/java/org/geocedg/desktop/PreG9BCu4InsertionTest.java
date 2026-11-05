/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Documents.M;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.insert;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.save;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoAngle.AngleStyle;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** CU4 B01 admission, fidelity, history and rollback for the two new P4 rows. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu4InsertionTest {

	@TempDir
	Path dir;

	@Test
	void authorGgbInsertsIntoNativeReceiverWithoutReplacingItsObjects() throws Exception {
		File source = PreG9BCu0Corpus.workingCopy(PreG9BCu0Corpus.GGB, dir);
		// This native fixture carries a verified public V2 DAG and durable identities.
		File receiver = onEdt(() -> PreG9BR6PlusF3Documents.locus(dir, "native-receiver"));
		AppGeoCeDG target = onEdt(() -> PreG9BCu0Corpus.open(receiver));
		onEdt(() -> {
			baseline(target);
			return null;
		});
		Set<GeoElement> original = onEdt(() -> GeoCeDGDocumentInsertion.identitySet(target));
		String id = target.getUniqueId();
		assertInserted(insert(target, source));
		awaitQuiescent(target);
		onEdt(() -> {
			assertEquals(id, target.getUniqueId());
			for (GeoElement existing : original) {
				assertSame(existing, target.getKernel().lookupLabel(existing.getLabelSimple()));
			}
			List<GeoElement> inserted = GeoCeDGDocumentInsertion.createdSince(target, original);
			List<Double> angles = inserted.stream().filter(geo -> geo instanceof GeoAngle
					&& geo.isIndependent()).map(geo -> ((GeoAngle) geo).getRawAngle()).sorted()
					.toList();
			assertEquals(List.of(Math.toRadians(40), Math.toRadians(47)), angles);
			assertEquals(1, inserted.stream().filter(geo -> geo instanceof GeoText
					&& geo.isIndependent()).count());
		});
	}

	@Test
	void nativeReceiverOutsideTheInheritedSnapshotContractIsRejectedBeforeMutation()
			throws Exception {
		File source = PreG9BCu0Corpus.workingCopy(PreG9BCu0Corpus.GGB, dir);
		File receiver = PreG9BCu0Corpus.workingCopy(PreG9BCu0Corpus.CEDG, dir);
		AppGeoCeDG target = onEdt(() -> PreG9BCu0Corpus.open(receiver));
		onEdt(() -> {
			baseline(target);
			return null;
		});
		PreG9BR6PlusF3Documents.Observation before = onEdt(
				() -> new PreG9BR6PlusF3Documents.Observation(target));
		GeoCeDGDocumentInsertion.Result result = insert(target, source);
		assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
				result.getOutcome(), result.getMessage());
		assertTrue(result.getMessage().contains("verified copy of the drawing"),
				result.getMessage());
		onEdt(() -> assertUnchanged(target, before));
	}

	@Test
	void angleAndLiteralTextSurviveCollisionsHistoryAndFileReopen() throws Exception {
		File source = source(Kernel.ANGLE_DEGREE, false);
		AppGeoCeDG target = target(Kernel.ANGLE_DEGREE);
		GeoElement oldAnchor = target.getKernel().lookupLabel("A");
		GeoElement oldBound = target.getKernel().lookupLabel("lo");
		int cursor = undo(target).getCursor();
		GeoCeDGDocumentInsertion.Result result = insert(target, source);
		assertInserted(result);
		awaitQuiescent(target);
		assertEquals(cursor + 1, undo(target).getCursor());
		assertSame(oldAnchor, target.getKernel().lookupLabel("A"));
		assertSame(oldBound, target.getKernel().lookupLabel("lo"));
		assertContents(target);
		onEdt(() -> undo(target).undo());
		awaitQuiescent(target);
		assertNull(target.getKernel().lookupLabel("theta"));
		assertNull(target.getKernel().lookupLabel("textAnchor"));
		onEdt(() -> undo(target).redo());
		awaitQuiescent(target);
		assertContents(target);

		File merged = onEdt(() -> save(target, dir, "merged"));
		AppGeoCeDG reopened = onEdt(() -> app(M));
		assertTrue(onEdt(() -> reopened.loadFile(merged, false)));
		assertContents(reopened);

		onEdt(() -> {
			GeoNumeric old = (GeoNumeric) reopened.getKernel().lookupLabel("lo");
			GeoNumeric copied = (GeoNumeric) reopened.getKernel().lookupLabel("lo_1");
			GeoAngle angle = (GeoAngle) reopened.getKernel().lookupLabel("theta");
			old.setValue(0.4);
			old.updateCascade();
			assertEquals(0.1, angle.getIntervalMin());
			copied.setValue(0.2);
			copied.updateCascade();
			assertEquals(0.2, angle.getIntervalMin());
			assertEquals(0.21, angle.getAnimationStep(), 1e-15);
		});
	}

	@Test
	void angleUnitsMustMatchEvenAcrossTwoDegreeDisplayModes() throws Exception {
		int[] units = {Kernel.ANGLE_DEGREE, Kernel.ANGLE_RADIANT,
				Kernel.ANGLE_DEGREES_MINUTES_SECONDS};
		for (int sourceUnit : units) {
			File source = source(sourceUnit, true);
			for (int targetUnit : units) {
				AppGeoCeDG target = target(targetUnit);
				PreG9BR6PlusF3Documents.Observation before =
						new PreG9BR6PlusF3Documents.Observation(target);
				GeoCeDGDocumentInsertion.Result result = insert(target, source);
				if (sourceUnit == targetUnit) {
					assertInserted(result);
					GeoAngle angle = (GeoAngle) target.getKernel().lookupLabel("theta");
					assertNotNull(angle);
					assertTrue(angle.isAutoStep());
					assertEquals(targetUnit, target.getKernel().getAngleUnit());
				} else {
					assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
							result.getOutcome(), result.getMessage());
					assertTrue(result.getMessage().contains("different angle units"),
							result.getMessage());
					assertUnchanged(target, before);
				}
			}
		}
	}

	@Test
	void crossUnitCopyCanPreserveAngularValuesWhilePublicInsertionRejects() throws Exception {
		onEdt(() -> {
			assertCrossUnitCounterexample(Kernel.ANGLE_DEGREE, Kernel.ANGLE_RADIANT,
					Math.toRadians(47), AngleStyle.ANTICLOCKWISE);
			assertCrossUnitCounterexample(Kernel.ANGLE_RADIANT, Kernel.ANGLE_DEGREES_MINUTES_SECONDS,
					-1.25, AngleStyle.UNBOUNDED);
			assertCrossUnitCounterexample(Kernel.ANGLE_DEGREES_MINUTES_SECONDS, Kernel.ANGLE_DEGREE,
					Math.toRadians(40.5), AngleStyle.ANTICLOCKWISE);
			assertPresentationDependentTextIsUnitSensitive();
			return null;
		});
	}

	private void assertPresentationDependentTextIsUnitSensitive() throws Exception {
		AppGeoCeDG origin = app(M);
		origin.getKernel().setAngleUnit(Kernel.ANGLE_DEGREE);
		GeoAngle slider = (GeoAngle) must(origin, "theta=47°");
		slider.setEuclidianVisible(true);
		must(origin, "formatted=Text(theta)");
		File source = save(origin, dir, "cross-unit-text");
		AppGeoCeDG loaded = app(M);
		assertTrue(loaded.loadFile(source, false));
		AppGeoCeDG receiver = app(M);
		receiver.getKernel().setAngleUnit(Kernel.ANGLE_RADIANT);
		new GeoCeDGInsertionCopyPaste(Set.of()).insertFrom(loaded, receiver, Set.of(), false);
		GeoText original = (GeoText) loaded.getKernel().lookupLabel("formatted");
		GeoText copied = (GeoText) receiver.getKernel().lookupLabel("formatted");
		assertNotEquals(original.getTextString(), copied.getTextString());
	}

	private void assertCrossUnitCounterexample(int from, int to, double raw, AngleStyle style)
			throws Exception {
		AppGeoCeDG origin = app(M);
		origin.getKernel().setAngleUnit(from);
		GeoAngle slider = from == Kernel.ANGLE_DEGREE
				? (GeoAngle) must(origin, "theta=47°")
				: new GeoAngle(origin.getKernel().getConstruction(), raw, style, false);
		if (!slider.isLabelSet()) {
			slider.setLabel("theta");
		}
		slider.setEuclidianVisible(true);
		slider.setIntervalMin(style == AngleStyle.UNBOUNDED ? -2 : 0);
		slider.setIntervalMax(2);
		slider.setAutoStep(false);
		slider.setAnimationStep(0.015625);
		slider.setAnimationSpeed(1.75);
		slider.setAnimationType(GeoElement.ANIMATION_DECREASING);
		must(origin, "dependent=sin(theta)");
		File source = save(origin, dir, "cross-unit-" + from);
		AppGeoCeDG receiver = app(M);
		receiver.getKernel().setAngleUnit(to);
		baseline(receiver);
		var before = new PreG9BR6PlusF3Documents.Observation(receiver);
		var rejected = insert(receiver, source);
		assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
				rejected.getOutcome(), rejected.getMessage());
		assertUnchanged(receiver, before);

		// Exercise the native copy independently of the public admission gate.
		AppGeoCeDG loaded = app(M);
		assertTrue(loaded.loadFile(source, false));
		var clipboard = new GeoCeDGInsertionCopyPaste(Set.of());
		clipboard.insertFrom(loaded, receiver, Set.of(), false);
		var original = (GeoAngle) loaded.getKernel().lookupLabel("theta");
		var copied = (GeoAngle) receiver.getKernel().lookupLabel("theta");
		assertSame(clipboard.getCopies().get(original), copied);
		assertEquals(raw, original.getRawAngle());
		assertEquals(raw, copied.getRawAngle());
		assertEquals(original.getValue(), copied.getValue());
		assertEquals(original.getAngleStyle(), copied.getAngleStyle());
		assertEquals(original.isKeepDegrees(), copied.isKeepDegrees());
		assertEquals(original.getIntervalMin(), copied.getIntervalMin());
		assertEquals(original.getIntervalMax(), copied.getIntervalMax());
		assertEquals(original.getIntervalMinObject().getAngleDim(),
				copied.getIntervalMinObject().getAngleDim());
		assertEquals(original.getIntervalMaxObject().getAngleDim(),
				copied.getIntervalMaxObject().getAngleDim());
		assertEquals(original.getAnimationStep(), copied.getAnimationStep());
		assertEquals(original.getAnimationStepObject().getAngleDim(),
				copied.getAnimationStepObject().getAngleDim());
		assertEquals(original.getAnimationSpeed(), copied.getAnimationSpeed());
		assertEquals(original.isAutoStep(), copied.isAutoStep());
		assertEquals(original.isAnimating(), copied.isAnimating());
		assertEquals(original.getAnimationType(), copied.getAnimationType());
		assertEquals(to, receiver.getKernel().getAngleUnit());
		assertNotEquals(original.toValueString(StringTemplate.defaultTemplate),
				copied.toValueString(StringTemplate.defaultTemplate));
		GeoElement dependent = receiver.getKernel().lookupLabel("dependent");
		assertSame(copied, dependent.getParentAlgorithm().getInput()[0]);
		GeoElement originalDependent = loaded.getKernel().lookupLabel("dependent");
		assertEquals(originalDependent.evaluateDouble(), dependent.evaluateDouble());
		double sample = (original.getIntervalMin() + original.getIntervalMax()) / 2;
		original.setValue(sample);
		copied.setValue(sample);
		original.updateCascade();
		copied.updateCascade();
		assertEquals(originalDependent.evaluateDouble(), dependent.evaluateDouble());
	}

	@Test
	void equalValueWrongReferenceTriggersCertifiedRollback() throws Exception {
		File source = source(Kernel.ANGLE_DEGREE, false);
		for (boolean changeAngle : new boolean[] {true, false}) {
			AppGeoCeDG target = target(Kernel.ANGLE_DEGREE);
			PreG9BR6PlusF3Documents.Observation before =
					new PreG9BR6PlusF3Documents.Observation(target);
			target.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
				@Override
				public void afterCommit() throws Exception {
					Kernel kernel = target.getKernel();
					if (changeAngle) {
						((GeoAngle) kernel.lookupLabel("theta")).setIntervalMin(
								(GeoNumeric) kernel.lookupLabel("lo"));
					} else {
						((GeoText) kernel.lookupLabel("textAnchor")).setStartPoint(
								(GeoPoint) kernel.lookupLabel("A"));
					}
				}
			});
			GeoCeDGDocumentInsertion.Result result = insert(target, source);
			assertEquals(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED,
					result.getOutcome(), result.getMessage());
			assertTrue(result.getFailure().getMessage().contains(
					changeAngle ? "angle slider theta" : "static text textAnchor"),
					String.valueOf(result.getFailure()));
			assertUnchanged(target, before);
		}
	}

	@Test
	void angleWithInvalidNativeIntervalStillRejected() throws Exception {
		File source = onEdt(() -> {
			AppGeoCeDG app = app(M);
			GeoAngle angle = (GeoAngle) must(app, "theta=47°");
			angle.setEuclidianVisible(true);
			angle.setIntervalMin(1);
			angle.setIntervalMax(1);
			return save(app, dir, "invalid-interval");
		});
		AppGeoCeDG target = target(Kernel.ANGLE_DEGREE);
		PreG9BR6PlusF3Documents.Observation before =
				new PreG9BR6PlusF3Documents.Observation(target);
		GeoCeDGDocumentInsertion.Result result = insert(target, source);
		assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
				result.getOutcome(), result.getMessage());
		assertTrue(result.getMessage().contains("theta"), result.getMessage());
		assertUnchanged(target, before);
	}

	private File source(int unit, boolean autoStep) throws Exception {
		return onEdt(() -> {
			AppGeoCeDG source = app(M);
			source.getKernel().setAngleUnit(unit);
			GeoNumeric min = (GeoNumeric) must(source, "lo=0.1");
			GeoNumeric max = (GeoNumeric) must(source, "hi=1.5");
			GeoNumeric speed = (GeoNumeric) must(source, "spd=2");
			GeoAngle angle = (GeoAngle) must(source, "theta=47°");
			angle.setEuclidianVisible(true);
			angle.setIntervalMin(min);
			angle.setIntervalMax(max);
			angle.setAutoStep(autoStep);
			if (!autoStep) {
				angle.setAnimationStep(source.getKernel().getAlgebraProcessor()
						.evaluateToNumeric("lo+0.01", source.getDefaultErrorHandler()));
			}
			angle.setAnimationSpeedObject(speed);
			angle.setAnimationType(GeoElement.ANIMATION_DECREASING);
			angle.setAnimating(true);
			angle.setSliderHorizontal(false);
			angle.setSliderFixed(true);
			angle.setAbsoluteScreenLocActive(true);
			angle.setSliderWidth(123, true);
			angle.setSliderLocation(30, 40, true);
			angle.setLabelMode(GeoElement.LABEL_NAME_VALUE);
			angle.setLabelVisible(true);
			GeoPoint anchor = (GeoPoint) must(source, "A=(1,2)");
			GeoText text = (GeoText) must(source, "textAnchor=\"A · lo · π\"");
			text.setStartPoint(anchor);
			GeoText pixel = (GeoText) must(source, "textPixel=\"x^2\"");
			pixel.setLaTeX(true, false);
			pixel.setAbsoluteScreenLocActive(true);
			pixel.setAbsoluteScreenLoc(27, 61);
			GeoText world = (GeoText) must(source, "textWorld=\"world\"");
			world.setRealWorldLoc(1.25, -2.5);
			must(source, "dependent=sin(theta)");
			return save(source, dir, "source-" + unit + "-" + autoStep);
		});
	}

	private static AppGeoCeDG target(int unit) throws Exception {
		return onEdt(() -> {
			AppGeoCeDG target = app(M);
			target.getKernel().setAngleUnit(unit);
			must(target, "A=(1,2)");
			must(target, "lo=0.1");
			must(target, "hi=1.5");
			must(target, "spd=2");
			baseline(target);
			return target;
		});
	}

	private static void assertContents(AppGeoCeDG app) throws Exception {
		onEdt(() -> {
			Kernel kernel = app.getKernel();
			GeoAngle angle = (GeoAngle) kernel.lookupLabel("theta");
			assertEquals(Math.toRadians(47), angle.getRawAngle());
			assertSame(kernel.lookupLabel("lo_1"), angle.getIntervalMinObject());
			assertSame(kernel.lookupLabel("hi_1"), angle.getIntervalMaxObject());
			assertSame(kernel.lookupLabel("spd_1"), angle.getAnimationSpeedObject());
			assertSame(angle, kernel.lookupLabel("dependent").getParentAlgorithm().getInput()[0]);
			assertEquals(0.11, angle.getAnimationStep(), 1e-15);
			assertEquals(GeoElement.ANIMATION_DECREASING, angle.getAnimationType());
			assertTrue(angle.isAnimating());
			assertEquals(GeoElement.LABEL_NAME_VALUE, angle.getLabelMode());
			assertTrue(angle.getLabelVisible());
			assertTrue(angle.isAbsoluteScreenLocActive());
			assertTrue(angle.isLockedPosition());
			assertFalse(angle.isSliderHorizontal());
			assertEquals(123, angle.getSliderWidth());
			assertEquals(30, angle.getSliderX());
			assertEquals(40, angle.getSliderY());
			GeoText anchored = (GeoText) kernel.lookupLabel("textAnchor");
			assertSame(kernel.lookupLabel("A_1"), anchored.getStartPoint());
			assertEquals("A · lo · π", anchored.getTextString());
			GeoText pixel = (GeoText) kernel.lookupLabel("textPixel");
			assertTrue(pixel.isLaTeX());
			assertTrue(pixel.isAbsoluteScreenLocActive());
			assertEquals(27, pixel.getAbsoluteScreenLocX());
			assertEquals(61, pixel.getAbsoluteScreenLocY());
			GeoText world = (GeoText) kernel.lookupLabel("textWorld");
			assertFalse(world.isAbsoluteScreenLocActive());
			assertEquals(1.25, world.getRealWorldLocX());
			assertEquals(-2.5, world.getRealWorldLocY());
		});
	}

	private static void assertInserted(GeoCeDGDocumentInsertion.Result result) {
		assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED,
				result.getOutcome(), () -> result.getMessage() + " " + result.getFailure());
	}

	private static void assertUnchanged(AppGeoCeDG target,
			PreG9BR6PlusF3Documents.Observation before) {
		PreG9BR6PlusF3Documents.Observation after =
				new PreG9BR6PlusF3Documents.Observation(target);
		assertEquals(List.of(), before.serializableDifferences(after));
		assertTrue(before.sameInstances(after));
	}
}
