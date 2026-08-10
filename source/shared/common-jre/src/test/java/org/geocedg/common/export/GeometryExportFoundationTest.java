/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.export.GeometryExportModel.DiagnosticCode;
import org.geocedg.common.export.GeometryExportModel.Entity;
import org.geocedg.common.export.GeometryExportModel.Exactness;
import org.geocedg.common.export.GeometryExportModel.GeometryType;
import org.geocedg.common.export.GeometryExportModel.Point2D;
import org.geocedg.common.export.GeometryExportModel.PointGeometry;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportModel.Style;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;

class GeometryExportFoundationTest extends BaseUnitTest {

	private final GeometryExportService service = new GeometryExportService();

	@Test
	void exportsSyntheticGeometryWithSemanticInvariants() {
		List<GeoElement> source = syntheticGeometry();
		GeometryExportModel model = service.createModel(source,
				SelectionMode.CURRENT_SELECTION);

		assertEquals(9, model.getEntities().size());
		assertEquals(1, model.getDiagnostics().size());
		assertEquals(DiagnosticCode.UNSUPPORTED,
				model.getDiagnostics().get(0).getCode());
		assertEquals(Arrays.asList(GeometryType.POINT, GeometryType.SEGMENT,
				GeometryType.CIRCLE, GeometryType.ARC, GeometryType.POLYLINE,
				GeometryType.POLYLINE, GeometryType.INFINITE_LINE,
				GeometryType.RAY, GeometryType.ELLIPSE), types(model));

		DxfDocument dxf = DxfDocument.parse(service.exportDxf(model));
		assertEquals("AC1015", dxf.headerValue("$ACADVER"));
		assertEquals("0", dxf.headerValue("$INSUNITS"));
		assertEquals(Arrays.asList("POINT", "LINE", "CIRCLE", "ARC",
				"LWPOLYLINE", "LWPOLYLINE", "XLINE", "RAY", "ELLIPSE"),
				dxf.entityTypes());
		assertEquals(3.0, dxf.entity("LINE").doubleValue(11), 0);
		assertEquals(4.0, dxf.entity("LINE").doubleValue(21), 0);
		assertEquals(2.0, dxf.entity("CIRCLE").doubleValue(40), 0);
		assertEquals(3, dxf.entities("LWPOLYLINE").get(0).intValue(90));
		assertEquals(1, dxf.entities("LWPOLYLINE").get(0).intValue(70));
		assertEquals(1.0, directionNorm(dxf.entity("XLINE")), 1E-12);
		assertEquals(1.0, directionNorm(dxf.entity("RAY")), 1E-12);
		for (int index = 0; index < model.getEntities().size(); index++) {
			assertEquals("GeoCeDG source "
					+ model.getEntities().get(index).getSourceId(),
					dxf.entities.get(index).value(999));
		}
	}

	@Test
	void zoomAndViewportDoNotChangeDxf() {
		List<GeoElement> source = syntheticGeometry();
		String before = service.exportDxf(service.createModel(source,
				SelectionMode.CURRENT_SELECTION));
		getApp().getActiveEuclidianView().setCoordSystem(430, 270, 137, 52);
		String after = service.exportDxf(service.createModel(source,
				SelectionMode.CURRENT_SELECTION));
		assertEquals(before, after);
	}

	@Test
	void mapsLayerColorAndVisibilityWithoutInventingPhysicalStyle() {
		GeoElement segment = add("s=Segment((0,0),(2,0))");
		segment.setLayer(7);
		segment.setObjColor(GColor.newColor(12, 34, 56));
		segment.setEuclidianVisible(false);

		GeometryExportModel model = service.createModel(
				Collections.singletonList(segment), SelectionMode.CURRENT_SELECTION);
		Entity entity = model.getEntities().get(0);
		assertEquals("GEOCEDG_L7", entity.getLayer());
		assertFalse(entity.getStyle().isVisible());
		DxfEntity dxf = DxfDocument.parse(service.exportDxf(model)).entity("LINE");
		assertEquals("GEOCEDG_L7", dxf.value(8));
		assertEquals((12 << 16) | (34 << 8) | 56, dxf.intValue(420));
		assertEquals(1, dxf.intValue(60));
	}

	@Test
	void refusesImplicitApproximationForLegacyLocusAndFunctions() {
		GeoElement function = add("f(x)=x^2");
		GeometryExportModel model = service.createModel(
				Collections.singletonList(function), SelectionMode.CURRENT_SELECTION);
		assertTrue(model.getEntities().isEmpty());
		assertEquals(DiagnosticCode.UNSUPPORTED,
				model.getDiagnostics().get(0).getCode());
		assertTrue(model.getDiagnostics().get(0).getMessage()
				.contains("No exact G5 mapping"));
	}

	@Test
	void approximationMetadataCannotBeSilent() {
		assertThrows(IllegalArgumentException.class, () -> new Entity("id", "point",
				"A", "0", new Style(0, 0, 0, true), Exactness.APPROXIMATE,
				null, new PointGeometry(new Point2D(0, 0))));
	}

	private List<GeoElement> syntheticGeometry() {
		List<GeoElement> source = new ArrayList<>();
		source.add(add("A=(1,2)"));
		source.add(add("s=Segment((0,0),(3,4))"));
		source.add(add("c=Circle((5,6),2)"));
		source.add(add("a=CircleArc((0,0),(1,0),(0,1))"));
		source.add(add("p=Polygon((0,0),(4,0),(4,3))"));
		source.add(add("q=Polyline((0,0),(1,2),(3,2))"));
		source.add(add("g=Line((0,0),(2,1))"));
		source.add(add("r=Ray((1,1),(2,3))"));
		source.add(add("e=Ellipse((-2,0),(2,0),(0,3))"));
		source.add(add("f(x)=x^2"));
		return source;
	}

	private static List<GeometryType> types(GeometryExportModel model) {
		List<GeometryType> types = new ArrayList<>();
		for (Entity entity : model.getEntities()) {
			types.add(entity.getGeometry().getType());
		}
		return types;
	}

	private static double directionNorm(DxfEntity entity) {
		double x = entity.doubleValue(11);
		double y = entity.doubleValue(21);
		return Math.sqrt(x * x + y * y);
	}

	private static final class DxfDocument {
		private final List<DxfPair> pairs;
		private final List<DxfEntity> entities;

		private DxfDocument(List<DxfPair> pairs, List<DxfEntity> entities) {
			this.pairs = pairs;
			this.entities = entities;
		}

		private static DxfDocument parse(String text) {
			String[] lines = text.split("\\r?\\n");
			assertEquals(0, lines.length % 2);
			List<DxfPair> pairs = new ArrayList<>();
			for (int index = 0; index < lines.length; index += 2) {
				pairs.add(new DxfPair(Integer.parseInt(lines[index]), lines[index + 1]));
			}
			List<DxfEntity> entities = new ArrayList<>();
			boolean inEntities = false;
			List<DxfPair> current = null;
			for (int index = 0; index < pairs.size(); index++) {
				DxfPair pair = pairs.get(index);
				if (pair.code == 0 && "SECTION".equals(pair.value)
						&& index + 1 < pairs.size()
						&& "ENTITIES".equals(pairs.get(index + 1).value)) {
					inEntities = true;
					index++;
					continue;
				}
				if (inEntities && pair.code == 0 && "ENDSEC".equals(pair.value)) {
					if (current != null) {
						entities.add(new DxfEntity(current));
					}
					break;
				}
				if (inEntities && pair.code == 0) {
					if (current != null) {
						entities.add(new DxfEntity(current));
					}
					current = new ArrayList<>();
				}
				if (inEntities && current != null) {
					current.add(pair);
				}
			}
			return new DxfDocument(pairs, entities);
		}

		private String headerValue(String variable) {
			for (int index = 0; index < pairs.size() - 1; index++) {
				if (pairs.get(index).code == 9 && variable.equals(pairs.get(index).value)) {
					return pairs.get(index + 1).value;
				}
			}
			throw new AssertionError("Missing header variable " + variable);
		}

		private List<String> entityTypes() {
			List<String> types = new ArrayList<>();
			for (DxfEntity entity : entities) {
				types.add(entity.value(0));
			}
			return types;
		}

		private DxfEntity entity(String type) {
			return entities(type).get(0);
		}

		private List<DxfEntity> entities(String type) {
			List<DxfEntity> result = new ArrayList<>();
			for (DxfEntity entity : entities) {
				if (type.equals(entity.value(0))) {
					result.add(entity);
				}
			}
			assertFalse(result.isEmpty(), "Missing DXF entity " + type);
			return result;
		}
	}

	private static final class DxfEntity {
		private final List<DxfPair> pairs;

		private DxfEntity(List<DxfPair> pairs) {
			this.pairs = pairs;
		}

		private String value(int code) {
			for (DxfPair pair : pairs) {
				if (pair.code == code) {
					return pair.value;
				}
			}
			throw new AssertionError("Missing entity group " + code);
		}

		private int intValue(int code) {
			return Integer.parseInt(value(code));
		}

		private double doubleValue(int code) {
			return Double.parseDouble(value(code));
		}
	}

	private static final class DxfPair {
		private final int code;
		private final String value;

		private DxfPair(int code, String value) {
			this.code = code;
			this.value = value;
		}
	}
}
