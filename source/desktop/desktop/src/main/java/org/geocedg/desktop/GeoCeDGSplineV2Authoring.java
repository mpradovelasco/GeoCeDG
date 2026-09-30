/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.arithmetic.MyList;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.MyError;
import org.geogebra.desktop.main.AppD;

/**
 * PRE-G9B-R4: turns one explicit ordered point selection into one ordinary
 * {@code SplineV2} command. It passes the selected points by reference, never
 * by label, and adds no spline, degree or closedness rule of its own; the
 * command and kernel remain the only authority. The closed tool alone appends
 * the first point object again, the synthesis {@code AD-R0-8} permits.
 */
final class GeoCeDGSplineV2Authoring {

	/** The command's documented point minimum; only the finish gesture uses it. */
	static final int MINIMUM_POINTS = 3;
	/** Initial text of the explicit-degree dialog: the command default. */
	static final String DEFAULT_DEGREE_TEXT = "3";

	/** Supplies the explicitly entered degree; a cancelled entry supplies nothing. */
	interface DegreeInput {
		/**
		 * @param app application
		 * @param mode active SplineV2 mode, for the dialog title
		 * @param accept receives the entered number when the user confirms
		 */
		void request(AppD app, int mode, Consumer<GeoNumberValue> accept);
	}

	private final AppD app;
	private DegreeInput degreeInput = GeoCeDGSplineV2Authoring::hostDegreeDialog;
	private MyError lastFailure;

	GeoCeDGSplineV2Authoring(AppD app) {
		this.app = app;
	}

	/**
	 * @param mode Euclidian mode
	 * @return whether the mode is one of the three SplineV2 authoring tools
	 */
	static boolean handles(int mode) {
		return mode == EuclidianConstants.MODE_SPLINE_V2
				|| mode == EuclidianConstants.MODE_SPLINE_V2_DEGREE
				|| mode == EuclidianConstants.MODE_SPLINE_V2_CLOSED;
	}

	/**
	 * Issues the single construction of the finished tool.
	 * @param mode active SplineV2 mode
	 * @param ordered points in the user's click order
	 * @return the created construction, or null when nothing was created
	 */
	GeoElement[] commit(int mode, GeoPointND[] ordered) {
		lastFailure = null;
		Kernel kernel = app.getKernel();
		Command command = new Command(kernel, Commands.SplineV2.name(), false);
		if (mode == EuclidianConstants.MODE_SPLINE_V2_DEGREE) {
			GeoNumberValue degree = requestDegree(mode);
			if (degree == null) {
				return null;
			}
			MyList points = new MyList(kernel);
			for (GeoPointND point : ordered) {
				points.addListElement(point.toGeoElement().wrap());
			}
			command.addArgument(points.wrap());
			command.addArgument(degree.toGeoElement().wrap());
		} else {
			for (GeoPointND point : ordered) {
				command.addArgument(point.toGeoElement().wrap());
			}
			if (mode == EuclidianConstants.MODE_SPLINE_V2_CLOSED) {
				command.addArgument(ordered[0].toGeoElement().wrap());
			}
		}
		try {
			GeoElement[] created = kernel.getAlgebraProcessor()
					.processCommand(command, new EvalInfo(true));
			return created == null || created.length == 0 ? null : created;
		} catch (MyError error) {
			lastFailure = error;
			if (app.isErrorDialogsActive()) {
				app.showError(error);
			}
			return null;
		}
	}

	private GeoNumberValue requestDegree(int mode) {
		AtomicReference<GeoNumberValue> entered = new AtomicReference<>();
		degreeInput.request(app, mode, entered::set);
		return entered.get();
	}

	/** The host number dialog is modal on Desktop, so it answers before returning. */
	private static void hostDegreeDialog(AppD app, int mode,
			Consumer<GeoNumberValue> accept) {
		app.getDialogManager().showNumberInputDialog(app.getToolName(mode),
				app.getLocalization().getMenu("SplineV2.DegreePrompt"),
				DEFAULT_DEGREE_TEXT, accept::accept);
	}

	void setDegreeInput(DegreeInput input) {
		degreeInput = input;
	}

	/** @return the command error of the last commit, or null */
	MyError getLastFailure() {
		return lastFailure;
	}
}
