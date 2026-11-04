/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus.interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusComponentSelectorResolver2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.metric.LocusMetricPositionBinder2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geocedg.common.kernel.locus.NativeTransactionalState2D.UnsupportedReason;

/** Separate, explicitly selected forward creation permission; never an R6/H1 result upgrade. */
public final class IndividualForwardPointCreation2D {

	public enum Permission { NOT_OFFERABLE, EXPLICIT_FORWARD_SELECTION_REQUIRED }

	public enum Proximity { FLOATING_POINT_UNCERTIFIED }

	public enum Failure {
		NONE, INVALID_SOURCE, INVALID_REFERENCE, UNSUPPORTED_CAPTURE, AUTHORITY_NOT_ESTABLISHED,
		UNSAFE_DISCOVERY, COMPONENT_NOT_ESTABLISHED, INVALID_FORWARD, BUDGET_EXHAUSTED,
		NO_ADDRESS_DISCOVERED, STALE_SELECTION, WRONG_THREAD, CONSUMED_SELECTION,
		UNSUPPORTED_HOST_STATE,
		FOREIGN_CHOICE, PUBLICATION_NOT_AUTHORIZED
	}

	public static final class Rejected extends IllegalStateException {
		private final Failure failure;
		private final UnsupportedReason nativeHostReason;
		private Rejected(Failure failure) {
			this(failure, null);
		}
		private Rejected(Failure failure, UnsupportedReason nativeHostReason) {
			super("INDIVIDUAL_FORWARD_" + failure.name());
			this.failure = failure;
			this.nativeHostReason = nativeHostReason;
		}
		public Failure getFailure() { return failure; }
		public UnsupportedReason getNativeHostReason() { return nativeHostReason; }
		public String getDiagnostic() {
			return nativeHostReason == UnsupportedReason.SOURCE_FIDELITY_RESOURCE_EXHAUSTED
					? nativeHostReason.name() : failure.name();
		}
	}

	/** Immutable structural/forward evidence, separate from the numerical proximity grade. */
	public static final class Choice extends LocusPointPublication2D {
		private final Selection owner;
		private final LocusSemanticAddress2D address;
		private final double residual;
		private Choice(Selection owner, LocusSemanticAddress2D address, double residual) {
			this.owner = owner;
			this.address = address;
			this.residual = residual;
		}
		public LocusSemanticAddress2D getAddress() { return address; }
		public Proximity getProximity() { return Proximity.FLOATING_POINT_UNCERTIFIED; }
		public double getComputedWorldResidual() { return residual; }
		@Override
		public LocusSemanticAddress2D takePublicationAddress(GeoLocusV2 source) {
			if (source != owner.source || owner.publicationReady != this
					|| Thread.currentThread() != owner.thread || !owner.current()) {
				throw new Rejected(Failure.PUBLICATION_NOT_AUTHORIZED);
			}
			owner.publicationReady = null;
			return address;
		}
		@Override
		public MetricPositionBinding2D takePreparedBinding(GeoLocusV2 source) {
			if (source != owner.source || owner.bindingReady != this
					|| Thread.currentThread() != owner.thread || !owner.current()) {
				throw new Rejected(Failure.PUBLICATION_NOT_AUTHORIZED);
			}
			owner.bindingReady = null;
			var binding = owner.preparedBinding;
			owner.preparedBinding = null;
			return binding;
		}
	}

	private IndividualForwardPointCreation2D() { }

	public static Selection query(GeoLocusV2 source, double x, double y,
			LocusPointInteractionPolicy2D policy) {
		return new Selection(source, null, x, y, policy);
	}

	public static Selection queryFromReference(GeoLocusV2 source, GeoPoint reference,
			LocusPointInteractionPolicy2D policy) {
		if (!validReference(source, reference)) { throw new Rejected(Failure.INVALID_REFERENCE); }
		return new Selection(source, reference, reference.getInhomX(), reference.getInhomY(), policy);
	}

	private static boolean validReference(GeoLocusV2 source, GeoPoint reference) {
		return source != null && reference != null
				&& reference.getConstruction() == source.getConstruction()
				&& source.getConstruction().getGeoSetConstructionOrder().contains(reference)
				&& reference.isDefined() && Double.isFinite(reference.getInhomX())
				&& Double.isFinite(reference.getInhomY());
	}

	public static final class Selection implements AutoCloseable {
		private final GeoLocusV2 source;
		private final GeoPoint reference;
		private final LocusPointInteractionQuery2D query;
		private final LocusDefinition2D definition;
		private final SpatialIdentityRegistry registry;
		private final DocumentAuthorityRecord authority;
		private final Thread thread = Thread.currentThread();
		private final LocusEvaluationSession2D session;
		private final long scope = LocusEvaluationSession2D.newSliceScope();
		private List<Choice> choices = List.of();
		private Failure failure = Failure.NONE;
		private long discoveryMisses;
		private long publications;
		private boolean finished;
		private Choice publicationReady;
		private Choice bindingReady;
		private MetricPositionBinding2D preparedBinding;

		private Selection(GeoLocusV2 source, GeoPoint reference, double x, double y,
				LocusPointInteractionPolicy2D policy) {
			if (source == null) { throw new Rejected(Failure.INVALID_SOURCE); }
			this.source = source;
			this.reference = reference;
			query = new LocusPointInteractionQuery2D(source, x, y, policy);
			definition = source.getSemanticDefinition();
			registry = source.getConstruction().getSpatialIdentityRegistry();
			authority = registry.getDocumentAuthorityRecord();
			var budget = policy.getWorkBudget();
			session = LocusEvaluationSession2D.hardBoundedForSnapshot(
					budget.getMaximumSemanticEvaluations(), budget.getMaximumSemanticEvaluations(),
					budget.getMaximumEvaluatorCompositionDepth());
			try {
				requireCapability();
				var discovery = withParent(() -> new LocusPointInteractionResolver2D()
						.discoverForForwardAddress(query, session));
				discoveryMisses = session.getTotalChargedMisses();
				if (session.isWorkLimitExhausted() || discovery.historicalResult.getReason()
						== LocusPointInteractionResult2D.Reason.BUDGET_EXHAUSTED) {
					throw new Rejected(Failure.BUDGET_EXHAUSTED);
				}
				if (!discovery.noObservedBarrier) { throw new Rejected(Failure.UNSAFE_DISCOVERY); }
				var offered = new ArrayList<Choice>();
				for (var seed : discovery.addresses) {
					var binding = validate(seed);
					var point = binding.getEvaluatedPoint().orElseThrow();
					offered.add(new Choice(this, seed, Math.hypot(
							point.getX() - x, point.getY() - y)));
				}
				if (offered.isEmpty()) { throw new Rejected(Failure.NO_ADDRESS_DISCOVERED); }
				if (!current()) { throw new Rejected(Failure.STALE_SELECTION); }
				choices = List.copyOf(offered);
			} catch (Rejected rejected) {
				failure = rejected.getFailure();
				session.close();
			} catch (LocusEvaluationSession2D.EvaluationWorkLimitException exhausted) {
				failure = Failure.BUDGET_EXHAUSTED;
				session.close();
			}
		}

		private void requireCapability() {
			if (!current()) { throw new Rejected(Failure.INVALID_SOURCE); }
			if (registry.getDocumentAuthorityRecord() != null) {
				throw new Rejected(Failure.AUTHORITY_NOT_ESTABLISHED);
			}
			if (!(definition.getEvaluatorCapability() instanceof ReconstructibleLocusEvaluator2D)
					|| !((ReconstructibleLocusEvaluator2D) definition.getEvaluatorCapability())
							.captureCertifiedProgramV3().isPresent()
					|| !((ReconstructibleLocusEvaluator2D) definition.getEvaluatorCapability())
							.getFidelityReport().isFaithful()) {
				throw new Rejected(Failure.UNSUPPORTED_CAPTURE);
			}
			if (definition.getBranches().stream().anyMatch(branch -> branch.getExistenceStructure()
					.getCompleteness() != Completeness.COMPLETE)) {
				throw new Rejected(Failure.COMPONENT_NOT_ESTABLISHED);
			}
		}

		private MetricPositionBinding2D validate(LocusSemanticAddress2D address) {
			requireCapability();
			var provider = definition.getProvider();
			var branch = definition.getBranch(address.getBranchKey());
			if (!source.getPersistentLocusId().equals(address.getSourceLocusId())
					|| !provider.getProviderId().equals(address.getProviderVersion()) || branch == null
					|| Double.doubleToLongBits(provider.canonicalize(address.getCanonicalParameter()))
							!= Double.doubleToLongBits(address.getCanonicalParameter())) {
				throw new Rejected(Failure.INVALID_FORWARD);
			}
			var component = LocusComponentSelectorResolver2D.resolve(address.getComponentLineageKey(),
					branch.getExistenceStructure(), List.of(branch.getLineage()),
					address.getCanonicalParameter());
			if (!component.getStatus().isResolved()) {
				throw new Rejected(Failure.COMPONENT_NOT_ESTABLISHED);
			}
			// Clear cached values for real confirmation work; the cumulative charge stays intact.
			session.clear();
			var binding = withParent(() -> new LocusMetricPositionBinder2D()
					.bind(address.toMetricPosition(), definition, session));
			if (session.isWorkLimitExhausted()) { throw new Rejected(Failure.BUDGET_EXHAUSTED); }
			var point = binding.getEvaluatedPoint().orElse(null);
			double residual = point == null ? Double.NaN : Math.hypot(
					point.getX() - query.getTargetX(), point.getY() - query.getTargetY());
			if (!binding.isValid() || !Double.isFinite(residual)
					|| residual > query.getPolicy().getMaximumWorldDistance()) {
				throw new Rejected(Failure.INVALID_FORWARD);
			}
			return binding;
		}

		private <T> T withParent(Supplier<T> operation) {
			class Holder { private T value; }
			var holder = new Holder();
			LocusEvaluationSession2D.runWithNestedServiceParent(session, scope,
					() -> holder.value = operation.get());
			return holder.value;
		}

		private boolean current() {
			return definition != null && source.isDefined()
					&& source.getSemanticDefinition() == definition
					&& definition.getDefinitionStatus() == DefinitionStatus.VALID
					&& source.getPersistentLocusId() != null
					&& source.getConstruction().getSpatialIdentityRegistry() == registry
					&& registry.getGeo(source.getPersistentLocusId()) == source
					&& java.util.Objects.equals(authority, registry.getDocumentAuthorityRecord())
					&& !source.getConstruction().isFileLoading()
					&& (reference == null || validReference(source, reference)
							&& reference.getInhomX() == query.getTargetX()
							&& reference.getInhomY() == query.getTargetY());
		}

		public Permission getPermission() {
			return failure == Failure.NONE && !choices.isEmpty()
					? Permission.EXPLICIT_FORWARD_SELECTION_REQUIRED : Permission.NOT_OFFERABLE;
		}
		public Failure getFailure() { return failure; }
		public List<Choice> getChoices() { return choices; }
		public long getSemanticMisses() { return session.getTotalChargedMisses(); }
		public long getDiscoveryMisses() { return discoveryMisses; }
		public long getPublicationAttempts() { return publications; }
		public M08SnapshotPlacement2D.Validity getValidity() {
			return finished ? M08SnapshotPlacement2D.Validity.CONSUMED
					: current() ? M08SnapshotPlacement2D.Validity.CURRENT
							: M08SnapshotPlacement2D.Validity.STALE;
		}

		public GeoPoint commit(String label, Choice choice) {
			return commit(label, choice, () -> { });
		}

		/** The facade contributes freshness to this same concrete publication transaction. */
		GeoPoint commit(String label, Choice choice, Runnable finalGuard) {
			if (Thread.currentThread() != thread) { throw new Rejected(Failure.WRONG_THREAD); }
			if (finished) { throw new Rejected(Failure.CONSUMED_SELECTION); }
			finished = true;
			try {
				if (choice == null) { return null; }
				if (choice.owner != this || !choices.contains(choice)) {
					throw new Rejected(Failure.FOREIGN_CHOICE);
				}
				if (!current()) { throw new Rejected(Failure.STALE_SELECTION); }
				var hostReason = LocusV2PublicOperations.getUnsupportedInteractiveHostStateReason(
						source.getConstruction());
				if (hostReason != null) {
					throw new Rejected(Failure.UNSUPPORTED_HOST_STATE, hostReason);
				}
				preparedBinding = validate(choice.address);
				if (!current()) { throw new Rejected(Failure.STALE_SELECTION); }
				publicationReady = choice;
				bindingReady = choice;
				publications++;
				return LocusPointPublication2D.publishAtomically(source, choice.address,
						() -> LocusV2PublicOperations.createIndividualForwardSemanticPoint(
								source.getConstruction(), label, source, choice),
						() -> {
							if (!current()) { throw new Rejected(Failure.STALE_SELECTION); }
							finalGuard.run();
						}, () -> new Rejected(Failure.INVALID_FORWARD));
			} catch (LocusEvaluationSession2D.EvaluationWorkLimitException exhausted) {
				throw new Rejected(Failure.BUDGET_EXHAUSTED);
			} finally { close(); }
		}

		@Override
		public void close() {
			finished = true;
			publicationReady = null;
			bindingReady = null;
			preparedBinding = null;
			session.close();
		}
	}
}
