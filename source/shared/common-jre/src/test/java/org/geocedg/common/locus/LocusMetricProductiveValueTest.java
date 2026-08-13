/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.metric.AbsentMetricValue2D;
import org.geocedg.common.kernel.locus.metric.EstablishedMetricErrorAmount2D;
import org.geocedg.common.kernel.locus.metric.FiniteMetricValue2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricAggregator2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricContribution2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricCoverage;
import org.geocedg.common.kernel.locus.metric.MetricDiagnostic2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricErrorAmountKind;
import org.geocedg.common.kernel.locus.metric.MetricErrorEvidence2D;
import org.geocedg.common.kernel.locus.metric.MetricEvaluatorMethod2D;
import org.geocedg.common.kernel.locus.metric.MetricMethod2D;
import org.geocedg.common.kernel.locus.metric.MetricProvenance2D;
import org.geocedg.common.kernel.locus.metric.MetricRectifiability;
import org.geocedg.common.kernel.locus.metric.MetricValueKind;
import org.geocedg.common.kernel.locus.metric.NotApplicableMetricErrorAmount2D;
import org.geocedg.common.kernel.locus.metric.NotEstablishedMetricErrorAmount2D;
import org.geocedg.common.kernel.locus.metric.PositiveInfinityMetricValue2D;
import org.geocedg.common.kernel.locus.metric.TotalLocusMetricQuery;
import org.junit.jupiter.api.Test;

/** Productive closed-value, result-axis and aggregation contracts. */
class LocusMetricProductiveValueTest {
	private static final LocusMetricPolicy2D POLICY =
			LocusMetricPolicy2D.initial();

	@Test
	void metricValuesAreClosedSentinelFreeAndNonNegative() {
		assertEquals(MetricValueKind.FINITE,
				new FiniteMetricValue2D(0).getKind());
		assertEquals(0,
				new FiniteMetricValue2D(-0.0).getFiniteValue().orElseThrow());
		assertTrue(new PositiveInfinityMetricValue2D()
				.getFiniteValue().isEmpty());
		assertTrue(new AbsentMetricValue2D().getFiniteValue().isEmpty());
		assertThrows(IllegalArgumentException.class,
				() -> new FiniteMetricValue2D(-1));
		assertThrows(IllegalArgumentException.class,
				() -> new FiniteMetricValue2D(Double.NaN));
		assertThrows(IllegalArgumentException.class,
				() -> new FiniteMetricValue2D(Double.POSITIVE_INFINITY));
	}

	@Test
	void closedErrorAmountsCannotExpressContradictoryAvailability() {
		assertEquals(MetricErrorAmountKind.ESTABLISHED,
				new EstablishedMetricErrorAmount2D(0).getKind());
		assertEquals(MetricErrorAmountKind.NOT_ESTABLISHED,
				new NotEstablishedMetricErrorAmount2D().getKind());
		assertEquals(MetricErrorAmountKind.NOT_APPLICABLE,
				new NotApplicableMetricErrorAmount2D().getKind());
		assertThrows(IllegalArgumentException.class,
				() -> new EstablishedMetricErrorAmount2D(-1));
		assertThrows(IllegalArgumentException.class,
				() -> new EstablishedMetricErrorAmount2D(Double.NaN));
	}

	@Test
	void errorEvidenceReusesNormativeG6Guarantee() {
		MetricErrorEvidence2D exact = MetricErrorEvidence2D.exact(
				"analytic identity");
		MetricErrorEvidence2D estimated = MetricErrorEvidence2D.estimated(
				1E-9, 1E-10, "quadrature", List.of("smooth speed"));
		MetricErrorEvidence2D uncertified =
				MetricErrorEvidence2D.uncertified("point refinement");
		assertEquals(LocusSemanticMetadata2D.NumericGuarantee.class,
				NumericGuarantee.class);
		assertEquals(NumericGuarantee.EXACT_ARITHMETIC,
				exact.getNumericGuarantee().orElseThrow());
		assertEquals(NumericGuarantee.ESTIMATED_ERROR,
				estimated.getNumericGuarantee().orElseThrow());
		assertEquals(NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				uncertified.getNumericGuarantee().orElseThrow());
		assertTrue(MetricErrorEvidence2D.notApplicable("absent")
				.getNumericGuarantee().isEmpty());
		assertThrows(IllegalArgumentException.class,
				() -> MetricErrorEvidence2D.estimated(1, 1, "bad",
						Collections.emptyList()));
	}

	@Test
	void finiteCompleteEstimatedSuccessIsScalarAdmissible() {
		LocusMetricResult2D result = aggregate(contribution("C1",
				new FiniteMetricValue2D(3), MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE,
				MetricErrorEvidence2D.estimated(1E-9, 1E-10, "test",
						List.of("smooth"))));
		assertEquals(MetricCoverage.COMPLETE, result.getCoverage());
		assertTrue(result.isScalarAdmissible());
		assertTrue(result.getTraversalOutcome().isEmpty());
	}

	@Test
	void floatingPointUncertifiedSuccessRemainsRichOnly() {
		LocusMetricResult2D result = aggregate(contribution("C1",
				new FiniteMetricValue2D(3), MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE,
				MetricErrorEvidence2D.uncertified("point refinement")));
		assertTrue(result.getMetricValue().getFiniteValue().isPresent());
		assertFalse(result.isScalarAdmissible());
	}

	@Test
	void infinitePlusUnsupportedKeepsKnownInfinityAndIncompleteCoverage() {
		LocusMetricResult2D result = aggregate(
				contribution("C1", new PositiveInfinityMetricValue2D(),
						MetricComputationStatus.SUCCESS,
						MetricRectifiability.NON_RECTIFIABLE,
						MetricErrorEvidence2D.notApplicable("infinite")),
				contribution("C2", new AbsentMetricValue2D(),
						MetricComputationStatus.UNSUPPORTED,
						MetricRectifiability.UNDETERMINED,
						MetricErrorEvidence2D.notApplicable("unsupported")));
		assertEquals(MetricValueKind.POSITIVE_INFINITY,
				result.getMetricValue().getKind());
		assertEquals(MetricCoverage.INCOMPLETE, result.getCoverage());
		assertEquals(MetricComputationStatus.UNSUPPORTED,
				result.getComputationStatus());
		assertEquals(MetricRectifiability.NON_RECTIFIABLE,
				result.getRectifiability());
		assertFalse(result.isScalarAdmissible());
		assertEquals(2, result.getContributions().size());
	}

	@Test
	void numericalFailureDominatesLimitAndUnsupportedWithoutDivergent() {
		LocusMetricResult2D result = aggregate(
				contribution("C1", new AbsentMetricValue2D(),
						MetricComputationStatus.UNSUPPORTED,
						MetricRectifiability.UNDETERMINED,
						MetricErrorEvidence2D.notApplicable("unsupported")),
				contribution("C2", new AbsentMetricValue2D(),
						MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
						MetricRectifiability.UNDETERMINED,
						MetricErrorEvidence2D.notApplicable("limit")),
				contribution("C3", new AbsentMetricValue2D(),
						MetricComputationStatus.NUMERICAL_FAILURE,
						MetricRectifiability.UNDETERMINED,
						MetricErrorEvidence2D.notApplicable("failure")));
		assertEquals(MetricComputationStatus.NUMERICAL_FAILURE,
				result.getComputationStatus());
	}

	@Test
	void contributionInputListsAreDefensivelyCopied() {
		java.util.ArrayList<LocusMetricContribution2D> mutable =
				new java.util.ArrayList<>();
		mutable.add(contribution("C1", new FiniteMetricValue2D(1),
				MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE,
				MetricErrorEvidence2D.exact("exact")));
		LocusMetricResult2D result =
				new LocusMetricAggregator2D().aggregateTotal(query(), mutable);
		mutable.clear();
		assertEquals(1, result.getContributions().size());
		assertThrows(UnsupportedOperationException.class,
				() -> result.getContributions().clear());
	}

	private static LocusMetricResult2D aggregate(
			LocusMetricContribution2D... contributions) {
		return new LocusMetricAggregator2D().aggregateTotal(query(),
				List.of(contributions));
	}

	private static TotalLocusMetricQuery query() {
		return new TotalLocusMetricQuery("value-fixture", 1, POLICY);
	}

	private static LocusMetricContribution2D contribution(String component,
			org.geocedg.common.kernel.locus.metric.MetricValue2D value,
			MetricComputationStatus status,
			MetricRectifiability rectifiability,
			MetricErrorEvidence2D error) {
		List<MetricDiagnostic2D> diagnostics =
				status == MetricComputationStatus.SUCCESS
						? Collections.emptyList()
						: List.of(new MetricDiagnostic2D(
								status == MetricComputationStatus.UNSUPPORTED
										? MetricDiagnosticCode2D
												.UNSUPPORTED_CAPABILITY
										: MetricDiagnosticCode2D.NUMERICAL_FAILURE,
								status.toString()));
		return new LocusMetricContribution2D("branch", component, value,
				status, rectifiability,
				ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
				MetricEvaluatorMethod2D.ANALYTIC,
				MetricMethod2D.CLOSED_FORM, error,
				new MetricProvenance2D("value-fixture", 1, "cap/v1",
						POLICY.getMetricAlgorithmVersion(),
						POLICY.getMetricPolicyVersion()),
				diagnostics);
	}
}
