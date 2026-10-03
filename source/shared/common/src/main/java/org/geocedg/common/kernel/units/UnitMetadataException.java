/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.units;

/**
 * Fail-closed rejection of recognized unit metadata (unit-system v1.0, section 8.7).
 * It is unchecked and not a {@code MyError}, so the Desktop document preflight and
 * the rejected-parse restore treat it as a rejection of the whole load.
 */
public final class UnitMetadataException extends IllegalStateException {
	private static final long serialVersionUID = 1L;

	/** Defect classes reported to the user (DQ-D1-4). */
	public enum Code {
		/** A {@code version} greater than the highest supported version. */
		UNSUPPORTED_VERSION,
		/** A structurally malformed recognized version-1 element. */
		MALFORMED_ELEMENT,
		/** An invalid factor, or a selected {@code usm} without a valid factor. */
		INVALID_USM_FACTOR,
		/** More than one element in one document construction. */
		DUPLICATE_ELEMENT,
		/** An element outside the document {@code <construction>}. */
		MISPLACED_ELEMENT
	}

	private final Code code;

	/**
	 * @param code defect class
	 * @param detail technical detail for the log
	 */
	public UnitMetadataException(Code code, String detail) {
		super("geocedgUnits " + code + ": " + detail);
		this.code = code;
	}

	/**
	 * @return defect class
	 */
	public Code getCode() {
		return code;
	}

	/**
	 * @param failure a load failure
	 * @return the unit rejection in its cause or suppressed chain, or {@code null}
	 */
	public static UnitMetadataException find(Throwable failure) {
		Throwable current = failure;
		int depth = 0;
		while (current != null && depth < 32) {
			if (current instanceof UnitMetadataException) {
				return (UnitMetadataException) current;
			}
			for (Throwable suppressed : current.getSuppressed()) {
				if (suppressed instanceof UnitMetadataException) {
					return (UnitMetadataException) suppressed;
				}
			}
			current = current.getCause();
			depth++;
		}
		return null;
	}
}
