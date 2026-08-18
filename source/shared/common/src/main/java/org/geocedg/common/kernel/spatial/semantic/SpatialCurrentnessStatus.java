/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

/** Whether certificate evidence belongs to the current source revision. */
public enum SpatialCurrentnessStatus {
	CURRENT,
	INVALIDATED,
	FAILED_CURRENT_REVISION
}
