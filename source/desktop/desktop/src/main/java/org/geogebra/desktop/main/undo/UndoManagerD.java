/*
 * GeoGebra - Dynamic Mathematics for Everyone
 * Copyright (c) GeoGebra GmbH, Altenbergerstr. 69, 4040 Linz, Austria
 * https://www.geogebra.org
 * 
 * This file is licensed by GeoGebra GmbH under the EUPL 1.2 licence and
 * may be used under the EUPL 1.2 in compatible projects (see Article 5
 * and the Appendix of EUPL 1.2 for details).
 * You may obtain a copy of the licence at:
 * https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 * 
 * Note: The overall GeoGebra software package is free to use for
 * non-commercial purposes only.
 * See https://www.geogebra.org/license for full licensing details
 */

package org.geogebra.desktop.main.undo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.DefaultListSelectionModel;

import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.main.App;
import org.geogebra.common.main.undo.AppState;
import org.geogebra.common.main.undo.UndoCommand;
import org.geogebra.common.main.undo.UndoManager;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.cas.view.CASViewD;
import org.geogebra.desktop.io.MyXMLioD;

/**
 * UndoManager handles undo information for a Construction. It uses an undo info
 * list with construction snapshots in temporary files.
 * 
 * @author Markus Hohenwarter
 */
public class UndoManagerD extends UndoManager {
	private long undoHistoryGeneration;

	/**
	 * Creates a new UndowManager for the given Construction.
	 * 
	 * @param cons
	 *            construction
	 */
	public UndoManagerD(Construction cons) {
		super(cons);
	}

	/**
	 * Adds construction state to undo info list.
	 */
	@Override
	public void storeUndoInfo(final StringBuilder currentUndoXML) {

		// force create event dispatcher before we go to thread
		app.getEventDispatcher();

		long scheduledGeneration;
		synchronized (this) {
			scheduledGeneration = undoHistoryGeneration;
		}
		Runnable storeUndoAction = () -> doStoreUndoInfo(currentUndoXML,
				scheduledGeneration);
		new Thread(storeUndoAction).start();
	}

	/**
	 * Adds construction state to undo info list.
	 * 
	 * @param undoXML
	 *            string builder with construction XML
	 */
	synchronized void doStoreUndoInfo(final StringBuilder undoXML,
			long scheduledGeneration) {
		try {
			// save to file
			AppState appStateToAdd = new FileAppState(undoXML);
			if (scheduledGeneration != undoHistoryGeneration) {
				appStateToAdd.delete();
				return;
			}

			// insert undo info
			UndoCommand command = new UndoCommand(appStateToAdd);
			maybeStoreUndoCommand(command);
			pruneStateList();
			if (undoInfoList.size() > 1) {
				notifyUnsaved();
			}
		} catch (Exception | OutOfMemoryError e) {
			Log.debug("storeUndoInfo: " + e);
			Log.debug(e);
		}

		onStoreUndo();
	}

	/**
	 * Serializes the current construction into a disposable undo baseline without
	 * changing the live undo or redo history.
	 *
	 * @return prepared baseline
	 * @throws IOException if the baseline cannot be serialized
	 */
	public PreparedUndoBaseline prepareUndoBaseline() throws IOException {
		// Keep dispatcher creation and XML/file serialization on the caller thread so
		// every recoverable failure happens before the old history is changed.
		app.getEventDispatcher();
		return new PreparedUndoBaseline(this, new UndoCommand(
				new FileAppState(construction.getCurrentUndoXML(true))));
	}

	/**
	 * Atomically replaces the undo and redo history with a prepared baseline.
	 * Any asynchronous store scheduled for an older history generation is
	 * discarded when it eventually reaches this manager.
	 *
	 * @param baseline prepared baseline owned by this manager
	 */
	public synchronized void commitUndoBaseline(PreparedUndoBaseline baseline) {
		UndoCommand command = baseline.commandFor(this);
		storeUndoInfoForProperties(false);
		clearUndoInfo();
		maybeStoreUndoCommand(command);
		undoHistoryGeneration++;
		baseline.markCommitted();
		try {
			onStoreUndo();
		} catch (RuntimeException updateFailure) {
			// The history swap is the commit. UI action refresh is best effort and must
			// not turn a successfully published document into a failed open.
			Log.debug(updateFailure);
		}
	}

	/**
	 * Disposable, manager-owned undo baseline prepared before an atomic history
	 * replacement.
	 */
	public static final class PreparedUndoBaseline implements AutoCloseable {
		private final UndoManagerD owner;
		private UndoCommand command;

		private PreparedUndoBaseline(UndoManagerD owner, UndoCommand command) {
			this.owner = owner;
			this.command = command;
		}

		private UndoCommand commandFor(UndoManagerD expectedOwner) {
			if (owner != expectedOwner || command == null) {
				throw new IllegalStateException("Undo baseline is unavailable");
			}
			return command;
		}

		private void markCommitted() {
			command = null;
		}

		@Override
		public void close() {
			UndoCommand disposableCommand = command;
			command = null;
			if (disposableCommand != null) {
				try {
					disposableCommand.delete();
				} catch (RuntimeException cleanupFailure) {
					// Cleanup must never mask the load failure that owns this rollback.
					Log.debug(cleanupFailure);
				}
			}
		}
	}

	/**
	 * restore info at position pos of undo list
	 */
	@Override
	final protected synchronized void loadUndoInfo(final AppState info,
			String slideID) {
		if (!(info instanceof FileAppState)) {
			Log.warn("Invalid undo state");
			restoreCurrentUndoInfo();
			return;
		}
		File tempFile = ((FileAppState) info).getFile();

		try (FileInputStream is = new FileInputStream(tempFile)) {
			// load from file

			// make sure objects are displayed in the correct View
			app.setActiveView(App.VIEW_EUCLIDIAN);

			// needed for GGB-517
			// keep information form listSelectionModel
			CASViewD casView = null;
			DefaultListSelectionModel listSelModel = null;
			if (app.getGuiManager() != null && app.getGuiManager().hasCasView()
					&& app.getGuiManager().getCasView() instanceof CASViewD) {
				casView = (CASViewD) app.getGuiManager().getCasView();
			}
			if (casView != null && casView.getListSelModel() != null && casView
					.getListSelModel() instanceof DefaultListSelectionModel) {
				listSelModel = (DefaultListSelectionModel) casView
						.getListSelModel();
			}

			int anchorIndex = 0;
			int leadIndex = 0;
			int maxIndex = 0;
			int minIndex = 0;
			boolean changed = false;

			if (listSelModel != null) {
				anchorIndex = listSelModel.getAnchorSelectionIndex();
				leadIndex = listSelModel.getLeadSelectionIndex();
				maxIndex = listSelModel.getMaxSelectionIndex();
				minIndex = listSelModel.getMinSelectionIndex();
				changed = true;
			}

			// load undo info
			app.getEventDispatcher().disableListeners();
			((MyXMLioD) construction.getXMLio()).readZipFromMemory(is);
			if (changed) {
				listSelModel.setAnchorSelectionIndex(anchorIndex);
				listSelModel.setLeadSelectionIndex(leadIndex);
				listSelModel.setSelectionInterval(minIndex, maxIndex);
			}
			app.getEventDispatcher().enableListeners();
		} catch (IOException | XMLParseException | RuntimeException e) {
			Log.error("Problem setting undo info");
			Log.debug(e);
			restoreCurrentUndoInfo();
		} catch (java.lang.OutOfMemoryError err) {
			Log.error("UndoManager.loadUndoInfo: " + err);
		}

	}

}
