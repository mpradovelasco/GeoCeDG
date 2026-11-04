/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.NativeTransactionalState2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoPoint;

/** Copied request for native 2D paths. All snapping remains in AlgoPointOnPath. */
final class GeoCeDGNativePointPlacement implements AutoCloseable {

	private final GeoElement source;
	private final GeoPoint reference;
	private final Object document;
	private final GeoCeDGPointRequest request;
	private final double x;
	private final double y;
	private final double radius;
	private boolean consumed;

	static boolean supports(GeoElement geo) {
		return geo != null && !geo.isAxis() && !geo.isGeoElement3D()
				&& (geo instanceof GeoLine || geo instanceof GeoConic);
	}

	GeoCeDGNativePointPlacement(GeoElement source, GeoPoint reference, double x, double y,
			double radius) {
		this(source, reference, x, y, radius, null);
	}

	GeoCeDGNativePointPlacement(GeoElement source, GeoCeDGPointRequest request) {
		this(source, request.getReference(), request.getX(), request.getY(),
				request.getRadius(), request);
	}

	private GeoCeDGNativePointPlacement(GeoElement source, GeoPoint reference,
			double x, double y, double radius, GeoCeDGPointRequest request) {
		if (!supports(source) || !current(source) || !Double.isFinite(x)
				|| !Double.isFinite(y) || !Double.isFinite(radius) || radius <= 0
				|| reference != null && (!current(reference)
						|| reference.getConstruction() != source.getConstruction()
						|| reference.isGeoElement3D() || reference.getInhomX() != x
						|| reference.getInhomY() != y)) {
			throw new IllegalArgumentException("INVALID_REFERENCE");
		}
		this.source = source;
		this.reference = reference;
		this.x = x;
		this.y = y;
		this.radius = radius;
		document = source.getConstruction().getSpatialIdentityRegistry();
		if (request == null) {
			var captured = GeoCeDGPointRequest.forSource(
					(org.geogebra.desktop.main.AppD) source.getKernel().getApplication(),
					source, x, y, LocusPointInteractionPolicy2D.initial(radius));
			this.request = reference == null ? captured : captured.withReference(reference);
		} else {
			request.requireSource(source);
			if (request.getReference() != reference) {
				throw new IllegalArgumentException("INVALID_REFERENCE");
			}
			this.request = request;
		}
	}

	private static boolean current(GeoElement geo) {
		return geo != null && geo.isDefined()
				&& geo.getConstruction().getGeoSetConstructionOrder().contains(geo)
				&& !geo.getConstruction().isFileLoading();
	}

	GeoPoint commit(boolean draggable) {
		if (consumed) { throw new IllegalStateException("CONSUMED_SELECTION"); }
		consumed = true;
		validateFreshness();
		var construction = source.getConstruction();
		if (reference != null) {
			var reason = LocusV2PublicOperations.getUnsupportedInteractiveHostStateReason(construction);
			if (reason != null) {
				NativeTransactionalState2D.requireAvailable(reason);
				throw new IllegalStateException("UNSUPPORTED_HOST_STATE");
			}
		}
		GeoPoint[] output = new GeoPoint[1];
		construction.runAtomicConstructionMutation(() -> {
			validateFreshness();
			GeoPoint point = construction.getKernel().getAlgoDispatcher()
					.point(null, (Path) source, x, y, true, false, true);
			point.setFixed(!draggable);
			validateFreshness();
			if (!current(point) || !Double.isFinite(point.getInhomX())
					|| !Double.isFinite(point.getInhomY()) || point.getPath() != source
					|| !(point.getParentAlgorithm() instanceof AlgoPointOnPath parent)
					|| parent.getPath() != source || parent.getP() != point
					|| parent.getInput().length != 1 || parent.getInput()[0] != source
					|| parent.getOutput().length != 1 || parent.getOutput()[0] != point
					|| point.isLocked() != !draggable
					|| Math.hypot(point.getInhomX() - x, point.getInhomY() - y) > radius) {
				throw new IllegalStateException("NATIVE_PLACEMENT_REJECTED");
			}
			output[0] = point;
		});
		return output[0];
	}

	private void validateFreshness() {
		if (!current(source) || document != source.getConstruction().getSpatialIdentityRegistry()
				|| !request.isCurrent()
				|| reference != null && (!current(reference)
						|| reference.getInhomX() != x || reference.getInhomY() != y)) {
			throw new IllegalStateException("STALE_SELECTION");
		}
	}

	@Override public void close() { consumed = true; }
}
