/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.desktop.export.DrawingScale;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.MyError;
import org.geogebra.desktop.main.AppD;

/**
 * Desktop orchestration of the native dimension tools (PRE-G9B-R6-plus-E2). It only
 * captures initial parameters once, at creation, and issues the ordinary command; the
 * geometry is the kernel's. After creation nothing here is read again.
 */
final class GeoCeDGDimensionTools {

	/** Paper overshoot target with a physical unit, mm (DQ-E2-11). */
	static final double PAPER_OVERSHOOT_MM = 2;
	/** Paper gap target with a physical unit, mm (DQ-E2-11). */
	static final double PAPER_GAP_MM = 1;
	/** View fallback overshoot without a physical unit, px (creation-time UI input). */
	static final double VIEW_OVERSHOOT_PX = 8;
	/** View fallback gap without a physical unit, px (creation-time UI input). */
	static final double VIEW_GAP_PX = 4;

	private final AppD app;
	private MyError lastFailure;

	GeoCeDGDimensionTools(AppD app) {
		this.app = app;
	}

	/**
	 * @param mode tool mode
	 * @return whether the mode is a native dimension tool
	 */
	static boolean handles(int mode) {
		return mode == EuclidianConstants.MODE_ALIGNED_DIMENSION
				|| mode == EuclidianConstants.MODE_LINEAR_DIMENSION;
	}

	/**
	 * Creation-time overshoot and gap in model units: 2 mm and 1 mm on paper through
	 * the effective construction unit and the session drawing scale when the unit is
	 * physical; otherwise 8 px and 4 px through the view's current scale. Both are
	 * materialized at once as explicit inputs and never re-read.
	 *
	 * @param state document unit state
	 * @param scale session drawing scale
	 * @param xScale view pixels per model unit along x
	 * @param yScale view pixels per model unit along y
	 * @return {overshoot, gap} in model units
	 */
	static double[] captureMagnitudes(UnitState state, DrawingScale scale, double xScale,
			double yScale) {
		if (state.isPhysical()) {
			double paperMetresPerModelUnit = state.effectiveConstructionMetresPerUnit()
					* scale.getNumerator() / scale.getDenominator();
			return new double[] {PAPER_OVERSHOOT_MM * 1E-3 / paperMetresPerModelUnit,
					PAPER_GAP_MM * 1E-3 / paperMetresPerModelUnit};
		}
		double pixelsPerUnit = Math.sqrt(xScale * yScale);
		return new double[] {VIEW_OVERSHOOT_PX / pixelsPerUnit,
				VIEW_GAP_PX / pixelsPerUnit};
	}

	/**
	 * Creates one dimension: explicit offset from the placement point, captured
	 * overshoot and gap, and the ordinary command.
	 *
	 * @param mode tool mode
	 * @param pointA measured point A
	 * @param pointB measured point B
	 * @param direction line or vector for the linear tool, else null
	 * @param x placement x (model)
	 * @param y placement y (model)
	 * @param view the view of the click
	 * @return outputs, or null when nothing was created
	 */
	GeoElement[] create(int mode, GeoPointND pointA, GeoPointND pointB,
			GeoElement direction, double x, double y, EuclidianView view) {
		lastFailure = null;
		Kernel kernel = app.getKernel();
		Construction cons = kernel.getConstruction();
		DrawingScale scale = app instanceof AppGeoCeDG
				? ((AppGeoCeDG) app).getDrawingScale() : DrawingScale.ONE_TO_ONE;
		double[] magnitudes = captureMagnitudes(cons.getUnitSystem().getState(), scale,
				view.getXscale(), view.getYscale());
		GeoNumeric offset = parameter(cons, 0);
		GeoNumeric overshoot = parameter(cons, magnitudes[0]);
		GeoNumeric gap = parameter(cons, magnitudes[1]);
		boolean linear = mode == EuclidianConstants.MODE_LINEAR_DIMENSION;
		Command command = new Command(kernel, linear ? Commands.LinearDimension.name()
				: Commands.AlignedDimension.name(), false);
		command.addArgument(pointA.toGeoElement().wrap());
		command.addArgument(pointB.toGeoElement().wrap());
		if (linear) {
			command.addArgument(direction.wrap());
		}
		command.addArgument(offset.wrap());
		command.addArgument(overshoot.wrap());
		command.addArgument(gap.wrap());
		GeoElement[] created = null;
		try {
			created = kernel.getAlgebraProcessor().processCommand(command,
					new EvalInfo(true));
		} catch (MyError error) {
			lastFailure = error;
		}
		AlgoNativeDimension algo = created == null || created.length == 0 ? null
				: AlgoNativeDimension.ownerOf(created[0]);
		double placed = algo == null ? Double.NaN : algo.offsetThrough(x, y);
		if (!Double.isFinite(placed)) {
			if (algo != null) {
				algo.remove();
			}
			for (GeoNumeric number : new GeoNumeric[] {offset, overshoot, gap}) {
				number.remove();
			}
			if (lastFailure == null) {
				lastFailure = new MyError(app.getLocalization(), app.getLocalization()
						.getMenu(linear ? "LinearDimension.InvalidPlacement"
								: "AlignedDimension.InvalidPlacement"));
			}
			if (app.isErrorDialogsActive()) {
				app.showError(lastFailure);
			}
			return null;
		}
		offset.setValue(placed);
		offset.updateCascade();
		return created;
	}

	private static GeoNumeric parameter(Construction cons, double value) {
		GeoNumeric number = new GeoNumeric(cons, value);
		number.setAuxiliaryObject(true);
		number.setEuclidianVisible(false);
		number.setLabel(null);
		return number;
	}

	/**
	 * @return the failure of the last creation, or null
	 */
	MyError getLastFailure() {
		return lastFailure;
	}
}
