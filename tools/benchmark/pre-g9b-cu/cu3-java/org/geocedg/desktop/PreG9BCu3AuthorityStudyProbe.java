/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.LocusCertifiedFold2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D;
import org.geocedg.desktop.PreG9BCu3M3Evaluator.C;
import org.geocedg.desktop.PreG9BCu3M3Evaluator.J;
import org.geocedg.desktop.PreG9BCu3M3Evaluator.L;
import org.geocedg.desktop.PreG9BCu3M3Evaluator.P;
import org.geocedg.desktop.PreG9BCu3M3Tower.Num;
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
import org.geogebra.common.kernel.algos.AlgoMidpoint;
import org.geogebra.common.kernel.algos.AlgoOrthoLinePointLine;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoVec2D;
import org.geogebra.common.kernel.matrix.Coords;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU3 constructive-authority study probe, run only through
 * {@code tools/benchmark/pre-g9b-cu/run-cu3-authority-study.ps1}. It compares, on copies of the
 * author model, three exact readings of the same construction: the binary64 representations
 * (the current target authority), a constructive semantics that recomputes every supported
 * ruler-and-compass object exactly from its parents and takes every other object at a binary64
 * stand-in, and the same semantics with points on paths anchored exactly to their paths. It
 * records the boundary of the exact class, the construction relations that each reading makes
 * exactly true, the representations of circles and lines, the five mandatory contacts with exact
 * jets, their genericity in the non-constructible point H, the dependence of a reconstructed
 * target on its reconstruction boundary, the fold symmetry, the dynamic sweeps and the stability
 * of the CU2 certificates. It is evidence for a study only: it assigns no verdict and changes no
 * product behaviour. It is not part of any source set.
 */
@EnabledIfSystemProperty(named = "geocedg.cu0.harness", matches = "cu3-authority-study")
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3AuthorityStudyProbe {

	private static final MathContext REPORT = new MathContext(12);
	private static final String ALPHA = "\u03b1_{cone}";

	/** The five mandatory contacts: locus, traced point, exact driver point, target. */
	private static final String[][] CONTACTS = {
			{"e_1*n at K", "e_1", "J_1", "K", "n"},
			{"e_1*c_1 at K", "e_1", "J_1", "K", "c_1"},
			{"o_1*h_2 at K", "o_1", "K_1", "K", "h_2"},
			{"e_1*c_1 at J", "e_1", "J_1", "J", "c_1"},
			{"e_1*r at J", "e_1", "J_1", "J", "r"}};

	@TempDir
	Path work;

	@Test
	void run() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.cu0.output"));
		Files.createDirectories(output);
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("schema_version", 1);
		root.put("id", "pre-g9b-cu3-constructive-authority-study");
		root.put("status", "CU3 CONSTRUCTIVE AUTHORITY STUDY PROBE EVIDENCE - NO VERDICT OF ITS OWN");
		Map<String, Object> generator = new LinkedHashMap<>();
		generator.put("runner", "tools/benchmark/pre-g9b-cu/run-cu3-authority-study.ps1");
		generator.put("source_commit", System.getProperty("geocedg.cu0.commit"));
		generator.put("source_tree", System.getProperty("geocedg.cu0.tree"));
		generator.put("product_sources_modified_relative_to_commit",
				Boolean.getBoolean("geocedg.cu0.productDirty"));
		root.put("generator", generator);
		root.put("readings", readings());
		AppGeoCeDG app = onEdt(() -> PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work)));
		root.put("boundary", onEdt(() -> boundary(app)));
		root.put("provenance_census", onEdt(() -> provenance(app)));
		root.put("representations", onEdt(() -> representations(app)));
		root.put("mandatory_contacts", onEdt(() -> contacts(app)));
		root.put("genericity_in_H", onEdt(() -> genericity(app)));
		root.put("reconstruction_boundaries", onEdt(() -> reconstructionBoundaries(app)));
		root.put("fold_symmetry", onEdt(() -> foldSymmetry(app)));
		root.put("dynamics", onEdt(() -> dynamics(app)));
		root.put("cu2_certificate_stability", onEdt(() -> stability(app)));
		Files.writeString(output.resolve("cu3-authority-study.json"),
				PreG9BCu0References.json(root), StandardCharsets.UTF_8);
	}

	private static Map<String, Object> readings() {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("binary64", "the kernel state taken as exact rationals: point (x/z, y/z) from its "
				+ "homogeneous coordinates; line coefficients (x, y, z) with the start and end "
				+ "points of a segment or ray; circle by its adapter (translation vector and first "
				+ "half axis) or by its matrix (A0 x^2 + A1 y^2 + A2 + 2 A3 xy + 2 A4 x + 2 A5 y)");
		row.put("stand_in", "constructive semantics: every object of a supported ruler-and-compass "
				+ "algorithm (join, segment, ray, parallel, perpendicular, meet, midpoint, circle "
				+ "through a point, circle with a free radius, line-circle intersection with the "
				+ "inherited selection) is recomputed exactly from its parents; free objects, "
				+ "points on paths and outputs of every other algorithm are binary64 stand-ins");
		row.put("anchored", "as stand_in, but a point on a line or circle path is the exact "
				+ "projection of its binary64 position onto the exact path");
		return row;
	}

	// ---------------------------------------------------------------- semantics

	/** Exact meaning of every GeoElement under one reading, memoized per instance. */
	static final class Semantics {
		enum Rule {
			STAND_IN,
			ANCHORED
		}

		final Rule rule;
		final boolean fallbackOnFailure;
		final PreG9BCu3M3Tower tower;
		final PreG9BCu3M3Evaluator ev;
		final Map<GeoElement, String> how = new IdentityHashMap<>();
		private final Map<GeoElement, Object> values = new IdentityHashMap<>();

		Semantics(Rule rule, boolean fallbackOnFailure) {
			this(rule, fallbackOnFailure, newTower());
		}

		private Semantics(Rule rule, boolean fallbackOnFailure, PreG9BCu3M3Tower tower) {
			this(rule, fallbackOnFailure, tower, new PreG9BCu3M3Evaluator(tower));
		}

		/** A reading that shares the arithmetic of another, so that values compare exactly. */
		Semantics(Rule rule, boolean fallbackOnFailure, PreG9BCu3M3Tower tower,
				PreG9BCu3M3Evaluator ev) {
			this.rule = rule;
			this.fallbackOnFailure = fallbackOnFailure;
			this.tower = tower;
			this.ev = ev;
		}

		void substitute(GeoElement geo, Object value, String reason) {
			values.put(geo, value);
			how.put(geo, reason);
		}

		Object eval(GeoElement geo) {
			Object known = values.get(geo);
			if (known != null) {
				return known;
			}
			Object value = compute(geo);
			values.put(geo, value);
			return value;
		}

		P point(GeoElement geo) {
			return (P) eval(geo);
		}

		L line(GeoElement geo) {
			return (L) eval(geo);
		}

		C circle(GeoElement geo) {
			return (C) eval(geo);
		}

		private Object compute(GeoElement geo) {
			AlgoElement parent = geo.getParentAlgorithm();
			if (parent == null) {
				how.put(geo, "FREE");
				return standIn(geo);
			}
			if (geo instanceof GeoPoint && ((GeoPoint) geo).isPointOnPath()) {
				if (rule == Rule.ANCHORED) {
					Object anchored = anchored((GeoPoint) geo);
					if (anchored != null) {
						how.put(geo, "PATH_POINT_ANCHORED");
						return anchored;
					}
				}
				how.put(geo, "PATH_POINT_STAND_IN");
				return standIn(geo);
			}
			String algorithm = parent.getClass().getSimpleName();
			try {
				Object value = constructive(geo, parent);
				if (value != null) {
					how.put(geo, "CONSTRUCTIVE " + algorithm);
					return value;
				}
			} catch (PreG9BCu3M3Evaluator.Stop stop) {
				if (!fallbackOnFailure) {
					throw stop;
				}
				how.put(geo, "EXACT_FAILURE " + stop.failure + " " + algorithm);
				return standIn(geo);
			}
			how.put(geo, "STAND_IN " + algorithm);
			return standIn(geo);
		}

		private Object constructive(GeoElement geo, AlgoElement parent) {
			if (parent instanceof AlgoJoinPointsSegment) {
				AlgoJoinPointsSegment join = (AlgoJoinPointsSegment) parent;
				return ev.joinOf(point(join.getP()), point(join.getQ()), true, false);
			}
			if (parent instanceof AlgoJoinPointsRay) {
				AlgoJoinPointsRay join = (AlgoJoinPointsRay) parent;
				return ev.joinOf(point(join.getP()), point(join.getQ()), true, true);
			}
			if (parent instanceof AlgoJoinPoints) {
				AlgoJoinPoints join = (AlgoJoinPoints) parent;
				return ev.joinOf(point(join.getP()), point(join.getQ()), false, false);
			}
			if (parent instanceof AlgoLinePointLine) {
				AlgoLinePointLine parallel = (AlgoLinePointLine) parent;
				return ev.parallelOf(point(parallel.getP()),
						line(parallel.getInput(1).toGeoElement()));
			}
			if (parent instanceof AlgoOrthoLinePointLine) {
				AlgoOrthoLinePointLine orthogonal = (AlgoOrthoLinePointLine) parent;
				return ev.perpendicularOf(point(orthogonal.getP()),
						line(orthogonal.getInput(1).toGeoElement()));
			}
			if (parent instanceof AlgoIntersectLines) {
				AlgoIntersectLines meet = (AlgoIntersectLines) parent;
				return ev.meetOf(line(meet.getg()), line(meet.geth()));
			}
			if (parent instanceof AlgoMidpoint) {
				AlgoMidpoint midpoint = (AlgoMidpoint) parent;
				return ev.midpointOf(point(midpoint.getP()), point(midpoint.getQ()));
			}
			if (parent instanceof AlgoCircleTwoPoints) {
				AlgoCircleTwoPoints circle = (AlgoCircleTwoPoints) parent;
				return ev.circleThroughOf(point(circle.getCenter().toGeoElement()),
						point(circle.getExternalPoint().toGeoElement()));
			}
			if (parent instanceof AlgoCirclePointRadius) {
				AlgoCirclePointRadius circle = (AlgoCirclePointRadius) parent;
				GeoElement radius = circle.getRadiusGeo();
				if (!(radius instanceof GeoNumeric) || radius.getParentAlgorithm() != null) {
					return null;
				}
				P centre = point(circle.getCenter().toGeoElement());
				J r = (J) eval(radius);
				return ev.circleOf(centre.inhomX, centre.inhomY, r.square());
			}
			if (parent instanceof AlgoIntersectSingle
					&& ((AlgoIntersectSingle) parent).getSourceIntersection()
							instanceof AlgoIntersectLineConic) {
				AlgoIntersectSingle single = (AlgoIntersectSingle) parent;
				return lineConic((AlgoIntersectLineConic) single.getSourceIntersection(),
						single.getSelectedIndex());
			}
			if (parent instanceof AlgoIntersectLineConic) {
				AlgoIntersectLineConic algorithm = (AlgoIntersectLineConic) parent;
				for (int output = 0; output < algorithm.getOutputLength(); output++) {
					if (algorithm.getOutput(output) == geo) {
						return lineConic(algorithm, output);
					}
				}
			}
			return null;
		}

		private Object lineConic(AlgoIntersectLineConic algorithm, int index) {
			if (!(algorithm.getConic() instanceof GeoConic)
					|| !((GeoConic) algorithm.getConic()).isCircle()) {
				return null;
			}
			boolean compaction = algorithm.isSelectionFirstIntersectionPending();
			int[] permutation = compaction ? new int[] {0, 1}
					: algorithm.getSelectionPermutation();
			boolean[] labelled = {algorithm.getOutput(0).isLabelSet(),
					algorithm.getOutput(1).isLabelSet()};
			return ev.lineCircleOutput(line(algorithm.getLine()),
					circle((GeoConic) algorithm.getConic()), compaction, permutation, labelled,
					index);
		}

		/** The exact projection of the binary64 position of a path point onto its exact path. */
		private Object anchored(GeoPoint point) {
			org.geogebra.common.kernel.Path path = point.getPath();
			P raw = (P) standIn(point);
			if (path instanceof GeoLine) {
				L l = line((GeoLine) path);
				J t = l.a.multiply(raw.inhomX).add(l.b.multiply(raw.inhomY)).add(l.c)
						.divide(l.a.square().add(l.b.square()));
				return ev.unit(raw.inhomX.subtract(l.a.multiply(t)),
						raw.inhomY.subtract(l.b.multiply(t)));
			}
			if (path instanceof GeoConic && ((GeoConic) path).isCircle()) {
				C c = circle((GeoConic) path);
				J dx = raw.inhomX.subtract(c.mx);
				J dy = raw.inhomY.subtract(c.my);
				J scale = c.radiusSquared.divide(dx.square().add(dy.square())).sqrt();
				return ev.unit(c.mx.add(dx.multiply(scale)), c.my.add(dy.multiply(scale)));
			}
			return null;
		}

		/** Binary64 stand-in: one exact meaning per object (a point by x/z and y/z). */
		Object standIn(GeoElement geo) {
			if (geo instanceof GeoPoint) {
				GeoPoint point = (GeoPoint) geo;
				J z = ev.constant(point.getZ());
				return ev.unit(ev.constant(point.getX()).divide(z),
						ev.constant(point.getY()).divide(z));
			}
			if (geo instanceof GeoLine) {
				return lineRepresentation(ev, (GeoLine) geo);
			}
			if (geo instanceof GeoConic && ((GeoConic) geo).isCircle()) {
				return adapterCircle(ev, (GeoConic) geo);
			}
			if (geo instanceof GeoNumeric) {
				return ev.constant(((GeoNumeric) geo).getValue());
			}
			throw new IllegalStateException("no stand-in for " + geo.getClass().getSimpleName());
		}
	}

	private static PreG9BCu3M3Tower newTower() {
		return new PreG9BCu3M3Tower(PreG9BCu3M3GateProbe.OPERATION_BUDGET,
				PreG9BCu3M3GateProbe.BIT_BUDGET, PreG9BCu3M3GateProbe.DEPTH_BUDGET);
	}

	private static L lineRepresentation(PreG9BCu3M3Evaluator ev, GeoLine line) {
		boolean segment = line instanceof GeoSegment;
		boolean ray = line instanceof GeoRay;
		GeoPoint start = (segment || ray) ? line.getStartPoint() : null;
		return ev.lineOf(ev.constant(line.getX()), ev.constant(line.getY()),
				ev.constant(line.getZ()), segment || ray, ray,
				start == null ? null : ev.constant(start.getInhomX()),
				start == null ? null : ev.constant(start.getInhomY()));
	}

	private static C adapterCircle(PreG9BCu3M3Evaluator ev, GeoConic circle) {
		GeoVec2D centre = circle.getTranslationVector();
		return ev.circleOf(ev.constant(centre.getX()), ev.constant(centre.getY()),
				ev.constant(circle.getHalfAxes()[0]).square());
	}

	private static C matrixCircle(PreG9BCu3M3Evaluator ev, GeoConic circle) {
		double[] m = circle.getFlatMatrix();
		J a0 = ev.constant(m[0]);
		J mx = ev.constant(m[4]).divide(a0).negate();
		J my = ev.constant(m[5]).divide(a0).negate();
		J radiusSquared = mx.square().add(my.square()).subtract(ev.constant(m[2]).divide(a0));
		return ev.circleOf(mx, my, radiusSquared);
	}

	// ---------------------------------------------------------------- boundary

	/**
	 * Every object of the model with its reading in the constructive semantics and its ideal
	 * class: constructible when its exact value follows from the free inputs by the supported
	 * ruler-and-compass operations; non-constructible otherwise, with the reason.
	 */
	private static Map<String, Object> boundary(AppGeoCeDG app) {
		Semantics s = new Semantics(Semantics.Rule.STAND_IN, true);
		Map<GeoElement, String> ideal = new IdentityHashMap<>();
		Map<String, Object> rows = new LinkedHashMap<>();
		int constructible = 0;
		int nonConstructible = 0;
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (!geo.isLabelSet() || !(geo instanceof GeoPoint || geo instanceof GeoLine
					|| geo instanceof GeoConic || geo instanceof GeoNumeric)) {
				continue;
			}
			String reading;
			try {
				s.eval(geo);
				reading = s.how.get(geo);
			} catch (RuntimeException failure) {
				reading = "NOT_EVALUATED " + failure.getClass().getSimpleName();
			}
			String idealClass = idealClass(geo, ideal);
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("reading", reading);
			row.put("ideal_class", idealClass);
			rows.put(geo.getLabelSimple(), row);
			if (idealClass.startsWith("CONSTRUCTIBLE")) {
				constructible++;
			} else {
				nonConstructible++;
			}
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("constructible_objects", constructible);
		result.put("non_constructible_objects", nonConstructible);
		result.put("objects", rows);
		return result;
	}

	/** Ideal class of an object, memoized: constructible or the first non-constructible cause. */
	private static String idealClass(GeoElement geo, Map<GeoElement, String> memo) {
		String known = memo.get(geo);
		if (known != null) {
			return known;
		}
		String result;
		AlgoElement parent = geo.getParentAlgorithm();
		if (parent == null) {
			result = "CONSTRUCTIBLE (free input)";
		} else if (geo instanceof GeoPoint && ((GeoPoint) geo).isPointOnPath()) {
			org.geogebra.common.kernel.Path path = ((GeoPoint) geo).getPath();
			if (path instanceof GeoLine) {
				String pathClass = idealClass((GeoLine) path, memo);
				result = pathClass.startsWith("CONSTRUCTIBLE")
						? "CONSTRUCTIBLE (point on a constructible line at a rational parameter)"
						: "NON_CONSTRUCTIBLE (point on a non-constructible line "
								+ ((GeoLine) path).getLabelSimple() + ")";
			} else if (path instanceof GeoConic) {
				result = "NON_CONSTRUCTIBLE (point on a conic at a binary64 angle parameter: "
						+ "trigonometric coordinates)";
			} else {
				result = "NON_CONSTRUCTIBLE (point on a path of class "
						+ path.getClass().getSimpleName() + ")";
			}
		} else if (isSupported(parent)) {
			result = "CONSTRUCTIBLE";
			for (int index = 0; index < parent.getInputLength(); index++) {
				GeoElement input = parent.getInput(index).toGeoElement();
				String inputClass = idealClass(input, memo);
				if (!inputClass.startsWith("CONSTRUCTIBLE")) {
					result = "NON_CONSTRUCTIBLE (depends on " + input.getLabelSimple() + ")";
					break;
				}
			}
		} else {
			result = "NON_CONSTRUCTIBLE (" + parent.getClass().getSimpleName() + ")";
		}
		memo.put(geo, result);
		return result;
	}

	private static boolean isSupported(AlgoElement parent) {
		return parent instanceof AlgoJoinPoints || parent instanceof AlgoJoinPointsSegment
				|| parent instanceof AlgoJoinPointsRay || parent instanceof AlgoLinePointLine
				|| parent instanceof AlgoOrthoLinePointLine || parent instanceof AlgoIntersectLines
				|| parent instanceof AlgoMidpoint || parent instanceof AlgoCircleTwoPoints
				|| parent instanceof AlgoCirclePointRadius
				|| parent instanceof AlgoIntersectLineConic
				|| parent instanceof AlgoIntersectSingle;
	}

	// ---------------------------------------------------------------- provenance census

	/** One construction relation implied by the provenance of an object. */
	private static final class Relation {
		final String kind;
		final GeoElement subject;
		final GeoElement object;

		Relation(String kind, GeoElement subject, GeoElement object) {
			this.kind = kind;
			this.subject = subject;
			this.object = object;
		}
	}

	private static List<Relation> relations(AppGeoCeDG app) {
		List<Relation> relations = new ArrayList<>();
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
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
				relations.add(new Relation("parallel", geo,
						parallel.getInput(1).toGeoElement()));
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

	private static Map<String, Object> provenance(AppGeoCeDG app) {
		List<Relation> relations = relations(app);
		PreG9BCu3M3Tower tower = new PreG9BCu3M3Tower(PreG9BCu3M3GateProbe.OPERATION_BUDGET,
				PreG9BCu3M3GateProbe.BIT_BUDGET, PreG9BCu3M3GateProbe.DEPTH_BUDGET);
		PreG9BCu3M3Evaluator binary = new PreG9BCu3M3Evaluator(tower);
		Semantics standIn = new Semantics(Semantics.Rule.STAND_IN, true);
		Semantics anchored = new Semantics(Semantics.Rule.ANCHORED, true);
		List<Object> rows = new ArrayList<>();
		Map<String, int[]> totals = new LinkedHashMap<>();
		for (Relation relation : relations) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("relation", relation.subject.getLabelSimple() + " " + relation.kind + " "
					+ relation.object.getLabelSimple());
			boolean circleRelation = relation.object instanceof GeoConic;
			String adapter = decideBinary64(binary, relation, false);
			row.put("binary64", adapter);
			count(totals, circleRelation ? "binary64 (adapter)" : "binary64", adapter);
			if (circleRelation) {
				String matrix = decideBinary64(binary, relation, true);
				row.put("binary64_matrix", matrix);
				count(totals, "binary64 (matrix)", matrix);
			}
			String a = decide(standIn, relation);
			row.put("stand_in", a);
			count(totals, "stand_in", a);
			String b = decide(anchored, relation);
			row.put("anchored", b);
			count(totals, "anchored", b);
			rows.add(row);
		}
		Map<String, Object> summary = new LinkedHashMap<>();
		for (Map.Entry<String, int[]> entry : totals.entrySet()) {
			Map<String, Object> counts = new LinkedHashMap<>();
			counts.put("exactly_true", entry.getValue()[0]);
			counts.put("exactly_false", entry.getValue()[1]);
			counts.put("not_decided", entry.getValue()[2]);
			summary.put(entry.getKey(), counts);
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("relations", relations.size());
		result.put("summary", summary);
		result.put("rows", rows);
		return result;
	}

	private static void count(Map<String, int[]> totals, String key, String outcome) {
		int[] counts = totals.computeIfAbsent(key, ignored -> new int[3]);
		counts[outcome.equals("true") ? 0 : outcome.startsWith("false") ? 1 : 2]++;
	}

	/** A relation decided exactly on the binary64 representations. */
	private static String decideBinary64(PreG9BCu3M3Evaluator ev, Relation relation,
			boolean matrix) {
		try {
			Object subject = binaryValue(ev, relation.subject, matrix);
			Object object = binaryValue(ev, relation.object, matrix);
			if ("in_limited_part".equals(relation.kind)) {
				return limitedBinary64(ev, (P) subject, (GeoLine) relation.object);
			}
			return decideValues(ev, relation.kind, subject, object);
		} catch (RuntimeException failure) {
			return "not decided: " + failure.getClass().getSimpleName();
		}
	}

	private static Object binaryValue(PreG9BCu3M3Evaluator ev, GeoElement geo, boolean matrix) {
		if (geo instanceof GeoPoint) {
			GeoPoint point = (GeoPoint) geo;
			J z = ev.constant(point.getZ());
			return ev.unit(ev.constant(point.getX()).divide(z), ev.constant(point.getY()).divide(z));
		}
		if (geo instanceof GeoLine) {
			return lineRepresentation(ev, (GeoLine) geo);
		}
		if (geo instanceof GeoConic && ((GeoConic) geo).isCircle()) {
			return matrix ? matrixCircle(ev, (GeoConic) geo) : adapterCircle(ev, (GeoConic) geo);
		}
		if (geo instanceof GeoNumeric) {
			return ev.constant(((GeoNumeric) geo).getValue());
		}
		throw new IllegalStateException(geo.getClass().getSimpleName());
	}

	/** Membership in a binary64 segment or ray by its own start and end points. */
	private static String limitedBinary64(PreG9BCu3M3Evaluator ev, P point, GeoLine line) {
		GeoPoint start = line.getStartPoint();
		J sx = ev.constant(start.getInhomX());
		J sy = ev.constant(start.getInhomY());
		J dx;
		J dy;
		GeoPoint end = line.getEndPoint();
		if (line instanceof GeoSegment && end != null) {
			dx = ev.constant(end.getInhomX()).subtract(sx);
			dy = ev.constant(end.getInhomY()).subtract(sy);
		} else {
			dx = ev.constant(line.getY());
			dy = ev.constant(line.getX()).negate();
		}
		J parameter = point.inhomX.subtract(sx).multiply(dx)
				.add(point.inhomY.subtract(sy).multiply(dy));
		if (parameter.sign() < 0) {
			return "false (before the start)";
		}
		if (line instanceof GeoSegment && end != null
				&& dx.square().add(dy.square()).subtract(parameter).sign() < 0) {
			return "false (after the end)";
		}
		return "true";
	}

	private static String decide(Semantics s, Relation relation) {
		try {
			Object subject = s.eval(relation.subject);
			Object object = s.eval(relation.object);
			if ("in_limited_part".equals(relation.kind)) {
				L line = (L) object;
				if (!line.bounded) {
					return "not decided: unbounded";
				}
				return limited(line, (P) subject) ? "true" : "false (outside the limited part)";
			}
			return decideValues(s.ev, relation.kind, subject, object);
		} catch (RuntimeException failure) {
			return "not decided: " + failure.getClass().getSimpleName();
		}
	}

	private static boolean limited(L line, P point) {
		J parameter = point.inhomX.subtract(line.startX).multiply(line.b)
				.subtract(point.inhomY.subtract(line.startY).multiply(line.a));
		if (parameter.sign() < 0) {
			return false;
		}
		return line.ray || line.a.square().add(line.b.square()).subtract(parameter).sign() >= 0;
	}

	private static String decideValues(PreG9BCu3M3Evaluator ev, String kind, Object subject,
			Object object) {
		J residual;
		switch (kind) {
		case "on":
			residual = residual(ev, object, (P) subject);
			break;
		case "parallel": {
			L first = (L) subject;
			L second = (L) object;
			residual = first.a.multiply(second.b).subtract(first.b.multiply(second.a));
			break;
		}
		case "perpendicular": {
			L first = (L) subject;
			L second = (L) object;
			residual = first.a.multiply(second.a).add(first.b.multiply(second.b));
			break;
		}
		case "centre": {
			C circle = (C) subject;
			P centre = (P) object;
			J dx = circle.mx.subtract(centre.inhomX);
			J dy = circle.my.subtract(centre.inhomY);
			residual = dx.sign() != 0 ? dx : dy;
			break;
		}
		case "radius": {
			C circle = (C) subject;
			J r = (J) object;
			residual = circle.radiusSquared.subtract(r.square());
			break;
		}
		default:
			throw new IllegalStateException(kind);
		}
		return residual.sign() == 0 ? "true" : "false (" + approx(residual.v) + ")";
	}

	/** Residual of a point with a line ({@code a x + b y + c}) or a circle (its power). */
	private static J residual(PreG9BCu3M3Evaluator ev, Object target, P point) {
		if (target instanceof L) {
			L line = (L) target;
			return line.a.multiply(point.inhomX).add(line.b.multiply(point.inhomY)).add(line.c);
		}
		C circle = (C) target;
		J dx = point.inhomX.subtract(circle.mx);
		J dy = point.inhomY.subtract(circle.my);
		return dx.square().add(dy.square()).subtract(circle.radiusSquared);
	}

	// ---------------------------------------------------------------- representations

	private static Map<String, Object> representations(AppGeoCeDG app) {
		Semantics s = new Semantics(Semantics.Rule.STAND_IN, true);
		Semantics anchoredSemantics = new Semantics(Semantics.Rule.ANCHORED, true);
		PreG9BCu3M3Evaluator ev = s.ev;
		Map<String, Object> circles = new LinkedHashMap<>();
		Map<String, Object> lines = new LinkedHashMap<>();
		Map<String, Object> points = new LinkedHashMap<>();
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (!geo.isLabelSet()) {
				continue;
			}
			try {
				if (geo instanceof GeoConic && ((GeoConic) geo).isCircle()) {
					circles.put(geo.getLabelSimple(), circleRow(s, anchoredSemantics,
							(GeoConic) geo));
				} else if (geo instanceof GeoLine && geo.getParentAlgorithm() != null) {
					Object value = s.eval(geo);
					if (s.how.get(geo).startsWith("CONSTRUCTIVE")) {
						lines.put(geo.getLabelSimple(), lineRow(ev, (GeoLine) geo, (L) value));
					}
				} else if (geo instanceof GeoPoint && geo.getParentAlgorithm() != null) {
					Object value = s.eval(geo);
					if (s.how.get(geo).startsWith("CONSTRUCTIVE")) {
						points.put(geo.getLabelSimple(), pointRow(ev, (GeoPoint) geo, (P) value));
					}
				}
			} catch (RuntimeException failure) {
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("outcome", "NOT_EVALUATED " + failure);
				(geo instanceof GeoConic ? circles : geo instanceof GeoLine ? lines : points)
						.put(geo.getLabelSimple(), row);
			}
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("circles", circles);
		result.put("lines", lines);
		result.put("points", points);
		return result;
	}

	private static Map<String, Object> circleRow(Semantics s, Semantics anchored, GeoConic conic) {
		PreG9BCu3M3Evaluator ev = s.ev;
		Map<String, Object> row = new LinkedHashMap<>();
		double[] m = conic.getFlatMatrix();
		row.put("matrix_A0_A1_A3", List.of(m[0], m[1], m[3]));
		C matrix = matrixCircle(ev, conic);
		C adapter = adapterCircle(ev, conic);
		Object value = s.eval(conic);
		row.put("reading", s.how.get(conic));
		C constructive = (C) value;
		row.put("radius_squared_matrix", approx(matrix.radiusSquared.v));
		row.put("matrix_minus_half_axis_squared",
				approx(matrix.radiusSquared.v.subtract(adapter.radiusSquared.v)));
		row.put("matrix_minus_constructive",
				approx(matrix.radiusSquared.v.subtract(constructive.radiusSquared.v)));
		row.put("half_axis_squared_minus_constructive",
				approx(adapter.radiusSquared.v.subtract(constructive.radiusSquared.v)));
		row.put("half_axes_equal", conic.getHalfAxis(0) == conic.getHalfAxis(1));
		row.put("matrix_centre_equals_translation_vector",
				matrix.mx.v.subtract(adapter.mx.v).signum() == 0
						&& matrix.my.v.subtract(adapter.my.v).signum() == 0);
		row.put("constructive_centre_minus_translation_vector",
				List.of(approx(constructive.mx.v.subtract(adapter.mx.v)),
						approx(constructive.my.v.subtract(adapter.my.v))));
		try {
			C anchoredValue = (C) anchored.eval(conic);
			row.put("anchored_minus_constructive_radius_squared",
					approx(towerless(anchoredValue.radiusSquared.v)
							.subtract(towerless(constructive.radiusSquared.v))));
		} catch (RuntimeException failure) {
			row.put("anchored", "NOT_EVALUATED " + failure.getClass().getSimpleName());
		}
		return row;
	}

	private static Map<String, Object> lineRow(PreG9BCu3M3Evaluator ev, GeoLine line, L exact) {
		Map<String, Object> row = new LinkedHashMap<>();
		L binary = lineRepresentation(ev, line);
		boolean proportional = exact.a.v.multiply(binary.b.v).subtract(exact.b.v
				.multiply(binary.a.v)).signum() == 0
				&& exact.a.v.multiply(binary.c.v).subtract(exact.c.v.multiply(binary.a.v))
						.signum() == 0
				&& exact.b.v.multiply(binary.c.v).subtract(exact.c.v.multiply(binary.b.v))
						.signum() == 0;
		row.put("proportional_to_binary64", proportional);
		Num scale = binary.a.v.signum() != 0 ? exact.a.v.divide(binary.a.v)
				: exact.b.v.divide(binary.b.v);
		row.put("direction_proportional", exact.a.v.multiply(binary.b.v)
				.subtract(exact.b.v.multiply(binary.a.v)).signum() == 0);
		row.put("offset_difference_after_scaling",
				approx(exact.c.v.subtract(scale.multiply(binary.c.v))));
		return row;
	}

	private static Map<String, Object> pointRow(PreG9BCu3M3Evaluator ev, GeoPoint point, P exact) {
		Map<String, Object> row = new LinkedHashMap<>();
		J z = ev.constant(point.getZ());
		Num dx = exact.inhomX.v.subtract(ev.constant(point.getX()).divide(z).v);
		Num dy = exact.inhomY.v.subtract(ev.constant(point.getY()).divide(z).v);
		row.put("equal_to_binary64", dx.signum() == 0 && dy.signum() == 0);
		row.put("difference", List.of(approx(dx), approx(dy)));
		return row;
	}

	/** Values of two towers are compared through their decimal approximations. */
	private static BigDecimal towerless(Num value) {
		return value.approximate();
	}

	// ---------------------------------------------------------------- mandatory contacts

	private static Map<String, Object> contacts(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		for (String[] contact : CONTACTS) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("stand_in", constructiveContact(app, Semantics.Rule.STAND_IN, contact, null));
			row.put("anchored", constructiveContact(app, Semantics.Rule.ANCHORED, contact, null));
			PreG9BCu3M3GateProbe.Contact captured = PreG9BCu3M3GateProbe.contact(app, contact[1],
					"E_1", contact[3], contact[4], false);
			if (captured.traced != null) {
				row.put("P_L_captured_configuration", List.of(approx(captured.traced.inhomX.v),
						approx(captured.traced.inhomY.v)));
				Object decision = ((Map<?, ?>) captured.report.get("authority")).get("decision");
				row.put("P_L_against_the_binary64_authority", decision);
				Map<?, ?> standIn = (Map<?, ?>) row.get("stand_in");
				Object exactP = standIn.get("P_exact_decimal");
				if (exactP instanceof List) {
					List<?> pc = (List<?>) exactP;
					BigDecimal dx = ((BigDecimal) pc.get(0)).subtract(towerless(
							captured.traced.inhomX.v));
					BigDecimal dy = ((BigDecimal) pc.get(1)).subtract(towerless(
							captured.traced.inhomY.v));
					row.put("P_stand_in_minus_P_L", List.of(approx(dx), approx(dy)));
				}
			}
			((Map<?, ?>) row.get("stand_in")).remove("P_exact_decimal");
			((Map<?, ?>) row.get("anchored")).remove("P_exact_decimal");
			rows.put(contact[0], row);
		}
		return rows;
	}

	/**
	 * One mandatory contact in a constructive reading: the exact driver point and its exact
	 * correspondence on the driver circle p, the exact traced point with its jets, and the
	 * incidence with the constructive target and with the binary64 target.
	 *
	 * @param hOverride exact replacement of the stand-in of H, or null
	 */
	static Map<String, Object> constructiveContact(AppGeoCeDG app, Semantics.Rule rule,
			String[] contact, P hOverride) {
		Map<String, Object> row = new LinkedHashMap<>();
		Semantics s = new Semantics(rule, false);
		try {
			if (hOverride != null) {
				s.substitute(lookup(app, "H"), copy(s.ev, hOverride), "H_OVERRIDE");
			}
			GeoConic p = (GeoConic) lookup(app, "p");
			Coords e0 = p.getEigenvec(0);
			Coords e1 = p.getEigenvec(1);
			if (!(e0.getX() == 1 && e0.getY() == 0 && e1.getX() == 0 && e1.getY() == 1)) {
				row.put("outcome", "OUT_OF_CLASS: the axes of p are not (1,0),(0,1)");
				return row;
			}
			C support = s.circle(p);
			P d = s.point(lookup(app, contact[3]));
			J radius = support.radiusSquared.sqrt();
			Num cosine = d.inhomX.v.subtract(support.mx.v).divide(radius.v);
			Num sine = d.inhomY.v.subtract(support.my.v).divide(radius.v);
			boolean zero = cosine.subtract(s.tower.one()).signum() == 0 && sine.signum() == 0;
			boolean pi = cosine.add(s.tower.one()).signum() == 0 && sine.signum() == 0;
			row.put("D_exact", xy(d));
			row.put("t_star", zero ? "0 (exact)" : pi ? "pi (exact)" : "other");
			P driverJet = s.ev.unit(
					s.ev.jet(d.inhomX.v, radius.v.multiply(sine).negate(),
							radius.v.multiply(cosine).negate()),
					s.ev.jet(d.inhomY.v, radius.v.multiply(cosine),
							radius.v.multiply(sine).negate()));
			s.substitute(lookup(app, "E_1"), driverJet, "DRIVER_JET");
			P traced = s.point(lookup(app, contact[2]));
			row.put("P", xy(traced));
			// Removed by the caller after use: decimal values to compare across towers.
			row.put("P_exact_decimal", List.of(towerless(traced.inhomX.v),
					towerless(traced.inhomY.v)));
			row.put("P_first_derivative_is_exact_zero (fold)", traced.inhomX.d1.signum() == 0
					&& traced.inhomY.d1.signum() == 0);
			GeoElement targetGeo = lookup(app, contact[4]);
			Object target = s.eval(targetGeo);
			J h = residual(s.ev, target, traced);
			row.put("incidence_with_the_constructive_target", h.sign() == 0 ? "EQUAL"
					: "DIFFERENT (" + approx(h.v) + ")");
			row.put("residual_first_derivative_is_exact_zero", h.d1 != null && h.d1.signum() == 0);
			row.put("residual_second_derivative", h.d2 == null ? null : approx(h.d2));
			if (target instanceof L && ((L) target).bounded) {
				row.put("in_the_limited_part_of_the_constructive_target",
						limited((L) target, traced));
			}
			Object binary = binaryValue(s.ev, targetGeo, false);
			J binaryResidual = residual(s.ev, binary, traced);
			row.put("incidence_with_the_binary64_target", binaryResidual.sign() == 0 ? "EQUAL"
					: "DIFFERENT (" + approx(binaryResidual.v) + ")");
			row.put("reading_of_H", s.how.get(lookup(app, "H")));
			row.put("boundary_support", support(s));
		} catch (PreG9BCu3M3Evaluator.Stop stop) {
			row.put("outcome", stop.failure.name() + ": " + stop.getMessage());
		} catch (PreG9BCu3M3Tower.BudgetExhausted exhausted) {
			row.put("outcome", "EXACT_EVALUATION_BUDGET_EXHAUSTED: " + exhausted.getMessage());
		} catch (RuntimeException failure) {
			StackTraceElement[] trace = failure.getStackTrace();
			row.put("outcome", "PROBE_FAILURE: " + failure
					+ (trace.length > 0 ? " at " + trace[0] : ""));
		}
		row.put("counters", counters(s.tower));
		return row;
	}

	/**
	 * The objects that a decision used without recomputing them: free inputs, points on paths
	 * and stand-ins of other algorithms, sorted by label. They are the premises whose integrity
	 * a decision inherits.
	 */
	private static Map<String, Object> support(Semantics s) {
		List<String> free = new ArrayList<>();
		List<String> boundary = new ArrayList<>();
		for (Map.Entry<GeoElement, String> entry : s.how.entrySet()) {
			String label = entry.getKey().getLabelSimple();
			String reading = entry.getValue();
			if ("FREE".equals(reading)) {
				free.add(label);
			} else if (reading.startsWith("PATH_POINT") || reading.startsWith("STAND_IN")
					|| reading.startsWith("H_OVERRIDE")) {
				boundary.add(label + " (" + reading + ")");
			}
		}
		free.sort(null);
		boundary.sort(null);
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("free_inputs", free);
		row.put("boundary_objects", boundary);
		return row;
	}

	private static P copy(PreG9BCu3M3Evaluator ev, P source) {
		return ev.unit(ev.constant(source.inhomX.v.approximate().doubleValue()),
				ev.constant(source.inhomY.v.approximate().doubleValue()));
	}

	// ---------------------------------------------------------------- genericity in H

	/**
	 * H is the only non-constructible input of the five contacts (a point on the line through
	 * the rotated point C'). The contacts are decided exactly with H moved to other rational
	 * positions: exact equality at every position is evidence (not a proof) that the incidence
	 * is an identity in the coordinates of H.
	 */
	private static Map<String, Object> genericity(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		GeoPoint h = (GeoPoint) lookup(app, "H");
		int equal = 0;
		int total = 0;
		for (int k = 1; k <= 12; k++) {
			double dx = ((k * 37) % 23 - 11) * 0.0371;
			double dy = ((k * 53) % 19 - 9) * 0.0293;
			PreG9BCu3M3Tower tower = new PreG9BCu3M3Tower(PreG9BCu3M3GateProbe.OPERATION_BUDGET,
					PreG9BCu3M3GateProbe.BIT_BUDGET, PreG9BCu3M3GateProbe.DEPTH_BUDGET);
			PreG9BCu3M3Evaluator ev = new PreG9BCu3M3Evaluator(tower);
			P moved = ev.unit(ev.constant(h.getInhomX() + dx), ev.constant(h.getInhomY() + dy));
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("H", List.of(h.getInhomX() + dx, h.getInhomY() + dy));
			for (String[] contact : CONTACTS) {
				Map<String, Object> decision = constructiveContact(app, Semantics.Rule.STAND_IN,
						contact, moved);
				Object incidence = decision.get("incidence_with_the_constructive_target");
				String outcome = incidence == null ? String.valueOf(decision.get("outcome"))
						: incidence + ("true".equals(String.valueOf(decision.get(
								"residual_first_derivative_is_exact_zero"))) ? ", h' = 0" : "");
				row.put(contact[0], outcome);
				total++;
				equal += incidence != null && "EQUAL".equals(incidence) ? 1 : 0;
			}
			rows.put("position_" + k, row);
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("decisions", total);
		result.put("exactly_equal", equal);
		result.put("positions", rows);
		return result;
	}

	// ---------------------------------------------------------------- reconstruction boundaries

	/**
	 * The same target reconstructed from different boundaries: the Sigma_L+ reconstruction of
	 * gate G-M3 (descendants of the driver point K), a reconstruction from D_1, the deep
	 * recomputation and the binary64 state. Different exact meanings for one object show that a
	 * reconstruction boundary is not an intrinsic authority.
	 */
	private static Map<String, Object> reconstructionBoundaries(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		PreG9BCu3M3Tower tower = new PreG9BCu3M3Tower(PreG9BCu3M3GateProbe.OPERATION_BUDGET,
				PreG9BCu3M3GateProbe.BIT_BUDGET, PreG9BCu3M3GateProbe.DEPTH_BUDGET);
		PreG9BCu3M3Evaluator ev = new PreG9BCu3M3Evaluator(tower);
		try {
			GeoPoint k = (GeoPoint) lookup(app, "K");
			AlgoIntersectLineConic algorithm = (AlgoIntersectLineConic) k.getParentAlgorithm();
			GeoConic p = (GeoConic) algorithm.getConic();
			L support = lineRepresentation(ev, algorithm.getLine());
			C circle = ev.circleOf(ev.constant(p.getMidpoint2D().getX()),
					ev.constant(p.getMidpoint2D().getY()), ev.constant(p.getHalfAxis(0)).square());
			boolean compaction = algorithm.isSelectionFirstIntersectionPending();
			int[] permutation = compaction ? new int[] {0, 1}
					: algorithm.getSelectionPermutation();
			boolean[] labelled = {algorithm.getOutput(0).isLabelSet(),
					algorithm.getOutput(1).isLabelSet()};
			PreG9BCu3M3GateProbe.Reconstruction fromK = new PreG9BCu3M3GateProbe.Reconstruction(
					ev, false);
			for (int output = 0; output < 2; output++) {
				GeoElement geo = algorithm.getOutput(output);
				fromK.substitute(geo, ev.lineCircleOutput(support, circle, compaction,
						permutation, labelled, output));
				fromK.roots.add(geo);
			}
			PreG9BCu3M3GateProbe.Reconstruction fromD1 = new PreG9BCu3M3GateProbe.Reconstruction(
					ev, false);
			fromD1.roots.add(lookup(app, "D_1"));
			PreG9BCu3M3GateProbe.Reconstruction deep = new PreG9BCu3M3GateProbe.Reconstruction(ev,
					true);
			for (String label : List.of("n", "h_2", "c_1")) {
				GeoElement target = lookup(app, label);
				Map<String, Object> meanings = new LinkedHashMap<>();
				List<Object> values = new ArrayList<>();
				List<String> names = List.of("Sigma_L+ from K (gate G-M3)", "from D_1",
						"deep recomputation", "binary64");
				values.add(fromK.eval(target));
				values.add(fromD1.eval(target));
				values.add(deep.eval(target));
				values.add(target instanceof GeoLine ? lineRepresentation(ev, (GeoLine) target)
						: matrixCircle(ev, (GeoConic) target));
				for (int first = 0; first < values.size(); first++) {
					for (int second = first + 1; second < values.size(); second++) {
						meanings.put(names.get(first) + " vs " + names.get(second),
								sameObject(values.get(first), values.get(second)));
					}
				}
				if (target instanceof GeoConic) {
					C adapter = adapterCircle(ev, (GeoConic) target);
					for (int index = 0; index < values.size(); index++) {
						meanings.put(names.get(index) + " vs adapter (half axis)",
								sameObject(values.get(index), adapter));
					}
				}
				rows.put(label, meanings);
			}
		} catch (RuntimeException failure) {
			rows.put("outcome", "PROBE_FAILURE: " + failure);
		}
		return rows;
	}

	private static String sameObject(Object first, Object second) {
		if (first instanceof L && second instanceof L) {
			L a = (L) first;
			L b = (L) second;
			boolean proportional = a.a.v.multiply(b.b.v).subtract(a.b.v.multiply(b.a.v))
					.signum() == 0
					&& a.a.v.multiply(b.c.v).subtract(a.c.v.multiply(b.a.v)).signum() == 0
					&& a.b.v.multiply(b.c.v).subtract(a.c.v.multiply(b.b.v)).signum() == 0;
			return proportional ? "same line" : "different lines";
		}
		if (first instanceof C && second instanceof C) {
			C a = (C) first;
			C b = (C) second;
			boolean centre = a.mx.v.subtract(b.mx.v).signum() == 0
					&& a.my.v.subtract(b.my.v).signum() == 0;
			Num radius = a.radiusSquared.v.subtract(b.radiusSquared.v);
			return centre && radius.signum() == 0 ? "same circle"
					: "different circles (centre equal " + centre + ", r^2 difference "
							+ approx(radius) + ")";
		}
		return "not comparable";
	}

	// ---------------------------------------------------------------- fold symmetry

	/**
	 * The traced points of e_1 and o_1 at pairs of rational driver points symmetric about the
	 * line i (t and -t near 0, pi - t and pi + t near pi), in the captured program of CU2 and in
	 * the constructive reading; and the folds that CU2 certifies.
	 */
	private static Map<String, Object> foldSymmetry(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		for (String[] locus : new String[][] {{"e_1", "J_1"}, {"o_1", "K_1"}}) {
			Map<String, Object> row = new LinkedHashMap<>();
			LocusDefinition2D definition = PreG9BCu0Corpus.locus(app, locus[0])
					.getSemanticDefinition();
			LocusExistenceStructure2D structure = definition.getBranch(PreG9BCu0Corpus.MAIN)
					.getExistenceStructure();
			List<Object> folds = new ArrayList<>();
			if (structure != null) {
				for (LocusCertifiedFold2D fold : structure.getCertifiedFolds()) {
					Map<String, Object> entry = new LinkedHashMap<>();
					entry.put("parameter", List.of(fold.getParameterLower(),
							fold.getParameterUpper()));
					entry.put("symmetry", fold.getSymmetry());
					entry.put("nature", String.valueOf(fold.getNature()));
					folds.add(entry);
				}
			}
			row.put("cu2_certified_folds", folds);
			ConstructionBoxModel2D box = ConstructionBoxModel2D.capture(definition);
			CertifiedConstructionProgram2D program = box == null ? null : box.getProgram();
			Map<String, Object> pairs = new LinkedHashMap<>();
			for (double m : new double[] {1.0 / 7, 2.0 / 9, 3.0 / 11}) {
				for (boolean nearPi : new boolean[] {false, true}) {
					String key = (nearPi ? "pi" : "0") + " +- m=" + m;
					pairs.put(key, symmetricPair(app, program, locus[1], m, nearPi));
				}
			}
			row.put("symmetric_driver_pairs", pairs);
			rows.put(locus[0], row);
		}
		return rows;
	}

	private static Map<String, Object> symmetricPair(AppGeoCeDG app,
			CertifiedConstructionProgram2D program, String traced, double m, boolean nearPi) {
		Map<String, Object> row = new LinkedHashMap<>();
		PreG9BCu3M3Tower tower = newTower();
		PreG9BCu3M3Evaluator ev = new PreG9BCu3M3Evaluator(tower);
		GeoConic p = (GeoConic) lookup(app, "p");
		Num mm = tower.of(m).square();
		Num cosine = tower.one().subtract(mm).divide(tower.one().add(mm));
		Num sine = tower.of(2 * m).divide(tower.one().add(mm));
		if (nearPi) {
			cosine = cosine.negate();
		}
		Num cx = tower.of(p.getMidpoint2D().getX());
		Num cy = tower.of(p.getMidpoint2D().getY());
		Num r = tower.of(p.getHalfAxis(0));
		P plus = ev.unit(ev.constant(cx.add(r.multiply(cosine))),
				ev.constant(cy.add(r.multiply(sine))));
		P minus = ev.unit(ev.constant(cx.add(r.multiply(cosine))),
				ev.constant(cy.subtract(r.multiply(sine))));
		try {
			if (program != null) {
				P a = ev.evaluate(program, plus);
				P b = ev.evaluate(program, minus);
				row.put("captured_program", equalPoints(a, b));
			}
		} catch (PreG9BCu3M3Evaluator.Stop stop) {
			row.put("captured_program", stop.failure.name());
		}
		try {
			// Both readings share one tower, so that the two traced points compare exactly.
			Semantics first = new Semantics(Semantics.Rule.STAND_IN, false, tower, ev);
			first.substitute(lookup(app, "E_1"), plus, "DRIVER");
			Semantics second = new Semantics(Semantics.Rule.STAND_IN, false, tower, ev);
			second.substitute(lookup(app, "E_1"), minus, "DRIVER");
			P a = first.point(lookup(app, traced));
			P b = second.point(lookup(app, traced));
			row.put("constructive_reading", equalPoints(a, b));
		} catch (RuntimeException failure) {
			row.put("constructive_reading", "FAILURE " + failure.getClass().getSimpleName());
		}
		return row;
	}

	private static String equalPoints(P a, P b) {
		boolean x = a.inhomX.v.subtract(b.inhomX.v).signum() == 0;
		boolean y = a.inhomY.v.subtract(b.inhomY.v).signum() == 0;
		return x && y ? "EQUAL (exact)" : "DIFFERENT (dx " + approx(a.inhomX.v.subtract(
				b.inhomX.v)) + ", dy " + approx(a.inhomY.v.subtract(b.inhomY.v)) + ")";
	}

	// ---------------------------------------------------------------- dynamics

	private static Map<String, Object> dynamics(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		double rbCone = number(app, "rbCone");
		for (double value : new double[] {3, 4, 6, 7.5}) {
			PreG9BCu0Corpus.setNumber(app, "rbCone", value);
			rows.put("rbCone=" + value, revision(app));
		}
		PreG9BCu0Corpus.setNumber(app, "rbCone", rbCone);
		double angle = number(app, ALPHA);
		for (double degrees : new double[] {35, 60}) {
			PreG9BCu0Corpus.setNumber(app, ALPHA, Math.toRadians(degrees));
			rows.put("alpha_cone=" + degrees, revision(app));
		}
		PreG9BCu0Corpus.setNumber(app, ALPHA, angle);
		GeoPoint d1 = (GeoPoint) lookup(app, "D_1");
		GeoPoint h = (GeoPoint) lookup(app, "H");
		double dx = d1.getInhomX();
		double dy = d1.getInhomY();
		for (double factor : new double[] {0.5, 1.3, 3.0}) {
			d1.setCoords(h.getInhomX() + factor * (dx - h.getInhomX()),
					h.getInhomY() + factor * (dy - h.getInhomY()), 1);
			d1.updateRepaint();
			rows.put("D_1 radius factor " + factor, revision(app));
		}
		d1.setCoords(dx, dy, 1);
		d1.updateRepaint();
		double exc = number(app, "excCyl");
		PreG9BCu0Corpus.setNumber(app, "excCyl", 3.7);
		rows.put("excCyl=3.7 (control)", revision(app));
		PreG9BCu0Corpus.setNumber(app, "excCyl", exc);
		rows.put("restored", revision(app));
		return rows;
	}

	private static Map<String, Object> revision(AppGeoCeDG app) {
		Map<String, Object> row = new LinkedHashMap<>();
		long maxOperations = 0;
		int maxDepth = 0;
		for (String[] contact : CONTACTS) {
			Map<String, Object> entry = new LinkedHashMap<>();
			PreG9BCu3M3GateProbe.Contact captured = PreG9BCu3M3GateProbe.contact(app, contact[1],
					"E_1", contact[3], contact[4], false);
			Object authority = captured.report.get("authority");
			entry.put("binary64_authority", authority instanceof Map
					? ((Map<?, ?>) authority).get("decision") : captured.report.get("outcome"));
			Map<String, Object> constructive = constructiveContact(app, Semantics.Rule.STAND_IN,
					contact, null);
			Object incidence = constructive.get("incidence_with_the_constructive_target");
			entry.put("constructive", incidence == null ? constructive.get("outcome") : incidence);
			entry.put("constructive_h1_zero",
					constructive.get("residual_first_derivative_is_exact_zero"));
			Object limited = constructive.get("in_the_limited_part_of_the_constructive_target");
			if (limited != null) {
				entry.put("constructive_in_limited_part", limited);
			}
			Map<?, ?> counters = (Map<?, ?>) constructive.get("counters");
			maxOperations = Math.max(maxOperations, ((Number) counters.get("operations"))
					.longValue());
			maxDepth = Math.max(maxDepth, ((Number) counters.get("tower_depth")).intValue());
			row.put(contact[0], entry);
		}
		row.put("constructive_max_operations", maxOperations);
		row.put("constructive_max_tower_depth", maxDepth);
		return row;
	}

	// ---------------------------------------------------------------- CU2 certificates

	/** CU2 existence structures of e_1 and o_1 at the base and after one-ulp perturbations. */
	private static Map<String, Object> stability(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		Map<String, Object> base = snapshot(app);
		rows.put("base", base);
		GeoPoint d1 = (GeoPoint) lookup(app, "D_1");
		double dx = d1.getInhomX();
		double dy = d1.getInhomY();
		d1.setCoords(Math.nextUp(dx), dy, 1);
		d1.updateRepaint();
		rows.put("D_1.x + 1 ulp", compare(base, snapshot(app)));
		d1.setCoords(dx, dy, 1);
		d1.updateRepaint();
		double rbCone = number(app, "rbCone");
		PreG9BCu0Corpus.setNumber(app, "rbCone", Math.nextUp(rbCone));
		rows.put("rbCone + 1 ulp", compare(base, snapshot(app)));
		PreG9BCu0Corpus.setNumber(app, "rbCone", rbCone);
		double angle = number(app, ALPHA);
		PreG9BCu0Corpus.setNumber(app, ALPHA, Math.nextUp(angle));
		rows.put("alpha_cone + 1 ulp", compare(base, snapshot(app)));
		PreG9BCu0Corpus.setNumber(app, ALPHA, angle);
		double t = PreG9BCu0Corpus.pathParameter(app, "H");
		PreG9BCu0Corpus.movePathPoint(app, "H", Math.nextUp(t));
		rows.put("H path parameter + 1 ulp", compare(base, snapshot(app)));
		PreG9BCu0Corpus.movePathPoint(app, "H", t);
		rows.put("restored", compare(base, snapshot(app)));
		return rows;
	}

	private static Map<String, Object> snapshot(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		for (String label : List.of("e_1", "o_1")) {
			Map<String, Object> row = new LinkedHashMap<>();
			LocusExistenceStructure2D structure = PreG9BCu0Corpus.locus(app, label)
					.getSemanticDefinition().getBranch(PreG9BCu0Corpus.MAIN)
					.getExistenceStructure();
			if (structure == null) {
				row.put("structure", "NONE");
				rows.put(label, row);
				continue;
			}
			row.put("completeness", String.valueOf(structure.getCompleteness()));
			row.put("certifier", structure.getCertifierContract());
			List<Object> components = new ArrayList<>();
			for (LocusExistenceStructure2D.ContinuousComponent component : structure
					.getContinuousValidComponents()) {
				components.add(List.of(component.getStart(), component.getEnd(),
						component.isClosedPeriodic(), component.crossesSeam()));
			}
			row.put("components", components);
			List<Object> folds = new ArrayList<>();
			for (LocusCertifiedFold2D fold : structure.getCertifiedFolds()) {
				folds.add(List.of(fold.getParameterLower(), fold.getParameterUpper(),
						fold.getSymmetry()));
			}
			row.put("folds", folds);
			row.put("point_components", structure.getPointComponents().size());
			row.put("singular_parameters", structure.getSingularParameters().size());
			rows.put(label, row);
		}
		return rows;
	}

	private static Map<String, Object> compare(Map<String, Object> base,
			Map<String, Object> changed) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("identical_to_base", base.equals(changed));
		if (!base.equals(changed)) {
			row.put("structures", changed);
		}
		return row;
	}

	// ---------------------------------------------------------------- helpers

	private static Map<String, Object> counters(PreG9BCu3M3Tower tower) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("operations", tower.operations());
		row.put("tower_depth", tower.depth());
		row.put("max_bits", tower.maxBits);
		return row;
	}

	private static List<Object> xy(P point) {
		return List.of(approx(point.inhomX.v), approx(point.inhomY.v));
	}

	private static String approx(Num value) {
		return value == null ? null : value.approximate().round(REPORT).toString();
	}

	private static String approx(BigDecimal value) {
		return value.round(REPORT).toString();
	}

	private static GeoElement lookup(AppGeoCeDG app, String label) {
		GeoElement geo = app.getKernel().lookupLabel(label);
		if (geo == null) {
			throw new IllegalStateException("missing " + label);
		}
		return geo;
	}

	private static double number(AppGeoCeDG app, String label) {
		return ((GeoNumeric) lookup(app, label)).getValue();
	}

	private static <T> T onEdt(Callable<T> action) throws Exception {
		return PreG9BR6PlusF3Fixtures.onEdt(action);
	}
}
