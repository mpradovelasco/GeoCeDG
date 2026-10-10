/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

/**
 * PRE-G9B-R6-plus-B effective export area: exact world bounds, the 2D source
 * view they belong to and the rule that produced them.
 *
 * <p>The world bounds are the exact geometric authority of every picture route
 * (author decisions AQ-X1 and AQ-X2 of 2026-10-02). An area is session or
 * derived presentation state: it is never serialized, never part of undo and
 * never geometry.
 */
public final class ExportArea {
	/** Rule that produced an effective area, in precedence order. */
	public enum Source {
		/** explicitly activated MANUAL producer */
		MANUAL,
		/** explicitly activated ISO A sized area, computed once (PRE-G9B-R6-plus-E3) */
		ISO_A_SELECTION,
		/** explicitly linked IsoABorder paper boundary, live (PRE-G9B-R6-plus-E3) */
		ISO_A_BORDER,
		/** explicitly activated EXPORT_POINTS producer */
		EXPORT_POINTS_EXPLICIT,
		/** Export_1/Export_2 used automatically */
		EXPORT_POINTS_AUTOMATIC,
		/** fallback: the visible viewport of the source view */
		VISIBLE_VIEWPORT
	}

	private final double xmin;
	private final double xmax;
	private final double ymin;
	private final double ymax;
	private final int sourceViewId;
	private final Source source;

	private ExportArea(double xmin, double xmax, double ymin, double ymax,
			int sourceViewId, Source source) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.sourceViewId = sourceViewId;
		this.source = source;
	}

	/**
	 * Normalizes the corners; refuses a non-finite or zero-area rectangle.
	 *
	 * @param x1 first x
	 * @param x2 second x
	 * @param y1 first y
	 * @param y2 second y
	 * @param sourceViewId view id of the 2D source view
	 * @param source producing rule
	 * @return the area, or null when it is not a valid rectangle
	 */
	public static ExportArea of(double x1, double x2, double y1, double y2,
			int sourceViewId, Source source) {
		if (!Double.isFinite(x1) || !Double.isFinite(x2) || !Double.isFinite(y1)
				|| !Double.isFinite(y2)) {
			return null;
		}
		double minX = Math.min(x1, x2);
		double maxX = Math.max(x1, x2);
		double minY = Math.min(y1, y2);
		double maxY = Math.max(y1, y2);
		if (!(maxX > minX) || !(maxY > minY)) {
			return null;
		}
		return new ExportArea(minX, maxX, minY, maxY, sourceViewId, source);
	}

	/** @return minimal world x */
	public double getXmin() {
		return xmin;
	}

	/** @return maximal world x */
	public double getXmax() {
		return xmax;
	}

	/** @return minimal world y */
	public double getYmin() {
		return ymin;
	}

	/** @return maximal world y */
	public double getYmax() {
		return ymax;
	}

	/** @return view id of the 2D source view */
	public int getSourceViewId() {
		return sourceViewId;
	}

	/** @return rule that produced this area */
	public Source getSource() {
		return source;
	}

	/**
	 * @param xscale source-view pixels per world unit
	 * @return exact width in source-view pixels
	 */
	public double pixelWidth(double xscale) {
		return (xmax - xmin) * xscale;
	}

	/**
	 * @param yscale source-view pixels per world unit
	 * @return exact height in source-view pixels
	 */
	public double pixelHeight(double yscale) {
		return (ymax - ymin) * yscale;
	}

	/**
	 * @param other other area
	 * @return whether both have the same bounds, view and source
	 */
	public boolean sameAs(ExportArea other) {
		return other != null && Double.compare(xmin, other.xmin) == 0
				&& Double.compare(xmax, other.xmax) == 0
				&& Double.compare(ymin, other.ymin) == 0
				&& Double.compare(ymax, other.ymax) == 0
				&& sourceViewId == other.sourceViewId && source == other.source;
	}

	@Override
	public String toString() {
		return source + "[" + xmin + ", " + xmax + "] x [" + ymin + ", " + ymax
				+ "] view " + sourceViewId;
	}
}
