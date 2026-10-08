/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.dimension;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.kernelND.GeoPointND;

/**
 * {@code AlignedDimension(A, B, Offset[, Overshoot, Gap])}: {@code L = |B - A|}; the
 * ordered pair A to B fixes the measurement direction and the sign of the offset the
 * side (left normal of A to B).
 */
public final class AlgoAlignedDimension extends AlgoNativeDimension {

	/**
	 * @param cons construction
	 * @param labels output labels, possibly null
	 * @param pointA measured point A
	 * @param pointB measured point B
	 * @param offset signed offset
	 * @param overshoot overshoot, or null (0)
	 * @param gap gap, or null (0); present exactly when overshoot is present
	 */
	public AlgoAlignedDimension(Construction cons, String[] labels, GeoPointND pointA,
			GeoPointND pointB, GeoNumberValue offset, GeoNumberValue overshoot,
			GeoNumberValue gap) {
		super(cons, pointA, pointB, offset, overshoot, gap);
		initialize(labels);
	}

	@Override
	protected GeoElement[] commandInputs() {
		if (getOvershootInput() == null) {
			return new GeoElement[] {getPointA().toGeoElement(),
					getPointB().toGeoElement(), getOffsetInput().toGeoElement()};
		}
		return new GeoElement[] {getPointA().toGeoElement(), getPointB().toGeoElement(),
				getOffsetInput().toGeoElement(), getOvershootInput().toGeoElement(),
				getGapInput().toGeoElement()};
	}

	@Override
	protected DimensionFigure2D figure(double ax, double ay, double bx, double by,
			double s, double o, double g) {
		return DimensionFigure2D.aligned(ax, ay, bx, by, s, o, g);
	}

	@Override
	public Commands getClassName() {
		return Commands.AlignedDimension;
	}

	@Override
	public int getRelatedModeID() {
		return EuclidianConstants.MODE_ALIGNED_DIMENSION;
	}
}
