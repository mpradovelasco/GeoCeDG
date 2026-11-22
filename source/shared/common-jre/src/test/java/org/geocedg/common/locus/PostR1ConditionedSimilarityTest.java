/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter.richResultOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCell2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCellKind2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedTraversalPolicy2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * Increment I4T of the conditioned use of Locus V2 (ADR 0042; contract {@code
 * post-r1-conditioned-use.md}, section 8.7): a similarity image inherits the conditioned
 * evidence of its source cell by cell and states it against the policy in its own world.
 * Criteria 5 and 8a to 8e, on the real kernel; criterion 8f is the unchanged similarity
 * suite.
 */
final class PostR1ConditionedSimilarityTest extends G9U0PublicSurfaceTestBase {
	private static final double LIMIT = 1e-3;

	/**
	 * The unit circle outside the certified class: one region, the whole period, of 128
	 * spans whose chord deviation at the nodes is 3.01e-4.
	 */
	private GeoLocusV2 circle(String extra) {
		add("c=Circle((0,0),1)");
		add("E=Point(c)");
		add("Q=(x(E),y(E)+0*exp(x(E))" + extra + ")");
		GeoLocusV2 locus = add("L=LocusV2(Q,E)");
		assertNotNull(locus);
		return locus;
	}

	/** A locus on a segment outside the certified class. */
	private GeoLocusV2 segment(String abscissa, String ordinate) {
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(" + abscissa + "," + ordinate + ")");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		assertNotNull(locus);
		return locus;
	}

	private static List<String> shape(LocusConditionedEvidence2D evidence) {
		List<String> shape = new ArrayList<>();
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			shape.add(cell.getLowerIndex() + ":" + cell.getUpperIndex() + ":" + cell.getKind()
					+ ":" + cell.getSpanClass());
		}
		return shape;
	}

	private static int count(LocusConditionedEvidence2D evidence,
			LocusConditionedCellKind2D kind) {
		int count = 0;
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			count += cell.getKind() == kind ? 1 : 0;
		}
		return count;
	}

	private static double largestDeviation(LocusConditionedEvidence2D evidence) {
		double largest = 0;
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			if (cell.getKind().isSpan()) {
				largest = Math.max(largest, cell.getChordDeviation());
			}
		}
		return largest;
	}

	private void value(String label, double value) {
		GeoNumeric number = (GeoNumeric) lookup(label);
		number.setValue(value);
		number.updateCascade();
	}

	@Test
	void anIsometryAReductionAndRatioZeroInheritTheCellsUnchanged() {
		// Criterion 8a.
		GeoLocusV2 source = circle("");
		LocusConditionedEvidence2D evidence = source.getConditionedEvidence(BRANCH);
		assertEquals(128, count(evidence, LocusConditionedCellKind2D.SPAN));
		assertEquals(3.01e-4, largestDeviation(evidence), 1e-6);
		String[] images = {"Translate(L,(3,-2))", "Rotate(L,pi/3,(1,2))", "Reflect(L,y=x)",
				"Dilate(L,0.5,(1,1))", "Dilate(L,0,(4,5))"};
		double[] ratios = {1, 1, 1, 0.5, 0};
		for (int index = 0; index < images.length; index++) {
			GeoLocusV2 image = add("T" + index + "=" + images[index]);
			assertNotNull(image, images[index]);
			LocusConditionedEvidence2D inherited = image.getConditionedEvidence(BRANCH);
			assertNotNull(inherited, images[index]);
			// The same ends, kinds and classes; nothing is evaluated, merged or coarsened.
			assertEquals(shape(evidence), shape(inherited), images[index]);
			assertEquals(0, inherited.getWork(), images[index]);
			assertEquals(ratios[index] * largestDeviation(evidence),
					largestDeviation(inherited), 1e-12, images[index]);
			assertEquals(evidence.getRegions(), inherited.getRegions(), images[index]);
			// Its own content identity, which records that of the source.
			assertNotEquals(evidence.getContentIdentity(), inherited.getContentIdentity());
			assertEquals(1, inherited.getInheritedSources().size());
			assertTrue(inherited.getInheritedSources().get(0).endsWith(
					"@" + evidence.getContentIdentity()), images[index]);
		}
		// The nodes of the image are the images of the nodes of the source.
		double[] nodes = evidence.getCells().get(0).getNodePoints();
		double[] moved = ((GeoLocusV2) lookup("T0")).getConditionedEvidence(BRANCH)
				.getCells().get(0).getNodePoints();
		for (int index = 0; index < nodes.length; index += 2) {
			assertEquals(nodes[index] + 3, moved[index], 1e-12);
			assertEquals(nodes[index + 1] - 2, moved[index + 1], 1e-12);
		}
	}

	@Test
	void aBandOfTheSourceIsABandOfTheImageWhateverTheRatio() {
		// Criterion 8a, second part: a jump of 0.01 is a discontinuity band of the source;
		// at ratio 0.01 it is 1e-4, below the limit, and it is still a band of the image.
		GeoLocusV2 source = segment("x(P)+0.01*floor(x(P)+0.3)", "exp(x(P))");
		LocusConditionedEvidence2D evidence = source.getConditionedEvidence(BRANCH);
		assertEquals(1, count(evidence, LocusConditionedCellKind2D.DISCONTINUITY_BAND));
		GeoLocusV2 image = add("T=Dilate(L,0.01)");
		LocusConditionedEvidence2D inherited = image.getConditionedEvidence(BRANCH);
		assertEquals(shape(evidence), shape(inherited));
		assertEquals(1, count(inherited, LocusConditionedCellKind2D.DISCONTINUITY_BAND));
		assertEquals(evidence.getRegions(), inherited.getRegions());
		assertFalse(inherited.inOneRegion(0.6, 0.8));
		// A magnification inherits it too.
		GeoLocusV2 larger = add("U=Dilate(L,3)");
		assertEquals(1, count(larger.getConditionedEvidence(BRANCH),
				LocusConditionedCellKind2D.DISCONTINUITY_BAND));
	}

	@Test
	void aMagnificationChecksEverySpanInItsOwnWorld() {
		// Criterion 8b, the adverse case: the bench of the unit circle.
		GeoLocusV2 source = circle("");
		LocusConditionedEvidence2D evidence = source.getConditionedEvidence(BRANCH);
		assertEquals(128, count(evidence, LocusConditionedCellKind2D.SPAN));
		GeoLocusV2 three = add("T3=Dilate(L,3)");
		LocusConditionedEvidence2D inherited = three.getConditionedEvidence(BRANCH);
		// 3 * 3.01e-4 = 9.04e-4 meets the limit: the cells are inherited unchanged.
		assertEquals(shape(evidence), shape(inherited));
		assertEquals(9.04e-4, largestDeviation(inherited), 2e-6);
		// Only the two seam nodes are read: their gap is a length of the image.
		assertEquals(2, inherited.getWork());

		GeoLocusV2 four = add("T4=Dilate(L,4)");
		LocusConditionedEvidence2D refined = four.getConditionedEvidence(BRANCH);
		// 4 * 3.01e-4 = 1.20e-3 exceeds it: copying the cells would publish 128 spans of
		// 1.20e-3. Every span is classified anew, and none exceeds the limit.
		assertEquals(256, count(refined, LocusConditionedCellKind2D.SPAN));
		assertEquals(256, refined.getCells().size());
		assertEquals(3.01e-4, largestDeviation(refined), 1e-6);
		assertTrue(largestDeviation(refined) <= LIMIT);
		assertTrue(refined.getWork() > 0);
		assertEquals(1, refined.getRegions().size());
		assertEquals(evidence.getRegions(), refined.getRegions());
	}

	@Test
	void theContentOfTheImageIsAFunctionOfTheState() {
		// Criterion 8d.
		circle("");
		add("k=4");
		GeoLocusV2 image = add("T=Dilate(L,k)");
		LocusConditionedEvidence2D direct = image.getConditionedEvidence(BRANCH);
		assertEquals(256, count(direct, LocusConditionedCellKind2D.SPAN));
		value("k", 0.5);
		LocusConditionedEvidence2D reduced = image.getConditionedEvidence(BRANCH);
		assertEquals(128, count(reduced, LocusConditionedCellKind2D.SPAN));
		assertNotEquals(direct.getContentIdentity(), reduced.getContentIdentity());
		value("k", 4);
		LocusConditionedEvidence2D back = image.getConditionedEvidence(BRANCH);
		// The content identity of the direct ratio 4; the counters are free.
		assertEquals(direct.getContentIdentity(), back.getContentIdentity());
		assertEquals(shape(direct), shape(back));

		// An image of an image decides against its immediate source and never coarsens.
		GeoLocusV2 second = add("U=Dilate(T,0.25)");
		LocusConditionedEvidence2D nested = second.getConditionedEvidence(BRANCH);
		assertEquals(shape(back), shape(nested));
		assertEquals(256, count(nested, LocusConditionedCellKind2D.SPAN));
		assertTrue(nested.getInheritedSources().get(0).endsWith(
				"@" + back.getContentIdentity()));
		assertEquals(0, nested.getWork());
	}

	@Test
	void aSpanThatFailsAtTheMinimumStepIsADiscontinuityBandOfTheImage() {
		// Criterion 8c. A jump of 9e-4 across the curve is below the limit of the source:
		// its cell is a span. At ratio 4 it is 3.6e-3, and no subdivision absorbs it.
		GeoLocusV2 source = segment("x(P)+0.0009*floor(x(P)+0.3)", "exp(x(P))");
		LocusConditionedEvidence2D evidence = source.getConditionedEvidence(BRANCH);
		assertEquals(0, count(evidence, LocusConditionedCellKind2D.DISCONTINUITY_BAND));
		assertEquals(1, evidence.getRegions().size());
		assertTrue(evidence.inOneRegion(0.6, 0.8));
		GeoLocusV2 image = add("T=Dilate(L,4)");
		LocusConditionedEvidence2D refined = image.getConditionedEvidence(BRANCH);
		assertEquals(1, count(refined, LocusConditionedCellKind2D.DISCONTINUITY_BAND));
		LocusConditionedCell2D band = refined.bandAt(0.7);
		assertNotNull(band);
		assertEquals(LocusConditionedCellKind2D.DISCONTINUITY_BAND, band.getKind());
		assertTrue(band.getUpper() - band.getLower() < 4e-6);
		assertTrue(largestDeviation(refined) <= LIMIT);
		// The image refuses what the source admits across the band.
		assertFalse(refined.inOneRegion(0.6, 0.8));
		add("A=Point(T,\"" + BRANCH + "\",0.6)");
		add("B=Point(T,\"" + BRANCH + "\",0.8)");
		LocusMetricResult2D onImage = richResultOf((GeoElement) add(
				"M=LocusLength(T,A,B)")).getMetricResult();
		assertNotEquals(MetricComputationStatus.SUCCESS, onImage.getComputationStatus());
		add("B2=Point(T,\"" + BRANCH + "\",0.9)");
		assertEquals(MetricComputationStatus.SUCCESS, richResultOf((GeoElement) add(
				"M2=LocusLength(T,B,B2)")).getMetricResult().getComputationStatus());
		// A point at a valid parameter inside the band is defined.
		GeoPoint inside = add("C=Point(T,\"" + BRANCH + "\","
				+ (0.5 * (band.getLower() + band.getUpper())) + ")");
		assertTrue(inside.isDefined());
	}

	@Test
	void aCellThatTheWorkLimitDoesNotReachIsUnexploredAndNeverASpan() {
		// Criterion 8c, work limit: the policy of the source admits its 257 nodes and not
		// the nodes that the image adds at ratio 4.
		GeoLocusV2 source = circle("");
		((AlgoDependentPointLocusV2) source.getParentAlgorithm())
				.useConditionedTraversalPolicy(new LocusConditionedTraversalPolicy2D(
						"conditioned-traversal/test-work-300", 5, 20, 1e-3, 0.35, 1e-3, 300));
		LocusConditionedEvidence2D evidence = source.getConditionedEvidence(BRANCH);
		assertEquals(128, count(evidence, LocusConditionedCellKind2D.SPAN));
		GeoLocusV2 image = add("T=Dilate(L,4)");
		LocusConditionedEvidence2D refined = image.getConditionedEvidence(BRANCH);
		assertTrue(count(refined, LocusConditionedCellKind2D.UNEXPLORED) > 0);
		assertTrue(refined.getWork() <= 300);
		assertTrue(largestDeviation(refined) <= LIMIT);
		for (LocusConditionedCell2D cell : refined.getCells()) {
			assertTrue(cell.getKind() == LocusConditionedCellKind2D.SPAN
					|| cell.getKind() == LocusConditionedCellKind2D.UNEXPLORED);
		}
	}

	@Test
	void aNodeAddedByTheImageThatContradictsTheSourceIsReportedToItsProducer() {
		// Criterion 8e. The hole of radius 1e-5 lies at the node 301/512 of the period: the
		// traversal of the source, whose nodes are multiples of 1/256, does not meet it; the
		// image at ratio 4 adds that node.
		double hole = -Math.PI + (Math.PI - (-Math.PI)) * (301.0 / 512);
		GeoLocusV2 source = circle("+0*sqrt((x(E)-(" + Math.cos(hole) + "))^2+(y(E)-("
				+ Math.sin(hole) + "))^2-1E-10)");
		LocusConditionedEvidence2D before = source.getConditionedEvidence(BRANCH);
		assertEquals(128, count(before, LocusConditionedCellKind2D.SPAN));
		assertTrue(before.getContradictions().isEmpty());
		GeoLocusV2 image = add("T=Dilate(L,4)");
		LocusConditionedEvidence2D refined = image.getConditionedEvidence(BRANCH);
		// The producer of the source confirmed it: it is in the content of the source.
		LocusConditionedEvidence2D after = source.getConditionedEvidence(BRANCH);
		// The reported node is the first node inside the hole that the image met.
		assertEquals(1, after.getContradictions().size());
		assertEquals(hole, after.getContradictions().get(0), 1e-5);
		assertNotNull(after.bandAt(hole));
		assertNotEquals(before.getContentIdentity(), after.getContentIdentity());
		// The image inherits the band, and its content identity records that of the source.
		assertNotNull(refined.bandAt(hole));
		assertTrue(refined.getInheritedSources().get(0).endsWith(
				"@" + after.getContentIdentity()));
		assertTrue(refined.getContradictions().isEmpty());
		assertTrue(largestDeviation(refined) <= LIMIT);
		assertFalse(refined.inOneRegion(hole - 0.01, hole + 0.01));
	}

	@Test
	void ratioZeroOnAConditionedSourceKeepsTheApprovedCollapsedImage() {
		// Criterion 5.
		GeoLocusV2 source = segment("x(P)", "sqrt(x(P)-0.3)+exp(x(P))");
		LocusConditionedEvidence2D evidence = source.getConditionedEvidence(BRANCH);
		add("k=2");
		GeoLocusV2 image = add("T=Dilate(L,k,(4,5))");
		GeoPoint point = add("W=Point(T,\"" + BRANCH + "\",0.6)");
		assertTrue(point.isDefined());
		final double x = point.getInhomX();
		final double y = point.getInhomY();
		value("k", 0);
		assertTrue(image.isDefined());
		LocusBranch2D branch = image.getSemanticDefinition().getBranch(BRANCH);
		assertTrue(branch.getProperties().contains(BranchProperty.COLLAPSED_IMAGE));
		assertEquals(source.getSemanticDefinition().getBranch(BRANCH)
				.getDeclaredDriverDomain(), branch.getDeclaredDriverDomain());
		// The cells are those of the source: no cell is classified anew because the image
		// collapsed, and the invalid gaps stay invalid.
		LocusConditionedEvidence2D collapsed = image.getConditionedEvidence(BRANCH);
		assertEquals(shape(evidence), shape(collapsed));
		assertEquals(0, largestDeviation(collapsed), 0);
		assertFalse(image.sampleConditioned(BRANCH, 0.1).isValid());
		assertTrue(image.sampleConditioned(BRANCH, 0.6).isValid());
		// An existing point keeps its address, evaluates at the centre and recovers.
		point = (GeoPoint) lookup("W");
		assertTrue(point.isDefined());
		assertEquals(4, point.getInhomX(), 1e-12);
		assertEquals(5, point.getInhomY(), 1e-12);
		// The total length is the zero value of the metric contract for a collapsed image.
		LocusMetricResult2D total = richResultOf((GeoElement) add("M=LocusLength(T)"))
				.getMetricResult();
		assertEquals(MetricComputationStatus.SUCCESS, total.getComputationStatus());
		assertEquals(0, total.getMetricValue().getFiniteValue().orElseThrow(), 0);
		assertTrue(total.getDiagnostics().toString().contains("COLLAPSED_IMAGE"));
		// A target through the centre is the non-isolated case: no root is fabricated.
		add("through:x=4");
		GeoLocusIntersectionResult rich = add("v=Intersect(T,through)");
		assertTrue(rich.getIntersectionResult().getFiniteSolutions().isEmpty());
		assertEquals(GeometryKind.UNRESOLVED, rich.getIntersectionResult().getGeometryKind());
		value("k", 2);
		point = (GeoPoint) lookup("W");
		assertTrue(point.isDefined());
		assertEquals(x, point.getInhomX(), 1e-12);
		assertEquals(y, point.getInhomY(), 1e-12);
	}
}
