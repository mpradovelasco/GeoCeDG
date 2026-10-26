/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;

/**
 * The enclosures of the traced point of the slice of one Locus V2 on parameter boxes, shared
 * by the constructive residual sources of its intersection queries in one revision (PRE-G9B-CU3
 * focal optimization T6). The traced point on a box is the interval-jet value of the traced node
 * with the driver at the box; it depends only on the ancestors of the traced node and of the
 * support of the driver, never on the target of a query. An enclosure is reused only under the
 * same key, the canonical text of everything the evaluation reads:
 * <ul>
 * <li>the version of the class table and of this memory;</li>
 * <li>the semantic revision of the locus, the family of the driver and its frame;</li>
 * <li>for every ancestor of the traced node and of the support, renumbered canonically (every
 * node after its inputs, by structural position, whatever the order of the cone), its
 * durable identity, its structural definition with its inputs and, outside the slice, its
 * captured binary64 state, as {@link ConstructiveCone#getStateDigest()} records it;</li>
 * <li>the node of the driver substitution;</li>
 * <li>the exact binary64 ends of the box.</li>
 * </ul>
 * Only the sources of the constructive configuration reach the memory. Any change of structure,
 * identity, selection state, branch or input of the slice changes the key; a new key discards
 * every enclosure of the earlier one, so the memory keeps at most one revision of one locus.
 * It is transient: nothing here is persisted, and a locus that is no longer referenced
 * releases its memory. It stores values exactly as the evaluation
 * produced them, so a reused enclosure has the same outward-rounded ends and guarantees as one
 * computed again.
 */
final class ConstructiveSliceEnclosureMemory {

	/** Versioned name of this memory and of the evaluation contract it reuses. */
	static final String VERSION = "constructive-slice-enclosure-memory/v1";
	/** Most boxes kept for one key; beyond it enclosures are computed and not kept. */
	static final int MAXIMUM_BOXES = 1 << 14;

	/** The value of a traced point whose evaluation failed on a box. */
	static final Object FAILED = new Object();

	private static final Map<GeoLocusV2, ConstructiveSliceEnclosureMemory> MEMORIES =
			new WeakHashMap<>();

	private String key;
	private final Map<Box, Object> traced = new HashMap<>();
	private int reused;

	/** One parameter box, by the bits of its binary64 ends. */
	private static final class Box {
		private final long lower;
		private final long upper;

		Box(double lower, double upper) {
			this.lower = Double.doubleToRawLongBits(lower);
			this.upper = Double.doubleToRawLongBits(upper);
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof Box && ((Box) other).lower == lower
					&& ((Box) other).upper == upper;
		}

		@Override
		public int hashCode() {
			return Long.hashCode(lower) * 31 + Long.hashCode(upper);
		}
	}

	private ConstructiveSliceEnclosureMemory() {
	}

	/**
	 * @param locus a Locus V2
	 * @return its memory, created empty on first use
	 */
	static ConstructiveSliceEnclosureMemory of(GeoLocusV2 locus) {
		synchronized (MEMORIES) {
			return MEMORIES.computeIfAbsent(locus,
					ignored -> new ConstructiveSliceEnclosureMemory());
		}
	}

	/**
	 * @return the enclosure of the traced point kept for the key and the box, {@link #FAILED}
	 *         when its evaluation failed, or {@code null} when none is kept
	 */
	synchronized Object get(String sliceKey, double lower, double upper) {
		if (!sliceKey.equals(key)) {
			return null;
		}
		Object value = traced.get(new Box(lower, upper));
		if (value != null) {
			reused++;
		}
		return value;
	}

	/**
	 * Keeps the enclosure of the traced point for the key and the box; a new key first discards
	 * every enclosure of the earlier one.
	 */
	synchronized void put(String sliceKey, double lower, double upper, Object value) {
		if (!sliceKey.equals(key)) {
			key = sliceKey;
			traced.clear();
		}
		if (traced.size() < MAXIMUM_BOXES) {
			traced.put(new Box(lower, upper), value);
		}
	}

	/** @return the number of boxes kept: for the tests */
	synchronized int size() {
		return traced.size();
	}

	/** @return the number of enclosures served from this memory so far: for the tests */
	synchronized int getReused() {
		return reused;
	}

	/**
	 * The canonical key of the slice of a cone: see the description of the class.
	 *
	 * @param cone the cone of one query
	 * @param tracedNode the traced node
	 * @param supportNode the node of the support of the driver
	 * @param family the family of the driver
	 * @param frame the frame of a circle driver, or {@code null}
	 * @param semanticRevision the semantic revision of the locus
	 * @return the key
	 */
	static String key(ConstructiveCone cone, int tracedNode, int supportNode,
			CertifiedConstructionProgram2D.Driver family, double[] frame,
			long semanticRevision) {
		// Canonical numbering: every node after its inputs, visited by structural position from
		// the traced node and then the support, so that the order of the nodes in the cone of a
		// query, which depends on its target, never enters the key.
		List<Integer> closure = new ArrayList<>();
		Map<Integer, Integer> renumbered = new HashMap<>();
		for (int root : new int[] {tracedNode, supportNode}) {
			List<int[]> stack = new ArrayList<>();
			stack.add(new int[] {root, 0});
			while (!stack.isEmpty()) {
				int[] top = stack.get(stack.size() - 1);
				if (renumbered.containsKey(top[0])) {
					stack.remove(stack.size() - 1);
					continue;
				}
				ConstructiveConeNode node = cone.getNode(top[0]);
				if (top[1] < node.getInputCount()) {
					stack.add(new int[] {node.getInput(top[1]++), 0});
				} else {
					stack.remove(stack.size() - 1);
					renumbered.put(top[0], renumbered.size());
					closure.add(top[0]);
				}
			}
		}
		StringBuilder text = new StringBuilder(VERSION).append('\n')
				.append(ConstructiveClassTable.VERSION).append('\n')
				.append("revision=").append(semanticRevision).append('\n')
				.append("driver=").append(family).append('\n');
		if (frame != null) {
			for (double value : frame) {
				text.append(Long.toHexString(Double.doubleToRawLongBits(value))).append(',');
			}
			text.append('\n');
		}
		for (int index : closure) {
			ConstructiveConeNode node = cone.getNode(index);
			text.append('m').append(renumbered.get(index)).append(' ')
					.append(node.getDurableIdentity() == null ? "-"
							: node.getDurableIdentity().toExternalForm())
					.append(' ').append(renumbered(node, renumbered));
			if (!node.isDriverSlice()) {
				text.append(node.isLiveDefined() ? " +" : " -");
				for (double value : node.getLiveState()) {
					text.append(Long.toHexString(Double.doubleToRawLongBits(value))).append(',');
				}
			}
			text.append('\n');
		}
		Integer replacement = renumbered.get(cone.getReplacementNode());
		text.append("driver := ").append(replacement == null ? "-" : "m" + replacement)
				.append('\n').append("traced := m").append(renumbered.get(tracedNode))
				.append('\n').append("support := m").append(renumbered.get(supportNode));
		return text.toString();
	}

	/** The structural definition of a node with its inputs renumbered. */
	private static String renumbered(ConstructiveConeNode node, Map<Integer, Integer> numbers) {
		String definition = node.describe();
		int open = definition.indexOf('(');
		int close = definition.indexOf(')');
		List<String> inputs = new ArrayList<>();
		for (int position = 0; position < node.getInputCount(); position++) {
			inputs.add("m" + numbers.get(node.getInput(position)));
		}
		return definition.substring(0, open + 1) + String.join(",", inputs)
				+ definition.substring(close);
	}

	/** Discards every memory: for the tests. */
	static void clearAll() {
		synchronized (MEMORIES) {
			MEMORIES.clear();
		}
	}

	/** @return the number of loci with a memory: for the tests */
	static int count() {
		synchronized (MEMORIES) {
			return MEMORIES.size();
		}
	}
}
