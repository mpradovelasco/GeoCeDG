/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.ReconstructionSliceFingerprint2D;
import org.geocedg.common.kernel.locus.ReconstructionSliceFingerprint2D.Change;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * Post-R1 repair A': the position of the driver of a nested Locus V2, and what is computed
 * from it, is evaluation state of that nested locus when it reaches the traced point of the
 * consumer only through the output of a locus of that driver ({@code locus-v2-semantics.md},
 * PRE-G9B-CU1 amendment: the value of a Locus V2 output does not depend on the driver
 * position).
 *
 * <p>Fixture: the nested locus {@code L} is the circle traced by the midpoint of its driver
 * {@code E} and a point {@code QF = (t, 0)}; the consumer {@code Z}, driven by {@code T},
 * traces a point built from a root of {@code Intersect(L, Ray(H, T))}.</p>
 */
final class PostR1NestedLocusDriverFingerprintTest extends G9U0PublicSurfaceTestBase {
	private static final double[] PARAMETERS = {2.7, 2.85, 3.0, 3.1, -3.1, -2.9, -2.75, 1.0};

	@Test
	void theDriverOfANestedLocusPublishesNoRevisionOfItsConsumer() {
		build();
		GeoLocusV2 consumer = locus("Z");
		GeoLocusV2 nested = locus("L");
		AlgoLocusIntersectionV2 producer = (AlgoLocusIntersectionV2) getKernel()
				.lookupLabel("v").getParentAlgorithm();
		moveNestedDriver(getKernel(), 0.3);
		moveNestedDriver(getKernel(), 0.6);
		long revision = consumer.getSemanticRevision();
		long nestedRevision = nested.getSemanticRevision();
		Object evaluator = consumer.getSemanticDefinition().getEvaluatorCapability();
		long builds = consumer.getInstrumentation().getDependencySliceBuilds();
		long values = consumer.getInstrumentation().getSliceChanges(Change.VALUES);
		long resolutions = producer.getResolutionCount();
		String fingerprint = fingerprint(getKernel(), "Z", "T");
		List<String> reference = evaluations(consumer);

		for (double angle : new double[] {1.0, 2.2, -2.0, -0.4, 3.1}) {
			moveNestedDriver(getKernel(), angle);
			assertEquals(nestedRevision, nested.getSemanticRevision());
			assertEquals(revision, consumer.getSemanticRevision(), "revision at " + angle);
			assertSame(evaluator, consumer.getSemanticDefinition().getEvaluatorCapability());
			assertEquals(fingerprint, fingerprint(getKernel(), "Z", "T"));
		}
		assertEquals(builds, consumer.getInstrumentation().getDependencySliceBuilds());
		assertEquals(values, consumer.getInstrumentation().getSliceChanges(Change.VALUES));
		assertEquals(resolutions, producer.getResolutionCount(),
				"and the intersection of the nested locus is not resolved again");
		assertEquals(reference, evaluations(consumer), "same function, bit for bit");

		// Reference: the same document read back builds every slice at this driver position.
		AppCommon rebuilt = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		rebuilt.setXML(getApp().getXML(), true);
		assertEquals(fingerprint, fingerprint(rebuilt.getKernel(), "Z", "T"));
		assertEquals(reference, evaluations((GeoLocusV2) rebuilt.getKernel().lookupLabel("Z")),
				"the reused slice equals a slice rebuilt from the document");
	}

	@Test
	void aDirectRouteToTheTracedPointKeepsTheNestedDriverAsAnInput() {
		build();
		add("YD=Midpoint(RA,E)");
		GeoLocusV2 direct = add("Y=LocusV2(YD,T)");
		assertNotNull(direct);
		long revision = direct.getSemanticRevision();
		long consumerRevision = locus("Z").getSemanticRevision();
		List<String> reference = evaluations(direct);

		moveNestedDriver(getKernel(), 1.0);
		assertTrue(direct.getSemanticRevision() > revision, "E is an input of Y");
		assertNotEquals(reference, evaluations(direct));
		assertEquals(consumerRevision, locus("Z").getSemanticRevision(),
				"while Z still reads E only through L");
	}

	@Test
	void anInputOfTheNestedDefinitionAndItsSupportStillInvalidate() {
		build();
		GeoLocusV2 consumer = locus("Z");
		List<String> reference = evaluations(consumer);

		// L_t: the parameter t changes the nested definition, in the same element that
		// reads the nested driver.
		long revision = consumer.getSemanticRevision();
		GeoNumeric parameter = (GeoNumeric) getKernel().lookupLabel("t");
		parameter.setValue(1.25);
		parameter.updateCascade();
		assertTrue(consumer.getSemanticRevision() > revision, "t changes the nested function");
		assertNotEquals(reference, evaluations(consumer));
		parameter.setValue(1.0);
		parameter.updateCascade();
		assertEquals(reference, evaluations(consumer));

		// The support of the nested driver is not evaluation state.
		revision = consumer.getSemanticRevision();
		GeoPoint centre = (GeoPoint) getKernel().lookupLabel("OC");
		centre.setCoords(0.0, 0.125, 1.0);
		centre.updateCascade();
		assertTrue(consumer.getSemanticRevision() > revision, "support of the nested driver");
	}

	@Test
	void aPointOnAPathThatMovesWithTheNestedDriverKeepsItsOwnParameter() {
		// A nested locus whose traced point reads a point K on a circle centred at its driver.
		add("c=Circle((0,0),2)");
		add("E=Point(c)");
		add("kE=Circle(E,0.5)");
		add("K=Point(kE)");
		add("QF=(1,0)");
		add("P=Midpoint(K,QF)");
		assertNotNull(add("N=LocusV2(P,E)"));
		add("k=Circle((0.5,0),3)");
		add("T=Point(k)");
		add("H=(4,0)");
		add("r=Ray(H,T)");
		GeoLocusIntersectionResult rich = add("w=Intersect(N,r)");
		assertNotNull(rich);
		getKernel().updateConstruction(false);
		// The slice that a consumer driven by T would read through the intersection.
		List<GeoElement> slice = new ArrayList<>(rich.getAllPredecessors());
		slice.add(rich);
		GeoElement driver = getKernel().lookupLabel("T");
		ReconstructionSliceFingerprint2D.Snapshot before = ReconstructionSliceFingerprint2D
				.snapshot(slice, Arrays.asList(driver, driver));

		moveNestedDriver(getKernel(), 1.0);
		GeoPoint onPath = (GeoPoint) getKernel().lookupLabel("K");
		double parameter = onPath.getPathParameter().getT();
		ReconstructionSliceFingerprint2D.Snapshot moved = ReconstructionSliceFingerprint2D
				.snapshot(slice, Arrays.asList(driver, driver));
		assertEquals(before.getValuesDigest(), moved.getValuesDigest(),
				"the path moved with the nested driver; the place of K on it did not");
		assertEquals(Change.NONE, moved.changeFrom(before));

		// K moves along its own path: its parameter is an input of the nested function.
		GeoPoint nestedDriver = (GeoPoint) getKernel().lookupLabel("E");
		onPath.setCoords(nestedDriver.getInhomX() + 0.5 * Math.cos(parameter + 0.7),
				nestedDriver.getInhomY() + 0.5 * Math.sin(parameter + 0.7), 1.0);
		onPath.updateCascade();
		ReconstructionSliceFingerprint2D.Snapshot replaced = ReconstructionSliceFingerprint2D
				.snapshot(slice, Arrays.asList(driver, driver));
		assertEquals(Change.VALUES, replaced.changeFrom(moved), "own parameter of K");
	}

	private void build() {
		add("OC=(0,0)");
		add("c=Circle(OC,2)");
		add("E=Point(c)");
		add("t=1");
		add("QF=(t,0)");
		add("M=Midpoint(E,QF)");
		assertNotNull(add("L=LocusV2(M,E)"));
		add("k=Circle((0.5,0),3)");
		add("T=Point(k)");
		add("H=(4,0)");
		add("r=Ray(H,T)");
		GeoPoint driver = (GeoPoint) getKernel().lookupLabel("T");
		driver.setCoords(0.5 + 3 * Math.cos(3.0), 3 * Math.sin(3.0), 1.0);
		driver.updateCascade();
		GeoLocusIntersectionResult rich = add("v=Intersect(L,r)");
		assertNotNull(rich);
		List<LocusIntersectionSolution2D> roots =
				rich.getIntersectionResult().getFiniteSolutions();
		assertEquals(2, roots.size());
		assertNotNull(add("RA=Intersect(v,\"" + roots.get(0).getIdentity().getRootToken()
				+ "\")"));
		assertNotNull(add("RB=Intersect(v,\"" + roots.get(1).getIdentity().getRootToken()
				+ "\")"));
		add("ZP=Midpoint(RA,T)");
		assertNotNull(add("Z=LocusV2(ZP,T)"));
		getKernel().updateConstruction(false);
	}

	private static void moveNestedDriver(Kernel kernel, double angle) {
		GeoPoint driver = (GeoPoint) kernel.lookupLabel("E");
		GeoPoint centre = (GeoPoint) kernel.lookupLabel("OC");
		double x = centre == null ? 0 : centre.getInhomX();
		double y = centre == null ? 0 : centre.getInhomY();
		driver.setCoords(x + 2 * Math.cos(angle), y + 2 * Math.sin(angle), 1.0);
		driver.updateCascade();
	}

	private GeoLocusV2 locus(String label) {
		return (GeoLocusV2) getKernel().lookupLabel(label);
	}

	private static String fingerprint(Kernel kernel, String locus, String driver) {
		AlgoDependentPointLocusV2 algorithm = (AlgoDependentPointLocusV2) kernel
				.lookupLabel(locus).getParentAlgorithm();
		GeoElement root = kernel.lookupLabel(driver);
		return ReconstructionSliceFingerprint2D.compute(
				algorithm.getReconstructionSliceGeos(), Arrays.asList(root, root));
	}

	private static List<String> evaluations(GeoLocusV2 locus) {
		List<String> rows = new ArrayList<>();
		for (double parameter : PARAMETERS) {
			LocusEvaluation2D evaluation = locus.evaluate(BRANCH, parameter,
					LocusEvaluationSession2D.reference());
			rows.add(parameter + ":" + evaluation.getStatus() + (evaluation.isValid()
					? ":" + Double.toHexString(evaluation.getPoint().getX()) + ","
							+ Double.toHexString(evaluation.getPoint().getY())
					: ""));
		}
		return rows;
	}
}
