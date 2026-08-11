/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusBranchSnapshot2D;
import org.geocedg.common.kernel.locus.LocusBranchSnapshotFunction2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusDynamicPointFunction2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusSourceSnapshot2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;

/** Internal V2 algorithm whose provider publishes dynamic branch topology. */
public final class AlgoDynamicBranchLocusV2 extends AlgoLocusV2 {
	private final String locusIdentity;
	private final List<GeoNumeric> sources;
	private final LocusDriverDomainProvider2D provider;
	private final LocusBranchSnapshotFunction2D branchFunction;
	private final LocusDynamicPointFunction2D pointFunction;
	private final String functionSignature;

	/** Creates a dynamic semantic definition over normal GeoNumeric DAG inputs. */
	public AlgoDynamicBranchLocusV2(Construction construction,
			String locusIdentity, List<GeoNumeric> sources,
			LocusDriverDomainProvider2D provider,
			LocusBranchSnapshotFunction2D branchFunction,
			LocusDynamicPointFunction2D pointFunction, String functionSignature) {
		super(construction, locusIdentity, toInputs(sources));
		this.locusIdentity = locusIdentity;
		this.sources = Collections.unmodifiableList(new ArrayList<>(sources));
		this.provider = provider;
		this.branchFunction = branchFunction;
		this.pointFunction = pointFunction;
		this.functionSignature = functionSignature;
		publishInitialSnapshot();
	}

	@Override
	protected LocusDefinition2D createCandidate(long candidateRevision) {
		double[] values = new double[sources.size()];
		for (int index = 0; index < values.length; index++) {
			GeoNumeric source = sources.get(index);
			if (!source.isDefined() || !Double.isFinite(source.getDouble())) {
				return invalidCandidate(candidateRevision);
			}
			values[index] = source.getDouble();
		}
		LocusSourceSnapshot2D sourceSnapshot = new LocusSourceSnapshot2D(values);
		LocusBranchSnapshot2D branchSnapshot = branchFunction.create(sourceSnapshot,
				getLocus().getSemanticDefinition());
		return new LocusDefinition2D(locusIdentity, candidateRevision,
				branchSnapshot.getDefinitionStatus(), provider,
				branchSnapshot.getBranches(),
				(definition, branch, parameter, session) -> LocusEvaluation2D.valid(
						pointFunction.evaluate(sourceSnapshot, branch, parameter, session),
						Regularity.UNKNOWN, branch.getQuality()),
				Determinism.POINTWISE_DETERMINISTIC,
				functionSignature + sourceSnapshot.getSemanticSignature(),
				getLocus().getInstrumentation());
	}

	private LocusDefinition2D invalidCandidate(long candidateRevision) {
		return new LocusDefinition2D(locusIdentity, candidateRevision,
				DefinitionStatus.DRIVER_INVALID, provider,
				Collections.emptyList(),
				(definition, branch, parameter, session) -> LocusEvaluation2D.invalid(
						org.geocedg.common.kernel.locus.LocusSemanticMetadata2D
								.EvaluationStatus.DEPENDENCY_UNDEFINED,
						branch.getQuality(), "Invalid dynamic source"),
				Determinism.POINTWISE_DETERMINISTIC,
				functionSignature + "|driver-invalid",
				getLocus().getInstrumentation());
	}

	private static GeoElement[] toInputs(List<GeoNumeric> sources) {
		return sources.toArray(new GeoElement[0]);
	}
}
