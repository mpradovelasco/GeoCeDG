/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

/**
 * PRE-G9B-R6-plus-C (C4, {@code DQ-C6}): a physical output that its format
 * cannot represent. The export fails explicitly with this message; no size is
 * ever clamped.
 */
public final class PhysicalExportLimitException extends IllegalStateException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message explicit failure text naming the quantity and its value
	 */
	public PhysicalExportLimitException(String message) {
		super(message);
	}
}
