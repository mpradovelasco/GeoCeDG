/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Objects;

/**
 * An element {@code a + b sqrt(Delta)} of the field {@code Q(V)[sqrt(Delta)]} of one
 * constructive identity certificate, with {@code a} and {@code b} rational functions of the
 * indeterminates {@code V}. The radicand {@code Delta} belongs to the arithmetic of the attempt;
 * {@code b} is zero for every element of the base field. An element is the zero element exactly
 * when both parts are the zero function.
 */
final class CertificateElement {

	private final CertificateRationalFunction rational;
	private final CertificateRationalFunction irrational;

	CertificateElement(CertificateRationalFunction rational,
			CertificateRationalFunction irrational) {
		this.rational = Objects.requireNonNull(rational);
		this.irrational = Objects.requireNonNull(irrational);
	}

	/** @return the part {@code a} */
	CertificateRationalFunction getRational() {
		return rational;
	}

	/** @return the part {@code b}, the coefficient of {@code sqrt(Delta)} */
	CertificateRationalFunction getIrrational() {
		return irrational;
	}

	/** @return whether it is the zero element of the field */
	boolean isZero() {
		return rational.isZero() && irrational.isZero();
	}

	/** @return whether it belongs to the base field {@code Q(V)} */
	boolean isRational() {
		return irrational.isZero();
	}

	/** @return the terms of both parts */
	int size() {
		return rational.size() + irrational.size();
	}

	@Override
	public String toString() {
		return irrational.isZero() ? rational.toString()
				: rational + " + (" + irrational + ") sqrt(Delta)";
	}
}
