/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.geocedg.common.kernel.locus.constructive.ConstructiveCone;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Circle;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Incidence;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.IncidenceResult;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Line;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Point;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Result;
import org.geocedg.common.kernel.locus.constructive.ExactEvaluationBudget;
import org.geocedg.common.kernel.locus.constructive.ExactQuadraticTower.Element;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoCirclePointRadius;
import org.geogebra.common.kernel.algos.AlgoCircleTwoPoints;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectLines;
import org.geogebra.common.kernel.algos.AlgoIntersectSingle;
import org.geogebra.common.kernel.algos.AlgoJoinPoints;
import org.geogebra.common.kernel.algos.AlgoJoinPointsRay;
import org.geogebra.common.kernel.algos.AlgoJoinPointsSegment;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoOrthoLinePointLine;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU3 increment E1, gate G-AUT-1 on the author model ({@code InterCilConoOblique.cedg},
 * without {@code e_2}): the census of the relations that the provenance of every object declares
 * (a point on a line or a circle, the membership of a limited part, parallels, perpendiculars,
 * centres and radii), decided exactly by the constructive evaluator of the product under the
 * approved budget. With the semantics of points on paths of D-AUT-3 none of the 160 is refuted:
 * 140 hold exactly, and the evaluation of twenty in the cone of the rotation exceeds the bits of
 * {@code cu3-exact-evaluation-budget/v1} and fails closed; the constructive authority study
 * decided all 160 under a characterization budget. The census enters only as a regression (matrix
 * row V-AUT-PROV-1). A free point at the binary64 coordinates of a point on a path refutes its
 * path unless the path is parallel to an axis.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3ProvenanceCensusTest {

	/** The relations whose exact evaluation exceeds the bits of the approved budget. */
	private static final List<String> EXHAUSTED = List.of("H on f_1", "S on f_1", "W on f_1",
			"W on i_1", "W in_limited_part f_1", "H on j_1", "U on j_1", "A_1 on j_1",
			"A_1 on i_1", "A_1 in_limited_part j_1", "W on h_1", "h_1 parallel k", "A_1 on k_1",
			"k_1 parallel k", "B_1 on m_1", "B_1 on h_1", "B_1 in_limited_part m_1", "C_1 on l_1",
			"C_1 on k_1", "C_1 in_limited_part l_1");

	@TempDir
	Path work;

	@Test
	void everyRelationThatTheProvenanceDeclaresHoldsExactly() throws Exception {
		Map<String, Object> rows = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			Construction construction = app.getKernel().getConstruction();
			List<Relation> relations = relations(construction);
			Map<String, Integer> kinds = new TreeMap<>();
			List<String> failed = new ArrayList<>();
			List<String> exhausted = new ArrayList<>();
			List<Relation> pathPoints = new ArrayList<>();
			for (Relation relation : relations) {
				kinds.merge(relation.kind, 1, Integer::sum);
				String outcome = decide(construction, relation);
				if (outcome.contains("EXACT_EVALUATION_BUDGET_EXHAUSTED")) {
					exhausted.add(relation.toString());
				} else if (!"HOLDS".equals(outcome)) {
					failed.add(relation + ": " + outcome);
				}
				if ("on".equals(relation.kind) && relation.subject instanceof GeoPoint
						&& ((GeoPoint) relation.subject).isPointOnPath()
						&& ((GeoPoint) relation.subject).getPath() == relation.object) {
					pathPoints.add(relation);
				}
			}
			// The adversarial reading: a free point at the binary64 coordinates.
			List<String> refutedCopies = new ArrayList<>();
			int copy = 0;
			for (Relation relation : pathPoints) {
				GeoPoint point = (GeoPoint) relation.subject;
				GeoElement free = PreG9BR6PlusF3Fixtures.must(app, "CensusCopy" + (++copy)
						+ "=(" + point.getInhomX() + "," + point.getInhomY() + ")");
				String outcome = decide(construction,
						new Relation("on", free, relation.object));
				if (!"HOLDS".equals(outcome)) {
					refutedCopies.add(relation.toString());
				}
			}
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("count", relations.size());
			values.put("kinds", kinds);
			values.put("failed", failed);
			values.put("exhausted", exhausted);
			values.put("path points", pathPoints.size());
			values.put("refuted copies", refutedCopies);
			return values;
		});
		assertEquals(160, rows.get("count"), rows.toString());
		assertEquals(List.of(), rows.get("failed"), "V-AUT-PROV-1: " + rows);
		// The study decided the census under a characterization budget. Under the approved
		// cu3-exact-evaluation-budget/v1, twenty relations of the cone of the rotation need more
		// than its 4 096 bits: they fail closed, undecided, and none is accepted or refuted.
		assertEquals(EXHAUSTED, rows.get("exhausted"), rows.toString());
		assertEquals(new TreeSet<>(List.of("H on j", "A_2 on b", "N on g_1", "E_1 on p")),
				new TreeSet<>((List<?>) rows.get("refuted copies")),
				"the binary64 copy of a point on a path that is not parallel to an axis: " + rows);
	}

	/** One relation that the provenance of an object declares. */
	private static final class Relation {
		final String kind;
		final GeoElement subject;
		final GeoElement object;

		Relation(String kind, GeoElement subject, GeoElement object) {
			this.kind = kind;
			this.subject = subject;
			this.object = object;
		}

		@Override
		public String toString() {
			return subject.getLabelSimple() + " " + kind + " " + object.getLabelSimple();
		}
	}

	/** The relations of the study census, in construction order. */
	private static List<Relation> relations(Construction construction) {
		List<Relation> relations = new ArrayList<>();
		for (GeoElement geo : construction.getGeoSetConstructionOrder()) {
			if (!geo.isLabelSet()) {
				continue;
			}
			AlgoElement parent = geo.getParentAlgorithm();
			if (geo instanceof GeoPoint && ((GeoPoint) geo).isPointOnPath()) {
				org.geogebra.common.kernel.Path path = ((GeoPoint) geo).getPath();
				if (path instanceof GeoLine || path instanceof GeoConic
						&& ((GeoConic) path).isCircle()) {
					relations.add(new Relation("on", geo, (GeoElement) path));
					if (path instanceof GeoSegment || path instanceof GeoRay) {
						relations.add(new Relation("in_limited_part", geo, (GeoElement) path));
					}
				}
				continue;
			}
			if (parent instanceof AlgoIntersectLines) {
				AlgoIntersectLines meet = (AlgoIntersectLines) parent;
				relations.add(new Relation("on", geo, meet.getg()));
				relations.add(new Relation("on", geo, meet.geth()));
				addLimited(relations, geo, meet.getg());
				addLimited(relations, geo, meet.geth());
			} else if (parent instanceof AlgoIntersectLineConic
					|| parent instanceof AlgoIntersectSingle && ((AlgoIntersectSingle) parent)
							.getSourceIntersection() instanceof AlgoIntersectLineConic) {
				AlgoIntersectLineConic algorithm = parent instanceof AlgoIntersectLineConic
						? (AlgoIntersectLineConic) parent
						: (AlgoIntersectLineConic) ((AlgoIntersectSingle) parent)
								.getSourceIntersection();
				if (algorithm.getConic() instanceof GeoConic
						&& ((GeoConic) algorithm.getConic()).isCircle()) {
					relations.add(new Relation("on", geo, algorithm.getLine()));
					addLimited(relations, geo, algorithm.getLine());
					relations.add(new Relation("on", geo, (GeoConic) algorithm.getConic()));
				}
			} else if (parent instanceof AlgoJoinPoints || parent instanceof AlgoJoinPointsSegment
					|| parent instanceof AlgoJoinPointsRay) {
				relations.add(new Relation("on", parent.getInput(0).toGeoElement(), geo));
				relations.add(new Relation("on", parent.getInput(1).toGeoElement(), geo));
			} else if (parent instanceof AlgoLinePointLine) {
				AlgoLinePointLine parallel = (AlgoLinePointLine) parent;
				relations.add(new Relation("on", parallel.getP(), geo));
				relations.add(new Relation("parallel", geo, parallel.getInput(1).toGeoElement()));
			} else if (parent instanceof AlgoOrthoLinePointLine) {
				AlgoOrthoLinePointLine orthogonal = (AlgoOrthoLinePointLine) parent;
				relations.add(new Relation("on", orthogonal.getP(), geo));
				relations.add(new Relation("perpendicular", geo,
						orthogonal.getInput(1).toGeoElement()));
			} else if (parent instanceof AlgoCircleTwoPoints) {
				AlgoCircleTwoPoints circle = (AlgoCircleTwoPoints) parent;
				relations.add(new Relation("centre", geo, circle.getCenter().toGeoElement()));
				relations.add(new Relation("on", circle.getExternalPoint().toGeoElement(), geo));
			} else if (parent instanceof AlgoCirclePointRadius) {
				AlgoCirclePointRadius circle = (AlgoCirclePointRadius) parent;
				relations.add(new Relation("centre", geo, circle.getCenter().toGeoElement()));
				if (circle.getRadiusGeo() instanceof GeoNumeric) {
					relations.add(new Relation("radius", geo, circle.getRadiusGeo()));
				}
			}
		}
		return relations;
	}

	private static void addLimited(List<Relation> relations, GeoElement point, GeoElement line) {
		if (line instanceof GeoSegment || line instanceof GeoRay) {
			relations.add(new Relation("in_limited_part", point, line));
		}
	}

	/**
	 * @return {@code HOLDS} when the relation holds exactly in the constructive configuration,
	 *         otherwise the outcome or the failure
	 */
	@SuppressWarnings("unchecked")
	private static String decide(Construction construction, Relation relation) {
		ConstructiveCone.Builder builder = ConstructiveCone.builder(construction);
		int subject = builder.add(relation.subject);
		int object = builder.add(relation.object);
		ConstructiveConeEvaluator<Element> exact = ConstructiveConeEvaluator.exact(
				builder.build(), ExactEvaluationBudget.V1);
		if ("on".equals(relation.kind) || "in_limited_part".equals(relation.kind)) {
			IncidenceResult incidence = exact.incidence(subject, object);
			// "on" is the support only: the limited part is its own relation.
			boolean holds = incidence.getIncidence() == Incidence.HOLDS
					|| "on".equals(relation.kind)
							&& incidence.getIncidence() == Incidence.NOT_ON_LIMITED_TARGET;
			return holds ? "HOLDS" : String.valueOf(incidence);
		}
		Result first = exact.evaluate(subject);
		Result second = exact.evaluate(object);
		if (!first.hasValue() || !second.hasValue()) {
			return first + " / " + second;
		}
		Element residual;
		switch (relation.kind) {
		case "parallel": {
			Line<Element> a = (Line<Element>) first.getValue();
			Line<Element> b = (Line<Element>) second.getValue();
			residual = a.getA().multiply(b.getB()).subtract(a.getB().multiply(b.getA()));
			break;
		}
		case "perpendicular": {
			Line<Element> a = (Line<Element>) first.getValue();
			Line<Element> b = (Line<Element>) second.getValue();
			residual = a.getA().multiply(b.getA()).add(a.getB().multiply(b.getB()));
			break;
		}
		case "centre": {
			Circle<Element> circle = (Circle<Element>) first.getValue();
			Point<Element> centre = (Point<Element>) second.getValue();
			Element dx = circle.getCenterX().subtract(centre.getX());
			residual = dx.signum() != 0 ? dx : circle.getCenterY().subtract(centre.getY());
			break;
		}
		case "radius": {
			Circle<Element> circle = (Circle<Element>) first.getValue();
			Element radius = (Element) second.getValue();
			residual = circle.getRadiusSquared().subtract(radius.square());
			break;
		}
		default:
			throw new IllegalStateException(relation.kind);
		}
		return residual.signum() == 0 ? "HOLDS" : "REFUTED";
	}
}
