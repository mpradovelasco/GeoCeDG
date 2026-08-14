/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Objects;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;

/** Immutable typed diagnostic attached to a query or solution. */
public final class IntersectionDiagnostic2D {
	private final DiagnosticCode code;
	private final String message;

	/** Creates one diagnostic without using magic numeric state. */
	public IntersectionDiagnostic2D(DiagnosticCode code, String message) {
		this.code = Objects.requireNonNull(code);
		if (message == null || message.trim().isEmpty()) {
			throw new IllegalArgumentException("Intersection diagnostic is required");
		}
		this.message = message;
	}

	public DiagnosticCode getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return code + ": " + message;
	}
}
