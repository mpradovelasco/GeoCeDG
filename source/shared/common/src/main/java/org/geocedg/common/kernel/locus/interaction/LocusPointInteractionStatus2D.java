/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.interaction;

/** Typed outcome of one viewport-independent semantic preimage query. */
public enum LocusPointInteractionStatus2D {
	NO_ADMISSIBLE_PREIMAGE,
	UNIQUE_ADMISSIBLE_PREIMAGE,
	MULTIPLE_SEMANTIC_PREIMAGES,
	UNRESOLVED_NUMERICAL_SEARCH,
	INVALID_SOURCE,
	DEGENERATE_SOURCE_IMAGE,
	UNSUPPORTED_CAPABILITY
}
