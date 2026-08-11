/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/** Deterministic mapping from a downstream to an upstream semantic parameter. */
public interface LocusParameterMap2D {
	/** @return upstream semantic parameter */
	double map(double downstreamParameter);
}
