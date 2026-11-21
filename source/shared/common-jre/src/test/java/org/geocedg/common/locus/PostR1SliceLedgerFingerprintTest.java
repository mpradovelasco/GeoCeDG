/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
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
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * Post-R1 repair A: the token ledger of a Locus V2 intersection that follows the driver of a
 * reconstruction slice enters the slice fingerprint without its evaluation state at the driver
 * position ({@code locus-v2-semantics.md}, PRE-G9B-CU1 amendment; {@code
 * locus-v2-intersections.md} section 6.1).
 *
 * <p>Fixture, independent of any author model: {@code L} is a closed locus, the circle of
 * centre {@code (0.5, 0)} and radius 1; the target {@code r} is a ray from a fixed point
 * outside it through the driver {@code T}; {@code v = Intersect(L, r)} has two roots while the
 * ray meets the circle and none otherwise; both roots are materialized by their exact tokens,
 * and {@code Z} traces a point built from one of them.</p>
 */
final class PostR1SliceLedgerFingerprintTest extends G9U0PublicSurfaceTestBase {
	private static final double[] PARAMETERS = {2.7, 2.85, 3.0, 3.1, -3.1, -2.9, -2.75, 1.0};

	@Test
	void movingOnlyTheDriverKeepsRevisionEvaluatorAndFunction() {
		build(true);
		GeoLocusV2 consumer = locus("Z");
		GeoLocusIntersectionResult rich = rich();
		moveDriver(3.0);
		long revision = consumer.getSemanticRevision();
		Object evaluator = consumer.getSemanticDefinition().getEvaluatorCapability();
		long builds = consumer.getInstrumentation().getDependencySliceBuilds();
		long structure = consumer.getInstrumentation().getSliceChanges(Change.STRUCTURE);
		long equivalent = consumer.getInstrumentation().getEquivalentRecomputes();
		List<String> reference = evaluations(consumer);
		String ledger = rich.getTokenLedgerState();
		String neutral = rich.getTokenLedgerEvaluationNeutralState();
		String fingerprint = fingerprint("Z", "T");

		int moved = 0;
		for (double angle : new double[] {2.95, 2.9, 3.05, 3.12, -3.1, -3.0, 3.0}) {
			moveDriver(angle);
			assertEquals(2, rich.getIntersectionResult().getFiniteSolutions().size(),
					"two current roots at " + angle);
			moved += rich.getTokenLedgerState().equals(ledger) ? 0 : 1;
			assertEquals(neutral, rich.getTokenLedgerEvaluationNeutralState(),
					"only localization evidence moved at " + angle);
			assertEquals(revision, consumer.getSemanticRevision(), "revision at " + angle);
			assertSame(evaluator, consumer.getSemanticDefinition().getEvaluatorCapability(),
					"evaluator at " + angle);
			assertEquals(fingerprint, fingerprint("Z", "T"), "fingerprint at " + angle);
		}
		// Premise of the regression: the persisted ledger did change with the driver.
		assertTrue(moved >= 5, "the canonical parameters of the roots follow the driver");
		assertEquals(builds, consumer.getInstrumentation().getDependencySliceBuilds());
		assertEquals(structure, consumer.getInstrumentation().getSliceChanges(Change.STRUCTURE));
		assertTrue(consumer.getInstrumentation().getEquivalentRecomputes() > equivalent);
		assertEquals(reference, evaluations(consumer), "same function, bit for bit");
	}

	@Test
	void leavingAndEnteringTheIncidenceDomainKeepsTheRevisionOfClaimedRoots() {
		build(true);
		GeoLocusV2 consumer = locus("Z");
		GeoLocusIntersectionResult rich = rich();
		moveDriver(3.0);
		long revision = consumer.getSemanticRevision();
		Object evaluator = consumer.getSemanticDefinition().getEvaluatorCapability();
		String ledger = rich.getTokenLedgerState();
		List<String> reference = evaluations(consumer);

		moveDriver(1.2);
		assertTrue(rich.getIntersectionResult() == null
				|| rich.getIntersectionResult().getFiniteSolutions().isEmpty(),
				"no current root outside the incidence domain");
		assertNotEquals(ledger, rich.getTokenLedgerState(), "claimed roots are dormant");
		assertFalse(point("RA").isDefined());
		assertEquals(revision, consumer.getSemanticRevision(), "dormant, same revision");
		assertEquals(reference, evaluations(consumer), "same function while dormant");

		moveDriver(3.0);
		assertEquals(ledger, rich.getTokenLedgerState(), "reactivated by the same bindings");
		assertTrue(point("RA").isDefined());
		assertEquals(revision, consumer.getSemanticRevision());
		assertSame(evaluator, consumer.getSemanticDefinition().getEvaluatorCapability());
		assertEquals(reference, evaluations(consumer));
	}

	@Test
	void aSliceBuiltWhileTheRootsAreDormantTracesTheSameFunction() {
		build(true);
		GeoLocusV2 consumer = locus("Z");
		moveDriver(3.0);
		List<String> reference = evaluations(consumer);
		long revision = consumer.getSemanticRevision();

		moveDriver(1.2);
		// A real non-driver change and its exact inverse rebuild the slice twice while
		// the claimed roots are dormant at the live driver position.
		GeoPoint origin = point("H");
		origin.setCoords(4.25, 0.0, 1.0);
		origin.updateCascade();
		assertTrue(consumer.getSemanticRevision() > revision, "a target input changed");
		assertNotEquals(reference, evaluations(consumer), "another function");
		long changed = consumer.getSemanticRevision();
		origin.setCoords(4.0, 0.0, 1.0);
		origin.updateCascade();
		assertTrue(consumer.getSemanticRevision() > changed, "and changed back");
		assertEquals(reference, evaluations(consumer),
				"built with dormant roots, evaluated as when they were current");
	}

	@Test
	void nonDriverInputsAndTokenLifecycleStillPublishRevisions() {
		build(false);
		GeoLocusV2 consumer = locus("Z");
		GeoLocusIntersectionResult rich = rich();
		moveDriver(3.0);

		// A non-driver input of the target: values part.
		long revision = consumer.getSemanticRevision();
		long values = consumer.getInstrumentation().getSliceChanges(Change.VALUES);
		GeoPoint origin = point("H");
		origin.setCoords(4.0, 0.125, 1.0);
		origin.updateCascade();
		assertTrue(consumer.getSemanticRevision() > revision, "non-driver input");
		assertEquals(values + 1, consumer.getInstrumentation().getSliceChanges(Change.VALUES));
		origin.setCoords(4.0, 0.0, 1.0);
		origin.updateCascade();

		// Only RA is materialized here: the other root is an unclaimed allocation, which
		// the ledger drops outside the incidence domain and allocates again on return.
		revision = consumer.getSemanticRevision();
		long structure = consumer.getInstrumentation().getSliceChanges(Change.STRUCTURE);
		String neutral = rich.getTokenLedgerEvaluationNeutralState();
		moveDriver(1.2);
		assertNotEquals(neutral, rich.getTokenLedgerEvaluationNeutralState(),
				"an allocation was dropped");
		assertTrue(consumer.getSemanticRevision() > revision, "token lifecycle change");
		assertTrue(consumer.getInstrumentation().getSliceChanges(Change.STRUCTURE) > structure);
		revision = consumer.getSemanticRevision();
		moveDriver(3.0);
		assertTrue(consumer.getSemanticRevision() > revision, "a new allocation");
	}

	@Test
	void aResultThatDoesNotFollowTheDriverKeepsItsCompleteLedger() {
		build(true);
		add("kw=Circle((0,0),1)");
		add("W=Point(kw)");
		add("YW=Midpoint(RA,W)");
		GeoLocusV2 other = add("Y=LocusV2(YW,W)");
		assertNotNull(other);
		moveDriver(3.0);
		long revision = other.getSemanticRevision();
		String fingerprint = fingerprint("Y", "W");
		// For Y, the result v is a non-driver input: its roots are part of the function.
		moveDriver(2.95);
		assertNotEquals(fingerprint, fingerprint("Y", "W"));
		assertTrue(other.getSemanticRevision() > revision);
	}

	@Test
	void reopeningKeepsTheFingerprintAndTheFunction() {
		build(true);
		moveDriver(2.95);
		GeoLocusV2 consumer = locus("Z");
		String fingerprint = fingerprint("Z", "T");
		List<String> reference = evaluations(consumer);
		String document = getApp().getXML();

		AppCommon reopened = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		reopened.setXML(document, true);
		Kernel kernel = reopened.getKernel();
		GeoLocusV2 loaded = (GeoLocusV2) kernel.lookupLabel("Z");
		assertNotNull(loaded);
		assertEquals(fingerprint, fingerprint(kernel, "Z", "T"));
		assertEquals(reference, evaluations(loaded));
		long revision = loaded.getSemanticRevision();
		GeoPoint driver = (GeoPoint) kernel.lookupLabel("T");
		driver.setCoords(0.5 + 3 * Math.cos(3.0), 3 * Math.sin(3.0), 1.0);
		driver.updateCascade();
		assertEquals(revision, loaded.getSemanticRevision(), "driver move after reopening");
		assertEquals(reference, evaluations(loaded));
	}

	private void build(boolean materializeBoth) {
		add("c=Circle((0,0),2)");
		add("E=Point(c)");
		add("QF=(1,0)");
		add("M=Midpoint(E,QF)");
		assertNotNull(add("L=LocusV2(M,E)"));
		add("k=Circle((0.5,0),3)");
		add("T=Point(k)");
		add("H=(4,0)");
		add("r=Ray(H,T)");
		moveDriver(3.0);
		GeoLocusIntersectionResult rich = add("v=Intersect(L,r)");
		assertNotNull(rich);
		List<LocusIntersectionSolution2D> roots =
				rich.getIntersectionResult().getFiniteSolutions();
		assertEquals(2, roots.size());
		assertNotNull(add("RA=Intersect(v,\"" + roots.get(0).getIdentity().getRootToken()
				+ "\")"));
		if (materializeBoth) {
			assertNotNull(add("RB=Intersect(v,\""
					+ roots.get(1).getIdentity().getRootToken() + "\")"));
		}
		add("ZP=Midpoint(RA,T)");
		assertNotNull(add("Z=LocusV2(ZP,T)"));
		getKernel().updateConstruction(false);
	}

	private void moveDriver(double angle) {
		GeoPoint driver = point("T");
		driver.setCoords(0.5 + 3 * Math.cos(angle), 3 * Math.sin(angle), 1.0);
		driver.updateCascade();
	}

	private GeoLocusV2 locus(String label) {
		return (GeoLocusV2) getKernel().lookupLabel(label);
	}

	private GeoPoint point(String label) {
		return (GeoPoint) getKernel().lookupLabel(label);
	}

	private GeoLocusIntersectionResult rich() {
		return (GeoLocusIntersectionResult) getKernel().lookupLabel("v");
	}

	private String fingerprint(String locus, String driver) {
		return fingerprint(getKernel(), locus, driver);
	}

	private static String fingerprint(Kernel kernel, String locus, String driver) {
		AlgoDependentPointLocusV2 algorithm = (AlgoDependentPointLocusV2) kernel
				.lookupLabel(locus).getParentAlgorithm();
		GeoElement root = kernel.lookupLabel(driver);
		return ReconstructionSliceFingerprint2D.compute(
				algorithm.getReconstructionSliceGeos(), Arrays.asList(root, root));
	}

	/** @return status and exact coordinate bits at fixed parameters */
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
