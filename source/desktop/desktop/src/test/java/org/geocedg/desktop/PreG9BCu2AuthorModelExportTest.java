/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.export.GeometryExportModel.Entity;
import org.geocedg.common.export.GeometryExportModel.Point2D;
import org.geocedg.common.export.GeometryExportModel.PolylineGeometry;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportPreflight;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.common.export.SourceExportOutcome;
import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusEvaluator2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.desktop.export.LatexSemanticExportSupport;
import org.geocedg.desktop.export.LatexSemanticExporter;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.export.pstricks.GeoGebraExport;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU2 step T11 on the author model: the DXF and LaTeX exports follow the topology of
 * the published certificates ({@code locus-v2-existence-components.md} section 15.8;
 * consumption design section 6; {@code geocedg/specs/export/latex-semantic-curve-export.md}
 * section 5). Matrix rows V-DXF-1, V-E2-3 (export parts), V-E2-9, V-AVAIL-3 (author model),
 * V-TEX-1 and V-TEX-2.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu2AuthorModelExportTest {
	private static final List<String> FIRST_FAMILY = List.of("o", "v", "w", "a_1");
	private static final List<String> CLOSED = List.of("o", "v", "w", "a_1", "e_1", "o_1");

	@TempDir
	Path work;

	/**
	 * V-DXF-1 and V-E2-3 (DXF part): one {@code LWPOLYLINE} per certified component, with the
	 * closure flag only on a closed periodic component: the six closed loci in penetration
	 * (excCyl 2.9) and the first family as one open arc across the seam in a bite (excCyl
	 * 3.7). {@code e_2} is one open certified component whose ends are never joined; the
	 * evaluation of its point fails computationally in two narrow parameter intervals where
	 * the copy of {@code w_1} reports {@code LIMIT_NOT_ESTABLISHED} (a limitation of the
	 * {@code CU1} metric, unchanged), so its export is partial (P5, A2.4-R to A2.6;
	 * D-CU2-T11-R1): open parts cut at the failures, never interpolated across them, each
	 * declaring both gaps with their cause in the mandatory sidecar.
	 */
	@Test
	void eachCertifiedComponentIsOnePolylineClosedOnlyWhenPeriodic() throws Exception {
		Map<String, String> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			Map<String, String> values = new LinkedHashMap<>();
			for (String label : CLOSED) {
				values.put(label, dxf(app, label));
			}
			values.put("e_2", dxf(app, "e_2"));
			values.put("e_2.parts", parts(app, "e_2"));
			PreG9BCu0Corpus.setNumber(app, "excCyl", 3.7);
			for (String label : FIRST_FAMILY) {
				values.put("bite." + label, dxf(app, label));
			}
			return values;
		});
		for (String label : CLOSED) {
			assertEquals("[1]", row.get(label), label + " " + row);
		}
		assertEquals("[0, 0, 0]", row.get("e_2"), row.toString());
		assertEquals("3 parts, 2 gaps of w_1 LIMIT_NOT_ESTABLISHED, not joined, sidecar",
				row.get("e_2.parts"), row.toString());
		for (String label : FIRST_FAMILY) {
			assertEquals("[0]", row.get("bite." + label), label + " " + row);
		}
	}

	/**
	 * V-E2-9 and V-AVAIL-3 on the author model. The one-sided limit at the excluded end of
	 * {@code e_2} is the value of {@code w_1} under the opposite same-position policy, the
	 * estimated total length of {@code o_1}. A work budget cannot make that limit fail alone:
	 * every value of {@code w_1} reads the same estimate of the length of {@code o_1}, so a
	 * budget that fails the limit fails every interior point too, which is V-E2-5 (probe of
	 * decision D-CU2-T11-R1: with budgets up to 2048 the limit and the seven probed interior
	 * parameters fail, from 3072 all of them evaluate). The test therefore withdraws the copy
	 * of the length from the origin-wrap lookup of the slice while the drawing and the exports
	 * are built, so that the limit is not available while the interior still evaluates: the
	 * drawing and the DXF and LaTeX exports end at the last evaluated vertex and declare the
	 * end ({@code END_NOT_EVALUATED}); nothing is extrapolated to the limit, the existence
	 * structure is unchanged, and restoring the lookup restores the limit.
	 */
	@Test
	@SuppressWarnings("unchecked")
	void anUnevaluatedEndOfE2IsDeclaredAndNeverExtrapolated() throws Exception {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.onEdt(() -> PreG9BCu0Corpus.open(
				PreG9BCu0Corpus.workingCopy(PreG9BCu0Corpus.CEDG, work)));
		EuclidianView view = PreG9BR6PlusBPictureFidelityTest.sized(app.getEuclidianView1());
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_2");
			Field evaluatorField = AlgoDependentPointLocusV2.class.getDeclaredField("evaluator");
			evaluatorField.setAccessible(true);
			ReconstructibleLocusEvaluator2D evaluator = (ReconstructibleLocusEvaluator2D)
					evaluatorField.get(e2.getParentAlgorithm());
			Field sourcesField = ReconstructibleLocusEvaluator2D.class.getDeclaredField(
					"programV3MetricSources");
			sourcesField.setAccessible(true);
			Map<Integer, AlgoLocusBetweenMetricV2> sources =
					(Map<Integer, AlgoLocusBetweenMetricV2>) sourcesField.get(evaluator);
			LocusDefinition2D definition = e2.getSemanticDefinition();
			String structure = definition.getBranch(PreG9BCu0Corpus.MAIN).getExistenceStructure()
					.getSemanticSignature();
			LocusPoint2D limit = componentEnd(definition).getPoint();
			Map<String, Object> values = new LinkedHashMap<>();
			sourcesField.set(evaluator, Map.of());
			try {
				LocusEvaluation2D end = componentEnd(definition);
				values.put("forced", interiorValid(definition) && !end.isValid());
				values.put("end", end.getStatus() + " " + end.getDiagnostic());
				LocusRenderData2D drawing = new LocusRenderCache2D().getOrBuild(e2,
						PreG9BCu0Corpus.authorPolicy());
				values.put("drawing.mark", drawing.getMarks().stream().anyMatch(mark ->
						mark.getKind() == LocusRenderData2D.MarkKind.END_NOT_EVALUATED));
				LocusPoint2D drawn = drawing.getVertices().get(drawing.getVertices().size() - 1)
						.getPoint();
				values.put("drawing.extrapolated", drawn.getX() == limit.getX()
						&& drawn.getY() == limit.getY());
				GeometryExportService service = new GeometryExportService();
				GeoCeDGDxfExportController controller = new GeoCeDGDxfExportController(app);
				GeometryExportPreflight preflight = service.preflight(List.of(e2),
						SelectionMode.CURRENT_SELECTION,
						GeometryExportRequest.builder(0.001).build(), controller::exportContext);
				List<SourceExportOutcome> outcomes = preflight.getModel().getOutcomes();
				SourceExportOutcome last = outcomes.get(outcomes.size() - 1);
				values.put("dxf.declared", last.getDeclarations().stream().anyMatch(
						declaration -> declaration.startsWith("END_NOT_EVALUATED")));
				values.put("dxf.sidecar", preflight.isSidecarRequired());
				List<Entity> entities = preflight.getModel().getEntities();
				List<Point2D> vertices = ((PolylineGeometry) entities.get(entities.size() - 1)
						.getGeometry()).getVertices();
				Point2D exported = vertices.get(vertices.size() - 1);
				values.put("dxf.extrapolated", exported.getX() == limit.getX()
						&& exported.getY() == limit.getY());
				showOnly(app, "e_2");
				values.put("latex.declared", PreG9BR6PlusCLatexExportTest.generate(
						PreG9BR6PlusCLatexExportTest.exporter(app, "pgf"), view)
						.contains("END_NOT_EVALUATED"));
				values.put("existence", structure.equals(definition.getBranch(
						PreG9BCu0Corpus.MAIN).getExistenceStructure().getSemanticSignature()));
			} finally {
				sourcesField.set(evaluator, sources);
			}
			values.put("restored", componentEnd(definition).isValid());
			return values;
		});
		assertEquals(true, row.get("forced"), "only the limit fails: " + row);
		assertEquals("EVALUATION_FAILED " + LocusEvaluator2D.ONE_SIDED_LIMIT_NOT_AVAILABLE,
				((String) row.get("end")).split(":")[0], row.toString());
		assertEquals(true, row.get("drawing.mark"), row.toString());
		assertEquals(false, row.get("drawing.extrapolated"), row.toString());
		assertEquals(true, row.get("dxf.declared"), row.toString());
		assertEquals(true, row.get("dxf.sidecar"), row.toString());
		assertEquals(false, row.get("dxf.extrapolated"), row.toString());
		assertEquals(true, row.get("latex.declared"), row.toString());
		assertEquals(true, row.get("existence"), row.toString());
		assertEquals(true, row.get("restored"), row.toString());
	}

	private static boolean interiorValid(LocusDefinition2D definition) {
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4)) {
			return definition.evaluate(PreG9BCu0Corpus.MAIN, 1, session).isValid();
		}
	}

	/** @return the one-sided limit at the excluded end of the component of {@code e_2} */
	private static LocusEvaluation2D componentEnd(LocusDefinition2D definition) {
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4)) {
			return definition.evaluateComponentEnd(PreG9BCu0Corpus.MAIN, 0, true, session);
		}
	}

	/**
	 * V-TEX-2 (decision D-CU2-11, option a): the LaTeX export tests cite the durable
	 * specification {@code latex-semantic-curve-export.md}, never the validation record of the
	 * decisions that it now holds.
	 */
	@Test
	void theLatexTestsCiteTheDurableSpecification() throws Exception {
		for (String test : List.of(
				"source/desktop/desktop/src/test/java/org/geocedg/desktop/"
						+ "PreG9BR6PlusCLatexExportTest.java",
				"source/desktop/desktop/src/test/java/org/geocedg/desktop/"
						+ "PreG9BCu2ExportDeclarationTest.java",
				"source/desktop/desktop/src/test/java/org/geocedg/desktop/"
						+ "PreG9BCu2AuthorModelExportTest.java",
				"source/shared/common-jre/src/test/java/org/geocedg/common/export/"
						+ "PreG9BR6PlusCSharedExportTest.java")) {
			String source = Files.readString(repositoryRoot().resolve(test),
					StandardCharsets.UTF_8);
			assertTrue(source.contains("latex-semantic-curve-export.md"), test);
			// The record names are split so that this file, which is checked too, never
			// contains them.
			assertFalse(source.contains("pre_g9b_r6_plus_c_" + "author_decisions_record"), test);
			assertFalse(source.contains("pre_g9b_r6_plus_c_" + "prompt_closeout_record"), test);
		}
	}

	private static Path repositoryRoot() {
		Path candidate = Path.of("").toAbsolutePath().normalize();
		while (candidate != null) {
			if (Files.isRegularFile(candidate.resolve("AGENTS.md"))) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		throw new IllegalStateException("GeoCeDG repository root not found");
	}

	/**
	 * @return the parts of the strict DXF export of a locus: their number, the computational
	 *         gaps that every part declares and their cause, whether consecutive parts share
	 *         an end, and whether the sidecar is mandatory
	 */
	private static String parts(AppGeoCeDG app, String label) {
		GeometryExportService service = new GeometryExportService();
		GeoCeDGDxfExportController controller = new GeoCeDGDxfExportController(app);
		GeometryExportPreflight preflight = service.preflight(List.of(
				PreG9BCu0Corpus.locus(app, label)), SelectionMode.CURRENT_SELECTION,
				GeometryExportRequest.builder(0.001).build(), controller::exportContext);
		List<SourceExportOutcome> outcomes = preflight.getModel().getOutcomes();
		Set<String> gaps = new TreeSet<>();
		boolean everyPartDeclares = true;
		for (SourceExportOutcome outcome : outcomes) {
			List<String> declared = outcome.getDeclarations().stream()
					.filter(item -> item.startsWith("COMPUTATIONAL_GAP")).toList();
			everyPartDeclares &= !declared.isEmpty();
			gaps.addAll(declared);
		}
		boolean causes = gaps.stream().allMatch(gap -> gap.contains(
				"dependency length w_1 LIMIT_NOT_ESTABLISHED"));
		boolean joined = false;
		List<Entity> entities = preflight.getModel().getEntities();
		for (int index = 0; index + 1 < entities.size(); index++) {
			List<Point2D> first = ((PolylineGeometry) entities.get(index).getGeometry())
					.getVertices();
			Point2D end = first.get(first.size() - 1);
			Point2D start = ((PolylineGeometry) entities.get(index + 1).getGeometry())
					.getVertices().get(0);
			joined |= end.getX() == start.getX() && end.getY() == start.getY();
		}
		return outcomes.size() + " parts, " + gaps.size() + " gaps of w_1 "
				+ (causes && everyPartDeclares ? "LIMIT_NOT_ESTABLISHED" : "other causes")
				+ ", " + (joined ? "joined" : "not joined") + ", "
				+ (preflight.isSidecarRequired() ? "sidecar" : "no sidecar");
	}

	/**
	 * V-TEX-1 and V-E2-3 (LaTeX part): in PGF, PSTricks and Asymptote a closed periodic
	 * component is the only closed path ({@code -- cycle}, {@code \pspolygon},
	 * {@code --cycle}); a bite is one open path, and {@code e_2} the same three open parts as
	 * in DXF, with the gaps declared in the comments; each document compiles with
	 * {@code -halt-on-error} where the tool is installed.
	 */
	@Test
	void latexClosesOnlyClosedPeriodicComponentsAndCompiles() throws Exception {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.onEdt(() -> PreG9BCu0Corpus.open(
				PreG9BCu0Corpus.workingCopy(PreG9BCu0Corpus.CEDG, work)));
		EuclidianView view = PreG9BR6PlusBPictureFidelityTest.sized(app.getEuclidianView1());
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("o", latex(app, view, "o"));
			values.put("e_2", latex(app, view, "e_2"));
			showOnly(app, "e_2");
			values.put("e_2.comments", PreG9BR6PlusCLatexExportTest.generate(
					PreG9BR6PlusCLatexExportTest.exporter(app, "pgf"), view)
					.contains("COMPUTATIONAL_GAP"));
			PreG9BCu0Corpus.setNumber(app, "excCyl", 3.7);
			values.put("bite.o", latex(app, view, "o"));
			return values;
		});
		assertEquals("1/1/1/1", row.get("o"), row.toString());
		assertEquals("3/0/0/0", row.get("e_2"), row.toString());
		assertEquals("1/0/0/0", row.get("bite.o"), row.toString());
		assertEquals(true, row.get("e_2.comments"), row.toString());
		compile(app, view);
	}

	/**
	 * @return closure flags of the {@code LWPOLYLINE} entities of the strict DXF, written
	 *         with the context of the product controller (an excluded end is declared in the
	 *         mandatory sidecar)
	 */
	private static String dxf(AppGeoCeDG app, String label) {
		GeometryExportService service = new GeometryExportService();
		GeoCeDGDxfExportController controller = new GeoCeDGDxfExportController(app);
		GeometryExportPreflight preflight = service.preflight(List.of(
				PreG9BCu0Corpus.locus(app, label)), SelectionMode.CURRENT_SELECTION,
				GeometryExportRequest.builder(0.001).build(), controller::exportContext);
		if (!preflight.isWritable()) {
			StringBuilder reasons = new StringBuilder("not writable");
			for (SourceExportOutcome outcome : preflight.getModel().getOutcomes()) {
				reasons.append(' ').append(outcome.getReason()).append(": ")
						.append(outcome.getMessage()).append(' ')
						.append(outcome.getDeclarations());
			}
			return reasons.toString();
		}
		String[] lines = service.encode(preflight).getDxfText().split("\r?\n", -1);
		List<String> flags = new ArrayList<>();
		boolean polyline = false;
		for (int index = 0; index + 1 < lines.length; index += 2) {
			String code = lines[index].trim();
			if ("0".equals(code)) {
				polyline = "LWPOLYLINE".equals(lines[index + 1].trim());
			} else if (polyline && "70".equals(code)) {
				flags.add(lines[index + 1].trim());
				polyline = false;
			}
		}
		return flags.toString();
	}

	/**
	 * Shows only one locus and counts its emitted paths and, in its LaTeX output, the closed
	 * paths of PGF, PSTricks and Asymptote beyond those of the empty picture (the Asymptote
	 * frame is itself a closed path).
	 *
	 * @return {@code emitted/pgfClosed/pstricksClosed/asymptoteClosed}, followed by the
	 *         export report when nothing was emitted
	 */
	private static String latex(AppGeoCeDG app, EuclidianView view, String label) {
		showOnly(app, null);
		int[] empty = closedPaths(app, view);
		showOnly(app, label);
		GeoGebraExport export = PreG9BR6PlusCLatexExportTest.exporter(app, "pgf");
		PreG9BR6PlusCLatexExportTest.generate(export, view);
		LatexSemanticExportSupport.Result result = ((LatexSemanticExporter) export)
				.getSemanticSupport().getLastResult();
		int emitted = result.getSources().stream()
				.filter(source -> label.equals(source.getLabel()))
				.mapToInt(LatexSemanticExportSupport.SourceReport::getEmittedCount).sum();
		int[] shown = closedPaths(app, view);
		return emitted + "/" + (shown[0] - empty[0]) + "/" + (shown[1] - empty[1]) + "/"
				+ (shown[2] - empty[2]) + (emitted == 0 ? " " + result.reportText() : "");
	}

	/** @return closed paths of the PGF, PSTricks and Asymptote outputs of the view */
	private static int[] closedPaths(AppGeoCeDG app, EuclidianView view) {
		String pgf = PreG9BR6PlusCLatexExportTest.generate(
				PreG9BR6PlusCLatexExportTest.exporter(app, "pgf"), view);
		String pstricks = PreG9BR6PlusCLatexExportTest.generate(
				PreG9BR6PlusCLatexExportTest.exporter(app, "pstricks"), view);
		String asymptote = PreG9BR6PlusCLatexExportTest.generate(
				PreG9BR6PlusCLatexExportTest.exporter(app, "asymptote"), view);
		return new int[] {count(pgf, "-- cycle;"), count(pstricks, "\\pspolygon"),
				count(asymptote, "--cycle")};
	}

	/** Shows only the element with the label, or nothing when the label is {@code null}. */
	private static void showOnly(AppGeoCeDG app, String label) {
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			geo.setEuclidianVisible(label != null && label.equals(geo.getLabelSimple()));
		}
	}

	private static int count(String text, String fragment) {
		int count = 0;
		for (int index = text.indexOf(fragment); index >= 0;
				index = text.indexOf(fragment, index + fragment.length())) {
			count++;
		}
		return count;
	}

	/** Compiles the documents of the three loci shown together, where the tools exist. */
	private void compile(AppGeoCeDG app, EuclidianView view) throws Exception {
		Map<String, String> documents = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			for (GeoElement geo : app.getKernel().getConstruction()
					.getGeoSetConstructionOrder()) {
				String label = geo.getLabelSimple();
				geo.setEuclidianVisible("o".equals(label) || "e_1".equals(label)
						|| "e_2".equals(label));
			}
			Map<String, String> texts = new LinkedHashMap<>();
			for (String dialect : new String[] {"pgf", "pstricks"}) {
				texts.put(dialect, PreG9BR6PlusCLatexExportTest.generate(
						PreG9BR6PlusCLatexExportTest.exporter(app, dialect), view));
			}
			return texts;
		});
		Path pdflatex = tool("pdflatex");
		Path latex = tool("latex");
		Path dvips = tool("dvips");
		if (pdflatex != null) {
			Path dir = Files.createDirectories(work.resolve("pgf"));
			Files.writeString(dir.resolve("p.tex"), documents.get("pgf"));
			run(dir, pdflatex.toString(), "-interaction=nonstopmode", "-halt-on-error", "p.tex");
			assertTrue(Files.size(dir.resolve("p.pdf")) > 0);
		} else {
			System.out.println("V-TEX-1 pdflatex UNAVAILABLE");
		}
		if (latex != null && dvips != null) {
			Path dir = Files.createDirectories(work.resolve("pstricks"));
			Files.writeString(dir.resolve("t.tex"), documents.get("pstricks"));
			run(dir, latex.toString(), "-interaction=nonstopmode", "-halt-on-error", "t.tex");
			run(dir, dvips.toString(), "-q", "t.dvi", "-o", "t.ps");
			assertTrue(Files.size(dir.resolve("t.ps")) > 0);
		} else {
			System.out.println("V-TEX-1 latex/dvips UNAVAILABLE");
		}
	}

	private static void run(Path dir, String... command) throws Exception {
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
