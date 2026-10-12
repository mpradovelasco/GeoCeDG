/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.insert;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.sleep;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.swing.JPanel;

import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.undo.UndoCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-F3 Stage A recovery: K1-S, K1-R-H guarded reload, serious-failure
 * isolation with its routes and exits, attach quiescence (G3), recovery-source capture
 * (G4) and exception preservation (G7) (FR-F3-1 to FR-F3-6).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusF3RecoveryTest {

	@TempDir
	Path dir;

	/** Isolation dialog answered by the test. */
	static final class Scripted implements GeoCeDGInsertionIsolation.Presentation {
		final Deque<GeoCeDGInsertionIsolation.Exit> exits = new ArrayDeque<>();
		final Deque<File> copyTargets = new ArrayDeque<>();
		final List<String> messages = new ArrayList<>();
		Runnable probe;
		boolean confirm;
		int choices;

		@Override
		public GeoCeDGInsertionIsolation.Exit choose(AppGeoCeDG app, String message,
				boolean reopenAvailable) {
			messages.add(message);
			choices++;
			if (probe != null) {
				Runnable once = probe;
				probe = null;
				once.run();
			}
			return exits.isEmpty() ? GeoCeDGInsertionIsolation.Exit.CLOSE : exits.poll();
		}

		@Override
		public File chooseCopyTarget(AppGeoCeDG app, File suggestion) {
			return copyTargets.poll();
		}

		@Override
		public boolean confirmReplace(AppGeoCeDG app, File target) {
			return confirm;
		}

		@Override
		public void inform(AppGeoCeDG app, String text) {
			messages.add(text);
		}
	}

	private static GeoCeDGDocumentInsertion.Faults failAfterCommit() {
		return new GeoCeDGDocumentInsertion.Faults() {
			@Override
			public void afterCommit() {
				throw new IllegalStateException("F3 injected post-commit failure");
			}
		};
	}

	private static boolean labelEverReappears(AppGeoCeDG app, String label) throws Exception {
		GeoCeDGUndoManager manager = undo(app);
		int start = manager.getCursor();
		boolean seen = app.getKernel().lookupLabel(label) != null;
		while (manager.undoPossible()) {
			onEdt(manager::undo);
			seen |= app.getKernel().lookupLabel(label) != null;
		}
		while (manager.redoPossible()) {
			onEdt(manager::redo);
			seen |= app.getKernel().lookupLabel(label) != null;
		}
		while (manager.getCursor() > start && manager.undoPossible()) {
			onEdt(manager::undo);
		}
		return seen;
	}

	@Test
	void f3r01SubtractiveRecoveryRestoresEverythingWithoutReset() throws Exception {
		for (boolean saved : new boolean[] {true, false}) {
			AppGeoCeDG target = PreG9BR6PlusF3Documents.target(dir, "r1-" + saved);
			if (!saved) {
				target.setUnsaved();
			}
			target.getDocumentInsertion().setFaults(failAfterCommit());
			PreG9BR6PlusF3Documents.Observation before =
					new PreG9BR6PlusF3Documents.Observation(target);
			GeoCeDGDocumentInsertion.Result result = insert(target,
					PreG9BR6PlusF3Documents.macro(dir, "r1-src-" + saved, false));
			assertEquals(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED,
					result.getOutcome(), String.valueOf(result.getFailure()));
			assertTrue(result.getMessage().contains("The drawing is unchanged"));
			assertTrue(result.getDeclaredResets().isEmpty());
			PreG9BR6PlusF3Documents.Observation after =
					new PreG9BR6PlusF3Documents.Observation(target);
			assertEquals(List.of(), before.serializableDifferences(after));
			assertTrue(before.sameInstances(after), "instances, F2 memory and export link kept");
			assertNull(target.getKernel().getMacro("MyMid"), "transferred macro removed");
			assertEquals(saved, target.isSaved());
			assertFalse(target.isInsertionIsolated());
			assertFalse(labelEverReappears(target, "N"));
		}
	}

	@Test
	void f3r02AFailedRemovalFallsBackToTheGuardedReload() throws Exception {
		AppGeoCeDG target = PreG9BR6PlusF3Documents.target(dir, "r2");
		target.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
			@Override
			public void afterCommit() {
				throw new IllegalStateException("F3 injected post-commit failure");
			}

			@Override
			public void duringRemoval() {
				throw new IllegalStateException("F3 injected removal failure");
			}
		});
		PreG9BR6PlusF3Documents.Observation before = new PreG9BR6PlusF3Documents.Observation(
				target);
		GeoCeDGDocumentInsertion.Result result = insert(target,
				PreG9BR6PlusF3Documents.elementary(dir, "r2-src"));
		assertEquals(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET,
				result.getOutcome(), String.valueOf(result.getFailure()));
		assertEquals(List.of(), before.serializableDifferences(
				new PreG9BR6PlusF3Documents.Observation(target)));
		assertTrue(result.getDeclaredResets().contains("Insert.Reset.Instances"));
		assertTrue(result.getFailure().getSuppressed().length > 0,
				"G7: cleanup kept as suppressed");
		assertFalse(labelEverReappears(target, "m"));
	}

	@Test
	void f3r03IdentityPayloadsRecoverByTheGuardedReloadOfTheBarrierBaseline()
			throws Exception {
		AppGeoCeDG target = PreG9BR6PlusF3Documents.target(dir, "r3");
		target.getDocumentInsertion().setFaults(failAfterCommit());
		PreG9BR6PlusF3Documents.Observation before = new PreG9BR6PlusF3Documents.Observation(
				target);
		String uuid = target.getUniqueId();
		GeoCeDGDocumentInsertion.Result result = insert(target,
				PreG9BR6PlusF3Documents.locus(dir, "r3-src"));
		assertEquals(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET,
				result.getOutcome(), String.valueOf(result.getFailure()));
		awaitQuiescent(target);
		PreG9BR6PlusF3Documents.Observation after = new PreG9BR6PlusF3Documents.Observation(
				target);
		// the paste's own store landed after the commit; the reload source was captured
		// by reference at the barrier (G4), and the held history is back unchanged
		assertEquals(List.of(), before.serializableDifferences(after));
		assertEquals(uuid, target.getUniqueId());
		assertTrue(result.getDeclaredResets().contains("Insert.Reset.ExportLink"));
		assertTrue(result.getMessage().contains("unlinked"), result.getMessage());
		assertFalse(labelEverReappears(target, "S"));
		assertFalse(labelEverReappears(target, "lenS"));
	}

	@Test
	void f3r04ALateStoreBeforeAttachIsCancelledByAClosingBarrier() throws Exception {
		AppGeoCeDG target = PreG9BR6PlusF3Documents.target(dir, "r4");
		int[] calls = {0};
		target.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
			@Override
			public void beforeAttach() {
				if (calls[0]++ == 0) {
					// an unrelated store of the operation generation, still pending
					target.storeUndoInfo();
				}
			}
		});
		PreG9BR6PlusF3Documents.Observation before = new PreG9BR6PlusF3Documents.Observation(
				target);
		GeoCeDGDocumentInsertion.Result result = insert(target,
				PreG9BR6PlusF3Documents.single(dir, "r4-src", "W=(3,3)"));
		assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED, result.getOutcome(),
				String.valueOf(result.getFailure()));
		sleep(400);
		awaitQuiescent(target);
		List<UndoCommand> history = undo(target).getCommands();
		assertEquals(before.cursor + 1, history.size(), "the late store never lands");
		for (int i = 0; i < before.cursor; i++) {
			assertSame(before.commands.get(i), history.get(i));
		}
	}

	@Test
	void f3r05SeriousFailureIsolatesAndOnlyTheExitsRemain() throws Exception {
		AppGeoCeDG target = PreG9BR6PlusF3Documents.target(dir, "r5");
		File current = target.getCurrentFile();
		final byte[] currentBytes = Files.readAllBytes(current.toPath());
		final String uuid = target.getUniqueId();
		final String preInsertion = GeoCeDGDocumentInsertion.Certificate.normalize(target.getXML());
		target.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
			@Override
			public void afterCommit() {
				throw new IllegalStateException("F3 injected post-commit failure");
			}

			@Override
			public void beforeReload(int attempt) {
				throw new IllegalStateException("F3 injected reload failure " + attempt);
			}
		});
		List<String> sink = new ArrayList<>();
		target.setInsertionMessageSink(sink::add);
		Scripted dialog = new Scripted();
		target.getInsertionIsolation().setPresentation(dialog);
		File copy = dir.resolve("r5-recovered.cedg").toFile();
		dialog.copyTargets.add(current);
		dialog.copyTargets.add(copy);
		dialog.exits.add(GeoCeDGInsertionIsolation.Exit.SAVE_COPY);
		dialog.exits.add(GeoCeDGInsertionIsolation.Exit.SAVE_COPY);
		dialog.exits.add(GeoCeDGInsertionIsolation.Exit.REOPEN);
		List<String> routes = new ArrayList<>();
		dialog.probe = () -> {
			try {
				routes.addAll(probeRoutes(target, dir));
			} catch (Exception failure) {
				routes.add("probe failed: " + failure);
			}
		};
		GeoCeDGDocumentInsertion.Result result = insert(target,
				PreG9BR6PlusF3Documents.locus(dir, "r5-src"));
		assertEquals(GeoCeDGDocumentInsertion.Outcome.SERIOUS_RECOVERY_FAILURE,
				result.getOutcome());
		assertEquals(3, dialog.choices, "save copy twice, then reopen");
		assertNull(dialog.probe, "the route probe ran while isolated");
		assertEquals(List.of(), routes, "every mutation route refused while isolated");
		assertTrue(dialog.messages.get(0).contains("could not be verified"));
		// exit 1: refused over the current file, then a verified copy with the identity
		assertTrue(dialog.messages.stream().anyMatch(m -> m.contains("new file")));
		assertTrue(copy.isFile());
		byte[] copyBytes = Files.readAllBytes(copy.toPath());
		assertEquals(uuid, GeoCeDGRecoverySnapshot.documentId(copyBytes));
		AppGeoCeDG reopenedCopy = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.MM);
		assertTrue(reopenedCopy.loadFile(copy, false));
		assertEquals(construction(preInsertion),
				construction(GeoCeDGDocumentInsertion.Certificate.normalize(reopenedCopy.getXML())),
				"the copy is the pre-insertion document");
		// the current file was never written
		assertTrue(java.util.Arrays.equals(currentBytes, Files.readAllBytes(current.toPath())));
		// exit 3: reopen from the file, undo re-enabled first; isolation ended
		assertFalse(target.isInsertionIsolated());
		assertTrue(target.getKernel().isUndoActive());
		assertFalse(target.isBlockUpdateScripts());
		assertNull(target.getInsertionIsolation().getSnapshot());
		assertEquals(current, target.getCurrentFile());
		assertNull(target.getKernel().lookupLabel("S"));
		assertNull(target.getKernel().lookupLabel("H2"), "reloaded from the saved file");
	}

	private static String construction(String xml) {
		int start = xml.indexOf("<construction");
		int end = xml.lastIndexOf("</construction>");
		return start < 0 || end < 0 ? xml : xml.substring(start, end);
	}

	/** Every route that could change, save or export the drawing; returns violations. */
	private static List<String> probeRoutes(AppGeoCeDG target, Path dir) throws Exception {
		List<String> violations = new ArrayList<>();
		if (!target.isInsertionIsolated()) {
			violations.add("not isolated");
		}
		File other = dir.resolve("r5-live.cedg").toFile();
		if (target.saveGeoGebraFile(other) || other.exists()) {
			violations.add("save of the live state");
		}
		GuiManagerGeoCeDG gui = (GuiManagerGeoCeDG) target.getGuiManager();
		if (gui.save() || gui.saveAs()) {
			violations.add("Save / Save As");
		}
		for (String action : new String[] {"document.save", "document.save-as",
				"document.insert-file", "document.apply-styles", "export.dxf-2d",
				"document.new", "document.open"}) {
			if (gui.getActionRegistry().unavailableReason(action) == null) {
				violations.add("action " + action);
			}
		}
		int count = target.getKernel().getConstruction().getGeoSetConstructionOrder().size();
		target.getCopyPaste().pasteFromXML(target, false);
		if (target.getKernel().getConstruction().getGeoSetConstructionOrder().size() != count) {
			violations.add("paste");
		}
		GeoCeDGUndoManager manager = undo(target);
		if (manager.undoPossible() || manager.redoPossible()) {
			violations.add("Undo/Redo");
		}
		manager.undo();
		if (target.getKernel().getConstruction().getGeoSetConstructionOrder().size() != count) {
			violations.add("undo changed the drawing");
		}
		if (!target.isBlockUpdateScripts()) {
			violations.add("update scripts");
		}
		if (target.getKernel().getAnimationManager().isRunning()) {
			violations.add("animation");
		}
		if (target.saveCurrentFile()) {
			violations.add("save prompt route");
		}
		if (target.clearConstruction()) {
			violations.add("New");
		}
		gui.doOpenFiles(new File[] {target.getCurrentFile()}, true);
		if (!target.isInsertionIsolated()) {
			violations.add("file drop / open");
		}
		KeyEvent delete = new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED, 0, 0,
				KeyEvent.VK_DELETE, KeyEvent.CHAR_UNDEFINED);
		if (target.dispatchKeyEvent(delete)) {
			violations.add("global keys");
		}
		if (target.getDocumentInsertion().insert(dir.resolve("x.cedg").toFile())
				.getOutcome() != GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION) {
			violations.add("Insert File");
		}
		if (target.getStyleApplication().apply(dir.resolve("x.cedg").toFile()).isApplied()) {
			violations.add("Apply Styles");
		}
		GeoElement first = target.getKernel().getConstruction().getGeoSetConstructionOrder()
				.first();
		target.copyGraphicsViewToClipboard(target.getEuclidianView1());
		if (first == null) {
			violations.add("empty drawing");
		}
		// pointer edits are refused before the event is even read
		org.geogebra.common.euclidian.event.AbstractEvent pointer = org.mockito.Mockito.mock(
				org.geogebra.common.euclidian.event.AbstractEvent.class);
		GeoCeDGEuclidianController controller = (GeoCeDGEuclidianController) target
				.getEuclidianView1().getEuclidianController();
		controller.wrapMousePressed(pointer);
		controller.wrapMouseDragged(pointer, true);
		controller.wrapMouseReleased(pointer);
		if (!org.mockito.Mockito.mockingDetails(pointer).getInvocations().isEmpty()) {
			violations.add("pointer editing");
		}
		// Algebra input submissions never reach an isolated drawing
		GeoElement[][] submitted = {null};
		GeoCeDGAlgebraInputSubmission.submit(target, "Zq=(1,1)",
				new org.geogebra.common.kernel.commands.EvalInfo(true),
				org.geogebra.common.main.error.ErrorHelper.silent(),
				result -> submitted[0] = result == null ? null : new GeoElement[0]);
		if (submitted[0] != null || target.getKernel().lookupLabel("Zq") != null) {
			violations.add("Algebra input");
		}
		if (target.getKernel().getConstruction().getGeoSetConstructionOrder().size() != count) {
			violations.add("the drawing changed during the probe");
		}
		return violations;
	}

	@Test
	void f3r06TheCloseExitDiscardsTheDrawingWithoutAPrompt() throws Exception {
		AppGeoCeDG target = PreG9BR6PlusF3Documents.target(dir, "r6");
		File current = target.getCurrentFile();
		final byte[] currentBytes = Files.readAllBytes(current.toPath());
		target.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
			@Override
			public void afterCommit() {
				throw new IllegalStateException("F3 injected post-commit failure");
			}

			@Override
			public void beforeCertificate() {
				// the restored drawing differs: the certificate must fail
				must(target, "a=8");
			}
		});
		Scripted dialog = new Scripted();
		dialog.exits.add(GeoCeDGInsertionIsolation.Exit.CLOSE);
		target.getInsertionIsolation().setPresentation(dialog);
		GeoCeDGDocumentInsertion.Result result = insert(target,
				PreG9BR6PlusF3Documents.single(dir, "r6-src", "W=(3,3)"));
		assertEquals(GeoCeDGDocumentInsertion.Outcome.SERIOUS_RECOVERY_FAILURE,
				result.getOutcome());
		assertEquals(1, dialog.choices);
		assertFalse(target.isInsertionIsolated());
		assertTrue(target.getKernel().isUndoActive());
		assertNull(target.getCurrentFile());
		assertTrue(target.getKernel().getConstruction().getGeoSetConstructionOrder().isEmpty());
		assertTrue(java.util.Arrays.equals(currentBytes, Files.readAllBytes(current.toPath())),
				"nothing was written");
	}

	@Test
	void f3r07CleanupFailuresAreSuppressedAndAnUnsafeHistoryStaysAside() throws Exception {
		AppGeoCeDG target = PreG9BR6PlusF3Documents.target(dir, "r7");
		IllegalStateException original = new IllegalStateException("F3 original failure");
		target.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
			@Override
			public void afterCommit() {
				throw original;
			}

			@Override
			public void beforeAttach() {
				throw new IllegalStateException("F3 injected attach failure");
			}
		});
		Scripted dialog = new Scripted();
		dialog.exits.add(GeoCeDGInsertionIsolation.Exit.REOPEN);
		boolean[] keptAside = {false};
		dialog.probe = () -> keptAside[0] =
				target.getInsertionIsolation().getKeptAsideHistory() != null;
		target.getInsertionIsolation().setPresentation(dialog);
		GeoCeDGDocumentInsertion.Result result = insert(target,
				PreG9BR6PlusF3Documents.single(dir, "r7-src", "W=(3,3)"));
		assertEquals(GeoCeDGDocumentInsertion.Outcome.SERIOUS_RECOVERY_FAILURE,
				result.getOutcome());
		assertSame(original, result.getFailure(), "the original failure is never replaced");
		assertTrue(original.getSuppressed().length >= 2);
		assertTrue(keptAside[0], "attach was unsafe: the history was kept aside");
		assertFalse(target.isInsertionIsolated());
		assertNotNull(target.getCurrentFile());
	}
}
