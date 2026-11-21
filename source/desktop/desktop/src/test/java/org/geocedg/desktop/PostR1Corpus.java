/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;

/**
 * Post-R1 regression corpus: verified access to the byte-exact copy of the author model of
 * 2026-11-20 under {@code models/regression/post-r1-locus-on-intersection/original}, to the
 * derived variant with the transported hole contour, to the public command list that builds
 * that transport and to the independent mathematical reference of the transported contour.
 * Every document is loaded from a working copy, never from the versioned bytes, and every
 * kernel operation runs on the event thread. Object labels are only the way to find the
 * objects of this model; no product code knows them.
 */
final class PostR1Corpus {

	/** The author model: oblique cone and cylinder with the hole loci {@code w_2}, {@code a_3}. */
	static final String ORIGINAL = "InterCilConoObliqueFinal_2.cedg";
	static final String ORIGINAL_SHA256 =
			"949c2c931d22ec33820cd3ae5b35e8a75b94f5f3d45474fb8e818d74ff98159b";
	static final long ORIGINAL_BYTES = 78_781;
	/** The original plus the transported hole contour {@code trHole}. */
	static final String TRANSPORT = "InterCilConoObliqueFinal_2.transport.cedg";
	static final String PACKAGE = "models/regression/post-r1-locus-on-intersection";
	/** Independent reference of the transported contour (not GeoCeDG, not authoritative). */
	static final String REFERENCE =
			"geocedg/validation/post-r1-locus/transport-reference.tsv";
	static final String MAIN = PreG9BCu0Corpus.MAIN;
	/** Label of the transported contour in the derived variant. */
	static final String HOLE = "trHole";

	/**
	 * The direct transport {@code N -> (X(N), R(N)) -> Y(N)} with admitted public forms only:
	 * the versioned command list of the model package, the single source of the recipe.
	 */
	static final String TRANSPORT_COMMANDS = PACKAGE + "/transport-commands.txt";

	private PostR1Corpus() {
	}

	/** @return the versioned original after checking its size and SHA-256 */
	static Path original() throws IOException {
		Path file = PreG9BCu0Corpus.repositoryRoot().resolve(PACKAGE).resolve("original")
				.resolve(ORIGINAL);
		long bytes = Files.size(file);
		String sha = PreG9BCu0Corpus.sha256(Files.readAllBytes(file));
		if (bytes != ORIGINAL_BYTES || !ORIGINAL_SHA256.equals(sha)) {
			throw new IllegalStateException("Corpus identity mismatch for " + ORIGINAL + ": "
					+ bytes + " " + sha);
		}
		return file;
	}

	/** @return the versioned derived variant */
	static Path transport() {
		return PreG9BCu0Corpus.repositoryRoot().resolve(PACKAGE).resolve("derived")
				.resolve(TRANSPORT);
	}

	/** @return a fresh working copy of a versioned document */
	static File workingCopy(Path versioned, Path directory) throws IOException {
		Path target = directory.resolve(versioned.getFileName().toString());
		Files.write(target, Files.readAllBytes(versioned));
		return target.toFile();
	}

	/** Runs one public command and fails with its error text. */
	static GeoElementND[] command(AppGeoCeDG app, String command) {
		GeoElementND[] result = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		if (result == null || result.length == 0) {
			throw new IllegalStateException("Command produced nothing: " + command);
		}
		return result;
	}

	/** Builds the transported contour on a loaded original; call on the event thread. */
	static GeoLocusV2 buildTransport(AppGeoCeDG app) {
		try {
			for (String line : Files.readAllLines(PreG9BCu0Corpus.repositoryRoot()
					.resolve(TRANSPORT_COMMANDS), StandardCharsets.UTF_8)) {
				if (!line.isBlank() && !line.startsWith("#")) {
					command(app, line.trim());
				}
			}
		} catch (IOException failure) {
			throw new IllegalStateException("Transport recipe is unavailable", failure);
		}
		return PreG9BCu0Corpus.locus(app, HOLE);
	}

	/** One row of the independent reference: driver parameter and developed point. */
	static final class ReferenceRow {
		final double theta;
		final double x;
		final double y;
		final double conjugateX;
		final double conjugateY;

		private ReferenceRow(double theta, double x, double y, double conjugateX,
				double conjugateY) {
			this.theta = theta;
			this.x = x;
			this.y = y;
			this.conjugateX = conjugateX;
			this.conjugateY = conjugateY;
		}
	}

	/** @return the reference rows of the contour of the finite cone (family {@code R}) */
	static List<ReferenceRow> reference() throws IOException {
		List<ReferenceRow> rows = new ArrayList<>();
		for (String line : Files.readAllLines(PreG9BCu0Corpus.repositoryRoot()
				.resolve(REFERENCE), StandardCharsets.UTF_8)) {
			String[] cells = line.split("\t");
			if (cells.length == 5 && !"thetaN".equals(cells[0])) {
				rows.add(new ReferenceRow(Double.parseDouble(cells[0]),
						Double.parseDouble(cells[1]), Double.parseDouble(cells[2]),
						Double.parseDouble(cells[3]), Double.parseDouble(cells[4])));
			}
		}
		return rows;
	}

	/** @return status and exact coordinate bits of a locus at fixed parameters */
	static List<String> evaluations(GeoLocusV2 locus, double[] parameters) {
		List<String> rows = new ArrayList<>();
		for (double parameter : parameters) {
			LocusEvaluation2D evaluation = locus.evaluate(MAIN, parameter,
					LocusEvaluationSession2D.reference());
			rows.add(parameter + ":" + evaluation.getStatus() + (evaluation.isValid()
					? ":" + Double.toHexString(evaluation.getPoint().getX()) + ","
							+ Double.toHexString(evaluation.getPoint().getY())
					: ""));
		}
		return rows;
	}

	static GeoElement geo(AppGeoCeDG app, String label) {
		GeoElement geo = app.getKernel().lookupLabel(label);
		if (geo == null) {
			throw new IllegalStateException("Missing object: " + label);
		}
		return geo;
	}
}
