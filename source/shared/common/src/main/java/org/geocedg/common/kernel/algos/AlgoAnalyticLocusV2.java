/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.List;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusPointFunction2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;

/** Internal pointwise analytic V2 definition driven by one normal GeoNumeric. */
public final class AlgoAnalyticLocusV2 extends AlgoLocusV2 {
	private final String locusIdentity;
	private final GeoNumeric source;
	private final LocusDriverDomainProvider2D provider;
	private final List<LocusBranch2D> branches;
	private final LocusPointFunction2D function;
	private final String functionSignature;

	/** Creates and immediately publishes the first immutable snapshot. */
	public AlgoAnalyticLocusV2(Construction construction, String locusIdentity,
			GeoNumeric source, LocusDriverDomainProvider2D provider,
			List<LocusBranch2D> branches, LocusPointFunction2D function,
			String functionSignature) {
		super(construction, locusIdentity, new GeoElement[] {source});
		this.locusIdentity = locusIdentity;
		this.source = source;
		this.provider = provider;
		this.branches = branches;
		this.function = function;
		this.functionSignature = functionSignature;
		publishInitialSnapshot();
	}

	@Override
	protected LocusDefinition2D createCandidate(long candidateRevision) {
		final boolean sourceDefined = source.isDefined()
				&& Double.isFinite(source.getDouble());
		final double capturedValue = sourceDefined ? source.getDouble() : 0;
		return new LocusDefinition2D(locusIdentity, candidateRevision,
				sourceDefined ? DefinitionStatus.VALID : DefinitionStatus.DRIVER_INVALID,
				provider, branches,
				(definition, branch, parameter, session) -> LocusEvaluation2D.valid(
						function.evaluate(capturedValue, branch, parameter, session),
						Regularity.UNKNOWN, branch.getQuality()),
				Determinism.POINTWISE_DETERMINISTIC,
				functionSignature + "|source=" + Double.toHexString(capturedValue)
						+ "|defined=" + sourceDefined,
				getLocus().getInstrumentation());
	}
}
