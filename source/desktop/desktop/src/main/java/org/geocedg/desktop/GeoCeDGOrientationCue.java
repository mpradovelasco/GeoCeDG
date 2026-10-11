/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geogebra.common.euclidian.Drawable;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.algos.AlgoAngularBisectorLines;
import org.geogebra.common.kernel.algos.AlgoDilate;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoJoinPoints;
import org.geogebra.common.kernel.algos.AlgoJoinPointsRay;
import org.geogebra.common.kernel.algos.AlgoJoinPointsSegment;
import org.geogebra.common.kernel.algos.AlgoLineBisector;
import org.geogebra.common.kernel.algos.AlgoLineBisectorSegment;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoLinePointVector;
import org.geogebra.common.kernel.algos.AlgoMacro;
import org.geogebra.common.kernel.algos.AlgoMirror;
import org.geogebra.common.kernel.algos.AlgoOrthoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoOrthoLinePointVector;
import org.geogebra.common.kernel.algos.AlgoRayPointVector;
import org.geogebra.common.kernel.algos.AlgoRotate;
import org.geogebra.common.kernel.algos.AlgoRotatePoint;
import org.geogebra.common.kernel.algos.AlgoTangentLine;
import org.geogebra.common.kernel.algos.AlgoTranslate;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoVector;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.util.DoubleUtil;

/**
 * PRE-G9B-R6-plus-F1 read-only orientation-cue model (specification
 * {@code geocedg/specs/presentation/orientation-cue.md}). It decides, for an object
 * drawn in a view, whether its kernel orientation {@link GeoLine#getDirection} may be
 * shown, and where the arrowhead sits on screen. The sense always comes from the
 * kernel; the view transform only places and rotates the arrowhead. Nothing here
 * creates, changes or retains any kernel, view or document state.
 */
final class GeoCeDGOrientationCue {
	/** Axial length of the arrowhead in device pixels (OTQ-F1-3). */
	static final double LENGTH = 10;
	/** Half of the 4-pixel base width of the arrowhead (OTQ-F1-3). */
	static final double HALF_WIDTH = 2;
	/** Shortest visible chord, on screen, that may carry a cue. */
	static final double MIN_CHORD = 4 * LENGTH;
	/** Distance kept between the visible chord and the view border. */
	static final double INSET = LENGTH;
	private static final int MAX_DEPTH = 512;

	/** Construction-route classes admitted by the eligibility matrix v1 (A1-A10). */
	private static final Set<Class<?>> POINT_ROUTES = Set.of(AlgoJoinPoints.class,
			AlgoJoinPointsSegment.class, AlgoJoinPointsRay.class, AlgoLineBisector.class);
	private static final Set<Class<?>> POINT_VECTOR_ROUTES = Set.of(
			AlgoRayPointVector.class, AlgoLinePointVector.class,
			AlgoOrthoLinePointVector.class);
	private static final Set<Class<?>> POINT_LINE_ROUTES = Set.of(AlgoLinePointLine.class,
			AlgoOrthoLinePointLine.class);
	/** Source-proven affine images (A14-A16; A13 is checked separately). */
	private static final Set<Class<?>> IMAGE_ROUTES = Set.of(AlgoRotate.class,
			AlgoRotatePoint.class, AlgoTranslate.class, AlgoDilate.class);

	/** One displayable cue: kernel sense in world units, anchor and unit in screen. */
	record Cue(GeoLine geo, double worldDx, double worldDy, double anchorX,
			double anchorY, double unitX, double unitY) {

		/** @return tip, then the two base corners, as x0, y0, x1, y1, x2, y2 */
		double[] triangle() {
			double tipX = anchorX + unitX * (2 * LENGTH / 3);
			double tipY = anchorY + unitY * (2 * LENGTH / 3);
			double baseX = anchorX - unitX * (LENGTH / 3);
			double baseY = anchorY - unitY * (LENGTH / 3);
			double normalX = -unitY * HALF_WIDTH;
			double normalY = unitX * HALF_WIDTH;
			return new double[] {tipX, tipY, baseX + normalX, baseY + normalY,
					baseX - normalX, baseY - normalY};
		}
	}

	/** Macro-input mapping from a macro construction to the construction that uses it. */
	private record Scope(Map<GeoElement, GeoElement> inputs, Scope outer) {
	}

	private GeoCeDGOrientationCue() {
	}

	/**
	 * @param view view whose drawables are inspected
	 * @return the cues of every eligible drawn object, in drawing order
	 */
	static List<Cue> cues(EuclidianView view) {
		if (view.getXaxisLog() || view.getYaxisLog()) {
			return Collections.emptyList();
		}
		List<Cue> cues = new ArrayList<>();
		for (Drawable drawable : new ArrayList<>(view.getAllDrawableList())) {
			Cue cue = cue(view, drawable.getGeoElement());
			if (cue != null) {
				cues.add(cue);
			}
		}
		return cues;
	}

	/**
	 * @param view view
	 * @param geo object
	 * @return the cue of the object in the view, or null when none may be shown
	 */
	static Cue cue(EuclidianView view, GeoElement geo) {
		if (!isCueType(geo) || view.getDrawableFor(geo) == null
				|| view.getXaxisLog() || view.getYaxisLog()) {
			return null;
		}
		GeoLine line = (GeoLine) geo;
		if (!line.isDefined() || !line.isEuclidianVisible()
				|| !line.isVisibleInView(view.getViewID())
				|| !view.getApplication().isLayerShown(line.getLayer())
				|| line.getLineThickness() <= 0 || hasMidpointArrow(line)
				|| !isAdmitted(line)) {
			return null;
		}
		double[] direction = new double[2];
		line.getDirection(direction);
		double dx = direction[0];
		double dy = direction[1];
		if (!Double.isFinite(line.getX()) || !Double.isFinite(line.getY())
				|| !Double.isFinite(line.getZ()) || !Double.isFinite(dx)
				|| !Double.isFinite(dy) || DoubleUtil.isZero(Math.hypot(dx, dy))) {
			return null;
		}
		double[] base = pathOrigin(line);
		if (base == null) {
			return null;
		}
		double originX = view.toScreenCoordXd(base[0]);
		double originY = view.toScreenCoordYd(base[1]);
		double stepX = dx * view.getXscale();
		double stepY = -dy * view.getYscale();
		double step = Math.hypot(stepX, stepY);
		if (!Double.isFinite(originX) || !Double.isFinite(originY) || !Double.isFinite(step)
				|| !(step > 0)) {
			return null;
		}
		double[] range = {line instanceof GeoRay || line instanceof GeoSegment ? 0
				: Double.NEGATIVE_INFINITY,
				line instanceof GeoSegment ? 1 : Double.POSITIVE_INFINITY};
		double minX = INSET;
		double minY = INSET;
		double maxX = view.getWidth() - INSET;
		double maxY = view.getHeight() - INSET;
		if (!(maxX > minX && maxY > minY)
				|| !clip(originX, originY, stepX, stepY, minX, minY, maxX, maxY, range)
				|| (range[1] - range[0]) * step < MIN_CHORD) {
			return null;
		}
		double t = anchorParameter(line, range, step, originX, originY, stepX, stepY,
				view.getWidth() / 2.0, view.getHeight() / 2.0);
		return new Cue(line, dx, dy, originX + t * stepX, originY + t * stepY,
				stepX / step, stepY / step);
	}

	/** @return whether the object is a 2D line, segment or ray (not an axis) */
	static boolean isCueType(GeoElementND geo) {
		return geo != null && (geo.getClass() == GeoLine.class
				|| geo.getClass() == GeoSegment.class || geo.getClass() == GeoRay.class);
	}

	private static boolean hasMidpointArrow(GeoLine line) {
		int decoration = line.getDecorationType();
		return line instanceof GeoSegment
				&& (decoration == GeoElementND.DECORATION_SEGMENT_ONE_ARROW
				|| decoration == GeoElementND.DECORATION_SEGMENT_TWO_ARROWS
				|| decoration == GeoElementND.DECORATION_SEGMENT_THREE_ARROWS);
	}

	/**
	 * The point of parameter 0 of the kernel path: the start point of a segment or
	 * ray, the defining start point of a line when finite, otherwise the foot of the
	 * perpendicular from the origin, exactly as {@code GeoLine.pathChanged}.
	 */
	private static double[] pathOrigin(GeoLine line) {
		GeoPoint start = line.getStartPoint();
		if (start != null && start.isFinite()) {
			return new double[] {start.getInhomX(), start.getInhomY()};
		}
		if (line instanceof GeoSegment || line instanceof GeoRay) {
			return null;
		}
		double x = line.getX();
		double y = line.getY();
		double z = line.getZ();
		double norm = x * x + y * y;
		double[] origin = {-z * x / norm, -z * y / norm};
		return Double.isFinite(origin[0]) && Double.isFinite(origin[1]) ? origin : null;
	}

	/** Liang-Barsky clipping of origin + t * step to the rectangle, narrowing range. */
	private static boolean clip(double x, double y, double sx, double sy, double minX,
			double minY, double maxX, double maxY, double[] range) {
		double[] p = {-sx, sx, -sy, sy};
		double[] q = {x - minX, maxX - x, y - minY, maxY - y};
		for (int i = 0; i < 4; i++) {
			if (p[i] == 0) {
				if (q[i] < 0) {
					return false;
				}
				continue;
			}
			double r = q[i] / p[i];
			if (p[i] < 0) {
				range[0] = Math.max(range[0], r);
			} else {
				range[1] = Math.min(range[1], r);
			}
		}
		return Double.isFinite(range[0]) && Double.isFinite(range[1]) && range[0] <= range[1];
	}

	private static double anchorParameter(GeoLine line, double[] range, double step,
			double originX, double originY, double stepX, double stepY, double centerX,
			double centerY) {
		if (line instanceof GeoSegment) {
			return range[0] <= 0.5 && 0.5 <= range[1] ? 0.5 : (range[0] + range[1]) / 2;
		}
		if (line instanceof GeoRay) {
			double nearStart = 3 * LENGTH / step;
			if (range[0] <= nearStart && nearStart <= range[1]) {
				return nearStart;
			}
		}
		double nearest = ((centerX - originX) * stepX + (centerY - originY) * stepY)
				/ (step * step);
		return Math.min(range[1], Math.max(range[0], nearest));
	}

	// ----------------------------------------------- eligibility matrix v1

	/**
	 * Eligibility matrix v1 (design section 3.3): admitted only when the route is
	 * constructively oriented or demonstrably inherited and every orientation-bearing
	 * input is itself admitted. Classification uses only the classes of the object,
	 * its parent algorithm and its inputs, read through public kernel API; unknown
	 * routes fail closed.
	 *
	 * @param line line, segment or ray
	 * @return whether its kernel orientation may be shown
	 */
	static boolean isAdmitted(GeoLine line) {
		return isCueType(line) && admitted(line, null, 0);
	}

	private static boolean admitted(GeoElement geo, Scope scope, int depth) {
		if (depth > MAX_DEPTH) {
			return false;
		}
		Scope current = scope;
		GeoElement resolved = geo;
		while (current != null && current.inputs().containsKey(resolved)) {
			resolved = current.inputs().get(resolved);
			current = current.outer();
		}
		if (!isCueType(resolved)) {
			return false;
		}
		AlgoElement algo = resolved.getParentAlgorithm();
		if (algo == null) {
			return false;
		}
		Class<?> route = algo.getClass();
		GeoElement[] input = algo.getInput();
		if (POINT_ROUTES.contains(route)) {
			return input.length >= 2 && finitePoint(input[0], current)
					&& finitePoint(input[1], current);
		}
		if (POINT_VECTOR_ROUTES.contains(route)) {
			return input.length == 2 && finitePoint(input[0], current)
					&& resolve(input[1], current) instanceof GeoVector;
		}
		if (POINT_LINE_ROUTES.contains(route)) {
			return input.length == 2 && finitePoint(input[0], current)
					&& admitted(input[1], current, depth + 1);
		}
		if (route == AlgoLineBisectorSegment.class) {
			return input.length == 1 && admitted(input[0], current, depth + 1);
		}
		if (route == AlgoAngularBisectorLines.class) {
			return input.length == 2 && admitted(input[0], current, depth + 1)
					&& admitted(input[1], current, depth + 1);
		}
		if (route == AlgoTangentLine.class) {
			return input.length == 2 && admitted(input[0], current, depth + 1);
		}
		if (route == AlgoMirror.class) {
			GeoElement mirror = input.length == 2 ? resolve(input[1], current) : null;
			return (mirror instanceof GeoPoint || mirror instanceof GeoLine)
					&& admitted(input[0], current, depth + 1);
		}
		if (IMAGE_ROUTES.contains(route)) {
			return input.length >= 2 && admitted(input[0], current, depth + 1);
		}
		if (route == AlgoMacro.class) {
			return macroAdmitted(resolved, (AlgoMacro) algo, current, depth + 1);
		}
		return false;
	}

	/**
	 * OTQ-F1-1 conservative macro rule: the output is matched to its macro-construction
	 * object by its position in the macro definition, the macro inputs to the actual
	 * inputs likewise, and the inner route is classified with the same matrix.
	 */
	private static boolean macroAdmitted(GeoElement output, AlgoMacro algo, Scope scope,
			int depth) {
		Macro macro = algo.getMacro();
		GeoElement[] macroInput = macro == null ? null : macro.getMacroInput();
		GeoElement[] macroOutput = macro == null ? null : macro.getMacroOutput();
		GeoElement[] input = algo.getInput();
		if (macroInput == null || macroOutput == null || input == null
				|| macroInput.length != input.length
				|| macroOutput.length != algo.getOutputLength()) {
			return false;
		}
		int index = -1;
		for (int i = 0; i < algo.getOutputLength(); i++) {
			if (algo.getOutput(i) == output) {
				index = i;
			}
		}
		if (index < 0 || macroOutput[index] == null
				|| macroOutput[index].getClass() != output.getClass()) {
			return false;
		}
		Map<GeoElement, GeoElement> inputs = new IdentityHashMap<>();
		for (int i = 0; i < input.length; i++) {
			if (macroInput[i] == null || input[i] == null) {
				return false;
			}
			inputs.put(macroInput[i], input[i]);
		}
		return admitted(macroOutput[index], new Scope(inputs, scope), depth);
	}

	private static GeoElement resolve(GeoElement geo, Scope scope) {
		Scope current = scope;
		GeoElement resolved = geo;
		while (current != null && current.inputs().containsKey(resolved)) {
			resolved = current.inputs().get(resolved);
			current = current.outer();
		}
		return resolved;
	}

	private static boolean finitePoint(GeoElement geo, Scope scope) {
		GeoElement resolved = resolve(geo, scope);
		return resolved instanceof GeoPoint && ((GeoPoint) resolved).isFinite();
	}
}
