/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.geogebra.common.util.FileExtensions;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.gui.GuiManagerD;

/**
 * PRE-G9B-R6-plus-F3 serious-failure isolation (P7, recovery design addendum
 * successor section 5, audit G5/NE2). Entered only when a failed insertion could not
 * be restored with a certificate. While active, the drawing cannot be edited, saved,
 * exported, inserted into, restyled, copied, pasted, changed by scripts, global
 * listeners, animation, Undo or Redo; product routes consult {@link #isActive()}, and
 * the modal dialog blocks the window. The only exits are: save the verified native
 * recovery snapshot to a new file (repeatable), reopen the document from its file, or
 * close it. No exit shows the inherited save-changes prompt and the uncertified state is
 * never written or exported. The snapshot is released, and the flag cleared, only after
 * Reopen or Close completed.
 */
final class GeoCeDGInsertionIsolation {

	/** User exits of the isolation dialog. */
	enum Exit {
		SAVE_COPY,
		REOPEN,
		CLOSE
	}

	/** Presentation of the isolation dialog and its file questions (injectable). */
	interface Presentation {
		/**
		 * @param app isolated document
		 * @param message P6 serious-failure message
		 * @param reopenAvailable whether the document has an existing file
		 * @return chosen exit
		 */
		Exit choose(AppGeoCeDG app, String message, boolean reopenAvailable);

		/**
		 * @param app isolated document
		 * @param suggestion suggested new file
		 * @return chosen target, or null after cancellation
		 */
		File chooseCopyTarget(AppGeoCeDG app, File suggestion);

		/**
		 * @param app isolated document
		 * @param target existing file that is not the current document
		 * @return whether the user explicitly agreed to replace it
		 */
		boolean confirmReplace(AppGeoCeDG app, File target);

		/**
		 * @param app isolated document
		 * @param text information shown to the user
		 */
		void inform(AppGeoCeDG app, String text);
	}

	/** Closes the document window without any save prompt (injectable). */
	interface Closer {
		void close(AppGeoCeDG app);
	}

	private final AppGeoCeDG app;
	private Presentation presentation;
	private Closer closer;
	private boolean active;
	private boolean scriptsWereBlocked;
	private GeoCeDGRecoverySnapshot snapshot;
	private GeoCeDGUndoManager.HeldHistory keptAside;

	GeoCeDGInsertionIsolation(AppGeoCeDG app) {
		this.app = app;
		this.presentation = swing();
		this.closer = AppGeoCeDG::closeWithoutSavePrompt;
	}

	void setPresentation(Presentation presentation) {
		this.presentation = presentation;
	}

	void setCloser(Closer closer) {
		this.closer = closer;
	}

	/** @return whether the drawing is isolated */
	boolean isActive() {
		return active;
	}

	/** @return the verified snapshot owned by the isolation, or null */
	GeoCeDGRecoverySnapshot getSnapshot() {
		return snapshot;
	}

	/** @return a history kept aside because re-attaching it was unsafe, or null */
	GeoCeDGUndoManager.HeldHistory getKeptAsideHistory() {
		return keptAside;
	}

	/**
	 * Isolates the drawing and runs the exit dialog until Reopen or Close completed.
	 *
	 * @param verified verified native recovery snapshot (ownership moves here)
	 * @param documentName inserted file name for the message
	 * @param heldHistory history kept aside, or null when it was re-attached
	 */
	void enter(GeoCeDGRecoverySnapshot verified, String documentName,
			GeoCeDGUndoManager.HeldHistory heldHistory) {
		snapshot = verified;
		keptAside = heldHistory;
		applyGuards();
		String message = app.layerText("Insert.Serious", documentName);
		while (active) {
			File file = app.getCurrentFile();
			boolean reopenAvailable = file != null && file.isFile();
			Exit exit = presentation.choose(app, message, reopenAvailable);
			if (exit == null) {
				continue;
			}
			switch (exit) {
			case SAVE_COPY:
				saveCopy();
				break;
			case REOPEN:
				if (reopenAvailable) {
					reopen(file);
				}
				break;
			case CLOSE:
				close();
				break;
			default:
				break;
			}
		}
	}

	private void applyGuards() {
		active = true;
		// the uncertified state is never "saved": every inherited prompt route then
		// reaches the refusing saveCurrentFile of the product
		app.setUnsaved();
		app.getKernel().getAnimationManager().stopAnimation();
		app.getKernel().setUndoActive(false);
		scriptsWereBlocked = app.isBlockUpdateScripts();
		app.setBlockUpdateScripts(true);
		app.getEventDispatcher().disableListeners();
		app.updateActions();
	}

	private void liftScriptGuards() {
		app.getEventDispatcher().enableListeners();
		app.setBlockUpdateScripts(scriptsWereBlocked);
	}

	private void saveCopy() {
		File current = app.getCurrentFile();
		File suggestion = GeoCeDGDocumentPolicy.recoveryCopySuggestion(current);
		File target = presentation.chooseCopyTarget(app, suggestion);
		if (target == null) {
			return;
		}
		target = GeoCeDGDocumentPolicy.normalizeNativeSuffix(target);
		if (!GeoCeDGDocumentPolicy.isNative(target)) {
			target = new File(target.getParentFile(), target.getName() + "."
					+ FileExtensions.GEOCEDG);
		}
		if (current != null && sameFile(current, target)) {
			presentation.inform(app, app.layerText("Insert.Isolation.NotCurrentFile"));
			return;
		}
		if (target.exists() && !presentation.confirmReplace(app, target)) {
			return;
		}
		try {
			snapshot.writeCopy(target.toPath(), app);
			presentation.inform(app, app.layerText("Insert.Isolation.CopySaved",
					target.getName()));
		} catch (IOException | GeoCeDGRecoverySnapshot.SnapshotException failure) {
			Log.debug(failure);
			presentation.inform(app, app.layerText("Insert.Isolation.CopyFailed",
					target.getName()));
		}
	}

	private static boolean sameFile(File a, File b) {
		try {
			return a.getCanonicalFile().equals(b.getCanonicalFile());
		} catch (IOException unresolved) {
			return a.getAbsoluteFile().equals(b.getAbsoluteFile());
		}
	}

	/** Undo first, then the ordinary native open of the existing file, never a prompt. */
	private void reopen(File file) {
		app.getKernel().setUndoActive(true);
		liftScriptGuards();
		boolean loaded;
		try {
			loaded = app.getGuiManager() instanceof GuiManagerD gui ? gui.loadFile(file, false)
					: app.loadFile(file, false);
		} catch (RuntimeException failure) {
			Log.debug(failure);
			loaded = false;
		}
		if (!loaded) {
			// the native transaction restored the uncertified state: isolate again
			applyGuards();
			presentation.inform(app, app.layerText("Insert.Isolation.ReopenFailed",
					file.getName()));
			return;
		}
		end();
	}

	private void close() {
		app.getKernel().setUndoActive(true);
		liftScriptGuards();
		try {
			closer.close(app);
		} catch (RuntimeException failure) {
			Log.debug(failure);
			applyGuards();
			presentation.inform(app, app.layerText("Insert.Isolation.CloseFailed"));
			return;
		}
		end();
	}

	private void end() {
		active = false;
		if (keptAside != null) {
			for (org.geogebra.common.main.undo.UndoCommand command : keptAside.getCommands()) {
				try {
					command.delete();
				} catch (RuntimeException cleanupFailure) {
					Log.debug(cleanupFailure);
				}
			}
			keptAside = null;
		}
		if (snapshot != null) {
			snapshot.release();
			snapshot = null;
		}
		app.updateActions();
	}

	/** @return the Swing presentation used in the product */
	static Presentation swing() {
		return new Presentation() {
			@Override
			public Exit choose(AppGeoCeDG app, String message, boolean reopenAvailable) {
				String save = app.layerText("Insert.Isolation.SaveCopy");
				String reopen = app.layerText("Insert.Isolation.Reopen");
				String close = app.layerText("Insert.Isolation.Close");
				Object[] options = reopenAvailable ? new Object[] {save, reopen, close}
						: new Object[] {save, close};
				int choice = JOptionPane.showOptionDialog(app.getMainComponent(), message,
						app.layerText("Insert.Isolation.Title"), JOptionPane.DEFAULT_OPTION,
						JOptionPane.ERROR_MESSAGE, null, options, options[0]);
				if (choice < 0) {
					return null;
				}
				Object chosen = options[choice];
				return chosen == save ? Exit.SAVE_COPY : chosen == reopen ? Exit.REOPEN
						: Exit.CLOSE;
			}

			@Override
			public File chooseCopyTarget(AppGeoCeDG app, File suggestion) {
				return ((GuiManagerD) app.getGuiManager()).showSaveDialog(FileExtensions.GEOCEDG,
						suggestion, app.layerText("Insert.Isolation.CopyFilter"), false, false);
			}

			@Override
			public boolean confirmReplace(AppGeoCeDG app, File target) {
				return JOptionPane.showConfirmDialog(app.getMainComponent(),
						app.layerText("Insert.Isolation.ConfirmReplace", target.getName()),
						app.layerText("Insert.Isolation.Title"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
			}

			@Override
			public void inform(AppGeoCeDG app, String text) {
				JOptionPane.showMessageDialog(app.getMainComponent(), text,
						app.layerText("Insert.Isolation.Title"),
						JOptionPane.INFORMATION_MESSAGE);
			}
		};
	}
}
