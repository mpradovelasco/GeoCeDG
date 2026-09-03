/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Exact polynomial-model design examples, not public SplineV2 interpolation,
 * interval certificates, native persistence or productive token selectors.
 *
 * <p>Let q(t) = (1-3t^2/2+t^3/2, 3t/2-t^3/2). Concatenating its four
 * quarter-turn rotations gives a regular C2 curve b of period four. The source
 * F(u)=b(8u), u in R/Z, traverses it twice. The target is either the ray segment
 * G_r(v)=(1/2+v)r or the full segment G_r(v)=(4v-2)r, v in [0,1]. Only the
 * control vector r is current source data. The path coordinate s, with r=b(s),
 * is a diagnostic lift used to prove monodromy and is never serialized.
 *
 * <p>The identity det(q,q')=3/2+3t^2(1-t)^2/4 is strictly positive. Together
 * with quarter-quadrant monotonicity, it proves each ray has exactly one image
 * preimage per traversal for every s, not just at sampled test values. Exact
 * C2 glue makes ordinary knot/seam crossings regular. Numerical checks below
 * exercise that algebra; they are not a substitute interval proof. Earlier
 * u/v/dual projected-rank counterexamples remain in
 * {@link G9S1R1PairSelectorCharacterizationTest}.
 */
final class G9S1R1PairAtlasCharacterizationTest {

	private static final double EPSILON = 1E-12;

	@Test
	void cubicQuarterHasStrictlyPositiveAngularJacobian() {
		for (int step = 0; step <= 16; step++) {
			double t = step / 16.0;
			double expected = 1.5 + 0.75 * t * t * (1 - t) * (1 - t);
			assertEquals(expected, cross(quarter(t), quarterDerivative(t)), EPSILON);
			assertTrue(expected >= 1.5);
			assertTrue(quarter(t).x >= 0 && quarter(t).y >= 0);
			assertTrue(quarterDerivative(t).x <= 0);
			assertTrue(quarterDerivative(t).y >= 0);
		}
	}

	@Test
	void rayPairHasExactlyTwoRegularAnalyticPreimagesThroughoutLoop() {
		// Exhaustiveness follows from the angular identity in the class contract.
		for (int step = 0; step <= 64; step++) {
			double s = 0.25 + step / 16.0;
			List<Pair> roots = rayRoots(s);
			assertEquals(2, rootSet(roots).size());
			assertEquals(0.5, wrap(roots.get(1).u - roots.get(0).u, 1), 0);
			for (Pair root : roots) {
				assertVector(source(root.u), rayTarget(control(s), root.v));
				assertTrue(cross(sourceDerivative(root.u), control(s)) <= -12);
			}
		}
	}

	@Test
	void fullSegmentHasFourSeparatedRootsInTwoContactClasses() {
		for (int step = 0; step < 16; step++) {
			double s = 0.25 + step / 4.0;
			List<Pair> roots = lineRoots(s);
			assertEquals(4, rootSet(roots).size());
			int positive = 0;
			int negative = 0;
			for (int index = 0; index < roots.size(); index++) {
				Pair root = roots.get(index);
				assertVector(source(root.u), lineTarget(control(s), root.v));
				double determinant = cross(sourceDerivative(root.u), control(s).scale(4));
				assertTrue(Math.abs(determinant) >= 48);
				positive += determinant > 0 ? 1 : 0;
				negative += determinant < 0 ? 1 : 0;
				assertEquals(0.25,
						wrap(roots.get((index + 1) % 4).u - root.u, 1), 0);
			}
			assertEquals(2, positive);
			assertEquals(2, negative);
		}
	}

	@Test
	void oneControlCircuitPermutesBothRayPreimages() {
		double start = 0.25;
		List<Pair> initial = rayRoots(start);
		List<Pair> continued = rayRoots(start + 4);
		assertVector(control(start), control(start + 4));
		assertPair(initial.get(0), continued.get(1));
		assertPair(initial.get(1), continued.get(0));
		assertNotEquals(initial.get(0).u, continued.get(0).u);
		// The list positions here are explicitly chosen analytic lifts, not IDs.
	}

	@Test
	void reverseAndSecondCircuitsHaveTheExpectedPermutation() {
		double start = 0.25;
		List<Pair> initial = rayRoots(start);
		assertPair(initial.get(1), rayRoots(start - 4).get(0));
		assertPair(initial.get(0), rayRoots(start - 4).get(1));
		assertPair(initial.get(0), rayRoots(start + 8).get(0));
		assertPair(initial.get(1), rayRoots(start + 8).get(1));
		List<Pair> fullInitial = lineRoots(start);
		List<Pair> fullContinued = lineRoots(start + 4);
		for (int index = 0; index < fullInitial.size(); index++) {
			assertPair(fullInitial.get((index + 2) % 4), fullContinued.get(index));
		}
	}

	@Test
	void sourceControlClosesExactlyWithoutPersistingThePathCoordinate() {
		String initial = diagnosticControlSnapshot(control(0.25));
		String finalSnapshot = diagnosticControlSnapshot(control(4.25));
		assertEquals(initial, finalSnapshot);
		assertFalse(initial.contains("phase"));
		assertFalse(initial.contains("lift"));
		assertFalse(initial.contains("history"));
		assertNotEquals(rayRoots(0.25).get(0).u, rayRoots(4.25).get(0).u);
		// F's fixed dyadic coefficients and the target's current r data are equal.
		// No changing numeric s input is hidden in these model source definitions.
	}

	@Test
	void allEightSpanJoinsIncludingThePeriodicSeamAreExactlyC2() {
		for (int span = 0; span < 8; span++) {
			assertVectorExact(rotate(quarter(1), span), rotate(quarter(0), span + 1));
			assertVectorExact(rotate(quarterDerivative(1), span).scale(8),
					rotate(quarterDerivative(0), span + 1).scale(8));
			assertVectorExact(rotate(quarterSecondDerivative(1), span).scale(64),
					rotate(quarterSecondDerivative(0), span + 1).scale(64));
		}
		assertVectorExact(source(0), source(1));
		assertVectorExact(sourceDerivative(0), sourceDerivative(1));
	}

	@Test
	void ordinaryKnotAndSeamCrossingsDoNotCollideAnalyticPreimages() {
		for (double boundary : new double[] {0, 1, 2, 3, 4}) {
			List<Pair> before = rayRoots(boundary - 1.0 / 16);
			List<Pair> after = rayRoots(boundary + 1.0 / 16);
			for (int sheet = 0; sheet < 2; sheet++) {
				assertEquals(1.0 / 64, wrap(after.get(sheet).u - before.get(sheet).u, 1), 0);
				assertVector(source(after.get(sheet).u), control(boundary + 1.0 / 16));
			}
			assertNotEquals(after.get(0).u, after.get(1).u);
		}
		assertTrue(rayRoots(-1.0 / 16).get(0).u > 0.99);
		assertTrue(rayRoots(1.0 / 16).get(0).u < 0.01);
	}

	@Test
	void operandSwapTransposesPreimagesAndReversesContactSign() {
		double s = 0.25;
		Vector r = control(s);
		for (Pair root : rayRoots(s)) {
			Pair transposed = new Pair(root.v, root.u);
			assertVector(rayTarget(r, transposed.u), source(transposed.v));
			double forward = cross(sourceDerivative(root.u), r);
			double reverse = cross(r, sourceDerivative(transposed.v));
			assertEquals(-forward, reverse, 0);
			assertTrue(forward < 0 && reverse > 0);
		}
		// Canonical unordered source context can restore this sign convention;
		// swapping operands does not eliminate the two-sheet covering obstruction.
	}

	@Test
	void reversedCubicCounterexampleExchangesTheSecondProjectionRegularly() {
		// Swap the first review's pair: C1(a)=(a^2-1,a^3-a), C2_t(b)=(b,t).
		// At t=0 the outer roots have a=+/-1,b=0. Their second projection
		// velocities db/dt=a differ, while det D(C1-C2)=2 stays nonsingular.
		double[] firstPreimages = {-1, 1};
		double[] secondVelocities = new double[2];
		for (int index = 0; index < firstPreimages.length; index++) {
			double a = firstPreimages[index];
			double aVelocity = 1 / (3 * a * a - 1);
			double bVelocity = 2 * a * aVelocity;
			secondVelocities[index] = bVelocity;
			assertEquals(0, 2 * a * aVelocity - bVelocity, 0);
			assertEquals(1, (3 * a * a - 1) * aVelocity, 0);
			assertEquals(2, 3 * a * a - 1, 0);
		}
		// IFT plus unequal velocities at equal b proves the local crossing;
		// this is not a sampled approximation or a monodromy assertion.
		assertNotEquals(secondVelocities[0], secondVelocities[1]);
	}

	@Test
	void equalContactAndLocalDegreeDoNotDistinguishTheTwoSheets() {
		for (Pair root : rayRoots(0.25)) {
			double contact = cross(sourceDerivative(root.u), control(0.25));
			double pairJacobian = -contact;
			assertTrue(contact < 0);
			assertTrue(pairJacobian > 0);
		}
		assertNotEquals(rayRoots(0.25).get(0).u, rayRoots(4.25).get(0).u);
	}

	@Test
	void contractibleControlLoopHasNoAnalyticPermutation() {
		double[] path = {0.25, 0.5, 0.375, 0.625, 0.25};
		List<Pair> initial = rayRoots(path[0]);
		for (double s : path) {
			for (Pair root : rayRoots(s)) {
				assertVector(source(root.u), control(s));
			}
		}
		List<Pair> finalRoots = rayRoots(path[path.length - 1]);
		assertPair(initial.get(0), finalRoots.get(0));
		assertPair(initial.get(1), finalRoots.get(1));
	}

	@Test
	void uniqueCommonRootWitnessPermitsAnOrdinaryChartChange() {
		List<Pair> roots = rayRoots(1);
		Box first = new Box(0.10, 0.20, 0.40, 0.60);
		Box second = new Box(0.11, 0.16, 0.45, 0.55);
		assertEquals(1, countInside(first, roots));
		assertEquals(1, countInside(second, roots));
		assertEquals(1, countInside(first.intersection(second), roots));
		assertTrue(first.contains(roots.get(0)) && second.contains(roots.get(0)));
		// A production transition needs a certified common-root witness. This
		// analytic example has one; changing its enclosing chart is not identity loss.
	}

	@Test
	void overlappingBoxesAloneDoNotEstablishTheSameRoot() {
		List<Pair> roots = rayRoots(1);
		Box first = new Box(0, 0.45, 0.40, 0.60);
		Box second = new Box(0.40, 0.75, 0.40, 0.60);
		assertEquals(1, countInside(first, roots));
		assertEquals(1, countInside(second, roots));
		Box overlap = first.intersection(second);
		assertTrue(overlap.lowerU < overlap.upperU);
		assertEquals(0, countInside(overlap, roots));
		assertTrue(first.contains(roots.get(0)));
		assertTrue(second.contains(roots.get(1)));
	}

	@Test
	void currentRootSetIsPathIndependentButAFullLiftHasMonodromy() {
		double start = 0.25;
		double finish = 0.75;
		List<Pair> direct = rayRoots(finish);
		List<Pair> incremental = rayRoots(start);
		for (int step = 1; step <= 8; step++) {
			incremental = rayRoots(start + step / 16.0);
		}
		assertPair(direct.get(0), incremental.get(0));
		assertPair(direct.get(1), incremental.get(1));
		assertEquals(rootSet(rayRoots(start)), rootSet(rayRoots(start + 4)));
		assertEquals(diagnosticControlSnapshot(control(start)),
				diagnosticControlSnapshot(control(start + 4)));
		assertNotEquals(rayRoots(start).get(0).u, rayRoots(start + 4).get(0).u);
		// Thus a single-valued snapshot selector cannot also follow this lifted
		// branch around every loop. No production cut or token policy is chosen here.
	}

	@Test
	void diagnosticSnapshotRoundTripContainsOnlyCurrentControlData() {
		String snapshot = diagnosticControlSnapshot(control(0.25));
		String[] fields = snapshot.split(";");
		assertEquals(3, fields.length);
		assertEquals("fixed-double-cubic/ray-control-v1", fields[0]);
		Vector restored = new Vector(Double.parseDouble(fields[1]),
				Double.parseDouble(fields[2]));
		assertEquals(snapshot, diagnosticControlSnapshot(restored));
		assertEquals(snapshot, diagnosticControlSnapshot(control(4.25)));
		// Test-local text only: this is NOT a native .cedg or ledger round-trip.
	}

	@Test
	void canonicalNumericTraceIgnoresDiagnosticRootEnumeration()
			throws NoSuchAlgorithmException {
		String forward = canonicalNumericTrace(false);
		String reverse = canonicalNumericTrace(true);
		assertEquals(forward, reverse);
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		String forwardHash = HexFormat.of().formatHex(
				sha256.digest(forward.getBytes(StandardCharsets.UTF_8)));
		String reverseHash = HexFormat.of().formatHex(
				sha256.digest(reverse.getBytes(StandardCharsets.UTF_8)));
		assertEquals(forwardHash, reverseHash);
		System.out.println("G9S1_R1_ATLAS_TRACE_SHA256=" + forwardHash);
	}

	@Test
	void tangencyAndRootMergeAreRealFailuresRatherThanChartEvents() {
		double t = 0.25;
		double x = quarter(t).x;
		for (double phase : new double[] {t, 4 - t, 4 + t, 8 - t}) {
			double u = phase / 8;
			assertEquals(x, source(u).x, 0);
			assertNotEquals(0, cross(sourceDerivative(u), new Vector(0, 1)));
		}
		for (double u : new double[] {0, 0.5}) {
			assertEquals(1, source(u).x, 0);
			assertEquals(0, cross(sourceDerivative(u), new Vector(0, 1)), 0);
		}
		// As x rises to 1, each upper/lower pair merges at a genuine tangent.
		// For x>1 there is no root: each quarter lies in [-1,1]^2.
		assertTrue(x < 1);
		for (int quarterIndex = 0; quarterIndex < 4; quarterIndex++) {
			assertTrue(Math.abs(rotate(quarter(t), quarterIndex).x) <= 1);
		}
	}

	private static Vector quarter(double t) {
		return new Vector(1 - 1.5 * t * t + 0.5 * t * t * t,
				1.5 * t - 0.5 * t * t * t);
	}

	private static Vector quarterDerivative(double t) {
		return new Vector(-3 * t + 1.5 * t * t, 1.5 - 1.5 * t * t);
	}

	private static Vector quarterSecondDerivative(double t) {
		return new Vector(-3 + 3 * t, -3 * t);
	}

	private static Vector rotate(Vector vector, int quarter) {
		switch (Math.floorMod(quarter, 4)) {
		case 0:
			return vector;
		case 1:
			return new Vector(-vector.y, vector.x);
		case 2:
			return new Vector(-vector.x, -vector.y);
		default:
			return new Vector(vector.y, -vector.x);
		}
	}

	private static Vector control(double s) {
		double phase = wrap(s, 4);
		int span = (int) Math.floor(phase);
		return rotate(quarter(phase - span), span);
	}

	private static Vector source(double u) {
		return control(8 * u);
	}

	private static Vector sourceDerivative(double u) {
		double phase = wrap(8 * u, 4);
		int span = (int) Math.floor(phase);
		return rotate(quarterDerivative(phase - span), span).scale(8);
	}

	private static Vector rayTarget(Vector control, double v) {
		return control.scale(0.5 + v);
	}

	private static Vector lineTarget(Vector control, double v) {
		return control.scale(4 * v - 2);
	}

	private static List<Pair> rayRoots(double diagnosticLift) {
		return List.of(new Pair(wrap(diagnosticLift / 8, 1), 0.5),
				new Pair(wrap((diagnosticLift + 4) / 8, 1), 0.5));
	}

	private static List<Pair> lineRoots(double diagnosticLift) {
		return List.of(new Pair(wrap(diagnosticLift / 8, 1), 0.75),
				new Pair(wrap((diagnosticLift + 2) / 8, 1), 0.25),
				new Pair(wrap((diagnosticLift + 4) / 8, 1), 0.75),
				new Pair(wrap((diagnosticLift + 6) / 8, 1), 0.25));
	}

	private static double wrap(double value, double period) {
		double canonical = value - period * Math.floor(value / period);
		return canonical == 0 ? 0 : canonical;
	}

	private static double cross(Vector first, Vector second) {
		return first.x * second.y - first.y * second.x;
	}

	private static Set<List<Double>> rootSet(List<Pair> roots) {
		Set<List<Double>> values = new HashSet<>();
		for (Pair root : roots) {
			values.add(List.of(root.u, root.v));
		}
		return values;
	}

	private static int countInside(Box box, List<Pair> roots) {
		int count = 0;
		for (Pair root : roots) {
			count += box.contains(root) ? 1 : 0;
		}
		return count;
	}

	private static String diagnosticControlSnapshot(Vector control) {
		return "fixed-double-cubic/ray-control-v1;" + Double.toHexString(control.x)
				+ ";" + Double.toHexString(control.y);
	}

	private static String canonicalNumericTrace(boolean reverseEnumeration) {
		StringBuilder trace = new StringBuilder("pair-atlas-diagnostic-trace/v1\n");
		for (int step = 0; step <= 64; step++) {
			double s = 0.25 + step / 16.0;
			Vector r = control(s);
			trace.append("snapshot=").append(step).append("|rx=").append(bits(r.x))
					.append("|ry=").append(bits(r.y)).append('\n');
			List<Pair> roots = new ArrayList<>(rayRoots(s));
			if (reverseEnumeration) {
				Collections.reverse(roots);
			}
			List<String> records = new ArrayList<>();
			for (Pair root : roots) {
				double jacobian = -cross(sourceDerivative(root.u), r);
				records.add("u=" + bits(root.u) + "|v=" + bits(root.v)
						+ "|jacobian=" + bits(jacobian));
			}
			// Sorting normalizes evidence encoding ONLY. These records are neither
			// selectors nor tokens, and their ordinal is never identity authority.
			Collections.sort(records);
			for (String record : records) {
				trace.append(record).append('\n');
			}
		}
		return trace.toString();
	}

	private static String bits(double value) {
		return Long.toHexString(Double.doubleToRawLongBits(value));
	}

	private static void assertVector(Vector expected, Vector actual) {
		assertEquals(expected.x, actual.x, EPSILON);
		assertEquals(expected.y, actual.y, EPSILON);
	}

	private static void assertVectorExact(Vector expected, Vector actual) {
		assertEquals(expected.x, actual.x, 0);
		assertEquals(expected.y, actual.y, 0);
	}

	private static void assertPair(Pair expected, Pair actual) {
		assertEquals(expected.u, actual.u, 0);
		assertEquals(expected.v, actual.v, 0);
	}

	private static final class Vector {
		private final double x;
		private final double y;

		private Vector(double x, double y) {
			this.x = x;
			this.y = y;
		}

		private Vector scale(double factor) {
			return new Vector(factor * x, factor * y);
		}
	}

	private static final class Pair {
		private final double u;
		private final double v;

		private Pair(double u, double v) {
			this.u = u;
			this.v = v;
		}
	}

	private static final class Box {
		private final double lowerU;
		private final double upperU;
		private final double lowerV;
		private final double upperV;

		private Box(double lowerU, double upperU, double lowerV, double upperV) {
			this.lowerU = lowerU;
			this.upperU = upperU;
			this.lowerV = lowerV;
			this.upperV = upperV;
		}

		private boolean contains(Pair root) {
			return lowerU < root.u && root.u < upperU
					&& lowerV < root.v && root.v < upperV;
		}

		private Box intersection(Box other) {
			return new Box(Math.max(lowerU, other.lowerU), Math.min(upperU, other.upperU),
					Math.max(lowerV, other.lowerV), Math.min(upperV, other.upperV));
		}
	}
}
