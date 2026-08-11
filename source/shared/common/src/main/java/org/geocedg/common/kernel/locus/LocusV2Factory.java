/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.geocedg.common.kernel.algos.AlgoAnalyticLocusV2;
import org.geocedg.common.kernel.algos.AlgoDynamicBranchLocusV2;
import org.geocedg.common.kernel.algos.AlgoNestedLocusV2;
import org.geocedg.common.kernel.algos.AlgoSegmentPathLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;

/** Typed internal/test creation seam; no command or persistence registration. */
public final class LocusV2Factory {
	private LocusV2Factory() {
		// Static internal factory.
	}

	/**
	 * Creates one stable branch whose valid domain is the full provider domain.
	 *
	 * @return immutable branch descriptor
	 */
	public static LocusBranch2D fullDomainBranch(String branchKey,
			LocusDriverDomainProvider2D provider, String provenance,
			Set<BranchProperty> additionalProperties) {
		EnumSet<BranchProperty> properties = additionalProperties.isEmpty()
				? EnumSet.noneOf(BranchProperty.class)
				: EnumSet.copyOf(additionalProperties);
		if (!properties.contains(BranchProperty.UNBOUNDED)) {
			properties.add(BranchProperty.FINITE);
		}
		if (provider.isPeriodic()) {
			properties.add(BranchProperty.PERIODIC);
		}
		return new LocusBranch2D(branchKey, provider.getDeclaredDomain(),
				Collections.singletonList(provider.getDeclaredDomain()),
				provider.getOrientation(), provenance, LocusLineage2D.unchanged(),
				properties, LocusQuality2D.analyticDoubleSemantic());
	}

	/**
	 * Creates a pointwise V2 through a normal GeoNumeric dependency edge.
	 *
	 * @return experimental internal semantic locus
	 */
	public static GeoLocusV2 createAnalytic(LocusV2Mode mode,
			Construction construction, String locusIdentity, GeoNumeric source,
			LocusDriverDomainProvider2D provider, List<LocusBranch2D> branches,
			LocusPointFunction2D function, String functionSignature) {
		requireV2Mode(mode);
		return new AlgoAnalyticLocusV2(construction, locusIdentity, source,
				provider, branches, function, functionSignature).getLocus();
	}

	/**
	 * Creates one semantic V2-on-V2 normal DAG dependency.
	 *
	 * @return experimental downstream semantic locus
	 */
	public static GeoLocusV2 createNested(LocusV2Mode mode,
			Construction construction, String locusIdentity, GeoLocusV2 upstream,
			String upstreamBranchKey, LocusDriverDomainProvider2D provider,
			List<LocusBranch2D> branches, LocusParameterMap2D parameterMap,
			LocusPointTransform2D pointTransform, String compositionSignature) {
		requireV2Mode(mode);
		return new AlgoNestedLocusV2(construction, locusIdentity, upstream,
				upstreamBranchKey, provider, branches, parameterMap, pointTransform,
				compositionSignature).getLocus();
	}

	/**
	 * Creates dynamic provider-owned branch/domain snapshots.
	 *
	 * @return experimental dynamic semantic locus
	 */
	public static GeoLocusV2 createDynamicAnalytic(LocusV2Mode mode,
			Construction construction, String locusIdentity,
			List<GeoNumeric> sources, LocusDriverDomainProvider2D provider,
			LocusBranchSnapshotFunction2D branchFunction,
			LocusDynamicPointFunction2D pointFunction, String functionSignature) {
		requireV2Mode(mode);
		return new AlgoDynamicBranchLocusV2(construction, locusIdentity, sources,
				provider, branchFunction, pointFunction, functionSignature).getLocus();
	}

	/**
	 * Creates the approved live segment-path pilot without PathParameter.
	 *
	 * @return experimental path-driven semantic locus
	 */
	public static GeoLocusV2 createSegmentPathDriven(LocusV2Mode mode,
			Construction construction, String locusIdentity, GeoSegment segment,
			GeoPoint constrainedDriver, double domainEpsilon,
			LocusPathPointFunction2D pointFunction, String functionSignature) {
		requireV2Mode(mode);
		return new AlgoSegmentPathLocusV2(construction, locusIdentity, segment,
				constrainedDriver, domainEpsilon, pointFunction, functionSignature)
				.getLocus();
	}

	private static void requireV2Mode(LocusV2Mode mode) {
		if (mode == null || mode == LocusV2Mode.LEGACY) {
			throw new IllegalStateException(
					"LEGACY mode never creates the experimental GeoLocusV2");
		}
	}
}
