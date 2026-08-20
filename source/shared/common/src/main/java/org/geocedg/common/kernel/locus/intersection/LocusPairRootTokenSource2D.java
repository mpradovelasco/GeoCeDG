/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Objects;
import java.util.function.Function;

/** Supplies opaque tokens without using result order or geometry. */
@FunctionalInterface
public interface LocusPairRootTokenSource2D {
	/** @return token scoped to one explicit constructive solution lineage */
	String nextToken(String solutionLineageKey);

	/**
	 * Returns a token scoped to one explicit constructive solution lineage.
	 *
	 * @return opaque semantic token
	 */
	default String nextToken(IntersectionTokenLineage2D lineage) {
		Objects.requireNonNull(lineage);
		return nextToken(lineage.getSolutionLineageKey());
	}

	/**
	 * Bridges the frozen G8 solution key and richer public semantic material.
	 * Legacy sources receive their exact original key; G9U0 semantic adapters
	 * consume only the stable component lineage.
	 *
	 * @return opaque token
	 */
	default String nextToken(String legacySolutionLineageKey,
			IntersectionTokenLineage2D lineage) {
		Objects.requireNonNull(lineage);
		return nextToken(Objects.requireNonNull(legacySolutionLineageKey));
	}

	/**
	 * Adapts a lineage-aware public token source without changing the frozen G8
	 * functional-interface shape.
	 *
	 * @return compatible token source
	 */
	static LocusPairRootTokenSource2D semantic(
			Function<IntersectionTokenLineage2D, String> source) {
		Objects.requireNonNull(source);
		return new LocusPairRootTokenSource2D() {
			@Override
			public String nextToken(String solutionLineageKey) {
				throw new IllegalStateException(
						"Semantic token lineage is required by this source");
			}

			@Override
			public String nextToken(IntersectionTokenLineage2D lineage) {
				return source.apply(Objects.requireNonNull(lineage));
			}

			@Override
			public String nextToken(String legacySolutionLineageKey,
					IntersectionTokenLineage2D lineage) {
				Objects.requireNonNull(legacySolutionLineageKey);
				return source.apply(Objects.requireNonNull(lineage));
			}
		};
	}
}
