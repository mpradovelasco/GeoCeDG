/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Explicit policy for a target not reachable before an open-branch boundary. */
public enum OpenBoundaryPolicy {
	STOP_AT_END,
	WRAP_TO_START,
	STRICT
}
