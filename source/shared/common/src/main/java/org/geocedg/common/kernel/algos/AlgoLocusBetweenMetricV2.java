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
import org.geocedg.common.kernel.locus.metric.LocusMetricPositionBinder2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResults2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricDiagnostic2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.locus.metric.PublicLocusMetricTraversalPolicy2D;
import org.geocedg.common.kernel.locus.metric.TraversalOutcome;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spline.SplineConstructorOccurrenceResolver2D;
import org.geocedg.common.kernel.spline.SplineConstructorOccurrenceResult2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;

/** Reconstructible public rich metric between two exact semantic positions. */
public final class AlgoLocusBetweenMetricV2 extends AlgoElement {
	private static final String CAPABILITY =
			"g9s1-public-evaluator-route-metric/v1";

	private final GeoLocusV2 source;
	private final GeoPoint start;
	private final GeoPoint target;
	private final GeoText directionInput;
	private final GeoText boundaryPolicyInput;
	private final GeoText samePositionPolicyInput;
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
	private final LocusMetricPositionBinder2D positionBinder =
			new LocusMetricPositionBinder2D();
	private final SplineConstructorOccurrenceResolver2D occurrenceResolver =
			new SplineConstructorOccurrenceResolver2D();
	private LocusMetricOwnerLease2D ownerLease;
	private String retainedStartOccurrenceKey;
	private String retainedTargetOccurrenceKey;

	/** Creates and publishes one public between-position rich query. */
	public AlgoLocusBetweenMetricV2(Construction construction, String label,
			GeoLocusV2 source, GeoPoint start, GeoPoint target,
			PersistentGeoId resultId) {
		this(construction, label, source, start, target, null, null, null,
				resultId);
	}

	/** Creates a public rich query with an explicit traversal policy. */
	public AlgoLocusBetweenMetricV2(Construction construction, String label,
			GeoLocusV2 source, GeoPoint start, GeoPoint target,
			GeoText directionInput, GeoText boundaryPolicyInput,
			GeoText samePositionPolicyInput, PersistentGeoId resultId) {
		super(construction);
		this.source = java.util.Objects.requireNonNull(source);
		this.start = java.util.Objects.requireNonNull(start);
		this.target = java.util.Objects.requireNonNull(target);
		this.directionInput = directionInput;
		this.boundaryPolicyInput = boundaryPolicyInput;
		this.samePositionPolicyInput = samePositionPolicyInput;
		if ((directionInput == null) != (boundaryPolicyInput == null)
				|| (directionInput == null) != (samePositionPolicyInput == null)) {
			throw new IllegalArgumentException(
					"The explicit traversal policy requires all three tokens");
		}
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
		input = directionInput == null
				? new GeoElement[] {source, start, target}
				: new GeoElement[] {source, start, target, directionInput,
						boundaryPolicyInput, samePositionPolicyInput};
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
					"Both endpoints must have current semantic endpoint authority");
			return;
		}
		try {
			PublicLocusMetricTraversalPolicy2D traversalPolicy =
					currentTraversalPolicy();
			EndpointResolution startResolution = resolveEndpoint(start,
					retainedStartOccurrenceKey);
			EndpointResolution targetResolution = resolveEndpoint(target,
					retainedTargetOccurrenceKey);
			if (!startResolution.isValid() || !targetResolution.isValid()) {
				publishFailure(revision, MetricComputationStatus.INVALID_QUERY,
						!startResolution.isValid()
								? "Start endpoint: " + startResolution.diagnostic
								: "Target endpoint: " + targetResolution.diagnostic);
				return;
			}
			retainedStartOccurrenceKey = startResolution.occurrenceKey;
			retainedTargetOccurrenceKey = targetResolution.occurrenceKey;
			MetricPositionBinding2D startBinding = startResolution.binding;
			MetricPositionBinding2D targetBinding = targetResolution.binding;
			if (startBinding == null || targetBinding == null) {
				publishFailure(revision, MetricComputationStatus.INVALID_QUERY,
						"An endpoint has no current semantic address");
				return;
			}
			BetweenPositionsMetricQuery query = new BetweenPositionsMetricQuery(
					startBinding, targetBinding, traversalPolicy.getDirection(),
					traversalPolicy.getBoundaryPolicy(),
					traversalPolicy.getSamePositionPolicy(), policy);
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
					"Invalid between-position metric request: "
							+ exception.getMessage());
		} catch (RuntimeException exception) {
			publishFailure(revision, MetricComputationStatus.NUMERICAL_FAILURE,
					"Between-position metric failed: "
							+ exception.getClass().getSimpleName());
		}
	}

	private PublicLocusMetricTraversalPolicy2D currentTraversalPolicy() {
		if (directionInput == null) {
			return PublicLocusMetricTraversalPolicy2D.parse(
					PublicLocusMetricTraversalPolicy2D.FORWARD,
					PublicLocusMetricTraversalPolicy2D.STRICT,
					PublicLocusMetricTraversalPolicy2D.ZERO_LENGTH);
		}
		if (!directionInput.isDefined() || !boundaryPolicyInput.isDefined()
				|| !samePositionPolicyInput.isDefined()) {
			throw new IllegalArgumentException(
					"Traversal policy tokens must be defined text values");
		}
		return PublicLocusMetricTraversalPolicy2D.parse(
				directionInput.getTextString(), boundaryPolicyInput.getTextString(),
				samePositionPolicyInput.getTextString());
	}

	private EndpointResolution resolveEndpoint(GeoPoint endpoint,
			String retainedOccurrenceKey) {
		if (endpoint.getParentAlgorithm() instanceof AlgoSemanticLocusPoint2D) {
			AlgoSemanticLocusPoint2D semanticParent =
					(AlgoSemanticLocusPoint2D) endpoint.getParentAlgorithm();
			if (semanticParent.getSource() == source) {
				MetricPositionBinding2D binding =
						semanticParent.bindCurrentPosition();
				return binding != null && binding.isValid()
						? EndpointResolution.semantic(binding)
						: EndpointResolution.invalid(
								"Explicit semantic address is not current");
			}
		}
		SplineConstructorOccurrenceResult2D provenance =
				occurrenceResolver.resolve(source, endpoint);
		if (provenance.getStatus()
				!= SplineConstructorOccurrenceResult2D.Status.UNIQUE) {
			return EndpointResolution.invalid(provenance.getStatus() + ": "
					+ provenance.getDiagnostic());
		}
		SplineConstructorOccurrenceResult2D.Match match =
				provenance.getUniqueMatch();
		if (retainedOccurrenceKey != null
				&& !retainedOccurrenceKey.equals(match.getOccurrenceKey())) {
			return EndpointResolution.invalid(
					"Constructor occurrence identity changed; retargeting is forbidden");
		}
		MetricPositionBinding2D binding = positionBinder.bind(
				match.getAddress().toMetricPosition(), source.getSemanticDefinition());
		return binding.isValid()
				? EndpointResolution.occurrence(binding, match.getOccurrenceKey())
				: EndpointResolution.invalid(
						"Constructor occurrence address is not current");
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

	private static final class EndpointResolution {
		private final MetricPositionBinding2D binding;
		private final String occurrenceKey;
		private final String diagnostic;

		private EndpointResolution(MetricPositionBinding2D binding,
				String occurrenceKey, String diagnostic) {
			this.binding = binding;
			this.occurrenceKey = occurrenceKey;
			this.diagnostic = diagnostic;
		}

		private static EndpointResolution semantic(
				MetricPositionBinding2D binding) {
			return new EndpointResolution(binding, null, null);
		}

		private static EndpointResolution occurrence(
				MetricPositionBinding2D binding, String key) {
			return new EndpointResolution(binding, key, null);
		}

		private static EndpointResolution invalid(String diagnostic) {
			return new EndpointResolution(null, null, diagnostic);
		}

		private boolean isValid() {
			return binding != null && binding.isValid();
		}
	}
}
