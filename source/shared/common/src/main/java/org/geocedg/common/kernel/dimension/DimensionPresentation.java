/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.dimension;

import java.util.ArrayList;

import org.geocedg.common.kernel.units.DocumentUnitSystem;
import org.geocedg.common.kernel.units.UnitQuantity;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoElement;

/**
 * Presentation string of a native dimension value (unit-system specification section 14,
 * DQ-E2-3). It is the only reader of the unit state for dimensions and runs outside
 * geometric computation: it formats an already computed model-unit length and never
 * feeds anything back into geometry.
 */
public final class DimensionPresentation {

	/** Presentation failure marker of a non-finite conversion (unit-system section 14.3). */
	public static final String FAILURE_MARKER = "?";

	private static final String LISTENER_KEY = "geocedg.native-dimensions.presentation";

	private DimensionPresentation() {
	}

	/**
	 * @param length model-unit length (finite, non-negative)
	 * @param state document unit state
	 * @param kernel kernel providing the number format
	 * @param tpl string template of the text output
	 * @return {@code format(display(L)) + " " + suffix(effP)}, the bare {@code format(L)}
	 *         when the construction unit is unspecified, or the failure marker plus the
	 *         suffix when the conversion is not finite; with the document policy "suffix
	 *         hidden" (unit-system v1.1, section 14.2) the same text without
	 *         {@code " " + suffix(effP)}
	 */
	public static String format(double length, UnitState state, Kernel kernel,
			StringTemplate tpl) {
		UnitQuantity quantity = state.display(length);
		if (quantity.getStatus() == UnitQuantity.Status.UNSPECIFIED) {
			return kernel.format(length, tpl);
		}
		String suffix = "";
		if (state.isDimensionUnitSuffixShown()) {
			UnitToken unit = state.effectivePresentationUnit();
			suffix = " " + state.symbolOf(unit);
		}
		if (quantity.getStatus() == UnitQuantity.Status.NOT_FINITE) {
			return FAILURE_MARKER + suffix;
		}
		return kernel.format(quantity.getValue(), tpl) + suffix;
	}

	/**
	 * Registers, once per construction, the presentation refresh that runs on every
	 * unit-state change. The refresh rewrites only the text strings of the native
	 * dimensions of that construction; it recomputes no geometry.
	 *
	 * @param cons construction
	 */
	static void ensureRefresh(Construction cons) {
		DocumentUnitSystem units = cons.getUnitSystem();
		units.addListenerOnce(LISTENER_KEY, () -> refreshAll(cons));
	}

	/**
	 * Rewrites the presentation strings of every native dimension of a construction.
	 *
	 * @param cons construction
	 */
	public static void refreshAll(Construction cons) {
		boolean changed = false;
		for (AlgoElement algo : new ArrayList<>(cons.getAlgoList())) {
			if (algo instanceof AlgoNativeDimension) {
				changed |= ((AlgoNativeDimension) algo).refreshPresentation(true);
			}
		}
		if (changed) {
			cons.getKernel().notifyRepaint();
		}
	}
}
