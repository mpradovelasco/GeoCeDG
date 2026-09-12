/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Closed non-mutating preflight outcome for one semantic redefine proposal. */
public enum SpatialRedefineAssessmentStatus {
	ADVANCED_RETAIN_AVAILABLE,
	ADVANCED_RETAIN_AVAILABLE_WITH_RELOCATION,
	LEGACY_REPLACEMENT_AVAILABLE,
	INVALID_DAG,
	INCOMPATIBLE_HOST_REDEFINE,
	UNSUPPORTED,
	AMBIGUOUS,
	STALE_ASSESSMENT
}
