/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.IndividualForwardPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.main.AppD;

/** Desktop compatibility adapter; routing and publication are kernel-owned. */
final class GeoCeDGReferencePlacement implements AutoCloseable {
	enum Route { R1_R6, R2_H1, INDIVIDUAL_FORWARD_ADDRESS_CREATION, REJECTED }
	static final class Rejected extends IllegalStateException {
		private static final long serialVersionUID = 1L;
		Rejected(String code) { super(code); }
	}
	/** UI capability wrapping the exact query-owned kernel choice. */
	static final class Choice {
		private final LocusPointCreation2D.Choice kernelChoice;
		private Choice(LocusPointCreation2D.Choice kernelChoice) {
			this.kernelChoice = kernelChoice;
		}
		LocusSemanticAddress2D getAddress() { return kernelChoice.getAddress(); }
	}

	private final LocusPointCreation2D.Selection selection;
	private final List<Choice> choices;
	private final GeoCeDGPointRequest request;

	GeoCeDGReferencePlacement(GeoLocusV2 source, GeoPoint reference,
			LocusPointInteractionPolicy2D policy) {
		this(source, referenceRequest(source, reference, policy));
	}

	GeoCeDGReferencePlacement(GeoLocusV2 source, double x, double y,
			LocusPointInteractionPolicy2D policy) {
		this(source, GeoCeDGPointRequest.forSource((AppD) source.getKernel().getApplication(),
				source, x, y, policy));
	}

	GeoCeDGReferencePlacement(GeoLocusV2 source, GeoCeDGPointRequest request) {
		request.requireSource(source);
		this.request = request;
		selection = request.getReference() == null
				? fromWorld(source, request.getX(), request.getY(), request.getPolicy())
				: fromReference(source, request.getReference(), request.getPolicy());
		try { request.requireCurrent(); }
		catch (RuntimeException failure) { selection.close(); throw failure; }
		choices = selection.getChoices().stream().map(Choice::new).toList();
	}

	private static GeoCeDGPointRequest referenceRequest(GeoLocusV2 source, GeoPoint reference,
			LocusPointInteractionPolicy2D policy) {
		if (reference == null || reference.getConstruction() != source.getConstruction()
				|| !source.getConstruction().getGeoSetConstructionOrder().contains(reference)
				|| !reference.isDefined() || !Double.isFinite(reference.getInhomX())
				|| !Double.isFinite(reference.getInhomY())) {
			throw new Rejected("INVALID_REFERENCE");
		}
		return GeoCeDGPointRequest.forSource((AppD) source.getKernel().getApplication(), source,
				reference.getInhomX(), reference.getInhomY(), policy).withReference(reference);
	}

	private static LocusPointCreation2D.Selection fromReference(GeoLocusV2 source,
			GeoPoint reference, LocusPointInteractionPolicy2D policy) {
		try { return LocusPointCreation2D.queryFromReference(source, reference, policy); }
		catch (LocusPointCreation2D.Rejected failure) { throw new Rejected(failure.getMessage()); }
		catch (IndividualForwardPointCreation2D.Rejected failure) { throw new Rejected(failure.getFailure().name()); }
	}

	private static LocusPointCreation2D.Selection fromWorld(GeoLocusV2 source,
			double x, double y, LocusPointInteractionPolicy2D policy) {
		try { return LocusPointCreation2D.query(source, x, y, policy); }
		catch (LocusPointCreation2D.Rejected failure) { throw new Rejected(failure.getMessage()); }
		catch (IndividualForwardPointCreation2D.Rejected failure) { throw new Rejected(failure.getFailure().name()); }
	}

	Route getRoute() { return Route.valueOf(selection.getRoute().name()); }
	List<Choice> getChoices() { return choices; }
	String getFailure() { return selection.getFailure(); }
	LocusPointInteractionResult2D getHistoricalResult() { return selection.getHistoricalResult(); }
	M08SnapshotPlacement2D.Selection getLocalSelection() { return selection.getLocalSelection(); }
	M08SnapshotPlacement2D.Validity getValidity() {
		var validity = selection.getValidity();
		return validity == M08SnapshotPlacement2D.Validity.CURRENT && !request.isCurrent()
				? M08SnapshotPlacement2D.Validity.STALE : validity;
	}
	long getSemanticMisses() { return selection.getSemanticMisses(); }
	GeoCeDGPointRequest getRequest() { return request; }
	GeoPoint commit(Choice choice) {
		return commit(choice, () -> { });
	}
	GeoPoint commit(Choice choice, Runnable contextGuard) {
		try {
			return selection.commit(null, choice == null ? null : choice.kernelChoice, () -> {
				request.requireCurrent();
				contextGuard.run();
			});
		}
		catch (LocusPointCreation2D.Rejected failure) { throw new Rejected(failure.getMessage()); }
		catch (IndividualForwardPointCreation2D.Rejected failure) { throw new Rejected(failure.getFailure().name()); }
		catch (GeoCeDGPointRequest.StaleContext failure) { throw new Rejected(failure.getMessage()); }
	}
	@Override public void close() { selection.close(); }
}
