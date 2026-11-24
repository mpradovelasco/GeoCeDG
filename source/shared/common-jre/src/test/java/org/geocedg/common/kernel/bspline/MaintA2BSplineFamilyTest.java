/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.algos.AlgoBSpline;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.PolynomialSpanCoefficients2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geocedg.common.math.bspline.BSplineData;
import org.geocedg.common.math.bspline.BSplineEvaluation;
import org.geocedg.common.math.bspline.BSplinePoint;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-A2: the family {@code bspline-2d/v1} through the public command and the
 * Locus V2 surface (matrix A-N1 BS-1, BS-2, BS-4 to BS-7, BS-9 to BS-12, BS-17 to BS-19,
 * BS-23 and the reader part of BS-24).
 */
public class MaintA2BSplineFamilyTest extends BaseUnitTest {
	private static final String BRANCH = AlgoBSpline.BRANCH_KEY;

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	private void points() {
		for (String point : new String[] {"A=(0,0)", "B=(1,2)", "C=(3,2.5)", "D=(4,0.5)",
				"E=(6,1)", "F=(7,3)", "G=(5,4)"}) {
			add(point);
		}
	}

	private static AlgoBSpline algo(GeoLocusV2 locus) {
		return (AlgoBSpline) locus.getParentAlgorithm();
	}

	private static double[] at(GeoLocusV2 locus, double u) {
		LocusDefinition2D definition = locus.getSemanticDefinition();
		LocusBranch2D branch = definition.getBranches().get(0);
		LocusEvaluation2D evaluation = definition.getEvaluatorCapability()
				.evaluate(definition, branch, u,
						org.geocedg.common.kernel.locus.LocusEvaluationSession2D.reference());
		assertTrue(evaluation.isValid(), evaluation.getDiagnostic());
		return new double[] {evaluation.getPoint().getX(), evaluation.getPoint().getY()};
	}

	@Test
	void bs01DegreeOneIsTheControlPolygonAndClosesOnlyExactly() {
		points();
		GeoLocusV2 polygon = add("b1=BSpline({A,B,C,D},1)");
		assertTrue(polygon.isDefined());
		assertArrayEquals(new double[] {1, 2}, at(polygon, 1), 0);
		assertArrayEquals(new double[] {3, 2.5}, at(polygon, 2), 0);
		assertArrayEquals(new double[] {0.5, 1}, at(polygon, 0.5), 1E-15);
		assertTrue(polygon.getSemanticDefinition().getBranches().get(0).getProvenance()
				.contains("continuity=0x1.0p0:C0,0x1.0p1:C0"));
		assertFalse(polygon.getSemanticDefinition().getProvider().isPeriodic());
		GeoLocusV2 closed = add("b2=BSpline({A,B,C,A},1)");
		assertTrue(closed.getSemanticDefinition().getProvider().isPeriodic());
		assertTrue(closed.getSemanticDefinition().getBranches().get(0).getProvenance()
				.endsWith("|seam=C0"));
		add("Z=(1E-15,0)");
		GeoLocusV2 near = add("b3=BSpline({A,B,C,Z},1)");
		assertFalse(near.getSemanticDefinition().getProvider().isPeriodic());
	}

	@Test
	void bs02CubicBezierStartsAtTheFirstControlPoint() {
		points();
		GeoLocusV2 cubic = add("b=BSpline({A,B,C,D})");
		assertEquals(Commands.BSpline, cubic.getParentAlgorithm().getClassName());
		assertArrayEquals(new double[] {0, 0}, at(cubic, 0), 0);
		assertArrayEquals(new double[] {4, 0.5}, at(cubic, 1), 0);
		assertEquals("B-spline", cubic.translatedTypeString());
		assertEquals(BSplineContractXml.KNOTS_CLAMPED_UNIFORM_V1, algo(cubic).getKnotRule());
		// the default degree is promoted to a labelled number, as for SplineV2
		assertEquals(2, cubic.getParentAlgorithm().getInput().length);
		assertTrue(cubic.getParentAlgorithm().getInput(1).isLabelSet());
	}

	@Test
	void bs04ContinuityFollowsTheMultiplicityExactly() {
		points();
		GeoLocusV2 double2 = add("m2=BSpline({A,B,C,D,E,F},3,{0,0,0,0,1,1,2,2,2,2})");
		assertTrue(double2.getSemanticDefinition().getBranches().get(0).getProvenance()
				.contains("continuity=0x1.0p0:C1"));
		GeoLocusV2 triple = add("m3=BSpline({A,B,C,D,E,F,G},3,{0,0,0,0,1,1,1,2,2,2,2})");
		assertTrue(triple.getSemanticDefinition().getBranches().get(0).getProvenance()
				.contains("continuity=0x1.0p0:C0"));
		// a knot of multiplicity p passes through a control point: corner, no G1 claim
		assertArrayEquals(new double[] {4, 0.5}, at(triple, 1), 0);
	}

	@Test
	void bs05TypedCausesPublishNoGeometryAndRecover() {
		points();
		GeoNumeric degree = add("p=3");
		GeoLocusV2 curve = add("b=BSpline({A,B,C,D,E},p)");
		assertTrue(curve.isDefined());
		degree.setValue(2.5);
		degree.updateCascade();
		assertCause(curve, BSplineInvalidity.DEGREE_INVALID);
		degree.setValue(8);
		degree.updateCascade();
		assertCause(curve, BSplineInvalidity.DEGREE_INVALID);
		degree.setValue(5);
		degree.updateCascade();
		assertCause(curve, BSplineInvalidity.TOO_FEW_CONTROL_POINTS);
		degree.setValue(3);
		degree.updateCascade();
		assertTrue(curve.isDefined());
		assertNull(algo(curve).getInvalidityCause());
		GeoPoint first = (GeoPoint) lookup("A");
		first.setUndefined();
		first.updateCascade();
		assertCause(curve, BSplineInvalidity.INPUT_UNDEFINED);
		first.setCoords(0, 0, 1);
		first.updateCascade();
		assertTrue(curve.isDefined());

		assertCause(add("k1=BSpline({A,B,C,D},3,{0,0,0,0,1,1,1})"),
				BSplineInvalidity.KNOT_COUNT_MISMATCH);
		assertCause(add("k2=BSpline({A,B,C,D},3,{0,0,0,0,1,1,1,0.5})"),
				BSplineInvalidity.KNOTS_DECREASING);
		assertCause(add("k3=BSpline({A,B,C,D},3,{0,0,0,0.5,1,1,1,1})"),
				BSplineInvalidity.NOT_CLAMPED);
		assertCause(add("k4=BSpline({A,B,C,D,E,F,G,A},3,{0,0,0,0,1,1,1,1,2,2,2,2})"),
				BSplineInvalidity.INTERIOR_MULTIPLICITY_EXCEEDED);
		assertCause(add("k5=BSpline({A,B,C,D},3,{0,0,0,0,1/0,1,1,1})"),
				BSplineInvalidity.NONFINITE_DATA);
		assertCause(add("k6=BSpline(Sequence((k,sin(k)),k,1,129),3)"),
				BSplineInvalidity.TOO_MANY_CONTROL_POINTS);
		assertCause(add("k7=BSpline({A,B,C})"), BSplineInvalidity.TOO_FEW_CONTROL_POINTS);
	}

	private static void assertCause(GeoLocusV2 curve, BSplineInvalidity cause) {
		assertNotNull(curve);
		assertFalse(curve.isDefined(), cause.name());
		assertNull(curve.getSemanticDefinition(), "no stale geometry: " + cause);
		assertEquals(cause, algo(curve).getInvalidityCause());
	}

	@Test
	void bs06DegeneracyIsDefined() {
		add("A=(1,1)");
		GeoLocusV2 collapsed = add("b=BSpline({A,A,A,A})");
		assertTrue(collapsed.isDefined());
		assertEquals(0, AlgoLocusMetricScalarAdapter.richResultOf(add("M=LocusLength(b)"))
				.getMetricResult().getMetricValue().getFiniteValue().orElseThrow(), 0);
		LocusDefinition2D definition = collapsed.getSemanticDefinition();
		assertEquals(Regularity.SINGULAR, definition.getEvaluatorCapability().evaluate(
				definition, definition.getBranches().get(0), 0.5, null).getRegularity());
	}

	@Test
	void bs09RootsOnKnotsAndSeamAreCountedOnce() {
		add("A=(0,-1)");
		add("B=(1,1)");
		add("C=(2,-1)");
		add("D=(3,1)");
		GeoLocusV2 polygon = add("b=BSpline({A,B,C,D},1)");
		add("level:y=1");
		GeoLocusIntersectionResult onKnot = add("R=Intersect(b,level)");
		List<LocusIntersectionSolution2D> roots =
				onKnot.getIntersectionResult().getFiniteSolutions();
		assertEquals(2, roots.size(), "one root per knot vertex B and D");
		add("P=(0,0)");
		add("Q=(2,0)");
		add("T=(1,2)");
		GeoLocusV2 closed = add("c=BSpline({P,Q,T,P},1)");
		assertTrue(closed.getSemanticDefinition().getProvider().isPeriodic());
		add("vertical:x=0");
		GeoLocusIntersectionResult seam = add("S=Intersect(c,vertical)");
		assertEquals(1, seam.getIntersectionResult().getFiniteSolutions().size(),
				"the seam root is owned by the first span only");
		assertNotNull(polygon);
	}

	@Test
	void bs10SimilarityImageForwardsTheLocalCoefficients() {
		points();
		GeoLocusV2 curve = add("b=BSpline({A,B,C,D,E},3)");
		GeoLocusV2 image = add("t=Translate(b,Vector((1,-2)))");
		add("A2=A+(1,-2)");
		add("B2=B+(1,-2)");
		add("C2=C+(1,-2)");
		add("D2=D+(1,-2)");
		add("E2=E+(1,-2)");
		GeoLocusV2 direct = add("d=BSpline({A2,B2,C2,D2,E2},3)");
		for (double u : new double[] {0, 0.3, 1, 1.5, 2}) {
			assertArrayEquals(at(direct, u), at(image, u), 1E-13);
		}
		LocusDefinition2D definition = image.getSemanticDefinition();
		PolynomialSpanCoefficients2D form = PolynomialSpanCoefficients2D.read(definition,
				(org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D) definition
						.getEvaluatorCapability(), BRANCH, 0);
		assertTrue(form.isLocal());
		assertNotNull(curve);
	}

	@Test
	void bs11MovingAControlPointChangesOnlyItsSupport() {
		points();
		GeoLocusV2 curve = add("b=BSpline({A,B,C,D,E,F,G},3)");
		BSplineSemanticEvaluator2D before = algo(curve).getEvaluator();
		GeoPoint moved = (GeoPoint) lookup("G");
		moved.setCoords(5.5, 4.25, 1);
		moved.updateCascade();
		BSplineSemanticEvaluator2D after = algo(curve).getEvaluator();
		// G = P_6 lies in the support of spans j = 6 only (j - p <= 6 <= j, j <= n = 6)
		int spans = after.getPolynomialSpanCount(BRANCH);
		for (int span = 0; span < spans; span++) {
			double[][] a = before.getLocalPolynomialCoordinateCoefficients(BRANCH, span);
			double[][] b = after.getLocalPolynomialCoordinateCoefficients(BRANCH, span);
			int knotIndex = after.getData().spanKnotIndex(span);
			boolean inSupport = knotIndex - 3 <= 6 && 6 <= knotIndex;
			if (inSupport) {
				assertFalse(java.util.Arrays.equals(a[0], b[0])
						&& java.util.Arrays.equals(a[1], b[1]), "span " + span);
			} else {
				assertArrayEquals(a[0], b[0], 0, "span " + span);
				assertArrayEquals(a[1], b[1], 0, "span " + span);
			}
		}
		GeoNumeric degree = (GeoNumeric) curve.getParentAlgorithm().getInput(1);
		degree.setValue(4);
		degree.updateCascade();
		assertTrue(curve.isDefined(), "a degree change recomputes without a locality claim");
		assertEquals(4, algo(curve).getEvaluator().getData().degree());
	}

	@Test
	void bs17BezierEquivalenceAndBs18OwnershipFollowTheLibrary() {
		points();
		for (int p = 1; p <= 6; p++) {
			StringBuilder list = new StringBuilder("{");
			String[] names = {"A", "B", "C", "D", "E", "F", "G"};
			for (int i = 0; i <= p; i++) {
				list.append(i == 0 ? "" : ",").append(names[i]);
			}
			GeoLocusV2 curve = add("z" + p + "=BSpline(" + list + "}," + p + ")");
			BSplineData data = algo(curve).getEvaluator().getData();
			assertEquals(1, data.spanCount());
			for (double u : new double[] {0, 0.25, 0.5, 0.75, 1}) {
				BSplinePoint point = BSplineEvaluation.point(data, u);
				assertArrayEquals(new double[] {point.x(), point.y()}, at(curve, u), 0,
						"the kernel evaluator is the library evaluation, bit for bit");
			}
		}
		GeoLocusV2 multi = add("w=BSpline({A,B,C,D,E,F,G},3)");
		BSplineData data = algo(multi).getEvaluator().getData();
		BSplinePoint right = BSplineEvaluation.pointInSpan(data, 4, 1);
		assertArrayEquals(new double[] {right.x(), right.y()}, at(multi, 1), 0);
		BSplinePoint last = BSplineEvaluation.pointInSpan(data, 6, 4);
		assertArrayEquals(new double[] {last.x(), last.y()}, at(multi, 4), 0);
	}

	@Test
	void bs19DefaultFormsRegenerateTheirKnotsWithoutReducingTheDegree() {
		points();
		GeoNumeric count = add("n=4");
		add("L=First({A,B,C,D,E,F},n)");
		GeoLocusV2 curve = add("b=BSpline(L,3)");
		assertEquals(1, algo(curve).getEvaluator().getData().spanCount());
		count.setValue(6);
		count.updateCascade();
		GeoLocusV2 grown = (GeoLocusV2) lookup("b");
		assertEquals(3, algo(grown).getEvaluator().getData().spanCount());
		assertEquals(3, algo(grown).getEvaluator().getData().degree());
		assertCause(add("short=BSpline({A,B},3)"), BSplineInvalidity.TOO_FEW_CONTROL_POINTS);
	}

	@Test
	void bs12SaveReloadKeepsBytesIdentityAndDependencies() {
		points();
		GeoLocusV2 curve = add("b=BSpline({A,B,C,D,E},3,{0,0,0,0,1.5,4,4,4,4})");
		add("M=LocusLength(b)");
		String identity = curve.getPersistentLocusId().toExternalForm();
		String saved = getApp().getXML();
		assertTrue(saved.contains("<geocedgBSpline contract=\"bspline-2d/v1\""
				+ " knots=\"explicit\"/>"));
		reload();
		GeoLocusV2 reopened = (GeoLocusV2) lookup("b");
		assertTrue(reopened.isDefined());
		assertEquals(identity, reopened.getPersistentLocusId().toExternalForm());
		assertEquals(saved, getApp().getXML());
		assertEquals(3, reopened.getParentAlgorithm().getInput().length);
	}

	@Test
	void bs24PassiveProvenanceRoundTripsUnchanged() {
		points();
		add("b=BSpline({A,B,C,D,E},3)");
		String block = "<geocedgBSpline contract=\"bspline-2d/v1\" knots=\"clamped-uniform/v1\">\n"
				+ "\t\t<approximationProvenance version=\"1\" source=\"sha256:00ff\""
				+ " method=\"m&amp;1\" tolerance=\"1.0E-4\"/>\n</geocedgBSpline>\n";
		String saved = getApp().getXML().replace(
				"<geocedgBSpline contract=\"bspline-2d/v1\" knots=\"clamped-uniform/v1\"/>\n",
				block);
		assertTrue(saved.contains(block));
		getApp().setXML(saved, true);
		assertTrue(lookup("b").isDefined());
		assertEquals(saved, getApp().getXML(), "save, reopen, save is byte-identical");
		assertTrue(algo((GeoLocusV2) lookup("b")).getPersistedContract()
				.hasApproximationProvenance());
		assertNotEquals(null, algo((GeoLocusV2) lookup("b")).getCapturedDefinition()
				.getDefinitionDigestHex());
	}

	@Test
	void bs23EvaluationNeverConsultsRenderData() {
		points();
		GeoLocusV2 curve = add("b=BSpline({A,B,C,D,E},3)");
		long render = curve.getInstrumentation().getRenderEvaluations();
		add("M=LocusLength(b)");
		add("line:y=1.5");
		add("R=Intersect(b,line)");
		assertEquals(render, curve.getInstrumentation().getRenderEvaluations());
	}
}
