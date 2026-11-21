/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Record of the conditioned evidence that one result read (candidate contract
 * {@code post-r1-conditioned-use.md}, section 7, point 6): the revision, the content
 * identity and the cells it used. The result is current while every one of those cells is
 * present unchanged in the published content of the same revision. Currentness is never
 * decided by comparing publication counters, and evidence of another revision is never
 * reused.
 */
public final class LocusConditionedEvidenceUse2D {
	private final long revision;
	private final String contentIdentity;
	private final List<LocusConditionedCell2D> cells;

	/**
	 * @param evidence content that the result read
	 * @param usedCells cells of that content that the result used
	 */
	public LocusConditionedEvidenceUse2D(LocusConditionedEvidence2D evidence,
			List<LocusConditionedCell2D> usedCells) {
		if (!evidence.getCells().containsAll(usedCells)) {
			throw new IllegalArgumentException("A used cell is not a cell of the content");
		}
		this.revision = evidence.getRevision();
		this.contentIdentity = evidence.getContentIdentity();
		this.cells = Collections.unmodifiableList(new ArrayList<>(usedCells));
	}

	/** @return revision of the content that the result read */
	public long getRevision() {
		return revision;
	}

	/** @return content identity that the result read */
	public String getContentIdentity() {
		return contentIdentity;
	}

	/** @return cells that the result used */
	public List<LocusConditionedCell2D> getCells() {
		return cells;
	}

	/**
	 * @param published content published now
	 * @return whether the result may still be served: same revision and every used cell
	 *         present with the same ends, kind and class
	 */
	public boolean isCurrent(LocusConditionedEvidence2D published) {
		return published != null && published.getRevision() == revision
				&& new HashSet<>(published.getCells()).containsAll(cells);
	}
}
