/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.freehep.graphics2d.font.Lookup;
import org.freehep.graphicsio.AbstractVectorGraphicsIO;
import org.freehep.graphicsio.font.CharstringEncoder;
import org.freehep.graphicsio.font.FontEmbedderType1;
import org.freehep.graphicsio.pdf.PDFGraphics2D;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GFont;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GProperty;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.export.GraphicExportDialog;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-F4-D scope amendment 01 (TD-PDF-3): the inherited FreeHEP
 * {@code CharstringEncoder} wrote no moveto when a subpath started at the current point
 * (zero displacement), so glyphs starting at the left side-bearing point had no defined
 * subpath start (Adobe Type 1 Font Format 1.1, section 6.4: {@code hsbw} does not place
 * the current point in the path; {@code closepath} does not reposition it). These tests
 * decode the charstrings and check them structurally and against the Java glyph shapes
 * FreeHEP encodes.
 *
 * <p>With {@code GEOCEDG_PRE_G9B_R6_PLUS_F4_D_PDF_OUT=<directory>} the glyph-cohort PDFs
 * are also copied there for the external renderer evidence.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusF4DType1OutlineTest {
	private static final String PDF_OUT = "GEOCEDG_PRE_G9B_R6_PLUS_F4_D_PDF_OUT";
	/** Capitals, lower case and the period whose outline starts at the side bearing. */
	static final String COHORT = "ABDEFHIKLMNPRXZ hklmnrxz .";
	/** Accented and special Latin glyphs of the same cohort. */
	static final String COHORT_ACCENTED = "ÀÁÂÃÄÅÆ"
			+ " ÈÉÊË Ñ ñ Þ ß Ž ž ı";
	/** Spanish text. */
	static final String SPANISH = "¿Año? ¡Ñandú! Cigüeña,"
			+ " éxito, Ángulo";
	/** FreeHEP's render context for PDF fonts ({@code AbstractVectorGraphicsIO}). */
	private static final FontRenderContext PDF_CONTEXT = new FontRenderContext(
			new AffineTransform(1, 0, 0, -1, 0, 0), true, true);
	private static final String[] PDF_DEFAULT_KEYS = {PDFGraphics2D.EMBED_FONTS,
		PDFGraphics2D.EMBED_FONTS_AS, AbstractVectorGraphicsIO.TEXT_AS_SHAPES};

	@TempDir
	Path temporary;

	// ------------------------------------------------- structural charstrings

	@Test
	void zeroDisplacementInitialMovetoBeginsTheSubpath() throws IOException {
		Charstring c = encode(e -> {
			e.startChar(77, 722);
			e.move(77, 0);
			e.line(77, 716);
			e.line(324, 716);
			e.line(324, 0);
			e.closePath(77, 0);
			e.endchar();
		});
		assertEquals(List.of("hsbw 77 722", "hmoveto 0", "vlineto 716", "hlineto 247",
				"vlineto -716", "closepath", "endchar"), c.ops);
		assertEquals(List.of("moveto 77 0", "lineto 77 716", "lineto 324 716", "lineto 324 0",
				"closepath"), c.path);
		assertEquals(List.of(), c.violations);
	}

	@Test
	void nonzeroDisplacementMovetosKeepTheirEncoding() throws IOException {
		// bytes by the charstring number and command encoding of the specification
		assertEquals(hex(charstring(50, 722, "hsbw", 488, 251, "rmoveto", "endchar")),
				hex(encode(e -> {
					e.startChar(50, 722);
					e.move(538, 251);
					e.endchar();
				}).bytes));
		assertEquals(hex(charstring(33, 556, "hsbw", 259, "vmoveto", "endchar")),
				hex(encode(e -> {
					e.startChar(33, 556);
					e.move(33, 259);
					e.endchar();
				}).bytes));
		assertEquals(hex(charstring(10, 500, "hsbw", 50, "hmoveto", "endchar")),
				hex(encode(e -> {
					e.startChar(10, 500);
					e.move(60, 0);
					e.endchar();
				}).bytes));
		// a large displacement uses the two-byte number form
		assertEquals(hex(charstring(0, 1000, "hsbw", -300, 1131, "rmoveto", "endchar")),
				hex(encode(e -> {
					e.startChar(0, 1000);
					e.move(-300, 1131);
					e.endchar();
				}).bytes));
		// fractional targets round to the same nonzero displacement
		assertEquals(hex(charstring(10, 500, "hsbw", 50, "hmoveto", "endchar")),
				hex(encode(e -> {
					e.startChar(10, 500);
					e.move(60.4, 0.3);
					e.endchar();
				}).bytes));
	}

	@Test
	void repeatedMovetosEachEmitAMoveto() throws IOException {
		Charstring c = encode(e -> {
			e.startChar(0, 500);
			e.move(100, 100);
			e.move(100, 100);
			e.move(200, 150);
			e.line(300, 150);
			e.endchar();
		});
		assertEquals(List.of("hsbw 0 500", "rmoveto 100 100", "hmoveto 0", "rmoveto 100 50",
				"hlineto 100", "endchar"), c.ops);
		assertEquals(List.of("moveto 100 100", "moveto 100 100", "moveto 200 150",
				"lineto 300 150"), c.path);
		assertEquals(List.of(), c.violations);
	}

	@Test
	void closedContourFollowedBySubpathAtTheCurrentPoint() throws IOException {
		Charstring c = encode(e -> {
			e.startChar(0, 500);
			e.move(0, 0);
			e.line(100, 0);
			e.line(100, 100);
			e.closePath(0, 0);
			// Type 1 closepath keeps the current point (100, 100)
			e.move(100, 100);
			e.line(200, 100);
			e.line(200, 200);
			e.closePath(100, 100);
			e.endchar();
		});
		assertEquals(List.of("hsbw 0 500", "hmoveto 0", "hlineto 100", "vlineto 100",
				"closepath", "hmoveto 0", "hlineto 100", "vlineto 100", "closepath", "endchar"),
				c.ops);
		assertEquals(List.of("moveto 0 0", "lineto 100 0", "lineto 100 100", "closepath",
				"moveto 100 100", "lineto 200 100", "lineto 200 200", "closepath"), c.path);
		assertEquals(List.of(), c.violations);
	}

	@Test
	void multipleSubpathsWithCurvesMatchTheSourceShape() throws IOException {
		GeneralPath ring = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		ring.append(new Rectangle2D.Double(40, 0, 500, 700), false);
		ring.append(new java.awt.geom.Ellipse2D.Double(140.3, 99.6, 300.2, 500.1), false);
		ring.moveTo(40, 0);
		ring.quadTo(300.7, 900.2, 540, 0);
		ring.closePath();
		Charstring c = encode(e -> {
			e.startChar(40, 600);
			e.drawPath(ring);
			e.endchar();
		});
		assertEquals(oracle(ring, 40), c.path);
		assertEquals(List.of(), c.violations);
		assertEquals(3, Collections.frequency(c.path, "closepath"));
	}

	// ----------------------------------------- glyphs against the Java shapes

	@Test
	void everyGlyphOfTheProductFontsEqualsItsJavaShape() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		Map<String, Font> fonts = new LinkedHashMap<>();
		for (boolean serif : new boolean[] {false, true}) {
			for (int style : new int[] {Font.PLAIN, Font.BOLD, Font.ITALIC,
				Font.BOLD | Font.ITALIC}) {
				Font font = app.getFontCanDisplayAwt("a", serif, style, 12);
				fonts.put((serif ? "serif" : "sans") + "-" + style, font);
			}
		}
		for (Map.Entry<String, Font> entry : fonts.entrySet()) {
			Font font = pdfFont(entry.getValue());
			RecordingEmbedder embedder = new RecordingEmbedder();
			embedder.includeFont(font, Lookup.getInstance().getTable("PDFLatin"), "F1");
			byte[] program = embedder.out.toByteArray();
			Map<String, byte[]> charstrings = charStrings(program,
					embedder.getAsciiLength(), embedder.getEncryptedLength());
			assertEquals(embedder.names.size(), charstrings.size(), entry.getKey() + " glyphs");
			assertTrue(embedder.names.size() > 200, entry.getKey() + " glyph count");
			int startingAtSideBearing = 0;
			for (int i = 0; i < embedder.names.size(); i++) {
				String name = embedder.names.get(i);
				GlyphMetrics metrics = embedder.metrics.get(i);
				double sideBearing = metrics == null ? 0 : metrics.getLSB();
				Charstring c = interpret(charstrings.get(name));
				String where = entry.getKey() + " /" + name;
				assertEquals(List.of(), c.violations, where);
				assertEquals("hsbw " + Math.round(sideBearing) + " "
						+ Math.round(metrics == null ? 1000 : metrics.getAdvance()), c.ops.get(0),
						where + " side bearing and width");
				assertEquals(oracle(embedder.shapes.get(i), sideBearing), c.path, where);
				if (c.ops.size() > 1 && c.ops.get(1).equals("hmoveto 0")) {
					startingAtSideBearing++;
				}
			}
			// the TD-PDF-3 cohort of this font is exercised
			assertTrue(startingAtSideBearing > 0, entry.getKey() + " no zero-displacement start");
		}
	}

	// --------------------------------------------- product PDFs: glyph cohort

	@Test
	void geocedgCohortPdfStartsEverySubpathWithAMoveto() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		sized(app.getEuclidianView1());
		app.getDocumentUnits().replace(UnitState.of(UnitToken.MM, null, null));
		List<String> strings = cohort(app.getKernel().getAlgebraProcessor()
				::processAlgebraCommand, 6);
		assertTrue(app.getExportAreaSession().defineManual(App.VIEW_EUCLIDIAN, -10, 90, -60,
				25));
		for (boolean shapes : new boolean[] {true, false}) {
			File file = temporary.resolve("geocedg_cohort-" + (shapes ? "shapes" : "embedded")
					+ ".pdf").toFile();
			app.getPictureExportService().writePDF(app.getEuclidianView1(), file, shapes);
			keep(file);
		}
		assertCohort("geocedg", temporary.resolve("geocedg_cohort-embedded.pdf").toFile(),
				strings);
	}

	@Test
	void classicCohortPdfStartsEverySubpathWithAMoveto() throws Exception {
		AppD classic = G9U1TestApp.withoutWindowDispatcher(new AppD(new CommandLineArguments(
				new String[] {"--silent"}), new JPanel(), true));
		sized(classic.getEuclidianView1());
		List<String> strings = cohort(classic.getKernel().getAlgebraProcessor()
				::processAlgebraCommand, 1.5);
		for (String point : new String[] {"Export_1=(-10,-60)", "Export_2=(90,25)"}) {
			GeoElement geo = classic.getKernel().getAlgebraProcessor().processAlgebraCommand(
					point, false)[0].toGeoElement();
			geo.setEuclidianVisible(false);
			geo.updateRepaint();
		}
		EuclidianView view = classic.getEuclidianView1();
		view.setPrintingScale(0.1);
		double scale = view.getPrintingScale() * 72 / 2.54 / view.getXscale();
		Properties defaults = PDFGraphics2D.getDefaultProperties();
		Map<String, String> saved = new HashMap<>();
		for (String key : PDF_DEFAULT_KEYS) {
			saved.put(key, defaults.getProperty(key));
		}
		try {
			for (boolean shapes : new boolean[] {true, false}) {
				File file = temporary.resolve("classic_cohort-" + (shapes ? "shapes"
						: "embedded") + ".pdf").toFile();
				GraphicExportDialog.exportPDF((EuclidianViewD) view, file, shapes,
						(int) Math.floor(view.getExportWidth() * scale),
						(int) Math.floor(view.getExportHeight() * scale), scale);
				keep(file);
			}
		} finally {
			for (Map.Entry<String, String> entry : saved.entrySet()) {
				if (entry.getValue() == null) {
					defaults.remove(entry.getKey());
				} else {
					defaults.setProperty(entry.getKey(), entry.getValue());
				}
			}
		}
		assertCohort("classic", temporary.resolve("classic_cohort-embedded.pdf").toFile(),
				strings);
	}

	/**
	 * Every embedded Type 1 program: every glyph begins each subpath with a moveto; the
	 * {@code /Widths} entry of every encoded glyph equals its {@code hsbw} width; the text
	 * of the page equals the drawn strings; the six font variants are embedded.
	 */
	private static void assertCohort(String route, File file, List<String> strings)
			throws Exception {
		String raw = PreG9BR6PlusF4DEmbeddedFontPdfTest.read(file);
		PreG9BR6PlusF4DEmbeddedFontPdfTest.PdfFile pdf =
				new PreG9BR6PlusF4DEmbeddedFontPdfTest.PdfFile(raw);
		Map<String, String> resources = pdf.fontResources();
		List<String> embedded = new ArrayList<>();
		for (Map.Entry<String, String> font : resources.entrySet()) {
			String dictionary = pdf.object(font.getValue());
			if (!dictionary.contains("/FontDescriptor")) {
				continue;
			}
			String descriptor = pdf.object(PreG9BR6PlusF4DEmbeddedFontPdfTest.reference(
					dictionary, "FontDescriptor"));
			String fontFile = pdf.object(PreG9BR6PlusF4DEmbeddedFontPdfTest.reference(
					descriptor, "FontFile"));
			byte[] program = streamBytes(fontFile);
			Map<String, byte[]> charstrings = charStrings(program, number(fontFile, "Length1"),
					number(fontFile, "Length2"));
			assertTrue(charstrings.size() > 200, route + " " + font.getKey() + " glyphs");
			Map<String, Integer> widths = new HashMap<>();
			for (Map.Entry<String, byte[]> glyph : charstrings.entrySet()) {
				Charstring c = interpret(glyph.getValue());
				assertEquals(List.of(), c.violations, route + " " + font.getKey() + " /"
						+ glyph.getKey());
				widths.put(glyph.getKey(), Integer.parseInt(c.ops.get(0).split(" ")[2]));
			}
			assertWidths(route + " " + font.getKey(), pdf, dictionary, widths);
			embedded.add(font.getKey());
		}
		assertEquals(6, embedded.size(), route + " embedded font programs " + embedded);
		List<String> extracted = new ArrayList<>();
		for (PreG9BR6PlusF4DEmbeddedFontPdfTest.TextBlock block : pdf.textBlocks()) {
			extracted.add(pdf.decode(block));
		}
		List<String> expected = new ArrayList<>(strings);
		Collections.sort(expected);
		Collections.sort(extracted);
		assertEquals(expected, extracted, route + " extracted text");
	}

	private static void assertWidths(String where,
			PreG9BR6PlusF4DEmbeddedFontPdfTest.PdfFile pdf, String dictionary,
			Map<String, Integer> glyphWidths) {
		String encoding = pdf.object(PreG9BR6PlusF4DEmbeddedFontPdfTest.reference(dictionary,
				"Encoding"));
		Matcher differences = Pattern.compile("/Differences\\s*\\[([^\\]]*)\\]")
				.matcher(encoding);
		assertTrue(differences.find(), where + " Differences");
		String[] names = new String[256];
		int code = 0;
		for (String item : differences.group(1).trim().split("\\s+")) {
			if (item.startsWith("/")) {
				names[code++] = item.substring(1);
			} else {
				code = Integer.parseInt(item);
			}
		}
		String widthsBody = pdf.object(PreG9BR6PlusF4DEmbeddedFontPdfTest.reference(dictionary,
				"Widths"));
		String[] widths = widthsBody.replace("[", " ").replace("]", " ").trim().split("\\s+");
		int first = number(dictionary, "FirstChar");
		int compared = 0;
		for (int c = 0; c < 256; c++) {
			if (names[c] == null || ".notdef".equals(names[c])
					|| !glyphWidths.containsKey(names[c])) {
				continue;
			}
			assertEquals((int) glyphWidths.get(names[c]), Integer.parseInt(widths[c - first]),
					where + " width of /" + names[c]);
			compared++;
		}
		assertTrue(compared > 150, where + " widths compared " + compared);
	}

	/** Eight lines of the glyph cohort in sans and serif, four styles, two sizes. */
	private static List<String> cohort(Evaluator evaluator, double multiplier) {
		Object[][] lines = {
			{COHORT, false, GFont.PLAIN, 1.0},
			{COHORT_ACCENTED, false, GFont.PLAIN, 1.0},
			{SPANISH, false, GFont.PLAIN, 1.0},
			{COHORT, false, GFont.BOLD, 1.0},
			{COHORT, false, GFont.ITALIC, 1.0},
			{COHORT_ACCENTED, false, GFont.BOLD | GFont.ITALIC, 1.0},
			{COHORT, true, GFont.PLAIN, 1.0},
			{SPANISH, true, GFont.BOLD, 1.0},
			{COHORT, false, GFont.PLAIN, 2.0}};
		List<String> strings = new ArrayList<>();
		for (int i = 0; i < lines.length; i++) {
			String string = (String) lines[i][0];
			GeoText text = (GeoText) evaluator.eval("C" + i + "=Text(\"" + string + "\",(0,"
					+ (18 - 8 * i) + "))", false)[0].toGeoElement();
			text.setObjColor(GColor.BLACK);
			text.setSerifFont((Boolean) lines[i][1]);
			text.setFontStyle((Integer) lines[i][2]);
			text.setFontSizeMultiplier(multiplier * (Double) lines[i][3]);
			text.setLabelVisible(false);
			text.updateVisualStyleRepaint(GProperty.FONT);
			text.updateRepaint();
			strings.add(string);
		}
		return strings;
	}

	private interface Evaluator {
		GeoElementND[] eval(String command, boolean storeUndo);
	}

	private static void sized(EuclidianView view) throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(1000, 700);
			view.updateSize();
			// world x from -12.5 to 112.5 and y from -62.5 to 25: every cohort line visible
			view.setCoordSystem(100, 200, 8, 8);
			view.setShowAxes(false, false);
			view.showGrid(false);
		});
	}

	private static void keep(File file) throws IOException {
		String out = System.getenv(PDF_OUT);
		if (out != null && !out.isEmpty() && file.isFile()) {
			Files.createDirectories(Paths.get(out));
			Files.copy(file.toPath(), Paths.get(out, file.getName()),
					StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/** The font FreeHEP's font table embeds for a drawn font (size 1000, no transform). */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Font pdfFont(Font drawn) {
		Map attributes = drawn.getAttributes();
		attributes.put(TextAttribute.SIZE, 1000f);
		attributes.remove(TextAttribute.TRANSFORM);
		attributes.remove(TextAttribute.SUPERSCRIPT);
		return new Font(attributes);
	}

	/** The PDF Type 1 embedder's glyph loop, recording each shape it encodes. */
	private static final class RecordingEmbedder extends FontEmbedderType1 {
		final ByteArrayOutputStream out;
		final List<String> names = new ArrayList<>();
		final List<Shape> shapes = new ArrayList<>();
		final List<GlyphMetrics> metrics = new ArrayList<>();

		RecordingEmbedder() {
			this(new ByteArrayOutputStream());
		}

		private RecordingEmbedder(ByteArrayOutputStream out) {
			super(PDF_CONTEXT, out, false);
			this.out = out;
		}

		@Override
		protected void writeGlyph(String characterName, Shape glyph,
				GlyphMetrics glyphMetrics) throws IOException {
			names.add(characterName);
			shapes.add(glyph);
			metrics.add(glyphMetrics);
			super.writeGlyph(characterName, glyph, glyphMetrics);
		}
	}

	// ------------------------------------------------- Type 1 decoding (6.2-7.3)

	private interface Script {
		void run(CharstringEncoder encoder) throws IOException;
	}

	/** An encoded charstring, its commands, its absolute path and its violations. */
	static final class Charstring {
		byte[] bytes;
		final List<String> ops = new ArrayList<>();
		final List<String> path = new ArrayList<>();
		final List<String> violations = new ArrayList<>();
	}

	private static Charstring encode(Script script) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		script.run(new CharstringEncoder(out));
		Charstring c = interpret(out.toByteArray());
		c.bytes = out.toByteArray();
		return c;
	}

	/**
	 * Interprets the path commands FreeHEP writes. {@code hsbw} sets the current point
	 * without starting a subpath; a line or curve outside a subpath begun by a moveto is a
	 * violation; {@code closepath} ends the subpath and keeps the current point.
	 */
	static Charstring interpret(byte[] code) {
		Charstring c = new Charstring();
		List<Integer> stack = new ArrayList<>();
		long x = 0;
		long y = 0;
		boolean open = false;
		int i = 0;
		while (i < code.length) {
			int v = code[i++] & 255;
			if (v >= 32) {
				if (v <= 246) {
					stack.add(v - 139);
				} else if (v <= 250) {
					stack.add((v - 247) * 256 + (code[i++] & 255) + 108);
				} else if (v <= 254) {
					stack.add(-(v - 251) * 256 - (code[i++] & 255) - 108);
				} else {
					stack.add(((code[i] & 255) << 24) | ((code[i + 1] & 255) << 16)
							| ((code[i + 2] & 255) << 8) | (code[i + 3] & 255));
					i += 4;
				}
				continue;
			}
			String op;
			boolean drawing = false;
			switch (v) {
			case 13:
				op = "hsbw";
				x = stack.get(0);
				y = 0;
				break;
			case 21:
				op = "rmoveto";
				x += stack.get(0);
				y += stack.get(1);
				open = true;
				c.path.add("moveto " + x + " " + y);
				break;
			case 22:
				op = "hmoveto";
				x += stack.get(0);
				open = true;
				c.path.add("moveto " + x + " " + y);
				break;
			case 4:
				op = "vmoveto";
				y += stack.get(0);
				open = true;
				c.path.add("moveto " + x + " " + y);
				break;
			case 5:
				op = "rlineto";
				x += stack.get(0);
				y += stack.get(1);
				drawing = true;
				c.path.add("lineto " + x + " " + y);
				break;
			case 6:
				op = "hlineto";
				x += stack.get(0);
				drawing = true;
				c.path.add("lineto " + x + " " + y);
				break;
			case 7:
				op = "vlineto";
				y += stack.get(0);
				drawing = true;
				c.path.add("lineto " + x + " " + y);
				break;
			case 8:
				op = "rrcurveto";
				StringBuilder curve = new StringBuilder("curveto");
				for (int k = 0; k < 6; k += 2) {
					x += stack.get(k);
					y += stack.get(k + 1);
					curve.append(' ').append(x).append(' ').append(y);
				}
				drawing = true;
				c.path.add(curve.toString());
				break;
			case 9:
				op = "closepath";
				if (!open) {
					c.violations.add("closepath without an open subpath at " + c.ops.size());
				}
				open = false;
				c.path.add("closepath");
				break;
			case 14:
				op = "endchar";
				break;
			default:
				throw new AssertionError("unexpected charstring command " + v);
			}
			if (drawing && !open) {
				c.violations.add(op + " without a preceding moveto at " + c.ops.size());
			}
			StringBuilder line = new StringBuilder(op);
			if (!"closepath".equals(op) && !"endchar".equals(op)) {
				for (int value : stack) {
					line.append(' ').append(value);
				}
			}
			c.ops.add(line.toString());
			stack.clear();
		}
		return c;
	}

	/**
	 * The path the encoder must write for a shape: every point rounded to font units,
	 * quadratic segments raised exactly to cubic ones from the unrounded previous point,
	 * zero-length lines omitted, a moveto for every subpath.
	 */
	static List<String> oracle(Shape shape, double sideBearing) {
		List<String> path = new ArrayList<>();
		long cx = Math.round(sideBearing);
		long cy = 0;
		double px = 0;
		double py = 0;
		double[] p = new double[6];
		for (PathIterator it = shape.getPathIterator(null); !it.isDone(); it.next()) {
			switch (it.currentSegment(p)) {
			case PathIterator.SEG_MOVETO:
				cx = Math.round(p[0]);
				cy = Math.round(p[1]);
				path.add("moveto " + cx + " " + cy);
				px = p[0];
				py = p[1];
				break;
			case PathIterator.SEG_LINETO:
				if (Math.round(p[0]) != cx || Math.round(p[1]) != cy) {
					cx = Math.round(p[0]);
					cy = Math.round(p[1]);
					path.add("lineto " + cx + " " + cy);
				}
				px = p[0];
				py = p[1];
				break;
			case PathIterator.SEG_QUADTO:
				double[] cubic = {p[0] + (px - p[0]) / 3., p[1] + (py - p[1]) / 3.,
					p[0] + (p[2] - p[0]) / 3., p[1] + (p[3] - p[1]) / 3., p[2], p[3]};
				cx = Math.round(cubic[4]);
				cy = Math.round(cubic[5]);
				path.add(curve(cubic));
				px = p[2];
				py = p[3];
				break;
			case PathIterator.SEG_CUBICTO:
				cx = Math.round(p[4]);
				cy = Math.round(p[5]);
				path.add(curve(p));
				px = p[4];
				py = p[5];
				break;
			default:
				path.add("closepath");
				px = 0;
				py = 0;
				break;
			}
		}
		return path;
	}

	private static String curve(double[] p) {
		return "curveto " + Math.round(p[0]) + " " + Math.round(p[1]) + " " + Math.round(p[2])
				+ " " + Math.round(p[3]) + " " + Math.round(p[4]) + " " + Math.round(p[5]);
	}

	/** Glyph name to decrypted charstring of a Type 1 program (hex or binary eexec). */
	static Map<String, byte[]> charStrings(byte[] program, int length1, int length2) {
		byte[] section = Arrays.copyOfRange(program, length1, length1 + length2);
		byte[] cipher = hexDecoded(section);
		byte[] plain = decrypt(cipher, 55665, 4);
		String text = new String(plain, StandardCharsets.ISO_8859_1);
		int lenIV = 4;
		Matcher iv = Pattern.compile("/lenIV (\\d+)").matcher(text);
		if (iv.find()) {
			lenIV = Integer.parseInt(iv.group(1));
		}
		int from = text.indexOf("/CharStrings");
		assertTrue(from >= 0, "CharStrings dictionary");
		Matcher glyph = Pattern.compile("/([A-Za-z0-9._]+) (\\d+) RD ").matcher(text);
		Map<String, byte[]> result = new LinkedHashMap<>();
		while (glyph.find(from)) {
			int n = Integer.parseInt(glyph.group(2));
			result.put(glyph.group(1), decrypt(Arrays.copyOfRange(plain, glyph.end(),
					glyph.end() + n), 4330, lenIV));
			from = glyph.end() + n;
		}
		return result;
	}

	private static byte[] hexDecoded(byte[] section) {
		boolean hex = true;
		for (int i = 0; i < Math.min(section.length, 64); i++) {
			char ch = (char) (section[i] & 255);
			hex &= Character.isWhitespace(ch) || Character.digit(ch, 16) >= 0;
		}
		if (!hex) {
			return section;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int high = -1;
		for (byte b : section) {
			int d = Character.digit((char) (b & 255), 16);
			if (d < 0) {
				continue;
			}
			if (high < 0) {
				high = d;
			} else {
				out.write(high * 16 + d);
				high = -1;
			}
		}
		return out.toByteArray();
	}

	private static byte[] decrypt(byte[] cipher, int key, int skip) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int r = key;
		for (int i = 0; i < cipher.length; i++) {
			int c = cipher[i] & 255;
			int plain = c ^ (r >> 8);
			r = ((c + r) * 52845 + 22719) & 0xFFFF;
			if (i >= skip) {
				out.write(plain);
			}
		}
		return out.toByteArray();
	}

	/**
	 * Charstring bytes per the number (6.2) and command (6.3) encoding of the
	 * specification: integers are numbers, strings are commands.
	 */
	private static byte[] charstring(Object... items) {
		Map<String, Integer> commands = Map.of("hsbw", 13, "rmoveto", 21, "hmoveto", 22,
				"vmoveto", 4, "endchar", 14);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (Object item : items) {
			if (item instanceof String) {
				out.write(commands.get(item));
				continue;
			}
			int v = (Integer) item;
			if (v >= -107 && v <= 107) {
				out.write(v + 139);
			} else if (v >= 108 && v <= 1131) {
				out.write((v - 108) / 256 + 247);
				out.write((v - 108) % 256);
			} else if (v >= -1131 && v <= -108) {
				out.write((-v - 108) / 256 + 251);
				out.write((-v - 108) % 256);
			} else {
				throw new IllegalArgumentException(String.valueOf(v));
			}
		}
		return out.toByteArray();
	}

	private static String hex(byte[] bytes) {
		StringBuilder s = new StringBuilder();
		for (byte b : bytes) {
			s.append(String.format("%02x", b));
		}
		return s.toString();
	}

	private static int number(String body, String key) {
		Matcher m = Pattern.compile("/" + key + "\\s+(\\d+)").matcher(body);
		assertTrue(m.find(), "/" + key);
		return Integer.parseInt(m.group(1));
	}

	/** The raw bytes of a PDF stream object after its ASCII85 and Flate filters. */
	private static byte[] streamBytes(String body) throws Exception {
		int start = body.indexOf("stream") + "stream".length();
		while (body.charAt(start) == '\r' || body.charAt(start) == '\n') {
			start++;
		}
		byte[] data = body.substring(start, body.lastIndexOf("endstream"))
				.getBytes(StandardCharsets.ISO_8859_1);
		String dictionary = body.substring(0, start);
		if (dictionary.contains("/ASCII85Decode")) {
			ByteArrayOutputStream raw = new ByteArrayOutputStream();
			long value = 0;
			int count = 0;
			for (byte b : data) {
				char c = (char) (b & 255);
				if (c == '~') {
					break;
				}
				if (Character.isWhitespace(c)) {
					continue;
				}
				if (c == 'z' && count == 0) {
					raw.write(new byte[4], 0, 4);
					continue;
				}
				value = value * 85 + (c - 33);
				if (++count == 5) {
					for (int shift = 24; shift >= 0; shift -= 8) {
						raw.write((int) (value >> shift) & 255);
					}
					value = 0;
					count = 0;
				}
			}
			if (count > 0) {
				int kept = count - 1;
				while (count < 5) {
					value = value * 85 + 84;
					count++;
				}
				for (int k = 0, shift = 24; k < kept; k++, shift -= 8) {
					raw.write((int) (value >> shift) & 255);
				}
			}
			data = raw.toByteArray();
		}
		if (dictionary.contains("/FlateDecode")) {
			Inflater inflater = new Inflater();
			inflater.setInput(data);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[1 << 16];
			while (!inflater.finished()) {
				int length = inflater.inflate(buffer);
				if (length == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
					break;
				}
				out.write(buffer, 0, length);
			}
			inflater.end();
			data = out.toByteArray();
		}
		return data;
	}
}
