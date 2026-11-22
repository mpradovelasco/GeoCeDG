/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/**
 * Typed cause of the state of a semantic point on a Locus V2 in the current revision. A point
 * whose address does not resolve is undefined with one of these causes
 * ({@code locus-v2-existence-components.md} sections 15.6 and 15.8,
 * {@code locus-v2-point-interaction.md} section 17); it is never reassociated silently.
 */
public enum LocusSemanticPointCause2D {
	/**
	 * The address resolves and binds: the point is defined. An address with a component
	 * selector resolves in a certified component; a parameter address resolves by the
	 * validity of its own evaluation (contract {@code post-r1-conditioned-use.md}, section
	 * 6.3).
	 */
	RESOLVED,
	/** The source locus, its semantic definition or identity, or the parameter is undefined. */
	INVALID_SOURCE,
	/**
	 * The branch input or its persisted address is undefined or malformed, the parameter is not
	 * finite, or the persisted address does not reproduce the numeric input.
	 */
	INVALID_ADDRESS,
	/** The principal-branch selector does not resolve the parameter. */
	PRINCIPAL_BRANCH_UNRESOLVED,
	/** The branch key names no branch of the current revision. */
	BRANCH_MISSING,
	/**
	 * A retained component selector has no demonstrated correspondence, or its corresponding
	 * components do not contain the parameter exactly once; the resolver status says which.
	 */
	SELECTOR_UNRESOLVED,
	/** The parameter lies in a point component, which a binary64 parameter never selects. */
	POINT_COMPONENT,
	/**
	 * The parameter lies in no certified component and no parameter address applies: a
	 * certified nonexistence, or a component selector that the same inputs held before.
	 */
	OUTSIDE_COMPONENTS,
	/** Several certified components contain the parameter within the domain tolerance. */
	AMBIGUOUS_COMPONENT,
	/**
	 * The same inputs now lie in a component other than the one accepted for them; the point is
	 * not retargeted.
	 */
	COMPONENT_CHANGED,
	/** The position does not bind in the revision; its typed binding carries the cause. */
	POSITION_UNBOUND,
	/** The parameter of a parameter address is outside the declared driver domain. */
	PARAMETER_OUTSIDE_DOMAIN,
	/**
	 * The evaluation at the parameter of a parameter address is invalid or failed; its
	 * typed binding carries the cause.
	 */
	EVALUATION_FAILED,
	/**
	 * A selecting dependency of the source has no established selection at the parameter of
	 * a parameter address.
	 */
	SELECTION_NOT_ESTABLISHED
}
