/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import org.geogebra.common.main.undo.UndoCommand;
import org.geogebra.common.main.undo.UndoPossibleListener;

/** Session-only save protection, indexed by the actual expert Undo checkpoints. */
final class GeoCeDGExpertSaveState implements AutoCloseable, UndoPossibleListener {
	private final GeoCeDGUndoManager undo;
	private final Runnable markUnsaved;
	private final List<Step> steps = new ArrayList<>();

	GeoCeDGExpertSaveState(GeoCeDGUndoManager undo, Runnable markUnsaved) {
		this.undo = undo;
		this.markUnsaved = markUnsaved;
		undo.addUndoListener(this);
	}

	void add(UndoCommand checkpoint, File... files) {
		var protectedPaths = new LinkedHashSet<Path>();
		for (File file : files) {
			if (file != null) {
				Path path = file.toPath().toAbsolutePath().normalize();
				protectedPaths.add(path);
				try {
					protectedPaths.add(path.toRealPath());
				} catch (IOException unavailable) {
					// Keep the selected spelling even when a previously associated file was deleted.
				}
			}
		}
		synchronized (undo) {
			steps.add(new Step(Objects.requireNonNull(checkpoint), List.copyOf(protectedPaths)));
		}
	}

	boolean isPending() {
		synchronized (undo) {
			reconcile();
			return steps.stream().anyMatch(step -> step.active);
		}
	}

	boolean isDistinctTarget(File target) {
		List<Step> active;
		synchronized (undo) {
			reconcile();
			active = steps.stream().filter(step -> step.active).toList();
		}
		try {
			Path candidate = target.toPath().toAbsolutePath().normalize();
			if (Files.isSymbolicLink(candidate)) {
				return false;
			}
			for (Step step : active) {
				for (Path protectedPath : step.protectedPaths) {
					if (samePath(candidate, protectedPath)) {
						return false;
					}
				}
			}
			return true;
		} catch (IOException failure) {
			return false;
		}
	}

	private static boolean samePath(Path candidate, Path protectedPath) throws IOException {
		if (candidate.equals(protectedPath)) {
			return true;
		}
		if (Files.exists(candidate) && Files.exists(protectedPath)) {
			return Files.isSameFile(candidate, protectedPath);
		}
		// A deleted original can still be addressed through another spelling of its parent.
		return candidate.getFileName().equals(protectedPath.getFileName())
				&& Files.exists(candidate.getParent()) && Files.exists(protectedPath.getParent())
				&& Files.isSameFile(candidate.getParent(), protectedPath.getParent());
	}

	void savedCopy() {
		synchronized (undo) {
			reconcile();
			steps.removeIf(step -> step.active);
		}
	}

	@Override
	public void undoPossible(boolean possible) {
		synchronized (undo) {
			if (reconcile()) {
				markUnsaved.run();
			}
		}
	}

	@Override
	public void redoPossible(boolean possible) {
		// The same history notification calls undoPossible first.
	}

	private boolean reconcile() {
		List<UndoCommand> commands = undo.getCommands();
		int cursor = undo.getCursor();
		boolean reactivated = false;
		for (Step step : steps) {
			int index = commands.indexOf(step.checkpoint);
			if (index >= 0) {
				boolean active = index < cursor;
				reactivated |= active && !step.active;
				step.active = active;
			}
			// A pruned applied checkpoint still contributes to the live drawing.
			// A discarded redo checkpoint was already marked inactive by Undo.
		}
		return reactivated;
	}

	@Override
	public void close() {
		synchronized (undo) {
			undo.removeUndoListener(this);
			steps.clear();
		}
	}

	private static final class Step {
		private final UndoCommand checkpoint;
		private final List<Path> protectedPaths;
		private boolean active = true;

		private Step(UndoCommand checkpoint, List<Path> protectedPaths) {
			this.checkpoint = checkpoint;
			this.protectedPaths = protectedPaths;
		}
	}
}
