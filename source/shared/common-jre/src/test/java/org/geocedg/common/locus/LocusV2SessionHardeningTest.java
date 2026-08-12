/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.EnumSet;

import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInstrumentationSnapshot2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusSessionDiagnostic2D.Kind;
import org.junit.jupiter.api.Test;

class LocusV2SessionHardeningTest {
	private static final String BRANCH_KEY = "session.sheet.main";

	@Test
	void boundedMemoizationUsesDeterministicFifoEviction() {
		LocusInstrumentation2D instrumentation = new LocusInstrumentation2D();
		LocusDefinition2D definition = definition("fifo", 1, instrumentation,
				(parameter, session) -> point(parameter));
		LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(2);

		definition.evaluate(BRANCH_KEY, -0.5, session);
		definition.evaluate(BRANCH_KEY, 0, session);
		definition.evaluate(BRANCH_KEY, -0.5, session);
		definition.evaluate(BRANCH_KEY, 0.5, session);
		definition.evaluate(BRANCH_KEY, -0.5, session);

		assertEquals(2, session.getCachedEntryCount());
		assertEquals(1, session.getHits());
		assertEquals(4, session.getMisses());
		assertEquals(2, session.getEvictions());
		assertEquals(4, instrumentation.getEvaluatorCalls());
	}

	@Test
	void evaluatorExceptionAlwaysCleansActiveKeyStack() {
		LocusDefinition2D definition = definition("exception", 1,
				new LocusInstrumentation2D(), (parameter, session) -> {
					throw new IllegalStateException("controlled failure");
				});
		LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4);

		LocusEvaluation2D result = definition.evaluate(BRANCH_KEY, 0, session);
		assertEquals(EvaluationStatus.EVALUATION_FAILED, result.getStatus());
		assertTrue(result.getDiagnostic().contains("controlled failure"));
		assertEquals(0, session.getActiveDepth());
		assertEquals(Kind.NONE, session.getLastDiagnostic().getKind());
	}

	@Test
	void reentryCycleProducesTypedDiagnosticAndCleansStack() {
		LocusDefinition2D[] holder = new LocusDefinition2D[1];
		holder[0] = definition("cycle", 1, new LocusInstrumentation2D(),
				(parameter, session) -> holder[0].evaluate(BRANCH_KEY, parameter,
						session));
		LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4);

		LocusEvaluation2D result = holder[0].evaluate(BRANCH_KEY, 0.25, session);
		assertEquals(EvaluationStatus.EVALUATION_FAILED, result.getStatus());
		assertEquals(Kind.CYCLE_REENTRY, session.getLastDiagnostic().getKind());
		assertEquals(2, session.getLastDiagnostic().getActivePath().size());
		assertEquals(1, session.getCycles());
		assertEquals(0, session.getActiveDepth());
	}

	@Test
	void oneSessionRejectsMixedRevisionsButCanBeExplicitlyCleared() {
		LocusDefinition2D revisionOne = definition("revisioned", 1,
				new LocusInstrumentation2D(),
				(parameter, session) -> point(parameter));
		LocusDefinition2D revisionTwo = definition("revisioned", 2,
				new LocusInstrumentation2D(),
				(parameter, session) -> point(parameter + 1));
		LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8);

		assertTrue(revisionOne.evaluate(BRANCH_KEY, 0, session).isValid());
		LocusEvaluation2D mixed = revisionTwo.evaluate(BRANCH_KEY, 0, session);
		assertEquals(EvaluationStatus.EVALUATION_FAILED, mixed.getStatus());
		assertEquals(Kind.INCOHERENT_REVISION,
				session.getLastDiagnostic().getKind());
		session.clear();
		assertEquals(0, session.getCachedEntryCount());
		assertTrue(session.getCoherentRevisions().isEmpty());
		assertEquals(new LocusPoint2D(1, 1),
				revisionTwo.evaluate(BRANCH_KEY, 0, session).getPoint());
	}

	@Test
	void disposedSessionReleasesStateAndCannotBeReused() {
		LocusDefinition2D definition = definition("disposed", 1,
				new LocusInstrumentation2D(),
				(parameter, session) -> point(parameter));
		LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8);
		definition.evaluate(BRANCH_KEY, 0, session);
		session.close();

		assertTrue(session.isClosed());
		assertEquals(0, session.getCachedEntryCount());
		assertTrue(session.getCoherentRevisions().isEmpty());
		LocusEvaluation2D afterClose = definition.evaluate(BRANCH_KEY, 0, session);
		assertEquals(EvaluationStatus.EVALUATION_FAILED, afterClose.getStatus());
		assertEquals(Kind.CLOSED_SESSION, session.getLastDiagnostic().getKind());
	}

	@Test
	void sessionOnOffResultsMatchAndInstrumentationSnapshotIsImmutable() {
		LocusInstrumentation2D instrumentation = new LocusInstrumentation2D();
		LocusDefinition2D definition = definition("equivalence", 1,
				instrumentation, (parameter, session) -> point(parameter));
		LocusEvaluation2D reference = definition.evaluate(BRANCH_KEY, 0.75,
				LocusEvaluationSession2D.reference());
		LocusEvaluationSession2D memoizing = LocusEvaluationSession2D.memoizing(4);
		LocusEvaluation2D cached = definition.evaluate(BRANCH_KEY, 0.75,
				memoizing);
		definition.evaluate(BRANCH_KEY, 0.75, memoizing);
		LocusInstrumentationSnapshot2D snapshot = instrumentation.snapshot();

		assertEquals(reference, cached);
		assertEquals(2, snapshot.getEvaluatorCalls());
		assertEquals(1, snapshot.getSessionHits());
		assertEquals(2, snapshot.getSessionMisses());
		assertFalse(snapshot.getEvaluatorCallsByLocus().isEmpty());
	}

	private static LocusDefinition2D definition(String identity, long revision,
			LocusInstrumentation2D instrumentation, TestEvaluator evaluator) {
		ExplicitNumericDomainProvider2D provider =
				new ExplicitNumericDomainProvider2D("session-parameter/v1",
						new LocusInterval2D(-1, 1, true, true),
						Orientation.INCREASING, false, 1E-14);
		LocusBranch2D branch = new LocusBranch2D(BRANCH_KEY,
				provider.getDeclaredDomain(),
				Collections.singletonList(provider.getDeclaredDomain()),
				Orientation.INCREASING, "g6r-session-fixture/v1",
				LocusLineage2D.unchanged(), EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());
		return new LocusDefinition2D(identity, revision, DefinitionStatus.VALID,
				provider, Collections.singletonList(branch),
				(definition, semanticBranch, parameter, session) ->
						evaluator.evaluate(parameter, session),
				Determinism.POINTWISE_DETERMINISTIC, identity + "/v1",
				instrumentation);
	}

	private static LocusEvaluation2D point(double parameter) {
		return LocusEvaluation2D.valid(new LocusPoint2D(parameter, parameter),
				Regularity.UNKNOWN, LocusQuality2D.analyticDoubleSemantic());
	}

	private interface TestEvaluator {
		LocusEvaluation2D evaluate(double parameter,
				LocusEvaluationSession2D session);
	}
}
