/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

/**
 * Declared hypotheses of a conditioned span (candidate contract
 * {@code post-r1-conditioned-use.md}, section 4). A finite traversal checks them at its nodes
 * and assumes them between the nodes; none is a proved fact.
 */
public enum LocusConditionedHypothesis2D {
	/** H1: the traced point is defined at every parameter of the span. */
	H1_VALIDITY,
	/** H2: every selecting dependency keeps the selection established at the nodes. */
	H2_SELECTION,
	/** H3: the traced function is continuous on the span. */
	H3_CONTINUITY,
	/** H4: the curve stays within the chord-deviation limit from the node polyline. */
	H4_RESOLUTION
}
