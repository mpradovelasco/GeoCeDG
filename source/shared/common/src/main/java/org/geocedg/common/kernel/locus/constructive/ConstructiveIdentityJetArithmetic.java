/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Objects;

import org.geocedg.common.kernel.locus.exact.ExactRational2D;

/**
 * Exact jets of order two over the field of one constructive identity certificate: the same
 * indeterminates, radicand and cell as its arithmetic, with the rules of differentiation of the
 * field operations. Every sign is the certified sign of a value, recorded in the same cell; a
 * derivative never decides a branch.
 */
final class ConstructiveIdentityJetArithmetic implements ConstructiveArithmetic<CertificateJet> {

	private final ConstructiveIdentityArithmetic field;

	/** @param field the arithmetic of the attempt, whose indeterminates and cell are shared */
	ConstructiveIdentityJetArithmetic(ConstructiveIdentityArithmetic field) {
		this.field = Objects.requireNonNull(field);
	}

	/** @return the arithmetic of the attempt */
	ConstructiveIdentityArithmetic getField() {
		return field;
	}

	/** @return the value as a jet whose derivatives are zero */
	CertificateJet constant(CertificateElement value) {
		return new CertificateJet(value, null, null);
	}

	@Override
	public CertificateJet ofBinary64(double value) {
		return constant(field.ofBinary64(value));
	}

	@Override
	public CertificateJet ofRational(ExactRational2D value) {
		return constant(field.ofRational(value));
	}

	@Override
	public CertificateJet add(CertificateJet a, CertificateJet b) {
		return new CertificateJet(field.add(a.getValue(), b.getValue()),
				sum(a.getFirst(), b.getFirst()), sum(a.getSecond(), b.getSecond()));
	}

	@Override
	public CertificateJet subtract(CertificateJet a, CertificateJet b) {
		return add(a, negate(b));
	}

	@Override
	public CertificateJet multiply(CertificateJet a, CertificateJet b) {
		CertificateElement value = field.multiply(a.getValue(), b.getValue());
		if (a.isConstant() && b.isConstant()) {
			return constant(value);
		}
		// (ab)' = a'b + ab'; (ab)'' = a''b + 2a'b' + ab''
		CertificateElement first = sum(product(a.getFirst(), b.getValue()),
				product(a.getValue(), b.getFirst()));
		CertificateElement cross = product(a.getFirst(), b.getFirst());
		CertificateElement second = sum(sum(product(a.getSecond(), b.getValue()),
				cross == null ? null : field.add(cross, cross)),
				product(a.getValue(), b.getSecond()));
		return new CertificateJet(value, first, second);
	}

	@Override
	public CertificateJet divide(CertificateJet a, CertificateJet b) {
		CertificateElement quotient = field.divide(a.getValue(), b.getValue());
		if (a.isConstant() && b.isConstant()) {
			return constant(quotient);
		}
		// q = a / b; q' = (a' - q b') / b; q'' = (a'' - 2 q' b' - q b'') / b
		CertificateElement first = quotientOf(difference(a.getFirst(),
				product(quotient, b.getFirst())), b.getValue());
		CertificateElement twice = product(first, b.getFirst());
		CertificateElement second = quotientOf(difference(difference(a.getSecond(),
				twice == null ? null : field.add(twice, twice)),
				product(quotient, b.getSecond())), b.getValue());
		return new CertificateJet(quotient, first, second);
	}

	@Override
	public CertificateJet negate(CertificateJet a) {
		return new CertificateJet(field.negate(a.getValue()),
				a.getFirst() == null ? null : field.negate(a.getFirst()),
				a.getSecond() == null ? null : field.negate(a.getSecond()));
	}

	@Override
	public CertificateJet sqrt(CertificateJet a) {
		CertificateElement root = field.sqrt(a.getValue());
		if (a.isConstant()) {
			return constant(root);
		}
		// r = sqrt(a); r' = a' / (2r); r'' = (a'' - 2 r'^2) / (2r)
		CertificateElement twice = field.add(root, root);
		CertificateElement first = quotientOf(a.getFirst(), twice);
		CertificateElement square = product(first, first);
		CertificateElement second = quotientOf(difference(a.getSecond(),
				square == null ? null : field.add(square, square)), twice);
		return new CertificateJet(root, first, second);
	}

	@Override
	public int sign(CertificateJet a) {
		return field.sign(a.getValue());
	}

	@Override
	public int signOfLiveCheck(CertificateJet a) {
		return field.signOfLiveCheck(a.getValue());
	}

	@Override
	public boolean isExact() {
		return false;
	}

	@Override
	public CertificateJet cos(double angle) {
		return null;
	}

	@Override
	public CertificateJet sin(double angle) {
		return null;
	}

	@Override
	public double[] enclosure(CertificateJet a) {
		return field.enclosure(a.getValue());
	}

	private CertificateElement sum(CertificateElement a, CertificateElement b) {
		return a == null ? b : b == null ? a : field.add(a, b);
	}

	private CertificateElement difference(CertificateElement a, CertificateElement b) {
		return b == null ? a : a == null ? field.negate(b) : field.subtract(a, b);
	}

	private CertificateElement product(CertificateElement a, CertificateElement b) {
		return a == null || b == null ? null : field.multiply(a, b);
	}

	private CertificateElement quotientOf(CertificateElement a, CertificateElement b) {
		return a == null ? null : field.divide(a, b);
	}
}
