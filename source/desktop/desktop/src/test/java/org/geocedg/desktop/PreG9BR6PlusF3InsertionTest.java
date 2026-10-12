/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Documents.M;
import static org.geocedg.desktop.PreG9BR6PlusF3Documents.MM;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.insert;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.save;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.algos.AlgoMacro;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.undo.UndoCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-F3 Stage B/C: restricted native insertion R (admission P4, dry run,
 * strict verification, L1), M1/MU-1, the operation-scoped buffer with D1 provenance,
 * one undo step and the kept document identity (FR-F3-6, FR-F3-7).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusF3InsertionTest {

	@TempDir
	Path dir;

	private static int count(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getGeoSetConstructionOrder().size();
	}

	/** A successful insertion: one step on the held prefix, Redo discarded. */
	private static void assertOneUndoStep(AppGeoCeDG app,
			PreG9BR6PlusF3Documents.Observation before) {
		GeoCeDGUndoManager manager = undo(app);
		List<UndoCommand> after = manager.getCommands();
		assertEquals(before.cursor + 1, after.size());
		assertEquals(after.size(), manager.getCursor());
		for (int i = 0; i < before.cursor; i++) {
			assertSame(before.commands.get(i), after.get(i), "held prefix " + i);
		}
		assertFalse(manager.redoPossible());
	}

	@Test
	void f3i01ElementaryInsertionIsOneUndoStepWithIdentityKept() throws Exception {
		AppGeoCeDG target = PreG9BR6PlusF3Documents.target(dir, "t1");
		File source = PreG9BR6PlusF3Documents.elementary(dir, "s1");
		PreG9BR6PlusF3Documents.Observation before = new PreG9BR6PlusF3Documents.Observation(
				target);
		String uuid = target.getUniqueId();
		final String xmlBefore = GeoCeDGDocumentInsertion.Certificate.normalize(target.getXML());
		int n = count(target);
		GeoCeDGDocumentInsertion.Result result = insert(target, source);
		assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED, result.getOutcome(),
				String.valueOf(result.getFailure()));
		assertNull(result.getMessage());
		assertEquals(uuid, target.getUniqueId());
		assertTrue(count(target) > n);
		assertFalse(target.isSaved());
		assertOneUndoStep(target, before);
		// target objects untouched; conflicting labels renamed (L1)
		for (Map.Entry<String, GeoElement> entry : before.certificate.instances.entrySet()) {
			assertSame(entry.getValue(), target.getKernel().lookupLabel(entry.getKey()));
		}
		assertNotNull(target.getKernel().lookupLabel("A_1"));
		assertNotNull(target.getKernel().lookupLabel("a_1"));
		GeoElement m = target.getKernel().lookupLabel("m");
		assertNotNull(m);
		// window buffer and recent list untouched; selection cleared by the paste
		assertTrue(new PreG9BR6PlusF3Documents.Observation(target).recent.equals(before.recent));
		assertSame(before.provenance, ((GeoCeDGCopyPaste) target.getCopyPaste()).getProvenance());
		// one Undo removes the whole insertion
		onEdt(() -> undo(target).undo());
		awaitQuiescent(target);
		assertEquals(xmlBefore,
				GeoCeDGDocumentInsertion.Certificate.normalize(target.getXML()));
		onEdt(() -> undo(target).redo());
		assertNotNull(target.getKernel().lookupLabel("A_1"));
	}

	@Test
	void f3i02LabelRenamingIsDeterministicAndClosed() throws Exception {
		File source = PreG9BR6PlusF3Documents.elementary(dir, "s2");
		List<String> first = new ArrayList<>();
		List<String> second = new ArrayList<>();
		List<List<String>> runs = List.of(first, second);
		for (int run = 0; run < runs.size(); run++) {
			List<String> labels = runs.get(run);
			AppGeoCeDG target = PreG9BR6PlusF3Documents.target(dir, "t2-" + run);
			int n = count(target);
			assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED,
					insert(target, source).getOutcome());
			List<GeoElement> all = new ArrayList<>(
					target.getKernel().getConstruction().getGeoSetConstructionOrder());
			for (GeoElement geo : all.subList(n, all.size())) {
				labels.add(geo.getLabelSimple());
			}
		}
		assertEquals(first, second);
	}

	@Test
	void f3i03RejectionsChangeNothing() throws Exception {
		AppGeoCeDG target = PreG9BR6PlusF3Documents.target(dir, "t3");
		Path bad = dir.resolve("garbage.cedg");
		Files.writeString(bad, "not a zip");
		Path off = dir.resolve("mesh.off");
		Files.writeString(off, "OFF\n0 0 0\n");
		Path txt = dir.resolve("notes.txt");
		Files.writeString(txt, "text");
		File[] sources = {
			dir.resolve("missing.cedg").toFile(), off.toFile(), txt.toFile(), bad.toFile(),
			PreG9BR6PlusF3Documents.empty(dir, "empty"),
			PreG9BR6PlusF3Documents.scripted(dir, "scripted"),
			PreG9BR6PlusF3Documents.single(dir, "three-d", "P3=(1,2,3)"),
			PreG9BR6PlusF3Documents.single(dir, "free-text", "tx=\"hello\""),
			PreG9BR6PlusF3Documents.single(dir, "arc", "A=(0,0)", "B=(1,0)",
					"arc=CircularArc(A,B,(0,1))"),
			PreG9BR6PlusF3Documents.identityWithMacro(dir, "identity-macro")};
		String[] reasons = {"does not exist", "OFF", "only .cedg", "valid", "no objects",
			"scripts", "cannot be inserted", "cannot be inserted", "cannot be inserted",
			"tools"};
		for (int i = 0; i < sources.length; i++) {
			PreG9BR6PlusF3Documents.Observation before =
					new PreG9BR6PlusF3Documents.Observation(target);
			GeoCeDGDocumentInsertion.Result result = insert(target, sources[i]);
			assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
					result.getOutcome(), sources[i].getName());
			assertTrue(result.getMessage().contains(reasons[i]),
					sources[i].getName() + ": " + result.getMessage());
			PreG9BR6PlusF3Documents.Observation after =
					new PreG9BR6PlusF3Documents.Observation(target);
			assertEquals(List.of(), before.serializableDifferences(after), sources[i].getName());
			assertTrue(before.sameInstances(after), sources[i].getName());
		}
	}

	@Test
	void f3i04MacroConflictAndMalformedSourcesAreRejected() throws Exception {
		AppGeoCeDG target = app(MM);
		PreG9BR6PlusF3Fixtures.defineMyMid(target, "Tq", true);
		baseline(target);
		PreG9BR6PlusF3Documents.Observation before = new PreG9BR6PlusF3Documents.Observation(
				target);
		GeoCeDGDocumentInsertion.Result result = insert(target,
				PreG9BR6PlusF3Documents.macro(dir, "mid", false));
		assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
				result.getOutcome());
		assertTrue(result.getMessage().contains("MyMid"), result.getMessage());
		assertEquals(List.of(), before.serializableDifferences(
				new PreG9BR6PlusF3Documents.Observation(target)));
	}

	@Test
	void f3i05MacrosAreTransferredOnceAndReusedByDigest() throws Exception {
		AppGeoCeDG target = app(MM);
		baseline(target);
		File source = PreG9BR6PlusF3Documents.macro(dir, "mid2", false);
		assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED,
				insert(target, source).getOutcome());
		Macro transferred = target.getKernel().getMacro("MyMid");
		assertNotNull(transferred);
		GeoElement n = target.getKernel().lookupLabel("N");
		assertSame(transferred, ((AlgoMacro) n.getParentAlgorithm()).getMacro());
		// repeated insertion: digest-equal reuse, no duplicate tool
		assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED,
				insert(target, source).getOutcome());
		assertEquals(1, GeoCeDGDocumentInsertion.macroNames(target).size());
		GeoElement n1 = target.getKernel().lookupLabel("N_1");
		assertSame(transferred, ((AlgoMacro) n1.getParentAlgorithm()).getMacro());
		// MU-1: after Undo of the second insertion the tool stays registered
		onEdt(() -> undo(target).undo());
		awaitQuiescent(target);
		assertNull(target.getKernel().lookupLabel("N_1"));
		assertNotNull(target.getKernel().getMacro("MyMid"));
	}

	@Test
	void f3i06NestedMacrosAreTransferredTransitively() throws Exception {
		AppGeoCeDG target = app(MM);
		baseline(target);
		assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED,
				insert(target, PreG9BR6PlusF3Documents.nestedMacro(dir, "nested")).getOutcome());
		assertNotNull(target.getKernel().getMacro("MyQuarter"));
		assertNotNull(target.getKernel().getMacro("MyMid"));
		GeoElement r = target.getKernel().lookupLabel("R");
		assertEquals(2, ((org.geogebra.common.kernel.geos.GeoPoint) r).getInhomX(), 1e-9);
		// a target with a different MyMid rejects the nested source before mutation
		AppGeoCeDG other = app(MM);
		PreG9BR6PlusF3Fixtures.defineMyMid(other, "Tq", true);
		baseline(other);
		GeoCeDGDocumentInsertion.Result result = insert(other,
				PreG9BR6PlusF3Documents.nestedMacro(dir, "nested2"));
		assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
				result.getOutcome());
		assertNull(other.getKernel().getMacro("MyQuarter"));
	}

	@Test
	void f3i07LocusV2AndSplineV2ReceiveFreshIdentitiesAndPersist() throws Exception {
		AppGeoCeDG target = PreG9BR6PlusF3Documents.target(dir, "t7");
		Map<GeoElement, PersistentGeoId> idsBefore = GeoCeDGDocumentInsertion.identities(target);
		PreG9BR6PlusF3Documents.Observation before = new PreG9BR6PlusF3Documents.Observation(
				target);
		String uuid = target.getUniqueId();
		GeoCeDGDocumentInsertion.Result result = insert(target,
				PreG9BR6PlusF3Documents.locus(dir, "s7"));
		assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED, result.getOutcome(),
				String.valueOf(result.getFailure()));
		assertEquals(uuid, target.getUniqueId());
		assertOneUndoStep(target, before);
		Map<GeoElement, PersistentGeoId> idsAfter = GeoCeDGDocumentInsertion.identities(target);
		assertTrue(idsAfter.size() > idsBefore.size());
		for (Map.Entry<GeoElement, PersistentGeoId> entry : idsBefore.entrySet()) {
			assertEquals(entry.getValue(), idsAfter.get(entry.getKey()), "target id kept");
		}
		assertEquals(before.certificate.linkedSheet, target.getLinkedIsoABorder(),
				"the target's export link survives a success");
		// save and reopen keep the identities and the document identity
		File saved = dir.resolve("t7-after.cedg").toFile();
		assertTrue(target.saveGeoGebraFile(saved));
		AppGeoCeDG reopened = app(MM);
		assertTrue(reopened.loadFile(saved, false));
		assertEquals(uuid, reopened.getUniqueId());
		assertEquals(idsAfter.size(), GeoCeDGDocumentInsertion.identities(reopened).size());
		assertEquals(GeoCeDGRecoverySnapshot.documentId(Files.readAllBytes(saved.toPath())),
				uuid);
	}

	@Test
	void f3i08NativeDimensionsKeepTheirValueUnderTargetUnits() throws Exception {
		AppGeoCeDG target = app(MM);
		baseline(target);
		File source = PreG9BR6PlusF3Documents.dimensions(dir, "s8");
		AppGeoCeDG helper = app(M);
		assertTrue(helper.loadFile(source, false));
		GeoCeDGDocumentInsertion.Result result = insert(target, source);
		assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED, result.getOutcome(),
				String.valueOf(result.getFailure()));
		for (GeoElement geo : helper.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			GeoElement inserted = target.getKernel().lookupLabel(geo.getLabelSimple());
			assertNotNull(inserted, geo.getLabelSimple());
			assertSame(geo.getParentAlgorithm() == null ? null
					: geo.getParentAlgorithm().getClass(), inserted.getParentAlgorithm() == null
							? null : inserted.getParentAlgorithm().getClass());
			if (geo instanceof org.geogebra.common.kernel.geos.GeoNumeric number) {
				assertEquals(number.getValue(), ((org.geogebra.common.kernel.geos.GeoNumeric)
						inserted).getValue(), 1e-9, "no scaling: " + geo.getLabelSimple());
			}
		}
		assertEquals(MM, target.getDocumentUnits().getState(), "target units unchanged");
		// D1: the inserted document's provenance gives the notice (m into mm)
		assertNotNull(PreG9BR6PlusF3Fixtures.pasteNotice(target));
	}

	@Test
	void f3i09IsoABorderIsInsertedWithoutItsExportLink() throws Exception {
		AppGeoCeDG target = app(MM);
		baseline(target);
		assertNull(target.getLinkedIsoABorder());
		GeoCeDGDocumentInsertion.Result result = insert(target,
				PreG9BR6PlusF3Documents.isoA(dir, "s9"));
		assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED, result.getOutcome(),
				String.valueOf(result.getFailure()));
		assertNotNull(target.getKernel().lookupLabel("Tsheet"));
		assertNull(target.getLinkedIsoABorder(), "session export link never imported");
	}

	@Test
	void f3i10TheCopyBufferIsIndependentOfTheInsertion() throws Exception {
		AppGeoCeDG target = PreG9BR6PlusF3Documents.target(dir, "t10");
		int circles = (int) target.getKernel().getConstruction().getGeoSetConstructionOrder()
				.stream().filter(GeoElement::isGeoConic).count();
		Object provenance = ((GeoCeDGCopyPaste) target.getCopyPaste()).getProvenance();
		assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED,
				insert(target, PreG9BR6PlusF3Documents.single(dir, "s10", "W=(3,3)"))
						.getOutcome());
		assertSame(provenance, ((GeoCeDGCopyPaste) target.getCopyPaste()).getProvenance());
		// Ctrl+V pastes what the user copied before (the circle tc), not the insertion
		onEdt(() -> target.getCopyPaste().pasteFromXML(target, false));
		long after = target.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
				.filter(GeoElement::isGeoConic).count();
		assertEquals(circles + 1, after);
	}

	@Test
	void f3i11LocusV2SourcesNeedTheLocusV2Surface() throws Exception {
		AppGeoCeDG target = app(MM, false);
		baseline(target);
		GeoCeDGDocumentInsertion.Result result = insert(target,
				PreG9BR6PlusF3Documents.locus(dir, "s11"));
		assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
				result.getOutcome());
		assertTrue(result.getMessage().contains("Locus V2"), result.getMessage());
	}

	@Test
	void f3i12InsertionRunsOnTheEventThreadOnly() throws Exception {
		AppGeoCeDG target = app(MM);
		File source = PreG9BR6PlusF3Documents.single(dir, "s12", "W=(3,3)");
		assertThrows(IllegalStateException.class,
				() -> target.getDocumentInsertion().insert(source));
	}

	@Test
	void f3i13UncertainAccountingRejectsBeforeMutation() throws Exception {
		AppGeoCeDG target = app(MM);
		baseline(target);
		File source = PreG9BR6PlusF3Documents.single(dir, "s13", "W=(3,3)");
		PreG9BR6PlusF3Fixtures.setAccounting(target, 0, true);
		PreG9BR6PlusF3Documents.Observation before = new PreG9BR6PlusF3Documents.Observation(
				target);
		GeoCeDGDocumentInsertion.Result degraded = insert(target, source);
		assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
				degraded.getOutcome());
		assertTrue(degraded.getMessage().contains("undo history"), degraded.getMessage());
		// a counted store that never completes: the bounded wait ends in a rejection
		PreG9BR6PlusF3Fixtures.setAccounting(target, 1, false);
		long start = System.nanoTime();
		GeoCeDGDocumentInsertion.Result pending = insert(target, source);
		assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
				pending.getOutcome());
		assertTrue(System.nanoTime() - start < 30_000_000_000L, "bounded");
		assertEquals(List.of(), before.serializableDifferences(
				new PreG9BR6PlusF3Documents.Observation(target)));
		assertNull(target.getKernel().lookupLabel("W"));
		baseline(target);
		assertTrue(undo(target).isQuiescent(), "re-established only by a baseline");
	}

	@Test
	void f3i14ASnapshotFailureRejectsBeforeMutation() throws Exception {
		AppGeoCeDG target = PreG9BR6PlusF3Documents.target(dir, "t14");
		target.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
			@Override
			public void beforeSnapshot() throws Exception {
				throw new java.io.IOException("F3 injected snapshot failure");
			}
		});
		PreG9BR6PlusF3Documents.Observation before = new PreG9BR6PlusF3Documents.Observation(
				target);
		GeoCeDGDocumentInsertion.Result result = insert(target,
				PreG9BR6PlusF3Documents.single(dir, "s14", "W=(3,3)"));
		assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
				result.getOutcome());
		assertEquals(List.of(), before.serializableDifferences(
				new PreG9BR6PlusF3Documents.Observation(target)));
		assertTrue(before.sameInstances(new PreG9BR6PlusF3Documents.Observation(target)));
	}

	@Test
	void f3i15GgbSourcesAndSaveAsKeepTheDocumentIdentity() throws Exception {
		org.geogebra.desktop.main.AppD classic = PreG9BR6PlusF3Fixtures.classic();
		must(classic, "A=(1,1)");
		must(classic, "B=(3,1)");
		must(classic, "s1=Segment(A,B)");
		File ggb = dir.resolve("classic.ggb").toFile();
		assertTrue(classic.saveGeoGebraFile(ggb));
		AppGeoCeDG target = app(MM);
		must(target, "Z=(0,9)");
		File file = save(target, dir, "t15");
		target.setCurrentFile(file);
		baseline(target);
		String uuid = target.getUniqueId();
		assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED, insert(target, ggb).getOutcome());
		assertEquals(uuid, target.getUniqueId());
		File saveAs = dir.resolve("t15-as.cedg").toFile();
		assertTrue(target.saveGeoGebraFile(saveAs));
		assertEquals(uuid, GeoCeDGRecoverySnapshot.documentId(Files.readAllBytes(
				saveAs.toPath())));
		assertNotEquals(-1, GeoCeDGDocumentInsertion.macroNames(target).size());
	}
}
