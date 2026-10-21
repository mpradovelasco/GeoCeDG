/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Maximality;

/**
 * Point component ({@code locus-v2-existence-components.md} section 15.4): existence of a
 * branch reduced to one isolated parameter {@code t*}, a kind of component of its own and
 * never an interval or arc of positive length.
 *
 * <p>{@code t*} is in general not a binary64 value; the component carries a certified closed
 * enclosure of it and the structural source of the isolated root. Membership and selection
 * use that structural address, never equality with a binary64 parameter. Its inclusion is
 * closed, its length is zero, and its position is a certified enclosure of the limit form or
 * unavailable. It is declared in results and in the inspector and is not drawn.</p>
 */
public final class LocusPointComponent2D {
	/** Availability of the geometric position of the point component. */
	public enum PositionState {
		/** A certified enclosure of {@code F(t*)} from the limit form. */
		CERTIFIED_ENCLOSURE,
		/** The component exists, but its position is not available. */
		UNAVAILABLE
	}

	private final double parameterLower;
	private final double parameterUpper;
	private final LocusBoundarySource2D source;
	private final Maximality maximality;
	private final LocusEvidenceNature2D nature;
	private final String evidenceKey;
	private final PositionState positionState;
	private final double[] positionEnclosure;

	/**
	 * @param parameterLower lower bound of the certified enclosure of {@code t*}
	 * @param parameterUpper upper bound of the certified enclosure of {@code t*}
	 * @param source structural source of the isolated root
	 * @param maximality established with certified nonexistence on both sides
	 * @param nature certifying nature of the proof
	 * @param evidenceKey revision-local evidence key; never a durable identity
	 * @param positionEnclosure {@code {xLower, xUpper, yLower, yUpper}} of
	 *        {@code F(t*)}, or {@code null} when the position is unavailable
	 */
	public LocusPointComponent2D(double parameterLower, double parameterUpper,
			LocusBoundarySource2D source, Maximality maximality,
			LocusEvidenceNature2D nature, String evidenceKey,
			double[] positionEnclosure) {
		if (!Double.isFinite(parameterLower) || !Double.isFinite(parameterUpper)
				|| parameterLower > parameterUpper) {
			throw new IllegalArgumentException("Invalid enclosure of an isolated parameter");
		}
		this.parameterLower = parameterLower;
		this.parameterUpper = parameterUpper;
		this.source = Objects.requireNonNull(source);
		this.maximality = Objects.requireNonNull(maximality);
		this.nature = Objects.requireNonNull(nature);
		if (!nature.certifies()) {
			throw new IllegalArgumentException("A point component needs a certifying proof");
		}
		if (evidenceKey == null || evidenceKey.trim().isEmpty()
				|| !evidenceKey.equals(evidenceKey.trim())) {
			throw new IllegalArgumentException("Point evidence key must be canonical");
		}
		this.evidenceKey = evidenceKey;
		if (positionEnclosure == null) {
			this.positionState = PositionState.UNAVAILABLE;
			this.positionEnclosure = null;
		} else {
			if (positionEnclosure.length != 4
					|| !(positionEnclosure[0] <= positionEnclosure[1])
					|| !(positionEnclosure[2] <= positionEnclosure[3])
					|| !Double.isFinite(positionEnclosure[0])
					|| !Double.isFinite(positionEnclosure[1])
					|| !Double.isFinite(positionEnclosure[2])
					|| !Double.isFinite(positionEnclosure[3])) {
				throw new IllegalArgumentException("Invalid position enclosure");
			}
			this.positionState = PositionState.CERTIFIED_ENCLOSURE;
			this.positionEnclosure = positionEnclosure.clone();
		}
	}

	/** @return lower bound of the certified enclosure of {@code t*} */
	public double getParameterLower() {
		return parameterLower;
	}

	/** @return upper bound of the certified enclosure of {@code t*} */
	public double getParameterUpper() {
		return parameterUpper;
	}

	/** @return structural source of the isolated root, its structural address */
	public LocusBoundarySource2D getSource() {
		return source;
	}

	/** @return whether certified nonexistence bounds the component on both sides */
	public Maximality getMaximality() {
		return maximality;
	}

	/** @return certifying nature of the proof */
	public LocusEvidenceNature2D getNature() {
		return nature;
	}

	/** @return revision-local evidence key; never a durable identity */
	public String getEvidenceKey() {
		return evidenceKey;
	}

	/** @return availability of the geometric position */
	public PositionState getPositionState() {
		return positionState;
	}

	/**
	 * @return {@code {xLower, xUpper, yLower, yUpper}} of {@code F(t*)}
	 * @throws IllegalStateException when the position is unavailable
	 */
	public double[] getPositionEnclosure() {
		if (positionEnclosure == null) {
			throw new IllegalStateException("The position of the point component is unavailable");
		}
		return positionEnclosure.clone();
	}

	/** @return zero: a point component contributes nothing to any total */
	public double getLength() {
		return 0;
	}

	/** @return canonical text of the point component */
	public String getSignature() {
		StringBuilder signature = new StringBuilder("point[")
				.append(Double.toHexString(parameterLower)).append(',')
				.append(Double.toHexString(parameterUpper)).append("]:")
				.append(source.getSignature()).append(':').append(maximality).append(':')
				.append(nature).append(':').append(evidenceKey).append(':')
				.append(positionState);
		if (positionEnclosure != null) {
			for (double bound : positionEnclosure) {
				signature.append(',').append(Double.toHexString(bound));
			}
		}
		return signature.toString();
	}

	@Override
	public String toString() {
		return getSignature();
	}
}
