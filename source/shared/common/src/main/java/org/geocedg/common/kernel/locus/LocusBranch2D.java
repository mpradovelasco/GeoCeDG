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
						declaredDriverDomain, validDomainComponents,
						properties != null && properties.contains(BranchProperty.PERIODIC)),
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
	 * Compatibility view of globally complete continuous-valid components. Since
	 * PRE-G9B-CU2 it is a legacy adapter: an arc is one lifted interval from its start,
	 * point components are never listed, and closure is decided only by
	 * {@link LocusExistenceStructure2D#isClosedPeriodic()}.
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
	 *         completeness; a legacy adapter like {@link #getValidDomainComponents()}
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
	 * Tests valid-domain membership using provider-owned eps_domain on the components of the
	 * existence structure; an arc lifted past its domain is tested on its arc coordinate
	 * (PRE-G9B-CU2). A point component is never selected by a parameter.
	 *
	 * @return whether the canonical parameter belongs to a valid component
	 */
	public boolean containsValidParameter(double canonicalParameter,
			LocusDriverDomainProvider2D provider) {
		if (!provider.contains(canonicalParameter)) {
			return false;
		}
		for (LocusExistenceStructure2D.ContinuousComponent component
				: existenceStructure.getContinuousValidComponents()) {
			if (component.contains(canonicalParameter, provider.getDomainEpsilon())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the certified component whose lifted interval is the extent, or {@code null}
	 */
	public LocusExistenceStructure2D.ContinuousComponent getComponentWithExtent(
			LocusInterval2D extent) {
		for (LocusExistenceStructure2D.ContinuousComponent component
				: existenceStructure.getContinuousValidComponents()) {
			if (component.getInterval().equals(extent)) {
				return component;
			}
		}
		return null;
	}

	/**
	 * Whether a consumer that traverses the component with this extent can evaluate both of
	 * its ends (PRE-G9B-CU2): an included binary64 end, the closing point of a certified
	 * closed periodic branch (the single closure authority, never the provider's
	 * periodicity), or an excluded structural event, whose one-sided limit the certificate
	 * establishes and {@link LocusDefinition2D#evaluateComponentEnd} evaluates.
	 *
	 * @return whether both ends of the component are evaluable
	 */
	public boolean hasEvaluableEnds(LocusInterval2D extent) {
		LocusExistenceStructure2D.ContinuousComponent component =
				getComponentWithExtent(extent);
		if (component == null) {
			return false;
		}
		return existenceStructure.isClosedPeriodic()
				|| isEvaluable(component.getLowerBoundary())
						&& isEvaluable(component.getUpperBoundary());
	}

	private static boolean isEvaluable(LocusBoundary2D end) {
		return end.isBinaryEndIncluded()
				|| end.getKind() == LocusBoundary2D.Kind.STRUCTURAL_EVENT;
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
