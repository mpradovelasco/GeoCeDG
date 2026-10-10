/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.geocedg.common.export.DxfEncodingResult;
import org.geocedg.common.export.GeometryExportModel;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportPreflight;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.common.kernel.sheet.AlgoIsoABorder;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.desktop.export.DrawingScale;
import org.geocedg.desktop.export.DxfFidelityManifestWriter;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-E3-R1: interoperable DXF and LaTeX export of an ISO A sheet. The
 * DXF of the E3 sample is the complete AC1015 container around unchanged entities,
 * with the linked paper as export area (DXF-R1-03, -05); the LaTeX label is text mode
 * with {@code \textemdash{}} anchored up and left of its point (TEX-R1-01, -06); a
 * physical LaTeX export has the export area as its page, compiled with the installed
 * TeX toolchain, measured and rasterized: the page is the paper at the session scale
 * and the four frame edges and the label are drawn (TEX-R1-02 to -05, -08); output
 * without a sheet keeps the host representation (TEX-R1-07). A missing toolchain is
 * reported as unavailable, never replaced by text inspection.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusE3R1ExportInteroperabilityTest {

	private static final double BP_PER_MM = 72 / 25.4;
	private static final String PGF_LABEL =
			"\\draw (4100.,-2870.) node[anchor=south east] {A3 \\textemdash{} 1:10};";
	private static final String PSTRICKS_LABEL =
			"\\rput[br](4100.,-2870.){\\psframebox[linestyle=none,framesep=0.3333em]{A3 "
					+ "\\textemdash{} 1:10}}";
	private static final String ASYMPTOTE_LABEL =
			"label(\"A3 \\textemdash{} 1:10\", (4100.,-2870.), NW);";
	private final GeometryExportService service = new GeometryExportService();

	@TempDir
	Path temporary;

	// ------------------------------------------------------------------- DXF

	@Test
	void theE3SampleDxfHasTheContainerAndTheLinkedPaperAsExportArea() throws Exception {
		AppGeoCeDG app = sheet(5, true);
		eval(app, "A=(1000,-1000)");
		eval(app, "B=(3000,-2000)");
		eval(app, "c=Circle(A,500)");
		GeoCeDGDxfExportController controller = new GeoCeDGDxfExportController(app);
		List<GeoElement> sources = new ArrayList<>(app.getKernel().getConstruction()
				.getGeoSetConstructionOrder());
		GeometryExportModel model = service.createModel(sources,
				SelectionMode.COMPLETE_CONSTRUCTION, controller.exportContext());
		String dxf = service.exportDxf(model);
		List<String[]> pairs = pairs(dxf);
		assertEquals("4", header(pairs, "$INSUNITS"));
		assertTrue(dxf.startsWith("999\r\nGeoCeDG neutral 2D geometry export"));
		assertTrue(dxf.contains("ISO_A_BORDER [0.0, 4200.0] x [-2970.0, 0.0] of view 1"));
		for (String section : new String[] {"HEADER", "TABLES", "BLOCKS", "ENTITIES",
				"OBJECTS"}) {
			assertTrue(dxf.contains("0\r\nSECTION\r\n2\r\n" + section + "\r\n"), section);
		}
		for (String table : new String[] {"VPORT", "LTYPE", "LAYER", "STYLE", "VIEW", "UCS",
				"APPID", "DIMSTYLE", "BLOCK_RECORD"}) {
			assertTrue(dxf.contains("0\r\nTABLE\r\n2\r\n" + table + "\r\n5\r\n"), table);
		}
		assertUniqueHandlesBelowSeed(pairs);
		List<List<String[]>> entities = entities(pairs);
		List<String> types = new ArrayList<>();
		for (List<String[]> entity : entities) {
			types.add(entity.get(0)[1]);
		}
		assertEquals(List.of("POINT", "LWPOLYLINE", "LWPOLYLINE", "POINT", "POINT",
				"CIRCLE"), types, "construction order: P, the sheet, A, B, c");
		List<String[]> paper = entities.get(1);
		List<String[]> frame = entities.get(2);
		assertEquals("1", value(paper, 60), "the hidden paper stays, invisible");
		assertEquals(null, value(frame, 60), "the frame is visible");
		assertEquals(List.of(0.0, 4200.0, 4200.0, 0.0, 0.0), values(paper, 10));
		assertEquals(List.of(0.0, 0.0, -2970.0, -2970.0, 0.0), values(paper, 20));
		assertEquals(List.of(200.0, 4100.0, 4100.0, 200.0, 200.0), values(frame, 10));
		assertEquals(List.of(-100.0, -100.0, -2870.0, -2870.0, -100.0), values(frame, 20));
		assertEquals("0", value(paper, 8));

		GeometryExportPreflight preflight = service.preflight(sources,
				SelectionMode.COMPLETE_CONSTRUCTION, GeometryExportRequest.builder(0.01)
						.requestSidecar(true).build(), controller::exportContext);
		DxfEncodingResult encoding = service.encode(preflight);
		assertTrue(encoding.getDxfText().contains("0\r\nSECTION\r\n2\r\nOBJECTS\r\n"),
				"the extended route shares the container");
		String manifest = new String(new DxfFidelityManifestWriter().prepare(preflight,
				encoding).getManifest().getBytes(), StandardCharsets.UTF_8);
		assertTrue(manifest.contains("\"resolved_producer\":\"iso_a_border\""), manifest);
	}

	@Test
	void everyGeoCeDGLayerFitsBelowTheEntityHandles() {
		AppGeoCeDG app = G9U1TestApp.create();
		List<GeoElement> sources = new ArrayList<>();
		for (int layer = 0; layer < 100; layer++) {
			GeoElement segment = eval(app, "s_{" + layer + "}=Segment((0," + layer + "),(1,"
					+ layer + "))");
			segment.setLayer(layer);
			sources.add(segment);
		}
		List<String[]> pairs = pairs(service.exportDxf(service.createModel(sources,
				SelectionMode.CURRENT_SELECTION)));
		assertUniqueHandlesBelowSeed(pairs);
		Set<String> layers = new HashSet<>();
		boolean inLayers = false;
		for (int i = 0; i < pairs.size(); i++) {
			if ("2".equals(pairs.get(i)[0]) && "LAYER".equals(pairs.get(i)[1])) {
				inLayers = true;
			}
			if (inLayers && "0".equals(pairs.get(i)[0]) && "ENDTAB".equals(pairs.get(i)[1])) {
				break;
			}
			if (inLayers && "100".equals(pairs.get(i)[0])
					&& "AcDbLayerTableRecord".equals(pairs.get(i)[1])) {
				layers.add(pairs.get(i + 1)[1]);
			}
		}
		assertEquals(100, layers.size(), "layer 0 and GEOCEDG_L1 to GEOCEDG_L99");
	}

	// ----------------------------------------------------------------- LaTeX

	@Test
	void theSheetLabelIsTextModeAnchoredUpAndLeftOfItsPointInEnglishAndSpanish() {
		for (String language : new String[] {"en", "es"}) {
			AppGeoCeDG app = sheet(10, true);
			app.setLanguage(language);
			EuclidianView view = app.getEuclidianView1();
			String pgf = latex(app, view, "pgf");
			String pstricks = latex(app, view, "pstricks");
			String asymptote = latex(app, view, "asymptote");
			assertTrue(pgf.contains(PGF_LABEL), language + ": " + pgf);
			assertTrue(pstricks.contains(PSTRICKS_LABEL), language + ": " + pstricks);
			assertTrue(asymptote.contains(ASYMPTOTE_LABEL), language + ": " + asymptote);
			for (String output : new String[] {pgf, pstricks, asymptote}) {
				assertFalse(output.contains("\\emdash") || output.contains(" emdash"),
						"no undefined em dash: " + output);
			}
			assertEquals("A3 " + (char) 0x2014 + " 1:10", AlgoIsoABorder.ownerOf(
					app.getKernel().lookupLabel("sheet")).getLabelText().getTextString(),
					"the kernel label is unchanged");
		}
	}

	@Test
	void aPhysicalLatexExportHasTheExportAreaAsItsPage() {
		AppGeoCeDG app = sheet(5, false);
		EuclidianView view = app.getEuclidianView1();
		String pgf = latex(app, view, "pgf");
		assertTrue(pgf.contains("\\documentclass[10pt,border=0pt]{standalone}"), pgf);
		assertTrue(pgf.contains("x=0.56905512pt,y=0.56905512pt"), "0.02 cm in points");
		assertTrue(pgf.contains("\\useasboundingbox(0.,-2970.) rectangle (4200.,0.);\n"
				+ "\\clip(0.,-2970.) rectangle (4200.,0.);"), pgf);
		String pstricks = latex(app, view, "pstricks");
		assertTrue(pstricks.contains("\\documentclass[10pt,border=0pt]{standalone}"));
		assertTrue(pstricks.contains("xunit=0.56905512pt,yunit=0.56905512pt"), pstricks);
		assertTrue(pstricks.contains("\\begin{pspicture*}(0.,-2970.)(4200.,0.)"), pstricks);
		String asymptote = latex(app, view, "asymptote");
		assertTrue(asymptote.contains("size(84.cm)"), "4200 units at 0.02 cm");
		assertTrue(asymptote.contains("draw((xmin,ymin)--(xmin,ymax)--(xmax,ymax)--"
				+ "(xmax,ymin)--cycle, invisible+linewidth(0));"), asymptote);
		assertTrue(asymptote.indexOf("invisible+linewidth(0)")
				< asymptote.indexOf("clip((xmin,ymin)"), "outlined before the clip");
		// without a construction unit the device output is the host output
		app.getDocumentUnits().replace(UnitState.EMPTY);
		String device = latex(app, view, "pgf");
		assertTrue(device.contains("\\documentclass[10pt]{article}"), device);
		assertTrue(device.contains("cm,y="), device);
		assertFalse(device.contains("standalone"));
	}

	@Test
	void theCompiledPagesAreThePaperAtTheSessionScaleWithFrameAndLabel() throws Exception {
		Path pdflatex = tool("pdflatex");
		Path latex = tool("latex");
		Path dvips = tool("dvips");
		Path ps2pdf = tool("ps2pdf");
		Path asy = tool("asy");
		Path gs = tool("gswin64c") != null ? tool("gswin64c") : tool("gs");
		for (int session : new int[] {10, 5}) {
			for (boolean frame : new boolean[] {true, false}) {
				AppGeoCeDG app = sheet(session, frame);
				EuclidianView view = app.getEuclidianView1();
				// the sheet was captured at 1:10: the page is the paper times 10 / session
				double width = 420 * 10.0 / session * BP_PER_MM;
				double height = 297 * 10.0 / session * BP_PER_MM;
				String name = "s" + session + (frame ? "f" : "n");
				Path dir = Files.createDirectories(temporary.resolve(name));
				if (pdflatex != null) {
					Files.writeString(dir.resolve("p.tex"), latex(app, view, "pgf"));
					run(dir, pdflatex.toString(), "-interaction=nonstopmode", "-halt-on-error",
							"p.tex");
					check(dir.resolve("p.pdf"), width, height, frame, true, gs, name + " pgf");
				} else {
					System.out.println("TEX-R1 pdflatex UNAVAILABLE");
				}
				if (latex != null && dvips != null && ps2pdf != null) {
					Files.writeString(dir.resolve("t.tex"), latex(app, view, "pstricks"));
					run(dir, latex.toString(), "-interaction=nonstopmode", "-halt-on-error",
							"t.tex");
					run(dir, dvips.toString(), "-q", "t.dvi", "-o", "t.ps");
					run(dir, ps2pdf.toString(), "t.ps", "t.pdf");
					check(dir.resolve("t.pdf"), width, height, frame, true, gs,
							name + " pstricks");
				} else {
					System.out.println("TEX-R1 latex/dvips/ps2pdf UNAVAILABLE");
				}
				if (asy != null) {
					asymptote(dir, asy, latex(app, view, "asymptote"), width, height, frame, gs,
							name);
				} else {
					System.out.println("TEX-R1 asy UNAVAILABLE");
				}
			}
		}
	}

	@Test
	void outputWithoutASheetKeepsTheHostTextAndCompiles() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = PreG9BR6PlusBPictureFidelityTest.sized(app.getEuclidianView1());
		view.setShowAxes(false, false);
		view.showGrid(false);
		app.getDocumentUnits().replace(UnitState.of(UnitToken.MM, null, null));
		eval(app, "A=(10,10)");
		eval(app, "B=(60,30)");
		eval(app, "s=Segment(A,B)");
		eval(app, "q=Polyline((0,0),(20,40),(80,10))");
		eval(app, "t=Text(\"Plain text\",(5,45))");
		eval(app, "d=AlignedDimension(A,B,8)");
		PreG9BR6PlusBPictureFidelityTest.define(app, -5, 95, -5, 55);
		String pgf = latex(app, view, "pgf");
		assertTrue(pgf.contains("node[anchor=north west] {Plain text}"), "host text: " + pgf);
		assertTrue(pgf.contains("<->,>=latex]"), "dimension line with arrows");
		assertTrue(pgf.contains("\\documentclass[10pt,border=0pt]{standalone}"));
		Path pdflatex = tool("pdflatex");
		if (pdflatex == null) {
			System.out.println("TEX-R1-07 pdflatex UNAVAILABLE");
			return;
		}
		Path dir = Files.createDirectories(temporary.resolve("plain"));
		Files.writeString(dir.resolve("p.tex"), pgf);
		run(dir, pdflatex.toString(), "-interaction=nonstopmode", "-halt-on-error", "p.tex");
		double[] page = pageSize(dir.resolve("p.pdf"), tool("gswin64c"));
		assertEquals(100 * BP_PER_MM, page[0], 0.05, "100 mm area at 1:1 mm: " + pgf);
		assertEquals(60 * BP_PER_MM, page[1], 0.05);
		assertTrue(pgf.contains("}%\n\\begin{tikzpicture}"), "no set-up space: " + pgf);
		Path latex = tool("latex");
		Path dvips = tool("dvips");
		Path ps2pdf = tool("ps2pdf");
		if (latex == null || dvips == null || ps2pdf == null) {
			System.out.println("TEX-R1-07 latex/dvips/ps2pdf UNAVAILABLE");
			return;
		}
		// OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE (pre-existing on 257c854a, outside
		// E3-R1): the E2 dimension value rotation is written with 14 decimals, which
		// TeX refuses as "Number too big"; PSTricks is compiled without the dimension
		app.getKernel().lookupLabel("d").remove();
		Files.writeString(dir.resolve("t.tex"), latex(app, view, "pstricks"));
		run(dir, latex.toString(), "-interaction=nonstopmode", "-halt-on-error", "t.tex");
		run(dir, dvips.toString(), "-q", "t.dvi", "-o", "t.ps");
		run(dir, ps2pdf.toString(), "t.ps", "t.pdf");
		page = pageSize(dir.resolve("t.pdf"), tool("gswin64c"));
		assertEquals(100 * BP_PER_MM, page[0], 0.05, "PSTricks page width");
		assertEquals(60 * BP_PER_MM, page[1], 0.05, "PSTricks page height");
	}

	// ---------------------------------------------------------------- helpers

	private static AppGeoCeDG sheet(int session, boolean frame) {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view;
		try {
			view = PreG9BR6PlusBPictureFidelityTest.sized(app.getEuclidianView1());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		view.setShowAxes(false, false);
		view.showGrid(false);
		app.getDocumentUnits().replace(UnitState.of(UnitToken.MM, null, null));
		app.setDrawingScale(DrawingScale.of(1, session));
		GeoElement point = eval(app, "P=(0,0)");
		GeoElement paper = eval(app, "sheet=IsoABorder(P,3,true,1,10,1.0," + frame + ")");
		point.setEuclidianVisible(false);
		assertTrue(app.getExportAreaSession().useIsoABorder(view.getViewID(),
				AlgoIsoABorder.ownerOf(paper)));
		return app;
	}

	private static String latex(AppGeoCeDG app, EuclidianView view, String dialect) {
		return PreG9BR6PlusCLatexExportTest.generate(
				PreG9BR6PlusCLatexExportTest.exporter(app, dialect), view);
	}

	private void asymptote(Path dir, Path asy, String code, double width, double height,
			boolean frame, Path gs, String name) throws Exception {
		Files.writeString(dir.resolve("a.asy"), code);
		String labelled = runAsy(dir, asy, "a");
		if (Files.exists(dir.resolve("a.pdf"))) {
			check(dir.resolve("a.pdf"), width, height, frame, true, gs, name + " asymptote");
			return;
		}
		// this workstation's Asymptote cannot typeset any label (a TeX pipe failure of
		// the installed asy); the label is then UNAVAILABLE and the drawing and page are
		// still compiled and measured without it
		assertTrue(labelled.contains("plain_Label.asy"), "unexpected asy failure: " + labelled);
		System.out.println("TEX-R1 asy label typesetting UNAVAILABLE (" + name + ")");
		StringBuilder unlabelled = new StringBuilder();
		for (String line : code.split("\n")) {
			if (!line.startsWith("label(")) {
				unlabelled.append(line).append('\n');
			}
		}
		Files.writeString(dir.resolve("b.asy"), unlabelled);
		runAsy(dir, asy, "b");
		check(dir.resolve("b.pdf"), width, height, frame, false, gs,
				name + " asymptote without label");
	}

	/**
	 * Asymptote 3.06 of this workstation returns 0 on runtime errors and may stay alive
	 * after writing its PDF; the PDF, not the exit code or the process end, decides.
	 */
	private static String runAsy(Path dir, Path asy, String base) throws Exception {
		Path log = dir.resolve(base + ".asy.txt");
		Process process = new ProcessBuilder(asy.toString(), "-f", "pdf", "-o", base,
				base + ".asy").directory(dir.toFile()).redirectErrorStream(true)
				.redirectOutput(log.toFile()).start();
		if (!process.waitFor(30, TimeUnit.SECONDS)) {
			process.destroyForcibly();
			System.out.println("TEX-R1 asy did not end within 30 s (" + base + ")");
		}
		return Files.readString(log, StandardCharsets.ISO_8859_1);
	}

	private static void check(Path pdf, double width, double height, boolean frame,
			boolean label, Path gs, String name) throws Exception {
		assertTrue(Files.exists(pdf), name + ": no PDF");
		double[] page = pageSize(pdf, gs);
		assertEquals(width, page[0], 0.05, name + " page width");
		assertEquals(height, page[1], 0.05, name + " page height");
		if (gs == null) {
			System.out.println("TEX-R1 raster check UNAVAILABLE (" + name + ")");
			return;
		}
		Path png = pdf.resolveSibling(pdf.getFileName() + ".png");
		run(pdf.getParent(), gs.toString(), "-q", "-dNOPAUSE", "-dBATCH", "-dSAFER",
				"-sDEVICE=pnggray", "-r36", "-sOutputFile=" + png.getFileName(),
				pdf.getFileName().toString());
		BufferedImage image = ImageIO.read(png.toFile());
		assertNotNull(image, name);
		int w = image.getWidth();
		int h = image.getHeight();
		// frame edges at 20 mm (left) and 10 mm (other sides) of the 420 x 297 mm paper
		double[] vertical = {20 / 420.0, 410 / 420.0};
		double[] horizontal = {10 / 297.0, 287 / 297.0};
		for (double fx : vertical) {
			double coverage = coverage(image, (int) Math.round(fx * w), true, h);
			assertTrue(frame ? coverage > 0.9 : coverage < 0.1,
					name + " vertical frame edge at " + fx + ": " + coverage);
		}
		for (double fy : horizontal) {
			double coverage = coverage(image, (int) Math.round(fy * h), false, w);
			assertTrue(frame ? coverage > 0.9 : coverage < 0.1,
					name + " horizontal frame edge at " + fy + ": " + coverage);
		}
		if (label) {
			// up and left of the anchor at (410 mm, 287 mm), inside the paper
			int x1 = (int) Math.round(410 / 420.0 * w) - 3;
			int x0 = (int) Math.round(340 / 420.0 * w);
			int y1 = (int) Math.round(287 / 297.0 * h) - 3;
			int y0 = (int) Math.round(250 / 297.0 * h);
			assertTrue(dark(image, x0, x1, y0, y1) > 10, name + " label above-left");
			assertEquals(0, dark(image, x1 + 6, w - 1, y1 + 6, h - 1),
					name + " nothing right of and below the anchor");
		}
	}

	private static double coverage(BufferedImage image, int at, boolean vertical, int length) {
		int hits = 0;
		int samples = 0;
		for (int t = length / 4; t < 3 * length / 4; t++) {
			samples++;
			for (int d = -2; d <= 2; d++) {
				int x = vertical ? at + d : t;
				int y = vertical ? t : at + d;
				if (x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight()
						&& (image.getRGB(x, y) & 0xff) < 128) {
					hits++;
					break;
				}
			}
		}
		return hits / (double) samples;
	}

	private static int dark(BufferedImage image, int x0, int x1, int y0, int y1) {
		int count = 0;
		for (int y = Math.max(0, y0); y <= Math.min(image.getHeight() - 1, y1); y++) {
			for (int x = Math.max(0, x0); x <= Math.min(image.getWidth() - 1, x1); x++) {
				if ((image.getRGB(x, y) & 0xff) < 128) {
					count++;
				}
			}
		}
		return count;
	}

	private static double[] pageSize(Path pdf, Path gs) throws Exception {
		Path pdfinfo = tool("pdfinfo");
		if (pdfinfo != null) {
			String info = run(pdf.getParent(), pdfinfo.toString(), pdf.getFileName().toString());
			Matcher m = Pattern.compile("Page size:\\s+([0-9.]+) x ([0-9.]+) pts").matcher(info);
			assertTrue(m.find(), info);
			return new double[] {Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2))};
		}
		assertNotNull(gs, "pdfinfo or Ghostscript is needed to measure the page");
		String box = run(pdf.getParent(), gs.toString(), "-q", "-dNODISPLAY", "-dNOSAFER",
				"-c", "(" + pdf.getFileName() + ") (r) file runpdfbegin 1 pdfgetpage "
						+ "/MediaBox pget pop == quit");
		Matcher m = Pattern.compile("\\[\\s*([-0-9.]+)\\s+([-0-9.]+)\\s+([-0-9.]+)\\s+([-0-9.]+)")
				.matcher(box);
		assertTrue(m.find(), box);
		return new double[] {Double.parseDouble(m.group(3)) - Double.parseDouble(m.group(1)),
				Double.parseDouble(m.group(4)) - Double.parseDouble(m.group(2))};
	}

	private static String run(Path dir, String... command) throws Exception {
		Path log = Files.createTempFile(dir, "run", ".txt");
		Process process = new ProcessBuilder(command).directory(dir.toFile())
				.redirectErrorStream(true).redirectOutput(log.toFile()).start();
		boolean ended = process.waitFor(240, TimeUnit.SECONDS);
		if (!ended) {
			process.destroyForcibly();
		}
		String output = Files.readString(log, StandardCharsets.ISO_8859_1);
		assertTrue(ended, String.join(" ", command) + " timed out:\n" + output);
		if (!command[0].toLowerCase().contains("asy")) {
			assertEquals(0, process.exitValue(), String.join(" ", command) + ":\n" + output);
		}
		return output;
	}

	private static Path tool(String name) {
		String path = System.getenv("PATH");
		if (path == null) {
			return null;
		}
		for (String entry : path.split(File.pathSeparator)) {
			for (String suffix : new String[] {".exe", ".bat", ".cmd", ""}) {
				Path candidate = Paths.get(entry, name + suffix);
				if (Files.isRegularFile(candidate)) {
					return candidate;
				}
			}
		}
		return null;
	}

	// -------------------------------------------------------------- DXF pairs

	private static List<String[]> pairs(String dxf) {
		String[] lines = dxf.split("\r\n", -1);
		List<String[]> pairs = new ArrayList<>();
		for (int i = 0; i + 1 < lines.length; i += 2) {
			pairs.add(new String[] {lines[i].trim(), lines[i + 1]});
		}
		return pairs;
	}

	private static String header(List<String[]> pairs, String variable) {
		for (int i = 0; i + 1 < pairs.size(); i++) {
			if ("9".equals(pairs.get(i)[0]) && variable.equals(pairs.get(i)[1])) {
				return pairs.get(i + 1)[1];
			}
		}
		throw new AssertionError("missing " + variable);
	}

	private static void assertUniqueHandlesBelowSeed(List<String[]> pairs) {
		int seed = Integer.parseInt(header(pairs, "$HANDSEED"), 16);
		Set<String> handles = new HashSet<>();
		boolean entities = false;
		for (int i = 0; i < pairs.size(); i++) {
			String[] pair = pairs.get(i);
			if ("2".equals(pair[0]) && "ENTITIES".equals(pair[1])) {
				entities = true;
			}
			if ("2".equals(pair[0]) && "OBJECTS".equals(pair[1])) {
				entities = false;
			}
			if ("5".equals(pair[0]) && !"$HANDSEED".equals(pairs.get(i - 1)[1])) {
				int value = Integer.parseInt(pair[1], 16);
				assertTrue(handles.add(pair[1]), "unique handle " + pair[1]);
				assertTrue(value < seed, pair[1] + " below the seed");
				assertTrue(entities ? value >= 0x100 : value < 0x100,
						"structural handles below the entity handles: " + pair[1]);
			}
		}
	}

	private static List<List<String[]>> entities(List<String[]> pairs) {
		List<List<String[]>> entities = new ArrayList<>();
		boolean inside = false;
		List<String[]> current = null;
		for (int i = 0; i < pairs.size(); i++) {
			String[] pair = pairs.get(i);
			if ("0".equals(pair[0]) && "SECTION".equals(pair[1])) {
				inside = "ENTITIES".equals(pairs.get(i + 1)[1]);
				i++;
				continue;
			}
			if (!inside) {
				continue;
			}
			if ("0".equals(pair[0])) {
				if ("ENDSEC".equals(pair[1])) {
					inside = false;
					continue;
				}
				current = new ArrayList<>();
				entities.add(current);
			}
			current.add(pair);
		}
		return entities;
	}

	private static String value(List<String[]> entity, int code) {
		for (String[] pair : entity) {
			if (Integer.parseInt(pair[0]) == code) {
				return pair[1];
			}
		}
		return null;
	}

	private static List<Double> values(List<String[]> entity, int code) {
		List<Double> values = new ArrayList<>();
		for (String[] pair : entity) {
			if (Integer.parseInt(pair[0]) == code) {
				values.add(Double.parseDouble(pair[1]));
			}
		}
		return values;
	}
}
