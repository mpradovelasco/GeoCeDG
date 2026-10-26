/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.geocedg.common.kernel.locus.constructive.ConstructiveCone;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Circle;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Incidence;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.IncidenceResult;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Line;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Point;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Result;
import org.geocedg.common.kernel.locus.constructive.ExactEvaluationBudget;
import org.geocedg.common.kernel.locus.constructive.ExactEvaluationCounters;
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
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU3 author disposition of 2026-10-26, item 3, run only through
 * {@code tools/benchmark/pre-g9b-cu/run-cu3-census-characterization.ps1}: the external
 * characterization of the provenance census of gate G-AUT-1 on the author model. Every relation
 * that the provenance of an object declares is decided with the constructive evaluator of the
 * product twice: under the approved {@code cu3-exact-evaluation-budget/v1}, as the census test
 * does, and under a characterization budget equal to the one of the constructive authority study
 * (200 000 000 operations, tower depth 48, 4 194 304 bits). The product budget API only admits
 * restriction, so this probe builds the characterization budget by reflection; nothing in the
 * product changes and the approved budget is untouched. The relations are those of
 * {@code PreG9BCu3ProvenanceCensusTest}, with the same semantics. It reads copies of the author
 * model only, assigns no verdict and is not part of any source set.
 */
@EnabledIfSystemProperty(named = "geocedg.cu0.harness", matches = "cu3-census-characterization")
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3CensusCharacterizationProbe {

	private static final long CHARACTERIZATION_OPERATIONS = 200_000_000L;
	private static final int CHARACTERIZATION_DEPTH = 48;
	private static final int CHARACTERIZATION_BITS = 1 << 22;

	@TempDir
	Path work;

	@Test
	void run() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.cu0.output"));
		Files.createDirectories(output);
		ExactEvaluationBudget characterization = characterizationBudget();
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("schema_version", 1);
		root.put("id", "pre-g9b-cu3-census-characterization");
		root.put("status", "CU3 EXTERNAL CHARACTERIZATION OF THE G-AUT-1 CENSUS - NO VERDICT OF ITS"
				+ " OWN; THE PRODUCT BUDGET IS UNCHANGED");
		Map<String, Object> generator = new LinkedHashMap<>();
		generator.put("runner", "tools/benchmark/pre-g9b-cu/run-cu3-census-characterization.ps1");
		generator.put("source_commit", System.getProperty("geocedg.cu0.commit"));
		generator.put("source_tree", System.getProperty("geocedg.cu0.tree"));
		generator.put("product_sources_modified_relative_to_commit",
				Boolean.getBoolean("geocedg.cu0.productDirty"));
		root.put("generator", generator);
		Map<String, Object> budgets = new LinkedHashMap<>();
		budgets.put("product", ExactEvaluationBudget.V1.toString());
		budgets.put("characterization", characterization.toString());
		budgets.put("characterization_construction", "reflection on the private constructor, in"
				+ " this probe only");
		root.put("budgets", budgets);
		root.put("census", PreG9BR6PlusF3Fixtures.onEdt(() -> census(characterization)));
		Files.writeString(output.resolve("cu3-census-characterization.json"),
				PreG9BCu0References.json(root), StandardCharsets.UTF_8);
	}

	private static ExactEvaluationBudget characterizationBudget() throws Exception {
		Constructor<ExactEvaluationBudget> constructor = ExactEvaluationBudget.class
				.getDeclaredConstructor(long.class, int.class, int.class);
		constructor.setAccessible(true);
		return constructor.newInstance(CHARACTERIZATION_OPERATIONS, CHARACTERIZATION_DEPTH,
				CHARACTERIZATION_BITS);
	}

	private Map<String, Object> census(ExactEvaluationBudget characterization) throws Exception {
		AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
		Construction construction = app.getKernel().getConstruction();
		List<Relation> relations = relations(construction);
		Map<String, Integer> productOutcomes = new TreeMap<>();
		Map<String, Integer> characterizationOutcomes = new TreeMap<>();
		Map<String, Object> exhausted = new LinkedHashMap<>();
		List<String> differing = new ArrayList<>();
		long largestOperations = 0;
		int largestDepth = 0;
		int largestBits = 0;
		for (Relation relation : relations) {
			Decision product = decide(construction, relation, ExactEvaluationBudget.V1);
			Decision wide = decide(construction, relation, characterization);
			productOutcomes.merge(product.key(), 1, Integer::sum);
			characterizationOutcomes.merge(wide.key(), 1, Integer::sum);
			if (wide.counters != null) {
				largestOperations = Math.max(largestOperations, wide.counters.getOperations());
				largestDepth = Math.max(largestDepth, wide.counters.getDepth());
				largestBits = Math.max(largestBits, wide.counters.getMaximumBits());
			}
			if (!"HOLDS".equals(product.outcome)) {
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("product", product.describe());
				row.put("characterization", wide.describe());
				exhausted.put(relation.toString(), row);
			} else if (!"HOLDS".equals(wide.outcome)) {
				differing.add(relation + ": " + wide.outcome);
			}
		}
		Map<String, Object> rows = new LinkedHashMap<>();
		rows.put("relations", relations.size());
		rows.put("outcomes_under_the_product_budget", productOutcomes);
		rows.put("outcomes_under_the_characterization_budget", characterizationOutcomes);
		rows.put("relations_not_holding_under_the_product_budget", exhausted);
		rows.put("holding_under_the_product_budget_but_not_under_characterization", differing);
		Map<String, Object> largest = new LinkedHashMap<>();
		largest.put("operations", largestOperations);
		largest.put("depth", largestDepth);
		largest.put("bits", largestBits);
		rows.put("largest_counters_under_characterization", largest);
		return rows;
	}

	/**
	 * The outcome of one relation with the counters of its exact evaluation and whether the
	 * evaluation reads a declared boundary substitute: an equality that depends on one is exact
	 * relative to the substitute, not a proof for the ideal object (specification section 7).
	 */
	private static final class Decision {
		private final String outcome;
		private final String detail;
		private final ExactEvaluationCounters counters;
		private final boolean substitutes;

		Decision(String outcome, String detail, ExactEvaluationCounters counters,
				boolean substitutes) {
			this.outcome = outcome;
			this.detail = detail;
			this.counters = counters;
			this.substitutes = substitutes;
		}

		/** @return the outcome, qualified by the dependence on boundary substitutes */
		String key() {
			return outcome + (substitutes ? " with boundary substitutes"
					: " without boundary substitutes");
		}

		Map<String, Object> describe() {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("outcome", outcome);
			row.put("reads_boundary_substitutes", substitutes);
			if (detail != null) {
				row.put("detail", detail);
			}
			if (counters != null) {
				row.put("operations", counters.getOperations());
				row.put("depth", counters.getDepth());
				row.put("maximum_bits", counters.getMaximumBits());
				row.put("square_roots", counters.getSquareRoots());
				row.put("sign_decisions", counters.getSignDecisions());
			}
			return row;
		}
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

	/** The relations of the study census, in construction order (as the census test). */
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

	/** Decides one relation as the census test does, under the given budget. */
	@SuppressWarnings("unchecked")
	private static Decision decide(Construction construction, Relation relation,
			ExactEvaluationBudget budget) {
		ConstructiveCone.Builder builder = ConstructiveCone.builder(construction);
		int subject = builder.add(relation.subject);
		int object = builder.add(relation.object);
		ConstructiveConeEvaluator<Element> exact = ConstructiveConeEvaluator.exact(
				builder.build(), budget);
		if ("on".equals(relation.kind) || "in_limited_part".equals(relation.kind)) {
			IncidenceResult incidence = exact.incidence(subject, object);
			boolean holds = incidence.getIncidence() == Incidence.HOLDS
					|| "on".equals(relation.kind)
							&& incidence.getIncidence() == Incidence.NOT_ON_LIMITED_TARGET;
			return new Decision(holds ? "HOLDS" : outcome(incidence.toString()),
					holds ? null : incidence.toString(), exact.getExactCounters(),
					!incidence.getSubstitutes().isEmpty());
		}
		Result first = exact.evaluate(subject);
		Result second = exact.evaluate(object);
		boolean substitutes = exact.dependsOnSubstitute(subject)
				|| exact.dependsOnSubstitute(object);
		if (!first.hasValue() || !second.hasValue()) {
			String detail = first + " / " + second;
			return new Decision(outcome(detail), detail, exact.getExactCounters(), substitutes);
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
		return new Decision(residual.signum() == 0 ? "HOLDS" : "REFUTED", null,
				exact.getExactCounters(), substitutes);
	}

	/** A short class of a non-holding outcome. */
	private static String outcome(String detail) {
		if (detail.contains("EXACT_EVALUATION_BUDGET_EXHAUSTED")
				|| detail.contains("BUDGET_EXHAUSTED")) {
			return "BUDGET_EXHAUSTED";
		}
		if (detail.contains("REFUTED")) {
			return "REFUTED";
		}
		return "NOT_DECIDED";
	}
}
