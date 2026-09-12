/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import javax.annotation.CheckForNull;

/**
 * Optional presentation-side decision seam for an already classified semantic
 * redefine. Implementations select an explicitly available execution mode or
 * cancel; they never recompute compatibility or impact.
 */
@FunctionalInterface
public interface SpatialRedefineAssessmentHandler {
	/**
	 * @param assessment current typed kernel assessment
	 * @return explicitly selected mode, or {@code null} to cancel
	 */
	@CheckForNull SpatialRedefineExecutionMode selectExecutionMode(
			SpatialRedefineAssessment assessment);

	/**
	 * Notifies presentation that a choice became stale before preparation. The
	 * caller must submit through a fresh staged-candidate flow; the old choice is
	 * never reused.
	 *
	 * @param assessment stale assessment whose choice was discarded
	 */
	default void assessmentBecameStale(SpatialRedefineAssessment assessment) {
		// Optional presentation notification.
	}
}
