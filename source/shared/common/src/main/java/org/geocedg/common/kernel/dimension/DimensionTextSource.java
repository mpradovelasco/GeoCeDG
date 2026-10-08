/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.dimension;

import org.geogebra.common.kernel.geos.GeoText;

/**
 * Minimal semantic seam between a native dimension algorithm and the presentation
 * layer (PRE-G9B-R6-plus-E2, DQ-E2-6). It identifies the presentation text of a
 * dimension and exposes model-space geometry only: no screen angle, pixel box, DPI,
 * viewport transform or screen offset. Renderers and exporters derive the aligned,
 * readable placement from it at drawing time and never store it.
 */
public interface DimensionTextSource {

	/**
	 * @param text a text
	 * @return whether the text is this dimension's presentation text output
	 */
	boolean isDimensionPresentationText(GeoText text);

	/**
	 * @return {anchorX, anchorY, dirX, dirY}: the dimension-line midpoint and the
	 *         unit measurement direction in model coordinates, or null when the text is
	 *         undefined
	 */
	double[] getDimensionTextFrame();

	/**
	 * @return dimension-line end points {x1, y1, x2, y2} in model coordinates, or null
	 *         when the dimension line is undefined
	 */
	double[] getDimensionLineEndpoints();

	/**
	 * @return outer tips of the extension lines {xA, yA, xB, yB} in model coordinates;
	 *         an undefined extension line contributes NaN
	 */
	double[] getExtensionTips();

	/**
	 * @return the model-space unit vector from the measured points towards the
	 *         dimension line (the overshoot side), or null when the offset is zero or
	 *         undefined
	 */
	double[] getOvershootDirection();
}
