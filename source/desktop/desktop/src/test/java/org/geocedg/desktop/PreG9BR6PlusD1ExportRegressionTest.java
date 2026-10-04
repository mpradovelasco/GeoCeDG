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
 * PRE-G9B-R6-plus-D1 T-EXPORT-REGRESSION, as superseded by PRE-G9B-R6-plus-C
 * (T-D1-REGRESSION): exports never change the unit state, its XML or the
 * coordinates; since C the DXF declares the construction unit and outputs without
 * an explicit device scale are physical (covered by the C focal tests).
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

	/**
	 * PRE-G9B-R6-plus-C replaces the D1 pin "every export is independent of the
	 * unit state" on purpose: DXF now declares the construction unit, while
	 * explicit device-scale outputs and every model coordinate stay identical
	 * and no export changes the unit state, its XML or the undo history.
	 */
	@Test
	void exportsKeepModelCoordinatesAndOnlyDxfDeclaresTheUnit() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getKernel().setContinuous(false);
		PreG9BR6PlusBPictureFidelityTest.sized(app.getEuclidianView1());
		for (String command : new String[] {"A=(0,0)", "B=(3,1)", "s=Segment(A,B)",
				"c=Circle(A,2)", "poly=Polygon((1,1),(2,1),(2,2))", "f(x)=x^2/4"}) {
			G9U1TestApp.eval(app, command);
		}
		List<Object> baseline = outputs(app);
		assertTrue(((String) baseline.get(3)).contains("$INSUNITS\r\n70\r\n0\r\n"),
				"an unspecified document stays unitless");
		assertEquals("UNITLESS/UNITLESS", baseline.get(4), "the UNITLESS contract");
		UnitState[] states = {UnitState.of(UnitToken.MM, null, null),
				UnitState.of(UnitToken.CM, UnitToken.MM, null),
				UnitState.of(UnitToken.M, null, null),
				UnitState.of(UnitToken.USM, UnitToken.CM, UsmDefinition.of(0.0254, "in", "in")),
				UnitState.of(null, null, UsmDefinition.of(0.3048, null, "ft"))};
		String[] units = {"MM/MM", "CM/CM", "M/M", "USM/USM", "UNITLESS/UNITLESS"};
		int[] codes = {4, 5, 6, 0, 0};
		for (int index = 0; index < states.length; index++) {
			UnitState state = states[index];
			app.getDocumentUnits().replace(state);
			final String xml = app.getXML();
			List<Object> outputs = outputs(app);
			assertArrayEquals((int[]) baseline.get(0), (int[]) outputs.get(0), "PNG " + state);
			assertEquals(baseline.get(1), outputs.get(1), "explicit device size " + state);
			assertEquals(baseline.get(2), outputs.get(2), "explicit SVG size " + state);
			String dxf = (String) outputs.get(3);
			assertTrue(dxf.contains("$INSUNITS\r\n70\r\n" + codes[index] + "\r\n"),
					"DXF unit header " + state);
			assertEquals(baseline.get(3), dxf
					.replace("$INSUNITS\r\n70\r\n" + codes[index] + "\r\n",
							"$INSUNITS\r\n70\r\n0\r\n")
					.replaceAll("999\r\nGeoCeDG construction unit usm[^\r]*\r\n", ""),
					"DXF coordinates and entities unchanged " + state);
			assertEquals(units[index], outputs.get(4), "neutral unit " + state);
			for (int i = 5; i < baseline.size(); i++) {
				assertEquals(baseline.get(i), outputs.get(i),
						"host LaTeX with explicit device units " + i + " " + state);
			}
			assertEquals(state, app.getDocumentUnits().getState(), "unit state " + state);
			assertEquals(xml, app.getXML(), "document XML " + state);
		}
	}
}
