/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

/**
 * Certified singular parameter ({@code locus-v2-existence-components.md} section 15.2;
 * {@code locus-v2-certified-construction-model.md} section 10.6): an even-multiplicity
 * zero of a validity predicate that is nonnegative on both sides. It is interior to a
 * component of positive length, which continues through it; the selection changes
 * analytic branch there and the traced curve may have a corner.
 *
 * <p>It is never a boundary and never a point component. Its parameter is in general not a
 * binary64 value; it carries a certified closed enclosure of it and the structural source
 * of the root.</p>
 */
public final class LocusSingularParameter2D {
	private final double parameterLower;
	private final double parameterUpper;
	private final LocusBoundarySource2D source;
	private final int multiplicity;
	private final LocusEvidenceNature2D nature;

	/**
	 * @param parameterLower lower bound of the certified enclosure of the root
	 * @param parameterUpper upper bound of the certified enclosure of the root
	 * @param source structural source of the root
	 * @param multiplicity even multiplicity of the root, at least 2
	 * @param nature certifying nature of the proof
	 */
	public LocusSingularParameter2D(double parameterLower, double parameterUpper,
			LocusBoundarySource2D source, int multiplicity, LocusEvidenceNature2D nature) {
		if (!Double.isFinite(parameterLower) || !Double.isFinite(parameterUpper)
				|| parameterLower > parameterUpper) {
			throw new IllegalArgumentException("Invalid enclosure of a singular parameter");
		}
		if (multiplicity < 2 || multiplicity % 2 != 0) {
			throw new IllegalArgumentException("A singular parameter has even multiplicity");
		}
		this.parameterLower = parameterLower;
		this.parameterUpper = parameterUpper;
		this.source = Objects.requireNonNull(source);
		this.multiplicity = multiplicity;
		this.nature = Objects.requireNonNull(nature);
		if (!nature.certifies()) {
			throw new IllegalArgumentException(
					"A singular parameter needs a certifying proof");
		}
	}

	/** @return lower bound of the certified enclosure of the root */
	public double getParameterLower() {
		return parameterLower;
	}

	/** @return upper bound of the certified enclosure of the root */
	public double getParameterUpper() {
		return parameterUpper;
	}

	/** @return structural source of the root */
	public LocusBoundarySource2D getSource() {
		return source;
	}

	/** @return even multiplicity of the root */
	public int getMultiplicity() {
		return multiplicity;
	}

	/** @return certifying nature of the proof */
	public LocusEvidenceNature2D getNature() {
		return nature;
	}

	/** @return canonical text of the singular parameter */
	public String getSignature() {
		return "singular[" + Double.toHexString(parameterLower) + ","
				+ Double.toHexString(parameterUpper) + "]:" + source.getSignature()
				+ ":multiplicity=" + multiplicity + ":" + nature;
	}

	@Override
	public String toString() {
		return getSignature();
	}
}
