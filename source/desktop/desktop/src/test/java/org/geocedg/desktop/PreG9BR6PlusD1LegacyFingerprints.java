/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.main.AppConfig;
import org.geogebra.common.main.settings.config.AppConfigDefault;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.headless.AppDNoGui;
import org.geogebra.desktop.headless.GFileHandler;
import org.geogebra.desktop.main.LocalizationD;

/**
 * PRE-G9B-R6-plus-D1 legacy byte-identity fingerprints. The same helper was run
 * on the unchanged product tree and on a git archive of the published base before
 * any D1 product edit; its output is the committed fixture. Each tracked document
 * is loaded in a fresh headless app (the GeoCeDG preflight configuration and the
 * Classic default configuration) and the SHA-256 of its construction XML and of
 * its full XML is recorded.
 */
final class PreG9BR6PlusD1LegacyFingerprints {

	/** Fixture resource of the committed fingerprints. */
	static final String FIXTURE =
			"org/geocedg/desktop/pre-g9b-r6-plus-d1/legacy-construction-fingerprints.json";

	private static final String SAMPLES =
			"source/desktop/desktop/src/main/java/org/geogebra/desktop/geogebra3D/samples/";
	private static final String DESKTOP_RESOURCES = "source/desktop/desktop/src/test/resources/";
	private static final String SHARED_RESOURCES =
			"source/shared/common-jre/src/test/resources/org/";
	private static final String REFERENCE_MODELS = "docs/references/cedg/models/g9p/";
	private static final String STUDY_MODELS = "docs/validation/g10p-study-optimization/models/";

	/** Every tracked .ggb and .cedg document outside source/web at the base. */
	static final List<String> CORPUS = List.of(
			REFERENCE_MODELS + "geocedg-reference-general-construction-workflow.ggb",
			REFERENCE_MODELS + "geocedg-reference-locus-cylindrical-graft-development.ggb",
			REFERENCE_MODELS + "geocedg-reference-locus-focal-sphere-illumination.ggb",
			REFERENCE_MODELS + "geocedg-reference-locus-truncated-cone-cylinder-connections.ggb",
			STUDY_MODELS + "ArticulatedDoor.ggb",
			STUDY_MODELS + "SphericValve.ggb",
			STUDY_MODELS + "SphericValveV2.ggb",
			STUDY_MODELS + "TruncatedCone.ggb",
			STUDY_MODELS + "dyscrete-elbows/CylElbow_3V.ggb",
			STUDY_MODELS + "dyscrete-elbows/coneElbow_3V.ggb",
			"models/legacy/inter-cil-cono-oblique-two-levels/original/"
					+ "InterCilConoObliqueTwoLevels.ggb",
			"models/legacy/inter-cil-cono-oblique/original/InterCilConoOblique.ggb",
			"models/legacy/template-v7/original/Templatev7.ggb",
			"models/regression/g9a2-spatial-point-pilot/g9a2-spatial-point-pilot.ggb",
			"models/regression/pre-g9b-s1-dxf/original/Elipse.cedg",
			"models/regression/pre-g9b-s1-dxf/original/TestExport1.cedg",
			"models/regression/pre-g9b-s3-text-view-scaling/pre-g9b-s3-text-view-scaling.ggb",
			"source/desktop/desktop/src/e2eTest/resources/circles.ggb",
			SAMPLES + "canon.ggb",
			SAMPLES + "colored-cube.ggb",
			SAMPLES + "cone.ggb",
			SAMPLES + "cube-and-sphere.ggb",
			SAMPLES + "cube.ggb",
			SAMPLES + "dandelin.ggb",
			SAMPLES + "dandelin2.ggb",
			SAMPLES + "geodesique.ggb",
			SAMPLES + "mode_circleaxispoint.ggb",
			SAMPLES + "mode_circlepointradiusdirection.ggb",
			SAMPLES + "mode_orthogonalplane.ggb",
			SAMPLES + "mode_parallelplane.ggb",
			SAMPLES + "mode_planepointline.ggb",
			SAMPLES + "mode_planethreepoint.ggb",
			SAMPLES + "mode_prism.ggb",
			SAMPLES + "mode_rightprism.ggb",
			SAMPLES + "mode_rotate.ggb",
			SAMPLES + "mode_sphere2.ggb",
			SAMPLES + "mode_spherepointradius.ggb",
			SAMPLES + "murkle-14thStellationIcosahedron2.ggb",
			SAMPLES + "murkle-morphing-polyhedra.ggb",
			SAMPLES + "polyhedron-cascade.ggb",
			SAMPLES + "polyhedron.ggb",
			SAMPLES + "pyramidVA.ggb",
			SAMPLES + "salade.ggb",
			SAMPLES + "spring.ggb",
			SAMPLES + "stack.ggb",
			SAMPLES + "stylebar_rotateview.ggb",
			SAMPLES + "stylebar_viewanaglyph.ggb",
			SAMPLES + "stylebar_viewcav.ggb",
			SAMPLES + "stylebar_vieworthographic.ggb",
			SAMPLES + "stylebar_viewperspective.ggb",
			SAMPLES + "test.ggb",
			SAMPLES + "tetrahedron-and-plane-construction.ggb",
			SAMPLES + "tetrahedron-and-plane-empty.ggb",
			SAMPLES + "tetrahedron-and-plane.ggb",
			SAMPLES + "viewInFrontOf.ggb",
			SAMPLES + "view_default.ggb",
			SAMPLES + "view_perspective.ggb",
			SAMPLES + "view_xy.ggb",
			SAMPLES + "view_xz.ggb",
			SAMPLES + "view_yz.ggb",
			DESKTOP_RESOURCES + "org/geocedg/desktop/g9u1-review/TestBasic1.cedg",
			DESKTOP_RESOURCES + "org/geocedg/desktop/pre-g9b-r5-b/pre-g9b-r5-b-historical-es.cedg",
			DESKTOP_RESOURCES + "svg/material-VrT75QCK.ggb",
			DESKTOP_RESOURCES + "svg/svgs.ggb",
			SHARED_RESOURCES + "geocedg/common/locus/g9u0-r2/locusFromMidpoint.cedg",
			SHARED_RESOURCES + "geocedg/common/locus/g9u0-r4/fourSolutions.cedg",
			SHARED_RESOURCES + "geocedg/common/locus/g9u0-r5/fourSolutionsDynamicDilate.cedg",
			SHARED_RESOURCES + "geogebra/common/io/ziptest.ggb");

	/** Fingerprint of one document in one configuration. */
	static final class Fingerprint {
		final String constructionXmlSha256;
		final String fullXmlSha256;
		final String exclusion;
		final boolean containsUnitElement;

		Fingerprint(String constructionXmlSha256, String fullXmlSha256,
				String exclusion, boolean containsUnitElement) {
			this.constructionXmlSha256 = constructionXmlSha256;
			this.fullXmlSha256 = fullXmlSha256;
			this.exclusion = exclusion;
			this.containsUnitElement = containsUnitElement;
		}
	}

	private PreG9BR6PlusD1LegacyFingerprints() {
	}

	/**
	 * @param root repository root
	 * @param classic whether to use the Classic default configuration
	 * @param loads number of fresh loads per document (2 checks determinism)
	 * @return fingerprints by repository-relative path, sorted
	 */
	static Map<String, Fingerprint> compute(Path root, boolean classic, int loads) {
		Map<String, Fingerprint> result = new TreeMap<>();
		for (String path : CORPUS) {
			result.put(path, fingerprint(root.resolve(path), classic, loads));
		}
		return result;
	}

	static Fingerprint fingerprint(Path file, boolean classic, int loads) {
		byte[] archive;
		try {
			archive = Files.readAllBytes(file);
		} catch (Exception exception) {
			return new Fingerprint(null, null, "unreadable", false);
		}
		String construction = null;
		String full = null;
		boolean unitElement = false;
		for (int i = 0; i < loads; i++) {
			String[] xml;
			try {
				xml = load(archive, classic);
			} catch (Throwable failure) {
				return new Fingerprint(null, null,
						"load failed: " + failure.getClass().getName(), false);
			}
			if (xml == null) {
				return new Fingerprint(null, null, "load rejected (MyError)", false);
			}
			String c = sha256(xml[0]);
			String f = sha256(xml[1]);
			unitElement |= xml[0].contains("geocedgUnits");
			if (construction != null
					&& (!construction.equals(c) || !full.equals(f))) {
				return new Fingerprint(null, null,
						"non-deterministic across fresh loads", false);
			}
			construction = c;
			full = f;
		}
		return new Fingerprint(construction, full, null, unitElement);
	}

	/**
	 * Loads on the event thread: a document that starts an animation on load ticks
	 * on that thread, so no tick can interleave with the load and the read.
	 *
	 * @return construction XML and full XML, or null when the reader rejected it
	 */
	static String[] load(byte[] archive, boolean classic) throws Exception {
		AtomicReference<Object> outcome = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
			try {
				outcome.set(loadOnEventThread(archive, classic));
			} catch (Throwable failure) {
				outcome.set(failure);
			}
		});
		Object value = outcome.get();
		if (value instanceof Exception) {
			throw (Exception) value;
		}
		if (value instanceof Error) {
			throw (Error) value;
		}
		return (String[]) value;
	}

	private static String[] loadOnEventThread(byte[] archive, boolean classic)
			throws Exception {
		AppConfig config = classic ? new AppConfigDefault()
				: new AppConfigGeoCeDG(true, true);
		Log previousLogger = Log.getLogger();
		AppDNoGui app;
		try {
			app = new AppDNoGui(new LocalizationD(3), true, config);
		} finally {
			Log.setLogger(previousLogger);
		}
		if (config instanceof AppConfigGeoCeDG) {
			((AppConfigGeoCeDG) config).getRuntimeFeatureService()
					.bindPreservationContext(() -> app.getKernel()
							.getConstruction().isFileLoading());
		}
		try {
			if (!GFileHandler.loadXML(app, new ByteArrayInputStream(archive), false)) {
				return null;
			}
			StringBuilder construction = new StringBuilder();
			app.getKernel().getConstruction().getConstructionXML(
					new XMLStringBuilder(construction), false);
			return new String[] {construction.toString(), app.getXML()};
		} finally {
			// A running animation would otherwise keep the application reachable
			// through its timer for the whole test JVM.
			app.getKernel().getAnimationManager().stopAnimation();
		}
	}

	/** @return deterministic JSON of both configurations, LF line ends */
	static String render(Map<String, Fingerprint> geocedg,
			Map<String, Fingerprint> classic) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n  \"schemaVersion\": 1,\n");
		sb.append("  \"phase\": \"PRE-G9B-R6-plus-D1\",\n");
		sb.append("  \"method\": \"PreG9BR6PlusD1LegacyFingerprints.compute, two fresh")
				.append(" headless loads per document\",\n");
		sb.append("  \"geocedg\": ");
		renderMap(sb, geocedg);
		sb.append(",\n  \"classic\": ");
		renderMap(sb, classic);
		sb.append("\n}\n");
		return sb.toString();
	}

	private static void renderMap(StringBuilder sb, Map<String, Fingerprint> map) {
		sb.append("[\n");
		List<String> rows = new ArrayList<>();
		for (Map.Entry<String, Fingerprint> entry : map.entrySet()) {
			Fingerprint fp = entry.getValue();
			StringBuilder row = new StringBuilder("    {\"path\": \"")
					.append(entry.getKey()).append('"');
			if (fp.exclusion != null) {
				row.append(", \"excluded\": \"").append(fp.exclusion).append('"');
			} else {
				row.append(", \"constructionXmlSha256\": \"")
						.append(fp.constructionXmlSha256).append('"')
						.append(", \"fullXmlSha256\": \"").append(fp.fullXmlSha256)
						.append('"');
			}
			row.append('}');
			rows.add(row.toString());
		}
		sb.append(String.join(",\n", rows)).append("\n  ]");
	}

	static String sha256(String text) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(text.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte b : digest) {
				hex.append(Character.forDigit((b >> 4) & 0xF, 16))
						.append(Character.forDigit(b & 0xF, 16));
			}
			return hex.toString();
		} catch (Exception exception) {
			throw new IllegalStateException(exception);
		}
	}

	static Path findRepositoryRoot() {
		Path candidate = Path.of("").toAbsolutePath().normalize();
		while (candidate != null) {
			if (Files.isRegularFile(candidate.resolve("AGENTS.md"))
					&& Files.isDirectory(candidate.resolve("geocedg"))) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		throw new IllegalStateException("GeoCeDG repository root not found");
	}
}
