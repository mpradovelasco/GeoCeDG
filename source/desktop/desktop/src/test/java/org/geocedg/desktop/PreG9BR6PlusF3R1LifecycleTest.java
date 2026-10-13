/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Documents.MM;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.insert;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.save;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.menubar.LoadFileListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-F3-R1 corrective regression (author smoke SF-F3-1 to SF-F3-5): hidden
 * insertion helpers must not stay registered as application windows (they redirected
 * Open Recent to a hidden window, added "(1)" to the title and kept the process alive),
 * and a Locus V2 construction that depends on the coordinate axes is admitted.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusF3R1LifecycleTest {

	@TempDir
	Path dir;

	/** The author's CircleM construction: mm, a line and a circle on A, B, A_1, B_1. */
	static File circleM(Path dir) {
		AppGeoCeDG s = app(MM);
		for (String c : new String[] {"A=(0,0)", "B=(2,1)", "f=Line(A,B)", "A_{1}=(0.79,1.05)",
				"B_{1}=(1.2,1.9)", "c=Circle(A_{1},B_{1})"}) {
			must(s, c);
		}
		return save(s, dir, "CircleM");
	}

	/**
	 * The author's LocusV2 construction (no units): a point on a circle, a line through
	 * it parallel to the x-axis, its intersection with the y-axis, the midpoint and the
	 * Locus V2 of that midpoint driven by the point on the circle.
	 */
	static File locusV2(Path dir) {
		AppGeoCeDG s = app(org.geocedg.common.kernel.units.UnitState.EMPTY);
		for (String c : new String[] {"A=(1.99,0.73)", "B=(4,1)", "c=Circle(A,B)", "C=Point(c)",
				"f=Line(C,xAxis)", "D=Intersect(f,yAxis)", "E=Midpoint(D,C)",
				"a=LocusV2(E,C)"}) {
			must(s, c);
		}
		return save(s, dir, "LocusV2");
	}

	/** Registered application windows whose application is an insertion helper. */
	private static int helperFrames() {
		int n = 0;
		for (GeoGebraFrame frame : new ArrayList<>(GeoGebraFrame.getInstances())) {
			if (frame.getApplication() instanceof AppGeoCeDG app && app.isInsertionHelper()) {
				n++;
			}
		}
		return n;
	}

	private static AppGeoCeDG opened(File file) throws Exception {
		AppGeoCeDG app = app(MM);
		assertTrue(onEdt(() -> app.loadFile(file, false)));
		baseline(app);
		return app;
	}

	private static void openRecent(AppGeoCeDG app, File file) throws Exception {
		onEdt(() -> new LoadFileListener(app, file).actionPerformed(null));
	}

	@Test
	void f3r1l1ARejectedInsertionLeavesNoHelperWindowAndOpenRecentLoads() throws Exception {
		File circle = circleM(dir);
		File scripted = PreG9BR6PlusF3Documents.scripted(dir, "scripted");
		AppGeoCeDG target = opened(circle);
		int frames = GeoGebraFrame.getInstanceCount();
		GeoCeDGDocumentInsertion.Result result = insert(target, scripted);
		assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
				result.getOutcome());
		assertEquals(0, helperFrames(), "no hidden helper stays registered as a window");
		assertEquals(frames, GeoGebraFrame.getInstanceCount());
		assertNull(GeoGebraFrame.getInstanceWithFile(scripted),
				"no window claims the inserted source");
		// Open Recent of the rejected source loads it into the user's window
		openRecent(target, scripted);
		assertEquals(scripted.getCanonicalFile(), target.getCurrentFile().getCanonicalFile());
	}

	@Test
	void f3r1l2ASuccessfulInsertionLeavesNoHelperWindowAndOpenRecentLoads() throws Exception {
		File circle = circleM(dir);
		File locus = locusV2(dir);
		AppGeoCeDG target = opened(locus);
		int frames = GeoGebraFrame.getInstanceCount();
		int objects = target.getKernel().getConstruction().getGeoSetConstructionOrder().size();
		GeoElement locusA = target.getKernel().lookupLabel("A");
		GeoCeDGDocumentInsertion.Result result = insert(target, circle);
		assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED, result.getOutcome(),
				String.valueOf(result.getFailure()));
		assertEquals(objects + 6,
				target.getKernel().getConstruction().getGeoSetConstructionOrder().size());
		assertSame(locusA, target.getKernel().lookupLabel("A"), "conflicts renamed, A kept");
		assertEquals(0, helperFrames());
		assertEquals(frames, GeoGebraFrame.getInstanceCount());
		assertNull(GeoGebraFrame.getInstanceWithFile(circle));
		target.setSaved();
		openRecent(target, circle);
		assertEquals(circle.getCanonicalFile(), target.getCurrentFile().getCanonicalFile());
		assertNotNull(target.getKernel().lookupLabel("c"));
		assertNull(target.getKernel().lookupLabel("a"), "the reopened document is CircleM");
	}

	@Test
	void f3r1l3RepeatedInsertionsAndStyleApplicationsDoNotAccumulateWindows()
			throws Exception {
		File circle = circleM(dir);
		File scripted = PreG9BR6PlusF3Documents.scripted(dir, "scripted");
		File single = PreG9BR6PlusF3Documents.single(dir, "w", "W=(3,3)");
		AppGeoCeDG target = opened(circle);
		int frames = GeoGebraFrame.getInstanceCount();
		for (int i = 0; i < 3; i++) {
			insert(target, scripted);
			insert(target, single);
			onEdt(() -> target.getStyleApplication().apply(circle));
		}
		assertEquals(0, helperFrames());
		assertEquals(frames, GeoGebraFrame.getInstanceCount());
	}

	@Test
	void f3r1r3NewAndOpenKeepTheCurrentDocumentAndAFreshTitleCount() throws Exception {
		File circle = circleM(dir);
		File locus = locusV2(dir);
		AppGeoCeDG target = opened(circle);
		int frames = GeoGebraFrame.getInstanceCount();
		insert(target, locus);
		onEdt(() -> {
			target.setSaved();
			target.fileNew();
		});
		assertNull(target.getCurrentFile());
		assertEquals(frames, GeoGebraFrame.getInstanceCount(),
				"no extra registered window behind a \"(1)\" title");
		openRecent(target, locus);
		assertEquals(locus.getCanonicalFile(), target.getCurrentFile().getCanonicalFile());
		openRecent(target, circle);
		assertEquals(circle.getCanonicalFile(), target.getCurrentFile().getCanonicalFile());
		File saved = dir.resolve("resaved.cedg").toFile();
		assertTrue(target.saveGeoGebraFile(saved));
		openRecent(target, locus);
		openRecent(target, saved);
		assertEquals(saved.getCanonicalFile(), target.getCurrentFile().getCanonicalFile());
	}

	@Test
	void f3r1a1AnAxisDependentLocusV2IsAdmittedAndBoundToTheTargetAxes() throws Exception {
		File circle = circleM(dir);
		File locus = locusV2(dir);
		AppGeoCeDG target = opened(circle);
		String uuid = target.getUniqueId();
		int cursor = undo(target).getCursor();
		GeoCeDGDocumentInsertion.Result result = insert(target, locus);
		// original F3 candidate: REJECTED_BEFORE_MUTATION, "the trial insertion failed
		// (closure f)": the built-in axes were taken for target objects
		assertEquals(GeoCeDGDocumentInsertion.Outcome.INSERTED, result.getOutcome(),
				result.getMessage() + " " + result.getFailure());
		assertEquals(uuid, target.getUniqueId());
		assertEquals(cursor + 1, undo(target).getCursor(), "one undo step");
		AlgoElement algo = null;
		for (GeoElement geo : target.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (geo.getParentAlgorithm() != null && "AlgoLinePointLine".equals(
					geo.getParentAlgorithm().getClass().getSimpleName())) {
				algo = geo.getParentAlgorithm();
			}
		}
		assertNotNull(algo, "the inserted line through C parallel to the x-axis");
		assertNotNull(target.getKernel().lookupLabel("f"), "the target's own line f kept");
		assertSame(target.getKernel().getConstruction().getXAxis(), algo.getInput(1),
				"bound to the target's x-axis, a construction constant");
		GeoElement locusCurve = target.getKernel().lookupLabel("a");
		assertNotNull(locusCurve);
		assertTrue(target.getKernel().getConstruction().getSpatialIdentityRegistry()
				.isParticipating(locusCurve));
		// save, reopen: the Locus V2 and its axis dependency persist
		File saved = dir.resolve("with-locus.cedg").toFile();
		assertTrue(target.saveGeoGebraFile(saved));
		AppGeoCeDG reopened = app(MM);
		assertTrue(reopened.loadFile(saved, false));
		assertEquals(uuid, reopened.getUniqueId());
		assertNotNull(reopened.getKernel().lookupLabel("a"));
		assertSame(reopened.getKernel().getConstruction().getYAxis(),
				reopened.getKernel().lookupLabel("D").getParentAlgorithm().getInput(1));
		// one Undo removes the whole insertion
		onEdt(() -> undo(target).undo());
		PreG9BR6PlusF3Fixtures.awaitQuiescent(target);
		assertNull(target.getKernel().lookupLabel("a"));
	}

	@Test
	void f3r1a2TheClosureCheckStillRejectsATrueExternalDependency() throws Exception {
		AppGeoCeDG helper = app(MM);
		must(helper, "Q=(1,1)");
		must(helper, "L=Line(Q,xAxis)");
		List<GeoElement> source = new ArrayList<>(
				helper.getKernel().getConstruction().getGeoSetConstructionOrder());
		AppGeoCeDG target = app(MM);
		must(target, "gl=Line((0,0),(1,2))");
		java.util.Set<GeoElement> before = GeoCeDGDocumentInsertion.identitySet(target);
		var ids = GeoCeDGDocumentInsertion.identities(target);
		must(target, "Q2=(1,1)");
		must(target, "L2=Line(Q2,gl)");
		assertEquals("closure L", GeoCeDGDocumentInsertion.verify(source,
				GeoCeDGDocumentInsertion.createdSince(target, before), target, helper, ids));
		AppGeoCeDG other = app(MM);
		java.util.Set<GeoElement> start = GeoCeDGDocumentInsertion.identitySet(other);
		must(other, "Q3=(1,1)");
		must(other, "L3=Line(Q3,yAxis)");
		assertEquals("closure L", GeoCeDGDocumentInsertion.verify(source,
				GeoCeDGDocumentInsertion.createdSince(other, start), other, helper,
				GeoCeDGDocumentInsertion.identities(other)), "x-axis must map to x-axis");
		AppGeoCeDG good = app(MM);
		java.util.Set<GeoElement> empty = GeoCeDGDocumentInsertion.identitySet(good);
		must(good, "Q4=(1,1)");
		must(good, "L4=Line(Q4,xAxis)");
		assertNull(GeoCeDGDocumentInsertion.verify(source,
				GeoCeDGDocumentInsertion.createdSince(good, empty), good, helper,
				GeoCeDGDocumentInsertion.identities(good)));
	}

	@Test
	void f3r1x1RecoveryPathsLeaveNoHelperWindowAndOpenRecentWorks() throws Exception {
		File circle = circleM(dir);
		File locus = locusV2(dir);
		AppGeoCeDG target = opened(circle);
		int frames = GeoGebraFrame.getInstanceCount();
		target.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
			@Override
			public void afterCommit() {
				throw new IllegalStateException("F3-R1 injected post-commit failure");
			}
		});
		GeoCeDGDocumentInsertion.Result result = insert(target, locus);
		assertEquals(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET,
				result.getOutcome(), String.valueOf(result.getFailure()));
		assertEquals(0, helperFrames());
		assertEquals(frames, GeoGebraFrame.getInstanceCount());
		assertFalse(target.isInsertionIsolated());
		target.getDocumentInsertion().setFaults(null);
		openRecent(target, locus);
		assertEquals(locus.getCanonicalFile(), target.getCurrentFile().getCanonicalFile());
		Path garbage = dir.resolve("broken.cedg");
		Files.writeString(garbage, "not a zip");
		insert(target, garbage.toFile());
		assertEquals(0, helperFrames());
		// a macro conflict rejected by admission
		File mid = PreG9BR6PlusF3Documents.macro(dir, "mid", false);
		final int framesNow = GeoGebraFrame.getInstanceCount();
		PreG9BR6PlusF3Fixtures.defineMyMid(target, "Tq", true);
		assertEquals(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION,
				insert(target, mid).getOutcome());
		assertEquals(0, helperFrames());
		// a serious failure: isolation, then the Reopen exit
		File reopenable = save(target, dir, "reopenable");
		target.setCurrentFile(reopenable);
		baseline(target);
		target.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
			@Override
			public void afterCommit() {
				throw new IllegalStateException("F3-R1 injected post-commit failure");
			}

			@Override
			public void beforeReload(int attempt) {
				throw new IllegalStateException("F3-R1 injected reload failure");
			}
		});
		PreG9BR6PlusF3RecoveryTest.Scripted dialog = new PreG9BR6PlusF3RecoveryTest.Scripted();
		dialog.exits.add(GeoCeDGInsertionIsolation.Exit.REOPEN);
		target.getInsertionIsolation().setPresentation(dialog);
		assertEquals(GeoCeDGDocumentInsertion.Outcome.SERIOUS_RECOVERY_FAILURE,
				insert(target, locus).getOutcome());
		assertFalse(target.isInsertionIsolated());
		assertEquals(0, helperFrames(), "the helper is released after the isolation exit");
		assertEquals(framesNow, GeoGebraFrame.getInstanceCount());
		target.getDocumentInsertion().setFaults(null);
		openRecent(target, circle);
		assertEquals(circle.getCanonicalFile(), target.getCurrentFile().getCanonicalFile());
	}
}
