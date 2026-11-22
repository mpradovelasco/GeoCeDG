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

import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;

/** Immutable semantic branch; valid-domain components do not define identity. */
public final class LocusBranch2D {
	/**
	 * Prefix of the revision-local evidence key of the handle of a conditioned region. A
	 * handle is no component of the existence structure and has no lineage (ADR 0042;
	 * contract {@code post-r1-conditioned-use.md}, section 3.1).
	 */
	public static final String CONDITIONED_REGION_KEY = "conditioned-region@";
	/**
	 * Prefix of the frame of a root on a conditioned region: the branch, which is the finest
	 * frame of the source that does not depend on the traversal (section 6.5, point 2).
	 */
	public static final String BRANCH_FRAME_PREFIX = "conditioned-use/branch-frame/v1/";
	private final String branchKey;
	private final LocusInterval2D declaredDriverDomain;
	private final LocusExistenceStructure2D existenceStructure;
	private final Orientation orientation;
	private final String provenance;
	private final LocusLineage2D lineage;
	private final Set<BranchProperty> properties;
	private final LocusQuality2D quality;
	private final List<ContinuousComponent> searchRegions;
	private final LocusConditionedEvidence2D searchEvidence;

	/**
	 * Creates a branch with explicit existence/continuity evidence. Since PRE-G9B-CU2 step
	 * T11 every producer supplies its certificate: the hybrid certificate of a dependent
	 * producer or the explicit certificate of a static or analytic producer ({@code
	 * locus-v2-existence-components.md} section 15.7); no certificate is implied.
	 */
	public LocusBranch2D(String branchKey, LocusInterval2D declaredDriverDomain,
			LocusExistenceStructure2D existenceStructure,
			Orientation orientation, String provenance, LocusLineage2D lineage,
			Set<BranchProperty> properties, LocusQuality2D quality) {
		this(branchKey, declaredDriverDomain, existenceStructure, orientation, provenance,
				lineage, properties, quality, null);
	}

	private LocusBranch2D(String branchKey, LocusInterval2D declaredDriverDomain,
			LocusExistenceStructure2D existenceStructure,
			Orientation orientation, String provenance, LocusLineage2D lineage,
			Set<BranchProperty> properties, LocusQuality2D quality,
			LocusConditionedEvidence2D searchEvidence) {
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
		this.searchEvidence = searchEvidence;
		this.searchRegions = searchEvidence == null ? Collections.emptyList()
				: regionHandles(searchEvidence);
	}

	/**
	 * The search view of this branch for a consumer that looks for roots (ADR 0042; contract
	 * {@code post-r1-conditioned-use.md}, sections 8.5 and 8.6): the same branch, with the
	 * conditioned regions of the published content as places to search beside its certified
	 * components. The view changes no fact of the branch: its existence structure, its
	 * signature and its equality are those of the branch, and a region is a revision-local
	 * handle that enters no identity.
	 *
	 * @param evidence published conditioned evidence of this branch, or {@code null}
	 * @return the search view, or this branch when there is no region
	 */
	public LocusBranch2D withConditionedSearch(LocusConditionedEvidence2D evidence) {
		if (evidence == null || evidence.getRegions().isEmpty()
				|| !branchKey.equals(evidence.getBranchKey())) {
			return searchEvidence == null ? this : new LocusBranch2D(branchKey,
					declaredDriverDomain, existenceStructure, orientation, provenance,
					lineage, properties, quality, null);
		}
		return new LocusBranch2D(branchKey, declaredDriverDomain, existenceStructure,
				orientation, provenance, lineage, properties, quality, evidence);
	}

	/**
	 * @return the certified components of the existence structure, in its order, followed
	 *         in a search view by the handles of the conditioned regions. Without a search
	 *         view it is the list of the existence structure
	 */
	public List<ContinuousComponent> getSearchComponents() {
		if (searchRegions.isEmpty()) {
			return existenceStructure.getContinuousValidComponents();
		}
		ArrayList<ContinuousComponent> all = new ArrayList<>(
				existenceStructure.getContinuousValidComponents());
		all.addAll(searchRegions);
		return Collections.unmodifiableList(all);
	}

	/** @return the intervals of {@link #getSearchComponents()}, in the same order */
	public List<LocusInterval2D> getSearchIntervals() {
		if (searchRegions.isEmpty()) {
			return componentIntervals();
		}
		ArrayList<LocusInterval2D> intervals = new ArrayList<>(componentIntervals());
		for (ContinuousComponent region : searchRegions) {
			intervals.add(region.getInterval());
		}
		return Collections.unmodifiableList(intervals);
	}

	/** @return the conditioned evidence of a search view, or {@code null} */
	public LocusConditionedEvidence2D getSearchEvidence() {
		return searchEvidence;
	}

	/** @return whether the component is the handle of a conditioned region */
	public static boolean isConditionedRegion(ContinuousComponent component) {
		return component != null
				&& component.getEvidenceKey().startsWith(CONDITIONED_REGION_KEY);
	}

	/**
	 * Frame under which the roots of a place to search are identified: the identity key of a
	 * certified component, with its unchanged text, or the branch frame for the handle of a
	 * conditioned region, which names the branch and nothing of the region.
	 *
	 * @param component one of {@link #getSearchComponents()}
	 * @return frame of the roots on the component
	 */
	public String searchFrame(ContinuousComponent component) {
		return isConditionedRegion(component) ? branchFrame(branchKey)
				: LocusComponentLineage2D.identityKey(existenceStructure, component);
	}

	/** @return the branch frame of a branch key */
	public static String branchFrame(String branchKey) {
		return BRANCH_FRAME_PREFIX + branchKey.length() + ":" + branchKey;
	}

	/** @return whether the frame is a branch frame */
	public static boolean isBranchFrame(String frame) {
		return frame != null && frame.startsWith(BRANCH_FRAME_PREFIX);
	}

	private static List<ContinuousComponent> regionHandles(
			LocusConditionedEvidence2D evidence) {
		ArrayList<ContinuousComponent> handles = new ArrayList<>();
		for (LocusInterval2D region : evidence.getRegions()) {
			handles.add(new ContinuousComponent(new LocusInterval2D(region.getLower(),
					region.getUpper(), true, true),
					CONDITIONED_REGION_KEY + Double.toHexString(region.getLower()),
					evidence.getPolicyVersion(),
					LocusExistenceStructure2D.Maximality.LOCAL_CERTIFIED_SUBINTERVAL,
					evidence.getNature().name()));
		}
		return Collections.unmodifiableList(handles);
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
				|| end.supportsOneSidedLimit();
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
				lineage, properties, quality, searchEvidence);
	}

	private List<LocusInterval2D> componentIntervals() {
		ArrayList<LocusInterval2D> intervals = new ArrayList<>();
		for (ContinuousComponent component
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
