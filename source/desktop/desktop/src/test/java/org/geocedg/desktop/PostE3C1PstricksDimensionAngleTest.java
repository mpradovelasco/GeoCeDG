/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.AutoColor;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1: the PSTricks rotation of an E2 dimension value
 * is a plain decimal with at most six fractional digits (PSTricks reads the fractional
 * digits as a TeX integer and refuses ten or more), within 5e-7 degrees of the reading
 * angle. Every other output stays byte-identical to the published base 00b525fd, and the
 * host rotation of other objects (OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION) is not
 * changed. Compilation uses the installed TeX toolchain; a missing tool is reported as
 * unavailable, never replaced by text inspection.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostE3C1PstricksDimensionAngleTest {
	private static final BigDecimal BOUND = new BigDecimal("5E-7");
	private static final Pattern PLAIN = Pattern.compile("-?(0|[1-9][0-9]*)(\\.[0-9]{0,5}[1-9])?");
	private static final Pattern DIMENSION_ROTATION =
			Pattern.compile("\\\\rput\\[b\\]\\{([^}]*)\\}");

	/** {name, A, B, command, expected PSTricks rotation}. */
	private static final String[][] CASES = {
		{"horizontal", "(10,10)", "(60,10)", "AlignedDimension(A,B,8)", "0"},
		{"vertical", "(10,10)", "(10,60)", "AlignedDimension(A,B,8)", "90"},
		{"oblique", "(10,10)", "(60,30)", "AlignedDimension(A,B,8)", "21.801409"},
		{"reversed", "(60,30)", "(10,10)", "AlignedDimension(A,B,8)", "21.801409"},
		{"degrees45", "(10,10)", "(60,60)", "AlignedDimension(A,B,8)", "45"},
		{"degrees135", "(60,10)", "(10,60)", "AlignedDimension(A,B,8)", "-45"},
		{"degrees30", "(10,10)", "(10+50*cos(30°),35)", "AlignedDimension(A,B,8)", "30"},
		{"nearHorizontal", "(10,10)", "(60,10.000000001)", "AlignedDimension(A,B,8)", "0"},
		{"nearVertical", "(10,10)", "(10.000000001,60)", "AlignedDimension(A,B,8)", "90"},
		{"veryShort", "(10,10)", "(10.0001,10.00005)", "AlignedDimension(A,B,8)", "26.565051"},
		{"linearOblique", "(10,10)", "(60,30)", "LinearDimension(A,B,Vector((1,2)),8)",
			"63.434949"},
		{"linearHorizontal", "(10,10)", "(60,30)", "LinearDimension(A,B,Vector((1,0)),8)",
			"0"},
	};

	/**
	 * SHA-256 of the outputs of {@link #mixedScene}, recorded by {@link #regressionDigests}
	 * on the published base 00b525fd before the correction (PSTricks with the dimension
	 * rotation token normalized to {@code \rput[b]{θ}}).
	 */
	private static final Map<String, String> BASE = Map.of(
			"pgf", "5b4c91057df858600c65e6292880df052f175b78eabcaeae75ded2739dae9fd8",
			"asymptote", "2318b3d3d8b9b5014c975ec7b524e92384f1b8bffb8b4a59b1ebf12c02501cfd",
			"pstricks.normalized",
			"027c75e5f04861d6dfc621f8a498e45f50dc6460f21a835d2fd05e33bae9ef5b");

	@TempDir
	Path temporary;

	@Test
	void everyDimensionFamilyIsWrittenWithBoundedPlainDecimals() throws Exception {
		for (String[] c : CASES) {
			AppGeoCeDG app = scene();
			eval(app, "A=" + c[1]);
			eval(app, "B=" + c[2]);
			GeoElement d = eval(app, "d=" + c[3]);
			double raw = readingAngle(d);
			String pst = latex(app, "pstricks");
			Matcher m = DIMENSION_ROTATION.matcher(pst);
			assertTrue(m.find(), c[0] + ": " + pst);
			String written = m.group(1);
			assertFalse(m.find(), c[0] + ": one dimension value");
			assertEquals(c[4], written, c[0]);
			assertTrue(PLAIN.matcher(written).matches(), c[0] + ": plain decimal " + written);
			System.out.println("C1 " + c[0] + " reading=" + raw + " written=" + written
					+ " |dtheta|=" + assertBound(raw, written, c[0]));
			// PGF/TikZ and Asymptote keep the host formatting of the same angle
			assertTrue(latex(app, "pgf").contains("node[rotate=" + hostFormat(app, raw)
					+ ",anchor=south]"), c[0]);
			assertTrue(latex(app, "asymptote").contains("label(rotate(" + hostFormat(app, raw)
					.replace("E", "e") + ")*Label("), c[0]);
		}
	}

	@Test
	void formattingIsBoundedPlainLocaleIndependentAndDeterministic() throws Exception {
		assertEquals("0", angle(0.0));
		assertEquals("0", angle(-0.0));
		assertEquals("0", angle(-1e-9));
		assertEquals("0", angle(1e-300));
		assertEquals("0", angle(4.9e-7));
		assertEquals("0.000001", angle(5.1e-7));
		assertEquals("90", angle(90.0));
		assertEquals("90", angle(89.9999996));
		assertEquals("89.999999", angle(89.9999994));
		assertEquals("-90", angle(-89.9999996));
		assertEquals("-89.999999", angle(-89.9999994));
		assertEquals("21.801409", angle(21.80140948635181));
		assertEquals("1.5", angle(1.5));
		assertEquals("-12.25", angle(-12.25));
		Locale original = Locale.getDefault();
		try {
			for (Locale locale : new Locale[] {Locale.GERMANY, Locale.FRANCE,
					new Locale("ar", "EG"), new Locale("hi", "IN")}) {
				Locale.setDefault(locale);
				assertEquals("21.801409", angle(21.80140948635181), locale.toString());
				assertEquals("-89.999999", angle(-89.9999994), locale.toString());
			}
		} finally {
			Locale.setDefault(original);
		}
		// domain sweep and rounding transitions: every value within the bound, plain
		Random random = new Random(20261010L);
		double worst = 0;
		for (int i = 0; i < 20000; i++) {
			double value = i < 10000 ? -90 + 180 * random.nextDouble()
					: Math.floor(-90e6 + 180e6 * random.nextDouble()) / 1e6 + 5e-7
							+ (random.nextInt(3) - 1) * Math.ulp(90.0);
			String written = angle(value);
			assertTrue(PLAIN.matcher(written).matches(), value + " -> " + written);
			worst = Math.max(worst, assertBound(value, written, Double.toString(value)));
			assertEquals(written, angle(value), "deterministic");
		}
		assertTrue(worst <= 5e-7, "worst " + worst);
		System.out.println("C1 sweep of 20000 angles: max |dtheta|=" + worst);
	}

	@Test
	void previouslyRefusedDimensionsCompileWithLatex() throws Exception {
		Path latex = tool("latex");
		Path dvips = tool("dvips");
		Path ps2pdf = tool("ps2pdf");
		if (latex == null || dvips == null || ps2pdf == null) {
			System.out.println("C1 latex/dvips/ps2pdf UNAVAILABLE");
			return;
		}
		Path pdfinfo = tool("pdfinfo");
		for (String[] c : CASES) {
			AppGeoCeDG app = scene();
			eval(app, "A=" + c[1]);
			eval(app, "B=" + c[2]);
			eval(app, "d=" + c[3]);
			Path dir = Files.createDirectories(temporary.resolve(c[0]));
			Files.writeString(dir.resolve("d.tex"), latex(app, "pstricks"));
			run(dir, latex.toString(), "-interaction=nonstopmode", "-halt-on-error", "d.tex");
			assertTrue(Files.size(dir.resolve("d.dvi")) > 0, c[0] + ": DVI");
			run(dir, dvips.toString(), "-q", "d.dvi", "-o", "d.ps");
			run(dir, ps2pdf.toString(), "d.ps", "d.pdf");
			Path pdf = dir.resolve("d.pdf");
			assertTrue(Files.isRegularFile(pdf) && Files.size(pdf) > 0, c[0] + ": PDF");
			byte[] head = new byte[5];
			System.arraycopy(Files.readAllBytes(pdf), 0, head, 0, 5);
			assertEquals("%PDF-", new String(head, StandardCharsets.ISO_8859_1), c[0]);
			if (pdfinfo != null) {
				String info = run(dir, pdfinfo.toString(), "d.pdf");
				Matcher pages = Pattern.compile("Pages:\\s+(\\d+)").matcher(info);
				assertTrue(pages.find() && Integer.parseInt(pages.group(1)) >= 1,
						c[0] + ": a valid PDF with pages:\n" + info);
				System.out.println("C1 " + c[0] + " pdfinfo pages=" + pages.group(1));
			}
			System.out.println("C1 " + c[0] + " compiled: latex -halt-on-error, dvips, ps2pdf"
					+ " exit 0; PDF " + Files.size(pdf) + " bytes");
		}
	}

	@Test
	void everyOtherOutputIsByteIdenticalToTheBase() throws Exception {
		Map<String, String> digests = regressionDigests();
		String out = System.getenv("GEOCEDG_POST_E3_C1_DIGEST_OUT");
		if (out != null && !out.isEmpty()) {
			Files.writeString(Paths.get(out), digests.toString(), StandardCharsets.UTF_8);
			return;
		}
		assertEquals(new TreeMap<>(BASE), digests);
	}

	@Test
	void hostRotationsKeepTheirInheritedFormatting() throws Exception {
		AppGeoCeDG app = mixedScene();
		String pst = latex(app, "pstricks");
		// OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION stays open: C1 does not touch it
		assertTrue(pst.contains("\\rput{18.434948822922014}"), pst);
	}

	// ----------------------------------------------------------------- helpers

	static Map<String, String> regressionDigests() throws Exception {
		AppGeoCeDG app = mixedScene();
		Map<String, String> digests = new TreeMap<>();
		digests.put("pgf", sha256(latex(app, "pgf")));
		digests.put("asymptote", sha256(latex(app, "asymptote")));
		digests.put("pstricks.normalized", sha256(DIMENSION_ROTATION.matcher(
				latex(app, "pstricks")).replaceAll("\\\\rput[b]{θ}")));
		return digests;
	}

	private static AppGeoCeDG mixedScene() throws Exception {
		AppGeoCeDG app = scene();
		eval(app, "A=(10,10)");
		eval(app, "B=(60,30)");
		eval(app, "d=AlignedDimension(A,B,8)");
		eval(app, "C=(70,20)");
		eval(app, "e=LinearDimension(B,C,Vector((1,2)),6)");
		eval(app, "s=Segment((0,0),(20,40))");
		eval(app, "q=Polyline((0,0),(20,45),(80,10))");
		eval(app, "t=Text(\"plain\",(5,45))");
		eval(app, "c=Circle((70,10),5)");
		eval(app, "ell=Ellipse((0,0),(30,10),(5,20))");
		return app;
	}

	private static AppGeoCeDG scene() throws Exception {
		resetAutomaticColors();
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = PreG9BR6PlusBPictureFidelityTest.sized(app.getEuclidianView1());
		view.setShowAxes(false, false);
		view.showGrid(false);
		PreG9BR6PlusBPictureFidelityTest.define(app, -50, 110, -50, 110);
		return app;
	}

	private static GeoElement eval(AppGeoCeDG app, String command) {
		GeoElement geo = G9U1TestApp.eval(app, command);
		assertNotNull(geo, command);
		return geo;
	}

	private static String latex(AppGeoCeDG app, String dialect) {
		return PreG9BR6PlusCLatexExportTest.generate(
				PreG9BR6PlusCLatexExportTest.exporter(app, dialect), app.getEuclidianView1());
	}

	private static double readingAngle(GeoElement dimension) throws Exception {
		Method placement = exportHelper().getDeclaredMethod("placement",
				AlgoNativeDimension.class, double.class, double.class);
		placement.setAccessible(true);
		double[] place = (double[]) placement.invoke(null,
				(AlgoNativeDimension) dimension.getParentAlgorithm(), 1.0, 1.0);
		assertNotNull(place);
		return place[2];
	}

	private static String angle(double degrees) throws Exception {
		Method format = exportHelper().getDeclaredMethod("pstricksAngle", double.class);
		format.setAccessible(true);
		return (String) format.invoke(null, degrees);
	}

	private static Class<?> exportHelper() throws ClassNotFoundException {
		return Class.forName("org.geocedg.desktop.export.DimensionLatexExport");
	}

	private static String hostFormat(AppGeoCeDG app, double degrees) {
		String number = app.getKernel().format(
				org.geogebra.common.util.DoubleUtil.checkDecimalFraction(degrees),
				org.geogebra.common.kernel.StringTemplate.printFigures(
						org.geogebra.common.kernel.arithmetic.ExpressionNodeConstants.StringType
								.PSTRICKS, 12, false));
		return org.geogebra.common.util.StringUtil.canonicalNumber2(number);
	}

	private static double assertBound(double source, String written, String context) {
		BigDecimal error = new BigDecimal(source).subtract(new BigDecimal(written)).abs();
		assertTrue(error.compareTo(BOUND) <= 0, context + ": |dtheta| = " + error);
		return error.doubleValue();
	}

	private static void resetAutomaticColors() throws ReflectiveOperationException {
		Field index = AutoColor.class.getDeclaredField("index");
		index.setAccessible(true);
		for (AutoColor scheme : AutoColor.values()) {
			index.setInt(scheme, 0);
		}
	}

	private static String sha256(String text) throws Exception {
		return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
				.digest(text.getBytes(StandardCharsets.UTF_8)));
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
		assertEquals(0, process.exitValue(), String.join(" ", command) + ":\n" + output);
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
}
