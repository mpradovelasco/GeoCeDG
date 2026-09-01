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
import org.geogebra.common.kernel.geos.GeoFunctionNVar;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.main.MyError;
import org.geogebra.common.plugin.GeoClass;

/** Feature-gated public command for a reconstructible semantic 2D spline. */
public final class CmdSplineV2 extends CommandProcessor {
	/** Creates the command processor. */
	public CmdSplineV2(Kernel kernel) {
		super(kernel);
	}

	@Override
	public GeoElement[] process(Command command, EvalInfo info) throws MyError {
		RuntimeFeatureService.requireLocusV2Access(cons);
		GeoElement[] arguments = resArgs(command, info);
		switch (arguments.length) {
		case 1:
			if (arguments[0] instanceof GeoList
					&& areFiniteTwoDimensionalPoints((GeoList) arguments[0])) {
				return singleton(create(command, (GeoList) arguments[0],
						new GeoNumeric(cons, 3), null));
			}
			throw argErr(command, arguments[0]);
		case 2:
			if (arguments[0] instanceof GeoList
					&& areFiniteTwoDimensionalPoints((GeoList) arguments[0])
					&& arguments[1] instanceof GeoNumberValue
					&& validDegree((GeoNumberValue) arguments[1],
							((GeoList) arguments[0]).size())) {
				return singleton(create(command, (GeoList) arguments[0],
						(GeoNumberValue) arguments[1], null));
			}
			throw argErr(command, invalidArgument(arguments));
		case 3: {
			if (arguments[0] instanceof GeoList
					&& areFiniteTwoDimensionalPoints((GeoList) arguments[0])
					&& arguments[1] instanceof GeoNumberValue
					&& validDegree((GeoNumberValue) arguments[1],
							((GeoList) arguments[0]).size())
					&& arguments[2] instanceof GeoFunctionNVar) {
				return singleton(create(command, (GeoList) arguments[0],
						(GeoNumberValue) arguments[1],
						(GeoFunctionNVar) arguments[2]));
			}
			GeoList pointList = wrapInList(kernel, arguments, arguments.length,
					GeoClass.POINT);
			if (pointList != null && areFiniteTwoDimensionalPoints(pointList)) {
				return singleton(create(command, pointList, new GeoNumeric(cons, 3),
						null));
			}
			throw argErr(command, invalidArgument(arguments));
		}
		default:
			GeoList list = wrapInList(kernel, arguments, arguments.length,
					GeoClass.POINT);
			if (list != null && areFiniteTwoDimensionalPoints(list)) {
				return singleton(create(command, list, new GeoNumeric(cons, 3),
						null));
			}
			throw argNumErr(command);
		}
	}

	private GeoLocusV2 create(Command command, GeoList points,
			GeoNumberValue degree, GeoFunctionNVar weight) {
		try {
			return LocusV2PublicOperations.createSpline(cons, command.getLabel(),
					points, degree, weight);
		} catch (IllegalArgumentException exception) {
			throw MyError.forCommand(loc,
					loc.getMenu("SplineV2.InvalidDefinition"), command.getName(),
					exception);
		}
	}

	private static boolean validDegree(GeoNumberValue degree, int pointCount) {
		double value = degree.getDouble();
		return Double.isFinite(value) && value == Math.rint(value) && value >= 3
				&& value <= pointCount
				&& org.geocedg.common.kernel.spline.SplinePolynomialModel2D
						.isWithinWorkPolicy(pointCount, (int) value);
	}

	private static boolean areFiniteTwoDimensionalPoints(GeoList list) {
		if (list.size() < 3) {
			return false;
		}
		for (int index = 0; index < list.size(); index++) {
			GeoElement element = list.get(index);
			if (!element.isGeoPoint() || element.isGeoElement3D()
					|| !element.isDefined()) {
				return false;
			}
		}
		return true;
	}

	private static GeoElement invalidArgument(GeoElement[] arguments) {
		for (GeoElement argument : arguments) {
			if (argument == null || !argument.isDefined()) {
				return argument;
			}
		}
		return arguments[arguments.length - 1];
	}

	private static GeoElement[] singleton(GeoLocusV2 output) {
		return new GeoElement[] {output};
	}
}
