/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.Hits;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.MyError;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.desktop.geogebra3D.euclidianFor3D.EuclidianControllerFor3DD;
import org.geogebra.desktop.main.AppD;

/** GeoCeDG-only controller seams for default-off experimental Locus V2 tools. */
public final class GeoCeDGEuclidianController
		extends EuclidianControllerFor3DD {

	private final GeoCeDGLocusV2Dialogs locusV2Dialogs;

	/**
	 * @param kernel kernel
	 */
	public GeoCeDGEuclidianController(Kernel kernel) {
		super(kernel);
		locusV2Dialogs = new GeoCeDGLocusV2Dialogs(
				(AppD) kernel.getApplication());
	}

	@Override
	protected boolean switchModeForProcessMode(Hits hits, boolean controlDown,
			boolean shiftDown, AsyncOperation<Boolean> callback,
			boolean selectionPreview) {
		if (!isLocusV2Mode(mode)) {
			return super.switchModeForProcessMode(hits, controlDown, shiftDown,
					callback, selectionPreview);
		}
		GeoElementND[] output = null;
		Construction construction = kernel.getConstruction();
		if (!RuntimeFeatureService.mayCreateLocusV2(construction)) {
			showFeatureError("LocusV2.FeatureDisabled", selectionPreview);
		} else {
			switch (mode) {
			case EuclidianConstants.MODE_LOCUS_V2:
				output = createPointDrivenLocus(hits, selectionPreview);
				break;
			case EuclidianConstants.MODE_LOCUS_V2_POINT:
				output = createSemanticPoint(hits, selectionPreview);
				break;
			case EuclidianConstants.MODE_LOCUS_V2_LENGTH:
				output = createTotalMetric(hits, selectionPreview);
				break;
			case EuclidianConstants.MODE_LOCUS_V2_LENGTH_BETWEEN:
				output = createBetweenMetric(hits, selectionPreview);
				break;
			default:
				break;
			}
		}
		return endOfSwitchModeForProcessMode(output, false, callback,
				selectionPreview);
	}

	@Override
	protected GeoElementND[] intersect(Hits hits, boolean selectionPreview) {
		if (!containsLocusV2(hits)) {
			return super.intersect(hits, selectionPreview);
		}
		Construction construction = kernel.getConstruction();
		if (!RuntimeFeatureService.mayCreateLocusV2(construction)) {
			showFeatureError("LocusV2.FeatureDisabled", selectionPreview);
			return null;
		}
		addSelectedGeo(hits, 2, false, selectionPreview);
		if (selGeos() < 2) {
			return null;
		}
		GeoElement[] selected = getSelectedGeos();
		GeoLocusV2 source = selected[0] instanceof GeoLocusV2
				? (GeoLocusV2) selected[0]
				: selected[1] instanceof GeoLocusV2
						? (GeoLocusV2) selected[1] : null;
		if (source == null) {
			return null;
		}
		GeoElement target = source == selected[0] ? selected[1] : selected[0];
		try {
			return new GeoElementND[] {ensureLabel(LocusV2PublicOperations.intersect(
					construction, null, source, target))};
		} catch (IllegalArgumentException exception) {
			showFeatureError("LocusV2.UnsupportedTarget", selectionPreview);
			return null;
		}
	}

	private GeoElementND[] createPointDrivenLocus(Hits hits,
			boolean selectionPreview) {
		if (hits.isEmpty()) {
			return null;
		}
		addSelectedPoint(hits, 2, false, selectionPreview);
		if (selPoints() < 2) {
			return null;
		}
		GeoPointND[] selected = getSelectedPointsND();
		if (!(selected[0] instanceof GeoPoint)
				|| !(selected[1] instanceof GeoPoint)) {
			showFeatureError("LocusV2.UnsupportedGenerator", selectionPreview);
			return null;
		}
		GeoPoint dependent = (GeoPoint) selected[0];
		GeoPoint driver = (GeoPoint) selected[1];
		if (!dependent.getAllPredecessors().contains(driver)) {
			clearSelections();
			showFeatureError("LocusV2.UnsupportedGenerator", selectionPreview);
			return null;
		}
		try {
			return new GeoElementND[] {ensureLabel(
					LocusV2PublicOperations.createPointDriven(
					kernel.getConstruction(), null, dependent, driver))};
		} catch (IllegalArgumentException exception) {
			clearSelections();
			showFeatureError("LocusV2.UnsupportedGenerator", selectionPreview);
			return null;
		}
	}

	private GeoElementND[] createSemanticPoint(Hits hits,
			boolean selectionPreview) {
		GeoLocusV2 source = selectSingleLocus(hits, selectionPreview);
		if (source == null || selectionPreview) {
			return null;
		}
		GeoPoint point = locusV2Dialogs.createSemanticPoint(source);
		return point == null ? null : new GeoElementND[] {ensureLabel(point)};
	}

	private GeoElementND[] createTotalMetric(Hits hits,
			boolean selectionPreview) {
		GeoLocusV2 source = selectSingleLocus(hits, selectionPreview);
		if (source == null || selectionPreview) {
			return null;
		}
		try {
			GeoLocusMetricResult result =
					ensureLabel(LocusV2PublicOperations.totalMetric(
							kernel.getConstruction(), null, source));
			locusV2Dialogs.inspectMetric(result);
			return new GeoElementND[] {result};
		} catch (IllegalArgumentException exception) {
			showFeatureError("LocusV2.MetricUnavailable", false);
			return null;
		}
	}

	private GeoElementND[] createBetweenMetric(Hits hits,
			boolean selectionPreview) {
		if (hits.isEmpty()) {
			return null;
		}
		addSelectedGeo(hits, 3, false, selectionPreview);
		if (selGeos() < 3 || selectionPreview) {
			return null;
		}
		GeoElement[] selected = getSelectedGeos();
		GeoLocusV2 source = null;
		GeoPoint[] positions = new GeoPoint[2];
		int positionCount = 0;
		for (GeoElement geo : selected) {
			if (geo instanceof GeoLocusV2 && source == null) {
				source = (GeoLocusV2) geo;
			} else if (geo instanceof GeoPoint && positionCount < 2) {
				positions[positionCount++] = (GeoPoint) geo;
			} else {
				showFeatureError("LocusV2.InvalidPosition", false);
				return null;
			}
		}
		if (source == null || positionCount != 2) {
			showFeatureError("LocusV2.InvalidPosition", false);
			return null;
		}
		try {
			GeoLocusMetricResult result =
					ensureLabel(LocusV2PublicOperations.betweenMetric(
							kernel.getConstruction(), null, source,
							positions[0], positions[1]));
			locusV2Dialogs.inspectMetric(result);
			return new GeoElementND[] {result};
		} catch (IllegalArgumentException exception) {
			showFeatureError("LocusV2.InvalidPosition", false);
			return null;
		}
	}

	private GeoLocusV2 selectSingleLocus(Hits hits,
			boolean selectionPreview) {
		if (hits.isEmpty()) {
			return null;
		}
		addSelectedGeo(hits, 1, false, selectionPreview);
		if (selGeos() < 1 || selectionPreview) {
			return null;
		}
		GeoElement[] selected = getSelectedGeos();
		if (selected.length == 1 && selected[0] instanceof GeoLocusV2) {
			return (GeoLocusV2) selected[0];
		}
		showFeatureError("LocusV2.UnsupportedGenerator", false);
		return null;
	}

	/** Opens the rich inspector and records an exact-token point, if chosen. */
	public void inspectRichResultSelection() {
		Construction construction = kernel.getConstruction();
		if (!RuntimeFeatureService.mayCreateLocusV2(construction)) {
			showFeatureError("LocusV2.FeatureDisabled", false);
			return;
		}
		GeoPoint point = locusV2Dialogs.inspectRichResult();
		if (point != null) {
			ensureLabel(point);
			app.storeUndoInfo();
			kernel.notifyRepaint();
		}
	}

	private static boolean isLocusV2Mode(int currentMode) {
		return currentMode == EuclidianConstants.MODE_LOCUS_V2
				|| currentMode == EuclidianConstants.MODE_LOCUS_V2_POINT
				|| currentMode == EuclidianConstants.MODE_LOCUS_V2_LENGTH
				|| currentMode
						== EuclidianConstants.MODE_LOCUS_V2_LENGTH_BETWEEN;
	}

	private static <T extends GeoElement> T ensureLabel(T output) {
		if (!output.isLabelSet()) {
			output.setLabel(null);
		}
		return output;
	}

	private boolean containsLocusV2(Hits hits) {
		for (GeoElement hit : hits) {
			if (hit instanceof GeoLocusV2) {
				return true;
			}
		}
		for (GeoElement selected : getSelectedGeoList()) {
			if (selected instanceof GeoLocusV2) {
				return true;
			}
		}
		return false;
	}

	private void showFeatureError(String key, boolean selectionPreview) {
		if (!selectionPreview && app.isErrorDialogsActive()) {
			app.showError(new MyError(app.getLocalization(),
					app.getLocalization().getMenu(key)));
		}
	}
}
