/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityBudgetExhaustedException.Limit;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;
import org.junit.jupiter.api.Test;

/**
 * The exact algebra of {@code constructive-identity-certificate/v1}
 * ({@code locus-v2-constructive-authority.md} sections 8 and 10): monomials of 16 indeterminates
 * without collision or overflow, an exponent beyond its range failing closed, the budget
 * {@code constructive-identity-certificate-budget/v1} and its injections, exact division and
 * square roots, the second-order mean-value enclosure against cancellation, and the field with one
 * radicand (gate G-AUT-8 for the certificate, row V-AUT-CIC-6).
 */
final class ConstructiveIdentityAlgebraTest {

	private final ConstructiveIdentityCounters counters =
			new ConstructiveIdentityCounters(ConstructiveIdentityBudget.V1);

	@Test
	void sixteenIndeterminatesHaveDistinctMonomialsInGradedOrder() {
		Set<CertificatePolynomial.Monomial> monomials = new HashSet<>();
		for (int index = 0; index < CertificatePolynomial.INDETERMINATES; index++) {
			CertificatePolynomial.Monomial monomial = CertificatePolynomial.Monomial.of(index);
			assertEquals(1, monomial.getDegree());
			assertEquals(1, monomial.exponent(index));
			for (int other = 0; other < CertificatePolynomial.INDETERMINATES; other++) {
				if (other != index) {
					assertEquals(0, monomial.exponent(other));
				}
			}
			assertTrue(monomials.add(monomial), "no collision");
		}
		CertificatePolynomial.Monomial first = CertificatePolynomial.Monomial.of(0);
		CertificatePolynomial.Monomial last = CertificatePolynomial.Monomial.of(15);
		CertificatePolynomial.Monomial square = last.times(last, counters);
		assertTrue(square.compareTo(first) < 0, "a higher degree comes first");
		assertTrue(first.compareTo(last) < 0, "the first indeterminate leads at equal degree");
		assertEquals(2, square.exponent(15));
		assertThrows(IllegalArgumentException.class,
				() -> CertificatePolynomial.Monomial.of(16));
	}

	@Test
	void anExponentBeyondItsRangeFailsClosedAndIsNeverWrapped() {
		for (int index : new int[] {0, 7, 8, 15}) {
			CertificatePolynomial power = CertificatePolynomial.indeterminate(index);
			CertificatePolynomial.Monomial top = CertificatePolynomial.Monomial.of(index);
			for (int exponent = 1; exponent < CertificatePolynomial.MAXIMUM_EXPONENT;
					exponent++) {
				top = top.times(CertificatePolynomial.Monomial.of(index), counters);
			}
			assertEquals(255, top.exponent(index), "255 is representable");
			CertificatePolynomial.Monomial limit = top;
			ConstructiveIdentityBudgetExhaustedException overflow = assertThrows(
					ConstructiveIdentityBudgetExhaustedException.class,
					() -> limit.times(CertificatePolynomial.Monomial.of(index), counters));
			assertEquals(Limit.EXPONENT_RANGE, overflow.getLimit());
			assertTrue(overflow.getMessage().startsWith("EXPONENT_RANGE_EXCEEDED"));
			// A polynomial product checks the exponents before multiplying any term.
			CertificatePolynomial high = power;
			for (int step = 0; step < 7; step++) {
				high = high.multiply(high, counters);
			}
			assertEquals(128, high.leadingMonomial().exponent(index));
			CertificatePolynomial product = high;
			assertEquals(Limit.EXPONENT_RANGE, assertThrows(
					ConstructiveIdentityBudgetExhaustedException.class,
					() -> product.multiply(product, counters)).getLimit());
		}
	}

	@Test
	void theArithmeticOfPolynomialsIsExact() {
		CertificatePolynomial x = CertificatePolynomial.indeterminate(0);
		CertificatePolynomial y = CertificatePolynomial.indeterminate(9);
		CertificatePolynomial sum = x.add(y, counters);
		CertificatePolynomial square = sum.multiply(sum, counters);
		CertificatePolynomial expanded = x.multiply(x, counters)
				.add(x.multiply(y, counters).scale(ExactRational2D.of(2), counters), counters)
				.add(y.multiply(y, counters), counters);

		assertTrue(square.sameAs(expanded));
		assertTrue(square.subtract(expanded, counters).isZero());
		assertEquals(3, square.size());
		CertificatePolynomial difference = x.multiply(x, counters)
				.subtract(y.multiply(y, counters), counters);
		CertificatePolynomial quotient = difference.divideExactly(x.subtract(y, counters), 24,
				6000, counters);
		assertNotNull(quotient);
		assertTrue(quotient.sameAs(sum), "(x^2 - y^2) / (x - y) = x + y");
		assertNull(difference.divideExactly(x.add(CertificatePolynomial.constant(
				ExactRational2D.of(1)), counters), 24, 6000, counters), "no exact quotient");
		CertificatePolynomial shifted = sum.subtract(CertificatePolynomial.constant(
				ExactRational2D.of(3)), counters);
		CertificatePolynomial root = shifted.multiply(shifted, counters).squareRoot(counters);
		assertNotNull(root);
		assertTrue(root.sameAs(shifted) || root.sameAs(shifted.negate()));
		assertNull(square.add(CertificatePolynomial.constant(ExactRational2D.of(1)), counters)
				.squareRoot(counters), "not a square");
		assertNull(square.negate().squareRoot(counters), "a negative leading coefficient");
	}

	@Test
	void everyLimitOfTheBudgetFailsClosedWithItsCounters() {
		CertificatePolynomial x = CertificatePolynomial.indeterminate(0);
		CertificatePolynomial y = CertificatePolynomial.indeterminate(1);
		CertificatePolynomial sum = x.add(y, new ConstructiveIdentityCounters(
				ConstructiveIdentityBudget.V1));

		ConstructiveIdentityCounters products = new ConstructiveIdentityCounters(
				ConstructiveIdentityBudget.restrictedTo(3, 16, 200_000, 4096));
		assertEquals(Limit.TERM_PRODUCTS, assertThrows(
				ConstructiveIdentityBudgetExhaustedException.class,
				() -> sum.multiply(sum, products)).getLimit());
		ConstructiveIdentityCounters terms = new ConstructiveIdentityCounters(
				ConstructiveIdentityBudget.restrictedTo(1000, 16, 2, 4096));
		assertEquals(Limit.TERMS, assertThrows(
				ConstructiveIdentityBudgetExhaustedException.class,
				() -> sum.multiply(sum, terms)).getLimit());
		ConstructiveIdentityCounters bits = new ConstructiveIdentityCounters(
				ConstructiveIdentityBudget.restrictedTo(1000, 16, 200_000, 8));
		CertificatePolynomial wide = CertificatePolynomial.constant(ExactRational2D.of(1000));
		assertEquals(Limit.BITS, assertThrows(
				ConstructiveIdentityBudgetExhaustedException.class,
				() -> wide.multiply(wide, bits)).getLimit());
		ConstructiveIdentityCounters indeterminates = new ConstructiveIdentityCounters(
				ConstructiveIdentityBudget.V1);
		for (int index = 0; index < ConstructiveIdentityBudget.MAXIMUM_INDETERMINATES; index++) {
			assertEquals(index, indeterminates.allocateIndeterminate());
		}
		ConstructiveIdentityBudgetExhaustedException seventeenth = assertThrows(
				ConstructiveIdentityBudgetExhaustedException.class,
				indeterminates::allocateIndeterminate);
		assertEquals(Limit.INDETERMINATES, seventeenth.getLimit());
		assertEquals(16, seventeenth.getCounters().getIndeterminates());
		assertThrows(IllegalArgumentException.class, () -> ConstructiveIdentityBudget
				.restrictedTo(ConstructiveIdentityBudget.MAXIMUM_TERM_PRODUCTS + 1, 16, 1, 1),
				"a budget can only be restricted");
		assertTrue(ConstructiveIdentityBudget.V1.isFixed());
	}

	@Test
	void theMeanValueFormKeepsTheSignThatCancellationLoses() {
		// p = (x - 1)^2 + 2^-90 on the binary64 box around 1: the naive enclosure of the
		// expanded polynomial is about 2^-50 wide and contains zero; the exact value at the
		// centre and the small gradient certify p > 0.
		CertificatePolynomial x = CertificatePolynomial.indeterminate(0);
		ExactRational2D epsilon = ExactRational2D.of(BigInteger.ONE, BigInteger.TWO.pow(90));
		CertificatePolynomial p = x.multiply(x, counters)
				.subtract(x.scale(ExactRational2D.of(2), counters), counters)
				.add(CertificatePolynomial.constant(ExactRational2D.of(1).add(epsilon)), counters);
		OutwardInterval[] box = new OutwardInterval[CertificatePolynomial.INDETERMINATES];
		for (int index = 0; index < box.length; index++) {
			box[index] = OutwardInterval.point(0);
		}
		box[0] = OutwardInterval.of(Math.nextDown(1.0), Math.nextUp(1.0));

		OutwardInterval meanValue = p.enclose(box, counters);
		OutwardInterval naive = naive(p, box);

		assertEquals(1, meanValue.certifiedSign(), String.valueOf(meanValue));
		assertNotEquals(1, naive.certifiedSign(), "naive: " + naive);
	}

	@Test
	void theFieldHasOneRadicandAndRefusesAnIndependentSecondRoot() {
		ConstructiveIdentityArithmetic field = new ConstructiveIdentityArithmetic(
				new ConstructiveIdentityCounters(ConstructiveIdentityBudget.V1));
		CertificateElement x = field.indeterminate("x", OutwardInterval.point(2));
		final CertificateElement y = field.indeterminate("y", OutwardInterval.point(3));
		CertificateElement one = field.ofBinary64(1);
		CertificateElement shifted = field.add(x, one);

		CertificateElement rational = field.sqrt(field.multiply(shifted, shifted));
		assertTrue(rational.isRational(), "a perfect square has a root in the base field");
		assertTrue(field.subtract(rational, shifted).isZero(), "the positive root on the box");
		assertNull(field.getRadicand());
		CertificateElement root = field.sqrt(x);
		assertNotNull(field.getRadicand(), "the radicand of the extension");
		assertTrue(field.subtract(field.multiply(root, root), x).isZero(), "sqrt(x)^2 = x");
		CertificateElement four = field.ofBinary64(4);
		CertificateElement joint = field.sqrt(field.multiply(four, x));
		assertTrue(field.subtract(joint, field.add(root, root)).isZero(),
				"sqrt(4x) = 2 sqrt(x) in the same extension");
		CertificateElement inverse = field.divide(one, field.add(one, root));
		assertTrue(field.subtract(field.multiply(inverse, field.add(one, root)), one).isZero(),
				"division through the norm");
		ConstructiveIdentityException second = assertThrows(ConstructiveIdentityException.class,
				() -> field.sqrt(y));
		assertEquals(ConstructiveIdentityCertificate.Cause.OUT_OF_CLASS, second.getTypedCause(),
				"V-AUT-CIC-6: a second independent square root is outside the class");
		assertEquals(ConstructiveIdentityCertificate.Cause.OUT_OF_CLASS,
				assertThrows(ConstructiveIdentityException.class,
						() -> field.sqrt(field.add(one, root))).getTypedCause(),
				"a nested square root");
	}

	@Test
	void signsAreCertifiedOnTheBoxAndRecordedInTheCell() {
		ConstructiveIdentityArithmetic field = new ConstructiveIdentityArithmetic(
				new ConstructiveIdentityCounters(ConstructiveIdentityBudget.V1));
		CertificateElement x = field.indeterminate("x", OutwardInterval.of(0.5, 1.5));
		CertificateElement one = field.ofBinary64(1);

		assertEquals(1, field.sign(field.add(x, one)));
		assertEquals(ConstructiveArithmetic.UNDECIDED, field.sign(field.subtract(x, one)),
				"x - 1 changes sign on the box");
		assertEquals(0, field.sign(field.subtract(x, x)), "the zero element");
		assertEquals(-1, field.signOfLiveCheck(field.subtract(x, field.ofBinary64(2))));
		assertEquals(1, field.getCell().size(), "only certified signs of the evaluation");
		assertEquals(1, field.getLiveConditions().size(), "live checks apart");
	}

	@Test
	void aFiniteAgreementIsNotTheZeroElement() {
		List<ExactRational2D> base = List.of(ExactRational2D.ofBinary64(0.7),
				ExactRational2D.ofBinary64(-1.3));
		List<List<ExactRational2D>> points = new ArrayList<>();
		points.add(base);
		points.addAll(SubstituteGenericPerturbationSet.of(base).getPoints());
		ExactRational2D[] coefficients = SubstituteGenericEvidenceTest.cubicThrough(points);
		ConstructiveIdentityArithmetic field = new ConstructiveIdentityArithmetic(
				new ConstructiveIdentityCounters(ConstructiveIdentityBudget.V1));
		CertificateElement x = field.indeterminate("w1", OutwardInterval.point(0.7));
		CertificateElement y = field.indeterminate("w2", OutwardInterval.point(-1.3));
		CertificateElement[] monomials = {field.ofBinary64(1), x, y, field.multiply(x, x),
				field.multiply(x, y), field.multiply(y, y), field.multiply(field.multiply(x, x), x),
				field.multiply(field.multiply(x, x), y), field.multiply(field.multiply(x, y), y),
				field.multiply(field.multiply(y, y), y)};
		CertificateElement residual = field.ofBinary64(0);
		for (int index = 0; index < monomials.length; index++) {
			residual = field.add(residual, field.multiply(field.ofRational(coefficients[index]),
					monomials[index]));
		}

		assertFalse(residual.isZero(), "V-AUT-RLX-2 in E2: the cubic that SUBSTITUTE_GENERIC"
				+ " admits on its nine points is no identity, so the certificate gives"
				+ " NOT_AN_IDENTITY and withdraws the relaxed evidence");
		for (List<ExactRational2D> point : points) {
			ExactRational2D[] values = new ExactRational2D[CertificatePolynomial.INDETERMINATES];
			values[0] = point.get(0);
			values[1] = point.get(1);
			assertTrue(residual.getRational().getNumerator().valueAt(values).isZero(),
					"it vanishes exactly at every point of the set");
		}
	}

	private static OutwardInterval naive(CertificatePolynomial p, OutwardInterval[] box) {
		OutwardInterval sum = OutwardInterval.point(0);
		for (java.util.Map.Entry<CertificatePolynomial.Monomial, ExactRational2D> term
				: p.getTerms().entrySet()) {
			OutwardInterval value = OutwardInterval.enclosing(term.getValue());
			for (int power = 0; power < term.getKey().exponent(0); power++) {
				value = value.multiply(box[0]);
			}
			sum = sum.add(value);
		}
		return sum;
	}
}
