/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * Step C1 of the corrections after block 3B (author authorization of 2026-11-22): the
 * revision-local key of a pair of places names the source of each side. Two sources that
 * share a branch key and have two components each used to give the pairs (first i, second
 * j) and (first j, second i) one key, and the whole intersection failed.
 */
final class PostR1PairPlaceKeyTest extends G9U0PublicSurfaceTestBase {
	private static final String TWO = "{false,{-2,-0.5,true,true},{0.5,2,true,true}}";

	private GeoLocusV2 locus(String label, String coordinate, String point, String expression) {
		add(coordinate + "=0");
		add(point + "=" + expression);
		add("D" + label + "=" + TWO);
		GeoLocusV2 locus = add(label + "=LocusV2(" + point + "," + coordinate + ",D" + label + ")");
		assertNotNull(locus);
		assertEquals(2, locus.getSemanticDefinition().getBranch(BRANCH)
				.getCertifiedContinuousValidComponents().size(), label);
		return locus;
	}

	private LocusIntersectionResult2D searched(String label, String command, double x,
			double y) {
		GeoLocusIntersectionResult rich = add(label + "=" + command);
		LocusIntersectionResult2D result = rich.getIntersectionResult();
		assertEquals(ComputationStatus.SUCCESS, result.getComputationStatus(),
				result.getDiagnostics().toString());
		List<String> covered = result.getCompletenessEvidence().getCoveredComponentKeys();
		assertEquals(4, covered.size(), covered.toString());
		assertEquals(4, new HashSet<>(covered).size(), "four distinct pairs of places");
		List<LocusIntersectionSolution2D> roots = result.getFiniteSolutions();
		assertEquals(1, roots.size(), result.getDiagnostics().toString());
		assertEquals(x, roots.get(0).getEvaluatedPoint().getX(), 1e-9);
		assertEquals(y, roots.get(0).getEvaluatedPoint().getY(), 1e-9);
		String token = roots.get(0).getIdentity().getRootToken();
		assertTrue(rich.isPointAdmissible(token), result.getDiagnostics().toString());
		GeoPoint point = add("X" + label + "=Intersect(" + label + ",\"" + token + "\")");
		assertTrue(point.isDefined());
		assertEquals(x, point.getInhomX(), 1e-9);
		assertEquals(y, point.getInhomY(), 1e-9);
		assertTrue(rich.getTokenLedgerState().contains("~P,"), rich.getTokenLedgerState());
		return result;
	}

	@Test
	void twoSourcesOfTwoComponentsThatShareTheirBranchKeyAreSearched() {
		locus("LA", "m", "QA", "(m,1)");
		locus("LB", "n", "QB", "(n,n)");
		// The root lies on the second component of each source.
		LocusIntersectionResult2D forward = searched("RAB", "Intersect(LA,LB)", 1, 1);
		LocusIntersectionResult2D backward = searched("RBA", "Intersect(LB,LA)", 1, 1);
		// The key of a pair of places does not depend on the order of the caller.
		assertEquals(new HashSet<>(forward.getCompletenessEvidence()
				.getCoveredComponentKeys()), new HashSet<>(backward.getCompletenessEvidence()
						.getCoveredComponentKeys()));
	}

	@Test
	void aRootOnComponentsOfDifferentOrdinalsIsFoundOnEitherMirroredPair() {
		locus("LA", "m", "QA", "(m,1)");
		// (first component of LA, second component of LB1): the pair that collided with
		// (second component of LA, first component of LB1).
		locus("LB1", "n", "QB1", "(-1,n)");
		searched("R1", "Intersect(LA,LB1)", -1, 1);
		// And the mirrored pair: (second component of LC, first component of LB2).
		locus("LC", "j", "QC", "(j,-1)");
		locus("LB2", "k", "QB2", "(1,k)");
		searched("R2", "Intersect(LC,LB2)", 1, -1);
	}
}
