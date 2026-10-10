/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.math.BigDecimal;

import org.geocedg.common.kernel.sheet.AlgoIsoABorder;
import org.geocedg.common.kernel.sheet.IsoASheet;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.desktop.export.DrawingScale;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.MyError;

/**
 * Desktop orchestration of the ISO A sheet tool (PRE-G9B-R6-plus-E3, design section
 * 12). It gates on a physical construction unit, captures {@code u = 10^-3 / fb(effC)}
 * and the drawing scale once, issues one ordinary {@code IsoABorder} command with
 * literal inputs, and asks whether to activate the sheet as the export area. After
 * creation nothing here is read again; the geometry is the kernel's.
 */
final class GeoCeDGIsoABorderTool {

	/** Initial ISO A size of the dialog (A3). */
	static final int DEFAULT_INDEX = 3;

	private final AppGeoCeDG app;

	GeoCeDGIsoABorderTool(AppGeoCeDG app) {
		this.app = app;
	}

	/**
	 * @param mode tool mode
	 * @return whether the mode is the ISO A sheet tool
	 */
	static boolean handles(int mode) {
		return mode == EuclidianConstants.MODE_ISO_A_BORDER;
	}

	/**
	 * Unit gate (DQ-E3-7, design section 12): with an unspecified unit, or a custom
	 * unit whose millimetre factor is not representable, the sheet actions are
	 * unavailable with this reason; nothing is inferred.
	 *
	 * @return localized reason, or null when a sheet may be created
	 */
	String unavailableReason() {
		UnitState state = app.getDocumentUnits().getState();
		if (!state.isPhysical()) {
			return app.layerText("IsoA.Unavailable.Unit");
		}
		if (!IsoASheet.isUnitFactor(IsoASheet.captureUnitFactor(
				state.effectiveConstructionMetresPerUnit()))) {
			return app.layerText("IsoA.Unavailable.Factor");
		}
		return null;
	}

	/**
	 * Applies the unit gate: shows the reason and, for a missing unit, offers
	 * Document Units.
	 *
	 * @return whether creation may proceed; otherwise the reason was shown
	 */
	boolean checkUnit() {
		String reason = unavailableReason();
		if (reason == null) {
			return true;
		}
		boolean unit = !app.getDocumentUnits().getState().isPhysical();
		if (app.getIsoABorderPrompt().inform(app, reason, app.layerText("IsoA.Dialog.Title"),
				unit ? app.layerText("IsoA.Unavailable.UnitAction") : null) && unit) {
			app.openDocumentUnits();
		}
		return false;
	}

	/**
	 * Creates one sheet at the reference point.
	 *
	 * @param point reference point (upper-left corner)
	 * @param createdByClick whether the tool click created the point; it is then hidden
	 *            on success and removed on cancel
	 * @return outputs, or null when nothing was created
	 */
	GeoElement[] create(GeoPointND point, boolean createdByClick) {
		if (!checkUnit()) {
			cleanup(point, createdByClick);
			return null;
		}
		UnitState state = app.getDocumentUnits().getState();
		double unitFactor = IsoASheet.captureUnitFactor(
				state.effectiveConstructionMetresPerUnit());
		GeoCeDGIsoABorderPrompt.SheetRequest request = app.getIsoABorderPrompt().askSheet(app,
				new GeoCeDGIsoABorderPrompt.SheetRequest(DEFAULT_INDEX, true,
						app.getDrawingScale(), IsoASheet.isFrameDefault(DEFAULT_INDEX), true));
		if (request == null || !IsoASheet.isIndex(request.index())) {
			cleanup(point, createdByClick);
			return null;
		}
		// the dialog never offers an impossible frame; a forced one is refused, not reduced
		boolean frame = request.innerFrame()
				&& IsoASheet.isFramePossible(request.index(), request.landscape());
		GeoElement[] created = issue(point, request, unitFactor, frame);
		if (created == null) {
			cleanup(point, createdByClick);
			return null;
		}
		AlgoIsoABorder algo = AlgoIsoABorder.ownerOf(created[0]);
		if (!request.showLabel()) {
			algo.getLabelText().setEuclidianVisible(false);
		}
		if (!frame) {
			// OTQ-E3-2: an undefined frame may be auxiliary; it stays explicitly undefined
			algo.getFrame().setAuxiliaryObject(true);
		}
		if (createdByClick) {
			// DQ-E3-2 / AQ-G3: corner points are hidden in the sheet presentation
			point.toGeoElement().setEuclidianVisible(false);
			point.toGeoElement().updateRepaint();
		}
		for (GeoElement geo : created) {
			geo.updateRepaint();
		}
		if (app.getIsoABorderPrompt().confirm(app, app.layerText("IsoA.Activate.Question"),
				app.layerText("IsoA.Activate.Title"))) {
			app.activateIsoABorder(algo);
		}
		return created;
	}

	private GeoElement[] issue(GeoPointND point, GeoCeDGIsoABorderPrompt.SheetRequest request,
			double unitFactor, boolean frame) {
		DrawingScale scale = request.scale();
		String command = "IsoABorder(" + point.getLabel(StringTemplate.defaultTemplate) + ","
				+ request.index() + "," + request.landscape() + "," + scale.getNumerator()
				+ "," + scale.getDenominator() + "," + literal(unitFactor) + "," + frame + ")";
		try {
			GeoElementND[] result = app.getKernel().getAlgebraProcessor()
					.processAlgebraCommandNoExceptionHandling(command, false,
							app.getErrorHandler(), false, null);
			if (result == null || result.length != 3) {
				return null;
			}
			GeoElement[] out = new GeoElement[result.length];
			for (int i = 0; i < result.length; i++) {
				out[i] = result[i].toGeoElement();
			}
			return out;
		} catch (MyError e) {
			app.showError(e);
			return null;
		}
	}

	/**
	 * The shortest round-trip decimal of the captured factor, in plain notation so the
	 * literal parses back to the identical binary64 (design section 7.2).
	 *
	 * @param value finite positive factor
	 * @return literal text
	 */
	static String literal(double value) {
		String shortest = Double.toString(value);
		return shortest.indexOf('E') < 0 ? shortest
				: new BigDecimal(shortest).toPlainString();
	}

	private static void cleanup(GeoPointND point, boolean createdByClick) {
		if (createdByClick && point != null) {
			point.toGeoElement().remove();
		}
	}
}
