/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.common.kernel.locus;

import java.util.List;

/** One revision's direction and durable declarations for a constructive cut. */
public record LocusTraversalCut2D(double parameter, List<String> declarations) {
	static final String BOUNDARY_PREDICATE = "DECLARED_CONSTRUCTIVE_CUT_V1";

	public LocusTraversalCut2D {
		if (!Double.isFinite(parameter) || declarations.isEmpty()) {
			throw new IllegalArgumentException("A constructive cut needs a finite direction and declaration");
		}
		parameter = parameter == 0 ? 0 : parameter;
		declarations = declarations.stream().sorted().distinct().toList();
	}

	LocusBoundary2D boundary(boolean start) {
		return new LocusBoundary2D(LocusBoundary2D.Kind.DECLARED_CONSTRUCTIVE_CUT,
				new LocusBoundarySource2D(String.join("+", declarations),
						BOUNDARY_PREDICATE, start ? "start" : "end"), start,
				LocusEvidenceNature2D.EXACT).withConstructiveCutDeclarations(declarations);
	}
}
