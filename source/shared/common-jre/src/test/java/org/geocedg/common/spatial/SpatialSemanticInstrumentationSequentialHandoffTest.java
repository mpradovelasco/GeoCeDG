/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.kernel.spatial.semantic.SpatialSemanticInstrumentation;
import org.junit.jupiter.api.Test;

/** Regression coverage for sequential host-thread lifecycle handoff. */
class SpatialSemanticInstrumentationSequentialHandoffTest {

	@Test
	void emptyStagedMergeMayCrossASequentialHostThreadHandoff()
			throws InterruptedException {
		SpatialSemanticInstrumentation live =
				new SpatialSemanticInstrumentation();
		final long[] evidenceBefore = evidenceSnapshot(live);
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread hostThread = new Thread(() -> {
			try {
				SpatialSemanticInstrumentation staged =
						new SpatialSemanticInstrumentation();
				live.preflightMergeFrom(staged);
				live.mergeFrom(staged);
			} catch (Throwable throwable) {
				failure.set(throwable);
			}
		}, "g9u0-r1-sequential-host");
		hostThread.setDaemon(true);

		hostThread.start();
		hostThread.join(10_000);

		assertFalse(hostThread.isAlive());
		assertNull(failure.get());
		assertArrayEquals(evidenceBefore, evidenceSnapshot(live));
		assertTrue(live.snapshotAuthoritativePublicationCounts().isEmpty());
	}

	@Test
	void nonEmptyStagedMergeStillRejectsForeignThread()
			throws InterruptedException {
		SpatialSemanticInstrumentation live =
				new SpatialSemanticInstrumentation();
		final long[] evidenceBefore = evidenceSnapshot(live);
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread hostThread = new Thread(() -> {
			try {
				SpatialSemanticInstrumentation staged =
						new SpatialSemanticInstrumentation();
				staged.recordDependencyUpdate();
				live.mergeFrom(staged);
			} catch (Throwable throwable) {
				failure.set(throwable);
			}
		}, "g9u0-r1-invalid-nonempty-handoff");
		hostThread.setDaemon(true);

		hostThread.start();
		hostThread.join(10_000);

		assertFalse(hostThread.isAlive());
		assertInstanceOf(IllegalStateException.class, failure.get());
		assertTrue(failure.get().getMessage().contains("thread-confined"));
		assertArrayEquals(evidenceBefore, evidenceSnapshot(live));
		assertTrue(live.snapshotAuthoritativePublicationCounts().isEmpty());
	}

	private static long[] evidenceSnapshot(
			SpatialSemanticInstrumentation instrumentation) {
		return new long[] {
				instrumentation.getFrameEvaluations(),
				instrumentation.getProjectionSystemEvaluations(),
				instrumentation.getDiagramMapForwardEvaluations(),
				instrumentation.getDiagramMapInverseEvaluations(),
				instrumentation.getHingeConsistencyEvaluations(),
				instrumentation.getChangeOfPlaneConsistencyEvaluations(),
				instrumentation.getProjectionSystemCertificatePublications(),
				instrumentation.getProjectionSystemCertificateRejections(),
				instrumentation.getReconstructionAttempts(),
				instrumentation.getRankEvaluations(),
				instrumentation.getCandidateObjectsBuilt(),
				instrumentation.getReprojectionEvaluations(),
				instrumentation.getCertificatePublications(),
				instrumentation.getFailurePublications(),
				instrumentation.getSupersededCandidateRejections(),
				instrumentation.getDependencyUpdates(),
				instrumentation.getDerivedViewPublications(),
				instrumentation.getDerivedViewWithdrawals(),
				instrumentation.getAuthoritativePublicationEpoch()
		};
	}
}
