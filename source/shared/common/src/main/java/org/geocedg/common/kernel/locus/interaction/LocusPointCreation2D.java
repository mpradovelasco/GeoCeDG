/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.kernel.locus.interaction;

import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.kernel.geos.GeoPoint;

/**
 * Shared creation orchestration for a transient world request or existing reference.
 * Each established route keeps its own admission, evidence, budget and publication.
 */
public final class LocusPointCreation2D {

	/** The selected existing kernel protocol, never an additional admission grade. */
	public enum Route { R1_R6, R2_H1, INDIVIDUAL_FORWARD_ADDRESS_CREATION, REJECTED }

	/** Failures of the outer selection lifecycle; route failures retain their own types. */
	public enum Failure {
		INVALID_SOURCE, INVALID_REFERENCE, STALE_SELECTION, WRONG_THREAD,
		CONSUMED_SELECTION, FOREIGN_CHOICE, PUBLICATION_NOT_AUTHORIZED,
		R1_FORWARD_REJECTED, BUDGET_EXHAUSTED
	}

	/** A typed lifecycle refusal, before any publication by the selected route. */
	public static final class Rejected extends IllegalStateException {
		private static final long serialVersionUID = 1L;
		private final Failure failure;

		private Rejected(Failure failure) {
			super(failure.name());
			this.failure = failure;
		}

		private Rejected(Failure failure, IllegalArgumentException cause) {
			super(failure.name(), cause);
			this.failure = failure;
		}

		public Failure getFailure() {
			return failure;
		}
	}

	/** Query-owned ephemeral choice; only its forward address is durable. */
	public static final class Choice {
		private final Selection owner;
		private final LocusPointInteractionCandidate2D historical;
		private final M08SnapshotPlacement2D.Choice local;
		private final IndividualForwardPointCreation2D.Choice forward;

		private Choice(Selection owner, LocusPointInteractionCandidate2D historical,
				M08SnapshotPlacement2D.Choice local) {
			this.owner = owner;
			this.historical = historical;
			this.local = local;
			this.forward = null;
		}

		private Choice(Selection owner, IndividualForwardPointCreation2D.Choice forward) {
			this.owner = owner;
			this.historical = null;
			this.local = null;
			this.forward = forward;
		}

		public LocusSemanticAddress2D getAddress() {
			return historical != null ? historical.getAddress()
					: local != null ? local.getAddress() : forward.getAddress();
		}
	}

	private LocusPointCreation2D() {
	}

	/** Queries a copied world request; no helper reference point is constructed. */
	public static Selection query(GeoLocusV2 source, double x, double y,
			LocusPointInteractionPolicy2D policy) {
		requireSource(source);
		return new Selection(source, null, x, y, policy);
	}

	/** Reads Q transiently and requires its copied value to remain current until commit. */
	public static Selection queryFromReference(GeoLocusV2 source, GeoPoint reference,
			LocusPointInteractionPolicy2D policy) {
		requireSource(source);
		if (!validReference(source, reference)) {
			throw new Rejected(Failure.INVALID_REFERENCE);
		}
		return new Selection(source, reference, reference.getInhomX(),
				reference.getInhomY(), policy);
	}

	private static void requireSource(GeoLocusV2 source) {
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

	/** One coherent source/request selection, owned by one synchronous construction thread. */
	public static final class Selection implements AutoCloseable {
		private final GeoLocusV2 source;
		private final GeoPoint reference;
		private final double x;
		private final double y;
		private final LocusDefinition2D definition;
		private final long revision;
		private final SpatialIdentityRegistry registry;
		private final PersistentGeoId sourceId;
		private final Thread thread = Thread.currentThread();
		private final LocusSnapshotPlacement2D.Selection historical;
		private final LocusPointInteractionResult2D historicalResult;
		private M08SnapshotPlacement2D.Selection local;
		private IndividualForwardPointCreation2D.Selection forward;
		private List<Choice> choices = List.of();
		private Route route = Route.REJECTED;
		private String failure;
		private boolean finished;

		private Selection(GeoLocusV2 source, GeoPoint reference, double x, double y,
				LocusPointInteractionPolicy2D policy) {
			this.source = source;
			this.reference = reference;
			this.x = x;
			this.y = y;
			definition = source.getSemanticDefinition();
			revision = definition == null ? 0 : definition.getSemanticRevision();
			registry = source.getConstruction().getSpatialIdentityRegistry();
			sourceId = source.getPersistentLocusId();
			historical = LocusSnapshotPlacement2D.query(source, x, y, policy);
			historicalResult = historical.getResult();
			if (!current()) {
				historical.close();
				failure = Failure.STALE_SELECTION.name();
				return;
			}
			if (historicalResult.getStatus()
					== LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE
					|| historicalResult.getStatus()
							== LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES) {
				route = Route.R1_R6;
				choices = historicalResult.getCandidates().stream()
						.map(candidate -> new Choice(this, candidate, null)).toList();
				return;
			}
			historical.close();
			// This typed reason follows the unchanged R6 separation and safety checks.
			if (historicalResult.getStatus()
					!= LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH
					|| historicalResult.getReason()
							!= LocusPointInteractionResult2D.Reason.GLOBAL_UNIQUENESS_NOT_ESTABLISHED) {
				failure = historicalResult.getReason().name();
				return;
			}
			var remaining = remainingPolicy(policy);
			if (remaining == null) {
				failure = Failure.BUDGET_EXHAUSTED.name();
				return;
			}
			local = reference == null ? M08SnapshotPlacement2D.query(source, x, y, remaining)
					: M08SnapshotPlacement2D.queryFromReference(source, reference, remaining);
			if (local.getResult().getPermission()
					!= M08SnapshotPlacement2D.Permission.EXPLICIT_LOCAL_SELECTION_REQUIRED) {
				failure = local.getResult().getFailure().name() + " / "
						+ local.getResult().getLocalFailure();
				local.close();
				if (local.getResult().getFailure() == M08SnapshotPlacement2D.Failure.BUDGET_EXHAUSTED) {
					return;
				}
				// Independent permission and proof, never an upgraded historical result.
				remaining = remainingPolicy(policy);
				if (remaining == null) {
					failure = Failure.BUDGET_EXHAUSTED.name();
					return;
				}
				forward = reference == null
						? IndividualForwardPointCreation2D.query(source, x, y, remaining)
						: IndividualForwardPointCreation2D.queryFromReference(source, reference, remaining);
				if (forward.getPermission() == IndividualForwardPointCreation2D.Permission
						.EXPLICIT_FORWARD_SELECTION_REQUIRED && current()) {
					route = Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION;
					failure = null;
					choices = forward.getChoices().stream()
							.map(candidate -> new Choice(this, candidate)).toList();
				} else {
					failure = forward.getFailure().name();
					forward.close();
				}
				return;
			}
			if (!current()) {
				local.close();
				failure = Failure.STALE_SELECTION.name();
				return;
			}
			route = Route.R2_H1;
			choices = local.getResult().getChoices().stream()
					.map(candidate -> new Choice(this, null, candidate)).toList();
		}

		/** Debit completed attempts; a different permission never restarts the operation budget. */
		private LocusPointInteractionPolicy2D remainingPolicy(LocusPointInteractionPolicy2D policy) {
			var work = historicalResult.getInstrumentation();
			long semantic = historical.getSemanticMisses();
			long subdivisions = work.getSubdivisions();
			long candidates = work.getRetainedCandidates();
			if (local != null) {
				semantic += local.getWorkSnapshot().getSemanticMisses();
				subdivisions += local.getResult().getHistoricalResult().getInstrumentation()
						.getSubdivisions() + local.getWorkSnapshot().getIntervalPasses();
				candidates += local.getResult().getHistoricalResult().getInstrumentation()
						.getRetainedCandidates();
			}
			var budget = policy.getWorkBudget();
			long evaluationsLeft = budget.getMaximumSemanticEvaluations() - semantic;
			long subdivisionsLeft = budget.getMaximumSubdivisions() - subdivisions;
			long candidatesLeft = budget.getMaximumCandidates() - candidates;
			if (evaluationsLeft < 1 || subdivisionsLeft < 2 || candidatesLeft < 1) { return null; }
			return new LocusPointInteractionPolicy2D(policy.getMaximumWorldDistance(),
					policy.getEqualDistanceTolerance(), policy.getParameterTolerance(),
					new LocusPointInteractionWorkBudget2D((int) evaluationsLeft, (int) subdivisionsLeft,
							budget.getMaximumRefinementIterations(), (int) candidatesLeft));
		}

		public long getSemanticMisses() {
			return historical.getSemanticMisses()
					+ (local == null ? 0 : local.getWorkSnapshot().getSemanticMisses())
					+ (forward == null ? 0 : forward.getSemanticMisses());
		}

		private boolean current() {
			return definition != null && source.isDefined()
					&& source.getSemanticDefinition() == definition
					&& definition.getSemanticRevision() == revision
					&& source.getConstruction().getSpatialIdentityRegistry() == registry
					&& sourceId.equals(source.getPersistentLocusId())
					&& registry.getGeo(sourceId) == source
					&& !source.getConstruction().isFileLoading()
					&& (reference == null || validReference(source, reference)
							&& reference.getInhomX() == x && reference.getInhomY() == y);
		}

		public Route getRoute() {
			return route;
		}

		public List<Choice> getChoices() {
			return choices;
		}

		/** Presentation of the typed route cause; it is never parsed to decide admission. */
		public String getFailure() {
			return failure;
		}

		public LocusPointInteractionResult2D getHistoricalResult() {
			return historicalResult;
		}

		public M08SnapshotPlacement2D.Selection getLocalSelection() {
			return local;
		}

		public IndividualForwardPointCreation2D.Selection getForwardSelection() {
			return forward;
		}

		public M08SnapshotPlacement2D.Validity getValidity() {
			return finished ? M08SnapshotPlacement2D.Validity.CONSUMED
					: current() ? M08SnapshotPlacement2D.Validity.CURRENT
							: M08SnapshotPlacement2D.Validity.STALE;
		}

		/** Commits one explicit member once. The gesture owner stores one normal Undo. */
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
				if (choice.owner != this || !choices.contains(choice)) {
					throw new Rejected(Failure.FOREIGN_CHOICE);
				}
				if (!current()) {
					throw new Rejected(Failure.STALE_SELECTION);
				}
				if (!RuntimeFeatureService.mayCreateLocusV2(source.getConstruction())) {
					throw new Rejected(Failure.PUBLICATION_NOT_AUTHORIZED);
				}
				if (route == Route.R1_R6) {
					try {
						return historical.commit(label, choice.historical);
					} catch (IllegalArgumentException rejected) {
						throw new Rejected(Failure.R1_FORWARD_REJECTED, rejected);
					}
				}
				if (route == Route.R2_H1) {
					return local.commit(label, choice.local);
				}
				if (route == Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION) {
					return forward.commit(label, choice.forward);
				}
				throw new Rejected(Failure.PUBLICATION_NOT_AUTHORIZED);
			} finally {
				historical.close();
				if (local != null) {
					local.close();
				}
				if (forward != null) { forward.close(); }
			}
		}

		@Override
		public void close() {
			finished = true;
			historical.close();
			if (local != null) {
				local.close();
			}
			if (forward != null) { forward.close(); }
		}
	}
}
