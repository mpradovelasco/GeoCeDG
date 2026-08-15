/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/** Supplies opaque provisional tokens without using result order or geometry. */
@FunctionalInterface
public interface LocusPairRootTokenSource2D {
	/** @return token scoped to one explicit constructive solution lineage */
	String nextToken(String solutionLineageKey);
}
