/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/**
 * Versioned policy {@code cu3-certified-admission/v1} of the existence grades, the certified
 * admission route and the conflict rule ({@code locus-v2-intersections.md} CU3.4; normalization
 * design sections 3.2 and 3.5). The G8B-R1 predicate and its thresholds of
 * {@link LocusIntersectionPolicy2D} are unchanged; this policy adds evidence beside them. Its
 * relative residual is the approved relative residual of section 8.4, a dimensionless
 * multiplier of the characteristic scale {@code L}, which never decides existence. Its limits
 * bound the work of one certificate; reaching one only leaves the certificate unestablished.
 */
public final class CertifiedAdmissionPolicy2D {
	/** Version of the policy, recorded in the evidence it produces. */
	public static final String VERSION = "cu3-certified-admission/v1";
	/**
	 * {@code tau_rel}: a transverse root with certified existence satisfies the residual
	 * condition when its normalized residual is within {@code tau_rel * L}.
	 */
	public static final double RELATIVE_RESIDUAL =
			LocusIntersectionPolicy2D.DEFAULT_RELATIVE_RESIDUAL_TOLERANCE;
	/** Boxes of a certificate: the sampling cell of the adaptive capability, then quarters. */
	static final int CELL_DIVISOR = 256;
	/** Successive quarterings of the certificate box around a triggered candidate. */
	static final int BOX_REDUCTIONS = 3;
	/** Bisection depth of a fixed-sign search inside one certificate box. */
	static final int MAXIMUM_DEPTH = 24;
	/** Box enclosures of one certificate. */
	static final int MAXIMUM_EVALUATIONS = 4_096;
	/** Bisection steps of a critical bracket or a root bracket. */
	static final int MAXIMUM_BISECTIONS = 80;
	/** Width of a converged bracket, in units in the last place of its parameter. */
	static final int BRACKET_ULPS = 4;
	/** Doublings of the bracket of a transverse root while its existence is certified. */
	static final int GRADE_EXPANSIONS = 48;

	private CertifiedAdmissionPolicy2D() {
		// Constants.
	}

	/**
	 * @param parameter a parameter
	 * @return the width of a converged bracket at the parameter
	 */
	static double bracketWidth(double parameter) {
		return BRACKET_ULPS * Math.ulp(Math.max(1, Math.abs(parameter)));
	}
}
