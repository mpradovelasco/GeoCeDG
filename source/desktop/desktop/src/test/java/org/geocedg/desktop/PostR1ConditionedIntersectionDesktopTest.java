/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCell2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * Increments I4 and I4T of the conditioned use of Locus V2 (ADR 0042) on the author model:
 * a segment across a hole locus that has no certified component gives a root with a durable
 * token and a point on it; the token is withheld when the root of its selector lies in the
 * other conditioned region and is current again when it returns; a similarity image inherits
 * the cells of the hole locus; and the native document reads everything back.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostR1ConditionedIntersectionDesktopTest {
	@TempDir
	Path work;

	@Test
	void aRootOnAHoleLocusIsIdentifiedWithheldInheritedAndReopened() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			Map<String, Object> out = new LinkedHashMap<>();
			AppGeoCeDG app = PreG9BCu0Corpus.open(PostR1Corpus.workingCopy(
					PostR1Corpus.original(), work));
			GeoLocusV2 hole = PreG9BCu0Corpus.locus(app, "w_2");
			LocusConditionedEvidence2D evidence = hole.getConditionedEvidence(
					PostR1Corpus.MAIN);
			assertNotNull(evidence);
			out.put("regions", evidence.getRegions().size());
			double firstAt = middle(evidence, 0, 0.5);
			double secondAt = middle(evidence, 1, 0.4);
			double secondMoved = middle(evidence, 1, 0.6);

			// A short segment across the curve at a parameter, along its normal.
			double[] ends = across(hole, secondAt);
			PostR1Corpus.command(app, "CUA=(" + ends[0] + "," + ends[1] + ")");
			PostR1Corpus.command(app, "CUB=(" + ends[2] + "," + ends[3] + ")");
			PostR1Corpus.command(app, "cuS=Segment(CUA,CUB)");
			GeoLocusIntersectionResult rich = (GeoLocusIntersectionResult) PostR1Corpus
					.command(app, "cuV=Intersect(w_2,cuS)")[0];
			List<LocusIntersectionSolution2D> roots =
					rich.getIntersectionResult().getFiniteSolutions();
			out.put("roots", roots.size());
			LocusIntersectionSolution2D root = roots.get(0);
			String token = root.getIdentity().getRootToken();
			out.put("allocated", root.getIdentity().getIdentityStatus());
			out.put("declared", root.getDiagnostics().toString()
					.contains("CONDITIONED_REGION_ROOT"));
			out.put("ledger version", rich.getTokenLedgerState().substring(0, 2));
			GeoPoint point = (GeoPoint) PostR1Corpus.command(app,
					"CUR=Intersect(cuV,\"" + token + "\")")[0];
			out.put("point defined", point.isDefined());
			out.put("point on the curve", distance(point, point(hole, secondAt)));

			// The root moves inside the region of its origin: the same token.
			move(app, hole, secondMoved);
			out.put("moved token", token.equals(only(rich).getIdentity().getRootToken()));
			out.put("moved status", only(rich).getIdentity().getIdentityStatus());
			out.put("moved point", point.isDefined());

			// The root of the same selector in the other region: the token is withheld.
			move(app, hole, firstAt);
			List<LocusIntersectionSolution2D> other =
					rich.getIntersectionResult().getFiniteSolutions();
			out.put("other roots", other.size());
			boolean held = false;
			for (LocusIntersectionSolution2D each : other) {
				held |= token.equals(each.getIdentity().getRootToken());
			}
			out.put("other holds the token", held);
			out.put("other point", point.isDefined());
			String withheld = rich.getTokenLedgerState();

			// Native document in the withheld state, then back in the region of the origin.
			File saved = work.resolve("conditioned-intersection.cedg").toFile();
			out.put("saved", app.saveGeoGebraFile(saved));
			move(app, hole, secondAt);
			out.put("back token", token.equals(only(rich).getIdentity().getRootToken()));
			out.put("back point", point.isDefined());

			// A similarity image inherits the cells of the hole locus.
			GeoLocusV2 image = (GeoLocusV2) PostR1Corpus.command(app,
					"cuT=Rotate(w_2,pi/3)")[0];
			LocusConditionedEvidence2D inherited = image.getConditionedEvidence(
					PostR1Corpus.MAIN);
			out.put("image cells", shape(evidence).equals(shape(inherited)));
			out.put("image work", inherited.getWork());
			out.put("image records the source", inherited.getInheritedSources().get(0)
					.endsWith("@" + evidence.getContentIdentity()));

			AppGeoCeDG reopened = PreG9BCu0Corpus.open(saved);
			GeoLocusIntersectionResult loaded = (GeoLocusIntersectionResult) reopened
					.getKernel().lookupLabel("cuV");
			out.put("reopened ledger", withheld.equals(loaded.getTokenLedgerState()));
			GeoElement loadedPoint = reopened.getKernel().lookupLabel("CUR");
			out.put("reopened point", loadedPoint.isDefined());
			GeoLocusV2 loadedHole = PreG9BCu0Corpus.locus(reopened, "w_2");
			move(reopened, loadedHole, secondAt);
			out.put("reopened back token", token.equals(only(loaded).getIdentity()
					.getRootToken()));
			out.put("reopened back point", reopened.getKernel().lookupLabel("CUR")
					.isDefined());
			return out;
		});
		assertEquals(2, row.get("regions"), row.toString());
		assertEquals(1, row.get("roots"), row.toString());
		assertEquals(IdentityStatus.NEW_TOPOLOGICAL_SOLUTION, row.get("allocated"),
				row.toString());
		assertEquals(Boolean.TRUE, row.get("declared"), row.toString());
		assertEquals("7|", row.get("ledger version"), row.toString());
		assertEquals(Boolean.TRUE, row.get("point defined"), row.toString());
		assertTrue((Double) row.get("point on the curve") < 1e-6, row.toString());
		assertEquals(Boolean.TRUE, row.get("moved token"), row.toString());
		assertEquals(IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED,
				row.get("moved status"), row.toString());
		assertEquals(Boolean.TRUE, row.get("moved point"), row.toString());
		assertEquals(1, row.get("other roots"), row.toString());
		assertEquals(Boolean.FALSE, row.get("other holds the token"), row.toString());
		assertEquals(Boolean.FALSE, row.get("other point"), row.toString());
		assertEquals(Boolean.TRUE, row.get("saved"), row.toString());
		assertEquals(Boolean.TRUE, row.get("back token"), row.toString());
		assertEquals(Boolean.TRUE, row.get("back point"), row.toString());
		assertEquals(Boolean.TRUE, row.get("image cells"), row.toString());
		assertEquals(0, row.get("image work"), row.toString());
		assertEquals(Boolean.TRUE, row.get("image records the source"), row.toString());
		assertEquals(Boolean.TRUE, row.get("reopened ledger"), row.toString());
		assertEquals(Boolean.FALSE, row.get("reopened point"), row.toString());
		assertEquals(Boolean.TRUE, row.get("reopened back token"), row.toString());
		assertEquals(Boolean.TRUE, row.get("reopened back point"), row.toString());
	}

	private static LocusIntersectionSolution2D only(GeoLocusIntersectionResult rich) {
		List<LocusIntersectionSolution2D> roots =
				rich.getIntersectionResult().getFiniteSolutions();
		assertEquals(1, roots.size(), rich.getIntersectionResult().getDiagnostics().toString());
		return roots.get(0);
	}

	/** Moves the segment across the curve to another parameter, with the same sense. */
	private static void move(AppGeoCeDG app, GeoLocusV2 hole, double parameter) {
		double[] ends = across(hole, parameter);
		GeoPoint first = (GeoPoint) app.getKernel().lookupLabel("CUA");
		GeoPoint second = (GeoPoint) app.getKernel().lookupLabel("CUB");
		first.setCoords(ends[0], ends[1], 1.0);
		second.setCoords(ends[2], ends[3], 1.0);
		GeoElement.updateCascade(new ArrayList<>(List.of(first, second)),
				new java.util.TreeSet<>(), true);
	}

	/** The ends of a short segment through the point of a parameter, along its normal. */
	private static double[] across(GeoLocusV2 hole, double parameter) {
		double[] at = point(hole, parameter);
		double[] before = point(hole, parameter - 1e-4);
		double[] after = point(hole, parameter + 1e-4);
		double tx = after[0] - before[0];
		double ty = after[1] - before[1];
		double norm = Math.hypot(tx, ty);
		double reach = 0.05;
		double nx = -ty / norm * reach;
		double ny = tx / norm * reach;
		return new double[] {at[0] - nx, at[1] - ny, at[0] + nx, at[1] + ny};
	}

	private static double middle(LocusConditionedEvidence2D evidence, int region,
			double fraction) {
		double lower = evidence.getRegions().get(region).getLower();
		double upper = evidence.getRegions().get(region).getUpper();
		return lower + fraction * (upper - lower);
	}

	private static List<String> shape(LocusConditionedEvidence2D evidence) {
		List<String> shape = new ArrayList<>();
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			shape.add(cell.getLowerIndex() + ":" + cell.getUpperIndex() + ":" + cell.getKind()
					+ ":" + cell.getSpanClass());
		}
		return shape;
	}

	private static double distance(GeoPoint point, double[] expected) {
		return Math.hypot(point.getInhomX() - expected[0], point.getInhomY() - expected[1]);
	}

	private static double[] point(GeoLocusV2 locus, double parameter) {
		LocusEvaluation2D evaluation = locus.evaluate(PostR1Corpus.MAIN, parameter,
				LocusEvaluationSession2D.reference());
		assertTrue(evaluation.isValid(), "parameter " + parameter);
		return new double[] {evaluation.getPoint().getX(), evaluation.getPoint().getY()};
	}
}
