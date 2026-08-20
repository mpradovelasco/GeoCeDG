/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Explicit single edit authority for one inert spatial-object revision. */
public enum EditAuthorityMode {
	/** Ordinary construction-DAG inputs are the sole edit authority. */
	CONSTRUCTION_DEFINED,
	PROJECTION_DEFINED,
	SPATIAL_DEFINED
}
