/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Objects;

/**
 * Typed reason why a provider could not describe a redefine proposal
 * (PRE-G9B-R3, D3-b). The registry maps the kind onto the closed assessment
 * status; the message remains an unlocalized kernel diagnostic.
 */
public final class SpatialRedefineDescriptionException
		extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

	/** Closed classification of a failed proposal description. */
	public enum Kind {
		/** A describable proposal that changes a durable-contract field. */
		DURABLE_CONTRACT_CHANGE,
		/** The semantic relation cannot be represented well enough to assess it. */
		UNDESCRIBABLE_PROPOSAL,
		/** Several semantic interpretations remain possible. */
		AMBIGUOUS
	}

	private final Kind kind;

	private SpatialRedefineDescriptionException(Kind kind, String message) {
		super(message);
		this.kind = Objects.requireNonNull(kind);
	}

	/** @return a durable-contract change such as a family, role or cardinality change */
	public static SpatialRedefineDescriptionException durableContractChange(
			String message) {
		return new SpatialRedefineDescriptionException(Kind.DURABLE_CONTRACT_CHANGE,
				message);
	}

	/** @return a proposal whose semantic relation cannot be described */
	public static SpatialRedefineDescriptionException undescribable(String message) {
		return new SpatialRedefineDescriptionException(Kind.UNDESCRIBABLE_PROPOSAL,
				message);
	}

	/** @return a genuinely ambiguous proposal */
	public static SpatialRedefineDescriptionException ambiguous(String message) {
		return new SpatialRedefineDescriptionException(Kind.AMBIGUOUS, message);
	}

	/** @return closed classification of this description failure */
	public Kind getKind() {
		return kind;
	}
}
