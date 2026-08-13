/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.metric.AnalyticLocusMetricCapability2D;
import org.geocedg.common.kernel.locus.metric.BetweenPositionsMetricQuery;
import org.geocedg.common.kernel.locus.metric.DifferentialLocusMetricCapability2D;
import org.geocedg.common.kernel.locus.metric.LocusAnalyticMetricEvaluation2D;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricCapabilityHierarchy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricEngine2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexMode;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricOwnerLease2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricCoverage;
import org.geocedg.common.kernel.locus.metric.MetricRectifiability;
import org.geocedg.common.kernel.locus.metric.MetricValueKind;
import org.geocedg.common.kernel.locus.metric.OpenBoundaryPolicy;
import org.geocedg.common.kernel.locus.metric.SamePositionPolicy;
import org.geocedg.common.kernel.locus.metric.TotalLocusMetricQuery;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;
import org.junit.jupiter.api.Test;

/** Productive unbounded/improper outcome taxonomy without viewport cutoff. */
class LocusMetricProductiveImproperTest {
	private final LocusMetricEngine2D engine = new LocusMetricEngine2D();

	@Test
	void finiteBetweenPositionsOnUnboundedBranchIsSupportedAnalytically() {
		LocusDefinition2D definition = unboundedDefinition(true, true);
		BetweenPositionsMetricQuery query = G7BMetricFixtures.between(
				definition, -1, 2, TraversalDirection.FORWARD,
				OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH);
		LocusMetricResult2D result = compute(query, definition,
				analyticImproper(true));
		assertEquals(3, result.getMetricValue().getFiniteValue()
				.orElseThrow(), 0);
		assertEquals(MetricCoverage.COMPLETE, result.getCoverage());
	}

	@Test
	void wholeUnboundedLineCanBeEstablishedPositiveInfinity() {
		LocusDefinition2D definition = unboundedDefinition(false, false);
		LocusMetricResult2D result = compute(total(definition), definition,
				analyticImproper(false));
		assertEquals(MetricValueKind.POSITIVE_INFINITY,
				result.getMetricValue().getKind());
		assertEquals(MetricCoverage.COMPLETE, result.getCoverage());
		assertFalse(result.isScalarAdmissible());
	}

	@Test
	void convergentImproperTotalCanBeFiniteWithAnalyticEvidence() {
		LocusDefinition2D definition = unboundedDefinition(false, true);
		LocusMetricResult2D result = compute(total(definition), definition,
				analyticImproper(true));
		assertEquals(2, result.getMetricValue().getFiniteValue()
				.orElseThrow(), 0);
		assertEquals(MetricComputationStatus.SUCCESS,
				result.getComputationStatus());
		assertTrue(result.isScalarAdmissible());
	}

	@Test
	void nonRectifiableAndPositiveInfiniteRemainDistinctAxes() {
		LocusDefinition2D definition = unboundedDefinition(false, false);
		LocusMetricCapabilityHierarchy2D capability =
				new LocusMetricCapabilityHierarchy2D(List.of(
						new AnalyticLocusMetricCapability2D(
								"nonrectifiable/v1",
								(branch, start, end) ->
										LocusAnalyticMetricEvaluation2D
												.positiveInfinity(true,
														"established infinite "
																+ "variation"))));
		LocusMetricResult2D result = compute(total(definition), definition,
				capability);
		assertEquals(MetricValueKind.POSITIVE_INFINITY,
				result.getMetricValue().getKind());
		assertEquals(MetricRectifiability.NON_RECTIFIABLE,
				result.getRectifiability());
		assertEquals(MetricComputationStatus.SUCCESS,
				result.getComputationStatus());
	}

	@Test
	void differentialOpenEndpointReturnsLimitNotEstablishedNotFailure() {
		LocusDefinition2D definition = unboundedDefinition(false, false);
		LocusMetricCapabilityHierarchy2D capability =
				new LocusMetricCapabilityHierarchy2D(List.of(
						new DifferentialLocusMetricCapability2D(
								"open-differential/v1",
								(source, branch, parameter, session) ->
										LocusDifferentialEvaluation2D.valid(1,
												0))));
		LocusMetricResult2D result = compute(total(definition), definition,
				capability);
		assertEquals(MetricValueKind.ABSENT,
				result.getMetricValue().getKind());
		assertEquals(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				result.getComputationStatus());
		assertEquals(MetricCoverage.INCOMPLETE, result.getCoverage());
	}

	@Test
	void missingCapabilitiesProduceLegitimateUnsupportedRichResult() {
		LocusDefinition2D definition = unboundedDefinition(true, true);
		LocusMetricResult2D result = compute(total(definition), definition,
				new LocusMetricCapabilityHierarchy2D(Collections.emptyList()));
		assertEquals(MetricValueKind.ABSENT,
				result.getMetricValue().getKind());
		assertEquals(MetricComputationStatus.UNSUPPORTED,
				result.getComputationStatus());
		assertEquals(MetricCoverage.INCOMPLETE, result.getCoverage());
	}

	private LocusMetricCapabilityHierarchy2D analyticImproper(
			boolean convergent) {
		return new LocusMetricCapabilityHierarchy2D(List.of(
				new AnalyticLocusMetricCapability2D("improper/v1",
						(branch, start, end) -> {
							if (start == -4 && end == 4) {
								return convergent
										? LocusAnalyticMetricEvaluation2D
												.exactFinite(2)
										: LocusAnalyticMetricEvaluation2D
												.positiveInfinity(false,
														"whole unbounded line");
							}
							return LocusAnalyticMetricEvaluation2D
									.exactFinite(Math.abs(end - start));
						})));
	}

	private LocusMetricResult2D compute(
			org.geocedg.common.kernel.locus.metric.LocusMetricQuery2D query,
			LocusDefinition2D definition,
			LocusMetricCapabilityHierarchy2D capability) {
		LocusMetricInstrumentation2D instrumentation =
				new LocusMetricInstrumentation2D();
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D(
				definition.getLocusIdentity(), instrumentation);
		try (LocusMetricOwnerLease2D lease = owner.acquireLease()) {
			return engine.compute(query, definition, capability, owner,
					LocusMetricIndexMode.LAZY_COMPONENT_REVISION,
					instrumentation, "improper");
		}
	}

	private static TotalLocusMetricQuery total(
			LocusDefinition2D definition) {
		return new TotalLocusMetricQuery(definition.getLocusIdentity(),
				definition.getSemanticRevision(),
				LocusMetricPolicy2D.initial());
	}

	private static LocusDefinition2D unboundedDefinition(
			boolean closedEndpoints, boolean convergent) {
		String identity = convergent ? "improper-convergent"
				: "improper-infinite";
		ExplicitNumericDomainProvider2D provider =
				new ExplicitNumericDomainProvider2D(
						"unbounded-transform/v1",
						new LocusInterval2D(-4, 4, true, true),
						Orientation.INCREASING, false,
						G7BMetricFixtures.EPS_DOMAIN);
		LocusInterval2D valid = new LocusInterval2D(-4, 4,
				closedEndpoints, closedEndpoints);
		LocusBranch2D branch = new LocusBranch2D(G7BMetricFixtures.BRANCH,
				provider.getDeclaredDomain(), List.of(valid),
				Orientation.INCREASING, "unbounded-transform/v1",
				LocusLineage2D.unchanged(),
				EnumSet.of(BranchProperty.UNBOUNDED),
				LocusQuality2D.analyticDoubleSemantic());
		return new LocusDefinition2D(identity, 1, DefinitionStatus.VALID,
				provider, List.of(branch),
				(source, semanticBranch, parameter, session) ->
						LocusEvaluation2D.valid(
								new LocusPoint2D(parameter, 0),
								Regularity.UNKNOWN,
								semanticBranch.getQuality()),
				Determinism.POINTWISE_DETERMINISTIC,
				"unbounded-transform/v1", new LocusInstrumentation2D());
	}
}
