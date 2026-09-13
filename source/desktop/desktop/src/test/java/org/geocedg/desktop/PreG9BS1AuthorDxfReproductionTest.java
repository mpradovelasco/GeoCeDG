/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.geocedg.common.export.GeometryExportModel;
import org.geocedg.common.export.GeometryExportModel.EllipseGeometry;
import org.geocedg.common.export.GeometryExportModel.GeometryType;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportPreflight;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.common.export.G9X1GeometryExportAdapter;
import org.geocedg.common.export.SourceExportOutcome.Fidelity;
import org.geocedg.common.export.SourceExportOutcome.Reason;
import org.geocedg.common.kernel.algos.AlgoSplineV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.desktop.export.DxfFidelityManifestWriter;
import org.geocedg.desktop.export.DxfPreparedOutput;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.headless.GFileHandler;
import org.geogebra.desktop.util.LoggerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Artifact-backed reproduction probes for PRE-G9B-S1. */
class PreG9BS1AuthorDxfReproductionTest {

	private static final Path FIXTURES = repositoryRoot().resolve(Path.of(
			"models", "regression", "pre-g9b-s1-dxf", "original"));
	private static final String ELLIPSE_SOURCE_SHA256 =
			"4ec66797984fe68cb6baf43fd141484ce45907f9a52e883ccfe0933dc411cea6";
	private static final String SEMANTIC_SOURCE_SHA256 =
			"3ad3900ccfd76ccb751f802f44764808ff26f6f4cdc58d26506f4e97c9b80612";
	private final GeometryExportService service = new GeometryExportService();

	@RegisterExtension
	final G9U1TestApp.Lifecycle lifecycle = new G9U1TestApp.Lifecycle();

	@Test
	void ellipseArtifactProducesTwoExactEllipseEntities() throws Exception {
		AppGeoCeDG app = app();
		Path source = fixture("Elipse.cedg", 16_379, ELLIPSE_SOURCE_SHA256);
		assertTrue(GFileHandler.loadXML(app, Files.newInputStream(source), false));
		GeometryExportModel model = service.createModel(sources(app),
				SelectionMode.COMPLETE_CONSTRUCTION);

		List<EllipseGeometry> ellipses = model.getEntities().stream()
				.filter(entity -> entity.getGeometry().getType() == GeometryType.ELLIPSE)
				.map(entity -> assertInstanceOf(EllipseGeometry.class,
						entity.getGeometry())).toList();
		assertEquals(2, ellipses.size());
		assertEquals(3.32, ellipses.get(0).getMajorAxis().norm(), 1e-12);
		assertEquals(0.6204819277108433,
				ellipses.get(0).getMinorMajorRatio(), 1e-12);
		assertEquals(3.32, ellipses.get(1).getMajorAxis().norm(), 1e-12);
		assertEquals(0.39759036144578314,
				ellipses.get(1).getMinorMajorRatio(), 1e-12);
		String dxf = service.exportDxf(model);
		assertEquals(2, entityCount(dxf, "ELLIPSE"));

		app.getEuclidianView1().setCoordSystem(400, 250, 120, 80);
		assertEquals(dxf, service.exportDxf(service.createModel(sources(app),
				SelectionMode.COMPLETE_CONSTRUCTION)));
	}

	@Test
	void semanticCurvesExposeApprovedExtendedDxfDisposition() throws Exception {
		AppGeoCeDG app = app();
		Path source = fixture("TestExport1.cedg", 25_350,
				SEMANTIC_SOURCE_SHA256);
		assertTrue(GFileHandler.loadXML(app, Files.newInputStream(source), false));
		final String constructionBefore = app.getXML();
		List<GeoElement> semanticCurves = sources(app).stream()
				.filter(GeoLocusV2.class::isInstance).toList();

		assertEquals(3, semanticCurves.size());
		assertEquals(List.of("IL1", "IL2", "m"), semanticCurves.stream()
				.map(GeoElement::getLabelSimple).sorted().toList());
		assertEquals(0, entityCount(service.exportDxf(service.createModel(sources(app),
				SelectionMode.COMPLETE_CONSTRUCTION)), "LWPOLYLINE"));

		List<GeoElement> splines = semanticCurves.stream()
				.filter(geo -> geo.getParentAlgorithm() instanceof AlgoSplineV2)
				.toList();
		assertEquals(2, splines.size());
		assertTrue(splines.stream().allMatch(geo ->
				"Semantic Spline V2".equals(geo.translatedTypeString())));
		GeometryExportPreflight splinePreflight = service.preflight(splines,
				SelectionMode.CURRENT_SELECTION,
				GeometryExportRequest.builder(0.001).build());
		assertTrue(splinePreflight.isWritable());
		assertTrue(splinePreflight.isSidecarRequired());
		assertEquals(2, splinePreflight.getApproximateCount());
		assertEquals(2, splinePreflight.getModel().getEntities().size());
		var encoding = service.encode(splinePreflight);
		assertEquals(2, entityCount(encoding.getDxfText(),
				"LWPOLYLINE"));
		assertEquals(2, encoding.getEncodedEntities().size());
		assertTrue(encoding.getEncodedEntities().values().stream()
				.allMatch(entity -> "LWPOLYLINE".equals(entity.getEntityType())));
		DxfPreparedOutput paired = new DxfFidelityManifestWriter().prepare(
				splinePreflight, encoding);
		assertTrue(paired.hasManifest());
		String manifest = new String(paired.getManifest().getBytes(),
				StandardCharsets.UTF_8);
		assertTrue(manifest.contains("\"sha256\":\""
				+ paired.getDxfSha256() + "\""));
		for (var entity : encoding.getEncodedEntities().values()) {
			assertTrue(manifest.contains("\"dxf_handle\":\""
					+ entity.getHandle() + "\""));
		}

		GeoElement locus = semanticCurves.stream()
				.filter(geo -> !(geo.getParentAlgorithm() instanceof AlgoSplineV2))
				.findFirst()
				.orElseThrow();
		assertEquals("Semantic Locus V2", locus.translatedTypeString());
		GeoLocusV2 locusV2 = (GeoLocusV2) locus;
		LocusDefinition2D definition = locusV2.getSemanticDefinition();
		assertEquals(DefinitionStatus.VALID, definition.getDefinitionStatus());
		assertTrue(definition.getProvider().isPeriodic());
		LocusBranch2D branch = definition.getBranches().get(0);
		assertTrue(branch.getProperties().contains(BranchProperty.PERIODIC));
		assertEquals(1, branch.getValidDomainComponents().size());
		LocusInterval2D component = branch.getValidDomainComponents().get(0);
		assertEquals(-Math.PI, component.getLower());
		assertEquals(Math.PI, component.getUpper());
		assertTrue(component.isLowerClosed());
		assertFalse(component.isUpperClosed());
		assertEquals(component, branch.getDeclaredDriverDomain());
		assertEquals(component, definition.getProvider().getDeclaredDomain());
		assertEquals(component.getLower(),
				definition.getProvider().canonicalize(component.getUpper()));
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.memoizing(32)) {
			assertEquals(List.of(EvaluationStatus.VALID, EvaluationStatus.VALID,
					EvaluationStatus.VALID, EvaluationStatus.DEPENDENCY_UNDEFINED,
					EvaluationStatus.VALID, EvaluationStatus.DEPENDENCY_UNDEFINED,
					EvaluationStatus.DEPENDENCY_UNDEFINED,
					EvaluationStatus.DEPENDENCY_UNDEFINED,
					EvaluationStatus.DEPENDENCY_UNDEFINED,
					EvaluationStatus.DEPENDENCY_UNDEFINED,
					EvaluationStatus.DEPENDENCY_UNDEFINED,
					EvaluationStatus.DEPENDENCY_UNDEFINED), List.of(
					definition.evaluate(branch.getBranchKey(), component.getLower(),
							session).getStatus(),
					definition.evaluate(branch.getBranchKey(), component.getUpper(),
							session).getStatus(),
					definition.evaluate(branch.getBranchKey(), 0, session).getStatus(),
					definition.evaluate(branch.getBranchKey(), -Math.PI / 2,
							session).getStatus(),
					definition.evaluate(branch.getBranchKey(), Math.PI / 2,
							session).getStatus(),
					definition.evaluate(branch.getBranchKey(), -Math.PI / 2 - 1e-6,
							session).getStatus(),
					definition.evaluate(branch.getBranchKey(), -Math.PI / 2 + 1e-6,
							session).getStatus(),
					definition.evaluate(branch.getBranchKey(), -2.5, session).getStatus(),
					definition.evaluate(branch.getBranchKey(), -2, session).getStatus(),
					definition.evaluate(branch.getBranchKey(), -1.8, session).getStatus(),
					definition.evaluate(branch.getBranchKey(), -1.5, session).getStatus(),
					definition.evaluate(branch.getBranchKey(), -1, session).getStatus()));
		}
		GeometryExportPreflight locusPreflight = service.preflight(List.of(locus),
				SelectionMode.CURRENT_SELECTION,
				GeometryExportRequest.builder(0.001).build());
		assertFalse(locusPreflight.isWritable());
		assertEquals(1, locusPreflight.getInvalidCount());
		assertEquals(0, locusPreflight.getModel().getEntities().size());
		assertEquals(Fidelity.INVALID,
				locusPreflight.getModel().getOutcomes().get(0).getFidelity());
		assertEquals(Reason.DISCONTINUITY_UNRESOLVED,
				locusPreflight.getModel().getOutcomes().get(0).getReason());

		GeometryExportPreflight combined = service.preflight(semanticCurves,
				SelectionMode.CURRENT_SELECTION,
				GeometryExportRequest.builder(0.001).build());
		assertFalse(combined.isWritable());
		assertEquals(2, combined.getApproximateCount());
		assertEquals(1, combined.getInvalidCount());
		assertNotEquals(0, combined.getModel().getEntities().size());
		List<GeoElement> sources = sources(app);
		GeometryExportPreflight complete = service.preflight(sources,
				SelectionMode.COMPLETE_CONSTRUCTION,
				GeometryExportRequest.builder(0.001).build());
		List<String> population = new ArrayList<>();
		for (var outcome : complete.getModel().getOutcomes()) {
			if (outcome.getFidelity() != Fidelity.UNSUPPORTED
					&& outcome.getFidelity() != Fidelity.INVALID) {
				continue;
			}
			GeoElement geo = null;
			for (int index = 0; index < sources.size(); index++) {
				if (outcome.getSourceId().equals(
						G9X1GeometryExportAdapter.requestSourceId(
								sources.get(index), index))) {
					geo = sources.get(index);
					break;
				}
			}
			population.add(outcome.getLabel() + "|" + outcome.getSourceType()
					+ "|" + outcome.getFidelity() + "|" + outcome.getReason()
					+ "|visible=" + outcome.isVisible() + "|auxiliary="
					+ (geo != null && geo.isAuxiliaryObject()) + "|parent="
					+ (geo == null || geo.getParentAlgorithm() == null ? "none"
							: geo.getParentAlgorithm().getClass().getSimpleName()));
		}
		assertEquals(26, complete.getExactCount());
		assertEquals(2, complete.getApproximateCount());
		assertEquals(15, complete.getUnsupportedCount());
		assertEquals(1, complete.getInvalidCount());
		assertEquals(14, complete.getHiddenCount());
		assertFalse(complete.isWritable());
		assertEquals(List.of(
				"Iso1|LIST|UNSUPPORTED|UNSUPPORTED_FAMILY|visible=false|"
						+ "auxiliary=false|parent=AlgoDependentList",
				"Iso2|LIST|UNSUPPORTED|UNSUPPORTED_FAMILY|visible=true|"
						+ "auxiliary=false|parent=AlgoDependentList",
				"a|NUMERIC|UNSUPPORTED|UNSUPPORTED_FAMILY|visible=false|"
						+ "auxiliary=false|parent=none",
				"b|NUMERIC|UNSUPPORTED|UNSUPPORTED_FAMILY|visible=false|"
						+ "auxiliary=false|parent=none",
				"c|LOCUS_INTERSECTION_RESULT|UNSUPPORTED|UNSUPPORTED_FAMILY|"
						+ "visible=false|auxiliary=false|parent=AlgoLocusIntersectionV2",
				"text1|TEXT|UNSUPPORTED|UNSUPPORTED_FAMILY|visible=false|"
						+ "auxiliary=true|parent=none",
				"d|LOCUS_INTERSECTION_RESULT|UNSUPPORTED|UNSUPPORTED_FAMILY|"
						+ "visible=false|auxiliary=false|parent=AlgoLocusIntersectionV2",
				"text2|TEXT|UNSUPPORTED|UNSUPPORTED_FAMILY|visible=false|"
						+ "auxiliary=true|parent=none",
				"e|LOCUS_INTERSECTION_RESULT|UNSUPPORTED|UNSUPPORTED_FAMILY|"
						+ "visible=false|auxiliary=false|parent=AlgoLocusIntersectionV2",
				"text3|TEXT|UNSUPPORTED|UNSUPPORTED_FAMILY|visible=false|"
						+ "auxiliary=true|parent=none",
				"i|LOCUS_INTERSECTION_RESULT|UNSUPPORTED|UNSUPPORTED_FAMILY|"
						+ "visible=false|auxiliary=false|parent=AlgoLocusIntersectionV2",
				"text4|TEXT|UNSUPPORTED|UNSUPPORTED_FAMILY|visible=false|"
						+ "auxiliary=true|parent=none",
				"l|LOCUS_INTERSECTION_RESULT|UNSUPPORTED|UNSUPPORTED_FAMILY|"
						+ "visible=false|auxiliary=false|parent=AlgoLocusIntersectionV2",
				"text5|TEXT|UNSUPPORTED|UNSUPPORTED_FAMILY|visible=false|"
						+ "auxiliary=true|parent=none",
				"text6|TEXT|UNSUPPORTED|UNSUPPORTED_FAMILY|visible=false|"
						+ "auxiliary=true|parent=none",
				"m|LOCUS_V2|INVALID|DISCONTINUITY_UNRESOLVED|visible=true|"
						+ "auxiliary=false|parent=AlgoDependentPointLocusV2"),
				population);
		assertEquals(constructionBefore, app.getXML());
	}

	private static Path fixture(String name, long size, String sha256)
			throws Exception {
		Path source = FIXTURES.resolve(name);
		assertEquals(size, Files.size(source));
		assertEquals(sha256, sha256(Files.readAllBytes(source)));
		return source;
	}

	private static String sha256(byte[] bytes) throws Exception {
		return java.util.HexFormat.of().formatHex(
				MessageDigest.getInstance("SHA-256").digest(bytes));
	}

	private static AppGeoCeDG app() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
		AppGeoCeDG app = new AppGeoCeDG(new CommandLineArguments(new String[] {
				"--silent", "--enableLocusV2=true", "--enableExtendedDxf=true"
		}), new JPanel());
		app.setErrorDialogsActive(false);
		return G9U1TestApp.withoutWindowDispatcher(app);
	}

	private static List<GeoElement> sources(AppGeoCeDG app) {
		return new ArrayList<>(app.getKernel().getConstruction()
				.getGeoSetConstructionOrder());
	}

	private static long entityCount(String dxf, String type) {
		String[] lines = dxf.split("\\r?\\n");
		long count = 0;
		for (int index = 0; index + 1 < lines.length; index += 2) {
			if ("0".equals(lines[index]) && type.equals(lines[index + 1])) {
				count++;
			}
		}
		return count;
	}

	private static Path repositoryRoot() {
		Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
		while (current != null && !Files.isRegularFile(current.resolve("AGENTS.md"))) {
			current = current.getParent();
		}
		if (current == null) {
			throw new IllegalStateException("Repository root is unavailable");
		}
		return current;
	}
}
