/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PRE-G9B-R6-plus-E3-R1: page composition of a physical LaTeX export. The host writes
 * an {@code article} document, whose text block crops any drawing larger than a few
 * centimetres, and a unit in centimetres such as {@code 0.01cm}, which the 16.16
 * fixed-point arithmetic of TeX turns into a scale error of up to 6e-4. A physical
 * export instead gets a page that is exactly the export area ({@code standalone},
 * no border, the export area as TikZ bounding box, since TikZ clipping does not bound
 * the picture) and its unit in TeX points with eight decimals; Asymptote, which fits
 * its picture to {@code size()}, gets the export area as an invisible zero-width
 * outline so that the area, not the visible content, defines the scale and the page.
 * The device-scale (non-physical) output is the host output.
 */
final class PhysicalLatexComposition {

	/** TeX points per centimetre. */
	static final double TEX_POINTS_PER_CM = 72.27 / 2.54;

	private static final Pattern ARTICLE = Pattern.compile(
			"\\\\documentclass\\[(\\d+)pt\\]\\{article\\}");
	private static final Pattern PGF_UNIT = Pattern.compile(
			"x=[0-9.Ee+-]+cm,y=[0-9.Ee+-]+cm");
	private static final Pattern PGF_CLIP = Pattern.compile(
			"\\\\clip(\\([-0-9.Ee+]+,[-0-9.Ee+]+\\) rectangle \\([-0-9.Ee+]+,[-0-9.Ee+]+\\));");
	private static final Pattern PSTRICKS_UNIT = Pattern.compile(
			"xunit=[0-9.Ee+-]+cm,yunit=[0-9.Ee+-]+cm");
	private static final String ASYMPTOTE_CLIP =
			"clip((xmin,ymin)--(xmin,ymax)--(xmax,ymax)--(xmax,ymin)--cycle);";

	private PhysicalLatexComposition() {
	}

	/**
	 * @param code complete PGF/TikZ output
	 * @param cmPerUnit physical output length of one model unit in centimetres
	 * @return the output with the exact page and the unit in TeX points
	 */
	static String pgf(String code, double cmPerUnit) {
		String unit = points(cmPerUnit);
		String composed = PGF_UNIT.matcher(standalone(code))
				.replaceAll(Matcher.quoteReplacement("x=" + unit + ",y=" + unit));
		if (composed.contains("\\begin{axis}")) {
			// a pgfplots axis keeps its own bounding box
			return composed;
		}
		// TikZ clipping does not bound the picture: the export area is its bounding box
		Matcher clip = PGF_CLIP.matcher(composed);
		return clip.find() ? composed.substring(0, clip.start()) + "\\useasboundingbox"
				+ clip.group(1) + ";\n" + composed.substring(clip.start()) : composed;
	}

	/**
	 * @param code complete PSTricks output
	 * @param cmPerUnit physical output length of one model unit in centimetres
	 * @return the output with the exact page and the unit in TeX points
	 */
	static String pstricks(String code, double cmPerUnit) {
		String unit = points(cmPerUnit);
		return PSTRICKS_UNIT.matcher(standalone(code)).replaceAll(
				Matcher.quoteReplacement("xunit=" + unit + ",yunit=" + unit));
	}

	/**
	 * @param code complete Asymptote output
	 * @param cmPerUnit unused; Asymptote computes in floating point
	 * @return the output whose picture bounds are the export area
	 */
	static String asymptote(String code, double cmPerUnit) {
		int clip = code.lastIndexOf(ASYMPTOTE_CLIP);
		if (clip < 0) {
			return code;
		}
		return code.substring(0, clip)
				+ "draw((xmin,ymin)--(xmin,ymax)--(xmax,ymax)--(xmax,ymin)--cycle,"
				+ " invisible+linewidth(0)); /* GeoCeDG: the export area is the page */\n"
				+ code.substring(clip);
	}

	/**
	 * @param cmPerUnit length in centimetres
	 * @return the same length in TeX points, eight decimals
	 */
	static String points(double cmPerUnit) {
		return String.format(Locale.ROOT, "%.8f", cmPerUnit * TEX_POINTS_PER_CM) + "pt";
	}

	private static String standalone(String code) {
		Matcher article = ARTICLE.matcher(code);
		if (!article.find()) {
			return code;
		}
		String composed = article.replaceFirst("\\\\documentclass[$1pt,border=0pt]{standalone}");
		// the host writes colour and unit set-up lines in the document body; on a
		// standalone page each line end would be an interword space beside the picture
		int body = composed.indexOf("\\begin{document}\n");
		int picture = firstOf(composed, "\\begin{tikzpicture}", "\\begin{pspicture");
		if (body < 0 || picture < body) {
			return composed;
		}
		int start = body + "\\begin{document}\n".length();
		StringBuilder setup = new StringBuilder();
		for (String line : composed.substring(start, picture).split("\n", -1)) {
			if (setup.length() > 0) {
				setup.append('\n');
			}
			setup.append(line);
			if (!line.isEmpty() && !line.endsWith("%")) {
				setup.append('%');
			}
		}
		return composed.substring(0, start) + setup + composed.substring(picture);
	}

	private static int firstOf(String text, String... markers) {
		int first = -1;
		for (String marker : markers) {
			int at = text.indexOf(marker);
			if (at >= 0 && (first < 0 || at < first)) {
				first = at;
			}
		}
		return first;
	}
}
