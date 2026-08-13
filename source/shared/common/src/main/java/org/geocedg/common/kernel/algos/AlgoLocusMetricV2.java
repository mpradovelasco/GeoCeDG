/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.metric.BetweenPositionsMetricQuery;
import org.geocedg.common.kernel.locus.metric.LocusMetricCapabilityHierarchy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricComponentBuildException;
import org.geocedg.common.kernel.locus.metric.LocusMetricEngine2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexMode;
import org.geocedg.common.kernel.locus.metric.LocusMetricOwnerLease2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricQuery2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResults2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricDiagnostic2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.TotalLocusMetricQuery;
import org.geocedg.common.kernel.locus.metric.TraversalOutcome;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Normal-DAG publisher for one internal Locus V2 metric query.
 */
public final class AlgoLocusMetricV2 extends AlgoElement {
	private final GeoLocusV2 source;
	private LocusMetricQuery2D query;
	private final LocusMetricCapabilityHierarchy2D capabilities;
	private final LocusMetricIndexMode indexMode;
	private final String consumerIdentity;
	private final GeoElement[] configuredInputs;
	private final GeoLocusMetricResult result;
	private final LocusMetricEngine2D engine = new LocusMetricEngine2D();
	private LocusMetricOwnerLease2D ownerLease;

	/**
	 * Creates, wires and computes an internal metric result.
	 *
	 * @param queryDependencies every GeoElement used to construct the immutable
	 *        query; may be empty when positions are internal semantic values
	 */
	public AlgoLocusMetricV2(Construction construction, GeoLocusV2 source,
			LocusMetricQuery2D query,
			LocusMetricCapabilityHierarchy2D capabilities,
			LocusMetricIndexMode indexMode, String consumerIdentity,
			GeoElement[] queryDependencies) {
		super(construction, false);
		if (consumerIdentity == null || consumerIdentity.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Metric consumer identity is required for work accounting");
		}
		this.source = source;
		this.query = query;
		this.capabilities = capabilities;
		this.indexMode = indexMode;
		this.consumerIdentity = consumerIdentity;
		this.configuredInputs = combineInputs(source, queryDependencies);
		this.result = new GeoLocusMetricResult(construction,
				source.getLocusIdentity());
		this.ownerLease = source.acquireMetricOwnerLease();
		setProtectedInput(true);
		setInputOutput();
		setDependencies();
		compute();
	}

	@Override
	protected void setInputOutput() {
		input = configuredInputs;
		setOnlyOutput(result);
	}

	@Override
	public void compute() {
		long revision = Math.max(1, source.getSemanticRevision());
		result.beginMetricRevision(revision);
		LocusDefinition2D definition = source.getSemanticDefinition();
		if (definition == null || !source.isDefined()) {
			publishFailure(revision, MetricComputationStatus.INVALID_QUERY,
					List.of(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.P1_FAILURE,
							"Source locus has no current defined semantic snapshot")));
			return;
		}
		try {
			ownerLease.getOwner().invalidateObsoleteRevision(revision);
			LocusMetricQuery2D currentQuery = queryForRevision(revision);
			LocusMetricResult2D candidate = engine.compute(currentQuery, definition,
					capabilities, ownerLease.getOwner(), indexMode,
					source.getMetricInstrumentation(), consumerIdentity);
			result.publishMetricResult(revision, candidate);
		} catch (LocusMetricComponentBuildException exception) {
			publishFailure(revision, exception.getComputationStatus(),
					exception.getDiagnostics());
		} catch (RuntimeException exception) {
			publishFailure(revision,
					MetricComputationStatus.NUMERICAL_FAILURE,
					List.of(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.NUMERICAL_FAILURE,
							"Unhandled metric computation failure was converted "
									+ "to a coherent P1 snapshot: "
									+ exception.getClass().getSimpleName())));
		}
	}

	private LocusMetricQuery2D queryForRevision(long revision) {
		if (query instanceof TotalLocusMetricQuery
				&& query.getSemanticRevision() != revision) {
			query = new TotalLocusMetricQuery(query.getLocusIdentity(), revision,
					query.getPolicy());
		}
		return query;
	}

	public GeoLocusMetricResult getResult() {
		return result;
	}

	public GeoLocusV2 getSource() {
		return source;
	}

	public LocusMetricQuery2D getQuery() {
		return query;
	}

	@Override
	public void remove() {
		if (ownerLease != null) {
			ownerLease.close();
			ownerLease = null;
		}
		super.remove();
	}

	@Override
	public Algos getClassName() {
		return Algos.Expression;
	}

	private void publishFailure(long revision,
			MetricComputationStatus status,
			List<MetricDiagnostic2D> diagnostics) {
		LocusMetricPolicy2D policy = query.getPolicy();
		Optional<TraversalOutcome> outcome =
				query instanceof BetweenPositionsMetricQuery
						? Optional.of(TraversalOutcome.TARGET_NOT_REACHABLE)
						: Optional.empty();
		result.publishMetricFailure(revision, LocusMetricResults2D.failure(
				source.getLocusIdentity(), revision, policy, status, outcome,
				diagnostics));
	}

	private static GeoElement[] combineInputs(GeoLocusV2 source,
			GeoElement[] queryDependencies) {
		ArrayList<GeoElement> inputs = new ArrayList<>();
		inputs.add(source);
		if (queryDependencies != null) {
			for (GeoElement dependency : queryDependencies) {
				if (dependency == null) {
					throw new IllegalArgumentException(
							"Metric query dependencies cannot contain null");
				}
				if (!inputs.contains(dependency)) {
					inputs.add(dependency);
				}
			}
		}
		return inputs.toArray(new GeoElement[0]);
	}
}
