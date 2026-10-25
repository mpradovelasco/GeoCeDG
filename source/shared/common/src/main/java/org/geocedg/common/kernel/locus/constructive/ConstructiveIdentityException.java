/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Objects;

/**
 * A constructive identity certificate cannot be attempted further: a construct outside its class,
 * an undecided precondition or an ideal value without a rigorous enclosure. The attempt ends with
 * {@code NOT_ESTABLISHED} and the typed cause; nothing is decided.
 */
final class ConstructiveIdentityException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final ConstructiveIdentityCertificate.Cause cause;

	ConstructiveIdentityException(ConstructiveIdentityCertificate.Cause cause, String message) {
		super(cause + ": " + message);
		this.cause = Objects.requireNonNull(cause);
	}

	/** @return the typed cause */
	ConstructiveIdentityCertificate.Cause getTypedCause() {
		return cause;
	}
}
