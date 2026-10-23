/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionRootRevisionEvidence2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.PreG9BCu3EnclosureProbeAccess;
import org.geogebra.common.kernel.PathParameter;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.matrix.Coords;
import org.geogebra.common.main.error.ErrorHandler;
import org.geogebra.common.util.AsyncOperation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU3 preparation probe, run only through
 * {@code tools/benchmark/pre-g9b-cu/run-cu3-probe.ps1}. Read-only characterization of the
 * contacts of the author model and of synthetic cases (scenarios P2 and P4 to P9 of the
 * preparation). It is not part of any source set and assigns no verdict: every
 * classification it writes is evidence for the candidate design, not product behaviour.
 *
 * <p>Interval enclosures compose the class-v3 second-order enclosure of the traced point
 * with the residual of a captured target in outward-rounded binary64 interval arithmetic.
 * The binary64 coefficients of a target are taken as exact constants.
 */
@EnabledIfSystemProperty(named = "geocedg.cu0.harness", matches = "cu3-preparation")
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3PreparationProbe {

	private static final double[] DELTAS = {1e-2, 1e-4, 1e-6};

	@TempDir
	Path work;

	@Test
	void run() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.cu0.output"));
		Files.createDirectories(output);
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("schema_version", 1);
		root.put("id", "pre-g9b-cu3-preparation-characterization");
		root.put("status", "CU3 PREPARATION EVIDENCE - CANDIDATE - NO VERDICT");
		Map<String, Object> generator = new LinkedHashMap<>();
		generator.put("runner", "tools/benchmark/pre-g9b-cu/run-cu3-probe.ps1");
		generator.put("source_commit", System.getProperty("geocedg.cu0.commit"));
		generator.put("source_tree", System.getProperty("geocedg.cu0.tree"));
		generator.put("product_sources_modified_relative_to_commit",
				Boolean.getBoolean("geocedg.cu0.productDirty"));
		root.put("generator", generator);
		root.put("model", onEdt(() -> model(PreG9BCu0Corpus.open(
				PreG9BCu0Corpus.withoutE2(work)))));
		root.put("P6_synthetic", onEdt(PreG9BCu3PreparationProbe::synthetic));
		root.put("P7_scale", onEdt(PreG9BCu3PreparationProbe::scale));
		root.put("P8_axes", onEdt(PreG9BCu3PreparationProbe::axes));
		Files.writeString(output.resolve("cu3-preparation-characterization.json"),
				PreG9BCu0References.json(root), StandardCharsets.UTF_8);
	}

	// ---------------------------------------------------------------- author model

	private static Map<String, Object> model(AppGeoCeDG app) {
		Map<String, Object> model = new LinkedHashMap<>();
		model.put("P1_runtime_provenance", provenance(app));
		model.put("P2_correspondence_and_identities", identities(app));
		model.put("P2_enclosures", enclosures(app));
		model.put("current_contacts", PreG9BCu0References.contacts(app));
		model.put("P5_seam_root", seam(app));
		model.put("P5_seam_contact_at_E_1_eq_J", seamContact(app));
		model.put("P3_high_precision_ideal_identity", idealIdentity(app));
		model.put("P10_current_reader_of_stage_a_syntax", currentReader(app));
		model.put("P4_dynamics", dynamics(app));
		model.put("P9_view_independence", viewIndependence(app));
		return model;
	}

	/** Facts 1 and 2: provenance of K and identity of the driver support. */
	private static Map<String, Object> provenance(AppGeoCeDG app) {
		Map<String, Object> row = new LinkedHashMap<>();
		GeoPoint k = point(app, "K");
		AlgoElement algo = k.getParentAlgorithm();
		row.put("K_parent_algorithm", algo == null ? null : algo.getClass().getSimpleName());
		List<Object> inputs = new ArrayList<>();
		if (algo != null) {
			for (int i = 0; i < algo.getInputLength(); i++) {
				inputs.add(algo.getInput(i).getLabelSimple());
			}
			List<Object> outputs = new ArrayList<>();
			for (int i = 0; i < algo.getOutputLength(); i++) {
				outputs.add(algo.getOutput(i).getLabelSimple());
			}
			row.put("K_algorithm_outputs", outputs);
		}
		row.put("K_algorithm_inputs", inputs);
		List<Object> incidences = new ArrayList<>();
		if (k.getIncidenceList() != null) {
			for (GeoElement geo : k.getIncidenceList()) {
				incidences.add(geo.getLabelSimple());
			}
		}
		row.put("K_incidence_list", incidences);
		row.put("K_is_point_on_path", k.isPointOnPath());
		GeoPoint e1 = point(app, "E_1");
		GeoElement p = app.getKernel().lookupLabel("p");
		row.put("E_1_is_point_on_path", e1.isPointOnPath());
		row.put("E_1_path_is_object_p", e1.getPath() == p);
		row.put("p_persistent_id", persistentId(app, p));
		row.put("E_1_path_persistent_id", e1.getPath() instanceof GeoElement path
				? persistentId(app, path) : null);
		row.put("E_1_t_in_document", e1.getPathParameter().getT());
		return row;
	}

	/** Fact 3 (K to t(K)) and fact 4 (F(t(K)) and the incidence chain at E_1 = K). */
	private static Map<String, Object> identities(AppGeoCeDG app) {
		Map<String, Object> row = new LinkedHashMap<>();
		GeoConic p = (GeoConic) app.getKernel().lookupLabel("p");
		GeoPoint k = point(app, "K");
		row.put("K", xy(k));
		row.put("B", xy(point(app, "B")));
		Coords e0 = p.getEigenvec(0);
		Coords e1v = p.getEigenvec(1);
		row.put("p_eigenvectors", List.of(List.of(e0.getX(), e0.getY()),
				List.of(e1v.getX(), e1v.getY())));
		double projected = projectedParameter(p, k);
		row.put("t_K_route_a_canonical_projection", projected);
		row.put("t_K_route_a_is_exact_zero", projected == 0);
		double analytic = Math.atan2(
				(k.getInhomY() - p.getMidpoint2D().getY()) * e1v.getY()
						+ (k.getInhomX() - p.getMidpoint2D().getX()) * e1v.getX(),
				(k.getInhomX() - p.getMidpoint2D().getX()) * e0.getX()
						+ (k.getInhomY() - p.getMidpoint2D().getY()) * e0.getY());
		row.put("t_K_route_c_eigenframe_angle", analytic);
		row.put("K_y_minus_B_y", k.getInhomY() - point(app, "B").getInhomY());
		double saved = PreG9BCu0Corpus.pathParameter(app, "E_1");
		PreG9BCu0Corpus.movePathPoint(app, "E_1", projected);
		GeoPoint e1 = point(app, "E_1");
		row.put("E_1_after_move", xy(e1));
		row.put("E_1_equals_K_bitwise", same(e1, k));
		row.put("E_1_distance_to_K", distance(e1, k));
		Map<String, Object> chain = new LinkedHashMap<>();
		for (String label : List.of("F_1", "M", "H_1", "J", "I", "I_1", "G_1", "J_1", "M_1",
				"K_1")) {
			chain.put(label, xy(point(app, label)));
		}
		row.put("points_at_E_1_eq_K", chain);
		Map<String, Object> equal = new LinkedHashMap<>();
		equal.put("F_1==M", same(point(app, "F_1"), point(app, "M")));
		equal.put("H_1==K", same(point(app, "H_1"), k));
		equal.put("H_1==J", same(point(app, "H_1"), point(app, "J")));
		equal.put("I_1==M", same(point(app, "I_1"), point(app, "M")));
		equal.put("G_1==M_1", same(point(app, "G_1"), point(app, "M_1")));
		equal.put("J_1==M_1", same(point(app, "J_1"), point(app, "M_1")));
		equal.put("J_1.x==M_1.x", point(app, "J_1").getInhomX()
				== point(app, "M_1").getInhomX());
		row.put("bitwise_equalities", equal);
		Map<String, Object> distances = new LinkedHashMap<>();
		distances.put("|J_1-M_1|", distance(point(app, "J_1"), point(app, "M_1")));
		distances.put("|G_1-M_1|", distance(point(app, "G_1"), point(app, "M_1")));
		distances.put("J_1 signed distance to n", lineValue(line(app, "n"),
				point(app, "J_1")));
		distances.put("J_1 radial residual to c_1", circleValue(
				(GeoConic) app.getKernel().lookupLabel("c_1"), point(app, "J_1")));
		distances.put("K_1 signed distance to h_2", lineValue(line(app, "h_2"),
				point(app, "K_1")));
		row.put("residuals_at_E_1_eq_K", distances);
		row.put("e_1_component_containing_t_K", component(app, "e_1", projected));
		row.put("o_1_component_containing_t_K", component(app, "o_1", projected));
		PreG9BCu0Corpus.movePathPoint(app, "E_1", saved);
		return row;
	}

	private static Map<String, Object> enclosures(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		double center = projectedParameter((GeoConic) app.getKernel().lookupLabel("p"),
				point(app, "K"));
		for (Pair pair : pairs(app)) {
			Map<String, Object> byDelta = new LinkedHashMap<>();
			for (double delta : DELTAS) {
				byDelta.put(key(delta), critical(pair.access(app), pair.residual, center,
						delta));
			}
			rows.put(pair.name, byDelta);
		}
		return rows;
	}

	private static Map<String, Object> seam(AppGeoCeDG app) {
		Map<String, Object> row = new LinkedHashMap<>();
		GeoElement geo = app.getKernel().lookupLabel("cu0X2");
		if (!(geo instanceof GeoLocusIntersectionResult result)) {
			row.put("status", "cu0X2 not present");
			return row;
		}
		Pair pair = pairs(app).get(1);
		List<Object> roots = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : result.getIntersectionResult()
				.getFiniteSolutions()) {
			Map<String, Object> root = new LinkedHashMap<>();
			IntersectionRootRevisionEvidence2D evidence = solution.getRevisionEvidence();
			double t = evidence.getSemanticParameter();
			root.put("semantic_parameter", t);
			root.put("contact_class", solution.getClassification().getContactClass().name());
			root.put("domain_location", solution.getClassification().getDomainLocation()
					.name());
			root.put("local_isolation", String.valueOf(evidence.getLocalIsolationStatus()));
			root.put("admissible", result.isPointAdmissible(solution.getIdentity()
					.getRootToken()));
			Map<String, Object> certification = new LinkedHashMap<>();
			for (double width : new double[] {1e-6, 1e-9}) {
				certification.put(key(width), transverse(pair.access(app), pair.residual,
						t - width, t + width));
			}
			root.put("probe_transverse_certification", certification);
			roots.add(root);
		}
		row.put("roots", roots);
		return row;
	}

	/**
	 * P5: the second frontal generator. At the seam parameter the driver E_1 is J, whose
	 * generator is the other front contour generator r = Segment[H, L]; the same identity then
	 * places J_1 on c_1 and on r. Periodic enclosures of the residuals at t = pi.
	 */
	private static Map<String, Object> seamContact(AppGeoCeDG app) {
		Map<String, Object> row = new LinkedHashMap<>();
		GeoConic p = (GeoConic) app.getKernel().lookupLabel("p");
		GeoPoint j = point(app, "J");
		double tJ = projectedParameter(p, j);
		row.put("t_J_canonical_projection", tJ);
		GeoElement contour = PreG9BR6PlusF3Fixtures.q(app, "cu3M=Intersect(c_1,r)");
		row.put("c_1_cap_r_created", contour instanceof GeoPoint);
		double saved = PreG9BCu0Corpus.pathParameter(app, "E_1");
		PreG9BCu0Corpus.movePathPoint(app, "E_1", tJ);
		GeoPoint e1 = point(app, "E_1");
		row.put("E_1_equals_J_bitwise", same(e1, j));
		GeoPoint j1 = point(app, "J_1");
		row.put("J_1_at_J", xy(j1));
		if (contour instanceof GeoPoint m) {
			row.put("c_1_cap_r", xy(m));
			row.put("|J_1-(c_1 cap r)|", distance(j1, m));
			row.put("J_1==(c_1 cap r) bitwise", same(j1, m));
		}
		row.put("J_1 signed distance to r", lineValue(line(app, "r"), j1));
		row.put("J_1 radial residual to c_1", circleValue(
				(GeoConic) app.getKernel().lookupLabel("c_1"), j1));
		PreG9BCu0Corpus.movePathPoint(app, "E_1", saved);
		PreG9BR6PlusF3Fixtures.q(app, "cu3Xr=Intersect(e_1,r)");
		row.put("current_Intersect(e_1,r)", classes(app, "cu3Xr"));
		Map<String, Object> enclosures = new LinkedHashMap<>();
		List<Pair> seamPairs = List.of(pairs(app).get(1),
				new Pair("e_1|r", "e_1", lineResidualOf(line(app, "r"))));
		for (Pair pair : seamPairs) {
			Map<String, Object> byDelta = new LinkedHashMap<>();
			for (double delta : DELTAS) {
				byDelta.put(key(delta), critical(pair.access(app), pair.residual, Math.PI,
						delta, true));
			}
			enclosures.put(pair.name, byDelta);
		}
		row.put("periodic_enclosures_at_pi", enclosures);
		return row;
	}

	/**
	 * P3: the identity J_1(K) = M_1 in the ideal configuration of the captured binary64
	 * inputs (H, B, the height of h, rbCone and D_1, taken as exact), evaluated with 80
	 * significant digits through the mirror branch H_1 = 2 I - K that the document selects,
	 * and through the direct branch H_1 = K. Evidence, not a proof: the proof is the analytic
	 * derivation of the preparation; this checks it and the two output branches numerically.
	 */
	private static Map<String, Object> idealIdentity(AppGeoCeDG app) {
		java.math.MathContext mc = new java.math.MathContext(80);
		GeoPoint hp = point(app, "H");
		GeoPoint bp = point(app, "B");
		GeoPoint d1 = point(app, "D_1");
		GeoLine h = line(app, "h");
		java.math.BigDecimal hx = new java.math.BigDecimal(hp.getInhomX());
		java.math.BigDecimal hy = new java.math.BigDecimal(hp.getInhomY());
		java.math.BigDecimal bx = new java.math.BigDecimal(bp.getInhomX());
		java.math.BigDecimal by = new java.math.BigDecimal(bp.getInhomY());
		java.math.BigDecimal radius = new java.math.BigDecimal(number(app, "rbCone"));
		java.math.BigDecimal ay = new java.math.BigDecimal(-h.getZ() / h.getY());
		java.math.BigDecimal dx = new java.math.BigDecimal(d1.getInhomX()).subtract(hx);
		java.math.BigDecimal dy = new java.math.BigDecimal(d1.getInhomY()).subtract(hy);
		java.math.BigDecimal r = dx.multiply(dx).add(dy.multiply(dy)).sqrt(mc);
		java.math.BigDecimal kx = bx.add(radius);
		java.math.BigDecimal ix = hx;
		java.math.BigDecimal[] m1 = towards(hx, hy, kx, ay, r, mc);
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("h_line_height_binary64", ay.doubleValue());
		row.put("is_h_horizontal_in_binary64", h.getX() == 0);
		row.put("B_y_equals_K_y", by.compareTo(new java.math.BigDecimal(
				point(app, "K").getInhomY())) == 0);
		for (boolean mirror : new boolean[] {true, false}) {
			java.math.BigDecimal h1x = mirror ? ix.add(ix).subtract(kx) : kx;
			java.math.BigDecimal[] g1 = towards(hx, hy, h1x, ay, r, mc);
			java.math.BigDecimal ratio = g1[1].subtract(hy).divide(ay.subtract(hy), mc);
			java.math.BigDecimal j1x = hx.add(ratio.multiply(kx.subtract(hx), mc), mc);
			java.math.BigDecimal j1y = g1[1];
			java.math.BigDecimal ex = j1x.subtract(m1[0]);
			java.math.BigDecimal ey = j1y.subtract(m1[1]);
			java.math.BigDecimal gap = ex.multiply(ex).add(ey.multiply(ey)).sqrt(mc);
			row.put(mirror ? "branch_H_1=2I-K" : "branch_H_1=K",
					gap.round(new java.math.MathContext(6)).toString());
		}
		row.put("digits", 80);
		return row;
	}

	/** Point at distance r from (x0, y0) towards (x1, y1). */
	private static java.math.BigDecimal[] towards(java.math.BigDecimal x0,
			java.math.BigDecimal y0, java.math.BigDecimal x1, java.math.BigDecimal y1,
			java.math.BigDecimal r, java.math.MathContext mc) {
		java.math.BigDecimal ux = x1.subtract(x0);
		java.math.BigDecimal uy = y1.subtract(y0);
		java.math.BigDecimal norm = ux.multiply(ux).add(uy.multiply(uy)).sqrt(mc);
		return new java.math.BigDecimal[] {x0.add(ux.multiply(r).divide(norm, mc), mc),
			y0.add(uy.multiply(r).divide(norm, mc), mc)};
	}

	/**
	 * P10: what the current product (a reader older than any CU3 contract) does with the
	 * candidate Stage A syntax. Each command runs once in the working copy and is removed.
	 */
	private static Map<String, Object> currentReader(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		for (String command : new String[] {"cu3P=Point(e_1,K)", "cu3I=Intersect(e_1,n,K)",
				"cu3O=Intersect(o_1,h_2,K)", "cu3C=Intersect(e_1,c_1,K)"}) {
			Map<String, Object> row = new LinkedHashMap<>();
			List<String> messages = new ArrayList<>();
			try {
				GeoElementND[] result = app.getKernel().getAlgebraProcessor()
						.processAlgebraCommandNoExceptionHandling(command, false,
								recorder(messages), false, null);
				boolean created = result != null && result.length > 0;
				row.put("created", created);
				if (created) {
					GeoElement geo = result[0].toGeoElement();
					row.put("class", geo.getClass().getSimpleName());
					row.put("parent_algorithm", geo.getParentAlgorithm() == null ? null
							: geo.getParentAlgorithm().getClass().getSimpleName());
					row.put("defined", geo.isDefined());
					if (geo instanceof GeoPoint point) {
						row.put("point", xy(point));
					}
					geo.remove();
				}
			} catch (Throwable failure) {
				row.put("created", false);
				row.put("exception", failure.getClass().getName());
			}
			row.put("error_messages", messages);
			rows.put(command, row);
		}
		return rows;
	}

	/** P4: dynamic sweeps of the four model parameters, restored after each change. */
	private static Map<String, Object> dynamics(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		Map<String, Object> rb = new LinkedHashMap<>();
		double rbCone = number(app, "rbCone");
		for (double value : new double[] {3, 4, 5, 6, 7, 7.5}) {
			PreG9BCu0Corpus.setNumber(app, "rbCone", value);
			rb.put(key(value), snapshot(app));
		}
		PreG9BCu0Corpus.setNumber(app, "rbCone", rbCone);
		rows.put("rbCone", rb);
		Map<String, Object> alpha = new LinkedHashMap<>();
		double angle = number(app, "\u03b1_{cone}");
		for (double degrees : new double[] {35, 47, 60}) {
			PreG9BCu0Corpus.setNumber(app, "\u03b1_{cone}", Math.toRadians(degrees));
			alpha.put(key(degrees), snapshot(app));
		}
		PreG9BCu0Corpus.setNumber(app, "\u03b1_{cone}", angle);
		rows.put("alpha_cone_degrees", alpha);
		Map<String, Object> sphere = new LinkedHashMap<>();
		GeoPoint d1 = point(app, "D_1");
		GeoPoint h = point(app, "H");
		double dx = d1.getInhomX();
		double dy = d1.getInhomY();
		for (double factor : new double[] {0.5, 1.0, 1.3, 3.0}) {
			d1.setCoords(h.getInhomX() + factor * (dx - h.getInhomX()),
					h.getInhomY() + factor * (dy - h.getInhomY()), 1);
			d1.updateRepaint();
			sphere.put("radius_factor_" + key(factor), snapshot(app));
		}
		d1.setCoords(dx, dy, 1);
		d1.updateRepaint();
		rows.put("D_1", sphere);
		Map<String, Object> cylinder = new LinkedHashMap<>();
		double exc = number(app, "excCyl");
		for (double value : new double[] {2.9, 3.7}) {
			PreG9BCu0Corpus.setNumber(app, "excCyl", value);
			cylinder.put(key(value), snapshot(app));
		}
		PreG9BCu0Corpus.setNumber(app, "excCyl", exc);
		rows.put("excCyl", cylinder);
		return rows;
	}

	private static Map<String, Object> snapshot(AppGeoCeDG app) {
		Map<String, Object> row = new LinkedHashMap<>();
		GeoPoint k = point(app, "K");
		GeoPoint m1 = point(app, "M_1");
		row.put("K_defined", k.isDefined());
		row.put("M_1_defined", m1.isDefined());
		if (!k.isDefined()) {
			return row;
		}
		GeoConic p = (GeoConic) app.getKernel().lookupLabel("p");
		double t = projectedParameter(p, k);
		row.put("t_K", t);
		double saved = PreG9BCu0Corpus.pathParameter(app, "E_1");
		PreG9BCu0Corpus.movePathPoint(app, "E_1", t);
		GeoPoint j1 = point(app, "J_1");
		row.put("J_1_defined_at_K", j1.isDefined());
		if (j1.isDefined() && m1.isDefined()) {
			row.put("|J_1-M_1|_at_K", distance(j1, m1));
			row.put("K_1_to_h_2_at_K", lineValue(line(app, "h_2"), point(app, "K_1")));
		}
		row.put("e_1_component_at_t_K", component(app, "e_1", t));
		PreG9BCu0Corpus.movePathPoint(app, "E_1", saved);
		if (m1.isDefined()) {
			Map<String, Object> verdicts = new LinkedHashMap<>();
			for (Pair pair : pairs(app)) {
				Map<String, Object> critical = critical(pair.access(app), pair.residual, t, 1e-4);
				verdicts.put(pair.name, critical.get("taxonomy_class"));
			}
			row.put("probe_taxonomy_delta_1e-4", verdicts);
		}
		Map<String, Object> current = new LinkedHashMap<>();
		current.put("Intersect(o_1,h_2)", classes(app, "u_1"));
		current.put("Intersect(e_1,n)", classes(app, "cu0X1"));
		current.put("Intersect(e_1,c_1)", classes(app, "cu0X2"));
		row.put("current_classification", current);
		return row;
	}

	private static Map<String, Object> viewIndependence(AppGeoCeDG app) {
		Map<String, Object> row = new LinkedHashMap<>();
		Pair pair = pairs(app).get(0);
		double center = projectedParameter((GeoConic) app.getKernel().lookupLabel("p"),
				point(app, "K"));
		String before = PreG9BCu0References.json(critical(pair.access(app), pair.residual,
				center, 1e-4));
		String classesBefore = PreG9BCu0References.json(classes(app, "cu0X1"));
		org.geogebra.common.euclidian.EuclidianView view = app.getActiveEuclidianView();
		double xZero = view.getXZero();
		double yZero = view.getYZero();
		double xScale = view.getXscale();
		double yScale = view.getYscale();
		view.setCoordSystem(xZero * 0.25, yZero * 3, xScale * 37.5, yScale * 37.5);
		String after = PreG9BCu0References.json(critical(pair.access(app), pair.residual,
				center, 1e-4));
		String classesAfter = PreG9BCu0References.json(classes(app, "cu0X1"));
		view.setCoordSystem(xZero, yZero, xScale, yScale);
		row.put("zoom_factor", 37.5);
		row.put("enclosure_identical", before.equals(after));
		row.put("classification_identical", classesBefore.equals(classesAfter));
		return row;
	}

	// ---------------------------------------------------------------- synthetic (P6, P7, P8)

	private static Map<String, Object> synthetic() {
		Map<String, Object> rows = new LinkedHashMap<>();
		double[] offsets = {1e-2, 1e-6, 1e-10, 1e-13, 0, -1e-13, -1e-10, -1e-6, -1e-2};
		Map<String, Object> lineRows = new LinkedHashMap<>();
		for (double delta : offsets) {
			AppGeoCeDG app = circleLocus(0, 1);
			PreG9BR6PlusF3Fixtures.must(app, "g=Line((0," + (1.5 + delta) + "),(1,"
					+ (1.5 + delta) + "))");
			lineRows.put(key(delta), syntheticCase(app, "g", lineResidualOf(line(app, "g")),
					Math.PI / 2));
		}
		rows.put("circle_r1.5_vs_line_y=1.5+delta", lineRows);
		Map<String, Object> circleRows = new LinkedHashMap<>();
		for (double delta : offsets) {
			AppGeoCeDG app = circleLocus(0, 1);
			PreG9BR6PlusF3Fixtures.must(app, "k=Circle((" + (3 + delta) + ",0),1.5)");
			circleRows.put(key(delta), syntheticCase(app, "k", circleResidualOf(
					(GeoConic) app.getKernel().lookupLabel("k")), 0));
		}
		rows.put("circle_r1.5_vs_circle_centre_3+delta_r1.5", circleRows);
		AppGeoCeDG endpoint = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
		for (String command : new String[] {"s=Segment((-1,0),(1,0))", "P=Point(s)",
				"Q=(x(P),(x(P)+1)^2)", "L=LocusV2(Q,P)", "g=Line((0,0),(1,0))"}) {
			PreG9BR6PlusF3Fixtures.must(endpoint, command);
		}
		Map<String, Object> end = new LinkedHashMap<>();
		end.put("current", intersect(endpoint, "g"));
		PreG9BCu3EnclosureProbeAccess access = PreG9BCu3EnclosureProbeAccess.capture(
				definition(endpoint, "L"));
		end.put("class_v3_captured", access != null);
		if (access != null) {
			end.put("one_sided_box_[0,1e-3]", jetSummary(access, lineResidualOf(
					line(endpoint, "g")), 0, 1e-3));
		}
		rows.put("parabola_tangent_at_included_endpoint", end);
		AppGeoCeDG arc = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
		for (String command : new String[] {"a=CircularArc((0,0),(1,0),(0,1))", "P=Point(a)",
				"Q=Dilate(P,1,(0,0))", "L=LocusV2(Q,P)", "g=Line((1,0),(1,1))"}) {
			PreG9BR6PlusF3Fixtures.must(arc, command);
		}
		Map<String, Object> arcRow = new LinkedHashMap<>();
		arcRow.put("current", intersect(arc, "g"));
		PreG9BCu3EnclosureProbeAccess arcAccess = PreG9BCu3EnclosureProbeAccess.capture(
				definition(arc, "L"));
		arcRow.put("class_v3_captured", arcAccess != null);
		if (arcAccess != null) {
			arcRow.put("one_sided_box_[0,1e-3]", jetSummary(arcAccess, lineResidualOf(
					line(arc, "g")), 0, 1e-3));
		}
		rows.put("quarter_arc_tangent_to_x=1_at_included_start", arcRow);
		return rows;
	}

	private static AppGeoCeDG circleLocus(double translation, double radius) {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
		for (String command : new String[] {"O=(" + translation + "," + translation + ")",
				"c=Circle(O," + radius + ")", "P=Point(c)", "Q=Dilate(P,1.5,O)",
				"L=LocusV2(Q,P)"}) {
			PreG9BR6PlusF3Fixtures.must(app, command);
		}
		return app;
	}

	private static Map<String, Object> syntheticCase(AppGeoCeDG app, String target,
			Residual residual, double center) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("current", intersect(app, target));
		PreG9BCu3EnclosureProbeAccess access = PreG9BCu3EnclosureProbeAccess.capture(
				definition(app, "L"));
		row.put("probe_delta_1e-3", access == null ? "CLASS_V3_NOT_CAPTURED"
				: critical(access, residual, center, 1e-3));
		return row;
	}

	private static Map<String, Object> intersect(AppGeoCeDG app, String target) {
		GeoElement geo = PreG9BR6PlusF3Fixtures.q(app, "R=Intersect(L," + target + ")");
		if (!(geo instanceof GeoLocusIntersectionResult)) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("created", false);
			return row;
		}
		return classes(app, "R");
	}

	/** P7: H02 scale and translation sweep with the probe's own certified existence. */
	private static Map<String, Object> scale() {
		Map<String, Object> rows = new LinkedHashMap<>();
		for (double translation : new double[] {0, 1e3, 1e6}) {
			Map<String, Object> byRadius = new LinkedHashMap<>();
			for (double r : new double[] {2, 20, 200, 2000, 20000}) {
				AppGeoCeDG app = circleLocus(translation, r);
				double y = translation + 0.5 * r;
				PreG9BR6PlusF3Fixtures.must(app, "g=Line((" + translation + "," + y + "),("
						+ (translation + 1) + "," + y + "))");
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("current", intersect(app, "g"));
				PreG9BCu3EnclosureProbeAccess access = PreG9BCu3EnclosureProbeAccess.capture(
						definition(app, "L"));
				Residual residual = lineResidualOf(line(app, "g"));
				double characteristic = 3 * r;
				row.put("candidate_characteristic_scale", characteristic);
				List<Object> roots = new ArrayList<>();
				for (double theta : new double[] {Math.asin(1.0 / 3),
						Math.PI - Math.asin(1.0 / 3)}) {
					Map<String, Object> root = new LinkedHashMap<>();
					root.put("analytic_parameter", theta);
					if (access != null) {
						root.put("probe_transverse_1e-7", transverse(access, residual,
								theta - 1e-7, theta + 1e-7));
					}
					double value = residualAtPoint(app, residual, theta);
					root.put("binary64_residual_at_analytic_parameter", value);
					root.put("normalized_residual", Math.abs(value) / characteristic);
					roots.add(root);
				}
				row.put("roots", roots);
				byRadius.put(key(r), row);
			}
			rows.put("translation_" + key(translation), byRadius);
		}
		return rows;
	}

	private static Map<String, Object> axes() {
		Map<String, Object> rows = new LinkedHashMap<>();
		for (String target : new String[] {"xAxis", "yAxis", "y=0"}) {
			AppGeoCeDG app = circleLocus(0, 2);
			List<String> messages = new ArrayList<>();
			Map<String, Object> row = new LinkedHashMap<>();
			try {
				GeoElementND[] result = app.getKernel().getAlgebraProcessor()
						.processAlgebraCommandNoExceptionHandling("X=Intersect(L," + target
								+ ")", false, recorder(messages), false, null);
				row.put("created", result != null && result.length > 0);
			} catch (Throwable failure) {
				row.put("created", false);
				row.put("exception", failure.getClass().getName() + ": " + failure.getMessage());
				List<Object> frames = new ArrayList<>();
				StackTraceElement[] trace = failure.getStackTrace();
				for (int i = 0; i < Math.min(14, trace.length); i++) {
					frames.add(trace[i].toString());
				}
				row.put("frames", frames);
				Throwable cause = failure.getCause();
				if (cause != null) {
					row.put("cause", cause.getClass().getName() + ": " + cause.getMessage());
					List<Object> causeFrames = new ArrayList<>();
					StackTraceElement[] causeTrace = cause.getStackTrace();
					for (int i = 0; i < Math.min(14, causeTrace.length); i++) {
						causeFrames.add(causeTrace[i].toString());
					}
					row.put("cause_frames", causeFrames);
				}
			}
			row.put("error_messages", messages);
			if (!target.contains("=")) {
				row.put("direct_public_operation", direct(app, target));
			}
			rows.put(target, row);
		}
		return rows;
	}

	/** Calls the public V2 operation directly, outside the algebra processor's handler. */
	private static Map<String, Object> direct(AppGeoCeDG app, String axis) {
		Map<String, Object> row = new LinkedHashMap<>();
		GeoElement target = app.getKernel().lookupLabel(axis);
		row.put("target_class", target == null ? null : target.getClass().getName());
		row.put("target_is_geo_line", target != null && target.isGeoLine());
		row.put("target_label_set", target != null && target.isLabelSet());
		try {
			GeoElement result = org.geocedg.common.kernel.locus.LocusV2PublicOperations
					.intersect(app.getKernel().getConstruction(), "Xd",
							PreG9BCu0Corpus.locus(app, "L"), target);
			row.put("created", result != null);
		} catch (Throwable failure) {
			row.put("created", false);
			Throwable root = failure;
			List<Object> chain = new ArrayList<>();
			while (root != null) {
				// identities generated for the session are random: normalize them
				chain.add(root.getClass().getName() + ": " + String.valueOf(root.getMessage())
						.replaceAll("geo:[0-9a-f]{32}", "geo:<session-id>"));
				if (root.getCause() == null) {
					break;
				}
				root = root.getCause();
			}
			row.put("exception_chain", chain);
			List<Object> frames = new ArrayList<>();
			StackTraceElement[] trace = root.getStackTrace();
			for (int i = 0; i < Math.min(16, trace.length); i++) {
				frames.add(trace[i].toString());
			}
			row.put("root_frames", frames);
		}
		return row;
	}

	private static ErrorHandler recorder(List<String> messages) {
		return new ErrorHandler() {
			@Override
			public void showError(String msg) {
				messages.add("error: " + msg);
			}

			@Override
			public void showCommandError(String command, String message) {
				messages.add("command error: " + command + ": " + message);
			}

			@Override
			public String getCurrentCommand() {
				return null;
			}

			@Override
			public boolean onUndefinedVariables(String string,
					AsyncOperation<String[]> callback) {
				messages.add("undefined variables: " + string);
				return false;
			}

			@Override
			public void resetError() {
				// nothing recorded
			}
		};
	}

	// ---------------------------------------------------------------- interval certification

	/**
	 * Certified critical point of the residual on {@code [c - delta, c + delta]}: sign of
	 * h'' on the box, opposite signs of h' at its ends (unique simple root t* of h'), a
	 * bisection bracket of t* and the enclosure of h on that bracket. Only open conditions
	 * are certified; the classification is the candidate taxonomy of the preparation.
	 */
	static Map<String, Object> critical(PreG9BCu3EnclosureProbeAccess access,
			Residual residual, double center, double delta) {
		return critical(access, residual, center, delta, false);
	}

	static Map<String, Object> critical(PreG9BCu3EnclosureProbeAccess access,
			Residual residual, double center, double delta, boolean periodic) {
		Map<String, Object> row = new LinkedHashMap<>();
		if (access == null) {
			row.put("taxonomy_class", "5_NOT_ESTABLISHED");
			row.put("reason", "CLASS_V3_NOT_CAPTURED");
			return row;
		}
		double a = center - delta;
		double b = center + delta;
		Jet box = residual.of(access.traced(a, b, periodic));
		Jet left = residual.of(access.traced(a, a, periodic));
		Jet right = residual.of(access.traced(b, b, periodic));
		if (box == null || left == null || right == null) {
			row.put("taxonomy_class", "5_NOT_ESTABLISHED");
			row.put("reason", "ENCLOSURE_NOT_AVAILABLE");
			return row;
		}
		row.put("h2_on_box", box.h2.list());
		row.put("h1_at_ends", List.of(left.h1.list(), right.h1.list()));
		row.put("h_at_ends", List.of(left.h0.list(), right.h0.list()));
		if (box.h2.hasZero()) {
			row.put("taxonomy_class", "5_NOT_ESTABLISHED");
			row.put("reason", "SECOND_DERIVATIVE_SIGN_NOT_CERTIFIED");
			return row;
		}
		int curvature = box.h2.lo > 0 ? 1 : -1;
		if (left.h1.hasZero() || right.h1.hasZero()
				|| Math.signum(left.h1.lo) == Math.signum(right.h1.lo)) {
			row.put("taxonomy_class", "5_NOT_ESTABLISHED");
			row.put("reason", "NO_CERTIFIED_SIGN_CHANGE_OF_H1");
			return row;
		}
		double leftSign = Math.signum(left.h1.lo);
		double lo = a;
		double hi = b;
		int steps = 0;
		double ambiguous = Double.NaN;
		while (steps < 200) {
			double m = lo + (hi - lo) / 2;
			if (m <= lo || m >= hi) {
				break;
			}
			Jet middle = residual.of(access.traced(m, m, periodic));
			if (middle == null || middle.h1.hasZero()) {
				ambiguous = m;
				break;
			}
			if (Math.signum(middle.h1.lo) == leftSign) {
				lo = m;
			} else {
				hi = m;
			}
			steps++;
		}
		int tightened = 0;
		if (!Double.isNaN(ambiguous)) {
			// h' is not decided at the midpoint: bracket the critical point by the tightest
			// symmetric pair of certified opposite signs around it.
			for (double width : new double[] {1e-8, 1e-10, 1e-12, 1e-14}) {
				double l = ambiguous - width;
				double u = ambiguous + width;
				if (l <= lo || u >= hi) {
					continue;
				}
				Jet ll = residual.of(access.traced(l, l, periodic));
				Jet uu = residual.of(access.traced(u, u, periodic));
				if (ll == null || uu == null || ll.h1.hasZero() || uu.h1.hasZero()
						|| Math.signum(ll.h1.lo) != leftSign
						|| Math.signum(uu.h1.lo) == leftSign) {
					break;
				}
				lo = l;
				hi = u;
				tightened++;
			}
		}
		Jet bracket = residual.of(access.traced(lo, hi, periodic));
		row.put("critical_point_bracket", List.of(lo, hi));
		row.put("bracket_width", hi - lo);
		row.put("bisection_steps", steps);
		row.put("symmetric_tightenings", tightened);
		if (bracket == null) {
			row.put("taxonomy_class", "5_NOT_ESTABLISHED");
			row.put("reason", "BRACKET_ENCLOSURE_NOT_AVAILABLE");
			return row;
		}
		Interval value = bracket.h0;
		row.put("h_at_critical_point_enclosure", value.list());
		row.put("h_enclosure_width", value.hi - value.lo);
		if (value.hasZero()) {
			row.put("taxonomy_class", "3_NUMERICALLY_COMPATIBLE_CONTACT");
			row.put("reason", "CRITICAL_POINT_CERTIFIED_H_AT_T_STAR_SIGN_UNDECIDED");
			return row;
		}
		int sign = value.lo > 0 ? 1 : -1;
		if (sign == curvature) {
			row.put("taxonomy_class", "4_CRITICAL_POINT_CERTIFIED_NO_INTERSECTION_ON_BOX");
			return row;
		}
		boolean ends = !left.h0.hasZero() && !right.h0.hasZero()
				&& Math.signum(left.h0.lo) == curvature && Math.signum(right.h0.lo) == curvature;
		row.put("taxonomy_class", ends
				? "4_CRITICAL_POINT_CERTIFIED_TWO_TRANSVERSE_ROOTS_ON_BOX"
				: "5_NOT_ESTABLISHED");
		if (!ends) {
			row.put("reason", "END_SIGNS_NOT_CERTIFIED");
		}
		return row;
	}

	/** Certified simple transverse root on {@code [a, b]}: opposite end signs and h' != 0. */
	static Map<String, Object> transverse(PreG9BCu3EnclosureProbeAccess access,
			Residual residual, double a, double b) {
		Map<String, Object> row = new LinkedHashMap<>();
		Jet box = residual.of(access.traced(a, b, false));
		Jet left = residual.of(access.traced(a, a, false));
		Jet right = residual.of(access.traced(b, b, false));
		if (box == null || left == null || right == null) {
			row.put("certified", false);
			row.put("reason", "ENCLOSURE_NOT_AVAILABLE");
			return row;
		}
		boolean signChange = !left.h0.hasZero() && !right.h0.hasZero()
				&& Math.signum(left.h0.lo) != Math.signum(right.h0.lo);
		row.put("h_at_ends", List.of(left.h0.list(), right.h0.list()));
		row.put("h1_on_box", box.h1.list());
		row.put("existence_by_certified_sign_change", signChange);
		row.put("uniqueness_by_h1_excluding_zero", !box.h1.hasZero());
		row.put("certified", signChange && !box.h1.hasZero());
		return row;
	}

	private static Map<String, Object> jetSummary(PreG9BCu3EnclosureProbeAccess access,
			Residual residual, double a, double b) {
		Map<String, Object> row = new LinkedHashMap<>();
		Jet box = residual.of(access.traced(a, b, false));
		Jet end = residual.of(access.traced(a, a, false));
		if (box == null || end == null) {
			row.put("status", "ENCLOSURE_NOT_AVAILABLE");
			return row;
		}
		row.put("h_at_endpoint", end.h0.list());
		row.put("h1_at_endpoint", end.h1.list());
		row.put("h1_on_box", box.h1.list());
		row.put("h2_on_box", box.h2.list());
		return row;
	}

	// ---------------------------------------------------------------- residuals

	interface Residual {
		/** @param jet {x, y, x', y', x'', y''} or null */
		Jet of(double[][] jet);
	}

	record Jet(Interval h0, Interval h1, Interval h2) {
	}

	private record Pair(String name, String locus, Residual residual) {
		PreG9BCu3EnclosureProbeAccess access(AppGeoCeDG app) {
			return PreG9BCu3EnclosureProbeAccess.capture(definition(app, locus));
		}
	}

	private static List<Pair> pairs(AppGeoCeDG app) {
		return List.of(new Pair("e_1|n", "e_1", lineResidualOf(line(app, "n"))),
				new Pair("e_1|c_1", "e_1", circleResidualOf(
						(GeoConic) app.getKernel().lookupLabel("c_1"))),
				new Pair("o_1|h_2", "o_1", lineResidualOf(line(app, "h_2"))));
	}

	static Residual lineResidualOf(GeoLine line) {
		double norm = Math.hypot(line.getX(), line.getY());
		Interval n = Interval.of(norm);
		Interval a = Interval.of(line.getX());
		Interval b = Interval.of(line.getY());
		Interval c = Interval.of(line.getZ());
		return jet -> {
			if (jet == null) {
				return null;
			}
			try {
				Interval x = Interval.of(jet[0]);
				Interval y = Interval.of(jet[1]);
				Interval h0 = a.mul(x).add(b.mul(y)).add(c).div(n);
				Interval h1 = a.mul(Interval.of(jet[2])).add(b.mul(Interval.of(jet[3]))).div(n);
				Interval h2 = a.mul(Interval.of(jet[4])).add(b.mul(Interval.of(jet[5]))).div(n);
				return new Jet(h0, h1, h2);
			} catch (ArithmeticException failure) {
				return null;
			}
		};
	}

	static Residual circleResidualOf(GeoConic conic) {
		Interval cx = Interval.of(conic.getMidpoint2D().getX());
		Interval cy = Interval.of(conic.getMidpoint2D().getY());
		Interval r = Interval.of(conic.getHalfAxis(0));
		return jet -> {
			if (jet == null) {
				return null;
			}
			try {
				Interval dx = Interval.of(jet[0]).sub(cx);
				Interval dy = Interval.of(jet[1]).sub(cy);
				Interval x1 = Interval.of(jet[2]);
				Interval y1 = Interval.of(jet[3]);
				Interval rho = dx.mul(dx).add(dy.mul(dy)).sqrt();
				Interval s = dx.mul(x1).add(dy.mul(y1));
				Interval h0 = rho.sub(r);
				Interval h1 = s.div(rho);
				Interval h2 = x1.mul(x1).add(y1.mul(y1)).add(dx.mul(Interval.of(jet[4])))
						.add(dy.mul(Interval.of(jet[5]))).div(rho)
						.sub(s.mul(s).div(rho.mul(rho).mul(rho)));
				return new Jet(h0, h1, h2);
			} catch (ArithmeticException failure) {
				return null;
			}
		};
	}

	/** Outward-rounded closed binary64 interval. */
	record Interval(double lo, double hi) {
		static Interval of(double value) {
			return new Interval(value, value);
		}

		static Interval of(double[] pair) {
			return new Interval(pair[0], pair[1]);
		}

		private static Interval out(double low, double high) {
			if (!Double.isFinite(low) || !Double.isFinite(high)) {
				throw new ArithmeticException("nonfinite bound");
			}
			return new Interval(Math.nextDown(low), Math.nextUp(high));
		}

		Interval add(Interval o) {
			return out(lo + o.lo, hi + o.hi);
		}

		Interval sub(Interval o) {
			return out(lo - o.hi, hi - o.lo);
		}

		Interval mul(Interval o) {
			double p1 = lo * o.lo;
			double p2 = lo * o.hi;
			double p3 = hi * o.lo;
			double p4 = hi * o.hi;
			return out(Math.min(Math.min(p1, p2), Math.min(p3, p4)),
					Math.max(Math.max(p1, p2), Math.max(p3, p4)));
		}

		Interval div(Interval o) {
			if (o.hasZero()) {
				throw new ArithmeticException("division through zero");
			}
			return mul(out(1 / o.hi, 1 / o.lo));
		}

		Interval sqrt() {
			if (hi < 0) {
				throw new ArithmeticException("negative root");
			}
			return out(Math.sqrt(Math.max(0, lo)), Math.sqrt(hi));
		}

		boolean hasZero() {
			return lo <= 0 && hi >= 0;
		}

		List<Object> list() {
			return Arrays.asList(lo, hi);
		}
	}

	// ---------------------------------------------------------------- helpers

	private static Map<String, Object> classes(AppGeoCeDG app, String label) {
		Map<String, Object> row = new LinkedHashMap<>();
		GeoElement geo = app.getKernel().lookupLabel(label);
		if (!(geo instanceof GeoLocusIntersectionResult result)) {
			row.put("created", false);
			return row;
		}
		row.put("created", true);
		List<Object> roots = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : result.getIntersectionResult()
				.getFiniteSolutions()) {
			Map<String, Object> root = new LinkedHashMap<>();
			root.put("parameter", solution.getRevisionEvidence().getSemanticParameter());
			root.put("contact_class", solution.getClassification().getContactClass().name());
			root.put("domain_location", solution.getClassification().getDomainLocation()
					.name());
			root.put("admissible", result.isPointAdmissible(solution.getIdentity()
					.getRootToken()));
			roots.add(root);
		}
		row.put("solutions", roots);
		return row;
	}

	private static String component(AppGeoCeDG app, String label, double t) {
		LocusDefinition2D definition = definition(app, label);
		if (definition == null) {
			return "NO_DEFINITION";
		}
		LocusBranch2D branch = definition.getBranch(PreG9BCu0Corpus.MAIN);
		LocusExistenceStructure2D structure = branch == null ? null
				: branch.getExistenceStructure();
		if (structure == null) {
			return "NO_STRUCTURE";
		}
		LocusExistenceStructure2D.ContinuousComponent component = structure
				.getComponentContaining(t);
		return (component == null ? "NOT_IN_A_CERTIFIED_CONTINUOUS_COMPONENT"
				: "IN_CERTIFIED_CONTINUOUS_COMPONENT") + "; completeness="
				+ structure.getCompleteness();
	}

	private static LocusDefinition2D definition(AppGeoCeDG app, String label) {
		GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
		return locus.getSemanticDefinition();
	}

	private static double residualAtPoint(AppGeoCeDG app, Residual residual, double t) {
		org.geocedg.common.kernel.locus.LocusPoint2D point;
		try (org.geocedg.common.kernel.locus.LocusEvaluationSession2D session =
				org.geocedg.common.kernel.locus.LocusEvaluationSession2D.memoizing(4)) {
			point = PreG9BCu0Corpus.point(PreG9BCu0Corpus.locus(app, "L"), t, session);
		}
		if (point == null) {
			return Double.NaN;
		}
		Jet jet = residual.of(new double[][] {{point.getX(), point.getX()},
			{point.getY(), point.getY()}, {0, 0}, {0, 0}, {0, 0}, {0, 0}});
		return jet == null ? Double.NaN : (jet.h0.lo + jet.h0.hi) / 2;
	}

	private static double projectedParameter(GeoConic conic, GeoPoint point) {
		PathParameter parameter = new PathParameter();
		conic.pointChanged(new Coords(point.getInhomX(), point.getInhomY(), 1), parameter);
		return parameter.getT();
	}

	private static String persistentId(AppGeoCeDG app, GeoElement geo) {
		Object id = app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoIdForSerialization(geo);
		return id == null ? null : id.toString();
	}

	private static GeoPoint point(AppGeoCeDG app, String label) {
		return (GeoPoint) app.getKernel().lookupLabel(label);
	}

	private static GeoLine line(AppGeoCeDG app, String label) {
		return (GeoLine) app.getKernel().lookupLabel(label);
	}

	private static double number(AppGeoCeDG app, String label) {
		return ((GeoNumeric) app.getKernel().lookupLabel(label)).getValue();
	}

	private static List<Object> xy(GeoPoint point) {
		return point.isDefined() ? Arrays.asList(point.getInhomX(), point.getInhomY())
				: Arrays.asList(null, null);
	}

	private static boolean same(GeoPoint a, GeoPoint b) {
		return Double.doubleToLongBits(a.getInhomX()) == Double.doubleToLongBits(b.getInhomX())
				&& Double.doubleToLongBits(a.getInhomY())
						== Double.doubleToLongBits(b.getInhomY());
	}

	private static double distance(GeoPoint a, GeoPoint b) {
		return Math.hypot(a.getInhomX() - b.getInhomX(), a.getInhomY() - b.getInhomY());
	}

	private static double lineValue(GeoLine line, GeoPoint point) {
		return (line.getX() * point.getInhomX() + line.getY() * point.getInhomY()
				+ line.getZ()) / Math.hypot(line.getX(), line.getY());
	}

	private static double circleValue(GeoConic conic, GeoPoint point) {
		return Math.hypot(point.getInhomX() - conic.getMidpoint2D().getX(),
				point.getInhomY() - conic.getMidpoint2D().getY()) - conic.getHalfAxis(0);
	}

	private static String key(double value) {
		return Double.toString(value);
	}

	private static <T> T onEdt(Callable<T> action) throws Exception {
		return PreG9BR6PlusF3Fixtures.onEdt(action);
	}
}
