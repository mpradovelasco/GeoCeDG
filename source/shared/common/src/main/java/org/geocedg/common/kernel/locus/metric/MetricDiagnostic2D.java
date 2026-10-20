/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/** Immutable typed metric diagnostic. */
public final class MetricDiagnostic2D {
	private final MetricDiagnosticCode2D code;
	private final String message;
	private final MetricWorkLimit2D workLimit;

	/** Creates a non-empty diagnostic. */
	public MetricDiagnostic2D(MetricDiagnosticCode2D code, String message) {
		this(code, message, MetricWorkLimit2D.NONE);
	}

	/**
	 * Creates a diagnostic that carries the typed work dimension it concerns
	 * ({@code locus-v2-metrics.md} section 25.7).
	 */
	public MetricDiagnostic2D(MetricDiagnosticCode2D code, String message,
			MetricWorkLimit2D workLimit) {
		this.code = Objects.requireNonNull(code);
		if (message == null || message.trim().isEmpty()) {
			throw new IllegalArgumentException("A metric diagnostic needs a message");
		}
		this.message = message;
		this.workLimit = Objects.requireNonNull(workLimit);
	}

	public MetricDiagnosticCode2D getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	/** @return exhausted work dimension, or {@code NONE} */
	public MetricWorkLimit2D getWorkLimit() {
		return workLimit;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MetricDiagnostic2D)) {
			return false;
		}
		MetricDiagnostic2D diagnostic = (MetricDiagnostic2D) other;
		return code == diagnostic.code && message.equals(diagnostic.message)
				&& workLimit == diagnostic.workLimit;
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, message, workLimit);
	}

	@Override
	public String toString() {
		return code + ": " + message;
	}
}
