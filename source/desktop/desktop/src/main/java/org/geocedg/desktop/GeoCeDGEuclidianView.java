/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.desktop.export.ExportArea;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.awt.GBasicStroke;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.awt.GPoint2D;
import org.geogebra.common.awt.GRectangle2D;
import org.geogebra.common.euclidian.EuclidianController;
import org.geogebra.common.euclidian.Hits;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.App;
import org.geogebra.common.main.settings.EuclidianSettings;
import org.geogebra.desktop.euclidian.EuclidianStyleBarD;
import org.geogebra.desktop.geogebra3D.euclidianFor3D.EuclidianViewFor3DD;

/** GeoCeDG Desktop view with live presentation overlays. */
public final class GeoCeDGEuclidianView extends EuclidianViewFor3DD {
	private static final GColor EXPORT_AREA_OVERLAY = GColor.newColor(230, 120, 0);
	private static final GColor INTERSECTION_BADGE_BACKGROUND = GColor.newColor(0, 90, 125);
	private GeoCeDGControlLayoutWorkspace controlLayout;

	/** Creates the regular Desktop view with a non-semantic overlay. */
	public GeoCeDGEuclidianView(EuclidianController controller, boolean[] axes,
			boolean grid, int number, EuclidianSettings settings) {
		super(controller, axes, grid, number, settings);
		getJPanel().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				if (controller instanceof GeoCeDGEuclidianController) {
					((GeoCeDGEuclidianController) controller)
							.invalidateNavigationCursorContext();
				}
				if (controlLayout != null) {
					controlLayout.cancelDrag();
				}
			}
		});
	}

	GeoCeDGControlLayoutWorkspace controlLayoutWorkspace() {
		if (controlLayout == null && getViewID() == App.VIEW_EUCLIDIAN
				&& getApplication() instanceof AppGeoCeDG app) {
			controlLayout = new GeoCeDGControlLayoutWorkspace(app, this);
		}
		return controlLayout;
	}

	void closeControlLayout() {
		if (controlLayout != null) {
			controlLayout.close();
		}
	}

	@Override
	public GPoint2D getControlLayoutPosition(GeoElement geo) {
		var workspace = controlLayoutWorkspace();
		return workspace == null ? null : workspace.position(geo);
	}

	@Override
	public double getControlLayoutSliderWidth(GeoNumeric slider) {
		var workspace = controlLayoutWorkspace();
		return workspace == null ? 0 : workspace.sliderWidth(slider);
	}

	@Override
	public boolean isControlLayoutVisible(GeoElement geo) {
		var workspace = controlLayoutWorkspace();
		return workspace == null || workspace.visible(geo);
	}

	@Override
	protected boolean drawInControlLayout(GeoElement geo) {
		var workspace = controlLayoutWorkspace();
		return workspace != null && workspace.deferred(geo);
	}

	@Override
	public void drawActionObjectsOnShownLayers(GGraphics2D graphics) {
		super.drawActionObjectsOnShownLayers(graphics);
		var workspace = controlLayoutWorkspace();
		if (workspace != null) {
			workspace.paint(graphics);
		}
	}

	@Override
	protected void filterPresentationHits(GPoint point, Hits hits) {
		var workspace = controlLayoutWorkspace();
		if (workspace != null) {
			workspace.filterHits(point, hits);
		}
	}

	@Override
	protected EuclidianStyleBarD newEuclidianStyleBar() {
		// PRE-G9B-R6-plus-F4-A: numeric line-thickness entry in the style bar
		return new GeoCeDGEuclidianStyleBar(this);
	}

	/**
	 * PRE-G9B-R6-plus-B transient overlay of the effective export area. Only the
	 * live paint draws it; exports paint an export viewport through
	 * {@code exportPaint}, which never calls this method.
	 */
	private void paintExportAreaOverlay(GGraphics2D graphics) {
		if (!(getApplication() instanceof AppGeoCeDG app) || !app.isExportAreaOverlayShown()) {
			return;
		}
		ExportArea area = app.getExportAreaSession().resolve(this);
		if (area == null || area.getSource() == ExportArea.Source.VISIBLE_VIEWPORT) {
			return;
		}
		double left = toScreenCoordXd(area.getXmin());
		double top = toScreenCoordYd(area.getYmax());
		double right = toScreenCoordXd(area.getXmax());
		double bottom = toScreenCoordYd(area.getYmin());
		graphics.setColor(EXPORT_AREA_OVERLAY);
		graphics.setStroke(AwtFactory.getPrototype().newBasicStroke(2,
				GBasicStroke.CAP_BUTT, GBasicStroke.JOIN_MITER, 10,
				new double[] { 8, 4 }));
		GRectangle2D outline = AwtFactory.getPrototype().newRectangle2D();
		outline.setRect(left, top, right - left, bottom - top);
		graphics.draw(outline);
	}

	@Override
	public void paint(GGraphics2D graphics) {
		super.paint(graphics);
		if (getApplication() instanceof AppGeoCeDG app) {
			// PRE-G9B-R6-plus-E3: every rebuild repaints, so a stale ISO_A_BORDER link
			// (undo, redo, deletion, reload) is dropped here by its identity test
			app.getExportAreaSession().validateLink();
		}
		paintExportAreaOverlay(graphics);
		if (getApplication() instanceof AppGeoCeDG app && app.isOrientationCueShown()) {
			// PRE-G9B-R6-plus-F1: kernel orientation cue, live Graphics 1 paint only;
			// exportPaint never calls this method, so no export or print shows it
			GeoCeDGOrientationCueRenderer.paint(this, graphics);
		}
		paintIndexedIntersections(graphics);
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

	private void paintIndexedIntersections(GGraphics2D graphics) {
		var app = getApplication();
		int size = Math.max(14, (int) Math.ceil(getFontPoint().getSize()) + 5);
		for (GeoElement geo : app.getSelectionManager().getSelectedGeos()) {
			int index = indexedIntersectionSlot(geo);
			var drawable = getDrawableFor(geo);
			if (index == 0 || !geo.isEuclidianVisible() || !isVisibleInThisView(geo)
					|| !app.isLayerShown(geo.getLayer())
					|| !geo.isAvailableAtConstructionStep(app.getKernel().getConstructionStep())
					|| drawable == null || drawable.getGeoElement() != geo) {
				continue;
			}
			GeoPoint point = (GeoPoint) geo;
			int x = toScreenCoordX(point.getInhomX());
			int y = toScreenCoordY(point.getInhomY());
			if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
				continue;
			}
			int left = Math.min(x + 7, Math.max(0, getWidth() - size));
			// Both labelled slots can remain defined at tangency and share the same point.
			int top = index == 1 ? Math.max(0, Math.min(y - size - 3, getHeight() - 2 * size))
					: Math.max(0, Math.min(getHeight() - size, Math.max(size, y + 3)));
			graphics.setFont(getFontPoint());
			graphics.setColor(INTERSECTION_BADGE_BACKGROUND);
			graphics.fillRect(left, top, size, size);
			graphics.setColor(GColor.WHITE);
			graphics.drawString(Integer.toString(index), left + 3, top + size - 3);
		}
	}

	/** Returns the current one-based command slot, or zero for an ineligible output. */
	static int indexedIntersectionSlot(GeoElement geo) {
		if (!(geo instanceof GeoPoint point) || !point.isDefined() || !point.isFinite()
				|| !(point.getParentAlgorithm() instanceof AlgoIntersectLineConic intersection)) {
			return 0;
		}
		GeoPoint[] slots = intersection.getIntersectionPoints();
		for (int index = 0; index < slots.length; index++) {
			if (slots[index] == point) {
				return index + 1;
			}
		}
		return 0;
	}
}
