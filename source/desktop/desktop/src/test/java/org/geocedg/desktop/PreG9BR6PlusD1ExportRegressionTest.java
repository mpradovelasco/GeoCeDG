/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.export.GeometryExportModel;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.export.pstricks.ExportFrameMinimal;
import org.geogebra.common.export.pstricks.GeoGebraExport;
import org.geogebra.common.export.pstricks.GeoGebraToAsymptote;
import org.geogebra.common.export.pstricks.GeoGebraToPgf;
import org.geogebra.common.export.pstricks.GeoGebraToPstricks;
import org.geogebra.desktop.export.pstricks.ExportGraphicsFactoryD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R6-plus-D1 T-EXPORT-REGRESSION: D1 does not reinterpret the current export
 * semantics. For every unit state the picture, DXF and LaTeX outputs are identical to
 * the unit-free document; DXF stays unitless (C owns the future unit header).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusD1ExportRegressionTest {

	private static List<Object> outputs(AppGeoCeDG app) throws Exception {
		EuclidianView view = app.getEuclidianView1();
		List<Object> outputs = new ArrayList<>();
		BufferedImage png = PreG9BR6PlusBPictureFidelityTest.png(app, view, 1);
		outputs.add(png.getRGB(0, 0, png.getWidth(), png.getHeight(), null, 0,
				png.getWidth()));
		outputs.add(png.getWidth() + "x" + png.getHeight());
		// Generated clip-path ids differ between any two exports; they carry no content.
		outputs.add(PreG9BR6PlusBPictureFidelityTest.svg(app, view, 0.0254)
				.replaceAll("clip[0-9a-f-]+", "clip"));
		GeometryExportService service = new GeometryExportService();
		GeometryExportModel model = service.createModel(new ArrayList<>(app.getKernel()
				.getConstruction().getGeoSetConstructionOrder()),
				GeometryExportModel.SelectionMode.COMPLETE_CONSTRUCTION);
		outputs.add(service.exportDxf(model));
		outputs.add(model.getSourceUnit() + "/" + model.getTargetUnit());
		for (GeoGebraExport export : new GeoGebraExport[] {
				new GeoGebraToPgf(app, new ExportGraphicsFactoryD()),
				new GeoGebraToPstricks(app, new ExportGraphicsFactoryD()),
				new GeoGebraToAsymptote(app, new ExportGraphicsFactoryD())}) {
			ExportFrameMinimal frame = new ExportFrameMinimal(view.getYmin(), view.getYmax());
			frame.setKeepColor();
			export.setFrame(frame);
			export.generateAllCode();
			outputs.add(frame.getCode());
		}
		return outputs;
	}

	@Test
	void everyExportIsIndependentOfTheUnitState() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getKernel().setContinuous(false);
		PreG9BR6PlusBPictureFidelityTest.sized(app.getEuclidianView1());
		for (String command : new String[] {"A=(0,0)", "B=(3,1)", "s=Segment(A,B)",
				"c=Circle(A,2)", "poly=Polygon((1,1),(2,1),(2,2))", "f(x)=x^2/4"}) {
			G9U1TestApp.eval(app, command);
		}
		List<Object> baseline = outputs(app);
		assertTrue(((String) baseline.get(3)).contains("$INSUNITS\r\n70\r\n0\r\n"),
				"DXF stays unitless");
		assertEquals("UNITLESS/UNITLESS", baseline.get(4), "the UNITLESS fidelity contract");
		UnitState[] states = {UnitState.of(UnitToken.MM, null, null),
				UnitState.of(UnitToken.CM, UnitToken.MM, null),
				UnitState.of(UnitToken.M, null, null),
				UnitState.of(UnitToken.USM, UnitToken.CM, UsmDefinition.of(0.0254, "in", "in")),
				UnitState.of(null, null, UsmDefinition.of(0.3048, null, "ft"))};
		for (UnitState state : states) {
			app.getDocumentUnits().replace(state);
			List<Object> outputs = outputs(app);
			assertArrayEquals((int[]) baseline.get(0), (int[]) outputs.get(0), "PNG " + state);
			for (int i = 1; i < baseline.size(); i++) {
				assertEquals(baseline.get(i), outputs.get(i), "output " + i + " " + state);
			}
		}
	}
}
