/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import org.geogebra.common.kernel.geos.GeoElement;

/** Provider gate that inspects the actual redefine result before host mutation. */
public interface SpatialRedefineProvider {
	/** @return the stable provider identifier stored in participating geo records */
	String getProviderId();

	/**
	 * Describes the actual parsed candidate from provider-owned semantics. Host
	 * class, label, command equality and output ordinal are not valid inputs.
	 *
	 * @return the provider-owned candidate signature
	 */
	SpatialRedefineSignature describeCandidate(SpatialRedefineContext context,
			GeoElement candidate);

	/**
	 * Proves whether this actual candidate preserves the provider-owned topology.
	 * Host class, label and instance reuse are not proof.
	 *
	 * @return true only with provider-owned topology evidence
	 */
	boolean isTopologyPreserving(SpatialRedefineContext context,
			GeoElement candidate);

	/** @return the provider's pre-mutation identity decision */
	SpatialRedefineDecision inspect(SpatialRedefineContext context,
			SpatialRedefineProposal proposal);
}
