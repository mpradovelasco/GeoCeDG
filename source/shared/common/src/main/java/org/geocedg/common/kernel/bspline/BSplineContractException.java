/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

/**
 * Fail-closed rejection of the persisted family contract of a {@code BSpline} command
 * ({@code bspline-2d.md} sections 8.3 and 10.5). It is unchecked and not a {@code MyError},
 * so {@code MyXMLio.doParseXML} rethrows it on every route and restores the construction
 * that existed before the parse; the inherited command-error collection never absorbs it.
 */
public final class BSplineContractException extends IllegalStateException {
	private static final long serialVersionUID = 1L;

	/** Typed causes of section 8.3 and 10.5. */
	public enum Code {
		/** A {@code contract} other than {@code bspline-2d/v1}; never read as v1. */
		UNSUPPORTED_CONTRACT_VERSION,
		/** A native {@code BSpline} command without its {@code geocedgBSpline} element. */
		CONTRACT_MISSING,
		/**
		 * A malformed family element: misplaced or duplicated, unknown attribute or child,
		 * missing or unknown {@code knots}, a knot rule that disagrees with the argument
		 * count, text content, or a malformed approximation provenance block.
		 */
		MALFORMED_CONTRACT,
		/** An approximation provenance block whose {@code version} is not supported. */
		UNSUPPORTED_PROVENANCE_VERSION
	}

	private final Code code;

	/**
	 * @param code typed cause
	 * @param detail technical detail for the log
	 */
	public BSplineContractException(Code code, String detail) {
		super("geocedgBSpline " + code + ": " + detail);
		this.code = code;
	}

	/**
	 * @return typed cause
	 */
	public Code getCode() {
		return code;
	}

	/**
	 * @param failure a load failure
	 * @return the family-contract rejection in its cause or suppressed chain, or null
	 */
	public static BSplineContractException find(Throwable failure) {
		Throwable current = failure;
		int depth = 0;
		while (current != null && depth < 32) {
			if (current instanceof BSplineContractException) {
				return (BSplineContractException) current;
			}
			for (Throwable suppressed : current.getSuppressed()) {
				if (suppressed instanceof BSplineContractException) {
					return (BSplineContractException) suppressed;
				}
			}
			current = current.getCause();
			depth++;
		}
		return null;
	}
}
