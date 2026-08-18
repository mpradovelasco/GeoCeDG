/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.runtime;

import java.util.Set;

import org.geocedg.common.kernel.spatial.identity.ProjectionSystemId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemCertificate;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialSemanticInstrumentation;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.geos.GeoBoolean;

/** Normal-DAG publisher for one complete version-two projection system. */
public final class AlgoProjectionSystemCertificate extends AlgoElement {
	private final SpatialSemanticInputs.SystemTopology topology;
	private final SpatialSemanticInstrumentation instrumentation;
	private final GeoBoolean statusOutput;
	private ProjectionSystemPilotCertificate certificate;

	AlgoProjectionSystemCertificate(Construction construction,
			SpatialSemanticInputs.SystemTopology topology,
			SpatialSemanticInstrumentation instrumentation) {
		super(construction, false);
		this.topology = topology;
		this.instrumentation = instrumentation;
		this.statusOutput = new GeoBoolean(construction);
		setProtectedInput(true);
		setInputOutput();
		setDependencies();
		compute();
	}

	@Override
	protected void setInputOutput() {
		input = topology.getInputs();
		setOnlyOutput(statusOutput);
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
		ProjectionSystemCertificate candidate;
		boolean evaluationCompleted = true;
		try {
			candidate = topology.evaluate(before, instrumentation);
		} catch (RuntimeException exception) {
			candidate = SpatialSemanticInputs.invalidatedSystem();
			evaluationCompleted = false;
		}
		SpatialSemanticInputs.Snapshot after = topology.capture();
		if (!before.sameRevisionAndValues(after)) {
			instrumentation.recordSupersededCandidateRejection();
			certificate = new ProjectionSystemPilotCertificate(topology.getSystemId(),
					before.getRevisionTuple(), before.getValueToken(),
					SpatialSemanticInputs.invalidatedSystem(), false);
			statusOutput.setUndefined();
			return;
		}
		certificate = new ProjectionSystemPilotCertificate(topology.getSystemId(),
				before.getRevisionTuple(), before.getValueToken(), candidate,
				evaluationCompleted);
		if (candidate.getStatus() == ProjectionSystemStatus.NOT_EVALUATED) {
			statusOutput.setUndefined();
		} else {
			statusOutput.setValue(candidate.getStatus()
					== ProjectionSystemStatus.CONSISTENT);
		}
	}

	/** Withdraws current evidence without scheduling any hidden recomputation. */
	public void invalidateCurrentRevision() {
		SpatialSemanticInputs.Snapshot snapshot = topology.capture();
		certificate = new ProjectionSystemPilotCertificate(topology.getSystemId(),
				snapshot.getRevisionTuple(), snapshot.getValueToken(),
				SpatialSemanticInputs.invalidatedSystem(), false);
		statusOutput.setUndefined();
	}

	public ProjectionSystemId getSystemId() {
		return topology.getSystemId();
	}

	public ProjectionSystemPilotCertificate getCertificate() {
		return certificate;
	}

	/** @return an internal DAG output, not a persisted semantic authority */
	public GeoBoolean getStatusOutput() {
		return statusOutput;
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
		return "GeoCeDGProjectionSystemCertificate";
	}
}
