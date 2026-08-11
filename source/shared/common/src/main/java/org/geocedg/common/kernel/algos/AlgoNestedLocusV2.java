/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusParameterMap2D;
import org.geocedg.common.kernel.locus.LocusPointTransform2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Internal V2-on-V2 algorithm. It recursively consumes only the upstream
 * immutable semantic evaluator through the same scoped session.
 */
public final class AlgoNestedLocusV2 extends AlgoLocusV2 {
	private final String locusIdentity;
	private final GeoLocusV2 upstream;
	private final String upstreamBranchKey;
	private final LocusDriverDomainProvider2D provider;
	private final List<LocusBranch2D> branches;
	private final LocusParameterMap2D parameterMap;
	private final LocusPointTransform2D pointTransform;
	private final String compositionSignature;

	/** Creates one normal DAG edge from the upstream V2 output. */
	public AlgoNestedLocusV2(Construction construction, String locusIdentity,
			GeoLocusV2 upstream, String upstreamBranchKey,
			LocusDriverDomainProvider2D provider, List<LocusBranch2D> branches,
			LocusParameterMap2D parameterMap, LocusPointTransform2D pointTransform,
			String compositionSignature) {
		super(construction, locusIdentity, new GeoElement[] {upstream});
		this.locusIdentity = locusIdentity;
		this.upstream = upstream;
		this.upstreamBranchKey = upstreamBranchKey;
		this.provider = provider;
		this.branches = branches;
		this.parameterMap = parameterMap;
		this.pointTransform = pointTransform;
		this.compositionSignature = compositionSignature;
		publishInitialSnapshot();
	}

	@Override
	protected LocusDefinition2D createCandidate(long candidateRevision) {
		final LocusDefinition2D upstreamDefinition = upstream.getSemanticDefinition();
		DefinitionStatus status = upstreamDefinition == null
				? DefinitionStatus.DRIVER_INVALID
				: upstreamDefinition.getDefinitionStatus();
		String upstreamSignature = upstreamDefinition == null ? "unpublished"
				: upstreamDefinition.getLocusIdentity() + "@"
						+ upstreamDefinition.getSemanticRevision();
		return new LocusDefinition2D(locusIdentity, candidateRevision, status,
				provider, branches, (definition, branch, parameter, session) -> {
					getLocus().getInstrumentation().recordRevisionConsumption();
					double upstreamParameter = parameterMap.map(parameter);
					LocusEvaluation2D upstreamResult = upstreamDefinition.evaluate(
							upstreamBranchKey, upstreamParameter, session);
					if (!upstreamResult.isValid()) {
						return LocusEvaluation2D.invalid(upstreamResult.getStatus(),
								branch.getQuality(), "Upstream " + upstreamSignature
										+ ": " + upstreamResult.getDiagnostic());
					}
					return LocusEvaluation2D.valid(pointTransform.transform(parameter,
							upstreamResult.getPoint()), Regularity.UNKNOWN,
							branch.getQuality());
				}, Determinism.POINTWISE_DETERMINISTIC,
				compositionSignature + "|upstream=" + upstreamSignature,
				getLocus().getInstrumentation());
	}
}
