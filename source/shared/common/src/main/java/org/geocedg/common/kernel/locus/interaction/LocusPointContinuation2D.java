/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.kernel.locus.interaction;

import java.util.Collections;
import java.util.Objects;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedAffineLocus2D;
import org.geocedg.common.kernel.locus.LocusComponentSelectorResolver2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D.SeamSide;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D;
import org.geocedg.common.kernel.locus.intersection.LocalContinuationEvidence2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPositionBinder2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.kernel.geos.GeoPoint;

/** Independent local continuation permission; global R6 results are never promoted. */
public final class LocusPointContinuation2D {

	/** Opaque gesture-local chart choice; each update still needs a fresh complete proof. */
	public static final class Context {
		private final GeoPoint point;
		private final GeoLocusV2 source;
		private final LocusDefinition2D definition;
		private final Thread thread = Thread.currentThread();
		private LocusSemanticAddress2D expectedAddress;
		private int coordinate = -1;
		private boolean stopped;

		/** Captures the current point/source identity without adding any durable state. */
		private Context(GeoPoint point, GeoLocusV2 source, LocusSemanticAddress2D current) {
			this.point = point;
			this.source = source;
			this.definition = source.getSemanticDefinition();
			this.expectedAddress = current;
		}

		/** Rejects stale, foreign or separately edited points before geometric work. */
		public boolean matches(GeoPoint currentPoint, GeoLocusV2 currentSource,
				LocusSemanticAddress2D current) {
			return !stopped && Thread.currentThread() == thread && point == currentPoint && source == currentSource
					&& definition == currentSource.getSemanticDefinition()
					&& expectedAddress != null && expectedAddress.equals(current);
		}

		public boolean isStopped() { return stopped; }
		public void stop() { stopped = true; }

		/** Advances only after the exact selected address was published by the host. */
		public void recordPublished(GeoPoint currentPoint, Result result) {
			if (!matches(currentPoint, result.preparedSource, result.originatingAddress)
					|| !result.isEstablished() || result.preparedBinding != null
					|| !result.address.equals(AlgoSemanticLocusPoint2D.requireSemanticParent(currentPoint)
							.getCurrentSemanticAddress())) {
				throw new IllegalStateException("Continuation context did not publish its edit");
			}
			expectedAddress = result.address;
			if (result.coordinate >= 0) {
				coordinate = result.coordinate;
			}
		}
	}

	/** Local permission and typed fail-closed outcomes. */
	public enum Status {
		CONTINUATION_ESTABLISHED, NO_CONTINUATION_ESTABLISHED, INVALID_SOURCE,
		AUTHORITY_NOT_ESTABLISHED, COMPONENT_NOT_ESTABLISHED, FIXED_POINT,
		BUDGET_EXHAUSTED, FORWARD_REJECTED, CONTINUATION_STOPPED, UNSUPPORTED_HOST_STATE
	}

	/** Transient local proof result; its address alone is durable. */
	public static final class Result {
		private final Status status;
		private final LocusSemanticAddress2D address;
		private final String method;
		private final long semanticEvaluations;
		private final long intervalPasses;
		private final LocusPointInteractionResult2D historical;
		private GeoLocusV2 preparedSource;
		private LocusDefinition2D preparedDefinition;
		private LocusSemanticAddress2D originatingAddress;
		private MetricPositionBinding2D preparedBinding;
		private int coordinate = -1;
		private Context context;
		private SpatialIdentityRegistry preparedRegistry;
		private DocumentAuthorityRecord preparedAuthority;
		private PersistentGeoId pointId;

		private Result(Status status, LocusSemanticAddress2D address, String method,
				long semanticEvaluations, long intervalPasses,
				LocusPointInteractionResult2D historical) {
			this.status = status;
			this.address = address;
			this.method = method;
			this.semanticEvaluations = semanticEvaluations;
			this.intervalPasses = intervalPasses;
			this.historical = historical;
		}

		public boolean isEstablished() { return status == Status.CONTINUATION_ESTABLISHED; }
		public boolean hasChanged() {
			return isEstablished() && !address.equals(originatingAddress);
		}
		public Status getStatus() { return status; }
		public LocusSemanticAddress2D getAddress() { return address; }
		public String getMethod() { return method; }
		public String getDiagnostic() { return isEstablished() ? method : status.name(); }
		public long getSemanticEvaluations() { return semanticEvaluations; }
		public long getIntervalPasses() { return intervalPasses; }
		public LocusPointInteractionResult2D getHistoricalResult() { return historical; }
		/** A host refusal retains the actual proof work already performed. */
		public Result unsupportedHost() {
			return new Result(Status.UNSUPPORTED_HOST_STATE, null, method,
					semanticEvaluations, intervalPasses, historical);
		}
		public NumericGuarantee getProximityGuarantee() {
			return NumericGuarantee.FLOATING_POINT_UNCERTIFIED;
		}

		private Result prepared(GeoLocusV2 source, LocusDefinition2D definition,
				LocusSemanticAddress2D current, MetricPositionBinding2D binding, Context context) {
			preparedSource = source;
			preparedDefinition = definition;
			originatingAddress = current;
			preparedBinding = binding;
			this.context = context;
			preparedRegistry = source.getConstruction().getSpatialIdentityRegistry();
			preparedAuthority = preparedRegistry.getDocumentAuthorityRecord();
			pointId = context == null ? null : preparedRegistry.getPersistentGeoId(context.point);
			return this;
		}

		/** Consumes the counted forward binding once, only for its exact current edit. */
		public MetricPositionBinding2D takePreparedBinding(GeoPoint point, GeoLocusV2 source,
				LocusSemanticAddress2D current) {
			if (!isEstablished() || preparedBinding == null || source != preparedSource
					|| source.getSemanticDefinition() != preparedDefinition
					|| !originatingAddress.equals(current) || context == null
					|| !context.matches(point, source, current)) {
				throw new IllegalStateException("Continuation binding is stale, foreign or consumed");
			}
			MetricPositionBinding2D binding = preparedBinding;
			preparedBinding = null;
			return binding;
		}

		/** Rechecks identity/revision/authority inside the rollback-capable transaction. */
		public void requireCurrentPublication(GeoPoint point) {
			if (context == null || preparedRegistry != point.getConstruction().getSpatialIdentityRegistry()
					|| preparedRegistry.getGeo(pointId) != point
					|| preparedRegistry.getGeo(address.getSourceLocusId()) != preparedSource
					|| !preparedSource.isDefined()
					|| preparedDefinition.getDefinitionStatus() != DefinitionStatus.VALID
					|| preparedSource.getSemanticDefinition() != preparedDefinition
					|| !Objects.equals(preparedAuthority, preparedRegistry.getDocumentAuthorityRecord())
					|| point.isLocked()
					|| !context.matches(point, preparedSource, originatingAddress)) {
				throw new IllegalStateException("Continuation publication lost its source authority or identity");
			}
		}
	}

	private LocusPointContinuation2D() {
	}

	/** Starts an opaque context; publication separately enforces the interaction role. */
	public static Context begin(GeoPoint point) {
		AlgoSemanticLocusPoint2D parent = AlgoSemanticLocusPoint2D.requireSemanticParent(point);
		return new Context(point, parent.getSource(), parent.getCurrentSemanticAddress());
	}

	/** A host guard refusal, before any geometric work or construction mutation. */
	public static Result refused(Status status) {
		return new Result(status, null, "", 0, 0, null);
	}

	/** Resolves a current address by historical complete evidence or a focal local chart. */
	public static Result resolve(GeoLocusV2 source, LocusSemanticAddress2D current,
			double x, double y, LocusPointInteractionPolicy2D policy) {
		return resolve(source, current, x, y, policy, null);
	}

	/** Same operation retaining a fixed coordinate chart throughout one host gesture. */
	public static Result resolve(GeoLocusV2 source, LocusSemanticAddress2D current,
			double x, double y, LocusPointInteractionPolicy2D policy, Context context) {
		LocusDefinition2D definition = source.getSemanticDefinition();
		if (current == null || definition == null || !source.isDefined()
				|| definition.getDefinitionStatus() != DefinitionStatus.VALID
				|| !Double.isFinite(x) || !Double.isFinite(y)
				|| !current.getSourceLocusId().equals(source.getPersistentLocusId())
				|| source.getConstruction().getSpatialIdentityRegistry()
						.getGeo(current.getSourceLocusId()) != source
				|| !current.getProviderVersion().equals(definition.getProvider().getProviderId())) {
			return refused(Status.INVALID_SOURCE);
		}
		Object capability = definition.getEvaluatorCapability();
		boolean historical = capability instanceof PiecewisePolynomialLocus2D
				|| capability instanceof CertifiedAffineLocus2D
				&& ((CertifiedAffineLocus2D) capability).supportsCertifiedAffine(definition);
		if (historical) {
			var limits = policy.getWorkBudget();
			try (var session = LocusEvaluationSession2D.hardBoundedForSnapshot(
					limits.getMaximumSemanticEvaluations(), limits.getMaximumSemanticEvaluations(),
					limits.getMaximumEvaluatorCompositionDepth())) {
				LocusPointInteractionResult2D result = new LocusPointInteractionResolver2D()
						.discoverForSnapshot(new LocusPointInteractionQuery2D(source, x, y,
								policy, current), session).historicalResult;
				LocusPointInteractionCandidate2D candidate = result.getUniqueCandidate();
				if (candidate == null) {
					return new Result(result.getReason() == LocusPointInteractionResult2D.Reason.BUDGET_EXHAUSTED
							? Status.BUDGET_EXHAUSTED : Status.NO_CONTINUATION_ESTABLISHED, null,
							"R6_CURRENT_COMPONENT", session.getTotalChargedMisses(), 0, result);
				}
				try {
					MetricPositionBinding2D binding = new LocusMetricPositionBinder2D().bind(
							candidate.getAddress().toMetricPosition(), definition, session);
					if (!binding.isValid() || session.isWorkLimitExhausted()) {
						return refused(session.isWorkLimitExhausted()
								? Status.BUDGET_EXHAUSTED : Status.FORWARD_REJECTED);
					}
					return new Result(Status.CONTINUATION_ESTABLISHED, candidate.getAddress(),
							"R6_CURRENT_COMPONENT", session.getTotalChargedMisses(), 0, result)
							.prepared(source, definition, current, binding, context);
				} catch (LocusEvaluationSession2D.EvaluationWorkLimitException exception) {
					return new Result(Status.BUDGET_EXHAUSTED, null, "",
							session.getTotalChargedMisses(), 0, result);
				}
			}
		}
		if (source.getConstruction().getSpatialIdentityRegistry().getDocumentAuthorityRecord() != null) {
			return refused(Status.AUTHORITY_NOT_ESTABLISHED);
		}
		var branch = definition.getBranch(current.getBranchKey());
		if (branch == null) {
			return refused(Status.COMPONENT_NOT_ESTABLISHED);
		}
		var component = LocusComponentSelectorResolver2D.resolve(current.getComponentLineageKey(),
				branch.getExistenceStructure(), Collections.singletonList(branch.getLineage()),
				current.getCanonicalParameter(), 0);
		if (!component.getStatus().isResolved()) {
			return refused(Status.COMPONENT_NOT_ESTABLISHED);
		}
		var budget = policy.getWorkBudget();
		long[] passes = {0};
		try (var session = LocusEvaluationSession2D.hardBoundedForSnapshot(
				budget.getMaximumSemanticEvaluations(), budget.getMaximumSemanticEvaluations(),
				budget.getMaximumEvaluatorCompositionDepth())) {
			try {
				LocalContinuationEvidence2D.Proposal[] proposal = {null};
				LocusEvaluationSession2D.runWithNestedServiceParent(session,
						LocusEvaluationSession2D.newSliceScope(), () -> proposal[0] =
								LocalContinuationEvidence2D.establish(definition, current,
										component.getComponent(), x, y, policy,
										context == null ? -1 : context.coordinate, () -> {
											if (passes[0] >= budget.getMaximumSubdivisions()) {
												throw new ProofBudgetExceeded();
											}
											passes[0]++;
										}));
				if (session.isWorkLimitExhausted()) {
					throw new ProofBudgetExceeded();
				}
				if (proposal[0] == null) {
					return new Result(Status.NO_CONTINUATION_ESTABLISHED, null, "",
							session.getTotalChargedMisses(), passes[0], null);
				}
				double parameter = proposal[0].getParameter();
				if (!component.getComponent().contains(parameter, 0)
						|| Double.doubleToLongBits(parameter) != Double.doubleToLongBits(
								definition.getProvider().canonicalize(parameter))) {
					return refused(Status.COMPONENT_NOT_ESTABLISHED);
				}
				SeamSide seam = !definition.getProvider().isPeriodic() ? SeamSide.NOT_PERIODIC
						: parameter == definition.getProvider().getDeclaredDomain().getLower()
								? proposal[0].getLift() == 0 ? SeamSide.LOWER_APPROACH
										: SeamSide.UPPER_APPROACH : SeamSide.INTERIOR;
				LocusSemanticAddress2D address = new LocusSemanticAddress2D(current.getSourceLocusId(),
						current.getProviderVersion(), current.getBranchKey(), component.getLineageKey(),
						parameter, proposal[0].getLift(), seam);
				var binding = new LocusMetricPositionBinder2D().bind(address.toMetricPosition(),
						definition, session);
				var point = binding.getEvaluatedPoint().orElse(null);
				if (session.isWorkLimitExhausted()) {
					throw new ProofBudgetExceeded();
				}
				boolean accepted = source.getSemanticDefinition() == definition && binding.isValid()
						&& point != null && Math.hypot(point.getX() - x, point.getY() - y)
								<= policy.getMaximumWorldDistance();
				Result result = new Result(accepted ? Status.CONTINUATION_ESTABLISHED : Status.FORWARD_REJECTED,
						accepted ? address : null, proposal[0].getMethod(),
						session.getTotalChargedMisses(), passes[0], null)
						.prepared(source, definition, current, accepted ? binding : null, context);
				result.coordinate = proposal[0].getCoordinate();
				return result;
			} catch (ProofBudgetExceeded | LocusEvaluationSession2D.EvaluationWorkLimitException exception) {
				return new Result(Status.BUDGET_EXHAUSTED, null, "",
						session.getTotalChargedMisses(), passes[0], null);
			}
		}
	}

	private static final class ProofBudgetExceeded extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
}
