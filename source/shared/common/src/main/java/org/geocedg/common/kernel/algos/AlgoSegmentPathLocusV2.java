/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.Collections;
import java.util.EnumSet;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusPathPointFunction2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.StablePathDomainProvider2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;

/**
 * Internal segment-path pilot. It captures the approved semantic mapping on
 * recompute and never uses public PathParameter as semantic identity.
 */
public final class AlgoSegmentPathLocusV2 extends AlgoLocusV2 {
	private final String locusIdentity;
	private final GeoSegment segment;
	private final GeoPoint constrainedDriver;
	private final double domainEpsilon;
	private final LocusPathPointFunction2D pointFunction;
	private final String functionSignature;

	/** Creates one live segment driver through normal kernel DAG inputs. */
	public AlgoSegmentPathLocusV2(Construction construction, String locusIdentity,
			GeoSegment segment, GeoPoint constrainedDriver, double domainEpsilon,
			LocusPathPointFunction2D pointFunction, String functionSignature) {
		super(construction, locusIdentity,
				new GeoElement[] {segment, constrainedDriver});
		this.locusIdentity = locusIdentity;
		this.segment = segment;
		this.constrainedDriver = constrainedDriver;
		this.domainEpsilon = domainEpsilon;
		this.pointFunction = pointFunction;
		this.functionSignature = functionSignature;
		publishInitialSnapshot();
	}

	@Override
	protected LocusDefinition2D createCandidate(long candidateRevision) {
		boolean valid = segment.isDefined() && constrainedDriver.isDefined()
				&& constrainedDriver.isPointOnPath()
				&& constrainedDriver.getPath() == segment
				&& segment.getStartPoint() != null && segment.getEndPoint() != null
				&& segment.getStartPoint().isDefined()
				&& segment.getEndPoint().isDefined();
		LocusPoint2D start = valid
				? new LocusPoint2D(segment.getStartPoint().getInhomX(),
						segment.getStartPoint().getInhomY())
				: new LocusPoint2D(0, 0);
		LocusPoint2D end = valid
				? new LocusPoint2D(segment.getEndPoint().getInhomX(),
						segment.getEndPoint().getInhomY())
				: new LocusPoint2D(0, 0);
		StablePathDomainProvider2D provider = StablePathDomainProvider2D.segment(
				"segment-native-t/v1", start, end, domainEpsilon);
		LocusBranch2D branch = LocusV2Factory.fullDomainBranch(
				"segment.sheet.main", provider, "stable-segment-path-provider/v1",
				EnumSet.noneOf(BranchProperty.class));
		return new LocusDefinition2D(locusIdentity, candidateRevision,
				valid ? DefinitionStatus.VALID : DefinitionStatus.DRIVER_INVALID,
				provider, valid ? Collections.singletonList(branch)
						: Collections.emptyList(),
				(definition, semanticBranch, parameter, session) -> {
					LocusPoint2D driverPoint = provider.evaluateDriverPoint(parameter);
					return LocusEvaluation2D.valid(pointFunction.evaluate(driverPoint,
							semanticBranch, parameter, session), Regularity.UNKNOWN,
							semanticBranch.getQuality());
				}, Determinism.POINTWISE_DETERMINISTIC,
				functionSignature + "|valid=" + valid + "|"
						+ provider.getSemanticSignature(),
				getLocus().getInstrumentation());
	}
}
