/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundaryImage2D;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU2 step T11, normative precision D-CU2-T11-R1, on the author model: the slice of a
 * revision rebuilt with the driver where the copy of {@code Q/R} cannot finish its first
 * initialization (inside the gap, or at the double root {@code t_c} on the bite side of the
 * critical value) is settled to the live selection state, which the accessors of both
 * algorithms show equal; the certificate is the one of the same configuration rebuilt with the
 * driver at {@code t = -1}; the live construction and the semantic identities are untouched.
 * Matrix rows V-REV-EXACT-1 (driver at {@code t = -1}, at {@code t_c} and inside the gap),
 * V-SEL-1 and V-PUB-1 (state part). Corrective descendant T13-R2 (decision D-CU2-T13-R2): the
 * model configurations of V-COINC-6 and V-COINC-9 that matrix section 3.5c requires.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu2AuthorModelSelectionSettlementTest {
	private static final List<String> FIRST_FAMILY = List.of("o", "v", "w", "a_1");
	/** Penetration and bite sides of the critical value of {@code excCyl}, one ulp apart. */
	private static final double PENETRATION = Double.longBitsToDouble(0x400d416bb67a38bbL);
	private static final double BITE = Double.longBitsToDouble(0x400d416bb67a38bcL);
	/** {@code t_c} of the matrix (section 1). */
	private static final double T_C = 1.2767651295;
	/** Saved parameter of {@code N}, inside the gap at excCyl 4.0 ({@code pins.H01}). */
	private static final double SAVED = 0.6456498832185171;
	/** Analytic ends of the gap at excCyl 3.7 (analysis section 3.3). */
	private static final double GAP_LOWER = 1.0418225300;
	private static final double GAP_UPPER = 1.5117077290;
	/** Variants of matrix section 3.5c: labelled outputs Q and R, unlabelled second R2. */
	private static final List<String> VARIANTS = List.of("lq", "lr", "lr2");
	private static final List<String> OUTPUTS = List.of("Q", "R", "R2");
	/** Root that the certified selection designates for each output, by ray parameter. */
	private static final List<Integer> ROOTS = List.of(0, 1, 1);

	@TempDir
	Path work;

	/**
	 * V-REV-EXACT-1 with the driver at {@code t = -1}, at {@code t_c} and inside the gap:
	 * every one-ulp change publishes one revision whose copy holds the exact bits of
	 * {@code excCyl}, reproduces the live selection state of {@code Q/R} on the accessors
	 * (settling it where the driver leaves the copy in its first initialization) and has the
	 * certificate of the same state reached with the driver at {@code t = -1}.
	 */
	@Test
	void oneUlpChangesAtEveryDriverPositionReproduceTheSelection() throws Exception {
		List<String> failures = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			List<String> problems = new ArrayList<>();
			Map<Double, String> reference = new LinkedHashMap<>();
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			for (double value : new double[] {PENETRATION, BITE, 4.0, Math.nextUp(4.0)}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", value);
				reference.put(value, signatures(app));
				check(app, problems, "t=-1 " + value, value, "not-needed");
			}
			PreG9BCu0Corpus.movePathPoint(app, "N", T_C);
			for (double value : new double[] {BITE, PENETRATION, BITE}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", value);
				check(app, problems, "t_c " + value, value, null);
				compare(problems, "t_c " + value, reference.get(value), signatures(app));
			}
			PreG9BCu0Corpus.setNumber(app, "excCyl", 4.0);
			PreG9BCu0Corpus.movePathPoint(app, "N", SAVED);
			if (app.getKernel().lookupLabel("Q").isDefined()) {
				problems.add("the saved driver is not in the gap");
			}
			for (double value : new double[] {Math.nextUp(4.0), 4.0, Math.nextUp(4.0)}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", value);
				check(app, problems, "gap " + value, value, "settled");
				compare(problems, "gap " + value, reference.get(value), signatures(app));
			}
			return problems;
		});
		assertEquals(List.of(), failures);
	}

	/**
	 * Contract steps 5 and 6 on the author model: the settlement of a revision rebuilt in the
	 * gap computes one auxiliary parameter per locus, reads the live construction only (the
	 * live selection state, the driver and every value are unchanged) and leaves the
	 * persistent locus identities and the component signatures equal to those of the same
	 * revision rebuilt outside the gap.
	 */
	@Test
	void theSettlementTouchesNeitherTheLiveConstructionNorTheIdentities() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			Map<String, Object> values = new LinkedHashMap<>();
			Map<String, String> identities = new LinkedHashMap<>();
			Map<String, Map<LocusCertificationWork2D, Long>> before = new LinkedHashMap<>();
			for (String label : FIRST_FAMILY) {
				identities.put(label, PreG9BCu0Corpus.locus(app, label).getPersistentLocusId()
						.toExternalForm());
				before.put(label, counters(PreG9BCu0Corpus.locus(app, label)));
			}
			String liveBefore = state(live(app));
			// excCyl moves the path of N: its coordinates follow, its parameter does not.
			String driverBefore = parameter((GeoPoint) app.getKernel().lookupLabel("N"));
			PreG9BCu0Corpus.setNumber(app, "excCyl", 4.0);
			values.put("live", liveBefore.equals(state(live(app))));
			values.put("driver", driverBefore.equals(parameter((GeoPoint) app.getKernel()
					.lookupLabel("N"))));
			StringBuilder settlement = new StringBuilder();
			for (String label : FIRST_FAMILY) {
				GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
				ReconstructibleLocusEvaluator2D evaluator = evaluator(locus);
				settlement.append(label).append('=')
						.append(evaluator.getSettlementOutcome().getToken())
						.append('/').append(delta(before.get(label), locus,
								LocusCertificationWork2D.SELECTION_SETTLEMENT))
						.append('/').append(delta(before.get(label), locus,
								LocusCertificationWork2D.SELECTION_SETTLEMENT_PARAMETER))
						.append('/').append(delta(before.get(label), locus,
								LocusCertificationWork2D.SELECTION_SETTLEMENT_FAILURE))
						.append(';');
				values.put("identity." + label, identities.get(label).equals(
						locus.getPersistentLocusId().toExternalForm()));
			}
			values.put("work", settlement.toString());
			String settled = signatures(app);
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			PreG9BCu0Corpus.setNumber(app, "excCyl", 4.5);
			PreG9BCu0Corpus.setNumber(app, "excCyl", 4.0);
			values.put("signatures", settled.equals(signatures(app)));
			return values;
		});
		assertEquals(true, row.get("live"), row.toString());
		assertEquals(true, row.get("driver"), row.toString());
		assertEquals("o=settled/1/1/0;v=settled/1/1/0;w=settled/1/1/0;a_1=settled/1/1/0;",
				row.get("work"), row.toString());
		for (String label : FIRST_FAMILY) {
			assertEquals(true, row.get("identity." + label), row.toString());
		}
		assertEquals(true, row.get("signatures"), row.toString());
	}

	/**
	 * V-COINC-6 on the author model (matrix section 3.5c; corrective descendant T13-R2): the
	 * labelled outputs {@code {Q, R}} of the model and an unlabelled second output
	 * {@code R2 = Intersect(p, t2, 2)} of {@code t2 = Ray(P, N)}, traced by {@code lq},
	 * {@code lr} and {@code lr2}, saved and reopened with the driver in the gap of the bite
	 * at excCyl 3.7, capture their selection rule without a valid sample: the outputs are
	 * undefined, the intersection stays in its first initialization, the copies reproduce it
	 * or depart from it with a typed departure and need no settlement, the rule captured is the
	 * compaction in raw order, and the certificates are those of the same document with the
	 * driver at {@code t = -1}; reopened again, the same. At the last binary64 of penetration
	 * there is no gap, and at the first binary64 of the bite the exact gap, 2.4e-8 wide, lies
	 * inside the inherited tangency window (finding F-CU2-1): the labelled output exists at
	 * {@code t_c}, so no driver starts there without intersection. An output undefined when it
	 * is created cannot be labelled, so the variants are started in the gap by reopening.
	 */
	@Test
	void theModelVariantsStartedInTheGapCaptureTheSameCertificates() throws Exception {
		List<String> failures = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			List<String> problems = new ArrayList<>();
			AppGeoCeDG bite = variants(BITE);
			PreG9BCu0Corpus.movePathPoint(bite, "N", T_C);
			if (!bite.getKernel().lookupLabel("Q").isDefined()) {
				problems.add("excCyl=" + BITE + ": Q is undefined at t_c");
			}
			for (double excCyl : new double[] {3.7}) {
				AppGeoCeDG app = variants(excCyl);
				Map<String, String> reference = certificates(app);
				PreG9BCu0Corpus.movePathPoint(app, "N", T_C);
				for (String pass : List.of("started", "reopened")) {
					app = PreG9BCu0Corpus.open(PreG9BR6PlusF3Fixtures.save(app, work,
							"gap-" + Double.toHexString(excCyl) + "-" + pass));
					String context = "excCyl=" + excCyl + " " + pass + " in the gap";
					if (app.getKernel().lookupLabel("Q").isDefined()
							|| app.getKernel().lookupLabel("R2").isDefined()) {
						problems.add(context + ": an output is defined in the gap");
					}
					if (!live(app).isSelectionFirstIntersectionPending()) {
						problems.add(context + ": the live intersection left its first "
								+ "initialization " + state(live(app)));
					}
					for (int variant = 0; variant < VARIANTS.size(); variant++) {
						ReconstructibleLocusEvaluator2D evaluator = evaluator(
								PreG9BCu0Corpus.locus(app, VARIANTS.get(variant)));
						evaluator.captureCertifiedProgram();
						if (!evaluator.isReconstructionFaithful()
								|| !"not-needed".equals(evaluator.getSettlementOutcome()
										.getToken())) {
							problems.add(context + " " + VARIANTS.get(variant) + ": "
									+ evaluator.getFidelityReport() + " "
									+ evaluator.getSettlementOutcome());
						}
						if (variant < 2) {
							String copyState = state((AlgoIntersectLineConic) copy(evaluator)
									.lookupLabel("Q").getParentAlgorithm());
							if (!copyState.equals(state(live(app)))
									&& !("[0, 1]/false/false".equals(copyState)
											&& List.of("Q").equals(evaluator.getFidelityReport()
													.getDepartedSelections()))) {
								problems.add(context + " " + VARIANTS.get(variant)
										+ ": copy state " + copyState);
							}
						}
					}
					if (compaction(app) != 1) {
						problems.add(context + ": the captured rule is not the compaction");
					}
					if (!reference.equals(certificates(app))) {
						problems.add(context + ": certificates differ from the driver at t = -1 "
								+ certificates(app) + " versus " + reference);
					}
				}
			}
			return problems;
		});
		assertEquals(List.of(), failures);
	}

	/**
	 * V-COINC-9 on the author model (matrix section 3.5c; corrective descendant T13-R2): at
	 * the last binary64 of penetration, the first of the bite and excCyl 3.7, with the variants
	 * of V-COINC-6 created with the driver at {@code t = -1} and started again by reopening with
	 * the driver at {@code t_c} and at 0.3, in every sample outside the unresolved bands the
	 * operative output of the live construction and of the slice is the root that the
	 * certified selection designates ({@code Q} the smaller ray parameter, {@code R} and
	 * {@code R2} the larger), against a 60-digit reference computed from the live binary64
	 * inputs of the ray and of the circle; where the two reference roots are closer than the
	 * error of a binary64 computation the output is that coincident root. Certified existence
	 * has a slice output and an undefined slice output is never published. At each certified
	 * end of {@code lq}, a simple root where the two roots coincide, the certified limit value
	 * encloses the 60-digit foot of the double root and is evidence only: at the binary64
	 * middle of its bracket the unlabelled output is the live one, and where it is undefined
	 * neither the slice nor the published evaluation gives a point.
	 */
	@Test
	void theModelOutputsAreTheRootsThatTheCertifiedSelectionDesignates() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			List<String> problems = new ArrayList<>();
			int compared = 0;
			int ends = 0;
			for (double excCyl : new double[] {PENETRATION, BITE, 3.7}) {
				// Each configuration retires the embedded hosts it opened (heap of the
				// final selection).
				try (G9U1TestApp.GlobalRoots roots = G9U1TestApp.GlobalRoots.capture()) {
					AppGeoCeDG app = variants(excCyl);
					for (double start : new double[] {Double.NaN, T_C, 0.3}) {
						String context = "excCyl=" + excCyl + (Double.isNaN(start) ? " created"
								: " started at " + start);
						if (!Double.isNaN(start)) {
							PreG9BCu0Corpus.movePathPoint(app, "N", start);
							app = PreG9BCu0Corpus.open(PreG9BR6PlusF3Fixtures.save(app, work,
									"start-" + Double.toHexString(excCyl) + "-" + start));
						}
						compared += designations(app, context, excCyl, problems);
						ends += limitValues(app, context, problems);
					}
				}
			}
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("problems", problems);
			values.put("compared", compared);
			values.put("ends", ends);
			return values;
		});
		assertEquals(List.of(), row.get("problems"));
		assertTrue((Integer) row.get("compared") > 5000, "compared outputs: " + row);
		assertTrue((Integer) row.get("ends") >= 12, "certified ends: " + row);
	}

	/** The model at excCyl with the driver at t = -1 and the two variants of section 3.5c. */
	private AppGeoCeDG variants(double excCyl) throws Exception {
		AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
		PreG9BCu0Corpus.movePathPoint(app, "N", -1);
		PreG9BCu0Corpus.setNumber(app, "excCyl", excCyl);
		for (String command : List.of("lq=LocusV2(Q,N)", "lr=LocusV2(R,N)", "t2=Ray(P,N)",
				"R2=Intersect(p,t2,2)", "lr2=LocusV2(R2,N)")) {
			app.getKernel().getAlgebraProcessor().processAlgebraCommand(command, false);
		}
		app.getKernel().updateConstruction(false);
		return app;
	}

	/** @return structure, roots and exact evidence of each variant */
	private static Map<String, String> certificates(AppGeoCeDG app) throws Exception {
		Map<String, String> certificates = new LinkedHashMap<>();
		for (String label : VARIANTS) {
			HybridExistenceCertifier2D.Result result = ((AlgoDependentPointLocusV2) PreG9BCu0Corpus
					.locus(app, label).getParentAlgorithm()).getHybridExistenceCertificate();
			certificates.put(label, result.getStructure().getSemanticSignature() + "|roots="
					+ result.getRoots() + "|exact=" + result.getExactEvidence());
		}
		return certificates;
	}

	/** @return the compaction flag of the line-circle step that {@code lq} captured */
	private static double compaction(AppGeoCeDG app) throws Exception {
		for (CertifiedConstructionProgram2D.Step step : evaluator(PreG9BCu0Corpus.locus(app,
				"lq")).captureCertifiedProgramV3().orElseThrow().getSteps()) {
			if (step.getOperation() == CertifiedConstructionProgram2D.Operation.LINE_CIRCLE) {
				return step.getParameter(0);
			}
		}
		return Double.NaN;
	}

	/** @return number of compared outputs; records every output that is not designated */
	private static int designations(AppGeoCeDG app, String context, double excCyl,
			List<String> problems) throws Exception {
		List<Double> samples = new ArrayList<>();
		for (int index = 0; index < 64; index++) {
			samples.add(-Math.PI + 2 * Math.PI * (index + 0.5) / 64);
		}
		List<Double> centres = excCyl == 3.7 ? List.of(T_C, GAP_LOWER, GAP_UPPER)
				: List.of(T_C);
		for (double centre : centres) {
			for (int index = -100; index <= 100; index++) {
				samples.add(centre + index * 1e-10);
			}
		}
		GeoPoint driver = (GeoPoint) app.getKernel().lookupLabel("N");
		int compared = 0;
		for (int variant = 0; variant < VARIANTS.size(); variant++) {
			GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, VARIANTS.get(variant));
			LocusExistenceStructure2D structure = locus.getSemanticDefinition()
					.getBranch(PreG9BCu0Corpus.MAIN).getExistenceStructure();
			ReconstructibleLocusEvaluator2D slice = evaluator(locus);
			try (LocusEvaluationSession2D copySession = LocusEvaluationSession2D.memoizing(8);
					LocusEvaluationSession2D publishedSession =
							LocusEvaluationSession2D.memoizing(8)) {
				for (double parameter : samples) {
					LocusExistenceStructure2D.CoverageState state = coverageState(structure,
							parameter);
					if (state == LocusExistenceStructure2D.CoverageState.UNRESOLVED) {
						continue;
					}
					PreG9BCu0Corpus.movePathPoint(app, "N", parameter);
					Reference reference = Reference.of(app, driver);
					String where = context + " " + VARIANTS.get(variant) + " t=" + parameter;
					GeoPoint live = (GeoPoint) app.getKernel().lookupLabel(OUTPUTS.get(variant));
					LocusEvaluation2D copy = slice.evaluate(null, null, parameter, copySession);
					compared += reference.check(where + " live", live.isDefined()
							? new double[] {live.getInhomX(), live.getInhomY()} : null,
							ROOTS.get(variant), problems);
					compared += reference.check(where + " slice", copy.isValid()
							? new double[] {copy.getPoint().getX(), copy.getPoint().getY()}
							: null, ROOTS.get(variant), problems);
					if (state == LocusExistenceStructure2D.CoverageState.CERTIFIED_EXISTS
							&& !copy.isValid()) {
						problems.add(where + ": certified existence without a slice output");
					}
					if (!copy.isValid() && locus.getSemanticDefinition().evaluate(
							PreG9BCu0Corpus.MAIN, parameter, publishedSession).isValid()) {
						problems.add(where + ": an undefined slice output is published");
					}
				}
			}
		}
		return compared;
	}

	/**
	 * At each certified end of {@code lq} the roots coincide: its limit value encloses the
	 * 60-digit foot of the double root. At the binary64 middle of the bracket the roots are
	 * apart by far more than the suppression tolerance of the unlabelled output, which is then
	 * defined and is what the slice of {@code lr2} gives; where it is undefined, nothing is
	 * given in its place.
	 *
	 * @return number of checked ends
	 */
	private static int limitValues(AppGeoCeDG app, String context, List<String> problems)
			throws Exception {
		GeoLocusV2 lq = PreG9BCu0Corpus.locus(app, "lq");
		GeoLocusV2 lr2 = PreG9BCu0Corpus.locus(app, "lr2");
		LocusDefinition2D definition = lq.getSemanticDefinition();
		GeoPoint driver = (GeoPoint) app.getKernel().lookupLabel("N");
		int ends = 0;
		for (LocusExistenceStructure2D.ContinuousComponent component : definition
				.getBranch(PreG9BCu0Corpus.MAIN).getExistenceStructure()
				.getContinuousValidComponents()) {
			for (LocusBoundary2D end : List.of(component.getLowerBoundary(),
					component.getUpperBoundary())) {
				if (end.getKind() != LocusBoundary2D.Kind.CERTIFIED_TRANSITION) {
					continue;
				}
				ends++;
				double parameter = end.getEnclosureLower()
						+ (end.getEnclosureUpper() - end.getEnclosureLower()) / 2;
				LocusBoundaryImage2D image = definition.encloseBoundaryImage(
						PreG9BCu0Corpus.MAIN, end);
				PreG9BCu0Corpus.movePathPoint(app, "N", parameter);
				Reference reference = Reference.of(app, driver);
				if (!image.isEnclosed() || !reference.isFootEnclosedNear(image)) {
					problems.add(context + ": the limit value of " + end
							+ " does not enclose the double root " + image);
				}
				GeoPoint live = (GeoPoint) app.getKernel().lookupLabel("R2");
				try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4)) {
					LocusEvaluation2D slice = evaluator(lr2).evaluate(null, null, parameter,
							session);
					boolean published = lr2.getSemanticDefinition().evaluate(
							PreG9BCu0Corpus.MAIN, parameter, session).isValid();
					if (!live.isDefined() && (slice.isValid() || published)) {
						problems.add(context + ": the limit value replaces an undefined output "
								+ end);
					}
					if (live.isDefined() && (!slice.isValid() || Math.hypot(
							slice.getPoint().getX() - live.getInhomX(), slice.getPoint().getY()
									- live.getInhomY()) > Reference.TOLERANCE)) {
						problems.add(context + ": the slice of lr2 is not the live output at "
								+ end + " " + slice);
					}
				}
			}
		}
		return ends;
	}

	private static LocusExistenceStructure2D.CoverageState coverageState(
			LocusExistenceStructure2D structure, double parameter) {
		for (LocusExistenceStructure2D.Coverage coverage : structure.getCoverage()) {
			if (coverage.getInterval().contains(parameter, 0)) {
				return coverage.getState();
			}
		}
		throw new IllegalStateException("No coverage at " + parameter);
	}

	/**
	 * Extended-precision roots of the ray from the live point P through the live driver with
	 * the circle of the live centre B and radius rbCone of the model, in increasing ray
	 * parameter, and the foot of the perpendicular from the centre, the limit of a double
	 * root.
	 */
	private static final class Reference {
		private static final MathContext MC = new MathContext(60, RoundingMode.HALF_EVEN);
		/** Error bound of a binary64 computation of a root of this configuration. */
		private static final double TOLERANCE = 1e-6;
		private final double[][] roots;
		private final double[] foot;

		private Reference(double[][] roots, double[] foot) {
			this.roots = roots;
			this.foot = foot;
		}

		static Reference of(AppGeoCeDG app, GeoPoint driver) {
			GeoPoint start = (GeoPoint) app.getKernel().lookupLabel("P");
			GeoPoint centre = (GeoPoint) app.getKernel().lookupLabel("B");
			BigDecimal radius = new BigDecimal(((GeoNumeric) app.getKernel()
					.lookupLabel("rbCone")).getValue());
			BigDecimal px = new BigDecimal(start.getInhomX());
			BigDecimal py = new BigDecimal(start.getInhomY());
			BigDecimal dx = new BigDecimal(driver.getInhomX()).subtract(px, MC);
			BigDecimal dy = new BigDecimal(driver.getInhomY()).subtract(py, MC);
			BigDecimal wx = px.subtract(new BigDecimal(centre.getInhomX()), MC);
			BigDecimal wy = py.subtract(new BigDecimal(centre.getInhomY()), MC);
			BigDecimal a = dx.multiply(dx, MC).add(dy.multiply(dy, MC), MC);
			BigDecimal b = wx.multiply(dx, MC).add(wy.multiply(dy, MC), MC)
					.multiply(BigDecimal.valueOf(2), MC);
			BigDecimal c = wx.multiply(wx, MC).add(wy.multiply(wy, MC), MC)
					.subtract(radius.multiply(radius, MC), MC);
			BigDecimal twice = a.multiply(BigDecimal.valueOf(2), MC);
			BigDecimal limit = b.negate().divide(twice, MC);
			double[] foot = {px.add(limit.multiply(dx, MC), MC).doubleValue(),
				py.add(limit.multiply(dy, MC), MC).doubleValue()};
			BigDecimal discriminant = b.multiply(b, MC).subtract(a.multiply(c, MC)
					.multiply(BigDecimal.valueOf(4), MC), MC);
			if (discriminant.signum() < 0) {
				return new Reference(null, foot);
			}
			BigDecimal root = discriminant.sqrt(MC);
			BigDecimal[] lambdas = {b.negate().subtract(root, MC).divide(twice, MC),
				b.negate().add(root, MC).divide(twice, MC)};
			double[][] points = new double[2][];
			for (int index = 0; index < 2; index++) {
				points[index] = new double[] {
					px.add(lambdas[index].multiply(dx, MC), MC).doubleValue(),
					py.add(lambdas[index].multiply(dy, MC), MC).doubleValue(),
					lambdas[index].doubleValue()};
			}
			return new Reference(points, foot);
		}

		/** @return 1 when an output was compared with the designated root, else 0 */
		int check(String where, double[] output, int designated, List<String> problems) {
			if (output == null || roots == null) {
				return 0;
			}
			if (roots[0][2] < 0) {
				problems.add(where + ": a reference root lies behind P");
			}
			boolean coincident = distance(roots[0], roots[1]) < 4 * TOLERANCE;
			double error = distance(roots[coincident ? 0 : designated], output);
			if (error > (coincident ? 4 * TOLERANCE : TOLERANCE)) {
				problems.add(where + ": output " + Arrays.toString(output) + " is not root "
						+ designated + " of " + Arrays.deepToString(roots));
			}
			return 1;
		}

		/** @return whether the image encloses the foot up to the tolerance */
		boolean isFootEnclosedNear(LocusBoundaryImage2D image) {
			return foot[0] >= image.getXLower() - 4 * TOLERANCE
					&& foot[0] <= image.getXUpper() + 4 * TOLERANCE
					&& foot[1] >= image.getYLower() - 4 * TOLERANCE
					&& foot[1] <= image.getYUpper() + 4 * TOLERANCE;
		}

		private static double distance(double[] first, double[] second) {
			return Math.hypot(first[0] - second[0], first[1] - second[1]);
		}
	}

	private static void check(AppGeoCeDG app, List<String> problems, String step,
			double value, String outcome) throws Exception {
		GeoNumeric excCyl = (GeoNumeric) app.getKernel().lookupLabel("excCyl");
		if (Double.doubleToRawLongBits(excCyl.getValue()) != Double.doubleToRawLongBits(value)) {
			problems.add(step + " excCyl is not exact");
		}
		AlgoIntersectLineConic live = live(app);
		for (String label : FIRST_FAMILY) {
			ReconstructibleLocusEvaluator2D evaluator = evaluator(PreG9BCu0Corpus.locus(app,
					label));
			GeoElement copyValue = copy(evaluator).lookupLabel("excCyl");
			if (!evaluator.isReconstructionFaithful()) {
				problems.add(step + " " + label + " unfaithful " + evaluator.getFidelityReport());
				continue;
			}
			if (Double.doubleToRawLongBits(((GeoNumeric) copyValue).getValue())
					!= Double.doubleToRawLongBits(value)) {
				problems.add(step + " " + label + " copy of excCyl is not exact");
			}
			AlgoIntersectLineConic copy = (AlgoIntersectLineConic) copy(evaluator)
					.lookupLabel("Q").getParentAlgorithm();
			if (!state(live).equals(state(copy))) {
				problems.add(step + " " + label + " live " + state(live) + " copy "
						+ state(copy));
			}
			if (outcome != null && !outcome.equals(evaluator.getSettlementOutcome().getToken())) {
				problems.add(step + " " + label + " settlement "
						+ evaluator.getSettlementOutcome());
			}
			LocusExistenceStructure2D structure = PreG9BCu0Corpus.locus(app, label)
					.getSemanticDefinition().getBranch(PreG9BCu0Corpus.MAIN)
					.getExistenceStructure();
			if (structure.getCompleteness() != LocusExistenceStructure2D.Completeness.COMPLETE) {
				problems.add(step + " " + label + " " + structure.getCompleteness());
			}
		}
	}

	private static void compare(List<String> problems, String step, String expected,
			String actual) {
		if (!expected.equals(actual)) {
			problems.add(step + " certificate differs from the driver at t = -1");
		}
	}

	/** @return semantic signatures of the published certificates of the first family */
	private static String signatures(AppGeoCeDG app) {
		StringBuilder text = new StringBuilder();
		for (String label : FIRST_FAMILY) {
			text.append(label).append('=').append(PreG9BCu0Corpus.locus(app, label)
					.getSemanticDefinition().getBranch(PreG9BCu0Corpus.MAIN)
					.getExistenceStructure().getSemanticSignature()).append('\n');
		}
		return text.toString();
	}

	private static Map<LocusCertificationWork2D, Long> counters(GeoLocusV2 locus) {
		Map<LocusCertificationWork2D, Long> counters = new LinkedHashMap<>();
		for (LocusCertificationWork2D kind : LocusCertificationWork2D.values()) {
			counters.put(kind, locus.getInstrumentation().getCertificationWork(kind));
		}
		return counters;
	}

	private static long delta(Map<LocusCertificationWork2D, Long> before, GeoLocusV2 locus,
			LocusCertificationWork2D kind) {
		return locus.getInstrumentation().getCertificationWork(kind) - before.get(kind);
	}

	private static AlgoIntersectLineConic live(AppGeoCeDG app) {
		return (AlgoIntersectLineConic) app.getKernel().lookupLabel("Q").getParentAlgorithm();
	}

	private static String state(AlgoIntersectLineConic intersection) {
		return Arrays.toString(intersection.getSelectionPermutation()) + "/"
				+ intersection.isSelectionFirstIntersectionPending() + "/"
				+ intersection.isSelectionPermutationPending();
	}

	private static String parameter(GeoPoint point) {
		return Double.toHexString(point.getPathParameter().getT());
	}

	private static Construction copy(ReconstructibleLocusEvaluator2D evaluator)
			throws ReflectiveOperationException {
		Field field = ReconstructibleLocusEvaluator2D.class.getDeclaredField(
				"isolatedConstruction");
		field.setAccessible(true);
		return (Construction) field.get(evaluator);
	}

	private static ReconstructibleLocusEvaluator2D evaluator(GeoLocusV2 locus)
			throws ReflectiveOperationException {
		Field field = AlgoDependentPointLocusV2.class.getDeclaredField("evaluator");
		field.setAccessible(true);
		return (ReconstructibleLocusEvaluator2D) field.get(locus.getParentAlgorithm());
	}
}
