/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.kernel.locus.interaction;

import java.util.Objects;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.kernel.geos.GeoPoint;

/** CU7-I1 snapshot selection. Only the forward address survives publication. */
public final class LocusSnapshotPlacement2D {

	private LocusSnapshotPlacement2D() {
	}

	/** Copies a world request; no reference point becomes a construction parent. */
	public static Selection query(GeoLocusV2 source, double x, double y,
			LocusPointInteractionPolicy2D policy) {
		Objects.requireNonNull(source);
		var id = source.getPersistentLocusId();
		if (id == null || source.getConstruction().getSpatialIdentityRegistry()
				.getGeo(id) != source) {
			throw new IllegalArgumentException("CU7_STALE_SELECTION");
		}
		return new Selection(new LocusPointInteractionQuery2D(source, x, y, policy));
	}

	/** One current, single-use selection; never a persistent identity or recipe. */
	public static final class Selection implements AutoCloseable {
		private final GeoLocusV2 source;
		private final LocusDefinition2D definition;
		private final SpatialIdentityRegistry registry;
		private final DocumentAuthorityRecord authority;
		private final LocusPointInteractionResult2D result;
		private final long semanticMisses;
		private boolean finished;

		private Selection(LocusPointInteractionQuery2D query) {
			source = query.getSource();
			definition = source.getSemanticDefinition();
			registry = source.getConstruction().getSpatialIdentityRegistry();
			authority = registry.getDocumentAuthorityRecord();
			var budget = query.getPolicy().getWorkBudget();
			// Same R6 session policy and result; separately retain actual nested work.
			try (var session = LocusEvaluationSession2D.memoizingWithLimits(
					budget.getMaximumSemanticEvaluations(), budget.getMaximumSemanticEvaluations(),
					budget.getMaximumEvaluatorCompositionDepth())) {
				result = new LocusPointInteractionResolver2D().resolveForPlacement(query, session);
				semanticMisses = session.getTotalChargedMisses();
			}
		}

		public LocusPointInteractionResult2D getResult() {
			return result;
		}

		/** Actual root and nested charge; does not alter historical R6 result telemetry. */
		public long getSemanticMisses() { return semanticMisses; }

		/**
		 * Commits one explicit member of this safe result. Null means cancellation.
		 * The caller owns the normal single Undo boundary, as for forward creation.
		 */
		public GeoPoint commit(String label, LocusPointInteractionCandidate2D chosen) {
			return commit(label, chosen, () -> { });
		}

		/**
		 * Adds a caller-owned liveness guard to the existing publication transaction.
		 * The guard can only refuse an operation; it grants no geometric permission.
		 */
		public GeoPoint commit(String label, LocusPointInteractionCandidate2D chosen,
				Runnable finalGuard) {
			Objects.requireNonNull(finalGuard);
			if (finished) {
				throw new IllegalStateException("CU7_SELECTION_CONSUMED");
			}
			finished = true;
			if (chosen == null) {
				return null;
			}
			if (result.getStatus() != LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE
					&& result.getStatus()
							!= LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES) {
				throw new IllegalArgumentException("CU7_SELECTION_NOT_ADMISSIBLE");
			}
			if (!result.getCandidates().contains(chosen)) {
				throw new IllegalArgumentException("CU7_SELECTION_NOT_IN_QUERY");
			}
			requireCurrent(chosen);
			finalGuard.run();
			return LocusPointPublication2D.publishAtomically(source, chosen.getAddress(),
					() -> LocusV2PublicOperations.createInteractiveSemanticPoint(
							source.getConstruction(), label, source, chosen),
					() -> {
						requireCurrent(chosen);
						finalGuard.run();
					}, () -> new IllegalArgumentException("CU7_FORWARD_MODEL_MISMATCH"));
		}

		private void requireCurrent(LocusPointInteractionCandidate2D chosen) {
			// Object equality here checks an ephemeral revision, never durable identity.
			if (definition == null || !source.isDefined()
					|| !Objects.equals(source.getPersistentLocusId(),
							chosen.getAddress().getSourceLocusId())
					|| source.getConstruction().getSpatialIdentityRegistry() != registry
					|| registry.getGeo(chosen.getAddress().getSourceLocusId()) != source
					|| !Objects.equals(authority, registry.getDocumentAuthorityRecord())
					|| source.getConstruction().isFileLoading()
					|| source.getSemanticDefinition() != definition) {
				throw new IllegalArgumentException("CU7_STALE_SELECTION");
			}
		}

		@Override
		public void close() {
			finished = true;
		}
	}
}
