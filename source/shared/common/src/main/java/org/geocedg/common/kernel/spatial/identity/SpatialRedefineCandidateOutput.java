/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Objects;

import org.geogebra.common.kernel.geos.GeoElement;

/** Provider-described candidate output before host mutation. */
public final class SpatialRedefineCandidateOutput implements SpatialRedefineOutput {
	private final GeoElement geo;
	private final SpatialRedefineSignature signature;
	private final SpatialRedefineHostState hostState;

	/** Creates one immutable candidate role assignment. */
	public SpatialRedefineCandidateOutput(GeoElement geo,
			SpatialRedefineSignature signature) {
		this.geo = Objects.requireNonNull(geo);
		this.signature = Objects.requireNonNull(signature);
		hostState = SpatialRedefineHostState.capture(geo);
	}

	@Override
	public GeoElement getGeo() {
		return geo;
	}

	@Override
	public SpatialRedefineSignature getSignature() {
		return signature;
	}

	SpatialRedefineHostState getHostState() {
		return hostState;
	}
}
