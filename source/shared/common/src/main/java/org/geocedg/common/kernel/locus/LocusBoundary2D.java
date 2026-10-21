/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Arrays;
import java.util.Objects;

/**
 * One end of a certified component ({@code locus-v2-existence-components.md} sections
 * 15.2 and 15.3): its kind, its structural source, whether the end belongs to the
 * component, and the nature of its proof. End inclusion follows the semantics of the
 * boundary predicate, never pointwise evaluation.
 *
 * <p>The exact end of a certified transition or event is in general not a binary64 value.
 * Such an end carries a certified enclosure of its parameter; {@link #isIncluded()} then
 * states the inclusion of the exact end, and the binary64 end of the component is the
 * closed inner bound of the enclosure, its safe interior (section 7: an enclosure is never
 * rounded into an invalid region).</p>
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
	private final double[] enclosure;

	/**
	 * @param kind kind of the end
	 * @param source structural source of the end
	 * @param included whether the end belongs to the component
	 * @param nature nature of the proof of the end
	 */
	public LocusBoundary2D(Kind kind, LocusBoundarySource2D source, boolean included,
			LocusEvidenceNature2D nature) {
		this(kind, source, included, nature, null);
	}

	private LocusBoundary2D(Kind kind, LocusBoundarySource2D source, boolean included,
			LocusEvidenceNature2D nature, double[] enclosure) {
		this.kind = Objects.requireNonNull(kind);
		this.source = Objects.requireNonNull(source);
		this.included = included;
		this.nature = Objects.requireNonNull(nature);
		this.enclosure = enclosure;
		if (kind == Kind.PERIODIC_CLOSURE && !included) {
			throw new IllegalArgumentException("A periodic closure belongs to its component");
		}
	}

	/**
	 * Creates the end of a component at a certified root whose parameter is enclosed.
	 *
	 * @param kind {@link Kind#CERTIFIED_TRANSITION} or {@link Kind#STRUCTURAL_EVENT}
	 * @param source structural source of the root
	 * @param included whether the exact root belongs to the component
	 * @param nature certifying nature of the proof of the root
	 * @param parameterLower lower bound of the certified enclosure of the root
	 * @param parameterUpper upper bound of the certified enclosure of the root, above the
	 *        lower bound
	 * @return enclosed end
	 */
	public static LocusBoundary2D enclosed(Kind kind, LocusBoundarySource2D source,
			boolean included, LocusEvidenceNature2D nature, double parameterLower,
			double parameterUpper) {
		if (kind != Kind.CERTIFIED_TRANSITION && kind != Kind.STRUCTURAL_EVENT) {
			throw new IllegalArgumentException("Only a certified root has an enclosure");
		}
		if (!nature.certifies()) {
			throw new IllegalArgumentException("An enclosed root needs a certifying proof");
		}
		if (!Double.isFinite(parameterLower) || !Double.isFinite(parameterUpper)
				|| !(parameterLower < parameterUpper)) {
			throw new IllegalArgumentException("Invalid enclosure of a root parameter");
		}
		return new LocusBoundary2D(kind, source, included, nature,
				new double[] {parameterLower, parameterUpper});
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

	/** @return whether the end is a certified root with an enclosed parameter */
	public boolean hasEnclosure() {
		return enclosure != null;
	}

	/** @return lower bound of the enclosure of the root, or {@code NaN} without one */
	public double getEnclosureLower() {
		return enclosure == null ? Double.NaN : enclosure[0];
	}

	/** @return upper bound of the enclosure of the root, or {@code NaN} without one */
	public double getEnclosureUpper() {
		return enclosure == null ? Double.NaN : enclosure[1];
	}

	/**
	 * @return whether the end is an enclosed certified transition of a line-circle
	 *         discriminant, where the traced point may have a square-root singularity in the
	 *         driver parameter (PRE-G9B-CU2, consumption design section 5.2): a representation
	 *         approaches it in the coordinate {@code s} with {@code t = t* + s^2}
	 */
	public boolean isSquareRootTransition() {
		return kind == Kind.CERTIFIED_TRANSITION && enclosure != null
				&& ConstructionValidityPredicate2D.DISCRIMINANT.name()
						.equals(source.getPredicate());
	}

	/**
	 * @return whether the binary64 end of the component belongs to it: the safe inner end
	 *         of an enclosed root always does; otherwise the end inclusion decides
	 */
	public boolean isBinaryEndIncluded() {
		return enclosure != null || included;
	}

	/** @return canonical text of the end */
	public String getSignature() {
		String signature = kind + ":" + source.getSignature() + ":"
				+ (included ? "closed" : "open") + ":" + nature;
		return enclosure == null ? signature : signature + ":enclosure["
				+ Double.toHexString(enclosure[0]) + "," + Double.toHexString(enclosure[1])
				+ "]";
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusBoundary2D)) {
			return false;
		}
		LocusBoundary2D boundary = (LocusBoundary2D) other;
		return kind == boundary.kind && source.equals(boundary.source)
				&& included == boundary.included && nature == boundary.nature
				&& Arrays.equals(enclosure, boundary.enclosure);
	}

	@Override
	public int hashCode() {
		return Objects.hash(kind, source, included, nature)
				+ 31 * Arrays.hashCode(enclosure);
	}

	@Override
	public String toString() {
		return getSignature();
	}
}
