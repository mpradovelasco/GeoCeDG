/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

/**
 * Non-blocking diagnostics of an admitted record (sections 4.3, 4.4, 4.6, 5, 9) or of the
 * import report (sections 4.2, 6, 8). None of them changes the curve.
 */
public enum DxfDiagnostic {
	/** Section 4.6: flag 4 set and no code 41; implicit unit weights. */
	RATIONAL_FLAG_WITH_IMPLICIT_UNIT_WEIGHTS,
	/** Section 4.6: flag 4 clear and {@code n + 1} weights all equal to 1. */
	EXPLICIT_UNIT_WEIGHTS_WITHOUT_RATIONAL_FLAG,
	/** Section 4.4: flag 1 disagrees with exact closure; the data decide. */
	CLOSED_FLAG_CONTRADICTED,
	/** Section 4.3: codes outside the recognized set; listed, never used. */
	UNRECOGNIZED_GROUP_CODES,
	/** Section 4.4: control and fit data present; fit points ignored. */
	FIT_POINTS_IGNORED,
	/** Section 4.3: tolerances 42-44 recorded as ignored. */
	TOLERANCES_IGNORED,
	/** Section 4.3: fit tangents recorded as ignored. */
	TANGENTS_IGNORED,
	/** Section 4.3: plane normal recorded, never used to transform. */
	NORMAL_RECORDED,
	/** Section 8: tessellation work limit reached; the render is incomplete. */
	RENDER_INCOMPLETE,
	/** Report, section 6: DXF units and document unit both specified and different. */
	UNITS_DIFFER,
	/** Report, section 4.2: INSERT entities present; never expanded. */
	INSERT_NOT_EXPANDED
}
