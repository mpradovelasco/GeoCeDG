/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MembershipStatus;

/** Immutable limited-target membership and boundary evidence. */
public final class TargetMembership2D {
	private final MembershipStatus status;
	private final boolean includedBoundary;
	private final String diagnostic;

	/** Creates one closed membership outcome. */
	public TargetMembership2D(MembershipStatus status,
			boolean includedBoundary, String diagnostic) {
		this.status = java.util.Objects.requireNonNull(status);
		if (diagnostic == null) {
			throw new IllegalArgumentException("Membership diagnostic is required");
		}
		this.includedBoundary = includedBoundary;
		this.diagnostic = diagnostic;
	}

	public MembershipStatus getStatus() {
		return status;
	}

	public boolean isIncludedBoundary() {
		return includedBoundary;
	}

	public String getDiagnostic() {
		return diagnostic;
	}
}
