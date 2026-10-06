/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.define;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.exportPoints;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.sized;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geocedg.common.export.DxfEncodingResult;
import org.geocedg.common.export.GeometryExportArea;
import org.geocedg.common.export.GeometryExportContext;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportPreflight;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.common.kernel.layers.HiddenLayerSet;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geocedg.desktop.export.DxfExportPreflightPresentation;
import org.geocedg.desktop.export.DxfFidelityManifestWriter;
import org.geocedg.desktop.export.DxfPreparedOutput;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.CommandLineArguments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

/**
 * PRE-G9B-R6-plus-C DXF Desktop contracts: the controller's export context
 * (unit state, persistent hidden layers, export area of the active Graphics view;
 * DQ-C2, DQ-C13), the version-2 sidecar (DQ-C11), header and sidecar agreement,
 * the usm rule, the reopened hidden layers (T-A2-REGRESSION) and unchanged
 * construction XML (T-COORDINATES).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusCDxfDesktopTest {
	private final GeometryExportService service = new GeometryExportService();

	@TempDir
	Path temporary;

	// ---------------------------------------------------------- export context

	@Test
	void theControllerReadsUnitHiddenLayersAndTheExplicitAreaOnly() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		GeoCeDGDxfExportController controller = new GeoCeDGDxfExportController(app);
		GeometryExportContext initial = controller.exportContext();
		assertEquals(GeometryExportArea.visibleViewportFallback(), initial.getArea());
		assertFalse(initial.hasAreaBoundary());
		SwingUtilities.invokeAndWait(() -> view.setCoordSystem(10, 120, 17, 17));
		assertEquals(initial, controller.exportContext(),
				"zoom never changes a DXF context without an explicit area");
		app.getDocumentUnits().replace(UnitState.of(UnitToken.M, UnitToken.MM, null));
		app.getLayerWorkspace().setLayerHidden(5, true);
		define(app, 1, 2, 3, 4);
		GeometryExportContext context = controller.exportContext();
		assertEquals(UnitState.of(UnitToken.M, UnitToken.MM, null),
				context.getUnitState());
		assertEquals(HiddenLayerSet.of(5), context.getHiddenLayers());
		assertTrue(context.hasAreaBoundary());
		assertEquals(GeometryExportArea.Producer.MANUAL, context.getArea().getProducer());
		assertEquals(1, context.getArea().getXmin(), 0);
		assertEquals(4, context.getArea().getYmax(), 0);
		app.getExportAreaSession().clear();
		exportPoints(app, new double[] {0, 0, 2, 2});
		assertEquals(GeometryExportArea.Producer.EXPORT_POINTS_AUTOMATIC,
				controller.exportContext().getArea().getProducer());
		assertEquals("Cartesian 2D world / m ($INSUNITS 6); coordinates unchanged",
				GeoCeDGDxfExportController.unitsText(context));
		assertEquals("Cartesian 2D world / UNITLESS", GeoCeDGDxfExportController
				.unitsText(GeometryExportContext.UNSPECIFIED));
	}

	// ----------------------------------------------------------- T-DXF-SIDECAR

	@Test
	void theVersionTwoSidecarAgreesWithTheHeader() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		sized(app.getEuclidianView1());
		GeoElement inside = eval(app, "s=Segment((0,0),(1,1))");
		GeoElement outside = eval(app, "far=Segment((40,40),(41,41))");
		GeoElement hidden = eval(app, "h=Segment((0,1),(1,2))");
		hidden.setLayer(3);
		eval(app, "f(x)=x^2");
		app.getLayerWorkspace().setLayerHidden(3, true);
		app.getDocumentUnits().replace(UnitState.of(UnitToken.MM, UnitToken.CM, null));
		define(app, -1, 5, -1, 5);
		GeoCeDGDxfExportController controller = new GeoCeDGDxfExportController(app);
		List<GeoElement> sources = new ArrayList<>(List.of(inside, outside, hidden,
				app.getKernel().lookupLabel("f")));
		GeometryExportPreflight preflight = service.preflight(sources,
				SelectionMode.CURRENT_SELECTION, GeometryExportRequest.builder(0.01)
						.addDefaultSemanticDomain(new GeometryExportRequest.SemanticDomain(
								"d", -1, 1, true, true)).build(),
				controller::exportContext);
		assertTrue(preflight.isWritable());
		assertTrue(preflight.isSidecarRequired(), "the approximate function");
		assertEquals(1, preflight.getOutsideExportAreaCount());
		DxfEncodingResult encoding = service.encode(preflight);
		String dxf = encoding.getDxfText();
		assertTrue(dxf.contains("$INSUNITS\r\n70\r\n4\r\n"));
		assertTrue(dxf.contains("2\r\nGEOCEDG_L3\r\n70\r\n0\r\n62\r\n-7\r\n"));
		DxfPreparedOutput output = new DxfFidelityManifestWriter().prepare(preflight,
				encoding);
		String manifest = new String(output.getManifest().getBytes(),
				StandardCharsets.UTF_8);
		assertTrue(manifest.startsWith("{\"schema\":\"org.geocedg.dxf.fidelity-manifest\","
				+ "\"schema_version\":2,"));
		assertTrue(manifest.contains("\"source_unit\":\"mm\",\"target_unit\":\"mm\","
				+ "\"insunits\":4,"), manifest);
		assertTrue(manifest.contains("\"units\":{\"metadata_schema\":"
				+ "\"org.geocedg.dxf.unit-metadata\",\"metadata_version\":1,"
				+ "\"state\":\"physical\",\"construction_unit\":\"mm\",\"insunits\":4,"
				+ "\"meters_per_unit\":\"0.001\",\"usm\":null,"), manifest);
		assertTrue(manifest.contains("\"layers\":{\"hidden_geocedg_layers\":[3],"
				+ "\"dxf_layers_off\":[\"GEOCEDG_L3\"],"), manifest);
		assertTrue(manifest.contains("\"export_area\":{\"rule\":"
				+ "\"geocedg-export-area-participation-b1/v1\",\"resolved_producer\":"
				+ "\"manual\",\"boundary\":true,\"bounds\":{\"xmin\":-1.0,\"xmax\":5.0,"
				+ "\"ymin\":-1.0,\"ymax\":5.0},\"source_view_id\":1,"
				+ "\"outside_export_area\":1}"), manifest);
		assertTrue(manifest.contains("\"outside_export_area\":[{\"source_id\":"),
				manifest);
		assertTrue(manifest.contains("\"code\":\"hidden_layer_off\""), manifest);
		assertTrue(manifest.contains("\"code\":\"outside_export_area\""), manifest);
		assertTrue(manifest.contains("\"outside_export_area\":1,\"custom_unit_usm\":false"),
				manifest);
		DxfExportPreflightPresentation presentation = DxfExportPreflightPresentation
				.from(preflight);
		assertTrue(presentation.getSummaryText().contains("$INSUNITS=4"));
		assertTrue(presentation.getWarningsText().contains("HIDDEN_LAYER_OFF"));
		assertTrue(presentation.getWarningsText().contains("OUTSIDE_EXPORT_AREA"));
		assertTrue(presentation.getHiddenLayersText().startsWith("GEOCEDG_L3"));
	}

	// -------------------------------------------------------------- T-DXF-USM

	@Test
	void aUsmDocumentAlwaysWritesItsSidecarWithTheCanonicalFactor() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		sized(app.getEuclidianView1());
		GeoElement segment = eval(app, "s=Segment((0,0),(1,1))");
		app.getDocumentUnits().replace(UnitState.of(UnitToken.USM, null,
				UsmDefinition.of(0.0254, "inch", "in")));
		GeoCeDGDxfExportController controller = new GeoCeDGDxfExportController(app);
		GeometryExportPreflight preflight = service.preflight(List.of(segment),
				SelectionMode.CURRENT_SELECTION, GeometryExportRequest.builder(0.01)
						.build(), controller::exportContext);
		assertEquals(0, preflight.getApproximateCount(), "wholly exact");
		assertTrue(preflight.isSidecarRequired(), "usm: the sidecar is mandatory");
		DxfEncodingResult encoding = service.encode(preflight);
		assertTrue(encoding.getDxfText().contains("$INSUNITS\r\n70\r\n0\r\n"));
		DxfPreparedOutput output = new DxfFidelityManifestWriter().prepare(preflight,
				encoding);
		assertTrue(output.hasManifest());
		String manifest = new String(output.getManifest().getBytes(),
				StandardCharsets.UTF_8);
		assertTrue(manifest.contains("\"construction_unit\":\"usm\",\"insunits\":0,"
				+ "\"meters_per_unit\":\"0.0254\",\"usm\":{\"token\":\"usm\","
				+ "\"meters_per_unit\":\"0.0254\",\"name\":\"inch\",\"symbol\":\"in\"}"),
				manifest);
		assertTrue(manifest.contains("\"code\":\"custom_unit_usm\""), manifest);
		assertTrue(DxfExportPreflightPresentation.from(preflight).getWarningsText()
				.startsWith("CUSTOM_UNIT_USM"));
	}

	@Test
	void theExactOnlyFlowRefusesUsmBecauseItWritesNoSidecar() throws Exception {
		AppGeoCeDG app = G9U1TestApp.withoutWindowDispatcher(new AppGeoCeDG(
				new CommandLineArguments(new String[] {"--silent",
						"--enableExtendedDxf=false"}), new JPanel()));
		app.setErrorDialogsActive(false);
		eval(app, "s=Segment((0,0),(1,1))");
		app.getDocumentUnits().replace(UnitState.of(UnitToken.USM, null,
				UsmDefinition.of(0.3048, null, "ft")));
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			dialogs.when(() -> JOptionPane.showConfirmDialog(any(), any(), any(),
					anyInt(), anyInt())).thenReturn(JOptionPane.OK_OPTION);
			new GeoCeDGDxfExportController(app).showExportDialog();
			dialogs.verify(() -> JOptionPane.showMessageDialog(any(),
					contains("usm needs the mandatory paired fidelity sidecar"), any(),
					anyInt()));
		}
	}

	// ------------------------------------------------------- T-A2-REGRESSION

	@Test
	void reopenedHiddenLayersAreOffInDxfAndOmittedInLatex() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		sized(app.getEuclidianView1());
		GeoElement onLayer = eval(app, "h=Circle((1,1),0.6789)");
		onLayer.setLayer(12);
		eval(app, "v=Circle((2,2),0.4321)");
		assertTrue(app.getLayerWorkspace().setLayerHidden(12, true));
		File file = temporary.resolve("hidden.cedg").toFile();
		assertTrue(app.saveGeoGebraFile(file));
		AppGeoCeDG reopened = G9U1TestApp.create();
		EuclidianView view = sized(reopened.getEuclidianView1());
		assertTrue(reopened.loadFile(file, false));
		String xml = reopened.getXML();
		GeoCeDGDxfExportController controller = new GeoCeDGDxfExportController(reopened);
		List<GeoElement> sources = new ArrayList<>(reopened.getKernel().getConstruction()
				.getGeoSetConstructionOrder());
		GeometryExportPreflight preflight = service.preflight(sources,
				SelectionMode.COMPLETE_CONSTRUCTION, GeometryExportRequest.builder(0.01)
						.build(), controller::exportContext);
		String dxf = service.encode(preflight).getDxfText();
		assertTrue(dxf.contains("2\r\nGEOCEDG_L12\r\n70\r\n0\r\n62\r\n-7\r\n"), dxf);
		assertTrue(dxf.contains("40\r\n0.6789\r\n"), "the object stays in the DXF");
		String latex = PreG9BR6PlusCLatexExportTest.generate(
				PreG9BR6PlusCLatexExportTest.exporter(reopened, "pgf"), view);
		assertFalse(latex.contains("0.6789"));
		assertTrue(latex.contains("0.4321"));
		assertEquals(xml, reopened.getXML(), "exports never change the document");
		assertNull(view.getSelectionRectangle());
		assertNotNull(reopened.getKernel().lookupLabel("h"));
	}

	// ------------------------------------------------- C-SMOKE-1 (author smoke)

	/**
	 * The author's smoke export of the smoke fixture, reproduced byte for byte: the
	 * individually hidden circle stays in the DXF with group 60 = 1 inside
	 * AcDbEntity, exactly where AutoCAD writes it for an invisible entity
	 * (DQ-C3); the hidden layer 3 is an OFF layer (DQ-C2); the area leaves out
	 * only far.
	 */
	@Test
	void theSmokeHiddenCircleStaysInvisibleWithGroup60() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		Path fixture = PreG9BR6PlusCLatexExportTest.repositoryRoot().resolve(
				"models/regression/pre-g9b-r6-plus-c-export-completion/fixtures/"
						+ "c-layers-and-area.cedg");
		assertTrue(app.loadFile(fixture.toFile(), false));
		GeoElement circle = app.getKernel().lookupLabel("hiddenObject");
		assertFalse(circle.isEuclidianVisible(), "hidden individually");
		assertEquals(0, circle.getLayer());
		assertTrue(app.isLayerShown(0), "not by a hidden layer");
		assertEquals(HiddenLayerSet.of(3), app.getDocumentHiddenLayers());
		GeoCeDGDxfExportController controller = new GeoCeDGDxfExportController(app);
		List<GeoElement> sources = new ArrayList<>(app.getKernel().getConstruction()
				.getGeoSetConstructionOrder());
		GeometryExportPreflight preflight = service.preflight(sources,
				SelectionMode.COMPLETE_CONSTRUCTION, GeometryExportRequest.builder(0.001)
						.allowApproximation(true).allowPartialOutput(false)
						.requestSidecar(true).build(), controller::exportContext);
		DxfEncodingResult encoding = service.encode(preflight);
		String dxf = encoding.getDxfText();
		assertEquals("33bdfca96dabbe95e8a330bee30c50fa20acb017249b08ab3265ad27db2db9f7",
				PreG9BR6PlusCBaseIdentityTest.sha256(dxf), "the author's smoke file");
		assertTrue(dxf.contains("0\r\nCIRCLE\r\n5\r\n103\r\n100\r\nAcDbEntity\r\n8\r\n0\r\n"
				+ "420\r\n0\r\n999\r\nGeoCeDG source geo-3-hiddenObject\r\n60\r\n1\r\n"
				+ "100\r\nAcDbCircle\r\n"), "60 = 1 within AcDbEntity, before AcDbCircle");
		assertTrue(dxf.contains("0\r\nLAYER\r\n2\r\nGEOCEDG_L3\r\n70\r\n0\r\n62\r\n-7\r\n"),
				"the hidden layer is OFF");
		assertTrue(dxf.contains("8\r\nGEOCEDG_L3\r\n420\r\n0\r\n999\r\nGeoCeDG source "
				+ "geo-1-onHiddenLayer\r\n100\r\nAcDbLine\r\n"),
				"a hidden layer is not object visibility: no 60");
		assertTrue(dxf.contains("8\r\n0\r\n420\r\n0\r\n999\r\nGeoCeDG source geo-0-inside"
				+ "\r\n100\r\nAcDbLine\r\n"), "a visible object stays visible");
		assertFalse(dxf.contains("geo-6-far"), "outside the explicit export area");
		String manifest = new String(new DxfFidelityManifestWriter().prepare(preflight,
				encoding).getManifest().getBytes(), StandardCharsets.UTF_8);
		assertTrue(manifest.contains("\"source_label\":\"hiddenObject\""), manifest);
		assertTrue(manifest.matches("(?s).*\"source_label\":\"hiddenObject\"[^}]*"
				+ "\"visible\":false.*"), "the sidecar records the hidden object");
		assertTrue(manifest.contains("\"hidden_geocedg_layers\":[3],"
				+ "\"dxf_layers_off\":[\"GEOCEDG_L3\"]"), manifest);
	}

	// ------------------------------------------------------------- C-UX-2

	@Test
	void theSaveDialogProposesTheDocumentName() throws Exception {
		assertEquals("MyConstruction.dxf", GeoCeDGDxfExportController.defaultDxfFileName(
				new File("C:/work/MyConstruction.cedg")));
		assertEquals("Example.dxf", GeoCeDGDxfExportController.defaultDxfFileName(
				new File("Example.ggb")));
		assertEquals("a.b.dxf", GeoCeDGDxfExportController.defaultDxfFileName(
				new File("a.b.cedg")));
		assertEquals("plain.dxf", GeoCeDGDxfExportController.defaultDxfFileName(
				new File("plain")));
		assertEquals("geocedg-export.dxf",
				GeoCeDGDxfExportController.defaultDxfFileName(null), "untitled");
		AppGeoCeDG app = G9U1TestApp.create();
		assertEquals("geocedg-export.dxf",
				new GeoCeDGDxfExportController(app).defaultDxfFileName(), "never saved");
		Path fixture = PreG9BR6PlusCLatexExportTest.repositoryRoot().resolve(
				"models/regression/pre-g9b-r6-plus-c-export-completion/fixtures/"
						+ "c-units-mm.cedg");
		assertTrue(app.loadFile(fixture.toFile(), false));
		File document = app.getCurrentFile();
		assertEquals("c-units-mm.dxf", new GeoCeDGDxfExportController(app)
				.defaultDxfFileName());
		assertEquals(document, app.getCurrentFile(), "the document file is unchanged");
	}
}
