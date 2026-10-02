/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.util.ArrayList;
import java.util.List;

import org.geogebra.common.awt.GDimension;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.settings.EuclidianSettings;

/**
 * PRE-G9B-R6-plus-B session authority of the export area (author decision
 * AQ-X2 of 2026-10-02).
 *
 * <p>Precedence: the explicitly activated producer, then {@code Export_1} and
 * {@code Export_2}, then the visible viewport of the source view. The upstream
 * selection rectangle never counts. The state is {@code SESSION}: it is never
 * serialized, never part of undo, never marks the document modified, and New
 * and Open reset it, after which {@code Export_1}/{@code Export_2} apply again
 * automatically. Every area keeps an explicit source view id; only the initial
 * MANUAL user interface is limited to Graphics 1 (DQ-B5).
 */
public final class ExportAreaSession {
	/** Producers implemented by B; ISO A producers belong to E3. */
	public enum Producer {
		/** live from the document points Export_1 and Export_2 */
		EXPORT_POINTS,
		/** a stored world rectangle */
		MANUAL
	}

	private final Kernel kernel;
	private final List<Runnable> listeners = new ArrayList<>();
	private Producer explicitProducer;
	private ExportArea manualArea;
	private boolean overlayShown;

	/**
	 * @param kernel kernel whose construction holds Export_1 and Export_2
	 */
	public ExportAreaSession(Kernel kernel) {
		this.kernel = kernel;
	}

	/**
	 * @param listener presentation listener (repaint only)
	 */
	public void addListener(Runnable listener) {
		listeners.add(listener);
	}

	/**
	 * Resolves the effective area of one 2D view.
	 *
	 * @param view source view
	 * @return effective area, or null when not even the viewport is valid
	 */
	public ExportArea resolve(EuclidianView view) {
		int viewId = view.getViewID();
		if (explicitProducer == Producer.MANUAL && manualArea != null
				&& manualArea.getSourceViewId() == viewId) {
			return manualArea;
		}
		ExportArea points = exportPoints(viewId,
				explicitProducer == Producer.EXPORT_POINTS
						? ExportArea.Source.EXPORT_POINTS_EXPLICIT
						: ExportArea.Source.EXPORT_POINTS_AUTOMATIC);
		if (points != null) {
			return points;
		}
		return visibleViewportOf(view);
	}

	/**
	 * @param view source view
	 * @return whether the explicitly activated producer cannot serve this view
	 *         and the resolution fell back to a later rule
	 */
	public boolean isExplicitProducerUnavailable(EuclidianView view) {
		if (explicitProducer == null) {
			return false;
		}
		ExportArea.Source source = resolveSource(view);
		return explicitProducer == Producer.MANUAL
				? source != ExportArea.Source.MANUAL
				: source != ExportArea.Source.EXPORT_POINTS_EXPLICIT;
	}

	private ExportArea.Source resolveSource(EuclidianView view) {
		ExportArea area = resolve(view);
		return area == null ? null : area.getSource();
	}

	/**
	 * Defines and activates the MANUAL producer. An inverted rectangle is
	 * normalized; a zero-area or non-finite one is refused and the previous
	 * state is kept.
	 *
	 * @param sourceViewId view id of the 2D source view
	 * @param x1 first world x
	 * @param x2 second world x
	 * @param y1 first world y
	 * @param y2 second world y
	 * @return whether the area was accepted
	 */
	public boolean defineManual(int sourceViewId, double x1, double x2,
			double y1, double y2) {
		ExportArea area = ExportArea.of(x1, x2, y1, y2, sourceViewId,
				ExportArea.Source.MANUAL);
		if (area == null) {
			return false;
		}
		manualArea = area;
		explicitProducer = Producer.MANUAL;
		changed();
		return true;
	}

	/**
	 * Activates EXPORT_POINTS explicitly; refused while the points do not form
	 * a valid rectangle. The session holds one area, so an earlier MANUAL
	 * rectangle is discarded.
	 *
	 * @param sourceViewId view id of the 2D source view
	 * @return whether it was activated
	 */
	public boolean useExportPoints(int sourceViewId) {
		if (exportPoints(sourceViewId, ExportArea.Source.EXPORT_POINTS_EXPLICIT) == null) {
			return false;
		}
		manualArea = null;
		explicitProducer = Producer.EXPORT_POINTS;
		changed();
		return true;
	}

	/** Back to the automatic resolution; the MANUAL rectangle is discarded. */
	public void clear() {
		explicitProducer = null;
		manualArea = null;
		changed();
	}

	/** New and Open: no explicit producer, no MANUAL rectangle, overlay hidden. */
	public void resetForDocument() {
		explicitProducer = null;
		manualArea = null;
		overlayShown = false;
		changed();
	}

	/** @param shown whether the non-exported overlay is shown */
	public void setOverlayShown(boolean shown) {
		if (overlayShown != shown) {
			overlayShown = shown;
			changed();
		}
	}

	/** @return whether the non-exported overlay is shown */
	public boolean isOverlayShown() {
		return overlayShown;
	}

	/** @return explicitly activated producer, or null */
	public Producer getExplicitProducer() {
		return explicitProducer;
	}

	/** @return stored MANUAL rectangle, or null */
	public ExportArea getManualArea() {
		return manualArea;
	}

	/**
	 * @param viewId view id
	 * @param source source to record
	 * @return Export_1/Export_2 area, or null when they are not two finite
	 *         points spanning a non-zero rectangle
	 */
	ExportArea exportPoints(int viewId, ExportArea.Source source) {
		GeoPoint first = finitePoint(kernel.lookupLabel(EuclidianView.EXPORT1));
		GeoPoint second = finitePoint(kernel.lookupLabel(EuclidianView.EXPORT2));
		if (first == null || second == null) {
			return null;
		}
		return ExportArea.of(first.getInhomX(), second.getInhomX(), first.getInhomY(),
				second.getInhomY(), viewId, source);
	}

	private static GeoPoint finitePoint(GeoElement geo) {
		if (!(geo instanceof GeoPoint) || !geo.isDefined()) {
			return null;
		}
		GeoPoint point = (GeoPoint) geo;
		return Double.isFinite(point.getInhomX()) && Double.isFinite(point.getInhomY())
				? point : null;
	}

	/**
	 * Visible viewport; a view that is not laid out yet (command line) uses the
	 * size and coordinate system stored in its settings (DQ-B9).
	 *
	 * @param view 2D view
	 * @return its visible viewport area, or null
	 */
	public static ExportArea visibleViewportOf(EuclidianView view) {
		if (view.getWidth() > 0 && view.getHeight() > 0) {
			return ExportArea.of(view.getXmin(), view.getXmax(), view.getYmin(),
					view.getYmax(), view.getViewID(), ExportArea.Source.VISIBLE_VIEWPORT);
		}
		EuclidianSettings settings = view.getSettings();
		GDimension size = settings == null ? null : settings.getPreferredSize();
		if (size == null || size.getWidth() <= 0 || size.getHeight() <= 0
				|| !(settings.getXscale() > 0) || !(settings.getYscale() > 0)) {
			return null;
		}
		double xmin = -settings.getXZero() / settings.getXscale();
		double xmax = (size.getWidth() - settings.getXZero()) / settings.getXscale();
		double ymax = settings.getYZero() / settings.getYscale();
		double ymin = (settings.getYZero() - size.getHeight()) / settings.getYscale();
		return ExportArea.of(xmin, xmax, ymin, ymax, view.getViewID(),
				ExportArea.Source.VISIBLE_VIEWPORT);
	}

	private void changed() {
		for (Runnable listener : listeners) {
			listener.run();
		}
	}
}
