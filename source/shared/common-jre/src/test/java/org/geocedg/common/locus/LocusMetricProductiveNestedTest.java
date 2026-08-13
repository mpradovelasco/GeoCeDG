/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.EnumSet;

import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.algos.AlgoLocusMetricV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexMode;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentationSnapshot2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.MetricValueKind;
import org.geocedg.common.kernel.locus.metric.TotalLocusMetricQuery;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/** Productive three-level metric/locus composition and forbidden-work gates. */
class LocusMetricProductiveNestedTest extends BaseUnitTest {
	private static final String BRANCH = "g7b.nested.main";

	@Test
	void threeLevelCompositionUsesNormalDagAndNoRenderOrLegacyAuthority() {
		GeoNumeric source = new GeoNumeric(getConstruction(), 1);
		source.setLabel("g7bNestedSource");
		GeoLocusV2 locus1 = locus("g7b-L1", source, 0);
		AlgoLocusMetricV2 metric1a = metric(locus1, "M1a");
		AlgoLocusMetricV2 metric1b = metric(locus1, "M1b");
		AlgoLocusMetricV2 metric1total = metric(locus1, "M1total");
		AlgoLocusMetricScalarAdapter scalar1 =
				new AlgoLocusMetricScalarAdapter(getConstruction(),
						metric1total.getResult());

		GeoLocusV2 locus2 = locus("g7b-L2",
				scalar1.getScalarOutput(), 2);
		AlgoLocusMetricV2 metric2a = metric(locus2, "M2a");
		AlgoLocusMetricV2 metric2total = metric(locus2, "M2total");
		AlgoLocusMetricScalarAdapter scalar2 =
				new AlgoLocusMetricScalarAdapter(getConstruction(),
						metric2total.getResult());

		GeoLocusV2 locus3 = locus("g7b-L3",
				scalar2.getScalarOutput(), 4);
		for (int point = 0; point < 100; point++) {
			double parameter = -1 + 2.0 * point / 99;
			assertTrue(locus3.evaluate(BRANCH, parameter,
					LocusEvaluationSession2D.reference()).isValid());
		}

		assertEquals(2, metric1a.getResult().getMetricResult()
				.getMetricValue().getFiniteValue().orElseThrow(), 0);
		assertEquals(2, metric1b.getResult().getMetricResult()
				.getMetricValue().getFiniteValue().orElseThrow(), 0);
		assertEquals(2, metric2a.getResult().getMetricResult()
				.getMetricValue().getFiniteValue().orElseThrow(), 0);
		assertEquals(1, locus1.getMetricSharedOwnerForDiagnostics()
				.statistics().getBuilds());
		assertEquals(2, locus1.getMetricInstrumentation().snapshot()
				.getCrossResultHits());
		assertEquals(1, locus2.getMetricSharedOwnerForDiagnostics()
				.statistics().getBuilds());
		assertEquals(1, locus2.getMetricInstrumentation().snapshot()
				.getCrossResultHits());
		assertForbiddenCounters(locus1);
		assertForbiddenCounters(locus2);
		assertForbiddenCounters(locus3);
		assertEquals(1, locus1.getMetricInstrumentation().snapshot()
				.getMaximumActiveBuilds());
		assertEquals(1, locus2.getMetricInstrumentation().snapshot()
				.getMaximumActiveBuilds());
	}

	@Test
	void upstreamChangeInvalidatesEveryDownstreamLevelWithoutStaleMetric() {
		GeoNumeric source = new GeoNumeric(getConstruction(), 1);
		GeoLocusV2 locus1 = locus("g7b-invalid-L1", source, 0);
		AlgoLocusMetricV2 metric1 = metric(locus1, "M1");
		AlgoLocusMetricScalarAdapter scalar1 =
				new AlgoLocusMetricScalarAdapter(getConstruction(),
						metric1.getResult());
		GeoLocusV2 locus2 = locus("g7b-invalid-L2",
				scalar1.getScalarOutput(), 2);
		AlgoLocusMetricV2 metric2 = metric(locus2, "M2");
		AlgoLocusMetricScalarAdapter scalar2 =
				new AlgoLocusMetricScalarAdapter(getConstruction(),
						metric2.getResult());
		final GeoLocusV2 locus3 = locus("g7b-invalid-L3",
				scalar2.getScalarOutput(), 4);

		source.setValue(3);
		source.updateCascade();
		assertEquals(MetricValueKind.FINITE,
				metric1.getResult().getMetricResult().getMetricValue().getKind());
		assertTrue(scalar1.getScalarOutput().isDefined());
		assertTrue(locus2.isDefined());
		assertEquals(MetricValueKind.FINITE,
				metric2.getResult().getMetricResult().getMetricValue().getKind());
		assertTrue(scalar2.getScalarOutput().isDefined());
		assertTrue(locus3.isDefined());
		assertEquals(locus1.getSemanticRevision(),
				metric1.getResult().getSourceSemanticRevision());
		assertEquals(locus2.getSemanticRevision(),
				metric2.getResult().getSourceSemanticRevision());
		assertForbiddenCounters(locus1);
		assertForbiddenCounters(locus2);
		assertForbiddenCounters(locus3);
	}

	private AlgoLocusMetricV2 metric(GeoLocusV2 locus, String consumer) {
		return new AlgoLocusMetricV2(getConstruction(), locus,
				new TotalLocusMetricQuery(locus.getLocusIdentity(),
						locus.getSemanticRevision(),
						LocusMetricPolicy2D.initial()),
				G7BMetricFixtures.analytic(1, "nested-line/v1"),
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION, consumer,
				new GeoElement[0]);
	}

	private GeoLocusV2 locus(String identity, GeoNumeric source,
			double verticalOffset) {
		ExplicitNumericDomainProvider2D provider =
				new ExplicitNumericDomainProvider2D("nested-param/v1",
						new LocusInterval2D(-1, 1, true, true),
						Orientation.INCREASING, false,
						G7BMetricFixtures.EPS_DOMAIN);
		LocusBranch2D branch = LocusV2Factory.fullDomainBranch(BRANCH, provider,
				"g7b-nested/v1", EnumSet.of(BranchProperty.FINITE));
		return LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), identity, source, provider,
				Collections.singletonList(branch),
				(value, semanticBranch, parameter, session) ->
						new LocusPoint2D(parameter + value,
								verticalOffset),
				"g7b-nested-line/v1");
	}

	private static void assertForbiddenCounters(GeoLocusV2 locus) {
		LocusMetricInstrumentationSnapshot2D counters =
				locus.getMetricInstrumentation().snapshot();
		assertEquals(0, counters.getRenderReads());
		assertEquals(0, counters.getLegacySampleReads());
		assertEquals(0, counters.getWholeLocusRegenerations());
		assertEquals(0, counters.getIndexBuildsInsideDownstreamPoint());
		assertEquals(0, counters.getDuplicateCompatibleBuilds());
	}
}
