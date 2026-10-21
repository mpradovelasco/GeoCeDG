/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.geocedg.common.kernel.locus.exact.RealRootStructure1D.RootKind;
import org.geocedg.common.kernel.locus.exact.SegmentDriverRootStructure2D.DomainEnd;
import org.geocedg.common.kernel.locus.exact.SegmentDriverRootStructure2D.Root;
import org.geocedg.common.kernel.locus.exact.SegmentDriverRootStructure2D.Status;
import org.junit.jupiter.api.Test;

/** Exact classification of first-level segment-driver predicates over {@code [0, 1]}. */
final class SegmentDriverRootStructure2DTest {
	private static final ExactRational2D WIDTH = ExactRational2D.of(BigInteger.ONE,
			BigInteger.ONE.shiftLeft(40));

	@Test
	void rootsAtBothEndsAreExactAndMarked() {
		ExactPolynomial1D p = poly(0, 1).multiply(poly(-1, 1))
				.multiply(ExactPolynomial1D.of(ExactRational2D.of(-1, 2), ExactRational2D.ONE));

		SegmentDriverRootStructure2D structure = classify(p);

		assertEquals(Status.CLASSIFIED, structure.status());
		assertEquals(3, structure.roots().size());
		Root lower = structure.roots().get(0);
		assertEquals(DomainEnd.LOWER, lower.end());
		assertTrue(lower.isDomainEnd());
		assertTrue(lower.isExact());
		assertEquals(0.0, lower.tLower());
		assertEquals(0.0, lower.tUpper());
		assertEquals(0, lower.signLeft());
		assertEquals(1, lower.innerSign());
		assertEquals(RootKind.TRANSITION, lower.kind());
		Root middle = structure.roots().get(1);
		assertEquals(DomainEnd.NONE, middle.end());
		assertFalse(middle.isDomainEnd());
		assertEquals(0.5, middle.tLower());
		assertEquals(0.5, middle.tUpper());
		assertThrows(IllegalStateException.class, middle::innerSign);
		Root upper = structure.roots().get(2);
		assertEquals(DomainEnd.UPPER, upper.end());
		assertEquals(1.0, upper.tUpper());
		assertEquals(-1, upper.innerSign());
		assertEquals(0, upper.signRight());
		assertEquals(Arrays.asList(0, 1, -1, 0), structure.gapSigns());
	}

	@Test
	void evenEndRootsAreClassifiedFromTheInnerSide() {
		SegmentDriverRootStructure2D isolated = classify(poly(0, 0, -1, -1));
		SegmentDriverRootStructure2D touching = classify(poly(1, -2, 1));

		Root atZero = isolated.roots().get(0);
		assertEquals(1, isolated.roots().size());
		assertEquals(DomainEnd.LOWER, atZero.end());
		assertEquals(2, atZero.multiplicity());
		assertEquals(-1, atZero.innerSign());
		assertEquals(RootKind.ISOLATED_POINT, atZero.kind());
		assertEquals(Arrays.asList(0, -1), isolated.gapSigns());
		Root atOne = touching.roots().get(0);
		assertEquals(1, touching.roots().size());
		assertEquals(DomainEnd.UPPER, atOne.end());
		assertEquals(2, atOne.multiplicity());
		assertEquals(1, atOne.innerSign());
		assertEquals(RootKind.SINGULAR_INTERIOR, atOne.kind());
		assertEquals(Arrays.asList(1, 0), touching.gapSigns());
	}

	@Test
	void interiorDoubleRootIsEnclosed() {
		ExactRational2D third = ExactRational2D.of(1, 3);
		ExactPolynomial1D p = ExactPolynomial1D.of(third.negate(), ExactRational2D.ONE).pow(2);

		SegmentDriverRootStructure2D structure = classify(p);

		assertEquals(1, structure.roots().size());
		Root root = structure.roots().get(0);
		assertFalse(root.isExact());
		assertTrue(root.lower().compareTo(third) < 0 && third.compareTo(root.upper()) < 0);
		assertTrue(root.upper().subtract(root.lower()).compareTo(WIDTH) <= 0);
		assertTrue(ExactRational2D.ofBinary64(root.tLower()).compareTo(root.lower()) <= 0);
		assertTrue(ExactRational2D.ofBinary64(root.tUpper()).compareTo(root.upper()) >= 0);
		assertEquals(2, root.multiplicity());
		assertEquals(RootKind.SINGULAR_INTERIOR, root.kind());
		assertEquals(Arrays.asList(1, 1), structure.gapSigns());
		assertTrue(structure.bisections() > 0);
	}

	@Test
	void segmentsWithoutRootsAndIdenticallyZeroPredicates() {
		SegmentDriverRootStructure2D negative = classify(poly(-2, 0, 1));
		SegmentDriverRootStructure2D zero = classify(ExactPolynomial1D.ZERO);

		assertTrue(negative.roots().isEmpty());
		assertEquals(Arrays.asList(-1), negative.gapSigns());
		assertEquals(Arrays.asList(1), classify(poly(1, 1)).gapSigns());
		assertEquals(Status.IDENTICALLY_ZERO, zero.status());
		assertTrue(zero.isIdenticallyZero());
		assertTrue(zero.roots().isEmpty());
		assertEquals(Arrays.asList(0), zero.gapSigns());
		assertNull(zero.rootStructure());
		assertEquals(0, zero.bisections());
		assertThrows(IllegalArgumentException.class, () -> SegmentDriverRootStructure2D
				.classify(poly(1, 1), ExactRational2D.ZERO, ExactBudget1D.DEFAULT));
	}

	@Test
	void classificationsAreDeterministic() {
		List<ExactPolynomial1D> predicates = Arrays.asList(poly(0, 0, -1, -1), poly(1, -2, 1),
				poly(-2, 0, 9), poly(0, 1).multiply(poly(-1, 1)));
		for (ExactPolynomial1D predicate : predicates) {
			SegmentDriverRootStructure2D first = classify(predicate);
			SegmentDriverRootStructure2D second = classify(predicate);
			assertEquals(first, second);
			assertEquals(first.hashCode(), second.hashCode());
			assertEquals(first.toString(), second.toString());
		}
	}

	private static SegmentDriverRootStructure2D classify(ExactPolynomial1D predicate) {
		return SegmentDriverRootStructure2D.classify(predicate, WIDTH, ExactBudget1D.DEFAULT);
	}

	private static ExactPolynomial1D poly(long... coefficients) {
		ExactRational2D[] values = new ExactRational2D[coefficients.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = ExactRational2D.of(coefficients[i]);
		}
		return ExactPolynomial1D.of(values);
	}
}
