/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

/**
 * One end of a certified component ({@code locus-v2-existence-components.md} sections
 * 15.2 and 15.3): its kind, its structural source, whether the end belongs to the
 * component, and the nature of its proof. End inclusion follows the semantics of the
 * boundary predicate, never pointwise evaluation.
 */
public final class LocusBoundary2D {
	/** Kind of a component end. */
	public enum Kind {
		/** An end of the declared domain or of a provider-declared component. */
		DOMAIN_END,
		/** A certified transition: a simple root of a validity predicate. */
		CERTIFIED_TRANSITION,
		/** A structural event, such as a discontinuity with valid sides. */
		STRUCTURAL_EVENT,
		/** The edge of an {@code UNRESOLVED} band; the component is then not maximal. */
		UNRESOLVED_BAND_EDGE,
		/**
		 * The closing point of a certified closed periodic component, which is not a
		 * boundary: both ends of the full-length arc are identified there.
		 */
		PERIODIC_CLOSURE,
		/** An end recorded before PRE-G9B-CU2, without a typed kind or source. */
		UNTYPED
	}

	private final Kind kind;
	private final LocusBoundarySource2D source;
	private final boolean included;
	private final LocusEvidenceNature2D nature;

	/**
	 * @param kind kind of the end
	 * @param source structural source of the end
	 * @param included whether the end belongs to the component
	 * @param nature nature of the proof of the end
	 */
	public LocusBoundary2D(Kind kind, LocusBoundarySource2D source, boolean included,
			LocusEvidenceNature2D nature) {
		this.kind = Objects.requireNonNull(kind);
		this.source = Objects.requireNonNull(source);
		this.included = included;
		this.nature = Objects.requireNonNull(nature);
		if (kind == Kind.PERIODIC_CLOSURE && !included) {
			throw new IllegalArgumentException("A periodic closure belongs to its component");
		}
	}

	/** @return untyped end recorded by a pre-CU2 certifier */
	public static LocusBoundary2D untyped(boolean included) {
		return new LocusBoundary2D(Kind.UNTYPED, new LocusBoundarySource2D("legacy",
				"untyped", "unspecified"), included, LocusEvidenceNature2D.NONE);
	}

	/** @return kind of the end */
	public Kind getKind() {
		return kind;
	}

	/** @return structural source of the end */
	public LocusBoundarySource2D getSource() {
		return source;
	}

	/** @return whether the end belongs to the component */
	public boolean isIncluded() {
		return included;
	}

	/** @return nature of the proof of the end */
	public LocusEvidenceNature2D getNature() {
		return nature;
	}

	/** @return canonical text of the end */
	public String getSignature() {
		return kind + ":" + source.getSignature() + ":" + (included ? "closed" : "open")
				+ ":" + nature;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusBoundary2D)) {
			return false;
		}
		LocusBoundary2D boundary = (LocusBoundary2D) other;
		return kind == boundary.kind && source.equals(boundary.source)
				&& included == boundary.included && nature == boundary.nature;
	}

	@Override
	public int hashCode() {
		return Objects.hash(kind, source, included, nature);
	}

	@Override
	public String toString() {
		return getSignature();
	}
}
