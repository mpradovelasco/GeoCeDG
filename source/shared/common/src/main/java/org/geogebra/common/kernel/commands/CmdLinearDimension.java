/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geogebra.common.kernel.commands;

import org.geocedg.common.kernel.dimension.AlgoLinearDimension;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.MyError;

/**
 * GeoCeDG native {@code LinearDimension(<Point A>, <Point B>, <Direction>, <Offset>
 * [, <Overshoot>, <Gap>])} (PRE-G9B-R6-plus-E2); the direction is a 2D line (including
 * segment, ray and axis) or a 2D vector. Omitted overshoot and gap are 0.
 */
public final class CmdLinearDimension extends CommandProcessor {

	/**
	 * @param kernel kernel
	 */
	public CmdLinearDimension(Kernel kernel) {
		super(kernel);
	}

	@Override
	public GeoElement[] process(Command command, EvalInfo info) throws MyError {
		int n = command.getArgumentNumber();
		if (n != 4 && n != 6) {
			throw argNumErr(command);
		}
		GeoElement[] arguments = resArgs(command, info);
		for (int i = 0; i < 2; i++) {
			if (!CmdAlignedDimension.isPlanePoint(arguments[i])) {
				throw argErr(command, arguments[i]);
			}
		}
		if (!AlgoLinearDimension.isDirection(arguments[2])) {
			throw argErr(command, arguments[2]);
		}
		for (int i = 3; i < n; i++) {
			if (!(arguments[i] instanceof GeoNumberValue)) {
				throw argErr(command, arguments[i]);
			}
		}
		AlgoLinearDimension algo = new AlgoLinearDimension(cons, command.getLabels(),
				(GeoPointND) arguments[0], (GeoPointND) arguments[1], arguments[2],
				(GeoNumberValue) arguments[3],
				n == 6 ? (GeoNumberValue) arguments[4] : null,
				n == 6 ? (GeoNumberValue) arguments[5] : null);
		return algo.getOutput();
	}
}
