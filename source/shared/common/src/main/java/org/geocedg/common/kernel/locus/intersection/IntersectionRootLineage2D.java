/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;

/** Explicit continuation/topology event evidence without universal genealogy. */
public final class IntersectionRootLineage2D {
	private final LineageEventKind eventKind;
	private final List<String> candidateParentTokens;
	private final List<String> candidateChildTokens;
	private final List<String> candidateParentContinuationKeys;
	private final boolean continuationEstablished;

	/** Creates immutable event evidence. */
	public IntersectionRootLineage2D(LineageEventKind eventKind,
			List<String> candidateParentTokens,
			List<String> candidateChildTokens,
			List<String> candidateParentContinuationKeys,
			boolean continuationEstablished) {
		this.eventKind = java.util.Objects.requireNonNull(eventKind);
		this.candidateParentTokens = immutableTokens(candidateParentTokens);
		this.candidateChildTokens = immutableTokens(candidateChildTokens);
		this.candidateParentContinuationKeys =
				immutableTokens(candidateParentContinuationKeys);
		this.continuationEstablished = continuationEstablished;
	}

	public LineageEventKind getEventKind() {
		return eventKind;
	}

	public List<String> getCandidateParentTokens() {
		return candidateParentTokens;
	}

	public List<String> getCandidateChildTokens() {
		return candidateChildTokens;
	}

	public List<String> getCandidateParentContinuationKeys() {
		return candidateParentContinuationKeys;
	}

	public boolean isContinuationEstablished() {
		return continuationEstablished;
	}

	private static List<String> immutableTokens(List<String> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<String> copy = new ArrayList<>();
		for (String token : input) {
			if (token == null || token.trim().isEmpty() || copy.contains(token)) {
				throw new IllegalArgumentException(
						"Lineage tokens must be unique and nonblank");
			}
			copy.add(token);
		}
		return Collections.unmodifiableList(copy);
	}
}
