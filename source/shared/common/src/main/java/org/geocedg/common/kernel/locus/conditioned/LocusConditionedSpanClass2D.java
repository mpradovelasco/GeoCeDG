/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Evidence class of a conditioned span (candidate contract
 * {@code post-r1-conditioned-use.md}, section 3.3). The class is evidence for diagnosis and
 * for choosing proof cells. It is never a permission: every class leaves the four hypotheses
 * open and every consumer treats the three classes alike.
 */
public enum LocusConditionedSpanClass2D {
	/** Nothing is proved on the cell; the node checks hold. */
	RESOLUTION_ONLY,
	/** The separation observed at the nodes is above the policy margin; it is not a bound. */
	MARGIN_ESTIMATED,
	/**
	 * A rigorous bound keeps the enclosed roots pairwise separated on the cell. It proves
	 * nothing else: the stability of a selection is not a consequence of a separation.
	 */
	MARGIN_BOUNDED;

	private static final Set<LocusConditionedHypothesis2D> ALL = Collections
			.unmodifiableSet(EnumSet.allOf(LocusConditionedHypothesis2D.class));

	/**
	 * @return the hypotheses that remain unproved on a span of this class: all four, for
	 *         every class
	 */
	public Set<LocusConditionedHypothesis2D> getOpenHypotheses() {
		return ALL;
	}
}
