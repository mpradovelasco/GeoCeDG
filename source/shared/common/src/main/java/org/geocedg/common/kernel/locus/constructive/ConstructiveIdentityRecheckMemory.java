/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.locus.exact.ExactRational2D;

/**
 * The reusable exact memory of one constructive identity certificate for its rechecks at later
 * revisions (PRE-G9B-CU3 focal optimization of the recheck, T1 to T3). It keeps mathematical
 * objects derived exactly from the immutable certificate and never a decision:
 * <ul>
 * <li>the prepared margin of each checked line-circle output. With the formal branches
 * {@code O} and {@code S} and the live point {@code P = (x, y)}, the margin
 * {@code |O - P|^2 - |S - P|^2} is the exact identity {@code A + x U + y W}, with
 * {@code A = |O|^2 - |S|^2}, {@code U = -2 (Ox - Sx)} and {@code W = -2 (Oy - Sy)} built once;</li>
 * <li>the specialization of the polynomials of the cell, of the radicand and of the prepared
 * margins at the exact values of the indeterminates whose interval on the box of the revision is
 * a single point. The values are substituted one indeterminate at a time in index order, and
 * each step is kept with its value, so that a change of one value only repeats the steps from
 * that indeterminate on. Exact substitution gives the same canonical polynomial whatever the
 * history, so an enclosure of this memory depends only on the box and the live points of its
 * revision and never on the revisions before it;</li>
 * <li>the partial derivatives of those polynomials, which they keep once computed.</li>
 * </ul>
 * Every recheck still encloses every condition and every margin on the box of its own revision.
 * An enclosure from this memory that does not certify the recorded sign, or any failure building
 * it, leaves the decision to the procedure of the candidate, which encloses the conditions and
 * builds the margins as before: the two enclosures are rigorous, and neither is assumed to
 * contain the other. Nothing here is persisted, enters an identity or outlives its certificate.
 */
final class ConstructiveIdentityRecheckMemory {

	private final Map<Integer, Margin> margins = new HashMap<>();
	private final Map<CertificatePolynomial, Chain> chains = new IdentityHashMap<>();
	private boolean disabled;
	private int substitutions;

	/** The prepared parts of the margin of one checked output; all null when unprepared. */
	private static final class Margin {
		private final CertificateElement constant;
		private final CertificateElement alongX;
		private final CertificateElement alongY;

		Margin(CertificateElement constant, CertificateElement alongX,
				CertificateElement alongY) {
			this.constant = constant;
			this.alongX = alongX;
			this.alongY = alongY;
		}
	}

	/** The substitution steps of one polynomial, in index order, each with its value. */
	private static final class Chain {
		private final CertificatePolynomial base;
		private final List<Integer> indices = new ArrayList<>();
		private final List<ExactRational2D> values = new ArrayList<>();
		private final List<CertificatePolynomial> results = new ArrayList<>();

		Chain(CertificatePolynomial polynomial) {
			base = polynomial.memoizing();
		}
	}

	/**
	 * The enclosure on the box of one condition of the cell, specialized at the point values of
	 * the box.
	 *
	 * @param certificate the certificate that owns this memory
	 * @param condition the index of the condition in the cell
	 * @param box the box of the revision
	 * @param counters the counters of this accelerated recheck
	 * @return the enclosure, or {@code null} when it cannot be built or certified finite
	 */
	OutwardInterval condition(ConstructiveIdentityCertificate certificate, int condition,
			OutwardInterval[] box, ConstructiveIdentityCounters counters) {
		if (isDisabled()) {
			return null;
		}
		try {
			ExactRational2D[] values = pointValues(box);
			CertificateElement element = specialize(certificate.getCell().get(condition)
					.getElement(), values, counters);
			return ConstructiveIdentityArithmetic.enclose(element, specializedRadicand(
					certificate, values, counters), box, counters);
		} catch (ConstructiveIdentityException | ConstructiveIdentityBudgetExhaustedException
				| ArithmeticException | IllegalArgumentException failed) {
			return null;
		}
	}

	/**
	 * The enclosure on the box of the margin of one checked output at its live point, by the
	 * prepared identity, specialized at the point values of the box.
	 *
	 * @param certificate the certificate that owns this memory
	 * @param correspondence the index of the checked output
	 * @param live the exact live point
	 * @param box the box of the revision
	 * @param counters the counters of this accelerated recheck, which also count the preparation
	 * @return the enclosure, or {@code null} when it cannot be prepared, built or certified
	 *         finite
	 */
	OutwardInterval margin(ConstructiveIdentityCertificate certificate, int correspondence,
			ExactRational2D[] live, OutwardInterval[] box, ConstructiveIdentityCounters counters) {
		if (isDisabled()) {
			return null;
		}
		try {
			Margin prepared = prepared(certificate, correspondence, counters);
			if (prepared.constant == null) {
				return null;
			}
			ExactRational2D[] values = pointValues(box);
			ConstructiveIdentityArithmetic field = certificate.getArithmetic()
					.forRecheck(counters);
			// The live coordinates are rational, so no product below involves the radicand.
			CertificateElement value = field.add(specialize(prepared.constant, values, counters),
					field.add(field.multiply(field.ofRational(live[0]),
							specialize(prepared.alongX, values, counters)),
							field.multiply(field.ofRational(live[1]),
									specialize(prepared.alongY, values, counters))));
			return ConstructiveIdentityArithmetic.enclose(value, specializedRadicand(
					certificate, values, counters), box, counters);
		} catch (ConstructiveIdentityException | ConstructiveIdentityBudgetExhaustedException
				| ArithmeticException | IllegalArgumentException failed) {
			return null;
		}
	}

	/**
	 * The margin of one checked output at a live point by the prepared identity, without
	 * specialization: for the tests.
	 *
	 * @return the margin, or {@code null} when it cannot be prepared
	 */
	CertificateElement preparedMargin(ConstructiveIdentityCertificate certificate,
			int correspondence, ExactRational2D[] live, ConstructiveIdentityArithmetic field) {
		Margin prepared = prepared(certificate, correspondence, field.getCounters());
		if (prepared.constant == null) {
			return null;
		}
		return field.add(prepared.constant, field.add(field.multiply(field.ofRational(live[0]),
				prepared.alongX), field.multiply(field.ofRational(live[1]), prepared.alongY)));
	}

	/** Makes every enclosure of this memory unavailable, as a failure would: for the tests. */
	synchronized void disable() {
		disabled = true;
	}

	private synchronized boolean isDisabled() {
		return disabled;
	}

	/** @return the number of substitution steps computed so far: for the tests */
	synchronized int getSubstitutions() {
		return substitutions;
	}

	/**
	 * The polynomial with the point values substituted, as the enclosures of this memory use it:
	 * for the tests.
	 *
	 * @param polynomial a polynomial of the certificate
	 * @param values the exact value of every point indeterminate, {@code null} for the others
	 * @param counters the counters that record the substituted polynomials
	 * @return the specialized polynomial
	 */
	CertificatePolynomial specialized(CertificatePolynomial polynomial, ExactRational2D[] values,
			ConstructiveIdentityCounters counters) {
		return specialize(polynomial, values, counters);
	}

	/**
	 * @param counters the counters of the recheck that asks for it, which count the preparation
	 *        (author decision D1-A)
	 * @return the prepared margin, built once, or an unprepared one when that fails; a
	 *         preparation that the budget of the recheck interrupts is not kept, so that a later
	 *         recheck prepares it again on its own budget
	 * @throws ConstructiveIdentityBudgetExhaustedException when the budget of the recheck is
	 *         exhausted
	 */
	private synchronized Margin prepared(ConstructiveIdentityCertificate certificate,
			int correspondence, ConstructiveIdentityCounters counters) {
		Margin known = margins.get(correspondence);
		if (known != null) {
			return known;
		}
		Margin built;
		try {
			ConstructiveIdentityCertificate.Correspondence checked = certificate
					.getCorrespondences().get(correspondence);
			ConstructiveIdentityArithmetic field = certificate.getArithmetic().forRecheck(
					counters);
			ConstructiveConeEvaluator.Point<CertificateElement> other = checked.getOther();
			ConstructiveConeEvaluator.Point<CertificateElement> selected = checked.getSelected();
			CertificateElement minusTwo = field.ofRational(ExactRational2D.of(-2));
			built = new Margin(field.subtract(norm(field, other), norm(field, selected)),
					field.multiply(minusTwo, field.subtract(other.getX(), selected.getX())),
					field.multiply(minusTwo, field.subtract(other.getY(), selected.getY())));
		} catch (ConstructiveIdentityBudgetExhaustedException exhausted) {
			throw exhausted;
		} catch (ConstructiveIdentityException | ArithmeticException
				| IllegalArgumentException failed) {
			built = new Margin(null, null, null);
		}
		margins.put(correspondence, built);
		return built;
	}

	private static CertificateElement norm(ConstructiveIdentityArithmetic field,
			ConstructiveConeEvaluator.Point<CertificateElement> point) {
		return field.add(field.multiply(point.getX(), point.getX()),
				field.multiply(point.getY(), point.getY()));
	}

	private CertificatePolynomial specializedRadicand(ConstructiveIdentityCertificate certificate,
			ExactRational2D[] values, ConstructiveIdentityCounters counters) {
		CertificatePolynomial radicand = certificate.getArithmetic().getRadicand();
		return radicand == null ? null : specialize(radicand, values, counters);
	}

	/**
	 * @return the exact value of every indeterminate whose interval is a single point, and
	 *         {@code null} for the others
	 */
	static ExactRational2D[] pointValues(OutwardInterval[] box) {
		ExactRational2D[] values = new ExactRational2D[box.length];
		for (int index = 0; index < box.length; index++) {
			if (box[index] != null && box[index].isFinite()
					&& box[index].getLower() == box[index].getUpper()) {
				values[index] = ExactRational2D.ofBinary64(box[index].getLower());
			}
		}
		return values;
	}

	private CertificateElement specialize(CertificateElement element, ExactRational2D[] values,
			ConstructiveIdentityCounters counters) {
		return new CertificateElement(specialize(element.getRational(), values, counters),
				specialize(element.getIrrational(), values, counters));
	}

	private CertificateRationalFunction specialize(CertificateRationalFunction function,
			ExactRational2D[] values, ConstructiveIdentityCounters counters) {
		return CertificateRationalFunction.specialized(
				specialize(function.getNumerator(), values, counters),
				specialize(function.getDenominator(), values, counters));
	}

	/**
	 * The polynomial with the point values substituted, one indeterminate at a time in index
	 * order; the steps whose indeterminates and values are unchanged are reused.
	 */
	private synchronized CertificatePolynomial specialize(CertificatePolynomial polynomial,
			ExactRational2D[] values, ConstructiveIdentityCounters counters) {
		if (polynomial.isConstant()) {
			return polynomial;
		}
		Chain chain = chains.computeIfAbsent(polynomial, ignored -> new Chain(polynomial));
		int[] occurring = polynomial.maximumExponents();
		CertificatePolynomial current = chain.base;
		int step = 0;
		for (int index = 0; index < values.length; index++) {
			if (values[index] == null || occurring[index] == 0) {
				continue;
			}
			if (step < chain.indices.size() && chain.indices.get(step) == index
					&& chain.values.get(step).compareTo(values[index]) == 0) {
				current = chain.results.get(step);
			} else {
				while (chain.indices.size() > step) {
					int last = chain.indices.size() - 1;
					chain.indices.remove(last);
					chain.values.remove(last);
					chain.results.remove(last);
				}
				current = current.specialize(index, values[index], counters);
				substitutions++;
				chain.indices.add(index);
				chain.values.add(values[index]);
				chain.results.add(current);
			}
			step++;
		}
		while (chain.indices.size() > step) {
			int last = chain.indices.size() - 1;
			chain.indices.remove(last);
			chain.values.remove(last);
			chain.results.remove(last);
		}
		return current;
	}
}
