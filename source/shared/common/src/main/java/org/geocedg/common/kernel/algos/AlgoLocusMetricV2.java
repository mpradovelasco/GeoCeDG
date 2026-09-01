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
import org.geocedg.common.kernel.locus.metric.DifferentialLocusMetricCapability2D;
import org.geocedg.common.kernel.locus.metric.EvaluatorOnlyLocusMetricCapability2D;
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
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.algos.GetCommand;
import org.geogebra.common.kernel.commands.Commands;
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
	private final boolean publicCommand;
	private final GetCommand commandName;
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
		this(construction, source, query, capabilities, indexMode,
				consumerIdentity, queryDependencies, false, Algos.Expression, null);
	}

	/** Creates a reconstructible public total rich metric result. */
	public AlgoLocusMetricV2(Construction construction, String label,
			GeoLocusV2 source, PersistentGeoId resultId) {
		this(construction, source,
				new TotalLocusMetricQuery(initialSourceIdentity(source),
						initialSemanticRevision(source),
						LocusMetricPolicy2D.publicExperimental()),
				new LocusMetricCapabilityHierarchy2D(List.of(
					new DifferentialLocusMetricCapability2D(
								"g9s1-public-semantic-differential/v1"),
					new EvaluatorOnlyLocusMetricCapability2D(
								"g9s1-public-evaluator-route-metric/v1"))),
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION,
				initialConsumerIdentity(construction, resultId,
						"/total-metric-consumer"), new GeoElement[0], true,
				Commands.LocusLength, label);
	}

	private AlgoLocusMetricV2(Construction construction, GeoLocusV2 source,
			LocusMetricQuery2D query,
			LocusMetricCapabilityHierarchy2D capabilities,
			LocusMetricIndexMode indexMode, String consumerIdentity,
			GeoElement[] queryDependencies, boolean addToConstructionList,
			GetCommand commandName, String label) {
		super(construction, addToConstructionList);
		if (consumerIdentity == null || consumerIdentity.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Metric consumer identity is required for work accounting");
		}
		this.source = source;
		this.query = query;
		this.capabilities = capabilities;
		this.indexMode = indexMode;
		this.consumerIdentity = consumerIdentity;
		this.publicCommand = addToConstructionList;
		this.commandName = commandName;
		this.configuredInputs = combineInputs(source, queryDependencies);
		this.result = new GeoLocusMetricResult(construction,
				publicCommand ? initialSourceIdentity(source)
						: source.getLocusIdentity());
		if (publicCommand) {
			result.enablePublicPersistence();
		}
		this.ownerLease = publicCommand ? null
				: source.acquireMetricOwnerLease();
		setProtectedInput(true);
		setInputOutput();
		setDependencies();
		compute();
		if (label != null) {
			result.setLabel(label);
		}
	}

	@Override
	protected void setInputOutput() {
		input = configuredInputs;
		setOnlyOutput(result);
	}

	@Override
	public void compute() {
		if (publicCommand && !publicIdentitiesReady()) {
			result.setUndefined();
			return;
		}
		if (publicCommand) {
			result.refreshSourceLocusIdentity(source.getLocusIdentity());
		}
		ensureOwnerLease();
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
					source.getMetricInstrumentation(), currentConsumerIdentity());
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
				&& (query.getSemanticRevision() != revision
						|| !query.getLocusIdentity()
								.equals(source.getLocusIdentity()))) {
			query = new TotalLocusMetricQuery(source.getLocusIdentity(), revision,
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
	public GetCommand getClassName() {
		return commandName;
	}

	private boolean publicIdentitiesReady() {
		return source.getPersistentLocusId() != null
				&& cons.getSpatialIdentityRegistry().getPersistentGeoId(result) != null;
	}

	private void ensureOwnerLease() {
		if (ownerLease != null
				&& (ownerLease.getOwner().isReleased()
						|| !ownerLease.getOwner().getLocusIdentity()
								.equals(source.getLocusIdentity()))) {
			ownerLease.close();
			ownerLease = null;
		}
		if (ownerLease == null) {
			ownerLease = source.acquireMetricOwnerLease();
		}
	}

	private String currentConsumerIdentity() {
		if (!publicCommand) {
			return consumerIdentity;
		}
		PersistentGeoId current = cons.getSpatialIdentityRegistry()
				.getPersistentGeoId(result);
		if (current == null) {
			throw new IllegalStateException(
					"Public metric result has no attached durable identity");
		}
		return current.toExternalForm() + "/total-metric-consumer";
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

	private static String initialSourceIdentity(GeoLocusV2 source) {
		PersistentGeoId current = java.util.Objects.requireNonNull(source)
				.getPersistentLocusId();
		return current == null ? "g9u0-pending-locus"
				: current.toExternalForm();
	}

	private static long initialSemanticRevision(GeoLocusV2 source) {
		return source.getPersistentLocusId() == null ? 1
				: Math.max(1, source.getSemanticRevision());
	}

	private static String initialConsumerIdentity(Construction construction,
			PersistentGeoId resultId, String suffix) {
		if (resultId == null) {
			if (!construction.isFileLoading()) {
				throw new IllegalArgumentException(
						"Public metric result identity is required");
			}
			return "g9u0-pending-metric-result" + suffix;
		}
		return resultId.toExternalForm() + suffix;
	}
}
