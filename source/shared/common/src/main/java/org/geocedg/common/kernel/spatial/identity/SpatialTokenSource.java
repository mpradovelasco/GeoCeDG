/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Injectable source of opaque tokens, independent of the kernel random state. */
public interface SpatialTokenSource {
	/** @return a candidate 32-character lowercase hexadecimal raw token */
	String nextToken();
}
