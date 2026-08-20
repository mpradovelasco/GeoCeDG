/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Objects;

/** Source of opaque tokens from explicit, non-coordinate semantic lineage. */
@FunctionalInterface
public interface IntersectionRootTokenSource2D {
	/** @return the next opaque token for legacy G8 callers */
	String nextToken();

	/**
	 * Returns a token scoped to explicit semantic solution lineage. Legacy G8
	 * sources deliberately ignore the additional material; public G9U0 sources
	 * use {@link #semantic(SemanticSource, RevisionLocalSource)}.
	 *
	 * @return opaque semantic token
	 */
	default String nextToken(IntersectionTokenLineage2D lineage) {
		Objects.requireNonNull(lineage);
		return nextToken();
	}

	/**
	 * Returns a token with separate, non-token continuation proof. Frozen G8
	 * sources ignore the proof; the public G9U0 ledger requires it before reuse.
	 *
	 * @return opaque semantic token
	 */
	default String nextToken(IntersectionTokenLineage2D lineage,
			IntersectionRootAddressProof2D addressProof) {
		Objects.requireNonNull(addressProof);
		return nextToken(lineage);
	}

	/**
	 * Returns a current-revision handle for a verified root whose durable semantic
	 * identity is not established. Frozen G8 sources retain their historical token
	 * behavior; public sources keep this handle outside the durable ledger.
	 *
	 * @return deterministic revision-local, non-selectable handle
	 */
	default String nextRevisionLocalHandle(IntersectionTokenLineage2D lineage,
			IntersectionRootRevisionEvidence2D revisionEvidence) {
		Objects.requireNonNull(revisionEvidence);
		return nextToken(lineage);
	}

	/**
	 * Adapts a lineage-aware public token source without changing the frozen G8
	 * functional-interface shape.
	 *
	 * @return compatible token source
	 */
	static IntersectionRootTokenSource2D semantic(SemanticSource source,
			RevisionLocalSource revisionLocalSource) {
		Objects.requireNonNull(source);
		Objects.requireNonNull(revisionLocalSource);
		return new IntersectionRootTokenSource2D() {
			@Override
			public String nextToken() {
				throw new IllegalStateException(
						"Semantic token lineage is required by this source");
			}

			@Override
			public String nextToken(IntersectionTokenLineage2D lineage) {
				throw new IllegalStateException(
						"Public semantic continuation proof is required by this source");
			}

			@Override
			public String nextToken(IntersectionTokenLineage2D lineage,
					IntersectionRootAddressProof2D addressProof) {
				return source.nextToken(Objects.requireNonNull(lineage),
						Objects.requireNonNull(addressProof));
			}

			@Override
			public String nextRevisionLocalHandle(
					IntersectionTokenLineage2D lineage,
					IntersectionRootRevisionEvidence2D revisionEvidence) {
				return revisionLocalSource.nextHandle(
						Objects.requireNonNull(lineage),
						Objects.requireNonNull(revisionEvidence));
			}
		};
	}

	/** Public token source that keeps revision proof outside token material. */
	@FunctionalInterface
	interface SemanticSource {
		/** @return next durable semantic token */
		String nextToken(IntersectionTokenLineage2D lineage,
				IntersectionRootAddressProof2D addressProof);
	}

	/** Public source of explicitly non-durable current-revision handles. */
	@FunctionalInterface
	interface RevisionLocalSource {
		/** @return next non-durable revision-local handle */
		String nextHandle(IntersectionTokenLineage2D lineage,
				IntersectionRootRevisionEvidence2D revisionEvidence);
	}
}
