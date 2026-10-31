/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.kernel.locus.interaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusComponentSelectorResolver2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.M08LocalAddressEvidence2D;
import org.geocedg.common.kernel.locus.intersection.M08LocalAddressEvidence2D.Work;
import org.geocedg.common.kernel.locus.metric.LocusMetricPositionBinder2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.kernel.geos.GeoPoint;

/**
 * Experimental M08-S only: individual forward address evidence and explicit choice.
 * No new R6 admission, continuation, distance minimum, persistent recipe or Q parent.
 */
public final class M08SnapshotPlacement2D {

	/** New permission; historical R6 coverage is reported independently. */
	public enum Permission { NOT_OFFERABLE, EXPLICIT_LOCAL_SELECTION_REQUIRED }

	/** This operation never enumerates the continuum of valid nearby addresses. */
	public enum GlobalCoverage { NOT_ESTABLISHED }

	/** Liveness is independent of the immutable evidence obtained during the query. */
	public enum Validity { CURRENT, STALE, CONSUMED }

	/** Typed refusal or lifecycle failure. No candidate count/string implies safety. */
	public enum Failure {
		NONE, INVALID_SOURCE, INVALID_REFERENCE, AUTHORITY_NOT_ESTABLISHED,
		UNSAFE_SEARCH, BUDGET_EXHAUSTED, NO_VERIFIED_ADDRESS, COMPONENT_NOT_ESTABLISHED,
		LOCAL_EVIDENCE_NOT_ESTABLISHED, FORWARD_MODEL_MISMATCH, STALE_SELECTION,
		FOREIGN_CHOICE, CONSUMED_SELECTION, WRONG_THREAD, PUBLICATION_NOT_AUTHORIZED
	}

	/** Immutable counters: semantic misses, interval passes and publication are distinct units. */
	public static final class WorkSnapshot {
		private final long semanticMisses;
		private final long discoveryMisses;
		private final long[] intervalPasses;
		private final long semanticLimit;
		private final long intervalLimit;
		private final long publications;

		private WorkSnapshot(Selection owner) {
			semanticMisses = owner.session.getTotalChargedMisses();
			discoveryMisses = owner.discoveryMisses;
			intervalPasses = owner.intervalPasses.clone();
			semanticLimit = owner.query.getPolicy().getWorkBudget().getMaximumSemanticEvaluations();
			intervalLimit = owner.query.getPolicy().getWorkBudget().getMaximumSubdivisions();
			publications = owner.publications;
		}

		public long getSemanticMisses() {
			return semanticMisses;
		}
		public long getDiscoveryMisses() {
			return discoveryMisses;
		}
		public long getSemanticLimit() {
			return semanticLimit;
		}
		public long getIntervalLimit() {
			return intervalLimit;
		}
		public long getPublicationAttempts() {
			return publications;
		}
		public long getRemainingSemanticMisses() {
			return semanticLimit - semanticMisses;
		}
		public long getIntervalPasses(Work kind) {
			return intervalPasses[kind.ordinal()];
		}
		public long getIntervalPasses() {
			long total = 0;
			for (long value : intervalPasses) {
				total += value;
			}
			return total;
		}
	}

	/** Typed rejection without mutation. */
	public static final class Rejected extends IllegalStateException {
		private static final long serialVersionUID = 1L;
		private final Failure failure;

		private Rejected(Failure failure) {
			super("M08_" + failure.name());
			this.failure = failure;
		}

		public Failure getFailure() {
			return failure;
		}
	}

	/** Immutable query evidence; no uniqueness or minimum assertion is made by this type. */
	public static final class Result {
		private final LocusPointInteractionResult2D historical;
		private final Failure failure;
		private final List<Choice> choices;
		private final M08LocalAddressEvidence2D.Status localFailure;

		private Result(LocusPointInteractionResult2D historical, Failure failure,
				List<Choice> choices, M08LocalAddressEvidence2D.Status localFailure) {
			this.historical = historical;
			this.failure = failure;
			this.choices = Collections.unmodifiableList(new ArrayList<>(choices));
			this.localFailure = localFailure;
		}

		public LocusPointInteractionResult2D getHistoricalResult() {
			return historical;
		}

		public Failure getFailure() {
			return failure;
		}

		public M08LocalAddressEvidence2D.Status getLocalFailure() {
			return localFailure;
		}

		public GlobalCoverage getGlobalCoverage() {
			return GlobalCoverage.NOT_ESTABLISHED;
		}

		public Permission getPermission() {
			return failure == Failure.NONE && !choices.isEmpty()
					? Permission.EXPLICIT_LOCAL_SELECTION_REQUIRED : Permission.NOT_OFFERABLE;
		}

		public List<Choice> getChoices() {
			return choices;
		}
	}

	/** Unforgeable query-owned choice. Its reference is an ephemeral capability, not identity. */
	public static final class Choice {
		private final Selection owner;
		private final LocusSemanticAddress2D address;
		private final M08LocalAddressEvidence2D evidence;

		private Choice(Selection owner, LocusSemanticAddress2D address,
				M08LocalAddressEvidence2D evidence) {
			this.owner = owner;
			this.address = address;
			this.evidence = evidence;
		}

		public LocusSemanticAddress2D getAddress() {
			return address;
		}

		public M08LocalAddressEvidence2D getEvidence() {
			return evidence;
		}

		/** Kernel factory handshake; cannot be used outside the owning commit call. */
		public LocusSemanticAddress2D takePublicationAddress(GeoLocusV2 source) {
			if (source != owner.source || owner.publicationReady != this
					|| Thread.currentThread() != owner.thread) {
				throw new Rejected(Failure.PUBLICATION_NOT_AUTHORIZED);
			}
			owner.publicationReady = null;
			return address;
		}

		/** One-use initial binding from the normal forward binder, prepared before any ID. */
		public MetricPositionBinding2D takePreparedBinding(GeoLocusV2 source) {
			if (source != owner.source || owner.initialBindingReady != this
					|| Thread.currentThread() != owner.thread || !owner.current()) {
				throw new Rejected(Failure.PUBLICATION_NOT_AUTHORIZED);
			}
			MetricPositionBinding2D binding = owner.preparedBinding;
			owner.initialBindingReady = null;
			owner.preparedBinding = null;
			return binding;
		}
	}

	private M08SnapshotPlacement2D() {
	}

	/** Copies a world request. The policy is immutable and never becomes point identity. */
	public static Selection query(GeoLocusV2 source, double x, double y,
			LocusPointInteractionPolicy2D policy) {
		requireSourceBinding(source);
		return new Selection(new LocusPointInteractionQuery2D(source, x, y, policy), null);
	}

	/** Reads a real existing Q transiently, without making it participate durably. */
	public static Selection queryFromReference(GeoLocusV2 source, GeoPoint reference,
			LocusPointInteractionPolicy2D policy) {
		requireSourceBinding(source);
		if (!validReference(source, reference)) {
			throw new Rejected(Failure.INVALID_REFERENCE);
		}
		return new Selection(new LocusPointInteractionQuery2D(source, reference.getInhomX(),
				reference.getInhomY(), policy), reference);
	}

	private static void requireSourceBinding(GeoLocusV2 source) {
		if (source == null || source.getPersistentLocusId() == null
				|| source.getConstruction().getSpatialIdentityRegistry()
						.getGeo(source.getPersistentLocusId()) != source) {
			throw new Rejected(Failure.INVALID_SOURCE);
		}
	}

	private static boolean validReference(GeoLocusV2 source, GeoPoint reference) {
		return reference != null && reference.getConstruction() == source.getConstruction()
				&& source.getConstruction().getGeoSetConstructionOrder().contains(reference)
				&& reference.isDefined() && Double.isFinite(reference.getInhomX())
				&& Double.isFinite(reference.getInhomY());
	}

	/** One document/source revision and one synchronous construction-thread owner. */
	public static final class Selection implements AutoCloseable {
		private final LocusPointInteractionQuery2D query;
		private final GeoLocusV2 source;
		private final GeoPoint reference;
		private final LocusDefinition2D definition;
		private final SpatialIdentityRegistry registry;
		private final PersistentGeoId sourceId;
		private final DocumentAuthorityRecord authority;
		private final Thread thread = Thread.currentThread();
		private final Result result;
		private final LocusEvaluationSession2D session;
		private final long[] intervalPasses = new long[Work.values().length];
		private long discoveryMisses;
		private long publications;
		private boolean finished;
		private Choice publicationReady;
		private Choice initialBindingReady;
		private MetricPositionBinding2D preparedBinding;

		private Selection(LocusPointInteractionQuery2D query, GeoPoint reference) {
			this.query = query;
			this.reference = reference;
			source = query.getSource();
			definition = source.getSemanticDefinition();
			registry = source.getConstruction().getSpatialIdentityRegistry();
			sourceId = source.getPersistentLocusId();
			authority = registry.getDocumentAuthorityRecord();
			var budget = query.getPolicy().getWorkBudget();
			session = LocusEvaluationSession2D.hardBoundedForSnapshot(
					budget.getMaximumSemanticEvaluations(), budget.getMaximumSemanticEvaluations(),
					budget.getMaximumEvaluatorCompositionDepth());
			var discovery = new LocusPointInteractionResolver2D().discoverForSnapshot(query, session);
			discoveryMisses = session.getTotalChargedMisses();
			var historical = discovery.historicalResult;
			List<Choice> choices = new ArrayList<>();
			Failure failure = Failure.NONE;
			M08LocalAddressEvidence2D.Status localFailure = null;
			if (!current()) {
				failure = Failure.INVALID_SOURCE;
			} else if (authority != null) {
				// No implicit transfer from captured to constructive configuration.
				failure = Failure.AUTHORITY_NOT_ESTABLISHED;
			} else if (session.isWorkLimitExhausted()
					|| historical.getReason() == LocusPointInteractionResult2D.Reason.BUDGET_EXHAUSTED) {
				failure = Failure.BUDGET_EXHAUSTED;
			} else if (!discovery.noObservedBarrier) {
				failure = Failure.UNSAFE_SEARCH;
			} else if (historical.getCandidates().isEmpty()) {
				failure = Failure.NO_VERIFIED_ADDRESS;
			} else {
				try {
					for (var seed : historical.getCandidates()) {
						var proof = prove(seed.getAddress());
						if (proof == null || !proof.isEstablished()) {
							localFailure = proof == null ? null : proof.getStatus();
							failure = proof == null ? Failure.COMPONENT_NOT_ESTABLISHED
									: Failure.LOCAL_EVIDENCE_NOT_ESTABLISHED;
							break;
						}
						if (prepareForward(seed.getAddress(), proof) == null) {
							failure = Failure.FORWARD_MODEL_MISMATCH;
							break;
						}
						choices.add(new Choice(this, seed.getAddress(), proof));
					}
				} catch (Rejected rejected) {
					if (rejected.getFailure() != Failure.BUDGET_EXHAUSTED) {
						throw rejected;
					}
					failure = Failure.BUDGET_EXHAUSTED;
				}
			}
			if (failure == Failure.NONE && !current()) {
				failure = Failure.STALE_SELECTION;
			}
			result = new Result(historical, failure,
					failure == Failure.NONE ? choices : Collections.emptyList(), localFailure);
			if (failure != Failure.NONE) {
				session.close();
			}
		}

		private M08LocalAddressEvidence2D prove(LocusSemanticAddress2D address) {
			var provider = definition.getProvider();
			if (!address.getSourceLocusId().equals(source.getPersistentLocusId())
					|| !address.getProviderVersion().equals(provider.getProviderId())
					|| Double.doubleToLongBits(provider.canonicalize(address.getCanonicalParameter()))
							!= Double.doubleToLongBits(address.getCanonicalParameter())) {
				return null;
			}
			var branch = definition.getBranch(address.getBranchKey());
			if (branch == null) {
				return null;
			}
			var component = LocusComponentSelectorResolver2D.resolve(address.getComponentLineageKey(),
					branch.getExistenceStructure(), Collections.singletonList(branch.getLineage()),
					address.getCanonicalParameter());
			if (!component.getStatus().isResolved()
					|| !component.getComponent().contains(Math.nextDown(address.getCanonicalParameter()), 0)
					|| !component.getComponent().contains(Math.nextUp(address.getCanonicalParameter()), 0)) {
				return null;
			}
			return M08LocalAddressEvidence2D.establish(definition, address.getBranchKey(),
					address.getCanonicalParameter(), component.getComponent().getInterval(),
					query.getTargetX(), query.getTargetY(), query.getPolicy().getMaximumWorldDistance(),
					this::chargeIntervalPass);
		}

		private void chargeIntervalPass(Work kind) {
			long total = 0;
			for (long count : intervalPasses) {
				total += count;
			}
			if (total >= query.getPolicy().getWorkBudget().getMaximumSubdivisions()) {
				throw new Rejected(Failure.BUDGET_EXHAUSTED);
			}
			intervalPasses[kind.ordinal()]++;
		}

		private MetricPositionBinding2D prepareForward(LocusSemanticAddress2D address,
				M08LocalAddressEvidence2D proof) {
			// Revalidate by actual work, retaining the accumulated root counters.
			session.clear();
			MetricPositionBinding2D binding;
			try {
				binding = new LocusMetricPositionBinder2D().bind(address.toMetricPosition(),
						definition, session);
			} catch (LocusEvaluationSession2D.EvaluationWorkLimitException exhausted) {
				throw new Rejected(Failure.BUDGET_EXHAUSTED);
			}
			if (session.isWorkLimitExhausted()) {
				throw new Rejected(Failure.BUDGET_EXHAUSTED);
			}
			var point = binding.getEvaluatedPoint().orElse(null);
			return binding.isValid() && proof.encloses(point)
					&& Math.hypot(point.getX() - query.getTargetX(), point.getY() - query.getTargetY())
							<= query.getPolicy().getMaximumWorldDistance() ? binding : null;
		}

		private boolean current() {
			return definition != null && source.isDefined() && sourceId != null
					&& source.getConstruction().getSpatialIdentityRegistry() == registry
					&& registry.getGeo(sourceId) == source
					&& sourceId.equals(source.getPersistentLocusId())
					&& source.getSemanticDefinition() == definition
					&& definition.getDefinitionStatus()
							== org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus.VALID
					&& Objects.equals(authority, registry.getDocumentAuthorityRecord())
					&& !source.getConstruction().isFileLoading();
		}

		private boolean referenceCurrent() {
			return reference == null || validReference(source, reference)
					&& reference.getInhomX() == query.getTargetX()
					&& reference.getInhomY() == query.getTargetY();
		}

		public Validity getValidity() {
			return finished ? Validity.CONSUMED
					: current() && referenceCurrent() ? Validity.CURRENT : Validity.STALE;
		}

		public LocusPointInteractionPolicy2D getPolicy() {
			return query.getPolicy();
		}

		public Result getResult() {
			return result;
		}

		/** @return actual accumulated work, including confirmation; not the historical R6 counters */
		public WorkSnapshot getWorkSnapshot() {
			return new WorkSnapshot(this);
		}

		/** Commits an explicitly chosen member once. Caller owns the single normal Undo. */
		public GeoPoint commit(String label, Choice choice) {
			if (Thread.currentThread() != thread) {
				throw new Rejected(Failure.WRONG_THREAD);
			}
			if (finished) {
				throw new Rejected(Failure.CONSUMED_SELECTION);
			}
			finished = true;
			try {
				if (choice == null) {
					return null;
				}
				if (choice.owner != this || !result.choices.contains(choice)) {
					throw new Rejected(Failure.FOREIGN_CHOICE);
				}
				if (!current() || !referenceCurrent()) {
					throw new Rejected(Failure.STALE_SELECTION);
				}
				var proof = prove(choice.address);
				preparedBinding = proof == null || !proof.isEstablished() ? null
						: prepareForward(choice.address, proof);
				if (preparedBinding == null) {
					throw new Rejected(Failure.LOCAL_EVIDENCE_NOT_ESTABLISHED);
				}
				if (!current() || !referenceCurrent()) {
					throw new Rejected(Failure.STALE_SELECTION);
				}
				publicationReady = choice;
				initialBindingReady = choice;
				publications++;
				return LocusV2PublicOperations.createSnapshotSemanticPoint(
						source.getConstruction(), label, source, choice);
			} finally {
				publicationReady = null;
				initialBindingReady = null;
				preparedBinding = null;
				session.close();
			}
		}

		@Override
		public void close() {
			finished = true;
			publicationReady = null;
			initialBindingReady = null;
			preparedBinding = null;
			session.close();
		}
	}
}
