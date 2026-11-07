/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.ChartStyleGeo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.plugin.Event;
import org.geogebra.common.plugin.EventDispatcher;
import org.geogebra.common.plugin.EventListener;

/** Native inspection input and a local, read-only receiver witness. EDT-owned. */
final class GeoCeDGLiveInspectionSnapshot implements AutoCloseable {
	private final String archiveSha256;
	private final Map<String, String> members;
	private final GeoCeDGExpertContext expertContext;
	private Session session;
	private byte[] archive;

	private GeoCeDGLiveInspectionSnapshot(byte[] archive,
			GeoCeDGInspectionArchive.Inspection inspection, Session session,
			GeoCeDGExpertContext expertContext) {
		this.archive = archive;
		this.archiveSha256 = inspection.sha256();
		this.members = inspection.entries().stream().collect(Collectors.toUnmodifiableMap(
				GeoCeDGInspectionArchive.Entry::name, GeoCeDGInspectionArchive.Entry::sha256));
		this.session = session;
		this.expertContext = expertContext;
	}

	static GeoCeDGLiveInspectionSnapshot capture(AppGeoCeDG app) throws IOException {
		return capture(app, out -> app.getXMLio().writeGeoGebraFile(out, false));
	}

	@FunctionalInterface
	interface ArchiveWriter {
		void write(OutputStream output) throws IOException;
	}

	static GeoCeDGLiveInspectionSnapshot capture(AppGeoCeDG app, ArchiveWriter writer)
			throws IOException {
		requireEdt();
		Construction construction = app.getKernel().getConstruction();
		if (!(construction.getUndoManager() instanceof GeoCeDGUndoManager undo)) {
			throw new IOException("Receiver has no GeoCeDG history monitor");
		}
		synchronized (undo) {
			requireQuiescent(app, construction, undo);
			requirePassiveWrite(construction);
			for (int i = 0; i < app.getKernel().getMacroNumber(); i++) {
				var macro = app.getKernel().getMacro(i);
				if (hasName(macro.getIconFileName())) {
					throw new IOException("Live capture does not cover macro icon resources");
				}
				requirePassiveWrite(macro.getMacroConstruction());
			}
			Session before = Session.read(app, construction, undo);
			var expertContext = GeoCeDGExpertContext.capture(app);
			var dispatcher = new EventDispatcher(app);
			ChangeWatch watch = new ChangeWatch();
			dispatcher.addEventListener(watch);
			try {
				var output = new CaptureBuffer();
				writer.write(output);
				byte[] bytes = output.toByteArray();
				var inspection = GeoCeDGInspectionArchive.inspect(bytes);
				if (inspection.documentId().isEmpty()
						|| !inspection.documentId().get().equals(before.documentId())
						|| inspection.findings().stream().anyMatch(finding -> switch (finding.code()) {
							case INVALID_ARCHIVE, INVALID_XML, LIMIT_EXCEEDED -> true;
							default -> false;
						})) {
					throw new IOException("Receiver archive failed passive validation or identity check");
				}
				if (watch.changed || !before.matches(app)
						|| !expertContext.equals(GeoCeDGExpertContext.capture(app))) {
					throw new IOException("Receiver changed during capture");
				}
				requireQuiescent(app, construction, undo);
				return new GeoCeDGLiveInspectionSnapshot(bytes, inspection, before, expertContext);
			} catch (RuntimeException failure) {
				throw new IOException("Live native capture failed", failure);
			} finally {
				dispatcher.removeEventListener(watch);
				app.getKernel().detach(dispatcher);
			}
		}
	}

	private static void requireQuiescent(AppGeoCeDG app, Construction construction,
			GeoCeDGUndoManager undo) throws IOException {
		if (app.isIniting() || app.isInsertionIsolated() || construction.isFileLoading()
				|| !app.getKernel().isNotifyViewsActive()
				|| app.getKernel().isSaving() || app.getKernel().isAnimationRunning()
				|| app.getKernel().isAnimationPaused() || app.getKernel().wantAnimationStarted()
				|| !undo.isQuiescent()) {
			throw new IOException("Receiver is busy");
		}
		if (app.hasScriptManager() && app.getScriptManager().hasListeners()) {
			throw new IOException("Live capture does not cover registered script listeners");
		}
	}

	private static void requirePassiveWrite(Construction construction) throws IOException {
		for (GeoElement geo : construction.getGeoSetLabelOrder()) {
			if (hasName(geo.getImageFileName())) {
				throw new IOException("Live capture does not cover image resources");
			}
			if (geo instanceof ChartStyleGeo chart) {
				for (int bar = 1; bar <= chart.getIntervals(); bar++) {
					if (chart.getStyle().getBarImage(bar) != null) {
						throw new IOException("Live capture does not cover chart image resources");
					}
				}
			}
		}
	}

	private static boolean hasName(String name) {
		return name != null && !name.isEmpty();
	}

	/** Compare effective state. Offer invalidation across reverted edits belongs to I4. */
	boolean matchesCurrentState(AppGeoCeDG app) {
		requireEdt();
		if (session == null) {
			return false;
		}
		synchronized (session.undo()) {
			if (!session.matches(app)) {
				return false;
			}
			try (var current = capture(app)) {
				return members.equals(current.members) && expertContext.equals(current.expertContext);
			} catch (IOException failure) {
				return false;
			}
		}
	}

	String archiveSha256() {
		return archiveSha256;
	}

	/** Stage bytes at a new .cedg path owned and cleaned up by the worker caller. */
	void stageReceiver(Path target) throws IOException {
		requireEdt();
		if (archive == null) {
			throw new IOException("Receiver snapshot is closed");
		}
		Files.write(target, archive, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
	}

	void stageLayerContext(Path target) throws IOException {
		requireEdt();
		if (archive == null) {
			throw new IOException("Receiver snapshot is closed");
		}
		expertContext.write(target);
	}

	@Override
	public void close() {
		requireEdt();
		archive = null;
		session = null;
	}

	private static void requireEdt() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("Live inspection capture requires the EDT");
		}
	}

	private record Session(AppGeoCeDG owner, Construction construction, GeoCeDGUndoManager undo,
			long epoch, GeoCeDGDocumentInsertion.HistoryRecord history, String documentId,
			String tubeId, File file, boolean saved, boolean undoActive, int mode,
			int workingLayer, int maxLayerUsed, List<GeoElement> selection,
			Map<GeoElement, Integer> effectiveLayers) {
		private static Session read(AppGeoCeDG app, Construction construction,
				GeoCeDGUndoManager undo) {
			Map<GeoElement, Integer> layers = new IdentityHashMap<>();
			construction.getGeoSetWithCasCellsConstructionOrder()
					.forEach(geo -> layers.put(geo, geo.getLayer()));
			return new Session(app, construction, undo, undo.getEpoch(),
					GeoCeDGDocumentInsertion.HistoryRecord.of(undo), app.getUniqueId(), app.getTubeId(),
					app.getCurrentFile(), app.isSaved(), app.getKernel().isUndoActive(), app.getMode(),
					app.getLayerWorkspace().getWorkingLayer(), app.getMaxLayerUsed(),
					List.copyOf(app.getSelectionManager().getSelectedGeos()), layers);
		}

		private boolean matches(AppGeoCeDG app) {
			return owner == app && construction == app.getKernel().getConstruction()
					&& undo == construction.getUndoManager() && epoch == undo.getEpoch()
					&& Objects.equals(documentId, app.getUniqueId())
					&& Objects.equals(tubeId, app.getTubeId())
					&& Objects.equals(file, app.getCurrentFile()) && saved == app.isSaved()
					&& undoActive == app.getKernel().isUndoActive() && mode == app.getMode()
					&& workingLayer == app.getLayerWorkspace().getWorkingLayer()
					&& maxLayerUsed == app.getMaxLayerUsed()
					&& selection.equals(app.getSelectionManager().getSelectedGeos())
					&& sameEffectiveLayers()
					&& history.mismatch(undo, null) == null;
		}

		private boolean sameEffectiveLayers() {
			var geos = construction.getGeoSetWithCasCellsConstructionOrder();
			return geos.size() == effectiveLayers.size() && geos.stream()
					.allMatch(geo -> Objects.equals(effectiveLayers.get(geo), geo.getLayer()));
		}
	}

	private static final class ChangeWatch implements EventListener {
		private volatile boolean changed;

		@Override
		public void sendEvent(Event event) {
			changed = true;
		}

		@Override
		public void reset() {
			changed = true;
		}
	}

	private static final class CaptureBuffer extends ByteArrayOutputStream {
		@Override
		public synchronized void write(int value) {
			requireCapacity(1);
			super.write(value);
		}

		@Override
		public synchronized void write(byte[] bytes, int offset, int length) {
			requireCapacity(length);
			super.write(bytes, offset, length);
		}

		private void requireCapacity(int length) {
			if (length > GeoCeDGInspectionArchive.MAX_ARCHIVE_BYTES - count) {
				throw new IllegalStateException("Receiver exceeds the compressed archive budget");
			}
		}
	}
}
