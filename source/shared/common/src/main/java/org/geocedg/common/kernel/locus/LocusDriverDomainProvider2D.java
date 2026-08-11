/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;

/** Versioned authority for the semantic parameter and its declared domain. */
public interface LocusDriverDomainProvider2D {
	/** @return versioned provider identifier */
	String getProviderId();

	/** @return provider-owned semantic parameter descriptor */
	String getParameterDescriptor();

	/** @return declared semantic driver domain */
	LocusInterval2D getDeclaredDomain();

	/** @return semantic orientation */
	Orientation getOrientation();

	/** @return whether endpoint-equivalent values are canonicalized */
	boolean isPeriodic();

	/** @return provider-owned domain predicate tolerance */
	double getDomainEpsilon();

	/**
	 * Maps equivalent periodic values to one stable semantic address.
	 *
	 * @return provider-canonical semantic parameter
	 */
	double canonicalize(double parameter);

	/**
	 * Tests membership after provider canonicalization.
	 *
	 * @return whether the canonical parameter belongs to the declared domain
	 */
	boolean contains(double canonicalParameter);

	/**
	 * Stable content descriptor used by semantic snapshot comparison.
	 *
	 * @return deterministic provider content signature
	 */
	String getSemanticSignature();
}
