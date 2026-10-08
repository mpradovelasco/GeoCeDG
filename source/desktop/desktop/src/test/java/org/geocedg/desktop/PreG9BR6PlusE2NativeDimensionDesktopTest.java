/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import javax.swing.SwingUtilities;

import org.geocedg.common.euclidian.draw.DrawDimensionText;
import org.geocedg.common.export.GeometryExportModel;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.common.kernel.dimension.AlgoAlignedDimension;
import org.geocedg.common.kernel.dimension.AlgoLinearDimension;
import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geocedg.desktop.export.DrawingScale;
import org.geocedg.desktop.resources.GeoCeDGToolImageResource;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.euclidian.DrawableND;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.Hits;
import org.geogebra.common.euclidian.draw.DrawText;
import org.geogebra.common.euclidian.event.AbstractEvent;
import org.geogebra.common.export.pstricks.ExportFrameMinimal;
import org.geogebra.common.export.pstricks.GeoGebraExport;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoMacro;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App.ExportType;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.awt.GBufferedImageD;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.euclidian.event.MouseEventD;
import org.geogebra.desktop.io.MyXMLioD;
import org.geogebra.desktop.main.GeoGebraPreferencesD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.desktop.util.LoggerD;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-E2 Desktop contract of the native dimensions: tools and capture,
 * the offset drag, the aligned value (drawing, hit testing, Corner, picture DPI),
 * LaTeX and DXF export, persistence and copy, profile and icons, EN/ES names and the
 * coexistence with the legacy macros. Every scenario runs in a fresh JVM of the test
 * classpath, so the complete Desktop suite retains none of its applications.
 */
class PreG9BR6PlusE2NativeDimensionDesktopTest {

	/** Scenario prefix that executes GGBScript matrix rows of one test method. */
	static final String MATRIX_ROWS = "matrixRows-";
	private static final int SCENARIO_TIMEOUT_SECONDS = 300;
	private static final double LINE_HALF = 2.5;

	@TempDir
	Path temporary;

	@Test
	void profileActionsModesTextAndIcons() throws Exception {
		isolated(temporary, "profile");
	}

	@Test
	void alignedToolPlacementAndCapture() throws Exception {
		isolated(temporary, "alignedTool");
	}

	@Test
	void linearToolWithLineAndVector() throws Exception {
		isolated(temporary, "linearTool");
	}

	@Test
	void dragEditsOnlyTheOffset() throws Exception {
		isolated(temporary, "drag");
	}

	@Test
	void alignedValueAngleSweep() throws Exception {
		isolated(temporary, "sweep");
	}

	@Test
	void alignedValueRobustness() throws Exception {
		isolated(temporary, "robustness");
	}

	@Test
	void pictureExportAtSeveralResolutions() throws Exception {
		isolated(temporary, "picture");
	}

	@Test
	void hitTestingFollowsTheRotatedValue() throws Exception {
		isolated(temporary, "hit");
	}

	@Test
	void cornerUsesTheDisplayedBounds() throws Exception {
		isolated(temporary, "corner");
	}

	@Test
	void latexExportsArrowsRotationAndCentring() throws Exception {
		isolated(temporary, "latex");
		LatexCompilation.compileIfAvailable(temporary);
	}

	@Test
	void dxfExportsOnlyTheSegmentSubset() throws Exception {
		isolated(temporary, "dxf");
	}

	@Test
	void persistenceCopyAndUndo() throws Exception {
		isolated(temporary, "persistence");
	}

	@Test
	void legacyMacrosCoexist() throws Exception {
		isolated(temporary, "legacy");
	}

	@Test
	void englishAndSpanishNamesAndScripts() throws Exception {
		isolated(temporary, "names");
	}

	/** Runs one scenario in a fresh JVM of the test classpath. */
	static void isolated(Path directory, String scenario) throws Exception {
		Path out = directory.resolve(scenario + ".txt");
		Path console = directory.resolve(scenario + ".console.txt");
		List<String> command = new ArrayList<>(List.of(
				ProcessHandle.current().info().command().orElseThrow(),
				"-cp", System.getProperty("java.class.path"),
				"--add-exports", "java.base/java.lang=ALL-UNNAMED",
				"--add-exports", "java.desktop/sun.awt=ALL-UNNAMED",
				"--add-exports", "java.desktop/sun.java2d=ALL-UNNAMED",
				Scenario.class.getName(), scenario, directory.toString(), out.toString()));
		Process process = new ProcessBuilder(command).redirectErrorStream(true)
				.redirectOutput(console.toFile()).start();
		boolean ended = process.waitFor(SCENARIO_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		if (!ended) {
			process.destroyForcibly();
		}
		String record = Files.exists(out) ? Files.readString(out) : "(no record) "
				+ (Files.exists(console) ? Files.readString(console) : "");
		assertTrue(ended, scenario + " ended within " + SCENARIO_TIMEOUT_SECONDS + " s: "
				+ record);
		assertTrue(record.startsWith("SCENARIO OK " + scenario), record);
		assertEquals(0, process.exitValue(), record);
		System.out.println(record);
	}

	/** Main class of one scenario JVM. */
	public static final class Scenario {
		private Scenario() {
		}

		/**
		 * @param args scenario, working directory, result file
		 */
		public static void main(String[] args) {
			String result;
			StringBuilder notes = new StringBuilder();
			try {
				AwtFactory.setPrototypeIfNull(new AwtFactoryD());
				Log.setLogger(new LoggerD());
				Path dir = Paths.get(args[1]);
				GeoGebraPreferencesD.setPropertyFileName(
						dir.resolve("e2-preferences.properties").toString());
				GeoCeDGUnitPreferences.useStoreForTesting(
						new PreG9BR6PlusD1DocumentUnitsTest.MemoryStore());
				Scenarios scenarios = new Scenarios(dir, notes);
				String scenario = args[0].startsWith(MATRIX_ROWS) ? MATRIX_ROWS : args[0];
				switch (scenario) {
				case MATRIX_ROWS:
					PreG9BR6CapabilityMatrixTest.executeRows(
							args[0].substring(MATRIX_ROWS.length()), dir);
					break;
				case "profile":
					scenarios.profile();
					break;
				case "alignedTool":
					scenarios.alignedTool();
					break;
				case "linearTool":
					scenarios.linearTool();
					break;
				case "drag":
					scenarios.drag();
					break;
				case "sweep":
					scenarios.sweep();
					break;
				case "robustness":
					scenarios.robustness();
					break;
				case "picture":
					scenarios.picture();
					break;
				case "hit":
					scenarios.hit();
					break;
				case "corner":
					scenarios.corner();
					break;
				case "latex":
					scenarios.latex();
					break;
				case "dxf":
					scenarios.dxf();
					break;
				case "persistence":
					scenarios.persistence();
					break;
				case "legacy":
					scenarios.legacy();
					break;
				case "names":
					scenarios.names();
					break;
				default:
					throw new IllegalArgumentException(args[0]);
				}
				result = "SCENARIO OK " + args[0] + "\n" + notes;
			} catch (Throwable failure) {
				StringBuilder text = new StringBuilder("SCENARIO FAILED " + args[0] + ": "
						+ failure + "\n" + notes);
				for (StackTraceElement element : failure.getStackTrace()) {
					text.append("\n    at ").append(element);
				}
				result = text.toString();
			}
			try {
				Files.write(Paths.get(args[2]), result.getBytes(StandardCharsets.UTF_8));
			} catch (Exception ignored) {
				// the parent reports the missing record
			}
			Runtime.getRuntime().halt(0);
		}
	}

	/** The scenarios; each runs alone in its JVM. */
	static final class Scenarios {
		private final Path dir;
		private final StringBuilder notes;

		Scenarios(Path dir, StringBuilder notes) {
			this.dir = dir;
			this.notes = notes;
		}

		private void note(String line) {
			notes.append(line).append('\n');
		}

		private static AppGeoCeDG app() throws Exception {
			AppGeoCeDG app = G9U1TestApp.create();
			sized(app, 800, 600, 50, 50);
			return app;
		}

		private static void sized(AppGeoCeDG app, int w, int h, double xs, double ys)
				throws Exception {
			EuclidianView view = app.getEuclidianView1();
			SwingUtilities.invokeAndWait(() -> {
				((EuclidianViewD) view).getJPanel().setSize(w, h);
				view.updateSize();
				view.setCoordSystem(w / 2.0, h / 2.0, xs, ys);
				view.setShowAxes(false, false);
				view.showGrid(false);
			});
		}

		private static GeoElement eval(AppGeoCeDG app, String command) {
			return G9U1TestApp.eval(app, command);
		}

		private static GeoElement[] dim(AppGeoCeDG app, String command) {
			GeoElementND[] result = app.getKernel().getAlgebraProcessor()
					.processAlgebraCommand(command, false);
			assertNotNull(result, command);
			GeoElement[] out = new GeoElement[result.length];
			for (int i = 0; i < result.length; i++) {
				out[i] = result[i].toGeoElement();
			}
			return out;
		}

		private static GeoCeDGEuclidianController controller(AppGeoCeDG app) {
			return (GeoCeDGEuclidianController) app.getEuclidianView1()
					.getEuclidianController();
		}

		private static Hits hit(GeoElement geo) {
			Hits hits = new Hits();
			if (geo != null) {
				hits.add(geo);
			}
			return hits;
		}

		private static boolean click(GeoCeDGEuclidianController controller, GeoElement geo,
				AtomicInteger commits) {
			return controller.processMode(hit(geo), false, false, changed -> {
				if (changed) {
					commits.incrementAndGet();
					controller.getApplication().storeUndoInfoAndStateForModeStarting(true);
				}
			});
		}

		private static AbstractEvent event(AppGeoCeDG app, double x, double y) {
			int sx = app.getEuclidianView1().toScreenCoordX(x);
			int sy = app.getEuclidianView1().toScreenCoordY(y);
			return MouseEventD.wrapEvent(new MouseEvent(app.getEuclidianView1().getJPanel(),
					MouseEvent.MOUSE_PRESSED, 1, 0, sx, sy, 1, false, MouseEvent.BUTTON1));
		}

		private static void await(BooleanSupplier condition) throws Exception {
			long deadline = System.nanoTime() + 5_000_000_000L;
			while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
				Thread.sleep(10);
			}
			assertTrue(condition.getAsBoolean());
		}

		private static AlgoNativeDimension onlyDimension(AppGeoCeDG app) {
			AlgoNativeDimension found = null;
			for (GeoElement geo : app.getKernel().getConstruction()
					.getGeoSetConstructionOrder()) {
				AlgoNativeDimension owner = AlgoNativeDimension.ownerOf(geo);
				if (owner != null) {
					assertTrue(found == null || found == owner, "one dimension");
					found = owner;
				}
			}
			return found;
		}

		// ---------------------------------------------------------------- profile

		void profile() throws Exception {
			AppGeoCeDG app = app();
			GeoCeDGProfile.ActionDefinition aligned =
					GeoCeDGProfile.getAction("measure.aligned-dimension");
			GeoCeDGProfile.ActionDefinition linear =
					GeoCeDGProfile.getAction("measure.linear-dimension");
			assertEquals("upstream-mode", aligned.kind());
			assertEquals(EuclidianConstants.MODE_ALIGNED_DIMENSION,
					aligned.mode().intValue());
			assertEquals(142, EuclidianConstants.MODE_ALIGNED_DIMENSION);
			assertEquals(EuclidianConstants.MODE_LINEAR_DIMENSION, linear.mode().intValue());
			assertEquals(143, EuclidianConstants.MODE_LINEAR_DIMENSION);
			app.setLocale(Locale.ENGLISH);
			assertEquals("Aligned Dimension", app.getToolName(142));
			assertEquals("Linear Dimension", app.getToolName(143));
			assertTrue(app.getToolHelp(142).startsWith("Select two points"));
			assertTrue(app.getToolHelp(143).contains("line or vector"));
			app.setLanguage("es");
			assertEquals("Cota alineada", app.getToolName(142));
			assertEquals("Cota lineal", app.getToolName(143));
			assertTrue(app.getToolHelp(142).startsWith("Selecciona dos puntos"));
			assertSame(GeoCeDGToolImageResource.ALIGNED_DIMENSION,
					GeoCeDGToolImageResource.forIconKey("geocedg.action.AlignedDimension"));
			assertSame(GeoCeDGToolImageResource.LINEAR_DIMENSION,
					GeoCeDGToolImageResource.forMode("lineardimension"));
			for (GeoCeDGToolImageResource icon : new GeoCeDGToolImageResource[] {
					GeoCeDGToolImageResource.ALIGNED_DIMENSION,
					GeoCeDGToolImageResource.LINEAR_DIMENSION}) {
				String svg = new String(PreG9BR6PlusE2NativeDimensionDesktopTest.class
						.getResourceAsStream(icon.getFilename()).readAllBytes(),
						StandardCharsets.UTF_8);
				assertTrue(svg.startsWith("<!-- GeoCeDG-authored; SPDX-License-Identifier:"
						+ " EUPL-1.2 -->"), icon.name());
				assertTrue(svg.contains("viewBox=\"0 0 24 24\""), icon.name());
				assertFalse(svg.contains("<image"), "vector source only: " + icon.name());
			}
			assertEquals(127, GeoCeDGProfile.getActions().size());
		}

		// ------------------------------------------------------------------ tools

		void alignedTool() throws Exception {
			AppGeoCeDG app = app();
			GeoPoint a = (GeoPoint) eval(app, "A=(0,0)");
			GeoPoint b = (GeoPoint) eval(app, "B=(4,0)");
			app.setMode(EuclidianConstants.MODE_ALIGNED_DIMENSION);
			GeoCeDGEuclidianController controller = controller(app);
			AtomicInteger commits = new AtomicInteger();
			click(controller, a, commits);
			click(controller, b, commits);
			assertEquals(0, commits.get(), "no creation before the placement click");
			EuclidianView view = app.getEuclidianView1();
			controller.mouseLoc = new GPoint(view.toScreenCoordX(2), view.toScreenCoordY(1.5));
			assertTrue(click(controller, null, commits));
			assertEquals(1, commits.get(), "one undo point per creation");
			AlgoNativeDimension algo = onlyDimension(app);
			assertTrue(algo instanceof AlgoAlignedDimension);
			assertEquals(5, algo.getInput().length);
			assertEquals(1.5, algo.getOffsetInput().getDouble(), 1E-12);
			// unspecified unit: the 8 px / 4 px view fallback at 50 px per unit
			assertEquals(0.16, algo.getOvershootInput().getDouble(), 1E-12);
			assertEquals(0.08, algo.getGapInput().getDouble(), 1E-12);
			for (GeoElement parameter : new GeoElement[] {
					algo.getOffsetInput().toGeoElement(),
					algo.getOvershootInput().toGeoElement(), algo.getGapInput().toGeoElement()}) {
				assertTrue(parameter.isIndependent() && parameter.isLabelSet());
				assertTrue(parameter.isAuxiliaryObject());
				assertFalse(parameter.isEuclidianVisible());
			}
			// one-shot capture: a later zoom, unit or scale change changes nothing
			sized(app, 800, 600, 100, 100);
			app.getKernel().getConstruction().getUnitSystem()
					.replace(UnitState.of(UnitToken.MM, null, null));
			app.setDrawingScale(DrawingScale.of(1, 5));
			assertEquals(0.16, algo.getOvershootInput().getDouble(), 1E-12);
			assertEquals(1.5, algo.getOffsetInput().getDouble(), 1E-12);
			// physical unit and drawing scale: 2 mm and 1 mm on paper
			sized(app, 800, 600, 50, 50);
			app.setMode(EuclidianConstants.MODE_ALIGNED_DIMENSION);
			click(controller, a, commits);
			click(controller, b, commits);
			controller.mouseLoc = new GPoint(view.toScreenCoordX(2), view.toScreenCoordY(-1));
			click(controller, null, commits);
			AlgoNativeDimension second = null;
			for (AlgoNativeDimension each : dimensions(app)) {
				if (each != algo) {
					second = each;
				}
			}
			assertNotNull(second);
			assertEquals(-1, second.getOffsetInput().getDouble(), 1E-12);
			// mm with 1:5: 2 mm on paper = 10 mm of model = 10 model units
			assertEquals(10, second.getOvershootInput().getDouble(), 1E-9);
			assertEquals(5, second.getGapInput().getDouble(), 1E-9);
			// the dimension line is below A B (negative offset on the left normal)
			assertEquals(-1, second.getDimensionLine().getStartPoint().getInhomY(), 1E-12);
			// no creation when A = B
			app.setMode(EuclidianConstants.MODE_ALIGNED_DIMENSION);
			int before = dimensions(app).size();
			app.setErrorDialogsActive(false);
			click(controller, a, commits);
			click(controller, a, commits);
			controller.mouseLoc = new GPoint(view.toScreenCoordX(1), view.toScreenCoordY(1));
			click(controller, null, commits);
			assertEquals(before, dimensions(app).size());
		}

		private static List<AlgoNativeDimension> dimensions(AppGeoCeDG app) {
			List<AlgoNativeDimension> all = new ArrayList<>();
			for (GeoElement geo : app.getKernel().getConstruction()
					.getGeoSetConstructionOrder()) {
				AlgoNativeDimension owner = AlgoNativeDimension.ownerOf(geo);
				if (owner != null && !all.contains(owner)) {
					all.add(owner);
				}
			}
			return all;
		}

		void linearTool() throws Exception {
			AppGeoCeDG app = app();
			GeoPoint a = (GeoPoint) eval(app, "A=(0,0)");
			GeoPoint b = (GeoPoint) eval(app, "B=(4,3)");
			GeoElement axis = app.getKernel().getXAxis();
			GeoElement vector = eval(app, "v=Vector((0,2))");
			EuclidianView view = app.getEuclidianView1();
			GeoCeDGEuclidianController controller = controller(app);
			AtomicInteger commits = new AtomicInteger();
			for (GeoElement direction : new GeoElement[] {axis, vector}) {
				app.setMode(EuclidianConstants.MODE_LINEAR_DIMENSION);
				click(controller, a, commits);
				click(controller, b, commits);
				controller.mouseLoc = new GPoint(view.toScreenCoordX(9), view.toScreenCoordY(9));
				click(controller, null, commits);
				assertEquals(0, dimensions(app).stream()
						.filter(d -> ((AlgoLinearDimension) d).getDirectionInput() == direction)
						.count(), "no placement before the direction");
				click(controller, direction, commits);
				controller.mouseLoc = new GPoint(view.toScreenCoordX(2), view.toScreenCoordY(4));
				click(controller, null, commits);
			}
			List<AlgoNativeDimension> created = dimensions(app);
			assertEquals(2, created.size());
			AlgoLinearDimension horizontal = (AlgoLinearDimension) created.get(0);
			assertSame(axis, horizontal.getDirectionInput());
			assertEquals(4, horizontal.getValue().getDouble(), 1E-12);
			assertEquals(4, horizontal.getOffsetInput().getDouble(), 1E-12);
			AlgoLinearDimension vertical = (AlgoLinearDimension) created.get(1);
			assertSame(vector, vertical.getDirectionInput());
			assertEquals(3, vertical.getValue().getDouble(), 1E-12);
			// left normal of (0,1) is (-1,0): the click at x = 2 is an offset of -2
			assertEquals(-2, vertical.getOffsetInput().getDouble(), 1E-12);
		}

		// ------------------------------------------------------------------- drag

		void drag() throws Exception {
			AppGeoCeDG app = app();
			final GeoPoint a = (GeoPoint) eval(app, "A=(-2,0)");
			final GeoPoint b = (GeoPoint) eval(app, "B=(2,0)");
			final GeoNumeric s = (GeoNumeric) eval(app, "s=1");
			final GeoElement[] out = dim(app, "AlignedDimension(A,B,s)");
			UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction()
					.getUndoManager();
			try (var baseline = undo.prepareUndoBaseline()) {
				undo.commitUndoBaseline(baseline);
			}
			CountDownLatch stored = new CountDownLatch(1);
			undo.addUndoInfoStoredListener(stored::countDown);
			repaint(app);
			app.setMode(EuclidianConstants.MODE_MOVE);
			GeoCeDGEuclidianController controller = controller(app);
			controller.wrapMousePressed(event(app, -1, 1));
			controller.wrapMouseDragged(event(app, -1, 2.5), true);
			controller.wrapMouseDragged(event(app, -0.5, 3), true);
			controller.wrapMouseReleased(event(app, -0.5, 3));
			assertEquals(3, s.getDouble(), 1E-9, "only the offset follows the pointer");
			assertEquals(-2, a.getInhomX(), 0);
			assertEquals(0, a.getInhomY(), 0);
			assertEquals(2, b.getInhomX(), 0);
			assertEquals(3, ((GeoSegment) out[1]).getStartPoint().getInhomY(), 1E-9);
			assertTrue(stored.await(5, TimeUnit.SECONDS), "the drag stores one undo point");
			app.getKernel().undo();
			await(() -> app.getKernel().lookupLabel("s") != null
					&& ((GeoNumeric) app.getKernel().lookupLabel("s")).getDouble() == 1);
			assertEquals(1, ((GeoNumeric) app.getKernel().lookupLabel("s")).getDouble(),
					1E-12, "one undo point restores the offset");
			app.getKernel().redo();
			await(() -> app.getKernel().lookupLabel("s") != null
					&& Math.abs(((GeoNumeric) app.getKernel().lookupLabel("s")).getDouble() - 3)
					< 1E-9);
			assertEquals(3, ((GeoNumeric) app.getKernel().lookupLabel("s")).getDouble(),
					1E-9);
			// a dimension whose offset is not a free number is not dragged, and the
			// measured points are never moved through it
			eval(app, "C=(-2,-3)");
			eval(app, "D=(2,-3)");
			GeoElement[] fixed = dim(app, "AlignedDimension(C,D,0.5*s)");
			repaint(app);
			GeoPoint c = (GeoPoint) app.getKernel().lookupLabel("C");
			double lineY = ((GeoSegment) fixed[1]).getStartPoint().getInhomY();
			controller.wrapMousePressed(event(app, 1, lineY));
			controller.wrapMouseDragged(event(app, 1, lineY + 2), true);
			controller.wrapMouseReleased(event(app, 1, lineY + 2));
			assertEquals(-3, c.getInhomY(), 0);
			assertEquals(lineY, ((GeoSegment) fixed[1]).getStartPoint().getInhomY(), 0);
			// the inherited move gate refuses every dimension output
			for (GeoElement output : fixed) {
				assertFalse(output.hasMoveableInputPoints(app.getEuclidianView1()),
						output.getLabelSimple());
				// the keyboard and multi-selection move route (MoveGeos) moves nothing
				org.geogebra.common.kernel.geos.MoveGeos.moveObjects(
						new ArrayList<>(List.of(output)),
						new org.geogebra.common.kernel.matrix.Coords(1, 1, 0), null, null,
						app.getEuclidianView1());
				assertEquals(-2, c.getInhomX(), 0, output.getLabelSimple());
				assertEquals(-3, c.getInhomY(), 0, output.getLabelSimple());
				assertEquals(lineY, ((GeoSegment) fixed[1]).getStartPoint().getInhomY(), 0);
			}
		}

		// ---------------------------------------------------------- aligned value

		private static GeoElement[] figure(AppGeoCeDG app, double degrees, boolean reversed,
				double offset, double length) {
			double r = Math.toRadians(degrees);
			double bx = length * Math.cos(r);
			double by = length * Math.sin(r);
			String a = "(0,0)";
			String b = "(" + bx + "," + by + ")";
			eval(app, "A=" + (reversed ? b : a));
			eval(app, "B=" + (reversed ? a : b));
			eval(app, "s=" + offset);
			GeoElement[] out = dim(app, "AlignedDimension(A,B,s,0.2,0.1)");
			app.getKernel().getConstruction().getUnitSystem()
					.replace(UnitState.of(UnitToken.CM, UnitToken.MM, null));
			style(app, out);
			return out;
		}

		private static void style(AppGeoCeDG app, GeoElement[] out) {
			for (GeoElement geo : app.getKernel().getConstruction()
					.getGeoSetConstructionOrder()) {
				geo.setEuclidianVisible(false);
				geo.updateRepaint();
			}
			out[1].setObjColor(GColor.BLUE);
			out[1].setLineThickness(5);
			out[1].setLineOpacity(255);
			for (int i = 2; i <= 3; i++) {
				out[i].setObjColor(GColor.GREEN);
				out[i].setLineThickness(3);
				out[i].setLineOpacity(255);
			}
			out[4].setObjColor(GColor.RED);
			for (int i = 1; i <= 4; i++) {
				out[i].setEuclidianVisible(true);
				out[i].updateRepaint();
			}
		}

		private static void repaint(AppGeoCeDG app) throws Exception {
			paint(app);
		}

		private static BufferedImage paint(AppGeoCeDG app) throws Exception {
			EuclidianView view = app.getEuclidianView1();
			BufferedImage img = new BufferedImage(view.getWidth(), view.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			SwingUtilities.invokeAndWait(() -> {
				view.updateAllDrawables(true);
				Graphics2D g = img.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				view.paint(new GGraphics2DD(g));
				g.dispose();
			});
			return img;
		}

		/** Measured properties of the drawn value against its dimension figure. */
		static final class Measure {
			double parallel;
			double longitudinal;
			int crossLine;
			int crossExtension;
			int pixels;
			double readable;
			double upsideDown;

			@Override
			public String toString() {
				return String.format(Locale.ROOT,
						"par=%.2f lon=%.2f crossLine=%d crossExt=%d px=%d read=%.2f/%.2f",
						parallel, longitudinal, crossLine, crossExtension, pixels, readable,
						upsideDown);
			}
		}

		private static Measure measure(AppGeoCeDG app, GeoElement[] out, List<int[]> reference)
				throws Exception {
			BufferedImage img = paint(app);
			EuclidianView view = app.getEuclidianView1();
			DrawableND drawable = view.getDrawableFor(out[4]);
			assertTrue(drawable instanceof DrawDimensionText,
					"dimension values use the dimension drawable");
			double theta = ((DrawDimensionText) drawable).getReadingAngleDegrees();
			double[] line = screen(view, (GeoSegment) out[1]);
			double[][] ext = {screen(view, (GeoSegment) out[2]),
					screen(view, (GeoSegment) out[3])};
			return measure(img, line, ext, theta, reference, 1);
		}

		private static double[] screen(EuclidianView view, GeoSegment segment) {
			if (!segment.isDefined()) {
				return null;
			}
			return new double[] {view.toScreenCoordXd(segment.getStartPoint().getInhomX()),
					view.toScreenCoordYd(segment.getStartPoint().getInhomY()),
					view.toScreenCoordXd(segment.getEndPoint().getInhomX()),
					view.toScreenCoordYd(segment.getEndPoint().getInhomY())};
		}

		static boolean red(int rgb) {
			int r = (rgb >> 16) & 255;
			int g = (rgb >> 8) & 255;
			int b = rgb & 255;
			return r > 120 && r - Math.max(g, b) > 70;
		}

		static List<int[]> mask(BufferedImage img) {
			List<int[]> p = new ArrayList<>();
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					if (red(img.getRGB(x, y))) {
						p.add(new int[] {x, y});
					}
				}
			}
			return p;
		}

		static double segDist(double px, double py, double[] s) {
			double dx = s[2] - s[0];
			double dy = s[3] - s[1];
			double len2 = dx * dx + dy * dy;
			double t = len2 == 0 ? 0 : ((px - s[0]) * dx + (py - s[1]) * dy) / len2;
			t = Math.max(0, Math.min(1, t));
			return Math.hypot(s[0] + t * dx - px, s[1] + t * dy - py);
		}

		static Measure measure(BufferedImage img, double[] line, double[][] ext,
				double theta, List<int[]> reference, double scale) {
			List<int[]> m = mask(img);
			Measure result = new Measure();
			result.pixels = m.size();
			assertTrue(m.size() > 20, "value pixels drawn");
			double cx = 0;
			double cy = 0;
			for (int[] p : m) {
				cx += p[0];
				cy += p[1];
			}
			cx /= m.size();
			cy /= m.size();
			double sxx = 0;
			double syy = 0;
			double sxy = 0;
			for (int[] p : m) {
				double dx = p[0] - cx;
				double dy = p[1] - cy;
				sxx += dx * dx;
				syy += dy * dy;
				sxy += dx * dy;
			}
			double alpha = -Math.toDegrees(0.5 * Math.atan2(2 * sxy, sxx - syy));
			double lineAngle = Math.toDegrees(Math.atan2(-(line[3] - line[1]),
					line[2] - line[0]));
			result.parallel = ((alpha - lineAngle) % 180 + 180 + 90) % 180 - 90;
			double mx = (line[0] + line[2]) / 2;
			double my = (line[1] + line[3]) / 2;
			double len = Math.hypot(line[2] - line[0], line[3] - line[1]);
			result.longitudinal = ((cx - mx) * (line[2] - line[0])
					+ (cy - my) * (line[3] - line[1])) / len;
			for (int[] p : m) {
				if (segDist(p[0], p[1], line) <= LINE_HALF * scale + 0.5) {
					result.crossLine++;
				}
				for (double[] e : ext) {
					if (e != null && segDist(p[0], p[1], e) <= 1.5 * scale + 0.5) {
						result.crossExtension++;
					}
				}
			}
			if (reference != null) {
				double rcx = 0;
				double rcy = 0;
				for (int[] p : reference) {
					rcx += p[0];
					rcy += p[1];
				}
				rcx /= reference.size();
				rcy /= reference.size();
				java.util.Set<Long> set = new java.util.HashSet<>();
				for (int[] p : m) {
					for (int dx = -1; dx <= 1; dx++) {
						for (int dy = -1; dy <= 1; dy++) {
							set.add(((long) (p[0] + dx) << 32) | ((p[1] + dy) & 0xffffffffL));
						}
					}
				}
				double t = Math.toRadians(theta);
				result.readable = overlap(reference, rcx, rcy, cx, cy, t, set);
				result.upsideDown = overlap(reference, rcx, rcy, cx, cy, t + Math.PI, set);
			}
			return result;
		}

		static double overlap(List<int[]> ref, double rcx, double rcy, double cx, double cy,
				double t, java.util.Set<Long> set) {
			int hit = 0;
			double c = Math.cos(t);
			double s = Math.sin(t);
			for (int[] p : ref) {
				double x = p[0] - rcx;
				double y = p[1] - rcy;
				long px = Math.round(cx + x * c + y * s);
				long py = Math.round(cy - x * s + y * c);
				if (set.contains((px << 32) | (py & 0xffffffffL))) {
					hit++;
				}
			}
			return hit / (double) ref.size();
		}

		private static void assertGood(Measure m, String context) {
			assertTrue(Math.abs(m.parallel) <= 3.5, context + " parallel " + m);
			assertTrue(Math.abs(m.longitudinal) <= 3, context + " centred " + m);
			assertEquals(0, m.crossLine, context + " dimension line clear " + m);
			assertEquals(0, m.crossExtension, context + " extension lines clear " + m);
			if (m.readable > 0) {
				assertTrue(m.readable > m.upsideDown, context + " readable " + m);
			}
		}

		private static void reset(AppGeoCeDG app) {
			app.setSaved();
			app.clearConstruction();
		}

		void sweep() throws Exception {
			AppGeoCeDG app = app();
			GeoElement[] reference = figure(app, 0, false, 1.5, 4);
			List<int[]> template = mask(paint(app));
			double[] angles = {0, 30, 44, 45, 46, 89, 90, 91, 135, 179};
			for (double offset : new double[] {1.5, -1.5}) {
				for (double angle : angles) {
					for (boolean reversed : new boolean[] {false, true}) {
						reset(app);
						GeoElement[] out = figure(app, angle, reversed, offset, 4);
						Measure m = measure(app, out, template);
						DrawDimensionText d = (DrawDimensionText) app.getEuclidianView1()
								.getDrawableFor(out[4]);
						double theta = d.getReadingAngleDegrees();
						assertTrue(theta > -90 && theta <= 90, "theta " + theta);
						String context = "angle=" + angle + " reversed=" + reversed
								+ " offset=" + offset;
						assertGood(m, context);
						note(context + " theta=" + theta + " " + m);
					}
				}
			}
			assertNotNull(reference);
			// horizontal reads left to right, vertical bottom to top, reversal identical
			reset(app);
			GeoElement[] h = figure(app, 0, true, 1.5, 4);
			paint(app);
			assertEquals(0, ((DrawDimensionText) app.getEuclidianView1()
					.getDrawableFor(h[4])).getReadingAngleDegrees(), 1E-9);
			reset(app);
			GeoElement[] v = figure(app, 90, true, 1.5, 4);
			paint(app);
			assertEquals(90, ((DrawDimensionText) app.getEuclidianView1()
					.getDrawableFor(v[4])).getReadingAngleDegrees(), 1E-9);
			// an ordinary text keeps the ordinary drawable
			GeoElement plain = eval(app, "plain=Text(\"x\", (1,1))");
			plain.setEuclidianVisible(true);
			plain.updateRepaint();
			paint(app);
			assertTrue(app.getEuclidianView1().getDrawableFor(plain) instanceof DrawText);
		}

		void robustness() throws Exception {
			AppGeoCeDG app = app();
			figure(app, 0, false, 1.5, 4);
			List<int[]> template = mask(paint(app));
			reset(app);
			GeoElement[] out = figure(app, 30, false, 1.5, 4);
			assertGood(measure(app, out, template), "base");
			sized(app, 800, 600, 100, 100);
			assertGood(measure(app, out, null), "zoom");
			sized(app, 1000, 700, 50, 50);
			assertGood(measure(app, out, null), "resize");
			sized(app, 800, 600, 50, 100);
			Measure anisotropic = measure(app, out, null);
			assertGood(anisotropic, "anisotropic axes");
			note("anisotropic " + anisotropic);
			sized(app, 800, 600, 50, 50);
			GeoText text = (GeoText) out[4];
			for (UnitState state : new UnitState[] {
					UnitState.of(UnitToken.CM, UnitToken.MM, null),
					UnitState.of(UnitToken.CM, UnitToken.M, null),
					UnitState.of(UnitToken.M, UnitToken.MM, null),
					UnitState.of(UnitToken.CM, UnitToken.USM,
							UsmDefinition.of(0.0254, "inch", "in"))}) {
				app.getKernel().getConstruction().getUnitSystem().replace(state);
				Measure m = measure(app, out, null);
				assertGood(m, "units " + state + " -> " + text.getTextString());
				note(text.getTextString() + " " + m);
			}
			assertEquals("4 cm", unitText(app, out, UnitState.of(UnitToken.CM, null, null)));
			assertEquals("40 mm", unitText(app, out, UnitState.of(UnitToken.CM, UnitToken.MM,
					null)));
			assertEquals("0.04 m", unitText(app, out, UnitState.of(UnitToken.CM, UnitToken.M,
					null)));
			// both sides, vertical and oblique dimensions of a short line
			reset(app);
			GeoElement[] below = figure(app, 120, false, -1.5, 4);
			assertGood(measure(app, below, null), "negative side");
			reset(app);
			GeoElement[] shortLine = figure(app, 0, false, 1.5, 1);
			app.getKernel().getConstruction().getUnitSystem().replace(
					UnitState.of(UnitToken.MM, UnitToken.M, null));
			Measure longValue = measure(app, shortLine, null);
			assertGood(longValue, "value longer than its line");
			note("long value " + ((GeoText) shortLine[4]).getTextString() + " " + longValue);
		}

		private static String unitText(AppGeoCeDG app, GeoElement[] out, UnitState state) {
			app.getKernel().getConstruction().getUnitSystem().replace(state);
			assertEquals(4, ((GeoNumeric) out[0]).getDouble(), 1E-12);
			return ((GeoText) out[4]).getTextString();
		}

		void picture() throws Exception {
			AppGeoCeDG app = app();
			GeoElement[] out = figure(app, 30, false, 1.5, 4);
			paint(app);
			EuclidianView view = app.getEuclidianView1();
			for (double scale : new double[] {1, 2, 3}) {
				BufferedImage image = GBufferedImageD.getAwtBufferedImage(app
						.getPictureExportService().exportImage(view, scale, false,
								ExportType.PNG));
				// the export viewport has its own origin: locate the line by its pixels
				double[] line = bluePrincipalSegment(image);
				Measure m = measure(image, line, new double[0][], 30, null, scale);
				assertTrue(Math.abs(m.parallel) <= 3.5, "scale " + scale + " " + m);
				assertTrue(Math.abs(m.longitudinal) <= 3 * scale, "scale " + scale + " " + m);
				assertEquals(0, m.crossLine, "scale " + scale + " " + m);
				note("export scale " + scale + " " + image.getWidth() + "x"
						+ image.getHeight() + " " + m);
			}
			assertEquals("40 mm", ((GeoText) out[4]).getTextString());
		}

		private static double[] bluePrincipalSegment(BufferedImage img) {
			List<int[]> pixels = new ArrayList<>();
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					int rgb = img.getRGB(x, y);
					int r = (rgb >> 16) & 255;
					int g = (rgb >> 8) & 255;
					int b = rgb & 255;
					if (b > 150 && r < 90 && g < 90) {
						pixels.add(new int[] {x, y});
					}
				}
			}
			assertTrue(pixels.size() > 20, "dimension line pixels");
			double cx = 0;
			double cy = 0;
			for (int[] p : pixels) {
				cx += p[0];
				cy += p[1];
			}
			cx /= pixels.size();
			cy /= pixels.size();
			double sxx = 0;
			double syy = 0;
			double sxy = 0;
			for (int[] p : pixels) {
				sxx += (p[0] - cx) * (p[0] - cx);
				syy += (p[1] - cy) * (p[1] - cy);
				sxy += (p[0] - cx) * (p[1] - cy);
			}
			double a = 0.5 * Math.atan2(2 * sxy, sxx - syy);
			double ux = Math.cos(a);
			double uy = Math.sin(a);
			double min = Double.MAX_VALUE;
			double max = -Double.MAX_VALUE;
			for (int[] p : pixels) {
				double t = (p[0] - cx) * ux + (p[1] - cy) * uy;
				min = Math.min(min, t);
				max = Math.max(max, t);
			}
			return new double[] {cx + min * ux, cy + min * uy, cx + max * ux, cy + max * uy};
		}

		// ----------------------------------------------------------- hit testing

		void hit() throws Exception {
			AppGeoCeDG app = app();
			double[][] cases = {{0, 50, 50}, {30, 50, 50}, {45, 50, 50}, {90, 50, 50},
					{135, 50, 50}, {179, 50, 50}, {30, 50, 100}, {30, 100, 100}};
			for (double[] c : cases) {
				reset(app);
				sized(app, 800, 600, c[1], c[2]);
				GeoElement[] out = figure(app, c[0], false, 1.5, 4);
				paint(app);
				EuclidianView view = app.getEuclidianView1();
				DrawDimensionText d = (DrawDimensionText) view.getDrawableFor(out[4]);
				double[] center = d.getPlacementCenter();
				double[] corners = d.getRotatedCorners();
				String context = "angle=" + c[0] + " scale=" + c[1] + "x" + c[2];
				assertTrue(d.hit((int) Math.round(center[0]), (int) Math.round(center[1]), 0),
						context);
				// a point beyond the rotated box along its height is outside
				double ux = corners[6] - corners[0];
				double uy = corners[7] - corners[1];
				double h = Math.hypot(ux, uy);
				int ox = (int) Math.round(center[0] + ux / h * (h / 2 + 6));
				int oy = (int) Math.round(center[1] + uy / h * (h / 2 + 6));
				assertFalse(d.hit(ox, oy, 0), context + " outside the rotated box");
				Hits hits = hitsAt(app, (int) Math.round(center[0]),
						(int) Math.round(center[1]));
				assertTrue(hits.contains(out[4]), context + " text hit " + hits);
				assertFalse(hits.contains(out[1]), context + " line not hit at the text");
				double[] line = screen(view, (GeoSegment) out[1]);
				int lx = (int) Math.round(line[0] + (line[2] - line[0]) * 0.1);
				int ly = (int) Math.round(line[1] + (line[3] - line[1]) * 0.1);
				Hits lineHits = hitsAt(app, lx, ly);
				assertTrue(lineHits.contains(out[1]), context + " line hit");
				assertFalse(lineHits.contains(out[4]), context + " text not hit on the line");
			}
		}

		private static Hits hitsAt(AppGeoCeDG app, int x, int y) throws Exception {
			EuclidianView view = app.getEuclidianView1();
			Hits[] result = new Hits[1];
			SwingUtilities.invokeAndWait(() -> {
				view.setHits(new GPoint(x, y), org.geogebra.common.euclidian.event
						.PointerEventType.MOUSE);
				result[0] = view.getHits().cloneHits();
			});
			return result[0];
		}

		void corner() throws Exception {
			AppGeoCeDG app = app();
			GeoElement[] out = figure(app, 30, false, 1.5, 4);
			out[4].setLabel("T");
			paint(app);
			EuclidianView view = app.getEuclidianView1();
			DrawDimensionText d = (DrawDimensionText) view.getDrawableFor(out[4]);
			double[] corners = d.getRotatedCorners();
			double minX = Double.MAX_VALUE;
			double maxY = -Double.MAX_VALUE;
			double maxX = -Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			for (int i = 0; i < 8; i += 2) {
				minX = Math.min(minX, corners[i]);
				maxX = Math.max(maxX, corners[i]);
				minY = Math.min(minY, corners[i + 1]);
				maxY = Math.max(maxY, corners[i + 1]);
			}
			GeoPoint c1 = (GeoPoint) eval(app, "C_1=Corner(T,1)");
			GeoPoint c3 = (GeoPoint) eval(app, "C_3=Corner(T,3)");
			paint(app);
			c1.updateCascade();
			c3.updateCascade();
			double tolerance = 1.5 / view.getXscale();
			assertEquals(view.toRealWorldCoordX(minX), c1.getInhomX(), tolerance);
			assertEquals(view.toRealWorldCoordY(maxY), c1.getInhomY(), tolerance);
			assertEquals(view.toRealWorldCoordX(maxX), c3.getInhomX(), tolerance);
			assertEquals(view.toRealWorldCoordY(minY), c3.getInhomY(), tolerance);
			note("Corner(T,1)=" + c1.toValueString(StringTemplate.testTemplate)
					+ " Corner(T,3)=" + c3.toValueString(StringTemplate.testTemplate));
		}

		// ----------------------------------------------------------------- export

		void latex() throws Exception {
			AppGeoCeDG app = app();
			GeoElement[] out = figure(app, 30, false, 1.5, 4);
			for (GeoElement geo : out) {
				geo.setObjColor(GColor.BLACK);
				geo.updateRepaint();
			}
			GeoElement other = eval(app, "other=Segment((0,-2),(3,-2))");
			other.setEuclidianVisible(true);
			other.updateRepaint();
			GeoElement plain = eval(app, "plain=Text(\"plain\", (0,-3))");
			plain.setEuclidianVisible(true);
			plain.updateRepaint();
			app.getKernel().getConstruction().getUnitSystem()
					.replace(UnitState.of(UnitToken.CM, UnitToken.MM, null));
			paint(app);
			EuclidianView view = app.getEuclidianView1();
			String[] dialects = {"pgf", "pstricks", "asymptote"};
			for (String dialect : dialects) {
				GeoGebraExport export = PreG9BR6PlusCLatexExportTest.exporter(app, dialect);
				String code = PreG9BR6PlusCLatexExportTest.generate(export, view);
				Files.writeString(dir.resolve("e2." + ("asymptote".equals(dialect) ? "asy"
						: dialect + ".tex")), code);
				note(dialect + ":\n" + code);
				switch (dialect) {
				case "pgf":
					assertTrue(code.contains("<->,>=latex]"), code);
					assertTrue(anyOf(code, "node[rotate=30,anchor=south] {40 mm}",
							"node[rotate=30.,anchor=south] {40 mm}"), code);
					assertEquals(1, count(code, "<->"), "only the dimension line has arrows");
					assertTrue(code.contains("node[anchor=north west] {plain}"),
							"ordinary text unchanged");
					break;
				case "pstricks":
					assertTrue(code.contains("{<->}("), code);
					assertTrue(anyOf(code, "\\rput[b]{30}(", "\\rput[b]{30.}("), code);
					assertTrue(code.contains("{\\raisebox{0.5ex}{40 mm}}"), code);
					assertEquals(1, count(code, "{<->}"));
					break;
				default:
					assertTrue(code.contains(", Arrows(6)"), code);
					assertTrue(anyOf(code, "label(rotate(30)*Label(\"40 mm\"), ",
							"label(rotate(30.)*Label(\"40 mm\"), "), code);
					assertTrue(anyOf(code, "dir(120)", "dir(120.)"), code);
					assertEquals(1, count(code, "Arrows(6)"));
					break;
				}
			}
		}

		private static boolean anyOf(String text, String... fragments) {
			for (String fragment : fragments) {
				if (text.contains(fragment)) {
					return true;
				}
			}
			return false;
		}

		private static int count(String text, String fragment) {
			int c = 0;
			for (int i = text.indexOf(fragment); i >= 0; i = text.indexOf(fragment, i + 1)) {
				c++;
			}
			return c;
		}

		void dxf() throws Exception {
			AppGeoCeDG app = app();
			figure(app, 30, false, 1.5, 4);
			GeometryExportService service = new GeometryExportService();
			GeometryExportModel model = service.createModel(new ArrayList<>(app.getKernel()
					.getConstruction().getGeoSetConstructionOrder()),
					GeometryExportModel.SelectionMode.COMPLETE_CONSTRUCTION);
			String dxf = service.exportDxf(model);
			assertEquals(3, count(dxf, "\r\nLINE\r\n"), "dimension and extension lines");
			assertEquals(0, count(dxf, "\r\nDIMENSION\r\n"), "no DIMENSION entity");
			assertEquals(0, count(dxf, "\r\nTEXT\r\n") + count(dxf, "\r\nMTEXT\r\n"));
			StringBuilder diagnostics = new StringBuilder();
			for (GeometryExportModel.Diagnostic d : model.getDiagnostics()) {
				diagnostics.append(d.getCode()).append(": ").append(d.getMessage()).append(';');
			}
			assertTrue(diagnostics.toString().contains("OUTSIDE_GEOMETRIC_POPULATION"),
					diagnostics.toString());
			note("dxf diagnostics: " + diagnostics);
		}

		// ------------------------------------------------------------ persistence

		void persistence() throws Exception {
			AppGeoCeDG app = app();
			eval(app, "A=(0,0)");
			eval(app, "B=(3,4)");
			eval(app, "s=1.5");
			GeoElement[] out = dim(app, "AlignedDimension(A,B,s)");
			dim(app, "LinearDimension(A,B,xAxis,s,0.2,0.1)");
			app.getKernel().getConstruction().getUnitSystem()
					.replace(UnitState.of(UnitToken.CM, UnitToken.MM, null));
			String xml = app.getXML();
			File file = dir.resolve("e2.cedg").toFile();
			((MyXMLioD) app.getXMLio()).writeGeoGebraFile(file);
			AppGeoCeDG reopened = G9U1TestApp.create();
			reopened.setSaved();
			assertTrue(reopened.loadXML(file, false));
			assertEquals(construction(xml), construction(reopened.getXML()),
					"save and reopen reproduce the construction");
			GeoText text = (GeoText) reopened.getKernel().lookupLabel(out[4].getLabelSimple());
			assertEquals("50 mm", text.getTextString());
			// copy and paste: a new dimension with its own explicit parameters
			int before = dimensionsCount(app);
			InternalClipboard.duplicate(app, new ArrayList<>(List.of(out[1])));
			assertEquals(before + 1, dimensionsCount(app));
			note("dimensions after paste " + dimensionsCount(app));
		}

		private static String construction(String xml) {
			int start = xml.indexOf("<construction");
			int end = xml.indexOf("</construction>");
			return xml.substring(start, end);
		}

		private static int dimensionsCount(AppGeoCeDG app) {
			return dimensions(app).size();
		}

		// ------------------------------------------------------------- legacy names

		void legacy() throws Exception {
			AppGeoCeDG app = app();
			Path copy = dir.resolve("Templatev7.ggb");
			Files.copy(PreG9BR6PlusE1LCuratedLibraryTest.repositoryRoot()
					.resolve("models/legacy/template-v7/original/Templatev7.ggb"), copy);
			app.setSaved();
			assertTrue(app.loadXML(copy.toFile(), false));
			Macro direct = app.getKernel().getMacro("directDimension");
			Macro axis = app.getKernel().getMacro("axisDimension");
			assertNotNull(direct);
			assertNotNull(axis);
			assertEquals("directDimension", direct.getCommandName(), "macro never renamed");
			eval(app, "P_1=(0,0)");
			eval(app, "P_2=(4,0)");
			GeoElement[] nativeOut = dim(app, "AlignedDimension(P_1,P_2,1)");
			assertTrue(nativeOut[0].getParentAlgorithm() instanceof AlgoAlignedDimension);
			GeoElementND[] legacyOut = app.getKernel().getAlgebraProcessor()
					.processAlgebraCommand("directDimension(P_1,P_2,10)", false);
			assertNotNull(legacyOut);
			assertTrue(legacyOut[0].toGeoElement().getParentAlgorithm() instanceof AlgoMacro,
					"the legacy macro still answers its own name");
			GeoElement[] linear = dim(app, "LinearDimension(P_1,P_2,xAxis,1)");
			assertTrue(linear[0].getParentAlgorithm() instanceof AlgoLinearDimension);
			String xml = app.getXML();
			app.setXML(xml, true);
			assertTrue(app.getXML().contains("<command name=\"AlignedDimension\">"));
			assertTrue(app.getXML().contains("<command name=\"directDimension\">"));
			assertEquals("directDimension",
					app.getKernel().getMacro("directDimension").getCommandName());
			// GGBScript and nested Execute reach the native commands next to the macros
			app.getKernel().getAlgebraProcessor().processAlgebraCommand(
					"Execute({\"E_1=AlignedDimension(P_1,P_2,2)\"})", false);
			assertTrue(app.getKernel().lookupLabel("E_1").getParentAlgorithm()
					instanceof AlgoAlignedDimension);

			// the user tool store keeps legacy-named packages readable in EN and ES
			Path store = dir.resolve("tools.json");
			AppGeoCeDG host = G9U1TestApp.create();
			GeoCeDGUserToolLibrary library = new GeoCeDGUserToolLibrary(host, store);
			library.install("legacy.ggt", legacyPackage("directDimension", "axisDimension"));
			byte[] bytes = Files.readAllBytes(store);
			for (String language : new String[] {"en", "es"}) {
				host.setLanguage(language);
				GeoCeDGUserToolLibrary reread = new GeoCeDGUserToolLibrary(host, store);
				assertEquals(1, reread.packages().size(), language);
			}
			assertTrue(Arrays.equals(bytes, Files.readAllBytes(store)), "store untouched");
			assertNull(Commands.lookupInternal("directDimension"));
			assertNull(Commands.lookupInternal("axisDimension"));
		}

		private static byte[] legacyPackage(String... names) throws Exception {
			AppGeoCeDG source = G9U1TestApp.create();
			GeoElement a = G9U1TestApp.eval(source, "A=(0,0)");
			GeoElement b = G9U1TestApp.eval(source, "B=(2,0)");
			GeoElement m = G9U1TestApp.eval(source, "M=Midpoint(A,B)");
			ArrayList<Macro> macros = new ArrayList<>();
			for (String name : names) {
				Macro macro = new Macro(source.getKernel(), name, new GeoElement[] {a, b},
						new GeoElement[] {m});
				source.getKernel().addMacro(macro);
				macros.add(macro);
			}
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			source.getXMLio().writeMacroStream(output, macros, new ArrayList<>());
			return output.toByteArray();
		}

		// --------------------------------------------------------------- names

		void names() throws Exception {
			AppGeoCeDG app = app();
			eval(app, "A=(0,0)");
			eval(app, "B=(4,0)");
			app.setLanguage("es");
			GeoElement[] es = dim(app, "CotaAlineada(A,B,1)");
			assertTrue(es[0].getParentAlgorithm() instanceof AlgoAlignedDimension);
			GeoElement[] esLinear = dim(app, "CotaLineal(A,B,EjeX,1)");
			assertTrue(esLinear[0].getParentAlgorithm() instanceof AlgoLinearDimension);
			GeoElement[] en = dim(app, "AlignedDimension(A,B,2)");
			assertTrue(en[0].getParentAlgorithm() instanceof AlgoAlignedDimension);
			assertEquals("AlignedDimension", app.getLocalization()
					.getReverseCommand("CotaAlineada"));
			assertEquals("LinearDimension", app.getLocalization()
					.getReverseCommand("cotalineal"));
			for (String legacy : new String[] {"DirectDimension", "AxisDimension"}) {
				assertNull(app.getLocalization().getReverseCommand(legacy), legacy);
			}
			assertEquals("Dimension", app.getLocalization().getReverseCommand("Dimensión"));
			String definition = es[0].getDefinition(StringTemplate.defaultTemplate);
			note("ES definition " + definition);
			assertTrue(definition.startsWith("AlignedDimension("), definition);
			app.setLanguage("en");
			assertEquals("AlignedDimension( <Point>, <Point>, <Offset> )\n"
					+ "AlignedDimension( <Point>, <Point>, <Offset>, <Overshoot>, <Gap> )",
					app.getLocalization().getCommandSyntax("AlignedDimension"));
			ErrorAccumulator errors = new ErrorAccumulator();
			app.getKernel().getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(
					"DirectDimension(A,B,1)", false, errors, false, null);
			assertTrue(errors.getErrors().contains("DirectDimension"), errors.getErrors());
		}
	}

	/** Compiles the generated LaTeX family when the toolchain is installed. */
	static final class LatexCompilation {
		private LatexCompilation() {
		}

		static void compileIfAvailable(Path dir) throws Exception {
			for (String[] job : new String[][] {{"pdflatex", "e2.pgf.tex"},
					{"latex", "e2.pstricks.tex"}, {"asy", "e2.asy"}}) {
				Path tool = find(job[0]);
				if (tool == null) {
					System.out.println("LATEX " + job[0] + " not installed: structural only");
					continue;
				}
				List<String> command = new ArrayList<>(List.of(tool.toString()));
				if (!"asy".equals(job[0])) {
					command.add("-interaction=nonstopmode");
					command.add("-halt-on-error");
				}
				command.add(job[1]);
				Process process = new ProcessBuilder(command).directory(dir.toFile())
						.redirectErrorStream(true)
						.redirectOutput(dir.resolve(job[1] + ".log.txt").toFile()).start();
				boolean ended = process.waitFor(240, TimeUnit.SECONDS);
				if (!ended) {
					process.destroyForcibly();
				}
				String log = Files.readString(dir.resolve(job[1] + ".log.txt"));
				assertTrue(ended && process.exitValue() == 0, job[0] + " " + job[1] + ":\n"
						+ log);
				System.out.println("LATEX " + job[0] + " " + job[1] + " compiled (exit 0)");
			}
		}

		private static Path find(String name) {
			String path = System.getenv("PATH");
			if (path == null) {
				return null;
			}
			for (String entry : path.split(File.pathSeparator)) {
				for (String suffix : new String[] {".exe", ""}) {
					Path candidate = Paths.get(entry, name + suffix);
					if (Files.isRegularFile(candidate)) {
						return candidate;
					}
				}
			}
			return null;
		}
	}
}
