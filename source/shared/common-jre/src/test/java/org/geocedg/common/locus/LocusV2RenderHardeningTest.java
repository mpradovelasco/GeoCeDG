/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

class LocusV2RenderHardeningTest extends BaseUnitTest {
	private static final String BRANCH_KEY = "render-hardening.sheet.main";
	private static final double VISUAL_TOLERANCE = 0.75;

	@Test
	void adaptiveVisualSamplingReducesLineVerticesWithoutSemanticChange() {
		GeoLocusV2 line = locus("adaptive-line", (source, parameter) ->
				new LocusPoint2D(parameter, 2 * parameter + source));
		long revision = line.getSemanticRevision();
		LocusEvaluation2D before = evaluate(line, 0.375);
		LocusRenderData2D uniform = new LocusRenderCache2D().getOrBuild(line,
				uniform(128));
		LocusRenderData2D adaptive = new LocusRenderCache2D().getOrBuild(line,
				adaptive(128, 200));

		assertEquals(129, uniform.getVertices().size());
		assertEquals(5, adaptive.getVertices().size());
		assertEquals(revision, line.getSemanticRevision());
		assertEquals(before, evaluate(line, 0.375));
		assertEquals(revision, adaptive.getSemanticRevision());
	}

	@Test
	void adaptiveParabolaRespectsItsVisualChordEnvelope() {
		GeoLocusV2 parabola = locus("adaptive-parabola", (source, parameter) ->
				new LocusPoint2D(parameter, parameter * parameter + source));
		LocusRenderPolicy2D policy = adaptive(256, 180);
		LocusRenderData2D data = new LocusRenderCache2D().getOrBuild(parabola,
				policy);

		assertTrue(data.getVertices().size() < 257);
		for (int index = 1; index < data.getVertices().size(); index++) {
			LocusRenderData2D.Vertex lower = data.getVertices().get(index - 1);
			LocusRenderData2D.Vertex upper = data.getVertices().get(index);
			if (upper.startsSubpath()) {
				continue;
			}
			double parameter = lower.getSemanticParameter()
					+ (upper.getSemanticParameter()
							- lower.getSemanticParameter()) / 2;
			LocusPoint2D midpoint = evaluate(parabola, parameter).getPoint();
			assertTrue(chordError(lower.getPoint(), midpoint, upper.getPoint(),
					policy) <= VISUAL_TOLERANCE + 1E-12);
		}
	}

	@Test
	void adaptiveSamplingPreservesComponentsPeriodicSeamAndUnboundedClipping() {
		ExplicitNumericDomainProvider2D discontinuousProvider = provider(-1, 1,
				false);
		LocusBranch2D discontinuousBranch = new LocusBranch2D(BRANCH_KEY,
				discontinuousProvider.getDeclaredDomain(), Arrays.asList(
						new LocusInterval2D(-1, -0.1, true, true),
						new LocusInterval2D(0.1, 1, true, true)),
				Orientation.INCREASING, "g6r-discontinuity/v1",
				LocusLineage2D.unchanged(), EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());
		GeoLocusV2 discontinuous = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "adaptive-discontinuous",
				new GeoNumeric(getConstruction(), 0), discontinuousProvider,
				Collections.singletonList(discontinuousBranch),
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, 1 / parameter),
				"g6r-discontinuity/v1");
		LocusRenderData2D disconnected = new LocusRenderCache2D().getOrBuild(
				discontinuous, adaptive(128, 100));
		assertEquals(2, disconnected.getVertices().stream()
				.filter(LocusRenderData2D.Vertex::startsSubpath).count());

		ExplicitNumericDomainProvider2D periodicProvider = provider(-Math.PI,
				Math.PI, true);
		LocusBranch2D periodicBranch = LocusV2Factory.fullDomainBranch(BRANCH_KEY,
				periodicProvider, "g6r-periodic/v1",
				EnumSet.noneOf(BranchProperty.class));
		GeoLocusV2 circle = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "adaptive-circle",
				new GeoNumeric(getConstruction(), 0), periodicProvider,
				Collections.singletonList(periodicBranch),
				(source, branch, parameter, session) -> new LocusPoint2D(
						Math.cos(parameter), Math.sin(parameter)),
				"g6r-periodic/v1");
		assertEquals(evaluate(circle, -Math.PI), evaluate(circle, Math.PI));
		LocusRenderData2D circleData = new LocusRenderCache2D().getOrBuild(circle,
				adaptive(256, 150));
		assertEquals(1, circleData.getVertices().stream()
				.filter(LocusRenderData2D.Vertex::startsSubpath).count());

		double endpoint = Math.PI / 2;
		ExplicitNumericDomainProvider2D unboundedProvider =
				new ExplicitNumericDomainProvider2D("unbounded-parameter/v1",
						new LocusInterval2D(-endpoint, endpoint, false, false),
						Orientation.INCREASING, false, 1E-14);
		LocusBranch2D unboundedBranch = new LocusBranch2D(BRANCH_KEY,
				unboundedProvider.getDeclaredDomain(),
				Collections.singletonList(unboundedProvider.getDeclaredDomain()),
				Orientation.INCREASING, "g6r-unbounded/v1",
				LocusLineage2D.unchanged(), EnumSet.of(BranchProperty.UNBOUNDED),
				LocusQuality2D.analyticDoubleSemantic());
		GeoLocusV2 unbounded = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "adaptive-unbounded",
				new GeoNumeric(getConstruction(), 0), unboundedProvider,
				Collections.singletonList(unboundedBranch),
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, Math.tan(parameter)),
				"g6r-unbounded/v1");
		LocusRenderData2D unboundedData = new LocusRenderCache2D().getOrBuild(
				unbounded, adaptive(256, 100));
		assertTrue(unboundedData.getVertices().stream().allMatch(vertex ->
				Double.isFinite(vertex.getPoint().getX())
						&& Double.isFinite(vertex.getPoint().getY())));
	}

	@Test
	void renderCacheReportsColdWarmEvictionAndRevisionMisses() {
		GeoNumeric source = new GeoNumeric(getConstruction(), 0);
		GeoLocusV2 locus = locus("cache-observation", source,
				(value, parameter) -> new LocusPoint2D(parameter, value + parameter));
		LocusRenderCache2D cache = new LocusRenderCache2D();
		LocusRenderPolicy2D policy = uniform(32);
		LocusRenderData2D first = cache.getOrBuild(locus, policy);
		assertEquals(first, cache.getOrBuild(locus, policy));
		source.setValue(1);
		source.updateCascade();
		LocusRenderData2D changed = cache.getOrBuild(locus, policy);

		assertNotSame(first, changed);
		assertEquals(1, cache.getHits());
		assertEquals(2, cache.getMisses());
		assertEquals(2, cache.getBuilds());
		assertEquals(locus.getSemanticRevision(), changed.getSemanticRevision());
		for (int index = 0; index < 5; index++) {
			cache.getOrBuild(locus, new LocusRenderPolicy2D(index + 2, 800, 600,
					50 + index, 50 + index, 32));
		}
		assertTrue(cache.getEvictions() > 0);
		cache.clear();
		assertEquals(0, cache.size());
	}

	private GeoLocusV2 locus(String identity, TestPointFunction function) {
		return locus(identity, new GeoNumeric(getConstruction(), 0), function);
	}

	private GeoLocusV2 locus(String identity, GeoNumeric source,
			TestPointFunction function) {
		ExplicitNumericDomainProvider2D provider = provider(-1, 1, false);
		LocusBranch2D branch = LocusV2Factory.fullDomainBranch(BRANCH_KEY,
				provider, "g6r-render-hardening/v1",
				EnumSet.noneOf(BranchProperty.class));
		return LocusV2Factory.createAnalytic(LocusV2Mode.V2, getConstruction(),
				identity, source, provider, Collections.singletonList(branch),
				(value, semanticBranch, parameter, session) ->
						function.evaluate(value, parameter),
				"g6r-render-hardening/v1");
	}

	private static ExplicitNumericDomainProvider2D provider(double lower,
			double upper, boolean periodic) {
		return new ExplicitNumericDomainProvider2D("render-hardening-parameter/v1",
				new LocusInterval2D(lower, upper, true, !periodic),
				Orientation.INCREASING, periodic, 1E-14);
	}

	private static LocusRenderPolicy2D uniform(int samples) {
		return new LocusRenderPolicy2D(1, 800, 600, 100, 100, samples);
	}

	private static LocusRenderPolicy2D adaptive(int fallbackSamples,
			double scale) {
		return LocusRenderPolicy2D.adaptive(1, 800, 600, scale, scale,
				fallbackSamples, VISUAL_TOLERANCE, 12);
	}

	private static LocusEvaluation2D evaluate(GeoLocusV2 locus,
			double parameter) {
		return locus.evaluate(BRANCH_KEY, parameter,
				LocusEvaluationSession2D.reference());
	}

	private static double chordError(LocusPoint2D lower, LocusPoint2D midpoint,
			LocusPoint2D upper, LocusRenderPolicy2D policy) {
		double ax = lower.getX() * policy.getXScale();
		double ay = lower.getY() * policy.getYScale();
		double bx = upper.getX() * policy.getXScale();
		double by = upper.getY() * policy.getYScale();
		double px = midpoint.getX() * policy.getXScale();
		double py = midpoint.getY() * policy.getYScale();
		double dx = bx - ax;
		double dy = by - ay;
		double lengthSquared = dx * dx + dy * dy;
		if (lengthSquared == 0) {
			return Math.hypot(px - ax, py - ay);
		}
		double ratio = ((px - ax) * dx + (py - ay) * dy) / lengthSquared;
		ratio = Math.max(0, Math.min(1, ratio));
		return Math.hypot(px - (ax + ratio * dx), py - (ay + ratio * dy));
	}

	private interface TestPointFunction {
		LocusPoint2D evaluate(double source, double parameter);
	}
}
