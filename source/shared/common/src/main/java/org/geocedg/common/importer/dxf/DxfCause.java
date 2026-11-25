/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

/**
 * Typed causes of {@code dxf-spline-reference/v1}. File causes reject the whole import
 * (section 4.1); selection causes (section 4.2) and admission causes (section 4.4, in
 * predicate order) reject one entity; {@link #SINGULAR_TRANSFORM} rejects a requested
 * transformation (section 6).
 */
public enum DxfCause {
	/** File: the binary DXF sentinel. */
	BINARY_DXF_UNSUPPORTED,
	/** File: odd line count, nonnumeric group code, missing or unterminated ENTITIES. */
	MALFORMED_DXF,
	/** File: size above {@code FILE_BYTES_MAX} or SPLINE count above the limit. */
	FILE_LIMIT_EXCEEDED,
	/** Selection: paper space (group 67 = 1). */
	PAPER_SPACE_UNSUPPORTED,
	/** Selection: inside BLOCKS (never reached through INSERT, which is never expanded). */
	BLOCK_CONTENT_UNSUPPORTED,
	/** Predicate 1: a numeric value is not a well-formed DXF real or integer. */
	MALFORMED_REAL,
	/** Predicate 1: a real overflows to an infinity. */
	NONFINITE_VALUE,
	/** Predicate 2: periodic flag (bit 2 of code 70). */
	PERIODIC_UNSUPPORTED,
	/** Predicate 3: no control-point data. */
	FIT_POINTS_ONLY_UNSUPPORTED,
	/** Predicate 4: degree outside {@code 1..P_MAX_IMPORT}. */
	DEGREE_OUT_OF_RANGE,
	/** Predicate 5: points read differ from code 73 (or x, y, z counts differ). */
	CONTROL_POINT_COUNT_MISMATCH,
	/** Predicate 5: {@code n + 1 < p + 1}. */
	TOO_FEW_CONTROL_POINTS,
	/** Predicate 5: {@code n + 1 > N_MAX_IMPORT}. */
	SIZE_OUT_OF_RANGE,
	/** Predicate 6: knots read, code 72 and {@code n + p + 2} differ. */
	KNOT_COUNT_MISMATCH,
	/** Predicate 7. */
	KNOTS_DECREASING,
	/** Predicate 8: ends not clamped. */
	NOT_CLAMPED_UNSUPPORTED,
	/** Predicate 8: {@code u_p = u_{n+1}}. */
	EMPTY_DOMAIN,
	/** Predicate 9: interior multiplicity above {@code p}. */
	INTERIOR_MULTIPLICITY_UNSUPPORTED,
	/** Predicate 10: code-41 count neither 0 nor {@code n + 1}. */
	WEIGHT_COUNT_MISMATCH,
	/** Predicate 11: rational flag clear and an explicit weight different from 1. */
	NON_UNIT_WEIGHTS_WITHOUT_RATIONAL_FLAG,
	/** Predicate 12: an explicit weight is not positive. */
	NONPOSITIVE_WEIGHT,
	/** Predicate 12: {@code max w / min w > W_RATIO_MAX}. */
	WEIGHT_RATIO_OUT_OF_RANGE,
	/** Predicate 13: control points not all at the same {@code z}. */
	NONPLANAR_UNSUPPORTED,
	/** Transformation: nonfinite coefficient or {@code a d - b c = 0}. */
	SINGULAR_TRANSFORM
}
