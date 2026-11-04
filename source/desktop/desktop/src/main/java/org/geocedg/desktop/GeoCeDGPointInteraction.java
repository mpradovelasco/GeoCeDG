/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.function.Function;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionCandidate2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionStatus2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointContinuation2D;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.main.AppD;

/** Frontend orchestration of kernel point operations; never implements inverse geometry or identity. */
final class GeoCeDGPointInteraction {
	private final AppD app;
	private LocusPointInteractionResult2D lastResult;
	private String lastMoveDiagnostic;
	private boolean lastMoveChanged;
	private GeoPoint helperDragPoint;
	private LocusPointContinuation2D.Context helperDragContext;
	private GeoCeDGPointRequest helperGestureRequest;

	GeoCeDGPointInteraction(AppD app) {
		this.app = app;
	}

	GeoPoint create(GeoLocusV2 source, double x, double y, double radius,
			Function<LocusPointInteractionResult2D,
					LocusPointInteractionCandidate2D> chooser) {
		var request = GeoCeDGPointRequest.forSource(app, source, x, y,
				LocusPointInteractionPolicy2D.initial(radius));
		try (var selection = query(source, request)) {
			return createHistorical(selection, request, chooser);
		}
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
		var request = GeoCeDGPointRequest.forSource(app, source,
				reference.getInhomX(), reference.getInhomY(), policy).withReference(reference);
		try (var selection = query(source, request)) {
			return createHistorical(selection, request, chooser);
		}
	}

	GeoCeDGReferencePlacement query(GeoLocusV2 source, GeoCeDGPointRequest request) {
		request.requireSource(source);
		var selection = new GeoCeDGReferencePlacement(source, request);
		lastResult = selection.getHistoricalResult();
		return selection;
	}

	/** Uses the existing R1/R6 result; a nonhistorical offer is handed to the panel. */
	GeoPoint createHistorical(GeoCeDGReferencePlacement selection, GeoCeDGPointRequest request,
			Function<LocusPointInteractionResult2D,
					LocusPointInteractionCandidate2D> chooser) {
		if (selection.getRequest() != request) { throw new GeoCeDGPointRequest.StaleContext(); }
		request.requireCurrent();
		lastResult = selection.getHistoricalResult();
		if (selection.getRoute() != GeoCeDGReferencePlacement.Route.R1_R6) { return null; }
		LocusPointInteractionCandidate2D candidate = lastResult.getUniqueCandidate();
		if (lastResult.getStatus() == LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES) {
			candidate = chooser.apply(lastResult);
		}
		// The chooser is a callback boundary. A changed view/context cannot publish.
		if (candidate == null || !request.isCurrent()) { return null; }
		if (!lastResult.getCandidates().contains(candidate)) {
			throw new IllegalArgumentException("CU7_SELECTION_NOT_IN_QUERY");
		}
		var address = candidate.getAddress();
		var choices = selection.getChoices().stream()
				.filter(choice -> choice.getAddress().equals(address)).toList();
		if (choices.size() != 1) { throw new IllegalArgumentException("CU7_SELECTION_NOT_IN_QUERY"); }
		return selection.commit(choices.get(0), request::requireCurrent);
	}

	boolean move(GeoPoint point, double x, double y, double radius) {
		if (helperDragPoint != point || helperDragContext == null) {
			helperDragContext = beginDrag(point);
			helperDragPoint = point;
			helperGestureRequest = GeoCeDGPointRequest.forSource(app,
					AlgoSemanticLocusPoint2D.requireSemanticParent(point).getSource(),
					x, y, LocusPointInteractionPolicy2D.initial(radius));
		}
		var request = GeoCeDGPointRequest.forSource(app,
				AlgoSemanticLocusPoint2D.requireSemanticParent(point).getSource(),
				x, y, LocusPointInteractionPolicy2D.initial(radius))
				.withContext(helperGestureRequest::isCurrent);
		return move(point, request, helperDragContext);
	}

	LocusPointContinuation2D.Context beginDrag(GeoPoint point) {
		return LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
	}

	boolean move(GeoPoint point, double x, double y, double radius,
			LocusPointContinuation2D.Context context) {
		var request = GeoCeDGPointRequest.forSource(app,
				AlgoSemanticLocusPoint2D.requireSemanticParent(point).getSource(),
				x, y, LocusPointInteractionPolicy2D.initial(radius));
		return move(point, request, context);
	}

	boolean move(GeoPoint point, GeoCeDGPointRequest request,
			LocusPointContinuation2D.Context context) {
		request.requireSource(AlgoSemanticLocusPoint2D.requireSemanticParent(point).getSource());
		var movement = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(
				point, request.getX(), request.getY(), request.getPolicy(), context,
				request::requireCurrent);
		lastResult = movement.getHistoricalResult();
		lastMoveChanged = movement.hasChanged();
		boolean spanish = "es".equals(app.getLocale().getLanguage());
		lastMoveDiagnostic = switch (movement.getStatus()) {
		case FIXED_POINT -> spanish ? "El punto está bloqueado" : "The point is fixed";
		case CONTINUATION_STOPPED -> spanish ? "Arrastre detenido; suelte y vuelva a seleccionar el punto"
				: "Drag stopped; release and select the point again";
		case BUDGET_EXHAUSTED -> spanish ? "Se alcanzó el presupuesto de continuación"
				: "Continuation work budget reached";
		case UNSUPPORTED_HOST_STATE -> "SOURCE_FIDELITY_RESOURCE_EXHAUSTED".equals(movement.getDiagnostic())
				? spanish ? "Se alcanzó el presupuesto para verificar la restauración nativa; documento sin cambios"
						: "Native restoration verification work budget reached; document unchanged"
				: spanish
				? "El estado actual del documento no permite una restauración exacta"
				: "Current document state does not permit exact restoration";
		case INVALID_SOURCE, AUTHORITY_NOT_ESTABLISHED, COMPONENT_NOT_ESTABLISHED -> spanish
				? "La fuente o su componente no permiten continuar este movimiento"
				: "The source or component cannot establish this continuation";
		default -> spanish ? "Barrera de continuación; se conserva la última posición válida"
				: "Continuation barrier; last valid position retained";
		};
		return movement.isEstablished();
	}

	String getLastMoveDiagnostic() { return lastMoveDiagnostic; }
	boolean didLastMoveChange() { return lastMoveChanged; }

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
