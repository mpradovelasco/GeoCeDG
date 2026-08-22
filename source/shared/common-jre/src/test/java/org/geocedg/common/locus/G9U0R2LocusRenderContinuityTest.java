/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.geocedg.common.euclidian.draw.DrawLocusV2;
import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.awt.GBasicStroke;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GGraphicsCommon;
import org.geogebra.common.awt.GPathIterator;
import org.geogebra.common.awt.GShape;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.junit.jupiter.api.Test;

class G9U0R2LocusRenderContinuityTest extends G9U0PublicSurfaceTestBase {
	private static final LocusRenderPolicy2D FIXED_POLICY =
			new LocusRenderPolicy2D(1, 800, 600, 100, 100, 96);

	// R2-L09
	@Test
	void addingMovingStylingAndDeletingCrossingLineCannotSplitTheLocus() {
		GeoLocusV2 locus = createParabola();
		GeoLocusMetricResult metric = totalMetric(locus);
		GeoLocusIntersectionResult intersection = intersect(locus, "y=1");
		CrossingWitness witness = CrossingWitness.capture(locus, metric,
				intersection);
		final GeoNumeric shift = add("lineShift=0");

		GeoElement line = add("crossLine:x=lineShift");
		witness.assertUnchanged(locus, metric, intersection);
		line.setObjColor(GColor.RED);
		line.setLineThickness(9);
		line.setLineType(EuclidianStyleConstants.LINE_TYPE_DASHED_DOTTED);
		line.setSelected(true);
		line.setHighlighted(true);
		witness.assertUnchanged(locus, metric, intersection);
		shift.setValue(0.5);
		shift.updateCascade();
		witness.assertUnchanged(locus, metric, intersection);
		line.remove();
		witness.assertUnchanged(locus, metric, intersection);
	}

	// R2-L10
	@Test
	void circleAndEverySupportedConicPreserveOneAndMultipleCrossings() {
		GeoLocusV2 locus = createLine();
		GeoLocusMetricResult metric = totalMetric(locus);
		GeoLocusIntersectionResult baseline = intersect(locus, "x=0");
		CrossingWitness witness = CrossingWitness.capture(locus, metric, baseline);
		final GeoNumeric shift = add("conicShift=0");
		GeoElement circle = add(
				"crossCircle=Circle((conicShift,0),1)");
		GeoElement ellipse = add("crossEllipse=Ellipse((-1+conicShift,0),"
				+ "(1+conicShift,0),1.5)");
		GeoElement parabola = add(
				"crossParabola=Parabola((conicShift,1),y=-1)");
		GeoElement hyperbola = add("crossHyperbola=Hyperbola("
				+ "(-1+conicShift,0),(1+conicShift,0),0.5)");
		GeoLocusIntersectionResult circleCrossings = add(
				"circleCrossings=Intersect(L,crossCircle)");
		GeoLocusIntersectionResult ellipseCrossings = add(
				"ellipseCrossings=Intersect(L,crossEllipse)");
		GeoLocusIntersectionResult parabolaCrossings = add(
				"parabolaCrossings=Intersect(L,crossParabola)");
		GeoLocusIntersectionResult hyperbolaCrossings = add(
				"hyperbolaCrossings=Intersect(L,crossHyperbola)");

		assertEquals(2, solutionCount(circleCrossings));
		assertEquals(2, solutionCount(ellipseCrossings));
		assertEquals(1, solutionCount(parabolaCrossings));
		assertEquals(2, solutionCount(hyperbolaCrossings));
		witness.assertUnchanged(locus, metric, baseline);
		List<GeoElement> conics = List.of(circle, ellipse, parabola, hyperbola);
		for (int index = 0; index < conics.size(); index++) {
			GeoElement conic = conics.get(index);
			conic.setObjColor(index % 2 == 0 ? GColor.BLUE : GColor.RED);
			conic.setLineThickness(6 + index);
			conic.setSelected(true);
		}
		witness.assertUnchanged(locus, metric, baseline);
		shift.setValue(0.25);
		shift.updateCascade();
		assertEquals(2, solutionCount(circleCrossings));
		assertEquals(2, solutionCount(ellipseCrossings));
		assertEquals(1, solutionCount(parabolaCrossings));
		assertEquals(2, solutionCount(hyperbolaCrossings));
		witness.assertUnchanged(locus, metric, baseline);
		for (GeoElement conic : conics) {
			conic.remove();
		}
		witness.assertUnchanged(locus, metric, baseline);
	}

	// R2-L11
	@Test
	void exactCanonicalRenderTopologySurvivesAllCrossingFixtures() {
		GeoLocusV2 locus = createParabola();
		String expected = fingerprint(locus);
		final String expectedSubpaths = subpathFingerprint(locus);
		assertEquals(1, subpathCount(locus));

		add("topologyLine:x=0");
		add("topologyCircle=Circle((0,0),1)");
		add("topologyEllipse=Ellipse((-1,0),(1,0),3)");
		add("topologyParabola=Parabola((0,1),y=-1)");
		add("topologyHyperbola=Hyperbola((-1,0),(1,0),0.5)");
		assertEquals(expected, fingerprint(locus));
		assertEquals(expectedSubpaths, subpathFingerprint(locus));

		locus.setObjColor(GColor.GREEN);
		locus.setLineThickness(9);
		locus.setLineType(EuclidianStyleConstants.LINE_TYPE_DASHED_DOTTED);
		locus.setLabelVisible(true);
		locus.setSelected(true);
		locus.setHighlighted(true);
		assertEquals(expected, fingerprint(locus));
		assertEquals(expectedSubpaths, subpathFingerprint(locus));
		assertEquals(1, subpathCount(locus));
	}

	// R2-L12
	@Test
	void genuineDiscontinuityOpenUnboundedClippingDashAndOverdrawStayDistinct() {
		GeoLocusV2 disconnected = createDisconnectedLine();
		final String disconnectedFingerprint = fingerprint(disconnected);
		assertEquals(2, subpathCount(disconnected));

		add("openT=0");
		add("OpenQ=(openT,openT^3)");
		add("OpenD={false,{-2,-1,false,false},{1,2,false,false}}");
		GeoLocusV2 open = add("Open=LocusV2(OpenQ,openT,OpenD)");
		assertNotNull(open);
		LocusRenderData2D openData = render(open);
		assertEquals(2, subpathCount(open));
		assertTrue(openData.getVertices().stream().allMatch(vertex -> {
			double parameter = vertex.getSemanticParameter();
			return parameter > -2 && parameter < -1
					|| parameter > 1 && parameter < 2;
		}));

		GeoLocusV2 unbounded = unboundedTangent();
		LocusRenderData2D unboundedData = render(unbounded);
		assertEquals(1, subpathCount(unbounded));
		assertTrue(unboundedData.getVertices().stream().allMatch(vertex ->
				Double.isFinite(vertex.getPoint().getX())
						&& Double.isFinite(vertex.getPoint().getY())));
		assertTrue(unboundedData.getVertices().get(0).getSemanticParameter()
				> -Math.PI / 2);
		assertTrue(unboundedData.getVertices()
				.get(unboundedData.getVertices().size() - 1)
				.getSemanticParameter() < Math.PI / 2);
		RecordingGraphics clipped = drawAtScale(unbounded, 1000);
		assertTrue(clipped.shapeDraws > 0);
		assertTrue(clipped.maximumAbsoluteCoordinate < 10_000);

		disconnected.setLineType(
				EuclidianStyleConstants.LINE_TYPE_DASHED_DOTTED);
		RecordingGraphics dashed = drawAtScale(disconnected, 100);
		assertTrue(dashed.strokes.stream()
				.anyMatch(stroke -> stroke.getDashArray() != null));
		assertTrue(dashed.pathMoveCounts.stream().allMatch(count -> count == 2));
		add("gapCrossingLine:x=0");
		add("gapCrossingCircle=Circle((0,0),1)");
		add("gapOverdraw=Segment((-2,0),(2,0))");
		assertEquals(disconnectedFingerprint, fingerprint(disconnected));
		assertEquals(2, subpathCount(disconnected));
		assertFalse(fingerprint(open).equals(fingerprint(unbounded)));
	}

	private GeoLocusV2 unboundedTangent() {
		double endpoint = Math.PI / 2;
		ExplicitNumericDomainProvider2D provider =
				new ExplicitNumericDomainProvider2D("r2-unbounded-parameter/v1",
						new LocusInterval2D(-endpoint, endpoint, false, false),
						Orientation.INCREASING, false, 1E-14);
		LocusBranch2D branch = new LocusBranch2D("r2.unbounded.main",
				provider.getDeclaredDomain(),
				Collections.singletonList(provider.getDeclaredDomain()),
				Orientation.INCREASING, "r2-unbounded/v1",
				LocusLineage2D.unchanged(),
				EnumSet.of(BranchProperty.UNBOUNDED),
				LocusQuality2D.analyticDoubleSemantic());
		return LocusV2Factory.createAnalytic(LocusV2Mode.V2, getConstruction(),
				"r2-unbounded", new GeoNumeric(getConstruction(), 0), provider,
				Collections.singletonList(branch),
				(source, semanticBranch, parameter, session) ->
						new LocusPoint2D(parameter, Math.tan(parameter)),
				"r2-unbounded/v1");
	}

	private RecordingGraphics drawAtScale(GeoLocusV2 locus, double scale) {
		EuclidianView view = getApp().getEuclidianView1();
		view.setCoordSystem(400, 300, scale, scale);
		DrawLocusV2 drawable = new DrawLocusV2(view, locus);
		RecordingGraphics graphics = new RecordingGraphics();
		drawable.draw(graphics);
		return graphics;
	}

	private static int solutionCount(GeoLocusIntersectionResult result) {
		assertNotNull(result);
		assertNotNull(result.getIntersectionResult());
		return result.getIntersectionResult().getFiniteSolutions().size();
	}

	private static String fingerprint(GeoLocusV2 locus) {
		return render(locus).getVertices().stream().map(vertex ->
				Long.toHexString(Double.doubleToLongBits(vertex.getPoint().getX()))
						+ "," + Long.toHexString(Double.doubleToLongBits(
								vertex.getPoint().getY()))
						+ "," + Long.toHexString(Double.doubleToLongBits(
								vertex.getSemanticParameter()))
						+ "," + vertex.startsSubpath())
				.collect(Collectors.joining("|"));
	}

	private static String subpathFingerprint(GeoLocusV2 locus) {
		return render(locus).getVertices().stream().map(vertex ->
				Long.toHexString(Double.doubleToLongBits(
						vertex.getSemanticParameter())) + ":"
						+ vertex.startsSubpath())
				.collect(Collectors.joining("|"));
	}

	private static long subpathCount(GeoLocusV2 locus) {
		return render(locus).getVertices().stream()
				.filter(LocusRenderData2D.Vertex::startsSubpath).count();
	}

	private static LocusRenderData2D render(GeoLocusV2 locus) {
		return new LocusRenderCache2D().getOrBuild(locus, FIXED_POLICY);
	}

	private static int pathMoveCount(GShape shape) {
		int count = 0;
		GPathIterator iterator = shape.getPathIterator(null);
		double[] coordinates = new double[6];
		while (!iterator.isDone()) {
			if (iterator.currentSegment(coordinates) == GPathIterator.SEG_MOVETO) {
				count++;
			}
			iterator.next();
		}
		return count;
	}

	private static double maximumAbsoluteCoordinate(GShape shape) {
		double maximum = 0;
		GPathIterator iterator = shape.getPathIterator(null);
		double[] coordinates = new double[6];
		while (!iterator.isDone()) {
			int segment = iterator.currentSegment(coordinates);
			int coordinateCount = segment == GPathIterator.SEG_CUBICTO ? 6
					: segment == GPathIterator.SEG_QUADTO ? 4
							: segment == GPathIterator.SEG_CLOSE ? 0 : 2;
			for (int index = 0; index < coordinateCount; index++) {
				maximum = Math.max(maximum, Math.abs(coordinates[index]));
			}
			iterator.next();
		}
		return maximum;
	}

	private static final class RecordingGraphics extends GGraphicsCommon {
		private final List<GBasicStroke> strokes = new ArrayList<>();
		private final List<Integer> pathMoveCounts = new ArrayList<>();
		private int shapeDraws;
		private double maximumAbsoluteCoordinate;

		@Override
		public void setStroke(GBasicStroke stroke) {
			strokes.add(stroke);
		}

		@Override
		public void draw(GShape shape) {
			super.draw(shape);
			shapeDraws++;
			pathMoveCounts.add(pathMoveCount(shape));
			maximumAbsoluteCoordinate = Math.max(maximumAbsoluteCoordinate,
					G9U0R2LocusRenderContinuityTest
							.maximumAbsoluteCoordinate(shape));
		}
	}

	private record CrossingWitness(PersistentGeoId identity,
			LocusDefinition2D definition, long revision,
			LocusMetricResult2D metric,
			LocusIntersectionResult2D intersection, String tokenLedger,
			String renderFingerprint, String subpathFingerprint) {

		static CrossingWitness capture(GeoLocusV2 locus,
				GeoLocusMetricResult metric,
				GeoLocusIntersectionResult intersection) {
			return new CrossingWitness(locus.getPersistentLocusId(),
					locus.getSemanticDefinition(), locus.getSemanticRevision(),
					metric.getMetricResult(), intersection.getIntersectionResult(),
					intersection.getTokenLedgerState(), fingerprint(locus),
					G9U0R2LocusRenderContinuityTest.subpathFingerprint(locus));
		}

		void assertUnchanged(GeoLocusV2 locus,
				GeoLocusMetricResult metricGeo,
				GeoLocusIntersectionResult intersectionGeo) {
			assertEquals(identity, locus.getPersistentLocusId());
			assertSame(definition, locus.getSemanticDefinition());
			assertEquals(revision, locus.getSemanticRevision());
			assertSame(metric, metricGeo.getMetricResult());
			assertSame(intersection, intersectionGeo.getIntersectionResult());
			assertEquals(tokenLedger, intersectionGeo.getTokenLedgerState());
			assertEquals(renderFingerprint, fingerprint(locus));
			assertEquals(subpathFingerprint,
					G9U0R2LocusRenderContinuityTest.subpathFingerprint(locus));
		}
	}
}
