/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.units.UnitState;
import org.geogebra.common.euclidian.MoveMode;
import org.geogebra.common.euclidian.UpdateActionStore;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.error.ErrorHelper;
import org.geogebra.common.main.undo.UndoCommand;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.undo.UndoManagerD;

/** PRE-G9B-R6-plus-F3 shared fixtures: embedded hosts, documents and histories. */
final class PreG9BR6PlusF3Fixtures {

	private PreG9BR6PlusF3Fixtures() {
	}

	static AppGeoCeDG app(UnitState state) {
		return app(state, true);
	}

	static AppGeoCeDG app(UnitState state, boolean locusV2) {
		AppGeoCeDG app = G9U1TestApp.create(locusV2);
		app.setLocale(Locale.ENGLISH);
		app.getKernel().setContinuous(false);
		app.getDocumentUnits().replace(state);
		app.getStatusBar().setNoticeTimer((delay, expiry) -> () -> { });
		return app;
	}

	static AppD classic() {
		AppD app = new AppD(new CommandLineArguments(new String[] {"--silent"}), new JPanel(),
				true);
		app.setErrorDialogsActive(false);
		app.setLocale(Locale.ENGLISH);
		return G9U1TestApp.withoutWindowDispatcher(app);
	}

	/** Silent evaluation: null when the command fails. */
	static GeoElement q(AppD app, String command) {
		try {
			GeoElementND[] result = app.getKernel().getAlgebraProcessor()
					.processAlgebraCommandNoExceptionHandling(command, false,
							ErrorHelper.silent(), false, null);
			return result == null || result.length == 0 ? null : result[0].toGeoElement();
		} catch (Throwable failure) {
			return null;
		}
	}

	static GeoElement must(AppD app, String command) {
		GeoElement geo = q(app, command);
		if (geo == null) {
			throw new IllegalStateException("command failed: " + command);
		}
		return geo;
	}

	static File save(AppGeoCeDG app, Path dir, String name) {
		File file = dir.resolve(name + ".cedg").toFile();
		if (!app.saveGeoGebraFile(file)) {
			throw new IllegalStateException("save " + name);
		}
		return file;
	}

	static GeoCeDGUndoManager undo(AppD app) {
		return (GeoCeDGUndoManager) app.getKernel().getConstruction().getUndoManager();
	}

	/** A fresh one-state history; the document is saved. */
	static void baseline(AppD app) throws IOException {
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
		try (UndoManagerD.PreparedUndoBaseline prepared = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(prepared);
		}
		app.setSaved();
	}

	/** Ordinary store, then a bounded wait until no store is pending. */
	static void store(AppGeoCeDG app) {
		app.storeUndoInfo();
		awaitQuiescent(app);
	}

	static void awaitQuiescent(AppGeoCeDG app) {
		GeoCeDGUndoManager undo = undo(app);
		long deadline = System.nanoTime() + 10_000_000_000L;
		while (!undo.isQuiescent()) {
			if (System.nanoTime() > deadline) {
				throw new IllegalStateException("store did not complete: pending="
						+ undo.getPendingStores() + " degraded=" + undo.isDegraded());
			}
			sleep(5);
		}
	}

	static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
		}
	}

	static <T> T onEdt(Callable<T> action) throws Exception {
		if (SwingUtilities.isEventDispatchThread()) {
			return action.call();
		}
		AtomicReference<T> result = new AtomicReference<>();
		AtomicReference<Exception> failure = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
			try {
				result.set(action.call());
			} catch (Exception thrown) {
				failure.set(thrown);
			}
		});
		if (failure.get() != null) {
			throw failure.get();
		}
		return result.get();
	}

	static void onEdt(Runnable action) throws Exception {
		onEdt(() -> {
			action.run();
			return null;
		});
	}

	static GeoCeDGDocumentInsertion.Result insert(AppGeoCeDG app, File file) throws Exception {
		return onEdt(() -> app.getDocumentInsertion().insert(file));
	}

	/** Production producer of a labeled update action (Move-tool drag path). */
	static void moveAction(AppGeoCeDG app, String label, double x, double y, boolean stitch) {
		GeoPoint point = (GeoPoint) app.getKernel().lookupLabel(label);
		app.getSelectionManager().setSelectedGeos(new ArrayList<>(List.of(point)));
		UpdateActionStore store = new UpdateActionStore(app.getSelectionManager(), undo(app));
		store.storeSelection(MoveMode.POINT);
		point.setCoords(x, y, 1);
		point.updateRepaint();
		store.setStitching(stitch);
		store.storeUndo();
		app.getSelectionManager().clearSelectedGeos();
	}

	/**
	 * Mixed history: baseline, two checkpoints, a labeled move, a stitched pair, one more
	 * checkpoint, then one Undo (a Redo state remains).
	 */
	static void mixedHistory(AppGeoCeDG app) throws Exception {
		must(app, "H1=(1,-3)");
		store(app);
		must(app, "G=(5,-3)");
		store(app);
		moveAction(app, "H1", 1, -4, false);
		moveAction(app, "H1", 1, -5, true);
		moveAction(app, "G", 5, -5, false);
		must(app, "H2=(2,-3)");
		store(app);
		onEdt(() -> undo(app).undo());
		awaitQuiescent(app);
	}

	/** Labels and grouping of each live command, with the cursor. */
	static String historySignature(AppGeoCeDG app) {
		GeoCeDGUndoManager undo = undo(app);
		synchronized (undo) {
			StringBuilder text = new StringBuilder("cursor=" + undo.getCursor());
			for (UndoCommand command : undo.getCommands()) {
				text.append(" [").append(command.getAction()).append(' ')
						.append(command.labels).append(' ')
						.append(command.shouldStitchToNext()).append(']');
			}
			return text.toString();
		}
	}

	/** A source macro MyMid(P, Q) defined by the midpoint (or by a third). */
	static void defineMyMid(AppGeoCeDG app, String prefix, boolean third) throws Exception {
		GeoElement a = must(app, prefix + "In1=(5,5)");
		GeoElement b = must(app, prefix + "In2=(6,7)");
		GeoElement out = must(app, third ? prefix + "Out=(" + prefix + "In1+" + prefix
				+ "In2)/3" : prefix + "Out=Midpoint(" + prefix + "In1," + prefix + "In2)");
		app.getKernel().addMacro(new Macro(app.getKernel(), "MyMid",
				new GeoElement[] {a, b}, new GeoElement[] {out}));
		a.remove();
		b.remove();
	}

	static String pasteNotice(AppGeoCeDG app) {
		JLabel label = app.getStatusBar().getSegment(GeoCeDGStatusBar.PASTE_NOTICE_SEGMENT);
		return label.isVisible() ? label.getText() : null;
	}

	/** Test-only access to the C1 counters of the installed manager. */
	static void setAccounting(AppGeoCeDG app, int pending, boolean degraded) throws Exception {
		GeoCeDGUndoManager undo = undo(app);
		synchronized (undo) {
			Field pendingField = GeoCeDGUndoManager.class.getDeclaredField("pending");
			pendingField.setAccessible(true);
			pendingField.setInt(undo, pending);
			Field degradedField = GeoCeDGUndoManager.class.getDeclaredField("degraded");
			degradedField.setAccessible(true);
			degradedField.setBoolean(undo, degraded);
		}
	}

	static byte[] bytes(File file) throws IOException {
		return Files.readAllBytes(file.toPath());
	}
}
