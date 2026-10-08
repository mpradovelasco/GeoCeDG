/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.dimension;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoVector;
import org.geogebra.common.kernel.kernelND.GeoPointND;

/**
 * {@code LinearDimension(A, B, Direction, Offset[, Overshoot, Gap])}:
 * {@code L = |<B - A, unit(Direction)>|}. The E2-local direction authority is
 * {@link GeoLine#getDirection(double[])} for a line, segment, ray or axis and the
 * coordinates for a vector; {@code getDirectionInD3} is never used. The direction sign
 * never changes the value; with the sign of the offset it fixes the side (left normal
 * of the direction).
 */
public final class AlgoLinearDimension extends AlgoNativeDimension {

	private final GeoElement direction;

	/**
	 * @param cons construction
	 * @param labels output labels, possibly null
	 * @param pointA measured point A
	 * @param pointB measured point B
	 * @param direction a 2D line (including segment, ray, axis) or a 2D vector
	 * @param offset signed offset
	 * @param overshoot overshoot, or null (0)
	 * @param gap gap, or null (0); present exactly when overshoot is present
	 */
	public AlgoLinearDimension(Construction cons, String[] labels, GeoPointND pointA,
			GeoPointND pointB, GeoElement direction, GeoNumberValue offset,
			GeoNumberValue overshoot, GeoNumberValue gap) {
		super(cons, pointA, pointB, offset, overshoot, gap);
		this.direction = direction;
		initialize(labels);
	}

	/**
	 * @param geo candidate direction
	 * @return whether the geo is an admissible 2D direction (line, segment, ray, axis
	 *         or vector)
	 */
	public static boolean isDirection(GeoElement geo) {
		return geo != null && !geo.isGeoElement3D()
				&& (geo instanceof GeoLine || geo instanceof GeoVector);
	}

	@Override
	protected GeoElement[] commandInputs() {
		if (getOvershootInput() == null) {
			return new GeoElement[] {getPointA().toGeoElement(), getPointB().toGeoElement(),
					direction, getOffsetInput().toGeoElement()};
		}
		return new GeoElement[] {getPointA().toGeoElement(), getPointB().toGeoElement(),
				direction, getOffsetInput().toGeoElement(),
				getOvershootInput().toGeoElement(), getGapInput().toGeoElement()};
	}

	@Override
	protected DimensionFigure2D figure(double ax, double ay, double bx, double by,
			double s, double o, double g) {
		double[] d = directionOf(direction);
		return DimensionFigure2D.linear(ax, ay, bx, by, d[0], d[1], s, o, g);
	}

	/**
	 * @param geo a line or vector
	 * @return its kernel direction, or NaN components when undefined
	 */
	static double[] directionOf(GeoElement geo) {
		double[] d = {Double.NaN, Double.NaN};
		if (geo == null || !geo.isDefined()) {
			return d;
		}
		if (geo instanceof GeoLine) {
			((GeoLine) geo).getDirection(d);
		} else if (geo instanceof GeoVector) {
			d[0] = ((GeoVector) geo).getX();
			d[1] = ((GeoVector) geo).getY();
		}
		return d;
	}

	/**
	 * @return the direction input
	 */
	public GeoElement getDirectionInput() {
		return direction;
	}

	@Override
	public Commands getClassName() {
		return Commands.LinearDimension;
	}

	@Override
	public int getRelatedModeID() {
		return EuclidianConstants.MODE_LINEAR_DIMENSION;
	}
}
