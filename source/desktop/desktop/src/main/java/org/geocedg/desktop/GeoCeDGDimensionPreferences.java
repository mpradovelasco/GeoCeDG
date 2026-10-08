/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * PRE-G9B-R6-plus-E2 smoke follow-up B1: the initial line thickness that the two
 * native dimension tools give the dimension line and both extension lines of a new
 * dimension. It lives in the GeoCeDG properties store, never in a document; a created
 * dimension keeps the thickness serialized in its own segment styles, so changing the
 * preference never alters existing dimensions. Reading never writes; an invalid stored
 * value behaves as unset.
 */
final class GeoCeDGDimensionPreferences {
	static final String LINE_THICKNESS_KEY = "geocedg.dimensions.initial-line-thickness.v1";
	/** Smallest accepted thickness, as the object properties line-thickness slider. */
	static final int MIN_LINE_THICKNESS = 1;
	/** Largest accepted thickness, as the object properties line-thickness slider. */
	static final int MAX_LINE_THICKNESS = GeoElement.MAX_LINE_WIDTH;

	private static GeoCeDGPresentationPreferences.Store storeOverride;

	private final GeoCeDGPresentationPreferences.Store store;

	GeoCeDGDimensionPreferences() {
		this(storeOverride != null ? storeOverride : GeoCeDGPresentationPreferences.systemStore());
	}

	GeoCeDGDimensionPreferences(GeoCeDGPresentationPreferences.Store store) {
		this.store = store;
	}

	/**
	 * Test seam: every instance created by the no-argument constructor uses the given
	 * store until the returned handle is closed, so tests never touch the real store.
	 */
	static AutoCloseable useStoreForTesting(GeoCeDGPresentationPreferences.Store store) {
		GeoCeDGPresentationPreferences.Store previous = storeOverride;
		storeOverride = store;
		return () -> storeOverride = previous;
	}

	/**
	 * @return the stored initial thickness, or the default
	 *         {@link AlgoNativeDimension#INITIAL_LINE_THICKNESS}
	 */
	int initialLineThickness() {
		String stored = store.load(LINE_THICKNESS_KEY);
		if (stored != null && stored.matches("[0-9]{1,3}")) {
			int value = Integer.parseInt(stored);
			if (value >= MIN_LINE_THICKNESS && value <= MAX_LINE_THICKNESS) {
				return value;
			}
		}
		return AlgoNativeDimension.INITIAL_LINE_THICKNESS;
	}

	/**
	 * @param thickness initial thickness of the segments of new dimensions
	 * @throws IllegalArgumentException outside the accepted range
	 */
	void setInitialLineThickness(int thickness) {
		if (thickness < MIN_LINE_THICKNESS || thickness > MAX_LINE_THICKNESS) {
			throw new IllegalArgumentException("line thickness " + thickness);
		}
		store.save(LINE_THICKNESS_KEY, Integer.toString(thickness));
	}
}
