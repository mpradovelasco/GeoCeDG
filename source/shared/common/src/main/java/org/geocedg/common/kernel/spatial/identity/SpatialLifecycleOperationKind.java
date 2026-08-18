/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Explicit, non-inferred lifecycle operation carried by one transaction. */
public enum SpatialLifecycleOperationKind {
	SEMANTIC_NO_OP,
	COMPATIBLE_DEFINITION_CHANGE,
	ADMITTED_TOPOLOGY_CHANGE,
	BINDING_ADD,
	BINDING_REMOVE,
	BINDING_REROLE,
	MAP_ADD,
	MAP_REMOVE,
	MAP_CHANGE,
	MAP_REROLE,
	FRAME_CHANGE,
	RELATION_ADD,
	RELATION_REMOVE,
	RELATION_CHANGE,
	RELATION_REROLE,
	SYSTEM_REPLACEMENT,
	TRUE_REPLACEMENT,
	REFERENCE_RECOVERY,
	EXPLICIT_MIGRATION,
	COMPLETE_CLOSURE_COPY,
	DECLARED_EXTERNAL_COPY
}
