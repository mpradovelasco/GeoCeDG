/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Explicit execution authority chosen after a non-mutating redefine assessment. */
public enum SpatialRedefineExecutionMode {
	ADVANCED_RETAIN,
	LEGACY_REPLACEMENT,
	/**
	 * Explicitly accepted identity-preserving durable-contract update: the target
	 * keeps its identity and receives the certified new dependency frontier.
	 */
	IDENTITY_PRESERVING_CONTRACT_UPDATE
}
