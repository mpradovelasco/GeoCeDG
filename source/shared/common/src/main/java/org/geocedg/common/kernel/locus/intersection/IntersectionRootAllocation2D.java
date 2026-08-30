/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/**
 * Opaque ledger allocation for one current semantic intersection root.
 *
 * <p>The continuation key binds an intrinsic deterministic selector to one
 * opaque allocation; it is not a coordinate, parameter, result position or
 * sampling index. Whether an existing allocation was resumed is diagnostic
 * lifecycle evidence only.</p>
 */
public final class IntersectionRootAllocation2D {
	private final String rootToken;
	private final String continuationKey;
	private final boolean reused;

	/** Creates one validated immutable allocation result. */
	IntersectionRootAllocation2D(String rootToken, String continuationKey,
			boolean reused) {
		this.rootToken = requireText(rootToken, "Root token");
		this.continuationKey = requireText(continuationKey,
				"Continuation key");
		this.reused = reused;
	}

	public String getRootToken() {
		return rootToken;
	}

	public String getContinuationKey() {
		return continuationKey;
	}

	/** @return whether an already committed ledger incarnation was resumed */
	public boolean isReused() {
		return reused;
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
