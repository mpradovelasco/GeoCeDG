/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusDefinition2D;

/**
 * PRE-G9B-CU3 preparation probe access, run only through
 * {@code tools/benchmark/pre-g9b-cu/run-cu3-probe.ps1}. It exposes, read-only and for the
 * probe only, the class-v3 second-order enclosure of the traced point that the product keeps
 * package-private ({@code locus-v2-certified-construction-model.md} section 10.7). It is not
 * part of any source set: the runner adds it for one explicit run and removes its classes
 * again. No product class, certificate or persisted state reads it.
 */
public final class PreG9BCu3EnclosureProbeAccess {

	private final ConstructionBoxModel2D model;

	private PreG9BCu3EnclosureProbeAccess(ConstructionBoxModel2D model) {
		this.model = model;
	}

	/**
	 * @param definition published definition of a reconstructible locus
	 * @return access to its class-v3 box model, or {@code null} outside the class
	 */
	public static PreG9BCu3EnclosureProbeAccess capture(LocusDefinition2D definition) {
		ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(definition);
		return model == null ? null : new PreG9BCu3EnclosureProbeAccess(model);
	}

	/**
	 * @return {@code {x, y, x', y', x'', y''}} as {@code {lower, upper}} pairs on the box, or
	 *         {@code null} when the program is not certified valid on it
	 */
	public double[][] traced(double lower, double upper, boolean periodic) {
		SplineOutwardInterval2D[][] jet = model.encloseTraced(
				new SplineOutwardInterval2D(lower, upper), periodic);
		if (jet == null) {
			return null;
		}
		return new double[][] {pair(jet[0][0]), pair(jet[0][1]), pair(jet[1][0]),
			pair(jet[1][1]), pair(jet[2][0]), pair(jet[2][1])};
	}

	/** @return whether every step of the captured program is structurally total */
	public boolean structurallyTotal() {
		return model.isStructurallyTotal();
	}

	private static double[] pair(SplineOutwardInterval2D interval) {
		return new double[] {interval.lower, interval.upper};
	}
}
