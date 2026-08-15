/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetSupportStatus;

/** Typed target-adapter support decision made before query execution. */
public final class IntersectionTargetSupport2D {
	private final TargetFamily family;
	private final TargetSupportStatus status;
	private final String targetType;
	private final String diagnostic;

	/** Creates one immutable closed support decision. */
	public IntersectionTargetSupport2D(TargetFamily family,
			TargetSupportStatus status, String targetType, String diagnostic) {
		this.family = java.util.Objects.requireNonNull(family);
		this.status = java.util.Objects.requireNonNull(status);
		this.targetType = requireText(targetType, "Target type");
		this.diagnostic = requireText(diagnostic, "Support diagnostic");
		if ((status == TargetSupportStatus.SUPPORTED)
				!= (family != TargetFamily.UNSUPPORTED)) {
			throw new IllegalArgumentException(
					"Supported status and target family must agree");
		}
	}

	public TargetFamily getFamily() {
		return family;
	}

	public TargetSupportStatus getStatus() {
		return status;
	}

	public String getTargetType() {
		return targetType;
	}

	public String getDiagnostic() {
		return diagnostic;
	}

	public boolean isSupported() {
		return status == TargetSupportStatus.SUPPORTED;
	}

	/** @return broad rich-result diagnostic matching this typed support state */
	public DiagnosticCode getDiagnosticCode() {
		switch (status) {
		case TARGET_UNDEFINED:
			return DiagnosticCode.INVALID_TARGET;
		case DOMAIN_NOT_EXPLICIT:
			return DiagnosticCode.TARGET_DOMAIN_NOT_EXPLICIT;
		case RESIDUAL_NORMALIZATION_UNAVAILABLE:
			return DiagnosticCode.RESIDUAL_NORMALIZATION_UNAVAILABLE;
		case NONPOLYNOMIAL_IMPLICIT:
		case UNSUPPORTED_TARGET_SUBTYPE:
			return DiagnosticCode.UNSUPPORTED_TARGET_SUBTYPE;
		case SUPPORTED:
		default:
			return DiagnosticCode.UNSUPPORTED_TARGET;
		}
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
