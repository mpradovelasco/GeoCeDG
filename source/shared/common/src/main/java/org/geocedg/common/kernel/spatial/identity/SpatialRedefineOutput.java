/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import org.geogebra.common.kernel.geos.GeoElement;

/** One old or candidate host output identified by a provider-owned stable role. */
public interface SpatialRedefineOutput {
	/** @return the exact host output represented by this value */
	GeoElement getGeo();

	/** @return the provider-owned continuity signature for the output */
	SpatialRedefineSignature getSignature();

	/** @return the persisted/provider-owned role; never a host output ordinal */
	default String getStableOutputRole() {
		return getSignature().getStableOutputRole();
	}
}
