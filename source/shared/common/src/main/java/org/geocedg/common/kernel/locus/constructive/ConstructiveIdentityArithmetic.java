/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.exact.ExactRational2D;

/**
 * The number system of one constructive identity certificate
 * ({@code locus-v2-constructive-authority.md} section 8): the field {@code Q(V)[sqrt(Delta)]} of
 * the rational functions of the indeterminates of the attempt, extended in version 1 by at most
 * one square root that is not a perfect square; perfect squares are extracted exactly, and a
 * second independent square root or a nested one is outside the class. Every sign that the
 * evaluation of the cone asks for is certified by the outward-rounded enclosure of the element on
 * the decision box and recorded with that sign: the recorded conditions are the cell of the
 * certificate. A sign whose enclosure contains zero is undecided; the zero element has the exact
 * sign zero, which no box changes. The signs of the checks against the live algorithm are
 * certified the same way and kept apart, because they read the live state of the revision.
 */
final class ConstructiveIdentityArithmetic implements ConstructiveArithmetic<CertificateElement> {

	/** One recorded sign condition: an element and its certified sign on the box. */
	static final class Condition {
		private final CertificateElement element;
		private final int sign;

		Condition(CertificateElement element, int sign) {
			this.element = element;
			this.sign = sign;
		}

		/** @return the element */
		CertificateElement getElement() {
			return element;
		}

		/** @return its certified sign, {@code 1} or {@code -1} */
		int getSign() {
			return sign;
		}
	}

	private final ConstructiveIdentityCounters counters;
	private final OutwardInterval[] box =
			new OutwardInterval[CertificatePolynomial.INDETERMINATES];
	private final List<String> names = new ArrayList<>();
	private final List<Condition> cell = new ArrayList<>();
	private final List<Condition> liveConditions = new ArrayList<>();
	private CertificatePolynomial radicand;

	/** @param counters the counters of the attempt, with its budget */
	ConstructiveIdentityArithmetic(ConstructiveIdentityCounters counters) {
		this.counters = Objects.requireNonNull(counters);
	}

	/**
	 * The same field for a recheck at another revision: the same indeterminates and radicand,
	 * with other counters and an empty cell, so that a recheck never changes the certificate
	 * whose conditions it encloses again.
	 *
	 * @param recheck the counters of the recheck
	 * @return the field of the recheck
	 */
	ConstructiveIdentityArithmetic forRecheck(ConstructiveIdentityCounters recheck) {
		ConstructiveIdentityArithmetic copy = new ConstructiveIdentityArithmetic(recheck);
		System.arraycopy(box, 0, copy.box, 0, box.length);
		copy.names.addAll(names);
		copy.radicand = radicand;
		return copy;
	}

	/** @return the counters of the attempt */
	ConstructiveIdentityCounters getCounters() {
		return counters;
	}

	/**
	 * A new indeterminate of the attempt.
	 *
	 * @param name the coordinate it stands for, in a report only
	 * @param interval its interval on the decision box, which contains its ideal value
	 * @return the indeterminate
	 */
	CertificateElement indeterminate(String name, OutwardInterval interval) {
		int index = counters.allocateIndeterminate();
		box[index] = Objects.requireNonNull(interval);
		names.add(name);
		return new CertificateElement(
				CertificateRationalFunction.of(CertificatePolynomial.indeterminate(index)), zero());
	}

	/** @return the names of the indeterminates, in index order */
	List<String> getNames() {
		return Collections.unmodifiableList(new ArrayList<>(names));
	}

	/** @return a copy of the decision box, one interval for each indeterminate */
	OutwardInterval[] getBox() {
		OutwardInterval[] copy = new OutwardInterval[names.size()];
		System.arraycopy(box, 0, copy, 0, copy.length);
		return copy;
	}

	/** @return the recorded conditions of the cell, in evaluation order */
	List<Condition> getCell() {
		return Collections.unmodifiableList(new ArrayList<>(cell));
	}

	/** @return the certified signs of the checks against the live algorithm */
	List<Condition> getLiveConditions() {
		return Collections.unmodifiableList(new ArrayList<>(liveConditions));
	}

	/** @return the radicand of the extension, or {@code null} for the base field */
	CertificatePolynomial getRadicand() {
		return radicand;
	}

	@Override
	public CertificateElement ofBinary64(double value) {
		return constant(ExactRational2D.ofBinary64(value));
	}

	@Override
	public CertificateElement ofRational(ExactRational2D value) {
		return constant(value);
	}

	@Override
	public CertificateElement add(CertificateElement a, CertificateElement b) {
		return new CertificateElement(a.getRational().add(b.getRational(), counters),
				a.getIrrational().add(b.getIrrational(), counters));
	}

	@Override
	public CertificateElement subtract(CertificateElement a, CertificateElement b) {
		return add(a, negate(b));
	}

	@Override
	public CertificateElement multiply(CertificateElement a, CertificateElement b) {
		if (a.isRational() && b.isRational()) {
			return new CertificateElement(a.getRational().multiply(b.getRational(), counters),
					zero());
		}
		CertificateRationalFunction delta = CertificateRationalFunction.of(radicand);
		CertificateRationalFunction rational = a.getRational().multiply(b.getRational(), counters)
				.add(a.getIrrational().multiply(b.getIrrational(), counters)
						.multiply(delta, counters), counters);
		CertificateRationalFunction irrational = a.getRational()
				.multiply(b.getIrrational(), counters)
				.add(a.getIrrational().multiply(b.getRational(), counters), counters);
		return new CertificateElement(rational, irrational);
	}

	/**
	 * @throws ConstructiveIdentityException with {@code OUT_OF_CLASS} if the divisor is the zero
	 *         element, which the evaluation never asks for, or if its norm vanishes because the
	 *         radicand is a square that was not recognized
	 */
	@Override
	public CertificateElement divide(CertificateElement a, CertificateElement b) {
		if (b.isZero()) {
			throw new ConstructiveIdentityException(
					ConstructiveIdentityCertificate.Cause.OUT_OF_CLASS, "a division by zero");
		}
		if (b.isRational()) {
			return new CertificateElement(a.getRational().divide(b.getRational(), counters),
					a.getIrrational().divide(b.getRational(), counters));
		}
		CertificateRationalFunction delta = CertificateRationalFunction.of(radicand);
		CertificateRationalFunction norm = b.getRational().multiply(b.getRational(), counters)
				.subtract(b.getIrrational().multiply(b.getIrrational(), counters)
						.multiply(delta, counters), counters);
		if (norm.isZero()) {
			throw new ConstructiveIdentityException(
					ConstructiveIdentityCertificate.Cause.OUT_OF_CLASS,
					"the radicand of the extension is a square");
		}
		CertificateElement inverse = new CertificateElement(
				b.getRational().divide(norm, counters),
				b.getIrrational().negate().divide(norm, counters));
		return multiply(a, inverse);
	}

	@Override
	public CertificateElement negate(CertificateElement a) {
		return new CertificateElement(a.getRational().negate(), a.getIrrational().negate());
	}

	/**
	 * The nonnegative square root. A value {@code num / den} of the base field has the root
	 * {@code sqrt(s) / scale}, with {@code s = num} and {@code scale = sqrt(den)} when the
	 * denominator is a perfect square, otherwise {@code s = num den} and {@code scale = den}. A
	 * perfect square {@code s} gives a root in the base field; otherwise {@code s} becomes the
	 * radicand of the extension, or must be a square times the radicand already there. The branch
	 * is the one whose sign is certified positive on the box.
	 *
	 * @throws ConstructiveIdentityException with {@code OUT_OF_CLASS} for a nested or a second
	 *         independent square root, with {@code PRECONDITION_UNDECIDED} when the sign of the
	 *         root is not certified
	 */
	@Override
	public CertificateElement sqrt(CertificateElement a) {
		if (a.isZero()) {
			return a;
		}
		if (!a.isRational()) {
			throw new ConstructiveIdentityException(
					ConstructiveIdentityCertificate.Cause.OUT_OF_CLASS, "a nested square root");
		}
		CertificateRationalFunction value = a.getRational();
		CertificatePolynomial denominatorRoot = value.getDenominator().squareRoot(counters);
		CertificatePolynomial s = denominatorRoot != null ? value.getNumerator()
				: value.getNumerator().multiply(value.getDenominator(), counters);
		CertificatePolynomial scale = denominatorRoot != null ? denominatorRoot
				: value.getDenominator();
		CertificatePolynomial root = s.squareRoot(counters);
		CertificateElement candidate;
		if (root != null) {
			candidate = new CertificateElement(CertificateRationalFunction.of(root, scale,
					counters), zero());
		} else if (radicand == null) {
			radicand = s;
			candidate = new CertificateElement(zero(), CertificateRationalFunction.of(
					CertificatePolynomial.constant(ExactRational2D.of(1)), scale, counters));
		} else {
			CertificatePolynomial joint = s.multiply(radicand, counters).squareRoot(counters);
			if (joint == null) {
				throw new ConstructiveIdentityException(
						ConstructiveIdentityCertificate.Cause.OUT_OF_CLASS,
						"a second independent square root");
			}
			candidate = new CertificateElement(zero(), CertificateRationalFunction.of(joint,
					radicand.multiply(scale, counters), counters));
		}
		int sign = sign(candidate);
		if (sign == UNDECIDED || sign == 0) {
			throw new ConstructiveIdentityException(
					ConstructiveIdentityCertificate.Cause.PRECONDITION_UNDECIDED,
					"the sign of a square root is not certified");
		}
		return sign > 0 ? candidate : negate(candidate);
	}

	@Override
	public int sign(CertificateElement a) {
		return certify(a, cell);
	}

	@Override
	public int signOfLiveCheck(CertificateElement a) {
		return certify(a, liveConditions);
	}

	@Override
	public boolean isExact() {
		return false;
	}

	@Override
	public CertificateElement cos(double angle) {
		return null;
	}

	@Override
	public CertificateElement sin(double angle) {
		return null;
	}

	@Override
	public double[] enclosure(CertificateElement a) {
		OutwardInterval enclosure = enclose(a, box);
		return enclosure == null
				? new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}
				: new double[] {enclosure.getLower(), enclosure.getUpper()};
	}

	/**
	 * @param a an element
	 * @param on a box with an interval for each indeterminate of the attempt
	 * @return a rigorous enclosure of the element over the box, or {@code null} when no finite
	 *         enclosure is certified
	 */
	OutwardInterval enclose(CertificateElement a, OutwardInterval[] on) {
		return enclose(a, radicand, on, counters);
	}

	/**
	 * @param a an element
	 * @param radicand the radicand of its extension, or {@code null} for the base field
	 * @param on a box with an interval for each indeterminate of the attempt
	 * @param counters the counters that record the polynomials of the enclosure
	 * @return a rigorous enclosure of the element over the box, or {@code null} when no finite
	 *         enclosure is certified
	 */
	static OutwardInterval enclose(CertificateElement a, CertificatePolynomial radicand,
			OutwardInterval[] on, ConstructiveIdentityCounters counters) {
		OutwardInterval[] full = new OutwardInterval[CertificatePolynomial.INDETERMINATES];
		System.arraycopy(on, 0, full, 0, Math.min(on.length, full.length));
		for (int index = 0; index < full.length; index++) {
			if (full[index] == null) {
				full[index] = OutwardInterval.point(0);
			}
		}
		OutwardInterval value = a.getRational().enclose(full, counters);
		if (value == null) {
			return null;
		}
		if (a.isRational()) {
			return value;
		}
		OutwardInterval coefficient = a.getIrrational().enclose(full, counters);
		OutwardInterval delta = radicand.enclose(full, counters);
		if (coefficient == null || !(delta.getLower() >= 0) || !delta.isFinite()) {
			return null;
		}
		return value.add(coefficient.multiply(delta.sqrt()));
	}

	private int certify(CertificateElement a, List<Condition> record) {
		if (a.isZero()) {
			return 0;
		}
		OutwardInterval enclosure = enclose(a, box);
		int sign = enclosure == null ? UNDECIDED : enclosure.certifiedSign();
		if (sign == 1 || sign == -1) {
			record.add(new Condition(a, sign));
			return sign;
		}
		return UNDECIDED;
	}

	private CertificateElement constant(ExactRational2D value) {
		return new CertificateElement(
				CertificateRationalFunction.of(CertificatePolynomial.constant(value)), zero());
	}

	private static CertificateRationalFunction zero() {
		return CertificateRationalFunction.of(
				CertificatePolynomial.constant(ExactRational2D.of(0)));
	}
}
