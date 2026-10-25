/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Arrays;

import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;

/**
 * The inherited selection state of one line-circle intersection, read and never written
 * ({@code locus-v2-certified-construction-model.md} section 10.2;
 * {@code locus-v2-constructive-authority.md} sections 3.2 and 8): either the compaction of the
 * raw roots that lie on the paths, in raw order, while the first intersection is still to be
 * established, or the fixed permutation from raw roots to outputs, or the special case of a
 * point already incident with both objects (D1), whose output at its index is that point and the
 * other output the raw root that is not it; and whether each output is labelled. The exact
 * evaluators apply it to the exact raw roots, which are ordered by their parameter along the
 * line.
 */
public final class InheritedLineCircleSelection {

	/** Cause of a selection state that the inherited rule does not let an evaluator reproduce. */
	public static final String NOT_RECONSTRUCTED = "SELECTION_NOT_RECONSTRUCTED";

	private final boolean compaction;
	private final int[] permutation;
	private final boolean[] labelled;
	private final int existing;

	private InheritedLineCircleSelection(boolean compaction, int[] permutation,
			boolean[] labelled, int existing) {
		this.compaction = compaction;
		this.permutation = permutation;
		this.labelled = labelled;
		this.existing = existing;
	}

	/**
	 * Reads the selection state of an intersection with two outputs.
	 *
	 * @param algorithm the inherited algorithm
	 * @return the state, or {@code null} when the inherited rule decides the outputs in a way no
	 *         evaluator reproduces: a tangent definition or a permutation that the next
	 *         computation sets by proximity
	 */
	static InheritedLineCircleSelection read(AlgoIntersectLineConic algorithm) {
		if (algorithm.getIntersectionPoints().length != 2) {
			return null;
		}
		boolean[] labelled = {algorithm.getOutput(0).isLabelSet(),
				algorithm.getOutput(1).isLabelSet()};
		if (algorithm.existingIntersections() != 0) {
			int index = algorithm.getSelectionExistingIntersectionIndex();
			return index == 0 || index == 1 ? new InheritedLineCircleSelection(false,
					new int[] {0, 1}, labelled, index) : null;
		}
		if (algorithm.isSelectionFirstIntersectionPending()) {
			return new InheritedLineCircleSelection(true, new int[] {0, 1}, labelled, -1);
		}
		if (algorithm.isSelectionPermutationPending()) {
			return null;
		}
		int[] permutation = algorithm.getSelectionPermutation();
		if (permutation == null || permutation.length != 2
				|| permutation[0] + permutation[1] != 1 || permutation[0] * permutation[1] != 0) {
			return null;
		}
		return new InheritedLineCircleSelection(false, permutation, labelled, -1);
	}

	/**
	 * @param compaction whether the roots on the paths are compacted in raw order
	 * @param permutation raw root of each output
	 * @param labelled whether each output is labelled
	 * @return a selection state, for synthetic programs
	 */
	public static InheritedLineCircleSelection of(boolean compaction, int[] permutation,
			boolean[] labelled) {
		if (permutation.length != 2 || labelled.length != 2
				|| permutation[0] + permutation[1] != 1 || permutation[0] * permutation[1] != 0) {
			throw new IllegalArgumentException("A line-circle selection has two outputs");
		}
		return new InheritedLineCircleSelection(compaction, permutation.clone(),
				labelled.clone(), -1);
	}

	/** @return whether the roots on the paths are compacted in raw order */
	public boolean isCompaction() {
		return compaction;
	}

	/**
	 * @return the output that is the point already incident with both objects in the inherited
	 *         special case, whose node is the third input of the output, or {@code -1}
	 */
	public int getExistingIntersectionOutput() {
		return existing;
	}

	/**
	 * @param output output index
	 * @return the raw root of the output under the fixed permutation
	 */
	public int getRawRoot(int output) {
		return permutation[output];
	}

	/**
	 * @param output output index
	 * @return whether the output is labelled
	 */
	public boolean isLabelled(int output) {
		return labelled[output];
	}

	/** @return a stable description, part of the structural definition of a node */
	public String describe() {
		return (existing >= 0 ? "existing(" + existing + ")" : compaction ? "compaction"
				: "permutation(" + permutation[0] + "," + permutation[1] + ")") + ",labels("
				+ (labelled[0] ? 1 : 0) + "," + (labelled[1] ? 1 : 0) + ")";
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof InheritedLineCircleSelection)) {
			return false;
		}
		InheritedLineCircleSelection that = (InheritedLineCircleSelection) other;
		return compaction == that.compaction && Arrays.equals(permutation, that.permutation)
				&& Arrays.equals(labelled, that.labelled) && existing == that.existing;
	}

	@Override
	public int hashCode() {
		return 31 * (31 * (31 * Boolean.hashCode(compaction) + Arrays.hashCode(permutation))
				+ Arrays.hashCode(labelled)) + existing;
	}

	@Override
	public String toString() {
		return describe();
	}
}
