/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

import org.geocedg.common.kernel.locus.LocusInterval2D;

/**
 * Producer-side owner of the conditioned evidence of one branch (candidate contract
 * {@code post-r1-conditioned-use.md}, section 7). It holds the contradiction set of the
 * current revision and publishes immutable contents.
 *
 * <p>A consumer, the renderer or a later evaluation only reports a parameter. The owner
 * evaluates it with its own sampler and, when the contradiction is confirmed, adds the
 * parameter to the set and publishes the content of the new set. A report that is not
 * confirmed, or whose parameter is already in the set, changes neither the content nor the
 * counter. A new revision discards the evidence and the set of the previous one. The
 * publication counter orders publications and is never an identity.</p>
 */
public final class LocusConditionedEvidenceOwner2D {
	/** Outcome of one report. */
	public enum ReportOutcome {
		/** The owner's own evaluation is not excluded by the cell: nothing changes. */
		NOT_CONFIRMED,
		/** The parameter is already in the contradiction set: nothing changes. */
		ALREADY_KNOWN,
		/** The contradiction was confirmed and the content of the new set was published. */
		CONFIRMED
	}

	private final String branchKey;
	private final double domainLower;
	private final double domainUpper;
	private final LocusConditionedTraversalPolicy2D policy;
	private final List<LocusInterval2D> certified;
	private final TreeSet<Double> contradictions = new TreeSet<>();
	private LocusConditionedTraversal2D.Nodes nodes;
	private LocusConditionedSampler2D sampler;
	private LocusConditionedEvidence2D published;
	private long publicationCounter;

	/**
	 * @param branchKey branch that the evidence describes
	 * @param domainLower lower end of the declared driver domain
	 * @param domainUpper upper end of the declared driver domain
	 * @param policy versioned policy
	 */
	public LocusConditionedEvidenceOwner2D(String branchKey, double domainLower,
			double domainUpper, LocusConditionedTraversalPolicy2D policy) {
		this(branchKey, domainLower, domainUpper, policy, List.of());
	}

	/**
	 * @param branchKey branch that the evidence describes
	 * @param domainLower lower end of the declared driver domain
	 * @param domainUpper upper end of the declared driver domain
	 * @param policy versioned policy
	 * @param certified parameter intervals that a certificate of the revisions of this
	 *        owner covers; the evidence publishes no cell inside them
	 */
	public LocusConditionedEvidenceOwner2D(String branchKey, double domainLower,
			double domainUpper, LocusConditionedTraversalPolicy2D policy,
			List<LocusInterval2D> certified) {
		this.branchKey = Objects.requireNonNull(branchKey);
		this.domainLower = domainLower;
		this.domainUpper = domainUpper;
		this.policy = Objects.requireNonNull(policy);
		this.certified = Collections.unmodifiableList(new ArrayList<>(certified));
	}

	/** @return versioned policy of this owner */
	public LocusConditionedTraversalPolicy2D getPolicy() {
		return policy;
	}

	/**
	 * Starts a revision: the evidence, the contradiction set and the cached nodes of the
	 * previous revision are discarded, and the content of the empty set is published.
	 *
	 * @param revision revision of the definition
	 * @param revisionSampler semantic evaluator of the producer for that revision
	 * @return published content
	 */
	public synchronized LocusConditionedEvidence2D startRevision(long revision,
			LocusConditionedSampler2D revisionSampler) {
		this.sampler = Objects.requireNonNull(revisionSampler);
		this.nodes = new LocusConditionedTraversal2D.Nodes();
		this.contradictions.clear();
		this.publicationCounter = 0;
		this.published = null;
		return publish(revision);
	}

	/** @return published content of the current revision, or {@code null} before any */
	public synchronized LocusConditionedEvidence2D getPublished() {
		return published;
	}

	/**
	 * @return counter of the publications of the current revision. It orders them and wakes
	 *         the dependents; it is never compared between two histories, never enters a
	 *         result as its evidence and is never persisted
	 */
	public synchronized long getPublicationCounter() {
		return publicationCounter;
	}

	/**
	 * Receives the report of a parameter at which an observer saw an evaluation that the
	 * kind of its cell excludes. The report changes nothing by itself.
	 *
	 * @param parameter reported canonical parameter
	 * @return what the owner's own evaluation decided
	 */
	public synchronized ReportOutcome report(double parameter) {
		if (published == null) {
			throw new IllegalStateException("No revision has been started");
		}
		Double canonical = LocusConditionedTraversal2D.canonicalParameter(parameter,
				domainLower, domainUpper);
		if (contradictions.contains(canonical)) {
			return ReportOutcome.ALREADY_KNOWN;
		}
		LocusConditionedSample2D own = Objects.requireNonNull(sampler.sample(canonical));
		boolean confirmed = false;
		for (LocusConditionedCell2D cell : published.cellsContaining(canonical)) {
			confirmed |= LocusConditionedTraversal2D.contradicts(cell, own, nodes, policy);
		}
		if (!confirmed) {
			return ReportOutcome.NOT_CONFIRMED;
		}
		contradictions.add(canonical);
		publish(published.getRevision());
		return ReportOutcome.CONFIRMED;
	}

	private LocusConditionedEvidence2D publish(long revision) {
		published = LocusConditionedTraversal2D.traverse(branchKey, revision, domainLower,
				domainUpper, policy, contradictions, sampler, nodes, certified);
		publicationCounter++;
		return published;
	}
}
