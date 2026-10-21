/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;

/** Read-only, viewport-independent point evaluator for one semantic snapshot. */
public interface LocusEvaluator2D {
	/** Diagnostic prefix of an excluded end whose one-sided limit is not evaluated. */
	String ONE_SIDED_LIMIT_NOT_AVAILABLE = "ONE_SIDED_LIMIT_NOT_AVAILABLE";
	/** Diagnostic prefix of a certified root whose image the limit form does not enclose. */
	String LIMIT_FORM_NOT_AVAILABLE = "LIMIT_FORM_NOT_AVAILABLE";

	/**
	 * Evaluates one already validated, provider-canonical parameter.
	 *
	 * @return typed semantic evaluation
	 */
	LocusEvaluation2D evaluate(LocusDefinition2D definition,
			LocusBranch2D branch, double canonicalParameter,
			LocusEvaluationSession2D session);

	/**
	 * Evaluates the one-sided limit of the traced point at an excluded end of a certified
	 * component ({@code locus-v2-existence-components.md} section 15.3). An excluded end is
	 * never the image of a canonical parameter: it is reached only through this explicit
	 * query. An evaluator without the capability fails explicitly; it never evaluates the
	 * canonical parameter of the end and never extrapolates.
	 *
	 * @param component certified component of the branch, whose end is excluded
	 * @param upperEnd whether the end is the upper end of an interval or the end of an arc,
	 *        approached from below; otherwise the lower end or the start, approached from
	 *        above
	 * @return typed evaluation of the limit
	 */
	default LocusEvaluation2D evaluateOneSidedLimit(LocusDefinition2D definition,
			LocusBranch2D branch, LocusExistenceStructure2D.ContinuousComponent component,
			boolean upperEnd, LocusEvaluationSession2D session) {
		return LocusEvaluation2D.invalid(EvaluationStatus.EVALUATION_FAILED,
				branch.getQuality(), ONE_SIDED_LIMIT_NOT_AVAILABLE
						+ ": the evaluator has no one-sided limit capability");
	}

	/**
	 * Encloses the image of a certified root that belongs to a component of the branch, a
	 * transition end or a singular parameter, with the limit form of the certificate
	 * (PRE-G9B-CU2, certification design section 3.4). It serves representation only, where
	 * the evaluator does not define the point at the root; an evaluator without the
	 * capability fails explicitly.
	 *
	 * @param parameterLower lower bound of the certified enclosure of the root parameter
	 * @param parameterUpper upper bound of the certified enclosure of the root parameter
	 * @param source structural source of the root
	 * @return enclosed image, or an unavailable one with its typed reason
	 */
	default LocusBoundaryImage2D encloseBoundaryImage(LocusDefinition2D definition,
			LocusBranch2D branch, double parameterLower, double parameterUpper,
			LocusBoundarySource2D source) {
		return LocusBoundaryImage2D.notAvailable(LIMIT_FORM_NOT_AVAILABLE
				+ ": the evaluator has no limit form");
	}
}
