/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.sheet.AlgoIsoABorder;
import org.geogebra.common.awt.GDimension;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoPolyLine;
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
 *
 * <p>PRE-G9B-R6-plus-E3 adds two explicit producers of the same authority:
 * {@code ISO_A_SELECTION}, an ISO A sized rectangle computed once and stored as
 * values, and {@code ISO_A_BORDER}, a live link to the paper boundary of one
 * {@code IsoABorder} (DQ-E3-8 to DQ-E3-10). The link is valid only while an
 * identity scan of the live construction still finds the linked paper boundary
 * as output 0 of the linked algorithm; a stale link is dropped, never re-resolved
 * by label, index, coordinate or position, and the B fallback applies.
 */
public final class ExportAreaSession {
	/** Explicit producers of the single export-area authority. */
	public enum Producer {
		/** live from the document points Export_1 and Export_2 */
		EXPORT_POINTS,
		/** a stored world rectangle */
		MANUAL,
		/** an ISO A sized world rectangle computed once (PRE-G9B-R6-plus-E3) */
		ISO_A_SELECTION,
		/** the live paper boundary of one IsoABorder (PRE-G9B-R6-plus-E3) */
		ISO_A_BORDER
	}

	private final Kernel kernel;
	private final List<Runnable> listeners = new ArrayList<>();
	private Producer explicitProducer;
	private ExportArea manualArea;
	private boolean overlayShown;
	private AlgoIsoABorder linkedBorder;
	private GeoPolyLine linkedPaper;
	private int linkedViewId;
	private boolean linkLost;

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
		if ((explicitProducer == Producer.MANUAL
				|| explicitProducer == Producer.ISO_A_SELECTION) && manualArea != null
				&& manualArea.getSourceViewId() == viewId) {
			return manualArea;
		}
		if (explicitProducer == Producer.ISO_A_BORDER && linkedViewId == viewId) {
			ExportArea border = borderArea();
			if (border != null) {
				return border;
			}
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
		switch (explicitProducer) {
		case MANUAL:
			return source != ExportArea.Source.MANUAL;
		case ISO_A_SELECTION:
			return source != ExportArea.Source.ISO_A_SELECTION;
		case ISO_A_BORDER:
			return source != ExportArea.Source.ISO_A_BORDER;
		case EXPORT_POINTS:
		default:
			return source != ExportArea.Source.EXPORT_POINTS_EXPLICIT;
		}
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
		return defineStored(Producer.MANUAL, ExportArea.Source.MANUAL, sourceViewId, x1,
				x2, y1, y2);
	}

	/**
	 * PRE-G9B-R6-plus-E3: defines and activates the {@code ISO_A_SELECTION} producer
	 * from a rectangle the caller computed once; refused like
	 * {@link #defineManual}.
	 *
	 * @param sourceViewId view id of the 2D source view
	 * @param x1 first world x
	 * @param x2 second world x
	 * @param y1 first world y
	 * @param y2 second world y
	 * @return whether the area was accepted
	 */
	public boolean defineIsoASelection(int sourceViewId, double x1, double x2, double y1,
			double y2) {
		return defineStored(Producer.ISO_A_SELECTION, ExportArea.Source.ISO_A_SELECTION,
				sourceViewId, x1, x2, y1, y2);
	}

	private boolean defineStored(Producer producer, ExportArea.Source source,
			int sourceViewId, double x1, double x2, double y1, double y2) {
		ExportArea area = ExportArea.of(x1, x2, y1, y2, sourceViewId, source);
		if (area == null) {
			return false;
		}
		releaseLink();
		manualArea = area;
		explicitProducer = producer;
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
		releaseLink();
		manualArea = null;
		explicitProducer = Producer.EXPORT_POINTS;
		changed();
		return true;
	}

	/**
	 * PRE-G9B-R6-plus-E3: links the paper boundary of one sheet as the explicit
	 * producer, after the user's explicit consent. Refused while the paper boundary is
	 * undefined or the sheet is not part of the live construction.
	 *
	 * @param sourceViewId view id of the 2D source view
	 * @param border the sheet
	 * @return whether the link was activated
	 */
	public boolean useIsoABorder(int sourceViewId, AlgoIsoABorder border) {
		if (border == null || !isLive(border, border.getPaper())
				|| border.getPaperBounds() == null) {
			return false;
		}
		manualArea = null;
		explicitProducer = Producer.ISO_A_BORDER;
		linkedBorder = border;
		linkedPaper = border.getPaper();
		linkedViewId = sourceViewId;
		linkLost = false;
		changed();
		return true;
	}

	/**
	 * Drops a stale {@code ISO_A_BORDER} link (deleted, rebuilt by undo, redo or a
	 * rebuilding redefinition, reloaded or replaced): the explicit producer is cleared,
	 * the B fallback applies and {@link #isLinkLost()} reports it until the next
	 * explicit choice. An undefined but live sheet keeps its link.
	 *
	 * @return whether a stale link was dropped
	 */
	public boolean validateLink() {
		if (explicitProducer != Producer.ISO_A_BORDER || isLive(linkedBorder, linkedPaper)) {
			return false;
		}
		releaseLink();
		explicitProducer = null;
		linkLost = true;
		changed();
		return true;
	}

	private ExportArea borderArea() {
		if (validateLink()) {
			return null;
		}
		double[] bounds = linkedBorder.getPaperBounds();
		return bounds == null ? null : ExportArea.of(bounds[0], bounds[1], bounds[2],
				bounds[3], linkedViewId, ExportArea.Source.ISO_A_BORDER);
	}

	/**
	 * Identity test only: the paper boundary must still be output 0 of the algorithm,
	 * that algorithm its parent, and the very same Java object a member of the live
	 * construction (an identity scan; {@code isInConstructionList} answers true for
	 * replaced objects and a sorted-set lookup would compare construction indices).
	 */
	private boolean isLive(AlgoIsoABorder border, GeoPolyLine paper) {
		if (border == null || paper == null || border.getOutputLength() == 0
				|| border.getOutput(AlgoIsoABorder.PAPER) != paper
				|| paper.getParentAlgorithm() != border) {
			return false;
		}
		for (GeoElement geo : kernel.getConstruction().getGeoSetConstructionOrder()) {
			if (geo == paper) {
				return true;
			}
		}
		return false;
	}

	private void releaseLink() {
		linkedBorder = null;
		linkedPaper = null;
		linkedViewId = 0;
		linkLost = false;
	}

	/** Back to the automatic resolution; the MANUAL rectangle is discarded. */
	public void clear() {
		releaseLink();
		explicitProducer = null;
		manualArea = null;
		changed();
	}

	/** New and Open: no explicit producer, no MANUAL rectangle, overlay hidden. */
	public void resetForDocument() {
		releaseLink();
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

	/** @return stored MANUAL or ISO_A_SELECTION rectangle, or null */
	public ExportArea getManualArea() {
		return manualArea;
	}

	/**
	 * @return the linked sheet while its link is live, or null (a stale link is
	 *         dropped first)
	 */
	public AlgoIsoABorder getLinkedBorder() {
		validateLink();
		return explicitProducer == Producer.ISO_A_BORDER ? linkedBorder : null;
	}

	/** @return whether the last ISO_A_BORDER link was dropped as stale */
	public boolean isLinkLost() {
		return linkLost;
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
