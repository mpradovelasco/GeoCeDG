/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

/** Type-specific projection-sufficiency result. */
public enum SpatialCertificateStatus {
	NOT_EVALUATED,
	VALID,
	UNDERDETERMINED,
	AMBIGUOUS,
	INCONSISTENT_PROJECTIONS,
	DEGENERATE,
	UNDEFINED
}
