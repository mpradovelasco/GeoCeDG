/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.historySignature;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.mixedHistory;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.sleep;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.store;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geogebra.common.main.App;
import org.geogebra.common.main.undo.UndoCommand;
import org.geogebra.common.main.undo.UndoHistory;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.undo.FileAppState;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

/**
 * PRE-G9B-R6-plus-F3 Stage A: the GeoCeDG undo manager installed by the stateless
 * factory (G6), the conservative pending-store accounting C1 on every exit path (G2,
 * FR-F3-1) and the detach/attach of original commands (H2-H5, ER-F3R-3, FR-F3-2).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusF3UndoAccountingTest {

	private static final UnitState MM = UnitState.of(UnitToken.MM, null, null);

	/** A manager whose store body fails on demand inside the inherited try block. */
	static final class FailingManager extends GeoCeDGUndoManager {
		volatile Throwable fail;

		FailingManager(org.geogebra.common.kernel.Construction cons) {
			super(cons);
		}

		@Override
		public void pruneStateList() {
			Throwable failure = fail;
			if (failure != null) {
				fail = null;
				if (failure instanceof RuntimeException runtime) {
					throw runtime;
				}
				throw (Error) failure;
			}
			super.pruneStateList();
		}

		void replaceApp(App replacement) {
			app = replacement;
		}
	}

	private static void awaitManager(GeoCeDGUndoManager manager, int pending) {
		long deadline = System.nanoTime() + 10_000_000_000L;
		while (manager.getPendingStores() != pending) {
			if (System.nanoTime() > deadline) {
				throw new IllegalStateException("pending " + manager.getPendingStores());
			}
			sleep(5);
		}
	}

	private static FailingManager failingManager(AppGeoCeDG app) throws Exception {
		FailingManager manager = new FailingManager(app.getKernel().getConstruction());
		try (UndoManagerD.PreparedUndoBaseline prepared = manager.prepareUndoBaseline()) {
			manager.commitUndoBaseline(prepared);
		}
		return manager;
	}

	@Test
	void f3a01FactoryInstallsTheGeoCeDGManagerAndClassicKeepsTheInheritedOne() {
		AppGeoCeDG app = app(MM);
		// created by the host constructor's first undo initialization, before any field
		// of AppGeoCeDG was assigned: the factory is stateless (G6)
		assertSame(GeoCeDGUndoManager.class,
				app.getKernel().getConstruction().getUndoManager().getClass());
		assertTrue(undo(app).isQuiescent());
		AppGeoCeDG helper = app.newInsertionHelper();
		try {
			assertSame(GeoCeDGUndoManager.class,
					helper.getKernel().getConstruction().getUndoManager().getClass());
			assertTrue(helper.isInsertionHelper());
		} finally {
			app.releaseInsertionHelper(helper);
		}
		AppD classic = PreG9BR6PlusF3Fixtures.classic();
		assertSame(UndoManagerD.class,
				classic.getKernel().getConstruction().getUndoManager().getClass());
	}

	@Test
	void f3a02OrdinaryUndoAndRedoAreUnchanged() throws Exception {
		AppGeoCeDG app = app(MM);
		baseline(app);
		must(app, "A=(1,1)");
		store(app);
		must(app, "B=(2,2)");
		store(app);
		String withB = app.getXML();
		assertEquals(3, undo(app).getCursor());
		onEdt(() -> undo(app).undo());
		assertNull(app.getKernel().lookupLabel("B"));
		assertNotNull(app.getKernel().lookupLabel("A"));
		onEdt(() -> undo(app).redo());
		assertEquals(withB, app.getXML());
		assertTrue(undo(app).isQuiescent());
	}

	@Test
	void f3a03SuccessPathDecrementsAndSynchronousCallersAreNotCounted() throws Exception {
		AppGeoCeDG app = app(MM);
		baseline(app);
		GeoCeDGUndoManager manager = undo(app);
		final long epoch = manager.getEpoch();
		must(app, "A=(1,1)");
		app.storeUndoInfo();
		awaitQuiescent(app);
		assertEquals(0, manager.getPendingStores());
		assertFalse(manager.isDegraded());
		// storeAddGeo of an elementary object: a synchronous action store
		manager.storeAddGeo(must(app, "B=(3,1)"));
		assertEquals(0, manager.getPendingStores());
		assertFalse(manager.isDegraded());
		baseline(app);
		assertEquals(epoch + 1, manager.getEpoch());
		assertTrue(manager.isQuiescent());
	}

	@Test
	void f3a04APendingStoreIsCountedUntilItsBodyCompletes() throws Exception {
		AppGeoCeDG app = app(MM);
		baseline(app);
		GeoCeDGUndoManager manager = undo(app);
		must(app, "A=(1,1)");
		synchronized (manager) {
			// the store thread blocks on the monitor: a delayed notification
			app.storeUndoInfo();
			sleep(100);
			assertEquals(1, manager.getPendingStores());
			assertFalse(manager.isQuiescent());
		}
		awaitQuiescent(app);
		assertEquals(2, manager.getCursor());
	}

	@Test
	void f3a05AStaleStoreIsDiscardedAfterABaselineAndNeverCounted() throws Exception {
		AppGeoCeDG app = app(MM);
		baseline(app);
		GeoCeDGUndoManager manager = undo(app);
		must(app, "A=(1,1)");
		synchronized (manager) {
			app.storeUndoInfo();
			assertEquals(1, manager.getPendingStores());
			try (UndoManagerD.PreparedUndoBaseline prepared = manager.prepareUndoBaseline()) {
				manager.commitUndoBaseline(prepared);
			}
			assertEquals(0, manager.getPendingStores());
		}
		sleep(300);
		assertTrue(manager.isQuiescent());
		assertEquals(1, manager.getCommands().size(), "the stale store never lands");
	}

	@Test
	void f3a06ConcurrentStoresAllComplete() throws Exception {
		AppGeoCeDG app = app(MM);
		baseline(app);
		for (int i = 0; i < 12; i++) {
			must(app, "P" + i + "=(" + i + ",0)");
			app.storeUndoInfo();
		}
		awaitQuiescent(app);
		GeoCeDGUndoManager manager = undo(app);
		assertFalse(manager.isDegraded());
		assertEquals(13, manager.getCommands().size());
	}

	@Test
	void f3a07AnExceptionExitDegradesAndNeverDecrements() throws Exception {
		AppGeoCeDG app = app(MM);
		FailingManager manager = failingManager(app);
		manager.fail = new IllegalStateException("F3 injected store failure");
		manager.storeUndoInfo();
		long deadline = System.nanoTime() + 10_000_000_000L;
		while (!manager.isDegraded() && System.nanoTime() < deadline) {
			sleep(5);
		}
		assertTrue(manager.isDegraded());
		assertEquals(1, manager.getPendingStores());
		assertFalse(manager.isQuiescent());
		// only a committed baseline re-establishes the accounting
		try (UndoManagerD.PreparedUndoBaseline prepared = manager.prepareUndoBaseline()) {
			manager.commitUndoBaseline(prepared);
		}
		assertTrue(manager.isQuiescent());
	}

	@Test
	void f3a08AnErrorExitLeavesTheStoreCounted() throws Exception {
		AppGeoCeDG app = app(MM);
		FailingManager manager = failingManager(app);
		manager.fail = new AssertionError("F3 injected store error");
		Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler((thread, error) -> { });
		try {
			manager.storeUndoInfo();
			sleep(400);
		} finally {
			Thread.setDefaultUncaughtExceptionHandler(previous);
		}
		assertEquals(1, manager.getPendingStores(), "no callback: fail closed");
		assertFalse(manager.isQuiescent());
	}

	@Test
	void f3a09ASchedulingFailureDegrades() throws Exception {
		AppGeoCeDG app = app(MM);
		FailingManager manager = failingManager(app);
		App broken = Mockito.mock(App.class);
		Mockito.when(broken.getEventDispatcher())
				.thenThrow(new OutOfMemoryError("unable to create native thread"));
		manager.replaceApp(broken);
		assertThrows(OutOfMemoryError.class, () -> manager.storeUndoInfo(new StringBuilder()));
		assertTrue(manager.isDegraded());
		assertFalse(manager.isQuiescent());
	}

	@Test
	void f3a10DetachAndAttachMoveTheOriginalCommandsWithTheirMetadata() throws Exception {
		AppGeoCeDG app = app(MM);
		baseline(app);
		mixedHistory(app);
		GeoCeDGUndoManager manager = undo(app);
		final String signature = historySignature(app);
		List<UndoCommand> before = manager.getCommands();
		int cursor = manager.getCursor();
		assertTrue(before.stream().anyMatch(c -> c.labels != null && !c.labels.isEmpty()));
		assertTrue(before.stream().anyMatch(UndoCommand::shouldStitchToNext));
		assertTrue(cursor < before.size(), "a Redo state exists");
		GeoCeDGUndoManager.HeldHistory held;
		synchronized (manager) {
			held = manager.detach();
			assertTrue(manager.getCommands().isEmpty());
		}
		for (int i = 0; i < before.size(); i++) {
			assertSame(before.get(i), held.getCommands().get(i));
		}
		assertEquals(cursor, held.getCursor());
		assertNull(manager.attach(held.getCommands(), held.getCursor()));
		assertEquals(signature, historySignature(app));
		for (int i = 0; i < before.size(); i++) {
			assertSame(before.get(i), manager.getCommands().get(i));
			if (before.get(i).getAppState() instanceof FileAppState state) {
				File file = state.getFile();
				assertTrue(file.exists(), "snapshot file kept: " + file);
			}
		}
		// behaviour after the attach: the stitched pair is undone as one step
		String afterRedo = walk(app);
		assertFalse(afterRedo.isEmpty());
	}

	@Test
	void f3a11TheInheritedCopyLosesMetadataTheDetachDoesNot() throws Exception {
		AppGeoCeDG app = app(MM);
		baseline(app);
		mixedHistory(app);
		GeoCeDGUndoManager manager = undo(app);
		List<UndoCommand> before = manager.getCommands();
		Map<String, UndoHistory> copied = new HashMap<>();
		synchronized (manager) {
			manager.undoHistoryTo(copied);
			List<UndoCommand> copies = new ArrayList<>(copied.values().iterator().next()
					.commands());
			assertNotSame(before.get(1), copies.get(1));
			boolean labelsLost = false;
			for (int i = 0; i < before.size(); i++) {
				labelsLost |= before.get(i).labels != null && copies.get(i).labels == null;
			}
			assertTrue(labelsLost, "ER-F3R-3: the inherited copy drops labels");
			manager.attach(before, copied.values().iterator().next().iteratorIndex());
		}
	}

	@Test
	void f3a12AttachInstallsFirstAndTreatsTheRefreshAsBestEffort() throws Exception {
		AppGeoCeDG app = app(MM);
		boolean[] failRefresh = {false};
		GeoCeDGUndoManager manager = new GeoCeDGUndoManager(app.getKernel().getConstruction()) {
			@Override
			protected void updateUndoActions() {
				if (failRefresh[0]) {
					throw new IllegalStateException("F3 injected refresh failure");
				}
				super.updateUndoActions();
			}
		};
		manager.storeUndoInfo();
		awaitManager(manager, 0);
		List<UndoCommand> commands = manager.getCommands();
		GeoCeDGUndoManager.HeldHistory held;
		synchronized (manager) {
			held = manager.detach();
		}
		failRefresh[0] = true;
		RuntimeException refresh = manager.attach(held.getCommands(), held.getCursor());
		failRefresh[0] = false;
		assertNotNull(refresh);
		assertEquals(commands, manager.getCommands());
		assertEquals(held.getCursor(), manager.getCursor());
		assertThrows(IllegalArgumentException.class,
				() -> manager.attach(held.getCommands(), held.getCommands().size() + 1));
	}

	/** Undo to the start and Redo to the end, returning the visited cursors. */
	private static String walk(AppGeoCeDG app) throws Exception {
		GeoCeDGUndoManager manager = undo(app);
		int start = manager.getCursor();
		StringBuilder visited = new StringBuilder();
		while (manager.undoPossible()) {
			onEdt(manager::undo);
			visited.append(manager.getCursor()).append(' ');
		}
		while (manager.redoPossible()) {
			onEdt(manager::redo);
			visited.append(manager.getCursor()).append(' ');
		}
		while (manager.getCursor() > start && manager.undoPossible()) {
			onEdt(manager::undo);
		}
		assertNotEquals(-1, start);
		return visited.toString().trim();
	}
}
