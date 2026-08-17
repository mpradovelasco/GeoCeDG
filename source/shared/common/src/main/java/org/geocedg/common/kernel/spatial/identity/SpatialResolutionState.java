/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Persistence-resolution state, separate from later geometric validity. */
public enum SpatialResolutionState {
	ACTIVE,
	BROKEN,
	UNSUPPORTED
}
