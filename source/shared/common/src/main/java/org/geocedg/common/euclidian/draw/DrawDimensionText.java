/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.euclidian.draw;

import org.geocedg.common.kernel.dimension.DimensionTextSource;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.awt.GAffineTransform;
import org.geogebra.common.awt.GGeneralPath;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.awt.GRectangle;
import org.geogebra.common.euclidian.Drawable;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.draw.DrawText;
import org.geogebra.common.kernel.geos.GeoText;

/**
 * Presentation-only drawable of the value text of a native dimension
 * (PRE-G9B-R6-plus-E2, DQ-E2-6). The text keeps its plain value string; this drawable
 * paints it parallel to the projected dimension line, centred on it, never upside
 * down, and lifted clear of the dimension and extension lines. The angle, the lift and
 * the pixel box are derived from the model geometry of {@link DimensionTextSource} and
 * the current view at every update; none of them is stored in the construction.
 *
 * <p>
 * It is selected only for the text output of a native dimension; every other text
 * keeps {@link DrawText} unchanged. Painting delegates to an ordinary {@link DrawText}
 * inside a rotation about the placement centre.
 * </p>
 */
public final class DrawDimensionText extends Drawable {

	/** Typographic clearance between the text box and the line it clears, in px. */
	public static final double CLEARANCE_PX = 2;
	/** Free length the dimension line must keep beyond the text box at each end, px. */
	public static final double END_MARGIN_PX = 4;

	private final GeoText text;
	private final DimensionTextSource source;
	private final DrawText delegate;
	private boolean visible;
	private double centerX;
	private double centerY;
	private double thetaDegrees;
	private double boxCenterX;
	private double boxCenterY;
	private double boxX;
	private double boxY;
	private double boxWidth;
	private double boxHeight;
	private final double[] corners = new double[8];

	/**
	 * @param view view
	 * @param text the dimension's presentation text
	 * @param source the owning dimension
	 */
	public DrawDimensionText(EuclidianView view, GeoText text, DimensionTextSource source) {
		this.view = view;
		this.text = text;
		this.geo = text;
		this.source = source;
		this.delegate = new DrawText(view, text);
		update();
	}

	/**
	 * Readable-angle rule: maps the undirected angle of a line to (-90, 90].
	 *
	 * @param phiDegrees line angle in degrees (mathematical orientation, y up)
	 * @return the reading angle theta in (-90, 90]
	 */
	public static double readableAngle(double phiDegrees) {
		return phiDegrees - 180 * Math.ceil((phiDegrees - 90) / 180);
	}

	@Override
	public void update() {
		delegate.update();
		double[] frame = source.getDimensionTextFrame();
		GRectangle box = delegate.getBounds();
		visible = frame != null && box != null && text.isDefined()
				&& text.isEuclidianVisible();
		if (!visible) {
			return;
		}
		boxX = box.getX();
		boxY = box.getY();
		boxWidth = box.getWidth();
		boxHeight = box.getHeight();
		boxCenterX = boxX + boxWidth / 2;
		boxCenterY = boxY + boxHeight / 2;
		double ax = view.toScreenCoordXd(frame[0]);
		double ay = view.toScreenCoordYd(frame[1]);
		double sx = view.toScreenCoordXd(frame[0] + frame[2]) - ax;
		double sy = view.toScreenCoordYd(frame[1] + frame[3]) - ay;
		if (!(Math.hypot(sx, sy) > 0) || !Double.isFinite(ax) || !Double.isFinite(ay)) {
			visible = false;
			return;
		}
		thetaDegrees = readableAngle(Math.toDegrees(Math.atan2(-sy, sx)));
		double theta = Math.toRadians(thetaDegrees);
		double upX = -Math.sin(theta);
		double upY = -Math.cos(theta);
		double lift = liftAlongUp(ax, ay, upX, upY);
		centerX = ax + upX * lift;
		centerY = ay + upY * lift;
		updateCorners(theta);
		storeRealWorldBounds();
	}

	private double liftAlongUp(double ax, double ay, double upX, double upY) {
		double base = boxHeight / 2 + CLEARANCE_PX;
		double[] line = source.getDimensionLineEndpoints();
		double lineLength = 0;
		if (line != null) {
			lineLength = Math.hypot(
					view.toScreenCoordXd(line[2]) - view.toScreenCoordXd(line[0]),
					view.toScreenCoordYd(line[3]) - view.toScreenCoordYd(line[1]));
		}
		if (boxWidth + 2 * END_MARGIN_PX <= lineLength) {
			return base;
		}
		// The value is longer than its dimension line: place it beyond the extension
		// tips, on the overshoot side, so that no line crosses the characters.
		double side = 1;
		double[] overshoot = source.getOvershootDirection();
		if (overshoot != null) {
			double ox = view.toScreenCoordXd(source.getDimensionTextFrame()[0]
					+ overshoot[0]) - ax;
			double oy = view.toScreenCoordYd(source.getDimensionTextFrame()[1]
					+ overshoot[1]) - ay;
			if (ox * upX + oy * upY < 0) {
				side = -1;
			}
		}
		double tipDistance = 0;
		double[] tips = source.getExtensionTips();
		for (int i = 0; i < 4; i += 2) {
			if (Double.isFinite(tips[i]) && Double.isFinite(tips[i + 1])) {
				double tx = view.toScreenCoordXd(tips[i]) - ax;
				double ty = view.toScreenCoordYd(tips[i + 1]) - ay;
				tipDistance = Math.max(tipDistance, Math.abs(tx * upX + ty * upY));
			}
		}
		return side * (tipDistance + base);
	}

	private void updateCorners(double theta) {
		double c = Math.cos(theta);
		double s = Math.sin(theta);
		double[] local = {boxX, boxY, boxX + boxWidth, boxY, boxX + boxWidth,
				boxY + boxHeight, boxX, boxY + boxHeight};
		for (int i = 0; i < 8; i += 2) {
			double lx = local[i] - boxCenterX;
			double ly = local[i + 1] - boxCenterY;
			// screen rotation by -theta (y down): counter-clockwise on screen
			corners[i] = centerX + lx * c + ly * s;
			corners[i + 1] = centerY - lx * s + ly * c;
		}
	}

	private void storeRealWorldBounds() {
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		for (int i = 0; i < 8; i += 2) {
			minX = Math.min(minX, corners[i]);
			maxX = Math.max(maxX, corners[i]);
			minY = Math.min(minY, corners[i + 1]);
			maxY = Math.max(maxY, corners[i + 1]);
		}
		// Corner(text, n): the axis-aligned bounds of the displayed, rotated value
		text.setBoundingBox(view.toRealWorldCoordX(minX), view.toRealWorldCoordY(minY),
				(maxX - minX) * view.getInvXscale(), -(maxY - minY) * view.getInvYscale());
	}

	@Override
	public void draw(GGraphics2D g2) {
		if (!visible) {
			return;
		}
		GAffineTransform transform = AwtFactory.getPrototype().newAffineTransform();
		transform.translate(centerX, centerY);
		transform.rotate(-Math.toRadians(thetaDegrees));
		transform.translate(-boxCenterX, -boxCenterY);
		g2.saveTransform();
		g2.transform(transform);
		delegate.draw(g2);
		g2.restoreTransform();
	}

	@Override
	public boolean hit(int x, int y, int hitThreshold) {
		if (!visible) {
			return false;
		}
		double theta = Math.toRadians(thetaDegrees);
		double c = Math.cos(theta);
		double s = Math.sin(theta);
		double dx = x - centerX;
		double dy = y - centerY;
		// inverse of the drawing rotation
		double lx = dx * c - dy * s;
		double ly = dx * s + dy * c;
		return Math.abs(lx) <= boxWidth / 2 + hitThreshold
				&& Math.abs(ly) <= boxHeight / 2 + hitThreshold;
	}

	@Override
	public boolean isInside(GRectangle rect) {
		if (!visible) {
			return false;
		}
		for (int i = 0; i < 8; i += 2) {
			if (!rect.contains(corners[i], corners[i + 1])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean intersectsRectangle(GRectangle rect) {
		return visible && outline().intersects(rect);
	}

	private GGeneralPath outline() {
		GGeneralPath path = AwtFactory.getPrototype().newGeneralPath();
		path.moveTo(corners[0], corners[1]);
		for (int i = 2; i < 8; i += 2) {
			path.lineTo(corners[i], corners[i + 1]);
		}
		path.closePath();
		return path;
	}

	@Override
	public GRectangle getBounds() {
		if (!visible) {
			return null;
		}
		return outline().getBounds();
	}

	@Override
	public boolean hitLabel(int x, int y) {
		return false;
	}

	/**
	 * @return the current reading angle in degrees, (-90, 90] (presentation only)
	 */
	public double getReadingAngleDegrees() {
		return thetaDegrees;
	}

	/**
	 * @return the current placement centre {x, y} in screen coordinates
	 *         (presentation only)
	 */
	public double[] getPlacementCenter() {
		return new double[] {centerX, centerY};
	}

	/**
	 * @return the rotated box corners {x0, y0, ..., x3, y3} in screen coordinates
	 *         (presentation only)
	 */
	public double[] getRotatedCorners() {
		return corners.clone();
	}
}
