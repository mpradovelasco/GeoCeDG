/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.kernel.locus.interaction;

import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;

/**
 * Work that one creation operation has already spent before a later, independent
 * permission of the same operation is queried.
 *
 * <p>The historical discovery of an operation is deterministic for its source revision and
 * request, and every permission repeats it. Its exact-key semantic values are therefore
 * replayed instead of being evaluated and charged again, while the later permission keeps
 * its own proofs and forward confirmations and can only spend what the operation has left:
 * the cumulative limits are never restarted. The replay carries no candidate, permission
 * or evidence; each permission still derives and proves its own.</p>
 */
final class LocusPointDiscoveryReplay2D {
	private final LocusEvaluationSession2D.ReplayMemo memo;
	private final long spentSemanticMisses;
	private final long spentSubdivisions;

	LocusPointDiscoveryReplay2D(LocusEvaluationSession2D.ReplayMemo memo,
			long spentSemanticMisses, long spentSubdivisions) {
		this.memo = Objects.requireNonNull(memo);
		this.spentSemanticMisses = spentSemanticMisses;
		this.spentSubdivisions = spentSubdivisions;
	}

	/** @return the semantic evaluations that this operation may still spend */
	long remainingSemanticEvaluations(LocusPointInteractionWorkBudget2D budget) {
		return budget.getMaximumSemanticEvaluations() - spentSemanticMisses;
	}

	/** @return discovery subdivisions and proof passes already spent by this operation */
	long getSpentSubdivisions() {
		return spentSubdivisions;
	}

	/** Seeds an idle session of the same operation with the already charged values. */
	void seed(LocusEvaluationSession2D session) {
		session.seedReplayMemo(memo);
	}
}
