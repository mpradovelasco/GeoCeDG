/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.geocedg.common.export.GeometryExportModel.ArcGeometry;
import org.geocedg.common.export.GeometryExportModel.CircleGeometry;
import org.geocedg.common.export.GeometryExportModel.EllipseGeometry;
import org.geocedg.common.export.GeometryExportModel.Entity;
import org.geocedg.common.export.GeometryExportModel.Geometry;
import org.geocedg.common.export.GeometryExportModel.LinearGeometry;
import org.geocedg.common.export.GeometryExportModel.Point2D;
import org.geocedg.common.export.GeometryExportModel.PointGeometry;
import org.geocedg.common.export.GeometryExportModel.PolylineGeometry;

/** Deterministic ASCII DXF AC1015 encoder over the neutral export model. */
public final class DxfExporter {

	/** DXF database version written by G5. */
	public static final String ACAD_VERSION = "AC1015";
	private static final String NEW_LINE = "\r\n";

	/**
	 * @param model neutral model
	 * @return complete ASCII DXF text
	 */
	public String export(GeometryExportModel model) {
		if (model == null) {
			throw new IllegalArgumentException("Geometry export model is required");
		}
		DxfPairs out = new DxfPairs();
		writeHeader(out);
		writeTables(out, model);
		out.pair(0, "SECTION");
		out.pair(2, "ENTITIES");
		int handle = 0x100;
		for (Entity entity : model.getEntities()) {
			writeEntity(out, entity, Integer.toHexString(handle++).toUpperCase());
		}
		out.pair(0, "ENDSEC");
		out.pair(0, "EOF");
		return out.toString();
	}

	private static void writeHeader(DxfPairs out) {
		out.pair(999, "GeoCeDG neutral 2D geometry export; exact G5 entities only");
		out.pair(0, "SECTION");
		out.pair(2, "HEADER");
		out.pair(9, "$ACADVER");
		out.pair(1, ACAD_VERSION);
		out.pair(9, "$INSUNITS");
		out.pair(70, 0);
		out.pair(0, "ENDSEC");
	}

	private static void writeTables(DxfPairs out, GeometryExportModel model) {
		Set<String> layers = new LinkedHashSet<>();
		layers.add("0");
		for (Entity entity : model.getEntities()) {
			layers.add(entity.getLayer());
		}
		out.pair(0, "SECTION");
		out.pair(2, "TABLES");
		out.pair(0, "TABLE");
		out.pair(2, "LTYPE");
		out.pair(70, 1);
		out.pair(0, "LTYPE");
		out.pair(2, "CONTINUOUS");
		out.pair(70, 0);
		out.pair(3, "Solid line");
		out.pair(72, 65);
		out.pair(73, 0);
		out.pair(40, 0);
		out.pair(0, "ENDTAB");
		out.pair(0, "TABLE");
		out.pair(2, "LAYER");
		out.pair(70, layers.size());
		for (String layer : layers) {
			out.pair(0, "LAYER");
			out.pair(2, layer);
			out.pair(70, 0);
			out.pair(62, 7);
			out.pair(6, "CONTINUOUS");
		}
		out.pair(0, "ENDTAB");
		out.pair(0, "ENDSEC");
	}

	private static void writeEntity(DxfPairs out, Entity entity, String handle) {
		Geometry geometry = entity.getGeometry();
		switch (geometry.getType()) {
		case POINT:
			writePoint(out, entity, handle, (PointGeometry) geometry);
			break;
		case SEGMENT:
			writeSegment(out, entity, handle, (LinearGeometry) geometry);
			break;
		case RAY:
			writeUnboundedLine(out, entity, handle, "RAY", "AcDbRay",
					(LinearGeometry) geometry);
			break;
		case INFINITE_LINE:
			writeUnboundedLine(out, entity, handle, "XLINE", "AcDbXline",
					(LinearGeometry) geometry);
			break;
		case CIRCLE:
			writeCircle(out, entity, handle, (CircleGeometry) geometry);
			break;
		case ARC:
			writeArc(out, entity, handle, (ArcGeometry) geometry);
			break;
		case ELLIPSE:
			writeEllipse(out, entity, handle, (EllipseGeometry) geometry);
			break;
		case POLYLINE:
			writePolyline(out, entity, handle, (PolylineGeometry) geometry);
			break;
		default:
			throw new IllegalArgumentException("Unsupported neutral geometry: "
					+ geometry.getType());
		}
	}

	private static void writePoint(DxfPairs out, Entity entity, String handle,
			PointGeometry geometry) {
		commonEntity(out, entity, handle, "POINT");
		out.pair(100, "AcDbPoint");
		point(out, 10, geometry.getPoint());
	}

	private static void writeSegment(DxfPairs out, Entity entity, String handle,
			LinearGeometry geometry) {
		commonEntity(out, entity, handle, "LINE");
		out.pair(100, "AcDbLine");
		point(out, 10, geometry.getStart());
		point(out, 11, geometry.getVector());
	}

	private static void writeUnboundedLine(DxfPairs out, Entity entity,
			String handle, String dxfType, String subclass, LinearGeometry geometry) {
		commonEntity(out, entity, handle, dxfType);
		out.pair(100, subclass);
		point(out, 10, geometry.getStart());
		point(out, 11, geometry.getVector());
	}

	private static void writeCircle(DxfPairs out, Entity entity, String handle,
			CircleGeometry geometry) {
		commonEntity(out, entity, handle, "CIRCLE");
		out.pair(100, "AcDbCircle");
		point(out, 10, geometry.getCenter());
		out.pair(40, geometry.getRadius());
	}

	private static void writeArc(DxfPairs out, Entity entity, String handle,
			ArcGeometry geometry) {
		commonEntity(out, entity, handle, "ARC");
		out.pair(100, "AcDbCircle");
		point(out, 10, geometry.getCenter());
		out.pair(40, geometry.getRadius());
		out.pair(100, "AcDbArc");
		out.pair(50, geometry.getStartAngleDegrees());
		out.pair(51, geometry.getEndAngleDegrees());
	}

	private static void writeEllipse(DxfPairs out, Entity entity, String handle,
			EllipseGeometry geometry) {
		commonEntity(out, entity, handle, "ELLIPSE");
		out.pair(100, "AcDbEllipse");
		point(out, 10, geometry.getCenter());
		point(out, 11, geometry.getMajorAxis());
		out.pair(40, geometry.getMinorMajorRatio());
		out.pair(41, geometry.getStartParameter());
		out.pair(42, geometry.getEndParameter());
	}

	private static void writePolyline(DxfPairs out, Entity entity, String handle,
			PolylineGeometry geometry) {
		commonEntity(out, entity, handle, "LWPOLYLINE");
		out.pair(100, "AcDbPolyline");
		out.pair(90, geometry.getVertices().size());
		out.pair(70, geometry.isClosed() ? 1 : 0);
		for (Point2D vertex : geometry.getVertices()) {
			out.pair(10, vertex.getX());
			out.pair(20, vertex.getY());
		}
	}

	private static void commonEntity(DxfPairs out, Entity entity, String handle,
			String type) {
		out.pair(0, type);
		out.pair(5, handle);
		out.pair(100, "AcDbEntity");
		out.pair(8, entity.getLayer());
		out.pair(420, entity.getStyle().toTrueColor());
		out.pair(999, "GeoCeDG source " + entity.getSourceId());
		if (!entity.getStyle().isVisible()) {
			out.pair(60, 1);
		}
	}

	private static void point(DxfPairs out, int xCode, Point2D point) {
		out.pair(xCode, point.getX());
		out.pair(xCode + 10, point.getY());
		out.pair(xCode + 20, 0);
	}

	private static final class DxfPairs {
		private final List<String> values = new ArrayList<>();

		private void pair(int code, Object value) {
			values.add(Integer.toString(code));
			values.add(format(value));
		}

		private String format(Object value) {
			if (value instanceof Double) {
				double number = (Double) value;
				if (Double.isNaN(number) || Double.isInfinite(number)) {
					throw new IllegalArgumentException("DXF values must be finite");
				}
				return Double.toString(number == 0 ? 0 : number);
			}
			return String.valueOf(value);
		}

		@Override
		public String toString() {
			return String.join(NEW_LINE, values) + NEW_LINE;
		}
	}
}
