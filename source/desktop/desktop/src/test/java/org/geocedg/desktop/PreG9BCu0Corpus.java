/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;

/**
 * PRE-G9B-CU0 regression corpus: verified access to the byte-exact copies of the two author
 * constructions of 2026-10-19 under {@code models/regression/pre-g9b-cu/original}, working
 * copies, the derived variant without {@code e_2} and the loading path used by the
 * characterization tests and by the reproducible harness. Every document is loaded from a
 * working copy, never from the versioned bytes, and every kernel operation runs on the event
 * thread because the Locus V2 metric owner is confined to its creating thread.
 */
final class PreG9BCu0Corpus {

	/** Native author model with the seven Locus V2 curves. */
	static final String CEDG = "InterCilConoOblique.cedg";
	/** Classic author model used for insertion and orientation characterization. */
	static final String GGB = "InterCilConoObliqueTest.ggb";
	static final String CEDG_SHA256 =
			"1ae6f51293fb111b54510948cbcc6e8ba38ab63b658ba3f8a668858ae890456c";
	static final long CEDG_BYTES = 58_896;
	static final String GGB_SHA256 =
			"88779d58bcbcb08449eec8ffe6f74982238c84020a347f4cf2011f20a2690398";
	static final long GGB_BYTES = 25_203;
	/** Repository-relative folder of the versioned copies. */
	static final String CORPUS = "models/regression/pre-g9b-cu/original";
	/** Repository-relative CU0 geometric reference baseline. */
	static final String BASELINE =
			"geocedg/validation/pre-g9b-cu/cu0-geometric-reference-baseline.json";
	/** Branch key of every Locus V2 of the corpus. */
	static final String MAIN = "generator.main";
	/** The author's Graphics 1 view of the native model (pixels per model unit). */
	static final double AUTHOR_SCALE = 23.40288335405867;
	static final int AUTHOR_WIDTH = 1071;
	static final int AUTHOR_HEIGHT = 780;
	/** Origin of the author's Graphics 1 view in screen pixels. */
	static final double AUTHOR_X_ZERO = 218.0621986290022;
	static final double AUTHOR_Y_ZERO = 395.8366112468825;

	/** The exact XML block of {@code e_2 = LocusV2(R_1, E_1)}, the last construction step. */
	private static final Pattern E2_BLOCK = Pattern.compile("<command name=\"LocusV2\">\\s*"
			+ "<input a0=\"R_1\" a1=\"E_1\"/>\\s*<output a0=\"e_2\"/>\\s*</command>\\s*"
			+ "<element type=\"locusv2\" label=\"e_2\" geocedgId=\"(geo:[0-9a-f]+)\">.*?"
			+ "</element>\\s*(?=</construction>)", Pattern.DOTALL);

	private PreG9BCu0Corpus() {
	}

	static Path repositoryRoot() {
		Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
		while (current != null && !Files.isRegularFile(current.resolve("AGENTS.md"))) {
			current = current.getParent();
		}
		if (current == null) {
			throw new IllegalStateException("Repository root is unavailable");
		}
		return current;
	}

	/** @return the versioned copy after checking its size and SHA-256 */
	static Path versioned(String name) throws IOException {
		Path file = repositoryRoot().resolve(CORPUS).resolve(name);
		long bytes = Files.size(file);
		String sha = sha256(Files.readAllBytes(file));
		long expectedBytes = CEDG.equals(name) ? CEDG_BYTES : GGB_BYTES;
		String expectedSha = CEDG.equals(name) ? CEDG_SHA256 : GGB_SHA256;
		if (bytes != expectedBytes || !expectedSha.equals(sha)) {
			throw new IllegalStateException("Corpus identity mismatch for " + name + ": "
					+ bytes + " " + sha);
		}
		return file;
	}

	/** @return a fresh working copy of a verified corpus document */
	static File workingCopy(String name, Path directory) throws IOException {
		Path target = directory.resolve(name);
		Files.write(target, Files.readAllBytes(versioned(name)));
		return target.toFile();
	}

	/**
	 * Derived working variant of the native model without its last step
	 * {@code e_2 = LocusV2(R_1, E_1)} and without the durable-identity record of
	 * {@code e_2}, which no other record references. Every other object keeps its
	 * definition; the reference harness checks that the variant reproduces the state of the
	 * complete model after removing {@code e_2}. The variant only avoids the cost of
	 * {@code e_2} where a characterization does not concern it.
	 */
	static File withoutE2(Path directory) throws IOException {
		Path target = directory.resolve("InterCilConoOblique-without-e_2.cedg");
		try (ZipInputStream in = new ZipInputStream(Files.newInputStream(versioned(CEDG)));
				ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(target))) {
			ZipEntry entry;
			boolean edited = false;
			while ((entry = in.getNextEntry()) != null) {
				byte[] bytes = readAll(in);
				if ("geogebra.xml".equals(entry.getName())) {
					String xml = new String(bytes, StandardCharsets.UTF_8);
					Matcher matcher = E2_BLOCK.matcher(xml);
					if (!matcher.find()) {
						throw new IllegalStateException("e_2 block not found");
					}
					String id = matcher.group(1);
					String edit = xml.substring(0, matcher.start()) + xml.substring(matcher.end());
					Matcher record = Pattern.compile("[ \\t]*<geo id=\"" + id
							+ "\"[^>]*/>\\r?\\n").matcher(edit);
					if (!record.find()) {
						throw new IllegalStateException("e_2 identity record not found");
					}
					edit = edit.substring(0, record.start()) + edit.substring(record.end());
					if (E2_BLOCK.matcher(edit).find() || edit.contains("\"e_2\"")
							|| edit.contains(id)) {
						throw new IllegalStateException("e_2 block is not unique");
					}
					bytes = edit.getBytes(StandardCharsets.UTF_8);
					edited = true;
				}
				out.putNextEntry(new ZipEntry(entry.getName()));
				out.write(bytes);
				out.closeEntry();
			}
			if (!edited) {
				throw new IllegalStateException("geogebra.xml not found");
			}
		}
		return target.toFile();
	}

	/** @return the {@code geogebra.xml} of a verified corpus document */
	static String constructionXml(String name) throws IOException {
		try (ZipInputStream in = new ZipInputStream(Files.newInputStream(versioned(name)))) {
			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				if ("geogebra.xml".equals(entry.getName())) {
					return new String(readAll(in), StandardCharsets.UTF_8);
				}
			}
		}
		throw new IllegalStateException("geogebra.xml not found in " + name);
	}

	/** Loads a working copy into a fresh embedded host; call on the event thread. */
	static AppGeoCeDG open(File file) {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
		if (!app.loadFile(file, false)) {
			throw new IllegalStateException("Could not load " + file);
		}
		return app;
	}

	static GeoLocusV2 locus(AppGeoCeDG app, String label) {
		GeoElement geo = app.getKernel().lookupLabel(label);
		if (!(geo instanceof GeoLocusV2 locus)) {
			throw new IllegalStateException("Not a Locus V2: " + label);
		}
		return locus;
	}

	/** @return the evaluated point, or null when the evaluation is not valid */
	static LocusPoint2D point(GeoLocusV2 locus, double t, LocusEvaluationSession2D session) {
		LocusEvaluation2D evaluation = locus.evaluate(MAIN, t, session);
		return evaluation.isValid() ? evaluation.getPoint() : null;
	}

	static void setNumber(AppGeoCeDG app, String label, double value) {
		GeoNumeric number = (GeoNumeric) app.getKernel().lookupLabel(label);
		number.setValue(value);
		number.updateRepaint();
	}

	/** Moves a point on a path to a path parameter, as a drag of the driver would. */
	static void movePathPoint(AppGeoCeDG app, String label, double t) {
		GeoPoint point = (GeoPoint) app.getKernel().lookupLabel(label);
		point.getPathParameter().setT(t);
		((org.geogebra.common.kernel.Path) point.getPath()).pathChanged(point);
		point.updateCoords();
		point.updateRepaint();
	}

	static double pathParameter(AppGeoCeDG app, String label) {
		return ((GeoPoint) app.getKernel().lookupLabel(label)).getPathParameter().getT();
	}

	/** The author's view as an adaptive render policy (one view, current product limits). */
	static LocusRenderPolicy2D authorPolicy() {
		return LocusRenderPolicy2D.adaptive(1, AUTHOR_WIDTH, AUTHOR_HEIGHT, AUTHOR_SCALE,
				AUTHOR_SCALE, 32, 0.75, 12);
	}

	static String sha256(byte[] bytes) {
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
			StringBuilder text = new StringBuilder();
			for (byte value : hash) {
				text.append(String.format("%02x", value));
			}
			return text.toString();
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException(impossible);
		}
	}

	private static byte[] readAll(InputStream in) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int read;
		while ((read = in.read(buffer)) > 0) {
			bytes.write(buffer, 0, read);
		}
		return bytes.toByteArray();
	}
}
