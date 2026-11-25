/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geocedg.common.importer.dxf.DxfTessellation;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.awt.GBasicStroke;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GGeneralPath;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.euclidian.EuclidianView;

/**
 * PRE-G9B-MAINT-A-DXF0 presentation of imported DXF references, shared by the live paint of
 * Graphics 1 and, since A-DXF0-R1, by the picture-export service of Graphics 1 (PNG, SVG, PDF,
 * EMF, print, clipboard and preview).
 *
 * <p>Each visible reference is drawn from its record tessellation, computed once in model
 * units; the target view only maps model to output coordinates, so the drawn polyline is the
 * same at every zoom, DPI, font size and output scale. The drawing is a path of the target
 * graphics, so vector outputs keep it as a vector path. References are not GeoElements: no
 * drawable, hit test, snap, layer or Undo entry, and nothing here reads or writes the
 * construction.
 */
final class GeoCeDGDxfReferencePainter {
	/** Reference presentation colour. */
	static final GColor REFERENCE_COLOR = GColor.newColor(150, 60, 170);
	/** Colour of the mark at the last point of an incomplete render. */
	static final GColor INCOMPLETE_COLOR = GColor.newColor(200, 0, 0);
	private static final double STROKE_WIDTH = 1.5;
	private static final double[] DASH = {7, 3};
	private static final double INCOMPLETE_MARK = 6;

	private GeoCeDGDxfReferencePainter() {
	}

	/**
	 * @param session session store R-a, or null
	 * @return whether at least one reference is visible
	 */
	static boolean hasVisible(GeoCeDGDxfReferenceSession session) {
		if (session == null) {
			return false;
		}
		for (GeoCeDGDxfReferenceSession.Entry entry : session.entries()) {
			if (entry.isVisible()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Draws the visible references of a session store into a view's coordinate system.
	 *
	 * @param session session store R-a
	 * @param view view whose model-to-output mapping is used (live view or export viewport)
	 * @param graphics target graphics
	 * @param styleScale view pixels per style pixel (1 on screen; the physical style scale of
	 *        an export viewport)
	 */
	static void paint(GeoCeDGDxfReferenceSession session, EuclidianView view,
			GGraphics2D graphics, double styleScale) {
		if (session == null || session.isEmpty()) {
			return;
		}
		double scale = styleScale > 0 && Double.isFinite(styleScale) ? styleScale : 1;
		graphics.setColor(REFERENCE_COLOR);
		graphics.setStroke(AwtFactory.getPrototype().newBasicStroke(STROKE_WIDTH * scale,
				GBasicStroke.CAP_BUTT, GBasicStroke.JOIN_ROUND, 10,
				new double[] {DASH[0] * scale, DASH[1] * scale}));
		for (GeoCeDGDxfReferenceSession.Entry entry : session.entries()) {
			if (!entry.isVisible()) {
				continue;
			}
			DxfTessellation render = entry.reference().tessellation();
			if (render.size() == 0) {
				continue;
			}
			GGeneralPath path = AwtFactory.getPrototype().newGeneralPath();
			for (int i = 0; i < render.size(); i++) {
				double x = view.toScreenCoordXd(render.x(i));
				double y = view.toScreenCoordYd(render.y(i));
				if (i == 0) {
					path.moveTo(x, y);
				} else {
					path.lineTo(x, y);
				}
			}
			graphics.draw(path);
			if (!render.isComplete()) {
				double x = view.toScreenCoordXd(render.x(render.size() - 1));
				double y = view.toScreenCoordYd(render.y(render.size() - 1));
				double d = INCOMPLETE_MARK * scale;
				GGeneralPath mark = AwtFactory.getPrototype().newGeneralPath();
				mark.moveTo(x - d, y - d);
				mark.lineTo(x + d, y + d);
				mark.moveTo(x - d, y + d);
				mark.lineTo(x + d, y - d);
				graphics.setColor(INCOMPLETE_COLOR);
				graphics.draw(mark);
				graphics.setColor(REFERENCE_COLOR);
			}
		}
	}
}
