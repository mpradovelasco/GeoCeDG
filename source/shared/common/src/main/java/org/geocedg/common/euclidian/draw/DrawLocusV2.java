/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.euclidian.draw;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geogebra.common.awt.GBasicStroke;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.awt.GRectangle;
import org.geogebra.common.awt.GShape;
import org.geogebra.common.euclidian.Drawable;
import org.geogebra.common.euclidian.EuclidianStatic;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.GeneralPathClipped;
import org.geogebra.common.plugin.EuclidianStyleConstants;

/** Dedicated G6B drawable derived exclusively from the semantic evaluator. */
public final class DrawLocusV2 extends Drawable {
	/** Opacity of the distinct style of exploratory evidence (decision E-1). */
	private static final int EXPLORATORY_ALPHA = 128;
	private final GeoLocusV2 locus;
	private final LocusRenderCache2D renderCache = new LocusRenderCache2D();
	private GeneralPathClipped path;
	private GeneralPathClipped exploratoryPath;
	private GBasicStroke exploratoryStroke;
	private GShape exploratoryShape;
	private boolean certifiedVertices;
	private boolean visible;
	private boolean labelVisible;

	/** Creates one V2 drawable owned by a single Euclidian view. */
	public DrawLocusV2(EuclidianView view, GeoLocusV2 locus) {
		this.view = view;
		this.locus = locus;
		this.geo = locus;
		update();
	}

	/** @return render cache of this view; callers only read its counters */
	public LocusRenderCache2D getRenderCache() {
		return renderCache;
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
		if (labelVisible && !labelPositionSet && !data.getExploratoryVertices().isEmpty()) {
			// A locus drawn only as exploratory evidence keeps its label on that evidence.
			LocusPoint2D point = data.getExploratoryVertices().get(0).getPoint();
			xLabel = (int) view.toScreenCoordXd(point.getX());
			yLabel = (int) view.toScreenCoordYd(point.getY());
			labelPositionSet = true;
		}
		if (labelVisible && labelPositionSet) {
			labelDesc = locus.getLabelDescription();
			xLabel += 5;
			yLabel += 4 + view.getFontSize();
			addLabelOffsetEnsureOnScreen(view.getFontLine());
		} else {
			labelVisible = false;
		}
		certifiedVertices = !data.getVertices().isEmpty();
		updateExploratoryPath(data);
		visible = view.intersects(path.getGeneralPath()) || exploratoryPath != null
				&& view.intersects(exploratoryPath.getGeneralPath());
	}

	/**
	 * Exploratory evidence of the unresolved bands (decision E-1): its own path, drawn with
	 * a distinct dotted, translucent style and never joined to a certified component.
	 */
	private void updateExploratoryPath(LocusRenderData2D data) {
		exploratoryShape = null;
		if (data.getExploratoryVertices().isEmpty()) {
			exploratoryPath = null;
			return;
		}
		if (exploratoryPath == null) {
			exploratoryPath = new GeneralPathClipped(view);
		}
		exploratoryPath.resetWithThickness(locus.getLineThickness());
		exploratoryStroke = EuclidianStatic.getStroke(objStroke.getLineWidth(),
				EuclidianStyleConstants.LINE_TYPE_DOTTED);
		for (LocusRenderData2D.Vertex vertex : data.getExploratoryVertices()) {
			double x = view.toScreenCoordXd(vertex.getPoint().getX());
			double y = view.toScreenCoordYd(vertex.getPoint().getY());
			if (vertex.startsSubpath()) {
				exploratoryPath.moveTo(x, y);
			} else {
				exploratoryPath.lineTo(x, y);
			}
		}
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
		if (exploratoryPath != null) {
			graphics.setPaint(getObjectColor().deriveWithAlpha(EXPLORATORY_ALPHA));
			graphics.setStroke(exploratoryStroke);
			exploratoryPath.draw(graphics);
		}
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
		if (strokedShape.intersects(x - hitThreshold, y - hitThreshold,
				2 * hitThreshold, 2 * hitThreshold)) {
			return true;
		}
		// Exploratory evidence selects the object only; it is no path of the locus.
		if (exploratoryPath == null) {
			return false;
		}
		if (exploratoryShape == null) {
			exploratoryShape = objStroke.createStrokedShape(
					exploratoryPath.getGeneralPath(), 256);
		}
		return exploratoryShape.intersects(x - hitThreshold, y - hitThreshold,
				2 * hitThreshold, 2 * hitThreshold);
	}

	@Override
	public boolean isInside(GRectangle rectangle) {
		if (path == null) {
			return false;
		}
		if (exploratoryPath == null) {
			return rectangle.contains(path.getBounds());
		}
		return (!certifiedVertices || rectangle.contains(path.getBounds()))
				&& rectangle.contains(exploratoryPath.getBounds());
	}

	@Override
	public GRectangle getBounds() {
		if (!visible || path == null) {
			return null;
		}
		if (exploratoryPath == null) {
			return path.getBounds();
		}
		return certifiedVertices ? path.getBounds().union(exploratoryPath.getBounds())
				: exploratoryPath.getBounds();
	}
}
