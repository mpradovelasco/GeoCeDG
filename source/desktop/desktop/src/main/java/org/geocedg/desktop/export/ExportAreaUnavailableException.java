/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

/**
 * PRE-G9B-R6-plus-B: no valid export area could be resolved, so no picture is
 * produced and no empty file is left behind (DQ-B9).
 */
public final class ExportAreaUnavailableException extends IllegalStateException {
	private static final long serialVersionUID = 1L;

	/**
	 * @param message diagnostic message
	 */
	public ExportAreaUnavailableException(String message) {
		super(message);
	}
}
