/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.awt.GRectangle;
import org.geogebra.common.awt.GRectangle2D;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.desktop.euclidian.EuclidianControllerD;
import org.geogebra.desktop.euclidian.EuclidianViewD;

/**
 * PRE-G9B-R6-plus-B offscreen export viewport (author decision AQ-X1 of
 * 2026-10-02).
 *
 * <p>A temporary 2D view whose coordinate system represents one export area
 * at the source view's scale: every view-bounded drawable (points, function
 * sampling, axes, grid, labels, Locus V2 tessellation) is computed for the area
 * itself, so content outside the live viewport is complete. The viewport has
 * its own controller, view id and drawables, no shared settings, and is never
 * attached to the kernel, so it changes no construction value and leaves the
 * live view untouched. A legacy {@code GeoLocus} is drawn only from the samples
 * the construction already holds ({@code OBS-B-LEGACY-LOCUS-OFFSCREEN-COVERAGE}).
 *
 * <p>The canvas is the exact area: the output scale maps the area's exact
 * width and height onto the output, and the clip is the exact rectangle.
 */
public final class ExportViewport extends EuclidianViewD {
	/** View id of every export viewport; distinct from all host view ids. */
	public static final int VIEW_ID = 0x40000000;

	private EuclidianView source;
	private double exactWidth;
	private double exactHeight;
	private int canvasWidth;
	private int canvasHeight;
	private double outputScaleX = 1;
	private double outputScaleY = 1;

	private ExportViewport(EuclidianView source) {
		super(new EuclidianControllerD(source.getKernel()), new boolean[] { false, false },
				false, EVNO_GENERAL, null);
	}

	/**
	 * @param source live 2D view whose presentation is copied
	 * @param area export area of that view
	 * @return a populated viewport for this area
	 */
	public static ExportViewport create(EuclidianView source, ExportArea area) {
		ExportViewport viewport = new ExportViewport(source);
		viewport.bind(source, area);
		return viewport;
	}

	private void bind(EuclidianView src, ExportArea area) {
		source = src;
		exactWidth = area.pixelWidth(src.getXscale());
		exactHeight = area.pixelHeight(src.getYscale());
		canvasWidth = Math.max(1, (int) Math.ceil(exactWidth));
		canvasHeight = Math.max(1, (int) Math.ceil(exactHeight));
		copyPresentation(src);
		setCoordSystem(-area.getXmin() * src.getXscale(), area.getYmax() * src.getYscale(),
				src.getXscale(), src.getYscale(), false);
		getKernel().notifyAddAll(this);
	}

	/**
	 * Copies presentation values through the view setters. The live view's
	 * settings are never shared: their bound objects would be written by this
	 * viewport's coordinate system.
	 */
	private void copyPresentation(EuclidianView src) {
		setAllowShowMouseCoords(false);
		setAllowToolTips(0);
		setBackground(src.getBackgroundCommon());
		setAxesColor(src.getAxesColor());
		setGridColor(src.getGridColor());
		setAxesLineStyle(src.getAxesLineStyle());
		setGridLineStyle(src.getGridLineStyle());
		for (int axis = 0; axis < 2; axis++) {
			setShowAxis(axis, src.getShowAxis(axis), false);
			setAxisLabel(axis, src.getAxisLabel(axis, true));
			if (src.isAutomaticAxesNumberingDistance()[axis]) {
				setAutomaticAxesNumberingDistance(true, axis);
			} else {
				setAxesNumberingDistance(src.getAxesDistanceObjects()[axis], axis);
			}
		}
		setAxesUnitLabels(src.getAxesUnitLabels().clone());
		setShowAxesNumbers(src.getShowAxesNumbers().clone());
		setAxesTickStyles(src.getAxesTickStyles().clone());
		setAxesCross(src.getAxesCross().clone());
		setPositiveAxes(src.getPositiveAxes().clone());
		setDrawBorderAxes(src.getDrawBorderAxes().clone());
		showGrid(src.getShowGrid());
		setGridIsBold(src.getGridIsBold());
		setGridType(src.getGridType());
		if (src.isAutomaticGridDistance()) {
			setAutomaticGridDistance(true);
		} else {
			setGridDistances(src.getGridDistances().clone());
		}
	}

	/**
	 * Printing scale (cm per unit) of a source view. A view that is not laid
	 * out yet, as on the command line, has none (0); it then gets the value the
	 * host rule of {@code calcPrintingScale} gives for its scale, without
	 * changing the view (DQ-B9).
	 *
	 * @param source live 2D view
	 * @return positive printing scale
	 */
	public static double printingScaleOf(EuclidianView source) {
		double scale = source.getPrintingScale();
		if (scale > 0 && Double.isFinite(scale)) {
			return scale;
		}
		double unitPerCm = PRINTER_PIXEL_PER_CM / source.getXscale();
		return Math.pow(10, -Math.round(Math.log10(unitPerCm)));
	}

	/**
	 * @param scaleX output units per viewport pixel along x
	 * @param scaleY output units per viewport pixel along y
	 */
	void setOutputScale(double scaleX, double scaleY) {
		outputScaleX = scaleX;
		outputScaleY = scaleY;
	}

	/** @return exact canvas width in viewport pixels */
	public double getExactWidth() {
		return exactWidth;
	}

	/** @return exact canvas height in viewport pixels */
	public double getExactHeight() {
		return exactHeight;
	}

	@Override
	public void attachView() {
		// Deliberately not attached to the kernel: no kernel view bounds, no
		// notifyEuclidianViewCE and no recomputation of view-dependent algorithms.
	}

	@Override
	public int getViewID() {
		return VIEW_ID;
	}

	@Override
	public boolean isVisibleInThisView(GeoElementND geo) {
		// Visibility follows the source view; identity stays this viewport's own.
		return source != null && source.isVisibleInThisView(geo);
	}

	@Override
	public int getWidth() {
		return canvasWidth;
	}

	@Override
	public int getHeight() {
		return canvasHeight;
	}

	@Override
	public int getExportWidth() {
		return canvasWidth;
	}

	@Override
	public int getExportHeight() {
		return canvasHeight;
	}

	@Override
	public GRectangle getFrame() {
		// Never Export_1/Export_2 or a selection rectangle: the canvas is the area.
		return AwtFactory.getPrototype().newRectangle(0, 0, canvasWidth, canvasHeight);
	}

	@Override
	public GRectangle getSelectionRectangle() {
		return null;
	}

	@Override
	protected void exportPaintPreScale(GGraphics2D g2d, double scale) {
		g2d.scale(outputScaleX, outputScaleY);
	}

	@Override
	public void exportPaintPre(GGraphics2D g2d, double scale, boolean transparency) {
		exportPaintPreScale(g2d, scale);
		// the exact area, no +2 band and no integer truncation: the Desktop
		// setClip(double...) truncates to int, a rectangle shape does not
		GRectangle2D clip = AwtFactory.getPrototype().newRectangle2D();
		clip.setRect(0, 0, exactWidth, exactHeight);
		g2d.setClip(clip);
		if (isTraceDrawn() || hasBackgroundImages()) {
			if (bgImage == null) {
				drawBackgroundWithImages(g2d, transparency);
			} else {
				drawBackgroundImage(g2d);
			}
		} else {
			if (!transparency) {
				clearBackground(g2d);
			}
			drawBackground(g2d);
		}
		g2d.setAntialiasing();
	}
}
