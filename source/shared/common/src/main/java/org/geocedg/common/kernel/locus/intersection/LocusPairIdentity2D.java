/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.nio.charset.StandardCharsets;

/** Source-order-independent identities for semantic Locus V2 pairs. */
public final class LocusPairIdentity2D {
	private LocusPairIdentity2D() {
	}

	/** @return canonical unordered identity for two semantic loci */
	public static String sourcePair(String first, String second) {
		return unordered(requireText(first), requireText(second));
	}

	/** @return canonical unordered branch/component-pair lineage */
	public static String componentPair(String firstBranch,
			String firstComponent, String secondBranch,
			String secondComponent) {
		String first = framed(requireText(firstBranch))
				+ framed(requireText(firstComponent));
		String second = framed(requireText(secondBranch))
				+ framed(requireText(secondComponent));
		return unordered(first, second);
	}

	/**
	 * Creates an opaque deterministic token from semantic lineage, never from
	 * coordinates, parameter values, result order or source revisions.
	 *
	 * @return opaque semantic solution token
	 */
	public static String solutionToken(String sourcePairIdentity,
			String constructiveLineage, String topologyContext,
			String solutionLineage) {
		String material = framed(requireText(sourcePairIdentity))
				+ framed(requireText(constructiveLineage))
				+ framed(requireText(topologyContext))
				+ framed(requireText(solutionLineage));
		long hash = fnv1a64(material.getBytes(StandardCharsets.UTF_8));
		return "locus-pair-root/" + Long.toUnsignedString(hash, 16);
	}

	private static String unordered(String first, String second) {
		return first.compareTo(second) <= 0
				? framed(first) + framed(second)
				: framed(second) + framed(first);
	}

	private static String framed(String value) {
		return value.length() + ":" + value;
	}

	private static long fnv1a64(byte[] value) {
		long hash = 0xcbf29ce484222325L;
		for (byte current : value) {
			hash ^= current & 0xffL;
			hash *= 0x100000001b3L;
		}
		return hash;
	}

	private static String requireText(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Semantic identity is required");
		}
		return value;
	}
}
