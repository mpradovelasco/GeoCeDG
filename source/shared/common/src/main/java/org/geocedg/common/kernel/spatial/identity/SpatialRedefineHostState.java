/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Objects;

import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Immutable host-operation evidence used only to classify redefine effect.
 * Durable continuity is decided independently by provider/type/schema/role.
 */
final class SpatialRedefineHostState {
	private final String geoType;
	private final boolean independent;
	private final boolean defined;
	private final String definition;
	private final String value;

	private SpatialRedefineHostState(GeoElement geo) {
		geoType = geo.getGeoClassType().name();
		independent = geo.isIndependent();
		defined = geo.isDefined();
		definition = geo.getDefinition(StringTemplate.xmlTemplate);
		value = geo.toValueString(StringTemplate.xmlTemplate);
	}

	static SpatialRedefineHostState capture(GeoElement geo) {
		return new SpatialRedefineHostState(Objects.requireNonNull(geo));
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof SpatialRedefineHostState)) {
			return false;
		}
		SpatialRedefineHostState state = (SpatialRedefineHostState) other;
		return independent == state.independent && defined == state.defined
				&& geoType.equals(state.geoType)
				&& definition.equals(state.definition) && value.equals(state.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(geoType, independent, defined, definition, value);
	}
}
