/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus.interaction;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;

/** One-use handoff from an owning selection to the normal forward point factory. */
public abstract class LocusPointPublication2D {

	LocusPointPublication2D() {
		// Only selection protocols in this package may provide the handoff.
	}

	public abstract LocusSemanticAddress2D takePublicationAddress(GeoLocusV2 source);

	public abstract MetricPositionBinding2D takePreparedBinding(GeoLocusV2 source);
}
