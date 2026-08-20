/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Objects;

/**
 * Non-token evidence for one exact semantic root address.
 *
 * <p>The proof is compared only by the public token ledger. It never enters
 * the opaque token material. Exact equality certifies only the conservative
 * G9U0 continuation subset: the same provider and target contracts at the same
 * canonical preimage. The parameter remains revision evidence, not token
 * identity. Moving-parameter continuation requires a future typed cross-revision
 * certificate and is deliberately not inferred from component uniqueness.</p>
 */
public final class IntersectionRootAddressProof2D {
	private final String sourceProviderSignature;
	private final String targetContractSignature;
	private final long canonicalParameterBits;

	/** Captures exact, deterministic address evidence from one current query. */
	public IntersectionRootAddressProof2D(String sourceProviderSignature,
			String targetContractSignature, double canonicalParameter) {
		this(sourceProviderSignature, targetContractSignature,
				Double.doubleToLongBits(canonicalParameter));
	}

	/**
	 * Restores canonical persisted address evidence.
	 *
	 * @return restored canonical address proof
	 */
	public static IntersectionRootAddressProof2D fromBits(
			String sourceProviderSignature, String targetContractSignature,
			long canonicalParameterBits) {
		return new IntersectionRootAddressProof2D(sourceProviderSignature,
				targetContractSignature, canonicalParameterBits);
	}

	private IntersectionRootAddressProof2D(String sourceProviderSignature,
			String targetContractSignature, long canonicalParameterBits) {
		this.sourceProviderSignature = requireText(sourceProviderSignature,
				"Source-provider signature");
		this.targetContractSignature = requireText(targetContractSignature,
				"Target-contract signature");
		double parameter = Double.longBitsToDouble(canonicalParameterBits);
		if (!Double.isFinite(parameter)
				|| Double.doubleToLongBits(parameter) != canonicalParameterBits) {
			throw new IllegalArgumentException(
					"Canonical semantic parameter bits must be finite");
		}
		this.canonicalParameterBits = canonicalParameterBits;
	}

	public String getSourceProviderSignature() {
		return sourceProviderSignature;
	}

	public String getTargetContractSignature() {
		return targetContractSignature;
	}

	public long getCanonicalParameterBits() {
		return canonicalParameterBits;
	}

	/**
	 * Compares only evidence invariant under one explicitly authorized closure
	 * copy. The source-provider signature must be replaced by the copied
	 * construction's current signature and is deliberately excluded here.
	 */
	boolean sameAddressUnderAuthorizedCopy(IntersectionRootAddressProof2D other) {
		return other != null
				&& canonicalParameterBits == other.canonicalParameterBits
				&& targetContractSignature.equals(other.targetContractSignature);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof IntersectionRootAddressProof2D)) {
			return false;
		}
		IntersectionRootAddressProof2D proof =
				(IntersectionRootAddressProof2D) other;
		return canonicalParameterBits == proof.canonicalParameterBits
				&& sourceProviderSignature.equals(proof.sourceProviderSignature)
				&& targetContractSignature.equals(proof.targetContractSignature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sourceProviderSignature, targetContractSignature,
				canonicalParameterBits);
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
