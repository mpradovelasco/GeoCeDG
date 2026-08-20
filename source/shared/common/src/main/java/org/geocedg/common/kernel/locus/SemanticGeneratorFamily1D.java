/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/** Closed productive G9U0 one-dimensional generator families. */
public enum SemanticGeneratorFamily1D {
	SCALAR_STATE("scalar-state/v1"),
	SEGMENT_POINT("segment-point/v1"),
	CIRCLE_POINT("circle-point/v1"),
	CIRCULAR_ARC_POINT("circular-arc-point/v1"),
	LOCUS_BRANCH_POINT("locus-branch-point/v1");

	private final String providerId;

	SemanticGeneratorFamily1D(String providerId) {
		this.providerId = providerId;
	}

	/** @return closed versioned provider token */
	public String getProviderId() {
		return providerId;
	}
}
