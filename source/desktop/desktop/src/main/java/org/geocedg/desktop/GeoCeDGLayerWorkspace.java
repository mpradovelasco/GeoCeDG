/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.geocedg.common.kernel.layers.HiddenLayerSet;
import org.geocedg.common.kernel.layers.LayerDomain;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * PRE-G9B-R6-plus-A-1 layer workspace: the working layer for new objects and the
 * set of hidden layers, the single runtime owner of both.
 *
 * <p>The working layer is Desktop session state: never serialized. Since
 * PRE-G9B-R6-plus-A-2 the hidden-layer set is document presentation (AQ-L3):
 * document-wide and persisted in the full document XML, but never written into the
 * undo, macro, clipboard or preferences XML and never an undo point. Neither value
 * is geometric truth, and neither ever writes an object's layer or visibility; a
 * hidden layer only stops its objects from being painted and hit in the views. The
 * layer domain is the product's configured domain {@code 0..maxLayer} (AQ-L1a).
 */
final class GeoCeDGLayerWorkspace {
	/** Lowest admissible layer. */
	static final int MIN_LAYER = 0;

	private final Kernel kernel;
	private final int maxLayer;
	private final BitSet hiddenLayers = new BitSet();
	private final List<Runnable> listeners = new ArrayList<>();
	private Runnable persistedSetEdited = () -> { };
	private int workingLayer = MIN_LAYER;
	private int documentTransitions;

	GeoCeDGLayerWorkspace(Kernel kernel) {
		this.kernel = kernel;
		this.maxLayer = LayerDomain.maxLayer(kernel.getApplication());
	}

	/** @return highest layer of the configured product domain */
	int getMaxLayer() {
		return maxLayer;
	}

	/** @return layer used by interactively created objects */
	int getWorkingLayer() {
		return workingLayer;
	}

	/**
	 * Explicit choice of the working layer. A hidden layer chosen this way is
	 * shown first (AQ-L7), so the working layer is never hidden; showing it changes
	 * the persisted hidden set.
	 *
	 * @param layer layer in the product domain
	 * @return whether the state changed
	 * @throws IllegalArgumentException outside 0..maxLayer; values never wrap or clamp
	 */
	boolean setWorkingLayer(int layer) {
		requireLayer(layer);
		boolean shown = hiddenLayers.get(layer);
		boolean changed = shown || workingLayer != layer;
		hiddenLayers.clear(layer);
		workingLayer = layer;
		if (changed) {
			fireChanged();
		}
		if (shown) {
			persistedSetEdited.run();
		}
		return changed;
	}

	/**
	 * @param layer layer
	 * @return whether the objects of the layer are painted and hittable
	 */
	boolean isLayerShown(int layer) {
		return layer < MIN_LAYER || layer > maxLayer || !hiddenLayers.get(layer);
	}

	/**
	 * @param layer layer
	 * @return whether the working layer could not be hidden: hiding is refused
	 */
	boolean isHidingRefused(int layer) {
		return layer == workingLayer;
	}

	/**
	 * Hides or shows one layer. Hiding the working layer is refused (AQ-L7). A change
	 * changes the persisted set (DQ-A2-2) but is never an undo point.
	 *
	 * @param layer layer in the product domain
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
		persistedSetEdited.run();
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

	/** @return the persisted document-wide hidden set (AQ-L3) */
	HiddenLayerSet getHiddenLayerSet() {
		return HiddenLayerSet.of(getHiddenLayers());
	}

	/**
	 * @param listener notified after a user edit of the persisted hidden set, never
	 *        after a document commit or reset
	 */
	void setPersistedSetEditListener(Runnable listener) {
		persistedSetEdited = listener;
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
	 * A committed document transition (DQ-A2-6): the hidden set becomes exactly the
	 * document's persisted set, never modified here, and the working layer follows
	 * DQ-A2-1: the highest layer used by a drawable object that is not hidden, else
	 * the lowest non-hidden layer of the domain. This only reads the document; no
	 * object layer or visibility is written.
	 *
	 * @param persisted the document's hidden set, empty for a legacy document
	 * @throws IllegalStateException if every layer of the domain is hidden, which the
	 *         reader rejects before any commit
	 */
	void commitOpenedDocument(HiddenLayerSet persisted) {
		hiddenLayers.clear();
		for (int layer : persisted.toList()) {
			hiddenLayers.set(layer);
		}
		int highest = -1;
		for (GeoElement geo : kernel.getConstruction().getGeoSetConstructionOrder()) {
			int layer = geo.getLayer();
			if (geo.isDrawable() && isLayerShown(layer)) {
				highest = Math.max(highest, Math.min(maxLayer, layer));
			}
		}
		workingLayer = highest >= MIN_LAYER ? highest : lowestShownLayer();
		fireChanged();
	}

	private int lowestShownLayer() {
		int layer = hiddenLayers.nextClearBit(MIN_LAYER);
		if (layer > maxLayer) {
			throw new IllegalStateException("every layer 0.." + maxLayer + " is hidden");
		}
		return layer;
	}

	void addListener(Runnable listener) {
		listeners.add(listener);
	}

	private void fireChanged() {
		for (Runnable listener : new ArrayList<>(listeners)) {
			listener.run();
		}
	}

	private void requireLayer(int layer) {
		if (layer < MIN_LAYER || layer > maxLayer) {
			throw new IllegalArgumentException("Layer outside 0.." + maxLayer + ": " + layer);
		}
	}

	/** A document transition with a success result. */
	@FunctionalInterface
	interface DocumentTransition {
		/** @return whether the transition succeeded */
		boolean run();
	}
}
