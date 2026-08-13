/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/** Complete-locus length query with no endpoint, direction or traversal. */
public final class TotalLocusMetricQuery implements LocusMetricQuery2D {
	private final String locusIdentity;
	private final long semanticRevision;
	private final LocusMetricPolicy2D policy;

	/** Creates a complete-locus query for one coherent revision. */
	public TotalLocusMetricQuery(String locusIdentity, long semanticRevision,
			LocusMetricPolicy2D policy) {
		if (locusIdentity == null || locusIdentity.trim().isEmpty()
				|| semanticRevision < 1) {
			throw new IllegalArgumentException(
					"Total query needs identity and positive revision");
		}
		this.locusIdentity = locusIdentity;
		this.semanticRevision = semanticRevision;
		this.policy = Objects.requireNonNull(policy);
	}

	@Override
	public String getLocusIdentity() {
		return locusIdentity;
	}

	@Override
	public long getSemanticRevision() {
		return semanticRevision;
	}

	@Override
	public LocusMetricPolicy2D getPolicy() {
		return policy;
	}
}
