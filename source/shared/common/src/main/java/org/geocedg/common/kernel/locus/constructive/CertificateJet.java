/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Objects;

/**
 * A value of the field of a constructive identity certificate with its first two derivatives in
 * the parameter of the driver of a locus slice, at the structural point: the exact jet of order
 * two that decides the order of contact. A {@code null} derivative is zero, so the objects that do
 * not move with the driver cost nothing more than their value.
 */
final class CertificateJet {

	private final CertificateElement value;
	private final CertificateElement first;
	private final CertificateElement second;

	CertificateJet(CertificateElement value, CertificateElement first,
			CertificateElement second) {
		this.value = Objects.requireNonNull(value);
		this.first = first;
		this.second = second;
	}

	/** @return the value */
	CertificateElement getValue() {
		return value;
	}

	/** @return the first derivative, or {@code null} for zero */
	CertificateElement getFirst() {
		return first;
	}

	/** @return the second derivative, or {@code null} for zero */
	CertificateElement getSecond() {
		return second;
	}

	/** @return whether both derivatives are zero by construction */
	boolean isConstant() {
		return first == null && second == null;
	}

	@Override
	public String toString() {
		return "[" + value + "; " + first + "; " + second + "]";
	}
}
