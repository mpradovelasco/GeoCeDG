/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Provider-owned semantic effect of one admitted redefine transaction. */
public enum SpatialRedefineEffect {
	/** The semantic definition and topology are unchanged. */
	NO_OP,
	/** The definition changed without changing provider-owned topology. */
	DEFINITION_CHANGE,
	/** The provider explicitly admits both a definition and topology change. */
	ADMITTED_TOPOLOGY_CHANGE
}
