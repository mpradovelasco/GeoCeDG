/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Optional;
import java.util.OptionalInt;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;

/**
 * Intrinsic current-snapshot selector for one public transverse root.
 *
 * <p>The base selector contains stable semantic component lineage and the typed
 * oriented contact germ established by the current kernel snapshot. When that
 * base selector is repeated, a versioned extension may also contain the root's
 * intrinsic rank in the current verified collision set, ordered by the
 * provider-declared semantic orientation. The rank is framed together with the
 * collision-set cardinality, orientation and periodic-domain kind; the integer
 * alone is never identity.</p>
 *
 * <p>No selector contains a coordinate, raw parameter, solver enumeration
 * order, screen state or prior-root position. The enclosing ledger material
 * separately binds source-pair, construction, topology and provider/target
 * contracts.</p>
 */
public final class IntersectionRootDeterministicSelector2D
		implements Comparable<IntersectionRootDeterministicSelector2D> {
	private static final String BASE_PREFIX =
			"g9u0-r4/deterministic-current-root/v1/";
	private static final String PHASE_PREFIX =
			"g9u0-r4/deterministic-current-root/v2/";
	private static final String NON_PERIODIC_PHASE =
			"ORIENTED_COMPONENT";
	private static final String PERIODIC_PHASE =
			"PERIODIC_FUNDAMENTAL_INTERVAL";
	private final String externalForm;
	private final String componentLineage;
	private final String currentRootGerm;
	private final Phase phase;

	private IntersectionRootDeterministicSelector2D(String externalForm,
			String componentLineage, String currentRootGerm, Phase phase) {
		this.externalForm = requireText(externalForm, "Root selector");
		this.componentLineage = requireText(componentLineage,
				"Component lineage");
		this.currentRootGerm = requireText(currentRootGerm,
				"Current root germ");
		this.phase = phase;
	}

	/**
	 * Builds the selector from exact current semantic evidence.
	 *
	 * @return deterministic selector independent of evaluation history
	 */
	public static IntersectionRootDeterministicSelector2D of(
			String componentLineage, String currentRootGerm) {
		String component = requireText(componentLineage, "Component lineage");
		String germ = requireText(currentRootGerm, "Current root germ");
		if (!PublicTargetIntersectionCapability2D
				.isCurrentPublicRootGermForComponent(germ, component)) {
			throw new IllegalArgumentException(
					"Selector requires a typed public transverse root germ");
		}
		return new IntersectionRootDeterministicSelector2D(BASE_PREFIX
				+ framed(component) + framed(germ), component, germ, null);
	}

	/**
	 * Builds an enriched selector for one repeated current base selector.
	 *
	 * <p>The rank is induced by the declared oriented semantic domain after the
	 * caller has canonicalized and ordered the complete current verified
	 * collision group. It is not the root's position in a solver result list.</p>
	 *
	 * @return versioned intrinsic phase selector
	 */
	public static IntersectionRootDeterministicSelector2D ofIntrinsicPhase(
			String componentLineage, String currentRootGerm,
			Orientation orientation, boolean periodic,
			int collisionCardinality, int intrinsicRank) {
		String component = requireText(componentLineage, "Component lineage");
		String germ = requireText(currentRootGerm, "Current root germ");
		if (!PublicTargetIntersectionCapability2D
				.isCurrentPublicRootGermForComponent(germ, component)) {
			throw new IllegalArgumentException(
					"Selector requires a typed public transverse root germ");
		}
		java.util.Objects.requireNonNull(orientation);
		if (collisionCardinality < 2 || intrinsicRank < 0
				|| intrinsicRank >= collisionCardinality) {
			throw new IllegalArgumentException(
					"Intrinsic phase rank is outside its collision set");
		}
		Phase phase = new Phase(orientation, periodic, collisionCardinality,
				intrinsicRank);
		String domainKind = periodic ? PERIODIC_PHASE : NON_PERIODIC_PHASE;
		String external = PHASE_PREFIX + framed(component) + framed(germ)
				+ framed(orientation.name()) + framed(domainKind)
				+ framed(Integer.toString(collisionCardinality))
				+ framed(Integer.toString(intrinsicRank));
		return new IntersectionRootDeterministicSelector2D(external, component,
				germ, phase);
	}

	/** Restores one canonically framed selector from durable ledger state. */
	static IntersectionRootDeterministicSelector2D parse(String externalForm) {
		String value = requireText(externalForm, "Root selector");
		if (value.startsWith(BASE_PREFIX)) {
			Frame component = frame(value, BASE_PREFIX.length());
			Frame germ = frame(value, component.nextOffset);
			if (germ.nextOffset != value.length()) {
				throw new IllegalArgumentException(
						"Deterministic root selector has trailing material");
			}
			IntersectionRootDeterministicSelector2D parsed = of(component.value,
					germ.value);
			if (!parsed.externalForm.equals(value)) {
				throw new IllegalArgumentException(
						"Deterministic root selector is not canonical");
			}
			return parsed;
		}
		if (!value.startsWith(PHASE_PREFIX)) {
			throw new IllegalArgumentException(
					"Unsupported deterministic root selector");
		}
		Frame component = frame(value, PHASE_PREFIX.length());
		Frame germ = frame(value, component.nextOffset);
		Frame orientation = frame(value, germ.nextOffset);
		Frame domainKind = frame(value, orientation.nextOffset);
		Frame cardinality = frame(value, domainKind.nextOffset);
		Frame rank = frame(value, cardinality.nextOffset);
		if (rank.nextOffset != value.length()) {
			throw new IllegalArgumentException(
					"Deterministic root selector has trailing material");
		}
		Orientation parsedOrientation;
		try {
			parsedOrientation = Orientation.valueOf(orientation.value);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(
					"Deterministic root selector orientation is invalid",
					exception);
		}
		boolean periodic;
		if (PERIODIC_PHASE.equals(domainKind.value)) {
			periodic = true;
		} else if (NON_PERIODIC_PHASE.equals(domainKind.value)) {
			periodic = false;
		} else {
			throw new IllegalArgumentException(
					"Deterministic root selector domain kind is invalid");
		}
		IntersectionRootDeterministicSelector2D parsed = ofIntrinsicPhase(
				component.value, germ.value, parsedOrientation, periodic,
				parseCanonicalInt(cardinality.value, true),
				parseCanonicalInt(rank.value, false));
		if (!parsed.externalForm.equals(value)) {
			throw new IllegalArgumentException(
					"Deterministic root selector is not canonical");
		}
		return parsed;
	}

	/** @return exact stable semantic component lineage */
	public String getComponentLineage() {
		return componentLineage;
	}

	/** @return typed current transverse-root germ */
	public String getCurrentRootGerm() {
		return currentRootGerm;
	}

	/** @return whether this selector needed intrinsic phase disambiguation */
	public boolean hasIntrinsicPhase() {
		return phase != null;
	}

	/** @return intrinsic rank, or empty for a unique base selector */
	public OptionalInt getIntrinsicPhaseRank() {
		return phase == null ? OptionalInt.empty()
				: OptionalInt.of(phase.rank);
	}

	/** @return verified collision-set cardinality, if phase-ranked */
	public OptionalInt getCollisionCardinality() {
		return phase == null ? OptionalInt.empty()
				: OptionalInt.of(phase.cardinality);
	}

	/** @return semantic orientation framing the phase rank */
	public Optional<Orientation> getPhaseOrientation() {
		return phase == null ? Optional.empty()
				: Optional.of(phase.orientation);
	}

	/** @return whether the rank is anchored in a periodic fundamental interval */
	public boolean isPeriodicPhase() {
		return phase != null && phase.periodic;
	}

	/** @return canonical durable selector encoding */
	public String toExternalForm() {
		return externalForm;
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(IntersectionRootDeterministicSelector2D other) {
		return externalForm.compareTo(
				java.util.Objects.requireNonNull(other).externalForm);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object other) {
		return other instanceof IntersectionRootDeterministicSelector2D
				&& externalForm.equals(
						((IntersectionRootDeterministicSelector2D) other).externalForm);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return externalForm.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return externalForm;
	}

	private static String framed(String value) {
		return value.length() + ":" + value;
	}

	private static Frame frame(String encoded, int offset) {
		int separator = encoded.indexOf(':', offset);
		if (separator <= offset) {
			throw new IllegalArgumentException(
					"Deterministic root selector frame is missing");
		}
		String lengthText = encoded.substring(offset, separator);
		int length;
		try {
			length = Integer.parseInt(lengthText);
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException(
					"Deterministic root selector frame length is invalid",
					exception);
		}
		if (length <= 0 || !Integer.toString(length).equals(lengthText)) {
			throw new IllegalArgumentException(
					"Deterministic root selector frame length is not canonical");
		}
		int start = separator + 1;
		int end;
		try {
			end = Math.addExact(start, length);
		} catch (ArithmeticException exception) {
			throw new IllegalArgumentException(
					"Deterministic root selector frame is too large", exception);
		}
		if (end > encoded.length()) {
			throw new IllegalArgumentException(
					"Deterministic root selector frame is truncated");
		}
		return new Frame(encoded.substring(start, end), end);
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}

	private static int parseCanonicalInt(String value, boolean cardinality) {
		int parsed;
		try {
			parsed = Integer.parseInt(value);
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException(
					"Deterministic root selector integer is invalid", exception);
		}
		if (!Integer.toString(parsed).equals(value)
				|| (cardinality ? parsed < 2 : parsed < 0)) {
			throw new IllegalArgumentException(
					"Deterministic root selector integer is not canonical");
		}
		return parsed;
	}

	private static final class Phase {
		private final Orientation orientation;
		private final boolean periodic;
		private final int cardinality;
		private final int rank;

		private Phase(Orientation orientation, boolean periodic,
				int cardinality, int rank) {
			this.orientation = orientation;
			this.periodic = periodic;
			this.cardinality = cardinality;
			this.rank = rank;
		}
	}

	private static final class Frame {
		private final String value;
		private final int nextOffset;

		private Frame(String value, int nextOffset) {
			this.value = value;
			this.nextOffset = nextOffset;
		}
	}
}
