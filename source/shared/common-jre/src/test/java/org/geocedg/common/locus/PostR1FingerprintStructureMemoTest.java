/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.ReconstructionSliceFingerprint2D.StructureMemo;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * Post-R1, D-V6: inside an isolated evaluation copy the structure part of the fingerprint of
 * a nested locus is reused, because such a copy is never edited, while its values, selection
 * and labels are read afresh at every update. The reuse must never keep a nested function
 * whose inputs changed.
 */
final class PostR1FingerprintStructureMemoTest extends G9U0PublicSurfaceTestBase {

	@Test
	void aNestedLocusOfTheSameDriverReusesItsStructureInTheCopy() throws Exception {
		add("c=Circle((0,0),2)");
		add("E=Point(c)");
		add("QF=(1,0)");
		add("M=Midpoint(E,QF)");
		assertNotNull(add("L1=LocusV2(M,E)"));
		add("H=(4,0)");
		add("r=Ray(H,E)");
		moveOnCircle("E", 0, 0, 2, 3.0);
		GeoLocusIntersectionResult rich = add("v=Intersect(L1,r)");
		List<LocusIntersectionSolution2D> roots =
				rich.getIntersectionResult().getFiniteSolutions();
		assertTrue(roots.size() >= 1, "the ray through E meets the circle of midpoints");
		assertNotNull(add("RA=Intersect(v,\"" + roots.get(0).getIdentity().getRootToken()
				+ "\")"));
		add("ZP=Midpoint(RA,E)");
		GeoLocusV2 consumer = add("Z=LocusV2(ZP,E)");
		assertNotNull(consumer);
		getKernel().updateConstruction(false);
		assertNull(producer(consumer).getStructureMemo(), "a live document never reuses");

		for (double parameter : new double[] {2.6, 2.8, 3.0, 3.1, -3.0, -2.8, 2.9}) {
			consumer.evaluate(BRANCH, parameter, LocusEvaluationSession2D.reference());
		}
		StructureMemo memo = nestedMemo(consumer, "L1");
		assertNotNull(memo, "the nested copy has a memo");
		assertTrue(memo.getReuses() >= 6, "reused at every driver sample: " + memo.getReuses());
		assertTrue(memo.getComputations() <= 3, "computed only while the copy was filled: "
				+ memo.getComputations());
	}

	@Test
	void aNestedDefinitionThatFollowsTheOuterDriverIsNeverStale() {
		// The nested locus Lt traces over its own driver E2; its definition reads QT, which
		// follows T, the driver of the consumer: every sample of the consumer is another
		// nested function.
		add("c=Circle((0,0),2)");
		add("E2=Point(c)");
		add("k=Circle((0,0),0.6)");
		add("T=Point(k)");
		add("QO=(2,0)");
		add("QT=Midpoint(T,QO)");
		add("Mt=Midpoint(E2,QT)");
		assertNotNull(add("Lt=LocusV2(Mt,E2)"));
		add("HA=(4,0.1)");
		add("HB=(-3,0.2)");
		add("l=Line(HA,HB)");
		moveOnCircle("T", 0, 0, 0.6, 0.4);
		GeoLocusIntersectionResult rich = add("v=Intersect(Lt,l)");
		List<LocusIntersectionSolution2D> roots =
				rich.getIntersectionResult().getFiniteSolutions();
		assertEquals(2, roots.size());
		assertNotNull(add("RA=Intersect(v,\"" + roots.get(0).getIdentity().getRootToken()
				+ "\")"));
		add("ZP=Midpoint(RA,T)");
		GeoLocusV2 consumer = add("Zt=LocusV2(ZP,T)");
		assertNotNull(consumer);
		getKernel().updateConstruction(false);

		double[] parameters = {0.4, 1.1, 2.0, -2.5, -1.2, 0.0, 2.9};
		double[][] evaluated = new double[parameters.length][];
		for (int index = 0; index < parameters.length; index++) {
			LocusEvaluation2D evaluation = consumer.evaluate(BRANCH, parameters[index],
					LocusEvaluationSession2D.reference());
			assertTrue(evaluation.isValid(), parameters[index] + ": " + evaluation.getStatus());
			evaluated[index] = new double[] {evaluation.getPoint().getX(),
					evaluation.getPoint().getY()};
		}
		// Reference, independent of the kernel: Lt is the circle of centre QT / 2 and radius 1,
		// with QT = (T + QO) / 2; the traced point is the midpoint of T and one fixed root of
		// that circle with the line through HA and HB.
		int root = -1;
		double distinct = 0;
		for (int index = 0; index < parameters.length; index++) {
			double tx = 0.6 * Math.cos(parameters[index]);
			double ty = 0.6 * Math.sin(parameters[index]);
			double cx = (tx + 2) / 4;
			double cy = ty / 4;
			double dx = -7;
			double dy = 0.1;
			double fx = 4 - cx;
			double fy = 0.1 - cy;
			double a = dx * dx + dy * dy;
			double b = 2 * (dx * fx + dy * fy);
			double c = fx * fx + fy * fy - 1;
			double discriminant = Math.sqrt(b * b - 4 * a * c);
			double[] error = new double[2];
			for (int k = 0; k < 2; k++) {
				double s = (-b + (k == 0 ? -discriminant : discriminant)) / (2 * a);
				error[k] = Math.hypot(evaluated[index][0] - (4 + s * dx + tx) / 2,
						evaluated[index][1] - (0.1 + s * dy + ty) / 2);
			}
			if (root < 0) {
				root = error[0] < error[1] ? 0 : 1;
			}
			assertEquals(0.0, error[root], 1E-9, "the same root of the nested function at "
					+ parameters[index]);
			distinct = Math.max(distinct, Math.hypot(evaluated[index][0] - evaluated[0][0],
					evaluated[index][1] - evaluated[0][1]));
		}
		assertTrue(distinct > 0.1, "the samples are different nested functions");
	}

	private void moveOnCircle(String label, double cx, double cy, double radius, double angle) {
		GeoPoint point = (GeoPoint) getKernel().lookupLabel(label);
		point.setCoords(cx + radius * Math.cos(angle), cy + radius * Math.sin(angle), 1.0);
		point.updateCascade();
	}

	private static AlgoDependentPointLocusV2 producer(GeoLocusV2 locus) {
		return (AlgoDependentPointLocusV2) locus.getParentAlgorithm();
	}

	/** @return the memo of the copy of a nested locus inside the slice of a consumer */
	private static StructureMemo nestedMemo(GeoLocusV2 consumer, String nestedLabel)
			throws ReflectiveOperationException {
		Object evaluator = consumer.getSemanticDefinition().getEvaluatorCapability();
		Field field = evaluator.getClass().getDeclaredField("isolatedConstruction");
		field.setAccessible(true);
		Construction copy = (Construction) field.get(evaluator);
		GeoLocusV2 nested = (GeoLocusV2) copy.lookupLabel(nestedLabel);
		return producer(nested).getStructureMemo();
	}
}
