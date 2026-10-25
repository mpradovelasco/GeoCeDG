/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * The kernel axes {@code xAxis} and {@code yAxis} as reserved references of the spatial identity
 * registry ({@code locus-v2-intersections.md} CU3.5; {@code g9-spatial-projection-semantics.md}
 * section 8.3; decision D-CU3-8, option A). Each has a fixed symbolic identity that is part of
 * the kernel contract, never a label: it is never allocated, never a record and never an
 * attachment, and it resolves to the axis of the construction that reads it. Only a public
 * Locus V2 output depends on one, as the direct input that the durable dependency projection
 * names; every other construction keeps the identity-free axes of earlier documents.
 */
public final class ReservedKernelReference {
	/** Reserved identity of {@code xAxis}. */
	public static final PersistentGeoId X_AXIS =
			new PersistentGeoId("000000000000000000000000000a0001");
	/** Reserved identity of {@code yAxis}. */
	public static final PersistentGeoId Y_AXIS =
			new PersistentGeoId("000000000000000000000000000a0002");

	private ReservedKernelReference() {
	}

	/**
	 * @param geo an object
	 * @return the reserved identity of a kernel axis of its construction, otherwise
	 *         {@code null}
	 */
	public static PersistentGeoId of(GeoElement geo) {
		if (geo == null) {
			return null;
		}
		Construction construction = geo.getConstruction();
		if (construction == null) {
			return null;
		}
		if (geo == construction.getXAxis()) {
			return X_AXIS;
		}
		return geo == construction.getYAxis() ? Y_AXIS : null;
	}

	/**
	 * @param id an identity
	 * @return whether it is a reserved reference
	 */
	public static boolean isReserved(SpatialIdentityId id) {
		return X_AXIS.equals(id) || Y_AXIS.equals(id);
	}

	/**
	 * @param rawToken a raw identity token of any kind
	 * @return whether it is the token of a reserved reference, which no identity of any kind
	 *         may use
	 */
	public static boolean isReservedToken(String rawToken) {
		return X_AXIS.getRawToken().equals(rawToken) || Y_AXIS.getRawToken().equals(rawToken);
	}

	/**
	 * @param construction a construction
	 * @param id a reserved identity
	 * @return the axis it names in the construction, or {@code null}
	 */
	public static GeoElement resolve(Construction construction, SpatialIdentityId id) {
		if (construction == null) {
			return null;
		}
		if (X_AXIS.equals(id)) {
			return construction.getXAxis();
		}
		return Y_AXIS.equals(id) ? construction.getYAxis() : null;
	}
}
