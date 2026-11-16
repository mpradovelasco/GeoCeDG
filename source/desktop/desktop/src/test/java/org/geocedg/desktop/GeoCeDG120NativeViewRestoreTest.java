/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Dimension;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.geocedg.common.kernel.locus.NativeTransactionalState2D;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.main.undo.FileAppState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** R10: retain the real target archive before testing exact native viewport Undo. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class GeoCeDG120NativeViewRestoreTest {
	@TempDir Path directory;

	private record Frame(int width, int height, double x, double y, double xs, double ys) {
		static Frame of(EuclidianView view) {
			return new Frame(view.getWidth(), view.getHeight(), view.getXZero(),
					view.getYZero(), view.getXscale(), view.getYscale());
		}
		Frame reciprocalRoundTrip() {
			double xmin = -x * (1 / xs), xmax = (width - x) * (1 / xs);
			double ymin = (y - height) * (1 / ys), ymax = y * (1 / ys);
			double nextXs = width / (xmax - xmin), nextYs = height / (ymax - ymin);
			return new Frame(width, height, -nextXs * xmin, nextYs * ymax, nextXs, nextYs);
		}
		long[] bits() {
			return new long[] {Double.doubleToLongBits(x), Double.doubleToLongBits(y),
					Double.doubleToLongBits(xs), Double.doubleToLongBits(ys)};
		}
		void assertExact(EuclidianView view) {
			assertEquals(width, view.getWidth()); assertEquals(height, view.getHeight());
			assertArrayEquals(bits(), of(view).bits(), "Viewport origin/scales must remain bit exact");
		}
	}

	@Test void recordedComputerUseTupleExposesTheNativeReciprocalRoundTrip() {
		var recorded = new Frame(1218, 632, -775.0909090909093, 142.07142857142856,
				55.36363636363638, 23.678571428571427);
		var reloaded = recorded.reciprocalRoundTrip();
		assertEquals(Double.doubleToLongBits(-775.0909090909092),
				Double.doubleToLongBits(reloaded.x));
		assertEquals(Double.doubleToLongBits(55.36363636363637),
				Double.doubleToLongBits(reloaded.xs));
		assertFalse(Arrays.equals(recorded.bits(), reloaded.bits()));
	}

	@Test void nativeZoomFrameSurvivesOwnedUndoRedoWithoutAnXmlOrCornerDrift() throws Exception {
		AppGeoCeDG app = onEdt(() -> { return G9U1TestApp.create(); });
		onEdt(() -> {
			var view = (EuclidianViewD) app.getEuclidianView1();
			view.setSize(new Dimension(1218, 632));
			view.updateSize();
			must(app, "Center=(0,0)"); must(app, "NativeCircle=Circle(Center,2)");
			must(app, "NativePlace=Point(NativeCircle)");
			must(app, "ViewCorner=Corner(1)"); must(app, "CornerChild=ViewCorner+(1,2)");
			view.setRealWorldCoordSystem(14, 36, -20, 6);
			return null;
		});
		awaitQuiescent(app);
		Frame frame = onEdt(() -> {
			EuclidianView view = app.getEuclidianView1();
			assertEquals(1218, view.getWidth()); assertEquals(632, view.getHeight());
			assertTrue(view.isZoomable());
			// A bounded deterministic sequence of native zoom operations selects a
			// non-fixed tuple; no product state or XML is edited to manufacture it.
			for (double factor : new double[] {1.1, 1.2, 1.01, 1.3, 1.07}) {
				view.zoomAroundCenter(factor);
				Frame current = Frame.of(view);
				if (!Arrays.equals(current.bits(), current.reciprocalRoundTrip().bits())) {
					return current;
				}
			}
			throw new AssertionError("Native zoom must exercise a non-fixed binary64 tuple");
		});
		Path evidence = Path.of(System.getProperty("geocedg.120.view.evidenceDir",
				directory.toString()));
		Files.createDirectories(evidence);
		String before = onEdt(() -> {
			assertNull(NativeTransactionalState2D.unsupportedReason(app.getKernel().getConstruction()));
			assertNotNull(app.getKernel().getConstruction().captureNativeUndoSnapshot(),
					"The fixture must use the owned MEMORY history path");
			baseline(app);
			var commands = undo(app).getCommands();
			var target = assertInstanceOf(FileAppState.class,
					commands.get(commands.size() - 1).getAppState());
			assertInstanceOf(GeoCeDGUndoManager.NativeFileAppState.class, target);
			Files.copy(target.getFile().toPath(), evidence.resolve("target-before-undo.ggb"),
					StandardCopyOption.REPLACE_EXISTING);
			String xml = app.getXML();
			Files.writeString(evidence.resolve("target-before-undo.xml"), xml, StandardCharsets.UTF_8);
			Files.writeString(evidence.resolve("target-frame.txt"), frame + "\nbits="
					+ Arrays.toString(frame.bits()) + "\nroundTrip=" + frame.reciprocalRoundTrip(),
					StandardCharsets.UTF_8);
			System.out.println("R10_VIEW_TARGET_ARCHIVED " + evidence.toAbsolutePath());
			return xml;
		});
		onEdt(() -> { must(app, "UndoMarker=(7,8)"); app.storeUndoInfo(); return null; });
		awaitQuiescent(app);
		for (int cycle = 0; cycle < 3; cycle++) {
			onEdt(() -> {
				assertTrue(undo(app).undoPossible());
				assertDoesNotThrow(() -> app.getKernel().undo(),
						"The archived native viewport target must load exactly");
				assertNull(app.getKernel().lookupLabel("UndoMarker"));
				frame.assertExact(app.getEuclidianView1());
				assertEquals(before, app.getXML());
				assertEquals(1, app.getKernel().getConstruction().getLastNativeUndoRestoreMetrics().dagCalls);
				assertTrue(undo(app).redoPossible());
				app.getKernel().redo();
				assertNotNull(app.getKernel().lookupLabel("UndoMarker"));
				frame.assertExact(app.getEuclidianView1());
				return null;
			});
			awaitQuiescent(app);
		}
	}
}
