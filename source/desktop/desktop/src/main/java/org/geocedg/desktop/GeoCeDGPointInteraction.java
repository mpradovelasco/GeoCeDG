/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.function.Function;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionCandidate2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionQuery2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResolver2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionStatus2D;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.main.AppD;

/** Frontend orchestration of R6; never implements inverse geometry or identity. */
final class GeoCeDGPointInteraction {
	private final AppD app;
	private LocusPointInteractionResult2D lastResult;

	GeoCeDGPointInteraction(AppD app) {
		this.app = app;
	}

	GeoPoint create(GeoLocusV2 source, double x, double y, double radius,
			Function<LocusPointInteractionResult2D,
					LocusPointInteractionCandidate2D> chooser) {
		lastResult = new LocusPointInteractionResolver2D().resolve(
				new LocusPointInteractionQuery2D(source, x, y,
						LocusPointInteractionPolicy2D.initial(radius)));
		LocusPointInteractionCandidate2D candidate = lastResult.getUniqueCandidate();
		if (lastResult.getStatus()
				== LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES) {
			candidate = chooser.apply(lastResult);
			if (candidate != null && !lastResult.getCandidates().contains(candidate)) {
				throw new IllegalArgumentException("Chooser did not return a current candidate");
			}
		}
		if (candidate == null) {
			return null;
		}
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				app.getKernel().getConstruction(), null, source, candidate);
		point.setLabel(null);
		return point;
	}

	boolean move(GeoPoint point, double x, double y, double radius) {
		lastResult = LocusV2PublicOperations.moveInteractiveSemanticPoint(point,
				x, y, LocusPointInteractionPolicy2D.initial(radius));
		return lastResult.getUniqueCandidate() != null;
	}

	LocusPointInteractionResult2D getLastResult() {
		return lastResult;
	}

	static boolean owns(GeoPoint point) {
		var registry = point.getConstruction().getSpatialIdentityRegistry();
		var id = registry.getPersistentGeoId(point);
		var record = id == null ? null : registry.getGeoRecord(id);
		return record != null && LocusV2PublicOperations.INTERACTION_POINT_OUTPUT_ROLE
				.equals(record.getStableOutputRole())
				&& ConstructionGeoRedefineProvider.hasDedicatedInteractionPointState(point);
	}
}
