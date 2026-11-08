/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.save;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.store;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.desktop.GeoCeDGInspectionResult.Phase;
import org.geogebra.common.kernel.geos.ChartStyleGeo;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.plugin.EventType;
import org.geogebra.common.plugin.ScriptType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Capture the native state of a Desktop receiver without saving or editing it. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class GeoCeDGLiveInspectionSnapshotTest {
	@TempDir
	Path directory;

	@Test
	void savedAndDirtyCapturesUseEffectiveReceiverAndPreserveHost() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create(false);
			var point = must(app, "A=(1,2)");
			var registry = point.getConstruction().getSpatialIdentityRegistry();
			var id = registry.allocatePersistentGeoId();
			registry.registerParticipation(point, new GeoIdentityRecord(id,
					ConstructionGeoRedefineProvider.PROVIDER_ID, "POINT",
					ConstructionGeoRedefineProvider.SCHEMA_ID, ConstructionGeoRedefineProvider.SCHEMA_VERSION,
					EditAuthorityMode.CONSTRUCTION_DEFINED, ProjectionBindingRole.NOT_APPLICABLE,
					"VALUE", 1, 0, 0));
			app.getSelectionManager().addSelectedGeo(point);
			var original = save(app, directory, "original").toPath();
			app.setCurrentFile(original.toFile());
			byte[] savedFile = Files.readAllBytes(original);
			var history = GeoCeDGDocumentInsertion.HistoryRecord.of(undo(app));
			try (var saved = GeoCeDGLiveInspectionSnapshot.capture(app)) {
				assertTrue(app.isSaved());
				assertTrue(saved.matchesCurrentState(app));
				must(app, "B=(2,2)");
				var segment = must(app, "s=Segment(A,B)");
				segment.setLayer(3);
				assertTrue(app.getLayerWorkspace().setLayerHidden(3, true));
				app.getDocumentUnits().replace(PreG9BR6PlusF3Documents.MM);
				app.setUnsaved();
				assertFalse(saved.matchesCurrentState(app));
				var before = GeoCeDGDocumentInsertion.Certificate.of(app);
				int mode = app.getMode();
				var selection = List.copyOf(app.getSelectionManager().getSelectedGeos());
				try (var dirty = GeoCeDGLiveInspectionSnapshot.capture(app)) {
					Path staged = directory.resolve("live.cedg");
					dirty.stageReceiver(staged);
					assertThrows(IOException.class, () -> dirty.stageReceiver(staged));
					var result = new GeoCeDGExpertInspection().inspect(original, staged);
					var receiver = result.stages().stream().filter(s -> s.phase() == Phase.RECEIVER)
							.findFirst().orElseThrow();
					assertEquals(dirty.archiveSha256(), receiver.sha256());
					assertEquals(app.getUniqueId(), receiver.documentId());
					assertEquals(PreG9BR6PlusF3Documents.MM, receiver.context().units());
					assertEquals(List.of(3), receiver.context().hiddenLayers());
					assertEquals(id.toExternalForm(), receiver.objects().stream()
							.filter(o -> "A".equals(o.label())).findFirst().orElseThrow().identity());
					var observedSegment = receiver.objects().stream().filter(o -> "s".equals(o.label()))
							.findFirst().orElseThrow();
					assertEquals(2, observedSegment.inputs().size());
					assertTrue(observedSegment.presentation().contains("layer=3"));
					assertTrue(dirty.matchesCurrentState(app));
					app.getLayerWorkspace().setWorkingLayer(2);
					assertFalse(dirty.matchesCurrentState(app));
					app.getLayerWorkspace().setWorkingLayer(0);
				}
				assertArrayEquals(savedFile, Files.readAllBytes(original));
				assertEquals(original, app.getCurrentFile().toPath());
				assertFalse(app.isSaved());
				assertFalse(app.getKernel().isSaving());
				assertFalse(app.getKernel().isAnimationRunning());
				assertEquals(mode, app.getMode());
				assertEquals(selection, app.getSelectionManager().getSelectedGeos());
				assertSame(point, app.getKernel().lookupLabel("A"));
				assertEquals(List.of(), before.serializableDifferences(
						GeoCeDGDocumentInsertion.Certificate.of(app)));
				assertNull(history.mismatch(undo(app), null));
			}
			return null;
		});
	}

	@Test
	void redefineDependenciesAndUndoRedoInvalidateTheObservedState() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create(false);
			app.setUndoActive(true);
			must(app, "A=(0,0)");
			must(app, "B=(1,0)");
			var other = must(app, "C=(0,1)");
			must(app, "s=Segment(A,B)");
			baseline(app);
			try (var first = GeoCeDGLiveInspectionSnapshot.capture(app)) {
				var changed = must(app, "s=Segment(A,C)");
				assertSame(other, changed.getParentAlgorithm().getInput(1));
				assertFalse(first.matchesCurrentState(app));
				store(app);
				try (var afterEdit = GeoCeDGLiveInspectionSnapshot.capture(app)) {
					app.getGgbApi().undo();
					assertEquals("B", app.getKernel().lookupLabel("s").getParentAlgorithm()
							.getInput(1).getLabelSimple());
					assertFalse(afterEdit.matchesCurrentState(app));
					try (var afterUndo = GeoCeDGLiveInspectionSnapshot.capture(app)) {
						app.getGgbApi().redo();
						assertEquals("C", app.getKernel().lookupLabel("s").getParentAlgorithm()
								.getInput(1).getLabelSimple());
						assertFalse(afterUndo.matchesCurrentState(app));
					}
				}
			}
			return null;
		});
	}

	@Test
	void detectsScalarLayerChangeOmittedByNativeWriter() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create(false);
			app.getLayerWorkspace().setWorkingLayer(4);
			must(app, "A=(0,0)");
			var scalar = must(app, "n=2");
			assertEquals(4, scalar.getLayer());
			String fragment = scalar.getXML();
			String nativeXml = app.getXML();
			int maxLayer = app.getMaxLayerUsed();
			try (var snapshot = GeoCeDGLiveInspectionSnapshot.capture(app)) {
				scalar.setLayer(0);
				assertEquals(fragment, scalar.getXML());
				assertFalse(nativeXml.equals(app.getXML()));
				assertEquals(maxLayer, app.getMaxLayerUsed());
				assertFalse(snapshot.matchesCurrentState(app));
			}
			return null;
		});
	}

	@Test
	void rejectsFailedOrReentrantCaptureAndReleasesTheWitness() throws Exception {
		AppGeoCeDG app = onEdt(() -> G9U1TestApp.create(false));
		assertThrows(IllegalStateException.class, () -> GeoCeDGLiveInspectionSnapshot.capture(app));
		onEdt(() -> {
			app.getKernel().setWantAnimationStarted(true);
			try {
				assertThrows(IOException.class, () -> GeoCeDGLiveInspectionSnapshot.capture(app));
			} finally {
				app.getKernel().setWantAnimationStarted(false);
			}
			assertThrows(IOException.class, () -> GeoCeDGLiveInspectionSnapshot.capture(app, out -> {
				out.write(0);
				throw new IOException("injected write failure");
			}));
			IOException changed = assertThrows(IOException.class,
					() -> GeoCeDGLiveInspectionSnapshot.capture(app, out -> {
						app.getXMLio().writeGeoGebraFile(out, false);
						must(app, "late=(1,2)");
					}));
			assertEquals("Receiver changed during capture", changed.getMessage());
			assertNotNull(app.getKernel().lookupLabel("late"));
			var snapshot = GeoCeDGLiveInspectionSnapshot.capture(app);
			snapshot.close();
			assertFalse(snapshot.matchesCurrentState(app));
			assertThrows(IOException.class, () -> snapshot.stageReceiver(directory.resolve("closed.cedg")));
			assertFalse(Files.exists(directory.resolve("closed.cedg")));
			return null;
		});
	}

	@Test
	void preservesNativeScriptBytesWithoutDispatchingAndRejectsUnsafeWriterResources()
			throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create(false);
			var n = (GeoNumeric) must(app, "n=2");
			n.setScript(app.createScript(ScriptType.GGBSCRIPT, "SetValue(n,9)", false),
					EventType.UPDATE);
			try (var snapshot = GeoCeDGLiveInspectionSnapshot.capture(app)) {
				Path staged = directory.resolve("script.cedg");
				snapshot.stageReceiver(staged);
				var passive = GeoCeDGInspectionArchive.capture(staged);
				assertFalse(passive.loadable());
				assertTrue(passive.findings().stream()
						.anyMatch(finding -> finding.code() == GeoCeDGInspectionArchive.Code.SCRIPT));
				assertEquals(2, n.getDouble());
			}
			var chart = must(app, "b=BarChart({1,2},{2,3})");
			((ChartStyleGeo) chart).getStyle().setBarImage("not-loaded.png", 1);
			String imageBefore = chart.getImageFileName();
			assertThrows(IOException.class, () -> GeoCeDGLiveInspectionSnapshot.capture(app,
					out -> { throw new AssertionError("Writer must not run for chart images"); }));
			assertEquals(imageBefore, chart.getImageFileName());
			return null;
		});
	}
}
