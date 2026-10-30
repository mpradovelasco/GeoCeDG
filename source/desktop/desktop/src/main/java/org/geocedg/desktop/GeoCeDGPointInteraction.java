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
import org.geocedg.common.kernel.locus.interaction.LocusSnapshotPlacement2D;
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
		return create(source, x, y, LocusPointInteractionPolicy2D.initial(radius), chooser);
	}

	/** Q is read once; only its current value crosses the kernel query boundary. */
	GeoPoint createFromReference(GeoLocusV2 source, GeoPoint reference,
			LocusPointInteractionPolicy2D policy,
			Function<LocusPointInteractionResult2D,
					LocusPointInteractionCandidate2D> chooser) {
		if (reference == null || reference.getConstruction() != source.getConstruction()
				|| !source.getConstruction().getGeoSetConstructionOrder().contains(reference)
				|| !reference.isDefined() || !Double.isFinite(reference.getInhomX())
				|| !Double.isFinite(reference.getInhomY())) {
			throw new IllegalArgumentException("CU7_INVALID_REFERENCE");
		}
		return create(source, reference.getInhomX(), reference.getInhomY(), policy, chooser);
	}

	private GeoPoint create(GeoLocusV2 source, double x, double y,
			LocusPointInteractionPolicy2D policy,
			Function<LocusPointInteractionResult2D,
					LocusPointInteractionCandidate2D> chooser) {
		try (var selection = LocusSnapshotPlacement2D.query(source, x, y,
				policy)) {
			lastResult = selection.getResult();
			LocusPointInteractionCandidate2D candidate = lastResult.getUniqueCandidate();
			if (lastResult.getStatus()
					== LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES) {
				candidate = chooser.apply(lastResult);
			}
			if (candidate == null) {
				return null;
			}
			GeoPoint point = selection.commit(null, candidate);
			point.setLabel(null);
			return point;
		}
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
