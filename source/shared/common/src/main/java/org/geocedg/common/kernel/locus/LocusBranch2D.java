/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;

/** Immutable semantic branch; valid-domain components do not define identity. */
public final class LocusBranch2D {
	private final String branchKey;
	private final LocusInterval2D declaredDriverDomain;
	private final List<LocusInterval2D> validDomainComponents;
	private final Orientation orientation;
	private final String provenance;
	private final LocusLineage2D lineage;
	private final Set<BranchProperty> properties;
	private final LocusQuality2D quality;

	/** Creates an immutable branch snapshot. */
	public LocusBranch2D(String branchKey, LocusInterval2D declaredDriverDomain,
			List<LocusInterval2D> validDomainComponents, Orientation orientation,
			String provenance, LocusLineage2D lineage,
			Set<BranchProperty> properties, LocusQuality2D quality) {
		if (branchKey == null || branchKey.trim().isEmpty()
				|| provenance == null || provenance.trim().isEmpty()) {
			throw new IllegalArgumentException("Stable branch key and provenance are required");
		}
		this.branchKey = branchKey;
		this.declaredDriverDomain = Objects.requireNonNull(declaredDriverDomain);
		this.validDomainComponents = immutableComponents(validDomainComponents);
		this.orientation = Objects.requireNonNull(orientation);
		this.provenance = provenance;
		this.lineage = Objects.requireNonNull(lineage);
		this.properties = immutableProperties(properties);
		this.quality = Objects.requireNonNull(quality);
	}

	public String getBranchKey() {
		return branchKey;
	}

	public LocusInterval2D getDeclaredDriverDomain() {
		return declaredDriverDomain;
	}

	public List<LocusInterval2D> getValidDomainComponents() {
		return validDomainComponents;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public String getProvenance() {
		return provenance;
	}

	public LocusLineage2D getLineage() {
		return lineage;
	}

	public Set<BranchProperty> getProperties() {
		return properties;
	}

	public LocusQuality2D getQuality() {
		return quality;
	}

	/**
	 * Tests valid-domain membership using provider-owned eps_domain.
	 *
	 * @return whether the canonical parameter belongs to a valid component
	 */
	public boolean containsValidParameter(double canonicalParameter,
			LocusDriverDomainProvider2D provider) {
		if (!provider.contains(canonicalParameter)) {
			return false;
		}
		for (LocusInterval2D component : validDomainComponents) {
			if (component.contains(canonicalParameter, provider.getDomainEpsilon())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Stable content descriptor, independent of samples and coordinates.
	 *
	 * @return deterministic semantic content signature
	 */
	public String getSemanticSignature() {
		return branchKey + "|" + declaredDriverDomain + "|"
				+ validDomainComponents + "|" + orientation + "|" + provenance
				+ "|" + lineage.getTransition() + "|" + lineage.getParentKeys()
				+ "|" + lineage.getChildKeys() + "|" + properties + "|"
				+ quality.hashCode();
	}

	private static List<LocusInterval2D> immutableComponents(
			List<LocusInterval2D> components) {
		Objects.requireNonNull(components);
		ArrayList<LocusInterval2D> copy = new ArrayList<>();
		for (LocusInterval2D component : components) {
			copy.add(Objects.requireNonNull(component));
		}
		return Collections.unmodifiableList(copy);
	}

	private static Set<BranchProperty> immutableProperties(
			Set<BranchProperty> properties) {
		Objects.requireNonNull(properties);
		EnumSet<BranchProperty> copy = properties.isEmpty()
				? EnumSet.noneOf(BranchProperty.class) : EnumSet.copyOf(properties);
		return Collections.unmodifiableSet(copy);
	}
}
