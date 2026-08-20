/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geogebra.common.kernel.commands;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2DomainDescriptor;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.MyError;

/** Reconstructible public processor for the experimental semantic locus. */
public final class CmdLocusV2 extends CommandProcessor {

	/**
	 * @param kernel kernel
	 */
	public CmdLocusV2(Kernel kernel) {
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
			case 2:
				if ((valid[0] = arguments[0] instanceof GeoPoint)
						&& (valid[1] = arguments[1] instanceof GeoPoint)) {
					return singleton(LocusV2PublicOperations.createPointDriven(
							cons, command.getLabel(), (GeoPoint) arguments[0],
							(GeoPoint) arguments[1]));
				}
				break;
			case 3:
				if ((valid[0] = arguments[0] instanceof GeoPoint)
						&& (valid[1] = arguments[1] instanceof GeoNumeric)
						&& (valid[2] = arguments[2] instanceof GeoList)) {
					GeoNumeric coordinate = (GeoNumeric) arguments[1];
					return singleton(LocusV2PublicOperations.createScalar(cons,
							command.getLabel(), (GeoPoint) arguments[0], coordinate,
							coordinate, parseDomain((GeoList) arguments[2], command)));
				}
				break;
			case 4:
				if ((valid[0] = arguments[0] instanceof GeoPoint)
						&& (valid[1] = arguments[1] instanceof GeoNumeric)
						&& (valid[2] = arguments[2] instanceof GeoNumeric)
						&& (valid[3] = arguments[3] instanceof GeoList)) {
					return singleton(LocusV2PublicOperations.createScalar(cons,
							command.getLabel(), (GeoPoint) arguments[0],
							(GeoNumeric) arguments[1], (GeoNumeric) arguments[2],
							parseDomain((GeoList) arguments[3], command)));
				}
				break;
			default:
				throw argNumErr(command);
			}
		} catch (IllegalArgumentException exception) {
			throw commandError(command, "LocusV2.UnsupportedGenerator",
					exception);
		}
		throw argErr(command, getBadArg(valid, arguments));
	}

	private LocusV2DomainDescriptor parseDomain(GeoList source,
			Command command) throws MyError {
		try {
			return LocusV2DomainDescriptor.parse(source);
		} catch (IllegalArgumentException exception) {
			throw commandError(command, "LocusV2.InvalidDomain", exception);
		}
	}

	private MyError commandError(Command command, String localizationKey,
			IllegalArgumentException cause) {
		return MyError.forCommand(loc, loc.getMenu(localizationKey),
				command.getName(), cause);
	}

	private static GeoElement[] singleton(GeoLocusV2 locus) {
		return new GeoElement[] {locus};
	}
}
