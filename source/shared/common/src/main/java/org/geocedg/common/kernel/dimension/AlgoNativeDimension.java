/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.dimension;

import java.util.Objects;

import org.geogebra.common.kernel.CircularDefinitionException;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.geos.GeoVec3D;
import org.geogebra.common.kernel.geos.LabelManager;
import org.geogebra.common.kernel.geos.SegmentStyle;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.util.debug.Log;

/**
 * Shared parent of the native dimension commands {@code AlignedDimension} and
 * {@code LinearDimension} (PRE-G9B-R6-plus-E2, representation beta). One algorithm owns
 * five outputs of existing types in a fixed contractual order:
 *
 * <ol start="0">
 * <li>{@link #VALUE}: the stable model-unit measure ({@code GeoNumeric});</li>
 * <li>{@link #DIMENSION_LINE}: the dimension line with arrow endings ({@code GeoSegment});</li>
 * <li>{@link #EXTENSION_A}: the extension line at A ({@code GeoSegment});</li>
 * <li>{@link #EXTENSION_B}: the extension line at B ({@code GeoSegment});</li>
 * <li>{@link #TEXT}: the presentation text ({@code GeoText}).</li>
 * </ol>
 *
 * <p>
 * {@link #compute()} is geometric only and never reads the unit state. The text string
 * is written by a separate presentation step that runs after it and on unit-state
 * changes (unit-system specification section 6, section 14; DQ-E2-3).
 * </p>
 */
public abstract class AlgoNativeDimension extends AlgoElement
		implements DimensionTextSource {

	/** Output index of the model-unit value. */
	public static final int VALUE = 0;
	/** Output index of the dimension line. */
	public static final int DIMENSION_LINE = 1;
	/** Output index of the extension line at A. */
	public static final int EXTENSION_A = 2;
	/** Output index of the extension line at B. */
	public static final int EXTENSION_B = 3;
	/** Output index of the presentation text. */
	public static final int TEXT = 4;
	/** Arrow ending of the dimension line at both ends. */
	public static final SegmentStyle ARROW_ENDING = SegmentStyle.ARROW_FILLED;
	/**
	 * Initial GeoGebra line thickness of the dimension line and both extension lines
	 * (PRE-G9B-R6-plus-E2 smoke follow-up B1); a presentation default like the arrow
	 * endings. Each segment then keeps and serializes its own style.
	 */
	public static final int INITIAL_LINE_THICKNESS = 2;

	private final GeoPointND pointA;
	private final GeoPointND pointB;
	private final GeoNumberValue offset;
	private final GeoNumberValue overshoot;
	private final GeoNumberValue gap;
	private final GeoNumeric value;
	private final GeoSegment dimensionLine;
	private final GeoSegment extensionA;
	private final GeoSegment extensionB;
	private final GeoText text;
	private final GeoPoint anchor;
	private DimensionFigure2D figure;

	/**
	 * @param cons construction
	 * @param pointA measured point A
	 * @param pointB measured point B
	 * @param offset signed offset (mandatory)
	 * @param overshoot overshoot, or null for the command default 0
	 * @param gap gap, or null for the command default 0
	 */
	protected AlgoNativeDimension(Construction cons, GeoPointND pointA, GeoPointND pointB,
			GeoNumberValue offset, GeoNumberValue overshoot, GeoNumberValue gap) {
		super(cons);
		this.pointA = Objects.requireNonNull(pointA);
		this.pointB = Objects.requireNonNull(pointB);
		this.offset = Objects.requireNonNull(offset);
		this.overshoot = overshoot;
		this.gap = gap;
		value = new GeoNumeric(cons);
		dimensionLine = newSegment(cons);
		extensionA = newSegment(cons);
		extensionB = newSegment(cons);
		text = new GeoText(cons);
		text.setIsTextCommand(true);
		anchor = new GeoPoint(cons);
		anchor.setCoords(0, 0, 1);
		try {
			text.setStartPoint(anchor);
		} catch (CircularDefinitionException e) {
			Log.debug("dimension text anchor: " + e.getMessage());
		}
		text.setAlwaysFixed(true);
		// as AlgoText: the drawable re-measures the box after centring (Corner, layout)
		text.setNeedsUpdatedBoundingBox(true);
		text.setHorizontalAlignment(0);
		text.setVerticalAlignment(0);
		dimensionLine.setStartStyle(ARROW_ENDING);
		dimensionLine.setEndStyle(ARROW_ENDING);
	}

	private static GeoSegment newSegment(Construction cons) {
		GeoSegment segment = new GeoSegment(cons);
		GeoPoint start = new GeoPoint(cons);
		start.setCoords(0, 0, 1);
		GeoPoint end = new GeoPoint(cons);
		end.setCoords(0, 0, 1);
		segment.setPoints(start, end);
		return segment;
	}

	/**
	 * Completes construction; subclasses call it once their own inputs are set. A
	 * single label names the value and the other outputs take default labels; several
	 * labels name the outputs in order.
	 *
	 * @param labels output labels, possibly null
	 */
	protected final void initialize(String[] labels) {
		setInputOutput();
		compute();
		refreshPresentation(false);
		DimensionPresentation.ensureRefresh(cons);
		String[] names = labels;
		if (labels != null && labels.length == 1 && labels[0] != null
				&& !labels[0].isEmpty()) {
			names = new String[getOutputLength()];
			names[VALUE] = labels[0];
		}
		LabelManager.setLabels(names, getOutput());
		for (GeoSegment segment : new GeoSegment[] {dimensionLine, extensionA,
				extensionB}) {
			segment.setLabelVisible(false);
			segment.setLineThickness(INITIAL_LINE_THICKNESS);
		}
	}

	@Override
	protected final void setInputOutput() {
		input = commandInputs();
		setOutputLength(5);
		setOutput(VALUE, value);
		setOutput(DIMENSION_LINE, dimensionLine);
		setOutput(EXTENSION_A, extensionA);
		setOutput(EXTENSION_B, extensionB);
		setOutput(TEXT, text);
		setDependencies();
	}

	/**
	 * @return the exact command inputs in command order
	 */
	protected abstract GeoElement[] commandInputs();

	/**
	 * @param ax A x
	 * @param ay A y
	 * @param bx B x
	 * @param by B y
	 * @param s offset
	 * @param o overshoot
	 * @param g gap
	 * @return the model-space figure
	 */
	protected abstract DimensionFigure2D figure(double ax, double ay, double bx,
			double by, double s, double o, double g);

	/**
	 * Geometric computation only: reads the geometric inputs, never the unit state.
	 */
	@Override
	public final void compute() {
		double ax = Double.NaN;
		double ay = Double.NaN;
		double bx = Double.NaN;
		double by = Double.NaN;
		if (isFinitePlanePoint(pointA) && isFinitePlanePoint(pointB)) {
			ax = pointA.getInhomX();
			ay = pointA.getInhomY();
			bx = pointB.getInhomX();
			by = pointB.getInhomY();
		}
		figure = figure(ax, ay, bx, by, numberOf(offset, Double.NaN),
				numberOf(overshoot, 0), numberOf(gap, 0));
		if (Double.isFinite(figure.value)) {
			value.setValue(figure.value);
		} else {
			value.setUndefined();
		}
		place(dimensionLine, figure.line);
		place(extensionA, figure.extensionA);
		place(extensionB, figure.extensionB);
		if (figure.frame == null) {
			anchor.setUndefined();
		} else {
			anchor.setCoords(figure.frame[0], figure.frame[1], 1);
		}
	}

	private static boolean isFinitePlanePoint(GeoPointND point) {
		return point.isDefined() && !point.isGeoElement3D() && point.isFinite();
	}

	private static double numberOf(GeoNumberValue number, double missing) {
		if (number == null) {
			return missing;
		}
		return number.toGeoElement().isDefined() ? number.getDouble() : Double.NaN;
	}

	private static void place(GeoSegment segment, double[] coords) {
		if (coords == null) {
			segment.setUndefined();
			return;
		}
		GeoPoint start = segment.getStartPoint();
		GeoPoint end = segment.getEndPoint();
		start.setCoords(coords[0], coords[1], 1);
		end.setCoords(coords[2], coords[3], 1);
		GeoVec3D.lineThroughPoints(start, end, segment);
		segment.calcLength();
	}

	/**
	 * Presentation step: rewrites only the text string from the computed value and the
	 * current document unit state. It never changes geometry or the value.
	 *
	 * @param notify whether to update and repaint the text and its dependents
	 * @return whether the string or its definedness changed
	 */
	final boolean refreshPresentation(boolean notify) {
		String next = null;
		if (figure != null && figure.frame != null && Double.isFinite(figure.value)) {
			next = DimensionPresentation.format(figure.value,
					cons.getUnitSystem().getState(), kernel, text.getStringTemplate());
		}
		String current = text.isDefined() ? text.getTextString() : null;
		if (Objects.equals(next, current)) {
			return false;
		}
		if (next == null) {
			text.setUndefined();
		} else {
			text.setTextString(next);
		}
		if (notify) {
			text.updateRepaint();
		}
		return true;
	}

	@Override
	protected void updateDependentGeos() {
		refreshPresentation(false);
		super.updateDependentGeos();
	}

	@Override
	public boolean isDimensionPresentationText(GeoText candidate) {
		return candidate == text;
	}

	@Override
	public double[] getDimensionTextFrame() {
		return figure == null || figure.frame == null || !text.isDefined() ? null
				: figure.frame.clone();
	}

	@Override
	public double[] getDimensionLineEndpoints() {
		return figure == null || figure.line == null ? null : figure.line.clone();
	}

	@Override
	public double[] getExtensionTips() {
		double[] tips = {Double.NaN, Double.NaN, Double.NaN, Double.NaN};
		if (figure != null && figure.extensionA != null) {
			tips[0] = figure.extensionA[2];
			tips[1] = figure.extensionA[3];
		}
		if (figure != null && figure.extensionB != null) {
			tips[2] = figure.extensionB[2];
			tips[3] = figure.extensionB[3];
		}
		return tips;
	}

	@Override
	public double[] getOvershootDirection() {
		double s = numberOf(offset, Double.NaN);
		if (figure == null || figure.normal == null || !(s != 0) || !Double.isFinite(s)) {
			return null;
		}
		double sign = Math.signum(s);
		return new double[] {sign * figure.normal[0], sign * figure.normal[1]};
	}

	/**
	 * Desktop drag support: the offset that moves the dimension line through a model
	 * point, keeping A, B and the direction unchanged.
	 *
	 * @param x model x
	 * @param y model y
	 * @return the offset, or NaN when the dimension frame is undefined
	 */
	public final double offsetThrough(double x, double y) {
		if (figure == null || figure.normal == null || !isFinitePlanePoint(pointA)) {
			return Double.NaN;
		}
		return DimensionFigure2D.offsetThrough(pointA.getInhomX(), pointA.getInhomY(),
				figure.normal[0], figure.normal[1], x, y);
	}

	/**
	 * @return the offset input (the only parameter a drag may edit)
	 */
	public final GeoNumberValue getOffsetInput() {
		return offset;
	}

	/**
	 * @return the overshoot input, or null when omitted
	 */
	public final GeoNumberValue getOvershootInput() {
		return overshoot;
	}

	/**
	 * @return the gap input, or null when omitted
	 */
	public final GeoNumberValue getGapInput() {
		return gap;
	}

	/**
	 * @return measured point A
	 */
	public final GeoPointND getPointA() {
		return pointA;
	}

	/**
	 * @return measured point B
	 */
	public final GeoPointND getPointB() {
		return pointB;
	}

	/**
	 * @return the model-unit value output
	 */
	public final GeoNumeric getValue() {
		return value;
	}

	/**
	 * @return the dimension-line output
	 */
	public final GeoSegment getDimensionLine() {
		return dimensionLine;
	}

	/**
	 * @return the extension line at A
	 */
	public final GeoSegment getExtensionA() {
		return extensionA;
	}

	/**
	 * @return the extension line at B
	 */
	public final GeoSegment getExtensionB() {
		return extensionB;
	}

	/**
	 * @return the presentation text output
	 */
	public final GeoText getPresentationText() {
		return text;
	}

	/**
	 * @return the current unit normal of the measurement direction, or null
	 */
	public final double[] getMeasurementNormal() {
		return figure == null || figure.normal == null ? null : figure.normal.clone();
	}

	/**
	 * @param geo a geo
	 * @return the native dimension that owns the geo as one of its outputs, or null
	 */
	public static AlgoNativeDimension ownerOf(GeoElement geo) {
		if (geo != null && geo.getParentAlgorithm() instanceof AlgoNativeDimension) {
			return (AlgoNativeDimension) geo.getParentAlgorithm();
		}
		return null;
	}
}
