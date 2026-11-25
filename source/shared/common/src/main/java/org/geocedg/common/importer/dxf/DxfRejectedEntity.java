/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

/**
 * One candidate entity that was not imported, with its single typed cause (the first failing
 * selection rule or admission predicate in the order of sections 4.2 and 4.4).
 */
public final class DxfRejectedEntity {

	private final String section;
	private final int ordinal;
	private final String handle;
	private final String layer;
	private final DxfCause cause;

	DxfRejectedEntity(String section, int ordinal, String handle, String layer,
			DxfCause cause) {
		this.section = section;
		this.ordinal = ordinal;
		this.handle = handle;
		this.layer = layer;
		this.cause = cause;
	}

	/**
	 * @return {@code ENTITIES} or {@code BLOCKS}
	 */
	public String section() {
		return section;
	}

	/**
	 * @return one-based ordinal among the entries of that section
	 */
	public int ordinal() {
		return ordinal;
	}

	/**
	 * @return handle as read, provenance only
	 */
	public String handle() {
		return handle;
	}

	/**
	 * @return layer as read, provenance only
	 */
	public String layer() {
		return layer;
	}

	/**
	 * @return the typed cause
	 */
	public DxfCause cause() {
		return cause;
	}
}
