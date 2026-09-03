/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spline;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Immutable arithmetic evidence for one derived structural spline construction. */
public final class SplineConstructionEvidence2D {

	/** Arithmetic route, not a change of spline family or scientific tolerance. */
	public enum Path {
		BINARY64, HIGH_PRECISION_BINARY64, HIGH_PRECISION_STRUCTURAL, REJECTED
	}

	/** Recorded numerical rejection; no prior revision or global counter is consulted. */
	public enum Failure {
		FAST_SOLVE_REJECTED, ORIGINAL_EQUATION_REJECTED, NONFINITE_CACHE,
		BINARY64_REPRESENTATION_REJECTED,
		PIVOT_REJECTED, REDUCED_RESIDUAL_REJECTED, STABILITY_NOT_ESTABLISHED,
		PRECISION_EXHAUSTED, WORK_EXHAUSTED
	}

	/** Failure retains bounded work evidence even when no model is admitted. */
	public static final class AdmissionException extends IllegalArgumentException {
		private final SplineConstructionEvidence2D evidence;

		AdmissionException(String message, SplineConstructionEvidence2D evidence) {
			super(message);
			this.evidence = evidence;
		}

		/** @return immutable evidence of this failed construction only */
		public SplineConstructionEvidence2D getEvidence() {
			return evidence;
		}
	}

	private final Path path;
	private final int precision;
	private final int retainedPrecision;
	private final int precisionLevels;
	private final long solveOperations;
	private final long expansionOperations;
	private final long admissionOperations;
	private final Map<Failure, Integer> failures;

	SplineConstructionEvidence2D(Path path, int precision, int retainedPrecision,
			int precisionLevels, long solveOperations, long expansionOperations,
			long admissionOperations,
			Map<Failure, Integer> failures) {
		this.path = path;
		this.precision = precision;
		this.retainedPrecision = retainedPrecision;
		this.precisionLevels = precisionLevels;
		this.solveOperations = solveOperations;
		this.expansionOperations = expansionOperations;
		this.admissionOperations = admissionOperations;
		this.failures = Collections.unmodifiableMap(new EnumMap<>(failures));
	}

	/** @return versioned bounded arithmetic policy */
	public String getPolicy() {
		return "spline-structural-precision/v1";
	}

	/** @return selected arithmetic route */
	public Path getPath() {
		return path;
	}

	/** @return largest working decimal precision tried, zero for the fast path */
	public int getWorkingPrecision() {
		return precision;
	}

	/** @return retained decimal significant digits, zero for exact binary64 weights */
	public int getRetainedPrecision() {
		return retainedPrecision;
	}

	/** @return number of higher-precision levels actually tried */
	public int getPrecisionLevels() {
		return precisionLevels;
	}

	/** @return counted solve arithmetic operations, not elapsed time or bit complexity */
	public long getSolveOperations() {
		return solveOperations;
	}

	/** @return counted exact-expansion arithmetic operations */
	public long getExpansionOperations() {
		return expansionOperations;
	}

	/** @return counted exact original-equation admission arithmetic operations */
	public long getAdmissionOperations() {
		return admissionOperations;
	}

	/** @return immutable rejection counts for this construction */
	public Map<Failure, Integer> getFailures() {
		return failures;
	}
}
