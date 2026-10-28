/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Optional;

/**
 * The regular tangent contact germ of PRE-G9B-CU3 increment {@code CU3-B2} (CU3-B2 design
 * sections 3, 4 and 12; {@code locus-v2-intersections.md} CU3.3 as amended). A regular tangent
 * contact is a double root of class 1 or 2 with order of contact two at a regular source, whose
 * side is the certified sign of {@code h''(t0)} with the canonical orientation of the target. The
 * germ cites the component lineage, the target family with its canonical orientation and the side;
 * it contains no binary64 value, interval, parameter, evidence class, order or configuration.
 */
final class RegularTangentAnchor2D {
	/** Prefix of the durable regular tangent contact germ. */
	static final String GERM_PREFIX = "pre-g9b-cu3/regular-tangent-germ/v1/";
	// Revision-local channel from the analysis to the capability; never persisted.
	private static final String MARKER = "pre-g9b-cu3/regular-tangent-anchor/v1/";
	private static final String POSITIVE = "positive";
	private static final String NEGATIVE = "negative";
	private static final String CANONICAL = "/canonical";

	private final String side;

	private RegularTangentAnchor2D(String side) {
		this.side = side;
	}

	/**
	 * @param canonicalSide the certified canonical side, {@code 1} or {@code -1}
	 * @return the anchor, or {@code null} for an undecided side
	 */
	static RegularTangentAnchor2D of(int canonicalSide) {
		return canonicalSide == 1 ? new RegularTangentAnchor2D(POSITIVE)
				: canonicalSide == -1 ? new RegularTangentAnchor2D(NEGATIVE) : null;
	}

	/** @return the revision-local marker that carries the anchor on a candidate */
	String marker() {
		return MARKER + side;
	}

	/** @return the anchor that a candidate carries, if any */
	static Optional<RegularTangentAnchor2D> fromMarker(Optional<String> key) {
		if (!key.isPresent() || !key.get().startsWith(MARKER)) {
			return Optional.empty();
		}
		String value = key.get().substring(MARKER.length());
		return POSITIVE.equals(value) || NEGATIVE.equals(value)
				? Optional.of(new RegularTangentAnchor2D(value)) : Optional.empty();
	}

	/**
	 * @param componentLineage the identity key of the component
	 * @param targetFamily the family of the target
	 * @return the durable germ of the anchor
	 */
	String germ(String componentLineage, String targetFamily) {
		return GERM_PREFIX + framed(componentLineage) + framed(targetFamily + CANONICAL)
				+ framed(side);
	}

	/** @return whether the key is a canonical regular tangent contact germ */
	static boolean isGerm(String key) {
		return germLineage(key).isPresent();
	}

	/** @return the component lineage that a canonical regular tangent germ cites */
	static Optional<String> germLineage(String key) {
		if (key == null || !key.startsWith(GERM_PREFIX)) {
			return Optional.empty();
		}
		try {
			String encoded = key.substring(GERM_PREFIX.length());
			String[] component = frame(encoded, 0);
			String[] target = frame(encoded, Integer.parseInt(component[1]));
			String[] side = frame(encoded, Integer.parseInt(target[1]));
			if (Integer.parseInt(side[1]) != encoded.length()
					|| !target[0].endsWith(CANONICAL)
					|| target[0].length() == CANONICAL.length()
					|| !(POSITIVE.equals(side[0]) || NEGATIVE.equals(side[0]))) {
				return Optional.empty();
			}
			String canonical = GERM_PREFIX + framed(component[0]) + framed(target[0])
					+ framed(side[0]);
			return canonical.equals(key) ? Optional.of(component[0]) : Optional.empty();
		} catch (ArithmeticException | IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	/** @return {@code {value, next offset}} of the frame at the offset */
	private static String[] frame(String value, int offset) {
		int separator = value.indexOf(':', offset);
		if (separator <= offset) {
			throw new IllegalArgumentException("Malformed regular tangent frame");
		}
		String lengthText = value.substring(offset, separator);
		int length = Integer.parseInt(lengthText);
		if (length <= 0 || !Integer.toString(length).equals(lengthText)) {
			throw new IllegalArgumentException("Noncanonical regular tangent frame");
		}
		int start = separator + 1;
		int end = Math.addExact(start, length);
		if (end > value.length()) {
			throw new IllegalArgumentException("Truncated regular tangent frame");
		}
		return new String[] {value.substring(start, end), Integer.toString(end)};
	}

	private static String framed(String value) {
		return value.length() + ":" + value;
	}

	@Override
	public String toString() {
		return "regular tangent anchor (side " + side + ")";
	}
}
