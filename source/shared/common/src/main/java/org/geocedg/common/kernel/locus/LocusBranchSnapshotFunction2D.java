/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/** Provider-owned dynamic branch/domain publication rule. */
public interface LocusBranchSnapshotFunction2D {
	/**
	 * Creates the branch publication for captured sources and prior revision.
	 *
	 * @return immutable definition status and branch snapshot
	 */
	LocusBranchSnapshot2D create(LocusSourceSnapshot2D sources,
			LocusDefinition2D previousDefinition);
}
