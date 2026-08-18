/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/**
 * Atomic runtime participant for a prepared identity-graph lifecycle change.
 */
public interface SpatialLifecycleRuntime {
	/**
	 * Prepares all semantic topology against an immutable graph while the live
	 * registry and current algorithms remain unchanged.
	 *
	 * @param graph prospective complete identity graph
	 * @return a prepared switch whose commit either fails before its terminal
	 *         runtime swap or completes successfully
	 */
	PreparedSwitch prepare(SpatialLifecycleProspectiveGraph graph);

	/** Prepared runtime half of one atomic lifecycle transaction. */
	interface PreparedSwitch {
		/**
		 * Installs the already prepared topology. A successful return is terminal;
		 * callers must not subsequently invoke {@link #rollback()}.
		 */
		void commit();

		/**
		 * Removes abandoned prepared state, or restores it after a throwing commit
		 * that has not completed its terminal runtime swap.
		 */
		void rollback();
	}
}
