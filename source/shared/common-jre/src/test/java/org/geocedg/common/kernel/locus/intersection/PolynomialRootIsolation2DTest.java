/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.locus.intersection.PolynomialRootIsolation2D.IsolationResult;
import org.geocedg.common.kernel.locus.intersection.PolynomialRootIsolation2D.RootCell;
import org.junit.jupiter.api.Test;

/** Contract tests for shared deterministic polynomial root isolation. */
final class PolynomialRootIsolation2DTest {
	private static final double TOLERANCE = 1E-12;

	@Test
	void derivativePartitionFindsSameClassRootsIndependentlyOfSignAtEnds() {
		WorkCounter work = new WorkCounter();
		IsolationResult result = PolynomialRootIsolation2D.isolate(
				new double[] {0.1875, -1, 1}, 0, 1, TOLERANCE, 80,
				work);

		assertFalse(result.isZeroPolynomial());
		assertEquals(2, result.getCells().size());
		assertEquals(0.25, result.getCells().get(0).getParameter(), TOLERANCE);
		assertEquals(0.75, result.getCells().get(1).getParameter(), TOLERANCE);
		assertTrue(work.isolationSubdivisions > 0);
		assertTrue(work.refinementsStarted > 0);
		assertTrue(work.refinementIterations > 0);
		assertThrows(UnsupportedOperationException.class,
				() -> result.getCells().add(result.getCells().get(0)));
	}

	@Test
	void repeatedRootAndZeroPolynomialRemainExplicit() {
		IsolationResult repeated = PolynomialRootIsolation2D.isolate(
				new double[] {0.25, -1, 1}, 0, 1, TOLERANCE, 80,
				new WorkCounter());
		IsolationResult zero = PolynomialRootIsolation2D.isolate(
				new double[] {0, 0, 0}, 0, 1, TOLERANCE, 80,
				new WorkCounter());

		assertEquals(1, repeated.getCells().size());
		RootCell cell = repeated.getCells().get(0);
		assertEquals(0.5, cell.getParameter(), TOLERANCE);
		assertTrue(cell.getLower() <= cell.getParameter());
		assertTrue(cell.getUpper() >= cell.getParameter());
		assertTrue(zero.isZeroPolynomial());
		assertTrue(zero.getCells().isEmpty());
	}

	@Test
	void insufficientRefinementBudgetFailsClosed() {
		assertThrows(LocusIntersectionWorkLimitException.class,
				() -> PolynomialRootIsolation2D.isolate(
						new double[] {-0.3, 0, 1}, 0, 1, TOLERANCE, 0,
						new WorkCounter()));
	}

	private static final class WorkCounter
			implements PolynomialRootIsolation2D.WorkRecorder {
		private long isolationSubdivisions;
		private long refinementsStarted;
		private long refinementIterations;

		@Override
		public void recordIsolationSubdivision(int depth) {
			isolationSubdivisions++;
		}

		@Override
		public void recordRefinementStarted() {
			refinementsStarted++;
		}

		@Override
		public void recordRefinementIteration(long iteration) {
			refinementIterations++;
		}
	}
}
