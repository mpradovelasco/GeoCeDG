/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.CheckForNull;

import org.geocedg.common.importer.dxf.DxfAffineTransform;
import org.geocedg.common.importer.dxf.DxfFileProvenance;
import org.geocedg.common.importer.dxf.DxfImportLimits;
import org.geocedg.common.importer.dxf.DxfImportReport;
import org.geocedg.common.importer.dxf.DxfSplineReader;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;

/**
 * PRE-G9B-MAINT-A-DXF0 action "Import DXF reference...": chooser, units and transformation
 * dialog, read-only import, report, and the session reference manager. The DXF file is
 * opened for reading only. Nothing here touches {@code Construction}, its XML or its Undo
 * history; the records go to the document session store.
 */
final class GeoCeDGDxfReferenceImport {

	/** Chooses the DXF file; {@code null} on cancellation. */
	interface Chooser {
		@CheckForNull File choose(AppGeoCeDG app);
	}

	/** Asks the explicit transformation after showing the units; {@code null} cancels. */
	interface TransformPrompt {
		@CheckForNull DxfAffineTransform ask(AppGeoCeDG app, DxfImportReport preview);
	}

	/** Shows a read-only text (report, inspector, messages). */
	interface Presenter {
		void show(AppGeoCeDG app, String title, String text);
	}

	/** Opens the session manager. */
	interface Manager {
		void open(AppGeoCeDG app);
	}

	private final AppGeoCeDG app;
	private Chooser chooser = GeoCeDGDxfReferenceDialogs::chooseFile;
	private TransformPrompt transformPrompt = GeoCeDGDxfReferenceDialogs::askTransform;
	private Presenter presenter = GeoCeDGDxfReferenceDialogs::showText;
	private Manager manager = GeoCeDGDxfReferenceDialogs::openManager;

	GeoCeDGDxfReferenceImport(AppGeoCeDG app) {
		this.app = app;
	}

	void setPorts(Chooser chooser, TransformPrompt prompt, Presenter presenter,
			Manager manager) {
		this.chooser = chooser;
		this.transformPrompt = prompt;
		this.presenter = presenter;
		this.manager = manager;
	}

	/**
	 * The menu action: import one file; afterwards (or when the chooser is cancelled while
	 * references exist) open the session manager for inspection, visibility and removal.
	 */
	void run() {
		File file = chooser.choose(app);
		if (file != null) {
			importFile(file);
		}
		if (!app.getDxfReferenceSession().isEmpty()) {
			manager.open(app);
		}
	}

	/**
	 * Imports one file into the session store.
	 *
	 * @param file DXF file, only read
	 * @return the report shown to the user, or {@code null} when cancelled or unreadable
	 */
	@CheckForNull DxfImportReport importFile(File file) {
		String unit = documentUnit(app);
		DxfImportReport preview;
		byte[] bytes = null;
		try {
			Path path = file.toPath();
			if (Files.size(path) > DxfImportLimits.FILE_BYTES_MAX) {
				preview = DxfSplineReader.oversized(file.getName(), DxfAffineTransform.IDENTITY,
						unit);
			} else {
				bytes = Files.readAllBytes(path);
				preview = DxfSplineReader.read(bytes, file.getName(), DxfAffineTransform.IDENTITY,
						unit, DxfImportLimits.DEFAULT);
			}
		} catch (IOException exception) {
			presenter.show(app, app.layerText("DxfReference.Report.Title"),
					app.layerText("DxfReference.ReadFailed", file.getName(),
							String.valueOf(exception.getMessage())));
			return null;
		}
		if (preview.fileCause() != null || preview.records().isEmpty()) {
			presenter.show(app, app.layerText("DxfReference.Report.Title"),
					GeoCeDGDxfReferenceText.report(app, preview));
			return preview;
		}
		DxfAffineTransform transform = transformPrompt.ask(app, preview);
		if (transform == null) {
			return null;
		}
		DxfImportReport report = transform.isIdentity() ? preview
				: DxfSplineReader.read(bytes, file.getName(), transform, unit,
						DxfImportLimits.DEFAULT);
		app.getDxfReferenceSession().addAll(report.records());
		presenter.show(app, app.layerText("DxfReference.Report.Title"),
				GeoCeDGDxfReferenceText.report(app, report));
		return report;
	}

	/** @return the document construction unit token, or {@code UNSPECIFIED_MODEL_UNIT} */
	static String documentUnit(AppGeoCeDG app) {
		UnitState state = app.getDocumentUnits().getState();
		UnitToken unit = state == null ? null : state.effectiveConstructionUnit();
		return unit == null ? DxfFileProvenance.UNSPECIFIED_MODEL_UNIT : unit.token();
	}

	Presenter presenter() {
		return presenter;
	}
}
