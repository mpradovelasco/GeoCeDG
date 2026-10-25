/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;

/**
 * The residual of a class-v3 source and a captured line, segment, ray or circle target in the
 * captured configuration of {@code CU2} with the binary64 target authority of the adapter
 * ({@code locus-v2-intersections.md} section 2.1 and CU3.4): the second-order enclosure of the
 * traced point of {@code locus-v2-certified-construction-model.md} section 10.7 composed with the
 * residual of the target (PRE-G9B-CU3 piece I-1, Stage B design section 4.3).
 */
final class CapturedResidualJetSource2D implements ResidualJetSource2D {
	/** Name of this decision configuration. */
	static final String CONFIGURATION = "captured-class-v3/target-adapter";

	private final ConstructionBoxModel2D model;
	private final CertifiedResidualTarget2D target;
	private final boolean periodic;

	private CapturedResidualJetSource2D(ConstructionBoxModel2D model,
			CertifiedResidualTarget2D target, boolean periodic) {
		this.model = model;
		this.target = target;
		this.periodic = periodic;
	}

	/**
	 * @return the residual of the source and the target, or {@code null} when the source has no
	 *         class-v3 program or the target no composable residual
	 */
	static ResidualJetSource2D capture(LocusDefinition2D definition,
			LocusIntersectionTarget2D target) {
		if (!(target instanceof CertifiedResidualTarget2D)) {
			return null;
		}
		ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(definition);
		if (model == null) {
			return null;
		}
		return new CapturedResidualJetSource2D(model, (CertifiedResidualTarget2D) target,
				model.getProgram().getDriver() == CertifiedConstructionProgram2D.Driver.CIRCLE);
	}

	@Override
	public String getConfiguration() {
		return CONFIGURATION;
	}

	@Override
	public ResidualJetEnclosure2D enclose(double lower, double upper) {
		try {
			SplineOutwardInterval2D[][] traced = model.encloseTraced(
					new SplineOutwardInterval2D(lower, upper), periodic);
			if (traced == null) {
				return null;
			}
			SplineOutwardInterval2D[] jet = target.residualJet(traced);
			return new ResidualJetEnclosure2D(bounds(jet[0]), bounds(jet[1]), bounds(jet[2]));
		} catch (ArithmeticException exception) {
			return null;
		}
	}

	@Override
	public double[] normalizedResidual(double parameter) {
		try {
			SplineOutwardInterval2D[][] traced = model.encloseTraced(
					SplineOutwardInterval2D.point(parameter), periodic);
			return traced == null ? null
					: bounds(target.normalizedResidual(traced[0][0], traced[0][1]));
		} catch (ArithmeticException exception) {
			return null;
		}
	}

	/** @return the extent of a bounded target, or {@code NaN} */
	double boundedTargetExtent() {
		return target.boundedExtent();
	}

	private static double[] bounds(SplineOutwardInterval2D interval) {
		return new double[] {interval.lower, interval.upper};
	}
}
