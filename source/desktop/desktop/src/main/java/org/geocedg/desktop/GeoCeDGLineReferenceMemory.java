/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.EventQueue;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoLinePointVector;
import org.geogebra.common.kernel.algos.AlgoOrthoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoOrthoLinePointVector;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoVector;
import org.geogebra.common.main.App;
import org.geogebra.common.plugin.Event;
import org.geogebra.common.plugin.EventListener;

/**
 * Session memory of the Parallel and Perpendicular tools of Graphics View 1
 * (geocedg/specs/ui/authoring-memory.md). Holds exact object references only:
 * currentness is exact-instance membership in the construction order, never a
 * label, {@code isInConstructionList} or {@code isLabelSet}. Not serialized; never
 * an undo point.
 */
final class GeoCeDGLineReferenceMemory implements EventListener {

	/** One independent slot per tool. */
	enum Slot {
		PARALLEL(AlgoLinePointLine.class, AlgoLinePointVector.class),
		PERPENDICULAR(AlgoOrthoLinePointLine.class, AlgoOrthoLinePointVector.class);

		private final Class<? extends AlgoElement> lineAlgorithm;
		private final Class<? extends AlgoElement> vectorAlgorithm;

		Slot(Class<? extends AlgoElement> lineAlgorithm,
				Class<? extends AlgoElement> vectorAlgorithm) {
			this.lineAlgorithm = lineAlgorithm;
			this.vectorAlgorithm = vectorAlgorithm;
		}

		/** @return whether the algorithm is one of this tool's admitted 2D constructions */
		boolean admits(AlgoElement algo) {
			return algo != null && (algo.getClass() == lineAlgorithm
					|| algo.getClass() == vectorAlgorithm);
		}

		/**
		 * @param mode tool mode
		 * @return the slot of the tool, or null for any other mode
		 */
		static Slot forMode(int mode) {
			if (mode == EuclidianConstants.MODE_PARALLEL) {
				return PARALLEL;
			}
			if (mode == EuclidianConstants.MODE_ORTHOGONAL) {
				return PERPENDICULAR;
			}
			return null;
		}
	}

	/** Observable state of a slot. */
	enum State {
		/** no current reference */
		EMPTY,
		/** current and usable reference */
		HELD,
		/** current reference, temporarily unusable */
		SUSPENDED,
		/** current reference, introduced after the current protocol step */
		SUSPENDED_BY_STEP,
		/** prior reference lost its identity; selection required */
		INVALIDATED
	}

	private final App app;
	private final Runnable refreshStatus;
	private final Map<Slot, GeoElement> slots = new EnumMap<>(Slot.class);
	private final EnumSet<Slot> invalidated = EnumSet.noneOf(Slot.class);
	private boolean listening;
	private boolean refreshPending;

	/**
	 * @param app application of the Graphics View 1 controller
	 * @param refreshStatus refreshes the current tool indicator on the Swing event thread
	 */
	GeoCeDGLineReferenceMemory(App app, Runnable refreshStatus) {
		this.app = app;
		this.refreshStatus = refreshStatus;
	}

	/**
	 * Admissibility of a reference: a 2D line (line, segment, ray, polygon edge), a 2D
	 * vector, or a function whose kernel linear interpretation is defined
	 * ({@link GeoFunction#getX()} finite; NaN for constant, higher-degree and
	 * non-polynomial functions).
	 *
	 * @param geo candidate
	 * @return whether the object may be remembered and reused
	 */
	static boolean isAdmissible(GeoElement geo) {
		if (geo == null || geo.isGeoElement3D()) {
			return false;
		}
		if (geo instanceof GeoLine || geo instanceof GeoVector) {
			return true;
		}
		return geo instanceof GeoFunction && Double.isFinite(((GeoFunction) geo).getX());
	}

	/**
	 * @param geo object
	 * @return whether this exact instance belongs to the current construction
	 */
	boolean isCurrent(GeoElement geo) {
		if (geo == null) {
			return false;
		}
		Kernel kernel = app.getKernel();
		for (GeoElement member : kernel.getConstruction().getGeoSetConstructionOrder()) {
			if (member == geo) {
				return true;
			}
		}
		return false;
	}

	private boolean isUsable(GeoElement geo) {
		return isAdmissible(geo) && geo.isDefined() && geo.isEuclidianVisible()
				&& geo.isVisibleInView(App.VIEW_EUCLIDIAN)
				&& app.isLayerShown(geo.getLayer());
	}

	/**
	 * @param slot slot
	 * @return the slot state; a reference that is no longer current is invalidated
	 */
	State getState(Slot slot) {
		GeoElement held = slots.get(slot);
		if (held == null) {
			return invalidated.contains(slot) ? State.INVALIDATED : State.EMPTY;
		}
		if (!isCurrent(held)) {
			invalidate(slot);
			return State.INVALIDATED;
		}
		if (!held.isAvailableAtConstructionStep(app.getKernel().getConstructionStep())) {
			return State.SUSPENDED_BY_STEP;
		}
		return isUsable(held) ? State.HELD : State.SUSPENDED;
	}

	/**
	 * @param slot slot
	 * @return the remembered reference if it is current and usable, otherwise null
	 */
	GeoElement getUsable(Slot slot) {
		return getState(slot) == State.HELD ? slots.get(slot) : null;
	}

	/**
	 * @param slot slot
	 * @return the held reference, whatever its state (tests and diagnostics)
	 */
	GeoElement peek(Slot slot) {
		return slots.get(slot);
	}

	/**
	 * Replaces the slot after a certified construction.
	 *
	 * @param slot slot
	 * @param reference the reference used by the created algorithm
	 */
	void remember(Slot slot, GeoElement reference) {
		if (!isAdmissible(reference) || !isCurrent(reference)) {
			return;
		}
		slots.put(slot, reference);
		invalidated.remove(slot);
		if (!listening) {
			app.getEventDispatcher().addEventListener(this);
			listening = true;
		}
		queueStatusRefresh();
	}

	private void invalidate(Slot slot) {
		slots.remove(slot);
		invalidated.add(slot);
	}

	private void invalidateStale() {
		for (Slot slot : Slot.values()) {
			GeoElement held = slots.get(slot);
			if (held != null && !isCurrent(held)) {
				invalidate(slot);
			}
		}
	}

	void clear() {
		invalidated.addAll(slots.keySet());
		slots.clear();
		queueStatusRefresh();
	}

	private void queueStatusRefresh() {
		// Protocol notifications precede the new Construction.step. Read it after
		// the synchronous navigation batch, without measuring every intermediate step.
		if (!refreshPending) {
			refreshPending = true;
			EventQueue.invokeLater(() -> {
				refreshPending = false;
				refreshStatus.run();
			});
		}
	}

	@Override
	public void sendEvent(Event evt) {
		if (slots.isEmpty()) {
			return;
		}
		switch (evt.type) {
		case UNDO:
		case REDO:
			clear();
			break;
		case REMOVE:
		case REDEFINE:
			invalidateStale();
			queueStatusRefresh();
			break;
		case ADD:
			queueStatusRefresh();
			break;
		case UPDATE:
		case UPDATE_STYLE:
			if (slots.containsValue(evt.target)) {
				queueStatusRefresh();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void reset() {
		// EventDispatcher.clearView: every construction rebuild and document boundary
		clear();
	}
}
