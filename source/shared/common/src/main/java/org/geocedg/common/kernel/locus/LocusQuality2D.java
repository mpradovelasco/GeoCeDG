/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationMethod;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.RepresentationRole;

/** Four independent quality axes for one semantic result. */
public final class LocusQuality2D {
	private final ConstructionFidelity constructionFidelity;
	private final EvaluationMethod evaluationMethod;
	private final RepresentationRole representationRole;
	private final NumericGuarantee numericGuarantee;

	/** Creates immutable quality metadata. */
	public LocusQuality2D(ConstructionFidelity constructionFidelity,
			EvaluationMethod evaluationMethod, RepresentationRole representationRole,
			NumericGuarantee numericGuarantee) {
		this.constructionFidelity = Objects.requireNonNull(constructionFidelity);
		this.evaluationMethod = Objects.requireNonNull(evaluationMethod);
		this.representationRole = Objects.requireNonNull(representationRole);
		this.numericGuarantee = Objects.requireNonNull(numericGuarantee);
	}

	/** @return the normative default for analytic double evaluation */
	public static LocusQuality2D analyticDoubleSemantic() {
		return new LocusQuality2D(ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
				EvaluationMethod.ANALYTIC_EVALUATION,
				RepresentationRole.SEMANTIC_RESULT,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED);
	}

	public ConstructionFidelity getConstructionFidelity() {
		return constructionFidelity;
	}

	public EvaluationMethod getEvaluationMethod() {
		return evaluationMethod;
	}

	public RepresentationRole getRepresentationRole() {
		return representationRole;
	}

	public NumericGuarantee getNumericGuarantee() {
		return numericGuarantee;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusQuality2D)) {
			return false;
		}
		LocusQuality2D quality = (LocusQuality2D) other;
		return constructionFidelity == quality.constructionFidelity
				&& evaluationMethod == quality.evaluationMethod
				&& representationRole == quality.representationRole
				&& numericGuarantee == quality.numericGuarantee;
	}

	@Override
	public int hashCode() {
		return Objects.hash(constructionFidelity, evaluationMethod,
				representationRole, numericGuarantee);
	}
}
