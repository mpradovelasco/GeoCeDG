/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessment;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessmentStatus;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineExecutionMode;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineImpactEntry;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineImpactReport;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Classic 5 consumption of the typed A3 kernel assessment. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PostG9U1A3FrontendRedefineTest {

	@Test
	void advancedRetainAndForcedRelocationNeedNoLegacyConfirmation()
			throws Exception {
		AppGeoCeDG app = appWithBaseSpline();
		assertTrue(app.getGuiManager().getAlgebraView() instanceof GeoCeDGAlgebraView);
		RecordingPresentation presentation = install(app);
		PersistentGeoId id = id(app, lookup(app, "S"));

		assertNotNull(submit(app, "S=SplineV2(pointsA,degree)").output);
		assertEquals(id, id(app, lookup(app, "S")));
		assertEquals(0, presentation.confirmations.size());

		for (String command : new String[] {"LeftUnaffected=7", "D=(-2,1)",
				"E=(0,2)", "F=(2,1)", "pointsB={D,E,F}", "RightUnaffected=8"}) {
			eval(app, command);
		}
		assertNotNull(submit(app, "S=SplineV2(pointsB,degree)").output);
		assertEquals(id, id(app, lookup(app, "S")));
		assertTrue(lookup(app, "pointsB").getConstructionIndex()
				< lookup(app, "S").getConstructionIndex());
		assertTrue(lookup(app, "S").getConstructionIndex()
				< lookup(app, "RightUnaffected").getConstructionIndex());
		assertTrue(lookup(app, "LeftUnaffected").getConstructionIndex()
				< lookup(app, "pointsB").getConstructionIndex());
		assertEquals(0, presentation.confirmations.size());
	}

	@Test
	void legacyCancelPresentsCompleteImpactAndLeavesConstructionUnchanged()
			throws Exception {
		AppGeoCeDG app = appWithImpactedSpline();
		RecordingPresentation presentation = install(app);
		UndoManagerD undo = establishUndoBaseline(app);
		String xml = app.getXML();
		PersistentGeoId id = id(app, lookup(app, "S"));

		Submission result = submit(app, "S=SplineV2(pointsA,degree,w)");

		assertNull(result.output);
		assertEquals("", result.errors.getErrors());
		assertEquals(xml, app.getXML());
		assertEquals(id, id(app, lookup(app, "S")));
		assertFalse(undo.undoPossible());
		assertEquals(1, presentation.confirmations.size());
		SpatialRedefineImpactReport impact = presentation.confirmations.get(0)
				.getImpactReport();
		assertNotNull(impact);
		assertEquals(SpatialRedefineImpactReport.Completeness.IMPACT_COMPLETE,
				impact.getCompleteness());
		assertEquals(impact.getEntries().size(), presentation.presentedEntryCount);
		String message = GeoCeDGSpatialRedefineFrontend.buildLegacyMessage(
				presentation.confirmations.get(0), "en");
		impact.getEntries().forEach(entry -> {
			assertTrue(message.contains(entry.getParticipantId().toExternalForm()));
			assertTrue(message.contains(entry.getRelationType()));
			entry.getAffectedSourceIds().forEach(source ->
					assertTrue(message.contains(source.toExternalForm())));
		});
	}

	@Test
	void explicitLegacyAcceptanceCreatesFreshIdentityAsOneUndoOperation()
			throws Exception {
		AppGeoCeDG app = appWithImpactedSpline();
		RecordingPresentation presentation = install(app);
		presentation.acceptLegacy = true;
		PersistentGeoId oldId = id(app, lookup(app, "S"));
		UndoManagerD undo = establishUndoBaseline(app);

		Submission result = submit(app, "S=SplineV2(pointsA,degree,w)");

		assertNotNull(result.output);
		PersistentGeoId replacementId = id(app, lookup(app, "S"));
		assertNotEquals(oldId, replacementId);
		assertEquals(1, presentation.confirmations.size());
		await(undo::undoPossible);
		assertEquals(1, undo.getHistorySize());
		app.getKernel().undo();
		await(() -> oldId.equals(id(app, lookup(app, "S"))));
		assertEquals(oldId, id(app, lookup(app, "S")));
	}

	@Test
	void staleConfirmationNeverExecutesAndNextSubmissionUsesNewAssessment()
			throws Exception {
		AppGeoCeDG app = appWithImpactedSpline();
		RecordingPresentation presentation = install(app);
		PersistentGeoId oldId = id(app, lookup(app, "S"));
		presentation.onConfirmation = () -> {
			if (presentation.confirmations.size() == 1) {
				eval(app, "ConcurrentMutation=1");
			}
		};
		presentation.acceptLegacy = true;

		Submission stale = submit(app, "S=SplineV2(pointsA,degree,w)");
		assertNull(stale.output);
		assertEquals(oldId, id(app, lookup(app, "S")));
		SpatialRedefineAssessment first = presentation.confirmations.get(0);
		assertFalse(first.isCurrent());
		assertEquals(1, presentation.staleNotifications);

		Submission fresh = submit(app, "S=SplineV2(pointsA,degree,w)");
		assertNotNull(fresh.output);
		assertEquals(2, presentation.confirmations.size());
		assertNotEquals(first, presentation.confirmations.get(1));
		assertNotEquals(oldId, id(app, lookup(app, "S")));
	}

	@Test
	void invalidAndNonOfferableStatusesNeverExposeLegacyExecution() {
		RecordingPresentation presentation = new RecordingPresentation();
		GeoCeDGSpatialRedefineFrontend frontend =
				new GeoCeDGSpatialRedefineFrontend(presentation);
		for (SpatialRedefineAssessmentStatus status : List.of(
				SpatialRedefineAssessmentStatus.INVALID_DAG,
				SpatialRedefineAssessmentStatus.INCOMPATIBLE_HOST_REDEFINE,
				SpatialRedefineAssessmentStatus.UNSUPPORTED,
				SpatialRedefineAssessmentStatus.AMBIGUOUS,
				SpatialRedefineAssessmentStatus.STALE_ASSESSMENT)) {
			SpatialRedefineAssessment assessment = mock(SpatialRedefineAssessment.class);
			when(assessment.getStatus()).thenReturn(status);
			assertNull(frontend.selectExecutionMode(assessment));
			assertSame(assessment, presentation.unavailable.get(
					presentation.unavailable.size() - 1));
		}
		assertEquals(0, presentation.confirmations.size());
	}

	@Test
	void ordinaryClassicGeoGebraRedefineRemainsUnchanged() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		RecordingPresentation presentation = install(app);
		eval(app, "k=1");

		Submission result = submit(app, "k=7");

		assertNotNull(result.output);
		assertEquals(7, lookup(app, "k").evaluateDouble());
		assertTrue(presentation.confirmations.isEmpty());
		assertTrue(presentation.unavailable.isEmpty());
	}

	@Test
	void frontendMapsBothAdvancedStatusesOnlyToAdvancedMode() {
		RecordingPresentation presentation = new RecordingPresentation();
		GeoCeDGSpatialRedefineFrontend frontend =
				new GeoCeDGSpatialRedefineFrontend(presentation);
		for (SpatialRedefineAssessmentStatus status : List.of(
				SpatialRedefineAssessmentStatus.ADVANCED_RETAIN_AVAILABLE,
				SpatialRedefineAssessmentStatus
						.ADVANCED_RETAIN_AVAILABLE_WITH_RELOCATION)) {
			SpatialRedefineAssessment assessment = mock(SpatialRedefineAssessment.class);
			when(assessment.getStatus()).thenReturn(status);
			assertEquals(SpatialRedefineExecutionMode.ADVANCED_RETAIN,
					frontend.selectExecutionMode(assessment));
		}
		assertTrue(presentation.confirmations.isEmpty());
	}

	@Test
	void localizedPresentationCoversEveryClosedImpactCode() {
		for (SpatialRedefineImpactEntry.PredictedStatus status
				: SpatialRedefineImpactEntry.PredictedStatus.values()) {
			assertFalse(GeoCeDGProfile.getText("Redefine.Predicted." + status.name(),
					"en").isBlank());
			assertFalse(GeoCeDGProfile.getText("Redefine.Predicted." + status.name(),
					"es").isBlank());
		}
		for (SpatialRedefineImpactEntry.RecoveryClass recovery
				: SpatialRedefineImpactEntry.RecoveryClass.values()) {
			assertFalse(GeoCeDGProfile.getText("Redefine.Recovery." + recovery.name(),
					"en").isBlank());
		}
		for (SpatialRedefineImpactEntry.ReasonCode reason
				: SpatialRedefineImpactEntry.ReasonCode.values()) {
			assertFalse(GeoCeDGProfile.getText("Redefine.Reason." + reason.name(),
					"en").isBlank());
		}
	}

	private static AppGeoCeDG appWithBaseSpline() {
		AppGeoCeDG app = G9U1TestApp.create();
		for (String command : new String[] {"A=(-2,0)", "B=(0,1)", "C=(2,0)",
				"pointsA={A,B,C}", "degree=3", "S=SplineV2(pointsA,degree)"}) {
			eval(app, command);
		}
		return app;
	}

	private static AppGeoCeDG appWithImpactedSpline() {
		AppGeoCeDG app = appWithBaseSpline();
		for (String command : new String[] {"w(x,y)=1+x^2+y^2",
				"P=Point(S,\"spline-v2/main\",-1)",
				"R=Point(S,\"spline-v2/main\",1)", "M=LocusLength(S,P,R)"}) {
			eval(app, command);
		}
		return app;
	}

	private static RecordingPresentation install(AppGeoCeDG app) {
		RecordingPresentation presentation = new RecordingPresentation();
		app.setSpatialRedefinePresentation(presentation);
		return presentation;
	}

	private static PersistentGeoId id(AppGeoCeDG app, GeoElement geo) {
		PersistentGeoId result = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(geo);
		assertNotNull(result);
		return result;
	}

	private static Submission submit(AppGeoCeDG app, String command) throws Exception {
		AtomicReference<GeoElementND[]> output = new AtomicReference<>();
		ErrorAccumulator errors = new ErrorAccumulator();
		GeoCeDGAlgebraInputSubmission.submit(app, command,
				new EvalInfo(true, true).withSliders(true).withSymbolic(true), errors,
				output::set);
		return new Submission(output.get(), errors);
	}

	private static UndoManagerD establishUndoBaseline(AppGeoCeDG app) throws Exception {
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction()
				.getUndoManager();
		try (UndoManagerD.PreparedUndoBaseline baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		return undo;
	}

	private static void await(BooleanSupplier condition) throws InterruptedException {
		long deadline = System.nanoTime() + 10_000_000_000L;
		while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
			Thread.sleep(10);
		}
		assertTrue(condition.getAsBoolean());
	}

	private record Submission(GeoElementND[] output, ErrorAccumulator errors) {
	}

	private static final class RecordingPresentation
			implements GeoCeDGSpatialRedefineFrontend.Presentation {
		private final List<SpatialRedefineAssessment> confirmations = new ArrayList<>();
		private final List<SpatialRedefineAssessment> unavailable = new ArrayList<>();
		private boolean acceptLegacy;
		private Runnable onConfirmation = () -> { };
		private int presentedEntryCount;
		private int staleNotifications;

		@Override
		public boolean confirmLegacy(SpatialRedefineAssessment assessment) {
			confirmations.add(assessment);
			presentedEntryCount = assessment.getImpactReport().getEntries().size();
			onConfirmation.run();
			return acceptLegacy;
		}

		@Override
		public void showUnavailable(SpatialRedefineAssessment assessment) {
			unavailable.add(assessment);
		}

		@Override
		public void showStaleAssessment() {
			staleNotifications++;
		}
	}
}
