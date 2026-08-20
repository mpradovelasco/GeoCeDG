/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Semantic role of one object-to-projection binding. */
public enum ProjectionBindingRole {
	/** The participating geo is not an object-to-projection binding. */
	NOT_APPLICABLE,
	DEFINING,
	DERIVED,
	AUXILIARY,
	ANALYSIS,
	PRESENTATION
}
