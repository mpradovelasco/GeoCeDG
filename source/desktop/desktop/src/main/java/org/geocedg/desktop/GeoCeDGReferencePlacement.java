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

	GeoCeDGReferencePlacement(GeoLocusV2 source, GeoPoint reference,
			LocusPointInteractionPolicy2D policy) {
		this(fromReference(source, reference, policy));
	}

	GeoCeDGReferencePlacement(GeoLocusV2 source, double x, double y,
			LocusPointInteractionPolicy2D policy) {
		this(fromWorld(source, x, y, policy));
	}

	private GeoCeDGReferencePlacement(LocusPointCreation2D.Selection selection) {
		this.selection = selection;
		choices = selection.getChoices().stream().map(Choice::new).toList();
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
	M08SnapshotPlacement2D.Validity getValidity() { return selection.getValidity(); }
	long getSemanticMisses() { return selection.getSemanticMisses(); }
	GeoPoint commit(Choice choice) {
		try { return selection.commit(null, choice == null ? null : choice.kernelChoice); }
		catch (LocusPointCreation2D.Rejected failure) { throw new Rejected(failure.getMessage()); }
		catch (IndividualForwardPointCreation2D.Rejected failure) { throw new Rejected(failure.getFailure().name()); }
	}
	@Override public void close() { selection.close(); }
}
