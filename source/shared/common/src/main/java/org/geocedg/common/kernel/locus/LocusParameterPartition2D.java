/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.List;

/**
 * Optional semantic partition of one oriented parameter component.
 *
 * <p>The breakpoints are mathematical input to numerical consumers. They are
 * never render samples, viewport state or solution identity by themselves.</p>
 */
public interface LocusParameterPartition2D {
	/**
	 * Returns the strictly interior, provider-canonical breakpoints for the
	 * requested branch and interval, in increasing numeric order.
	 *
	 * @return immutable deterministic breakpoint list
	 */
	List<Double> getInteriorBreakpoints(String branchKey, double lower,
			double upper);
}
