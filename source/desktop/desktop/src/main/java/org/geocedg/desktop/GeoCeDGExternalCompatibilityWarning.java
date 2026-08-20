/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import javax.swing.JOptionPane;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.main.AppD;

/** GeoCeDG-only warning at the final native-file write boundary. */
final class GeoCeDGExternalCompatibilityWarning {

	private GeoCeDGExternalCompatibilityWarning() {
	}

	/**
	 * @param app GeoCeDG desktop application
	 * @return whether the requested native save may continue
	 */
	static boolean confirmSave(AppD app) {
		if (!containsNativeLocusV2(app.getKernel().getConstruction())) {
			return true;
		}
		Object[] options = {app.getLocalization().getMenu("Save"),
				app.getLocalization().getMenu("Cancel")};
		int decision = JOptionPane.showOptionDialog(app.getMainComponent(),
				app.getLocalization().getMenu(
						"LocusV2.ExternalCompatibility.SaveWarning"),
				app.getLocalization().getMenu(
						"LocusV2.ExternalCompatibility.Title"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
				options, options[1]);
		return decision == 0;
	}

	/**
	 * @param construction construction being saved
	 * @return whether native experimental Locus V2 data are present
	 */
	static boolean containsNativeLocusV2(Construction construction) {
		for (GeoElement geo : construction.getGeoSetConstructionOrder()) {
			if (geo instanceof GeoLocusV2
					|| geo instanceof GeoLocusMetricResult
					|| geo instanceof GeoLocusIntersectionResult) {
				return true;
			}
		}
		return false;
	}
}
