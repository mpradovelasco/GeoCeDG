/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.sized;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.geocedg.common.export.DxfEncodingResult;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportPreflight;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.common.export.SemanticExportClassification.ExportClass;
import org.geocedg.common.export.SourceExportOutcome;
import org.geocedg.desktop.export.DxfFidelityManifestWriter;
import org.geocedg.desktop.export.LatexSemanticExportSupport;
import org.geocedg.desktop.export.LatexSemanticExporter;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.export.pstricks.GeoGebraExport;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-CU2 step T10 (A2.6; {@code dxf-curve-fidelity-and-approximation.md} section 10;
 * {@code latex-semantic-curve-export.md} section 5): a certified component with an excluded
 * end is exported, not dropped; what its path does not represent is declared in the DXF
 * sidecar (schema version 3) and in the LaTeX comments, and the export is incomplete.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu2ExportDeclarationTest {
	private final GeometryExportService service = new GeometryExportService();

	/**
	 * The open lower end of an explicit-domain locus is approached from inside and declared:
	 * the strict DXF request stays writable with its mandatory sidecar, which lists the
	 * declaration on the outcome and as a warning; the PGF code carries it in the component
	 * comment and is classified incomplete.
	 */
	@Test
	void anApproachedOpenEndIsDeclaredInTheSidecarAndTheComments() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		final EuclidianView view = sized(app.getEuclidianView1());
		eval(app, "s=0");
		GeoElement point = eval(app, "Q=(s,s^2)");
		point.setEuclidianVisible(false);
		point.updateRepaint();
		eval(app, "D={false,{-2,-1,false,true}}");
		GeoElement locus = eval(app, "L=LocusV2(Q,s,D)");
		GeoCeDGDxfExportController controller = new GeoCeDGDxfExportController(app);
		GeometryExportPreflight preflight = service.preflight(List.of(locus),
				SelectionMode.CURRENT_SELECTION, GeometryExportRequest.builder(0.01).build(),
				controller::exportContext);
		assertTrue(preflight.isWritable(), "an open end no longer drops the component");
		assertTrue(preflight.isSidecarRequired());
		SourceExportOutcome outcome = preflight.getModel().getOutcomes().get(0);
		assertTrue(outcome.isEmitted());
		assertEquals(1, outcome.getDeclarations().size(), outcome.getDeclarations().toString());
		assertTrue(outcome.getDeclarations().get(0).startsWith("OPEN_END_APPROACHED"));
		DxfEncodingResult encoding = service.encode(preflight);
		String manifest = new String(new DxfFidelityManifestWriter().prepare(preflight,
				encoding).getManifest().getBytes(), StandardCharsets.UTF_8);
		assertTrue(manifest.contains("\"schema_version\":3"), manifest);
		assertTrue(manifest.contains("\"declarations\":[\"OPEN_END_APPROACHED"), manifest);
		assertTrue(manifest.contains("\"code\":\"representation_declaration\""), manifest);
		assertTrue(manifest.contains("\"declared_components\":0"), manifest);

		GeoGebraExport export = PreG9BR6PlusCLatexExportTest.exporter(app, "pgf");
		String code = PreG9BR6PlusCLatexExportTest.generate(export, view);
		LatexSemanticExportSupport.Result result = ((LatexSemanticExporter) export)
				.getSemanticSupport().getLastResult();
		assertEquals(ExportClass.INCOMPLETE_WITH_CERTIFIED_COMPONENTS,
				result.getExportClass(), result.reportText());
		assertTrue(code.contains("; declared OPEN_END_APPROACHED: the excluded lower end -2.0"),
				code);
		assertTrue(result.reportText().contains("declared OPEN_END_APPROACHED"),
				result.reportText());
	}

	/**
	 * Consumption design section 5.6: the definition inspector shows the declared fidelity of
	 * the drawing of the active view, with its typed marks.
	 */
	@Test
	void theInspectorShowsTheDrawingFidelity() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		eval(app, "s=0");
		eval(app, "Q=(s,s^2)");
		eval(app, "D={false,{-2,-1,false,true}}");
		GeoElement locus = eval(app, "L=LocusV2(Q,s,D)");
		view.updateAllDrawables(true);
		String details = GeoCeDGDefinitionInspector.semanticDetails(app, locus);
		assertTrue(details.contains("drawing fidelity: PARTIAL (locus-render-policy/v2)"),
				details);
		assertTrue(details.contains("OPEN_END_APPROACHED"), details);
	}
}
