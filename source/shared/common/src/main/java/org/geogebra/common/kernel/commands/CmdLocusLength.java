/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geogebra.common.kernel.commands;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.MyError;

/** Public rich metric processor for Locus V2. */
public final class CmdLocusLength extends CommandProcessor {

	/**
	 * @param kernel kernel
	 */
	public CmdLocusLength(Kernel kernel) {
		super(kernel);
	}

	@Override
	public GeoElement[] process(Command command, EvalInfo info) throws MyError {
		RuntimeFeatureService.requireLocusV2Access(cons);
		int argumentCount = command.getArgumentNumber();
		GeoElement[] arguments = resArgs(command, info);
		boolean[] valid = new boolean[argumentCount];
		try {
			switch (argumentCount) {
			case 1:
				if (valid[0] = arguments[0] instanceof GeoLocusV2) {
					return new GeoElement[] {LocusV2PublicOperations.totalMetric(
							cons, command.getLabel(), (GeoLocusV2) arguments[0])};
				}
				break;
			case 3:
				if ((valid[0] = arguments[0] instanceof GeoLocusV2)
						&& (valid[1] = arguments[1] instanceof GeoPoint)
						&& (valid[2] = arguments[2] instanceof GeoPoint)) {
					return new GeoElement[] {LocusV2PublicOperations.betweenMetric(
							cons, command.getLabel(), (GeoLocusV2) arguments[0],
							(GeoPoint) arguments[1], (GeoPoint) arguments[2])};
				}
				break;
			default:
				throw argNumErr(command);
			}
		} catch (IllegalArgumentException exception) {
			throw MyError.forCommand(loc,
					loc.getMenu("LocusV2.InvalidPosition"), command.getName(),
					exception);
		}
		throw argErr(command, getBadArg(valid, arguments));
	}
}
