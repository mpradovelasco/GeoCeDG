/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geogebra.common.kernel.commands;

import org.geocedg.common.kernel.sheet.AlgoIsoABorder;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.MyError;

/**
 * GeoCeDG native {@code IsoABorder(<Point>, <ISO A index>, <Landscape>,
 * <Scale numerator>, <Scale denominator>, <Model units per millimetre>, <Inner frame>)}
 * (PRE-G9B-R6-plus-E3). One signature, no overloads; every input is explicit and no
 * default reads units, the session scale or a view.
 */
public final class CmdIsoABorder extends CommandProcessor {

	/**
	 * @param kernel kernel
	 */
	public CmdIsoABorder(Kernel kernel) {
		super(kernel);
	}

	@Override
	public GeoElement[] process(Command command, EvalInfo info) throws MyError {
		if (command.getArgumentNumber() != 7) {
			throw argNumErr(command);
		}
		GeoElement[] arguments = resArgs(command, info);
		if (!CmdAlignedDimension.isPlanePoint(arguments[0])) {
			throw argErr(command, arguments[0]);
		}
		for (int i : new int[] {1, 3, 4, 5}) {
			if (!(arguments[i] instanceof GeoNumberValue)) {
				throw argErr(command, arguments[i]);
			}
		}
		for (int i : new int[] {2, 6}) {
			if (!arguments[i].isGeoBoolean()) {
				throw argErr(command, arguments[i]);
			}
		}
		AlgoIsoABorder algo = new AlgoIsoABorder(cons, command.getLabels(),
				(GeoPointND) arguments[0], (GeoNumberValue) arguments[1],
				(GeoBoolean) arguments[2], (GeoNumberValue) arguments[3],
				(GeoNumberValue) arguments[4], (GeoNumberValue) arguments[5],
				(GeoBoolean) arguments[6]);
		return algo.getOutput();
	}
}
