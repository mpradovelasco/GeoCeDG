/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.kernel.locus.interaction;

import java.util.Objects;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geogebra.common.kernel.geos.GeoPoint;

/** CU7-I1 snapshot selection. Only the forward address survives publication. */
public final class LocusSnapshotPlacement2D {

	private LocusSnapshotPlacement2D() {
	}

	/** Copies a world request; no reference point becomes a construction parent. */
	public static Selection query(GeoLocusV2 source, double x, double y,
			LocusPointInteractionPolicy2D policy) {
		return new Selection(new LocusPointInteractionQuery2D(source, x, y, policy));
	}

	/** One current, single-use selection; never a persistent identity or recipe. */
	public static final class Selection implements AutoCloseable {
		private final GeoLocusV2 source;
		private final LocusDefinition2D definition;
		private final LocusPointInteractionResult2D result;
		private boolean finished;

		private Selection(LocusPointInteractionQuery2D query) {
			source = query.getSource();
			definition = source.getSemanticDefinition();
			result = new LocusPointInteractionResolver2D().resolve(query);
		}

		public LocusPointInteractionResult2D getResult() {
			return result;
		}

		/**
		 * Commits one explicit member of this safe result. Null means cancellation.
		 * The caller owns the normal single Undo boundary, as for forward creation.
		 */
		public GeoPoint commit(String label, LocusPointInteractionCandidate2D chosen) {
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
			// Object equality here checks an ephemeral revision, never durable identity.
			if (definition == null || !source.isDefined()
					|| !Objects.equals(source.getPersistentLocusId(),
							chosen.getAddress().getSourceLocusId())
					|| source.getConstruction().getSpatialIdentityRegistry()
							.getGeo(chosen.getAddress().getSourceLocusId()) != source
					|| source.getSemanticDefinition() != definition) {
				throw new IllegalArgumentException("CU7_STALE_SELECTION");
			}
			return LocusV2PublicOperations.createInteractiveSemanticPoint(
					source.getConstruction(), label, source, chosen);
		}

		@Override
		public void close() {
			finished = true;
		}
	}
}
