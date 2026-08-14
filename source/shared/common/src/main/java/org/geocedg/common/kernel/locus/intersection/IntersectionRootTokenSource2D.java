/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/** Algorithm-local source of opaque, non-coordinate root tokens. */
public interface IntersectionRootTokenSource2D {
	/** @return a new token unique within the constructive intersection lineage */
	String nextToken();
}
