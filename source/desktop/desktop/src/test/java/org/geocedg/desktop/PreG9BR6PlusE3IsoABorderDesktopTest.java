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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.geocedg.common.export.GeometryExportArea;
import org.geocedg.common.kernel.sheet.AlgoIsoABorder;
import org.geocedg.common.kernel.sheet.IsoASheet;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geocedg.desktop.export.DrawingScale;
import org.geocedg.desktop.export.ExportArea;
import org.geocedg.desktop.export.ExportAreaSession;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.Hits;
import org.geogebra.common.export.pstricks.ExportFrameMinimal;
import org.geogebra.common.export.pstricks.GeoGebraExport;
import org.geogebra.common.gui.toolbar.ToolBar;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoPolyLine;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.main.GeoGebraPreferencesD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.desktop.util.LoggerD;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-E3 Desktop contract of the ISO A sheet: menu-only placement, the
 * tool with its unit gate, one-shot capture, dialog defaults, frame rules and cleanup;
 * the ISO_A_BORDER and ISO_A_SELECTION producers with AQ-X2 precedence and the
 * identity-only link lifecycle; physical-size and scale-label coherence, "Use sheet
 * scale" and the activation warning; physical export; the transient indicators; the
 * Classic surface; legacy coexistence and EN/ES names. Every scenario runs in a fresh
 * JVM of the test classpath, so the complete Desktop suite retains none of its hosts.
 */
class PreG9BR6PlusE3IsoABorderDesktopTest {

	/** Scenario prefix that executes GGBScript matrix rows of one test method. */
	static final String MATRIX_ROWS = "matrixRows-";
	private static final int SCENARIO_TIMEOUT_SECONDS = 300;
	private static final String DASH = String.valueOf((char) 0x2014);

	@TempDir
	Path temporary;

	@Test
	void profileMenuOnlyPlacementAndNames() throws Exception {
		isolated(temporary, "profile");
	}

	@Test
	void toolCreatesOneSheetWithOneUndoPoint() throws Exception {
		isolated(temporary, "tool");
	}

	@Test
	void unitGateBlocksCreationAndOffersDocumentUnits() throws Exception {
		isolated(temporary, "gate");
	}

	@Test
	void creationTimeCaptureForEveryUnit() throws Exception {
		isolated(temporary, "capture");
	}

	@Test
	void cancelAndFrameRules() throws Exception {
		isolated(temporary, "frame");
	}

	@Test
	void explicitLinkLifecycleByIdentity() throws Exception {
		isolated(temporary, "link");
	}

	@Test
	void producerPrecedenceAndIsoASelection() throws Exception {
		isolated(temporary, "precedence");
	}

	@Test
	void coherenceUseSheetScaleAndActivationWarning() throws Exception {
		isolated(temporary, "coherence");
	}

	@Test
	void physicalExportOfTheLinkedPaper() throws Exception {
		isolated(temporary, "export");
	}

	@Test
	void indicatorsAreTransientPresentation() throws Exception {
		isolated(temporary, "indicators");
	}

	@Test
	void classicSurfaceHasNoSheetGui() throws Exception {
		isolated(temporary, "classic");
	}

	@Test
	void legacyMacrosCoexist() throws Exception {
		isolated(temporary, "legacy");
	}

	@Test
	void englishAndSpanishNames() throws Exception {
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
			watchdog(args[0], Paths.get(args[2]), notes);
			try {
				AwtFactory.setPrototypeIfNull(new AwtFactoryD());
				Log.setLogger(new LoggerD());
				Path dir = Paths.get(args[1]);
				GeoGebraPreferencesD.setPropertyFileName(
						dir.resolve("e3-preferences.properties").toString());
				GeoCeDGUnitPreferences.useStoreForTesting(
						new PreG9BR6PlusD1DocumentUnitsTest.MemoryStore());
				Scenarios scenarios = new Scenarios(dir, notes);
				if (args[0].startsWith(MATRIX_ROWS)) {
					PreG9BR6CapabilityMatrixTest.executeRows(
							args[0].substring(MATRIX_ROWS.length()), dir);
				} else {
					Scenarios.class.getDeclaredMethod(args[0]).invoke(scenarios);
				}
				result = "SCENARIO OK " + args[0] + "\n" + notes;
			} catch (Throwable thrown) {
				Throwable failure = thrown instanceof java.lang.reflect.InvocationTargetException
						&& thrown.getCause() != null ? thrown.getCause() : thrown;
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

	/** Records every thread stack shortly before the parent's timeout, then halts. */
	private static void watchdog(String scenario, Path out, StringBuilder notes) {
		Thread watchdog = new Thread(() -> {
			try {
				Thread.sleep(TimeUnit.SECONDS.toMillis(SCENARIO_TIMEOUT_SECONDS - 30));
			} catch (InterruptedException e) {
				return;
			}
			StringBuilder text = new StringBuilder("SCENARIO HUNG " + scenario + "\n" + notes);
			Thread.getAllStackTraces().forEach((thread, stack) -> {
				text.append("\n\"").append(thread.getName()).append("\" ")
						.append(thread.getState());
				for (StackTraceElement element : stack) {
					text.append("\n    at ").append(element);
				}
			});
			try {
				Files.write(out, text.toString().getBytes(StandardCharsets.UTF_8));
			} catch (Exception ignored) {
				// the parent reports the missing record
			}
			Runtime.getRuntime().halt(1);
		}, "scenario-watchdog");
		watchdog.setDaemon(true);
		watchdog.start();
	}

	/** Scripted interaction: no modal dialog ever opens. */
	static final class FakePrompt implements GeoCeDGIsoABorderPrompt {
		SheetRequest sheet;
		SheetRequest offered;
		SelectionRequest selection;
		boolean activate;
		boolean chooseAction;
		final List<String> informed = new ArrayList<>();
		final List<String> actions = new ArrayList<>();
		final List<String> questions = new ArrayList<>();

		@Override
		public SheetRequest askSheet(AppGeoCeDG app, SheetRequest initial) {
			offered = initial;
			return sheet;
		}

		@Override
		public SelectionRequest askSelection(AppGeoCeDG app, SelectionRequest initial) {
			return selection;
		}

		@Override
		public boolean confirm(AppGeoCeDG app, String message, String title) {
			questions.add(message);
			return activate;
		}

		@Override
		public boolean inform(AppGeoCeDG app, String message, String title, String action) {
			informed.add(message);
			actions.add(action);
			return action != null && chooseAction;
		}
	}

	/** The scenarios; each runs alone in its JVM (invoked reflectively by name). */
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

		private static AppGeoCeDG app(UnitState units, DrawingScale scale) throws Exception {
			AppGeoCeDG app = G9U1TestApp.create();
			EuclidianView view = app.getEuclidianView1();
			SwingUtilities.invokeAndWait(() -> {
				((EuclidianViewD) view).getJPanel().setSize(800, 600);
				view.updateSize();
				view.setCoordSystem(400, 300, 0.02, 0.02);
				view.setShowAxes(false, false);
				view.showGrid(false);
			});
			app.getDocumentUnits().replace(units);
			app.setDrawingScale(scale);
			return app;
		}

		private static UnitState mm() {
			return UnitState.of(UnitToken.MM, null, null);
		}

		private static FakePrompt prompt(AppGeoCeDG app, int index, boolean landscape,
				DrawingScale scale, boolean frame, boolean label, boolean activate) {
			FakePrompt prompt = new FakePrompt();
			prompt.sheet = new GeoCeDGIsoABorderPrompt.SheetRequest(index, landscape, scale,
					frame, label);
			prompt.activate = activate;
			app.setIsoABorderPrompt(prompt);
			return prompt;
		}

		private static GeoElement eval(AppGeoCeDG app, String command) {
			return G9U1TestApp.eval(app, command);
		}

		private static GeoCeDGEuclidianController controller(AppGeoCeDG app) {
			return (GeoCeDGEuclidianController) app.getEuclidianView1()
					.getEuclidianController();
		}

		private static boolean click(GeoCeDGEuclidianController controller, GeoElement geo,
				AtomicInteger commits) {
			Hits hits = new Hits();
			if (geo != null) {
				hits.add(geo);
			}
			return controller.processMode(hits, false, false, changed -> {
				if (changed) {
					commits.incrementAndGet();
					controller.getApplication().storeUndoInfoAndStateForModeStarting(true);
				}
			});
		}

		private static List<AlgoIsoABorder> sheets(AppGeoCeDG app) {
			List<AlgoIsoABorder> found = new ArrayList<>();
			for (GeoElement geo : app.getKernel().getConstruction()
					.getGeoSetConstructionOrder()) {
				AlgoIsoABorder owner = AlgoIsoABorder.ownerOf(geo);
				if (owner != null && !found.contains(owner)) {
					found.add(owner);
				}
			}
			return found;
		}

		private static AlgoIsoABorder only(AppGeoCeDG app) {
			List<AlgoIsoABorder> found = sheets(app);
			assertEquals(1, found.size(), "one sheet");
			return found.get(0);
		}

		private static GeoElement[] typed(AppGeoCeDG app, String command) {
			ErrorAccumulator errors = new ErrorAccumulator();
			GeoElementND[] result = app.getKernel().getAlgebraProcessor()
					.processAlgebraCommandNoExceptionHandling(command, false, errors, false,
							null);
			assertNotNull(result, command + ": " + errors.getErrors());
			GeoElement[] out = new GeoElement[result.length];
			for (int i = 0; i < result.length; i++) {
				out[i] = result[i].toGeoElement();
			}
			return out;
		}

		private static ExportArea area(AppGeoCeDG app) {
			return app.getExportAreaSession().resolve(app.getEuclidianView1());
		}

		// ---------------------------------------------------------------- profile

		void profile() throws Exception {
			final AppGeoCeDG app = app(mm(), DrawingScale.ONE_TO_ONE);
			GeoCeDGProfile.ActionDefinition border =
					GeoCeDGProfile.getAction("presentation.iso-a-border");
			assertEquals("upstream-mode", border.kind());
			assertEquals(144, EuclidianConstants.MODE_ISO_A_BORDER);
			assertEquals(EuclidianConstants.MODE_ISO_A_BORDER, border.mode().intValue());
			assertEquals("product-action",
					GeoCeDGProfile.getAction("export.area.iso-a").kind());
			assertEquals("product-action",
					GeoCeDGProfile.getAction("export.area.use-iso-a-border").kind());
			assertEquals(130, GeoCeDGProfile.getActions().size());
			JSONObject profile = new JSONObject(Files.readString(
					PreG9BR6PlusE1LCuratedLibraryTest.repositoryRoot()
							.resolve("apps/geocedg/application-profile.yml")));
			JSONArray groups = profile.getJSONArray("presentation_groups");
			boolean placed = false;
			for (int i = 0; i < groups.length(); i++) {
				JSONObject group = groups.getJSONObject(i);
				String ids = group.getJSONArray("action_ids").toString();
				String toolbar = group.getJSONArray("toolbar_action_ids").toString();
				assertFalse(toolbar.contains("iso-a"), "no toolbar button: " + group);
				if ("construction-annotations-media".equals(group.getString("id"))) {
					assertEquals("[\"presentation.image\",\"presentation.iso-a-border\"]", ids);
					placed = true;
				}
				if ("file-export-area".equals(group.getString("id"))) {
					assertTrue(ids.contains("export.area.iso-a")
							&& ids.contains("export.area.use-iso-a-border"), ids);
				}
			}
			assertTrue(placed);
			app.setLocale(Locale.ENGLISH);
			assertEquals("ISO A Border", app.getToolName(144));
			assertTrue(app.getToolHelp(144).startsWith("Click a point"));
			app.setLanguage("es");
			assertEquals("Marco ISO A", app.getToolName(144));
			GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
			assertNotNull(registry.get("presentation.iso-a-border"));
			assertEquals("Definir área de exportación ISO A…",
					registry.get("export.area.iso-a").getValue(javax.swing.Action.NAME));
		}

		// ------------------------------------------------------------------- tool

		void tool() throws Exception {
			AppGeoCeDG app = app(mm(), DrawingScale.of(1, 50));
			FakePrompt prompt = prompt(app, 3, true, DrawingScale.of(1, 50), true, true,
					false);
			GeoPoint p = (GeoPoint) eval(app, "P=(10,20)");
			app.setMode(EuclidianConstants.MODE_ISO_A_BORDER);
			AtomicInteger commits = new AtomicInteger();
			assertTrue(click(controller(app), p, commits));
			assertEquals(1, commits.get(), "one undo point");
			assertEquals(EuclidianConstants.MODE_MOVE, app.getMode(), "one-shot mode");
			AlgoIsoABorder algo = only(app);
			assertEquals(DrawingScale.of(1, 50), prompt.offered.scale(),
					"the dialog starts from the session scale");
			assertEquals(3, prompt.offered.index());
			assertTrue(prompt.offered.innerFrame(), "A3: frame on by default");
			assertEquals(1, prompt.questions.size(), "the yes/no activation question");
			assertNull(app.getExportAreaSession().getExplicitProducer(),
					"creation never replaces the export area");
			double[] bounds = algo.getPaperBounds();
			assertEquals(10, bounds[0], 0);
			assertEquals(10 + 21000, bounds[1], 0);
			assertEquals(20 - 14850, bounds[2], 0);
			assertEquals(1, algo.getUnitFactor(), 0);
			assertEquals("A3 " + DASH + " 1:50", algo.getLabelText().getTextString());
			assertFalse(algo.getPaper().isEuclidianVisible(), "paper hidden");
			assertTrue(algo.getFrame().isEuclidianVisible() && algo.getFrame().isDefined());
			assertTrue(p.isEuclidianVisible(), "an existing point keeps its presentation");
			String xml = app.getXML();
			assertTrue(xml.contains("<input a0=\"P\" a1=\"3\" a2=\"true\" a3=\"1\" a4=\"50\""
					+ " a5=\"1.0\" a6=\"true\"/>"), xml);
			app.getKernel().undo();
			assertEquals(0, sheets(app).size(), "undo removes the whole sheet");
			app.getKernel().redo();
			assertEquals(1, sheets(app).size());
			// a single preselected point skips the click; the registry stores one undo point
			GeoPoint q = (GeoPoint) eval(app, "Q=(0,0)");
			app.getSelectionManager().clearSelectedGeos();
			app.getSelectionManager().addSelectedGeo(q);
			prompt.sheet = new GeoCeDGIsoABorderPrompt.SheetRequest(4, false,
					DrawingScale.of(1, 10), true, false);
			new GeoCeDGActionRegistry(app).invoke("presentation.iso-a-border",
					new ActionEvent(app, 0, "test"));
			assertEquals(2, sheets(app).size());
			AlgoIsoABorder second = AlgoIsoABorder.ownerOf(app.getKernel().getConstruction()
					.getLastGeoElement());
			assertNotNull(second);
			assertFalse(second.getLabelText().isEuclidianVisible(), "label off on request");
			note("tool XML " + xml.substring(xml.indexOf("<command name=\"IsoABorder\">"),
					xml.indexOf("</command>", xml.indexOf("<command name=\"IsoABorder\">"))));
		}

		void gate() throws Exception {
			AppGeoCeDG app = app(UnitState.EMPTY, DrawingScale.ONE_TO_ONE);
			FakePrompt prompt = prompt(app, 3, true, DrawingScale.ONE_TO_ONE, true, true,
					false);
			GeoPoint p = (GeoPoint) eval(app, "P=(0,0)");
			assertNull(app.getIsoABorderTool().create(p, true));
			assertEquals(1, prompt.informed.size());
			assertTrue(prompt.informed.get(0).contains("Document Units"),
					prompt.informed.get(0));
			assertNotNull(prompt.actions.get(0), "offers Document Units");
			assertTrue(sheets(app).isEmpty());
			assertNull(app.getKernel().lookupLabel("P"), "the click point is removed");
			GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
			String reason = registry.unavailableReason("presentation.iso-a-border");
			assertNotNull(reason, "unavailable without a unit");
			assertTrue(reason.contains("Document Units"), reason);
			assertEquals(reason, registry.unavailableReason("export.area.iso-a"));
			assertNull(registry.unavailableReason("export.area.use-iso-a-border"));
			registry.refresh();
			assertFalse(registry.get("presentation.iso-a-border").isEnabled());
			assertEquals(reason, registry.get("presentation.iso-a-border")
					.getValue(javax.swing.Action.SHORT_DESCRIPTION), "the reason is shown");
			registry.invoke("presentation.iso-a-border", new ActionEvent(app, 0, "test"));
			assertEquals(2, prompt.informed.size(), "an invocation repeats the reason");
			assertNotNull(prompt.actions.get(1), "with the Document Units route");
			assertTrue(app.getMode() != EuclidianConstants.MODE_ISO_A_BORDER,
					"the mode is not entered without a unit");
			assertFalse(app.defineIsoASelection(), "ISO_A_SELECTION is gated too");
			app.getDocumentUnits().replace(UnitState.of(UnitToken.USM, null,
					UsmDefinition.of(0.0254, "inch", "in")));
			assertNull(registry.unavailableReason("presentation.iso-a-border"),
					"a valid usm enables the action");
			// a valid but extreme usm whose millimetre factor overflows is refused
			app.getDocumentUnits().replace(UnitState.of(UnitToken.USM, null,
					UsmDefinition.of(1E-313, "tiny", "t")));
			prompt.informed.clear();
			GeoPoint q = (GeoPoint) eval(app, "Q=(0,0)");
			assertNull(app.getIsoABorderTool().create(q, false));
			assertTrue(prompt.informed.get(0).contains("not representable"),
					prompt.informed.get(0));
			assertNotNull(app.getKernel().lookupLabel("Q"), "an existing point is kept");
			// a typed command stays a pure function of its inputs
			app.getDocumentUnits().replace(UnitState.EMPTY);
			GeoElement[] out = typed(app, "IsoABorder(Q,3,true,1,1,1,true)");
			assertTrue(out[0].isDefined());
			assertSame(IsoASheet.PhysicalCoherence.NOT_DETERMINABLE,
					app.sheetCoherence(AlgoIsoABorder.ownerOf(out[0])).getPhysical());
		}

		void capture() throws Exception {
			Object[][] cases = {
				{UnitState.of(UnitToken.CM, null, null), 0.001 / 0.01},
				{UnitState.of(UnitToken.M, null, null), 0.001 / 1.0},
				{UnitState.of(UnitToken.USM, null, UsmDefinition.of(0.0254, "inch", "in")),
						0.001 / 0.0254},
				{UnitState.of(UnitToken.USM, null, UsmDefinition.of(1E305, "huge", "h")),
						0.001 / 1E305}};
			for (Object[] item : cases) {
				AppGeoCeDG app = app((UnitState) item[0], DrawingScale.of(1, 2));
				prompt(app, 4, false, DrawingScale.of(1, 2), true, true, false);
				// the origin keeps even the 1e-308-sized sheet unabsorbed
				GeoPoint p = (GeoPoint) eval(app, "P=(0,0)");
				AlgoIsoABorder algo = AlgoIsoABorder.ownerOf(
						app.getIsoABorderTool().create(p, false)[0]);
				double expected = (Double) item[1];
				assertEquals(Double.doubleToLongBits(expected),
						Double.doubleToLongBits(algo.getUnitFactor()), item[0].toString());
				double[] before = algo.getPaperBounds();
				File file = dir.resolve("capture.cedg").toFile();
				assertTrue(app.saveGeoGebraFile(file));
				AppGeoCeDG reopened = G9U1TestApp.create();
				assertTrue(reopened.loadFile(file, false));
				AlgoIsoABorder again = only(reopened);
				double[] after = again.getPaperBounds();
				for (int i = 0; i < 4; i++) {
					assertEquals(Double.doubleToLongBits(before[i]),
							Double.doubleToLongBits(after[i]), "reopen " + item[0] + " " + i);
				}
				note("captured u " + item[0] + " = " + algo.getUnitFactor() + " reliability "
						+ algo.getReliability());
				if (expected < Double.MIN_NORMAL) {
					assertSame(IsoASheet.Reliability.UNGUARANTEED, algo.getReliability());
				}
				// one-shot capture: later unit and scale changes change nothing
				app.getDocumentUnits().replace(mm());
				app.setDrawingScale(DrawingScale.of(1, 100));
				assertEquals(Double.doubleToLongBits(expected),
						Double.doubleToLongBits(algo.getUnitFactor()));
				assertEquals(before[1], algo.getPaperBounds()[1], 0);
			}
		}

		void frame() throws Exception {
			AppGeoCeDG app = app(mm(), DrawingScale.ONE_TO_ONE);
			FakePrompt prompt = prompt(app, 3, true, DrawingScale.ONE_TO_ONE, true, true,
					true);
			prompt.sheet = null;
			GeoPoint created = (GeoPoint) eval(app, "C=(0,0)");
			assertNull(app.getIsoABorderTool().create(created, true), "cancelled");
			assertNull(app.getKernel().lookupLabel("C"), "click point removed on cancel");
			assertTrue(sheets(app).isEmpty());
			assertEquals(0, prompt.questions.size(), "nothing to activate");
			// A10 portrait: an impossible frame is refused, never reduced
			prompt.sheet = new GeoCeDGIsoABorderPrompt.SheetRequest(10, false,
					DrawingScale.ONE_TO_ONE, true, true);
			prompt.activate = false;
			GeoPoint p = (GeoPoint) eval(app, "P=(0,0)");
			AlgoIsoABorder a10 = AlgoIsoABorder.ownerOf(app.getIsoABorderTool().create(p,
					true)[0]);
			assertFalse(a10.getFrame().isDefined());
			assertTrue(a10.getFrame().isAuxiliaryObject(), "OTQ-E3-2: auxiliary, undefined");
			assertTrue(a10.getPaper().isDefined());
			assertTrue(a10.getLabelText().isDefined());
			assertFalse(p.isEuclidianVisible(), "a click-created corner point is hidden");
			assertTrue(IsoASheet.isFrameDefault(4) && !IsoASheet.isFrameDefault(5));
		}

		// ------------------------------------------------------------------- link

		void link() throws Exception {
			AppGeoCeDG app = app(mm(), DrawingScale.of(1, 50));
			app.getKernel().setUndoActive(true);
			FakePrompt prompt = prompt(app, 3, true, DrawingScale.of(1, 50), true, true,
					true);
			GeoPoint p = (GeoPoint) eval(app, "P=(0,0)");
			AlgoIsoABorder algo = AlgoIsoABorder.ownerOf(app.getIsoABorderTool().create(p,
					false)[0]);
			assertTrue(prompt.informed.isEmpty(), "coherent: no warning");
			ExportAreaSession session = app.getExportAreaSession();
			assertSame(ExportAreaSession.Producer.ISO_A_BORDER, session.getExplicitProducer());
			ExportArea linked = area(app);
			assertSame(ExportArea.Source.ISO_A_BORDER, linked.getSource());
			assertEquals(21000, linked.getXmax() - linked.getXmin(), 0, "paper, not frame");
			assertFalse(algo.getPaper().isEuclidianVisible(), "hidden paper still serves");
			p.setCoords(100, 200, 1);
			p.updateCascade();
			assertEquals(100, area(app).getXmin(), 0, "live while valid");
			// undefined but live: unavailable, link kept, recovered
			GeoNumeric u = (GeoNumeric) eval(app, "uu=1");
			GeoElement[] typedSheet = typed(app, "IsoABorder(P,4,true,1,1,uu,false)");
			AlgoIsoABorder typedAlgo = AlgoIsoABorder.ownerOf(typedSheet[0]);
			assertTrue(app.activateIsoABorder(typedAlgo));
			u.setValue(-1);
			u.updateCascade();
			assertSame(ExportArea.Source.VISIBLE_VIEWPORT, area(app).getSource());
			assertTrue(session.isExplicitProducerUnavailable(app.getEuclidianView1()));
			assertSame(ExportAreaSession.Producer.ISO_A_BORDER, session.getExplicitProducer());
			u.setValue(1);
			u.updateCascade();
			assertSame(ExportArea.Source.ISO_A_BORDER, area(app).getSource());
			// deletion: stale, dropped, never re-resolved
			typedSheet[0].remove();
			assertTrue(session.validateLink());
			assertTrue(session.isLinkLost());
			assertNull(session.getExplicitProducer());
			assertSame(ExportArea.Source.VISIBLE_VIEWPORT, area(app).getSource());
			// undo rebuilds the construction: the same label is a different object
			UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction()
					.getUndoManager();
			try (UndoManagerD.PreparedUndoBaseline baseline = undo.prepareUndoBaseline()) {
				undo.commitUndoBaseline(baseline);
			}
			CountDownLatch stored = new CountDownLatch(1);
			undo.addUndoInfoStoredListener(stored::countDown);
			eval(app, "zz=5");
			app.storeUndoInfo();
			assertTrue(stored.await(5, TimeUnit.SECONDS), "undo point stored");
			assertTrue(app.activateIsoABorder(algo));
			String label = algo.getPaper().getLabelSimple();
			app.getKernel().undo();
			assertNull(app.getKernel().lookupLabel("zz"), "undo restored the baseline");
			GeoElement rebuilt = app.getKernel().lookupLabel(label);
			assertNotNull(rebuilt, "the rebuilt sheet exists under the same label");
			assertFalse(rebuilt == algo.getPaper());
			assertTrue(session.validateLink(), "identity test drops the stale link");
			assertNull(session.getLinkedBorder(), "never re-resolved by label");
			// New resets the session
			assertTrue(app.activateIsoABorder(AlgoIsoABorder.ownerOf(rebuilt)));
			app.setSaved();
			app.clearConstruction();
			assertNull(session.getExplicitProducer());
			assertFalse(session.isLinkLost());
		}

		void precedence() throws Exception {
			AppGeoCeDG app = app(mm(), DrawingScale.of(1, 10));
			prompt(app, 4, false, DrawingScale.of(1, 10), true, true, false);
			eval(app, "Export_1=(-5,-5)");
			eval(app, "Export_2=(5,5)");
			assertSame(ExportArea.Source.EXPORT_POINTS_AUTOMATIC, area(app).getSource());
			GeoPoint p = (GeoPoint) eval(app, "P=(10,20)");
			AlgoIsoABorder algo = AlgoIsoABorder.ownerOf(app.getIsoABorderTool().create(p,
					false)[0]);
			assertSame(ExportArea.Source.EXPORT_POINTS_AUTOMATIC, area(app).getSource(),
					"no automatic replacement");
			assertTrue(app.activateIsoABorder(algo));
			assertSame(ExportArea.Source.ISO_A_BORDER, area(app).getSource(),
					"explicit producer > Export_1/Export_2");
			app.clearExportArea();
			assertSame(ExportArea.Source.EXPORT_POINTS_AUTOMATIC, area(app).getSource());
			// ISO_A_SELECTION: computed once at the session scale and unit
			FakePrompt selection = (FakePrompt) app.getIsoABorderPrompt();
			selection.selection = new GeoCeDGIsoABorderPrompt.SelectionRequest(10, 20, 4,
					false);
			assertTrue(app.activateIsoABorder(algo));
			assertTrue(app.defineIsoASelection());
			ExportArea iso = area(app);
			assertSame(ExportArea.Source.ISO_A_SELECTION, iso.getSource());
			assertSame(ExportAreaSession.Producer.ISO_A_SELECTION,
					app.getExportAreaSession().getExplicitProducer(), "replaces the link");
			assertNull(app.getExportAreaSession().getLinkedBorder());
			assertEquals(10, iso.getXmin(), 0);
			assertEquals(10 + 2100, iso.getXmax(), 0);
			assertEquals(20 - 2970, iso.getYmin(), 0);
			app.setDrawingScale(DrawingScale.of(1, 20));
			assertEquals(10 + 2100, area(app).getXmax(), 0, "values, computed once");
			assertTrue(app.useExportPointsArea());
			assertSame(ExportArea.Source.EXPORT_POINTS_EXPLICIT, area(app).getSource());
			// DXF consumes the same authority with the new producer values
			assertTrue(app.activateIsoABorder(algo));
			GeometryExportArea dxf = new GeoCeDGDxfExportController(app).exportContext()
					.getArea();
			assertSame(GeometryExportArea.Producer.ISO_A_BORDER, dxf.getProducer());
			assertTrue(dxf.isBoundary());
		}

		// -------------------------------------------------------------- coherence

		void coherence() throws Exception {
			AppGeoCeDG app = app(mm(), DrawingScale.of(1, 100));
			FakePrompt prompt = prompt(app, 3, true, DrawingScale.of(1, 50), true, true,
					true);
			prompt.chooseAction = false;
			GeoPoint p = (GeoPoint) eval(app, "P=(0,0)");
			AlgoIsoABorder algo = AlgoIsoABorder.ownerOf(app.getIsoABorderTool().create(p,
					false)[0]);
			assertEquals(DrawingScale.of(1, 100), app.getDrawingScale(),
					"the tool never changes the session scale");
			assertEquals(1, prompt.informed.size(), "the activation warning");
			String warning = prompt.informed.get(0);
			note("warning " + warning);
			assertTrue(warning.contains("210 " + (char) 0xd7 + " 148.5")
					&& warning.contains("420 " + (char) 0xd7 + " 297"), warning);
			assertNotNull(prompt.actions.get(0), "Use sheet scale offered");
			IsoASheet.Coherence before = app.sheetCoherence(algo);
			assertSame(IsoASheet.PhysicalCoherence.INCOHERENT, before.getPhysical());
			assertSame(IsoASheet.ScaleCoherence.DIFFERENT, before.getScale());
			double[] geometry = algo.getPaperBounds();
			assertTrue(app.useSheetScale());
			assertEquals(DrawingScale.of(1, 50), app.getDrawingScale());
			assertSame(IsoASheet.PhysicalCoherence.COHERENT,
					app.sheetCoherence(algo).getPhysical());
			assertEquals(geometry[1], algo.getPaperBounds()[1], 0, "geometry unchanged");
			// a later unit change: the scale cannot fix it, and nothing claims it does
			app.getDocumentUnits().replace(UnitState.of(UnitToken.CM, null, null));
			IsoASheet.Coherence cm = app.sheetCoherence(algo);
			assertSame(IsoASheet.PhysicalCoherence.INCOHERENT, cm.getPhysical());
			assertSame(IsoASheet.ScaleCoherence.MATCH, cm.getScale());
			assertFalse(app.useSheetScale(), "not offered when the scale already matches");
			String message = app.sheetCoherenceMessage(algo, cm);
			assertTrue(message.contains("construction unit"), message);
			assertEquals(geometry[1], algo.getPaperBounds()[1], 0);
			// physical coherence can hold while the label scale differs
			app.setDrawingScale(DrawingScale.of(1, 500));
			IsoASheet.Coherence cm500 = app.sheetCoherence(algo);
			assertSame(IsoASheet.PhysicalCoherence.COHERENT, cm500.getPhysical());
			assertSame(IsoASheet.ScaleCoherence.DIFFERENT, cm500.getScale());
			app.getDocumentUnits().replace(UnitState.EMPTY);
			IsoASheet.Coherence none = app.sheetCoherence(algo);
			assertSame(IsoASheet.PhysicalCoherence.NOT_DETERMINABLE, none.getPhysical());
			assertSame(IsoASheet.ScaleCoherence.NOT_APPLICABLE, none.getScale());
			assertEquals("A3 " + DASH + " 1:50", algo.getLabelText().getTextString(),
					"the label keeps the captured scale");
		}

		void export() throws Exception {
			AppGeoCeDG app = app(mm(), DrawingScale.of(1, 50));
			prompt(app, 3, true, DrawingScale.of(1, 50), true, true, true);
			GeoPoint p = (GeoPoint) eval(app, "P=(37.5,-12.25)");
			app.getIsoABorderTool().create(p, false);
			EuclidianView view = app.getEuclidianView1();
			double[] box = PreG9BR6PlusBPictureFidelityTest.mediaBox(
					PreG9BR6PlusBPictureFidelityTest.pdf(app, view));
			assertEquals(420 * 72 / 25.4, box[2] - box[0], 0.001, "PDF width");
			assertEquals(297 * 72 / 25.4, box[3] - box[1], 0.001, "PDF height");
			String svg = PreG9BR6PlusBPictureFidelityTest.svg(app, view, -1);
			assertTrue(svg.contains("width=\"42cm\"") || svg.contains("width=\"42.0cm\""),
					svg.substring(0, Math.min(400, svg.length())));
			// every route consumes the same live PAPER rectangle
			ExportArea paper = area(app);
			assertSame(ExportArea.Source.ISO_A_BORDER, paper.getSource());
			BufferedImage png = PreG9BR6PlusBPictureFidelityTest.png(app, view,
					1);
			assertEquals(Math.round(paper.pixelWidth(view.getXscale())), png.getWidth(), 1,
					"PNG width");
			assertEquals(Math.round(paper.pixelHeight(view.getYscale())), png.getHeight(), 1,
					"PNG height");
			AtomicReference<GeoGebraExport> pgf = new AtomicReference<>();
			app.newGeoGebraToPgf(pgf::set);
			ExportFrameMinimal frame = new ExportFrameMinimal(view.getYmin(), view.getYmax());
			pgf.get().setFrame(frame);
			pgf.get().generateAllCode();
			String latex = frame.getCode();
			assertTrue(latex.contains("21037.5") && latex.contains("-14862.25"),
					"PGF bounds are the paper: " + latex.substring(0,
							Math.min(800, latex.length())));
			GeometryExportArea dxf = new GeoCeDGDxfExportController(app).exportContext()
					.getArea();
			assertSame(GeometryExportArea.Producer.ISO_A_BORDER, dxf.getProducer());
			assertEquals(37.5, dxf.getXmin(), 0);
			assertEquals(37.5 + 21000, dxf.getXmax(), 0);
			assertEquals(-12.25 - 14850, dxf.getYmin(), 0);
			assertEquals(-12.25, dxf.getYmax(), 0);
			app.setDrawingScale(DrawingScale.of(1, 100));
			box = PreG9BR6PlusBPictureFidelityTest.mediaBox(
					PreG9BR6PlusBPictureFidelityTest.pdf(app, view));
			assertEquals(210 * 72 / 25.4, box[2] - box[0], 0.001, "other scale: scaled page");
			String xml = app.getXML();
			assertFalse(xml.contains("ISO_A") || xml.contains("iso_a"),
					"no export-area state in the document");
			note("PDF 1:100 " + (box[2] - box[0]) + " x " + (box[3] - box[1]) + " pt");
		}

		void indicators() throws Exception {
			AppGeoCeDG app = app(mm(), DrawingScale.of(1, 100));
			final FakePrompt prompt = prompt(app, 3, true, DrawingScale.of(1, 50), true, true,
					true);
			GeoCeDGStatusBar bar = app.getStatusBar();
			assertFalse(bar.getSegment(GeoCeDGStatusBar.SHEET_SEGMENT).isVisible());
			GeoPoint p = (GeoPoint) eval(app, "P=(0,0)");
			final AlgoIsoABorder algo = AlgoIsoABorder.ownerOf(app.getIsoABorderTool().create(p,
					false)[0]);
			bar.updateText();
			String text = bar.getSegment(GeoCeDGStatusBar.SHEET_SEGMENT).getText();
			note("status " + text);
			assertTrue(bar.getSegment(GeoCeDGStatusBar.SHEET_SEGMENT).isVisible());
			assertTrue(text.contains("A3") && text.contains("210 " + (char) 0xd7 + " 148.5")
					&& text.contains("1:100") && text.contains("1:50"), text);
			JComponent control = new GeoCeDGExportScalePresentation(app)
					.createScaleControl(() -> { });
			GeoCeDGExportScalePresentation.SheetNotice notice = find(control,
					GeoCeDGExportScalePresentation.SheetNotice.class);
			assertNotNull(notice);
			assertTrue(notice.isVisible());
			JButton use = find(notice, JButton.class);
			assertTrue(use.isVisible(), "Use sheet scale offered while scales differ");
			use.doClick();
			assertEquals(DrawingScale.of(1, 50), app.getDrawingScale(), "explicit action");
			notice.refresh();
			assertFalse(use.isVisible());
			String xml = app.getXML();
			assertFalse(xml.contains("sheet-coherence") || xml.contains("Use sheet scale"));
			algo.getPaper().remove();
			app.getExportAreaSession().validateLink();
			bar.updateText();
			assertTrue(bar.getSegment(GeoCeDGStatusBar.SHEET_SEGMENT).getText()
					.contains("link"), "lost link reported");
			assertTrue(prompt.questions.size() >= 1);
		}

		private static <T> T find(Component root, Class<T> type) {
			if (type.isInstance(root)) {
				return type.cast(root);
			}
			if (root instanceof Container container) {
				for (Component child : container.getComponents()) {
					T found = find(child, type);
					if (found != null) {
						return found;
					}
				}
			}
			return null;
		}

		// ---------------------------------------------------- classic and legacy

		void classic() throws Exception {
			AppGeoCeDG app = app(mm(), DrawingScale.ONE_TO_ONE);
			String classicTools = ToolBar.getAllToolsNoMacros(false, false, false);
			assertFalse((" " + classicTools + " ").contains(" 144 "),
					"no Classic toolbar entry for mode 144");
			// the command is the shared, ungated one in the GeoCeDG host too
			eval(app, "P=(0,0)");
			GeoElement[] out = typed(app, "IsoABorder(P,3,true,1,1,1,true)");
			assertTrue(out[0].getParentAlgorithm() instanceof AlgoIsoABorder);
			assertTrue(app.getKernel().getAlgebraProcessor().getCommandDispatcher()
					.isAllowedByCommandFilters(org.geogebra.common.kernel.commands.Commands
							.IsoABorder), "no feature gate");
		}

		void legacy() throws Exception {
			Path original = PreG9BR6PlusE1LCuratedLibraryTest.repositoryRoot()
					.resolve("models/legacy/template-v7/original/Templatev7.ggb");
			assertEquals("f62e5b7a92bcd95f10b8afda348763a57ccbd0c10dbc0c2bccc7049831ed4113",
					HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
							.digest(Files.readAllBytes(original))), "historical model unchanged");
			AppGeoCeDG app = app(mm(), DrawingScale.ONE_TO_ONE);
			Path copy = dir.resolve("Templatev7.ggb");
			Files.copy(original, copy);
			app.setSaved();
			assertTrue(app.loadXML(copy.toFile(), false));
			Macro land = app.getKernel().getMacro("sheetISOAnLand");
			assertNotNull(land, "the legacy sheet macro still exists");
			eval(app, "P_9=(0,0)");
			GeoElement[] out = typed(app, "IsoABorder(P_9,3,true,1,1,1,true)");
			assertTrue(out[0].getParentAlgorithm() instanceof AlgoIsoABorder,
					"no collision with the 24 document macros");
			assertEquals("sheetISOAnLand", land.getCommandName(), "macro never renamed");
		}

		void names() throws Exception {
			AppGeoCeDG app = app(mm(), DrawingScale.ONE_TO_ONE);
			eval(app, "P_9=(0,0)");
			app.setLanguage("es");
			GeoElement[] es = typed(app, "MarcoISOA(P_9,4,false,1,1,1,false)");
			assertTrue(es[0].getParentAlgorithm() instanceof AlgoIsoABorder);
			String definition = es[0].getDefinition(StringTemplate.defaultTemplate);
			note("ES definition " + definition);
			assertTrue(definition.startsWith("IsoABorder("), definition);
			assertEquals("IsoABorder", app.getLocalization().getReverseCommand("MarcoISOA"));
			assertEquals("IsoABorder", app.getLocalization().getReverseCommand("marcoisoa"));
			app.setLanguage("en");
			assertEquals("IsoABorder( <Point>, <ISO A Index>, <Landscape>, <Scale Numerator>,"
					+ " <Scale Denominator>, <Model Units per Millimetre>, <Inner Frame> )",
					app.getLocalization().getCommandSyntax("IsoABorder"));
		}
	}
}
