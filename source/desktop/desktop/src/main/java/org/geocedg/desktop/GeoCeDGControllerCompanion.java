/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianController;
import org.geogebra.common.geogebra3D.euclidianFor3D.EuclidianControllerFor3DCompanion;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.kernelND.GeoPointND;

/**
 * Controller companion of GeoCeDG Graphics View 1. Its only change: the angle created
 * by the Angle with Given Size tool is hidden in the graphical views by the ordinary
 * object visibility (geocedg/specs/ui/authoring-memory.md section 8). The four-argument
 * {@code createAngle} is reached only through {@code DialogManager.doAngleFixed},
 * which stores the undo state after this method returns. Constructed inside the
 * controller's superclass constructor, so it reads no controller field.
 */
final class GeoCeDGControllerCompanion extends EuclidianControllerFor3DCompanion {

	/**
	 * @param ec Graphics View 1 controller
	 */
	GeoCeDGControllerCompanion(EuclidianController ec) {
		super(ec);
	}

	@Override
	public GeoAngle createAngle(GeoPointND p1, GeoPointND p2, GeoNumberValue a,
			boolean clockWise) {
		GeoAngle angle = super.createAngle(p1, p2, a, clockWise);
		if (angle != null && ec.getMode() == EuclidianConstants.MODE_ANGLE_FIXED) {
			// Tool route only: the rotated point and the angle's geometry stay as created.
			angle.setEuclidianVisible(false);
			angle.updateRepaint();
		}
		return angle;
	}
}
