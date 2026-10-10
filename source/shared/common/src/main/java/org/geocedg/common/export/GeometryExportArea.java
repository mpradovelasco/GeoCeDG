/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

/**
 * PRE-G9B-R6-plus-C (author decision {@code DQ-C13}, choice {@code B1}): the
 * export area that a neutral geometry export consumes, as resolved from the
 * single PRE-G9B-R6-plus-B {@code ExportArea} authority of the active 2D
 * Graphics view.
 *
 * <p>Only the explicit producers ({@code MANUAL}, {@code ISO_A_SELECTION},
 * {@code ISO_A_BORDER}, {@code EXPORT_POINTS_EXPLICIT} and
 * {@code EXPORT_POINTS_AUTOMATIC}) define a closed participation boundary.
 * The {@code VISIBLE_VIEWPORT} fallback is explicitly no boundary: the export
 * keeps its model-space population and never depends on the zoom, so its record
 * carries neither bounds nor a view. An area is session state: it is never
 * serialized and never geometry.
 */
public final class GeometryExportArea {

	/** Versioned participation rule named in reports, comments and sidecars. */
	public static final String RULE_ID = "geocedg-export-area-participation-b1/v1";

	/** Producer of the resolved area, in the PRE-G9B-R6-plus-B precedence. */
	public enum Producer {
		/** explicitly activated stored world rectangle */
		MANUAL,
		/** explicitly activated ISO A sized area, computed once (PRE-G9B-R6-plus-E3) */
		ISO_A_SELECTION,
		/** explicitly linked IsoABorder paper boundary, live (PRE-G9B-R6-plus-E3) */
		ISO_A_BORDER,
		/** explicitly activated Export_1/Export_2 producer */
		EXPORT_POINTS_EXPLICIT,
		/** Export_1/Export_2 used automatically */
		EXPORT_POINTS_AUTOMATIC,
		/** fallback visible viewport: no DXF boundary */
		VISIBLE_VIEWPORT
	}

	private static final GeometryExportArea VISIBLE_VIEWPORT_FALLBACK =
			new GeometryExportArea(Producer.VISIBLE_VIEWPORT, Double.NaN,
					Double.NaN, Double.NaN, Double.NaN, -1);

	private final Producer producer;
	private final double xmin;
	private final double xmax;
	private final double ymin;
	private final double ymax;
	private final int sourceViewId;

	private GeometryExportArea(Producer producer, double xmin, double xmax,
			double ymin, double ymax, int sourceViewId) {
		this.producer = producer;
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.sourceViewId = sourceViewId;
	}

	/**
	 * @param producer explicit producer; never {@code VISIBLE_VIEWPORT}
	 * @param xmin minimal world x
	 * @param xmax maximal world x
	 * @param ymin minimal world y
	 * @param ymax maximal world y
	 * @param sourceViewId view id of the 2D source view
	 * @return closed participation boundary
	 */
	public static GeometryExportArea explicit(Producer producer, double xmin,
			double xmax, double ymin, double ymax, int sourceViewId) {
		if (producer == null || producer == Producer.VISIBLE_VIEWPORT) {
			throw new IllegalArgumentException(
					"An explicit export area requires an explicit producer");
		}
		if (!Double.isFinite(xmin) || !Double.isFinite(xmax)
				|| !Double.isFinite(ymin) || !Double.isFinite(ymax)
				|| !(xmax > xmin) || !(ymax > ymin)) {
			throw new IllegalArgumentException(
					"An explicit export area requires finite ordered bounds");
		}
		return new GeometryExportArea(producer, normalizeZero(xmin),
				normalizeZero(xmax), normalizeZero(ymin), normalizeZero(ymax),
				sourceViewId);
	}

	/** @return the fallback record: no participation boundary */
	public static GeometryExportArea visibleViewportFallback() {
		return VISIBLE_VIEWPORT_FALLBACK;
	}

	public Producer getProducer() {
		return producer;
	}

	/** @return whether the area restricts participation */
	public boolean isBoundary() {
		return producer != Producer.VISIBLE_VIEWPORT;
	}

	/** @return minimal world x; NaN without a boundary */
	public double getXmin() {
		return xmin;
	}

	/** @return maximal world x; NaN without a boundary */
	public double getXmax() {
		return xmax;
	}

	/** @return minimal world y; NaN without a boundary */
	public double getYmin() {
		return ymin;
	}

	/** @return maximal world y; NaN without a boundary */
	public double getYmax() {
		return ymax;
	}

	/** @return source view id; -1 without a boundary */
	public int getSourceViewId() {
		return sourceViewId;
	}

	/** @return deterministic one-line description for reports and comments */
	public String describe() {
		if (!isBoundary()) {
			return "no explicit export area (VISIBLE_VIEWPORT fallback): "
					+ "model-space population, zoom-independent";
		}
		return producer + " [" + xmin + ", " + xmax + "] x [" + ymin + ", "
				+ ymax + "] of view " + sourceViewId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof GeometryExportArea)) {
			return false;
		}
		GeometryExportArea area = (GeometryExportArea) other;
		return producer == area.producer
				&& Double.compare(xmin, area.xmin) == 0
				&& Double.compare(xmax, area.xmax) == 0
				&& Double.compare(ymin, area.ymin) == 0
				&& Double.compare(ymax, area.ymax) == 0
				&& sourceViewId == area.sourceViewId;
	}

	@Override
	public int hashCode() {
		int hash = producer.hashCode();
		hash = 31 * hash + Double.hashCode(xmin);
		hash = 31 * hash + Double.hashCode(xmax);
		hash = 31 * hash + Double.hashCode(ymin);
		hash = 31 * hash + Double.hashCode(ymax);
		return 31 * hash + sourceViewId;
	}

	@Override
	public String toString() {
		return describe();
	}

	private static double normalizeZero(double value) {
		return value == 0 ? 0 : value;
	}
}
