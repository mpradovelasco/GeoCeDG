/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/**
 * Nature of the proof behind one existence fact ({@code locus-v2-existence-components.md}
 * section 15.1). Only exact proofs and rigorous interval arithmetic certify.
 */
public enum LocusEvidenceNature2D {
	/** Exact symbolic or algebraic proof. */
	EXACT(true),
	/** Interval arithmetic with rigorous, outward-rounded bounds. */
	RIGOROUS_INTERVAL(true),
	/** Numerical verification under stated hypotheses: an estimate under its contract. */
	NUMERICAL_UNDER_HYPOTHESES(false),
	/** Sampled or bisected evidence without authority. */
	EXPLORATORY(false),
	/** No evidence: the fact is asserted, unresolved or not established. */
	NONE(false);

	private final boolean certifying;

	LocusEvidenceNature2D(boolean certifying) {
		this.certifying = certifying;
	}

	/** @return whether a fact with this nature is certified */
	public boolean certifies() {
		return certifying;
	}
}
