/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

/** Independent validity state of a required projection-system context. */
public enum ProjectionSystemStatus {
	NOT_EVALUATED,
	CONSISTENT,
	INCONSISTENT,
	DEGENERATE,
	UNDEFINED
}
