/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

/**
 * Mathematical nature of an admitted record (section 3), decided by the effective weights
 * alone by exact binary64 comparison. The DXF rational flag never decides it.
 */
public enum DxfNature {
	/** All effective weights equal to 1 (implicit or explicit). */
	POLYNOMIAL,
	/** Explicit weights all exactly equal to a common value different from 1. */
	RATIONAL_EQUAL_WEIGHTS,
	/** Explicit weights not all equal. */
	RATIONAL
}
