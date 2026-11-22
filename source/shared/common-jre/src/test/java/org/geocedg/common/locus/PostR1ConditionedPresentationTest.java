/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.euclidian.draw.DrawLocusV2;
import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.ConditionedRegion;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.Fidelity;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.Mark;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.MarkKind;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.Vertex;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCell2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCellKind2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidenceOwner2D.ReportOutcome;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedHypothesis2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedState2D;
import org.geogebra.common.awt.GBasicStroke;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GGraphicsCommon;
import org.geogebra.common.awt.GPaint;
import org.geogebra.common.awt.GPathIterator;
import org.geogebra.common.awt.GShape;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.junit.jupiter.api.Test;

/**
 * Increment I2 of the conditioned use of Locus V2 (ADR 0042; contract {@code
 * post-r1-conditioned-use.md}, section 9): a conditioned region is one stroke with the
 * style of the locus, never joined across a band; a system mark, a hollow rhombus of 7
 * pixels, identifies each visible region independently of the style; and the state of the
 * object is a reading of the published content.
 */
final class PostR1ConditionedPresentationTest extends G9U0PublicSurfaceTestBase {

	private GeoLocusV2 outsideClass(String abscissa, String ordinate) {
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(" + abscissa + "," + ordinate + ")");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		assertNotNull(locus);
		return locus;
	}

	private static LocusRenderPolicy2D policy(double scale) {
		return LocusRenderPolicy2D.adaptive(1, 800, 600, scale, scale, 32, 0.75, 12);
	}

	private static LocusRenderData2D render(GeoLocusV2 locus, double scale) {
		return new LocusRenderCache2D().getOrBuild(locus, policy(scale));
	}

	private static List<Mark> marks(LocusRenderData2D data, MarkKind kind) {
		List<Mark> found = new ArrayList<>();
		for (Mark mark : data.getMarks()) {
			if (mark.getKind() == kind) {
				found.add(mark);
			}
		}
		return found;
	}

	@Test
	void aConditionedRegionIsOneStrokeAndTheDrawingIsNeverCalledComplete() {
		GeoLocusV2 locus = outsideClass("x(P)", "exp(x(P))");
		LocusRenderData2D data = render(locus, 40);
		assertTrue(data.getVertices().isEmpty(), "no certified component is drawn");
		assertTrue(data.getExploratoryVertices().isEmpty(),
				"the regions replace the dotted exploratory evidence");
		assertEquals(1, data.getConditionedRegions().size());
		ConditionedRegion region = data.getConditionedRegions().get(0);
		assertEquals(BRANCH, region.getBranchKey());
		assertEquals(0.0, region.getLower(), 0);
		assertEquals(1.0, region.getUpper(), 0);
		List<Vertex> stroke = data.getConditionedVertices();
		assertEquals(region.getVertexCount(), stroke.size());
		assertTrue(stroke.size() > 16);
		// One subpath, in increasing parameter, on the curve.
		for (int index = 0; index < stroke.size(); index++) {
			Vertex vertex = stroke.get(index);
			assertEquals(index == 0, vertex.startsSubpath(), "vertex " + index);
			assertEquals(Math.exp(vertex.getPoint().getX()), vertex.getPoint().getY(), 1e-12);
			assertEquals(vertex.getPoint().getX(), vertex.getSemanticParameter(), 1e-12);
			if (index > 0) {
				assertTrue(vertex.getSemanticParameter()
						> stroke.get(index - 1).getSemanticParameter());
			}
		}
		assertEquals(1, marks(data, MarkKind.CONDITIONED_REGION).size());
		assertEquals(Fidelity.PARTIAL, data.getFidelity());
		// Drawing certifies nothing.
		assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED, locus
				.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getCompleteness());
	}

	@Test
	void noStrokeCrossesABand() {
		// An absence up to 0.3 and a jump at 0.75: two regions and three kinds of band.
		GeoLocusV2 locus = outsideClass("x(P)+floor(x(P)+0.25)",
				"sqrt(x(P)-0.3)+exp(x(P))");
		LocusConditionedEvidence2D evidence = locus.getConditionedEvidence(BRANCH);
		assertEquals(2, evidence.getRegions().size());
		for (double scale : new double[] {40, 4000}) {
			LocusRenderData2D data = render(locus, scale);
			assertEquals(2, data.getConditionedRegions().size(), "scale " + scale);
			int starts = 0;
			for (Vertex vertex : data.getConditionedVertices()) {
				starts += vertex.startsSubpath() ? 1 : 0;
				double parameter = vertex.getSemanticParameter();
				assertNotNull(evidence.regionOf(parameter) != null ? Boolean.TRUE
						: evidence.getRegions().stream().anyMatch(region
								-> region.getLower() == parameter || region.getUpper() == parameter)
										? Boolean.TRUE : null,
						"vertex inside a region, parameter " + parameter);
				for (LocusConditionedCell2D cell : evidence.cellsContaining(parameter)) {
					assertTrue(cell.getKind().isSpan()
							|| evidence.cellsContaining(parameter).size() == 2,
							"no vertex strictly inside a band");
				}
			}
			assertEquals(2, starts, "each region is its own subpath, scale " + scale);
			for (ConditionedRegion region : data.getConditionedRegions()) {
				Vertex first = data.getConditionedVertices().get(region.getFirstVertex());
				Vertex last = data.getConditionedVertices().get(region.getFirstVertex()
						+ region.getVertexCount() - 1);
				assertTrue(first.startsSubpath());
				assertTrue(first.getSemanticParameter() >= region.getLower());
				assertTrue(last.getSemanticParameter() <= region.getUpper());
			}
		}
	}

	@Test
	void theStyleIsTheUsersAndTheMarkIsIndependentOfIt() {
		GeoLocusV2 locus = outsideClass("x(P)", "exp(x(P))");
		EuclidianView view = getApp().getEuclidianView1();
		view.setCoordSystem(100, 500, 200, 200);
		double[] anchor = null;
		for (int[] style : new int[][] {{EuclidianStyleConstants.LINE_TYPE_FULL, 2},
				{EuclidianStyleConstants.LINE_TYPE_DASHED_LONG, 7},
				{EuclidianStyleConstants.LINE_TYPE_DOTTED, 13}}) {
			locus.setLineType(style[0]);
			locus.setLineThickness(style[1]);
			locus.setObjColor(GColor.newColor(200, 30, 60));
			locus.updateRepaint();
			DrawLocusV2 drawable = new DrawLocusV2(view, locus);
			Recorder graphics = new Recorder();
			drawable.draw(graphics);
			// First the stroke of the region, with the stroke of the object: its line type
			// and its thickness, opaque. No dotted translucent evidence is drawn.
			assertEquals(2, graphics.drawn.size(), "stroke and mark, style " + style[0]);
			GBasicStroke stroke = graphics.strokes.get(0);
			assertEquals(java.util.Arrays.toString(org.geogebra.common.euclidian
					.EuclidianStatic.getStroke(style[1] / 2f, style[0]).getDashArray()),
					java.util.Arrays.toString(stroke.getDashArray()));
			assertEquals(style[1] / 2.0, stroke.getLineWidth(), 1e-9);
			assertEquals(255, graphics.colors.get(0).getAlpha());
			assertTrue(graphics.drawn.get(0)[4] > 16, "the region stroke has many segments");

			// Then the system mark: a closed rhombus of 7 pixels, hollow, outlined by a solid
			// stroke of one pixel with the colour of the object.
			double[] mark = graphics.drawn.get(1);
			assertEquals(7.0, mark[2] - mark[0], 1e-9, "width in pixels");
			assertEquals(7.0, mark[3] - mark[1], 1e-9, "height in pixels");
			assertEquals(5.0, mark[4], 0, "four sides and a closure");
			assertEquals(1.0, graphics.strokes.get(1).getLineWidth(), 0);
			assertTrue(graphics.strokes.get(1).getDashArray() == null
					|| graphics.strokes.get(1).getDashArray().length == 0);
			assertEquals(locus.getObjectColor().getARGB() | 0xFF000000,
					graphics.colors.get(1).getARGB() | 0xFF000000);
			assertEquals(1, graphics.filled.size(), "hollow: filled with the background");
			double[] anchors = drawable.getConditionedMarkAnchors();
			assertEquals(2, anchors.length);
			if (anchor != null) {
				assertEquals(anchor[0], anchors[0], 0, "independent of the style");
				assertEquals(anchor[1], anchors[1], 0);
			}
			anchor = anchors;
			// The mark has no hit area: away from the stroke nothing is hit.
			assertFalse(drawable.hit((int) anchors[0] + 40, (int) anchors[1] + 40, 3));
		}
		// Another zoom: the mark keeps its size in pixels and sits on the visible stroke.
		view.setCoordSystem(100, 500, 37, 37);
		DrawLocusV2 zoomed = new DrawLocusV2(view, locus);
		Recorder graphics = new Recorder();
		zoomed.draw(graphics);
		double[] mark = graphics.drawn.get(graphics.drawn.size() - 1);
		assertEquals(7.0, mark[2] - mark[0], 1e-9);
		assertEquals(7.0, mark[3] - mark[1], 1e-9);
		// A region that is not visible has no mark.
		view.setCoordSystem(-5000, 500, 37, 37);
		assertEquals(0, new DrawLocusV2(view, locus).getConditionedMarkAnchors().length);
	}

	@Test
	void aCertifiedLocusIsDrawnAsBeforeAndHasNoMark() {
		GeoLocusV2 locus = createLine();
		LocusRenderData2D data = render(locus, 40);
		assertFalse(data.getVertices().isEmpty());
		assertTrue(data.getConditionedVertices().isEmpty());
		assertTrue(data.getConditionedRegions().isEmpty());
		assertTrue(marks(data, MarkKind.CONDITIONED_REGION).isEmpty());
		assertEquals(Fidelity.COMPLETE, data.getFidelity());
		EuclidianView view = getApp().getEuclidianView1();
		view.setCoordSystem(400, 300, 50, 50);
		DrawLocusV2 drawable = new DrawLocusV2(view, locus);
		assertEquals(0, drawable.getConditionedMarkAnchors().length);
		Recorder graphics = new Recorder();
		drawable.draw(graphics);
		assertEquals(1, graphics.drawn.size());
		assertTrue(graphics.filled.isEmpty());
		assertNull(LocusConditionedState2D.of(locus));
	}

	@Test
	void aPublishedContentIsDrawnAgainAndTheDrawingDecidesNothing() {
		GeoLocusV2 locus = outsideClass("x(P)",
				"x(P)+0*sqrt((x(P)-0.3)^2-1E-8)+0*exp(x(P))");
		LocusRenderCache2D cache = new LocusRenderCache2D();
		LocusRenderData2D before = cache.getOrBuild(locus, policy(40));
		assertEquals(1, before.getConditionedRegions().size());
		assertEquals(before, cache.getOrBuild(locus, policy(40)));
		long builds = cache.getBuilds();
		long revision = locus.getSemanticRevision();
		assertEquals(ReportOutcome.CONFIRMED,
				locus.reportConditionedContradiction(BRANCH, 0.3));
		LocusRenderData2D after = cache.getOrBuild(locus, policy(40));
		assertEquals(builds + 1, cache.getBuilds(), "another content is another drawing");
		assertEquals(2, after.getConditionedRegions().size());
		assertEquals(revision, locus.getSemanticRevision());
		assertEquals(revision, after.getSemanticRevision());
		for (Vertex vertex : after.getConditionedVertices()) {
			assertFalse(Math.abs(vertex.getSemanticParameter() - 0.3) < 1e-7);
		}
	}

	@Test
	void theStateOfTheObjectIsAReadingOfThePublishedContent() {
		GeoLocusV2 locus = outsideClass("x(P)+floor(x(P)+0.25)",
				"sqrt(x(P)-0.3)+exp(x(P))");
		LocusConditionedState2D state = LocusConditionedState2D.of(locus);
		assertNotNull(state);
		assertEquals(1, state.getBranches().size());
		LocusConditionedState2D.Branch branch = state.getBranches().get(0);
		assertEquals(BRANCH, branch.getBranchKey());
		assertEquals("conditioned-traversal/v1", branch.getPolicyVersion());
		assertEquals(2, branch.getRegions().size());
		assertEquals(2, state.getRegionCount());
		assertEquals(1, branch.getBands().get(LocusConditionedCellKind2D.ABSENT_BAND));
		assertEquals(1, branch.getBands().get(LocusConditionedCellKind2D.TRANSITION_BAND));
		assertEquals(1, branch.getBands().get(LocusConditionedCellKind2D.DISCONTINUITY_BAND));
		assertEquals(List.of(LocusConditionedHypothesis2D.H1_VALIDITY,
				LocusConditionedHypothesis2D.H2_SELECTION,
				LocusConditionedHypothesis2D.H3_CONTINUITY,
				LocusConditionedHypothesis2D.H4_RESOLUTION), state.getHypotheses());
		assertEquals(0, branch.getContradictions());
	}

	/** Records what a drawable draws: paints, strokes, and the bounds of each shape. */
	private static final class Recorder extends GGraphicsCommon {
		private final List<GColor> colors = new ArrayList<>();
		private final List<GBasicStroke> strokes = new ArrayList<>();
		/** Per drawn shape: minimum x, minimum y, maximum x, maximum y, segments. */
		private final List<double[]> drawn = new ArrayList<>();
		private final List<double[]> filled = new ArrayList<>();
		private GPaint paint;
		private GBasicStroke stroke;

		@Override
		public void setPaint(GPaint value) {
			paint = value;
		}

		@Override
		public void setColor(GColor value) {
			paint = value;
		}

		@Override
		public void setStroke(GBasicStroke value) {
			stroke = value;
		}

		@Override
		public void draw(GShape shape) {
			colors.add((GColor) paint);
			strokes.add(stroke);
			drawn.add(bounds(shape));
		}

		@Override
		public void fill(GShape shape) {
			filled.add(bounds(shape));
		}

		private static double[] bounds(GShape shape) {
			double[] bounds = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
					Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 0};
			GPathIterator iterator = shape.getPathIterator(null);
			double[] coordinates = new double[6];
			while (!iterator.isDone()) {
				int type = iterator.currentSegment(coordinates);
				bounds[4]++;
				if (type != GPathIterator.SEG_CLOSE) {
					bounds[0] = Math.min(bounds[0], coordinates[0]);
					bounds[1] = Math.min(bounds[1], coordinates[1]);
					bounds[2] = Math.max(bounds[2], coordinates[0]);
					bounds[3] = Math.max(bounds[3], coordinates[1]);
				}
				iterator.next();
			}
			return bounds;
		}
	}
}
