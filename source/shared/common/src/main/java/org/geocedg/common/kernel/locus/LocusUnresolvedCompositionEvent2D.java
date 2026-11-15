/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.common.kernel.locus;

import java.util.Objects;

/** An upstream structural event whose geometric action on this image is unproved. */
public record LocusUnresolvedCompositionEvent2D(double parameter, LocusBoundarySource2D source) {
	public LocusUnresolvedCompositionEvent2D {
		if (!Double.isFinite(parameter)) {
			throw new IllegalArgumentException("A structural event needs a finite direction");
		}
		parameter = parameter == 0 ? 0 : parameter;
		Objects.requireNonNull(source);
	}
}
