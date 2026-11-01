/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionCandidate2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionStatus2D;
import org.geocedg.common.kernel.locus.interaction.LocusSnapshotPlacement2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.kernel.geos.GeoPoint;

/** Client composition only. Each route retains its own kernel authority and budget. */
final class GeoCeDGReferencePlacement implements AutoCloseable {
	enum Route { R1_R6, R2_H1, REJECTED }
	static final class Rejected extends IllegalStateException {
		private static final long serialVersionUID = 1L;
		Rejected(String code) { super(code); }
	}
	/** UI capability, never a durable selector or a substitute kernel candidate. */
	static final class Choice {
		private final GeoCeDGReferencePlacement owner;
		private final LocusPointInteractionCandidate2D historical;
		private final M08SnapshotPlacement2D.Choice local;
		private Choice(GeoCeDGReferencePlacement owner, LocusPointInteractionCandidate2D historical,
				M08SnapshotPlacement2D.Choice local) { this.owner=owner; this.historical=historical; this.local=local; }
		LocusSemanticAddress2D getAddress() {
			return historical != null ? historical.getAddress() : local.getAddress();
		}
	}
	private final GeoLocusV2 source;
	private final GeoPoint reference;
	private final double x;
	private final double y;
	private final LocusDefinition2D definition;
	private final long revision;
	private final SpatialIdentityRegistry registry;
	private final PersistentGeoId sourceId;
	private final Thread thread=Thread.currentThread();
	private final LocusSnapshotPlacement2D.Selection historical;
	private final LocusPointInteractionResult2D historicalResult;
	private M08SnapshotPlacement2D.Selection local;
	private List<Choice> choices=List.of();
	private Route route=Route.REJECTED;
	private String failure;
	private boolean finished;

	GeoCeDGReferencePlacement(GeoLocusV2 source, GeoPoint reference, LocusPointInteractionPolicy2D policy) {
		if(source==null || source.getPersistentLocusId()==null
				|| source.getConstruction().getSpatialIdentityRegistry().getGeo(source.getPersistentLocusId())!=source) {
			throw new Rejected("INVALID_SOURCE");
		}
		if(!validReference(source,reference)) { throw new Rejected("INVALID_REFERENCE"); }
		this.source=source;
		this.reference=reference;
		x=reference.getInhomX();
		y=reference.getInhomY();
		definition=source.getSemanticDefinition();
		revision=definition==null?0:definition.getSemanticRevision();
		registry=source.getConstruction().getSpatialIdentityRegistry();
		sourceId=source.getPersistentLocusId();
		historical=LocusSnapshotPlacement2D.query(source,x,y,policy);
		historicalResult=historical.getResult();
		if(!current()) {
			historical.close();
			failure="STALE_SELECTION";
			return;
		}
		if(historicalResult.getStatus()==LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE
				|| historicalResult.getStatus()==LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES) {
			route=Route.R1_R6;
			choices=historicalResult.getCandidates().stream().map(c->new Choice(this,c,null)).toList();
			return;
		}
		historical.close();
		// Only this typed reason follows R6 separation and safety checks. No string heuristic.
		if(historicalResult.getStatus()!=LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH
				|| historicalResult.getReason()!=LocusPointInteractionResult2D.Reason.GLOBAL_UNIQUENESS_NOT_ESTABLISHED) {
			failure=historicalResult.getReason().name();
			return;
		}
		local=M08SnapshotPlacement2D.queryFromReference(source,reference,policy);
		if(local.getResult().getPermission()!=M08SnapshotPlacement2D.Permission.EXPLICIT_LOCAL_SELECTION_REQUIRED) {
			failure=local.getResult().getFailure().name()+" / "+local.getResult().getLocalFailure();
			local.close();
			return;
		}
		if(!current()) {
			local.close();
			failure="STALE_SELECTION";
			return;
		}
		route=Route.R2_H1;
		choices=local.getResult().getChoices().stream().map(c->new Choice(this,null,c)).toList();
	}
	private static boolean validReference(GeoLocusV2 source, GeoPoint q) {
		return q!=null && q.getConstruction()==source.getConstruction()
			&& source.getConstruction().getGeoSetConstructionOrder().contains(q) && q.isDefined()
			&& Double.isFinite(q.getInhomX()) && Double.isFinite(q.getInhomY());
	}
	private boolean current() {
		return definition!=null && source.isDefined() && source.getSemanticDefinition()==definition
			&& definition.getSemanticRevision()==revision && source.getConstruction().getSpatialIdentityRegistry()==registry
			&& sourceId.equals(source.getPersistentLocusId()) && registry.getGeo(sourceId)==source
			&& !source.getConstruction().isFileLoading() && validReference(source,reference)
			&& reference.getInhomX()==x && reference.getInhomY()==y;
	}
	Route getRoute() { return route; }
	List<Choice> getChoices() { return choices; }
	String getFailure() { return failure; }
	LocusPointInteractionResult2D getHistoricalResult() { return historicalResult; }
	M08SnapshotPlacement2D.Selection getLocalSelection() { return local; }
	M08SnapshotPlacement2D.Validity getValidity() {
		return finished?M08SnapshotPlacement2D.Validity.CONSUMED
			:current()?M08SnapshotPlacement2D.Validity.CURRENT:M08SnapshotPlacement2D.Validity.STALE;
	}
	GeoPoint commit(Choice choice) {
		if(Thread.currentThread()!=thread) { throw new Rejected("WRONG_THREAD"); }
		if(finished) { throw new Rejected("CONSUMED_SELECTION"); }
		finished=true;
		try {
			if(choice==null) { return null; }
			if(choice.owner!=this || !choices.contains(choice)) { throw new Rejected("FOREIGN_CHOICE"); }
			if(!current()) { throw new Rejected("STALE_SELECTION"); }
			if(!RuntimeFeatureService.mayCreateLocusV2(source.getConstruction())) { throw new Rejected("PUBLICATION_NOT_AUTHORIZED"); }
			if(route==Route.R1_R6) {
				try { return historical.commit(null,choice.historical); }
				catch(IllegalArgumentException rejected) { throw new Rejected("R1_FORWARD_REJECTED"); }
			}
			if(route==Route.R2_H1) { return local.commit(null,choice.local); }
			throw new Rejected("PUBLICATION_NOT_AUTHORIZED");
		} finally {
			historical.close();
			if(local!=null) { local.close(); }
		}
	}
	@Override
	public void close() {
		finished=true;
		historical.close();
		if(local!=null) { local.close(); }
	}
}
