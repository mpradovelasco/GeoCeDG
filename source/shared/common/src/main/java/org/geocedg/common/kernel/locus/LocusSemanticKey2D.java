/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

/** Full scoped-memoization address for semantic evaluation. */
public final class LocusSemanticKey2D {
	private final String locusIdentity;
	private final long semanticRevision;
	private final String branchKey;
	private final long canonicalParameterBits;

	/** Creates a key from provider-canonical semantic parameter bits. */
	public LocusSemanticKey2D(String locusIdentity, long semanticRevision,
			String branchKey, double canonicalParameter) {
		if (locusIdentity == null || locusIdentity.trim().isEmpty()
				|| branchKey == null || branchKey.trim().isEmpty()
				|| semanticRevision < 1 || !Double.isFinite(canonicalParameter)) {
			throw new IllegalArgumentException(
					"A semantic key needs finite canonical data and stable identity");
		}
		this.locusIdentity = locusIdentity;
		this.semanticRevision = semanticRevision;
		this.branchKey = branchKey;
		double normalized = canonicalParameter == 0 ? 0 : canonicalParameter;
		this.canonicalParameterBits = Double.doubleToLongBits(normalized);
	}

	public String getLocusIdentity() {
		return locusIdentity;
	}

	public long getSemanticRevision() {
		return semanticRevision;
	}

	public String getBranchKey() {
		return branchKey;
	}

	public double getCanonicalParameter() {
		return Double.longBitsToDouble(canonicalParameterBits);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusSemanticKey2D)) {
			return false;
		}
		LocusSemanticKey2D key = (LocusSemanticKey2D) other;
		return semanticRevision == key.semanticRevision
				&& canonicalParameterBits == key.canonicalParameterBits
				&& locusIdentity.equals(key.locusIdentity)
				&& branchKey.equals(key.branchKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(locusIdentity, semanticRevision, branchKey,
				canonicalParameterBits);
	}

	@Override
	public String toString() {
		return locusIdentity + "@" + semanticRevision + ":" + branchKey + ":"
				+ Double.toHexString(getCanonicalParameter());
	}
}
