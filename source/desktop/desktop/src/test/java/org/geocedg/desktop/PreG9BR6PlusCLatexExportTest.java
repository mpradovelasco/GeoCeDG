/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.define;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.sized;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;

import org.geocedg.common.export.SemanticExportClassification.ExportClass;
import org.geocedg.common.export.SemanticExportClassification.SourceClass;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.desktop.export.DrawingScale;
import org.geocedg.desktop.export.ExportArea;
import org.geocedg.desktop.export.LatexExportPanel;
import org.geocedg.desktop.export.LatexSemanticExportSupport;
import org.geocedg.desktop.export.LatexSemanticExporter;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.export.pstricks.ExportFrameMinimal;
import org.geogebra.common.export.pstricks.GeoGebraExport;
import org.geogebra.common.export.pstricks.GeoGebraToPgf;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.desktop.export.pstricks.ExportGraphicsFactoryD;
import org.geogebra.desktop.headless.GFileHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R6-plus-C LaTeX contracts (C9, C10-C14; DQ-C4, DQ-C5, DQ-C12, DQ-C13):
 * PSTricks, PGF/TikZ and Asymptote start from the single export area and write
 * bound edits as MANUAL; they follow effective visibility; with a construction unit
 * their units are fb(c) * 100 * a / b; Locus V2 and Spline V2 are written as one
 * path per certified component with deterministic disclosure comments and the C14
 * result model; failures are never silent.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusCLatexExportTest {
	private static final String[] DIALECTS = {"pgf", "pstricks", "asymptote"};

	// ------------------------------------------------------------ T-LATEX-AREA

	@Test
	void boundsComeFromTheExportAreaAndEditsWriteManual() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		eval(app, "A=(21,11)");
		view.setSelectionRectangle(AwtFactory.getPrototype().newRectangle(10, 10, 20,
				20));
		define(app, 20, 23, 10, 13);
		for (String dialect : DIALECTS) {
			GeoGebraExport export = exporter(app, dialect);
			assertEquals(20, export.getxmin(), 0, dialect);
			assertEquals(23, export.getxmax(), 0, dialect);
			assertEquals(10, export.getymin(), 0, dialect);
			assertEquals(13, export.getymax(), 0, dialect);
			String code = generate(export, view);
			assertTrue(code.contains("21"), "the area outside the window is complete: "
					+ dialect);
		}
		view.setSelectionRectangle(null);
		GeoGebraExport export = exporter(app, "pgf");
		export.setxmin(19);
		ExportArea manual = app.getExportAreaSession().resolve(view);
		assertEquals(ExportArea.Source.MANUAL, manual.getSource());
		assertEquals(19, manual.getXmin(), 0, "a bound edit writes MANUAL");
		assertNull(view.getSelectionRectangle(), "the selection rectangle is never written");
		export.setxmax(19);
		assertEquals(23, app.getExportAreaSession().resolve(view).getXmax(), 0,
				"a zero-width rectangle never replaces the area");

		AppGeoCeDG viewport = G9U1TestApp.create();
		EuclidianView visible = sized(viewport.getEuclidianView1());
		GeoGebraExport fallback = exporter(viewport, "pgf");
		assertEquals(visible.getXmin(), fallback.getxmin(), 0,
				"without an explicit area: the visible viewport, as at the base");
		assertEquals(visible.getYmax(), fallback.getymax(), 0);
	}

	// ------------------------------------------------------ T-LATEX-VISIBILITY

	@Test
	void hiddenLayersAndHiddenObjectsAreExcludedLikeInPictures() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		final GeoElement shown = eval(app, "c1=Circle((1,1),0.4321)");
		GeoElement onLayer = eval(app, "c2=Circle((2,1),0.6789)");
		onLayer.setLayer(4);
		GeoElement hidden = eval(app, "c3=Circle((3,1),0.2468)");
		hidden.setEuclidianVisible(false);
		hidden.updateRepaint();
		app.getLayerWorkspace().setLayerHidden(4, true);
		for (String dialect : DIALECTS) {
			String code = generate(exporter(app, dialect), view);
			assertTrue(code.contains("0.4321"), dialect + " keeps the visible circle");
			assertFalse(code.contains("0.6789"), dialect + " omits the hidden layer");
			assertFalse(code.contains("0.2468"), dialect + " omits the hidden object");
		}
		app.getLayerWorkspace().setLayerHidden(4, false);
		assertTrue(generate(exporter(app, "pgf"), view).contains("0.6789"));
		assertTrue(shown.isEuclidianVisible() && onLayer.isEuclidianVisible());
	}

	// ----------------------------------------------------------- T-LATEX-UNITS

	@Test
	void physicalUnitsAreDerivedAndDeviceUnitsStayWithoutAConstructionUnit()
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		eval(app, "A=(1,1)");
		define(app, 0, 4, 0, 3);
		String device = generate(exporter(app, "pgf"), view);
		assertTrue(device.contains("x=1.0cm,y=1.0cm"), device);
		assertFalse(device.contains("GeoCeDG physical scale"));
		app.getDocumentUnits().replace(UnitState.of(UnitToken.CM, UnitToken.MM, null));
		app.setDrawingScale(DrawingScale.of(1, 2));
		String pgf = generate(exporter(app, "pgf"), view);
		// PRE-G9B-R6-plus-E3-R1: the physical unit is written in TeX points
		// (0.5 cm = 14.22637795 pt), free of the fixed-point error of "0.5cm"
		assertTrue(pgf.contains("x=14.22637795pt,y=14.22637795pt"), pgf);
		assertTrue(pgf.startsWith("% GeoCeDG physical scale: drawing scale 1:2, "
				+ "construction unit cm: 1 model unit = 0.5 cm on the output"), pgf);
		String pstricks = generate(exporter(app, "pstricks"), view);
		assertTrue(pstricks.contains("xunit=14.22637795pt,yunit=14.22637795pt"), pstricks);
		String asymptote = generate(exporter(app, "asymptote"), view);
		assertTrue(asymptote.contains("size(2.cm)"), "4 units at 0.5 cm: " + asymptote);
		app.getDocumentUnits().replace(UnitState.of(UnitToken.MM, null, null));
		assertTrue(generate(exporter(app, "pgf"), view).contains("x=1.42263780pt,y=1.42263780pt"));
		app.getDocumentUnits().replace(UnitState.of(UnitToken.MM, UnitToken.M, null));
		assertEquals(generate(exporter(app, "pgf"), view),
				generate(exporter(app, "pgf"), view), "deterministic");
		assertTrue(generate(exporter(app, "pgf"), view).contains(
				"x=1.42263780pt,y=1.42263780pt"), "presentationUnit is never a scale factor");
	}

	// -------------------------------------------------------- T-LATEX-SEMANTIC

	@Test
	void locusV2IsWrittenAsOnePathPerCertifiedComponentInEveryDialect()
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		final EuclidianView view = sized(app.getEuclidianView1());
		eval(app, "s=0");
		eval(app, "Q=(s,s^2/4)");
		eval(app, "D={false,{-2,-1,true,true},{1,2,true,true}}");
		eval(app, "L=LocusV2(Q,s,D)");
		eval(app, "t=0");
		eval(app, "P=(cos(t)+3,sin(t)+1)");
		eval(app, "E={true,{0,2*pi,true,false}}");
		eval(app, "C=LocusV2(P,t,E)");
		String pgf = generate(exporter(app, "pgf"), view);
		assertTrue(pgf.startsWith("% GeoCeDG export classification: COMPLETE; "
				+ "semantic curves (Locus V2, Spline V2): 2 source(s), tolerance 0.001 "
				+ "model units, ESTIMATED_ERROR"), pgf);
		assertTrue(pgf.contains("% GeoCeDG semantic curve L ["), pgf);
		assertTrue(pgf.contains(": COMPLETE; components emitted 2 of 2"), pgf);
		assertEquals(0, countComponents(pgf, "L"), "every component emitted");
		assertEquals(3, count(pgf, ": APPROXIMATE, ESTIMATED_ERROR achieved"),
				"one disclosed path per component: two of L and one of C");
		assertTrue(pgf.contains("-- cycle;"), "the full-period certificate closes C");
		String pstricks = generate(exporter(app, "pstricks"), view);
		assertTrue(pstricks.contains("\\pspolygon"), "closed component in PSTricks");
		assertTrue(pstricks.contains("% GeoCeDG semantic curve C ["));
		String asymptote = generate(exporter(app, "asymptote"), view);
		assertTrue(asymptote.startsWith("// GeoCeDG export classification: COMPLETE"),
				asymptote);
		assertTrue(asymptote.contains("--cycle"), "closed component in Asymptote");
		assertEquals(pgf, generate(exporter(app, "pgf"), view), "deterministic text");
	}

	@Test
	void splineV2FollowsTheSameApproximationContractAtEveryDegree() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		eval(app, "S3=SplineV2({(0,0),(1,1),(2,0),(3,1)},3)");
		eval(app, "S5=SplineV2({(0,2),(1,3),(2,2),(3,3),(4,2),(5,3)},5)");
		String pgf = generate(exporter(app, "pgf"), view);
		assertTrue(pgf.contains("% GeoCeDG semantic curve S3 ["), pgf);
		assertTrue(pgf.contains("% GeoCeDG semantic curve S5 ["), pgf);
		assertTrue(pgf.contains(": APPROXIMATE, ESTIMATED_ERROR achieved"), pgf);
		assertFalse(pgf.contains(" .. controls "), "no exact Bezier special case");
		assertFalse(pgf.contains("EXACT"), "no exactness claim because of the degree");
	}

	// -------------------------------------------------------- T-LATEX-FAILURES

	@Test
	void anIncompleteLocusIsEmittedAndReportedIncomplete() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		Path source = repositoryRoot().resolve(Paths.get("models", "regression",
				"pre-g9b-s1-dxf", "original", "TestExport1.cedg"));
		assertTrue(GFileHandler.loadXML(app, Files.newInputStream(source), false));
		String constructionBefore = app.getXML();
		GeoGebraExport export = exporter(app, "pgf");
		String code = generate(export, app.getEuclidianView1());
		LatexSemanticExportSupport.Result result = support(export).getLastResult();
		assertEquals(ExportClass.INCOMPLETE_WITH_CERTIFIED_COMPONENTS,
				result.getExportClass(), result.reportText());
		LatexSemanticExportSupport.SourceReport locus = result.getSources().stream()
				.filter(item -> "m".equals(item.getLabel())).findFirst().orElseThrow();
		assertEquals(SourceClass.INCOMPLETE_WITH_CERTIFIED_COMPONENTS,
				locus.getSourceClass());
		assertEquals(2, locus.getEmittedCount(), "locally certified components emitted");
		assertTrue(code.contains("% Incomplete: some semantic output is locally "
				+ "certified only or failed"), code);
		assertTrue(code.contains("coverage LOCALLY_CERTIFIED_GLOBAL_NOT_ESTABLISHED"),
				code);
		assertTrue(result.reportText().contains("INCOMPLETE_WITH_CERTIFIED_COMPONENTS"));
		assertEquals(constructionBefore, app.getXML(), "the construction is untouched");
	}

	@Test
	void failedComponentsAreDisclosedAndNeverBridged() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		eval(app, "A=(5,5)");
		eval(app, "s=0");
		eval(app, "Q=(s,s^2)");
		eval(app, "D={false,{-2,-1,true,true},{1,2,true,true}}");
		eval(app, "L=LocusV2(Q,s,D)");
		GeoGebraExport export = exporter(app, "pgf");
		LatexSemanticExportSupport support = support(export);
		support.setTolerance(1E-300);
		String code = generate(export, view);
		LatexSemanticExportSupport.Result result = support.getLastResult();
		LatexSemanticExportSupport.SourceReport report = result.getSources().get(0);
		assertEquals(SourceClass.NO_ADMISSIBLE_OUTPUT, report.getSourceClass(),
				result.reportText());
		assertEquals(ExportClass.INCOMPLETE_WITH_CERTIFIED_COMPONENTS,
				result.getExportClass(), "the point is other admissible content");
		assertTrue(code.contains("[WORK_LIMIT]"), code);
		assertFalse(code.contains("\\draw [") && code.contains("(-1.,1.)-- (1.,1.)"),
				"never bridged");
		assertTrue(result.reportText().contains("[WORK_LIMIT]"));

		AppGeoCeDG lonely = G9U1TestApp.create();
		EuclidianView lonelyView = sized(lonely.getEuclidianView1());
		eval(lonely, "s=0");
		GeoElement point = eval(lonely, "Q=(s,s^2)");
		point.setEuclidianVisible(false);
		point.updateRepaint();
		eval(lonely, "D={false,{-2,-1,true,true}}");
		eval(lonely, "L=LocusV2(Q,s,D)");
		GeoGebraExport only = exporter(lonely, "pgf");
		support(only).setTolerance(1E-300);
		assertEquals("", generate(only, lonelyView),
				"nothing admissible at all: no code");
		assertEquals(ExportClass.REJECTED_NO_ADMISSIBLE_OUTPUT,
				support(only).getLastResult().getExportClass());
	}

	@Test
	void invalidToleranceAndStaleSourcesRejectTheWholeExport() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		final GeoNumeric k = (GeoNumeric) eval(app, "k=1");
		eval(app, "s=0");
		eval(app, "Q=(s,s^2/4)");
		eval(app, "D={false,{-2,-k,true,true}}");
		eval(app, "L=LocusV2(Q,s,D)");
		GeoGebraExport export = exporter(app, "pgf");
		LatexSemanticExportSupport support = support(export);
		support.setTolerance(Double.NaN);
		assertEquals("", generate(export, view), "invalid tolerance: no code");
		assertEquals(ExportClass.REJECTED_NO_ADMISSIBLE_OUTPUT,
				support.getLastResult().getExportClass());
		assertTrue(support.getLastResult().getRejection().contains("tolerance"));
		support.setTolerance(-1);
		assertEquals("", generate(export, view));

		support.setTolerance(0.001);
		assertNull(support.begin());
		k.setValue(1.5);
		k.updateCascade();
		assertEquals("", support.finish(new StringBuilder("code"), "%", null),
				"a source changed during generation");
		assertTrue(support.getLastResult().getRejection()
				.startsWith("STALE_SOURCE_REVISION"));
	}

	@Test
	void aLegacyLocusKeepsTheHostOutput() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		eval(app, "c=Circle((0,0),2)");
		eval(app, "P=Point(c)");
		eval(app, "R=(x(P),y(P)/2)");
		eval(app, "loc=Locus(R,P)");
		GeoGebraToPgf host = new GeoGebraToPgf(app, new ExportGraphicsFactoryD());
		assertEquals(generate(host, view), generate(exporter(app, "pgf"), view),
				"no semantic source, no unit: the host bytes");
	}

	// --------------------------------------------------------------- T-API, report

	@Test
	void theApiAndTheDialogPanelUseTheSameSemanticExport() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		sized(app.getEuclidianView1());
		eval(app, "S=SplineV2({(0,0),(1,1),(2,0)},3)");
		AtomicReference<String> api = new AtomicReference<>();
		app.getGgbApi().exportPGF(api::set);
		assertNotNull(api.get());
		assertTrue(api.get().contains("GeoCeDG semantic curve S ["), api.get());

		GeoGebraExport export = exporter(app, "pgf");
		JComponent component = app.getExportScalePresentation().createLatexPanel(export,
				() -> { });
		LatexExportPanel panel = (LatexExportPanel) component;
		panel.getToleranceField().setText("0.01");
		assertEquals(0.01, support(export).getTolerance(), 0);
		panel.getToleranceField().setText("abc");
		assertTrue(Double.isNaN(support(export).getTolerance()));
		panel.getToleranceField().setText("0.002");
		generate(export, app.getEuclidianView1());
		assertTrue(panel.getReportText().startsWith("Export classification: COMPLETE"),
				panel.getReportText());
		assertTrue(panel.getReportText().contains("0.002 model units"));
	}

	// ------------------------------------------------------------------ helpers

	static GeoGebraExport exporter(AppGeoCeDG app, String dialect) {
		AtomicReference<GeoGebraExport> created = new AtomicReference<>();
		switch (dialect) {
		case "pgf":
			app.newGeoGebraToPgf(created::set);
			break;
		case "pstricks":
			app.newGeoGebraToPstricks(created::set);
			break;
		default:
			app.newGeoGebraToAsymptote(created::set);
			break;
		}
		assertTrue(created.get() instanceof LatexSemanticExporter);
		return created.get();
	}

	static String generate(GeoGebraExport export, EuclidianView view) {
		ExportFrameMinimal frame = new ExportFrameMinimal(view.getYmin(), view.getYmax());
		frame.setKeepColor();
		export.setFrame(frame);
		export.generateAllCode();
		return frame.getCode().replace("\r\n", "\n");
	}

	private static LatexSemanticExportSupport support(GeoGebraExport export) {
		return ((LatexSemanticExporter) export).getSemanticSupport();
	}

	private static int count(String text, String fragment) {
		int count = 0;
		for (int index = text.indexOf(fragment); index >= 0;
				index = text.indexOf(fragment, index + 1)) {
			count++;
		}
		return count;
	}

	private static int countComponents(String code, String label) {
		Matcher matcher = Pattern.compile("% GeoCeDG semantic curve " + label
				+ " \\[[^\\]]*\\]: [A-Z_]+; components emitted (\\d+) of (\\d+)")
				.matcher(code);
		assertTrue(matcher.find(), code);
		return Integer.parseInt(matcher.group(1)) - Integer.parseInt(matcher.group(2));
	}

	static Path repositoryRoot() {
		Path directory = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		while (directory != null && !Files.isDirectory(directory.resolve("models"))) {
			directory = directory.getParent();
		}
		assertNotNull(directory, "repository root");
		return directory;
	}
}
