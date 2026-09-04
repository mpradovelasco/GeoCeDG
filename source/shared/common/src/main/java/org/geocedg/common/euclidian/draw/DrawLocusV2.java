/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.euclidian.draw;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.awt.GRectangle;
import org.geogebra.common.euclidian.Drawable;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.GeneralPathClipped;

/** Dedicated G6B drawable derived exclusively from the semantic evaluator. */
public final class DrawLocusV2 extends Drawable {
	private final GeoLocusV2 locus;
	private final LocusRenderCache2D renderCache = new LocusRenderCache2D();
	private GeneralPathClipped path;
	private boolean visible;
	private boolean labelVisible;

	/** Creates one V2 drawable owned by a single Euclidian view. */
	public DrawLocusV2(EuclidianView view, GeoLocusV2 locus) {
		this.view = view;
		this.locus = locus;
		this.geo = locus;
		update();
	}

	@Override
	public void update() {
		visible = locus.isDefined() && locus.isEuclidianVisible();
		if (!visible) {
			return;
		}
		updateStrokes(locus);
		if (path == null) {
			path = new GeneralPathClipped(view);
		}
		path.resetWithThickness(locus.getLineThickness());
		strokedShape = null;
		LocusRenderData2D data = renderCache.getOrBuild(locus,
				LocusRenderPolicy2D.from(view));
		labelVisible = locus.isLabelVisible();
		boolean labelPositionSet = false;
		for (LocusRenderData2D.Vertex vertex : data.getVertices()) {
			LocusPoint2D point = vertex.getPoint();
			double x = view.toScreenCoordXd(point.getX());
			double y = view.toScreenCoordYd(point.getY());
			if (labelVisible && (!labelPositionSet
					|| !isOnScreen(xLabel, yLabel)) && isOnScreen(x, y)) {
				xLabel = (int) x;
				yLabel = (int) y;
				labelPositionSet = true;
			}
			if (labelVisible && !labelPositionSet) {
				xLabel = (int) x;
				yLabel = (int) y;
				labelPositionSet = true;
			}
			if (vertex.startsSubpath()) {
				path.moveTo(x, y);
			} else {
				path.lineTo(x, y);
			}
		}
		if (labelVisible && labelPositionSet) {
			labelDesc = locus.getLabelDescription();
			xLabel += 5;
			yLabel += 4 + view.getFontSize();
			addLabelOffsetEnsureOnScreen(view.getFontLine());
		} else {
			labelVisible = false;
		}
		visible = view.intersects(path.getGeneralPath());
	}

	private boolean isOnScreen(double x, double y) {
		return x >= 0 && x <= view.getWidth() && y >= 0 && y <= view.getHeight();
	}

	@Override
	public void draw(GGraphics2D graphics) {
		if (!visible || path == null) {
			return;
		}
		if (isHighlighted()) {
			graphics.setPaint(locus.getSelColor());
			graphics.setStroke(selStroke);
			path.draw(graphics);
		}
		graphics.setPaint(getObjectColor());
		graphics.setStroke(objStroke);
		path.draw(graphics);
		if (labelVisible) {
			graphics.setFont(view.getFontLine());
			graphics.setPaint(locus.getLabelColor());
			drawLabel(graphics);
		}
	}

	@Override
	public boolean hit(int x, int y, int hitThreshold) {
		if (!visible || path == null) {
			return false;
		}
		// A semantic curve has no selectable interior. This is presentation only:
		// the subsequent Point query is resolved by the kernel in world space.
		if (strokedShape == null) {
			strokedShape = objStroke.createStrokedShape(path.getGeneralPath(), 256);
		}
		return strokedShape.intersects(x - hitThreshold, y - hitThreshold,
				2 * hitThreshold, 2 * hitThreshold);
	}

	@Override
	public boolean isInside(GRectangle rectangle) {
		return path != null && rectangle.contains(path.getBounds());
	}

	@Override
	public GRectangle getBounds() {
		return visible && path != null ? path.getBounds() : null;
	}
}
