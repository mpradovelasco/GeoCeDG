/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusSessionDiagnostic2D.Kind;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU1 revision stability and slice-scoped sessions ({@code locus-v2-semantics.md}
 * v1.2; validation matrix V-REV-2, V-REV-3, V-SESS-1 and V-SESS-2).
 */
class PreG9BCu1RevisionAndSessionTest extends G9U0PublicSurfaceTestBase {

	private static final String[][] CHANGES = {{"k", "2"}, {"A", "(2,-1)"}, {"r", "4"},
			{"f(x)", "x^2+1"}, {"txt", "\"abc\""}, {"bo", "false"}, {"lst", "{1,2,4}"}};

	/**
	 * V-REV-2: one change of each non-driver input class publishes a new revision whose
	 * samples and length equal, bit for bit, those of a freshly created construction with
	 * the same commands and values.
	 */
	@Test
	void everyNonDriverInputClassPublishesAnExactRevision() {
		Map<String, String> values = new LinkedHashMap<>();
		values.put("k", "1");
		values.put("A", "(1,2)");
		values.put("r", "3");
		values.put("f(x)", "x^2");
		values.put("txt", "\"ab\"");
		values.put("bo", "true");
		values.put("lst", "{1,2,3}");
		GeoLocusV2 live = construction("", values);
		int generation = 0;
		for (String[] change : CHANGES) {
			long revision = live.getSemanticRevision();
			values.put(change[0], change[1]);
			add(change[0] + "=" + change[1]);
			generation++;
			String where = "after " + change[0] + "=" + change[1];
			// A redefinition may rebuild the dependents under the same labels; a rebuilt
			// locus is a new semantic lineage, otherwise the revision must advance.
			GeoLocusV2 current = (GeoLocusV2) lookup("L");
			assertNotNull(current, where);
			assertTrue(current != live || live.getSemanticRevision() > revision, where);
			if (!change[0].equals("f(x)")) {
				// A value change of an independent input keeps the object.
				assertTrue(current == live, where);
			}
			live = current;
			GeoLocusV2 fresh = construction("g" + generation, values);
			assertEquals(samples(fresh), samples(live), where);
			assertEquals(Double.doubleToLongBits(length(lookupMetric("Mg" + generation))),
					Double.doubleToLongBits(length(lookupMetric("M"))), where);
		}
	}

	/**
	 * V-REV-3: a slice that reads a random value has no deterministic fingerprint across
	 * recomputations: every recomputation that changes it publishes a revision.
	 */
	@Test
	void randomSliceInputIsNeverFalselyReused() {
		add("cr=Circle((0,0),2)");
		add("Pr=Point(cr)");
		add("rnd=RandomBetween(1,1000000)");
		add("Qr=Pr+(rnd,0)");
		GeoLocusV2 locus = add("Lr=LocusV2(Qr,Pr)");
		GeoLocusMetricResult metric = add("Mr=LocusLength(Lr)");
		double value = ((org.geogebra.common.kernel.geos.GeoNumeric) lookup("rnd"))
				.getValue();
		long revision = locus.getSemanticRevision();
		int changes = 0;
		for (int round = 0; round < 5; round++) {
			getKernel().updateConstruction(true);
			double next = ((org.geogebra.common.kernel.geos.GeoNumeric) lookup("rnd"))
					.getValue();
			if (next != value) {
				changes++;
				assertTrue(locus.getSemanticRevision() > revision, "round " + round);
				revision = locus.getSemanticRevision();
				value = next;
				LocusPoint2D point = evaluate(locus, 0);
				assertEquals(next + 2, point.getX(), 1E-9, "round " + round);
			}
		}
		assertTrue(changes >= 4, "the random value changed " + changes + " times");
		assertTrue(metric.isDefined());
	}

	/**
	 * V-SESS-1: an evaluator that re-enters through a slice-scoped child session is a
	 * typed cycle; the shared active-key stack stops it without overflow.
	 */
	@Test
	void reentryThroughASliceScopedChildIsATypedCycle() {
		long scope = LocusEvaluationSession2D.newSliceScope();
		List<Kind> kinds = new ArrayList<>();
		LocusDefinition2D[] holder = new LocusDefinition2D[1];
		holder[0] = definition("child-cycle", (parameter, session) -> {
			LocusEvaluation2D[] nested = new LocusEvaluation2D[1];
			LocusEvaluationSession2D.runWithNestedServiceParent(session, scope, () -> {
				try (LocusEvaluationSession2D child =
						LocusEvaluationSession2D.openServiceSession(false, 1)) {
					assertTrue(child.isNestedServiceSession());
					nested[0] = holder[0].evaluate(BRANCH_KEY, parameter, child);
					kinds.add(child.getLastDiagnostic().getKind());
				}
			});
			return nested[0];
		});
		LocusEvaluationSession2D root = LocusEvaluationSession2D.memoizing(4);
		LocusEvaluation2D result = holder[0].evaluate(BRANCH_KEY, 0.25, root);
		assertEquals(EvaluationStatus.EVALUATION_FAILED, result.getStatus());
		assertTrue(kinds.contains(Kind.CYCLE_REENTRY), kinds.toString());
		assertEquals(0, root.getActiveDepth());
		assertFalse(root.isNestedServiceSession());
	}

	/**
	 * V-SESS-2: nested evaluations are charged to the root budget without interrupting the
	 * nested service; the root stops at its own boundary with a typed limit and publishes
	 * no partial value.
	 */
	@Test
	void nestedWorkIsChargedToTheRootAndStopsAtItsBoundary() {
		long scope = LocusEvaluationSession2D.newSliceScope();
		LocusDefinition2D inner = definition("inner", (parameter, session) ->
				point(parameter));
		int[] completedNested = {0};
		LocusDefinition2D outer = definition("outer", (parameter, session) -> {
			LocusEvaluationSession2D.runWithNestedServiceParent(session, scope, () -> {
				try (LocusEvaluationSession2D child =
						LocusEvaluationSession2D.openServiceSession(false, 1)) {
					for (int index = 0; index < 5; index++) {
						assertTrue(inner.evaluate(BRANCH_KEY, parameter / (index + 2),
								child).isValid());
						completedNested[0]++;
					}
				}
			});
			return point(parameter);
		});
		LocusEvaluationSession2D root =
				LocusEvaluationSession2D.memoizingWithMissLimit(64, 10);
		List<LocusEvaluation2D> published = new ArrayList<>();
		LocusEvaluationSession2D.EvaluationWorkLimitException limit = assertThrows(
				LocusEvaluationSession2D.EvaluationWorkLimitException.class, () -> {
					for (int index = 0; index < 10; index++) {
						published.add(outer.evaluate(BRANCH_KEY, 0.05 * (index + 1), root));
					}
				});
		assertNotNull(limit.getMessage());
		// The first outer evaluation (1 own + 5 nested misses) completes; the second
		// starts below the limit and completes its nested work; the third stops at the
		// root boundary before any work.
		assertEquals(2, published.size());
		assertEquals(10, completedNested[0]);
		assertEquals(12, root.getTotalChargedMisses());
		assertEquals(10, root.getNestedMisses());
		assertEquals(0, root.getActiveDepth());
	}

	private GeoLocusV2 construction(String suffix, Map<String, String> values) {
		for (Map.Entry<String, String> entry : values.entrySet()) {
			String name = entry.getKey().equals("f(x)") ? "f" + suffix + "(x)"
					: entry.getKey() + suffix;
			add(name + "=" + entry.getValue());
		}
		add("c" + suffix + "=Circle(A" + suffix + ",r" + suffix + "+f" + suffix + "(0))");
		add("P" + suffix + "=Point(c" + suffix + ")");
		add("Q" + suffix + "=P" + suffix + "+(k" + suffix + "*f" + suffix
				+ "(1)+If(bo" + suffix + ",0,1)+Length(txt" + suffix + ")+Sum(lst"
				+ suffix + "),0)");
		GeoLocusV2 locus = add("L" + suffix + "=LocusV2(Q" + suffix + ",P" + suffix + ")");
		assertNotNull(locus);
		assertNotNull(add("M" + suffix + "=LocusLength(L" + suffix + ")"));
		return locus;
	}

	private GeoLocusMetricResult lookupMetric(String label) {
		GeoLocusMetricResult metric = (GeoLocusMetricResult) lookup(label);
		assertNotNull(metric, label);
		return metric;
	}

	private static double length(GeoLocusMetricResult metric) {
		return metric.getMetricResult().getMetricValue().getFiniteValue().orElseThrow();
	}

	private static List<Long> samples(GeoLocusV2 locus) {
		List<Long> bits = new ArrayList<>();
		for (int index = 0; index < 16; index++) {
			LocusPoint2D point = evaluate(locus, -Math.PI + 2 * Math.PI * index / 16);
			bits.add(Double.doubleToLongBits(point.getX()));
			bits.add(Double.doubleToLongBits(point.getY()));
		}
		return bits;
	}

	private static LocusPoint2D evaluate(GeoLocusV2 locus, double parameter) {
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4)) {
			LocusEvaluation2D evaluation = locus.evaluate(BRANCH, parameter, session);
			assertTrue(evaluation.isValid(), evaluation.getDiagnostic());
			return evaluation.getPoint();
		}
	}

	private static final String BRANCH_KEY = "cu1.session.main";

	private static LocusDefinition2D definition(String identity, TestEvaluator evaluator) {
		ExplicitNumericDomainProvider2D provider = new ExplicitNumericDomainProvider2D(
				"cu1-session-parameter/v1", new LocusInterval2D(-1, 1, true, true),
				Orientation.INCREASING, false, 1E-14);
		LocusBranch2D branch = new LocusBranch2D(BRANCH_KEY, provider.getDeclaredDomain(),
				Collections.singletonList(provider.getDeclaredDomain()),
				Orientation.INCREASING, "cu1-session-fixture/v1",
				LocusLineage2D.unchanged(), EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());
		return new LocusDefinition2D(identity, 1, DefinitionStatus.VALID, provider,
				Collections.singletonList(branch),
				(definition, semanticBranch, parameter, session) ->
						evaluator.evaluate(parameter, session),
				Determinism.POINTWISE_DETERMINISTIC, identity + "/v1",
				new LocusInstrumentation2D());
	}

	private static LocusEvaluation2D point(double parameter) {
		return LocusEvaluation2D.valid(new LocusPoint2D(parameter, parameter),
				Regularity.UNKNOWN, LocusQuality2D.analyticDoubleSemantic());
	}

	private interface TestEvaluator {
		LocusEvaluation2D evaluate(double parameter, LocusEvaluationSession2D session);
	}
}
