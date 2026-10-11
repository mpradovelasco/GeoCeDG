/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.awt.GBasicStroke;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GGeneralPath;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.euclidian.EuclidianView;

/**
 * PRE-G9B-R6-plus-F1 rendering helper: a discreet hollow triangular arrowhead (axial
 * length 10, base 4 device pixels) in the object's colour, interior filled with the
 * view background. It draws only into the live Graphics View 1 paint; it has no hit
 * area and creates no construction object.
 */
final class GeoCeDGOrientationCueRenderer {

	private GeoCeDGOrientationCueRenderer() {
	}

	/**
	 * Paints every cue of the view.
	 *
	 * @param view live view
	 * @param graphics its paint graphics
	 */
	static void paint(EuclidianView view, GGraphics2D graphics) {
		GColor background = view.getBackgroundCommon();
		GBasicStroke stroke = AwtFactory.getPrototype().newBasicStroke(1,
				GBasicStroke.CAP_BUTT, GBasicStroke.JOIN_MITER);
		for (GeoCeDGOrientationCue.Cue cue : GeoCeDGOrientationCue.cues(view)) {
			GGeneralPath path = path(cue);
			if (background != null) {
				graphics.setColor(background);
				graphics.fill(path);
			}
			GColor colour = cue.geo().getObjectColor();
			graphics.setColor(GColor.newColor(colour.getRed(), colour.getGreen(),
					colour.getBlue(), Math.max(0, Math.min(255, cue.geo().getLineOpacity()))));
			graphics.setStroke(stroke);
			graphics.draw(path);
		}
	}

	/** @return the closed triangle of a cue in screen coordinates */
	static GGeneralPath path(GeoCeDGOrientationCue.Cue cue) {
		double[] t = cue.triangle();
		GGeneralPath path = AwtFactory.getPrototype().newGeneralPath();
		path.moveTo(t[0], t[1]);
		path.lineTo(t[2], t[3]);
		path.lineTo(t[4], t[5]);
		path.closePath();
		return path;
	}
}
