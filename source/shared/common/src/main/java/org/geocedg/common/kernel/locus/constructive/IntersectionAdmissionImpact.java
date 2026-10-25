/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.intersection.ConstructiveRequalification2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ConstructiveAdmission;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTarget2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTargets2D;
import org.geocedg.common.kernel.locus.intersection.ResidualJetSource2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * The part of the impact report of the adoption event that re-qualifies the roots G8B-R1 admits
 * in the public intersections of the document ({@code locus-v2-constructive-authority.md} section
 * 7.4; validation matrix row V-AUT-ADOPT-8): each admitted root is listed with its admission
 * under the constructive authority, {@code STRICT}, {@code RELAXED} or {@code NOT_ESTABLISHED},
 * computed before the policy is applied and without publishing anything.
 */
final class IntersectionAdmissionImpact
		implements AuthorityPolicyAdoption.AdoptionImpactContributor {
	/** Admission of a root under the legacy policy, as the report names it. */
	static final String LEGACY_ADMISSION = "ADMITTED by G8B-R1 under "
			+ AuthorityPolicy.LEGACY_AUTHORITY;

	@Override
	public int contribute(Construction construction,
			List<AuthorityAdoptionImpact.Entry> entries) {
		int examined = 0;
		for (GeoElement geo : construction.getGeoSetConstructionOrder()) {
			AlgoElement parent = geo.getParentAlgorithm();
			if (!(parent instanceof AlgoLocusIntersectionV2)
					|| parent.getClassName() != Commands.Intersect
					|| ((AlgoLocusIntersectionV2) parent).getResult() != geo) {
				continue;
			}
			AlgoLocusIntersectionV2 intersection = (AlgoLocusIntersectionV2) parent;
			GeoLocusIntersectionResult result = intersection.getResult();
			LocusIntersectionResult2D rich = result.getIntersectionResult();
			GeoLocusV2 locus = intersection.getSource();
			LocusDefinition2D definition = locus.getSemanticDefinition();
			if (rich == null || definition == null) {
				continue;
			}
			LocusIntersectionTarget2D target;
			try {
				target = LocusIntersectionTargets2D.capture(intersection.getTarget(),
						"adoption-impact", rich.getSourceBinding().getTargetUpdateStamp());
			} catch (IllegalArgumentException unsupported) {
				continue;
			}
			ResidualJetSource2D source = ConstructiveResidualJetSource.capture(locus,
					intersection.getTarget());
			for (LocusIntersectionSolution2D solution : rich.getFiniteSolutions()) {
				if (!result.isPointAdmissible(solution.getIdentity().getRootToken())) {
					continue;
				}
				examined++;
				ConstructiveAdmission admission = ConstructiveRequalification2D.of(definition,
						target, source, solution);
				entries.add(new AuthorityAdoptionImpact.Entry(geo.getLabelSimple(),
						"root at driver parameter "
								+ solution.getRevisionEvidence().getSemanticParameter(),
						LEGACY_ADMISSION, admission.name(), cause(admission, source)));
			}
		}
		return examined;
	}

	private static String cause(ConstructiveAdmission admission, ResidualJetSource2D source) {
		switch (admission) {
		case STRICT:
			return RelationIntegrityRecord.Evidence.CERTIFIED_ENCLOSURE.name();
		case RELAXED:
			return RelationIntegrityRecord.Evidence.ENCLOSURE_COMPATIBLE.name();
		default:
			return source == null ? "NO_CONSTRUCTIVE_ENCLOSURE" : "EVIDENCE_INSUFFICIENT";
		}
	}
}
