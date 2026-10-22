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
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectSingle;
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
	 * {@code lr} and {@code lr2}, started with the driver where the traced output has no valid
	 * sample: at the last binary64 of penetration, the first of the 201 drivers within 10^-8 of
	 * {@code t_c} where the inherited rule suppresses R2; at the first binary64 of the bite,
	 * {@code t_c}, where R2 is suppressed and the labelled outputs exist (finding F-CU2-1: the
	 * exact gap, 2.4e-8 wide, lies inside the inherited tangency window); at excCyl 3.7,
	 * {@code t_c}, inside the gap, where no output exists. Saved and reopened with the driver
	 * there, and reopened again, both intersections stay in their first initialization, which
	 * each copy reproduces with no settlement or leaves with a typed departure; the rule
	 * captured is the compaction in raw order, the rule of the same document with the driver
	 * at {@code t = -1} (the permutation [0, 1], both outputs labelled or both unlabelled) in
	 * its other mode; the certificates are those of that document, bit for bit and durable
	 * identities included. Created by command with the driver there in the model, the
	 * unlabelled variant captures the same compaction, and the loci of the labelled outputs of
	 * the model settle to their permutation; their certificates are the same up to the durable
	 * identities of the new geos, named by their labels or algorithms.
	 */
	@Test
	void theModelVariantsStartedInTheGapCaptureTheSameCertificates() throws Exception {
		List<String> failures = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			List<String> problems = new ArrayList<>();
			for (double excCyl : new double[] {PENETRATION, BITE, 3.7}) {
				// Each configuration retires the embedded hosts it opened.
				try (G9U1TestApp.GlobalRoots roots = G9U1TestApp.GlobalRoots.capture()) {
					startedWithoutSample(excCyl, problems);
				}
			}
			return problems;
		});
		assertEquals(List.of(), failures);
	}

	/** V-COINC-6 in one configuration of the model; records every problem. */
	private void startedWithoutSample(double excCyl, List<String> problems) throws Exception {
		AppGeoCeDG app = variants(excCyl);
		Map<String, String> reference = certificates(app);
		Map<String, String> named = new LinkedHashMap<>();
		Map<String, String> rules = new LinkedHashMap<>();
		for (String label : VARIANTS) {
			named.put(label, PreG9BCu2AuthorModelSliceFidelityTest.named(reference.get(label),
					app.getKernel().getConstruction()));
			rules.put(label, evaluator(PreG9BCu0Corpus.locus(app, label))
					.captureCertifiedProgramV3().orElseThrow().getSelectionSignature());
		}
		Map<String, String> permutations = new LinkedHashMap<>();
		permutations.put("lq", "2LINE_CIRCLE[0x0.0p0,0x0.0p0,0x1.0p0,0x1.0p0,0x1.0p0];"
				+ "3INTERSECTION_OUTPUT[0x0.0p0];");
		permutations.put("lr", "2LINE_CIRCLE[0x0.0p0,0x0.0p0,0x1.0p0,0x1.0p0,0x1.0p0];"
				+ "3INTERSECTION_OUTPUT[0x1.0p0];");
		permutations.put("lr2", "2LINE_CIRCLE[0x0.0p0,0x0.0p0,0x1.0p0,0x0.0p0,0x0.0p0];"
				+ "3INTERSECTION_OUTPUT[0x1.0p0];");
		if (!permutations.equals(rules)) {
			problems.add("excCyl=" + excCyl + ": rules at t = -1 " + rules);
			return;
		}
		double start = T_C;
		if (excCyl == PENETRATION) {
			start = Double.NaN;
			for (int index = -100; index <= 100 && Double.isNaN(start); index++) {
				PreG9BCu0Corpus.movePathPoint(app, "N", T_C + index * 1e-10);
				if (!app.getKernel().lookupLabel("R2").isDefined()) {
					start = T_C + index * 1e-10;
				}
			}
			if (Double.isNaN(start)) {
				problems.add("excCyl=" + excCyl + ": R2 is defined at the 201 drivers");
				return;
			}
		}
		PreG9BCu0Corpus.movePathPoint(app, "N", start);
		// The labelled outputs exist at the start except in the gap of excCyl 3.7.
		boolean labelled = excCyl != 3.7;
		for (String pass : List.of("started", "reopened")) {
			app = PreG9BCu0Corpus.open(PreG9BR6PlusF3Fixtures.save(app, work,
					"start-" + Double.toHexString(excCyl) + "-" + pass));
			String context = "excCyl=" + excCyl + " " + pass + " at " + start;
			if (app.getKernel().lookupLabel("Q").isDefined() != labelled
					|| app.getKernel().lookupLabel("R").isDefined() != labelled
					|| app.getKernel().lookupLabel("R2").isDefined()) {
				problems.add(context + ": outputs Q, R, R2 defined "
						+ app.getKernel().lookupLabel("Q").isDefined() + ", "
						+ app.getKernel().lookupLabel("R").isDefined() + ", "
						+ app.getKernel().lookupLabel("R2").isDefined());
			}
			if (!live(app).isSelectionFirstIntersectionPending()
					|| !source(app).isSelectionFirstIntersectionPending()) {
				problems.add(context + ": the live intersections left their first "
						+ "initialization " + state(live(app)) + " " + state(source(app)));
			}
			for (String label : VARIANTS) {
				captured(app, context, label, "not-needed", compaction(rules.get(label)),
						problems);
			}
			if (!reference.equals(certificates(app))) {
				problems.add(context + ": certificates differ from the driver at t = -1 "
						+ certificates(app) + " versus " + reference);
			}
		}
		AppGeoCeDG created = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
		PreG9BCu0Corpus.movePathPoint(created, "N", -1);
		PreG9BCu0Corpus.setNumber(created, "excCyl", excCyl);
		PreG9BCu0Corpus.movePathPoint(created, "N", start);
		for (String command : List.of("t2=Ray(P,N)", "R2=Intersect(p,t2,2)",
				"lr2=LocusV2(R2,N)", "lq=LocusV2(Q,N)", "lr=LocusV2(R,N)")) {
			created.getKernel().getAlgebraProcessor().processAlgebraCommand(command, false);
		}
		String context = "excCyl=" + excCyl + " created at " + start;
		if (created.getKernel().lookupLabel("R2").isDefined()
				|| !source(created).isSelectionFirstIntersectionPending()) {
			problems.add(context + ": R2 " + created.getKernel().lookupLabel("R2").isDefined()
					+ " " + state(source(created)));
		}
		Map<String, String> found = certificates(created);
		for (String label : VARIANTS) {
			boolean indexed = "lr2".equals(label);
			captured(created, context, label, indexed ? "not-needed" : "settled",
					indexed ? compaction(rules.get(label)) : rules.get(label), problems);
			String certificate = PreG9BCu2AuthorModelSliceFidelityTest.named(found.get(label),
					created.getKernel().getConstruction());
			if (!named.get(label).equals(certificate)) {
				problems.add(context + " " + label + ": certificate " + certificate
						+ " versus " + named.get(label));
			}
		}
	}

	/**
	 * Records a problem unless the copy of the locus, validated again, is faithful with the
	 * settlement outcome; reproduces the selection state of the live intersection, or a point
	 * query of the certification took it out of the first initialization that it reproduced,
	 * which the report types as a departure named by the first output of the intersection; and
	 * the rule captured for the revision is the given one.
	 */
	private static void captured(AppGeoCeDG app, String context, String label, String outcome,
			String rule, List<String> problems) throws Exception {
		String where = context + " " + label;
		ReconstructibleLocusEvaluator2D evaluator = evaluator(PreG9BCu0Corpus.locus(app, label));
		evaluator.captureCertifiedProgram();
		if (!evaluator.isReconstructionFaithful()
				|| !outcome.equals(evaluator.getSettlementOutcome().getToken())) {
			problems.add(where + ": " + evaluator.getFidelityReport() + " "
					+ evaluator.getSettlementOutcome());
		}
		boolean indexed = "lr2".equals(label);
		AlgoIntersectLineConic live = indexed ? source(app) : live(app);
		AlgoElement parent = copy(evaluator).lookupLabel(indexed ? "R2" : "Q")
				.getParentAlgorithm();
		AlgoIntersectLineConic copy = (AlgoIntersectLineConic) (indexed
				? ((AlgoIntersectSingle) parent).getSourceIntersection() : parent);
		if (!state(live).equals(state(copy)) && !(live.isSelectionFirstIntersectionPending()
				&& "[0, 1]/false/false".equals(state(copy))
				&& List.of(indexed ? "R2/source" : "Q").equals(evaluator.getFidelityReport()
						.getDepartedSelections()))) {
			problems.add(where + ": copy state " + state(copy) + ", live " + state(live) + " "
					+ evaluator.getFidelityReport());
		}
		String captured = evaluator.captureCertifiedProgramV3()
				.map(CertifiedConstructionProgram2D::getSelectionSignature)
				.orElse("refused " + evaluator.getProgramV3Refusal());
		if (!rule.equals(captured)) {
			problems.add(where + ": rule " + captured + " instead of " + rule);
		}
	}

	/**
	 * @return the rule with its line-circle step in compaction mode, the raw order and the
	 *         label state unchanged
	 */
	private static String compaction(String rule) {
		return rule.replace("LINE_CIRCLE[0x0.0p0,", "LINE_CIRCLE[0x1.0p0,");
	}

	/**
	 * V-COINC-9 on the author model (matrix section 3.5c; corrective descendant T13-R2): at
	 * the last binary64 of penetration, the first of the bite and excCyl 3.7, with the variants
	 * of V-COINC-6 created with the driver at {@code t = -1}, recomputed, undone and redone,
	 * and started again by reopening with the driver at {@code t_c} and at 0.3, in every sample
	 * outside the unresolved bands the operative output of the live construction and of the
	 * slice is the root that the certified selection designates ({@code Q} the smaller ray
	 * parameter, {@code R} and {@code R2} the larger), against a 60-digit reference computed
	 * from the live binary64 inputs of the ray and of the circle; where the two reference roots
	 * are closer than the error of a binary64 computation the output is that coincident root,
	 * and where the binary64 driver leaves no real root a defined output is the foot of the
	 * double root, the inherited tangency window (finding F-CU2-1). The samples include offsets
	 * of 10^-7 to 10^-2 from {@code t_c} and from the analytic ends of the gap, where the roots
	 * separate. Certified existence has a slice output, an undefined slice output is never
	 * published, and no point is published where nonexistence is certified. At each certified
	 * end of {@code lq}, a simple root where the two roots coincide, the certified limit value
	 * encloses with no tolerance the 60-digit feet of the double root at the binary64 ends and
	 * middle of its bracket, computed from the captured constants, and is evidence only: at the
	 * binary64 middle of its bracket the unlabelled output is the live one, and where it is
	 * undefined neither the slice nor the published evaluation gives a point.
	 */
	@Test
	void theModelOutputsAreTheRootsThatTheCertifiedSelectionDesignates() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			List<String> problems = new ArrayList<>();
			int[] counts = new int[2];
			int ends = 0;
			for (double excCyl : new double[] {PENETRATION, BITE, 3.7}) {
				// Each configuration retires the embedded hosts it opened (heap of the
				// final selection).
				try (G9U1TestApp.GlobalRoots roots = G9U1TestApp.GlobalRoots.capture()) {
					AppGeoCeDG app = variants(excCyl);
					// A free number outside the construction, whose change is undone and
					// redone.
					app.getKernel().getAlgebraProcessor().processAlgebraCommand("spare=1",
							false);
					PreG9BR6PlusF3Fixtures.baseline(app);
					for (String state : List.of("created", "recomputed", "undone", "redone",
							"started at t_c", "started at 0.3")) {
						if ("recomputed".equals(state)) {
							app.getKernel().updateConstruction(false);
						} else if ("undone".equals(state)) {
							PreG9BCu0Corpus.setNumber(app, "spare", 2);
							PreG9BR6PlusF3Fixtures.store(app);
							app.getKernel().getConstruction().getUndoManager().undo();
							PreG9BR6PlusF3Fixtures.awaitQuiescent(app);
						} else if ("redone".equals(state)) {
							app.getKernel().getConstruction().getUndoManager().redo();
							PreG9BR6PlusF3Fixtures.awaitQuiescent(app);
						} else if (state.startsWith("started")) {
							double start = state.endsWith("t_c") ? T_C : 0.3;
							PreG9BCu0Corpus.movePathPoint(app, "N", start);
							app = PreG9BCu0Corpus.open(PreG9BR6PlusF3Fixtures.save(app, work,
									"start-" + Double.toHexString(excCyl) + "-" + start));
						}
						String context = "excCyl=" + excCyl + " " + state;
						double spare = ((GeoNumeric) app.getKernel().lookupLabel("spare"))
								.getValue();
						if (spare != ("undone".equals(state) || "created".equals(state)
								|| "recomputed".equals(state) ? 1 : 2)) {
							problems.add(context + ": spare=" + spare);
						}
						designations(app, context, excCyl, problems, counts);
						ends += limitValues(app, context, problems);
					}
				}
			}
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("problems", problems);
			values.put("compared", counts[0]);
			values.put("rootless", counts[1]);
			values.put("ends", ends);
			return values;
		});
		assertEquals(List.of(), row.get("problems"));
		assertTrue((Integer) row.get("compared") > 10000, "compared outputs: " + row);
		assertTrue((Integer) row.get("rootless") > 0, "outputs without a real root: " + row);
		assertTrue((Integer) row.get("ends") >= 24, "certified ends: " + row);
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

	/**
	 * Records every output that is not designated; adds the number of compared outputs, and
	 * of those compared where the binary64 driver leaves no real root, to the counts.
	 */
	private static void designations(AppGeoCeDG app, String context, double excCyl,
			List<String> problems, int[] counts) throws Exception {
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
			for (double offset : new double[] {1e-7, 1e-6, 3e-6, 1e-5, 1e-4, 1e-3, 1e-2}) {
				samples.add(centre - offset);
				samples.add(centre + offset);
			}
		}
		GeoPoint driver = (GeoPoint) app.getKernel().lookupLabel("N");
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
					int checked = reference.check(where + " live", live.isDefined()
							? new double[] {live.getInhomX(), live.getInhomY()} : null,
							ROOTS.get(variant), problems)
							+ reference.check(where + " slice", copy.isValid()
									? new double[] {copy.getPoint().getX(), copy.getPoint().getY()}
									: null, ROOTS.get(variant), problems);
					counts[0] += checked;
					counts[1] += reference.hasRoots() ? 0 : checked;
					boolean published = locus.getSemanticDefinition().evaluate(
							PreG9BCu0Corpus.MAIN, parameter, publishedSession).isValid();
					if (state == LocusExistenceStructure2D.CoverageState.CERTIFIED_EXISTS
							&& !copy.isValid()) {
						problems.add(where + ": certified existence without a slice output");
					}
					if (!copy.isValid() && published) {
						problems.add(where + ": an undefined slice output is published");
					}
					if (state == LocusExistenceStructure2D.CoverageState.CERTIFIED_DOES_NOT_EXIST
							&& published) {
						problems.add(where + ": a point is published where nonexistence is"
								+ " certified");
					}
				}
			}
		}
	}

	/**
	 * At each certified end of {@code lq} the roots coincide: its limit value encloses, with no
	 * tolerance, the 60-digit feet of the double root at the binary64 ends and middle of the
	 * bracket, computed from the captured constants. At the binary64 middle of the bracket the
	 * roots are apart by far more than the suppression tolerance of the unlabelled output,
	 * which is then defined and is what the slice of {@code lr2} gives; where it is undefined,
	 * nothing is given in its place.
	 *
	 * @return number of checked ends
	 */
	private static int limitValues(AppGeoCeDG app, String context, List<String> problems)
			throws Exception {
		GeoLocusV2 lq = PreG9BCu0Corpus.locus(app, "lq");
		GeoLocusV2 lr2 = PreG9BCu0Corpus.locus(app, "lr2");
		LocusDefinition2D definition = lq.getSemanticDefinition();
		double[] constants = captured(evaluator(lq).captureCertifiedProgramV3().orElseThrow());
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
				for (double t : new double[] {end.getEnclosureLower(), parameter,
						end.getEnclosureUpper()}) {
					BigDecimal[] foot = foot(constants, t);
					if (!image.isEnclosed() || !encloses(image, foot)) {
						problems.add(context + ": the limit value " + image + " of " + end
								+ " does not enclose the foot (" + foot[0] + ", " + foot[1]
								+ ") at " + t);
					}
				}
				PreG9BCu0Corpus.movePathPoint(app, "N", parameter);
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
			if (output == null) {
				return 0;
			}
			if (roots == null) {
				// No real root at the binary64 driver: a defined output is the coincident root
				// that the inherited tangency window gives (finding F-CU2-1), the foot.
				if (distance(foot, output) > 4 * TOLERANCE) {
					problems.add(where + ": output " + Arrays.toString(output)
							+ " without a real root is not the foot " + Arrays.toString(foot));
				}
				return 1;
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

		/** @return whether the binary64 driver leaves two real roots */
		boolean hasRoots() {
			return roots != null;
		}

		private static double distance(double[] first, double[] second) {
			return Math.hypot(first[0] - second[0], first[1] - second[1]);
		}
	}

	/**
	 * @return the constants that the class-v3 program captured
	 *         ({@code locus-v2-certified-construction-model.md} section 10.6): the driver
	 *         circle by its half axes, eigenvectors and midpoint (0 to 7), the point P (8, 9)
	 *         and the circle p by its matrix entries A2, A4 and A5 (10 to 12)
	 */
	private static double[] captured(CertifiedConstructionProgram2D program) {
		double[] constants = new double[13];
		for (int index = 0; index < 8; index++) {
			constants[index] = program.getDriverConstant(index);
		}
		int found = 0;
		for (CertifiedConstructionProgram2D.Step step : program.getSteps()) {
			List<CertifiedConstructionProgram2D.Operand> operands = step.getOperands();
			if (step.getOperation() == CertifiedConstructionProgram2D.Operation.RAY
					&& operands.get(0).getKind()
							== CertifiedConstructionProgram2D.OperandKind.POINT) {
				constants[8] = operands.get(0).getValue(3);
				constants[9] = operands.get(0).getValue(4);
				found++;
			} else if (step.getOperation()
					== CertifiedConstructionProgram2D.Operation.LINE_CIRCLE
					&& operands.get(1).getKind()
							== CertifiedConstructionProgram2D.OperandKind.CIRCLE) {
				for (int index = 0; index < 3; index++) {
					constants[10 + index] = operands.get(1).getValue(index);
				}
				found++;
			}
		}
		if (found != 2) {
			throw new IllegalStateException("program layout " + program.getSignature());
		}
		return constants;
	}

	/**
	 * @return the 60-digit foot of the perpendicular from the centre of p to the line from P
	 *         through the driver at {@code t}, the limit of a double root, from the captured
	 *         constants and the 60-digit cosine and sine of the binary64 parameter
	 */
	private static BigDecimal[] foot(double[] constants, double t) {
		MathContext context = Reference.MC;
		BigDecimal[] k = new BigDecimal[constants.length];
		for (int index = 0; index < constants.length; index++) {
			k[index] = new BigDecimal(constants[index]);
		}
		// The driver as GeoConicND.pathChangedWithoutCheckEllipse places it.
		BigDecimal along = k[0].multiply(series(new BigDecimal(t), true), context);
		BigDecimal across = k[1].multiply(series(new BigDecimal(t), false), context);
		BigDecimal dx = along.multiply(k[2], context).add(across.multiply(k[4], context), context)
				.add(k[6], context).subtract(k[8], context);
		BigDecimal dy = along.multiply(k[3], context).add(across.multiply(k[5], context), context)
				.add(k[7], context).subtract(k[9], context);
		// The circle x^2 + y^2 + 2 A4 x + 2 A5 y + A2 = 0 has the centre (-A4, -A5).
		BigDecimal wx = k[8].add(k[11], context);
		BigDecimal wy = k[9].add(k[12], context);
		BigDecimal lambda = dx.multiply(wx, context).add(dy.multiply(wy, context), context)
				.negate().divide(dx.multiply(dx, context).add(dy.multiply(dy, context), context),
						context);
		return new BigDecimal[] {k[8].add(lambda.multiply(dx, context), context),
			k[9].add(lambda.multiply(dy, context), context)};
	}

	private static BigDecimal series(BigDecimal x, boolean cosine) {
		MathContext context = Reference.MC;
		BigDecimal square = x.multiply(x, context);
		BigDecimal term = cosine ? BigDecimal.ONE : x;
		BigDecimal sum = term;
		BigDecimal limit = new BigDecimal("1e-70");
		for (int k = cosine ? 2 : 3; term.abs().compareTo(limit) > 0; k += 2) {
			term = term.multiply(square, context).negate()
					.divide(BigDecimal.valueOf((long) k * (k - 1)), context);
			sum = sum.add(term, context);
		}
		return sum;
	}

	/** @return whether the image encloses the point, with no tolerance */
	private static boolean encloses(LocusBoundaryImage2D image, BigDecimal[] point) {
		return new BigDecimal(image.getXLower()).compareTo(point[0]) <= 0
				&& point[0].compareTo(new BigDecimal(image.getXUpper())) <= 0
				&& new BigDecimal(image.getYLower()).compareTo(point[1]) <= 0
				&& point[1].compareTo(new BigDecimal(image.getYUpper())) <= 0;
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

	/** @return the line-conic intersection whose second output R2 selects */
	private static AlgoIntersectLineConic source(AppGeoCeDG app) {
		return (AlgoIntersectLineConic) ((AlgoIntersectSingle) app.getKernel().lookupLabel("R2")
				.getParentAlgorithm()).getSourceIntersection();
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
