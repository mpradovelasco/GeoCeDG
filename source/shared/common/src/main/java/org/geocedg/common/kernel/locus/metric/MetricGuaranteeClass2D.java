/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/**
 * Guarantee class of a metric result ({@code locus-v2-metrics.md} section 25.1),
 * derived from its value kind, numeric guarantee and method.
 */
public enum MetricGuaranteeClass2D {
	/** Class 1: structural or analytic identity with a rounding bound. */
	STRUCTURAL,
	/** Class 2: rigorous enclosure from first-derivative enclosures. */
	CERTIFIED_ENCLOSURE,
	/** Class 3: refinement estimate justified only by recorded hypotheses. */
	ESTIMATED,
	/** Class 4: no value. */
	NOT_ESTABLISHED,
	/** Pre-CU1 policies only: a floating-point value without error evidence. */
	UNCERTIFIED;

	/** @return whether this class certifies its error bound */
	public boolean isCertified() {
		return this == STRUCTURAL || this == CERTIFIED_ENCLOSURE;
	}
}
