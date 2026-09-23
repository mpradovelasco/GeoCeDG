/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.algos.AlgoLocusLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D.SeamSide;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoPoint;

/**
 * Exact resolver for a metric endpoint materialized from one Locus V2 pair root
 * (section 23) or single-source root (section 24.4).
 *
 * <p>The walk mirrors the selected-root certifier: every non-matching shape is
 * rejected without geometric search. Side selection is by exact locus identity,
 * never by caller order, coordinates, proximity, labels or solution index.</p>
 */
public final class IntersectionEndpointProvenanceResolver2D {
	private static final String OCCURRENCE_VERSION =
			"metric-endpoint/intersection-occurrence/v1";
	private static final String SINGLE_SOURCE_OCCURRENCE_VERSION =
			"metric-endpoint/single-source-intersection-occurrence/v1";

	/**
	 * Resolves only explicit exact-token pair-intersection provenance.
	 *
	 * @return zero, one, many, or unresolved current semantic addresses
	 */
	public IntersectionEndpointProvenanceResult2D resolve(GeoLocusV2 addressedSource,
			GeoPoint endpoint) {
		if (addressedSource == null || endpoint == null) {
			return IntersectionEndpointProvenanceResult2D.unresolved(0,
					"A source and endpoint are required");
		}
		long revision = Math.max(0, addressedSource.getSemanticRevision());
		AlgoElement parent = endpoint.getParentAlgorithm();
		if (!(parent instanceof AlgoLocusIntersectionPointV2)) {
			return IntersectionEndpointProvenanceResult2D.noAddress(revision,
					"The endpoint is not an exact-token intersection point");
		}
		AlgoLocusIntersectionPointV2 consumer = (AlgoLocusIntersectionPointV2) parent;
		GeoLocusIntersectionResult rich = consumer.getRichInput();
		if (rich == null
				|| !(rich.getParentAlgorithm() instanceof AlgoLocusLocusIntersectionV2)) {
			return IntersectionEndpointProvenanceResult2D.noAddress(revision,
					"The intersection carries no per-side locus evidence");
		}
		LocusDefinition2D definition = addressedSource.getSemanticDefinition();
		if (!addressedSource.isDefined() || definition == null) {
			return IntersectionEndpointProvenanceResult2D.unresolved(revision,
					"The addressed source has no current semantic definition");
		}
		String token = consumer.getEffectiveRootToken();
		if (token == null) {
			return IntersectionEndpointProvenanceResult2D.unresolved(revision,
					"The intersection point has no current effective root token");
		}
		Optional<LocusIntersectionSolution2D> solution =
				rich.findExactPointAdmissibleSolution(token);
		if (!solution.isPresent()) {
			return IntersectionEndpointProvenanceResult2D.unresolved(revision,
					"The exact root token is not current");
		}
		Optional<LocusPairIntersectionEvidence2D> evidence =
				solution.get().getPairEvidence();
		if (!evidence.isPresent()) {
			return IntersectionEndpointProvenanceResult2D.unresolved(revision,
					"The current pair root lacks per-side evidence");
		}
		SpatialIdentityRegistry registry = endpoint.getConstruction()
				.getSpatialIdentityRegistry();
		return resolveSides(evidence.get(), definition, revision,
				addressedSource.getPersistentLocusId(),
				registry.getPersistentGeoId(rich), token,
				registry.getPersistentGeoId(endpoint));
	}

	/**
	 * Applies the metric side-selection, currentness and key rules to one exact
	 * pair root. Side order comes from the evidence; selection is by identity.
	 *
	 * @return NO_ADDRESS when no side is the addressed source; otherwise final
	 */
	static IntersectionEndpointProvenanceResult2D resolveSides(
			LocusPairIntersectionEvidence2D evidence, LocusDefinition2D definition,
			long revision, PersistentGeoId sourceId, PersistentGeoId richId,
			String token, PersistentGeoId endpointId) {
		String sourceIdentity = definition.getLocusIdentity();
		LocusPairSourceRevisionEvidence2D[] sides = {
			evidence.getFirst(), evidence.getSecond()};
		String[] sideNames = {"first", "second"};
		List<Integer> matching = new ArrayList<>();
		for (int index = 0; index < sides.length; index++) {
			if (sourceIdentity.equals(sides[index].getLocusIdentity())) {
				matching.add(index);
			}
		}
		if (matching.isEmpty()) {
			return IntersectionEndpointProvenanceResult2D.noAddress(revision,
					"The addressed source is not a side of this intersection");
		}
		if (endpointId == null || richId == null || sourceId == null
				|| token == null) {
			return IntersectionEndpointProvenanceResult2D.unresolved(revision,
					"Durable source, intersection and endpoint identities are required");
		}
		List<IntersectionEndpointProvenanceResult2D.Match> matches = new ArrayList<>();
		for (int index : matching) {
			LocusPairSourceRevisionEvidence2D side = sides[index];
			if (side.getSemanticRevision() != definition.getSemanticRevision()) {
				// A stale identity match is final: it must not fall through.
				return IntersectionEndpointProvenanceResult2D.unresolved(revision,
						"The pair-root side is stale for the addressed source revision");
			}
			LocusSemanticAddress2D address = address(definition, sourceId, side);
			if (address == null) {
				return IntersectionEndpointProvenanceResult2D.unresolved(revision,
						"The pair-root side has no unique current branch component");
			}
			String occurrenceKey = OCCURRENCE_VERSION + "|addressed-source="
					+ sourceId.toExternalForm() + "|rich-result="
					+ richId.toExternalForm() + "|token=" + token + "|side="
					+ sideNames[index] + "|point=" + endpointId.toExternalForm();
			matches.add(new IntersectionEndpointProvenanceResult2D.Match(
					occurrenceKey, address));
		}
		return IntersectionEndpointProvenanceResult2D.resolved(revision, matches);
	}

	/**
	 * Resolves only explicit exact-token single-source intersection provenance
	 * ({@code locus-v2-metrics.md} section 24.4).
	 *
	 * @return zero, one, or unresolved current semantic addresses
	 */
	public IntersectionEndpointProvenanceResult2D resolveSingleSource(
			GeoLocusV2 addressedSource, GeoPoint endpoint) {
		if (addressedSource == null || endpoint == null) {
			return IntersectionEndpointProvenanceResult2D.unresolved(0,
					"A source and endpoint are required");
		}
		long revision = Math.max(0, addressedSource.getSemanticRevision());
		AlgoElement parent = endpoint.getParentAlgorithm();
		if (!(parent instanceof AlgoLocusIntersectionPointV2)) {
			return IntersectionEndpointProvenanceResult2D.noAddress(revision,
					"The endpoint is not an exact-token intersection point");
		}
		AlgoLocusIntersectionPointV2 consumer = (AlgoLocusIntersectionPointV2) parent;
		GeoLocusIntersectionResult rich = consumer.getRichInput();
		if (rich == null
				|| !(rich.getParentAlgorithm() instanceof AlgoLocusIntersectionV2)) {
			return IntersectionEndpointProvenanceResult2D.noAddress(revision,
					"The intersection is not a single-source Locus V2 intersection");
		}
		GeoLocusV2 intersected =
				((AlgoLocusIntersectionV2) rich.getParentAlgorithm()).getSource();
		if (intersected == null || !addressedSource.getLocusIdentity()
				.equals(intersected.getLocusIdentity())) {
			return IntersectionEndpointProvenanceResult2D.noAddress(revision,
					"The addressed source is not the intersected source");
		}
		LocusDefinition2D definition = addressedSource.getSemanticDefinition();
		if (!addressedSource.isDefined() || definition == null) {
			return IntersectionEndpointProvenanceResult2D.unresolved(revision,
					"The addressed source has no current semantic definition");
		}
		String token = consumer.getEffectiveRootToken();
		if (token == null) {
			return IntersectionEndpointProvenanceResult2D.unresolved(revision,
					"The intersection point has no current effective root token");
		}
		Optional<LocusIntersectionSolution2D> solution =
				rich.findExactPointAdmissibleSolution(token);
		if (!solution.isPresent() || solution.get().getPairEvidence().isPresent()) {
			return IntersectionEndpointProvenanceResult2D.unresolved(revision,
					"The exact single-source root token is not current");
		}
		SpatialIdentityRegistry registry = endpoint.getConstruction()
				.getSpatialIdentityRegistry();
		return resolveSingle(solution.get().getRevisionEvidence(), definition,
				revision, addressedSource.getPersistentLocusId(),
				registry.getPersistentGeoId(rich), token,
				registry.getPersistentGeoId(endpoint));
	}

	/**
	 * Applies the single-source currentness, component and key rules to one
	 * exact root of the addressed source. A source match is always final.
	 *
	 * @return UNIQUE or UNRESOLVED_INVALID
	 */
	static IntersectionEndpointProvenanceResult2D resolveSingle(
			IntersectionRootRevisionEvidence2D evidence, LocusDefinition2D definition,
			long revision, PersistentGeoId sourceId, PersistentGeoId richId,
			String token, PersistentGeoId endpointId) {
		if (endpointId == null || richId == null || sourceId == null
				|| token == null) {
			return IntersectionEndpointProvenanceResult2D.unresolved(revision,
					"Durable source, intersection and endpoint identities are required");
		}
		if (evidence.getLocusSemanticRevision() != definition.getSemanticRevision()) {
			// A stale identity match is final: it must not fall through.
			return IntersectionEndpointProvenanceResult2D.unresolved(revision,
					"The single-source root is stale for the addressed source revision");
		}
		LocusBranch2D branch = definition.getBranch(evidence.getBranchSnapshotKey());
		LocusDriverDomainProvider2D provider = definition.getProvider();
		double raw = evidence.getSemanticParameter();
		double canonical = provider.canonicalize(raw);
		int selected = branch == null || !Double.isFinite(canonical) ? -1
				: uniqueComponent(branch.getValidDomainComponents(), canonical,
						provider.getDomainEpsilon());
		LocusSemanticAddress2D address = selected < 0 ? null
				: addressFromRawParameter(definition, sourceId, branch.getBranchKey(), raw);
		if (address == null || !IntersectionCapabilityContext2D.componentKey(
				branch.getBranchKey(), selected).equals(
						evidence.getResolvedValidComponentKey())) {
			return IntersectionEndpointProvenanceResult2D.unresolved(revision,
					"The single-source root has no unique agreeing current component");
		}
		String occurrenceKey = SINGLE_SOURCE_OCCURRENCE_VERSION + "|addressed-source="
				+ sourceId.toExternalForm() + "|rich-result=" + richId.toExternalForm()
				+ "|token=" + token + "|point=" + endpointId.toExternalForm();
		return IntersectionEndpointProvenanceResult2D.resolvedSingle(revision,
				new IntersectionEndpointProvenanceResult2D.Match(occurrenceKey, address));
	}

	private static LocusSemanticAddress2D address(LocusDefinition2D definition,
			PersistentGeoId sourceId, LocusPairSourceRevisionEvidence2D side) {
		// The lifted solver coordinate is search evidence, never endpoint authority.
		return addressFromRawParameter(definition, sourceId, side.getBranchKey(),
				side.getSemanticParameter());
	}

	/**
	 * Builds the section 23.4 address of one raw branch parameter on the addressed
	 * source: provider canonicalization, exactly one containing valid component,
	 * and the A1 periodic lift and seam side. Shared by the section 24 families.
	 *
	 * @return current address, or {@code null} without a unique containing
	 *         component
	 */
	public static LocusSemanticAddress2D addressFromRawParameter(
			LocusDefinition2D definition, PersistentGeoId sourceId, String branchKey,
			double raw) {
		LocusBranch2D branch = definition.getBranch(branchKey);
		if (branch == null) {
			return null;
		}
		LocusDriverDomainProvider2D provider = definition.getProvider();
		double canonical = provider.canonicalize(raw);
		if (!Double.isFinite(canonical)) {
			return null;
		}
		List<LocusInterval2D> components = branch.getValidDomainComponents();
		int selected = uniqueComponent(components, canonical,
				provider.getDomainEpsilon());
		if (selected < 0) {
			return null;
		}
		long lift = periodicLift(provider, raw);
		SeamSide seam = seamSide(provider, raw, canonical, lift);
		return new LocusSemanticAddress2D(sourceId, provider.getProviderId(),
				branch.getBranchKey(), LocusComponentLineage2D.create(
						branch.getBranchKey(), components.get(selected)), canonical,
				lift, seam);
	}

	/** @return index of the only containing component, or negative */
	private static int uniqueComponent(List<LocusInterval2D> components,
			double canonical, double epsilon) {
		int selected = -1;
		for (int index = 0; index < components.size(); index++) {
			if (components.get(index).contains(canonical, epsilon)) {
				if (selected >= 0) {
					return -2;
				}
				selected = index;
			}
		}
		return selected;
	}

	private static long periodicLift(LocusDriverDomainProvider2D provider,
			double rawParameter) {
		if (!provider.isPeriodic()) {
			return 0;
		}
		LocusInterval2D domain = provider.getDeclaredDomain();
		return (long) Math.floor((rawParameter - domain.getLower())
				/ (domain.getUpper() - domain.getLower()));
	}

	private static SeamSide seamSide(LocusDriverDomainProvider2D provider,
			double rawParameter, double canonical, long lift) {
		if (!provider.isPeriodic()) {
			return SeamSide.NOT_PERIODIC;
		}
		LocusInterval2D domain = provider.getDeclaredDomain();
		if (canonical != domain.getLower() && canonical != domain.getUpper()) {
			return SeamSide.INTERIOR;
		}
		return rawParameter == canonical && lift == 0
				? SeamSide.LOWER_APPROACH : SeamSide.UPPER_APPROACH;
	}
}
