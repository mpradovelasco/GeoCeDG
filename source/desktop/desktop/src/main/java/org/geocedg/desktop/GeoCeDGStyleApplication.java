/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.geos.GProperty;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.io.DocumentArchivePreflight;

/**
 * PRE-G9B-R6-plus-F3 "Apply Styles..." / "Aplicar estilos..." (T0, P5): copies object
 * colour, line style, line thickness, point style, point size and fill opacity from the
 * source document's construction default of each object's default type to the selected
 * objects, or to every object when nothing is selected, except native-dimension,
 * IsoABorder and Locus V2 family outputs. Nothing else changes (no geometry, label,
 * layer, hidden-layer membership, caption, label offset or visibility, fixed flag,
 * visibility, show condition, dynamic colour, construction default, labeling style or
 * unit). One undoable step; a source that cannot be validated and loaded changes
 * nothing. The inherited {@code AppD.applyTemplate} and {@code TemplateHelper} are
 * never used.
 */
final class GeoCeDGStyleApplication {

	/** Outcome of one style application. */
	static final class Result {
		private final boolean applied;
		private final int restyled;
		private final String message;

		Result(boolean applied, int restyled, String message) {
			this.applied = applied;
			this.restyled = restyled;
			this.message = message;
		}

		boolean isApplied() {
			return applied;
		}

		int getRestyled() {
			return restyled;
		}

		/** @return message for the user, or null */
		String getMessage() {
			return message;
		}
	}

	private final AppGeoCeDG app;

	GeoCeDGStyleApplication(AppGeoCeDG app) {
		this.app = app;
	}

	/**
	 * @param file resolved source document
	 * @return the outcome
	 */
	Result apply(File file) {
		String name = file == null ? "" : file.getName();
		if (app.isInsertionIsolated()) {
			return refused(name, "Insert.Reject.Isolated");
		}
		String refusal = GeoCeDGDocumentPolicy.insertionRefusal(file);
		if (refusal != null) {
			return refused(name, refusal);
		}
		try {
			if (!DocumentArchivePreflight.validate(Files.readAllBytes(file.toPath()),
					app.createDocumentPreflightConfig())) {
				return refused(name, "Insert.Reject.Invalid");
			}
		} catch (Exception invalid) {
			return refused(name, "Insert.Reject.Invalid");
		}
		AppGeoCeDG helper = null;
		try {
			helper = app.newInsertionHelper();
			if (!helper.loadFile(file, false)) {
				return refused(name, "Insert.Reject.Invalid");
			}
			ConstructionDefaults source = helper.getKernel().getConstruction()
					.getConstructionDefaults();
			ConstructionDefaults target = app.getKernel().getConstruction()
					.getConstructionDefaults();
			int restyled = 0;
			for (GeoElement geo : scope(app)) {
				if (isExcluded(geo)) {
					continue;
				}
				GeoElement defaults = source.getDefaultGeo(target.getDefaultType(geo));
				if (defaults != null && restyle(geo, defaults)) {
					restyled++;
				}
			}
			if (restyled > 0) {
				app.getKernel().notifyRepaint();
				app.storeUndoInfo();
				app.setUnsaved();
			}
			return new Result(true, restyled, null);
		} catch (RuntimeException failure) {
			return refused(name, "Insert.Reject.Invalid");
		} finally {
			app.releaseInsertionHelper(helper);
		}
	}

	/**
	 * @param app target document
	 * @return the selected objects of the drawing, or every object when none is selected.
	 *         The inherited selection keeps the objects of a document closed by Open, Open
	 *         Recent or New (F3-R2); they no longer belong to the drawing and never count
	 *         as selected.
	 */
	static List<GeoElement> scope(AppGeoCeDG app) {
		Kernel kernel = app.getKernel();
		List<GeoElement> scope = new ArrayList<>();
		for (GeoElement selected : app.getSelectionManager().getSelectedGeos()) {
			if (selected.isLabelSet()
					&& kernel.lookupLabel(selected.getLabelSimple()) == selected) {
				scope.add(selected);
			}
		}
		if (scope.isEmpty()) {
			scope.addAll(kernel.getConstruction().getGeoSetConstructionOrder());
		}
		return scope;
	}

	private Result refused(String name, String reasonKey) {
		return new Result(false, 0, app.layerText("Styles.Rejected", name,
				app.layerText(reasonKey)));
	}

	/**
	 * @param geo target object
	 * @return whether T0 never restyles it (native dimensions, IsoABorder, Locus V2
	 *         family outputs)
	 */
	static boolean isExcluded(GeoElement geo) {
		GeoCeDGInsertionAdmission.Family family = GeoCeDGInsertionAdmission.classify(geo);
		return family == GeoCeDGInsertionAdmission.Family.DIMENSION
				|| family == GeoCeDGInsertionAdmission.Family.ISO_A_BORDER
				|| family == GeoCeDGInsertionAdmission.Family.LOCUS_V2;
	}

	/** The six whitelisted properties only. */
	private static boolean restyle(GeoElement geo, GeoElement defaults) {
		boolean changed = false;
		if (!geo.getObjectColor().equals(defaults.getObjectColor())) {
			geo.setObjColor(defaults.getObjectColor());
			changed = true;
		}
		if (geo.getLineThickness() != defaults.getLineThickness()) {
			geo.setLineThickness(defaults.getLineThickness());
			changed = true;
		}
		if (geo.getLineType() != defaults.getLineType()) {
			geo.setLineType(defaults.getLineType());
			changed = true;
		}
		if (geo instanceof GeoPoint point && defaults instanceof GeoPoint model) {
			if (point.getPointSize() != model.getPointSize()) {
				point.setPointSize(model.getPointSize());
				changed = true;
			}
			if (point.getPointStyle() != model.getPointStyle()) {
				point.setPointStyle(model.getPointStyle());
				changed = true;
			}
		}
		if (geo.getAlphaValue() != defaults.getAlphaValue()) {
			geo.setAlphaValue(defaults.getAlphaValue());
			changed = true;
		}
		if (changed) {
			geo.updateVisualStyle(GProperty.COMBINED);
		}
		return changed;
	}
}
