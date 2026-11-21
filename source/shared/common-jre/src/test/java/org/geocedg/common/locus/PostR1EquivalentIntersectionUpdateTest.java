/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * Post-R1 repair B: an update of a public Locus V2 intersection whose complete semantic inputs
 * are those of its current payload is not a recompute ({@code locus-v2-intersections.md}
 * section 10). The source {@code L} is a closed locus driven by {@code E}; the target is a
 * fixed line. Moving {@code E} recomputes {@code L} equivalently and the cascade reaches the
 * intersection, whose inputs did not change.
 */
final class PostR1EquivalentIntersectionUpdateTest extends G9U0PublicSurfaceTestBase {

	@Test
	void anEquivalentCascadeDoesNotResolveAgain() {
		build(getKernel());
		AlgoLocusIntersectionV2 producer = producer(getKernel());
		GeoLocusV2 source = (GeoLocusV2) getKernel().lookupLabel("L");
		long revision = source.getSemanticRevision();
		moveSourceDriver(getKernel(), 0.4);
		moveSourceDriver(getKernel(), 0.8);
		long resolutions = producer.getResolutionCount();
		long equivalent = producer.getEquivalentUpdateCount();
		LocusIntersectionResult2D payload = rich(getKernel()).getIntersectionResult();
		String state = state(getKernel());

		for (double angle : new double[] {1.2, 2.0, -2.5, -0.7, 0.1, 3.0}) {
			moveSourceDriver(getKernel(), angle);
			assertEquals(revision, source.getSemanticRevision(), "the source keeps its revision");
			assertSame(payload, rich(getKernel()).getIntersectionResult(),
					"the payload stays current at " + angle);
			assertEquals(state, state(getKernel()), "same roots, tokens and points at " + angle);
		}
		assertEquals(resolutions, producer.getResolutionCount(), "no resolution was started");
		assertEquals(equivalent + 6, producer.getEquivalentUpdateCount());
		assertTrue(rich(getKernel()).isDefined());
	}

	@Test
	void realChangesOfTheInputsResolveAgain() {
		build(getKernel());
		AlgoLocusIntersectionV2 producer = producer(getKernel());
		GeoLocusV2 source = (GeoLocusV2) getKernel().lookupLabel("L");
		settle(getKernel());
		long resolutions = producer.getResolutionCount();
		String state = state(getKernel());

		// One unit in the last place of an input of the target.
		GeoPoint through = (GeoPoint) getKernel().lookupLabel("B1");
		through.setCoords(3.0, Math.nextUp(0.4), 1.0);
		through.updateCascade();
		assertEquals(resolutions + 1, producer.getResolutionCount(), "target input");
		through.setCoords(3.0, 0.9, 1.0);
		through.updateCascade();
		assertEquals(resolutions + 2, producer.getResolutionCount());
		assertNotEquals(state, state(getKernel()), "other roots");

		// A non-driver input of the source publishes a revision of the source.
		long revision = source.getSemanticRevision();
		resolutions = producer.getResolutionCount();
		GeoPoint fixed = (GeoPoint) getKernel().lookupLabel("QF");
		fixed.setCoords(1.0, 0.25, 1.0);
		fixed.updateCascade();
		assertTrue(source.getSemanticRevision() > revision);
		assertTrue(producer.getResolutionCount() > resolutions, "source revision");

		// The new inputs are resolved, confirmed once and then kept.
		settle(getKernel());
		resolutions = producer.getResolutionCount();
		moveSourceDriver(getKernel(), 2.2);
		assertEquals(resolutions, producer.getResolutionCount());
	}

	@Test
	void aLedgerTransitionAndAFailureAreNeverReused() {
		build(getKernel());
		AlgoLocusIntersectionV2 producer = producer(getKernel());
		settle(getKernel());
		long resolutions = producer.getResolutionCount();

		// A new point claims the second root: the ledger changed outside a resolution.
		List<LocusIntersectionSolution2D> roots =
				rich(getKernel()).getIntersectionResult().getFiniteSolutions();
		assertNotNull(add("RB=Intersect(v,\"" + roots.get(1).getIdentity().getRootToken()
				+ "\")"));
		moveSourceDriver(getKernel(), 1.9);
		assertTrue(producer.getResolutionCount() > resolutions, "claim");
		assertTrue(((GeoPoint) getKernel().lookupLabel("RB")).isDefined());

		// An undefined target is a failure payload: every update resolves it again.
		settle(getKernel());
		GeoPoint through = (GeoPoint) getKernel().lookupLabel("B1");
		through.setUndefined();
		through.updateCascade();
		assertFalse(((GeoPoint) getKernel().lookupLabel("RA")).isDefined());
		assertNotEquals(ComputationStatus.SUCCESS,
				rich(getKernel()).getIntersectionResult().getComputationStatus());
		resolutions = producer.getResolutionCount();
		moveSourceDriver(getKernel(), 0.3);
		moveSourceDriver(getKernel(), 0.6);
		assertEquals(resolutions + 2, producer.getResolutionCount(), "a failure is not kept");

		through.setCoords(3.0, 0.4, 1.0);
		through.updateCascade();
		assertTrue(((GeoPoint) getKernel().lookupLabel("RA")).isDefined(), "recovered");
	}

	@Test
	void theSameTrajectoryWithAndWithoutTheReuseGivesTheSameStates() {
		build(getKernel());
		AppCommon reference = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		reference.setXML(getApp().getXML(), true);
		// The same document read back: both sides start from the same persisted state.
		getApp().setXML(reference.getXML(), true);
		producer(reference.getKernel()).setEquivalentUpdateReuse(false);
		long referenceResolutions = producer(reference.getKernel()).getResolutionCount();
		long resolutions = producer(getKernel()).getResolutionCount();

		List<String> expected = new ArrayList<>();
		List<String> actual = new ArrayList<>();
		for (Kernel kernel : new Kernel[] {reference.getKernel(), getKernel()}) {
			List<String> states = kernel == getKernel() ? actual : expected;
			for (double angle : new double[] {0.4, 0.8, 1.6, -2.0}) {
				moveSourceDriver(kernel, angle);
				states.add(state(kernel));
			}
			GeoPoint through = (GeoPoint) kernel.lookupLabel("B1");
			through.setCoords(3.0, 0.7, 1.0);
			through.updateCascade();
			states.add(state(kernel));
			for (double angle : new double[] {-1.0, 0.2, 2.9}) {
				moveSourceDriver(kernel, angle);
				states.add(state(kernel));
			}
			through.setCoords(3.0, 6.0, 1.0);
			through.updateCascade();
			states.add(state(kernel));
			moveSourceDriver(kernel, 1.1);
			states.add(state(kernel));
			through.setCoords(3.0, 0.4, 1.0);
			through.updateCascade();
			moveSourceDriver(kernel, 1.3);
			moveSourceDriver(kernel, 1.5);
			states.add(state(kernel));
		}
		assertEquals(expected, actual, "values, tokens, statuses, guarantees and currentness");
		assertEquals(reference.getXML(), getApp().getXML(), "the same persisted document");
		long saved = (producer(reference.getKernel()).getResolutionCount() - referenceResolutions)
				- (producer(getKernel()).getResolutionCount() - resolutions);
		assertTrue(saved >= 6, "resolutions saved on the same trajectory: " + saved);
		assertNotSame(reference.getKernel(), getKernel());
	}

	private void build(Kernel kernel) {
		add("c=Circle((0,0),2)");
		add("E=Point(c)");
		add("QF=(1,0)");
		add("M=Midpoint(E,QF)");
		assertNotNull(add("L=LocusV2(M,E)"));
		add("A1=(-2,0.3)");
		add("B1=(3,0.4)");
		add("l=Line(A1,B1)");
		GeoLocusIntersectionResult rich = add("v=Intersect(L,l)");
		assertNotNull(rich);
		List<LocusIntersectionSolution2D> roots =
				rich.getIntersectionResult().getFiniteSolutions();
		assertEquals(2, roots.size());
		assertNotNull(add("RA=Intersect(v,\"" + roots.get(0).getIdentity().getRootToken()
				+ "\")"));
		kernel.updateConstruction(false);
	}

	/** Two equivalent updates: the first confirms the resolution, the second keeps it. */
	private static void settle(Kernel kernel) {
		moveSourceDriver(kernel, 0.4);
		moveSourceDriver(kernel, 0.8);
		moveSourceDriver(kernel, 1.2);
	}

	private static void moveSourceDriver(Kernel kernel, double angle) {
		GeoPoint driver = (GeoPoint) kernel.lookupLabel("E");
		driver.setCoords(2 * Math.cos(angle), 2 * Math.sin(angle), 1.0);
		driver.updateCascade();
	}

	private static GeoLocusIntersectionResult rich(Kernel kernel) {
		return (GeoLocusIntersectionResult) kernel.lookupLabel("v");
	}

	private static AlgoLocusIntersectionV2 producer(Kernel kernel) {
		return (AlgoLocusIntersectionV2) rich(kernel).getParentAlgorithm();
	}

	/** Everything a consumer can read, without the update ordinal of the payload. */
	private static String state(Kernel kernel) {
		GeoLocusIntersectionResult rich = rich(kernel);
		StringBuilder text = new StringBuilder();
		text.append("defined=").append(rich.isDefined());
		LocusIntersectionResult2D result = rich.getIntersectionResult();
		if (result != null) {
			text.append('|').append(result.getComputationStatus()).append('|')
					.append(result.getGeometryKind()).append('|')
					.append(result.getCurrentness()).append('|')
					.append(result.getSupportLevel()).append('|')
					.append(result.getNumericGuarantee()).append('|')
					.append(result.getCompletenessEvidence().getCompleteness());
			for (LocusIntersectionSolution2D solution : result.getFiniteSolutions()) {
				text.append("|root=").append(solution.getIdentity().getRootToken()).append(':')
						.append(solution.getIdentity().getIdentityStatus()).append(':')
						.append(Double.toHexString(solution.getRevisionEvidence()
								.getSemanticParameter())).append(':')
						.append(Double.toHexString(solution.getEvaluatedPoint().getX()))
						.append(',')
						.append(Double.toHexString(solution.getEvaluatedPoint().getY()))
						.append(':').append(solution.getRevisionEvidence().getNumericGuarantee())
						.append(':').append(solution.getClassification().getContactClass());
			}
		}
		text.append("|ledger=").append(rich.getTokenLedgerState());
		for (String label : new String[] {"RA", "RB"}) {
			GeoElement point = kernel.lookupLabel(label);
			if (point instanceof GeoPoint) {
				text.append('|').append(label).append('=').append(point.isDefined());
				if (point.isDefined()) {
					text.append(':').append(Double.toHexString(((GeoPoint) point).getInhomX()))
							.append(',')
							.append(Double.toHexString(((GeoPoint) point).getInhomY()));
				}
			}
		}
		return text.toString();
	}
}
