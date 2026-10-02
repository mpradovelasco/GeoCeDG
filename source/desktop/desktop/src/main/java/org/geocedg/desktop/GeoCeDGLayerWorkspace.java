/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.plugin.EuclidianStyleConstants;

/**
 * PRE-G9B-R6-plus-A-1 session layer workspace: the working layer for new
 * objects and the set of hidden layers.
 *
 * <p>Both values are Desktop session state, not geometric truth. They are never
 * serialized, never written into the undo, macro, clipboard or preferences XML,
 * never create an undo point and never write an object's layer or visibility.
 * A hidden layer only stops its objects from being painted and hit in the
 * views (author decisions AQ-L2, AQ-L3 and AQ-L7 of 2026-10-02).
 */
final class GeoCeDGLayerWorkspace {
	/** Lowest layer of the unchanged host domain. */
	static final int MIN_LAYER = 0;
	/** Highest layer of the unchanged host domain; A-1 does not widen it. */
	static final int MAX_LAYER = EuclidianStyleConstants.MAX_LAYERS;

	private final Kernel kernel;
	private final BitSet hiddenLayers = new BitSet(MAX_LAYER + 1);
	private final List<Runnable> listeners = new ArrayList<>();
	private int workingLayer = MIN_LAYER;
	private int documentTransitions;

	GeoCeDGLayerWorkspace(Kernel kernel) {
		this.kernel = kernel;
	}

	/** @return layer used by interactively created objects */
	int getWorkingLayer() {
		return workingLayer;
	}

	/**
	 * Explicit choice of the working layer. A hidden layer chosen this way is
	 * shown first (AQ-L7), so the working layer is never hidden.
	 *
	 * @param layer layer in the host domain
	 * @return whether the state changed
	 * @throws IllegalArgumentException outside 0..9; values never wrap or clamp
	 */
	boolean setWorkingLayer(int layer) {
		requireLayer(layer);
		boolean changed = hiddenLayers.get(layer) || workingLayer != layer;
		hiddenLayers.clear(layer);
		workingLayer = layer;
		if (changed) {
			fireChanged();
		}
		return changed;
	}

	/**
	 * @param layer layer
	 * @return whether the objects of the layer are painted and hittable
	 */
	boolean isLayerShown(int layer) {
		return layer < MIN_LAYER || layer > MAX_LAYER || !hiddenLayers.get(layer);
	}

	/**
	 * @param layer layer
	 * @return whether the working layer could not be hidden: hiding is refused
	 */
	boolean isHidingRefused(int layer) {
		return layer == workingLayer;
	}

	/**
	 * Hides or shows one layer. Hiding the working layer is refused (AQ-L7).
	 *
	 * @param layer layer in the host domain
	 * @param hidden whether the layer is hidden
	 * @return whether the state changed
	 */
	boolean setLayerHidden(int layer, boolean hidden) {
		requireLayer(layer);
		if (hidden && isHidingRefused(layer)) {
			return false;
		}
		if (hiddenLayers.get(layer) == hidden) {
			return false;
		}
		hiddenLayers.set(layer, hidden);
		fireChanged();
		return true;
	}

	/** @return hidden layers in ascending order */
	List<Integer> getHiddenLayers() {
		List<Integer> layers = new ArrayList<>();
		for (int layer = hiddenLayers.nextSetBit(0); layer >= 0;
				layer = hiddenLayers.nextSetBit(layer + 1)) {
			layers.add(layer);
		}
		return layers;
	}

	/**
	 * Layer for an object the application is creating now.
	 *
	 * @param construction owner of the new object
	 * @param upstreamLayer host layer
	 * @return the working layer for interactive creation in the document
	 *         construction, otherwise the unchanged host layer
	 */
	int layerForNewObject(Construction construction, int upstreamLayer) {
		return isInteractiveCreation(construction) ? workingLayer : upstreamLayer;
	}

	/**
	 * Interactive creation excludes every rebuild: file and undo/redo reading,
	 * redefinition rebuilds, paste, macro (tool) constructions and the document
	 * transition of New and Open.
	 */
	private boolean isInteractiveCreation(Construction construction) {
		Construction document = kernel.getConstruction();
		return documentTransitions == 0 && construction == document
				&& !document.isFileLoading() && !kernel.getLoadingMode();
	}

	/**
	 * Runs a document transition (Open) during which no object takes the
	 * working layer.
	 *
	 * @param transition transition
	 * @return its result
	 */
	boolean runDocumentTransition(DocumentTransition transition) {
		documentTransitions++;
		try {
			return transition.run();
		} finally {
			documentTransitions--;
		}
	}

	/** File &gt; New: the working layer starts at 0 and no layer is hidden. */
	void resetForNewDocument() {
		workingLayer = MIN_LAYER;
		hiddenLayers.clear();
		fireChanged();
	}

	/**
	 * File &gt; Open: the working layer becomes the highest layer used by a
	 * drawable object of the opened document. This only reads the document;
	 * no object layer or visibility is written.
	 */
	void resetForOpenedDocument() {
		int highest = MIN_LAYER;
		for (GeoElement geo : kernel.getConstruction().getGeoSetConstructionOrder()) {
			if (geo.isDrawable()) {
				highest = Math.max(highest, geo.getLayer());
			}
		}
		workingLayer = Math.min(MAX_LAYER, highest);
		hiddenLayers.clear();
		fireChanged();
	}

	void addListener(Runnable listener) {
		listeners.add(listener);
	}

	private void fireChanged() {
		for (Runnable listener : new ArrayList<>(listeners)) {
			listener.run();
		}
	}

	private static void requireLayer(int layer) {
		if (layer < MIN_LAYER || layer > MAX_LAYER) {
			throw new IllegalArgumentException("Layer outside 0.." + MAX_LAYER + ": " + layer);
		}
	}

	/** A document transition with a success result. */
	@FunctionalInterface
	interface DocumentTransition {
		/** @return whether the transition succeeded */
		boolean run();
	}
}
