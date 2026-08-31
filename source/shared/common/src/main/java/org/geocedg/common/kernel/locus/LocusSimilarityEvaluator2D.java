/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;

/** Immutable evaluator of {@code T(L(u))} over one coherent source revision. */
public final class LocusSimilarityEvaluator2D implements LocusEvaluator2D {
	private final LocusDefinition2D sourceDefinition;
	private final LocusSimilarityTransform2D transform;
	private final String signature;

	/** Captures the exact immutable source snapshot and finite transformation. */
	public LocusSimilarityEvaluator2D(LocusDefinition2D sourceDefinition,
			LocusSimilarityTransform2D transform) {
		this.sourceDefinition = Objects.requireNonNull(sourceDefinition);
		this.transform = Objects.requireNonNull(transform);
		signature = "locus-similarity-evaluator/v1|source="
				+ sourceDefinition.getLocusIdentity() + "@"
				+ sourceDefinition.getSemanticRevision() + "|"
				+ transform.getSemanticSignature();
	}

	@Override
	public LocusEvaluation2D evaluate(LocusDefinition2D definition,
			LocusBranch2D branch, double canonicalParameter,
			LocusEvaluationSession2D session) {
		LocusEvaluation2D source = sourceDefinition.evaluate(branch.getBranchKey(),
				canonicalParameter, session);
		if (!source.isValid()) {
			return LocusEvaluation2D.invalid(source.getStatus(),
					transformedQuality(source.getQuality()), source.getDiagnostic());
		}
		try {
			return LocusEvaluation2D.valid(transform.transform(source.getPoint()),
					transform.isCollapsed() ? Regularity.SINGULAR
							: source.getRegularity(),
					transformedQuality(source.getQuality()));
		} catch (IllegalArgumentException exception) {
			return LocusEvaluation2D.invalid(EvaluationStatus.NON_FINITE,
					transformedQuality(source.getQuality()),
					"Similarity image is nonfinite");
		}
	}

	/** @return stable signature of the captured source revision and transform */
	public String getEvaluatorSignature() {
		return signature;
	}

	private static LocusQuality2D transformedQuality(LocusQuality2D source) {
		return new LocusQuality2D(source.getConstructionFidelity(),
				source.getEvaluationMethod(), source.getRepresentationRole(),
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED);
	}
}
