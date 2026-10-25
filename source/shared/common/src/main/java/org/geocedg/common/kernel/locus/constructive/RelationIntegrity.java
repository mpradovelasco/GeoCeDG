/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

/**
 * Integrity of a relation under the constructive authority ({@code relation-integrity/v1},
 * {@code locus-v2-constructive-authority.md} section 6). It is a type of its own, independent of
 * the numeric guarantee of any representation; it is revision evidence, never persisted and never
 * part of a token, a selector or the construction graph.
 */
public enum RelationIntegrity {
	/** Proved or rigorously certified for the constructive objects. */
	STRICT(3),
	/** Admitted by the versioned positive relaxed policy, without rigorous contradiction. */
	RELAXED(2),
	/** Neither proved, admitted nor contradicted. */
	NOT_ESTABLISHED(1),
	/** Rigorously refuted for the constructive objects. */
	CONTRADICTED(0);

	/** Versioned name of the integrity contract. */
	public static final String VERSION = "relation-integrity/v1";

	private final int strength;

	RelationIntegrity(int strength) {
		this.strength = strength;
	}

	/**
	 * Integrity of a derivation: that of its method bounded by the worst of its premises. A
	 * contradicted premise voids the derivation, whose result is {@code NOT_ESTABLISHED}. A
	 * refutation is rigorous only when every premise it uses is {@code STRICT}; otherwise the
	 * relation is {@code NOT_ESTABLISHED}, neither admitted nor contradicted.
	 *
	 * @param method integrity that the method gives with all premises strict
	 * @param premises integrity of every premise that the proof uses
	 * @return the propagated integrity
	 */
	public static RelationIntegrity propagate(RelationIntegrity method,
			Iterable<RelationIntegrity> premises) {
		RelationIntegrity result = method;
		for (RelationIntegrity premise : premises) {
			if (premise == CONTRADICTED) {
				return NOT_ESTABLISHED;
			}
			if (method == CONTRADICTED) {
				if (premise != STRICT) {
					return NOT_ESTABLISHED;
				}
			} else if (premise.strength < result.strength) {
				result = premise;
			}
		}
		return result;
	}

	/**
	 * @param minimum integrity a consumer requires
	 * @return whether this integrity satisfies it ({@code CONTRADICTED} never does)
	 */
	public boolean satisfies(RelationIntegrity minimum) {
		return this != CONTRADICTED && strength >= minimum.strength;
	}
}
