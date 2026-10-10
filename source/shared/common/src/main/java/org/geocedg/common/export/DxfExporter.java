/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
	/** First entity handle; G5 entity handles are unchanged by the E3-R1 container. */
	private static final int FIRST_ENTITY_HANDLE = 0x100;
	private static final String MODEL_SPACE = "*Model_Space";
	private static final String PAPER_SPACE = "*Paper_Space";

	/**
	 * @param model neutral model
	 * @return complete ASCII DXF text
	 */
	public String export(GeometryExportModel model) {
		if (model != null && model.getModelVersion() >= 2) {
			throw new IllegalArgumentException(
					"G9X1 models require controlled preflight encoding");
		}
		if (model != null && !allEntitiesExact(model)) {
			throw new IllegalArgumentException(
					"Approximate geometry requires controlled preflight encoding");
		}
		return encode(model).getDxfText();
	}

	/**
	 * Encodes one validated neutral model and exposes actual DXF identities.
	 *
	 * @param model neutral model
	 * @return deterministic text and neutral-entity handle mappings
	 */
	DxfEncodingResult encode(GeometryExportModel model) {
		if (model == null) {
			throw new IllegalArgumentException("Geometry export model is required");
		}
		if (model.getModelVersion() >= 2) {
			for (SourceExportOutcome outcome : model.getOutcomes()) {
				if (!outcome.isEmitted()) {
					throw new IllegalArgumentException(
							"Strict G9X1 model contains a non-emitted component: "
									+ outcome.getComponentAddress().getComponentKey());
				}
			}
		}
		Map<String, SourceExportOutcome> emittedOutcomes = emittedOutcomes(model);
		DxfPairs out = new DxfPairs();
		writeHeader(out, allEntitiesExact(model), model);
		Structure structure = new Structure();
		writeTables(out, model, structure);
		writeBlocks(out, structure);
		out.pair(0, "SECTION");
		out.pair(2, "ENTITIES");
		int handle = FIRST_ENTITY_HANDLE;
		Map<String, DxfEncodingResult.EntityEncoding> encodings =
				new LinkedHashMap<>();
		for (Entity entity : model.getEntities()) {
			SourceExportOutcome outcome = emittedOutcomes.remove(
					entity.getNeutralEntityId());
			if (outcome == null) {
				throw new IllegalArgumentException(
						"Neutral entity has no exportable component outcome: "
								+ entity.getNeutralEntityId());
			}
			String actualHandle = Integer.toHexString(handle++).toUpperCase();
			String entityType = dxfType(entity.getGeometry());
			writeEntity(out, entity, actualHandle);
			encodings.put(entity.getNeutralEntityId(),
					new DxfEncodingResult.EntityEncoding(entity.getNeutralEntityId(),
							actualHandle, entityType));
		}
		if (!emittedOutcomes.isEmpty()) {
			throw new IllegalArgumentException(
					"Exportable outcome has no neutral entity: "
							+ emittedOutcomes.keySet().iterator().next());
		}
		out.pair(0, "ENDSEC");
		writeObjects(out, structure);
		out.pair(0, "EOF");
		return new DxfEncodingResult(model, out.toString(), encodings);
	}

	private static void writeHeader(DxfPairs out, boolean allExact,
			GeometryExportModel model) {
		out.pair(999, allExact
				? "GeoCeDG neutral 2D geometry export; exact G5 entities only"
				: "GeoCeDG neutral 2D geometry export; fidelity sidecar required");
		GeometryExportContext context = model.getContext();
		if (model.getTargetUnit() == GeometryExportModel.Unit.USM) {
			// PRE-G9B-R6-plus-C (DQ-C11): the core DXF stays unitless; the
			// paired sidecar carries the declared physical meaning.
			out.pair(999, "GeoCeDG construction unit usm (custom unit, "
					+ context.getCanonicalMetresPerUnit()
					+ " m per unit); $INSUNITS 0; physical meaning only in the "
					+ "paired fidelity sidecar");
		}
		if (context.hasAreaBoundary()) {
			// PRE-G9B-R6-plus-C (DQ-C13): B1 participation, never clipping.
			out.pair(999, "GeoCeDG export area " + GeometryExportArea.RULE_ID
					+ ": " + context.getArea().describe()
					+ "; whole sources and components meeting the closed area; "
					+ model.getAreaExclusions().size()
					+ " outside the export area, reported, not emitted");
		}
		out.pair(0, "SECTION");
		out.pair(2, "HEADER");
		out.pair(9, "$ACADVER");
		out.pair(1, ACAD_VERSION);
		out.pair(9, "$HANDSEED");
		out.pair(5, Integer.toHexString(FIRST_ENTITY_HANDLE
				+ model.getEntities().size()).toUpperCase());
		out.pair(9, "$INSUNITS");
		out.pair(70, model.getTargetUnit().getInsunitsCode());
		out.pair(0, "ENDSEC");
	}

	/**
	 * PRE-G9B-R6-plus-E3-R1: the smallest AC1015 container that AutoCAD reads and an
	 * independent auditor accepts without repairs: the nine symbol tables with
	 * handles, owners and subclass markers, the model and paper space blocks, and the
	 * root dictionary with the plot-style placeholder every R2000 layer points to.
	 * Structural handles stay below the entity handles, which keep their G5 values.
	 */
	private static void writeTables(DxfPairs out, GeometryExportModel model,
			Structure structure) {
		Set<String> layers = new LinkedHashSet<>();
		layers.add("0");
		for (Entity entity : model.getEntities()) {
			layers.add(entity.getLayer());
		}
		out.pair(0, "SECTION");
		out.pair(2, "TABLES");
		structure.emptyTable(out, "VPORT");
		String ltype = structure.table(out, "LTYPE", 3);
		for (String name : new String[] {"ByBlock", "ByLayer"}) {
			structure.record(out, "LTYPE", ltype, "AcDbLinetypeTableRecord");
			out.pair(2, name);
			out.pair(70, 0);
			out.pair(3, "");
			out.pair(72, 65);
			out.pair(73, 0);
			out.pair(40, 0.0);
		}
		structure.record(out, "LTYPE", ltype, "AcDbLinetypeTableRecord");
		out.pair(2, "CONTINUOUS");
		out.pair(70, 0);
		out.pair(3, "Solid line");
		out.pair(72, 65);
		out.pair(73, 0);
		out.pair(40, 0);
		out.pair(0, "ENDTAB");
		String layerTable = structure.table(out, "LAYER", layers.size());
		for (String layer : layers) {
			structure.record(out, "LAYER", layerTable, "AcDbLayerTableRecord");
			out.pair(2, layer);
			out.pair(70, 0);
			// PRE-G9B-R6-plus-C (DQ-C2): a persistently hidden GeoCeDG layer is an
			// OFF DXF layer (negative color); its objects stay in the file.
			out.pair(62, model.isLayerOff(layer) ? -7 : 7);
			out.pair(6, "CONTINUOUS");
			out.pair(390, structure.plotStylePlaceholder);
		}
		out.pair(0, "ENDTAB");
		String style = structure.table(out, "STYLE", 1);
		structure.record(out, "STYLE", style, "AcDbTextStyleTableRecord");
		out.pair(2, "Standard");
		out.pair(70, 0);
		out.pair(40, 0.0);
		out.pair(41, 1.0);
		out.pair(50, 0.0);
		out.pair(71, 0);
		out.pair(42, 2.5);
		out.pair(3, "txt");
		out.pair(4, "");
		out.pair(0, "ENDTAB");
		structure.emptyTable(out, "VIEW");
		structure.emptyTable(out, "UCS");
		String appid = structure.table(out, "APPID", 1);
		structure.record(out, "APPID", appid, "AcDbRegAppTableRecord");
		out.pair(2, "ACAD");
		out.pair(70, 0);
		out.pair(0, "ENDTAB");
		structure.table(out, "DIMSTYLE", 0);
		out.pair(100, "AcDbDimStyleTable");
		out.pair(71, 0);
		out.pair(0, "ENDTAB");
		String blockRecords = structure.table(out, "BLOCK_RECORD", 2);
		structure.modelSpace = structure.record(out, "BLOCK_RECORD", blockRecords,
				"AcDbBlockTableRecord");
		out.pair(2, MODEL_SPACE);
		structure.paperSpace = structure.record(out, "BLOCK_RECORD", blockRecords,
				"AcDbBlockTableRecord");
		out.pair(2, PAPER_SPACE);
		out.pair(0, "ENDTAB");
		out.pair(0, "ENDSEC");
	}

	private static void writeBlocks(DxfPairs out, Structure structure) {
		out.pair(0, "SECTION");
		out.pair(2, "BLOCKS");
		structure.block(out, MODEL_SPACE, structure.modelSpace, false);
		structure.block(out, PAPER_SPACE, structure.paperSpace, true);
		out.pair(0, "ENDSEC");
	}

	private static void writeObjects(DxfPairs out, Structure structure) {
		out.pair(0, "SECTION");
		out.pair(2, "OBJECTS");
		out.pair(0, "DICTIONARY");
		out.pair(5, structure.rootDictionary);
		out.pair(330, "0");
		out.pair(100, "AcDbDictionary");
		out.pair(281, 1);
		out.pair(3, "ACAD_PLOTSTYLENAME");
		out.pair(350, structure.plotStyleDictionary);
		out.pair(0, "ACDBDICTIONARYWDFLT");
		out.pair(5, structure.plotStyleDictionary);
		out.pair(330, structure.rootDictionary);
		out.pair(100, "AcDbDictionary");
		out.pair(281, 1);
		out.pair(3, "Normal");
		out.pair(350, structure.plotStylePlaceholder);
		out.pair(100, "AcDbDictionaryWithDefault");
		out.pair(340, structure.plotStylePlaceholder);
		out.pair(0, "ACDBPLACEHOLDER");
		out.pair(5, structure.plotStylePlaceholder);
		out.pair(330, structure.plotStyleDictionary);
		out.pair(0, "ENDSEC");
	}

	/** Deterministic handles and owners of the AC1015 container (E3-R1). */
	private static final class Structure {
		private int next = 1;
		private final String rootDictionary = allocate();
		private final String plotStyleDictionary = allocate();
		private final String plotStylePlaceholder = allocate();
		private String modelSpace;
		private String paperSpace;

		private String allocate() {
			if (next >= FIRST_ENTITY_HANDLE) {
				throw new IllegalArgumentException(
						"DXF container handles would reach the entity handles");
			}
			return Integer.toHexString(next++).toUpperCase();
		}

		private String table(DxfPairs out, String name, int count) {
			String handle = allocate();
			out.pair(0, "TABLE");
			out.pair(2, name);
			out.pair(5, handle);
			out.pair(330, "0");
			out.pair(100, "AcDbSymbolTable");
			out.pair(70, count);
			return handle;
		}

		private void emptyTable(DxfPairs out, String name) {
			table(out, name, 0);
			out.pair(0, "ENDTAB");
		}

		private String record(DxfPairs out, String type, String owner, String subclass) {
			String handle = allocate();
			out.pair(0, type);
			out.pair(5, handle);
			out.pair(330, owner);
			out.pair(100, "AcDbSymbolTableRecord");
			out.pair(100, subclass);
			return handle;
		}

		private void block(DxfPairs out, String name, String owner, boolean paper) {
			out.pair(0, "BLOCK");
			out.pair(5, allocate());
			out.pair(330, owner);
			out.pair(100, "AcDbEntity");
			if (paper) {
				out.pair(67, 1);
			}
			out.pair(8, "0");
			out.pair(100, "AcDbBlockBegin");
			out.pair(2, name);
			out.pair(70, 0);
			out.pair(10, 0.0);
			out.pair(20, 0.0);
			out.pair(30, 0.0);
			out.pair(3, name);
			out.pair(1, "");
			out.pair(0, "ENDBLK");
			out.pair(5, allocate());
			out.pair(330, owner);
			out.pair(100, "AcDbEntity");
			if (paper) {
				out.pair(67, 1);
			}
			out.pair(8, "0");
			out.pair(100, "AcDbBlockEnd");
		}
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

	private static String dxfType(Geometry geometry) {
		switch (geometry.getType()) {
		case POINT:
			return "POINT";
		case SEGMENT:
			return "LINE";
		case RAY:
			return "RAY";
		case INFINITE_LINE:
			return "XLINE";
		case CIRCLE:
			return "CIRCLE";
		case ARC:
			return "ARC";
		case ELLIPSE:
			return "ELLIPSE";
		case POLYLINE:
			return "LWPOLYLINE";
		default:
			throw new IllegalArgumentException("Unsupported neutral geometry: "
					+ geometry.getType());
		}
	}

	private static Map<String, SourceExportOutcome> emittedOutcomes(
			GeometryExportModel model) {
		Map<String, SourceExportOutcome> outcomes = new LinkedHashMap<>();
		for (SourceExportOutcome outcome : model.getOutcomes()) {
			if (!outcome.isEmitted()) {
				continue;
			}
			if (outcomes.put(outcome.getNeutralEntityId(), outcome) != null) {
				throw new IllegalArgumentException("Duplicate exportable outcome: "
						+ outcome.getNeutralEntityId());
			}
		}
		return outcomes;
	}

	private static boolean allEntitiesExact(GeometryExportModel model) {
		for (Entity entity : model.getEntities()) {
			if (entity.getExactness() != GeometryExportModel.Exactness.EXACT) {
				return false;
			}
		}
		return true;
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
