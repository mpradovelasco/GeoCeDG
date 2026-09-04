/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.euclidian.EuclidianController;
import org.geogebra.common.main.settings.EuclidianSettings;
import org.geogebra.desktop.geogebra3D.euclidianFor3D.EuclidianViewFor3DD;

/** GeoCeDG-only transient rich-result overlay; creates no construction objects. */
public final class GeoCeDGEuclidianView extends EuclidianViewFor3DD {
	/** Creates the regular Desktop view with a non-semantic overlay. */
	public GeoCeDGEuclidianView(EuclidianController controller, boolean[] axes,
			boolean grid, int number, EuclidianSettings settings) {
		super(controller, axes, grid, number, settings);
	}

	@Override
	public void paint(GGraphics2D graphics) {
		super.paint(graphics);
		if (!(getEuclidianController() instanceof GeoCeDGEuclidianController)) {
			return;
		}
		GeoCeDGIntersectionSession session = ((GeoCeDGEuclidianController)
				getEuclidianController()).getIntersectionSession();
		if (session == null || !session.isMarkersVisible()) {
			return;
		}
		GeoLocusIntersectionResult rich = session.getActive();
		if (rich == null || !rich.isDefined()) {
			return;
		}
		graphics.setStroke(AwtFactory.getPrototype().newBasicStroke(2));
		var materialized = session.materializedTokens();
		var selected = session.selectedTokens();
		for (LocusIntersectionSolution2D solution : session.markerSolutions()) {
			LocusPoint2D point = solution.getEvaluatedPoint();
			if (point == null || !Double.isFinite(point.getX())
					|| !Double.isFinite(point.getY())) {
				continue;
			}
			String token = solution.getIdentity().getRootToken();
			graphics.setColor(GColor.newColor(0, 120, 170));
			int x = toScreenCoordX(point.getX());
			int y = toScreenCoordY(point.getY());
			graphics.drawLine(x - 5, y - 5, x + 5, y + 5);
			graphics.drawLine(x - 5, y + 5, x + 5, y - 5);
			if (materialized.contains(token)) {
				graphics.drawRect(x - 6, y - 6, 12, 12);
			}
			if (selected.contains(token)) {
				graphics.drawRect(x - 8, y - 8, 16, 16);
			}
		}
	}
}
