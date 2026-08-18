/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.runtime;

import java.util.Set;

import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.semantic.SpatialCertificateStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialPointCertificate;
import org.geocedg.common.kernel.spatial.semantic.SpatialSemanticInstrumentation;
import org.geocedg.common.kernel.spatial.semantic.Vector3;
import org.geogebra.common.geogebra3D.kernel3D.geos.GeoPoint3D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;

/**
 * Normal-DAG projection-defined POINT evaluator and one-way 3D adapter.
 */
public final class AlgoProjectionDefinedSpatialPoint extends AlgoElement {
	private final SpatialSemanticInputs.PointTopology topology;
	private final SpatialSemanticInstrumentation instrumentation;
	private final GeoPoint3D derivedPoint;
	private SpatialPointPilotCertificate certificate;

	AlgoProjectionDefinedSpatialPoint(Construction construction,
			SpatialSemanticInputs.PointTopology topology,
			SpatialSemanticInstrumentation instrumentation) {
		super(construction, false);
		this.topology = topology;
		this.instrumentation = instrumentation;
		this.derivedPoint = new GeoPoint3D(construction);
		setProtectedInput(true);
		setInputOutput();
		setDependencies();
		this.derivedPoint.setSelectionAllowed(false);
		compute();
		derivedPoint.notifyAdd();
	}

	@Override
	protected void setInputOutput() {
		input = topology.getInputs();
		setOnlyOutput(derivedPoint);
	}

	@Override
	public void compute() {
		SpatialSemanticInputs.Snapshot before = topology.capture();
		if (certificate != null && certificate.isCurrentRevision()
				&& before.isCurrentTopology()
				&& before.getRevisionTuple().equals(certificate.getRevisionTuple())
				&& before.getValueToken().equals(
						certificate.getValueSnapshotToken())) {
			return;
		}
		instrumentation.recordDependencyUpdate();
		SpatialPointCertificate candidate;
		boolean evaluationCompleted = true;
		try {
			candidate = topology.evaluate(before, instrumentation);
		} catch (RuntimeException exception) {
			candidate = SpatialSemanticInputs.invalidatedPoint();
			evaluationCompleted = false;
		}
		SpatialSemanticInputs.Snapshot after = topology.capture();
		if (!before.sameRevisionAndValues(after)) {
			instrumentation.recordSupersededCandidateRejection();
			publishInvalidated(before);
			return;
		}
		certificate = new SpatialPointPilotCertificate(topology.getObjectId(),
				topology.getSystemId(), before.getRevisionTuple(),
				before.getValueToken(), candidate, evaluationCompleted);
		if (candidate.getCertificateStatus() == SpatialCertificateStatus.VALID
				&& candidate.getPoint().isPresent()) {
			Vector3 point = candidate.getPoint().get();
			derivedPoint.setCoords(point.getX(), point.getY(), point.getZ(), 1);
			instrumentation.recordDerivedViewPublication();
		} else {
			withdrawDerivedPoint();
		}
	}

	/** Withdraws current evidence and the one-way view payload immediately. */
	public void invalidateCurrentRevision() {
		publishInvalidated(topology.capture());
	}

	private void publishInvalidated(SpatialSemanticInputs.Snapshot snapshot) {
		certificate = new SpatialPointPilotCertificate(topology.getObjectId(),
				topology.getSystemId(), snapshot.getRevisionTuple(),
				snapshot.getValueToken(), SpatialSemanticInputs.invalidatedPoint(), false);
		withdrawDerivedPoint();
	}

	private void withdrawDerivedPoint() {
		if (derivedPoint.isDefined()) {
			instrumentation.recordDerivedViewWithdrawal();
		}
		derivedPoint.setUndefined();
	}

	public SpatialObjectId getObjectId() {
		return topology.getObjectId();
	}

	public SpatialPointPilotCertificate getCertificate() {
		return certificate;
	}

	/**
	 * @return transient, unlabeled, non-selectable and non-persisted 3D point
	 */
	public GeoPoint3D getDerivedPoint() {
		return derivedPoint;
	}

	public Set<SpatialIdentityId> getDependencyIdentities() {
		return topology.getDependencyIdentities();
	}

	String getTopologyToken() {
		return topology.getStructureToken();
	}

	@Override
	public Algos getClassName() {
		return Algos.Expression;
	}

	@Override
	public String toString(StringTemplate template) {
		return "GeoCeDGProjectionDefinedPoint";
	}
}
