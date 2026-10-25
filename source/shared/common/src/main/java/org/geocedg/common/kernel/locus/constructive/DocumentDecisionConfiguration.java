/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Objects;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionDecisionConfiguration2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTarget2D;
import org.geocedg.common.kernel.locus.intersection.ResidualJetSource2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * The decision configuration of a public intersection of a document, read from the authority
 * policy of the document at every computation: the captured configuration under
 * {@code legacy-representation-authority/v1}, the constructive configuration under
 * {@code constructive-authority/v1} ({@code locus-v2-intersections.md}, constructive authority
 * amendment). The policy changes only by the explicit adoption event, Undo or a reload.
 */
public final class DocumentDecisionConfiguration implements IntersectionDecisionConfiguration2D {
	private final Construction construction;
	private final GeoLocusV2 locus;
	private final GeoElement target;

	/**
	 * @param construction the document construction
	 * @param locus the source of the query
	 * @param target the target of the query
	 */
	public DocumentDecisionConfiguration(Construction construction, GeoLocusV2 locus,
			GeoElement target) {
		this.construction = Objects.requireNonNull(construction);
		this.locus = Objects.requireNonNull(locus);
		this.target = Objects.requireNonNull(target);
	}

	@Override
	public ResidualJetSource2D residualJets(LocusDefinition2D definition,
			LocusIntersectionTarget2D capturedTarget) {
		return isConstructive() ? ConstructiveResidualJetSource.capture(locus, target)
				: CAPTURED.residualJets(definition, capturedTarget);
	}

	@Override
	public boolean isConstructive() {
		return AuthorityPolicy.of(construction).isConstructive();
	}
}
