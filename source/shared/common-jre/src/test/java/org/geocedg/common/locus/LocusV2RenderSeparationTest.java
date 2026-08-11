/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import org.geocedg.common.euclidian.draw.DrawLocusV2;
import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.euclidian.DrawableND;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

class LocusV2RenderSeparationTest extends BaseUnitTest {
	private static final String BRANCH_KEY = "fixture.sheet.main";

	@Test
	void differentTessellationPoliciesPreserveSameSemanticLocus() {
		GeoLocusV2 locus = parabola();
		long revision = locus.getSemanticRevision();
		LocusEvaluation2D semanticBefore = evaluate(locus, 0.375);
		LocusRenderCache2D cache = new LocusRenderCache2D();
		LocusRenderData2D coarse = cache.getOrBuild(locus,
				new LocusRenderPolicy2D(1, 800, 600, 50, 50, 32));
		LocusRenderData2D fine = cache.getOrBuild(locus,
				new LocusRenderPolicy2D(1, 800, 600, 200, 200, 128));

		assertEquals(33, coarse.getVertices().size());
		assertEquals(129, fine.getVertices().size());
		assertEquals(revision, coarse.getSemanticRevision());
		assertEquals(revision, fine.getSemanticRevision());
		assertEquals(semanticBefore, evaluate(locus, 0.375));
		assertEquals(revision, locus.getSemanticRevision());
		assertEquals(0, locus.getInstrumentation().getDependencySliceBuilds());
		assertEquals(0, locus.getInstrumentation().getWholeLocusRegenerations());
		assertTrue(locus.getInstrumentation().getRenderEvaluations() > 0);
	}

	@Test
	void renderCacheIsBoundedPerDrawablePolicySpace() {
		GeoLocusV2 locus = parabola();
		LocusRenderCache2D cache = new LocusRenderCache2D();
		for (int index = 0; index < 7; index++) {
			cache.getOrBuild(locus, new LocusRenderPolicy2D(1, 800, 600,
					50 + index, 50 + index, 32 + index));
		}
		assertEquals(4, cache.size());
	}

	@Test
	void dedicatedDrawableUsesViewOnlyForPresentation() {
		GeoLocusV2 locus = parabola();
		EuclidianView view = getApp().getEuclidianView1();
		LocusEvaluation2D before = evaluate(locus, -0.25);
		long revision = locus.getSemanticRevision();
		DrawableND drawable = view.newDrawable(locus);
		assertInstanceOf(DrawLocusV2.class, drawable);

		view.setCoordSystem(400, 300, 40, 40);
		drawable.update();
		view.setCoordSystem(400, 300, 180, 180);
		drawable.update();

		assertEquals(before, evaluate(locus, -0.25));
		assertEquals(revision, locus.getSemanticRevision());
		assertEquals(0, locus.getInstrumentation().getDependencyUpdates());
		assertTrue(locus.getInstrumentation().getRenderEvaluations() > 0);
	}

	@Test
	void unboundedImageIsEvaluatedSemanticallyAndClippedOnlyForRender() {
		double endpoint = Math.PI / 2;
		ExplicitNumericDomainProvider2D provider = provider(-endpoint, endpoint,
				false, false);
		LocusBranch2D branch = new LocusBranch2D(BRANCH_KEY,
				provider.getDeclaredDomain(),
				Collections.singletonList(provider.getDeclaredDomain()),
				Orientation.INCREASING, "unbounded-tangent/v1",
				org.geocedg.common.kernel.locus.LocusLineage2D.unchanged(),
				EnumSet.of(BranchProperty.UNBOUNDED),
				org.geocedg.common.kernel.locus.LocusQuality2D
						.analyticDoubleSemantic());
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "unbounded-tangent",
				new GeoNumeric(getConstruction(), 1), provider,
				Collections.singletonList(branch), (source, semanticBranch, parameter,
						session) ->
						new LocusPoint2D(parameter, Math.tan(parameter)),
				"unbounded-tangent/v1");

		assertTrue(Math.abs(evaluate(locus, 1.56).getPoint().getY()) > 50);
		assertEquals(org.geocedg.common.kernel.locus.LocusSemanticMetadata2D
				.EvaluationStatus.OUT_OF_DOMAIN,
				evaluate(locus, endpoint).getStatus());
		LocusRenderData2D data = new LocusRenderCache2D().getOrBuild(locus,
				new LocusRenderPolicy2D(1, 800, 600, 100, 100, 64));
		assertEquals(65, data.getVertices().size());
		for (LocusRenderData2D.Vertex vertex : data.getVertices()) {
			assertTrue(Double.isFinite(vertex.getPoint().getX()));
			assertTrue(Double.isFinite(vertex.getPoint().getY()));
		}
	}

	@Test
	void disconnectedValidComponentsProduceSeparateRenderSubpaths() {
		ExplicitNumericDomainProvider2D provider = provider(-1, 1, true, true);
		LocusBranch2D branch = new LocusBranch2D(BRANCH_KEY,
				provider.getDeclaredDomain(), Arrays.asList(
						new LocusInterval2D(-1, -0.1, true, true),
						new LocusInterval2D(0.1, 1, true, true)),
				Orientation.INCREASING, "discontinuity/v1",
				org.geocedg.common.kernel.locus.LocusLineage2D.unchanged(),
				EnumSet.of(BranchProperty.FINITE),
				org.geocedg.common.kernel.locus.LocusQuality2D
						.analyticDoubleSemantic());
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "discontinuous-reciprocal",
				new GeoNumeric(getConstruction(), 0), provider,
				Collections.singletonList(branch),
				(source, semanticBranch, parameter, session) ->
						new LocusPoint2D(parameter, 1 / parameter),
				"discontinuous-reciprocal/v1");
		assertEquals(org.geocedg.common.kernel.locus.LocusSemanticMetadata2D
				.EvaluationStatus.OUT_OF_DOMAIN, evaluate(locus, 0).getStatus());
		LocusRenderData2D data = new LocusRenderCache2D().getOrBuild(locus,
				new LocusRenderPolicy2D(1, 800, 600, 100, 100, 32));
		assertEquals(2, data.getVertices().stream()
				.filter(LocusRenderData2D.Vertex::startsSubpath).count());
	}

	private GeoLocusV2 parabola() {
		ExplicitNumericDomainProvider2D provider = provider(-1, 1, true, true);
		LocusBranch2D branch = LocusV2Factory.fullDomainBranch(BRANCH_KEY,
				provider, "render-separation-fixture/v1",
				EnumSet.noneOf(BranchProperty.class));
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "render-parabola-" + hashCode(),
				new GeoNumeric(getConstruction(), 1), provider,
				Collections.singletonList(branch), (source, semanticBranch, parameter,
						session) ->
						new LocusPoint2D(parameter, parameter * parameter),
				"render-parabola/v1");
		locus.getInstrumentation().reset();
		return locus;
	}

	private static ExplicitNumericDomainProvider2D provider(double lower,
			double upper, boolean lowerClosed, boolean upperClosed) {
		return new ExplicitNumericDomainProvider2D("render-parameter/v1",
				new LocusInterval2D(lower, upper, lowerClosed, upperClosed),
				Orientation.INCREASING, false, 1E-14);
	}

	private static LocusEvaluation2D evaluate(GeoLocusV2 locus,
			double parameter) {
		return locus.evaluate(BRANCH_KEY, parameter,
				LocusEvaluationSession2D.reference());
	}
}
