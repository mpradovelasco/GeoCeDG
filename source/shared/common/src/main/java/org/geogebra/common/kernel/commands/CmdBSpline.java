/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geogebra.common.kernel.commands;

import org.geocedg.common.kernel.bspline.BSplineDefinition2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.main.MyError;

/**
 * PRE-G9B-MAINT-A2: feature-gated public command of the control-point B-spline family
 * {@code bspline-2d/v1} ({@code bspline-2d.md} section 4.2). Only argument types are checked
 * here; every numeric precondition gives a created, undefined curve with the typed cause of
 * section 7, never an argument error and never a silent degree reduction.
 */
public final class CmdBSpline extends CommandProcessor {
	/**
	 * @param kernel kernel
	 */
	public CmdBSpline(Kernel kernel) {
		super(kernel);
	}

	@Override
	public GeoElement[] process(Command command, EvalInfo info) throws MyError {
		RuntimeFeatureService.requireLocusV2Access(cons);
		GeoElement[] arguments = resArgs(command, info);
		switch (arguments.length) {
		case 1:
			requirePointList(command, arguments[0]);
			return singleton(create(command, (GeoList) arguments[0],
					new GeoNumeric(cons, BSplineDefinition2D.DEFAULT_DEGREE), null));
		case 2:
			requirePointList(command, arguments[0]);
			requireNumber(command, arguments[1]);
			return singleton(create(command, (GeoList) arguments[0],
					(GeoNumberValue) arguments[1], null));
		case 3:
			requirePointList(command, arguments[0]);
			requireNumber(command, arguments[1]);
			requireNumberList(command, arguments[2]);
			return singleton(create(command, (GeoList) arguments[0],
					(GeoNumberValue) arguments[1], (GeoList) arguments[2]));
		default:
			throw argNumErr(command);
		}
	}

	private GeoLocusV2 create(Command command, GeoList points, GeoNumberValue degree,
			GeoList knots) {
		return LocusV2PublicOperations.createBSpline(cons, command.getLabel(), points, degree,
				knots);
	}

	private void requirePointList(Command command, GeoElement argument) {
		if (!(argument instanceof GeoList)) {
			throw argErr(command, argument);
		}
		GeoList list = (GeoList) argument;
		for (int index = 0; index < list.size(); index++) {
			GeoElement element = list.get(index);
			if (!element.isGeoPoint() || element.isGeoElement3D()) {
				throw argErr(command, argument);
			}
		}
	}

	private void requireNumber(Command command, GeoElement argument) {
		if (!(argument instanceof GeoNumberValue)) {
			throw argErr(command, argument);
		}
	}

	private void requireNumberList(Command command, GeoElement argument) {
		if (!(argument instanceof GeoList)) {
			throw argErr(command, argument);
		}
		GeoList list = (GeoList) argument;
		for (int index = 0; index < list.size(); index++) {
			if (!(list.get(index) instanceof GeoNumberValue)) {
				throw argErr(command, argument);
			}
		}
	}

	private static GeoElement[] singleton(GeoLocusV2 output) {
		return new GeoElement[] {output};
	}
}
