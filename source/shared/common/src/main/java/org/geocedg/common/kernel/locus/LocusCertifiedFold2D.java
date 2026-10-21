/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

/**
 * Certified non-degenerate fold of the traced map (PRE-G9B-CU2, certification design
 * section 5; {@code locus-v2-certified-construction-model.md} section 10.7): a stationary point
 * {@code F'(t0) = 0} whose existence follows from a verified symmetry and whose
 * non-degeneracy and isolation follow from one component of {@code F''} excluding zero on
 * an isolation band. The curve turns back along the same arc there; the band is the only
 * place where a drawing may waive its turning criterion (consumption design section 5.1).
 *
 * <p>The band is given in the coordinates of the certificate and may extend one period
 * past the canonical domain around the seam; {@link #containsCanonical} reads it modulo
 * the period.</p>
 */
public final class LocusCertifiedFold2D {
	private final double parameterLower;
	private final double parameterUpper;
	private final double bandLower;
	private final double bandUpper;
	private final String symmetry;
	private final LocusEvidenceNature2D nature;

	/**
	 * @param parameterLower lower bound of the canonical enclosure of {@code t0}
	 * @param parameterUpper upper bound of the canonical enclosure of {@code t0}
	 * @param bandLower lower end of the isolation band
	 * @param bandUpper upper end of the isolation band
	 * @param symmetry verified symmetry that proves the existence
	 * @param nature certifying nature of the isolation proof
	 */
	public LocusCertifiedFold2D(double parameterLower, double parameterUpper,
			double bandLower, double bandUpper, String symmetry,
			LocusEvidenceNature2D nature) {
		if (!Double.isFinite(parameterLower) || !Double.isFinite(parameterUpper)
				|| parameterLower > parameterUpper || !Double.isFinite(bandLower)
				|| !Double.isFinite(bandUpper) || !(bandLower < bandUpper)) {
			throw new IllegalArgumentException("Invalid enclosure of a fold");
		}
		this.parameterLower = parameterLower;
		this.parameterUpper = parameterUpper;
		this.bandLower = bandLower;
		this.bandUpper = bandUpper;
		this.symmetry = Objects.requireNonNull(symmetry);
		this.nature = Objects.requireNonNull(nature);
		if (!nature.certifies()) {
			throw new IllegalArgumentException("A fold needs a certifying proof");
		}
	}

	/** @return lower bound of the canonical enclosure of {@code t0} */
	public double getParameterLower() {
		return parameterLower;
	}

	/** @return upper bound of the canonical enclosure of {@code t0} */
	public double getParameterUpper() {
		return parameterUpper;
	}

	/** @return lower end of the isolation band */
	public double getBandLower() {
		return bandLower;
	}

	/** @return upper end of the isolation band */
	public double getBandUpper() {
		return bandUpper;
	}

	/** @return verified symmetry that proves the existence of the stationary point */
	public String getSymmetry() {
		return symmetry;
	}

	/** @return certifying nature of the isolation proof */
	public LocusEvidenceNature2D getNature() {
		return nature;
	}

	/**
	 * @param period period of the canonical domain, or {@code 0} on a bounded domain
	 * @return whether the canonical parameter lies in the isolation band, modulo the period
	 */
	public boolean containsCanonical(double canonicalParameter, double period) {
		if (bandLower <= canonicalParameter && canonicalParameter <= bandUpper) {
			return true;
		}
		return period > 0 && (bandLower <= canonicalParameter + period
				&& canonicalParameter + period <= bandUpper
				|| bandLower <= canonicalParameter - period
						&& canonicalParameter - period <= bandUpper);
	}

	/** @return canonical text of the fold */
	public String getSignature() {
		return "fold[" + Double.toHexString(parameterLower) + ","
				+ Double.toHexString(parameterUpper) + "]:band["
				+ Double.toHexString(bandLower) + "," + Double.toHexString(bandUpper) + "]:"
				+ symmetry + ":" + nature;
	}

	@Override
	public String toString() {
		return getSignature();
	}
}
