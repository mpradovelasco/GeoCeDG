/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionStatus2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.euclidian.Drawable;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianCursor;
import org.geogebra.common.euclidian.Hits;
import org.geogebra.common.euclidian.event.AbstractEvent;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.ModeSetter;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.MyError;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.desktop.geogebra3D.euclidianFor3D.EuclidianControllerFor3DD;
import org.geogebra.desktop.main.AppD;

/** GeoCeDG-only orchestration of existing semantic operations and view gestures. */
public final class GeoCeDGEuclidianController
		extends EuclidianControllerFor3DD {

	private final GeoCeDGLocusV2Dialogs locusV2Dialogs;
	private final GeoCeDGIntersectionSession intersectionSession;
	private final GeoCeDGPointInteraction pointInteraction;
	private final GeoCeDGSimilarityTools similarityTools;
	private GeoPoint interactionDragPoint;
	private boolean interactionGesture;
	private boolean interactionChanged;
	private boolean interactionTailCancelled;
	private boolean zoomWindowActive;
	private boolean zoomWindowDragging;
	private int zoomStartX;
	private int zoomStartY;

	/**
	 * @param kernel kernel
	 */
	public GeoCeDGEuclidianController(Kernel kernel) {
		super(kernel);
		intersectionSession = new GeoCeDGIntersectionSession(
				(AppD) kernel.getApplication());
		pointInteraction = new GeoCeDGPointInteraction((AppD) kernel.getApplication());
		similarityTools = new GeoCeDGSimilarityTools((AppD) kernel.getApplication());
		locusV2Dialogs = new GeoCeDGLocusV2Dialogs(
				(AppD) kernel.getApplication(), intersectionSession);
	}

	@Override
	public void wrapMousePressed(AbstractEvent event) {
		interactionTailCancelled = false;
		if (zoomWindowActive && !event.isRightClick()) {
			zoomWindowDragging = true;
			zoomStartX = event.getX();
			zoomStartY = event.getY();
			getView().setSelectionRectangle(null);
			return;
		}
		interactionGesture = false;
		interactionChanged = false;
		interactionDragPoint = null;
		if (!event.isRightClick() && !event.isControlDown()
				&& RuntimeFeatureService.mayCreateLocusV2(kernel.getConstruction())
				&& (mode == EuclidianConstants.MODE_POINT
						|| mode == EuclidianConstants.MODE_POINT_ON_OBJECT
						|| mode == EuclidianConstants.MODE_MOVE)) {
			setMouseLocation(event);
			setViewHits(event.getType());
			Hits hits = getView().getHits();
			List<GeoPoint> ownedPoints = new ArrayList<>();
			boolean ordinaryPointHit = false;
			for (GeoElement hit : hits) {
				if (hit instanceof GeoPoint) {
					if (GeoCeDGPointInteraction.owns((GeoPoint) hit)) {
						ownedPoints.add((GeoPoint) hit);
					} else {
						ordinaryPointHit = true;
					}
				}
			}
			if (!ownedPoints.isEmpty()) {
				interactionDragPoint = locusV2Dialogs.chooseOwnedPoint(ownedPoints);
				interactionGesture = true;
				if (interactionDragPoint != null) {
					app.getSelectionManager().clearSelectedGeos();
					app.getSelectionManager().addSelectedGeo(interactionDragPoint);
					getView().requestFocusInWindow();
					getView().setCursor(EuclidianCursor.DRAG);
				}
				return;
			}
			if (ordinaryPointHit) {
				// Existing ordinary points retain the inherited selection contract.
				super.wrapMousePressed(event);
				return;
			}
			if (mode == EuclidianConstants.MODE_MOVE && inspectMarkerHit(event)) {
				interactionGesture = true;
				return;
			}
			if (mode != EuclidianConstants.MODE_MOVE) {
				List<GeoLocusV2> sources = new ArrayList<>();
				for (GeoElement hit : hits) {
					if (hit instanceof GeoLocusV2
							&& getView().getDrawableFor(hit) instanceof Drawable
							&& ((Drawable) getView().getDrawableFor(hit)).hit(event.getX(),
									event.getY(),
									app.getCapturingThreshold(event.getType()))) {
						sources.add((GeoLocusV2) hit);
					}
				}
				if (!sources.isEmpty()) {
					interactionGesture = true;
					GeoLocusV2 source = locusV2Dialogs.chooseSemanticSource(sources);
					if (source != null) {
						try {
							interactionDragPoint = pointInteraction.create(source,
									getView().toRealWorldCoordX(event.getX()),
									getView().toRealWorldCoordY(event.getY()),
									interactionRadius(event),
									locusV2Dialogs::chooseSemanticPreimage);
							interactionChanged = interactionDragPoint != null;
							boolean created = interactionChanged;
							if (created) {
								app.getSelectionManager().clearSelectedGeos();
								app.getSelectionManager().addSelectedGeo(interactionDragPoint);
							}
							if (interactionChanged && (sources.size() > 1
									|| pointInteraction.getLastResult().getStatus()
									== LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES)) {
								// A modal chooser may consume the canvas release event.
								app.storeUndoInfo();
								interactionChanged = false;
								interactionDragPoint = null;
							}
							if (!created) {
								locusV2Dialogs.showInteractionOutcome(
										pointInteraction.getLastResult(), true);
							}
						} catch (IllegalArgumentException exception) {
							locusV2Dialogs.showInteractionOutcome(null, true);
						}
					}
					kernel.notifyRepaint();
					return;
				}
			}
		}
		super.wrapMousePressed(event);
	}

	@Override
	public void wrapMouseDragged(AbstractEvent event, boolean startCapture) {
		if (interactionTailCancelled) {
			return;
		}
		if (zoomWindowDragging) {
			getView().setSelectionRectangle(AwtFactory.getPrototype().newRectangle(
					Math.min(zoomStartX, event.getX()), Math.min(zoomStartY, event.getY()),
					Math.abs(event.getX() - zoomStartX), Math.abs(event.getY() - zoomStartY)));
			getView().repaintView();
			return;
		}
		if (!interactionGesture) {
			super.wrapMouseDragged(event, startCapture);
			return;
		}
		if (interactionDragPoint != null) {
			try {
				boolean moved = pointInteraction.move(interactionDragPoint,
						getView().toRealWorldCoordX(event.getX()),
						getView().toRealWorldCoordY(event.getY()), interactionRadius(event));
				interactionChanged |= moved;
				getView().setToolTipText(moved ? null
						: locusV2Dialogs.interactionMessage(pointInteraction.getLastResult()));
			} catch (RuntimeException exception) {
				getView().setToolTipText(locusV2Dialogs.interactionMessage(null));
				// A rollback can reconstruct host instances. Never keep dragging an
				// obsolete reference or acquire another point by position.
				interactionDragPoint = null;
			}
			kernel.notifyRepaint();
		}
	}

	@Override
	public void wrapMouseReleased(AbstractEvent event) {
		if (interactionTailCancelled) {
			interactionTailCancelled = false;
			return;
		}
		if (zoomWindowDragging) {
			zoomWindowDragging = false;
			zoomWindowActive = false;
			var rectangle = getView().getSelectionRectangle();
			getView().setSelectionRectangle(null);
			if (rectangle != null && rectangle.getWidth() >= 10
					&& rectangle.getHeight() >= 10) {
				// Explicit ZoomWindow is independent of the optional Shift-drag gesture.
				getView().setAnimatedRealWorldCoordSystem(
						getView().toRealWorldCoordX(rectangle.getMinX()),
						getView().toRealWorldCoordX(rectangle.getMaxX()),
						getView().toRealWorldCoordY(rectangle.getMaxY()),
						getView().toRealWorldCoordY(rectangle.getMinY()), 15, true);
			}
			getView().setCursor(EuclidianCursor.DEFAULT);
			return;
		}
		if (!interactionGesture) {
			super.wrapMouseReleased(event);
			return;
		}
		if (interactionChanged) {
			app.storeUndoInfo();
		}
		interactionGesture = false;
		interactionChanged = false;
		interactionDragPoint = null;
		getView().setToolTipText(null);
		getView().setCursor(EuclidianCursor.DEFAULT);
		kernel.notifyRepaint();
	}

	private double interactionRadius(AbstractEvent event) {
		return Math.max(1, app.getCapturingThreshold(event.getType()))
				* Math.hypot(getView().getInvXscale(), getView().getInvYscale());
	}

	/** Arms a frontend-only rectangle gesture using inherited view navigation. */
	public void activateZoomWindow() {
		app.setMode(EuclidianConstants.MODE_MOVE);
		zoomWindowActive = true;
		getView().setCursor(EuclidianCursor.ZOOM_IN);
	}

	/** @return whether the next drag defines a zoom rectangle */
	public boolean isZoomWindowActive() {
		return zoomWindowActive;
	}

	@Override
	public void setMode(int newMode, ModeSetter setter) {
		if (interactionGesture) {
			if (interactionChanged) {
				app.storeUndoInfo();
			}
			interactionGesture = false;
			interactionChanged = false;
			interactionDragPoint = null;
			// Discard the remaining events from this press after a tool change.
			// They must not drive either the old semantic point or the new tool.
			interactionTailCancelled = true;
			if (getView() != null) {
				getView().setToolTipText(null);
				getView().setCursor(EuclidianCursor.DEFAULT);
			}
		}
		if (similarityTools != null) {
			similarityTools.reset();
		}
		if ((zoomWindowActive || zoomWindowDragging) && getView() != null) {
			getView().setSelectionRectangle(null);
		}
		zoomWindowActive = false;
		zoomWindowDragging = false;
		super.setMode(newMode, setter);
	}

	private boolean inspectMarkerHit(AbstractEvent event) {
		if (!intersectionSession.isMarkersVisible()) {
			return false;
		}
		List<LocusIntersectionSolution2D> hits = new ArrayList<>();
		int threshold = Math.max(5, app.getCapturingThreshold(event.getType()));
		for (LocusIntersectionSolution2D solution : intersectionSession.markerSolutions()) {
			var point = solution.getEvaluatedPoint();
			if (point != null && Math.abs(getView().toScreenCoordXd(point.getX())
					- event.getX()) <= threshold
					&& Math.abs(getView().toScreenCoordYd(point.getY())
							- event.getY()) <= threshold) {
				hits.add(solution);
			}
		}
		if (hits.isEmpty()) {
			return false;
		}
		LocusIntersectionSolution2D selected = locusV2Dialogs.chooseMarkerSolution(hits);
		if (selected != null) {
			intersectionSession.select(List.of(selected.getIdentity().getRootToken()));
			// Only presentation hit testing occurred; no new solve or identity inference.
			locusV2Dialogs.inspectActiveIntersection();
		}
		return true;
	}

	@Override
	protected boolean switchModeForProcessMode(Hits hits, boolean controlDown,
			boolean shiftDown, AsyncOperation<Boolean> callback,
			boolean selectionPreview) {
		if (similarityTools.handles(mode, hits)) {
			GeoLocusV2 image = similarityTools.click(mode, hits, selectionPreview);
			if (!selectionPreview && !similarityTools.isSelecting()) {
				clearSelections();
			}
			return endOfSwitchModeForProcessMode(image == null ? null
					: new GeoElementND[] {image}, false, callback, selectionPreview);
		}
		if (!isLocusV2Mode(mode)) {
			if (mode == EuclidianConstants.MODE_INTERSECT && !selectionPreview
					&& callback != null && intersectionSession.isAutoMaterialize()) {
				GeoLocusIntersectionResult previous = intersectionSession.getActive();
				return super.switchModeForProcessMode(hits, controlDown, shiftDown,
						changed -> {
							GeoLocusIntersectionResult created = intersectionSession.getActive();
							// The explicit opt-in mutation belongs to the same user
							// action; the host stores its single compound undo below.
							if (Boolean.TRUE.equals(changed) && created != previous
									&& created != null
									&& intersectionSession.getActive() == created) {
								intersectionSession.materializeAll(false);
							}
							callback.callback(changed);
						}, false);
			}
			return super.switchModeForProcessMode(hits, controlDown, shiftDown,
					callback, selectionPreview);
		}
		GeoElementND[] output = null;
		Construction construction = kernel.getConstruction();
		if (!RuntimeFeatureService.mayCreateLocusV2(construction)) {
			showFeatureError("LocusV2.FeatureDisabled", selectionPreview);
		} else {
			switch (mode) {
			case EuclidianConstants.MODE_LOCUS_V2:
				output = createPointDrivenLocus(hits, selectionPreview);
				break;
			case EuclidianConstants.MODE_LOCUS_V2_POINT:
				output = createSemanticPoint(hits, selectionPreview);
				break;
			case EuclidianConstants.MODE_LOCUS_V2_LENGTH:
				output = createTotalMetric(hits, selectionPreview);
				break;
			case EuclidianConstants.MODE_LOCUS_V2_LENGTH_BETWEEN:
				output = createBetweenMetric(hits, selectionPreview);
				break;
			default:
				break;
			}
		}
		return endOfSwitchModeForProcessMode(output, false, callback,
				selectionPreview);
	}

	@Override
	protected GeoElementND[] intersect(Hits hits, boolean selectionPreview) {
		if (!containsLocusV2(hits)) {
			return super.intersect(hits, selectionPreview);
		}
		Construction construction = kernel.getConstruction();
		if (!RuntimeFeatureService.mayCreateLocusV2(construction)) {
			showFeatureError("LocusV2.FeatureDisabled", selectionPreview);
			return null;
		}
		addSelectedGeo(hits, 2, false, selectionPreview);
		if (selGeos() < 2 || selectionPreview) {
			return null;
		}
		GeoElement[] selected = getSelectedGeos();
		GeoLocusV2 source = selected[0] instanceof GeoLocusV2
				? (GeoLocusV2) selected[0]
				: selected[1] instanceof GeoLocusV2
						? (GeoLocusV2) selected[1] : null;
		if (source == null) {
			return null;
		}
		GeoElement target = source == selected[0] ? selected[1] : selected[0];
		try {
			GeoLocusIntersectionResult rich = ensureLabel(LocusV2PublicOperations.intersect(
					construction, null, source, target));
			intersectionSession.activate(rich);
			return new GeoElementND[] {rich};
		} catch (IllegalArgumentException exception) {
			showFeatureError("LocusV2.UnsupportedTarget", selectionPreview);
			return null;
		}
	}

	private GeoElementND[] createPointDrivenLocus(Hits hits,
			boolean selectionPreview) {
		if (hits.isEmpty()) {
			return null;
		}
		addSelectedPoint(hits, 2, false, selectionPreview);
		if (selPoints() < 2 || selectionPreview) {
			return null;
		}
		GeoPointND[] selected = getSelectedPointsND();
		if (!(selected[0] instanceof GeoPoint)
				|| !(selected[1] instanceof GeoPoint)) {
			showFeatureError("LocusV2.UnsupportedGenerator", selectionPreview);
			return null;
		}
		GeoPoint dependent = (GeoPoint) selected[0];
		GeoPoint driver = (GeoPoint) selected[1];
		if (!dependent.getAllPredecessors().contains(driver)) {
			clearSelections();
			showFeatureError("LocusV2.UnsupportedGenerator", selectionPreview);
			return null;
		}
		try {
			return new GeoElementND[] {ensureLabel(
					LocusV2PublicOperations.createPointDriven(
					kernel.getConstruction(), null, dependent, driver))};
		} catch (IllegalArgumentException exception) {
			clearSelections();
			showFeatureError("LocusV2.UnsupportedGenerator", selectionPreview);
			return null;
		}
	}

	private GeoElementND[] createSemanticPoint(Hits hits,
			boolean selectionPreview) {
		GeoLocusV2 source = selectSingleLocus(hits, selectionPreview);
		if (source == null || selectionPreview) {
			return null;
		}
		GeoPoint point = locusV2Dialogs.createSemanticPoint(source);
		return point == null ? null : new GeoElementND[] {ensureLabel(point)};
	}

	private GeoElementND[] createTotalMetric(Hits hits,
			boolean selectionPreview) {
		GeoLocusV2 source = selectSingleLocus(hits, selectionPreview);
		if (source == null || selectionPreview) {
			return null;
		}
		try {
			GeoLocusMetricResult result =
					ensureLabel(LocusV2PublicOperations.totalMetric(
							kernel.getConstruction(), null, source));
			locusV2Dialogs.inspectMetric(result);
			return new GeoElementND[] {result};
		} catch (IllegalArgumentException exception) {
			showFeatureError("LocusV2.MetricUnavailable", false);
			return null;
		}
	}

	private GeoElementND[] createBetweenMetric(Hits hits,
			boolean selectionPreview) {
		if (hits.isEmpty()) {
			return null;
		}
		addSelectedGeo(hits, 3, false, selectionPreview);
		if (selGeos() < 3 || selectionPreview) {
			return null;
		}
		GeoElement[] selected = getSelectedGeos();
		GeoLocusV2 source = null;
		GeoPoint[] positions = new GeoPoint[2];
		int positionCount = 0;
		for (GeoElement geo : selected) {
			if (geo instanceof GeoLocusV2 && source == null) {
				source = (GeoLocusV2) geo;
			} else if (geo instanceof GeoPoint && positionCount < 2) {
				positions[positionCount++] = (GeoPoint) geo;
			} else {
				showFeatureError("LocusV2.InvalidPosition", false);
				return null;
			}
		}
		if (source == null || positionCount != 2) {
			showFeatureError("LocusV2.InvalidPosition", false);
			return null;
		}
		try {
			GeoLocusMetricResult result =
					ensureLabel(LocusV2PublicOperations.betweenMetric(
							kernel.getConstruction(), null, source,
							positions[0], positions[1]));
			locusV2Dialogs.inspectMetric(result);
			return new GeoElementND[] {result};
		} catch (IllegalArgumentException exception) {
			showFeatureError("LocusV2.InvalidPosition", false);
			return null;
		}
	}

	private GeoLocusV2 selectSingleLocus(Hits hits,
			boolean selectionPreview) {
		if (hits.isEmpty()) {
			return null;
		}
		addSelectedGeo(hits, 1, false, selectionPreview);
		if (selGeos() < 1 || selectionPreview) {
			return null;
		}
		GeoElement[] selected = getSelectedGeos();
		if (selected.length == 1 && selected[0] instanceof GeoLocusV2) {
			return (GeoLocusV2) selected[0];
		}
		showFeatureError("LocusV2.UnsupportedGenerator", false);
		return null;
	}

	/** Opens the rich inspector and records an exact-token point, if chosen. */
	public void inspectRichResultSelection() {
		Construction construction = kernel.getConstruction();
		if (!RuntimeFeatureService.mayCreateLocusV2(construction)) {
			showFeatureError("LocusV2.FeatureDisabled", false);
			return;
		}
		GeoPoint point = locusV2Dialogs.inspectRichResult();
		if (point != null) {
			ensureLabel(point);
			app.storeUndoInfo();
			kernel.notifyRepaint();
		}
	}

	/** @return selected or still-live inspected rich intersection, without a solve */
	public boolean hasActiveIntersectionResult() {
		for (GeoElement selected : app.getSelectionManager().getSelectedGeos()) {
			if (selected instanceof GeoLocusIntersectionResult) {
				intersectionSession.activate((GeoLocusIntersectionResult) selected);
				break;
			}
		}
		return intersectionSession.getActive() != null;
	}

	/** @return whether at least one current unmaterialized exact token is eligible */
	public boolean hasEligibleIntersectionSolutions() {
		return hasActiveIntersectionResult() && !intersectionSession.eligibleTokens().isEmpty();
	}

	/** @return whether the explicit selection can be materialized in the requested form */
	public boolean hasSelectedIntersectionSolutions(boolean single) {
		return hasActiveIntersectionResult() && intersectionSession.hasEligibleSelection(single);
	}

	/** Creates exactly one explicitly selected exact-token solution. */
	public void materializeSelectedIntersectionSolution() {
		materializeSelected(true);
	}

	/** Creates a coherent undoable batch of explicitly selected exact-token solutions. */
	public void materializeSelectedIntersectionSolutions() {
		materializeSelected(false);
	}

	private void materializeSelected(boolean single) {
		if (hasActiveIntersectionResult()) {
			try {
				if (intersectionSession.materializeSelected(single, true).isEmpty()) {
					inspectRichResultSelection();
				}
			} catch (IllegalArgumentException exception) {
				locusV2Dialogs.showStaleIntersection();
			}
		}
	}

	/** Creates every currently eligible exact-token solution, in one undo transaction. */
	public void materializeAllEligibleIntersectionSolutions() {
		if (hasActiveIntersectionResult()) {
			try {
				intersectionSession.materializeAll(true);
			} catch (IllegalArgumentException exception) {
				locusV2Dialogs.showStaleIntersection();
			}
		}
	}

	/** Controls transient presentation only. */
	public void setIntersectionMarkersVisible(boolean visible) {
		hasActiveIntersectionResult();
		intersectionSession.setMarkersVisible(visible);
		kernel.notifyRepaint();
	}

	/** @return transient marker preference */
	public boolean isIntersectionMarkersVisible() {
		return intersectionSession.isMarkersVisible();
	}

	/** Controls new explicit frontend queries only; never a recompute listener. */
	public void setAutoMaterializeIntersectionSolutions(boolean enabled) {
		intersectionSession.setAutoMaterialize(enabled);
	}

	/** @return explicit frontend session preference */
	public boolean isAutoMaterializeIntersectionSolutions() {
		return intersectionSession.isAutoMaterialize();
	}

	/**
	 * Explicit Algebra submission has created this new rich owner. The caller
	 * stores one compound undo after this opted-in frontend mutation. This is
	 * not called by recompute, load, scripts or selection.
	 * @param rich newly created query returned by the ordinary command
	 */
	public void afterExplicitIntersectionCreation(GeoLocusIntersectionResult rich) {
		intersectionSession.activate(rich);
		if (intersectionSession.isAutoMaterialize()) {
			intersectionSession.materializeAll(false);
		}
	}

	GeoCeDGIntersectionSession getIntersectionSession() {
		return intersectionSession;
	}

	private static boolean isLocusV2Mode(int currentMode) {
		return currentMode == EuclidianConstants.MODE_LOCUS_V2
				|| currentMode == EuclidianConstants.MODE_LOCUS_V2_POINT
				|| currentMode == EuclidianConstants.MODE_LOCUS_V2_LENGTH
				|| currentMode
						== EuclidianConstants.MODE_LOCUS_V2_LENGTH_BETWEEN;
	}

	private static <T extends GeoElement> T ensureLabel(T output) {
		if (!output.isLabelSet()) {
			output.setLabel(null);
		}
		return output;
	}

	private boolean containsLocusV2(Hits hits) {
		for (GeoElement hit : hits) {
			if (hit instanceof GeoLocusV2) {
				return true;
			}
		}
		for (GeoElement selected : getSelectedGeoList()) {
			if (selected instanceof GeoLocusV2) {
				return true;
			}
		}
		return false;
	}

	private void showFeatureError(String key, boolean selectionPreview) {
		if (!selectionPreview && app.isErrorDialogsActive()) {
			app.showError(new MyError(app.getLocalization(),
					app.getLocalization().getMenu(key)));
		}
	}
}
