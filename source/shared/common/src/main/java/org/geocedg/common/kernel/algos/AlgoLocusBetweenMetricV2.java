/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.List;

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
import org.geocedg.common.kernel.locus.metric.LocusMetricResults2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricDiagnostic2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.locus.metric.OpenBoundaryPolicy;
import org.geocedg.common.kernel.locus.metric.SamePositionPolicy;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;
import org.geocedg.common.kernel.locus.metric.TraversalOutcome;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;

/** Reconstructible public rich metric between two exact semantic positions. */
public final class AlgoLocusBetweenMetricV2 extends AlgoElement {
	private static final String CAPABILITY =
			"g9s1-public-evaluator-route-metric/v1";

	private final GeoLocusV2 source;
	private final GeoPoint start;
	private final GeoPoint target;
	private final GeoLocusMetricResult result;
	private final LocusMetricPolicy2D policy =
			LocusMetricPolicy2D.publicExperimental();
	private final LocusMetricCapabilityHierarchy2D capabilities =
			new LocusMetricCapabilityHierarchy2D(List.of(
					new DifferentialLocusMetricCapability2D(
							"g9s1-public-semantic-differential/v1"),
					EvaluatorOnlyLocusMetricCapability2D
							.withDirectRouteRefinement(CAPABILITY)));
	private final LocusMetricEngine2D engine = new LocusMetricEngine2D();
	private LocusMetricOwnerLease2D ownerLease;

	/** Creates and publishes one public between-position rich query. */
	public AlgoLocusBetweenMetricV2(Construction construction, String label,
			GeoLocusV2 source, GeoPoint start, GeoPoint target,
			PersistentGeoId resultId) {
		super(construction);
		this.source = java.util.Objects.requireNonNull(source);
		this.start = java.util.Objects.requireNonNull(start);
		this.target = java.util.Objects.requireNonNull(target);
		this.result = new GeoLocusMetricResult(construction,
				initialSourceIdentity(source));
		if (resultId == null && !construction.isFileLoading()) {
			throw new IllegalArgumentException(
					"Public metric result identity is required");
		}
		result.enablePublicPersistence();
		setProtectedInput(true);
		setInputOutput();
		compute();
		result.setLabel(label);
	}

	@Override
	protected void setInputOutput() {
		input = new GeoElement[] {source, start, target};
		setOnlyOutput(result);
		setDependencies();
	}

	@Override
	public void compute() {
		if (source.getPersistentLocusId() == null
				|| cons.getSpatialIdentityRegistry().getPersistentGeoId(result)
						== null) {
			result.setUndefined();
			return;
		}
		ensureOwnerLease();
		long revision = Math.max(1, source.getSemanticRevision());
		result.refreshSourceLocusIdentity(source.getLocusIdentity());
		result.beginMetricRevision(revision);
		LocusDefinition2D definition = source.getSemanticDefinition();
		if (definition == null || !source.isDefined()) {
			publishFailure(revision, MetricComputationStatus.INVALID_QUERY,
					"Source locus has no current semantic definition");
			return;
		}
		if (!start.isDefined() || !target.isDefined()) {
			publishFailure(revision, MetricComputationStatus.INVALID_QUERY,
					"Both endpoints must be current defined semantic points");
			return;
		}
		try {
			AlgoSemanticLocusPoint2D startParent =
					AlgoSemanticLocusPoint2D.requireSemanticParent(start);
			AlgoSemanticLocusPoint2D targetParent =
					AlgoSemanticLocusPoint2D.requireSemanticParent(target);
			if (startParent.getSource() != source
					|| targetParent.getSource() != source) {
				publishFailure(revision, MetricComputationStatus.INVALID_QUERY,
						"Both endpoints must be exact positions on this locus");
				return;
			}
			MetricPositionBinding2D startBinding =
					startParent.bindCurrentPosition();
			MetricPositionBinding2D targetBinding =
					targetParent.bindCurrentPosition();
			if (startBinding == null || targetBinding == null) {
				publishFailure(revision, MetricComputationStatus.INVALID_QUERY,
						"An endpoint has no current semantic address");
				return;
			}
			BetweenPositionsMetricQuery query = new BetweenPositionsMetricQuery(
					startBinding, targetBinding, TraversalDirection.FORWARD,
					OpenBoundaryPolicy.STRICT, SamePositionPolicy.ZERO_LENGTH,
					policy);
			ownerLease.getOwner().invalidateObsoleteRevision(revision);
			result.publishMetricResult(revision, engine.compute(query, definition,
					capabilities, ownerLease.getOwner(),
					LocusMetricIndexMode.LAZY_COMPONENT_REVISION,
					source.getMetricInstrumentation(), currentConsumerIdentity()));
		} catch (LocusMetricComponentBuildException exception) {
			result.publishMetricFailure(revision, LocusMetricResults2D.failure(
					source.getLocusIdentity(), revision, policy,
					exception.getComputationStatus(),
					java.util.Optional.of(
							TraversalOutcome.TARGET_NOT_REACHABLE),
					exception.getDiagnostics()));
		} catch (IllegalArgumentException exception) {
			publishFailure(revision, MetricComputationStatus.INVALID_QUERY,
					"Between-position metric requires exact semantic addresses");
		} catch (RuntimeException exception) {
			publishFailure(revision, MetricComputationStatus.NUMERICAL_FAILURE,
					"Between-position metric failed: "
							+ exception.getClass().getSimpleName());
		}
	}

	public GeoLocusMetricResult getResult() {
		return result;
	}

	@Override
	public Commands getClassName() {
		return Commands.LocusLength;
	}

	@Override
	public void remove() {
		if (ownerLease != null) {
			ownerLease.close();
			ownerLease = null;
		}
		super.remove();
	}

	private void publishFailure(long revision,
			MetricComputationStatus status, String diagnostic) {
		result.publishMetricFailure(revision, LocusMetricResults2D.failure(
				source.getLocusIdentity(), revision, policy, status,
				java.util.Optional.of(TraversalOutcome.TARGET_NOT_REACHABLE),
				List.of(new MetricDiagnostic2D(
						status == MetricComputationStatus.NUMERICAL_FAILURE
								? MetricDiagnosticCode2D.NUMERICAL_FAILURE
								: MetricDiagnosticCode2D.P1_FAILURE,
						diagnostic))));
	}

	private String currentConsumerIdentity() {
		PersistentGeoId current = cons.getSpatialIdentityRegistry()
				.getPersistentGeoId(result);
		if (current == null) {
			throw new IllegalStateException(
					"Public metric result has no attached durable identity");
		}
		return current.toExternalForm() + "/between-metric-consumer";
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

	private static String initialSourceIdentity(GeoLocusV2 source) {
		PersistentGeoId current = java.util.Objects.requireNonNull(source)
				.getPersistentLocusId();
		return current == null ? "g9u0-pending-locus"
				: current.toExternalForm();
	}
}
