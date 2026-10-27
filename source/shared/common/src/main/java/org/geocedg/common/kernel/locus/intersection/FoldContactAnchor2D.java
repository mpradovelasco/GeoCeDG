/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusCertifiedFold2D;

/**
 * The exact label of a certified fold of {@code CU2} and the fold-contact germ of PRE-G9B-CU3
 * increment {@code CU3-B1} (ADR 0039 decision 3 as amended; Stage B design section 6.1; CU3-B1
 * design sections 2 and 3). The label is the involution of {@code fold-symmetry/v1} and its fixed
 * point, decided from the canonical enclosure of {@code t0} alone: the two fixed points of an
 * involution lie {@code pi} apart, so the label is decided when the enclosure meets exactly one
 * of them. Neither the label nor the germ contains a binary64 value, an interval, an evidence
 * class, an order or a decision configuration.
 */
final class FoldContactAnchor2D {
	/** Prefix of the durable fold-contact germ. */
	static final String GERM_PREFIX = "pre-g9b-cu3/fold-contact-germ/v1/";
	/** Involution {@code t -> -t}, mirror {@code HORIZONTAL}, fixed points {@code 0, pi}. */
	static final String NEGATION = "t->-t";
	/** Involution {@code t -> pi - t}, mirror {@code VERTICAL}, fixed points {@code +-pi/2}. */
	static final String SUPPLEMENT = "t->pi-t";
	// Revision-local channel from the analysis to the capability; never persisted.
	private static final String MARKER = "pre-g9b-cu3/fold-contact-anchor/v1/";
	private static final double HALF_PI = Math.PI / 2;

	private final String involution;
	private final String fixedPoint;
	private final double lower;
	private final double upper;

	private FoldContactAnchor2D(String involution, String fixedPoint, double lower,
			double upper) {
		this.involution = involution;
		this.fixedPoint = fixedPoint;
		this.lower = lower;
		this.upper = upper;
	}

	/**
	 * @param fold a certified fold
	 * @return its anchor, or {@code null} when the symmetry is unknown or the enclosure of
	 *         {@code t0} does not meet exactly one fixed point of the involution
	 */
	static FoldContactAnchor2D of(LocusCertifiedFold2D fold) {
		double low = fold.getParameterLower();
		double high = fold.getParameterUpper();
		String involution;
		boolean first;
		boolean second;
		String firstLabel;
		String secondLabel;
		String symmetry = fold.getSymmetry();
		if ((ConstructionSymmetry2D.VERSION + ":" + ConstructionSymmetry2D.Mirror.HORIZONTAL)
				.equals(symmetry)) {
			involution = NEGATION;
			first = low <= 0 && 0 <= high;
			second = meets(low, high, Math.PI, Math.nextUp(Math.PI))
					|| meets(low, high, -Math.nextUp(Math.PI), -Math.PI);
			firstLabel = "0";
			secondLabel = "pi";
		} else if ((ConstructionSymmetry2D.VERSION + ":" + ConstructionSymmetry2D.Mirror.VERTICAL)
				.equals(symmetry)) {
			involution = SUPPLEMENT;
			first = meets(low, high, HALF_PI, Math.nextUp(HALF_PI));
			second = meets(low, high, -Math.nextUp(HALF_PI), -HALF_PI);
			firstLabel = "pi/2";
			secondLabel = "-pi/2";
		} else {
			return null;
		}
		if (first == second) {
			return null;
		}
		return new FoldContactAnchor2D(involution, first ? firstLabel : secondLabel, low,
				high);
	}

	// The real fixed point lies in [a, b]; the enclosure meets it when it meets [a, b].
	private static boolean meets(double low, double high, double a, double b) {
		return low <= b && a <= high;
	}

	/** @return the involution of the anchor */
	String getInvolution() {
		return involution;
	}

	/** @return the exact fixed point of the anchor: {@code 0}, {@code pi} or {@code +-pi/2} */
	String getFixedPoint() {
		return fixedPoint;
	}

	/** @return the label: the framed involution followed by the framed fixed point */
	String label() {
		return framed(involution) + framed(fixedPoint);
	}

	/** @return the midpoint of the canonical enclosure of {@code t0} */
	double center() {
		return lower + (upper - lower) / 2;
	}

	/**
	 * @param low lower end of a parameter interval near the anchor, in its coordinates
	 * @param high upper end
	 * @return an outward enclosure of its image under the involution, the reflection
	 *         {@code t -> 2 t0 - t} about the enclosed fixed point
	 */
	double[] image(double low, double high) {
		return new double[] {Math.nextDown(2 * lower - high), Math.nextUp(2 * upper - low)};
	}

	/** @return the revision-local marker that carries the anchor on a candidate */
	String marker() {
		return MARKER + label();
	}

	/** @return the anchor that a candidate carries, if any */
	static Optional<FoldContactAnchor2D> fromMarker(Optional<String> key) {
		if (!key.isPresent() || !key.get().startsWith(MARKER)) {
			return Optional.empty();
		}
		String[] label = parseLabel(key.get().substring(MARKER.length()));
		return label == null ? Optional.empty()
				: Optional.of(new FoldContactAnchor2D(label[0], label[1], 0, 0));
	}

	/** @return the durable germ of the anchor on the component with the given lineage */
	String germ(String componentLineage) {
		return GERM_PREFIX + framed(componentLineage) + framed(label());
	}

	/** @return whether the key is a canonical fold-contact germ */
	static boolean isGerm(String key) {
		return germLineage(key).isPresent();
	}

	/** @return the component lineage that a canonical fold-contact germ cites */
	static Optional<String> germLineage(String key) {
		if (key == null || !key.startsWith(GERM_PREFIX)) {
			return Optional.empty();
		}
		try {
			String encoded = key.substring(GERM_PREFIX.length());
			String[] component = frame(encoded, 0);
			String[] label = frame(encoded, Integer.parseInt(component[1]));
			if (Integer.parseInt(label[1]) != encoded.length()
					|| parseLabel(label[0]) == null) {
				return Optional.empty();
			}
			String canonical = GERM_PREFIX + framed(component[0]) + framed(label[0]);
			return canonical.equals(key) ? Optional.of(component[0]) : Optional.empty();
		} catch (ArithmeticException | IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	/** @return {@code {involution, fixed point}} of a canonical label, or {@code null} */
	private static String[] parseLabel(String label) {
		try {
			String[] involution = frame(label, 0);
			String[] point = frame(label, Integer.parseInt(involution[1]));
			if (Integer.parseInt(point[1]) != label.length()) {
				return null;
			}
			boolean valid = NEGATION.equals(involution[0])
					&& ("0".equals(point[0]) || "pi".equals(point[0]))
					|| SUPPLEMENT.equals(involution[0])
							&& ("pi/2".equals(point[0]) || "-pi/2".equals(point[0]));
			return valid ? new String[] {involution[0], point[0]} : null;
		} catch (ArithmeticException | IllegalArgumentException exception) {
			return null;
		}
	}

	/** @return {@code {value, next offset}} of the frame at the offset */
	private static String[] frame(String value, int offset) {
		int separator = value.indexOf(':', offset);
		if (separator <= offset) {
			throw new IllegalArgumentException("Malformed fold-contact frame");
		}
		String lengthText = value.substring(offset, separator);
		int length = Integer.parseInt(lengthText);
		if (length <= 0 || !Integer.toString(length).equals(lengthText)) {
			throw new IllegalArgumentException("Noncanonical fold-contact frame");
		}
		int start = separator + 1;
		int end = Math.addExact(start, length);
		if (end > value.length()) {
			throw new IllegalArgumentException("Truncated fold-contact frame");
		}
		return new String[] {value.substring(start, end), Integer.toString(end)};
	}

	private static String framed(String value) {
		return value.length() + ":" + value;
	}

	@Override
	public String toString() {
		return "fold anchor (" + involution + ", " + fixedPoint + ")";
	}
}
