/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoVector;
import org.geogebra.common.main.error.ErrorHelper;
import org.geogebra.desktop.main.AppD;

/** Tool selection/dialog adapter for the existing public R5 construction operations. */
final class GeoCeDGSimilarityTools {

	private final AppD app;
	private GeoLocusV2 source;

	GeoCeDGSimilarityTools(AppD app) {
		this.app = app;
	}

	void reset() {
		source = null;
	}

	boolean isSelecting() {
		return source != null;
	}

	boolean handles(int mode, List<GeoElement> hits) {
		return supported(mode) && (source != null
				|| hits.stream().anyMatch(GeoLocusV2.class::isInstance)
				|| app.getSelectionManager().getSelectedGeos().stream()
						.anyMatch(GeoLocusV2.class::isInstance));
	}

	GeoLocusV2 click(int mode, List<GeoElement> hits, boolean preview) {
		if (preview) {
			return null;
		}
		if (!RuntimeFeatureService.mayCreateLocusV2(app.getKernel().getConstruction())) {
			app.showError("LocusV2.FeatureDisabled");
			reset();
			return null;
		}
		List<GeoElement> selected = new ArrayList<>(app.getSelectionManager().getSelectedGeos());
		if (source == null) {
			List<GeoLocusV2> sources = hits.stream().filter(GeoLocusV2.class::isInstance)
					.map(GeoLocusV2.class::cast).toList();
			if (sources.isEmpty()) {
				sources = selected.stream().filter(GeoLocusV2.class::isInstance)
						.map(GeoLocusV2.class::cast).toList();
			}
			source = choose(sources, mode);
			if (source == null) {
				return null;
			}
			app.getSelectionManager().addSelectedGeo(source);
		}
		List<GeoElement> operands = hits.stream().filter(geo -> operand(mode, geo)).toList();
		if (operands.isEmpty()) {
			operands = selected.stream().filter(geo -> operand(mode, geo)).toList();
		}
		GeoElement argument = choose(operands, mode);
		if (argument == null) {
			return null;
		}
		if (app.getKernel().isAxis(argument)) {
			// G9A deliberately excludes built-in constants from ordinary durable
			// attachments. Do not invent an ID or an implicit replacement line.
			JOptionPane.showMessageDialog(app.getMainComponent(), GeoCeDGProfile.getText(
					"Similarity.OrdinaryAxisRequired", app.getLocale().getLanguage()),
					app.getToolName(mode), JOptionPane.INFORMATION_MESSAGE);
			finish();
			return null;
		}
		String input = null;
		if (mode == EuclidianConstants.MODE_ROTATE_BY_ANGLE
				|| mode == EuclidianConstants.MODE_DILATE_FROM_POINT) {
			input = JOptionPane.showInputDialog(app.getMainComponent(),
					app.getLocalization().getMenu(mode == EuclidianConstants.MODE_ROTATE_BY_ANGLE
							? "Angle" : "Dilate.Factor"),
					mode == EuclidianConstants.MODE_ROTATE_BY_ANGLE ? "45\u00b0" : "2");
			if (input == null) {
				finish();
				return null;
			}
		}
		String numericInput = input;
		GeoLocusV2[] result = new GeoLocusV2[1];
		try {
			var registry = app.getKernel().getConstruction().getSpatialIdentityRegistry();
			var id = registry.getPersistentGeoId(source);
			// Ordinary operands need not have a spatial registry ID yet. Check
			// current object membership, never substitute a label/position match.
			boolean currentOperand = app.getKernel().getConstruction()
					.getGeoSetConstructionOrder().stream().anyMatch(geo -> geo == argument);
			if (id == null || registry.getGeo(id) != source || !currentOperand) {
				throw new IllegalArgumentException("Obsolete semantic source selection");
			}
			app.getKernel().getConstruction().runAtomicConstructionMutation(() -> {
				GeoNumberValue value = numericInput == null ? null : app.getKernel()
						.getAlgebraProcessor().evaluateToNumeric(numericInput,
								ErrorHelper.silent());
				if (numericInput != null && (value == null
						|| !Double.isFinite(value.getDouble()))) {
					throw new IllegalArgumentException("Invalid similarity input");
				}
				result[0] = construct(mode, argument, value);
				result[0].setLabel(null);
			});
			return result[0];
		} catch (IllegalArgumentException exception) {
			app.showError("InvalidInput");
			return null;
		} finally {
			finish();
		}
	}

	private GeoLocusV2 construct(int mode, GeoElement argument, GeoNumberValue value) {
		var construction = app.getKernel().getConstruction();
		switch (mode) {
		case EuclidianConstants.MODE_TRANSLATE_BY_VECTOR:
			return LocusV2PublicOperations.translate(construction, null, source,
					(GeoVector) argument);
		case EuclidianConstants.MODE_MIRROR_AT_LINE:
			return LocusV2PublicOperations.reflect(construction, null, source, (GeoLine) argument);
		case EuclidianConstants.MODE_MIRROR_AT_POINT:
			return LocusV2PublicOperations.reflect(construction, null, source, (GeoPoint) argument);
		case EuclidianConstants.MODE_ROTATE_BY_ANGLE:
			return LocusV2PublicOperations.rotate(construction, null, source, value,
					(GeoPoint) argument);
		case EuclidianConstants.MODE_DILATE_FROM_POINT:
			return LocusV2PublicOperations.dilate(construction, null, source, value,
					(GeoPoint) argument);
		default:
			throw new IllegalArgumentException("Unsupported similarity tool");
		}
	}

	private void finish() {
		reset();
		app.getSelectionManager().clearSelectedGeos();
	}

	private <T> T choose(List<T> choices, int mode) {
		if (choices.size() == 1) {
			return choices.get(0);
		}
		if (choices.isEmpty()) {
			return null;
		}
		Object selected = JOptionPane.showInputDialog(app.getMainComponent(),
				app.getToolHelp(mode), app.getToolName(mode), JOptionPane.QUESTION_MESSAGE,
				null, choices.toArray(), null);
		for (T choice : choices) {
			if (choice == selected) {
				return choice;
			}
		}
		return null;
	}

	private static boolean operand(int mode, GeoElement geo) {
		if (geo.isGeoElement3D()) {
			return false;
		}
		if (mode == EuclidianConstants.MODE_TRANSLATE_BY_VECTOR) {
			return geo instanceof GeoVector;
		}
		if (mode == EuclidianConstants.MODE_MIRROR_AT_LINE) {
			return geo instanceof GeoLine;
		}
		return geo instanceof GeoPoint;
	}

	private static boolean supported(int mode) {
		return mode == EuclidianConstants.MODE_TRANSLATE_BY_VECTOR
				|| mode == EuclidianConstants.MODE_MIRROR_AT_LINE
				|| mode == EuclidianConstants.MODE_MIRROR_AT_POINT
				|| mode == EuclidianConstants.MODE_ROTATE_BY_ANGLE
				|| mode == EuclidianConstants.MODE_DILATE_FROM_POINT;
	}
}
