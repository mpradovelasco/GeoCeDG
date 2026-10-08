/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geogebra.common.kernel.commands;

import org.geocedg.common.kernel.dimension.AlgoAlignedDimension;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.MyError;

/**
 * GeoCeDG native {@code AlignedDimension(<Point A>, <Point B>, <Offset>
 * [, <Overshoot>, <Gap>])} (PRE-G9B-R6-plus-E2). Omitted overshoot and gap are 0; no
 * default reads units, view or length.
 */
public final class CmdAlignedDimension extends CommandProcessor {

	/**
	 * @param kernel kernel
	 */
	public CmdAlignedDimension(Kernel kernel) {
		super(kernel);
	}

	@Override
	public GeoElement[] process(Command command, EvalInfo info) throws MyError {
		int n = command.getArgumentNumber();
		if (n != 3 && n != 5) {
			throw argNumErr(command);
		}
		GeoElement[] arguments = resArgs(command, info);
		for (int i = 0; i < 2; i++) {
			if (!isPlanePoint(arguments[i])) {
				throw argErr(command, arguments[i]);
			}
		}
		for (int i = 2; i < n; i++) {
			if (!(arguments[i] instanceof GeoNumberValue)) {
				throw argErr(command, arguments[i]);
			}
		}
		AlgoAlignedDimension algo = new AlgoAlignedDimension(cons, command.getLabels(),
				(GeoPointND) arguments[0], (GeoPointND) arguments[1],
				(GeoNumberValue) arguments[2],
				n == 5 ? (GeoNumberValue) arguments[3] : null,
				n == 5 ? (GeoNumberValue) arguments[4] : null);
		return algo.getOutput();
	}

	/**
	 * @param geo argument
	 * @return whether it is a 2D point
	 */
	static boolean isPlanePoint(GeoElement geo) {
		return geo != null && geo.isGeoPoint() && !geo.isGeoElement3D();
	}
}
