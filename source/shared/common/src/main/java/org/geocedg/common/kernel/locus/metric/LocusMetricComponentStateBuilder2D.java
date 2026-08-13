/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Builds one complete component state privately before index publication. */
@FunctionalInterface
public interface LocusMetricComponentStateBuilder2D {
	/**
	 * @param key complete component key
	 * @return privately built immutable component state
	 */
	LocusMetricComponentState2D buildComponentState(
			LocusMetricIndexKey2D key);
}
