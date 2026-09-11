/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spline;

import java.util.ArrayList;

import org.geocedg.common.kernel.algos.AlgoLocusSimilarityTransform2D;
import org.geocedg.common.kernel.algos.AlgoSplineV2;
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
import org.geogebra.common.kernel.algos.AlgoDependentList;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoPoint;

/** Exact resolver for source-point occurrences in an ordered SplineV2 constructor. */
public final class SplineConstructorOccurrenceResolver2D {
	private static final String OCCURRENCE_VERSION =
			"spline-v2/input-occurrence/v1";

	/**
	 * Resolves only explicit constructor occurrence provenance. Similarity images
	 * are followed through their declared R5 source lineage; all other source
	 * families fail without geometric search.
	 *
	 * @return zero, one, many, or unresolved current semantic addresses
	 */
	public SplineConstructorOccurrenceResult2D resolve(GeoLocusV2 addressedSource,
			GeoPoint endpoint) {
		if (addressedSource == null || endpoint == null) {
			return SplineConstructorOccurrenceResult2D.unresolved(0,
					"A source and endpoint are required");
		}
		long revision = Math.max(0, addressedSource.getSemanticRevision());
		LocusDefinition2D definition = addressedSource.getSemanticDefinition();
		if (!addressedSource.isDefined() || definition == null) {
			return SplineConstructorOccurrenceResult2D.unresolved(revision,
					"The addressed source has no current semantic definition");
		}
		GeoLocusV2 lineage = addressedSource;
		AlgoElement parent = lineage.getParentAlgorithm();
		int lineageDepth = 0;
		while (parent instanceof AlgoLocusSimilarityTransform2D) {
			lineage = ((AlgoLocusSimilarityTransform2D) parent).getSource();
			if (lineage == null || !lineage.isDefined()
					|| lineage.getSemanticDefinition() == null || ++lineageDepth > 32) {
				return SplineConstructorOccurrenceResult2D.unresolved(revision,
						"R5 source lineage is not current and exact");
			}
			parent = lineage.getParentAlgorithm();
		}
		if (!(parent instanceof AlgoSplineV2)) {
			return SplineConstructorOccurrenceResult2D.noAddress(revision,
					"The source has no supported SplineV2 constructor lineage");
		}
		return resolveSpline((AlgoSplineV2) parent, addressedSource, endpoint,
				definition, revision);
	}

	private static SplineConstructorOccurrenceResult2D resolveSpline(
			AlgoSplineV2 spline, GeoLocusV2 addressedSource, GeoPoint endpoint,
			LocusDefinition2D definition, long revision) {
		SplinePolynomialModel2D model = spline.getPolynomialModel();
		GeoList points = spline.getConstructorPointList();
		AlgoElement listParent = points == null ? null : points.getParentAlgorithm();
		if (model == null || !(listParent instanceof AlgoDependentList)) {
			return SplineConstructorOccurrenceResult2D.unresolved(revision,
					"SplineV2 needs a current direct ordered constructor list");
		}
		GeoElement[] orderedOccurrences = listParent.getInput();
		if (orderedOccurrences.length != points.size()
				|| orderedOccurrences.length != model.getKnots().length) {
			return SplineConstructorOccurrenceResult2D.unresolved(revision,
					"The ordered constructor topology is not exactly reconstructible");
		}
		SpatialIdentityRegistry registry = endpoint.getConstruction()
				.getSpatialIdentityRegistry();
		PersistentGeoId endpointId = registry.getPersistentGeoId(endpoint);
		PersistentGeoId constructorId = registry.getPersistentGeoId(points);
		PersistentGeoId rootSourceId = spline.getLocus().getPersistentLocusId();
		PersistentGeoId addressedSourceId = addressedSource.getPersistentLocusId();
		if (endpointId == null || constructorId == null || rootSourceId == null
				|| addressedSourceId == null) {
			return SplineConstructorOccurrenceResult2D.unresolved(revision,
					"Durable constructor, source and endpoint identities are required");
		}
		double[] knots = model.getKnots();
		ArrayList<SplineConstructorOccurrenceResult2D.Match> matches =
				new ArrayList<>();
		for (int slot = 0; slot < orderedOccurrences.length; slot++) {
			GeoElement occurrence = orderedOccurrences[slot];
			PersistentGeoId occurrencePointId =
					registry.getPersistentGeoId(occurrence);
			if (!endpointId.equals(occurrencePointId)) {
				continue;
			}
			LocusSemanticAddress2D address = address(definition,
					addressedSourceId, knots[slot]);
			if (address == null) {
				return SplineConstructorOccurrenceResult2D.unresolved(revision,
						"A constructor knot has no unique current branch component");
			}
			String occurrenceKey = OCCURRENCE_VERSION + "|addressed-source="
					+ addressedSourceId.toExternalForm() + "|root-source="
					+ rootSourceId.toExternalForm() + "|constructor="
					+ constructorId.toExternalForm() + "|slot=" + slot + "|point="
					+ endpointId.toExternalForm();
			matches.add(new SplineConstructorOccurrenceResult2D.Match(
					occurrenceKey, slot, address));
		}
		if (matches.isEmpty()) {
			return SplineConstructorOccurrenceResult2D.noAddress(revision,
					"The endpoint has no occurrence in this SplineV2 constructor");
		}
		return SplineConstructorOccurrenceResult2D.resolved(revision, matches);
	}

	private static LocusSemanticAddress2D address(LocusDefinition2D definition,
			PersistentGeoId sourceId, double rawParameter) {
		LocusDriverDomainProvider2D provider = definition.getProvider();
		double canonical = provider.canonicalize(rawParameter);
		if (!Double.isFinite(canonical)) {
			return null;
		}
		LocusBranch2D branch = definition.getBranch(AlgoSplineV2.BRANCH_KEY);
		if (branch == null) {
			return null;
		}
		LocusInterval2D selected = null;
		for (LocusInterval2D component : branch.getValidDomainComponents()) {
			if (component.contains(canonical, provider.getDomainEpsilon())) {
				if (selected != null) {
					return null;
				}
				selected = component;
			}
		}
		if (selected == null) {
			return null;
		}
		long lift = periodicLift(provider, rawParameter);
		SeamSide side = seamSide(provider, rawParameter, canonical, lift);
		return new LocusSemanticAddress2D(sourceId, provider.getProviderId(),
				branch.getBranchKey(), LocusComponentLineage2D.create(
						branch.getBranchKey(), selected), canonical, lift, side);
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
