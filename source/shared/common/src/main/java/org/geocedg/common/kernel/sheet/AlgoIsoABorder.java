/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.sheet;

import java.util.Objects;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.CircularDefinitionException;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoPolyLine;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.geos.LabelManager;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.geogebra.common.util.debug.Log;

/**
 * Native {@code IsoABorder(<Point>, <ISO A index>, <Landscape>, <Scale numerator>,
 * <Scale denominator>, <Model units per millimetre>, <Inner frame>)}
 * (PRE-G9B-R6-plus-E3, representation R2). One algorithm owns three outputs of existing
 * types in a fixed contractual order:
 *
 * <ol start="0">
 * <li>{@link #PAPER}: the nominal ISO 216 paper boundary, a {@code GeoPolyLine} closed
 * by repeating its first vertex, hidden by default, the {@code ExportArea}
 * rectangle;</li>
 * <li>{@link #FRAME}: the optional inner drawing frame (margins 20/10/10/10 mm), a
 * closed {@code GeoPolyLine}; undefined while disabled or impossible;</li>
 * <li>{@link #LABEL}: the construction label {@code A<n>}, an em dash and {@code a:b}
 * ({@code GeoText}).</li>
 * </ol>
 *
 * <p>
 * {@link #compute()} reads only the seven inputs: never the unit state, the session
 * drawing scale, a view or the screen (unit-system section 6.1). Outputs are never
 * removed or replaced when inputs become invalid; they become undefined.
 * </p>
 */
public final class AlgoIsoABorder extends AlgoElement {

	/** Output index of the paper boundary. */
	public static final int PAPER = 0;
	/** Output index of the inner frame. */
	public static final int FRAME = 1;
	/** Output index of the construction label. */
	public static final int LABEL = 2;
	/** Initial line thickness of the paper boundary (shown only on request). */
	public static final int PAPER_LINE_THICKNESS = 2;
	/** Initial line thickness of the inner frame (E1 fallback style: black, 3). */
	public static final int FRAME_LINE_THICKNESS = 3;

	private final GeoPointND point;
	private final GeoNumberValue index;
	private final GeoBoolean landscape;
	private final GeoNumberValue numerator;
	private final GeoNumberValue denominator;
	private final GeoNumberValue unitFactor;
	private final GeoBoolean innerFrame;
	private final GeoPolyLine paper;
	private final GeoPolyLine frame;
	private final GeoText label;
	private final GeoPoint[] paperCorners;
	private final GeoPoint[] frameCorners;
	private final GeoPoint anchor;

	/**
	 * @param cons construction
	 * @param labels output labels, possibly null; a single label names the paper boundary
	 * @param point upper-left corner of the paper boundary
	 * @param index ISO A index
	 * @param landscape whether the width is the long side
	 * @param numerator captured scale numerator a
	 * @param denominator captured scale denominator b
	 * @param unitFactor captured model units per millimetre u
	 * @param innerFrame whether the inner frame is enabled
	 */
	public AlgoIsoABorder(Construction cons, String[] labels, GeoPointND point,
			GeoNumberValue index, GeoBoolean landscape, GeoNumberValue numerator,
			GeoNumberValue denominator, GeoNumberValue unitFactor, GeoBoolean innerFrame) {
		super(cons);
		this.point = Objects.requireNonNull(point);
		this.index = Objects.requireNonNull(index);
		this.landscape = Objects.requireNonNull(landscape);
		this.numerator = Objects.requireNonNull(numerator);
		this.denominator = Objects.requireNonNull(denominator);
		this.unitFactor = Objects.requireNonNull(unitFactor);
		this.innerFrame = Objects.requireNonNull(innerFrame);
		paperCorners = corners(cons);
		frameCorners = corners(cons);
		paper = closedPolyLine(cons, paperCorners);
		frame = closedPolyLine(cons, frameCorners);
		label = new GeoText(cons);
		label.setIsTextCommand(true);
		anchor = new GeoPoint(cons);
		anchor.setCoords(0, 0, 1);
		try {
			label.setStartPoint(anchor);
		} catch (CircularDefinitionException e) {
			Log.debug("sheet label anchor: " + e.getMessage());
		}
		label.setAlwaysFixed(true);
		label.setNeedsUpdatedBoundingBox(true);
		// drawn up and to the left of the anchor (AlignDrawText: -1 left of, 1 above)
		label.setHorizontalAlignment(-1);
		label.setVerticalAlignment(1);
		setInputOutput();
		compute();
		String[] names = labels;
		if (labels != null && labels.length == 1 && labels[0] != null
				&& !labels[0].isEmpty()) {
			names = new String[getOutputLength()];
			names[PAPER] = labels[0];
		}
		LabelManager.setLabels(names, getOutput());
		applyInitialPresentation();
	}

	private static GeoPoint[] corners(Construction cons) {
		GeoPoint[] corners = new GeoPoint[4];
		for (int i = 0; i < corners.length; i++) {
			corners[i] = new GeoPoint(cons);
			corners[i].setCoords(0, 0, 1);
		}
		return corners;
	}

	private static GeoPolyLine closedPolyLine(Construction cons, GeoPoint[] corners) {
		return new GeoPolyLine(cons, new GeoPointND[] {corners[0], corners[1], corners[2],
				corners[3], corners[0]});
	}

	private void applyInitialPresentation() {
		// as the XML reader sets for every polyline (ConsElementXMLHandler.init), so the
		// first save already equals its reopened form: a 2D sheet has no 3D presence
		paper.setVisibleInView3D(false);
		frame.setVisibleInView3D(false);
		paper.setEuclidianVisible(false);
		paper.setLineType(EuclidianStyleConstants.LINE_TYPE_DOTTED);
		paper.setLineThickness(PAPER_LINE_THICKNESS);
		paper.setObjColor(GColor.BLACK);
		paper.setLabelVisible(false);
		frame.setLineType(EuclidianStyleConstants.LINE_TYPE_FULL);
		frame.setLineThickness(FRAME_LINE_THICKNESS);
		frame.setObjColor(GColor.BLACK);
		frame.setLineOpacity(255);
		frame.setLabelVisible(false);
		label.setObjColor(GColor.BLACK);
	}

	@Override
	protected void setInputOutput() {
		input = new GeoElement[] {point.toGeoElement(), index.toGeoElement(), landscape,
				numerator.toGeoElement(), denominator.toGeoElement(), unitFactor.toGeoElement(),
				innerFrame};
		setOutputLength(3);
		setOutput(PAPER, paper);
		setOutput(FRAME, frame);
		setOutput(LABEL, label);
		setDependencies();
	}

	@Override
	public Commands getClassName() {
		return Commands.IsoABorder;
	}

	@Override
	public int getRelatedModeID() {
		return EuclidianConstants.MODE_ISO_A_BORDER;
	}

	/**
	 * Geometric computation only (design sections 4, 5, 7 and 15).
	 */
	@Override
	public void compute() {
		double n = valueOf(index);
		double a = valueOf(numerator);
		double b = valueOf(denominator);
		double u = valueOf(unitFactor);
		if (!isFinitePlanePoint(point) || !IsoASheet.isIndex(n) || !IsoASheet.isScaleTerm(a)
				|| !IsoASheet.isScaleTerm(b) || !IsoASheet.isUnitFactor(u)
				|| !landscape.isDefined() || !innerFrame.isDefined()) {
			setAllUndefined();
			return;
		}
		int size = (int) n;
		boolean wide = landscape.getBoolean();
		int widthMm = IsoASheet.paperWidthMm(size, wide);
		int heightMm = IsoASheet.paperHeightMm(size, wide);
		double x = point.getInhomX();
		double y = point.getInhomY();
		double width = IsoASheet.conv(widthMm, a, b, u);
		double height = IsoASheet.conv(heightMm, a, b, u);
		if (!representable(x, y, width, height)) {
			setAllUndefined();
			return;
		}
		place(paper, paperCorners, x, y, width, height);
		anchor.setCoords(x + IsoASheet.conv(widthMm - IsoASheet.MARGIN_RIGHT_MM, a, b, u),
				y - IsoASheet.conv(heightMm - IsoASheet.MARGIN_BOTTOM_MM, a, b, u), 1);
		label.setTextString(IsoASheet.label(size, (long) a, (long) b));
		if (!innerFrame.getBoolean() || !IsoASheet.isFramePossible(size, wide)) {
			frame.setUndefined();
			return;
		}
		double frameX = x + IsoASheet.conv(IsoASheet.MARGIN_LEFT_MM, a, b, u);
		double frameY = y - IsoASheet.conv(IsoASheet.MARGIN_TOP_MM, a, b, u);
		double frameWidth = IsoASheet.conv(IsoASheet.frameWidthMm(size, wide), a, b, u);
		double frameHeight = IsoASheet.conv(IsoASheet.frameHeightMm(size, wide), a, b, u);
		if (!representable(frameX, frameY, frameWidth, frameHeight)) {
			frame.setUndefined();
			return;
		}
		place(frame, frameCorners, frameX, frameY, frameWidth, frameHeight);
	}

	/**
	 * Not representable (design section 7.6): a non-finite or non-positive converted
	 * side, a non-finite corner or an absorbed addition. Nothing is clamped.
	 */
	private static boolean representable(double x, double y, double width, double height) {
		if (!Double.isFinite(x) || !Double.isFinite(y) || !(width > 0) || !(height > 0)
				|| !Double.isFinite(width) || !Double.isFinite(height)) {
			return false;
		}
		double right = x + width;
		double bottom = y - height;
		return Double.isFinite(right) && Double.isFinite(bottom) && right != x && bottom != y;
	}

	private static void place(GeoPolyLine polyLine, GeoPoint[] corners, double x, double y,
			double width, double height) {
		double right = x + width;
		double bottom = y - height;
		corners[0].setCoords(x, y, 1);
		corners[1].setCoords(right, y, 1);
		corners[2].setCoords(right, bottom, 1);
		corners[3].setCoords(x, bottom, 1);
		polyLine.calcLength();
	}

	private void setAllUndefined() {
		paper.setUndefined();
		frame.setUndefined();
		label.setUndefined();
		anchor.setUndefined();
	}

	private static boolean isFinitePlanePoint(GeoPointND candidate) {
		return candidate.isDefined() && !candidate.isGeoElement3D() && candidate.isFinite();
	}

	private static double valueOf(GeoNumberValue number) {
		return number.toGeoElement().isDefined() ? number.getDouble() : Double.NaN;
	}

	/**
	 * @return the current reliability, or null when the paper boundary is undefined
	 */
	public IsoASheet.Reliability getReliability() {
		if (!paper.isDefined()) {
			return null;
		}
		return IsoASheet.reliability((int) valueOf(index), landscape.getBoolean(),
				valueOf(numerator), valueOf(denominator), valueOf(unitFactor),
				innerFrame.getBoolean());
	}

	/**
	 * Derived coherence of the captured sheet with a current unit factor and session
	 * scale (presentation only; nothing is stored).
	 *
	 * @param metresPerUnit current effective construction metre factor, NaN if none
	 * @param sessionNumerator session scale numerator
	 * @param sessionDenominator session scale denominator
	 * @return coherence states
	 */
	public IsoASheet.Coherence coherence(double metresPerUnit, long sessionNumerator,
			long sessionDenominator) {
		boolean pointValid = isFinitePlanePoint(point);
		return IsoASheet.coherence(paper.isDefined() && pointValid, valueOf(index),
				landscape.isDefined() && landscape.getBoolean(), valueOf(numerator),
				valueOf(denominator), valueOf(unitFactor),
				innerFrame.isDefined() && innerFrame.getBoolean(),
				pointValid ? point.getInhomX() : Double.NaN,
				pointValid ? point.getInhomY() : Double.NaN, metresPerUnit, sessionNumerator,
				sessionDenominator);
	}

	/**
	 * @return paper bounds {xmin, xmax, ymin, ymax}, or null while undefined
	 */
	public double[] getPaperBounds() {
		if (!paper.isDefined()) {
			return null;
		}
		return new double[] {paperCorners[0].getInhomX(), paperCorners[1].getInhomX(),
				paperCorners[2].getInhomY(), paperCorners[0].getInhomY()};
	}

	/** @return the paper-boundary output */
	public GeoPolyLine getPaper() {
		return paper;
	}

	/** @return the inner-frame output */
	public GeoPolyLine getFrame() {
		return frame;
	}

	/** @return the construction-label output */
	public GeoText getLabelText() {
		return label;
	}

	/** @return the reference-point input */
	public GeoPointND getPoint() {
		return point;
	}

	/** @return captured scale numerator, NaN when undefined */
	public double getScaleNumerator() {
		return valueOf(numerator);
	}

	/** @return captured scale denominator, NaN when undefined */
	public double getScaleDenominator() {
		return valueOf(denominator);
	}

	/** @return captured model units per millimetre, NaN when undefined */
	public double getUnitFactor() {
		return valueOf(unitFactor);
	}

	/** @return ISO A index input value, NaN when undefined */
	public double getIndexValue() {
		return valueOf(index);
	}

	/** @return orientation input value */
	public boolean isLandscape() {
		return landscape.isDefined() && landscape.getBoolean();
	}

	/** @return inner-frame input value */
	public boolean isInnerFrameEnabled() {
		return innerFrame.isDefined() && innerFrame.getBoolean();
	}

	/**
	 * Association through the kernel's own construction relation only: the parent
	 * algorithm of an output; never a label, coordinate or position.
	 *
	 * @param geo a geo
	 * @return the sheet that owns the geo as one of its outputs, or null
	 */
	public static AlgoIsoABorder ownerOf(GeoElement geo) {
		if (geo != null && geo.getParentAlgorithm() instanceof AlgoIsoABorder) {
			return (AlgoIsoABorder) geo.getParentAlgorithm();
		}
		return null;
	}
}
