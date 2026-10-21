/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Exploratory evidence of one branch ({@code locus-v2-existence-components.md} section 15.1,
 * level {@code EXPLORATORY}): sampled valid runs, validity transitions bracketed by
 * bisection and suspected jumps, with method, policy version and work.
 *
 * <p>It is a sibling of {@link LocusExistenceStructure2D}, derived per revision and policy,
 * never persisted and never part of the semantic snapshot, so it advances no revision. It is
 * never promoted to a component, a completeness claim or an identity, it has no authority
 * over the dependency graph, and it is never exported.</p>
 */
public final class LocusExploratoryEvidence2D {
	private final String branchKey;
	private final long semanticRevision;
	private final String method;
	private final String policyVersion;
	private final long work;
	private final List<LocusInterval2D> validRuns;
	private final List<LocusInterval2D> bracketedTransitions;
	private final List<LocusInterval2D> suspectedJumps;

	/**
	 * @param branchKey branch the evidence explores
	 * @param semanticRevision revision the evidence was derived from
	 * @param method exploration method
	 * @param policyVersion versioned exploration policy
	 * @param work evaluator calls spent
	 * @param validRuns parameter ranges between consecutive valid samples
	 * @param bracketedTransitions brackets of validity transitions found by bisection
	 * @param suspectedJumps brackets of suspected discontinuities with valid sides
	 */
	public LocusExploratoryEvidence2D(String branchKey, long semanticRevision,
			String method, String policyVersion, long work,
			List<LocusInterval2D> validRuns, List<LocusInterval2D> bracketedTransitions,
			List<LocusInterval2D> suspectedJumps) {
		this.branchKey = requireText(branchKey, "Branch key");
		if (semanticRevision < 0 || work < 0) {
			throw new IllegalArgumentException("Revision and work cannot be negative");
		}
		this.semanticRevision = semanticRevision;
		this.method = requireText(method, "Exploration method");
		this.policyVersion = requireText(policyVersion, "Exploration policy version");
		this.work = work;
		this.validRuns = immutable(validRuns);
		this.bracketedTransitions = immutable(bracketedTransitions);
		this.suspectedJumps = immutable(suspectedJumps);
	}

	/** @return branch the evidence explores */
	public String getBranchKey() {
		return branchKey;
	}

	/** @return revision the evidence was derived from */
	public long getSemanticRevision() {
		return semanticRevision;
	}

	/** @return exploration method */
	public String getMethod() {
		return method;
	}

	/** @return versioned exploration policy */
	public String getPolicyVersion() {
		return policyVersion;
	}

	/** @return evaluator calls spent */
	public long getWork() {
		return work;
	}

	/** @return parameter ranges between consecutive valid samples */
	public List<LocusInterval2D> getValidRuns() {
		return validRuns;
	}

	/** @return brackets of validity transitions found by bisection */
	public List<LocusInterval2D> getBracketedTransitions() {
		return bracketedTransitions;
	}

	/** @return brackets of suspected discontinuities with valid sides */
	public List<LocusInterval2D> getSuspectedJumps() {
		return suspectedJumps;
	}

	/** @return {@link LocusEvidenceNature2D#EXPLORATORY}: this evidence never certifies */
	public LocusEvidenceNature2D getNature() {
		return LocusEvidenceNature2D.EXPLORATORY;
	}

	/**
	 * @param declared components that a generator declares for its driver domain
	 * @return the same evidence without what lies outside every declared component
	 */
	public LocusExploratoryEvidence2D restrictedTo(List<LocusInterval2D> declared) {
		return new LocusExploratoryEvidence2D(branchKey, semanticRevision, method,
				policyVersion, work, inside(validRuns, declared),
				inside(bracketedTransitions, declared), inside(suspectedJumps, declared));
	}

	private static List<LocusInterval2D> inside(List<LocusInterval2D> intervals,
			List<LocusInterval2D> declared) {
		List<LocusInterval2D> kept = new ArrayList<>();
		for (LocusInterval2D interval : intervals) {
			for (LocusInterval2D component : declared) {
				if (component.contains(interval.getLower(), 0)
						&& component.contains(interval.getUpper(), 0)) {
					kept.add(interval);
					break;
				}
			}
		}
		return kept;
	}

	private static List<LocusInterval2D> immutable(List<LocusInterval2D> intervals) {
		ArrayList<LocusInterval2D> copy = new ArrayList<>(Objects.requireNonNull(intervals));
		for (LocusInterval2D interval : copy) {
			Objects.requireNonNull(interval);
		}
		return Collections.unmodifiableList(copy);
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty() || !value.equals(value.trim())) {
			throw new IllegalArgumentException(name + " must be canonical");
		}
		return value;
	}
}
