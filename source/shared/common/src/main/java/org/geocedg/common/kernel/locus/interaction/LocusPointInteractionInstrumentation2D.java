/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.interaction;

/** Query-local functional counters with deterministic ceiling enforcement. */
final class LocusPointInteractionInstrumentation2D {
	private final LocusPointInteractionWorkBudget2D budget;
	private long semanticEvaluations;
	private long branchesInspected;
	private long componentsInspected;
	private long polynomialSpans;
	private long subdivisions;
	private long refinementIterations;
	private long retainedCandidates;
	private long localSearches;
	private long globalSearches;
	private long globalFallbacks;
	private long cacheHits;
	private long cacheMisses;

	LocusPointInteractionInstrumentation2D(
			LocusPointInteractionWorkBudget2D budget) {
		this.budget = budget;
	}

	void semanticEvaluation() {
		semanticEvaluations = increment(semanticEvaluations,
				budget.getMaximumSemanticEvaluations(), "semantic evaluations");
	}

	void branch() {
		branchesInspected = increment(branchesInspected,
				budget.getMaximumSubdivisions(), "branches inspected");
	}

	void component() {
		componentsInspected = increment(componentsInspected,
				budget.getMaximumSubdivisions(), "components inspected");
	}

	void searchScope(boolean local) {
		if (local) {
			localSearches++;
		} else {
			globalSearches++;
		}
	}

	void sessionCache(long hits, long misses) {
		if (misses > budget.getMaximumSemanticEvaluations()) {
			throw new WorkLimitException("nested semantic evaluations");
		}
		semanticEvaluations = misses;
		cacheHits = hits;
		cacheMisses = misses;
	}

	void polynomialSpan() {
		polynomialSpans = increment(polynomialSpans,
				budget.getMaximumSubdivisions(), "polynomial spans");
	}

	void subdivision() {
		subdivisions = increment(subdivisions, budget.getMaximumSubdivisions(),
				"semantic subdivisions");
	}

	void refinementIteration() {
		refinementIterations = increment(refinementIterations,
				(long) budget.getMaximumSubdivisions()
						* budget.getMaximumRefinementIterations(),
				"refinement iterations");
	}

	void candidate() {
		retainedCandidates = increment(retainedCandidates,
				budget.getMaximumCandidates(), "retained candidates");
	}

	LocusPointInteractionInstrumentationSnapshot2D snapshot() {
		return new LocusPointInteractionInstrumentationSnapshot2D(
				semanticEvaluations, branchesInspected, componentsInspected,
				polynomialSpans, subdivisions, refinementIterations,
				retainedCandidates, localSearches, globalSearches,
				globalFallbacks, cacheHits, cacheMisses);
	}

	private static long increment(long current, long maximum, String name) {
		if (current >= maximum) {
			throw new WorkLimitException(name);
		}
		return current + 1;
	}

	static final class WorkLimitException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		WorkLimitException(String name) {
			super("R6 work budget exhausted: " + name);
		}
	}
}
