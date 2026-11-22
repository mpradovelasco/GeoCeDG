/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedAbsenceCause2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCell2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCellKind2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidenceOwner2D.ReportOutcome;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidenceUse2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedSample2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedSpanClass2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedTraversalPolicy2D;
import org.geocedg.common.kernel.locus.conditioned.LocusSelectionTestimony2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * Increment I0 of the conditioned use of Locus V2 (ADR 0042; contract {@code
 * post-r1-conditioned-use.md}, sections 3, 5 and 7) on the real producer: the testimony
 * that the reconstructible evaluator reads from its slice, and the conditioned evidence that
 * the producer of a Locus V2 owns per revision. No public operation reads the evidence in
 * this increment.
 */
final class PostR1ConditionedProducerTest extends G9U0PublicSurfaceTestBase {

	/** An operation outside the certified class: faithful slice, no certified component. */
	private GeoLocusV2 outsideClass(String ordinate) {
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(x(P)," + ordinate + ")");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		assertNotNull(locus);
		assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED, locus
				.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getCompleteness());
		assertTrue(locus.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getContinuousValidComponents().isEmpty());
		return locus;
	}

	private static ReconstructibleLocusEvaluator2D evaluator(GeoLocusV2 locus) {
		return (ReconstructibleLocusEvaluator2D) locus.getSemanticDefinition()
				.getEvaluatorCapability();
	}

	private static LocusConditionedSample2D sample(GeoLocusV2 locus, double parameter) {
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.reference()) {
			return evaluator(locus).evaluateWithTestimony(parameter, session);
		}
	}

	private static int count(LocusConditionedEvidence2D evidence,
			LocusConditionedCellKind2D kind) {
		int count = 0;
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			count += cell.getKind() == kind ? 1 : 0;
		}
		return count;
	}

	@Test
	void anUncertifiedFaithfulLocusPublishesOneRegionThatNeverCertifies() {
		GeoLocusV2 locus = outsideClass("exp(x(P))");
		LocusConditionedEvidence2D evidence = locus.getConditionedEvidence(BRANCH);
		assertNotNull(evidence);
		assertEquals(LocusEvidenceNature2D.NUMERICAL_UNDER_HYPOTHESES, evidence.getNature());
		assertFalse(evidence.getNature().certifies());
		assertEquals(locus.getSemanticRevision(), evidence.getRevision());
		assertEquals(LocusConditionedTraversalPolicy2D.VERSION_1, evidence.getPolicyVersion());
		assertEquals(evidence.getCells().size(),
				count(evidence, LocusConditionedCellKind2D.SPAN));
		List<LocusInterval2D> regions = evidence.getRegions();
		assertEquals(1, regions.size());
		assertEquals(0.0, regions.get(0).getLower());
		assertEquals(1.0, regions.get(0).getUpper());
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			// No selecting dependency: nothing testifies a margin.
			assertEquals(LocusConditionedSpanClass2D.RESOLUTION_ONLY, cell.getSpanClass());
			assertTrue(cell.getChordDeviation() <= 1e-3);
		}
		// The definition and its existence structure are unchanged: nothing is certified.
		assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED, locus
				.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getCompleteness());
		assertTrue(locus.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getContinuousValidComponents().isEmpty());
		// Once per revision: the same immutable content is served again.
		assertSame(evidence, locus.getConditionedEvidence(BRANCH));
		assertEquals(1, locus.getConditionedPublicationCounter(BRANCH));
		assertNull(locus.getConditionedEvidence("another-branch"));
	}

	@Test
	void aCertifiedLocusPublishesNoConditionedEvidence() {
		GeoLocusV2 locus = createLine();
		assertEquals(LocusExistenceStructure2D.Completeness.COMPLETE, locus
				.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getCompleteness());
		assertNull(locus.getConditionedEvidence(BRANCH));
		assertEquals(0, locus.getConditionedPublicationCounter(BRANCH));
		assertNull(locus.reportConditionedContradiction(BRANCH, 0.0));
	}

	@Test
	void anUndefinedDependencyIsAConditionedAbsenceAndItsEdgeATransitionBand() {
		GeoLocusV2 locus = outsideClass("sqrt(x(P)-0.3)+exp(x(P))");
		LocusConditionedSample2D absent = sample(locus, 0.1);
		assertEquals(LocusConditionedSample2D.Outcome.ABSENT, absent.getOutcome());
		assertEquals(LocusConditionedAbsenceCause2D.DEPENDENCY_UNDEFINED, absent.getCause());
		assertTrue(LocusConditionedAbsenceCause2D.isEligible(absent.getCause()));
		assertTrue(sample(locus, 0.6).isValid());

		LocusConditionedEvidence2D evidence = locus.getConditionedEvidence(BRANCH);
		assertNotNull(evidence);
		assertTrue(count(evidence, LocusConditionedCellKind2D.ABSENT_BAND) > 0);
		assertEquals(1, count(evidence, LocusConditionedCellKind2D.TRANSITION_BAND));
		assertEquals(0, count(evidence, LocusConditionedCellKind2D.FAILURE_BAND));
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			if (cell.getKind() == LocusConditionedCellKind2D.TRANSITION_BAND) {
				// Localized to the minimum step of the policy.
				assertTrue(cell.getLower() <= 0.3 && 0.3 <= cell.getUpper());
				assertTrue(cell.getUpper() - cell.getLower() <= Math.pow(2, -20) * 1.0000001);
			} else if (cell.getKind() == LocusConditionedCellKind2D.ABSENT_BAND) {
				assertTrue(cell.getUpper() <= 0.3 + 1e-6);
				assertEquals(LocusConditionedAbsenceCause2D.DEPENDENCY_UNDEFINED,
						cell.getCause());
			}
		}
		// One region, to the right of the edge; the absence is not a certified nonexistence.
		assertEquals(1, evidence.getRegions().size());
		assertTrue(evidence.getRegions().get(0).getLower() > 0.3);
		assertFalse(evidence.inOneRegion(0.2, 0.6));
		assertTrue(evidence.inOneRegion(0.5, 0.9));
	}

	@Test
	void aNewRevisionDiscardsTheEvidenceOfThePreviousOne() {
		add("k=1");
		GeoLocusV2 locus = outsideClass("exp(k x(P))");
		LocusConditionedEvidence2D first = locus.getConditionedEvidence(BRANCH);
		assertNotNull(first);
		long revision = locus.getSemanticRevision();

		// A driver move publishes no revision and keeps the evidence.
		GeoPoint driver = (GeoPoint) lookup("P");
		driver.setCoords(0.7, 0, 1);
		driver.updateCascade();
		assertEquals(revision, locus.getSemanticRevision());
		assertSame(first, locus.getConditionedEvidence(BRANCH));

		GeoNumeric factor = (GeoNumeric) lookup("k");
		factor.setValue(2);
		factor.updateCascade();
		assertTrue(locus.getSemanticRevision() > revision);
		LocusConditionedEvidence2D second = locus.getConditionedEvidence(BRANCH);
		assertNotNull(second);
		assertEquals(locus.getSemanticRevision(), second.getRevision());
		assertNotEquals(first.getContentIdentity(), second.getContentIdentity());
		assertEquals(1, locus.getConditionedPublicationCounter(BRANCH));
		assertTrue(second.getContradictions().isEmpty());
	}

	@Test
	void aReportIsConfirmedOnlyByTheProducersOwnEvaluation() {
		// A hole of width 2e-4 between the nodes of a straight span.
		GeoLocusV2 locus = outsideClass(
				"x(P)+0*sqrt((x(P)-0.3)^2-1E-8)+0*exp(x(P))");
		LocusConditionedEvidence2D before = locus.getConditionedEvidence(BRANCH);
		assertNotNull(before);
		assertEquals(before.getCells().size(),
				count(before, LocusConditionedCellKind2D.SPAN));
		assertTrue(before.inOneRegion(0.1, 0.9));

		// A report at a parameter where the producer evaluates valid changes nothing.
		assertEquals(ReportOutcome.NOT_CONFIRMED,
				locus.reportConditionedContradiction(BRANCH, 0.5));
		assertSame(before, locus.getConditionedEvidence(BRANCH));
		assertEquals(1, locus.getConditionedPublicationCounter(BRANCH));
		// Outside the declared domain nothing is evaluated.
		assertEquals(ReportOutcome.NOT_CONFIRMED,
				locus.reportConditionedContradiction(BRANCH, 7.0));

		assertEquals(ReportOutcome.CONFIRMED,
				locus.reportConditionedContradiction(BRANCH, 0.3));
		LocusConditionedEvidence2D after = locus.getConditionedEvidence(BRANCH);
		assertEquals(2, locus.getConditionedPublicationCounter(BRANCH));
		assertEquals(before.getRevision(), after.getRevision());
		assertNotEquals(before.getContentIdentity(), after.getContentIdentity());
		assertEquals(List.of(0.3), after.getContradictions());
		assertFalse(after.inOneRegion(0.1, 0.9));
		assertFalse(before.withdrawnBy(after).isEmpty());
		// A result that used a withdrawn cell is no longer current; one that used only
		// cells that remain keeps its result (contract section 7, points 5 and 6).
		assertFalse(new LocusConditionedEvidenceUse2D(before,
				before.cellsBetween(0.1, 0.9)).isCurrent(after));
		assertTrue(new LocusConditionedEvidenceUse2D(before,
				before.cellsBetween(0.6, 0.9)).isCurrent(after));
		assertEquals(ReportOutcome.ALREADY_KNOWN,
				locus.reportConditionedContradiction(BRANCH, 0.3));
		assertEquals(2, locus.getConditionedPublicationCounter(BRANCH));
		// The revision of the definition did not change: evidence is not definition.
		assertEquals(before.getRevision(), locus.getSemanticRevision());
	}

	@Test
	void theEvaluatorTestifiesTheTokenOfTheRootThatItsSliceSelects() {
		add("c=Circle((0,0),2)");
		add("E=Point(c)");
		add("QF=(1,0)");
		add("M=Midpoint(E,QF)");
		assertNotNull(add("L=LocusV2(M,E)"));
		add("k=Circle((0.5,0),3)");
		add("T=Point(k)");
		add("H=(4,0)");
		add("r=Ray(H,T)");
		GeoPoint driver = (GeoPoint) lookup("T");
		driver.setCoords(0.5 + 3 * Math.cos(3.0), 3 * Math.sin(3.0), 1.0);
		driver.updateCascade();
		GeoLocusIntersectionResult rich = add("v=Intersect(L,r)");
		List<LocusIntersectionSolution2D> roots =
				rich.getIntersectionResult().getFiniteSolutions();
		assertEquals(2, roots.size());
		String token = roots.get(0).getIdentity().getRootToken();
		assertNotNull(add("RA=Intersect(v,\"" + token + "\")"));
		add("ZP=Midpoint(RA,T)");
		GeoLocusV2 consumer = add("Z=LocusV2(ZP,T)");
		assertNotNull(consumer);
		getKernel().updateConstruction(false);

		double parameter = ((GeoPoint) lookup("T")).getPathParameter().getT();
		LocusConditionedSample2D here = sample(consumer, parameter);
		assertTrue(here.isValid());
		assertEquals(1, here.getTestimony().size());
		LocusSelectionTestimony2D testimony = here.getTestimony().get(0);
		assertEquals(LocusSelectionTestimony2D.Kind.LOCUS_ROOT_TOKEN, testimony.getKind());
		assertTrue(testimony.isEstablished());
		// The exact token, never an index, and the distance to the other root of the result.
		assertEquals(token, testimony.getSelectionKey());
		assertTrue(testimony.hasSeparation());
		assertTrue(testimony.getSeparation() > 1);
		assertEquals(here.getTestimony().get(0).getOperationIdentity(),
				sample(consumer, -3.0).getTestimony().get(0).getOperationIdentity());

		// Where the ray misses the source the result of the slice is current and publishes
		// no root for the token: a conditioned absence with its eligible cause.
		LocusConditionedSample2D missed = sample(consumer, 1.5);
		assertEquals(LocusConditionedSample2D.Outcome.ABSENT, missed.getOutcome());
		assertEquals(LocusConditionedAbsenceCause2D.INTERSECTION_ROOT_ABSENT,
				missed.getCause());

		assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED, consumer
				.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getCompleteness());
		LocusConditionedEvidence2D evidence = consumer.getConditionedEvidence(BRANCH);
		assertNotNull(evidence);
		assertEquals(3, evidence.getRegions().size());
		assertEquals(4, count(evidence, LocusConditionedCellKind2D.TRANSITION_BAND));
		assertEquals(0, count(evidence, LocusConditionedCellKind2D.FAILURE_BAND));
		assertEquals(0, count(evidence, LocusConditionedCellKind2D.SELECTION_BAND));
		assertTrue(evidence.getWork() < LocusConditionedTraversalPolicy2D.v1().getWorkLimit());
		int estimated = 0;
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			if (cell.getKind() == LocusConditionedCellKind2D.ABSENT_BAND) {
				assertEquals(LocusConditionedAbsenceCause2D.INTERSECTION_ROOT_ABSENT,
						cell.getCause());
			}
			if (cell.getKind().isSpan()) {
				// A separation observed at nodes is never a bound between them.
				assertNotEquals(LocusConditionedSpanClass2D.MARGIN_BOUNDED,
						cell.getSpanClass());
				estimated += cell.getSpanClass()
						== LocusConditionedSpanClass2D.MARGIN_ESTIMATED ? 1 : 0;
			}
		}
		assertTrue(estimated > 0);
		assertTrue(evidence.inOneRegion(2.8, 3.1));
		assertFalse(evidence.inOneRegion(0.0, 3.0));
		// The same evidence whatever was evaluated in between: it is content of the revision.
		assertSame(evidence, consumer.getConditionedEvidence(BRANCH));
	}

	@Test
	void theEvaluatorTestifiesTheSettledStateOfAnInheritedLineConicSelection() {
		add("c=Circle((0,0),2)");
		add("s=Segment((-1,0),(1,0))");
		add("P=Point(s)");
		add("g=Line(P,P+(0,1))");
		add("I=Intersect(c,g,1)");
		GeoLocusV2 locus = add("L=LocusV2(I,P)");
		assertNotNull(locus);
		LocusConditionedSample2D sample = sample(locus, 0.5);
		assertTrue(sample.isValid());
		assertEquals(1, sample.getTestimony().size());
		LocusSelectionTestimony2D testimony = sample.getTestimony().get(0);
		assertEquals(LocusSelectionTestimony2D.Kind.LINE_CONIC_SELECTION, testimony.getKind());
		assertTrue(testimony.isEstablished());
		assertFalse(testimony.hasSeparation());
		assertTrue(sample.hasSameSelections(sample(locus, 0.25)));
	}
}
