/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Explicit failure of an atomic identity or persistence transaction. */
public final class SpatialIdentityException extends IllegalStateException {
	private static final long serialVersionUID = 1L;

	private final SpatialIdentityDiagnostic diagnostic;

	/** Creates a failure carrying structured diagnostic evidence. */
	public SpatialIdentityException(SpatialIdentityDiagnostic diagnostic) {
		super(diagnostic.toString());
		this.diagnostic = diagnostic;
	}

	/** Creates a failure carrying evidence and its parsing or validation cause. */
	public SpatialIdentityException(SpatialIdentityDiagnostic diagnostic,
			Throwable cause) {
		super(diagnostic.toString(), cause);
		this.diagnostic = diagnostic;
	}

	public SpatialIdentityDiagnostic getDiagnostic() {
		return diagnostic;
	}
}
