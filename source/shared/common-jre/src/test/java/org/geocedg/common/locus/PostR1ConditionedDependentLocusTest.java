/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCell2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCellKind2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidenceOwner2D.ReportOutcome;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.junit.jupiter.api.Test;

/**
 * Increment I4 of the conditioned use of Locus V2 (ADR 0042; contract {@code
 * post-r1-conditioned-use.md}, section 8.8): dependent and nested loci. A locus driven by
 * a point of a conditioned locus has the bands of its support; a locus whose traced point
 * reads a conditioned source records the content that it inherits, and is computed again
 * when that content changes.
 */
final class PostR1ConditionedDependentLocusTest extends G9U0PublicSurfaceTestBase {

	private GeoLocusV2 support(String ordinate) {
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(x(P)," + ordinate + ")");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		assertNotNull(locus);
		return locus;
	}

	/** A locus driven by a point of the support: its image translated by (0, 1). */
	private GeoLocusV2 driven() {
		add("u0=0.2");
		add("S=Point(L,\"" + BRANCH + "\",u0)");
		add("Y=S+(0,1)");
		GeoLocusV2 dependent = add("D=LocusV2(Y,S)");
		assertNotNull(dependent);
		return dependent;
	}

	private static int bands(LocusConditionedEvidence2D evidence) {
		int count = 0;
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			count += cell.getKind().isSpan() ? 0 : 1;
		}
		return count;
	}

	private static boolean covered(LocusConditionedEvidence2D evidence, double lower,
			double upper) {
		for (LocusInterval2D region : evidence.getRegions()) {
			if (region.getUpper() > lower && region.getLower() < upper) {
				return false;
			}
		}
		return true;
	}

	@Test
	void theBandsOfTheSupportAreBandsOfTheLocusThatItDrives() {
		GeoLocusV2 support = support("exp(x(P))+0*sqrt(abs(x(P)-0.5)-0.01)");
		GeoLocusV2 dependent = driven();
		LocusConditionedEvidence2D source = support.getConditionedEvidence(BRANCH);
		assertEquals(2, source.getRegions().size());
		// Its driver domain is the declared domain of the support, and its existence is not
		// established: it has no certificate of its own.
		assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED, dependent
				.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getCompleteness());
		LocusConditionedEvidence2D evidence = dependent.getConditionedEvidence(BRANCH);
		assertNotNull(evidence);
		assertEquals(source.getDomainLower(), evidence.getDomainLower(), 0);
		assertEquals(source.getDomainUpper(), evidence.getDomainUpper(), 0);
		// The regions of the support are the pieces on which the driver is defined.
		assertEquals(2, evidence.getRegions().size());
		for (int index = 0; index < 2; index++) {
			assertEquals(source.getRegions().get(index).getLower(),
					evidence.getRegions().get(index).getLower(), 1e-5);
			assertEquals(source.getRegions().get(index).getUpper(),
					evidence.getRegions().get(index).getUpper(), 1e-5);
		}
		assertTrue(covered(evidence, 0.491, 0.509));
		// The content records the content of the support that it inherits.
		assertEquals(1, evidence.getInheritedSources().size());
		assertTrue(evidence.getInheritedSources().get(0).endsWith(
				"@" + source.getContentIdentity()));
		assertTrue(evidence.getContentIdentity().contains(source.getContentIdentity()));
	}

	@Test
	void aBandThatOnlyAReportRevealsOnTheSupportBecomesABandOfTheDependent() {
		// The hole of half width 1e-4 at 0.3 is met by no node of either traversal.
		GeoLocusV2 support = support("exp(x(P))+0*sqrt((x(P)-0.3)^2-1E-8)");
		GeoLocusV2 dependent = driven();
		assertEquals(1, support.getConditionedEvidence(BRANCH).getRegions().size());
		LocusConditionedEvidence2D before = dependent.getConditionedEvidence(BRANCH);
		assertEquals(1, before.getRegions().size());
		assertEquals(0, bands(before));
		final long revision = dependent.getSemanticRevision();

		assertEquals(ReportOutcome.CONFIRMED,
				support.reportConditionedContradiction(BRANCH, 0.3));
		LocusConditionedEvidence2D source = support.getConditionedEvidence(BRANCH);
		assertEquals(2, source.getRegions().size());
		LocusConditionedEvidence2D after = dependent.getConditionedEvidence(BRANCH);
		// No result of the previous content is served: the content identity changed with
		// the content of the support, and the band of the support is a band here.
		assertNotEquals(before.getContentIdentity(), after.getContentIdentity());
		assertTrue(after.getInheritedSources().get(0).endsWith(
				"@" + source.getContentIdentity()));
		assertEquals(2, after.getRegions().size());
		assertTrue(covered(after, 0.2999, 0.3001));
		assertFalse(after.inOneRegion(0.2, 0.4));
		assertEquals(revision, dependent.getSemanticRevision());
	}

	@Test
	void aLocusThatReadsARootOfAConditionedSourceRecordsWhatItInherits() {
		// The traced point is built from the root of a conditioned locus with a moving
		// line: the nested locus computes its own evidence on its own function.
		final GeoLocusV2 source = support("exp(x(P))+0*sqrt((x(P)-0.3)^2-1E-8)");
		add("c=Segment((0,1.1),(0,2.5))");
		add("T=Point(c)");
		add("g=Line(T,T+(1,0))");
		GeoLocusIntersectionResult rich = add("v=Intersect(L,g)");
		List<LocusIntersectionSolution2D> roots =
				rich.getIntersectionResult().getFiniteSolutions();
		assertEquals(1, roots.size());
		add("R=Intersect(v,\"" + roots.get(0).getIdentity().getRootToken() + "\")");
		add("Z=R+(0,1)");
		GeoLocusV2 nested = add("N=LocusV2(Z,T)");
		assertNotNull(nested);
		getKernel().updateConstruction(false);
		LocusConditionedEvidence2D evidence = nested.getConditionedEvidence(BRANCH);
		assertNotNull(evidence);
		assertFalse(evidence.getRegions().isEmpty());
		assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED, nested
				.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getCompleteness());
		assertEquals(1, evidence.getInheritedSources().size());
		String content = source.getConditionedEvidence(BRANCH).getContentIdentity();
		assertTrue(evidence.getInheritedSources().get(0).endsWith("@" + content));

		// Another content of the source: the nested content is another content too.
		assertEquals(ReportOutcome.CONFIRMED,
				source.reportConditionedContradiction(BRANCH, 0.3));
		LocusConditionedEvidence2D after = nested.getConditionedEvidence(BRANCH);
		assertNotEquals(evidence.getContentIdentity(), after.getContentIdentity());
		assertTrue(after.getInheritedSources().get(0).endsWith("@"
				+ source.getConditionedEvidence(BRANCH).getContentIdentity()));
		// Where the root of the source would lie across the confirmed band from the origin
		// of its token, the token is withheld in the slice and the nested point is absent
		// or failed there: never a span.
		boolean band = false;
		for (LocusConditionedCell2D cell : after.getCells()) {
			band |= cell.getKind() != LocusConditionedCellKind2D.SPAN;
		}
		assertTrue(band);
	}
}
