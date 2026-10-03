/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.layers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * PRE-G9B-R6-plus-A-2 (AQ-L3): immutable, document-wide set of hidden layers, the
 * persisted value of the {@code DOCUMENT_PRESENTATION} hidden-layer state. It is
 * presentation only: it never changes object visibility, geometry, dependencies,
 * identity or numeric values. Its members are non-negative and kept in strictly
 * ascending order, which is also the canonical serialized order.
 */
public final class HiddenLayerSet {
	/** The empty set: nothing is hidden and nothing is written. */
	public static final HiddenLayerSet EMPTY = new HiddenLayerSet(new int[0]);

	private final int[] layers;

	private HiddenLayerSet(int[] layers) {
		this.layers = layers;
	}

	/**
	 * @param layers hidden layers in any order, duplicates allowed
	 * @return the set
	 * @throws IllegalArgumentException for a negative layer
	 */
	public static HiddenLayerSet of(Collection<Integer> layers) {
		int[] values = new int[layers.size()];
		int i = 0;
		for (Integer layer : layers) {
			values[i++] = layer;
		}
		return of(values);
	}

	/**
	 * @param layers hidden layers in any order, duplicates allowed
	 * @return the set
	 * @throws IllegalArgumentException for a negative layer
	 */
	public static HiddenLayerSet of(int... layers) {
		int[] sorted = layers.clone();
		Arrays.sort(sorted);
		int size = 0;
		for (int i = 0; i < sorted.length; i++) {
			if (sorted[i] < 0) {
				throw new IllegalArgumentException("negative layer " + sorted[i]);
			}
			if (size == 0 || sorted[size - 1] != sorted[i]) {
				sorted[size++] = sorted[i];
			}
		}
		return size == 0 ? EMPTY : new HiddenLayerSet(Arrays.copyOf(sorted, size));
	}

	/** @return whether no layer is hidden */
	public boolean isEmpty() {
		return layers.length == 0;
	}

	/** @return number of hidden layers */
	public int size() {
		return layers.length;
	}

	/**
	 * @param layer layer
	 * @return whether the layer is hidden
	 */
	public boolean contains(int layer) {
		return Arrays.binarySearch(layers, layer) >= 0;
	}

	/**
	 * @param maxLayer highest admissible layer
	 * @return whether every layer of {@code 0..maxLayer} is hidden (DQ-A2-1: invalid
	 *         persisted metadata)
	 */
	public boolean coversDomain(int maxLayer) {
		if (layers.length != maxLayer + 1) {
			return false;
		}
		return layers[0] == 0 && layers[layers.length - 1] == maxLayer;
	}

	/** @return the hidden layers in ascending order */
	public List<Integer> toList() {
		List<Integer> list = new ArrayList<>(layers.length);
		for (int layer : layers) {
			list.add(layer);
		}
		return Collections.unmodifiableList(list);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof HiddenLayerSet
				&& Arrays.equals(layers, ((HiddenLayerSet) other).layers);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(layers);
	}

	@Override
	public String toString() {
		return toList().toString();
	}
}
