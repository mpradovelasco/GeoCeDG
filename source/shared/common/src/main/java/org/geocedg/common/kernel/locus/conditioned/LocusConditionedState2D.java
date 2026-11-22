/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;

/**
 * State of the conditioned evidence of a Locus V2, as the properties of the object state it
 * (ADR 0042; contract {@code post-r1-conditioned-use.md}, section 9, point 3): per branch,
 * the policy version, the conditioned regions and the bands by kind, with the hypotheses
 * that every span leaves open. It is a reading of the published content of the current
 * revision, language-neutral; the frontend puts it in ordinary words. It never states
 * certification or completeness.
 */
public final class LocusConditionedState2D {
	/** State of one branch. */
	public static final class Branch {
		private final String branchKey;
		private final String policyVersion;
		private final List<LocusInterval2D> regions;
		private final Map<LocusConditionedCellKind2D, Integer> bands;
		private final int contradictions;

		private Branch(LocusConditionedEvidence2D evidence) {
			this.branchKey = evidence.getBranchKey();
			this.policyVersion = evidence.getPolicyVersion();
			this.regions = Collections.unmodifiableList(evidence.getRegions());
			EnumMap<LocusConditionedCellKind2D, Integer> counts =
					new EnumMap<>(LocusConditionedCellKind2D.class);
			LocusConditionedCell2D previous = null;
			for (LocusConditionedCell2D cell : evidence.getCells()) {
				// Adjacent cells of one kind are one band for the reader.
				if (!cell.getKind().isSpan() && (previous == null
						|| previous.getKind() != cell.getKind()
						|| previous.getUpperIndex() != cell.getLowerIndex())) {
					counts.merge(cell.getKind(), 1, Integer::sum);
				}
				previous = cell;
			}
			this.bands = Collections.unmodifiableMap(counts);
			this.contradictions = evidence.getContradictions().size();
		}

		/** @return branch that the state describes */
		public String getBranchKey() {
			return branchKey;
		}

		/** @return version of the traversal policy of the evidence */
		public String getPolicyVersion() {
			return policyVersion;
		}

		/** @return conditioned regions, in parameter order */
		public List<LocusInterval2D> getRegions() {
			return regions;
		}

		/** @return number of bands of each kind that the evidence has */
		public Map<LocusConditionedCellKind2D, Integer> getBands() {
			return bands;
		}

		/** @return confirmed contradictions of the revision */
		public int getContradictions() {
			return contradictions;
		}
	}

	private final List<Branch> branches;

	private LocusConditionedState2D(List<Branch> branches) {
		this.branches = Collections.unmodifiableList(branches);
	}

	/**
	 * @param locus locus to describe
	 * @return state of the branches that publish conditioned evidence, or {@code null}
	 *         when none does: a certified locus has no conditioned state
	 */
	public static LocusConditionedState2D of(GeoLocusV2 locus) {
		LocusDefinition2D definition = locus == null || !locus.isDefined() ? null
				: locus.getSemanticDefinition();
		if (definition == null) {
			return null;
		}
		List<Branch> branches = new ArrayList<>();
		for (LocusBranch2D branch : definition.getBranches()) {
			LocusConditionedEvidence2D evidence = locus.getConditionedEvidence(
					branch.getBranchKey());
			if (evidence != null) {
				branches.add(new Branch(evidence));
			}
		}
		return branches.isEmpty() ? null : new LocusConditionedState2D(branches);
	}

	/** @return state of each branch with conditioned evidence */
	public List<Branch> getBranches() {
		return branches;
	}

	/** @return the hypotheses that every conditioned span leaves open */
	public List<LocusConditionedHypothesis2D> getHypotheses() {
		return List.of(LocusConditionedHypothesis2D.values());
	}

	/** @return conditioned regions of all the branches */
	public int getRegionCount() {
		int count = 0;
		for (Branch branch : branches) {
			count += branch.getRegions().size();
		}
		return count;
	}
}
