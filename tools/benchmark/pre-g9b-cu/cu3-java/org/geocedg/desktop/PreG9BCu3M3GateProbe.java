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

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.OperandKind;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D;
import org.geocedg.common.kernel.locus.intersection.PreG9BCu3EnclosureProbeAccess;
import org.geocedg.desktop.PreG9BCu3M3Evaluator.C;
import org.geocedg.desktop.PreG9BCu3M3Evaluator.J;
import org.geocedg.desktop.PreG9BCu3M3Evaluator.L;
import org.geocedg.desktop.PreG9BCu3M3Evaluator.P;
import org.geocedg.desktop.PreG9BCu3M3Evaluator.Selection;
import org.geocedg.desktop.PreG9BCu3M3Tower.Num;
import org.geogebra.common.kernel.PathParameter;
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
 * PRE-G9B-CU3 gate G-M3 prototype, run only through
 * {@code tools/benchmark/pre-g9b-cu/run-cu3-m3-gate.ps1}. It evaluates the captured class-v3
 * programs exactly at exact driver points ({@link PreG9BCu3M3Evaluator}), decides every incidence
 * against the authority of its target ({@code locus-v2-certified-construction-model.md} section
 * 11.3: line coefficients and limited part; circle centre and half axis of the adapter), and
 * records as diagnostics the reconstruction of the target, the deeper joint recomputation, the
 * representations of a circle, the exact jets, the root counts near the contacts, the arithmetic
 * hypotheses of the captured constants, the correspondence with the live algorithm and the
 * deterministic counters. It reads copies of the author model and synthetic constructions only.
 * It is evidence for the author's gate decision: it assigns no verdict and changes no product
 * behaviour. It is not part of any source set.
 */
@EnabledIfSystemProperty(named = "geocedg.cu0.harness", matches = "cu3-m3-gate")
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3M3GateProbe {

	/** Budgets of the measurement runs: generous, so that the counters measure the need. */
	static final long OPERATION_BUDGET = 200_000_000L;
	static final int BIT_BUDGET = 1 << 22;
	static final int DEPTH_BUDGET = 48;
	/** Half width of the parameter box of the root counts, around the exact contact. */
	static final double COUNT_BOX = 1e-3;
	private static final MathContext REPORT = new MathContext(12);

	@TempDir
	Path work;

	@Test
	void run() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.cu0.output"));
		Files.createDirectories(output);
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("schema_version", 1);
		root.put("id", "pre-g9b-cu3-m3-gate");
		root.put("status", "CU3 GATE G-M3 PROTOTYPE EVIDENCE - NO VERDICT OF ITS OWN");
		Map<String, Object> generator = new LinkedHashMap<>();
		generator.put("runner", "tools/benchmark/pre-g9b-cu/run-cu3-m3-gate.ps1");
		generator.put("source_commit", System.getProperty("geocedg.cu0.commit"));
		generator.put("source_tree", System.getProperty("geocedg.cu0.tree"));
		generator.put("product_sources_modified_relative_to_commit",
				Boolean.getBoolean("geocedg.cu0.productDirty"));
		root.put("generator", generator);
		Map<String, Object> budgets = new LinkedHashMap<>();
		budgets.put("operations", OPERATION_BUDGET);
		budgets.put("bits", BIT_BUDGET);
		budgets.put("tower_depth", DEPTH_BUDGET);
		budgets.put("count_box_half_width", COUNT_BOX);
		root.put("measurement_budgets", budgets);
		root.put("model", onEdt(() -> model(PreG9BCu0Corpus.open(
				PreG9BCu0Corpus.withoutE2(work)))));
		root.put("controls", onEdt(PreG9BCu3M3GateProbe::controls));
		root.put("configurations", onEdt(PreG9BCu3M3GateProbe::configurations));
		Files.writeString(output.resolve("cu3-m3-gate.json"), PreG9BCu0References.json(root),
				StandardCharsets.UTF_8);
	}

	// ---------------------------------------------------------------- author model

	private static Map<String, Object> model(AppGeoCeDG app) {
		Map<String, Object> model = new LinkedHashMap<>();
		model.put("hypotheses_5_8", hypotheses(app));
		Map<String, Object> contacts = new LinkedHashMap<>();
		contacts.put("e_1*n at K", contact(app, "e_1", "E_1", "K", "n", true).report);
		contacts.put("e_1*c_1 at K", contact(app, "e_1", "E_1", "K", "c_1", true).report);
		contacts.put("o_1*h_2 at K", contact(app, "o_1", "E_1", "K", "h_2", true).report);
		contacts.put("e_1*c_1 at J", contact(app, "e_1", "E_1", "J", "c_1", true).report);
		contacts.put("e_1*r at J", contact(app, "e_1", "E_1", "J", "r", true).report);
		model.put("mandatory_contacts", contacts);
		model.put("other_branch_of_H_1", otherBranch(app));
		model.put("live_correspondence", liveCorrespondence(app));
		return model;
	}

	/** Analysis section 5.8: the relations of the theorem in the captured binary64 states. */
	private static Map<String, Object> hypotheses(AppGeoCeDG app) {
		PreG9BCu3M3Tower tower = new PreG9BCu3M3Tower(OPERATION_BUDGET, BIT_BUDGET,
				DEPTH_BUDGET);
		Map<String, Object> row = new LinkedHashMap<>();
		for (String label : List.of("A", "B", "H", "I")) {
			GeoPoint point = point(app, label);
			row.put(label + ".z", point.getZ());
		}
		GeoLine g = line(app, "g");
		GeoLine h = line(app, "h");
		GeoLine i = line(app, "i");
		GeoLine k = line(app, "k");
		row.put("g vertical (b == 0)", g.getY() == 0);
		row.put("k vertical (b == 0)", k.getY() == 0);
		row.put("h horizontal (a == 0)", h.getX() == 0);
		row.put("i horizontal (a == 0)", i.getX() == 0);
		row.put("A on g exactly", incident(tower, g, point(app, "A")));
		row.put("A on h exactly", incident(tower, h, point(app, "A")));
		row.put("B on i exactly", incident(tower, i, point(app, "B")));
		row.put("H on k exactly", incident(tower, k, point(app, "H")));
		row.put("I on i exactly", incident(tower, i, point(app, "I")));
		row.put("I on k exactly", incident(tower, k, point(app, "I")));
		row.put("I == (H.x, B.y) exactly", point(app, "I").getInhomX() == point(app, "H")
				.getInhomX() && point(app, "I").getInhomY() == point(app, "B").getInhomY());
		GeoConic c1 = (GeoConic) app.getKernel().lookupLabel("c_1");
		double[] matrix = c1.getFlatMatrix();
		GeoVec2D centre = c1.getTranslationVector();
		row.put("centre of c_1 == H (matrix)", -matrix[4] == point(app, "H").getInhomX()
				&& -matrix[5] == point(app, "H").getInhomY());
		row.put("centre of c_1 == H (adapter translation vector)",
				centre.getX() == point(app, "H").getInhomX()
						&& centre.getY() == point(app, "H").getInhomY());
		GeoConic p = (GeoConic) app.getKernel().lookupLabel("p");
		Coords e0 = p.getEigenvec(0);
		Coords e1 = p.getEigenvec(1);
		row.put("p centre == B", p.getMidpoint2D().getX() == point(app, "B").getInhomX()
				&& p.getMidpoint2D().getY() == point(app, "B").getInhomY());
		row.put("p eigenvectors (1,0),(0,1)", e0.getX() == 1 && e0.getY() == 0
				&& e1.getX() == 0 && e1.getY() == 1);
		row.put("p half axes equal", p.getHalfAxis(0) == p.getHalfAxis(1));
		row.put("p half axis == rbCone", p.getHalfAxis(0)
				== ((GeoNumeric) app.getKernel().lookupLabel("rbCone")).getValue());
		return row;
	}

	private static boolean incident(PreG9BCu3M3Tower tower, GeoLine line, GeoPoint point) {
		return tower.of(line.getX()).multiply(tower.of(point.getX()))
				.add(tower.of(line.getY()).multiply(tower.of(point.getY())))
				.add(tower.of(line.getZ()).multiply(tower.of(point.getZ()))).signum() == 0;
	}

	// ---------------------------------------------------------------- one contact

	/** Result of one contact: its report and the exact objects reused by other rows. */
	static final class Contact {
		final Map<String, Object> report = new LinkedHashMap<>();
		PreG9BCu3M3Tower tower;
		PreG9BCu3M3Evaluator evaluator;
		CertifiedConstructionProgram2D program;
		P driverPoint;
		P otherBranch;
		P traced;
		Num cosine;
		Num sine;
		boolean exactZero;
		boolean exactPi;
	}

	/**
	 * Decides the incidence of the exact point of {@code locus} at the exact driver point of
	 * {@code d} with {@code target}, against the target's authority.
	 */
	static Contact contact(AppGeoCeDG app, String locusLabel, String driverLabel, String dLabel,
			String targetLabel, boolean withJointRecomputation) {
		Contact contact = new Contact();
		Map<String, Object> report = contact.report;
		long started = System.nanoTime();
		contact.tower = new PreG9BCu3M3Tower(OPERATION_BUDGET, BIT_BUDGET, DEPTH_BUDGET);
		contact.evaluator = new PreG9BCu3M3Evaluator(contact.tower);
		PreG9BCu3M3Tower tower = contact.tower;
		PreG9BCu3M3Evaluator ev = contact.evaluator;
		report.put("locus", locusLabel);
		report.put("driver", dLabel);
		report.put("target", targetLabel);
		try {
			LocusDefinition2D definition = definition(app, locusLabel);
			ConstructionBoxModel2D box = ConstructionBoxModel2D.capture(definition);
			if (box == null) {
				report.put("outcome", "NO_CLASS_V3_PROGRAM");
				return contact;
			}
			CertifiedConstructionProgram2D program = box.getProgram();
			contact.program = program;
			report.put("program", programSummary(program));
			if (program.getDriver() != Driver.CIRCLE) {
				report.put("outcome", "EXACT_EVALUATION_OUT_OF_CLASS: driver family "
						+ program.getDriver());
				return contact;
			}
			// Driver meaning of the support (rule 2 of section 11.2).
			Num h0 = tower.of(program.getDriverConstant(0));
			Num h1 = tower.of(program.getDriverConstant(1));
			Num e0x = tower.of(program.getDriverConstant(2));
			Num e0y = tower.of(program.getDriverConstant(3));
			Num e1x = tower.of(program.getDriverConstant(4));
			Num e1y = tower.of(program.getDriverConstant(5));
			Num mx = tower.of(program.getDriverConstant(6));
			Num my = tower.of(program.getDriverConstant(7));
			boolean circle = h0.subtract(h1).signum() == 0
					&& e0x.multiply(e1x).add(e0y.multiply(e1y)).signum() == 0
					&& e0x.square().add(e0y.square()).subtract(tower.one()).signum() == 0
					&& e1x.square().add(e1y.square()).subtract(tower.one()).signum() == 0;
			report.put("driver_meaning_is_exact_circle", circle);
			if (!circle) {
				report.put("outcome", "EXACT_EVALUATION_OUT_OF_CLASS: the driver meaning is not "
						+ "an exact circle");
				return contact;
			}
			// Facts 1 and 2: provenance of D and identity of the driver support.
			GeoPoint driver = point(app, driverLabel);
			GeoPoint d = point(app, dLabel);
			AlgoElement parent = d.getParentAlgorithm();
			int index = -1;
			AlgoIntersectLineConic algorithm = null;
			if (d.isPointOnPath() && d.getPath() == driver.getPath()) {
				// PATH_POINT: the correspondence is the path parameter, exact by definition, but
				// the point cos(t), sin(t) is algebraic only at t = 0 for a binary64 t.
				double t = d.getPathParameter().getT();
				report.put("provenance", "PATH_POINT with path parameter " + t);
				report.put("outcome", t == 0 ? "EXACT DRIVER POINT AT t = 0 (not exercised here)"
						: "EXACT_EVALUATION_OUT_OF_CLASS: the exact point of a path parameter other "
								+ "than 0 has transcendental coordinates");
				return contact;
			}
			if (parent instanceof AlgoIntersectLineConic) {
				algorithm = (AlgoIntersectLineConic) parent;
				for (int output = 0; output < algorithm.getOutputLength(); output++) {
					if (algorithm.getOutput(output) == d) {
						index = output;
					}
				}
			} else if (parent instanceof AlgoIntersectSingle
					&& ((AlgoIntersectSingle) parent).getSourceIntersection()
							instanceof AlgoIntersectLineConic) {
				algorithm = (AlgoIntersectLineConic) ((AlgoIntersectSingle) parent)
						.getSourceIntersection();
				index = ((AlgoIntersectSingle) parent).getSelectedIndex();
			}
			if (algorithm == null || index < 0) {
				report.put("outcome", "PROVENANCE_NOT_ESTABLISHED");
				return contact;
			}
			boolean sameSupport = algorithm.getConic() == driver.getPath();
			report.put("provenance", "INTERSECTION_OUTPUT " + algorithm.getClass().getSimpleName()
					+ " output " + index);
			report.put("driver_support_is_the_same_object", sameSupport);
			if (!sameSupport) {
				report.put("outcome", "DRIVER_SUPPORT_MISMATCH");
				return contact;
			}
			GeoLine line = algorithm.getLine();
			L supportLine = lineAuthority(ev, line);
			C supportCircle = ev.circleOf(ev.constant(mx), ev.constant(my),
					ev.constant(h0.square()));
			boolean compaction = algorithm.isSelectionFirstIntersectionPending();
			int[] permutation = compaction ? new int[] {0, 1}
					: algorithm.getSelectionPermutation();
			if (!compaction && algorithm.isSelectionPermutationPending()) {
				report.put("outcome", "SELECTION_NOT_RECONSTRUCTED: permutation pending");
				return contact;
			}
			boolean[] labelled = {algorithm.getOutput(0).isLabelSet(),
					algorithm.getOutput(1).isLabelSet()};
			report.put("selection_state", (compaction ? "compaction" : "permutation "
					+ permutation[0] + "," + permutation[1]) + "; labelled " + labelled[0] + ","
					+ labelled[1]);
			P exactD = ev.lineCircleOutput(supportLine, supportCircle, compaction, permutation,
					labelled, index);
			P exactOther = ev.lineCircleOutput(supportLine, supportCircle, compaction,
					permutation, labelled, 1 - index);
			contact.driverPoint = exactD;
			contact.otherBranch = exactOther;
			report.put("D_star", xy(exactD));
			report.put("D_live", List.of(d.getInhomX(), d.getInhomY()));
			// Fact 3: exact correspondence on the driver meaning.
			Num dx = exactD.inhomX.v.subtract(mx);
			Num dy = exactD.inhomY.v.subtract(my);
			Num cosine = dx.multiply(e0x).add(dy.multiply(e0y)).divide(h0);
			Num sine = dx.multiply(e1x).add(dy.multiply(e1y)).divide(h1);
			contact.cosine = cosine;
			contact.sine = sine;
			report.put("D_star_on_the_driver_circle_exactly",
					cosine.square().add(sine.square()).subtract(tower.one()).signum() == 0);
			contact.exactZero = cosine.subtract(tower.one()).signum() == 0 && sine.signum() == 0;
			contact.exactPi = cosine.add(tower.one()).signum() == 0 && sine.signum() == 0;
			report.put("t_star", contact.exactZero ? "0 (exact)" : contact.exactPi ? "pi (exact)"
					: "enclosed (cos " + approx(cosine) + ", sin " + approx(sine) + ")");
			// Exact driver jet at t*.
			Num firstX = h1.multiply(cosine).multiply(e1x).subtract(h0.multiply(sine)
					.multiply(e0x));
			Num firstY = h1.multiply(cosine).multiply(e1y).subtract(h0.multiply(sine)
					.multiply(e0y));
			Num secondX = h0.multiply(cosine).multiply(e0x).add(h1.multiply(sine).multiply(e1x))
					.negate();
			Num secondY = h0.multiply(cosine).multiply(e0y).add(h1.multiply(sine).multiply(e1y))
					.negate();
			P driverJet = ev.unit(ev.jet(exactD.inhomX.v, firstX, secondX),
					ev.jet(exactD.inhomY.v, firstY, secondY));
			// Fact 4: the exact point of L in its captured configuration.
			P traced = ev.evaluate(program, driverJet);
			contact.traced = traced;
			report.put("P_L", xy(traced));
			report.put("P_L_first_derivative", List.of(approx(traced.inhomX.d1),
					approx(traced.inhomY.d1)));
			report.put("P_L_first_derivative_is_exact_zero (fold)", traced.inhomX.d1.signum() == 0
					&& traced.inhomY.d1.signum() == 0);
			report.put("selections", selections(ev));
			GeoElement target = app.getKernel().lookupLabel(targetLabel);
			report.put("authority", authority(ev, target, traced));
			report.put("count_near_the_contact", count(app, locusLabel, target, contact));
			report.put("reconstruction_of_the_target", reconstruction(app, ev, target, traced,
					d, exactD, exactOther, algorithm));
			if (withJointRecomputation) {
				report.put("joint_recomputation_sigma_B", joint(app, locusLabel, driverLabel, d,
						target, exactD));
			}
		} catch (PreG9BCu3M3Evaluator.Stop stop) {
			report.put("outcome", stop.failure.name() + ": " + stop.getMessage());
		} catch (PreG9BCu3M3Tower.BudgetExhausted exhausted) {
			report.put("outcome", "EXACT_EVALUATION_BUDGET_EXHAUSTED: " + exhausted.getMessage());
		} catch (RuntimeException failure) {
			StackTraceElement[] trace = failure.getStackTrace();
			report.put("outcome", "PROBE_FAILURE: " + failure
					+ (trace.length > 0 ? " at " + trace[0] : "")
					+ (trace.length > 1 ? " < " + trace[1] : ""));
		}
		report.put("counters", counters(contact.tower, started));
		return contact;
	}

	private static Map<String, Object> programSummary(CertifiedConstructionProgram2D program) {
		Map<String, Object> summary = new LinkedHashMap<>();
		summary.put("version", program.getVersion());
		summary.put("driver", program.getDriver().name());
		summary.put("steps", program.getSteps().size());
		int lineCircle = 0;
		int circleConstants = 0;
		for (Step step : program.getSteps()) {
			lineCircle += step.getOperation() == Operation.LINE_CIRCLE ? 1 : 0;
			for (Operand operand : step.getOperands()) {
				circleConstants += operand.getKind() == OperandKind.CIRCLE ? 1 : 0;
			}
		}
		summary.put("line_circle_steps", lineCircle);
		summary.put("circle_constants", circleConstants);
		summary.put("selection_signature", program.getSelectionSignature());
		return summary;
	}

	private static List<Object> selections(PreG9BCu3M3Evaluator ev) {
		List<Object> rows = new ArrayList<>();
		for (Selection selection : ev.selections) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("node", selection.node);
			row.put("selected_raw_root", selection.selectedRaw);
			row.put("raw_root_0", List.of(approx(selection.roots[0]), approx(selection.roots[1])));
			row.put("raw_root_1", List.of(approx(selection.roots[2]), approx(selection.roots[3])));
			rows.add(row);
		}
		return rows;
	}

	// ---------------------------------------------------------------- target authority

	/** Section 11.3: the authoritative meaning of a line target, its binary64 coefficients. */
	private static L lineAuthority(PreG9BCu3M3Evaluator ev, GeoLine line) {
		boolean segment = line instanceof GeoSegment;
		boolean ray = line instanceof GeoRay;
		GeoPoint start = (segment || ray) ? line.getStartPoint() : null;
		return ev.lineOf(ev.constant(line.getX()), ev.constant(line.getY()),
				ev.constant(line.getZ()), segment || ray, ray,
				start == null ? null : ev.constant(start.getInhomX()),
				start == null ? null : ev.constant(start.getInhomY()));
	}

	/**
	 * The incidence decided against the authority of the target, with its exact residual jet;
	 * for a circle, the comparison of its representations.
	 */
	private static Map<String, Object> authority(PreG9BCu3M3Evaluator ev, GeoElement target,
			P point) {
		Map<String, Object> row = new LinkedHashMap<>();
		J residual;
		if (target instanceof GeoLine) {
			GeoLine line = (GeoLine) target;
			row.put("family", target instanceof GeoSegment ? "SEGMENT"
					: target instanceof GeoRay ? "RAY" : "LINE");
			row.put("authority", "binary64 coefficients (" + line.getX() + ", " + line.getY() + ", "
					+ line.getZ() + ")");
			residual = ev.constant(line.getX()).multiply(point.inhomX)
					.add(ev.constant(line.getY()).multiply(point.inhomY))
					.add(ev.constant(line.getZ()));
			if (target instanceof GeoSegment || target instanceof GeoRay) {
				row.put("limited_part", limitedPart(ev, line, point));
			}
		} else if (target instanceof GeoConic && ((GeoConic) target).isCircle()) {
			GeoConic circle = (GeoConic) target;
			GeoVec2D centre = circle.getTranslationVector();
			double radius = circle.getHalfAxes()[0];
			row.put("family", "CIRCLE");
			row.put("authority", "adapter centre (" + centre.getX() + ", " + centre.getY()
					+ ") and half axis " + radius);
			J dx = point.inhomX.subtract(ev.constant(centre.getX()));
			J dy = point.inhomY.subtract(ev.constant(centre.getY()));
			Num radiusSquared = ev.constant(radius).v.square();
			residual = dx.square().add(dy.square()).subtract(ev.constant(radiusSquared));
			row.put("representations", representations(ev, circle));
		} else {
			row.put("outcome", "TARGET_UNSUPPORTED");
			return row;
		}
		int sign = residual.sign();
		row.put("decision", sign == 0 ? "EQUAL" : "DIFFERENT");
		row.put("residual_sign", sign);
		row.put("residual_approx", approx(residual.v));
		if (residual.d1 != null) {
			row.put("residual_first_derivative", approx(residual.d1));
			row.put("residual_first_derivative_is_exact_zero", residual.d1.signum() == 0);
			row.put("residual_second_derivative", approx(residual.d2));
		}
		return row;
	}

	/** Membership of the point in the limited part of a segment or ray, decided exactly. */
	private static Map<String, Object> limitedPart(PreG9BCu3M3Evaluator ev, GeoLine line,
			P point) {
		Map<String, Object> row = new LinkedHashMap<>();
		GeoPoint start = line.getStartPoint();
		J sx = ev.constant(start.getInhomX());
		J sy = ev.constant(start.getInhomY());
		J ux;
		J uy;
		if (line instanceof GeoSegment) {
			GeoPoint end = line.getEndPoint();
			ux = ev.constant(end.getInhomX()).subtract(sx);
			uy = ev.constant(end.getInhomY()).subtract(sy);
		} else {
			ux = ev.constant(line.getY());
			uy = ev.constant(line.getX()).negate();
		}
		J dot = point.inhomX.subtract(sx).multiply(ux).add(point.inhomY.subtract(sy).multiply(uy));
		J norm = ux.square().add(uy.square());
		boolean lower = dot.sign() >= 0;
		boolean upper = !(line instanceof GeoSegment) || norm.subtract(dot).sign() >= 0;
		row.put("member", lower && upper);
		row.put("parameter_approx", approx(dot.v.divide(norm.v)));
		return row;
	}

	/** The matrix, the half axis and the binary64 implicit coefficient of a circle. */
	private static Map<String, Object> representations(PreG9BCu3M3Evaluator ev, GeoConic circle) {
		Map<String, Object> row = new LinkedHashMap<>();
		double[] matrix = circle.getFlatMatrix();
		GeoVec2D centre = circle.getTranslationVector();
		double radius = circle.getHalfAxes()[0];
		Num a4 = ev.constant(matrix[4]).v;
		Num a5 = ev.constant(matrix[5]).v;
		Num matrixSquared = a4.square().add(a5.square()).subtract(ev.constant(matrix[2]).v);
		Num halfAxisSquared = ev.constant(radius).v.square();
		double implicit = centre.getX() * centre.getX() + centre.getY() * centre.getY()
				- radius * radius;
		Num implicitSquared = ev.constant(centre.getX()).v.square()
				.add(ev.constant(centre.getY()).v.square()).subtract(ev.constant(implicit).v);
		row.put("matrix_radius_squared", approx(matrixSquared));
		row.put("half_axis_radius_squared", approx(halfAxisSquared));
		row.put("binary64_implicit_radius_squared", approx(implicitSquared));
		row.put("matrix_equals_half_axis_exactly", matrixSquared.subtract(halfAxisSquared)
				.signum() == 0);
		row.put("matrix_minus_half_axis", approx(matrixSquared.subtract(halfAxisSquared)));
		row.put("matrix_centre_equals_adapter_centre", -matrix[4] == centre.getX()
				&& -matrix[5] == centre.getY());
		return row;
	}

	// ---------------------------------------------------------------- root count near t*

	/**
	 * The exact residual value and jets at the contact parameter with an interval enclosure of
	 * the second derivative on the box {@code [t* - delta, t* + delta]} decide the number of
	 * roots in the box (the conflict-rule certificate of the PRE-G9B-CU3 amendment, CU3.4).
	 */
	private static Map<String, Object> count(AppGeoCeDG app, String locusLabel,
			GeoElement target, Contact contact) {
		Map<String, Object> row = new LinkedHashMap<>();
		if (!contact.exactZero && !contact.exactPi) {
			row.put("outcome", "NOT_COMPUTED: the contact parameter is not 0 or pi");
			return row;
		}
		PreG9BCu3EnclosureProbeAccess access = PreG9BCu3EnclosureProbeAccess.capture(
				definition(app, locusLabel));
		if (access == null) {
			row.put("outcome", "NO_ENCLOSURE_MODEL");
			return row;
		}
		double centre = contact.exactZero ? 0 : Math.PI;
		boolean periodic = contact.exactPi;
		double lower = centre - COUNT_BOX;
		double upper = centre + COUNT_BOX;
		// Condition (C1): the box lies in one certified continuous component of L.
		LocusExistenceStructure2D structure = definition(app, locusLabel)
				.getBranch(PreG9BCu0Corpus.MAIN).getExistenceStructure();
		LocusExistenceStructure2D.ContinuousComponent component = structure == null ? null
				: structure.getComponentContaining(contact.exactZero ? 0 : -Math.PI);
		row.put("certified_continuous_component", component == null ? "NONE"
				: component.isClosedPeriodic() ? "CLOSED_PERIODIC" : "ARC");
		if (component == null) {
			row.put("outcome", "NO_CERTIFICATE: the contact is not in a certified component");
			return row;
		}
		double[][] jet = access.traced(lower, upper, periodic);
		if (jet == null) {
			row.put("outcome", "BOX_NOT_VALID");
			return row;
		}
		double[] second = secondDerivative(target, jet);
		row.put("box", List.of(lower, upper));
		row.put("residual_second_derivative_enclosure", List.of(second[0], second[1]));
		if (second[0] <= 0 && second[1] >= 0) {
			row.put("outcome", "UNDECIDED: the second-derivative enclosure contains zero");
			return row;
		}
		boolean convex = second[0] > 0;
		double m = convex ? second[0] : -second[1];
		double big = convex ? second[1] : -second[0];
		PreG9BCu3M3Tower tower = contact.tower;
		J residual = residualJet(contact.evaluator, target, contact.traced);
		Num h0 = convex ? residual.v : residual.v.negate();
		Num h1 = convex ? residual.d1 : residual.d1.negate();
		// For all t in the box: h0 + h1 u + m u^2/2 <= h <= h0 + h1 u + M u^2/2 (convex form).
		Num lowerMinimum = h0.subtract(h1.square().divide(tower.of(2 * m)));
		Num upperMinimum = h0.subtract(h1.square().divide(tower.of(2 * big)));
		double[] ends = endValues(access, target, lower, upper, periodic);
		row.put("residual_at_box_ends", List.of(ends[0], ends[1], ends[2], ends[3]));
		boolean endsPositive = convex ? ends[0] > 0 && ends[2] > 0 : ends[1] < 0 && ends[3] < 0;
		// The Taylor point u = -h1 / M of the upper bound must lie inside the box.
		boolean insideBox = h1.square().subtract(tower.of(big).square().multiply(
				tower.of(COUNT_BOX).square())).signum() <= 0;
		row.put("taylor_point_inside_the_box", insideBox);
		if (lowerMinimum.signum() > 0) {
			row.put("outcome", "N = 0: the residual keeps the sign of its second derivative");
		} else if (upperMinimum.signum() < 0 && endsPositive && insideBox) {
			row.put("outcome", "N = 2: two simple roots, one on each side of the critical point");
		} else if (residual.v.signum() == 0 && residual.d1.signum() == 0) {
			row.put("outcome", "N = 1 double at the exact contact");
		} else {
			row.put("outcome", "UNDECIDED");
		}
		row.put("lower_bound_of_the_minimum", approx(convex ? lowerMinimum
				: lowerMinimum.negate()));
		return row;
	}

	private static J residualJet(PreG9BCu3M3Evaluator ev, GeoElement target, P point) {
		if (target instanceof GeoLine) {
			GeoLine line = (GeoLine) target;
			return ev.constant(line.getX()).multiply(point.inhomX)
					.add(ev.constant(line.getY()).multiply(point.inhomY))
					.add(ev.constant(line.getZ()));
		}
		GeoConic circle = (GeoConic) target;
		GeoVec2D centre = circle.getTranslationVector();
		double radius = circle.getHalfAxes()[0];
		J dx = point.inhomX.subtract(ev.constant(centre.getX()));
		J dy = point.inhomY.subtract(ev.constant(centre.getY()));
		return dx.square().add(dy.square()).subtract(ev.constant(ev.constant(radius).v.square()));
	}

	/** Outward enclosure of the residual's second derivative from the class-v3 jets. */
	private static double[] secondDerivative(GeoElement target, double[][] jet) {
		if (target instanceof GeoLine) {
			GeoLine line = (GeoLine) target;
			Iv value = Iv.of(line.getX()).times(Iv.of(jet[4])).plus(Iv.of(line.getY())
					.times(Iv.of(jet[5])));
			return new double[] {value.lo, value.hi};
		}
		GeoConic circle = (GeoConic) target;
		GeoVec2D centre = circle.getTranslationVector();
		Iv x = Iv.of(jet[0]).minus(Iv.of(centre.getX()));
		Iv y = Iv.of(jet[1]).minus(Iv.of(centre.getY()));
		Iv value = Iv.of(jet[2]).sq().plus(Iv.of(jet[3]).sq()).plus(x.times(Iv.of(jet[4])))
				.plus(y.times(Iv.of(jet[5]))).times(Iv.of(2));
		return new double[] {value.lo, value.hi};
	}

	/** @return lower and upper enclosures of the residual at both box ends */
	private static double[] endValues(PreG9BCu3EnclosureProbeAccess access, GeoElement target,
			double lower, double upper, boolean periodic) {
		double[] result = new double[4];
		double[] at = {lower, upper};
		for (int side = 0; side < 2; side++) {
			double[][] jet = access.traced(at[side], at[side], periodic);
			Iv value;
			if (jet == null) {
				result[2 * side] = Double.NaN;
				result[2 * side + 1] = Double.NaN;
				continue;
			}
			if (target instanceof GeoLine) {
				GeoLine line = (GeoLine) target;
				value = Iv.of(line.getX()).times(Iv.of(jet[0])).plus(Iv.of(line.getY())
						.times(Iv.of(jet[1]))).plus(Iv.of(line.getZ()));
			} else {
				GeoConic circle = (GeoConic) target;
				GeoVec2D centre = circle.getTranslationVector();
				double radius = circle.getHalfAxes()[0];
				value = Iv.of(jet[0]).minus(Iv.of(centre.getX())).sq()
						.plus(Iv.of(jet[1]).minus(Iv.of(centre.getY())).sq())
						.minus(Iv.of(radius).sq());
			}
			result[2 * side] = value.lo;
			result[2 * side + 1] = value.hi;
		}
		return result;
	}

	// ---------------------------------------------------------------- diagnostics

	/**
	 * The target recomputed exactly from its subgraph, with {@code D} and the other output of
	 * its algorithm replaced by their exact points and every other ancestor at its binary64
	 * state (the earlier rule 6): a diagnostic only, compared with the authority.
	 */
	private static Map<String, Object> reconstruction(AppGeoCeDG app, PreG9BCu3M3Evaluator ev,
			GeoElement target, P point, GeoPoint d, P exactD, P exactOther,
			AlgoIntersectLineConic algorithm) {
		Map<String, Object> row = new LinkedHashMap<>();
		Reconstruction graph = new Reconstruction(ev, false);
		graph.substitute(d, exactD);
		for (int output = 0; output < algorithm.getOutputLength(); output++) {
			if (algorithm.getOutput(output) != d) {
				graph.substitute(algorithm.getOutput(output), exactOther);
			}
		}
		graph.roots.add(d);
		for (int output = 0; output < algorithm.getOutputLength(); output++) {
			graph.roots.add(algorithm.getOutput(output));
		}
		try {
			Object value = graph.eval(target);
			row.putAll(compareWithAuthority(ev, target, value, point));
		} catch (PreG9BCu3M3Evaluator.Stop stop) {
			row.put("outcome", stop.failure.name() + ": " + stop.getMessage());
		} catch (IllegalStateException unsupported) {
			row.put("outcome", "OUT_OF_CLASS: " + unsupported.getMessage());
		}
		return row;
	}

	/** The deeper joint recomputation {@code Sigma_B}: every object from the free inputs. */
	private static Map<String, Object> joint(AppGeoCeDG app, String locusLabel,
			String driverLabel, GeoPoint d, GeoElement target, P exactD) {
		Map<String, Object> row = new LinkedHashMap<>();
		PreG9BCu3M3Tower tower = new PreG9BCu3M3Tower(OPERATION_BUDGET, BIT_BUDGET,
				DEPTH_BUDGET);
		PreG9BCu3M3Evaluator ev = new PreG9BCu3M3Evaluator(tower);
		Reconstruction graph = new Reconstruction(ev, true);
		GeoPoint driver = point(app, driverLabel);
		try {
			// The exact driver point of Sigma_B is D recomputed deeply.
			Object deepD = graph.eval(d);
			graph.substitute(driver, deepD);
			GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, locusLabel);
			GeoElement traced = null;
			AlgoElement parent = locus.getParentAlgorithm();
			for (int input = 0; parent != null && input < parent.getInputLength(); input++) {
				GeoElement candidate = parent.getInput(input).toGeoElement();
				if (candidate instanceof GeoPoint && candidate != driver && traced == null) {
					traced = candidate;
				}
			}
			if (traced == null) {
				row.put("outcome", "OUT_OF_CLASS: no traced point");
				return row;
			}
			P pointB = (P) graph.eval(traced);
			Object targetB = graph.eval(target);
			row.put("P_B", xy(pointB));
			row.put("incidence_in_sigma_B", residualSign(ev, targetB, pointB) == 0 ? "HOLDS"
					: "DOES_NOT_HOLD");
			row.put("note", "diagnostic only; it never establishes the incidence");
		} catch (PreG9BCu3M3Evaluator.Stop stop) {
			row.put("outcome", stop.failure.name() + ": " + stop.getMessage());
		} catch (IllegalStateException unsupported) {
			row.put("outcome", "OUT_OF_CLASS: " + unsupported.getMessage());
		} catch (PreG9BCu3M3Tower.BudgetExhausted exhausted) {
			row.put("outcome", "BUDGET_EXHAUSTED: " + exhausted.getMessage());
		}
		row.put("counters", counters(tower, System.nanoTime()));
		return row;
	}

	private static Map<String, Object> compareWithAuthority(PreG9BCu3M3Evaluator ev,
			GeoElement target, Object value, P point) {
		Map<String, Object> row = new LinkedHashMap<>();
		if (value instanceof L) {
			L reconstructed = (L) value;
			GeoLine line = (GeoLine) target;
			Num a = ev.constant(line.getX()).v;
			Num b = ev.constant(line.getY()).v;
			Num c = ev.constant(line.getZ()).v;
			boolean proportional = reconstructed.a.v.multiply(b).subtract(reconstructed.b.v
					.multiply(a)).signum() == 0
					&& reconstructed.a.v.multiply(c).subtract(reconstructed.c.v.multiply(a))
							.signum() == 0
					&& reconstructed.b.v.multiply(c).subtract(reconstructed.c.v.multiply(b))
							.signum() == 0;
			row.put("reconstructed_coefficients", List.of(approx(reconstructed.a.v),
					approx(reconstructed.b.v), approx(reconstructed.c.v)));
			row.put("proportional_to_the_authority", proportional);
			Num scale = a.signum() != 0 ? reconstructed.a.v.divide(a)
					: reconstructed.b.v.divide(b);
			row.put("offset_difference_after_scaling", approx(reconstructed.c.v.subtract(
					scale.multiply(c))));
		} else if (value instanceof C) {
			C reconstructed = (C) value;
			GeoConic circle = (GeoConic) target;
			GeoVec2D centre = circle.getTranslationVector();
			Num radiusSquared = ev.constant(circle.getHalfAxes()[0]).v.square();
			row.put("reconstructed_radius_squared", approx(reconstructed.radiusSquared.v));
			row.put("equal_to_the_authority", reconstructed.mx.v.subtract(ev.constant(
					centre.getX()).v).signum() == 0 && reconstructed.my.v.subtract(
					ev.constant(centre.getY()).v).signum() == 0
					&& reconstructed.radiusSquared.v.subtract(radiusSquared).signum() == 0);
		}
		int sign = residualSign(ev, value, point);
		row.put("incidence_with_the_reconstruction", sign == 0 ? "EQUAL" : "DIFFERENT");
		row.put("note", "diagnostic only (section 11.3); it never establishes the incidence");
		return row;
	}

	private static int residualSign(PreG9BCu3M3Evaluator ev, Object target, P point) {
		if (target instanceof L) {
			L line = (L) target;
			return line.a.v.multiply(point.inhomX.v).add(line.b.v.multiply(point.inhomY.v))
					.add(line.c.v).signum();
		}
		C circle = (C) target;
		Num dx = point.inhomX.v.subtract(circle.mx.v);
		Num dy = point.inhomY.v.subtract(circle.my.v);
		return dx.square().add(dy.square()).subtract(circle.radiusSquared.v).signum();
	}

	/**
	 * Exact recomputation of a subgraph of GeoElements with the exact formulas of the class.
	 * In shallow mode only the descendants of the roots are recomputed and every other object
	 * is taken at its binary64 state (a circle by its matrix); in deep mode every object is
	 * recomputed from the free inputs.
	 */
	static final class Reconstruction {
		private final PreG9BCu3M3Evaluator ev;
		private final boolean deep;
		private final Map<GeoElement, Object> values = new IdentityHashMap<>();
		final List<GeoElement> roots = new ArrayList<>();

		Reconstruction(PreG9BCu3M3Evaluator ev, boolean deep) {
			this.ev = ev;
			this.deep = deep;
		}

		void substitute(GeoElement geo, Object value) {
			values.put(geo, value);
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

		private boolean recompute(GeoElement geo) {
			if (deep) {
				return geo.getParentAlgorithm() != null;
			}
			for (GeoElement root : roots) {
				if (geo == root || geo.isChildOf(root)) {
					return true;
				}
			}
			return false;
		}

		private Object compute(GeoElement geo) {
			AlgoElement parent = geo.getParentAlgorithm();
			if (!recompute(geo) || parent == null) {
				return binary64(geo);
			}
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
						(L) eval(parallel.getInput(1).toGeoElement()));
			}
			if (parent instanceof AlgoOrthoLinePointLine) {
				AlgoOrthoLinePointLine orthogonal = (AlgoOrthoLinePointLine) parent;
				return ev.perpendicularOf(point(orthogonal.getP()),
						(L) eval(orthogonal.getInput(1).toGeoElement()));
			}
			if (parent instanceof AlgoIntersectLines) {
				AlgoIntersectLines meet = (AlgoIntersectLines) parent;
				return ev.meetOf((L) eval(meet.getg()), (L) eval(meet.geth()));
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
				P centre = point(circle.getCenter().toGeoElement());
				GeoElement radius = circle.getRadiusGeo();
				if (!(radius instanceof GeoNumeric)) {
					throw new IllegalStateException("radius is not a number");
				}
				J r = ev.constant(((GeoNumeric) radius).getValue());
				return ev.circleOf(centre.inhomX, centre.inhomY, r.square());
			}
			if (parent instanceof AlgoIntersectSingle) {
				AlgoIntersectSingle single = (AlgoIntersectSingle) parent;
				if (!(single.getSourceIntersection() instanceof AlgoIntersectLineConic)) {
					throw new IllegalStateException("unsupported single intersection");
				}
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
			if (geo instanceof GeoPoint && ((GeoPoint) geo).isPointOnPath()) {
				return binary64(geo);
			}
			throw new IllegalStateException(parent.getClass().getSimpleName());
		}

		private Object lineConic(AlgoIntersectLineConic algorithm, int index) {
			boolean compaction = algorithm.isSelectionFirstIntersectionPending();
			int[] permutation = compaction ? new int[] {0, 1}
					: algorithm.getSelectionPermutation();
			boolean[] labelled = {algorithm.getOutput(0).isLabelSet(),
					algorithm.getOutput(1).isLabelSet()};
			return ev.lineCircleOutput((L) eval(algorithm.getLine()),
					(C) eval(algorithm.getConic()), compaction, permutation, labelled, index);
		}

		private P point(GeoElement geo) {
			return (P) eval(geo);
		}

		private Object binary64(GeoElement geo) {
			if (geo instanceof GeoPoint) {
				GeoPoint point = (GeoPoint) geo;
				return ev.new P(ev.constant(point.getX()), ev.constant(point.getY()),
						ev.constant(point.getZ()), ev.constant(point.getInhomX()),
						ev.constant(point.getInhomY()), point.getZ() == 1);
			}
			if (geo instanceof GeoLine) {
				return lineAuthority(ev, (GeoLine) geo);
			}
			if (geo instanceof GeoConic && ((GeoConic) geo).isCircle()) {
				double[] matrix = ((GeoConic) geo).getFlatMatrix();
				J a4 = ev.constant(matrix[4]);
				J a5 = ev.constant(matrix[5]);
				return ev.circleOf(a4.negate(), a5.negate(),
						a4.square().add(a5.square()).subtract(ev.constant(matrix[2])));
			}
			throw new IllegalStateException("unsupported leaf " + geo.getClass().getSimpleName());
		}
	}

	/** The other branch of {@code H_1}: the theorem with the reconstructed targets. */
	private static Map<String, Object> otherBranch(AppGeoCeDG app) {
		Map<String, Object> row = new LinkedHashMap<>();
		Contact contact = contact(app, "e_1", "E_1", "K", "n", false);
		if (contact.program == null || contact.driverPoint == null) {
			row.put("outcome", "NOT_AVAILABLE");
			return row;
		}
		List<Step> steps = new ArrayList<>(contact.program.getSteps());
		int flipped = -1;
		for (int index = 0; index < steps.size(); index++) {
			Step step = steps.get(index);
			if (step.getOperation() == Operation.LINE_CIRCLE
					&& step.getOperands().get(1).getKind() == OperandKind.NODE) {
				// d_1 = Circle[I, E_1] is a circle node; H_1 is its intersection with i.
				double first = step.getParameter(0) == 1 ? 1 : step.getParameter(2);
				double second = step.getParameter(0) == 1 ? 0 : step.getParameter(1);
				steps.set(index, new Step(Operation.LINE_CIRCLE, step.getOperands(), 0, first,
						second, step.getParameter(3), step.getParameter(4)));
				flipped = index + 1;
			}
		}
		row.put("flipped_line_circle_node", flipped);
		CertifiedConstructionProgram2D program = contact.program;
		double[] constants = new double[8];
		for (int index = 0; index < 8; index++) {
			constants[index] = program.getDriverConstant(index);
		}
		CertifiedConstructionProgram2D other = new CertifiedConstructionProgram2D(
				program.getDriver(), constants, program.getDomain(), steps);
		PreG9BCu3M3Evaluator ev = contact.evaluator;
		try {
			P traced = ev.evaluate(other, ev.unit(contact.driverPoint.inhomX,
					contact.driverPoint.inhomY));
			row.put("P_L_other_branch", xy(traced));
			GeoPoint m1 = point(app, "M_1");
			row.put("live_M_1", List.of(m1.getInhomX(), m1.getInhomY()));
			for (String label : List.of("n", "c_1")) {
				GeoElement target = app.getKernel().lookupLabel(label);
				Map<String, Object> decision = new LinkedHashMap<>();
				decision.put("authority", authority(ev, target, traced).get("decision"));
				AlgoIntersectLineConic algorithm = (AlgoIntersectLineConic) point(app, "K")
						.getParentAlgorithm();
				decision.put("reconstruction", reconstruction(app, ev, target, traced,
						point(app, "K"), contact.driverPoint, contact.otherBranch, algorithm)
						.get("incidence_with_the_reconstruction"));
				row.put(label, decision);
			}
		} catch (PreG9BCu3M3Evaluator.Stop stop) {
			row.put("outcome", stop.failure.name() + ": " + stop.getMessage());
		}
		return row;
	}

	/**
	 * G-M3-3: with the live driver moved to the exact correspondence of {@code K} and of
	 * {@code J}, every exact branch of the program and of the algorithm of {@code D} is compared
	 * with the live outputs; the selected exact root must be the live one and the other exact
	 * root must be far from it. The live driver is restored afterwards.
	 */
	private static Map<String, Object> liveCorrespondence(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		double saved = PreG9BCu0Corpus.pathParameter(app, "E_1");
		GeoConic p = (GeoConic) app.getKernel().lookupLabel("p");
		try {
			for (String dLabel : List.of("K", "J")) {
				Map<String, Object> row = new LinkedHashMap<>();
				double t = projectedParameter(p, point(app, dLabel));
				row.put("live_parameter", t);
				PreG9BCu0Corpus.movePathPoint(app, "E_1", t);
				for (String locus : List.of("e_1", "o_1")) {
					Contact contact = contact(app, locus, "E_1", dLabel, "c_1", false);
					Map<String, Object> branch = new LinkedHashMap<>();
					if (contact.traced == null) {
						branch.put("outcome", contact.report.get("outcome"));
						row.put(locus, branch);
						continue;
					}
					GeoPoint d = point(app, dLabel);
					branch.put("D_star_minus_live_D", distance(contact.driverPoint, d));
					branch.put("other_branch_minus_live_D", distance(contact.otherBranch, d));
					List<Object> outputs = new ArrayList<>();
					for (Selection selection : contact.evaluator.selections) {
						Map<String, Object> output = new LinkedHashMap<>();
						output.put("node", selection.node);
						GeoPoint nearest = nearestLive(app, selection);
						output.put("live_output", nearest == null ? null : nearest.getLabelSimple());
						if (nearest != null) {
							int selected = selection.selectedRaw;
							output.put("selected_exact_minus_live", distance(selection.roots[2
									* selected], selection.roots[2 * selected + 1], nearest));
							output.put("other_exact_minus_live", distance(selection.roots[2
									* (1 - selected)], selection.roots[2 * (1 - selected) + 1],
									nearest));
						}
						outputs.add(output);
					}
					branch.put("line_circle_outputs", outputs);
					String tracedLabel = "e_1".equals(locus) ? "J_1" : "K_1";
					branch.put("P_L_minus_live_traced_point", distance(contact.traced,
							point(app, tracedLabel)));
					row.put(locus, branch);
				}
				rows.put(dLabel, row);
			}
		} finally {
			PreG9BCu0Corpus.movePathPoint(app, "E_1", saved);
		}
		return rows;
	}

	private static GeoPoint nearestLive(AppGeoCeDG app, Selection selection) {
		GeoPoint best = null;
		double bestDistance = Double.POSITIVE_INFINITY;
		for (String label : List.of("H_1", "G_1")) {
			GeoPoint candidate = point(app, label);
			double d = distance(selection.roots[2 * selection.selectedRaw],
					selection.roots[2 * selection.selectedRaw + 1], candidate);
			if (d < bestDistance) {
				bestDistance = d;
				best = candidate;
			}
		}
		return best;
	}

	// ---------------------------------------------------------------- controls

	private static Map<String, Object> controls() {
		Map<String, Object> rows = new LinkedHashMap<>();
		rows.put("V-M3-5 accidental exact equality: radius 1.5 against y = 1.5",
				synthetic(circleLocus(), "g=Line((0,1.5),(1,1.5))", "g").report);
		rows.put("V-M3-4 target shifted one ulp: y = nextUp(1.5)",
				synthetic(circleLocus(), "g=Line((0," + Math.nextUp(1.5) + "),(1,"
						+ Math.nextUp(1.5) + "))", "g").report);
		AppGeoCeDG sum = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
		for (String command : new String[] {"O=(0,0)", "c=Circle(O,1)", "P=Point(c)",
				"Q_1=P+(0.1,0)", "Q=Q_1+(0.2,0)", "L=LocusV2(Q,P)"}) {
			PreG9BR6PlusF3Fixtures.must(sum, command);
		}
		rows.put("V-M3-6 accidental equality of non-representable values: 0.1 + 0.2 against x = 0.3",
				synthetic(sum, "g=Line((0.3,0),(0.3,1))", "g").report);
		Map<String, Object> outOfClass = new LinkedHashMap<>();
		AppGeoCeDG pathPoint = circleLocus();
		PreG9BR6PlusF3Fixtures.must(pathPoint, "g=Line((0,1.5),(1,1.5))");
		PreG9BR6PlusF3Fixtures.must(pathPoint, "D_p=Point(c)");
		PreG9BCu0Corpus.movePathPoint(pathPoint, "D_p", 0.7);
		outOfClass.put("path point at the generic parameter 0.7",
				contact(pathPoint, "L", "P", "D_p", "g", false).report.get("outcome"));
		AppGeoCeDG budget = circleLocus();
		PreG9BR6PlusF3Fixtures.must(budget, "g=Line((0,1.5),(1,1.5))");
		outOfClass.put("budget injection (8 operations)", syntheticWithBudget(budget, "g", 8));
		rows.put("V-M3-8 out of class and budget", outOfClass);
		return rows;
	}

	private static AppGeoCeDG circleLocus() {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
		for (String command : new String[] {"O=(0,0)", "c=Circle(O,1)", "P=Point(c)",
				"Q=Dilate(P,1.5,O)", "L=LocusV2(Q,P)"}) {
			PreG9BR6PlusF3Fixtures.must(app, command);
		}
		return app;
	}

	/**
	 * A synthetic contact at the exact driver point {@code D}, the output of
	 * {@code Intersect(c, x = 0)} with positive ordinate.
	 */
	private static Contact synthetic(AppGeoCeDG app, String targetCommand, String target) {
		PreG9BR6PlusF3Fixtures.must(app, targetCommand);
		String dLabel = topIntersection(app, "c", "Line((0,0),(0,1))");
		Contact contact = contact(app, "L", "P", dLabel, target, false);
		return contact;
	}

	private static Map<String, Object> syntheticWithBudget(AppGeoCeDG app, String target,
			long operations) {
		String dLabel = topIntersection(app, "c", "Line((0,0),(0,1))");
		Contact probe = contact(app, "L", "P", dLabel, target, false);
		Map<String, Object> row = new LinkedHashMap<>();
		PreG9BCu3M3Tower tower = new PreG9BCu3M3Tower(operations, BIT_BUDGET, DEPTH_BUDGET);
		PreG9BCu3M3Evaluator ev = new PreG9BCu3M3Evaluator(tower);
		try {
			ev.evaluate(probe.program, ev.unit(ev.constant(probe.driverPoint.inhomX.v),
					ev.constant(probe.driverPoint.inhomY.v)));
			row.put("outcome", "EVALUATED WITHIN THE INJECTED BUDGET");
		} catch (PreG9BCu3M3Tower.BudgetExhausted exhausted) {
			row.put("outcome", "EXACT_EVALUATION_BUDGET_EXHAUSTED: " + exhausted.getMessage());
		}
		return row;
	}

	/** @return label of the output of {@code Intersect(conic, line)} with positive ordinate */
	private static String topIntersection(AppGeoCeDG app, String conic, String line) {
		PreG9BR6PlusF3Fixtures.must(app, "v_D=" + line);
		PreG9BR6PlusF3Fixtures.must(app, "D_a=Intersect(" + conic + ",v_D,1)");
		PreG9BR6PlusF3Fixtures.must(app, "D_b=Intersect(" + conic + ",v_D,2)");
		GeoPoint a = point(app, "D_a");
		return a.isDefined() && a.getInhomY() > 0 ? "D_a" : "D_b";
	}

	// ---------------------------------------------------------------- configurations

	private static Map<String, Object> configurations() {
		Map<String, Object> rows = new LinkedHashMap<>();
		rows.put("V-CFG-1 target Circle(O,(1,-1)), incidence only in Sigma_B",
				rayConfiguration("t=Circle(O,(1,-1))", "t", true));
		rows.put("V-CFG-2 target is the captured constant c itself",
				rayConfiguration(null, "c", true));
		rows.put("V-CFG-3 target is a term-identical copy Circle(O,(1,1))",
				rayConfiguration("t=Circle(O,(1,1))", "t", false));
		rows.put("V-CFG-4 target Circle(O,(-1,1)) with the same binary64 radius",
				rayConfiguration("t=Circle(O,(-1,1))", "t", false));
		rows.put("V-CFG-5 author model", "see model.mandatory_contacts");
		rows.put("V-CFG-6 driver support used as a circle operand", representationConflict());
		return rows;
	}

	/** {@code c = Circle(O, (1, 1))}, {@code Q = Intersect(c, Ray(O, P))} on the ray. */
	private static Map<String, Object> rayConfiguration(String targetCommand, String target,
			boolean joint) {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
		for (String command : new String[] {"O=(0,0)", "c=Circle(O,(1,1))", "d=Circle(O,3)",
				"P=Point(d)", "s=Ray(O,P)", "Q=Intersect(c,s,1)"}) {
			PreG9BR6PlusF3Fixtures.must(app, command);
		}
		if (!point(app, "Q").isDefined()) {
			app.getKernel().lookupLabel("Q").remove();
			PreG9BR6PlusF3Fixtures.must(app, "Q=Intersect(c,s,2)");
		}
		PreG9BR6PlusF3Fixtures.must(app, "L=LocusV2(Q,P)");
		if (targetCommand != null) {
			PreG9BR6PlusF3Fixtures.must(app, targetCommand);
		}
		PreG9BR6PlusF3Fixtures.must(app, "v_D=Line((0,0),(1,0))");
		PreG9BR6PlusF3Fixtures.must(app, "D_a=Intersect(d,v_D,1)");
		PreG9BR6PlusF3Fixtures.must(app, "D_b=Intersect(d,v_D,2)");
		String dLabel = point(app, "D_a").isDefined() && point(app, "D_a").getInhomX() > 0
				? "D_a" : "D_b";
		Contact contact = contact(app, "L", "P", dLabel, target, joint);
		return contact.report;
	}

	/** The driver support d is also a circle operand of the program; two representations. */
	private static Map<String, Object> representationConflict() {
		Map<String, Object> row = new LinkedHashMap<>();
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
		// The line avoids P, so that P is not an existing intersection of d and m.
		for (String command : new String[] {"O=(0,0)", "d=Circle(O,(2,1))", "P=Point(d)",
				"m=OrthogonalLine((0.3,0.2),Line(O,P))", "Q=Intersect(d,m,1)",
				"L=LocusV2(Q,P)"}) {
			PreG9BR6PlusF3Fixtures.must(app, command);
		}
		ConstructionBoxModel2D box = ConstructionBoxModel2D.capture(definition(app, "L"));
		if (box == null) {
			row.put("outcome", "NO_CLASS_V3_PROGRAM");
			return row;
		}
		CertifiedConstructionProgram2D program = box.getProgram();
		PreG9BCu3M3Tower tower = new PreG9BCu3M3Tower(OPERATION_BUDGET, BIT_BUDGET,
				DEPTH_BUDGET);
		Num driverSquared = tower.of(program.getDriverConstant(0)).square();
		row.put("driver_half_axis_squared", approx(driverSquared));
		List<Object> operands = new ArrayList<>();
		boolean conflict = false;
		for (Step step : program.getSteps()) {
			for (Operand operand : step.getOperands()) {
				if (operand.getKind() == OperandKind.CIRCLE) {
					Num a4 = tower.of(operand.getValue(1));
					Num a5 = tower.of(operand.getValue(2));
					Num matrixSquared = a4.square().add(a5.square())
							.subtract(tower.of(operand.getValue(0)));
					operands.add(approx(matrixSquared) + " (minus the driver: "
							+ approx(matrixSquared.subtract(driverSquared)) + ")");
					// By construction of this case the operand is the driver support d itself.
					conflict |= matrixSquared.subtract(driverSquared).signum() != 0;
				}
			}
		}
		row.put("circle_operand_matrix_radius_squared", operands);
		row.put("outcome", operands.isEmpty() ? "NO_CIRCLE_OPERAND"
				: conflict ? "CONFIGURATION_REPRESENTATION_CONFLICT"
						: "REPRESENTATIONS_EQUAL");
		row.put("note", "the class-v3 program records no provenance of its constants; the probe "
				+ "knows by construction that the circle operand is the driver support");
		return row;
	}

	// ---------------------------------------------------------------- helpers

	private static Map<String, Object> counters(PreG9BCu3M3Tower tower, long started) {
		Map<String, Object> row = new LinkedHashMap<>();
		if (tower == null) {
			return row;
		}
		row.put("operations", tower.operations());
		row.put("additions", tower.additions);
		row.put("multiplications", tower.multiplications);
		row.put("inversions", tower.inversions);
		row.put("sign_decisions", tower.signDecisions);
		row.put("square_roots", tower.squareRoots);
		row.put("rational_square_roots", tower.rationalSquareRoots);
		row.put("tower_depth", tower.depth());
		row.put("max_bits", tower.maxBits);
		// Times are informative only and are left out so that the output is deterministic.
		return row;
	}

	private static List<Object> xy(P point) {
		return List.of(approx(point.inhomX.v), approx(point.inhomY.v));
	}

	private static String approx(Num value) {
		return value == null ? null : value.approximate().round(REPORT).toString();
	}

	private static double distance(P exact, GeoPoint live) {
		return distance(exact.inhomX.v, exact.inhomY.v, live);
	}

	private static double distance(Num x, Num y, GeoPoint live) {
		BigDecimal dx = x.approximate().subtract(new BigDecimal(live.getInhomX()));
		BigDecimal dy = y.approximate().subtract(new BigDecimal(live.getInhomY()));
		return Math.hypot(dx.doubleValue(), dy.doubleValue());
	}

	private static double projectedParameter(GeoConic conic, GeoPoint point) {
		PathParameter parameter = new PathParameter();
		conic.pointChanged(new Coords(point.getInhomX(), point.getInhomY(), 1), parameter);
		return parameter.getT();
	}

	private static LocusDefinition2D definition(AppGeoCeDG app, String label) {
		return PreG9BCu0Corpus.locus(app, label).getSemanticDefinition();
	}

	private static GeoPoint point(AppGeoCeDG app, String label) {
		return (GeoPoint) app.getKernel().lookupLabel(label);
	}

	private static GeoLine line(AppGeoCeDG app, String label) {
		return (GeoLine) app.getKernel().lookupLabel(label);
	}

	private static <T> T onEdt(Callable<T> action) throws Exception {
		return PreG9BR6PlusF3Fixtures.onEdt(action);
	}

	/** Outward-rounded binary64 interval. */
	static final class Iv {
		final double lo;
		final double hi;

		Iv(double lo, double hi) {
			this.lo = lo;
			this.hi = hi;
		}

		static Iv of(double value) {
			return new Iv(value, value);
		}

		static Iv of(double[] pair) {
			return new Iv(pair[0], pair[1]);
		}

		Iv plus(Iv o) {
			return new Iv(Math.nextDown(lo + o.lo), Math.nextUp(hi + o.hi));
		}

		Iv minus(Iv o) {
			return new Iv(Math.nextDown(lo - o.hi), Math.nextUp(hi - o.lo));
		}

		Iv times(Iv o) {
			double a = lo * o.lo;
			double b = lo * o.hi;
			double c = hi * o.lo;
			double d = hi * o.hi;
			return new Iv(Math.nextDown(Math.min(Math.min(a, b), Math.min(c, d))),
					Math.nextUp(Math.max(Math.max(a, b), Math.max(c, d))));
		}

		Iv sq() {
			if (lo >= 0) {
				return new Iv(Math.nextDown(lo * lo), Math.nextUp(hi * hi));
			}
			if (hi <= 0) {
				return new Iv(Math.nextDown(hi * hi), Math.nextUp(lo * lo));
			}
			return new Iv(0, Math.nextUp(Math.max(lo * lo, hi * hi)));
		}
	}
}
