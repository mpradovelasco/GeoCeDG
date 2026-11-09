/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.document.controls;

/** Rejection before publishing a document control layout. */
public final class ControlLayoutException extends IllegalStateException {
	public enum Code {
		CONTEXT, VERSION, GRAMMAR, LIMIT, DUPLICATE, ORPHAN, OWNER
	}

	private final Code code;

	/** Creates a layout rejection with a stable diagnostic code. */
	public ControlLayoutException(Code code, String detail) {
		super("CU8_LAYOUT_" + code + ": " + detail);
		this.code = code;
	}

	public Code code() {
		return code;
	}
}
