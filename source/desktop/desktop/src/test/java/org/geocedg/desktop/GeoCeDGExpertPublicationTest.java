/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED;
import static org.geocedg.desktop.GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET;
import static org.geocedg.desktop.GeoCeDGDocumentInsertion.Outcome.INSERTED;
import static org.geocedg.desktop.GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION;
import static org.geocedg.desktop.GeoCeDGDocumentInsertion.Outcome.SERIOUS_RECOVERY_FAILURE;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.PathRegionHandling;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Expert-only use of the same transaction, history and recovery barriers as F3. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class GeoCeDGExpertPublicationTest {
	private static final String REPORT = "a".repeat(64);
	private static final String OTHER_REPORT = "b".repeat(64);

	@TempDir
	Path directory;

	@Test
	void publishesTheConsentedArcAndUndoesToTheUnsavedLiveReceiver() throws Exception {
		onEdt(() -> {
			Source source = source();
			AppGeoCeDG app = receiver();
			var resident = app.getKernel().lookupLabel("A");
			String uuid = app.getUniqueId();
			var construction = app.getKernel().getConstruction();
			construction.addProtectedLabel("reserve");
			must(app, "Live=(7,8)");
			app.setUnsaved();
			var resources = GeoCeDGExpertIntegrity.receiverResources(app);
			var service = app.getDocumentInsertion();
			var review = service.beginExpertReview();
			Trial trial = trial(app, review, source);
			var offer = review.offer(source.file(), source.bytes(), trial.fingerprint(), REPORT, () -> true);
			var result = service.publishExpert(offer, REPORT);
			assertEquals(INSERTED, result.getOutcome(), String.valueOf(result.getFailure()));
			assertNotNull(result.getExpertStep());
			assertSame(resident, app.getKernel().lookupLabel("A"));
			assertEquals(Set.of("reserve"), construction.snapshotProtectedLabels());
			assertEquals(uuid, app.getUniqueId());
			assertFalse(app.isSaved());
			assertEquals(resources, GeoCeDGExpertIntegrity.receiverResources(app));
			var arc = (GeoConicPart) app.getKernel().lookupLabel(trial.arcLabel());
			assertTrue(arc.isDefined());
			assertNull(GeoCeDGInsertionAdmission.classify(arc));
			assertEquals(trial.layer(), arc.getLayer());
			assertEquals(REJECTED_BEFORE_MUTATION, service.publishExpert(offer, REPORT).getOutcome());
			assertSame(result.getExpertStep(), undo(app).getCommands().get(undo(app).getCursor() - 1));
			undo(app).undo();
			assertNull(app.getKernel().lookupLabel(trial.arcLabel()));
			assertNotNull(app.getKernel().lookupLabel("Live"));
			undo(app).redo();
			assertTrue(app.getKernel().lookupLabel(trial.arcLabel()).isDefined());
			return null;
		});
	}

	@Test
	void cancelledOfferLeavesTheReceiverAndHistoryUntouched() throws Exception {
		onEdt(() -> {
			Source source = source();
			AppGeoCeDG app = receiver();
			var before = GeoCeDGDocumentInsertion.Certificate.of(app);
			var history = GeoCeDGDocumentInsertion.HistoryRecord.of(undo(app));
			var service = app.getDocumentInsertion();
			GeoCeDGDocumentInsertion.ExpertOffer offer;
			try (var review = service.beginExpertReview()) {
				offer = review.offer(source.file(), source.bytes(), REPORT, REPORT, () -> true);
			}
			assertEquals(REJECTED_BEFORE_MUTATION, service.publishExpert(offer, REPORT).getOutcome());
			var after = GeoCeDGDocumentInsertion.Certificate.of(app);
			assertEquals(List.of(), before.serializableDifferences(after));
			assertTrue(before.sameInstances(after));
			assertNull(history.mismatch(undo(app), null));
			return null;
		});
	}

	@Test
	void rejectsAnUnexpectedResultAndRestoresTheReceiverBeforeHistoryPublication() throws Exception {
		onEdt(() -> {
			Source source = source();
			for (boolean afterStore : List.of(false, true)) {
				AppGeoCeDG app = receiver();
				var before = GeoCeDGDocumentInsertion.Certificate.of(app);
				var service = app.getDocumentInsertion();
				var review = service.beginExpertReview();
				Trial trial = trial(app, review, source);
				var history = GeoCeDGDocumentInsertion.HistoryRecord.of(undo(app));
				var offer = review.offer(source.file(), source.bytes(), trial.fingerprint(), REPORT, () -> true);
				var changed = new AtomicBoolean();
				service.setFaults(new GeoCeDGDocumentInsertion.Faults() {
					@Override
					public void beforeOwnStore() {
						if (!afterStore) {
							changeCopiedPoint();
						}
					}

					@Override
					public void beforeAttach() {
						if (afterStore) {
							changeCopiedPoint();
						}
					}

					private void changeCopiedPoint() {
						if (changed.compareAndSet(false, true)) {
							var arc = (GeoConicPart) app.getKernel().lookupLabel(trial.arcLabel());
							var point = (GeoPoint) app.getKernel().lookupLabel("B");
							assertEquals(3.0, arc.getHalfAxis(0));
							point.setCoords(4, 0, 1);
							point.updateCascade();
							assertTrue(arc.isDefined());
							assertEquals(4.0, arc.getHalfAxis(0));
						}
					}
				});
				var result = service.publishExpert(offer, REPORT);
				assertTrue(changed.get(), String.valueOf(result.getFailure()));
				assertEquals(FAILED_RECOVERY_CERTIFIED, result.getOutcome(), String.valueOf(result.getFailure()));
				String stage = afterStore ? "history attachment" : "pre-history state";
				assertTrue(result.getFailure().getMessage().contains("differs from the consented trial at " + stage));
				var after = GeoCeDGDocumentInsertion.Certificate.of(app);
				assertTrue(before.sameInstances(after));
				assertEquals(List.of(), before.serializableDifferences(after));
				assertNull(app.getKernel().lookupLabel(trial.arcLabel()));
				assertNull(app.getKernel().lookupLabel("B"));
				assertNull(result.getExpertStep());
				assertNull(history.mismatch(undo(app), null));
			}
			return null;
		});
	}

	@Test
	void invalidatesAnOfferAcrossARevertedLayerEdit() throws Exception {
		onEdt(() -> {
			Source source = source();
			AppGeoCeDG app = receiver();
			var service = app.getDocumentInsertion();
			var review = service.beginExpertReview();
			var offer = review.offer(source.file(), source.bytes(), REPORT, REPORT, () -> true);
			var a = app.getKernel().lookupLabel("A");
			a.setLayer(2);
			a.setLayer(0);
			app.restoreMaxLayerUsed(0);
			assertTrue(review.snapshot().matchesCurrentState(app), "the bytes and effective context match again");
			assertFalse(review.isCurrent(), "the offer stays stale after a reversion");
			assertEquals(REJECTED_BEFORE_MUTATION, service.publishExpert(offer, REPORT).getOutcome());
			assertNull(app.getKernel().lookupLabel("arc"));
			return null;
		});
	}

	@Test
	void invalidatesAnOfferAcrossARevertedPathRegionModeEdit() throws Exception {
		onEdt(() -> {
			Source source = source();
			AppGeoCeDG app = receiver();
			var kernel = app.getKernel();
			kernel.setUsePathAndRegionParameters(PathRegionHandling.ON);
			app.setUnsaved();
			var before = GeoCeDGDocumentInsertion.Certificate.of(app);
			var history = GeoCeDGDocumentInsertion.HistoryRecord.of(undo(app));
			var service = app.getDocumentInsertion();
			var review = service.beginExpertReview();
			Trial trial = trial(app, review, source);
			var offer = review.offer(source.file(), source.bytes(), trial.fingerprint(), REPORT, () -> true);
			assertTrue(review.isCurrent());
			kernel.setUsePathAndRegionParameters(PathRegionHandling.OFF);
			kernel.setUsePathAndRegionParameters(PathRegionHandling.ON);
			assertTrue(review.snapshot().matchesCurrentState(app), "the native drawing matches again");
			assertFalse(review.isCurrent(), "the offer stays stale after restoring the mode");
			assertEquals(REJECTED_BEFORE_MUTATION, service.publishExpert(offer, REPORT).getOutcome());
			var after = GeoCeDGDocumentInsertion.Certificate.of(app);
			assertTrue(before.sameInstances(after));
			assertEquals(List.of(), before.serializableDifferences(after));
			assertNull(history.mismatch(undo(app), null));
			assertNull(kernel.lookupLabel(trial.arcLabel()));
			return null;
		});
	}

	@Test
	void retainsTheDurableIdentityWhenTheWorkerCannotCertifyItsRemapping() throws Exception {
		onEdt(() -> {
			Source source = source();
			AppGeoCeDG app = receiver();
			var point = app.getKernel().lookupLabel("A");
			var registry = point.getConstruction().getSpatialIdentityRegistry();
			var id = registry.allocatePersistentGeoId();
			registry.registerParticipation(point, new GeoIdentityRecord(id,
					ConstructionGeoRedefineProvider.PROVIDER_ID, "POINT",
					ConstructionGeoRedefineProvider.SCHEMA_ID,
					ConstructionGeoRedefineProvider.SCHEMA_VERSION,
					EditAuthorityMode.CONSTRUCTION_DEFINED, ProjectionBindingRole.NOT_APPLICABLE,
					"VALUE", 1, 0, 0));
			var before = GeoCeDGDocumentInsertion.Certificate.of(app);
			var history = GeoCeDGDocumentInsertion.HistoryRecord.of(undo(app));
			try (var review = app.getDocumentInsertion().beginExpertReview()) {
				Path staged = directory.resolve("identity-receiver.cedg");
				Path context = directory.resolve("identity-context.bin");
				review.snapshot().stageReceiver(staged);
				review.snapshot().stageLayerContext(context);
				var worker = new GeoCeDGExpertInspection();
				var report = worker.inspectExpert(source.file().toPath(), staged,
						GeoCeDGExpertContext.read(context), null);
				assertFalse(worker.expertTrial().available());
				assertTrue(report.diagnostics().stream().anyMatch(diagnostic -> diagnostic.detail()
						.contains("Spatial identity remapping and recovery are not verified")),
						report.diagnostics()::toString);
			}
			assertSame(point, app.getKernel().lookupLabel("A"));
			assertEquals(id, registry.getPersistentGeoId(point));
			assertEquals(List.of(), before.serializableDifferences(GeoCeDGDocumentInsertion.Certificate.of(app)));
			assertNull(history.mismatch(undo(app), null));
			return null;
		});
	}

	@Test
	void consumesConsentForTheExactReportAndRejectsChangedSourceInsideBarrier() throws Exception {
		onEdt(() -> {
			Source source = source();
			AppGeoCeDG app = receiver();
			var service = app.getDocumentInsertion();
			var review = service.beginExpertReview();
			var mismatched = review.offer(source.file(), source.bytes(), REPORT, REPORT, () -> true);
			assertEquals(REJECTED_BEFORE_MUTATION, service.publishExpert(mismatched, OTHER_REPORT).getOutcome());
			assertEquals(REJECTED_BEFORE_MUTATION, service.publishExpert(mismatched, REPORT).getOutcome());
			var second = service.beginExpertReview();
			var offer = second.offer(source.file(), source.bytes(), REPORT, REPORT, () -> true);
			service.setFaults(new GeoCeDGDocumentInsertion.Faults() {
				@Override
				public void beforeSnapshot() throws Exception {
					Files.write(source.file().toPath(), new byte[] {1});
				}
			});
			var history = GeoCeDGDocumentInsertion.HistoryRecord.of(undo(app));
			assertEquals(REJECTED_BEFORE_MUTATION, service.publishExpert(offer, REPORT).getOutcome());
			assertNull(history.mismatch(undo(app), null));
			assertNull(app.getKernel().lookupLabel("arc"));
			return null;
		});
	}

	@Test
	void aSourceChangeBeforeHistoryAttachRestoresTheReceiver() throws Exception {
		onEdt(() -> {
			Source source = source();
			AppGeoCeDG app = receiver();
			var service = app.getDocumentInsertion();
			var review = service.beginExpertReview();
			Trial trial = trial(app, review, source);
			var offer = review.offer(source.file(), source.bytes(), trial.fingerprint(), REPORT, () -> true);
			var history = GeoCeDGDocumentInsertion.HistoryRecord.of(undo(app));
			var changed = new AtomicBoolean();
			service.setFaults(new GeoCeDGDocumentInsertion.Faults() {
				@Override
				public void beforeAttach() throws Exception {
					if (changed.compareAndSet(false, true)) {
						Files.write(source.file().toPath(), new byte[] {1});
					}
				}
			});
			var result = service.publishExpert(offer, REPORT);
			assertTrue(changed.get(), String.valueOf(result.getFailure()));
			assertEquals(FAILED_RECOVERY_CERTIFIED, result.getOutcome(), String.valueOf(result.getFailure()));
			assertTrue(result.getFailure().getMessage().contains("changed before history attachment"));
			assertNull(history.mismatch(undo(app), null));
			assertTrue(app.isSaved());
			assertNull(app.getKernel().lookupLabel("arc"));
			return null;
		});
	}

	@Test
	void restoresExactLiveStateOrDeclaresTheGuardedSessionReset() throws Exception {
		onEdt(() -> {
			Source source = source();
			for (boolean reload : List.of(false, true)) {
				AppGeoCeDG app = receiver();
				var resident = app.getKernel().lookupLabel("A");
				resident.setLayer(4);
				app.getLayerWorkspace().setWorkingLayer(4);
				app.getSelectionManager().addSelectedGeo(resident);
				app.getKernel().getConstruction().addProtectedLabel("reserve");
				var before = GeoCeDGDocumentInsertion.Certificate.of(app);
				var resources = GeoCeDGExpertIntegrity.receiverResources(app);
				var history = GeoCeDGDocumentInsertion.HistoryRecord.of(undo(app));
				var service = app.getDocumentInsertion();
				AtomicBoolean injected = new AtomicBoolean();
				service.setFaults(new GeoCeDGDocumentInsertion.Faults() {
					@Override
					public void afterCommit() {
						injected.set(true);
						throw new IllegalStateException("injected after expert copy");
					}

					@Override
					public void duringRemoval() {
						if (reload) {
							throw new IllegalStateException("injected removal failure");
						}
					}
				});
				var review = service.beginExpertReview();
				Trial trial = trial(app, review, source);
				var offer = review.offer(source.file(), source.bytes(), trial.fingerprint(), REPORT, () -> true);
				var result = service.publishExpert(offer, REPORT);
				assertTrue(injected.get(), String.valueOf(result.getFailure()));
				assertEquals(reload ? FAILED_RECOVERY_CERTIFIED_SESSION_RESET : FAILED_RECOVERY_CERTIFIED,
						result.getOutcome(), String.valueOf(result.getFailure()));
				assertEquals(List.of(), before.serializableDifferences(GeoCeDGDocumentInsertion.Certificate.of(app)));
				assertEquals(resources, GeoCeDGExpertIntegrity.receiverResources(app));
				assertNull(history.mismatch(undo(app), null));
				assertEquals(4, app.getKernel().lookupLabel("A").getLayer());
				assertEquals(4, app.getLayerWorkspace().getWorkingLayer());
				assertEquals(Set.of("reserve"), app.getKernel().getConstruction().snapshotProtectedLabels());
				assertNull(app.getKernel().lookupLabel(trial.arcLabel()));
				if (!reload) {
					assertSame(resident, app.getKernel().lookupLabel("A"));
					assertEquals(List.of(resident), app.getSelectionManager().getSelectedGeos());
				}
			}
			return null;
		});
	}

	@Test
	void isolatesWhenAChangedPrototypeCannotBeRecovered() throws Exception {
		onEdt(() -> {
			Source source = source();
			AppGeoCeDG app = receiver();
			var service = app.getDocumentInsertion();
			var defaults = app.getKernel().getConstruction().getConstructionDefaults();
			var prototype = defaults.getDefaultGeo(ConstructionDefaults.DEFAULT_CONIC);
			var isolation = new PreG9BR6PlusF3RecoveryTest.Scripted();
			app.getInsertionIsolation().setPresentation(isolation);
			var review = service.beginExpertReview();
			Trial trial = trial(app, review, source);
			var offer = review.offer(source.file(), source.bytes(), trial.fingerprint(), REPORT, () -> true);
			service.setFaults(new GeoCeDGDocumentInsertion.Faults() {
				@Override
				public void afterCommit() {
					prototype.setAnimationStep(0.25);
				}
			});
			var result = service.publishExpert(offer, REPORT);
			assertEquals(SERIOUS_RECOVERY_FAILURE, result.getOutcome(), String.valueOf(result.getFailure()));
			assertTrue(result.getFailure().getMessage().contains("defaults or resources differ"));
			assertEquals(1, isolation.choices);
			return null;
		});
	}

	@Test
	void isolatesWhenNeitherRecoveryRouteCanCertifyTheReceiver() throws Exception {
		onEdt(() -> {
			Source source = source();
			AppGeoCeDG app = receiver();
			File current = PreG9BR6PlusF3Fixtures.save(app, directory, "receiver");
			byte[] original = Files.readAllBytes(current.toPath());
			app.setCurrentFile(current);
			var service = app.getDocumentInsertion();
			var dialog = new PreG9BR6PlusF3RecoveryTest.Scripted();
			var probed = new AtomicBoolean();
			dialog.probe = () -> {
				assertTrue(app.isInsertionIsolated());
				assertFalse(app.getKernel().isUndoActive());
				probed.set(true);
			};
			app.getInsertionIsolation().setPresentation(dialog);
			service.setFaults(new GeoCeDGDocumentInsertion.Faults() {
				@Override
				public void afterCommit() {
					throw new IllegalStateException("injected after expert copy");
				}

				@Override
				public void duringRemoval() {
					throw new IllegalStateException("injected removal failure");
				}

				@Override
				public void beforeReload(int attempt) {
					throw new IllegalStateException("injected reload failure " + attempt);
				}
			});
			var review = service.beginExpertReview();
			Trial trial = trial(app, review, source);
			var offer = review.offer(source.file(), source.bytes(), trial.fingerprint(), REPORT, () -> true);
			assertEquals(SERIOUS_RECOVERY_FAILURE, service.publishExpert(offer, REPORT).getOutcome());
			assertTrue(probed.get());
			assertArrayEquals(original, Files.readAllBytes(current.toPath()));
			return null;
		});
	}

	private Source source() throws Exception {
		AppGeoCeDG source = G9U1TestApp.create(false);
		must(source, "A=(0,0)");
		must(source, "B=(3,0)");
		must(source, "C=(0,3)");
		must(source, "arc=CircularArc(A,B,C)").setLayer(4);
		Path path = directory.resolve("arc.cedg");
		byte[] bytes = GeoCeDGExpertIntegrity.nativeArchive(source);
		Files.write(path, bytes);
		return new Source(path.toFile(), bytes);
	}

	private static AppGeoCeDG receiver() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create(false);
		app.getInsertionIsolation().setPresentation(new PreG9BR6PlusF3RecoveryTest.Scripted());
		app.setUndoActive(true);
		must(app, "A=(1,1)");
		baseline(app);
		return app;
	}

	private Trial trial(AppGeoCeDG app, GeoCeDGDocumentInsertion.ExpertReview review, Source source)
			throws Exception {
		Path nativeInput = Files.createTempDirectory(directory, "trial").resolve("live.cedg");
		review.snapshot().stageReceiver(nativeInput);
		Path context = nativeInput.resolveSibling("context.bin");
		review.snapshot().stageLayerContext(context);
		AppGeoCeDG from = null;
		AppGeoCeDG to = null;
		try {
			from = GeoCeDGExpertIntegrity.loadSource(app, source.bytes());
			to = GeoCeDGExpertIntegrity.loadSource(app, Files.readAllBytes(nativeInput));
			GeoCeDGExpertContext.read(context).context().apply(to);
			var copy = GeoCeDGExpertIntegrity.copy(from, to);
			var arc = copy.copies().get(from.getKernel().lookupLabel("arc"));
			return new Trial(copy.fingerprint(), arc.getLabelSimple(), arc.getLayer());
		} finally {
			GeoCeDGExpertIntegrity.releaseSource(app, from);
			GeoCeDGExpertIntegrity.releaseSource(app, to);
		}
	}

	private record Source(File file, byte[] bytes) {
	}

	private record Trial(String fingerprint, String arcLabel, int layer) {
	}
}
