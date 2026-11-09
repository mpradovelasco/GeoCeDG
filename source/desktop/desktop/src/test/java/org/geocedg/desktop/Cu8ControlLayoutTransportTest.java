/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED;
import static org.geocedg.desktop.GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET;
import static org.geocedg.desktop.GeoCeDGDocumentInsertion.Outcome.INSERTED;
import static org.geocedg.desktop.GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION;
import static org.geocedg.desktop.PreG9BR6PlusF3Documents.M;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.insert;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.save;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.store;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;

import org.geocedg.common.document.controls.ControlLayout;
import org.geocedg.common.document.controls.ControlLayout.Anchor;
import org.geocedg.common.document.controls.ControlLayout.Panel;
import org.geocedg.common.document.controls.ControlLayout.Slot;
import org.geocedg.common.document.controls.ControlLayoutException;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.geos.AbsoluteScreenLocateable;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.main.App;
import org.geogebra.desktop.gui.ContextMenuGeoElementD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Panel layout is documentary state on the receiving side of fragment transport. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class Cu8ControlLayoutTransportTest {
	@TempDir
	Path directory;

	@Test
	void rejectsEmptyAndPopulatedSourcesBeforeCopyOrAngularConsent() throws Exception {
		onEdt(() -> {
			AppGeoCeDG receiver = receiver(Kernel.ANGLE_RADIANT).app();
			var before = new PreG9BR6PlusF3Documents.Observation(receiver);
			assertTrue(undo(receiver).redoPossible());
			for (boolean populated : new boolean[] {false, true}) {
				File source = markedSource(populated);
				var service = receiver.getDocumentInsertion();
				assertSourceRejected(receiver, service.insert(source));
				assertSourceRejected(receiver, service.insert(source,
						ignored -> fail("Panel source reached angular consent")));
				var after = new PreG9BR6PlusF3Documents.Observation(receiver);
				assertEquals(List.of(), before.serializableDifferences(after));
				assertTrue(before.sameInstances(after));
			}
			return null;
		});
	}

	@Test
	void expertRefusesSourceAndReceiverWithoutWideningPassiveCoverage() throws Exception {
		onEdt(() -> {
			AppGeoCeDG receiver = receiver(Kernel.ANGLE_DEGREE).app();
			var before = new PreG9BR6PlusF3Documents.Observation(receiver);
			var exception = assertThrows(IOException.class,
					receiver.getDocumentInsertion()::beginExpertReview);
			assertEquals(receiver.layerText("Insert.Reject.ControlLayoutReceiver"),
					exception.getMessage());
			for (boolean populated : new boolean[] {false, true}) {
				byte[] source = Files.readAllBytes(markedSource(populated).toPath());
				var inspection = GeoCeDGInspectionArchive.inspect(source);
				assertFalse(inspection.loadable());
				assertTrue(inspection.findings().stream().anyMatch(finding ->
						finding.code() == GeoCeDGInspectionArchive.Code.CONTROL_LAYOUT));
				assertEquals(receiver.layerText("Insert.Reject.ControlLayoutSource"),
						assertThrows(IOException.class, () ->
								GeoCeDGExpertIntegrity.loadSource(receiver, source)).getMessage());
			}
			var after = new PreG9BR6PlusF3Documents.Observation(receiver);
			assertEquals(List.of(), before.serializableDifferences(after));
			assertTrue(before.sameInstances(after));
			return null;
		});
	}

	@Test
	void partialAndWholeClipboardSelectionsCopyLooseControls() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = app(M);
			var members = List.of(slider(app, "n"), must(app, "run=Button(\"Run\")"),
					must(app, "flag=Checkbox(\"Show\")"), must(app, "edit=InputBox(n)"));
			Panel panel = layout(app).addPanel(Anchor.WORLD, 1, -2, 290, 180, true);
			for (GeoElement control : members) {
				prepareControl(control);
				assertTrue(layout(app).isEligible(control), control.getLabelSimple());
				layout(app).addSlot(panel.id(), control, 6, 8);
			}
			baseline(app);
			for (int size : new int[] {1, members.size()}) {
				var before = GeoCeDGDocumentInsertion.identitySet(app);
				app.getCopyPaste().copyToXML(app, members.subList(0, size));
				app.getCopyPaste().pasteFromXML(app);
				List<GeoElement> copies = GeoCeDGDocumentInsertion.createdSince(app, before);
				for (GeoElement selected : members.subList(0, size)) {
					assertTrue(copies.stream().anyMatch(copy -> copy.getClass() == selected.getClass()),
							selected.getLabelSimple());
				}
				assertEquals(List.of(panel), layout(app).panels());
				assertEquals(members.size(), layout(app).slots(panel.id()).size());
				for (GeoElement member : members) {
					assertSame(member, layout(app).slot(member).control());
				}
				for (GeoElement copy : copies) {
					assertNull(layout(app).slot(copy));
				}
			}
			return null;
		});
	}

	@Test
	void nativePlacementEditsRequireDetachmentBeforeChangingTheSelection() throws Exception {
		onEdt(() -> {
			AppGeoCeDG target = app(M);
			GeoNumeric member = slider(target, "n");
			GeoNumeric loose = slider(target, "m");
			Panel panel = layout(target).addPanel(Anchor.FIXED, 50, 60, 120, 90, true);
			Slot original = layout(target).addSlot(panel.id(), member, 14, 20);
			baseline(target);
			var before = new PreG9BR6PlusF3Documents.Observation(target);
			ArrayList<GeoElement> selection = new ArrayList<>(List.of(member, loose));
			var menu = new ContextMenuGeoElementD(target, selection, new Point(80, 80));
			Arrays.stream(menu.getWrappedPopup().getComponents())
					.filter(JCheckBoxMenuItem.class::isInstance).map(JCheckBoxMenuItem.class::cast)
					.filter(item -> target.getLocalization().getMenu("FixObject").equals(item.getText()))
					.findFirst().orElseThrow().doClick();
			assertFalse(member.isLockedPosition());
			assertFalse(loose.isLockedPosition());
			assertThrows(ControlLayoutException.class, () ->
					target.getKernel().getConstruction().createGroupFromSelected(selection));
			assertThrows(ControlLayoutException.class, () -> member.addView(App.VIEW_EUCLIDIAN2));
			assertSame(original, layout(target).slot(member));
			var after = new PreG9BR6PlusF3Documents.Observation(target);
			assertEquals(List.of(), before.serializableDifferences(after));
			assertTrue(before.sameInstances(after));
			return null;
		});
	}

	@Test
	void ordinaryAndAngularInsertionKeepReceiverMembershipThroughUndoAndRedo() throws Exception {
		onEdt(() -> {
			for (boolean mixed : new boolean[] {false, true}) {
				Receiver fixture = receiver(mixed ? Kernel.ANGLE_RADIANT : Kernel.ANGLE_DEGREE);
				AppGeoCeDG target = fixture.app();
				AppGeoCeDG source = app(M);
				source.getKernel().setAngleUnit(Kernel.ANGLE_DEGREE);
				if (mixed) {
					GeoAngle angle = (GeoAngle) must(source, "theta=47°");
					angle.setEuclidianVisible(true);
					angle.setIntervalMin(0);
					angle.setIntervalMax(2);
					angle.setAutoStep(false);
					angle.setAnimationStep(0.015625);
					angle.setAnimationSpeed(1.75);
					must(source, "n=sin(theta)");
					must(source, "c=cos(theta)");
					must(source, "Added=(c,n)");
				} else {
					must(source, "Added=(2,3)");
					must(source, "n=7");
				}
				File file = save(source, directory, "source-" + mixed);
				int cursor = undo(target).getCursor();
				var prefix = undo(target).getCommands().subList(0, cursor);
				assertTrue(undo(target).redoPossible());
				GeoElement original = lookup(target, "n");
				var result = mixed ? target.getDocumentInsertion().insert(file, preview -> true)
						: insert(target, file);
				assertEquals(INSERTED, result.getOutcome(),
						() -> result.getMessage() + " " + result.getFailure());
				assertSame(original, lookup(target, "n"));
				assertMember(fixture, original);
				assertNull(layout(target).slot(lookup(target, "n_1")));
				assertEquals(cursor + 1, undo(target).getCursor());
				assertFalse(undo(target).redoPossible());
				for (int i = 0; i < cursor; i++) {
					assertSame(prefix.get(i), undo(target).getCommands().get(i));
				}
				undo(target).undo();
				assertNull(target.getKernel().lookupLabel("Added"));
				assertMember(fixture, lookup(target, "n"));
				undo(target).redo();
				assertNotNull(lookup(target, "Added"));
				assertMember(fixture, lookup(target, "n"));
			}
			return null;
		});
	}

	@Test
	void failureKeepsRedoWithSameOwnerOrCertifiedNativeReclaim() throws Exception {
		onEdt(() -> {
			File source = PreG9BR6PlusF3Documents.single(directory, "rollback-source", "Added=(2,3)");
			for (boolean reload : new boolean[] {false, true}) {
				Receiver fixture = receiver(Kernel.ANGLE_DEGREE);
				AppGeoCeDG target = fixture.app();
				GeoElement original = lookup(target, "n");
				var before = new PreG9BR6PlusF3Documents.Observation(target);
				target.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
					private boolean firstAttach = true;

					@Override
					public void beforeAttach() {
						if (!firstAttach) {
							return;
						}
						firstAttach = false;
						if (!reload) {
							throw new IllegalStateException("CU8 injected publication failure");
						}
						Slot slot = layout(target).slot(original);
						layout(target).updateSlot(new Slot(slot.panelId(), slot.id(), original, 31, 61));
					}
				});
				var result = insert(target, source);
				assertEquals(reload ? FAILED_RECOVERY_CERTIFIED_SESSION_RESET : FAILED_RECOVERY_CERTIFIED,
						result.getOutcome(), () -> result.getMessage() + " " + result.getFailure());
				var after = new PreG9BR6PlusF3Documents.Observation(target);
				assertEquals(List.of(), before.serializableDifferences(after));
				assertTrue(undo(target).redoPossible());
				GeoElement restored = lookup(target, "n");
				assertMember(fixture, restored);
				if (reload) {
					assertNotSame(original, restored);
					assertNull(layout(target).slot(original));
				} else {
					assertSame(original, restored);
				}
				undo(target).redo();
				assertNotNull(lookup(target, "RedoPoint"));
				assertNull(target.getKernel().lookupLabel("Added"));
				assertMember(fixture, lookup(target, "n"));
			}
			return null;
		});
	}

	private File markedSource(boolean populated) {
		AppGeoCeDG source = app(M);
		source.getKernel().setAngleUnit(Kernel.ANGLE_DEGREE);
		must(source, "Added=(2,3)");
		GeoNumeric numeric = slider(source, "sourceSlider");
		Panel panel = layout(source).addPanel(Anchor.FIXED, 31, 73, 230, 100, true);
		if (populated) {
			layout(source).addSlot(panel.id(), numeric, 10, 20);
		}
		return save(source, directory, "marked-" + populated);
	}

	private static Receiver receiver(int angularMode) throws Exception {
		AppGeoCeDG receiver = app(M);
		receiver.getKernel().setAngleUnit(angularMode);
		must(receiver, "A=(1,2)");
		GeoNumeric resident = slider(receiver, "n");
		Panel panel = layout(receiver).addPanel(Anchor.FIXED, 120, 80, 320, 120, true);
		Slot slot = layout(receiver).addSlot(panel.id(), resident, 13, 27);
		baseline(receiver);
		must(receiver, "RedoPoint=(4,5)");
		store(receiver);
		undo(receiver).undo();
		awaitQuiescent(receiver);
		return new Receiver(receiver, panel, slot.id());
	}

	private static GeoNumeric slider(AppGeoCeDG app, String label) {
		GeoNumeric slider = (GeoNumeric) must(app, label + "=Slider(0,10,1)");
		slider.setAbsoluteScreenLocActive(true);
		slider.setSliderFixed(false);
		prepareControl(slider);
		return slider;
	}

	private static void prepareControl(GeoElement control) {
		control.setViewFlags(new ArrayList<>(List.of(App.VIEW_EUCLIDIAN)));
		control.setEuclidianVisible(true);
		((AbsoluteScreenLocateable) control).setAbsoluteScreenLoc(40, 58);
		if (control instanceof GeoBoolean checkbox) {
			checkbox.setCheckboxFixed(false);
		}
	}

	private static GeoElement lookup(AppGeoCeDG app, String label) {
		return G9U1TestApp.lookup(app, label);
	}

	private static ControlLayout layout(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getControlLayout();
	}

	private static void assertSourceRejected(AppGeoCeDG receiver,
			GeoCeDGDocumentInsertion.Result result) {
		assertEquals(REJECTED_BEFORE_MUTATION, result.getOutcome());
		assertTrue(result.getMessage().contains(receiver.layerText("Insert.Reject.ControlLayoutSource")));
		assertNull(receiver.getKernel().lookupLabel("Added"));
	}

	private static void assertMember(Receiver receiver, GeoElement owner) {
		ControlLayout layout = layout(receiver.app());
		assertEquals(List.of(receiver.panel()), layout.panels());
		assertEquals(1, layout.slots(receiver.panel().id()).size());
		Slot member = layout.slot(owner);
		assertNotNull(member);
		assertSame(owner, member.control());
		assertEquals(receiver.slotId(), member.id());
		assertEquals(13, member.dx());
		assertEquals(27, member.dy());
	}

	private record Receiver(AppGeoCeDG app, Panel panel, String slotId) {
	}
}
