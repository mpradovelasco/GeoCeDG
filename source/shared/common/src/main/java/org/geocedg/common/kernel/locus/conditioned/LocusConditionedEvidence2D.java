/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;

/**
 * Conditioned evidence of one branch for one revision and one policy version (candidate
 * contract {@code post-r1-conditioned-use.md}, sections 3 and 7): an immutable partition of
 * the declared driver domain into conditioned spans and typed bands.
 *
 * <p>It is of nature {@link LocusEvidenceNature2D#NUMERICAL_UNDER_HYPOTHESES} and certifies
 * nothing. It is derived per revision, never persisted, never part of the semantic snapshot
 * and never identity material. Its content is a function of the traced function, the policy
 * and the contradiction set, and of nothing else; the content identity states exactly
 * that. A publication counter is another attribute, kept by the owner, and is never part of
 * this value.</p>
 */
public final class LocusConditionedEvidence2D {
	private static final String IDENTITY_KIND = "conditioned-evidence/v1";

	private final String branchKey;
	private final long revision;
	private final String policyVersion;
	private final double domainLower;
	private final double domainUpper;
	private final List<Double> contradictions;
	private final List<LocusConditionedCell2D> cells;
	private final int work;
	private final boolean seamNodesAgree;
	private final List<String> inheritedSources;
	private final String functionIdentity;
	private final String contentIdentity;

	LocusConditionedEvidence2D(String branchKey, long revision, String policyVersion,
			double domainLower, double domainUpper, List<Double> contradictions,
			List<LocusConditionedCell2D> cells, int work, boolean seamNodesAgree) {
		this(branchKey, revision, policyVersion, domainLower, domainUpper, contradictions,
				cells, work, seamNodesAgree, Collections.emptyList());
	}

	private LocusConditionedEvidence2D(String branchKey, long revision, String policyVersion,
			double domainLower, double domainUpper, List<Double> contradictions,
			List<LocusConditionedCell2D> cells, int work, boolean seamNodesAgree,
			List<String> inheritedSources) {
		this(branchKey, revision, policyVersion, domainLower, domainUpper, contradictions,
				cells, work, seamNodesAgree, inheritedSources, null);
	}

	private LocusConditionedEvidence2D(String branchKey, long revision, String policyVersion,
			double domainLower, double domainUpper, List<Double> contradictions,
			List<LocusConditionedCell2D> cells, int work, boolean seamNodesAgree,
			List<String> inheritedSources, String functionIdentity) {
		this.functionIdentity = functionIdentity;
		this.inheritedSources = Collections.unmodifiableList(
				new ArrayList<>(inheritedSources));
		this.branchKey = branchKey;
		this.revision = revision;
		this.policyVersion = policyVersion;
		this.domainLower = domainLower;
		this.domainUpper = domainUpper;
		this.contradictions = Collections.unmodifiableList(new ArrayList<>(contradictions));
		this.cells = Collections.unmodifiableList(new ArrayList<>(cells));
		this.work = work;
		this.seamNodesAgree = seamNodesAgree;
		// The traced function of a producer is identified by its revision; that of a
		// similarity image by the state of its similarity, so that equal states give equal
		// contents whatever the order in which the ratio took its values (section 8.7).
		StringBuilder identity = new StringBuilder(IDENTITY_KIND).append('|')
				.append(branchKey).append('|').append(functionIdentity == null
						? String.valueOf(revision) : "image=" + functionIdentity).append('|')
				.append(policyVersion).append('|');
		for (int i = 0; i < this.contradictions.size(); i++) {
			identity.append(i == 0 ? "" : ",").append(Long.toHexString(
					Double.doubleToLongBits(this.contradictions.get(i))));
		}
		for (String source : this.inheritedSources) {
			identity.append("|inherits=").append(source);
		}
		this.contentIdentity = identity.toString();
	}

	/**
	 * The same content with the record of the conditioned sources that the traced function
	 * reads (contract {@code post-r1-conditioned-use.md}, section 8.8): every span inherits
	 * the hypotheses of those contents, and the content identity names them, so that a
	 * result that read this content is not served after one of them changes.
	 *
	 * @param sources the content identities of the conditioned evidence of each source
	 * @return content that records the sources
	 */
	public LocusConditionedEvidence2D withInheritedSources(List<String> sources) {
		return sources.isEmpty() && inheritedSources.isEmpty() ? this
				: new LocusConditionedEvidence2D(branchKey, revision, policyVersion,
						domainLower, domainUpper, contradictions, cells, work, seamNodesAgree,
						sources, functionIdentity);
	}

	/**
	 * The same content as the evidence of a similarity image (section 8.7, point 5): its
	 * content identity is {@code (traced function of the image, policy version, content
	 * identity of the source evidence, contradiction set of the image)}.
	 *
	 * @param imageFunction identity of the traced function of the image, the similarity
	 *        included
	 * @param sources the content identity of the evidence of the immediate source
	 * @return content of the image
	 */
	LocusConditionedEvidence2D asImage(String imageFunction, List<String> sources) {
		return new LocusConditionedEvidence2D(branchKey, revision, policyVersion,
				domainLower, domainUpper, contradictions, cells, work, seamNodesAgree,
				sources, java.util.Objects.requireNonNull(imageFunction));
	}

	/**
	 * @return the content identities of the conditioned sources whose hypotheses every span
	 *         of this content inherits; empty for a function that reads none
	 */
	public List<String> getInheritedSources() {
		return inheritedSources;
	}

	/** @return branch that the evidence describes */
	public String getBranchKey() {
		return branchKey;
	}

	/** @return revision of the definition that the evidence was derived from */
	public long getRevision() {
		return revision;
	}

	/** @return version of the traversal policy */
	public String getPolicyVersion() {
		return policyVersion;
	}

	/** @return lower end of the declared driver domain */
	public double getDomainLower() {
		return domainLower;
	}

	/** @return upper end of the declared driver domain */
	public double getDomainUpper() {
		return domainUpper;
	}

	/**
	 * @return canonical parameters at which the producer confirmed a contradiction, in
	 *         ascending order: a set, with no order of discovery and no multiplicity
	 */
	public List<Double> getContradictions() {
		return contradictions;
	}

	/** @return cells of the partition, in ascending order */
	public List<LocusConditionedCell2D> getCells() {
		return cells;
	}

	/** @return distinct nodes that the traversal read, charged to the work limit */
	public int getWork() {
		return work;
	}

	/**
	 * @return canonical content identity: revision, policy version and contradiction set.
	 *         Two evidences with the same content identity have the same cells, kinds and
	 *         classes
	 */
	public String getContentIdentity() {
		return contentIdentity;
	}

	/** @return {@link LocusEvidenceNature2D#NUMERICAL_UNDER_HYPOTHESES}: never certifies */
	public LocusEvidenceNature2D getNature() {
		return LocusEvidenceNature2D.NUMERICAL_UNDER_HYPOTHESES;
	}

	/**
	 * @return whether the nodes at the two ends of the declared domain are valid, have the
	 *         same established selections and coincide within the chord-deviation limit. On
	 *         a periodic provider a single region that covers the whole period with this
	 *         agreement is a closed-conditioned region (contract section 8.3); it proves no
	 *         closure
	 */
	public boolean seamNodesAgree() {
		return seamNodesAgree;
	}

	/**
	 * @param parameter canonical parameter
	 * @return the region that contains the parameter with no band and no unexplored cell
	 *         at it, or {@code null}: a parameter at an end shared with a band is in none
	 */
	public LocusInterval2D regionOf(double parameter) {
		if (!inOneRegion(parameter, parameter)) {
			return null;
		}
		for (LocusInterval2D region : getRegions()) {
			if (region.getLower() <= parameter && parameter <= region.getUpper()) {
				return region;
			}
		}
		return null;
	}

	/**
	 * @param parameter canonical parameter
	 * @return the first cell at the parameter that is not a span, or {@code null}
	 */
	public LocusConditionedCell2D bandAt(double parameter) {
		for (LocusConditionedCell2D cell : cellsContaining(parameter)) {
			if (!cell.getKind().isSpan()) {
				return cell;
			}
		}
		return null;
	}

	/**
	 * @param parameter canonical parameter
	 * @return the cells whose closed range contains the parameter: one, or two at a shared
	 *         end
	 */
	public List<LocusConditionedCell2D> cellsContaining(double parameter) {
		return cellsBetween(parameter, parameter);
	}

	/**
	 * @param first one canonical parameter
	 * @param second another canonical parameter
	 * @return every cell that meets the closed range between the two parameters
	 */
	public List<LocusConditionedCell2D> cellsBetween(double first, double second) {
		double low = Math.min(first, second);
		double high = Math.max(first, second);
		List<LocusConditionedCell2D> found = new ArrayList<>();
		for (LocusConditionedCell2D cell : cells) {
			if (cell.getUpper() >= low && cell.getLower() <= high) {
				found.add(cell);
			}
		}
		return found;
	}

	/**
	 * Decides whether two parameters lie in one conditioned region: every cell from one to
	 * the other is a span. A parameter at an end shared with a band is not inside a region.
	 *
	 * @param first one canonical parameter
	 * @param second another canonical parameter
	 * @return whether no band and no unexplored cell meets the range between them
	 */
	public boolean inOneRegion(double first, double second) {
		List<LocusConditionedCell2D> between = cellsBetween(first, second);
		if (between.isEmpty()) {
			return false;
		}
		LocusConditionedCell2D previous = null;
		for (LocusConditionedCell2D cell : between) {
			// A certified interval between two cells is not part of a region: the cells of
			// one region are adjacent.
			if (!cell.getKind().isSpan() || (previous != null
					&& previous.getUpperIndex() != cell.getLowerIndex())) {
				return false;
			}
			previous = cell;
		}
		double low = Math.min(first, second);
		double high = Math.max(first, second);
		return between.get(0).getLower() <= low
				&& high <= between.get(between.size() - 1).getUpper();
	}

	/**
	 * @return conditioned regions: maximal runs of adjacent spans. A region is not a
	 *         component: it has no lineage, no ordinal identity and no persisted form
	 */
	public List<LocusInterval2D> getRegions() {
		List<LocusInterval2D> regions = new ArrayList<>();
		LocusConditionedCell2D first = null;
		LocusConditionedCell2D last = null;
		for (LocusConditionedCell2D cell : cells) {
			if (cell.getKind().isSpan() && last != null
					&& last.getUpperIndex() == cell.getLowerIndex()) {
				last = cell;
				continue;
			}
			if (first != null) {
				regions.add(new LocusInterval2D(first.getLower(), last.getUpper(), true, true));
			}
			first = cell.getKind().isSpan() ? cell : null;
			last = first;
		}
		if (first != null) {
			regions.add(new LocusInterval2D(first.getLower(), last.getUpper(), true, true));
		}
		return regions;
	}

	/**
	 * @param newer content published after this one for the same revision
	 * @return the cells that the newer content withdraws: those that are not present in it
	 *         with the same ends, kind and class
	 */
	public List<LocusConditionedCell2D> withdrawnBy(LocusConditionedEvidence2D newer) {
		Set<LocusConditionedCell2D> kept = new HashSet<>(newer.cells);
		List<LocusConditionedCell2D> withdrawn = new ArrayList<>();
		for (LocusConditionedCell2D cell : cells) {
			if (!kept.contains(cell)) {
				withdrawn.add(cell);
			}
		}
		return withdrawn;
	}
}
