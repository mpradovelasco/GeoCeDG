/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;

/**
 * PRE-G9B-CU0 geometric references and characterization pins of the author corpus.
 *
 * <p>Two kinds of values are computed. {@code preserve} values are geometric facts of the
 * unchanged product (locus points, lengths, localized branch ends, contact points and
 * contact curvatures, the admissible transverse root): corrections must reproduce them
 * within the declared tolerances unless an approved contract changes them explicitly.
 * {@code pins} record the signature of a characterized defect together with its owning
 * PRE-G9B-CU package; they are reference evidence, not normative expectations, and the owning
 * package inverts or replaces them with its own acceptance criteria.
 *
 * <p>The same methods produce the committed baseline (harness scenario {@code references})
 * and the values the characterization tests compare against it. Call them on the event
 * thread. Methods that change the document restore the saved state before returning.
 */
final class PreG9BCu0References {

	/** Loci driven by {@code N} on the cylinder circle {@code g_1}. */
	static final List<String> FIRST_FAMILY = List.of("o", "v", "w", "a_1");
	/** Loci driven by {@code E_1} on the cone base circle {@code p}, without {@code e_2}. */
	static final List<String> SECOND_FAMILY = List.of("e_1", "o_1");
	/** Construction points recorded as references. */
	static final List<String> POINTS = List.of("W", "A_1", "B_1", "C_1", "J_1", "K_1",
			"M_1", "N_1", "O_1", "P_1", "Q_1", "R_1");
	/** Absolute tolerance of locus and construction points (model units). */
	static final double POINT_TOLERANCE = 1e-9;
	/**
	 * Absolute tolerance of values derived from a semantic length (model units). The current
	 * evaluator-only metric of {@code o_1} differs from an independent reference by about
	 * 1.1e-7 (H12), so a conforming metric correction may move these values by that order.
	 */
	static final double LENGTH_TOLERANCE = 1e-6;
	/** Absolute tolerance of localized branch ends (driver parameter). */
	static final double BOUNDARY_TOLERANCE = 1e-7;
	/** {@code excCyl} values of the existence sweep (B07, H01). */
	static final double[] SWEEP = {2.9, 3.7, 3.85, 3.9};
	/** Grid size of the sampled existence evidence. */
	static final int SWEEP_SAMPLES = 1440;
	/** Driver parameters at which {@code d_2 = Length(w_1)} is recorded. */
	static final double[] LENGTH_PARAMETERS = {-3, -1, -1e-3, 1e-3, 1, 3,
			Math.PI - 1e-3, -Math.PI + 1e-3};

	private PreG9BCu0References() {
	}

	/** Uniform grid {@code -pi + 2 pi k / n}, k = 0..n-1, over the circle driver domain. */
	static double[] grid(int n) {
		double[] values = new double[n];
		for (int k = 0; k < n; k++) {
			values[k] = -Math.PI + 2 * Math.PI * k / n;
		}
		return values;
	}

	// ---------------------------------------------------------------- preserved references

	/** Saved driver positions, parameters, construction points and {@code d_2}. */
	static Map<String, Object> state(AppGeoCeDG app) {
		Map<String, Object> state = new LinkedHashMap<>();
		state.put("excCyl", number(app, "excCyl"));
		state.put("N_t", PreG9BCu0Corpus.pathParameter(app, "N"));
		state.put("E_1_t", PreG9BCu0Corpus.pathParameter(app, "E_1"));
		state.put("d_2", number(app, "d_2"));
		Map<String, Object> points = new LinkedHashMap<>();
		for (String label : POINTS) {
			points.put(label, xy(app, label));
		}
		state.put("points", points);
		return state;
	}

	/** Locus samples {@code [t, x, y]}; an invalid evaluation is {@code [t, null, null]}. */
	static List<Object> samples(GeoLocusV2 locus, double[] parameters) {
		List<Object> rows = new ArrayList<>();
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			for (double t : parameters) {
				LocusPoint2D point = PreG9BCu0Corpus.point(locus, t, session);
				rows.add(point == null ? list(t, null, null)
						: list(t, point.getX(), point.getY()));
			}
		}
		return rows;
	}

	/** Samples of the six loci other than {@code e_2} on the 24-point driver grid. */
	static Map<String, Object> locusSamples(AppGeoCeDG app) {
		Map<String, Object> loci = new LinkedHashMap<>();
		double[] grid = grid(24);
		for (String label : FIRST_FAMILY) {
			loci.put(label, samples(PreG9BCu0Corpus.locus(app, label), grid));
		}
		for (String label : SECOND_FAMILY) {
			loci.put(label, samples(PreG9BCu0Corpus.locus(app, label), withZero(grid)));
		}
		return loci;
	}

	/**
	 * {@code d_2 = Length(LocusLength(o_1, N_1, K_1))} with the driver {@code E_1} moved to
	 * each parameter of {@link #LENGTH_PARAMETERS}; the driver is restored afterwards.
	 */
	static List<Object> orientedLengthProfile(AppGeoCeDG app) {
		double saved = PreG9BCu0Corpus.pathParameter(app, "E_1");
		List<Object> rows = new ArrayList<>();
		try {
			for (double t : LENGTH_PARAMETERS) {
				PreG9BCu0Corpus.movePathPoint(app, "E_1", t);
				rows.add(list(t, number(app, "d_2")));
			}
		} finally {
			PreG9BCu0Corpus.movePathPoint(app, "E_1", saved);
		}
		return rows;
	}

	/**
	 * Total semantic length {@code Length(L)} of a Locus V2, or null when the guarded
	 * scalar is not admissible. Adds an auxiliary object to the working document, so call
	 * it after every counter-based characterization.
	 */
	static Double totalLength(AppGeoCeDG app, String locus) {
		GeoElement length = PreG9BR6PlusF3Fixtures.q(app, "cu0Total"
				+ locus.replace("_", "") + "=Length(" + locus + ")");
		return length instanceof GeoNumeric number && number.isDefined()
				? number.getValue() : null;
	}

	/** Samples of {@code e_2} on the grid and on both sides of the oriented-length origin. */
	static List<Object> e2Samples(AppGeoCeDG app) {
		return samples(PreG9BCu0Corpus.locus(app, "e_2"), e2Parameters());
	}

	static double[] e2Parameters() {
		double[] grid = grid(24);
		double[] extra = {-1e-6, 1e-6, -1e-3, 1e-3, Math.PI - 1e-9};
		double[] values = new double[grid.length + extra.length];
		System.arraycopy(grid, 0, values, 0, grid.length);
		System.arraycopy(extra, 0, values, grid.length, extra.length);
		return values;
	}

	// ---------------------------------------------------------------- B10.1, H03 (owner CU1)

	/**
	 * B10.1 pin: work done inside the isolated slice of {@code e_2} by two single-point
	 * evaluations. The nested copy of {@code o_1} is only reachable by reflection in the
	 * current implementation; CU1 is expected to expose nested counters and replace this pin.
	 */
	static Map<String, Object> nestedComposition(AppGeoCeDG app) throws ReflectiveOperationException {
		GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_2");
		GeoLocusV2 copy = nestedCopy(e2, "o_1");
		Map<String, Object> pin = new LinkedHashMap<>();
		pin.put("incidence", "B10.1");
		pin.put("owner", "CU1");
		pin.put("isolated_slice_objects", isolatedConstruction(e2).getGeoSetConstructionOrder()
				.size());
		List<Object> steps = new ArrayList<>();
		for (double t : new double[] {1.0, -1.0}) {
			long revision = copy.getSemanticRevision();
			long builds = copy.getMetricInstrumentation().snapshot().getComponentStateBuilds();
			long evaluations = copy.getInstrumentation().snapshot().getEvaluatorCalls();
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
				if (PreG9BCu0Corpus.point(e2, t, session) == null) {
					throw new IllegalStateException("e_2 is not valid at " + t);
				}
			}
			Map<String, Object> step = new LinkedHashMap<>();
			step.put("t", t);
			step.put("nested_revision_delta", copy.getSemanticRevision() - revision);
			step.put("nested_metric_builds_delta", copy.getMetricInstrumentation().snapshot()
					.getComponentStateBuilds() - builds);
			step.put("nested_evaluator_calls_delta", copy.getInstrumentation().snapshot()
					.getEvaluatorCalls() - evaluations);
			steps.add(step);
		}
		pin.put("single_point_evaluations", steps);
		pin.put("outer_dependency_slice_builds", e2.getInstrumentation().snapshot()
				.getDependencySliceBuilds());
		pin.put("outer_dependency_slice_synchronizations", e2.getInstrumentation().snapshot()
				.getDependencySliceSynchronizations());
		return pin;
	}

	/** H03 pin: a pure driver move republishes {@code o_1} and rebuilds its metric index. */
	static Map<String, Object> revisionChurn(AppGeoCeDG app) {
		GeoLocusV2 o1 = PreG9BCu0Corpus.locus(app, "o_1");
		double saved = PreG9BCu0Corpus.pathParameter(app, "E_1");
		long revision = o1.getSemanticRevision();
		long builds = o1.getMetricInstrumentation().snapshot().getComponentStateBuilds();
		PreG9BCu0Corpus.movePathPoint(app, "E_1", saved + 0.05);
		long movedRevision = o1.getSemanticRevision();
		long movedBuilds = o1.getMetricInstrumentation().snapshot().getComponentStateBuilds();
		PreG9BCu0Corpus.movePathPoint(app, "E_1", saved);
		Map<String, Object> pin = new LinkedHashMap<>();
		pin.put("incidence", "H03");
		pin.put("owner", "CU1");
		pin.put("o_1_revision_delta_on_driver_move", movedRevision - revision);
		pin.put("o_1_metric_builds_delta_on_driver_move", movedBuilds - builds);
		return pin;
	}

	static GeoLocusV2 nestedCopy(GeoLocusV2 outer, String label)
			throws ReflectiveOperationException {
		for (GeoElement geo : isolatedConstruction(outer).getGeoSetConstructionOrder()) {
			if (geo instanceof GeoLocusV2 copy && label.equals(copy.getLabelSimple())) {
				return copy;
			}
		}
		throw new IllegalStateException("No nested copy " + label);
	}

	static Construction isolatedConstruction(GeoLocusV2 outer)
			throws ReflectiveOperationException {
		AlgoDependentPointLocusV2 algo = (AlgoDependentPointLocusV2) outer.getParentAlgorithm();
		Field evaluator = AlgoDependentPointLocusV2.class.getDeclaredField("evaluator");
		evaluator.setAccessible(true);
		Field construction = ReconstructibleLocusEvaluator2D.class
				.getDeclaredField("isolatedConstruction");
		construction.setAccessible(true);
		return (Construction) construction.get(evaluator.get(algo));
	}

	// ---------------------------------------------------------------- B10.2 (owner CU2)

	/** B10.2 pin: existence, oriented-length jump and render chord of {@code e_2}. */
	static Map<String, Object> lengthWrap(AppGeoCeDG app) {
		GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_2");
		Map<String, Object> pin = new LinkedHashMap<>();
		pin.put("incidence", "B10.2");
		pin.put("owner", "CU2");
		pin.put("existence", existence(e2));
		LocusPoint2D before;
		LocusPoint2D after;
		LocusPoint2D seamLow;
		LocusPoint2D seamHigh;
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			before = PreG9BCu0Corpus.point(e2, -1e-6, session);
			after = PreG9BCu0Corpus.point(e2, 1e-6, session);
			seamLow = PreG9BCu0Corpus.point(e2, -Math.PI, session);
			seamHigh = PreG9BCu0Corpus.point(e2, Math.PI - 1e-9, session);
		}
		pin.put("jump_parameter", 0.0);
		pin.put("jump_distance", distance(before, after));
		pin.put("seam_distance", distance(seamLow, seamHigh));
		LocusRenderData2D render = new LocusRenderCache2D().getOrBuild(e2,
				PreG9BCu0Corpus.authorPolicy());
		List<LocusRenderData2D.Vertex> vertices = render.getVertices();
		double chord = 0;
		for (int i = 1; i < vertices.size(); i++) {
			LocusRenderData2D.Vertex a = vertices.get(i - 1);
			LocusRenderData2D.Vertex b = vertices.get(i);
			double low = Math.min(a.getSemanticParameter(), b.getSemanticParameter());
			double high = Math.max(a.getSemanticParameter(), b.getSemanticParameter());
			if (!b.startsSubpath() && low <= 0 && high >= 0 && low < high) {
				chord = Math.max(chord, distance(a.getPoint(), b.getPoint()));
			}
		}
		pin.put("render_vertices", vertices.size());
		pin.put("render_subpaths", subpaths(vertices));
		pin.put("render_segment_across_jump_length", chord);
		return pin;
	}

	// ---------------------------------------------------------------- B07, H01 (owner CU2)

	/**
	 * Existence sweep over {@code excCyl}: certified structure versus sampled evidence, branch
	 * ends localized by bisection on evaluation validity (numerical evidence, not a
	 * certificate) and the render gap at those ends; then H01 at {@code excCyl = 4}. The
	 * saved {@code excCyl} and {@code N} are restored.
	 */
	static Map<String, Object> existenceSweep(AppGeoCeDG app) {
		double savedExc = number(app, "excCyl");
		double savedN = PreG9BCu0Corpus.pathParameter(app, "N");
		Map<String, Object> sweep = new LinkedHashMap<>();
		try {
			for (double value : SWEEP) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", value);
				Map<String, Object> loci = new LinkedHashMap<>();
				for (String label : FIRST_FAMILY) {
					loci.put(label, existenceEvidence(PreG9BCu0Corpus.locus(app, label)));
				}
				sweep.put(key(value), loci);
			}
			PreG9BCu0Corpus.setNumber(app, "excCyl", 4.0);
			Map<String, Object> h01 = new LinkedHashMap<>();
			h01.put("incidence", "H01");
			h01.put("owner", "CU2");
			h01.put("excCyl", 4.0);
			h01.put("N_t", PreG9BCu0Corpus.pathParameter(app, "N"));
			h01.put("traced_point_W_defined", app.getKernel().lookupLabel("W").isDefined());
			h01.put("defined_at_saved_driver", definedFlags(app));
			PreG9BCu0Corpus.movePathPoint(app, "N", -1.0);
			h01.put("defined_with_driver_at_minus_one", definedFlags(app));
			sweep.put("H01", h01);
		} finally {
			PreG9BCu0Corpus.setNumber(app, "excCyl", savedExc);
			PreG9BCu0Corpus.movePathPoint(app, "N", savedN);
		}
		return sweep;
	}

	private static Map<String, Object> definedFlags(AppGeoCeDG app) {
		Map<String, Object> flags = new LinkedHashMap<>();
		for (String label : FIRST_FAMILY) {
			flags.put(label, PreG9BCu0Corpus.locus(app, label).isDefined());
		}
		return flags;
	}

	static Map<String, Object> existenceEvidence(GeoLocusV2 locus) {
		Map<String, Object> evidence = existence(locus);
		LocusDefinition2D definition = locus.getSemanticDefinition();
		if (definition == null) {
			return evidence;
		}
		LocusInterval2D domain = definition.getProvider().getDeclaredDomain();
		double a = domain.getLower();
		double b = domain.getUpper();
		List<Object> runs = new ArrayList<>();
		List<Object> ends = new ArrayList<>();
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4096)) {
			double runStart = Double.NaN;
			double previous = Double.NaN;
			boolean previousValid = false;
			for (int i = 0; i <= SWEEP_SAMPLES; i++) {
				double t = a + (b - a) * i / SWEEP_SAMPLES;
				boolean valid = PreG9BCu0Corpus.point(locus, t, session) != null;
				if (i > 0 && valid != previousValid) {
					double end = bisect(locus, previous, t, previousValid);
					LocusPoint2D point = PreG9BCu0Corpus.point(locus, end, session);
					ends.add(list(end, point.getX(), point.getY()));
				}
				if (valid && Double.isNaN(runStart)) {
					runStart = t;
				}
				if (!valid && !Double.isNaN(runStart)) {
					runs.add(list(runStart, previous));
					runStart = Double.NaN;
				}
				previous = t;
				previousValid = valid;
			}
			if (!Double.isNaN(runStart)) {
				runs.add(list(runStart, previous));
			}
		}
		evidence.put("sampled_valid_runs", runs);
		evidence.put("localized_branch_ends", ends);
		LocusRenderData2D render = new LocusRenderCache2D().getOrBuild(locus,
				PreG9BCu0Corpus.authorPolicy());
		List<LocusRenderData2D.Vertex> vertices = render.getVertices();
		evidence.put("render_vertices", vertices.size());
		evidence.put("render_subpaths", subpaths(vertices));
		double gap = 0;
		for (Object end : ends) {
			List<?> row = (List<?>) end;
			LocusPoint2D point = new LocusPoint2D((Double) row.get(1), (Double) row.get(2));
			double best = Double.POSITIVE_INFINITY;
			for (LocusRenderData2D.Vertex vertex : vertices) {
				best = Math.min(best, distance(point, vertex.getPoint()));
			}
			gap = Math.max(gap, best * PreG9BCu0Corpus.AUTHOR_SCALE);
		}
		evidence.put("max_render_gap_at_branch_end_px", gap);
		return evidence;
	}

	/** Bisection between a valid and an invalid parameter; returns the valid side. */
	private static double bisect(GeoLocusV2 locus, double left, double right,
			boolean leftValid) {
		double valid = leftValid ? left : right;
		double invalid = leftValid ? right : left;
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			for (int i = 0; i < 60 && Math.abs(valid - invalid) > 1e-13; i++) {
				double middle = (valid + invalid) / 2;
				if (PreG9BCu0Corpus.point(locus, middle, session) != null) {
					valid = middle;
				} else {
					invalid = middle;
				}
			}
		}
		return valid;
	}

	static Map<String, Object> existence(GeoLocusV2 locus) {
		Map<String, Object> existence = new LinkedHashMap<>();
		existence.put("defined", locus.isDefined());
		LocusDefinition2D definition = locus.getSemanticDefinition();
		if (definition == null) {
			return existence;
		}
		LocusBranch2D branch = definition.getBranches().get(0);
		LocusExistenceStructure2D structure = branch.getExistenceStructure();
		existence.put("completeness", structure.getCompleteness().name());
		existence.put("certifier_contract", String.valueOf(structure.getCertifierContract()));
		List<Object> components = new ArrayList<>();
		for (LocusInterval2D component : branch.getCertifiedContinuousValidComponents()) {
			components.add(component.toString());
		}
		existence.put("certified_components", components);
		existence.put("declared_domain", definition.getProvider().getDeclaredDomain()
				.toString());
		existence.put("periodic", definition.getProvider().isPeriodic()
				&& branch.getProperties().contains(BranchProperty.PERIODIC));
		existence.put("full_period_closure", fullPeriodClosure(definition, branch));
		return existence;
	}

	/** The renderer's closing condition for a certified periodic component. */
	private static boolean fullPeriodClosure(LocusDefinition2D definition,
			LocusBranch2D branch) {
		List<LocusInterval2D> components = branch.getCertifiedContinuousValidComponents();
		return definition.getProvider().isPeriodic()
				&& branch.getProperties().contains(BranchProperty.PERIODIC)
				&& branch.getExistenceStructure().getCompleteness()
						== LocusExistenceStructure2D.Completeness.COMPLETE
				&& components.size() == 1
				&& components.get(0).equals(branch.getDeclaredDriverDomain())
				&& components.get(0).equals(definition.getProvider().getDeclaredDomain());
	}

	// ---------------------------------------------------------------- B06 (owner CU2)

	/** B06 pin and smoothness reference: render tessellation versus semantic smoothness. */
	static Map<String, Object> tessellation(AppGeoCeDG app) {
		Map<String, Object> loci = new LinkedHashMap<>();
		List<String> labels = new ArrayList<>(FIRST_FAMILY);
		labels.addAll(SECOND_FAMILY);
		for (String label : labels) {
			GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
			List<LocusRenderData2D.Vertex> vertices = new LocusRenderCache2D()
					.getOrBuild(locus, PreG9BCu0Corpus.authorPolicy()).getVertices();
			double maxTurn = 0;
			for (int i = 2; i < vertices.size(); i++) {
				if (vertices.get(i).startsSubpath() || vertices.get(i - 1).startsSubpath()) {
					continue;
				}
				maxTurn = Math.max(maxTurn, turn(vertices.get(i - 2).getPoint(),
						vertices.get(i - 1).getPoint(), vertices.get(i).getPoint()));
			}
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("render_vertices", vertices.size());
			row.put("render_max_turn_degrees", Math.toDegrees(maxTurn));
			row.put("midpoint_defect_h1e-3", midpointDefect(locus, 1e-3));
			row.put("midpoint_defect_h1e-4", midpointDefect(locus, 1e-4));
			loci.put(label, row);
		}
		Map<String, Object> pin = new LinkedHashMap<>();
		pin.put("incidence", "B06");
		pin.put("owner", "CU2");
		pin.put("loci", loci);
		return pin;
	}

	private static double midpointDefect(GeoLocusV2 locus, double h) {
		LocusInterval2D domain = locus.getSemanticDefinition().getProvider().getDeclaredDomain();
		double a = domain.getLower();
		double b = domain.getUpper();
		double max = 0;
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			for (int i = 0; i < 100; i++) {
				double t = a + (b - a) * (i + 0.5) / 100;
				LocusPoint2D m = PreG9BCu0Corpus.point(locus, t, session);
				LocusPoint2D l = PreG9BCu0Corpus.point(locus, t - h, session);
				LocusPoint2D r = PreG9BCu0Corpus.point(locus, t + h, session);
				if (m == null || l == null || r == null) {
					continue;
				}
				max = Math.max(max, Math.hypot(m.getX() - (l.getX() + r.getX()) / 2,
						m.getY() - (l.getY() + r.getY()) / 2));
			}
		}
		return max;
	}

	// ---------------------------------------------------------------- B08, B09 (owner CU3)

	/**
	 * B08/B09: the three requested contacts and the transverse seam root of
	 * {@code e_1} with {@code c_1}; contact curvature from the second difference of the
	 * signed residual at the contact parameter. Adds {@code cu0X1 = Intersect(e_1, n)} and
	 * {@code cu0X2 = Intersect(e_1, c_1)} to the working document.
	 */
	static Map<String, Object> contacts(AppGeoCeDG app) {
		Map<String, Object> contacts = new LinkedHashMap<>();
		contacts.put("Intersect(o_1,h_2)", solutions(app,
				(GeoLocusIntersectionResult) app.getKernel().lookupLabel("u_1")));
		contacts.put("Intersect(e_1,n)", solutions(app, (GeoLocusIntersectionResult)
				PreG9BR6PlusF3Fixtures.must(app, "cu0X1=Intersect(e_1,n)")));
		contacts.put("Intersect(e_1,c_1)", solutions(app, (GeoLocusIntersectionResult)
				PreG9BR6PlusF3Fixtures.must(app, "cu0X2=Intersect(e_1,c_1)")));
		GeoLine n = (GeoLine) app.getKernel().lookupLabel("n");
		GeoLine h2 = (GeoLine) app.getKernel().lookupLabel("h_2");
		GeoConic c1 = (GeoConic) app.getKernel().lookupLabel("c_1");
		Map<String, Object> curvature = new LinkedHashMap<>();
		curvature.put("e_1|n", contactCurvature(PreG9BCu0Corpus.locus(app, "e_1"),
				lineResidual(n)));
		curvature.put("e_1|c_1", contactCurvature(PreG9BCu0Corpus.locus(app, "e_1"),
				circleResidual(c1)));
		curvature.put("o_1|h_2", contactCurvature(PreG9BCu0Corpus.locus(app, "o_1"),
				lineResidual(h2)));
		contacts.put("signed_residual_at_t0", curvature);
		return contacts;
	}

	private static List<Object> solutions(AppGeoCeDG app, GeoLocusIntersectionResult geo) {
		LocusIntersectionResult2D result = geo.getIntersectionResult();
		List<Object> rows = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : result.getFiniteSolutions()) {
			Map<String, Object> row = new LinkedHashMap<>();
			LocusPoint2D point = solution.getEvaluatedPoint();
			row.put("point", list(point.getX(), point.getY()));
			row.put("contact_class", solution.getClassification().getContactClass().name());
			row.put("domain_location", solution.getClassification().getDomainLocation().name());
			row.put("identity_status", solution.getIdentity().getIdentityStatus().name());
			row.put("admissible", geo.isPointAdmissible(solution.getIdentity().getRootToken()));
			rows.add(row);
		}
		rows.sort((a, b) -> Double.compare(xOf(a), xOf(b)));
		return rows;
	}

	private static double xOf(Object row) {
		return (Double) ((List<?>) ((Map<?, ?>) row).get("point")).get(0);
	}

	interface Residual {
		double at(LocusPoint2D point);
	}

	static Residual lineResidual(GeoLine line) {
		double a = line.getX();
		double b = line.getY();
		double c = line.getZ();
		double norm = Math.hypot(a, b);
		return p -> (a * p.getX() + b * p.getY() + c) / norm;
	}

	static Residual circleResidual(GeoConic conic) {
		double cx = conic.getMidpoint2D().getX();
		double cy = conic.getMidpoint2D().getY();
		double r = conic.getHalfAxis(0);
		return p -> Math.hypot(p.getX() - cx, p.getY() - cy) - r;
	}

	/** Signed residual h(0) and its centred differences at the contact parameter t = 0. */
	private static Map<String, Object> contactCurvature(GeoLocusV2 locus, Residual residual) {
		double step = 1e-3;
		double left;
		double middle;
		double right;
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			left = residual.at(PreG9BCu0Corpus.point(locus, -step, session));
			middle = residual.at(PreG9BCu0Corpus.point(locus, 0, session));
			right = residual.at(PreG9BCu0Corpus.point(locus, step, session));
		}
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("h", middle);
		row.put("dh", (right - left) / (2 * step));
		row.put("d2h", (right - 2 * middle + left) / (step * step));
		return row;
	}

	// ---------------------------------------------------------------- H02, H06 (owner CU3)

	/**
	 * H02 pin: transverse roots of a circular Locus V2 of radius 1.5 r against a line and a
	 * segment of the same relative position, for r = 2, 20, 200 and 2000.
	 */
	static Map<String, Object> scaleSweep() {
		Map<String, Object> sweep = new LinkedHashMap<>();
		for (double r : new double[] {2, 20, 200, 2000}) {
			AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
			String[] commands = {"O=(0,0)", "c=Circle(O," + r + ")", "P=Point(c)",
					"Q=Dilate(P,1.5,O)", "L=LocusV2(Q,P)",
					"g=Line((0," + 0.5 * r + "),(1," + 0.5 * r + "))",
					"s=Segment((-" + 2 * r + "," + 0.4 * r + "),(" + 2 * r + "," + 0.6 * r
							+ "))", "R1=Intersect(L,g)", "R2=Intersect(L,s)"};
			for (String command : commands) {
				PreG9BR6PlusF3Fixtures.must(app, command);
			}
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("line", admissibility((GeoLocusIntersectionResult) app.getKernel()
					.lookupLabel("R1")));
			row.put("segment", admissibility((GeoLocusIntersectionResult) app.getKernel()
					.lookupLabel("R2")));
			sweep.put(key(r), row);
		}
		Map<String, Object> pin = new LinkedHashMap<>();
		pin.put("incidence", "H02");
		pin.put("owner", "CU3");
		pin.put("geometric_truth", "two transverse roots for the line and for the segment at"
				+ " every scale");
		pin.put("radius", sweep);
		return pin;
	}

	private static Map<String, Object> admissibility(GeoLocusIntersectionResult geo) {
		int admissible = 0;
		List<LocusIntersectionSolution2D> solutions = geo.getIntersectionResult()
				.getFiniteSolutions();
		for (LocusIntersectionSolution2D solution : solutions) {
			admissible += geo.isPointAdmissible(solution.getIdentity().getRootToken()) ? 1 : 0;
		}
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("published_solutions", solutions.size());
		row.put("admissible_solutions", admissible);
		return row;
	}

	/**
	 * H10/H11 pins (owner CU1) with their geometric truth: the evaluator-only metric of a
	 * point-driven Locus V2 tracing {@code y = sin(2 pi k x)} on {@code [0, 1]}, and of a
	 * circle of radius r traced by a uniform angular driver. The reference lengths are
	 * independent midpoint-rule integrals of the exact arc-length integrand (sin family)
	 * and {@code 2 pi r} (circles).
	 */
	static Map<String, Object> metricConformance() {
		Map<String, Object> sinus = new LinkedHashMap<>();
		for (int k : new int[] {1, 2, 4}) {
			AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
			for (String command : new String[] {"s=Segment((0,0),(1,0))", "P=Point(s)",
					"Q=(x(P), sin(" + k + "*2*pi*x(P)))", "L=LocusV2(Q,P)"}) {
				PreG9BR6PlusF3Fixtures.must(app, command);
			}
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("reported_length", totalLength(app, "L"));
			row.put("reference_length", sinusLength(k));
			sinus.put("k=" + k, row);
		}
		Map<String, Object> circles = new LinkedHashMap<>();
		for (double r : new double[] {1, 7, 8, 20}) {
			AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
			for (String command : new String[] {"c=Circle((0,0)," + r + ")", "P=Point(c)",
					"Q=P+(0,0)", "L=LocusV2(Q,P)"}) {
				PreG9BR6PlusF3Fixtures.must(app, command);
			}
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("reported_length", totalLength(app, "L"));
			row.put("reference_length", 2 * Math.PI * r);
			circles.put(key(r), row);
		}
		Map<String, Object> pin = new LinkedHashMap<>();
		pin.put("incidence", "H10/H11");
		pin.put("owner", "CU1");
		pin.put("sinus_on_unit_segment", sinus);
		pin.put("circle_radius", circles);
		return pin;
	}

	/** Midpoint rule (2 000 000 cells) of the exact arc-length integrand of sin(2 pi k x). */
	static double sinusLength(int k) {
		int n = 2_000_000;
		double h = 1.0 / n;
		double sum = 0;
		for (int i = 0; i < n; i++) {
			double slope = k * 2 * Math.PI * Math.cos(k * 2 * Math.PI * (i + 0.5) * h);
			sum += Math.sqrt(1 + slope * slope) * h;
		}
		return sum;
	}

	/**
	 * Independent length of a periodic Locus V2 over its whole driver domain: Richardson
	 * extrapolation of closed chord sums with 16 384 and 65 536 uniform driver samples.
	 */
	static double independentLength(GeoLocusV2 locus) {
		double coarse = closedChordSum(locus, 16_384);
		double fine = closedChordSum(locus, 65_536);
		return fine + (fine - coarse) / 15;
	}

	private static double closedChordSum(GeoLocusV2 locus, int n) {
		double sum = 0;
		LocusPoint2D first = null;
		LocusPoint2D last = null;
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			for (int i = 0; i < n; i++) {
				LocusPoint2D point = PreG9BCu0Corpus.point(locus, -Math.PI + 2 * Math.PI * i / n,
						session);
				if (first == null) {
					first = point;
				}
				if (last != null) {
					sum += distance(last, point);
				}
				last = point;
			}
		}
		return sum + distance(first, last);
	}

	/** H06 pin: the axes are not accepted as Locus V2 intersection targets. */
	static Map<String, Object> axisTarget() {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
		for (String command : new String[] {"O=(0,0)", "c=Circle(O,2)", "P=Point(c)",
				"Q=Dilate(P,1.5,O)", "L=LocusV2(Q,P)"}) {
			PreG9BR6PlusF3Fixtures.must(app, command);
		}
		Map<String, Object> pin = new LinkedHashMap<>();
		pin.put("incidence", "H06");
		pin.put("owner", "CU3");
		pin.put("Intersect(L,xAxis)_created", PreG9BR6PlusF3Fixtures.q(app,
				"X=Intersect(L,xAxis)") != null);
		pin.put("Intersect(L,yAxis)_created", PreG9BR6PlusF3Fixtures.q(app,
				"Y=Intersect(L,yAxis)") != null);
		pin.put("Intersect(L,y=0)_created", PreG9BR6PlusF3Fixtures.q(app,
				"Z=Intersect(L,y=0)") != null);
		return pin;
	}

	// ---------------------------------------------------------------- baseline composition

	/**
	 * Splits an existence sweep into its preserved part (localized branch ends, geometric
	 * facts the renderer must reach) and its pinned part (certificate, sampled runs, render).
	 */
	static Map<String, Object> branchEnds(Map<String, Object> sweep) {
		Map<String, Object> ends = new LinkedHashMap<>();
		for (double value : SWEEP) {
			Map<String, Object> loci = new LinkedHashMap<>();
			Map<?, ?> evidence = (Map<?, ?>) sweep.get(key(value));
			for (String label : FIRST_FAMILY) {
				loci.put(label, ((Map<?, ?>) evidence.get(label)).get("localized_branch_ends"));
			}
			ends.put(key(value), loci);
		}
		return ends;
	}

	static Map<String, Object> existencePins(Map<String, Object> sweep) {
		Map<String, Object> pins = new LinkedHashMap<>();
		pins.put("incidence", "B07");
		pins.put("owner", "CU2");
		for (double value : SWEEP) {
			Map<String, Object> loci = new LinkedHashMap<>();
			Map<?, ?> evidence = (Map<?, ?>) sweep.get(key(value));
			for (String label : FIRST_FAMILY) {
				Map<String, Object> row = new LinkedHashMap<>();
				for (Map.Entry<?, ?> entry : ((Map<?, ?>) evidence.get(label)).entrySet()) {
					if (!"localized_branch_ends".equals(entry.getKey())) {
						row.put(String.valueOf(entry.getKey()), entry.getValue());
					}
				}
				loci.put(label, row);
			}
			pins.put(key(value), loci);
		}
		return pins;
	}

	/** Contact points and residual differences (preserved geometric facts). */
	static Map<String, Object> contactPoints(Map<String, Object> contacts) {
		Map<String, Object> points = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : contacts.entrySet()) {
			if (entry.getValue() instanceof List<?> rows) {
				List<Object> list = new ArrayList<>();
				for (Object row : rows) {
					list.add(((Map<?, ?>) row).get("point"));
				}
				points.put(entry.getKey(), list);
			} else {
				points.put(entry.getKey(), entry.getValue());
			}
		}
		return points;
	}

	/** Contact classification and admissibility (pins owned by CU3). */
	static Map<String, Object> contactPins(Map<String, Object> contacts) {
		Map<String, Object> pins = new LinkedHashMap<>();
		pins.put("incidence", "B08/B09");
		pins.put("owner", "CU3");
		for (Map.Entry<String, Object> entry : contacts.entrySet()) {
			if (entry.getValue() instanceof List<?> rows) {
				List<Object> list = new ArrayList<>();
				for (Object row : rows) {
					Map<String, Object> pin = new LinkedHashMap<>();
					for (Map.Entry<?, ?> field : ((Map<?, ?>) row).entrySet()) {
						if (!"point".equals(field.getKey())) {
							pin.put(String.valueOf(field.getKey()), field.getValue());
						}
					}
					list.add(pin);
				}
				pins.put(entry.getKey(), list);
			}
		}
		return pins;
	}

	/**
	 * Every section that needs the derived variant without {@code e_2}, in the order the
	 * baseline was generated (counter pins before the sections that add objects).
	 */
	static Map<String, Map<String, Object>> variantSections(AppGeoCeDG app) {
		Map<String, Object> preserve = new LinkedHashMap<>();
		Map<String, Object> pins = new LinkedHashMap<>();
		preserve.put("state", state(app));
		preserve.put("loci", locusSamples(app));
		pins.put("H03", revisionChurn(app));
		preserve.put("oriented_length_profile", orientedLengthProfile(app));
		Map<String, Object> sweep = existenceSweep(app);
		preserve.put("branch_ends", branchEnds(sweep));
		pins.put("B07", existencePins(sweep));
		pins.put("H01", sweep.get("H01"));
		pins.put("B06", tessellation(app));
		Map<String, Object> contacts = contacts(app);
		preserve.put("contacts", contactPoints(contacts));
		pins.put("B08_B09", contactPins(contacts));
		preserve.put("o_1_total_length", totalLength(app, "o_1"));
		preserve.put("o_1_total_length_independent",
				independentLength(PreG9BCu0Corpus.locus(app, "o_1")));
		Map<String, Map<String, Object>> sections = new LinkedHashMap<>();
		sections.put("preserve", preserve);
		sections.put("pins", pins);
		return sections;
	}

	/** Every section that needs the complete native model with {@code e_2}. */
	static Map<String, Map<String, Object>> e2Sections(AppGeoCeDG app)
			throws ReflectiveOperationException {
		Map<String, Object> preserve = new LinkedHashMap<>();
		Map<String, Object> pins = new LinkedHashMap<>();
		preserve.put("e_2", e2Samples(app));
		pins.put("B10.1", nestedComposition(app));
		Map<String, Object> wrap = lengthWrap(app);
		preserve.put("e_2_seam_distance", wrap.remove("seam_distance"));
		pins.put("B10.2", wrap);
		Map<String, Map<String, Object>> sections = new LinkedHashMap<>();
		sections.put("preserve", preserve);
		sections.put("pins", pins);
		return sections;
	}

	/** Sections that use synthetic constructions only. */
	static Map<String, Object> syntheticPins() {
		Map<String, Object> pins = new LinkedHashMap<>();
		pins.put("H02", scaleSweep());
		pins.put("H06", axisTarget());
		pins.put("H10_H11", metricConformance());
		return pins;
	}

	// ---------------------------------------------------------------- helpers

	static double number(AppGeoCeDG app, String label) {
		return ((GeoNumeric) app.getKernel().lookupLabel(label)).getValue();
	}

	static List<Object> xy(AppGeoCeDG app, String label) {
		GeoPoint point = (GeoPoint) app.getKernel().lookupLabel(label);
		return point.isDefined() ? list(point.getInhomX(), point.getInhomY())
				: list(null, null);
	}

	static double[] withZero(double[] grid) {
		double[] values = new double[grid.length + 1];
		System.arraycopy(grid, 0, values, 0, grid.length);
		values[grid.length] = 0;
		return values;
	}

	static int subpaths(List<LocusRenderData2D.Vertex> vertices) {
		int count = 0;
		for (LocusRenderData2D.Vertex vertex : vertices) {
			count += vertex.startsSubpath() ? 1 : 0;
		}
		return count;
	}

	static double distance(LocusPoint2D a, LocusPoint2D b) {
		return Math.hypot(a.getX() - b.getX(), a.getY() - b.getY());
	}

	private static double turn(LocusPoint2D a, LocusPoint2D b, LocusPoint2D c) {
		double first = Math.atan2(b.getY() - a.getY(), b.getX() - a.getX());
		double second = Math.atan2(c.getY() - b.getY(), c.getX() - b.getX());
		double difference = Math.abs(second - first);
		return difference > Math.PI ? 2 * Math.PI - difference : difference;
	}

	static String key(double value) {
		return String.format(Locale.ROOT, "%s", value);
	}

	static List<Object> list(Object... values) {
		List<Object> list = new ArrayList<>();
		for (Object value : values) {
			list.add(value);
		}
		return list;
	}

	// ---------------------------------------------------------------- deterministic JSON

	/** Deterministic JSON: insertion-ordered maps, LF line ends, two-space indentation. */
	static String json(Object value) {
		StringBuilder text = new StringBuilder();
		write(text, value, 0);
		return text.append('\n').toString();
	}

	private static void write(StringBuilder text, Object value, int depth) {
		if (value == null) {
			text.append("null");
		} else if (value instanceof Map<?, ?> map) {
			if (map.isEmpty()) {
				text.append("{}");
				return;
			}
			text.append("{\n");
			int index = 0;
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				indent(text, depth + 1);
				string(text, String.valueOf(entry.getKey()));
				text.append(": ");
				write(text, entry.getValue(), depth + 1);
				text.append(++index < map.size() ? ",\n" : "\n");
			}
			indent(text, depth);
			text.append('}');
		} else if (value instanceof List<?> list) {
			if (list.stream().allMatch(item -> !(item instanceof Map || item instanceof List))) {
				text.append('[');
				for (int i = 0; i < list.size(); i++) {
					write(text, list.get(i), depth);
					text.append(i + 1 < list.size() ? ", " : "");
				}
				text.append(']');
				return;
			}
			text.append("[\n");
			for (int i = 0; i < list.size(); i++) {
				indent(text, depth + 1);
				write(text, list.get(i), depth + 1);
				text.append(i + 1 < list.size() ? ",\n" : "\n");
			}
			indent(text, depth);
			text.append(']');
		} else if (value instanceof Double number) {
			text.append(Double.isFinite(number) ? Double.toString(number) : "null");
		} else if (value instanceof Number || value instanceof Boolean) {
			text.append(value);
		} else {
			string(text, value.toString());
		}
	}

	private static void indent(StringBuilder text, int depth) {
		text.append("  ".repeat(depth));
	}

	private static void string(StringBuilder text, String value) {
		text.append('"');
		for (char c : value.toCharArray()) {
			switch (c) {
			case '"' -> text.append("\\\"");
			case '\\' -> text.append("\\\\");
			case '\n' -> text.append("\\n");
			case '\r' -> text.append("\\r");
			case '\t' -> text.append("\\t");
			default -> {
				if (c < 0x20 || c > 0x7e) {
					text.append(String.format("\\u%04x", (int) c));
				} else {
					text.append(c);
				}
			}
			}
		}
		text.append('"');
	}
}
