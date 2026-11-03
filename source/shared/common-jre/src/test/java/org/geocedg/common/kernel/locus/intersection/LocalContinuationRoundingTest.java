/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/** Exact adversaries for the pointwise Math accuracy premise, without mocking Math. */
class LocalContinuationRoundingTest {

	@Test
	void pointwiseAccuracyEnvelopeCoversPositiveAndNegativeBinadeBoundaries() {
		List<Executable> cases = new ArrayList<>();
		for (int exponent : new int[] {-1000, -500, -53, -10, -1, 0, 1}) {
			double boundary = Math.scalb(1.0, exponent);
			BigDecimal exactPositiveTarget = new BigDecimal(boundary)
					.add(new BigDecimal(Math.ulp(boundary)).divide(BigDecimal.valueOf(8)));
			double positiveReturn = Math.nextDown(Math.nextDown(Math.nextDown(boundary)));
			for (int sign : new int[] {1, -1}) {
				BigDecimal exactTarget = sign > 0 ? exactPositiveTarget : exactPositiveTarget.negate();
				double pointwiseAllowedReturn = sign * positiveReturn;
				cases.add(() -> {
					SplineOutwardInterval2D enclosure = LocalContinuationEvidence2D
							.atan2AccuracyEnvelope(pointwiseAllowedReturn);
					assertTrue(new BigDecimal(enclosure.lower).compareTo(exactTarget) <= 0
							&& new BigDecimal(enclosure.upper).compareTo(exactTarget) >= 0,
							"Pointwise 1.625-ulp error at binade 2^" + exponent + ", sign=" + sign);
				});
			}
		}
		assertAll("The two-exact-result-ulp premise must cover both binade directions", cases);
	}

	@Test
	void pointwiseAccuracyEnvelopeCoversProvedBinaryAtan2InputBracket() {
		// For exact binary x=1, y below, prove the true angle is strictly between
		// 1 and 1+2^-54. This is not an assertion that a JDK returns the synthetic r,
		// nor that r satisfies every global constraint on a whole Math implementation.
		Rational ordinate = Rational.fromDouble(0x1.8eb245cbee3a6p0);
		Rational lowerAngle = Rational.of(1);
		Rational upperAngle = lowerAngle.add(new Rational(BigInteger.ONE,
				BigInteger.ONE.shiftLeft(54)));
		Rational[] tangentAtLower = tangentBounds(lowerAngle);
		Rational[] tangentAtUpper = tangentBounds(upperAngle);
		assertTrue(tangentAtLower[1].compareTo(ordinate) < 0);
		assertTrue(ordinate.compareTo(tangentAtUpper[0]) < 0);

		double pointwiseAllowedReturn = 0x1.ffffffffffffdp-1;
		Rational exactReturned = Rational.fromDouble(pointwiseAllowedReturn);
		Rational exactUlp = Rational.fromDouble(Math.ulp(1.0));
		assertTrue(lowerAngle.subtract(exactReturned).compareTo(exactUlp) > 0);
		assertTrue(upperAngle.subtract(exactReturned).compareTo(exactUlp.multiply(Rational.of(2))) < 0);

		SplineOutwardInterval2D enclosure = LocalContinuationEvidence2D
				.atan2AccuracyEnvelope(pointwiseAllowedReturn);
		assertTrue(Rational.fromDouble(enclosure.lower).compareTo(lowerAngle) <= 0);
		assertTrue(Rational.fromDouble(enclosure.upper).compareTo(upperAngle) >= 0,
				"The enclosure must cover the exactly proved real-angle bracket above one");
	}

	private static Rational[] tangentBounds(Rational argument) {
		Rational[] sine = trigBounds(argument, true);
		Rational[] cosine = trigBounds(argument, false);
		assertTrue(cosine[0].compareTo(Rational.of(0)) > 0);
		return new Rational[] {sine[0].divide(cosine[1]), sine[1].divide(cosine[0])};
	}

	private static Rational[] trigBounds(Rational argument, boolean sine) {
		assertTrue(argument.compareTo(Rational.of(0)) > 0
				&& argument.compareTo(Rational.of(11).divide(Rational.of(10))) < 0);
		// Here the alternating Taylor terms decrease. k=16 is an exact upper
		// partial sum and k=17 an exact lower sum; no rounded transcendentals occur.
		Rational sum = Rational.of(0);
		Rational upper = null;
		for (int k = 0; k < 18; k++) {
			int exponent = 2 * k + (sine ? 1 : 0);
			BigInteger factorial = BigInteger.ONE;
			for (int factor = 2; factor <= exponent; factor++) {
				factorial = factorial.multiply(BigInteger.valueOf(factor));
			}
			Rational term = argument.pow(exponent).divide(new Rational(factorial, BigInteger.ONE));
			sum = sum.add(k % 2 == 0 ? term : term.negate());
			if (k == 16) {
				upper = sum;
			}
		}
		return new Rational[] {sum, upper};
	}

	private record Rational(BigInteger numerator, BigInteger denominator)
			implements Comparable<Rational> {
		private Rational {
			if (denominator.signum() == 0) {
				throw new ArithmeticException("Zero denominator");
			}
			if (denominator.signum() < 0) {
				numerator = numerator.negate();
				denominator = denominator.negate();
			}
			BigInteger divisor = numerator.gcd(denominator);
			numerator = numerator.divide(divisor);
			denominator = denominator.divide(divisor);
		}

		static Rational of(long value) {
			return new Rational(BigInteger.valueOf(value), BigInteger.ONE);
		}

		static Rational fromDouble(double value) {
			BigDecimal exact = new BigDecimal(value);
			return exact.scale() >= 0
					? new Rational(exact.unscaledValue(), BigInteger.TEN.pow(exact.scale()))
					: new Rational(exact.unscaledValue().multiply(BigInteger.TEN.pow(-exact.scale())),
							BigInteger.ONE);
		}

		Rational add(Rational other) {
			return new Rational(numerator.multiply(other.denominator)
					.add(other.numerator.multiply(denominator)), denominator.multiply(other.denominator));
		}

		Rational subtract(Rational other) { return add(other.negate()); }
		Rational negate() { return new Rational(numerator.negate(), denominator); }
		Rational multiply(Rational other) {
			return new Rational(numerator.multiply(other.numerator), denominator.multiply(other.denominator));
		}
		Rational divide(Rational other) {
			return new Rational(numerator.multiply(other.denominator), denominator.multiply(other.numerator));
		}
		Rational pow(int exponent) {
			return new Rational(numerator.pow(exponent), denominator.pow(exponent));
		}
		@Override public int compareTo(Rational other) {
			return numerator.multiply(other.denominator).compareTo(other.numerator.multiply(denominator));
		}
	}
}
