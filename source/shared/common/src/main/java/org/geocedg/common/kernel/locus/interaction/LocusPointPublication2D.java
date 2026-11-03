/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus.interaction;

import java.util.function.Supplier;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.kernel.geos.GeoPoint;

/** One-use handoff from an owning selection to the normal forward point factory. */
public abstract class LocusPointPublication2D {

	LocusPointPublication2D() {
		// Only selection protocols in this package may provide the handoff.
	}

	public abstract LocusSemanticAddress2D takePublicationAddress(GeoLocusV2 source);

	public abstract MetricPositionBinding2D takePreparedBinding(GeoLocusV2 source);

	/** One concrete protocol snapshot, including labeling callbacks and final facade guards. */
	static GeoPoint publishAtomically(GeoLocusV2 source, LocusSemanticAddress2D address,
			Supplier<GeoPoint> publication, Runnable finalGuard,
			Supplier<? extends RuntimeException> invariantFailure) {
		GeoPoint[] result = {null};
		source.getConstruction().runAtomicConstructionMutation(() -> {
			result[0] = publication.get();
			// Source/reference guards retain their own typed stale-selection diagnostics.
			finalGuard.run();
			if (!matchesPublishedForward(source, address, result[0])) {
				throw invariantFailure.get();
			}
		});
		return result[0];
	}

	private static boolean matchesPublishedForward(GeoLocusV2 source,
			LocusSemanticAddress2D address, GeoPoint point) {
		if (point == null || !point.isDefined() || !source.isDefined()
				|| point.getConstruction() != source.getConstruction()
				|| !RuntimeFeatureService.mayCreateLocusV2(source.getConstruction())
				|| !(point.getParentAlgorithm() instanceof AlgoSemanticLocusPoint2D)) {
			return false;
		}
		AlgoSemanticLocusPoint2D parent = (AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
		var registry = source.getConstruction().getSpatialIdentityRegistry();
		var pointId = registry.getPersistentGeoId(point);
		var record = pointId == null ? null : registry.getGeoRecord(pointId);
		var definition = source.getSemanticDefinition();
		var binding = parent.getMetricPositionBinding();
		if (definition == null || parent.getSource() != source
				|| !parent.hasCurrentAddressState(address)
				|| !address.getSourceLocusId().equals(source.getPersistentLocusId())
				|| registry.getGeo(address.getSourceLocusId()) != source
				|| pointId == null || registry.getGeo(pointId) != point || record == null
				|| !ConstructionGeoRedefineProvider.INTERACTION_POINT_OUTPUT_ROLE
						.equals(record.getStableOutputRole())
				|| !ConstructionGeoRedefineProvider.hasDedicatedInteractionPointState(point)
				|| binding == null || !binding.isValid()
				|| binding.getSemanticRevision() != definition.getSemanticRevision()
				|| !address.toMetricPosition().equals(binding.getSemanticPosition())) {
			return false;
		}
		var evaluated = binding.getEvaluatedPoint().orElse(null);
		return evaluated != null && sameCoordinate(point.getInhomX(), evaluated.getX())
				&& sameCoordinate(point.getInhomY(), evaluated.getY());
	}

	private static boolean sameCoordinate(double actual, double expected) {
		return Double.isFinite(actual) && Double.isFinite(expected)
				&& Double.doubleToLongBits(actual == 0 ? 0 : actual)
						== Double.doubleToLongBits(expected == 0 ? 0 : expected);
	}
}
