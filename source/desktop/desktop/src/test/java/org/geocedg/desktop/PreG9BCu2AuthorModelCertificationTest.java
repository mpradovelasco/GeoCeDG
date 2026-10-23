/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.OperandKind;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusCertificationScope2D;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.LocusCertifiedFold2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.ReconstructionSliceFingerprint2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.CertifiedStationaryPoint;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.ExactEvidence;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Policy;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Result;
import org.geocedg.common.kernel.locus.metric.LocusMetricFunctionStructure2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricFunctionStructure2D.OriginWrap;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricWorkBudget2D;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU2 step T7: the shadow certificates of the author model
 * ({@code locus-existence/hybrid/v1}; author model analysis sections 3 to 5). Matrix rows
 * V-PER-1, V-PER-3, V-FOLD-1, V-FOLD-2, V-FOLD-3, V-ARC-1, V-ARC-2, V-TR-1,
 * V-TR-4 (certificate part), V-CRIT-5 and V-CRIT-7 to V-CRIT-10 (model parts), V-COMPL-1,
 * V-E2-1, V-E2-4 and V-E2-5 (certificate parts), and gates GC-4, GC-6 and GC-8. Every
 * boundary is compared with the analytic reference {@code t_c +- beta} evaluated with 50
 * digits from the captured parameters of the model, never with pointwise validity. Step
 * T11 publishes the certificates; the tests read the published certificate of each revision,
 * and rows V-PUB-1 to V-PUB-4 check the publication rule. Corrective descendant T13-R4
 * (decision D-CU2-T13-R4): V-DET-1 on the complete author model.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu2AuthorModelCertificationTest {
	private static final List<String> FIRST_FAMILY = List.of("o", "v", "w", "a_1");
	private static final List<String> CLOSED = List.of("o", "v", "w", "a_1", "e_1", "o_1");
	/** Last penetration binary64 below the critical value (analysis section 3.8.3). */
	private static final long LAST_PENETRATION = 0x400d416bb67a38bbL;
	/** Bite values of V-TR-1. */
	private static final double[] BITES = {3.66, 3.7, 3.85, 3.9, 3.95, 3.96, 4.0, 4.5, 5.0};
	/** Transition bracket width of the default policy: {@code 2^-40} periods. */
	private static final double DELTA = Math.scalb(2 * Math.PI, -40);
	/** Measured excess of the pointwise validity beyond the exact root (F-CU2-1). */
	private static final double VALIDITY_EXCESS = 4.3E-6;
	private static final MathContext DIGITS = new MathContext(50);
	private static final BigDecimal PI = new BigDecimal(
			"3.14159265358979323846264338327950288419716939937510582097494459");

	@TempDir
	Path work;

	/**
	 * V-PER-1, V-FOLD-1, V-FOLD-3 (the turn of {@code w}), V-E2-1, V-E2-4, GC-4, GC-6 and
	 * GC-8 at the author's parameters: the six loci of the two families are closed periodic
	 * components, {@code e_1} folds at {@code 0} and at the seam, and {@code e_2} is one
	 * full-length open arc at {@code t_N = 0}, certified without reading any length; the
	 * origin wrap of {@code w_1} is at the exact address of {@code N_1}, with a certified
	 * positive jump whose value is a reference to an estimated length.
	 */
	@Test
	void theAuthorParametersAreCertifiedFromStructuralFactsOnly() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			Map<String, Object> values = new LinkedHashMap<>();
			for (String label : CLOSED) {
				Result result = certificate(app, label);
				values.put(label, shape(result));
				values.put(label + ".budgets", budgets(result));
				values.put(label + ".stationary", stationary(result));
				values.put(label + ".folds", folds(result));
			}
			GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_2");
			// Since step T11 the certificate is computed when the revision is published, while
			// the document opens: GC-6 counts the reads since the creation of e_2, with the
			// single certificate computation of its revision, and none when it is read.
			long reads = metricReads(e2);
			Result result = certificate(app, "e_2");
			values.put("e_2.reads", reads + "/" + (metricReads(e2) - reads) + "/"
					+ e2.getInstrumentation().getCertificationWork(
							LocusCertificationWork2D.CERTIFICATE_COMPUTATION));
			values.put("e_2", shape(result));
			values.put("e_2.budgets", budgets(result));
			values.put("e_2.stationary", stationary(result));
			ContinuousComponent arc = result.getStructure().getContinuousValidComponents().get(0);
			values.put("e_2.arc", arc.isFullLength() + "/" + arc.getStart() + "/" + arc.getEnd()
					+ "/" + arc.getMaximality() + "/" + arc.getArcCoordinate(0));
			values.put("e_2.start", end(arc.getLowerBoundary()));
			values.put("e_2.end", end(arc.getUpperBoundary()));
			values.put("e_2.coverage", coverage(result.getStructure()));
			AlgoLocusBetweenMetricV2 w1 = (AlgoLocusBetweenMetricV2) app.getKernel()
					.lookupLabel("w_1").getParentAlgorithm();
			LocusMetricFunctionStructure2D structure = w1.getFunctionStructure();
			OriginWrap wrap = structure.getEvents().get(0);
			double address = ((AlgoSemanticLocusPoint2D) app.getKernel().lookupLabel("N_1")
					.getParentAlgorithm()).getCurrentSemanticAddress().getCanonicalParameter();
			values.put("w_1", structure.isCertified() + "/" + structure.getPieces().size() + "/"
					+ structure.getEvents().size());
			values.put("w_1.wrap", (wrap.getDriverParameter() == address) + "/" + address + "/"
					+ wrap.getDirection() + "/" + wrap.getSamePosition() + "/"
					+ wrap.isJumpPositive());
			LocusMetricResult2D jump = w1.resolveJumpValue();
			double estimate = jump.getMetricValue().getFiniteValue().getAsDouble();
			values.put("w_1.jump", jump.getGuaranteeClass() + "/"
					+ (wrap.getJumpLowerBound() <= estimate && estimate
							<= wrap.getJumpUpperBound()));
			LocusMetricFunctionStructure2D copy = evaluator(e2).getMetricStructuresV3()
					.values().iterator().next();
			values.put("w_1.copy", copy.getPieces().get(0).getSignature()
					.equals(structure.getPieces().get(0).getSignature())
					&& copy.getEvents().get(0).getDriverParameter() == wrap.getDriverParameter()
					&& copy.getEvents().get(0).getJumpLowerBound() == wrap.getJumpLowerBound()
					&& copy.getEvents().get(0).getJumpUpperBound() == wrap.getJumpUpperBound());
			return values;
		});
		for (String label : CLOSED) {
			assertEquals("COMPLETE/true/0/0", row.get(label), label + " " + row);
			assertEquals("within", row.get(label + ".budgets"), label + " " + row);
		}
		assertEquals("FOLD:HORIZONTAL:-0x1.921fb54442d19p1,FOLD:HORIZONTAL:0x0.0p0",
				row.get("e_1.stationary"), row.toString());
		// PRE-G9B-CU2 step T10: the structure publishes both folds of e_1 with their isolation
		// bands, the only turn exemptions of its drawing besides singular vertices (V-B06-2).
		assertEquals("-0x1.921fb54442d19p1:true,0x0.0p0:true", row.get("e_1.folds"),
				row.toString());
		for (String label : List.of("o", "v", "w", "a_1", "o_1", "e_2")) {
			assertEquals("", row.get(label + ".stationary"), label + " " + row);
		}
		for (String label : List.of("o", "v", "w", "a_1", "o_1")) {
			assertEquals("", row.get(label + ".folds"), label + " " + row);
		}
		assertEquals("0/0/1", row.get("e_2.reads"), "GC-6 " + row);
		assertEquals("COMPLETE/false/0/0", row.get("e_2"), row.toString());
		assertEquals("within", row.get("e_2.budgets"), row.toString());
		assertEquals("true/0.0/0.0/ESTABLISHED/0.0", row.get("e_2.arc"), row.toString());
		assertEquals("STRUCTURAL_EVENT:ORIGIN_WRAP:start:true:EXACT", row.get("e_2.start"),
				row.toString());
		assertEquals("STRUCTURAL_EVENT:ORIGIN_WRAP:end:false:EXACT", row.get("e_2.end"),
				row.toString());
		assertEquals("[-3.141592653589793, 3.141592653589793):CERTIFIED_EXISTS",
				row.get("e_2.coverage"), row.toString());
		assertEquals("true/1/1", row.get("w_1"), row.toString());
		assertEquals("true/0.0/FORWARD/ZERO_LENGTH/true", row.get("w_1.wrap"), row.toString());
		assertEquals("ESTIMATED/true", row.get("w_1.jump"), row.toString());
		assertEquals(true, row.get("w_1.copy"), row.toString());
	}

	/**
	 * V-ARC-1, V-ARC-2, V-TR-1 and V-CRIT-5 (model part): in every bite of the sweep each
	 * locus of the first family is one arc across the seam whose two closed ends are exact
	 * transitions enclosing the analytic ends {@code t_c +- beta} in brackets no wider than
	 * {@code delta}; the arc starts at {@code t_c + beta}, has length {@code 2 pi - 2 beta},
	 * and holds {@code +-pi} with one arc coordinate each.
	 */
	@Test
	void everyBiteIsOneArcAcrossTheSeamWithTheAnalyticEnds() throws Exception {
		List<String> failures = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			List<String> problems = new ArrayList<>();
			for (double excCyl : BITES) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", excCyl);
				BigDecimal[] reference = reference(app, 5, 1.5, excCyl);
				double lower = reference[0].doubleValue();
				double upper = reference[1].doubleValue();
				double beta = reference[1].subtract(reference[0], DIGITS)
						.divide(BigDecimal.valueOf(2), DIGITS).doubleValue();
				for (String label : FIRST_FAMILY) {
					String key = label + "@" + excCyl;
					Result result = certificate(app, label);
					LocusExistenceStructure2D structure = result.getStructure();
					if (!"COMPLETE/false/2/0".equals(shape(result))
							|| structure.getContinuousValidComponents().size() != 1) {
						problems.add(key + " shape " + shape(result));
						continue;
					}
					ContinuousComponent arc = structure.getContinuousValidComponents().get(0);
					LocusBoundary2D start = arc.getLowerBoundary();
					LocusBoundary2D end = arc.getUpperBoundary();
					if (!arc.crossesSeam() || !encloses(start, upper) || !encloses(end, lower)
							|| arc.getStart() != start.getEnclosureUpper()
							|| arc.getEnd() != end.getEnclosureLower()
							|| Math.abs(arc.getLength() - (2 * Math.PI - 2 * beta)) > 2 * DELTA) {
						problems.add(key + " arc " + arc.getStart() + ".." + arc.getEnd() + " "
								+ end(start) + " " + end(end) + " reference " + lower + ","
								+ upper);
					}
					for (LocusBoundary2D boundary : List.of(start, end)) {
						if (!"CERTIFIED_TRANSITION".equals(boundary.getKind().name())
								|| !boundary.isIncluded()
								|| boundary.getEnclosureUpper() - boundary.getEnclosureLower()
										> DELTA) {
							problems.add(key + " end " + end(boundary));
						}
					}
					if (!"within".equals(budgets(result))) {
						problems.add(key + " budgets " + budgets(result));
					}
					if (excCyl == 3.7) {
						for (double seam : new double[] {-Math.PI, Math.nextDown(Math.PI)}) {
							double coordinate = arc.getArcCoordinate(seam);
							if (!(coordinate >= 0 && coordinate <= arc.getLength())) {
								problems.add(key + " the seam side " + seam
										+ " has no arc coordinate: " + coordinate);
							}
						}
					}
				}
			}
			return problems;
		});
		assertEquals(List.of(), failures);
	}

	/**
	 * V-PUB-1 (step T11; inverts {@code pins.H01} of CU0): at excCyl 4.0 the saved driver lies
	 * in the gap, where the traced point {@code W} is undefined; the four loci are published
	 * all the same, each {@code COMPLETE} and one arc across the seam whose closed ends enclose
	 * the analytic ends {@code t_c +- beta}.
	 */
	@Test
	void theLociArePublishedWhileTheSavedDriverIsInTheGap() throws Exception {
		List<String> failures = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.setNumber(app, "excCyl", 4.0);
			List<String> problems = new ArrayList<>();
			if (app.getKernel().lookupLabel("W").isDefined()) {
				problems.add("the traced point W is defined at the saved driver");
			}
			BigDecimal[] reference = reference(app, 5, 1.5, 4.0);
			for (String label : FIRST_FAMILY) {
				GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
				Result result = certificate(app, label);
				LocusExistenceStructure2D structure = result.getStructure();
				LocusDefinition2D definition = locus.getSemanticDefinition();
				// The definition binds the certificate structure to its revision.
				if (!locus.isDefined() || definition == null || !definition
						.getBranch(PreG9BCu0Corpus.MAIN).getExistenceStructure()
						.getSemanticSignature().equals(structure.getSemanticSignature())) {
					problems.add(label + " is not published");
					continue;
				}
				List<ContinuousComponent> components = structure.getContinuousValidComponents();
				if (!"COMPLETE/false/2/0".equals(shape(result)) || components.size() != 1
						|| !components.get(0).crossesSeam()
						|| !encloses(components.get(0).getLowerBoundary(),
								reference[1].doubleValue())
						|| !encloses(components.get(0).getUpperBoundary(),
								reference[0].doubleValue())) {
					problems.add(label + " " + shape(result) + " " + coverage(structure));
				}
			}
			return problems;
		});
		assertEquals(List.of(), failures);
	}

	/**
	 * V-PUB-2 (F-CU2-3): with the saved driver, which enters the gap from excCyl
	 * 3.958845384, every value of excCyl publishes a revision of its own whose certificates
	 * and points are, bit for bit, those of the same document saved and opened again.
	 */
	@Test
	void everyRevisionPublishesItsOwnGeometry() throws Exception {
		List<String> failures = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			List<String> problems = new ArrayList<>();
			Map<String, Long> revisions = new LinkedHashMap<>();
			int step = 0;
			for (double excCyl : new double[] {3.95, 3.96, 4.0, 4.5, 5.0}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", excCyl);
				String live = published(app);
				AppGeoCeDG reopened = PreG9BCu0Corpus.open(PreG9BR6PlusF3Fixtures.save(app,
						work, "revision-" + step++));
				String fresh = published(reopened);
				if (!live.equals(fresh)) {
					problems.add(excCyl + " live " + live + " reopened " + fresh);
				}
				for (String label : FIRST_FAMILY) {
					long revision = PreG9BCu0Corpus.locus(app, label).getSemanticRevision();
					Long previous = revisions.put(label, revision);
					if (previous != null && revision <= previous) {
						problems.add(excCyl + " " + label + " kept revision " + revision);
					}
				}
			}
			return problems;
		});
		assertEquals(List.of(), failures);
	}

	/**
	 * V-PUB-3 (D-CU2-8; D-CU2-T11-R1 section 3): beyond excCyl 6.7915 the discriminant is
	 * negative on the whole circle (the test raises the slider bound, which is
	 * {@code rbCone}); each locus of the first family is published and valid,
	 * {@code COMPLETE} with no component, its total length is zero and its intersection with
	 * a line is a complete empty set. After the dynamic change the settlement cannot
	 * reproduce the live selection, the copy is never declared faithful and the proof of
	 * nonexistence uses no unreproduced selection; the same document saved and opened again
	 * is faithful and publishes the same result from its ordinary certificate.
	 */
	@Test
	void anEmptyCertifiedCoverIsPublishedComplete() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			GeoNumeric excCyl = (GeoNumeric) app.getKernel().lookupLabel("excCyl");
			excCyl.setIntervalMax(new MyDouble(app.getKernel(), 10));
			PreG9BCu0Corpus.setNumber(app, "excCyl", 7.0);
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("excCyl", excCyl.getValue());
			G9U1TestApp.eval(app, "emptyCoverLine:y=0");
			for (String label : FIRST_FAMILY) {
				G9U1TestApp.eval(app, "emptyCoverTotal" + label.replace("_", "")
						+ "=LocusLength(" + label + ")");
				G9U1TestApp.eval(app, "emptyCoverCut" + label.replace("_", "") + "=Intersect("
						+ label + ",emptyCoverLine)");
				values.put(label, emptyCover(app, label));
			}
			AppGeoCeDG reopened = PreG9BCu0Corpus.open(PreG9BR6PlusF3Fixtures.save(app, work,
					"empty-cover"));
			values.put("reopened excCyl",
					((GeoNumeric) reopened.getKernel().lookupLabel("excCyl")).getValue());
			for (String label : FIRST_FAMILY) {
				values.put("reopened " + label, emptyCover(reopened, label));
			}
			return values;
		});
		assertEquals(7.0, row.get("excCyl"), row.toString());
		assertEquals(7.0, row.get("reopened excCyl"), row.toString());
		for (String label : FIRST_FAMILY) {
			assertEquals("true/VALID/COMPLETE/0/0/0.0/EMPTY/COMPLETE/budget-exhausted/false/"
					+ "[CERTIFIED_DOES_NOT_EXIST:SELECTION_INDEPENDENT_NONEXISTENCE]",
					row.get(label), row.toString());
			assertEquals("true/VALID/COMPLETE/0/0/0.0/EMPTY/COMPLETE/not-needed/true/"
					+ "[CERTIFIED_DOES_NOT_EXIST:PREDICATE_CERTIFIED_FALSE]",
					row.get("reopened " + label), row.toString());
		}
	}

	/**
	 * One row of V-PUB-3: publication, completeness, components, total length and cut, then
	 * the settlement outcome, the fidelity of the slice copy and the states and causes of the
	 * coverage.
	 */
	private static String emptyCover(AppGeoCeDG app, String label)
			throws ReflectiveOperationException {
		GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
		LocusDefinition2D definition = locus.getSemanticDefinition();
		LocusExistenceStructure2D structure = definition.getBranch(PreG9BCu0Corpus.MAIN)
				.getExistenceStructure();
		String name = label.replace("_", "");
		GeoLocusMetricResult total = (GeoLocusMetricResult) app.getKernel()
				.lookupLabel("emptyCoverTotal" + name);
		GeoLocusIntersectionResult cut = (GeoLocusIntersectionResult) app.getKernel()
				.lookupLabel("emptyCoverCut" + name);
		ReconstructibleLocusEvaluator2D evaluator = evaluator(locus);
		Set<String> causes = new TreeSet<>();
		for (Coverage item : structure.getCoverage()) {
			causes.add(item.getState() + ":" + item.getCause());
		}
		return locus.isDefined() + "/" + definition.getDefinitionStatus() + "/"
				+ structure.getCompleteness() + "/"
				+ structure.getContinuousValidComponents().size() + "/"
				+ structure.getPointComponents().size() + "/"
				+ total.getMetricResult().getMetricValue().getFiniteValue().orElse(-1) + "/"
				+ cut.getIntersectionResult().getGeometryKind() + "/"
				+ cut.getIntersectionResult().getCompletenessEvidence().getCompleteness() + "/"
				+ evaluator.getSettlementOutcome().getToken() + "/"
				+ evaluator.isReconstructionFaithful() + "/" + causes;
	}

	/**
	 * V-PUB-4 (H01, F-CU2-3): a free point of the slice that is undefined is an undefined
	 * definition input: the locus is undefined and its previous definition is discarded, so
	 * no consumer reads stale geometry; defining the point again publishes a new revision.
	 */
	@Test
	void anUndefinedDefinitionInputLeavesNoStaleDefinition() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			GeoLocusV2 o = PreG9BCu0Corpus.locus(app, "o");
			GeoPoint free = null;
			for (GeoElement input : ((AlgoDependentPointLocusV2) o.getParentAlgorithm())
					.getDefinitionInputs()) {
				if (input instanceof GeoPoint && input.isIndependent()) {
					free = (GeoPoint) input;
					break;
				}
			}
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("free", free != null);
			long revision = o.getSemanticRevision();
			double x = free.getInhomX();
			double y = free.getInhomY();
			free.setUndefined();
			free.updateRepaint();
			values.put("undefined", !o.isDefined() && o.getSemanticDefinition() == null);
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4)) {
				o.evaluate(PreG9BCu0Corpus.MAIN, 0, session);
				values.put("stale", true);
			} catch (IllegalStateException expected) {
				values.put("stale", false);
			}
			free.setCoords(x, y, 1);
			free.updateRepaint();
			values.put("republished", o.isDefined() && o.getSemanticRevision() > revision);
			return values;
		});
		assertEquals(true, row.get("free"), row.toString());
		assertEquals(true, row.get("undefined"), row.toString());
		assertEquals(false, row.get("stale"), row.toString());
		assertEquals(true, row.get("republished"), row.toString());
	}

	/**
	 * V-TR-4 (certificate part): pointwise validity is not a boundary. At excCyl 3.7 the
	 * evaluator of the slice still returns valid points beyond the exact end of the arc,
	 * inside the measured excess, where the certificate states nonexistence by the negative
	 * discriminant. Since step T11 the certificate is published, and the published locus
	 * refuses such a point (F-CU2-1): the evaluator of the slice is read directly.
	 */
	@Test
	void pointwiseValidityBeyondTheExactEndIsNotExistence() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			PreG9BCu0Corpus.setNumber(app, "excCyl", 3.7);
			Map<String, Object> values = new LinkedHashMap<>();
			GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, "o");
			ReconstructibleLocusEvaluator2D slice = evaluator(locus);
			LocusExistenceStructure2D structure = certificate(app, "o").getStructure();
			LocusBoundary2D end = structure.getContinuousValidComponents().get(0)
					.getUpperBoundary();
			double outside = end.getEnclosureUpper();
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(256)) {
				double valid = Double.NaN;
				// Offsets from the measured excess down to 2^-24 of it, beyond the enclosure.
				for (int exponent = 0; exponent <= 24 && Double.isNaN(valid); exponent++) {
					double parameter = outside + Math.scalb(VALIDITY_EXCESS, -exponent);
					if (slice.evaluate(null, null, parameter, session).isValid()) {
						valid = parameter;
					}
				}
				values.put("valid", valid);
				values.put("state", Double.isNaN(valid) ? "none"
						: coverageAt(structure, valid).getState() + ":"
								+ coverageAt(structure, valid).getEvidence());
				values.put("published", Double.isNaN(valid) ? "none"
						: locus.evaluate(PreG9BCu0Corpus.MAIN, valid, session).getStatus());
			}
			return values;
		});
		assertTrue(!Double.isNaN((Double) row.get("valid")), row.toString());
		assertTrue(((String) row.get("state")).startsWith("CERTIFIED_DOES_NOT_EXIST:")
				&& ((String) row.get("state")).endsWith("DISCRIMINANT_NEGATIVE"),
				row.toString());
		assertEquals(EvaluationStatus.DEPENDENCY_UNDEFINED, row.get("published"),
				row.toString());
	}

	/**
	 * V-CRIT-7, V-CRIT-10 and GC-8: the thirteen binary64 values of excCyl centred at the
	 * critical value are classified exactly, penetration up to {@code 0x1.d416bb67a38bbp1}
	 * and bite from the next value, with no double root and every exact certificate within
	 * its budgets; a second pass reproduces every certificate bit for bit.
	 */
	@Test
	void contiguousBinary64ValuesAreClassifiedExactlyAndDeterministically() throws Exception {
		List<List<String>> passes = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			List<List<String>> both = new ArrayList<>();
			for (int pass = 0; pass < 2; pass++) {
				List<String> rows = new ArrayList<>();
				for (long k = -6; k <= 6; k++) {
					double excCyl = Double.longBitsToDouble(LAST_PENETRATION + k);
					PreG9BCu0Corpus.setNumber(app, "excCyl", excCyl);
					for (String label : FIRST_FAMILY) {
						Result result = certificate(app, label);
						rows.add(k + ":" + label + ":" + shape(result) + ":"
								+ result.getStructure().getSingularParameters().size() + ":"
								+ budgets(result) + ":" + stationary(result).isEmpty() + ":"
								+ result.getStructure().getSemanticSignature().hashCode() + ":"
								+ result.getExactEvidence());
					}
				}
				both.add(rows);
			}
			return both;
		});
		List<String> first = passes.get(0);
		assertEquals(first, passes.get(1), "V-CRIT-10");
		for (String row : first) {
			String[] parts = row.split(":");
			long k = Long.parseLong(parts[0]);
			String expected = k <= 0 ? "COMPLETE/true/0/0" : "COMPLETE/false/2/0";
			assertEquals(expected, parts[2], row);
			assertEquals("0", parts[3], "no double root: " + row);
			assertEquals("within", parts[4], row);
			assertEquals("true", parts[5], "V-FOLD-3: a closed turn is not a fold: " + row);
		}
	}

	/**
	 * V-PER-3 and GC-8: with rbCone 8 and 10 the ray {@code n_1} from {@code I} and the vertical
	 * {@code b_1} through {@code J_1} coincide as verticals at the two drivers
	 * {@code t* = +-arccos((x_I - x_B) / rbCone)} of {@code o_1}, located independently to 50
	 * digits from the captured coordinates: each lies in a narrow unresolved band that no
	 * component crosses, and every band of {@code o_1} lies within {@code 1e-9} of one of
	 * them (with rbCone 10 the box predicate of the ray parameter is decided on a certified
	 * island of about {@code 3e-12} between two bands next to each {@code t*}), while the other
	 * loci stay closed; every certificate stays within its box and exact budgets. The
	 * structure of {@code w_1} then stops at the band edges, and {@code e_2} states no
	 * nonexistence it cannot prove.
	 */
	@Test
	void constructionDegeneraciesAreBandsThatNoComponentCrosses() throws Exception {
		List<String> failures = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			List<String> problems = new ArrayList<>();
			for (double rbCone : new double[] {8, 10}) {
				PreG9BCu0Corpus.setNumber(app, "rbCone", rbCone);
				for (String label : List.of("o", "v", "w", "a_1", "e_1")) {
					if (!"COMPLETE/true/0/0".equals(shape(certificate(app, label)))) {
						problems.add(label + "@" + rbCone + " "
								+ shape(certificate(app, label)));
					}
				}
				for (String label : List.of("o", "v", "w", "a_1", "e_1", "o_1")) {
					if (!"within".equals(budgets(certificate(app, label)))) {
						problems.add(label + "@" + rbCone + " " + budgets(certificate(app, label)));
					}
				}
				LocusExistenceStructure2D source = certificate(app, "o_1").getStructure();
				List<BigDecimal> degenerate = coincidentVerticals(app, rbCone);
				int bands = 0;
				for (Coverage item : source.getCoverage()) {
					if (item.getState() != CoverageState.UNRESOLVED) {
						continue;
					}
					bands++;
					LocusInterval2D band = item.getInterval();
					double middle = band.getLower() + (band.getUpper() - band.getLower()) / 2;
					// Only the bands of the degenerate drivers: each lies within 1e-9 of one.
					if (band.getUpper() - band.getLower() > 1E-9
							|| source.getComponentContaining(middle) != null
							|| !item.getEvidence().contains("RAY_PARAMETER")
							|| degenerate.stream().noneMatch(root -> root.subtract(
									new BigDecimal(middle)).abs().compareTo(new BigDecimal("1e-9"))
											< 0)) {
						problems.add("o_1@" + rbCone + " band " + band + " " + item.getEvidence()
								+ " for " + degenerate);
					}
				}
				for (BigDecimal root : degenerate) {
					// The 50-digit t* lies in a band, and no component contains it.
					boolean inBand = source.getCoverage().stream().anyMatch(item ->
							item.getState() == CoverageState.UNRESOLVED
									&& new BigDecimal(item.getInterval().getLower()).compareTo(root)
											<= 0 && root.compareTo(new BigDecimal(
													item.getInterval().getUpper())) <= 0);
					double nearest = root.doubleValue();
					boolean crossed = source.getComponentContaining(Math.nextDown(nearest)) != null
							&& source.getComponentContaining(Math.nextUp(nearest)) != null;
					if (!inBand || source.getComponentContaining(nearest) != null || crossed) {
						problems.add("o_1@" + rbCone + " t*=" + root + " in a band: " + inBand);
					}
				}
				if (bands < degenerate.size()) {
					problems.add("o_1@" + rbCone + " has " + bands + " bands for "
							+ degenerate.size() + " degenerate drivers");
				}
				LocusExistenceStructure2D dependent = certificate(app, "e_2").getStructure();
				for (Coverage item : dependent.getCoverage()) {
					if (item.getState() == CoverageState.CERTIFIED_DOES_NOT_EXIST) {
						problems.add("e_2@" + rbCone + " claims " + item);
					}
				}
				if (dependent.getCompleteness().name().equals("COMPLETE")) {
					problems.add("e_2@" + rbCone + " is complete");
				}
			}
			return problems;
		});
		assertEquals(List.of(), failures);
	}

	/**
	 * V-CRIT-9: changing a non-driver parameter moves the critical value as the formula
	 * {@code excCyl = (rbCone - rbCylinder) / cos(thetaP)} says, and the exact classification
	 * follows it; every revision computes its own certificate.
	 */
	@Test
	void nonDriverParametersMoveTheCriticalValue() throws Exception {
		List<String> failures = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			List<String> problems = new ArrayList<>();
			Result previous = certificate(app, "o");
			PreG9BCu0Corpus.setNumber(app, "rbCylinder", 2.0);
			problems.addAll(classification(app, 2.0, previous));
			PreG9BCu0Corpus.setNumber(app, "rbCylinder", 1.5);
			PreG9BCu0Corpus.setNumber(app, "excCyl", 2.9);
			previous = certificate(app, "o");
			PreG9BCu0Corpus.setNumber(app, "\u03b1_{cyl}", Math.toRadians(35));
			problems.addAll(classification(app, 1.5, previous));
			return problems;
		});
		assertEquals(List.of(), failures);
	}

	/**
	 * V-CRIT-8: undoing and redoing excCyl 2.9, 3.7, the first bite binary64 and 2.9 again
	 * restores, at each step, the certificates and the selection state of a construction
	 * freshly opened with the same value.
	 */
	@Test
	void undoAndRedoRestoreTheCertificates() throws Exception {
		double[] values = {2.9, 3.7, Double.longBitsToDouble(LAST_PENETRATION + 1), 2.9};
		List<String> failures = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			Map<Double, String> fresh = new LinkedHashMap<>();
			for (double value : values) {
				AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
				PreG9BCu0Corpus.setNumber(app, "excCyl", value);
				fresh.put(value, signatures(app));
			}
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BR6PlusF3Fixtures.baseline(app);
			for (int index = 1; index < values.length; index++) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", values[index]);
				PreG9BR6PlusF3Fixtures.store(app);
			}
			List<String> problems = new ArrayList<>();
			for (int index = values.length - 2; index >= 0; index--) {
				app.getKernel().getConstruction().getUndoManager().undo();
				PreG9BR6PlusF3Fixtures.awaitQuiescent(app);
				compare(problems, "undo to " + values[index], fresh.get(values[index]),
						signatures(app));
			}
			for (int index = 1; index < values.length; index++) {
				app.getKernel().getConstruction().getUndoManager().redo();
				PreG9BR6PlusF3Fixtures.awaitQuiescent(app);
				compare(problems, "redo to " + values[index], fresh.get(values[index]),
						signatures(app));
			}
			return problems;
		});
		assertEquals(List.of(), failures);
	}

	/**
	 * V-FOLD-2: the symmetry of {@code e_1} is verified on the captured constants. With the
	 * centre {@code I} of {@code d_1} moved {@code 2^-40} off the mirror {@code i} in its
	 * captured program, no fold is certified although the locus stays closed; the same
	 * program rebuilt without the move keeps both folds.
	 */
	@Test
	void theFoldsOfE1RestOnTheCapturedConstants() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			CertifiedConstructionProgram2D program = evaluator(PreG9BCu0Corpus.locus(app,
					"e_1")).captureCertifiedProgramV3().orElseThrow();
			Map<String, Object> values = new LinkedHashMap<>();
			for (double shift : new double[] {0, Math.scalb(1.0, -40)}) {
				Result result = HybridExistenceCertifier2D.certify(new ConstructionBoxModel2D(
						movedCentre(program, shift)), program.getDomain(), true,
						PreG9BCu0Corpus.MAIN, null, Policy.DEFAULT, null);
				values.put("shift=" + shift, shape(result) + "|" + stationary(result));
			}
			return values;
		});
		assertEquals("COMPLETE/true/0/0|FOLD:HORIZONTAL:-0x1.921fb54442d19p1,"
				+ "FOLD:HORIZONTAL:0x0.0p0", row.get("shift=0.0"), row.toString());
		assertEquals("COMPLETE/true/0/0|", row.get("shift=" + Math.scalb(1.0, -40)),
				row.toString());
	}

	/**
	 * V-COMPL-1: completeness is a cover. With a test box budget that runs out before the
	 * circle is covered, every explored box of {@code o} being valid, the certificate is
	 * never complete and the uncovered region is an explicit band of exhausted budget.
	 */
	@Test
	void anExhaustedBudgetIsNeverComplete() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			CertifiedConstructionProgram2D program = evaluator(PreG9BCu0Corpus.locus(app,
					"o")).captureCertifiedProgramV3().orElseThrow();
			Result result = HybridExistenceCertifier2D.certify(new ConstructionBoxModel2D(
					program), program.getDomain(), true, PreG9BCu0Corpus.MAIN, null,
					Policy.DEFAULT.withBoxBudget(16), null);
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("completeness", result.getStructure().getCompleteness().name());
			values.put("boxes", result.getExistenceBoxes());
			values.put("coverage", coverage(result.getStructure()));
			return values;
		});
		assertEquals("NOT_ESTABLISHED", row.get("completeness"), row.toString());
		assertTrue((Long) row.get("boxes") <= 16, row.toString());
		assertTrue(((String) row.get("coverage")).contains("UNRESOLVED:BUDGET_EXHAUSTED"),
				row.toString());
	}

	/**
	 * V-E2-5 (certificate part) and GC-6: with a test policy whose work budget makes the
	 * estimate of {@code w_1} fail in the slice of {@code e_2} at one driver value, that point
	 * is not evaluated, and the existence structure recomputed meanwhile is identical, with
	 * no metric value read; restoring the policy restores the point. Since step T9 the point
	 * is an {@code EVALUATION_FAILED}, a computational failure, never nonexistence (P5).
	 */
	@Test
	void aFailingLengthEstimateLeavesTheCertificateUnchanged() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_2");
			ReconstructibleLocusEvaluator2D evaluator = evaluator(e2);
			Result before = certificate(app, "e_2");
			AlgoLocusBetweenMetricV2 length = sliceLength(evaluator);
			Field field = AlgoLocusBetweenMetricV2.class.getDeclaredField("policy");
			field.setAccessible(true);
			LocusMetricPolicy2D policy = (LocusMetricPolicy2D) field.get(length);
			Map<String, Object> values = new LinkedHashMap<>();
			field.set(length, policy.withWorkBudget(new MetricWorkBudget2D(3, 1, 1)));
			try {
				values.put("failing", status(e2, 1));
				values.put("length", length.getResult().getMetricResult()
						.getComputationStatus().name());
				CertifiedConstructionProgram2D program =
						evaluator.captureCertifiedProgramV3().orElseThrow();
				long reads = metricReads(e2);
				Result again = LocusCertificationScope2D.certify(e2.getInstrumentation(), () ->
						HybridExistenceCertifier2D.certify(new ConstructionBoxModel2D(program),
								evaluator.getMetricStructuresV3(),
								evaluator.getOperationIdentitiesV3(), program.getDomain(), true,
								PreG9BCu0Corpus.MAIN, null, Policy.DEFAULT, null));
				values.put("reads", metricReads(e2) - reads);
				values.put("identical", again.getStructure().getSemanticSignature()
						.equals(before.getStructure().getSemanticSignature()));
			} finally {
				field.set(length, policy);
			}
			values.put("restored", status(e2, 1));
			values.put("same", before == certificate(app, "e_2"));
			values.put("exists", coverageAt(before.getStructure(), 1).getState().name());
			return values;
		});
		assertEquals("EVALUATION_FAILED", row.get("failing"), row.toString());
		assertEquals("LIMIT_NOT_ESTABLISHED", row.get("length"), row.toString());
		assertEquals(0L, row.get("reads"), "GC-6 " + row);
		assertEquals(true, row.get("identical"), row.toString());
		assertEquals("VALID", row.get("restored"), row.toString());
		assertEquals(true, row.get("same"), row.toString());
		assertEquals(CoverageState.CERTIFIED_EXISTS.name(), row.get("exists"), row.toString());
	}

	/** Checks the classification on both sides of the critical value of the formula. */
	private static List<String> classification(AppGeoCeDG app, double rbCylinder,
			Result previous) throws Exception {
		List<String> problems = new ArrayList<>();
		BigDecimal cosine = cosThetaP(app, 5);
		double critical = new BigDecimal(5).subtract(new BigDecimal(rbCylinder), DIGITS)
				.divide(cosine, DIGITS).doubleValue();
		for (double offset : new double[] {-0.01, 0.01}) {
			PreG9BCu0Corpus.setNumber(app, "excCyl", critical + offset);
			Result result = certificate(app, "o");
			String expected = offset < 0 ? "COMPLETE/true/0/0" : "COMPLETE/false/2/0";
			if (!expected.equals(shape(result))) {
				problems.add("rbCylinder " + rbCylinder + " critical " + critical + " offset "
						+ offset + ": " + shape(result));
			}
			if (result == previous) {
				problems.add("a certificate was reused across revisions");
			}
			// GC-8 in the variants of V-CRIT-9.
			if (!"within".equals(budgets(result))) {
				problems.add("rbCylinder " + rbCylinder + " offset " + offset + ": "
						+ budgets(result));
			}
			previous = result;
		}
		return problems;
	}

	private static void compare(List<String> problems, String step, String expected,
			String actual) {
		if (!expected.equals(actual)) {
			problems.add(step + ": expected " + expected + " but was " + actual);
		}
	}

	/** @return certificate and selection signatures of the first family */
	private static String signatures(AppGeoCeDG app) throws Exception {
		StringBuilder signatures = new StringBuilder();
		for (String label : FIRST_FAMILY) {
			GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
			Result result = certificate(app, label);
			signatures.append(label).append('=')
					.append(result.getStructure().getSemanticSignature().hashCode()).append('/')
					.append(evidence(result)).append('/')
					.append(evaluator(locus).captureCertifiedProgramV3().orElseThrow()
							.getSignature().hashCode())
					.append(';');
		}
		return signatures.toString();
	}

	private static String evidence(Result result) {
		StringBuilder text = new StringBuilder();
		for (ExactEvidence evidence : result.getExactEvidence()) {
			text.append(evidence.getSignature().hashCode()).append(',');
		}
		return text.toString();
	}

	/** @return the program with the centre of its circle through the driver moved in y */
	private static CertifiedConstructionProgram2D movedCentre(
			CertifiedConstructionProgram2D program, double shift) {
		List<Step> steps = new ArrayList<>();
		int moved = 0;
		for (Step step : program.getSteps()) {
			List<Operand> operands = step.getOperands();
			if (step.getOperation() == Operation.CIRCLE_THROUGH
					&& operands.get(0).getKind() == OperandKind.POINT) {
				Operand centre = operands.get(0);
				operands = List.of(Operand.point(centre.getValue(0),
						centre.getValue(1) + shift * centre.getValue(2), centre.getValue(2),
						centre.getValue(3), centre.getValue(4) + shift), operands.get(1));
				moved++;
			}
			steps.add(new Step(step.getOperation(), operands, step.getDescriptor(),
					parameters(step)));
		}
		if (moved != 1) {
			throw new IllegalStateException("Expected one circle through the driver: " + moved);
		}
		double[] constants = new double[8];
		for (int index = 0; index < constants.length; index++) {
			constants[index] = program.getDriverConstant(index);
		}
		return new CertifiedConstructionProgram2D(program.getDriver(), constants,
				program.getDomain(), steps);
	}

	private static double[] parameters(Step step) {
		List<Double> values = new ArrayList<>();
		for (int index = 0;; index++) {
			try {
				values.add(step.getParameter(index));
			} catch (IndexOutOfBoundsException exception) {
				break;
			}
		}
		double[] result = new double[values.size()];
		for (int index = 0; index < result.length; index++) {
			result[index] = values.get(index);
		}
		return result;
	}

	/** @return {@code t_c - beta} and {@code t_c + beta} with 50 digits */
	private static BigDecimal[] reference(AppGeoCeDG app, double rbCone, double rbCylinder,
			double excCyl) {
		BigDecimal sine = new BigDecimal(rbCone).divide(distancePB(app), DIGITS);
		BigDecimal thetaP = inverse(sine, false);
		BigDecimal tc = PI.divide(BigDecimal.valueOf(2), DIGITS).subtract(thetaP, DIGITS);
		BigDecimal cosine = BigDecimal.ONE.subtract(sine.multiply(sine, DIGITS), DIGITS)
				.sqrt(DIGITS);
		BigDecimal argument = new BigDecimal(rbCone).subtract(new BigDecimal(excCyl)
				.multiply(cosine, DIGITS), DIGITS).divide(new BigDecimal(rbCylinder), DIGITS);
		BigDecimal beta = inverse(argument, true);
		return new BigDecimal[] {tc.subtract(beta, DIGITS), tc.add(beta, DIGITS)};
	}

	private static BigDecimal cosThetaP(AppGeoCeDG app, double rbCone) {
		BigDecimal sine = new BigDecimal(rbCone).divide(distancePB(app), DIGITS);
		return BigDecimal.ONE.subtract(sine.multiply(sine, DIGITS), DIGITS).sqrt(DIGITS);
	}

	/** @return {@code |PB|} from the captured binary64 coordinates */
	private static BigDecimal distancePB(AppGeoCeDG app) {
		GeoPoint p = (GeoPoint) app.getKernel().lookupLabel("P");
		GeoPoint b = (GeoPoint) app.getKernel().lookupLabel("B");
		BigDecimal dx = new BigDecimal(p.getInhomX()).subtract(new BigDecimal(b.getInhomX()));
		BigDecimal dy = new BigDecimal(p.getInhomY()).subtract(new BigDecimal(b.getInhomY()));
		return dx.multiply(dx, DIGITS).add(dy.multiply(dy, DIGITS), DIGITS).sqrt(DIGITS);
	}

	/**
	 * V-PER-3: the drivers of {@code o_1} at which {@code n_1 = Ray(I, E_1)} is vertical and
	 * so coincides with the vertical {@code b_1} through {@code J_1}, whose abscissa is that of
	 * {@code E_1 = B + rbCone (cos t, sin t)}.
	 *
	 * @return {@code -t*} and {@code t*}, {@code t* = arccos((x_I - x_B) / rbCone)}, with 50
	 *         digits from the captured binary64 coordinates
	 */
	private static List<BigDecimal> coincidentVerticals(AppGeoCeDG app, double rbCone) {
		GeoPoint i = (GeoPoint) app.getKernel().lookupLabel("I");
		GeoPoint b = (GeoPoint) app.getKernel().lookupLabel("B");
		BigDecimal ratio = new BigDecimal(i.getInhomX()).subtract(new BigDecimal(b.getInhomX()))
				.divide(new BigDecimal(rbCone), DIGITS);
		BigDecimal root = inverse(ratio, true);
		return List.of(root.negate(), root);
	}

	/** Newton inversion of cos ({@code acos}) or sin ({@code asin}) with 50 digits. */
	private static BigDecimal inverse(BigDecimal value, boolean cosine) {
		BigDecimal angle = new BigDecimal(cosine ? Math.acos(value.doubleValue())
				: Math.asin(value.doubleValue()));
		for (int iteration = 0; iteration < 8; iteration++) {
			BigDecimal residual = series(angle, cosine).subtract(value, DIGITS);
			BigDecimal slope = series(angle, !cosine);
			angle = cosine ? angle.add(residual.divide(slope, DIGITS), DIGITS)
					: angle.subtract(residual.divide(slope, DIGITS), DIGITS);
		}
		return angle;
	}

	/** Taylor series of cos or sin with 50 digits, for arguments below 4 in magnitude. */
	private static BigDecimal series(BigDecimal x, boolean cosine) {
		BigDecimal term = cosine ? BigDecimal.ONE : x;
		BigDecimal sum = term;
		BigDecimal square = x.multiply(x, DIGITS);
		for (int n = 1; n < 80; n++) {
			long denominator = cosine ? (2L * n - 1) * (2L * n) : (2L * n) * (2L * n + 1);
			term = term.multiply(square, DIGITS).negate().divide(
					BigDecimal.valueOf(denominator), DIGITS);
			sum = sum.add(term, DIGITS);
		}
		return sum;
	}

	private static boolean encloses(LocusBoundary2D boundary, double reference) {
		return boundary.hasEnclosure() && boundary.getEnclosureLower() <= reference
				&& reference <= boundary.getEnclosureUpper();
	}

	/** @return the published certificates and points of the first family, bit for bit */
	private static String published(AppGeoCeDG app) {
		StringBuilder text = new StringBuilder();
		for (String label : FIRST_FAMILY) {
			LocusDefinition2D definition = PreG9BCu0Corpus.locus(app, label)
					.getSemanticDefinition();
			text.append(label).append('=').append(definition.getBranch(PreG9BCu0Corpus.MAIN)
					.getExistenceStructure().getSemanticSignature());
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(16)) {
				for (double t : new double[] {-Math.PI, -2, -1, 0, 1, 2, 3}) {
					LocusEvaluation2D evaluation = definition.evaluate(PreG9BCu0Corpus.MAIN, t,
							session);
					text.append('|').append(evaluation.getStatus());
					if (evaluation.isValid()) {
						text.append(':').append(Double.toHexString(evaluation.getPoint().getX()))
								.append(',')
								.append(Double.toHexString(evaluation.getPoint().getY()));
					}
				}
			}
			text.append(';');
		}
		return text.toString();
	}

	private static String shape(Result result) {
		LocusExistenceStructure2D structure = result.getStructure();
		return structure.getCompleteness() + "/" + structure.isClosedPeriodic() + "/"
				+ result.getCertifiedTransitions() + "/" + result.getUnresolvedBands();
	}

	/** GC-4 and GC-8: boxes and exact certificates within their budgets. */
	private static String budgets(Result result) {
		Policy policy = Policy.DEFAULT;
		if (result.getExistenceBoxes() > policy.getBoxBudget()) {
			return "boxes " + result.getExistenceBoxes();
		}
		for (ExactEvidence evidence : result.getExactEvidence()) {
			if ("BUDGET_EXHAUSTED".equals(evidence.getStatus())
					|| evidence.getMaximumCoefficientBits()
							> policy.getExactBudget().maximumCoefficientBits()
					|| evidence.getMaximumRootBisections()
							> policy.getExactBudget().maximumBisectionsPerRoot()) {
				return "exact " + evidence;
			}
		}
		return "within";
	}

	private static String stationary(Result result) {
		StringBuilder text = new StringBuilder();
		for (CertifiedStationaryPoint point : result.getStationaryPoints()) {
			text.append(text.length() == 0 ? "" : ",").append(point.getKind()).append(':')
					.append(point.getSymmetry().substring(point.getSymmetry().indexOf(':') + 1))
					.append(':').append(Double.toHexString(point.getParameterLower()));
		}
		return text.toString();
	}

	/** @return the published folds, each with whether its band is the isolation box of a fold */
	private static String folds(Result result) {
		StringBuilder text = new StringBuilder();
		for (LocusCertifiedFold2D fold : result.getStructure().getCertifiedFolds()) {
			boolean band = result.getStationaryPoints().stream().anyMatch(point ->
					point.getKind() == CertifiedStationaryPoint.StationaryKind.FOLD
							&& point.getIsolationLower() == fold.getBandLower()
							&& point.getIsolationUpper() == fold.getBandUpper());
			text.append(text.length() == 0 ? "" : ",")
					.append(Double.toHexString(fold.getParameterLower())).append(':').append(band);
		}
		return text.toString();
	}

	private static String end(LocusBoundary2D boundary) {
		return boundary.getKind() + ":" + boundary.getSource().getPredicate() + ":"
				+ boundary.getSource().getSide() + ":" + boundary.isIncluded() + ":"
				+ boundary.getNature();
	}

	private static String coverage(LocusExistenceStructure2D structure) {
		StringBuilder text = new StringBuilder();
		for (Coverage item : structure.getCoverage()) {
			text.append(text.length() == 0 ? "" : ";").append(item.getInterval()).append(':')
					.append(item.getState())
					.append(item.getState() == CoverageState.UNRESOLVED ? ":" + item.getCause()
							: "");
		}
		return text.toString();
	}

	private static Coverage coverageAt(LocusExistenceStructure2D structure, double parameter) {
		for (Coverage item : structure.getCoverage()) {
			LocusInterval2D interval = item.getInterval();
			if (interval.contains(parameter, 0)) {
				return item;
			}
		}
		throw new IllegalStateException("No coverage at " + parameter);
	}

	private static String status(GeoLocusV2 locus, double parameter) {
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			LocusEvaluation2D evaluation = locus.evaluate(PreG9BCu0Corpus.MAIN, parameter,
					session);
			return evaluation.getStatus().name();
		}
	}

	private static long metricReads(GeoLocusV2 locus) {
		return locus.getInstrumentation().getCertificationWork(
				LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION)
				+ locus.getInstrumentation().getNestedCertificationWork(
						LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION);
	}

	/**
	 * V-DET-1 on the author model (corrective descendant T13-R4): two runs, each opening the
	 * complete model anew and taking it through the same changes (the driver to
	 * {@code t = -1}; excCyl to 2.9, 3.7 and 3.85, to the last binary64 of penetration and the
	 * first of the bite, to 7.0, where the cover is empty, and back to 2.9; then rbCone to 8,
	 * where the construction degenerates), give results identical bit for bit at every step
	 * for every locus of the model, {@code e_2} included, and again after undoing and redoing
	 * the last two changes and after reopening the saved document: the semantic revision, the
	 * slice fingerprint and the published branch (coverage, components with their ends,
	 * brackets and sources, singular parameters, folds and the branch lineage), the full
	 * signatures of the roots, exact evidence and stationary points and the box count, the
	 * lineage keys and transitions, the events of the slice (settlement outcome, fidelity
	 * report, captured selection rule and the work of certification) and the structure of
	 * every metric with its events. The model file carries the durable identities, so nothing
	 * is normalized.
	 */
	@Test
	void twoRunsOfTheAuthorModelGiveIdenticalResults() throws Exception {
		File model = PreG9BCu0Corpus.workingCopy(PreG9BCu0Corpus.CEDG, work);
		String first = PreG9BR6PlusF3Fixtures.onEdt(() -> run(model, work));
		String second = PreG9BR6PlusF3Fixtures.onEdt(() -> run(model, work));
		assertEquals(first, second);
		for (String part : List.of("e_2 revision=", "|component=", "|fold=", "TRANSITION[",
				"|coefficients=", "ORIGIN_WRAP", "UNRESOLVED", "SPLIT[", "MERGED[",
				"DISAPPEARED[", "APPEARED[", "settlement=", " rule=2LINE_CIRCLE",
				"CERTIFICATE_COMPUTATION=", "undo 0", "redo 1", "reopened")) {
			assertTrue(first.contains(part), "the runs compare " + part);
		}
	}

	/**
	 * One run of V-DET-1 on a newly opened model: the changes, the last two undone and
	 * redone, then the document saved and reopened.
	 */
	private static String run(File model, Path work) throws Exception {
		// The run retires the embedded hosts it opened.
		try (G9U1TestApp.GlobalRoots roots = G9U1TestApp.GlobalRoots.capture()) {
			AppGeoCeDG app = PreG9BCu0Corpus.open(model);
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			PreG9BR6PlusF3Fixtures.baseline(app);
			StringBuilder text = new StringBuilder(results(app, "opened"));
			double penetration = Double.longBitsToDouble(LAST_PENETRATION);
			for (double value : new double[] {2.9, 3.7, 3.85, penetration,
					Math.nextUp(penetration), 7.0, 2.9}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", value);
				PreG9BR6PlusF3Fixtures.store(app);
				text.append(results(app, "excCyl=" + Double.toHexString(value)));
			}
			PreG9BCu0Corpus.setNumber(app, "rbCone", 8);
			PreG9BR6PlusF3Fixtures.store(app);
			text.append(results(app, "rbCone=8"));
			for (int step = 0; step < 2; step++) {
				app.getKernel().getConstruction().getUndoManager().undo();
				PreG9BR6PlusF3Fixtures.awaitQuiescent(app);
				text.append(results(app, "undo " + step));
			}
			for (int step = 0; step < 2; step++) {
				app.getKernel().getConstruction().getUndoManager().redo();
				PreG9BR6PlusF3Fixtures.awaitQuiescent(app);
				text.append(results(app, "redo " + step));
			}
			app = PreG9BCu0Corpus.open(PreG9BR6PlusF3Fixtures.save(app, work, "determinism"));
			return text.append(results(app, "reopened")).toString();
		}
	}

	/** @return every result of the model that a second run must reproduce bit for bit */
	private static String results(AppGeoCeDG app, String step) throws Exception {
		StringBuilder text = new StringBuilder("step ").append(step).append('\n');
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (geo.getParentAlgorithm() instanceof AlgoDependentPointLocusV2) {
				AlgoDependentPointLocusV2 algorithm =
						(AlgoDependentPointLocusV2) geo.getParentAlgorithm();
				GeoLocusV2 locus = (GeoLocusV2) geo;
				LocusBranch2D branch = locus.getSemanticDefinition() == null ? null
						: locus.getSemanticDefinition().getBranch(PreG9BCu0Corpus.MAIN);
				text.append(geo.getLabelSimple()).append(" revision=")
						.append(locus.getSemanticRevision()).append(" fingerprint=")
						.append(ReconstructionSliceFingerprint2D.compute(
								algorithm.getReconstructionSliceGeos(),
								List.of((GeoElement) field(algorithm, "state"),
										(GeoElement) field(algorithm, "coordinate"))))
						.append("\n branch=").append(branch == null ? "none"
								: branch.getSemanticSignature());
				Result result = algorithm.getHybridExistenceCertificate();
				if (result != null) {
					StringBuilder roots = new StringBuilder();
					result.getRoots().forEach(root -> roots.append(root.getSignature())
							.append(';'));
					StringBuilder exact = new StringBuilder();
					result.getExactEvidence().forEach(evidence -> exact.append(
							evidence.getSignature()).append(';'));
					StringBuilder stationary = new StringBuilder();
					result.getStationaryPoints().forEach(point -> stationary.append(
							point.getSignature()).append(';'));
					text.append("\n roots=").append(roots)
							.append("\n exact=").append(exact)
							.append("\n stationary=").append(stationary)
							.append("\n boxes=").append(result.getExistenceBoxes())
							.append("\n keys=").append(LocusComponentLineage2D.keys(
									result.getStructure()))
							.append("\n lineage=").append(algorithm.getHybridLineage());
				}
				if (locus.getSemanticDefinition() != null
						&& locus.getSemanticDefinition().getEvaluatorCapability()
								instanceof ReconstructibleLocusEvaluator2D) {
					// The events of the slice: settlement, departures and the captured rule.
					ReconstructibleLocusEvaluator2D evaluator = (ReconstructibleLocusEvaluator2D)
							locus.getSemanticDefinition().getEvaluatorCapability();
					text.append("\n settlement=").append(evaluator.getSettlementOutcome()
							.getToken()).append(" fidelity=").append(evaluator.getFidelityReport())
							.append("\n rule=").append(evaluator.captureCertifiedProgramV3()
									.map(CertifiedConstructionProgram2D::getSelectionSignature)
									.orElse("refused " + evaluator.getProgramV3Refusal()));
				}
				text.append("\n work=");
				for (LocusCertificationWork2D kind : LocusCertificationWork2D.values()) {
					text.append(kind.name()).append('=').append(locus.getInstrumentation()
							.getCertificationWork(kind)).append(' ');
				}
				text.append('\n');
			} else if (geo.getParentAlgorithm() instanceof AlgoLocusBetweenMetricV2) {
				text.append(geo.getLabelSimple()).append(" metric=").append(
						((AlgoLocusBetweenMetricV2) geo.getParentAlgorithm())
								.getFunctionStructure().getSignature()).append('\n');
			}
		}
		return text.toString();
	}

	private static Object field(Object target, String name)
			throws ReflectiveOperationException {
		Field field = AlgoDependentPointLocusV2.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(target);
	}

	private static Result certificate(AppGeoCeDG app, String label) {
		Result result = ((AlgoDependentPointLocusV2) PreG9BCu0Corpus.locus(app, label)
				.getParentAlgorithm()).getHybridExistenceCertificate();
		if (result == null) {
			throw new IllegalStateException("No certificate for " + label);
		}
		return result;
	}

	private static ReconstructibleLocusEvaluator2D evaluator(GeoLocusV2 locus)
			throws ReflectiveOperationException {
		Field field = AlgoDependentPointLocusV2.class.getDeclaredField("evaluator");
		field.setAccessible(true);
		return (ReconstructibleLocusEvaluator2D) field.get(locus.getParentAlgorithm());
	}

	/** @return the rich length of the slice copy behind the metric input of the program */
	@SuppressWarnings("unchecked")
	private static AlgoLocusBetweenMetricV2 sliceLength(ReconstructibleLocusEvaluator2D evaluator)
			throws ReflectiveOperationException {
		Field field = ReconstructibleLocusEvaluator2D.class.getDeclaredField(
				"programV3MetricSources");
		field.setAccessible(true);
		Map<Integer, AlgoLocusBetweenMetricV2> sources =
				(Map<Integer, AlgoLocusBetweenMetricV2>) field.get(evaluator);
		return sources.values().iterator().next();
	}
}
