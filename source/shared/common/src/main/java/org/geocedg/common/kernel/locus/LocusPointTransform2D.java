/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/** Deterministic downstream construction over one upstream semantic point. */
public interface LocusPointTransform2D {
	/** @return deterministic downstream semantic point */
	LocusPoint2D transform(double downstreamParameter, LocusPoint2D upstreamPoint);
}
