/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.units;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.geocedg.common.kernel.units.UnitQuantity;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R6-plus-D1 T-STATE and T-USM-VALIDATION: unit-system v1.0 sections 3, 4.2,
 * 4.4 and 5.
 */
class PreG9BR6PlusD1UnitStateTest {
	private static final UsmDefinition INCH = UsmDefinition.of(0.0254, "inch", "in");

	@Test
	void tokensAreExactAndCaseSensitive() {
		assertSame(UnitToken.MM, UnitToken.fromToken("mm"));
		assertSame(UnitToken.CM, UnitToken.fromToken("cm"));
		assertSame(UnitToken.M, UnitToken.fromToken("m"));
		assertSame(UnitToken.USM, UnitToken.fromToken("usm"));
		for (String other : new String[] {"MM", "Mm", "", " mm", "km", "in", "unspecified",
				null}) {
			assertNull(UnitToken.fromToken(other), String.valueOf(other));
		}
		assertThrows(IllegalStateException.class, UnitToken.USM::builtInMetresPerUnit);
	}

	@Test
	void builtInFactorsAreTheNearestBinary64ToTheSiValues() {
		assertNearest(new BigDecimal("0.001"), UnitToken.MM.builtInMetresPerUnit());
		assertNearest(new BigDecimal("0.01"), UnitToken.CM.builtInMetresPerUnit());
		assertNearest(BigDecimal.ONE, UnitToken.M.builtInMetresPerUnit());
	}

	private static void assertNearest(BigDecimal exact, double value) {
		BigDecimal error = new BigDecimal(value).subtract(exact).abs();
		assertTrue(error.compareTo(new BigDecimal(Math.nextUp(value)).subtract(exact).abs())
				<= 0);
		assertTrue(error.compareTo(new BigDecimal(Math.nextDown(value)).subtract(exact)
				.abs()) <= 0);
	}

	@Test
	void stateConstraintsHoldForEveryInstance() {
		assertSame(UnitState.EMPTY, UnitState.of(null, null, null));
		assertTrue(UnitState.EMPTY.isEmpty());
		assertThrows(IllegalArgumentException.class,
				() -> UnitState.of(null, UnitToken.MM, null));
		assertThrows(IllegalArgumentException.class,
				() -> UnitState.of(UnitToken.USM, null, null));
		assertThrows(IllegalArgumentException.class,
				() -> UnitState.of(UnitToken.MM, UnitToken.USM, null));
		assertThrows(IllegalArgumentException.class,
				() -> UnitState.of(null, UnitToken.USM, INCH));
		UnitState usmOnly = UnitState.of(null, null, INCH);
		assertFalse(usmOnly.isEmpty());
		assertFalse(usmOnly.isPhysical());
	}

	@Test
	void effectiveUnitFunctionCoversEveryCombination() {
		UnitToken[] constructions = {null, UnitToken.MM, UnitToken.CM, UnitToken.M,
				UnitToken.USM};
		UnitToken[] presentations = {null, UnitToken.MM, UnitToken.CM, UnitToken.M,
				UnitToken.USM};
		for (UnitToken c : constructions) {
			for (UnitToken p : presentations) {
				if (c == null && p != null) {
					continue;
				}
				UnitState state = UnitState.of(c, p, INCH);
				assertEquals(c, state.effectiveConstructionUnit());
				UnitToken expected = c == null ? null : p == null ? c : p;
				assertEquals(expected, state.effectivePresentationUnit(), c + "/" + p);
			}
		}
		assertNull(UnitState.EMPTY.effectiveConstructionUnit());
		assertNull(UnitState.EMPTY.effectivePresentationUnit());
		assertTrue(Double.isNaN(UnitState.EMPTY.effectiveConstructionMetresPerUnit()));
		assertEquals(0.0254, UnitState.of(UnitToken.USM, null, INCH)
				.effectiveConstructionMetresPerUnit());
		assertEquals("in", UnitState.of(UnitToken.USM, null, INCH).symbolOf(UnitToken.USM));
		assertEquals("usm", UnitState.of(UnitToken.USM, null,
				UsmDefinition.of(2, null, null)).symbolOf(UnitToken.USM));
	}

	@Test
	void conversionsAndTheirFailureStates() {
		UnitState cm = UnitState.of(UnitToken.CM, null, null);
		assertEquals(0.1, cm.physical(10).getValue(), 1e-15);
		assertEquals(10, cm.display(10).getValue(), 0);
		assertEquals(100, UnitState.of(UnitToken.CM, UnitToken.MM, null).display(10)
				.getValue(), 1e-12);
		assertSame(UnitQuantity.UNSPECIFIED, UnitState.EMPTY.physical(10));
		assertSame(UnitQuantity.UNSPECIFIED, UnitState.EMPTY.display(10));
		assertSame(UnitQuantity.UNSPECIFIED,
				UnitState.of(null, null, INCH).display(10));
		UnitState huge = UnitState.of(UnitToken.USM, UnitToken.MM,
				UsmDefinition.of(Double.MAX_VALUE, null, null));
		assertEquals(UnitQuantity.Status.NOT_FINITE, huge.physical(10).getStatus());
		assertEquals(UnitQuantity.Status.NOT_FINITE, huge.display(10).getStatus());
		assertThrows(IllegalStateException.class, () -> huge.display(10).getValue());
	}

	@Test
	void physicalMeaningComparesBinary64FactorsOnly() {
		UnitState mm = UnitState.of(UnitToken.MM, null, null);
		UnitState usmMillimetre = UnitState.of(UnitToken.USM, null,
				UsmDefinition.of(0.001, "milli", "mmm"));
		assertTrue(UnitState.samePhysicalMeaning(mm, usmMillimetre));
		assertTrue(UnitState.samePhysicalMeaning(mm, UnitState.of(UnitToken.MM,
				UnitToken.CM, INCH)), "presentation never changes physical meaning");
		assertFalse(UnitState.samePhysicalMeaning(mm, UnitState.of(UnitToken.CM, null,
				null)));
		assertFalse(UnitState.samePhysicalMeaning(
				UnitState.of(UnitToken.USM, null, UsmDefinition.of(0.0254, null, null)),
				UnitState.of(UnitToken.USM, null, UsmDefinition.of(0.3048, null, null))));
		assertTrue(UnitState.samePhysicalMeaning(
				UnitState.of(UnitToken.USM, null, UsmDefinition.of(0.0254, "a", null)),
				UnitState.of(UnitToken.USM, null, UsmDefinition.of(0.0254, "b", "c"))));
		assertFalse(UnitState.samePhysicalMeaning(UnitState.EMPTY, UnitState.EMPTY));
		assertFalse(UnitState.samePhysicalMeaning(UnitState.EMPTY, mm));
	}

	@Test
	void documentOperationsFollowSection71() {
		UnitState state = UnitState.of(UnitToken.MM, UnitToken.CM, INCH);
		UnitState unspecified = state.withConstructionUnit(null);
		assertNull(unspecified.getConstructionSelection());
		assertNull(unspecified.getPresentationSelection(), "presentation is removed");
		assertEquals(INCH, unspecified.getUsm(), "the usm definition is kept");
		assertThrows(IllegalArgumentException.class,
				() -> unspecified.withPresentationUnit(UnitToken.MM));
		UnitState usmSelected = state.withConstructionUnit(UnitToken.USM);
		assertThrows(IllegalArgumentException.class, usmSelected::withoutUsm);
		assertNull(state.withoutUsm().getUsm());
		assertEquals(UnitToken.CM, state.withPresentationUnit(UnitToken.CM)
				.getPresentationSelection());
		assertNull(state.withPresentationUnit(null).getPresentationSelection());
		assertEquals(UnitState.of(UnitToken.MM, UnitToken.CM, INCH), state);
		assertEquals(state.hashCode(), UnitState.of(UnitToken.MM, UnitToken.CM, INCH)
				.hashCode());
		assertNotEquals(state, state.withUsm(UsmDefinition.of(0.0254, "inch", null)));
	}

	@Test
	void usmFactorValidity() {
		for (double invalid : new double[] {0, -0.0, -1, Double.NaN,
				Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -Double.MIN_VALUE}) {
			assertThrows(IllegalArgumentException.class,
					() -> UsmDefinition.of(invalid, null, null), Double.toString(invalid));
		}
		for (double valid : new double[] {Double.MIN_VALUE, Double.MIN_NORMAL,
				Double.MAX_VALUE, 0.0254, 1}) {
			assertEquals(valid, UsmDefinition.of(valid, null, null).getMetresPerUnit());
		}
	}

	@Test
	void usmNameAndSymbolValidity() {
		String sixtyFour = "a".repeat(64);
		assertTrue(UsmDefinition.isValidLabel(sixtyFour));
		assertFalse(UsmDefinition.isValidLabel(sixtyFour + "a"));
		String mathBold = new String(Character.toChars(0x1D400));
		assertTrue(UsmDefinition.isValidLabel(mathBold.repeat(64)),
				"64 scalar values in 128 chars");
		assertFalse(UsmDefinition.isValidLabel(mathBold.repeat(65)));
		assertTrue(UsmDefinition.isValidLabel("inch of mercury"));
		assertTrue(UsmDefinition.isValidLabel("pulgada"));
		for (String invalid : new String[] {"", " in", "in ", "\tin", "in\n", " in",
				"in　", "i\u0007n", "i\u0085n", "\uD800", "a\uDC00b"}) {
			assertFalse(UsmDefinition.isValidLabel(invalid), invalid);
		}
		assertThrows(IllegalArgumentException.class, () -> UsmDefinition.of(1, " x", null));
		assertThrows(IllegalArgumentException.class, () -> UsmDefinition.of(1, null, ""));
		assertEquals("usm", UsmDefinition.of(1, "name only", null).displaySymbol());
		assertEquals("in", INCH.displaySymbol());
	}
}
