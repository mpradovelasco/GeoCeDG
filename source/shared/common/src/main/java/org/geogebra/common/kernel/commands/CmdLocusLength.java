/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geogebra.common.kernel.commands;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.main.MyError;

/** Numeric public Locus V2 metric and reconstruction of historical rich results. */
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
				if (cons.isFileLoading() && arguments[0] instanceof GeoLocusMetricResult rich) {
					return new GeoElement[] {LocusV2PublicOperations.scalarFromMetric(
							cons, command.getLabel(), rich, Commands.LocusLength)};
				}
				if (valid[0] = arguments[0] instanceof GeoLocusV2) {
					GeoLocusV2 locus = (GeoLocusV2) arguments[0];
					return new GeoElement[] {cons.isFileLoading()
							? LocusV2PublicOperations.totalMetric(cons, command.getLabel(), locus)
							: LocusV2PublicOperations.scalarLength(cons, command.getLabel(),
									locus, Commands.LocusLength)};
				}
				break;
			case 3:
				if ((valid[0] = arguments[0] instanceof GeoLocusV2)
						&& (valid[1] = arguments[1] instanceof GeoPoint)
						&& (valid[2] = arguments[2] instanceof GeoPoint)) {
					return new GeoElement[] {between(command, arguments, null, null, null)};
				}
				break;
			case 6:
				if ((valid[0] = arguments[0] instanceof GeoLocusV2)
						&& (valid[1] = arguments[1] instanceof GeoPoint)
						&& (valid[2] = arguments[2] instanceof GeoPoint)
						&& (valid[3] = arguments[3] instanceof GeoText)
						&& (valid[4] = arguments[4] instanceof GeoText)
						&& (valid[5] = arguments[5] instanceof GeoText)) {
					return new GeoElement[] {between(command, arguments, (GeoText) arguments[3],
							(GeoText) arguments[4], (GeoText) arguments[5])};
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

	private GeoElement between(Command command, GeoElement[] arguments,
			GeoText direction, GeoText boundary, GeoText samePosition) {
		GeoLocusV2 locus = (GeoLocusV2) arguments[0];
		GeoPoint start = (GeoPoint) arguments[1];
		GeoPoint end = (GeoPoint) arguments[2];
		return cons.isFileLoading()
				? LocusV2PublicOperations.betweenMetric(cons, command.getLabel(),
						locus, start, end, direction, boundary, samePosition)
				: LocusV2PublicOperations.scalarBetweenLength(cons, command.getLabel(),
						locus, start, end, direction, boundary, samePosition, Commands.LocusLength);
	}
}
