/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.freehep.graphics2d.font.CharTable;
import org.freehep.graphics2d.font.Lookup;
import org.freehep.graphicsio.AbstractVectorGraphicsIO;
import org.freehep.graphicsio.pdf.PDFGraphics2D;
import org.freehep.graphicsio.svg.SVGGraphics2D;
import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GProperty;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.common.main.App.ExportType;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.export.GraphicExportDialog;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-F4-D (TD-PDF-2): with "Export text as shapes" unchecked, every PDF
 * that draws text failed with {@link StackOverflowError} because the inherited FreeHEP
 * {@code AbstractCharTable.toName(char)} called itself (upstream {@code c4fd03ce}). Each
 * obligation of the canonical prompt is a separate test; obligations 6 to 8 compare
 * digests with fixtures produced by this same class on the base product
 * ({@code 0c788526}), after only the normalizations the existing fingerprint tests
 * apply (PDF date strings, SVG clip identifiers).
 *
 * <p>With {@code GEOCEDG_PRE_G9B_R6_PLUS_F4_D_FINGERPRINT_OUT=<directory>} the digests
 * are written there instead of being compared (fixture production on the base); with
 * {@code GEOCEDG_PRE_G9B_R6_PLUS_F4_D_PDF_OUT=<directory>} every exported PDF is also
 * copied there for the external renderer evidence.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusF4DEmbeddedFontPdfTest {
	private static final String FIXTURES = "pre-g9b-r6-plus-f4-d/";
	private static final String FINGERPRINT_OUT =
			"GEOCEDG_PRE_G9B_R6_PLUS_F4_D_FINGERPRINT_OUT";
	private static final String PDF_OUT = "GEOCEDG_PRE_G9B_R6_PLUS_F4_D_PDF_OUT";
	private static final String TEXT = "GeoCeDG F4-D 0123";
	private static final String ACCENTED = "Cota \u00f1 \u00e1 \u00e9 \u00dc (x)";
	private static final String[] TABLES = {"PDFLatin", "Symbol", "Zapfdingbats"};
	/** The PDF standard 14 fonts, which FreeHEP references by name without a program. */
	private static final Set<String> STANDARD_FONTS = Set.of("Courier", "Courier-Bold",
			"Courier-Oblique", "Courier-BoldOblique", "Helvetica", "Helvetica-Bold",
			"Helvetica-Oblique", "Helvetica-BoldOblique", "Times-Roman", "Times-Bold",
			"Times-Italic", "Times-BoldItalic", "Symbol", "ZapfDingbats");
	private static final String[] PDF_DEFAULT_KEYS = {PDFGraphics2D.EMBED_FONTS,
		PDFGraphics2D.EMBED_FONTS_AS, AbstractVectorGraphicsIO.TEXT_AS_SHAPES};
	private static final String[] SVG_DEFAULT_KEYS = {SVGGraphics2D.EMBED_FONTS,
		AbstractVectorGraphicsIO.TEXT_AS_SHAPES};

	/** The four production routes that write a FreeHEP PDF. */
	private enum Route {
		GEOCEDG_PHYSICAL, GEOCEDG_LEGACY, GEOCEDG_DIALOG, CLASSIC_DIALOG
	}

	/** An exported file and the strings its scene draws. */
	private static final class Export {
		final File file;
		final List<String> strings;

		Export(File file, List<String> strings) {
			this.file = file;
			this.strings = strings;
		}
	}

	@TempDir
	Path temporary;

	/** The strings drawn by the scene built last. */
	private List<String> drawn;

	// ---------------------------------------------------------- the delegation

	@Test
	void characterLookupDelegatesToTheCharacterOverloadForEveryCharacter() {
		for (String tableName : TABLES) {
			CharTable table = Lookup.getInstance().getTable(tableName);
			assertNotNull(table, tableName);
			int named = 0;
			for (int code = 0; code <= Character.MAX_VALUE; code++) {
				char c = (char) code;
				String expected = table.toName(Character.valueOf(c));
				String actual = table.toName(c);
				if (expected == null ? actual != null : !expected.equals(actual)) {
					assertEquals(expected, actual, tableName + " U+" + hex(code));
				}
				int encoding = expectedEncoding(table, expected);
				int actualEncoding = table.toEncoding(c);
				if (encoding != actualEncoding) {
					assertEquals(encoding, actualEncoding, tableName + " encoding U+" + hex(code));
				}
				named += expected == null ? 0 : 1;
			}
			assertTrue(named > 0, tableName + " names no character");
		}
	}

	// ------------------------------------------------------------ obligation 1

	@Test
	void obligation1EmbeddedFontRoutesFinishWithoutStackOverflow() throws Exception {
		Map<Route, String> failures = new LinkedHashMap<>();
		for (Route route : Route.values()) {
			try {
				Export export = export(route, false);
				assertTrue(export.file.isFile(), route + " wrote no file");
			} catch (StackOverflowError e) {
				failures.put(route, e.getClass().getName() + " at " + top(e));
			}
		}
		assertEquals(Collections.emptyMap(), failures,
				"embedded-font PDF routes ending in StackOverflowError");
	}

	// ------------------------------------------------------------ obligation 2

	@Test
	void obligation2EmbeddedFontPdfsAreComplete() throws Exception {
		for (Route route : Route.values()) {
			Export export = export(route, false);
			byte[] bytes = Files.readAllBytes(export.file.toPath());
			assertTrue(bytes.length > 0, route + " empty file");
			String raw = new String(bytes, StandardCharsets.ISO_8859_1);
			assertTrue(raw.startsWith("%PDF-1."), route + " header");
			assertTrue(raw.stripTrailing().endsWith("%%EOF"), route + " %%EOF");
			assertTrue(raw.contains("\ntrailer") || raw.contains("\rtrailer"),
					route + " trailer");
			assertTrue(raw.contains("startxref"), route + " startxref");
			PdfFile pdf = new PdfFile(raw);
			assertFalse(pdf.pages().isEmpty(), route + " page");
			String content = pdf.pageContent();
			assertTrue(content.contains("BT") && content.contains("Tj")
					&& content.contains("ET"), route + " text in the content stream");
			assertTrue(content.contains(" l") || content.contains(" re"),
					route + " linework in the content stream");
		}
	}

	// ------------------------------------------------------------ obligation 3

	@Test
	void obligation3TextFontsAreType1AndEveryNonStandardFontIsEmbedded()
			throws Exception {
		for (Route route : Route.values()) {
			PdfFile pdf = new PdfFile(read(export(route, false).file));
			Map<String, String> resources = pdf.fontResources();
			List<TextBlock> blocks = pdf.textBlocks();
			assertFalse(blocks.isEmpty(), route + " text blocks");
			int embedded = 0;
			for (TextBlock block : blocks) {
				String ref = resources.get(block.font);
				assertNotNull(ref, route + " font resource " + block.font);
				String font = pdf.object(ref);
				assertTrue(font.contains("/Type /Font"), route + " font dictionary " + ref);
				assertTrue(font.contains("/Subtype /Type1"), route + " Type 1 font " + ref);
				String base = name(font, "BaseFont");
				assertNotNull(base, route + " BaseFont " + ref);
				if (STANDARD_FONTS.contains(base)) {
					continue;
				}
				String descriptor = pdf.object(reference(font, "FontDescriptor"));
				String program = pdf.object(reference(descriptor, "FontFile"));
				assertTrue(program.contains("/Length1"), route + " Type 1 program " + base);
				String decoded = pdf.stream(program);
				assertTrue(decoded.startsWith("%!"), route + " Type 1 program header "
						+ base);
				embedded++;
			}
			// the scene font is not a standard 14 font, so its program must be embedded
			assertTrue(embedded > 0, route + " no text uses an embedded font program");
		}
	}

	// ------------------------------------------------------------ obligation 4

	@Test
	void obligation4ExtractedTextEqualsTheSourceStrings() throws Exception {
		for (Route route : Route.values()) {
			Export export = export(route, false);
			PdfFile pdf = new PdfFile(read(export.file));
			List<String> extracted = new ArrayList<>();
			for (TextBlock block : pdf.textBlocks()) {
				extracted.add(pdf.decode(block));
			}
			List<String> expected = new ArrayList<>(export.strings);
			Collections.sort(expected);
			Collections.sort(extracted);
			assertEquals(expected, extracted, route + " extracted text");
		}
	}

	// ------------------------------------------------------------ obligation 6

	@Test
	void obligation6TextAsShapesPdfsAreIdenticalToTheBase() throws Exception {
		Map<String, String> digests = new TreeMap<>();
		digests.put("geocedg.physical.shapes.pdf",
				sha256(normalizedPdf(export(Route.GEOCEDG_PHYSICAL, true).file)));
		digests.put("geocedg.legacy.shapes.pdf",
				sha256(normalizedPdf(export(Route.GEOCEDG_LEGACY, true).file)));
		digests.put("geocedg.dialog.shapes.pdf",
				sha256(normalizedPdf(export(Route.GEOCEDG_DIALOG, true).file)));
		digests.put("classic.dialog.shapes.pdf",
				sha256(normalizedPdf(export(Route.CLASSIC_DIALOG, true).file)));
		compareWithBase("o6", digests);
	}

	// ------------------------------------------------------------ obligation 7

	@Test
	void obligation7ClassicRasterAndEmfAreIdenticalToTheBase() throws Exception {
		Map<String, String> digests = new TreeMap<>();
		AppD classic = classic();
		EuclidianView view = classic.getEuclidianView1();
		double scale = classicScale(view);
		int width = (int) Math.floor(view.getExportWidth() * scale);
		int height = (int) Math.floor(view.getExportHeight() * scale);
		File png = temporary.resolve("classic.png").toFile();
		GraphicExportDialog.exportPNG((EuclidianViewD) view, png, false, 72, scale, false,
				ExportType.PNG);
		BufferedImage image = ImageIO.read(png);
		assertNotNull(image, "classic PNG");
		digests.put("classic.png.pixels", sha256(pixels(image)));
		File emf = temporary.resolve("classic.emf").toFile();
		GraphicExportDialog.exportEMF((EuclidianViewD) view, emf, false, width, height,
				scale);
		digests.put("classic.emf", sha256(Files.readAllBytes(emf.toPath())));
		compareWithBase("o7", digests);
	}

	// ------------------------------------------------------------ obligation 8

	@Test
	void obligation8SvgWithAndWithoutEmbeddedFontsIsIdenticalToTheBase()
			throws Exception {
		Map<String, String> digests = new TreeMap<>();
		for (boolean physical : new boolean[] {true, false}) {
			AppGeoCeDG app = product(physical);
			for (boolean shapes : new boolean[] {true, false}) {
				ByteArrayOutputStream svg = new ByteArrayOutputStream();
				app.getPictureExportService().writeSVG(app.getEuclidianView1(), svg, shapes,
						-1, 1, false);
				digests.put("geocedg." + (physical ? "physical" : "legacy")
						+ (shapes ? ".shapes" : ".embedded") + ".svg", sha256(normalizedSvg(svg)));
			}
		}
		AppD classic = classic();
		EuclidianView view = classic.getEuclidianView1();
		double scale = classicScale(view);
		int width = (int) Math.floor(view.getExportWidth() * scale);
		int height = (int) Math.floor(view.getExportHeight() * scale);
		Map<String, String> saved = defaults(SVGGraphics2D.getDefaultProperties(),
				SVG_DEFAULT_KEYS);
		try {
			for (boolean shapes : new boolean[] {true, false}) {
				ByteArrayOutputStream svg = new ByteArrayOutputStream();
				GraphicExportDialog.exportSVG(classic, view, svg, shapes, width, height, -1,
						-1, scale, false);
				digests.put("classic.dialog" + (shapes ? ".shapes" : ".embedded") + ".svg",
						sha256(normalizedSvg(svg)));
			}
		} finally {
			restore(SVGGraphics2D.getDefaultProperties(), saved);
		}
		compareWithBase("o8", digests);
	}

	// ------------------------------------------------------------------ export

	private Export export(Route route, boolean textAsShapes) throws Exception {
		File file = temporary.resolve(route.name().toLowerCase(Locale.ROOT)
				+ (textAsShapes ? "-shapes" : "-embedded") + ".pdf").toFile();
		List<String> strings;
		switch (route) {
		case GEOCEDG_PHYSICAL:
		case GEOCEDG_LEGACY:
			AppGeoCeDG app = product(route == Route.GEOCEDG_PHYSICAL);
			strings = drawn;
			app.getPictureExportService().writePDF(app.getEuclidianView1(), file,
					textAsShapes);
			break;
		case GEOCEDG_DIALOG:
			AppGeoCeDG dialog = product(true);
			strings = drawn;
			// the picture dialog's PDF writer delegates to the application route
			GraphicExportDialog.exportPDF(dialog.getEuclidianView1(), file, textAsShapes, 1,
					1, 1);
			break;
		default:
			AppD classic = classic();
			strings = drawn;
			EuclidianView view = classic.getEuclidianView1();
			double scale = classicScale(view);
			Map<String, String> saved = defaults(PDFGraphics2D.getDefaultProperties(),
					PDF_DEFAULT_KEYS);
			try {
				GraphicExportDialog.exportPDF((EuclidianViewD) view, file, textAsShapes,
						(int) Math.floor(view.getExportWidth() * scale),
						(int) Math.floor(view.getExportHeight() * scale), scale);
			} finally {
				restore(PDFGraphics2D.getDefaultProperties(), saved);
			}
			break;
		}
		String out = System.getenv(PDF_OUT);
		if (out != null && !out.isEmpty() && file.isFile()) {
			Files.createDirectories(Paths.get(out));
			Files.copy(file.toPath(), Paths.get(out, file.getName()),
					StandardCopyOption.REPLACE_EXISTING);
		}
		return new Export(file, strings);
	}

	private static double classicScale(EuclidianView view) {
		view.setPrintingScale(0.1);
		return view.getPrintingScale() * 72 / 2.54 / view.getXscale();
	}

	private static Map<String, String> defaults(Properties properties, String[] keys) {
		Map<String, String> saved = new HashMap<>();
		for (String key : keys) {
			saved.put(key, properties.getProperty(key));
		}
		return saved;
	}

	/** The Classic writers store their options in the JVM-wide FreeHEP defaults. */
	private static void restore(Properties properties, Map<String, String> saved) {
		for (Map.Entry<String, String> entry : saved.entrySet()) {
			if (entry.getValue() == null) {
				properties.remove(entry.getKey());
			} else {
				properties.setProperty(entry.getKey(), entry.getValue());
			}
		}
	}

	// ------------------------------------------------------------------ scenes

	private AppGeoCeDG product(boolean physical) throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		sized(app.getEuclidianView1());
		app.getDocumentUnits().replace(physical ? UnitState.of(UnitToken.MM, null, null)
				: UnitState.EMPTY);
		drawn = scene(app.getKernel().getAlgebraProcessor()::processAlgebraCommand);
		assertTrue(app.getExportAreaSession().defineManual(App.VIEW_EUCLIDIAN, -10, 50, -10,
				20));
		return app;
	}

	private AppD classic() throws Exception {
		AppD classic = G9U1TestApp.withoutWindowDispatcher(new AppD(new CommandLineArguments(
				new String[] {"--silent"}), new JPanel(), true));
		sized(classic.getEuclidianView1());
		drawn = scene(classic.getKernel().getAlgebraProcessor()::processAlgebraCommand);
		classic.getKernel().getAlgebraProcessor().processAlgebraCommand("Export_1=(-10,-10)",
				false);
		classic.getKernel().getAlgebraProcessor().processAlgebraCommand("Export_2=(50,20)",
				false);
		for (String label : new String[] {"Export_1", "Export_2"}) {
			GeoElement point = classic.getKernel().lookupLabel(label);
			point.setEuclidianVisible(false);
			point.updateRepaint();
		}
		return classic;
	}

	private interface Evaluator {
		GeoElementND[] eval(String command, boolean storeUndo);
	}

	/**
	 * A labelled segment, two construction texts and a dimension with its value.
	 *
	 * @return the strings the scene draws
	 */
	private static List<String> scene(Evaluator evaluator) {
		GeoElement segment = first(evaluator, "Seg=Segment((0,0),(40,0))");
		segment.setObjColor(GColor.newColor(255, 0, 0));
		segment.setLabelMode(GeoElementND.LABEL_NAME);
		segment.setLabelVisible(true);
		segment.updateVisualStyleRepaint(GProperty.COLOR);
		segment.updateRepaint();
		text((GeoText) first(evaluator, "T1=Text(\"" + TEXT + "\",(0,8))"),
				GColor.newColor(0, 0, 255));
		text((GeoText) first(evaluator, "T2=Text(\"" + ACCENTED + "\",(0,14))"),
				GColor.newColor(0, 128, 0));
		GeoElement[] dimension = first(evaluator,
				"d=AlignedDimension((0,-6),(40,-6),-2,2,1)").getParentAlgorithm().getOutput();
		AlgoNativeDimension algo = AlgoNativeDimension.ownerOf(dimension[0]);
		for (GeoElement input : algo.getInput()) {
			if (input.isGeoPoint()) {
				input.setEuclidianVisible(false);
				input.updateRepaint();
			}
		}
		return List.of(segment.getLabelDescription(), TEXT, ACCENTED,
				algo.getPresentationText().getTextStringSafe());
	}

	private static void text(GeoText text, GColor color) {
		text.setObjColor(color);
		text.setFontSizeMultiplier(4);
		text.setLabelVisible(false);
		text.updateVisualStyleRepaint(GProperty.FONT);
		text.updateRepaint();
	}

	private static GeoElement first(Evaluator evaluator, String command) {
		return evaluator.eval(command, false)[0].toGeoElement();
	}

	private static void sized(EuclidianView view) throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(800, 600);
			view.updateSize();
			view.setCoordSystem(100, 500, 8, 8);
			view.setShowAxes(false, false);
			view.showGrid(false);
		});
	}

	// ------------------------------------------------------------- PDF reading

	/** One BT ... ET block: the font resource it selects and its string bytes. */
	static final class TextBlock {
		final String font;
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		TextBlock(String font) {
			this.font = font;
		}
	}

	/** Indirect objects of a FreeHEP PDF, read as ISO-8859-1 text. */
	static final class PdfFile {
		private final Map<String, String> objects = new LinkedHashMap<>();

		PdfFile(String raw) {
			Matcher object = Pattern.compile("(?s)(?:^|[\\r\\n])(\\d+) 0 obj(.*?)endobj")
					.matcher(raw);
			while (object.find()) {
				objects.put(object.group(1) + " 0 R", object.group(2));
			}
		}

		String object(String reference) {
			String body = objects.get(reference);
			assertNotNull(body, "object " + reference);
			return body;
		}

		List<String> pages() {
			List<String> pages = new ArrayList<>();
			for (String body : objects.values()) {
				if (body.contains("/Type /Page") && !body.contains("/Type /Pages")) {
					pages.add(body);
				}
			}
			return pages;
		}

		String pageContent() throws Exception {
			StringBuilder content = new StringBuilder();
			for (String page : pages()) {
				content.append(stream(object(reference(page, "Contents"))));
			}
			return content.toString();
		}

		/** The decoded stream of an object body. */
		String stream(String body) throws Exception {
			if (body.contains("/Filter")) {
				return PostE2P1DimensionCaptureCharacterizationTest.pdfContent(body);
			}
			int start = body.indexOf("stream") + "stream".length();
			while (body.charAt(start) == '\r' || body.charAt(start) == '\n') {
				start++;
			}
			return body.substring(start, body.lastIndexOf("endstream"));
		}

		/** Font resource names of the page resources mapped to their objects. */
		Map<String, String> fontResources() {
			Map<String, String> fonts = new HashMap<>();
			for (String body : objects.values()) {
				if (body.contains("/Font") && body.contains("/ProcSet")) {
					String value = body.substring(body.indexOf("/Font") + "/Font".length())
							.trim();
					String dictionary = value.startsWith("<<") ? value
							: object(value.substring(0, value.indexOf('R') + 1));
					Matcher entry = Pattern.compile("/([A-Za-z0-9]+)\\s+(\\d+ 0 R)")
							.matcher(dictionary);
					while (entry.find()) {
						fonts.put(entry.group(1), entry.group(2));
					}
				}
			}
			return fonts;
		}

		List<TextBlock> textBlocks() throws Exception {
			String content = pageContent();
			List<TextBlock> blocks = new ArrayList<>();
			TextBlock current = null;
			String font = null;
			List<String> operands = new ArrayList<>();
			int i = 0;
			while (i < content.length()) {
				char c = content.charAt(i);
				if (Character.isWhitespace(c)) {
					i++;
				} else if (c == '(') {
					ByteArrayOutputStream string = new ByteArrayOutputStream();
					i = literal(content, i, string);
					operands.add("(" + new String(string.toByteArray(),
							StandardCharsets.ISO_8859_1));
				} else if (c == '[' || c == ']') {
					i++;
				} else {
					int end = i;
					while (end < content.length() && !Character.isWhitespace(content.charAt(end))
							&& "()[]".indexOf(content.charAt(end)) < 0) {
						end++;
					}
					String token = content.substring(i, end);
					i = end;
					if (token.startsWith("/") || token.matches("[-+]?[0-9.]+")) {
						operands.add(token);
						continue;
					}
					switch (token) {
					case "BT":
						current = new TextBlock(font);
						break;
					case "Tf":
						font = operands.get(operands.size() - 2).substring(1);
						if (current != null && current.bytes.size() == 0) {
							current = new TextBlock(font);
						}
						break;
					case "Tj":
					case "TJ":
						assertNotNull(current, "text shown outside BT");
						assertEquals(current.font, font, "one font per text block");
						for (String operand : operands) {
							if (operand.startsWith("(")) {
								byte[] b = operand.substring(1).getBytes(
										StandardCharsets.ISO_8859_1);
								current.bytes.write(b, 0, b.length);
							}
						}
						break;
					case "ET":
						assertNotNull(current, "ET without BT");
						blocks.add(current);
						current = null;
						break;
					default:
						break;
					}
					operands.clear();
				}
			}
			return blocks;
		}

		/** Character codes to Unicode through the font's /Differences glyph names. */
		String decode(TextBlock block) {
			String font = object(fontResources().get(block.font));
			String encoding = object(reference(font, "Encoding"));
			String[] names = new String[256];
			Matcher differences = Pattern.compile("/Differences\\s*\\[([^\\]]*)\\]")
					.matcher(encoding);
			assertTrue(differences.find(), "Differences of " + block.font);
			int code = 0;
			for (String item : differences.group(1).trim().split("\\s+")) {
				if (item.startsWith("/")) {
					names[code++] = item.substring(1);
				} else {
					code = Integer.parseInt(item);
				}
			}
			CharTable latin = Lookup.getInstance().getTable("PDFLatin");
			StringBuilder text = new StringBuilder();
			for (byte b : block.bytes.toByteArray()) {
				String glyph = names[b & 255];
				assertNotNull(glyph, "glyph of code " + (b & 255));
				char unicode = latin.toUnicode(glyph);
				assertTrue(unicode != 0 && unicode != '\uffff', "Unicode of " + glyph);
				text.append(unicode);
			}
			return text.toString();
		}
	}

	/** Reads a PDF literal string starting at {@code (}; returns the index after it. */
	private static int literal(String content, int start, ByteArrayOutputStream out) {
		int depth = 0;
		int i = start;
		while (i < content.length()) {
			char c = content.charAt(i++);
			if (c == '\\') {
				char next = content.charAt(i++);
				if (next >= '0' && next <= '7') {
					int value = next - '0';
					for (int k = 0; k < 2 && i < content.length()
							&& content.charAt(i) >= '0' && content.charAt(i) <= '7'; k++) {
						value = value * 8 + content.charAt(i++) - '0';
					}
					out.write(value);
				} else {
					int index = "nrtbf".indexOf(next);
					out.write(index >= 0 ? "\n\r\t\b\f".charAt(index) : next);
				}
			} else if (c == '(') {
				if (depth++ > 0) {
					out.write(c);
				}
			} else if (c == ')') {
				if (--depth == 0) {
					return i;
				}
				out.write(c);
			} else {
				out.write(c);
			}
		}
		throw new AssertionError("unterminated PDF string");
	}

	static String reference(String body, String key) {
		Matcher m = Pattern.compile("/" + key + "\\s+(\\d+ 0 R)").matcher(body);
		assertTrue(m.find(), "/" + key + " reference");
		return m.group(1);
	}

	static String name(String body, String key) {
		Matcher m = Pattern.compile("/" + key + "\\s*/([^\\s/\\[\\]()<>]+)").matcher(body);
		return m.find() ? m.group(1) : null;
	}

	// ----------------------------------------------------------------- digests

	private void compareWithBase(String obligation, Map<String, String> digests)
			throws Exception {
		String json = json(digests);
		String out = System.getenv(FINGERPRINT_OUT);
		if (out != null && !out.isEmpty()) {
			Files.createDirectories(Paths.get(out));
			Files.writeString(Paths.get(out, obligation + "-base-fingerprints.json"), json,
					StandardCharsets.UTF_8);
			return;
		}
		String resource = FIXTURES + obligation + "-base-fingerprints.json";
		InputStream fixture = PreG9BR6PlusF4DEmbeddedFontPdfTest.class
				.getResourceAsStream(resource);
		assertNotNull(fixture, resource);
		String expected = new String(fixture.readAllBytes(), StandardCharsets.UTF_8);
		Matcher entry = Pattern.compile("\"([a-z0-9.]+)\": \"([0-9a-f]{64})\"")
				.matcher(expected);
		int count = 0;
		while (entry.find()) {
			assertEquals(entry.group(2), digests.get(entry.group(1)),
					obligation + " " + entry.group(1));
			count++;
		}
		assertEquals(digests.size(), count, obligation + ": every digest has a base value");
	}

	static String read(File file) throws Exception {
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1);
	}

	/** The existing PDF normalization: creation and modification date strings. */
	private static byte[] normalizedPdf(File file) throws Exception {
		return read(file).replaceAll("\\(D:[0-9+\\-Z']*\\)", "(D:)")
				.getBytes(StandardCharsets.ISO_8859_1);
	}

	/** The existing SVG normalization: generated clip-path identifiers. */
	private static byte[] normalizedSvg(ByteArrayOutputStream svg) {
		return svg.toString(StandardCharsets.UTF_8).replaceAll("clip[0-9a-f-]+", "clip")
				.getBytes(StandardCharsets.UTF_8);
	}

	private static byte[] pixels(BufferedImage image) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(image.getWidth() >> 8);
		out.write(image.getWidth());
		out.write(image.getHeight() >> 8);
		out.write(image.getHeight());
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int rgb = image.getRGB(x, y);
				out.write(rgb >> 24);
				out.write(rgb >> 16);
				out.write(rgb >> 8);
				out.write(rgb);
			}
		}
		return out.toByteArray();
	}

	private static String sha256(byte[] bytes) throws Exception {
		StringBuilder hex = new StringBuilder();
		for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes)) {
			hex.append(String.format("%02x", b));
		}
		return hex.toString();
	}

	private static String json(Map<String, String> digests) {
		StringBuilder json = new StringBuilder("{\n");
		int i = 0;
		for (Map.Entry<String, String> entry : digests.entrySet()) {
			json.append("  \"").append(entry.getKey()).append("\": \"")
					.append(entry.getValue()).append('"')
					.append(++i < digests.size() ? ",\n" : "\n");
		}
		return json.append("}\n").toString();
	}

	private static int expectedEncoding(CharTable table, String name) {
		if (name == null) {
			return 0;
		}
		try {
			int encoding = table.toEncoding(name);
			return encoding > 255 ? 0 : encoding;
		} catch (Exception e) {
			return 0;
		}
	}

	private static String hex(int code) {
		return String.format("%04X", code);
	}

	private static String top(Throwable t) {
		StackTraceElement[] trace = t.getStackTrace();
		return trace.length == 0 ? "?" : trace[0].toString();
	}
}
