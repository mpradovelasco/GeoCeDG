/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D.Failure;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D.Permission;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D.Rejected;
import org.geocedg.common.kernel.locus.intersection.M08LocalAddressEvidence2D.Work;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** H1: actual evaluator counters are independent of the admission work ledger. */
class M08SnapshotPlacementBudgetTest extends G9U0PublicSurfaceTestBase {

	private final List<GeoLocusV2> chain = new ArrayList<>();

	private GeoLocusV2 composed(int depth) {
		chain.add(add("S=SplineV2({(-2,0),(-2/3,0),(2/3,0),(2,0)},3)"));
		String previous = "S";
		for (int index = 1; index <= depth; index++) {
			String label = "T" + index;
			chain.add(add(label + "=Translate(" + previous + ",(0,0))"));
			previous = label;
		}
		return chain.get(depth);
	}

	private long actualCalls() {
		return chain.stream().mapToLong(source -> source.getInstrumentation().snapshot()
				.getEvaluatorCalls()).sum();
	}

	private M08SnapshotPlacement2D.Selection query(GeoLocusV2 source, int semantic, int interval) {
		return M08SnapshotPlacement2D.query(source, 0.25, 0.01,
				new LocusPointInteractionPolicy2D(0.1, 1E-10, 1E-12,
						new LocusPointInteractionWorkBudget2D(semantic, interval, 80, 1024)));
	}

	private static M08SnapshotPlacement2D.Choice offered(M08SnapshotPlacement2D.Selection selection) {
		assertEquals(Permission.EXPLICIT_LOCAL_SELECTION_REQUIRED,
				selection.getResult().getPermission(), selection.getResult().getFailure().name());
		return selection.getResult().getChoices().get(0);
	}

	@Test
	void h1Limit149CannotOfferAfter154ActualEvaluations() {
		var source = composed(10);
		String before = getApp().getXML();
		long calls = actualCalls();
		var selection = query(source, 149, 512);
		assertEquals(143, selection.getWorkSnapshot().getDiscoveryMisses());
		assertEquals(149, actualCalls() - calls);
		assertEquals(149, selection.getWorkSnapshot().getSemanticMisses());
		assertEquals(Failure.BUDGET_EXHAUSTED, selection.getResult().getFailure());
		assertEquals(Permission.NOT_OFFERABLE, selection.getResult().getPermission());
		assertTrue(selection.getResult().getChoices().isEmpty());
		assertEquals(before, getApp().getXML());
		assertEquals(0, selection.getWorkSnapshot().getPublicationAttempts());
	}

	@Test
	void exactlySufficientAccumulatedBudgetPublishesWithoutAnUnchargedReevaluation() {
		var source = composed(10);
		long before = actualCalls();
		var selection = query(source, 165, 512);
		var choice = offered(selection);
		assertEquals(154, actualCalls() - before);
		assertEquals(11, selection.getWorkSnapshot().getRemainingSemanticMisses());
		GeoPoint p = selection.commit("P", choice);
		assertNotNull(p);
		assertEquals(165, actualCalls() - before);
		assertEquals(165, selection.getWorkSnapshot().getSemanticMisses());
		assertEquals(0, selection.getWorkSnapshot().getRemainingSemanticMisses());
		assertEquals(8, selection.getWorkSnapshot().getIntervalPasses());
		assertEquals(1, selection.getWorkSnapshot().getPublicationAttempts());
		assertEquals(choice.getAddress(), ((AlgoSemanticLocusPoint2D) p.getParentAlgorithm())
				.getCurrentSemanticAddress());
		// Future ordinary recomputation still uses the normal forward evaluator.
		long publishedCalls = actualCalls();
		p.getParentAlgorithm().compute();
		assertEquals(11, actualCalls() - publishedCalls);
	}

	@Test
	void oneSemanticUnitShortAtCommitLeavesDocumentAndIdentitiesUntouched() {
		var source = composed(10);
		long beforeCalls = actualCalls();
		var selection = query(source, 164, 512);
		var choice = offered(selection);
		String before = getApp().getXML();
		assertEquals(Failure.BUDGET_EXHAUSTED, assertThrows(Rejected.class,
				() -> selection.commit("P", choice)).getFailure());
		assertEquals(164, actualCalls() - beforeCalls);
		assertEquals(164, selection.getWorkSnapshot().getSemanticMisses());
		assertEquals(0, selection.getWorkSnapshot().getPublicationAttempts());
		assertEquals(before, getApp().getXML());
		assertNull(lookup("P"));
		assertEquals(Failure.CONSUMED_SELECTION, assertThrows(Rejected.class,
				() -> selection.commit("P", choice)).getFailure());
		assertEquals(164, actualCalls() - beforeCalls);
	}

	@Test
	void oneSemanticUnitShortAtQueryCannotPublishPartialProof() {
		var source = composed(10);
		long calls = actualCalls();
		String before = getApp().getXML();
		var selection = query(source, 153, 512);
		assertEquals(Failure.BUDGET_EXHAUSTED, selection.getResult().getFailure());
		assertEquals(153, actualCalls() - calls);
		assertEquals(Permission.NOT_OFFERABLE, selection.getResult().getPermission());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void exactlySufficientQueryHasNoFictitiousRemainderForConfirmation() {
		var source = composed(10);
		long calls = actualCalls();
		var selection = query(source, 154, 512);
		var choice = offered(selection);
		String before = getApp().getXML();
		assertEquals(0, selection.getWorkSnapshot().getRemainingSemanticMisses());
		assertEquals(Failure.BUDGET_EXHAUSTED, assertThrows(Rejected.class,
				() -> selection.commit("P", choice)).getFailure());
		assertEquals(154, actualCalls() - calls);
		assertEquals(before, getApp().getXML());
	}

	@Test
	void intervalPassesHaveTheirOwnExplicitUnitsAndExactAccumulatedLimit() {
		var source = composed(0);
		var selection = query(source, 1000, 8);
		var choice = offered(selection);
		assertEquals(4, selection.getWorkSnapshot().getIntervalPasses());
		selection.commit("P", choice);
		assertEquals(8, selection.getWorkSnapshot().getIntervalLimit());
		assertEquals(8, selection.getWorkSnapshot().getIntervalPasses());
		for (Work work : Work.values()) {
			assertEquals(2, selection.getWorkSnapshot().getIntervalPasses(work));
		}
		assertEquals(15, selection.getWorkSnapshot().getSemanticMisses());
	}

	@Test
	void oneIntervalPassShortAtConfirmationRejectsBeforeAnyReservation() {
		var source = composed(0);
		var selection = query(source, 1000, 7);
		var choice = offered(selection);
		String before = getApp().getXML();
		assertEquals(Failure.BUDGET_EXHAUSTED, assertThrows(Rejected.class,
				() -> selection.commit("P", choice)).getFailure());
		assertEquals(7, selection.getWorkSnapshot().getIntervalPasses());
		assertEquals(0, selection.getWorkSnapshot().getPublicationAttempts());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void staleCancelledAndConsumedSelectionsCannotSpendMoreWork() {
		var source = composed(0);
		var selection = query(source, 1000, 512);
		var choice = offered(selection);
		source.setUndefined();
		long work = selection.getWorkSnapshot().getSemanticMisses();
		String before = getApp().getXML();
		assertEquals(Failure.STALE_SELECTION, assertThrows(Rejected.class,
				() -> selection.commit("P", choice)).getFailure());
		assertEquals(work, selection.getWorkSnapshot().getSemanticMisses());
		assertEquals(before, getApp().getXML());
		selection.close();
		assertEquals(Failure.CONSUMED_SELECTION, assertThrows(Rejected.class,
				() -> selection.commit("P", choice)).getFailure());
	}

	@Test
	void preparedForwardCapabilityCannotBeConsumedOutsideItsCommit() {
		var source = composed(0);
		var selection = query(source, 1000, 512);
		var choice = offered(selection);
		String before = getApp().getXML();
		assertEquals(Failure.PUBLICATION_NOT_AUTHORIZED, assertThrows(Rejected.class,
				() -> choice.takePreparedBinding(source)).getFailure());
		assertEquals(Failure.PUBLICATION_NOT_AUTHORIZED, assertThrows(Rejected.class,
				() -> AlgoSemanticLocusPoint2D.createWithSnapshotBinding(
						getConstruction(), source, null, null, choice)).getFailure());
		assertEquals(before, getApp().getXML());
		selection.close();
	}

	@Test
	void hardSessionCountsNestedServicesEvenWhenTheirExceptionIsAbsorbed() {
		LocusDefinition2D inner = composed(0).getSemanticDefinition();
		String branch = inner.getBranches().get(0).getBranchKey();
		long scope = LocusEvaluationSession2D.newSliceScope();
		LocusDefinition2D outer = new LocusDefinition2D("h1-outer", 1,
				inner.getDefinitionStatus(), inner.getProvider(), inner.getBranches(),
				(definition, semanticBranch, parameter, session) -> {
					LocusEvaluationSession2D.runWithNestedServiceParent(session, scope, () -> {
						try (var child = LocusEvaluationSession2D.openServiceSession(false, 1)) {
							for (int i = 0; i < 20; i++) {
								inner.evaluate(branch, parameter + i * 0.001, child);
							}
						}
					});
					return inner.evaluate(branch, parameter, session);
				}, inner.getDeterminism(), "h1-outer/v1", new LocusInstrumentation2D());
		try (var session = LocusEvaluationSession2D.hardBoundedForSnapshot(16, 3, 128)) {
			assertFalse(outer.evaluate(branch, 0.5, session).isValid());
			assertEquals(3, session.getTotalChargedMisses());
			assertEquals(2, session.getNestedMisses());
			assertTrue(session.isWorkLimitExhausted());
			session.clear();
			assertTrue(session.isWorkLimitExhausted());
			assertEquals(3, session.getTotalChargedMisses());
		}
	}

	@Test
	void exactKeyCacheHitsDoNotPretendToExecuteSemanticEvaluators() {
		var source = composed(0);
		var definition = source.getSemanticDefinition();
		String branch = definition.getBranches().get(0).getBranchKey();
		long calls = actualCalls();
		try (var session = LocusEvaluationSession2D.hardBoundedForSnapshot(16, 1, 128)) {
			assertTrue(definition.evaluate(branch, 0.5, session).isValid());
			assertTrue(definition.evaluate(branch, 0.5, session).isValid());
			assertEquals(1, session.getHits());
			assertEquals(1, session.getTotalChargedMisses());
			assertEquals(1, actualCalls() - calls);
			assertThrows(LocusEvaluationSession2D.EvaluationWorkLimitException.class,
					() -> definition.evaluate(branch, 0.6, session));
			assertEquals(1, actualCalls() - calls);
			assertTrue(session.isWorkLimitExhausted());
		}
	}
}
