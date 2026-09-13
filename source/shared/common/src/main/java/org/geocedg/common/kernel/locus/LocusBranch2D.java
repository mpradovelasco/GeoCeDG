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
	private final LocusExistenceStructure2D existenceStructure;
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
		this(branchKey, declaredDriverDomain,
				LocusExistenceStructure2D.completeCompatibility(branchKey,
						declaredDriverDomain, validDomainComponents),
				orientation, provenance, lineage, properties, quality);
	}

	/** Creates a branch with explicit existence/continuity evidence. */
	public LocusBranch2D(String branchKey, LocusInterval2D declaredDriverDomain,
			LocusExistenceStructure2D existenceStructure,
			Orientation orientation, String provenance, LocusLineage2D lineage,
			Set<BranchProperty> properties, LocusQuality2D quality) {
		if (branchKey == null || branchKey.trim().isEmpty()
				|| provenance == null || provenance.trim().isEmpty()) {
			throw new IllegalArgumentException("Stable branch key and provenance are required");
		}
		this.branchKey = branchKey;
		this.declaredDriverDomain = Objects.requireNonNull(declaredDriverDomain);
		this.existenceStructure = Objects.requireNonNull(existenceStructure);
		if (!branchKey.equals(existenceStructure.getBranchKey())
				|| !declaredDriverDomain.equals(
						existenceStructure.getCanonicalDomain())) {
			throw new IllegalArgumentException(
					"Existence evidence must belong to this branch and domain");
		}
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

	/**
	 * Compatibility view of globally complete continuous-valid components.
	 *
	 * @return complete components, or an empty list when global completeness is
	 *         not established
	 */
	public List<LocusInterval2D> getValidDomainComponents() {
		if (existenceStructure.getCompleteness()
				!= LocusExistenceStructure2D.Completeness.COMPLETE) {
			return Collections.emptyList();
		}
		return componentIntervals();
	}

	/** @return explicit revision-bound existence and continuity evidence */
	public LocusExistenceStructure2D getExistenceStructure() {
		return existenceStructure;
	}

	/**
	 * @return locally certified continuous-valid intervals, irrespective of global
	 *         completeness
	 */
	public List<LocusInterval2D> getCertifiedContinuousValidComponents() {
		return componentIntervals();
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
		for (LocusInterval2D component : componentIntervals()) {
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
				+ existenceStructure.getSemanticSignature() + "|" + orientation
				+ "|" + provenance
				+ "|" + lineage + "|" + properties + "|"
				+ quality.getConstructionFidelity() + "|"
				+ quality.getEvaluationMethod() + "|"
				+ quality.getRepresentationRole() + "|"
				+ quality.getNumericGuarantee();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusBranch2D)) {
			return false;
		}
		LocusBranch2D branch = (LocusBranch2D) other;
		return branchKey.equals(branch.branchKey)
				&& declaredDriverDomain.equals(branch.declaredDriverDomain)
				&& existenceStructure.getSemanticSignature().equals(
						branch.existenceStructure.getSemanticSignature())
				&& orientation == branch.orientation
				&& provenance.equals(branch.provenance)
				&& lineage.equals(branch.lineage)
				&& properties.equals(branch.properties)
				&& quality.equals(branch.quality);
	}

	@Override
	public int hashCode() {
		return Objects.hash(branchKey, declaredDriverDomain,
				existenceStructure.getSemanticSignature(),
				orientation, provenance, lineage, properties, quality);
	}

	@Override
	public String toString() {
		return getSemanticSignature();
	}

	LocusBranch2D bindExistenceToRevision(long revision) {
		return new LocusBranch2D(branchKey, declaredDriverDomain,
				existenceStructure.bindToRevision(revision), orientation, provenance,
				lineage, properties, quality);
	}

	private List<LocusInterval2D> componentIntervals() {
		ArrayList<LocusInterval2D> intervals = new ArrayList<>();
		for (LocusExistenceStructure2D.ContinuousComponent component
				: existenceStructure.getContinuousValidComponents()) {
			intervals.add(component.getInterval());
		}
		return Collections.unmodifiableList(intervals);
	}

	private static Set<BranchProperty> immutableProperties(
			Set<BranchProperty> properties) {
		Objects.requireNonNull(properties);
		EnumSet<BranchProperty> copy = properties.isEmpty()
				? EnumSet.noneOf(BranchProperty.class) : EnumSet.copyOf(properties);
		return Collections.unmodifiableSet(copy);
	}
}
