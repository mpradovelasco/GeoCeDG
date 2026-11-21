/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

/**
 * Kind of one cell of a conditioned evidence (candidate contract
 * {@code post-r1-conditioned-use.md}, section 3.1). Only a {@link #SPAN} carries interval
 * evidence under hypotheses; every other kind is a band that no continuation crosses.
 */
public enum LocusConditionedCellKind2D {
	/** Valid nodes, one established selection and the geometric criteria of the policy. */
	SPAN,
	/** Validity differs between nodes; localized to the minimum step. */
	TRANSITION_BAND,
	/** The established selection differs between nodes or is not established at a node. */
	SELECTION_BAND,
	/** A geometric criterion still fails at the minimum step. */
	DISCONTINUITY_BAND,
	/** A node failed with a cause that is not a geometric absence. */
	FAILURE_BAND,
	/** Every node is invalid with the same eligible geometric cause. */
	ABSENT_BAND,
	/** The work limit of the policy ended before the cell was classified. */
	UNEXPLORED;

	/** @return whether a cell of this kind is a conditioned span */
	public boolean isSpan() {
		return this == SPAN;
	}
}
