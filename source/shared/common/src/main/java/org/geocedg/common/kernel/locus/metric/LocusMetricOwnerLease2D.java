/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Consumer lease for one active source-locus metric owner. */
public interface LocusMetricOwnerLease2D extends AutoCloseable {
	/** @return active shared owner */
	LocusMetricSharedOwner2D getOwner();

	@Override
	void close();
}
