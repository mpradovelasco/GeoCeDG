/*
 * GeoGebra - Dynamic Mathematics for Everyone
 * Copyright (c) GeoGebra GmbH, Altenbergerstr. 69, 4040 Linz, Austria
 * https://www.geogebra.org
 *
 * This file is licensed by GeoGebra GmbH under the EUPL 1.2 licence and
 * may be used under the EUPL 1.2 in compatible projects (see Article 5
 * and the Appendix of EUPL 1.2 for details).
 * You may obtain a copy of the licence at:
 * https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Note: The overall GeoGebra software package is free to use for
 * non-commercial purposes only.
 * See https://www.geogebra.org/license for full licensing details
 */

package org.geogebra.common.euclidian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Predicate;

import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoPriorityComparator;

/**
 * List to store Drawable objects for fast drawing.
 */
public class DrawableList extends ArrayList<Drawable> {

	private Comparator<Drawable> comparator;

	/**
	 * Create a DrawableList with the given GeoPriorityComparator
	 * @param comparator defines sorting of this list
	 */
	public DrawableList(final GeoPriorityComparator comparator) {
		this.comparator = new Comparator<>() {
			@Override
			public int compare(Drawable a, Drawable b) {
				return comparator.compare(a.geo, b.geo, false);
			}
		};
	}

	@Override
	public final boolean add(Drawable d) {
		if (d == null) {
			return false;
		}

		int i = 0;
		while (i < size() && comparator.compare(get(i), d) < 0) {
			i++;
		}

		add(i, d);
		return true;
	}

	/**
	 * Draws all drawables in the list.
	 * 
	 * @param g2
	 *            Graphic to be used
	 */
	public final void drawAll(GGraphics2D g2) {
		drawAll(g2, d -> true);
	}

	/**
	 * GeoCeDG (2026-10-02): PRE-G9B-R6-plus-A-1 draws only the drawables the
	 * predicate admits; {@link #drawAll(GGraphics2D)} admits all of them.
	 *
	 * @param g2
	 *            graphics
	 * @param shown
	 *            presentation predicate
	 */
	public final void drawAll(GGraphics2D g2, Predicate<Drawable> shown) {
		for (Drawable d : this) {
			if (!shown.test(d)) {
				continue;
			}
			GeoElement geo = d.getGeoElement();
			if (d.isInteractiveEditor()) {
				d.updateIfNeeded();
			} else if (geo.isDefined()
					&& !(geo.isGeoList() && ((GeoList) geo).drawAsComboBox())
					&& !geo.isGeoInputBox() && !geo.isMask() && !geo.isMeasurementTool()
					&& !geo.isSpotlight()) {
				d.updateIfNeeded();
				d.draw(g2);
			}
		}
	}

	/**
	 * Updates all drawables in list
	 */
	public final void updateAll() {
		for (Drawable d : this) {
			d.update();
		}
	}

	/**
	 * Sort in drawing order.
	 */
	public void sort() {
		Collections.sort(this, comparator);
	}

	@Override
	public boolean equals(Object other) {
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
