/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.UnaryOperator;

import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidenceOwner2D.ReportOutcome;

/**
 * Owner of the conditioned evidence of a similarity image of a locus (ADR 0042; contract
 * {@code post-r1-conditioned-use.md}, section 8.7).
 *
 * <p>The image inherits the evidence of its immediate source cell by cell and states it
 * against the policy in its own world. A band or an unexplored cell of the source is
 * inherited with its kind whatever the ratio. A span is inherited unchanged when the ratio
 * is not above 1; above 1 it is checked again with its lengths in the world of the image,
 * and a span that fails is classified anew by the ordinary rule on the image function.
 * Nothing is merged or coarsened.</p>
 *
 * <p>The content identity is {@code (traced function of the image, policy version, content
 * identity of the source evidence, contradiction set of the image)}: a function of the
 * current state, never of the order in which the ratio took its values. A contradiction of
 * H1, H2 or H3 is a contradiction of the source cell and is reported to the producer of the
 * source; a contradiction of a length criterion in the world of the image belongs to the
 * contradiction set of the image.</p>
 */
public final class LocusConditionedImageOwner2D {
	/** The immediate source of the image, as its producer publishes it. */
	public interface Source {
		/** @return published conditioned evidence of the source, or {@code null} */
		LocusConditionedEvidence2D evidence();

		/**
		 * @param parameter canonical parameter reported to the producer of the source
		 * @return what the producer of the source decided, or {@code null}
		 */
		ReportOutcome report(double parameter);

		/** @return identity of the source locus and branch, for the record of the image */
		String identity();
	}

	/** Bound of the rounds in which nodes added by the image are reported to the source. */
	private static final int REPORT_ROUNDS = 8;

	private final LocusConditionedTraversalPolicy2D policy;
	private final TreeSet<Double> contradictions = new TreeSet<>();
	private final LocusConditionedTraversal2D.Nodes nodes =
			new LocusConditionedTraversal2D.Nodes();
	private LocusConditionedEvidence2D published;
	private String sourceContent;
	private long publicationCounter;
	private boolean sourceConfirmed;

	/**
	 * @param policy versioned policy, with its limits in the world of the image
	 */
	public LocusConditionedImageOwner2D(LocusConditionedTraversalPolicy2D policy) {
		this.policy = Objects.requireNonNull(policy);
	}

	/** @return published content of the image, or {@code null} before any */
	public synchronized LocusConditionedEvidence2D getPublished() {
		return published;
	}

	/** @return counter of the publications of this owner; never an identity */
	public synchronized long getPublicationCounter() {
		return publicationCounter;
	}

	/**
	 * @return whether the producer of the source confirmed a report of this owner since the
	 *         last call; the caller then lets the dependents of the source follow
	 */
	public synchronized boolean takeSourceConfirmed() {
		boolean confirmed = sourceConfirmed;
		sourceConfirmed = false;
		return confirmed;
	}

	/**
	 * The content of the image for the current content of its source.
	 *
	 * @param revision revision of the definition of the image
	 * @param functionIdentity identity of the traced function of the image, the similarity
	 *        included; equal states give equal identities
	 * @param ratio absolute ratio of the similarity
	 * @param source immediate source
	 * @param imageSampler semantic evaluator of the image
	 * @param imageOfNodes image of the node points of a span of the source
	 * @return published content, or {@code null} when the source publishes none
	 */
	public synchronized LocusConditionedEvidence2D get(long revision,
			String functionIdentity, double ratio, Source source,
			LocusConditionedSampler2D imageSampler, UnaryOperator<double[]> imageOfNodes) {
		LocusConditionedEvidence2D inherited = source.evidence();
		if (inherited == null) {
			published = null;
			sourceContent = null;
			return null;
		}
		if (published != null && inherited.getContentIdentity().equals(sourceContent)) {
			return published;
		}
		LocusConditionedEvidence2D candidate = null;
		for (int round = 0; round < REPORT_ROUNDS; round++) {
			sourceContent = inherited.getContentIdentity();
			candidate = LocusConditionedTraversal2D.inherit(inherited, revision, ratio,
					policy, contradictions, imageSampler, nodes, imageOfNodes)
					.asImage(functionIdentity, List.of(source.identity() + "@"
							+ sourceContent));
			if (!reportAddedNodes(candidate, inherited, source)) {
				break;
			}
			// The source published another content: the image inherits that one.
			inherited = source.evidence();
			if (inherited == null) {
				published = null;
				sourceContent = null;
				return null;
			}
		}
		published = candidate;
		publicationCounter++;
		return published;
	}

	/**
	 * A node that the image added inside a span of its source and whose evaluation is not
	 * valid, or does not keep the selection, contradicts H1 to H3 of that span: it is
	 * reported to the producer of the source, which decides with its own evaluator.
	 *
	 * @return whether the producer of the source confirmed a report
	 */
	private boolean reportAddedNodes(LocusConditionedEvidence2D candidate,
			LocusConditionedEvidence2D inherited, Source source) {
		boolean confirmed = false;
		for (LocusConditionedCell2D cell : candidate.getCells()) {
			LocusConditionedCellKind2D kind = cell.getKind();
			if (kind.isSpan() || kind == LocusConditionedCellKind2D.DISCONTINUITY_BAND
					|| kind == LocusConditionedCellKind2D.UNEXPLORED) {
				continue;
			}
			double middle = 0.5 * (cell.getLower() + cell.getUpper());
			if (inherited.bandAt(middle) != null || inherited.regionOf(middle) == null) {
				// Inherited with its kind: nothing that the image added.
				continue;
			}
			for (double parameter : new double[] {cell.getLower(), middle,
					cell.getUpper()}) {
				confirmed |= source.report(parameter) == ReportOutcome.CONFIRMED;
			}
		}
		sourceConfirmed |= confirmed;
		return confirmed;
	}

	/**
	 * Receives the report of a parameter of the image. A contradiction of H1 to H3 is one
	 * of the source cell: the producer of the source decides it first. Otherwise the owner
	 * decides, with the evaluator of the image, whether the resolution of the span that
	 * contains the parameter is contradicted in the world of the image.
	 *
	 * @return what was decided
	 */
	public synchronized ReportOutcome report(double parameter, Source source,
			LocusConditionedSampler2D imageSampler) {
		if (published == null) {
			return null;
		}
		if (!(parameter >= published.getDomainLower()
				&& parameter <= published.getDomainUpper())) {
			return ReportOutcome.NOT_CONFIRMED;
		}
		Double canonical = LocusConditionedTraversal2D.canonicalParameter(parameter,
				published.getDomainLower(), published.getDomainUpper());
		if (contradictions.contains(canonical)) {
			return ReportOutcome.ALREADY_KNOWN;
		}
		ReportOutcome upstream = source.report(canonical);
		if (upstream == ReportOutcome.CONFIRMED) {
			sourceConfirmed = true;
			// The next request inherits the new content of the source.
			sourceContent = null;
			return ReportOutcome.CONFIRMED;
		}
		LocusConditionedSample2D own = Objects.requireNonNull(imageSampler.sample(canonical));
		if (!own.isValid()) {
			return upstream == ReportOutcome.ALREADY_KNOWN ? upstream
					: ReportOutcome.NOT_CONFIRMED;
		}
		for (LocusConditionedCell2D cell : published.cellsContaining(canonical)) {
			if (LocusConditionedTraversal2D.exceedsResolution(cell, own.getX(), own.getY(),
					policy)) {
				contradictions.add(canonical);
				sourceContent = null;
				return ReportOutcome.CONFIRMED;
			}
		}
		return upstream == ReportOutcome.ALREADY_KNOWN ? upstream
				: ReportOutcome.NOT_CONFIRMED;
	}
}
